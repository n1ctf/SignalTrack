/*
 * Copyright 2009, Sven Strickroth <email@cs-ware.de>
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

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class DownloadConfiguration {
	private static final Logger log = Logger.getLogger(DownloadConfiguration.class.getName());

	public static final String TYPE = "Type";

	public abstract void save(Properties prop);

	public abstract void load(Properties prop);

	protected void setTemplateProperty(Properties prop, String key, String value) {
		log.log(Level.INFO, "setting property {0} to value {1}", new Object[] {key, value});
		prop.setProperty(key, value);
	}

	/**
	 * Getter for type
	 * 
	 * @return the type
	 */
	public abstract String getType();

	public abstract TileList getTileList(DownloadJob downloadJob);
}
