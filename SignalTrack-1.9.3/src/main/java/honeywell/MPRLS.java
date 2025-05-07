package honeywell;
/**
 * Utility class to read pressure from Honeywell MPR series pressure sensor breakout
 * board. 
 * @author John R. Chartkoff
 */

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinEdge;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;
import com.pi4j.util.Console;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.io.IOException;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public final class MPRLS {
    
    private static final int[] MPRLS_ADDRESS_SET = {0x18, 0x30};
    
    private static final int DEFAULT_OUTPUT_MAX = 0xE66666;
    private static final int DEFAULT_OUTPUT_MIN = 0x19999A;
    private static final int DEFAULT_PSI_MAX = 25;
    private static final int DEFAULT_PSI_MIN = 0;
    private static final byte I2C_ADDR = (byte) MPRLS_ADDRESS_SET[0];
    private static final byte[] MPRLS_COMMAND_READ_PRESSURE = { (byte) 0xAA, (byte) 0x00, (byte) 0x00 };
    private static final byte MPRLS_STATUS_POWERED = 0x40; // Status SPI powered bit
    private static final byte MPRLS_STATUS_BUSY = 0x20; // Status busy bit
    private static final byte MPRLS_STATUS_FAILED = 0x04; // Status bit for integrity fail
    private static final byte MPRLS_STATUS_MATHSAT = 0x01; // Status bit for math saturation
    
    private static final double MILLIBAR_CONVERSION_FACTOR = 68.947572932; // millibar = psi * 68.947572932
    
    private static final Pin DEFAULT_EOC_PIN = RaspiPin.GPIO_00;
    private static final Pin DEFAULT_RESET_PIN = RaspiPin.GPIO_02;
    private static final int DEFAULT_I2C_BUS = I2CBus.BUS_1;
    private static final int RESET_DEBOUNCE_PERIOD = 10;
    private static final int REQUEST_PRESSURE_PERIOD = 50;
    
    private int outputMax = DEFAULT_OUTPUT_MAX;
    private int outputMin = DEFAULT_OUTPUT_MIN;
    private int psiMax = DEFAULT_PSI_MAX;
    private int psiMin = DEFAULT_PSI_MIN;
    
    private I2CDevice device = null;
    private I2CBus i2c;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private boolean shutDownRequest = false;
    private static final Logger LOG = Logger.getLogger(MPRLS.class.getName());
    private GpioController gpio;
    private GpioPinDigitalInput eoc;
    private GpioPinDigitalOutput reset;
    private Timer resetDebounceTimer;
    private Timer requestPressureTimer;
    private double currentPsi;
    private int status;
    private int rawPsi;
    private boolean debug;
    private Console console;
    
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    
    /**
     * enum of events fired by this class; 
     * 
     * A byte consisting of all status bits as follows:
     * Bit | Meaning
     *  7    Always 0
     *  6    1 = Device is powered / 0 = Device not powered
     *  5    1 = Busy flag - device is busy.
     *  4    Always 0
     *  3    Always 0
     *  2    Checksum based memory integrity test run during startup. 1 = pass / 0 = fail
     *  1    Internal math saturation error. 1 = error
     */
    public enum Event {
        /**
         * Event fired after pressure is requested and the EOC line goes low, that carries the status byte;
         */
        STATUS, 

        /**
         * The value of the 3 byte word calculated by the Honeywell MPR series pressure sensor.
         */
        RAW_PSI, 

        /**
         * Math saturation (see definition above)
         */
        MATH_SAT, 

        /**
         * Event fired on startup if memory is defective.
         */
        FAIL, 

        /**
         * Event fired on startup if no hardware errors occur.
         */
        READY, 

        /**
         * Event fired upon completion of calculation of latest pressure reading, but only if
         * there is a difference in pressure from the last reading. Event value is pressure in PSI.
         */
        VALID_PRESSURE_CHANGE }

    /**
     * This default constructor creates an instance of MPRLS with default parameters. 
     */
    public MPRLS() {
        this(I2C_ADDR, DEFAULT_I2C_BUS, DEFAULT_RESET_PIN, DEFAULT_EOC_PIN, false);
    }
    
    /**
     * This constructor allows setting of debug mode with default parameters.
     * @param debug
     */
    public MPRLS(boolean debug) {
        this(I2C_ADDR, DEFAULT_I2C_BUS, DEFAULT_RESET_PIN, DEFAULT_EOC_PIN, debug);
    }
    
    /**
     * 
     * @param address
     * @param i2cBus
     * @param resetPin
     * @param eocPin
     * @param debug
     */
    public MPRLS(byte address, int i2cBus, Pin resetPin, Pin eocPin, boolean debug) {
        this.debug = debug;
        
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override    
            public void run() {
                shutdown();
            }
        });
        
        // create Pi4J console wrapper/helper
        // (This is a utility class to abstract some of the boilerplate code)
        console = new Console();

        // print program title/header
        console.title("<-- N1CTF -->", "MPRLS Console");

        // allow for user to exit program using CTRL-C
        console.promptForExit();
        
        gpio = GpioFactory.getInstance();

        eoc = gpio.provisionDigitalInputPin(eocPin, PinPullResistance.PULL_DOWN);
        eoc.setShutdownOptions(true);
        eoc.addListener(new GpioPinListenerDigitalImpl());

        resetDebounceTimer = new Timer(true);
        requestPressureTimer = new Timer(true);

        reset = gpio.provisionDigitalOutputPin(resetPin, PinState.HIGH);
        reset.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
        reset.addListener(new GpioPinListenerDigitalImpl());
        
        executor.execute(new ConnectToI2C(address, i2cBus));
        
    }
 
    private boolean connect(byte address, int i2cBus) {
        try {
            i2c = I2CFactory.getInstance(i2cBus);
            device = i2c.getDevice(address);
            int i = device.read();
            
            return i == 64;  
                
        } catch (UnsupportedBusNumberException | NullPointerException | IOException ex) {
            LOG.log(Level.INFO, null, ex.getMessage());
            return false;
        }
        
    }
    
    /**
     * @return Returns the sensor count value which is proportional to the PSI. This value must be processed
     * through a transfer function to be converted to PSI. Use this method if you wish to process
     * the count measurement externally. See the Honeywell MPR series pressure sensor data sheet for 
     * more information.
     */
    public double getRawPsi() {
        return rawPsi;
    }
    
    /**
     *
     * @return Return PSI to MILLIBAR_CONVERSION_FACTOR constant
     */
    public static double getPsiToMilibarConversionFactor() {
        return MILLIBAR_CONVERSION_FACTOR;
    }
    
    /**
     *
     * @param psi
     * @return Millibars
     */
    public static double getMilibarsFromPsi(double psi) {
        return MILLIBAR_CONVERSION_FACTOR * psi;
    }
    
    /** 
     * @return Returns the currently used maximum output count.
     */
    public int getOutputMax() {
        return outputMax;
    }
    
    /**
     * Allows for the use of a non-default maximum output count. Used for calculation in the transfer 
     * function only. The default is 0xE66666 counts.
     * @param outputMax
     */
    public void setOutputMax(int outputMax) {
        this.outputMax = outputMax;
    }
    
    /**
     * @return The currently assigned minimum output count.
     */
    public int getOutputMin() {
        return outputMin;
    }
    
    /**
     * Allows for the use of a non-default minimum output count. Used for calculation in the transfer 
     * function only. The default is 0x19999A counts.
     * @param outputMin
     */
    public void setOutputMin(int outputMin) {
        this.outputMin = outputMin;
    }
    
    /**
     * @return The currently assigned maximum measurable PSI.
     */
    public int getPsiMax() {
        return psiMax;
    }
    
    /**
     * Allows for the use of a non-default maximum PSI. Used for calculation in the transfer function only.
     * The default is 25 PSI.
     * @param psiMax
     */
    public void setPsiMax(int psiMax) {
        this.psiMax = psiMax;
    }
    
    /**
     * @return The currently assigned minimum measurable PSI. 
     */
    public int getPsiMin() {
        return psiMin;
    }
    
    /**
     * Allows for the use of a non-default minimum PSI. Used for calculation in the transfer function only.
     * The default is 0 PSI.
     * @param psiMin
     */
    public void setPsiMin(int psiMin) {
        this.psiMin = psiMin;
    }
    
    /**
     * Stops the MPRLS sensor and stops sending events.
     * Closes the GPIO EOC and RESET lines, closes the I2C bus, and stops measurement requests.
     */
    public void stop() {
        shutdown();
    }
    
    private void reset(boolean r) {
        if (r) {
            reset.low();
            resetDebounceTimer.schedule(new ResetDebounceTimerTask(), RESET_DEBOUNCE_PERIOD);
        }
    }

    private void shutdown() {
        try {
            shutDownRequest = true;
            pcs.firePropertyChange(Event.READY.name(), null, false);
            if (gpio != null && !gpio.isShutdown()) gpio.shutdown();
            if (i2c != null) i2c.close();
            if (requestPressureTimer != null) requestPressureTimer.cancel();
            if (resetDebounceTimer != null) resetDebounceTimer.cancel();
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }
    
    private double getPressure() {
        int raw_psi;
        byte[] r = { 0, 0, 0, 0 };
        double psi = 0;
        try {
            device.read(r, 0, 4);
            
            if (debug) console.println("Status: " + asInt(r[0]));
            if (debug) console.println("Data 1: " + asInt(r[1]));
            if (debug) console.println("Data 2: " + asInt(r[2]));
            if (debug) console.println("Data 3: " + asInt(r[3]));
            
            pcs.firePropertyChange(Event.STATUS.name(), status, asInt(r[0]));
            
            status = asInt(r[0]);
            
            raw_psi = asInt(r[1]); 
            raw_psi <<= 8;
            raw_psi |= asInt(r[2]); 
            raw_psi <<=8;
            raw_psi |= asInt(r[3]);
            
            pcs.firePropertyChange(Event.RAW_PSI.name(), rawPsi, raw_psi);
            rawPsi = raw_psi;
            
            psi = ((double) raw_psi - 0x19999A) * (psiMax - psiMin);
            psi /= (double) (outputMax - outputMin);
            psi += psiMin;
            
            psi = round(psi, 3);
            
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        
        return psi;
    }
    
    /**
     * 
     * @param value
     * @param precision
     * @return A double precision number rounded to precision decimal points
     */
    public static double round (double value, int precision) {
        final int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }
    
    private int asInt(byte b) {
        int i = b;
        if (i < 0) { i = i + 256; }
        return i;
    }
    
    /**
     * Reads the status register and checks bit 6 to see if the MPRLS sensor is ready.
     * @return 1 if it is ready, and 0 if it is not. 
     */
    public boolean isReady() {
        boolean ready = false;
        try {
            byte b = (byte) device.read(); // Reads the first byte, which is the status byte.
            if ((b & MPRLS_STATUS_POWERED) == MPRLS_STATUS_POWERED) ready = true;
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        return ready;
    }
    
    /**
     * Reads the status register and checks bit 5 to see if MPRLS sensor is busy calculating 
     * a response for the latest pressure request. 
     * @return 0 if sensor is not busy, and 1 if it is still busy.
     */
    public boolean isBusy() {
        boolean busy = false;
        try {
            byte b = (byte) device.read(); // Reads the first byte, which is the status byte.
            if ((b & MPRLS_STATUS_BUSY) == MPRLS_STATUS_BUSY) busy = true;
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        return busy;
    }
    
    /**
     * Reads the status register and checks bit 0 for a math saturation condition.
     * @return 1 if last read request caused a math saturation condition, and 0 if not.
     */
    public boolean isMathSat() {
        boolean mathSat = false;
        try {
            byte b = (byte) device.read(); // Reads the first byte, which is the status byte.
            if ((b & MPRLS_STATUS_MATHSAT) == MPRLS_STATUS_MATHSAT) mathSat = true;
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        return mathSat;
    }
    
    /**
     * Reads the status register and checks if the memory integrity test passed or failed. 
     * The check is only performed on startup.
     * @return 1 if a failure occurred, and 0 if no failures occurred.
     */
    public boolean isFail() {
        boolean fail = false;
        try {
            byte b = (byte) device.read(); // Reads the first byte, which is the status byte.
            if ((b & MPRLS_STATUS_FAILED) == MPRLS_STATUS_FAILED) fail = true;
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        return fail;
    }
    
    /**
     * @return A double precision number of the PSI after it is processed through the default 
     * transfer function with a 10% to 90% calibration curve. See Honeywell MPR series data sheet for 
     * complete instructions.
     */
    public double getCurrentPsi() {
        return currentPsi;
    }
    
    /**
     * @return The pressure in millibars by multiplying the PSI by 68.947572932.
     * Identical as as getHectoPascals(). 
     */
    public double getMillibars() {
        return MILLIBAR_CONVERSION_FACTOR * getCurrentPsi();
    }
    
    /**
     * @return The pressure in HectoPascals by multiplying the PSI by 68.947572932.
     * Hectopascals are identical to getMillibars(). Included for convenience only.
     */
    public double getHectoPascals() {
        return MILLIBAR_CONVERSION_FACTOR * getCurrentPsi();
    }
    
    private class GpioPinListenerDigitalImpl implements GpioPinListenerDigital {
        
        public GpioPinListenerDigitalImpl() {}

        @Override
        public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
            if (debug) console.println(" --> GPIO PIN STATE CHANGE: " + event.getPin() + " = " + 
                    event.getEdge());
            if (event.getPin().getPin() ==  DEFAULT_EOC_PIN && event.getEdge() == PinEdge.RISING) {
                if (isMathSat()) {
                    pcs.firePropertyChange(Event.MATH_SAT.name(), null, true);
                    String s = "MATH SATURATION error - internal hardware error";
                    LOG.log(Level.SEVERE, s);
                } else if (!isBusy()) {
                    double psi = getPressure();
                    console.println("Pressure PSI = " + psi);
                    pcs.firePropertyChange(Event.VALID_PRESSURE_CHANGE.name(), getCurrentPsi(), psi);
                    currentPsi = psi;
                }
            }
        }
    }
    
    private class ConnectToI2C implements Runnable {
        private final byte address;
        private final int i2cBus;
        
        private ConnectToI2C(byte address, int i2cBus) {
            this.address = address;
            this.i2cBus = i2cBus;
        }
        
        @Override
        public void run() {
            boolean connected = connect(address, i2cBus);

            if (!connected) {
                String s = "MPRLS not found as initially specified on I2C Bus " + i2cBus + " at address " + address;
                LOG.log(Level.INFO, s);
                if (debug) console.println(s);
            }

            while (!connected) {
                for (int b : getAvailableI2CBusSet()) {
                    for (int a = 0; a < MPRLS_ADDRESS_SET.length; a++) {
                        connected = connect((byte) MPRLS_ADDRESS_SET[a], (byte) b); 
                        String s = "Checking I2C Bus " + b + " for MPRLS at address " + MPRLS_ADDRESS_SET[a];
                        LOG.log(Level.INFO, s);
                        if (debug) console.println(s);
                        if (connected) {
                            String c = "Found MPRLS Device on I2C Bus " + b + " at Address {0}" + MPRLS_ADDRESS_SET[a];
                            LOG.log(Level.INFO, c);
                            if (debug) console.println(c);
                            break;
                        }
                    }
                    if (connected) break;
                } 
            }

            reset(connected);
        }
        
    }
    
    private int[] getAvailableI2CBusSet() {
        int[] ids = {};
        try {
            ids = I2CFactory.getBusIds();
            String s = "Found the following I2C busses: {0}" + " "  + Arrays.toString(ids);
            LOG.log(Level.INFO, s);
            if (debug) console.println(s);
        } catch (IOException ex) {
            String s = "I/O error during fetch of I2C busses occurred";
            LOG.log(Level.INFO, s, ex.getMessage());
            if (debug) console.println(s);
        }
        return ids;
    }
    
    private class ResetDebounceTimerTask extends TimerTask {
        @Override
        public void run() {
            reset.high();
            if (isFail()) {
                pcs.firePropertyChange(Event.FAIL.name(), null, true);
                LOG.log(Level.INFO, "MEMORY FAILURE - MPRLS hardware failure");
            } else if (isReady()) {
                pcs.firePropertyChange(Event.READY.name(), null, true);
                requestPressureTimer.scheduleAtFixedRate(new RequestPressureTimerTask(), 
                        REQUEST_PRESSURE_PERIOD, REQUEST_PRESSURE_PERIOD);
            }
        }
    }
    
    private class RequestPressureTimerTask extends TimerTask {
        @Override
        public void run() {
            if (shutDownRequest) return;
            try {
                device.write(MPRLS_COMMAND_READ_PRESSURE);
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * @return The instance of the PropertyChangeSupport() object that is used in this class.
     */
    public PropertyChangeSupport getPropertyChangeSupport() {
        return pcs;
    }
    
    /**
     * Create a PropertyChangeListenerObject in a class that has scope of this instance to
     * and add it to this instance to listen for events that this class fires. All event propertyNames 
     * will be from the Event enum in this class. Use Event.VALID_PRESSURE_CHANGE.name(), 
     * for example, to check when a new pressure is detected.
     * @param listener - an Instance of java.beans.PropertyChangeListener()
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    /**
     * Removes the given PropertyChangeListener() from the list of listeners to notify.
     * @param listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }
	
    /**
     * Checks to see if the giver PrpertyChangeListener is in the list of listeners to me notified.
     * @param listener
     * @return
     */
    public boolean isPropertyChangeListenerRegistered(PropertyChangeListener listener) {
        return pcs.getPropertyChangeListeners().length > 0;
    }

    /**
     * 
     * @return A List of all PropertyChangeListeners() that are currently registered for notification.
     */
    public PropertyChangeListener[] getPropertyChangeListeners() {
        return pcs.getPropertyChangeListeners();
    }
    
    /**
     *
     * @param args
     */
    public static void main(final String[] args) {
        SwingUtilities.invokeLater(new RunnableImpl(args));
    }

    private static class RunnableImpl implements Runnable {

        private final String[] args;

        public RunnableImpl(String[] args) {
            this.args = args;
        }

        @Override
        public void run() {
            boolean d = true;
            byte address = I2C_ADDR;
            int i2cBus = DEFAULT_I2C_BUS;
            Pin resetPin = DEFAULT_RESET_PIN;
            Pin eocPin = DEFAULT_EOC_PIN;
            
            try {
                final Options options = new Options();
                
                options.addOption(new Option("a", "I2C Address"));
                options.addOption(new Option("b", "I2C Bus"));
                options.addOption(new Option("r", "GPIO Bus Reset Line"));
                options.addOption(new Option("e", "GPIO Bus EOC Line"));
                options.addOption(new Option("d", true, "debug"));
                
                final CommandLineParser parser = new DefaultParser();
                
                final CommandLine cmd = parser.parse(options, args);
                
                if (cmd.hasOption("a")) {
                    address = Byte.parseByte(cmd.getOptionValue("a"));
                }
                if (cmd.hasOption("b")) {
                    i2cBus = Integer.parseInt(cmd.getOptionValue("b"));
                }
                if (cmd.hasOption("r")) {
                    resetPin = RaspiPin.getPinByName(cmd.getOptionValue("r"));
                }
                if (cmd.hasOption("e")) {
                    eocPin = RaspiPin.getPinByName(cmd.getOptionValue("e"));
                }
                if (cmd.hasOption("d")) {
                    d = true;
                }
                
                new MPRLS(address, i2cBus, resetPin, eocPin, d);
                
            } catch (final ParseException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
    }

}

