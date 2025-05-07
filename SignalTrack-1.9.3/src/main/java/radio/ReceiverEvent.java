package radio;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class ReceiverEvent extends PropertyChangeSupport {
	private static final long serialVersionUID = 1L;
	
	public static final String SIGNAL_OFFSET = "SIGNAL_OFFSET";
	public static final String DTMF_DECODE = "DTMF_DECODE";
	public static final String WAVEFORM_DATA = "WAVEFORM_DATA";
	public static final String BREAK = "BREAK";
	public static final String FIRMWARE = "FIRMWARE";
	public static final String COUNTRY = "COUNTRY";
	public static final String DSP = "DSP";
	public static final String POWER_STATE_CHANGE = "POWER_STATE_CHANGE";
	public static final String ON_LINE = "ON_LINE";
	public static final String PROTOCOL = "PROTOCOL";
	public static final String RECEIVED_DATA_ERROR = "RECEIVED_DATA_ERROR";
	public static final String EXCEPTION = "EXCEPTION";
	public static final String RSSI = "RSSI";
	public static final String DBM = "DBM";
	public static final String BUSY = "BUSY";
	public static final String BER = "BER";
	public static final String SINAD_CHANGE = "SINAD_CHANGE";
	public static final String CAL_FILE_CHANGE = "CAL_FILE_CHANGE";
	public static final String RX_CENTERED = "RX_CENTERED";
	public static final String PL_DECODE = "PL_DECODE";
	public static final String DPL_DECODE = "DPL_DECODE";
	public static final String CURRENT_FREQ = "CURRENT_FREQ";
	public static final String CURRENT_MODE = "CURRENT_MODE";
	public static final String ACK = "ACK";
	public static final String RX_DATA = "RX_DATA";
	public static final String BAUD_RATE_CHANGE = "BAUD_RATE_CHANGE";
	public static final String CANCEL_EVENTS = "CANCEL_EVENTS";
	public static final String RADIO_THREADS_TERMINATED = "RADIO_THREADS_TERMINATED";
	public static final String RADIO_FEATURE_SET_READY = "RADIO_FEATURE_SET_READY";
	public static final String START_RADIO_WITH_SYSTEM = "START_RADIO_WITH_SYSTEM";
	
	public ReceiverEvent() {
		super(new Object());
	}
	
	public ReceiverEvent(Object object) {
		super(object);
	}
	
	public ReceiverEvent getReceiverEvent() {
		return this;
	}

	public boolean isPropertyChangeListenerRegistered(PropertyChangeListener listener) {
		for (PropertyChangeListener l : getPropertyChangeListeners()) {
			if (listener.equals(l)) return true;
		}
		return false;
	}
}
