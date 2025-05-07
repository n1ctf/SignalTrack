package n1ctf;

import java.awt.Color;
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

import javax.swing.JLabel;
import javax.swing.JPanel;

import network.NetworkParameterSet;

import tcp.TCPClient;
import tcp.TcpIpConfigurationComponent;

public class AirQualitySensorClient implements AutoCloseable {
	
	protected static final byte[] DEFAULT_RADIATION_SENSOR_ADDRESS = {(byte) 192, (byte) 168, (byte) 50, (byte) 127};
    
	public static final int DEFAULT_RADIATION_SENSOR_DATA_PORT = 6181;
	public static final boolean DEFAULT_START_WITH_SYSTEM = false;
	public static final int TERMINATE_TIMEOUT = 1000;  // milliseconds

    public static final String ECO2 = "ECO2";
    public static final String TVOC = "TVOC";
    
    public static final String INVALID_REQUEST = "INVALID_REQUEST";

    public static final String EQUIPMENT_CODE = "RAD-100";
    public static final String SERIAL_NUMBER = "";
    public static final String MANUFACTURER = "N1CTF";
    public static final String MODEL_NAME = "AQS100";
    public static final String SOFTWARE_VERSION = "1.3";
    public static final String HARDWARE_VERSION = "1.0";
    
    public static final long START_WAIT = 1000;     // milliseconds
    public static final long REQUEST_RATE = 10000;   // milliseconds
    
	private static final String $REQ_ECO2 = "$REQ_ECO2";
	private static final String $REQ_TVOC = "$REQ_TVOC";
	
	public static final String $SET_HUMID = "$SET_HUMID";
	
	private static final long FAIL_TIMEOUT_SECONDS = 90;

	private final FailTimer tvocFailTimer;
	private final FailTimer eco2FailTimer;
	
    private final ScheduledExecutorService scheduler;

	private int eco2 = -1;
	private int tvoc = -1;
    
    private static final Logger LOG = Logger.getLogger(AirQualitySensorClient.class.getName());
    
    private final Preferences userPrefs = Preferences.userRoot().node(AirQualitySensorClient.class.getName());
    
    private NetworkParameterSet currentNetParams;
    private NetworkParameterSet netParams;
    
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final TCPClient tcpClient;
    private PropertyChangeListener tcpClientListener;
    
    private boolean connected;
    private boolean startWithSystem;
    
	public AirQualitySensorClient() {
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                close();
            }
        });

		
		eco2FailTimer = new FailTimer(FAIL_TIMEOUT_SECONDS);
		tvocFailTimer = new FailTimer(FAIL_TIMEOUT_SECONDS);
		
        configureComponentListeners();

    	scheduler = Executors.newSingleThreadScheduledExecutor();

        try {
        	netParams = new NetworkParameterSet(InetAddress.getByAddress(userPrefs.getByteArray(getUniqueDeviceId() + "inet4Address", DEFAULT_RADIATION_SENSOR_ADDRESS)),		
        			userPrefs.getInt(getUniqueDeviceId() + "portNumber", DEFAULT_RADIATION_SENSOR_DATA_PORT));
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
		eco2FailTimer.close();
		tvocFailTimer.close();
		stopScheduler();
		tcpClient.disconnect();
	}

	public boolean isOpen() {
		return !scheduler.isShutdown();
	}
	
	public void updateHumidityGramsPerCubicMeter(double gm3) {
		tcpClient.write(($SET_HUMID + " " + gm3 + "\n").getBytes(StandardCharsets.UTF_8));
	}

    private void stopScheduler() {
    	if (scheduler != null) {
			try {
				LOG.log(Level.INFO, "Initializing AirQualitySencorClient.scheduler service termination....");
				scheduler.shutdown();
				scheduler.awaitTermination(2, TimeUnit.SECONDS);
				LOG.log(Level.INFO, "AirQualitySencorClient.scheduler service has gracefully terminated");
			} catch (InterruptedException e) {
				scheduler.shutdownNow();
				LOG.log(Level.SEVERE, "AirQualitySencorClient.scheduler service has timed out after 2 seconds of waiting to terminate processes.");
				Thread.currentThread().interrupt();
			}
		}
    }
    
	private class FetchData implements Runnable {
        @Override
        public synchronized void run() {
        	tcpClient.write($REQ_ECO2.getBytes(StandardCharsets.UTF_8));
			tcpClient.write($REQ_TVOC.getBytes(StandardCharsets.UTF_8));
        }
    }
	
	private void configureComponentListeners() {
		eco2FailTimer.getPropertyChangeSupport().addPropertyChangeListener(event -> {
        	if (event.getPropertyName().equals(FailTimer.FAIL)) {
        		eco2 = -1;
        		pcs.firePropertyChange(ECO2, null, eco2);
        	}
        });
        tvocFailTimer.getPropertyChangeSupport().addPropertyChangeListener(event -> {
        	if (event.getPropertyName().equals(FailTimer.FAIL)) {
        		tvoc = -1;
        		pcs.firePropertyChange(TVOC, null, tvoc);
        	}
        });
    	tcpClientListener = event -> {
        	if (event.getPropertyName().equals(TCPClient.Event.DATA_RECEIVED.name())) {
        		final String dataString = ((String) event.getNewValue()).trim();
        		try {
        			final String[] dataSet = dataString.split(" ");
	        		LOG.log(Level.INFO, "Data receive event from AirQualitySensorServer: {0} {1}", new Object[] {dataSet[0], dataSet[1]});
	        		if (dataSet[0].contains($REQ_ECO2)) {
	        			eco2 = Integer.parseInt(dataSet[1]);
	        			pcs.firePropertyChange(ECO2, null, eco2);
	        			eco2FailTimer.resetTimer();
	        		} else if (dataSet[0].contains($REQ_TVOC)) {
	        			tvoc = Integer.parseInt(dataSet[1]);
	        			pcs.firePropertyChange(TVOC, null, tvoc);
	    				tvocFailTimer.resetTimer();
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
            	if (!isConnected() && startWithSystem) {
            		LOG.log(Level.INFO, "Connection ACCEPTED by AirQualitySensorServer");
            		scheduler.scheduleAtFixedRate(new FetchData(), START_WAIT, REQUEST_RATE, TimeUnit.MILLISECONDS);
                }
            	setConnected(true);
            }
            if (event.getPropertyName().equals(TCPClient.Event.CONNECTION_DROPPED.name())) {
            	LOG.log(Level.INFO, "Connection DROPPED by AirQualitySensorServer");
            	setConnected(false);
            	stopScheduler();
            }
            if (event.getPropertyName().equals(TCPClient.Event.PING_FAILURE.name())) {
            	LOG.log(Level.INFO, "AirQualitySensorServer is unreachable");
            	setConnected(false);
            	stopScheduler();
            	tcpClient.disconnect();
            	tcpClient.connect(netParams.getInetAddress(), netParams.getPortNumber());
            }
        };
    }
	
	public static final JLabel getECO2Flag(int eco2) {
        final JLabel label = new JLabel();
        label.setBackground(Color.LIGHT_GRAY);
        label.setForeground(Color.BLACK);
        label.setText("OUT OF RANGE");
        if (eco2 >= 300 && eco2 <= 450) {
            label.setBackground(Color.GREEN);
            label.setText("<html>IDEAL</html>");
        } else if (eco2 >= 451 && eco2 <= 600) {
            label.setBackground(Color.YELLOW);
            label.setForeground(Color.BLACK);
            label.setText("<html>EXCELLENT</html>");
        } else if (eco2 >= 601 && eco2 <= 1000) {
            label.setBackground(Color.ORANGE);
            label.setForeground(Color.BLACK);
            label.setText("<html>ACCEPTABLE</html>");
        } else if (eco2 >= 1001) {
            label.setBackground(Color.RED);
            label.setForeground(Color.WHITE);
            label.setText("<html>ELEVATED</html>");
        }
        return label;
    }
	
	public static final JLabel getTVOCFlag(int tvoc) {
        final JLabel label = new JLabel();
        label.setBackground(Color.LIGHT_GRAY);
        label.setForeground(Color.BLACK);
        label.setText("OUT OF RANGE");
        if (tvoc >= 0 && tvoc <= 220) {
            label.setBackground(Color.GREEN);
            label.setForeground(Color.BLACK);
            label.setText("<html>GOOD</html>");
        } else if (tvoc >= 221 && tvoc <= 660) {
            label.setBackground(Color.YELLOW);
            label.setForeground(Color.BLACK);
            label.setText("<html>MODERATE</html>");
        } else if (tvoc >= 661 && tvoc <= 1430) {
            label.setBackground(Color.ORANGE);
            label.setForeground(Color.BLACK);
            label.setText("<html>HIGH</html>");
        } else if (tvoc >= 1431 && tvoc <= 2200) {
            label.setBackground(Color.RED);
            label.setForeground(Color.WHITE);
            label.setText("<html>VERY HIGH</html>");
        } else if (tvoc >= 2201 && tvoc <= 3330) {
            label.setBackground(Color.BLUE);
            label.setForeground(Color.WHITE);
            label.setText("<html>EXTREME</html>");
        } else if (tvoc >= 3301) {
            label.setBackground(Color.MAGENTA);
            label.setForeground(Color.WHITE);
            label.setText("<html>DANGEROUS</html>");
        }
        return label;
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

	public int getEco2() {
		return eco2;
	}

	public int getTvoc() {
		return tvoc;
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
		currentNetParams = netParams;
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
