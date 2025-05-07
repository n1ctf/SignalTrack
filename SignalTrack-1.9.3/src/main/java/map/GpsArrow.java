package map;

import java.awt.geom.Point2D;

public interface GpsArrow extends MapObject {
	Point2D getLonLat();
	void setVisible(boolean visible);
	int getAngle();
	double getRadius();
	void setAngle(int angle);
}
