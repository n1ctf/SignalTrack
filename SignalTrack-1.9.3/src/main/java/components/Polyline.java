package components;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

public class Polyline implements Shape {
	private final GeneralPath path = new GeneralPath();
	private List<Point> list;
	
	public Polyline() {}
	
	public Polyline(List<Point> list) {
		this.list = list;
		createPolyline(list);
	}

	private GeneralPath createPolyline(List<Point> list) {
    	final GeneralPath p = new GeneralPath();
    	p.moveTo(list.get(0).x, list.get(0).y);
    	for (int i = 1; i < list.size(); i++) {
	        final Point n = list.get(i); 
	    	p.lineTo(n.x, n.y);
	    }
		return p;
    }
	
	public List<Point> getPoints() {
		return list;
	}
	
	public void setPoints(List<Point> list) {
		this.list = list;
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
	public PathIterator getPathIterator(final AffineTransform at) {
		return path.getPathIterator(at);
	}

	@Override
	public PathIterator getPathIterator(AffineTransform at, double flatness) {
		return path.getPathIterator(at, flatness);
	}
}

