package geometry;

import java.awt.geom.Point2D;

public interface ICoordinate {

    double getLat();

    void setLat(double lat);

    double getLon();

    void setLon(double lon);
    
    Point2D getLonLat();
}
