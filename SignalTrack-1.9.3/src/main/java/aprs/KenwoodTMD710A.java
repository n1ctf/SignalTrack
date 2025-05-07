package aprs;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.nio.charset.StandardCharsets;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JPanel;

import meteorology.AbstractEnvironmentSensor;
import tty.SerialComponent;

import tty.AbstractTeletypeController;
import tty.AbstractTeletypeController.BaudRate;
import tty.AbstractTeletypeController.DataBits;
import tty.AbstractTeletypeController.FlowControl;
import tty.AbstractTeletypeController.Parity;
import tty.AbstractTeletypeController.StopBits;
import tty.AbstractTeletypeController.TTYEvents;

public class KenwoodTMD710A extends AbstractTerminalNodeClient {
	private static final long serialVersionUID = 3834709090542979251L;
	
	public static final String MODEL_NAME = "TM-D710A APRS Tranciever";
	public static final String MANUFACTURER = "Kenwood";
	
	public static final String SETTINGS_TITLE_PREFIX = "TNC";

	private static final BaudRate DEFAULT_BAUD_RATE = BaudRate.BAUDRATE_4800;
	private static final Parity DEFAULT_PARITY = Parity.NONE;
	private static final StopBits DEFAULT_STOP_BITS = StopBits.STOPBITS_2;
	private static final DataBits DEFAULT_DATA_BITS = DataBits.DATABITS_8;
	private static final FlowControl DEFAULT_FLOW_CONTROL_IN = FlowControl.NONE;
	private static final FlowControl DEFAULT_FLOW_CONTROL_OUT = FlowControl.NONE;
	private static final boolean SERIAL_PARAMETERS_FIXED = false;
	private static final boolean SERIAL_BAUD_RATE_FIXED = false;

	private static final BaudRate[] SUPPORTED_BAUD_RATES = { BaudRate.BAUDRATE_300, BaudRate.BAUDRATE_1200,
			BaudRate.BAUDRATE_4800, BaudRate.BAUDRATE_9600, BaudRate.BAUDRATE_19200, BaudRate.BAUDRATE_38400 };

	private static final Logger LOG = Logger.getLogger(KenwoodTMD710A.class.getName());
	private static final int READ_BUFFER_LEN = 2048;
	
	private final AbstractTeletypeController tty = AbstractTeletypeController.getTTyPortInstance(AbstractTeletypeController.getCatalogMap().getKey("JSSC TTY Port v2.9.5"), false);
	private PropertyChangeListener serialPortPropertyChangeListener;
	
	private byte[] readBuffer;
	
	public KenwoodTMD710A(AbstractEnvironmentSensor aes, Boolean clearAllPreferences) {
		super(aes, clearAllPreferences);
        configureListeners();

        tty.setDefaultBaudRate(DEFAULT_BAUD_RATE);
		tty.setDefaultDataBits(DEFAULT_DATA_BITS);
		tty.setDefaultFlowControlIn(DEFAULT_FLOW_CONTROL_IN);
		tty.setDefaultFlowControlOut(DEFAULT_FLOW_CONTROL_OUT);
		tty.setDefaultParity(DEFAULT_PARITY);
		tty.setDefaultStopBits(DEFAULT_STOP_BITS);
		tty.setDeviceAssignedBaudRateFixed(SERIAL_BAUD_RATE_FIXED);
		tty.setDeviceAssignedParametersFixed(SERIAL_PARAMETERS_FIXED);
		tty.setSupportedBaudRates(SUPPORTED_BAUD_RATES);

		tty.getPropertyChangeSupport().addPropertyChangeListener(serialPortPropertyChangeListener);
	}

	private void configureListeners() {
		serialPortPropertyChangeListener = (PropertyChangeEvent event) -> {
			if (TTYEvents.RX_CHAR.name().equals(event.getPropertyName())) {
				try {
					String data = null;
					final StringBuilder sb = new StringBuilder();
					int len;
					do {
						readBuffer = tty.readBytes();
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
					super.processNMEAData(data);
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
		tty.savePreferences();
	}

	@Override
	public long getSerialVersionUID() {
		return serialVersionUID;
	}

	@Override
	public boolean isRFPortOpen() {
		return tty.isPortOpen();
	}

	@Override
	public void close() {
		super.close();
		tty.closePort();
        tty.getPropertyChangeSupport().removePropertyChangeListener(serialPortPropertyChangeListener);
	}

	@Override
	public JPanel[] getTTYConfigurationComponentArray() {
		return new SerialComponent(tty).getSettingsPanelArray(SETTINGS_TITLE_PREFIX);
	}

	@Override
	public void sendUpdate(String string) {
		tty.writeString(string, StandardCharsets.US_ASCII);
	}

}