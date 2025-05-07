package meteorology;

import java.awt.Dialog;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.beans.PropertyChangeSupport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

import meteorology.AbstractEnvironmentSensor.PrecipitationUnits;
import meteorology.AbstractEnvironmentSensor.TemperatureUnits;
import meteorology.AbstractEnvironmentSensor.SpeedUnit;

import n1ctf.TempSensorClient;
import n1ctf.AirQualitySensorClient;
import n1ctf.GeigerCounterClient;
import time.DateTimeServiceComponent;

public class EnvironmentMonitorComponent extends JDialog {
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = Logger.getLogger(EnvironmentMonitorComponent.class.getName());
	private static final PropertyChangeSupport pcs = new PropertyChangeSupport(EnvironmentMonitorComponent.class);
	
	private JTabbedPane tabbedPane;
	private JButton okButton;
	private JButton cancelButton;
	private JButton applyButton;
	
	private JCheckBox cbStartWeatherSensorWithSystem;
	private JCheckBox cbEnableDebug;
	private JCheckBox cbUseGpsForStationLocation;
	
	private JComboBox<String> jcboDevice;
	private DefaultComboBoxModel<String> jcboDeviceModel;
    
	private JComboBox<PrecipitationUnits> jcboPrecipitationUnits;
	private DefaultComboBoxModel<PrecipitationUnits> jcboPrecipitationUnitsModel;
    
	private JComboBox<TemperatureUnits> jcboTemperatureUnits;
	private DefaultComboBoxModel<TemperatureUnits> jcboTemperatureUnitsModel;
	
	private JComboBox<SpeedUnit> jcboWindSpeedUnits;
	private DefaultComboBoxModel<SpeedUnit> jcboWindSpeedUnitsModel;
    
    private JTextField jtfDefaultStationMetersAMSL;
    private JTextField jtfDefaultStationLatitude;
    private JTextField jtfDefaultStationLongitude;
    
    private JLabel lblDeviceLabel;
    private JLabel lblDefaultStationMetersAMSLLabel;
    private JLabel lblDefaultLatitudeLabel;
    private JLabel lblDefaultLongitudeLabel;
    private JLabel lblPrecipitationUnitsLabel;
    private JLabel lblTemperatureUnitsLabel;
    private JLabel lblWindSpeedUnitsLabel;
    
	private final List<JSpinner> jSpinDir = new ArrayList<>(8);
	private final List<JSpinner> jSpinSpeed = new ArrayList<>(8);

	private final AbstractEnvironmentSensor abstractEnvironmentSensor;
	private final TempSensorClient bgts;
	private final DateTimeServiceComponent dts;
	private final GeigerCounterClient radDetector;
	private final AirQualitySensorClient aqs;
	
	private final String[] windDirHeadingLabels = new String[] {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
	private final String[] windSpeedHeadingLabels = new String[] {"0-5", "5-10", "10-15", "15-20", "20-30", "30-60", "60-90", "90-150"};
	
	public EnvironmentMonitorComponent(AbstractEnvironmentSensor abstractEnvironmentSensor, TempSensorClient bgts, GeigerCounterClient radDetector, AirQualitySensorClient aqs, DateTimeServiceComponent dts) {
		this.abstractEnvironmentSensor = abstractEnvironmentSensor;
		this.bgts = bgts;
		this.dts = dts;
		this.radDetector = radDetector;
		this.aqs = aqs;
		
		super.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent event) {
				if (WindowEvent.WINDOW_CLOSING == event.getID()) {
					applyButtonActionListenerEvent();
				}
			}
		});

		initializeComponents();
		setComponentValues();
		loadComboBoxes();
		configureListeners();
		drawGraphicalUserInterface();
		
		showDisplay();
	}
	
	public static PropertyChangeSupport getPropertyChangeSupport() {
		return pcs;
	}
	
	private void initializeComponents() {
		setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
		setTitle("Environment Monitor Settings");

		tabbedPane = new JTabbedPane();

		applyButton = new JButton("Apply");
		cancelButton = new JButton("Cancel");
		okButton = new JButton("OK");
		
        okButton.setMultiClickThreshhold(50L);        
        cancelButton.setMultiClickThreshhold(50L);
        applyButton.setMultiClickThreshhold(50L);
		
        lblDeviceLabel = new JLabel();
        jcboDeviceModel = new DefaultComboBoxModel<>();
        jcboDevice = new JComboBox<>(jcboDeviceModel);
        
        cbStartWeatherSensorWithSystem = new JCheckBox();
        cbEnableDebug = new JCheckBox();
        cbUseGpsForStationLocation = new JCheckBox();
        
        lblDefaultLatitudeLabel = new JLabel();
        jtfDefaultStationLatitude = new JTextField();
        
        lblDefaultLongitudeLabel = new JLabel();
        jtfDefaultStationLongitude = new JTextField();
        
        lblDefaultStationMetersAMSLLabel = new JLabel();
        jtfDefaultStationMetersAMSL = new JTextField();
        
        lblPrecipitationUnitsLabel = new JLabel();
        jcboPrecipitationUnitsModel = new DefaultComboBoxModel<>();
        jcboPrecipitationUnits = new JComboBox<>(jcboPrecipitationUnitsModel);
        
        lblTemperatureUnitsLabel = new JLabel();
        jcboTemperatureUnitsModel = new DefaultComboBoxModel<>();
        jcboTemperatureUnits = new JComboBox<>(jcboTemperatureUnitsModel);
        
        lblWindSpeedUnitsLabel = new JLabel();
        jcboWindSpeedUnitsModel = new DefaultComboBoxModel<>();
        jcboWindSpeedUnits = new JComboBox<>(jcboWindSpeedUnitsModel);
        
        for (int i = 0; i < 8; i++) {
        	final JSpinner jspnDir = new JSpinner();
        	
        	final int directionCalValue = abstractEnvironmentSensor.getWindDirectionCalList().get(i);
        	jspnDir.setModel(new SpinnerNumberModel(directionCalValue, -180, 180, 1));
        	jspnDir.setVerifyInputWhenFocusTarget(false);
        	
        	jSpinDir.add(jspnDir);
        	
        	final JSpinner jspnSpd = new JSpinner();
        	
        	final double speedCalValue = abstractEnvironmentSensor.getWindSpeedCalList().get(i);
        	jspnSpd.setModel(new SpinnerNumberModel(speedCalValue, -10, 10, 1));
        	jspnSpd.setVerifyInputWhenFocusTarget(false);
        	
        	jSpinSpeed.add(jspnSpd);
        }
	}

	private void setComponentValues() {
		tabbedPane.removeAll();
		tabbedPane.addTab("Environment Monitor Settings", null, getWeatherSensorSettingsMainPanel(), null);
		tabbedPane.addTab("Wind Calibration Settings", null, getWindCalibrationPanel(), null);
		
		if (dts != null) {
			for (JPanel panel : dts.getSettingsPanelArray()) {
				tabbedPane.addTab(panel.getName(), panel);
			}
		}
		
		if (abstractEnvironmentSensor.getConfigurationComponentArray() != null) {
			for (JPanel panel : abstractEnvironmentSensor.getConfigurationComponentArray()) {
				tabbedPane.addTab(panel.getName(), panel);
			}
		}
		
		if (bgts.getConfigurationComponentArray() != null) {
			for (JPanel panel : bgts.getConfigurationComponentArray()) {
				tabbedPane.addTab(panel.getName(), panel);
			}
		}

		if (radDetector.getConfigurationComponentArray() != null) {
			for (JPanel panel : radDetector.getConfigurationComponentArray()) {
				tabbedPane.addTab(panel.getName(), panel);
			}
		}
		
		if (aqs.getConfigurationComponentArray() != null) {
			for (JPanel panel : aqs.getConfigurationComponentArray()) {
				tabbedPane.addTab(panel.getName(), panel);
			}
		}
		
		cbStartWeatherSensorWithSystem.setText("Start Environment Monitor With System");    
        cbStartWeatherSensorWithSystem.setSelected(abstractEnvironmentSensor.isStartWithSystem());
        
        cbEnableDebug.setText("Enable Verbose Logging");
        cbEnableDebug.setSelected(abstractEnvironmentSensor.isDebug());
        
		lblDeviceLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        lblDeviceLabel.setText("Atmospheric Monitor");
        
        cbUseGpsForStationLocation.setText("Use GPS for Station Location if Available");
        cbUseGpsForStationLocation.setSelected(abstractEnvironmentSensor.isUseGpsForStationLocation());

        lblDefaultLatitudeLabel.setText("Default Station Latitude");
        lblDefaultLatitudeLabel.setHorizontalAlignment(SwingConstants.LEFT);
        jtfDefaultStationLatitude.setText(String.valueOf(abstractEnvironmentSensor.getStationLatitudeDegrees()));

        lblDefaultLongitudeLabel.setText("Default Station Longitude");
        lblDefaultLongitudeLabel.setHorizontalAlignment(SwingConstants.LEFT);
        jtfDefaultStationLongitude.setText(String.valueOf(abstractEnvironmentSensor.getStationLongitudeDegrees()));
        
        lblDefaultStationMetersAMSLLabel.setText("Default Station Meters AMSL");
        lblDefaultStationMetersAMSLLabel.setHorizontalAlignment(SwingConstants.LEFT);
        jtfDefaultStationMetersAMSL.setText(String.valueOf(abstractEnvironmentSensor.getStationElevationMeters()));
        
        lblPrecipitationUnitsLabel.setText("Precipitation Units");
        lblPrecipitationUnitsLabel.setHorizontalAlignment(SwingConstants.LEFT);

        lblTemperatureUnitsLabel.setText("Temperature Units");
        lblTemperatureUnitsLabel.setHorizontalAlignment(SwingConstants.LEFT);

        lblWindSpeedUnitsLabel.setText("Wind Speed Units");
        lblWindSpeedUnitsLabel.setHorizontalAlignment(SwingConstants.LEFT);
	}

	private void configureListeners() {
		okButton.addActionListener(_ -> okButtonActionListenerEvent());
        cancelButton.addActionListener(_ -> cancelButtonActionListenerEvent());
        applyButton.addActionListener(_ -> applyButtonActionListenerEvent());
	}

    private void applyButtonActionListenerEvent() {
        abstractEnvironmentSensor.setStartWithSystem(cbStartWeatherSensorWithSystem.isSelected());
        abstractEnvironmentSensor.setDebug(cbEnableDebug.isSelected());
        abstractEnvironmentSensor.setUseGpsForStationLocation(cbUseGpsForStationLocation.isSelected());
        abstractEnvironmentSensor.setStationElevationMeters(Double.parseDouble(jtfDefaultStationMetersAMSL.getText()));
        abstractEnvironmentSensor.setStationLatitudeDegrees(Double.parseDouble(jtfDefaultStationLatitude.getText()));
        abstractEnvironmentSensor.setStationLongitudeDegrees(Double.parseDouble(jtfDefaultStationLongitude.getText()));
        abstractEnvironmentSensor.setTemperatureUnits(TemperatureUnits.values()[jcboTemperatureUnits.getSelectedIndex()]);
        abstractEnvironmentSensor.setWindSpeedUnits(SpeedUnit.values()[jcboWindSpeedUnits.getSelectedIndex()]);
        abstractEnvironmentSensor.setPrecipitationUnits(PrecipitationUnits.values()[jcboPrecipitationUnits.getSelectedIndex()]);
        for (int i = 0; i < abstractEnvironmentSensor.getWindDirectionCalList().size(); i++) {
			abstractEnvironmentSensor.getWindDirectionCalList().set(i, (Integer) jSpinDir.get(i).getModel().getValue());
			abstractEnvironmentSensor.getWindSpeedCalList().set(i, (Double) jSpinSpeed.get(i).getModel().getValue());
        }
        abstractEnvironmentSensor.savePreferences();
        bgts.saveClientSettings();
        radDetector.saveClientSettings();
        if (dts != null) {
			dts.saveSettings();
		}
    }
    
    private void okButtonActionListenerEvent() {
        applyButton.doClick();
        setVisible(false);
        dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }

    private void cancelButtonActionListenerEvent() {
    	setVisible(false);
        dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }

    // Load this combo box with all the available sensors that extend the abstract AbstractAtmosphereSensor class and 
    // set the list to the initially passed selection.
	private void loadComboBoxes() {
		final SwingWorker<String[], Void> worker = new SwingWorker<String[], Void>() {
		
		@Override
		protected String[] doInBackground() throws Exception {
        	return AbstractEnvironmentSensor.getCatalogMap().values().toArray(new String[AbstractEnvironmentSensor.getCatalogMap().values().toArray().length]);
		}
		
		@Override
		protected void done() {
		    try {
		        jcboDevice.removeAllItems();
		        jcboDeviceModel.addAll(Arrays.asList(get()));
		        jcboDevice.setSelectedIndex(abstractEnvironmentSensor.getCatalogMapIndex());
	            jcboDevice.addItemListener(event -> {
	                if (event.getStateChange() == ItemEvent.SELECTED) {
	                    final JComboBox<?> cb = (JComboBox<?>) event.getSource();  
	                    final String className = AbstractEnvironmentSensor.getCatalogMap().getKey(cb.getSelectedItem());
	                    abstractEnvironmentSensor.notifyClassNameChange(className);
                        JOptionPane.showMessageDialog(new JDialog(), "Change to Environment Sensor Will Take Place Upon Next Restart",
            	    	 	"Notification", JOptionPane.INFORMATION_MESSAGE);
	                }
	            });
		        } catch (final IllegalArgumentException | ExecutionException e) {
		            LOG.log(Level.WARNING, e.getMessage(), e);
		        } catch (InterruptedException e) {
		            LOG.log(Level.WARNING, e.getMessage(), e);
		            Thread.currentThread().interrupt();
		        }
		    }
		};
		
		worker.execute();

		jcboTemperatureUnitsModel.addAll(Arrays.asList(AbstractEnvironmentSensor.TemperatureUnits.values()));
        jcboTemperatureUnits.setSelectedIndex(abstractEnvironmentSensor.getTemperatureUnits().ordinal());
        
        jcboTemperatureUnits.addItemListener(event -> {
            if (event.getStateChange() == ItemEvent.SELECTED) {
                final JComboBox<?> cb = (JComboBox<?>) event.getSource();  
                abstractEnvironmentSensor.setTemperatureUnits(TemperatureUnits.valueOf((String) cb.getSelectedItem()));
            }
        });      
        
        jcboPrecipitationUnitsModel.addAll(Arrays.asList(AbstractEnvironmentSensor.PrecipitationUnits.values()));
        jcboPrecipitationUnits.setSelectedIndex(abstractEnvironmentSensor.getPrecipitationUnits().ordinal());
        
        jcboPrecipitationUnits.addItemListener(event -> {
            if (event.getStateChange() == ItemEvent.SELECTED) {
                final JComboBox<?> cb = (JComboBox<?>) event.getSource();  
                abstractEnvironmentSensor.setPrecipitationUnits(PrecipitationUnits.valueOf((String) cb.getSelectedItem()));
            }
        });
		 
        jcboWindSpeedUnitsModel.addAll(Arrays.asList(AbstractEnvironmentSensor.SpeedUnit.values()));
        jcboWindSpeedUnits.setSelectedIndex(abstractEnvironmentSensor.getWindSpeedUnits().ordinal());
        
        jcboWindSpeedUnits.addItemListener(event -> {
            if (event.getStateChange() == ItemEvent.SELECTED) {
                final JComboBox<?> cb = (JComboBox<?>) event.getSource();  
                abstractEnvironmentSensor.setWindSpeedUnits(SpeedUnit.valueOf((String) cb.getSelectedItem()));
            }
        });
	}
	
	private void drawGraphicalUserInterface() {
		final GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
					.addGroup(layout.createSequentialGroup()
						.addContainerGap()
						.addComponent(tabbedPane, 600, 600, 600))
					.addGroup(layout.createSequentialGroup()
						.addComponent(okButton, 90, 90, 90)
						.addComponent(applyButton, 90, 90, 90)
						.addComponent(cancelButton, 90, 90, 90)))
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
				.addComponent(tabbedPane, 350, 400, 450)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(okButton)
					.addComponent(applyButton)
					.addComponent(cancelButton))
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
	}
	
	private void showDisplay() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        pack();
        setModalityType(ModalityType.APPLICATION_MODAL);
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
    }
	
	private JPanel getWeatherSensorSettingsMainPanel() {
		final JPanel panel = new JPanel();

		final GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);

        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(42, 42, 42)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(cbStartWeatherSensorWithSystem)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblDeviceLabel, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jcboDevice, GroupLayout.PREFERRED_SIZE, 350, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(180, Short.MAX_VALUE))
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addGap(32, 32, 32)
                            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(lblDefaultStationMetersAMSLLabel, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                                .addComponent(lblDefaultLongitudeLabel, GroupLayout.Alignment.LEADING)
                                .addComponent(lblDefaultLatitudeLabel, GroupLayout.Alignment.LEADING)
                                .addComponent(lblPrecipitationUnitsLabel, GroupLayout.Alignment.LEADING)
                                .addComponent(lblTemperatureUnitsLabel, GroupLayout.Alignment.LEADING)	
                                .addComponent(lblWindSpeedUnitsLabel, GroupLayout.Alignment.LEADING))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(jtfDefaultStationMetersAMSL, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 110, GroupLayout.PREFERRED_SIZE)
                                .addComponent(jtfDefaultStationLongitude, GroupLayout.Alignment.LEADING)
                                .addComponent(jtfDefaultStationLatitude, GroupLayout.Alignment.LEADING)
                                .addComponent(jcboPrecipitationUnits, GroupLayout.Alignment.LEADING)
                                .addComponent(jcboTemperatureUnits, GroupLayout.Alignment.LEADING)	
                                .addComponent(jcboWindSpeedUnits, GroupLayout.Alignment.LEADING))
                            .addContainerGap(227, Short.MAX_VALUE))
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(cbUseGpsForStationLocation)
                                .addComponent(cbEnableDebug))
                            .addGap(117, 117, 117)))))
            );
        
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jcboDevice, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDeviceLabel))
                .addGap(18, 18, 18)
                .addComponent(cbStartWeatherSensorWithSystem)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbEnableDebug)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbUseGpsForStationLocation)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(lblDefaultLatitudeLabel)
                    .addComponent(jtfDefaultStationLatitude, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                	.addComponent(lblDefaultLongitudeLabel)	
                    .addComponent(jtfDefaultStationLongitude, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                	.addComponent(lblDefaultStationMetersAMSLLabel)	
                    .addComponent(jtfDefaultStationMetersAMSL, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    	.addComponent(lblPrecipitationUnitsLabel)	
                        .addComponent(jcboPrecipitationUnits, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))                 
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    	.addComponent(lblTemperatureUnitsLabel)	
                        .addComponent(jcboTemperatureUnits, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    	.addComponent(lblWindSpeedUnitsLabel)	
                        .addComponent(jcboWindSpeedUnits, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        
        return panel;
	}
	
	private JPanel getWindCalibrationPanel() {
		final JPanel panel = new JPanel();
		
		final JPanel windSpeedCal = getJpnlWindSpeedCalibrationPanel();
		final JPanel windDirectionCal = getJpnlWindDirectionCalibrationPanel();
		
		final GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
				.addGap(20, 20, 20)
				.addComponent(windDirectionCal, 225, 225, 225)
				.addGap(18, 18, 18)
				.addComponent(windSpeedCal, 225, 225, 225)
				.addContainerGap(20, Short.MAX_VALUE)));
		
		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(windDirectionCal, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(windSpeedCal, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap()));

		return panel;
	}

	private JPanel getJpnlWindDirectionCalibrationPanel() {
		final JPanel panel = new JPanel();

		final List<JLabel> headingLabelList = new ArrayList<>(8);
		
		for (int i = 0; i < 8; i++) {
			headingLabelList.add(i, new JLabel(windDirHeadingLabels[i]));
			headingLabelList.get(i).setHorizontalAlignment(SwingConstants.CENTER);
			headingLabelList.get(i).setHorizontalTextPosition(SwingConstants.CENTER);
		}

		panel.setBorder(BorderFactory.createTitledBorder(null, "Wind Direction Calibration",
				TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Segoe UI", 1, 12))); 

		final GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
				.addGap(23, 23, 23)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addComponent(headingLabelList.get(0), GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(headingLabelList.get(1), GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(headingLabelList.get(2), GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(headingLabelList.get(3), GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(headingLabelList.get(4), GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(headingLabelList.get(5),  GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(headingLabelList.get(6), GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(headingLabelList.get(7), GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
					.addComponent(jSpinDir.get(0), GroupLayout.PREFERRED_SIZE, 66, GroupLayout.PREFERRED_SIZE)
					.addComponent(jSpinDir.get(1), GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
					.addComponent(jSpinDir.get(2), GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
					.addComponent(jSpinDir.get(3), GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
					.addComponent(jSpinDir.get(4), GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
					.addComponent(jSpinDir.get(5), GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
					.addComponent(jSpinDir.get(6), GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
					.addComponent(jSpinDir.get(7), GroupLayout.PREFERRED_SIZE, 66, GroupLayout.PREFERRED_SIZE))
				.addGap(23, 23, 23)));
		
		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
				.addGap(15, 15, 15)
				.addGroup(layout
					.createParallelGroup(GroupLayout.Alignment.LEADING, false)
					.addComponent(jSpinDir.get(0))
					.addComponent(headingLabelList.get(0), GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout
					.createParallelGroup(GroupLayout.Alignment.LEADING, false)
					.addComponent(jSpinDir.get(1))
					.addComponent(headingLabelList.get(1), GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout
					.createParallelGroup(GroupLayout.Alignment.LEADING, false)
					.addComponent(jSpinDir.get(2))
					.addComponent(headingLabelList.get(2), GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout
					.createParallelGroup(GroupLayout.Alignment.LEADING, false)
					.addComponent(jSpinDir.get(3))
					.addComponent(headingLabelList.get(3), GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout
					.createParallelGroup(GroupLayout.Alignment.LEADING, false)
					.addComponent(jSpinDir.get(4))
					.addComponent(headingLabelList.get(4), GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout
					.createParallelGroup(GroupLayout.Alignment.LEADING, false)
					.addComponent(jSpinDir.get(5))
					.addComponent(headingLabelList.get(5), GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout
					.createParallelGroup(GroupLayout.Alignment.LEADING, false)
					.addComponent(jSpinDir.get(6))
					.addComponent(headingLabelList.get(6), GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout
					.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(headingLabelList.get(7), GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
					.addComponent(jSpinDir.get(7), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addContainerGap(15, Short.MAX_VALUE)));

		return panel;
	}

	private JPanel getJpnlWindSpeedCalibrationPanel() {
		final JPanel panel = new JPanel();

		final List<JLabel> speedLabelList = new ArrayList<>(8);
		
		for (int i = 0; i < 8; i++) {
			speedLabelList.add(i, new JLabel(windSpeedHeadingLabels[i]));
			speedLabelList.get(i).setHorizontalAlignment(SwingConstants.CENTER);
			speedLabelList.get(i).setHorizontalTextPosition(SwingConstants.CENTER);
		}

		panel.setBorder(BorderFactory.createTitledBorder(null, "Wind Speed Calibration",
				TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Segoe UI", 1, 12))); 
		
		final GroupLayout layout = new GroupLayout(panel);
		
		panel.setLayout(layout);
		
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
				.addGap(23, 23, 23)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addComponent(speedLabelList.get(0), GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(speedLabelList.get(1), GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(speedLabelList.get(2), GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(speedLabelList.get(3), GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(speedLabelList.get(4), GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(speedLabelList.get(5),  GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(speedLabelList.get(6), GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(speedLabelList.get(7), GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
					.addComponent(jSpinSpeed.get(0), GroupLayout.PREFERRED_SIZE, 66, GroupLayout.PREFERRED_SIZE)
					.addComponent(jSpinSpeed.get(1), GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
					.addComponent(jSpinSpeed.get(2), GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
					.addComponent(jSpinSpeed.get(3), GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
					.addComponent(jSpinSpeed.get(4), GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
					.addComponent(jSpinSpeed.get(5), GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
					.addComponent(jSpinSpeed.get(6), GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
					.addComponent(jSpinSpeed.get(7), GroupLayout.PREFERRED_SIZE, 66, GroupLayout.PREFERRED_SIZE))
				.addGap(23, 23, 23)));
	
		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
				.addGap(15, 15, 15)
				.addGroup(layout
					.createParallelGroup(GroupLayout.Alignment.LEADING, false)
					.addComponent(jSpinSpeed.get(0))
					.addComponent(speedLabelList.get(0), GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout
					.createParallelGroup(GroupLayout.Alignment.LEADING, false)
					.addComponent(jSpinSpeed.get(1))
					.addComponent(speedLabelList.get(1), GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout
					.createParallelGroup(GroupLayout.Alignment.LEADING, false)
					.addComponent(jSpinSpeed.get(2))
					.addComponent(speedLabelList.get(2), GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout
					.createParallelGroup(GroupLayout.Alignment.LEADING, false)
					.addComponent(jSpinSpeed.get(3))
					.addComponent(speedLabelList.get(3), GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout
					.createParallelGroup(GroupLayout.Alignment.LEADING, false)
					.addComponent(jSpinSpeed.get(4))
					.addComponent(speedLabelList.get(4), GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout
					.createParallelGroup(GroupLayout.Alignment.LEADING, false)
					.addComponent(jSpinSpeed.get(5))
					.addComponent(speedLabelList.get(5), GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout
					.createParallelGroup(GroupLayout.Alignment.LEADING, false)
					.addComponent(jSpinSpeed.get(6))
					.addComponent(speedLabelList.get(6), GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout
					.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(speedLabelList.get(7), GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
					.addComponent(jSpinSpeed.get(7), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addContainerGap(15, Short.MAX_VALUE)));
	
		return panel;
	}

}
