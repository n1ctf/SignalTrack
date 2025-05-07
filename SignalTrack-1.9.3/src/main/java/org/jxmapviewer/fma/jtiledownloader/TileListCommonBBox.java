/*
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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TileListCommonBBox extends TileListCommon {
	private static final Logger log = Logger.getLogger(TileListCommonBBox.class.getName());
	private int[] _xTopLeft = new int[] { 0 };
	private int[] _yTopLeft = new int[] { 0 };
	private int[] _xBottomRight = new int[] { 0 };
	private int[] _yBottomRight = new int[] { 0 };

	@Override
	public List<Tile> getTileListToDownload() {
		final List<Tile> tilesToDownload = new ArrayList<>();

		for (int indexZoomLevel = 0; indexZoomLevel < getDownloadZoomLevels().length; indexZoomLevel++) {
			final int zoom = getDownloadZoomLevels()[indexZoomLevel];

			final int xStart = Math.min(_xTopLeft[indexZoomLevel], _xBottomRight[indexZoomLevel]);
			final int xEnd = Math.max(_xTopLeft[indexZoomLevel], _xBottomRight[indexZoomLevel]);

			final int yStart = Math.min(_yTopLeft[indexZoomLevel], _yBottomRight[indexZoomLevel]);
			final int yEnd = Math.max(_yTopLeft[indexZoomLevel], _yBottomRight[indexZoomLevel]);

			for (int downloadTileXIndex = xStart; downloadTileXIndex <= xEnd; downloadTileXIndex++) {
				for (int downloadTileYIndex = yStart; downloadTileYIndex <= yEnd; downloadTileYIndex++) {
					final String urlPathToFile = zoom + "/" + downloadTileXIndex + "/" + downloadTileYIndex + ".png";

					log.log(Level.FINE, "add {0} to download list", urlPathToFile);
					tilesToDownload.add(new Tile(downloadTileXIndex, downloadTileYIndex, zoom));
				}
			}

		}

		log.fine("finished");

		return tilesToDownload;

	}

	/**
	 * @param minLat
	 * @param minLon
	 * @param maxLat
	 * @param maxLon
	 */
	public void calculateTileValuesXY(double minLat, double minLon, double maxLat, double maxLon) {
		final int zoomLevelSize = getDownloadZoomLevels().length;

		_xTopLeft = new int[zoomLevelSize];
		_yTopLeft = new int[zoomLevelSize];
		_xBottomRight = new int[zoomLevelSize];
		_yBottomRight = new int[zoomLevelSize];

		for (int indexZoomLevel = 0; indexZoomLevel < zoomLevelSize; indexZoomLevel++) {
			setXTopLeft(calculateTileX(minLon, getDownloadZoomLevels()[indexZoomLevel]), indexZoomLevel);
			setYTopLeft(calculateTileY(maxLat, getDownloadZoomLevels()[indexZoomLevel]), indexZoomLevel);
			setXBottomRight(calculateTileX(maxLon, getDownloadZoomLevels()[indexZoomLevel]), indexZoomLevel);
			setYBottomRight(calculateTileY(minLat, getDownloadZoomLevels()[indexZoomLevel]), indexZoomLevel);

			log.log(Level.FINE, "XTopLeft = {0} ", getXTopLeft()[indexZoomLevel]);
			log.log(Level.FINE, "YTopLeft = {0} ", getYTopLeft()[indexZoomLevel]);
			log.log(Level.FINE, "XBottomRight = {0} ", getXBottomRight()[indexZoomLevel]);
			log.log(Level.FINE, "YBottomRight = {0} ", getYBottomRight()[indexZoomLevel]);
		}

	}

	/**
	 * Setter for topLeft
	 * 
	 * @param value the xTopLeft to set
	 */
	public final void initXTopLeft(int value) {
		_xTopLeft = new int[getDownloadZoomLevels().length];
		for (int index = 0; index < getDownloadZoomLevels().length; index++) {
			_xTopLeft[index] = value;

		}
	}

	/**
	 * Setter for topLeft
	 * 
	 * @param value the xTopLeft to set
	 */
	public final void initYTopLeft(int value) {
		_yTopLeft = new int[getDownloadZoomLevels().length];
		for (int index = 0; index < getDownloadZoomLevels().length; index++) {
			_yTopLeft[index] = value;

		}
	}

	/**
	 * Setter for BottomRight
	 * 
	 * @param value the xBottomRight to set
	 */
	public final void initXBottomRight(int value) {
		_xBottomRight = new int[getDownloadZoomLevels().length];
		for (int index = 0; index < getDownloadZoomLevels().length; index++) {
			_xBottomRight[index] = value;

		}
	}

	/**
	 * Setter for BottomRight
	 * 
	 * @param value the xBottomRight to set
	 */
	public final void initYBottomRight(int value) {
		_yBottomRight = new int[getDownloadZoomLevels().length];
		for (int index = 0; index < getDownloadZoomLevels().length; index++) {
			_yBottomRight[index] = value;

		}
	}

	/**
	 * Getter for xTopLeft
	 * 
	 * @return the xTopLeft
	 */
	public final int[] getXTopLeft() {
		return _xTopLeft.clone();
	}

	/**
	 * Setter for topLeft
	 * 
	 * @param topLeft the xTopLeft to set
	 * @param index
	 */
	public final void setXTopLeft(int topLeft, int index) {
		_xTopLeft[index] = topLeft;
	}

	/**
	 * Getter for yTopLeft
	 * 
	 * @return the yTopLeft
	 */
	public final int[] getYTopLeft() {
		return _yTopLeft.clone();
	}

	/**
	 * Setter for topLeft
	 * 
	 * @param topLeft the yTopLeft to set
	 * @param index
	 */
	public final void setYTopLeft(int topLeft, int index) {
		_yTopLeft[index] = topLeft;
	}

	/**
	 * Getter for xBottomRight
	 * 
	 * @return the xBottomRight
	 */
	public final int[] getXBottomRight() {
		return _xBottomRight.clone();
	}

	/**
	 * Setter for bottomRight
	 * 
	 * @param bottomRight the xBottomRight to set
	 * @param index
	 */
	public final void setXBottomRight(int bottomRight, int index) {
		_xBottomRight[index] = bottomRight;
	}

	/**
	 * Getter for yBottomRight
	 * 
	 * @return the yBottomRight
	 */
	public final int[] getYBottomRight() {
		return _yBottomRight.clone();
	}

	/**
	 * Setter for bottomRight
	 * 
	 * @param bottomRight the yBottomRight to set
	 * @param index
	 */
	public final void setYBottomRight(int bottomRight, int index) {
		_yBottomRight[index] = bottomRight;
	}

	/**
	 * @return tile count
	 */
	public int getTileCount() {
		int count = 0;
		for (int indexZoomLevels = 0; indexZoomLevels < getDownloadZoomLevels().length; indexZoomLevels++) {
			// WTF?
			count += Integer.parseInt(Integer.toString((Math.abs(getXBottomRight()[indexZoomLevels] - getXTopLeft()[indexZoomLevels]) + 1)
							* (Math.abs(getYBottomRight()[indexZoomLevels] - getYTopLeft()[indexZoomLevels]) + 1)));
		}

		return count;
	}

}
