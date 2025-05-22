package geometry;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import utility.Utility;

public final class GeometryUtils {

	private GeometryUtils() {
		throw new IllegalStateException("Utility class");
	}
	
	public static synchronized double getAngleFromScreenCoords(Point2D origin, Point2D current) {
		if (origin == null || current == null) {
			return -1;
		}
		final double y = origin.getY() - current.getY();
		final double x = current.getX() - origin.getX();
		final double t = Math.abs(Math.toDegrees(Math.atan(y / x)));
		double r = 0;
		if (x > 0 && y > 0) {
			r = t + 90;
		}
		if (x > 0 && y < 0) {
			r = t;
		}
		if (x < 0 && y > 0) {
			r = t + 180;
		}
		if (x < 0 && y < 0) {
			r = t + 270;
		}
		return r;
	}

	public static synchronized Rectangle2D getRectangleCoordsFromScreenPoints(Point2D origin, Point2D current) {
		Rectangle2D r = null;
		if (origin != null && current != null) {
			final double t = getAngleFromScreenCoords(origin, current);
			final double width = Math.abs(origin.getX() - current.getX());
			final double height = Math.abs(origin.getY() - current.getY());
			if (t >= 0 && t < 90) {
				r = new Rectangle2D.Double(origin.getX(), origin.getY() + height, width, height);
			}
			if (t >= 90 && t < 180) {
				r = new Rectangle2D.Double(origin.getX(), origin.getY(), width, height);
			}
			if (t >= 180 && t < 270) {
				r = new Rectangle2D.Double(origin.getX() - width, origin.getY(), width, height);
			}
			if (t >= 270 && t < 360) {
				r = new Rectangle2D.Double(origin.getX() - width, origin.getY() + height, width, height);
			}
		}
		return r;
	}
	
	public static synchronized  Point2D getIntersectPoint(Line2D a, Line2D b) {
		final double d = ((a.getX1() - a.getX2()) * (b.getY1() - b.getY2()))
				- ((a.getY1() - a.getY2()) * (b.getX1() - b.getX2()));
		if (Utility.isZero(d)) {
			return null;
		}

		final double xi = (((b.getX1() - b.getX2()) * ((a.getX1() * a.getY2()) - (a.getY1() * a.getX2())))
				- ((a.getX1() - a.getX2()) * ((b.getX1() * b.getY2()) - (b.getY1() * b.getX2())))) / d;
		final double yi = (((b.getY1() - b.getY2()) * ((a.getX1() * a.getY2()) - (a.getY1() * a.getX2())))
				- ((a.getY1() - a.getY2()) * ((b.getX1() * b.getY2()) - (b.getY1() * b.getX2())))) / d;

		return new Point2D.Double(xi, yi);
	}
	
	public static synchronized boolean isInside(final RectangularShape outer, final RectangularShape inner) {
		return !(inner.getX() < outer.getX() || inner.getX() + inner.getWidth() > outer.getX() + outer.getWidth()
				|| inner.getY() > outer.getY() || inner.getY() - inner.getHeight() < outer.getY() - outer.getHeight());
	}
	
	public static synchronized List<Coordinate> getSouthWestCoordinatesOfAllTestTilesWithinRectangle(final Rectangle2D r, Collection<Rectangle2D> rectangles) {
		final List<Coordinate> list = new CopyOnWriteArrayList<>();
		rectangles.stream().filter(tile -> (GeometryUtils.isInside(r, tile)))
				.forEachOrdered(tile -> list.add(new Coordinate(tile.getX(), tile.getY() - tile.getHeight())));
		return list;
	}
	
}
