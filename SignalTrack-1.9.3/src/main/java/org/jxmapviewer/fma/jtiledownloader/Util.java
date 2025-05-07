/*
 * Copyright 2009, Sven Strickroth <email@cs-ware.de>
 * 
 * parsePasteUrl by:
 * Copyright 2008, Friedrich Maier
 * 
 * This file is part of JTileDownloader.
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

import java.util.LinkedList;

/**
 * Class with helper methods
 */
public class Util {
	/**
	 * Returns valid array of zoomlevels to download
	 * 
	 * @param selectedTileProvider
	 * @param zoomLevelString
	 * @return int[] zoomlevels
	 */

	private Util() {
		throw new IllegalStateException("Utility class");
	}

	public static int[] getOutputZoomLevelArray(TileProviderIf selectedTileProvider, String zoomLevelString) {
		final int minZoom = selectedTileProvider == null ? 0 : selectedTileProvider.getMinZoom();
		final int maxZoom = selectedTileProvider == null ? 20 : selectedTileProvider.getMaxZoom();
		final LinkedList<Integer> zoomLevels = new LinkedList<>();
		for (String zoomLevel : zoomLevelString.split(",")) {
			final int z1;
			final int z2;
			final int p = zoomLevel.indexOf('-');
			if (p > 0) {
				z1 = Integer.parseInt(zoomLevel.substring(0, p).trim());
				z2 = Integer.parseInt(zoomLevel.substring(p + 1).trim());
			} else {
				z1 = Integer.parseInt(zoomLevel.trim());
				z2 = z1;
			}
			for (int selectedZoom = z1; selectedZoom <= z2; selectedZoom++) {
				if (selectedZoom <= maxZoom && selectedZoom >= minZoom && !zoomLevels.contains(selectedZoom)) {
					zoomLevels.add(selectedZoom);
				}
			}
		}
		final int[] parsedLevels = new int[zoomLevels.size()];
		for (int i = 0; i < zoomLevels.size(); i++) {
			parsedLevels[i] = zoomLevels.get(i);
		}
		return parsedLevels;
	}

	/**
	 * @param tileServer
	 * @return tileProvider
	 */
	public static TileProviderIf getTileProvider(String tileServer) {
		final TileProviderIf[] tileProviders = new TileProviderList().getTileProviderList();
		for (TileProviderIf tileProvider : tileProviders) {
			if (tileProvider.getName().equalsIgnoreCase(tileServer)) {
				return tileProvider;
			}
		}
		return new GenericTileProvider(tileServer);
	}

	public static void parsePasteUrl(String url, TileListUrlSquare tileList) {
		if (url == null || url.isEmpty()) {
			tileList.setLatitude(0);
			tileList.setLongitude(0);
			return;
		}

		try {
			final int posLat = url.indexOf("lat=");
			String lat = url.substring(posLat);
			final int posLon = url.indexOf("lon=");
			String lon = url.substring(posLon);

			int posAnd = lat.indexOf('&');
			lat = lat.substring(4, posAnd).replace(',', '.');
			posAnd = lon.indexOf('&');
			lon = lon.substring(4, posAnd).replace(',', '.');

			if (!lat.isEmpty() && !lon.isEmpty()) {
				tileList.setLatitude(Double.parseDouble(lat));
				tileList.setLongitude(Double.parseDouble(lon));
			}
		} catch (NumberFormatException e) {
			tileList.setLatitude(0);
			tileList.setLongitude(0);
		}
	}
}
