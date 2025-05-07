/*
 * Copyright (C) 2013 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.signaltrack.layermanager;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import gov.nasa.worldwind.WorldWindow;


/**
 * Combines the layer manager and elevation model manager in a single frame.
 *
 * @author tag
 * @version $Id: LayerAndElevationManagerPanel.java 2109 2014-06-30 16:52:38Z
 *          tgaskins $
 */
public class LayerAndElevationManagerPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	protected LayerManagerPanel layerManagerPanel;
	protected ElevationModelManagerPanel elevationModelManagerPanel;

	public LayerAndElevationManagerPanel(WorldWindow wwd) {
		super(new BorderLayout(10, 10));
		
		this.layerManagerPanel = new LayerManagerPanel(wwd);
		this.add(layerManagerPanel, BorderLayout.CENTER);
		
		this.elevationModelManagerPanel = new ElevationModelManagerPanel(wwd);
		this.add(elevationModelManagerPanel, BorderLayout.SOUTH);
	}

	public void updateLayers(WorldWindow wwd) {
		this.layerManagerPanel.update(wwd);
	}

	public void updateElevations(WorldWindow wwd) {
		this.elevationModelManagerPanel.update(wwd);
	}

	public void update(WorldWindow wwd) {
		this.updateLayers(wwd);
		this.updateElevations(wwd);
	}
}
