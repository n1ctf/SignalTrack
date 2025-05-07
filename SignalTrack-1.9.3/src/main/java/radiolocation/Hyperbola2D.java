package radiolocation;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class Hyperbola2D implements Shape {
	private GeneralPath arcPath = new GeneralPath();
	private double a;
	private double b;
	private double d;
	private Point2D center;
	private double length;
	
	public Hyperbola2D() {}
	
	public Hyperbola2D(double a, double b, double d, Point2D center, double length) {
		this.a = a;
		this.b = b;
		this.d = d;
		this.center = center;
		this.length = length;
		createHyperbola(a, b, d, center, length);
	}

	private GeneralPath arcPath(Point2D.Double[] pointArray) {
    	final GeneralPath ap = new GeneralPath();
    	ap.moveTo(pointArray[0].getX(), pointArray[0].getY());
    	for (int i = 1; i < pointArray.length; i++) {
	        final Point2D n = pointArray[i]; 
	    	ap.lineTo(n.getX(), n.getY());
	    }
		return ap;
    }

    private void createHyperbola(double a, double b, double d, Point2D center, double length) {
		final Point2D.Double[] arcArray = createHyperbolicArc(b, a, length);
		final Point2D.Double[] pointArray =  createHyperbolicPointArray(arcArray, center, d);
		arcPath = arcPath(pointArray);
	}
    
    public static Point2D.Double[] createHyperbolicArc(double b, double a, double length) {
    	final Point2D.Double[] arc = new Point2D.Double[(int) (length - a)];
    	double x = a;
    	for (int i = 0; i < arc.length; i++) {
    		final double y2 = (1d - ((x*x) / (a*a))) * -(b*b);
    		final double y =  Math.sqrt(y2);
    		arc[i] = new Point2D.Double((int) Math.round(x), (int) Math.round(y));
    		x++;
    	}
    	return arc;
    }
    
    public static Point2D.Double[] createHyperbolicPointArray(Point2D.Double[] arcArray, Point2D center, double d) {
    	final Point2D.Double[] pa = new Point2D.Double[arcArray.length * 2];
    	for (int i = arcArray.length - 1; i >= 0; i--) {
    		final Point2D n = arcArray[i];
	        final double t = Math.toDegrees(Math.atan(n.getY()/n.getX()));
	        final double tp = d - t - 90;
	        final double hp = Math.sqrt((n.getX()*n.getX())+(n.getY()*n.getY()));
	        final double x = Math.cos(Math.toRadians(tp)) * hp;
	    	final double y = Math.sin(Math.toRadians(tp)) * hp;
	    	pa[arcArray.length - 1 - i] = new Point2D.Double(center.getX() + x, center.getY() + y);
    	}
    	for (int i = 0; i < arcArray.length; i++) {
    		final Point2D n = arcArray[i];
	        final double t = Math.toDegrees(Math.atan(-n.getY()/n.getX()));
	        final double tp = d - t - 90;
	        final double hp = Math.sqrt((n.getX()*n.getX())+(n.getY()*n.getY()));       
	        final double x = Math.cos(Math.toRadians(tp)) * hp;
	    	final double y = Math.sin(Math.toRadians(tp)) * hp; 
	    	pa[arcArray.length + i] = new Point2D.Double(center.getX() + x, center.getY() + y);
    	}
    	return pa;
    }

    public double getDistanceX(Point2D a, Point2D b) {
    	return Math.abs(b.getX() - a.getX());
    }

    public void setA(double a) {
    	this.a = a;
    	createHyperbola(a, b, d, center, length);
    }
    
    public void setB(double b) {
    	this.b = b;
    	createHyperbola(a, b, d, center, length);
    }
    
    public void setDirection(double d) {
    	this.d = d;
    	createHyperbola(a, b, d, center, length);
    }
    
    public void setCenter(Point2D center) {
    	this.center = center;
    	createHyperbola(a, b, d, center, length);
    }
    
    public void setLength(double length) {
    	this.length = length;
    	createHyperbola(a, b, d, center, length);
    }
    
    public void setHyperbola(double a, double b, double d, Point2D center, double length) {
    	this.a = a;
    	this.b = b;
    	this.d = d;
    	this.center = center;
    	this.length = length;
    }
    
    public double getA() {
    	return a;
    }
    
    public double getB() {
    	return b;
    }
    
    public double getDirection() {
    	return d;
    }
    
    public Point2D getCenter() {
    	return center;
    }
    
    public double getLength() {
    	return length;
    }
    
    public double getAngle() {
    	return Math.atan(a/b);
    }
    
	@Override
	public Rectangle getBounds() {
		return arcPath.getBounds();
	}

	@Override
	public Rectangle2D getBounds2D() {
		return arcPath.getBounds2D();
	}

	@Override
	public boolean contains(double x, double y) {
		return arcPath.contains(x, y);
	}

	@Override
	public boolean contains(Point2D p) {
		return arcPath.contains(p);
	}

	@Override
	public boolean intersects(double x, double y, double w, double h) {
		return arcPath.intersects(x, y, w, h);
	}

	@Override
	public boolean intersects(Rectangle2D r) {
		return arcPath.intersects(r);
	}

	@Override
	public boolean contains(double x, double y, double w, double h) {
		return arcPath.contains(x, y, w, h);
	}

	@Override
	public boolean contains(Rectangle2D r) {
		return arcPath.contains(r);
	}

	@Override
	public PathIterator getPathIterator(final AffineTransform at) {
		return arcPath.getPathIterator(at);
	}

	@Override
	public PathIterator getPathIterator(AffineTransform at, double flatness) {
		return arcPath.getPathIterator(at, flatness);
	}
}

