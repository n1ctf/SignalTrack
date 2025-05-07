/*
 * MercatorUtils.java
 *
 * Created on October 7, 2006, 6:02 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.jxmapviewer.viewer.util;

import java.awt.Point;
import java.awt.geom.Point2D;

import geometry.ICoordinate;

/**
 * A utility class of methods that help when dealing with standard Mercator
 * projections.
 *
 * @author joshua.marinacci@sun.com
 */
public final class MercatorUtils {

	/**
	 * default tile size
	 */
	public static final int DEFAULT_TILE_SIZE = 256;
	/**
	 * maximum latitude (north) for Mercator display
	 */
	public static final double MAX_LAT = 85.05112877980659;
	/**
	 * minimum latitude (south) for Mercator display
	 */
	public static final double MIN_LAT = -85.05112877980659;
	/**
	 * equatorial earth radius for EPSG:3857 (Mercator)
	 */
	private static final double EARTH_RADIUS = 6378137;

	/**
	 * Creates a new instance of MercatorUtils
	 */

	private MercatorUtils() {
	}

	/**
	 * instance with tile size of 256 for easy conversions
	 */
	public static final MercatorUtils MERCATOR_256 = new MercatorUtils();

	/**
	 * @param longitudeDegrees the longitude in degrees
	 * @param radius           the world radius in pixels
	 * @return the x value
	 */
	public static int longToX(double longitudeDegrees, double radius) {
		final double longitude = Math.toRadians(longitudeDegrees);
		return (int) (radius * longitude);
	}

	/**
	 * @param latitudeDegrees the latitude in degrees
	 * @param radius          the world radius in pixels
	 * @return the y value
	 */
	public static int latToY(double latitudeDegrees, double radius) {
		final double latitude = Math.toRadians(latitudeDegrees);
		final double y = radius / 2.0 * Math.log((1.0 + Math.sin(latitude)) / (1.0 - Math.sin(latitude)));
		return (int) y;
	}

	/**
	 * @param x      the x value
	 * @param radius the world radius in pixels
	 * @return the longitude in degrees
	 */
	public static double xToLong(int x, double radius) {
		final double longRadians = x / radius;
		final double longDegrees = Math.toDegrees(longRadians);
		/*
		 * The user could have panned around the world a lot of times. Lat long goes
		 * from -180 to 180. So every time a user gets to 181 we want to subtract 360
		 * degrees. Every time a user gets to -181 we want to add 360 degrees.
		 */
		final int rotations = (int) Math.floor((longDegrees + 180) / 360);
		return longDegrees - (rotations * 360);
	}

	/**
	 * @param y      the y value
	 * @param radius the world radius in pixels
	 * @return the latitude in degrees
	 */
	public static double yToLat(int y, double radius) {
		final double latitude = (Math.PI / 2) - (2 * Math.atan(Math.exp(-1.0 * y / radius)));
		return Math.toDegrees(latitude);
	}

	public static double radius(int aZoomlevel) {
		return (DEFAULT_TILE_SIZE * (1 << aZoomlevel)) / (2.0 * Math.PI);
	}

	/**
	 * Returns the absolute number of pixels in y or x, defined as: 2^ZoomLevel *
	 * DEFAULT_TILE_SIZE where DEFAULT_TILE_SIZE is the width of a tile in pixels
	 *
	 * @param aZoomlevel zoom level to request pixel data
	 * @return number of pixels
	 */
	public static int getMaxPixels(int aZoomlevel) {
		return DEFAULT_TILE_SIZE * (1 << aZoomlevel);
	}

	public static int falseEasting(int aZoomlevel) {
		return getMaxPixels(aZoomlevel) / 2;
	}

	public static int falseNorthing(int aZoomlevel) {
		return (-1 * getMaxPixels(aZoomlevel)) / 2;
	}

	/**
	 * Transform pixelspace to coordinates and get the distance.
	 *
	 * @param x1        the first x coordinate
	 * @param y1        the first y coordinate
	 * @param x2        the second x coordinate
	 * @param y2        the second y coordinate
	 *
	 * @param zoomLevel the zoom level
	 * @return the distance
	 */
	public static double getDistance(int x1, int y1, int x2, int y2, int zoomLevel) {
		final double la1 = yToLat(y1, zoomLevel);
		final double lo1 = xToLon(x1, zoomLevel);
		final double la2 = yToLat(y2, zoomLevel);
		final double lo2 = xToLon(x2, zoomLevel);

		return getDistance(lo1, la1, lo2, la2);
	}

	/**
	 * Gets the distance using Spherical law of cosines.
	 *
	 * @param la1 the Latitude in degrees
	 * @param lo1 the Longitude in degrees
	 * @param la2 the Latitude from 2nd coordinate in degrees
	 * @param lo2 the Longitude from 2nd coordinate in degrees
	 * @return the distance
	 */
	public static double getDistance(double lo1, double la1, double lo2, double la2) {
		final double aStartLat = Math.toRadians(la1);
		final double aStartLong = Math.toRadians(lo1);
		final double aEndLat = Math.toRadians(la2);
		final double aEndLong = Math.toRadians(lo2);

		final double distance = Math.acos((Math.sin(aStartLat) * Math.sin(aEndLat))
				+ (Math.cos(aStartLat) * Math.cos(aEndLat) * Math.cos(aEndLong - aStartLong)));

		return EARTH_RADIUS * distance;
	}

	/**
	 * Transform longitude to pixelspace
	 *
	 *
	 * Mathematical optimization<br>
	 * <code>
	 * x = radius(aZoomlevel) * toRadians(aLongitude) + falseEasting(aZoomLevel)<br>
	 * x = getMaxPixels(aZoomlevel) / (2 * PI) * (aLongitude * PI) / 180 +
	 * getMaxPixels(aZoomlevel) / 2<br>
	 * x = getMaxPixels(aZoomlevel) * aLongitude / 360 + 180 *
	 * getMaxPixels(aZoomlevel) / 360<br>
	 * x = getMaxPixels(aZoomlevel) * (aLongitude + 180) / 360<br>
	 * </code>
	 *
	 *
	 * @param aLongitude [-180..180]
	 * @param aZoomlevel zoom level
	 * @return [0..2^ZoomLevel*TILE_SIZE[
	 */
	public static double lonToX(double aLongitude, int aZoomlevel) {
		final int mp = getMaxPixels(aZoomlevel);
		final double x = (mp * (aLongitude + 180L)) / 360L;
		return Math.min(x, mp);
	}

	/**
	 * Transforms latitude to pixelspace
	 *
	 * Mathematical optimization<br>
	 * <code>
	 * log(u) := log((1.0 + sin(toRadians(aLat))) / (1.0 - sin(toRadians(aLat))<br>
	 *
	 * y = -1 * (radius(aZoomlevel) / 2 * log(u)))) -
	 * falseNorthing(aZoomlevel))<br>
	 * y = -1 * (getMaxPixel(aZoomlevel) / 2 * PI / 2 * log(u)) - -1 *
	 * getMaxPixel(aZoomLevel) / 2<br>
	 * y = getMaxPixel(aZoomlevel) / (-4 * PI) * log(u)) +
	 * getMaxPixel(aZoomLevel) / 2<br>
	 * y = getMaxPixel(aZoomLevel) * ((log(u) / (-4 * PI)) + 1/2)<br>
	 * </code>
	 *
	 * @param aLat       [-90...90]
	 * @param aZoomlevel zoom level
	 * @return [0..2^ZoomLevel*TILE_SIZE[
	 */
	public static double latToY(double aLat, int aZoomlevel) {
		final double lat = Math.clamp(aLat, MIN_LAT, MAX_LAT);
		final double sinLat = Math.sin(Math.toRadians(lat));
		final double log = Math.log((1.0 + sinLat) / (1.0 - sinLat));
		final int mp = getMaxPixels(aZoomlevel);
		final double y = mp * (0.5 - (log / (4.0 * Math.PI)));
		return Math.min(y, mp - 1D);
	}

	/**
	 * Transforms pixel coordinate X to longitude
	 *
	 * 
	 * Mathematical optimization<br>
	 * <code>
	 * lon = toDegree((aX - falseEasting(aZoomlevel)) / radius(aZoomlevel))<br>
	 * lon = 180 / PI * ((aX - getMaxPixels(aZoomlevel) / 2) /
	 * getMaxPixels(aZoomlevel) / (2 * PI)<br>
	 * lon = 180 * ((aX - getMaxPixels(aZoomlevel) / 2) /
	 * getMaxPixels(aZoomlevel))<br>
	 * lon = 360 / getMaxPixels(aZoomlevel) * (aX - getMaxPixels(aZoomlevel) /
	 * 2)<br>
	 * lon = 360 * aX / getMaxPixels(aZoomlevel) - 180<br>
	 * </code>
	 * 
	 *
	 * @param aX         [0..2^ZoomLevel*TILE_WIDTH[
	 * @param aZoomlevel zoom level
	 * @return ]-180..180[
	 */
	public static double xToLon(int aX, int aZoomlevel) {
		return ((360d * aX) / getMaxPixels(aZoomlevel)) - 180.0;
	}

	/**
	 * Transforms pixel coordinate Y to latitude
	 *
	 * @param aY         [0..2^ZoomLevel*TILE_WIDTH[
	 * @param aZoomlevel zoom level
	 * @return [MIN_LAT..MAX_LAT] is about [-85..85]
	 */
	public static double yToLat(int aY, int aZoomlevel) {
		final int y = aY + falseNorthing(aZoomlevel);
		final double latitude = (Math.PI / 2) - (2 * Math.atan(Math.exp((-1.0 * y) / radius(aZoomlevel))));
		return -1 * Math.toDegrees(latitude);
	}

	public static Point lonLatToXY(double lon, double lat, int zoom) {
		return new Point((int) lonToX(lon, zoom), (int) latToY(lat, zoom));
	}

	public static Point2D.Double xyToLonLat(int x, int y, int zoom) {
		return new Point.Double(xToLon(x, zoom), yToLat(y, zoom));
	}

	public static Point2D.Double xyToLonLat(Point xy, int zoom) {
		return xyToLonLat(xy.x, xy.y, zoom);
	}

	public static Point lonLatToXY(ICoordinate coord, int zoom) {
		return lonLatToXY(coord.getLon(), coord.getLat(), zoom);
	}

}
