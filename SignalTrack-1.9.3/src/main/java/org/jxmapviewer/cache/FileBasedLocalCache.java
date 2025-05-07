package org.jxmapviewer.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * A file/folder-based cache
 */
public class FileBasedLocalCache implements LocalCache {

	private static final Logger LOG = Logger.getLogger(FileBasedLocalCache.class.getName());

	private final File cacheDir;
	private final boolean checkForUpdates;

	/**
	 * @param cacheDir        the root cache folder
	 * @param checkForUpdates true, if the remote URL should be checked for updates
	 *                        first
	 */
	public FileBasedLocalCache(File cacheDir, boolean checkForUpdates) {
		this.cacheDir = cacheDir;
		this.checkForUpdates = checkForUpdates;
		if (!cacheDir.exists() && !cacheDir.mkdirs()) {
			LOG.log(Level.WARNING, "Unable to create directory: {0}", cacheDir.toPath());
		}
	}

	@Override
	public InputStream get(URL url) throws IOException {
		final File localFile = getLocalFile(cacheDir, url);

		if (!localFile.exists()) {
			// the file isn't already in our cache, return null
			return null;
		}

		if (checkForUpdates && isUpdateAvailable(url, localFile)) {
			// there is an update available, so don't return cached version
			return null;
		}

		return new FileInputStream(localFile);
	}

	@Override
    public void put(URL url, InputStream data) throws IOException {
		final File localFile = getLocalFile(cacheDir, url);
        LOG.log(Level.INFO, "local file to be put: {0}", localFile.getPath());
        if (localFile.getParentFile().exists() || localFile.getParentFile().mkdirs()) {
            try (FileOutputStream out = new FileOutputStream(localFile)) {
                copy(data, out);
            }
        } else {
            LOG.log(Level.WARNING, "Unable to create directory: {0}", cacheDir.toPath());
        }
    }

	/**
	 * Returns the local File corresponding to the given remote URI.
	 *
	 * @param cacheDir
	 * @param remoteUri the remote URI
	 * @return the corresponding local file
	 */
	public static File getLocalFile(File cacheDir, URL remoteUri) {
		final StringBuilder sb = new StringBuilder();

		final String host = remoteUri.getHost();
		final String query = remoteUri.getQuery();
		final String path = remoteUri.getPath();

		if (host != null) {
			sb.append(host);
		}
		if (path != null) {
			sb.append(path);
		}
		if (query != null) {
			sb.append('?');
			sb.append(query);
		}

		String name;

		final int maxLen = 250;

		name = sb.length() < maxLen ? sb.toString() : sb.substring(0, maxLen);

		name = name.replace('?', '$');
		name = name.replace('*', '$');
		name = name.replace(':', '$');
		name = name.replace('<', '$');
		name = name.replace('>', '$');
		name = name.replace('"', '$');

		return new File(cacheDir, name);
	}

	/**
	 * @param remoteUri the remote URI
	 * @param localFile the corresponding local file
	 * @return true if the resource at the given remote URI is newer than the
	 *         resource cached locally.
	 */
	private static boolean isUpdateAvailable(URL remoteUri, File localFile) {
		final URLConnection conn;
		try {
			conn = remoteUri.openConnection();
		} catch (IOException ex) {
			LOG.log(Level.WARNING, null, ex);
			return false;
		}

		if (!(conn instanceof HttpURLConnection)) {
			// don't bother with non-http connections
			return false;
		}

		long remoteLastMod = 0L;
		final HttpURLConnection httpconn = (HttpURLConnection) conn;
		// disable caching so we don't get in feedback loop with ResponseCache
		httpconn.setUseCaches(false);
		try {
			httpconn.connect();
			remoteLastMod = httpconn.getLastModified();
		} catch (UnknownHostException ex) {
			return false;
		} catch (IOException ex) {
			LOG.log(Level.WARNING, null, ex);
			return false;
		} finally {
			httpconn.disconnect();
		}

		return (remoteLastMod > localFile.lastModified());
	}

	// use Java7 functionality when upgrading
	private static long copy(InputStream source, OutputStream sink) throws IOException {
		long nread = 0L;
		final byte[] buf = new byte[8192];
		int n;
		while ((n = source.read(buf)) > 0) {
			sink.write(buf, 0, n);
			nread += n;
		}
		return nread;
	}
}
