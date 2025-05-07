package coverage;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.text.NumberFormatter;

public class StaticTestComponent extends JDialog {
	private static final long serialVersionUID = 1L;
	
	private JPanel staticSignalLocationSettingsPanel;
    private JPanel flightColoringPanel;
	private JTabbedPane tabbedPane;
	
	private JButton okButton;
	private JButton cancelButton;
	private JButton applyButton;
	
	private JLabel maxAllowedDeviationLabel;
	private JLabel cursorDiameterLabel;
	private JLabel traceDiameterLabel;
	private JLabel intersectPointDiameterLabel;
	
	private JFormattedTextField tfMaxAllowedDeviation;
    private JFormattedTextField tfCursorDiameter;
    private JFormattedTextField tfTraceDiameter;
    private JFormattedTextField tfIntersectPointSize;
    
	private JCheckBox cbShowFlightPaths;
	private JCheckBox cbShowAsymptotes;
	private JCheckBox cbShowCursors;
	private JCheckBox cbShowTraces;
	private JCheckBox cbTraceEqualsFlightColor;
	private JCheckBox cbShowTargetRing;
	private JCheckBox cbShowIntersectPoints;

	private JTextField tfAsymptoteColor;
	private JTextField tfTraceColor;
	private JTextField tfCursorColor;
	private JTextField tfTargetRingColor;
	private JTextField tfIntersectPointColor;
	
	private final JTextField[] tfFlight = new JTextField[14];

    private final JLabel[] flightLabel = new JLabel[14];
    
    private JColorChooser jcc;
	private JDialog jccDialog;
	private JButton jccApply;
	private JButton jccCancel;
	private int colorIndex;
	
	private String testName;
	private transient StaticTestObject sto;
	
	public StaticTestComponent(StaticTestObject sto) {
		this.sto = sto;
		initializeComponents();
		drawGraphicalUserInterface();
		setVisible(true);
	}

	private void initializeComponents() {
		setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		
		jccDialog = new JDialog();
		jccApply = new JButton("Apply");
		jccCancel = new JButton("Cancel");
		
		jcc = new JColorChooser(Color.RED);
		jcc.setPreviewPanel(new JPanel());
		
		final AbstractColorChooserPanel[] oldPanels = jcc.getChooserPanels();
	    
		for (final AbstractColorChooserPanel oldPanel : oldPanels) {
	    	final String clsName = oldPanel.getClass().getName();
	    	if (clsName.equals("javax.swing.colorchooser.ColorChooserPanel")) {
				jcc.removeChooserPanel(oldPanel);
			}
	    }
	    
		jccDialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		jccDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		cbShowFlightPaths = new JCheckBox();
		cbShowAsymptotes = new JCheckBox();
		cbShowTraces = new JCheckBox();
		cbTraceEqualsFlightColor = new JCheckBox();
		cbShowCursors = new JCheckBox();
		cbShowTargetRing = new JCheckBox();
		cbShowIntersectPoints = new JCheckBox();

        tfAsymptoteColor = new JTextField();
        tfAsymptoteColor.setEditable(false);
        tfAsymptoteColor.setBackground(sto.getAsymptoteColor());
        tfAsymptoteColor.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        
        tfTraceColor = new JTextField();
        tfTraceColor.setEditable(false);
        
        if (sto.isTraceEqualsFlightColor()) {
        	tfTraceColor.setBackground(Color.LIGHT_GRAY);
        } else {
        	tfTraceColor.setBackground(sto.getTraceColor());
        }
        
        tfTraceColor.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        
        tfCursorColor = new JTextField();
        tfCursorColor.setEditable(false);
        tfCursorColor.setBackground(sto.getCursorColor());
        tfCursorColor.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        
        tfTargetRingColor = new JTextField();
        tfTargetRingColor.setEditable(false);
        tfTargetRingColor.setBackground(sto.getTargetRingColor());
        tfTargetRingColor.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        
        tfIntersectPointColor = new JTextField();
        tfIntersectPointColor.setEditable(false);
        tfIntersectPointColor.setBackground(sto.getIntersectPointColor());
        tfIntersectPointColor.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

        for (int i = 0; i < tfFlight.length; i++) {
        	flightLabel[i] = new JLabel();
        	flightLabel[i].setText("Flight " + (i + 1));
        	tfFlight[i] = new JTextField();
        	tfFlight[i].setBackground(sto.getFlightColor()[i]);
        	tfFlight[i].setEditable(false);
        	tfFlight[i].setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
		
		okButton = new JButton("OK");
        okButton.setMultiClickThreshhold(50L);

        cancelButton = new JButton("Cancel");
        cancelButton.setMultiClickThreshhold(50L);

        applyButton = new JButton("Apply");
        applyButton.setMultiClickThreshhold(50L);
		
		setTitle("Static Signal Location Analysis");

		tabbedPane = new JTabbedPane();		
		
		maxAllowedDeviationLabel = new JLabel();
		maxAllowedDeviationLabel.setText("Angle to Target  ");
		
		cursorDiameterLabel = new JLabel("Cursor Diameter (pixels) ");
		cursorDiameterLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		
		traceDiameterLabel = new JLabel("Trace Diameter (pixels) ");
		traceDiameterLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		
		intersectPointDiameterLabel = new JLabel("Intersect Point Width (pixels) ");
		intersectPointDiameterLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		
		cbShowFlightPaths.setSelected(sto.isShowFlightPaths());
		cbShowAsymptotes.setSelected(sto.isShowAsymptotes());
		cbShowTraces.setSelected(sto.isShowTraces());
		cbTraceEqualsFlightColor.setSelected(sto.isTraceEqualsFlightColor());
		cbShowCursors.setSelected(sto.isShowCursors());
		cbShowTargetRing.setSelected(sto.isShowTargetRing());
		cbShowIntersectPoints.setSelected(sto.isShowIntersectPoints());
		
		staticSignalLocationSettingsPanel = new JPanel();
		staticSignalLocationSettingsPanel.setBorder(BorderFactory.createEtchedBorder());
		
		flightColoringPanel = new JPanel();
		flightColoringPanel.setBorder(BorderFactory.createTitledBorder("Flight Path Colors"));
		
		cbShowFlightPaths.setText("Enable Flight Decorations  ");
		cbShowAsymptotes.setText("Show Asymptotes  ");
		cbShowCursors.setText("Show Cursors  ");
		cbShowTraces.setText("Show Flight Traces  ");
		cbTraceEqualsFlightColor.setText("Trace Color = Flight Color  ");
		cbShowTargetRing.setText("Show Target Ring  ");
		cbShowIntersectPoints.setText("Show Intersect Points  ");
		
		tabbedPane.addTab(" Flight Path Settings ", null, staticSignalLocationSettingsPanel, null);
		
		okButton.addActionListener(_ -> {
			applyButton.doClick();
			setVisible(false);
		});

		cancelButton.addActionListener(_ -> setVisible(false));

		applyButton.addActionListener(_ -> applyButtonActionListenerEvent());
		
		for (int i = 0; i < tfFlight.length; i++) {
			final int q = i;
			tfFlight[i].addMouseListener(new MouseAdapter() {
	            @Override
	            public void mouseClicked(MouseEvent event) {
	                setjccDialog(q);
	            }
	        });
		}
		
		tfAsymptoteColor.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                tfAsymptoteColorMouseClicked();
            }
        });
		
		tfCursorColor.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                tfCursorColorMouseClicked();
            }
        });
		
		tfIntersectPointColor.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                tfIntersectPointColorMouseClicked();
            }
        });
		
		tfTargetRingColor.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                tfTargetRingColorMouseClicked();
            }
        });
		
		tfTraceColor.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                tfTraceColorMouseClicked();
            }
        });
		
		cbShowFlightPaths.addItemListener(this::cbShowFlightPathsCheckBoxItemStateChanged);

		cbShowAsymptotes.addItemListener(this::cbShowAsymptotesCheckBoxItemStateChanged);

		cbShowCursors.addItemListener(this::cbShowCursorsCheckBoxItemStateChanged);

		cbShowTraces.addItemListener(this::cbShowTraceCheckBoxItemStateChanged);

		cbTraceEqualsFlightColor.addItemListener(this::cbTraceEqualsFlightColorCheckBoxItemStateChanged);

		cbShowTargetRing.addItemListener(this::cbShowTargetRingCheckBoxItemStateChanged);

		cbShowIntersectPoints.addItemListener(this::cbShowIntersectPointsCheckBoxItemStateChanged);

		final NumberFormat integerFormat = NumberFormat.getIntegerInstance();
		final NumberFormatter numberFormatter = new NumberFormatter(integerFormat);
		final DecimalFormat twoDigitIntegerFormat = new DecimalFormat("#0");
		numberFormatter.setValueClass(Integer.class);
		numberFormatter.setAllowsInvalid(true);
		numberFormatter.setMinimum(2);
		numberFormatter.setMaximum(99);
		numberFormatter.setCommitsOnValidEdit(false);

		tfIntersectPointSize = new JFormattedTextField(numberFormatter);
		tfIntersectPointSize.setText(twoDigitIntegerFormat.format(sto.getIntersectPointDiameter()));
		tfIntersectPointSize.setHorizontalAlignment(SwingConstants.CENTER);
		tfIntersectPointSize.setFont(new Font("Calabri", Font.PLAIN, 11));
		tfIntersectPointSize.setBackground(Color.WHITE);
		tfIntersectPointSize.setForeground(Color.BLACK);
		
		tfIntersectPointSize.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				tfIntersectPointSize.setFont(new Font("Calabri", Font.BOLD, 11));
			}
			@Override
			public void focusLost(FocusEvent e) {
				
			}	
		});

		tfIntersectPointSize.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent event) {
				
			}
			@Override
			public void keyPressed(KeyEvent event) {
				if (event.getKeyCode() == KeyEvent.VK_ENTER) {
			        tfIntersectPointSize.setFont(new Font("Calabri", Font.PLAIN, 11));
					tfIntersectPointSize.transferFocus();
				}
			}
			@Override
			public void keyReleased(KeyEvent event) {

			}
		});

		tfMaxAllowedDeviation = new JFormattedTextField(numberFormatter);
		tfMaxAllowedDeviation.setText(twoDigitIntegerFormat.format(sto.getMaxAllowedDeviation()));
		tfMaxAllowedDeviation.setHorizontalAlignment(SwingConstants.CENTER);
		tfMaxAllowedDeviation.setFont(new Font("Calabri", Font.PLAIN, 11));
		tfMaxAllowedDeviation.setBackground(Color.WHITE);
		tfMaxAllowedDeviation.setForeground(Color.BLACK);
		
		tfMaxAllowedDeviation.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				tfMaxAllowedDeviation.setFont(new Font("Calabri", Font.BOLD, 11));
			}
			@Override
			public void focusLost(FocusEvent e) {
				
			}	
		});

		tfMaxAllowedDeviation.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent event) {
				
			}
			@Override
			public void keyPressed(KeyEvent event) {
				if (event.getKeyCode() == KeyEvent.VK_ENTER) {
			        tfMaxAllowedDeviation.setFont(new Font("Calabri", Font.PLAIN, 11));
					tfMaxAllowedDeviation.transferFocus();
				}
			}
			@Override
			public void keyReleased(KeyEvent event) {

			}
		});

		tfCursorDiameter = new JFormattedTextField(numberFormatter);
		tfCursorDiameter.setText(twoDigitIntegerFormat.format(sto.getCursorDiameter()));
		tfCursorDiameter.setHorizontalAlignment(SwingConstants.CENTER);
		tfCursorDiameter.setFont(new Font("Calabri", Font.PLAIN, 11));
		tfCursorDiameter.setBackground(Color.WHITE);
		tfCursorDiameter.setForeground(Color.BLACK);
		
		tfCursorDiameter.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				tfCursorDiameter.setFont(new Font("Calabri", Font.BOLD, 11));
			}
			@Override
			public void focusLost(FocusEvent e) {
				
			}	
		});

		tfCursorDiameter.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent event) {
				
			}
			@Override
			public void keyPressed(KeyEvent event) {
				if (event.getKeyCode() == KeyEvent.VK_ENTER) {
			        tfCursorDiameter.setFont(new Font("Calabri", Font.PLAIN, 11));
					tfCursorDiameter.transferFocus();
				}
			}
			@Override
			public void keyReleased(KeyEvent event) {

			}
		});
		
		tfTraceDiameter = new JFormattedTextField(numberFormatter);
		tfTraceDiameter.setText(twoDigitIntegerFormat.format(sto.getTraceDiameter()));
		tfTraceDiameter.setHorizontalAlignment(SwingConstants.CENTER);
		tfTraceDiameter.setFont(new Font("Calabri", Font.PLAIN, 11));
		tfTraceDiameter.setBackground(Color.WHITE);
		tfTraceDiameter.setForeground(Color.BLACK);
		
		tfTraceDiameter.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				tfTraceDiameter.setFont(new Font("Calabri", Font.BOLD, 11));
			}
			@Override
			public void focusLost(FocusEvent e) {
				
			}	
		});

		tfTraceDiameter.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent event) {
				
			}
			@Override
			public void keyPressed(KeyEvent event) {
				if (event.getKeyCode() == KeyEvent.VK_ENTER) {
			        tfTraceDiameter.setFont(new Font("Calabri", Font.PLAIN, 11));
					tfTraceDiameter.transferFocus();
				}
			}
			@Override
			public void keyReleased(KeyEvent event) {

			}
		});
		
		final String cancelName = "cancel";
        final InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), cancelName);
        final ActionMap actionMap = getRootPane().getActionMap();
        
        actionMap.put(cancelName, new AbstractAction() {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        
		jccCancel.addActionListener(_ -> jccDialog.setVisible(false));

		jccApply.addActionListener(_ -> {
			final Color selectedColor = jcc.getColor();
			jccDialog.setVisible(false);
			if ((colorIndex >= 0) && (colorIndex <= 13)) {
				sto.getFlightColor()[colorIndex] = selectedColor;
				tfFlight[colorIndex].setBackground(selectedColor);
			}
			if (colorIndex == 14) {
				sto.setAsymptoteColor(selectedColor);
				tfAsymptoteColor.setBackground(selectedColor);
			}
			if (colorIndex == 15) {
				sto.setTraceColor(selectedColor);
				tfTraceColor.setBackground(selectedColor);
			}
			if (colorIndex == 16) {
				sto.setCursorColor(selectedColor);
				tfCursorColor.setBackground(selectedColor);
			}
			if (colorIndex == 17) {
				sto.setTargetRingColor(selectedColor);
				tfTargetRingColor.setBackground(selectedColor);
			}
			if (colorIndex == 18) {
				sto.setIntersectPointColor(selectedColor);
				tfIntersectPointColor.setBackground(selectedColor);
			}
		});
	}	

	protected void cbShowIntersectPointsCheckBoxItemStateChanged(ItemEvent event) {
		sto.setShowIntersectPoints(event.getStateChange() == ItemEvent.SELECTED);
	}

	protected void cbShowTargetRingCheckBoxItemStateChanged(ItemEvent event) {
		sto.setShowTargetRing(event.getStateChange() == ItemEvent.SELECTED);
	}

	protected void cbShowTraceCheckBoxItemStateChanged(ItemEvent event) {
		sto.setShowTraces(event.getStateChange() == ItemEvent.SELECTED);
	}
	
	protected void cbTraceEqualsFlightColorCheckBoxItemStateChanged(ItemEvent event) {
		if (event.getStateChange() == ItemEvent.SELECTED) {
			sto.setTraceEqualsFlightColor(true);
			tfTraceColor.setEnabled(false);
			tfTraceColor.setBackground(Color.LIGHT_GRAY);
		} else {
			sto.setTraceEqualsFlightColor(false);
			tfTraceColor.setEnabled(true);
		}
	}
	
	protected void cbShowCursorsCheckBoxItemStateChanged(ItemEvent event) {
		sto.setShowCursors(event.getStateChange() == ItemEvent.SELECTED);
	}

	protected void cbShowAsymptotesCheckBoxItemStateChanged(ItemEvent event) {
		sto.setShowAsymptotes(event.getStateChange() == ItemEvent.SELECTED);
	}

	protected void cbShowFlightPathsCheckBoxItemStateChanged(ItemEvent event) {
		sto.setShowFlightPaths(event.getStateChange() == ItemEvent.SELECTED);
	}

	protected void tfTraceColorMouseClicked() {
		colorIndex = 15;
		jccDialog.setTitle("Trace Color");
		jccDialog.setVisible(true);
	}

	protected void tfTargetRingColorMouseClicked() {
		colorIndex = 17;
		jccDialog.setTitle("Target Ring Color");
		jccDialog.setVisible(true);
	}

	protected void tfIntersectPointColorMouseClicked() {
		colorIndex = 18;
		jccDialog.setTitle("Intersect Point Color");
		jccDialog.setVisible(true);
	}

	protected void tfCursorColorMouseClicked() {
		colorIndex = 16;
		jccDialog.setTitle("Cursor Color");
		jccDialog.setVisible(true);
	}

	protected void tfAsymptoteColorMouseClicked() {
		colorIndex = 14;
		jccDialog.setTitle("Asymptote Color");
		jccDialog.setVisible(true);
	}
	
	private void setjccDialog(final int colorIndex) {
		this.colorIndex = colorIndex;
		jccDialog.setTitle(String.format("Flight %d Color", colorIndex + 1));
		jccDialog.setVisible(true);
	}
	
	public String getTestName() {
		return testName;
	}

	public void setTestName(String testName) {
		this.testName = testName;
	}

	public void showSettingsDialog(boolean showSettingsDialog) {
		setVisible(showSettingsDialog);
	}

	private void applyButtonActionListenerEvent() {
		sto.setCursorDiameter(Double.parseDouble(tfCursorDiameter.getText()));
		sto.setTraceDiameter(Double.parseDouble(tfTraceDiameter.getText()));
		sto.setIntersectPointDiameter(Double.parseDouble(tfIntersectPointSize.getText()));		
		tfIntersectPointSize.setFont(new Font("Calabri", Font.PLAIN, 11));
		tfCursorDiameter.setFont(new Font("Calabri", Font.PLAIN, 11));
		tfTraceDiameter.setFont(new Font("Calabri", Font.PLAIN, 11));
		tfMaxAllowedDeviation.setFont(new Font("Calabri", Font.PLAIN, 11));
		sto.saveSettings();
	}
	
	private void drawGraphicalUserInterface() {
		final GroupLayout flightColoringPanelLayout = new GroupLayout(flightColoringPanel);
        flightColoringPanel.setLayout(flightColoringPanelLayout);
        flightColoringPanelLayout.setAutoCreateGaps(true);
        flightColoringPanelLayout.setAutoCreateContainerGaps(true);
        
        flightColoringPanelLayout.setHorizontalGroup(flightColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(flightColoringPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(flightColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(flightColoringPanelLayout.createSequentialGroup()
                        .addComponent(flightLabel[0],70,70,70)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfFlight[0],70,70,70))
                    .addGroup(flightColoringPanelLayout.createSequentialGroup()
                        .addComponent(flightLabel[1],70,70,70)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfFlight[1],70,70,70))
                    .addGroup(flightColoringPanelLayout.createSequentialGroup()
                        .addComponent(flightLabel[2],70,70,70)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfFlight[2],70,70,70))
                    .addGroup(flightColoringPanelLayout.createSequentialGroup()
                        .addComponent(flightLabel[3],70,70,70)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfFlight[3],70,70,70))
                    .addGroup(flightColoringPanelLayout.createSequentialGroup()
                        .addComponent(flightLabel[4],70,70,70)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfFlight[4],70,70,70))
                    .addGroup(flightColoringPanelLayout.createSequentialGroup()
                        .addComponent(flightLabel[5],70,70,70)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfFlight[5],70,70,70))
                    .addGroup(flightColoringPanelLayout.createSequentialGroup()
                        .addComponent(flightLabel[6],70,70,70)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfFlight[6],70,70,70))
                    .addGroup(flightColoringPanelLayout.createSequentialGroup()
                        .addComponent(flightLabel[7],70,70,70)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfFlight[7],70,70,70))
                    .addGroup(flightColoringPanelLayout.createSequentialGroup()
                        .addComponent(flightLabel[8],70,70,70)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfFlight[8],70,70,70))
                    .addGroup(flightColoringPanelLayout.createSequentialGroup()
                        .addComponent(flightLabel[9],70,70,70)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfFlight[9],70,70,70))
                    .addGroup(flightColoringPanelLayout.createSequentialGroup()
                        .addComponent(flightLabel[10],70,70,70)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfFlight[10],70,70,70))
                    .addGroup(flightColoringPanelLayout.createSequentialGroup()
                        .addComponent(flightLabel[11],70,70,70)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfFlight[11],70,70,70))
                    .addGroup(flightColoringPanelLayout.createSequentialGroup()
                        .addComponent(flightLabel[12],70,70,70)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfFlight[12],70,70,70))
                    .addGroup(flightColoringPanelLayout.createSequentialGroup()
                        .addComponent(flightLabel[13],70,70,70)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfFlight[13],70,70,70)))
                .addContainerGap()));
        
        flightColoringPanelLayout.setVerticalGroup(flightColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(GroupLayout.Alignment.TRAILING, flightColoringPanelLayout.createSequentialGroup()
                .addGap(10,10,10)
                .addGroup(flightColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(flightLabel[0])
                	.addComponent(tfFlight[0],15,15,15))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(flightColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(flightLabel[1])
                	.addComponent(tfFlight[1],15,15,15))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(flightColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(flightLabel[2])
                    .addComponent(tfFlight[2],15,15,15))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(flightColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(flightLabel[3])
                    .addComponent(tfFlight[3],15,15,15))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(flightColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(flightLabel[4])
                    .addComponent(tfFlight[4],15,15,15))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(flightColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(flightLabel[5])
                    .addComponent(tfFlight[5],15,15,15))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(flightColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(flightLabel[6])
                    .addComponent(tfFlight[6],15,15,15))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(flightColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(flightLabel[7])
                    .addComponent(tfFlight[7],15,15,15))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(flightColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(flightLabel[8])
                    .addComponent(tfFlight[8],15,15,15))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(flightColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(flightLabel[9])
                    .addComponent(tfFlight[9],15,15,15))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(flightColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(flightLabel[10])
                    .addComponent(tfFlight[10],15,15,15))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(flightColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(flightLabel[11])
                    .addComponent(tfFlight[11],15,15,15))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(flightColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(flightLabel[12])
                    .addComponent(tfFlight[12],15,15,15))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(flightColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(flightLabel[13])
                    .addComponent(tfFlight[13],15,15,15))
                .addContainerGap(10, Short.MAX_VALUE)));
        
        final GroupLayout staticSignalLocationSettingsPanelLayout = new GroupLayout(staticSignalLocationSettingsPanel);
        staticSignalLocationSettingsPanel.setLayout(staticSignalLocationSettingsPanelLayout);
        staticSignalLocationSettingsPanelLayout.setAutoCreateGaps(true);
        staticSignalLocationSettingsPanelLayout.setAutoCreateContainerGaps(true);

        staticSignalLocationSettingsPanelLayout.setHorizontalGroup(
        		staticSignalLocationSettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(staticSignalLocationSettingsPanelLayout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addGroup(staticSignalLocationSettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(cbShowFlightPaths)
                    .addGroup(staticSignalLocationSettingsPanelLayout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addGroup(staticSignalLocationSettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(cbTraceEqualsFlightColor)
                            .addGroup(staticSignalLocationSettingsPanelLayout.createSequentialGroup()
                            	.addComponent(maxAllowedDeviationLabel)
                            	.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            	.addComponent(tfMaxAllowedDeviation, 30,30,30))
                            .addGroup(staticSignalLocationSettingsPanelLayout.createSequentialGroup()
                                .addGroup(staticSignalLocationSettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                    .addComponent(cbShowIntersectPoints)
                                    .addComponent(cbShowCursors)
                                    .addComponent(cbShowAsymptotes)
                                    .addComponent(cbShowTargetRing)
                                    .addComponent(cbShowTraces))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(staticSignalLocationSettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                    .addGroup(staticSignalLocationSettingsPanelLayout.createSequentialGroup()
                                        .addGroup(staticSignalLocationSettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                            .addGroup(staticSignalLocationSettingsPanelLayout.createSequentialGroup()
                                                .addGroup(staticSignalLocationSettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                    .addComponent(tfIntersectPointColor, 70,70,70)
                                                    .addComponent(tfTraceColor, 70,70,70)
                                                    .addComponent(tfAsymptoteColor, 70,70,70)
                                                    .addComponent(tfTargetRingColor, 70,70,70)
                                                    .addComponent(tfCursorColor, 70,70,70))
                                                .addGroup(staticSignalLocationSettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                    .addGroup(staticSignalLocationSettingsPanelLayout.createSequentialGroup()
                                                        .addGap(5,5,5)
                                                        .addComponent(cursorDiameterLabel, 230,230,230)
                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(tfCursorDiameter, 30,30,30))
                                                    .addGroup(staticSignalLocationSettingsPanelLayout.createSequentialGroup()
                                                        .addGap(5,5,5)
                                                        .addComponent(intersectPointDiameterLabel, 230,230,230)
                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(tfIntersectPointSize, 30,30,30))
                                                    .addGroup(staticSignalLocationSettingsPanelLayout.createSequentialGroup()
                                                         .addGap(5,5,5)
                                                         .addComponent(traceDiameterLabel, 230,230,230)
                                                         .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                         .addComponent(tfTraceDiameter, 30,30,30))))
                                            .addGroup(staticSignalLocationSettingsPanelLayout.createSequentialGroup()))
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE))
                                    .addGroup(staticSignalLocationSettingsPanelLayout.createSequentialGroup()
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                .addComponent(flightColoringPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap()));
        
        staticSignalLocationSettingsPanelLayout.setVerticalGroup(
            staticSignalLocationSettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(staticSignalLocationSettingsPanelLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(staticSignalLocationSettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(staticSignalLocationSettingsPanelLayout.createSequentialGroup()
                        .addComponent(cbShowFlightPaths)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(staticSignalLocationSettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(cbShowCursors)
                            .addComponent(tfCursorColor, 25,25,25)
                            .addComponent(cursorDiameterLabel)
                            .addComponent(tfCursorDiameter, 25,25,25))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(staticSignalLocationSettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(cbShowIntersectPoints)
                            .addComponent(tfIntersectPointColor, 25,25,25)
                            .addComponent(intersectPointDiameterLabel)
                            .addComponent(tfIntersectPointSize, 25,25,25))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(staticSignalLocationSettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    		.addComponent(cbShowTraces)
                            .addComponent(tfTraceColor, 25,25,25)
                            .addComponent(traceDiameterLabel)
                            .addComponent(tfTraceDiameter, 25,25,25))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(staticSignalLocationSettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(cbShowTargetRing)
                            .addComponent(tfTargetRingColor, 25,25,25))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(staticSignalLocationSettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    		.addComponent(cbShowAsymptotes)
                            .addComponent(tfAsymptoteColor, 25,25,25))
                        .addGap(40, 40, 40)
                        .addGroup(staticSignalLocationSettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        	.addComponent(cbTraceEqualsFlightColor))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(staticSignalLocationSettingsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        	.addComponent(maxAllowedDeviationLabel)
                            .addComponent(tfMaxAllowedDeviation, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                    .addComponent(flightColoringPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap()));

        final GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                	.addGroup(layout.createSequentialGroup()
		                .addContainerGap()
		                .addComponent(tabbedPane,GroupLayout.PREFERRED_SIZE,GroupLayout.DEFAULT_SIZE,GroupLayout.PREFERRED_SIZE))
		                .addGroup(layout.createSequentialGroup()
			                .addComponent(okButton,90,90,90)
			                .addComponent(applyButton,90,90,90)
			                .addComponent(cancelButton,90,90,90)))
		        	.addContainerGap(GroupLayout.DEFAULT_SIZE,Short.MAX_VALUE)));

        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
        	.addGroup(GroupLayout.Alignment.TRAILING,layout.createSequentialGroup()
        		.addComponent(tabbedPane,GroupLayout.PREFERRED_SIZE,GroupLayout.DEFAULT_SIZE,GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
	                .addComponent(okButton)
	                .addComponent(applyButton)
	                .addComponent(cancelButton))
                .addContainerGap(GroupLayout.DEFAULT_SIZE,Short.MAX_VALUE)));
        
        final GroupLayout jccDialogLayout = new GroupLayout(jccDialog.getContentPane());
		jccDialog.getContentPane().setLayout(jccDialogLayout);
		jccDialogLayout.setHorizontalGroup(
	            jccDialogLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
	            .addGroup(GroupLayout.Alignment.TRAILING, jccDialogLayout.createSequentialGroup()
	                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	                .addComponent(jccApply, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
	                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
	                .addComponent(jccCancel, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
	                .addGap(134, 134, 134))
	            .addGroup(jccDialogLayout.createSequentialGroup()
	                .addContainerGap()
	                .addComponent(jcc, GroupLayout.PREFERRED_SIZE, 420, GroupLayout.PREFERRED_SIZE)
	                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		
	        jccDialogLayout.setVerticalGroup(
	            jccDialogLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
	            .addGroup(jccDialogLayout.createSequentialGroup()
	                .addContainerGap()
	                .addComponent(jcc, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)
	                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
	                .addGroup(jccDialogLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
	                    .addComponent(jccApply)
	                    .addComponent(jccCancel))
	                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
	        
		final Toolkit tk = Toolkit.getDefaultToolkit();
		final Dimension screenSize = tk.getScreenSize();

		jccDialog.pack();
		jccDialog.setLocation((screenSize.width / 2) - (jccDialog.getWidth() / 2),
				(screenSize.height / 2) - (jccDialog.getHeight() / 2));
		
		pack();
		
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		setLocation((screenSize.width / 2) - (getWidth() / 2), (screenSize.height / 2) - (getHeight() / 2));
	}
	
	public static void main(String[] args) {
		new StaticTestComponent(new StaticTestObject(false, new File("test")));
	}
}
