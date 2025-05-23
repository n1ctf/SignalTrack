package org.jxmapviewer.fma.jtiledownloader;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DownloadJob implements DownloadConfigurationSaverIf {
	private static final Logger log = Logger.getLogger(DownloadJob.class.getName());

	private final Properties prop = new Properties();

	private String _outputZoomLevels = "";
	private String _tileServer = "";
	private String _outputLocation = "";
	private String _type = "";

	private static final String OUTPUT_ZOOM_LEVEL = "OutputZoomLevel";
	private static final String TILE_SERVER = "TileServer";
	private static final String OUTPUTLOCATION = "OutputLocation";

	public static final String TYPE = "Type";

	public DownloadJob() {}

	/**
	 * constructor setting propertyFileName
	 * 
	 * @param propertyFileName
	 */
	public DownloadJob(String propertyFileName) {
		loadFromFile(propertyFileName);
	}

	public void saveToFile(String propertyFileName) {
		setTemplateProperty(prop, OUTPUT_ZOOM_LEVEL, _outputZoomLevels);
		setTemplateProperty(prop, TILE_SERVER, _tileServer);
		setTemplateProperty(prop, OUTPUTLOCATION, _outputLocation);
		
		try (FileOutputStream fos = new FileOutputStream(propertyFileName);) {
			prop.storeToXML(fos, null); 
		} catch (IOException e) {
			log.log(Level.SEVERE, "Error saving job to file {0}", propertyFileName);
		}
	}

	private void loadFromFile(String fileName) {
		try (FileInputStream fis = new FileInputStream(fileName);){
			prop.loadFromXML(fis);
		} catch (IOException e) {
			log.log(Level.SEVERE, "Error loading job from file {0}", fileName);
		}

		_type = prop.getProperty(TYPE, "");
		_outputZoomLevels = prop.getProperty(OUTPUT_ZOOM_LEVEL, "12");
		_tileServer = prop.getProperty(TILE_SERVER, "");
		_outputLocation = prop.getProperty(OUTPUTLOCATION, "tiles");
	}

	protected void setTemplateProperty(Properties prop, String key, String value) {
		log.log(Level.CONFIG, "setting property {0} to value {1}", new Object[] { key, value });
		prop.setProperty(key, value);
	}

	/**
	 * Getter for outputZoomLevel
	 * 
	 * @return the outputZoomLevel
	 */
	public final String getOutputZoomLevels() {
		return _outputZoomLevels;
	}

	/**
	 * Setter for outputZoomLevel
	 * 
	 * @param outputZoomLevel the outputZoomLevel to set
	 */
	public final void setOutputZoomLevels(String outputZoomLevel) {
		_outputZoomLevels = outputZoomLevel;
	}

	/**
	 * Getter for tileServer
	 * 
	 * @return the tileServer
	 */
	public final String getTileServer() {
		return _tileServer;
	}

	public final TileProviderIf getTileProvider() {
		return Util.getTileProvider(getTileServer());
	}

	/**
	 * Setter for tileServer
	 * 
	 * @param tileServer the tileServer to set
	 */
	public final void setTileServer(String tileServer) {
		_tileServer = tileServer;
	}

	/**
	 * Getter for outputLocation
	 * 
	 * @return the outputLocation
	 */
	public final String getOutputLocation() {
		return _outputLocation;
	}

	/**
	 * Setter for outputLocation
	 * 
	 * @param outputLocation the outputLocation to set
	 */
	public final void setOutputLocation(String outputLocation) {
		_outputLocation = outputLocation;
	}

	/**
	 * Getter for type
	 * 
	 * @return the type
	 */
	public final String getType() {
		return _type;
	}

	@Override
	public void saveDownloadConfig(DownloadConfiguration config) {
		config.save(prop);
	}

	@Override
	public void loadDownloadConfig(DownloadConfiguration config) {
		config.load(prop);
	}
}
