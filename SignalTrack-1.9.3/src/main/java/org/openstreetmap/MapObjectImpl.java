package org.openstreetmap;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import javax.swing.UIManager;

import org.jxmapviewer.Style;

import components.Layer;

public abstract class MapObjectImpl {
    private Layer layer;
    private String name;
    private Style style;
    private Boolean visible;
    private int id;
    private long timeToDelete;
    private long timeToGoStale;

    private Style normalizedStyle;
    private Style flashIntervalStyle;
    private boolean flash;
    private ActionListener flashTimerActionListener = _ -> flashTimerActionListenerEvent();
    private Timer flashTimer = new Timer(250, flashTimerActionListener);
    
    protected MapObjectImpl() {
    	this(null, null, null, 0);
    }
    
    protected MapObjectImpl(Style style) {
        this(null, null, style, 0);
    }
    
    protected MapObjectImpl(String name) {
        this(null, name, null, 0);
    }
    
    protected MapObjectImpl(Layer layer) {
        this(layer, null, null, 0);
    }
    
    protected MapObjectImpl(Layer layer, Style style) {
        this(layer, null, style, 0);
    }
    
    protected MapObjectImpl(Layer layer, String name, Style style) {
        this(layer, name, style, 0);
    }
    
    protected MapObjectImpl(String name, Style style) {
        this(null, name, style, 0);
    }
    
    protected MapObjectImpl(Layer layer, String name) {
        this(layer, name, null, 0);
    }

    protected MapObjectImpl(Layer layer, String name, Style style, int id) {
        super();
        this.layer = layer;
        this.name = name;
        this.style = style;
        this.id = id;
        normalizedStyle = new Style(style.getColor(), style.getBackColor(), style.getStroke(), style.getFont());
        Color flashIntervalBackColor = new Color(style.getBackColor().getRed(), style.getBackColor().getGreen(), style.getBackColor().getBlue(), 0);
        flashIntervalStyle = new Style(style.getColor(), flashIntervalBackColor, style.getStroke(), style.getFont());
    }

    public boolean isFlash() {
        return flash;
    }

    public void setFlash(boolean flash) {
        this.flash = flash;
        if (flash) {
        	this.flashTimer.setRepeats(true);
	        this.flashTimer.setInitialDelay(50);
	        this.flashTimer.start();
        } else {
        	this.flashTimer.stop();
        	style = normalizedStyle;
        }
    }

    public void setFlashIntervalStyle(Style flashIntervalStyle) {
    	this.flashIntervalStyle = flashIntervalStyle;
    }
    
    public Layer getLayer() {
        return layer;
    }

    public void setLayer(Layer layer) {
        this.layer = layer;
    }

    public Style getStyle() {
        return style;
    }

    public Style getStyleAssigned() {
        return style == null ? (layer == null ? null : layer.getStyle()) : style;
    }

    public void setStyle(Style style) {
        this.style = style;
    }

    public Color getColor() {
        final Style styleAssigned = getStyleAssigned();
        return styleAssigned == null ? null : getStyleAssigned().getColor();
    }

    public void setColor(Color color) {
        if ((style == null) && (color != null)) {
            style = new Style();
        }
        if (style != null) {
            style.setColor(color);
        }
    }

    public Color getBackColor() {
        final Style styleAssigned = getStyleAssigned();
        return styleAssigned == null ? null : getStyleAssigned().getBackColor();
    }

    public void setBackColor(Color backColor) {
        if ((style == null) && (backColor != null)) {
            style = new Style();
        }
        if (style != null) {
            style.setBackColor(backColor);
        }
    }

    public Stroke getStroke() {
        final Style styleAssigned = getStyleAssigned();
        return styleAssigned == null ? null : getStyleAssigned().getStroke();
    }

    public void setStroke(Stroke stroke) {
        if ((style == null) && (stroke != null)) {
            style = new Style();
        }
        if (style != null) {
            style.setStroke(stroke);
        }
    }

    public Font getFont() {
        final Style styleAssigned = getStyleAssigned();
        return styleAssigned == null ? null : getStyleAssigned().getFont();
    }

    public void setFont(Font font) {
        if ((style == null) && (font != null)) {
            style = new Style();
        }
        if (style != null) {
            style.setFont(font);
        }
    }

    private boolean isVisibleLayer() {
        return (layer == null) || (layer.isVisible() == null) || layer.isVisible();
    }

    public boolean isVisible() {
        return visible == null ? isVisibleLayer() : visible.booleanValue();
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public String getName() {
        return name;
    }

    public int getID() {
        return id;
    }

    public void setName(String txt) {
        this.name = txt;
    }

    public void setID(int id) {
        this.id = id;
    }

    public static Stroke getDefaultStroke() {
        return new BasicStroke();
    }

    public void setTimeToDelete(long timeToDelete) {
        this.timeToDelete = timeToDelete;
    }

    public long getTimeToDelete() {
        return timeToDelete;
    }

    public long getTimeToGoStale() {
        return timeToGoStale;
    }

    public void setTimeToGoStale(long timeToGoStale) {
        this.timeToGoStale = timeToGoStale;
    }

    public static Font getDefaultFont() {
        Font f = UIManager.getDefaults().getFont("TextField.font");
        if (f == null) {
            f = Font.decode(null);
        }
        return new Font(f.getName(), Font.BOLD, f.getSize());
    }

    public void paintText(Graphics g, Point position) {
        if ((name != null) && (g != null) && (position != null)) {
            if (getFont() == null) {
                final Font f = getDefaultFont();
                setFont(new Font(f.getName(), Font.BOLD, f.getSize()));
            }
            g.setColor(Color.DARK_GRAY);
            g.setFont(getFont());
            g.drawString(name, position.x + MapMarkerDot.DOT_RADIUS + 6, position.y + MapMarkerDot.DOT_RADIUS);
        }
    }

    private void flashTimerActionListenerEvent() {
		Thread thread = new Thread(new FlashTimerAction());
		thread.start();
	}
    
	private class FlashTimerAction implements Runnable {
		@Override
		public void run() {
			setStyle(getStyleAssigned().getBackColor().equals(normalizedStyle.getBackColor()) ? flashIntervalStyle : normalizedStyle);
		}
	}
	
}
