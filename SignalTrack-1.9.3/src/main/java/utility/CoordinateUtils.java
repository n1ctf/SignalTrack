package utility;

import java.awt.Point;
import java.awt.geom.Point2D;

public final class CoordinateUtils {
	public static final double equatorialRadius = 6378137.0;
	public static final double polarRadius = 6356752.314;
	public static final double flattening = 0.00335281066474748; // (equatorialRadius-polarRadius)/equatorialRadius;
	public static final double inverseFlattening = 298.257223563; // 1/flattening;
	public static final double rho = 6368573.744;
	public static final double nu = 6389236.914;
	public static final double S = 5103266.421;
	public static final double rm = Math.pow(equatorialRadius * polarRadius, 1 / 2.0);
	public static final double k0 = 0.9996;
	public static final double e = Math.sqrt(1.0 - Math.pow(polarRadius / equatorialRadius, 2.0));
	public static final double e1sq = (e * e) / (1.0 - (e * e));
	public static final double n = (equatorialRadius - polarRadius) / (equatorialRadius + polarRadius);
	public static final double A0 = 6367449.146;
	public static final double A6 = -1.00541E-07;
	public static final double B0 = 16038.42955;
	public static final double C0 = 16.83261333;
	public static final double D0 = 0.021984404;
	public static final double E0 = 0.000312705;
	public static final double sin1 = 4.84814E-06;
	public static final double b = 6356752.314;
	public static final double a = 6378137.0;
	
	public static final String southernHemisphere = "ACDEFGHJKLM";
	
	private static double easting;
	private static double northing;
	
	private static double p = -0.483084;
	private static double k1 = 5101225.115;
	private static double k2 = 3750.291596;
	private static double k3 = 1.397608151;
	private static double k4 = 214839.3105;
	private static double k5 = -2.995382942;
	
	private static double phi1;
	
	private static double fact1;
	private static double fact2;
	private static double fact3;
	private static double fact4;
	
	private static double a3;
	
	private CoordinateUtils() {
		throw new IllegalStateException("Utility class");
	}

	public static Point convertLonLatToGridSquare(Point2D point, Point2D arcSeconds) {

		final Point gridSquare = new Point();

		if (Utility.isZero(arcSeconds.getX()) || Utility.isZero(arcSeconds.getY())) {
			return gridSquare;
		}

		validate(point);

		try {
			final double verticalPosition = point.getY() / (arcSeconds.getY() / 3600.0);
			final double horizontalEdge = (int) verticalPosition * (arcSeconds.getY() / 3600.0);
			final double horizontalPosition = point.getX() / (arcSeconds.getX() / 3600.0);
			final double verticalEdge = (int) horizontalPosition * (arcSeconds.getX() / 3600.0);

			final Point2D centerOfGrid = new Point2D.Double(verticalEdge + ((arcSeconds.getX() / 3600.0) * 0.5),
					horizontalEdge - ((arcSeconds.getY() / 3600.0) * 0.5));

			final String UTM = convertLonLatToUTM(centerOfGrid);

			final String[] utm = UTM.split(" ");

			if (utm.length == 4) {
				final double x = Integer.parseInt(utm[2].substring(2));
				while (utm[3].length() < 7) {
					utm[3] = "0" + utm[3];
				}
				final double y = Integer.parseInt(utm[3].substring(3));
				gridSquare.setLocation(x, y);
			}
		} catch (final NumberFormatException ex) {
			ex.printStackTrace();
		}

		return gridSquare;
	}
	
	public static Point2D convertLonLatToCenterOfGrid(Point2D point, Point2D arcSeconds) throws IllegalArgumentException {

		final Point2D centerOfGrid = new Point2D.Double();

		if (Utility.isZero(arcSeconds.getX()) || Utility.isZero(arcSeconds.getY())) {
			return centerOfGrid;
		}

		validate(point);

		final double verticalPosition = point.getY() / (arcSeconds.getY() / 3600.0);
		final double horizontalPosition = point.getX() / (arcSeconds.getX() / 3600.0);

		final double verticalEdge = (int) horizontalPosition * (arcSeconds.getX() / 3600.0);
		final double horizontalEdge = (int) verticalPosition * (arcSeconds.getY() / 3600.0);

		final double verticalCenterOfGrid;

		if (horizontalEdge < 0) {
			verticalCenterOfGrid = horizontalEdge - ((arcSeconds.getX() / 3600.0) * 0.5);
		} else {
			verticalCenterOfGrid = horizontalEdge + ((arcSeconds.getX() / 3600.0) * 0.5);
		}

		final double horizontalCenterOfGrid;

		if (verticalEdge < 0) {
			horizontalCenterOfGrid = verticalEdge - ((arcSeconds.getY() / 3600.0) * 0.5);
		} else {
			horizontalCenterOfGrid = verticalEdge + ((arcSeconds.getY() / 3600.0) * 0.5);
		}

		centerOfGrid.setLocation(horizontalCenterOfGrid, verticalCenterOfGrid);

		return centerOfGrid;
	}

	public static String convertLonLatToUTM(Point2D point) {

		validate(point);

		String utm = "";

		setVariables(point);

		final String longZone = getLongZone(point.getX());
		final LatZones latZones = new LatZones();
		final String latZone = latZones.getLatZone(point.getY());

		final double _easting = getEasting();
		final double _northing = getNorthing(point.getY());

		utm = longZone + " " + latZone + " " + ((int) _easting) + " " + ((int) _northing);

		return utm;
	}

	private static void validate(Point2D point) {
		if ((point.getY() < -90.0) || (point.getY() > 90.0) || (point.getX() < -180.0) || (point.getX() > 180.0)) {
			throw new IllegalArgumentException("Longitude = " + point.getX() + " / Latitude = " + point.getY());
		}
	}

	private static void setVariables(Point2D point) {
		final double y = (point.getY() * Math.PI) / 180.0;
		final double x = point.getX();

		final double nu = equatorialRadius / Math.pow(1.0 - Math.pow(e * Math.sin(y), 2.0), (1.0 / 2.0));

		final double var1 = x < 0.0 ? ((int) ((180.0 + x) / 6.0)) + 1.0 : ((int) (x / 6.0)) + 31D;

		final double var2 = (6.0 * var1) - 183.0;
		final double var3 = x - var2;

		p = (var3 * 3600.0) / 10000.0;

		final double s = ((((A0 * y) - (B0 * Math.sin(2 * y))) + (C0 * Math.sin(4 * y))) - (D0 * Math.sin(6 * y)))
				+ (E0 * Math.sin(8 * y));

		k1 = s * k0;
		k2 = (nu * Math.sin(y) * Math.cos(y) * Math.pow(sin1, 2) * k0 * (100000000)) / 2;
		k3 = ((Math.pow(sin1, 4) * nu * Math.sin(y) * Math.pow(Math.cos(y), 3)) / 24) * ((5 - Math.pow(Math.tan(y), 2))
				+ (9 * e1sq * Math.pow(Math.cos(y), 2)) + (4 * Math.pow(e1sq, 2) * Math.pow(Math.cos(y), 4))) * k0
				* (10000000000000000L);

		k4 = nu * Math.cos(y) * sin1 * k0 * 10000;

		k5 = Math.pow(sin1 * Math.cos(y), 3) * (nu / 6)
				* ((1 - Math.pow(Math.tan(y), 2)) + (e1sq * Math.pow(Math.cos(y), 2))) * k0 * 1000000000000L;
	}

	private static String getLongZone(double longitude) {
		final double longZone = longitude < 0.0 ? ((180.0 + longitude) / 6) + 1 : (longitude / 6) + 31;
		String val = String.valueOf((int) longZone);
		if (val.length() == 1) {
			val = "0" + val;
		}
		return val;
	}

	private static double getNorthing(double latitude) {
		double northing = k1 + (k2 * p * p) + (k3 * Math.pow(p, 4));
		if (latitude < 0.0) {
			northing += 10000000.0;
		}
		return northing;
	}

	private static double getEasting() {
		return 500000 + ((k4 * p) + (k5 * Math.pow(p, 3)));
	}

	private static class LatZones {
		private final char[] negLetters = { 'A', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M' };

		private final int[] negDegrees = { -90, -84, -72, -64, -56, -48, -40, -32, -24, -16, -8 };

		private final char[] posLetters = { 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Z' };

		private final int[] posDegrees = { 0, 8, 16, 24, 32, 40, 48, 56, 64, 72, 84 };

		public String getLatZone(double latitude) {
			int latIndex = -2;
			final int lat = (int) latitude;

			if (lat >= 0) {
				final int len = posLetters.length;
				for (int i = 0; i < len; i++) {
					if (lat == posDegrees[i]) {
						latIndex = i;
						break;
					}
					if (lat <= posDegrees[i]) {
						latIndex = i - 1;
						break;
					}
				}
			} else {
				final int len = negLetters.length;
				for (int i = 0; i < len; i++) {
					if (lat == negDegrees[i]) {
						latIndex = i;
						break;
					}
					if (lat < negDegrees[i]) {
						latIndex = i - 1;
						break;
					}
				}
			}

			if (latIndex == -1) {
				latIndex = 0;
			}
			if (lat >= 0) {
				if (latIndex == -2) {
					latIndex = posLetters.length - 1;
				}
				return String.valueOf(posLetters[latIndex]);
			} else {
				if (latIndex == -2) {
					latIndex = negLetters.length - 1;
				}
				return String.valueOf(negLetters[latIndex]);
			}
		}

	}

	private static String getHemisphere(String latZone) {
		String hemisphere = "N";
		if (southernHemisphere.contains(latZone)) {
			hemisphere = "S";
		}
		return hemisphere;
	}

	public static Point2D convertUTMToLatLong(String utm) {
		final double zoneCM;
		final int zone;
		final Point2D lonlat = new Point.Double();
		final String str = utm;
		final String[] u = utm.split(" ");
		zone = Integer.parseInt(u[0]);
		final String latZone = u[1];
		easting = Double.parseDouble(u[2]);
		northing = Double.parseDouble(u[3]);
		final String hemisphere = getHemisphere(latZone);
		double latitude;
		final double longitude;

		if ("S".equals(hemisphere)) {
			northing = 10000000 - northing;
		}
		
		setVariables();

		latitude = (180 * (phi1 - (fact1 * (fact2 + fact3 + fact4)))) / Math.PI;

		zoneCM = zone > 0 ? (6 * zone) - 183.0 : 3.0;

		longitude = zoneCM - a3;
		if ("S".equals(hemisphere)) {
			latitude = -latitude;
		}

		lonlat.setLocation(longitude, latitude);

		if ((lonlat.getY() < -90.0) || (lonlat.getY() > 90.0) || (lonlat.getX() < -180.0) || (lonlat.getX() > 180.0)) {
			throw new IllegalArgumentException("Input UTM = " + str);
		}

		return lonlat;
	}

	private static void setVariables() {
		final double a2;
		final double lof3;
		final double lof2;
		final double lof1;
		final double q0;
		final double t0;
		final double dd0;
		final double a1;
		final double r0;
		final double n0;
		final double cd;
		final double cc;
		final double cb;
		final double ca;
		final double ei;
		final double mu;
		final double arc;
		
		arc = northing / k0;
		mu = arc / (a * (1 - (Math.pow(e, 2) / 4.0) - ((3 * Math.pow(e, 4)) / 64.0) - ((5 * Math.pow(e, 6)) / 256.0)));

		ei = (1 - Math.pow((1 - (e * e)), (1 / 2.0))) / (1 + Math.pow((1 - (e * e)), (1 / 2.0)));

		ca = ((3 * ei) / 2) - ((27 * Math.pow(ei, 3)) / 32.0);

		cb = ((21 * Math.pow(ei, 2)) / 16) - ((55 * Math.pow(ei, 4)) / 32);
		cc = (151 * Math.pow(ei, 3)) / 96;
		cd = (1097 * Math.pow(ei, 4)) / 512;
		phi1 = mu + (ca * Math.sin(2 * mu)) + (cb * Math.sin(4 * mu)) + (cc * Math.sin(6 * mu))
				+ (cd * Math.sin(8 * mu));

		n0 = a / Math.pow((1 - Math.pow((e * Math.sin(phi1)), 2)), (1 / 2.0));

		r0 = (a * (1 - (e * e))) / Math.pow((1 - Math.pow((e * Math.sin(phi1)), 2)), (3 / 2.0));
		fact1 = (n0 * Math.tan(phi1)) / r0;

		a1 = 500000 - easting;
		dd0 = a1 / (n0 * k0);
		fact2 = (dd0 * dd0) / 2;

		t0 = Math.pow(Math.tan(phi1), 2);
		q0 = e1sq * Math.pow(Math.cos(phi1), 2);
		fact3 = (((5 + (3 * t0) + (10 * q0)) - (4 * q0 * q0) - (9 * e1sq)) * Math.pow(dd0, 4)) / 24;

		fact4 = (((61 + (90 * t0) + (298 * q0) + (45 * t0 * t0)) - (252 * e1sq) - (3 * q0 * q0)) * Math.pow(dd0, 6))
				/ 720;

		lof1 = a1 / (n0 * k0);
		lof2 = ((1 + (2 * t0) + q0) * Math.pow(dd0, 3)) / 6.0;
		lof3 = (((((5 - (2 * q0)) + (28 * t0)) - (3 * Math.pow(q0, 2))) + (8 * e1sq) + (24 * Math.pow(t0, 2)))
				* Math.pow(dd0, 5)) / 120;
		a2 = ((lof1 - lof2) + lof3) / Math.cos(phi1);
		a3 = (a2 * 180) / Math.PI;

	}
}
