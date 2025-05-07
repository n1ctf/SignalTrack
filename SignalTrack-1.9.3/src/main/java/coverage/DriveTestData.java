package coverage;

import java.awt.geom.Point2D;
import java.util.Arrays;

public class DriveTestData {
	private Long id;
	private String sentence; 
	private Long millis;
	private Double[] ber = new Double[10];
	private Integer[] rssi = new Integer[10];
	private Integer[] sinad = new Integer[10];
	private Double[] freq = new Double[10];
	private UTMTestTile testTileLastMeasured = new UTMTestTile();
	private Integer tilesTraversed;
	private Integer measurementDelayTimer;
	private Integer tileIndexPointer;
	private Point2D.Double tileSize = new Point2D.Double();
	private Integer maximumSamplesPerTile;
	private Integer minimumSamplesPerTile;
	private Point2D.Double position = new Point2D.Double();
	private Double dopplerDirection;
	private Integer dopplerQuality;
	private Integer marker;
	
	public DriveTestData() {}
	
	public DriveTestData(DriveTestData data) {
		this.id = data.id;
		this.sentence = data.sentence;
		this.millis = data.millis;
		this.ber = data.ber;
		this.rssi = data.rssi;
		this.sinad = data.sinad;
		this.freq = data.freq;
		this.testTileLastMeasured = data.testTileLastMeasured;
		this.tilesTraversed = data.tilesTraversed;
		this.measurementDelayTimer = data.measurementDelayTimer;
		this.tileIndexPointer = data.tileIndexPointer;
		this.tileSize = data.tileSize;
		this.maximumSamplesPerTile = data.maximumSamplesPerTile;
		this.minimumSamplesPerTile = data.minimumSamplesPerTile;
		this.position = data.position;
		this.dopplerDirection = data.dopplerDirection;
		this.dopplerQuality = data.dopplerQuality;
		this.marker = data.marker;
	}

	public static Object[] toObjectArray(DriveTestData data) {
		final Object[] obj = new Object[58];
		obj[0] = data.id;
		obj[1] = data.sentence;
		obj[2] = Long.toString(data.millis);
		
		for (Integer i = 0; i < 10; i++) {
	    	obj[(i*4)+3] = data.ber[i];
	    	obj[(i*4)+4] = data.rssi[i];
	    	obj[(i*4)+5] = data.sinad[i];
	    	obj[(i*4)+6] = data.freq[i];
	    }
		
		obj[43] = data.testTileLastMeasured.getEasting();
		obj[44] = data.testTileLastMeasured.getNorthing();
		obj[45] = data.testTileLastMeasured.getGridZoneDesignator();
		obj[46] = data.tilesTraversed;
		obj[47] = data.measurementDelayTimer;
		obj[48] = data.tileIndexPointer;
		obj[49] = data.tileSize.x;
		obj[50] = data.tileSize.y;
		obj[51] = data.maximumSamplesPerTile;
		obj[52] = data.minimumSamplesPerTile;
		obj[53] = data.position.x;
		obj[54] = data.position.y;
		obj[55] = data.dopplerDirection;
		obj[56] = data.dopplerQuality;
		obj[57] = data.marker;
		
		return obj;
	}

	public static DriveTestData objectArrayToDriveTestData(Object[] obj) {
		final DriveTestData dt = new DriveTestData();
		dt.id = (Long) obj[0];
		dt.sentence = (String) obj[1];
	    dt.millis = (Long) obj[2];
	    
	    for (Integer i = 0; i < 10; i++) { 	
	    	dt.ber[i] = (Double) obj[(i*4)+3];
	    	dt.rssi[i] = (Integer) obj[(i*4)+4];
	    	dt.sinad[i] = (Integer) obj[(i*4)+5];
	    	dt.freq[i] = (Double) obj[(i*4)+6];
	    }
		
	    dt.testTileLastMeasured.setEasting((Integer) obj[43]);
		dt.testTileLastMeasured.setNorthing((Integer) obj[44]);
		dt.testTileLastMeasured.setGridZoneDesignator((String) obj[45]);
		dt.tilesTraversed = (Integer) obj[46];
		dt.measurementDelayTimer = (Integer) obj[47];  
		dt.tileIndexPointer = (Integer) obj[48];
		dt.tileSize.x = (Double) obj[49];
		dt.tileSize.y = (Double) obj[50];
		dt.maximumSamplesPerTile = (Integer) obj[51];
		dt.minimumSamplesPerTile = (Integer) obj[52];
		dt.position.x = (Double) obj[53];
		dt.position.y = (Double) obj[54];
		dt.dopplerDirection = (Double) obj[55];
		dt.dopplerQuality = (Integer) obj[56];
		dt.marker = (Integer) obj[57];
		
		return 	dt;
	}
	
    /**
     * The user ID is unique for each User. So this should compare User by ID only.
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object other) {
        return (other instanceof DriveTestData) && (id != null)
             ? id.equals(((DriveTestData) other).id)
             : (other == this);
    }

    /**
     * The user ID is unique for each User. So User with same ID should return same hashcode.
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return (id != null) 
             ? (this.getClass().hashCode() + id.hashCode()) 
             : super.hashCode();
    }
    
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSentence() {
		return sentence;
	}

	public void setSentence(String sentence) {
		this.sentence = sentence;
	}

	public Long getMillis() {
		return millis;
	}

	public void setMillis(Long millis) {
		this.millis = millis;
	}

	public Double[] getBer() {
		return ber.clone();
	}

	public void setBer(Double[] ber) {
		this.ber = ber.clone();
	}

	public Integer[] getRssi() {
		return rssi.clone();
	}

	public void setRssi(Integer[] rssi) {
		this.rssi = rssi.clone();
	}

	public Integer[] getSinad() {
		return sinad.clone();
	}

	public void setSinad(Integer[] sinad) {
		this.sinad = sinad.clone();
	}

	public Double[] getFreq() {
		return freq.clone();
	}

	public void setFreq(Double[] freq) {
		this.freq = freq.clone();
	}

	public UTMTestTile getTestTileLastMeasured() {
		return testTileLastMeasured;
	}

	public void setTestTileLastMeasured(UTMTestTile testTileLastMeasured) {
		this.testTileLastMeasured = testTileLastMeasured;
	}

	public Integer getTilesTraversed() {
		return tilesTraversed;
	}

	public void setTilesTraversed(Integer tilesTraversed) {
		this.tilesTraversed = tilesTraversed;
	}

	public Integer getMeasurementDelayTimer() {
		return measurementDelayTimer;
	}

	public void setMeasurementDelayTimer(Integer measurementDelayTimer) {
		this.measurementDelayTimer = measurementDelayTimer;
	}

	public Integer getTileIndexPointer() {
		return tileIndexPointer;
	}

	public void setTileIndexPointer(Integer tileIndexPointer) {
		this.tileIndexPointer = tileIndexPointer;
	}

	public Point2D.Double getTileSize() {
		return tileSize;
	}

	public void setTileSize(Point2D.Double tileSize) {
		this.tileSize = tileSize;
	}

	public Integer getMaximumSamplesPerTile() {
		return maximumSamplesPerTile;
	}

	public void setMaximumSamplesPerTile(Integer maximumSamplesPerTile) {
		this.maximumSamplesPerTile = maximumSamplesPerTile;
	}

	public Integer getMinimumSamplesPerTile() {
		return minimumSamplesPerTile;
	}

	public void setMinimumSamplesPerTile(Integer minimumSamplesPerTile) {
		this.minimumSamplesPerTile = minimumSamplesPerTile;
	}

	public Point2D.Double getPosition() {
		return position;
	}

	public void setPosition(Point2D.Double position) {
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

	@Override
	public String toString() {
		return "DriveTestData [id=" + id + ", sentence=" + sentence + ", millis=" + millis + ", ber="
				+ Arrays.toString(ber) + ", rssi=" + Arrays.toString(rssi) + ", sinad=" + Arrays.toString(sinad)
				+ ", freq=" + Arrays.toString(freq) + ", tilesTraversed=" + tilesTraversed + ", measurementDelayTimer="
				+ measurementDelayTimer + ", tileIndexPointer=" + tileIndexPointer + ", tileSize=" + tileSize
				+ ", maximumSamplesPerTile=" + maximumSamplesPerTile + ", minimumSamplesPerTile="
				+ minimumSamplesPerTile + ", position=" + position + ", dopplerDirection=" + dopplerDirection
				+ ", dopplerQuality=" + dopplerQuality + ", marker=" + marker + "]";
	}
	
	
}

