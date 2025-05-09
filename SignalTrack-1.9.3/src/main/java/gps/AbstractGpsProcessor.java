package gps;

import java.awt.Color;

import java.awt.geom.Point2D;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.lang.reflect.InvocationTargetException;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import geometry.CoordinateUtils;

import io.github.coordinates2country.Coordinates2Country;

import meteorology.Meteorology;

import net.iakovlev.timeshape.TimeZoneEngine;

import osm.OSMReverseGeoCodeAPI;

import utility.Vincenty;

// The purpose of this class is to provide common NMEA decoding and event reporting to any class that abstracts an 
// NMEA capable GPS receiver. This is also a factory class that creates static instances of any implementing GPS receiver
// in its set of catalogs. 
//
// Each implemented GPS device will need to implement a means to communicate over an appropriate interface.
// It will also need to save it settings and implement a Configuration interface.
//
// The configuration interface will provide a standard means of providing a settings dialog to any implementing 
// application. This is accomplished by way of a JPanel that contains user selectable GUI settings. To implement a
// settings dialog, use the ConfigurationInterface method JPanel[] getSettingsPanels(); This will return an array
// of all the necessary JPanels that the implementing application can add to a GPS settings dialog containing a 
// JTabbedPane. The first panel in the JTabbedPane would be the selector for what GPS receiver to use, and any other
// application related settings, such as weather or not to start the GPS receiver on system startup.
// The subsequent panels would be added as necessary when the user selects a specific GPS device.  
//
// For example, a GPS receiver with a TTY interface will provide /dev/ interface selections and baud rate selections
// that will be available from the ConfigInterface. However, a GPST device will instead show IP address and port number.  
//
// The GPS device will implement this abstract class, and this abstract class will provide all necessary methods for
// any implementing applications.
//
// To implement a GPS receiver in an application, use the factory method GPS getGPSReceiverInstance(className) 
// and pass it the GPS receiver class name, such as "gps.GPSDClient" or "aprs.KenwoodTMD710A". 
// The implementing application will be responsible for implementing a GPS settings dialog that
// will list the all implemented devices. The implementing application will also need to save all the operational 
// settings, such as starting coordinates and last selected device.
// 
// Once the implementing application has instantiated a GPS receiver, is can register listeners and receive GPS updates.
//
// All the interfacing will we abstract to the implementing application, and the responsibility of the GPS device.

public abstract class AbstractGpsProcessor implements AutoCloseable {
	
	public static final boolean DEFAULT_DEBUG_MODE = false;
	public static final Point2D.Double DEFAULT_STARTUP_POSITION = new Point2D.Double(-83, 35);
	public static final NmeaVersion DEFAULT_NMEA_VERSION = NmeaVersion.V31;
	public static final GnssSystem DEFAULT_GNSS_SYSTEM = GnssSystem.GPS;
	public static final ZoneId DEFAULT_ZONE_ID = ZoneId.systemDefault();
	public static final Locale DEFAULT_LOCALE = Locale.ROOT;
	public static final String GNSS_SYSTEM = "GNSS_SYSTEM";
	public static final String NMEA_VERSION = "NMEA_VERSION";
	public static final String RDF_HEADING_TRUE = "RDF_HEADING_TRUE";
	public static final String RDF_HEADING_RELATIVE = "RDF_HEADING_RELATIVE";
	public static final String FIX_QUALITY = "FIX_QUALITY";
	public static final String SPEED_MADE_GOOD_METERS_PER_SECOND = "SPEED_MADE_GOOD_METERS_PER_SECOND";
	public static final String SPEED_OVER_GROUND_MADE_GOOD_METERS_PER_SECOND = "SPEED_OVER_GROUND_MADE_GOOD_METERS_PER_SECOND";
	public static final String FIX_TYPE = "FIX_TYPE";
	public static final String FIX_MODE = "FIX_MODE";
	public static final String VALID_FIX = "VALID_FIX";
	public static final String VALID_TIME = "VALID_TIME";
	public static final String VALID_POSITION = "VALID_POSITION";
	public static final String TIME_ZONE_ID = "TIME_ZONE_ID";
	public static final String LOCALE = "LOCALE";
	public static final String VALID_ALTITUDE_METERS = "VALID_ALTITUDE_METERS";
	public static final String GPWPL_WAYPOINT_REPORT = "GPWPL_WAYPOINT_REPORT";
	public static final String PKWDWPL_APRS_REPORT = "PKWDWPL_APRS_REPORT";
	public static final String COURSE_MADE_GOOD_TRUE = "COURSE_MADE_GOOD_TRUE";
	public static final String FAA_MODE = "FAA_MODE";
	public static final String CRC_ERROR = "CRC_ERROR";
	public static final String RX_DATA = "RX_DATA";
	public static final String GPSD_DATA = "GPSD_DATA";
	public static final String RADIUS_UPDATED = "RADIUS_UPDATED";
	public static final String GSV_LIST_READY = "GSV_LIST_READY";
	public static final String GSA_LIST_READY = "GSA_LIST_READY";
	public static final String CLASS_NAME_CHANGE = "CLASS_NAME_CHANGE";
	public static final String TX_DATA = "TX_DATA";
	public static final String CONFIG_ERROR = "CONFIG_ERROR";
	public static final String ERROR = "ERROR";
	public static final String INVALID_ADDRESS = "INVALID_ADDRESS";
	public static final String DSR = "DSR";
	public static final String CTS = "CTS";
	public static final String RLSD = "RLSD";
	public static final String ONLINE = "ONLINE";
	public static final String READY = "READY";

	private static final Preferences userPrefs = Preferences.userRoot().node(AbstractGpsProcessor.class.getName());
	private static final Logger LOG = Logger.getLogger(AbstractGpsProcessor.class.getName());

	private static Locale currentLocale = DEFAULT_LOCALE;
	
	private boolean reverseGeoCodeLookupPending = false;
	
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	
	private OSMReverseGeoCodeAPI reverseGeoCode;
	private PropertyChangeListener reverseGeoCodeListener;
	private String rStr = "";
	private NmeaVersion nmeaVersion;
	private GnssSystem gnssSystem;
	private Point2D.Double position;
	private Point2D.Double lastKnownPosition;
	private FixQuality fixQuality = FixQuality.INVALID;
	private FixStatus fixStatus = FixStatus.NO_SIGNAL;
	private FAAMode faaMode;
	private FixMode fixMode;
	private FixType fixType = FixType.FIX_NOT_AVAILABLE;
	private boolean validTrueRdfHeading;
	private double courseMadeGoodTrue;
	private double speedMadeGoodMetersPerSecond;
	private double speedOverGroundMadeGoodMetersPerSecond;
	private double altitudeMeters;
	private double altitudeOverEllipsoid;
	private int satellitesInView;
	private int numberSatellitesUsedInNavigationSolution;
	private int qualityIndicator;
	private int pseudorangeResidue;
	private double magneticVariation;
	private double horizontalPositionErrorMeters;
	private double verticalPositionErrorMeters;
	private double sphericalEquivalentPositionErrorMeters;
	private double barometricAltitudeFeet;
	private double compassHeading;
	private double horizontalDilutionOfPrecision;
	private double verticalDilutionOfPrecision;
	private double positionDilutionOfPrecision;
	private double hypersphericalDilutionOfPrecision;
	private double latitudeDilutionOfPrecision;
	private double longitudeDilutionOfPrecision;
	private double timeDilutionOfPrecision;
	private long differentialFixAgeMilliseconds;
	private int differentialStationID;
	private double rdfHeadingRelative;
	private double rdfHeadingTrue;
	private RdfQuality rdfQuality;
	private ZoneId zoneId = DEFAULT_ZONE_ID;
	private String gpvtgMessageString;
	private String gprmcMessageString;
	private String gpggaMessageString;
	private String gpwplMessageString;
	private String gpgsaMessageString;
	private String gpgsvMessageString;
	private String pkwdwplMessageString;
	private final Calendar date = Calendar.getInstance();
	private ZonedDateTime utcZonedDateTime;
	private boolean enableEvents = true;
	private boolean reportCRCErrors;
	private boolean continuousUpdate;
	private double gpsSymbolRadius;
	private boolean startGPSWithSystem;
	private boolean enableGPSTracking;
	private boolean centerMapOnGPSPosition;
	private boolean run;
	
	private final String className;
	
	private ExecutorService executor;

	private final List<Satellite> svList = new ArrayList<>();
	
	protected AbstractGpsProcessor(String uniqueIdentifier, boolean clearAllPreferences) {
		this(uniqueIdentifier, clearAllPreferences, DEFAULT_STARTUP_POSITION);
	}

	protected AbstractGpsProcessor(String className, boolean clearAllPreferences, Point2D.Double lastKnownPosition) {
		this.className = className;
		this.lastKnownPosition = lastKnownPosition;
		
		clearAllPreferences(clearAllPreferences);
		loadPreferences(className);
		
		initializeComponents();
		initializeListeners();
		
		pcs.firePropertyChange(READY, null, startGPSWithSystem);
	}
	
	public abstract JPanel[] getConfigurationComponentArray();
	
	public abstract void saveClientSettings();

	public abstract String getDeviceManufacturer();

	public abstract String getDeviceModel();

	public abstract boolean isPortOpen();

	public abstract void write(Object object);

	public abstract long getSerialVersionUID();
	
	private void clearAllPreferences(boolean clearAllPreferences) {
		if (clearAllPreferences) {
			try {
				AbstractGpsProcessor.userPrefs.clear();
			} catch (final BackingStoreException ex) {
				LOG.log(Level.WARNING, ex.getMessage(), ex);
			}
		}
	}
	
	private void loadPreferences(String deviceID) {
		gpsSymbolRadius = userPrefs.getDouble(deviceID + "GPSSymbolRadius", 5D);
		startGPSWithSystem = userPrefs.getBoolean(deviceID + "StartGPSWithSystem", true);
		enableGPSTracking = userPrefs.getBoolean(deviceID + "EnableGPSTracking", true);
		centerMapOnGPSPosition = userPrefs.getBoolean(deviceID + "CenterMapOnGPSPosition", true);
		reportCRCErrors = userPrefs.getBoolean(deviceID + "ReportCRCErrors", false);
		lastKnownPosition = new Point2D.Double(
				userPrefs.getDouble(deviceID + "DefaultStartupLongitude", DEFAULT_STARTUP_POSITION.getX()),
				userPrefs.getDouble(deviceID + "DefaultStartupLatitude", DEFAULT_STARTUP_POSITION.getY()));
	}

	public void savePreferences() {
		savePreferences(className);
	}
	
	public void saveClassName(String className) {
		pcs.firePropertyChange(CLASS_NAME_CHANGE, null, className);
	}

	private void initializeListeners() {
		reverseGeoCodeListener = event -> {
			if (OSMReverseGeoCodeAPI.Event.LOCALE.name().equals(event.getPropertyName())) {
				pcs.firePropertyChange(LOCALE, currentLocale, event.getNewValue());
				setLocale((Locale) event.getNewValue());
				reverseGeoCodeLookupPending = false;
			}
		};
	}

	private void initializeComponents() {
		reverseGeoCode = new OSMReverseGeoCodeAPI();
	}
	
	private void savePreferences(String deviceID) {
		userPrefs.putDouble(deviceID + "GPSSymbolRadius", gpsSymbolRadius);
		userPrefs.putBoolean(deviceID + "StartGPSWithSystem", startGPSWithSystem);
		userPrefs.putBoolean(deviceID + "EnableGPSTracking", enableGPSTracking);
		userPrefs.putBoolean(deviceID + "CenterMapOnGPSPosition", centerMapOnGPSPosition);
		userPrefs.putBoolean(deviceID + "ReportCRCErrors", reportCRCErrors);
		userPrefs.putDouble(deviceID + "DefaultStartupLongitude", lastKnownPosition.getX());
		userPrefs.putDouble(deviceID + "DefaultStartupLatitude", lastKnownPosition.getY());
		saveClientSettings();
	}

	public static BidiMap<String, String> getCatalogMap() {
		final BidiMap<String, String> catalog = new DualHashBidiMap<>();
		catalog.put("gps.GenericNMEASerialGPSReceiver", "Standard TTY NMEA GPS Receiver");
		catalog.put("gps.GPSdClient", "GPSd Telnet Client");
		catalog.put("gps.OStarzBTQ920Receiver", "OStarz Bluetooth NMEA Receiver");
		catalog.put("gps.PanasonicCF31", "Panasonic CF-31 Toughbook Integrated GPS Receiver");
		return catalog;
	}
	
	public PropertyChangeSupport getGPSPropertyChangeSupport() {
		return pcs;
	}

	public void setGpsSymbolRadius(int gpsSymbolRadius) {
		pcs.firePropertyChange(RADIUS_UPDATED, this.gpsSymbolRadius, gpsSymbolRadius);
		this.gpsSymbolRadius = gpsSymbolRadius;
	}

	public NmeaVersion getNmeaVersion() {
		return nmeaVersion;
	}

	public void setNmeaVersion(NmeaVersion nmeaVersion) {
		if (isEnableEvents()) {
			pcs.firePropertyChange(NMEA_VERSION, this.nmeaVersion, nmeaVersion);
		}
		this.nmeaVersion = nmeaVersion;
	}

	public GnssSystem getGnssSystem() {
		return gnssSystem;
	}

	public void setGnssSystem(GnssSystem gnssSystem) {
		if (isEnableEvents()) {
			pcs.firePropertyChange(GNSS_SYSTEM, this.gnssSystem, gnssSystem);
		}
		this.gnssSystem = gnssSystem;
	}

	public boolean isStartGPSWithSystem() {
		return startGPSWithSystem;
	}

	public void setStartGPSWithSystem(boolean startGPSWithSystem) {
		this.startGPSWithSystem = startGPSWithSystem;
	}

	public boolean isEnableGPSTracking() {
		return enableGPSTracking;
	}

	public void setEnableGPSTracking(boolean enableGPSTracking) {
		this.enableGPSTracking = enableGPSTracking;
	}

	public boolean isCenterMapOnGPSPosition() {
		return centerMapOnGPSPosition;
	}

	public void setCenterMapOnGPSPosition(boolean centerMapOnGPSPosition) {
		this.centerMapOnGPSPosition = centerMapOnGPSPosition;
	}

	public boolean isContinuousUpdate() {
		return continuousUpdate;
	}

	public void setContinuousUpdate(boolean continuousUpdate) {
		this.continuousUpdate = continuousUpdate;
	}

	public double getGpsSymbolRadius() {
		return gpsSymbolRadius;
	}

	public void setGpsSymbolRadius(double gpsSymbolRadius) {
		this.gpsSymbolRadius = gpsSymbolRadius;
	}

	public void setValidTrueRdfHeading(boolean validTrueRdfHeading) {
		this.validTrueRdfHeading = validTrueRdfHeading;
	}

	public void setRdfHeadingRelative(double rdfHeadingRelative) {
		this.rdfHeadingRelative = rdfHeadingRelative;
	}

	public void setRdfHeadingTrue(double rdfHeadingTrue) {
		this.rdfHeadingTrue = rdfHeadingTrue;
	}

	public void setRdfQuality(RdfQuality rdfQuality) {
		this.rdfQuality = rdfQuality;
	}

	public void setAltitudeMeters(double altitudeMeters) {
		if (isEnableEvents()) {
			pcs.firePropertyChange(VALID_ALTITUDE_METERS, this.altitudeMeters, altitudeMeters);
		}
		this.altitudeMeters = altitudeMeters;
	}

	public FixStatus getFixStatus() {
		return fixStatus;
	}

	public void setFixStatus(FixStatus fixStatus) {
		this.fixStatus = fixStatus;
	}

	public void setFixQuality(FixQuality fixQuality) {
		if (isEnableEvents()) {
			pcs.firePropertyChange(FIX_QUALITY, this.fixQuality, fixQuality);
		}
		this.fixQuality = fixQuality;
	}

	public void setFaaMode(FAAMode faaMode) {
		if (isEnableEvents()) {
			pcs.firePropertyChange(FAA_MODE, this.faaMode, faaMode);
		}
		this.faaMode = faaMode;
	}

	public void setFixMode(FixMode fixMode) {
		if (isEnableEvents()) {
			pcs.firePropertyChange(FIX_MODE, this.fixMode, fixMode);
		}
		this.fixMode = fixMode;
	}

	public void setFixType(FixType fixType) {
		if (isEnableEvents()) {
			pcs.firePropertyChange(FIX_TYPE, this.fixType, fixType);
		}
		this.fixType = fixType;
	}

	public double getSpeedMadeGoodMetersPerSecond() {
		return speedMadeGoodMetersPerSecond;
	}

	public void setSpeedMadeGoodMetersPerSecond(double speedMadeGoodMetersPerSecond) {
		if (isEnableEvents()) {
			pcs.firePropertyChange(SPEED_MADE_GOOD_METERS_PER_SECOND, this.speedMadeGoodMetersPerSecond,
					speedMadeGoodMetersPerSecond);
		}
		this.speedMadeGoodMetersPerSecond = speedMadeGoodMetersPerSecond;
	}

	public void setAltitudeOverEllipsoid(double altitudeOverEllipsoid) {
		this.altitudeOverEllipsoid = altitudeOverEllipsoid;
	}

	public void setSatellitesInView(int satellitesInView) {
		this.satellitesInView = satellitesInView;
	}

	public void setMagneticVariation(double magneticVariation) {
		this.magneticVariation = magneticVariation;
	}

	public void setHorizontalPositionErrorMeters(double horizontalPositionErrorMeters) {
		this.horizontalPositionErrorMeters = horizontalPositionErrorMeters;
	}

	public void setVerticalPositionErrorMeters(double verticalPositionErrorMeters) {
		this.verticalPositionErrorMeters = verticalPositionErrorMeters;
	}

	public void setSphericalEquivalentPositionErrorMeters(double sphericalEquivalentPositionErrorMeters) {
		this.sphericalEquivalentPositionErrorMeters = sphericalEquivalentPositionErrorMeters;
	}

	public int getNumberSatellitesUsedInNavigationSolution() {
		return numberSatellitesUsedInNavigationSolution;
	}

	public void setNumberSatellitesUsedInNavigationSolution(int numberSatellitesUsedInNavigationSolution) {
		this.numberSatellitesUsedInNavigationSolution = numberSatellitesUsedInNavigationSolution;
	}

	public int getQualityIndicator() {
		return qualityIndicator;
	}

	public void setQualityIndicator(int qualityIndicator) {
		this.qualityIndicator = qualityIndicator;
	}

	public int getPseudorangeResidue() {
		return pseudorangeResidue;
	}

	public void setPseudorangeResidue(int pseudorangeResidue) {
		this.pseudorangeResidue = pseudorangeResidue;
	}

	public ZonedDateTime getUtcZonedDateTime() {
		return utcZonedDateTime;
	}

	public double getHypersphericalDilutionOfPrecision() {
		return hypersphericalDilutionOfPrecision;
	}

	public void setHypersphericalDilutionOfPrecision(double hypersphericalDilutionOfPrecision) {
		this.hypersphericalDilutionOfPrecision = hypersphericalDilutionOfPrecision;
	}

	public double getLatitudeDilutionOfPrecision() {
		return latitudeDilutionOfPrecision;
	}

	public void setLatitudeDilutionOfPrecision(double latitudeDilutionOfPrecision) {
		this.latitudeDilutionOfPrecision = latitudeDilutionOfPrecision;
	}

	public double getLongitudeDilutionOfPrecision() {
		return longitudeDilutionOfPrecision;
	}

	public void setLongitudeDilutionOfPrecision(double longitudeDilutionOfPrecision) {
		this.longitudeDilutionOfPrecision = longitudeDilutionOfPrecision;
	}

	public double getTimeDilutionOfPrecision() {
		return timeDilutionOfPrecision;
	}

	public void setTimeDilutionOfPrecision(double timeDilutionOfPrecision) {
		this.timeDilutionOfPrecision = timeDilutionOfPrecision;
	}

	public void setHorizontalDilutionOfPrecision(double horizontalDilutionOfPrecision) {
		this.horizontalDilutionOfPrecision = horizontalDilutionOfPrecision;
	}

	public void setVerticalDilutionOfPrecision(double verticalDilutionOfPrecision) {
		this.verticalDilutionOfPrecision = verticalDilutionOfPrecision;
	}

	public void setBarometricAltitudeFeet(double barometricAltitudeFeet) {
		this.barometricAltitudeFeet = barometricAltitudeFeet;
	}

	public void setCompassHeading(double compassHeading) {
		this.compassHeading = compassHeading;
	}

	public void setDifferentialFixAgeMilliseconds(long differentialFixAgeMilliseconds) {
		this.differentialFixAgeMilliseconds = differentialFixAgeMilliseconds;
	}

	public void setDifferentialStationID(int differentialStationID) {
		this.differentialStationID = differentialStationID;
	}

	public List<Satellite> getSatelitesInView() {
		return Collections.unmodifiableList(svList);
	}

	public boolean isEnableEvents() {
		return enableEvents;
	}

	public void setEnableEvents(boolean enableEvents) {
		this.enableEvents = enableEvents;
	}

	public FixQuality getFixQuality() {
		return fixQuality;
	}

	public double getVerticalDilutionOfPrecision() {
		return verticalDilutionOfPrecision;
	}

	public long getDifferentialFixAgeMilliseconds() {
		return differentialFixAgeMilliseconds;
	}

	public int getDifferentialStationID() {
		return differentialStationID;
	}

	public FAAMode getFaaMode() {
		return faaMode;
	}

	public FixMode getFixMode() {
		return fixMode;
	}

	public FixType getFixType() {
		return fixType;
	}

	public boolean isValidTrueRdfHeading() {
		return validTrueRdfHeading;
	}

	public Point2D.Double getPosition() {
		return position;
	}

	private void setPosition(Point2D.Double position) {
		this.position = position;
		lastKnownPosition = position;
		if (currentLocale == Locale.ROOT && !reverseGeoCodeLookupPending) {
			reverseGeoCodeLookupPending = true;
			reverseGeoCode.requestReverseGeoCodeEventService(position.getX(), position.getY());
		}
		processPosition(position);
	}

	public void setPositionDilutionOfPrecision(double positionDilutionOfPrecision) {
		this.positionDilutionOfPrecision = positionDilutionOfPrecision;
	}

	public double getSpeedOverGroundMadeGoodMetersPerSecond() {
		return speedOverGroundMadeGoodMetersPerSecond;
	}

	public void setSpeedOverGroundMadeGoodMetersPerSecond(double speedOverGroundMadeGoodMetersPerSecond) {
		this.speedOverGroundMadeGoodMetersPerSecond = speedOverGroundMadeGoodMetersPerSecond;
	}

	public void setCourseMadeGoodTrue(double courseMadeGoodTrue) {
		this.courseMadeGoodTrue = courseMadeGoodTrue;
	}

	public double getCourseMadeGoodTrue() {
		return courseMadeGoodTrue;
	}

	public double getCourseMadeGoodMagnetic() {
		return courseMadeGoodTrue + magneticVariation;
	}

	public double getSpeedMadeGoodMPH() {
		return speedMadeGoodMetersPerSecond * 2.2369362920544;
	}

	public double getSpeedMadeGoodKnots() {
		return speedMadeGoodMetersPerSecond * 1.9438444924574;
	}

	public double getSpeedMadeGoodKPH() {
		return speedMadeGoodMetersPerSecond * 3.6;
	}

	public double getAltitudeMeters() {
		return altitudeMeters;
	}

	public double getAltitudeFeet() {
		return Vincenty.metersToFeet(altitudeMeters);
	}

	public double getPositionDilutionOfPrecision() {
		return positionDilutionOfPrecision;
	}

	public double getAltitudeOverEllipsoid() {
		return altitudeOverEllipsoid;
	}

	public int getSatellitesInView() {
		return satellitesInView;
	}

	public double getMagneticVariation() {
		return magneticVariation;
	}

	public double getHorizontalPositionErrorMeters() {
		return horizontalPositionErrorMeters;
	}

	public double getVerticalPositionErrorMeters() {
		return verticalPositionErrorMeters;
	}

	public double getSphericalEquivalentPositionErrorMeters() {
		return sphericalEquivalentPositionErrorMeters;
	}

	public double getBarometricAltitudeFeet() {
		return barometricAltitudeFeet;
	}

	public double getCompassHeading() {
		return compassHeading;
	}

	public double getHorizontalDilutionOfPrecision() {
		return horizontalDilutionOfPrecision;
	}

	public double getRdfHeadingRelative() {
		return rdfHeadingRelative;
	}

	public double getRdfHeadingTrue() {
		return rdfHeadingTrue;
	}

	public RdfQuality getRdfQuality() {
		return rdfQuality;
	}
	
	public ZoneId getZoneId() {
		return zoneId;
	}

	public static Locale getLocale() {
		return currentLocale;
	}
	
	public static String getLocale(double longitude, double latitude) {
		return Coordinates2Country.country(longitude, latitude);
	}
	
	public String getPkwdwplMessageString() {
		return pkwdwplMessageString;
	}

	public String getGpvtgMessageString() {
		return gpvtgMessageString;
	}

	public String getGpgsaMessageString() {
		return gpgsaMessageString;
	}

	public String getGpgsvMessageString() {
		return gpgsvMessageString;
	}

	public String getGprmcMessageString() {
		return gprmcMessageString;
	}

	public String getGpggaMessageString() {
		return gpggaMessageString;
	}

	public String getGpwplMessageString() {
		return gpwplMessageString;
	}

	public long getMillisFromEpoch() {
		return utcZonedDateTime.toInstant().toEpochMilli();
	}

	public void setLocalDateTime(LocalDateTime localDateTime) {
		utcZonedDateTime = localDateTime.atZone(ZoneOffset.UTC);
	}

	private void setISO8601UTCTime(String iso8601UTCTime) {
		final Instant instant = Instant.parse(iso8601UTCTime);
		final ZoneId utc = ZoneId.of("UTC");
		utcZonedDateTime = instant.atZone(utc);
		if (isEnableEvents()) {
			pcs.firePropertyChange(VALID_TIME, null, instant.toEpochMilli());
		}
	}

	public List<Satellite> getSvList() {
		return new ArrayList<>(svList);
	}

	public void addSatellite(gps.Satellite sat) {
		svList.add(sat);
	}

	public void addSatellite(com.ivkos.gpsd4j.messages.Satellite sat) {
		svList.add(new gps.Satellite(sat));
	}

	public Point2D.Double getLastKnownPosition() {
		return lastKnownPosition;
	}

	public void setLastKnownPosition(Point2D.Double lastKnownPosition) {
		this.lastKnownPosition = lastKnownPosition;
	}

	public void setZoneId(ZoneId zoneId) {
		this.zoneId = zoneId;
	}
	
	public static void setLocale(Locale locale) {
		AbstractGpsProcessor.currentLocale = locale;
	}
	
	public void setGpvtgMessageString(String gpvtgMessageString) {
		this.gpvtgMessageString = gpvtgMessageString;
	}

	public void setGprmcMessageString(String gprmcMessageString) {
		this.gprmcMessageString = gprmcMessageString;
	}

	public void setGpggaMessageString(String gpggaMessageString) {
		this.gpggaMessageString = gpggaMessageString;
	}

	public void setGpwplMessageString(String gpwplMessageString) {
		this.gpwplMessageString = gpwplMessageString;
	}

	public void setGpgsaMessageString(String gpgsaMessageString) {
		this.gpgsaMessageString = gpgsaMessageString;
	}

	public void setGpgsvMessageString(String gpgsvMessageString) {
		this.gpgsvMessageString = gpgsvMessageString;
	}

	public void setPkwdwplMessageString(String pkwdwplMessageString) {
		this.pkwdwplMessageString = pkwdwplMessageString;
	}

	public void setReportCRCErrors(boolean reportCRCErrors) {
		this.reportCRCErrors = reportCRCErrors;
	}

	public boolean isReportCRCErrors() {
		return reportCRCErrors;
	}

	public String getClassName() {
		return this.getClass().getName();
	}

	protected void processNMEAData(String instr) {
		int iStart;
		int iEnd;
		int iTemp;
		rStr += instr;
		while (!rStr.isEmpty() && run) {
			iStart = rStr.indexOf('$', 0);
			if (iStart >= 0) {
				iTemp = rStr.indexOf('*', iStart + 1);
				iEnd = iTemp > 0 ? iTemp + 3 : 0;
			} else {
				iEnd = 0;
			}
			if ((iStart >= 0) && (rStr.length() >= iEnd) && (iEnd > 0)) {
				executor.submit(new NmeaDecoder(rStr.substring(iStart, iEnd)));
				rStr = rStr.length() > (iEnd + 1) ? rStr.substring(iEnd + 1) : "";
			} else {
				break;
			}
		}
	}

	private class NmeaDecoder implements Runnable {

		private final String message;

		public NmeaDecoder(String message) {
			this.message = message;
		}

		@Override
		public void run() {
			if (isEnableEvents()) {
				pcs.firePropertyChange(AbstractGpsProcessor.RX_DATA, null, message);
			}
			
			validTrueRdfHeading = false;

			if ("%".equals(message.substring(0, 1))) {
				if (message.trim().length() == 6) {
					rdfHeadingRelative = Integer.parseInt(message.substring(1, 4));
					rdfQuality = getRdfQuality(Integer.parseInt(message.substring(5, 6)));
					if (getSpeedMadeGoodMPH() >= 1) {
						rdfHeadingTrue = courseMadeGoodTrue + rdfHeadingRelative;
						if (isEnableEvents()) {
							pcs.firePropertyChange(RDF_HEADING_TRUE, null, rdfHeadingTrue);
						}
						validTrueRdfHeading = true;
					} else {
						if (isEnableEvents()) {
							pcs.firePropertyChange(RDF_HEADING_RELATIVE, null, rdfHeadingRelative);
						}
						validTrueRdfHeading = false;
					}
				}
			} else if ("$".equals(message.substring(0, 1))) {
				if (!checksum(message)) {
					setFixQuality(FixQuality.INVALID);
					if (isEnableEvents()) {
						pcs.firePropertyChange(CRC_ERROR, null, message);
					}
				} else {
					final String completeMsg = message;
					final String[] a = message.substring(0, message.indexOf('*')).split(",");

					switch (a[0]) {
					case "$GPRMC" -> {
						// $GPRMC,123519,A,4807.038,N,01131.000,E,022.4,084.4,230394,003.1,W*6A
						gprmcMessageString = completeMsg;
						try {
							date.set(Calendar.HOUR_OF_DAY, Integer.parseInt(a[1].substring(0, 2)));
							date.set(Calendar.MINUTE, Integer.parseInt(a[1].substring(2, 4)));
							date.set(Calendar.SECOND, Integer.parseInt(a[1].substring(4, 6)));
							date.set(Calendar.DATE, Integer.parseInt(a[9].substring(0, 2)));
							date.set(Calendar.MONTH, Integer.parseInt(a[9].substring(2, 4)) - 1);
							date.set(Calendar.YEAR, Integer.parseInt("20" + a[9].substring(4, 6)));
							date.set(Calendar.MILLISECOND, Integer.parseInt(a[1].substring(7, 10)));

							setMillisFromEpoch(date.getTimeInMillis());

							if (!a[2].isEmpty()) {
								if ("A".equals(a[2])) { // Status A=active or V=void
									setFixQuality(FixQuality.GPS);
								} else {
									setFixQuality(FixQuality.INVALID);
								}
							}

							if (!a[3].isEmpty() || !a[5].isEmpty()) {
								double lat;
								double lon;

								lat = (Double.parseDouble(a[3].substring(0, 2)) * 1000000)
										+ (Double.parseDouble(a[3].substring(2)) * 16666.6666667);

								if ("S".equals(a[4])) {
									lat = -lat;
								}

								lon = (Double.parseDouble(a[5].substring(0, 3)) * 1000000)
										+ (Double.parseDouble(a[5].substring(3)) * 16666.6666667);

								if ("W".equals(a[6])) {
									lon = -lon;
								}

								if (fixQuality != FixQuality.INVALID) {
									setPosition(new Point2D.Double(lon / 1000000, lat / 1000000));
								}
							}

							if (!a[7].isEmpty()) {
								setSpeedOverGroundMadeGoodMetersPerSecond(
										Meteorology.convertKnotsToMetersPerSecond(Double.parseDouble(a[7]))); 
							}

							if (!a[10].isEmpty() && !a[11].isEmpty()) {
								setMagneticVariation(
										("E".equals(a[11])) ? Double.parseDouble(a[10]) : -Double.parseDouble(a[10]));
							}

							if (getSpeedMadeGoodMPH() > 1.0 && !a[8].isEmpty()) {
								setCourseMadeGoodTrue(Double.parseDouble(a[8])); // Track angle in degrees (True)
							}

							if (a.length > 12 && !a[12].isEmpty()) {
								setFixQuality(getRmcFixQuality(a[12]));
							} else {
								setFixQuality(FixQuality.INVALID);
							}

						} catch (StringIndexOutOfBoundsException | ArrayIndexOutOfBoundsException
								| NumberFormatException ex) {
							LOG.log(Level.WARNING, ex.getMessage(), ex);
						}
					}
					case "$GPGGA" -> {
						setGpggaMessageString(completeMsg);
						try {
							if (!a[2].isEmpty() || !a[4].isEmpty()) {
								double lat;
								double lon;

								lat = (Double.parseDouble(a[2].substring(0, 2)) * 1000000.0)
										+ (Double.parseDouble(a[2].substring(2)) * 16666.6666667);

								if ("S".equals(a[3])) {
									lat = -lat;
								}

								lon = (Double.parseDouble(a[4].substring(0, 3)) * 1000000.0)
										+ (Double.parseDouble(a[4].substring(3)) * 16666.6666667);

								if ("W".equals(a[5])) {
									lon = -lon;
								}

								if (fixQuality != FixQuality.INVALID) {
									setPosition(new Point2D.Double(lon / 1000000.0, lat / 1000000.0));
								}
							}

							if (!a[6].isEmpty()) {
								setFixQuality(getGgaFixQuality(Integer.parseInt(a[6])));
							}

							if (!a[9].isEmpty()) {
								setAltitudeMeters(Double.parseDouble(a[9]));
							}

							if (!a[11].isEmpty()) {
								setAltitudeOverEllipsoid(Double.parseDouble(a[11]));
							}

							if (!a[8].isEmpty()) {
								setHorizontalDilutionOfPrecision(Double.parseDouble(a[8]));
							}

							if (!a[7].isEmpty()) {
								setSatellitesInView(Integer.parseInt(a[7]));
							}

							if (!a[13].isEmpty() && !a[14].isEmpty()) {
								setDifferentialFixAgeMilliseconds((long) (Double.parseDouble(a[13]) * 1E3));
								setDifferentialStationID(Integer.parseInt(a[14]));
							}

						} catch (StringIndexOutOfBoundsException | ArrayIndexOutOfBoundsException
								| NumberFormatException ex) {
							LOG.log(Level.WARNING, ex.getMessage(), ex);
						}
					}
					case "$GPGSV" -> {
						setGpgsvMessageString(completeMsg);
						try {
							if ("1".equals(a[2])) {
								if (!a[3].isEmpty()) {
									setSatellitesInView(Integer.parseInt(a[3]));
								}
								getSvList().clear();
							}

							for (int i = 4; i <= a.length - 4; i += 4) {
								if (!a[i].isEmpty() || !a[i + 1].isEmpty() || !a[i + 2].isEmpty()
										|| !a[i + 3].isEmpty()) {
									getSvList().add(new Satellite(a[i], a[i + 1], a[i + 2], a[i + 3]));
								}
							}

							if (getSvList().size() == getSatellitesInView()) {
								pcs.firePropertyChange(GSV_LIST_READY, null, getSvList());
							}

						} catch (StringIndexOutOfBoundsException | ArrayIndexOutOfBoundsException
								| NumberFormatException ex) {
							LOG.log(Level.WARNING, ex.getMessage(), ex);
						}
					}
					case "$GPGSA" -> {
						setGpgsaMessageString(completeMsg);

						try {
							if (!a[1].isEmpty()) {
								setFixMode(("M".equals(a[1])) ? FixMode.MANUAL : FixMode.AUTOMATIC);
							}
							if (!a[2].isEmpty()) {
								setFixType(FixType.values()[Integer.parseInt(a[2])]);
							}

							final List<Satellite> svListTemp = new ArrayList<>();

							getSvList().forEach(s -> {
								try {
									svListTemp.add((Satellite) s.clone());
								} catch (CloneNotSupportedException e) {
									e.printStackTrace();
								}
							});

							getSvList().clear();

							for (int i = 3; i <= 14; i++) {
								if (!a[i].isEmpty()) {
									getSvList().add(new Satellite(a[i], "", "", ""));
								}
							}

							if (isEnableEvents()) {
								pcs.firePropertyChange(GSA_LIST_READY, svListTemp, getSvList());
							}

							if (!a[15].isEmpty()) {
								setPositionDilutionOfPrecision(Double.parseDouble(a[15]));
							}

							if (!a[16].isEmpty()) {
								setHorizontalDilutionOfPrecision(Double.parseDouble(a[16]));
							}

							if (!a[17].isEmpty()) {
								setVerticalDilutionOfPrecision(Double.parseDouble(a[17]));
							}

							if (a.length == 19) {
								setNmeaVersion(NmeaVersion.V41);
								if (!a[18].isEmpty()) {
									setGnssSystem(getGnssSystem(a[18]));
								}
							} else {
								setNmeaVersion(NmeaVersion.V31);
								setGnssSystem(GnssSystem.GPS);
							}

						} catch (StringIndexOutOfBoundsException | ArrayIndexOutOfBoundsException  
								| NumberFormatException ex) {
							LOG.log(Level.WARNING, ex.getMessage(), ex);
						}
					}
					
					case "$GPVTG" -> {
						// $GPVTG,140.88,T,,M,8.04,N,14.89,K,D*05
						setGpvtgMessageString(completeMsg);
						try {
							if (!a[1].isEmpty()) {
								setCourseMadeGoodTrue(Double.parseDouble(a[1]));
							}
							if (!a[3].isEmpty()) {
								setMagneticVariation(Double.parseDouble(a[3]) - Double.parseDouble(a[1]));
							}
							if (!a[5].isEmpty()) {
								setSpeedMadeGoodMetersPerSecond(
										Meteorology.convertKnotsToMetersPerSecond(Double.parseDouble(a[5])));
							}

							if (!a[7].isEmpty()) {
								setSpeedOverGroundMadeGoodMetersPerSecond(Meteorology
										.convertKilometersPerHourToMetersPerSecond(Double.parseDouble(a[7])));
							}

						} catch (StringIndexOutOfBoundsException | ArrayIndexOutOfBoundsException
								| NumberFormatException ex) {
							LOG.log(Level.WARNING, ex.getMessage(), ex);
						}
					}
					
					default -> LOG.log(Level.WARNING, "Unsupported NMEA sentence: {0}", completeMsg);
					}
				}
			}
		}
		
		private void setMillisFromEpoch(long millisFromEpoch) {
			final Instant instant = Instant.ofEpochMilli(millisFromEpoch);
			final ZoneId utc = ZoneId.of("UTC");
			utcZonedDateTime = instant.atZone(utc);
			date.setTimeInMillis(millisFromEpoch);
			if (isEnableEvents()) {
				pcs.firePropertyChange(VALID_TIME, null, millisFromEpoch);
			}
		}
	}
	
	private void processPosition(Point2D.Double position) {
		if (!continuousUpdate && position != null && this.position != null
				&& (Math.abs(this.position.distance(position)) > 0.00003) && isEnableEvents()) {
			pcs.firePropertyChange(VALID_POSITION, this.position, position);
			zoneId = getZoneId(position);
			pcs.firePropertyChange(TIME_ZONE_ID, this.zoneId, zoneId);
		}
		if (position != null && (continuousUpdate || this.position == null) && isEnableEvents()) {
			pcs.firePropertyChange(VALID_POSITION, null, position);
			zoneId = getZoneId(position);
			pcs.firePropertyChange(TIME_ZONE_ID, null, zoneId);
		}
		if (position != null) {
			this.position = (Point2D.Double) position.clone();
		}
	}

	public static ZoneId getZoneId(Point2D position) {
		final TimeZoneEngine timeZoneEngine = TimeZoneEngine.initialize();
		final Optional<ZoneId> zid = timeZoneEngine.query(position.getY(), position.getX());
        ZoneId z = DEFAULT_ZONE_ID;
        if (zid.isPresent()) {
            z = zid.get();
        }
        return z;
    }
	
	public String getPKWDWPLTestString(Point2D point) {
		// $PKWDWPL,053046,V,3953.79,N,08242.93,W,1,142,280320,000225,KM4TT-12,/j*6D
		final OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
		final String d = now.format(DateTimeFormatter.ofPattern("ddMMyy"));
		final String utc = now.format(DateTimeFormatter.ofPattern("HHmmss"));

		final String lon = toNmeaFormattedLongitude(point.getX());

		final String lat = toNmeaFormattedLatitude(point.getY());

		final String s = "$PKWDWPL," + utc + ",V," + lat + "," + lon + ","
				+ "%03d".formatted((int) getSpeedMadeGoodKnots()) + ","
				+ "%03d".formatted((int) getCourseMadeGoodTrue()) + "," + d + ","
				+ "%06d".formatted((int) getAltitudeMeters()) + ",N1CTF-3,/j*";
		final String c = Integer.toHexString(calculateNmeaCheckSum(s));

		return s + c;
	}
	
	public static String toNmeaFormattedLongitude(double degrees) {
		final DecimalFormat formatter = (DecimalFormat) NumberFormat.getNumberInstance();

		formatter.applyPattern("000");
		final String dd = formatter.format(Math.abs((int) degrees));

		formatter.applyPattern("00.0000");
		final String mm = formatter.format(Math.abs(degrees - (int) degrees) * 60);

		final String ew = (degrees >= 0) ? "E" : "W";

		return dd + mm + "," + ew;
	}

	public static String toNmeaFormattedLatitude(double degrees) {
		final DecimalFormat formatter = (DecimalFormat) NumberFormat.getNumberInstance();

		formatter.applyPattern("00");
		final String dd = formatter.format(Math.abs((int) degrees));

		formatter.applyPattern("00.0000");
		final String mm = formatter.format(Math.abs(degrees - (int) degrees) * 60);

		final String ns = (degrees >= 0) ? "N" : "S";

		return dd + mm + "," + ns;
	}

	public static int calculateNmeaCheckSum(String nmeaString) {
		final String chkDat = nmeaString.substring(1, nmeaString.indexOf('*'));

		int s = chkDat.charAt(0);

		for (int i = 1; i < chkDat.length(); i++) {
			s ^= chkDat.charAt(i);
		}

		return s;
	}

	public static synchronized boolean checksum(String input) {
		final String chkDat;
		final String[] dat;
		
		String chkSum;

		if (input.indexOf('*', 2) < 2) {
			return false;
		} else {
			try {
				// Take everything, starting at the second character, all the way up to, but not
				// including the asterisk.
				// This is what will be calculated against the checksum.
				chkDat = input.substring(1, input.indexOf('*'));

				// Convert the entire input string into a string array, demarcated by commas.
				dat = input.split(",");

				// Get the last element of the array, including the leading asterisk.
				chkSum = dat[dat.length - 1];

				// Take everything after, but not including the asterisk as the checksum.
				// The checksum is a two digit hex number.
				chkSum = chkSum.substring(chkSum.indexOf('*') + 1);

				// Start calculating the checksum with the first character of the input string.
				int s = chkDat.charAt(0);

				// Iterate though the substring of the input string that is to be checked and
				// add the sum to the result.
				for (int i = 1; i < chkDat.length(); i++) {
					s ^= chkDat.charAt(i);
				}

				// Compare the data to the accompanying checksum. If they match, the integrity
				// of the transmission is.
				// guaranteed, and a true result is returned. If not a match, then fire an event
				// and return false.
				return (s == Integer.valueOf(chkSum, 16));

			} catch (final NumberFormatException ex) {
				return false;
			}
		}
	}

	public String getUTMCoordinates() {
		return CoordinateUtils.lonLatToUTM(getPosition()).toString().toUpperCase(getLocale());
	}

	public String getMGRSLocation() {
		return CoordinateUtils.lonLatToMGRS(getPosition(), 5).toString().toUpperCase(getLocale());
	}

	public static FixQuality getRmcFixQuality(String s) {
		return switch (s) {
			case "A" -> FixQuality.GPS;
			case "D" -> FixQuality.DGPS;
			case "E" -> FixQuality.DEAD_RECON;
			default -> FixQuality.INVALID;
		};
	}

	public static FixStatus getGpsdFixStatus(int i) {
		return switch (i) {
			case 0 -> FixStatus.NO_SIGNAL;
			case 1 -> FixStatus.SEARCHING;
			case 2 -> FixStatus.ACQUIRED;
			case 3 -> FixStatus.DETECTED_UNUSABLE;
			case 4 -> FixStatus.LOCK_SYNC;
			case 5 -> FixStatus.CARRIER_LOCK_SYNC;
			case 6 -> FixStatus.CARRIER_LOCK_SYNC;
			case 7 -> FixStatus.CARRIER_LOCK_SYNC;
			default -> FixStatus.NO_SIGNAL;
		};
	}

	public static FixQuality getGgaFixQuality(int i) {
		return switch (i) {
			case 0 -> FixQuality.INVALID;
			case 1 -> FixQuality.GPS;
			case 2 -> FixQuality.DGPS;
			case 3 -> FixQuality.PPS;
			case 4 -> FixQuality.RTK;
			case 5 -> FixQuality.FLOAT_RTK;
			case 6 -> FixQuality.DEAD_RECON;
			default -> FixQuality.INVALID;
		};
	}

	public static GnssSystem getGnssSystem(String id) {
		return switch (id) {
		case "1" -> GnssSystem.GPS;
		case "2" -> GnssSystem.GLONASS;
		case "3" -> GnssSystem.GALILEO;
		case "4" -> GnssSystem.BDS;
		case "5" -> GnssSystem.QZSS;
		case "6" -> GnssSystem.NAVIC;
		case "7" -> GnssSystem.RESERVED;
		default -> GnssSystem.GPS;
		};
	}

	public static FAAMode getFaaMode(String mode) {
		return switch (mode) {
			case "A" -> FAAMode.AUTONOMOUS;
			case "D" -> FAAMode.DIFFERENTIAL;
			case "E" -> FAAMode.ESTIMATED;
			case "N" -> FAAMode.DATA_NOT_VALID;
			case "R" -> FAAMode.RTK_FLOAT;
			case "S" -> FAAMode.SIMULATOR;
			case "V" -> FAAMode.DATA_NOT_VALID;
			default -> FAAMode.DATA_NOT_VALID;
		};
	}

	public static RdfQuality getRdfQuality(int q) {
		return switch (q) {
			case 0 -> RdfQuality.RDF_QUAL_0;
			case 1 -> RdfQuality.RDF_QUAL_1;
			case 2 -> RdfQuality.RDF_QUAL_2;
			case 3 -> RdfQuality.RDF_QUAL_3;
			case 4 -> RdfQuality.RDF_QUAL_4;
			case 5 -> RdfQuality.RDF_QUAL_5;
			case 6 -> RdfQuality.RDF_QUAL_6;
			case 7 -> RdfQuality.RDF_QUAL_7;
			case 8 -> RdfQuality.RDF_QUAL_8;
			default -> RdfQuality.RDF_QUAL_8;
		};
	}

	// This method must be overridden in the extending class to disconnect the data stream through the associated config class.
	// The overriding method must also call 'super.stopGPS()' to run the commands in this method.
	public void stopGPS() {
		if (executor != null) {
			try {
				LOG.log(Level.INFO, "Initializing AbstractGpsProcessor.executor service termination....");
				executor.shutdown();
				executor.awaitTermination(20, TimeUnit.SECONDS);
				LOG.log(Level.INFO, "AbstractGpsProcessor.executor service has gracefully terminated");
			} catch (InterruptedException e) {
				LOG.log(Level.SEVERE, "AbstractGpsProcessor.executor service has timed out after 20 seconds of waiting to terminate processes.");
				Thread.currentThread().interrupt();
			}
		}
		run = false;
		savePreferences();
		position = null;
		setFixQuality(FixQuality.INVALID);
		setFaaMode(FAAMode.DATA_NOT_VALID);
		setFixType(FixType.FIX_NOT_AVAILABLE);
		setValidTrueRdfHeading(false);
		setRdfHeadingTrue(-1);
		setRdfHeadingRelative(-1);
		setSpeedMadeGoodMetersPerSecond(0);
		setCourseMadeGoodTrue(-1);
		setAltitudeMeters(0);
		setAltitudeOverEllipsoid(0);
		setSatellitesInView(0);
		setHorizontalPositionErrorMeters(-1);
		setVerticalPositionErrorMeters(-1);
		setSphericalEquivalentPositionErrorMeters(-1);
		setBarometricAltitudeFeet(-1);
		setHorizontalDilutionOfPrecision(-1);
		setVerticalDilutionOfPrecision(-1);
		setRdfQuality(RdfQuality.RDF_QUAL_0);
		setGprmcMessageString("");
		setGpggaMessageString("");
		setGpwplMessageString("");
		setGpgsaMessageString("");
		setGpgsvMessageString("");
		setPkwdwplMessageString("");
		setEnableEvents(false);
		reverseGeoCode.getPropertyChangeSupport().removePropertyChangeListener(reverseGeoCodeListener);
	}

	@Override
	public void close() {
		stopGPS();
		for (PropertyChangeListener pcl : pcs.getPropertyChangeListeners()) {
			pcs.removePropertyChangeListener(pcl);
		}
	}
	
	public void startGPS() {
		run = true;
		setEnableEvents(true);
		reverseGeoCode.getPropertyChangeSupport().addPropertyChangeListener(reverseGeoCodeListener);
		executor = Executors.newCachedThreadPool();
		pcs.firePropertyChange(FIX_QUALITY, null, FixQuality.INVALID);
	}

	public static String getReceiverClassNameFor(String compositeName) {
		return getReceiverClassNameFor(compositeName, compositeName);
	}
	
	public static String getReceiverClassNameFor(String manufacturer, String model) {
		String className = (String) AbstractGpsProcessor.getCatalogMap().keySet().toArray()[0];
		try {
			final Iterator<String> iterator = AbstractGpsProcessor.getCatalogMap().keySet().iterator();
			while (iterator.hasNext()) {
				final String gpsReceiverElement = iterator.next();
			    if (manufacturer.toUpperCase(getLocale()).trim().contains(gpsReceiverElement.toUpperCase(getLocale()).trim())
						&& model.toUpperCase(getLocale()).trim().contains(gpsReceiverElement.toUpperCase(getLocale()).trim())) {
					className = gpsReceiverElement;
					break;
				}
			}
		} catch (NullPointerException ex) {
			LOG.log(Level.SEVERE, "getGPSReceiverInstanceFor(String manufacturer, String model) returns NULL", ex);
		}
		return className;
	}
	
	public static AbstractGpsProcessor getReceiverInstance(StringBuilder className, Boolean clearAllPreferences) {
		return getReceiverInstance(className.toString(), clearAllPreferences);
	}

	public static synchronized AbstractGpsProcessor getReceiverInstance(String className, Boolean clearAllPreferences) {
		boolean isValidClassName = false;
		final Iterator<String> iterator = AbstractGpsProcessor.getCatalogMap().keySet().iterator();
		while (iterator.hasNext()) {
			final String cn = iterator.next();
			if (cn.equals(className)) {
				isValidClassName = true;
				break;
			}
		}
		final Class<?> classTemp;
		AbstractGpsProcessor gpsInstance = null;
		try {
			if (!isValidClassName) {
				className = (String) AbstractGpsProcessor.getCatalogMap().keySet().toArray()[0];
			}
			classTemp = Class.forName(className);
			final Class<?>[] cArg = new Class<?>[1];
			cArg[0] = Boolean.class;
			gpsInstance = (AbstractGpsProcessor) classTemp.getDeclaredConstructor(cArg).newInstance(clearAllPreferences);
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
		return gpsInstance;
	}

	public void processJsonGpsdData(String jsonString) {
		try {
			final JSONObject jsonObject = new JSONObject(jsonString);
			if (DEFAULT_DEBUG_MODE) {
				LOG.log(Level.INFO, "GPST jsonString: {0}", jsonString);
			}
			if (isEnableEvents()) {
				pcs.firePropertyChange(AbstractGpsProcessor.GPSD_DATA, null, jsonString);
			}
			if ("TPV".equals(jsonObject.getString("class"))) {
				if (jsonObject.has("lon") && jsonObject.has("lat")) {
					setPosition(new Point2D.Double(jsonObject.getDouble("lon"), jsonObject.getDouble("lat")));
					setFixQuality(FixQuality.GPS);
				}
				if (jsonObject.has("alt")) {
					setAltitudeMeters(jsonObject.getDouble("alt"));
				}
				if (jsonObject.has("mode")) {
					setFixType(FixType.values()[jsonObject.getInt("mode")]);
				}
				if (jsonObject.has("time")) {
					setISO8601UTCTime(jsonObject.getString("time"));
				}
				if (jsonObject.has("speed")) {
					setSpeedMadeGoodMetersPerSecond(jsonObject.getDouble("speed"));
				}
				if (jsonObject.has("track")) {
					setCourseMadeGoodTrue(jsonObject.getDouble("track"));
				}
			}	
			if ("SKY".equals(jsonObject.getString("class"))) {
				if (jsonObject.has("hdop")) {
					setHorizontalDilutionOfPrecision(jsonObject.getDouble("hdop"));
				}
				if (jsonObject.has("vdop")) {
					setVerticalDilutionOfPrecision(jsonObject.getDouble("vdop"));
				}
				if (jsonObject.has("pdop")) {
					setPositionDilutionOfPrecision(jsonObject.getDouble("pdop"));
				}
				if (jsonObject.has("prRes")) {
					setPseudorangeResidue(jsonObject.getInt("prRes"));
				}
				if (jsonObject.has("gdop")) {
					setHypersphericalDilutionOfPrecision(jsonObject.getDouble("gdop"));
				}
				if (jsonObject.has("ydop")) {
					setLatitudeDilutionOfPrecision(jsonObject.getDouble("ydop"));
				}
				if (jsonObject.has("xdop")) {
					setLongitudeDilutionOfPrecision(jsonObject.getDouble("xdop"));
				}
				if (jsonObject.has("time")) {
					setISO8601UTCTime(jsonObject.getString("time"));
				}
				if (jsonObject.has("tdop")) {
					setTimeDilutionOfPrecision(jsonObject.getDouble("tdop"));
				}
				if (jsonObject.has("qual")) {
					setFixStatus(getGpsdFixStatus(jsonObject.getInt("qual")));
				}
				final JSONArray satelliteArray = jsonObject.getJSONArray("satellites");
				for (int i = 0; i < satelliteArray.length(); i++) {
					final JSONObject sat = satelliteArray.getJSONObject(i);
					getSvList().add(new Satellite(String.valueOf(sat.getInt("PRN")), String.valueOf(sat.getInt("az")),
							String.valueOf(sat.getInt("el")), String.valueOf(sat.getInt("ss"))));
				}
			}
		} catch (JSONException ex) {
			LOG.log(Level.INFO, "NOT a Valid JSON String: {0}", jsonString);
			throw new JSONException(ex);
		}
	}
	
	public static Color getGpsColor(FixQuality fixQuality) {
		return switch (fixQuality) {
			case INVALID -> Color.RED;
			case GPS -> Color.GREEN;
			case DGPS -> Color.BLUE;
			case DEAD_RECON -> Color.ORANGE;
			default -> Color.RED;
		};
	}

	public static Color getGpsStatusBackgroundColor(FixQuality fixQuality) {
		return switch (fixQuality) {
			case INVALID -> Color.RED;
			case GPS -> Color.GREEN;
			case DGPS -> Color.BLUE;
			case DEAD_RECON -> Color.YELLOW;
			default -> Color.RED;
		};
	}

	public static Color getGpsStatusForegroundColor(FixQuality fixQuality) {
		return switch (fixQuality) {
			case INVALID -> Color.BLACK;
			case GPS -> Color.BLACK;
			case DGPS -> Color.LIGHT_GRAY;
			case DEAD_RECON -> Color.BLACK;
			default -> Color.BLACK;
		};
	}

	public static String getGpsStatusText(FixQuality fixQuality) {
		return switch (fixQuality) {
			case INVALID -> "ACQUIRE";
			case GPS -> "GPS FIX";
			case DGPS -> "DGPS Fix";
			case DEAD_RECON -> "ESTIMATE";
			default -> "";
		};
	}
}
