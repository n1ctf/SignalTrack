package aprs;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;

import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import javax.swing.text.PlainDocument;

import utility.IntegerFilter;

public class APRSComponent extends JDialog {

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_FONT = "Calabri";
	private JTabbedPane tabbedPane;
	
	private JButton okButton;
	private JButton cancelButton;
	private JButton applyButton;
	
	private JFormattedTextField ftfAprsSymbolSize;
	private JFormattedTextField ftfTimeToLive;
	private JFormattedTextField ftfTimeToGoStale;
	
	private JLabel timeToLiveLabel;
	private JLabel timeToGoStaleLabel;
	private JLabel aprsSymbolSizeLabel;
	
	private JCheckBox cbStartAprsWithSystem;
	private JCheckBox cbEnableAprsTracking;
	private JCheckBox cbReportCRCErrors;
	private JCheckBox cbTimeToLiveEnabled;
	private JCheckBox cbTimeToGoStaleEnabled;
	private JCheckBox cbReportTNC;
	private JCheckBox cbReportIS;
	private JCheckBox cbReportCWOP;
	private JCheckBox cbReportWUG;
	private JCheckBox cbReportRadMon;
	
	private final transient AprsProcessor aprsProcessor;

	public APRSComponent(AprsProcessor aprsProcessor) {
		this.aprsProcessor = aprsProcessor;
		
		setVisible(false);

		initializeComponents();
		setComponentValues();
		drawGraphicalUserInterface();
		configureListeners();

		setVisible(true);
	}
	
	private void configureListeners() {
		okButton.addActionListener(this::okButtonActionListenerEvent);

		cancelButton.addActionListener(this::cancelButtonActionListenerEvent);

		applyButton.addActionListener(this::applyButtonActionListenerEvent);

		ftfAprsSymbolSize.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				ftfAprsSymbolSize.setFont(new Font(DEFAULT_FONT, Font.BOLD, 11));
			}

			@Override
			public void focusLost(FocusEvent e) {
				// NOOP
			}
		});

		ftfAprsSymbolSize.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent event) {
				// NOOP
			}

			@Override
			public void keyPressed(KeyEvent event) {
				if (event.getKeyCode() == KeyEvent.VK_ENTER) {
					ftfAprsSymbolSize.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 11));
					ftfAprsSymbolSize.transferFocus();
				}
			}

			@Override
			public void keyReleased(KeyEvent event) {
				// NOOP
			}
		});

		ftfTimeToLive.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				ftfTimeToLive.setFont(new Font(DEFAULT_FONT, Font.BOLD, 11));
			}

			@Override
			public void focusLost(FocusEvent e) {
				// NOOP
			}
		});

		ftfTimeToLive.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent event) {
				// NOOP
			}

			@Override
			public void keyPressed(KeyEvent event) {
				if (event.getKeyCode() == KeyEvent.VK_ENTER) {
					ftfTimeToLive.setFont(new Font("Calabri", Font.PLAIN, 11));
					ftfTimeToLive.transferFocus();
				}
			}

			@Override
			public void keyReleased(KeyEvent event) {
				// NOOP
			}
		});

		ftfTimeToGoStale.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				ftfTimeToGoStale.setFont(new Font("Calabri", Font.BOLD, 11));
			}

			@Override
			public void focusLost(FocusEvent e) {
				// NOOP
			}
		});

		ftfTimeToGoStale.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent event) {
				// NOOP
			}

			@Override
			public void keyPressed(KeyEvent event) {
				if (event.getKeyCode() == KeyEvent.VK_ENTER) {
					ftfTimeToGoStale.setFont(new Font("Calabri", Font.PLAIN, 11));
					ftfTimeToGoStale.transferFocus();
				}
			}

			@Override
			public void keyReleased(KeyEvent event) {
				// NOOP
			}
		});
	}

	private void setComponentValues() {
		ftfAprsSymbolSize.setText(String.valueOf(aprsProcessor.getSymbolSize()));
		
		cbStartAprsWithSystem.setSelected(aprsProcessor.isStartAPRSWithSystem());
		cbEnableAprsTracking.setSelected(aprsProcessor.isEnableAPRSTracking());
		cbReportCRCErrors.setSelected(aprsProcessor.isReportCRCErrors());
		cbTimeToLiveEnabled.setSelected(aprsProcessor.isTimeToLiveEnabled());
		cbTimeToGoStaleEnabled.setSelected(aprsProcessor.isTimeToGoStaleEnabled());
		
		if (aprsProcessor.getTimeToLiveMinutes() < Integer.MAX_VALUE) {
			ftfTimeToLive.setText(String.valueOf(aprsProcessor.getTimeToLiveMinutes()));
		} else {
			ftfTimeToLive.setText("");
		}
		
		if (aprsProcessor.getTimeToGoStaleMinutes() < Integer.MAX_VALUE) {
			ftfTimeToGoStale.setText(String.valueOf(aprsProcessor.getTimeToGoStaleMinutes()));
		} else {
			ftfTimeToGoStale.setText("");
		}

		cbReportTNC.setSelected(aprsProcessor.getAPRSTNCClient().isReportEnabled());
		cbReportIS.setSelected(aprsProcessor.getAPRSISClient().isReportEnabled());
		cbReportRadMon.setSelected(aprsProcessor.getRadMonAPI().isReportEnabled());
		cbReportCWOP.setSelected(aprsProcessor.getCWOPUpdater().isReportEnabled());
		cbReportWUG.setSelected(aprsProcessor.getWUGUpdater().isReportEnabled());
		
		tabbedPane.removeAll();
		
		tabbedPane.addTab("Map Viewer Settings", null, getAprsViewerSettingsPanel(), null);
		tabbedPane.addTab("Reporting Settings", null, getWeatherReportingSettingsPanel(), null);
		tabbedPane.addTab("APRS TNC Settings", null, aprsProcessor.getAPRSTNCClient().getSettingsPanel(), null);		
		tabbedPane.addTab("APRS IS Settings", null, aprsProcessor.getAPRSISClient().getSettingsPanel());
		tabbedPane.addTab("CWOP Reporting Settings", null, aprsProcessor.getCWOPUpdater().getSettingsPanel());
		tabbedPane.addTab("WUG Reporting Settings", null, aprsProcessor.getWUGUpdater().getSettingsPanel());
		tabbedPane.addTab("RadMon Reporting Settings", null, aprsProcessor.getRadMonAPI().getSettingsPanel());
	}

	private void initializeComponents() {
		setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		setTitle("APRS Settings");
		
		tabbedPane = new JTabbedPane();

		timeToLiveLabel = new JLabel("Minutes Without Update");
		timeToLiveLabel.setHorizontalAlignment(SwingConstants.LEFT);

		timeToGoStaleLabel = new JLabel("Minutes Without Update");
		timeToGoStaleLabel.setHorizontalAlignment(SwingConstants.LEFT);

		cbReportTNC = new JCheckBox("Report Weather Information Using TNC");

		cbReportIS = new JCheckBox("Report Weather Information Over Internet");
		
		cbReportCWOP = new JCheckBox("Report Weather Information to CWOP");
		
		cbReportWUG = new JCheckBox("Report Weather Information to Weather Underground");
		
		cbReportRadMon = new JCheckBox("Report Radiation Level to RadMon");

		cbStartAprsWithSystem = new JCheckBox("Start APRS With System");
		cbEnableAprsTracking = new JCheckBox("Enable APRS Tracking");
		cbReportCRCErrors = new JCheckBox("Report APRS Circular Redundancy Check Failures");
		cbTimeToLiveEnabled = new JCheckBox("Enable Delete Icon After ");
		cbTimeToGoStaleEnabled = new JCheckBox("Enable Strike Out Icon After ");

		okButton = new JButton("OK");
		okButton.setMultiClickThreshhold(50L);

		cancelButton = new JButton("Cancel");
		cancelButton.setMultiClickThreshhold(50L);

		applyButton = new JButton("Apply");
		applyButton.setMultiClickThreshhold(50L);

		aprsSymbolSizeLabel = new JLabel("APRS Symbol Size (pixels)");
		aprsSymbolSizeLabel.setHorizontalAlignment(SwingConstants.RIGHT);

		ftfAprsSymbolSize = new JFormattedTextField();
		ftfAprsSymbolSize.setHorizontalAlignment(SwingConstants.CENTER);
		ftfAprsSymbolSize.setFont(new Font("Calabri", Font.PLAIN, 11));
		ftfAprsSymbolSize.setBackground(Color.WHITE);
		ftfAprsSymbolSize.setForeground(Color.BLACK);

		applyIntegerFilter(ftfAprsSymbolSize);

		ftfTimeToLive = new JFormattedTextField();
		ftfTimeToLive.setHorizontalAlignment(SwingConstants.CENTER);
		ftfTimeToLive.setFont(new Font("Calabri", Font.PLAIN, 11));
		ftfTimeToLive.setBackground(Color.WHITE);
		ftfTimeToLive.setForeground(Color.BLACK);

		applyIntegerFilter(ftfTimeToLive);

		ftfTimeToGoStale = new JFormattedTextField();
		ftfTimeToGoStale.setHorizontalAlignment(SwingConstants.CENTER);
		ftfTimeToGoStale.setFont(new Font("Calabri", Font.PLAIN, 11));
		ftfTimeToGoStale.setBackground(Color.WHITE);
		ftfTimeToGoStale.setForeground(Color.BLACK);

		applyIntegerFilter(ftfTimeToGoStale);
	}
	
	public AprsProcessor getAPRS() {
		return aprsProcessor;
	}

	private void applyIntegerFilter(JFormattedTextField jftf) {
		final PlainDocument pDoc = (PlainDocument) jftf.getDocument();
		pDoc.setDocumentFilter(new IntegerFilter());
	}

	private void applyButtonActionListenerEvent(ActionEvent event) {
		aprsProcessor.setReportCRCErrors(cbReportCRCErrors.isSelected());
		aprsProcessor.setAprsSymbolSize(Integer.parseInt(ftfAprsSymbolSize.getText()));
		aprsProcessor.setStartAPRSWithSystem(cbStartAprsWithSystem.isSelected());
		aprsProcessor.setEnableAPRSTracking(cbEnableAprsTracking.isSelected());
		aprsProcessor.setTimeToGoStaleEnabled(cbTimeToGoStaleEnabled.isSelected());
		
		if (cbTimeToGoStaleEnabled.isSelected()) {
			if (ftfTimeToGoStale.getText().isBlank()) {
				ftfTimeToGoStale.setText("60");
			}
			aprsProcessor.setTimeToGoStaleMinutes(Integer.parseInt(ftfTimeToGoStale.getText()));
		}
		
		aprsProcessor.setTimeToLiveEnabled(cbTimeToLiveEnabled.isSelected());
		
		if (cbTimeToLiveEnabled.isSelected()) {
			if (ftfTimeToLive.getText().isBlank()) {
				ftfTimeToLive.setText("60");
			}
			aprsProcessor.setTimeToLiveMinutes(Integer.parseInt(ftfTimeToLive.getText()));
		}	
		
		aprsProcessor.getAPRSTNCClient().runService(cbReportTNC.isSelected());
		aprsProcessor.getAPRSISClient().runService(cbReportIS.isSelected());
		aprsProcessor.getRadMonAPI().runService(cbReportRadMon.isSelected());
		aprsProcessor.getCWOPUpdater().runService(cbReportCWOP.isSelected());
		aprsProcessor.getWUGUpdater().runService(cbReportWUG.isSelected());

		aprsProcessor.savePreferences();
	}

	private void okButtonActionListenerEvent(ActionEvent event) {
		applyButton.doClick();
		dispose();
	}

	private void cancelButtonActionListenerEvent(ActionEvent event) {
		dispose();
	}

	private JPanel getAprsViewerSettingsPanel() {
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
                        .addComponent(aprsSymbolSizeLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ftfAprsSymbolSize, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE))
                    .addComponent(cbEnableAprsTracking)
                    .addComponent(cbStartAprsWithSystem)
                    .addComponent(cbReportCRCErrors)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(cbTimeToLiveEnabled)
                            .addComponent(cbTimeToGoStaleEnabled))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(ftfTimeToLive, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                            .addComponent(ftfTimeToGoStale, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(timeToLiveLabel)
                            .addComponent(timeToGoStaleLabel))))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        
            layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(cbEnableAprsTracking)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(cbStartAprsWithSystem)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(cbReportCRCErrors)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(cbTimeToLiveEnabled)
                        .addComponent(ftfTimeToLive, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(timeToLiveLabel))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(cbTimeToGoStaleEnabled)
                        .addComponent(ftfTimeToGoStale, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(timeToGoStaleLabel))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(aprsSymbolSizeLabel)
                        .addComponent(ftfAprsSymbolSize, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		return panel;
	}

	private JPanel getWeatherReportingSettingsPanel() {
		final JPanel panel = new JPanel();

		final GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
	            .addGroup(layout.createSequentialGroup()
	                .addContainerGap(60, 60)
	                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
	                    .addComponent(cbReportTNC)
	                    .addComponent(cbReportIS)
	                    .addComponent(cbReportCWOP)
	                    .addComponent(cbReportWUG)
	                    .addComponent(cbReportRadMon))
	                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		
	        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
	            .addGroup(layout.createSequentialGroup()
	                .addContainerGap(40, 40)
	                .addComponent(cbReportTNC)
	                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
	                .addComponent(cbReportIS)
	                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
	                .addComponent(cbReportCWOP)
	                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
	                .addComponent(cbReportWUG)
	                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
	                .addComponent(cbReportRadMon)
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
						.addComponent(tabbedPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGroup(layout.createSequentialGroup()
						.addComponent(okButton, 90, 90, 90)
						.addComponent(applyButton, 90, 90, 90)
						.addComponent(cancelButton, 90, 90, 90)))
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
				.addComponent(tabbedPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
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
	}
}
