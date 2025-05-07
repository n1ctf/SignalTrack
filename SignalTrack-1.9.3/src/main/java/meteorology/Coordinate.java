package meteorology;

import java.util.Objects;

/**
 *
 * @author n1ctf
 */
public class Coordinate {
	
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
		if (!(obj instanceof Coordinate)) {
			return false;
		}
		final Coordinate other = (Coordinate) obj;
		return Double.doubleToLongBits(elevationFeet) == Double.doubleToLongBits(other.elevationFeet)
				&& Double.doubleToLongBits(latitudeDegrees) == Double.doubleToLongBits(other.latitudeDegrees)
				&& Double.doubleToLongBits(longitudeDegrees) == Double.doubleToLongBits(other.longitudeDegrees);
	}

	@Override
	public String toString() {
		return "Coordinate [longitudeDegrees=" + longitudeDegrees + ", latitudeDegrees=" + latitudeDegrees
				+ ", elevationFeet=" + elevationFeet + "]";
	}
}

