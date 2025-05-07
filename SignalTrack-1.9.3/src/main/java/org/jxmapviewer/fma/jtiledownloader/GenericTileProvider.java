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

import java.io.File;

public class GenericTileProvider implements TileProviderIf {
	protected String name;
	protected String url;

	protected GenericTileProvider() {
	}

	/**
	 * @param url
	 */
	public GenericTileProvider(String url) {
		if (!url.endsWith("/")) {
			url = url + "/";
		}
		this.name = url;
		this.url = url;
	}

	/**
	 * @param name
	 * @param url
	 */
	public GenericTileProvider(String name, String url) {
		this.name = name;
		this.url = url;
	}

	@Override
	public int getMaxZoom() {
		return 18;
	}

	@Override
	public int getMinZoom() {
		return 0;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getTileType() {
		return "png";
	}

	@Override
	public String getTileUrl(Tile tile) {
		return getTileServerUrl() + tile.getZ() + "/" + tile.getX() + "/" + tile.getY() + "." + getTileType();
	}

	@Override
	public String getTileServerUrl() {
		return url;
	}

	@Override
	public String getTileFilename(Tile tile) {
		return tile.getZ() + File.separator + tile.getX() + File.separator + tile.getY() + "." + getTileType();
	}
}
