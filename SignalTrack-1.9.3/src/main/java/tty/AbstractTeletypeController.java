package tty;

import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import utility.Utility;
import jssc.SerialPortList;

public abstract class AbstractTeletypeController implements AutoCloseable {

	public enum TTYEvents {
		CTS, 
		DSR, 
		RLSD, 
		RING, 
		BREAK, 
		TX_EMPTY, 
		RX_FLAG, 
		RX_CHAR, 
		ERROR, 
		PURGE_FAILURE, 
		FLOW_CONTROL_ERROR, 
		TX_DATA,
		RX_DATA, 
		DATA_RECEIVED, 
		ONLINE, 
		INVALID_COMM_PORT, 
		ADVISE_PORT_CLOSING, 
		PORT_OPEN, 
		TTY_PORT_CONFIGURATION_ERROR,
		SEND_TO_TTY_PORT, 
		CLOSE_TTY_PORT, 
		SET_BAUD_RATE, 
		TTY_PORT_ERROR, 
		RTS_NOT_SET_ERROR, 
		DTR_NOT_SET_ERROR,
		RLSD_QUERY_ERROR, 
		TRANSMIT_FAILURE, 
		RECEIVE_FAILURE, 
		FRAMING_ERROR, 
		PARITY_ERROR, 
		BREAK_ERROR, 
		OVERRUN_ERROR, 
		TRANSMIT_ERROR, 
		PORT_BUSY, 
		CTS_QUERY_ERROR, 
		DSR_QUERY_ERROR, 
		RING_QUERY_ERROR
	}
	
	public enum FlowControl { NONE, RTSCTS, XONXOFF }
	
	public enum Parity { NONE, ODD, EVEN, MARK, SPACE }
	
	public enum StopBits { STOPBITS_1, STOPBITS_1_5, STOPBITS_2 }
	
	public enum DataBits { DATABITS_5, DATABITS_6, DATABITS_7, DATABITS_8 }
	
	public enum BaudRate { 
		BAUDRATE_110, 
		BAUDRATE_300, 
		BAUDRATE_600, 
		BAUDRATE_1200, 
		BAUDRATE_2400, 
		BAUDRATE_4800, 
		BAUDRATE_9600, 
		BAUDRATE_14400, 
		BAUDRATE_19200, 
		BAUDRATE_38400, 
		BAUDRATE_57600, 
		BAUDRATE_115200, 
		BAUDRATE_128000, 
		BAUDRATE_256000 
	}

	private static final Preferences userPrefs = Preferences.userRoot().node(AbstractTeletypeController.class.getName());
	private static final Logger LOG = Logger.getLogger(AbstractTeletypeController.class.getName());
	private static String className;
	
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	protected boolean reportFramingErrors;
	protected boolean reportConfigurationErrors;
	protected boolean reportBufferOverrunErrors;
	protected boolean reportParityMismatchErrors;
	protected boolean reportDTRNotSetErrors;
	protected boolean reportRTSNotSetErrors;
	protected boolean reportEventMaskErrors;
	protected boolean reportFlowControlErrors;
	protected boolean reportPurgeFailures;
	protected boolean reportBreakInterrupts;
	protected boolean reportTransmitFailures;
	protected boolean reportReceiveFailures;

	private boolean reportTransmitAdvice;
	private boolean reportReceiveAdvice;

	protected boolean logSerialPortErrors;

	protected Parity parity;
	protected StopBits stopBits;
	protected DataBits dataBits;
	protected BaudRate baudRate;
	protected FlowControl flowControlIn;
	protected FlowControl flowControlOut;

	private Parity defaultParity = Parity.NONE;
	private StopBits defaultStopBits = StopBits.STOPBITS_1;
	private DataBits defaultDataBits = DataBits.DATABITS_8;
	private BaudRate defaultBaudRate = BaudRate.BAUDRATE_4800;
	private FlowControl defaultFlowControlIn = FlowControl.NONE;
	private FlowControl defaultFlowControlOut = FlowControl.NONE;
	
	private boolean deviceAssignedParametersFixed;
	private boolean deviceAssignedBaudRateFixed;

	private String portName;
	
	private BaudRate[] supportedBaudRates = BaudRate.values();
	
	private boolean eventRXCHAR;
	private boolean eventRXFLAG;
	private boolean eventTXEMPTY;
	private boolean eventCTS;
	private boolean eventDSR;
	private boolean eventRLSD;
	private boolean eventERR;
	private boolean eventRING;
	private boolean eventBREAK;
	
	private final String deviceId;

	protected AbstractTeletypeController(String deviceId) {
		this.deviceId = deviceId;
		
		configureLogger();
		
		loadPreferences(deviceId);
	}
	
	public void clearAllPreferences() {
		try {
			AbstractTeletypeController.userPrefs.clear();
		} catch (final BackingStoreException ex) {
			LOG.log(Level.WARNING, ex.getMessage());
		}
	}

	public PropertyChangeSupport getPropertyChangeSupport() {
		return pcs;
	}

	public static BidiMap<String, String> getCatalogMap() {
		final BidiMap<String, String> catalog = new DualHashBidiMap<>();
		catalog.put("tty.JsscCommPort", "JSSC TTY Port v2.9.5");
		return catalog;
	}
	
	public int getCatalogMapIndex() {
		int r = 0;
		for (int i = 0; i < getCatalogMap().size(); i++) {
			if (getCatalogMap().keySet().toArray()[i].equals(getClassName())) {
				r = i;
				break;
			}
		}
		return r;
	}
	
	public String getClassName() {
		return className;
	}
	
	public void savePreferences() {
		savePreferences(deviceId);
	}
	
	public void setInvalidPortName(String string) {
		pcs.firePropertyChange(TTYEvents.INVALID_COMM_PORT.name(), null, string);
	}
	
	public FlowControl getFlowControlIn() {
		return flowControlIn;
	}
	
	public FlowControl getFlowControlOut() {
		return flowControlOut;
	}

	public Parity getParity() {
		return parity;
	}

	public StopBits getStopBits() {
		return stopBits;
	}

	public DataBits getDataBits() {
		return dataBits;
	}

	public BaudRate getBaudRate() {
		return baudRate;
	}

	public String getPortName() {
		return portName;
	}

	public Parity getDefaultParity() {
		return defaultParity;
	}

	public void setDefaultParity(Parity defaultParity) {
		this.defaultParity = defaultParity;
	}

	public StopBits getDefaultStopBits() {
		return defaultStopBits;
	}

	public void setDefaultStopBits(StopBits defaultStopBits) {
		this.defaultStopBits = defaultStopBits;
	}

	public DataBits getDefaultDataBits() {
		return defaultDataBits;
	}

	public void setDefaultDataBits(DataBits defaultDataBits) {
		this.defaultDataBits = defaultDataBits;
	}

	public BaudRate getDefaultBaudRate() {
		return defaultBaudRate;
	}

	public void setDefaultBaudRate(BaudRate defaultBaudRate) {
		this.defaultBaudRate = defaultBaudRate;
	}

	public FlowControl getDefaultFlowControlIn() {
		return defaultFlowControlIn;
	}

	public void setDefaultFlowControlIn(FlowControl defaultFlowControlIn) {
		this.defaultFlowControlIn = defaultFlowControlIn;
	}

	public FlowControl getDefaultFlowControlOut() {
		return defaultFlowControlOut;
	}

	public void setDefaultFlowControlOut(FlowControl defaultFlowControlOut) {
		this.defaultFlowControlOut = defaultFlowControlOut;
	}

	public BaudRate[] getSupportedBaudRates() {
		return supportedBaudRates.clone();
	}

	public void setSupportedBaudRates(BaudRate[] supportedBaudRates) {
		this.supportedBaudRates = supportedBaudRates.clone();
	}

	public void advisePortClosing() {
		pcs.firePropertyChange(TTYEvents.ADVISE_PORT_CLOSING.name(), null, null);
	}
	
	public void reportErrorClosing(Exception ex) {
		pcs.firePropertyChange(TTYEvents.ERROR.name(), null, ex);
	}
	
	public void reportPortBusy(String portName) {
		pcs.firePropertyChange(TTYEvents.PORT_BUSY.name(), null, portName);
	}
	
	public void breakEvent() {
		pcs.firePropertyChange(TTYEvents.BREAK.name(), null, null);
	}

	public void rlsdEvent() {
		pcs.firePropertyChange(TTYEvents.RLSD.name(), null, null);
	}

	public void ctsEvent() {
		pcs.firePropertyChange(TTYEvents.CTS.name(), null, null);
	}

	public void dsrEvent() {
		pcs.firePropertyChange(TTYEvents.DSR.name(), null, null);
	}

	public void rxCharEvent() {
		pcs.firePropertyChange(TTYEvents.RX_CHAR.name(), null, null);
	}
	
	public void rxFlagEvent() {
		pcs.firePropertyChange(TTYEvents.RX_FLAG.name(), null, null);
	}
	
	public void txEmptyEvent() {
		pcs.firePropertyChange(TTYEvents.TX_EMPTY.name(), null, null);
	}

	public void ringEvent() {
		pcs.firePropertyChange(TTYEvents.RING.name(), null, null);
	}
	
	public void framingError() {
		pcs.firePropertyChange(TTYEvents.FRAMING_ERROR.name(), null, null);
	}
	
	public void parityError() {
		pcs.firePropertyChange(TTYEvents.PARITY_ERROR.name(), null, null);
	}
	
	public void overrunError() {
		pcs.firePropertyChange(TTYEvents.OVERRUN_ERROR.name(), null, null);
	}
	
	public void breakError() {
		pcs.firePropertyChange(TTYEvents.BREAK_ERROR.name(), null, null);
	}
	
	public void transmitError() {
		pcs.firePropertyChange(TTYEvents.TRANSMIT_ERROR.name(), null, null);
	}
	
	public void reportDtrNotSetError(Exception ex) {
		if (isReportDTRNotSetErrors()) {
			pcs.firePropertyChange(TTYEvents.RTS_NOT_SET_ERROR.name(), null, ex);
		}
	}

	public void reportRlsdQueryError(Exception ex) {
		if (isReportDTRNotSetErrors()) {
			pcs.firePropertyChange(TTYEvents.RLSD_QUERY_ERROR.name(), null, ex);
		}
	}

	public void reportRingQueryError(Exception ex) {
		if (isReportDTRNotSetErrors()) {
			pcs.firePropertyChange(TTYEvents.RING_QUERY_ERROR.name(), null, ex);
		}
	}
	
	public void reportDsrQueryError(Exception ex) {
		if (isReportDTRNotSetErrors()) {
			pcs.firePropertyChange(TTYEvents.DSR_QUERY_ERROR.name(), null, ex);
		}
	}
	
	public void reportCtsQueryError(Exception ex) {
		if (isReportDTRNotSetErrors()) {
			pcs.firePropertyChange(TTYEvents.CTS_QUERY_ERROR.name(), null, ex);
		}
	}
	
	public void reportRtsNotSetError(Exception ex) {
		if (isReportRTSNotSetErrors()) {
			pcs.firePropertyChange(TTYEvents.DTR_NOT_SET_ERROR.name(), null, ex);
		}
	}

	public void reportPurgeFailure(Exception ex) {
		if (isReportPurgeFailures()) {
			pcs.firePropertyChange(TTYEvents.PURGE_FAILURE.name(), null, ex);
		}
	}

	public void reportConfigurationParameterError(Exception ex) {
		if (isReportConfigurationErrors()) {
			pcs.firePropertyChange(TTYEvents.TTY_PORT_CONFIGURATION_ERROR.name(), null, ex);
		}
	}

	public void reportFlowControlFailure(Exception ex) {
		if (isReportFlowControlErrors()) {
			pcs.firePropertyChange(TTYEvents.FLOW_CONTROL_ERROR.name(), null, ex);
		}
	}

	public void reportTransmitFailure(Exception ex) {
		if (isReportTransmitFailures()) {
			pcs.firePropertyChange(TTYEvents.TRANSMIT_FAILURE.name(), null, ex);
		}
	}

	public void reportTransmitAdvice(String data) {
		if (isReportTransmitAdvice()) {
			pcs.firePropertyChange(TTYEvents.TX_DATA.name(), null, data);
		}
	}

	public void reportReceiveAdvice(String data) {
		if (isReportReceiveAdvice()) {
			pcs.firePropertyChange(TTYEvents.RX_DATA.name(), null, data);
		}
	}

	public void reportReceiveFailure(Exception ex) {
		if (isReportReceiveFailures()) {
			pcs.firePropertyChange(TTYEvents.RECEIVE_FAILURE.name(), null, ex);
		}
	}

	public boolean isReportFramingErrors() {
		return reportFramingErrors;
	}

	public void setReportFramingErrors(boolean reportFramingErrors) {
		this.reportFramingErrors = reportFramingErrors;
	}

	public boolean isReportConfigurationErrors() {
		return reportConfigurationErrors;
	}

	public void setReportConfigurationErrors(boolean reportConfigurationErrors) {
		this.reportConfigurationErrors = reportConfigurationErrors;
	}

	public boolean isReportBufferOverrunErrors() {
		return reportBufferOverrunErrors;
	}

	public void setReportBufferOverrunErrors(boolean reportBufferOverrunErrors) {
		this.reportBufferOverrunErrors = reportBufferOverrunErrors;
	}

	public boolean isReportParityMismatchErrors() {
		return reportParityMismatchErrors;
	}

	public void setReportParityMismatchErrors(boolean reportParityMismatchErrors) {
		this.reportParityMismatchErrors = reportParityMismatchErrors;
	}

	public boolean isReportDTRNotSetErrors() {
		return reportDTRNotSetErrors;
	}

	public void setReportDTRNotSetErrors(boolean reportDTRNotSetErrors) {
		this.reportDTRNotSetErrors = reportDTRNotSetErrors;
	}

	public boolean isReportRTSNotSetErrors() {
		return reportRTSNotSetErrors;
	}

	public void setReportRTSNotSetErrors(boolean reportRTSNotSetErrors) {
		this.reportRTSNotSetErrors = reportRTSNotSetErrors;
	}

	public boolean isReportEventMaskErrors() {
		return reportEventMaskErrors;
	}

	public void setReportEventMaskErrors(boolean reportEventMaskErrors) {
		this.reportEventMaskErrors = reportEventMaskErrors;
	}

	public boolean isReportFlowControlErrors() {
		return reportFlowControlErrors;
	}

	public void setReportFlowControlErrors(boolean reportFlowControlErrors) {
		this.reportFlowControlErrors = reportFlowControlErrors;
	}

	public boolean isReportPurgeFailures() {
		return reportPurgeFailures;
	}

	public void setReportPurgeFailures(boolean reportPurgeFailures) {
		this.reportPurgeFailures = reportPurgeFailures;
	}

	public boolean isReportBreakInterrupts() {
		return reportBreakInterrupts;
	}

	public boolean isReportTransmitAdvice() {
		return reportTransmitAdvice;
	}

	public void setReportTransmitAdvice(boolean reportTransmitAdvice) {
		this.reportTransmitAdvice = reportTransmitAdvice;
	}

	public boolean isLogSerialPortErrors() {
		return logSerialPortErrors;
	}

	public void setLogSerialPortErrors(boolean logSerialPortErrors) {
		this.logSerialPortErrors = logSerialPortErrors;
	}

	public void setReportBreakInterrupts(boolean reportBreakInterrupts) {
		this.reportBreakInterrupts = reportBreakInterrupts;
	}

	public boolean isReportTransmitFailures() {
		return reportTransmitFailures;
	}

	public void setReportTransmitFailures(boolean reportTransmitFailures) {
		this.reportTransmitFailures = reportTransmitFailures;
	}

	public boolean isReportReceiveFailures() {
		return reportReceiveFailures;
	}

	public void setReportReceiveFailures(boolean reportReceiveFailures) {
		this.reportReceiveFailures = reportReceiveFailures;
	}

	public boolean isReportReceiveAdvice() {
		return reportReceiveAdvice;
	}

	public void setReportReceiveAdvice(boolean reportReceiveAdvice) {
		this.reportReceiveAdvice = reportReceiveAdvice;
	}

	public boolean isEventRXCHAR() {
		return eventRXCHAR;
	}

	public void setEventRXCHAR(boolean eventRXCHAR) {
		this.eventRXCHAR = eventRXCHAR;
	}

	public boolean isEventRXFLAG() {
		return eventRXFLAG;
	}

	public void setEventRXFLAG(boolean eventRXFLAG) {
		this.eventRXFLAG = eventRXFLAG;
	}

	public boolean isEventTXEMPTY() {
		return eventTXEMPTY;
	}

	public void setEventTXEMPTY(boolean eventTXEMPTY) {
		this.eventTXEMPTY = eventTXEMPTY;
	}

	public boolean isEventCTS() {
		return eventCTS;
	}

	public void setEventCTS(boolean eventCTS) {
		this.eventCTS = eventCTS;
	}

	public boolean isEventDSR() {
		return eventDSR;
	}

	public void setEventDSR(boolean eventDSR) {
		this.eventDSR = eventDSR;
	}

	public boolean isEventRLSD() {
		return eventRLSD;
	}

	public void setEventRLSD(boolean eventRLSD) {
		this.eventRLSD = eventRLSD;
	}

	public boolean isEventERR() {
		return eventERR;
	}

	public void setEventERR(boolean eventERR) {
		this.eventERR = eventERR;
	}

	public boolean isEventRING() {
		return eventRING;
	}

	public void setEventRING(boolean eventRING) {
		this.eventRING = eventRING;
	}

	public boolean isEventBREAK() {
		return eventBREAK;
	}

	public void setEventBREAK(boolean eventBREAK) {
		this.eventBREAK = eventBREAK;
	}

	public boolean isDeviceAssignedParametersFixed() {
		return deviceAssignedParametersFixed;
	}

	public void setDeviceAssignedParametersFixed(boolean deviceAssignedParametersFixed) {
		this.deviceAssignedParametersFixed = deviceAssignedParametersFixed;
	}

	public boolean isDeviceAssignedBaudRateFixed() {
		return deviceAssignedBaudRateFixed;
	}

	public void setDeviceAssignedBaudRateFixed(boolean deviceAssignedBaudRateFixed) {
		this.deviceAssignedBaudRateFixed = deviceAssignedBaudRateFixed;
	}

	public static int getIntegerFromBaudRate(BaudRate baudRate) {
		return switch (baudRate) {
			case BAUDRATE_110 -> 110;
			case BAUDRATE_300 -> 300;
			case BAUDRATE_600 -> 600;
			case BAUDRATE_1200 -> 1200;
			case BAUDRATE_2400 -> 2400;
			case BAUDRATE_4800 -> 4800;
			case BAUDRATE_9600 -> 9600;
			case BAUDRATE_14400 -> 14400;
			case BAUDRATE_19200 -> 19200;
			case BAUDRATE_38400 -> 38400;
			case BAUDRATE_57600 -> 57600;
			case BAUDRATE_115200 -> 115200;
			case BAUDRATE_128000 -> 128000;
			case BAUDRATE_256000 -> 256000;
			default -> 4800;
		};
	}
	
	public static BaudRate getBaudRateFromInteger(int i) {
		return switch (i) {
			case 110 -> BaudRate.BAUDRATE_110;
			case 300 -> BaudRate.BAUDRATE_300;
			case 600 -> BaudRate.BAUDRATE_600;
			case 1200 -> BaudRate.BAUDRATE_1200;
			case 2400 -> BaudRate.BAUDRATE_2400;
			case 4800 -> BaudRate.BAUDRATE_4800;
			case 9600 -> BaudRate.BAUDRATE_9600;
			case 14400 -> BaudRate.BAUDRATE_14400;
			case 19200 -> BaudRate.BAUDRATE_19200;
			case 38400 -> BaudRate.BAUDRATE_38400;
			case 57600 -> BaudRate.BAUDRATE_57600;
			case 115200 -> BaudRate.BAUDRATE_115200;
			case 128000 -> BaudRate.BAUDRATE_128000;
			case 256000 -> BaudRate.BAUDRATE_256000;
			default -> BaudRate.BAUDRATE_4800;
		};
	}
	
	public static BaudRate[] getBaudRateArrayFromIntegerArray(Integer[] ia) {
		final BaudRate[] ba = new BaudRate[ia.length];
		for (int i = 0; i < ia.length; i++) {
			ba[i] = getBaudRateFromInteger(ia[i]);
		}
		return ba;
	}
	
	public void savePreferences(String deviceId) {
		userPrefs.put(deviceId + "portName", portName);
		userPrefs.putInt(deviceId + "baudRate", baudRate.ordinal());
		userPrefs.putInt(deviceId + "dataBits", dataBits.ordinal());
		userPrefs.putInt(deviceId + "stopBits", stopBits.ordinal());
		userPrefs.putInt(deviceId + "parity", parity.ordinal());
		userPrefs.putInt(deviceId + "flowControlIn", flowControlIn.ordinal());
		userPrefs.putInt(deviceId + "flowControlOut", flowControlOut.ordinal());

		userPrefs.putBoolean(deviceId + "eventBREAK", eventBREAK);
		userPrefs.putBoolean(deviceId + "eventCTS", eventCTS);
		userPrefs.putBoolean(deviceId + "eventDSR", eventDSR);
		userPrefs.putBoolean(deviceId + "eventERR", eventERR);
		userPrefs.putBoolean(deviceId + "eventRXCHAR", eventRXCHAR);
		userPrefs.putBoolean(deviceId + "eventRING", eventRING);
		userPrefs.putBoolean(deviceId + "eventRLSD", eventRLSD);
		userPrefs.putBoolean(deviceId + "eventRXFLAG", eventRXFLAG);
		userPrefs.putBoolean(deviceId + "eventTXEMPTY", eventTXEMPTY);

		userPrefs.putBoolean(deviceId + "reportConfigurationErrors", reportConfigurationErrors);
		userPrefs.putBoolean(deviceId + "reportRTSErrors", reportRTSNotSetErrors);
		userPrefs.putBoolean(deviceId + "reportDTRErrors", reportDTRNotSetErrors);
		userPrefs.putBoolean(deviceId + "reportFramingErrors", reportFramingErrors);
		userPrefs.putBoolean(deviceId + "reportBufferOverrunErrors", reportBufferOverrunErrors);
		userPrefs.putBoolean(deviceId + "reportParityMismatchErrors", reportParityMismatchErrors);
		userPrefs.putBoolean(deviceId + "reportBreakInterrupts", reportBreakInterrupts);
		userPrefs.putBoolean(deviceId + "reportEventMaskErrors", reportEventMaskErrors);
		userPrefs.putBoolean(deviceId + "reportFlowControlErrors", reportFlowControlErrors);
		userPrefs.putBoolean(deviceId + "reportPurgeFailures", reportPurgeFailures);
		userPrefs.putBoolean(deviceId + "reportTransmitFailures", reportTransmitFailures);

		userPrefs.putBoolean(deviceId + "logSerialPortErrors", logSerialPortErrors);
	}
	
	public boolean setPortName(final String portName) {
		if (Utility.isComPortValid(portName)) {
			this.portName = portName;
			return true;
		} else {
			LOG.log(Level.SEVERE, "Invalid Port Name");
			return false;
		}
	}

	public static String getSystemDefaultPortName() {
		final String[] pnStr = SerialPortList.getPortNames();
		String pn = "";
		if (pnStr.length > 0) {
			pn = pnStr[0];
		}
		return pn;
	}

	private void loadPreferences(String deviceID) {
		portName = userPrefs.get(deviceID + "portName", getSystemDefaultPortName());

		baudRate = BaudRate.values()[(userPrefs.getInt(deviceID + "baduRate", defaultBaudRate.ordinal()))];
		dataBits = DataBits.values()[(userPrefs.getInt(deviceID + "dataBits", defaultDataBits.ordinal()))];
		stopBits = StopBits.values()[(userPrefs.getInt(deviceID + "stopBits", defaultStopBits.ordinal()))];
		parity = Parity.values()[(userPrefs.getInt(deviceID + "parity", defaultParity.ordinal()))];
		
		flowControlIn = FlowControl.values()[(userPrefs.getInt(deviceID + "flowControlIn", defaultFlowControlIn.ordinal()))];
		flowControlOut = FlowControl.values()[(userPrefs.getInt(deviceID + "flowControlOut", defaultFlowControlOut.ordinal()))];

		eventRXCHAR = userPrefs.getBoolean(deviceID + "eventRXCHAR", true);
		eventRXFLAG = userPrefs.getBoolean(deviceID + "eventRXFLAG", true);
		eventTXEMPTY = userPrefs.getBoolean(deviceID + "eventTXEMPTY", true);
		eventCTS = userPrefs.getBoolean(deviceID + "eventCTS", true);
		eventDSR = userPrefs.getBoolean(deviceID + "eventDSR", true);
		eventRLSD = userPrefs.getBoolean(deviceID + "eventRLSD", true);
		eventERR = userPrefs.getBoolean(deviceID + "eventERR", true);
		eventRING = userPrefs.getBoolean(deviceID + "eventRING", true);
		eventBREAK = userPrefs.getBoolean(deviceID + "eventBREAK", true);

		reportConfigurationErrors = userPrefs.getBoolean(deviceID + "reportConfigurationErrors", true);
		reportRTSNotSetErrors = userPrefs.getBoolean(deviceID + "reportRTSErrors", true);
		reportDTRNotSetErrors = userPrefs.getBoolean(deviceID + "reportDTRErrors", true);
		reportFramingErrors = userPrefs.getBoolean(deviceID + "reportFramingErrors", false);
		reportBufferOverrunErrors = userPrefs.getBoolean(deviceID + "reportBufferOverrunErrors", true);
		reportParityMismatchErrors = userPrefs.getBoolean(deviceID + "reportParityMismatchErrors", true);
		reportBreakInterrupts = userPrefs.getBoolean(deviceID + "reportBreakInterrupts", true);
		reportEventMaskErrors = userPrefs.getBoolean(deviceID + "reportEventMaskErrors", true);
		reportFlowControlErrors = userPrefs.getBoolean(deviceID + "reportFlowControlErrors", true);
		reportPurgeFailures = userPrefs.getBoolean(deviceID + "reportPurgeFailures", true);
		reportTransmitFailures = userPrefs.getBoolean(deviceID + "reportTransmitFailures", true);

		logSerialPortErrors = userPrefs.getBoolean(deviceID + "logSerialPortErrors", true);
	}
	
	public static synchronized AbstractTeletypeController getTTyPortInstance(String className, Boolean clearAllPreferences) {
		boolean isValidClassName = false;
		final Iterator<String> iterator = AbstractTeletypeController.getCatalogMap().keySet().iterator();
		while (iterator.hasNext()) {
			final String cn = iterator.next();
			if (cn.equals(className)) {
				isValidClassName = true;
				break;
			}
		}
		final Class<?> classTemp;
		AbstractTeletypeController instance = null;
		try {
			if (!isValidClassName) {
				className = (String) AbstractTeletypeController.getCatalogMap().keySet().toArray()[0];
			}
			classTemp = Class.forName(className);
			final Class<?>[] cArg = new Class<?>[1];
			cArg[0] = Boolean.class;
			instance = (AbstractTeletypeController) classTemp.getDeclaredConstructor(cArg).newInstance(clearAllPreferences);
		} catch (InstantiationException e) {
			LOG.log(Level.WARNING, "InstantiationException", e);
		} catch (IllegalAccessException e) {
			LOG.log(Level.WARNING, "IllegalAccessException", e);
		} catch (IllegalArgumentException e) {
			LOG.log(Level.WARNING, "IllegalArgumentException", e);
		} catch (InvocationTargetException e) {
			LOG.log(Level.WARNING, "InvocationTargetException", e);
		} catch (NoSuchMethodException e) {
			LOG.log(Level.WARNING, "NoSuchMethodException", e);
		} catch (SecurityException e) {
			LOG.log(Level.WARNING, "SecurityException", e);
		} catch (ClassNotFoundException e) {
			LOG.log(Level.WARNING, "ClassNotFoundException", e);
		}
		return instance;
	}

	@Override
	public void close() {
		for (Handler handler : LOG.getHandlers()) {
			LOG.removeHandler(handler);
	        handler.close();
		}
	}
	
	private void configureLogger() {
		Handler fh = null;
		Handler ch = null;
		try {
			fh = new FileHandler("%t/AbstractTeletypeController.log");
			ch = new ConsoleHandler();
			LOG.addHandler(fh);
			LOG.setLevel(Level.FINEST);
			LOG.addHandler(ch);
			LOG.setUseParentHandlers(false);
		} catch (SecurityException | IOException e) {
			LOG.log(Level.WARNING, e.getMessage());
		}
	}


	public static String serialPortErrorMessage(final int eventMask) {
		return switch (eventMask) {
			case 1 -> "Parameter Configuration Error";
			case 2 -> "Buffer Overrun Error";
			case 4 -> "Parity Mismatch Error";
			case 8 -> "Framing Error";
			case 16 -> "Data Terminal Ready (DTR) Line Not Set As Requested";
			case 32 -> "Ready To Send (RTS) Line Not Set As Requested";
			case 64 -> "Event Mask Not Set As Requested";
			case 128 -> "Flow Control Not Set As Requested";
			case 256 -> "Port Not Purged";
			case 512 -> "Break Interrupt";
			case 1024 -> "Transmit Error";
			case 2048 -> "Framing Error";
			case 4096 -> "Buffer Overrun Error";
			case 8192 -> "Parity Mismatch Error";
			default -> "Unspecified Serial Communications Error";
		};
	}
	
	public abstract String getLibraryName();

	public abstract String getLibraryRevision();
	
	public abstract byte[] readBytes();

	public abstract byte[] readBytes(int byteCount);

	public abstract String readString();

	public abstract String readString(int byteCount);

	public abstract int[] readIntArray();

	public abstract boolean isCTS();

	public abstract boolean isDSR();

	public abstract boolean isRING();

	public abstract boolean isRLSD();
	
	public abstract void setBaudRate(BaudRate baudRate);

	public abstract void setDataBits(DataBits dataBits);

	public abstract void setDTR(boolean dtr);

	public abstract void setRTS(boolean rts);

	public abstract void setStopBits(StopBits stopBits);

	public abstract void setParity(Parity parity);

	public abstract void setFlowControlIn(FlowControl flowControlIn);
	
	public abstract void setFlowControlOut(FlowControl flowControlOut);
	
	public abstract void setParameters(BaudRate baudRate, DataBits dataBits, StopBits stopBits, Parity parity);

	public abstract void purgeTxClear();

	public abstract void purgeTxAbort();

	public abstract void purgeRxClear();

	public abstract void purgeRxAbort();

	public abstract void openPort(String portName);

	public abstract void openPort();
	
	public abstract void closePort();

	public abstract boolean isPortOpen();
	
	public abstract boolean write(Object object);

	public abstract boolean writeString(String string, Charset charset);

}
