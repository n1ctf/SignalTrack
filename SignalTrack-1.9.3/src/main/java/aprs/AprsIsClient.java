package aprs;

import java.net.UnknownHostException;

import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import java.util.logging.Level;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.JPanel;

import meteorology.AbstractEnvironmentSensor;
import meteorology.Meteorology;

import meteorology.AbstractEnvironmentSensor.SpeedUnit;

import network.NetworkParameterSet;

import tcp.TCPClient;

public class AprsIsClient extends AbstractAPRSProcessor {

	public static final boolean DEFAULT_DEBUG = true;
	public static final int DEFAULT_NETWORK_PARAMETER_SELECT = 1;
	public static final String DEFAULT_CALLSIGN = "N1CTF-6";
	public static final String DEFAULT_PASSWORD = "14471";
	public static final String SERVER_CONNECT_STRING = "# aprsc";
	public static final String DEFAULT_EQUIPMENT_ID = "SignalTrack-1.9.3_Ecowitt-WS90";
	public static final int MAX_HOSTS = 12;
	public static final String DEFAULT_ABBREVIATED_TAG = "IS";
	public static final String DEFAULT_TAG = "APRS-IS";

	private final Preferences userPrefs = Preferences.userRoot().node(AprsIsClient.class.getName());
	private final AbstractEnvironmentSensor aes;
	
	private List<NetworkParameterSet> netParams = new ArrayList<>(12);
	private int netParamSelect = DEFAULT_NETWORK_PARAMETER_SELECT;
	private TCPClient tcpClient;
	private boolean loginVerified;
	private boolean connectionAccepted;
	private String htmlString;
	private String updateString;
	private String callSign = DEFAULT_CALLSIGN;
	private String password = DEFAULT_PASSWORD;
	private String equipmentId = DEFAULT_EQUIPMENT_ID;

	public AprsIsClient(AbstractEnvironmentSensor aes, boolean clearAllPreferences) {
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
		
		loadSettings();
		initializeServices();
		initListeners();
	}

	private void initializeServices() {
		tcpClient = new TCPClient(StandardCharsets.UTF_8);
		tcpClient.connect(netParams.get(netParamSelect).getInetAddress(), netParams.get(netParamSelect).getPortNumber());
	}

	private void initListeners() {
		tcpClient.getPropertyChangeSupport().addPropertyChangeListener(event -> {
			if (TCPClient.Event.CONNECTION_DROPPED.name().equals(event.getPropertyName())) {
				// NOOP
			}
			if (TCPClient.Event.CONNECTION_ACCEPTED.name().equals(event.getPropertyName())) {
				// NOOP
			}
			if (TCPClient.Event.DATA_RECEIVED.name().equals(event.getPropertyName())) {
				pcs.firePropertyChange(Event.RX_UPDATE.name(), event.getOldValue(), event.getNewValue());
				final String data = (String) event.getNewValue();
				if ((!connectionAccepted && data.contains(SERVER_CONNECT_STRING))) {
					connectionAccepted = true;
					LOG.log(Level.INFO, "Initial Response from Server Has Been Received: {0}", data);
					tcpClient.write(getAuthenticationString());
				} else if (!loginVerified && connectionAccepted && (data).contains(getAuthenticationVerificationString())) {
					loginVerified = true;
					LOG.log(Level.INFO, "Login Has Been Verified: {0}", event.getNewValue());
				}
			}
			if (TCPClient.Event.DATA_TRANSMITTED.name().equals(event.getPropertyName())) {
				pcs.firePropertyChange(Event.TX_UPDATE.name(), null, event.getNewValue());
			}
		});
	}

	@Override
	public void close() {
		super.close();
		saveSettings();
		tcpClient.close();
	}

	public List<NetworkParameterSet> getNetParams() {
		return netParams;
	}

	public void setNetParams(List<NetworkParameterSet> netParams) {
		this.netParams = netParams;
	}

	public int getNetParamSelect() {
		return netParamSelect;
	}

	public void setNetParamSelect(int netParamSelect) {
		this.netParamSelect = netParamSelect;
	}

	public String getCallSign() {
		return callSign;
	}

	public void setCallSign(String callSign) {
		this.callSign = callSign;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEquipmentId() {
		return equipmentId;
	}

	public void setEquipmentId(String equipmentId) {
		this.equipmentId = equipmentId;
	}

	private String getAuthenticationVerificationString() {
		final StringBuilder bld = new StringBuilder();
		bld.append("# logresp ");
		bld.append(callSign);
		bld.append(" verified");
		return bld.toString();
	}

	private byte[] getAuthenticationString() {
		// Example: user YOURCALLSIGN-6 pass YOURPASSWORD vers ESP8266Weather 1
		final StringBuilder bld = new StringBuilder();
		bld.append("user ");
		bld.append(callSign);
		bld.append(" pass ");
		bld.append(password);
		bld.append("\r\n");
		return bld.toString().getBytes(StandardCharsets.UTF_8);
	}

	public void saveSettings() {
		int i = 0;
		try {
			for (i = 0; i < MAX_HOSTS; i++) {
				userPrefs.put("hostName" + i, netParams.get(i).getHostName());
				userPrefs.putInt("portNumber" + i, netParams.get(i).getPortNumber());
				userPrefs.put("description", netParams.get(i).getDescription());
			}
		} catch (IndexOutOfBoundsException _) {
			LOG.log(Level.WARNING, "Number of saved APRS/IS addresses: {0} is less than list size because {1} servers were not available.", 
					new Object[] {i, MAX_HOSTS - i});
		}
		userPrefs.put("callSign", callSign);
		userPrefs.put("password", password);
		userPrefs.put("equipmentId", equipmentId);
		userPrefs.putInt("select", netParamSelect);
	}

	private void loadSettings() {
		for (int i = 0; i < MAX_HOSTS; i++) {
			try {
				netParams.add(new NetworkParameterSet(
					userPrefs.get("hostName" + i, AprsIsMultiAddressConfigurationComponent.getDefaultNetworkParameterList().get(i).getHostName()),
					userPrefs.getInt("portNumber" + i, AprsIsMultiAddressConfigurationComponent.getDefaultNetworkParameterList().get(i).getPortNumber()),
					userPrefs.get("description", AprsIsMultiAddressConfigurationComponent.getDefaultNetworkParameterList().get(i).getDescription())));
			} catch (UnknownHostException | IndexOutOfBoundsException ex) {
				LOG.log(Level.WARNING, ex.getMessage(), ex);
			} 
		}
		callSign = userPrefs.get("callSign", DEFAULT_CALLSIGN);
		password = userPrefs.get("password", DEFAULT_PASSWORD);
		equipmentId = userPrefs.get("equipmentId", DEFAULT_EQUIPMENT_ID);
		netParamSelect = userPrefs.getInt("select", DEFAULT_NETWORK_PARAMETER_SELECT);
	}

	private void writeToIsServer(String weatherReport) {
		if (loginVerified) {
			tcpClient.write(weatherReport.getBytes(StandardCharsets.UTF_8));
			pcs.firePropertyChange(Event.TX_UPDATE.name(), null, weatherReport);
			LOG.log(Level.INFO, "Data Written to APRS-IS Server: {0}", weatherReport);
		} else {
			LOG.log(Level.INFO, "Not Logged in to APRS-IS Server - Data Rejected: {0}", weatherReport);
			pcs.firePropertyChange(Event.TX_ERROR.name(), null, weatherReport);
		}
	}

    public static String degreesLatitudeToGPSFormat(double degreesLatitude) {
    	final int degrees = Math.abs((int) degreesLatitude);
    	final double minutes = Math.abs((degreesLatitude - degrees) * 60D);
    	final int m = (int) minutes;
    	final int mm = (int) ((minutes - m) * 100);
    	final String dir = degreesLatitude > 0 ? "N" : "S";
        return String.format(Locale.US,"%02d%02d.%02d%s", degrees, m, mm, dir);
    }
    
    public static String degreesLongitudeToGPSFormat(double degreesLongitude) {
    	final int degrees = Math.abs((int) degreesLongitude);
    	final double minutes = (Math.abs(degreesLongitude) - degrees) * 60D;
    	final int m = (int) minutes;
    	final int mm = (int) ((minutes - m) * 100);
    	final String dir = degreesLongitude > 0 ? "E" : "W";
        return String.format(Locale.US,"%03d%02d.%02d%s", degrees, m, mm, dir);
    }
    
    public static String humidityPercentToAprsFormat(double humidity) {
    	if (humidity >= 100.0) {
    		return "00";
    	} else {
    		return String.valueOf(Math.round(humidity));
    	}
    }
    
    public static String degreesFahrenheitToAprsFormat(double degrees) {
        return (degrees >= 0.0) ? String.format(Locale.US, "%03d", Math.round(degrees)) : 
                String.format(Locale.US, "%s%02d", "-", Math.round(degrees));
    }
    
    private String getAprsIsWeatherReportString(AbstractEnvironmentSensor aes, String callSign) {
	    // YOURCALLSIGN-6>APRS:=XXXX.XXN/XXXXX.XXE_.../...g...t70r...p...P...h57b.....L....ESPTEST
    	final StringBuilder bld = new StringBuilder(callSign);
	    bld.append(">APRS:=");
	    bld.append(degreesLatitudeToGPSFormat(aes.getStationLatitudeDegrees()));
	    bld.append("/");
	    bld.append(degreesLongitudeToGPSFormat(aes.getStationLongitudeDegrees()));
	    bld.append("_");
	    bld.append(aes.getWindDirectionTrue() == -1 ? "..." : String.format(Locale.US, "%03d", aes.getWindDirectionTrue()));
        bld.append("/");
        bld.append(aes.getCurrentWindSpeed(SpeedUnit.MPH) > -1 ? String.format(Locale.US, "%03d", Math.round(aes.getCurrentWindSpeed(SpeedUnit.MPH))) : "...");
        bld.append(String.format(Locale.US, "%s%03d", "g", Math.round(aes.getPeakPeriodicWindSpeedMeasurement(5, SpeedUnit.MPH))));
        bld.append(aes.getTempExteriorFahrenheit() > - 999D  ? String.format(Locale.US, "%s%s", "t", degreesFahrenheitToAprsFormat(aes.getTempExteriorFahrenheit())): "t...");
        final double rainfallInchesLastHour = Meteorology.convertMillimetersToInches(aes.getRainfallMillimetersLastHour());
        bld.append(rainfallInchesLastHour > -1 ? String.format(Locale.US, "%s%03d", "r", Math.round(rainfallInchesLastHour * 100)) : "r...");
        final double rainfallInchesLast24Hours = Meteorology.convertMillimetersToInches(aes.getRainfallMillimetersLast24Hours());
        bld.append(rainfallInchesLast24Hours > -1 ? String.format(Locale.US, "%s%03d", "p", Math.round(rainfallInchesLast24Hours * 100)) : "p...");
        bld.append(aes.getDailyRainInches() > -1 ? String.format(Locale.US, "%s%03d", "P", Math.round(aes.getDailyRainInches() * 100)) : "P...");
        bld.append(aes.getExteriorHumidity() > -1 ? String.format(Locale.US, "%s%s", "h", humidityPercentToAprsFormat(aes.getExteriorHumidity())) : "h..");
        bld.append(aes.getBarometricPressureRelativeHPA() > -1 ? String.format(Locale.US, "%s%05d", "b", Math.round(aes.getBarometricPressureRelativeHPA() * 10.0)) : "b.....");
        bld.append(aes.getLuminosityWM2() > -1 ? String.format(Locale.US, "%s%03d", "L", Math.round(aes.getLuminosityWM2())) : "L...");
	    bld.append(String.format(Locale.US, "%s", aes.getEquipmentCode()));

	    // N1CTF-9>APRS:=4005.62N/08304.46W_195/002g000t038r000p000P000h83b0981.1eSIGNALTRACK-1.9.3_Ecowitt-WS90
	    
	    final int i = bld.toString().indexOf(">") + 1;
	    
	    LOG.log(Level.INFO, "{0} {1} {2} {3} {4} {5} {6} {7} {8} {9} {10} {11} {12} {13} {14} {15} {16} {17}",
                new Object[]{
                    "\n--------------------- APRS IS Weather Report  ---------------------",
                    "\n   Call Sign:                    " + bld.substring(0, i),
                    "\n   Header :                      " + bld.substring(i, i + 5),
                    "\n   Latitude :                    " + bld.substring(i + 6, i + 14),
                    "\n   Longitude :                   " + bld.substring(i + 15, i + 24),
                    "\n   Wind Direction :              " + bld.substring(i + 25, i + 28),
                    "\n   Wind Speed MPH :              " + bld.substring(i + 29, i + 32),
                    "\n   Wind Speed 5 Min Peak MPH :   " + bld.substring(i + 33, i + 36),
                    "\n   Exterior Temp Fahrenheit :    " + bld.substring(i + 37, i + 40),
                    "\n   Rain Hourly Total Inches :    " + bld.substring(i + 41, i + 44),
                    "\n   Rain This Day Total Inches :  " + bld.substring(i + 45, i + 48),
                    "\n   Rain Last 24 Hours Inches :   " + bld.substring(i + 49, i + 52),
                    "\n   Percent Humidity :            " + bld.substring(i + 53, i + 55),
                    "\n   Barometric Pressure mBars :   " + bld.substring(i + 56, i + 61),
                    "\n   Luminosity WM2:               " + bld.substring(i + 62, i + 65),
                    "\n   Equipment Code :              " + bld.substring(i + 65, i + 65 + aes.getEquipmentCode().length()),
                    "\n   Complete String :             " + bld.toString(),
                    "\n-------------------- End APRS IS Weather Report ------------------"});
	    
	    return bld.toString();
    }
    
    public String getAprsIsHTMLWeatherReportString(AbstractEnvironmentSensor aes, String callSign) {
    	final String time = toAprsHourMinuteSecondUTC(aes.getZonedDateTimeUTC());
    	    	
    	final int windDirectionTrue = aes.getWindDirectionTrue();
    	final double currentWindSpeed = aes.getCurrentWindSpeed(SpeedUnit.MPH);
    	final double gustingWindSpeed = aes.getPeakPeriodicWindSpeedMeasurement(5, SpeedUnit.MPH);
    	final double tempExteriorFahrenheit = aes.getTempExteriorFahrenheit();
    	final double rainfallInchesLastHour = Meteorology.convertMillimetersToInches(aes.getRainfallMillimetersLastHour());
    	final double rainfallInchesLast24Hours = Meteorology.convertMillimetersToInches(aes.getRainfallMillimetersLast24Hours());
    	final double rainThisDayTotalInches = Math.round(aes.getDailyRainInches());
    	final int exteriorHumidity = aes.getExteriorHumidity();
    	final double barometricPressureRelativeHPA = aes.getBarometricPressureRelativeHPA();
    	final double luminosityWM2 = aes.getLuminosityWM2();
    	final String equipmentCode = aes.getEquipmentCode();
        
    	final StringBuilder html = new StringBuilder("<HTML>");
        html.append("APRS IS WEATHER REPORT TRANSMITTED TO MODEM at ");
        html.append(time);
        html.append("<br>");
		
        html.append("&emsp;Call Sign: ");
		html.append(callSign);
		html.append("<br>");
        
		html.append("&emsp;Latitude: ");
		html.append(degreesLatitudeToGPSFormat(aes.getStationLatitudeDegrees()));
		html.append("<br>");
		
		html.append("&emsp;Longitude: ");
		html.append(degreesLongitudeToGPSFormat(aes.getStationLongitudeDegrees()));
		html.append("<br>");

		html.append("&emsp;Wind Direction: ");
		html.append(windDirectionTrue);
		html.append("<br>");
		
		html.append("&emsp;Wind Speed MPH: ");
		html.append(currentWindSpeed);
		html.append("<br>");
		
		html.append("&emsp;Wind Speed 5 Min Peak MPH: ");
		html.append(gustingWindSpeed);
		html.append("<br>");
		
		html.append("&emsp;Exterior Temp Fahrenheit: ");
		html.append(tempExteriorFahrenheit);
		html.append("<br>");
		
		html.append("&emsp;Rain Last Hour Inches: ");
		html.append(rainfallInchesLastHour);
		html.append("<br>");
		
		html.append("&emsp;Rain Last 24 Hours Inches: ");
		html.append(rainfallInchesLast24Hours);
		html.append("<br>");
		
		html.append("&emsp;Rain This Calendar Day Total Inches: ");
		html.append(rainThisDayTotalInches);
		html.append("<br>");
		
		html.append("&emsp;Percent Humidity: ");
		html.append(exteriorHumidity);
		html.append("<br>");
		
		html.append("&emsp;Barometric Pressure mBars: ");
		html.append(barometricPressureRelativeHPA);
		html.append("<br>");
		
		html.append("&emsp;Luminosoty WM2: ");
		html.append(luminosityWM2);
		html.append("<br>");
		
		html.append("&emsp;Equipment Code: ");
		html.append(equipmentCode);
		
		html.append("</HTML>");
		
		html.trimToSize();
		
		return html.toString();
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
	public boolean sendUpdate() {
		updateString = getAprsIsWeatherReportString(aes, getCallSign());
		htmlString = getAprsIsHTMLWeatherReportString(aes, getCallSign());
 		writeToIsServer(updateString);
		return true;
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
	public JPanel getSettingsPanel() {
		return new AprsIsMultiAddressConfigurationComponent(this).getNetworkInterfaceConfigPanel();
	}
	
	@Override
	public String getClassName() {
		return this.getClass().getName();
	}
    
    public static String toAprsDayHourMinute(ZonedDateTime zdt) {
        return String.format(Locale.US, "%02d%02d%02d%s", zdt.getDayOfMonth(), zdt.getHour(), zdt.getMinute(), "/");
    }
    
    public static String toAprsHourMinuteSecondUTC(ZonedDateTime zdt) {
        return String.format(Locale.US, "%02d%02d%02d%s", zdt.getHour(), zdt.getMinute(), zdt.getSecond(), "z");
    }

}
