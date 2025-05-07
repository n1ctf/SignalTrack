package org.jxmapviewer;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;
import java.util.Objects;

public class Style {

    private Color color;
    private Color backColor;
    private Stroke stroke;
    private Font font;

    private static final AlphaComposite TRANSPARENCY = AlphaComposite.getInstance(AlphaComposite.SRC_OVER);
    private static final AlphaComposite OPAQUE = AlphaComposite.getInstance(AlphaComposite.SRC);

    public Style() {}

    public Style(Color color, Color backColor, Stroke stroke, Font font) {
        this.color = color;
        this.backColor = backColor;
        this.stroke = stroke;
        this.font = font;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Color getBackColor() {
        return backColor;
    }

    public void setBackColor(Color backColor) {
        this.backColor = backColor;
    }

    public Stroke getStroke() {
        return stroke;
    }

    public void setStroke(Stroke stroke) {
        this.stroke = stroke;
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    private static AlphaComposite getAlphaComposite(Color color) {
        return color.getAlpha() == 255 ? OPAQUE : TRANSPARENCY;
    }

    public AlphaComposite getAlphaComposite() {
        return getAlphaComposite(color);
    }

    public AlphaComposite getBackAlphaComposite() {
        return getAlphaComposite(backColor);
    }
    
    public int getAlpha() {
    	return color.getAlpha();
    }
    
    public int getBackAlpha() {
    	return backColor.getAlpha();
    }
    
    public void setBackAlpha(int alpha) {
    	backColor = new Color(backColor.getRed(), backColor.getGreen(), backColor.getBlue(), alpha);
    }
    
    public void setAlpha(int alpha) {
    	color = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 19 * hash + Objects.hashCode(this.color);
        hash = 19 * hash + Objects.hashCode(this.backColor);
        hash = 19 * hash + Objects.hashCode(this.stroke);
        hash = 19 * hash + Objects.hashCode(this.font);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Style other = (Style) obj;
        if (!Objects.equals(this.color, other.color)) {
            return false;
        }
        if (!Objects.equals(this.backColor, other.backColor)) {
            return false;
        }
        if (!Objects.equals(this.stroke, other.stroke)) {
            return false;
        }
        return (Objects.equals(this.font, other.font));
    }

    @Override
    public String toString() {
    	final StringBuilder sb = new StringBuilder();
        sb.append("Style{color=").append(color);
        sb.append(", backColor=").append(backColor);
        sb.append(", stroke=").append(stroke);
        sb.append(", font=").append(font);
        sb.append('}');
        return sb.toString();
    }

}
