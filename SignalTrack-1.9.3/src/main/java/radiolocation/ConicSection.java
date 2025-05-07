package radiolocation;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import coverage.StaticMeasurement;
import geometry.Coordinate;
import geometry.ICoordinate;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import utility.Vincenty;

public class ConicSection {
	private static final int ARRAY_SIZE = 512;
    
	private Point2D vertex;
    private Point2D focus;
    private Point2D center;
    private double dbm;
    private double catt;
    private double a;
    private double b;
    private double direction;
    private double approachAngle;
    private double eccentricity;
    private double directrix;
    private StaticMeasurement sma;
    private StaticMeasurement smb;
    private List<Point2D> arcPointList;
    private List<Point2D> hyperbolicPointList;
    private Point2D[] hyperbolicPointArray;
    private List<LatLon> hyperbolicLatLonList;

    public ConicSection(StaticMeasurement sma, StaticMeasurement smb) {
        this.sma = sma;
        this.smb = smb;
        calculateHyperbolicArc(sma, smb);
        createPoints();
    }

    private void calculateHyperbolicArc(StaticMeasurement sma, StaticMeasurement smb) {
        direction = courseMadeGood(sma, smb);
        approachAngle = getApproachAngle(sma, smb);
        center = center(sma.getPoint());
        dbm = distanceBetweenMeasurements(sma, smb);
        catt = RFPath.conicAngleToTarget(sma, smb);
        a = a(center, catt, sma.getGpsAltitudeFeet());
        b = b(a, catt);
        vertex = vertex(center, a, direction);
        focus = focus(center, direction, a, b);
        eccentricity = getEccentricity(a, b);
        directrix = a / eccentricity;
    }

    private void createPoints() {
        Point2D[] hyperbolicArc = createHyperbolicArc(b, a);
        hyperbolicPointArray = createSymmetricArcPointArray(hyperbolicArc, center, direction);
        arcPointList = arrayToList(hyperbolicArc);
        hyperbolicPointList = arrayToList(hyperbolicPointArray);
        hyperbolicLatLonList = arrayToLatLonList(hyperbolicPointArray);
    }

    private Point2D center(Point2D pa) {
        return pa;
    }

    private double distanceBetweenMeasurements(StaticMeasurement sma, StaticMeasurement smb) {
        return Vincenty.distanceToDirect(sma.getPoint(), sma.getGpsAltitudeFeet(), smb.getPoint(), smb.getGpsAltitudeFeet());
    }

    private double getApproachAngle(StaticMeasurement sma, StaticMeasurement smb) {
        final double altitudeDelta = sma.getGpsAltitudeFeet() - smb.getGpsAltitudeFeet();
        final double horizontalRange = Vincenty.distanceToDirect(smb.getPoint(), 0, sma.getPoint(), 0);
        return Math.toDegrees(Math.atan(altitudeDelta / horizontalRange));
    }

    private double getEccentricity(double a, double b) {
        return Math.sqrt((a * a) + (b * b)) / a;
    }

    private double courseMadeGood(StaticMeasurement sma, StaticMeasurement smb) {
        return Vincenty.finalBearingTo(smb.getPoint(), sma.getPoint());
    }

    private Point2D focus(Point2D center, double cmg, double a, double b) {
        final double c = Math.sqrt((a * a) + (b * b));
        final double dm = Vincenty.degreesToMeters(c, cmg, center.getY());
        return Vincenty.getVincentyDirect(center, cmg, dm).getDestinationPoint();
    }

    private Point2D vertex(Point2D center, double a, double cmg) {
        final double dm = Vincenty.degreesToMeters(a, cmg, center.getY());
        return Vincenty.getVincentyDirect(center, cmg, dm).getDestinationPoint();
    }

    private double a(Point2D center, double catt, double altitude) {
        final double da = altitude / Math.tan(Math.toRadians(catt));
        return Vincenty.metersToDegrees(da, 90, center.getY());
    }

    private double b(double a, double catt) {
        return a * Math.tan(Math.toRadians(catt));
    }

    private List<Point2D> arrayToList(Point2D[] array) {
        return Arrays.asList(array);
    }

    private List<LatLon> arrayToLatLonList(Point2D[] array) {
        final List<LatLon> list = new CopyOnWriteArrayList<>();
        for (final Point2D point : array) {
            list.add(LatLon.fromDegrees(point.getY(), point.getX()));

        }
        return list;
    }

    public List<ICoordinate> getCoordinateList() {
        final List<ICoordinate> coords = Collections.synchronizedList(new CopyOnWriteArrayList<>());
        hyperbolicPointList.forEach(point -> coords.add(new Coordinate(point.getX(), point.getY())));
        return coords;
    }

    private Point2D[] createHyperbolicArc(final double b, final double a) {
        final Point2D[] arc = new Point2D[ARRAY_SIZE];
        final double incr = 1.0 / arc.length;
        double x = a;
        for (int i = 0; i < arc.length; i++) {
            final double y = b * Math.sqrt((((x * x) / (a * a)) - 1.0));
            arc[i] = new Point.Double(x, y);
            x += incr;
        }
        return arc;
    }

    private Point2D[] createSymmetricArcPointArray(Point2D[] arcArray, Point2D c, double cmg) {
        final Point2D[] pa = new Point2D.Double[arcArray.length * 2];
        for (int i = arcArray.length - 1; i >= 0; i--) {
            final Point2D n = arcArray[i];
            final Point2D pk = new Point2D.Double(c.getX() + n.getX(), c.getY() + n.getY());
            pa[arcArray.length - 1 - i] = translate(c, pk, cmg - 90.0);
        }
        for (int i = 0; i < arcArray.length; i++) {
            final Point2D n = arcArray[i];
            final Point2D pq = new Point2D.Double(c.getX() + n.getX(), c.getY() - n.getY());
            pa[arcArray.length + i] = translate(c, pq, cmg - 90.0);
        }
        return pa;
    }

    private Point2D translate(final Point2D c, final Point2D pt, final double rotate) {
        final double t = LatLon.rhumbAzimuth(LatLon.fromDegrees(c.getY(), c.getX()), LatLon.fromDegrees(pt.getY(), pt.getX())).getDegrees();
        final Angle h = Angle.fromDegrees(c.distance(pt));
        final LatLon d = LatLon.greatCircleEndPosition(LatLon.fromDegrees(c.getY(), c.getX()), Angle.fromDegrees(rotate + t), h);
        return new Point2D.Double(d.getLongitude().getDegrees(), d.getLatitude().getDegrees());
    }

    public List<LatLon> getAsymptoteLatLonList() {
        final double h = center.distance(hyperbolicPointList.get(0));
        final List<LatLon> list = Collections.synchronizedList(new CopyOnWriteArrayList<>());
        final double y1 = ((b / a) * h) + center.getY();
        final double x = center.getX() + h;
        final double y2 = ((-b / a) * h) + center.getY();
        final Point2D p1 = translate(center, new Point.Double(x, y1), direction - 90.0);
        final Point2D p2 = translate(center, new Point.Double(x, y2), direction - 90.0);
        list.add(LatLon.fromDegrees(p1.getY(), p1.getX()));
        list.add(LatLon.fromDegrees(center.getY(), center.getX()));
        list.add(LatLon.fromDegrees(p2.getY(), p2.getX()));
        return list;
    }

    public List<ICoordinate> getAsymptoteCoordinateList() {
        final List<ICoordinate> coords = Collections.synchronizedList(new CopyOnWriteArrayList<>());
        final List<LatLon> latLon = getAsymptoteLatLonList();
        for (int i = 0; i < latLon.size(); i++) {
            coords.add(new Coordinate(latLon.get(i).longitude.degrees, latLon.get(i).latitude.degrees));
        }
        return coords;
    }

    public List<Point2D> getAsymptotePointList() {
        final List<Point2D> points = Collections.synchronizedList(new CopyOnWriteArrayList<>());
        final List<LatLon> latLon = getAsymptoteLatLonList();
        for (int i = 0; i < latLon.size(); i++) {
            points.add(new Point.Double(latLon.get(i).longitude.degrees, latLon.get(i).latitude.degrees));
        }
        return points;
    }

    public Point2D[] getAsymptotePointArray() {
        final Point2D[] point = new Point2D.Double[3];
        final List<LatLon> latLon = getAsymptoteLatLonList();
        for (int i = 0; i < latLon.size(); i++) {
            point[i] = new Point.Double(latLon.get(i).longitude.degrees, latLon.get(i).latitude.degrees);
        }
        return point;
    }

    public List<Point2D> getArcPointList() {
        return Collections.unmodifiableList(arcPointList);
    }

    public List<LatLon> getHyperbolicLatLonList() {
        return Collections.unmodifiableList(hyperbolicLatLonList);
    }

    public List<Point2D> getHyperbolicPointList() {
        return Collections.unmodifiableList(hyperbolicPointList);
    }

    public StaticMeasurement getSMA() {
        return sma;
    }

    public Point2D[] getHyperbolicPointArray() {
        return hyperbolicPointArray.clone();
    }

    public StaticMeasurement getSMB() {
        return smb;
    }

    public double getA() {
        return a;
    }

    public double getB() {
        return b;
    }

    public double getDirection() {
        return direction;
    }

    public double getDirectrix() {
        return directrix;
    }

    public Point2D getFocus() {
        return focus;
    }

    public double getEccentricity() {
        return eccentricity;
    }

    public Point2D getCenter() {
        return center;
    }

    public Point2D getVertex() {
        return vertex;
    }

    public double getConicAngleToTarget() {
        return catt;
    }

    public double getDistanceBetweenMeasurements() {
        return dbm;
    }

    public double getApproachAngle() {
        return approachAngle;
    }

    public void setA(double a) {
        this.a = a;
        vertex = vertex(center, a, direction);
        b = Math.tan(Math.toRadians(catt)) * a;
        focus = focus(center, direction, a, b);
        eccentricity = getEccentricity(a, b);
        directrix = a / eccentricity;
        createPoints();
    }

    public void setB(double b) {
        this.b = b;
        focus = focus(center, direction, a, b);
        eccentricity = getEccentricity(a, b);
        directrix = a / eccentricity;
        createPoints();
    }

    public void setCourseMadeGood(double courseMadeGood) {
        this.direction = courseMadeGood;
        vertex = vertex(center, a, courseMadeGood);
        focus = focus(center, courseMadeGood, a, b);
        createPoints();
    }

    public void setAngle(double catt) {
        this.catt = catt;
        b = Math.tan(Math.toRadians(catt)) * a;
        focus = focus(center, direction, a, b);
        eccentricity = getEccentricity(a, b);
        directrix = a / eccentricity;
        createPoints();
    }

    public void setEccentricity(double eccentricity) {
        this.eccentricity = eccentricity;
        b = Math.sqrt(((eccentricity * a) * (eccentricity * a)) - (a * a));
        catt = Math.toDegrees(Math.atan(b / a));
        directrix = a / eccentricity;
        vertex = vertex(center, a, direction);
        focus = focus(center, direction, a, b);
        createPoints();
    }

    public void setFocus(Point2D focus) {
        this.focus = focus;
        final double c = Math.sqrt(((focus.getX() - center.getX()) * (focus.getX() - center.getX())) + ((focus.getY() - center.getY()) * (focus.getY() - center.getY())));
        a = Math.sqrt((c * c) - (b * b));
        b = Math.tan(Math.toRadians(catt)) * a;
        vertex = vertex(center, a, direction);
        eccentricity = getEccentricity(a, b);
        directrix = a / eccentricity;
        createPoints();
    }

    public void setCenter(Point2D center) {
        this.center = center;
        vertex = vertex(center, a, direction);
        focus = focus(center, direction, a, b);
        createPoints();
    }

    public void setVertex(Point2D vertex) {
        this.vertex = vertex;
        final double c = Math.sqrt(((vertex.getX() - center.getX()) * (vertex.getX() - center.getX())) + ((vertex.getY() - center.getY()) * (vertex.getY() - center.getY())));
        a = Math.sqrt((c * c) - (b * b));
        b = Math.tan(Math.toRadians(catt)) * a;
        focus = focus(center, direction, a, b);
        eccentricity = getEccentricity(a, b);
        directrix = a / eccentricity;
        createPoints();
    }

    public void setLeadingStaticMeasurement(StaticMeasurement sma) {
        this.sma = sma;
        calculateHyperbolicArc(sma, smb);
        createPoints();
    }

    public void setTrailingStaticMeasurement(StaticMeasurement smb) {
        this.smb = smb;
        calculateHyperbolicArc(sma, smb);
        createPoints();
    }

}
