package lunar;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import time.AstronomicalTime;
import time.AstronomicalTime.LunarPosition;

/**
 *
 * @author n1ctf
 */
public class MoonCalc {
    public static final double DURATION_IN_DAYS_OF_LUNAR_CYCLE = 29.53058770576;
    public static final long SECONDS_PER_DAY = 86400;
    public static final double SECONDS_PER_LUNAR_CYCLE = DURATION_IN_DAYS_OF_LUNAR_CYCLE * SECONDS_PER_DAY;
    public static final ZonedDateTime NEW_MOON_Y2K = ZonedDateTime.of(2000, 1, 6, 18, 14, 0, 0, ZoneOffset.UTC);
    
    private final double percentageOfLunation;  
    private final double fractionOfLunation;
    private final double phaseOfLunation;
    private final double angleOfLunation;
    private final double moonAgeInDays;
    private final double azimuth;
    private final double elevation;
    private final double distance;
    private final double parallacticAngle;
    
    private final ZonedDateTime riseTime;
    private final ZonedDateTime setTime;
    
    private final LunarPosition lunarPosition;
    
    public MoonCalc(ZonedDateTime zdt, double stationLatitudeDegrees, double stationLongitudeDegrees) {
    	
    	final long secondsSinceY2KFirstFullMoon = zdt.toEpochSecond() - NEW_MOON_Y2K.toEpochSecond();
        
        double currentSeconds = secondsSinceY2KFirstFullMoon % SECONDS_PER_LUNAR_CYCLE;
        
        if (currentSeconds < 0) {
            currentSeconds += SECONDS_PER_LUNAR_CYCLE;
        }
        
        final double currentFractionOfTheLunarCycle = currentSeconds / SECONDS_PER_LUNAR_CYCLE;
        percentageOfLunation = currentFractionOfTheLunarCycle * 100;
        moonAgeInDays = currentFractionOfTheLunarCycle * DURATION_IN_DAYS_OF_LUNAR_CYCLE;
        
		azimuth = AstronomicalTime.getLunarAzimuth(zdt, stationLatitudeDegrees, stationLongitudeDegrees);
		elevation = AstronomicalTime.getLunarAltitude(zdt, stationLatitudeDegrees, stationLongitudeDegrees);
		distance = AstronomicalTime.getLunarDistance(zdt);
		parallacticAngle = AstronomicalTime.getLunarParallacticAngle(zdt, stationLatitudeDegrees, stationLongitudeDegrees);
				
		fractionOfLunation = AstronomicalTime.getLunarIlluminationFraction(zdt);
		phaseOfLunation = AstronomicalTime.getLunarPhase(zdt);
		angleOfLunation = AstronomicalTime.getLunarPhaseAngle(zdt);
		
		lunarPosition = AstronomicalTime.getLunarPosition(zdt, stationLatitudeDegrees, stationLongitudeDegrees);
		
		riseTime = AstronomicalTime.getLunarRiseTime(zdt, stationLatitudeDegrees, stationLongitudeDegrees);
		setTime = AstronomicalTime.getLunarSetTime(zdt, stationLatitudeDegrees, stationLongitudeDegrees);
    }
    
    public LunarPosition getLunarHorizon() {
		return lunarPosition;
	}
    
	public double getFractionOfLunation() {
		return fractionOfLunation;
	}
	
	public double getPhaseOfLunation() {
		return phaseOfLunation;
	}
	
	public double getAngleOfLunation() {
		return angleOfLunation;
	}
	
	public double getAzimuth() {
		return azimuth;
	}
	
	public double getElevation() {
		return elevation;
	}
	
	public double getDistance() {
		return distance;
	}
	
	public double getParallacticAngle() {
		return parallacticAngle;
	}
	
	public ZonedDateTime getRiseTime() {
		return riseTime;
	}
	
	public ZonedDateTime getSetTime() {
		return setTime;
	}
	
	public double getPercentageOfLunation() {
        return percentageOfLunation;
    }
	
    public double getMoonAgeInDays() {
        return moonAgeInDays;
    }
    
    public String getMoonPhaseName() {
        return getMoonPhaseName(moonAgeInDays);
    }
    
    public String getMoonPhaseNameAbbreviated() {
        return getMoonPhaseNameAbbreviated(moonAgeInDays);
    }
    
    public static String getMoonPhaseName(double ageInDays) {
        if ((ageInDays >= 0 && ageInDays < 1.0) || (ageInDays >= 28.53058770576 && ageInDays < 29.53058770576)) {
            return "NEW";
        } else if (ageInDays >= 1 && ageInDays < 6.38264692644) {
            return "WAXING CRESCENT";
        } else if (ageInDays >= 6.38264692644 && ageInDays < 8.38264692644) {
            return "FIRST QUARTER";
        } else if (ageInDays >= 8.38264692644 && ageInDays < 13.76529385288) {
            return "WAXING GIBBOUS";
        } else if (ageInDays >= 13.76529385288 && ageInDays < 15.76529385288) {
            return "FULL";
        } else if (ageInDays >= 15.76529385288 && ageInDays < 21.14794077932) {
            return "WANING GIBBOUS";
        } else if (ageInDays >= 21.14794077932 && ageInDays < 23.14794077932) {
            return "LAST QUARTER";
        } else if (ageInDays >= 23.14794077932 && ageInDays < 28.53058770576) {
            return "WANING CRESCENT";
        } else {
        	return "";
        }
    }
    
    public static String getMoonPhaseNameAbbreviated(double ageInDays) {
        if ((ageInDays >= 0 && ageInDays < 1.0) || (ageInDays >= 28.53058770576 && ageInDays < 29.53058770576)) {
            return "New MoonCalc ";
        } else if (ageInDays >= 1 && ageInDays < 6.38264692644) {
            return "Wax Cres ";
        } else if (ageInDays >= 6.38264692644 && ageInDays < 8.38264692644) {
            return "Frst Qtr ";
        } else if (ageInDays >= 8.38264692644 && ageInDays < 13.76529385288) {
            return "Wax Gibb ";
        } else if (ageInDays >= 13.76529385288 && ageInDays < 15.76529385288) {
            return "Full MoonCalc";
        } else if (ageInDays >= 15.76529385288 && ageInDays < 21.14794077932) {
            return "Wan Gibb ";
        } else if (ageInDays >= 21.14794077932 && ageInDays < 23.14794077932) {
            return "Last Qtr ";
        } else if (ageInDays >= 23.14794077932 && ageInDays < 28.53058770576) {
            return "Wan Cres ";
        } else {
        	return "";
        }
    }
  
}
