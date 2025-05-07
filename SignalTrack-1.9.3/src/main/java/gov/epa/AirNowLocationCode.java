package gov.epa;

import java.util.Objects;

public class AirNowLocationCode {
	private final String locationCode;
	private final double latitide;
	private final double longitude;
	private final String cityName;
	private final String state;

	public AirNowLocationCode(String locationCode, double latitide, double longitude, String cityName, String state) {
		this.locationCode = locationCode;
		this.latitide = latitide;
		this.longitude = longitude;
		this.cityName = cityName;
		this.state = state;
	}

	public String getLocationCode() {
		return locationCode;
	}

	public double getLatitide() {
		return latitide;
	}

	public double getLongitude() {
		return longitude;
	}

	public String getCityName() {
		return cityName;
	}

	public String getState() {
		return state;
	}

	@Override
	public int hashCode() {
		return Objects.hash(cityName, locationCode, latitide, longitude, state);
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
		final AirNowLocationCode other = (AirNowLocationCode) obj;
		return Objects.equals(cityName, other.cityName) && Objects.equals(locationCode, other.locationCode)
				&& Double.doubleToLongBits(latitide) == Double.doubleToLongBits(other.latitide)
				&& Double.doubleToLongBits(longitude) == Double.doubleToLongBits(other.longitude)
				&& Objects.equals(state, other.state);
	}

	@Override
	public String toString() {
		return "AirNowLocationCode [index=" + locationCode + ", latitide=" + latitide + ", longitude=" + longitude
				+ ", cityName=" + cityName + ", state=" + state + "]";
	}

}
