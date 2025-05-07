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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

public class ErrorTileListView extends JDialog {
	private static final Logger log = Logger.getLogger(ErrorTileListView.class.getName());

	private static final String[] COL_HEADS = new String[] { "No", "Tile", "Error" };
	private static final int[] COL_SIZE = new int[] { 30, 300, 80 };

	private static final int VIEW_SIZE_X = 550;
	private static final int VIEW_SIZE_Y = 480;
	private static final String RETRY = "RETRY";
	private static final String CLOSE = "CLOSE";
	private static final long serialVersionUID = 1L;
	private final transient List<TileDownloadError> _errorTileList;

	private JTable _errorTable;
	private final JButton _close;
	private final JButton _retry;

	private int _exitCode = CODE_CLOSE;
	public static final int CODE_CLOSE = 0;
	public static final int CODE_RETRY = 1;

	/**
	 * @param errorTileList
	 * @throws HeadlessException
	 */
	public ErrorTileListView(List<TileDownloadError> errorTileList) {
		setTitle("ErrorTileListView");
		setModal(true);
		_errorTileList = errorTileList;

		setPreferredSize(new Dimension(VIEW_SIZE_X, VIEW_SIZE_Y));
		setLayout(new BorderLayout());
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		initTable();

		_close = new JButton("Close");
		_close.setPreferredSize(new Dimension(100, 25));
		_close.setActionCommand(CLOSE);
		_close.addActionListener(new MyActionListener());

		_retry = new JButton("Retry");
		_retry.setPreferredSize(new Dimension(100, 25));
		_retry.setActionCommand(RETRY);
		_retry.addActionListener(new MyActionListener());

		initializeView();
	}

	/**
	 * 
	 */
	private void initTable() {
		final DefaultTableColumnModel cm = new DefaultTableColumnModel();
		for (int i = 0; i < COL_HEADS.length; ++i) {
			final TableColumn col = new TableColumn(i, COL_SIZE[i]);
			col.setHeaderValue(COL_HEADS[i]);
			cm.addColumn(col);
		}

		final TableModel tm = new ErrorTileListViewTableModel(_errorTileList);

		_errorTable = new JTable(tm, cm);
		_errorTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
	}

	/**
	 * 
	 */
	private void initializeView() {
		final JScrollPane scrollPane = new JScrollPane(_errorTable);
		scrollPane.setPreferredSize(new Dimension(VIEW_SIZE_X - 20, VIEW_SIZE_Y - 90));
		scrollPane.getViewport().add(_errorTable, BorderLayout.CENTER);
		add(scrollPane);

		final JPanel panelButtons = new JPanel();
		panelButtons.add(_retry);
		panelButtons.add(_close);

		setLayout(new FlowLayout());
		add(panelButtons);

		pack();
	}

	/**
	 * Getter for errorTileList
	 * 
	 * @return the errorTileList
	 */
	public List<TileDownloadError> getErrorTileList() {
		return _errorTileList;
	}

	/**
	 * Setter for exitCode
	 * 
	 * @param exitCode the exitCode to set
	 */
	public void setExitCode(int exitCode) {
		_exitCode = exitCode;
	}

	/**
	 * Getter for exitCode
	 * 
	 * @return the exitCode
	 */
	public int getExitCode() {
		return _exitCode;
	}

	class MyActionListener implements ActionListener {

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			final String actionCommand = e.getActionCommand();
			log.log(Level.FINE, "button pressed -> {0}", actionCommand);

			if (actionCommand.equalsIgnoreCase(CLOSE)) {
				setExitCode(CODE_CLOSE);
			}
			if (actionCommand.equalsIgnoreCase(RETRY)) {
				setExitCode(CODE_RETRY);
			}
			_close.removeActionListener(this);
			_retry.removeActionListener(this);
			dispose();
		}
	}

}
