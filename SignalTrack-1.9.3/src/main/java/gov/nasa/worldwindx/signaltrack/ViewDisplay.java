/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.signaltrack;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.awt.ViewInputHandler;
import gov.nasa.worldwind.event.PositionEvent;
import gov.nasa.worldwind.event.PositionListener;
import gov.nasa.worldwind.event.RenderingEvent;
import gov.nasa.worldwind.event.RenderingListener;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.util.Logging;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;

// heavily modified from gov.nasa.worldwindx.examples.view.ViewSwitch for SignalTrack by John

public class ViewDisplay extends JPanel implements PositionListener, RenderingListener, ActionListener {
	private static final long serialVersionUID = 1L;

	// Units constants
	public static final String UNIT_METRIC = "gov.nasa.worldwind.StatusBar.Metric";
	public static final String UNIT_IMPERIAL = "gov.nasa.worldwind.StatusBar.Imperial";
	private static final double METER_TO_MILE = 0.000621371192;

	private transient WorldWindow eventSource;
	protected final JLabel latDisplay = new JLabel("");
	protected final JLabel lonDisplay = new JLabel(Logging.getMessage("term.OffGlobe"));
	protected final JLabel eleDisplay = new JLabel("");
	protected final JLabel headingDisplay = new JLabel("");
	protected final JLabel pitchDisplay = new JLabel("");
	private static final String elevationUnit = UNIT_METRIC;
	private static final String angleFormat = Angle.ANGLE_FORMAT_DD;

	// Viewer class information. view and inputHandler member variables are lazy
	// initialized.
	// Constructor gets the class names for the View, ViewInputHandler pair.
	public static class ViewerClass {
		private String viewClassName;
		private String inputHandlerClassName;
		private View view;
		private ViewInputHandler viewInputHandler;

		ViewerClass(String viewClassName, String inputHandlerClassName) {
			this.viewClassName = viewClassName;
			this.inputHandlerClassName = inputHandlerClassName;
			this.view = null;
			this.viewInputHandler = null;
		}

		public String getViewClassName() {
			return viewClassName;
		}

		public void setViewClassName(String viewClassName) {
			this.viewClassName = viewClassName;
		}

		public String getInputHandlerClassName() {
			return inputHandlerClassName;
		}

		public void setInputHandlerClassName(String inputHandlerClassName) {
			this.inputHandlerClassName = inputHandlerClassName;
		}

		public View getView() {
			return view;
		}

		public void setView(View view) {
			this.view = view;
		}

		public ViewInputHandler getViewInputHandler() {
			return viewInputHandler;
		}

		public void setViewInputHandler(ViewInputHandler viewInputHandler) {
			this.viewInputHandler = viewInputHandler;
		}
	}

	// Maps the combo box label to class information.
	public static class ViewerClassMap extends HashMap<String, ViewerClass> {
		private static final long serialVersionUID = 1L;
	}

	protected static final transient ViewerClassMap classNameList = new ViewerClassMap();

	// Orbit view class information
	public static final transient ViewerClass orbitViewer = new ViewerClass("gov.nasa.worldwind.view.orbit.BasicOrbitView",
			"gov.nasa.worldwind.view.orbit.OrbitViewInputHandler");
	// Fly viewer class information
	public static final transient ViewerClass flyViewer = new ViewerClass("gov.nasa.worldwind.view.firstperson.BasicFlyView",
			"gov.nasa.worldwind.view.firstperson.FlyViewInputHandler");

	// Viewer class array used for loop that initializes the map.
	private transient ViewerClass[] viewerClasses = { flyViewer, orbitViewer };

	// Viewer names for the combo box
	private String[] viewerNames = { "Fly", "Orbit" };
	private String currentName;
	private JComboBox<String> viewList;
	private transient WorldWindow wwd;

	public ViewDisplay(WorldWindow wwd) {
		super(new GridLayout(0, 1));

		this.wwd = wwd;

		// Initialize the viewer label -> viewer class map
		for (int i = 0; i < 2; i++) {
			classNameList.put(viewerNames[i], viewerClasses[i]);
		}

		setViewer(viewerClasses[0], false);
		currentName = viewerNames[0];
		viewList = new JComboBox<>(viewerNames);
		viewList.addActionListener(this);

		// Set up the viewer parameter display
		headingDisplay.setHorizontalAlignment(SwingConstants.CENTER);
		pitchDisplay.setHorizontalAlignment(SwingConstants.CENTER);
		latDisplay.setHorizontalAlignment(SwingConstants.CENTER);
		lonDisplay.setHorizontalAlignment(SwingConstants.CENTER);
		eleDisplay.setHorizontalAlignment(SwingConstants.CENTER);

		this.add(viewList);

		try {
			this.add(new GazetteerPanel(wwd, "gov.nasa.worldwind.poi.YahooGazetteer"), SwingConstants.CENTER);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Error creating Gazetteer");
		}

		this.add(latDisplay);
		this.add(lonDisplay);
		this.add(eleDisplay);
		this.add(headingDisplay);
		this.add(pitchDisplay);
	}

	public void setViewer(ViewerClass vc, boolean copyValues) {
		if (vc.view == null) {
			vc.view = (View) WorldWind.createComponent(vc.viewClassName);
			vc.viewInputHandler = vc.view.getViewInputHandler();
		}
		if (copyValues) {
			final View viewToCopy = wwd.getView();

			try {
				vc.view.copyViewState(viewToCopy);
				wwd.setView(vc.view);
			} catch (IllegalArgumentException iae) {
				JOptionPane.showMessageDialog(this, "Cannot switch to new view from this position/orientation");
				viewList.setSelectedItem(currentName);
			}
		} else {
			wwd.setView(vc.view);
		}
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == viewList) {
			final String classLabel = (String) viewList.getSelectedItem();
			final ViewerClass vc = classNameList.get(classLabel);

			setViewer(vc, true);
		}
	}

	@Override
	public void moved(PositionEvent event) {
		// NO OP
	}

	public void setEventSource(WorldWindow newEventSource) {
		if (this.eventSource != null) {
			this.eventSource.removePositionListener(this);
			this.eventSource.removeRenderingListener(this);
		}

		if (newEventSource != null) {
			newEventSource.addPositionListener(this);
			newEventSource.addRenderingListener(this);
		}

		this.eventSource = newEventSource;
	}

	protected String makeEyeAltitudeDescription(double metersAltitude) {
		final String s;
		final String altitude = Logging.getMessage("term.Altitude");
		if (UNIT_IMPERIAL.equals(elevationUnit)) {
			s = "%s %7d mi".formatted(altitude, (int) Math.round(metersAltitude * METER_TO_MILE));
		} else {
			s = "%s %7d m".formatted(altitude, (int) Math.round(metersAltitude));
		}
		return s;
	}

	protected String makeAngleDescription(String label, Angle angle) {
		final String s;
		if (Angle.ANGLE_FORMAT_DMS.equals(angleFormat)) {
			s = "%s %s".formatted(label, angle.toDMSString());
		} else {
			s = "%s %7.4f\u00B0".formatted(label, angle.degrees);
		}
		return s;
	}

	@Override
	public void stageChanged(RenderingEvent event) {
		if (!event.getStage().equals(RenderingEvent.BEFORE_BUFFER_SWAP)) {
			return;
		}

		EventQueue.invokeLater(() -> {
			if (eventSource.getView() != null && eventSource.getView().getEyePosition() != null) {
				final Position newPos = eventSource.getView().getEyePosition();

				if (newPos != null) {
					final String las = makeAngleDescription("Lat", newPos.getLatitude());
					final String los = makeAngleDescription("Lon", newPos.getLongitude());
					final String heading = makeAngleDescription("Heading", eventSource.getView().getHeading());
					final String pitch = makeAngleDescription("Pitch", eventSource.getView().getPitch());

					latDisplay.setText(las);
					lonDisplay.setText(los);
					eleDisplay.setText(makeEyeAltitudeDescription(newPos.getElevation()));
					headingDisplay.setText(heading);
					pitchDisplay.setText(pitch);
				} else {
					latDisplay.setText("");
					lonDisplay.setText(Logging.getMessage("term.OffGlobe"));
					eleDisplay.setText("");
					pitchDisplay.setText("");
					headingDisplay.setText("");
				}
			} else {
				eleDisplay.setText(Logging.getMessage("term.Altitude"));
			}
		});
	}
}
