package components;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import utility.Vincenty;

public class MapDimension {
	private final double upperLatitude;
	private final double lowerLatitude;
	private final double leftLongitude;
	private final double rightLongitude;
	private final Rectangle2D mapRectangle;
	
	public enum Hemisphere { NORTH, EAST, SOUTH, WEST }
	
	public MapDimension(Rectangle2D mapRectangle) {
		this(mapRectangle.getY(), mapRectangle.getX() + Math.abs(mapRectangle.getWidth()), mapRectangle.getY() - Math.abs(mapRectangle.getHeight()),
				mapRectangle.getX());
	}
	
	public MapDimension(double upperLatitude, double rightLongitude, double lowerLatitude, double leftLongitude) {
		this.upperLatitude = upperLatitude;
		this.rightLongitude = rightLongitude;
		this.lowerLatitude = lowerLatitude;
		this.leftLongitude = leftLongitude;
		mapRectangle = new Rectangle2D.Double(leftLongitude, upperLatitude, Math.abs(rightLongitude - leftLongitude),
				Math.abs(upperLatitude - lowerLatitude));
	}
	
	public Rectangle2D getMapRectangle() {
		return mapRectangle;
	}
	
	public Point2D getMapDimensionInArcSeconds() {
		final Point2D dimension = getMapDimensionInDegrees();
		return new Point.Double(dimension.getX() * 3600d, dimension.getY() * 3600D);
	}
	
	public Point2D getMapDimensionInDegrees() {
		final double latitude = Math.abs(upperLatitude - lowerLatitude);
		final double longitude = Math.abs(rightLongitude - leftLongitude);
		return new Point.Double(longitude, latitude);
	}
	
	public Point2D getMapDimensionInMeters() {
		return Vincenty.degreesToMeters(getMapDimensionInDegrees().getX(), lowerLatitude);
	}
	
	public Point2D getMapDimensionInFeet() {
		return Vincenty.degreesToFeet(getMapDimensionInDegrees().getX(), lowerLatitude);
	}
	
	public double getUpperLatitude() {
		return upperLatitude;
	}
	
	public double getLowerLatitude() {
		return lowerLatitude;
	}
	
	public double getRightLongitude() {
		return rightLongitude;
	}
	
	public double getLeftLongitude() {
		return leftLongitude;
	}
	
	public Hemisphere getLatitudenalHemisphere() {
		if ((upperLatitude + lowerLatitude) >= 0) {
			return Hemisphere.NORTH;
		} else {
			return Hemisphere.SOUTH;
		}
	}
	
	public Hemisphere getLongitudenalHemisphere() {
		if ((leftLongitude + rightLongitude) <= 0) {
			return Hemisphere.WEST;
		} else {
			return Hemisphere.EAST;
		}
	}
}
