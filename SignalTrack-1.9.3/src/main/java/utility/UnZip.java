package utility;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UnZip implements Runnable {
	private static final Log LOG = LogFactory.getLog(UnZip.class);
	
	public static final String UNZIPPED = "UNZIPPED";
	public static final String CHECKSUM = "CHECKSUM";
 	public static final int BUFFER = 2048;
	private final File zippedFolder;
	private final File outputFolder;
	
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	public UnZip (File zippedFolder, File outputFolder) {
		this.zippedFolder = zippedFolder;
		this.outputFolder = outputFolder;
	}
	
	@Override
	public synchronized void run() {
		try (FileInputStream fis = new  FileInputStream(zippedFolder); 
				CheckedInputStream checksum = new CheckedInputStream(fis, new Adler32());
				ZipInputStream zis = new ZipInputStream(new BufferedInputStream(checksum))) {

    		ZipEntry zipEntry;
         
    		while((zipEntry = zis.getNextEntry()) != null) {
				final String name = outputFolder.getParent() + File.separator + zipEntry.getName();
				if(zipEntry.isDirectory()) {
					final File file = new File(name);
			        if (!file.mkdirs()) {
			        	LOG.info("Unable to create directory: " + file.toPath());
			        	return;
			        }
			    } else {
	    			int count;
	    			final byte[] data = new byte[BUFFER];
	    			final int i = name.lastIndexOf(File.separator);
	    	        final File file =  new File(name.substring(0, i) + File.separator + name.substring(i+1,name.length()));
	    	        
	    	        try (FileOutputStream fos = new FileOutputStream(file); 
	    	        		BufferedOutputStream bos =  new BufferedOutputStream(fos, BUFFER)) {
		    			while ((count = zis.read(data, 0, BUFFER)) != -1) {
		    				bos.write(data, 0, count);
		    			}
	    	        }
			    }
    		}
    		pcs.firePropertyChange(CHECKSUM, null, checksum.getChecksum().getValue());
    		pcs.firePropertyChange(UNZIPPED, null, outputFolder);
      } catch(final IOException e) {
         LOG.warn(e); 
      }
	}

	public void removeAllPropertyChangeListeners() {
        for (final PropertyChangeListener listener : pcs.getPropertyChangeListeners()) {
        	pcs.removePropertyChangeListener(listener);
        }
    }
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		if (!pcs.hasListeners(null)) {
			pcs.addPropertyChangeListener(listener);
		}
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }  
}
