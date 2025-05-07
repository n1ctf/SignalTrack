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
package gov.nasa.worldwindx.signaltrack.measuretool;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.layers.TerrainProfileLayer;
import gov.nasa.worldwind.util.measure.MeasureTool;
import gov.nasa.worldwind.util.measure.MeasureToolController;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class MeasureToolDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	
	private int lastTabIndex = -1;
	private final JTabbedPane tabbedPane = new JTabbedPane();
	private final transient TerrainProfileLayer profile;
	private final transient PropertyChangeListener measureToolListener = new MeasureToolListener();
	private final transient WorldWindow wwd;
	
	public MeasureToolDialog(WorldWindow wwd, TerrainProfileLayer profile) {
		this.wwd = wwd;
		this.profile = profile;
		
		setAlwaysOnTop(true);
		setTitle("Measure Tool");
		
		profile.setEnabled(true);
		profile.setEventSource(wwd);
		profile.setFollow(TerrainProfileLayer.FOLLOW_PATH);
		profile.setShowProfileLine(false);

		// Add + tab
		tabbedPane.add(new JPanel());
		tabbedPane.setTitleAt(0, "+");
		tabbedPane.addChangeListener(_ -> {
			if (tabbedPane.getSelectedIndex() == 0) {
				// Add new measure tool in a tab when '+' selected
				final MeasureTool measureTool = new MeasureTool(wwd);
				measureTool.setController(new MeasureToolController());
				tabbedPane.add(new MeasureToolPanel(wwd, measureTool));
				tabbedPane.setTitleAt(tabbedPane.getTabCount() - 1, Integer.toString((tabbedPane.getTabCount() - 1)));
				tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
				switchMeasureTool();
			} else {
				switchMeasureTool();
			}
		});

		// Add measure tool control panel to tabbed pane
		final MeasureTool measureTool = new MeasureTool(wwd);
		measureTool.setController(new MeasureToolController());
		tabbedPane.add(new MeasureToolPanel(wwd, measureTool));
		tabbedPane.setTitleAt(1, "1");
		tabbedPane.setSelectedIndex(1);
		
		switchMeasureTool();

		add(tabbedPane);
		pack();
		setVisible(true);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
	}

	public MeasureTool getMeasureTool() {
		return ((MeasureToolPanel) tabbedPane.getComponentAt(lastTabIndex)).getMeasureTool();
	}
	
	private class MeasureToolListener implements PropertyChangeListener {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			// Measure shape position list changed - update terrain profile
			if (event.getPropertyName().equals(MeasureTool.EVENT_POSITION_ADD)
					|| event.getPropertyName().equals(MeasureTool.EVENT_POSITION_REMOVE)
					|| event.getPropertyName().equals(MeasureTool.EVENT_POSITION_REPLACE)) {
				updateProfile(((MeasureTool) event.getSource()));
			} 
		}
	}

	private void switchMeasureTool() {
		// Disarm last measure tool when changing tab and switching tool
		if (lastTabIndex != -1) {
			final MeasureTool mt = ((MeasureToolPanel) tabbedPane.getComponentAt(lastTabIndex)).getMeasureTool();
			mt.setArmed(false);
			mt.removePropertyChangeListener(measureToolListener);
		}
		// Update terrain profile from current measure tool
		lastTabIndex = tabbedPane.getSelectedIndex();
		final MeasureTool mt = ((MeasureToolPanel) tabbedPane.getComponentAt(lastTabIndex)).getMeasureTool();
		mt.addPropertyChangeListener(measureToolListener);
		updateProfile(mt);
	}

	private void updateProfile(MeasureTool mt) {
		final ArrayList<? extends LatLon> positions = mt.getPositions();
		if (positions != null && positions.size() > 1) {
			profile.setPathPositions(positions);
			profile.setEnabled(true);
		} else {
			profile.setEnabled(false);
		}
		wwd.redraw();
	}

}
