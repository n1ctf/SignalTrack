package gov.epa;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.rometools.rome.feed.synd.SyndEntryImpl;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import net.htmlparser.jericho.Renderer;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Source;

import utility.Vincenty;

import n1ctf.FailTimer;

public class AirNowAPI implements AutoCloseable {

    public enum Event {
        LOCATION,
        AGENCY,
        CURRENT_AIR_QUALITY_DTG,
        LAST_UPDATE_DTG,
        AQI_OZONE,
        AQI_PARTICLE_POLLUTION_2_5_MICRONS,
        AQI_PARTICLE_POLLUTION_10_MICRONS,
        NETWORK_ERROR,
        FAIL_AQI_OZONE,
        FAIL_AQI_PPM_2_5,
        FAIL_AQI_PPM_10
    }
    
    public static final String OZONE_SERVICE_UNAVAILABLE_STATEMENT = """
		<HTML>NOTICE:<br>
		The United States Environmental Protection Agency does not report Ozone AQI during this month.</HTML>
		""";
    
    public static final String GREEN_DEFINITION = """
		<HTML>Concern Level: NONE<br>
		Air quality is satisfactory, and air pollution poses little or no risk.</HTML>
		""";
    
    public static final String YELLOW_DEFINITION = """
		<HTML>Concern Level: MODERATE<br>
		Air quality is acceptable. However, there may be a risk for some people,<br>
		particularly those who are unusually sensitive to air pollution.</HTML>
		""";
    
    public static final String ORANGE_DEFINITION = """
		<HTML>Concern Level: UNHEALTHY FOR SENSITIVE GROUPS<br>
		Members of sensitive groups may experience health effects.<br>
		The general public is less likely to be affected.</HTML>
		""";
    
    public static final String RED_DEFINITION = """
		<HTML>Concern Level: UNHEALTHY<br>
		Some members of the general public may experience health effects, and<br>
		members of sensitive groups may experience more serious health effects.</HTML>
		""";

    public static final String PURPLE_DEFINITION = """
		<HTML>Concern Level: VERY UNHEALTHY<br>
		Health alert: The risk of health effects is increased for all persons.</HTML>
		""";
    
    public static final String MAROON_DEFINITION = """
		<HTML>Concern Level: HAZARDOUS<br>
		Health emergency conditions exist: all persons are more likely to be affected.</HTML>
		""";
    
    public static final String DEFAULT_FEED_SOURCE = "https://feeds.airnowapi.org/rss/realtime/nnn.xml";
    public static final String DEFAULT_LOCATION_CODE = "29"; // Columbus, Ohio
    public static final boolean DEFAULT_DEBUG_MODE = true;
    public static final long DEFAULT_INITIAL_WAIT = 15;     // seconds
    public static final long DEFULT_UPDATE_RATE = 300;    // seconds
    public static final Logger LOG = Logger.getLogger(AirNowAPI.class.getName());
    public static final long DEFAULT_FAIL_TIMEOUT_SECONDS = 360;
    
    private Locale locale = Locale.getDefault();
    
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    private static final List<AirNowLocationCode> airNowLocationCodeSet = new ArrayList<>(128);
    
    private String locationCode = DEFAULT_LOCATION_CODE;
    private String city;
    private String state;
    private String agency;
    private String location;
    private String currentAirQuality;
    private String lastUpdate;
    
    private int aqiOzone = -1;
    private int aqiPPM25 = -1;
    private int aqiPPM10 = -1;
    
    private boolean debug;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    
    private final FailTimer failAQIOzone = new FailTimer(DEFAULT_FAIL_TIMEOUT_SECONDS);
    private final FailTimer failAQIPPM25 = new FailTimer(DEFAULT_FAIL_TIMEOUT_SECONDS);
    private final FailTimer failAQIPPM10 = new FailTimer(DEFAULT_FAIL_TIMEOUT_SECONDS);
    
    private PropertyChangeListener failTimerListenerAQIOzone;
    private PropertyChangeListener failTimerListenerAQIPPM25;
    private PropertyChangeListener failTimerListenerAQIPPM10;

    public AirNowAPI() {
        this(DEFAULT_LOCATION_CODE, DEFAULT_DEBUG_MODE);
    }

    public AirNowAPI(double latitude, double longitude, boolean debug) {
        this(getLocationCodeElement(latitude, longitude).getLocationCode(), debug);
    }

    public AirNowAPI(String locationCode, boolean debug) {
        this.locationCode = locationCode;
        this.debug = debug;

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                close();
            }
        });
        
        failTimerListenerAQIOzone = (PropertyChangeEvent event) -> {
        	if (FailTimer.FAIL.equals(event.getPropertyName())) {
				pcs.firePropertyChange(Event.FAIL_AQI_OZONE.name(), null, null);
			}
        };
        
        failTimerListenerAQIPPM25 = (PropertyChangeEvent event) -> {
        	if (FailTimer.FAIL.equals(event.getPropertyName())) {
				pcs.firePropertyChange(Event.FAIL_AQI_PPM_2_5.name(), null, null);
			}
        };
        
        failTimerListenerAQIPPM10 = (PropertyChangeEvent event) -> {
        	if (FailTimer.FAIL.equals(event.getPropertyName())) {
				pcs.firePropertyChange(Event.FAIL_AQI_PPM_10.name(), null, null);
			}
        };
        
        start();
    }

	public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getFeedSource() {
        return DEFAULT_FEED_SOURCE;
    }

    public String getLocationCode() {
        return locationCode;
    }

    public String getAgency() {
        return agency;
    }

    public String getLocation() {
        return location;
    }

    public ZonedDateTime getCurrentAirQualityZonedDateTime() {
        return fromEPAAirNowAPIDateTimeGroup(currentAirQuality);
    }

    public ZonedDateTime getLastUpdateZonedDateTime() {
        return fromRFC1123DateTimeGroup(lastUpdate);
    }

    private final class Update implements Runnable {

        private final String locationCode;
        private final String feedSource;

        private Update(String feedSource, String locationCode) {
            this.feedSource = feedSource;
            this.locationCode = locationCode;
        }

        @Override
        public void run() {
        	try {
        		final URL feedSourceURL = getFeedSourceURL(feedSource, locationCode);
	
        		final List<String> listOfLines = parseStringOfLinesToListOfLines(getApiResponse(feedSourceURL));
        		
        		final int aqiOzoneCheck = Integer.parseInt(getAqiOzone(listOfLines));
	            pcs.firePropertyChange(Event.AQI_OZONE.name(), aqiOzone, aqiOzoneCheck);
	            aqiOzone = aqiOzoneCheck;
	            
	            final int aqiPPM25Check = Integer.parseInt(getAqiParticlePolution2point5Microns(listOfLines));
	            pcs.firePropertyChange(Event.AQI_PARTICLE_POLLUTION_2_5_MICRONS.name(), aqiPPM25, aqiPPM25Check);
	            aqiPPM25 = aqiPPM25Check;
	
	            final int aqiPPM10Check = Integer.parseInt(getAqiParticlePolution10Microns(listOfLines));            
	            pcs.firePropertyChange(Event.AQI_PARTICLE_POLLUTION_10_MICRONS.name(), aqiPPM10, aqiPPM10Check);
	            aqiPPM10 = aqiPPM10Check;
	
	            final String currentAirQualityStr = getCurrentAirQualityDTG(listOfLines);
	
	            if (currentAirQualityStr != null) {
	                pcs.firePropertyChange(Event.CURRENT_AIR_QUALITY_DTG.name(), currentAirQuality, currentAirQualityStr);
	                currentAirQuality = currentAirQualityStr;
	            } else {
	                if (debug) {
	                    LOG.log(Level.INFO, "**** NO CurrentAirQuality DTG is provided ****");
	                }
	            }
	
	            final String lastUpdateStr = getLastUpdateDTG(listOfLines);
	
	            if (lastUpdateStr != null) {
	                pcs.firePropertyChange(Event.LAST_UPDATE_DTG.name(), lastUpdate, lastUpdateStr);
	                lastUpdate = lastUpdateStr;
	            } else {
	                if (debug) {
	                    LOG.log(Level.INFO, "**** NO Last Update DTG is provided ****");
	                }
	            }
	
	            final String locationStr = getLocation(listOfLines);
	
	            if (locationStr != null) {
	                pcs.firePropertyChange(Event.LOCATION.name(), location, locationStr);
	                location = locationStr;
	            } else {
	                if (debug) {
	                    LOG.log(Level.INFO, "**** NO Location is provided ****");
	                }
	            }
	
	            final String agencyStr = getAgency(listOfLines);
	
	            if (agencyStr != null) {
	                pcs.firePropertyChange(Event.AGENCY.name(), agency, agencyStr);
	                agency = agencyStr;
	            } else {
	                if (debug) {
	                    LOG.log(Level.INFO, "**** NO Agency is provided ****");
	                }
	            }
	
	            if (debug) {
	                LOG.log(Level.INFO, "{0} {1} {2} {3} {4} {5} {6} {7} {8} {9} {10} {11} {12} {13} {14} {15}", new Object[]{
	                    "---------------------- EPA AirNowAPI ----------------------",
	                    "\n    DTG Current Air Quality:               ", currentAirQuality,
	                    "\n    Location:                              ", location,
	                    "\n    Agency:                                ", agency,
	                    "\n    AQI Ozone:                             ", aqiOzone,
	                    "\n    AQI Particle Pollution => 2.5 Microns: ", aqiPPM25,
	                    "\n    AQI Particle Pollution => 10 Microns:  ", aqiPPM10,
	                    "\n    DTG Last Update:                       ", lastUpdate,
	                    "\n-------------------- END EPA AirNowAPI --------------------"
	                });
	            }
        	} catch (NullPointerException _) {
        		if (debug) {
                    LOG.log(Level.INFO, "**** AirNowAPI URL is Invalid ****");
                }
        	}
        }
        
        private String getAgency(List<String> list) {
            String str = null;
            for (ListIterator<String> iter = list.listIterator(); iter.hasNext();) {
            	final String element = iter.next().toUpperCase(getLocale());
                if (element.contains("AGENCY:")) {
                    str = element.split(" ")[1];
                }
            }
            return str;
        }

        private String getLocation(List<String> list) {
            String str = null;
            for (ListIterator<String> iter = list.listIterator(); iter.hasNext();) {
            	final String element = iter.next().toUpperCase(getLocale());
                if (element.contains("LOCATION:")) {
                    str = element.split(" ")[1] + " " + element.split(" ")[2];
                }
            }
            return str;
        }

        private String getAqiOzone(List<String> list) {
        	boolean valid = false;
        	String str = "-1";
            for (ListIterator<String> iter = list.listIterator(); iter.hasNext();) {
            	final String element = iter.next().toUpperCase(getLocale());
                if (element.contains("OZONE")) {
                	final String s = getStringOfNumbersFromString(element);
                    if (!s.isBlank()) {
                    	str = s;
                    	valid = true;
                    	break;
                    }
                }
            }
            if (valid) {
            	failAQIOzone.resetTimer();
            }
            return str;
        }

        private String getAqiParticlePolution2point5Microns(List<String> list) {
        	boolean valid = false;
            String str = "-1";
            for (ListIterator<String> iter = list.listIterator(); iter.hasNext();) {
                String element = iter.next().toUpperCase(getLocale());
                if (element.contains("PARTICLE POLLUTION (2.5 MICRONS)")) {
                    element = element.replace("2.5 MICRONS", "");
                    final String s = getStringOfNumbersFromString(element);
                    if (!s.isBlank()) {
                    	str = s;
                    	valid = true;
                    	break;
                    }
                }   
            }
            if (valid) {
            	failAQIPPM25.resetTimer();
            }
            return str;
        }

        private String getAqiParticlePolution10Microns(List<String> list) {
        	boolean valid = false;
            String str = "-1";
            for (ListIterator<String> iter = list.listIterator(); iter.hasNext();) {
                String element = iter.next().toUpperCase(getLocale());
                if (element.contains("PARTICLE POLLUTION (10 MICRONS)")) {
                    element = element.replace("10 MICRONS", "");
                    final String s = getStringOfNumbersFromString(element);
                    if (!s.isBlank()) {
                    	str = s;
                    	valid = true;
                    	break;
                    }
                }
            }
            if (valid) {
            	failAQIPPM10.resetTimer();
            } 
            return str;
        }

        private String getCurrentAirQualityDTG(List<String> list) {
            String str = null;
            for (ListIterator<String> iter = list.listIterator(); iter.hasNext();) {
            	final String element = iter.next().toUpperCase(getLocale());
            	final int i = element.indexOf("CURRENT AIR QUALITY: ");
                if (i > -1) {
                    str = element.substring(i + 21);
                }
            }
            return str;
        }

        private String getLastUpdateDTG(List<String> list) {
            String str = null;
            for (ListIterator<String> iter = list.listIterator(); iter.hasNext();) {
            	final String element = iter.next().toUpperCase(getLocale());
            	final int i = element.indexOf("LAST UPDATE: ");

                if (i > -1) {
                    str = element.substring(i + 13);
                }
            }
            return str;
        }

        private URL getFeedSourceURL(String feedSource, String locationCode) {
            try {
                return new URI(feedSource.replace("nnn", locationCode)).toURL();
            } catch (MalformedURLException _) {
                LOG.log(Level.WARNING, "Error Retrieving FeedSource URL: {0}", feedSource);
                pcs.firePropertyChange(Event.NETWORK_ERROR.name(), null, "Error Retrieving: " + feedSource);
            } catch (URISyntaxException e) {
                LOG.log(Level.SEVERE, null, e);
            }
            return null;
        }

        private String getApiResponse(URL feedSource) {
            String str = null;
            final SyndFeedInput input = new SyndFeedInput();
            try {
            	final SyndFeed feed = input.build(new XmlReader(feedSource.openStream()));
            	final SyndEntryImpl entry = (SyndEntryImpl) feed.getEntries().get(0);
            	final String desc = entry.getDescription().toString();
            	final Source htmlSource = new Source(desc);
            	final Segment segment = new Segment(htmlSource, 0, htmlSource.length());
            	final Renderer htmlRender = new Renderer(segment).setIncludeHyperlinkURLs(true);
                str = htmlRender.toString().replace("\\n\\r", "");
            } catch (UnknownHostException e) {
                LOG.log(Level.SEVERE, null, e);
                stop();
            } catch (IOException | FeedException | IllegalArgumentException e) {
                LOG.log(Level.SEVERE, null, e);
            }
            return str;
        }

        private List<String> parseStringOfLinesToListOfLines(String stringOfLines) {
        	final List<String> list = new ArrayList<>();
            try (BufferedReader in = new BufferedReader(new StringReader(stringOfLines))) {
                in.lines().forEach(list::add);
            } catch (IOException e) {
                LOG.log(Level.SEVERE, null, e);
            }
            return list;
        }
        
        private String getStringOfNumbersFromString(String s) {
        	final StringBuilder sb = new StringBuilder();
            s.chars()
                    .mapToObj(c -> (char) c)
                    .filter(Character::isDigit)
                    .forEach(sb::append);
            return sb.toString();
        }
    }

    @Override
    public void close() {
    	stop();
        for (PropertyChangeListener pcl : pcs.getPropertyChangeListeners()) {
            pcs.removePropertyChangeListener(pcl);
        }
    }
    
    private void start() {
        failAQIOzone.getPropertyChangeSupport().addPropertyChangeListener(failTimerListenerAQIOzone);
        failAQIPPM25.getPropertyChangeSupport().addPropertyChangeListener(failTimerListenerAQIPPM25);
        failAQIPPM10.getPropertyChangeSupport().addPropertyChangeListener(failTimerListenerAQIPPM10);

        scheduler.scheduleAtFixedRate(new Update(getFeedSource(), getLocationCode()),
                DEFAULT_INITIAL_WAIT,
                DEFULT_UPDATE_RATE,
                TimeUnit.SECONDS);
        
        if (debug) {
            LOG.log(Level.INFO, "AirNowAPI scheduler started");
        }
    }

    private void stop() {
    	failAQIOzone.getPropertyChangeSupport().removePropertyChangeListener(failTimerListenerAQIOzone);
    	failAQIPPM25.getPropertyChangeSupport().removePropertyChangeListener(failTimerListenerAQIPPM25);
    	failAQIPPM10.getPropertyChangeSupport().removePropertyChangeListener(failTimerListenerAQIPPM10);
    	
		if (scheduler != null) {
			try {
				LOG.log(Level.INFO, "Initializing AirNowAPI.scheduler service termination....");
				scheduler.shutdown();
				scheduler.awaitTermination(5, TimeUnit.SECONDS);
				LOG.log(Level.INFO, "AirNowAPI.scheduler service has gracefully terminated");
			} catch (InterruptedException _) {
				scheduler.shutdownNow();
				LOG.log(Level.SEVERE, "AirNowAPI.scheduler request for shutdown has timed out after 5 seconds of waiting to terminate processes.");
				Thread.currentThread().interrupt();
			}
		}
		
		failAQIPPM25.close();		
		failAQIPPM10.close();
		failAQIOzone.close();
    }

    public static Color getBackground(int aqi) {
    	Color bg = Color.LIGHT_GRAY;
    	if (aqi >= 0 && aqi <= 50) {
    		bg = new Color(0, 255, 0);
        } else if (aqi >= 51 && aqi <= 100) {
        	bg = new Color(255, 255, 0);
        } else if (aqi >= 101 && aqi <= 150) {
        	bg = new Color(255, 126, 0);
        } else if (aqi >= 151 && aqi <= 200) {
        	bg = new Color(255, 0, 0);
        } else if (aqi >= 201 && aqi <= 300) {
        	bg = new Color(143, 63, 151);
        } else if (aqi >= 301) {
        	bg = new Color(126, 0, 35);
        }
        return bg;
    }
    
    public static Color getForeground(int aqi) {
    	Color fg = Color.BLACK;
        if (aqi >= 151) {
	        fg = Color.WHITE;
        }
        return fg;
    }
    
    public static String getToolTipText(Event event, int aqi) {
    	String tt = "NO DATA";
        if (aqi < 0 && event == Event.AQI_OZONE) {
	        tt = OZONE_SERVICE_UNAVAILABLE_STATEMENT;
    	} else if (aqi >= 0 && aqi <= 50) {
	        tt = GREEN_DEFINITION;
        } else if (aqi >= 51 && aqi <= 100) {
	        tt = YELLOW_DEFINITION;
        } else if (aqi >= 101 && aqi <= 150) {
	        tt = ORANGE_DEFINITION;
        } else if (aqi >= 151 && aqi <= 200) {
	        tt = RED_DEFINITION;
        } else if (aqi >= 201 && aqi <= 300) {
	        tt = PURPLE_DEFINITION;
        } else if (aqi >= 301) {
	        tt = MAROON_DEFINITION;
        }
        return tt;
    }
    
    public static ZonedDateTime fromEPAAirNowAPIDateTimeGroup(String dtg) {
        // Example: 07/01/23 8:00 PM EDT
    	final OffsetDateTime dateTime = OffsetDateTime.parse(dtg, DateTimeFormatter.ofPattern("MM/dd/uu hh:mm aa zzz"));
        return dateTime.toZonedDateTime();
    }

    public static ZonedDateTime fromRFC1123DateTimeGroup(String dtg) {
        // Example: Sun, 17 Dec 2023 03:15:00 EST
    	final OffsetDateTime dateTime = OffsetDateTime.parse(dtg, DateTimeFormatter.RFC_1123_DATE_TIME);
        return dateTime.toZonedDateTime();
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public PropertyChangeSupport getPropertyChangeSupport() {
        return pcs;
    }

    public void addPropertyChangeListener(final PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(final PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }
    
    private static void loadDefaultLocationCodes() {
    	airNowLocationCodeSet.clear();
    	
    	airNowLocationCodeSet.add(new AirNowLocationCode("29", 39.961176, -82.998794, "Columbus", "OH"));
    	
    	airNowLocationCodeSet.add(new AirNowLocationCode("24", 39.103119, -84.512016, "Cincinnati", "OH"));
    	airNowLocationCodeSet.add(new AirNowLocationCode("25", 41.499320, -81.694361, "Cleveland", "OH"));
    	airNowLocationCodeSet.add(new AirNowLocationCode("180", 39.758948, -84.191607, "Dayton", "OH"));
    	airNowLocationCodeSet.add(new AirNowLocationCode("502", 41.663938, -83.555212, "Toledo", "OH"));
    	airNowLocationCodeSet.add(new AirNowLocationCode("279", 41.499320, -80.649519, "Youngstown", "OH"));
    	
    	airNowLocationCodeSet.add(new AirNowLocationCode("311", 42.280826, -83.743038, "Ann Arbor", "MI"));
    	airNowLocationCodeSet.add(new AirNowLocationCode("336", 42.116706, -86.454189, "Benton Harbor", "MI"));
    	airNowLocationCodeSet.add(new AirNowLocationCode("35", 42.331427, -83.045754, "Detroit", "MI"));
    	airNowLocationCodeSet.add(new AirNowLocationCode("337", 46.308912, -86.206988, "Eastern Upper Peninsula", "MI"));
    	airNowLocationCodeSet.add(new AirNowLocationCode("312", 43.012527, -83.687456, "Flint", "MI"));
    	airNowLocationCodeSet.add(new AirNowLocationCode("48", 42.963360, -85.668086, "Grand Rapids", "MI"));
    	airNowLocationCodeSet.add(new AirNowLocationCode("338", 44.314739, -84.764750, "Houghton Lake", "MI"));
    	airNowLocationCodeSet.add(new AirNowLocationCode("313", 42.291707, -85.587229, "Kalamazoo", "MI"));
    	airNowLocationCodeSet.add(new AirNowLocationCode("314", 42.732535, -84.555535, "Lansing", "MI"));
    	airNowLocationCodeSet.add(new AirNowLocationCode("339", 43.955283, -86.452583, "Ludington", "MI"));
    	airNowLocationCodeSet.add(new AirNowLocationCode("315", 43.419470, -83.950807, "Saginaw", "MI"));
    	airNowLocationCodeSet.add(new AirNowLocationCode("316", 44.763057, -85.620632, "Traverse City", "MI"));
    	
    	airNowLocationCodeSet.add(new AirNowLocationCode("40", 37.971559, -87.571090, "Evansville", "IN"));
    	airNowLocationCodeSet.add(new AirNowLocationCode("44", 41.079273, -85.139351, "Fort Wayne", "IN"));
    	airNowLocationCodeSet.add(new AirNowLocationCode("53", 41.593370, -87.346427, "Gary", "IN"));
    	airNowLocationCodeSet.add(new AirNowLocationCode("61", 39.768403, -86.158068, "Indianapolis", "IN"));
    	airNowLocationCodeSet.add(new AirNowLocationCode("140", 41.676355, -86.2519902, "South Bend", "IN"));
    	airNowLocationCodeSet.add(new AirNowLocationCode("150", 39.466703, -87.413909, "Tarra Haute", "IN"));
    }
    
    private static AirNowLocationCode getLocationCodeElement(double latitude, double longitude) {
    	loadDefaultLocationCodes();
    	double distance = Double.MAX_VALUE;
    	int y = 0;
    	for (int x = 0; x < airNowLocationCodeSet.size(); x++) {
    		final double d = Math.abs(Vincenty.distanceToOnSurface(new Point2D.Double(longitude, latitude), new Point2D.Double(airNowLocationCodeSet.get(x).getLongitude(), airNowLocationCodeSet.get(x).getLatitide())));
    		if (d <= distance) {
    			distance = d;
    			y = x;
    		}
    	}
    	return airNowLocationCodeSet.get(y);
    }
}
