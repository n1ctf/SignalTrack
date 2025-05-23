/*
 * Copyright 2008, Friedrich Maier
 * Copyright 2009 - 2010, Sven Strickroth <email@cs-ware.de>
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
 * 
 * 
 */

package org.jxmapviewer.fma.jtiledownloader;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;

import java.util.logging.Level;
import java.util.logging.Logger;

public class TileListDownloader {
	private static final Logger LOG = Logger.getLogger(TileListDownloader.class.getName());
	private static final int MAX_RETRIES = 5;
	
	private LinkedList<Tile> _tilesToDownload;
	private String _downloadPath;
	private final TileProviderIf _tileProvider;
	private final ArrayList<TileListDownloaderThread> downloaderThreads = new ArrayList<>();

	private TileDownloaderListener _listener;

	private boolean paused;

	private int _numberOfTilesToDownload;
	private int _errorCount;
	private int _updatedTilesCount;
	private final LinkedList<TileDownloadError> _errorTileList = new LinkedList<>();

	

	/**
	 * @param downloadPath
	 * @param tilesToDownload
	 * @param tileProvider
	 */
	public TileListDownloader(String downloadPath, TileList tilesToDownload, TileProviderIf tileProvider) {
		setDownloadPath(downloadPath);
		setTilesToDownload(tilesToDownload.getTileListToDownload());
		_tileProvider = tileProvider;

		if (AppConfiguration.getInstance().isUseProxyServer()) {
			if (AppConfiguration.getInstance().isProxyServerRequiresAuthentitication()) {
				ProxyConnection.setProxyData(AppConfiguration.getInstance().getProxyServer(),
						Integer.parseInt(AppConfiguration.getInstance().getProxyServerPort()),
						AppConfiguration.getInstance().getProxyServerUser(),
						AppConfiguration.getInstance().getProxyServerPassword());
			} else {
				ProxyConnection.setProxyData(AppConfiguration.getInstance().getProxyServer(),
						Integer.parseInt(AppConfiguration.getInstance().getProxyServerPort()));
			}
		}
	}

	public void start() {
		paused = false;
		for (int i = 0; i < AppConfiguration.getInstance().getDownloadThreads(); i++) {
			final TileListDownloaderThread downloaderThread = new TileListDownloaderThread();
			downloaderThread.start();
			downloaderThreads.add(downloaderThread);
		}
	}

	public void abort() {
		downloaderThreads.forEach(TileListDownloaderThread::interrupt);
		if (paused && runningThreads() == 0) {
			fireDownloadStoppedEvent();
		}
	}

	public void pause() {
		paused = true;
		downloaderThreads.forEach(TileListDownloaderThread::interrupt);
	}

	public void setListener(TileDownloaderListener listener) {
		_listener = listener;
	}

	private void fireDownloadStoppedEvent() {
		if (_listener != null && isLastThread()) {
			_listener.downloadStopped(_numberOfTilesToDownload - _tilesToDownload.size(), _numberOfTilesToDownload);
		}
	}

	private synchronized int runningThreads() {
		int runningThreads = 0;
		for (TileListDownloaderThread downloaderThread : downloaderThreads) {
			if (downloaderThread.isAlive()) {
				runningThreads++;
			}
		}
		return runningThreads;
	}

	/**
	 * @return is the current thread is the last thread
	 */
	private boolean isLastThread() {
		return (runningThreads() <= 1);
	}

	/**
	 * Setter for downloadPath
	 * 
	 * @param downloadPath the downloadPath to set
	 */
	public void setDownloadPath(String downloadPath) {
		_downloadPath = downloadPath;
	}

	/**
	 * Getter for downloadPath
	 * 
	 * @return the downloadPath
	 */
	public String getDownloadPath() {
		return _downloadPath;
	}

	/**
	 * Setter for tilesToDownload
	 * 
	 * @param tilesToDownload the tilesToDownload to set
	 */
	public synchronized void setTilesToDownload(List<Tile> tilesToDownload) {
		if (tilesToDownload != null) {
			_tilesToDownload = new LinkedList<>(tilesToDownload);
			final int tileSortingPolicy = AppConfiguration.getInstance().getTileSortingPolicy();
			if (tileSortingPolicy > 0) {
				_tilesToDownload.sort(TileComparatorFactory.getComparator(tileSortingPolicy));
			}
		} else {
			_tilesToDownload = new LinkedList<>();
		}
		_numberOfTilesToDownload = _tilesToDownload.size();
	}

	public class TileListDownloaderThread extends Thread {
		/**
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			Tile tileToDownload = null;
			int tilesDownloaded = 0;
			while ((tileToDownload = getTilesToDownload()) != null) {
				if (tilesDownloaded > 0 && AppConfiguration.getInstance().isWaitingAfterNrOfTiles()
						&& (tilesDownloaded) % (AppConfiguration.getInstance().getWaitNrTiles()) == 0) {
					final int waitSeconds = AppConfiguration.getInstance().getWaitSeconds();
					final String waitMsg = "Waiting " + waitSeconds + " sec to resume";
					LOG.info(waitMsg);
					fireWaitResume(waitMsg);
					Thread.ofVirtual().start(() -> {
					    try {
					        Thread.sleep(1000);
					    } catch (InterruptedException e) {
					        Thread.currentThread().interrupt();
					    }
					});
				}

				if (interrupted()) {
					requeueTile(tileToDownload);
					if (paused) {
						fireDownloadPausedEvent();
					} else {
						fireDownloadStoppedEvent();
					}
					return;
				}

				LOG.log(Level.INFO, "Downloading tile {0} to {1}",
						new Object[] {tileToDownload, getDownloadPath()});

				final TileDownloadResult result = doDownload(tileToDownload);

				if (result.getCode() != TileDownloadResult.CODE_OK) {
					addedTileDownloadError(result, tileToDownload);
					fireErrorOccuredEvent(tileToDownload);
				}

				tilesDownloaded++;
			}
			fireDownloadCompleteEvent();
		}

		private synchronized void addedTileDownloadError(TileDownloadResult result, Tile tileToDownload) {
			_errorCount++;
			final TileDownloadError error = new TileDownloadError();
			error.setTile(tileToDownload);
			error.setResult(result);
			_errorTileList.add(error);
		}

		private TileDownloadResult doDownload(Tile tileToDownload) {
			TileDownloadResult result = new TileDownloadResult();

			URL url = null;
			try {
				url = new URI(_tileProvider.getTileUrl(tileToDownload)).toURL();
			} catch (MalformedURLException e) {
				result.setCode(TileDownloadResult.CODE_MALFORMED_URL_EXECPTION);
				result.setMessage(TileDownloadResult.MSG_MALFORMED_URL_EXECPTION);
				return result;
			} catch (URISyntaxException e) {
				LOG.log(Level.WARNING, null, e);
			}

			final String fileName = getDownloadPath() + File.separator + _tileProvider.getTileFilename(tileToDownload);
			final String filePath = getDownloadPath() + File.separator + tileToDownload.getPath();

			final File downloadDir = new File(filePath);
			
			if (!downloadDir.exists()) {
				LOG.log(Level.INFO, "Creating download directory {0}", downloadDir.getPath());
				if (!downloadDir.mkdirs()) {
					LOG.log(Level.WARNING, "Unable to create download directory: {0}", downloadDir.toPath());
				}
			}

			for (int retries = 0; retries < MAX_RETRIES; retries++) {
				result = doSingleDownload(fileName, url);
				if (result.getCode() == TileDownloadResult.CODE_OK) {
					fireDownloadedTileEvent(fileName, result.isUpdatedTile());
					break;
				} else if (result.getCode() == TileDownloadResult.CODE_HTTP_500) {
					// HTTP-500 Error - retry again
					fireWaitHttp500ErrorToResume("HTTP/500 - wait 10 sec. to retry");
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						break;
					}
				} else
				// unknown error
				{
					return result;
				}

			}

			return result;
		}

		/**
		 * @param fileName
		 * @param url
		 * @return TileDownloadResult
		 */
		private TileDownloadResult doSingleDownload(String fileName, URL url) {
			final TileDownloadResult result = new TileDownloadResult();

			final File file = new File(fileName);
			if (file.exists()) {
				final Calendar cal = Calendar.getInstance();
				cal.add(Calendar.HOUR, -24 * AppConfiguration.getInstance().getMinimumAgeInDays());
				if (!AppConfiguration.getInstance().isOverwriteExistingFiles()) {
					result.setCode(TileDownloadResult.CODE_OK);
					result.setMessage(TileDownloadResult.MSG_OK);
					return result;
				} else if (file.lastModified() >= cal.getTimeInMillis()) {
					result.setCode(TileDownloadResult.CODE_OK);
					result.setMessage(TileDownloadResult.MSG_OK);
					return result;
				}
			}

			HttpURLConnection urlConnection = null;
			boolean imageNeedsToBeErased = false;
			InputStream inputStream = null;
			try (BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file))) {
				urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.setRequestProperty("User-Agent", "SignalTrack/" + Constants.VERSION);
				urlConnection.setUseCaches(false);

				// if last modified since would work like this and you would get a 304 response
				// code
				// but it seems as if no tile server supports this so far
				urlConnection.setIfModifiedSince(file.lastModified());

				final long lastModified = urlConnection.getLastModified();

				// do not overwrite file if not changed: required because setIfModifiedSince
				// doesn't work for tile-servers atm
				// Mapnik-Servers do not send LastModified-headers...
				if (file.length() > 0 && (lastModified != 0 && file.lastModified() >= lastModified)
						|| urlConnection.getResponseCode() == HttpURLConnection.HTTP_NOT_MODIFIED) {
					result.setCode(TileDownloadResult.CODE_OK);
					result.setMessage(TileDownloadResult.MSG_OK);
					return result;
				}

				inputStream = urlConnection.getInputStream();

				imageNeedsToBeErased = true;
				int temp = inputStream.read();
				while (temp != -1) {
					bufferedOutputStream.write(temp);
					temp = inputStream.read();
				}
				bufferedOutputStream.flush();
				imageNeedsToBeErased = false;
			} catch (FileNotFoundException e) {
				LOG.log(Level.SEVERE, "Error downloading tile", e);
				result.setCode(TileDownloadResult.CODE_FILENOTFOUND);
				result.setMessage(TileDownloadResult.MSG_FILENOTFOUND);
				return result;
			} catch (UnknownHostException e) {
				LOG.log(Level.SEVERE, "Could not find host for a tile", e);
				result.setCode(TileDownloadResult.CODE_UNKNOWN_HOST_EXECPTION);
				result.setMessage(TileDownloadResult.MSG_UNKNOWN_HOST_EXECPTION);
				return result;
			} catch (IOException e) {
				LOG.log(Level.SEVERE, "Error downloading tile", e);
				try {
					if (urlConnection != null && urlConnection.getResponseCode() == 500) {
						result.setCode(TileDownloadResult.CODE_HTTP_500);
						result.setMessage(TileDownloadResult.MSG_HTTP_500);
						return result;
					} else {
						result.setCode(TileDownloadResult.CODE_UNKNOWN_ERROR);
						if (urlConnection != null && !urlConnection.getResponseMessage().isEmpty()) {
							result.setMessage(urlConnection.getResponseMessage());
						} else {
							result.setMessage(TileDownloadResult.MSG_UNKNOWN_ERROR);
						}
						return result;
					}
				} catch (IOException e1) {
					LOG.log(Level.SEVERE, "Error getting response message", e1);
					result.setCode(TileDownloadResult.CODE_UNKNOWN_ERROR);
					result.setMessage(TileDownloadResult.MSG_UNKNOWN_ERROR);
					return result;
				} catch (Exception th) {
					LOG.log(Level.SEVERE, "Error getting response message", th);
					result.setCode(TileDownloadResult.CODE_UNKNOWN_ERROR);
					result.setMessage(TileDownloadResult.MSG_UNKNOWN_ERROR);
					return result;
				}
			} finally {
				if (imageNeedsToBeErased) {
					LOG.info("Deleting incomplete tile " + file.getPath());
					try {
						Files.delete(file.toPath());
					} catch (Exception e) {
						LOG.log(Level.SEVERE, "Could not delete", e);
					}
				}
				try {
					if (inputStream != null) {
						inputStream.close();
					}
				} catch (IOException e) {
					LOG.log(Level.SEVERE, "Could not close InputStream", e);
				}
			}

			result.setCode(TileDownloadResult.CODE_OK);
			result.setMessage(TileDownloadResult.MSG_OK);
			result.setUpdatedTile(true);
			return result;
		}

		/**
		 * @param fileName
		 * @param updatedTile
		 */
		private void fireDownloadedTileEvent(String fileName, boolean updatedTile) {
			if (updatedTile) {
				increaseUpdatedCount();
			}
			if (_listener != null) {
				_listener.downloadedTile(_numberOfTilesToDownload - _tilesToDownload.size(), _numberOfTilesToDownload,
						fileName, _updatedTilesCount, updatedTile);
			}
		}

		private synchronized void increaseUpdatedCount() {
			_updatedTilesCount++;
		}

		/**
		 * @param tile
		 */
		private void fireErrorOccuredEvent(Tile tile) {
			if (_listener != null) {
				_listener.errorOccured(_numberOfTilesToDownload - _tilesToDownload.size(), _numberOfTilesToDownload,
						tile);
			}
		}

		private void fireDownloadPausedEvent() {
			if (_listener != null && isLastThread()) {
				_listener.downloadPaused(_numberOfTilesToDownload - _tilesToDownload.size(), _numberOfTilesToDownload);
			}
		}

		private void fireDownloadCompleteEvent() {
			if (_listener != null && isLastThread()) {
				_listener.downloadComplete(_errorCount, new ArrayList<>(_errorTileList),
						_updatedTilesCount);
			}
		}

		/**
		 * 
		 */
		private void fireWaitResume(String message) {
			if (_listener != null) {
				_listener.setInfo(message);
			}
		}

		/**
		 * 
		 */
		private void fireWaitHttp500ErrorToResume(String message) {
			if (_listener != null) {
				_listener.setInfo(message);
			}
		}

		/**
		 * Get tile to download
		 * 
		 * @return a tile
		 */
		private synchronized Tile getTilesToDownload() {
			return _tilesToDownload.poll();
		}

		private synchronized void requeueTile(Tile tile) {
			_tilesToDownload.add(tile);
		}

	}
}
