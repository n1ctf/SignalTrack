package components;

import java.awt.Color;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;

public class Bearing {
	private int index;
    private Point2D point;
    private double b;
    private double distance;
    private int quality;
    private Color color;

    public Bearing(int index, Point2D point, double b, double distance, int quality, Color color) {
        this.index = index;
    	this.point = point;
        this.b = b;
        this.distance = distance;
        this.quality = quality;
        this.color = color;
    }

    public int getIndex() {
    	return index;
    }
    
    public Point2D getBearingPosition() {
        return point;
    }

    public double getBearing() {
        return b;
    }
    
    public double getDistance() {
    	return distance;
    }
    
    public int getQuality() {
    	return quality;
    }
    
    public Color getColor() {
    	return color;
    }
    
    public void setIndex(int index) {
    	this.index = index;
    }
    
    public void setBearingPosition(Point2D point) {
    	this.point = point;
    }
    
    public void setBearing(double bearing) {
    	this.b = bearing;
    }
    
    public void setDistance(double distance) {
    	this.distance = distance;
    }
    
    public void setQuality(int quality) {
    	this.quality = quality;
    }
    
    public void setColor(Color color) {
    	this.color = color;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(b);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((color == null) ? 0 : color.hashCode());
		temp = Double.doubleToLongBits(distance);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + index;
		result = prime * result + quality;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Bearing other = (Bearing) obj;
		if (Double.doubleToLongBits(b) != Double.doubleToLongBits(other.b)) {
			return false;
		}
		if (color == null) {
			if (other.color != null) {
				return false;
			}
		} else if (!color.equals(other.color)) {
			return false;
		}
		if (Double.doubleToLongBits(distance) != Double.doubleToLongBits(other.distance)) {
			return false;
		}
		return index == other.index && quality == other.quality;
	}

	@Override
	public String toString() {
		return "Bearing [index=" + index + ", point=" + point + ", b=" + b + ", distance=" + distance + ", quality="
				+ quality + ", color=" + color + "]";
	}

	public double getX() {
		return point.getX();
	}

	public double getY() {
		return point.getY();
	}

	public void setLocation(double x, double y) {
		point.setLocation(x, y);
	}

	public void setLocation(Point2D p) {
		point.setLocation(p);
	}

	public double distanceSq(double px, double py) {
		return point.distanceSq(px, py);
	}

	public double distanceSq(Point2D pt) {
		return point.distanceSq(pt);
	}

	public double distance(double px, double py) {
		return point.distance(px, py);
	}

	public double distance(Point2D pt) {
		return point.distance(pt);
	}

	public int getRed() {
		return color.getRed();
	}

	public int getGreen() {
		return color.getGreen();
	}

	public int getBlue() {
		return color.getBlue();
	}

	public int getAlpha() {
		return color.getAlpha();
	}

	public int getRGB() {
		return color.getRGB();
	}

	public Color brighter() {
		return color.brighter();
	}

	public Color darker() {
		return color.darker();
	}

	public float[] getRGBComponents(float[] compArray) {
		return color.getRGBComponents(compArray);
	}

	public float[] getRGBColorComponents(float[] compArray) {
		return color.getRGBColorComponents(compArray);
	}

	public float[] getComponents(float[] compArray) {
		return color.getComponents(compArray);
	}

	public float[] getColorComponents(float[] compArray) {
		return color.getColorComponents(compArray);
	}

	public float[] getComponents(ColorSpace cspace, float[] compArray) {
		return color.getComponents(cspace, compArray);
	}

	public float[] getColorComponents(ColorSpace cspace, float[] compArray) {
		return color.getColorComponents(cspace, compArray);
	}

	public ColorSpace getColorSpace() {
		return color.getColorSpace();
	}

	public PaintContext createContext(ColorModel cm, Rectangle r, Rectangle2D r2d, AffineTransform xform,
			RenderingHints hints) {
		return color.createContext(cm, r, r2d, xform, hints);
	}

	public int getTransparency() {
		return color.getTransparency();
	} 
}
