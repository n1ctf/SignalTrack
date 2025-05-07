package org.openstreetmap;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Point2D;

import org.jxmapviewer.Style;

import components.Layer;
import geometry.Coordinate;
import map.MapTriangle;

public class MapMarkerTriangle extends MapObjectImpl implements MapTriangle {

    private Coordinate coord;
    private double width;
    private final STYLE markerStyle;
    
    public MapMarkerTriangle(Coordinate coord, double width) {
        this(null, null, coord, width);
    }

    public MapMarkerTriangle(String name, Coordinate coord, double width) {
        this(null, name, coord, width);
    }

    public MapMarkerTriangle(Layer layer, Coordinate coord, double width) {
        this(layer, null, coord, width);
    }

    public MapMarkerTriangle(double lon, double lat, double width) {
        this(null, null, new Coordinate(lon, lat), width);
    }

    public MapMarkerTriangle(Layer layer, double lon, double lat, double width) {
        this(layer, null, new Coordinate(lon, lat), width);
    }

    public MapMarkerTriangle(Layer layer, String name, Coordinate coord, double width) {
        this(layer, name, coord, width, STYLE.VARIABLE, getDefaultStyle());
    }

    public MapMarkerTriangle(Layer layer, String name, Coordinate coord, double width, STYLE markerStyle, Style style) {
        super(layer, name, style);
        this.markerStyle = markerStyle;
        this.coord = coord;
        this.width = width;
    }

    @Override
    public Coordinate getCoordinate() {
        return coord;
    }

    @Override
    public double getLat() {
        return coord.getLat();
    }

    @Override
    public double getLon() {
        return coord.getLon();
    }

    @Override
    public double getWidth() {
        return width;
    }

    @Override
    public STYLE getMarkerStyle() {
        return markerStyle;
    }
    
    public void setWidth(int width) {
    	this.width = width;
    }
    
    @Override
    public void paint(Graphics g, Point position, double width) {
        // Build triangle out of a polygon shape class
    	final Polygon triangle = new Polygon();
    	final Point p1 = new Point((int) Math.round(position.x - (width / 2)), (int) Math.round(position.y + (width / 2)));
    	final Point p2 = new Point((int) Math.round(position.x + (width / 2)), (int) Math.round(position.y + (width / 2)));
    	final Point p3 = new Point(position.x, (int) Math.round(position.y - (width / 2)));

        triangle.addPoint(p1.x, p1.y);
        triangle.addPoint(p2.x, p2.y);
        triangle.addPoint(p3.x, p3.y);

    	// Prepare graphics
        final Color oldColor = g.getColor();
        g.setColor(getColor());

        Stroke oldStroke = null;
        if (g instanceof Graphics2D) {
            final Graphics2D g2 = (Graphics2D) g;
            oldStroke = g2.getStroke();
            g2.setStroke(getStroke());
        }
        // Draw
        g.drawPolygon(triangle);
		if ((g instanceof Graphics2D) && (getBackColor() != null)) {
            final Graphics2D g2 = (Graphics2D) g;
            final Composite oldComposite = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
            g2.setPaint(getBackColor());
            g2.setComposite(oldComposite);
        }
        // Restore graphics
        g.setColor(oldColor);
        if (g instanceof Graphics2D) {
            ((Graphics2D) g).setStroke(oldStroke);
        }
        final Rectangle rec = triangle.getBounds();
        final Point corner = rec.getLocation();
        final Point p = new Point(corner.x+(rec.width/2), corner.y+(rec.height/2));
        if ((getLayer() == null) || Boolean.TRUE.equals(getLayer().isVisibleTexts())) {
			paintText(g, p);
		}
    }

    public static Style getDefaultStyle() {
        return new Style(new Color(255,0,0,128), new Color(255,0,0,64), null, getDefaultFont());
    }

    @Override
    public String toString() {
        return "MapTriangle at " + getLon() + " " + getLat();
    }

    @Override
    public void setLat(double lat) {
        if (coord == null) {
			coord = new Coordinate(lat, 0);
		} else {
			coord.setLat(lat);
		}
    }

    @Override
    public void setLon(double lon) {
        if (coord == null) {
			coord = new Coordinate(0, lon);
		} else {
			coord.setLon(lon);
		}
    }

	@Override
	public Point2D.Double getLonLat() {
		return new Point2D.Double(coord.getLon(), coord.getLat());
	}

}
