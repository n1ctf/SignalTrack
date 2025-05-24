package org.jxmapviewer.cache;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Paths;

import java.nio.file.attribute.BasicFileAttributes;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import java.util.logging.Level;

import javax.imageio.ImageIO;

import java.util.logging.Logger;

import org.jxmapviewer.viewer.TileCache;

/**
 *    This class was designed by John: 
 *    What this is supposed to do is load all the map files saved to the HDD into memory.
 *    It reads the file names and last modified date into an array, sorted by last used.
 *    Then it loads them ordered by most recently used.
 *    Not sure if it helps performance, or not?
 *    I might need to put a limit on the number of files it tries to preload, 
 *    and make it optional in the settings.
 */

public class CachePreloader {
    private static final Logger LOG = Logger.getLogger(CachePreloader.class.getName());

	private TileCache tileCache;
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	private ExecutorService executor;
	
	public enum ChangedProperty{ LOADING, UPDATE, LOAD_COMPLETE } 
	
	public CachePreloader(File fileDirectoryPath, TileCache tileCache) {
		this.tileCache = tileCache; 
		executor = Executors.newCachedThreadPool();
		executor.execute(new RestoreDiskCache(fileDirectoryPath));
	}
	
	public static List<File> byAccessTime(File directory) {
		final List<File> fileList = Collections.synchronizedList(new CopyOnWriteArrayList<>());
		try {
			fileList.addAll(Arrays.asList(directory.listFiles()));
		} catch (NullPointerException ex) {
			LOG.log(Level.WARNING, ex.getLocalizedMessage());
		}
		return byAccessTime(fileList);
	}
	
	public static List<File> byAccessTime(List<File> fileList) {
		fileList.sort(new FileAccessTimeCompare());
		return fileList;
	}

	public static class FileAccessTimeCompare implements Comparator<File> {
		@Override
	    public int compare(File o1, File o2) {
	    	int i = 0;
	    	try {
	    		i = getLastAccessedTime(o2).compareTo(getLastAccessedTime(o1));
	    	} catch (NullPointerException ex) {
	    		LOG.log(Level.WARNING, ex.getLocalizedMessage());
	    	}
	    	return i;
	    }
	}
	
	public static Long getLastAccessedTime(File file) {
		Long lastAccessed = 0L;
		final BasicFileAttributes attributes;
		try {
			attributes = Files.readAttributes(Paths.get(file.getPath()), BasicFileAttributes.class);
			lastAccessed = attributes.lastAccessTime().toMillis();
		} catch (final IOException ex) {
			LOG.log(Level.WARNING, ex.getLocalizedMessage());
		}
		return lastAccessed;
	}
	
	public void cancel() {
		if (executor != null) {
			try {
				LOG.log(Level.INFO, "Initializing CachePreloader executor service termination....");
				executor.shutdown();
				executor.awaitTermination(3, TimeUnit.SECONDS);
				LOG.log(Level.INFO, "Initializing CachePreloader executor service has gracefully terminated");
			} catch (InterruptedException _) {
				executor.shutdownNow();
				LOG.log(Level.SEVERE, "Initializing CachePreloader executor service has timed out after 3 seconds of waiting to terminate processes.");
				Thread.currentThread().interrupt();
			}
		}
	}
	
	private final class RestoreDiskCache implements Runnable {
		private final File fileDirectoryPath;
	
		private RestoreDiskCache(File fileDirectoryPath) {
			this.fileDirectoryPath = fileDirectoryPath;
		}
		
		@Override
		public synchronized void run() {
			try {
				final File[] files = fileDirectoryPath.listFiles();
				Arrays.sort(files, Comparator.comparingLong(File::lastModified));

	 			int filesRestored = 0;

	 			LOG.log(Level.INFO, "Preloading {0} stored files into memory", files.length);
	 			
				if (files.length != 0) {
					// fire loading event with number of files to be pre-loaded to set up progress bar
					pcs.firePropertyChange(ChangedProperty.LOADING.name(), null, files.length);
					for (final File file : files) {
						if (file.isFile()) {
							try {
								tileCache.addToImageCache(FileBasedLocalCache.getLocalFile(fileDirectoryPath, 
										file.toURI().toURL()).toURI(), ImageIO.read(file));
								filesRestored ++;
								if (filesRestored % 10 == 0) { // fire event on every 10th round to update progress bar
									pcs.firePropertyChange(ChangedProperty.LOADING.name(), null, filesRestored);
									LOG.log(Level.INFO, "Completed preloading {0} files so far", filesRestored);
								}
							} catch (IOException ex) {
								LOG.log(Level.WARNING, ex.getLocalizedMessage());
							}
						}
				    }
				}
				// fire load complete event and tell how many files successfully restored to close progress bar
				pcs.firePropertyChange(ChangedProperty.LOAD_COMPLETE.name(), null, filesRestored);
				LOG.log(Level.INFO, "Completed preloading {0} files from disk to memory", filesRestored);
			} catch (final CancellationException ex) {
				LOG.log(Level.WARNING, ex.getLocalizedMessage());
			}
		}
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }
    
    public PropertyChangeListener[] getPropertyChangeListeners() {
    	return pcs.getPropertyChangeListeners();
    }
    
    public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
    	return pcs.getPropertyChangeListeners(propertyName);
    }
}
