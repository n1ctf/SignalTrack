package org.openstreetmap;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.util.Arrays;
import java.util.List;

import org.jxmapviewer.Style;

import components.Layer;
import geometry.ICoordinate;
import map.MapPolygon;

public class MapPolygonImpl extends MapObjectImpl implements MapPolygon {

    private final List<? extends ICoordinate> points;

    public MapPolygonImpl(ICoordinate... points) {
        this(null, null, points);
    }

    public MapPolygonImpl(List<? extends ICoordinate> points) {
        this(null, null, points);
    }

    public MapPolygonImpl(String name, List<? extends ICoordinate> points) {
        this(null, name, points);
    }

    public MapPolygonImpl(String name, ICoordinate... points) {
        this(null, name, points);
    }

    public MapPolygonImpl(Layer layer, List<? extends ICoordinate> points) {
        this(layer, null, points);
    }

    public MapPolygonImpl(Layer layer, String name, List<? extends ICoordinate> points) {
        this(layer, name, points, getDefaultStyle());
    }

    public MapPolygonImpl(Layer layer, String name, ICoordinate... points) {
        this(layer, name, Arrays.asList(points), getDefaultStyle());
    }

    public MapPolygonImpl(Layer layer, String name, List<? extends ICoordinate> points, Style style) {
        super(layer, name, style);
        this.points = points;
    }

    public MapPolygonImpl(Layer layer, String name, List<? extends ICoordinate> points, Style style, int id) {
        super(layer, name, style, id);
        this.points = points;
    }

    @Override
    public List<? extends ICoordinate> getPoints() {
        return this.points;
    }

    @Override
    public void paint(Graphics g, List<Point> points) {
        final Polygon polygon = new Polygon();
        for (final Point p : points) {
            polygon.addPoint(p.x, p.y);
        }
        paint(g, polygon);
    }

    @Override
    public void paint(Graphics g, Polygon polygon) {
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
        g.drawPolygon(polygon);
        if ((g instanceof Graphics2D) && (getBackColor() != null)) {
            final Graphics2D g2 = (Graphics2D) g;
            final Composite oldComposite = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
            g2.setPaint(getBackColor());
            g2.fillPolygon(polygon);
            g2.setComposite(oldComposite);
        }
        // Restore graphics
        g.setColor(oldColor);
        if (g instanceof Graphics2D) {
            ((Graphics2D) g).setStroke(oldStroke);
        }
        final Rectangle rec = polygon.getBounds();
        final Point corner = rec.getLocation();
        final Point p = new Point(corner.x + (rec.width / 2), corner.y + (rec.height / 2));
        if ((getLayer() == null) || Boolean.TRUE.equals(getLayer().isVisibleTexts())) {
            paintText(g, p);
        }
    }

    public static Style getDefaultStyle() {
        return new Style(Color.BLUE, new Color(100, 100, 100, 50), new BasicStroke(2), getDefaultFont());
    }

    @Override
    public String toString() {
        return "MapPolygon [points=" + points + "]";
    }
}
