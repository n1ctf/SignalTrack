package aprs;

import java.time.ZonedDateTime;

import java.util.Locale;

import java.util.logging.Level;
import java.util.logging.Logger;

import meteorology.AbstractEnvironmentSensor;
import meteorology.AbstractEnvironmentSensor.SpeedUnit;
import meteorology.Meteorology;

import time.ConsolidatedTime;

/**
 *
 * @author John R.Chartkoff
 */
public class AprsWeatherPositGenerator {
	
    public static final String ULTW_HEADER = "$ULTW";
    public static final String DATA_LOGGER_HEADER = "!!";
    public static final String APRS_RF_COMPLETE_HEADER = "/";
    public static final String DEFAULT_APRS_SYMBOL_TABLE_ID = "/";
    public static final String DEFAULT_APRS_SYMBOL_CODE = "_";
    public static final String DEFAULT_APP_NAME = "SIGNALTRACK-1.9.3_Ecowitt-WS90";
    
	private static final Logger LOG = Logger.getLogger(AprsWeatherPositGenerator.class.getName());
	
    private final AbstractEnvironmentSensor aes;
    
    public AprsWeatherPositGenerator(AbstractEnvironmentSensor aes) {
        this.aes = aes;
    }
    
    public String getAprsTNCWeatherReportString() {
        return getAprsRfWeatherReportString(DEFAULT_APRS_SYMBOL_TABLE_ID, DEFAULT_APRS_SYMBOL_CODE);
    }
    
    public String getAprsTNCHTMLString() {
        return getAprsRfHTMLString(DEFAULT_APRS_SYMBOL_TABLE_ID, DEFAULT_APRS_SYMBOL_CODE);
    }
    
    //  --- APRS Complete Weather Report Definition ---
	//  	c = wind direction (in degrees).
	//		s = sustained one-minute wind speed (in mph).
	//		g = gust (peak wind speed in mph in the last 5 minutes).
	//		t = temperature (in degrees Fahrenheit). Temperatures below zero are expressed as -01 to -99.
	//		r = rainfall (in hundredths of an inch) in the last hour.
	//		p = rainfall (in hundredths of an inch) in the last 24 hours.
	//		P = rainfall (in hundredths of an inch) since midnight.
	//		h = humidity (in %. 00 = 100%).
	//		b = barometric pressure (in tenths of millibars/tenths of hPascal).
	//		L = luminosity (in watts per square meter) 999 and below.
	//		l (lower-case letter “L”) = luminosity (in watts per square meter) 1000 and above.(L is inserted in place of one of the rain values).
	//		s = snowfall (in inches) in the last 24 hours.
    //      X = First, two digits of precision and the last digit is the order of magnitude in NANOSEVERTS/Hr
    //          So, 123 is 12 * 10^3 nanosieverts/hr or 12 microsieverts/hr
    //          Or, 456 is 45 * 10^6 nanosieverts/hr or 45 millisieverts/hr
	//		# = raw rain counter
    
    public String getAprsRfWeatherReportString(String symbolTableId, String symbolCode) {
        final StringBuilder bld = new StringBuilder();
        
        bld.append(APRS_RF_COMPLETE_HEADER);
        
        final String time = toAprsHourMinuteSecondUTC(aes.getZonedDateTimeUTC());
        final int windDirectionTrue = aes.getWindDirectionTrue();
        final double currentWindSpeed = aes.getCurrentWindSpeed(SpeedUnit.MPH);
        final double tempExteriorFahrenheit = aes.getTempExteriorFahrenheit();
        final double gustingWindSpeed = aes.getPeakPeriodicWindSpeedMeasurement(5, SpeedUnit.MPH);
        final double rainfallInchesLastHour = Meteorology.convertMillimetersToInches(aes.getRainfallMillimetersLastHour());
        final double rainThisDayTotalInches = aes.getDailyRainInches();
        final int exteriorHumidity = aes.getExteriorHumidity();
        final double barometricPressureRelativeHPA = aes.getBarometricPressureRelativeHPA();
        final double luminosityWM2 = aes.getLuminosityWM2();
        final double gammaRadiationMicroSievertsPerHour = aes.getGammaRadiationMicroSievertsPerHour();
        
        bld.append(time);
        bld.append(degreesLatitudeToGPSFormat(aes.getStationLatitudeDegrees())); 
        bld.append(symbolTableId);
        bld.append(degreesLongitudeToGPSFormat(aes.getStationLongitudeDegrees())); 
        bld.append(symbolCode);
        bld.append(windDirectionTrue == -1 ? "..." : String.format(Locale.US, "%03d", windDirectionTrue));
        bld.append("/");
        bld.append(currentWindSpeed > -1 ? String.format(Locale.US, "%03d", Math.round(currentWindSpeed)) : "...");
        bld.append(gustingWindSpeed > -1 ? String.format(Locale.US, "%s%03d", "g", Math.round(gustingWindSpeed)) : "g...");
        bld.append(tempExteriorFahrenheit > - 999D ? String.format(Locale.US, "%s%s", "t", degreesFahrenheitToAprsFormat(tempExteriorFahrenheit)): "t...");
        bld.append(rainfallInchesLastHour > -1 ? String.format(Locale.US, "%s%03d", "r", Math.round(rainfallInchesLastHour * 100)) : "r...");
        bld.append(rainThisDayTotalInches > -1 ? String.format(Locale.US, "%s%03d", "P", Math.round(rainThisDayTotalInches * 100)) : "P...");
        // double rainfallInchesLast24Hours = Meteorology.convertMillimetersToInches(aes.getRainfallMillimetersLast24Hours());
        // bld.append(rainfallInchesLast24Hours > -1 ? String.format(Locale.US, "%s%03d", "p", Math.round(rainfallInchesLast24Hours * 100)) : "p...");
        bld.append(exteriorHumidity > -1 ? String.format(Locale.US, "%s%s", "h", humidityPercentToAprsFormat(exteriorHumidity)) : "h..");
        bld.append(barometricPressureRelativeHPA > -1 ? String.format(Locale.US, "%s%05d", "b", Math.round(barometricPressureRelativeHPA * 10.0)) : "b.....");
        bld.append(luminosityWM2 > -1 ? getLuminositySentence(luminosityWM2) : "L...");
        bld.append(gammaRadiationMicroSievertsPerHour > -1 ? String.format(Locale.US, "%s%s", "X", toNanoSievertsPerHourAprsFormatFromSeiverts(gammaRadiationMicroSievertsPerHour / 1E6)) : "X...");
        bld.append(String.format(Locale.US, "%s", aes.getEquipmentCode())); 
        bld.append("\r\n");
        
        bld.trimToSize();
		
        LOG.log(Level.INFO, "{0} {1} {2} {3} {4} {5} {6} {7} {8} {9} {10} {11} {12} {13} {14} {15} {16} {17} {18}",
            new Object[]{
                "\n--------------------- APRS RF Weather Report  ---------------------",
                "\n   Header:                            " + bld.substring(0, 1),
                "\n   HH MM SS :                         " + bld.substring(1, 8),
                "\n   Latitude :                         " + bld.substring(8, 16),
                "\n   Symbol ID :                        " + bld.substring(16, 17),
                "\n   Longitude :                        " + bld.substring(17, 26),
                "\n   Symbol Code :                      " + bld.substring(26, 27),
                "\n   Wind Direction / Speed MPH :       " + bld.substring(27, 34),
                "\n   Wind Speed 5 Min Peak MPH :        " + bld.substring(35, 38),
                "\n   Exterior Temp Fahrenheit :         " + bld.substring(39, 42),
                "\n   Rain Hourly Total Inches :         " + bld.substring(43, 46),
                "\n   Rain This Day Total Inches :       " + bld.substring(47, 50),
                "\n   Percent Humidity :                 " + bld.substring(51, 53),
                "\n   Barometric Pressure mBars :        " + bld.substring(54, 59),
                "\n   Luminosoty WM2 :                   " + bld.substring(60, 63),
                "\n   NanoSieverts / Hr :                " + bld.substring(64, 67),
                "\n   Equipment Code :                   " + bld.substring(68, 72),
                
                "\n   Complete String :                  " + bld.toString(),
                
                "\n-------------------- End APRS RF Weather Report ------------------"});
        
        return bld.toString();
    }

    public String getAprsRfHTMLString(String symbolTableId, String symbolCode) {
    	final String time = toAprsHourMinuteSecondUTC(aes.getZonedDateTimeUTC());
    	final int windDirectionTrue = aes.getWindDirectionTrue();
    	final double currentWindSpeed = aes.getCurrentWindSpeed(SpeedUnit.MPH);
    	final double tempExteriorFahrenheit = aes.getTempExteriorFahrenheit();
    	final double gustingWindSpeed = aes.getPeakPeriodicWindSpeedMeasurement(5, SpeedUnit.MPH);
    	final double rainfallInchesLastHour = Meteorology.convertMillimetersToInches(aes.getRainfallMillimetersLastHour());
    	final double rainThisDayTotalInches = aes.getDailyRainInches();
    	final int exteriorHumidity = aes.getExteriorHumidity();
    	final double barometricPressureRelativeHPA = aes.getBarometricPressureRelativeHPA();
    	final double luminosityWM2 = aes.getLuminosityWM2();
    	final double gammaRadiationMicroSievertsPerHour = aes.getGammaRadiationMicroSievertsPerHour();
        
    	final StringBuilder html = new StringBuilder("<HTML>");
        html.append("APRS RF REPORT TRANSMITTED TO MODEM at ");
        html.append(time);
        html.append("<br>");
		
		html.append("&emsp;Latitude: ");
		html.append(degreesLatitudeToGPSFormat(aes.getStationLatitudeDegrees()));
		html.append("<br>");
		
		html.append("&emsp;Longitude: ");
		html.append(degreesLongitudeToGPSFormat(aes.getStationLongitudeDegrees()));
		html.append("<br>");
		
		html.append("&emsp;Symbol Table ID: ");
		html.append(symbolTableId);
		html.append("<br>");
		
		html.append("&emsp;Symbol Code: ");
		html.append(symbolCode);
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
		
		html.append("&emsp;Rain Hourly Total Inches: ");
		html.append(rainfallInchesLastHour);
		html.append("<br>");
		
		html.append("&emsp;Rain This Day Total Inches: ");
		html.append(rainThisDayTotalInches);
		html.append("<br>");
		
		html.append("&emsp;Percent Humidity: ");
		html.append(exteriorHumidity);
		html.append("<br>");
		
		html.append("&emsp;Barometric Pressure mBars: ");
		html.append(barometricPressureRelativeHPA);
		html.append("<br>");
		
		html.append("&emsp;Luminosoty WM2: ");
		html.append(getLuminositySentence(luminosityWM2));
		html.append("<br>");
		
		html.append("&emsp;Gamma Radiation MicroSieverts / Hr: ");
		html.append(gammaRadiationMicroSievertsPerHour);
		html.append("<br>");
		
		html.append("&emsp;Equipment Code: ");
		html.append(aes.getEquipmentCode());
		
		html.append("</HTML>");
		
		html.trimToSize();
		
		return html.toString();
    }
    
    public static String getLuminositySentence(double wm2) {
    	String str = String.valueOf(wm2);
    	str = str + "0000";
    	str = str.replace(".", "");
    	return wm2 >= 1 ? "l" + str.substring(0,3) : "L" + str.substring(1,4);
    }
    
    public String getAprsIsWeatherReportString(String callSign) {
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
    
    //    DATA LOGGER MODE - RECORD STRUCTURE
    //
    //    2400 baud, 8 data bits, 1 stop bit, no parity. Flow control is NONE or HARDWARE.
    //    Characters are ASCII hex digits (0-9, A-F).
    //    All fields are 4 bytes except 2 bytes where indicated in the Complete Record Mode.
    //    Most significant digit first.
    //
    //    Header = !! (or || if in Multiple Output Mode)
    //    Data Fields
    //        1. Wind Speed (0.1 kph)
    //        2. Wind Direction (0-255)
    //        3. Outdoor Temp (0.1 deg F)
    //        4. Rain* Long Term Total (0.01 inches)
    //        5. Barometer (0.1 mbar)
    //        6. Indoor Temp (0.1 deg F)
    //        7. Outdoor Humidity (0.1%)
    //        8. Indoor Humidity (0.1%)
    //        9. Date (day of year)
    //        10. Time (minute of day)
    //        11. Today's Rain Total (0.01 inches)*
    //        12. 1 Minute Wind Speed Average (0.1kph)* 
    //    Carriage Return & Line Feed 
    //
    //    Sample from Byonics:   !!005A0011019104C9281D0296--------0047035E0000008B
    
    public String getPeetDataLoggerCharacterString() {
    	final StringBuilder bld = new StringBuilder();
        
        bld.append(DATA_LOGGER_HEADER);
        
        bld.append("%04X".formatted((int) (Math.round(aes.getCurrentWindSpeed(SpeedUnit.KPH) * 10))));
        bld.append("%04X".formatted(aes.getWindDirectionTrue() * 255 / 359));
        bld.append("%04X".formatted((int) (Math.round(aes.getTempExteriorFahrenheit() * 10))));
        bld.append("%04X".formatted((int) (Math.round(aes.getYearlyRainInches() * 100))));
        bld.append("%04X".formatted((int) (Math.round(aes.getBarometricPressureRelativeHPA() * 10))));
        bld.append("%04X".formatted((int) (Math.round(aes.getTempInteriorFahrenheit() * 10))));
        bld.append("%04X".formatted(aes.getExteriorHumidity() * 10));
        bld.append("%04X".formatted(aes.getInteriorHumidity() * 10));	
        bld.append("%04X".formatted((aes.getZonedDateTimeUTC().getDayOfYear())));
        bld.append("%04X".formatted((ConsolidatedTime.getMinuteOfDay(aes.getZonedDateTimeUTC()))));
        bld.append("%04X".formatted((int) (Math.round(aes.getDailyRainInches() * 100))));
        bld.append("%04X".formatted((int) (Math.round(aes.getAveragePeriodicWindSpeedMeasurement(1, SpeedUnit.KPH) * 10))));	
        
        bld.append("\r\n");
        
        bld.trimToSize();

        LOG.log(Level.INFO, "{0} {1} {2} {3} {4} {5} {6} {7} {8} {9} {10} {11} {12} {13} {14} {15}",
            new Object[]{
                "\n--------------------- Peet Data Logger  ---------------------",
                "\n   Header:                            " + "  " + bld.substring(0, 2),
                "\n   Wind Speed - KPH * 10 :            " + "  0x" + bld.substring(2, 6),
                "\n   Wind Direction (0-255) :           " + "  0x" + bld.substring(6, 10),
                "\n   Exterior Temp - Fahrenheit * 10 :  " + "  0x" + bld.substring(10, 14),
                "\n   Rain YTD - Inches * 100 :          " + "  0x" + bld.substring(14, 18),
                "\n   Barometric Pressure - mbars * 10 : " + "  0x" + bld.substring(18, 22),
                "\n   Interior Temp - Fahrenheit * 10 :  " + "  0x" + bld.substring(22, 26),
                "\n   Exterior Humidity - Percent * 10 : " + "  0x" + bld.substring(26, 30),
                "\n   Interior Humidity - Percent * 10 : " + "  0x" + bld.substring(30, 34),
                "\n   Day of Year (1-365) :              " + "  0x" + bld.substring(34, 38),
                "\n   Minute of Day (0-1439) :           " + "  0x" + bld.substring(38, 42),
                "\n   Daily Rain - Inches * 100 :        " + "  0x" + bld.substring(42, 46),
                "\n   Wind Speed 1 Min Avg - KPH * 10 :  " + "  0x" + bld.substring(46, 50),
                "\n   Complete String :                    " + bld.toString(),
                "\n-------------------- End Peet Data Logger ------------------"});
        
        return bld.toString();
    }

	//    PACKET MODE - RECORD STRUCTURE
	//
	//    Header = $ULTW
	//    Data Fields
	//        1. Wind Speed Peak over last 5 min. (0.1 kph)
	//        2. Wind Direction of Wind Speed Peak (0-255)
	//        3. Current Outdoor Temp (0.1 deg F)
	//        4. Rain Long Term Total (0.01 in.)
	//        5. Current Barometer (0.1 mbar)
	//        6. Barometer 3 Hour Pressure Change(0.1 mbar)
	//        7. Barometer Corr. Factor(LSW)
	//        8. Barometer Corr. Factor(MSW)
	//        9. Current Outdoor Humidity (0.1%)
	//        10. Date (day of year)
	//        11. Time (minute of day)
	//        12. Today's Rain Total (0.01 inches)*
	//        13. 5 Minute Wind Speed Average (0.1kph)* 
	//    Carriage Return & Line Feed
	//
	//	  *Some instruments may not include field 13, some may not include 12 or 13. Please contact Peet Bros. if this presents any problem.
	//	  Packet Mode records are output every five minutes:  first, upon selection of Packet Mode; then, at every 5 minutes past the hour (12:00, 12:05, 12:10, etc.), continuosly.
    //    Total size: 44, 48 or 52 characters (hex digits) + header, carriage return and line feed. 
  
    public String getPeetULTWCharacterString() {
    	final StringBuilder bld = new StringBuilder();
        bld.append(ULTW_HEADER);
        
        bld.append("%04X".formatted((Math.round(aes.getAveragePeriodicWindSpeedMeasurement(5, SpeedUnit.KPH) * 10))));
        bld.append("%04X".formatted(aes.getAveragePeriodicWindDirectionMeasurement(5) * 255 / 359));
        bld.append("%04X".formatted((Math.round(aes.getTempExteriorFahrenheit() * 10))));
        bld.append("%04X".formatted((Math.round(aes.getYearlyRainInches() * 100))));
        bld.append("%04X".formatted((Math.round(aes.getBarometricPressureRelativeHPA() * 10))));
        bld.append("%04X".formatted((Math.round(aes.get3HourBarometerPressureDeltaMillibars()))));
        bld.append("%04X".formatted(aes.getBarometerCorrectionFactorLSW()));
        bld.append("%04X".formatted(aes.getBarometerCorrectionFactorMSW()));	
        bld.append("%04X".formatted(aes.getExteriorHumidity() * 10));	
        bld.append("%04X".formatted((aes.getZonedDateTimeUTC().getDayOfYear())));
        bld.append("%04X".formatted((ConsolidatedTime.getMinuteOfDay(aes.getZonedDateTimeUTC()))));
        bld.append("%04X".formatted((Math.round(aes.getDailyRainInches() * 100))));
        bld.append("%04X".formatted((Math.round(aes.getPeakPeriodicWindSpeedMeasurement(5, SpeedUnit.KPH) * 10))));	
        
        bld.append("\r\n");
        
        bld.trimToSize();
        
        LOG.log(Level.INFO, "{0} {1} {2} {3} {4} {5} {6} {7} {8} {9} {10} {11} {12} {13} {14} {15} {16}",
                new Object[]{
                    "\n--------------------- Peet Data Logger  ---------------------",
                    "\n   Header:                                " + "  " + bld.substring(0, 5),
                    "\n   Wind Speed - KPH * 10 :                " + "  0x" + bld.substring(5, 9),
                    "\n   Wind Direction (0-255) :               " + "  0x" + bld.substring(9, 13),
                    "\n   Exterior Temp - Fahrenheit * 10 :      " + "  0x" + bld.substring(13, 17),
                    "\n   Rain YTD - Inches * 100 :              " + "  0x" + bld.substring(17, 21),
                    "\n   Barometric Pressure - mbars * 10 :     " + "  0x" + bld.substring(21, 25),
                    "\n   Barometric Pressure - Delta  * 10 :    " + "  0x" + bld.substring(25, 29),
                    "\n   Barometric Pressure - Correction LSW : " + "  0x" + bld.substring(29, 33),
                    "\n   Barometric Pressure - Correction MSW : " + "  0x" + bld.substring(33, 37),
                    "\n   Exterior Humidity - Percent * 10 :     " + "  0x" + bld.substring(37, 41),
                    "\n   Day of Year (1-365) :                  " + "  0x" + bld.substring(41, 45),
                    "\n   Minute of Day (0-1439) :               " + "  0x" + bld.substring(45, 49),
                    "\n   Daily Rain - Inch * 100 :              " + "  0x" + bld.substring(49, 53),
                    "\n   Wind Peed 5 Min Avg - KPH * 10 :       " + "  0x" + bld.substring(53, 57),
                    "\n   Complete String :                        " + bld.toString(),
                    "\n-------------------- End Peet Data Logger ------------------"});
        
        return bld.toString();
    }

    @Override
    public String toString() {
        return getPeetDataLoggerCharacterString();
    }
    
    public static String humidityPercentToAprsFormat(double humidity) {
    	if (humidity >= 100.0) {
    		return "00";
    	} else {
    		return String.valueOf(Math.round(humidity));
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
    
    public static String toAprsDayHourMinute(ZonedDateTime zdt) {
        return String.format(Locale.US, "%02d%02d%02d%s", zdt.getDayOfMonth(), zdt.getHour(), zdt.getMinute(), "/");
    }
    
    public static String toAprsHourMinuteSecondUTC(ZonedDateTime zdt) {
        return String.format(Locale.US, "%02d%02d%02d%s", zdt.getHour(), zdt.getMinute(), zdt.getSecond(), "z");
    }
    
    public static String degreesFahrenheitToAprsFormat(double degrees) {
        return (degrees >= 0.0) ? String.format(Locale.US, "%03d", Math.round(degrees)) : 
                String.format(Locale.US, "%s%02d", "-", Math.round(degrees));
    }
    
    // 99 Sieverts == 99,000,000,000 nanoSieverts == 999
    // 6.3 Sieverts == 6,300,000,000 nanoSieverts == 638
    // 0.000000167 Seiverts == 0.122 microSeiverts == 167 nanoSeiverts == 121
    
    public static String toNanoSievertsPerHourAprsFormatFromSeiverts(double sieverts) {
    	if (sieverts >= 10.0) {
    		return "%s%s".formatted(Math.min(Math.round(sieverts), 99), "9");
    	} else if (sieverts >= 1.0) {
    		return "%s%s".formatted(Math.round(sieverts * 10), "8");
    	} else if (sieverts >= 0.1) {
    		return "%s%s".formatted(Math.round(sieverts * 100), "7");
    	} else if (sieverts >= 0.01) {
    		return "%s%s".formatted(Math.round(sieverts * 1000), "6");
    	} else if (sieverts >= 0.001) {
    		return "%s%s".formatted(Math.round(sieverts * 10000), "5");
    	} else if (sieverts >= 0.0001) {
    		return "%s%s".formatted(Math.round(sieverts * 100000), "4");
    	} else if (sieverts >= 0.00001) {
    		return "%s%s".formatted(Math.round(sieverts * 1000000), "3");
    	} else if (sieverts >= 0.000001) {
    		return "%s%s".formatted(Math.round(sieverts * 10000000), "2");
    	} else if (sieverts >= 0.0000001) {
    		return "%s%s".formatted(Math.round(sieverts * 100000000), "1");
    	} else return "000";
    }

}
