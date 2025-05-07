package time;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingWorker;

import org.apache.commons.net.time.TimeTCPClient;

public class TimeTCP extends SwingWorker<Date, Void> implements AutoCloseable {
	private static final String[] DEFAULT_TIME_SERVER = { "pool.ntp.org" };
    
	public static final String NETWORK_TCP_TIME_AVAILABLE = "NETWORK_TCP_TIME_AVAILABLE";
    public static final int DEFAULT_TIMEOUT = 60000;
    private static final Logger LOG = Logger.getLogger(TimeTCP.class.getName());
    
	private String[] hosts = new String[1];
	private final int timeout;
	
	public TimeTCP() {
		this(DEFAULT_TIME_SERVER, DEFAULT_TIMEOUT);
	}
	
	public TimeTCP(String host, int timeout) {
		this(toStringArray(host) , timeout);
	}
	
	public TimeTCP(String[] hosts, int timeout) {
		this.hosts = hosts.clone();
		this.timeout = timeout;
		
		configureLogger();
	}
	
	private static String[] toStringArray(String host) {
    	return new String[] { host };
    }
	
    @Override
    protected Date doInBackground() {
        final TimeTCPClient client = new TimeTCPClient();
	    Date date = null;
	    client.setDefaultTimeout(timeout);
	    for (final String host : hosts) {
		    try {
		        client.connect(host);
		        date = client.getDate();
		    } catch (final IOException e) {
				LOG.log(Level.WARNING, e.getMessage());
			}
	    }
	    try {
			client.disconnect();
		} catch (final IOException e) {
			LOG.log(Level.WARNING, e.getMessage());
		}
	    return date;
    }
    @Override
    protected void done() {
    	try {
			firePropertyChange(NETWORK_TCP_TIME_AVAILABLE, null, get());
		} catch (InterruptedException | ExecutionException e) {
			Thread.currentThread().interrupt();
			LOG.log(Level.WARNING, e.getMessage());
		}
    }
		
    private void configureLogger() {
		Handler fh = null;
		Handler ch = null;
		try {
			fh = new FileHandler("%t/TimeTCP.log");
			ch = new ConsoleHandler();
			LOG.addHandler(fh);
			LOG.setLevel(Level.FINEST);
			LOG.addHandler(ch);
			LOG.setUseParentHandlers(false);
		} catch (SecurityException | IOException e) {
			LOG.log(Level.WARNING, e.getMessage());
		}
	}
    
    @Override
    public void close() {
    	for (Handler handler : LOG.getHandlers()) {
			LOG.removeHandler(handler);
	        handler.close();
		}
    }
}
