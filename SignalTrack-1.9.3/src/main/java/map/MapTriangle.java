package map;

import java.awt.Graphics;
import java.awt.Point;

import geometry.ICoordinate;

public interface MapTriangle extends MapObject, ICoordinate {

    enum STYLE {
        FIXED,
        VARIABLE
    }

    ICoordinate getCoordinate();

    @Override
    double getLat();

    @Override
    double getLon();

    double getWidth();
    
    STYLE getMarkerStyle();

    void paint(Graphics g, Point position, double width);
}
