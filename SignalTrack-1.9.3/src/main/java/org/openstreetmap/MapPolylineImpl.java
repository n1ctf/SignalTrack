package org.openstreetmap;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.util.Arrays;
import java.util.List;

import org.jxmapviewer.Style;

import components.Layer;
import components.Polyline;
import geometry.ICoordinate;
import map.MapPolyline;

public class MapPolylineImpl extends MapObjectImpl implements MapPolyline {

    private List<? extends ICoordinate> points;

    public MapPolylineImpl(ICoordinate... points) {
        this(null, null, points);
    }

    public MapPolylineImpl(List<? extends ICoordinate> points) {
        this(null, null, points);
    }

    public MapPolylineImpl(String name, List<? extends ICoordinate> points) {
        this(null, name, points);
    }

    public MapPolylineImpl(String name, ICoordinate... points) {
        this(null, name, points);
    }

    public MapPolylineImpl(Layer layer, List<? extends ICoordinate> points) {
        this(layer, null, points);
    }

    public MapPolylineImpl(Layer layer, String name, List<? extends ICoordinate> points) {
        this(layer, name, points, getDefaultStyle());
    }

    public MapPolylineImpl(Layer layer, String name, ICoordinate... points) {
        this(layer, name, Arrays.asList(points), getDefaultStyle());
    }

    public MapPolylineImpl(Layer layer, String name, List<? extends ICoordinate> points, Style style) {
        super(layer, name, style);
        this.points = points;
    }

    public MapPolylineImpl(Layer layer, String name, Style style) {
        super(layer, name, style);
    }

    @Override
    public List<? extends ICoordinate> getPoints() {
        return this.points;
    }

    @Override
    public void paint(Graphics g, List<Point> points) {
        final Polyline polyline = new Polyline(points);
        paint(g, polyline);
    }

    private int[][] listToArray(List<Point> points) {
        final int[][] array = new int[2][points.size()];
        for (int i = 0; i < points.size(); i++) {
            array[0][i] = points.get(i).x;
            array[1][i] = points.get(i).y;
        }
        return array;
    }

    @Override
    public void paint(Graphics g, Polyline polyline) {

        final Color oldColor = g.getColor();
        g.setColor(getColor());

        Stroke oldStroke = null;
        if (g instanceof Graphics2D) {
            final Graphics2D g2 = (Graphics2D) g;
            oldStroke = g2.getStroke();
            g2.setStroke(getStroke());
        }

        final int[][] array = listToArray(polyline.getPoints());
        g.drawPolyline(array[0], array[1], points.size());

        g.setColor(oldColor);
        if (g instanceof Graphics2D) {
            ((Graphics2D) g).setStroke(oldStroke);
        }
        final Rectangle rec = polyline.getBounds();
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
        return "MapPolyline [points=" + points + "]";
    }

    @Override
    public void setPoints(List<? extends ICoordinate> points) {
        this.points = points;
    }

}
