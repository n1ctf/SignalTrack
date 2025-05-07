package tty;

import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;

import jssc.SerialPortList;

import tty.AbstractTeletypeController.BaudRate;
import tty.AbstractTeletypeController.FlowControl;
import tty.AbstractTeletypeController.StopBits;
import tty.AbstractTeletypeController.DataBits;
import tty.AbstractTeletypeController.Parity;

public class SerialComponent {

    private JComboBox<String> selectComPortComboBox;
    private DefaultComboBoxModel<Integer> selectBaudRateComboBoxModel;
    private JComboBox<Integer> selectBaudRateComboBox;
    
    private JComboBox<?> selectFlowControlInComboBox;
    private JComboBox<?> selectFlowControlOutComboBox;
    
    private JLabel selectComPortComboBoxLabel;
    private JLabel selectBaudRateComboBoxLabel;
    private JLabel selectDataBitsButtonGroupLabel;
    private JLabel selectStopBitsButtonGroupLabel;
    private JLabel selectParityButtonGroupLabel;
    private JLabel selectFlowControlInComboBoxLabel;
    private JLabel selectFlowControlOutComboBoxLabel;
    
    private JRadioButton dataBits5;
    private JRadioButton dataBits6;
    private JRadioButton dataBits7;
    private JRadioButton dataBits8;
    
    private JRadioButton stopBits1;
    private JRadioButton stopBits15;
    private JRadioButton stopBits2;
    
    private JRadioButton parityNone;
    private JRadioButton parityOdd;
    private JRadioButton parityEven;
    private JRadioButton parityMark;
    private JRadioButton paritySpace;

    private JCheckBox enableDTRCheckBox;
    private JCheckBox enableRTSCheckBox;
    private JCheckBox eventRXCHARCheckBox;
    private JCheckBox eventRXFLAGCheckBox;
    private JCheckBox eventTXEMPTYCheckBox;
    private JCheckBox eventCTSCheckBox;
    private JCheckBox eventDSRCheckBox;
    private JCheckBox eventRLSDCheckBox;
    private JCheckBox eventERRCheckBox;
    private JCheckBox eventRINGCheckBox;
    private JCheckBox eventBREAKCheckBox;
    
    private JCheckBox cbReportFramingErrors;
    private JCheckBox cbReportConfigurationErrors;
    private JCheckBox cbReportBufferOverrunErrors;
    private JCheckBox cbReportParityMismatchErrors;
    private JCheckBox cbReportDTRNotSetErrors;
    private JCheckBox cbReportRTSNotSetErrors;
    private JCheckBox cbReportEventMaskErrors;
    private JCheckBox cbReportFlowControlErrors;
    private JCheckBox cbReportPurgeFailures;
    private JCheckBox cbReportBreakInterrupts;
    private JCheckBox cbReportTransmitFailures;
    private JCheckBox cbLogSerialPortErrors;
    
    private ButtonGroup dataBitsButtonGroup;
    private ButtonGroup parityButtonGroup;
    private ButtonGroup stopBitsButtonGroup;
    
    private final AbstractTeletypeController abstractTeletypeController;

    public SerialComponent(AbstractTeletypeController abstractTeletypeController) {
        this.abstractTeletypeController = abstractTeletypeController;

        initializeComponents();
        updateComponents();
        configureListeners();
    }
    
    private void enableSerialBaudRateComponents(boolean disabled) {
        selectBaudRateComboBox.setEnabled(!disabled);
    }

    private void enableSerialInterfaceComponents(boolean disabled) {
        selectFlowControlInComboBox.setEnabled(!disabled);
        selectFlowControlOutComboBox.setEnabled(!disabled);
        dataBits5.setEnabled(!disabled);
        dataBits6.setEnabled(!disabled);
        dataBits7.setEnabled(!disabled);
        dataBits8.setEnabled(!disabled);
        stopBits1.setEnabled(!disabled);
        stopBits15.setEnabled(!disabled);
        stopBits2.setEnabled(!disabled);
        parityNone.setEnabled(!disabled);
        parityOdd.setEnabled(!disabled);
        parityEven.setEnabled(!disabled);
        parityMark.setEnabled(!disabled);
        paritySpace.setEnabled(!disabled);
        enableDTRCheckBox.setEnabled(!disabled);
        enableRTSCheckBox.setEnabled(!disabled);
        eventRXFLAGCheckBox.setEnabled(!disabled);
        eventTXEMPTYCheckBox.setEnabled(!disabled);
        eventCTSCheckBox.setEnabled(!disabled);
        eventDSRCheckBox.setEnabled(!disabled);
        eventRLSDCheckBox.setEnabled(!disabled);
        eventERRCheckBox.setEnabled(!disabled);
        eventRINGCheckBox.setEnabled(!disabled);
        eventBREAKCheckBox.setEnabled(!disabled);
    }

    private void initializeComponents() {
    	final DefaultComboBoxModel<FlowControl> selectFlowControlComboBoxModel;
        
        dataBitsButtonGroup = new ButtonGroup();
        parityButtonGroup = new ButtonGroup();
        stopBitsButtonGroup = new ButtonGroup();

        selectBaudRateComboBoxModel = new DefaultComboBoxModel<>();
        selectBaudRateComboBox = new JComboBox<>(selectBaudRateComboBoxModel);
        selectBaudRateComboBoxLabel = new JLabel("Baud Rate");
        selectBaudRateComboBoxLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        final DefaultComboBoxModel<String> selectComPortComboBoxModel = new DefaultComboBoxModel<>(SerialPortList.getPortNames());
        selectComPortComboBox = new JComboBox<>(selectComPortComboBoxModel);
        selectComPortComboBoxLabel = new JLabel("Com Port");
        selectComPortComboBoxLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        selectFlowControlInComboBoxLabel = new JLabel("Flow Control In");
        selectFlowControlInComboBoxLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        selectFlowControlOutComboBoxLabel = new JLabel("Flow Control Out");
        selectFlowControlOutComboBoxLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        selectDataBitsButtonGroupLabel = new JLabel("Data Bits");
        selectDataBitsButtonGroupLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        selectStopBitsButtonGroupLabel = new JLabel("Stop Bits");
        selectStopBitsButtonGroupLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        selectParityButtonGroupLabel = new JLabel("Parity Bits");
        selectParityButtonGroupLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        enableDTRCheckBox = new JCheckBox("Force Data Terminal Ready (DTR) Line High");
        enableRTSCheckBox = new JCheckBox("Force Ready To Send (RTS) Line High");
        eventRXCHARCheckBox = new JCheckBox("Enable Receive");
        eventRXFLAGCheckBox = new JCheckBox("Enable Receive Flag Events");
        eventTXEMPTYCheckBox = new JCheckBox("Enable Transmit Buffer Empty Detection");
        eventCTSCheckBox = new JCheckBox("Enable Clear to Send Signaling");
        eventDSRCheckBox = new JCheckBox("Enable Data Set Ready Signaling");
        eventRLSDCheckBox = new JCheckBox("Enable Receive Line Signal Detection");
        eventERRCheckBox = new JCheckBox("Enable Error Warnings");
        eventRINGCheckBox = new JCheckBox("Enable Ring Detection");
        eventBREAKCheckBox = new JCheckBox("Enable Break Detection");

        cbReportConfigurationErrors = new JCheckBox("Report Serial Port Configuration Errors");
        cbReportRTSNotSetErrors = new JCheckBox("Report If Ready To Send Line Fails to Set");
        cbReportDTRNotSetErrors = new JCheckBox("Report If Data Terminal Ready Line Fails to Set");
        cbReportFramingErrors = new JCheckBox("Report Framing Errors");
        cbReportParityMismatchErrors = new JCheckBox("Report Parity Mismatch Errors");
        cbReportBufferOverrunErrors = new JCheckBox("Report Buffer Overrun Errors");
        cbReportEventMaskErrors = new JCheckBox("Report Event Mask Configuration Errors");
        cbReportFlowControlErrors = new JCheckBox("Report Flow Control Configuration Errors");
        cbReportPurgeFailures = new JCheckBox("Report Purge Failures");
        cbReportBreakInterrupts = new JCheckBox("Report Break Interrupts");
        cbReportTransmitFailures = new JCheckBox("Report Transmit Failures");
        cbLogSerialPortErrors = new JCheckBox("Log Serial Port Errors");

        selectFlowControlComboBoxModel = new DefaultComboBoxModel<>(FlowControl.values());
        selectFlowControlInComboBox = new JComboBox<>(selectFlowControlComboBoxModel);
        selectFlowControlOutComboBox = new JComboBox<>(selectFlowControlComboBoxModel);

        dataBits5 = new JRadioButton("5");
        dataBits6 = new JRadioButton("6");
        dataBits7 = new JRadioButton("7");
        dataBits8 = new JRadioButton("8");

        dataBitsButtonGroup.add(dataBits5);
        dataBitsButtonGroup.add(dataBits6);
        dataBitsButtonGroup.add(dataBits7);
        dataBitsButtonGroup.add(dataBits8);

        stopBits1 = new JRadioButton("1");
        stopBits15 = new JRadioButton("1.5");
        stopBits2 = new JRadioButton("2");

        stopBitsButtonGroup.add(stopBits1);
        stopBitsButtonGroup.add(stopBits15);
        stopBitsButtonGroup.add(stopBits2);

        parityNone = new JRadioButton("None");
        parityOdd = new JRadioButton("Odd");
        parityEven = new JRadioButton("Even");
        parityMark = new JRadioButton("Mark");
        paritySpace = new JRadioButton("Space");

        parityButtonGroup.add(parityNone);
        parityButtonGroup.add(parityOdd);
        parityButtonGroup.add(parityEven);
        parityButtonGroup.add(parityMark);
        parityButtonGroup.add(paritySpace);
    }

    private void updateComponents() {
        selectFlowControlInComboBox.setSelectedItem(abstractTeletypeController.getFlowControlIn().name());
        selectFlowControlOutComboBox.setSelectedItem(abstractTeletypeController.getFlowControlOut().name());

        for (int i = 0; i < selectComPortComboBox.getItemCount(); i++) {
            selectComPortComboBox.setSelectedIndex(i);
            if (selectComPortComboBox.getItemAt(i).equalsIgnoreCase(abstractTeletypeController.getPortName())) {
                break;
            }
        }

        setAvailableBaudRates(abstractTeletypeController.getSupportedBaudRates(), abstractTeletypeController.getBaudRate());

        final ButtonModel stopBitsModel = switch (abstractTeletypeController.stopBits) {
			case STOPBITS_1 -> stopBits1.getModel();
			case STOPBITS_1_5 -> stopBits15.getModel();
			case STOPBITS_2 -> stopBits2.getModel();
			default -> null;
		};

        stopBitsButtonGroup.setSelected(stopBitsModel, true);

        final ButtonModel dataBitsModel = switch (abstractTeletypeController.dataBits) {
			case DATABITS_5 -> dataBits5.getModel();
			case DATABITS_6 -> dataBits6.getModel();
			case DATABITS_7 -> dataBits7.getModel();
			case DATABITS_8 -> dataBits8.getModel();
			default -> null;
		};

        dataBitsButtonGroup.setSelected(dataBitsModel, true);

        final ButtonModel parityModel = switch (abstractTeletypeController.parity) {
			case NONE -> parityNone.getModel();
			case ODD -> parityOdd.getModel();
			case EVEN -> parityEven.getModel();
			case MARK -> parityMark.getModel();
			case SPACE -> paritySpace.getModel();
			default -> null;
		};

        parityButtonGroup.setSelected(parityModel, true);

        enableDTRCheckBox.setSelected(abstractTeletypeController.isReportDTRNotSetErrors());
        enableRTSCheckBox.setSelected(abstractTeletypeController.isReportRTSNotSetErrors());
        
        eventRXCHARCheckBox.setSelected(abstractTeletypeController.isEventRXCHAR());
        eventRXFLAGCheckBox.setSelected(abstractTeletypeController.isEventRXFLAG());
        eventTXEMPTYCheckBox.setSelected(abstractTeletypeController.isEventTXEMPTY());
        eventCTSCheckBox.setSelected(abstractTeletypeController.isEventCTS());
        eventDSRCheckBox.setSelected(abstractTeletypeController.isEventDSR());
        eventRLSDCheckBox.setSelected(abstractTeletypeController.isEventRLSD());
        eventERRCheckBox.setSelected(abstractTeletypeController.isEventERR());
        eventRINGCheckBox.setSelected(abstractTeletypeController.isEventRING());
        eventBREAKCheckBox.setSelected(abstractTeletypeController.isEventBREAK());

        cbLogSerialPortErrors.setSelected(abstractTeletypeController.logSerialPortErrors);
        cbReportBreakInterrupts.setSelected(abstractTeletypeController.reportBreakInterrupts);
        cbReportBufferOverrunErrors.setSelected(abstractTeletypeController.reportBufferOverrunErrors);
        cbReportConfigurationErrors.setSelected(abstractTeletypeController.reportConfigurationErrors);
        cbReportDTRNotSetErrors.setSelected(abstractTeletypeController.reportDTRNotSetErrors);
        cbReportEventMaskErrors.setSelected(abstractTeletypeController.reportEventMaskErrors);
        cbReportFlowControlErrors.setSelected(abstractTeletypeController.reportFlowControlErrors);
        cbReportFramingErrors.setSelected(abstractTeletypeController.reportFramingErrors);
        cbReportParityMismatchErrors.setSelected(abstractTeletypeController.reportParityMismatchErrors);
        cbReportPurgeFailures.setSelected(abstractTeletypeController.reportPurgeFailures);
        cbReportRTSNotSetErrors.setSelected(abstractTeletypeController.reportRTSNotSetErrors);
        cbReportTransmitFailures.setSelected(abstractTeletypeController.reportTransmitFailures);

        enableSerialInterfaceComponents(abstractTeletypeController.isDeviceAssignedParametersFixed());
        enableSerialBaudRateComponents(abstractTeletypeController.isDeviceAssignedBaudRateFixed());
    }

    private void configureListeners() {
        final RadioButtonHandler rbh = new RadioButtonHandler();

        parityNone.addItemListener(rbh);
        parityOdd.addItemListener(rbh);
        parityEven.addItemListener(rbh);
        parityMark.addItemListener(rbh);
        paritySpace.addItemListener(rbh);
        
        dataBits5.addItemListener(rbh);
        dataBits6.addItemListener(rbh);
        dataBits7.addItemListener(rbh);
        dataBits8.addItemListener(rbh);
        
        stopBits1.addItemListener(rbh);
        stopBits15.addItemListener(rbh);
        stopBits2.addItemListener(rbh);

        selectComPortComboBox.addItemListener((ItemEvent event) -> {
            if (event.getStateChange() == ItemEvent.SELECTED) {
                selectComPortComboBoxActionPerformed(event);
            }
        });

        selectBaudRateComboBox.addItemListener((ItemEvent event) -> {
            if (event.getStateChange() == ItemEvent.SELECTED) {
                selectBaudRateComboBoxActionPerformed(event);
            }
        });

        selectFlowControlInComboBox.addItemListener((ItemEvent event) -> {
            if (event.getStateChange() == ItemEvent.SELECTED) {
            	flowControlInComboBoxActionPerformed(event);
            }
        });

        selectFlowControlOutComboBox.addItemListener((ItemEvent event) -> {
            if (event.getStateChange() == ItemEvent.SELECTED) {
            	flowControlOutComboBoxActionPerformed(event);
            }
        });

        enableDTRCheckBox.addActionListener(this::enableDTRCheckBoxActionPerformed);

        enableRTSCheckBox.addActionListener(this::enableRTSCheckBoxActionPerformed);

        eventRXCHARCheckBox.addActionListener(this::eventRXCHARCheckBoxActionPerformed);

        eventRXFLAGCheckBox.addActionListener(this::eventRXFLAGCheckBoxActionPerformed);

        eventTXEMPTYCheckBox.addActionListener(this::eventTXEMPTYCheckBoxActionPerformed);

        eventCTSCheckBox.addActionListener(this::eventCTSCheckBoxActionPerformed);

        eventDSRCheckBox.addActionListener(this::eventDSRCheckBoxActionPerformed);

        eventRLSDCheckBox.addActionListener(this::eventRLSDCheckBoxActionPerformed);

        eventERRCheckBox.addActionListener(this::eventERRCheckBoxActionPerformed);

        eventRINGCheckBox.addActionListener(this::eventRINGCheckBoxActionPerformed);

        eventBREAKCheckBox.addActionListener(this::eventBREAKCheckBoxActionPerformed);

        cbReportEventMaskErrors.addActionListener(this::cbReportEventMaskErrorsActionPerformed);

        cbReportFlowControlErrors.addActionListener(this::cbReportFlowControlErrorsActionPerformed);

        cbReportPurgeFailures.addActionListener(this::cbReportPurgeFailuresActionPerformed);

        cbReportTransmitFailures.addActionListener(this::cbReportTransmitFailuresActionPerformed);

        cbReportBreakInterrupts.addActionListener(this::cbReportBreakInterruptsActionPerformed);

        cbReportConfigurationErrors.addActionListener(this::cbReportConfigurationErrorsActionPerformed);

        cbReportRTSNotSetErrors.addActionListener(this::cbReportRTSNotSetErrorsActionPerformed);

        cbReportDTRNotSetErrors.addActionListener(this::cbReportDTRNotSetErrorsActionPerformed);

        cbReportFramingErrors.addActionListener(this::cbReportFramingErrorsActionPerformed);

        cbReportParityMismatchErrors.addActionListener(this::cbReportParityMismatchErrorsActionPerformed);

        cbReportBufferOverrunErrors.addActionListener(this::cbReportBufferOverrunErrorsActionPerformed);

        cbLogSerialPortErrors.addActionListener(this::cbLogSerialPortErrorsActionPerformed);
    }

    private void enableRTSCheckBoxActionPerformed(ActionEvent event) {
        abstractTeletypeController.setRTS(((JCheckBox) event.getSource()).isSelected());
    }

    private void enableDTRCheckBoxActionPerformed(ActionEvent event) {
    	abstractTeletypeController.setDTR(((JCheckBox) event.getSource()).isSelected());
    }

    private void cbReportBufferOverrunErrorsActionPerformed(ActionEvent event) {
        abstractTeletypeController.reportBufferOverrunErrors = ((JCheckBox) event.getSource()).isSelected();
    }

    private void cbReportParityMismatchErrorsActionPerformed(ActionEvent event) {
        abstractTeletypeController.reportParityMismatchErrors = ((JCheckBox) event.getSource()).isSelected();
    }

    private void cbReportFramingErrorsActionPerformed(ActionEvent event) {
        abstractTeletypeController.reportFramingErrors = ((JCheckBox) event.getSource()).isSelected();
    }

    private void cbReportDTRNotSetErrorsActionPerformed(ActionEvent event) {
        abstractTeletypeController.reportDTRNotSetErrors = ((JCheckBox) event.getSource()).isSelected();
    }

    private void cbReportRTSNotSetErrorsActionPerformed(ActionEvent event) {
        abstractTeletypeController.reportRTSNotSetErrors = ((JCheckBox) event.getSource()).isSelected();
    }

    private void cbReportConfigurationErrorsActionPerformed(ActionEvent event) {
        abstractTeletypeController.reportConfigurationErrors = ((JCheckBox) event.getSource()).isSelected();
    }

    private void cbReportBreakInterruptsActionPerformed(ActionEvent event) {
        abstractTeletypeController.reportBreakInterrupts = ((JCheckBox) event.getSource()).isSelected();
    }

    private void cbReportTransmitFailuresActionPerformed(ActionEvent event) {
        abstractTeletypeController.reportTransmitFailures = ((JCheckBox) event.getSource()).isSelected();
    }

    private void cbReportPurgeFailuresActionPerformed(ActionEvent event) {
        abstractTeletypeController.reportPurgeFailures = ((JCheckBox) event.getSource()).isSelected();
    }

    private void cbReportFlowControlErrorsActionPerformed(ActionEvent event) {
        abstractTeletypeController.reportFlowControlErrors = ((JCheckBox) event.getSource()).isSelected();
    }

    private void cbReportEventMaskErrorsActionPerformed(ActionEvent event) {
        abstractTeletypeController.reportEventMaskErrors = ((JCheckBox) event.getSource()).isSelected();
    }

    private void cbLogSerialPortErrorsActionPerformed(ActionEvent event) {
        abstractTeletypeController.logSerialPortErrors = ((JCheckBox) event.getSource()).isSelected();
    }

    private void eventBREAKCheckBoxActionPerformed(ActionEvent event) {
        abstractTeletypeController.setEventBREAK(((JCheckBox) event.getSource()).isSelected());
    }

    private void eventRINGCheckBoxActionPerformed(ActionEvent event) {
        abstractTeletypeController.setEventRING(((JCheckBox) event.getSource()).isSelected());
    }

    private void eventERRCheckBoxActionPerformed(ActionEvent event) {
        abstractTeletypeController.setEventERR(((JCheckBox) event.getSource()).isSelected());
    }

    private void eventRLSDCheckBoxActionPerformed(ActionEvent event) {
        abstractTeletypeController.setEventRLSD(((JCheckBox) event.getSource()).isSelected());
    }

    private void eventDSRCheckBoxActionPerformed(ActionEvent event) {
        abstractTeletypeController.setEventDSR(((JCheckBox) event.getSource()).isSelected());
    }

    private void eventCTSCheckBoxActionPerformed(ActionEvent event) {
        abstractTeletypeController.setEventCTS(((JCheckBox) event.getSource()).isSelected());
    }

    private void eventTXEMPTYCheckBoxActionPerformed(ActionEvent event) {
        abstractTeletypeController.setEventTXEMPTY(((JCheckBox) event.getSource()).isSelected());
    }

    private void eventRXFLAGCheckBoxActionPerformed(ActionEvent event) {
        abstractTeletypeController.setEventRXFLAG(((JCheckBox) event.getSource()).isSelected());
    }

    private void eventRXCHARCheckBoxActionPerformed(ActionEvent event) {
        abstractTeletypeController.setEventRXCHAR(((JCheckBox) event.getSource()).isSelected());
    }
    private class RadioButtonHandler implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent ie) {
            if (ie.getSource().equals(dataBits5)) {
                abstractTeletypeController.dataBits = DataBits.DATABITS_5;
            } else if (ie.getSource().equals(dataBits6)) {
                abstractTeletypeController.dataBits = DataBits.DATABITS_6;
            } else if (ie.getSource().equals(dataBits7)) {
                abstractTeletypeController.dataBits = DataBits.DATABITS_7;
            } else if (ie.getSource().equals(dataBits8)) {
                abstractTeletypeController.dataBits = DataBits.DATABITS_8;
            } else if (ie.getSource().equals(stopBits1)) {
                abstractTeletypeController.stopBits = StopBits.STOPBITS_1;
            } else if (ie.getSource().equals(stopBits15)) {
                abstractTeletypeController.stopBits = StopBits.STOPBITS_1_5;
            } else if (ie.getSource().equals(stopBits2)) {
                abstractTeletypeController.stopBits = StopBits.STOPBITS_2;
            } else if (ie.getSource().equals(parityNone)) {
                abstractTeletypeController.parity = Parity.NONE;
            } else if (ie.getSource().equals(parityOdd)) {
                abstractTeletypeController.parity = Parity.ODD;
            } else if (ie.getSource().equals(parityEven)) {
                abstractTeletypeController.parity = Parity.EVEN;
            } else if (ie.getSource().equals(parityMark)) {
                abstractTeletypeController.parity = Parity.MARK;
            } else if (ie.getSource().equals(paritySpace)) {
                abstractTeletypeController.parity = Parity.SPACE;
            }
        }
    }

    private void selectComPortComboBoxActionPerformed(ItemEvent event) {
        final JComboBox<?> cb = (JComboBox<?>) event.getSource();
        if (!abstractTeletypeController.setPortName((String) cb.getSelectedItem())) {
        	JOptionPane.showMessageDialog(null,
				"Error opening " + (String) cb.getSelectedItem() + "\n\n" + "The configured tty port is not available.\n",
				"Port Not Available", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void selectBaudRateComboBoxActionPerformed(ItemEvent event) {
        final JComboBox<?> cb = (JComboBox<?>) event.getSource();
        abstractTeletypeController.baudRate = AbstractTeletypeController.getBaudRateFromInteger(Integer.parseInt(cb.getSelectedItem().toString()));
    }

    private void flowControlInComboBoxActionPerformed(ItemEvent event) {
        final JComboBox<?> cb = (JComboBox<?>) event.getSource();
        abstractTeletypeController.flowControlIn = (FlowControl) cb.getSelectedItem();
    }

    private void flowControlOutComboBoxActionPerformed(ItemEvent event) {
        final JComboBox<?> cb = (JComboBox<?>) event.getSource();
        abstractTeletypeController.flowControlOut = (FlowControl) cb.getSelectedItem();
    }

    private void setAvailableBaudRates(BaudRate[] supportedBaudRates, BaudRate selectedBaudRate) {
        selectBaudRateComboBoxModel.removeAllElements();
        selectBaudRateComboBox.validate();
        for (final BaudRate br : supportedBaudRates) {
            selectBaudRateComboBoxModel.addElement(AbstractTeletypeController.getIntegerFromBaudRate(br));
        }
        selectBaudRateComboBox.setSelectedItem(AbstractTeletypeController.getIntegerFromBaudRate(selectedBaudRate));
    }
    
    public JPanel[] getSettingsPanelArray(String titlePrefix) {
    	return new JPanel[] {getSerialConfigGui(titlePrefix), getErrorNotificationGui(titlePrefix), getEventManagementGui(titlePrefix)};
    }
    
    public JPanel getSerialConfigGui(String titlePrefix) {
        final JPanel panel = new JPanel();
        panel.setName(titlePrefix + " TTY Port Settings");
        final GroupLayout layout = new GroupLayout(panel);

        panel.setLayout(layout);

        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                        .addGap(20, 20, 20)
                                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                                .addComponent(selectComPortComboBoxLabel)
                                                .addComponent(selectBaudRateComboBoxLabel))
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                .addComponent(selectComPortComboBox, 120, 120, 120)
                                                .addComponent(selectBaudRateComboBox, 90, 90, 90))
                                        .addGap(20, 20, 20)
                                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                                .addGroup(layout.createSequentialGroup()
                                                        .addComponent(selectFlowControlOutComboBoxLabel)
                                                        .addComponent(selectFlowControlOutComboBox, 100, 100, 100))
                                                .addGroup(layout.createSequentialGroup()
                                                        .addComponent(selectFlowControlInComboBoxLabel)
                                                        .addComponent(selectFlowControlInComboBox, 100, 100, 100))))
                                .addGroup(layout.createSequentialGroup()
                                        .addContainerGap()
                                        .addComponent(selectDataBitsButtonGroupLabel, 80, 80, 80)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(dataBits5)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(dataBits6)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(dataBits7)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(dataBits8))
                                .addGroup(layout.createSequentialGroup()
                                        .addContainerGap()
                                        .addComponent(selectStopBitsButtonGroupLabel, 80, 80, 80)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(stopBits1)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(stopBits15)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(stopBits2))
                                .addGroup(layout.createSequentialGroup()
                                        .addContainerGap()
                                        .addComponent(selectParityButtonGroupLabel, 80, 80, 80)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(parityNone)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(parityOdd)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(parityEven)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(parityMark)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(paritySpace)))
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(selectComPortComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(selectComPortComboBoxLabel)
                                .addComponent(selectFlowControlInComboBoxLabel)
                                .addComponent(selectFlowControlInComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(selectBaudRateComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(selectBaudRateComboBoxLabel)
                                .addComponent(selectFlowControlOutComboBoxLabel)
                                .addComponent(selectFlowControlOutComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addGap(5, 5, 5)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(selectDataBitsButtonGroupLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(dataBits5)
                                .addComponent(dataBits6)
                                .addComponent(dataBits7)
                                .addComponent(dataBits8))
                        .addGap(5, 5, 5)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(selectStopBitsButtonGroupLabel)
                                .addComponent(stopBits1)
                                .addComponent(stopBits15)
                                .addComponent(stopBits2))
                        .addGap(5, 5, 5)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(selectParityButtonGroupLabel)
                                .addComponent(parityNone)
                                .addComponent(parityOdd)
                                .addComponent(parityEven)
                                .addComponent(parityMark)
                                .addComponent(paritySpace))
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        return panel;
    }

    public JPanel getEventManagementGui(String titlePrefix) {
        final JPanel panel = new JPanel();
        panel.setName(titlePrefix + " TTY Event Management");
        final GroupLayout layout = new GroupLayout(panel);

        panel.setLayout(layout);

        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addGap(28, 28, 28)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(eventRXCHARCheckBox)
                                .addComponent(eventRXFLAGCheckBox)
                                .addComponent(eventBREAKCheckBox)
                                .addComponent(eventCTSCheckBox)
                                .addComponent(eventDSRCheckBox))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(eventRINGCheckBox)
                                .addComponent(eventRLSDCheckBox)
                                .addComponent(eventTXEMPTYCheckBox)
                                .addComponent(eventERRCheckBox))
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addGap(33, 33, 33)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(eventRINGCheckBox)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(eventRLSDCheckBox)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(eventTXEMPTYCheckBox)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(eventERRCheckBox))
                                .addGap(64, 64, 64)
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(eventRXCHARCheckBox)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(eventRXFLAGCheckBox)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(eventBREAKCheckBox)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(eventCTSCheckBox)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(eventDSRCheckBox)))
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        return panel;
    }

    public JPanel getErrorNotificationGui(String titlePrefix) {
        final JPanel panel = new JPanel();
        panel.setName(titlePrefix + " TTY Error Notification Settings");
        final GroupLayout layout = new GroupLayout(panel);

        panel.setLayout(layout);

        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addGap(28, 28, 28)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(cbReportFramingErrors)
                                .addComponent(cbReportConfigurationErrors)
                                .addComponent(cbReportBufferOverrunErrors)
                                .addComponent(cbReportParityMismatchErrors)
                                .addComponent(cbReportDTRNotSetErrors)
                                .addComponent(cbReportRTSNotSetErrors)
                                .addComponent(cbReportEventMaskErrors))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(cbReportFlowControlErrors)
                                .addComponent(cbReportPurgeFailures)
                                .addComponent(cbReportBreakInterrupts)
                                .addComponent(cbReportTransmitFailures)
                                .addComponent(cbLogSerialPortErrors))
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(cbReportFlowControlErrors)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(cbReportPurgeFailures)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(cbReportBreakInterrupts)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(cbReportTransmitFailures)
                                        .addGap(64, 64, 64)
                                        .addComponent(cbLogSerialPortErrors))
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(cbReportFramingErrors)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(cbReportConfigurationErrors)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(cbReportBufferOverrunErrors)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(cbReportParityMismatchErrors)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(cbReportDTRNotSetErrors)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(cbReportRTSNotSetErrors)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(cbReportEventMaskErrors)))
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        return panel;
    }

}
