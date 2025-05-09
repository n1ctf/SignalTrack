
package org.jxmapviewer.viewer;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jxmapviewer.cache.LocalCache;
import org.jxmapviewer.cache.NoOpLocalCache;
import org.jxmapviewer.util.ProjectProperties;
import org.jxmapviewer.viewer.util.GeoUtil;

/**
 * The <code>AbstractTileFactory</code> provides a basic implementation for the
 * TileFactory.
 */
public abstract class AbstractTileFactory extends TileFactory {
	private static final Log log = LogFactory.getLog(AbstractTileFactory.class);

	/**
	 * Note that the name and version are actually set by Gradle so there is no need
	 * to bump a version manually when new release is made.
	 */
	private static final String DEFAULT_USER_AGENT = ProjectProperties.INSTANCE.getName() + "/"
			+ ProjectProperties.INSTANCE.getVersion();

	private int threadPoolSize = 4;
	private String userAgent = DEFAULT_USER_AGENT;
	private ExecutorService service;

	// TODO the tile map should be static ALWAYS, regardless of the number
	// of GoogleTileFactories because each tile is, really, a singleton.
	private final LinkedHashMap<String, Tile> tileMap = new LinkedHashMap<String, Tile>() {
		private static final int CACHE_SIZE = 512;
		private static final long serialVersionUID = 1;

		@Override
		protected boolean removeEldestEntry(final Map.Entry<String, Tile> eldest) {
			return size() > CACHE_SIZE;
		}
	};

	private TileCache cache = new TileCache();

	/**
	 * Creates a new instance of DefaultTileFactory using the spcified
	 * TileFactoryInfo
	 * 
	 * @param info a TileFactoryInfo to configure this TileFactory
	 */
	public AbstractTileFactory(TileFactoryInfo info) {
		super(info);
	}

	/**
	 * Returns the tile that is located at the given tilePoint for this zoom. For
	 * example, if getMapSize() returns 10x20 for this zoom, and the tilePoint is
	 * (3,5), then the appropriate tile will be located and returned.
	 */
	@Override
	public Tile getTile(int x, int y, int zoom) {
		return getTile(x, y, zoom, true);
	}

	private Tile getTile(int tpx, int tpy, int zoom, boolean eagerLoad) {
		// wrap the tiles horizontally --> mod the X with the max width
		// and use that
		int tileX = tpx;// tilePoint.getX();
		final int numTilesWide = (int) getMapSize(zoom).getWidth();
		if (tileX < 0) {
			tileX = numTilesWide - (Math.abs(tileX) % numTilesWide);
		}

		tileX = tileX % numTilesWide;
		final int tileY = tpy;
		// TilePoint tilePoint = new TilePoint(tileX, tpy);
		final String url = getInfo().getTileUrl(tileX, tileY, zoom);// tilePoint);
		// System.out.println("loading: " + url);

		Tile.Priority pri = Tile.Priority.High;
		if (!eagerLoad) {
			pri = Tile.Priority.Low;
		}
		final Tile tile;
		// System.out.println("testing for validity: " + tilePoint + " zoom = " + zoom);
		if (!tileMap.containsKey(url)) {
			if (!GeoUtil.isValidTile(tileX, tileY, zoom, getInfo())) {
				tile = new Tile(tileX, tileY, zoom);
			} else {
				tile = new Tile(tileX, tileY, zoom, url, pri, this);
				startLoading(tile);
			}
			tileMap.put(url, tile);
		} else {
			tile = tileMap.get(url);

			// Remove the tile from the map if its loading failed. This will allow the
			// factory to try
			// and re-load the tile when it is requested sometime in the future.
			if (tile.loadingFailed()) {
				log.info("Removing from map: " + tile.getURL() + ", tile failed to load");
				tileMap.remove(url);
			}

			// if its in the map but is low and isn't loaded yet
			// but we are in high mode
			if (tile.getPriority() == Tile.Priority.Low && eagerLoad && !tile.isLoaded()) {
				// System.out.println("in high mode and want a low");
				// tile.promote();
				promote(tile);
			}
		}

		/*
		 * if (eagerLoad && doEagerLoading) { for (int i = 0; i<1; i++) { for (int j =
		 * 0; j<1; j++) { // preload the 4 tiles under the current one if(zoom > 0) {
		 * eagerlyLoad(tilePoint.getX()*2, tilePoint.getY()*2, zoom-1);
		 * eagerlyLoad(tilePoint.getX()*2+1, tilePoint.getY()*2, zoom-1);
		 * eagerlyLoad(tilePoint.getX()*2, tilePoint.getY()*2+1, zoom-1);
		 * eagerlyLoad(tilePoint.getX()*2+1, tilePoint.getY()*2+1, zoom-1); } } } }
		 */

		return tile;
	}

	/*
	 * private void eagerlyLoad(int x, int y, int zoom) { TilePoint t1 = new
	 * TilePoint(x,y); if(!isLoaded(t1,zoom)) { getTile(t1,zoom,false); } }
	 */

	// private boolean isLoaded(int x, int y, int zoom) {
	// String url = getInfo().getTileUrl(zoom,x,y);
	// return tileMap.containsKey(url);
	// }

	/**
	 * @return the tile cache
	 */
	public TileCache getTileCache() {
		return cache;
	}

	/**
	 * @param cache the tile cache
	 */
	public void setTileCache(TileCache cache) {
		this.cache = cache;
	}

	/** ==== threaded tile loading stuff === */
	/**
	 * Thread pool for loading the tiles
	 */
	private final BlockingQueue<Tile> tileQueue = new PriorityBlockingQueue<>(5, new Comparator<Tile>() {
		@Override
		public int compare(Tile o1, Tile o2) {
			if (o1.getPriority() == Tile.Priority.Low && o2.getPriority() == Tile.Priority.High) {
				return 1;
			}
			if (o1.getPriority() == Tile.Priority.High && o2.getPriority() == Tile.Priority.Low) {
				return -1;
			}
			return 0;

		}
	});

	private LocalCache localCache = new NoOpLocalCache();

	/**
	 * Subclasses may override this method to provide their own executor services.
	 * This method will be called each time a tile needs to be loaded.
	 * Implementations should cache the ExecutorService when possible.
	 * 
	 * @return ExecutorService to load tiles with
	 */
	protected synchronized ExecutorService getService() {
		if (service == null) {
			// System.out.println("creating an executor service with a threadpool of size "
			// + threadPoolSize);
			service = Executors.newFixedThreadPool(threadPoolSize, new ThreadFactory() {
				private int count = 0;

				@Override
				public Thread newThread(Runnable r) {
					final Thread t = new Thread(r, "tile-pool-" + count++);
					t.setPriority(Thread.MIN_PRIORITY);
					t.setDaemon(true);
					return t;
				}
			});
		}
		return service;
	}

	@Override
	public void dispose() {
		if (service != null) {
			service.shutdown();
			service = null;
		}
	}

	/**
	 * Set the number of threads to use for loading the tiles. This controls the
	 * number of threads used by the ExecutorService returned from getService().
	 * Note, this method should be called before loading the first tile. Calls after
	 * the first tile are loaded will have no effect by default.
	 * 
	 * @param size the thread pool size
	 */
	public void setThreadPoolSize(int size) {
		if (size <= 0) {
			throw new IllegalArgumentException(
					"size invalid: " + size + ". The size of the threadpool must be greater than 0.");
		}
		threadPoolSize = size;
	}

	/**
	 * Set the User agent that will be used when making a tile request.
	 *
	 * Some tile server usage policies requires application to identify itself, so
	 * please make sure that it is set properly.
	 *
	 * @param userAgent User agent to be used.
	 */
	public void setUserAgent(String userAgent) {
		if (userAgent == null || userAgent.isEmpty()) {
			throw new IllegalArgumentException("User agent can't be null or empty.");
		}

		this.userAgent = userAgent;
	}

	@Override
	protected synchronized void startLoading(Tile tile) {
		if (tile.isLoading()) {
			// System.out.println("already loading. bailing");
			return;
		}
		tile.setLoading(true);
		try {
			tileQueue.put(tile);
			getService().submit(createTileRunner(tile));
		} catch (InterruptedException ex) {
			ex.printStackTrace();
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * Subclasses can override this if they need custom TileRunners for some reason
	 * 
	 * @param tile the tile (unused!)
	 * @return the tile runner
	 */
	protected Runnable createTileRunner(Tile tile) {
		return new TileRunner();
	}

	/**
	 * Increase the priority of this tile so it will be loaded sooner.
	 * 
	 * @param tile the tile
	 */
	public synchronized void promote(Tile tile) {
		if (tileQueue.contains(tile)) {
			try {
				tileQueue.remove(tile);
				tile.setPriority(Tile.Priority.High);
				tileQueue.put(tile);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
				Thread.currentThread().interrupt();
			}
		}
	}

	@Override
	public void setLocalCache(LocalCache cache) {
		this.localCache = cache;
	}

	/**
	 * @return the number of pending (loading or queues) tiles
	 */
	@Override
	public synchronized int getPendingTiles() {
		return tileQueue.size();
	}

	/**
	 * An inner class which actually loads the tiles. Used by the thread queue.
	 * Subclasses can override this via {@link #createTileRunner(Tile)} if
	 * necessary.
	 */
	private class TileRunner implements Runnable {
		/**
		 * Gets the full URI of a tile.
		 * 
		 * @param tile the tile
		 * @throws URISyntaxException if the URI is invalid
		 * @return a URI for the tile
		 */
		protected URI getURI(Tile tile) throws URISyntaxException {
			return tile.getURL() == null ? null : new URI(tile.getURL());
		}

		@Override
		public void run() {
			/*
			 * Attempt to load the tile from its URL. If loading fails, retry two more
			 * times. If all attempts fail, nothing else is done. This way, if there is some
			 * kind of URL-specific failure, the pooled thread can try to load other tiles.
			 */
			final Tile tile = tileQueue.remove();
			tile.setLoadingFailed(false);

			int remainingAttempts = 3;
			while (!tile.isLoaded() && remainingAttempts > 0) {
				remainingAttempts--;
				try {
					final URI uri = getURI(tile);
					BufferedImage img = cache.get(uri);
					if (img == null) {
						final byte[] bimg = cacheInputStream(uri.toURL());
						// img = PaintUtils.loadCompatibleImage(new ByteArrayInputStream(bimg));
						img = ImageIO.read(new ByteArrayInputStream(bimg));
						cache.put(uri, bimg, img);
						img = cache.get(uri);
					}
					if (img == null) {
						// System.out.println("error loading: " + uri);
						log.info("Failed to load: " + uri);
					} else {
						final BufferedImage i = img;
						SwingUtilities.invokeAndWait(() -> {
							tile.image = new SoftReference<>(i);
							tile.setLoaded(true);
							fireTileLoadedEvent(tile);
						});
					}
				} catch (OutOfMemoryError memErr) {
					cache.needMoreMemory();
				} catch (FileNotFoundException fnfe) { // relevant for local URLs such as JAR/ZIP files only
					log.error("Unable to load tile: " + fnfe.getMessage());
					remainingAttempts = 0;
					tile.setLoadingFailed(true);
				} catch (InvocationTargetException e) {
					if (remainingAttempts == 0) {
						log.error("Failed to load a tile at URL: " + tile.getURL() + ", stopping", e);
						tile.setLoadingFailed(true);
					} else {
						log.warn("Failed to load a tile at URL: " + tile.getURL() + ", retrying", e);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
					Thread.currentThread().interrupt();
				} catch (IOException | URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			tile.setLoading(false);
		}

		private byte[] cacheInputStream(URL url) throws IOException {
			InputStream ins = localCache.get(url);
			if (ins == null) {
				final URLConnection connection = url.openConnection();
				connection.setRequestProperty("User-Agent", userAgent);
				addCustomRequestProperties(connection);
				ins = connection.getInputStream();
			}
			try {
				final byte[] data = readAllBytes(ins);
				localCache.put(url, new ByteArrayInputStream(data));
				return data;
			} finally {
				ins.close();
			}
		}

		private byte[] readAllBytes(InputStream ins) throws IOException {
			final ByteArrayOutputStream bout = new ByteArrayOutputStream();
			final byte[] buf = new byte[256];
			while (true) {
				final int n = ins.read(buf);
				if (n == -1) {
					break;
				}
				bout.write(buf, 0, n);
			}
			return bout.toByteArray();
		}
	}

	/**
	 * Adds custom request properties to the connection before sending the request.
	 * By default, no properties are added at all.
	 *
	 * @param connection connection for tile request
	 */
	protected void addCustomRequestProperties(URLConnection connection) {
		// no further headers by default
	}
}