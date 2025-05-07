/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.signaltrack;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.exception.NoItemException;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.poi.*;
import gov.nasa.worldwind.view.orbit.OrbitView;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

/**
 * Gazetteer search panel that allows the user to enter a search term in a text
 * field. When a search is performed the view will animate to the top search
 * result.
 *
 * @author tag
 * @version $Id: GazetteerPanel.java 1171 2013-02-11 21:45:02Z dcollins $
 */
@SuppressWarnings("unchecked")
public class GazetteerPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(GazetteerPanel.class.getName());
	private final transient WorldWindow wwd;
	private transient Gazetteer gazeteer;
	private final JPanel resultsPanel;
	private final JComboBox<PointOfInterest> resultsBox;

	/**
	 * Create a new panel.
	 *
	 * @param wwd                World window to animate when a search is performed.
	 * @param gazetteerClassName Name of the gazetteer class to instantiate. If this
	 *                           parameter is {@code null} a {@link YahooGazetteer}
	 *                           is instantiated.
	 *
	 * @throws IllegalAccessException if the Gazetteer class does not expose a
	 *                                publicly accessible no-arg constructor.
	 * @throws InstantiationException if an exception occurs while instantiating the
	 *                                the gazetteer class.
	 * @throws ClassNotFoundException if the gazetteer class cannot be found.
	 */
	public GazetteerPanel(final WorldWindow wwd, String gazetteerClassName)
			throws IllegalAccessException, InstantiationException, ClassNotFoundException {
		super(new BorderLayout());

		if (gazetteerClassName != null) {
			this.gazeteer = this.constructGazetteer(gazetteerClassName);
		} else {
			this.gazeteer = new YahooGazetteer();
		}

		this.wwd = wwd;

		// The label
		final URL imageURL = this.getClass().getResource("/images/32x32-icon-earth.png");
		final ImageIcon icon = new ImageIcon(imageURL);
		final JLabel label = new JLabel(icon);
		label.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));

		// The text field
		final JTextField field = new JTextField("Name or Lat,Lon?");
		field.addActionListener(actionEvent -> EventQueue.invokeLater(() -> {
			try {
				handleEntryAction(actionEvent);
			} catch (NoItemException e) {
				log.log(Level.WARNING, e.getMessage());
				JOptionPane.showMessageDialog(GazetteerPanel.this,
								"Location not available \"" + (field.getText() != null ? field.getText() : "") + "\"\n"
										+ "(" + e.getMessage() + ")",
								"Location Not Available", JOptionPane.ERROR_MESSAGE);
			} catch (IllegalArgumentException e) {
				log.log(Level.WARNING, e.getMessage());
				JOptionPane.showMessageDialog(GazetteerPanel.this, "Error parsing input \""
										+ (field.getText() != null ? field.getText() : "") + "\"\n" + e.getMessage(),
								"Lookup Failure", JOptionPane.ERROR_MESSAGE);
			} catch (Exception e) {
				log.log(Level.WARNING, e.getMessage());
				JOptionPane.showMessageDialog(GazetteerPanel.this, "Error looking up \""
										+ (field.getText() != null ? field.getText() : "") + "\"\n" + e.getMessage(),
								"Lookup Failure", JOptionPane.ERROR_MESSAGE);
			}
		}));

		// Enclose entry field in an inner panel in order to control spacing/padding
		final JPanel fieldPanel = new JPanel(new BorderLayout());
		fieldPanel.add(field, BorderLayout.CENTER);
		fieldPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15));
		fieldPanel.setPreferredSize(new Dimension(100, 30));

		// Put everything together
		this.add(label, BorderLayout.WEST);
		this.add(fieldPanel, BorderLayout.CENTER);

		resultsPanel = new JPanel(new FlowLayout());
		resultsPanel.add(new JLabel("Results: "));
		resultsBox = new JComboBox<>();
		resultsBox.setPreferredSize(new Dimension(300, 30));
		resultsBox.addActionListener(actionEvent -> EventQueue.invokeLater(() -> {
			final JComboBox<PointOfInterest> cb = (JComboBox<PointOfInterest>) actionEvent.getSource();
			final PointOfInterest selectedPoi = (PointOfInterest) cb.getSelectedItem();
			moveToLocation(selectedPoi);
		}));
		resultsPanel.add(resultsBox);
		resultsPanel.setVisible(false);
		this.add(resultsPanel, BorderLayout.EAST);
	}

	private Gazetteer constructGazetteer(String className)
			throws ClassNotFoundException, IllegalAccessException, InstantiationException {
		if (className == null || className.isEmpty()) {
			throw new IllegalArgumentException("Gazetteer class name is null");
		}

		final Class<?> c = Class.forName(className.trim());
		Object o = null;
		try {
			o = c.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			log.log(Level.WARNING, e.getMessage());
		}

		if (!(o instanceof Gazetteer)) {
			throw new IllegalArgumentException("Gazetteer class name is null");
		}

		return (Gazetteer) o;
	}

	private void handleEntryAction(ActionEvent actionEvent) {
		String lookupString = null;

		// hide any previous results
		resultsPanel.setVisible(false);
		if (actionEvent.getSource() instanceof JTextComponent jTextComponent) {
			lookupString = jTextComponent.getText();
		}

		if (lookupString == null || lookupString.isEmpty()) {
			return;
		}

		final List<PointOfInterest> poi = parseSearchValues(lookupString);

		if (poi != null) {
			if (poi.size() == 1) {
				this.moveToLocation(poi.get(0));
			} else {
				resultsBox.removeAllItems();
				poi.forEach(resultsBox::addItem);
				resultsPanel.setVisible(true);
			}
		}
	}

	/*
	 * Sample inputs Coordinate formats: 39.53, -119.816 (Reno, NV) 21 10 14 N, 86
	 * 51 0 W (Cancun)
	 */
	private List<PointOfInterest> parseSearchValues(String searchStr) {
		final String sepRegex = ","; // other separators??
		searchStr = searchStr.trim();
		final String[] searchValues = searchStr.split(sepRegex);
		if (searchValues.length == 1) {
			return queryService(searchValues[0].trim());
		} else if (searchValues.length == 2) {// possible coordinates
			// any numbers at all?
			final String regex = "\\d";
			final Pattern pattern = Pattern.compile(regex);
			final Matcher matcher = pattern.matcher(searchValues[1]); // Street Address may have numbers in first field so use
																// 2nd
			if (matcher.find()) {
				final List<PointOfInterest> list = new ArrayList<>();
				list.add(parseCoordinates(searchValues));
				return list;
			} else {
				return queryService(searchValues[0].trim() + "+" + searchValues[1].trim());
			}
		} else {
			// build search string and send to service
			final StringBuilder sb = new StringBuilder();
			for (int i = 0; i < searchValues.length; i++) {
				sb.append(searchValues[i].trim());
				if (i < searchValues.length - 1) {
					sb.append("+");
				}
			}

			return queryService(sb.toString());
		}
	}

	private List<PointOfInterest> queryService(String queryString) {
		return this.gazeteer.findPlaces(queryString);
	}

	// throws IllegalArgumentException
	private PointOfInterest parseCoordinates(String[] coords) {
		if (isDecimalDegrees(coords)) {
			final Double d1 = Double.parseDouble(coords[0].trim());
			final Double d2 = Double.parseDouble(coords[1].trim());

			return new BasicPointOfInterest(LatLon.fromDegrees(d1, d2));
		} else {   // may be in DMS
			final Angle aLat = Angle.fromDMS(coords[0].trim());
			final Angle aLon = Angle.fromDMS(coords[1].trim());

			return new BasicPointOfInterest(LatLon.fromDegrees(aLat.getDegrees(), aLon.getDegrees()));
		}
	}

	private boolean isDecimalDegrees(String[] coords) {
		try {
			Double.parseDouble(coords[0].trim());
			Double.parseDouble(coords[1].trim());
		} catch (NumberFormatException nfe) {
			return false;
		}

		return true;
	}

	public void moveToLocation(PointOfInterest location) {
		// Use a PanToIterator to iterate view to target position
		this.wwd.getView().goTo(new Position(location.getLatlon(), 0), 25e3);
	}

	public void moveToLocation(Sector sector, Double altitude) {
		final OrbitView view = (OrbitView) this.wwd.getView();

		final Globe globe = this.wwd.getModel().getGlobe();

		if (altitude == null || altitude == 0) {
			final double t = sector.getDeltaLonRadians();
			final double w = 0.5 * t * 6378137.0;
			altitude = w / this.wwd.getView().getFieldOfView().tanHalfAngle();
		}

		if (globe != null && view != null) {
			this.wwd.getView().goTo(new Position(sector.getCentroid(), 0), altitude);
		}
	}
}
