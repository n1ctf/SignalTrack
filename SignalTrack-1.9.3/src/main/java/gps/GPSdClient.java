package gps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import java.nio.charset.StandardCharsets;

import java.util.LinkedList;
import java.util.Queue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.JPanel;

import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.apache.commons.net.telnet.SuppressGAOptionHandler;
import org.apache.commons.net.telnet.TelnetClient;
import org.apache.commons.net.telnet.TerminalTypeOptionHandler;

import network.NetworkParameterSet;

import tcp.TcpIpConfigurationComponent;

public class GPSdClient extends AbstractGpsProcessor {
	private static final long serialVersionUID = 2429324720708819141L;
	
	public static final boolean DEFAULT_DEBUG = false; 
	public static final String MODEL_NAME = "GPSd Client";
	public static final String MANUFACTURER = "Apache Telnet";
	public static final int DEFAULT_PORT = 2947;
	
	protected static final byte[] DEFAULT_ADDRESS = {(byte) 192, (byte) 168, (byte) 56, (byte) 28};
	
	private static final Logger LOG = Logger.getLogger(GPSdClient.class.getName());
	
	protected static final char[] WATCH_COMMAND = { '?', 'W', 'A', 'T', 'C', 'H', '=', '{', '"', 'e', 'n', 'a', 'b', 'l',
			'e', '"', ':', 't', 'r', 'u', 'e', ',', '"', 'j', 's', 'o', 'n', '"', ':', 't', 'r', 'u', 'e', '}', ';' };
	
	private final Preferences userPrefs = Preferences.userRoot().node(GPSdClient.class.getName());
	
	private ExecutorService executor;
	
	private TelnetClient telnet;
    
	private NetworkParameterSet netParams;

	private boolean debug;
	private boolean enabled;
	
	private final Queue<String> writeQueue = new LinkedList<>();
	
	public GPSdClient(Boolean clearAllPreferences) {
		this(clearAllPreferences, DEFAULT_DEBUG);
	}
	
	public GPSdClient(Boolean clearAllPreferences, Boolean debug) {
		super(String.valueOf(serialVersionUID), clearAllPreferences);
		
		this.debug = debug;
		
		initializeShutdownHook();
		
		try {
			netParams = new NetworkParameterSet(InetAddress.getByAddress(userPrefs.getByteArray(String.valueOf(serialVersionUID) + "inet4Address", DEFAULT_ADDRESS)),		
			userPrefs.getInt(String.valueOf(serialVersionUID) + "portNumber", DEFAULT_PORT));
			if (Boolean.TRUE.equals(clearAllPreferences)) {
				this.userPrefs.clear();
			}
			
		} catch (UnknownHostException ex) {
			LOG.log(Level.CONFIG, ex.getMessage(), ex);	        
			fireErrorEvent(ex);
		} catch (BackingStoreException ex) {
			LOG.log(Level.CONFIG, ex.getMessage(), ex);
		}
	}

	private void fireErrorEvent(UnknownHostException ex) {
		getGPSPropertyChangeSupport().firePropertyChange(ERROR, null, ex.getMessage());
	}
	
	@Override
	public void close() {
		super.close();
		super.savePreferences();
		this.stopGPS();
		saveClientSettings();
	}

	@Override
	public void saveClientSettings() {
		userPrefs.putByteArray(String.valueOf(serialVersionUID) + "inet4Address", netParams.getInetAddress().getAddress());
    	userPrefs.putInt(String.valueOf(serialVersionUID) + "portNumber", netParams.getPortNumber());
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
	public boolean isPortOpen() {
		return telnet.isConnected();
	}

	@Override
	public void write(Object object) {
		writeQueue.add((String) object);
	}

	@Override
	public long getSerialVersionUID() {
		return serialVersionUID;
	}

	@Override
	public void startGPS() {
		super.startGPS();
		executor = Executors.newSingleThreadExecutor();
		enabled = true;
		
		final TerminalTypeOptionHandler ttopt = new TerminalTypeOptionHandler("VT200", false, false, true, false);
		final SuppressGAOptionHandler gaopt = new SuppressGAOptionHandler(true, true, true, true);
    	
    	telnet = new TelnetClient();
    	
    	try {
			telnet.addOptionHandler(ttopt);
			telnet.addOptionHandler(gaopt);
		} catch (InvalidTelnetOptionException ex) {
			if (debug) {
				LOG.log(Level.WARNING, ex.getMessage(), ex);
			}
		} catch (IOException ex) {
			if (debug) {
				LOG.log(Level.WARNING, ex.getMessage(), ex);
			}
		}
    	
    	try {
    		executor.execute(new Reader());
    	} catch (RejectedExecutionException ex) {
    		if (debug) {
				LOG.log(Level.WARNING, ex.getMessage(), ex);
			}
    		stopGPS();
    	}
	}
	
	@Override
	public void stopGPS() {
		try {
			super.stopGPS();
			
			enabled = false;
			
			if (executor != null) {
				try {
					LOG.log(Level.INFO, "Initializing GPSdClient Service termination....");
					executor.shutdown();
					executor.awaitTermination(20, TimeUnit.SECONDS);
					LOG.log(Level.INFO, "GPSdClient Service has gracefully terminated");
				} catch (InterruptedException e) {
					LOG.log(Level.SEVERE, "GPSdClient Service has timed out after 20 seconds of waiting to terminate processes.");
					Thread.currentThread().interrupt();
				}
			}
			
			telnet.disconnect();
		} catch (IOException ex) {
			LOG.log(Level.WARNING, ex.getMessage(), ex);
		}
	}
	
	private class Reader implements Runnable {

		@Override
		public void run() {
			boolean watchCommandSent = false;

    		telnet.setDefaultTimeout(5000);
    		
			try {
				telnet.connect(netParams.getInetAddress(), netParams.getPortNumber());
				
				while (!telnet.isConnected() && enabled) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException ex) {
						if (debug) {
							LOG.log(Level.WARNING, ex.getMessage(), ex);
						}
						Thread.currentThread().interrupt();
						enabled = false;
					}
				}
				
				getGPSPropertyChangeSupport().firePropertyChange(ONLINE, null, true);
				
				if (debug) {
					LOG.log(Level.INFO, "Telnet Stream Reader Thread is Initialized");
				}
				
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(telnet.getInputStream(), StandardCharsets.UTF_8));
						PrintStream out = new PrintStream(telnet.getOutputStream(), true, StandardCharsets.UTF_8)) {
					while (telnet.isConnected() && enabled) {
						while (reader.ready()) {
							if (!enabled) {
								break;
							}
							final String line = reader.readLine();
							if (debug) {
								LOG.log(Level.INFO, "Input Stream: {0}", line);
							}
							processJsonGpsdData(line);
							if (isEnableEvents()) {
								getGPSPropertyChangeSupport().firePropertyChange(AbstractGpsProcessor.RX_DATA, null, line);
							}
							if (!watchCommandSent && line.contains("VERSION")) {
								write(new String(WATCH_COMMAND) + "\r\n");
								watchCommandSent = true;
							}	
							while (!writeQueue.isEmpty()) {
								if (!enabled) {
									writeQueue.clear();
									break;
								}
								final String outString = writeQueue.poll();
								out.println(outString);
								out.flush();
								if (debug) {
									LOG.log(Level.INFO, "Output Stream {0}", out);
								}
								if (isEnableEvents()) {
									getGPSPropertyChangeSupport().firePropertyChange(AbstractGpsProcessor.TX_DATA, null, outString);
								}
							}
						}
					}
				} catch (NullPointerException ex) {
					if (debug) {
						LOG.log(Level.WARNING, ex.getMessage(), ex);	
					}
				} catch (SocketTimeoutException ex) {
					if (debug) {
						LOG.log(Level.WARNING, ex.getMessage(), ex);
					}
				} catch (IOException ex) {
					if (debug) {
						LOG.log(Level.WARNING, ex.getMessage(), ex);
					}
				}
				
				if (debug) {
					LOG.log(Level.INFO, "Client is Disconnected");
				}
			
			} catch (IOException ex) {
				if (debug) {
					LOG.log(Level.WARNING, ex.getMessage(), ex);
				}
			}
			
			getGPSPropertyChangeSupport().firePropertyChange(ONLINE, null, false);
		}
	}
	
	@Override
	public JPanel[] getConfigurationComponentArray() {
		return new TcpIpConfigurationComponent(netParams, MANUFACTURER + " " + MODEL_NAME + " TCP/IP Settings").
				getSettingsPanelArray();
	}
	
	private void initializeShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                close();
            }
        });
    }
}
