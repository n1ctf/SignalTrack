package signaltrack;

import aprs.APRSComponent;
import aprs.AbstractAPRSProcessor;
import aprs.AprsProcessor;
import components.EventPanel;
import gov.epa.AirNowAPI;

import gov.nasa.api.donki.NASASpaceWeatherProcessor;
import gov.nasa.api.donki.NASASpaceWeatherSettingsComponent;

import gov.nasa.api.donki.NASASpaceWeatherGUI.Style;

import gov.nasa.api.ners.NetworkEarthRotationService;

import gps.AbstractGpsProcessor;

import gps.GPSComponent;

import java.awt.Cursor;
import java.awt.EventQueue;

import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

import java.awt.geom.Point2D;

import java.time.ZoneId;

import java.util.Locale;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import meteorology.AbstractEnvironmentSensor;
import meteorology.EnvironmentMonitorComponent;
import meteorology.EnvironmentMonitorGUI;
import meteorology.EnvironmentMonitorGUI.DisplaySize;

import n1ctf.TempSensorClient;
import n1ctf.AirQualitySensorClient;
import n1ctf.GeigerCounterClient;

import time.ConsolidatedTime;
import time.ConsolidatedTimeComponent;
import time.DateTimeServiceComponent;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import org.apache.commons.lang3.ThreadUtils;

/**
 *
 * @author John
 */
public class WeatherKiosk implements AutoCloseable {

	private enum UserPrefs {
		STATION_LATITUDE_DEGREES,
		STATION_LONGITUDE_DEGREES,
		STATION_ELEVATION_METERS,
		LOCALE,
		ZONE_ID,
		GPS_CLASS_NAME
	}
	
	public static final boolean DEFAULT_PRINT_APRS_RF_REPORT_TO_NARRATIVE = true;
	public static final boolean DEFAULT_PRINT_APRS_IS_REPORT_TO_NARRATIVE = true;
	public static final boolean DEFAULT_PRINT_APRS_CWOP_REPORT_TO_NARRATIVE = true;
	public static final boolean DEFAULT_PRINT_APRS_WUG_REPORT_TO_NARRATIVE = true;
	public static final boolean DEFAULT_PRINT_APRS_RADMON_REPORT_TO_NARRATIVE = true;
	
	public static final boolean DEFAULT_SHOW_GUI = true;
    
	private static final Logger LOG = Logger.getLogger(WeatherKiosk.class.getName());

	private final Preferences userPref = Preferences.userRoot().node(getClass().getName());
	
	private int startGpsArg = -1;
	private boolean showGui = DEFAULT_SHOW_GUI;

	private String gpsClassName;

	private Boolean clearAllPrefs = false;
		
	private final ConsolidatedTime consolidatedTime;
	private final TempSensorClient tempSensor;
	private final GeigerCounterClient gc;
	private final AirQualitySensorClient aqs;
	private final AirNowAPI airNow;
	private final NetworkEarthRotationService ners;
	private final NASASpaceWeatherProcessor swp;
	private final AbstractEnvironmentSensor aes;
	private final AbstractGpsProcessor gpsProcessor;
	private final AprsProcessor aprsProcessor;	// Purpose is to operate all APRS transmission services
	private final EventPanel eventPanel;
	
    private EnvironmentMonitorGUI gui;

	/**
	 *
	 * @param args
	 */
	public WeatherKiosk(String[] args) {

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				close();
			}
		});
		
		initializeCLI(args);

		getSettings();

		consolidatedTime = new ConsolidatedTime(ZoneId.systemDefault());
		tempSensor = new TempSensorClient(false, true, false, false);
		gc = new GeigerCounterClient(true, true, false, false);
		aqs = new AirQualitySensorClient();
		airNow = new AirNowAPI(consolidatedTime.getLatitudeDegrees(), consolidatedTime.getLongitudeDegrees(), true);
		ners = new NetworkEarthRotationService();
		swp = new NASASpaceWeatherProcessor(Style.SINGLE_COLUMN_NO_TIMEOUT_DISPLAY);
		
		eventPanel = new EventPanel();
		
		aes = AbstractEnvironmentSensor.getInstance(
				(String) AbstractEnvironmentSensor.getCatalogMap().keySet().toArray()[0], consolidatedTime, clearAllPrefs);
		
		aprsProcessor = new AprsProcessor(aes, clearAllPrefs);

		if (startGpsArg != -1) {
			if (startGpsArg > AbstractGpsProcessor.getCatalogMap().size() - 1) {
				startGpsArg = 0;
			}
			gpsClassName = (String) AbstractGpsProcessor.getCatalogMap().keySet().toArray()[startGpsArg];
		}
		
		gpsProcessor = AbstractGpsProcessor.getReceiverInstance(gpsClassName, clearAllPrefs);
		
		if (showGui) {			
			gui = new EnvironmentMonitorGUI(DisplaySize.Size_Foundation_7_Inch, aes, airNow, tempSensor, gc, aqs, 
					swp, ners, aprsProcessor, consolidatedTime, eventPanel);
		}
		
		aes.addPropertyChangeListener(event -> {
			if (AbstractEnvironmentSensor.Events.HUMIDITY_GR_PER_M3.name().equals(event.getPropertyName())) {
				aqs.updateHumidityGramsPerCubicMeter((double) event.getNewValue());
			}
		});
		
		gpsProcessor.getGPSPropertyChangeSupport().addPropertyChangeListener(event -> {
			if (AbstractGpsProcessor.VALID_POSITION.equals(event.getPropertyName())) {
				if (aes.isUseGpsForStationLocation()) {
					aes.setStationLongitudeDegrees(((Point2D) event.getNewValue()).getX());
					aes.setStationLatitudeDegrees(((Point2D) event.getNewValue()).getY());
				}
				ConsolidatedTime.setLatitudeDegrees(((Point2D) event.getNewValue()).getX());
				ConsolidatedTime.setLongitudeDegrees(((Point2D) event.getNewValue()).getY());
			}
			if (AbstractGpsProcessor.VALID_ALTITUDE_METERS.equals(event.getPropertyName())) {
				if (aes.isUseGpsForStationLocation()) {
					aes.setStationElevationMeters((Double) event.getNewValue());
				}
				ConsolidatedTime.setAltitudeMeters((double) event.getNewValue());
			}
			if (AbstractGpsProcessor.VALID_TIME.equals(event.getPropertyName())) {
				consolidatedTime.setGpsTimeInMillis((long) event.getNewValue());
				aes.setGpsTimeInMillis((long) event.getNewValue());
			}
			if (AbstractGpsProcessor.TIME_ZONE_ID.equals(event.getPropertyName())) {
				consolidatedTime.setLocalZoneId((ZoneId) event.getNewValue());
				aes.setZoneId((ZoneId) event.getNewValue());	
			}
			if (AbstractGpsProcessor.LOCALE.equals(event.getPropertyName())) {
				Locale.setDefault((Locale) event.getNewValue());
				if (gui != null) {
					gui.setLocale((Locale) event.getNewValue());
				}
				aes.setLocale((Locale) event.getNewValue());
				airNow.setLocale((Locale) event.getNewValue());
				aprsProcessor.setLocale((Locale) event.getNewValue());
				
			}
		});
		
		aprsProcessor.getAPRSPropertyChangeSupport().addPropertyChangeListener(event -> {
			if (AbstractAPRSProcessor.Event.REPORT_SENTENCE.name().equals(event.getPropertyName())) {
				if (DEFAULT_PRINT_APRS_RADMON_REPORT_TO_NARRATIVE) {
					eventPanel.appendEventNarrative("APRS RADMON Report: " + event.getNewValue());
				}
				if (DEFAULT_PRINT_APRS_WUG_REPORT_TO_NARRATIVE) {
					eventPanel.appendEventNarrative("WUG Report: " + event.getNewValue());
				}
				if (DEFAULT_PRINT_APRS_CWOP_REPORT_TO_NARRATIVE) {
					eventPanel.appendEventNarrative("CWOP Report: " + event.getNewValue());
				}
				if (DEFAULT_PRINT_APRS_IS_REPORT_TO_NARRATIVE) {
					eventPanel.appendEventNarrative("APRS-IS Report: " + event.getNewValue());
				}
				if (DEFAULT_PRINT_APRS_RF_REPORT_TO_NARRATIVE) {
					eventPanel.appendEventNarrative("APRS-RF Report: " + event.getNewValue());
				}
			}
		});
		
		swp.getPropertyChangeSupport().addPropertyChangeListener(event -> {
			if (NASASpaceWeatherProcessor.EVENT_NARRATIVE.equals(event.getPropertyName())) {
				eventPanel.appendEventNarrative((String) event.getNewValue());
			}
		});

		aprsProcessor.startAPRS();
		tempSensor.start();
		gc.start();
		aes.startSensor();
		gpsProcessor.startGPS();
		consolidatedTime.startAutomaticNetworkTimeUpdates();
		
		initializeListeners();
	}

	private void initializeListeners() {
		if (gui != null) {
			gui.getExitButton().addActionListener(event -> {
			    if (event.getID() == ActionEvent.ACTION_PERFORMED) {
			    	gui.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			    	close();
			    }
			});
		}
		if (gui != null) {
			gui.getConfigAPRSButton().addActionListener(event -> {
			    if (event.getID() == ActionEvent.ACTION_PERFORMED) {
			    	gui.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			    	new APRSComponent(aprsProcessor);
			    	gui.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			    }
			});
		}
		if (gui != null) {
			gui.getConfigGPSButton().addActionListener(event -> {
			    if (event.getID() == ActionEvent.ACTION_PERFORMED) {
			    	gui.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			    	new GPSComponent(gpsProcessor);
			    	gui.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			    }
			});
		}
		if (gui != null) {
			gui.getConfigSensorButton().addActionListener(event -> {
			    if (event.getID() == ActionEvent.ACTION_PERFORMED) {
			    	gui.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			    	try (DateTimeServiceComponent dts = new DateTimeServiceComponent(consolidatedTime, ners)) {
			    		new EnvironmentMonitorComponent(aes, tempSensor, gc, aqs, dts);
			    	}
			    	gui.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			    }
			});
		}
		if (gui != null) {
			gui.getConfigSWPButton().addActionListener(event -> {
			    if (event.getID() == ActionEvent.ACTION_PERFORMED) {
			    	gui.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			    	new NASASpaceWeatherSettingsComponent(swp);
			    	gui.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			    }
			});
		}
		if (gui != null) {
			gui.getTimeZoneButton().addActionListener(event -> {
			    if (event.getID() == ActionEvent.ACTION_PERFORMED) {
			    	gui.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			    	new ConsolidatedTimeComponent(consolidatedTime);
			    	gui.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			    }
			});
		}
		gc.getPropertyChangeSupport().addPropertyChangeListener(event -> {
			if (GeigerCounterClient.RADIATION_CPM.equals(event.getPropertyName())) {
				aes.setCountsPerMinute((int) event.getNewValue());
			}
		});
		gc.getPropertyChangeSupport().addPropertyChangeListener(event -> {
			if (GeigerCounterClient.GAMMA_RADIATION.equals(event.getPropertyName())) {
				aes.setGammaRadiationMicroSievertsPerHour((double) event.getNewValue());
			}
		});
		gc.getPropertyChangeSupport().addPropertyChangeListener(event -> {
			if (GeigerCounterClient.BETA_RADIATION.equals(event.getPropertyName())) {
				aes.setBetaRadiationMicroSievertsPerHour((double) event.getNewValue());
			}
		});
		gc.getPropertyChangeSupport().addPropertyChangeListener(event -> {
			if (GeigerCounterClient.ALPHA_RADIATION.equals(event.getPropertyName())) {
				aes.setAlphaRadiationMicroSievertsPerHour((double) event.getNewValue());
			}
		});
		aqs.getPropertyChangeSupport().addPropertyChangeListener(event -> {
			if (AirQualitySensorClient.ECO2.equals(event.getPropertyName())) {
				aes.setCarbonDioxidePPM((int) event.getNewValue());
			}
		});
		aqs.getPropertyChangeSupport().addPropertyChangeListener(event -> {
			if (AirQualitySensorClient.TVOC.equals(event.getPropertyName())) {
				aes.setTotalVolatileOrganicCompoundsPPB((int) event.getNewValue()); 
			}
		});
	}
	
	private void initializeCLI(String[] args) {
		try {
			final Options options = new Options();
			final CommandLineParser parser = new DefaultParser();
			
			options.addOption("n", "nogui", false , "no gui");
			options.addOption("g", true, "gpsProcessor");
			options.addOption("c", "clear", false, "clear all preferences");

			final CommandLine cmd = parser.parse(options, args);

			if (cmd.hasOption("n")) {
				showGui = false;
			}                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          
			
			if (cmd.hasOption("g")) {
				startGpsArg = Integer.parseInt(cmd.getOptionValue("g"));
			}

			if (cmd.hasOption("c")) {
				clearAllPrefs = true;
				try {
					userPref.clear();
				} catch (final BackingStoreException ex) {
					LOG.log(Level.WARNING, ex.getMessage(), ex);
				}
			}

		} catch (ParseException ex) {
			LOG.log(Level.WARNING, ex.getMessage(), ex);
		} catch (IllegalArgumentException ex) {
			LOG.log(Level.WARNING, "An Illegal Argument has been passed via the command line", ex);
		}
	}

	@Override
	public void close() {
		saveSettings();

		if (consolidatedTime != null) {
			consolidatedTime.close();
		}
		
		if (tempSensor != null) {
			tempSensor.close();
		}
		
		if (airNow != null) {
			airNow.close();
		}
		
		if (ners != null) {
			ners.close();
		}
		
		if (swp != null) {
			swp.close();
		}
		
		if (aes != null) {
			aes.close();
		}
		
		if (aqs != null) {
			aqs.close();
		}
		
		if (gc != null) {
			gc.close();
		}
		
		if (gpsProcessor != null) {
			gpsProcessor.close();
		}
		
		if (aprsProcessor != null) {
			aprsProcessor.close();
		}
		
		if (gui != null) {
			gui.dispatchEvent(new WindowEvent(gui, WindowEvent.WINDOW_CLOSING));
		}
		
		ThreadUtils.getAllThreads().forEach(t -> LOG.log(Level.INFO, "Thread Name: {0} , Is Daemon: {1}", new Object[] { t.getName(), t.isDaemon() }));
		
		LOG.log(Level.INFO, "Program Complete");
		
		System.exit(0);
	}

	private void getSettings() {
		gpsClassName = userPref.get(UserPrefs.GPS_CLASS_NAME.name(), (String) AbstractGpsProcessor.getCatalogMap().keySet().toArray()[0]);
	}
	
	private void saveSettings() {
		userPref.put(UserPrefs.GPS_CLASS_NAME.name(), gpsClassName);
	}
	
	public static void main(final String[] args) {
		EventQueue.invokeLater(() -> new WeatherKiosk(args));
	}

}
