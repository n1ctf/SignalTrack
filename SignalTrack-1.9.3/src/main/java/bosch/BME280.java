package bosch;
/**
 * Class to read pressure from Bosch BME280 environmental sensor breakout
 * board. 
 * @author John R. Chartkoff
 */


import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import com.pi4j.util.Console;

import meteorology.AbstractEnvironmentSensor;

import time.ConsolidatedTime;

import java.io.IOException;

import java.nio.file.NoSuchFileException;

import java.text.SimpleDateFormat;

import java.util.Arrays;
import java.util.Calendar;
import java.util.TimeZone;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JPanel;

public final class BME280 extends AbstractEnvironmentSensor {
    
    public enum FilterCoefficient {
        DISABLED,
        FC2,
        FC4,
        FC8,
        FC16
    }

    public enum InterMeasurementStandByPeriod {
        US500,
        US62500,
        US125000,
        US250000,
        US500000,
        US1000000,
        US10000,
        US20000
    }

    public enum Mode {
        SLEEP_MODE,
        FORCED_MODE,
        NORMAL_MODE
    }

    public enum Oversampling {
        DISABLED,
        X1,
        X2,
        X4,
        X8,
        X16
    }

    public enum DebugLevel {
        SILENT,
        CONSOLE_ONLY,
        LOG,
        LOG_ALL

    }
    
    public enum Event {
        STATUS, 
        FAIL, 
        READY, 
        VALID_PRESSURE_CHANGE, 
        VALID_HUMIDITY_CHANGE, 
        VALID_TEMP_CHANGE
    }
    
    private static final long START_WAIT = 1000;     // milliseconds
    private static final long REQUEST_RATE = 200;    // milliseconds
    private static final long WAIT_FOR_CONNECT_TIMEOUT = 16;   // seconds
    
    private static final boolean DEFAULT_DEBUG = false;
    
    public static final String EQUIPMENT_CODE = "Bosch BME-280";
    public static final String SERIAL_NUMBER = "";
    public static final String MANUFACTURER = "Bosch";
    public static final String MODEL_NAME = "BME-280";
    public static final String SOFTWARE_VERSION = "2.2.4";
    public static final String HARDWARE_VERSION = "1.1";
    
    private static final byte[] BME280_ADDRESS_SET = {(byte) 0x76, (byte) 0x77};
    private static final byte ID_REGISTER = (byte) 0xD0;
    private static final byte RESET_REGISTER = (byte) 0xE0;
    private static final byte CTRL_HUM_REGISTER = (byte) 0xF2;
    private static final byte STATUS_REGISTER = (byte) 0xF3;
    private static final byte CTRL_MEAS_REGISTER = (byte) 0xF4;
    private static final byte CONFIG_REGISTER = (byte) 0xF5;
    
    private static final byte BME280_CHIP_ID = (byte) 0x60;
    private static final byte CMD_RESET = (byte) 0xB6;
    
    private static final byte SAMPLING_DISABLED = 0x00;
    private static final byte OVERSAMPLING_1X = 0x01;
    private static final byte OVERSAMPLING_2X = 0x02;
    private static final byte OVERSAMPLING_4X = 0x03;
    private static final byte OVERSAMPLING_8X = 0x04;
    private static final byte OVERSAMPLING_16X = 0x05;
    
    private static final byte STATUS_MEASURING = 0x08;
    private static final byte STATUS_UPDATING = 0x01;
    
    private static final byte MODE_SLEEP = 0x00;
    private static final byte MODE_FORCED = 0x01;
    private static final byte MODE_NORMAL = 0x02;

    private static final byte INTER_MEASUREMENT_STAND_BY_TIME_500US = 0x00;
    private static final byte INTER_MEASUREMENT_STAND_BY_TIME_62500US = 0x01;
    private static final byte INTER_MEASUREMENT_STAND_BY_TIME_125000US = 0x02;
    private static final byte INTER_MEASUREMENT_STAND_BY_TIME_250000US = 0x03;
    private static final byte INTER_MEASUREMENT_STAND_BY_TIME_500000US = 0x04;
    private static final byte INTER_MEASUREMENT_STAND_BY_TIME_1000000US = 0x05;
    private static final byte INTER_MEASUREMENT_STAND_BY_TIME_10000US = 0x06;
    private static final byte INTER_MEASUREMENT_STAND_BY_TIME_20000US = 0x07;
    
    private static final byte FILTER_DISABLED = 0x00;
    private static final byte FILTER_COEFFICIENT_2 = 0x01;
    private static final byte FILTER_COEFFICIENT_4 = 0x02;
    private static final byte FILTER_COEFFICIENT_8 = 0x03;
    private static final byte FILTER_COEFFICIENT_16 = 0x04;
    
    private static final byte I2C_ADDR = BME280_ADDRESS_SET[1];

    private static final double MILLIBARS_PER_PSI = 68.947572932; // millibar = psi * 68.947572932
    private static final double MILLIBARS_PER_INCHES_HG = 33.864; //devide millibars by 33.864

    private static final int DEFAULT_I2C_BUS = I2CBus.BUS_1;
       
    private static final boolean DEBUG = DEFAULT_DEBUG;   
    private static final Logger LOG = Logger.getLogger(BME280.class.getName());
    
    private Oversampling pressureOversampling = Oversampling.X16;
    private Oversampling temperatureOversampling = Oversampling.X16;
    private Oversampling humidityOversampling = Oversampling.X16;
    private Mode mode = Mode.FORCED_MODE;
    private FilterCoefficient filterCoefficient = FilterCoefficient.FC16;
    private InterMeasurementStandByPeriod interMeasurementStandByPeriod = InterMeasurementStandByPeriod.US250000;
    
    private final long[] dig_T = new long[3];
    private final long[] dig_P = new long[9];
    private final long[] dig_H = new long[6];
    
    private I2CDevice device;
    private I2CBus i2c;
    private int i2cBus;
    private byte address;
    private double pressureMillibars;
    private double tempCelsius;
    private double relativeHumidity;

    private Console console;

    private ScheduledExecutorService scheduler;
    private ExecutorService connectionExecutor = Executors.newSingleThreadExecutor();
    
    public BME280(ConsolidatedTime consolidatedTime, Boolean clearAllPreferences) {
        this(I2C_ADDR, DEFAULT_I2C_BUS, consolidatedTime, clearAllPreferences);
    }

    public BME280(byte address, int i2cBus, ConsolidatedTime consolidatedTime, Boolean clearAllPreferences) {
    	super(getUniqueDeviceId(), consolidatedTime, clearAllPreferences);
    	
    	this.address = address;
    	this.i2cBus = i2cBus;

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override    
            public void run() {
                shutdown();
            }
        });

        if (DEBUG) {
            console = new Console();
            console.title("<-- Amateur Radio Station N1CTF -->", "BME280 Console");
            console.promptForExit();
        }

    }
    
    @Override
    public void startSensor() {
    	connectionExecutor.execute(new ConnectToI2C(address, i2cBus));
    	scheduler = Executors.newScheduledThreadPool(0);
    	scheduler.scheduleAtFixedRate(new ReadDevice(), START_WAIT, REQUEST_RATE, TimeUnit.MILLISECONDS);
    }
    
    public synchronized void reset() {
        try {
            device.write(RESET_REGISTER, CMD_RESET);
        } catch (IOException | NullPointerException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    public static int booleanToInt(boolean value) {
        return value ? 1 : 0;
    }
    
    public static int concatenateBytes(byte msb, byte lsb) {
        return ((asInt(msb)) << 8) | asInt(lsb);
    }
    
    public static double celsiusToFahrenheit(double celsius) {
        return (celsius * 9D/5D) + 32D;
    }
    
    public static double millibarsToInchesOfMercury(double millibars) {
        return millibars / MILLIBARS_PER_INCHES_HG;
    }

    public static double psiToMillibars(double psi) {
        return MILLIBARS_PER_PSI * psi;
    }
    
    public static double millibarsToPSI(double millibars) {
        return millibars / MILLIBARS_PER_PSI;
    }

    public void stop() {
        shutdown();
    }

    private void shutdown() {
        try {
            pcs.firePropertyChange(Event.READY.name(), null, false);
            if (i2c != null) {
                i2c.close();
                i2c = null;
            }
            if (console != null) {
                console.clearScreen();
            }

            if (connectionExecutor != null) {
    			try {
    				LOG.log(Level.SEVERE, "Initializing BME280 Connection Executor termination....");
    				connectionExecutor.shutdown();
    				connectionExecutor.awaitTermination(20, TimeUnit.SECONDS);
    				LOG.log(Level.SEVERE, " Connection Executor has gracefully terminated");
    			} catch (InterruptedException e) {
    				LOG.log(Level.SEVERE, " Connection Executor has timed out after 20 seconds of waiting to terminate processes.");
    				Thread.currentThread().interrupt();
    			}
    		}
            
            if (scheduler != null) {
    			try {
    				LOG.log(Level.SEVERE, "Initializing BME280 Service termination....");
    				scheduler.shutdown();
    				scheduler.awaitTermination(20, TimeUnit.SECONDS);
    				LOG.log(Level.SEVERE, "BME280 Service has gracefully terminated");
    			} catch (InterruptedException e) {
    				LOG.log(Level.SEVERE, "BME280 Service has timed out after 20 seconds of waiting to terminate processes.");
    				Thread.currentThread().interrupt();
    			}
    		}
            
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    public static double round (double value, int precision) {
        final int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }
    
    public static int asInt(byte b) {
        int i = b;
        if (i < 0) { 
        	i += 256; 
        }
        return i;
    }
        
    @Override
    public double getDewPointFahrenheit() {
        return celsiusToFahrenheit(getDewPointCelsius());
    }
    
    @Override
    public double getDewPointCelsius() {
        double H = (Math.log10(getRelativeHumidity()) -2 ) / 0.4343 + (17.62 * getTempExteriorCelsius()) / (243.12 + getTempExteriorCelsius());
        return 243.12 * H / (17.62 - H); 
    }
    
    public double getRelativeHumidity() {
        return relativeHumidity;
    }

    public double getTempFahrenheit() {
        return celsiusToFahrenheit(tempCelsius);
    }
    
    public double getPressureInchesOfMercury() {
        return pressureMillibars / MILLIBARS_PER_INCHES_HG;
    }
    
    public double getPressurePSI() {
        return pressureMillibars / MILLIBARS_PER_PSI;
    }
    
    public static String getCurrentUTCTimeCode() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMMMMMMM yyyy H:mm:ss z");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(Calendar.getInstance().getTime());
    }
        
    public FilterCoefficient getFilterCoefficient() {
        return filterCoefficient;
    }

    public void setFilterCoefficient(FilterCoefficient filterCoefficient) {
        this.filterCoefficient = filterCoefficient;
    }

    public InterMeasurementStandByPeriod getInterMeasurementStandByPeriod() {
        return interMeasurementStandByPeriod;
    }

    public void setInterMeasurementStandByPeriod(InterMeasurementStandByPeriod interMeasurementStandByPeriod) {
        this.interMeasurementStandByPeriod = interMeasurementStandByPeriod;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public Oversampling getPressureOversampling() {
        return pressureOversampling;
    }

    public void setPressureOversampling(Oversampling pressureOversampling) {
        this.pressureOversampling = pressureOversampling;
    }

    public Oversampling getTemperatureOversampling() {
        return temperatureOversampling;
    }

    public void setTemperatureOversampling(Oversampling temperatureOversampling) {
        this.temperatureOversampling = temperatureOversampling;
    }

    public Oversampling getHumidityOversampling() {
        return humidityOversampling;
    }

    public void setHumidityOversampling(Oversampling humidityOversampling) {
        this.humidityOversampling = humidityOversampling;
    }

    private class ReadDevice implements Runnable {
        @Override
        public synchronized void run() {
            try {
            	connectionExecutor.shutdown();
                connectionExecutor.awaitTermination(WAIT_FOR_CONNECT_TIMEOUT, TimeUnit.SECONDS);
                
                device.write(CTRL_HUM_REGISTER, getOversampleWord(getHumidityOversampling()));
                device.write(CTRL_MEAS_REGISTER, getCtrlMeasRegisterConfigWord());
                device.write(CONFIG_REGISTER, getConfigRegisterWord());

                while ((device.read(STATUS_REGISTER) & STATUS_MEASURING) == STATUS_MEASURING
                        || (device.read(STATUS_REGISTER) & STATUS_UPDATING) == STATUS_UPDATING) {
                    try {
                        this.wait(10);
                    } catch (InterruptedException ex) {
                        LOG.log(Level.SEVERE, null, ex);
                        Thread.currentThread().interrupt();
                    }
                }
                
                byte[] buffer = new byte[8];
                
                device.read(0xF7, buffer, 0, 8);

                if (DEBUG) {
                    console.println("Output Buffer Read Result: " + Arrays.toString(buffer));
                }
                
                int temp = asInt(buffer[3]) << 12;
                temp |= asInt(buffer[4]) << 4;
                temp |= asInt(buffer[5]) >> 4;
                
                int press = asInt(buffer[0]) << 16;
                press |= asInt(buffer[1]) << 8;
                press |= asInt(buffer[2]) >> 4;
                
                int humid = asInt(buffer[6]);
                humid <<= 8;
                humid |= asInt(buffer[7]);
            
                double t = getTempCelsius(temp);
                pcs.firePropertyChange(Event.VALID_TEMP_CHANGE.name(), tempCelsius, t);
                tempCelsius = t;
                
                double p = getCompensatedPressure(press, temp);
                pcs.firePropertyChange(Event.VALID_PRESSURE_CHANGE.name(), pressureMillibars, p);
                pressureMillibars = p;
                
                double h = getCompensatedHumidity(humid, temp);
                pcs.firePropertyChange(Event.VALID_HUMIDITY_CHANGE.name(), relativeHumidity, h);
                relativeHumidity = h;

                if (DEBUG) {
                    console.println();
                    console.println("Weather Conditions for: " + getCurrentUTCTimeCode());
                    console.println("  Temperature = " + round(getTempFahrenheit(), 2) + " Degrees Fahrenheit");
                    console.println("  Barometric Pressure = " + round(getPressureInchesOfMercury(), 2) + " Inches of Mercury");
                    console.println("  Dew Point Fahrenheit = " + round(getDewPointFahrenheit(), 2) + " Degrees Fahrenheit");
                    console.println("  Relative Humidity = " + round(getRelativeHumidity(), 2) + " Percent");
                    console.println("-----------------------");
                }
            } catch (InterruptedException ex) {
                LOG.log(Level.SEVERE, null, ex);
                Thread.currentThread().interrupt();
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }  
        
        // Returns temperature in DegC, resolution is 0.01 DegC. Output value of “5123” equals 51.23 DegC.
        private double getTempCelsius(int adcTemp) {
            return (((getHighResTemp(adcTemp) * 5) + 128) >> 8) / 100D;
        }
        
        // Returns humidity in %RH as unsigned 32 bit integer in Q22.10 format (22 integer and 10 fractional bits).
        // Output value of “47445” represents 47445/1024 = 46.333 %RH
        private double getCompensatedHumidity(long adc_H, int adcTemp) {
            if (humidityOversampling == Oversampling.DISABLED) {
                return -1;
            }
            
            long v_x1_u32r = getHighResTemp(adcTemp) - 76800;
            
            v_x1_u32r = (((((adc_H << 14) - ((dig_H[3]) << 20) - ((dig_H[4]) * v_x1_u32r)) +
                (16384)) >> 15) * (((((((v_x1_u32r * (dig_H[5])) >> 10) * (((v_x1_u32r *
                (dig_H[2])) >> 11) + (32768))) >> 10) + (2097152)) *
                (dig_H[1]) + 8192) >> 14));

            v_x1_u32r = (v_x1_u32r - (((((v_x1_u32r >> 15) * (v_x1_u32r >> 15)) >> 7) * (dig_H[0])) >> 4));

            v_x1_u32r = (v_x1_u32r < 0 ? 0 : v_x1_u32r);

            v_x1_u32r = (v_x1_u32r > 419430400 ? 419430400 : v_x1_u32r);

            return (v_x1_u32r >> 12) /1024D;
        }
     
        private long getHighResTemp(long adcTemp) {
            long var1 = ((adcTemp / 8) - (dig_T[0] * 2));
            var1 = (var1 * (dig_T[1])) / 2048;
            long var2 = ((adcTemp / 16) - (dig_T[0]));
            var2 = (((var2 * var2) / 4096) * (dig_T[2])) / 16384;

            return var1 + var2;
        }
        
        // Returns pressure in Pa as unsigned 32 bit integer in Q24.8 format (24 integer bits and 8 fractional bits).
        // Output value of “24674867” represents 24674867/256 = 96386.2 Pa = 963.862 hPa
        private double getCompensatedPressure(long adcPress, int adcTemp) {
            if (pressureOversampling == Oversampling.DISABLED) {
                return -1;
            }
            
            long var1;
            long var2;
            long var3;

            var1 = getHighResTemp(adcTemp) - 128000L;
            var2 = var1 * var1 * dig_P[5];
            var2 += (var1 * dig_P[4] * 131072L);
            var2 += (dig_P[3] * 34359738368L);
            var1 = ((var1 * var1 * dig_P[2]) / 256) + (var1 * (dig_P[1]) * 4096);
            var3 = 140737488355328L;
            var1 = (var3 + var1) * (dig_P[0]) / 8589934592L;

            if (var1 == 0) {
                return -1; // avoid exception caused by division by zero
            }
          
            long var4 = 1048576L - adcPress;
            var4 = (((var4 * (2147483648L)) - var2) * 3125) / var1;
            var1 = ((dig_P[8]) * (var4 / 8192) * (var4 / 8192)) / 33554432L;

            var2 = ((dig_P[7]) * var4) / 524288L;
            var4 = ((var4 + var1 + var2) / 256) + ((dig_P[6]) * 16);
            
            return (((var4 / 2.0) * 100) / 128.0) / 25600D;
        }
        
        private byte getConfigRegisterWord() {
            byte config = 0;
            config |= getInterMeasurementStandByPeriodWord(getInterMeasurementStandByPeriod()) << 5;
            config |= getFilterCoefficientWord(getFilterCoefficient()) << 2;
            return config; 
        }
        
        private byte getCtrlMeasRegisterConfigWord() {
            byte config = 0;
            config |= getOversampleWord(getTemperatureOversampling()) << 5;
            config |= getOversampleWord(getTemperatureOversampling()) << 2;
            config |= getModeWord(getMode());
            return config;
        }

	    private byte getInterMeasurementStandByPeriodWord(InterMeasurementStandByPeriod time) {
	        switch(time) {
	            case US500: return INTER_MEASUREMENT_STAND_BY_TIME_500US;
	            case US62500: return INTER_MEASUREMENT_STAND_BY_TIME_62500US;
	            case US125000: return INTER_MEASUREMENT_STAND_BY_TIME_125000US;
	            case US250000: return INTER_MEASUREMENT_STAND_BY_TIME_250000US;
	            case US500000: return INTER_MEASUREMENT_STAND_BY_TIME_500000US;
	            case US1000000: return INTER_MEASUREMENT_STAND_BY_TIME_1000000US;
	            case US10000: return INTER_MEASUREMENT_STAND_BY_TIME_10000US;
	            case US20000: return INTER_MEASUREMENT_STAND_BY_TIME_20000US;
	            default: return INTER_MEASUREMENT_STAND_BY_TIME_500US;
	        }
	    }
	    
	    private byte getOversampleWord(Oversampling value) {
	        switch(value) {
	            case DISABLED: return SAMPLING_DISABLED;
	            case X1: return OVERSAMPLING_1X;
	            case X2: return OVERSAMPLING_2X;
	            case X4: return OVERSAMPLING_4X;
	            case X8: return OVERSAMPLING_8X;
	            case X16: return OVERSAMPLING_16X;
	            default: return SAMPLING_DISABLED;
	        }
	    } 
	    
	    private byte getModeWord(Mode mode) {
	        switch(mode) {
	            case SLEEP_MODE: return MODE_SLEEP;
	            case NORMAL_MODE: return MODE_NORMAL;
	            case FORCED_MODE: return MODE_FORCED;
	            default: return MODE_NORMAL;
	        }
	    } 
	    
	    private byte getFilterCoefficientWord(FilterCoefficient coefficient) {
	        switch(coefficient) {
	            case DISABLED: return FILTER_DISABLED;
	            case FC2: return FILTER_COEFFICIENT_2;
	            case FC4: return FILTER_COEFFICIENT_4;
	            case FC8: return FILTER_COEFFICIENT_8;
	            case FC16: return FILTER_COEFFICIENT_16;
	            default: return FILTER_DISABLED;
	        }
	    } 
    }
    
    private final class ConnectToI2C implements Runnable {
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
                LOG.log(Level.INFO, "BME280 not found as specified");

	            for (int b : getAvailableI2CBusSet()) {
	                for (int a = 0; a < BME280_ADDRESS_SET.length; a++) {
	                    LOG.log(Level.INFO, "Checking I2C Bus {0} at Address {1}", new Object[] {b, BME280_ADDRESS_SET[a]});             	
	                    connected = connect(BME280_ADDRESS_SET[a], (byte) b); 
	                    if (connected) {
	                        LOG.log(Level.INFO, "Found BME280 Device on I2C Bus {0} at Address {1}", new Object[] {b, BME280_ADDRESS_SET[a]});
	                        break;
	                    }
	                } 
	            }
            }
            
            if (connected) {
            	pcs.firePropertyChange(Event.READY.name(), null, true);
	            reset();
	            loadCompValues();
	            startSensor();
            }
        } 
        
        private void loadCompValues() {
            try {
                int dig_H3_lsb;
                int dig_H3_msb;
                int dig_H4_lsb;
                int dig_H4_msb;
                
                byte[] reg_data = new byte[33];
                
                device.read(0x88, reg_data, 0, 26);
                device.read(0xE1, reg_data, 26, 7);
                
                dig_T[0] = concatenateBytes(reg_data[1], reg_data[0]);
                dig_T[1] = concatenateBytes(reg_data[3], reg_data[2]);
                dig_T[2] = concatenateBytes(reg_data[5], reg_data[4]);
                dig_P[0] = concatenateBytes(reg_data[7], reg_data[6]);
                dig_P[1] = concatenateBytes(reg_data[9], reg_data[8]);
                dig_P[2] = concatenateBytes(reg_data[11], reg_data[10]);
                dig_P[3] = concatenateBytes(reg_data[13], reg_data[12]);
                dig_P[4] = concatenateBytes(reg_data[15], reg_data[14]);
                dig_P[5] = concatenateBytes(reg_data[17], reg_data[16]);
                dig_P[6] = concatenateBytes(reg_data[19], reg_data[18]);
                dig_P[7] = concatenateBytes(reg_data[21], reg_data[20]);
                dig_P[8] = concatenateBytes(reg_data[23], reg_data[22]);
                dig_H[0] = reg_data[25]; 
                dig_H[1] = concatenateBytes(reg_data[27], reg_data[26]);  
                dig_H[2] = reg_data[28];
                dig_H3_msb = reg_data[29] * 16;
                dig_H3_lsb = reg_data[30] & 0x0F;
                dig_H[3] = dig_H3_msb | dig_H3_lsb;
                dig_H4_msb = reg_data[31] * 16;
                dig_H4_lsb = reg_data[30] >> 4;
                dig_H[4] = dig_H4_msb | dig_H4_lsb;
                dig_H[5] = reg_data[32];
                
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
        
        private synchronized boolean connect(byte address, int i2cBus) {
            try {
                i2c = I2CFactory.getInstance(i2cBus);
                device = i2c.getDevice(address);

                int i = device.read(ID_REGISTER);
                
                return i == BME280_CHIP_ID;  
                    
            } catch (UnsupportedBusNumberException | NullPointerException | IOException ex) {
                LOG.log(Level.INFO, null, ex.getMessage());
                return false;
            }
            
        }
        
        private int[] getAvailableI2CBusSet() {
            int[] ids = {};
            try {
                ids = I2CFactory.getBusIds();
                LOG.log(Level.INFO, "Found the following I2C busses: {0}", Arrays.toString(ids));
            } catch (NoSuchFileException ex) {
                LOG.log(Level.INFO, "No I2C Interface Found", ex);
            } catch (IOException ex) {
                LOG.log(Level.INFO, "I/O error during fetch of I2C busses occurred", ex);
            }
            return ids;
        }
        
    }

	@Override
	public boolean isOpen() {
		return !scheduler.isTerminated();
	}
	
	@Override
    public int getWeatherDataRequestsPerMinute() {
    	long periodSeconds = REQUEST_RATE / 1000;
    	return (int) (60 / periodSeconds);
    }
	
    @Override
	public String getDeviceManufacturer() {
		return MANUFACTURER;
	}

	@Override
	public String getDeviceModel() {
		return MODEL_NAME;
	}

	@Override
	public String getDeviceSerialNumber() {
		return SERIAL_NUMBER;
	}

	@Override
	public String getHardwareVersion() {
		return HARDWARE_VERSION;
	}
	
	@Override
	public String getSoftwareVersion() {
		return SOFTWARE_VERSION;
	}
    
    @Override
    public String getEquipmentCode() {
        return EQUIPMENT_CODE;
    }
    
    public static String getUniqueDeviceId() {
		return MANUFACTURER + "_" + MODEL_NAME + "_" + SERIAL_NUMBER + "_" + 
				HARDWARE_VERSION + "_" + SOFTWARE_VERSION + "_" + EQUIPMENT_CODE;
    }

	@Override
	public void saveClientSettings() {
		// there are no user settings in this class
	}

	@Override
	public JPanel[] getConfigurationComponentArray() {
		return new JPanel[0];
	}

	@Override
	public void stopSensor() {
		scheduler.shutdown();
	}

	@Override
    public String getClassName() {
    	return BME280.class.getName();
    }

	@Override
	public double getTempExteriorCelsius() {
		return tempCelsius;
	}

	@Override
	public double getBarometricPressureRelativeHPA() {
		return pressureMillibars;
	}

	@Override
	public int getExteriorHumidity() {
		return (int) Math.round(relativeHumidity);
	}

}


