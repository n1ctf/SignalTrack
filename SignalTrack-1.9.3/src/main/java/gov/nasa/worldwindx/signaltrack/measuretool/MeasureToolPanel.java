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
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.UnitsFormat;
import gov.nasa.worldwind.util.measure.MeasureTool;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.ArrayList;

/**
 * Control panel for the MeasureTool.
 *
 * @author Patrick Murris
 * @version $Id: MeasureToolPanel.java 2226 2014-08-14 15:56:45Z tgaskins $
 * @see gov.nasa.worldwind.util.measure.MeasureTool
 */
public class MeasureToolPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private final transient WorldWindow wwd;
    private final transient MeasureTool measureTool;

    private JCheckBox freeHandCheck;
    private JButton newButton;
    private JButton pauseButton;
    private JButton endButton;
    private JLabel[] pointLabels;
    private JLabel lengthLabel;
    private JLabel areaLabel;
    private JLabel widthLabel;
    private JLabel heightLabel;
    private JLabel headingLabel;
    private JLabel centerLabel;

    private static final ArrayList<Position> LINE = new ArrayList<>();
    private static final ArrayList<Position> PATH = new ArrayList<>();
    private static final ArrayList<Position> POLYGON = new ArrayList<>();

    static {
        LINE.add(Position.fromDegrees(44, 7, 0));
        LINE.add(Position.fromDegrees(45, 8, 0));

        PATH.addAll(LINE);
        PATH.add(Position.fromDegrees(46, 6, 0));
        PATH.add(Position.fromDegrees(47, 5, 0));
        PATH.add(Position.fromDegrees(45, 6, 0));

        POLYGON.addAll(PATH);
        POLYGON.add(Position.fromDegrees(44, 7, 0));
    }

    public MeasureToolPanel(WorldWindow wwdObject, MeasureTool measureToolObject) {
        super(new BorderLayout());
        this.wwd = wwdObject;
        this.measureTool = measureToolObject;
        this.makePanel(new Dimension(200, 100));

        // Handle measure tool events
        measureTool.addPropertyChangeListener((PropertyChangeEvent event) -> {
            // Add, remove or change positions
            if (event.getPropertyName().equals(MeasureTool.EVENT_POSITION_ADD)
                    || event.getPropertyName().equals(MeasureTool.EVENT_POSITION_REMOVE)
                    || event.getPropertyName().equals(MeasureTool.EVENT_POSITION_REPLACE)) {
                fillPointsPanel();    // Update position list when changed
            } // The tool was armed / disarmed
            else if (event.getPropertyName().equals(MeasureTool.EVENT_ARMED)) {
                if (measureTool.isArmed()) {
                    newButton.setEnabled(false);
                    pauseButton.setText("Pause");
                    pauseButton.setEnabled(true);
                    endButton.setEnabled(true);
                    ((Component) wwd).setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                } else {
                    newButton.setEnabled(true);
                    pauseButton.setText("Pause");
                    pauseButton.setEnabled(false);
                    endButton.setEnabled(false);
                    ((Component) wwd).setCursor(Cursor.getDefaultCursor());
                }

            } // Metric changed - sent after each render frame
            else if (event.getPropertyName().equals(MeasureTool.EVENT_METRIC_CHANGED)) {
                updateMetric();
            }
        });
    }

    public MeasureTool getMeasureTool() {
        return this.measureTool;
    }

    private void makePanel(Dimension size) {
        // Shape combo
    	final JPanel shapePanel = new JPanel(new GridLayout(1, 2, 5, 5));
        shapePanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        shapePanel.add(new JLabel("Shape:"));
        final JComboBox<String> shapeCombo = new JComboBox<>(new String[]{"Line", "Path", "Polygon", "Circle", "Ellipse", "Square", "Rectangle"});
        shapeCombo.addActionListener((ActionEvent event) -> {
        	final String item = (String) ((JComboBox<?>) event.getSource()).getSelectedItem();
            switch (item) {
				case "Line" -> measureTool.setMeasureShapeType(MeasureTool.SHAPE_LINE);
				case "Path" -> measureTool.setMeasureShapeType(MeasureTool.SHAPE_PATH);
				case "Polygon" -> measureTool.setMeasureShapeType(MeasureTool.SHAPE_POLYGON);
				case "Circle" -> measureTool.setMeasureShapeType(MeasureTool.SHAPE_CIRCLE);
				case "Ellipse" -> measureTool.setMeasureShapeType(MeasureTool.SHAPE_ELLIPSE);
				case "Square" -> measureTool.setMeasureShapeType(MeasureTool.SHAPE_SQUARE);
				case "Rectangle" -> measureTool.setMeasureShapeType(MeasureTool.SHAPE_QUAD);
			}
        });
        
        shapePanel.add(shapeCombo);

        // Path type combo
        final JPanel pathTypePanel = new JPanel(new GridLayout(1, 2, 5, 5));
        pathTypePanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        pathTypePanel.add(new JLabel("Path type:"));
        final JComboBox<String> pathTypeCombo = new JComboBox<>(new String[]{"Linear", "Rhumb", "Great circle"});
        pathTypeCombo.setSelectedIndex(2);
        pathTypeCombo.addActionListener((ActionEvent event) -> {
        	final String item = (String) ((JComboBox<?>) event.getSource()).getSelectedItem();
            switch (item) {
				case "Linear" -> measureTool.setPathType(AVKey.LINEAR);
				case "Rhumb" -> measureTool.setPathType(AVKey.RHUMB_LINE);
				case "Great circle" -> measureTool.setPathType(AVKey.GREAT_CIRCLE);
			}
        });
        pathTypePanel.add(pathTypeCombo);

        // Units combo
        final JPanel unitsPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        unitsPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        unitsPanel.add(new JLabel("Units:"));
        final JComboBox<String> unitsCombo = new JComboBox<>(new String[]{"M/M\u00b2", "KM/KM\u00b2", "KM/Hectare", "Feet/Feet\u00b2",
            "Miles/Miles\u00b2", "Nm/Miles\u00b2", "Yards/Acres"});
        unitsCombo.setSelectedItem("KM/KM\u00b2");
        unitsCombo.addActionListener((ActionEvent event) -> {
        	final String item = (String) ((JComboBox<?>) event.getSource()).getSelectedItem();
            switch (item) {
				case "M/M\u00b2" -> {
					measureTool.getUnitsFormat().setLengthUnits(UnitsFormat.METERS);
					measureTool.getUnitsFormat().setAreaUnits(UnitsFormat.SQUARE_METERS);
				}
				case "KM/KM\u00b2" -> {
					measureTool.getUnitsFormat().setLengthUnits(UnitsFormat.KILOMETERS);
					measureTool.getUnitsFormat().setAreaUnits(UnitsFormat.SQUARE_KILOMETERS);
				}
				case "KM/Hectare" -> {
					measureTool.getUnitsFormat().setLengthUnits(UnitsFormat.KILOMETERS);
					measureTool.getUnitsFormat().setAreaUnits(UnitsFormat.HECTARE);
				}
				case "Feet/Feet\u00b2" -> {
					measureTool.getUnitsFormat().setLengthUnits(UnitsFormat.FEET);
					measureTool.getUnitsFormat().setAreaUnits(UnitsFormat.SQUARE_FEET);
				}
				case "Miles/Miles\u00b2" -> {
					measureTool.getUnitsFormat().setLengthUnits(UnitsFormat.MILES);
					measureTool.getUnitsFormat().setAreaUnits(UnitsFormat.SQUARE_MILES);
				}
				case "Nm/Miles\u00b2" -> {
					measureTool.getUnitsFormat().setLengthUnits(UnitsFormat.NAUTICAL_MILES);
					measureTool.getUnitsFormat().setAreaUnits(UnitsFormat.SQUARE_MILES);
				}
				case "Yards/Acres" -> {
					measureTool.getUnitsFormat().setLengthUnits(UnitsFormat.YARDS);
					measureTool.getUnitsFormat().setAreaUnits(UnitsFormat.ACRE);
				}
			}
        });
        unitsPanel.add(unitsCombo);

        // Angles combo
        final JPanel anglesPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        anglesPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        anglesPanel.add(new JLabel("Angle Format:"));
        final JComboBox<String> anglesCombo = new JComboBox<>(new String[]{"DD", "DMS"});
        anglesCombo.setSelectedItem("DD");
        anglesCombo.addActionListener((ActionEvent event) -> {
        	final String item = (String) ((JComboBox<?>) event.getSource()).getSelectedItem();
            measureTool.getUnitsFormat().setShowDMS("DMS".equals(item));
        });
        anglesPanel.add(anglesCombo);

        // Check boxes panel
        final JPanel checkPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        checkPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        final JCheckBox followCheck = new JCheckBox("Follow terrain");
        followCheck.setSelected(measureTool.isFollowTerrain());
        followCheck.addActionListener((ActionEvent event) -> {
        	final JCheckBox cb = (JCheckBox) event.getSource();
            measureTool.setFollowTerrain(cb.isSelected());
            wwd.redraw();
        });
        checkPanel.add(followCheck);

        final JCheckBox showControlsCheck = new JCheckBox("Control points");
        showControlsCheck.setSelected(measureTool.isShowControlPoints());
        showControlsCheck.addActionListener((ActionEvent event) -> {
        	final JCheckBox cb = (JCheckBox) event.getSource();
            measureTool.setShowControlPoints(cb.isSelected());
            wwd.redraw();
        });
        checkPanel.add(showControlsCheck);

        final JCheckBox rubberBandCheck = new JCheckBox("Rubber band");
        rubberBandCheck.setSelected(measureTool.getController().isUseRubberBand());
        rubberBandCheck.addActionListener((ActionEvent event) -> {
        	final JCheckBox cb = (JCheckBox) event.getSource();
            measureTool.getController().setUseRubberBand(cb.isSelected());
            freeHandCheck.setEnabled(cb.isSelected());
            wwd.redraw();
        });
        checkPanel.add(rubberBandCheck);

        freeHandCheck = new JCheckBox("Free Hand");
        freeHandCheck.setSelected(measureTool.getController().isFreeHand());
        freeHandCheck.addActionListener((ActionEvent event) -> {
        	final JCheckBox cb = (JCheckBox) event.getSource();
            measureTool.getController().setFreeHand(cb.isSelected());
            wwd.redraw();
        });
        checkPanel.add(freeHandCheck);

        final JCheckBox showAnnotationCheck = new JCheckBox("Tooltip");
        showAnnotationCheck.setSelected(measureTool.isShowAnnotation());
        showAnnotationCheck.addActionListener((ActionEvent event) -> {
        	final JCheckBox cb = (JCheckBox) event.getSource();
            measureTool.setShowAnnotation(cb.isSelected());
            wwd.redraw();
        });
        checkPanel.add(showAnnotationCheck);

        // Color buttons
        final JPanel colorPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        colorPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        final JButton lineColorButton = new JButton("Line");
        lineColorButton.addActionListener((ActionEvent event) -> {
        	final Color c = JColorChooser.showDialog(colorPanel,
                    "Choose a color...", ((JButton) event.getSource()).getBackground());
            if (c != null) {
                ((JButton) event.getSource()).setBackground(c);
                measureTool.setLineColor(c);
                final Color fill = new Color(c.getRed() / 255f * .5f,
                        c.getGreen() / 255f * .5f, c.getBlue() / 255f * .5f, .5f);
                measureTool.setFillColor(fill);
            }
        });
        colorPanel.add(lineColorButton);
        lineColorButton.setBackground(measureTool.getLineColor());

        final JButton pointColorButton = new JButton("Points");
        pointColorButton.addActionListener((ActionEvent event) -> {
        	final Color c = JColorChooser.showDialog(colorPanel,
                    "Choose a color...", ((JButton) event.getSource()).getBackground());
            if (c != null) {
                ((JButton) event.getSource()).setBackground(c);
                measureTool.getControlPointsAttributes().setBackgroundColor(c);
            }
        });
        colorPanel.add(pointColorButton);
        pointColorButton.setBackground(measureTool.getControlPointsAttributes().getBackgroundColor());

        final JButton annotationColorButton = new JButton("Tooltip");
        annotationColorButton.addActionListener((ActionEvent event) -> {
        	final Color c = JColorChooser.showDialog(colorPanel,
                    "Choose a color...", ((JButton) event.getSource()).getBackground());
            if (c != null) {
                ((JButton) event.getSource()).setBackground(c);
                measureTool.getAnnotationAttributes().setTextColor(c);
            }
        });
        annotationColorButton.setBackground(measureTool.getAnnotationAttributes().getTextColor());
        colorPanel.add(annotationColorButton);

        // Action buttons
        final JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        newButton = new JButton("New");
        newButton.addActionListener(_ -> {
            measureTool.clear();
            measureTool.setArmed(true);
        });
        buttonPanel.add(newButton);
        newButton.setEnabled(true);

        pauseButton = new JButton("Pause");
        pauseButton.addActionListener(_ -> {
            measureTool.setArmed(!measureTool.isArmed());
            pauseButton.setText(!measureTool.isArmed() ? "Resume" : "Pause");
            pauseButton.setEnabled(true);
            ((Component) wwd).setCursor(!measureTool.isArmed() ? Cursor.getDefaultCursor()
                    : Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        });
        buttonPanel.add(pauseButton);
        pauseButton.setEnabled(false);

        endButton = new JButton("End");
        endButton.addActionListener(_ -> measureTool.setArmed(false));
        buttonPanel.add(endButton);
        endButton.setEnabled(false);

        // Preset buttons
        final JPanel presetPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        presetPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        JButton bt = new JButton("Polyline");
        bt.addActionListener(_ -> {
            shapeCombo.setSelectedIndex(1);
            measureTool.setMeasureShape(new Path(PATH));
        });
        presetPanel.add(bt);
        bt = new JButton("Surf. Quad");
        bt.addActionListener(_ -> {
            shapeCombo.setSelectedIndex(6);
            measureTool.setMeasureShape(new SurfaceQuad(Position.fromDegrees(44, 7, 0), 100e3, 50e3, Angle.fromDegrees(30)));
        });
        presetPanel.add(bt);
        bt = new JButton("Polygon");
        bt.addActionListener(_ -> {
            shapeCombo.setSelectedIndex(2);
            measureTool.setMeasureShape(new SurfacePolygon(POLYGON));
        });
        presetPanel.add(bt);

        // Point list
        final JPanel pointPanel = new JPanel(new GridLayout(0, 1, 0, 4));
        pointPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        this.pointLabels = new JLabel[100];
        for (int i = 0; i < this.pointLabels.length; i++) {
            this.pointLabels[i] = new JLabel("");
            pointPanel.add(this.pointLabels[i]);
        }

        // Put the point panel in a container to prevent scroll panel from stretching the vertical spacing.
        final JPanel dummyPanel = new JPanel(new BorderLayout());
        dummyPanel.add(pointPanel, BorderLayout.NORTH);

        // Put the point panel in a scroll bar.
        final JScrollPane scrollPane = new JScrollPane(dummyPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        if (size != null) {
            scrollPane.setPreferredSize(size);
        }

        // Metric
        final JPanel metricPanel = new JPanel(new GridLayout(0, 2, 0, 4));
        metricPanel.setBorder(new CompoundBorder(
                new TitledBorder("Metric"), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        metricPanel.add(new JLabel("Length:"));
        lengthLabel = new JLabel();
        metricPanel.add(lengthLabel);
        metricPanel.add(new JLabel("Area:"));
        areaLabel = new JLabel();
        metricPanel.add(areaLabel);
        metricPanel.add(new JLabel("Width:"));
        widthLabel = new JLabel();
        metricPanel.add(widthLabel);
        metricPanel.add(new JLabel("Height:"));
        heightLabel = new JLabel();
        metricPanel.add(heightLabel);
        metricPanel.add(new JLabel("Heading:"));
        headingLabel = new JLabel();
        metricPanel.add(headingLabel);
        metricPanel.add(new JLabel("Center:"));
        centerLabel = new JLabel();
        metricPanel.add(centerLabel);

        // Add all the panels to a titled panel
        final JPanel outerPanel = new JPanel();
        outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.Y_AXIS));
        outerPanel.setBorder(
                new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9), new TitledBorder("Measure")));
        outerPanel.setToolTipText("Measure tool control and info");
        outerPanel.add(colorPanel);
        outerPanel.add(shapePanel);
        outerPanel.add(pathTypePanel);
        outerPanel.add(unitsPanel);
        outerPanel.add(anglesPanel);
        outerPanel.add(checkPanel);
        outerPanel.add(buttonPanel);
        outerPanel.add(presetPanel);
        outerPanel.add(metricPanel);
        outerPanel.add(scrollPane);

        this.add(outerPanel, BorderLayout.NORTH);
    }

    private void fillPointsPanel() {
        int i = 0;
        if (measureTool.getPositions() != null) {
            for (LatLon pos : measureTool.getPositions()) {
                if (i == this.pointLabels.length) {
                    break;
                }

                final String las = "Lat %7.4f\u00B0".formatted(pos.getLatitude().getDegrees());
                final String los = "Lon %7.4f\u00B0".formatted(pos.getLongitude().getDegrees());
                pointLabels[i++].setText(las + "  " + los);
            }
        }
        // Clear remaining labels
        for (; i < this.pointLabels.length; i++) {
            pointLabels[i].setText("");
        }

    }

    private void updateMetric() {
        // Update length label
        double value = measureTool.getLength();
        String s;
        if (value <= 0) {
            s = "na";
        } else if (value < 1000) {
            s = "%,7.1f m".formatted(value);
        } else {
            s = "%,7.3f km".formatted(value / 1000);
        }
        lengthLabel.setText(s);

        // Update area label
        value = measureTool.getArea();
        if (value < 0) {
            s = "na";
        } else if (value < 1e6) {
            s = "%,7.1f m2".formatted(value);
        } else {
            s = "%,7.3f km2".formatted(value / 1e6);
        }
        areaLabel.setText(s);

        // Update width label
        value = measureTool.getWidth();
        if (value < 0) {
            s = "na";
        } else if (value < 1000) {
            s = "%,7.1f m".formatted(value);
        } else {
            s = "%,7.3f km".formatted(value / 1000);
        }
        widthLabel.setText(s);

        // Update height label
        value = measureTool.getHeight();
        if (value < 0) {
            s = "na";
        } else if (value < 1000) {
            s = "%,7.1f m".formatted(value);
        } else {
            s = "%,7.3f km".formatted(value / 1000);
        }
        heightLabel.setText(s);

        // Update heading label
        final Angle angle = measureTool.getOrientation();
        if (angle != null) {
            s = "%,6.2f\u00B0".formatted(angle.degrees);
        } else {
            s = "na";
        }
        headingLabel.setText(s);

        // Update center label
        final Position center = measureTool.getCenterPosition();
        if (center != null) {
            s = "%,7.4f\u00B0 %,7.4f\u00B0".formatted(center.getLatitude().degrees, center.getLongitude().degrees);
        } else {
            s = "na";
        }
        centerLabel.setText(s);
    }
}
