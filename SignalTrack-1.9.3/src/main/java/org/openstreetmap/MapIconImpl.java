package org.openstreetmap;

import java.awt.BasicStroke;
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
import java.util.logging.Logger;

import org.jxmapviewer.Style;

import components.Layer;
import geometry.Coordinate;
import geometry.ICoordinate;
import map.MapIcon;

public class MapIconImpl extends MapObjectImpl implements MapIcon {

    private BufferedImage image;
    private ICoordinate lonLat;
    private Dimension size;
    private int angle;
    private boolean strikeout = false;

    private static final Logger log = Logger.getLogger(MapIconImpl.class.getName());

    public MapIconImpl(BufferedImage image, ICoordinate lonLat, Dimension size) {
        this(image, null, null, lonLat, size, getDefaultStyle());
    }

    public MapIconImpl(BufferedImage image, String name, ICoordinate lonLat, Dimension size) {
        this(image, null, name, lonLat, size, getDefaultStyle());
    }

    public MapIconImpl(BufferedImage image, Layer layer, ICoordinate lonLat, Dimension size) {
        this(image, layer, null, lonLat, size, getDefaultStyle());
    }

    public MapIconImpl(BufferedImage image, double lon, double lat, Dimension size) {
        this(image, null, null, new Coordinate(lon, lat), size, getDefaultStyle());
    }

    public MapIconImpl(BufferedImage image, Layer layer, double lon, double lat, Dimension size) {
        this(image, layer, null, new Coordinate(lon, lat), size, getDefaultStyle());
    }

    public MapIconImpl(BufferedImage image, Layer layer, String name, ICoordinate lonLat, Dimension size) {
        this(image, layer, name, lonLat, size, getDefaultStyle());
    }

    public MapIconImpl(BufferedImage image, Layer layer, String name, ICoordinate lonLat, Dimension size, Style style) {
        super(layer, name, style);
        this.image = image;
        this.lonLat = lonLat;
        this.size = size;
    }

    @Override
    public int getAngle() {
        return angle;
    }

    public void setAngle(int angle) {
        this.angle = angle;
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    @Override
    public Point2D getLonLat() {
        return lonLat.getLonLat();
    }

    @Override
    public void setLonLat(ICoordinate lonLat) {
        this.lonLat = lonLat;
    }

    @Override
    public Dimension getSize() {
        return size;
    }

    public void setSize(Dimension size) {
        this.size = size;
    }

    public boolean isStrikeout() {
        return strikeout;
    }

    public void setStrikeout(boolean strikeout) {
        this.strikeout = strikeout;
    }

    @Override
    public void paint(Graphics gOrig, Point position, Dimension2D size, int angle) {
        Graphics2D g = (Graphics2D) gOrig.create();
        try {
            Point ul = new Point((int) (position.x - (size.getWidth() / 2)),
                    (int) (position.y - (size.getHeight() / 2)));
            g.drawImage(scaleImage(rotate(image, angle), (int) size.getWidth(), (int) size.getHeight(), Color.WHITE),
                    ul.x, ul.y, null);
            if (strikeout) {
                g.setColor(Color.RED);
                g.setStroke(new BasicStroke(5));
                g.drawLine(ul.x, ul.y, ul.x + (int) size.getWidth(), ul.y + (int) size.getHeight());
            }
            if ((getLayer() == null) || Boolean.TRUE.equals(getLayer().isVisibleTexts())) {
                paintText(g, position);
            }
        } catch (NullPointerException | IllegalArgumentException e) {
            log.warning(e.getMessage());
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
        at.translate((newWidth - w) / 2d, (newHeight - h) / 2d);

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
        return new Style(new Color(255, 0, 0, 128), new Color(255, 0, 0, 64), null, getDefaultFont());
    }

    @Override
    public String toString() {
        return "MapIcon at " + getLon() + " " + getLat();
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
}
