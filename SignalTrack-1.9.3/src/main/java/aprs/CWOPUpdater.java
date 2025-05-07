package aprs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import java.time.ZonedDateTime;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import java.util.logging.Level;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.net.ssl.HttpsURLConnection;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;

import meteorology.AbstractEnvironmentSensor;
import meteorology.Meteorology;
import meteorology.AbstractEnvironmentSensor.SpeedUnit;

// https://send.cwop.rest/?id=CW0001&lat=38.892954&long=−‌77.0328578&time=2024-10-09T03:02:46.624Z&tempf=85&windspeedmph=1.3&
// windgustmph=3.3&winddir=144&pressure=1004.2&humidity=62&solarradiation=42&rainin=0&dailyrainin=5.8&last24hrrainin=7.1 
 
public class CWOPUpdater extends AbstractAPRSProcessor {
	
	public static final String DEFAULT_REST_URL = "https://send.cwop.rest/";
	public static final String USER_AGENT = "Mozilla/5.0";
	public static final String DEFAULT_ID = "N1CTF";
	public static final String DEFAULT_ABBREVIATED_TAG = "CWOP";
	public static final String DEFAULT_TAG = "CWOP";
		
	private JTextField jtfId;			// Required. Your CWOP or Ham Radio ID.
	private Double lat;					// Required. Your weather station's latitude in decimal degrees.
	private Double lon;					// Required. Your weather station's longitude in decimal degrees.
	private ZonedDateTime time;			// Required. The time that your weather reading was taken in ISO Date or Epoch (milliseconds) format.
	private Double tempf;				// Required. The temperature in degrees fahrenheit.
	private Double windSpeedMPH;		// Required. The wind speed in miles per hour.
	private Double windGustMPH;			// Required. The wind gust in miles per hour.
	private Double windDir;				// Required. The direction that the wind is coming from, 0–359 degrees.
	private Double pressureMillibars;	// Optional. The barometer pressure in Hectopascals/Millibars.
	private Double humidity;			// Optional. The relative humidity from 0 to 100 percent.
	private Double solarRadiation; 		// Optional. The solar radiation in W/m².
	private Double lastHourRainInches;	// Optional. The amount of rain that has fallen over the past hour in inches.
	private Double dailyRainInches;		// Optional. The amount of rain that has fallen since midnight in inches.
	private Double last24HrRainInches;	// Optional. The amount of rain that has fallen over the past 24 hours in inches.
	
	private String cwopUpdateString;
	private String cwopHTMLString;

	private AbstractEnvironmentSensor aes;
	private final Preferences userPrefs = Preferences.userRoot().node(CWOPUpdater.class.getName());
	
	private final ExecutorService updater = Executors.newSingleThreadExecutor();
	
	public CWOPUpdater(AbstractEnvironmentSensor aes, boolean clearAllPreferences) {
		super(aes);
		this.aes = aes;
		
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
		
		jtfId = new JTextField();
		
		loadSettings();
	}

	private String getCWOPUpdateString() {
		if (testForRequiredElements()) {
			
			final StringBuilder bld = new StringBuilder();
			
			bld.append(DEFAULT_REST_URL);
			
			bld.append("?id=");
			bld.append(jtfId.getText());
			
			bld.append("&lat=");
			bld.append(AbstractEnvironmentSensor.toDecimalFormat(lat, 6));
			
			bld.append("&long=");
			bld.append(AbstractEnvironmentSensor.toDecimalFormat(lon, 6));
			
			bld.append("&time=");
			bld.append(toISO8601(time));
			
			bld.append("&tempf=");
			bld.append(AbstractEnvironmentSensor.toDecimalFormat(tempf, 1));
			
			bld.append("&windspeedmph=");
			bld.append(AbstractEnvironmentSensor.toDecimalFormat(windSpeedMPH, 1));
			
			bld.append("&windgustmph=");
			bld.append(AbstractEnvironmentSensor.toDecimalFormat(windGustMPH, 1));
			
			bld.append("&winddir=");
			bld.append(AbstractEnvironmentSensor.toDecimalFormat(windDir, 0));

			if (pressureMillibars != null) {
				bld.append("&pressure=");
				bld.append(AbstractEnvironmentSensor.toDecimalFormat(pressureMillibars, 1));
			}
			
			if (humidity != null) {
				bld.append("&humidity=");
				bld.append(AbstractEnvironmentSensor.toDecimalFormat(humidity, 1));
			}
			
			if (solarRadiation != null) {
				bld.append("&solarradiation=");
				bld.append(AbstractEnvironmentSensor.toDecimalFormat(solarRadiation, 0));
			}
			
			if (lastHourRainInches != null) {
				bld.append("&rainin=");
				bld.append(AbstractEnvironmentSensor.toDecimalFormat(lastHourRainInches, 1));
			}
			
			if (dailyRainInches != null) {
				bld.append("&dailyrainin=");
				bld.append(AbstractEnvironmentSensor.toDecimalFormat(dailyRainInches, 1));
			}
			
			if (last24HrRainInches != null) {
				bld.append("&last24hrrainin=");
				bld.append(AbstractEnvironmentSensor.toDecimalFormat(last24HrRainInches, 1));
			}
			
			return bld.toString();
		}
		return null;
	}
	
	private String getCWOPHTMLString() {
		final StringBuilder bld = new StringBuilder();
		
		bld.append("<HTML>");
		
		bld.append("CITIZEN WEATHER OBSERVER REPORT at ");
		bld.append(toISO8601(time));
		bld.append("<br>");
		
		bld.append("&emsp;Station ID: ");
		bld.append(jtfId.getText());
		bld.append("<br>");
		
		bld.append("&emsp;Latitude: ");
		bld.append(AbstractEnvironmentSensor.toDecimalFormat(lat, 6));
		bld.append("<br>");
		
		bld.append("&emsp;Longitude: ");
		bld.append(AbstractEnvironmentSensor.toDecimalFormat(lon, 6));
		bld.append("<br>");
		
		bld.append("&emsp;Temperature Fahrenheit: ");
		bld.append(AbstractEnvironmentSensor.toDecimalFormat(tempf, 1));
		bld.append("<br>");
		
		bld.append("&emsp;Wind Speed MPH: ");
		bld.append(AbstractEnvironmentSensor.toDecimalFormat(windSpeedMPH, 1));
		bld.append("<br>");
		
		bld.append("&emsp;Wind Gust MPH: ");
		bld.append(AbstractEnvironmentSensor.toDecimalFormat(windGustMPH, 1));
		bld.append("<br>");
		
		bld.append("&emsp;Wind Direction: ");
		bld.append(AbstractEnvironmentSensor.toDecimalFormat(windDir, 0));
		bld.append("<br>");
		
		if (pressureMillibars != null) {
			bld.append("&emsp;Pressure Millibars: ");
			bld.append(AbstractEnvironmentSensor.toDecimalFormat(pressureMillibars, 1));
			bld.append("<br>");
		}
		
		if (humidity != null) {
			bld.append("&emsp;Humidity: ");
			bld.append(AbstractEnvironmentSensor.toDecimalFormat(humidity, 1));
			bld.append("<br>");
		}
		
		if (solarRadiation != null) {
			bld.append("&emsp;Solar Radiation WM2: ");
			bld.append(AbstractEnvironmentSensor.toDecimalFormat(solarRadiation, 0));
			bld.append("<br>");
		}
		
		if (lastHourRainInches != null) {
			bld.append("&emsp;Rain Inches Last Hour: ");
			bld.append(AbstractEnvironmentSensor.toDecimalFormat(lastHourRainInches, 1));
			bld.append("<br>");
		}
		
		if (dailyRainInches != null) {
			bld.append("&emsp;Rain Inches This Day: ");
			bld.append(AbstractEnvironmentSensor.toDecimalFormat(dailyRainInches, 1));
			bld.append("<br>");
		}
		
		if (last24HrRainInches != null) {
			bld.append("&emsp;Rain Inches Last 24 HR: ");
			bld.append(AbstractEnvironmentSensor.toDecimalFormat(last24HrRainInches, 1));
			bld.append("<br>");
		}
		bld.append("</HTML>");
		
		return bld.toString();
	}
	
	@Override
	public synchronized boolean sendUpdate() {
		try {
			setLat(aes.getStationLatitudeDegrees());
			setLon(aes.getStationLongitudeDegrees());
			setTime(aes.getZonedDateTimeUTC());
			setTempf(aes.getTempExteriorFahrenheit());
			setWindSpeedMPH(aes.getCurrentWindSpeed(SpeedUnit.MPH));
			setWindGustMPH(aes.getGustingWindSpeed(SpeedUnit.MPH));
			setWindDir(aes.getWindDirectionTrue());
			setPressureMillibars(aes.getBarometricPressureRelativeHPA());
			setHumidity(aes.getExteriorHumidity());
			setSolarRadiation(aes.getLuminosityWM2());
			setLastHourRainInches(Meteorology.convertMillimetersToInches(aes.getRainfallMillimetersLastHour()));
			setLast24HrRainInches(Meteorology.convertMillimetersToInches(aes.getRainfallMillimetersLast24Hours()));
			setDailyRainInches(aes.getDailyRainInches());
			
			final String str = getCWOPUpdateString();
			
			cwopUpdateString = str;
			
			cwopHTMLString = getCWOPHTMLString();
			if (str != null) {
				updater.execute(new Update(str));
			} else {
				pcs.firePropertyChange(Event.TX_ERROR.name(), null, null);
			}
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

		final JLabel lblIdLabel = new JLabel("Station ID");

        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(80, 80, 80)
                .addComponent(lblIdLabel, 80, 80, 80)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jtfId, 120, 120, 120)
                .addContainerGap(230, Short.MAX_VALUE)
            )
        );
        
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(100, 100, 100)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(lblIdLabel)
                    .addComponent(jtfId))
                .addContainerGap(180, Short.MAX_VALUE)
            )
        );

		return panel;
	}
	
	public void saveSettings() {
		userPrefs.put("id", jtfId.getText());
	}
	
	private void loadSettings() {
		jtfId.setText(userPrefs.get("id", DEFAULT_ID));
	}
	
	public void setId(String id) {
		this.jtfId.setText(id);
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public void setLon(double lon) {
		this.lon = lon;
	}

	public void setTime(ZonedDateTime time) {
		this.time = time;
	}

	public void setTempf(double tempf) {
		this.tempf = tempf;
	}

	public void setWindSpeedMPH(double windSpeedMPH) {
		this.windSpeedMPH = windSpeedMPH;
	}

	public void setWindGustMPH(double windGustMPH) {
		this.windGustMPH = windGustMPH;
	}

	public void setWindDir(double windDir) {
		this.windDir = windDir;
	}

	public void setPressureMillibars(double pressureMillibars) {
		this.pressureMillibars = pressureMillibars;
	}

	public void setHumidity(double humidity) {
		this.humidity = humidity;
	}

	public void setSolarRadiation(double solarRadiation) {
		this.solarRadiation = solarRadiation;
	}

	public void setLastHourRainInches(double lastHourRainInches) {
		this.lastHourRainInches = lastHourRainInches;
	}

	public void setDailyRainInches(double dailyRainInches) {
		this.dailyRainInches = dailyRainInches;
	}

	public void setLast24HrRainInches(double last24HrRainInches) {
		this.last24HrRainInches = last24HrRainInches;
	}

	@Override
	public String getUpdateString() {
		return cwopUpdateString;
	}
	
	@Override
	public String getHTMLString() {
		return cwopHTMLString;
	}
	
	private boolean testForRequiredElements() {		
		return (jtfId != null && lat != null && lon != null && time != null && tempf != null && windSpeedMPH != null && 
			windGustMPH != null && windDir != null);
	}
	
	private class Update implements Runnable {
		private final String statement;
		
		private Update(String statement) {
			this.statement = statement;
		}

		@Override
		public void run() {
			try {
				final URL obj = URI.create(statement).toURL();
				final HttpsURLConnection httpsURLConnection = (HttpsURLConnection) obj.openConnection();
		        httpsURLConnection.setRequestMethod("GET");
		        httpsURLConnection.setRequestProperty("User-Agent", USER_AGENT);
		        LOG.log(Level.INFO, "Request Sent: {0}", statement);
		        pcs.firePropertyChange(Event.TX_UPDATE.name(), null, statement);
		        final int responseCode = httpsURLConnection.getResponseCode();
		        LOG.log(Level.INFO, "GET Response Code: {0}", responseCode);
		        if (responseCode == HttpURLConnection.HTTP_OK) {
		        	final BufferedReader in = new BufferedReader(new InputStreamReader(httpsURLConnection.getInputStream()));
		            final StringBuilder response = new StringBuilder();
		            in.lines().forEach(response::append); 
		            in.close();
		            LOG.log(Level.INFO, "Response Received: {0}", response);
					pcs.firePropertyChange(Event.RX_UPDATE.name(), null, response.toString());
		            LOG.log(Level.INFO, "GET Response: {0}", responseCode);
		        } else {
		        	LOG.log(Level.INFO, "GET Response Error: {0}", responseCode);
		        	pcs.firePropertyChange(Event.RX_ERROR.name(), null, responseCode);
		        }
			} catch (IOException | IllegalArgumentException ex) {
				LOG.log(Level.WARNING, ex.getLocalizedMessage(), ex);
				pcs.firePropertyChange(Event.HTTP_ERROR.name(), null, ex.getMessage());
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
