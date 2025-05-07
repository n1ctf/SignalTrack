package components;

import java.awt.geom.Point2D;

import utility.Vincenty;

public class SurfaceLine {
	private Point2D beginPoint;
	private Point2D endPoint;
	private double bearing;
	private double meters;
	
	public SurfaceLine() {}
	
	public SurfaceLine(double meters, double bearing, Point2D beginPoint) {
		this.meters = meters;
		this.bearing = bearing;
		this.beginPoint = beginPoint;
	}
	
	public SurfaceLine(double meters, double bearing, double latitude) {
		this.meters = meters;
		this.bearing = bearing;
		beginPoint = new Point2D.Double(-181, latitude);
	}
	
	public SurfaceLine(Point2D beginPoint, Point2D endPoint) {
		this.beginPoint = beginPoint;
		this.endPoint = endPoint;
		
		this.meters = Vincenty.distanceToOnSurface(beginPoint, endPoint);
		this.bearing = Vincenty.finalBearingTo(beginPoint, endPoint);
	}

	public void setMeters(double meters) {
		this.meters = meters;
	}

	public Point2D getBeginPoint() {
		return beginPoint;
	}

	public void setBeginPoint(Point2D beginPoint) {
		this.beginPoint = beginPoint;
	}

	public Point2D getEndPoint() {
		return endPoint;
	}

	public void setEndPoint(Point2D endPoint) {
		this.endPoint = endPoint;
	}

	public double getBearing() {
		return bearing;
	}

	public void setBearing(double bearing) {
		this.bearing = bearing;
	}
	
	public double getMeters() {
		return meters;
	}

	public double getKiloMeters() {
		return meters / 1E3;
	}
	
	public double getFeet() {
		 return Vincenty.metersToFeet(meters);
	}
	
	public double getMiles() {
		return Vincenty.metersToFeet(meters) / Vincenty.FEET_PER_MILE;
	}
	
	public double getArcSeconds() {
		if (beginPoint != null) {
			return Vincenty.metersToArcSeconds(meters, bearing, beginPoint.getY());
		} else {
			return -1;
		}
	}
}
