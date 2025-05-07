package coverage;

import java.awt.Color;
import java.awt.Point;

import java.awt.geom.Point2D;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.util.ArrayList;
import java.util.List;

import geometry.CoordinateUtils;
import geometry.CoordinateUtils.Precision;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.coords.UTMCoord;

public class TestTile {

    private int id;
    private Color color;
    private int tileReference;
    private String testName;
    private int zone;
    private long easting;
    private long northing;
    private String latBand;
    private Point2D lonlat;
    private Point2D tileSize;
    private UTMCoord utmCoord;
    private Precision precision;
    private double sinad = 0;
    private double ber = 0;
    private double dBm = 0;
    private int measurementCount = 0;
    private boolean accessable;
    private String message = "";
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public TestTile() {
    }

    public TestTile(TestTile testTile) {
        this.tileReference = testTile.getTileReference();
        this.testName = testTile.getTestName();
        this.zone = testTile.getGridZone();
        this.easting = testTile.getEasting();
        this.northing = testTile.getNorthing();
        this.latBand = testTile.getLatBand();
        this.lonlat = testTile.getLonLat();
        this.precision = testTile.getPrecision();
        this.tileSize = testTile.getTileSize();
        this.ber = testTile.getAvgBer();
        this.dBm = testTile.getAvgdBm();
        this.id = testTile.getID();
        this.measurementCount = testTile.getMeasurementCount();
        this.accessable = testTile.isAccessable();
        this.message = testTile.getMessage();
        this.pcs = testTile.getPcs();
        this.sinad = testTile.getAvgSinad();
        this.utmCoord = testTile.getUtmCoord();
    }

    public TestTile(String message) {
        this.message = message;
    }

    public TestTile(String testName, Point2D lonlat, Point2D tileSize, Precision precision) {
        this(generatedTileReference(testName, lonlat, tileSize, precision), testName,
            UTMCoord.fromLatLon(Angle.fromDegreesLatitude(lonlat.getY()), 
            Angle.fromDegreesLongitude(lonlat.getX())), tileSize, precision);
    }

    public TestTile(int tileReference, String testName, Point2D lonlat, Point2D tileSize, Precision precision) {
        this(tileReference, testName, UTMCoord.fromLatLon(Angle.fromDegreesLatitude(lonlat.getY()),
                Angle.fromDegreesLongitude(lonlat.getX())), tileSize, precision);
    }

    public TestTile(String testName, Point2D lonlat, Point2D tileSize, int zone,
            String hemisphere, long easting, long northing, Precision precision) {
        this(generatedTileReference(testName, lonlat, tileSize, zone, hemisphere, easting, northing, precision),
                testName, lonlat, tileSize, zone, hemisphere, easting, northing, precision);
    }

    public TestTile(int tileReference, String testName, Point2D lonlat, Point2D tileSize, int zone,
            String hemisphere, long easting, long northing, Precision precision) {
        this.tileReference = tileReference;
        this.testName = testName;
        this.zone = zone;
        this.easting = easting;
        this.northing = northing;
        this.latBand = hemisphere;
        this.lonlat = lonlat;
        this.precision = precision;
        this.tileSize = tileSize;
        this.utmCoord = new UTMCoord(Angle.fromDegreesLatitude(lonlat.getY()), Angle.fromDegreesLongitude(lonlat.getX()), zone,
                hemisphere, easting, northing);
    }

    public TestTile(String testName, UTMCoord utmCoord, Point2D tileSize, Precision precision) {
        this(generatedTileReference(testName, utmCoord, tileSize, precision), testName, utmCoord, tileSize, precision);
    }

    public TestTile(int tileReference, String testName, UTMCoord utmCoord, Point2D tileSize, Precision precision) {
        this.tileReference = tileReference;
        this.testName = testName;
        this.utmCoord = utmCoord;
        this.precision = precision;
        this.tileSize = tileSize;
        this.easting = (long) utmCoord.getEasting();
        this.northing = (long) utmCoord.getNorthing();
        this.latBand = utmCoord.getHemisphere().equalsIgnoreCase(AVKey.NORTH) ? "N" : "S";
        this.lonlat = new Point.Double(utmCoord.getLongitude().getDegrees(), utmCoord.getLatitude().getDegrees());
        this.zone = utmCoord.getZone();
    }

    public String getMessage() {
        return message;
    }

    public int getID() {
        return this.id;
    }

    public void setID(int id) {
        this.id = id;
    }

    public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public void setTileReference(int tileReference) {
        this.tileReference = tileReference;
    }

    public int getTileReference() {
        return tileReference;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getTestName() {
        return testName;
    }

    public String getTileNameString() {
        return this.utmCoord.toString();
    }

    public void setTileSize(Point2D tileSize) {
        this.tileSize = tileSize;
    }

    public Point2D getTileSize() {
        return this.tileSize;
    }

    public int getGridZone() {
        return this.zone;
    }

    public Point2D getLonLat() {
        return this.lonlat;
    }

    public void setLonLat(Point2D lonlat) {
        this.lonlat = lonlat;
    }

    public List<Point2D> getTileCoordinates() {
    	List<Point2D> list = new ArrayList<>();
    		list.add(lonlat);
    		list.add(new Point2D.Double(lonlat.getX() + tileSize.getX(), lonlat.getY()));
    		list.add(new Point2D.Double(lonlat.getX() + tileSize.getX(), lonlat.getY() - tileSize.getY()));
    		list.add(new Point2D.Double(lonlat.getX(), lonlat.getY() - tileSize.getY()));
    	return list;
    }
    
    public String getLatBand() {
        return this.latBand;
    }

    public long getEasting() {
        return this.easting;
    }

    public long getNorthing() {
        return this.northing;
    }

    public Precision getPrecision() {
        return this.precision;
    }

    public void setEasting(long easting) {
        this.easting = easting;
    }

    public void setNorthing(long northing) {
        this.northing = northing;
    }

    public void setGridZone(int zone) {
        this.zone = zone;
    }

    public void setLatBand(String latBand) {
        this.latBand = latBand;
    }

    public void setPrecision(Precision precision) {
        this.precision = precision;
    }

    public void setPrecision(int ordinal) {
        this.precision = Precision.values()[ordinal];
    }

    public int getMeasurementCount() {
        return this.measurementCount;
    }

    public void incrementMeasurementCount() {
        this.measurementCount++;
    }

    public void decrementMeasurementCount() {
        this.measurementCount--;
    }

    public void setMeasurementCount(int measurementCount) {
        this.measurementCount = measurementCount;
    }

    public boolean isAccessable() {
        return accessable;
    }

    public void setAccessable(boolean accessable) {
        this.accessable = accessable;
    }

    public void setAvgSinad(double sinad) {
        this.sinad = sinad;
    }

    public void setAvgBer(double ber) {
        this.ber = ber;
    }

    public void setAvgdBm(double dBm) {
        this.dBm = dBm;
    }

    public void addSinadToAverage(double sinad) {
        this.sinad += sinad;
    }

    public double getAvgSinad() {
        if (this.measurementCount > 0) {
            return this.sinad / this.measurementCount;
        }
        return 0d;
    }

    public void addBerToAverage(double ber) {
        this.ber += ber;
    }

    public double getAvgBer() {
        if (this.measurementCount > 0) {
            return this.ber / this.measurementCount;
        }
        return 0;
    }

    public UTMCoord getUtmCoord() {
        return utmCoord;
    }

    public void setUtmCoord(UTMCoord utmCoord) {
        this.utmCoord = utmCoord;
    }

    public void adddBmToAverage(double dBm) {
        this.dBm += dBm;
    }

    public double getAvgdBm() {
        if (this.measurementCount > 0) {
            return this.dBm / this.measurementCount;
        }
        return 0;
    }

    public PropertyChangeSupport getPcs() {
        return pcs;
    }

    public void setPcs(PropertyChangeSupport pcs) {
        this.pcs = pcs;
    }

    @Override
    public String toString() {
        if (!message.equals("")) {
            return message;
        }
        return this.zone + " " + easting + "mE " + northing + "mN";
    }

    public String toFormattedTestTileDesignator() {
        return CoordinateUtils.utmCoordToTestTileString(this.utmCoord, this.precision);
    }

    public Object[] toObjectArray() {
        final Object[] obj = new Object[17];
        obj[0] = getID();
        obj[1] = getTileReference();
        obj[2] = getTestName();
        obj[3] = getEasting();
        obj[4] = getNorthing();
        obj[5] = getGridZone();
        obj[6] = getLonLat().getX();
        obj[7] = getLonLat().getY();
        obj[8] = getPrecision().ordinal();
        obj[9] = getLatBand();
        obj[10] = getAvgSinad();
        obj[11] = getAvgBer();
        obj[12] = getAvgdBm();
        obj[13] = getTileSize().getX();
        obj[14] = getTileSize().getY();
        obj[15] = getMeasurementCount();
        obj[16] = isAccessable();
        return obj;
    }

    public void fromObjectArray(Object[] obj) {
        setID((Integer) obj[0]);
        setTileReference((Integer) obj[1]);
        setTestName((String) obj[2]);
        setEasting((Long) obj[3]);
        setNorthing((Long) obj[4]);
        setGridZone((Integer) obj[5]);
        setLonLat(new Point2D.Double((double) obj[6], (double) obj[7]));
        setPrecision((Integer) obj[8]);
        setLatBand((String) obj[9]);
        setAvgSinad((double) obj[10]);
        setAvgBer((double) obj[11]);
        setAvgdBm((double) obj[12]);
        setTileSize(new Point2D.Double((double) obj[13], (double) obj[14]));
        setMeasurementCount((Integer) obj[15]);
        setAccessable((Boolean) obj[16]);
    }

    public static TestTile toTestTile(Object[] obj) {
        final TestTile testTile = new TestTile();
        testTile.setID((Integer) obj[0]);
        testTile.setTileReference((Integer) obj[1]);
        testTile.setTestName((String) obj[2]);
        testTile.setEasting((Long) obj[3]);
        testTile.setNorthing((Long) obj[4]);
        testTile.setGridZone((Integer) obj[5]);
        testTile.setLonLat(new Point2D.Double((double) obj[6], (double) obj[7]));
        testTile.setPrecision((Integer) obj[8]);
        testTile.setLatBand((String) obj[9]);
        testTile.setAvgSinad((double) obj[10]);
        testTile.setAvgBer((double) obj[11]);
        testTile.setAvgdBm((double) obj[12]);
        testTile.setTileSize(new Point2D.Double((double) obj[13], (double) obj[14]));
        testTile.setMeasurementCount((Integer) obj[15]);
        testTile.setAccessable((Boolean) obj[16]);
        return testTile;
    }

    public static TestTile copy(TestTile testTile) throws CloneNotSupportedException {
        if (!(testTile instanceof Cloneable)) {
            throw new CloneNotSupportedException("Clone Not Supported Exception");
        }
        return new TestTile(testTile);
    }

    public static int generatedTileReference(String testName, UTMCoord utmCoord, Point2D tileSize,
            Precision precision) {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((utmCoord == null) ? 0 : utmCoord.hashCode());
        result = (prime * result) + ((tileSize == null) ? 0 : tileSize.hashCode());
        result = (prime * result) + ((precision == null) ? 0 : precision.hashCode());
        result = (prime * result) + ((testName == null) ? 0 : testName.hashCode());
        return result;
    }

    public static int generatedTileReference(String testName, Point2D lonlat, Point2D tileSize, int zone,
            String hemisphere, long easting, long northing, Precision precision) {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((lonlat == null) ? 0 : lonlat.hashCode());
        result = (prime * result) + ((tileSize == null) ? 0 : tileSize.hashCode());
        result = (prime * result) + (int) (easting ^ (easting >>> 32));
        result = (prime * result) + ((hemisphere == null) ? 0 : hemisphere.hashCode());
        result = (prime * result) + (int) (northing ^ (northing >>> 32));
        result = (prime * result) + ((precision == null) ? 0 : precision.hashCode());
        result = (prime * result) + ((testName == null) ? 0 : testName.hashCode());
        result = (prime * result) + zone;
        return result;
    }

    public static int generatedTileReference(String testName, Point2D lonlat, Point2D tileSize,
            Precision precision) {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((lonlat == null) ? 0 : lonlat.hashCode());
        result = (prime * result) + ((tileSize == null) ? 0 : tileSize.hashCode());
        result = (prime * result) + ((precision == null) ? 0 : precision.hashCode());
        result = (prime * result) + ((testName == null) ? 0 : testName.hashCode());
        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + (int) (easting ^ (easting >>> 32));
        result = (prime * result) + ((latBand == null) ? 0 : latBand.hashCode());
        result = (prime * result) + (int) (northing ^ (northing >>> 32));
        result = (prime * result) + ((precision == null) ? 0 : precision.hashCode());
        result = (prime * result) + ((testName == null) ? 0 : testName.hashCode());
        result = (prime * result) + zone;
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
        final TestTile other = (TestTile) obj;
        if (easting != other.getEasting()) {
            return false;
        }
        if (latBand == null) {
            if (other.getLatBand() != null) {
                return false;
            }
        } else if (!latBand.equals(other.getLatBand())) {
            return false;
        }
        if (northing != other.getNorthing()) {
            return false;
        }
        if (precision != other.getPrecision()) {
            return false;
        }
        if (testName == null) {
            if (other.getTestName() != null) {
                return false;
            }
        } else if (!testName.equals(other.getTestName())) {
            return false;
        }
        return zone == other.getGridZone();
    }

    public boolean equalsLonLat(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (lonlat == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TestTile other = (TestTile) obj;
        if (other.getLonLat() == null) {
            return false;
        }
        if (lonlat.getX() != other.getLonLat().getX()) {
            return false;
        }
        return lonlat.getY() == other.getLonLat().getY();
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.removePropertyChangeListener(listener);
    }
}
