package osm;

import java.awt.EventQueue;

import java.beans.PropertyChangeSupport;

import java.io.IOException;
import java.io.InputStream;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import java.util.Locale;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;

import org.xml.sax.SAXException;

import com.neovisionaries.i18n.CountryCode;

public final class OSMReverseGeoCodeAPI {
	
	public enum Event {
        NETWORK_ERROR,
        ISO3166_2_lvl4,
        ISO3166_2_lvl8,
        HOUSE_NUMBER,
        ROAD,
        NEIGHBORHOOD,
        CITY,
        MUNICIPALITY,
        COUNTY,
        STATE,
        POSTCODE,
        COUNTRY,
        COUNTRY_CODE,
        LOCALE
    }
	
	private final Executor executor = Executors.newSingleThreadExecutor();
	
	private static final Logger LOG = Logger.getLogger(OSMReverseGeoCodeAPI.class.getName());
	private static final boolean DEFAULT_DEBUG = true;
	
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	
	public void requestReverseGeoCodeEventService(double longitude, double latitude) {
		executor.execute(new ReadOSMAPI(longitude, latitude, DEFAULT_DEBUG));
	}
	
	public void requestReverseGeoCodeEventService(double longitude, double latitude, boolean debug) {
		executor.execute(new ReadOSMAPI(longitude, latitude, debug));
	}

	private final class ReadOSMAPI implements Runnable {
		private final double longitude;
		private final double latitude;
		private final boolean debug;
		
		private String ISO3166_2_lvl4;
		private String ISO3166_2_lvl8;
		private String house_number;
		private String road;
		private String neighbourhood;
		private String city;
		private String municipality;
		private String county;
		private String state;
		private String postcode;
		private String country;
		private String country_code;
		private Locale locale = Locale.ROOT;
		
		private ReadOSMAPI(double longitude, double latitude, boolean debug) {
			this.longitude = longitude;
			this.latitude = latitude;
			this.debug = debug;
		}
		
		@Override
        public void run() {	
			try {
				final URL url = getURL(longitude, latitude);
				
				if (url == null) {
					return;
				}
				
				final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
				final DocumentBuilder docBuilder = factory.newDocumentBuilder();
				
				final InputStream stream = url.openStream();
				final Document document = docBuilder.parse(stream);
				
				try {
					ISO3166_2_lvl4 = document.getElementsByTagName("ISO3166-2-lvl4").item(0).getTextContent();		
					locale = getLocaleByISO3166v2lvl4UsingCoutryCode(ISO3166_2_lvl4);
				} catch (NullPointerException ex) {
					if (debug) {
				        LOG.log(Level.INFO, "**** NO ISO3166-2-lvl4 is provided ****");
				    }
				}
				try {
					ISO3166_2_lvl8 = document.getElementsByTagName("ISO3166-2-lvl8").item(0).getTextContent();
				} catch (NullPointerException ex) {
					if (debug) {
				        LOG.log(Level.INFO, "**** NO ISO3166-2-lvl4 is provided ****");
				    }
				}
				try {
				house_number = document.getElementsByTagName("house_number").item(0).getTextContent();
				} catch (NullPointerException ex) {
					if (debug) {
				        LOG.log(Level.INFO, "**** NO House Number is provided ****");
				    }
				}
				try {
				road = document.getElementsByTagName("road").item(0).getTextContent();
				} catch (NullPointerException ex) {
					if (debug) {
				        LOG.log(Level.INFO, "**** NO Road is provided ****");
				    }
				}
				try {
				neighbourhood = document.getElementsByTagName("neighbourhood").item(0).getTextContent();
				} catch (NullPointerException ex) {
					if (debug) {
				        LOG.log(Level.INFO, "**** NO Neighborhood is provided ****");
				    }
				}
				try {
				city = document.getElementsByTagName("city").item(0).getTextContent();
				} catch (NullPointerException ex) {
					if (debug) {
				        LOG.log(Level.INFO, "**** NO City is provided ****");
				    }
				}
				try {
				municipality = document.getElementsByTagName("municipality").item(0).getTextContent();
				} catch (NullPointerException ex) {
					if (debug) {
				        LOG.log(Level.INFO, "**** NO Municipality is provided ****");
				    }
				}
				try {
				county = document.getElementsByTagName("county").item(0).getTextContent();
				} catch (NullPointerException ex) {
					if (debug) {
				        LOG.log(Level.INFO, "**** NO County is provided ****");
				    }
				}
				try {
					state = document.getElementsByTagName("state").item(0).getTextContent();
				} catch (NullPointerException ex) {
					if (debug) {
				        LOG.log(Level.INFO, "**** NO State is provided ****");
				    }
				}
				try {
				postcode = document.getElementsByTagName("postcode").item(0).getTextContent();
				} catch (NullPointerException ex) {
					if (debug) {
				        LOG.log(Level.INFO, "**** NO Postcode is provided ****");
				    }
				}
				try {
				country = document.getElementsByTagName("country").item(0).getTextContent();
				} catch (NullPointerException ex) {
					if (debug) {
				        LOG.log(Level.INFO, "**** NO Country is provided ****");
				    }
				}
				try {
				country_code = document.getElementsByTagName("country_code").item(0).getTextContent();
				} catch (NullPointerException ex) {
					if (debug) {
				        LOG.log(Level.INFO, "**** NO Country Code is provided ****");
				    }
				}
	
			} catch (IOException | ParserConfigurationException | SAXException e) {
				e.printStackTrace();
			}
			
			if (ISO3166_2_lvl4 != null) {
				pcs.firePropertyChange(Event.ISO3166_2_lvl4.name(), null, ISO3166_2_lvl4);
			}
			if (ISO3166_2_lvl8 != null) {
				pcs.firePropertyChange(Event.ISO3166_2_lvl8.name(), null, ISO3166_2_lvl8);
			}
			if (house_number != null) {
				pcs.firePropertyChange(Event.HOUSE_NUMBER.name(), null, house_number);
			}
			if (road != null) {
				pcs.firePropertyChange(Event.ROAD.name(), null, road);
			}
			if (neighbourhood != null) {
				pcs.firePropertyChange(Event.NEIGHBORHOOD.name(), null, neighbourhood);
			} 
			if (city != null) {
				pcs.firePropertyChange(Event.CITY.name(), null, city);
			} 
			if (municipality != null) {
				pcs.firePropertyChange(Event.MUNICIPALITY.name(), null, municipality);
			} 
			if (county != null) {
				pcs.firePropertyChange(Event.COUNTY.name(), null, county);
			} 
			if (state != null) {
				pcs.firePropertyChange(Event.STATE.name(), null, state);
			} 
			if (postcode != null) {
				pcs.firePropertyChange(Event.POSTCODE.name(), null, postcode);
			} 
			if (country != null) {
				pcs.firePropertyChange(Event.COUNTRY.name(), null, country);
			}
			if (country_code != null) {
				pcs.firePropertyChange(Event.COUNTRY_CODE.name(), null, country_code);
			}
			if (locale != null) {
				pcs.firePropertyChange(Event.LOCALE.name(), null, locale);
			} else {
				locale = Locale.ROOT;
			}
			
			if (debug) {
				LOG.log(Level.INFO, "{0} {1} {2} {3} {4} {5} {6} {7} {8} {9} {10} {11} {12} {13} {14} {15} {16} {17} {18} " +
					"{19} {20} {21} {22} {23} {24} {25} {26} {27}", new Object[] {
					"----------------- OSM Reverse Geocode API -----------------",
					"\n    ISO3166-2-lvl4: ",
					ISO3166_2_lvl4,
					"\n    ISO3166-2-lvl8: ",
					ISO3166_2_lvl8,
					"\n    House Number: ",
					house_number,
					"\n    Road: ",
					road,
					"\n    Neighborhood: ",
					neighbourhood,
					"\n    City: ",
					city,
					"\n    Municipality: ",
					municipality,
					"\n    County: ",
					county,
					"\n    State: ",
					state,
					"\n    Post Code: ",
					postcode,
					"\n    Country : ",
					country,
					"\n    Country Code: ",
					country_code,
					"\n    Locale: ",
					locale.getDisplayName(),
					"\n------------- END OSM Reverse Geocode API ---------------"
				});
			}
        }	
		
		private URL getURL(double longitude, double latitude) {
			final StringBuilder sb = new StringBuilder();
			try {
				// example: https://nominatim.openstreetmap.org/reverse?format=xml&lat=52.5487429714954&lon=-1.81602098644987&
				//          zoom=18&addressdetails=1
				sb.append("https://nominatim.openstreetmap.org/reverse?format=xml&lat=");
				sb.append(String.valueOf(latitude));
				sb.append("&lon=");
				sb.append(String.valueOf(longitude));
				sb.append("&zoom=18&addressdetails=1");
		    	return new URI(sb.toString()).toURL();
			} catch (URISyntaxException e) {
				LOG.log(Level.SEVERE, null, e);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
	    	return null;
	    }
	}
	
	public static Locale getLocaleByISO3166v2lvl4(String iso3166) {
		Locale locale = Locale.ROOT;
		for (Locale l : Locale.getAvailableLocales()) {
			if (l.getCountry().equals(iso3166.substring(0, 2))) {
				locale = l;
			}
		}
		return locale;
	}
	
	public static Locale getLocaleByISO3166v2lvl4UsingCoutryCode(String iso3166) {
		return CountryCode.getByAlpha2Code(iso3166.substring(0, 2)).toLocale();			
	}
	
	public PropertyChangeSupport getPropertyChangeSupport() {
		return pcs;
	} 
	
    public static void main(String[] args) {
		EventQueue.invokeLater(() -> new OSMReverseGeoCodeAPI().requestReverseGeoCodeEventService(-83.074465, 40.093618333));
	}
}
