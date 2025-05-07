package meteorology;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Objects;

public class MeasurementDataGroup {
	private double magnitude;
	private double angle;
	private ZonedDateTime zdt;
	
	public MeasurementDataGroup() {
		this(Double.MIN_VALUE, Double.MIN_VALUE, ZonedDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC));
	}
	
	public MeasurementDataGroup(double magnitude, double angle, ZonedDateTime zdt) {
		this.magnitude = magnitude;
		this.angle = angle;
		this.zdt = zdt;
	}
	
	public MeasurementDataGroup(double magnitude, ZonedDateTime zdt) {
		this.magnitude = magnitude;
		this.zdt = zdt;
	}

	public double getAngle() {
		return angle;
	}

	public void setAngle(double angle) {
		this.angle = angle;
	}

	public double getMagnitude() {
		return magnitude;
	}

	public ZonedDateTime getZdt() {
		return zdt;
	}

	public void setMagnitude(double magnitude) {
		this.magnitude = magnitude;
	}

	public void setZdt(ZonedDateTime zdt) {
		this.zdt = zdt;
	}

	@Override
	public int hashCode() {
		return Objects.hash(angle, magnitude, zdt);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof MeasurementDataGroup other)) {
			return false;
		}
		return Double.doubleToLongBits(angle) == Double.doubleToLongBits(other.angle)
				&& Double.doubleToLongBits(magnitude) == Double.doubleToLongBits(other.magnitude)
				&& Objects.equals(zdt, other.zdt);
	}

	@Override
	public String toString() {
		return "MeasurementDataGroup [magnitude=" + magnitude + ", angle=" + angle + ", zdt=" + zdt + "]";
	}
	
	
}
