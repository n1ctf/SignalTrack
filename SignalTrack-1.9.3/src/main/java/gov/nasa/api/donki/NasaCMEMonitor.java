package gov.nasa.api.donki;

/**
 *
 * @author John
 */
import java.awt.Color;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import java.time.ZonedDateTime;

import java.util.logging.Level;
import java.util.logging.Logger;

import json.JsonReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NasaCMEMonitor extends AbstractNasaMonitor {
    public enum Event {
        ACTIVITY_ID,
        TIME21_5,
        ESTIMATED_SHOCK_ARRIVAL_TIME,
        ESTIMATED_DURATION,
        HALF_ANGLE,
        START_TIME,
        IS_EARTH_GB,
        TYPE,
        LONGITUDE,
        LATITUDE,
        LINK,
        NO_EVENTS,
        NETWORK_ERROR,
        DATA_FETCH_COMPLETE,
        FLAG_LOADED
    }
    
    private static final boolean ADVISE_ON_NO_EVENTS = false;
    private static final String API_HEADER = "https://api.nasa.gov/DONKI/CMEAnalysis?";
    private static final String TEST_URL_GROUP = "https://api.nasa.gov/DONKI/CMEAnalysis?startDate=2016-09-01&endDate=2016-09-30&mostAccurateOnly=true&speed=500&halfAngle=30&catalog=ALL&api_key=DEMO_KEY";
    private static final Logger LOG = Logger.getLogger(NasaCMEMonitor.class.getName());
    private static final String CLASS_DESCRIPTOR_STRING = "Coronal Mass Ejection Monitor";
    private static final long DEFAULT_PERSISTENCE_PERIOD_MINUTES = 7200;
	private static final long DEFAULT_API_QUERY_PERIOD_SECONDS = 120;
	
    private String activityID;
    private String type;
    private boolean isEarthGB;
    private double longitude;
    private double latitude;
    private double halfAngle;
    private double estimatedDuration;
    private URL link;
    private ZonedDateTime time21_5;
    private ZonedDateTime estimatedShockArrivalTime;
    private ZonedDateTime startTime;
    private boolean noEvents;
    private ZonedDateTime activityTime;
    private String toolTipText;

    public NasaCMEMonitor(String apiKey, boolean debug) {
        this(API_HEADER, apiKey, debug);
    }
    
    public NasaCMEMonitor(String apiHeader, String apiKey, boolean debug) {
        super(apiHeader, apiKey, debug);
    }
	
	@Override
	public ZonedDateTime getActivityTime() {
		return activityTime;
	}
	
    @Override
    public String getAPIHeader() {
        return API_HEADER;
    }

    @Override
    public String getTestURLGroup() {
        return TEST_URL_GROUP;
    }

    public boolean isIsEarthGB() {
        return isEarthGB;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getEstimatedDuration() {
        return estimatedDuration;
    }

    @Override
    public ZonedDateTime getStartTime() {
        return startTime;
    }
    
    public double getHalfAngle() {
		return halfAngle;
	}

	public ZonedDateTime getTime21_5() {
		return time21_5;
	}

	public String getActivityID() {
        return activityID;
    }

    public String getType() {
        return type;
    }

    public URL getLink() {
        return link;
    }

    public ZonedDateTime getEstimatedShockArrivalTime() {
        return estimatedShockArrivalTime;
    }

    @Override
    public Logger getLogger() {
        return LOG;
    }

    @Override
    public Runnable update(String urlGroup) {
        return new Update(urlGroup);
    }

    protected final class Update implements Runnable {
        private final String urlGroup;

        private Update(String urlGroup) {
            this.urlGroup = urlGroup;    
        }

        @Override
        public void run() {
        	String jsonString = null;
            try (JsonReader jsonReader = new JsonReader(isDebug())) {
                jsonString = jsonReader.readJsonFromUrl(urlGroup);
                if (jsonString != null && jsonString.length() > 2 && (activityTime == null || getAgeOfEventInMinutes() < getPersistenceMinutes())) {
                    noEvents = false;
                    
                    final JSONArray jsonArray = new JSONArray(jsonString);
                    final JSONObject lastElement = (JSONObject) jsonArray.get(jsonArray.length() - 1);
                    
                    if (isDebug()) {
                        LOG.log(Level.INFO, "******** NasaCMEMonitor.Update.JSONObject.lastElement -> {0}", lastElement);
                    }
                    
                    try {
                    	final String str = lastElement.getString("associatedCMEID");
                        pcs.firePropertyChange(Event.ACTIVITY_ID.name(), activityID, str);
                        activityID = str;
                    } catch (JSONException _) {
                        if (isDebug()) {
                            LOG.log(Level.INFO, "**** NO activityID is provided ****");
                        }
                    }
                    
                    try {
                    	final ZonedDateTime zdt = fromNasaDateTimeGroup(lastElement.getString("time21_5"));
                        pcs.firePropertyChange(Event.TIME21_5.name(), time21_5, zdt);
                        time21_5 = zdt;
                    } catch (JSONException _) {
                        if (isDebug()) {
                            LOG.log(Level.INFO, "**** NO CME time21_5 is provided ****");
                        }
                    }
                    
                    try {
                    	final ZonedDateTime zdt = fromNasaDateTimeGroup(lastElement.getString("startTime"));
                        pcs.firePropertyChange(Event.START_TIME.name(), startTime, zdt);
                        startTime = zdt;
                        activityTime = zdt;
                    } catch (JSONException _) {
                        if (isDebug()) {
                            LOG.log(Level.INFO, "**** NO CME startTime is provided ****");
                        }
                    }
                    
                    try {
                    	final boolean b = lastElement.getBoolean("isEarthGB");
                        pcs.firePropertyChange(Event.IS_EARTH_GB.name(), isEarthGB, b);
                        isEarthGB = b;
                    } catch (JSONException _) {
                        if (isDebug()) {
                            LOG.log(Level.INFO, "**** NO CME isEarthGB is provided ****");
                        }
                    }
                    
                    try {
                    	final double a = lastElement.getDouble("halfAngle");
                        pcs.firePropertyChange(Event.HALF_ANGLE.name(), halfAngle, a);
                        halfAngle = a;
                    } catch (JSONException _) {
                        if (isDebug()) {
                            LOG.log(Level.INFO, "**** NO CME halfAngle is provided ****");
                        }
                    }
                    
                    try {
                    	final double d = lastElement.getDouble("latitude");
                        pcs.firePropertyChange(Event.LATITUDE.name(), latitude, d);
                        latitude = d;
                    } catch (JSONException _) {
                        if (isDebug()) {
                            LOG.log(Level.INFO, "**** NO CME latitude is provided ****");
                        }
                    }
                    
                    try {
                    	final double d = lastElement.getDouble("longitude");
                        pcs.firePropertyChange(Event.LONGITUDE.name(), longitude, d);
                        longitude = d;
                    } catch (JSONException _) {
                        if (isDebug()) {
                            LOG.log(Level.INFO, "**** NO CME longitude is provided ****");
                        }
                    }
                    
                    try {
                    	final double d = lastElement.getDouble("estimatedDuration");
                        pcs.firePropertyChange(Event.ESTIMATED_DURATION.name(), estimatedDuration, d);
                        estimatedDuration = d;
                    } catch (JSONException _) {
                        if (isDebug()) {
                            LOG.log(Level.INFO, "**** NO CME estimatedDuration is provided ****");
                        }
                    }
                    
                    try {
                    	final ZonedDateTime zdt = fromNasaDateTimeGroup(lastElement.getString("estimatedShockArrivalTime"));
                        pcs.firePropertyChange(Event.ESTIMATED_SHOCK_ARRIVAL_TIME.name(), estimatedShockArrivalTime, zdt);
                        estimatedShockArrivalTime = zdt;
                    } catch (JSONException _) {
                        if (isDebug()) {
                            LOG.log(Level.INFO, "**** NO CME estimatedShockArrivalTime is provided ****");
                        }
                    }
                    
                    try {
                    	final String str = lastElement.getString("type");
                        pcs.firePropertyChange(Event.TYPE.name(), type, str);
                        type = str;
                    } catch (JSONException _) {
                        if (isDebug()) {
                            LOG.log(Level.INFO, "**** NO CME type is provided ****");
                        }
                    }
                    
                    try {
                    	final URL newLink = new URI(lastElement.getString("link")).toURL();
						pcs.firePropertyChange(Event.LINK.name(), link, newLink);
						link = newLink;
					} catch (JSONException _) {
						if (isDebug()) {
							LOG.log(Level.INFO, "**** NO link is provided ****");
						}
					} catch (MalformedURLException _) {
						if (isDebug()) {
							LOG.log(Level.INFO, "Malformed URL: {0}", link);
						}
					} catch (URISyntaxException _) {
						if (isDebug()) {
							LOG.log(Level.INFO, "Malformed URI: {0}", link);
						}
					}
                    
                    if (isDebug()) {
						LOG.log(Level.INFO, "{0} {1} {2} {3} {4} {5} {6} {7} {8} {9} {10} {11} {12} {13} {14} {15}" +
								"{16} {17} {18} {19} {20} {21} {22} {23} {24} {25}",
							new Object[] {
								"\n----------------------------- CME Event -----------------------------",
								"\n   CurrentUTC:                       ", getCurrentUTC(),
								"\n   Activity ID:                      ", activityID,
								"\n   Type:                             ", type,
								"\n   Start Time:                       ", startTime,	
								"\n   Time21_5:                         ", time21_5,
								"\n   Half Angle:                       ", halfAngle,
								"\n   Estimated Shock Arrival Time:     ", estimatedShockArrivalTime,
								"\n   Estimated Duration:               ", estimatedDuration,
								"\n   Earth Glancing Blow:              ", isEarthGB,
								"\n   Latitude:                         ", latitude,
								"\n   Longitude:                        ", longitude,
								"\n   Link URI:                         ", link,
								"\n--------------------------- END CME Event ---------------------------" });
                    }
                    
                    final StringBuilder bld = new StringBuilder();
					
					bld.append("<HTML>");
					bld.append("CORONAL MASS EJECTION IDENTIFIER: ");
					bld.append(activityID);
					bld.append("<br>");
					bld.append("&emsp;Class Type: ");
					bld.append(type + " : " + getTypeIndicator(type));
					bld.append("<br>");
					bld.append("&emsp;Start Time: ");
					bld.append(startTime);
					bld.append("<br>");
					bld.append("&emsp;Time 21.5: ");
					bld.append(time21_5);
					bld.append("<br>");			
					bld.append("&emsp;Half Angle ");
					bld.append(halfAngle);
					bld.append("<br>");
					bld.append("&emsp;Estimated Shock Arrival Time: ");
					bld.append(estimatedShockArrivalTime);
					bld.append("<br>");
					bld.append("&emsp;Estimated Duration: ");
					bld.append(halfAngle);
					bld.append("<br>");
					bld.append("&emsp;Earth Glancing Blow: ");
					bld.append(isEarthGB);
					bld.append("<br>");
					bld.append("&emsp;Solar Latitude: ");
					bld.append(latitude);
					bld.append("<br>");
					bld.append("&emsp;Solar Longitude: ");
					bld.append(longitude);
					bld.append("<br>");
					bld.append("&emsp;Link URI: ");
					bld.append(link);
					bld.append("</HTML>");
					
					toolTipText = bld.toString();

                } else {
                    type = null;
                    toolTipText = "No CORONAL MASS EJECTION (CME) events detected in the last " + persistenceMinutes + " minutes.";
                    time21_5 = null;
                    startTime = null;
                    estimatedShockArrivalTime = null;
                    if (ADVISE_ON_NO_EVENTS) {
						pcs.firePropertyChange(Event.NO_EVENTS.name(), noEvents, getNarrativeText());
					}
                    noEvents = true;
                    if (isDebug()) {
                        LOG.log(Level.INFO, "NO REPORTABLE CORONAL MASS EJECTIONS IN THE LAST {0} Minutes", persistenceMinutes);
                    }
                }
            } catch (JSONException _) {
                LOG.log(Level.WARNING, "Error Retrieving: {0}", urlGroup);
				LOG.log(Level.WARNING, "Returned json String: {0}", jsonString);
                pcs.firePropertyChange(Event.NETWORK_ERROR.name(), null, "Error Retrieving: " + urlGroup);
            }  finally {
				pcs.firePropertyChange(Event.DATA_FETCH_COMPLETE.name(), null, Boolean.TRUE);
			}
        }
        
        private String getTypeIndicator(String type) {
	    	if (type.contains("CLEAR")) {
	            return "";
	    	} else if (type.contains("ER")) {
	            return "Extreemly Rare : Velocity > 3,000 km/s";
	        } else if (type.contains("R")) {
	            return "Rare : 2,000 km/s > Velocity < 2,999 km/s";
	        } else if (type.contains("O")) {
	            return "Occasional : 1,000 km/s > Velocity < 1,999 km/s";
	        } else if (type.contains("C")) {
	            return "Common : 500 km/s > Velocity < 999 km/s";
	        } else if (type.contains("S")) {
	        	return "Minimal Concern : Velocity < 500 km/s";
	        } else {
	        	return "";
	        }
	    }
    }

    @Override
	public String getNarrativeText() {
		if (!noEvents && activityID != null && type != null) {
			return "CME Activity ID: " + activityID;
		} else {
			return "NO Coronal Mass Ejections Reported";
		}
	}
    
    @Override
	public String getToolTipText() {
		return toolTipText;
	}

    @Override
    public String getFlagText() {
        if (noEvents || activityID == null || type == null) {
            return "NO CME EVENTS < " + persistenceMinutes + " MIN";
        } else {
            return activityID + " Type: " + type;
        }
    }

    @Override
    public Color getFlagColor() {
        if (noEvents || activityID == null || type == null) {
            return Color.GREEN;
        }
        if (type.contains("ER")) {
            return new Color(126, 0, 35); // DARK_RED
        } else if (type.contains("R")) {
            return Color.RED;
        } else if (type.contains("O")) {
            return Color.ORANGE;
        } else if (type.contains("C")) {
            return new Color(255, 204, 0); // LIGHT_ORANGE
        } else if (type.contains("S")) {
        	return Color.YELLOW;
        } else if (type.contains("CLEAR")) {
            return Color.GREEN;
        } else {
            return Color.LIGHT_GRAY;
        }
    }
	
    @Override
    public Color getFlagTextColor() {
        if (noEvents || activityID == null || type == null) {
            return Color.BLACK;
        }
        if (type.contains("ER")) {
        	return Color.LIGHT_GRAY;
        } else if (type.contains("R")) {
        	return Color.BLACK;
        } else if (type.contains("O")) {
        	return Color.BLACK;
        } else if (type.contains("C")) {
        	return Color.BLACK;
        } else if (type.contains("S")) {
        	return Color.BLACK;
        } else if (type.contains("CLEAR")) {
        	return Color.BLACK;
        } else {
        	return Color.BLACK;
        }
    }
    
	@Override
	public void setNoEvents(boolean noEvents) {
		this.noEvents = noEvents;
	}
    
    @Override
	public String getClassDescriptorString() {
		return CLASS_DESCRIPTOR_STRING;
	}
    
    @Override
	public long getDefaultPersistencePeriodMinutes() {
		return DEFAULT_PERSISTENCE_PERIOD_MINUTES;
	}

	@Override
	public long getDefaultApiQueryPeriodSeconds() {
		return DEFAULT_API_QUERY_PERIOD_SECONDS;
	}
}
