/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.signaltrack;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;


import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.exception.WWAbsentRequirementException;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.CompassLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.ViewControlsLayer;
import gov.nasa.worldwind.layers.ViewControlsSelectListener;
import gov.nasa.worldwind.layers.WorldMapLayer;
import gov.nasa.worldwind.layers.placename.PlaceNameLayer;
import gov.nasa.worldwind.util.StatisticsPanel;
import gov.nasa.worldwind.util.StatusBar;
import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwind.view.orbit.BasicOrbitView;

/**
 * Provides a base application framework for simple WorldWind applications.
 * Examine other examples in this package to see how it's used.
 *
 * @version $Id: ApplicationTemplate.java 2115 2014-07-01 17:58:16Z tgaskins $
 */
public class ApplicationTemplate {
	private static final Logger log = Logger.getLogger(ApplicationTemplate.class.getName());

	public static class AppPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		protected transient WorldWindow wwd;
		protected transient BasicOrbitView view;
		protected StatusBar statusBar;
		protected transient ToolTipController toolTipController;
		protected transient HighlightController highlightController;

		public AppPanel(Dimension canvasSize, boolean includeStatusBar) {
			super(new BorderLayout());

			this.wwd = this.createWorldWindow();
			((Component) this.wwd).setPreferredSize(canvasSize);

			// Create the default model as described in the current WorldWind properties.
			final Model m = (Model) WorldWind.createConfigurationComponent(AVKey.MODEL_CLASS_NAME);
			this.wwd.setModel(m);

			view = new BasicOrbitView();

			wwd.setView(view);

			// Setup a select listener for the worldmap click-and-go feature
			this.wwd.addSelectListener(new ClickAndGoSelectListener(this.getWwd(), WorldMapLayer.class));

			this.add((Component) this.wwd, BorderLayout.CENTER);

			if (includeStatusBar) {
				this.statusBar = new StatusBar();
				this.add(statusBar, BorderLayout.PAGE_END);
				this.statusBar.setEventSource(wwd);
			}

			// Add controllers to manage highlighting and tool tips.
			this.toolTipController = new ToolTipController(this.getWwd(), AVKey.DISPLAY_NAME, null);
			this.highlightController = new HighlightController(this.getWwd(), SelectEvent.ROLLOVER);
		}

		protected WorldWindow createWorldWindow() {
			return new WorldWindowGLCanvas();
		}

		public WorldWindow getWwd() {
			return wwd;
		}

		public BasicOrbitView getView() {
			return view;
		}

		public StatusBar getStatusBar() {
			return statusBar;
		}
	}

	protected static class AppFrame extends JFrame {
		private static final long serialVersionUID = 1L;

		private Dimension canvasSize = new Dimension(800, 600);
		protected AppPanel wwjPanel;
		protected JPanel controlPanel;
		protected StatisticsPanel statsPanel;

		public AppFrame() {
			this.initialize(true, true, false);
		}

		public AppFrame(Dimension size) {
			this.canvasSize = size;
			this.initialize(true, true, false);
		}

		public AppFrame(boolean includeStatusBar, boolean includeLayerPanel, boolean includeStatsPanel) {
			this.initialize(includeStatusBar, includeLayerPanel, includeStatsPanel);
		}

		protected void initialize(boolean includeStatusBar, boolean includeLayerPanel, boolean includeStatsPanel) {
			// Create the WorldWindow.
			this.wwjPanel = this.createAppPanel(this.canvasSize, includeStatusBar);
			this.wwjPanel.setPreferredSize(canvasSize);

			// Put the pieces together.
			this.getContentPane().add(wwjPanel, BorderLayout.CENTER);
			if (includeLayerPanel) {
				this.controlPanel = new JPanel(new BorderLayout(10, 10));
				this.controlPanel.add(new FlatWorldPanel(this.getWwd()), BorderLayout.NORTH);
				this.getContentPane().add(this.controlPanel, BorderLayout.WEST);
			}

			if (includeStatsPanel || System.getProperty("gov.nasa.worldwind.showStatistics") != null) {
				this.statsPanel = new StatisticsPanel(this.wwjPanel.getWwd(), new Dimension(250, canvasSize.height));
				this.getContentPane().add(this.statsPanel, BorderLayout.EAST);
			}

			// Create and install the view controls layer and register a controller for it
			// with the World Window.
			final ViewControlsLayer viewControlsLayer = new ViewControlsLayer();
			insertBeforeCompass(getWwd(), viewControlsLayer);
			this.getWwd().addSelectListener(new ViewControlsSelectListener(this.getWwd(), viewControlsLayer));

			// Register a rendering exception listener that's notified when exceptions occur
			// during rendering.
			this.wwjPanel.getWwd().addRenderingExceptionListener(t -> {
				if (t instanceof WWAbsentRequirementException) {
					String message = "Computer does not meet minimum graphics requirements.\n";
					message += "Please install up-to-date graphics driver and try again.\n";
					message += "Reason: " + t.getMessage() + "\n";
					message += "This program will end when you press OK.";

					JOptionPane.showMessageDialog(AppFrame.this, message, "Unable to Start Program",
							JOptionPane.ERROR_MESSAGE);
				}
			});

			// Search the layer list for layers that are also select listeners and register
			// them with the World
			// Window. This enables interactive layers to be included without specific
			// knowledge of them here.
			this.wwjPanel.getWwd().getModel().getLayers().stream().filter(SelectListener.class::isInstance).forEach(layer -> this.getWwd().addSelectListener((SelectListener) layer));

			this.pack();

			// Center the application on the screen.
			WWUtil.alignComponent(null, this, AVKey.CENTER);
			this.setResizable(true);
		}

		protected AppPanel createAppPanel(Dimension canvasSize, boolean includeStatusBar) {
			return new AppPanel(canvasSize, includeStatusBar);
		}

		public Dimension getCanvasSize() {
			return canvasSize;
		}

		public AppPanel getWwjPanel() {
			return wwjPanel;
		}

		public WorldWindow getWwd() {
			return this.wwjPanel.getWwd();
		}

		public StatusBar getStatusBar() {
			return this.wwjPanel.getStatusBar();
		}

		public JPanel getControlPanel() {
			return this.controlPanel;
		}

		public StatisticsPanel getStatsPanel() {
			return statsPanel;
		}

		public void setToolTipController(ToolTipController controller) {
			if (this.wwjPanel.toolTipController != null) {
				this.wwjPanel.toolTipController.dispose();
			}

			this.wwjPanel.toolTipController = controller;
		}

		public void setHighlightController(HighlightController controller) {
			if (this.wwjPanel.highlightController != null) {
				this.wwjPanel.highlightController.dispose();
			}

			this.wwjPanel.highlightController = controller;
		}
	}

	public static void insertBeforeCompass(WorldWindow wwd, Layer layer) {
		// Insert the layer into the layer list just before the compass.
		int compassPosition = 0;
		final LayerList layers = wwd.getModel().getLayers();
		for (Layer l : layers) {
			if (l instanceof CompassLayer) {
				compassPosition = layers.indexOf(l);
			}
		}
		layers.add(compassPosition, layer);
	}

	public static void insertBeforePlacenames(WorldWindow wwd, Layer layer) {
		// Insert the layer into the layer list just before the placenames.
		int compassPosition = 0;
		final LayerList layers = wwd.getModel().getLayers();
		for (Layer l : layers) {
			if (l instanceof PlaceNameLayer) {
				compassPosition = layers.indexOf(l);
			}
		}
		layers.add(compassPosition, layer);
	}

	public static void insertAfterPlacenames(WorldWindow wwd, Layer layer) {
		// Insert the layer into the layer list just after the placenames.
		int compassPosition = 0;
		final LayerList layers = wwd.getModel().getLayers();
		for (Layer l : layers) {
			if (l instanceof PlaceNameLayer) {
				compassPosition = layers.indexOf(l);
			}
		}
		layers.add(compassPosition + 1, layer);
	}

	public static void insertBeforeLayerName(WorldWindow wwd, Layer layer, String targetName) {
		final LayerList layers = wwd.getModel().getLayers();
		// Insert the layer into the layer list just before the target layer.
		final int targetPosition = layers.stream().filter(l -> l.getName().contains(targetName)).findFirst().map(layers::indexOf).orElse(0);
		layers.add(targetPosition, layer);
	}

	static {
		System.setProperty("java.net.useSystemProxies", "true");
		if (Configuration.isMacOS()) {
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", "WorldWind Application");
			System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
			System.setProperty("apple.awt.brushMetalLook", "true");
		} else if (Configuration.isWindowsOS()) {
			System.setProperty("sun.awt.noerasebackground", "true"); // prevents flashing during window resizing
		}
	}

	public static AppFrame start(String appName, Class<?> appFrameClass) {
		try {
			final AppFrame frame = (AppFrame) appFrameClass.getDeclaredConstructor().newInstance();
			frame.setTitle(appName);
			frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			java.awt.EventQueue.invokeLater(() -> frame.setVisible(true));
			return frame;
		} catch (Exception e) {
			log.log(Level.WARNING, e.getMessage());
			return null;
		}
	}

	public static AppFrame start(String appName, Class<?> appFrameClass, Point2D center, double zoom) {
		try {
			final AppFrame frame = (AppFrame) appFrameClass.getDeclaredConstructor().newInstance();
			frame.setTitle(appName);
			frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			java.awt.EventQueue.invokeLater(() -> frame.setVisible(true));
			final Position pos = new Position(Angle.fromDegreesLatitude(center.getY()),
					Angle.fromDegreesLongitude(center.getX()), 0);
			frame.getWwjPanel().getView().setCenterPosition(pos);
			frame.getWwjPanel().getView().setZoom(zoom);
			return frame;
		} catch (Exception e) {
			log.log(Level.WARNING, e.getMessage());
			return null;
		}
	}

	public static void main(String[] args) {
		// Call the static start method like this from the main method of your derived
		// class.
		// Substitute your application's name for the first argument.
		start("WorldWind Application", AppFrame.class, new Point2D.Double(-83, 40), 6);
	}
}
