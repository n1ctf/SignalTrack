package org.jxmapviewer.fma.jtiledownloader;

public class Constants {
	
	private Constants() {
	    throw new IllegalStateException("Utility class");
	  }
	
	public static final String VERSION = "0.6";

	public static final double EARTH_CIRC_POLE = 40.007863 * Math.pow(10, 6);
	public static final double EARTH_CIRC_EQUATOR = 40.075016 * Math.pow(10, 6);
	public static final double MIN_LON = -180;
	public static final double MAX_LON = 180;
	public static final double MIN_LAT = -85.0511;
	public static final double MAX_LAT = 85.0511;

}
