package n1ctf;

import java.beans.PropertyChangeSupport;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FailTimer implements AutoCloseable {
	public static final String FAIL = "FAIL";
    public static final Logger LOG = Logger.getLogger(FailTimer.class.getName());

	private final long timeoutSeconds;
	private final PropertyChangeSupport pcs;
	private final ScheduledExecutorService scheduler;
	private int counter = 0;
	
	public FailTimer() {
		this(5);
	}
	
	public FailTimer(long timeoutSeconds) {
		this.timeoutSeconds = timeoutSeconds;
	
		pcs = new PropertyChangeSupport(this);
		scheduler = Executors.newSingleThreadScheduledExecutor();
		scheduler.scheduleAtFixedRate(new Update(), 0, 1000, TimeUnit.MILLISECONDS);
		
		LOG.log(Level.INFO, "FailTimer has been initialized for {0}", getCallerClassName());
	}
	
	private class Update implements Runnable {
		@Override
		public void run() {
	 		counter++;
			if (counter >= timeoutSeconds) {
				LOG.log(Level.INFO, "{0} failed to receive a reset after {1} seconds", new Object[] {getCallerClassName(), timeoutSeconds});
				pcs.firePropertyChange(FAIL, null, -1);
				counter = 0;
			}
		}
	}
	
	public void resetTimer() {
		counter = 0;
	}
	
	@Override
	public void close() {
		counter = 0;
		scheduler.shutdownNow();
		LOG.log(Level.INFO, "{0} failTimer has been closed", getCallerClassName());
	}
	
	public PropertyChangeSupport getPropertyChangeSupport() {
		return pcs;
	}
	
	public static String getCallerClassName() { 
        final StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
        for (int i=1; i<stElements.length; i++) {
            final StackTraceElement ste = stElements[i];
            if (!ste.getClassName().equals(FailTimer.class.getName()) && ste.getClassName().indexOf("java.lang.Thread")!=0) {
                return ste.getClassName();
            }
        }
        return null;
    }
	
	public static String getCallerCallerClassName() { 
	    final StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
	    String callerClassName = null;
	    for (int i=1; i<stElements.length; i++) {
	        final StackTraceElement ste = stElements[i];
	        if (!ste.getClassName().equals(FailTimer.class.getName())&& ste.getClassName().indexOf("java.lang.Thread")!=0) {
	            if (callerClassName==null) {
	                callerClassName = ste.getClassName();
	            } else if (!callerClassName.equals(ste.getClassName())) {
	                return ste.getClassName();
	            }
	        }
	    }
	    return null;
	}
}
