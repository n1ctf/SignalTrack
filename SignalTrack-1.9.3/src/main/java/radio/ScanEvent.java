package radio;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class ScanEvent extends PropertyChangeSupport {
	private static final long serialVersionUID = 6192294883918185372L;
	
	public static final String SCAN_MEASUREMENT_READY = "SCAN_MEASUREMENT_READY";
	public static final String SCAN_DWELL_TIME_READY = "SCAN_DWELL_TIME_READY";
	public static final String SCAN_LIST_MODIFIED = "SCAN_LIST_MODIFIED";
	public static final String SCAN_ENABLE_CHANGE = "SCAN_ENABLE_CHANGE";
	public static final String SCAN_MEASUREMENT_LIST_READY = "SCAN_MEASUREMENT_LIST_READY";
	
	public ScanEvent() {
		super(new Object());
	}
	
	public ScanEvent(Object object) {
		super(object);
	}
	
	public ScanEvent getScanEvent() {
		return this;
	}

	public boolean isPropertyChangeListenerRegistered(PropertyChangeListener listener) {
		for (PropertyChangeListener l : getPropertyChangeListeners()) {
			if (listener.equals(l)) return true;
		}
		return false;
	}
}
