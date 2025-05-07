package texasInstruments.ads1115;

/**
 * Utility class to read analog levels from a TI based ADS1115 analog to digital
 * converter breakout board.
 *
 * @author John R. Chartkoff
 */

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.io.IOException;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 */
public class ADS1115 implements AutoCloseable {

    // Config register flag/data positions
    public static final byte ADS1115_OS_FLAG_POS = 15;
    public static final byte ADS1115_MUX2_DAT_POS = 14;
    public static final byte ADS1115_MUX1_DAT_POS = 13;
    public static final byte ADS1115_MUX0_DAT_POS = 12;
    public static final byte ADS1115_PGA2_DAT_POS = 11;
    public static final byte ADS1115_PGA1_DAT_POS = 10;
    public static final byte ADS1115_PGA0_DAT_POS = 9;
    public static final byte ADS1115_MODE_FLAG_POS = 8;
    public static final byte ADS1115_DR2_DAT_POS = 7;
    public static final byte ADS1115_DR1_DAT_POS = 6;
    public static final byte ADS1115_DR0_DAT_POS = 5;
    public static final byte ADS1115_COMP_MODE_POS = 4;
    public static final byte ADS1115_COMP_POL_POS = 3;
    public static final byte ADS1115_COMP_LAT_POS = 2;
    public static final byte ADS1115_COMP_QUE1_DAT_POS = 1;
    public static final byte ADS1115_COMP_QUE0_DAT_POS = 0;

    // I2C addresses. You have to connect the address pin with the corresponding data line
    public static final byte ADS1115_I2C_ADDR_GND = (byte) 0b1001000;
    public static final byte ADS1115_I2C_ADDR_VDD = (byte) 0b1001001;
    public static final byte ADS1115_I2C_ADDR_SDA = (byte) 0b1001010;
    public static final byte ADS1115_I2C_ADDR_SCL = (byte) 0b1001011;

    // This application is only using single ended signal outputCode, which half the scale from ground to +VIN
    public static final int ADS1115_MIN_MEASUREMENT_VAL = 0x0000;
    public static final int ADS1115_MAX_MEASUREMENT_VAL = 0x7FFF;

    // Register addresses
    public static final byte ADS1115_CONVERSION_REG_ADDR = (byte) 0b00;
    public static final byte ADS1115_CONFIG_REG_ADDR = (byte) 0b01;
    public static final byte ADS1115_LOW_TRESH_REG_ADDR = (byte) 0b10;
    public static final byte ADS1115_HIGH_TRESH_REG_ADDR = (byte) 0b11;

    // Register defaults
    public static final int ADS1115_CONVERSION_REG_DEF = 0x0000;
    public static final int ADS1115_CONFIG_REG_DEF = 0x8583;
    public static final int ADS1115_LOW_TRESH_REG_DEF = 0x8000;
    public static final int ADS1115_HIGH_TRESH_REG_DEF = 0x7FFF;

    // Operational status macros
    public static final byte ADS1115_OS_START_SINGLE = (byte) 0b1;
    public static final byte ADS1115_OS_ONGOING_CONV = (byte) 0b0;
    public static final byte ADS1115_OS_NO_CONV = (byte) 0b1;
    
    // Device operation mode macros
    public static final byte ADS1115_MODE_CONTINOUS = (byte) 0b0;
    public static final byte ADS1115_MODE_SINGLE = (byte) 0b1;

    // Comparator mode macros
    public static final byte ADS1115_COMP_MODE_TRADITIONAL = (byte) 0b0;
    public static final byte ADS1115_COMP_MODE_WINDOW = (byte) 0b1;

    // Comparator polarity macros
    public static final byte ADS1115_COMP_POL_LOW = (byte) 0b0;
    public static final byte ADS1115_COMP_POL_HIGH = (byte) 0b1;

    // Latching comparator macros
    public static final byte ADS1115_COMP_LAT_NO_LATCH = (byte) 0b0;
    public static final byte ADS1115_COMP_LAT_LATCH = (byte) 0b1;

    public static final long START_WAIT = 1000;     // milliseconds
    public static final long REQUEST_RATE = 200;   // milliseconds
    public static final long CONNECT_RETRY_RATE = 3000;   // milliseconds

    protected static final int[] ADS1115_ADDRESS_SET = {0x48, 0x49, 0x50, 0x51};
    public static final int I2C_ADDR = ADS1115_ADDRESS_SET[0];

    public static final int DEFAULT_I2C_BUS = I2CBus.BUS_1;

    private static final Logger LOG = Logger.getLogger(ADS1115.class.getName());
    
    private final EnumMap<MultiplexerModes, Byte> multiplexerModes = new EnumMap<>(MultiplexerModes.class);
    private final EnumMap<SamplesPerSecondSet, Byte> samplesPerSecondSet = new EnumMap<>(SamplesPerSecondSet.class);
    private final EnumMap<PGAModes, Byte> pgaModes = new EnumMap<>(PGAModes.class);
    private final EnumMap<PGAModes, Double> multiplierValueSet = new EnumMap<>(PGAModes.class);
    private final EnumMap<PGAModes, Double> maximumVoltageSet = new EnumMap<>(PGAModes.class);
    private final EnumMap<ComparatorQueueSet, Byte> comparatorQueueSet = new EnumMap<>(ComparatorQueueSet.class);

    private I2CDevice device;
    private I2CBus i2c;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private int debugLevel;
    private ScheduledFuture<?> serviceRequestHandle;
    private final ExecutorService executor = Executors.newScheduledThreadPool(0);
    private Thread connectToI2C;
    private byte slaveAddress = ADS1115_I2C_ADDR_GND;
    private MultiplexerModes muxMode = MultiplexerModes.ADS1115_MUX_AIN0_GND;
    private PGAModes pgaMode = PGAModes.ADS1115_PGA_6_144;
    private byte conversionMode = ADS1115_MODE_SINGLE;
    private byte comparatorMode = ADS1115_COMP_MODE_TRADITIONAL;
    private SamplesPerSecondSet dataRate = SamplesPerSecondSet.ADS1115_DR_128_SPS;
    private byte latching = ADS1115_COMP_LAT_NO_LATCH;
    private byte polarity = ADS1115_COMP_POL_LOW;
    private ComparatorQueueSet comparatorQueue = ComparatorQueueSet.ADS1115_COMP_QUE_DISABLE;

    private double ain0ToGnd = -999;
    private double ain1ToGnd = -999;
    private double ain2ToGnd = -999;
    private double ain3ToGnd = -999;

    public ADS1115() {
        this(I2C_ADDR, DEFAULT_I2C_BUS, 0);
        this.device = null;
    }

    public ADS1115(int debugLevel) {
        this(I2C_ADDR, DEFAULT_I2C_BUS, debugLevel);
        this.device = null;
    }

    public ADS1115(int address, int i2cBus, int debugLevel) {
        this.device = null;
        this.debugLevel = debugLevel;

        loadMaps();
        
        executor.execute(connectToI2C = new Thread(new ConnectToI2C(address, i2cBus)));
    }

    private void loadMaps() {
        samplesPerSecondSet.put(SamplesPerSecondSet.ADS1115_DR_8_SPS, (byte) 0b000);
        samplesPerSecondSet.put(SamplesPerSecondSet.ADS1115_DR_16_SPS, (byte) 0b001);
        samplesPerSecondSet.put(SamplesPerSecondSet.ADS1115_DR_32_SPS, (byte) 0b010);
        samplesPerSecondSet.put(SamplesPerSecondSet.ADS1115_DR_64_SPS, (byte) 0b011);
        samplesPerSecondSet.put(SamplesPerSecondSet.ADS1115_DR_128_SPS, (byte) 0b100);
        samplesPerSecondSet.put(SamplesPerSecondSet.ADS1115_DR_250_SPS, (byte) 0b101);
        samplesPerSecondSet.put(SamplesPerSecondSet.ADS1115_DR_475_SPS, (byte) 0b110);
        samplesPerSecondSet.put(SamplesPerSecondSet.ADS1115_DR_860_SPS, (byte) 0b111);

        multiplexerModes.put(MultiplexerModes.ADS1115_MUX_AIN0_AIN1, (byte) 0b000);
        multiplexerModes.put(MultiplexerModes.ADS1115_MUX_AIN0_AIN3, (byte) 0b001);
        multiplexerModes.put(MultiplexerModes.ADS1115_MUX_AIN1_AIN3, (byte) 0b010);
        multiplexerModes.put(MultiplexerModes.ADS1115_MUX_AIN2_AIN3, (byte) 0b011);
        multiplexerModes.put(MultiplexerModes.ADS1115_MUX_AIN0_GND, (byte) 0b100);
        multiplexerModes.put(MultiplexerModes.ADS1115_MUX_AIN1_GND, (byte) 0b101);
        multiplexerModes.put(MultiplexerModes.ADS1115_MUX_AIN2_GND, (byte) 0b110);
        multiplexerModes.put(MultiplexerModes.ADS1115_MUX_AIN3_GND, (byte) 0b111);

        pgaModes.put(PGAModes.ADS1115_PGA_6_144, (byte) 0b000);
        pgaModes.put(PGAModes.ADS1115_PGA_4_096, (byte) 0b001);
        pgaModes.put(PGAModes.ADS1115_PGA_2_048, (byte) 0b010);
        pgaModes.put(PGAModes.ADS1115_PGA_1_024, (byte) 0b011);
        pgaModes.put(PGAModes.ADS1115_PGA_0_512, (byte) 0b100);
        pgaModes.put(PGAModes.ADS1115_PGA_0_256, (byte) 0b101);

        multiplierValueSet.put(PGAModes.ADS1115_PGA_6_144, 0.1875);
        multiplierValueSet.put(PGAModes.ADS1115_PGA_4_096, 0.125);
        multiplierValueSet.put(PGAModes.ADS1115_PGA_2_048, 0.0625);
        multiplierValueSet.put(PGAModes.ADS1115_PGA_1_024, 0.03125);
        multiplierValueSet.put(PGAModes.ADS1115_PGA_0_512, 0.015625);
        multiplierValueSet.put(PGAModes.ADS1115_PGA_0_256, 0.0078125);

        maximumVoltageSet.put(PGAModes.ADS1115_PGA_6_144, 6.144);
        maximumVoltageSet.put(PGAModes.ADS1115_PGA_4_096, 4.096);
        maximumVoltageSet.put(PGAModes.ADS1115_PGA_2_048, 2.048);
        maximumVoltageSet.put(PGAModes.ADS1115_PGA_1_024, 1.024);
        maximumVoltageSet.put(PGAModes.ADS1115_PGA_0_512, 0.512);
        maximumVoltageSet.put(PGAModes.ADS1115_PGA_0_256, 0.256);

        comparatorQueueSet.put(ComparatorQueueSet.ADS1115_COMP_QUE_ONE_CONV, (byte) 0b00);
        comparatorQueueSet.put(ComparatorQueueSet.ADS1115_COMP_QUE_TWO_CONV, (byte) 0b01);
        comparatorQueueSet.put(ComparatorQueueSet.ADS1115_COMP_QUE_FOUR_CONV, (byte) 0b10);
        comparatorQueueSet.put(ComparatorQueueSet.ADS1115_COMP_QUE_DISABLE, (byte) 0b11);
    }

    public static int booleanToInt(boolean value) {
        return value ? 1 : 0;
    }

    public static int concatenateBytes(byte msb, byte lsb) {
        return (asInt(msb) << 8) | asInt(lsb);
    }

    public static double round(double value, int precision) {
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

    public void start() {
    	try (final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1)) {
    		serviceRequestHandle = scheduler.scheduleAtFixedRate(new ReadDevice(), START_WAIT, REQUEST_RATE, TimeUnit.MILLISECONDS);
    	}
    }

    public synchronized void reset() {
        try {
            device.write(asByteArray(ADS1115_CONFIG_REG_ADDR, ADS1115_CONFIG_REG_DEF));
            device.write(asByteArray(ADS1115_LOW_TRESH_REG_ADDR, ADS1115_LOW_TRESH_REG_DEF));
            device.write(asByteArray(ADS1115_HIGH_TRESH_REG_ADDR, ADS1115_HIGH_TRESH_REG_DEF));
        } catch (IOException | NullPointerException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private byte[] asByteArray(byte addr, int word) {
        return new byte[] {addr, (byte) (word & 0xFF), (byte) ((word >> 8) & 0xFF)};

    }

    @Override
    public void close() {
        try {
            pcs.firePropertyChange(Event.READY.name(), null, false);
            if (i2c != null) {
                i2c.close();
            }
            if (serviceRequestHandle != null) {
                serviceRequestHandle.cancel(true);
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
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

    public PropertyChangeListener[] getPropertyChangeListeners() {
        return pcs.getPropertyChangeListeners();
    }

    private class ReadDevice implements Runnable {

        @Override
        public synchronized void run() {
            try {
                for (MultiplexerModes mmode : MultiplexerModes.values()) {
                    connectToI2C.join();

                    final double currentVoltage = round(readVoltage(mmode), 3);

                    switch (mmode) {
						case ADS1115_MUX_AIN0_GND -> {
							pcs.firePropertyChange(Event.VOLTAGE_CHANGE_AIN0_GND.name(), round(ain0ToGnd, 3), currentVoltage);
							ain0ToGnd = currentVoltage;
						}
						case ADS1115_MUX_AIN1_GND -> {
							pcs.firePropertyChange(Event.VOLTAGE_CHANGE_AIN1_GND.name(), round(ain1ToGnd, 3), currentVoltage);
							ain1ToGnd = currentVoltage;
						}
						case ADS1115_MUX_AIN2_GND -> {
							pcs.firePropertyChange(Event.VOLTAGE_CHANGE_AIN2_GND.name(), round(ain2ToGnd, 3), currentVoltage);
							ain2ToGnd = currentVoltage;
						}
						case ADS1115_MUX_AIN3_GND -> {
							pcs.firePropertyChange(Event.VOLTAGE_CHANGE_AIN3_GND.name(), round(ain3ToGnd, 3), currentVoltage);
							ain3ToGnd = currentVoltage;
						}
						case ADS1115_MUX_AIN0_AIN1 -> {
						}
						case ADS1115_MUX_AIN0_AIN3 -> {
						}
						case ADS1115_MUX_AIN1_AIN3 -> {
						}
						case ADS1115_MUX_AIN2_AIN3 -> {
						}
						default -> {
						}
					}
                }
            } catch (InterruptedException ex) {
                LOG.log(Level.SEVERE, null, ex);
                Thread.currentThread().interrupt();
            }
        }
        
        private synchronized double readVoltage(MultiplexerModes mmode) {

            try {
                setMuxMode(mmode);
                device.write(asByteArray(ADS1115_CONFIG_REG_ADDR, getConfigRegister()));
                
                TimeUnit.MICROSECONDS.sleep(getWaitPeriodMicroseconds() + 1);
                
                device.write(ADS1115_CONVERSION_REG_ADDR);
                final byte[] input = new byte[2];
                device.read(input, 0, 2);
                final int outputCode = getBytesAsInteger(input);

                return getVoltageFromOutputCode(outputCode);

            } catch (InterruptedException ex) {
                if (debugLevel >= 2) {
                    LOG.log(Level.INFO, null, ex);
                    Thread.currentThread().interrupt();
                }
                return 0.0;
            } catch (IOException ex) {
                if (debugLevel >= 2) {
                    LOG.log(Level.INFO, null, ex);
                }
                return 0.0;
            }
        }
        
        private byte[] asByteArray(byte addr, byte[] word) {
            return new byte[] {addr, word[0], word[1]};
        }
        
        private byte[] getConfigRegister() {
            int configReg = asInt(ADS1115_OS_START_SINGLE) << ADS1115_OS_FLAG_POS;
            configReg |= asInt(multiplexerModes.get(getMuxMode())) << ADS1115_MUX0_DAT_POS;
            configReg |= asInt(pgaModes.get(getPgaMode())) << ADS1115_PGA0_DAT_POS;
            configReg |= asInt(getConversionMode()) << ADS1115_MODE_FLAG_POS;
            configReg |= asInt(samplesPerSecondSet.get(getDataRate())) << ADS1115_DR0_DAT_POS;
            configReg |= asInt(getComparatorMode()) << ADS1115_COMP_MODE_POS;
            configReg |= asInt(getPolarity()) << ADS1115_COMP_POL_POS;
            configReg |= asInt(getLatching()) << ADS1115_COMP_LAT_POS;
            configReg |= asInt(comparatorQueueSet.get(getComparatorQueue())) << ADS1115_COMP_QUE0_DAT_POS;

            final byte[] data = new byte[2];

            data[0] = (byte) (configReg & 0xFF);
            data[1] = (byte) ((configReg >> 8) & 0xFF);

            return new byte[]{data[1], data[0]};
        }
        
        private long getWaitPeriodMicroseconds() {
            return switch (getDataRate()) {
				case ADS1115_DR_8_SPS -> getCalculatedDelayMicroseconds(8);
				case ADS1115_DR_16_SPS -> getCalculatedDelayMicroseconds(16);
				case ADS1115_DR_32_SPS -> getCalculatedDelayMicroseconds(32);
				case ADS1115_DR_64_SPS -> getCalculatedDelayMicroseconds(64);
				case ADS1115_DR_128_SPS -> getCalculatedDelayMicroseconds(128);
				case ADS1115_DR_250_SPS -> getCalculatedDelayMicroseconds(250);
				case ADS1115_DR_475_SPS -> getCalculatedDelayMicroseconds(475);
				case ADS1115_DR_860_SPS -> getCalculatedDelayMicroseconds(860);
				default -> getCalculatedDelayMicroseconds(860);
			};
        }
        
        private long getCalculatedDelayMicroseconds(int samplesPerSecond) {
            return Math.round((((1.0 / samplesPerSecond) * 1.1) * 1E6) + 20.0);
        }
        
        private int getBytesAsInteger(byte[] bytes) {
            return ((bytes[0] & 0xFF) << 8) | (bytes[1] & 0xFF);
        }

        private double getVoltageFromOutputCode(final int outputCode) {
            double rawData = outputCode;
            if (rawData > 32767) {
                rawData -= 65535;
            }
            return (rawData * multiplierValueSet.get(getPgaMode()) / 1E3D);
        }
    }
//
//    public static double getGallonsPerMinute() {
//        return 0.0;
//    }

    public static ZonedDateTime getCurrentUTCTimeCode() {
    	final Instant nowUtc = Instant.now();
    	final ZoneId utc = ZoneId.of("UTC");
        return ZonedDateTime.ofInstant(nowUtc, utc);
    }

    public I2CDevice getDevice() {
        return device;
    }

    public I2CBus getI2c() {
        return i2c;
    }

    public int getDebugLevel() {
        return debugLevel;
    }

    public void setDebugLevel(int debugLevel) {
        this.debugLevel = debugLevel;
    }

    public boolean isConnected() {
        try {
            return device.read(ADS1115_CONFIG_REG_ADDR) >= 0;
        } catch (IOException ex) {
            return false;
        }
    }

    public boolean isReady() {
        try {
            return (device.read(ADS1115_CONFIG_REG_ADDR) & (ADS1115_OS_START_SINGLE << ADS1115_OS_FLAG_POS)) > 0;
        } catch (IOException ex) {
            return false;
        }
    }

    public byte getSlaveAddress() {
        return slaveAddress;
    }

    public void setSlaveAddress(byte slaveAddress) {
        this.slaveAddress = slaveAddress;
    }

    public MultiplexerModes getMuxMode() {
        return muxMode;
    }

    public void setMuxMode(MultiplexerModes muxMode) {
        this.muxMode = muxMode;
    }

    public PGAModes getPgaMode() {
        return pgaMode;
    }

    public void setPgaMode(PGAModes pgaMode) {
        this.pgaMode = pgaMode;
    }

    public byte getConversionMode() {
        return conversionMode;
    }

    public void setConversionMode(byte conversionMode) {
        this.conversionMode = conversionMode;
    }

    public byte getComparatorMode() {
        return comparatorMode;
    }

    public void setComparatorMode(byte comparatorMode) {
        this.comparatorMode = comparatorMode;
    }

    public SamplesPerSecondSet getDataRate() {
        return dataRate;
    }

    public void setDataRate(SamplesPerSecondSet dataRate) {
        this.dataRate = dataRate;
    }

    public byte getLatching() {
        return latching;
    }

    public void setLatching(byte latching) {
        this.latching = latching;
    }

    public byte getPolarity() {
        return polarity;
    }

    public void setPolarity(byte polarity) {
        this.polarity = polarity;
    }

    public ComparatorQueueSet getComparatorQueue() {
        return comparatorQueue;
    }

    public void setComparatorQueue(ComparatorQueueSet comparatorQueue) {
        this.comparatorQueue = comparatorQueue;
    }

    private final class ConnectToI2C implements Runnable {

        private final int address;
        private final int i2cBus;

        private ConnectToI2C(int address, int i2cBus) {
            this.address = address;
            this.i2cBus = i2cBus;
        }

        @Override
        public synchronized void run() {
            if (!connect(address, i2cBus)) {
                LOG.log(Level.INFO, "ADS1115 NOT FOUND on I2C Bus {0} at Address {1}", new Object[]{i2cBus, address});

                final int[] i2cBusArray = getAvailableI2CBusSet();

                if (i2cBusArray.length == 0) {
                    LOG.log(Level.INFO, "NO I2C BUSSES ARE PRESENT ON THIS SYSTEM");
                } else {
                    for (int b : i2cBusArray) {
                        for (int aADS1115_ADDRESS_SET : ADS1115_ADDRESS_SET) {
                            LOG.log(Level.INFO, "Checking I2C Bus {0} for ADS1115 at address {1}", new Object[]{b, aADS1115_ADDRESS_SET});
                            if (connect((byte) aADS1115_ADDRESS_SET, (byte) b)) {
                                break;
                            }
                        }
                        if (isConnected()) {
                            break;
                        }
                    }
                }
            }

            if (isConnected()) {
                LOG.log(Level.INFO, "ADS1115 found as specified on I2C Bus {0} at Address {1}", new Object[]{i2c.getBusNumber(), "%02X".formatted(device.getAddress() & 0xFF)});
                reset();
                start();
            }
        }
        
        private int[] getAvailableI2CBusSet() {
            int[] ids = {};
            try {
                ids = I2CFactory.getBusIds();
                LOG.log(Level.INFO, "Found the following I2C busses: {0}", Arrays.toString(ids));
            } catch (IOException ex) {
                LOG.log(Level.INFO, "I/O error during fetch of I2C busses occurred {0}", ex.getMessage());
            }
            return ids;
        }
        
        private synchronized boolean connect(int address, int i2cBus) {
            try {
                i2c = I2CFactory.getInstance(i2cBus);
                device = i2c.getDevice(address);
                return isConnected();

            } catch (UnsatisfiedLinkError | UnsupportedBusNumberException | NullPointerException | IOException ex) {
                LOG.log(Level.INFO, null, ex.getMessage());
                return false;
            }
        }
    }
}
