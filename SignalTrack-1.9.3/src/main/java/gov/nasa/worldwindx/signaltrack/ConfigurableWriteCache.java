package gov.nasa.worldwindx.signaltrack;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.cache.BasicDataFileStore;

import java.io.File;

import org.w3c.dom.Node;

public class ConfigurableWriteCache extends BasicDataFileStore {

	private File path;
    
	/**
     * Causes the ConfigurableWriteCache to configure the environment for use
     * @param path
     */
    public void configure(File cacheRoot) {
        Configuration.setValue(AVKey.DATA_FILE_STORE_CLASS_NAME, ConfigurableWriteCache.class.getName());
        path = cacheRoot;
    }

	@Override
	public File getWriteLocation() {
		if (path == null) {
			path = new BasicDataFileStore().getWriteLocation();
		}
		return path;
	}

	public ConfigurableWriteCache() {
		buildWritePaths(null);
		buildReadPaths(null);
	}

	@Override
	protected void buildReadPaths(Node arg0) {
	    this.readLocations.add(0, this.writeLocation);
	}

	@Override
	protected void buildWritePaths(Node arg0) {
	    this.writeLocation = new StoreLocation(getWriteLocation());
	    this.readLocations.add(0, this.writeLocation);
	}
}
