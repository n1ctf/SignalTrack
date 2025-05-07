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


public class TempSensorClient implements AutoCloseable {
	
	protected static final byte[] DEFAULT_BLACK_GLOBE_TEMP_SENSOR_ADDRESS = {(byte) 192, (byte) 168, (byte) 56, (byte) 168};
    public static final int DEFAULT_BLACK_GLOBE_TEMP_SENSOR_DATA_PORT = 6181;
	public static final boolean DEFAULT_START_WITH_SYSTEM = false;
	public static final int TERMINATE_TIMEOUT = 1000;  // milliseconds
	
    public static final String CH0_TEMP = "CH0_TEMP";
    public static final String CH1_TEMP = "CH1_TEMP";
    public static final String CH2_TEMP = "CH2_TEMP";
    public static final String CH3_TEMP = "CH3_TEMP";
    
    public static final String EQUIPMENT_CODE = "BGTS-100";
    public static final String SERIAL_NUMBER = "";
    public static final String MANUFACTURER = "N1CTF";
    public static final String MODEL_NAME = "BGTS100";
    public static final String SOFTWARE_VERSION = "1.2";
    public static final String HARDWARE_VERSION = "1.0";
    
    public static final long START_WAIT = 1000;     // milliseconds
    public static final long REQUEST_RATE = 10000;   // milliseconds
    
    private static final String $REQ_CH0_TEMP = "$REQ_CH0_TEMP";
    private static final String $REQ_CH1_TEMP = "$REQ_CH1_TEMP";
    private static final String $REQ_CH2_TEMP = "$REQ_CH2_TEMP";
    private static final String $REQ_CH3_TEMP = "$REQ_CH3_TEMP";
    
    private static final long FAIL_TIMEOUT_SECONDS = 90;
    
    private static final Logger LOG = Logger.getLogger(TempSensorClient.class.getName());
    
    private final Preferences userPrefs = Preferences.userRoot().node(TempSensorClient.class.getName());
    private final NetworkParameterSet currentNetParams;
    private NetworkParameterSet netParams;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    private final TCPClient tcpClient;

    private final ScheduledExecutorService scheduler;
	private final FailTimer failTimerCh0;
	private final FailTimer failTimerCh1;
	private final FailTimer failTimerCh2;
	private final FailTimer failTimerCh3;
	
    private PropertyChangeListener tcpClientListener;
    
    private boolean connected;
    private boolean startWithSystem;
	
	private double ch0 = -256D;
	private double ch1 = -256D;
	private double ch2 = -256D;
	private double ch3 = -256D;
	
	private boolean reqCh0 = true;
	private boolean reqCh1 = true;
	private boolean reqCh2 = true;
	private boolean reqCh3 = true;
	
	public TempSensorClient(boolean reqCh0, boolean reqCh1, boolean reqCh2, boolean reqCh3) {
		this.reqCh0 = reqCh0;
		this.reqCh1 = reqCh1;
		this.reqCh2 = reqCh2;
		this.reqCh3 = reqCh3;
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                close();
            }
        });
		
		failTimerCh0 = new FailTimer(FAIL_TIMEOUT_SECONDS);
		failTimerCh1 = new FailTimer(FAIL_TIMEOUT_SECONDS);
		failTimerCh2 = new FailTimer(FAIL_TIMEOUT_SECONDS);
		failTimerCh3 = new FailTimer(FAIL_TIMEOUT_SECONDS);
		
		configureComponentListeners();

		scheduler = Executors.newSingleThreadScheduledExecutor();
		
        try {
        	netParams = new NetworkParameterSet(InetAddress.getByAddress(userPrefs.getByteArray(getUniqueDeviceId() + "inet4Address", DEFAULT_BLACK_GLOBE_TEMP_SENSOR_ADDRESS)),		
        		userPrefs.getInt(getUniqueDeviceId() + "portNumber", DEFAULT_BLACK_GLOBE_TEMP_SENSOR_DATA_PORT));
        	startWithSystem = userPrefs.getBoolean(getUniqueDeviceId() + "startWithSystem", DEFAULT_START_WITH_SYSTEM);
		} catch (UnknownHostException ex) {
			LOG.log(Level.CONFIG, ex.getMessage());	        
		}
        
        currentNetParams = netParams;
        
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
		failTimerCh0.close();
		failTimerCh1.close();
		failTimerCh2.close();
		failTimerCh3.close();
		stopScheduler();
		if (tcpClient != null) {
    		tcpClient.disconnect();
    	}
	}
	
	private void configureComponentListeners() {
		failTimerCh0.getPropertyChangeSupport().addPropertyChangeListener(event -> {
        	if (event.getPropertyName().equals(FailTimer.FAIL)) {
        		ch0 = -256D;
                pcs.firePropertyChange(CH0_TEMP, null, ch0);
        	}
        });
		failTimerCh1.getPropertyChangeSupport().addPropertyChangeListener(event -> {
        	if (event.getPropertyName().equals(FailTimer.FAIL)) {
        		ch1 = -256D;
                pcs.firePropertyChange(CH1_TEMP, null, ch1);
        	}
        });
		failTimerCh2.getPropertyChangeSupport().addPropertyChangeListener(event -> {
        	if (event.getPropertyName().equals(FailTimer.FAIL)) {
        		ch2 = -256D;
                pcs.firePropertyChange(CH2_TEMP, null, ch2);
        	}
        });
		failTimerCh3.getPropertyChangeSupport().addPropertyChangeListener(event -> {
        	if (event.getPropertyName().equals(FailTimer.FAIL)) {
        		ch3 = -256D;
                pcs.firePropertyChange(CH3_TEMP, null, ch3);
        	}
        });
        tcpClientListener = event -> {
        	if (event.getPropertyName().equals(TCPClient.Event.DATA_RECEIVED.name())) {
        		final String dataString = ((String) event.getNewValue()).trim();
        		try {
        			final String[] dataSet = dataString.split(" ");
	        		LOG.log(Level.INFO, "Data receive event from TempSensorClient TCP Client: {0} {1}", new Object[] {dataSet[0], dataSet[1]});
	        		if (dataSet[0].contains($REQ_CH0_TEMP)) {
	                    pcs.firePropertyChange(CH0_TEMP, null, Double.parseDouble(dataSet[1]));
	        			ch0 = Double.parseDouble(dataSet[1]);
	        			failTimerCh0.resetTimer();
	        		} else if (dataSet[0].contains($REQ_CH1_TEMP)) {
	                    pcs.firePropertyChange(CH1_TEMP, null, Double.parseDouble(dataSet[1]));
	        			ch1 = Double.parseDouble(dataSet[1]);
	        			failTimerCh1.resetTimer();
	        		} else if (dataSet[0].contains($REQ_CH2_TEMP)) {
	                    pcs.firePropertyChange(CH2_TEMP, null, Double.parseDouble(dataSet[1]));
	        			ch2 = Double.parseDouble(dataSet[1]);
	        			failTimerCh2.resetTimer();
	        		} else if (dataSet[0].contains($REQ_CH3_TEMP)) {
	                    pcs.firePropertyChange(CH3_TEMP, null, Double.parseDouble(dataSet[1]));
	        			ch3 = Double.parseDouble(dataSet[1]);
	        			failTimerCh3.resetTimer();
	        		} else {
	        			LOG.log(Level.INFO, "Receive event data header from TempSensor TCP Server is not recognized: {0}. Data is ignored...", dataSet[0]);
	        		}
        		} catch (NumberFormatException ex) {
        			LOG.log(Level.INFO, "NumberFormatException on receive data: {0}. Data does not contain a parseable number.", dataString);
        		} catch (ArrayIndexOutOfBoundsException ex) {
        			LOG.log(Level.INFO, "ArrayIndexOutOfBoundsException on receive data: {0}. Data is not included in statement from AirQualitySensor server...", dataString);
        		}
            }
        	if (event.getPropertyName().equals(TCPClient.Event.CONNECTION_ACCEPTED.name())) {
                if (!isConnected()) {
                	LOG.log(Level.INFO, "TempSensorServer is connected");
                	scheduler.scheduleAtFixedRate(new FetchData(), START_WAIT, REQUEST_RATE, TimeUnit.MILLISECONDS);
                }
                setConnected(true);
            }
            if (event.getPropertyName().equals(TCPClient.Event.CONNECTION_DROPPED.name())) {
            	setConnected(false);
                stopScheduler();
            }
            if (event.getPropertyName().equals(TCPClient.Event.PING_FAILURE.name())) {
            	LOG.log(Level.INFO, "TempSensorServer is unreachable");
            	setConnected(false);
            	stopScheduler();
            	tcpClient.disconnect();
            	tcpClient.connect(netParams.getInetAddress(), netParams.getPortNumber());
            }
        };
    }

	private class FetchData implements Runnable {
        @Override
        public synchronized void run() {
        	if (reqCh0) {
				tcpClient.write("$REQ_CH0_TEMP\n".getBytes(StandardCharsets.UTF_8));
			}
        	if (reqCh1) {
				tcpClient.write("$REQ_CH1_TEMP\n".getBytes(StandardCharsets.UTF_8));
			}
        	if (reqCh2) {
				tcpClient.write("$REQ_CH2_TEMP\n".getBytes(StandardCharsets.UTF_8));
			}
        	if (reqCh3) {
				tcpClient.write("$REQ_CH3_TEMP\n".getBytes(StandardCharsets.UTF_8));
			}
        }
    }
	
	public double getCh0() {
		return ch0;
	}

	public double getCh1() {
		return ch1;
	}

	public double getCh2() {
		return ch2;
	}

	public double getCh3() {
		return ch3;
	}

	public boolean isStartWithSystem() {
		return startWithSystem;
	}

	public void setStartWithSystem(boolean startWithSystem) {
		this.startWithSystem = startWithSystem;
	}

    private void stopScheduler() {
    	if (scheduler != null) {
			try {
				LOG.log(Level.INFO, "Initializing TempSensorClient.scheduler termination....");
				scheduler.shutdown();
				scheduler.awaitTermination(3, TimeUnit.SECONDS);
				LOG.log(Level.INFO, "TempSensorClient.scheduler has gracefully terminated");
			} catch (InterruptedException e) {
				scheduler.shutdownNow();
				LOG.log(Level.SEVERE, "TempSensorClient.scheduler has timed out after 3 seconds of waiting to terminate processes.");
				Thread.currentThread().interrupt();
			}
		}
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
    }

}
