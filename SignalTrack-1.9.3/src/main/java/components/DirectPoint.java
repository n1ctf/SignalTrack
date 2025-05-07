package components;

import java.awt.geom.Point2D;
import java.util.Objects;

public class DirectPoint {
	private Point2D destinationPoint;
	private double finalBearing;
	private double initialBearing;
	
	public DirectPoint(Point2D destinationPoint, double initialBearing, double finalBearing) {
		validateLonLat(destinationPoint);
		this.destinationPoint = destinationPoint;
		this.initialBearing = validateBearing(initialBearing);
		this.finalBearing = validateBearing(finalBearing);
	}

	public double getDestinationX() {
		return destinationPoint.getX();
	}
	
	public double getDestnationY() {
		return destinationPoint.getY();
	}
	
	public Point2D getDestinationPoint() {
		return destinationPoint;
	}

	public void setDestinationPoint(Point2D point) {
		this.destinationPoint = point;
	}

	public double getFinalBearing() {
		return finalBearing;
	}

	public void setFinalBearing(double finalBearing) {
		this.finalBearing = finalBearing;
	}

	public double getInitialBearing() {
		return initialBearing;
	}

	public void setInitialBearing(double initialBearing) {
		this.initialBearing = initialBearing;
	}

	public static void validateLonLat(Point2D point) {
		if ((point.getY() < -90.0) || (point.getY() > 90.0) || (point.getX() < -180.0) || (point.getX() > 180.0)) {
 			throw new IllegalArgumentException("Longitude = " + point.getX() + " / Latitude = " + point.getY());
		}
	}
	
	public static double validateBearing(double bearing) {
		while (bearing > 360.0) {
			bearing -= 360.0;
		}
		while (bearing < 0) {
			bearing += 360.0;
		}
		return bearing;
	}

	@Override
	public int hashCode() {
		return Objects.hash(finalBearing, initialBearing, destinationPoint);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof DirectPoint other)) {
			return false;
		}
		return Double.doubleToLongBits(finalBearing) == Double.doubleToLongBits(other.finalBearing)
				&& Double.doubleToLongBits(initialBearing) == Double.doubleToLongBits(other.initialBearing)
				&& Objects.equals(destinationPoint, other.destinationPoint);
	}

	@Override
	public String toString() {
		return "DirectPoint [destinationPoint=" + destinationPoint + ", finalBearing=" + finalBearing + ", initialBearing=" + initialBearing
				+ "]";
	}	
}
