package components;

public class InversePoint {
	private double distanceMeters;
	private double initialBearing;
	private double finalBearing;

	public InversePoint(double distanceMeters, double initialBearing, double finalBearing) {
		if ((initialBearing >= 360.0) || (initialBearing < 0.0)) {
			throw new IllegalArgumentException("Initial Bearing = " + initialBearing + " degrees");
		}
		if ((finalBearing >= 360.0) || (finalBearing < 0.0)) {
			throw new IllegalArgumentException("Final Bearing = " + finalBearing + " degrees");
		}
		this.distanceMeters = distanceMeters;
		this.initialBearing = initialBearing;
		this.finalBearing = finalBearing;
	}

	public double getDistanceMeters() {
		return distanceMeters;
	}

	public void setDistanceMeters(double distanceMeters) {
		this.distanceMeters = distanceMeters;
	}

	public double getInitialBearing() {
		return initialBearing;
	}

	public void setInitialBearing(double initialBearing) {
		this.initialBearing = initialBearing;
	}

	public double getFinalBearing() {
		return finalBearing;
	}

	public void setFinalBearing(double finalBearing) {
		this.finalBearing = finalBearing;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(distanceMeters);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(finalBearing);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(initialBearing);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		final InversePoint other = (InversePoint) obj;
		if (Double.doubleToLongBits(distanceMeters) != Double.doubleToLongBits(other.distanceMeters)) {
			return false;
		}
		if (Double.doubleToLongBits(finalBearing) != Double.doubleToLongBits(other.finalBearing)) {
			return false;
		}
		return Double.doubleToLongBits(initialBearing) == Double.doubleToLongBits(other.initialBearing);
	}

	@Override
	public String toString() {
		return "InversePoint [distanceMeters=" + distanceMeters + ", initialBearing=" + initialBearing
				+ ", finalBearing=" + finalBearing + "]";
	}
	
}
