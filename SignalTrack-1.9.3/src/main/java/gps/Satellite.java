package gps;

/**
 *
 * @author John
 */
public class Satellite implements Cloneable {

    private String prn;
    private String elevation;
    private String azimuth;
    private String snr; // in dBHz

    /**
     *
     * @param prn
     * @param elevation
     * @param azimuth
     * @param snr
     */
    public Satellite(String prn, String elevation, String azimuth, String snr) {
        this.prn = prn;
        this.elevation = elevation;
        this.azimuth = azimuth;
        this.snr = snr;
    }

    public Satellite(com.ivkos.gpsd4j.messages.Satellite satellite) {
    	this.prn = String.valueOf(satellite.getPRN());
    	this.elevation = String.valueOf(satellite.getElevation());
    	this.azimuth = String.valueOf(satellite.getAzimuth());
    	this.snr = String.valueOf(satellite.getSignalStrength());
    }
    
    public Satellite(Satellite satellite) {
		this.prn = satellite.prn;
		this.elevation = satellite.elevation;
		this.azimuth = satellite.azimuth;
		this.snr = satellite.snr;
	}

	/**
     * @return the prn
     */
    public String getPrn() {
        return prn;
    }

    /**
     * @param prn the prn to set
     */
    public void setPrn(String prn) {
        this.prn = prn;
    }

    /**
     * @return the elevation
     */
    public String getElevation() {
        return elevation;
    }

    /**
     * @param elevation the elevation to set
     */
    public void setElevation(String elevation) {
        this.elevation = elevation;
    }

    /**
     * @return the azimuth
     */
    public String getAzimuth() {
        return azimuth;
    }

    /**
     * @param azimuth the azimuth to set
     */
    public void setAzimuth(String azimuth) {
        this.azimuth = azimuth;
    }

    /**
     * @return the snr
     */
    public String getSnr() {
        return snr;
    }

    /**
     * @param snr the snr to set
     */
    public void setSnr(String snr) {
        this.snr = snr;
    }

    @Override
    public int hashCode() {
        final var prime = 31;
        var result = 1;
        result = prime * result + ((azimuth == null) ? 0 : azimuth.hashCode());
        result = prime * result + ((elevation == null) ? 0 : elevation.hashCode());
        result = prime * result + ((prn == null) ? 0 : prn.hashCode());
        result = prime * result + ((snr == null) ? 0 : snr.hashCode());
        return result;
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
     */
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
        final var other = (Satellite) obj;
        if (azimuth == null) {
            if (other.azimuth != null) {
                return false;
            }
        } else if (!azimuth.equals(other.azimuth)) {
            return false;
        }
        if (elevation == null) {
            if (other.elevation != null) {
                return false;
            }
        } else if (!elevation.equals(other.elevation)) {
            return false;
        }
        if (prn == null) {
            if (other.prn != null) {
                return false;
            }
        } else if (!prn.equals(other.prn)) {
            return false;
        }
        if (snr == null) {
            if (other.snr != null) {
                return false;
            }
        } else if (!snr.equals(other.snr)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Satellite [prn=" + prn + ", elevation=" + elevation + ", azimuth=" + azimuth + ", snr=" + snr + "]";
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
      return super.clone();
    }
    
    
}
