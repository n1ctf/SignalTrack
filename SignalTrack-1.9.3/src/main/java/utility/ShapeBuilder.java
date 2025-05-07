package utility;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class ShapeBuilder implements Shape {
	private GeneralPath generalPath = new GeneralPath();

	public ShapeBuilder(Point2D.Double[] pointArray ) {
		generalPath = generalPath(pointArray);
	}
	
    private GeneralPath generalPath(Point2D.Double[] pointArray) {
    	final GeneralPath path = new GeneralPath();
    	path.moveTo(pointArray[0].x, pointArray[0].y);
    	for (int i = 1; i < pointArray.length; i++) {
	        final Point2D.Double n = pointArray[i]; 
	    	path.lineTo(n.x, n.y);
	    }
		return path;
    }    
    
	@Override
	public Rectangle getBounds() {
		return generalPath.getBounds();
	}

	@Override
	public Rectangle2D getBounds2D() {
		return generalPath.getBounds2D();
	}

	@Override
	public boolean contains(double x, double y) {
		return generalPath.contains(x, y);
	}

	@Override
	public boolean contains(Point2D p) {
		return generalPath.contains(p);
	}

	@Override
	public boolean intersects(double x, double y, double w, double h) {
		return generalPath.intersects(x, y, w, h);
	}

	@Override
	public boolean intersects(Rectangle2D r) {
		return generalPath.intersects(r);
	}

	@Override
	public boolean contains(double x, double y, double w, double h) {
		return generalPath.contains(x, y, w, h);
	}

	@Override
	public boolean contains(Rectangle2D r) {
		return generalPath.contains(r);
	}

	@Override
	public PathIterator getPathIterator(final AffineTransform at) {
		return generalPath.getPathIterator(at);
	}

	@Override
	public PathIterator getPathIterator(AffineTransform at, double flatness) {
		return generalPath.getPathIterator(at, flatness);
	}
}

