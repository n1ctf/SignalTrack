
package org.jxmapviewer.viewer;

/**
 * Notified when the status of a tile has changed
 * 
 * @author Martin Steiger
 */
public interface TileListener {
	/**
	 * Notification when a tile is loaded
	 * 
	 * @param tile the tile
	 */
	void tileLoaded(Tile tile);

	/**
	 * Notification when all tiles are loaded
	 */
	void allTilesLoaded();
	
	/**
	 * Notification when all tiles are loading
	 */
	void tilesLoading();
}
