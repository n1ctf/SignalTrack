package time;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import javax.swing.border.TitledBorder;

import gov.nasa.api.ners.NetworkEarthRotationService;
import gov.nasa.api.ners.NetworkEarthRotationService.Precision;

public class DateTimeServiceComponent implements AutoCloseable {
	private static final Logger LOG = Logger.getLogger(DateTimeServiceComponent.class.getName());
	private static final Preferences userPrefs = Preferences.userRoot().node(DateTimeServiceComponent.class.getName());
	private static final PropertyChangeSupport pcs = new PropertyChangeSupport(DateTimeServiceComponent.class);

	private static final Locale DEFAULT_LOCALE = Locale.US;
	private static final ZoneId DEFAULT_ZONE_ID = ZoneId.systemDefault();
	
	private static final String DEFAULT_PANEL_NAME = "Time Rotator Panel Selector";
	
	private static final long CYCLE_DELAY_MILLISECONDS = 1000;

	private JLabel lblTimeLabel;

	private ConsolidatedTime consolidatedTime;

	private Locale locale;
	private ZoneId zoneId;
	
	private String panelName = DEFAULT_PANEL_NAME;

	private boolean cycle;
	
	private NetworkEarthRotationService ners;
	
	private final List<JCheckBox> timeStandardCheckboxList = new ArrayList<>(8);
		
	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	
	public DateTimeServiceComponent(ConsolidatedTime consolidatedTime, NetworkEarthRotationService ners) {
		this.consolidatedTime = consolidatedTime;
		this.ners = ners;
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				close();
			}
		});
		
		loadPreferences();
		initializeComponents();
		updateComponents();
		initializeListeners();
	}

	private void initializeComponents() {
		try {
			lblTimeLabel = new JLabel();
			lblTimeLabel.setFont(new Font("Tahoma", Font.BOLD, 16));
		} catch (NullPointerException npe) {
			LOG.log(Level.CONFIG, npe.getMessage());
		}
	}

	private void loadPreferences() {
		locale = forDisplayName(userPrefs.get("Locale", DEFAULT_LOCALE.getDisplayName()));
		zoneId = ZoneId.of(userPrefs.get("ZoneId", DEFAULT_ZONE_ID.getId()));
		
		for (int i = 0; i <= 6; i++) {
			final JCheckBox jcb = new JCheckBox();
			jcb.setSelected(userPrefs.getBoolean("TimeStandardCheckboxListElement_" + i, true));
			timeStandardCheckboxList.add(jcb);
		}
	}
	
	private static Locale forDisplayName(String displayName) {
		Locale locale = DEFAULT_LOCALE;
		for (Locale loc : Locale.getAvailableLocales()) {
			if (loc.getDisplayName().equals(displayName)) {
				locale = loc;
				break;
			}
		}
		return locale;
	}

	public void startTimeStandardRotate() {
		executor.execute(new TimeStandardRotator());
	}

	public void stopTimeStandardRotate() {
		cycle = false;
		if (executor != null) {
			try {
				LOG.log(Level.INFO, "Initializing DateTimeServiceComponent.executor service termination....");
				executor.shutdown();
				executor.awaitTermination(8, TimeUnit.SECONDS);
				LOG.log(Level.INFO, "DateTimeServiceComponent.executor service has gracefully terminated");
			} catch (InterruptedException e) {
				executor.shutdownNow();
				LOG.log(Level.SEVERE, "DateTimeServiceComponent.executor service has timed out after 8 seconds of waiting to terminate processes.");
				Thread.currentThread().interrupt();
			}
		}
	}

	private final class TimeStandardRotator implements Runnable {
		
		private int count;
		
		private TimeStandardRotator() {
			count = 0;
		}
		
		@Override
		public void run() {
			try {
				cycle = true;
				while (cycle) {
					while (cycle && timeStandardCheckboxList.get(0).isSelected()) {
						final LocalTime localTime = AstronomicalTime.getApparentSiderealTime(ners.getAdjustedUT1Time(),
							consolidatedTime.getLatitudeDegrees(), consolidatedTime.getLongitudeDegrees(), 
							(int) consolidatedTime.getAltitudeMeters());
						final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
						final String formattedString = localTime.format(formatter);
						updateTimeClockLabel(lblTimeLabel, formattedString, ners.getUT1Precision(), "LAST");
						lblTimeLabel.setToolTipText("Local Apparent Sidereal Time");
						if (!cycle) {
							break;
						}
						TimeUnit.MILLISECONDS.sleep(CYCLE_DELAY_MILLISECONDS);
						count++;
						if (count >= 6 || !cycle) {
							count = 0;
							break;
						}
					}
					while (cycle && timeStandardCheckboxList.get(1).isSelected()) {
						final LocalTime localTime = AstronomicalTime.getGreenwichMeanSiderealTime(ners.getAdjustedUT1Time(),
							consolidatedTime.getLatitudeDegrees(), consolidatedTime.getLongitudeDegrees(), 
							(int) consolidatedTime.getAltitudeMeters());
						final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
						final String formattedString = localTime.format(formatter);
						updateTimeClockLabel(lblTimeLabel, formattedString, ners.getUT1Precision(), "GMST");
						lblTimeLabel.setToolTipText("Greenwich Mean Sidereal Time");
						if (!cycle) {
							break;
						}
						TimeUnit.MILLISECONDS.sleep(CYCLE_DELAY_MILLISECONDS);
						count++;
						if (count >= 6 || !cycle) {
							count = 0;
							break;
						}
					}
					while (cycle && timeStandardCheckboxList.get(2).isSelected()) {
						final LocalTime localTime = AstronomicalTime.getGreenwichApparentSiderealTime(ners.getAdjustedUT1Time(),
							consolidatedTime.getLatitudeDegrees(), consolidatedTime.getLongitudeDegrees(), 
							(int) consolidatedTime.getAltitudeMeters());
						final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
						final String formattedString = localTime.format(formatter);
						updateTimeClockLabel(lblTimeLabel, formattedString, ners.getUT1Precision(), "GAST");
						lblTimeLabel.setToolTipText("Greenwich Apparent Sidereal Time");
						if (!cycle) {
							break;
						}
						TimeUnit.MILLISECONDS.sleep(CYCLE_DELAY_MILLISECONDS);
						count++;
						if (count >= 6 || !cycle) {
							count = 0;
							break;
						}
					}
					while (cycle && timeStandardCheckboxList.get(3).isSelected()) {
						final ZonedDateTime zonedDateTime = consolidatedTime.getCurrentUTC();
						final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
						final String formattedString = zonedDateTime.format(formatter);
						updateTimeClockLabel(lblTimeLabel, formattedString, consolidatedTime.getTimeStratum(), "UTC");
						lblTimeLabel.setToolTipText("Universal Coordinated Time");
						if (!cycle) {
							break;
						}
						TimeUnit.MILLISECONDS.sleep(CYCLE_DELAY_MILLISECONDS);
						count++;
						if (count >= 6 || !cycle) {
							count = 0;
							break;
						}
					}
					while (cycle && timeStandardCheckboxList.get(4).isSelected()) { 
						final LocalDateTime localDateTime = ners.getAdjustedUT1Time();
						final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
						final String formattedString = localDateTime.format(formatter);
						updateTimeClockLabel(lblTimeLabel, formattedString, ners.getUT1Precision(), "UT1");
						lblTimeLabel.setToolTipText("Universal Time Version 1");
						if (!cycle) {
							break;
						}
						TimeUnit.MILLISECONDS.sleep(CYCLE_DELAY_MILLISECONDS);
						count++;
						if (count >= 6 || !cycle) {
							count = 0;
							break;
						}
					}
					while (cycle && timeStandardCheckboxList.get(5).isSelected()) {
						final ZonedDateTime localDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(consolidatedTime.getBestTimeInMillis()), zoneId);
						final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
						final String formattedString = localDateTime.format(formatter);
						updateTimeClockLabel(lblTimeLabel, formattedString, consolidatedTime.getTimeStratum(), zoneId, locale);
						lblTimeLabel.setToolTipText(zoneId.getDisplayName(TextStyle.FULL, locale) + " " + " " + locale.getDisplayCountry(locale));
						if (!cycle) {
							break;
						}
						TimeUnit.MILLISECONDS.sleep(CYCLE_DELAY_MILLISECONDS);
						count++;
						if (count >= 6 || !cycle) {
							count = 0;
							break;
						}
					}
					while (cycle && timeStandardCheckboxList.get(6).isSelected()) {
						final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
						final String formattedString = ners.getAdjustedTAITime().format(formatter);
						updateTimeClockLabel(lblTimeLabel, formattedString, ners.getTAIPrecision(), "TAI");
						lblTimeLabel.setToolTipText("International Atomic Time");
						if (!cycle) {
							break;
						}
						TimeUnit.MILLISECONDS.sleep(CYCLE_DELAY_MILLISECONDS);
						count++;
						if (count >= 6 || !cycle) {
							count = 0;
							break;
						}
					}
				}
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		}
	}
	
	public void setPanelName(String panelName) {
		this.panelName = panelName;
	}

	private void updateComponents() {
		lblTimeLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblTimeLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblTimeLabel.setText("Time");
		lblTimeLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		lblTimeLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		lblTimeLabel.setOpaque(true);

		timeStandardCheckboxList.get(0).setText("Local Apparent Sidereal Time");
		timeStandardCheckboxList.get(1).setText("Greenwich Mean Sidereal Time");
		timeStandardCheckboxList.get(2).setText("Greenwich Apparent Sidereal Time");
		timeStandardCheckboxList.get(3).setText("Universal Coordinated Time");
		timeStandardCheckboxList.get(4).setText("Universal Time Version 1");
		timeStandardCheckboxList.get(5).setText("Local Solar Time");
		timeStandardCheckboxList.get(6).setText("International Atomic Time");
	}

	public void saveSettings() {
		userPrefs.put("Locale", locale.getDisplayName());
		
		userPrefs.put("ZoneId", zoneId.getId());
		
		for (int i = 0; i < timeStandardCheckboxList.size(); i++) {
			userPrefs.putBoolean("TimeStandardCheckboxListElement_" + i, timeStandardCheckboxList.get(i).isSelected());
		}
	}

	@Override
    public void close() {
		saveSettings();
		stopTimeStandardRotate();
		for (PropertyChangeListener pcl : pcs.getPropertyChangeListeners()) {
			pcs.removePropertyChangeListener(pcl);
		}
    }

	private void initializeListeners() {
		consolidatedTime.addPropertyChangeListener(event -> {
			if (ConsolidatedTime.FAIL.equals(event.getPropertyName())) {
				final ZonedDateTime zdt = ZonedDateTime.now();
				final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
				final String formattedString = zdt.format(formatter);
				updateTimeClockLabel(lblTimeLabel, formattedString, ConsolidatedTime.STRATUM_UNSYNC, consolidatedTime.getLocalZoneId(), consolidatedTime.getLocale());
			}
		});
	}

	public void updateTimeClockLabel(JLabel timeLabel, String timeString, int timeStrata, ZoneId zoneId, Locale locale) {
		invokeLaterInDispatchThreadIfNeeded(() -> {
			final String zoneIdName = ConsolidatedTime.getZoneIdDisplayShortWithDST(zoneId, locale);
			if (timeStrata == ConsolidatedTime.STRATUM_GPS) {
				timeLabel.setText(timeString + " " + zoneIdName + " Strata GPS");
				timeLabel.setBackground(Color.BLUE);
				timeLabel.setForeground(Color.WHITE);
			} else if (timeStrata >= ConsolidatedTime.STRATUM_NTP0) {
				timeLabel.setText(timeString + " " + zoneIdName + " Strata NTP" + timeStrata);
				timeLabel.setBackground(Color.BLUE);
				timeLabel.setForeground(Color.WHITE);
			} else if (timeStrata >= ConsolidatedTime.STRATUM_NTP1 && timeStrata <= ConsolidatedTime.STRATUM_NTP3) {
				timeLabel.setText(timeString + " " + zoneIdName + " Strata NTP" + timeStrata);
				timeLabel.setBackground(Color.GREEN);
				timeLabel.setForeground(Color.BLACK);
			} else if (timeStrata >= ConsolidatedTime.STRATUM_NTP4 && timeStrata <= ConsolidatedTime.STRATUM_NTP15) {
				timeLabel.setText(timeString + " " + zoneIdName + " Strata NTP" + timeStrata);
				timeLabel.setBackground(Color.YELLOW);
				timeLabel.setForeground(Color.BLACK);	
			} else if (timeStrata == ConsolidatedTime.STRATUM_UNSYNC) {
				timeLabel.setText(timeString + " " + zoneIdName + " STA Clock");
				timeLabel.setBackground(Color.ORANGE);
				timeLabel.setForeground(Color.RED);
			} else {
				timeLabel.setText(timeString + " " + zoneIdName + " Error");
				timeLabel.setBackground(Color.RED);
				timeLabel.setForeground(Color.GRAY);
			}
		});
	}
	
	public void updateTimeClockLabel(JLabel timeLabel, String timeString, int timeStrata, String standard) {
		invokeLaterInDispatchThreadIfNeeded(() -> {
			if ((timeStrata == ConsolidatedTime.STRATUM_GPS) || (timeStrata == ConsolidatedTime.STRATUM_NTP0)) {
				timeLabel.setText(timeString + " " + standard + " Strata GPS");
				timeLabel.setBackground(Color.BLUE);
				timeLabel.setForeground(Color.WHITE);
			} else if ((timeStrata >= ConsolidatedTime.STRATUM_NTP1) && (timeStrata <= ConsolidatedTime.STRATUM_NTP3)) {
				timeLabel.setBackground(Color.GREEN);
				timeLabel.setForeground(Color.WHITE);
				timeLabel.setText(timeString + " " + standard + " Strata NTP" + timeStrata);
			} else if ((timeStrata >= ConsolidatedTime.STRATUM_NTP4) && (timeStrata <= ConsolidatedTime.STRATUM_NTP15)) {
				timeLabel.setBackground(Color.YELLOW);
				timeLabel.setForeground(Color.WHITE);
				timeLabel.setText(timeString + " " + standard + " Strata NTP" + timeStrata);
			} else if (timeStrata == ConsolidatedTime.STRATUM_UNSYNC) {
				timeLabel.setBackground(Color.MAGENTA);
				timeLabel.setForeground(Color.WHITE);
				timeLabel.setText(timeString + " " + standard + " STA Clock");
			} else {
				timeLabel.setBackground(Color.RED);
				timeLabel.setForeground(Color.GRAY);
				timeLabel.setText(timeString + " " + standard + " Error");
			}
		});
	}

	public void updateTimeClockLabel(JLabel timeLabel, String timeString, Precision precision, String standard) {
		invokeLaterInDispatchThreadIfNeeded(() -> {
			final String text = timeString + " " + standard + " ";
			if (precision.equals(Precision.NERS_FAIL)) {
				timeLabel.setText(text + "ESTIMATED"); 
				timeLabel.setBackground(Color.GREEN);
				timeLabel.setForeground(Color.WHITE);
			} else if (precision.equals(Precision.HIGH)) {
				timeLabel.setText(text + "High Precision"); 
				timeLabel.setBackground(Color.BLUE);
				timeLabel.setForeground(Color.WHITE);
			} else if (precision.equals(Precision.MEDIUM)) {
				timeLabel.setText(text + "Med Precision");
				timeLabel.setBackground(Color.ORANGE);
				timeLabel.setForeground(Color.BLACK);
			} else if (precision.equals(Precision.LOW)) {
				timeLabel.setText(text + "Low Precision");
				timeLabel.setBackground(Color.MAGENTA);
				timeLabel.setForeground(Color.BLACK);
			} else {
				timeLabel.setText(text + "Unsync");
				timeLabel.setBackground(Color.RED);
				timeLabel.setForeground(Color.BLACK);
			}
		});
	}
	
	public void addPropertyChangeListener(final PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(final PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}

	public PropertyChangeSupport getPropertyChangeSupport() {
		return pcs;
	}

	public JPanel[] getSettingsPanelArray() {
		return new JPanel[] {getConfigGUI()};
	}

	public JPanel getTimePanelFoundation7InchDisplay() {
		final JPanel panel = new JPanel();

		panel.setBorder(BorderFactory.createTitledBorder(null, "Time", TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION, new Font("Tahoma", Font.BOLD, 12)));
		
		final GroupLayout layout = new GroupLayout(panel);

		panel.setLayout(layout);

		lblTimeLabel.setFont(new Font("Tahoma", Font.BOLD, 16));
		
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
				.addGap(1, 1, 1)
				.addComponent(lblTimeLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addGap(1, 1, 1))
		);

		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
				.addGap(1, 1, 1)
				.addComponent(lblTimeLabel, 26, 26, 26)
				.addGap(1, 1, 1))
		);

		return panel;
	}
	
	public JPanel getTimePanel() {
		final JPanel panel = new JPanel();

		panel.setBorder(BorderFactory.createTitledBorder(null, "Time", TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION, new Font("Tahoma", 1, 14)));
		
		final GroupLayout layout = new GroupLayout(panel);

		panel.setLayout(layout);

		lblTimeLabel.setFont(new Font("Tahoma", Font.BOLD, 16));
		
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
				.addContainerGap()
				.addComponent(lblTimeLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addContainerGap())
		);

		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
				.addContainerGap()
				.addComponent(lblTimeLabel, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
				.addContainerGap())
		);

		return panel;
	}

	private JPanel getSelectorPanel() {
		final JPanel panel = new JPanel();
		
		final GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
        
        panel.setBorder(BorderFactory.createTitledBorder("Time Panel Selector"));
        
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            		.addComponent(timeStandardCheckboxList.get(0))
            		.addComponent(timeStandardCheckboxList.get(1))
    				.addComponent(timeStandardCheckboxList.get(2))
    				.addComponent(timeStandardCheckboxList.get(3))
    				.addComponent(timeStandardCheckboxList.get(4))
    				.addComponent(timeStandardCheckboxList.get(5))
    				.addComponent(timeStandardCheckboxList.get(6)))
                .addContainerGap(10, Short.MAX_VALUE))
        );
	
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(timeStandardCheckboxList.get(0))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(timeStandardCheckboxList.get(1))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(timeStandardCheckboxList.get(2))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(timeStandardCheckboxList.get(3))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(timeStandardCheckboxList.get(4))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(timeStandardCheckboxList.get(5))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(timeStandardCheckboxList.get(6))
                .addGap(14, 14, 14))
        );
		
		return panel;
	}
	
	private JPanel getConfigGUI() {
		final JPanel selectorPanel = getSelectorPanel();
		
		final JPanel panel = new JPanel();

		panel.setName(panelName);

		final GroupLayout layout = new GroupLayout(panel);

		panel.setLayout(layout);

		layout.setHorizontalGroup(
	            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
	            .addGroup(layout.createSequentialGroup()
	                .addGap(80, 80, 80)
	                .addComponent(selectorPanel)
	                .addContainerGap(286, Short.MAX_VALUE))
	        );
	        layout.setVerticalGroup(
	            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(layout.createSequentialGroup()
	                .addGap(20, 20, 20)
	                .addComponent(selectorPanel)
	                .addContainerGap(160, Short.MAX_VALUE))
	        );

		return panel;
	}

	private void invokeLaterInDispatchThreadIfNeeded(Runnable runnable) {
		if (EventQueue.isDispatchThread()) {
			runnable.run();
		} else {
			SwingUtilities.invokeLater(runnable);
		}
	}
}
