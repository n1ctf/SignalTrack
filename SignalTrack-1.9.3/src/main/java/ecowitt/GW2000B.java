package ecowitt;

import java.beans.PropertyChangeListener;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.nio.ByteBuffer;

import java.util.Arrays;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.prefs.Preferences;

import javax.swing.JPanel;

import org.apache.commons.lang3.ArrayUtils;

import meteorology.AbstractEnvironmentSensor;

import network.NetworkParameterSet;

import tcp.SynchronizedTCPClient;
import tcp.TcpIpConfigurationComponent;

import time.ConsolidatedTime;

/**
 *
 * @author John
 */
public class GW2000B extends AbstractEnvironmentSensor {

    protected static final byte[] DEFAULT_GW2000B_ADDRESS = {(byte) 192, (byte) 168, (byte) 56, (byte) 169};
    public static final int DEFAULT_GW2000B_DATA_PORT = 45000;
    public static final double DEFAULT_STATION_ELEVATION_FEET = 896D;

    public static final String EQUIPMENT_CODE = "jSIG";
    public static final String SERIAL_NUMBER = "";
    public static final String MANUFACTURER = "Ecowitt";
    public static final String MODEL_NAME = "GW2000B";
    public static final String SOFTWARE_VERSION = "2.2.4";
    public static final String HARDWARE_VERSION = "";

    public static final long START_WAIT = 1000;     // milliseconds
    public static final long REQUEST_RATE = 10000;   // milliseconds

    public static final int HEADER = 0xFFFF;
    public static final int RETURN_DATA_TYPE_INDEX = 2;  // zero based location of data type (LIVEDATA or RAINDATA) on returned data array
    public static final byte BROADCAST = (byte) 0x12;
    public static final int RAINDATA_ARRAY_LENGTH = 61;
    public static final int LIVEDATA_ARRAY_LENGTH = 49;

    public static final byte RAINDATA = (byte) 0x57; // Note: RAINDATA includes the associated command
    private static final DataSet RAIN_RATE = new DataSet(0x80, 5, 2);
    private static final DataSet RAIN_DAY = new DataSet(0x83, 8, 4);
    private static final DataSet RAIN_WEEK = new DataSet(0x84, 13, 4);
    private static final DataSet RAIN_MONTH = new DataSet(0x85, 18, 4);
    private static final DataSet RAIN_YEAR = new DataSet(0x86, 23, 4);
    private static final DataSet RAIN_EVENT = new DataSet(0x81, 28, 2);

    public static final byte LIVEDATA = (byte) 0x27; // Note: LIVEDATA includes the associated command
    private static final DataSet INT_TEMP = new DataSet(0x01, 5, 2);
    private static final DataSet INT_HUMID = new DataSet(0x06, 8, 1);
    private static final DataSet BARO_PRESS_ABS = new DataSet(0x08, 10, 2);
    private static final DataSet BARO_PRESS_REL = new DataSet(0x09, 13, 2);
    private static final DataSet EXT_TEMP = new DataSet(0x02, 16, 2);
    private static final DataSet EXT_HUMID = new DataSet(0x07, 19, 1);
    private static final DataSet WIND_DIR_TRUE = new DataSet(0x0A, 21, 2);
    private static final DataSet CURRENT_WIND_SPEED = new DataSet(0x0B, 24, 2);
    private static final DataSet GUSTING_WIND_SPEED = new DataSet(0x0C, 27, 2);
    private static final DataSet LUX_SOLAR = new DataSet(0x15, 30, 4);
    private static final DataSet UV_LEVEL = new DataSet(0x16, 35, 2);
    private static final DataSet UV_INDEX = new DataSet(0x17, 38, 1);
    private static final DataSet MAX_WIND_SPEED_DAILY = new DataSet(0x19, 40, 2);

    private static final Logger LOG = Logger.getLogger(GW2000B.class.getName());
    private final Preferences userPrefs = Preferences.userRoot().node(GW2000B.class.getName());
    private final NetworkParameterSet netParams = new NetworkParameterSet(InetAddress.getByAddress(DEFAULT_GW2000B_ADDRESS), DEFAULT_GW2000B_DATA_PORT);
    private final SynchronizedTCPClient tcpClient;
    private PropertyChangeListener tcpClientListener;
    
	private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    
	private ExecutorService executor = Executors.newSingleThreadExecutor();

    
    private boolean rainDataComplete;
    private boolean liveDataComplete;
    
    public enum Event {
        STATUS,
        FAIL,
        READY,
        VALID_PRESSURE_CHANGE,
        VALID_HUMIDITY_CHANGE,
        VALID_TEMP_CHANGE
    }

    public GW2000B(ConsolidatedTime consolidatedTime, Boolean clearAllPreferences) throws UnknownHostException {
        super(getUniqueDeviceId(), consolidatedTime, clearAllPreferences);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                close();
            }
        });

        configureComponentListeners();

        scheduler = Executors.newSingleThreadScheduledExecutor();
        executor = Executors.newSingleThreadExecutor();

        try {
            netParams.setInetAddress(InetAddress.getByAddress(userPrefs.getByteArray(getUniqueDeviceId() + "inet4Address", InetAddress.getByAddress(DEFAULT_GW2000B_ADDRESS).getAddress())));
            netParams.setPortNumber(userPrefs.getInt(getUniqueDeviceId() + "portNumber", DEFAULT_GW2000B_DATA_PORT));
        } catch (UnknownHostException ex) {
            LOG.log(Level.CONFIG, ex.getMessage(), ex);
        }

        tcpClient = new SynchronizedTCPClient();
    }

    @Override
    public void startSensor() {
    	if (!isConnected()) {
	        if (!tcpClient.isPropertyChangeListenerRegistered(tcpClientListener)) { 
	        	tcpClient.addPropertyChangeListener(tcpClientListener);
	        }
	        tcpClient.startTCPConnection(netParams);
	        setEnableEvents(true);
    	}
    }
    
    @Override
    public void stopSensor() {
    	setEnableEvents(false);
		tcpClient.stopTCPConnection();
		if (tcpClient.isPropertyChangeListenerRegistered(tcpClientListener)) { 
        	tcpClient.removePropertyChangeListener(tcpClientListener);
        }
    }

    public static byte[] getPrimitiveByteArray(Byte[] byteArrayObject) {
        return ArrayUtils.toPrimitive(byteArrayObject);
    }

    public static Byte[] getByteArrayObject(byte[] primitiveByteArray) {
        return ArrayUtils.toObject(primitiveByteArray);
    }

    private void configureComponentListeners() {
        tcpClientListener = event -> {
            if (event.getPropertyName().equals(SynchronizedTCPClient.Event.DATA_RECEIVED.name())) {
            	executor.execute(new ProcessData((byte[]) event.getNewValue(), isDebug()));
            }
            if (event.getPropertyName().equals(SynchronizedTCPClient.Event.CONNECTION_ACCEPTED.name())) {
            	setConnected(true);
                pcs.firePropertyChange(Event.READY.name(), null, true);
                startPeriodicDataFetch();
            }
            if (event.getPropertyName().equals(SynchronizedTCPClient.Event.CONNECTION_DROPPED.name())) {
                setConnected(false);
                stopPeriodicDataFetch();
            }
        };
    }

    private void startPeriodicDataFetch() {
        scheduler.scheduleAtFixedRate(new FetchData(), START_WAIT, REQUEST_RATE, TimeUnit.MILLISECONDS);
    }

    private void stopPeriodicDataFetch() {
    	if (scheduler != null) {
			try {
				LOG.log(Level.INFO, "Initializing GW2000B.scheduler service termination....");
				scheduler.shutdown();
				scheduler.awaitTermination(2, TimeUnit.SECONDS);
				LOG.log(Level.INFO, "GW2000B.scheduler service has gracefully terminated");
			} catch (InterruptedException _) {
				scheduler.shutdownNow();
				LOG.log(Level.SEVERE, ";GW2000B.scheduler service has timed out after 22 seconds of waiting to terminate processes.");
				Thread.currentThread().interrupt();
			}
		}
    }

    @Override
    public void close() {
        saveClientSettings();
        
        pcs.firePropertyChange(Event.READY.name(), null, false);
        
        if (tcpClient != null) {
            tcpClient.close();
        }
        
        stopPeriodicDataFetch();
        
        if (executor != null) {
			try {
				LOG.log(Level.INFO, "Initializing GW2000B.executor service termination....");
				executor.shutdown();
				executor.awaitTermination(2, TimeUnit.SECONDS);
				LOG.log(Level.INFO, "GW2000B.executor service has gracefully terminated");
			} catch (InterruptedException _) {
				executor.shutdownNow();
				LOG.log(Level.SEVERE, "GW2000B.executor service has timed out after 2 seconds of waiting to terminate processes.");
				Thread.currentThread().interrupt();
			}
		}
    }

    private class FetchData implements Runnable {
        @Override
        public void run() {
            fetchLiveData();
            fetchRainData();
        }
    }

    private final class ProcessData implements Runnable {
    	private final byte[] byteArray;
    	private final boolean debug;
    	
        private ProcessData(byte[] byteArray, boolean debug) {
            this.byteArray = byteArray;
            this.debug = debug;
        }

        @Override
        public void run() {
            final byte[] data = Arrays.copyOf(byteArray, byteArray.length);
            if (!isByteArrayValid(data)) {
                LOG.log(Level.INFO, "Checksum Failure in Byte Array: {0}", Arrays.toString(data));
                return;
            }
            if (debug) {
                for (int i = 0; i < data.length; i++) {
                    int x = data[i];
                    if (x < 0) {
                        x += 256;
                    }
                    LOG.log(Level.INFO, "Data: {0} {1} {2}", new Object[]{x, Integer.toHexString(x), i});
                }
            }
            
            if (data[RETURN_DATA_TYPE_INDEX] == RAINDATA) {
                rainRateMillimetersPerHour = getRainRateMillimetersPerHour(data);
                eventRainMillimeters = getEventRainMillimeters(data);
                dailyRainMillimeters = getDailyRainMillimeters(data);
                weeklyRainMillimeters = getWeeklyRainMillimeters(data);
                monthlyRainMillimeters = getMonthlyRainMillimeters(data);
                yearlyRainMillimeters = getYearlyRainMillimeters(data);
                
                LOG.log(Level.INFO, "{0} {1} {2} {3} {4} {5} {6} {7} {8} {9} {10} {11} {12} {13}",
                    new Object[]{
                        "\n------------------------- GW2000B Rain Data -------------------------",
                        "\n   Rain Rate Millimeters Per Hour:  ", rainRateMillimetersPerHour,
                        "\n   Event Rain Millimeters:          ", eventRainMillimeters,
                        "\n   Daily Rain Millimeters:          ", dailyRainMillimeters,
                        "\n   Weekly Rain Millimeters:         ", weeklyRainMillimeters,
                        "\n   Monthly Rain Millimeters:        ", monthlyRainMillimeters,
                        "\n   Yearly Rain Millimeters:         ", yearlyRainMillimeters,
                        "\n----------------------- END GW2000B Rain Data -----------------------"});
                
                rainDataComplete = true;
            }

            if (data[RETURN_DATA_TYPE_INDEX] == LIVEDATA) {
                tempInteriorCelsius = getInteriorTemperatureCelsius(data);
                tempExteriorCelsius = getExteriorTemperatureCelsius(data);
                interiorHumidity = getInteriorHumidityPercent(data);
                exteriorHumidity = getExteriorHumidityPercent(data);
                barometricPressureAbsoluteHPA = getBarometricPressureAbsoluteHPA(data);
                barometricPressureRelativeHPA = getBarometricPressureRelativeHPA(data);
                windDirectionTrue = getWindDirectionTrue(data);
                currentWindSpeedMetersPerSecond = getCurrentWindSpeedMetersPerSecond(data);
                gustingWindSpeedMetersPerSecond = getGustingWindSpeedMetersPerSecond(data);
                maxDailyWindSpeedMetersPerSecond = getMaxDailyWindSpeedMetersPerSecond(data);
                luminosityWM2 = getCurrentLuminosityWM2(data);
                currentUvLevel = getCurrentUvLevel(data);
                currentUvIndex = getCurrentUvIndex(data);

                LOG.log(Level.INFO, "{0} {1} {2} {3} {4} {5} {6} {7} {8} {9} {10} {11} {12} {13} {14} {15} {16} {17} {18}"
                    + "{19} {20} {21} {22} {23} {24} {25} {26} {27} {28} {29} {30} {31} {32} {33}",
                    new Object[]{
                        "\n------------------------- GW2000B Live Data -------------------------",
                        "\n   Interior Temperature:            ", tempInteriorCelsius,
                        "\n   Exterior Temperature:            ", tempExteriorCelsius,
                        "\n   Interior Humidity:               ", interiorHumidity,
                        "\n   Exterior Humidity:               ", exteriorHumidity,
                        "\n   Dew Point:                       ", getDewPointFahrenheit(),
                        "\n   Barometric Pressure - Absolute:  ", barometricPressureAbsoluteHPA,
                        "\n   Barometric Pressure - Relative:  ", barometricPressureRelativeHPA,
                        "\n   Pressure Altitude - Feet:        ", getPressureAltitudeFeet(),
                        "\n   Heat Index Degrees Fahrenheit:   ", getHeatIndexFahrenheit(),
                        "\n   Wind Direction - True:           ", windDirectionTrue,
                        "\n   Current Wind Speed MPH:          ", currentWindSpeedMetersPerSecond,
                        "\n   Gusting Wind speed MPH:          ", gustingWindSpeedMetersPerSecond,
                        "\n   Max Daily Wind Speed MPH:        ", maxDailyWindSpeedMetersPerSecond,
                        "\n   Current LUX:                     ", String.valueOf(luminosityWM2),
                        "\n   Current UV Level:                ", currentUvLevel,
                        "\n   Current UV Index:                ", currentUvIndex,
                        "\n----------------------- END GW2000B Live Data -----------------------"});
            
                liveDataComplete = true;
            }
            
            if (rainDataComplete && liveDataComplete) {
            	setDataComplete();
            	liveDataComplete = false;
            	rainDataComplete = false;
            }
            
        }
        
        private int getWindDirectionTrue(byte[] data) {
            return getDoubleWordValue(getSubArrayObject(WIND_DIR_TRUE, data));
        }

        private double getInteriorTemperatureCelsius(byte[] data) {
            return getDoubleWordValue(getSubArrayObject(INT_TEMP, data)) / 10.0;
        }

        private double getExteriorTemperatureCelsius(byte[] data) {
            return getDoubleWordValue(getSubArrayObject(EXT_TEMP, data)) / 10.0;
        }

        private int getInteriorHumidityPercent(byte[] data) {
            return getSingleWordValue(getSubArrayObject(INT_HUMID, data));
        }

        private int getExteriorHumidityPercent(byte[] data) {
            return getSingleWordValue(getSubArrayObject(EXT_HUMID, data));
        }

        private double getRainRateMillimetersPerHour(byte[] data) {
            return getDoubleWordValue(getSubArrayObject(RAIN_RATE, data)) / 10.0;
        }

        private double getEventRainMillimeters(byte[] data) {
            return getDoubleWordValue(getSubArrayObject(RAIN_EVENT, data)) / 10.0;
        }

        private double getDailyRainMillimeters(byte[] data) {
            return getQuadWordValue(getSubArrayObject(RAIN_DAY, data)) / 10.0;
        }

        private double getMaxDailyWindSpeedMetersPerSecond(byte[] data) {
            return getDoubleWordValue(getSubArrayObject(MAX_WIND_SPEED_DAILY, data)) / 10.0;
        }

        private double getGustingWindSpeedMetersPerSecond(byte[] data) {
            return getDoubleWordValue(getSubArrayObject(GUSTING_WIND_SPEED, data)) / 10.0;
        }

        private double getBarometricPressureAbsoluteHPA(byte[] data) {
            return getDoubleWordValue(getSubArrayObject(BARO_PRESS_ABS, data)) / 10.0;
        }

        private double getBarometricPressureRelativeHPA(byte[] data) {
            return getDoubleWordValue(getSubArrayObject(BARO_PRESS_REL, data)) / 10.0;
        }

        private int getCurrentUvIndex(byte[] data) {
            return getSingleWordValue(getSubArrayObject(UV_INDEX, data));
        }

        private double getCurrentUvLevel(byte[] data) {
            return getDoubleWordValue(getSubArrayObject(UV_LEVEL, data)) / 10.0;
        }

        private double getCurrentLuminosityWM2(byte[] data) {
            return getQuadWordValue(getSubArrayObject(LUX_SOLAR, data)) / 10.0;
        }

        private double getCurrentWindSpeedMetersPerSecond(byte[] data) {
            return getDoubleWordValue(getSubArrayObject(CURRENT_WIND_SPEED, data)) / 10.0;
        }

        private double getWeeklyRainMillimeters(byte[] data) {
            return getQuadWordValue(getSubArrayObject(RAIN_WEEK, data)) / 10.0;
        }

        private double getMonthlyRainMillimeters(byte[] data) {
            return getQuadWordValue(getSubArrayObject(RAIN_MONTH, data)) / 10.0;
        }

        private double getYearlyRainMillimeters(byte[] data) {
            return getQuadWordValue(getSubArrayObject(RAIN_YEAR, data)) / 10.0;
        }
    }

    public synchronized void fetchRainData() {
    	final ByteBuffer rainDataRequest = ByteBuffer.allocate(5);
        rainDataRequest.put((byte) 0xFF);
        rainDataRequest.put((byte) 0xFF);
        rainDataRequest.put(RAINDATA);  //  command
        rainDataRequest.put((byte) 0x03);
        rainDataRequest.put(getChecksum((byte) (RAINDATA + 0x03)));
        if (tcpClient.isConnected() && tcpClient.allowRequests()) {
            tcpClient.synchronizedWrite(rainDataRequest.array(), RAINDATA_ARRAY_LENGTH);
        }
    }

    public synchronized void fetchLiveData() {
    	final ByteBuffer liveDataRequest = ByteBuffer.allocate(5);
        liveDataRequest.put((byte) 0xFF);
        liveDataRequest.put((byte) 0xFF);
        liveDataRequest.put(LIVEDATA);  //  command
        liveDataRequest.put((byte) 0x03);
        liveDataRequest.put(getChecksum((byte) (LIVEDATA + 0x03)));
        if (tcpClient.isConnected() && tcpClient.allowRequests()) {
            tcpClient.synchronizedWrite(liveDataRequest.array(), LIVEDATA_ARRAY_LENGTH);
        }
    }

    private static Object[] getSubArrayObject(DataSet dataSet, byte[] data) {
    	final Byte[] byteObject = ArrayUtils.toObject(data);
        return ArrayUtils.subarray(byteObject, dataSet.getPosition() + 1, dataSet.getPosition() + dataSet.getNumBytes() + 1);
    }

    public static int getSingleWordValue(Object[] subArray) {
        int value = -1;
        if ((Byte) subArray[0] != null) {
            value = ((Byte) subArray[0] < 0 ? ((Byte) subArray[0] + 256) : ((Byte) subArray[0] + 0));
        }
        return value;
    }

    public static int getDoubleWordValue(Object[] subarray) {
        int value = -1;
        if ((Byte) subarray[0] != null && (Byte) subarray[1] != null) {
            value = ((Byte) subarray[1] < 0 ? ((Byte) subarray[1] + 256) : ((Byte) subarray[1]))
                    + (((Byte) subarray[0]) << 8);
        }
        return value;
    }

    public static int getQuadWordValue(Object[] subarray) {
        int value = -1;
        if ((Byte) subarray[0] != null && (Byte) subarray[1] != null && (Byte) subarray[2] != null && (Byte) subarray[3] != null) {
            value = ((Byte) subarray[3] < 0 ? ((Byte) subarray[3] + 256) : (Byte) subarray[3])
                    + (((Byte) subarray[2] < 0 ? ((Byte) subarray[2] + 256) : (Byte) subarray[2]) << 8)
                    + (((Byte) subarray[1] < 0 ? ((Byte) subarray[1] + 256) : (Byte) subarray[1]) << 16)
                    + (((Byte) subarray[0]) << 31);
        }
        return value;
    }

    public static byte getChecksum(byte data) {
    	final byte[] b = {data};
        return getChecksum(b);
    }

    public static byte getChecksum(byte[] data) {
        // Ecowitt uses a very simple 1 byte wrapping checksum to validate both requests and responses.
    	// CHECKSUM: 1 byte, CHECKSUM=CMD+SIZE+DATA1+DATA2+…+DATAn
        int csum = 0;
        for (int d : data) {
            csum += d;
        }
        return (byte) (csum & 0xFF);
    }
    
	/*
	 * Mode: GW1000 V1.0 Serial number: FOS-ENG-022-A 
	 * 1. Data exchange format： 
	 * 		Fixed header, CMD, SIZE, DATA1, DATA2, … , DATAn, CHECKSUM 
	 * 		Fixed header: 2 bytes, header is fixed as 0xffff 
	 * 		CMD: 1 byte, Command 
	 * 		SIZE: 1 byte, packet size，counted from CMD till CHECKSUM 
	 * 		DATA: n bytes, payloads，variable length
	 * 		CHECKSUM: 1 byte, CHECKSUM=CMD+SIZE+DATA1+DATA2+…+DATAn
	 */
    
    public static synchronized boolean isByteArrayValid(byte[] data) {
    	if (data.length == 0) {
    		return false;
    	} else {
    		final int reportedChecksum = Byte.toUnsignedInt(data[data.length - 1]);
    		final byte[] subset = Arrays.copyOfRange(data, 2, data.length - 1);
    		final int calculatedChecksum = Byte.toUnsignedInt(getChecksum(subset));
	        LOG.log(Level.INFO, "Reported Checksum: {0}  Calculated Checksum: {1}",
	        		new Object[] {
	    				reportedChecksum,
	    				calculatedChecksum,
	        		});
	        
	        return reportedChecksum == calculatedChecksum;
    	}
    }

    public static byte[] parseStringToByteArray(String str) {
    	final byte[] b = new byte[4];
    	final String[] s = str.split("\\.");
        for (int i = 0; i < 4; i++) {
            b[i] = Byte.parseByte(s[i]);
        }
        return b;
    }

    @Override
    public String getClassName() {
    	return GW2000B.class.getName();
    }
    
    @Override
    public int getWeatherDataRequestsPerMinute() {
    	final long periodSeconds = REQUEST_RATE / 1000;
    	return (int) (60 / periodSeconds);
    }
    
    @Override
    public boolean isOpen() {
        return !scheduler.isShutdown();
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
        return MANUFACTURER + "_" + MODEL_NAME + "_" + SERIAL_NUMBER + "_"
                + HARDWARE_VERSION + "_" + SOFTWARE_VERSION + "_" + EQUIPMENT_CODE;
    }

    @Override
    public JPanel[] getConfigurationComponentArray() {
        return new TcpIpConfigurationComponent(netParams, MANUFACTURER + " " + MODEL_NAME + " TCP/IP Settings").getSettingsPanelArray();
    }

    @Override
    public void saveClientSettings() {
        userPrefs.putByteArray(String.valueOf(getUniqueDeviceId()) + "inet4Address", netParams.getInetAddress().getAddress());
        userPrefs.putInt(String.valueOf(getUniqueDeviceId()) + "portNumber", netParams.getPortNumber());
    }

}
