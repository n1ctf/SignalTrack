package gov.nasa.api.donki;

import java.awt.Color;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import java.time.temporal.ChronoUnit;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import time.ConsolidatedTime;

public abstract class AbstractNasaMonitor implements AutoCloseable {

    public static final String DEFAULT_API_KEY = "NZtm8vzNM9fbXcwyiHamGLuM8f1yw3guyXXscOqW";
    public static final int TERMINATE_TIMEOUT = 10;  // seconds
    public static final String DEMO_API_KEY = "DEMO_KEY";
    public static final boolean DEFAULT_DEBUG_MODE = true;
    
    public enum Events {
    	SECONDS_TO_NEXT_UPDATE,
    	MINUTES_ELAPSED,
    	MINUTES_ELAPSED_TIMEOUT
    }
    
    protected final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    private final ScheduledExecutorService oneSecondTimerScheduler = Executors.newSingleThreadScheduledExecutor();

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    
    private final String apiKey;
    private final String apiHeader;
    
    private boolean debug;
    private boolean enabled;
    private long checkPeriodSeconds;
    private long checkPeriodSecondsRemaining;
    private int secondCounter;
    
    private long minutesElapsed = -1;
    protected long persistenceMinutes;
    
    protected AbstractNasaMonitor(String apiHeader, String apiKey) {
        this(apiHeader, apiKey, DEFAULT_DEBUG_MODE);
    }
    
    protected AbstractNasaMonitor(String apiHeader, String apiKey, boolean debug) {
        this.apiHeader = apiHeader;
        this.apiKey = apiKey;
        this.debug = debug; 
        
        Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				close();
			}
		});
    }  
    
    public abstract ZonedDateTime getActivityTime();
    
    public abstract Runnable update(String urlGroup);

    public abstract ZonedDateTime getStartTime();
    
    public abstract String getClassDescriptorString();
    
    public abstract String getFlagText();

    public abstract String getNarrativeText();
    
    public abstract Color getFlagColor();
    
    public abstract Color getFlagTextColor();

    public abstract Logger getLogger();

    public abstract String getAPIHeader();

    public abstract String getTestURLGroup();
    
    public abstract long getDefaultPersistencePeriodMinutes();
    
    public abstract long getDefaultApiQueryPeriodSeconds();
    
    public abstract void setNoEvents(boolean noEvents);
    
    public abstract String getToolTipText();

    public boolean isDebug() {
        return debug;
    }

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public boolean isEnabled() {
		return enabled;
	}
	
	public void start() {
		oneSecondTimerScheduler.scheduleAtFixedRate(new OneSecondCounter(), 1, 1, TimeUnit.SECONDS);
	}
	
	private final class OneSecondCounter implements Runnable {
		@Override
		public void run() {
			if (checkPeriodSecondsRemaining == 0) {
				checkPeriodSecondsRemaining = checkPeriodSeconds;
				if (isEnabled()) {
					executor.execute(update(getURLGroup(apiHeader, getCurrentUTCMinusPersistenceMinutes(), getCurrentUTC())));
				}
			}
			if (isEnabled()) {
				checkPeriodSecondsRemaining--;
				if (getActivityTime() != null && getAgeOfEventInMinutes() < getPersistenceMinutes()) {
					if (secondCounter == 0) {
						minutesElapsed++;
						secondCounter = 60;
					}
					secondCounter--;
				} else {
					minutesElapsed = -1;
				}
				pcs.firePropertyChange(Events.MINUTES_ELAPSED.name(), checkPeriodSecondsRemaining, minutesElapsed);
			}
		}
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		if (debug) {
            getLogger().log(Level.INFO, "NASA DONKI API Update Scheduler for {0} has been {1}", 
            		new Object[] {getClassDescriptorString(), enabled ? "ENABLED" : "DISABLED"});
        }
		final boolean condition = !enabled && oneSecondTimerScheduler != null;
		if (condition) {
			try {
				getLogger().log(Level.INFO, "Initializing AbstractNasaMonitor.oneSecondTimerScheduler service termination....");
				oneSecondTimerScheduler.shutdown();
				oneSecondTimerScheduler.awaitTermination(2, TimeUnit.SECONDS);
				getLogger().log(Level.INFO, "AbstractNasaMonitor.oneSecondTimerScheduler service has gracefully terminated");
			} catch (InterruptedException _) {
				oneSecondTimerScheduler.shutdownNow();
				getLogger().log(Level.SEVERE, "AbstractNasaMonitor.oneSecondTimerScheduler service has timed out after 2 seconds of waiting to terminate processes.");
				Thread.currentThread().interrupt();
			}
		}
	}

	public ZonedDateTime getCurrentUTC() {
        return ZonedDateTime.ofInstant(Instant.now(), ZoneId.of("UTC"));
    }

	public ZonedDateTime getRecheckTime(long futureSeconds) {
		return getCurrentUTC().plusSeconds(futureSeconds);
	}
	
    protected ZonedDateTime getCurrentUTCMinus24H() {
    	final Instant nowUtc = Instant.now();
    	final Instant nowMinus24H = nowUtc.minus(24, ChronoUnit.HOURS);
    	final ZoneId utc = ZoneId.of("UTC");
        return ZonedDateTime.ofInstant(nowMinus24H, utc);
    }
    
    protected ZonedDateTime getCurrentUTCMinusPersistenceMinutes() {
    	final Instant nowUtc = Instant.now();
    	final Instant nowMinusPersistenceMinutes = nowUtc.minus(persistenceMinutes, ChronoUnit.MINUTES);
    	final ZoneId utc = ZoneId.of("UTC");
        return ZonedDateTime.ofInstant(nowMinusPersistenceMinutes, utc);
    }

    public static ZonedDateTime fromNasaDateTimeGroup(String nasaDateTimeGroup) {
        return ConsolidatedTime.fromNasaDateTimeGroup(nasaDateTimeGroup);
    }
    
    protected long getAgeOfEventInMinutes() {
    	if (getActivityTime() == null) {
    		return -1;
    	} else {
    		return Math.abs(ChronoUnit.MINUTES.between(getActivityTime(), getCurrentUTC()));
    	}
    }

    public long getMinutesElapsed() {
    	return minutesElapsed;
    }
    
    public long getPersistenceMinutes() {
    	return persistenceMinutes;
    }
    
	public void setPersistenceMinutes(long persistenceMinutes) {
		this.persistenceMinutes = persistenceMinutes;
	}
	
	public long getCheckPeriodSeconds() {
		return checkPeriodSeconds;
	}

	public long getCheckPeriodSecondsRemaining() {
		return checkPeriodSecondsRemaining;
	}
	
	public void setCheckPeriodSeconds(long checkPeriodSeconds) {
		this.checkPeriodSeconds = checkPeriodSeconds;
	}

	@Override
    public void close() {
		setEnabled(false);
		if (executor != null) {
			try {
				getLogger().log(Level.INFO, "Initializing AbstractNasaMonitor.executor service termination....");
				executor.shutdown();
				executor.awaitTermination(2, TimeUnit.SECONDS);
				getLogger().log(Level.INFO, "AbstractNasaMonitor.executor service has gracefully terminated");
			} catch (InterruptedException _) {
				executor.shutdownNow();
				getLogger().log(Level.SEVERE, "AbstractNasaMonitor.executor service has timed out after 2 seconds of waiting to terminate processes.");
				Thread.currentThread().interrupt();
			}
		}
    }
	
    public void addPropertyChangeListener(final PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(final PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public final String getURLGroup(String urlString, ZonedDateTime startDate, ZonedDateTime endDate) {
    	final String str = urlString
                + "startDate="
                + startDate.getYear() + "-" + startDate.getMonthValue() + "-" + startDate.getDayOfMonth()
                + "&endDate="
                + endDate.getYear() + "-" + endDate.getMonthValue() + "-" + endDate.getDayOfMonth()
                + "&api_key="
                + apiKey;
        if (debug) {
            getLogger().log(Level.INFO, str);
        }
        return str;
    }
    
    public String getLongestOccurenceOf(String element, JSONArray jsonArray) {
		String str = "";
		for (int i = 0; i < jsonArray.length(); i++) {
			final JSONObject obj = (JSONObject) jsonArray.get(i);
			if (obj.getString(element).length() > str.length()) {
				str = obj.getString(element);
			}
		}
		return str;
	}

}
