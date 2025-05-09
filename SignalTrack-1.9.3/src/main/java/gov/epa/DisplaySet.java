package gov.epa;

import java.awt.Color;
import java.util.Objects;

public class DisplaySet {
	
	public static final Color DEFAULT_BACKGROUND_COLOR = Color.LIGHT_GRAY; 
	public static final Color DEFAULT_FOREGROUND_COLOR = Color.BLACK;
	public static final String NO_DATA = "NO DATA";
	
	private Color background;
	private Color foreground;
	private String toolTipText;
	
	public DisplaySet() {
		this(DEFAULT_BACKGROUND_COLOR, DEFAULT_FOREGROUND_COLOR, NO_DATA);
	}
	
	public DisplaySet(Color background, Color foreground, String toolTipText) {
		this.background = background;
		this.foreground = foreground;
		this.toolTipText = toolTipText;
	}
	
	public Color getBackground() {
		return background;
	}
	
	public Color getForeground() {
		return foreground;
	}
	
	public String getToolTipText() {
		return toolTipText;
	}

	@Override
	public int hashCode() {
		return Objects.hash(background, foreground, toolTipText);
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
		final DisplaySet other = (DisplaySet) obj;
		return Objects.equals(background, other.background) && Objects.equals(foreground, other.foreground)
				&& Objects.equals(toolTipText, other.toolTipText);
	}

	@Override
	public String toString() {
		return "DisplaySet [background=" + background + ", foreground=" + foreground + ", toolTipText=" + toolTipText
				+ "]";
	}
}
