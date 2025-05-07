package aprs;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import java.util.concurrent.atomic.AtomicBoolean;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JPanel;

import meteorology.AbstractEnvironmentSensor;

public abstract class AbstractAPRSProcessor implements AutoCloseable {
	
	public enum Event {
		REPORT_SENTENCE,
		TIME_OF_NEXT_UPDATE,
		TIME_OF_LAST_UPDATE,
		HTML_STRING,
		TX_UPDATE,
		TX_ERROR,
		RX_UPDATE,
		RX_ERROR,
		HTTP_ERROR,
		REPORT_ENABLE,
		NMEA_DATA,
		GPWPL_WAYPOINT_REPORT,
		PKWDWPL_APRS_REPORT,
		CRC_ERROR,
		CLASS_NAME,
		RX_DATA,
		TX_DATA,
		CONFIG_ERROR,
		ERROR,
		INVALID_ADDRESS,
		DSR,
		CTS,
		RLSD,
		ONLINE
	}
	
	public static final long DEFAULT_REPORT_INITIAL_WAIT_SECONDS = 30;  // seconds
	public static final long DEFAULT_REPORT_UPDATE_RATE_SECONDS = 600; // seconds
	
	public static final long DEFAULT_SERVICE_TERMINATION_WAIT_SECONDS = 2; // seconds
	public static final long DEFAULT_WEATHER_DATA_SECONDS_TO_EXPIRE = 300; // seconds 
	
	public static final Logger LOG = Logger.getLogger(AbstractAPRSProcessor.class.getName());

	protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	
	private final AtomicBoolean serviceRunning = new AtomicBoolean(false);
	private final AbstractEnvironmentSensor environmentSensor;
	
	private PropertyChangeListener environmentSensorPropertyChangeListener;
	private ScheduledExecutorService reportScheduler;
	
	private long reportInitialWaitSeconds = DEFAULT_REPORT_INITIAL_WAIT_SECONDS;
	private long reportUpdateRateSeconds = DEFAULT_REPORT_UPDATE_RATE_SECONDS;
	private long serviceTerminationWaitSeconds = DEFAULT_SERVICE_TERMINATION_WAIT_SECONDS;
	private static final long weatherDataSecondsToExpire = DEFAULT_WEATHER_DATA_SECONDS_TO_EXPIRE;
	
	private boolean reportEnable;
	
	private ZonedDateTime timeOfLastUpdate;
	private ZonedDateTime timeOfLastWeatherReport;

	public abstract String getClassName();
	public abstract String getTag();
	public abstract String getAbbreviatedTag();
	public abstract boolean sendUpdate();
	public abstract String getUpdateString();
	public abstract String getHTMLString();
	public abstract JPanel getSettingsPanel();
	
	protected AbstractAPRSProcessor(AbstractEnvironmentSensor environmentSensor) {
		this.environmentSensor = environmentSensor;
		environmentSensor.addPropertyChangeListener(environmentSensorPropertyChangeListener);
		initializeListeners();
	}
	
	public static String toISO8601(ZonedDateTime zonedDateTime) {
		final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSz");
        return zonedDateTime.format(formatter);
    }
	
	public static String toDecimalFormat(double value, int decimalPlaces) {
		final DecimalFormat df = new DecimalFormat();
		df.setGroupingUsed(false);
		df.setMaximumFractionDigits(decimalPlaces);
		df.setMinimumFractionDigits(decimalPlaces);
		df.setRoundingMode(RoundingMode.HALF_UP);
		return df.format(value);
	}
	
	public AbstractEnvironmentSensor getEnvironmentSensor() {
		return environmentSensor;
	}
	
	public boolean isReportEnabled() {
		return reportEnable;
	}
	
	public PropertyChangeSupport getPropertyChangeSupport() {
		return pcs;
	}
	
	public void setReportInitialWaitSeconds(long reportInitialWaitSeconds) {
		this.reportInitialWaitSeconds = reportInitialWaitSeconds;
	}
	
	public void setReportUpdateRateSeconds(long reportUpdateRateSeconds) {
		this.reportUpdateRateSeconds = reportUpdateRateSeconds;
	}
	
	public void setServiceTerminationWaitSeconds(long serviceTerminationWaitSeconds) {
		this.serviceTerminationWaitSeconds = serviceTerminationWaitSeconds;
	}
	
	private void initializeListeners() {
		environmentSensorPropertyChangeListener = event -> {
			if (AbstractEnvironmentSensor.Events.DATA_COMPLETE.name().equals(event.getPropertyName())) {
				timeOfLastWeatherReport = environmentSensor.getZonedDateTimeUTC();
			}
		};
	}
	
	private void setReportEnable(boolean reportEnable) {
		if (reportEnable) {
			pcs.addPropertyChangeListener(environmentSensorPropertyChangeListener);
			if (!serviceRunning.get()) {
				reportScheduler = Executors.newSingleThreadScheduledExecutor();
				reportScheduler.scheduleAtFixedRate(new ReportUpdater(), reportInitialWaitSeconds,
					reportUpdateRateSeconds, TimeUnit.SECONDS);
				serviceRunning.set(true);
			}
		} else {
			
			if (reportScheduler != null) {
				try {
					LOG.log(Level.INFO, "Initializing AbstractAPRSOperator.ReportScheduler termination....");
					reportScheduler.shutdown();
					reportScheduler.awaitTermination(serviceTerminationWaitSeconds, TimeUnit.SECONDS);
					serviceRunning.set(false);
					LOG.log(Level.INFO, "AbstractAPRSOperator.ReportScheduler has gracefully terminated");
				} catch (InterruptedException e) {
					reportScheduler.shutdownNow();
					LOG.log(Level.SEVERE, "AbstractAPRSOperator.ReportScheduler has timed out after {0} seconds of waiting to terminate processes.", serviceTerminationWaitSeconds);
					Thread.currentThread().interrupt();
				}
			}
		}
		pcs.firePropertyChange(Event.REPORT_ENABLE.name(), this.reportEnable, reportEnable);
		this.reportEnable = reportEnable;
		LOG.log(Level.INFO, "Periodic reporting has been {0}" , reportEnable ? "ENABLED" : "DISABLED");
	}
	
	public ZonedDateTime getTimeOfNextUpdate() {
		return timeOfLastUpdate.plusSeconds(reportUpdateRateSeconds);
	}
	
	public ZonedDateTime getTimeOfLastUpdate() {
		return timeOfLastUpdate;
	}
	
	public boolean isWeatherDataExpired() {
		return timeOfLastWeatherReport == null || timeOfLastWeatherReport.plusSeconds(weatherDataSecondsToExpire).isAfter(environmentSensor.getZonedDateTimeUTC());
	}

	public void runService(boolean run) {
		setReportEnable(run);
	}
	
	@Override
	public void close() {	
		environmentSensor.removePropertyChangeListener(environmentSensorPropertyChangeListener);
	}

	private final class ReportUpdater implements Runnable {
		@Override
		public void run() {
			if (sendUpdate() && reportEnable) {
				timeOfLastUpdate = environmentSensor.getZonedDateTimeUTC();
				pcs.firePropertyChange(Event.TIME_OF_LAST_UPDATE.name(), null, getTimeOfLastUpdate());
				pcs.firePropertyChange(Event.TIME_OF_NEXT_UPDATE.name(), null, getTimeOfNextUpdate());
				pcs.firePropertyChange(Event.REPORT_SENTENCE.name(), null, getUpdateString());
				pcs.firePropertyChange(Event.HTML_STRING.name(), null, getHTMLString());
			}
		}
	}

}
