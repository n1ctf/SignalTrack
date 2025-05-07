package time;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import java.util.logging.Level;
import java.util.logging.Logger;

import jparsec.ephem.EphemerisElement;
import jparsec.ephem.Target;
import jparsec.observer.ObserverElement;
import jparsec.time.SiderealTime;
import jparsec.time.TimeElement;
import jparsec.util.JPARSECException;

/**
 * Created with IDEA.
 *
 * @author mastcer@gmail.com by leelory
 * @description a tiny java library for calculating sun/moon positions and phases
 *              <p>
 *              Based on a JavaScript library AstronomicalTime for calculating sun/moon
 *              position and light phases. https://github.com/mourner/suncalc
 * @date 2019/5/18 : 11:39
 */
public final class AstronomicalTime {
	private static final Logger LOG = Logger.getLogger(AstronomicalTime.class.getName());

	/**
	 * sun calculations are based on http://aa.quae.nl/en/reken/zonpositie.html
	 * formulas date/time constants and conversions
	 */
	
	public static final double RADIAN = Math.PI / 180;
	public static final double dayMs = 1000D * 60D * 60D * 24D;  // approximate milliSeconds per day
	public static final double J1970 = 2440588; // Julian date as of midday of 01 January 1970, terrestrial time
	public static final double J2000 = 2451545; // Julian date as of midday of 01 January 2000, terrestrial time
	public static final double SOLAR_DISTANCE_TO_EARTH_KM = 149598000;
	public static final double J0 = 0.0009;
	public static final double e = RADIAN * 23.4397;  // obliquity of the Earth
	public static final double SECONDS_PER_RADIAN = 206264.8062471;
	public static final double GRAVITATIONAL_CONSTANT = 6.647E-11;
	public static final double SOLAR_MASS_KG = 1.98892E30;
	public static final double GAUSSIAN_GRAVITATIONAL_CONSTANT = 0.017202098950000;
	public static final double EARTH_MEAN_SIDEREAL_DAY_SECONDS = 86164.09891;
	public static final double EARTH_MEAN_ROTATION_RATE_RADIANS_PER_SECOND = Math.toRadians(360/86164.09891);
	
	public static final int SECONDS_PER_SOLAR_DAY =  86400;
	
	public enum LunarPosition {
		ALWAYS_ABOVE_HORIZON, 
		ALWAYS_BELOW_HORIZON,
		VISIBLE
	}
	
	private AstronomicalTime() {
		throw new IllegalStateException("Utility class");
	}

	private static boolean isZero(double value){
		return isZero(value, 0.000000001);
	}
	
	private static boolean isZero(double value, double threshold){
	    return value >= -threshold && value <= threshold;
	}
	
	public static double toJulian(ZonedDateTime zdt) {
		return (zdt.toInstant().getEpochSecond() * 1000) / dayMs - 0.5 + J1970;
	}

	public static double toJulian(LocalDateTime zdt) {
		return (zdt.toEpochSecond(ZoneOffset.UTC) * 1000) / dayMs - 0.5 + J1970;
	}
	
	public static ZonedDateTime fromJulianZdt(double j, ZonedDateTime zdt) {
		return ZonedDateTime.ofInstant(Instant.ofEpochMilli((long) ((j + 0.5 - J1970) * dayMs)), zdt.getZone());
	}
	
	public static LocalDateTime fromJulian(double j) {
		return LocalDateTime.ofInstant(Instant.ofEpochMilli((long) ((j + 0.5 - J1970) * dayMs)), ZoneOffset.UTC);
	}
	
	public static double toDays(ZonedDateTime zdt) {
		return toJulian(zdt) - J2000;
	}
	
	public static double toDays(LocalDateTime ldt) {
		return toJulian(ldt) - J2000;
	}
	
	// return atan(sin(l) * cos(e) - tan(b) * sin(e), cos(l));
	private static double rightAscension(double l, double b) {
		return Math.atan2(Math.sin(l) * Math.cos(e) - Math.tan(b) * Math.sin(e), Math.cos(l));
	}

	// return asin(sin(b) * cos(e) + cos(b) * sin(e) * sin(l));
	private static double declination(double l, double b) {
		return Math.asin(Math.sin(b) * Math.cos(e) + Math.cos(b) * Math.sin(e) * Math.sin(l));
	}
	
	// return atan(sin(H), cos(H) * sin(phi) - tan(dec) * cos(phi));
	private static double azimuth(double h, double phi, double dec) {
		return Math.atan2(Math.sin(h), Math.cos(h) * Math.sin(phi) - Math.tan(dec) * Math.cos(phi));
	}

	// return asin(sin(phi) * sin(dec) + cos(phi) * cos(dec) * cos(H));
	private static double altitude(double h, double phi, double dec) {
		return Math.asin(Math.sin(phi) * Math.sin(dec) + Math.cos(phi) * Math.cos(dec) * Math.cos(h));
	}
	
	// return rad * (280.16 + 360.9856235 * d) - lw;
	private static double siderealTime(double days, double lw) {
		return RADIAN * (280.16 + 360.9856235 * days) - lw;
	}

	private static double astroRefraction(double h) {
		// the following formula works for positive altitudes only.
		if (h < 0) {
			// if h = -0.08901179 a div/0 would occur.
			h = 0;
		}
		// formula 16.4 of "Astronomical Algorithms" 2nd edition by Jean Meeus
		// (Willmann-Bell, Richmond) 1998.
		// 1.02 / Math.tan(h + 10.26 / (h + 5.10)) h in degrees, result in arc minutes
		// ->
		// converted to RADIAN:
		return 0.0002967 / Math.tan(h + 0.00312536 / (h + 0.08901179));
	}

	// rad * (357.5291 + 0.98560028 * d); }
	private static double solarMeanAnomaly(double d) {
		return RADIAN * (357.5291 + 0.98560028 * d);
	}

	// C = rad * (1.9148 * sin(M) + 0.02 * sin(2 * M) + 0.0003 * sin(3 * M)), // equation of center
	// P = rad * 102.9372; // perihelion of the Earth
	// return M + C + P + PI;
	private static double eclipticLongitude(double m) {
		// equation of center
		final double c = RADIAN * (1.9148 * Math.sin(m) + 0.02 * Math.sin(2 * m) + 0.0003 * Math.sin(3 * m));
		// perihelion of the Earth
		final double p = RADIAN * 102.9372;
		return m + c + p + Math.PI;
	}

	private static double getSolarDeclination(double d) {
		final double m = solarMeanAnomaly(d);
		final double l = eclipticLongitude(m);
		
		return declination(l, 0);
	}

	private static double getSolarRightAscension(double d) {
		final double m = solarMeanAnomaly(d);
		final double l = eclipticLongitude(m);
		
		return rightAscension(l, 0);
	}
	
	public static double toSeconds(double radians) {
		return Math.toDegrees(radians) * 3600;
	}	
	
	public static LocalTime getGreenwichMeanSiderealTime(LocalDateTime ut1, double latitudeDegrees, double longitudeDegrees, int altitudeMeters) {
		double st = 0;
		try {
			final TimeElement te = new TimeElement(toJulian(ut1), TimeElement.SCALE.UNIVERSAL_TIME_UT1);
			final ObserverElement oe = new ObserverElement("None", Math.toRadians(longitudeDegrees), Math.toRadians(latitudeDegrees), altitudeMeters, 0);
			final EphemerisElement ee = new EphemerisElement(Target.TARGET.EARTH, EphemerisElement.COORDINATES_TYPE.APPARENT, EphemerisElement.EQUINOX_OF_DATE, true, EphemerisElement.REDUCTION_METHOD.getLatest(), EphemerisElement.FRAME.DYNAMICAL_EQUINOX_J2000);
			st = SiderealTime.greenwichMeanSiderealTime(te, oe, ee);
				        
		} catch (JPARSECException ex) {
			LOG.log(Level.INFO, JPARSECException.getCurrentTrace());
		}
		return radiansToLocalTime(st);
	}
	
	public static LocalTime getGreenwichApparentSiderealTime(LocalDateTime ut1, double latitudeDegrees, double longitudeDegrees, int altitudeMeters) {
		double st = 0;
		try {
			final TimeElement te = new TimeElement(toJulian(ut1), TimeElement.SCALE.UNIVERSAL_TIME_UT1);
			final ObserverElement oe = new ObserverElement("None", Math.toRadians(longitudeDegrees), Math.toRadians(latitudeDegrees), altitudeMeters, 0);
			final EphemerisElement ee = new EphemerisElement(Target.TARGET.EARTH, EphemerisElement.COORDINATES_TYPE.APPARENT, EphemerisElement.EQUINOX_OF_DATE, true, EphemerisElement.REDUCTION_METHOD.getLatest(), EphemerisElement.FRAME.DYNAMICAL_EQUINOX_J2000);
			st = SiderealTime.greenwichApparentSiderealTime(te, oe, ee);
				        
		} catch (JPARSECException ex) {
			LOG.log(Level.INFO, JPARSECException.getCurrentTrace());
		}
		return radiansToLocalTime(st);
	}
	
	public static LocalTime getApparentSiderealTime(LocalDateTime ut1, double latitudeDegrees, double longitudeDegrees, int altitudeMeters) {
		double st = 0;
		try {
			final TimeElement te = new TimeElement(toJulian(ut1), TimeElement.SCALE.UNIVERSAL_TIME_UT1);
			final ObserverElement oe = new ObserverElement("None", Math.toRadians(longitudeDegrees), Math.toRadians(latitudeDegrees), altitudeMeters, 0);
			final EphemerisElement ee = new EphemerisElement(Target.TARGET.EARTH, EphemerisElement.COORDINATES_TYPE.APPARENT, EphemerisElement.EQUINOX_OF_DATE, true, EphemerisElement.REDUCTION_METHOD.getLatest(), EphemerisElement.FRAME.DYNAMICAL_EQUINOX_J2000);
			st = SiderealTime.apparentSiderealTime(te, oe, ee);
				        
		} catch (JPARSECException ex) {
			LOG.log(Level.INFO, JPARSECException.getCurrentTrace());
		}
		return radiansToLocalTime(st);
	}
	
	public static LocalTime radiansToLocalTime(double radians) {		
		double deg = Math.toDegrees(radians);
		deg = (deg < 0) ? (deg + 360.0) : deg;
		deg = (deg >= 360.0) ? (deg - 360.0) : deg;
		final double hoursDec = deg / 15;
		final double minutes = (hoursDec - (int) hoursDec) * 60;
		final double seconds = (minutes - (int) minutes) * 60;
		final double nanosOfSecond = (seconds - (int) seconds) * 1E9;
		
		return LocalTime.of((int) hoursDec, (int) minutes, (int) seconds, (int) nanosOfSecond);
	}
	
	public static double normalizeAngle(double angle) {
        double remainder = angle % 360;
        if (remainder < 0) {
            remainder += 360;
        }
        return remainder;
    }
	
	public static double getEarthRotationAngleRadians(ZonedDateTime zdt) {
		return 2 * Math.PI * (0.7790572732640 + 1.00273781191135448 * toDays(zdt));
	} 

	public static double getSolarDistanceToEarthKm() {
		return SOLAR_DISTANCE_TO_EARTH_KM;
	}
	
	public static double getSolarAzimuth(ZonedDateTime zdt, double lat, double lng) {
		final double lw = RADIAN * -lng;
		final double phi = RADIAN * lat;
		final double d = toDays(zdt);
		final double h = siderealTime(d, lw) - getSolarRightAscension(d); 
		
		return azimuth(h, phi, getSolarDeclination(d)) + Math.toRadians(180);
	}
	
	// δ = 23.45 * sin(360 * (284 + n) / 365)
	public static double getSolarDec(LocalDateTime ldt) {
		return 23.45 * Math.sin(360D * (284D + ldt.getDayOfYear() / 365D));
	}
	
	// α = asin(sin(δ) * sin(φ) + cos(δ) * cos(φ) * cos(ω))
	public static double getSolarElevation(LocalDateTime ldt, double latitude) {
		final double decl = getSolarDec(ldt);
		final double hourAngle = getHourAngle(ldt);
		return Math.toDegrees(Math.asin(Math.sin(decl) * Math.sin(Math.toRadians(latitude)) + Math.cos(decl) * Math.cos(Math.toRadians(latitude)) * Math.cos(hourAngle)));
	}
	
	public static double getHourAngle(LocalDateTime ldt) {
		final int hour = ldt.getHour();
		final int minute = ldt.getMinute();
		final int second = ldt.getSecond();
		final long totalSeconds = (hour * 3600L) + (minute * 60L) + second ;
		final double t = ldt.getDayOfYear() + (totalSeconds / 86400D);
		return 15 * (t - 12);
	}
	
	public static double getSolarElevation(ZonedDateTime zdt, double lat, double lng) {
		final double lw = RADIAN * -lng;
		final double phi = RADIAN * lat;
		final double d = toDays(zdt);
		final double h = siderealTime(d, lw) - getSolarRightAscension(d);
		
		return altitude(h, phi, getSolarDeclination(d));
	}

	private static double julianCycle(double d, double lw) {
		return Math.round(d - J0 - lw / (2 * Math.PI));
	}

	private static double approxTransit(double ht, double lw, double n) {
		return J0 + (ht + lw) / (2 * Math.PI) + n;
	}

	private static double solarTransitJ(double ds, double m, double l) {
		return J2000 + ds + 0.0053 * Math.sin(m) - 0.0069 * Math.sin(2 * l);
	}

	private static double hourAngle(double h, double phi, double d) {
		return Math.acos((Math.sin(h) - Math.sin(phi) * Math.sin(d)) / (Math.cos(phi) * Math.cos(d)));
	}

	private static final double getSetJ(final double h, final double lw, final double phi, final double dec, final double n, final double m, final double l) {
		final double w = hourAngle(h, phi, dec);
		final double a = approxTransit(w, lw, n);
		return solarTransitJ(a, m, l);
	}

	public static ZonedDateTime getSolarNoon(ZonedDateTime zdt, final double longitude) {
		final double lw = RADIAN * -longitude;

		final double d = toDays(zdt);
		final double n = julianCycle(d, lw);
		final double ds = approxTransit(0, lw, n);

		final double M = solarMeanAnomaly(ds);
		final double L = eclipticLongitude(M);
		
		final double jNoon = solarTransitJ(ds, M, L);
		return fromJulianZdt(jNoon, zdt);
	}
	
	public static ZonedDateTime getNadir(ZonedDateTime zdt, final double longitude) {
		final double lw = RADIAN * -longitude;

		final double d = toDays(zdt);
		final double n = julianCycle(d, lw);
		final double ds = approxTransit(0, lw, n);

		final double m = solarMeanAnomaly(ds);
		final double L = eclipticLongitude(m);
		
		final double jNoon = solarTransitJ(ds, m, L);
		
		return fromJulianZdt(jNoon - 0.5, zdt);	
	}
	
	public static ZonedDateTime getSunrise(ZonedDateTime zdt, final double latitude, final double longitude) {
		final double lw = RADIAN * -longitude;
		final double phi = RADIAN * latitude;

		final double d = toDays(zdt);
		final double n = julianCycle(d, lw);
		final double ds = approxTransit(0, lw, n);

		final double M = solarMeanAnomaly(ds);
		final double L = eclipticLongitude(M);
		final double dec = declination(L, 0);

		final double jNoon = solarTransitJ(ds, M, L);
		
		final double jSet = getSetJ(-0.833 * RADIAN, lw, phi, dec, n, M, L);
		final double jRise = jNoon - (jSet - jNoon);
		
		return fromJulianZdt(jRise, zdt);
	}
	
	public static ZonedDateTime getSunset(ZonedDateTime zdt, final double latitude, final double longitude) {
		final double lw = RADIAN * -longitude;
		final double phi = RADIAN * latitude;

		final double d = toDays(zdt);
		final double n = julianCycle(d, lw);
		final double ds = approxTransit(0, lw, n);

		final double M = solarMeanAnomaly(ds);
		final double L = eclipticLongitude(M);
		final double dec = declination(L, 0);
		
		final double jSet = getSetJ(-0.833 * RADIAN, lw, phi, dec, n, M, L);
		
		return fromJulianZdt(jSet, zdt);
	}
	
	public static ZonedDateTime getSunriseEnd(ZonedDateTime zdt, final double latitude, final double longitude) {
		
		final double lw = RADIAN * -longitude;
		final double phi = RADIAN * latitude;

		final double d = toDays(zdt);
		final double n = julianCycle(d, lw);
		final double ds = approxTransit(0, lw, n);

		final double M = solarMeanAnomaly(ds);
		final double L = eclipticLongitude(M);
		final double dec = declination(L, 0);

		final double jNoon = solarTransitJ(ds, M, L);
		
		final double jSet = getSetJ(-0.3 * RADIAN, lw, phi, dec, n, M, L);
		final double jRise = jNoon - (jSet - jNoon);
		
		return fromJulianZdt(jRise, zdt);
	}
	
	public static ZonedDateTime getSunriseStart(ZonedDateTime zdt, final double latitude, final double longitude) {
		final double lw = RADIAN * -longitude;
		final double phi = RADIAN * latitude;

		final double d = toDays(zdt);
		final double n = julianCycle(d, lw);
		final double ds = approxTransit(0, lw, n);

		final double M = solarMeanAnomaly(ds);
		final double L = eclipticLongitude(M);
		final double dec = declination(L, 0);
		
		final double jSet = getSetJ(-0.3 * RADIAN, lw, phi, dec, n, M, L);
		
		return fromJulianZdt(jSet, zdt);
	}
	
	public static ZonedDateTime getDawn(ZonedDateTime zdt, final double latitude, final double longitude) {
		final double lw = RADIAN * -longitude;
		final double phi = RADIAN * latitude;

		final double d = toDays(zdt);
		final double n = julianCycle(d, lw);
		final double ds = approxTransit(0, lw, n);

		final double M = solarMeanAnomaly(ds);
		final double L = eclipticLongitude(M);
		final double dec = declination(L, 0);

		final double jNoon = solarTransitJ(ds, M, L);
		
		final double jSet = getSetJ(-6.0 * RADIAN, lw, phi, dec, n, M, L);
		final double jRise = jNoon - (jSet - jNoon);
		
		return fromJulianZdt(jRise, zdt);
	}
	
	public static ZonedDateTime getDusk(ZonedDateTime zdt, final double latitude, final double longitude) {
		final double lw = RADIAN * -longitude;
		final double phi = RADIAN * latitude;

		final double d = toDays(zdt);
		final double n = julianCycle(d, lw);
		final double ds = approxTransit(0, lw, n);

		final double M = solarMeanAnomaly(ds);
		final double L = eclipticLongitude(M);
		final double dec = declination(L, 0);
		
		final double jSet = getSetJ(-6.0 * RADIAN, lw, phi, dec, n, M, L);
		
		return fromJulianZdt(jSet, zdt);
	}
	
	public static ZonedDateTime getNauticalDawn(ZonedDateTime zdt, final double latitude, final double longitude) {
		final double lw = RADIAN * -longitude;
		final double phi = RADIAN * latitude;

		final double d = toDays(zdt);
		final double n = julianCycle(d, lw);
		final double ds = approxTransit(0, lw, n);

		final double M = solarMeanAnomaly(ds);
		final double L = eclipticLongitude(M);
		final double dec = declination(L, 0);

		final double jNoon = solarTransitJ(ds, M, L);
		
		final double jSet = getSetJ(-12.0 * RADIAN, lw, phi, dec, n, M, L);
		final double jRise = jNoon - (jSet - jNoon);
		
		return fromJulianZdt(jRise, zdt);
	}
	
	public static ZonedDateTime getNauticalDusk(ZonedDateTime zdt, final double latitude, final double longitude) {
		final double lw = RADIAN * -longitude;
		final double phi = RADIAN * latitude;

		final double d = toDays(zdt);
		final double n = julianCycle(d, lw);
		final double ds = approxTransit(0, lw, n);

		final double M = solarMeanAnomaly(ds);
		final double L = eclipticLongitude(M);
		final double dec = declination(L, 0);
		
		final double jSet = getSetJ(-12.0 * RADIAN, lw, phi, dec, n, M, L);
		
		return fromJulianZdt(jSet, zdt);
	}
	
	public static ZonedDateTime getNightEnd(ZonedDateTime zdt, final double latitude, final double longitude) {
		final double lw = RADIAN * -longitude;
		final double phi = RADIAN * latitude;

		final double d = toDays(zdt);
		final double n = julianCycle(d, lw);
		final double ds = approxTransit(0, lw, n);

		final double M = solarMeanAnomaly(ds);
		final double L = eclipticLongitude(M);
		final double dec = declination(L, 0);

		final double jNoon = solarTransitJ(ds, M, L);
		
		final double jSet = getSetJ(-18.0 * RADIAN, lw, phi, dec, n, M, L);
		final double jRise = jNoon - (jSet - jNoon);
		
		return fromJulianZdt(jRise, zdt);
	}
	
	public static ZonedDateTime getNight(ZonedDateTime zdt, final double latitude, final double longitude) {
		final double lw = RADIAN * -longitude;
		final double phi = RADIAN * latitude;

		final double d = toDays(zdt);
		final double n = julianCycle(d, lw);
		final double ds = approxTransit(0, lw, n);

		final double M = solarMeanAnomaly(ds);
		final double L = eclipticLongitude(M);
		final double dec = declination(L, 0);
		
		final double jSet = getSetJ(-18.0 * RADIAN, lw, phi, dec, n, M, L);
		
		return fromJulianZdt(jSet, zdt);
	}
	
	public static ZonedDateTime getGoldenHourEnd(ZonedDateTime zdt, final double latitude, final double longitude) {
		final double lw = RADIAN * -longitude;
		final double phi = RADIAN * latitude;

		final double d = toDays(zdt);
		final double n = julianCycle(d, lw);
		final double ds = approxTransit(0, lw, n);

		final double M = solarMeanAnomaly(ds);
		final double L = eclipticLongitude(M);
		final double dec = declination(L, 0);

		final double jNoon = solarTransitJ(ds, M, L);
		
		final double jSet = getSetJ(6.0 * RADIAN, lw, phi, dec, n, M, L);
		final double jRise = jNoon - (jSet - jNoon);
		
		return fromJulianZdt(jRise, zdt);
	}
	
	public static ZonedDateTime getGoldenHour(ZonedDateTime zdt, final double latitude, final double longitude) {
		final double lw = RADIAN * -longitude;
		final double phi = RADIAN * latitude;

		final double d = toDays(zdt);
		final double n = julianCycle(d, lw);
		final double ds = approxTransit(0, lw, n);

		final double M = solarMeanAnomaly(ds);
		final double L = eclipticLongitude(M);
		final double dec = declination(L, 0);
		
		final double jSet = getSetJ(6.0 * RADIAN, lw, phi, dec, n, M, L);
		
		return fromJulianZdt(jSet, zdt);
	}

	private static final double getLunarRightAscension(final double d) {
		// geocentric ecliptic coordinates of the moon
		
		// ecliptic longitude
		final double L = RADIAN * (218.316 + 13.176396 * d);
		
		// mean anomaly
		final double M = RADIAN * (134.963 + 13.064993 * d);
		
		// mean distance
		final double F = RADIAN * (93.272 + 13.229350 * d);
		
		// longitude
		final double l = L + RADIAN * 6.289 * Math.sin(M);
		
		// latitude
		final double b = RADIAN * 5.128 * Math.sin(F);
		
		return rightAscension(l, b);
	}

	private static final double getLunarDeclination(final double d) {
		// geocentric ecliptic coordinates of the moon
		
		// ecliptic longitude
		final double L = RADIAN * (218.316 + 13.176396 * d);
		
		// mean anomaly
		final double M = RADIAN * (134.963 + 13.064993 * d);
		
		// mean distance
		final double F = RADIAN * (93.272 + 13.229350 * d);
		
		// longitude
		final double l = L + RADIAN * 6.289 * Math.sin(M);
		
		// latitude
		final double b = RADIAN * 5.128 * Math.sin(F);
		
		return declination(l, b);
	}
	
	private static final double getLunarDistance(final double d) {
		// geocentric ecliptic coordinates of the moon

		// mean anomaly
		final double M = RADIAN * (134.963 + 13.064993 * d);

		// distance to the moon in km
		return 385001 - 20905 * Math.cos(M);
	}
	
	public static final double getLunarAzimuth(ZonedDateTime zdt, final double latitude, final double longitude) {
		final double lw = RADIAN * -longitude;
		final double phi = RADIAN * latitude;
		final double d = toDays(zdt);
		final double h = siderealTime(d, lw) - getLunarRightAscension(d);

		return azimuth(h, phi, getLunarDeclination(d)) + Math.PI;
	}
	
	public static final double getLunarAltitude(ZonedDateTime zdt, final double latitude, final double longitude) {
		final double lw = RADIAN * -longitude;
		final double phi = RADIAN * latitude;
		final double d = toDays(zdt);
		final double H = siderealTime(d, lw) - getLunarRightAscension(d);
		
		double h = altitude(H, phi, getLunarDeclination(d));
			
		h += astroRefraction(h);
		
		return h;
	}
	
	public static final double getLunarDistance(ZonedDateTime zdt) {
		final double d = toDays(zdt);
		return getLunarDistance(d);
	}
	
	public static final double getLunarParallacticAngle(ZonedDateTime zdt, final double latitude, final double longitude) {
		final double lw = RADIAN * -longitude;
		final double phi = RADIAN * latitude;
		final double d = toDays(zdt);
		final double H = siderealTime(d, lw) - getLunarRightAscension(d);
		return  Math.atan2(Math.sin(H), Math.tan(phi) * Math.cos(getLunarDeclination(d)) - Math.sin(getLunarDeclination(d)) * Math.cos(H));
	}

	public static final double getLunarIlluminationFraction(ZonedDateTime zdt) {
		final double d = toDays(zdt);
		final double phi = Math.acos(Math.sin(getSolarDeclination(d)) * Math.sin(getLunarDeclination(d))
				+ Math.cos(getSolarDeclination(d)) * Math.cos(getLunarDeclination(d)) * Math.cos(getSolarRightAscension(d) - getLunarRightAscension(d)));
		final double inc = Math.atan2(SOLAR_DISTANCE_TO_EARTH_KM * Math.sin(phi), getLunarDistance(d) - SOLAR_DISTANCE_TO_EARTH_KM * Math.cos(phi));
		
		return (1 + Math.cos(inc)) / 2;
	}
	
	public static final double getLunarPhase(ZonedDateTime zdt) {
		final double d = toDays(zdt);
		final double phi = Math.acos(Math.sin(getSolarDeclination(d)) * Math.sin(getLunarDeclination(d))
				+ Math.cos(getSolarDeclination(d)) * Math.cos(getLunarDeclination(d)) * Math.cos(getSolarRightAscension(d) - getLunarRightAscension(d)));
		final double inc = Math.atan2(SOLAR_DISTANCE_TO_EARTH_KM * Math.sin(phi), getLunarDistance(d) - SOLAR_DISTANCE_TO_EARTH_KM * Math.cos(phi));
		final double angle = Math.atan2(Math.cos(getSolarDeclination(d)) * Math.sin(getSolarRightAscension(d) - getLunarRightAscension(d)),
				Math.sin(getSolarDeclination(d)) * Math.cos(getLunarDeclination(d)) - Math.cos(getSolarDeclination(d)) * 
				Math.sin(getLunarDeclination(d)) * Math.cos(getSolarRightAscension(d) - getLunarRightAscension(d)));
		
		return 0.5 + 0.5 * inc * (angle < 0 ? -1 : 1) / Math.PI;
	}
	
	public static double getLunarPhaseAngle(ZonedDateTime zdt) {
		final double d = toDays(zdt);
		return Math.atan2(Math.cos(getSolarDeclination(d)) * Math.sin(getSolarRightAscension(d) - getLunarRightAscension(d)),
				Math.sin(getSolarDeclination(d)) * Math.cos(getLunarDeclination(d)) - Math.cos(getSolarDeclination(d)) * 
				Math.sin(getLunarDeclination(d)) * Math.cos(getSolarRightAscension(d) - getLunarRightAscension(d)));
	}

	private static ZonedDateTime hoursLater(ZonedDateTime ut1, double h) {
		final long s = (long) (h * 3600);
		return ut1.plusSeconds(s);
	}

	public static LunarPosition getLunarPosition(ZonedDateTime zdt, double lat, double lng) {

		zdt = zdt.withHour(0);
		zdt = zdt.withMinute(0);
		zdt = zdt.withSecond(0);
		zdt = zdt.withNano(0);
		
		LunarPosition reply = LunarPosition.VISIBLE;
		
		final double hc = 0.133 * RADIAN;
		
		double h0 = getLunarAltitude(zdt, lat, lng) - hc;
		double h1;
		double h2;
		double rise = 0;
		double set = 0;
		double a;
		double b;
		double xe;
		double ye = 0;
		double d;
		int roots;
		double x1 = 0;
		double x2 = 0;
		double dx;

		// go in 2-hour chunks, each time seeing if a 3-point quadratic curve crosses
		// zero (which means rise or set)
		for (int i = 1; i <= 24; i += 2) {
			h1 = getLunarAltitude(hoursLater(zdt, i), lat, lng) - hc;
			h2 = getLunarAltitude(hoursLater(zdt, i + 1.0), lat, lng) - hc;

			a = (h0 + h2) / 2 - h1;
			b = (h2 - h0) / 2;
			xe = -b / (2 * a);
			ye = (a * xe + b) * xe + h1;
			d = b * b - 4 * a * h1;
			roots = 0;

			if (d >= 0) {
				dx = Math.sqrt(d) / (Math.abs(a) * 2);
				x1 = xe - dx;
				x2 = xe + dx;
				if (Math.abs(x1) <= 1) {
					roots++;
				}
				if (Math.abs(x2) <= 1) {
					roots++;
				}
				if (x1 < -1) {
					x1 = x2;
				}
			}

			if (roots == 1) {
				if (h0 < 0) {
					rise = i + x1;
				} else {
					set = i + x1;
				}

			} else if (roots == 2) {
				rise = i + (ye < 0 ? x2 : x1);
				set = i + (ye < 0 ? x1 : x2);
			}

			if (!isZero(rise) && !isZero(set)) {
				break;
			}

			h0 = h2;
		}

		if (isZero(rise) && isZero(set)) {
			reply = (ye > 0 ? LunarPosition.ALWAYS_ABOVE_HORIZON : LunarPosition.ALWAYS_BELOW_HORIZON);
		}
		
		return reply;
	}
	
	public static ZonedDateTime getLunarSetTime(ZonedDateTime zdt, double lat, double lng) {

		zdt = zdt.withHour(0);
		zdt = zdt.withMinute(0);
		zdt = zdt.withSecond(0);
		zdt = zdt.withNano(0);
		
		final double hc = 0.133 * RADIAN;
		
		double h0 = getLunarAltitude(zdt, lat, lng) - hc;
		double h1;
		double h2;
		double rise = 0;
		double set = 0;
		double a;
		double b;
		double xe;
		double ye = 0;
		double d;
		int roots;
		double x1 = 0;
		double x2 = 0;
		double dx;

		// go in 2-hour chunks, each time seeing if a 3-point quadratic curve crosses
		// zero (which means rise or set)
		for (int i = 1; i <= 24; i += 2) {
			h1 = getLunarAltitude(hoursLater(zdt, i), lat, lng) - hc;
			h2 = getLunarAltitude(hoursLater(zdt, i + 1.0), lat, lng) - hc;

			a = (h0 + h2) / 2 - h1;
			b = (h2 - h0) / 2;
			xe = -b / (2 * a);
			ye = (a * xe + b) * xe + h1;
			d = b * b - 4 * a * h1;
			roots = 0;

			if (d >= 0) {
				dx = Math.sqrt(d) / (Math.abs(a) * 2);
				x1 = xe - dx;
				x2 = xe + dx;
				if (Math.abs(x1) <= 1) {
					roots++;
				}
				if (Math.abs(x2) <= 1) {
					roots++;
				}
				if (x1 < -1) {
					x1 = x2;
				}
			}

			if (roots == 1) {
				if (h0 < 0) {
					rise = i + x1;
				} else {
					set = i + x1;
				}

			} else if (roots == 2) {
				rise = i + (ye < 0 ? x2 : x1);
				set = i + (ye < 0 ? x1 : x2);
			}

			if (!isZero(rise) && !isZero(set)) {
				break;
			}

			h0 = h2;
		}

		ZonedDateTime zSet = hoursLater(zdt, set);
		
		final ZonedDateTime zRise = hoursLater(zdt, rise);
		
		if (zSet.isBefore(zRise)) {
			zSet = zSet.plusDays(1);
		}
		
		return zSet;
	}
	
	public static ZonedDateTime getLunarRiseTime(ZonedDateTime zdt, double lat, double lng) {
		
		zdt = zdt.withHour(0);
		zdt = zdt.withMinute(0);
		zdt = zdt.withSecond(0);
		zdt = zdt.withNano(0);
		
		final double hc = 0.133 * RADIAN;
		double h0 = getLunarAltitude(zdt, lat, lng) - hc;
		double h1;
		double h2;
		double rise = 0;
		double set = 0;
		double a;
		double b;
		double xe;
		double ye = 0;
		double d;
		int roots;
		double x1 = 0;
		double x2 = 0;
		double dx;
		
		// go in 2-hour chunks, each time seeing if a 3-point quadratic curve crosses
		// zero (which means rise or set)
		for (int i = 1; i <= 24; i += 2) {
			h1 = getLunarAltitude(hoursLater(zdt, i), lat, lng) - hc;
			h2 = getLunarAltitude(hoursLater(zdt, i + 1.0), lat, lng) - hc;

			a = (h0 + h2) / 2 - h1;
			b = (h2 - h0) / 2;
			xe = -b / (2 * a);
			ye = (a * xe + b) * xe + h1;
			d = b * b - 4 * a * h1;
			roots = 0;

			if (d >= 0) {
				dx = Math.sqrt(d) / (Math.abs(a) * 2);
				x1 = xe - dx;
				x2 = xe + dx;
				if (Math.abs(x1) <= 1) {
					roots++;
				}
				if (Math.abs(x2) <= 1) {
					roots++;
				}
				if (x1 < -1) {
					x1 = x2;
				}
			}

			if (roots == 1) {
				if (h0 < 0) {
					rise = i + x1;
				} else {
					set = i + x1;
				}

			} else if (roots == 2) {
				rise = i + (ye < 0 ? x2 : x1);
				set = i + (ye < 0 ? x1 : x2);
			}

			if (!isZero(rise) && !isZero(set)) {
				break;
			}

			h0 = h2;
		}

		return hoursLater(zdt, rise);
	}
	
	// USNO Method
//	public ZonedDateTime getGMST() {  
//		
//		double midnight = Math.floor(2458484.833333) + 0.5;         // J0 = 2458484.5
//		double days_since_midnight = 2458484.833333 - 2458484.5;  	// = 0.333333
//		double hours_since_midnight = 0.333333 * 24;              	// H = 8.0
//		double days_since_epoch = 2458484.833333 - 2451545.0;    	// D = 6939.833333
//		double centuries_since_epoch = days_since_epoch / 36525;  	// T = 0.190002
//		double whole_days_since_epoch = 2458484.5 - 2451545.0;    	// D0 = 6939.5
//
//		double GMST = 6.697374558 
//		     + 0.06570982441908 * whole_days_since_epoch 
//		     + 1.00273790935 * hours_since_midnight
//		     + 0.000026 * centuries_since_epoch * 2;        		// = 470.712605328
//
//		double GMST_hours = 470 % 24; 								// = 14
//		double GMST_minutes =  Math.floor(0.712605328 * 60); 		// = 42(.7563197)
//		double GMST_seconds =  0.7563197 * 60; 						// = 45.38
//		
//		
//	}
}
