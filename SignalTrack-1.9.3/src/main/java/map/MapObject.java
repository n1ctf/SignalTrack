package map;

import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;

import org.jxmapviewer.Style;

import components.Layer;


public interface MapObject {
    Layer getLayer();

    void setLayer(Layer layer);

    Style getStyle();

    Style getStyleAssigned();

    Color getColor();

    Color getBackColor();

    Stroke getStroke();

    Font getFont();

    String getName();
    
    int getID();

    boolean isVisible();
    
    void setTimeToDelete(long millis);
    
    long getTimeToDelete();
    
    void setTimeToGoStale(long millis);
    
    long getTimeToGoStale();
    
    void setFlash(boolean flash);
    
    boolean isFlash();
}
