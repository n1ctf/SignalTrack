package hebrew;

public class Location {

	private String name = "";
	
	private int latitude;
	private int longitude;
	private int timeZone;
	private int elevation;

	public Location(String name, int latitude, int longitude, int timeZone, int elevation) {
		this.name = name;
		this.latitude = latitude;
		this.longitude = longitude;
		this.timeZone = timeZone;
		this.elevation = elevation;
	}

	public String getName() {
		return name;
	}

	public int getLatitude() {
		return latitude;
	}

	public int getLongitude() {
		return longitude;
	}

	public int getTimeZone() {
		return timeZone;
	}

	public int getElevation() {
		return elevation;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setLatitude(int latitude) {
		this.latitude = latitude;
	}

	public void setLongitude(int longitude) {
		this.longitude = longitude;
	}

	public void setTimeZone(int timeZone) {
		this.timeZone = timeZone;
	}

	public void setElevation(int elevation) {
		this.elevation = elevation;
	}
}
