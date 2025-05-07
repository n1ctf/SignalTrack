package n1ctf;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.nio.charset.StandardCharsets;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.prefs.Preferences;

import javax.swing.JPanel;

import network.NetworkParameterSet;

import tcp.TCPClient;
import tcp.TcpIpConfigurationComponent;

public class GeigerCounterClient implements AutoCloseable {
	
	protected static final byte[] DEFAULT_RADIATION_SENSOR_ADDRESS = {(byte) 192, (byte) 168, (byte) 50, (byte) 126};
    
	public static final int DEFAULT_RADIATION_SENSOR_DATA_PORT = 6181;
	public static final boolean DEFAULT_START_WITH_SYSTEM = false;
	public static final int TERMINATE_TIMEOUT = 1000;  // milliseconds
	
	public static final String RADIATION_CPM = "RADIATION_CPM";
    public static final String GAMMA_RADIATION = "GAMMA_RADIATION";
    public static final String BETA_RADIATION = "BETA_RADIATION";
    public static final String ALPHA_RADIATION = "ALPHA_RADIATION";
    
    public static final String INVALID_REQUEST = "INVALID_REQUEST";

    public static final String EQUIPMENT_CODE = "RAD-100";
    public static final String SERIAL_NUMBER = "";
    public static final String MANUFACTURER = "N1CTF";
    public static final String MODEL_NAME = "RAD100";
    public static final String SOFTWARE_VERSION = "1.3";
    public static final String HARDWARE_VERSION = "1.0";
    
    public static final long START_WAIT = 1000;     // milliseconds
    public static final long REQUEST_RATE = 10000;   // milliseconds
    
    private static final String $REQ_CPM = "$REQ_CPM";
    private static final String $REQ_GAMMA = "$REQ_GAMMA";
	private static final String $REQ_BETA = "$REQ_BETA";
	private static final String $REQ_ALPHA = "$REQ_ALPHA";
	
    private static final long FAIL_TIMEOUT_SECONDS = 90;

	private final FailTimer cpmFailTimer;
	private final FailTimer gammaFailTimer;
	private final FailTimer betaFailTimer;
	private final FailTimer alphaFailTimer;
	
    private final ScheduledExecutorService scheduler;
	
    private int cpm = -1;
	private double gamma = -1D;
	private double beta = -1D;
	private double alpha = -1D;
    
    private static final Logger LOG = Logger.getLogger(GeigerCounterClient.class.getName());
    
    private final Preferences userPrefs = Preferences.userRoot().node(GeigerCounterClient.class.getName());
    
    private final NetworkParameterSet currentNetParams;
    private NetworkParameterSet netParams;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final TCPClient tcpClient;
    private PropertyChangeListener tcpClientListener;
    
    private boolean connected;
    private boolean startWithSystem;
    
    private final boolean getCPM;
    private final boolean getGamma;
    private final boolean getBeta;
    private final boolean getAlpha;
		
	public GeigerCounterClient(boolean getCPM, boolean getGamma, boolean getBeta, boolean getAlpha) {
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                close();
            }
        });

		this.getCPM = getCPM;
		this.getGamma = getGamma;
		this.getBeta = getBeta;
		this.getAlpha = getAlpha;
		
		cpmFailTimer = new FailTimer(FAIL_TIMEOUT_SECONDS);
		gammaFailTimer = new FailTimer(FAIL_TIMEOUT_SECONDS);
		betaFailTimer = new FailTimer(FAIL_TIMEOUT_SECONDS);
		alphaFailTimer = new FailTimer(FAIL_TIMEOUT_SECONDS);
		
        configureComponentListeners();

        try {
        	netParams = new NetworkParameterSet(InetAddress.getByAddress(userPrefs.getByteArray(getUniqueDeviceId() + "inet4Address", DEFAULT_RADIATION_SENSOR_ADDRESS)),		
        			userPrefs.getInt(getUniqueDeviceId() + "portNumber", DEFAULT_RADIATION_SENSOR_DATA_PORT));
        	startWithSystem = userPrefs.getBoolean(getUniqueDeviceId() + "startWithSystem", DEFAULT_START_WITH_SYSTEM);
		} catch (UnknownHostException ex) {
			LOG.log(Level.CONFIG, ex.getMessage());	        
		}
        
        currentNetParams = netParams;
        
    	scheduler = Executors.newSingleThreadScheduledExecutor();
    		
        tcpClient = new TCPClient();	

        tcpClient.getPropertyChangeSupport().addPropertyChangeListener(tcpClientListener);
        
        if (startWithSystem) {
        	start();
        }
	}

	public void start() {
		tcpClient.connect(netParams.getInetAddress(), netParams.getPortNumber());
	}
	
	public void stop() {
		gammaFailTimer.close();
		cpmFailTimer.close();
		betaFailTimer.close();
		alphaFailTimer.close();
		stopScheduler();
		tcpClient.disconnect();	
	}
	
	public boolean isOpen() {
		return !scheduler.isShutdown();
	}

    private void stopScheduler() {
    	if (scheduler != null) {
			try {
				LOG.log(Level.INFO, "Initializing AirQualitySencorClient.scheduler service termination....");
				scheduler.shutdown();
				scheduler.awaitTermination(5, TimeUnit.SECONDS);
				LOG.log(Level.INFO, "AirQualitySencorClient.scheduler service has gracefully terminated");
			} catch (InterruptedException e) {
				scheduler.shutdownNow();
				LOG.log(Level.SEVERE, "AirQualitySencorClient.scheduler service has timed out after 3 seconds of waiting to terminate processes.");
				Thread.currentThread().interrupt();
			}
		}
    }
    
	private class FetchData implements Runnable {
        @Override
        public synchronized void run() {
        	try {
	        	if (getCPM) {
	        		tcpClient.write($REQ_CPM.getBytes(StandardCharsets.UTF_8));
	        		TimeUnit.MILLISECONDS.sleep(500);
	        	}
	        	if (getGamma) {
	        		tcpClient.write($REQ_GAMMA.getBytes(StandardCharsets.UTF_8));
	        		TimeUnit.MILLISECONDS.sleep(500);
	        	}
	        	if (getBeta) {
	        		tcpClient.write($REQ_BETA.getBytes(StandardCharsets.UTF_8));
	        		TimeUnit.MILLISECONDS.sleep(500);
	        	}
	        	if (getAlpha) {
	        		tcpClient.write($REQ_ALPHA.getBytes(StandardCharsets.UTF_8));
	        		TimeUnit.MILLISECONDS.sleep(500);
	        	}
        	} catch (InterruptedException ex) {
        		Thread.currentThread().interrupt();
        	}
        }
    }
	
	private void configureComponentListeners() {
		cpmFailTimer.getPropertyChangeSupport().addPropertyChangeListener(event -> {
        	if (event.getPropertyName().equals(FailTimer.FAIL)) {
        		cpm = -1;
        		pcs.firePropertyChange(RADIATION_CPM, null, cpm);
        	}
        });
		
		gammaFailTimer.getPropertyChangeSupport().addPropertyChangeListener(event -> {
        	if (event.getPropertyName().equals(FailTimer.FAIL)) {
        		gamma = -1D;
        		pcs.firePropertyChange(GAMMA_RADIATION, null, gamma);
        	}
        });
        
		betaFailTimer.getPropertyChangeSupport().addPropertyChangeListener(event -> {
        	if (event.getPropertyName().equals(FailTimer.FAIL)) {
        		beta = -1D;
        		pcs.firePropertyChange(BETA_RADIATION, null, beta);
        	}
        });
        
        alphaFailTimer.getPropertyChangeSupport().addPropertyChangeListener(event -> {
        	if (event.getPropertyName().equals(FailTimer.FAIL)) {
        		alpha = -1D;
        		pcs.firePropertyChange(ALPHA_RADIATION, null, alpha);
        	}
        });
    	tcpClientListener = event -> {
        	if (event.getPropertyName().equals(TCPClient.Event.DATA_RECEIVED.name())) {
        		final String dataString = ((String) event.getNewValue()).trim();
        		try {
        			final String[] dataSet = dataString.split(" ");
	        		LOG.log(Level.INFO, "Data receive event from AirQualitySensorServer: {0} {1}", new Object[] {dataSet[0], dataSet[1]});
	        		if (dataSet[0].contains($REQ_CPM)) {
	        			cpm = Integer.parseInt(dataSet[1]);
	                    pcs.firePropertyChange(RADIATION_CPM, null, cpm);
	                    cpmFailTimer.resetTimer();
	        		} else if (dataSet[0].contains($REQ_GAMMA)) {
	        			gamma = Double.parseDouble(dataSet[1]);
	                    pcs.firePropertyChange(GAMMA_RADIATION, null, gamma);
	                    gammaFailTimer.resetTimer();
	        		} else if (dataSet[0].contains($REQ_BETA)) {
	        			beta = Double.parseDouble(dataSet[1]);
	        			pcs.firePropertyChange(BETA_RADIATION, null, beta);
	        			betaFailTimer.resetTimer();
	        		} else if (dataSet[0].contains($REQ_ALPHA)) {
	        			alpha = Double.parseDouble(dataSet[1]);
	        			pcs.firePropertyChange(ALPHA_RADIATION, null, alpha);
	        			alphaFailTimer.resetTimer();
	        		} else {
	        			LOG.log(Level.INFO, "Receive event data header from AirQualitySensor TCP Server is not recognized: {0}. Data is ignored...", dataSet[0]);
	        		}
        		} catch (NumberFormatException ex) {
        			LOG.log(Level.INFO, "NumberFormatException on receive data: {0}. Data does not contain a parseable number.", dataString);
        		} catch (ArrayIndexOutOfBoundsException ex) {
        			LOG.log(Level.INFO, "ArrayIndexOutOfBoundsException on receive data: {0}. Data is not included in statement from AirQualitySensor server...", dataString);
        		}
            }
            if (event.getPropertyName().equals(TCPClient.Event.CONNECTION_ACCEPTED.name())) {
            	if (!isConnected()) {
            		LOG.log(Level.INFO, "Connection ACCEPTED by GeigerCounterServer");
            		scheduler.scheduleAtFixedRate(new FetchData(), START_WAIT, REQUEST_RATE, TimeUnit.MILLISECONDS);
                }
            	setConnected(true);
            }
            if (event.getPropertyName().equals(TCPClient.Event.CONNECTION_DROPPED.name())) {
            	LOG.log(Level.INFO, "Connection DROPPED by GeigerCounterServer");
            	setConnected(false);
            	stopScheduler();
            }
            if (event.getPropertyName().equals(TCPClient.Event.PING_FAILURE.name())) {
            	LOG.log(Level.INFO, "AirSensorServer is unreachable");
            	setConnected(false);
            	stopScheduler();
            	tcpClient.disconnect();
            	tcpClient.connect(netParams.getInetAddress(), netParams.getPortNumber());
            }
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
	
	public int getCPM() {
		return cpm;
	}

	public double getGamma() {
		return gamma;
	}

	public double getBeta() {
		return beta;
	}

	public double getAlpha() {
		return alpha;
	}

	public String getDeviceManufacturer() {
		return MANUFACTURER;
	}

	public String getDeviceModel() {
		return MODEL_NAME;
	}

	public String getDeviceSerialNumber() {
		return SERIAL_NUMBER;
	}

	public String getHardwareVersion() {
		return HARDWARE_VERSION;
	}

	public String getSoftwareVersion() {
		return SOFTWARE_VERSION;
	}

    public String getEquipmentCode() {
        return EQUIPMENT_CODE;
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
    	if (!currentNetParams.equals(netParams)) {
    		tcpClient.reConnect(netParams.getInetAddress(), netParams.getPortNumber());
    	}
	}
    
    @Override
    public void close() {
    	stop();
    	saveClientSettings();
    	stopScheduler();
    	tcpClient.disconnect();
    }

}
