package shapes;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Asymptote2D implements Shape {
	private Point2D.Double center;
    private double direction;
    private double catt;
    private double length;
    private transient Path2D path;

    public Asymptote2D(Point2D.Double center, double catt, double length) {
        this(center, 0, catt, length);
    }

    public Asymptote2D(Point2D.Double[] points) {
        path = path(Arrays.asList(points));
        center = points[1];
        catt = calculateCATT(Arrays.asList(points));
        direction = calculateDirection(Arrays.asList(points));
    }

    public Asymptote2D(List<Point2D.Double> pointArray) {
        path = path(pointArray);
        center = pointArray.get(1);
        catt = calculateCATT(pointArray);
        direction = calculateDirection(pointArray);
    }

    public Asymptote2D(Point2D.Double center, double direction, double catt, double length) {
        this.center = center;
        this.length = length;
        this.direction = direction;
        this.catt = catt;
        path = path(center, direction, catt, length);
    }

    public void setLength(double length) {
        this.length = length;
        path = path(center, direction, catt, length);
    }

    private double calculateDirection(List<Point2D.Double> pointArray) {
        final double anglePos = getInverseAngle(pointArray.get(1), pointArray.get(0));
        final double angleNeg = getInverseAngle(pointArray.get(1), pointArray.get(2));
        return Math.abs(anglePos - angleNeg) / 2D;
    }

    private double calculateCATT(List<Point2D.Double> pointArray) {
        final double anglePos = getInverseAngle(pointArray.get(1), pointArray.get(0));
        final double angleNeg = getInverseAngle(pointArray.get(1), pointArray.get(2));
        return Math.abs(anglePos - angleNeg);
    }

    private double getInverseAngle(Point2D.Double p1, Point2D.Double p2) {
        final double x = p2.x - p1.x;
        final double y = p2.y - p1.y;
        return Math.toDegrees(Math.atan(y / x));
    }

    private Path2D path(List<Point2D.Double> pointArray) {
        final Path2D p = new Path2D.Double();
        p.moveTo(pointArray.get(0).x, pointArray.get(0).y);
        p.lineTo(pointArray.get(1).x, pointArray.get(1).y);
        p.lineTo(pointArray.get(2).x, pointArray.get(2).y);
        return p;
    }

    private Path2D path(Point2D.Double center, double direction, double catt, double length) {
        final Path2D p = new Path2D.Double();
        final Point2D n = new Point2D.Double(length, Math.tan(Math.toRadians(catt)) * length);
        double tp = direction + catt;
        final double hp = Math.sqrt((n.getX() * n.getX()) + (n.getY() * n.getY()));
        double x = Math.sin(Math.toRadians(tp)) * hp;
        double y = Math.cos(Math.toRadians(tp)) * hp;
        p.moveTo(center.x + x, center.y - y);
        p.lineTo(center.x, center.y);
        tp = direction - catt;
        x = Math.sin(Math.toRadians(tp)) * hp;
        y = Math.cos(Math.toRadians(tp)) * hp;
        p.lineTo(center.x + x, center.y - y);
        return p;
    }

    public double getLength() {
        return length;
    }

    public double getDirection() {
        return direction;
    }

    public Point2D.Double getCenter() {
        return center;
    }

    public double getConicAngleToTarget() {
        return catt;
    }

    public void setDirection(double direction) {
        this.direction = direction;
        path = path(center, direction, catt, length);
    }

    public void setConicAngleToTarget(double catt) {
        this.catt = catt;
        path = path(center, direction, catt, length);
    }

    public void setCenter(Point2D.Double center) {
        this.center = center;
        path = path(center, direction, catt, length);
    }

    @Override
    public Rectangle getBounds() {
        return path.getBounds();
    }

    @Override
    public Rectangle2D getBounds2D() {
        return path.getBounds2D();
    }

    @Override
    public boolean contains(double x, double y) {
        return path.contains(x, y);
    }

    @Override
    public boolean contains(Point2D p) {
        return path.contains(p);
    }

    @Override
    public boolean intersects(double x, double y, double w, double h) {
        return path.intersects(x, y, w, h);
    }

    @Override
    public boolean intersects(Rectangle2D r) {
        return path.intersects(r);
    }

    @Override
    public boolean contains(double x, double y, double w, double h) {
        return path.contains(x, y, w, h);
    }

    @Override
    public boolean contains(Rectangle2D r) {
        return path.contains(r);
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at) {
        return path.getPathIterator(at);
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        return path.getPathIterator(at, flatness);
    }

	@Override
	public String toString() {
		return "Asymptote2D [center=" + center + ", direction=" + direction + ", catt=" + catt + ", length=" + length
				+ ", path=" + path + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(catt, center, direction, length);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Asymptote2D other)) {
			return false;
		}
		return Double.doubleToLongBits(catt) == Double.doubleToLongBits(other.catt)
				&& Objects.equals(center, other.center)
				&& Double.doubleToLongBits(direction) == Double.doubleToLongBits(other.direction)
				&& Double.doubleToLongBits(length) == Double.doubleToLongBits(other.length);
	}

}
