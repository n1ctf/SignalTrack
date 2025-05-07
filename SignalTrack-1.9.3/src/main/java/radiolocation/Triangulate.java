package radiolocation;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import components.Bearing;
import utility.Utility;

public class Triangulate extends SwingWorker<Point2D.Double, Void> {

    private static final double RDF_BEARING_LENGTH_IN_DEGREES = 0.500;

    private final List<Bearing> bearingList;
    private List<Point2D> intersectList;
    private Point2D intersectPoint;

    public Triangulate(List<Bearing> bearingList) {
        this.bearingList = bearingList;
    }

    @Override
    protected Point2D.Double doInBackground() throws Exception {
        intersectList = intersections(bearingList);
        double ilx = 0;
        double ily = 0;
        for (int i = 0; i < intersectList.size(); i++) {
            ilx = ilx + intersectList.get(i).getX();
            ily = ily + intersectList.get(i).getY();
        }
        final double ilxd = ilx / intersectList.size();
        final double ilyd = ily / intersectList.size();

        return new Point.Double(ilxd, ilyd);
    }

    @Override
    protected void done() {
        try {
            intersectPoint = get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
    }

    public Point2D getIntersectPoint() {
        return intersectPoint;
    }

    public List<Point2D> getIntersectList() {
        return intersectList;
    }

    private List<Point2D> intersections(List<Bearing> bearingList) {
        final List<Point2D> il = new ArrayList<>();
        if (bearingList.size() >= 2) {
            for (int n = 0; n < bearingList.size(); n++) {
                for (int i = n + 1; i < bearingList.size(); i++) {
                    il.add(intersect(bearingList.get(i).getBearingPosition(),
                            bearingList.get(i).getBearing(), bearingList.get(n).getBearingPosition(),
                            bearingList.get(n).getBearing(), RDF_BEARING_LENGTH_IN_DEGREES));
                }
            }
        }
        return il;
    }

    private Point2D intersect(Point2D a, double t, Point2D b, double z, double length) {
        final double lonA = a.getX();
        final double latA = a.getY();
        final double lonB = b.getX();
        final double latB = b.getY();

        final double tA = t;
        final double tB = z;

        final double lonA2 = (Math.sin((tA * Math.PI) / 180.0) * length) + a.getX();
        final double latA2 = (Math.cos((tA * Math.PI) / 180.0) * length) + a.getY();
        final double lonB2 = (Math.sin((tB * Math.PI) / 180.0) * length) + b.getX();
        final double latB2 = (Math.cos((tB * Math.PI) / 180.0) * length) + b.getY();

        final double d = ((lonA - lonA2) * (latB - latB2)) - ((latA - latA2) * (lonB - lonB2));
        if (Utility.isZero(d)) {
            return null;
        }

        final double xi = (((lonB - lonB2) * ((lonA * latA2) - (latA * lonA2))) - ((lonA - lonA2)
                * ((lonB * latB2) - (latB * lonB2)))) / d;
        final double yi = (((latB - latB2) * ((lonA * latA2) - (latA * lonA2))) - ((latA - latA2)
                * ((lonB * latB2) - (latB * lonB2)))) / d;

        return new Point.Double(xi, yi);
    }

}
