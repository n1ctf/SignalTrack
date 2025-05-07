package vishay;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;
import com.pi4j.util.Console;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 *
 * @author John R. Chartkoff
 */
public class VEML6075 {

    public static final byte VEML6075_ADDR = 0x10; // I2C address (cannot be changed)
    public static final byte VEML6075_REG_CONF = 0x00; // Configuration register
    public static final byte VEML6075_REG_UVA = 0x07; // UVA band raw measurement
    public static final byte VEML6075_REG_DARK = 0x08; // Dark current (? measurement
    public static final byte VEML6075_REG_UVB = 0x09; // UVB band raw measurement
    public static final byte VEML6075_REG_UVCOMP1 = 0x0A; // UV1 compensation value
    public static final byte VEML6075_REG_UVCOMP2 = 0x0B; // UV2 compensation value
    public static final byte VEML6075_REG_ID = 0x0C; // Manufacture ID
    public static final byte VEML6075_CHIPSET_ID = 0x26;

    public static final double VEML6075_DEFAULT_UVA_A_COEFF = 2.22;         // Default for no coverglass
    public static final double VEML6075_DEFAULT_UVA_B_COEFF = 1.33;         // Default for no coverglass
    public static final double VEML6075_DEFAULT_UVB_C_COEFF = 2.95;         // Default for no coverglass
    public static final double VEML6075_DEFAULT_UVB_D_COEFF = 1.74;         // Default for no coverglass
    public static final double VEML6075_DEFAULT_UVA_RESPONSE = 0.001461;    // Default for no coverglass
    public static final double VEML6075_DEFAULT_UVB_RESPONSE = 0.002591;    // Default for no coverglass

    private static final long START_WAIT = 2000;    // milliseconds
    private static final long REQUEST_RATE = 500;   // milliseconds
    
    private I2CDevice device = null;
    private I2CBus i2c;
    
    private int uva; 
    private int uvb;
    
    private byte[] uv_conf = {0x00, 0x00};
    
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private static final Logger LOG = Logger.getLogger(VEML6075.class.getName());
    private boolean debug;
    private Console console;
    private ScheduledFuture<?> handle = null;
    private ScheduledExecutorService scheduler;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Thread connectToI2C;
    private static final int DEFAULT_I2C_BUS = I2CBus.BUS_1;

    public enum VEML6075_Integration_Time {
        VEML6075_50MS,
        VEML6075_100MS,
        VEML6075_200MS,
        VEML6075_400MS,
        VEML6075_800MS,
    }

    public enum Event {
        STATUS, FAIL, READY
    }

    public VEML6075() {
        this(VEML6075_ADDR, DEFAULT_I2C_BUS, false);
    }

    public VEML6075(boolean debug) {
        this(VEML6075_ADDR, DEFAULT_I2C_BUS, debug);
    }

    public VEML6075(byte address, int i2cBus, boolean debug) {
        this.debug = debug;

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                shutdown();
            }
        });

        if (debug) {
            console = new Console();
            console.title("<-- N1CTF -->", "SGP30 Console");
            console.promptForExit();
        }
        
        connectToI2C = new Thread(new VEML6075.ConnectToI2C(address, i2cBus));
        
        executor.execute(connectToI2C);
    }

    public void start() {
        scheduler = Executors.newScheduledThreadPool(1);
        handle = scheduler.scheduleAtFixedRate(new ReadDevice(), START_WAIT, REQUEST_RATE, TimeUnit.MILLISECONDS);
    }

    public void open() {
        
    }
    
    public void close() {
        try {
            i2c.close();
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private void shutdown() {
        scheduler.shutdownNow();
        executor.shutdownNow();
        handle.cancel(true);
        close();
    }

    public static int booleanToInt(boolean value) {
        return value ? 1 : 0;
    }

    public void setEnabled(boolean enabled) {
        if (enabled) {
            uv_conf[0] &= ~0x01;
        } else {
            uv_conf[0] = 0x01;
        }
        writeConfig(uv_conf);
    }
    
    public void setIntegrationTime(VEML6075_Integration_Time time) {
        switch (time) {
            case VEML6075_50MS:
                uv_conf[0] |= 0b000 << 4;
                break;
            case VEML6075_100MS:
                uv_conf[0] |= 0b001 << 4;
                break;
            case VEML6075_200MS:
                uv_conf[0] |= 0b010 << 4;
                break;
            case VEML6075_400MS:
                uv_conf[0] |= 0b011 << 4;
                break;
            case VEML6075_800MS:
                uv_conf[0] |= 0b100 << 4;
                break;
            default:
                break;
        }
        writeConfig(uv_conf);
    }

    public VEML6075_Integration_Time getIntegrationTime() {
        int v = uv_conf[0] >> 4;
        return VEML6075_Integration_Time.values()[v];
    }

    public void setForcedMode(boolean forcedMode) {
        if (forcedMode) {
            uv_conf[0] &= ~(1 << 1);   
        } else {
            uv_conf[0] |= 1 << 1;
        }
        writeConfig(uv_conf);
    }

    public boolean getForcedMode() {
        return uv_conf[0] >> 1 == 1;
    }

    public void setHighDynamicRange(boolean dynamicRange) {
        if (dynamicRange) {
            uv_conf[0] &= ~(1 << 3);
        } else {
            uv_conf[0] |= 1 << 3;
        }
        writeConfig(uv_conf);
    }

    public boolean getHighDynamicRange() {
        return uv_conf[0] >> 3 == 1;
    }

    public synchronized void writeDevice(byte register, int value) {
        try {
            byte[] buffer = new byte[2];
            buffer[0] |= value;
            buffer[1] |= value >> 8;
            
            device.write(register, buffer);
            if (debug) {
                LOG.log(Level.INFO, "Writing {0} to Config Register {1}", new Object[] {value, register});
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }
    
    public synchronized void writeConfig(byte[] uv_conf) {
        try {
            device.write(VEML6075_REG_CONF, uv_conf);
            if (debug) {
            	LOG.log(Level.INFO, "Writing to Config Register {0}", Arrays.toString(uv_conf));
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private class ReadDevice implements Runnable {
        
        @Override
        public synchronized void run() {
            try {
                connectToI2C.join();

                uva = readRegister(VEML6075_REG_UVA);
                
                if (debug) {
                    console.println("Output Buffer Read Result for UVA: " + uva);
                }

            } catch (InterruptedException ex) {
                LOG.log(Level.WARNING, null, ex);
                Thread.currentThread().interrupt();
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
                if (debug) {
                    LOG.log(Level.INFO, "SGP30 not found");
                }
            } else {
                setEnabled(false);
                setForcedMode(true);
                setIntegrationTime(VEML6075_Integration_Time.VEML6075_100MS);
                setHighDynamicRange(true);
                setEnabled(true);
                start();
            }
        }
    }

    private synchronized boolean connect(byte address, int i2cBus) {
        try {
            i2c = I2CFactory.getInstance(i2cBus);
            device = i2c.getDevice(address);
            uv_conf = readConfig();
            setEnabled(false);
            setHighDynamicRange(false);
            
            readRegister(VEML6075_REG_ID);
            
            readRegister(VEML6075_REG_CONF);
            
            
            
            return true;//i == VEML6075_CHIPSET_ID;
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } catch (UnsupportedBusNumberException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        return false;
    }

    private synchronized byte[] readConfig() {
        byte[] read = new byte[2];
        byte[] write = {0x00, VEML6075_REG_CONF};
        try {
            int i = device.read(write, 0, 2, read, 0, 2);
            if (debug) {
                LOG.log(Level.INFO, "Read " + i + " bytes from register: " + VEML6075_REG_CONF + " contents: " + read[1] + " " + read[0]);
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        return read;
    }
    
    private synchronized int readRegister(byte register) {
        byte[] read = new byte[2];
        byte[] write = {0x00, register};
        try {
            int i = device.read(write, 0, 2, read, 0, 2);
            if (debug) {
                LOG.log(Level.INFO, "Read " + i + " bytes from register: " + register + " contents: " + read[1] + " " + read[0]);
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        return read[1] << 8 | read[0];
    }

    private int[] getAvailableI2CBusSet() {
        int[] ids = {};
        try {
            ids = I2CFactory.getBusIds();
            if (debug) {
                LOG.log(Level.INFO, "Found the following I2C busses: {0}", Arrays.toString(ids));
            }
        } catch (IOException ex) {
            if (debug) {
                LOG.log(Level.INFO, "I/O error during fetch of I2C busses occurred", ex.getMessage());
            }
        }
        return ids;
    }

    public PropertyChangeSupport getPropertyChangeSupport() {
        return pcs;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public boolean isPropertyChangeListenerRegistered(PropertyChangeListener listener) {
        return pcs.getPropertyChangeListeners().length > 0;
    }

    public PropertyChangeListener[] getPropertyChangeListeners() {
        return pcs.getPropertyChangeListeners();
    }

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
            boolean debug = true;
            byte address = VEML6075_ADDR;
            int i2cBus = DEFAULT_I2C_BUS;

            try {
                final Options options = new Options();

                options.addOption(new Option("a", "I2C Address"));
                options.addOption(new Option("b", "I2C Bus"));
                options.addOption(new Option("d", true, "debug"));

                final CommandLineParser parser = new DefaultParser();

                final CommandLine cmd = parser.parse(options, args);

                if (cmd.hasOption("a")) {
                    address = Byte.parseByte(cmd.getOptionValue("a"));
                }
                if (cmd.hasOption("b")) {
                    i2cBus = Integer.parseInt(cmd.getOptionValue("b"));
                }
                if (cmd.hasOption("d")) {
                    debug = true;
                }

                new VEML6075(address, i2cBus, debug);

            } catch (final ParseException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
    }

}
