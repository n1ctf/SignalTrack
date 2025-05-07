package geometry;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Objects;

public class Coordinate implements ICoordinate {
	
	private static final double MAX_LON = 180.0;
	private static final double MIN_LON = -180.0;
	private static final double MAX_LAT = 90.0;
	private static final double MIN_LAT = -90.0;
	
	private Point2D data;
    
    public Coordinate(double lon, double lat) {
        data = new Point.Double(lon, lat);
    }
    
    public Coordinate(Point2D coord) {
    	data = coord;
    }
    
    @Override
    public double getLat() {
        return data.getY();
    }

    @Override
    public void setLat(double lat) {
        data.setLocation(data.getX(), lat);
    }

    @Override
    public double getLon() {
        return data.getX();
    }

    @Override
    public void setLon(double lon) {
        data.setLocation(lon, data.getY());
    }
    
    @Override
    public Point2D getLonLat() {
    	return new Point.Double(data.getX(), data.getY());
    }
    
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(data.getX());
        out.writeObject(data.getY());
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        data = new Point.Double((Double) in.readObject(), (Double) in.readObject());
    }

    @Override
    public String toString() {
        return "Coordinate[" + data.getX() + ", " + data.getY() + "]";
    }

    @Override
    public int hashCode() {
        var hash = 3;
        hash = (61 * hash) + Objects.hashCode(this.data);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Coordinate other = (Coordinate) obj;
        return Objects.equals(this.data, other.data);
    }

	public double getX() {
		return data.getX();
	}

	public double getY() {
		return data.getY();
	}

	public void setLocation(double x, double y) {
		data.setLocation(x, y);
	}

	public void setLocation(Point2D p) {
		data.setLocation(p);
	}

	public double distanceSq(double px, double py) {
		return data.distanceSq(px, py);
	}

	public double distanceSq(Point2D pt) {
		return data.distanceSq(pt);
	}

	public double distance(double px, double py) {
		return data.distance(px, py);
	}

	public double distance(Point2D pt) {
		return data.distance(pt);
	}
	

	public static boolean validLongitude(double longitude) {
		return (longitude <= MAX_LON && longitude >= MIN_LON);
	}
	
	public static boolean validLatitude(double latitude) {
		return (latitude <= MAX_LAT && latitude >= MIN_LAT);
	}
 
}
