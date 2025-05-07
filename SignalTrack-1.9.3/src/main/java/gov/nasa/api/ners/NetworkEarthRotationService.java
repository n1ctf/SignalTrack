package gov.nasa.api.ners;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.io.IOException;

import java.net.URI;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.nio.channels.UnresolvedAddressException;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import java.time.format.DateTimeFormatter;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.prefs.Preferences;

import java.util.regex.Pattern;

public final class NetworkEarthRotationService implements AutoCloseable {

	public static final double SOLAR_DAY_SECONDS = 86400;
	public static final double GRAVITATIONAL_CONSTANT = 6.647E-11;
	public static final double SOLAR_MASS_KG = 1.98892E30;
	public static final double GAUSSIAN_GRAVITATIONAL_CONSTANT = 0.017202098950000; // radians/day
	public static final String NERS_RESPONSE_ERROR = "ERROR:";

	private static final long DEFAULT_UPDATE_PERIOD_MILLIS = 120000; // Interrogate NERS server every 2 minutes
	
	private static final String SPACE_PATTERN = "\\s+";

	private static final Logger LOG = Logger.getLogger(NetworkEarthRotationService.class.getName());
	
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(NetworkEarthRotationService.class);
	
	private final Preferences userPref = Preferences.userRoot().node(getClass().getName());
		
	private final ScheduledExecutorService queryScheduler = Executors.newSingleThreadScheduledExecutor();
	private final ScheduledExecutorService updateScheduler = Executors.newSingleThreadScheduledExecutor();
	
	private long taiInstantReportedNanos = 0;
	private long utcInstantReportedNanos = 0;
	
	private LocalDateTime taiTime = null;
	private LocalDateTime ut1Time = null;
	private LocalDateTime utcTime = null;
	
	private double lodOffsetSeconds;
	private double xPoleCoordArcSec;
	private double yPoleCoordArcSec;
	private double xPoleCoordArcSecPerDay;
	private double yPoleCoordArcSecPerDay;
	
	private boolean nersFail = true;

    public enum Event {
    	TAI_TIME,
    	UT1_TIME,
    	UTC_TIME,
    	UT1_FAIL,
    	TAI_PRECISION,
    	UT1_PRECISION,
    	UTC_PRECISION,
    	LOD_OFFSET_SECONDS,
    	X_POLE_COORDINATE_ARC_SECONDS,
    	Y_POLE_COORDINATE_ARC_SECONDS,
    	X_POLE_COORDINATE_ARC_SECONDS_PER_DAY,
    	Y_POLE_COORDINATE_ARC_SECONDS_PER_DAY,
    }

    public enum Precision {
    	HIGH,
    	MEDIUM,
    	LOW,
    	UNSYNC,
    	NERS_FAIL
    }
    
    public NetworkEarthRotationService() {
    	
    	Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				close();
			}
		});

    	final Long savedSystemMillis = userPref.getLong("systemMillis", 0);
    	final Long millisToAdd = System.currentTimeMillis() - savedSystemMillis;
    	
    	final LocalDateTime savedTAI = LocalDateTime.ofEpochSecond(userPref.getLong("taiEpochSeconds", 0L), 0, ZoneOffset.UTC);
    	taiTime = savedTAI.plusNanos((long) (millisToAdd * 1E6));
    	
    	final LocalDateTime savedUT1 = LocalDateTime.ofEpochSecond(userPref.getLong("ut1EpochSeconds", 0L), 0, ZoneOffset.UTC);
    	ut1Time = savedUT1.plusNanos((long) (millisToAdd * 1E6));
    	
    	final LocalDateTime savedUTC = LocalDateTime.ofEpochSecond(userPref.getLong("utcEpochSeconds", 0L), 0, ZoneOffset.UTC);
    	utcTime = savedUTC.plusNanos((long) (millisToAdd * 1E6));
    	
    	queryScheduler.scheduleAtFixedRate(new Query(), 100, DEFAULT_UPDATE_PERIOD_MILLIS, TimeUnit.MILLISECONDS);
    	updateScheduler.scheduleAtFixedRate(new Update(), 1100, 1000, TimeUnit.MILLISECONDS);
    }

	public LocalDateTime getAdjustedTAITime() {
		return taiTime.plusNanos((long) (System.currentTimeMillis() * 1E6) - taiInstantReportedNanos);
	}

	public Precision getTAIPrecision() {
		LOG.log(Level.INFO, "System.nanoTime(): {0}, taiInstantReportedNanos: {1}, timeElapsedSeconds: {2}", 
			new Object[] {
				String.valueOf(System.nanoTime()),
				String.valueOf(taiInstantReportedNanos),
				String.valueOf((System.nanoTime() - taiInstantReportedNanos) / 1E9)
			}
		);
		return getPrecision(System.nanoTime() - taiInstantReportedNanos);
	}
	
	public LocalDateTime getAdjustedUT1Time() {
		return ut1Time.plusNanos(System.nanoTime() - taiInstantReportedNanos);
	}
	
	public Precision getUT1Precision() {
		return getPrecision(System.nanoTime() - taiInstantReportedNanos);
	}
	
	public LocalDateTime getAdjustedUTCTime() {
		return utcTime.plusNanos(System.nanoTime() - utcInstantReportedNanos);
	}
	public Precision getUTCPrecision() {
		return getPrecision(System.nanoTime() - utcInstantReportedNanos);
	}
	
	public Double getLODOffsetSeconds() {
		return lodOffsetSeconds;
	}

	public Double getDistanceFromEarthToSun() {
		return Math.cbrt(GRAVITATIONAL_CONSTANT * SOLAR_MASS_KG * Math.pow((SOLAR_DAY_SECONDS + lodOffsetSeconds), 2)
				/ Math.pow(GAUSSIAN_GRAVITATIONAL_CONSTANT, 2));
	}
	
	public Double getXPoleCoordinateArcSeconds() {
		return xPoleCoordArcSec;
	}

	public Double getYPoleCoordinateArcSeconds() {
		return yPoleCoordArcSec;
	}

	public Double getXPoleCoordinateArcSecondsPerDay() {
		return xPoleCoordArcSecPerDay;
	}

	public Double getYPoleCoordinateArcSecondsPerDay() {
		return yPoleCoordArcSecPerDay;
	}

	private class Update implements Runnable {
		@Override
		public void run() {
			pcs.firePropertyChange(Event.TAI_TIME.name(), null, getAdjustedTAITime());
			pcs.firePropertyChange(Event.TAI_PRECISION.name(), null, getTAIPrecision());
			
			pcs.firePropertyChange(Event.UT1_TIME.name(), null, getAdjustedUT1Time());
			pcs.firePropertyChange(Event.UT1_PRECISION.name(), null, getUT1Precision());
			
			pcs.firePropertyChange(Event.UTC_TIME.name(), null, getAdjustedUTCTime());
			pcs.firePropertyChange(Event.UTC_PRECISION.name(), null, getUTCPrecision());

			pcs.firePropertyChange(Event.LOD_OFFSET_SECONDS.name(), null, getLODOffsetSeconds());
			
			pcs.firePropertyChange(Event.X_POLE_COORDINATE_ARC_SECONDS.name(), null, getXPoleCoordinateArcSeconds());
			
			pcs.firePropertyChange(Event.Y_POLE_COORDINATE_ARC_SECONDS.name(), null, getYPoleCoordinateArcSeconds());

			pcs.firePropertyChange(Event.X_POLE_COORDINATE_ARC_SECONDS_PER_DAY.name(), null, getXPoleCoordinateArcSecondsPerDay());

			pcs.firePropertyChange(Event.Y_POLE_COORDINATE_ARC_SECONDS_PER_DAY.name(), null, getYPoleCoordinateArcSecondsPerDay());
		}
	}
	
	private class Query implements Runnable {
		@Override
		public void run() {
			runTimeStandardUpdate();
			runUTCUpdate();
			runLODOffsetUpdate();
			runXPoleCoordArcSecUpdate();
			runYPoleCoordArcSecUpdate();
			runXPoleCoordArcSecPerDayUpdate();
			runYPoleCoordArcSecPerDayUpdate();
		}
		
		private void runTimeStandardUpdate() {
			double ut1mtaiSeconds = 0;
			final HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create("https://earthrotation.smce.nasa.gov/cgi-bin/eop_online.py?req_date=now&param=ut1mtai")).build();
			try (HttpClient client = HttpClient.newHttpClient()) {
				final long requestTime = System.nanoTime();
				final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
				final Pattern pt = Pattern.compile(SPACE_PATTERN, Pattern.UNICODE_CHARACTER_CLASS);
				final String[] words = pt.split(response.body());
				if (words.length >= 3 && "s".equals(words[3])) {
					ut1mtaiSeconds = Double.parseDouble(words[2].replace("D", "E"));
					nersFail = false;	
					final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd-HH:mm:ss.SSSSSSS");
					final LocalDateTime taiInstant = LocalDateTime.parse(words[1].strip(), formatter);
					taiInstantReportedNanos = System.nanoTime();
					final long propagationNanos = Math.round((taiInstantReportedNanos - requestTime) / 2.0);  // half of round trip time
					LOG.log(Level.INFO, "NERS UT1 server->client network propagation time: {0} seconds", propagationNanos / 1E9D);
					taiTime = taiInstant.plusNanos(propagationNanos);
					ut1Time = taiTime.plusNanos((long) (ut1mtaiSeconds * 1E9) + (System.nanoTime() - taiInstantReportedNanos));
					pcs.firePropertyChange(Event.TAI_TIME.name(), null, taiTime);
					pcs.firePropertyChange(Event.UT1_TIME.name(), null, ut1Time);
				} else {
					nersFail = true;
					LOG.log(Level.INFO, "NERS UT1mTAI Service Temporarily Unavailable at {0}", utcTime);
					pcs.firePropertyChange(Event.UT1_FAIL.name(), null, null);
				}
			} catch (IOException e) {
				LOG.log(Level.SEVERE, e.getLocalizedMessage(), e);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				LOG.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}  
		}
		
		private void runUTCUpdate() {
			Double utcmtaiSeconds = null;
			final HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create("https://earthrotation.smce.nasa.gov/cgi-bin/eop_online.py?req_date=now&param=utcmtai")).build();
			final long requestTime = System.nanoTime();
			try (HttpClient client = HttpClient.newHttpClient()) {
				final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
				utcInstantReportedNanos = System.nanoTime();
				final Pattern pt = Pattern.compile(SPACE_PATTERN, Pattern.UNICODE_CHARACTER_CLASS);
				final String[] words = pt.split(response.body());
				utcmtaiSeconds = Double.parseDouble(words[2].replace("D", "E"));
				final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd-HH:mm:ss.SSSSSSS");
				final LocalDateTime taiInstant = LocalDateTime.parse(words[1].strip(), formatter);
				final long propagationNanos = Math.round((utcInstantReportedNanos - requestTime) / 2.0);
				LOG.log(Level.INFO, "NERS UTC server->client network propagation time: {0} seconds", propagationNanos / 1E9D);
				final LocalDateTime tai = taiInstant.plusNanos(propagationNanos);
				utcTime = tai.plusNanos((long) (utcmtaiSeconds * 1E9) + System.nanoTime() - utcInstantReportedNanos);
				pcs.firePropertyChange(Event.UTC_TIME.name(), null, utcTime);
			} catch (IOException | UnresolvedAddressException e) {
				LOG.log(Level.WARNING, e.getLocalizedMessage(), e);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				LOG.log(Level.WARNING, e.getLocalizedMessage(), e);
			} 
		}
	
		private void runLODOffsetUpdate() {
			final HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create("https://earthrotation.smce.nasa.gov/cgi-bin/eop_online.py?req_date=now&param=lod")).build();
			try (HttpClient client = HttpClient.newHttpClient()) {
				final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
				final Pattern pt = Pattern.compile(SPACE_PATTERN, Pattern.UNICODE_CHARACTER_CLASS);
				final String[] words = pt.split(response.body());
				lodOffsetSeconds = Double.parseDouble(words[2].replace("D", "E"));
				pcs.firePropertyChange(Event.LOD_OFFSET_SECONDS.name(), null, lodOffsetSeconds);
			} catch (IOException e) {
				LOG.log(Level.SEVERE, e.getLocalizedMessage(), e);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				LOG.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}	
		
		private void runXPoleCoordArcSecUpdate() {
			final HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create("https://earthrotation.smce.nasa.gov/cgi-bin/eop_online.py?req_date=now&param=xpol")).build();
			try (HttpClient client = HttpClient.newHttpClient()) {
				final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
				final Pattern pt = Pattern.compile(SPACE_PATTERN, Pattern.UNICODE_CHARACTER_CLASS);
				final String[] words = pt.split(response.body());
				xPoleCoordArcSec = Double.parseDouble(words[2].replace("D", "E"));
				pcs.firePropertyChange(Event.X_POLE_COORDINATE_ARC_SECONDS.name(), null, xPoleCoordArcSec);
			} catch (IOException e) {
				LOG.log(Level.SEVERE, e.getLocalizedMessage(), e);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				LOG.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
		
		private void runYPoleCoordArcSecUpdate() {
			final HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create("https://earthrotation.smce.nasa.gov/cgi-bin/eop_online.py?req_date=now&param=ypol")).build();
			try (HttpClient client = HttpClient.newHttpClient()) {
				final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
				final Pattern pt = Pattern.compile(SPACE_PATTERN, Pattern.UNICODE_CHARACTER_CLASS);
				final String[] words = pt.split(response.body());
				yPoleCoordArcSec = Double.parseDouble(words[2].replace("D", "E"));
				pcs.firePropertyChange(Event.Y_POLE_COORDINATE_ARC_SECONDS.name(), null, yPoleCoordArcSec);
			} catch (IOException e) {
				LOG.log(Level.SEVERE, e.getLocalizedMessage(), e);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				LOG.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
		
		private void runXPoleCoordArcSecPerDayUpdate() {
			final HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create("https://earthrotation.smce.nasa.gov/cgi-bin/eop_online.py?req_date=now&param=xpolr")).build();
			try (HttpClient client = HttpClient.newHttpClient()) {
				final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
				final Pattern pt = Pattern.compile(SPACE_PATTERN, Pattern.UNICODE_CHARACTER_CLASS);
				final String[] words = pt.split(response.body());
				xPoleCoordArcSecPerDay = Double.parseDouble(words[2].replace("D", "E"));
				pcs.firePropertyChange(Event.X_POLE_COORDINATE_ARC_SECONDS_PER_DAY.name(), null, xPoleCoordArcSecPerDay);
			} catch (IOException e) {
				LOG.log(Level.SEVERE, e.getLocalizedMessage(), e);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				LOG.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
		
		private void runYPoleCoordArcSecPerDayUpdate() {
			final HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create("https://earthrotation.smce.nasa.gov/cgi-bin/eop_online.py?req_date=now&param=ypolr")).build();
			try (HttpClient client = HttpClient.newHttpClient()) {
				final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
				final Pattern pt = Pattern.compile(SPACE_PATTERN, Pattern.UNICODE_CHARACTER_CLASS);
				final String[] words = pt.split(response.body());
				yPoleCoordArcSecPerDay = Double.parseDouble(words[2].replace("D", "E"));
				pcs.firePropertyChange(Event.Y_POLE_COORDINATE_ARC_SECONDS_PER_DAY.name(), null, yPoleCoordArcSecPerDay);
			} catch (IOException e) {
				LOG.log(Level.SEVERE, e.getLocalizedMessage(), e);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				LOG.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
	}
	
	private Precision getPrecision(long ageInNanos) {
		if (nersFail) {
			return Precision.NERS_FAIL;
		} else if (ageInNanos < 120000000000L) {
			return Precision.HIGH; 	// 2 Minutes
		} else if (ageInNanos < 600000000000L) {
			return Precision.MEDIUM; 	// 10 Minutes
		} else if (ageInNanos < 1800000000000L) {
			return Precision.LOW; 	// 30 Minutes
		} else {
			return Precision.UNSYNC;
		}
	}
	
    public PropertyChangeSupport getPropertyChangeSupport() {
    	return pcs;
    }
    
    @Override
    public void close() {
    	userPref.putLong("systemMillis", System.currentTimeMillis());
    	userPref.putLong("taiEpochSeconds", taiTime.toEpochSecond(ZoneOffset.UTC));
    	userPref.putLong("ut1EpochSeconds", ut1Time.toEpochSecond(ZoneOffset.UTC));
    	userPref.putLong("utcEpochSeconds", utcTime.toEpochSecond(ZoneOffset.UTC));
    	
    	if (queryScheduler != null) {
			try {
				LOG.log(Level.INFO, "Initializing NetworkEarthRotationService.queryScheduler service termination....");
				queryScheduler.shutdown();
				queryScheduler.awaitTermination(20, TimeUnit.SECONDS);
				LOG.log(Level.INFO, "NetworkEarthRotationService.queryScheduler service has gracefully terminated");
			} catch (InterruptedException e) {
				LOG.log(Level.SEVERE, "NetworkEarthRotationService.queryScheduler service has timed out after 20 seconds of waiting to terminate processes.");
				Thread.currentThread().interrupt();
			}
		}
    	
    	if (updateScheduler != null) {
			try {
				LOG.log(Level.INFO, "Initializing NetworkEarthRotationService.updateScheduler service termination....");
				updateScheduler.shutdown();
				updateScheduler.awaitTermination(20, TimeUnit.SECONDS);
				LOG.log(Level.INFO, "NetworkEarthRotationService.updateScheduler service has gracefully terminated");
			} catch (InterruptedException e) {
				LOG.log(Level.SEVERE, "NetworkEarthRotationService.updateScheduler service has timed out after 20 seconds of waiting to terminate processes.");
				Thread.currentThread().interrupt();
			}
		}
    	
    	for (PropertyChangeListener pcl : pcs.getPropertyChangeListeners()) {
			pcs.removePropertyChangeListener(pcl);
		}
    }
    
}
