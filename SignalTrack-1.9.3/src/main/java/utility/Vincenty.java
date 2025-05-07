package utility;

import java.awt.Point;
import java.awt.geom.Point2D;

import components.DirectPoint;
import components.InversePoint;
import gavaghan.geodesy.Ellipsoid;
import gavaghan.geodesy.GeodeticCalculator;
import gavaghan.geodesy.GeodeticCurve;
import gavaghan.geodesy.GlobalCoordinates;

public final class Vincenty { // WGS-84
	
	public static final double EQUATORIAL_RADIUS = 6378137.0; // meters
	public static final double POLAR_RADIUS = 6356752.314245;
	public static final double FIRST_ECCENTRICITY = 0.08181919092890624;
	public static final double FIRST_ECCENTRICITY_SQUARED = 0.006694380004260827;
	public static final double SECOND_ECCENTRICITY = 0.08209443803685366;
	public static final double SECOND_ECCENTRICITY_SQUARED = 0.006739496756586903;
	public static final double FLATTENING = 0.0033528106718309896;
	public static final double FLATTENING_INVERSE = 298.2572229328697;
	public static final double FEET_PER_METER = 3.2808398950131234;
	public static final double FEET_PER_MILE = 5280.0;
	public static final double ASTRONOMICAL_UNIT_METERS = 149597870700.0;
	public static final double secondsPerJulianYear = 316880878;
	public static final double secondsPerSiderealYear = 31471982.4751328304;
	public static final double minutesPerJulianYear = 5281347.966666667;
	public static final double daysPerJulianYear = 365.25;
	public static final double daysPerGregorianYear = 365.2425;
	public static final double daysPerSiderealYear = 365.25636;
	public static final double daysPerTropicalYear = 365.242190402;
	public static final double hoursPerSidrealDay = 23.9344699;
	public static final double secondsPerSidrealDay = 86164.09053;
	public static final double hoursPerSolarDay = 24.0;
	public static final double minutesPerHour = 60.0;
	public static final double secondsPerMinute = 60.0;
	public static final double millisPerSecond = 1000.0;
	public static final double yearsUnixTimeAheadOfNTFSEpoc = 369.0;

	private Vincenty() {
		throw new IllegalStateException("Utility Class");
	}
	
	public static InversePoint getVincentyInverse(Point2D p1, Point2D p2) {
		return getVincentyInverse(Ellipsoid.WGS84, p1, p2);
	}
	
	public static InversePoint getVincentyInverse(Ellipsoid reference, Point2D p1, Point2D p2) {
		final GeodeticCalculator geoCalc = new GeodeticCalculator();
		final GlobalCoordinates a = new GlobalCoordinates(p1.getY(), p1.getX());
		final GlobalCoordinates b = new GlobalCoordinates(p2.getY(), p2.getX());
		final GeodeticCurve geoCurve = geoCalc.calculateGeodeticCurve(reference, a, b);
		final double ellipseMeters = geoCurve.getEllipsoidalDistance();
		return new InversePoint(ellipseMeters, geoCurve.getAzimuth(), geoCurve.getReverseAzimuth());
	}

	public static DirectPoint getVincentyDirect(Point2D p1, double initialBearing, double distance) { 
		return getVincentyDirect(Ellipsoid.WGS84, p1, initialBearing, distance);
	}
	
	public static DirectPoint getVincentyDirect(Ellipsoid reference, Point2D p1, double initialBearing, double distance) {
		final GeodeticCalculator geoCalc = new GeodeticCalculator();
		final GlobalCoordinates origin = new GlobalCoordinates(p1.getY(), p1.getX());
		final double[] endBearing = new double[1];
		final GlobalCoordinates dest = geoCalc.calculateEndingGlobalCoordinates(reference, origin, initialBearing, distance,
				endBearing);
		return new DirectPoint(new Point2D.Double(dest.getLongitude(), dest.getLatitude()), initialBearing,
				endBearing[0]);
	}

	public static void testVincenty() {
		final double bearing = 45;
		final double latitude = 10;

		final Point2D origin = new Point2D.Double(0.0, latitude);

		final Point2D directPoint = getVincentyDirect(origin, bearing, 1000).getDestinationPoint();

		final double meters = getVincentyInverse(origin, directPoint).getDistanceMeters();

		System.out.println("A VincentyDirect Point 1 kM away (on the surface) from the origin, at " + bearing
				+ " degrees : " + directPoint);

		System.out.println("The VincentyInverse distance in kiloMeters between the two points: " + meters / 1E3);

		final double differenceInDegrees = origin.distance(directPoint);

		System.out.println("Straight line distance in degrees between the two points, at " + bearing + " degrees: "
				+ differenceInDegrees);
		
		final double degFromKilos = getDegreesFromKilometers(1.0, bearing, latitude);
		
		System.out.println("getDegreesFromKilometers(" + 1.0 + " kM) : " + degFromKilos);

		final double kilosFromDeg = getKilometersFromDegrees(degFromKilos, bearing, latitude);
		
		System.out.println("getKilometersFromDegrees(" + degFromKilos + " degrees) : " + kilosFromDeg);
	}

	// taken from http://www.csgnetwork.com/degreelenllavcalc.html
	public static double getMetersPerDegreeOfLatitude(double latitude) {
		final double m1 = 111132.92; // latitude calculation term 1
		final double m2 = -559.82; // latitude calculation term 2
		final double m3 = 1.175; // latitude calculation term 3
		final double m4 = -0.0023; // latitude calculation term 4
		return  m1 + (m2 * Math.cos(2 * latitude)) + (m3 * Math.cos(4 * latitude)) + (m4 * Math.cos(6 * latitude));
	}
	
	// taken from http://www.csgnetwork.com/degreelenllavcalc.html
	public static double getMetersPerDegreeOfLongitude(double latitude) {
		final double p1 = 111412.84; // longitude calculation term 1
		final double p2 = -93.5; // longitude calculation term 2
		final double p3 = 0.118; // longitude calculation term 3
		return (p1 * Math.cos(latitude)) + (p2 * Math.cos(3 * latitude)) + (p3 * Math.cos(5 * latitude));
	}

	public static double getDegreesFromKilometers(double kilometers, double bearing, double latitude) {
		final Point2D origin = new Point2D.Double(0.0, latitude);
		final Point2D destination = getVincentyDirect(origin, bearing, kilometers * 1E3).getDestinationPoint();
		// We can use straight subtraction below because the origin and destination are in ellipsoidal surface 
		// compensated values, as provided by Vincenty's direct formula. 
		return Math.abs(origin.distance(destination));
	}

	public static double getKilometersFromDegrees(double degrees, double bearing, double latitude) {
		final double degreesFromOneKilometer = getDegreesFromKilometers(1.0, bearing, latitude); // = kilometers / degree
		// For example, if 1 kM = 0.5 degrees at the given latitude and bearing, and if the user wants to know 
		// how many kilometers are in, say, 2 degrees, you would divide 2 by 0.5, and the result would be 4 kM.
		// The solution therefore is: degreesFromOneKilometer / degrees
		return degreesFromOneKilometer / degrees;
	}

	public static double getKilometersPerDegree(double bearing, double latitude) {
		return getKilometersFromDegrees(1, bearing, latitude);
	}

	private static void validateLatitude(double latitude) {
		if ((latitude < -90.0) || (latitude > 90.0)) {
			throw new IllegalArgumentException("Latitude = " + latitude + " Degrees");
		}
	}

	private static void validateDegrees(double degrees) {
		if ((degrees < -180.0) || (degrees > 180.0)) {
			throw new IllegalArgumentException("Degrees = " + degrees);
		}
	}

	private static void validateArcSeconds(double arcSeconds) {
		if ((arcSeconds < (-180.0 * 3600.0)) || (arcSeconds > (180.0 * 3600.0))) {
			throw new IllegalArgumentException("ArcSeconds = " + arcSeconds);
		}
	}

	private static void validateLonLat(Point2D point) {
		if ((point.getY() < -90.0) || (point.getY() > 90.0) || (point.getX() < -180.0) || (point.getX() > 180.0)) {
			throw new IllegalArgumentException("Longitude = " + point.getX() + " / Latitude = " + point.getY());
		}
	}

	private static double getBearing(double bearing) {
		while (bearing >= 360.0) {
			bearing -= 360.0;
		}
		while (bearing < 0) {
			bearing += 360.0;
		}
		return bearing;
	}

	public static double degreesToArcSeconds(double degrees) {
		validateDegrees(degrees);
		return degrees * 3600.0;
	}

	public static double arcSecondsToDegrees(double arcSeconds) {
		validateArcSeconds(arcSeconds);
		return arcSeconds / 3600.0;
	}

	public static double feetToDegrees(double feet, double bearing, double latitude) {
		validateLatitude(latitude);
		return feetToArcSeconds(feet, getBearing(bearing), latitude) / 3600.0;
	}

	public static double metersToDegrees(double meters, double bearing, double latitude) {
		return metersToArcSeconds(meters, bearing, latitude) / 3600.0;
	}

	public static double feetToArcSeconds(double feet, double bearing, double latitude) {
		validateLatitude(latitude);
		final double kpd = getKilometersPerDegree(getBearing(bearing), latitude);
		final double fpd = kpd * FEET_PER_METER * 1000.0;
		final double dpf = 1.0 / fpd;
		return dpf * 3600.0 * feet;
	}

	public static float feetToArcSeconds(float feet, float bearing, float latitude) {
		return (float) feetToArcSeconds((double) feet, (double) bearing, (double) latitude);
	}

	public static double milesToDegrees(double miles, double bearing, double latitude) {
		validateLatitude(latitude);
		return feetToArcSeconds(miles * FEET_PER_MILE, getBearing(bearing), latitude) / 3600.0;
	}

	public static Point2D feetToArcSeconds(double feet, double latitude) {
		final double x = feetToArcSeconds(feet, 90, latitude);
		final double y = feetToArcSeconds(feet, 0, latitude);
		return new Point.Double(x, y);
	}

	public static double metersToArcSeconds(double meters, double bearing, double latitude) {
		validateLatitude(latitude);
		final double kpd = getKilometersPerDegree(getBearing(bearing), latitude);
		final double mpd = kpd * 1E3;
		final double dpm = 1.0 / mpd;
		return dpm * 3600.0 * meters;
	}

	public static double degreesToFeet(double degrees, double bearing, double latitude) {
		validateLatitude(latitude);
		validateDegrees(degrees);
		return arcSecondsToFeet(degrees * 3600.0, getBearing(bearing), latitude);
	}

	public static double degreesToMiles(double degrees, double bearing, double latitude) {
		validateLatitude(latitude);
		validateDegrees(degrees);
		return arcSecondsToFeet(degrees * 3600.0, getBearing(bearing), latitude) / FEET_PER_MILE;
	}

	public static Point2D degreesToFeet(double degrees, double latitude) {
		final double x = degreesToFeet(degrees, 90, latitude);
		final double y = degreesToFeet(degrees, 0, latitude);
		return new Point.Double(x, y);
	}

	public static double degreesToMeters(double degrees, double bearing, double latitude) {
		validateLatitude(latitude);
		validateDegrees(degrees);
		return arcSecondsToMeters(degrees * 3.6E3, getBearing(bearing), latitude);
	}

	public static Point2D degreesToMeters(double degrees, double latitude) {
		final double x = degreesToMeters(degrees, 90, latitude);
		final double y = degreesToMeters(degrees, 0, latitude);
		return new Point.Double(x, y);
	}

	public static double arcSecondsToFeet(double arcSeconds, double bearing, double latitude) {
		validateLatitude(latitude);
		validateArcSeconds(arcSeconds);
		final double kpd = getKilometersPerDegree(getBearing(bearing), latitude);
		final double fpd = kpd * 1E3 * FEET_PER_METER;
		return (arcSeconds * fpd) / 3.6E3;
	}

	public static double arcSecondsToMeters(double arcSeconds, double bearing, double latitude) {
		validateLatitude(latitude);
		validateArcSeconds(arcSeconds);
		final double kpd = getKilometersPerDegree(getBearing(bearing), latitude);
		final double mpd = kpd * 1000.0;
		return (arcSeconds * mpd) / 3600.0;
	}

	public static double metersToFeet(double meters) {
		return meters * FEET_PER_METER;
	}

	public static double feetToMeters(double feet) {
		return feet / FEET_PER_METER;
	}

	public static double degreesPerKilometer(double bearing, double latitude) {
		return 1.0 / getKilometersPerDegree(bearing, latitude);
	}

	public static double distanceToOnSurface(Point2D p1, Point2D p2) {
		validateLonLat(p1);
		validateLonLat(p2);
		try {
			return getVincentyInverse(p1, p2).getDistanceMeters();
		} catch (final Exception ae) {
			throw new IllegalArgumentException("Points do not converge. The distance can not be calculated.");
		}
	}

	public static double distanceToDirect(Point2D p1, double p1a, Point2D p2, double p2a) {
		validateLonLat(p1);
		validateLonLat(p2);
		final double dp = Math.abs(p1.distance(p2));
		final double dv = Math.abs(p1a - p2a);
		return Math.sqrt((dp * dp) + (dv * dv));
	}

	public static double initialBearingTo(Point2D p1, Point2D p2) {
		validateLonLat(p1);
		validateLonLat(p2);
		try {
			return getVincentyInverse(p1, p2).getInitialBearing();
		} catch (final Exception ae) {
			throw new IllegalArgumentException(
					"Points do not converge. The destination point can not be oberved from the originating point.");
		}
	}

	public static double finalBearingTo(Point2D p1, Point2D p2) {
		validateLonLat(p1);
		validateLonLat(p2);
		try {
			return getVincentyInverse(p1, p2).getFinalBearing();
		} catch (final Exception ae) {
			throw new IllegalArgumentException(
					"Points do not converge. The originating point can not be oberved from the destination point.");
		}
	}

	public static Point2D destinationPoint(Point2D p1, double initialBearing, double distance) {
		validateLonLat(p1);
		final DirectPoint directPoint = getVincentyDirect(p1, getBearing(initialBearing), distance);
		return directPoint.getDestinationPoint();
	}

	public static double finalBearingOn(Point2D p1, double initialBearing, double distance) {
		validateLonLat(p1);
		final DirectPoint directPoint = getVincentyDirect(p1, getBearing(initialBearing), distance);
		return directPoint.getFinalBearing();
	}

	public static void main(String[] args) {
		testVincenty();
	}
}
