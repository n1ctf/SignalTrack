package aprs;

import java.awt.geom.Point2D;

import java.beans.PropertyChangeSupport;

import java.util.Locale;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import meteorology.AbstractEnvironmentSensor;

public class AprsProcessor implements AutoCloseable {
	
	public enum Event {
		OPERATE_APRS_ROTATOR,
		APRS_SYMBOL_SIZE_UPDATED
	}
	
	public static final boolean DEFAULT_DEBUG = true;
	
	public static final String CRLF = "\r\n";
	
	private static final String DEFAULT_SOFTWARE_VERSION = "SignalTrack-1.9.3";
	
	private static final Preferences userPrefs = Preferences.userRoot().node(AprsProcessor.class.getName());

	private Locale locale;

	private static final Logger LOG = Logger.getLogger(AprsProcessor.class.getName());

	private final ExecutorService executor = Executors.newSingleThreadExecutor();

	private String softwareVersion = DEFAULT_SOFTWARE_VERSION;

	private int symbolSize;
	private int timeToLiveMinutes;
	private int timeToGoStaleMinutes;
	private int courseMadeGood;

	private boolean timeToLiveEnabled;
	private boolean timeToGoStaleEnabled;
	private boolean startAPRSWithSystem;
	private boolean enableAPRSTracking;
	private boolean enableIconLabels;
	private boolean reportCRCErrors;
	
	private boolean rotate;
	
	private Point2D reportPosition;
	
	private double speedMadeGood;
	
	protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	private final AbstractEnvironmentSensor environmentSensor;
	
	private final AbstractAPRSProcessor aprsTNCClient;
	private final AbstractAPRSProcessor aprsIsClient;
	private final AbstractAPRSProcessor cwopUpdater;
	private final AbstractAPRSProcessor wugUpdater;
	private final AbstractAPRSProcessor radMonAPI;
	
	public AprsProcessor(AbstractEnvironmentSensor environmentSensor, boolean clearAllPreferences) {
		this.environmentSensor = environmentSensor;
		
		if (clearAllPreferences) {
			try {
				AprsProcessor.userPrefs.clear();
			} catch (final BackingStoreException ex) {
				if (isDebug()) {
					LOG.log(Level.WARNING, ex.getMessage(), ex);
				}
			}
		}
		
		aprsTNCClient = AbstractTerminalNodeClient.getTNCInstance(
				(String) AbstractTerminalNodeClient.getCatalogMap().keySet().toArray()[0], environmentSensor, clearAllPreferences);
		
		aprsIsClient = new AprsIsClient(environmentSensor, clearAllPreferences);
		wugUpdater = new WUndergroundUpdater(environmentSensor, clearAllPreferences);
		radMonAPI = new RadMonAPI(environmentSensor, clearAllPreferences);
		cwopUpdater = new CWOPUpdater(environmentSensor, clearAllPreferences);
		
	}

	private void loadPreferences() {
		symbolSize = userPrefs.getInt("APRSSymbolSize", 24);
		startAPRSWithSystem = userPrefs.getBoolean("StartAPRSWithSystem", false);
		enableAPRSTracking = userPrefs.getBoolean("EnableAPRSTracking", true);
		enableIconLabels = userPrefs.getBoolean("EnableIconLabels", true);
		reportCRCErrors = userPrefs.getBoolean("ReportCRCErrors", true);
		timeToLiveEnabled = userPrefs.getBoolean("TimeToLive", true);
		timeToGoStaleEnabled = userPrefs.getBoolean("TimeToGoStaleEnabled", true);
		timeToLiveMinutes = userPrefs.getInt("TimeToLiveMinutes", 60);
		timeToGoStaleMinutes = userPrefs.getInt("TimeToGoStaleMinutes", 30);
		aprsIsClient.runService(userPrefs.getBoolean("ReportIS", false));
		aprsTNCClient.runService(userPrefs.getBoolean("ReportTNC", false));
		cwopUpdater.runService(userPrefs.getBoolean("ReportCWOP", false));
		wugUpdater.runService(userPrefs.getBoolean("ReportWUG", false));
		radMonAPI.runService(userPrefs.getBoolean("ReportRadMon", false));
	}

	public void savePreferences() {
		userPrefs.putInt("APRSSymbolSize", symbolSize);
		userPrefs.putBoolean("StartAPRSWithSystem", startAPRSWithSystem);
		userPrefs.putBoolean("EnableAPRSTracking", enableAPRSTracking);
		userPrefs.putBoolean("EnableIconLabels", enableIconLabels);
		userPrefs.putBoolean("ReportCRCErrors", reportCRCErrors);
		userPrefs.putBoolean("TimeToLive", timeToLiveEnabled);
		userPrefs.putBoolean("TimeToGoStaleEnabled", timeToGoStaleEnabled);
		userPrefs.putInt("TimeToLiveMinutes", timeToLiveMinutes);
		userPrefs.putInt("TimeToGoStaleMinutes", timeToGoStaleMinutes);
		userPrefs.putBoolean("ReportIS", aprsIsClient.isReportEnabled());
		userPrefs.putBoolean("ReportTNC", aprsTNCClient.isReportEnabled());
		userPrefs.putBoolean("ReportCWOP", cwopUpdater.isReportEnabled());
		userPrefs.putBoolean("ReportWUG", wugUpdater.isReportEnabled());
		userPrefs.putBoolean("ReportRadMon", radMonAPI.isReportEnabled());
		
		updateRotateStatus();
	}

	public AbstractAPRSProcessor getRadMonAPI() {
		return radMonAPI;
	}
	
	public AbstractAPRSProcessor getCWOPUpdater() {
		return cwopUpdater;
	}
		
	public AbstractAPRSProcessor getWUGUpdater() {
		return wugUpdater;
	}
	
	public AbstractAPRSProcessor getAPRSISClient() {
		return aprsIsClient;
	}
	
	public AbstractAPRSProcessor getAPRSTNCClient() {
		return aprsTNCClient;
	}

	public String getSensorNameString() {
		return environmentSensor.getDeviceManufacturer() + "_" + environmentSensor.getDeviceModel();
	}
	
	public String getSoftwareVersion() {
		return softwareVersion;
	}

	public void setSoftwareVersion(String softwareVersion) {
		this.softwareVersion = softwareVersion;
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public PropertyChangeSupport getAPRSPropertyChangeSupport() {
		return pcs;
	}

	public boolean isDebug() {
		return DEFAULT_DEBUG;
	}

	public int getTimeToGoStaleMinutes() {
		if (isTimeToGoStaleEnabled()) {
			return timeToGoStaleMinutes;
		} else {
			return Integer.MAX_VALUE;
		}
	}

	public void setTimeToGoStaleMinutes(int timeToGoStaleMinutes) {
		this.timeToGoStaleMinutes = timeToGoStaleMinutes;
	}

	public int getTimeToLiveMinutes() {
		return isTimeToLiveEnabled() ? timeToLiveMinutes : Integer.MAX_VALUE;
	}

	public boolean isReportCRCErrors() {
		return reportCRCErrors;
	}

	public void setReportCRCErrors(boolean reportCRCErrors) {
		this.reportCRCErrors = reportCRCErrors;
	}

	public void setTimeToLiveMinutes(int timeToLiveMinutes) {
		this.timeToLiveMinutes = timeToLiveMinutes;
	}

	public boolean isTimeToLiveEnabled() {
		return timeToLiveEnabled;
	}

	public void setTimeToLiveEnabled(boolean timeToLiveEnabled) {
		this.timeToLiveEnabled = timeToLiveEnabled;
	}

	public boolean isTimeToGoStaleEnabled() {
		return timeToGoStaleEnabled;
	}

	public void setTimeToGoStaleEnabled(boolean timeToGoStaleEnabled) {
		this.timeToGoStaleEnabled = timeToGoStaleEnabled;
	}

	public boolean isEnableIconLabels() {
		return enableIconLabels;
	}

	public void setEnableIconLabels(boolean enableIconLabels) {
		this.enableIconLabels = enableIconLabels;
	}

	public void setAprsSymbolSize(int aprsSymbolSize) {
		this.symbolSize = aprsSymbolSize;
		pcs.firePropertyChange(Event.APRS_SYMBOL_SIZE_UPDATED.name(), null, aprsSymbolSize);
	}

	public boolean isStartAPRSWithSystem() {
		return startAPRSWithSystem;
	}

	public void setStartAPRSWithSystem(boolean startAPRSWithSystem) {
		this.startAPRSWithSystem = startAPRSWithSystem;
	}

	public boolean isEnableAPRSTracking() {
		return enableAPRSTracking;
	}

	public void setEnableAPRSTracking(boolean enableAPRSTracking) {
		this.enableAPRSTracking = enableAPRSTracking;
	}

	public Point2D getReportPosition() {
		return reportPosition;
	}

	public double getReportSpeedMadeGood() {
		return speedMadeGood;
	}

	public int getReportCourseMadeGood() {
		return courseMadeGood;
	}

	public void setSymbolSize(int symbolSize) {
		this.symbolSize = symbolSize;
	}

	public int getSymbolSize() {
		return symbolSize;
	}

	public void stopAPRS() {	
		rotate = false;	
		
		if (executor != null) {
			try {
				LOG.log(Level.INFO, "Initializing AbstractAPRSProcessor.executor termination....");
				executor.shutdown();
				executor.awaitTermination(5, TimeUnit.SECONDS);
				LOG.log(Level.INFO, "AbstractAPRSProcessor.executor has gracefully terminated");
			} catch (InterruptedException e) {
				executor.shutdownNow();
				LOG.log(Level.SEVERE, "AbstractAPRSProcessor.executor has timed out after 5 seconds of waiting to terminate processes.");
				Thread.currentThread().interrupt();
			}
		}

		aprsTNCClient.runService(false);
		aprsIsClient.runService(false);
		radMonAPI.runService(false);
		cwopUpdater.runService(false);
		wugUpdater.runService(false);
		
		aprsTNCClient.close();
		aprsIsClient.close();
		radMonAPI.close();
		cwopUpdater.close();
		wugUpdater.close();
		
		pcs.firePropertyChange(Event.OPERATE_APRS_ROTATOR.name(), null, rotate);
	}

	public void startAPRS() {
		loadPreferences();
		updateRotateStatus();
	}
	
	public boolean isRotate() {
		return aprsTNCClient.isReportEnabled() || aprsIsClient.isReportEnabled() || radMonAPI.isReportEnabled() 
			|| cwopUpdater.isReportEnabled() || wugUpdater.isReportEnabled();
	}
	
	public void updateRotateStatus() {
		final boolean r = isRotate();
		pcs.firePropertyChange(Event.OPERATE_APRS_ROTATOR.name(), this.rotate, r);
		this.rotate = r;
	}
	
	@Override
	public void close() {
		savePreferences();
		stopAPRS();
	}

	public static String getIconPathNameFromSSID(final String ssid) {
		final String sRet;
		switch (ssid) {
			case "0" -> sRet = "SSID-00.png";
			case "1" -> sRet = "SSID-01.png";
			case "2" -> sRet = "SSID-02.png";
			case "3" -> sRet = "SSID-03.png";
			case "4" -> sRet = "SSID-04.png";
			case "5" -> sRet = "SSID-05.png";
			case "6" -> sRet = "SSID-06.png";
			case "7" -> sRet = "SSID-07.png";
			case "8" -> sRet = "SSID-08.png";
			case "9" -> sRet = "SSID-09.png";
			case "10" -> sRet = "SSID-10.png";
			case "11" -> sRet = "SSID-11.png";
			case "12" -> sRet = "SSID-12.png";
			case "13" -> sRet = "SSID-13.png";
			case "14" -> sRet = "SSID-14.png";
			case "15" -> sRet = "SSID-15.png";
			default -> sRet = "SSID-00.png";
		}
		return sRet;
	}

}
