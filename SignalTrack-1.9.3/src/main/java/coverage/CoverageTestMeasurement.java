package coverage;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class CoverageTestMeasurement {
	private Integer id;
	private String sentence; 
	private Long millis;
	private List<Double> ber = new ArrayList<>(10);
	private List<Double> dBm = new ArrayList<>(10);
	private List<Double> sinad = new ArrayList<>(10);
	private List<Double> freq = new ArrayList<>(10);
	private List<Boolean> select = new ArrayList<>(10);
	private Integer testTileID;
	private Point2D position;
	private Double dopplerDirection = 0D;
	private Integer dopplerQuality = 0;
	private Integer marker = 0;
	
	public CoverageTestMeasurement() {}
	
	public CoverageTestMeasurement(CoverageTestMeasurement data) {
		this.id = data.id;
		this.sentence = data.sentence;
		this.millis = data.millis;
		this.ber = data.ber;
		this.dBm = data.dBm;
		this.sinad = data.sinad;
		this.freq = data.freq;
		this.select = data.select;
		this.testTileID = data.testTileID;
		this.position = data.position;
		this.dopplerDirection = data.dopplerDirection;
		this.dopplerQuality = data.dopplerQuality;
		this.marker = data.marker;
	}

	public Object[] toObjectArray() {
		final Object[] obj = new Object[59];
		obj[0] = id;
		obj[1] = sentence;
		obj[2] = millis;
		
		for (Integer i = 0; i < 10; i++) {
	    	obj[(i*5)+3] = ber.get(i);
	    	obj[(i*5)+4] = dBm.get(i);
	    	obj[(i*5)+5] = sinad.get(i);
	    	obj[(i*5)+6] = freq.get(i);
	    	obj[(i*5)+7] = select.get(i);
	    }
		
		obj[53] = testTileID;
		obj[54] = position.getX();
		obj[55] = position.getY();
		obj[56] = dopplerDirection;
		obj[57] = dopplerQuality;
		obj[58] = marker;
		
		return obj;
	}

	public static CoverageTestMeasurement fromObjectArray(final Object[] obj) {
		final CoverageTestMeasurement ct = new CoverageTestMeasurement();
		ct.id = (Integer) obj[0];
		ct.sentence = (String) obj[1];
	    ct.millis = (Long) obj[2];
	    
	    for (Integer i = 0; i < 10; i++) { 	
	    	ct.ber.add((Double) obj[(i*5)+3]);
	    	ct.dBm.add((Double) obj[(i*5)+4]);
	    	ct.sinad.add((Double) obj[(i*5)+5]);
	    	ct.freq.add((Double) obj[(i*5)+6]);
	    	ct.select.add((Boolean) obj[(i*5)+7]);
	    }

	    ct.testTileID = (Integer) obj[53];  
		ct.position = new Point.Double((Double) obj[54], (Double) obj[55]);
		ct.dopplerDirection = (Double) obj[56];
		ct.dopplerQuality = (Integer) obj[57];
		ct.marker = (Integer) obj[58];
		
		return 	ct;
	}

    @Override
    public boolean equals(Object other) {
        return (other instanceof CoverageTestMeasurement ctm) && (id != null) ? id.equals((ctm).id) : (other == this);
    }

    @Override
    public int hashCode() {
        return (id != null) 
             ? (this.getClass().hashCode() + id.hashCode()) 
             : super.hashCode();
    }
    
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
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

	public List<Double> getBer() {
		return new ArrayList<>(ber);
	}

	public void setBer(List<Double> ber) {
		this.ber = new ArrayList<>(ber);
	}
	
	public void setBer(int index, Double ber) {
		this.ber.add(index, ber);
	}
	
	public void setdBm(int index, Double dBm) {
		this.dBm.add(index, dBm);
	}
	
	public void setSinad(int index, Double sinad) {
		this.sinad.add(index, sinad);
	}
	
	public void setSelect(List<Boolean> select) {
		this.select = new ArrayList<>(select);
	}
	
	public void setSelect(int index, Boolean select) {
		this.select.add(index, select);
	}
	
	public Boolean getSelect(int index) {
		return select.get(index);
	}
	
	public Double getFreq(int index) {
		return freq.get(index);
	}
	
	public Double getBer(int index) {
		return ber.get(index);
	}
	
	public Double getSinad(int index) {
		return sinad.get(index);
	}
	
	public Double getdBm(int index) {
		return dBm.get(index);
	}
	
	public List<Double> getdBm() {
		return new ArrayList<>(dBm);
	}

	public void setdBm(List<Double> dBm) {
		this.dBm = new ArrayList<>(dBm);
	}

	public List<Double> getSinad() {
		return new ArrayList<>(sinad);
	}

	public void setSinad(List<Double> sinad) {
		this.sinad = new ArrayList<>(sinad);
	}
	
	public void setFreq(int index, Double freq) {
		this.freq.add(index, freq);
	}
	
	public List<Double> getFreq() {
		return new ArrayList<>(freq);
	}

	public void setFreq(List<Double> freq) {
		this.freq = new ArrayList<>(freq);
	}

	public Integer getTestTileID() {
		return testTileID;
	}

	public void setTestTileID(Integer testTileID) {
		this.testTileID = testTileID;
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

}

