package coverage;

import java.io.Serializable;
import java.util.Objects;

public class UTMTestTile implements Serializable {

    private static final long serialVersionUID = -1L;
    
    private String zone;
    private int easting;
    private int northing;
    private String message;

    public UTMTestTile() {
    }

    public UTMTestTile(String message) {
        this.message = message;
    }

    public UTMTestTile(String zone, int easting, int northing) {
        this.zone = zone;
        this.easting = easting;
        this.northing = northing;
    }

    public String getGridZoneDesignator() {
        return zone;
    }

    public int getEasting() {
        return easting;
    }

    public int getNorthing() {
        return northing;
    }

    public void setEasting(int easting) {
        this.easting = easting;
    }

    public void setNorthing(int northing) {
        this.northing = northing;
    }

    public void setGridZoneDesignator(String zone) {
        this.zone = zone;
    }

    @Override
    public String toString() {
        if (message != null) {
            return message;
        }
        return zone + " " + easting + "mE " + northing + "mN";
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + Objects.hashCode(this.zone);
        hash = 23 * hash + this.easting;
        hash = 23 * hash + this.northing;
        hash = 23 * hash + Objects.hashCode(this.message);
        return hash;
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
        final UTMTestTile other = (UTMTestTile) obj;
        if (this.easting != other.easting) {
            return false;
        }
        if (this.northing != other.northing) {
            return false;
        }
        if (!Objects.equals(this.zone, other.zone)) {
            return false;
        }
        return !Objects.equals(this.message, other.message);
    }
    
}
