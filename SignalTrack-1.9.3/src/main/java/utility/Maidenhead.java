package utility;

import java.awt.geom.Point2D;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Maidenhead {
	private static final Logger LOG = Logger.getLogger(Maidenhead.class.getName());
    private String mh;

    public Maidenhead(String p1, String p2) {
    	final double lat;
    	final double lon;
        try {
            lat = Double.parseDouble(p1);
            lon = Double.parseDouble(p2);

            mh = lonLatToGridSquare(lon, lat);
        } catch (NumberFormatException e) {
            LOG.log(Level.WARNING, e.getMessage());
        }
    }

    public Maidenhead(double lon, double lat) {
        try {
            mh = lonLatToGridSquare(lon, lat);
        } catch (Exception e) {
            LOG.log(Level.WARNING, e.getMessage());
        }
    }

    public Maidenhead(Point2D lonLat) {
        this(lonLat.getX(), lonLat.getY());
    }

    public static String lonLatToGridSquare(Point2D lonLat) {
        try {
            return lonLatToGridSquare(lonLat.getX(), lonLat.getY());
        } catch (Exception e) {
            LOG.log(Level.WARNING, e.getMessage());
            return e.getMessage();
        }

    }

    public static String lonLatToGridSquare(double lon, double lat) {
        if (Double.isNaN(lat)) {
            throw new ArithmeticException("Lat is NaN");
        }
        if (Double.isNaN(lon)) {
            throw new ArithmeticException("Lon is NaN");
        }
        if (Math.abs(lat) > 89.9) {
            throw new ArithmeticException("Invalid at Poles");
        }
        if (Math.abs(lat) > 90) {
            throw new ArithmeticException("Invalid Latitude: " + lat);
        }
        if (Math.abs(lon) > 180) {
            throw new ArithmeticException("Invalid Longitude: " + lon);
        }

        final double adjLat;
        final double adjLon;
        final char GLat;
        final char GLon;
        final String nLat;
        final String nLon;
        final char gLat;
        final char gLon;
        final double rLat;
        final double rLon;
        final String U = "ABCDEFGHIJKLMNOPQRSTUVWX";
        final String L = U.toUpperCase(Locale.US);
        
        adjLat = lat + 90;
        adjLon = lon + 180;
        GLat = U.charAt((int) (adjLat / 10));
        GLon = U.charAt((int) (adjLon / 20));
        nLat = Integer.toString((int) (adjLat % 10));
        nLon = Integer.toString((int) ((adjLon / 2) % 10));
        rLat = (adjLat - (int) (adjLat)) * 60;
        rLon = (adjLon - 2 * (int) (adjLon / 2)) * 60;
        gLat = L.charAt((int) (rLat / 2.5));
        gLon = L.charAt((int) (rLon / 5));
        return "" + GLon + GLat + nLon + nLat + gLon + gLat;
    }

    @Override
    public String toString() {
        return mh;
    }

	@Override
	public int hashCode() {
		return Objects.hash(mh);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Maidenhead other)) {
			return false;
		}
		return Objects.equals(mh, other.mh);
	}
    
    
}
