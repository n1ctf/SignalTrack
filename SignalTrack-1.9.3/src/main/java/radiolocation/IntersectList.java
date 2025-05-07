package radiolocation;

import java.awt.Point;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import utility.Utility;
import utility.Vincenty;

public class IntersectList extends SwingWorker<List<Point2D>, Void> {

	private List<ConicSection> coneList;
	private int add;
	
	public IntersectList() {}
	
	public IntersectList(List<ConicSection> coneList, int previousListSize) {
    	this.coneList = coneList;
    	add = coneList.size() - previousListSize;
	}

    @Override
    protected List<Point2D> doInBackground() {
    	final List<Point2D> intersectList = new ArrayList<>();
    	Point2D p1;
    	Point2D p2;
    	Point2D p3;
    	Point2D p4;
    	Line2D l1;
    	Line2D l2;
    	if (((coneList != null) && (coneList.size() >= 2)) && ((coneList.size() > 1) && (add > 0))) {
	    	for (int i = coneList.size() - 1; i >= (coneList.size() - add); i--) {
	    		final List<Point2D> sa1 = coneList.get(i).getHyperbolicPointList();
	    		if (sa1 != null) {
					for (int n = i - 1; n >= 0; n--) {
						final ArrayList<Point2D> intersectPoints = new ArrayList<>(3);
						final List<Point2D> sa2 = coneList.get(n).getHyperbolicPointList();
						if (sa2 != null) {
							for (int q = 0; q < sa1.size(); q += 10) {
							    p1 = sa1.get(q);
							    if (q < (sa1.size() - 10)) {
							    	p2 = sa1.get(q + 10);
							    	l1 = new Line2D.Double(p1, p2);
							    	for (int r = 0; r < sa2.size(); r += 10) {
									    p3 = sa2.get(r);
									    if (r < (sa2.size() - 10)) {
									    	p4 = sa2.get(r + 10);
									    	l2 = new Line2D.Double(p3, p4);
									    	if (l1.intersectsLine(l2)) {
									    		final Point2D intersection = intersect(l1, l2);
												intersectPoints.add(intersection);
											}
									    } else {
											break;
										}	
							    	}
							    } else {
									break;
								}	
							}
						}
						if (isConvergent(coneList.get(i), coneList.get(n))) {
							final Point2D nearestPoint = selectNearestPoint(coneList.get(i), coneList.get(n), intersectPoints);
							intersectList.add(nearestPoint);
						} else {
							if (!intersectPoints.isEmpty()) {
								intersectList.addAll(intersectPoints);
							}
						}
					}
				}
	    	}
    	}
    	return intersectList;
	}
    
    @Override
	protected void done() {
		try {
			final List<Point2D> il = get();
			if ((il != null) && !il.isEmpty()) {
				firePropertyChange("INTERSECT_LIST_COMPLETE", null, il);
			}
		} catch (InterruptedException ex) {
			ex.printStackTrace();
			Thread.currentThread().interrupt();
		} catch (ExecutionException | NullPointerException ex) {
			ex.printStackTrace();
		}
	}

    private boolean isConvergent(ConicSection c1, ConicSection  c2) {
    	final double db = c1.getSMB().getPoint().distance(c2.getSMB().getPoint());
    	final double da = c1.getSMA().getPoint().distance(c2.getSMA().getPoint());
    	return da < db;
    }

    private Point2D selectNearestPoint(ConicSection c1, ConicSection  c2, ArrayList<Point2D> points) {
    	final double angleBetweenPoints = Vincenty.finalBearingTo(c1.getSMA().getPoint(), c2.getSMA().getPoint());
    	final double distanceBetweenPoints = Vincenty.distanceToOnSurface(c1.getSMA().getPoint(), c2.getSMA().getPoint());
    	double distanceToNearestPoint = Double.MAX_VALUE;
    	final Point2D centerPoint = Vincenty.getVincentyDirect(c1.getSMA().getPoint(), angleBetweenPoints, distanceBetweenPoints / 2D).getDestinationPoint();
    	Point2D nearestPoint = null;
    	for (int i = 0; i < points.size(); i++) {
    		final double distanceToThisPoint = centerPoint.distance(points.get(i));
    		if (distanceToThisPoint <= distanceToNearestPoint) {
    			distanceToNearestPoint = distanceToThisPoint;
    			nearestPoint = points.get(i);
    		}
    	}
    	return nearestPoint;
    }
    
	private Point2D intersect(Line2D a, Line2D b) {
        final double d = ((a.getX1() - a.getX2()) * (b.getY1() - b.getY2())) - ((a.getY1() - a.getY2()) * (b.getX1() - b.getX2()));
        if (Utility.isZero(d)) {
            return null;
        }

        final double xi = (((b.getX1() - b.getX2()) * ((a.getX1() * a.getY2()) - (a.getY1() * a.getX2()))) - ((a.getX1() - a.getX2()) * ((b.getX1() * b.getY2()) - (b.getY1() * b.getX2())))) / d;
        final double yi = (((b.getY1() - b.getY2()) * ((a.getX1() * a.getY2()) - (a.getY1() * a.getX2()))) - ((a.getY1() - a.getY2()) * ((b.getX1() * b.getY2()) - (b.getY1() * b.getX2())))) / d;

        return new Point.Double(xi, yi);
    }

}
