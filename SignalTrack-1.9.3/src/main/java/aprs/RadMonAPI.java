/**
 * RadMonAPI
 * @author John R. Chartkoff
 * @version 1.9.4,  &nbsp; 
 * @since SDK1.4
 */

package aprs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

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

public class RadMonAPI extends AbstractAPRSProcessor {
	public static final String DEFAULT_REST_URL = "https://radmon.org/radmon.php?";
	public static final String USER_AGENT = "Mozilla/5.0";
	public static final String DEFAULT_USERNAME = "N1CTF";
	public static final String DEFAULT_PASSWORD = "q7FVm89HeeD";
	public static final String UNIT_CPM = "CPM";
	public static final double DEFAULT_STATION_LATITUDE = 40.093727;
	public static final double DEFAULT_STATION_LONGITUDE = -083.074661;
	public static final double DEFAULT_CONVERSION_FACTOR = 0.00812;
	public static final int DEFAULT_ALERT_CPM = 50;
	public static final int DEFAULT_WARNING_CPM = 100;
	public static final String DEFAULT_ABBREVIATED_TAG = "RadM";
	public static final String DEFAULT_TAG = "RadMon";
	
	private static final long serialVersionUID = 242932472098757019L;
	private static final Preferences userPrefs = Preferences.userRoot().node(RadMonAPI.class.getName());
	private final ExecutorService updater = Executors.newSingleThreadExecutor();
	
	private Double stationLatitude = DEFAULT_STATION_LATITUDE;
	private Double stationLongitude = DEFAULT_STATION_LONGITUDE;
	
	private final JTextField jtfUserName = new JTextField();
	private final JTextField jtfPassword = new JTextField();					
	private final JTextField jtfConversionFactor = new JTextField(String.valueOf(DEFAULT_CONVERSION_FACTOR)); 
	private final JTextField jtfAlertCPM = new JTextField(String.valueOf(DEFAULT_ALERT_CPM));
	private final JTextField jtfWarningCPM = new JTextField(String.valueOf(DEFAULT_WARNING_CPM));
	
	private int cpm = -1;
	
	private String htmlString = "";
	
	private String updateString = "";
	
	private String restURL = DEFAULT_REST_URL;
	
	private AbstractEnvironmentSensor aes;
	
	public RadMonAPI(AbstractEnvironmentSensor aes, Boolean clearAllPreferences) {
		super(aes);
		
		this.aes = aes;
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				close();
			}
		});

		if (Boolean.TRUE.equals(clearAllPreferences)) {
			try {
				userPrefs.clear();
			} catch (final BackingStoreException ex) {
				LOG.log(Level.WARNING, ex.getMessage(), ex);
			}
		}
	
		loadSettings();
		
		setStationLongitude(aes.getStationLongitudeDegrees());
		setStationLatitude(aes.getStationLatitudeDegrees());
		setConversionFactor();
		setWarningAlert();
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
		        if (responseCode == HttpURLConnection.HTTP_OK) { // success
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

	private synchronized void sendUpdate(String updateString) {
		try {
			this.updateString = updateString;
			htmlString = getRadMonHTMLString();
			if (updateString != null) {	
				updater.execute(new Update(updateString));
			} else {
				pcs.firePropertyChange(Event.TX_ERROR.name(), null, null);
			}
		} catch (RejectedExecutionException ex) {
			LOG.log(Level.WARNING, ex.getMessage(), ex);
			pcs.firePropertyChange(Event.TX_ERROR.name(), null, ex.getMessage());
		}
	}
	
	@Override
	public JPanel getSettingsPanel() {
		final JPanel panel = new JPanel();

		final GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		final JLabel lblUserNameLabel = new JLabel("User Name");
		final JLabel lblPasswordLabel = new JLabel("Password");
		final JLabel lblConversionFactorLabel = new JLabel("Conversion Factor");
		final JLabel lblAlertCPMLabel = new JLabel("Alert On or Above Counts per Minute");
		final JLabel lblWarningCPMLabel = new JLabel("Warn On or Above Counts per Minute");
		
		layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
	            .addContainerGap()
	            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
	            	.addComponent(lblUserNameLabel, 290, 290, 290)
	            	.addComponent(lblPasswordLabel, 290, 290, 290)
	            	.addComponent(lblConversionFactorLabel, 290, 290, 290)
	            	.addComponent(lblAlertCPMLabel, 290, 290, 290)
	            	.addComponent(lblWarningCPMLabel, 290, 290, 290))
	            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
	            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
	            	.addComponent(jtfUserName, 150, 150, 150)
	            	.addComponent(jtfPassword, 150, 150, 150)
	            	.addComponent(jtfConversionFactor, 80, 80, 80)
	            	.addComponent(jtfAlertCPM, 40, 40, 40)
	            	.addComponent(jtfWarningCPM, 40, 40, 40))
	            .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(lblUserNameLabel)
                    .addComponent(jtfUserName))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPasswordLabel)
                    .addComponent(jtfPassword))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED) 
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(lblConversionFactorLabel)
                    .addComponent(jtfConversionFactor))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(lblAlertCPMLabel)
                    .addComponent(jtfAlertCPM))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(lblWarningCPMLabel)
                    .addComponent(jtfWarningCPM))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		return panel;
	}
	
	public void saveSettings() {
		userPrefs.put("username", jtfUserName.getText());
		userPrefs.put("password", jtfPassword.getText());
		userPrefs.putInt("alertCPM", Integer.parseInt(jtfAlertCPM.getText()));
		userPrefs.putInt("warningCPM", Integer.parseInt(jtfWarningCPM.getText()));
		userPrefs.putDouble("conversionFactor", Double.parseDouble(jtfConversionFactor.getText()));
	}

	private void loadSettings() {
		jtfUserName.setText(userPrefs.get("username", DEFAULT_USERNAME));
		jtfPassword.setText(userPrefs.get("password", DEFAULT_PASSWORD));
		jtfAlertCPM.setText(String.valueOf(userPrefs.getInt("alertCPM", DEFAULT_ALERT_CPM)));
		jtfWarningCPM.setText(String.valueOf(userPrefs.getInt("warningCPM", DEFAULT_WARNING_CPM)));
		jtfConversionFactor.setText(String.valueOf(userPrefs.getDouble("conversionFactor", DEFAULT_CONVERSION_FACTOR)));		
	}
	
	/**
	 * Sets the password that is transmitted with each submission to the RadMon server
	 * @param password The password selected when creating your account
	 */
	public void setPassword(String password) {
		this.jtfPassword.setText(password);
	}

	/**
	 * Sets the user name that is transmitted with each submission to the RadMon server
	 * @param userName The user name selected when creating your account
	 */
	public void setUserName(String userName) {
		this.jtfUserName.setText(userName);
	}

	public String getRestURL() {
		return restURL;
	}

	public void setRestURL(String restURL) {
		this.restURL = restURL;
	}

	public Double getStationLatitude() {
		return stationLatitude;
	}

	public Double getStationLongitude() {
		return stationLongitude;
	}

	public void setStationLatitude(Double stationLatitude) {
		this.stationLatitude = stationLatitude;
	}

	public void setStationLongitude(Double stationLongitude) {
		this.stationLongitude = stationLongitude;
	}

	public int getAlertCPM() {
		return Integer.parseInt(jtfAlertCPM.getText());
	}

	public int getWarningCPM() {
		return Integer.parseInt(jtfWarningCPM.getText());
	}

	public void setAlertCPM(int alertCPM) {
		this.jtfAlertCPM.setText(String.valueOf(alertCPM));
	}

	public void setWarningCPM(int warningCPM) {
		this.jtfWarningCPM.setText(String.valueOf(warningCPM));
	}

	public long getSerialVersionUID() {
		return serialVersionUID;
	}
	
	public Double getConversionFactor() {
		return Double.parseDouble(jtfConversionFactor.getText());
	}

	public void setStationLatitudeLongitude() {
		setStationLatitudeLongitude(getStationLatitude(), getStationLongitude());
	}
	
	// function=setlatitudelongitude&user=Simomax&password=datasendingpassword&latitude=53.80951&longitude=-3.014777
	public void setStationLatitudeLongitude(double stationLatitude, double stationLongitude) {
		this.stationLatitude = stationLatitude;
		this.stationLongitude = stationLongitude;

		final StringBuilder bld = new StringBuilder();
		
		bld.append(DEFAULT_REST_URL);
		
		bld.append("function=" + Function.SET_LAT_LON.name());
		
		bld.append("&user=");
		bld.append(jtfUserName.getText());
		
		bld.append("&password=");
		bld.append(jtfPassword.getText());
		
		bld.append("&latitude=");
		bld.append(toDecimalFormat(getStationLatitude(), 6));
		
		bld.append("&longitude=");
		bld.append(toDecimalFormat(getStationLongitude(), 6));
		
		sendUpdate(bld.toString());
	}

	// function=submit&user=Simomax&password=datasendingpassword&value=100&unit=CPM
	/**
	 * @param cpm
	 */
	public void submitCPM(int cpm) {
		this.cpm = cpm;
		
		if (cpm > -1) {
			final StringBuilder bld = new StringBuilder();
			
			bld.append(DEFAULT_REST_URL);
			
			bld.append("function=" + Function.SUBMIT.name());
			
			bld.append("&user=");
			bld.append(jtfUserName.getText());
			
			bld.append("&password=");
			bld.append(jtfPassword.getText());
			
			bld.append("&value=");
			bld.append(String.valueOf(cpm));
			
			bld.append("&unit=");
			bld.append(UNIT_CPM);
			
			sendUpdate(bld.toString());
		}
	}

	public void submitCPMWithCurrentLatitudeLongitude(int cpm) {
		submitCPMWithCurrentLatitudeLongitude(cpm, stationLatitude, stationLongitude);
	}
	
	// function=submit&user=Simomax&password=datasendingpassword&value=100&unit=CPM&latitude=54.8097&longitude=-2.01445
	public void submitCPMWithCurrentLatitudeLongitude(int cpm, double latitude, double longitude) {
		this.cpm = cpm;
		
		if (cpm > -1) {
			final StringBuilder bld = new StringBuilder();
			
			bld.append(DEFAULT_REST_URL);
			
			bld.append("function=" + Function.SUBMIT.name());
			
			bld.append("&user=");
			bld.append(jtfUserName.getText());
			
			bld.append("&password=");
			bld.append(jtfPassword.getText());
			
			bld.append("&value=");
			bld.append(String.valueOf(cpm));
			
			bld.append("&unit=");
			bld.append(UNIT_CPM);
			
			bld.append("&latitude=");
			bld.append(toDecimalFormat(latitude, 6));
			
			bld.append("&longitude=");
			bld.append(toDecimalFormat(longitude, 6));
			
			sendUpdate(bld.toString());
		}
	}
	
	public void setConversionFactor() {
		setConversionFactor(Double.parseDouble(jtfConversionFactor.getText()));
	}
	
	// function=setconversionfactor&user=Simomax&password=datasendingpassword&value=0.00833
	public void setConversionFactor(double conversionFactor) {
		this.jtfConversionFactor.setText(String.valueOf(conversionFactor));
		
		final StringBuilder bld = new StringBuilder();
		
		bld.append(DEFAULT_REST_URL);
		
		bld.append("function=" + Function.SET_CONVERSION_FACTOR.name());
		
		bld.append("&user=");
		bld.append(jtfUserName.getText());
		
		bld.append("&password=");
		bld.append(jtfPassword.getText());
		
		bld.append("&value=");
		bld.append(String.valueOf(conversionFactor));
		
		sendUpdate(bld.toString());
	}
	
	public void setWarningAlert() {
		setWarningAlert(getWarningCPM(), getAlertCPM());
	}
	
	// function=setwarnalert&user=Simomax&password=datasendingpassword&warn=70&alert=120
	public void setWarningAlert(int warningCPM, int alertCPM) {
		this.jtfWarningCPM.setText(String.valueOf(warningCPM));
		this.jtfAlertCPM.setText(String.valueOf(alertCPM));
		
		final StringBuilder bld = new StringBuilder();
		
		bld.append(DEFAULT_REST_URL);
		
		bld.append("function=" + Function.SET_WARN_ALERT.name());
		
		bld.append("&user=");
		bld.append(jtfUserName.getText());
		
		bld.append("&password=");
		bld.append(jtfPassword.getText());
		
		bld.append("&warn=");
		bld.append(getWarningCPM());
		
		bld.append("&alert=");
		bld.append(getAlertCPM());
		
		sendUpdate(bld.toString());
	}		
	
	// function=setalertsenabled&user=Simomax&password=datasendingpassword
	public void setAlertsEnabled() {
		final StringBuilder bld = new StringBuilder();
		
		bld.append(DEFAULT_REST_URL);
		
		bld.append("function=" + Function.SET_ALERTS_ENABLED.name());
		
		bld.append("&user=");
		bld.append(jtfUserName.getText());
		
		bld.append("&password=");
		bld.append(jtfPassword.getText());
		
		sendUpdate(bld.toString());
	}	
	
	// function=setalertsdisabled&user=Simomax&password=datasendingpassword
	public void setAlertsDisabled() {
		final StringBuilder bld = new StringBuilder();
		
		bld.append(DEFAULT_REST_URL);
		
		bld.append("function=" + Function.SET_ALERTS_DISABLED.name());
		
		bld.append("&user=");
		bld.append(jtfUserName.getText());
		
		bld.append("&password=");
		bld.append(jtfPassword.getText());
		
		sendUpdate(bld.toString());
	}	
	
	@Override
	public boolean sendUpdate() {
		if (aes.getCountsPerMinute() > -1) {
			sendUpdate(aes.getCountsPerMinute());
			return true;
		}
		return false;
	}
	
	public void sendUpdate(int cpm) {
		submitCPMWithCurrentLatitudeLongitude(cpm);
	}
	
	public String getRadMonHTMLString() {
		final StringBuilder bld = new StringBuilder();
		
		bld.append("<HTML>");
		
		bld.append("RadMon Report at ");
		bld.append(toISO8601(getEnvironmentSensor().getZonedDateTimeUTC()));
		bld.append("<br>");
		
		bld.append("&emsp;Rest URL: ");
		bld.append(DEFAULT_REST_URL);
		bld.append("<br>");
		
		bld.append("&emsp;User Name: ");
		bld.append(jtfUserName.getText());
		bld.append("<br>");
		
		bld.append("&emsp;Password: ");
		bld.append(jtfPassword.getText());
		bld.append("<br>");
		
		bld.append("&emsp;Latitude: ");
		bld.append(stationLatitude);
		bld.append("<br>");
		
		bld.append("&emsp;Longitude: ");
		bld.append(stationLongitude);
		bld.append("<br>");
		
		bld.append("&emsp;Counts per Minute: ");
		bld.append(cpm);
		bld.append("<br>");
		
		bld.append("</HTML>");
		
		return bld.toString();
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
	public String getAbbreviatedTag() {
		return DEFAULT_ABBREVIATED_TAG;
	}
	
	@Override
	public String getTag() {
		return DEFAULT_TAG;
	}
	
	@Override
	public void close() {
		super.close();
		saveSettings();
	}

	@Override
	public String getClassName() {
		return this.getClass().getName();
	}
}
