package gps;

import java.nio.charset.StandardCharsets;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JPanel;

import tty.SerialComponent;
import tty.AbstractTeletypeController;
import tty.AbstractTeletypeController.BaudRate;
import tty.AbstractTeletypeController.DataBits;
import tty.AbstractTeletypeController.FlowControl;
import tty.AbstractTeletypeController.Parity;
import tty.AbstractTeletypeController.StopBits;
import tty.AbstractTeletypeController.TTYEvents;

public class PanasonicCF31 extends AbstractGpsProcessor {

    private static final long serialVersionUID = 2429326110040309141L;

    private static final String MODEL_NAME = "CF-31 Integrated GPS Receiver";
    private static final String MANUFACTURER = "Panasonic";

	public static final String SETTINGS_TITLE_PREFIX = "GPS";

    private static final BaudRate DEFAULT_BAUD_RATE = BaudRate.BAUDRATE_4800;
    private static final Parity DEFAULT_PARITY = Parity.NONE;
    private static final StopBits DEFAULT_STOP_BITS = StopBits.STOPBITS_2;
    private static final DataBits DEFAULT_DATA_BITS = DataBits.DATABITS_8;
    private static final FlowControl DEFAULT_FLOW_CONTROL_IN = FlowControl.NONE;
    private static final FlowControl DEFAULT_FLOW_CONTROL_OUT = FlowControl.NONE;
    private static final boolean SERIAL_PARAMETERS_FIXED = true;
    private static final boolean SERIAL_BAUD_RATE_FIXED = true;
    private static final BaudRate[] SUPPORTED_BAUD_RATES = { BaudRate.BAUDRATE_4800 };
     
    private static final int READ_BUFFER_LEN = 2048;  
    
    private static final Logger LOG = Logger.getLogger(PanasonicCF31.class.getName());
    
    private final AbstractTeletypeController tty = AbstractTeletypeController.getTTyPortInstance(AbstractTeletypeController.getCatalogMap().getKey("JSSC TTY Port v2.9.5"), false);

	private byte[] readBuffer;
	
    public PanasonicCF31(Boolean clearAllPreferences) {
        super(String.valueOf(serialVersionUID), clearAllPreferences);
        initListeners();
        setDefaultValues();
    }
    
    private void setDefaultValues() {
    	tty.setDefaultBaudRate(DEFAULT_BAUD_RATE);
    	tty.setDefaultDataBits(DEFAULT_DATA_BITS);
    	tty.setDefaultFlowControlIn(DEFAULT_FLOW_CONTROL_IN);
    	tty.setDefaultFlowControlOut(DEFAULT_FLOW_CONTROL_OUT);
    	tty.setDefaultParity(DEFAULT_PARITY);
    	tty.setDefaultStopBits(DEFAULT_STOP_BITS);
    	tty.setDeviceAssignedBaudRateFixed(SERIAL_BAUD_RATE_FIXED);
    	tty.setDeviceAssignedParametersFixed(SERIAL_PARAMETERS_FIXED);
    	tty.setSupportedBaudRates(SUPPORTED_BAUD_RATES);
    }
    
    private void initListeners() {
    	tty.getPropertyChangeSupport().addPropertyChangeListener(event -> {
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
					processNMEAData(data);
					getGPSPropertyChangeSupport().firePropertyChange(AbstractGpsProcessor.RX_DATA, null, data);
				} catch (NullPointerException ex) {
					LOG.log(Level.WARNING, ex.getMessage(), ex);
				}
			}
			if (TTYEvents.TX_DATA.name().equals(event.getPropertyName())) {
				getGPSPropertyChangeSupport().firePropertyChange(AbstractGpsProcessor.TX_DATA, null, event.getNewValue());
			}
			if (TTYEvents.TTY_PORT_CONFIGURATION_ERROR.name().equals(event.getPropertyName())) {
				getGPSPropertyChangeSupport().firePropertyChange(AbstractGpsProcessor.CONFIG_ERROR, event.getOldValue(), event.getNewValue());
			}
			if (TTYEvents.ERROR.name().equals(event.getPropertyName())) {
				getGPSPropertyChangeSupport().firePropertyChange(AbstractGpsProcessor.ERROR, event.getOldValue(), event.getNewValue());
			}
			if (TTYEvents.TTY_PORT_ERROR.name().equals(event.getPropertyName())) {
				getGPSPropertyChangeSupport().firePropertyChange(AbstractGpsProcessor.INVALID_ADDRESS, event.getOldValue(), event.getNewValue());
			}
			if (TTYEvents.DSR.name().equals(event.getPropertyName())) {
				getGPSPropertyChangeSupport().firePropertyChange(AbstractGpsProcessor.DSR, event.getOldValue(), event.getNewValue());
			}
			if (TTYEvents.CTS.name().equals(event.getPropertyName())) {
				getGPSPropertyChangeSupport().firePropertyChange(AbstractGpsProcessor.CTS, event.getOldValue(), event.getNewValue());
			}
			if (TTYEvents.RLSD.name().equals(event.getPropertyName())) {
				getGPSPropertyChangeSupport().firePropertyChange(AbstractGpsProcessor.RLSD, event.getOldValue(), event.getNewValue());
			}
			if (TTYEvents.ONLINE.name().equals(event.getPropertyName())) {
				getGPSPropertyChangeSupport().firePropertyChange(AbstractGpsProcessor.ONLINE, event.getOldValue(), event.getNewValue());
				setEnableEvents((boolean) event.getNewValue());
			}
		});
	}

    @Override
	public void saveClientSettings() {
		tty.savePreferences();
	}
    
	@Override
	public String getDeviceManufacturer() {
		return MANUFACTURER;
	}

	@Override
	public String getDeviceModel() {
		return MODEL_NAME;
	}

	@Override
	public void write(Object object) {
		tty.writeString((String) object, StandardCharsets.US_ASCII);
	}

	@Override
	public long getSerialVersionUID() {
		return serialVersionUID;
	}

	@Override
	public boolean isPortOpen() {
		return tty.isPortOpen();
	}

	@Override
	public void startGPS() {
		super.startGPS();
		tty.openPort();
	}
	
	@Override
	public void stopGPS() {
		super.stopGPS();
		tty.closePort();
	}

	@Override
	public JPanel[] getConfigurationComponentArray() {
		return new SerialComponent(tty).getSettingsPanelArray(SETTINGS_TITLE_PREFIX);
	}

}
