/*
 * Copyright 2008, Friedrich Maier
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

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

public class ErrorTileListViewTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;
	private final transient List<Object> data;

	/**
	 * @param errorTileList
	 */
	public ErrorTileListViewTableModel(List<TileDownloadError> errorTileList) {
		data = new ArrayList<>();
		int count = 0;
		for (TileDownloadError tde : errorTileList) {
			final List<Object> rowData = new ArrayList<>();
			rowData.add(Integer.toString(++count));
			rowData.add(tde.getTile());
			rowData.add(tde.getResult().getMessage());
			data.add(rowData);
		}
	}

	/**
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		return 3;
	}

	/**
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	@Override
	public int getRowCount() {
		return data == null ? 0 : data.size();
	}

	/**
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Object getValueAt(int row, int column) {
		if (data == null) {
			return null;
		}

		final List<Object> rowData = (ArrayList<Object>) data.get(row);

		return rowData == null ? null : rowData.get(column);
	}

}
