package meteorology;

import java.awt.geom.Point2D;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.lang.reflect.InvocationTargetException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.JPanel;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import baeldung.FifoFixedSizeQueue;

import time.ConsolidatedTime;

/**
 *
 * @author n1ctf
 */
public abstract class AbstractEnvironmentSensor implements AutoCloseable {
	private static final Logger LOG = Logger.getLogger(AbstractEnvironmentSensor.class.getName());
	private static final Preferences userPrefs = Preferences.userRoot().node(AbstractEnvironmentSensor.class.getName());
	private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("America/New_York");
	private static final double DEFAULT_STATION_LATITUDE_DEGREES = 40.09372167;
	private static final double DEFAULT_STATION_LONGITUDE_DEGREES = -83.07445333;
	private static final double DEFAULT_STATION_ELEVATION_METERS = 282.5D;
	private static final Locale DEFAULT_LOCALE = Locale.US;
	private static final boolean DEFAULT_DEBUG_MODE = false;
	private static final boolean DEFAULT_START_WITH_SYSTEM = false;
	private static final boolean DEFAULT_USE_GPS_FOR_STATION_LOCATION = true;
    private static final SpeedUnit DEFAULT_WIND_SPEED_UNITS = SpeedUnit.MPH;
	private static final TemperatureUnits DEFAULT_TEMPERATURE_UNITS = TemperatureUnits.Fahrenheit;
	private static final PrecipitationUnits DEFAULT_PRECIPITATION_UNITS = PrecipitationUnits.Inch;
	private static final PressureUnits DEFAULT_PRESSURE_UNITS = PressureUnits.Inch_Hg;
	private static final ElevationUnits DEFAULT_ELEVATION_UNITS = ElevationUnits.Feet;
	private static final int DEFAULT_TEMPERATURE_QUEUE_SIZE = 1440; // require 1 reading per minute   
	
	private Locale locale = DEFAULT_LOCALE;
	
	protected static final Map<SpeedUnit, String> windSpeedUnitSuffix = new EnumMap<>(SpeedUnit.class);
	protected static final Map<TemperatureUnits, String> temperatureUnitSymbol = new EnumMap<>(TemperatureUnits.class);
	protected static final Map<PrecipitationUnits, String> precipitationUnitSuffix = new EnumMap<>(PrecipitationUnits.class);
	protected static final Map<PressureUnits, String> pressureUnitSuffix = new EnumMap<>(PressureUnits.class);
	protected static final Map<ElevationUnits, String> elevationUnitSuffix = new EnumMap<>(ElevationUnits.class);
	
	protected final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	
	protected double rainLastSecondMilimeters;

	protected double dewPointDegreesFahrenheit = -1D;
	protected double lastPeakRainRateMillimetersPerHour = -1D;
	
	private int tempAverageCount;
	private double tempAverageCelsius;
	private long sampleDepthHours = Long.MIN_VALUE;
	private double highTemp = Double.MIN_VALUE;
	private double lowTemp = Double.MAX_VALUE;
	private ZonedDateTime tempCollectionStart;

	private boolean startWithSystem = DEFAULT_START_WITH_SYSTEM;
	private boolean connected;
	private boolean enableEvents = true;
	private boolean useGpsForStationLocation = DEFAULT_USE_GPS_FOR_STATION_LOCATION;
	private boolean debug = DEFAULT_DEBUG_MODE;
	
	private ZoneId zoneId = DEFAULT_ZONE_ID;
	
	private double stationLatitudeDegrees = DEFAULT_STATION_LATITUDE_DEGREES;
	private double stationLongitudeDegrees = DEFAULT_STATION_LONGITUDE_DEGREES;
	private double stationElevationMeters = DEFAULT_STATION_ELEVATION_METERS;
	
	private SpeedUnit windSpeedUnits;
	private TemperatureUnits temperatureUnits;
	private PrecipitationUnits precipitationUnits;
	private PressureUnits pressureUnits;
	private ElevationUnits elevationUnits;
	
	protected MeasurementDataGroup thisDayHighTemp = new MeasurementDataGroup();
	protected MeasurementDataGroup thisDayLowTemp = new MeasurementDataGroup();
	
	protected double thisDayLowTempMagnitude = Double.MAX_VALUE;
	protected double thisDayHighTempMagnitude = Double.MIN_VALUE;
	
	private ConsolidatedTime consolidatedTime;
	
	protected int countsPerMinute = -1;
	protected double gammaRadiationMicroSievertsPerHour = -1D;
	protected double betaRadiationMicroSievertsPerHour = -1D;
	protected double alphaRadiationMicroSievertsPerHour = -1D;
	protected double totalVolatileOrganicCompoundsPPB = -1D;
	protected double carbonDioxidePPM = -1D;
	
	protected double tempExteriorCelsius = -999D;
	protected double tempInteriorCelsius = -999D;
	protected double barometricPressureAbsoluteHPA = -1D;
	protected double barometricPressureRelativeHPA = -1D;
	protected double maxDailyWindSpeedMetersPerSecond = -1D;
	protected double gustingWindSpeedMetersPerSecond = -1D;
	protected double currentWindSpeedMetersPerSecond = -1D;
	protected double luminosityWM2 = -1D;
	protected double currentUvLevel = -1D;

	protected int windDirectionTrue = -1;
	protected int currentUvIndex = -1;
	protected int interiorHumidity = -1;
	protected int exteriorHumidity = -1;
	
	protected double rainRateMillimetersPerHour = -1D;
    protected double eventRainMillimeters = -1D;
    protected double dailyRainMillimeters = -1D;
    protected double weeklyRainMillimeters = -1D;
    protected double monthlyRainMillimeters = -1D;
    protected double yearlyRainMillimeters = -1D;
		
	private final ExecutorService executor = Executors.newSingleThreadExecutor();

	private final FifoFixedSizeQueue<Long> periodMeasurementQueue = new FifoFixedSizeQueue<>(8); // Use 8 measurements to calculate the report rate
	private final FifoFixedSizeQueue<MeasurementDataGroup> rainfallQueue = new FifoFixedSizeQueue<>(1440); // 24 hours of measurements every minute
	private final FifoFixedSizeQueue<MeasurementDataGroup> windSpeedQueue = new FifoFixedSizeQueue<>(300); // 5 minutes of measurements every second
	private final FifoFixedSizeQueue<MeasurementDataGroup> baroPressureQueue = new FifoFixedSizeQueue<>(180); // 3 hours of measurements every minute
	private final FifoFixedSizeQueue<MeasurementDataGroup> temperatureQueue = new FifoFixedSizeQueue<>(DEFAULT_TEMPERATURE_QUEUE_SIZE); // 24 hours of measurements every minute during day 
	
	private List<Double> windSpeedCalList = new ArrayList<>(8);
	private List<Integer> windDirectionCalList = new ArrayList<>(8);

	private String deviceId;
	
	public enum Events {
		CLASS_NAME_CHANGE,
		LOCALE,
		ZONE_ID,
		HUMIDITY_GR_PER_M3,
		STATION_EVEVATION_FEET,
		LAST_PEAK_RAIN_RATE_MM_PER_HR,
		LAST_PEAK_RAIN_RATE_MM_PER_HR_ZDT,
		THIS_24H_HIGH_TEMP,
		THIS_24H_LOW_TEMP,
		WIND_SPEED_UNITS_UPDATE,
		TEMP_UNITS_UPDATE,
		PRECIPITATION_UNITS_UPDATE,
		WIND_SPEED_CAL_UPDATE,
		WIND_DIRECTION_CAL_UPDATE,
		CONNECTED,
		DATA_COMPLETE,
		TEMP_SAMPLE_TIME_SPAN
	}
	
	private enum UserPrefs {
		LAST_PEAK_RAIN_RATE_MM_PER_HOUR, 
		LAST_PEAK_RAIN_RATE_ZDT, 
		WIND_SPEED_CAL, 
		WIND_DIRECTION_CAL, 
		IPV4_ADDRESS, 
		TCP_DATA_PORT_NUMBER,
		TCP_BROADCAST_PORT_NUMBER, 
		ZONE_ID, 
		START_WITH_SYSTEM,
		DEBUG_MODE,
		STATION_LATITUDE_DEGREES,
		STATION_LONGITUDE_DEGREES,
		STATION_ELEVATION_METERS,
		LOCALE,
		TIME_ZONE,
		USE_GPS_FOR_STATION_LOCATION,
		WIND_SPEED_UNITS,
		TEMPERATURE_UNITS,
		PRECIPITATION_UNITS,
		PRESSURE_UNITS,
		ELEVATION_UNITS
	}
	
	public enum SpeedUnit {
		MPH,
		KPH,
		FPS,
		MPS
	}
	
	public enum TemperatureUnits {
		Fahrenheit,
		Celsius,
		Kelvin,
		Rankine
	}
	
	public enum PrecipitationUnits {
		Inch,
		Millimeter
	}
	
	public enum ElevationUnits {
		Feet,
		Meters,
		Flight_Level,
		Kilometers,
	}
	
	public enum PressureUnits {
		Inch_Hg,
		Millimeter_Hg,
		Millibar,
		hPa
	}
	
	public enum Direction {
		N,
		NE,
		E,
		SE,
		S,
		SW,
		W,
		NW,
		UNKNOWN
	}
	
	protected AbstractEnvironmentSensor(String deviceId, ConsolidatedTime consolidatedTime, boolean clearAllPreferences) {
		this.deviceId = deviceId;
		this.consolidatedTime = consolidatedTime;
				
		if (clearAllPreferences) {
			clearAllPreferences();
		}
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				close();
			}
		});
		
		initializeComponents();
		loadPreferences(deviceId);
	}
	
	public abstract String getClassName();
	public abstract int getWeatherDataRequestsPerMinute();
	public abstract String getEquipmentCode();
	public abstract String getDeviceModel();
	public abstract String getDeviceManufacturer();
	public abstract String getDeviceSerialNumber();
	public abstract String getHardwareVersion();
	public abstract String getSoftwareVersion();
	public abstract void saveClientSettings();
	public abstract boolean isOpen();
	public abstract void startSensor();
	public abstract void stopSensor();
	public abstract JPanel[] getConfigurationComponentArray();

    public double applyWindSpeedCal(double speed) {
    	final List<Double> calList = getWindSpeedCalList();
    	double s = -1;
    	if (speed >= 0 && speed < 5) {
    		s = speed + calList.get(0);
    	} else if (speed >= 5 && speed < 10) {
    		s = speed + calList.get(1);
    	} else if (speed >= 10 && speed < 15) {
    		s = speed + calList.get(2);
    	} else if (speed >= 15 && speed < 20) {
    		s = speed + calList.get(3);
    	} else if (speed >= 20 && speed < 30) {
    		s = speed + calList.get(4);
    	} else if (speed >= 30 && speed < 60) {
    		s = speed + calList.get(5);
    	} else if (speed >= 60 && speed < 90) {
    		s = speed + calList.get(6);
    	} else if (speed >= 90 && speed < 150) {
    		s = speed + calList.get(7);
    	}
    	return s;
    }
    
    public int applyWindDirectionCal(int dir) {
    	final List<Integer> calList = getWindDirectionCalList();
    	int d = -1;
    	if (dir >= 0 && dir < 45) {
    		d = dir + calList.get(0);
    	} else if (dir >= 45 && dir < 90) {
    		d = dir + calList.get(1);
    	} else if (dir >= 90 && dir < 135) {
    		d = dir + calList.get(2);
    	} else if (dir >= 135 && dir < 180) {
    		d = dir + calList.get(3);
    	} else if (dir >= 180 && dir < 225) {
    		d = dir + calList.get(4);
    	} else if (dir >= 225 && dir < 270) {
    		d = dir + calList.get(5);
    	} else if (dir >= 270 && dir < 315) {
    		d = dir + calList.get(6);
    	} else if (dir >= 315 && dir < 360) {
    		d = dir + calList.get(7);
    	}
    	return d;
    }
	
	private void initializeComponents() {
		windSpeedUnitSuffix.put(SpeedUnit.MPH, "MPH");
		windSpeedUnitSuffix.put(SpeedUnit.KPH, "kM/Hr");
		windSpeedUnitSuffix.put(SpeedUnit.FPS, "Mtr/Sec");
		
		temperatureUnitSymbol.put(TemperatureUnits.Fahrenheit, "\u00B0F");
		temperatureUnitSymbol.put(TemperatureUnits.Celsius, "\u00B0C");
		temperatureUnitSymbol.put(TemperatureUnits.Kelvin, "\u00B0K");
		temperatureUnitSymbol.put(TemperatureUnits.Rankine, "\u00B0R");
		
		precipitationUnitSuffix.put(PrecipitationUnits.Inch, "Inches");
		precipitationUnitSuffix.put(PrecipitationUnits.Millimeter, "Millimeters");
		
		pressureUnitSuffix.put(PressureUnits.Inch_Hg, "Inches Hg");
		pressureUnitSuffix.put(PressureUnits.Millimeter_Hg, "Millimeters Hg");
		pressureUnitSuffix.put(PressureUnits.Millibar, "Millibars");
		pressureUnitSuffix.put(PressureUnits.hPa, "hPa");
		
		elevationUnitSuffix.put(ElevationUnits.Feet, "Feet");
		elevationUnitSuffix.put(ElevationUnits.Flight_Level, "Flight Level");
		elevationUnitSuffix.put(ElevationUnits.Kilometers, "Kilometers");
		elevationUnitSuffix.put(ElevationUnits.Meters, "Meters");
	}
	
	public int getCountsPerMinute() {
		return countsPerMinute;
	}

	public void setCountsPerMinute(int countsPerMinute) {
		this.countsPerMinute = countsPerMinute;
	}

	public double getGammaRadiationMicroSievertsPerHour() {
		return gammaRadiationMicroSievertsPerHour;
	}
	
	public void setGammaRadiationMicroSievertsPerHour(double gammaRadiationMicroSievertsPerHour) {
		this.gammaRadiationMicroSievertsPerHour = gammaRadiationMicroSievertsPerHour;
	}

	public double getBetaRadiationMicroSievertsPerHour() {
		return betaRadiationMicroSievertsPerHour;
	}

	public void setBetaRadiationMicroSievertsPerHour(double betaRadiationMicroSievertsPerHour) {
		this.betaRadiationMicroSievertsPerHour = betaRadiationMicroSievertsPerHour;
	}

	public double getAlphaRadiationMicroSievertsPerHour() {
		return alphaRadiationMicroSievertsPerHour;
	}

	public void setAlphaRadiationMicroSievertsPerHour(double alphaRadiationMicroSievertsPerHour) {
		this.alphaRadiationMicroSievertsPerHour = alphaRadiationMicroSievertsPerHour;
	}

	public double getTotalVolatileOrganicCompoundsPPB() {
		return totalVolatileOrganicCompoundsPPB;
	}

	public void setTotalVolatileOrganicCompoundsPPB(double totalVolatileOrganicCompoundsPPB) {
		this.totalVolatileOrganicCompoundsPPB = totalVolatileOrganicCompoundsPPB;
	}

	public double getCarbonDioxidePPM() {
		return carbonDioxidePPM;
	}

	public void setCarbonDioxidePPM(double carbonDioxidePPM) {
		this.carbonDioxidePPM = carbonDioxidePPM;
	}

	public String getElevationStringFromFeet(double feet, int decimalPlaces) {
		final double elevation;
		if (null == getElevationUnits()) {
			elevation = feet;
		} else {
			elevation = switch (getElevationUnits()) {
				case Feet -> feet;
				case Meters -> Meteorology.convertFeetToMeters(feet);
				case Kilometers -> Meteorology.convertFeetToMeters(feet * 1000);
				case Flight_Level -> feet / 100;
			};
		}
		return toDecimalFormat(elevation, decimalPlaces);
	}
	
	public String getElevationStringFromMeters(double meters, int decimalPlaces) {
		final double elevation;
		if (null == getElevationUnits()) {
			elevation = meters;
		} else {
			elevation = switch (getElevationUnits()) {
				case Feet -> Meteorology.convertMetersToFeet(meters);
				case Meters -> meters;
				case Kilometers -> Meteorology.convertMetersToFeet(meters * 1000);
				case Flight_Level -> Meteorology.convertMetersToFeet(meters) / 100;
			};
		}
		return toDecimalFormat(elevation, decimalPlaces);
	}
	
	public String getRainStringFromInches(double inches) {
		final double length;
		if (getPrecipitationUnits() == PrecipitationUnits.Millimeter) {
			length = Meteorology.convertInchesToMillimeters(inches);
		} else {
			length = inches;
		}
		return toRainFormat(length);
	}

	public String getRainStringFromMillimeters(double millimeters) {
		final double length;
		if (getPrecipitationUnits() == PrecipitationUnits.Inch) {
			length = Meteorology.convertMillimetersToInches(millimeters);
		} else {
			length = millimeters;
		}
		return toRainFormat(length);
	}

	public String getTempStringFromCelsius(double celsius, int decimalPlaces) {
		final double temp;
		if (null == getTemperatureUnits()) {
			temp = celsius;
		} else {
			temp = switch (getTemperatureUnits()) {
				case Fahrenheit -> Meteorology.convertCelsiusToFahrenheit(celsius);
				case Kelvin -> Meteorology.convertCelsiusToKelvin(celsius);
				case Rankine -> Meteorology.convertCelsiusToRankine(celsius);
				default -> celsius;
			};
		}
		return toTemperatureFormat(temp, decimalPlaces);
	}

	public String getTempStringFromFahrenheit(double fahrenheit, int decimalPlaces) {
		final double temp;
		if (null == getTemperatureUnits()) {
			temp = fahrenheit;
		} else {
			temp = switch (getTemperatureUnits()) {
			case Celsius -> Meteorology.convertFahrenheitToCelsius(fahrenheit);
			case Kelvin -> Meteorology.convertFahrenheitToKelvin(fahrenheit);
			case Rankine -> Meteorology.convertFahrenheitToRankine(fahrenheit);
			default -> fahrenheit;
			};
		}
		return toTemperatureFormat(temp, decimalPlaces);
	}
	
	public String getPressureStringFromInchesHg(double inHg, int decimalPlaces) {
		final double pressure;
		if (null == getPressureUnits()) {
			pressure = inHg;
		} else {
			pressure = switch (getPressureUnits()) {
				case Inch_Hg -> inHg;
				case Millimeter_Hg -> Meteorology.convertInchesToMillimeters(inHg);
				case Millibar -> Meteorology.convertInchesHgToHPa(inHg);
				case hPa -> Meteorology.convertInchesHgToHPa(inHg);
			};
		}
		return toDecimalFormat(pressure, decimalPlaces);
	}
	
	public String getPressureStringFromHPa(double hpa, int decimalPlaces) {
		final double pressure;
		if (null == getPressureUnits()) {
			pressure = hpa;
		} else {
			pressure = switch (getPressureUnits()) {
				case Inch_Hg -> Meteorology.convertHPaToInchesHg(hpa);
				case Millimeter_Hg -> Meteorology.convertHPaToMillimetersHg(hpa);
				case Millibar -> hpa;
				case hPa -> hpa;
			};
		}
		return toDecimalFormat(pressure, decimalPlaces);
	}
	
	public String getWindSpeedStringFromMetersPerSecond(double mps, int decimalPlaces) {
		final double windSpeed;
		if (null == getWindSpeedUnits()) {
			windSpeed = mps;
		} else {
			windSpeed = switch (getWindSpeedUnits()) {
			case FPS -> Meteorology.convertMetersToFeet(mps);
			case KPH -> (mps / 1000.0) * 3600.0;
			case MPH -> Meteorology.convertMetersPerSecondToMPH(mps);
			case MPS -> mps;
			};
		}
		return toDecimalFormat(windSpeed, decimalPlaces);
	}

	public static String toRainFormat(double d) {
		final int dp = d >= 100 ? 1 : 2;
		final DecimalFormat df = new DecimalFormat();
		df.setGroupingUsed(false);
		df.setMaximumFractionDigits(dp);
		df.setMinimumFractionDigits(dp);
		df.setRoundingMode(RoundingMode.HALF_UP);
		return df.format(d);
	}

	public static String toDecimalFormat(double value, int decimalPlaces) {
		final DecimalFormat df = new DecimalFormat();
		df.setGroupingUsed(false);
		df.setMaximumFractionDigits(decimalPlaces);
		df.setMinimumFractionDigits(decimalPlaces);
		df.setRoundingMode(RoundingMode.HALF_UP);
		return df.format(value);
	}

	public static String toRadiationFormat(double value, int decimalPlaces) {
		final DecimalFormat df = new DecimalFormat();
		df.setGroupingUsed(false);
		df.setMaximumFractionDigits(decimalPlaces);
		df.setMinimumFractionDigits(decimalPlaces);
		df.setRoundingMode(RoundingMode.HALF_UP);
		return df.format(value) + " \u00B5Sv/Hr";
	}

	public static String toTemperatureFormat(double value, int decimalPlaces) {
		final DecimalFormat df = new DecimalFormat();
		df.setGroupingUsed(false);
		df.setMaximumFractionDigits(decimalPlaces);
		df.setMinimumFractionDigits(decimalPlaces);
		df.setRoundingMode(RoundingMode.HALF_UP);
		df.setPositivePrefix("+");
		return df.format(value);
	}
	
	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	private void clearAllPreferences() {
		try {
			AbstractEnvironmentSensor.userPrefs.clear();
		} catch (final BackingStoreException ex) {
			if (isDebug()) {
				LOG.log(Level.WARNING, ex.getMessage(), ex);
			}
		}
	}
	
	private void loadPreferences(String deviceId) {
		windDirectionCalList.clear();
		windSpeedCalList.clear();
		
		for (int i = 0; i < 8; i++) {
			windSpeedCalList.add(userPrefs.getDouble(deviceId + UserPrefs.WIND_SPEED_CAL + String.valueOf(i), 0));
			windDirectionCalList.add(userPrefs.getInt(deviceId + UserPrefs.WIND_DIRECTION_CAL.name() + String.valueOf(i), 0));
		}

		windSpeedUnits = SpeedUnit.values()[userPrefs.getInt(deviceId + UserPrefs.WIND_SPEED_UNITS.name(), DEFAULT_WIND_SPEED_UNITS.ordinal())];
		temperatureUnits = TemperatureUnits.values()[userPrefs.getInt(deviceId + UserPrefs.TEMPERATURE_UNITS.name(), DEFAULT_TEMPERATURE_UNITS.ordinal())];
		precipitationUnits = PrecipitationUnits.values()[userPrefs.getInt(deviceId + UserPrefs.PRECIPITATION_UNITS.name(), DEFAULT_PRECIPITATION_UNITS.ordinal())];
		pressureUnits = PressureUnits.values()[userPrefs.getInt(deviceId + UserPrefs.PRESSURE_UNITS.name(), DEFAULT_PRESSURE_UNITS.ordinal())];
		elevationUnits = ElevationUnits.values()[userPrefs.getInt(deviceId + UserPrefs.ELEVATION_UNITS.name(), DEFAULT_ELEVATION_UNITS.ordinal())];
		stationElevationMeters = userPrefs.getDouble(deviceId + UserPrefs.STATION_ELEVATION_METERS.name(), DEFAULT_STATION_ELEVATION_METERS);
		stationLongitudeDegrees = userPrefs.getDouble(deviceId + UserPrefs.STATION_LONGITUDE_DEGREES.name(), DEFAULT_STATION_LONGITUDE_DEGREES);
		stationLatitudeDegrees = userPrefs.getDouble(deviceId + UserPrefs.STATION_LATITUDE_DEGREES.name(), DEFAULT_STATION_LATITUDE_DEGREES);
		zoneId = ZoneId.of(userPrefs.get(deviceId + UserPrefs.ZONE_ID.name(), DEFAULT_ZONE_ID.getId()));
		startWithSystem = userPrefs.getBoolean(deviceId + UserPrefs.START_WITH_SYSTEM.name(), DEFAULT_START_WITH_SYSTEM);
		debug = userPrefs.getBoolean(deviceId + UserPrefs.DEBUG_MODE.name(), DEFAULT_DEBUG_MODE);
		useGpsForStationLocation = userPrefs.getBoolean(deviceId + UserPrefs.USE_GPS_FOR_STATION_LOCATION.name(), DEFAULT_USE_GPS_FOR_STATION_LOCATION);
		locale = Locale.forLanguageTag(userPrefs.get(deviceId + UserPrefs.LOCALE.name(), DEFAULT_LOCALE.getLanguage()));
	}

	public static BidiMap<String, String> getCatalogMap() {
		final BidiMap<String, String> catalog = new DualHashBidiMap<>();
		catalog.put("bosch.BME280", "Bosch BME280 Temp/Pressure/Humidity Sensor");
		catalog.put("ecowitt.GW2000B", "Ecowitt GW2000B Gateway");
		return catalog;
	}
	
	public void savePreferences() {
		savePreferences(deviceId);
	}

	private void savePreferences(String deviceId) {
		for (int i = 0; i < 8; i++) {
			userPrefs.putDouble(deviceId + UserPrefs.WIND_SPEED_CAL + i, windSpeedCalList.get(i));
			userPrefs.putDouble(deviceId + UserPrefs.WIND_DIRECTION_CAL + i, windDirectionCalList.get(i));
		}

		userPrefs.putInt(deviceId + UserPrefs.WIND_SPEED_UNITS.name(), getWindSpeedUnits().ordinal());
		userPrefs.putInt(deviceId + UserPrefs.TEMPERATURE_UNITS.name(), getTemperatureUnits().ordinal());
		userPrefs.putInt(deviceId + UserPrefs.PRECIPITATION_UNITS.name(), getPrecipitationUnits().ordinal());
		userPrefs.putInt(deviceId + UserPrefs.PRESSURE_UNITS.name(), getPressureUnits().ordinal());
		userPrefs.putInt(deviceId + UserPrefs.ELEVATION_UNITS.name(), getElevationUnits().ordinal());
		userPrefs.putDouble(deviceId + UserPrefs.STATION_ELEVATION_METERS.name(), getStationElevationMeters());
		userPrefs.putDouble(deviceId + UserPrefs.STATION_LONGITUDE_DEGREES.name(), getStationLongitudeDegrees());
		userPrefs.putDouble(deviceId + UserPrefs.STATION_LATITUDE_DEGREES.name(), getStationLatitudeDegrees());
		userPrefs.put(deviceId + UserPrefs.LOCALE.name(), locale.getLanguage());
		userPrefs.put(deviceId + UserPrefs.ZONE_ID.name(), zoneId.getId());
		userPrefs.putBoolean(deviceId + UserPrefs.START_WITH_SYSTEM.name(), startWithSystem);
		userPrefs.putBoolean(deviceId + UserPrefs.DEBUG_MODE.name(), debug);
		userPrefs.putBoolean(deviceId + UserPrefs.USE_GPS_FOR_STATION_LOCATION.name(), useGpsForStationLocation);

		saveClientSettings();
	}
	
	public void notifyClassNameChange(String className) {
		pcs.firePropertyChange(Events.CLASS_NAME_CHANGE.name(), null, className);
	}
	
	public SpeedUnit getWindSpeedUnits() {
		return windSpeedUnits;
	}

	public TemperatureUnits getTemperatureUnits() {
		return temperatureUnits;
	}

	public PrecipitationUnits getPrecipitationUnits() {
		return precipitationUnits;
	}
	
	public ElevationUnits getElevationUnits() {
		return elevationUnits;
	}
	
	public PressureUnits getPressureUnits() {
		return pressureUnits;
	}
	
	public void setWindSpeedUnits(SpeedUnit windSpeedUnits) {
		if (isEnableEvents()) {
			pcs.firePropertyChange(Events.WIND_SPEED_UNITS_UPDATE.name(), this.windSpeedUnits, windSpeedUnits);
		}
		this.windSpeedUnits = windSpeedUnits;
	}

	public void setTemperatureUnits(TemperatureUnits temperatureUnits) {
		if (isEnableEvents()) {
			pcs.firePropertyChange(Events.TEMP_UNITS_UPDATE.name(), this.temperatureUnits, temperatureUnits);
		}
		this.temperatureUnits = temperatureUnits;
	}

	public void setPrecipitationUnits(PrecipitationUnits precipitationUnits) {
		if (isEnableEvents()) {
			pcs.firePropertyChange(Events.PRECIPITATION_UNITS_UPDATE.name(), this.precipitationUnits, precipitationUnits);
		}
		this.precipitationUnits = precipitationUnits;
	}

	public void stopMonitor() {
		setEnableEvents(false);
	}
	
	public boolean isEnableEvents() {
		return enableEvents;
	}

	public void setEnableEvents(boolean enableEvents) {
		this.enableEvents = enableEvents;
	}

	public boolean isConnected() {
		return connected;
	}
	
	public void setConnected(boolean connected) {
		if (isEnableEvents()) {
			pcs.firePropertyChange(Events.CONNECTED.name(), this.connected, connected);
		}
		this.connected = connected;
	}
	
	// TODO: calculate region from Lat/Lon
	
	public double getStationLatitudeDegrees() {
		return stationLatitudeDegrees;
	}

	public void setStationLatitudeDegrees(double stationLatitudeDegrees) {
		this.stationLatitudeDegrees = stationLatitudeDegrees;
	}

	public Point2D getStationPosition() {
		return new Point2D.Double(stationLongitudeDegrees, stationLatitudeDegrees);
	}
	
	public void setGpsTimeInMillis(long gpsTime) {
		consolidatedTime.setGpsTimeInMillis(gpsTime);
	}
	
	public double getStationLongitudeDegrees() {
		return stationLongitudeDegrees;
	}

	public void setStationLongitudeDegrees(double stationLongitudeDegrees) {
		this.stationLongitudeDegrees = stationLongitudeDegrees;
	}

	public boolean isStartWithSystem() {
		return startWithSystem;
	}
	
	public void setStartWithSystem(boolean startWithSystem) {
		this.startWithSystem = startWithSystem;
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		if (isEnableEvents()) {
			pcs.firePropertyChange(Events.LOCALE.name(), this.locale, locale);
		}
		this.locale = locale;
	}

	public boolean isUseGpsForStationLocation() {
		return useGpsForStationLocation;
	}

	public void setUseGpsForStationLocation(boolean useGpsForStationLocation) {
		this.useGpsForStationLocation = useGpsForStationLocation;
	}
	
	public void setStationElevationMeters(double stationElevationMeters) {
		this.stationElevationMeters = stationElevationMeters;
	}
	
	public double getStationElevationMeters() {
		return stationElevationMeters;
	}
	
	public double getPressureAltitudeFeet() {
		return Meteorology.getPressureAltitudeFeet(getBarometricPressureAbsoluteHPA());
	}
	
	public double getAltimeterPressureInHg() {
		return Meteorology.getAltimeterInchesHg(getBarometricPressureAbsoluteHPA(), getStationElevationMeters());
	}

	public double getStationPressureInHg() {
		return Meteorology.getStationPressureInHgFromAltimeterInHg(getAltimeterPressureInHg(), getStationElevationMeters());
	}
	
	public double getSeaLevelPressureMillibars() {
		return Meteorology.getSeaLevelPressureMillibars(getBarometricPressureAbsoluteHPA(), getStationElevationMeters());
	}
	
	public double getWindChillDegreesFahrenheit() {
		return Meteorology.getWindChillDegreesFahrenheit(getTempExteriorFahrenheit(), getCurrentWindSpeed(SpeedUnit.MPH));
	}
	
	public List<Double> getWindSpeedCalList() {
		return new ArrayList<>(windSpeedCalList);
	}

	public void setWindSpeedCalList(List<Double> windSpeedCalList) {
		if (isEnableEvents()) {
			pcs.firePropertyChange(Events.WIND_SPEED_CAL_UPDATE.name(), this.windSpeedCalList, windSpeedCalList);
		}
		this.windSpeedCalList = new ArrayList<>(windSpeedCalList);
	}

	public List<Integer> getWindDirectionCalList() {
		return new ArrayList<>(windDirectionCalList);
	}

	public void setWindDirectionCalList(List<Integer> windDirectionCalList) {
		if (isEnableEvents()) {
			pcs.firePropertyChange(Events.WIND_DIRECTION_CAL_UPDATE.name(), this.windDirectionCalList, windDirectionCalList);
		}
		this.windDirectionCalList = new ArrayList<>(windDirectionCalList);
	}

	public ZonedDateTime getCurrentZonedDateTime() {
		return consolidatedTime.getBestZonedDateTime(getZoneId());
	}

	public ZonedDateTime getZonedDateTimeUTC() {
		return consolidatedTime.getBestZonedDateTimeUTC();
	}
	
	private void setLastPeakRainRate(double lastPeakRainRateMmPerHour, ZonedDateTime lastPeakRainRateZdt) {
		if (isEnableEvents()) {
			pcs.firePropertyChange(Events.LAST_PEAK_RAIN_RATE_MM_PER_HR.name(), null, new MeasurementDataGroup(lastPeakRainRateMmPerHour, lastPeakRainRateZdt));
		}
	}
	
	public ZoneId getZoneId() {
		return zoneId;
	}

	public void setZoneId(ZoneId zoneId) {
		if (isEnableEvents()) {
			pcs.firePropertyChange(Events.ZONE_ID.name(), this.zoneId, zoneId);
		}
		this.zoneId = zoneId;
	}
	
	public double getRainLastSecondMilimeters() {
		return rainLastSecondMilimeters;
	}

	public double getDewPointDegreesFahrenheit() {
		return dewPointDegreesFahrenheit;
	}

	public double getLastPeakRainRateMillimetersPerHour() {
		return lastPeakRainRateMillimetersPerHour;
	}

	public double getTempExteriorCelsius() {
		return tempExteriorCelsius;
	}

	public double getTempInteriorCelsius() {
		return tempInteriorCelsius;
	}

	public double getBarometricPressureAbsoluteHPA() {
		return barometricPressureAbsoluteHPA;
	}

	public double getBarometricPressureRelativeHPA() {
		return barometricPressureRelativeHPA;
	}

	public double getMaxDailyWindSpeed(SpeedUnit speedUnits) {
		return fromMetersPerSecond(maxDailyWindSpeedMetersPerSecond, speedUnits);
	}

	public double getGustingWindSpeed(SpeedUnit speedUnits) {
		return fromMetersPerSecond(gustingWindSpeedMetersPerSecond, speedUnits);
	}

	public double getCurrentWindSpeed(SpeedUnit speedUnits) {
		return fromMetersPerSecond(currentWindSpeedMetersPerSecond, speedUnits);
	}

	public double getLuminosityWM2() {
		return luminosityWM2;
	}

	public double getCurrentUvLevel() {
		return currentUvLevel;
	}

	public int getWindDirectionTrue() {
		return windDirectionTrue;
	}

	public int getCurrentUvIndex() {
		return currentUvIndex;
	}

	public int getInteriorHumidity() {
		return interiorHumidity;
	}

	public int getExteriorHumidity() {
		return exteriorHumidity;
	}

	public double getExteriorHumidityGM3() {
		return Meteorology.getAbsoluteHumidityGM3(tempExteriorCelsius, exteriorHumidity);
	}
	
	public double getRainRateMillimetersPerHour() {
		return rainRateMillimetersPerHour;
	}

	public double getEventRainMillimeters() {
		return eventRainMillimeters;
	}

	public double getDailyRainMillimeters() {
		return dailyRainMillimeters;
	}

	public double getWeeklyRainMillimeters() {
		return weeklyRainMillimeters;
	}

	public double getMonthlyRainMillimeters() {
		return monthlyRainMillimeters;
	}

	public double getYearlyRainMillimeters() {
		return yearlyRainMillimeters;
	}

	public double getYearlyRainInches() {
		return Meteorology.convertMillimetersToInches(getYearlyRainMillimeters());
	}
	
	public double getMonthlyRainInches() {
		return Meteorology.convertMillimetersToInches(getMonthlyRainMillimeters());
	}
	
	public double getWeeklyRainInches() {
		return Meteorology.convertMillimetersToInches(getWeeklyRainMillimeters());
	}
	
	public double getDailyRainInches() {
		return Meteorology.convertMillimetersToInches(getDailyRainMillimeters());
	}
	
	public double getRainFallInchesLastHour() {
		return Meteorology.convertMillimetersToInches(getRainfallMillimetersLastHour());
	}
	
	public double getRainFallInchesLast24Hours() {
		return Meteorology.convertMillimetersToInches(getRainfallMillimetersLast24Hours());
	}
	
	public double getDewPointFahrenheit() {
		return Meteorology.convertCelsiusToFahrenheit(getDewPointCelsius());
	}

	public double getTempInteriorFahrenheit() {
		return Meteorology.convertCelsiusToFahrenheit(getTempInteriorCelsius());
	}

	public double getTempExteriorFahrenheit() {
		return Meteorology.convertCelsiusToFahrenheit(getTempExteriorCelsius());
	}

	public double getDewPointCelsius() {
		return Meteorology.getDewPointCelsiusDavis(getExteriorHumidity(), getTempExteriorCelsius());
	}
	
	public double getHeatIndexFahrenheit() {
		return Meteorology.getHeatIndexDegreesFahrenheit(Meteorology.convertCelsiusToFahrenheit(getTempExteriorCelsius()), getExteriorHumidity());
	}

	private void updatePeriodMeasurementQueue(long millis) {
		periodMeasurementQueue.offer(millis);
	}
	
	private void updateTempDependentVariables(double tempExteriorCelsius) {	
		if (tempAverageCount < getWeatherDataRequestsPerMinute()) {
			tempAverageCelsius += tempExteriorCelsius;
			tempAverageCount++;
		}
		if (tempAverageCount == getWeatherDataRequestsPerMinute()) {
			final double avgTemp = tempAverageCelsius / tempAverageCount;
			final ZonedDateTime zdt = consolidatedTime.getBestZonedDateTime(getZoneId());
			if (tempCollectionStart == null) {
				tempCollectionStart = zdt;
			}
			temperatureQueue.offer(new MeasurementDataGroup(avgTemp, zdt));
			tempAverageCelsius = 0;
			tempAverageCount = 0;
			executor.execute(new AnalyzeTemperatureTrends(temperatureQueue));
		}
	}
	
	private void updateRainRateDependentVariables(double rainRateMillimetersPerHour) {
		if (lastPeakRainRateMillimetersPerHour < rainRateMillimetersPerHour) {
			lastPeakRainRateMillimetersPerHour = rainRateMillimetersPerHour;
			setLastPeakRainRate(lastPeakRainRateMillimetersPerHour, consolidatedTime.getBestZonedDateTime(getZoneId()));	
		}
	}
	
	private void updateBarometerQueue(double baroPressureMillibars) {
		baroPressureQueue.offer(new MeasurementDataGroup(baroPressureMillibars, consolidatedTime.getBestZonedDateTime(getZoneId())));
	}
	
	// This method is updated every time a reading is transmitted from the sensor.
	// The GW2000B/WS-90 sensor reports the instantaneous rain rate in millimeters per hour. It does not have an accumulator 
	// bucket, and thus can only estimate the rain rate, by checking periodically for rain on the sensor.
	// The sensor sample rate used by the WS90 calculator is not published.
	// However, we require an actual rain amount that has occurred during each measurement period.
	// This method converts the incoming report in millimeters/hour to millimeters of rain during the average period length
	// and pushes the value to the rainfall queue for later analysis.
	private void updateRainfallQueue(double rainRateMillimetersPerHour) {
		// An average report period is calculated as the data is received:
		final double reportPeriodMilliseconds = getAverageMeasurementReportPeriodMillis();
		// The report period is calculated:
		final double rainRateMillimetersPerMillisecond = rainRateMillimetersPerHour / 3600000; // 3600000 == number of milliseconds per hour.
		if (reportPeriodMilliseconds > 0) {
			final double rainfallMillimetersThisPeriod = rainRateMillimetersPerMillisecond * reportPeriodMilliseconds;
			rainfallQueue.offer(new MeasurementDataGroup(rainfallMillimetersThisPeriod, ZonedDateTime.now()));
		}
	}
	
	private void updateAbsoluteHumidityGramsPerCubicMeter(double gramsPerCubicMeter) {
		pcs.firePropertyChange(Events.HUMIDITY_GR_PER_M3.name(), null, gramsPerCubicMeter);
	}
	
	private void updateWindSpeedQueue(double windSpeed, int direction) {
		windSpeedQueue.offer(new MeasurementDataGroup(windSpeed, direction, consolidatedTime.getBestZonedDateTime(getZoneId())));
	}
	
	public double getAltitudeFeet() {
		return Meteorology.getAltimeterInFeetFromMillibars(getBarometricPressureAbsoluteHPA(), Meteorology.convertMetersToFeet(getStationElevationMeters()));
	}
	
	public double getBarometerCorrectionFactorMSW() {
		return 0;
	}
	
	public double getBarometerCorrectionFactorLSW() {
		return 0;
	}
	
	// This method searches the entire measurement set and finds the first measurement that is so many 'seconds'
	// back in time. This type of search is necessary because we can not be sure exactly when this measurement was reported.
	private int findStartIndexAtZdtMinusSeconds(FifoFixedSizeQueue<MeasurementDataGroup> queue, long seconds) {
		int n = 0;
		if (!queue.isEmpty()) {
			final MeasurementDataGroup[] mdg = queue.toArray(new MeasurementDataGroup[queue.size()]);
			final ZonedDateTime firstEntry = mdg[0].getZdt();
			final ZonedDateTime latestEntry = mdg[mdg.length-1].getZdt();
			// Check to make sure the queue is long enough to find an entry.
			if (latestEntry.minusSeconds(seconds).isAfter(firstEntry)) {
				// Work backwards from end of the FIFO queue, looking back 'seconds' to find the index of the specified record. 
				for (int i = mdg.length-1; i >= 0; i--) {
					if (mdg[i].getZdt().plusSeconds(seconds).isBefore(latestEntry)) {
						n = i;
						break;
					}
				}
			}
		}
		return n;
	}

	public double getRainfallMillimetersLast24Hours() {	
		final int n = findStartIndexAtZdtMinusSeconds(rainfallQueue, 1440 * 60L);
		if (!rainfallQueue.isEmpty()) {
			final MeasurementDataGroup[] mdg = rainfallQueue.toArray(new MeasurementDataGroup[rainfallQueue.size()]);
			double total = 0;
			for (int i = n; i < mdg.length; i++) {
				total += mdg[i].getMagnitude();
			}
			return total;
		}
		return 0;
	}
	
	public double getRainfallMillimetersLastHour() {	
		final int n = findStartIndexAtZdtMinusSeconds(rainfallQueue, 60 * 60L);
		if (!rainfallQueue.isEmpty()) {
			final MeasurementDataGroup[] mdg = rainfallQueue.toArray(new MeasurementDataGroup[rainfallQueue.size()]);
			double total = 0;
			for (int i = n; i < mdg.length; i++) {
				total += mdg[i].getMagnitude();
			}
			return total;
		}
		return 0;
	}

	private synchronized long getAverageMeasurementReportPeriodMillis() {
		final int n = periodMeasurementQueue.size();
		final Long[] millis = periodMeasurementQueue.toArray(new Long[n]);
		final long[] period = new long[n];
		if (n > 1) {
			long total = 0;
			for (int i = 0; i < n - 1; i++) {
				final long timeOfReport = millis[i + 1];
				final long timeOfReportPrevious = millis[i];
				period[i] =  timeOfReport - timeOfReportPrevious;
				total += period[i];
			}
			return total / (n - 1);
		}
		return 0;
	}
	
	public double getPeakPeriodicWindSpeedMeasurement(int minutes, SpeedUnit windSpeedUnits) {
		final int n = findStartIndexAtZdtMinusSeconds(windSpeedQueue, minutes * 60L);
		int maxSpeedIndex = n;
		
		final MeasurementDataGroup[] mdg = windSpeedQueue.toArray(new MeasurementDataGroup[windSpeedQueue.size()]);
		
		if (!windSpeedQueue.isEmpty()) {
			for (int i = n; i < mdg.length; i++) {
				final double m = mdg[i].getMagnitude();
				if (m > mdg[maxSpeedIndex].getMagnitude()) {
					maxSpeedIndex = i;
				}
			}
		}
		
		return fromMetersPerSecond(mdg[maxSpeedIndex].getMagnitude(), windSpeedUnits);
	}
	
	public double getPeakPeriodicWindDirectionMeasurement(int minutes) {
		final int n = findStartIndexAtZdtMinusSeconds(windSpeedQueue, minutes * 60L);
		int maxSpeedIndex = n;
		
		final MeasurementDataGroup[] mdg = windSpeedQueue.toArray(new MeasurementDataGroup[windSpeedQueue.size()]);
		
		if (!windSpeedQueue.isEmpty()) {
			for (int i = n; i < mdg.length; i++) {
				final double m = mdg[i].getMagnitude();
				if (m > mdg[maxSpeedIndex].getMagnitude()) {
					maxSpeedIndex = i;
				}
			}
		}
		
		return mdg[maxSpeedIndex].getAngle();
	}
	
	// The lowest value over x minutes will reveal the maximum sustained wind speed over that period
	public double getSustainedPeriodicWindSpeedMeasurement(int minutes, SpeedUnit windSpeedUnits) {
		final int x = findStartIndexAtZdtMinusSeconds(windSpeedQueue, minutes * 60L);
		int minSpeedIndex = x;
		
		final MeasurementDataGroup[] mdg = windSpeedQueue.toArray(new MeasurementDataGroup[windSpeedQueue.size()]);
		
		if (!windSpeedQueue.isEmpty()) {
			for (int i = x; i < mdg.length; i++) {
				final double m = mdg[i].getMagnitude();
				if (m < mdg[minSpeedIndex].getMagnitude()) {
					minSpeedIndex = i;
				}
			}
		}

		return fromMetersPerSecond(mdg[minSpeedIndex].getMagnitude(), windSpeedUnits);
	}

	public double getSustainedPeriodicWindDirectionMeasurement(int minutes) {
		final int x = findStartIndexAtZdtMinusSeconds(windSpeedQueue, minutes * 60L);
		final int[] dir = {0, 0, 0, 0, 0, 0, 0, 0};
		
		final MeasurementDataGroup[] mdg = windSpeedQueue.toArray(new MeasurementDataGroup[windSpeedQueue.size()]);
		
		if (!windSpeedQueue.isEmpty()) {
			for (int i = x; i < mdg.length; i++) {
				final double d = mdg[i].getAngle();
				if (d >= 337.5 || d < 22.5) {
					dir[0]++;
				} else if (d >= 22.5 && d < 67.5) {
					dir[1]++;
				} else if (d >= 67.5 && d < 112.5) {
					dir[2]++;
				} else if (d >= 112.5 && d < 157.5) {
					dir[3]++;
				} else if (d >= 157.5 && d < 202.5) {
					dir[4]++;
				} else if (d >= 202.5 && d < 247.5) {
					dir[5]++;
				} else if (d >= 247.5 && d < 292.5) {
					dir[6]++;
				} else if (d >= 292.5 && d < 337.5) {
					dir[7]++;
				}
			}
		}
		
		int maxElement = dir[0];
		for (int i = 1; i < dir.length; i++) {
			if (i > maxElement) {
				maxElement = i;
			}
		}

		return fromDirectionElement(maxElement);
	}
	public static double fromMetersPerSecond(double metersPerSecond, SpeedUnit speedUnits) {
		double speed = metersPerSecond;
		if (speedUnits == SpeedUnit.FPS) {
			speed = Meteorology.convertMetersToFeet(speed);
		} else if (speedUnits == SpeedUnit.KPH) {
			speed = Meteorology.convertMetersPerSecondToKPH(speed);
		} else if (speedUnits == SpeedUnit.MPH) {
			speed = Meteorology.convertMetersPerSecondToMPH(speed);
		}
		return speed;
	}
	
	public static double fromMillibars(double millibars, PressureUnits pressureUnits) {
		double pressure = millibars;
		if (pressureUnits == PressureUnits.Inch_Hg) {
			pressure = Meteorology.convertMillibarsToInHg(millibars);
		} else if (pressureUnits == PressureUnits.Millimeter_Hg) {
			pressure = Meteorology.convertInchesToMillimeters(Meteorology.convertMillibarsToInHg(millibars));
		}
		return pressure;
	}
	
	public static Direction getDirectionCode(double d) {
		if (d >= 337.5 || d < 22.5) {
			return Direction.N;
		} else if (d >= 22.5 && d < 67.5) {
			return Direction.NE;
		} else if (d >= 67.5 && d < 112.5) {
			return Direction.E;
		} else if (d >= 112.5 && d < 157.5) {
			return Direction.SE;
		} else if (d >= 157.5 && d < 202.5) {
			return Direction.S;
		} else if (d >= 202.5 && d < 247.5) {
			return Direction.SW;
		} else if (d >= 247.5 && d < 292.5) {
			return Direction.W;
		} else if (d >= 292.5 && d < 337.5) {
			return Direction.NW;
		} else {
			return Direction.UNKNOWN;
		}
	}
	
	public static double toDirectionDegrees(Direction dir) {
		if (dir == Direction.N) {
			return 0;
		} else if (dir == Direction.NE) {
			return 45;
		} else if (dir == Direction.E) {
			return 90;
		} else if (dir == Direction.SE) {
			return 135;
		} else if (dir == Direction.S) {
			return 180;
		} else if (dir == Direction.SW) {
			return 225;
		} else if (dir == Direction.W) {
			return 270;
		} else if (dir == Direction.NW) {
			return 315;
		} else {
			return -1;
		}
	}
	
	public static double fromDirectionElement(int element) {
		return element * 45D;
	}

	public double getAveragePeriodicWindSpeedMeasurement(int minutes, SpeedUnit windSpeedUnits) {
		final int n = findStartIndexAtZdtMinusSeconds(windSpeedQueue, minutes * 60L);
		final MeasurementDataGroup[] mdg = windSpeedQueue.toArray(new MeasurementDataGroup[windSpeedQueue.size()]);
		double avgSpeed = 0;
		if (n > 0) {
			double totalSpeed = 0;
			int c = 0;
			for (int i = n; i < mdg.length; i++) {
				totalSpeed += mdg[i].getMagnitude();
				c++;
				avgSpeed = totalSpeed / c;
			}
		}
		return fromMetersPerSecond(avgSpeed, windSpeedUnits);
	}
	
	public double getAveragePeriodicWindDirectionMeasurement(int minutes) {
		final int n = findStartIndexAtZdtMinusSeconds(windSpeedQueue, minutes * 60L);
		final MeasurementDataGroup[] mdg = windSpeedQueue.toArray(new MeasurementDataGroup[windSpeedQueue.size()]);
		double avgDir = 0;
		if (n > 0) {
			double totalDir = 0;
			int c = 0;
			for (int i = n; i < mdg.length; i++) {
				totalDir += mdg[i].getMagnitude();
				c++;
				avgDir = totalDir / c;
			}
		}
		return avgDir;
	}
	
	public double get3HourBarometerPressureDeltaMillibars() {
		final int n = findStartIndexAtZdtMinusSeconds(baroPressureQueue, 180);
		int minMeasIndex = n;
		int maxMeasIndex = n;
		final MeasurementDataGroup[] mdg = baroPressureQueue.toArray(new MeasurementDataGroup[baroPressureQueue.size()]);
		if (n > 0) {
			for (int i = n; i < mdg.length; i++) {
				final double m = mdg[i].getMagnitude();
				if (m < mdg[minMeasIndex].getMagnitude()) {
					minMeasIndex = i;
				}
				if (m > mdg[maxMeasIndex].getMagnitude()) {
					maxMeasIndex = i;
				}
			}
		}
		return mdg[maxMeasIndex].getMagnitude() - mdg[minMeasIndex].getMagnitude();
	}
	
	// this method gets called every time the sensor transmits a complete measurement set
	public void setDataComplete() {
		updatePeriodMeasurementQueue(System.currentTimeMillis());		
		updateBarometerQueue(getBarometricPressureRelativeHPA());
		updateRainfallQueue(getRainRateMillimetersPerHour());
		updateRainRateDependentVariables(getRainRateMillimetersPerHour());
		updateTempDependentVariables(getTempExteriorCelsius());
		updateWindSpeedQueue(getCurrentWindSpeed(SpeedUnit.MPS), getWindDirectionTrue());

		updateAbsoluteHumidityGramsPerCubicMeter(getExteriorHumidityGM3());
		
		pcs.firePropertyChange(Events.DATA_COMPLETE.name(), null, consolidatedTime.getBestZonedDateTime(getZoneId()));
	}
	
	public ConsolidatedTime getConsolidatedTime() {
		return consolidatedTime;
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}

	public boolean isPropertyChangeListenerRegistered() {
		return pcs.getPropertyChangeListeners().length > 0;
	}

	public PropertyChangeListener[] getPropertyChangeListeners() {
		return pcs.getPropertyChangeListeners();
	}

	public int getCatalogMapIndex() {
		int r = 0;
		for (int i = 0; i < getCatalogMap().size(); i++) {
			if (getCatalogMap().keySet().toArray()[i].equals(getClassName())) {
				r = i;
				break;
			}
		}
		return r;
	}

	public String getSensorClassNameFor(String compositeName) {
		return getSensorClassNameFor(compositeName, compositeName);
	}
	
	public static String getSensorClassNameFor(String manufacturer, String model) {
		String className = (String) AbstractEnvironmentSensor.getCatalogMap().keySet().toArray()[0];
		try {
			final Iterator<String> iterator = AbstractEnvironmentSensor.getCatalogMap().keySet().iterator();
			while (iterator.hasNext()) {
				final String sensorElement = iterator.next();
			    if (manufacturer.toUpperCase(DEFAULT_LOCALE).trim().contains(sensorElement.toUpperCase(DEFAULT_LOCALE).trim())
						&& model.toUpperCase(DEFAULT_LOCALE).trim().contains(sensorElement.toUpperCase(DEFAULT_LOCALE).trim())) {
					className = sensorElement;
					break;
				}
			}
		} catch (NullPointerException ex) {
			LOG.log(Level.SEVERE, "getInstanceFor(String manufacturer, String model) returns NULL", ex);
		}
		return className;
	}

	public static synchronized AbstractEnvironmentSensor getInstance(String className, ConsolidatedTime consolidatedTime, Boolean clearAllPreferences) {
		boolean isValidClassName = false;
		final Iterator<String> iterator = AbstractEnvironmentSensor.getCatalogMap().keySet().iterator();
		while (iterator.hasNext()) {
			final String cn = iterator.next();
			if (cn.equals(className)) {
				isValidClassName = true;
				break;
			}
		}
		final Class<?> classTemp;
		AbstractEnvironmentSensor instance = null;
		try {
			if (!isValidClassName) {
				className = (String) AbstractEnvironmentSensor.getCatalogMap().keySet().toArray()[0];
			}
			classTemp = Class.forName(className);
			final Class<?>[] cArg = new Class<?>[2];
			cArg[0] = ConsolidatedTime.class;
			cArg[1] = Boolean.class;
			instance = (AbstractEnvironmentSensor) classTemp.getDeclaredConstructor(cArg).newInstance(consolidatedTime, clearAllPreferences);
		} catch (InstantiationException e) {
			LOG.log(Level.WARNING, "InstantiationException", e);
		} catch (IllegalAccessException e) {
			LOG.log(Level.WARNING, "IllegalAccessException", e);
		} catch (IllegalArgumentException e) {
			LOG.log(Level.WARNING, "IllegalArgumentException", e);
		} catch (InvocationTargetException e) {
			LOG.log(Level.WARNING, "InvocationTargetException", e);
		} catch (NoSuchMethodException e) {
			LOG.log(Level.WARNING, "NoSuchMethodException", e);
		} catch (SecurityException e) {
			LOG.log(Level.WARNING, "SecurityException", e);
		} catch (ClassNotFoundException e) {
			LOG.log(Level.WARNING, "ClassNotFoundException", e);
		}
		return instance;
	}

	@Override
	public void close() {
		this.savePreferences();
		
		if (executor != null) {
			try {
				LOG.log(Level.INFO, "Initializing AbstractEnvironemntSensor.AnalyzeTemperatureTrends service termination....");
				executor.shutdown();
				executor.awaitTermination(20, TimeUnit.SECONDS);
				LOG.log(Level.INFO, "AbstractEnvironemntSensor.AnalyzeTemperatureTrends service has gracefully terminated");
			} catch (InterruptedException e) {
				LOG.log(Level.SEVERE, "AbstractEnvironemntSensor.AnalyzeTemperatureTrends service has timed out after 20 seconds of waiting to terminate processes.");
				Thread.currentThread().interrupt();
			}
		}
		
		for (PropertyChangeListener pcl : pcs.getPropertyChangeListeners()) {
			pcs.removePropertyChangeListener(pcl);
		}
	}
	
	public String getDeviceId() {
		return deviceId;
	}

	// Recursive function to find the minimum element in an array
	public static synchronized int findMinimumElement(List<MeasurementDataGroup> nums, int left, int right) {

		// find the middle element. To avoid overflow, use mid = low + (high - low) / 2
		final int mid = left + (left + right) / 2;

		// check if the middle element is less than its neighbors
		if ((mid == 0 || nums.get(mid - 1).getMagnitude() >= nums.get(mid).getMagnitude()) &&
				(mid == nums.size() - 1 || nums.get(mid + 1).getMagnitude() >= nums.get(mid).getMagnitude())) {
			return mid;
		}
		
		// If the left neighbor of `mid` is less than the middle element,
		// find the minimum recursively in the left subarray
		if (mid - 1 >= 0 && nums.get(mid - 1).getMagnitude() < nums.get(mid).getMagnitude()) {
			return findMinimumElement(nums, left, mid - 1);
		}
		
		// If the right neighbor of `mid` is less than the middle element,
		// find the minimum recursively in the right subarray
		return findMinimumElement(nums, mid + 1, right);
	}
	
	public static synchronized int findMinimumElement(List<MeasurementDataGroup> nums) {
		double min = Double.MAX_VALUE;
		int y = 0;
		for (int i = 0; i < nums.size(); i++) {
			if (nums.get(i).getMagnitude() <= min) {
				min = nums.get(i).getMagnitude();
				y = i;
			}
		}
		return y;
	}
	
	// Recursive function to find the peak element in an array
	public static synchronized int findPeakElement(List<MeasurementDataGroup> nums, int left, int right) {
		// find the middle element. To avoid overflow, use mid = low + (high - low) / 2
		final int mid = left + (left + right) / 2;

		// check if the middle element is greater than its neighbors
		if ((mid == 0 || nums.get(mid - 1).getMagnitude() <= nums.get(mid).getMagnitude()) &&
				(mid == nums.size() - 1 || nums.get(mid + 1).getMagnitude() <= nums.get(mid).getMagnitude())) {
			return mid;
		}
		
		// If the left neighbor of `mid` is greater than the middle element,
		// find the peak recursively in the left subarray
		if (mid - 1 >= 0 && nums.get(mid - 1).getMagnitude() > nums.get(mid).getMagnitude()) {
			return findPeakElement(nums, left, mid - 1);
		}
		
		// If the right neighbor of `mid` is greater than the middle element,
		// find the peak recursively in the right subarray
		return findPeakElement(nums, mid + 1, right);
	}

	public static synchronized int findMaximumElement(List<MeasurementDataGroup> nums) {
		double max = Double.MIN_VALUE;
		int y = 0;
		for (int i = 0; i < nums.size(); i++) {
			if (nums.get(i).getMagnitude() >= max) {
				max = nums.get(i).getMagnitude();
				y = i;
			}
		}
		return y;
	}
	
	private class AnalyzeTemperatureTrends implements Runnable {
		private final FifoFixedSizeQueue<MeasurementDataGroup> tempDataQueue;
		
		private AnalyzeTemperatureTrends(FifoFixedSizeQueue<MeasurementDataGroup> tempDataQueue) {
			this.tempDataQueue = tempDataQueue;
		}
		
		@Override
		public void run() {
			if (!tempDataQueue.isEmpty()) {
				final List<MeasurementDataGroup> measurementList = new ArrayList<>(tempDataQueue.size());
				final Iterator<MeasurementDataGroup> iter = tempDataQueue.iterator();
				while (iter.hasNext()) {
					final MeasurementDataGroup mdg = iter.next();
					measurementList.add(mdg);
				}
				final int highIndex = findMaximumElement(measurementList);
				pcs.firePropertyChange(Events.THIS_24H_HIGH_TEMP.name(), highTemp, measurementList.get(highIndex));
				highTemp = measurementList.get(highIndex).getMagnitude();
				final int lowIndex = findMinimumElement(measurementList);
				pcs.firePropertyChange(Events.THIS_24H_LOW_TEMP.name(), lowTemp, measurementList.get(lowIndex));
				lowTemp = measurementList.get(lowIndex).getMagnitude();
				final long hours = getSampleDepthHours();
				if (hours <= 24) {
					pcs.firePropertyChange(Events.TEMP_SAMPLE_TIME_SPAN.name(), sampleDepthHours, hours);
					sampleDepthHours = hours;
				}
			}
		}
		
		private long getSampleDepthHours() {
			long hours = 0;
			if (!temperatureQueue.isEmpty() && temperatureQueue.size() > 1) {
				final ZonedDateTime a = temperatureQueue.peek(0).getZdt();
				final ZonedDateTime b = temperatureQueue.peek(temperatureQueue.size() - 1).getZdt();
				hours = Math.abs(ChronoUnit.HOURS.between(a, b));
			}
			return hours;
		}
	}
	
}
