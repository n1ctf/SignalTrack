package geometry;

import java.awt.Dimension;
import java.awt.Point;

import java.awt.geom.Point2D;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import components.MapDimension;
import coverage.TestTile;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;

import gov.nasa.worldwind.geom.coords.MGRSCoord;
import gov.nasa.worldwind.geom.coords.UPSCoord;
import gov.nasa.worldwind.geom.coords.UTMCoord;

import gov.nasa.worldwind.globes.Earth;
import utility.Utility;

public class CoordinateUtils {

	private CoordinateUtils() {}
	
	/** The Constant rho. */
	public static final double rho = 6368573.744;

	/** The Constant A6. */
	public static final double A6 = -1.00541E-07;

	/** The Constant southernHemisphere. */
	public static final String southernHemisphere = "ACDEFGHJKLM";

	/** The Constant equatorialRadius. */
	public static final double equatorialRadius = 6378137.0;

	/** The Constant polarRadius. */
	public static final double polarRadius = 6356752.314;

	/** The Constant flattening. */
	public static final double flattening = 0.00335281066474748;

	/** The Constant inverseFlattening. */
	public static final double inverseFlattening = 298.257223563;

	/** The Constant rm. */
	public static final double rm = Math.pow(equatorialRadius * polarRadius, 1 / 2.0);

	/** The Constant k0. */
	public static final double k0 = 0.9996;

	/** The Constant e. */
	public static final double e = Math.sqrt(1.0 - Math.pow(polarRadius / equatorialRadius, 2.0));

	/** The Constant e1sq. */
	public static final double e1sq = (e * e) / (1.0 - (e * e));

	/** The Constant n. */
	public static final double n = (equatorialRadius - polarRadius) / (equatorialRadius + polarRadius);

	/** The Constant A0. */
	public static final double A0 = 6367449.146;

	/** The Constant B0. */
	public static final double B0 = 16038.42955;

	/** The Constant C0. */
	public static final double C0 = 16.83261333;

	/** The Constant D0. */
	public static final double D0 = 0.021984404;

	/** The Constant E0. */
	public static final double E0 = 0.000312705;

	/** The Constant sin1. */
	public static final double sin1 = 4.84814E-06;

	/** The Constant b. */
	public static final double b = 6356752.314;

	/** The Constant a. */
	public static final double a = 6378137.0;

	/**
	 * The Enum Precision.
	 */
	public enum Precision {

		/** The precision 1000 km. */
		PRECISION_1000_KM,
		/** The precision 100 km. */
		PRECISION_100_KM,
		/** The precision 10 km. */
		PRECISION_10_KM,
		/** The precision 1 km. */
		PRECISION_1_KM,
		/** The precision 100 m. */
		PRECISION_100_M,
		/** The precision 10 m. */
		PRECISION_10_M,

		/** The precision 1 m. */
		PRECISION_1_M
	}

	/**
	 * Lon lat to MGRS.
	 *
	 * @param lonlat    the lonlat
	 * @param precision the precision
	 * @return the MGRS coord
	 */
	public static synchronized MGRSCoord lonLatToMGRS(Point2D lonlat, int precision) {
		return MGRSCoord.fromLatLon(Angle.fromDegreesLatitude(lonlat.getY()), Angle.fromDegreesLongitude(lonlat.getX()),
				precision);
	}

	/**
	 * Gzd parse.
	 *
	 * @param mgrs the mgrs
	 * @return the string[]
	 */
	public static synchronized String[] gzdParse(final MGRSCoord mgrs) {
		final String[] ret = new String[2];
		final String s = mgrs.toString().substring(0, 3);
		ret[0] = "";
		ret[1] = "";
		Pattern pattern = Pattern.compile("\\d");
		Matcher matcher = pattern.matcher(s);
		while (matcher.find()) {
			ret[0] += matcher.group();
		}
		pattern = Pattern.compile("\\D");
		matcher = pattern.matcher(s);
		if (matcher.find()) {
			ret[1] = matcher.group();
		}
		return ret;
	}

	/**
	 * Lon lat to UPS.
	 *
	 * @param lonlat the lonlat
	 * @return the UPS coord
	 */
	public static synchronized UPSCoord lonLatToUPS(Point2D lonlat) {
		return UPSCoord.fromLatLon(Angle.fromDegreesLatitude(lonlat.getY()), Angle.fromDegreesLongitude(lonlat.getX()));
	}

	/**
	 * Utm to UPS.
	 *
	 * @param hemisphere the hemisphere
	 * @param easting    the easting
	 * @param northing   the northing
	 * @return the UPS coord
	 */
	public static synchronized UPSCoord utmToUPS(String hemisphere, double easting, double northing) {
		return UPSCoord.fromUTM(hemisphere, easting, northing);
	}

	/**
	 * Lon lat to UTM.
	 *
	 * @param lonlat the lonlat
	 * @return the UTM coord
	 */
	public static synchronized UTMCoord lonLatToUTM(Point2D lonlat) {
		return UTMCoord.fromLatLon(Angle.fromDegreesLatitude(lonlat.getY()), Angle.fromDegreesLongitude(lonlat.getX()));
	}

	/**
	 * Lon lat to UTM test tile.
	 *
	 * @param lonlat     the lonlat
	 * @param arcSeconds the arc seconds
	 * @return the test tile
	 */
	public static synchronized TestTile lonLatToUTMTestTile(Point2D lonlat, Point2D arcSeconds) {
		return new TestTile(null, lonLatToUTM(lonlat), arcSeconds, Precision.PRECISION_1_M);
	}

	/**
	 * Utm to lon lat.
	 *
	 * @param zone       the zone
	 * @param hemisphere the hemisphere
	 * @param easting    the easting
	 * @param northing   the northing
	 * @return the point 2 D
	 */
	public static synchronized Point2D utmToLonLat(int zone, String hemisphere, double easting, double northing) {
		final LatLon latLon = UTMCoord.locationFromUTMCoord(zone, hemisphere, easting, northing, new Earth());
		return new Point2D.Double(latLon.getLongitude().getDegrees(), latLon.getLatitude().getDegrees());
	}

	public static synchronized TestTile lonLatToTestTile(Point2D lonLat, Point2D arcSeconds, Point2D reference, Point2D gridSize) {
		if (arcSeconds == null || Utility.isZero(arcSeconds.getX()) || Utility.isZero(arcSeconds.getY())) {
			return null;
		}
		if (lonLat == null || lonLat.getY() < -90.0 || lonLat.getY() > 90.0 || lonLat.getX() < -180.0 || lonLat.getX() > 180.0) {
			return null;
		}
		if (reference == null) {
			return null;
		}
		if (gridSize == null) {
			return null;
		}

		final Point2D lowerRightCornerOfTile = lonLatToLowerLeftCornerOfTile(lonLat, arcSeconds, reference, gridSize);
		if (lowerRightCornerOfTile == null) {
			return new TestTile("Off Test Grid");
		}
		final UTMCoord utm = lonLatToUTM(lowerRightCornerOfTile);

		return utmToUTMTestTile(utm, arcSeconds, Precision.PRECISION_1_M);
	}

	public static synchronized Position lonLatToTestTilePosition(Point2D lonlat, Point2D arcSeconds, Point2D reference,
			Point2D gridSize) {
		if (Utility.isZero(arcSeconds.getX()) || Utility.isZero(arcSeconds.getY())) {
			return null;
		}
		if ((lonlat.getY() < -90.0) || (lonlat.getY() > 90.0) || (lonlat.getX() < -180.0) || (lonlat.getX() > 180.0)) {
			return null;
		}

		final Point2D pt = lonLatToLowerLeftCornerOfTile(lonlat, arcSeconds, reference, gridSize);

		if (pt == null) {
			return null;
		}

		return new Position(LatLon.fromDegrees(pt.getY(), pt.getX()), 0);
	}

	/**
	 * Utm coord to test tile string.
	 *
	 * @param utm       the utm
	 * @param precision the precision
	 * @return the string
	 */
	public static synchronized String utmCoordToTestTileString(UTMCoord utm, Precision precision) {
		if (utm == null) {
			return null;
		}
		try {
			final int easting = (int) utm.getEasting();
			final int northing = (int) utm.getNorthing();

			final String strEasting = String.valueOf(easting);
			final String strNorthing = String.valueOf(northing);

			int e = 0;
			int n = 0;

			switch (precision) {
				case PRECISION_1000_KM -> {
					e = Integer.parseInt(strEasting.substring(0, 1));
					n = Integer.parseInt(strNorthing.substring(0, 1));
				}
				case PRECISION_100_KM -> {
					e = Integer.parseInt(strEasting.substring(0, 2));
					n = Integer.parseInt(strNorthing.substring(0, 2));
				}
				case PRECISION_10_KM -> {
					e = Integer.parseInt(strEasting.substring(0, 3));
					n = Integer.parseInt(strNorthing.substring(0, 3));
				}
				case PRECISION_1_KM -> {
					e = Integer.parseInt(strEasting.substring(0, 4));
					n = Integer.parseInt(strNorthing.substring(0, 4));
				}
				case PRECISION_100_M -> {
					e = Integer.parseInt(strEasting.substring(0, 5));
					n = Integer.parseInt(strNorthing.substring(0, 5));
				}
				case PRECISION_10_M -> {
					e = Integer.parseInt(strEasting.substring(0, 6));
					n = Integer.parseInt(strNorthing.substring(0, 6));
				}
				case PRECISION_1_M -> {
					e = Integer.parseInt(strEasting.substring(0, 6));
					n = Integer.parseInt(strNorthing.substring(0, 7));
				}
			}

			final String zoneString = "%02d".formatted(utm.getZone())
					+ utm.getHemisphere().substring(0, 1).toUpperCase(Locale.US);

			return zoneString + " " + e + " " + n;
		} catch (final StringIndexOutOfBoundsException ex) {
			ex.printStackTrace();
			return "Error";
		}
	}

	/**
	 * Utm to UTM test tile.
	 *
	 * @param utm       the utm
	 * @param tileSize  the tile size
	 * @param precision the precision
	 * @return the test tile
	 */
	public static synchronized TestTile utmToUTMTestTile(UTMCoord utm, Point2D tileSize, Precision precision) {
		return new TestTile(null, utm, tileSize, precision);
	}

	/**
	 * Finds the lower left LonLat coordinate of the tile occupied by lonLat
	 *
	 * @param lonlat   the LonLat (X,Y) coordinates that are the subject of the
	 *                 request
	 * @param arcSec   The size of the tile in arc-seconds
	 * @param ulRef    the upper left corner of the grid
	 * @param gridSize the grid (X,Y) size in degrees
	 * @return the Lower left corner of the occupied tile
	 */
	public static synchronized Point2D lonLatToLowerLeftCornerOfTile(Point2D lonlat, Point2D tileSize, Point2D refPt,
			Point2D gridSize) {
		if (Utility.isZero(tileSize.getX()) || Utility.isZero(tileSize.getY())) {
			return null;
		}
		if (lonlat.getY() < -90.0 || lonlat.getY() > 90.0 || lonlat.getX() < -180.0 || lonlat.getX() > 180.0) {
			return null;
		}

		// 1 - get UL reference point
		// 2 - calculate X-Y degrees per tile
		// 3 - calculate how many degrees X-Y in from UL
		// 4 - calculate number of tiles X-Y from UL
		// 5 - calculate next lowest X-Y from step 4
		// 6 - add reference point to result
		// 7 - return lower left point

		final double verticalDistanceFromUL = refPt.getY() - lonlat.getY();
		final double horizontalDistanceFromUL = lonlat.getX() - refPt.getX();

		final double verticalEdge;
		final double horizontalEdge;

		if ((verticalDistanceFromUL >= 0) && (horizontalDistanceFromUL >= 0)
				&& (verticalDistanceFromUL <= gridSize.getY()) && (horizontalDistanceFromUL <= gridSize.getX())) {

			final int verticalTileNumber = (int) (verticalDistanceFromUL / (tileSize.getY() / 3600.0)) + 1;

			final int horizontalTileNumber = (int) (horizontalDistanceFromUL / (tileSize.getX() / 3600.0));

			verticalEdge = refPt.getX() + (horizontalTileNumber * (tileSize.getX() / 3600.0));

			horizontalEdge = refPt.getY() - (verticalTileNumber * (tileSize.getY() / 3600.0));

		} else {
			return null;
		}

		return new Point.Double(verticalEdge, horizontalEdge);
	}

	/**
	 * Coordinate to screen point.
	 *
	 * @param lonLat the lon lat
	 * @param mapDim the map dim
	 * @param screen the screen
	 * @return the point 2 D
	 */
	public static synchronized Point2D coordinateToScreenPoint(Point2D lonLat, MapDimension mapDim, Dimension screen) {
		final double x = screen.width - ((mapDim.getRightLongitude() - lonLat.getX())
				* (screen.width / mapDim.getMapDimensionInDegrees().getX()));
		final double y = screen.height + ((mapDim.getLowerLatitude() - lonLat.getY())
				* (screen.height / mapDim.getMapDimensionInDegrees().getY()));
		return new Point2D.Double(x, y);
	}

	/**
	 * Coordinate array to screen point array.
	 *
	 * @param coords the coords
	 * @param mapDim the map dim
	 * @param screen the screen
	 * @return the point 2 d[]
	 */
	public static synchronized Point2D[] coordinateArrayToScreenPointArray(Point2D.Double[] coords, MapDimension mapDim,
			Dimension screen) {
		final Point2D[] screenPoints = new Point2D.Double[coords.length];
		for (int i = 0; i < coords.length; i++) {
			screenPoints[i] = coordinateToScreenPoint(coords[i], mapDim, screen);
		}
		return screenPoints;
	}

}
