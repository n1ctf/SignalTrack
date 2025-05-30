package org.openstreetmap;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.List;

import javax.swing.UIManager;

import org.jxmapviewer.Style;

public class AbstractLayer {
    private LayerGroup parent;
    private String name;
    private String description;
    private Style style;
    private Boolean visible;
    private Boolean visibleTexts = Boolean.TRUE;

    public AbstractLayer(String name) {
        this(name, (String) null);
    }

    public AbstractLayer(String name, String description) {
        this(name, description, getDefaultStyle());
    }

    public AbstractLayer(String name, Style style) {
        this(name, null, style);
    }

    public AbstractLayer(String name, String description, Style style) {
        this(null, name, description, style);
    }

    public AbstractLayer(LayerGroup parent, String name) {
        this(parent, name, getDefaultStyle());
    }

    public AbstractLayer(LayerGroup parent, String name, Style style) {
        this(parent, name, null, style);
    }

    public AbstractLayer(LayerGroup parent, String name, String description, Style style) {
        setParent(parent);
        setName(name);
        setDescription(description);
        setStyle(style);
        setVisible(Boolean.TRUE);

        if (parent != null) {
			parent.add(this);
		}
    }

    public LayerGroup getParent() {
        return parent;
    }

    public void setParent(LayerGroup parent) {
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Style getStyle() {
        return style;
    }

    public void setStyle(Style style) {
        this.style = style;
    }

    public Boolean isVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public static <E> List<E> add(List<E> list, E element) {
        if (element != null) {
            if (list == null) {
				list = new ArrayList<>();
			}
            if (!list.contains(element)) {
				list.add(element);
			}
        }
        return list;
    }

    public Boolean isVisibleTexts() {
        return visibleTexts;
    }

    public void setVisibleTexts(Boolean visibleTexts) {
        this.visibleTexts = visibleTexts;
    }

    @Override
    public String toString() {
        return name;
    }
    
    public static Stroke getDefaultStroke() {
    	return new BasicStroke();	
    }
    
    public static Style getDefaultStyle() {
        return new Style(new Color(255,0,0,127), new Color(255,0,0,64), getDefaultStroke(), getDefaultFont());
    }
    
    public static Font getDefaultFont() {
        Font f = UIManager.getDefaults().getFont("TextField.font");
        if (f == null) {
            f = Font.decode(null);
        }
        return new Font(f.getName(), Font.BOLD, f.getSize());
    }

}
