package org.openstreetmap;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.List;

import org.jxmapviewer.Style;

import components.Layer;
import geometry.Coordinate;
import geometry.ICoordinate;
import map.MapRectangle;

public class MapRectangleImpl extends MapObjectImpl implements MapRectangle {

    private ICoordinate upperLeft;
    private ICoordinate lowerRight;

    public MapRectangleImpl(ICoordinate upperLeft, ICoordinate lowerRight) {
        this(null, null, upperLeft, lowerRight);
    }

    public MapRectangleImpl(String name, ICoordinate upperLeft, ICoordinate lowerRight) {
        this(null, name, upperLeft, lowerRight);
    }

    public MapRectangleImpl(Layer layer, ICoordinate upperLeft, ICoordinate lowerRight) {
        this(layer, null, upperLeft, lowerRight);
    }

    public MapRectangleImpl(Layer layer, String name, ICoordinate upperLeft, ICoordinate lowerRight) {
        this(layer, name, upperLeft, lowerRight, getDefaultStyle());
    }

    public MapRectangleImpl(Layer layer, String name, ICoordinate upperLeft, ICoordinate lowerRight, Style style) {
        super(layer, name, style);
        this.upperLeft = upperLeft;
        this.lowerRight = lowerRight;
    }

    @Override
    public void setUpperLeft(ICoordinate upperLeft) {
        this.upperLeft = upperLeft;
    }

    @Override
    public void setLowerRight(ICoordinate lowerRight) {
        this.lowerRight = lowerRight;
    }

    @Override
    public ICoordinate getUpperLeft() {
        return upperLeft;
    }

    @Override
    public ICoordinate getLowerRight() {
        return lowerRight;
    }

    @Override
    public List<ICoordinate> getPoints() {
        return Arrays.asList(
                getUpperLeft(),
                getLowerRight(),
                new Coordinate(getUpperLeft().getLon(), getLowerRight().getLat()),
                new Coordinate(getLowerRight().getLon(), getUpperLeft().getLat()));
    }

    @Override
    public Rectangle2D getRectangle2D() {
        return new Rectangle2D.Double(upperLeft.getLon(), upperLeft.getLat(),
                Math.abs(lowerRight.getLon() - upperLeft.getLon()), Math.abs(lowerRight.getLat() - upperLeft.getLat()));
    }

    @Override
    public void setRectangle(Rectangle2D rectangle) {
        upperLeft = new Coordinate(rectangle.getX(), rectangle.getY());
        lowerRight = new Coordinate(rectangle.getX() - rectangle.getWidth(), rectangle.getY() - rectangle.getHeight());
    }

    @Override
    public void paint(Graphics g, Point upperLeft, Point lowerRight) {
        // Prepare graphics
        final Color oldColor = g.getColor();
        g.setColor(getColor());
        Stroke oldStroke = null;
        if (g instanceof Graphics2D) {
            final Graphics2D g2 = (Graphics2D) g;
            oldStroke = g2.getStroke();
            g2.setStroke(getStroke());
        }
        final int width = Math.abs(lowerRight.x - upperLeft.x);
        final int height = Math.abs(lowerRight.y - upperLeft.y);
        // Draw
        g.drawRect(upperLeft.x, upperLeft.y, width, height);
        // Restore graphics
        g.setColor(oldColor);
        if (g instanceof Graphics2D) {
            ((Graphics2D) g).setStroke(oldStroke);
        }
        final Point p = new Point(upperLeft.x + (width / 2), upperLeft.y + (height / 2));
        if ((getLayer() == null) || Boolean.TRUE.equals(getLayer().isVisibleTexts())) {
            paintText(g, p);
        }
    }

    public static Style getDefaultStyle() {
        return new Style(Color.BLUE, null, new BasicStroke(2), getDefaultFont());
    }

    @Override
    public String toString() {
        return "MapRectangle from " + getUpperLeft() + " to " + getLowerRight();
    }
}
