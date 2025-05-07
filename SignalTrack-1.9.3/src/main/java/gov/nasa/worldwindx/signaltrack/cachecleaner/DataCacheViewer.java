/*
 * Copyright 2006-2009, 2017, 2020 United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 * 
 * The NASA World Wind Java (WWJ) platform is licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 * NASA World Wind Java (WWJ) also contains the following 3rd party Open Source
 * software:
 * 
 *     Jackson Parser – Licensed under Apache 2.0
 *     GDAL – Licensed under MIT
 *     JOGL – Licensed under  Berkeley Software Distribution (BSD)
 *     Gluegen – Licensed under Berkeley Software Distribution (BSD)
 * 
 * A complete listing of 3rd Party software notices and licenses included in
 * NASA World Wind Java (WWJ)  can be found in the WorldWindJava-v2.2 3rd-party
 * notices and licenses PDF found in code directory.
 */

package gov.nasa.worldwindx.signaltrack.cachecleaner;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.cache.BasicDataFileStore;
import gov.nasa.worldwind.cache.FileStore;
import gov.nasa.worldwindx.examples.util.FileStoreDataSet;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.File;
import java.util.Formatter;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

/**
 * The DataCacheViewer is a tool that allows the user to view and delete cached
 * WorldWind files based on how old they are. The utility shows the various
 * directories in the cache root, how large they each are, when they were last
 * used, and how many files exist in them that are older than a day, week, month
 * or year. It also allows the user to delete all files older than a specified
 * number of days, weeks, months or years.
 *
 * @author tag
 * @version $Id: DataCacheViewer.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class DataCacheViewer extends JDialog {
	private static final long serialVersionUID = 1L;
	
	private final JPanel panel;
	private final CacheTable table;
	private final JButton delBtn;
	private final JSpinner ageSpinner;
	private final JComboBox<String> ageUnit;
	private final JLabel deleteSizeLabel;

	public DataCacheViewer(File cacheRoot) {
		this.panel = new JPanel(new BorderLayout(5, 5));

		final JLabel rootLabel = new JLabel("Cache Root: " + cacheRoot.getPath());
		rootLabel.setBorder(new EmptyBorder(10, 15, 10, 10));
		this.panel.add(rootLabel, BorderLayout.NORTH);

		this.table = new CacheTable();
		this.table.setDataSets(cacheRoot.getPath(), FileStoreDataSet.getDataSets(cacheRoot));
		final JScrollPane sp = new JScrollPane(table);
		this.panel.add(sp, BorderLayout.CENTER);

		final JPanel pa = new JPanel(new BorderLayout(10, 10));
		pa.add(new JLabel("Delete selected data older than"), BorderLayout.WEST);
		this.ageSpinner = new JSpinner(new SpinnerNumberModel(6, 0, 10000, 1));
		this.ageSpinner.setToolTipText("0 selects the entire dataset regardless of age");
		final JPanel pas = new JPanel();
		pas.add(this.ageSpinner);
		pa.add(pas, BorderLayout.CENTER);
		this.ageUnit = new JComboBox<>(new String[] { "Hours", "Days", "Weeks", "Months", "Years" });
		this.ageUnit.setSelectedItem("Months");
		this.ageUnit.setEditable(false);
		pa.add(this.ageUnit, BorderLayout.EAST);

		final JPanel pb = new JPanel(new BorderLayout(5, 10));
		this.deleteSizeLabel = new JLabel("Total to delete: 0 MB");
		pb.add(this.deleteSizeLabel, BorderLayout.WEST);
		this.delBtn = new JButton("Delete");
		this.delBtn.setEnabled(false);
		final JButton quitButton = new JButton("Quit");
		final JPanel pbb = new JPanel();
		pbb.add(this.delBtn);
		pb.add(pbb, BorderLayout.CENTER);
		pbb.add(quitButton);

		final JPanel pc = new JPanel(new BorderLayout(5, 10));
		pc.add(pa, BorderLayout.WEST);
		pc.add(pb, BorderLayout.EAST);

		final JPanel ctlPanel = new JPanel(new BorderLayout(10, 10));
		ctlPanel.setBorder(new EmptyBorder(10, 10, 20, 10));
		ctlPanel.add(pc, BorderLayout.CENTER);

		this.panel.add(ctlPanel, BorderLayout.SOUTH);

		this.ageUnit.addItemListener(_ -> update());

		this.ageSpinner.addChangeListener(_ -> update());

		this.table.getSelectionModel().addListSelectionListener(_ -> update());

		this.delBtn.addActionListener(_ -> {
			panel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			final Thread t = new Thread(() -> {
				try {
					final List<FileStoreDataSet> dataSets = table.getSelectedDataSets();
					final int age = Integer.parseInt(ageSpinner.getValue().toString());
					final String unit = getUnitKey();

					dataSets.forEach(ds -> {
						ds.deleteOutOfScopeFiles(unit, age, false);
						if (ds.getSize() == 0) {
							table.deleteDataSet(ds);
							ds.delete(false);
						}
					});
				} finally {
					update();
					SwingUtilities.invokeLater(() -> panel.setCursor(Cursor.getDefaultCursor()));
				}
			});
			t.start();
		});

		quitButton.addActionListener(_ -> dispose());
		
		this.setPreferredSize(new Dimension(800, 300));
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		this.getContentPane().add(panel, BorderLayout.CENTER);
		this.pack();

		// Center the application on the screen.
		final Dimension prefSize = this.getPreferredSize();
		final Dimension parentSize;
		final Point parentLocation = new Point(0, 0);
		parentSize = Toolkit.getDefaultToolkit().getScreenSize();
		final int x = parentLocation.x + (parentSize.width - prefSize.width) / 2;
		final int y = parentLocation.y + (parentSize.height - prefSize.height) / 2;
		this.setLocation(x, y);
		this.setTitle("WorldWind Cache Manager Tool");
		this.setVisible(true);
	}

	protected void update() {
		final List<FileStoreDataSet> dataSets = this.table.getSelectedDataSets();
		final int age = Integer.parseInt(this.ageSpinner.getValue().toString());

		if (dataSets.isEmpty()) {
			this.deleteSizeLabel.setText("Total to delete: 0 MB");
			this.delBtn.setEnabled(false);
			return;
		}

		final String unit = this.getUnitKey();

		long totalSize = 0;
		for (FileStoreDataSet ds : dataSets) {
			totalSize += ds.getOutOfScopeSize(unit, age);
		}

		final Formatter formatter = new Formatter();
		formatter.format("%5.1f", (totalSize) / 1e6);
		this.deleteSizeLabel.setText("Total to delete: " + formatter.toString() + " MB");

		formatter.close();
		
		this.delBtn.setEnabled(true);
	}

	protected String getUnitKey() {
		String unit = null;
		final String unitString = (String) this.ageUnit.getSelectedItem();
		switch (unitString) {
			case "Hours" -> unit = FileStoreDataSet.HOUR;
			case "Days" -> unit = FileStoreDataSet.DAY;
			case "Weeks" -> unit = FileStoreDataSet.WEEK;
			case "Months" -> unit = FileStoreDataSet.MONTH;
			case "Years" -> unit = FileStoreDataSet.YEAR;
			default -> unit = null;
		}

		return unit;
	}

	static {
		if (Configuration.isMacOS()) {
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", "WorldWind Cache Cleaner");
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			final JFrame frame = new JFrame();
			frame.setPreferredSize(new Dimension(800, 300));
			frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

			final FileStore store = new BasicDataFileStore();
			final File cacheRoot = store.getWriteLocation();
			final DataCacheViewer viewerPanel = new DataCacheViewer(cacheRoot);
			frame.getContentPane().add(viewerPanel.panel, BorderLayout.CENTER);
			frame.pack();

			// Center the application on the screen.
			final Dimension prefSize = frame.getPreferredSize();
			final Dimension parentSize;
			final java.awt.Point parentLocation = new java.awt.Point(0, 0);
			parentSize = Toolkit.getDefaultToolkit().getScreenSize();
			final int x = parentLocation.x + (parentSize.width - prefSize.width) / 2;
			final int y = parentLocation.y + (parentSize.height - prefSize.height) / 2;
			frame.setLocation(x, y);
			frame.setVisible(true);
		});
	}
}
