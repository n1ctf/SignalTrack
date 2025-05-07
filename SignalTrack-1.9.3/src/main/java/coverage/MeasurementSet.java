package coverage;

import java.awt.Point;
import java.awt.geom.Point2D;

public class MeasurementSet {
	private Long id = 0L; 
	private Long millis;
	private Integer testTileID;
	private Point2D position;
	private Double dopplerDirection = 0d;
	private Integer dopplerQuality = 0;
	private Integer marker = 0;
	
	public MeasurementSet() {}

	public MeasurementSet(MeasurementSet measurementSet) {
		this.id = measurementSet.id;
		this.millis = measurementSet.millis;
		this.testTileID = measurementSet.testTileID;
		this.position = measurementSet.position;
		this.dopplerDirection = measurementSet.dopplerDirection;
		this.dopplerQuality = measurementSet.dopplerQuality;
		this.marker = measurementSet.marker;
	}

	public Object[] toObjectArray() {
		final Object[] obj = new Object[8];
		obj[0] = id;
		obj[1] = testTileID;
		obj[2] = millis;
		obj[3] = position.getX();
		obj[4] = position.getY();
		obj[5] = dopplerDirection;
		obj[6] = dopplerQuality;
		obj[7] = marker;
		return obj;
	}

	public static MeasurementSet toMeasurementSet(final Object[] obj) {
		final MeasurementSet set = new MeasurementSet();
		set.id = (Long) obj[0];
		set.testTileID = (Integer) obj[1];
	    set.millis = (Long) obj[2]; 
		set.position = new Point.Double((Double) obj[3], (Double) obj[4]);
		set.dopplerDirection = (Double) obj[5];
		set.dopplerQuality = (Integer) obj[6];
		set.marker = (Integer) obj[7];
		return 	set;
	}
	
	public void fromObjectArray(final Object[] obj) {
		id = (Long) obj[0];
		testTileID = (Integer) obj[1];
	    millis = (Long) obj[2]; 
		position = new Point.Double((Double) obj[3], (Double) obj[4]);
		dopplerDirection = (Double) obj[5];
		dopplerQuality = (Integer) obj[6];
		marker = (Integer) obj[7];
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getTestTileID() {
		return testTileID;
	}

	public void setTestTileID(Integer testTileID) {
		this.testTileID = testTileID;
	}

	public Long getMillis() {
		return millis;
	}

	public void setMillis(Long millis) {
		this.millis = millis;
	}

	public Point2D getPosition() {
		return position;
	}

	public void setPosition(Point2D position) {
		this.position = position;
	}

	public Double getDopplerDirection() {
		return dopplerDirection;
	}

	public void setDopplerDirection(Double dopplerDirection) {
		this.dopplerDirection = dopplerDirection;
	}

	public Integer getDopplerQuality() {
		return dopplerQuality;
	}

	public void setDopplerQuality(Integer dopplerQuality) {
		this.dopplerQuality = dopplerQuality;
	}

	public Integer getMarker() {
		return marker;
	}

	public void setMarker(Integer marker) {
		this.marker = marker;
	}
	
	public static MeasurementSet copy(MeasurementSet measurementSet) throws CloneNotSupportedException {
        if(!(measurementSet instanceof Cloneable)) {
            throw new CloneNotSupportedException("Clone Not Supported Exception");
        }
        return new MeasurementSet(measurementSet);
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((dopplerDirection == null) ? 0 : dopplerDirection.hashCode());
		result = (prime * result) + ((dopplerQuality == null) ? 0 : dopplerQuality.hashCode());
		result = (prime * result) + ((id == null) ? 0 : id.hashCode());
		result = (prime * result) + ((marker == null) ? 0 : marker.hashCode());
		result = (prime * result) + ((millis == null) ? 0 : millis.hashCode());
		result = (prime * result) + ((position == null) ? 0 : position.hashCode());
		result = (prime * result) + ((testTileID == null) ? 0 : testTileID.hashCode());
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
		final MeasurementSet other = (MeasurementSet) obj;
		if (dopplerDirection == null) {
			if (other.dopplerDirection != null) {
				return false;
			}
		} else if (!dopplerDirection.equals(other.dopplerDirection)) {
			return false;
		}
		if (dopplerQuality == null) {
			if (other.dopplerQuality != null) {
				return false;
			}
		} else if (!dopplerQuality.equals(other.dopplerQuality)) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (marker == null) {
			if (other.marker != null) {
				return false;
			}
		} else if (!marker.equals(other.marker)) {
			return false;
		}
		if (millis == null) {
			if (other.millis != null) {
				return false;
			}
		} else if (!millis.equals(other.millis)) {
			return false;
		}
		if (position == null) {
			if (other.position != null) {
				return false;
			}
		} else if (!position.equals(other.position)) {
			return false;
		}
		if (testTileID == null) {
			if (other.testTileID != null) {
				return false;
			}
		} else if (!testTileID.equals(other.testTileID)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "MeasurementSet [id=" + id + ", millis=" + millis + ", testTileID=" + testTileID + ", position="
				+ position + ", dopplerDirection=" + dopplerDirection + ", dopplerQuality=" + dopplerQuality
				+ ", marker=" + marker + "]";
	}
	
}

