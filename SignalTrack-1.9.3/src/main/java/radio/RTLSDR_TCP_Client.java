package radio;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.io.IOException;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.nio.charset.StandardCharsets;

import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.prefs.Preferences;

import javax.swing.JPanel;

import network.NetworkParameterSet;

import tcp.SynchronizedTCPClient;

import tcp.TcpIpConfigurationComponent;

public class RTLSDR_TCP_Client extends AbstractRadioReceiver implements AutoCloseable {
	
	protected static final byte[] DEFAULT_ADDRESS = {(byte) 192, (byte) 168, (byte) 50, (byte) 127};
    public static final int DEFAULT_PORT = 6181;
	public static final boolean DEFAULT_START_WITH_SYSTEM = false;
	public static final byte DEFAULT_DESTINATION = 1;
    
    public static final String EQUIPMENT_CODE = "PICO-2";
    public static final String SERIAL_NUMBER = "";
    public static final String MANUFACTURER = "NOOELEC";
    public static final String MODEL_NAME = "";
    public static final String SOFTWARE_VERSION = "1.0.4";
    public static final String HARDWARE_VERSION = "1.0";

    private static final boolean SUPPORTS_SINAD = true;
	private static final boolean SUPPORTS_BER = false;
	private static final boolean SUPPORTS_RSSI = true;
	private static final boolean SUPPORTS_AGC = false;
	private static final boolean SUPPORTS_AFC = false;
	private static final boolean SUPPORTS_ATTENUATOR = true;
	private static final boolean SUPPORTS_NOISE_BLANKER = true;
	private static final boolean SUPPORTS_VOLUME_CONTROL = true;
	private static final boolean SUPPORTS_SQUELCH_CONTROL = true;
	private static final boolean SUPPORTS_COR = true;
	private static final boolean SUPPORTS_MULTI_MODE = true;
	private static final boolean SUPPORTS_MULTI_FILTER = true;
	private static final boolean SUPPORTS_COUNTRY_CODE_RETRIEVAL = true;
	private static final boolean SUPPORTS_FIRMWARE_RETRIEVAL = true;
	private static final boolean SUPPORTS_IF_SHIFT = false;
	private static final boolean SUPPORTS_VOICE_SCAN = false;
	private static final boolean SUPPORTS_DSP = false;
	private static final boolean SUPPORTS_FEATURE_CODE_RETRIEVAL = false;
	private static final double MINIMUM_RX_FREQ = 0.050;
	private static final double MAXIMUM_RX_FREQ = 1300.0;
	private static final double MINIMUM_ATTN = 0.0;
    private static final double MAXIMUM_ATTN = 125.0;
	private static final int RSSI_UPPER_LIMIT = 230;
	private static final int RSSI_LOWER_LIMIT = 0;
	private static final double DEFAULT_NOISE_FLOOR = -135;
	private static final double DEFAULT_SATURATION = -21;
	private static final double ADJACENT_CHANNEL_REJECTION_DB = 60.0;
	private static final double SIGNAL_REQ_FOR_12DB_SINAD = -115.0; 
	private static final double SIGNAL_REQ_FOR_20DB_QUIETING = -117.0; 
	private static final double SIGNAL_REQ_FOR_5PCT_BER = -118;
    
	private static final EncryptionProtocol[] ENCRYPTION_PROTOCOLS = { EncryptionProtocol.CLEAR };
	
	private static final Integer[] FILTERS_HZ = { 2800, 6000, 15000, 50000, 230000 };
	
	private static final StandardModeName[] SUPPORTED_MODES = { 
		StandardModeName.WFM,
		StandardModeName.FM, 
		StandardModeName.NFM, 
		StandardModeName.AM,
		StandardModeName.USB, 
		StandardModeName.LSB 
	};
	
    private static final Logger LOG = Logger.getLogger(RTLSDR_TCP_Client.class.getName());
    
    private final Preferences userPrefs = Preferences.userRoot().node(RTLSDR_TCP_Client.class.getName());
    
    private NetworkParameterSet netParams;

    private SynchronizedTCPClient tcpClient;
    private PropertyChangeListener tcpClientListener;
    
    private boolean connected;
    private boolean startWithSystem;
    
    private int centerFreq;
    private int samplingRate;
    private int bandFreq;
    private byte destination = DEFAULT_DESTINATION;

	public RTLSDR_TCP_Client() {
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                close();
            }
        });
		
		try {
			Handler fh = new FileHandler("%t/RTLSDR_TCP_Client.log");
			Handler ch = new ConsoleHandler();
			LOG.addHandler(fh);
			LOG.setLevel(Level.FINEST);
			LOG.addHandler(ch);
			LOG.setUseParentHandlers(false);
		} catch (SecurityException | IOException ex) {
			LOG.log(Level.WARNING, ex.getMessage(), ex);
		}
		
        configureListeners();
        
        try {
        	netParams = new NetworkParameterSet(InetAddress.getByAddress(userPrefs.getByteArray(getUniqueDeviceId() + "inet4Address", DEFAULT_ADDRESS)),		
        			userPrefs.getInt(getUniqueDeviceId() + "portNumber", DEFAULT_PORT));
        	userPrefs.getBoolean(getUniqueDeviceId() + "startWithSystem", DEFAULT_START_WITH_SYSTEM);
		} catch (UnknownHostException ex) {
			LOG.log(Level.CONFIG, ex.getMessage());	        
		}
        
        tcpClient = new SynchronizedTCPClient(StandardCharsets.UTF_8);	

        tcpClient.addPropertyChangeListener(tcpClientListener);
	}
	
	public void setNetParams(NetworkParameterSet netParams) {
		this.netParams = netParams;
	}
	
	public void setFrequency(double freqMHz) {
		
	}
	
	@Override
	public void startRadio() {
		if (!tcpClient.isConnected()) {
			tcpClient.startTCPConnection(netParams);
		}
	}
	
	@Override
	public void stopRadio() {
		tcpClient.stopTCPConnection();
	}
	
	private void configureListeners() {
    	tcpClientListener = event -> {
        	
        };
    }
	
	public boolean isStartWithSystem() {
		return startWithSystem;
	}

	public void setStartWithSystem(boolean startWithSystem) {
		this.startWithSystem = startWithSystem;
	}
	
    public PropertyChangeSupport getPropertyChangeSupport() {
    	return pcs;
    }
    
	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}
    
    public static String getUniqueDeviceId() {
		return MANUFACTURER + "_" + MODEL_NAME + "_" + SERIAL_NUMBER + "_" + 
				HARDWARE_VERSION + "_" + SOFTWARE_VERSION + "_" + EQUIPMENT_CODE;
    }

	public JPanel[] getConfigurationComponentArray() {
		return new TcpIpConfigurationComponent(netParams, MANUFACTURER + " " + MODEL_NAME + " TCP/IP Settings").getSettingsPanelArray();
	}

	public void saveClientSettings() {
		userPrefs.putByteArray(getUniqueDeviceId() + "inet4Address", netParams.getInetAddress().getAddress());
    	userPrefs.putInt(getUniqueDeviceId() + "portNumber", netParams.getPortNumber());
    	userPrefs.putBoolean(getUniqueDeviceId() + "startWithSystem", startWithSystem);
	}
    
    @Override
    public void close() {
    	saveClientSettings();
    	stopRadio();
        for (Handler handler : LOG.getHandlers()) {
			LOG.removeHandler(handler);
			handler.close();
		}
    }

	@Override
	public String getManufacturer() {
		return MANUFACTURER;
	}

	@Override
	public String getModel() {
		return MODEL_NAME;
	}

	@Override
	public boolean isSupportsAGC() {
		return SUPPORTS_AGC;
	}

	@Override
	public boolean isSupportsAFC() {
		return SUPPORTS_AFC;
	}

	@Override
	public boolean isSupportsNoiseBlanker() {
		return SUPPORTS_NOISE_BLANKER;
	}

	@Override
	public boolean isSupportsVoiceScan() {
		return SUPPORTS_VOICE_SCAN;
	}

	@Override
	public boolean isSupportsIFShift() {
		return SUPPORTS_IF_SHIFT;
	}

	@Override
	public boolean isSupportsVolumeSet() {
		return SUPPORTS_VOLUME_CONTROL;
	}

	@Override
	public boolean isSupportsSquelchSet() {
		return SUPPORTS_SQUELCH_CONTROL;
	}

	@Override
	public boolean isSupportsMultiMode() {
		return SUPPORTS_MULTI_MODE;
	}

	@Override
	public boolean isSupportsMultiFilter() {
		return SUPPORTS_MULTI_FILTER;
	}

	@Override
	public boolean isSupportsCountryCodeRetrieval() {
		return SUPPORTS_COUNTRY_CODE_RETRIEVAL;
	}

	@Override
	public boolean isSupportsFirmwareRetrieval() {
		return SUPPORTS_FIRMWARE_RETRIEVAL;
	}

	@Override
	public boolean isSupportsDSP() {
		return SUPPORTS_DSP; 
	}

	@Override
	public boolean isSupportsSINAD() {
		return SUPPORTS_SINAD;
	}

	@Override
	public boolean isSupportsBER() {
		return SUPPORTS_BER;
	}

	@Override
	public boolean isSupportsRSSI() {
		return SUPPORTS_RSSI;
	}

	@Override
	public Integer[] getAvailableFilters() {
		return FILTERS_HZ.clone();
	}

	@Override
	public boolean isSupportsFeatureCodeRetrieval() {
		return SUPPORTS_FEATURE_CODE_RETRIEVAL;
	}

	@Override
	public double getMinRxFreq() {
		return MINIMUM_RX_FREQ;
	}

	@Override
	public double getMaxRxFreq() {
		return MAXIMUM_RX_FREQ;
	}

	@Override
	public boolean isSupportsCOR() {
		return SUPPORTS_COR;
	}

	@Override
	public boolean isSupportsAttenuator() {
		return SUPPORTS_ATTENUATOR;
	}

	@Override
	public int getRssiUpperLimit() {
		return RSSI_UPPER_LIMIT;
	}

	@Override
	public int getRssiLowerLimit() {
		return RSSI_LOWER_LIMIT;
	}

	@Override
	public EncryptionProtocol[] getEncryptionProtocols() {
		return ENCRYPTION_PROTOCOLS.clone();
	}

	@Override
	public StandardModeName[] getModeNameValues() {
		return SUPPORTED_MODES.clone();
	}

	@Override
	public double getDefaultNoiseFloor() {
		return DEFAULT_NOISE_FLOOR;
	}

	@Override
	public double getDefaultSaturation() {
		return DEFAULT_SATURATION;
	}

	@Override
	public double getAdjacentChannelRejectiondB() {
		return ADJACENT_CHANNEL_REJECTION_DB;
	}

	@Override
	public double getSignalReqFor12dBSINADdBm() {
		return SIGNAL_REQ_FOR_12DB_SINAD;
	}

	@Override
	public double getSignalReqFor20dBQuietingdBm() {
		return SIGNAL_REQ_FOR_20DB_QUIETING;
	}

	@Override
	public double getSignalReqFor5PctBERdBm() {
		return SIGNAL_REQ_FOR_5PCT_BER;
	}

	@Override
	public double getMinAttenuator() {
		return MINIMUM_ATTN;
	}

	@Override
	public double getMaxAttenuator() {
		return MAXIMUM_ATTN;
	}

	@Override
	public String getDefaultSerialNumber() {
		return SERIAL_NUMBER;
	}

	@Override
	public long getVersionUID() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void processData(String data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getInterfaceName() {
		// TODO Auto-generated method stub
		return null;
	}

}
