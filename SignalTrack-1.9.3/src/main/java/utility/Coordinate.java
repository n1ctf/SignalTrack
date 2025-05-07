package utility;

import java.io.Serializable;
import java.util.Objects;

import meteorology.Meteorology;

public class Coordinate implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final double MAX_LONGITUDE = 180.0;
	private static final double MIN_LONGITUDE = -180.0;
	private static final double MAX_LATITUDE = 90.0;
	private static final double MIN_LATITUDE = -90.0;

	private final double longitudeDegrees;
	private final double latitudeDegrees;
	private final double elevationFeet;

	public Coordinate(double longitudeDegrees, double latitudeDegrees, double elevationFeet) {
		this.longitudeDegrees = longitudeDegrees;
		this.latitudeDegrees = latitudeDegrees;
		this.elevationFeet = elevationFeet;
	}

	public double getLongitudeDegrees() {
		return longitudeDegrees;
	}

	public double getLatitudeDegrees() {
		return latitudeDegrees;
	}

	public double getElevationFeet() {
		return elevationFeet;
	}

	public double getElevationMeters() {
		return Meteorology.convertFeetToMeters(elevationFeet);
	}

	@Override
	public int hashCode() {
		return Objects.hash(elevationFeet, latitudeDegrees, longitudeDegrees);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Coordinate other)) {	
			return false;
		}
		return Double.doubleToLongBits(elevationFeet) == Double.doubleToLongBits(other.elevationFeet)
				&& Double.doubleToLongBits(latitudeDegrees) == Double.doubleToLongBits(other.latitudeDegrees)
				&& Double.doubleToLongBits(longitudeDegrees) == Double.doubleToLongBits(other.longitudeDegrees);
	}
	
	@Override
	public String toString() {
		return "Coordinate [longitudeDegrees=" + longitudeDegrees + ", latitudeDegrees=" + latitudeDegrees
				+ ", elevationFeet=" + elevationFeet + "]";
	}

	public static boolean validLongitude(double longitude) {
		return (longitude <= MAX_LONGITUDE && longitude >= MIN_LONGITUDE);
	}
	
	public static boolean validLatitude(double latitude) {
		return (latitude <= MAX_LATITUDE && latitude >= MIN_LATITUDE);
	}

}
