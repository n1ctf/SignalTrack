package coverage;

import java.awt.geom.Point2D;

import radio.AbstractRadioReceiver.StandardModeName;

public class StaticMeasurement {

    private int id;
    private String testName;
    private Point2D point = new Point2D.Double();
    private long timeStamp;
    private double courseMadeGoodTrue;
    private double speedMadeGoodKPH;
    private double barometricAltitudeFeet;
    private double radarAltitudeFeet;
    private double gpsAltitudeFeet;
    private double frequencyMHz;
    private double dBm;
    private StandardModeName modeName;
    private int flight;

    public StaticMeasurement() {
    }

    public StaticMeasurement(StaticMeasurement staticMeasurement) {
        this.id = staticMeasurement.id;
        this.testName = staticMeasurement.testName;
        this.point = staticMeasurement.point;
        this.timeStamp = staticMeasurement.timeStamp;
        this.courseMadeGoodTrue = staticMeasurement.courseMadeGoodTrue;
        this.speedMadeGoodKPH = staticMeasurement.speedMadeGoodKPH;
        this.barometricAltitudeFeet = staticMeasurement.barometricAltitudeFeet;
        this.radarAltitudeFeet = staticMeasurement.radarAltitudeFeet;
        this.gpsAltitudeFeet = staticMeasurement.gpsAltitudeFeet;
        this.frequencyMHz = staticMeasurement.frequencyMHz;
        this.dBm = staticMeasurement.dBm;
        this.modeName = staticMeasurement.modeName;
        this.flight = staticMeasurement.flight;
    }

    public StaticMeasurement(String testName, Point2D point, long timeStamp, double courseMadeGoodTrue,
            double speedMadeGoodKPH, double barometricAltitudeFeet, double radarAltitudeFeet, double gpsAltitudefeet,
            double frequencyMHz, double dBm, StandardModeName mode, int flight) {
        this.testName = testName;
        this.point = point;
        this.timeStamp = timeStamp;
        this.courseMadeGoodTrue = courseMadeGoodTrue;
        this.speedMadeGoodKPH = speedMadeGoodKPH;
        this.barometricAltitudeFeet = barometricAltitudeFeet;
        this.radarAltitudeFeet = radarAltitudeFeet;
        this.gpsAltitudeFeet = gpsAltitudefeet;
        this.frequencyMHz = frequencyMHz;
        this.dBm = dBm;
        this.modeName = mode;
        this.flight = flight;
    }

    public static Object[] staticMeasurementToObjectArray(StaticMeasurement sm) {
        final Object[] obj = new Object[14];
        obj[0] = sm.id;
        obj[1] = sm.testName;
        obj[2] = sm.timeStamp;
        obj[3] = sm.point.getX();
        obj[4] = sm.point.getY();
        obj[5] = sm.courseMadeGoodTrue;
        obj[6] = sm.speedMadeGoodKPH;
        obj[7] = sm.barometricAltitudeFeet;
        obj[8] = sm.radarAltitudeFeet;
        obj[9] = sm.gpsAltitudeFeet;
        obj[10] = sm.frequencyMHz;
        obj[11] = sm.dBm;
        obj[12] = sm.modeName;
        obj[13] = sm.flight;
        return obj;
    }

    public static StaticMeasurement objectArrayToStaticMeasurement(Object[] obj) {
        final StaticMeasurement sm = new StaticMeasurement();
        sm.id = (int) obj[0];
        sm.testName = (String) obj[1];
        sm.timeStamp = (long) obj[2];
        sm.point.setLocation((double) obj[3], (double) obj[4]);
        sm.courseMadeGoodTrue = (double) obj[5];
        sm.speedMadeGoodKPH = (double) obj[6];
        sm.barometricAltitudeFeet = (double) obj[7];
        sm.radarAltitudeFeet = (double) obj[8];
        sm.gpsAltitudeFeet = (double) obj[9];
        sm.frequencyMHz = (double) obj[10];
        sm.dBm = (double) obj[11];
        sm.modeName = (StandardModeName) obj[12];
        sm.flight = (int) obj[13];
        return sm;
    }

    public int getID() {
        return id;
    }

    public void setID(int id) {
        this.id = id;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getTestName() {
        return testName;
    }

    public Point2D getPoint() {
        return point;
    }

    public void setPoint(final Point2D point) {
        this.point = point;
    }

    public double getdBm() {
        return dBm;
    }

    public void setdBm(final double dBm) {
        this.dBm = dBm;
    }

    public double getCourseMadeGoodTrue() {
        return courseMadeGoodTrue;
    }

    public void setCourseMadeGoodTrue(final double courseMadeGoodTrue) {
        this.courseMadeGoodTrue = courseMadeGoodTrue;
    }

    public double getSpeedMadeGoodKPH() {
        return speedMadeGoodKPH;
    }

    public void setSpeedMadeGoodKPH(final double speedMadeGoodKPH) {
        this.speedMadeGoodKPH = speedMadeGoodKPH;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(final long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public double getBarometricAltitudeFeet() {
        return barometricAltitudeFeet;
    }

    public void setBarometricAltitudeFeet(final double barometricAltitudeFeet) {
        this.barometricAltitudeFeet = barometricAltitudeFeet;
    }

    public double getRadarAltitudeFeet() {
        return radarAltitudeFeet;
    }

    public void setRadarAltitudeFeet(final double radarAltitudeFeet) {
        this.radarAltitudeFeet = radarAltitudeFeet;
    }

    public double getGpsAltitudeFeet() {
        return gpsAltitudeFeet;
    }

    public void setGpsAltitudeFeet(final double gpsAltitudeFeet) {
        this.gpsAltitudeFeet = gpsAltitudeFeet;
    }

    public double getFrequencyMHz() {
        return frequencyMHz;
    }

    public void setFrequencyMHz(final double frequencyMHz) {
        this.frequencyMHz = frequencyMHz;
    }

    public StandardModeName getModeName() {
        return modeName;
    }

    public void setModeName(StandardModeName modeName) {
        this.modeName = modeName;
    }

    public int getFlight() {
        return flight;
    }

    public void setFlight(final int flight) {
        this.flight = flight;
    }

    public static StaticMeasurement copy(StaticMeasurement staticMeasurement) {
        return new StaticMeasurement(staticMeasurement);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(barometricAltitudeFeet);
        result = (prime * result) + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(courseMadeGoodTrue);
        result = (prime * result) + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(dBm);
        result = (prime * result) + (int) (temp ^ (temp >>> 32));
        result = (prime * result) + ((modeName == null) ? 0 : modeName.hashCode());
        result = (prime * result) + flight;
        temp = Double.doubleToLongBits(frequencyMHz);
        result = (prime * result) + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(gpsAltitudeFeet);
        result = (prime * result) + (int) (temp ^ (temp >>> 32));
        result = (prime * result) + id;
        result = (prime * result) + ((point == null) ? 0 : point.hashCode());
        temp = Double.doubleToLongBits(radarAltitudeFeet);
        result = (prime * result) + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(speedMadeGoodKPH);
        result = (prime * result) + (int) (temp ^ (temp >>> 32));
        result = (prime * result) + ((testName == null) ? 0 : testName.hashCode());
        result = (prime * result) + (int) (timeStamp ^ (timeStamp >>> 32));
        return result;
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
        final StaticMeasurement other = (StaticMeasurement) obj;
        if (Double.doubleToLongBits(barometricAltitudeFeet) != Double.doubleToLongBits(other.barometricAltitudeFeet)) {
            return false;
        }
        if (Double.doubleToLongBits(courseMadeGoodTrue) != Double.doubleToLongBits(other.courseMadeGoodTrue)) {
            return false;
        }
        if (Double.doubleToLongBits(dBm) != Double.doubleToLongBits(other.dBm)) {
            return false;
        }
        if (modeName == null) {
            if (other.modeName != null) {
                return false;
            }
        } else if (!modeName.equals(other.modeName)) {
            return false;
        }
        if (flight != other.flight) {
            return false;
        }
        if (Double.doubleToLongBits(frequencyMHz) != Double.doubleToLongBits(other.frequencyMHz)) {
            return false;
        }
        if (Double.doubleToLongBits(gpsAltitudeFeet) != Double.doubleToLongBits(other.gpsAltitudeFeet)) {
            return false;
        }
        if (id != other.id) {
            return false;
        }
        if (point == null) {
            if (other.point != null) {
                return false;
            }
        } else if (!point.equals(other.point)) {
            return false;
        }
        if (Double.doubleToLongBits(radarAltitudeFeet) != Double.doubleToLongBits(other.radarAltitudeFeet)) {
            return false;
        }
        if (Double.doubleToLongBits(speedMadeGoodKPH) != Double.doubleToLongBits(other.speedMadeGoodKPH)) {
            return false;
        }
        if (testName == null) {
            if (other.testName != null) {
                return false;
            }
        } else if (!testName.equals(other.testName)) {
            return false;
        }

        return timeStamp != other.timeStamp;
    }

    @Override
    public String toString() {
        return "StaticMeasurement [id=" + id + ", testName=" + testName + ", point=" + point + ", dBm=" + dBm
                + ", courseMadeGoodTrue=" + courseMadeGoodTrue + ", speedMadeGoodKPH=" + speedMadeGoodKPH
                + ", timeStamp=" + timeStamp + ", barometricAltitudeFeet=" + barometricAltitudeFeet
                + ", radarAltitudeFeet=" + radarAltitudeFeet + ", gpsAltitudeFeet=" + gpsAltitudeFeet
                + ", frequencyMHz=" + frequencyMHz + ", emissionDesignator=" + modeName + ", flight=" + flight
                + "]";
    }

}
