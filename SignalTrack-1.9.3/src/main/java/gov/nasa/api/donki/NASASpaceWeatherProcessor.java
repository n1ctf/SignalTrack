package gov.nasa.api.donki;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeSupport;
import java.awt.Color;
import java.awt.EventQueue;
import java.net.URL;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javax.swing.SwingUtilities;

import gov.nasa.api.donki.NASASpaceWeatherGUI.Style;

/**
 *
 * @author n1ctf
 */
public class NASASpaceWeatherProcessor implements AutoCloseable {
	public static final String EVENT_NARRATIVE = "EVENT_NARRATIVE";
	
	private static final String CRLF = "\r\n";
	private static final Preferences userPrefs = Preferences.userRoot().node(NASASpaceWeatherProcessor.class.getName());
	private static final int TERMINATE_TIMEOUT = 10;  // seconds
    private static final Logger LOG = Logger.getLogger(NASASpaceWeatherProcessor.class.getName());
    
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	private final List<AbstractNasaMonitor> nasaMonitor = new ArrayList<>(8);
	
	private final NASASpaceWeatherGUI nasaSpaceWeatherGUI;
	private final RSGPanel rsgPanel;
	
	private final ScheduledExecutorService startScheduler = Executors.newScheduledThreadPool(1);
	
	private int startMonitorIndex;
	private Style style;
	
	public NASASpaceWeatherProcessor() {
		this(Style.SINGLE_COLUMN);
	}
	
	public NASASpaceWeatherProcessor(Style style) {
		this.style = style;

		nasaSpaceWeatherGUI = new NASASpaceWeatherGUI();
		rsgPanel = new RSGPanel();
		
		initializeShutdownHook();
		initializeComponents();
		getPreferences();
		configureListeners();
		configureComponents();
		initializeSchedulers();
	}
	
	private void initializeComponents() {
		nasaMonitor.add(new NasaSolarFlareMonitor(AbstractNasaMonitor.JOHNS_API_KEY, true));
		nasaMonitor.add(new NasaCMEMonitor(AbstractNasaMonitor.JOHNS_API_KEY, true));
		nasaMonitor.add(new NasaGeomagneticStormMonitor(AbstractNasaMonitor.JOHNS_API_KEY, true));
		nasaMonitor.add(new NasaSolarEnergeticParticleMonitor(AbstractNasaMonitor.JOHNS_API_KEY, true));
		nasaMonitor.add(new NasaInterplanetaryShockMonitor(AbstractNasaMonitor.JOHNS_API_KEY, true));
		nasaMonitor.add(new NasaRadiationBeltEnhancementMonitor(AbstractNasaMonitor.JOHNS_API_KEY, true));
		nasaMonitor.add(new NasaHighSpeedStreamMonitor(AbstractNasaMonitor.JOHNS_API_KEY, true));
		nasaMonitor.add(new NasaMagnetopauseCrossingMonitor(AbstractNasaMonitor.JOHNS_API_KEY, true));
	}
	
	private void configureComponents() {
		invokeLaterInDispatchThreadIfNeeded(() -> {
			for (int i = 0; i < nasaMonitor.size(); i++) {
				nasaSpaceWeatherGUI.getFlag().get(i).setText(nasaMonitor.get(i).getClassDescriptorString());
				nasaSpaceWeatherGUI.getFlag().get(i).setBackground(Color.LIGHT_GRAY);
			}
		});
	}
	
	public RSGPanel getRSGPanel() {
		return rsgPanel;
	}
	
	public void setStyle(Style style) {
		this.style = style;
	}
	
	public List<AbstractNasaMonitor> getNasaMonitorList() {
		return new ArrayList<>(nasaMonitor);
	}

	public AbstractNasaMonitor getNasaMonitor(int element) {
		return nasaMonitor.get(element);
	}
	
	protected void savePreferences() {
		for (int i = 0; i < nasaMonitor.size(); i++) {
			userPrefs.putBoolean("MonitorEnable_" + i, nasaMonitor.get(i).isEnabled());
			userPrefs.putLong("MonitorPersistenceMinutes_" + i, nasaMonitor.get(i).getPersistenceMinutes());
			userPrefs.putLong("MonitorRecheckSeconds_" + i, nasaMonitor.get(i).getCheckPeriodSeconds());
		}
		configureComponents();
	}

	private void getPreferences() {
		for (int i = 0; i < nasaMonitor.size(); i++) {
			nasaMonitor.get(i).setPersistenceMinutes(userPrefs.getLong("MonitorPersistenceMinutes_" + i, nasaMonitor.get(i).getDefaultPersistencePeriodMinutes()));
			nasaMonitor.get(i).setCheckPeriodSeconds(userPrefs.getLong("MonitorRecheckSeconds_" + i, nasaMonitor.get(i).getDefaultApiQueryPeriodSeconds()));
			nasaMonitor.get(i).setEnabled(userPrefs.getBoolean("MonitorEnable_" + i, true));
		}
	}
	
	private void updateFlagWithTimeAndMinutesRemaining(int element, long seconds, long minutes) {
		String ttu = " TTU:" + "%03d".formatted(seconds) + " S";
		if (minutes != -1) {
			ttu += " TE:" + "%05d".formatted(minutes) + " M";
		}
		final String str = ttu;
		invokeLaterInDispatchThreadIfNeeded(() -> nasaSpaceWeatherGUI.getFlag(element).setText(nasaMonitor.get(element).getFlagText() + str));
	}
	
	private class StartMonitors implements Runnable {
		@Override
		public void run() {
			if (nasaMonitor.get(startMonitorIndex).isEnabled()) {
				nasaMonitor.get(startMonitorIndex).start();
			}
			startMonitorIndex++;
			if (startMonitorIndex >= nasaMonitor.size()) {
				if (startScheduler != null) {
					try {
						LOG.log(Level.INFO, "Initializing NASASpaceWeatherProcessor.startScheduler service termination....");
						startScheduler.shutdown();
						startScheduler.awaitTermination(20, TimeUnit.SECONDS);
						LOG.log(Level.INFO, "NASASpaceWeatherProcessor.startScheduler service has gracefully terminated");
					} catch (InterruptedException e) {
						LOG.log(Level.SEVERE, "NASASpaceWeatherProcessor.startScheduler service has timed out after 20 seconds of waiting to terminate processes.");
						Thread.currentThread().interrupt();
					}
				}
			}
		}
	}
	
	private void initializeSchedulers() {	
		startScheduler.scheduleAtFixedRate(new StartMonitors(), 2000, 3000, TimeUnit.MILLISECONDS);
	}
	
	private void configureListeners() {
		nasaMonitor.get(0).addPropertyChangeListener(this::flrMonitorChangeEvent);
		nasaMonitor.get(1).addPropertyChangeListener(this::cmeMonitorChangeEvent);
		nasaMonitor.get(2).addPropertyChangeListener(this::gstMonitorChangeEvent);
		nasaMonitor.get(3).addPropertyChangeListener(this::sepMonitorChangeEvent);
		nasaMonitor.get(4).addPropertyChangeListener(this::ipsMonitorChangeEvent);
		nasaMonitor.get(5).addPropertyChangeListener(this::rbeMonitorChangeEvent);
		nasaMonitor.get(6).addPropertyChangeListener(this::hssMonitorChangeEvent);
		nasaMonitor.get(7).addPropertyChangeListener(this::mpcMonitorChangeEvent);
	}
	
	private void flrMonitorChangeEvent(final PropertyChangeEvent event) {
		invokeLaterInDispatchThreadIfNeeded(() -> {
			if (event.getPropertyName().equals(NasaSolarFlareMonitor.Event.FLARE_ID.name())) {
				pcs.firePropertyChange(EVENT_NARRATIVE, null, "Flare ID: " + (String) event.getNewValue() + CRLF);
			}
			if (event.getPropertyName().equals(NasaSolarFlareMonitor.Event.RADIO_BLACKOUT.name())) {
				rsgPanel.setRadioBlackout((RadioBlackout) event.getNewValue());
			}
			if (event.getPropertyName().equals(NasaSolarFlareMonitor.Event.BEGIN_TIME.name())) {
				pcs.firePropertyChange(EVENT_NARRATIVE, null, 
						"Solar Flare HAS Occured at: " + ((ZonedDateTime) event.getNewValue()).format(DateTimeFormatter.ISO_INSTANT) + CRLF);
			}
			if (event.getPropertyName().equals(NasaSolarFlareMonitor.Event.PEAK_TIME.name())) {
				pcs.firePropertyChange(EVENT_NARRATIVE, null, 
						"Solar Flare will PEAK at: " + ((ZonedDateTime) event.getNewValue()).format(DateTimeFormatter.ISO_INSTANT) + CRLF);
			}
			if (event.getPropertyName().equals(NasaSolarFlareMonitor.Event.END_TIME.name())) {
				pcs.firePropertyChange(EVENT_NARRATIVE, null, 
						"Solar Flare will END at: " + ((ZonedDateTime) event.getNewValue()).format(DateTimeFormatter.ISO_INSTANT) + CRLF);
			}
			if (event.getPropertyName().equals(NasaSolarFlareMonitor.Event.NO_EVENTS.name())) {
				pcs.firePropertyChange(EVENT_NARRATIVE, null, (String) event.getNewValue() + CRLF);
			}
			if (event.getPropertyName().equals(NasaSolarFlareMonitor.Event.NETWORK_ERROR.name())) {
				pcs.firePropertyChange(EVENT_NARRATIVE, null, (String) event.getNewValue() + CRLF);
			}
			if (event.getPropertyName().equals(NasaSolarFlareMonitor.Event.LINK.name())) {
				nasaSpaceWeatherGUI.setFlareEventLink((URL) event.getNewValue());
			}
			if (event.getPropertyName().equals(NasaSolarFlareMonitor.Event.DATA_FETCH_COMPLETE.name())) {
				nasaSpaceWeatherGUI.getFlag(0).setForeground(nasaMonitor.get(0).getFlagTextColor());
				nasaSpaceWeatherGUI.getFlag(0).setBackground(nasaMonitor.get(0).getFlagColor());
				nasaSpaceWeatherGUI.getFlag(0).setText(nasaMonitor.get(0).getFlagText());
				nasaSpaceWeatherGUI.getFlag(0).setToolTipText(nasaMonitor.get(0).getToolTipText());
			}
			if (event.getPropertyName().equals(AbstractNasaMonitor.Events.MINUTES_ELAPSED.name())) {
				if (style != Style.SINGLE_COLUMN_NO_TIMEOUT_DISPLAY) {
					updateFlagWithTimeAndMinutesRemaining(0, (long) event.getOldValue(), (long) event.getNewValue());			
				}
			}
		});
	}
	
	private void cmeMonitorChangeEvent(final PropertyChangeEvent event) {
		invokeLaterInDispatchThreadIfNeeded(() -> {
			if (event.getPropertyName().equals(NasaCMEMonitor.Event.ACTIVITY_ID.name())) {
				pcs.firePropertyChange(EVENT_NARRATIVE, null, "CME ActivityID: " + (String) event.getNewValue() + CRLF);
			}
			if (event.getPropertyName().equals(NasaCMEMonitor.Event.IS_EARTH_GB.name())) {
				pcs.firePropertyChange(EVENT_NARRATIVE, null, 
						"CME Expect Earth Glanging Blow: " + (((boolean) event.getNewValue()) ? "TRUE" : "FALSE") + CRLF);
			}
			if (event.getPropertyName().equals(NasaCMEMonitor.Event.TIME21_5.name())) {
				pcs.firePropertyChange(EVENT_NARRATIVE, null, 
						"Coronal Mass Ejection HAS Occured at: " + ((ZonedDateTime) event.getNewValue()).format(DateTimeFormatter.ISO_INSTANT) + CRLF);
			}
			if (event.getPropertyName().equals(NasaCMEMonitor.Event.ESTIMATED_SHOCK_ARRIVAL_TIME.name())) {
				pcs.firePropertyChange(EVENT_NARRATIVE, null, "CME Estimated Arrival Time: "
						+ ((ZonedDateTime) event.getNewValue()).format(DateTimeFormatter.ISO_INSTANT) + CRLF);
			}
			if (event.getPropertyName().equals(NasaCMEMonitor.Event.ESTIMATED_DURATION.name())) {
				pcs.firePropertyChange(EVENT_NARRATIVE, null, "CME Duration: " + (double) event.getNewValue() + CRLF);
			}
			if (event.getPropertyName().equals(NasaCMEMonitor.Event.DATA_FETCH_COMPLETE.name())) {
				nasaSpaceWeatherGUI.getFlag(1).setForeground(nasaMonitor.get(1).getFlagTextColor());
				nasaSpaceWeatherGUI.getFlag(1).setBackground(nasaMonitor.get(1).getFlagColor());
				nasaSpaceWeatherGUI.getFlag(1).setText(nasaMonitor.get(1).getFlagText());
				nasaSpaceWeatherGUI.getFlag(1).setToolTipText(nasaMonitor.get(1).getToolTipText());
			}
			if (event.getPropertyName().equals(NasaCMEMonitor.Event.LINK.name())) {
				pcs.firePropertyChange(EVENT_NARRATIVE, null, event.getNewValue() + CRLF);
				nasaSpaceWeatherGUI.setCMEEventLink((URL) event.getNewValue());
			}
			if (event.getPropertyName().equals(NasaCMEMonitor.Event.NO_EVENTS.name())) {
				pcs.firePropertyChange(EVENT_NARRATIVE, null, (String) event.getNewValue() + CRLF);
			}
			if (event.getPropertyName().equals(NasaCMEMonitor.Event.NETWORK_ERROR.name())) {
				pcs.firePropertyChange(EVENT_NARRATIVE, null, (String) event.getNewValue() + CRLF);
			}
			if (event.getPropertyName().equals(AbstractNasaMonitor.Events.MINUTES_ELAPSED.name())) {
				if (style != Style.SINGLE_COLUMN_NO_TIMEOUT_DISPLAY) {
					updateFlagWithTimeAndMinutesRemaining(1, (long) event.getOldValue(), (long) event.getNewValue());
				}
			}
		});
	}
	
	private void gstMonitorChangeEvent(final PropertyChangeEvent event) {
		invokeLaterInDispatchThreadIfNeeded(() -> {
			if (event.getPropertyName().equals(NasaGeomagneticStormMonitor.Event.GST_ID.name())) {
				pcs.firePropertyChange(EVENT_NARRATIVE, null, (String) event.getNewValue() + CRLF);
			}
			if (event.getPropertyName().equals(NasaGeomagneticStormMonitor.Event.START_TIME.name())) {
				pcs.firePropertyChange(EVENT_NARRATIVE, null, "GST Start Time: "
						+ ((ZonedDateTime) event.getNewValue()).format(DateTimeFormatter.ISO_INSTANT) + CRLF);
			}
			if (event.getPropertyName().equals(NasaGeomagneticStormMonitor.Event.LINK.name())) {
				pcs.firePropertyChange(EVENT_NARRATIVE, null, event.getNewValue() + CRLF);
				nasaSpaceWeatherGUI.setGSTEventLink((URL) event.getNewValue());
			}
			if (event.getPropertyName().equals(NasaGeomagneticStormMonitor.Event.OBSERVED_TIME.name())) {
				pcs.firePropertyChange(EVENT_NARRATIVE, null, "GST Observed Time: "
						+ ((ZonedDateTime) event.getNewValue()).format(DateTimeFormatter.ISO_INSTANT) + CRLF);
			}
			if (event.getPropertyName().equals(NasaGeomagneticStormMonitor.Event.KP_INDEX.name())) {
				nasaSpaceWeatherGUI.setGeomagneticStormFlag(Color.BLACK, nasaMonitor.get(2).getFlagColor(),
						nasaMonitor.get(2).getFlagText());
			}
			if (event.getPropertyName().equals(NasaGeomagneticStormMonitor.Event.GEOMAGNETIC_STORM.name())) {
				rsgPanel.setGeomagneticStorm((GeomagneticStorm) event.getNewValue());
			}
			if (event.getPropertyName().equals(NasaGeomagneticStormMonitor.Event.NO_EVENTS.name())) {
				pcs.firePropertyChange(EVENT_NARRATIVE, null, (String) event.getNewValue() + CRLF);
			}
			if (event.getPropertyName().equals(NasaGeomagneticStormMonitor.Event.NETWORK_ERROR.name())) {
				pcs.firePropertyChange(EVENT_NARRATIVE, null, (String) event.getNewValue() + CRLF);
			}
			if (event.getPropertyName().equals(NasaGeomagneticStormMonitor.Event.DATA_FETCH_COMPLETE.name())) {
				nasaSpaceWeatherGUI.getFlag(2).setForeground(nasaMonitor.get(2).getFlagTextColor());
				nasaSpaceWeatherGUI.getFlag(2).setBackground(nasaMonitor.get(2).getFlagColor());
				nasaSpaceWeatherGUI.getFlag(2).setText(nasaMonitor.get(2).getFlagText());
				nasaSpaceWeatherGUI.getFlag(2).setToolTipText(nasaMonitor.get(2).getToolTipText());
			}
			if (event.getPropertyName().equals(AbstractNasaMonitor.Events.MINUTES_ELAPSED.name())) {
				if (style != Style.SINGLE_COLUMN_NO_TIMEOUT_DISPLAY) {
					updateFlagWithTimeAndMinutesRemaining(2, (long) event.getOldValue(), (long) event.getNewValue());			
				}
			}
		});
	}
	
	private void sepMonitorChangeEvent(final PropertyChangeEvent event) {
		invokeLaterInDispatchThreadIfNeeded(() -> {
			if (event.getPropertyName().equals(NasaSolarEnergeticParticleMonitor.Event.SEP_ID.name())) {
				pcs.firePropertyChange(EVENT_NARRATIVE, null, (String) event.getNewValue() + CRLF);
			}
			if (event.getPropertyName().equals(NasaSolarEnergeticParticleMonitor.Event.EVENT_TIME.name())) {
				pcs.firePropertyChange(EVENT_NARRATIVE, null, "SEP Emmitted on : "
						+ ((ZonedDateTime) event.getNewValue()).format(DateTimeFormatter.ISO_INSTANT) + CRLF);
			}
			if (event.getPropertyName().equals(NasaSolarEnergeticParticleMonitor.Event.LINK.name())) {
				pcs.firePropertyChange(EVENT_NARRATIVE, null, event.getNewValue() + CRLF);
				nasaSpaceWeatherGUI.setSEPEventLink((URL) event.getNewValue());
			}
			if (event.getPropertyName().equals(NasaSolarEnergeticParticleMonitor.Event.NO_EVENTS.name())) {
				pcs.firePropertyChange(EVENT_NARRATIVE, null, (String) event.getNewValue() + CRLF);
			}
			if (event.getPropertyName().equals(NasaSolarEnergeticParticleMonitor.Event.NETWORK_ERROR.name())) {
				pcs.firePropertyChange(EVENT_NARRATIVE, null, "The NASA Server returned HTTP response code 503 for URL "
						+ "https://api.nasa.gov/DONKI/SEP" + CRLF);
			}
			if (event.getPropertyName().equals(NasaSolarEnergeticParticleMonitor.Event.SOLAR_RADIATION_STORM.name())) {
				rsgPanel.setSolarRadiationStorm((SolarRadiationStorm) event.getNewValue());
			}
			if (event.getPropertyName().equals(NasaSolarEnergeticParticleMonitor.Event.DATA_FETCH_COMPLETE.name())) {
				nasaSpaceWeatherGUI.getFlag(3).setForeground(nasaMonitor.get(3).getFlagTextColor());
				nasaSpaceWeatherGUI.getFlag(3).setBackground(nasaMonitor.get(3).getFlagColor());
				nasaSpaceWeatherGUI.getFlag(3).setText(nasaMonitor.get(3).getFlagText());
				nasaSpaceWeatherGUI.getFlag(3).setToolTipText(nasaMonitor.get(3).getToolTipText());
			}
			if (event.getPropertyName().equals(AbstractNasaMonitor.Events.MINUTES_ELAPSED.name())) {
				if (style != Style.SINGLE_COLUMN_NO_TIMEOUT_DISPLAY) {
					updateFlagWithTimeAndMinutesRemaining(3, (long) event.getOldValue(), (long) event.getNewValue());			
				}
			}
		});
	}

	private void ipsMonitorChangeEvent(final PropertyChangeEvent event) {
		invokeLaterInDispatchThreadIfNeeded(() -> {
			if (event.getPropertyName().equals(NasaInterplanetaryShockMonitor.Event.ACTIVITY_ID.name())) {
				pcs.firePropertyChange(EVENT_NARRATIVE, null, (String) event.getNewValue() + CRLF);
			}
			if (event.getPropertyName().equals(NasaInterplanetaryShockMonitor.Event.EVENT_TIME.name())) {
				pcs.firePropertyChange(EVENT_NARRATIVE, null, "IPS Colision DTG : "
						+ ((ZonedDateTime) event.getNewValue()).format(DateTimeFormatter.ISO_INSTANT) + CRLF);
			}
			if (event.getPropertyName().equals(NasaInterplanetaryShockMonitor.Event.LOCATION.name())) {
				pcs.firePropertyChange(EVENT_NARRATIVE, null, "IPS Colision Location : " + ((String) event.getNewValue()) + CRLF);
			}
			if (event.getPropertyName().equals(NasaInterplanetaryShockMonitor.Event.LINK.name())) {
				pcs.firePropertyChange(EVENT_NARRATIVE, null, event.getNewValue() + CRLF);
				nasaSpaceWeatherGUI.setIPSEventLink((URL) event.getNewValue());
			}
			if (event.getPropertyName().equals(NasaInterplanetaryShockMonitor.Event.NO_EVENTS.name())) {
				pcs.firePropertyChange(EVENT_NARRATIVE, null, (String) event.getNewValue() + CRLF);
			}
			if (event.getPropertyName().equals(NasaInterplanetaryShockMonitor.Event.NETWORK_ERROR.name())) {
				pcs.firePropertyChange(EVENT_NARRATIVE, null, "The NASA Server returned HTTP response code 503 for URL "
						+ "https://api.nasa.gov/DONKI/IPS" + CRLF);
			}
			if (event.getPropertyName().equals(NasaInterplanetaryShockMonitor.Event.DATA_FETCH_COMPLETE.name())) {
				nasaSpaceWeatherGUI.getFlag(4).setForeground(nasaMonitor.get(4).getFlagTextColor());
				nasaSpaceWeatherGUI.getFlag(4).setBackground(nasaMonitor.get(4).getFlagColor());
				nasaSpaceWeatherGUI.getFlag(4).setText(nasaMonitor.get(4).getFlagText());
				nasaSpaceWeatherGUI.getFlag(4).setToolTipText(nasaMonitor.get(4).getToolTipText());
			}
			if (event.getPropertyName().equals(AbstractNasaMonitor.Events.MINUTES_ELAPSED.name())) {
				if (style != Style.SINGLE_COLUMN_NO_TIMEOUT_DISPLAY) {
					updateFlagWithTimeAndMinutesRemaining(4, (long) event.getOldValue(), (long) event.getNewValue());			
				}
			}
		});
	}

	private void rbeMonitorChangeEvent(final PropertyChangeEvent event) {
		invokeLaterInDispatchThreadIfNeeded(() -> {
			if (event.getPropertyName().equals(NasaRadiationBeltEnhancementMonitor.Event.RBE_ID.name())) {
				pcs.firePropertyChange(EVENT_NARRATIVE, null, (String) event.getNewValue() + CRLF);
			}
			if (event.getPropertyName().equals(NasaRadiationBeltEnhancementMonitor.Event.EVENT_TIME.name())) {
				pcs.firePropertyChange(EVENT_NARRATIVE, null, "RBE Released on : "
						+ ((ZonedDateTime) event.getNewValue()).format(DateTimeFormatter.ISO_INSTANT) + CRLF);
			}
			if (event.getPropertyName().equals(NasaRadiationBeltEnhancementMonitor.Event.NO_EVENTS.name())) {
				pcs.firePropertyChange(EVENT_NARRATIVE, null, (String) event.getNewValue() + CRLF);
			}
			if (event.getPropertyName().equals(NasaRadiationBeltEnhancementMonitor.Event.LINK.name())) {
				pcs.firePropertyChange(EVENT_NARRATIVE, null, event.getNewValue() + CRLF);
				nasaSpaceWeatherGUI.setRBEEventLink((URL) event.getNewValue());
			}
			if (event.getPropertyName().equals(NasaRadiationBeltEnhancementMonitor.Event.NETWORK_ERROR.name())) {
				pcs.firePropertyChange(EVENT_NARRATIVE, null, "The NASA Server returned HTTP response code 503 for URL "
						+ "https://api.nasa.gov/DONKI/RBE" + CRLF);
			}
			if (event.getPropertyName().equals(NasaRadiationBeltEnhancementMonitor.Event.DATA_FETCH_COMPLETE.name())) {
				nasaSpaceWeatherGUI.getFlag(5).setForeground(nasaMonitor.get(5).getFlagTextColor());
				nasaSpaceWeatherGUI.getFlag(5).setBackground(nasaMonitor.get(5).getFlagColor());
				nasaSpaceWeatherGUI.getFlag(5).setText(nasaMonitor.get(5).getFlagText());
				nasaSpaceWeatherGUI.getFlag(5).setToolTipText(nasaMonitor.get(5).getToolTipText());
			}
			if (event.getPropertyName().equals(AbstractNasaMonitor.Events.MINUTES_ELAPSED.name())) {
				if (style != Style.SINGLE_COLUMN_NO_TIMEOUT_DISPLAY) {
					updateFlagWithTimeAndMinutesRemaining(5, (long) event.getOldValue(), (long) event.getNewValue());			
				}
			}
		});
	}
	
	private void hssMonitorChangeEvent(final PropertyChangeEvent event) {
		invokeLaterInDispatchThreadIfNeeded(() -> {
			if (event.getPropertyName().equals(NasaHighSpeedStreamMonitor.Event.HSS_ID.name())) {
				pcs.firePropertyChange(EVENT_NARRATIVE, null, (String) event.getNewValue() + CRLF);
			}
			if (event.getPropertyName().equals(NasaHighSpeedStreamMonitor.Event.EVENT_TIME.name())) {
				pcs.firePropertyChange(EVENT_NARRATIVE, null, "HSS Released on : "
						+ ((ZonedDateTime) event.getNewValue()).format(DateTimeFormatter.ISO_INSTANT) + CRLF);
			}
			if (event.getPropertyName().equals(NasaHighSpeedStreamMonitor.Event.NO_EVENTS.name())) {
				pcs.firePropertyChange(EVENT_NARRATIVE, null, (String) event.getNewValue() + CRLF);
			}
			if (event.getPropertyName().equals(NasaHighSpeedStreamMonitor.Event.LINK.name())) {
				pcs.firePropertyChange(EVENT_NARRATIVE, null, event.getNewValue() + CRLF);
				nasaSpaceWeatherGUI.setHSSEventLink((URL) event.getNewValue());
			}
			if (event.getPropertyName().equals(NasaHighSpeedStreamMonitor.Event.NETWORK_ERROR.name())) {
				pcs.firePropertyChange(EVENT_NARRATIVE, null, "The NASA Server returned HTTP response code 503 for URL "
						+ "https://api.nasa.gov/DONKI/HSS" + CRLF);
			}
			if (event.getPropertyName().equals(NasaHighSpeedStreamMonitor.Event.DATA_FETCH_COMPLETE.name())) {
				nasaSpaceWeatherGUI.getFlag(6).setForeground(nasaMonitor.get(6).getFlagTextColor());
				nasaSpaceWeatherGUI.getFlag(6).setBackground(nasaMonitor.get(6).getFlagColor());
				nasaSpaceWeatherGUI.getFlag(6).setText(nasaMonitor.get(6).getFlagText());
				nasaSpaceWeatherGUI.getFlag(6).setToolTipText(nasaMonitor.get(6).getToolTipText());
			}
			if (event.getPropertyName().equals(AbstractNasaMonitor.Events.MINUTES_ELAPSED.name())) {
				if (style != Style.SINGLE_COLUMN_NO_TIMEOUT_DISPLAY) {
					updateFlagWithTimeAndMinutesRemaining(6, (long) event.getOldValue(), (long) event.getNewValue());			
				}
			}
		});
	}
	
	private void mpcMonitorChangeEvent(final PropertyChangeEvent event) {
		invokeLaterInDispatchThreadIfNeeded(() -> {
			if (event.getPropertyName().equals(NasaMagnetopauseCrossingMonitor.Event.MPC_ID.name())) {
				pcs.firePropertyChange(EVENT_NARRATIVE, null, (String) event.getNewValue() + CRLF);
			}
			if (event.getPropertyName().equals(NasaMagnetopauseCrossingMonitor.Event.EVENT_TIME.name())) {
				pcs.firePropertyChange(EVENT_NARRATIVE, null, "MPC Emmitted on : "
						+ ((ZonedDateTime) event.getNewValue()).format(DateTimeFormatter.ISO_INSTANT) + CRLF);
			}
			if (event.getPropertyName().equals(NasaMagnetopauseCrossingMonitor.Event.NO_EVENTS.name())) {
				pcs.firePropertyChange(EVENT_NARRATIVE, null, (String) event.getNewValue() + CRLF);
			}
			if (event.getPropertyName().equals(NasaMagnetopauseCrossingMonitor.Event.LINK.name())) {
				pcs.firePropertyChange(EVENT_NARRATIVE, null, event.getNewValue() + CRLF);
				nasaSpaceWeatherGUI.setMPCEventLink((URL) event.getNewValue());
			}
			if (event.getPropertyName().equals(NasaMagnetopauseCrossingMonitor.Event.NETWORK_ERROR.name())) {
				pcs.firePropertyChange(EVENT_NARRATIVE, null, (String) event.getNewValue() + CRLF);
			}
			if (event.getPropertyName().equals(NasaMagnetopauseCrossingMonitor.Event.DATA_FETCH_COMPLETE.name())) {
				nasaSpaceWeatherGUI.getFlag(7).setForeground(nasaMonitor.get(7).getFlagTextColor());
				nasaSpaceWeatherGUI.getFlag(7).setBackground(nasaMonitor.get(7).getFlagColor());
				nasaSpaceWeatherGUI.getFlag(7).setText(nasaMonitor.get(7).getFlagText());
				nasaSpaceWeatherGUI.getFlag(7).setToolTipText(nasaMonitor.get(7).getToolTipText());
			}
			if (event.getPropertyName().equals(AbstractNasaMonitor.Events.MINUTES_ELAPSED.name())) {
				if (style != Style.SINGLE_COLUMN_NO_TIMEOUT_DISPLAY) {
					updateFlagWithTimeAndMinutesRemaining(7, (long) event.getOldValue(), (long) event.getNewValue());
				}
			}
		});
	}

	@Override
	public void close() {
		nasaMonitor.stream().filter(aNasaMonitor -> aNasaMonitor != null).forEach(AbstractNasaMonitor::close);
		if (startScheduler != null) {
			try {
				LOG.log(Level.SEVERE, "Initializing AbstractNASAMonitor.secondScheduler service termination....");
				startScheduler.shutdownNow();
				startScheduler.awaitTermination(TERMINATE_TIMEOUT, TimeUnit.SECONDS);
				LOG.log(Level.SEVERE, "NASASpaceWeatherProcessor.startScheduler service has gracefully terminated");
			} catch (InterruptedException e) {
				LOG.log(Level.SEVERE, "NASASpaceWeatherProcessor.startScheduler service has timed out after {0} seconds of waiting to terminate processes.", TERMINATE_TIMEOUT);
				Thread.currentThread().interrupt();
			}
		}
	}
	
	public PropertyChangeSupport getPropertyChangeSupport() {
		return pcs;
	}
	
	public NASASpaceWeatherGUI getNASASpaceWeatherGUI() {
		return nasaSpaceWeatherGUI;
	}
	
	private void initializeShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                close();
            }
        });
    }
	
	private static void invokeLaterInDispatchThreadIfNeeded(Runnable runnable) {
		if (EventQueue.isDispatchThread()) {
			runnable.run();
		} else {
			SwingUtilities.invokeLater(runnable);
		}
	}
}
