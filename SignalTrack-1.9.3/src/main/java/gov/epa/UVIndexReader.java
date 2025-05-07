package gov.epa;

/**
 *
 * @author John
 */
import java.awt.Color;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import json.JsonReader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import static time.ConsolidatedTime.fromEPAEnvirofactsStyleDateGroup;

public class UVIndexReader implements AutoCloseable {

    public enum Event {
        MAX_DAILY_UV_INDEX,
        CITY,
        STATE,
        ZIP_CODE,
        UV_ALERT,
        DATE,
        NO_DATA,
        NETWORK_ERROR
    }

    public static final String DEFAULT_REQUESTED_ZIP_CODE = "43235";
    public static final boolean DEFAULT_DEBUG_MODE = true;
    private static final long INITIAL_WAIT = 3;     // seconds
    private static final long UPDATE_RATE = 120;    // seconds
    private static final Logger LOG = Logger.getLogger(UVIndexReader.class.getName());

    protected final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private final String requestedZipCode;

    private String city;
    private String state;
    private int dailyUvIndex = -1;
    private String uvAlert;
    private String dateString;
    private String repliedZipCode;
    private boolean noData;
    private boolean debug;
    private final List<UVIndexGroup> hourlyUvIndexGroupList = new ArrayList<>(20);
    private ScheduledExecutorService scheduler;

    public UVIndexReader() {
        this(DEFAULT_REQUESTED_ZIP_CODE, DEFAULT_DEBUG_MODE);
    }

    public UVIndexReader(boolean debug) {
        this(DEFAULT_REQUESTED_ZIP_CODE, debug);
    }

    public UVIndexReader(String requestedZipCode, boolean debug) {
        this.requestedZipCode = requestedZipCode;
        this.debug = debug;
        
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                close();
            }
        });
        
        scheduler = Executors.newScheduledThreadPool(1);
        startUpdateTimer();
    }

    public int getDailyUvIndex() {
        return dailyUvIndex;
    }

    public String getRepliedZipCode() {
        return repliedZipCode;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getUvAlert() {
        return uvAlert;
    }

    public ZonedDateTime getZonedDateTime() {
        return fromEPAEnvirofactsStyleDateGroup(dateString);
    }

    public static final Color getUvIndexWarningColor(final int index) {
        if (index >= 0 && index <= 2) {
            return Color.GREEN;
        }
        if (index >= 3 && index <= 5) {
            return Color.YELLOW;
        }
        if (index >= 6 && index <= 7) {
            return Color.ORANGE;
        }
        if (index >= 8 && index <= 10) {
            return Color.RED;
        }
        return index >= 11 ? Color.MAGENTA : Color.WHITE;
    }

    public static final String getUvIndexWarningText(final int index) {
        if (index >= 0 && index <= 2) {
            return "LOW";
        }
        if (index >= 3 && index <= 5) {
            return "MEDIUM";
        }
        if (index >= 6 && index <= 7) {
            return "HIGH";
        }
        if (index >= 8 && index <= 10) {
            return "VERY HIGH";
        }
        return index >= 11 ? "EXTREME HIGH" : "NO DATA";
    }

    private final class Update implements Runnable {

        private final String requestedZipCode;

        private Update(String requestedZipCode) {
            this.requestedZipCode = requestedZipCode;
        }

        @Override
        public void run() {
            try (JsonReader jsonReader = new JsonReader(debug)) {
            	final String jsonString = jsonReader.readJsonFromUrl(getDailyUvUndexURLGroup(requestedZipCode));
            	if (!jsonString.isEmpty()) {
            		final JSONArray jsonArray = new JSONArray(jsonString);
            		final JSONObject lastElement = (JSONObject) jsonArray.get(jsonArray.length() - 1);
                    try {
                    	final String str = lastElement.getString("UV_ALERT");
                        pcs.firePropertyChange(Event.UV_ALERT.name(), uvAlert, str);
                        uvAlert = str;
                    } catch (JSONException ex) {
                        if (debug) {
                            LOG.log(Level.INFO, "**** NO UV Alert Group is provided ****", ex);
                        }
                    }
                    try {
                    	final int i = lastElement.getInt("UV_INDEX");
                        pcs.firePropertyChange(Event.MAX_DAILY_UV_INDEX.name(), dailyUvIndex, i);
                        dailyUvIndex = i;
                    } catch (JSONException ex) {
                        if (debug) {
                            LOG.log(Level.INFO, "**** NO UV Alert Daily Index is provided ****", ex);
                        }
                    }
                    try {
                    	final String str = lastElement.getString("DATE");
                        pcs.firePropertyChange(Event.DATE.name(), dateString, str);
                        dateString = str;
                    } catch (JSONException ex) {
                        if (debug) {
                            LOG.log(Level.INFO, "**** NO UV Alert Date is provided ****", ex);
                        }
                    }
                    try {
                    	final String str = lastElement.getString("CITY");
                        pcs.firePropertyChange(Event.CITY.name(), city, str);
                        city = str;
                    } catch (JSONException ex) {
                        if (debug) {
                            LOG.log(Level.INFO, "**** NO UV Alert Locality CITY is provided ****", ex);
                        }
                    }
                    try {
                    	final String str = lastElement.getString("ZIP_CODE");
                        pcs.firePropertyChange(Event.ZIP_CODE.name(), repliedZipCode, str);
                        repliedZipCode = str;
                    } catch (JSONException ex) {
                        if (debug) {
                            LOG.log(Level.INFO, "**** NO UV Alert Locality ZIP CODE is provided ****", ex);
                        }
                    }
                    try {
                    	final String str = lastElement.getString("STATE");
                        pcs.firePropertyChange(Event.STATE.name(), state, str);
                        state = str;
                    } catch (JSONException ex) {
                        if (debug) {
                            LOG.log(Level.INFO, "**** NO UV Alert Locality STATE is provided ****", ex);
                        }
                    }
                    if (debug) {
                        LOG.log(Level.INFO, 
                        "---------------EPA Envirofacts--------------\n" +
                        "   UV Alert Group: {0}", uvAlert + "\n" +
                        "   Date: {0}, " + dateString + "\n" +
                        "   Max Daily UV Index: {0}, " + dailyUvIndex + "\n" +
                        "   Requested Zip Code: {0}, " + requestedZipCode + "\n" +
                        "   Replied Zip Code: {0}, " + repliedZipCode + "\n" +
                        "   City: {0}, " + city + "\n" +
                        "   State: {0}, " + state + "\n" +
                        "----------- END EPA Envirofacts-------------");
                    }
                } else {
                	final boolean b = true;
                    pcs.firePropertyChange(Event.NO_DATA.name(), noData, b);
                    noData = b;
                    if (debug) {
                        LOG.log(Level.INFO, "No UV Data for specified Zip Code: {0}", requestedZipCode);
                    }
                }
            	
            } catch (JSONException ex) {
            	LOG.log(Level.WARNING, null, ex);
                pcs.firePropertyChange(Event.NETWORK_ERROR.name(), null, "Error Retrieving Daily UV Index for ZipCode: " + requestedZipCode);
            }
            
            try (JsonReader jsonReader = new JsonReader(debug)) {
            	final String jsonString = jsonReader.readJsonFromUrl(getHourlyUvUndexURLGroup(requestedZipCode));

                if (!jsonString.isEmpty()) {
                	final JSONArray jsonArray = new JSONArray(jsonString);

                    hourlyUvIndexGroupList.clear();

                    for (int i = 0; i < 21; i++) {
                    	final JSONObject element = (JSONObject) jsonArray.get(i);
                    	final String zip = element.getString("ZIP");
                    	final int order = element.getInt("ORDER");
                    	final int uvValue = element.getInt("UV_VALUE");
                        final String cty = element.getString("CITY");
                        final String sta = element.getString("STATE");
                        final String dateTime = element.getString("DATE_TIME");

                        hourlyUvIndexGroupList.add(new UVIndexGroup(zip, order, uvValue, cty, sta, dateTime));
                    }
                }
            } catch (JSONException ex) {
            	LOG.log(Level.WARNING, null, ex);
                pcs.firePropertyChange(Event.NETWORK_ERROR.name(), null, "Error Retrieving Hourly UV Index for ZipCode: " + requestedZipCode);
            }
        }

        private String getHourlyUvUndexURLGroup(String zipCode) {
        	final String str = "https://data.epa.gov/efservice/getEnvirofactsUVHOURLY/ZIP/"
                    + zipCode
                    + "/"
                    + "JSON";
            if (debug) {
                LOG.log(Level.INFO, str);
            }
            return str;
        }

        private String getDailyUvUndexURLGroup(String zipCode) {
        	final String str = "https://data.epa.gov/efservice/getEnvirofactsUVDAILY/ZIP/"
                    + zipCode
                    + "/"
                    + "JSON";
            if (debug) {
                LOG.log(Level.INFO, str);
            }
            return str;
        }
    }

    public List<UVIndexGroup> getHourlyUvIndexGroupList() {
        return new ArrayList<>(hourlyUvIndexGroupList);
    }

    @Override
    public void close() {
        for (PropertyChangeListener pcl : pcs.getPropertyChangeListeners()) {
            pcs.removePropertyChangeListener(pcl);
        }
        scheduler.shutdownNow();
        scheduler.shutdown();
    }

    private void startUpdateTimer() {
        scheduler.scheduleAtFixedRate(new Update(requestedZipCode),
                INITIAL_WAIT,
                UPDATE_RATE,
                TimeUnit.SECONDS);
    }

    public void addPropertyChangeListener(final PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(final PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }
}
