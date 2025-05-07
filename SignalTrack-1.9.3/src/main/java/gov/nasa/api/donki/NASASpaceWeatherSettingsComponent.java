package gov.nasa.api.donki;

import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

public class NASASpaceWeatherSettingsComponent extends JDialog {
	private static final long serialVersionUID = 1L;
	
	private JLabel jlblPersist;
	private JLabel jlblCheck;
	private JLabel jlblEventMonitor;

	private JTabbedPane tabbedPane;
	private JButton okButton;
	private JButton cancelButton;
	private JButton applyButton;

	private List<JTextField> jtfPersist;
	private List<JTextField> jtfCheck;
	private List<JCheckBox> jcbEnable;
	
	private final transient NASASpaceWeatherProcessor swp;

	public NASASpaceWeatherSettingsComponent(NASASpaceWeatherProcessor swp) {
		this.swp = swp;
		
		initializeComponents();
		setComponentValues();
		drawGraphicalUserInterface();
		configureListeners();

		display();
	}

	private void initializeComponents() {
		invokeLaterInDispatchThreadIfNeeded(() -> {
			jtfPersist = new ArrayList<>(8);
			jtfCheck = new ArrayList<>(8);
			jcbEnable = new ArrayList<>(8);
			
			tabbedPane = new JTabbedPane();
			
			jlblEventMonitor = new JLabel();
			jlblPersist = new JLabel();
			jlblCheck = new JLabel();
			
			okButton = new JButton("OK");
			cancelButton = new JButton("Cancel");
			applyButton = new JButton("Apply");
	        
	        for (int i = 0; i < swp.getNasaMonitorList().size(); i++) {
	        	final JTextField jtf = new JTextField();
				jtf.setHorizontalAlignment(SwingConstants.CENTER);
	        	jtf.setText(String.valueOf(swp.getNasaMonitor(i).getPersistenceMinutes()));
	        	jtfPersist.add(jtf);
	        }
	        
	        for (int i = 0; i < swp.getNasaMonitorList().size(); i++) {
	        	final JTextField jtf = new JTextField();
				jtf.setHorizontalAlignment(SwingConstants.CENTER);
	        	jtf.setText(String.valueOf(swp.getNasaMonitor(i).getCheckPeriodSeconds()));
	        	jtfCheck.add(jtf);
	        }
	        
	        for (int i = 0; i < swp.getNasaMonitorList().size(); i++) {
	        	final JCheckBox jcb = new JCheckBox();
	        	jcb.setSelected(swp.getNasaMonitor(i).isEnabled());
	        	jcbEnable.add(jcb);
	        }
		});
	}

	private void configureListeners() {
		super.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent event) {
				if (WindowEvent.WINDOW_CLOSING == event.getID()) {
					setVisible(false);
				}
			}
		});
		
		okButton.addActionListener(_ -> okButtonActionListenerEvent());
        cancelButton.addActionListener(_ -> cancelButtonActionListenerEvent());
        applyButton.addActionListener(_ -> applyButtonActionListenerEvent());
	}

	private void setComponentValues() {
		invokeLaterInDispatchThreadIfNeeded(() -> {
			setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
			setTitle("Space Weather Monitor Settings");
			setAlwaysOnTop(true);
			setResizable(false);
			
			jlblEventMonitor.setFont(new Font("Segoe UI", 1, 12));
	        jlblCheck.setFont(new Font("Segoe UI", 1, 12));
	        jlblPersist.setFont(new Font("Segoe UI", 1, 12));
			
			okButton.setMultiClickThreshhold(50L);
			cancelButton.setMultiClickThreshhold(50L);
			applyButton.setMultiClickThreshhold(50L);
			
			jlblPersist.setHorizontalAlignment(SwingConstants.LEFT);
			jlblEventMonitor.setText("Event Monitor");
			
					
	        jlblPersist.setHorizontalAlignment(SwingConstants.CENTER);
	        jlblPersist.setText("Persist (Min)");
	        
	        jlblCheck.setHorizontalAlignment(SwingConstants.CENTER);
	        jlblCheck.setText("Check (Sec)");
	
	        jcbEnable.get(0).setText("Solar Flares");
	        jcbEnable.get(1).setText("Coronal Mass Ejections");
	        jcbEnable.get(2).setText("Geomagnetic Storms");
	        jcbEnable.get(3).setText("Solar Enegertic Particle Release");
	        jcbEnable.get(4).setText("Interplanetary Shocks");
	        jcbEnable.get(5).setText("Radiation Belt Enhancements");
	        jcbEnable.get(6).setText("High Speed Streams");
	        jcbEnable.get(7).setText("Magnetopause Crossings");
	
			tabbedPane.removeAll();
			tabbedPane.addTab("Space Weather Monitor Settings", null, getSpaceWeatherEventSettingsPanel(), null);
		});
	}

	private void applyButtonActionListenerEvent() {
		for (int i = 0; i < swp.getNasaMonitorList().size(); i++) {
			swp.getNasaMonitor(i).setEnabled(jcbEnable.get(i).isSelected());
			swp.getNasaMonitor(i).setPersistenceMinutes(Long.parseLong(jtfPersist.get(i).getText()));
			swp.getNasaMonitor(i).setCheckPeriodSeconds(Long.parseLong(jtfCheck.get(i).getText()));
		}
		swp.savePreferences();
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

	private JPanel getSpaceWeatherEventSettingsPanel() {
		final JPanel panel = new JPanel(new GridBagLayout());
		
        panel.setBorder(BorderFactory.createTitledBorder(null, "Space Weather Events", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Segoe UI", 1, 14))); // NOI18N

        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.gridy = 0;
        gbc.ipadx = 130;
        panel.add(jlblEventMonitor, gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 1;
        gbc.gridy = 0;
        gbc.ipadx = 30;
        panel.add(jlblPersist, gbc);
        
        gbc.gridx = 2;
        gbc.gridwidth = 1;
        gbc.gridy = 0;
        gbc.ipadx = 30;
        panel.add(jlblCheck, gbc);
        
        for (int y = 0; y < swp.getNasaMonitorList().size(); y++) {
        	gbc.gridy = y + 1;
        	gbc.gridwidth = 1;
        	gbc.gridx = 0;
        	panel.add(jcbEnable.get(y), gbc);
        	gbc.gridx = 1;
        	panel.add(jtfPersist.get(y), gbc);
        	gbc.gridx = 2;
        	panel.add(jtfCheck.get(y), gbc);
        }
        
		return panel;
	}

	private void drawGraphicalUserInterface() {
		invokeLaterInDispatchThreadIfNeeded(() -> {
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
						.addGroup(layout.createSequentialGroup().addComponent(okButton, 90, 90, 90)
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
		});
	}

	private static void invokeLaterInDispatchThreadIfNeeded(Runnable runnable) {
		if (EventQueue.isDispatchThread()) {
			runnable.run();
		} else {
			SwingUtilities.invokeLater(runnable);
		}
	}
	
	private void display() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
    }
}
