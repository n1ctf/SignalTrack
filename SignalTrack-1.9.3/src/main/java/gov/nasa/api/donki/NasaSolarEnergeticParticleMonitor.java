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

public class NasaSolarEnergeticParticleMonitor extends AbstractNasaMonitor {
	public enum Event {
		SEP_ID, 
		EVENT_TIME, 
		DISPLAY_NAME,
		MEV,
		LINK,
		SOLAR_RADIATION_STORM,
		NO_EVENTS, 
		NETWORK_ERROR,
		DATA_FETCH_COMPLETE
	}
	
	private static final boolean ADVISE_ON_NO_EVENTS = false;
	private static final String API_HEADER = "https://api.nasa.gov/DONKI/SEP?";
	public static final String TEST_URL_GROUP = "https://api.nasa.gov/DONKI/SEP?startDate=2012-07-01&endDate=2012-07-30&api_key=JOHNS_API_KEY";
	private static final Logger LOG = Logger.getLogger(NasaSolarEnergeticParticleMonitor.class.getName());
	private static final String CLASS_DESCRIPTOR_STRING = "Solar Energetic Particle Monitor";
	private static final long DEFAULT_PERSISTENCE_PERIOD_MINUTES = 7200;
	private static final long DEFAULT_API_QUERY_PERIOD_SECONDS = 120;
	
	private String sepID;
	private ZonedDateTime eventTime ;
	private String displayName;
	private SolarRadiationStormScale solarRadiationStormScale = SolarRadiationStormScale.S0;
	private URL link;
	private double mev = -1;
	private boolean noEvents;
	private ZonedDateTime activityTime;
	private String toolTipText;
	
	public NasaSolarEnergeticParticleMonitor(String apiKey, boolean debug) {
		this(API_HEADER, apiKey, debug);
	}

	public NasaSolarEnergeticParticleMonitor(String apiHeader, String apiKey, boolean debug) {
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

	public String getDisplayName() {
		return displayName;
	}

	public String getSepID() {
		return sepID;
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
						LOG.log(Level.INFO, "******** NasaSolarEnergeticParticleMonitor.Update.JSONObject.lastElement -> {0}", lastElement);
					}
					
					try {
						pcs.firePropertyChange(Event.SEP_ID.name(), sepID, lastElement.getString("sepID"));
						sepID = lastElement.getString("sepID");
					} catch (JSONException _) {
						if (isDebug()) {
							LOG.log(Level.INFO, "**** NO sepID is provided ****");
						}
					}
					
					try {
						pcs.firePropertyChange(Event.EVENT_TIME.name(), eventTime, fromNasaDateTimeGroup(lastElement.getString("eventTime")));
						eventTime = fromNasaDateTimeGroup(lastElement.getString("eventTime"));
						activityTime = eventTime;
					} catch (JSONException _) {
						if (isDebug()) {
							LOG.log(Level.INFO, "**** NO SEP eventTime is provided ****");
						}
					}
					
					try {
						final JSONArray instrumentsArray = lastElement.getJSONArray("instruments");					
						final String str = instrumentsArray.get(0).toString();
						pcs.firePropertyChange(Event.DISPLAY_NAME.name(), displayName, str);
						final double v = getMeV(str);
						final SolarRadiationStormScale srs = getSolarRadiationStormSeverity(v);
						pcs.firePropertyChange(Event.SOLAR_RADIATION_STORM.name(), solarRadiationStormScale, srs); 
						pcs.firePropertyChange(Event.MEV.name(), mev, v);
						mev = v;
						solarRadiationStormScale = srs;
						displayName = str;
					} catch (JSONException _) {
						if (isDebug()) {
							LOG.log(Level.INFO, "**** NO SEP displayName is provided ****");
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
								"\n-------------------SOLAR ENERGETIC PARTICLE EVENTS-------------------",
								"\n   CurrentUTC:                       ", getCurrentUTC(),
								"\n   SEP ID:                           ", sepID,
								"\n   Event Time:                       ", eventTime,
								"\n   Display Name:                     ", displayName,	
								"\n   MeV:                              ", mev,
								"\n   Link URI:                         ", link,
								"\n-----------------------SOLAR ENERGETIC PARTICLE----------------------" });
                    }
					
					final StringBuilder bld = new StringBuilder();
					
					bld.append("<HTML>");
					bld.append("SOLAR ENEGERTIC PARTICLE IDENTIFIER: ");
					bld.append(sepID);
					bld.append("<br>");
					bld.append("&emsp;Event Time: ");
					bld.append(eventTime);
					bld.append("<br>");
					bld.append("&emsp;Million Electron Volts: ");
					bld.append(mev);
					bld.append("<br>");
					bld.append("&emsp;Indication: ");
					bld.append(getSolarRadiationStormIndication(mev));
					bld.append("<br>");
					bld.append("&emsp;Link URI: ");
					bld.append(link);
					bld.append("</HTML>");
					
					toolTipText = bld.toString();
				} else {
					sepID = null;
					eventTime = null;
					mev = -1;
					toolTipText = "No SOLAR ENERGETIC PARTICLE (SEP) events detected in the last " + persistenceMinutes + " minutes.";
					if (ADVISE_ON_NO_EVENTS) {
						pcs.firePropertyChange(Event.NO_EVENTS.name(), noEvents, getNarrativeText());
					}					
					noEvents = true;
					if (isDebug()) {
						LOG.log(Level.INFO, "NO SOLAR ENERGETIC PARTICLE EVENTS IN THE LAST {0} Minutes", persistenceMinutes);
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
		
		private double getMeV(String displayName) {
			double e = -1;
			if (displayName != null && displayName.contains("MeV")) {
				displayName = displayName.substring(24);
				final String numbers = getStringOfNumbersFromString(displayName);
				if (numbers.length() > -1) {
					int begin = 0;
					if (numbers.indexOf('>') > -1) {
						begin = numbers.indexOf('>') + 1;
					} else if (numbers.indexOf('-') > -1) {
						begin = numbers.indexOf('-') + 1;
					}
					e = Double.parseDouble(numbers.substring(begin));
				}
			}
			return e;
		}
		
		private String getStringOfNumbersFromString(String s) {
			final StringBuilder sb = new StringBuilder();
	        s.chars()
	        	.mapToObj(c -> (char) c)
	        	.filter(c -> Character.isDigit(c) || c == '.' || c == '-')
	        	.forEach(sb::append);
	        return sb.toString();
	    }
		
		private SolarRadiationStormScale getSolarRadiationStormSeverity(double mev) {
			if (mev > 10E4) {
				return SolarRadiationStormScale.S5;
			} else if (mev > 10E3) {
				return SolarRadiationStormScale.S4;
			} else if (mev > 10E2) {
				return SolarRadiationStormScale.S3;
			} else if (mev > 10E1) {
				return SolarRadiationStormScale.S2;
			} else if (mev > 10) {
				return SolarRadiationStormScale.S1;
			} else {
				return SolarRadiationStormScale.S0;
			}
	    }
		
		private String getSolarRadiationStormIndication(double mev) {
			if (mev > 10E4) {
				return "S5 - EXTREME";
			} else if (mev > 10E3) {
				return "S4 - SEVERE";
			} else if (mev > 10E2) {
				return "S3 - STRONG";
			} else if (mev > 10E1) {
				return "S2 - MODERATE";
			} else if (mev > 10) {
				return "S1 - MINOR";
			} else {
				return "UNDETECTABLE";
			}
	    }
	}

	@Override
	public String getNarrativeText() {
		if (noEvents || sepID == null) {
			return "NO Solar Energetic Particle Events Reported";
		} else {
			return "SEP Activity ID: " + sepID + " - Max EMF: " + mev + " Million Electron Volts";
		}
	}

	@Override
	public String getToolTipText() {
		return toolTipText;
	}
	
	@Override
	public String getFlagText() {
		if (noEvents || sepID == null) {
			return "NO SEP EVENTS < " + persistenceMinutes + " MIN";
		} else {
			return sepID + " EMF:" + mev + "MeV";
		}
	}
	
	@Override
	public final Color getFlagColor() {
		if (noEvents) {
            return Color.GREEN;
        } else if (mev > 10E4) {
			return new Color(126, 0, 35); // DARK_RED
		} else if (mev > 10E3) {
			return Color.RED;
		} else if (mev > 10E2) {
			return Color.ORANGE;
		} else if (mev > 10E1) {
			return new Color(255, 204, 0); // LIGHT_ORANGE
		} else if (mev >= 10) {
			return Color.YELLOW;
		} else {
			return Color.GREEN;
		}
	}
	@Override
	public final Color getFlagTextColor() {
		if (noEvents) {
            return Color.BLACK;
        } else if (mev > 10E4) {
        	return Color.LIGHT_GRAY;
		} else if (mev > 10E3) {
			return Color.BLACK;
		} else if (mev > 10E2) {
			return Color.BLACK;
		} else if (mev > 10E1) {
			return Color.BLACK;
		} else if (mev >= 10) {
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
