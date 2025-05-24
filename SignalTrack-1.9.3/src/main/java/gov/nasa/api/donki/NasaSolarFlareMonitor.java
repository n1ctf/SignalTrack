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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import json.JsonReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NasaSolarFlareMonitor extends AbstractNasaMonitor {
	
	public enum Event {
		FLARE_ID, 
		CLASS_TYPE,
		RADIO_BLACKOUT,
		SOURCE_LOCATION, 
		ACTIVE_REGION_NUMBER, 
		BEGIN_TIME, 
		PEAK_TIME, 
		END_TIME, 
		LINK, 
		INSTRUMENTS,
		LINKED_EVENTS, 
		NO_EVENTS, 
		NETWORK_ERROR, 
		DATA_FETCH_COMPLETE
	}
	
	private static final boolean ADVISE_ON_NO_EVENTS = false;
	private static final String API_HEADER = "https://api.nasa.gov/DONKI/FLR?";
	private static final String TEST_URL_GROUP = "https://api.nasa.gov/DONKI/FLR?startDate=2016-01-01&endDate=2016-01-30&api_key=DEMO_KEY";
	private static final Logger LOG = Logger.getLogger(NasaSolarFlareMonitor.class.getName());
	private static final String CLASS_DESCRIPTOR_STRING = "Solar Flare Monitor";
	private static final long DEFAULT_PERSISTENCE_PERIOD_MINUTES = 7200;
	private static final long DEFAULT_API_QUERY_PERIOD_SECONDS = 120;
	
	protected String flareID;
	private List<String> instruments = new ArrayList<>(8);
	private RadioBlackoutScale radioBlackoutScale = RadioBlackoutScale.R0;
	private ZonedDateTime beginTime;
	private ZonedDateTime peakTime;
	private ZonedDateTime endTime;
	private ZonedDateTime activityTime;
	private URL link;
	private String classType;
	private String sourceLocation;
	private int activeRegionNumber;
	private List<String> linkedEvents = new ArrayList<>(8);
	private boolean noEvents;
	private String toolTipText;

	public NasaSolarFlareMonitor(String apiKey, boolean debug) {
		this(API_HEADER, apiKey, debug);
	}

	public NasaSolarFlareMonitor(String apiHeader, String apiKey, boolean debug) {
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

	public String getFlareID() {
		return flareID;
	}

	public ZonedDateTime getBeginTime( ) {
		return beginTime;
	}
	
	@Override
	public ZonedDateTime getStartTime() {
		return peakTime;
	}

	public ZonedDateTime getPeakTime() {
		return peakTime;
	}

	public ZonedDateTime getEndTime() {
		return endTime;
	}

	public String getClassType() {
		return classType;
	}

	public String getSourceLocation() {
		return sourceLocation;
	}

	public int getActiveRegionNumber() {
		return activeRegionNumber;
	}

	public URL getLink() {
		return link;
	}

	public List<String> getInstruments() {
		return Collections.unmodifiableList(instruments);
	}

	public List<String> getLinkedEvents() {
		return Collections.unmodifiableList(linkedEvents);
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
				
				// The following statement serves 2 purposes: To know if the class has ever encountered a Solar Flare event,
				// and if it has, then to keep checking for the length of time that event should persist on the screen. 
				// The state of the peakTime variable tests if the API call ever received a response that included peakTime. 
				// If activityTime is null, then it hasn't received a response, or it was an incomplete response.
				// In either case, the query will be sent every 2 minutes by default, or whatever the user selects. 
				// If activityTime is not null, then there was a complete solar flare report from NASA. That report should stay
				// on screen for the user selected number of minutes - which defaults to DEFAULT_PERSISTENCE_PERIOD_MINUTES.  
				// The age in minutes reported by the overridden abstract getActivityTime() method - which in this case 
				// returns the currentUTC() time the flrID was published - is calculated in the getAgeOfEventInMinutes() method. The age of 
				// the solar flare event is assumed to start when the flrID statement is received. So long as the age is less than the persistence period
				// this test will be true, and the query will be resent every DEFAULT_API_QUERY_PERIOD_SECONDS.
				// In addition, this test is suppressed from running if, for any reason, the jsonString.length() is 2 or less.
				
                if (jsonString != null && jsonString.length() > 2 && (activityTime == null || getAgeOfEventInMinutes() < getPersistenceMinutes())) {
					noEvents = false;
					
					final JSONArray jsonArray = new JSONArray(jsonString);
					final JSONObject lastElement = (JSONObject) jsonArray.get(jsonArray.length() - 1);
					
					if (isDebug()) {
						LOG.log(Level.INFO, "******** NasaSolarFlareMonitor.Update.JSONObject.lastElement -> {0}", lastElement);
					}
					
					try {
						// String str = getLongestOccurenceOf("flrID", jsonArray);
						String str = lastElement.getString("flrID");
						str = str.length() > 4 ? str : "Unidentified Flare";
						pcs.firePropertyChange(Event.FLARE_ID.name(), flareID, str);
						flareID = str;
					} catch (JSONException _) {
						if (isDebug()) {
							LOG.log(Level.INFO, "**** NO flareID is provided ****");
						}
					}
					
					try {
						final String ct = lastElement.getString("classType");
						final RadioBlackoutScale rb = getRadioBlackoutRisk(ct);
						pcs.firePropertyChange(Event.CLASS_TYPE.name(), classType, ct);
						pcs.firePropertyChange(Event.RADIO_BLACKOUT.name(), radioBlackoutScale, rb);
						classType = ct;
						radioBlackoutScale = rb;
					} catch (JSONException _) {
						if (isDebug()) {
							LOG.log(Level.INFO, "**** NO classType is provided ****");
						}
					}
					
					try {
						final String str = lastElement.getString("sourceLocation");
						pcs.firePropertyChange(Event.SOURCE_LOCATION.name(), sourceLocation, str);
						sourceLocation = str;
					} catch (JSONException _) {
						if (isDebug()) {
							LOG.log(Level.INFO, "**** NO sourceLocation is provided ****");
						}
					}
					
					try {
						final int i = lastElement.getInt("activeRegionNum");
						pcs.firePropertyChange(Event.ACTIVE_REGION_NUMBER.name(), activeRegionNumber, i);
						activeRegionNumber = i;
					} catch (JSONException _) {
						if (isDebug()) {
							LOG.log(Level.INFO, "**** NO activeRegionNumber is provided ****");
						}
					}
					
					try {
						final ZonedDateTime zdt = fromNasaDateTimeGroup(lastElement.getString("beginTime"));
						pcs.firePropertyChange(Event.BEGIN_TIME.name(), beginTime, zdt);
						beginTime = zdt;
					} catch (JSONException _) {
						if (isDebug()) {
							LOG.log(Level.INFO, "**** NO beginTime is provided ****");
						}
					}
					
					try {
						final ZonedDateTime zdt = fromNasaDateTimeGroup(lastElement.getString("peakTime"));
						pcs.firePropertyChange(Event.PEAK_TIME.name(), peakTime, zdt);
						peakTime = zdt;
					} catch (JSONException _) {
						if (isDebug()) {
							LOG.log(Level.INFO, "**** NO peakTime is provided ****");
						}
					}
					
					try {
						final ZonedDateTime zdt = fromNasaDateTimeGroup(lastElement.getString("endTime"));
						pcs.firePropertyChange(Event.END_TIME.name(), endTime, zdt);
						endTime = zdt;
						activityTime = zdt;
					} catch (JSONException _) {
						if (isDebug()) {
							LOG.log(Level.INFO, "**** NO endTime is provided ****");
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
					
					try {
						if (lastElement.get("instruments") != null) {
	 						pcs.firePropertyChange(Event.INSTRUMENTS.name(), instruments, JsonReader.jsonArrayToStringList((JSONArray) lastElement.get("instruments")));
							instruments = JsonReader.jsonArrayToStringList((JSONArray) lastElement.get("instruments"));
						}
					} catch (JSONException _) {
						if (isDebug()) {
							LOG.log(Level.INFO, "**** NO instruments are provided ****");
						}
					}
					try {
						if (lastElement.get("linkedEvents") != null) {
							final List<String> le = JsonReader.jsonArrayToStringList((JSONArray) lastElement.get("linkedEvents"));
							pcs.firePropertyChange(Event.LINKED_EVENTS.name(), linkedEvents, le);
							linkedEvents = le;
						}
					} catch (JSONException | ClassCastException _) {
						if (isDebug()) {
							LOG.log(Level.INFO, "**** NO linkedEvents are provided ****");
						}
					} 
					
					if (isDebug()) {
						LOG.log(Level.INFO, "-----------------SOLAR FLARE----------------");
						LOG.log(Level.INFO, " Flare Identification Group: {0}", flareID);
						LOG.log(Level.INFO, " CurrentUTC: {0}", getCurrentUTC());
						LOG.log(Level.INFO, " FlareURLGroup: {0}", urlGroup);
						LOG.log(Level.INFO, " classType: {0}", classType);
						LOG.log(Level.INFO, " beginTime: {0}", beginTime);
						LOG.log(Level.INFO, " peakTime: {0}", peakTime);
						LOG.log(Level.INFO, " endTime: {0}", endTime);
						LOG.log(Level.INFO, " link: {0}", link);
						LOG.log(Level.INFO, "--------------------------------------------");
					}
					
					final StringBuilder bld = new StringBuilder();
					
					bld.append("<HTML>");
					bld.append("SOLAR FLARE IDENTIFIER: ");
					bld.append(flareID);
					bld.append("<br>");
					bld.append("&emsp;Flare Class: ");
					bld.append(classType + " : " + getIntensity(classType));
					bld.append("<br>");
					bld.append("&emsp;Radio Blackout Indication: ");
					bld.append(getRadioBlackoutRiskText(classType));
					bld.append("<br>");
					bld.append("&emsp;Begin Time: ");
					bld.append(beginTime);
					bld.append("<br>");
					bld.append("&emsp;Peak Time: ");
					bld.append(peakTime);
					bld.append("<br>");
					bld.append("&emsp;End Time: ");
					bld.append(endTime);
					bld.append("<br>");
					bld.append("&emsp;Link URI: ");
					bld.append(link);
					bld.append("</HTML>");
					
					toolTipText = bld.toString();
					
				} else {
					flareID = null;
					toolTipText = "No SOLAR FLARE (FLR) events detected in the last " + persistenceMinutes + " minutes.";
					peakTime = null;
					if (ADVISE_ON_NO_EVENTS) {
						pcs.firePropertyChange(Event.NO_EVENTS.name(), noEvents, getNarrativeText());
					}
					noEvents = true;
					if (isDebug()) {
						LOG.log(Level.INFO, "NO REPORTABLE SOLAR FLARE EVENTS IN THE LAST {0} Minutes", persistenceMinutes);
					}
				}
			} catch (JSONException _) {
				LOG.log(Level.WARNING, "Error Retrieving Data from URL Group: {0}\n  Returned json String: {1}", 
						new Object[] { urlGroup, jsonString });
				pcs.firePropertyChange(Event.NETWORK_ERROR.name(), null, "Error Retrieving: " + urlGroup);
			} finally {
				pcs.firePropertyChange(Event.DATA_FETCH_COMPLETE.name(), null, Boolean.TRUE);
			}
		}
		
		private RadioBlackoutScale getRadioBlackoutRisk(String type) {
			try {
				final double level = Double.parseDouble(type.substring(1));
				if (type.contains("M") && level <= 2) {
					return RadioBlackoutScale.R1;
				}
				if (type.contains("M") && level <= 5) {
					return RadioBlackoutScale.R2;
				}
				if (type.contains("X") && level <= 9) {
					return RadioBlackoutScale.R3;
				}
				if (type.contains("X") && level <= 15) {
					return RadioBlackoutScale.R4;
				}
				if (type.contains("X")) {
					return RadioBlackoutScale.R5;
				}
			} catch (IndexOutOfBoundsException _) {
				LOG.log(Level.WARNING, "Index Out Of Bounds Exception: {0}", type);
			} catch (NumberFormatException _) {
				LOG.log(Level.WARNING, "Number Format Exception: {0}", type);
			}
			return RadioBlackoutScale.R0;
	    }
		
		private String getRadioBlackoutRiskText(String type) {
			try {
				final double level = Double.parseDouble(type.substring(1));
				if (type.contains("M") && level <= 2) {
					return "R1 - MINOR";
				}
				if (type.contains("M") && level <= 5) {
					return "R2 - MODERATE";
				}
				if (type.contains("X") && level <= 9) {
					return "R3 - STRONG";
				}
				if (type.contains("X") && level <= 15) {
					return "R4 - SEVERE";
				}
				if (type.contains("X")) {
					return "R5 - EXTREME";
				}
			} catch (IndexOutOfBoundsException _) {
				LOG.log(Level.WARNING, "Index Out Of Bounds Exception: {0}", type);
			} catch (NumberFormatException _) {
				LOG.log(Level.WARNING, "Number Format Exception: {0}", type);
			}
			return "NO IMPACT ON RADIO COMMUNICATIONS";
	    }
		
		private String getIntensity(String type) {
	    	if (type.contains("CLEAR")) {
	            return "";
	    	} else if (type.contains("X")) {
	            return "Intensity >= 100 \u00B5W/m\u00B2";
	        } else if (type.contains("M")) {
	            return "Intensity = 10 \u00B5W/m\u00B2 to 100 \u00B5W/m\u00B2";
	        } else if (type.contains("C")) {
	            return "Intensity = 1 \u00B5W/m\u00B2 to 10 \u00B5W/m\u00B2";
	        } else if (type.contains("B")) {
	            return "Intensity = 0.1 \u00B5W/m\u00B2 to 1 \u00B5W/m\u00B2";
	        } else if (type.contains("A")) {
	        	return "Intensity < 0.1 \u00B5W/m\u00B2";
	        } else {
	        	return "";
	        }
	    }
	}
	
	@Override
	public String getToolTipText() {
		return toolTipText;
	}
	
	@Override
	public Color getFlagColor() {
		if (noEvents || classType == null) {
			return Color.GREEN;
		}
		if (classType.contains("CLEAR")) {
			return new Color(0, 255, 0);
		} else if (classType.contains("X")) {
			return new Color(126, 0, 35);
		} else if (classType.contains("M")) {
			return new Color(255, 0, 0);
		} else if (classType.contains("C")) {
			return new Color(255, 102, 0);
		} else if (classType.contains("B")) {
			return new Color(255, 204, 0);
		} else if (classType.contains("A")) {
			return new Color(255, 255, 0);
		} else {
			return Color.LIGHT_GRAY;
		}
	}
	
	@Override
	public Color getFlagTextColor() {
		if (noEvents || classType == null) {
			return Color.BLACK;
		}
		if (classType.contains("CLEAR")) {
			return Color.BLACK;
		} else if (classType.contains("X")) {
			return Color.LIGHT_GRAY;
		} else if (classType.contains("M")) {
			return Color.BLACK;
		} else if (classType.contains("C")) {
			return Color.BLACK;
		} else if (classType.contains("B")) {
			return Color.BLACK;
		} else if (classType.contains("A")) {
			return Color.BLACK;
		} else {
			return Color.BLACK;
		}
	}
	
	@Override
	public String getFlagText() {
		if (noEvents || flareID == null) {
			return "NO FLARE EVENTS < " + persistenceMinutes + " MIN";
		}
		return flareID + " Class:" + classType;
	}

	@Override
	public String getNarrativeText() {
		if (!noEvents && flareID != null) {
			return "Flare ID: " + flareID + " Class: " + classType;
		}
		return "NO Solar Flares Reported";
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
