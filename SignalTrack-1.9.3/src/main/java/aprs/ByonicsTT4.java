package aprs;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.nio.charset.StandardCharsets;

import java.util.logging.Level;

import meteorology.AbstractEnvironmentSensor;

import tty.AbstractTeletypeController.BaudRate;
import tty.AbstractTeletypeController.DataBits;
import tty.AbstractTeletypeController.FlowControl;
import tty.AbstractTeletypeController.Parity;
import tty.AbstractTeletypeController.StopBits;
import tty.AbstractTeletypeController.TTYEvents;

public class ByonicsTT4 extends AbstractTerminalNodeClient {
	private static final long serialVersionUID = 2429325779488236070L;

	public static final String MODEL_NAME = "TinyTrak 4";
	public static final String MANUFACTURER = "Byonics";
	
	private static final String DEFAULT_TT4_PORT_B_NAME = "/dev/serial/by-id/usb-Prolific_Technology_Inc._USB-Serial_Controller_D-if00-port0";
	private static final BaudRate DEFAULT_BAUD_RATE = BaudRate.BAUDRATE_4800;
	private static final Parity DEFAULT_PARITY = Parity.NONE;
	private static final StopBits DEFAULT_STOP_BITS = StopBits.STOPBITS_1;
	private static final DataBits DEFAULT_DATA_BITS = DataBits.DATABITS_8;
	private static final FlowControl DEFAULT_FLOW_CONTROL_IN = FlowControl.NONE;
	private static final FlowControl DEFAULT_FLOW_CONTROL_OUT = FlowControl.NONE;
	private static final boolean SERIAL_PARAMETERS_FIXED = false;
	private static final boolean SERIAL_BAUD_RATE_FIXED = false;
	private static final boolean DEFAULT_FORCE_DTR = true;
	private static final boolean DEFAULT_FORCE_RTS = true;

	private static final BaudRate[] SUPPORTED_BAUD_RATES = { BaudRate.BAUDRATE_300, BaudRate.BAUDRATE_1200,
			BaudRate.BAUDRATE_4800, BaudRate.BAUDRATE_9600, BaudRate.BAUDRATE_19200, BaudRate.BAUDRATE_38400 };

	private static final int READ_BUFFER_LEN = 2048;
		
	private PropertyChangeListener serialPortPropertyChangeListener;

	private byte[] readBuffer;

	protected ByonicsTT4(AbstractEnvironmentSensor aes, Boolean clearAllPreferences) {
		super(aes, clearAllPreferences);
        configureListeners();

		getTTY().setDefaultBaudRate(DEFAULT_BAUD_RATE);
		getTTY().setDefaultDataBits(DEFAULT_DATA_BITS);
		getTTY().setDefaultFlowControlIn(DEFAULT_FLOW_CONTROL_IN);
		getTTY().setDefaultFlowControlOut(DEFAULT_FLOW_CONTROL_OUT);
		getTTY().setDefaultParity(DEFAULT_PARITY);
		getTTY().setDefaultStopBits(DEFAULT_STOP_BITS);
		getTTY().setDeviceAssignedBaudRateFixed(SERIAL_BAUD_RATE_FIXED);
		getTTY().setDeviceAssignedParametersFixed(SERIAL_PARAMETERS_FIXED);
		getTTY().setSupportedBaudRates(SUPPORTED_BAUD_RATES);
		getTTY().setDTR(DEFAULT_FORCE_DTR);
    	getTTY().setRTS(DEFAULT_FORCE_RTS);
		
		getTTY().openPort(DEFAULT_TT4_PORT_B_NAME);
		
		getTTY().getPropertyChangeSupport().addPropertyChangeListener(serialPortPropertyChangeListener);
	}

	private void configureListeners() {
		serialPortPropertyChangeListener = (PropertyChangeEvent event) -> {
			if (TTYEvents.RX_CHAR.name().equals(event.getPropertyName())) {
				try {
					String data = null;
					final StringBuilder sb = new StringBuilder();
					int len;
					do {
						readBuffer = getTTY().readBytes();
						if (readBuffer == null) {
							return;
						} else {
							len = readBuffer.length;
						}
						if (len >= 1) {
							sb.append(new String(readBuffer, 0, len, StandardCharsets.UTF_8));
							data = sb.toString().trim();
						}
					} while (len == READ_BUFFER_LEN);
					processNMEAData(data);
					super.pcs.firePropertyChange(Event.NMEA_DATA.name(), null, data);
				} catch (NullPointerException ex) {
					LOG.log(Level.WARNING, ex.getMessage(), ex);
				}
			}
			if (TTYEvents.RX_DATA.name().equals(event.getPropertyName())) {
				pcs.firePropertyChange(Event.RX_DATA.name(), null, event.getNewValue());
			}
			if (TTYEvents.TX_DATA.name().equals(event.getPropertyName())) {
				pcs.firePropertyChange(Event.TX_DATA.name(), null, event.getNewValue());
			}
			if (TTYEvents.TTY_PORT_CONFIGURATION_ERROR.name().equals(event.getPropertyName())) {
				pcs.firePropertyChange(Event.CONFIG_ERROR.name(), event.getOldValue(), event.getNewValue());
			}
			if (TTYEvents.ERROR.name().equals(event.getPropertyName())) {
				pcs.firePropertyChange(Event.ERROR.name(), event.getOldValue(), event.getNewValue());
			}
			if (TTYEvents.TTY_PORT_ERROR.name().equals(event.getPropertyName())) {
				pcs.firePropertyChange(Event.INVALID_ADDRESS.name(), event.getOldValue(), event.getNewValue());
			}
			if (TTYEvents.DSR.name().equals(event.getPropertyName())) {
				pcs.firePropertyChange(Event.DSR.name(), event.getOldValue(), event.getNewValue());
			}
			if (TTYEvents.CTS.name().equals(event.getPropertyName())) {
				pcs.firePropertyChange(Event.CTS.name(), event.getOldValue(), event.getNewValue());
			}
			if (TTYEvents.RLSD.name().equals(event.getPropertyName())) {
				pcs.firePropertyChange(Event.RLSD.name(), event.getOldValue(), event.getNewValue());
			}
			if (TTYEvents.ONLINE.name().equals(event.getPropertyName())) {
				pcs.firePropertyChange(Event.ONLINE.name(), event.getOldValue(), event.getNewValue());
			}
		};
	}

	@Override
	public void saveClientSettings() {
		getTTY().savePreferences();
	}

	@Override
	public void sendUpdate(String string) {
		getTTY().writeString(string, StandardCharsets.US_ASCII);
	}

	@Override
	public long getSerialVersionUID() {
		return serialVersionUID;
	}

	@Override
	public boolean isRFPortOpen() {
		return getTTY().isPortOpen();
	}

	@Override
	public void close() {
		super.close();
		getTTY().closePort();
        getTTY().getPropertyChangeSupport().removePropertyChangeListener(serialPortPropertyChangeListener);
	}
	
}