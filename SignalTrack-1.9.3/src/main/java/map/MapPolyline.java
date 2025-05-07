package map;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Stroke;
import java.util.List;

import components.Polyline;
import geometry.ICoordinate;

public interface MapPolyline extends MapObject {
	
	void setStroke(Stroke stroke);
	
	void setColor(Color color);
	
	void setPoints(List<? extends ICoordinate> points);
	
    List<? extends ICoordinate> getPoints();
    
    void paint(Graphics g, List<Point> points);
    
    void paint(Graphics g, Polyline polyline);
}
