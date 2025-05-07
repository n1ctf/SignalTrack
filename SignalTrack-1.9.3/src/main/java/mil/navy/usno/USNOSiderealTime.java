package mil.navy.usno;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import gov.nasa.api.ners.NetworkEarthRotationService;

import json.JsonReader;

public class USNOSiderealTime implements AutoCloseable {

	public enum Event {
		EQOFEQ, GAST, GMST, LAST, LMST, REFERENCE_LOCAL_DATE_TIME, NO_DATA, NETWORK_ERROR
	}

	private static final long INITIAL_WAIT = 3; // seconds
	private static final long UPDATE_RATE = 60; // seconds
	private static final Logger LOG = Logger.getLogger(USNOSiderealTime.class.getName());

	private static final boolean DEFAULT_DEBUG_MODE = true;
	private static final double DEFAULT_LATITUDE = 40.0;
	private static final double DEFAULT_LONGITUDE = -83.0;

	protected final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	private double equationOfTheEquinoxes;
	
	private LocalTime grenwichApparentSiderealTime;
	private LocalTime grenwichMeanSiderealTime;
	private LocalTime localApparentSiderealTime;
	private LocalTime localMeanSiderealTime;
	private LocalDateTime referenceLocalDateTime;
	
	private boolean noData;
	private boolean debug;

	private double longitude;
	private double latitude;

	private NetworkEarthRotationService ners;
	
	private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
	
	private final Executor executor = Executors.newSingleThreadExecutor();

	public USNOSiderealTime(NetworkEarthRotationService ners) {
		this(ners, DEFAULT_LATITUDE, DEFAULT_LONGITUDE, DEFAULT_DEBUG_MODE);
	}

	public USNOSiderealTime(NetworkEarthRotationService ners, boolean debug) {
		this(ners, DEFAULT_LATITUDE, DEFAULT_LONGITUDE, debug);
	}

	public USNOSiderealTime(NetworkEarthRotationService ners, double latitude, double longitude, boolean debug) {
		this.debug = debug;
		this.latitude = latitude;
		this.longitude = longitude;
		this.ners = ners;
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				close();
			}
		});
		
	}

	public LocalDateTime getReferenceLocalDateTime() {
		return referenceLocalDateTime;
	}

	public double getEquationOfTheEquinoxes() {
		return equationOfTheEquinoxes;
	}

	public LocalTime getGrenwichApparentSiderealTime() {
		return grenwichApparentSiderealTime;
	}

	public LocalTime getGrenwichMeanSiderealTime() {
		return grenwichMeanSiderealTime;
	}

	public LocalTime getLocalApparentSiderealTime() {
		return localApparentSiderealTime;
	}

	public LocalTime getLocalMeanSiderealTime() {
		return localMeanSiderealTime;
	}

	public boolean isDebug() {
		return debug;
	}

	public double getLongitude() {
		return longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	@Override
    public void close() {
		for (PropertyChangeListener pcl : pcs.getPropertyChangeListeners()) {
			pcs.removePropertyChangeListener(pcl);
		}
		if (scheduler != null) {
			try {
				LOG.log(Level.SEVERE, "Initializing USNO Sidereal Time Service termination....");
				scheduler.shutdown();
				scheduler.awaitTermination(3, TimeUnit.SECONDS);
				LOG.log(Level.SEVERE, "USNO Sidereal Time Service has gracefully terminated");
			} catch (InterruptedException e) {
				LOG.log(Level.SEVERE, "USNO Sidereal Time Service has timed out after 3 seconds of waiting to terminate processes.");
				Thread.currentThread().interrupt();
			}
		}
    }
	
	public void requestSiderealTime() {
		executor.execute(new Update(ners));
	}

	public void startAutoUpdate() {
		scheduler.scheduleAtFixedRate(new Update(ners), INITIAL_WAIT, UPDATE_RATE, TimeUnit.SECONDS);
	}

	public void stopAutoUpdate() {
		scheduler.shutdown();
	}

	public void addPropertyChangeListener(final PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(final PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}

	private final class Update implements Runnable {

		private int day;
		private int month;
		private int year;
		private String refTime;
		private final NetworkEarthRotationService ners;
		
		private Update(NetworkEarthRotationService ners) {
			this.ners = ners;
		}

        @Override
		public void run() {
            String jsonString = null;	
			try (JsonReader jsonReader = new JsonReader(isDebug())) {
				final String urlGroup = getURLGroup(ners.getAdjustedUT1Time());
				jsonString = jsonReader.readJsonFromUrl(urlGroup);
				if (jsonString.length() > 2) {

					final JSONObject jsonObject = new JSONObject(jsonString);
					final JSONObject propertiesObject = (JSONObject) jsonObject.get("properties");
					final JSONArray dataElement = propertiesObject.getJSONArray("data");

					final JSONObject data = (JSONObject) dataElement.get(0);

					try {
						final double eqofeq = data.getDouble("eqofeq");
						pcs.firePropertyChange(Event.EQOFEQ.name(), equationOfTheEquinoxes, eqofeq);
						equationOfTheEquinoxes = eqofeq;
					} catch (JSONException ex) {
						if (debug) {
							LOG.log(Level.INFO, "**** NO Equation of the Equinoxes is provided ****", ex);
						}
					}
					try {
						final LocalTime time = LocalTime.parse(data.getString("gmst"));
						pcs.firePropertyChange(Event.GMST.name(), grenwichMeanSiderealTime, time);
						grenwichMeanSiderealTime = time;
					} catch (JSONException ex) {
						if (debug) {
							LOG.log(Level.INFO, "**** NO Greenwich Mean Sidereal Time is provided ****", ex);
						}
					}
					try {
						final LocalTime time = LocalTime.parse(data.getString("gast"));
						pcs.firePropertyChange(Event.GMST.name(), grenwichApparentSiderealTime, time);
						grenwichApparentSiderealTime = time;
					} catch (JSONException ex) {
						if (debug) {
							LOG.log(Level.INFO, "**** NO Greenwich Apparent Sidereal Time is provided ****", ex);
						}
					}
					try {
						final LocalTime time = LocalTime.parse(data.getString("lmst"));
						pcs.firePropertyChange(Event.LMST.name(), localMeanSiderealTime, time);
						localMeanSiderealTime = time;
					} catch (JSONException ex) {
						if (debug) {
							LOG.log(Level.INFO, "**** NO Local Mean Sidereal Time is provided ****", ex);
						}
					}
					try {
						final LocalTime time = LocalTime.parse(data.getString("last"));
						pcs.firePropertyChange(Event.LAST.name(), localApparentSiderealTime, time);
						localApparentSiderealTime = time;
					} catch (JSONException ex) {
						if (debug) {
							LOG.log(Level.INFO, "**** NO Local Apparent Sidereal Time is provided ****", ex);
						}
					}
					try {
						refTime = data.getString("ut1time");
					} catch (JSONException ex) {
						if (debug) {
							LOG.log(Level.INFO, "**** NO UT1 Time is provided ****", ex);
						}
					}
					try {
						day = data.getInt("day");
					} catch (JSONException ex) {
						if (debug) {
							LOG.log(Level.INFO, "**** NO DAY is provided ****", ex);
						}
					}
					try {
						month = data.getInt("month");
					} catch (JSONException ex) {
						if (debug) {
							LOG.log(Level.INFO, "**** NO MONTH is provided ****", ex);
						}
					}
					try {
						year = data.getInt("year");
					} catch (JSONException ex) {
						if (debug) {
							LOG.log(Level.INFO, "**** NO YEAR is provided ****", ex);
						}
					}
					referenceLocalDateTime = LocalDateTime.of(LocalDate.of(year, month, day),
							LocalTime.parse(refTime, DateTimeFormatter.ofPattern("HH:mm:ss.S")));

					if (debug) {
						LOG.log(Level.INFO, "{0} {1} {2} {3} {4} {5} {6} {7} {8} {9} {10} {11} {12} {13} {14}, {15}",
							new Object[] {
								"\n------------------------- USNO Sidereal Time -------------------------",
								"\n   Request Date/Time Group (UT1):    ", ners.getAdjustedUT1Time(),
								"\n   Report Date/Time Group (UT1):     ", referenceLocalDateTime,
								"\n   Equation of the Equinoxes:        ", equationOfTheEquinoxes,
								"\n   Greenwich Mean Sidereal Time:     ", grenwichMeanSiderealTime,										
								"\n   Greenwich Apparent Sidereal Time: ", grenwichApparentSiderealTime,
								"\n   Local Mean Sidereal Time:         ", localMeanSiderealTime,
								"\n   Local Apparent Sidereal Time:     ", localApparentSiderealTime,
								"\n----------------------- END USNO Sidereal Time -----------------------" });
					}
				} else {
					final boolean b = true;
					pcs.firePropertyChange(Event.NO_DATA.name(), noData, b);
					noData = b;
					if (debug) {
						LOG.log(Level.INFO, "No Data Returned from the USNO from {0}", urlGroup);
					}
				}

			} catch (JSONException ex) {
				LOG.log(Level.WARNING, null, ex);
				pcs.firePropertyChange(Event.NETWORK_ERROR.name(), null, "Error Retrieving Data from the USNO");
			}
		}

		private String getURLGroup(LocalDateTime localDateTime) {
			final StringBuilder sb = new StringBuilder();

			sb.append("https://aa.usno.navy.mil/api/siderealtime?date=");
			sb.append(localDateTime.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
			sb.append("&coords=");
			sb.append(String.valueOf(getLatitude()));
			sb.append(",");
			sb.append(String.valueOf(getLongitude()));
			sb.append("&reps=1&intv_mag=5&intv_unit=minutes&time=");
			sb.append(localDateTime.toLocalTime().format(DateTimeFormatter.ISO_LOCAL_TIME));

			if (debug) {
				LOG.log(Level.INFO, "Requested URL: {0}", sb);
			}
			
			return sb.toString();
		}
		
	}

}
