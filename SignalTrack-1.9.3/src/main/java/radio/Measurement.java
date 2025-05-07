package radio;

import java.io.Serializable;
import java.util.Objects;

import radio.AbstractRadioReceiver.AccessMode;
import radio.AbstractRadioReceiver.StandardModeName;

public class Measurement implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private Integer id = 0;
	private Long measurementSetID;
	private Integer channelNumber;
	private int bandwidthHz;
	private Double ber;
	private Double dBm;
	private Double sinad;
	private Double frequency;
	private Boolean selected;
	private String plTone = "100.0";
	private String dplCode = "331";
	private boolean dplInverted;
	private byte colorCode = 1;
	private byte timeSlot = 1;
	private String networkAccessCode = "293";
	private AccessMode accessMode = AccessMode.CSQ;
	private StandardModeName modeName = StandardModeName.NFM;
	
	public Measurement() {}

	public Measurement(Measurement measurement) {
		this.id = measurement.id;
		this.measurementSetID = measurement.measurementSetID;
		this.channelNumber = measurement.channelNumber;
		this.ber = measurement.ber;
		this.dBm = measurement.dBm;
		this.sinad = measurement.sinad;
		this.frequency = measurement.frequency;
		this.selected = measurement.selected;
		this.plTone = measurement.plTone;
		this.dplCode = measurement.dplCode;
		this.dplInverted = measurement.dplInverted;
		this.networkAccessCode = measurement.networkAccessCode;
		this.accessMode = measurement.accessMode;
		this.modeName = measurement.getModeName();
		this.bandwidthHz = measurement.getBandwidthHz();
	}

	public Object[] toObjectArray() {
		final Object[] obj = new Object[15];
		obj[0] = id;
		obj[1] = measurementSetID;
		obj[2] = channelNumber;
	    obj[3] = ber;
	    obj[4] = dBm;
	    obj[5] = sinad;
	    obj[6] = frequency;
	    obj[7] = selected;
	    obj[8] = plTone;
	    obj[9] = dplCode;
	    obj[10] = dplInverted;
	    obj[11] = networkAccessCode;
	    obj[12] = accessMode;
	    obj[13] = modeName;
	    obj[14] = bandwidthHz;
	    return obj;
	}

	public static Measurement toMeasurement(final Object[] obj) {
		final Measurement measurement = new Measurement();
		measurement.id = (Integer) obj[0];
		measurement.measurementSetID = (Long) obj[1];
	    measurement.channelNumber = (Integer) obj[2];	
    	measurement.ber = (Double) obj[3];
    	measurement.dBm = (Double) obj[4];
    	measurement.sinad = (Double) obj[5];
    	measurement.frequency = (Double) obj[6];
    	measurement.selected = (Boolean) obj[7];
    	measurement.plTone = (String) obj[8];
    	measurement.dplCode = (String) obj[9];
    	measurement.dplInverted = (Boolean) obj[10];
    	measurement.networkAccessCode = (String) obj[11];
    	measurement.accessMode = (AccessMode) obj[12];
    	measurement.modeName = (StandardModeName) obj[13];
    	measurement.bandwidthHz = (Integer) obj[14];
    	
		return 	measurement;
	}
	
	public void fromObjectArray(final Object[] obj) {
		id = (Integer) obj[0];
		measurementSetID = (Long) obj[1];
	    channelNumber = (Integer) obj[2];	
    	ber = (Double) obj[3];
    	dBm = (Double) obj[4];
    	sinad = (Double) obj[5];
    	frequency = (Double) obj[6];
    	selected = (Boolean) obj[7];
    	plTone = (String) obj[8];
    	dplCode = (String) obj[9];
    	dplInverted = (Boolean) obj[10];
    	networkAccessCode = (String) obj[11];
    	accessMode = (AccessMode) obj[12];
    	modeName = (StandardModeName) obj[13];
    	bandwidthHz = (Integer) obj[14];
	}
	
	public final Integer getId() {
		return id;
	}

	public final void setId(Integer id) {
		this.id = id;
	}
	
	public final Long getMeasurementSetID() {
		return measurementSetID;
	}

	public final void setMeasurementSetID(Long sequence) {
		this.measurementSetID = sequence;
	}

	public final Integer getChannelNumber() {
		return channelNumber;
	}

	public final void setChannelNumber(Integer channelNumber) {
		this.channelNumber = channelNumber;
	}

	public final Double getBer() {
		return ber;
	}

	public final void setBer(Double ber) {
		this.ber = ber;
	}

	public final Double getdBm() {
		return dBm;
	}

	public final void setdBm(Double dBm) {
		this.dBm = dBm;
	}

	public final Double getSinad() {
		return sinad;
	}

	public final void setSinad(Double sinad) {
		this.sinad = sinad;
	}

	public final Double getFrequency() {
		return frequency;
	}

	public final void setFrequency(Double frequency) {
		this.frequency = frequency;
	}

	public final Boolean getSelected() {
		return selected;
	}

	public final void setSelected(Boolean selected) {
		this.selected = selected;	
	}
	
	public String getPLTone() {
		return plTone;
	}

	public void setPLTone(String plTone) {
		this.plTone = plTone;
	}

	public String getDPLCode() {
		return dplCode;
	}

	public void setDPLCode(String dplCode) {
		this.dplCode = dplCode;
	}

	public boolean isDPLInverted() {
		return dplInverted;
	}

	public void setDPLInverted(boolean dplInverted) {
		this.dplInverted = dplInverted;
	}

	public String getNetworkAccessCode() {
		return networkAccessCode;
	}

	public void setNetworkAccessCode(String networkAccessCode) {
		this.networkAccessCode = networkAccessCode;
	}

	public AccessMode getSquelchMode() {
		return accessMode;
	}

	public void setSquelchMode(AccessMode accessMode) {
		this.accessMode = accessMode;
	}

	public StandardModeName getModeName() {
		return modeName;
	}
	
	public void setModeName(StandardModeName modeName) {
		this.modeName = modeName;
	}
	
	public byte getColorCode() {
		return colorCode;
	}

	public void setColorCode(byte colorCode) {
		this.colorCode = colorCode;
	}

	public byte getTimeSlot() {
		return timeSlot;
	}

	public void setTimeSlot(byte timeSlot) {
		this.timeSlot = timeSlot;
	}
	
	public int getBandwidthHz() {
		return bandwidthHz;
	}

	public void setBandwidthHz(int bandwidthHz) {
		this.bandwidthHz = bandwidthHz;
	}

	public static Measurement copy(Measurement measurement) {
        return new Measurement(measurement);
    }

	@Override
	public int hashCode() {
		return Objects.hash(bandwidthHz, ber, channelNumber, colorCode, dBm, dplCode, dplInverted, frequency, id,
				measurementSetID, modeName, networkAccessCode, plTone, selected, sinad, accessMode, timeSlot);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Measurement))
			return false;
		Measurement other = (Measurement) obj;
		return bandwidthHz == other.bandwidthHz && Objects.equals(ber, other.ber)
				&& Objects.equals(channelNumber, other.channelNumber) && colorCode == other.colorCode
				&& Objects.equals(dBm, other.dBm) && Objects.equals(dplCode, other.dplCode)
				&& dplInverted == other.dplInverted && Objects.equals(frequency, other.frequency)
				&& Objects.equals(id, other.id) && Objects.equals(measurementSetID, other.measurementSetID)
				&& modeName == other.modeName && Objects.equals(networkAccessCode, other.networkAccessCode)
				&& Objects.equals(plTone, other.plTone) && Objects.equals(selected, other.selected)
				&& Objects.equals(sinad, other.sinad) && accessMode == other.accessMode && timeSlot == other.timeSlot;
	}

	@Override
	public String toString() {
		return "Measurement [id=" + id + ", measurementSetID=" + measurementSetID + ", channelNumber=" + channelNumber
				+ ", bandwidthHz=" + bandwidthHz + ", ber=" + ber + ", dBm=" + dBm + ", sinad=" + sinad + ", frequency="
				+ frequency + ", selected=" + selected + ", plTone=" + plTone + ", dplCode=" + dplCode
				+ ", dplInverted=" + dplInverted + ", colorCode=" + colorCode + ", timeSlot=" + timeSlot
				+ ", networkAccessCode=" + networkAccessCode + ", squelchMode=" + accessMode + ", modeName=" + modeName
				+ "]";
	}
}

