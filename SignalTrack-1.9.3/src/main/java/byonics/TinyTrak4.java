package byonics;

import java.beans.PropertyChangeSupport;

import java.nio.charset.StandardCharsets;

import java.util.logging.Level;
import java.util.logging.Logger;

import tty.AbstractTeletypeController;
import tty.AbstractTeletypeController.BaudRate;
import tty.AbstractTeletypeController.DataBits;
import tty.AbstractTeletypeController.Parity;
import tty.AbstractTeletypeController.StopBits;

/**
 *
 * @author n1ctf
 */
public class TinyTrak4 implements AutoCloseable {

    public enum Event {
        NEW_DATA
    }

    public static final boolean DEFAULT_DEBUG_MODE = true;
    
    private static final Logger LOG = Logger.getLogger(TinyTrak4.class.getName());
    private static final PropertyChangeSupport pcs = new PropertyChangeSupport(TinyTrak4.class);

    private final AbstractTeletypeController tty = AbstractTeletypeController.getTTyPortInstance(AbstractTeletypeController.getCatalogMap().getKey("JSSC TTY Port v2.9.5"), false);
    
    private boolean debug;

    public TinyTrak4(String portDescriptor) {
        this(portDescriptor, DEFAULT_DEBUG_MODE);
    }
    
    public TinyTrak4(String portDescriptor, boolean debug) {
        this.debug = debug;
        initializeShutdownHook();
    
    	tty.setBaudRate(BaudRate.BAUDRATE_2400);
    	tty.setDataBits(DataBits.DATABITS_8);
    	tty.setStopBits(StopBits.STOPBITS_1);
    	tty.setParity(Parity.NONE);
    	tty.setDTR(true);
    	tty.setRTS(true);
    	
        tty.openPort(portDescriptor);
        
        tty.getPropertyChangeSupport().addPropertyChangeListener(event -> {
        	if (AbstractTeletypeController.TTYEvents.RX_DATA.name().equals(event.getPropertyName())) {
        		pcs.firePropertyChange(Event.NEW_DATA.name(), null, event.getNewValue());
        	}
        });
    }
    
    private void initializeShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                close();
            }
        });
    }

    @Override
    public void close() {
        tty.closePort();
    }

    public void writeBytes(String str) {
        if (tty.isPortOpen()) {
	        if (debug) {
	        	LOG.log(Level.INFO, "Write to Comm Port: {0}", str);
	        }
	        final byte[] buffer = str.getBytes(StandardCharsets.UTF_8);
	        tty.write(buffer);
        } else {
            LOG.log(Level.INFO, "ComPort is CLOSED: {0} not written", str);
        }
    }
}
