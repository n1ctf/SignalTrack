package map;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.List;

import geometry.ICoordinate;


/**
 * Interface to be implemented by rectangles that can be displayed on the map.
 *
 * @author Stefan Zeller
 * @see JMapViewer#addMapRectangle(MapRectangle)
 * @see JMapViewer#getMapRectangleList()
 */
public interface MapRectangle extends MapObject {

    /**
     * @return Latitude/Longitude of top left of rectangle
     */
    ICoordinate getUpperLeft();
    
    /**
     * @return Latitude/Longitude of lower right of rectangle
     */
    ICoordinate getLowerRight();

    /**
     * Paints the map rectangle on the map. The <code>topLeft</code> and
     * <code>bottomRight</code> are specifying the coordinates within <code>g</code>
     *
     * @param g graphics structure for painting
     * @param topLeft top left edge of painting region
     * @param bottomRight bottom right edge of painting region
     */
    void paint(Graphics g, Point topLeft, Point bottomRight);

	void setUpperLeft(ICoordinate upperLeft);

	void setLowerRight(ICoordinate lowerRight);
	
	void setRectangle(Rectangle2D rectangle);
	
	Rectangle2D getRectangle2D();

	List<ICoordinate> getPoints();
}
