package radio;

import java.util.Objects;

public class ScanElement extends Measurement {
	private static final long serialVersionUID = 1L;
	
	private Boolean selected = false;
	private Boolean sampleRSSI = false;
	private Boolean sampleBER = false;
	private Boolean sampleSINAD = false;
	private Boolean noiseBlanker = false;
	private Boolean afc = false;
	private Boolean agc = false;
	private Double attenuator = 0.0D; 

	public Boolean isNoiseBlanker() {
		return noiseBlanker;
	}

	public void setNoiseBlanker(Boolean noiseBlanker) {
		this.noiseBlanker = noiseBlanker;
	}

	public Boolean isAFC() {
		return afc;
	}

	public void setAFC(Boolean afc) {
		this.afc = afc;
	}

	public Boolean isAGC() {
		return agc;
	}

	public void setAGC(Boolean agc) {
		this.agc = agc;
	}

	public Double getAttenuator() {
		return attenuator;
	}

	public void setAttenuator(Double attenuator) {
		this.attenuator = attenuator;
	}

	public boolean isScanSelected() {
		return selected;
	}
	
	public void setScanSelected(boolean selected) {
		this.selected = selected;
	}
	
	public boolean isSampleRSSI() {
		return sampleRSSI;
	}
	
	public void setSampleRSSI(boolean sampleRSSI) {
		this.sampleRSSI = sampleRSSI;
	}
	
	public boolean isSampleBER() {
		return sampleBER;
	}
	
	public void setSampleBER(boolean sampleBER) {
		this.sampleBER = sampleBER;
	}
	
	public boolean isSampleSINAD() {
		return sampleSINAD;
	}
	
	public void setSampleSINAD(boolean sampleSINAD) {
		this.sampleSINAD = sampleSINAD;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ Objects.hash(afc, agc, attenuator, noiseBlanker, sampleBER, sampleRSSI, sampleSINAD, selected);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}	
		if (!(obj instanceof ScanElement)) {
			return false;
		}
		ScanElement other = (ScanElement) obj;
		return Objects.equals(afc, other.afc) && Objects.equals(agc, other.agc)
				&& Objects.equals(attenuator, other.attenuator) && Objects.equals(noiseBlanker, other.noiseBlanker)
				&& Objects.equals(sampleBER, other.sampleBER) && Objects.equals(sampleRSSI, other.sampleRSSI)
				&& Objects.equals(sampleSINAD, other.sampleSINAD) && Objects.equals(selected, other.selected);
	}

	@Override
	public String toString() {
		return "ScanElement [selected=" + selected + ", sampleRSSI=" + sampleRSSI + ", sampleBER=" + sampleBER
				+ ", sampleSINAD=" + sampleSINAD + ", noiseBlanker=" + noiseBlanker + ", afc=" + afc + ", agc=" + agc
				+ ", attenuator=" + attenuator + "]";
	}

}
