package gov.nasa.api.donki;

/**
 *
 * @author John
 */

import java.awt.Color;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.ZonedDateTime;
import json.JsonReader;

public class NasaRadiationBeltEnhancementMonitor extends AbstractNasaMonitor {
	public enum Event {
		RBE_ID, 
		EVENT_TIME, 
		DISPLAY_NAME, 
		LINK, 
		NO_EVENTS, 
		NETWORK_ERROR, 
		DATA_FETCH_COMPLETE
	}

	private static final boolean ADVISE_ON_NO_EVENTS = false;
	private static final String API_HEADER = "https://api.nasa.gov/DONKI/RBE?";
	public static final String TEST_URL_GROUP = "https://api.nasa.gov/DONKI/RBE?startDate=2016-01-01&endDate=2016-01-30&api_key=DEMO_KEY";
	private static final Logger LOG = Logger.getLogger(NasaRadiationBeltEnhancementMonitor.class.getName());
	private static final String CLASS_DESCRIPTOR_STRING = "Radiation Belt Enhancement Monitor";
	private static final long DEFAULT_PERSISTENCE_PERIOD_MINUTES = 7200;
	private static final long DEFAULT_API_QUERY_PERIOD_SECONDS = 120;
	
	private String rbeID;
	private ZonedDateTime eventTime;
	private String displayName;
	private URL link;
	private boolean noEvents;
	private ZonedDateTime activityTime;
	private String toolTipText;
	
	public NasaRadiationBeltEnhancementMonitor(String apiKey, boolean debug) {
		this(API_HEADER, apiKey, debug);
	}

	public NasaRadiationBeltEnhancementMonitor(String apiHeader, String apiKey, boolean debug) {
		super(apiHeader, apiKey, debug);
	}

	@Override
	public ZonedDateTime getActivityTime() {
		return activityTime;
	}
	
	@Override
	public Logger getLogger() {
		return LOG;
	}

	@Override
	public String getAPIHeader() {
		return API_HEADER;
	}

	@Override
	public String getTestURLGroup() {
		return TEST_URL_GROUP;
	}

	@Override
	public ZonedDateTime getStartTime() {
		return eventTime;
	}

	public String getRbeID() {
		return rbeID;
	}

	public String getDisplayName() {
		return displayName;
	}

	public URL getLink() {
		return link;
	}

	@Override
	public Runnable update(String urlGroup) {
		return new Update(urlGroup);
	}

	private final class Update implements Runnable {

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
						LOG.log(Level.INFO, "******** NasaRadiationBeltEnhancementMonitor.Update.JSONObject.lastElement -> {0}", lastElement);
					}
					
					try {
						pcs.firePropertyChange(Event.RBE_ID.name(), rbeID, lastElement.getString("rbeID"));
						rbeID = lastElement.getString("rbeID");
					} catch (JSONException _) {
						if (isDebug()) {
							LOG.log(Level.INFO, "**** NO rbeID is provided ****");
						}
					}
					
					try {
						pcs.firePropertyChange(Event.EVENT_TIME.name(), eventTime, fromNasaDateTimeGroup(lastElement.getString("eventTime")));
						eventTime = fromNasaDateTimeGroup(lastElement.getString("eventTime"));
						activityTime = eventTime;
					} catch (JSONException _) {
						if (isDebug()) {
							LOG.log(Level.INFO, "**** NO RBE eventTime is provided ****");
						}
					}
					
					try {
						pcs.firePropertyChange(Event.DISPLAY_NAME.name(), displayName, lastElement.getString("displayName"));
						displayName = lastElement.getString("displayName");
					} catch (JSONException _) {
						if (isDebug()) {
							LOG.log(Level.INFO, "**** NO RBE displayName is provided ****");
						}
					}
					
					try {
						final URL url = new URI(lastElement.getString("link")).toURL();
						pcs.firePropertyChange(Event.LINK.name(), link, url);
						link = url;
					} catch (JSONException | URISyntaxException _) {
						if (isDebug()) {
							LOG.log(Level.INFO, "**** NO link is provided ****");
						}
					}
					
					if (isDebug()) {
						LOG.log(Level.INFO, "------RADIATION BELT ENHANCEMENT EVENTS-----");
						LOG.log(Level.INFO, " CurrentUTC: {0}", getCurrentUTC());
						LOG.log(Level.INFO, " rbeID: {0}", rbeID);
						LOG.log(Level.INFO, " eventTime: {0}", eventTime);
						LOG.log(Level.INFO, " displayName: {0}", displayName);
						LOG.log(Level.INFO, " link: {0}", link);
						LOG.log(Level.INFO, "--------------------------------------------");
					}
					
					final StringBuilder bld = new StringBuilder();
					
					bld.append("<HTML>");
					bld.append("RADIATION BELT ENHANCEMENT IDENTIFIER: ");
					bld.append(rbeID);
					bld.append("<br>");
					bld.append("&emsp;Event Time: ");
					bld.append(eventTime);
					bld.append("<br>");
					bld.append("&emsp;Display Name: ");
					bld.append(displayName);
					bld.append("<br>");
					bld.append("&emsp;Link URI: ");
					bld.append(link);
					bld.append("</HTML>");
					
					toolTipText = bld.toString();
				} else {
					rbeID = null;
					eventTime = null;
					toolTipText = "No RADIATION BELT ENHANCEMENT (RBE) events detected in the last " + persistenceMinutes + " minutes.";
					if (ADVISE_ON_NO_EVENTS) {
						pcs.firePropertyChange(Event.NO_EVENTS.name(), noEvents, getNarrativeText());
					}					
					noEvents = true;
					if (isDebug()) {
						LOG.log(Level.INFO, "NO RADIATION BELT ENHANCEMENT EVENTS IN THE LAST {0} Minutes", persistenceMinutes);
					}
				}
			} catch (IOException | JSONException _) {
				LOG.log(Level.WARNING, "Error Retrieving: {0}", urlGroup);
				LOG.log(Level.WARNING, "Returned json String: {0}", jsonString);
				pcs.firePropertyChange(Event.NETWORK_ERROR.name(), null, "Error Retrieving: " + urlGroup);
			} finally {
				pcs.firePropertyChange(Event.DATA_FETCH_COMPLETE.name(), null, Boolean.TRUE);
			}
		}
	}

	@Override
	public String getNarrativeText() {
		if (noEvents || rbeID == null) {
			return "NO Radiation Belt Enhancements Reported";
		} else {
			return "RBE Activity ID: " + rbeID;
		}
	}

	@Override
	public String getToolTipText() {
		return toolTipText;
	}
	
	@Override
	public String getFlagText() {
		if (noEvents || rbeID == null) {
			return "NO RBE EVENTS < " + persistenceMinutes + " MIN";
		} else {
			return rbeID;
		}
	}

	@Override
	public Color getFlagColor() {
		return noEvents || rbeID == null ? Color.GREEN : Color.YELLOW;
	}
	
	@Override
	public Color getFlagTextColor() {
		return Color.BLACK;
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
