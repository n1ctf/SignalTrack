package map;

import java.awt.Graphics;
import java.awt.Point;

import geometry.ICoordinate;

public interface MapMarker extends MapObject, ICoordinate {
	enum STYLE {
        FIXED,
        VARIABLE,
        ARC_SECONDS
    }

    ICoordinate getCoordinate();

    @Override
    double getLat();

    @Override
    double getLon();

    double getRadius();
    
    STYLE getMarkerStyle();

    void paint(Graphics g, Point position, int radius);
}
