package meteorology;

import java.time.ZonedDateTime;

import net.e175.klaus.solarpositioning.DeltaT;
import net.e175.klaus.solarpositioning.SPA;
import net.e175.klaus.solarpositioning.SolarPosition;

/**
 * The Class Meteorology.
 *
 * @author n1ctf
 */
/**
 * 
 */
public class Meteorology {
	
	/** PRESSURE_ALTITUDE_DEFINITION. */
	public static final String PRESSURE_ALTITUDE_DEFINITION = """
		<HTML>PRESSURE ALTITUDE: In aviation, pressure altitude is the height above a standard <br>
		datum plane (SDP), which is a theoretical level where the weight of the <br>
		atmosphere is 29.921 inches of mercury (1,013.2 mbar; 14.696 psi) <br>
		as measured by a barometer. It indicates altitude obtained when an <br>
		altimeter is set to an agreed baseline pressure under certain circumstances <br>
		in which the aircraft’s altimeter would be unable to give a useful altitude <br>
		readout. Examples would be landing at a high altitude or near sea level <br>
		under conditions of exceptionally high air pressure. Old altimeters were <br>
		typically limited to displaying the altitude when set between 950 mb and 1030 mb. <br>
		Standard pressure, the baseline used universally, is 1013.25 hectopascals (hPa), <br>
		which is equivalent to 1013.25 mb or 29.92 inches of mercury (inHg). This setting <br>
		is equivalent to the atmospheric pressure at mean sea level (MSL) in the ISA. <br>
		Pressure altitude is primarily used in aircraft-performance calculations and in <br>
		high-altitude flight (i.e., above the transition altitude).</HTML>
		""";
	
	/** ABSOLUTE_PRESSURE_DEFINITION. */
	public static final String ABSOLUTE_PRESSURE_DEFINITION = """
		<HTML>ABSOLUTE PRESSURE: Absolute barometric pressure is a comparison of how much pressure is exerted <br>
		by the atmosphere compared to a vacuum, a space where there are no gases at all. <br>
		The air pressure in a vacuum would be zero, since there are no gases to exert <br>
		pressure on objects. Absolute barometric pressure is used primarily in scientific <br>
		studies and industrial applications requiring precise data. Measurements known <br>
		as "corrected barometric pressure" are used in most other applications, <br> 
		like weather reports.</HTML>
		""";
	
	/** ALTIMETER_DEFINITION. */
	public static final String ALTIMETER_DEFINITION = """
		<HTML>ALTIMETER SETTING: This is the pressure reading most commonly heard in <br>
		radio and television broadcasts. It is not the true barometric <br>
		pressure at a station. Instead it is the pressure "reduced" to mean <br>
		sea level using the temperature profile of the "standard" atmosphere, <br>
		which is representative of average conditions over the United States <br>
		at 40 degrees north latitude. The altimeter setting is the pressure <br>
		value to which an aircraft altimeter scale is set so that it will <br>
		indicate the altitude (above mean sea level) of the aircraft on the <br>
		ground at the location for which the pressure value was determined. <br>
		The altimeter setting is an attempt to remove elevation effects from <br>
		pressure readings using "standard" conditions.</HTML>
		""";
	
	/** STATION_PRESSURE_DEFINITION. */
	public static final String STATION_PRESSURE_DEFINITION = """
		<HTML>STATION PRESSURE: This is the pressure that is observed at a <br>
		specific elevation and is the true barometric pressure of <br>
		a location. It is the pressure exerted by the atmosphere at <br>
		a point as a result of gravity acting upon the "column" of <br>
		air that lies directly above the point. Consequently, higher <br>
		elevations above sea level experience lower pressure since <br>
		there is less atmosphere on which gravity can act. Put another way, <br>
		the weight of the atmosphere decreases as one increases in elevation. <br>
		Consequently then, in general, for every thousand feet of elevation <br>
		gain, the pressure drops about 1 inch of mercury. For example, <br>
		locations near 5000 feet (about 1500 meters) above mean sea level <br>
		normally have pressures on the order of 24 inches of mercury.</HTML>
		""";
	
	/** SEA_LEVEL_PRESSURE_DEFINITION. */
	public static final String SEA_LEVEL_PRESSURE_DEFINITION = """
		<HTML>SEA LEVEL PRESSURE: Atmospheric pressure, also known as air pressure <br>
		or barometric pressure (after the barometer), is the pressure within the  <br>
		atmosphere of Earth. The standard atmosphere (symbol: atm) is a unit of  <br>
		pressure defined as 101,325 Pa (1,013.25 hPa), which is equivalent to  <br>
		1,013.25 millibars, 760 mm Hg, 29.9212 inches Hg, or 14.696 psi.The atm unit <br>
		is roughly equivalent to the mean sea-level atmospheric pressure on Earth; that is,<br>
		the Earth's atmospheric pressure at sea level is approximately 1 atm. <br>
		In most circumstances, atmospheric pressure is closely approximated by the <br>
		hydrostatic pressure caused by the weight of air above the measurement point. <br>
		As elevation increases, there is less overlying atmospheric mass, so atmospheric <br>
		pressure decreases with increasing elevation. Because the atmosphere is thin <br>
		relative to the Earth's radius—especially the dense atmospheric layer at low <br>
		altitudes—the Earth's gravitational acceleration as a function of altitude can be <br>
		approximated as constant and contributes little to this fall-off. Pressure measures <br>
		force per unit area, with SI units of pascals (1 pascal = 1 newton per square metre, 1 N/m2). <br>
		On average, a column of air with a cross-sectional area of 1 square centimetre (cm2), <br>
		measured from the mean (average) sea level to the top of Earth's atmosphere, has a <br>
		mass of about 1.03 kilogram and exerts a force or "weight" of about 10.1 newtons, <br>
		resulting in a pressure of 10.1 N/cm2 or 101 kN/m2 (101 kilopascals, kPa). A column <br>
		of air with a cross-sectional area of 1 in2 would have a weight of about 14.7 lbf, <br>
		resulting in a pressure of 14.7 lbf/in2. </HTML>
		""";
	
    /** The Constant SOLAR_CONSTANT_W_PER_METER_SQUARED. */
    public static final double SOLAR_CONSTANT_W_PER_METER_SQUARED = 1367.0; // consistent with the World Radiation Center (WRC) solar constant
    
    /** The Constant STEPHAN_BOLTZMAN_CONSTANT. */
    public static final double STEPHAN_BOLTZMAN_CONSTANT = 5.67 * 10E-08;
    
    /** The Constant DRY_ADIABATIC_LAPSE_RATE_DEG_C_PER_KM. */
    public static final double DRY_ADIABATIC_LAPSE_RATE_DEG_C_PER_KM = 9.76;
    
    /** The Constant LATENT_HEAT_OF_VAPORIZATION_J_PER_KG. */
    public static final double LATENT_HEAT_OF_VAPORIZATION_J_PER_KG = 2501000.0;
    
    /** The Constant DRY_AIR_GAS_CONSTANT_J_PER_KG_KELVIN. */
    public static final double DRY_AIR_GAS_CONSTANT_J_PER_KG_KELVIN = 287.0;
    
    /** The Constant SATURATED_AIR_GAS_CONSTANT_J_PER_KG_KELVIN. */
    public static final double SATURATED_AIR_GAS_CONSTANT_J_PER_KG_KELVIN = 4615.0;
    
    /** The Constant SPECIFIC_HEAT_OF_DRY_AIR_J_PER_KG_KELVIN. */
    public static final double SPECIFIC_HEAT_OF_DRY_AIR_J_PER_KG_KELVIN = 1005.7;
    
    /** The Constant CONSTANT_PRESSURE_SPECIFIC_HEAT_J_PER_KG. */
    public static final double CONSTANT_PRESSURE_SPECIFIC_HEAT_J_PER_KG = 1004.68506;
    
    /** The Constant ACCELERATION_DUE_TO_GRAVITY_METERS_PER_SECOND_PER_SECOND. */
    public static final double ACCELERATION_DUE_TO_GRAVITY_METERS_PER_SECOND_PER_SECOND = 9.80065;
    
    /** The Constant MOLAR_MASS_OF_EARTH_AIR_KG_PER_MOL. */
    public static final double MOLAR_MASS_OF_EARTH_AIR_KG_PER_MOL = 0.02896968;
    
    /** The Constant UNIVERSAL_GAS_CONSTANT_FOR_EARTH_ATMOSPHERE_J_PER_MOL_K. */
    public static final double UNIVERSAL_GAS_CONSTANT_FOR_EARTH_ATMOSPHERE_J_PER_MOL_K = 8.314462618;
    
    /** The Constant AVG_LAPSE_RATE_OF_STANDARD_ATMOSPHERE_DEG_C_PER_KM. */
    public static final double AVG_LAPSE_RATE_OF_STANDARD_ATMOSPHERE_DEG_C_PER_KM = 6.5;
    
    /** The Constant AVG_LAPSE_RATE_OF_STANDARD_ATMOSPHERE_DEG_K_PER_M. */
    public static final double AVG_LAPSE_RATE_OF_STANDARD_ATMOSPHERE_DEG_K_PER_M = 0.0065;
    
    /** The Constant TEMP_STANDARD_ATMOSPHERE_AT_SEA_LEVEL_KELVIN. */
    public static final double TEMP_STANDARD_ATMOSPHERE_AT_SEA_LEVEL_KELVIN = 288.15;
    
    public static final double HPA_PER_INCH_HG = 33.863886666667; // at 0 degrees Celsius
    
    public static final double HPA_PER_MILLIMETER_HG = 1.3332239;
    
    public static final double INCHES_HG_PER_PASCAL = 0.00029529983071445;
    
    /** The Constant KILOMETERS_PER_MILE. */
    public static final double KILOMETERS_PER_MILE = 1.6093440;
    
    /** The Constant IN_HG_PER_MILLIBAR. */
    public static final double IN_HG_PER_MILLIBAR = 1 / 33.86389;
    
    /** The Constant INCHES_PER_MILLIMETER. */
    public static final double INCHES_PER_MILLIMETER = 0.03937008;
    
    /** The Constant MPH_AT_1_METER_PER_SEC. */
    public static final double MPH_AT_1_METER_PER_SEC = 2.236936;
    
    /** The Constant KPH_AT_1_METER_PER_SEC. */
    public static final double KPH_AT_1_METER_PER_SEC = 3.6;
    
    /** The Constant KNOTS_AT_1_METER_PER_SEC. */
    public static final double KNOTS_AT_1_METER_PER_SEC = 1.9438444924406;
    
    /** The Constant NAUTICAL_MILES_PER_KILOMETER. */
    public static final double NAUTICAL_MILES_PER_KILOMETER = 0.53995680;
    
    /** The Constant FEET_PER_METER. */
    public static final double FEET_PER_METER = 3.280839895;
    
    /** The Constant METERS_PER_FOOT. */
    public static final double METERS_PER_FOOT = 1 / FEET_PER_METER;
    
    /** The Constant MILLIBARS_AT_STANDARD_DATUM_PLANE. */
    public static final double MILLIBARS_AT_STANDARD_DATUM_PLANE = 1013.25;
    
    /** The Constant TEMP_F_AT_STANDARD_DATUM_PLANE. */
    public static final double TEMP_F_AT_STANDARD_DATUM_PLANE = 59.0;
    
    /** The Constant CRITICAL_PRESSURE_FOR_WATER_MPa. */
    public static final double CRITICAL_PRESSURE_FOR_WATER_MPa = 22.064;
    
    /** The Constant CRITICAL_TEMP_FOR_WATER_KELVINS. */
    public static final double CRITICAL_TEMP_FOR_WATER_KELVINS = 647.096;
    
    /** The Constant SPECIFIC_GAS_CONSTANT_FOR_WATER_VAPOR_JULES_PER_KG. */
    public static final double SPECIFIC_GAS_CONSTANT_FOR_WATER_VAPOR_JULES_PER_KG = 461.5;
    
    
    private static final double a1 = -7.85951783;
    private static final double a2 = 1.84408259;
    private static final double a3 = -11.7866497;
    private static final double a4 = 22.6807411;
    private static final double a5 = -15.9618719;
    private static final double a6 = 1.80122502;

    /**
     * Gets the Rasmussen saturated water vapor pressure in MPa
     *
     * @param tempKelvin the temperature in degrees kelvins
     * @return the Rasmussen saturated water vapor pressure in MPa
     * 
     * Pws = saturated water vapor pressure
     * Pc = critical pressure equal to 22.064 MPa
     * Tc = critical temperature equal to 647.096 K
     * ϑ = (1 − T ⁄ Tc)
     * a1 = −7.85951783
     * a2 = 1.84408259
     * a3 = −11.7866497
     * a4 = 22.6807411
     * a5 = −15.9618719
     * a6 = 1.80122502
     * 
     * ln(Pws ⁄ Pc) = Tc ⁄ T * (a1 * ϑ + a2 * ϑ1.5+ a3 * ϑ3+ a4 * ϑ3.5+ a5 * ϑ4+ a6 * ϑ7.5)
     * 
     */
    public static double getRasmussenSaturatedWaterVaporPressureMPa(double tempKelvin) {
    	final double t = 1 - (tempKelvin / CRITICAL_TEMP_FOR_WATER_KELVINS);
    	final double a = (a1*t)+(a2*Math.pow(t,1.5))+(a3*Math.pow(t,3))+(a4*Math.pow(t,3.5))+(a5*Math.pow(t,4))+(a6*Math.pow(t,7.5)); 
    	return  Math.exp((CRITICAL_TEMP_FOR_WATER_KELVINS / tempKelvin) * a) * CRITICAL_PRESSURE_FOR_WATER_MPa;
    }

    /**
     * Gets the Antoine saturated water vapor pressure mmHg.
     *
     * @param tempCelsius the temperature in celsius
     * @return the antoine saturated water vapor pressure mmHg
     * 
     * for temperature celsius: 1 > 100
     * A = 8.07131
     * B = 1730.63
     * C = 233.426
     * 
     * log10(P) = A - (B / (T + C))
     */
    public static double getAntoineSaturatedWaterVaporPressureMmHg(double tempCelsius) {
    	return Math.pow(10, 8.07131-(1730.63/(tempCelsius+233.426)));
    }
    
    /**
     * Gets the Arden-Buck saturated water vapor pressure in KiloPascals
     *
     * @param tempCelsius the temp celsius
     * @return the Arden-Buck saturated water vapor pressure
     */
    public static double getArdenBuckSaturatedWaterVaporPressureKPa(double tempCelsius) {
    	return 0.61121 * Math.exp((18.678-(tempCelsius/234.5))*(tempCelsius/(257.14+tempCelsius)));
    }
    
    /**
     * Gets the August-Roche-Magnus saturated water vapor pressure in KiloPascals
     *
     * @param tempCelsius the temperature in degrees celsius
     * @return the August-Roche-Magnus saturated water vapor pressure
     */
    public static double getAugustRocheMagnusSaturatedWaterVaporPressureKPa(double tempCelsius) {
    	return 0.61094 * Math.exp((17.625*tempCelsius)/(tempCelsius+243.04));
    }
    
    /**
     * Gets the Tetens saturated water vapor pressure in KiloPascals
     *
     * @param tempCelsius the temperature in degrees celsius
     * @return the Tetens saturated water vapor pressure
     */
    public static double getTetensSaturatedWaterVaporPressureKPa(double tempCelsius) {
    	return 0.61078 * Math.exp((17.27*tempCelsius)/(tempCelsius+237.3));
    }
    
    
    /**
     * Gets the absolute humidity in grams per cubic meter
     *
     * @param tempCelsius the temperature in degrees celsius
     * @param relativeHumidityPercent the percent of relative humidity (0 - 100)
     * @return the absolute humidity
     * 
     * AH = Pₐ/(Rw × T) = Absolute humidity, calculated in kilograms per cubic meter (kg/m³)
     * 
     * where:
     * 		Pₐ = Ps​(RH/100) = Actual vapor pressure in Pascals 
     * 		T = Air absolute temperature in Kelvins
     * 		Rw = 461.5 J/(kg K) = Specific gas constant for water vapor
     * 
     */
    public static double getAbsoluteHumidityGM3(double tempCelsius, double relativeHumidityPercent) {
    	final double Rw = SPECIFIC_GAS_CONSTANT_FOR_WATER_VAPOR_JULES_PER_KG;
    	final double T = Meteorology.convertCelsiusToKelvin(tempCelsius);
    	final double Ps = getRasmussenSaturatedWaterVaporPressureMPa(T) * 10E3;
    	final double Pa = Ps * relativeHumidityPercent;  			
    	return (Pa / (Rw * T)) * 1000;
    }
    
    /**
     * Gets the actual vapor pressure in MegaPascals
     *
     * @param tempKelvin the temperature in degrees kelvin
     * @param relativeHumidityPercent the relative humidity percent (0 - 100)
     * @return the actual vapor pressure
     */
    public static double getActualVaporPressureMPa(double tempKelvin, double relativeHumidityPercent) { 
    	return getAntoineSaturatedWaterVaporPressureMmHg(tempKelvin) * relativeHumidityPercent / 100.0;	
    }
    
    /**
     * Convert inches to millimeters
     *
     * @param inches the inches
     * @return millibars
     */
    public static double convertInchesToMillimeters(double inches) {
    	return inches / Meteorology.INCHES_PER_MILLIMETER;
    }
    
    /**
     * Convert feet to meters
     *
     * @param feet the feet
     * @return meters
     */
    public static double convertFeetToMeters(double feet) {
        return  feet * METERS_PER_FOOT;
    }
	    
    /**
     * Convert meters to feet
     *
     * @param meters the meters
     * @return feet
     */
    public static double convertMetersToFeet(double meters) {
        return meters * FEET_PER_METER;
    }
    
    /**
     * Convert meters per second to miles per hour
     *
     * @param metersPerSecond the meters per second
     * @return miles per hour
     */
    public static double convertMetersPerSecondToMPH(double metersPerSecond) {
        return metersPerSecond * Meteorology.MPH_AT_1_METER_PER_SEC;
    }
    
    /**
     * Convert miles per hour to meters per second.
     *
     * @param milesPerHour the miles per hour
     * @return meters per second
     */
    public static double convertMilesPerHourToMetersPerSecond(double milesPerHour) {
        return milesPerHour / Meteorology.MPH_AT_1_METER_PER_SEC;
    }
    
    /**
     * Convert meters per second to kilometers per hour
     *
     * @param metersPerSecond the meters per second
     * @return kilometers per hour
     */
    public static double convertMetersPerSecondToKPH(double metersPerSecond) {
        return metersPerSecond * Meteorology.KPH_AT_1_METER_PER_SEC;
    }
    
    public static double convertHPaToInchesHg(double hpa) {
    	return hpa / HPA_PER_INCH_HG;
    }
    
    public static double convertInchesHgToHPa(double inhg) {
    	return inhg * HPA_PER_INCH_HG;
    }
    
    public static double convertHPaToMillimetersHg(double hpa) {
    	return hpa / HPA_PER_MILLIMETER_HG;
    }
    
    /**
     * Convert kilometers per hour to meters per second
     *
     * @param kilometersPerHour the kilometers per hour
     * @return meters per second
     */
    public static double convertKilometersPerHourToMetersPerSecond(double kilometersPerHour) {
        return kilometersPerHour / Meteorology.KPH_AT_1_METER_PER_SEC;
    }
    
    /**
     * Convert meters per second to knots
     *
     * @param metersPerSecond the meters per second
     * @return knots
     */
    public static double convertMetersPerSecondToKnots(double metersPerSecond) {
        return metersPerSecond * KNOTS_AT_1_METER_PER_SEC;
    }
    
    /**
     * Convert knots to meters per second
     *
     * @param knots the knots
     * @return meters per second
     */
    public static double convertKnotsToMetersPerSecond(double knots) {
        return knots / Meteorology.KNOTS_AT_1_METER_PER_SEC;
    }
    
    /**
     * Convert kilometers to nautical miles
     *
     * @param kilometers the kilometers
     * @return nautical miles
     */
    public static double convertKilometersToNauticalMiles(double kilometers) {
        return kilometers * Meteorology.NAUTICAL_MILES_PER_KILOMETER;
    }
    
    /**
     * Convert nautical miles to kilometers
     *
     * @param nauticalMiles the nautical miles
     * @return kilometers
     */
    public static double convertNauticalMilesToKilometers(double nauticalMiles) {
        return nauticalMiles / Meteorology.KNOTS_AT_1_METER_PER_SEC;
    }
    
    /**
     * Convert solar lux to Watts per meter squared
     *
     * @param lux the lux
     * @return Watts per meter squared
     */
    public static double convertLuxSolarToWM2(double lux) {
        return (lux * 0.0079);
    }

    /**
     * Convert degrees celsius to degrees fahrenheit
     *
     * @param celsius the degrees celsius
     * @return degrees fahrenheit
     */
    public static double convertCelsiusToFahrenheit(double celsius) {
        return ((9D / 5D) * celsius) + 32D;
    }

    /**
     * Convert fahrenheit to celsius.
     *
     * @param fahrenheit the fahrenheit
     * @return the double
     */
    public static double convertFahrenheitToCelsius(double fahrenheit) {
        return (5D / 9D) * (fahrenheit + 32D);
    }

    /**
     * Convert kelvin to fahrenheit.
     *
     * @param kelvin the kelvin
     * @return the double
     */
    public static double convertKelvinToFahrenheit(double kelvin) {
        return ((9D / 5D) * (kelvin - 273.15)) + 32D;
    }

    /**
     * Convert fahrenheit to kelvin.
     *
     * @param fahrenheit the fahrenheit
     * @return the double
     */
    public static double convertFahrenheitToKelvin(double fahrenheit) {
        return (5D / 9D) * (fahrenheit - 32D) + 273.16;
    }

    /**
     * Convert rankine to fahrenheit.
     *
     * @param rankine the rankine
     * @return the double
     */
    public static double convertRankineToFahrenheit(double rankine) {
        return rankine - 459.69;
    }

    /**
     * Convert fahrenheit to rankine.
     *
     * @param fahrenheit the fahrenheit
     * @return the double
     */
    public static double convertFahrenheitToRankine(double fahrenheit) {
        return fahrenheit + 459.69;
    }

    /**
     * Convert kelvin to celsius.
     *
     * @param kelvin the kelvin
     * @return the double
     */
    public static double convertKelvinToCelsius(double kelvin) {
        return kelvin - 273.16;
    }

    /**
     * Convert celsius to kelvin.
     *
     * @param celsius the celsius
     * @return the double
     */
    public static double convertCelsiusToKelvin(double celsius) {
        return celsius + 273.16;
    }

    /**
     * Convert kelvin to rankine.
     *
     * @param kelvin the kelvin
     * @return the double
     */
    public static double convertKelvinToRankine(double kelvin) {
        return (((9D / 5D) * (kelvin - 273.16)) + 32D) + 459.69;
    }

    /**
     * Convert rankine to kelvin.
     *
     * @param rankine the rankine
     * @return the double
     */
    public static double convertRankineToKelvin(double rankine) {
        return ((5D / 9D) * ((rankine - 459.69) - 32D)) + 273.16;
    }

    /**
     * Convert celsius to rankine.
     *
     * @param celsius the celsius
     * @return the double
     */
    public static double convertCelsiusToRankine(double celsius) {
        return (((9D / 5D) * celsius) + 32D) + 459.69;
    }

    /**
     * Convert rankine to celsius.
     *
     * @param rankine the rankine
     * @return the double
     */
    public static double convertRankineToCelsius(double rankine) {
        return (5D / 9D) * ((rankine - 459.69) - 32D);
    }

    /**
     * Convert miles to kilometers.
     *
     * @param miles the miles
     * @return the double
     */
    public static double convertMilesToKilometers(double miles) {
        return miles * Meteorology.KILOMETERS_PER_MILE;
    }

    /**
     * Convert kilometers to miles.
     *
     * @param kilometers the kilometers
     * @return the double
     */
    public static double convertKilometersToMiles(double kilometers) {
        return kilometers / Meteorology.KILOMETERS_PER_MILE;
    }
    
    /**
     * Convert millimeters to inches.
     *
     * @param millimeters the millimeters
     * @return the double
     */
    public static double convertMillimetersToInches(double millimeters) {
        return millimeters * Meteorology.INCHES_PER_MILLIMETER;
    }
    
    /**
     * Convert millibars to in hg.
     *
     * @param millibars the millibars
     * @return the double
     */
    public static double convertMillibarsToInHg(double millibars) {
        return millibars * Meteorology.IN_HG_PER_MILLIBAR;
    }

    /**
     * Find relative optical length.
     *
     * @param zenith the zenith
     * @param elevationMeters the elevation meters
     * @return the double
     */
    public static double findRelativeOpticalLength(double zenith, double elevationMeters) {
        return Math.exp(((((-0.000118 * elevationMeters) - 1.638) * 10.0) - 9) * Math.pow(elevationMeters, 2)) / Math.cos(zenith);
    }

    /**
     * Find angle of incidence.
     *
     * @param solarZenith the solar zenith
     * @param solarAzimuth the solar azimuth
     * @param surfaceZenith the surface zenith
     * @param surfaceAzimuth the surface azimuth
     * @return the double
     */
    public static double findAngleOfIncidence(double solarZenith, double solarAzimuth, double surfaceZenith, double surfaceAzimuth) {
        return (Math.acos(Math.cos(solarZenith) * Math.cos(surfaceZenith)) + (Math.sin(solarZenith) * Math.sin(surfaceZenith) * Math.cos((solarAzimuth - surfaceAzimuth))));
    }

    /**
     * Find atmospheric vapor pressure.
     *
     * @param dewPoint the dew point
     * @param ambientTemp the ambient temp
     * @param millibars the millibars
     * @return the double
     */
    public static double findAtmosphericVaporPressure(double dewPoint, double ambientTemp, double millibars) {
        return Math.exp((17.67 * (dewPoint - ambientTemp)) / (ambientTemp + 243.5)) * (1.0007 + (0.00000346 * millibars)) * (6.112 * Math.exp((17.502 * ambientTemp) / (240.97 + ambientTemp)));
    }

    /**
     * Find atmospheric vapor pressure.
     *
     * @param relativeHumidity the relative humidity
     * @param tempCelsius the temp celsius
     * @return the double
     */
    public static double findAtmosphericVaporPressure(double relativeHumidity, double tempCelsius) {
        return relativeHumidity * 0.01 * 6.112 * Math.exp((17.62 * tempCelsius) / (tempCelsius + 243.12));
    }

    /**
     * Find thermal emissivity.
     *
     * @param ea the ea
     * @return the double
     */
    public static double findThermalEmissivity(double ea) {
        return 0.575 * Math.pow(ea, 1D / 7D);
    }

    /**
     * Find the wet bulb temperature in degrees celsius
     *
     * @param relativeHumidity the relative humidity in percent (0 - 100)
     * @param tempCelsius the temperature in decrees celsius
     * @return the wet bulb temperature in degrees celsius
     * 
     *  	Tw = T * arctan[0.151977 * (rh% + 8.313659)^(1/2)] + 
     *  		 arctan(T + rh%) - 
     *  		 arctan(rh% - 1.676331) + 
     *  		 0.00391838 * (rh%)^(3/2) * arctan(0.023101 * rh%) - 4.686035
     */
    public static double findWetBulbTemperatureCelsius(double relativeHumidity, double tempCelsius) {
        return tempCelsius * Math.atan(0.151977 * Math.pow(relativeHumidity + 8.313659, 0.5)) + Math.atan(tempCelsius + relativeHumidity)
                - Math.atan(relativeHumidity - 1.676331) + 0.00391838 * Math.pow(relativeHumidity, 3D / 2D) * Math.atan(0.023101 * relativeHumidity) - 4.686035;
    }

    /**
     * Find wet bulb globe temperature in degrees
     * This calculator works in fahrenheit, celsius and Kelvin, as long as all the parameters are in the same standard
     *
     * @param wetBulbTemp the wet bulb temperature in degrees
     * @param blackGlobeTemp the black globe temperature in degrees
     * @param dryBulbTemp the dry bulb temperature in degrees
     * @return the wet bulb globe temperature in degrees 
     */
    public static double findWetBulbGlobeTemperature(double wetBulbTemp, double blackGlobeTemp, double dryBulbTemp) {
        return (0.7 * wetBulbTemp) + (0.2 * blackGlobeTemp) + (0.1 * dryBulbTemp);
    }
    
    /**
     * Gets the dew point in degrees celsius
     *
     * @param relativeHumidity the relative humidity in percent (0 - 100)
     * @param tempCelsius the temperature in degrees celsius
     * @return the dew point in degrees celsius
     */
    public static double getDewPointCelsius(double relativeHumidity, double tempCelsius) {
        final double H = (Math.log10(relativeHumidity) - 2) / 0.4343 + (17.62 * tempCelsius) / (243.12 + tempCelsius);
        return 243.12 * H / (17.62 - H);
    }

    /**
     * Gets the dew point celsius using the Davis method
     *
     * @param relativeHumidity the relative humidity in percent (0 - 100)
     * @param tempCelsius the temperature in degrees celsius
     * @return the dew point in degrees celsius using the Davis method
     */
    public static final double getDewPointCelsiusDavis(double relativeHumidity, double tempCelsius) {
        final double v = findAtmosphericVaporPressure(relativeHumidity, tempCelsius);
        final double n = (243.12 * (Math.log(v))) - 440.1;
        final double d = 19.43 - Math.log(v);
        return n / d;
    }

    /**
     * Find lifting condensation level kilometers.
     *
     * @param tempCelsius the temperature celsius
     * @param dewPointCelsius the dew point celsius
     * @return the double
     */
    public static double findLiftingCondensationLevelKilometers(double tempCelsius, double dewPointCelsius) {
        final double a = 0.125;
        return a * (tempCelsius - dewPointCelsius);
    }

    /**
     * Find approximate absolute pressure in millibars
     *
     * @param elevationMeters the elevation meters
     * @return the approximate absolute pressure in millibars
     */
    public static double findApproximateAbsolutePressureMillibars(double elevationMeters) {
        final double sslp = 101325D; // Pascals
        final double tksl = Meteorology.TEMP_STANDARD_ATMOSPHERE_AT_SEA_LEVEL_KELVIN;
        final double alr = Meteorology.AVG_LAPSE_RATE_OF_STANDARD_ATMOSPHERE_DEG_K_PER_M;
        final double bracket = tksl / (tksl + alr * elevationMeters);
        final double exponent = (Meteorology.ACCELERATION_DUE_TO_GRAVITY_METERS_PER_SECOND_PER_SECOND
                * Meteorology.MOLAR_MASS_OF_EARTH_AIR_KG_PER_MOL / Meteorology.UNIVERSAL_GAS_CONSTANT_FOR_EARTH_ATMOSPHERE_J_PER_MOL_K * alr);
        return sslp * 0.01 * Math.pow(bracket, exponent);
    }

    /**
     * Find the moist adiabatic lapse rate
     *
     * @param tempCelsius the temperature in celsius
     * @param absolutePressureMilliBars the absolute pressure in millibars
     * @param dewPointCelsius the dew point in degrees celsius
     * @param relativeHumidityPercent the relative humidity in percent (0 - 100)
     * @return the Moist Adiabatic Lapse Rate
     * 
     */
    public static double findMoistAdiabaticLapseRate(double tempCelsius, double absolutePressureMilliBars, double dewPointCelsius, double relativeHumidityPercent) {
        final double tk = convertCelsiusToKelvin(tempCelsius);
        final double Îµ = Meteorology.DRY_AIR_GAS_CONSTANT_J_PER_KG_KELVIN / Meteorology.SATURATED_AIR_GAS_CONSTANT_J_PER_KG_KELVIN; // the ratio of the gas constants of dry air and saturated air
        final double e = dewPointCelsius * relativeHumidityPercent * 0.01; // where RH is the Relative Humidity in %
        final double r = (Îµ * e) / (absolutePressureMilliBars - e);
        final double rs = ((Îµ * dewPointCelsius) / (absolutePressureMilliBars - dewPointCelsius)); // the saturated mixing ratio of saturated air to dry air
        final double n = 1 + ((rs * Meteorology.LATENT_HEAT_OF_VAPORIZATION_J_PER_KG) / (Meteorology.DRY_AIR_GAS_CONSTANT_J_PER_KG_KELVIN * tk));
        final double Cp = Meteorology.SPECIFIC_HEAT_OF_DRY_AIR_J_PER_KG_KELVIN * (1 + (0.84 * r)); // specific heat of the actual air
        final double d = Cp + ((Math.pow(Meteorology.LATENT_HEAT_OF_VAPORIZATION_J_PER_KG, 2) * rs * Îµ)
                / (Meteorology.DRY_AIR_GAS_CONSTANT_J_PER_KG_KELVIN * Math.pow(tk, 2)));
        return Meteorology.ACCELERATION_DUE_TO_GRAVITY_METERS_PER_SECOND_PER_SECOND * 1000D * (n / d);
    }
    
    /**
     * Find wet bulb temperature.
     *
     * @param tempCelsius the temperature celsius
     * @param moistAdiabaticLapseRate the moist adiabatic lapse rate
     * @param liftingCondensationLevelKilometers the lifting condensation level kilometers
     * @return the double
     */
    public static double findWetBulbTemperature(double tempCelsius, double moistAdiabaticLapseRate, double liftingCondensationLevelKilometers) {
        final double tlcl = tempCelsius - (Meteorology.DRY_ADIABATIC_LAPSE_RATE_DEG_C_PER_KM * liftingCondensationLevelKilometers);
        return tlcl + (moistAdiabaticLapseRate * liftingCondensationLevelKilometers);
    }

    public static double getSeaLevelPressureMillibars(double stationPressureAbsoluteMillibars, double stationElevationMeters) {
    	return stationPressureAbsoluteMillibars * (293.0 / (293.0 - stationElevationMeters * AVG_LAPSE_RATE_OF_STANDARD_ATMOSPHERE_DEG_K_PER_M));
    }
    
    /**
     * Gets the altimeter calibration setting in inches of mercury
     *
     * @param stationPressureAbsoluteMillibars the station pressure in millibars (absloute)
     * @param stationElevationMeters the station elevation in meters
     * @return the altimeter calibration setting in inches of mercury
     */
    public static final double getAltimeterInchesHg(double stationPressureAbsoluteMillibars, double stationElevationMeters) {
    	final double altMb = getAltimeterMillibars(stationPressureAbsoluteMillibars, stationElevationMeters);
    	return convertMillibarsToInHg(altMb);
    }
    
    /**
     * Gets the altimeter calibration setting in millibars
     *
     * @param stationPressureAbsoluteMillibars the station pressure in millibars (absolute) 
     * @param stationElevationMeters the station elevation in meters
     * @return the altimeter calibration setting in millibars
     */
    public static double getAltimeterMillibars(double stationPressureAbsoluteMillibars, double stationElevationMeters) {
    	return (stationPressureAbsoluteMillibars - 0.3) * Math.pow((1 + (((Math.pow(MILLIBARS_AT_STANDARD_DATUM_PLANE, 0.190284) * AVG_LAPSE_RATE_OF_STANDARD_ATMOSPHERE_DEG_K_PER_M) / TEMP_STANDARD_ATMOSPHERE_AT_SEA_LEVEL_KELVIN) * (stationElevationMeters / Math.pow(stationPressureAbsoluteMillibars - 0.3, 0.190284)))), 1 / 0.190284);
    }
    
    /**
     * Gets the pressure altitude in feet
     *
     * @param stationPressureAbsoluteMillibars the station pressure in millibars (absolute)
     * @return the pressure altitude in feet
     */
    public static double getPressureAltitudeFeet(double stationPressureAbsoluteMillibars) {
    	return (1.0 - Math.pow(stationPressureAbsoluteMillibars / MILLIBARS_AT_STANDARD_DATUM_PLANE, 0.190284)) * 145366.45;
    }

    /**
     * Gets the altimeter in feet from millibars (absolute) and station elevation (feet)
     *
     * @param stationPressureAbsoluteMillibars the station pressure in millibars (absolute)
     * @param stationElevationFeet the station elevation in feet
     * @return the altimeter in feet
     */
    public static double getAltimeterInFeetFromMillibars(double stationPressureAbsoluteMillibars, double stationElevationFeet) {
    	return getAltimeterInFeetFromInHg(convertMillibarsToInHg(stationPressureAbsoluteMillibars), stationElevationFeet);
    }

    /**
     * Gets the altimeter in feet from inches of mercury
     *
     * @param stationPressureAbsoluteHg the station pressure (absolute) in inches of mercury
     * @param stationElevationFeet the station elevation in feet
     * @return the altimeter in feet
     */
    public static double getAltimeterInFeetFromInHg(double stationPressureAbsoluteHg, double stationElevationFeet) {
        return Math.pow((Math.pow(stationPressureAbsoluteHg, 0.190284) + (1.313E-5 * stationElevationFeet)), (1 / 0.190284));
    }

    /**
     * Gets the station pressure in inches of mercury from altimeter (inches of mercury) and elevation (meters)
     *
     * @param altInHg the altitude in inches of mercury
     * @param staEleMeters the station elevation in meters
     * @return the station pressure in inches of mercury
     */
    public static double getStationPressureInHgFromAltimeterInHg(double altInHg, double staEleMeters) {
    	return altInHg * Math.pow((TEMP_STANDARD_ATMOSPHERE_AT_SEA_LEVEL_KELVIN-(AVG_LAPSE_RATE_OF_STANDARD_ATMOSPHERE_DEG_K_PER_M*staEleMeters))/TEMP_STANDARD_ATMOSPHERE_AT_SEA_LEVEL_KELVIN, 5.2561);
    }
    
    /**
     * Gets the heat index degrees fahrenheit
     *
     * @param t the temperature in degrees fahrenheit
     * @param rh the relative humidity in percent (1.0 to 100.0)
     * @return the heat index degrees fahrenheit
     * 
     * 		The computation of the heat index is a refinement of a result obtained by
	 * 		multiple regression analysis carried out by Lans P. Rothfusz and described in
	 * 		a 1990 National Weather Service (NWS) Technical Attachment (SR 90-23). 
	 * 		
	 * 		The regression equation of Rothfusz is: 
	 * 			HI = -42.379 + 2.04901523*T +
	 * 				 10.14333127*RH - .22475541*T*RH - .00683783*T*T - .05481717*RH*RH +
	 * 			     .00122874*T*T*RH + .00085282*T*RH*RH - .00000199*T*T*RH*RH
	 * 
	 * 		Order of Operations: parenthesis exponent multiply divide add subtract
	 * 
	 * 		If the RH is less than 13% and the temperature is between 80 and 112 degrees F, then the following 
     * 		adjustment is subtracted from HI: [(13-RH)/4]*SQRT{[17-ABS(T-95.)]/17}, where ABS and SQRT are the 
     * 		absolute value and square root functions, respectively.
     *
     * 		On the other hand, if the RH is greater than 85% and the temperature is between 80 and 87 degrees F, 
     * 		then the following adjustment is added to HI: [(RH-85)/10] * [(87-T)/5]
     *
     * 		The Rothfusz regression is not appropriate when conditions of temperature and humidity warrant a 
     * 		heat index value below about 80 degrees F. In those cases, a simpler formula is applied to calculate 
     * 		values consistent with Steadman's results: HI = 0.5 * {T + 61.0 + [(T-68.0)*1.2] + (RH*0.094)} 
     *
	 * 
     */
    public static double getHeatIndexDegreesFahrenheit(double t, double rh) {
    	final double c1 = -42.379;
        final double c2 = 2.04901523;
        final double c3 = 10.14333127;
        final double c4 = 0.22475541;
        final double c5 = 0.00683783;
        final double c6 = 0.05481717;
        final double c7 = 0.00122874;
        final double c8 = 0.00085282;
        final double c9 = 0.00000199;   
        
        double rothfusz = c1 + 
               (c2 * t) + 
               (c3 * rh) - 
               (c4 * t * rh) -
               (c5 * t * t) -
               (c6 * rh * rh) + 
               (c7 * t * t * rh) + 
               (c8 * t * rh * rh) - 
               (c9 * t * t * rh * rh);
   
        if (t < 80.0) {
        	rothfusz = 0.5 * (t + 61.0 + ((t - 68.0) * 1.2) + (rh * 0.094));
        	
        } else if (t >= 80.0 && t < 112.0 && rh < 13.0) {
        	rothfusz -= ((13.0 - rh) / 4.0) * Math.sqrt((17.0 - Math.abs(t - 95.0)) / 17.0);
        	
        } else if (t >= 80.0 && t < 87.0 && rh >= 85.0) {
        	rothfusz  += ((rh - 85.0) / 10.0) * ((87.0 - t) / 5.0);
        }
        
        return rothfusz;
    }
    
    /**
     * Gets the heat index description
     *
     * @param heatIndexDegreesFahrenheit the heat index in degrees fahrenheit
     * @return the heat index description
     */
    public static String getHeatIndexDescription(double heatIndexDegreesFahrenheit) {
        if (heatIndexDegreesFahrenheit < 80) {
            return "BELOW INDEX";  
        } else if (heatIndexDegreesFahrenheit >= 80 && heatIndexDegreesFahrenheit < 91) {
            return "VERY WARM";  
        } else if (heatIndexDegreesFahrenheit >= 91 && heatIndexDegreesFahrenheit < 104) {
            return "HOT";  
        } else if (heatIndexDegreesFahrenheit >= 104 && heatIndexDegreesFahrenheit < 128) {
            return "VERY HOT";  
        } else if (heatIndexDegreesFahrenheit >= 128 && heatIndexDegreesFahrenheit < 279) {
            return "EXTREEMLY HOT";  
        }  else if (heatIndexDegreesFahrenheit >= 279) {
            return "ABOVE INDEX";  
        } 
        return "ERROR";
    }

    /**
     * Gets the relative temperature, at it feels on the skin, in degrees fahrenheit due to wind conditions
     *
     * @param tempFahrenheit the temperature in degrees fahrenheit
     * @param windSpeedMPH the wind speed in miles per hour
     * @return the wind chill equivalent temperature in degrees fahrenheit
     */
    public static double getWindChillDegreesFahrenheit(double tempFahrenheit, double windSpeedMPH) {
    	if (tempFahrenheit < -50 || tempFahrenheit > 50) {
    		throw new TemperatureOutOfRangeException("Wind chill equivalent temperature applies to air temperature between -50°F (-45°C, 228K) and 50°F (10°C, 283K) only");
    	}
        return 35.74 + 
               (0.6215 * tempFahrenheit) -
               (35.75 * Math.pow(windSpeedMPH, 0.16)) + 
               (0.4275 * tempFahrenheit * Math.pow(windSpeedMPH, 0.16));
    }
    
    /**
     * Gets the SolarPosition at the provided ZonedDateTime, as it appears from the provided Coordinate
     *
     * @param zonedDateTime the Java.time.ZonedDateTime 
     * @param coordinate the Meteorology.Coordinate
     * @return the SolarPosition
     */
    public SolarPosition getCurrentSolarPosition(ZonedDateTime zonedDateTime, Coordinate coordinate) {
        return SPA.calculateSolarPosition(
                zonedDateTime,
                coordinate.getLatitudeDegrees(), // latitude (degrees)
                coordinate.getLongitudeDegrees(), // longitude (degrees)
                coordinate.getElevationMeters(), // elevation (m)
                DeltaT.estimate(zonedDateTime.toLocalDate())); // delta T (s)
    }
    
    public static final String getSeaLevelPressureDefinitionText() {
    	return SEA_LEVEL_PRESSURE_DEFINITION;
    }
    
    /**
     * Gets the station pressure definition text.
     *
     * @return the station pressure definition text
     */
    public static final String getStationPressureDefinitionText() {
    	return STATION_PRESSURE_DEFINITION;
    }
    
    /**
     * Gets the altimeter definition text.
     *
     * @return the altimeter definition text
     */
    public static final String getAltimeterDefinitionText() {
    	return ALTIMETER_DEFINITION;
    }
    
    /**
     * Gets the pressure altitude definition text.
     *
     * @return the pressure altitude definition text
     */
    public static final String getPressureAltitudeDefinitionText() {
    	return PRESSURE_ALTITUDE_DEFINITION;
    }
    
    /**
     * Gets the absolute pressure definition text.
     *
     * @return the absolute pressure definition text
     */
    public static final String getAbsolutePressureDefinitionText() {
    	return ABSOLUTE_PRESSURE_DEFINITION;
    }
    
}

