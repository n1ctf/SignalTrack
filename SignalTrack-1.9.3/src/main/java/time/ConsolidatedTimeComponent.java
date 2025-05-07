package time;

import java.awt.Dialog;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import components.SortedComboBoxModel;

public class ConsolidatedTimeComponent extends JDialog {
	private static final long serialVersionUID = 1L;

	private static final Logger LOG = Logger.getLogger(ConsolidatedTimeComponent.class.getName());
	
	private static final String DEFAULT_PANEL_NAME = "Time and Locale Settings";

	private JLabel lblDefaultTimeZoneLabel;
	private JLabel lblLocaleLabel;

	private JComboBox<String> jcboLocale;
	private JComboBox<String> jcboTimeZone;
	
	private Locale loc;
	private ZoneId zid;
	
	private String panelName = DEFAULT_PANEL_NAME;
	
	private transient ConsolidatedTime consolidatedTime;
	
	private JTabbedPane tabbedPane;
	private JButton okButton;
	private JButton cancelButton;
	private JButton applyButton;
	
	public ConsolidatedTimeComponent(ConsolidatedTime consolidatedTime) {
		this.consolidatedTime = consolidatedTime;

		super.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent event) {
				if (WindowEvent.WINDOW_CLOSING == event.getID()) {
					applyButtonActionListenerEvent();
				}
			}
		});
		
		initializeComponents();
		updateComponents();
		initializeListeners();
		drawGraphicalUserInterface();
		
		showDisplay();
		
	}

	private void initializeComponents() {
		try {
			tabbedPane = new JTabbedPane();

			applyButton = new JButton("Apply");
			cancelButton = new JButton("Cancel");
			okButton = new JButton("OK");

			loc = consolidatedTime.getLocale();
			zid = consolidatedTime.getLocalZoneId();
			
			SortedComboBoxModel<String> jcboTimeZoneModel = new SortedComboBoxModel<>(getZoneIdNameArray());
			lblDefaultTimeZoneLabel = new JLabel();
			jcboTimeZone = new JComboBox<>(jcboTimeZoneModel);

			SortedComboBoxModel<String> jcboLocaleModel = new SortedComboBoxModel<>(getLocaleDisplayNameArray());
			lblLocaleLabel = new JLabel();
			jcboLocale = new JComboBox<>(jcboLocaleModel);
		} catch (NullPointerException npe) {
			LOG.log(Level.CONFIG, npe.getMessage());
		}
	}
	
    private void applyButtonActionListenerEvent() {
        consolidatedTime.setLocale(this.loc);
        consolidatedTime.setLocalZoneId(this.zid);
    }
    
    private void okButtonActionListenerEvent() {
    	applyButtonActionListenerEvent();
        closeWindow();
    }

    private void cancelButtonActionListenerEvent() {
    	closeWindow();
    }
	
    private void closeWindow() {
    	setVisible(false);
        dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }
    
	private static Locale forDisplayName(String displayName) {
		Locale locale = ConsolidatedTime.DEFAULT_LOCALE;
		for (Locale loc : Locale.getAvailableLocales()) {
			if (loc.getDisplayName().equals(displayName)) {
				locale = loc;
				break;
			}
		}
		return locale;
	}

	public void setPanelName(String panelName) {
		this.panelName = panelName;
	}
	
	public String getPanelName() {
		return panelName;
	}

	private static String[] getZoneIdNameArray() {
		return ZoneId.getAvailableZoneIds().toArray(new String[ZoneId.getAvailableZoneIds().size()]);
	}

	private static String[] getLocaleDisplayNameArray() {
		List<String> list = new ArrayList<>();
		for (Locale l : Locale.getAvailableLocales()) {
			list.add(l.getDisplayName());
		}
		return list.toArray(new String[list.size()]);
	}

	private void updateComponents() {
		setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
		setTitle("Consolidated Time Service Settings");
		
		okButton.setMultiClickThreshhold(50L);        
        cancelButton.setMultiClickThreshhold(50L);
        applyButton.setMultiClickThreshhold(50L);
		
		tabbedPane.removeAll();
		tabbedPane.addTab("Station Settings", null, getConfigGUI(), null);
		
		lblLocaleLabel.setText("Locality");
		lblLocaleLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		jcboLocale.setSelectedItem(consolidatedTime.getLocale().getDisplayName());

		lblDefaultTimeZoneLabel.setText("Time Zone");
		lblDefaultTimeZoneLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		jcboTimeZone.setSelectedItem(consolidatedTime.getLocalZoneId().getId());
	}

	private void initializeListeners() {
		okButton.addActionListener(_ -> okButtonActionListenerEvent());
        
		cancelButton.addActionListener(_ -> cancelButtonActionListenerEvent());
        
        applyButton.addActionListener(_ -> applyButtonActionListenerEvent());
        
		jcboLocale.addItemListener(event -> {
			if (event.getStateChange() == ItemEvent.SELECTED) {
				JComboBox<?> cb = (JComboBox<?>) event.getSource();
				loc = forDisplayName((String) cb.getSelectedItem());
			}
		});
		
		jcboTimeZone.addItemListener(event -> {
			if (event.getStateChange() == ItemEvent.SELECTED) {
				JComboBox<?> cb = (JComboBox<?>) event.getSource();
				zid = ZoneId.of((String) cb.getSelectedItem());
			}
		});
	}

    private JPanel getConfigGUI() {
		JPanel panel = new JPanel();
		
		GroupLayout layout = new GroupLayout(panel);

		panel.setLayout(layout);

		layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblLocaleLabel, 100, 100, 100)
                    .addComponent(lblDefaultTimeZoneLabel, 100, 100, 100))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addComponent(jcboLocale, 350, 350, 350)
                    .addComponent(jcboTimeZone, 350, 350, 350))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(lblLocaleLabel)
                    .addComponent(jcboLocale))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(lblDefaultTimeZoneLabel)
                    .addComponent(jcboTimeZone))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		return panel;
	}
    
    private void drawGraphicalUserInterface() {
		GroupLayout layout = new GroupLayout(getContentPane());
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

}
