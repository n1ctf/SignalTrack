package radiolocation;

import coverage.StaticMeasurement;
import jsc.util.Logarithm;

public final class RFPath {

	private RFPath() {
	    throw new IllegalStateException("Utility class");
	}
	
    public static double conicAngleToTarget(StaticMeasurement sma, StaticMeasurement smb) {    	
    	return 0;
    }

    public static double getFreeSpacePathLossdB(double meters, double frequencyMhz) {
    	final Logarithm log = new Logarithm(10);
    	return ((20 * log.log(meters)) + (20 * log.log(frequencyMhz))) - 27.55;
    }
    
	public static double getRelativeRFDistanceInMeters(double dB, double frequencyMhz) {
    	final Logarithm log = new Logarithm(10);
    	return log.antilog(((Math.abs(dB) + 27.55) - (20 * log.log(frequencyMhz))) / 20);
    }
	
	public static double pyth(double x, double y) {
		return Math.sqrt((x * x) + (y * y));
	}

}
