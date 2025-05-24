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

public class NasaGeomagneticStormMonitor extends AbstractNasaMonitor {
	public enum Event {
		GST_ID, 
		START_TIME, 
		OBSERVED_TIME, 
		KP_INDEX, 
		GEOMAGNETIC_STORM,
		LINK, 
		NO_EVENTS, 
		NETWORK_ERROR,
		DATA_FETCH_COMPLETE
	}
	
	private static final boolean ADVISE_ON_NO_EVENTS = false;
	private static final String API_HEADER = "https://api.nasa.gov/DONKI/GST?";
	private static final String TEST_URL_GROUP = "https://api.nasa.gov/DONKI/GST?startDate=2016-01-01&endDate=2016-01-30&api_key=DEMO_KEY";
	private static final Logger LOG = Logger.getLogger(NasaGeomagneticStormMonitor.class.getName());
	private static final String CLASS_DESCRIPTOR_STRING = "Geomagnetic Storm Monitor";
	private static final long DEFAULT_PERSISTENCE_PERIOD_MINUTES = 7200;
	private static final long DEFAULT_API_QUERY_PERIOD_SECONDS = 120;
	
	private String gstID;
	private int kpIndex;
	private GeomagneticStormScale geomagneticStormScale = GeomagneticStormScale.G0;
	private ZonedDateTime startTime;
	private ZonedDateTime observedTime;
	private ZonedDateTime activityTime;
	private URL link;
	private boolean noEvents;
	private String toolTipText;

	public NasaGeomagneticStormMonitor(String apiKey, boolean debug) {
		this(API_HEADER, apiKey, debug);
	}

	public NasaGeomagneticStormMonitor(String apiHeader, String apiKey, boolean debug) {
		super(apiHeader, apiKey, debug);
	}
	
	@Override
	public ZonedDateTime getActivityTime() {
		return activityTime;
	}
	
	public int getKpIndex() {
		return kpIndex;
	}

	@Override
	public ZonedDateTime getStartTime() {
		return startTime;
	}

	public String getGstID() {
		return gstID;
	}

	public ZonedDateTime getObservedTime() {
		return observedTime;
	}

	public URL getLink() {
		return link;
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
						LOG.log(Level.INFO, "******** NasaGSTMonitor.Update.JSONObject.lastElement -> {0}", lastElement);
					}
					
					try {
						pcs.firePropertyChange(Event.GST_ID.name(), gstID, lastElement.getString("gstID"));
						gstID = lastElement.getString("gstID");
					} catch (JSONException _) {
						if (isDebug()) {
							LOG.log(Level.INFO, "**** NO gstID is provided ****");
						}
					}
					
					try {
						pcs.firePropertyChange(Event.START_TIME.name(), startTime, fromNasaDateTimeGroup(lastElement.getString("startTime")));
						startTime = fromNasaDateTimeGroup(lastElement.getString("startTime"));
						activityTime = startTime;
					} catch (JSONException _) {
						if (isDebug()) {
							LOG.log(Level.INFO, "**** NO GST startTime is provided ****");
						}
					}
					
					try {
						final int kp = lastElement.getInt("kpIndex");
						final GeomagneticStormScale gst = getGeomagneticStormLevel(kp);
						pcs.firePropertyChange(Event.GEOMAGNETIC_STORM.name(), geomagneticStormScale, gst);
						pcs.firePropertyChange(Event.KP_INDEX.name(), kpIndex, kp);
						kpIndex = kp;
						geomagneticStormScale = gst;
					} catch (JSONException _) {
						if (isDebug()) {
							LOG.log(Level.INFO, "**** NO GST kpIndex is provided ****");
						}
					}
					
					try {
						pcs.firePropertyChange(Event.OBSERVED_TIME.name(), observedTime, fromNasaDateTimeGroup(lastElement.getString("observedTime")));
						observedTime = fromNasaDateTimeGroup(lastElement.getString("observedTime"));
					} catch (JSONException _) {
						if (isDebug()) {
							LOG.log(Level.INFO, "**** NO GST observedTime is provided ****");
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
						LOG.log(Level.INFO, "{0} {1} {2} {3} {4} {5} {6} {7} {8} {9} {10} {11} {12} {13}",
							new Object[] {
								"\n----------------- GEOMAGNETIC STORMS -----------------",
								"\n   CurrentUTC:            ", getCurrentUTC(),
								"\n   GST ID:                ", gstID,
								"\n   Start Time:            ", startTime,										
								"\n   Observed Time:         ", observedTime,
								"\n   kp Index:              ", kpIndex,
								"\n   Link URI:              ", link,
								"\n--------------- END GEOMAGNETIC STORMS ---------------" });
					}
					
					final StringBuilder bld = new StringBuilder();
					
					bld.append("<HTML>");
					bld.append("GEOMAGNETIC STORM IDENTIFIER: ");
					bld.append(gstID);
					bld.append("<br>");
					bld.append("&emsp;Start Time: ");
					bld.append(startTime);
					bld.append("<br>");
					bld.append("&emsp;Observed Time: ");
					bld.append(observedTime);
					bld.append("<br>");
					bld.append("&emsp;kp Index: ");
					bld.append(kpIndex);
					bld.append("<br>");
					bld.append("&emsp;Indication: ");
					bld.append(getGeomagneticStormIndication(kpIndex));
					bld.append("<br>");
					bld.append("&emsp;Link URI: ");
					bld.append(link);
					bld.append("</HTML>");
					
					toolTipText = bld.toString();
				} else {
					kpIndex = 0;
					gstID = null;
					toolTipText = "No GEOMAGNETIC STORM (GST) events detected in the last " + persistenceMinutes + " minutes.";
					startTime = null;
					observedTime = null;
					if (ADVISE_ON_NO_EVENTS) {
						pcs.firePropertyChange(Event.NO_EVENTS.name(), noEvents, getNarrativeText());
					}
					noEvents = true;
					if (isDebug()) {
						LOG.log(Level.INFO, "**** NO REPORTABLE GEOMAGNETIC STORMS IN THE LAST {0} Minutes", persistenceMinutes);
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
		
		private GeomagneticStormScale getGeomagneticStormLevel(int kp) {
			return switch (kp) {
				case 9 -> GeomagneticStormScale.G5;
				case 8 -> GeomagneticStormScale.G4;
				case 7 -> GeomagneticStormScale.G3;
				case 6 -> GeomagneticStormScale.G2;
				case 5 -> GeomagneticStormScale.G1;
				default -> GeomagneticStormScale.G0;
			};
	    }
		
		private String getGeomagneticStormIndication(int kp) {
			return switch (kp) {
				case 9 -> "G5 - EXTREME";
				case 8 -> "G4 - SEVERE";
				case 7 -> "G3 - STRONG";
				case 6 -> "G2 - MODERATE";
				case 5 -> "G1 - MINOR";
				default -> "UNDETECTABLE";
			};
	    }
	}

	@Override
	public String getToolTipText() {
		return toolTipText;
	}
	
	@Override
	public String getNarrativeText() {
		if (!noEvents && gstID != null) {
			return "GST Activity ID: " + gstID;
		} else {
			return "NO Geomagnetic Storms Reported";
		}
	}

	@Override
	public String getFlagText() {
		if (noEvents || (kpIndex >= 0 && kpIndex < 5)) {
			return "NO GST EVENTS < " + persistenceMinutes + " MIN";
		} else if (kpIndex == 5) {
			return gstID + " - G1 - MINOR GST";
		} else if (kpIndex == 6) {
			return gstID + " - G2 - MODERATE GST";
		} else if (kpIndex == 7) {
			return gstID + " - G3 - STRONG GST";
		} else if (kpIndex == 8) {
			return gstID + " - G4 - SEVERE GST";
		} else if (kpIndex == 9) {
			return gstID + " - G5 - EXTREME GST";
		} else {
			return "GST API Read Error";
		}
	}

	@Override
	public Color getFlagColor() {
		if (noEvents || (kpIndex >= 0 && kpIndex < 5)) {
			return Color.GREEN;
		} else if (kpIndex == 5) {
			return Color.YELLOW;
		} else if (kpIndex == 6) {
			return new Color(255, 204, 0);
		} else if (kpIndex == 7) {
			return Color.ORANGE;
		} else if (kpIndex == 8) {
			return Color.RED;
		} else if (kpIndex == 9) {
			return new Color(126, 0, 35);
		} else {
			return Color.GREEN;
		}
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
