/*
 * Copyright 2009, Sven Strickroth <email@cs-ware.de>
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

import java.text.MessageFormat;

/**
 * Mapnik Tile Provider
 */
public class MapSurferTileProvider extends RotatingTileProvider {
	private static final String[] SUBDOMAINS = { "1" };
	private int serverNumber = -1;

	public MapSurferTileProvider() {
		url = "http://tiles{0}.mapsurfer.net/tms_r.ashx?x={2}&y={3}&z={1}";
	}

	/**
	 * @see org.jxmapviewer.fma.jtiledownloader.RotatingTileProvider#getSubDomains()
	 */
	@Override
	protected String[] getSubDomains() {
		return SUBDOMAINS;
	}

	/**
	 * @see org.jxmapviewer.fma.jtiledownloader.TileProviderIf#getTileUrl(Tile)
	 */
	@Override
	public String getTileUrl(Tile tile) {
		serverNumber = (serverNumber + 1) % getSubDomains().length;
		final Object[] ar = new Object[] { getSubDomains()[serverNumber], String.valueOf(tile.getZ()),
				String.valueOf(tile.getX()), String.valueOf(tile.getY()) };
		return MessageFormat.format(url, ar);
	}

	/**
	 * @see org.jxmapviewer.fma.jtiledownloader.GenericTileProvider#getName()
	 */
	@Override
	public String getName() {
		return "MapSurfer (Road)";
	}

	/**
	 * @see org.jxmapviewer.fma.jtiledownloader.GenericTileProvider#getMaxZoom()
	 */
	@Override
	public int getMaxZoom() {
		return 19;
	}
}
