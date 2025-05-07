package radio;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.IllegalComponentStateException;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;

import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import java.beans.PropertyChangeListener;

import java.io.File;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.text.NumberFormatter;

import components.SortedComboBoxModel;
import radio.AbstractRadioReceiver.AccessMode;
import radio.AbstractRadioReceiver.DataString;
import radio.AbstractRadioReceiver.StandardModeName;

public class RadioComponent extends JDialog {

    private static final long serialVersionUID = 1L;

    public static final File DEFAULT_CAL_FILE_PARENT = new File(System.getProperty("user.home") + File.separator
            + "SignalTrack" + File.separator + "cal");

    private static final Logger LOG = Logger.getLogger(RadioComponent.class.getName());

    private boolean applied;

    private JButton okButton;
    private JButton cancelButton;
    private JButton applyButton;
    private JButton openCalFileEditorButton;
    private JButton newFileButton;

    private SortedComboBoxModel<String> calFileComboBoxModel;
    private DefaultComboBoxModel<String>[] jcboSquelchCodeModelArray;
    private DefaultComboBoxModel<String>[] jcboSquelchModeModelArray;
    private DefaultComboBoxModel<String>[] jcboColorCodeArray;
    
    private JComboBox<?> deviceComboBox;
    private JComboBox<?> calFileComboBox;
    private JComboBox<String> jcboColorCode;
    private JComboBox<String> jcboDPL;
    private JComboBox<Integer> jcboFilter;
    private JComboBox<String> jcboMode;
    private JComboBox<String> jcboSquelchMode;
    private JComboBox<String> jcboNAC;
    private JComboBox<String> jcboPL;
    
    private JComboBox<?>[] jcboModeArray;
    private JComboBox<?>[] jcboSquelchCodeArray;
    private JComboBox<?>[] jcboSquelchModeArray;
    
    private JLabel jlblAFC;
    private JLabel jlblAGC;
    private JLabel jlblAttenuator;
    private JLabel jlblFrequency;
    private JLabel jlblMode;
    private JLabel jlblRSSI;
    private JLabel jlblSINAD;
    private JLabel jlblBER;
    private JLabel jlblNoiseBlanker;
    private JLabel jlblSelect;
    private JLabel jlblSquelchCode;
    private JLabel jlblSquelchMode;
    private JLabel deviceComboBoxLabel;
    private JLabel calFileComboBoxLabel;
    private JLabel jlblVolume;
    private JLabel jlblSquelch;

    private JCheckBox startRadioWithSystemCheckBox;

    private JSlider jSliderVolume;
    private JSlider jSliderSquelch;

    private JFormattedTextField jftfFreq;
    private JFormattedTextField jftfAttn;

    private JCheckBox jcbAFC;
    private JCheckBox jcbAGC;
    private JCheckBox jcbDPLInv;
    private JCheckBox jcbNB;
    private JCheckBox jcbSampleRSSI;
    private JCheckBox jcbSampleSINAD;
    private JCheckBox jcbSampleBER;

    private JLabel jLabelAttenuator;
    private JLabel jLabelColorCode;
    private JLabel jLabelDPL;
    private JLabel jLabelDPLInv;
    private JLabel jLabelFilter;
    private JLabel jLabelFrequency;
    private JLabel jLabelMode;
    private JLabel jLabelSquelchMode;
    private JLabel jLabelNAC;
    private JLabel jLabelPL;

    private JLabel[] jlblScanLabelArray;

    private JCheckBox[] jcbAFCArray;
    private JCheckBox[] jcbAGCArray;
    private JCheckBox[] jcbNBArray;
    private JCheckBox[] jcbSampleRSSIArray;
    private JCheckBox[] jcbSampleSINADArray;
    private JCheckBox[] jcbSampleBERArray;
    private JCheckBox[] jcbSelectArray;

    private JPanel vfoScanPanel;
    
    private ButtonGroup vfoScanButtonGroup;

    private JRadioButton vfoModeButton;
    private JRadioButton scanModeButton;

    private JFormattedTextField[] jftfAttnArray;
    private JFormattedTextField[] jftfFreqArray;

    private NumberFormatter freqFormatter;
    private NumberFormatter attnFormatter;

    private transient MouseMotionAdapter volumeSliderMouseMotionAdapter;
    private transient MouseMotionAdapter squelchSliderMouseMotionAdapter;

    private boolean blockEvents;
    
    private boolean errorDialogQueued;

    private transient AbstractRadioReceiver abstractRadioReceiver;
    
    public RadioComponent(AbstractRadioReceiver abstractRadioReceiver) {
        this.abstractRadioReceiver = abstractRadioReceiver;

        initializeSwingComponents();
        configureSwingComponents();

        initializeListeners();
        
        initializeComponentListeners();
        addComponentListeners();

        drawGraphicalUserInterface();
    }

    private void saveValues() {
        abstractRadioReceiver.setRssiEnabled(jcbSampleRSSI.isSelected());
        abstractRadioReceiver.setBerEnabled(jcbSampleBER.isSelected());
        abstractRadioReceiver.setSinadEnabled(jcbSampleSINAD.isSelected());
        abstractRadioReceiver.setAFC(jcbAFC.isSelected());
        abstractRadioReceiver.setAGC(jcbAGC.isSelected());
        abstractRadioReceiver.setNoiseBlanker(jcbNB.isSelected());
        abstractRadioReceiver.setPL((int) (Double.parseDouble(((String) jcboPL.getSelectedItem()).trim()) * 10));
        abstractRadioReceiver.setDPL(Integer.parseInt(((String) jcboDPL.getSelectedItem()).trim()));
        abstractRadioReceiver.setNetworkAccessCode(((String) jcboNAC.getSelectedItem()).trim());
        abstractRadioReceiver.setDPLInverted(jcbDPLInv.isSelected());
        abstractRadioReceiver.setDigitalColorCode(Integer.parseInt(((String) jcboColorCode.getSelectedItem()).trim().split("/")[0]));
        abstractRadioReceiver.setTimeSlot(Integer.parseInt(((String)jcboColorCode.getSelectedItem()).trim().split("/")[1]));
        abstractRadioReceiver.setFilterHz((int) jcboFilter.getSelectedItem());
        abstractRadioReceiver.setModeName(StandardModeName.valueOf((String) jcboMode.getSelectedItem()));

        if (!jftfAttn.getText().isBlank()) {
            abstractRadioReceiver.setAttenuator(Double.parseDouble(jftfAttn.getText()));
        }

        if (!jftfFreq.getText().isBlank()) {
            abstractRadioReceiver.setFrequency(Double.parseDouble(jftfFreq.getText()));
        }

        abstractRadioReceiver.setSquelchMode(AccessMode.valueOf(((String) jcboSquelchMode.getSelectedItem()).trim()));
        
        for (int i = 0; i < abstractRadioReceiver.getScanList().size(); i++) {
        	if (!jftfFreqArray[i].getText().isBlank()) {
        		abstractRadioReceiver.getScanElement(i).setFrequency(Double.parseDouble(jftfFreqArray[i].getText()));
        	}
        	if (!jftfAttnArray[i].getText().isBlank()) {
        		abstractRadioReceiver.getScanElement(i).setAttenuator(Double.parseDouble(jftfAttnArray[i].getText()));
        	}
        }
    }

    private void initializeListeners() {
    	final PropertyChangeListener cdoListener = event -> {
			if (CalibrationDataObject.Event.SEMAPHORE_ACQUIRE.name().equals(event.getPropertyName())) {
				setCursor(new Cursor(Cursor.WAIT_CURSOR));
			}
			if (CalibrationDataObject.Event.SEMAPHORE_RELEASE.name().equals(event.getPropertyName())) {
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
    	};
    	CalibrationDataObject.getPropertyChangeSupport().addPropertyChangeListener(cdoListener);
    }
    
    private void setDeviceComboBox(File calFile) {
        final String model = CalibrationDataObject.getDataString(calFile, DataString.MODEL).toUpperCase(Locale.US);
        final String manufacturer = CalibrationDataObject.getDataString(calFile, DataString.MANUFACTURER).toUpperCase(Locale.US);
        deviceComboBox.setSelectedItem(manufacturer + " " + model);
    }

    private JTabbedPane getTabbedPane() {
        final JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab(" System Settings ", null, getSystemPanel(), null);
        tabbedPane.addTab(" VFO Settings ", null, getRadioPanel(), null);
        tabbedPane.addTab(" Scan Channels ", null, getScanElementPanel(), null);
        
        for (JPanel panel : abstractRadioReceiver.getConfigurationComponentArray()) {
        	tabbedPane.addTab(panel.getName(), panel);
        }
  
        return tabbedPane;
    }

    public void stopRadio() {
        abstractRadioReceiver.stopRadio();
    }

    private void addComponentListeners() {
        final RadioButtonHandler radioButtonHandler = new RadioButtonHandler();

        vfoModeButton.addItemListener(radioButtonHandler);
        scanModeButton.addItemListener(radioButtonHandler);

        jSliderVolume.addMouseMotionListener(volumeSliderMouseMotionAdapter);
        jSliderSquelch.addMouseMotionListener(squelchSliderMouseMotionAdapter);

        okButton.addActionListener(_ -> okButtonActionListenerEvent());
        cancelButton.addActionListener(_ -> cancelButtonActionListenerEvent());
        applyButton.addActionListener(_ -> applyButtonActionListenerEvent());
        newFileButton.addActionListener(_ -> newCalFileButtonActionListenerEvent());
        openCalFileEditorButton.addActionListener(_ -> openCalFileEditorActionListenerEvent());
        
        deviceComboBox.addItemListener(this::deviceComboBoxActionPerformed);
        startRadioWithSystemCheckBox.addActionListener(this::startRadioWithSystemActionPerformed);
        calFileComboBox.addItemListener(this::calFileComboBoxActionPerformed);
        
        jftfFreq.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                jftfFreq.setFont(new Font("Calabri", Font.BOLD, 12));
            }

            @Override
            public void focusLost(FocusEvent e) {
            	validateFrequency(jftfFreq);
                jftfFreq.setFont(new Font("Calabri", Font.PLAIN, 11));
            }
        });

        jftfFreq.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent event) {
            	// NOOP
            }

            @Override
            public void keyPressed(KeyEvent event) {
            	
                if ((event.getKeyCode() == KeyEvent.VK_ENTER)) {
                	validateFrequency(jftfFreq);
                    jftfFreq.setFont(new Font("Calabri", Font.PLAIN, 11));
                }
                if (event.getKeyCode() == KeyEvent.VK_TAB) {
                	validateFrequency(jftfFreq);
                    jftfFreq.setFont(new Font("Calabri", Font.PLAIN, 11));
                }
            }

            @Override
            public void keyReleased(KeyEvent event) {
            	// NOOP
            }
        });

        jftfAttn.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {            	
                jftfAttn.setFont(new Font("Calabri", Font.BOLD, 12));
            }

            @Override
            public void focusLost(FocusEvent e) {
            	validateAttenuator(jftfAttn);
                jftfAttn.setFont(new Font("Calabri", Font.PLAIN, 11));
            }
        });

        jftfAttn.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent event) {
            	// NOOP
            }

            @Override
            public void keyPressed(KeyEvent event) {
                if ((event.getKeyCode() == KeyEvent.VK_ENTER) || (event.getKeyCode() == KeyEvent.VK_TAB)) {
                    jftfAttn.setFont(new Font("Calabri", Font.PLAIN, 11));
                    validateAttenuator(jftfAttn);
                }
            }

            @Override
            public void keyReleased(KeyEvent event) {
            	// NOOP
            }
        });

        jcboMode.addItemListener(_ -> updateModeComboBox());

        jcboSquelchMode.addItemListener(_ -> {
            if (jcboSquelchMode.getSelectedItem() == null) {
                return;
            }
            updateSquelchCodeComboBoxes();
        });

        for (int ix = 0; ix < abstractRadioReceiver.getScanListSize(); ix++) {
            final int i = ix;

            jcbSelectArray[i].addActionListener(e -> {
                final JCheckBox cb = (JCheckBox) e.getSource();
                abstractRadioReceiver.getScanElement(i).setScanSelected(cb.isSelected());
            });

            jftfFreqArray[i].addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    jftfFreqArray[i].setFont(new Font("Calabri", Font.BOLD, 12));
                }

                @Override
                public void focusLost(FocusEvent e) {
                    jftfFreqArray[i].setFont(new Font("Calabri", Font.PLAIN, 11));
                    validateFrequency(jftfFreqArray[i]);
                }
            });

            jftfFreqArray[i].addKeyListener(new KeyListener() {
                @Override
                public void keyTyped(KeyEvent event) {
                	// NOOP
                }

                @Override
                public void keyPressed(KeyEvent event) {
                    if ((event.getKeyCode() == KeyEvent.VK_ENTER) || (event.getKeyCode() == KeyEvent.VK_TAB)) {
                        jftfFreqArray[i].setFont(new Font("Calabri", Font.PLAIN, 11));
                        validateFrequency(jftfFreqArray[i]);
                    }
                }

                @Override
                public void keyReleased(KeyEvent event) {
                	// NOOP
                }
            });

            jftfAttnArray[i].addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    jftfAttnArray[i].setFont(new Font("Calabri", Font.BOLD, 12));
                }

                @Override
                public void focusLost(FocusEvent e) {
                    jftfAttnArray[i].setFont(new Font("Calabri", Font.PLAIN, 11));
                    validateAttenuator(jftfAttnArray[i]);
                }
            });

            jftfAttnArray[i].addKeyListener(new KeyListener() {
                @Override
                public void keyTyped(KeyEvent event) {
                	// NOOP
                }

                @Override
                public void keyPressed(KeyEvent event) {
                    if ((event.getKeyCode() == KeyEvent.VK_ENTER) || (event.getKeyCode() == KeyEvent.VK_TAB)) {
                        jftfAttnArray[i].setFont(new Font("Calabri", Font.PLAIN, 11));
                        validateAttenuator(jftfAttnArray[i]);
                    }
                }

                @Override
                public void keyReleased(KeyEvent event) {
                	// NOOP
                }
            });

            jcboModeArray[i].addItemListener(_ -> {
                if (!blockEvents) {
                    abstractRadioReceiver.getScanElement(i).setModeName(StandardModeName.valueOf((String) jcboModeArray[i].getSelectedItem()));
                    updateSquelchModeComboBox(i);
                    updateSquelchCodeComboBox(i);
                }
            });

            jcboSquelchModeArray[i].addItemListener(_ -> {
                if (!blockEvents) {
                    abstractRadioReceiver.getScanElement(i).setSquelchMode(AccessMode.valueOf((String) jcboSquelchModeArray[i].getSelectedItem()));
                    updateSquelchCodeComboBox(i);
                }
            });

            jcboSquelchCodeArray[i].addItemListener(_ -> {
                if (!blockEvents) {
                	if (jcboSquelchModeArray[i].getSelectedIndex() == AccessMode.CSQ.ordinal()) {
                		abstractRadioReceiver.getScanElement(i).setPLTone("");
                	}
                    if (jcboSquelchModeArray[i].getSelectedIndex() == AccessMode.PL.ordinal()) {
                        abstractRadioReceiver.getScanElement(i).setPLTone((String) jcboSquelchCodeArray[i].getSelectedItem());
                    }
                    if (jcboSquelchModeArray[i].getSelectedIndex() == AccessMode.DPL.ordinal()) {
                        abstractRadioReceiver.getScanElement(i).setDPLCode((String) jcboSquelchCodeArray[i].getSelectedItem());
                    }
                    if (jcboSquelchModeArray[i].getSelectedIndex() == AccessMode.NAC.ordinal()) {
                        abstractRadioReceiver.getScanElement(i).setNetworkAccessCode((String) jcboSquelchCodeArray[i].getSelectedItem());
                    }
                    if (jcboSquelchModeArray[i].getSelectedIndex() == AccessMode.CC.ordinal()) {
                        abstractRadioReceiver.getScanElement(i).setColorCode((byte) jcboColorCodeArray[i].getSelectedItem());
                    }
                }
            });

            jcbSampleBERArray[i].addActionListener(e -> {
            	final JCheckBox cb = (JCheckBox) e.getSource();
                abstractRadioReceiver.getScanElement(i).setSampleBER(cb.isSelected());
            });

            jcbSampleRSSIArray[i].addActionListener(e -> {
            	final JCheckBox cb = (JCheckBox) e.getSource();
                abstractRadioReceiver.getScanElement(i).setSampleRSSI(cb.isSelected());
            });

            jcbSampleSINADArray[i].addActionListener(e -> {
            	final JCheckBox cb = (JCheckBox) e.getSource();
                abstractRadioReceiver.getScanElement(i).setSampleSINAD(cb.isSelected());
            });

            jcbAFCArray[i].addActionListener(e -> {
            	final JCheckBox cb = (JCheckBox) e.getSource();
                abstractRadioReceiver.getScanElement(i).setAFC(cb.isSelected());
            });

            jcbAGCArray[i].addActionListener(e -> {
            	final JCheckBox cb = (JCheckBox) e.getSource();
                abstractRadioReceiver.getScanElement(i).setAGC(cb.isSelected());
            });

            jcbNBArray[i].addActionListener(e -> {
            	final JCheckBox cb = (JCheckBox) e.getSource();
                abstractRadioReceiver.getScanElement(i).setNoiseBlanker(cb.isSelected());
            });
        }
    }

    private void initializeComponentListeners() {
        volumeSliderMouseMotionAdapter = new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent event) {
                volumeSliderMouseDragged(event);
            }
        };

        squelchSliderMouseMotionAdapter = new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent event) {
                squelchSliderMouseDragged(event);
            }
        };
    }

    private void updateModeComboBox() {
        switch (((String) jcboMode.getSelectedItem()).trim()) {
	        case "LSB" -> {
	            jcboSquelchMode.removeAllItems();
	            jcboSquelchMode.addItem("CSQ");
	            jcbNB.setEnabled(false);
	            jcbAGC.setEnabled(false);
	            jcbAFC.setEnabled(false);
	            jcbSampleRSSI.setEnabled(true);
	            jcbSampleBER.setEnabled(false);
	            jcbSampleBER.setSelected(false);
	            jcbSampleSINAD.setEnabled(false);
	            jcboSquelchMode.setSelectedIndex(0);
	        }
	        case "USB" -> {
	            jcboSquelchMode.removeAllItems();
	            jcboSquelchMode.addItem("CSQ");
	            jcbNB.setEnabled(false);
	            jcbAGC.setEnabled(false);
	            jcbAFC.setEnabled(false);
	            jcbSampleRSSI.setEnabled(true);
	            jcbSampleBER.setEnabled(false);
	            jcbSampleBER.setSelected(false);
	            jcbSampleSINAD.setEnabled(false);
	            jcboSquelchMode.setSelectedIndex(0);
	        }
	        case "AM" -> {
	            jcboSquelchMode.removeAllItems();
	            jcboSquelchMode.addItem("CSQ");
	            jcbNB.setEnabled(false);
	            jcbAGC.setEnabled(false);
	            jcbAFC.setEnabled(false);
	            jcbSampleRSSI.setEnabled(true);
	            jcbSampleBER.setEnabled(false);
	            jcbSampleBER.setSelected(false);
	            jcbSampleSINAD.setEnabled(false);
	            jcboSquelchMode.setSelectedIndex(0);
	        }
	        case "CW" -> {
	            jcboSquelchMode.removeAllItems();
	            jcboSquelchMode.addItem("CSQ");
	            jcbNB.setEnabled(false);
	            jcbAGC.setEnabled(false);
	            jcbAFC.setEnabled(false);
	            jcbSampleRSSI.setEnabled(true);
	            jcbSampleBER.setEnabled(false);
	            jcbSampleBER.setSelected(false);
	            jcbSampleSINAD.setEnabled(false);
	            jcboSquelchMode.setSelectedIndex(0);
	        }
            case "FM" -> {
                jcboSquelchMode.removeAllItems();
                jcboSquelchMode.addItem("CSQ");
                jcboSquelchMode.addItem("PL");
                jcboSquelchMode.addItem("DPL");
                jcbNB.setEnabled(false);
                jcbAGC.setEnabled(false);
                jcbAFC.setEnabled(false);
                jcbSampleRSSI.setEnabled(true);
                jcbSampleBER.setEnabled(false);
                jcbSampleBER.setSelected(false);
                jcbSampleSINAD.setEnabled(true);
                jcboSquelchMode.setSelectedIndex(0);
            }
            case "NFM" -> {
                jcboSquelchMode.removeAllItems();
                jcboSquelchMode.addItem("CSQ");
                jcboSquelchMode.addItem("PL");
                jcboSquelchMode.addItem("DPL");
                jcbNB.setEnabled(false);
                jcbAGC.setEnabled(false);
                jcbAFC.setEnabled(false);
                jcbSampleRSSI.setEnabled(true);
                jcbSampleBER.setEnabled(false);
                jcbSampleBER.setSelected(false);
                jcbSampleSINAD.setEnabled(true);
                jcboSquelchMode.setSelectedIndex(0);
            }
            case "WFM" -> {
                jcboSquelchMode.removeAllItems();
                jcboSquelchMode.addItem("CSQ");
                jcbNB.setEnabled(false);
                jcbAGC.setEnabled(false);
                jcbAFC.setEnabled(false);
                jcbSampleRSSI.setEnabled(true);
                jcbSampleBER.setEnabled(false);
                jcbSampleBER.setSelected(false);
                jcbSampleSINAD.setEnabled(false);
                jcboSquelchMode.setSelectedIndex(0);
            }
            case "P25_PHASE_1" -> {
                jcboSquelchMode.removeAllItems();
                jcboSquelchMode.addItem("NAC");
                jcbNB.setEnabled(false);
                jcbAGC.setEnabled(false);
                jcbAFC.setEnabled(false);
                jcbSampleRSSI.setEnabled(true);
                jcbSampleBER.setEnabled(true);
                jcbSampleSINAD.setEnabled(false);
                jcbSampleSINAD.setSelected(false);
                jcboSquelchMode.setSelectedIndex(0);
            }
            case "DMR" -> {
                jcboSquelchMode.removeAllItems();
                jcboSquelchMode.addItem("CC");
                jcbNB.setEnabled(false);
                jcbAGC.setEnabled(false);
                jcbAFC.setEnabled(false);
                jcbSampleRSSI.setEnabled(true);
                jcbSampleBER.setEnabled(true);
                jcbSampleSINAD.setEnabled(false);
                jcbSampleSINAD.setSelected(false);
                jcboSquelchMode.setSelectedIndex(0);
            }
            case "DSTAR" -> {
                jcboSquelchMode.removeAllItems();
                jcboSquelchMode.addItem("");
                jcboSquelchMode.setSelectedIndex(-1);
                jcbNB.setEnabled(false);
                jcbAGC.setEnabled(false);
                jcbAFC.setEnabled(false);
                jcbSampleRSSI.setEnabled(true);
                jcbSampleBER.setEnabled(true);
                jcbSampleSINAD.setEnabled(false);
                jcbSampleSINAD.setSelected(false);
            }
            default -> {
                jcboSquelchMode.removeAllItems();
                jcbNB.setEnabled(false);
                jcbAGC.setEnabled(false);
                jcbAFC.setEnabled(false);
                jcbSampleRSSI.setEnabled(false);
                jcbSampleBER.setEnabled(false);
                jcbSampleBER.setSelected(false);
                jcbSampleSINAD.setEnabled(false);
                jcbSampleSINAD.setSelected(false);
            }
        }
    }
   
    private void updateSquelchCodeComboBoxes() {
        if (jcboSquelchMode.getSelectedItem() == null || ((String) jcboSquelchMode.getSelectedItem()).isBlank()) {
        	jcboPL.setEnabled(false);
            jcboDPL.setEnabled(false);
            jcboNAC.setEnabled(false);
            jcboColorCode.setEnabled(false);
            jcbDPLInv.setEnabled(false);
            jftfAttn.setEnabled(false);
            jcboFilter.setEnabled(false);
            return;
        }
        switch (((String) jcboSquelchMode.getSelectedItem()).trim()) {
            case "CSQ" -> {
                jcboPL.setEnabled(false);
                jcboDPL.setEnabled(false);
                jcboNAC.setEnabled(false);
                jcboColorCode.setEnabled(false);
                jcbDPLInv.setEnabled(false);
                jftfAttn.setEnabled(true);
                jcboFilter.setEnabled(true);
            }
            case "PL" -> {
                jcboPL.setEnabled(true);
                jcboDPL.setEnabled(false);
                jcboNAC.setEnabled(false);
                jcboColorCode.setEnabled(false);
                jcbDPLInv.setEnabled(false);
                jftfAttn.setEnabled(true);
                jcboFilter.setEnabled(true);
            }
            case "DPL" -> {
                jcboPL.setEnabled(false);
                jcboDPL.setEnabled(true);
                jcboNAC.setEnabled(false);
                jcboColorCode.setEnabled(false);
                jcbDPLInv.setEnabled(true);
                jftfAttn.setEnabled(true);
                jcboFilter.setEnabled(true);
            }
            case "NAC" -> {
                jcboPL.setEnabled(false);
                jcboDPL.setEnabled(false);
                jcboNAC.setEnabled(true);
                jcboColorCode.setEnabled(false);
                jcbDPLInv.setEnabled(false);
                jftfAttn.setEnabled(true);
                jcboFilter.setEnabled(true);
            }
            case "CC" -> {
                jcboPL.setEnabled(false);
                jcboDPL.setEnabled(false);
                jcboNAC.setEnabled(false);
                jcboColorCode.setEnabled(true);
                jcbDPLInv.setEnabled(false);
                jftfAttn.setEnabled(true);
                jcboFilter.setEnabled(true);
            }
            default -> {
                jcboPL.setEnabled(false);
                jcboDPL.setEnabled(false);
                jcboNAC.setEnabled(false);
                jcboColorCode.setEnabled(false);
                jcbDPLInv.setEnabled(false);
                jftfAttn.setEnabled(false);
                jcboFilter.setEnabled(false);
            }
        }
    }

    private void updateSquelchModeComboBox(int i) {
        blockEvents = true;
        jcboSquelchModeArray[i].removeAllItems();
        jcboSquelchModeModelArray[i].addAll(AbstractRadioReceiver.getAccessModeStringList(abstractRadioReceiver.getScanElement(i).getModeName()));
        jcboSquelchModeArray[i].revalidate();
        jcboSquelchModeArray[i].setSelectedIndex(abstractRadioReceiver.getScanElement(i).getSquelchMode().ordinal());
        blockEvents = false;
    }
    
    private static boolean isZero(double value){
		return isZero(value, 0.000000001);
	}
	
	private static boolean isZero(double value, double threshold){
	    return value >= -threshold && value <= threshold;
	}

    private void updateSquelchCodeComboBox(int i) {
        blockEvents = true;
        jcboSquelchCodeArray[i].removeAllItems();
        if (jcboSquelchModeArray[i].getSelectedIndex() == (AccessMode.CSQ.ordinal())) {
            jcboSquelchCodeArray[i].setEditable(false);
            jcboSquelchCodeArray[i].setEnabled(false);
        } else if (jcboSquelchModeArray[i].getSelectedIndex() == (AccessMode.PL.ordinal())) {
            jcboSquelchCodeArray[i].setEditable(false);
            jcboSquelchCodeModelArray[i].addAll(Arrays.asList(abstractRadioReceiver.getToneSquelchValues()));
            jcboSquelchCodeArray[i].setEnabled(true);
            jcboSquelchCodeArray[i].setSelectedItem(abstractRadioReceiver.getScanElement(i).getPLTone());
        } else if (jcboSquelchModeArray[i].getSelectedIndex() == (AccessMode.DPL.ordinal())) {
            jcboSquelchCodeArray[i].setEditable(false);
            jcboSquelchCodeModelArray[i].addAll(Arrays.asList(abstractRadioReceiver.getDigitalSquelchValues()));
            jcboSquelchCodeArray[i].setEnabled(true);
            jcboSquelchCodeArray[i].setSelectedItem(abstractRadioReceiver.getScanElement(i).getDPLCode());
        } else if (jcboSquelchModeArray[i].getSelectedIndex() == (AccessMode.NAC.ordinal())) {
            jcboSquelchCodeArray[i].setEnabled(true);
            jcboSquelchCodeModelArray[i].addAll(Arrays.asList(abstractRadioReceiver.getDigitalNACValues()));
            jcboSquelchCodeArray[i].setEditable(true);
            jcboSquelchCodeArray[i].setSelectedItem(abstractRadioReceiver.getScanElement(i).getNetworkAccessCode());
        } else if (jcboSquelchModeArray[i].getSelectedIndex() == (AccessMode.CC.ordinal())) {
            jcboSquelchCodeArray[i].setEnabled(true);
            jcboSquelchCodeModelArray[i].addAll(Arrays.asList(abstractRadioReceiver.getDigitalColorCodeValues()));
            jcboSquelchCodeArray[i].setEditable(false);
            jcboSquelchCodeArray[i].setSelectedItem(abstractRadioReceiver.getScanElement(i).getColorCode());
        }
        jcboSquelchCodeArray[i].revalidate();
        blockEvents = false;
    }
    
    private void startRadioWithSystemActionPerformed(ActionEvent event) {
        final JCheckBox cb = (JCheckBox) event.getSource();
        abstractRadioReceiver.setStartRadioWithSystem(cb.isSelected());
    }

    private void validateFrequency(JFormattedTextField jftfFreq) {
    	
    	if (!jftfFreq.getText().isBlank() && !errorDialogQueued &&
    		(Double.parseDouble(jftfFreq.getText()) < abstractRadioReceiver.getMinRxFreq() || 
    		Double.parseDouble(jftfFreq.getText()) > abstractRadioReceiver.getMaxRxFreq())) {
    		
    		errorDialogQueued = true;
    		
    		JOptionPane.showMessageDialog(new JDialog(), "Frequency Must be >= " + abstractRadioReceiver.getMinRxFreq() + 
    			" MHz and <= " + abstractRadioReceiver.getMaxRxFreq() + " MHz",
    			"Frequency Out of Range for this Model", JOptionPane.ERROR_MESSAGE);
            
    		jftfFreq.setText(String.valueOf(abstractRadioReceiver.getMaxRxFreq()));
    		
    		errorDialogQueued = false;
    		
    		clickComponent(jftfFreq);
    	}

    }
    
    private void validateAttenuator(JFormattedTextField jftfAttn) {
		
    	if (!jftfAttn.getText().isBlank() && !errorDialogQueued &&
    		(Double.parseDouble(jftfAttn.getText()) < abstractRadioReceiver.getMinAttenuator() || 
    		Double.parseDouble(jftfAttn.getText()) > abstractRadioReceiver.getMaxAttenuator())) {
    		
    		errorDialogQueued = true;
    		
    		JOptionPane.showMessageDialog(new JDialog(), "Attenuator Must be >= " + abstractRadioReceiver.getMinAttenuator() + 
    			" dB and <= " + abstractRadioReceiver.getMaxAttenuator() + " dB",
    			"Attenuator Out of Range for this Model", JOptionPane.ERROR_MESSAGE);
            
    		jftfAttn.setText(String.valueOf(abstractRadioReceiver.getMinAttenuator()));
    		
    		errorDialogQueued = false;
    		
    		clickComponent(jftfAttn);
    	} 
    }
    
    private void clickComponent(JComponent comp){
    	try {
    		final Robot robot = new Robot();
    		final Point loc = comp.getLocationOnScreen();
    		final Dimension size = comp.getSize();
            robot.mouseMove(loc.x + size.width / 2, loc.y + size.height / 2);
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
    	} catch (AWTException e) {
			LOG.log(Level.WARNING, "Component {0} must be showing on the screen to be automated.", comp.getName());
		} catch (IllegalComponentStateException ex) {
			LOG.log(Level.WARNING, "Component {0} must be showing on the screen to determine its location.", comp.getName());
		}
    }
     
    private List<String> getCalFileListForManufacturerModel(String manufacturer, String model, File calFileParentFile) {
    	final List<String> list = new ArrayList<>();
        if (calFileParentFile != null) {
	        for (final File calFile : calFileParentFile.listFiles()) {
	            final String mod = CalibrationDataObject.getDataString(calFile, DataString.MODEL).toUpperCase(Locale.US);
	            final String mfr = CalibrationDataObject.getDataString(calFile, DataString.MANUFACTURER).toUpperCase(Locale.US);
	            if ((mod != null) && (mfr != null) && model.toUpperCase(Locale.US).contains(mod) && manufacturer.toUpperCase(Locale.US).contains(mfr)) {
	            	list.add(calFile.getPath());
	            }
	        }
        }
        if (list.isEmpty()) {
            calFileNotFoundMessage("There are no calibration files available for: " + manufacturer + " model " + model);
        }
        return list;
    }

    private void calFileNotFoundMessage(String message) {
        invokeLaterInDispatchThreadIfNeeded(() -> JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(RadioComponent.this),
            message, "Calibration File Not Found", JOptionPane.ERROR_MESSAGE));
    }

    private void deviceComboBoxActionPerformed(ItemEvent event) {
        final JComboBox<?> cb = (JComboBox<?>) event.getSource();
        final SwingWorker<List<String>, Void> worker = new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() throws Exception {
                final File[] calFiles = AbstractRadioReceiver.getCalParentFile().listFiles();
                final List<String> calFilesForModel = Collections.synchronizedList(new CopyOnWriteArrayList<>());
                if (calFiles != null) {
                    for (final File calFile : calFiles) {
                        final String calFileNameString = CalibrationDataObject.getDataString(calFile, DataString.MODEL);
                        if (((String) cb.getSelectedItem()).contains(calFileNameString)) {
                            calFilesForModel.add(calFile.getPath());
                        }
                    }
                }
                return calFilesForModel;
            }

            @Override
            protected void done() {
                try {
                    calFileComboBox.removeAllItems();
                    final List<String> list = get();
                    list.forEach(calFileComboBoxModel::addElement);
                    calFileComboBox.validate();
                    calFileComboBox.setSelectedIndex(0);
                    abstractRadioReceiver = AbstractRadioReceiver.getRadioInstance(new File(((String) calFileComboBox.getSelectedItem()).trim()), false);
                } catch (final InterruptedException e) {
                    LOG.log(Level.SEVERE, "InterruptedException", e);
                    Thread.currentThread().interrupt();
                } catch (final ExecutionException e) {
                	LOG.log(Level.SEVERE, "ExecutionException", e);
                } finally {
                	setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            }
        };

        worker.execute();
    }

    private void calFileComboBoxActionPerformed(ItemEvent event) {
        if (event.getStateChange() == ItemEvent.SELECTED) {
            final JComboBox<?> cb = (JComboBox<?>) event.getSource();
            abstractRadioReceiver = AbstractRadioReceiver.getRadioInstance(new File(((String) cb.getSelectedItem()).trim()), false);
        }
    }

    private void openCalFileEditorActionListenerEvent() {
        abstractRadioReceiver.getCalibrationDataObject().openCalibrationComponent();
    }
    
    private void newCalFileButtonActionListenerEvent() {
    	final String[] deviceStr = ((String) deviceComboBox.getSelectedItem()).split(" ");
    	
    	final AbstractRadioReceiver radio = AbstractRadioReceiver.getRadioInstanceFor(deviceStr[0], deviceStr[1]);
    	
    	final String serialNumber = (String) JOptionPane.showInputDialog(this, "Enter Serial Number of New Radio",
    		"Create New Calibration File for " + deviceComboBox.getSelectedItem(), 
    		JOptionPane.QUESTION_MESSAGE, null, null, radio.getDefaultSerialNumber());

        AbstractRadioReceiver.createNewCalFileRecord(deviceStr[0], deviceStr[1], serialNumber);
    	
    	calFileComboBox.removeAllItems();
    	calFileComboBoxModel.addAll(getCalFileListForManufacturerModel(deviceStr[0], deviceStr[1], AbstractRadioReceiver.getCalParentFile()));
    	calFileComboBox.setSelectedItem(abstractRadioReceiver.getCalFile().getPath());
        
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }
    
    private void applyButtonActionListenerEvent() {
        applied = true;
        saveValues();
        abstractRadioReceiver.savePreferences();
    }

    private void cancelButtonActionListenerEvent() {
        dispose();
    }

    private void okButtonActionListenerEvent() {
        if (!applied) {
            applyButtonActionListenerEvent();
        }
        dispose();
    }

    private void volumeSliderMouseDragged(MouseEvent event) {
        final JSlider js = (JSlider) event.getSource();
        abstractRadioReceiver.setVolume(js.getValue());
    }

    private void squelchSliderMouseDragged(MouseEvent event) {
        final JSlider js = (JSlider) event.getSource();
        abstractRadioReceiver.setSquelch(js.getValue());
    }

	@SuppressWarnings("unchecked")
	private void initializeSwingComponents() {
		setModal(true);
		setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
		
		final DefaultComboBoxModel<String>[] jcboModeModelArray;
		final DefaultComboBoxModel<String> deviceComboBoxModel;
        
		final NumberFormat frequencyFormat = NumberFormat.getNumberInstance();
        frequencyFormat.setMaximumIntegerDigits(4);
        frequencyFormat.setMinimumIntegerDigits(1);
        frequencyFormat.setMaximumFractionDigits(5);
        frequencyFormat.setMinimumFractionDigits(1);
        frequencyFormat.setGroupingUsed(false);

        freqFormatter = new NumberFormatter(frequencyFormat);
        freqFormatter.setCommitsOnValidEdit(false);
        freqFormatter.setValueClass(Double.class);
        freqFormatter.setAllowsInvalid(true);
        freqFormatter.setOverwriteMode(true);
        
        final NumberFormat attnFormat = NumberFormat.getNumberInstance();
        attnFormat.setMaximumIntegerDigits(3);
        attnFormat.setMinimumIntegerDigits(1);
        attnFormat.setMaximumFractionDigits(2);
        attnFormat.setMinimumFractionDigits(1);
        attnFormat.setGroupingUsed(false);

        attnFormatter = new NumberFormatter(attnFormat);
        attnFormatter.setCommitsOnValidEdit(false);
        attnFormatter.setValueClass(Double.class);
        attnFormatter.setAllowsInvalid(true);
        attnFormatter.setOverwriteMode(true);

        jSliderVolume = new JSlider(0, 255, 0);
        jSliderVolume.setBorder(BorderFactory.createTitledBorder("Volume"));
        jSliderVolume.setPaintTicks(true);
        jSliderVolume.setPaintTrack(true);
        jSliderVolume.setMinorTickSpacing(16);
        jSliderVolume.setMajorTickSpacing(64);
        
        jSliderSquelch = new JSlider(0, 255, 0);
        jSliderSquelch.setBorder(BorderFactory.createTitledBorder("Squelch"));
        jSliderSquelch.setPaintTicks(true);
        jSliderSquelch.setPaintTrack(true);
        jSliderSquelch.setMinorTickSpacing(16);
        jSliderSquelch.setMajorTickSpacing(64);

        jftfFreq = new JFormattedTextField(freqFormatter);
        jftfFreq.setHorizontalAlignment(SwingConstants.RIGHT);
        jftfFreq.setFont(new Font("Calabri", Font.PLAIN, 11));
        jftfFreq.setBackground(Color.WHITE);
        jftfFreq.setForeground(Color.BLACK);
        jftfFreq.setFocusLostBehavior(JFormattedTextField.COMMIT);
        jftfFreq.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Collections.emptySet());
        
        jftfAttn = new JFormattedTextField(attnFormatter);
        jftfAttn.setHorizontalAlignment(SwingConstants.RIGHT);
        jftfAttn.setFont(new Font("Calabri", Font.PLAIN, 11));
        jftfAttn.setBackground(Color.WHITE);
        jftfAttn.setForeground(Color.BLACK);
        jftfAttn.setFocusLostBehavior(JFormattedTextField.COMMIT);
        jftfAttn.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Collections.emptySet());
        
        vfoScanPanel = new JPanel();
        vfoScanPanel.setBorder(BorderFactory.createTitledBorder("Channel Select"));

        vfoScanButtonGroup = new ButtonGroup();
        vfoModeButton = new JRadioButton("VFO Mode");
        scanModeButton = new JRadioButton("Scan Mode");
        vfoScanButtonGroup.add(vfoModeButton);
        vfoScanButtonGroup.add(scanModeButton);

        jcbAFC = new JCheckBox("Automatic Frequency Control");
        jcbAGC = new JCheckBox("Automatic Gain Control");
        jcbNB = new JCheckBox("Noise Blanker");
        jcbSampleRSSI = new JCheckBox("Sample Signal Strength");
        jcbSampleSINAD = new JCheckBox("Sample SINAD");
        jcbSampleBER = new JCheckBox("Sample Bit Error Rate");

        jcbDPLInv = new JCheckBox();
        jcbDPLInv.setEnabled(false);

        final DefaultComboBoxModel<String> jComboBoxColorCodeModel = new DefaultComboBoxModel<>(abstractRadioReceiver.getDigitalColorCodeValues());
        jcboColorCode = new JComboBox<>(jComboBoxColorCodeModel);
        jcboColorCode.setEnabled(false);

        final DefaultComboBoxModel<String> jComboBoxDPLModel = new DefaultComboBoxModel<>(abstractRadioReceiver.getDigitalSquelchValues());
        jcboDPL = new JComboBox<>(jComboBoxDPLModel);
        jcboDPL.setEnabled(false);

        final DefaultComboBoxModel<String> jComboBoxPLModel = new DefaultComboBoxModel<>(abstractRadioReceiver.getToneSquelchValues());
        jcboPL = new JComboBox<>(jComboBoxPLModel);
        jcboPL.setEnabled(false);

        final DefaultComboBoxModel<String> jComboBoxNACModel = new DefaultComboBoxModel<>(abstractRadioReceiver.getDigitalNACValues());
        jcboNAC = new JComboBox<>(jComboBoxNACModel);
        jcboNAC.setEnabled(false);
        
        final DefaultComboBoxModel<String> jComboBoxModeModel = new DefaultComboBoxModel<>(getModeNames());
        jcboMode = new JComboBox<>(jComboBoxModeModel);

        final DefaultComboBoxModel<String> jComboBoxSquelchModeModel = new DefaultComboBoxModel<>();
        jcboSquelchMode = new JComboBox<>(jComboBoxSquelchModeModel);

        final DefaultComboBoxModel<Integer> jComboBoxFilterModel = new DefaultComboBoxModel<>(abstractRadioReceiver.getAvailableFilters());
        jcboFilter = new JComboBox<>(jComboBoxFilterModel);

        jLabelAttenuator = new JLabel("Attn (dB)");
        jLabelAttenuator.setHorizontalAlignment(SwingConstants.CENTER);

        jLabelColorCode = new JLabel("CC / TS");
        jLabelColorCode.setHorizontalAlignment(SwingConstants.CENTER);

        jLabelDPL = new JLabel("DPL");
        jLabelDPL.setHorizontalAlignment(SwingConstants.CENTER);

        jcbDPLInv.setHorizontalAlignment(SwingConstants.CENTER);
        
        jLabelDPLInv = new JLabel("INV");
        jLabelDPLInv.setHorizontalAlignment(SwingConstants.CENTER);

        jLabelFilter = new JLabel("Filter (Hz)");
        jLabelFilter.setHorizontalAlignment(SwingConstants.CENTER);

        jLabelFrequency = new JLabel("Frequency (MHz)");
        jLabelFrequency.setHorizontalAlignment(SwingConstants.CENTER);

        jLabelMode = new JLabel("Mode");
        jLabelMode.setHorizontalAlignment(SwingConstants.CENTER);

        jLabelSquelchMode = new JLabel("Sq Mode");
        jLabelSquelchMode.setHorizontalAlignment(SwingConstants.CENTER);

        jLabelNAC = new JLabel("NAC");
        jLabelNAC.setHorizontalAlignment(SwingConstants.CENTER);

        jLabelPL = new JLabel("PL");
        jLabelPL.setHorizontalAlignment(SwingConstants.CENTER);

        deviceComboBoxLabel = new JLabel("Device Type");
        calFileComboBoxLabel = new JLabel("Calibration File");
        
        jlblVolume = new JLabel("Volume");
        jlblSquelch = new JLabel("Squelch");
        
        startRadioWithSystemCheckBox = new JCheckBox("Start Radio With System");
        
        okButton = new JButton("OK");
        cancelButton = new JButton("Cancel");
        applyButton = new JButton("Apply");
        openCalFileEditorButton = new JButton("Open Cal File Editor");
        newFileButton = new JButton("Create New Cal File");

        jlblScanLabelArray = new JLabel[10];

        jcbAFCArray = new JCheckBox[10];
        jcbAGCArray = new JCheckBox[10];
        jcbNBArray = new JCheckBox[10];
        jcbSampleRSSIArray = new JCheckBox[10];
        jcbSampleSINADArray = new JCheckBox[10];
        jcbSampleBERArray = new JCheckBox[10];
        jcbSelectArray = new JCheckBox[10];

        jcboModeArray = new JComboBox<?>[10];
        jcboSquelchCodeArray = new JComboBox<?>[10];
        jcboSquelchModeArray = new JComboBox<?>[10];

        jcboModeModelArray = new DefaultComboBoxModel[10];
        jcboSquelchCodeModelArray = new DefaultComboBoxModel[10];
        jcboSquelchModeModelArray = new DefaultComboBoxModel[10];

        jftfAttnArray = new JFormattedTextField[10];
        jftfFreqArray = new JFormattedTextField[10];

        jlblSelect = new JLabel();
        jlblFrequency = new JLabel();
        jlblSquelchMode = new JLabel();
        jlblSquelchCode = new JLabel();
        jlblMode = new JLabel();
        jlblRSSI = new JLabel();
        jlblSINAD = new JLabel();
        jlblBER = new JLabel();
        jlblNoiseBlanker = new JLabel();
        jlblAGC = new JLabel();
        jlblAFC = new JLabel();
        jlblAttenuator = new JLabel();

        deviceComboBoxLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        calFileComboBoxLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        jlblVolume.setHorizontalAlignment(SwingConstants.CENTER);
        jlblSquelch.setHorizontalAlignment(SwingConstants.CENTER);

        okButton.setFont(new Font("Tahoma", Font.BOLD, 11));
        okButton.setMultiClickThreshhold(50L);
        
        cancelButton.setFont(new Font("Tahoma", Font.BOLD, 11));
        cancelButton.setMultiClickThreshhold(50L);
        
        applyButton.setFont(new Font("Tahoma", Font.BOLD, 11));
        applyButton.setMultiClickThreshhold(50L);
        
        newFileButton.setFont(new Font("Tahoma", Font.BOLD, 11));
        newFileButton.setMultiClickThreshhold(50L);

        openCalFileEditorButton.setFont(new Font("Tahoma", Font.BOLD, 11));
        openCalFileEditorButton.setMultiClickThreshhold(50L);

        startRadioWithSystemCheckBox.setMultiClickThreshhold(50L);

        for (int i = 0; i < abstractRadioReceiver.getScanListSize(); i++) {
            jlblScanLabelArray[i] = new JLabel("F" + (i + 1));

            jftfFreqArray[i] = new JFormattedTextField(freqFormatter);
            jftfFreqArray[i].setHorizontalAlignment(SwingConstants.RIGHT);
            jftfFreqArray[i].setFont(new Font("Calabri", Font.PLAIN, 11));
            jftfFreqArray[i].setBackground(Color.WHITE);
            jftfFreqArray[i].setForeground(Color.BLACK);
            jftfFreqArray[i].setFocusLostBehavior(JFormattedTextField.COMMIT);
            jftfFreqArray[i].setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Collections.emptySet());
            
            jftfAttnArray[i] = new JFormattedTextField(attnFormatter);
            jftfAttnArray[i].setHorizontalAlignment(SwingConstants.RIGHT);
            jftfAttnArray[i].setFont(new Font("Calabri", Font.PLAIN, 11));
            jftfAttnArray[i].setBackground(Color.WHITE);
            jftfAttnArray[i].setForeground(Color.BLACK);
            jftfAttnArray[i].setFocusLostBehavior(JFormattedTextField.COMMIT);
            jftfAttnArray[i].setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Collections.emptySet());

            jcbSelectArray[i] = new JCheckBox("");
            jcbSelectArray[i].setHorizontalAlignment(SwingConstants.CENTER);
            jcbSelectArray[i].setHorizontalTextPosition(SwingConstants.CENTER);
            
            jcbAFCArray[i] = new JCheckBox("");
            jcbAFCArray[i].setHorizontalAlignment(SwingConstants.CENTER);
            jcbAFCArray[i].setHorizontalTextPosition(SwingConstants.CENTER);
            
            jcbAGCArray[i] = new JCheckBox("");
            jcbAGCArray[i].setHorizontalAlignment(SwingConstants.CENTER);
            jcbAGCArray[i].setHorizontalTextPosition(SwingConstants.CENTER);

            jcbNBArray[i] = new JCheckBox("");
            jcbNBArray[i].setHorizontalAlignment(SwingConstants.CENTER);
            jcbNBArray[i].setHorizontalTextPosition(SwingConstants.CENTER);
            
            jcbSampleRSSIArray[i] = new JCheckBox("");
            jcbSampleRSSIArray[i].setHorizontalAlignment(SwingConstants.CENTER);
            jcbSampleRSSIArray[i].setHorizontalTextPosition(SwingConstants.CENTER);
            
            jcbSampleSINADArray[i] = new JCheckBox("");
            jcbSampleSINADArray[i].setHorizontalAlignment(SwingConstants.CENTER);
            jcbSampleSINADArray[i].setHorizontalTextPosition(SwingConstants.CENTER);
            
            jcbSampleBERArray[i] = new JCheckBox("");
            jcbSampleBERArray[i].setHorizontalAlignment(SwingConstants.CENTER);
            jcbSampleBERArray[i].setHorizontalTextPosition(SwingConstants.CENTER);
            
            jcboModeModelArray[i] = new DefaultComboBoxModel<>(getModeNames()); 
            jcboModeArray[i] = new JComboBox<>(jcboModeModelArray[i]);
            
            jcboSquelchModeModelArray[i] = new DefaultComboBoxModel<>();
            jcboSquelchModeArray[i] = new JComboBox<>(jcboSquelchModeModelArray[i]);
            
            jcboSquelchCodeModelArray[i] = new DefaultComboBoxModel<>();
            jcboSquelchCodeArray[i] = new JComboBox<>(jcboSquelchCodeModelArray[i]);
        }

        jlblSelect.setHorizontalAlignment(SwingConstants.CENTER);
        jlblSelect.setText("Select");
        jlblSelect.setFocusable(false);
        jlblSelect.setHorizontalTextPosition(SwingConstants.CENTER);

        jlblFrequency.setHorizontalAlignment(SwingConstants.CENTER);
        jlblFrequency.setText("Frequency");
        jlblFrequency.setFocusable(false);
        jlblFrequency.setHorizontalTextPosition(SwingConstants.CENTER);

        jlblSquelchMode.setHorizontalAlignment(SwingConstants.CENTER);
        jlblSquelchMode.setText("Squelch");
        jlblSquelchMode.setFocusable(false);
        jlblSquelchMode.setHorizontalTextPosition(SwingConstants.CENTER);

        jlblSquelchCode.setHorizontalAlignment(SwingConstants.CENTER);
        jlblSquelchCode.setText("Code");
        jlblSquelchCode.setFocusable(false);
        jlblSquelchCode.setHorizontalTextPosition(SwingConstants.CENTER);

        jlblMode.setHorizontalAlignment(SwingConstants.CENTER);
        jlblMode.setText("Mode");
        jlblMode.setFocusable(false);
        jlblMode.setHorizontalTextPosition(SwingConstants.CENTER);

        jlblRSSI.setHorizontalAlignment(SwingConstants.CENTER);
        jlblRSSI.setText("RSSI");
        jlblRSSI.setFocusable(false);
        jlblRSSI.setHorizontalTextPosition(SwingConstants.CENTER);

        jlblSINAD.setHorizontalAlignment(SwingConstants.CENTER);
        jlblSINAD.setText("SINAD");
        jlblSINAD.setFocusable(false);
        jlblSINAD.setHorizontalTextPosition(SwingConstants.CENTER);

        jlblBER.setHorizontalAlignment(SwingConstants.CENTER);
        jlblBER.setText("BER");
        jlblBER.setFocusable(false);
        jlblBER.setHorizontalTextPosition(SwingConstants.CENTER);

        jlblNoiseBlanker.setHorizontalAlignment(SwingConstants.CENTER);
        jlblNoiseBlanker.setText("NB");
        jlblNoiseBlanker.setFocusable(false);
        jlblNoiseBlanker.setHorizontalTextPosition(SwingConstants.CENTER);

        jlblAGC.setHorizontalAlignment(SwingConstants.CENTER);
        jlblAGC.setText("AGC");
        jlblAGC.setFocusable(false);
        jlblAGC.setHorizontalTextPosition(SwingConstants.CENTER);

        jlblAFC.setHorizontalAlignment(SwingConstants.CENTER);
        jlblAFC.setText("AFC");
        jlblAFC.setFocusable(false);
        jlblAFC.setHorizontalTextPosition(SwingConstants.CENTER);

        jlblAttenuator.setHorizontalAlignment(SwingConstants.CENTER);
        jlblAttenuator.setText("ATTN");
        jlblAttenuator.setFocusable(false);
        jlblAttenuator.setHorizontalTextPosition(SwingConstants.CENTER);

        calFileComboBoxModel = new SortedComboBoxModel<>();
        calFileComboBox = new JComboBox<>(calFileComboBoxModel);

        deviceComboBoxModel = new DefaultComboBoxModel<>(abstractRadioReceiver.getAllDevicesWithCalFiles());
        deviceComboBox = new JComboBox<>(deviceComboBoxModel);

        deviceComboBox.setEditable(false);
        calFileComboBox.setEditable(false);
    }

    private String[] getModeNames() {
    	final String[] modeNames = new String[abstractRadioReceiver.getModeNameValues().length];
		for (int i = 0; i < abstractRadioReceiver.getModeNameValues().length; i++) {
			modeNames[i] = abstractRadioReceiver.getModeNameValues()[i].name();
		}
		return modeNames;
	}

    private void updateScanObjects() {
        for (int i = 0; i < abstractRadioReceiver.getScanListSize(); i++) {
            jcbSelectArray[i].setSelected(abstractRadioReceiver.getScanElement(i).isScanSelected() && 
            		!isZero(abstractRadioReceiver.getScanElement(i).getFrequency()));
            if (!isZero(abstractRadioReceiver.getScanElement(i).getFrequency())) {
                jftfFreqArray[i].setText(new DecimalFormat("###0.0####").format(abstractRadioReceiver.getScanElement(i).getFrequency()));
            }
            if (!isZero(abstractRadioReceiver.getScanElement(i).getAttenuator())) {
                jftfAttnArray[i].setText(new DecimalFormat("##0.0#").format(abstractRadioReceiver.getScanElement(i).getAttenuator()));
            }
            jcboModeArray[i].setSelectedItem(abstractRadioReceiver.getScanElement(i).getModeName().name());
            jcbSampleBERArray[i].setSelected(abstractRadioReceiver.getScanElement(i).isSampleBER());
            jcbSampleRSSIArray[i].setSelected(abstractRadioReceiver.getScanElement(i).isSampleRSSI());
            jcbSampleSINADArray[i].setSelected(abstractRadioReceiver.getScanElement(i).isSampleSINAD());
            jcbAGCArray[i].setSelected(abstractRadioReceiver.getScanElement(i).isAGC());
            jcbAFCArray[i].setSelected(abstractRadioReceiver.getScanElement(i).isAFC());
            jcbNBArray[i].setSelected(abstractRadioReceiver.getScanElement(i).isNoiseBlanker());
            updateSquelchModeComboBox(i);
            updateSquelchCodeComboBox(i);
        }
    }

    private void configureSwingComponents() {
        setDeviceComboBox(abstractRadioReceiver.getCalFile());
        
        final String[] mfrMod = ((String) deviceComboBox.getSelectedItem()).split(" ");
        calFileComboBoxModel.addAll(getCalFileListForManufacturerModel(mfrMod[0], mfrMod[1], AbstractRadioReceiver.getCalParentFile()));
        calFileComboBox.setSelectedItem(abstractRadioReceiver.getCalFile().getPath());

        updateScanObjects();

        final ButtonModel vfoScanModeButtonModel;

        if (abstractRadioReceiver.isScanEnabled()) {
            vfoScanModeButtonModel = scanModeButton.getModel();
        } else {
            vfoScanModeButtonModel = vfoModeButton.getModel();
        }

        vfoScanButtonGroup.setSelected(vfoScanModeButtonModel, true);

        jSliderVolume.setEnabled(abstractRadioReceiver.isSupportsVolumeSet());
        jSliderSquelch.setEnabled(abstractRadioReceiver.isSupportsSquelchSet());
        jlblVolume.setEnabled(abstractRadioReceiver.isSupportsVolumeSet());
        jlblSquelch.setEnabled(abstractRadioReceiver.isSupportsSquelchSet());

        startRadioWithSystemCheckBox.setSelected(abstractRadioReceiver.isStartRadioWithSystem());

        freqFormatter.setMinimum(abstractRadioReceiver.getMinRxFreq());
        freqFormatter.setMaximum(abstractRadioReceiver.getMaxRxFreq());

        attnFormatter.setMinimum(abstractRadioReceiver.getMinAttenuator());
        attnFormatter.setMaximum(abstractRadioReceiver.getMaxAttenuator());

        jSliderVolume.setValue((int) Math.round(abstractRadioReceiver.getVolume()));
        jSliderSquelch.setValue((int) Math.round(abstractRadioReceiver.getSquelch()));
        jcbSampleRSSI.setSelected(abstractRadioReceiver.isRssiEnabled());
        jcbSampleBER.setSelected(abstractRadioReceiver.isBerEnabled());
        jcbSampleSINAD.setSelected(abstractRadioReceiver.isSinadEnabled());
        jcbAGC.setSelected(abstractRadioReceiver.isAGC());
        jcbAFC.setSelected(abstractRadioReceiver.isAFC());
        jcbNB.setSelected(abstractRadioReceiver.isNoiseBlanker());
        jcbDPLInv.setSelected(abstractRadioReceiver.isDPLInverted());
        jftfFreq.setText(String.valueOf(abstractRadioReceiver.getFrequency()));
        jftfAttn.setText(String.valueOf(abstractRadioReceiver.getAttenuator()));
        jcboMode.setSelectedItem(abstractRadioReceiver.getModeName().name());

        updateModeComboBox();

        setColorCodeComboBox(abstractRadioReceiver.getDigitalColorCode(), abstractRadioReceiver.getTimeSlot());
        jcboDPL.setSelectedItem("%03d".formatted(abstractRadioReceiver.getDPL()));
        jcboPL.setSelectedItem(String.valueOf(abstractRadioReceiver.getPL() / 10.0));
        jcboFilter.setSelectedItem(abstractRadioReceiver.getFilterHz());
        jcboNAC.setSelectedItem(abstractRadioReceiver.getNetworkAccessCode());

        jcboSquelchMode.setSelectedItem(abstractRadioReceiver.getSquelchMode().name());

        updateSquelchCodeComboBoxes();
    }

    private void setColorCodeComboBox(int colorCode, int timeSlot) {
        jcboColorCode.setSelectedItem(String.valueOf(colorCode) + "/" + timeSlot);
    }

    private JPanel getRadioPanel() {
        final JPanel panel = new JPanel();

        final GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);

        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                            .addComponent(jftfFreq, 115, 115, 115)
                            .addComponent(jLabelFrequency, 115, 115, 115))
                        .addGap(10, 10, 10)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                            .addComponent(jLabelMode, 120, 120, 120)
                            .addComponent(jcboMode, 120, 120, 120))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                            .addComponent(jcboSquelchMode, 100, 100, 100)
                            .addComponent(jLabelSquelchMode, 100, 100, 100))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(jcboFilter, 100, 100, 100)
                            .addComponent(jLabelFilter, 100, 100, 100))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(jftfAttn, 70, 70, 70)
                            .addComponent(jLabelAttenuator, 70, 70, 70))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                            .addComponent(jcboColorCode, 60, 60, 60)
                            .addComponent(jLabelColorCode, 60, 60, 60))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(jLabelNAC, 80, 80, 80)
                            .addComponent(jcboNAC, 80, 80, 80))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(jcboPL, 80, 80, 80)
                            .addComponent(jLabelPL, 80, 80, 80))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(jcboDPL, 80, 80, 80)
                            .addComponent(jLabelDPL, 80, 80, 80))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                            .addComponent(jcbDPLInv, 40, 40, 40)
                            .addComponent(jLabelDPLInv, 40, 40, 40))
                        .addGap(44, 44, 44))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jSliderVolume, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jSliderSquelch, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(119, 119, 119)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                    .addComponent(jcbNB)
                                    .addComponent(jcbAFC)
                                    .addComponent(jcbAGC))
                                .addGap(159, 159, 159)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                    .addComponent(jcbSampleRSSI)
                                    .addComponent(jcbSampleSINAD)
                                    .addComponent(jcbSampleBER))))
                        .addContainerGap(235, Short.MAX_VALUE)))));

        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
		    .addGroup(layout.createSequentialGroup()
		            .addContainerGap()
		            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
		                    .addComponent(jSliderSquelch, 60, 60, 60)
		                    .addComponent(jSliderVolume, 60, 60, 60))
		            .addGap(43, 43, 43)
		            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
		                    .addComponent(jLabelFrequency)
		                    .addComponent(jLabelMode)
		                    .addComponent(jLabelSquelchMode)
		                    .addComponent(jLabelFilter)
		                    .addComponent(jLabelAttenuator)
		                    .addComponent(jLabelColorCode)
		                    .addComponent(jLabelNAC)
		                    .addComponent(jLabelPL)
		                    .addComponent(jLabelDPL)
		                    .addComponent(jLabelDPLInv))
		            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
		            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
		                    .addComponent(jcbDPLInv)
		                    .addComponent(jcboDPL, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
		                    .addComponent(jcboPL, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
		                    .addComponent(jcboNAC, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
		                    .addComponent(jcboColorCode, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
		                    .addComponent(jftfAttn, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
		                    .addComponent(jcboFilter, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
		                    .addComponent(jcboSquelchMode, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
		                    .addComponent(jcboMode, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
		                    .addComponent(jftfFreq, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
		            .addGap(17, 17, 17)
		            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
		                    .addComponent(jcbAGC)
		                    .addComponent(jcbSampleRSSI))
		            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
		            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
		                    .addComponent(jcbAFC)
		                    .addComponent(jcbSampleSINAD))
		            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
		            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
		                    .addComponent(jcbNB)
		                    .addComponent(jcbSampleBER))
		            .addContainerGap(73, Short.MAX_VALUE)));

        return panel;
    }

    private JPanel getSystemPanel() {
        final JPanel panel = new JPanel();

        final GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);

        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                            .addComponent(calFileComboBoxLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(deviceComboBoxLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(calFileComboBox, GroupLayout.PREFERRED_SIZE, 440, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(openCalFileEditorButton))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(deviceComboBox, GroupLayout.PREFERRED_SIZE, 160, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(newFileButton))))
                    .addComponent(startRadioWithSystemCheckBox)
                    .addComponent(vfoScanPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                    .addComponent(deviceComboBox, GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(newFileButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(deviceComboBoxLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addComponent(calFileComboBox)
                    .addComponent(calFileComboBoxLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(openCalFileEditorButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(startRadioWithSystemCheckBox, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(vfoScanPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(110, 110, 110))
        );
        
        final GroupLayout vfoScanPanelLayout = new GroupLayout(vfoScanPanel);
        
        vfoScanPanel.setLayout(vfoScanPanelLayout);
       
        vfoScanPanelLayout.setHorizontalGroup(vfoScanPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(vfoScanPanelLayout.createSequentialGroup()
                .addGroup(vfoScanPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(scanModeButton)
                    .addComponent(vfoModeButton))
                .addGap(0, 46, Short.MAX_VALUE)));
        
        vfoScanPanelLayout.setVerticalGroup(vfoScanPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(vfoScanPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scanModeButton)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(vfoModeButton)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        	);

        return panel;
    }

    private JPanel getScanElementPanel() {
        final JPanel panel = new JPanel();

        final GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);

        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(jlblScanLabelArray[0])
                    .addComponent(jlblScanLabelArray[1])
                    .addComponent(jlblScanLabelArray[2])
                    .addComponent(jlblScanLabelArray[3])
                    .addComponent(jlblScanLabelArray[4])
                    .addComponent(jlblScanLabelArray[5])
                    .addComponent(jlblScanLabelArray[6])
                    .addComponent(jlblScanLabelArray[7])
                    .addComponent(jlblScanLabelArray[8])
                    .addComponent(jlblScanLabelArray[9]))
            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(jlblSelect, GroupLayout.PREFERRED_SIZE, 36, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbSelectArray[0], GroupLayout.PREFERRED_SIZE, 36, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbSelectArray[1], GroupLayout.PREFERRED_SIZE, 36, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbSelectArray[2], GroupLayout.PREFERRED_SIZE, 36, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbSelectArray[3], GroupLayout.PREFERRED_SIZE, 36, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbSelectArray[4], GroupLayout.PREFERRED_SIZE, 36, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbSelectArray[5], GroupLayout.PREFERRED_SIZE, 36, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbSelectArray[6], GroupLayout.PREFERRED_SIZE, 36, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbSelectArray[7], GroupLayout.PREFERRED_SIZE, 36, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbSelectArray[8], GroupLayout.PREFERRED_SIZE, 36, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbSelectArray[9], GroupLayout.PREFERRED_SIZE, 36, GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(jlblFrequency, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jftfFreqArray[0], GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jftfFreqArray[1], GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jftfFreqArray[2], GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jftfFreqArray[3], GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jftfFreqArray[4], GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jftfFreqArray[5], GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jftfFreqArray[6], GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jftfFreqArray[7], GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jftfFreqArray[8], GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jftfFreqArray[9], GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(jlblSquelchMode, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcboSquelchModeArray[0], GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcboSquelchModeArray[1], GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcboSquelchModeArray[2], GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcboSquelchModeArray[3], GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcboSquelchModeArray[4], GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcboSquelchModeArray[5], GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcboSquelchModeArray[6], GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcboSquelchModeArray[7], GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcboSquelchModeArray[8], GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcboSquelchModeArray[9], GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(jcboSquelchCodeArray[9], GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcboSquelchCodeArray[8], GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcboSquelchCodeArray[7], GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcboSquelchCodeArray[6], GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcboSquelchCodeArray[5], GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcboSquelchCodeArray[4], GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcboSquelchCodeArray[3], GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcboSquelchCodeArray[2], GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcboSquelchCodeArray[1], GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcboSquelchCodeArray[0], GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jlblSquelchCode, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(jlblMode, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcboModeArray[0], GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcboModeArray[1], GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcboModeArray[2], GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcboModeArray[3], GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcboModeArray[4], GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcboModeArray[5], GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcboModeArray[6], GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcboModeArray[7], GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcboModeArray[8], GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcboModeArray[9], GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(jcbSampleRSSIArray[0], GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jlblRSSI, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbSampleRSSIArray[1], GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbSampleRSSIArray[2], GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbSampleRSSIArray[3], GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbSampleRSSIArray[4], GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbSampleRSSIArray[5], GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbSampleRSSIArray[6], GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbSampleRSSIArray[7], GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbSampleRSSIArray[8], GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbSampleRSSIArray[9], GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(jlblSINAD, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbSampleSINADArray[0], GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbSampleSINADArray[1], GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbSampleSINADArray[2], GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbSampleSINADArray[3], GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbSampleSINADArray[4], GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbSampleSINADArray[5], GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbSampleSINADArray[6], GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbSampleSINADArray[7], GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbSampleSINADArray[8], GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbSampleSINADArray[9], GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(jlblBER, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbSampleBERArray[0], GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbSampleBERArray[1], GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbNBArray[2], GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbSampleBERArray[3], GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbSampleBERArray[4], GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbSampleBERArray[5], GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbSampleBERArray[6], GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbSampleBERArray[7], GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbSampleBERArray[8], GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbSampleBERArray[9], GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(jcbNBArray[9], GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbNBArray[8], GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbNBArray[7], GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbNBArray[6], GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbNBArray[5], GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbNBArray[4], GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbNBArray[3], GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbSampleBERArray[2], GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbNBArray[1], GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbNBArray[0], GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jlblNoiseBlanker, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(jlblAGC, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbAGCArray[0], GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbAGCArray[1], GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbAGCArray[2], GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbAGCArray[3], GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbAGCArray[4], GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbAGCArray[5], GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbAGCArray[6], GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbAGCArray[7], GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbAGCArray[8], GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbAGCArray[9], GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(jlblAFC, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbAFCArray[0], GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbAFCArray[1], GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbAFCArray[2], GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbAFCArray[3], GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbAFCArray[4], GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbAFCArray[5], GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbAFCArray[6], GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbAFCArray[7], GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbAFCArray[8], GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcbAFCArray[9], GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(jlblAttenuator, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jftfAttnArray[0], GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jftfAttnArray[1], GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jftfAttnArray[2], GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jftfAttnArray[3], GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jftfAttnArray[4], GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jftfAttnArray[5], GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jftfAttnArray[6], GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jftfAttnArray[7], GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jftfAttnArray[8], GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jftfAttnArray[9], GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE))
            .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(jlblSINAD)
                                        .addComponent(jlblBER)
                                        .addComponent(jlblRSSI)
                                        .addComponent(jlblAttenuator)
                                        .addComponent(jlblSquelchCode)
                                        .addComponent(jlblFrequency)
                                        .addComponent(jlblSquelchMode)
                                        .addComponent(jlblMode)
                                        .addComponent(jlblSelect)
                                        .addComponent(jlblAFC)
                                        .addComponent(jlblNoiseBlanker)
                                        .addComponent(jlblAGC))
                                .addGap(6, 6, 6)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                        .addComponent(jftfAttnArray[0], GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jcbSampleRSSIArray[0])
                                        .addComponent(jcbSampleSINADArray[0])
                                        .addComponent(jcbSampleBERArray[0])
                                        .addComponent(jcbSelectArray[0])
                                        .addComponent(jftfFreqArray[0], GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jcboSquelchModeArray[0], GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jcboModeArray[0], GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jcboSquelchCodeArray[0], GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jlblScanLabelArray[0])
                                        .addComponent(jcbAFCArray[0])
                                        .addComponent(jcbNBArray[0])
                                        .addComponent(jcbAGCArray[0]))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                        .addComponent(jcbNBArray[1])
                                        .addComponent(jcbAGCArray[1])
                                        .addComponent(jcbAFCArray[1])
                                        .addComponent(jftfAttnArray[1], GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jcbSelectArray[1])
                                        .addComponent(jftfFreqArray[1], GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jcboSquelchModeArray[1], GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jcboSquelchCodeArray[1], GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jcboModeArray[1], GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jcbSampleRSSIArray[1])
                                        .addComponent(jcbSampleSINADArray[1])
                                        .addComponent(jcbSampleBERArray[1])
                                        .addComponent(jlblScanLabelArray[1]))
                                .addGap(6, 6, 13)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                        .addComponent(jcbNBArray[2])
                                        .addComponent(jcbAGCArray[2])
                                        .addComponent(jcbAFCArray[2])
                                        .addComponent(jftfAttnArray[2], GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jcboSquelchModeArray[2], GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jcbSampleBERArray[2])
                                        .addComponent(jcbSampleRSSIArray[2])
                                        .addComponent(jcboModeArray[2], GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jcbSelectArray[2])
                                        .addComponent(jcbSampleSINADArray[2])
                                        .addComponent(jftfFreqArray[2], GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jcboSquelchCodeArray[2], GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jlblScanLabelArray[2]))
                                .addGap(6, 6, 19)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                        .addComponent(jcbNBArray[3])
                                        .addComponent(jcbAGCArray[3])
                                        .addComponent(jcbAFCArray[3])
                                        .addComponent(jcbSelectArray[3])
                                        .addComponent(jftfFreqArray[3], GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jcboSquelchModeArray[3], GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jcboSquelchCodeArray[3], GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jcboModeArray[3], GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jcbSampleRSSIArray[3])
                                        .addComponent(jcbSampleSINADArray[3])
                                        .addComponent(jcbSampleBERArray[3])
                                        .addComponent(jlblScanLabelArray[3])
                                        .addComponent(jftfAttnArray[3], GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addGap(6, 6, 22)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                        .addComponent(jcbSelectArray[4])
                                        .addComponent(jftfFreqArray[4], GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jcboSquelchModeArray[4], GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jlblScanLabelArray[4])
                                        .addComponent(jcboSquelchCodeArray[4], GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jcboModeArray[4], GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jcbSampleRSSIArray[4])
                                        .addComponent(jcbSampleSINADArray[4])
                                        .addComponent(jcbSampleBERArray[4])
                                        .addComponent(jftfAttnArray[4], GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jcbNBArray[4])
                                        .addComponent(jcbAGCArray[4])
                                        .addComponent(jcbAFCArray[4]))
                                .addGap(6, 6, 19)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                        .addComponent(jlblScanLabelArray[5])
                                        .addComponent(jftfFreqArray[5], GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jcboSquelchCodeArray[5], GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jcboModeArray[5], GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jcbSelectArray[5])
                                        .addComponent(jcbSampleRSSIArray[5])
                                        .addComponent(jcbSampleBERArray[5])
                                        .addComponent(jcboSquelchModeArray[5], GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jcbSampleSINADArray[5])
                                        .addComponent(jcbNBArray[5])
                                        .addComponent(jftfAttnArray[5], GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jcbAFCArray[5])
                                        .addComponent(jcbAGCArray[5]))
                                .addGap(6, 6, 12)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                        .addComponent(jcbNBArray[6])
                                        .addComponent(jcbAFCArray[6])
                                        .addComponent(jftfAttnArray[6], GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jlblScanLabelArray[6])
                                        .addComponent(jcbSampleSINADArray[6])
                                        .addComponent(jcboSquelchCodeArray[6], GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jcbSampleRSSIArray[6])
                                        .addComponent(jcboModeArray[6], GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jcbSampleBERArray[6])
                                        .addComponent(jcboSquelchModeArray[6], GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jcbSelectArray[6])
                                        .addComponent(jftfFreqArray[6], GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jcbAGCArray[6]))
                                .addGap(6, 6, 12)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                        .addComponent(jcbAGCArray[7])
                                        .addComponent(jcbNBArray[7])
                                        .addComponent(jftfAttnArray[7], GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jcbAFCArray[7])
                                        .addComponent(jlblScanLabelArray[7])
                                        .addComponent(jcboModeArray[7], GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jftfFreqArray[7], GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jcboSquelchCodeArray[7], GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jcbSampleRSSIArray[7])
                                        .addComponent(jcbSampleSINADArray[7])
                                        .addComponent(jcbSampleBERArray[7])
                                        .addComponent(jcboSquelchModeArray[7], GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jcbSelectArray[7]))
                                .addGap(6, 6, 12)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                        .addComponent(jcbAGCArray[8])
                                        .addComponent(jftfAttnArray[8], GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jcbNBArray[8])
                                        .addComponent(jcbAFCArray[8])
                                        .addComponent(jlblScanLabelArray[8])
                                        .addComponent(jcbSampleRSSIArray[8])
                                        .addComponent(jcboSquelchCodeArray[8], GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jftfFreqArray[8], GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jcboModeArray[8], GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jcbSampleSINADArray[8])
                                        .addComponent(jcboSquelchModeArray[8], GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jcbSelectArray[8])
                                        .addComponent(jcbSampleBERArray[8]))
                                .addGap(6, 6, 12)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                        .addComponent(jcbAFCArray[9])
                                        .addComponent(jcbNBArray[9])
                                        .addComponent(jcbAGCArray[9])
                                        .addComponent(jftfAttnArray[9], GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jlblScanLabelArray[9])
                                        .addComponent(jcbSampleSINADArray[9])
                                        .addComponent(jcbSampleBERArray[9])
                                        .addComponent(jcbSampleRSSIArray[9])
                                        .addComponent(jcboSquelchCodeArray[9], GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jftfFreqArray[9], GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jcboSquelchModeArray[9], GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jcboModeArray[9], GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jcbSelectArray[9]))
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        return panel;
    }

    private JPanel getGUI(JTabbedPane tabbedPane) {
        final JPanel panel = new JPanel();

        final GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);

        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addComponent(okButton, 90, 90, 90)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(applyButton, 90, 90, 90)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(cancelButton, 90, 90, 90))
                                .addComponent(tabbedPane, GroupLayout.PREFERRED_SIZE, 930, GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(tabbedPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(cancelButton)
                                .addComponent(applyButton)
                                .addComponent(okButton))
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        return panel;
    }

    private void drawGraphicalUserInterface() {
        add(getGUI(getTabbedPane()));

        pack();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        final Toolkit tk = Toolkit.getDefaultToolkit();

        final Dimension screenSize = tk.getScreenSize();

        setLocation((screenSize.width / 2) - (getWidth() / 2), (screenSize.height / 2) - (getHeight() / 2));

        setModal(true);
        setTitle("Receiver Settings");
        setVisible(true);
    }

    private static void invokeLaterInDispatchThreadIfNeeded(Runnable runnable) {
        if (EventQueue.isDispatchThread()) {
            runnable.run();
        } else {
            SwingUtilities.invokeLater(runnable);
        }
    }

    private class RadioButtonHandler implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent ie) {
            if (ie.getSource() == vfoModeButton) {
                abstractRadioReceiver.setScanEnabled(false);

            } else if (ie.getSource() == scanModeButton) {
                abstractRadioReceiver.setScanEnabled(true);
            }
        }
    }

}
