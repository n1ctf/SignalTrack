package aprs;

import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;

import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import meteorology.AbstractEnvironmentSensor;
import meteorology.AbstractEnvironmentSensor.SpeedUnit;
 
public class WUndergroundUpdater extends AbstractAPRSProcessor {
	
	public static final String DEFAULT_REST_URL = "https://rtupdate.wunderground.com";
	public static final String DEFAULT_PATH_URL = "/weatherstation/updateweatherstation.php";
	public static final String USER_AGENT = "Mozilla/5.0";
	public static final String DEFAULT_ID = "KOHCOLUM915";
	public static final String DEFAULT_KEY = "jipwVn9X";
	public static final String DEFAULT_ABBREVIATED_TAG = "WUG";
	public static final String DEFAULT_TAG = "WUG";
	
	private JTextField jtfId;			// Required. Provided in wunderground account
	private JTextField jtfKey;			// Required. Provided in wunderground account

	private AbstractEnvironmentSensor aes;

	private final Preferences userPrefs = Preferences.userRoot().node(WUndergroundUpdater.class.getName());
	
	private final ExecutorService updater = Executors.newSingleThreadExecutor();
	
	private String updateString = "";
	
	private String htmlString = "";
	
	/*
		winddir - [0-360 instantaneous wind direction]
		windspeedmph - [mph instantaneous wind speed]
		windgustmph - [mph current wind gust, using software specific time period]
		windgustdir - [0-360 using software specific time period]
		windspdmph_avg2m  - [mph 2 minute average wind speed mph]
		winddir_avg2m - [0-360 2 minute average wind direction]
		windgustmph_10m - [mph past 10 minutes wind gust mph ]
		windgustdir_10m - [0-360 past 10 minutes wind gust direction]
		humidity - [% outdoor humidity 0-100%]
		dewptf- [F outdoor dewpoint F]
		tempf - [F outdoor temperature] (for extra outdoor sensors use temp2f, temp3f, and so on)
		rainin - [rain inches over the past hour)] -- the accumulated rainfall in the past 60 min
		dailyrainin - [rain inches so far today in local time]
		baromin - [barometric pressure inches]
		weather - [text] -- metar style (+RA)
		clouds - [text] -- SKC, FEW, SCT, BKN, OVC
		soiltempf - [F soil temperature] (for sensors 2,3,4 use soiltemp2f, soiltemp3f, and soiltemp4f)
		soilmoisture - [%] (for sensors 2,3,4 use soilmoisture2, soilmoisture3, and soilmoisture4)
		leafwetness - [%] (for sensor 2 use leafwetness2)
		solarradiation - [W/m^2]
		UV - [index]
		visibility - [nm visibility]
		indoortempf - [F indoor temperature F]
		indoorhumidity - [% indoor humidity 0-100]
	 */
	
	public WUndergroundUpdater(AbstractEnvironmentSensor aes, boolean clearAllPreferences) {
		super(aes);
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				close();
			}
		});

		if (clearAllPreferences) {
			try {
				userPrefs.clear();
			} catch (final BackingStoreException ex) {
				LOG.log(Level.WARNING, ex.getMessage(), ex);
			}
		}
		
		this.aes = aes;
		
		jtfId = new JTextField();
		jtfKey = new JTextField();
		
		loadSettings();
	}
	
	@Override
	public String getUpdateString() {
		return updateString;
	}
	
	@Override
	public String getHTMLString() {
		return htmlString;
	}
	
	@Override
	public synchronized boolean sendUpdate() {
		try {
			updater.execute(new Update(aes));
			return true;
		} catch (RejectedExecutionException ex) {
			LOG.log(Level.WARNING, ex.getMessage(), ex);
			pcs.firePropertyChange(Event.TX_ERROR.name(), null, ex.getMessage());
			return false;
		}
	}
	
	@Override
	public JPanel getSettingsPanel() {
		final JPanel panel = new JPanel();

		final GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		final JLabel lblIdLabel = new JLabel("System ID");
		final JLabel lblKeyLabel = new JLabel("System Key");

		layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                .addComponent(lblIdLabel, 100, 100, 100)
                .addComponent(lblKeyLabel, 100, 100, 100))
            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                .addComponent(jtfId, 200, 200, 200)
                .addComponent(jtfKey, 200, 200, 200))
            .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
    
		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(lblIdLabel)
                    .addComponent(jtfId))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(lblKeyLabel)
                    .addComponent(jtfKey))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		return panel;
	}
	
	public void saveSettings() {
		userPrefs.put("id", jtfId.getText());
		userPrefs.put("key", jtfKey.getText());
	}
	
	private void loadSettings() {
		jtfId.setText(userPrefs.get("id", DEFAULT_ID));
		jtfKey.setText(userPrefs.get("key", DEFAULT_KEY));
	}

	public String getWUGHTMLString() {
		final StringBuilder bld = new StringBuilder();
		
		bld.append("<HTML>");
		
		bld.append("THE WEATHER UNDERGROUND REPORT at ");
		bld.append(toISO8601(aes.getZonedDateTimeUTC()));
		bld.append("<br>");
		
		bld.append("&emsp;Rest URL: ");
		bld.append(DEFAULT_REST_URL + DEFAULT_PATH_URL);
		bld.append("<br>");
		
		bld.append("&emsp;Station ID: ");
		bld.append(jtfId.getText());
		bld.append("<br>");
		
		bld.append("&emsp;Key: ");
		bld.append(jtfKey.getText());
		bld.append("<br>");
		
		bld.append("&emsp;Action: ");
		bld.append("updateraw");
		bld.append("<br>");

		bld.append("&emsp;Temperature Fahrenheit: ");
		bld.append(aes.getTempExteriorFahrenheit());
		bld.append("<br>");
		
		bld.append("&emsp;Dew Point Fahrenheit: ");
		bld.append(aes.getDewPointFahrenheit());
		bld.append("<br>");
		
		bld.append("&emsp;Humidity Percent: ");
		bld.append(aes.getExteriorHumidity());
		bld.append("<br>");
		
		bld.append("&emsp;Wind Gust Peak Speed Over Last Minute MPH: ");
		bld.append(aes.getPeakPeriodicWindSpeedMeasurement(1, SpeedUnit.MPH));
		bld.append("<br>");
		
		bld.append("&emsp;Wind Gust Peak Direction Over Last Minute: ");
		bld.append(aes.getPeakPeriodicWindDirectionMeasurement(1));
		bld.append("<br>");
		
		bld.append("&emsp;Current Wind Speed MPH: ");
		bld.append(aes.getCurrentWindSpeed(SpeedUnit.MPH));
		bld.append("<br>");
		
		bld.append("&emsp;Current Wind Direction: ");
		bld.append(aes.getWindDirectionTrue());
		bld.append("<br>");

		bld.append("&emsp;Wind Average Speed Over Last 2 Minutes MPH: ");
		bld.append(aes.getAveragePeriodicWindSpeedMeasurement(2, SpeedUnit.MPH));
		bld.append("<br>");
		
		bld.append("&emsp;Wind Average Direction Over Last 2 Minutes: ");
		bld.append(aes.getAveragePeriodicWindDirectionMeasurement(2));
		bld.append("<br>");

		bld.append("&emsp;Wind Sustained Speed Over Last 10 Minutes MPH: ");
		bld.append(aes.getSustainedPeriodicWindSpeedMeasurement(10, SpeedUnit.MPH));
		bld.append("<br>");
		
		bld.append("&emsp;Wind Sustained Direction Over Last 10 Minutes: ");
		bld.append(aes.getSustainedPeriodicWindDirectionMeasurement(10));
		bld.append("<br>");
		
		bld.append("&emsp;Station Barometric Pressure Inches Hg: ");
		bld.append(aes.getStationPressureInHg());
		bld.append("<br>");
		
		bld.append("&emsp;Rainfall Inches Last Hour: ");
		bld.append(aes.getRainFallInchesLastHour());
		bld.append("<br>");
		
		bld.append("&emsp;Rainfall Inches Last 24 Hours: ");
		bld.append(aes.getRainFallInchesLast24Hours());
		bld.append("<br>");
		
		bld.append("&emsp;Current Solar Radiation WM2: ");
		bld.append(aes.getLuminosityWM2());

		bld.append("</HTML>");
		
		return bld.toString();
	}
	
	private class Update implements Runnable {
		private final AbstractEnvironmentSensor aes;
		
		private Update(AbstractEnvironmentSensor aes) {
			this.aes = aes;
		}

		@Override
		public void run() {
			
			final UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(DEFAULT_REST_URL);
	        
	        builder.path(DEFAULT_PATH_URL);
	        
	        builder.queryParam("ID", DEFAULT_ID);
	        builder.queryParam("PASSWORD", DEFAULT_KEY);
	        builder.queryParam("action", "updateraw");
	        builder.queryParam("dateutc", "now");
	        builder.queryParam("tempf", aes.getTempExteriorFahrenheit());
	        builder.queryParam("dewptf", aes.getDewPointFahrenheit());
	        builder.queryParam("humidity", aes.getExteriorHumidity());
	        builder.queryParam("windgustmph", aes.getPeakPeriodicWindSpeedMeasurement(1, SpeedUnit.MPH));
    		builder.queryParam("windgustdir", aes.getPeakPeriodicWindDirectionMeasurement(1));
    		builder.queryParam("winddir", aes.getWindDirectionTrue());
	        builder.queryParam("windspeedmph", aes.getCurrentWindSpeed(SpeedUnit.MPH));
	        builder.queryParam("windspdmph_avg2m", aes.getAveragePeriodicWindSpeedMeasurement(2, SpeedUnit.MPH));
	        builder.queryParam("winddir_avg2m", aes.getAveragePeriodicWindDirectionMeasurement(2));
	        builder.queryParam("windgustmph_10m", aes.getSustainedPeriodicWindSpeedMeasurement(10, SpeedUnit.MPH));	
	        builder.queryParam("windgustdir_10m", aes.getSustainedPeriodicWindDirectionMeasurement(10));
	        builder.queryParam("baromin", aes.getStationPressureInHg());
	        builder.queryParam("rainin", aes.getRainFallInchesLastHour());
	        builder.queryParam("dailyrainin", aes.getRainFallInchesLast24Hours());
	        builder.queryParam("solarRadiation", aes.getLuminosityWM2());

	        final URI targetURI = builder.build().encode().toUri();
	        
	        updateString = targetURI.toString();
	        
	        htmlString = getWUGHTMLString();
	        
	        pcs.firePropertyChange(Event.TX_UPDATE.name(), null, updateString);
	        
	        try {
	        	final RestTemplate restTemplate = new RestTemplate();
	        	final String response = restTemplate.getForObject(targetURI, String.class);
	            pcs.firePropertyChange(Event.RX_UPDATE.name(), null, response);
	        } catch (RestClientException e) {
	        	LOG.log(Level.SEVERE, builder.toString(), e.getStackTrace());
	        	pcs.firePropertyChange(Event.RX_ERROR.name(), null, builder.toString());
	        }
		}
	}

	@Override
	public void close() {
		super.close();
		saveSettings();
	}

	@Override
	public String getAbbreviatedTag() {
		return DEFAULT_ABBREVIATED_TAG;
	}
	
	@Override
	public String getTag() {
		return DEFAULT_TAG;
	}
	
	@Override
	public String getClassName() {
		return this.getClass().getName();
	}
}
