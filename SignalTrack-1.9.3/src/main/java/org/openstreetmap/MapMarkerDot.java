package org.openstreetmap;

import java.awt.Color;

import org.jxmapviewer.Style;

import components.Layer;
import geometry.Coordinate;

public class MapMarkerDot extends MapMarkerCircle {

    public static final int DOT_RADIUS = 5;

    public MapMarkerDot(Coordinate coord) {
        this(null, null, coord);
    }

    public MapMarkerDot(String name, Coordinate coord) {
        this(null, name, coord);
    }

    public MapMarkerDot(Layer layer, Coordinate coord) {
        this(layer, null, coord);
    }

    public MapMarkerDot(Layer layer, String name, Coordinate coord) {
        this(layer, name, coord, getDefaultStyle());
    }

    public MapMarkerDot(Color color, double lat, double lon) {
        this(null, null, lat, lon);
        setColor(color);
    }

    public MapMarkerDot(double lat, double lon) {
        this(null, null, lat, lon);
    }

    public MapMarkerDot(Layer layer, double lat, double lon) {
        this(layer, null, lat, lon);
    }

    public MapMarkerDot(Layer layer, String name, double lat, double lon) {
        this(layer, name, new Coordinate(lat, lon), getDefaultStyle());
    }

    public MapMarkerDot(Layer layer, String name, Coordinate coord, Style style) {
        super(layer, name, coord, DOT_RADIUS, STYLE.FIXED, style);
    }
    
    public MapMarkerDot(Coordinate coord, double radius, Style style) {
        super(null, null, coord, radius, STYLE.FIXED, style);
    }
    
    public static Style getDefaultStyle() {
        return new Style(Color.BLACK, Color.YELLOW, null, getDefaultFont());
    }
}
