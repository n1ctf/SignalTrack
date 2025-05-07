package com.leelory.suncalc;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import utility.Utility;

import static java.lang.Math.*;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created with  IDEA.
 *
 * @author mastcer@gmail.com by leelory
 * @description a tiny java library for calculating sun/moon positions and phases
 * <p>
 * Based on a JavaScript library AstronomicalTime for calculating sun/moon position and light phases.
 * https://github.com/mourner/suncalc
 * @date 2019/5/18 : 11:39
 */
public final class SunCalc4JavaUtils {

	public static final double J0 = 0.0009;
    public static final double RAD = PI / 180;
    public static final double E = RAD * 23.4397;
    /**
     * sun calculations are based on http://aa.quae.nl/en/reken/zonpositie.html formulas
     * date/time constants and conversions
     */
    public static final double DAY_MS = 1000 * 60 * 60 * 24D;
    public static final double J1970 = 2440588;
    public static final double J2000 = 2451545;

    protected static List<Object> times = Lists.newArrayList(
            Lists.newArrayList(-0.833D, "sunrise", "sunset"),
            Lists.newArrayList(-0.3D, "sunriseEnd", "sunsetStart"),
            Lists.newArrayList(-6D, "dawn", "dusk"),
            Lists.newArrayList(-12D, "nauticalDawn", "nauticalDusk"),
            Lists.newArrayList(-18D, "nightEnd", "night"),
            Lists.newArrayList(6D, "goldenHourEnd", "goldenHour"));
    
    private SunCalc4JavaUtils() {
        throw new IllegalStateException("Utility class");
      }
    
    protected static double toJulian(Date date) {
        return date.getTime() / DAY_MS - 0.5 + J1970;
    }

    protected static Date fromJulian(double j) {
        return new Date((long) ((j + 0.5 - J1970) * DAY_MS));
    }

    protected static double toDays(Date date) {
        return toJulian(date) - J2000;
    }

    protected static double rightAscension(double l, double b) {
        return atan2(sin(l) * cos(E) - tan(b) * sin(E), cos(l));
    }

    protected static double declination(double l, double b) {
        return asin(sin(b) * cos(E) + cos(b) * sin(E) * sin(l));
    }

    protected static double azimuth(double h, double phi, double dec) {
        return atan2(sin(h), cos(h) * sin(phi) - tan(dec) * cos(phi));
    }

    protected static double altitude(double h, double phi, double dec) {
        return asin(sin(phi) * sin(dec) + cos(phi) * cos(dec) * cos(h));
    }

    protected static double siderealTime(double d, double lw) {
        return RAD * (280.16 + 360.9856235 * d) - lw;
    }

    protected static double astroRefraction(double h) {
        // the following formula works for positive altitudes only.
        if (h < 0) {
            // if h = -0.08901179 a div/0 would occur.
            h = 0;
        }
        // formula 16.4 of "Astronomical Algorithms" 2nd edition by Jean Meeus (Willmann-Bell, Richmond) 1998.
        // 1.02 / tan(h + 10.26 / (h + 5.10)) h in degrees, result in arc minutes -> converted to RAD:
        return 0.0002967 / tan(h + 0.00312536 / (h + 0.08901179));
    }

    protected static double solarMeanAnomaly(double d) {
        return RAD * (357.5291 + 0.98560028 * d);
    }

    protected static double eclipticLongitude(double m) {
        // equation of center
    	final double c = RAD * (1.9148 * sin(m) + 0.02 * sin(2 * m) + 0.0003 * sin(3 * m));
        // perihelion of the Earth
    	final double p = RAD * 102.9372;
        return m + c + p + PI;
    }

    protected static Map<String, Double> sunCoords(double d) {
    	final Map<String, Double> map = Maps.newConcurrentMap();
    	final double m = solarMeanAnomaly(d);
    	final double l = eclipticLongitude(m);
        map.put("dec", declination(l, 0));
        map.put("ra", rightAscension(l, 0));
        return map;
    }

    public static Map<String, Double> getPosition(Date date, double lat, double lng) {
    	final Map<String, Double> map = Maps.newConcurrentMap();
    	final double lw = RAD * -lng;
    	final double phi = RAD * lat;
    	final double d = toDays(date);
    	final Map<String, Double> c = sunCoords(d);
    	final double h = siderealTime(d, lw) - c.get("ra");
        map.put("azimuth", azimuth(h, phi, c.get("dec")));
        map.put("altitude", altitude(h, phi, c.get("dec")));
        return map;
    }

    /**
     * adds a custom time to the times config
     *
     * @param angle
     * @param riseName
     * @param setName
     */
    public static void addTime(double angle, String riseName, String setName) {
        times.add(Lists.newArrayList(angle, riseName, setName));
    }

    protected static double julianCycle(double d, double lw) {
        return round(d - J0 - lw / (2 * PI));
    }

    protected static double approxTransit(double ht, double lw, double n) {
        return J0 + (ht + lw) / (2 * PI) + n;
    }

    protected static double solarTransitJ(double ds, double m, double l) {
        return J2000 + ds + 0.0053 * sin(m) - 0.0069 * sin(2 * l);
    }

    protected static double hourAngle(double h, double phi, double d) {
        return acos((sin(h) - sin(phi) * sin(d)) / (cos(phi) * cos(d)));
    }

    protected static double getSetJ(double h, double lw, double phi, double dec, double n, double m, double l) {
    	final double w = hourAngle(h, phi, dec);
    	final double a = approxTransit(w, lw, n);
        return solarTransitJ(a, m, l);
    }

    public static Map<Object, Date> getTimes(Date date, double lat, double lng) {

    	final double lw = RAD * -lng;
    	final double phi = RAD * lat;

    	final double d = toDays(date);
        
    	final double n = julianCycle(d, lw);
    	final double ds = approxTransit(0, lw, n);

    	final double m = solarMeanAnomaly(ds);
    	final double l = eclipticLongitude(m);
    	final double dec = declination(l, 0);

    	final double jNoon = solarTransitJ(ds, m, l);

    	final Map<Object, Date> result = Maps.newConcurrentMap();
        
        result.put("solarNoon", fromJulian(jNoon));
        result.put("nadir", fromJulian(jNoon - 0.5));

        for (int i = 0, len = times.size(); i < len; i += 1) {
            @SuppressWarnings("unchecked")
            final List<Object> time = (List<Object>) times.get(i);
            final double jSet = getSetJ(Double.parseDouble(time.get(0).toString()) * RAD, lw, phi, dec, n, m, l);
            final double jRise = jNoon - (jSet - jNoon);

            result.put(time.get(1), fromJulian(jRise));
            result.put(time.get(2), fromJulian(jSet));
        }

        return result;
    }

    static Map<String, Double> moonCoords(double d) {
        // geocentric ecliptic coordinates of the moon
    	final Map<String, Double> result = Maps.newConcurrentMap();
        // ecliptic longitude
    	final double L = RAD * (218.316 + 13.176396 * d);
        // mean anomaly
    	final double M = RAD * (134.963 + 13.064993 * d);
        //// mean distance
    	final double F = RAD * (93.272 + 13.229350 * d);
        // longitude
    	final double l = L + RAD * 6.289 * sin(M);
        // latitude
    	final double b = RAD * 5.128 * sin(F);
        // distance to the moon in km
    	final double dt = 385001 - 20905 * cos(M);
        result.put("ra", rightAscension(l, b));
        result.put("dec", declination(l, b));
        result.put("dist", dt);
        return result;
    }

    public static Map<String, Double> getMoonPosition(Date date, double lat, double lng) {
    	final Map<String, Double> result = Maps.newConcurrentMap();
    	final double lw = RAD * -lng;
    	final double phi = RAD * lat;
    	final double d = toDays(date);

    	final Map<String, Double> c = moonCoords(d);
    	final double H = siderealTime(d, lw) - c.get("ra");
        double h = altitude(H, phi, c.get("dec"));
        // formula 14.1 of "Astronomical Algorithms" 2nd edition by Jean Meeus (Willmann-Bell, Richmond) 1998.
        final double pa = atan2(sin(H), tan(phi) * cos(c.get("dec")) - sin(c.get("dec")) * cos(H));
        // altitude correction for refraction
        h += astroRefraction(h);
        result.put("azimuth", azimuth(H, phi, c.get("dec")));
        result.put("altitude", h);
        result.put("distance", c.get("dist"));
        result.put("parallacticAngle", pa);
        return result;
    }

    public static Map<String, Double> getMoonIllumination(Date date) {
    	final Map<String, Double> result = Maps.newConcurrentMap();
    	final double d = toDays(date == null ? new Date() : date);
    	final Map<String, Double> s = sunCoords(d);
    	final Map<String, Double> m = moonCoords(d);
        // distance from Earth to Sun in km
    	final double sdist = 149598000;

    	final double phi = acos(sin(s.get("dec")) * sin(m.get("dec")) + cos(s.get("dec")) * cos(m.get("dec")) * cos(s.get("ra") - m.get("ra")));
    	final double inc = atan2(sdist * sin(phi), m.get("dist") - sdist * cos(phi));
    	final double angle = atan2(cos(s.get("dec")) * sin(s.get("ra") - m.get("ra")), sin(s.get("dec")) * cos(m.get("dec")) - cos(s.get("dec")) * sin(m.get("dec")) * cos(s.get("ra") - m.get("ra")));
        result.put("fraction", (1 + cos(inc)) / 2);
        result.put("phase", 0.5 + 0.5 * inc * (angle < 0 ? -1 : 1) / PI);
        result.put("angle", angle);
        return result;
    }

    static Date hoursLater(Date date, double h) {
        return new Date((long) (date.getTime() + h * DAY_MS / 24));
    }

    public static Map<String, Object> getMoonTimes(Date date, double lat, double lng) {
        return getMoonTimes(date, lat, lng, false);
    }

    public static Map<String, Object> getMoonTimes(Date date, double lat, double lng, boolean isUTC) {
    	final Map<String, Object> result = Maps.newConcurrentMap();
        //is GMT
        Calendar calendar = Calendar.getInstance();
        if (isUTC) {
            calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        }
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        final Date t = calendar.getTime();

        final double hc = 0.133 * RAD;
        double h0 = getMoonPosition(t, lat, lng).get("altitude") - hc;
        double h1;
        double h2;
        double rise = 0;
        double set = 0;
        double a;
        double b;
        double xe;
        double ye = 0;
        double d;
        double roots;
        double x1 = 0;
        double x2 = 0;
        double dx;

        // go in 2-hour chunks, each time seeing if a 3-point quadratic curve crosses zero (which means rise or set)
        for (int i = 1; i <= 24; i += 2) {
            h1 = getMoonPosition(hoursLater(t, i), lat, lng).get("altitude") - hc;
            h2 = getMoonPosition(hoursLater(t, i + 1D), lat, lng).get("altitude") - hc;

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

            if (Utility.equals(roots, 1)) {
                if (h0 < 0) {
                    rise = i + x1;
                } else {
                    set = i + x1;
                }

            } else if (Utility.equals(roots, 2)) {
                rise = i + (ye < 0 ? x2 : x1);
                set = i + (ye < 0 ? x1 : x2);
            }

            if (!Utility.equals(rise, 0) && !Utility.equals(set, 0)) {
                break;
            }

            h0 = h2;
        }

        if (!Utility.equals(rise, 0)) {
            result.put("rise", hoursLater(t, rise));
        }
        if (!Utility.equals(set, 0)) {
            result.put("set", hoursLater(t, set));
        }
        if (Utility.equals(rise, 0) && Utility.equals(set, 0)) {
            result.put(ye > 0 ? "alwaysUp" : "alwaysDown", true);
        }
        return result;
    }

}