/*
 * Copyright 2008, Friedrich Maier
 * 
 * This file is part of JTileDownloader.
 * (see http://wiki.openstreetmap.org/index.php/JTileDownloader)
 *
 * JTileDownloader is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JTileDownloader is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy (see file COPYING.txt) of the GNU 
 * General Public License along with JTileDownloader.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.jxmapviewer.fma.jtiledownloader;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class AppConfiguration implements DownloadConfigurationSaverIf {

    private static final Logger LOG = Logger.getLogger(AppConfiguration.class.getName());

    private static final String APP_CONFIG_PROPERTIES = "appConfig.xml";

    private static final AppConfiguration instance = new AppConfiguration();
    private static final String USE_PROXY_SERVER = "UseProxyServer";
    private static final String PROXY_SERVER = "ProxyServer";
    private static final String PROXY_SERVER_PORT = "ProxyServerPort";
    private static final String USE_PROXY_SERVER_AUTH = "UseProxyServerAuth";
    private static final String PROXY_SERVER_USER = "ProxyServerUser";
    private static final String PROXY_SERVER_PASSWORD = "ProxyServerPassword";

    private static final String SHOW_TILE_PREVIEW = "ShowTilePreview";

    private static final String TILE_SERVER = "TileServer";
    private static final String LAST_ZOOM = "LastZoom";
    private static final String LAST_OUTPUTFOLDER = "LastOutoutFolder";

    private static final String DOWNLOAD_THREADS = "DownloadThreads";
    private static final String OVERWRITE_EXISTING_FILES = "OverwriteExistingFiles";

    private static final String MINIMUM_AGE_IN_DAYS = "MinimumAgeInDays";

    private static final String WAIT_AFTER_NR_TILES = "WaitAfterNrTiles";
    private static final String WAIT_SECONDS = "WaitSeconds";
    private static final String WAIT_NR_TILES = "WaitNrTiles";

    private static final String INPUT_PANEL_INDEX = "InputPanelIndex";
    private static final String TILE_SORTING_POLICY = "TileSortingPolicy";
    private final Properties prop = new Properties();

    private boolean _useProxyServer;
    private String _proxyServer = "";
    private String _proxyServerPort = "";
    private boolean _proxyServerRequireesAuthentitication;
    private String _proxyServerUser = "";
    private String _proxyServerPassword = "";

    private boolean _showTilePreview = true;

    private String _tileServer = "";
    private String _lastZoom = "12";
    private String _outputFolder = "tiles";

    private int _downloadThreads = 1;
    private boolean _overwriteExistingFiles = true;

    private int _minimumAgeInDays = 7;

    private boolean _waitAfterNrTiles = true;
    private int _waitSeconds = 2;
    private int _waitNrTiles = 10;

    private int _inputPanelIndex;
    private int _tileSortingPolicy = TileComparatorFactory.COMPARE_QUAD;

    private AppConfiguration() {
        loadFromFile();
    }

    public static AppConfiguration getInstance() {
        return instance;
    }

    public void saveToFile() {
        try (FileOutputStream fos = new FileOutputStream(APP_CONFIG_PROPERTIES)) {
            prop.storeToXML(fos, null);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Error storing preferences", e);
        }
    }

    private void setProperty(Properties prop, String key, String value) {
        LOG.log(Level.CONFIG, "setting property {0} to value {1}", new Object[]{key, value});
        prop.setProperty(key, value);
    }

    private void loadFromFile() {
        try (FileInputStream fis = new FileInputStream(APP_CONFIG_PROPERTIES)) {
            prop.loadFromXML(fis);
            prop.entrySet().forEach(key -> LOG.log(Level.CONFIG, "Property {0}={1}", new Object[]{key, prop.get(key)}));
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Error loading preferences", e);
        }

        _useProxyServer = Boolean.parseBoolean(prop.getProperty(USE_PROXY_SERVER, String.valueOf(_useProxyServer)));
        _proxyServer = prop.getProperty(PROXY_SERVER, _proxyServer);
        _proxyServerPort = prop.getProperty(PROXY_SERVER_PORT, _proxyServerPort);
        _proxyServerRequireesAuthentitication = Boolean.parseBoolean(prop.getProperty(USE_PROXY_SERVER_AUTH, String.valueOf(_proxyServerRequireesAuthentitication)));
        _proxyServerUser = prop.getProperty(PROXY_SERVER_USER, _proxyServerUser);
        _proxyServerPassword = prop.getProperty(PROXY_SERVER_PASSWORD, _proxyServerPassword);

        setShowTilePreview(Boolean.parseBoolean(prop.getProperty(SHOW_TILE_PREVIEW, String.valueOf(isShowTilePreview()))));

        setTileServer(prop.getProperty(TILE_SERVER, _tileServer));
        setLastZoom(prop.getProperty(LAST_ZOOM, _lastZoom));
        setOutputFolder(prop.getProperty(LAST_OUTPUTFOLDER, _outputFolder));

        setDownloadThreads(Integer.parseInt(prop.getProperty(DOWNLOAD_THREADS, String.valueOf(getDownloadThreads()))));
        setOverwriteExistingFiles(Boolean.parseBoolean(prop.getProperty(OVERWRITE_EXISTING_FILES, String.valueOf(isOverwriteExistingFiles()))));

        setMinimumAgeInDays(Integer.parseInt(prop.getProperty(MINIMUM_AGE_IN_DAYS, String.valueOf(getMinimumAgeInDays()))));

        setWaitAfterNrTiles(Boolean.parseBoolean(prop.getProperty(WAIT_AFTER_NR_TILES, String.valueOf(isWaitingAfterNrOfTiles()))));
        setWaitSeconds(Integer.parseInt(prop.getProperty(WAIT_SECONDS, String.valueOf(getWaitSeconds()))));
        setWaitNrTiles(Integer.parseInt(prop.getProperty(WAIT_NR_TILES, String.valueOf(getWaitNrTiles()))));

        setInputPanelIndex(Integer.parseInt(prop.getProperty(INPUT_PANEL_INDEX, String.valueOf(getInputPanelIndex()))));
        setTileSortingPolicy(Integer.parseInt(prop.getProperty(TILE_SORTING_POLICY, String.valueOf(getTileSortingPolicy()))));
    }

    /**
     * Getter for useProxyServer
     *
     * @return the useProxyServer
     */
    public boolean isUseProxyServer() {
        return _useProxyServer;
    }

    /**
     * Setter for useProxyServer
     *
     * @param useProxyServer the useProxyServer to set
     */
    public void setUseProxyServer(boolean useProxyServer) {
        _useProxyServer = useProxyServer;
        setProperty(prop, USE_PROXY_SERVER, String.valueOf(_useProxyServer));
    }

    /**
     * Getter for proxyServer
     *
     * @return the proxyServer
     */
    public String getProxyServer() {
        return _proxyServer;
    }

    /**
     * Setter for proxyServer
     *
     * @param proxyServer the proxyServer to set
     */
    public void setProxyServer(String proxyServer) {
        _proxyServer = proxyServer;
        setProperty(prop, PROXY_SERVER, _proxyServer);
    }

    /**
     * Getter for proxyServerPort
     *
     * @return the proxyServerPort
     */
    public String getProxyServerPort() {
        return _proxyServerPort;
    }

    /**
     * Setter for proxyServerPort
     *
     * @param proxyServerPort the proxyServerPort to set
     */
    public void setProxyServerPort(String proxyServerPort) {
        _proxyServerPort = proxyServerPort;
        setProperty(prop, PROXY_SERVER_PORT, _proxyServerPort);
    }

    /**
     * Getter for isProxyServerRequiresAuthentitication
     *
     * @return the isProxyServerRequiresAuthentitication
     */
    public boolean isProxyServerRequiresAuthentitication() {
        return _proxyServerRequireesAuthentitication;
    }

    /**
     * Setter for setProxyServerRequiresAuthentitication
     *
     * @param useProxyServerAuthentitication
     */
    public void setProxyServerRequiresAuthentitication(boolean useProxyServerAuthentitication) {
        _proxyServerRequireesAuthentitication = useProxyServerAuthentitication;
        setProperty(prop, USE_PROXY_SERVER_AUTH, String.valueOf(_proxyServerRequireesAuthentitication));
    }

    /**
     * Getter for proxyServerUser
     *
     * @return the proxyServerUser
     */
    public String getProxyServerUser() {
        return _proxyServerUser;
    }

    /**
     * Setter for proxyServerUser
     *
     * @param proxyServerUser the proxyServerUser to set
     */
    public void setProxyServerUser(String proxyServerUser) {
        _proxyServerUser = proxyServerUser;
        setProperty(prop, PROXY_SERVER_USER, _proxyServerUser);
    }

    /**
     * Getter for proxyServerPassword
     *
     * @return the proxyServerPassword
     */
    public String getProxyServerPassword() {
        return _proxyServerPassword;
    }

    /**
     * Setter for proxyServerPassword
     *
     * @param proxyServerPassword the proxyServerPassword to set
     */
    public void setProxyServerPassword(String proxyServerPassword) {
        _proxyServerPassword = proxyServerPassword;
        setProperty(prop, PROXY_SERVER_PASSWORD, _proxyServerPassword);
    }

    /**
     * Setter for showTilePreview
     *
     * @param showTilePreview the showTilePreview to set
     */
    public void setShowTilePreview(boolean showTilePreview) {
        _showTilePreview = showTilePreview;
        setProperty(prop, SHOW_TILE_PREVIEW, String.valueOf(_showTilePreview));
    }

    /**
     * Getter for showTilePreview
     *
     * @return the showTilePreview
     */
    public boolean isShowTilePreview() {
        return _showTilePreview;
    }

    /**
     * Setter for waitAfterNrTiles
     *
     * @param waitAfterNrTiles the waitAfterNrTiles to set
     */
    public void setWaitAfterNrTiles(boolean waitAfterNrTiles) {
        _waitAfterNrTiles = waitAfterNrTiles;
        setProperty(prop, WAIT_AFTER_NR_TILES, String.valueOf(_waitAfterNrTiles));
    }

    /**
     * Getter for isWaitingAfterNrOfTiles
     *
     * @return the isWaitingAfterNrOfTiles
     */
    public boolean isWaitingAfterNrOfTiles() {
        return _waitAfterNrTiles;
    }

    /**
     * Setter for waitSeconds
     *
     * @param waitSeconds the waitSeconds to set
     */
    public void setWaitSeconds(int waitSeconds) {
        if (waitSeconds > 0) {
            _waitSeconds = waitSeconds;
        }
        setProperty(prop, WAIT_SECONDS, String.valueOf(_waitSeconds));
    }

    /**
     * Getter for waitSeconds
     *
     * @return the waitSeconds
     */
    public int getWaitSeconds() {
        return _waitSeconds;
    }

    /**
     * Setter for waitNrTiles
     *
     * @param waitNrTiles the waitNrTiles to set
     */
    public void setWaitNrTiles(int waitNrTiles) {
        if (waitNrTiles > 0) {
            _waitNrTiles = waitNrTiles;
        }
        setProperty(prop, WAIT_NR_TILES, String.valueOf(_waitNrTiles));
    }

    /**
     * Getter for waitNrTiles
     *
     * @return the waitNrTiles
     */
    public int getWaitNrTiles() {
        return _waitNrTiles;
    }

    /**
     * Getter for inputPanelIndex
     *
     * @return the inputPanelIndex
     */
    public int getInputPanelIndex() {
        return _inputPanelIndex;
    }

    /**
     * Setter for inputPanelIndex
     *
     * @param inputPanelIndex the inputPanelIndex to set
     */
    public void setInputPanelIndex(int inputPanelIndex) {
        _inputPanelIndex = inputPanelIndex;
        setProperty(prop, INPUT_PANEL_INDEX, String.valueOf(_inputPanelIndex));
    }

    /**
     * Getter for overwriteExistingFiles
     *
     * @return the overwriteExistingFiles
     */
    public boolean isOverwriteExistingFiles() {
        return _overwriteExistingFiles;
    }

    /**
     * Setter for overwriteExistingFiles
     *
     * @param overwriteExistingFiles the _verwriteExistingFiles to set
     */
    public void setOverwriteExistingFiles(boolean overwriteExistingFiles) {
        _overwriteExistingFiles = overwriteExistingFiles;
        setProperty(prop, OVERWRITE_EXISTING_FILES, String.valueOf(_overwriteExistingFiles));
    }

    /**
     * Getter for minimumAgeInDays
     *
     * @return the minimumAgeInDays
     */
    public int getMinimumAgeInDays() {
        return _minimumAgeInDays;
    }

    /**
     * Setter for minimumAgeInDays
     *
     * @param minimumAgeInDays the minimumAgeInDays to set
     */
    public void setMinimumAgeInDays(int minimumAgeInDays) {
        if (minimumAgeInDays >= 0) {
            _minimumAgeInDays = minimumAgeInDays;
        }
        setProperty(prop, MINIMUM_AGE_IN_DAYS, String.valueOf(_minimumAgeInDays));
    }

    @Override
    public void loadDownloadConfig(DownloadConfiguration config) {
    	final Properties downloadProps = new Properties();
        prop.keySet().stream().map(String.class::cast).filter(propertyName -> 
        	(propertyName.startsWith(config.getType() + "."))).forEachOrdered(propertyName -> 
        		downloadProps.setProperty(propertyName.substring(config.getType().length() + 1), prop.getProperty(propertyName)));
        config.load(downloadProps);
    }

    /**
     * @param config
     * @see
     * org.openstreetmap.fma.jtiledownloader.config.DownloadConfigurationSaverIf#saveDownloadConfig(org.openstreetmap.fma.jtiledownloader.config.DownloadConfiguration)
     */
    @Override
    public void saveDownloadConfig(DownloadConfiguration config) {
    	final Properties downloadProps = new Properties();
        config.save(downloadProps);
        downloadProps.keySet().stream().map(String.class::cast).filter(propertyName -> 
        	(!propertyName.equals(DownloadConfiguration.TYPE))).forEachOrdered(propertyName -> 
            	prop.setProperty(config.getType() + "." + propertyName, downloadProps.getProperty(propertyName)));
    }

    /**
     * Getter for lastZoom
     *
     * @return the lastZoom
     */
    public String getLastZoom() {
        return _lastZoom;
    }

    /**
     * Setter for lastZoom
     *
     * @param lastZoom the lastZoom to set
     */
    public void setLastZoom(String lastZoom) {
        _lastZoom = lastZoom;
        setProperty(prop, LAST_ZOOM, _lastZoom);
    }

    /**
     * Getter for outputFolder
     *
     * @return the outputFolder
     */
    public String getOutputFolder() {
        return _outputFolder;
    }

    /**
     * Setter for outputFolder
     *
     * @param outputFolder the outputFolder to set
     */
    public void setOutputFolder(String outputFolder) {
        _outputFolder = outputFolder;
        setProperty(prop, LAST_OUTPUTFOLDER, _outputFolder);
    }

    /**
     * Getter for tileServer
     *
     * @return the tileServer
     */
    public String getTileServer() {
        return _tileServer;
    }

    /**
     * Setter for tileServer
     *
     * @param tileServer the tileServer to set
     */
    public void setTileServer(String tileServer) {
        _tileServer = tileServer;
        setProperty(prop, TILE_SERVER, _tileServer);
    }

    /**
     * Getter for downloadThreads
     *
     * @return the downloadThreads
     */
    public int getDownloadThreads() {
        return _downloadThreads;
    }

    /**
     * Setter for downloadThreads
     *
     * @param downloadThreads the downloadThreads to set
     */
    public void setDownloadThreads(int downloadThreads) {
        if (downloadThreads >= 1) {
            _downloadThreads = downloadThreads;
        }
        setProperty(prop, DOWNLOAD_THREADS, String.valueOf(_downloadThreads));
    }

    public int getTileSortingPolicy() {
        return _tileSortingPolicy;
    }

    public void setTileSortingPolicy(int tileSortingPolicy) {
        if (tileSortingPolicy >= 0 && tileSortingPolicy < TileComparatorFactory.COMPARE_COUNT) {
            this._tileSortingPolicy = tileSortingPolicy;
        }
        setProperty(prop, TILE_SORTING_POLICY, Integer.toString(this._tileSortingPolicy));
    }
}
