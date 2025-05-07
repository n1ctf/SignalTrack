package components;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class CrissCross2D implements Shape {
	private double width;
	private double height;
	private double x;
	private double y;
	private GeneralPath path = null;
	
	public CrissCross2D() {}

	public CrissCross2D(double x, double y) {
		this(x, y, 6, 6);
	}
	
	public CrissCross2D(Point2D.Double position) {
		this(position.x, position.y, 6, 6);
	}
	
	public CrissCross2D(double x, double y, double width) {
		this(x, y, width, width);
	}
	
	public CrissCross2D(Point2D.Double position, double width) {
		this(position.x, position.y, width, width);
	}
	
	public CrissCross2D(Point2D.Double position, double width, double height) {
		this(position.x, position.y, width, height);
	}
	
	public CrissCross2D(Point2D.Double position, Dimension size) {
		this(position.x, position.y, size.width, size.height);
	}
	
	public CrissCross2D(double x, double y, Dimension size) {
		this(x, y, size.width, size.height);
	}
	
	public CrissCross2D(Rectangle2D.Double rectangle) {
		this(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
	}
	
	public CrissCross2D(double x, double y, double width, double height) {
		this.x = x;
		this.y = y;
		this.height = height;
		this.width = width;
		path = path(x, y, width, height);
	}

	private GeneralPath path(double x, double y, double width, double height) {
		final GeneralPath p = new GeneralPath();      
    	p.moveTo(x - (width / 2), y - (height / 2));
    	p.lineTo(x + (width / 2), y + (height / 2));
    	p.moveTo(x - (width / 2), y + (height / 2)); 
    	p.lineTo(x + (width / 2), y - (height / 2));
		return p;
	}

	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	
	public double getWidth() {
    	return width;
    }

	public double getHeight() {
    	return height;
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

}
