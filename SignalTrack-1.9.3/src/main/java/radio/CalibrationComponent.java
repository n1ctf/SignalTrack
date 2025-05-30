package radio;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.Window;

import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.File;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.text.PlainDocument;

import hamlib.RigCodes;
import utility.DoubleFilter;
import utility.IntegerFilter;

// This CalibrationComponent() is the GUI used for making changes to a CalibrationDataObject(), which is a representation
// of a file system based record. 

public class CalibrationComponent extends JDialog {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(CalibrationComponent.class.getName());

    private JPanel chartPanel;
    private JPanel rssiTextFieldPanel;
    private JPanel calibrationGraphic;

    private JComboBox<Double> dBmComboBox;
    private DefaultComboBoxModel<Double> dBmComboBoxModel;
    
    private JComboBox<String> modelComboBox;
    private DefaultComboBoxModel<String> modelComboBoxModel;
    
    private JComboBox<String> manufacturerComboBox;
    private DefaultComboBoxModel<String> manufacturerComboBoxModel;
    
    private JComboBox<String> sourceComboBox;
    private DefaultComboBoxModel<String> sourceComboBoxModel;

    private JLabel calFileTetFieldLabel;
    private JLabel manufacturerComboBoxLabel;
    private JLabel modelComboBoxLabel;
    private JLabel equalSignLabel;
    private JLabel snTextFieldLabel;
    private JLabel rssiCurrentLabel;
    private JLabel rssiTextFieldLabel;
    private JLabel acrTextFieldLabel;
    private JLabel quietingTextFieldLabel;
    private JLabel berTextFieldLabel;
    private JLabel sinadTextFieldLabel;
    private JLabel dBmComboBoxLabel;
    private JLabel sourceComboBoxLabel;
    private JLabel noiseFloorTextFieldLabel;
    private JLabel saturationTextFieldLabel;

    private JButton okButton;
    private JButton cancelButton;
    private JButton applyButton;
    private JButton rssiSetButton;

    private JFormattedTextField calFileTextField;
    private JFormattedTextField rssiCurrentTextField;
    private JFormattedTextField rssiTextField;
    private JFormattedTextField snTextField;
    private JFormattedTextField acrTextField;
    private JFormattedTextField quietingTextField;
    private JFormattedTextField berTextField;
    private JFormattedTextField sinadTextField;
    private JFormattedTextField noiseFloorTextField;
    private JFormattedTextField saturationTextField;

    private transient HierarchyListener hl;
    private final transient CalibrationDataObject cdo;
    
    public CalibrationComponent(final CalibrationDataObject cdo) {
    	this.cdo = cdo;
    	
    	initializeFrame();
        initializeComponents();
        
        loadCalFileFormattedTextField(cdo.getCalFile());
        loadManufacturerStringComboBox(cdo.getSource());
        loadModelStringComboBox(cdo.getSource(), cdo.getManufacturer());
        loadSourceComboBox(cdo.getSource());
        if (loaddBmComboBox()) { 
        	updateChart();
        	createGui();
        	addListeners();
        	normalizeControls();
        }
    }

    private void initializeFrame() {
        setModal(false);
        setTitle("Receiver Calibration Utility");
    }
    
    public CalibrationDataObject getCalibrationDataObject() {
        return cdo;
    }

    public void setCurrentRSSI(int rssi) {
        rssiSetButton.setEnabled(true);
        rssiCurrentTextField.setText(String.valueOf(rssi) + " / " + cdo.getdBmElement(rssi) + "dBm");
    }

    private void applyDoubleFilter(JFormattedTextField jftf) {
        final PlainDocument pDoc = (PlainDocument) jftf.getDocument();
        pDoc.setDocumentFilter(new DoubleFilter());
    }

    private void applyIntegerFilter(JFormattedTextField jftf) {
        final PlainDocument pDoc = (PlainDocument) jftf.getDocument();
        pDoc.setDocumentFilter(new IntegerFilter());
    }

    private boolean loaddBmComboBox() {
    	boolean valid = false;
        try {
        	dBmComboBox.setEnabled(false);
            int selectedIndex = dBmComboBox.getSelectedIndex();
            if (selectedIndex < 0) {
                selectedIndex = 0;
            }
            dBmComboBox.removeAllItems();
            dBmComboBoxModel.addAll(Arrays.asList(cdo.getCalibrationSelectordBmArray()));
            dBmComboBox.setSelectedIndex(selectedIndex >= dBmComboBox.getComponentCount() ? 0 : selectedIndex);
            dBmComboBox.setEnabled(true);
            valid = true;
        } catch (IllegalArgumentException _) {
        	setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        	LOG.log(Level.INFO, "Calibration record for {0} {1} {2} is incomplete", new Object[] { 
        		cdo.getManufacturer(), cdo.getModelString(), cdo.getSerialString() });     
        	JOptionPane.showMessageDialog(new JDialog(),
        		"Calibration record for " + cdo.getManufacturer() + " " + cdo.getModelString() + ", serial number " + 
        	cdo.getSerialString() + " is incomplete", 
        		"Calibration Record Error", JOptionPane.ERROR_MESSAGE);
    	}
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        return valid;        
    }

    private void loadCalFileFormattedTextField(File calFile) {
    	calFileTextField.setText(calFile.getAbsolutePath());
    }
    
    private void loadManufacturerStringComboBox(String source) {
        invokeLaterInDispatchThreadIfNeeded(() -> {
            manufacturerComboBox.setEnabled(false);
            manufacturerComboBoxModel.removeAllElements();
            if (source.contains(AbstractRadioReceiver.getProviderCatalog().get(1))) {
                manufacturerComboBoxModel.addAll(RigCodes.getManufacturerSet());
            }
            if (source.contains(AbstractRadioReceiver.getProviderCatalog().get(0))) {
            	final List<String> list = new ArrayList<>();
                for (String signalTrackCatalogEntry : AbstractRadioReceiver.getRadioCatalog()) {
                	final AbstractRadioReceiver abstractRadioReceiver = AbstractRadioReceiver.getRadioInstance(signalTrackCatalogEntry);
                    if (list.isEmpty() || !list.contains(abstractRadioReceiver.getManufacturer())) {
                        list.add(abstractRadioReceiver.getManufacturer());
                        manufacturerComboBoxModel.addElement(abstractRadioReceiver.getManufacturer());
                    }
                }
            }
            manufacturerComboBox.setSelectedItem(cdo.getManufacturer());
            manufacturerComboBox.setEnabled(true);
        });
    }

    private void loadSourceComboBox(String source) {
        invokeLaterInDispatchThreadIfNeeded(() -> {
            sourceComboBox.setEnabled(false);
            sourceComboBoxModel.removeAllElements();
            sourceComboBoxModel.addAll(AbstractRadioReceiver.getProviderCatalog());
            sourceComboBox.setSelectedItem(source);
            sourceComboBox.setEnabled(true);
        });
    }

    private void loadModelStringComboBox(String source, String mfr) {
        invokeLaterInDispatchThreadIfNeeded(() -> {
            modelComboBox.setEnabled(false);
            modelComboBoxModel.removeAllElements();
            if (source.contains(AbstractRadioReceiver.getProviderCatalog().get(1))) {
                modelComboBoxModel.addAll(RigCodes.getModelSetForManufacturer(mfr));
            }
            if (source.contains(AbstractRadioReceiver.getProviderCatalog().get(0))) {
            	final List<String> list = new ArrayList<>();
                for (String signalTrackCatalogEntry : AbstractRadioReceiver.getRadioCatalog()) {
                	final AbstractRadioReceiver abstractRadioReceiver = AbstractRadioReceiver.getRadioInstance(signalTrackCatalogEntry);
                    if (abstractRadioReceiver.getManufacturer().toUpperCase(Locale.getDefault()).contains(mfr.toUpperCase(Locale.getDefault()))
                            && (!list.isEmpty() || !list.contains(abstractRadioReceiver.getModel()))) {
                        list.add(abstractRadioReceiver.getModel());
                    }
                }
                modelComboBoxModel.addAll(list);
            }
            modelComboBox.setSelectedItem(cdo.getModelString());
            modelComboBox.setEnabled(true);
        });
    }

    private void updateChart() {
        final SwingWorker<JPanel, Void> worker = new SwingWorker<JPanel, Void>() {
            @Override
            protected JPanel doInBackground() throws Exception {
                return new RadioCalibrationPlot(cdo.getXYSeriesCollection(), "RSSI", "dBm");
            }

            @Override
            protected void done() {
                try {
                    chartPanel.removeAll();
                    final JPanel rcp = get();
                    chartPanel.add(rcp, BorderLayout.CENTER);
                    chartPanel.revalidate();
                } catch (final InterruptedException e) {
                    LOG.log(Level.WARNING, null, e);
                    Thread.currentThread().interrupt();
                } catch (ExecutionException | IllegalArgumentException e) {
                    LOG.log(Level.WARNING, null, e);
                } 
            }
        };
        worker.execute();
    }

    private void cancelButtonActionListenerEvent() {
        dispose();
    }

    private void okButtonActionListenerEvent() {
        applyButtonActionListenerEvent();
        dispose();
    }
    
    private void applyButtonActionListenerEvent() {
    	cdo.save();
    }
    
    private boolean setupListenersWhenConnected() {
        final Window parentFrame = (Window) getParent();
        parentFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                if (WindowEvent.WINDOW_CLOSING == event.getID()) {
                	cdo.save();
                }
            }
        });
        return true;
    }

    private void addListeners() {
    	addHierarchyListener(hl);
    	
    	okButton.addActionListener(_ -> okButtonActionListenerEvent());
        cancelButton.addActionListener(_ -> cancelButtonActionListenerEvent());
        applyButton.addActionListener(_ -> applyButtonActionListenerEvent());

        rssiTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent event) {
                rssiTextFieldFocusGainedEvent(event);
            }

            @Override
            public void focusLost(FocusEvent event) {
                rssiTextFieldFocusLostEvent(event);
            }
        });

        rssiTextField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent event) {
            	// NOOP
            }

            @Override
            public void keyPressed(KeyEvent event) {
                rssiTextFieldKeyPressedEvent(event);
            }

            @Override
            public void keyReleased(KeyEvent event) {
            	// NOOP
            }
        });

        snTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent event) {
                snTextFieldFocusGainedEvent(event);
            }

            @Override
            public void focusLost(FocusEvent event) {
                snTextFieldFocusLostEvent(event);
            }
        });

        snTextField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent event) {
            	// NOOP
            }

            @Override
            public void keyPressed(KeyEvent event) {
                snTextFieldKeyPressedEvent(event);
            }

            @Override
            public void keyReleased(KeyEvent event) {
            	// NOOP
            }
        });

        quietingTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent event) {
                quietingTextFieldFocusGainedEvent(event);
            }

            @Override
            public void focusLost(FocusEvent event) {
                quietingTextFieldFocusLostEvent(event);
            }
        });

        quietingTextField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent event) {
            	// NOOP
            }

            @Override
            public void keyPressed(KeyEvent event) {
                quietingTextFieldKeyPressedEvent(event);
            }

            @Override
            public void keyReleased(KeyEvent event) {
            	// NOOP
            }
        });

        berTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent event) {
                berTextFieldFocusGainedEvent(event);
            }

            @Override
            public void focusLost(FocusEvent event) {
                berTextFieldFocusLostEvent(event);
            }
        });

        berTextField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent event) {
            	// NOOP
            }

            @Override
            public void keyPressed(KeyEvent event) {
                berTextFieldKeyPressedEvent(event);
            }

            @Override
            public void keyReleased(KeyEvent event) {
            	// NOOP
            }
        });

        sinadTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent event) {
                sinadTextFieldFocusGainedEvent(event);
            }

            @Override
            public void focusLost(FocusEvent event) {
                sinadTextFieldFocusLostEvent(event);
            }
        });

        sinadTextField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent event) {
            	// NOOP
            }

            @Override
            public void keyPressed(KeyEvent event) {
                sinadTextFieldKeyPressedEvent(event);
            }

            @Override
            public void keyReleased(KeyEvent event) {
            	// NOOP
            }
        });

        saturationTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent event) {
                saturationTextFieldFocusGainedEvent(event);
            }

            @Override
            public void focusLost(FocusEvent event) {
                saturationTextFieldFocusLostEvent(event);
            }
        });

        saturationTextField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent event) {
            	// NOOP
            }

            @Override
            public void keyPressed(KeyEvent event) {
                saturationTextFieldKeyPressedEvent(event);
            }

            @Override
            public void keyReleased(KeyEvent event) {
            	// NOOP
            }
        });

        noiseFloorTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent event) {
                noiseFloorTextFieldFocusGainedEvent(event);
            }

            @Override
            public void focusLost(FocusEvent event) {
                noiseFloorTextFieldFocusLostEvent(event);
            }
        });

        noiseFloorTextField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent event) {
            	// NOOP
            }

            @Override
            public void keyPressed(KeyEvent event) {
                noiseFloorTextFieldKeyPressedEvent(event);
            }

            @Override
            public void keyReleased(KeyEvent event) {
            	// NOOP
            }
        });

        acrTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent event) {
                acrTextFieldFocusGainedEvent(event);
            }

            @Override
            public void focusLost(FocusEvent event) {
                acrTextFieldFocusLostEvent(event);
            }
        });

        acrTextField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent event) {
            	// NOOP
            }

            @Override
            public void keyPressed(KeyEvent event) {
                acrTextFieldKeyPressedEvent(event);
            }

            @Override
            public void keyReleased(KeyEvent event) {
            	// NOOP
            }
        });

        rssiSetButton.addActionListener(_ -> rssiSetButtonActionEvent());

        dBmComboBox.addItemListener(event -> {
            if (event.getStateChange() == ItemEvent.SELECTED) {
                if (!dBmComboBox.isEnabled()) {
                    return;
                }
                dBmComboBoxItemSelectedEvent(event);
            }
        });

        modelComboBox.addItemListener(_ -> {
            if (!modelComboBox.isEnabled()) {
                return;
            }
            cdo.setModelString(modelComboBox.getSelectedItem().toString());
            noiseFloorTextField.setText(String.valueOf(cdo.getNoiseFloor()));
            saturationTextField.setText(String.valueOf(cdo.getSaturation()));
        });

        manufacturerComboBox.addItemListener(_ -> {
            if (!manufacturerComboBox.isEnabled()) {
                return;
            }
            cdo.setManufacturer(manufacturerComboBox.getSelectedItem().toString());
            loadModelStringComboBox(sourceComboBox.getSelectedItem().toString(),
                    manufacturerComboBox.getSelectedItem().toString());
        });

        sourceComboBox.addItemListener(event -> {
            if (!sourceComboBox.isEnabled()) {
                return;
            }
            sourceComboBoxItemChangedEvent(event);
        });
        
        final String cancelName = "cancel";
        final InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), cancelName);
        final ActionMap actionMap = getRootPane().getActionMap();

        actionMap.put(cancelName, new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                cdo.save();
            }
        });
    }
    
    private String getSourceString() {
        String source = null;
        if (((String) sourceComboBox.getSelectedItem()).contains("HAMLIB")) {
            source = sourceComboBox.getSelectedItem().toString() + " "
                    + RigCodes.getRigCode(manufacturerComboBox.getSelectedItem().toString(),
                            modelComboBox.getSelectedItem().toString());
        }
        if (sourceComboBox.getSelectedItem().toString().contains("SIGNALTRACK")) {
            source = "SIGNALTRACK";
        }
        return source;
    }

    @SuppressWarnings("unchecked")
    private void sourceComboBoxItemChangedEvent(ItemEvent event) {
        final JComboBox<String> cb = (JComboBox<String>) event.getSource();
        cdo.setSource(getSourceString());
        loadManufacturerStringComboBox(sourceComboBox.getSelectedItem().toString());
        loadModelStringComboBox(cb.getSelectedItem().toString(), manufacturerComboBox.getSelectedItem().toString());
    }
    
    private void rssiTextFieldFocusLostEvent(FocusEvent event) {
    	final JFormattedTextField tf = (JFormattedTextField) event.getSource();
        tf.setFont(new Font("Tahoma", Font.PLAIN, 11));
        cdo.setRssi(dBmComboBox.getSelectedIndex(), Integer.parseInt(tf.getText()));
        updateChart();
    }
    
    private void rssiTextFieldFocusGainedEvent(FocusEvent event) {
    	final JFormattedTextField tf = (JFormattedTextField) event.getSource();
        tf.setFont(new Font("Tahoma", Font.BOLD, 11));
    }

    private void rssiTextFieldKeyPressedEvent(KeyEvent event) {
    	final JFormattedTextField tf = (JFormattedTextField) event.getSource();
        if (event.getKeyCode() == KeyEvent.VK_ENTER) {
            tf.setFont(new Font("Tahoma", Font.PLAIN, 11));
            tf.transferFocus();
        }
    }

    private void snTextFieldFocusLostEvent(FocusEvent event) {
    	final JFormattedTextField tf = (JFormattedTextField) event.getSource();
        tf.setFont(new Font("Tahoma", Font.PLAIN, 11));
        cdo.setSerialString(snTextField.getText());
    }

    private void snTextFieldFocusGainedEvent(FocusEvent event) {
    	final JFormattedTextField tf = (JFormattedTextField) event.getSource();
        tf.setFont(new Font("Tahoma", Font.BOLD, 11));
    }

    private void snTextFieldKeyPressedEvent(KeyEvent event) {
    	final JFormattedTextField tf = (JFormattedTextField) event.getSource();
        if (event.getKeyCode() == KeyEvent.VK_ENTER) {
            tf.setFont(new Font("Tahoma", Font.PLAIN, 11));
            tf.transferFocus();
        }
    }

    private void noiseFloorTextFieldFocusLostEvent(FocusEvent event) {
    	final JFormattedTextField tf = (JFormattedTextField) event.getSource();
        tf.setFont(new Font("Tahoma", Font.PLAIN, 11));
        cdo.setNoiseFloor(Double.parseDouble(tf.getText()));
        loaddBmComboBox();
        updateChart();
    }

    private void noiseFloorTextFieldFocusGainedEvent(FocusEvent event) {
    	final JFormattedTextField tf = (JFormattedTextField) event.getSource();
        tf.setFont(new Font("Tahoma", Font.BOLD, 11));
    }

    private void noiseFloorTextFieldKeyPressedEvent(KeyEvent event) {
    	final JFormattedTextField tf = (JFormattedTextField) event.getSource();
        if (event.getKeyCode() == KeyEvent.VK_ENTER) {
            tf.setFont(new Font("Tahoma", Font.PLAIN, 11));
            tf.transferFocus();
        }
    }

    private void saturationTextFieldFocusLostEvent(FocusEvent event) {
    	final JFormattedTextField tf = (JFormattedTextField) event.getSource();
        tf.setFont(new Font("Tahoma", Font.PLAIN, 11));
        cdo.setSaturation(Double.parseDouble(tf.getText()));
        loaddBmComboBox();
        updateChart();
    }

    private void saturationTextFieldFocusGainedEvent(FocusEvent event) {
    	final JFormattedTextField tf = (JFormattedTextField) event.getSource();
        tf.setFont(new Font("Tahoma", Font.BOLD, 11));
    }

    private void saturationTextFieldKeyPressedEvent(KeyEvent event) {
    	final JFormattedTextField tf = (JFormattedTextField) event.getSource();
        if (event.getKeyCode() == KeyEvent.VK_ENTER) {
            tf.setFont(new Font("Tahoma", Font.PLAIN, 11));
            tf.transferFocus();
        }
    }

    private void acrTextFieldFocusLostEvent(FocusEvent event) {
    	final JFormattedTextField tf = (JFormattedTextField) event.getSource();
        tf.setFont(new Font("Tahoma", Font.PLAIN, 11));
        cdo.setAdjacentChannelRejection(Double.parseDouble(tf.getText()));
    }

    private void acrTextFieldFocusGainedEvent(FocusEvent event) {
    	final JFormattedTextField tf = (JFormattedTextField) event.getSource();
        tf.setFont(new Font("Tahoma", Font.BOLD, 11));
    }

    private void acrTextFieldKeyPressedEvent(KeyEvent event) {
    	final JFormattedTextField tf = (JFormattedTextField) event.getSource();
        if (event.getKeyCode() == KeyEvent.VK_ENTER) {
            tf.setFont(new Font("Tahoma", Font.PLAIN, 11));
            tf.transferFocus();
        }
    }

    private void sinadTextFieldFocusLostEvent(FocusEvent event) {
    	final JFormattedTextField tf = (JFormattedTextField) event.getSource();
        tf.setFont(new Font("Tahoma", Font.PLAIN, 11));
        cdo.setSignalReqFor12dBSINAD(Double.parseDouble(tf.getText()));
    }

    private void sinadTextFieldFocusGainedEvent(FocusEvent event) {
    	final JFormattedTextField tf = (JFormattedTextField) event.getSource();
        tf.setFont(new Font("Tahoma", Font.BOLD, 11));
    }

    private void sinadTextFieldKeyPressedEvent(KeyEvent event) {
    	final JFormattedTextField tf = (JFormattedTextField) event.getSource();
        if (event.getKeyCode() == KeyEvent.VK_ENTER) {
            tf.setFont(new Font("Tahoma", Font.PLAIN, 11));
            tf.transferFocus();
        }
    }

    private void berTextFieldFocusLostEvent(FocusEvent event) {
    	final JFormattedTextField tf = (JFormattedTextField) event.getSource();
        tf.setFont(new Font("Tahoma", Font.PLAIN, 11));
        cdo.setSignalReqFor5PctBER(Double.parseDouble(tf.getText()));
    }

    private void berTextFieldFocusGainedEvent(FocusEvent event) {
    	final JFormattedTextField tf = (JFormattedTextField) event.getSource();
        tf.setFont(new Font("Tahoma", Font.BOLD, 11));
    }

    private void berTextFieldKeyPressedEvent(KeyEvent event) {
    	final JFormattedTextField tf = (JFormattedTextField) event.getSource();
        if (event.getKeyCode() == KeyEvent.VK_ENTER) {
            tf.setFont(new Font("Tahoma", Font.PLAIN, 11));
            tf.transferFocus();
        }
    }

    private void quietingTextFieldFocusLostEvent(FocusEvent event) {
    	final JFormattedTextField tf = (JFormattedTextField) event.getSource();
        tf.setFont(new Font("Tahoma", Font.PLAIN, 11));
        cdo.setSignalReqFor20dBQuieting(Double.parseDouble(tf.getText()));
    }

    private void quietingTextFieldFocusGainedEvent(FocusEvent event) {
    	final JFormattedTextField tf = (JFormattedTextField) event.getSource();
        tf.setFont(new Font("Tahoma", Font.BOLD, 11));
    }

    private void quietingTextFieldKeyPressedEvent(KeyEvent event) {
    	final JFormattedTextField tf = (JFormattedTextField) event.getSource();
        if (event.getKeyCode() == KeyEvent.VK_ENTER) {
            tf.setFont(new Font("Tahoma", Font.PLAIN, 11));
            tf.transferFocus();
        }
    }

    private void rssiSetButtonActionEvent() {
        final int rssiSetValue = Integer.parseInt(rssiCurrentTextField.getText());
        cdo.setRssi(dBmComboBox.getSelectedIndex(), rssiSetValue);
        loaddBmComboBox();
        rssiTextField.setText(String.valueOf(cdo.getRssi(dBmComboBox.getSelectedIndex())));
        updateChart();
    }

    private void dBmComboBoxItemSelectedEvent(ItemEvent event) {
        final JComboBox<?> cb = (JComboBox<?>) event.getSource();
        rssiTextField.setText(String.valueOf(cdo.getRssi(cb.getSelectedIndex())));
        rssiSetButton.setEnabled(false);
    }

    private void normalizeControls() {
    	invokeLaterInDispatchThreadIfNeeded(() -> {
            noiseFloorTextField.setText(String.valueOf(cdo.getNoiseFloor()));
            saturationTextField.setText(String.valueOf(cdo.getSaturation()));
            sinadTextField.setText(String.valueOf(cdo.getSignalReqFor12dBSINAD()));
            rssiTextField.setText(String.valueOf(cdo.getRssi(dBmComboBox.getSelectedIndex())));
            quietingTextField.setText(String.valueOf(cdo.getSignalReqFor20dBQuieting()));
            acrTextField.setText(String.valueOf(cdo.getAdjacentChannelRejection()));
            berTextField.setText(String.valueOf(cdo.getSignalReqFor5PctBER()));
            snTextField.setText(String.valueOf(cdo.getSerialString()));
            rssiCurrentTextField.setText("000");

            calFileTextField.setEnabled(false);
            
            noiseFloorTextField.setEnabled(true);
            saturationTextField.setEnabled(true);
            sinadTextField.setEnabled(true);
            rssiTextField.setEnabled(true);
            quietingTextField.setEnabled(true);
            acrTextField.setEnabled(true);
            berTextField.setEnabled(true);
            snTextField.setEnabled(false);

            dBmComboBox.setEnabled(true);
            dBmComboBox.setRenderer(setCustomColors(null, Color.BLACK));

            modelComboBox.setEnabled(false);
            modelComboBox.setRenderer(setCustomColors(null, Color.GRAY));

            manufacturerComboBox.setEnabled(false);
            manufacturerComboBox.setRenderer(setCustomColors(null, Color.GRAY));

            sourceComboBox.setEnabled(false);
            sourceComboBox.setRenderer(setCustomColors(null, Color.GRAY));

            rssiSetButton.setEnabled(false);
            applyButton.setEnabled(true);
            okButton.setEnabled(true);
            cancelButton.setEnabled(true);

            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        });
    }
    
    private void initializeComponents() {
        try {
        	hl = new HierarchyListener() {
                @Override
                public void hierarchyChanged(HierarchyEvent event) {
                    if (event.getChangeFlags() == HierarchyEvent.DISPLAYABILITY_CHANGED) {
                        setupListenersWhenConnected();
                        removeHierarchyListener(this);
                    }
                }
            };
        	
            okButton = new JButton("OK");
            okButton.setFont(new Font("Tahoma", Font.BOLD, 11));
            okButton.setMultiClickThreshhold(50L);

            cancelButton = new JButton("Cancel");
            cancelButton.setFont(new Font("Tahoma", Font.BOLD, 11));
            cancelButton.setMultiClickThreshhold(50L);

            applyButton = new JButton("Apply");
            applyButton.setFont(new Font("Tahoma", Font.BOLD, 11));
            applyButton.setMultiClickThreshhold(50L);

            calibrationGraphic = new JPanel();
            calibrationGraphic.setBorder(BorderFactory.createTitledBorder("RSSI vs. Actual dBm Calibration Plot"));
            calibrationGraphic.setFont(new Font("Tahoma", Font.PLAIN, 11));

            chartPanel = new JPanel();

            rssiTextFieldPanel = new JPanel();
            rssiTextFieldPanel.setFont(new Font("Tahoma", Font.PLAIN, 11));
            rssiTextFieldPanel.setBorder(BorderFactory.createTitledBorder("RSSI vs. Actual dBm Calibration Table"));

            rssiSetButton = new JButton("SET");
            rssiSetButton.setFont(new Font("Tahoma", Font.BOLD, 11));
            rssiSetButton.setMultiClickThreshhold(50L);

            equalSignLabel = new JLabel("=");
            equalSignLabel.setFont(new Font("Tahoma", Font.BOLD, 18));
            equalSignLabel.setHorizontalAlignment(SwingConstants.CENTER);
            equalSignLabel.setHorizontalTextPosition(SwingConstants.CENTER);

            rssiCurrentTextField = new JFormattedTextField("0");
            rssiCurrentTextField.setFont(new Font("Tahoma", Font.PLAIN, 11));
            rssiCurrentTextField.setHorizontalAlignment(SwingConstants.CENTER);
            rssiCurrentTextField.setToolTipText("The current RSSI reported by the radio");
            rssiCurrentTextField.setBackground(Color.WHITE);
            rssiCurrentTextField.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
            rssiCurrentTextField.setFocusable(false);
            rssiCurrentLabel = new JLabel("Received RSSI");
            rssiCurrentLabel.setHorizontalAlignment(SwingConstants.CENTER);
            rssiCurrentLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));

            rssiTextField = new JFormattedTextField();
            rssiTextField.setFont(new Font("Tahoma", Font.PLAIN, 11));
            rssiTextField.setHorizontalAlignment(SwingConstants.CENTER);
            rssiTextField.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
            rssiTextField.setToolTipText("The RSSI value, as detected by the radio, when a given signal level is applied to the antenna port.");
            rssiTextField.setDisabledTextColor(Color.BLACK);
            
            applyIntegerFilter(rssiTextField);
            
            rssiTextFieldLabel = new JLabel("RSSI:");
            rssiTextFieldLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));
            rssiTextFieldLabel.setHorizontalAlignment(SwingConstants.CENTER);

            calFileTextField = new JFormattedTextField();
            calFileTextField.setForeground(Color.BLACK);
            calFileTextField.setFont(new Font("Tahoma", Font.BOLD, 11));
            calFileTextField.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
            calFileTextField.setHorizontalAlignment(SwingConstants.CENTER);
            calFileTextField.setToolTipText("Calibration file to view, edit or rename");
            calFileTextField.setDisabledTextColor(Color.BLACK);
            
            calFileTetFieldLabel = new JLabel("Cal File");
            calFileTetFieldLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));
            calFileTetFieldLabel.setHorizontalAlignment(SwingConstants.LEFT);     
            
            snTextField = new JFormattedTextField();
            snTextField.setFont(new Font("Tahoma", Font.PLAIN, 11));
            snTextField.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
            snTextField.setHorizontalAlignment(SwingConstants.CENTER);
            snTextField.setToolTipText("Serial number of selected or new radio");
            snTextField.setDisabledTextColor(Color.BLACK);
            
            snTextFieldLabel = new JLabel("Serial Number");
            snTextFieldLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));
            snTextFieldLabel.setHorizontalAlignment(SwingConstants.LEFT);

            sinadTextField = new JFormattedTextField();
            sinadTextField.setFont(new Font("Tahoma", Font.PLAIN, 11));
            sinadTextField.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
            sinadTextField.setHorizontalAlignment(SwingConstants.RIGHT);
            sinadTextField.setToolTipText("dBm required at antenna port to produce 12dB of SINAD");
            sinadTextField.setDisabledTextColor(Color.BLACK);
            
            applyDoubleFilter(sinadTextField);
            
            sinadTextFieldLabel = new JLabel("Signal Requird for 12dB SINAD (dBm)");
            sinadTextFieldLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));
            sinadTextFieldLabel.setHorizontalAlignment(SwingConstants.LEFT);

            berTextField = new JFormattedTextField();
            berTextField.setFont(new Font("Tahoma", Font.PLAIN, 11));
            berTextField.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
            berTextField.setHorizontalAlignment(SwingConstants.RIGHT);
            berTextField.setToolTipText("dBm required at antenna port to produce a 5% bit error rate");
            berTextField.setDisabledTextColor(Color.BLACK);
            
            applyDoubleFilter(berTextField);
            
            berTextFieldLabel = new JLabel("Signal Required for 5% BER (dBm)");
            berTextFieldLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));
            berTextFieldLabel.setHorizontalAlignment(SwingConstants.LEFT);

            noiseFloorTextField = new JFormattedTextField();
            noiseFloorTextField.setFont(new Font("Tahoma", Font.PLAIN, 11));
            noiseFloorTextField.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
            noiseFloorTextField.setHorizontalAlignment(SwingConstants.RIGHT);
            noiseFloorTextField.setToolTipText("Effective noise level, as a function of the receiver noise figure, that limits the weakest signal that can be discerned.");
            noiseFloorTextField.setDisabledTextColor(Color.BLACK);
            
            applyDoubleFilter(noiseFloorTextField);
            
            noiseFloorTextFieldLabel = new JLabel("Noise Floor(dBm)");
            noiseFloorTextFieldLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));
            noiseFloorTextFieldLabel.setHorizontalAlignment(SwingConstants.LEFT);

            saturationTextField = new JFormattedTextField();
            saturationTextField.setFont(new Font("Tahoma", Font.PLAIN, 11));
            saturationTextField.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
            saturationTextField.setHorizontalAlignment(SwingConstants.RIGHT);
            saturationTextField.setToolTipText("Maximum signal level above which signal level changes can not be discerned.");
            saturationTextField.setDisabledTextColor(Color.BLACK);
            
            applyDoubleFilter(saturationTextField);
            
            saturationTextFieldLabel = new JLabel("Receiver Saturation (dBm)");
            saturationTextFieldLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));
            saturationTextFieldLabel.setHorizontalAlignment(SwingConstants.LEFT);

            acrTextField = new JFormattedTextField();
            acrTextField.setFont(new Font("Tahoma", Font.PLAIN, 11));
            acrTextField.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
            acrTextField.setHorizontalAlignment(SwingConstants.RIGHT);
            acrTextField.setToolTipText("dB Adjacent Channel Rejection");
            acrTextField.setDisabledTextColor(Color.BLACK);
            
            applyDoubleFilter(acrTextField);
            
            acrTextFieldLabel = new JLabel("Adjacent Channel Rejection (dB)");
            acrTextFieldLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));
            acrTextFieldLabel.setHorizontalAlignment(SwingConstants.LEFT);

            quietingTextField = new JFormattedTextField();
            quietingTextField.setFont(new Font("Tahoma", Font.PLAIN, 11));
            quietingTextField.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
            quietingTextField.setHorizontalAlignment(SwingConstants.RIGHT);
            quietingTextField.setToolTipText("Signal in dBm required at antenna port to produce 20dB of quieting");
            quietingTextField.setDisabledTextColor(Color.BLACK);
            
            applyDoubleFilter(quietingTextField);
            
            quietingTextFieldLabel = new JLabel("Signal Required for 20dB Quieting (dBm)");
            quietingTextFieldLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));
            quietingTextFieldLabel.setHorizontalAlignment(SwingConstants.LEFT);

            sourceComboBoxModel = new DefaultComboBoxModel<>();
            sourceComboBox = new JComboBox<>(sourceComboBoxModel);
            sourceComboBox.setForeground(Color.GRAY);
            sourceComboBox.setEditable(false);
            sourceComboBox.setEnabled(false);
            sourceComboBox.setFont(new Font("Tahoma", Font.BOLD, 11));
            sourceComboBox.setToolTipText("The internal software module that provides communication with the radio.");
            sourceComboBoxLabel = new JLabel("Source");
            sourceComboBoxLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));
            sourceComboBoxLabel.setHorizontalAlignment(SwingConstants.LEFT);
            sourceComboBox.setRenderer(setCustomColors(null, Color.BLACK));

            manufacturerComboBoxModel = new DefaultComboBoxModel<>();
            manufacturerComboBox = new JComboBox<>(manufacturerComboBoxModel);
            manufacturerComboBox.setForeground(Color.BLACK);
            manufacturerComboBox.setEditable(false);
            manufacturerComboBox.setEnabled(false);
            manufacturerComboBox.setFont(new Font("Tahoma", Font.BOLD, 11));
            manufacturerComboBoxLabel = new JLabel("Mfr");
            manufacturerComboBoxLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));
            manufacturerComboBoxLabel.setHorizontalAlignment(SwingConstants.LEFT);
            manufacturerComboBox.setRenderer(setCustomColors(null, Color.BLACK));       

            modelComboBoxModel = new DefaultComboBoxModel<>();
            modelComboBox = new JComboBox<>(modelComboBoxModel);
            modelComboBox.setForeground(Color.BLACK);
            modelComboBox.setEditable(false);
            modelComboBox.setEnabled(false);
            modelComboBox.setFont(new Font("Tahoma", Font.BOLD, 11));
            modelComboBoxLabel = new JLabel("Model");
            modelComboBoxLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));
            modelComboBoxLabel.setHorizontalAlignment(SwingConstants.LEFT);
            modelComboBox.setRenderer(setCustomColors(null, Color.BLACK));

            dBmComboBoxModel = new DefaultComboBoxModel<>();
            dBmComboBox = new JComboBox<>(dBmComboBoxModel);
            dBmComboBox.setEditable(false);
            dBmComboBox.setEnabled(false);
            dBmComboBox.setForeground(Color.BLACK);
            dBmComboBox.setFont(new Font("Tahoma", Font.BOLD, 11));
            dBmComboBox.setToolTipText("The signal level (in dBm), generated by external test equipment, measured at the antenna port, which will equate to the corresponding RSSI value to the left.");
            dBmComboBoxLabel = new JLabel("dBm");
            dBmComboBoxLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));
            dBmComboBoxLabel.setHorizontalAlignment(SwingConstants.LEFT);
            dBmComboBox.setRenderer(setCustomColors(null, Color.BLACK));

        } catch (final NullPointerException ex) {
        	LOG.log(Level.WARNING, null, ex);
        }
    }

    private DefaultListCellRenderer setCustomColors(Color background, Color foreground) {
        return new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public void paint(Graphics g) {
                if (foreground != null) {
                    setForeground(foreground);
                }
                if (background != null) {
                    setBackground(background);
                }
                super.paint(g);
            }
        };
    }

    public JPanel getCalibrationPanelGui() {
        final JPanel panel = new JPanel();
        final GroupLayout layout = new GroupLayout(panel);

        panel.setLayout(layout);

        final GroupLayout rssiTextFieldPanelLayout = new GroupLayout(rssiTextFieldPanel);
        rssiTextFieldPanel.setLayout(rssiTextFieldPanelLayout);

        rssiTextFieldPanelLayout.setHorizontalGroup(rssiTextFieldPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(rssiTextFieldPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(rssiTextFieldLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rssiTextField, 40, 40, 40)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(equalSignLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(dBmComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dBmComboBoxLabel)
                .addGap(18, 18, 18)
                .addGroup(rssiTextFieldPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                    .addComponent(rssiCurrentLabel)
                    .addComponent(rssiCurrentTextField, 40, 40, 40)
                    .addComponent(rssiSetButton, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        rssiTextFieldPanelLayout.setVerticalGroup(rssiTextFieldPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(rssiTextFieldPanelLayout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(rssiTextFieldPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                    .addComponent(rssiTextFieldLabel)
                    .addComponent(rssiTextField, 19, 19, 19)
                    .addComponent(equalSignLabel, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE)
                    .addComponent(dBmComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(dBmComboBoxLabel))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(rssiTextFieldPanelLayout.createSequentialGroup()
                .addComponent(rssiCurrentLabel)
                .addGap(8, 8, 8)
                .addComponent(rssiCurrentTextField)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rssiSetButton)
                .addGap(0, 11, Short.MAX_VALUE)));

        final GroupLayout calibrationGraphicLayout = new GroupLayout(calibrationGraphic);

        calibrationGraphic.setLayout(calibrationGraphicLayout);

        calibrationGraphicLayout.setHorizontalGroup(calibrationGraphicLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(calibrationGraphicLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(chartPanel, GroupLayout.DEFAULT_SIZE, 537, Short.MAX_VALUE)
                .addContainerGap()));

        calibrationGraphicLayout.setVerticalGroup(calibrationGraphicLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(chartPanel, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 349, Short.MAX_VALUE));

        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(calFileTetFieldLabel)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(calFileTextField, GroupLayout.PREFERRED_SIZE, 350, GroupLayout.PREFERRED_SIZE))
                            .addComponent(rssiTextFieldPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(manufacturerComboBoxLabel)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(manufacturerComboBox, GroupLayout.PREFERRED_SIZE, 127, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(modelComboBoxLabel)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(modelComboBox, GroupLayout.PREFERRED_SIZE, 133, GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(sourceComboBoxLabel)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(sourceComboBox, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(snTextFieldLabel)
                                .addGap(7, 7, 7)
                                .addComponent(snTextField, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(16, 16, 16)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(sinadTextFieldLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(quietingTextFieldLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(berTextFieldLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(acrTextFieldLabel, GroupLayout.PREFERRED_SIZE, 192, GroupLayout.PREFERRED_SIZE)
                                    .addComponent(noiseFloorTextFieldLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(saturationTextFieldLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                    .addComponent(acrTextField, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)
                                    .addComponent(sinadTextField, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)
                                    .addComponent(quietingTextField, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)
                                    .addComponent(berTextField, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)
                                    .addComponent(noiseFloorTextField, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)
                                    .addComponent(saturationTextField, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE))))
                    	.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    	.addComponent(calibrationGraphic, GroupLayout.DEFAULT_SIZE, 569, Short.MAX_VALUE)))
            	.addContainerGap()));

        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                            .addComponent(calFileTetFieldLabel)
                            .addComponent(calFileTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                            .addComponent(sourceComboBoxLabel)
                            .addComponent(sourceComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(snTextFieldLabel)
                            .addComponent(snTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                            .addComponent(manufacturerComboBoxLabel)
                            .addComponent(manufacturerComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(modelComboBoxLabel)
                            .addComponent(modelComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addGap(14, 14, 14)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                            .addComponent(acrTextFieldLabel)
                            .addComponent(acrTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                            .addComponent(sinadTextFieldLabel)
                            .addComponent(sinadTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                            .addComponent(quietingTextFieldLabel)
                            .addComponent(quietingTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                            .addComponent(berTextFieldLabel)
                            .addComponent(berTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                            .addComponent(noiseFloorTextFieldLabel)
                            .addComponent(noiseFloorTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                            .addComponent(saturationTextFieldLabel)
                            .addComponent(saturationTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(rssiTextFieldPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addComponent(calibrationGraphic, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        return panel;
    }

    private void createGui() {
        final GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);

        final JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab(" Calibration ", null, getCalibrationPanelGui(), null);

        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                        .addComponent(tabbedPane, GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGap(0, 660, Short.MAX_VALUE)
                                                .addComponent(okButton, 80, 80, 80)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(applyButton, 80, 80, 80)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(cancelButton, 80, 80, 80)))
                                .addContainerGap()));

        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(tabbedPane, GroupLayout.DEFAULT_SIZE, 352, Short.MAX_VALUE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(okButton)
                                        .addComponent(applyButton)
                                        .addComponent(cancelButton))
                                .addContainerGap()));

        pack();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        final Toolkit tk = Toolkit.getDefaultToolkit();
        final Dimension screenSize = tk.getScreenSize();
        setLocation((screenSize.width / 2) - (getWidth() / 2), (screenSize.height / 2) - (getHeight() / 2));

        setVisible(true);
    }

    private static void invokeLaterInDispatchThreadIfNeeded(Runnable runnable) {
        if (EventQueue.isDispatchThread()) {
            runnable.run();
        } else {
            SwingUtilities.invokeLater(runnable);
        }
    }

}
