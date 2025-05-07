package map;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Dimension2D;

import geometry.ICoordinate;

public interface MapIcon extends MapObject, ICoordinate {
    ICoordinate getCoordinate();

    @Override
    double getLat();

    @Override
    double getLon();
    
    int getAngle();
    
    void setVisible(boolean isVisible);
    
    Dimension2D getSize();
    
    void setLonLat(ICoordinate lonLat);
    
    void paint(Graphics g, Point position, Dimension2D size, int angle);
}
