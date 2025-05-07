package aprs;

import java.awt.geom.Point2D;

import java.awt.image.BufferedImage;

import java.util.Calendar;

public interface AprsIcon {
	Point2D getLonLat();
	Calendar getTimeOfReport();
	int getCourseMadeGood();
	double getSpeedMadeGood();
	double getAltitude();
	String getCallSign();
	String getSsid();
	BufferedImage getImage();
	
	@Override
	boolean equals(Object obj);

}

