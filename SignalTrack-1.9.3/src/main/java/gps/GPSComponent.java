package gps;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.text.PlainDocument;

import utility.DoubleFilter;

public class GPSComponent extends JDialog {
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger.getLogger(GPSComponent.class.getName());
    
    private JTabbedPane tabbedPane;
    private JButton okButton;
    private JButton cancelButton;
    private JButton applyButton;
    private JComboBox<String> deviceComboBox;
    private DefaultComboBoxModel<String> deviceComboBoxModel;
    private JLabel deviceComboBoxLabel;
    private JCheckBox cbStartGpsWithSystem;
    private JCheckBox cbEnableGpsTracking;
    private JCheckBox cbCenterMapOnGPSPosition;
    private JCheckBox cbReportCRCFailures;
    private JCheckBox cbContinuousUpdate;
    private JFormattedTextField ftfGpsCursorRadius;
    private JLabel gpsSymbolRadiusLabel;

    private final AbstractGpsProcessor abstractGpsProcessor;

    public GPSComponent(AbstractGpsProcessor abstractGpsProcessor) {
        this.abstractGpsProcessor = abstractGpsProcessor;

        initializeComponents();
        setComponentValues();
        loadDeviceComboBox();
        configureListeners();
        drawGraphicalUserInterface();
    }

    private void configureListeners() {
        okButton.addActionListener(_ -> okButtonActionListenerEvent());

        cancelButton.addActionListener(_ -> cancelButtonActionListenerEvent());

        applyButton.addActionListener(_ -> applyButtonActionListenerEvent());

        ftfGpsCursorRadius.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                ftfGpsCursorRadius.setFont(new Font("Calabri", Font.BOLD, 11));
            }

            @Override
            public void focusLost(FocusEvent e) {
            	// NOOP
            }
        });

        ftfGpsCursorRadius.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent event) {
            	// NOOP
            }

            @Override
            public void keyPressed(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_ENTER) {
                    ftfGpsCursorRadius.setFont(new Font("Calabri", Font.PLAIN, 11));
                    ftfGpsCursorRadius.transferFocus();
                }
            }

            @Override
            public void keyReleased(KeyEvent event) {
              // NOOP
            }
        });

    }

    private void applyDoubleFilter(JFormattedTextField jftf) {
        final PlainDocument pDoc = (PlainDocument) jftf.getDocument();
        pDoc.setDocumentFilter(new DoubleFilter());
    }

    private void setComponentValues() {
        ftfGpsCursorRadius.setText(new DecimalFormat("#0").format(abstractGpsProcessor.getGpsSymbolRadius()));
        cbStartGpsWithSystem.setSelected(abstractGpsProcessor.isStartGPSWithSystem());
        cbEnableGpsTracking.setSelected(abstractGpsProcessor.isEnableGPSTracking());
        cbCenterMapOnGPSPosition.setSelected(abstractGpsProcessor.isCenterMapOnGPSPosition());
        cbReportCRCFailures.setSelected(abstractGpsProcessor.isReportCRCErrors());
        cbContinuousUpdate.setSelected(abstractGpsProcessor.isContinuousUpdate());
        tabbedPane.removeAll();
        tabbedPane.addTab("GPS Settings", null, getGpsPanel(), null);
        for (JPanel panel : abstractGpsProcessor.getConfigurationComponentArray()) {
        	tabbedPane.addTab(panel.getName(), panel);
        }
    }

    private void initializeComponents() {
        setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        setTitle("GPS Settings");

        tabbedPane = new JTabbedPane();

        deviceComboBoxLabel = new JLabel("Device Type");
        deviceComboBoxLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        cbStartGpsWithSystem = new JCheckBox("Start GPS With System");
        cbEnableGpsTracking = new JCheckBox("Enable GPS Tracking");
        cbCenterMapOnGPSPosition = new JCheckBox("Center Map on GPS Position");
        cbReportCRCFailures = new JCheckBox("Report GPS Circular Redundancy Check Failures");
        cbContinuousUpdate = new JCheckBox("Update on All GPS Reports, Regardless of Change in Location");
        
        okButton = new JButton("OK");
        okButton.setMultiClickThreshhold(50L);

        cancelButton = new JButton("Cancel");
        cancelButton.setMultiClickThreshhold(50L);

        applyButton = new JButton("Apply");
        applyButton.setMultiClickThreshhold(50L);

        deviceComboBoxModel = new DefaultComboBoxModel<>();
        deviceComboBox = new JComboBox<>(deviceComboBoxModel);

        gpsSymbolRadiusLabel = new JLabel("GPS Symbol Radius (pixels)");
        gpsSymbolRadiusLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        ftfGpsCursorRadius = new JFormattedTextField();
        ftfGpsCursorRadius.setHorizontalAlignment(SwingConstants.CENTER);
        ftfGpsCursorRadius.setFont(new Font("Calabri", Font.PLAIN, 11));
        ftfGpsCursorRadius.setBackground(Color.WHITE);
        ftfGpsCursorRadius.setForeground(Color.BLACK);

        applyDoubleFilter(ftfGpsCursorRadius);
    }

    private void loadDeviceComboBox() {
        final SwingWorker<String[], Void> worker = new SwingWorker<String[], Void>() {

            @Override
            protected String[] doInBackground() throws Exception {
            	return AbstractGpsProcessor.getCatalogMap().values().toArray(new String[AbstractGpsProcessor.getCatalogMap().values().toArray().length]);
            }

            @Override
            protected void done() {
                try {
                    deviceComboBox.removeAllItems();
                    deviceComboBoxModel.addAll(Arrays.asList(get()));
                    deviceComboBox.setSelectedItem(AbstractGpsProcessor.getCatalogMap().get(abstractGpsProcessor.getClassName()));
                    deviceComboBox.addItemListener(event -> {
                        if (event.getStateChange() == ItemEvent.SELECTED) {
                            final JComboBox<?> cb = (JComboBox<?>) event.getSource();
                            final String className = AbstractGpsProcessor.getCatalogMap().getKey(cb.getSelectedItem());
                            abstractGpsProcessor.saveClassName(className);
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
    }

    private void applyButtonActionListenerEvent() {
        abstractGpsProcessor.setReportCRCErrors(cbReportCRCFailures.isSelected());
        abstractGpsProcessor.setGpsSymbolRadius(Integer.parseInt(ftfGpsCursorRadius.getText()));
        abstractGpsProcessor.setStartGPSWithSystem(cbStartGpsWithSystem.isSelected());
        abstractGpsProcessor.setEnableGPSTracking(cbEnableGpsTracking.isSelected());
        abstractGpsProcessor.setCenterMapOnGPSPosition(cbCenterMapOnGPSPosition.isSelected());
        abstractGpsProcessor.setContinuousUpdate(cbContinuousUpdate.isSelected());
        abstractGpsProcessor.savePreferences();
    }
    
    private void okButtonActionListenerEvent() {
        applyButton.doClick();
        dispose();
    }

    private void cancelButtonActionListenerEvent() {
        dispose();
    }

    private JPanel getGpsPanel() {
    	final JPanel panel = new JPanel();
    	final GroupLayout gpsPanelLayout = new GroupLayout(panel);
        panel.setLayout(gpsPanelLayout);
        gpsPanelLayout.setAutoCreateGaps(true);
        gpsPanelLayout.setAutoCreateContainerGaps(true);

        gpsPanelLayout.setHorizontalGroup(gpsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(gpsPanelLayout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addGroup(gpsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(cbReportCRCFailures)
                                .addComponent(cbStartGpsWithSystem)
                                .addComponent(cbCenterMapOnGPSPosition)
                                .addComponent(cbEnableGpsTracking)
                                .addComponent(cbContinuousUpdate)
                                .addGroup(gpsPanelLayout.createSequentialGroup()
                                        .addComponent(gpsSymbolRadiusLabel)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(ftfGpsCursorRadius, 40, 40, 40))
                                .addGroup(gpsPanelLayout.createSequentialGroup()
                                        .addComponent(deviceComboBoxLabel)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(deviceComboBox, 400, 400, 400)))
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        gpsPanelLayout.setVerticalGroup(gpsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(gpsPanelLayout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addGroup(gpsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(deviceComboBoxLabel)
                                .addComponent(deviceComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addGap(25, 25, 25)
                        .addComponent(cbEnableGpsTracking)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbCenterMapOnGPSPosition)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbStartGpsWithSystem)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbReportCRCFailures)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbContinuousUpdate)
                        .addGap(25, 25, 25)
                        .addGroup(gpsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(gpsSymbolRadiusLabel)
                                .addComponent(ftfGpsCursorRadius, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
    	
    	return panel;
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
                                        .addComponent(tabbedPane, 650, 650, 650)) 
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(okButton, 90, 90, 90)
                                        .addComponent(applyButton, 90, 90, 90)
                                        .addComponent(cancelButton, 90, 90, 90)))
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(tabbedPane, 330, 330, 350)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(okButton)
                                .addComponent(applyButton)
                                .addComponent(cancelButton))
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        final Toolkit tk = Toolkit.getDefaultToolkit();
        final Dimension screenSize = tk.getScreenSize();

        pack();
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLocation((screenSize.width / 2) - (getWidth() / 2), (screenSize.height / 2) - (getHeight() / 2));
        setResizable(false);
        setVisible(true);
    }
    
}

