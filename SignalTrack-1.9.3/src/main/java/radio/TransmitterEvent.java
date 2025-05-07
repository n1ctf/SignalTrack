package radio;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class TransmitterEvent extends PropertyChangeSupport {
	private static final long serialVersionUID = 1L;

	public static final String FIRMWARE = "FIRMWARE";
	public static final String COUNTRY = "COUNTRY";
	public static final String DSP = "DSP";
	public static final String POWER_STATE_CHANGE = "POWER_STATE_CHANGE";
	public static final String TX_POWER_OUTPUT_WATTS = "TX_POWER_OUTPUT_WATTS";
	public static final String ON_LINE = "ON_LINE";
	public static final String PROTOCOL = "PROTOCOL";
	public static final String TRANSMISSION_DATA_ERROR = "RECEIVED_DATA_ERROR";
	public static final String EXCEPTION = "EXCEPTION";
	public static final String CAL_FILE_CHANGE = "CAL_FILE_CHANGE";
	public static final String PL_ENCODE = "PL_ENCODE";
	public static final String DPL_ENCODE = "DPL_ENCODE";
	public static final String CURRENT_FREQ = "CURRENT_FREQ";
	public static final String CURRENT_MODE = "CURRENT_MODE";
	public static final String ACK = "ACK";
	public static final String TX_DATA = "TX_DATA";
	public static final String PTT = "PTT";
	public static final String BAUD_RATE_CHANGE = "BAUD_RATE_CHANGE";
	public static final String CANCEL_EVENTS = "CANCEL_EVENTS";
	public static final String RADIO_THREADS_TERMINATED = "RADIO_THREADS_TERMINATED";
	public static final String RADIO_FEATURE_SET_READY = "RADIO_FEATURE_SET_READY";
	public static final String START_RADIO_WITH_SYSTEM = "START_RADIO_WITH_SYSTEM";
	
	public TransmitterEvent() {
		super(new Object());
	}
	
	public TransmitterEvent(Object object) {
		super(object);
	}
	
	public TransmitterEvent getTransmitterEvent() {
		return this;
	}

	public boolean isPropertyChangeListenerRegistered(PropertyChangeListener listener) {
		for (PropertyChangeListener l : getPropertyChangeListeners()) {
			if (listener.equals(l)) return true;
		}
		return false;
	}
}
