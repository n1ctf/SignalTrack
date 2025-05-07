package aprs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import java.util.Calendar;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.jxmapviewer.Style;
import org.openstreetmap.MapObjectImpl;

import components.Layer;

import geometry.Coordinate;
import geometry.ICoordinate;

import map.MapIcon;

public class AprsIconImpl extends MapObjectImpl implements MapIcon, AprsIcon {
	public static final String PAGE_0 = "/aprs-symbols-64-0@2x.png";
    public static final String PAGE_1 = "/aprs-symbols-64-1@2x.png";
    
    private static final Logger LOG = Logger.getLogger(AprsIconImpl.class.getName());
    
    private boolean flash;
    private BufferedImage image;
    private ICoordinate lonLat;
    private Dimension size;
    private int angle;
    private Calendar timeOfReport;
    private int courseMadeGood;
    private double speedMadeGood;
    private double altitude;
    private String callSign = "";
    private String ssid = "";

    

    public AprsIconImpl(Calendar timeOfReport, Point2D coords, String callSign, String ssid) {
        this(timeOfReport, coords, -1, -1, -1, callSign, ssid, null, null, new Layer("AbstractAprsProcessor"), null, getDefaultStyle());
    }

    public AprsIconImpl(Calendar timeOfReport, Point2D coords, double speedMadeGood, int courseMadeGood, double altitude,
            String callSign, String ssid, Character table, Character symbol) {
        this(timeOfReport, coords, speedMadeGood, courseMadeGood, altitude, callSign, ssid, table, symbol,
                new Layer("AbstractAprsProcessor"), null, getDefaultStyle());
    }

    public AprsIconImpl(Calendar timeOfReport, Point2D coords, double speedMadeGood, int courseMadeGood, double altitude,
            String callSign, String ssid, Character table, Character symbol, Layer layer, String name, Style style) {
        super(layer, name, style);

        this.timeOfReport = timeOfReport;
        this.lonLat = new Coordinate(coords);
        this.speedMadeGood = speedMadeGood;
        this.courseMadeGood = courseMadeGood;
        this.altitude = altitude;
        this.callSign = callSign;
        this.ssid = ssid;

        if (table != null && symbol != null) {
            image = new AprsSymbol(table, symbol).getImage();
        } else {
            image = new AprsSymbol(ssid).getImage();
        }

        if (timeOfReport == null) {
            this.timeOfReport = Calendar.getInstance();
        }
    }

    @Override
    public void setLonLat(ICoordinate lonLat) {
        this.lonLat = lonLat;
    }

    @Override
    public Dimension getSize() {
        return size;
    }

    @Override
    public boolean isFlash() {
        return flash;
    }

    @Override
    public void setFlash(boolean flash) {
        this.flash = flash;
    }

    @Override
    public void paint(Graphics gOrig, Point position, Dimension2D size, int angle) {
        Graphics2D g = (Graphics2D) gOrig.create();
        try {
            g.drawImage(scaleImage(rotate(image, angle), (int) size.getWidth(), (int) size.getHeight(), Color.WHITE),
                    (int) (position.x - (size.getWidth() / 2)), (int) (position.y - (size.getHeight() / 2)), null);
            if ((getLayer() == null) || Boolean.TRUE.equals(getLayer().isVisibleTexts())) {
                paintText(g, position);
            }
        } catch (NullPointerException | IllegalArgumentException ex) {
            LOG.log(Level.WARNING, ex.getMessage(), ex);
        } finally {
            g.dispose();
        }
    }

    private BufferedImage rotate(BufferedImage img, double angle) {
        double rads = Math.toRadians(angle);
        double sin = Math.abs(Math.sin(rads));
        double cos = Math.abs(Math.cos(rads));
        int w = img.getWidth();
        int h = img.getHeight();
        int newWidth = (int) Math.floor(w * cos + h * sin);
        int newHeight = (int) Math.floor(h * cos + w * sin);

        BufferedImage rotated = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = rotated.createGraphics();
        AffineTransform at = new AffineTransform();
        at.translate((newWidth - w) / 2D, (newHeight - h) / 2D);

        int x = w / 2;
        int y = h / 2;

        at.rotate(rads, x, y);
        g2d.setTransform(at);
        g2d.drawImage(img, 0, 0, null);
        g2d.dispose();

        return rotated;
    }

    private BufferedImage scaleImage(BufferedImage img, int width, int height, Color background) {
        int imgWidth = img.getWidth();
        int imgHeight = img.getHeight();
        if (imgWidth * height < imgHeight * width) {
            width = imgWidth * height / imgHeight;
        } else {
            height = imgHeight * width / imgWidth;
        }
        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = newImage.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setBackground(background);
            g.clearRect(0, 0, width, height);
            g.drawImage(img, 0, 0, width, height, null);
        } finally {
            g.dispose();
        }
        return newImage;
    }

    public static Style getDefaultStyle() {
        return new Style(new Color(255, 0, 0), new Color(255, 0, 0, 64), null, getDefaultFont());
    }

    @Override
    public String toString() {
        return "AprsIconImpl [lonLat=" + lonLat + ", timeOfReport=" + timeOfReport + ", courseMadeGood="
                + courseMadeGood + ", speedMadeGood=" + speedMadeGood + ", altitude=" + altitude + ", callSign="
                + callSign + ", ssid=" + ssid + "]";
    }

    @Override
    public void setLat(double lat) {
        lonLat.setLat(lat);
    }

    @Override
    public void setLon(double lon) {
        lonLat.setLon(lon);
    }

    @Override
    public ICoordinate getCoordinate() {
        return lonLat;
    }

    @Override
    public double getLat() {
        return lonLat.getLat();
    }

    @Override
    public double getLon() {
        return lonLat.getLon();
    }

    @Override
    public int getAngle() {
        return angle;
    }

    @Override
    public Calendar getTimeOfReport() {
        return timeOfReport;
    }

    @Override
    public int getCourseMadeGood() {
        return courseMadeGood;
    }

    @Override
    public double getSpeedMadeGood() {
        return speedMadeGood;
    }

    @Override
    public double getAltitude() {
        return altitude;
    }

    @Override
    public String getCallSign() {
        return callSign;
    }

    @Override
    public String getSsid() {
        return ssid;
    }

    @Override
    public BufferedImage getImage() {
        return image;
    }

    @Override
    public Point2D getLonLat() {
        return lonLat.getLonLat();
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((callSign == null) ? 0 : callSign.hashCode());
        result = prime * result + ((image == null) ? 0 : image.hashCode());
        result = prime * result + ((ssid == null) ? 0 : ssid.hashCode());
        return result;
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
     */
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
        AprsIconImpl other = (AprsIconImpl) obj;
        if (callSign == null) {
            if (other.callSign != null) {
                return false;
            }
        } else if (!callSign.equals(other.callSign)) {
            return false;
        }
        if (image == null) {
            if (other.image != null) {
                return false;
            }
        } else if (!image.equals(other.image)) {
            return false;
        }
        if (ssid == null) {
            if (other.ssid != null) {
                return false;
            }
        } else if (!ssid.equals(other.ssid)) {
            return false;
        }
        return true;
    }

}
