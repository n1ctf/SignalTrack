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

import map.AbstractMap;

/**
 * Mapnik Tile Provider
 */
public class MapnikTileProvider extends RotatingTileProvider {
	private static final String[] SUBDOMAINS = { "a", "b", "c" };

	// String url = String.format("https://tile.openstreetmap.org/%d/%d/%d.png", z, x, y);
    // Path filePath = Paths.get("tiles", String.valueOf(z), String.valueOf(x), y + ".png");
	
	public MapnikTileProvider() {
		url = "https://{0}.tile.openstreetmap.org/";
	}

	@Override
	protected String[] getSubDomains() {
		return SUBDOMAINS;
	}

	@Override
	public String getName() {
		return AbstractMap.SignalTrackMapNames.OpenStreetMap.name();
	}
}
