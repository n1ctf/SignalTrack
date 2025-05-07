package tty;

import java.io.UnsupportedEncodingException;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import java.util.Arrays;

import java.util.logging.Level;
import java.util.logging.Logger;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;

public class JsscCommPort extends AbstractTeletypeController {
	private static final long serialVersionUID = 1L;
	
	public static final String LIBRARY_NAME = "JSSC";
	public static final String LIBRARY_REVISION = "v2.9.5";
	public static final int PARAMETER_ERROR = 1;
	public static final int DTR_NOT_SET = 16;
	public static final int RTS_NOT_SET = 32;
	public static final int EVENT_MASK_ERROR = 64;
	public static final int FLOW_CONTROL_ERROR = 128;
	public static final int PORT_NOT_PURGED = 256;
	public static final int ERROR_OVERRUN = 2;
	public static final int ERROR_PARITY = 4;
	public static final int ERROR_FRAME = 8;
	public static final int INTERRUPT_BREAK = 512;
	public static final int INTERRUPT_TX = 1024;
	public static final int INTERRUPT_FRAME = 2048;
	public static final int INTERRUPT_OVERRUN = 4096;
	public static final int INTERRUPT_PARITY = 8192;

	public static final boolean enableEventRXCHAR = true;
	public static final boolean enableEventRXFLAG = true;
	public static final boolean enableEventTXEMPTY = true;
	public static final boolean enableEventCTS = true;
	public static final boolean enableEventDSR = true;
	public static final boolean enableEventRLSD = true;
	public static final boolean enableEventERR = true;
	public static final boolean enableEventRING = true;
	public static final boolean enableEventBREAK = true;
	
	private static final Logger LOG = Logger.getLogger(JsscCommPort.class.getName());
	
	private boolean logSerialPortExceptions;
	private int eventMask;
	private boolean enableEvents;

	private SerialPort serialPort;
	

	public JsscCommPort(Boolean clearAllPreferences) {
		super(String.valueOf(serialVersionUID));
		if (Boolean.TRUE.equals(clearAllPreferences)) {
			super.clearAllPreferences();
		}
		enableAllEvents();
	}

	private void enableAllEvents() {
		setEnableBreakEvent(enableEventBREAK);
		setEnableErrorEvent(enableEventERR);
		setEnableTxEmptyEvent(enableEventTXEMPTY);
		setEnableCTSEvent(enableEventCTS);
		setEnableDSREvent(enableEventDSR);
		setEnableRingEvent(enableEventRING);
		setEnableRLSDEvent(enableEventRLSD);
		setEnableRxFlagEvent(enableEventRXFLAG);
		setEnableRxCharEvent(enableEventRXCHAR);
	}

	private int getFlowControlMask() {
		int flow = 0;
		
		if (flowControlIn == FlowControl.NONE) {
			flow += SerialPort.FLOWCONTROL_NONE;
		} else if (flowControlIn == FlowControl.RTSCTS) {
			flow += SerialPort.FLOWCONTROL_RTSCTS_IN;
		} else if (flowControlIn == FlowControl.XONXOFF) {
			flow += SerialPort.FLOWCONTROL_XONXOFF_IN;
		} 
		
		if (flowControlOut == FlowControl.NONE) {
			flow += SerialPort.FLOWCONTROL_NONE;
		} else if (flowControlOut == FlowControl.RTSCTS) {
			flow += SerialPort.FLOWCONTROL_RTSCTS_OUT;
		} else if (flowControlOut == FlowControl.XONXOFF) {
			flow += SerialPort.FLOWCONTROL_XONXOFF_OUT;
		} 	
		
		return flow;
	}
	
	private void setEventMask(int eventMask) {
		try {
			if ((serialPort != null) && serialPort.isOpened()) {
				serialPort.setEventsMask(eventMask);
			}
		} catch (final SerialPortException ex) {
			if (logSerialPortExceptions) {
				LOG.log(Level.WARNING, ex.getMessage());
			}
			reportConfigurationParameterError(ex);
		}
	}
	
	private boolean purgeAll() {
        try {
            return serialPort.purgePort(SerialPort.PURGE_RXABORT + SerialPort.PURGE_RXCLEAR + SerialPort.PURGE_TXABORT
                    + SerialPort.PURGE_TXCLEAR);
        } catch (final SerialPortException ex) {
            if (logSerialPortExceptions) {
                LOG.log(Level.WARNING, ex.getMessage());
            }
            reportPurgeFailure(ex);
            return false;
        }
    }

	@Override
	public void purgeRxAbort() {
		try {
			serialPort.purgePort(SerialPort.PURGE_RXABORT);
		} catch (final SerialPortException ex) {
			if (logSerialPortExceptions) {
				LOG.log(Level.WARNING, ex.getMessage());
			} else {
				reportPurgeFailure(ex);
			}
		}
	}

	@Override
	public void purgeRxClear() {
		try {
			serialPort.purgePort(SerialPort.PURGE_RXCLEAR);
		} catch (final SerialPortException ex) {
			if (logSerialPortExceptions) {
				LOG.log(Level.WARNING, ex.getMessage());
			}
			reportPurgeFailure(ex);
		}
	}

	@Override
	public void purgeTxAbort() {
		try {
			serialPort.purgePort(SerialPort.PURGE_TXABORT);
		} catch (final SerialPortException ex) {
			if (logSerialPortExceptions) {
				LOG.log(Level.WARNING, ex.getMessage());
			}
			reportPurgeFailure(ex);
		}
	}

	@Override
	public void purgeTxClear() {
		try {
			serialPort.purgePort(SerialPort.PURGE_TXCLEAR);
		} catch (final SerialPortException ex) {
			if (logSerialPortExceptions) {
				LOG.log(Level.WARNING, ex.getMessage());
			}
			reportPurgeFailure(ex);
		}
	}

	private void setEnableTxEmptyEvent(boolean eventTXEMPTY) {
		if (eventTXEMPTY) {
			eventMask |= SerialPort.MASK_TXEMPTY;
		} else {
			eventMask &= ~SerialPort.MASK_TXEMPTY;
		}
		setEventMask(eventMask);
	}

	private void setEnableRxFlagEvent(boolean eventRXFLAG) {
		if (eventRXFLAG) {
			eventMask |= SerialPort.MASK_RXFLAG;
		} else {
			eventMask &= ~SerialPort.MASK_RXFLAG;
		}
		setEventMask(eventMask);
	}

	private void setEnableCTSEvent(boolean eventCTS) {
		if (eventCTS) {
			eventMask |= SerialPort.MASK_CTS;
		} else {
			eventMask &= ~SerialPort.MASK_CTS;
		}
		setEventMask(eventMask);
	}

	private void setEnableDSREvent(boolean eventDSR) {
		if (eventDSR) {
			eventMask |= SerialPort.MASK_DSR;
		} else {
			eventMask &= ~SerialPort.MASK_DSR;
		}
		setEventMask(eventMask);
	}

	private void setEnableRLSDEvent(boolean eventRLSD) {
		if (eventRLSD) {
			eventMask |= SerialPort.MASK_RLSD;
		} else {
			eventMask &= ~SerialPort.MASK_RLSD;
		}
		setEventMask(eventMask);
	}

	private void setEnableErrorEvent(boolean eventERR) {
		if (eventERR) {
			eventMask |= SerialPort.MASK_ERR;
		} else {
			eventMask &= ~SerialPort.MASK_ERR;
		}
		setEventMask(eventMask);
	}

	private void setEnableRingEvent(boolean eventRING) {
		if (eventRING) {
			eventMask |= SerialPort.MASK_RING;
		} else {
			eventMask &= ~SerialPort.MASK_RING;
		}
		setEventMask(eventMask);
	}

	private void setEnableBreakEvent(boolean eventBREAK) {
		if (eventBREAK) {
			eventMask |= SerialPort.MASK_BREAK;
		} else {
			eventMask &= ~SerialPort.MASK_BREAK;
		}
		setEventMask(eventMask);
	}

	private void setEnableRxCharEvent(boolean eventRXCHAR) {
		if (eventRXCHAR) {
			eventMask |= SerialPort.MASK_RXCHAR;
		} else {
			eventMask &= ~SerialPort.MASK_RXCHAR;
		}
		setEventMask(eventMask);
	}

	@Override
	public boolean isRLSD() {
		try {
			return serialPort.isRLSD();
		} catch (final SerialPortException ex) {
			if (logSerialPortExceptions) {
				LOG.log(Level.WARNING, ex.getMessage());
			}
			reportRlsdQueryError(ex);
		}
		return false;
	}

	@Override
	public boolean isDSR() {
		try {
			return serialPort.isDSR();
		} catch (final SerialPortException ex) {
			if (logSerialPortExceptions) {
				LOG.log(Level.WARNING, ex.getMessage());
			}
			reportDsrQueryError(ex);
		}
		return false;
	}
	
	@Override
	public boolean isRING() {
		try {
			return serialPort.isRING();
		} catch (final SerialPortException ex) {
			if (logSerialPortExceptions) {
				LOG.log(Level.WARNING, ex.getMessage());
			}
			reportRingQueryError(ex);
		}
		return false;
	}
	
	@Override
	public boolean isCTS() {
		try {
			return serialPort.isCTS();
		} catch (final SerialPortException ex) {
			if (logSerialPortExceptions) {
				LOG.log(Level.WARNING, ex.getMessage());
			}
			reportCtsQueryError(ex);
		}
		return false;
	}
	
	@Override
	public void setDTR(boolean dtr) {
		try {
			if ((serialPort != null) && serialPort.isOpened() && 
					(serialPort.getFlowControlMode() == SerialPort.FLOWCONTROL_RTSCTS_IN ||
					serialPort.getFlowControlMode() == SerialPort.FLOWCONTROL_RTSCTS_OUT)) {
				serialPort.setDTR(dtr);
			} else {
				reportDtrNotSetError(null);
			}
		} catch (final SerialPortException ex) {
			if (logSerialPortExceptions) {
				LOG.log(Level.WARNING, ex.getMessage());
			}
			reportDtrNotSetError(ex);
		}
	}

	@Override
	public void setRTS(boolean rts) {
		try {
			if ((serialPort != null) && serialPort.isOpened() && 
			(serialPort.getFlowControlMode() == SerialPort.FLOWCONTROL_RTSCTS_IN ||
			serialPort.getFlowControlMode() == SerialPort.FLOWCONTROL_RTSCTS_OUT)) {
				serialPort.setRTS(rts);
			} else {
				reportRtsNotSetError(null);
			}
		} catch (final SerialPortException ex) {
			if (logSerialPortExceptions) {
				LOG.log(Level.WARNING, ex.getMessage());
			}
			reportRtsNotSetError(ex);
		}
	}

	@Override
	public void setFlowControlIn(FlowControl flowControlIn) {
		try {
			this.flowControlIn = flowControlIn;
			if ((serialPort != null) && serialPort.isOpened()) {
				serialPort.setFlowControlMode(getFlowControlMask());
			}
		} catch (final SerialPortException ex) {
			if (logSerialPortExceptions) {
				LOG.log(Level.WARNING, ex.getMessage());
			}
			reportFlowControlFailure(ex);
		}
	}

	@Override
	public void setFlowControlOut(FlowControl flowControlOut) {
		try {
			this.flowControlOut = flowControlOut;
			if ((serialPort != null) && serialPort.isOpened()) {
				serialPort.setFlowControlMode(getFlowControlMask());
			}
		} catch (final SerialPortException ex) {
			if (logSerialPortExceptions) {
				LOG.log(Level.WARNING, ex.getMessage());
			}
			reportFlowControlFailure(ex);
		}
	}
	
	@Override
	public void setParity(Parity parity) {
		this.parity = parity;
		applyPortParameters();
	}

	@Override
	public void setStopBits(StopBits stopBits) {
		this.stopBits = stopBits;
		applyPortParameters();
	}

	@Override
	public void setDataBits(DataBits dataBits) {
		this.dataBits = dataBits;
		applyPortParameters();
	}

	@Override
	public void setBaudRate(BaudRate baudRate) {
		this.baudRate = baudRate;
		applyPortParameters();
	}

	@Override
	public void setParameters(BaudRate baudRate, DataBits dataBits, StopBits stopBits, Parity parity) {
		this.baudRate = baudRate;
		this.dataBits = dataBits;
		this.stopBits = stopBits;
		this.parity = parity;
		applyPortParameters();
	}

	private void applyPortParameters() {
		try {
			if ((serialPort != null) && serialPort.isOpened()) {
				serialPort.setParams(translateBaudRate(baudRate), translateDataBits(dataBits), translateStopBits(stopBits), translateParity(parity));
				if (serialPort.isOpened()) {
					close();
					openPort();
				}
			}
		} catch (final SerialPortException ex) {
			if (logSerialPortExceptions) {
				LOG.log(Level.WARNING, ex.getMessage());
			}
			reportConfigurationParameterError(ex);
		}
	}

	private int translateBaudRate(BaudRate baudRate) {
		return switch (baudRate) {
			case BAUDRATE_110 -> SerialPort.BAUDRATE_110;
			case BAUDRATE_300 -> SerialPort.BAUDRATE_300;
			case BAUDRATE_600 -> SerialPort.BAUDRATE_600;
			case BAUDRATE_1200 -> SerialPort.BAUDRATE_1200;
			case BAUDRATE_2400 -> SerialPort.BAUDRATE_2400;
			case BAUDRATE_4800 -> SerialPort.BAUDRATE_4800;
			case BAUDRATE_9600 -> SerialPort.BAUDRATE_9600;
			case BAUDRATE_14400 -> SerialPort.BAUDRATE_14400;
			case BAUDRATE_19200 -> SerialPort.BAUDRATE_19200;
			case BAUDRATE_38400 -> SerialPort.BAUDRATE_38400;
			case BAUDRATE_57600 -> SerialPort.BAUDRATE_57600;
			case BAUDRATE_115200 -> SerialPort.BAUDRATE_115200;
			case BAUDRATE_128000 -> SerialPort.BAUDRATE_128000;
			case BAUDRATE_256000 -> SerialPort.BAUDRATE_256000;
			default -> SerialPort.BAUDRATE_4800;
		};
	}
	
	private int translateDataBits(DataBits dataBits) {
		return switch (dataBits) {
			case DATABITS_5 -> SerialPort.DATABITS_5;
			case DATABITS_6 -> SerialPort.DATABITS_6;
			case DATABITS_7 -> SerialPort.DATABITS_7;
			case DATABITS_8 -> SerialPort.DATABITS_8;
			default -> SerialPort.DATABITS_8;
		};
	}
	
	private int translateStopBits(StopBits stopBits) {
		return switch (stopBits) {
			case STOPBITS_1 -> SerialPort.STOPBITS_1;
			case STOPBITS_1_5 -> SerialPort.STOPBITS_1_5;
			case STOPBITS_2 -> SerialPort.STOPBITS_2;
			default -> SerialPort.STOPBITS_1;
		};
	}
	
	private int translateParity(Parity parity) {
		return switch (parity) {
			case NONE -> SerialPort.PARITY_NONE;
			case ODD -> SerialPort.PARITY_ODD;
			case EVEN -> SerialPort.PARITY_EVEN;
			case MARK -> SerialPort.PARITY_MARK;
			case SPACE -> SerialPort.PARITY_SPACE;
			default -> SerialPort.PARITY_NONE;
		};
	}
	
	@Override
	public boolean write(Object object) {
		try {
			if (object instanceof Byte b) {
				return writeByte(b);
			} else if (object instanceof byte[] ba) {
				return writeByteArray(ba);
			} else if (object instanceof String str) {
				return writeString(str);
			} else if (object instanceof Integer i) {
				return writeInt(i);
			} else if (object instanceof int[] is) {
				return writeIntArray(is);
			} else {
				return false;
			}
		} catch (final SerialPortException ex) {
			if (logSerialPortExceptions) {
				LOG.log(Level.WARNING, ex.getMessage());
			}
			reportTransmitFailure(ex);
			return false;
		}
	}

	private boolean writeByte(byte singleByte) throws SerialPortException {
		try {
			if ((serialPort != null) && serialPort.isOpened() && serialPort.writeByte(singleByte)) {
				reportTransmitAdvice(String.valueOf(singleByte));
				return true;
			} else {
				return false;
			}
		} catch (final SerialPortException ex) {
			if (logSerialPortExceptions) {
				LOG.log(Level.WARNING, ex.getMessage());
			}
			reportTransmitFailure(ex);
			return false;
		}
	}

	private boolean writeByteArray(byte[] byteArray) throws SerialPortException {
		try {
			return serialPort != null && serialPort.isOpened() && serialPort.writeBytes(byteArray);
		} catch (final SerialPortException ex) {
			if (logSerialPortExceptions) {
				LOG.log(Level.WARNING, ex.getMessage());
			}
			reportTransmitFailure(ex);
			return false;
		}
	}

	private boolean writeInt(int singleInt) throws SerialPortException {
		try {
			return serialPort != null && serialPort.isOpened() && serialPort.writeInt(singleInt);
		} catch (final SerialPortException ex) {
			if (logSerialPortExceptions) {
				LOG.log(Level.WARNING, ex.getMessage());
			}
			reportTransmitFailure(ex);
			return false;
		}
	}

	private boolean writeIntArray(int[] intArray) throws SerialPortException {
		try {
			return serialPort != null && serialPort.isOpened() && serialPort.writeIntArray(intArray);
		} catch (final SerialPortException ex) {
			if (logSerialPortExceptions) {
				LOG.log(Level.WARNING, ex.getMessage());
			}
			reportTransmitFailure(ex);
			return false;
		}
	}

	private boolean writeString(String singleString) {
		try {
			return serialPort != null && serialPort.isOpened()
					&& serialPort.writeString(singleString, StandardCharsets.US_ASCII.name());
		} catch (final SerialPortException ex) {
			if (logSerialPortExceptions) {
				LOG.log(Level.WARNING, ex.getMessage());
			}
			reportTransmitFailure(ex);
			return false;
		} catch (UnsupportedEncodingException ex) {
			reportTransmitFailure(ex);
			return false;
		}
	}

	@Override
	public boolean writeString(String string, Charset charset) {
		try {
			return serialPort != null && serialPort.isOpened() && serialPort.writeString(string,charset.name());
		} catch (final SerialPortException ex) {
			if (logSerialPortExceptions) {
				LOG.log(Level.WARNING, ex.getMessage());
			}
			reportTransmitFailure(ex);
			return false;
		} catch (UnsupportedEncodingException ex) {
			reportTransmitFailure(ex);
			return false;
		}
	}

	@Override
	public byte[] readBytes() {
		byte[] b = {};
		try {
			if ((serialPort != null) && serialPort.isOpened()) {
				b = serialPort.readBytes();
				reportReceiveAdvice(new String(b, StandardCharsets.UTF_8));
			}
		} catch (final NullPointerException | SerialPortException ex) {
			if (logSerialPortExceptions) {
				LOG.log(Level.WARNING, ex.getMessage());
			}
			reportReceiveFailure(ex);
		}
		return b;
	}

	@Override
	public int[] readIntArray() {
		int[] i = {};
		try {
			if ((serialPort != null) && serialPort.isOpened()) {
				i = serialPort.readIntArray();
				final String[] a=Arrays.toString(i).split("[\\[\\]]")[1].split(", "); 
				reportReceiveAdvice(Arrays.toString(a)); 
			}
		} catch (final SerialPortException ex) {
			if (logSerialPortExceptions) {
				LOG.log(Level.WARNING, ex.getMessage());
			}
			reportReceiveFailure(ex);
		}
		return i;
	}

	@Override
	public byte[] readBytes(int byteCount) {
		byte[] b = {};
		try {
			if ((serialPort != null) && serialPort.isOpened()) {
				b = serialPort.readBytes(byteCount);
				reportReceiveAdvice(new String(b, StandardCharsets.UTF_8));
			}
		} catch (final SerialPortException ex) {
			if (logSerialPortExceptions) {
				LOG.log(Level.WARNING, ex.getMessage());
			}
			reportReceiveFailure(ex);
		}
		return b;
	}

	@Override
	public String readString() {
		String s = "";
		try {
			if ((serialPort != null) && serialPort.isOpened()) {
				s = serialPort.readString();
				reportReceiveAdvice(s);
			}
		} catch (final SerialPortException ex) {
			if (logSerialPortExceptions) {
				LOG.log(Level.WARNING, ex.getMessage());
			}
			reportReceiveFailure(ex);
		}
		return s;
	}

	@Override
	public String readString(int byteCount) {
		String s = "";
		try {
			if ((serialPort != null) && serialPort.isOpened()) {
				final String input = serialPort.readString(byteCount);
				s = input.replace("\n", "");
				reportReceiveAdvice(s);
			}
		} catch (final SerialPortException ex) {
			if (logSerialPortExceptions) {
				LOG.log(Level.WARNING, ex.getMessage());
			}
			reportReceiveFailure(ex);
		}
		return s;
	}

	private class SerialPortReader implements SerialPortEventListener {

		@Override
        public void serialEvent(SerialPortEvent event) {
            if (event.isBREAK()) {
                breakEvent();
            }
            if (event.isRLSD() && enableEvents) {
                rlsdEvent();
            }
            if (event.isCTS() && enableEvents) { // end device is advising it can receive data
                ctsEvent(); 
            }
            if (event.isRXCHAR() && enableEvents) {
                rxCharEvent();
            }
            if (event.isDSR() && enableEvents) { // end device is requesting to send data
                dsrEvent();
            }
            if (event.isERR()) {
                final int errValue = event.getEventValue();
                if (errValue == ERROR_FRAME || errValue == INTERRUPT_FRAME) {
                    framingError();
                }
                if (errValue == ERROR_PARITY || errValue == INTERRUPT_PARITY) {
                    parityError();
                }
                if (errValue == ERROR_OVERRUN || errValue == INTERRUPT_OVERRUN) {
                    overrunError();
                }
                if (errValue == INTERRUPT_BREAK) {
                    breakError();
                }
                if (errValue == INTERRUPT_TX) {
                    transmitError();
                }
            }
            if (event.isTXEMPTY()) {
                txEmptyEvent();
            }
            if (event.isRXFLAG()) {
                rxFlagEvent();
            }
            if (event.isRING()) {
                ringEvent();
            }
        }
	}

	@Override
	public void openPort(String portName) {
		try {
			if ((serialPort != null) && (getPortName().equals(portName)) && serialPort.isOpened()) {
				return;
			}
			if ((serialPort != null) && (!getPortName().equals(portName)) && serialPort.isOpened()) {
				serialPort.closePort();
			}
			if ((serialPort == null) || (!getPortName().equals(portName))) {
				serialPort = new SerialPort(portName);
			}
			enableEvents = true;
			setPortName(portName);
			if (!serialPort.isOpened()) {
				serialPort.openPort();
			}
			final boolean paramsOk = serialPort.setParams(translateBaudRate(baudRate), translateDataBits(dataBits), translateStopBits(stopBits), translateParity(parity));
			if (!paramsOk) {
				reportConfigurationParameterError(null);
			}
			final boolean dtrOk = serialPort.setDTR(false); // we advise we are ready to receive data
			if (!dtrOk) {
				reportDtrNotSetError(null);
			}
			final boolean rtsOk = serialPort.setRTS(false); // we are requesting to send data
			if (!rtsOk) {
				reportRtsNotSetError(null);
			}
			final boolean eventMaskOk = serialPort.setEventsMask(eventMask);
			if (!eventMaskOk) {
				reportConfigurationParameterError(null);
			}
			final boolean flowOk = serialPort.setFlowControlMode(getFlowControlMask());
			if (!flowOk) {
				reportFlowControlFailure(null);
			}
			final boolean purgeOk = purgeAll();
			if (!purgeOk) {
				reportPurgeFailure(null);
			}
			serialPort.addEventListener(new SerialPortReader());
			LOG.log(Level.INFO, "TTY Port Connection Established with File: {0}", portName);
		} catch (final SerialPortException ex) {
			if (ex.getExceptionType().equals(SerialPortException.TYPE_PORT_BUSY)) {
				LOG.log(Level.INFO, "TTY Port Busy: {0}", portName);
				reportPortBusy(portName);
			}
			if (logSerialPortExceptions) {
				LOG.log(Level.WARNING, portName, ex);
			}
		}
	}

	@Override
	public void openPort() {
		if (getPortName() != null) {
			openPort(getPortName());
		} else {
			final String[] ports = SerialPortList.getPortNames();
			if ((ports != null) && (ports.length > 0)) {
				openPort(ports[0]);
			}
		}
	}

	@Override
    public void closePort() {
        try {
            advisePortClosing();
            if ((serialPort != null) && serialPort.isOpened()) {
                purgeAll();
                serialPort.setDTR(false);
                serialPort.setRTS(false);
            }
        } catch (final SerialPortException ex) {
            if (logSerialPortExceptions) {
                LOG.log(Level.WARNING, ex.getMessage());
            }
            reportErrorClosing(ex);
        } finally {
            if ((serialPort != null) && serialPort.isOpened()) {
                try {
                    serialPort.closePort();
                } catch (SerialPortException ex) {
                    LOG.log(Level.WARNING, ex.getMessage());
                    reportErrorClosing(ex);
                }
            }
        }
    }

	@Override
	public String getLibraryName() {
		return LIBRARY_NAME;
	}

	@Override
	public String getLibraryRevision() {
		return LIBRARY_REVISION;
	}

	@Override
	public boolean isPortOpen() {
		return serialPort.isOpened();
	}

	@Override
	public void close() {
        try {
			serialPort.removeEventListener();
		} catch (SerialPortException ex) {
			LOG.log(Level.WARNING, ex.getMessage());
		}
        enableEvents = false;
		closePort();
	}

}
