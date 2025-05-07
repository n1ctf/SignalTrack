package aprs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import java.awt.event.ItemEvent;

import java.awt.geom.Point2D;

import java.beans.PropertyChangeSupport;

import java.lang.reflect.InvocationTargetException;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import java.util.logging.Level;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;

import org.apache.commons.collections4.BidiMap;

import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import meteorology.AbstractEnvironmentSensor;

import tty.AbstractTeletypeController;
import tty.SerialComponent;

public abstract class AbstractTerminalNodeClient extends AbstractAPRSProcessor {

	public static final String DEFAULT_ABBREVIATED_TAG = "RF";
	public static final String DEFAULT_TAG = "RF-TNC";
	public static final boolean DEFAULT_DEBUG = true;
	public static final String CRLF = "\r\n";
	public static final String SETTINGS_TITLE_PREFIX = "TNC";
	
	private static final Dimension DEFAULT_BUTTON_DIM = new Dimension(200, 18);
	private static final String DEFAULT_FONT = "Arial";
	
	private static final String DEFAULT_SOFTWARE_VERSION = "SignalTrack-1.9.3";
	private static final Preferences userPrefs = Preferences.userRoot().node(AbstractTerminalNodeClient.class.getName());
	private static Locale locale;

	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	
	private JComboBox<String> deviceComboBox;
	private DefaultComboBoxModel<String> deviceComboBoxModel;
	
	private String softwareVersion = DEFAULT_SOFTWARE_VERSION;

	private String htmlString;
	private String updateString;
	
	private JButton ttyPortSettingsButton;
	private JButton ttyErrorNotificationSettingsButton;
	private JButton ttyEventManagementSettingsButton;
	
	private JPanel ttySettingsPanel;
	
	private AbstractEnvironmentSensor aes;
	private AbstractTeletypeController tty;

	private String rStr = "";
	private String gpwplMessageString;
	private String pkwdwplMessageString;

	public abstract void saveClientSettings();
	public abstract boolean isRFPortOpen();
	public abstract void sendUpdate(String string);
	public abstract long getSerialVersionUID();
	
	protected AbstractTerminalNodeClient(AbstractEnvironmentSensor aes, boolean clearAllPreferences) {
		super(aes);
		
		this.aes = aes;
		
		if (clearAllPreferences) {
			try {
				AbstractTerminalNodeClient.userPrefs.clear();
			} catch (final BackingStoreException ex) {
				if (isDebug()) {
					LOG.log(Level.WARNING, ex.getMessage(), ex);
				}
			}
		}	
		
		tty = AbstractTeletypeController.getTTyPortInstance(AbstractTeletypeController.getCatalogMap().getKey("JSSC TTY Port v2.9.5"), false);
		
		ttySettingsPanel = new JPanel();
		ttySettingsPanel.setBorder(BorderFactory.createEtchedBorder());
		ttySettingsPanel.setOpaque(true);
		
		deviceComboBoxModel = new DefaultComboBoxModel<>();
		deviceComboBox = new JComboBox<>(deviceComboBoxModel);
		
		loadDeviceComboBox();
		
		ttyPortSettingsButton = new JButton();
		
		ttyPortSettingsButton.setText("TTY Port Settings");
		ttyPortSettingsButton.setPreferredSize(DEFAULT_BUTTON_DIM);
		ttyPortSettingsButton.setFont(new Font(DEFAULT_FONT, Font.BOLD, 12));
		ttyPortSettingsButton.setForeground(Color.BLACK);
		ttyPortSettingsButton.setRolloverEnabled(true);
		ttyPortSettingsButton.setFocusable(false);
		ttyPortSettingsButton.setMultiClickThreshhold(50L);
		ttyPortSettingsButton.setSelected(true);
		
		ttyErrorNotificationSettingsButton = new JButton();
		
		ttyErrorNotificationSettingsButton.setText("TTY Error Notification Settings");
		ttyErrorNotificationSettingsButton.setPreferredSize(DEFAULT_BUTTON_DIM);
		ttyErrorNotificationSettingsButton.setFont(new Font(DEFAULT_FONT, Font.BOLD, 12));
		ttyErrorNotificationSettingsButton.setForeground(Color.BLACK);
		ttyErrorNotificationSettingsButton.setRolloverEnabled(true);
		ttyErrorNotificationSettingsButton.setFocusable(false);
		ttyErrorNotificationSettingsButton.setMultiClickThreshhold(50L);
		
		ttyEventManagementSettingsButton = new JButton();
		
		ttyEventManagementSettingsButton.setText("TTY Event Management Settings");
		ttyEventManagementSettingsButton.setPreferredSize(DEFAULT_BUTTON_DIM);
		ttyEventManagementSettingsButton.setFont(new Font(DEFAULT_FONT, Font.BOLD, 12));
		ttyEventManagementSettingsButton.setForeground(Color.BLACK);
		ttyEventManagementSettingsButton.setRolloverEnabled(true);
		ttyEventManagementSettingsButton.setFocusable(false);
		ttyEventManagementSettingsButton.setMultiClickThreshhold(50L);
		
		ttySettingsPanel.add(getTTYConfigurationComponentArray()[0]);
		
		initListeners();
	}
	
	private void loadDeviceComboBox() {
		final String[] clientList = getCatalogMap().values().toArray(new String[getCatalogMap().values().toArray().length]);
        deviceComboBox.removeAllItems();
        deviceComboBoxModel.addAll(Arrays.asList(clientList));
        deviceComboBox.setSelectedItem(getCatalogMap().get(String.valueOf(getClassName())));
        deviceComboBox.addItemListener(event -> {
            if (event.getStateChange() == ItemEvent.SELECTED) {
                final JComboBox<?> cb = (JComboBox<?>) event.getSource();
                final String className = AbstractTerminalNodeClient.getCatalogMap().getKey(cb.getSelectedItem());
                saveClassName(className);
                JOptionPane.showMessageDialog(new JDialog(), "Change to APRS TNC Will Take Place Upon Next Restart",
    	    	 	"Notification", JOptionPane.INFORMATION_MESSAGE);
            }
        });
	}

	private void initListeners() {
		ttyPortSettingsButton.addActionListener(_ -> {
			ttySettingsPanel.removeAll();
			ttySettingsPanel.add(getTTYConfigurationComponentArray()[0]);
			ttySettingsPanel.setVisible(true);
			ttySettingsPanel.revalidate();
		});
		ttyErrorNotificationSettingsButton.addActionListener(_ -> {
			ttySettingsPanel.removeAll();
			ttySettingsPanel.add(getTTYConfigurationComponentArray()[1]);
			ttySettingsPanel.setVisible(true);
			ttySettingsPanel.revalidate();
		});
		ttyEventManagementSettingsButton.addActionListener(_ -> {
			ttySettingsPanel.removeAll();
			ttySettingsPanel.add(getTTYConfigurationComponentArray()[2]);
			ttySettingsPanel.setVisible(true);
			ttySettingsPanel.revalidate();
		});
	}

	@Override
	public String getClassName() {
		return this.getClass().getName();
	}
	
	public static BidiMap<String, String> getCatalogMap() {
		final BidiMap<String, String> catalog = new DualHashBidiMap<>();
		catalog.put("aprs.ByonicsTT4", "Byonics TinyTrak IV");
		catalog.put("aprs.KenwoodTMD710A", "Kenwood TM-D710A Dual Band Transceiver");
		return catalog;
	}

	public AbstractTeletypeController getTTY() {
		return tty;
	}
	
	public String getSoftwareVersion() {
		return softwareVersion;
	}

	public void setSoftwareVersion(String softwareVersion) {
		this.softwareVersion = softwareVersion;
	}

	public static Locale getLocale() {
		return locale;
	}

	public static void setLocale(Locale locale) {
		AbstractTerminalNodeClient.locale = locale;
	}
	
	public PropertyChangeSupport getAPRSPropertyChangeSupport() {
		return pcs;
	}

	public boolean isDebug() {
		return DEFAULT_DEBUG;
	}

	@Override
	public JPanel getSettingsPanel() {
		final JPanel panel = new JPanel();

		final GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		final JLabel deviceComboBoxLabel = new JLabel("Terminal Node Controller");
		deviceComboBoxLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(ttySettingsPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(ttyPortSettingsButton)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(ttyErrorNotificationSettingsButton)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(ttyEventManagementSettingsButton))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(deviceComboBoxLabel, GroupLayout.PREFERRED_SIZE, 180, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(deviceComboBox, GroupLayout.PREFERRED_SIZE, 226, GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 20, Short.MAX_VALUE)))
                .addContainerGap()));
	
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(deviceComboBoxLabel)
                    .addComponent(deviceComboBox))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(ttyPortSettingsButton)
                    .addComponent(ttyErrorNotificationSettingsButton)
                    .addComponent(ttyEventManagementSettingsButton))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ttySettingsPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap()));
	        
		return panel;
	}
	
	@Override
	public void close() {
		super.close();
		if (executor != null) {
			try {
				LOG.log(Level.INFO, "Initializing AbstractAPRSProcessor.executor termination....");
				executor.shutdown();
				executor.awaitTermination(5, TimeUnit.SECONDS);
				LOG.log(Level.INFO, "AbstractAPRSProcessor.executor has gracefully terminated");
			} catch (InterruptedException e) {
				executor.shutdownNow();
				LOG.log(Level.SEVERE, "AbstractAPRSProcessor.executor has timed out after 5 seconds of waiting to terminate processes.");
				Thread.currentThread().interrupt();
			}
		}
	}

	public static String getTNCClassNameFor(String compositeName) {
		return getTNCClassNameFor(compositeName, compositeName);
	}
	
	public static String getTNCClassNameFor(String manufacturer, String model) {
		String className = (String) AbstractTerminalNodeClient.getCatalogMap().keySet().toArray()[0];
		try {
			final Iterator<String> iterator = AbstractTerminalNodeClient.getCatalogMap().keySet().iterator();
			while (iterator.hasNext()) {
				final String aprsReceiverElement = iterator.next();
			    if (manufacturer.toUpperCase(getLocale()).trim().contains(aprsReceiverElement.toUpperCase(getLocale()).trim())
						&& model.toUpperCase(getLocale()).trim().contains(aprsReceiverElement.toUpperCase(getLocale()).trim())) {
					className = aprsReceiverElement;
					break;
				}
			}
		} catch (NullPointerException ex) {
			LOG.log(Level.SEVERE, "getTNCInstanceFor(String manufacturer, String model) returns NULL", ex);
		}
		return className;
	}

	public static AbstractTerminalNodeClient getTNCInstance(StringBuilder className, AbstractEnvironmentSensor aes, Boolean clearAllPreferences) {
		return getTNCInstance(className.toString(), aes, clearAllPreferences);
	}

	public static AbstractTerminalNodeClient getTNCInstance(String className, AbstractEnvironmentSensor aes, Boolean clearAllPreferences) {
		boolean isValidClassName = false;
		final Iterator<String> iterator = AbstractTerminalNodeClient.getCatalogMap().keySet().iterator();
		while (iterator.hasNext()) {
			final String cn = iterator.next();
			if (cn.equals(className)) {
				isValidClassName = true;
				break;
			}
		}
		final Class<?> classTemp;
		AbstractTerminalNodeClient instance = null;
		try {
			if (!isValidClassName) {
				className = (String) AbstractTerminalNodeClient.getCatalogMap().keySet().toArray()[0];
			}
			classTemp = Class.forName(className);
			final Class<?>[] cArg = new Class<?>[2];
			cArg[0] = AbstractEnvironmentSensor.class;
			cArg[1] = Boolean.class;
			instance = (AbstractTerminalNodeClient) classTemp.getDeclaredConstructor(cArg).newInstance(aes, clearAllPreferences);
		} catch (InstantiationException e) {
			LOG.log(Level.WARNING, "InstantiationException", e);
		} catch (IllegalAccessException e) {
			LOG.log(Level.WARNING, "IllegalAccessException", e);
		} catch (IllegalArgumentException e) {
			LOG.log(Level.WARNING, "IllegalArgumentException", e);
		} catch (InvocationTargetException e) {
			LOG.log(Level.WARNING, "InvocationTargetException", e);
		} catch (NoSuchMethodException e) {
			LOG.log(Level.WARNING, "NoSuchMethodException", e);
		} catch (SecurityException e) {
			LOG.log(Level.WARNING, "SecurityException", e);
		} catch (ClassNotFoundException e) {
			LOG.log(Level.WARNING, "ClassNotFoundException", e);
		}
		return instance;
	}

	public static String getIconPathNameFromSSID(final String ssid) {
		final String sRet;
		switch (ssid) {
			case "0" -> sRet = "SSID-00.png";
			case "1" -> sRet = "SSID-01.png";
			case "2" -> sRet = "SSID-02.png";
			case "3" -> sRet = "SSID-03.png";
			case "4" -> sRet = "SSID-04.png";
			case "5" -> sRet = "SSID-05.png";
			case "6" -> sRet = "SSID-06.png";
			case "7" -> sRet = "SSID-07.png";
			case "8" -> sRet = "SSID-08.png";
			case "9" -> sRet = "SSID-09.png";
			case "10" -> sRet = "SSID-10.png";
			case "11" -> sRet = "SSID-11.png";
			case "12" -> sRet = "SSID-12.png";
			case "13" -> sRet = "SSID-13.png";
			case "14" -> sRet = "SSID-14.png";
			case "15" -> sRet = "SSID-15.png";
			default -> sRet = "SSID-00.png";
		}
		return sRet;
	}
	
	public void saveClassName(String className) {
		pcs.firePropertyChange(Event.CLASS_NAME.name(), null, className);
	}
	
	public JPanel[] getTTYConfigurationComponentArray() {
		return new SerialComponent(tty).getSettingsPanelArray(SETTINGS_TITLE_PREFIX);
	}
	
	@Override
	public boolean sendUpdate() {
		final AprsWeatherPositGenerator wpg = new AprsWeatherPositGenerator(aes);
		updateString = wpg.getAprsTNCWeatherReportString();
		htmlString = wpg.getAprsTNCHTMLString();
		sendUpdate(updateString);
		return true;
	}
	
	@Override
	public String getAbbreviatedTag() {
		return DEFAULT_ABBREVIATED_TAG;
	}
	
	@Override
	public String getTag() {
		return DEFAULT_TAG;
	}

	@Override
	public String getUpdateString() {
		return updateString;
	}

	@Override
	public String getHTMLString() {
		return htmlString;
	}

	public String getPkwdwplMessageString() {
		return pkwdwplMessageString;
	}

	public String getGpwplMessageString() {
		return gpwplMessageString;
	}

	public void setGpwplMessageString(String gpwplMessageString) {
		this.gpwplMessageString = gpwplMessageString;
	}

	public void setPkwdwplMessageString(String pkwdwplMessageString) {
		this.pkwdwplMessageString = pkwdwplMessageString;
	}

	public void processNMEAData(String data) {
		int iStart;
		int iEnd;
		int iTemp;
		rStr += data;
		while (!rStr.isEmpty()) {
			iStart = rStr.indexOf('$', 0);
			if (iStart >= 0) {
				iTemp = rStr.indexOf('*', iStart + 1);
				iEnd = iTemp > 0 ? iTemp + 3 : 0;
			} else {
				iEnd = 0;
			}
			if ((iStart >= 0) && (rStr.length() >= iEnd) && (iEnd > 0)) {
				executor.execute(new NmeaDecoder(rStr.substring(iStart, iEnd)));
				rStr = rStr.length() > (iEnd + 1) ? rStr.substring(iEnd + 1) : "";
			} else {
				break;
			}
		}
	}

	public class NmeaDecoder implements Runnable {

		private final String message;

		public NmeaDecoder(String message) {
			this.message = message;
		}

		@Override
		public void run() {
			pcs.firePropertyChange(Event.NMEA_DATA.name(), null, message);

			if ("$".equals(message.substring(0, 1))) {
				if (!checksum(message)) {
					pcs.firePropertyChange(Event.CRC_ERROR.name(), null, message);
				} else {
					final String completeMsg = message;
					final String[] a = message.substring(0, message.indexOf('*')).split(",");

					switch (a[0]) {
						case "$GPWPL" -> {
							setGpwplMessageString(completeMsg);
							final Point2D aprsPosition;
							final String callSign;
							final String ssid;
							try {
								double lat;
								double lon;
	
								lat = (Double.parseDouble(a[1].substring(0, 2)) * 1000000.0)
										+ (Double.parseDouble(a[1].substring(2)) * 16666.6666667);
	
								if ("S".equals(a[2])) {
									lat = -lat;
								}
	
								lon = (Double.parseDouble(a[3].substring(0, 3)) * 1000000.0)
										+ (Double.parseDouble(a[3].substring(3)) * 16666.6666667);
	
								if ("W".equals(a[4])) {
									lon = -lon;
								}
	
								aprsPosition = new Point2D.Double(lon / 1000000.0, lat / 1000000.0);
	
								if (!a[5].isEmpty()) {
									final String[] station = a[5].split("-");
									callSign = station[0];
									ssid = station.length > 1 ? station[1] : "";
									final AprsIcon aprsIcon = new AprsIconImpl(Calendar.getInstance(), aprsPosition, callSign, ssid);
									pcs.firePropertyChange(Event.GPWPL_WAYPOINT_REPORT.name(), null, aprsIcon);
								}
	
							} catch (StringIndexOutOfBoundsException | ArrayIndexOutOfBoundsException
									| NumberFormatException ex) {
								if (isDebug()) {
									LOG.log(Level.WARNING, ex.getMessage(), ex);
								}
							}
						}

						case "$PKWDWPL" -> {
							// Kenwood Standard APRS Sentence
							// example:
							// $PKWDWPL,053046,V,3953.79,N,08242.93,W,1,142,280320,000225,KM4TT-12,/j*6D
	
							final boolean validReport;
							final Calendar dateOfReport = Calendar.getInstance();
							Point2D aprsPosition = null;
							double aprsSpeedMadeGood = -1;
							int aprsCourseMadeGood = -1;
							double aprsAltitude = -1;
							final String callSign;
							final String ssid;
							final Character table;
							final Character symbol;
							
							try {
								setPkwdwplMessageString(completeMsg);
								dateOfReport.set(Calendar.HOUR_OF_DAY, Integer.parseInt(a[1].substring(0, 2)));
								dateOfReport.set(Calendar.MINUTE, Integer.parseInt(a[1].substring(2, 4)));
								dateOfReport.set(Calendar.SECOND, Integer.parseInt(a[1].substring(4, 6)));
								dateOfReport.set(Calendar.DATE, Integer.parseInt(a[9].substring(0, 2)));
								dateOfReport.set(Calendar.MONTH, Integer.parseInt(a[9].substring(2, 4)) - 1);
								dateOfReport.set(Calendar.YEAR, Integer.parseInt("20" + a[9].substring(4, 6)));
								dateOfReport.set(Calendar.MILLISECOND, Integer.parseInt(a[1].substring(7, 10)));
	
								validReport = (!"V".equals(a[2]));
	
								if (!a[3].isEmpty() && !a[5].isEmpty()) {
									double lat;
									double lon;
	
									lat = (Double.parseDouble(a[3].substring(0, 2)) * 1000000)
											+ (Double.parseDouble(a[3].substring(2)) * 16666.6666667);
	
									if ("S".equals(a[4])) {
										lat = -lat;
									}
	
									lon = (Double.parseDouble(a[5].substring(0, 3)) * 1000000)
											+ (Double.parseDouble(a[5].substring(3)) * 16666.6666667);
	
									if ("W".equals(a[6])) {
										lon = -lon;
									}
	
									if (validReport) {
										aprsPosition = new Point2D.Double(lon / 1000000, lat / 1000000);
									}
								}
	
								if (!a[7].isEmpty()) {
									aprsSpeedMadeGood = Double.parseDouble(a[7]) * 1.852;
								}
								if (!a[8].isEmpty()) {
									aprsCourseMadeGood = Integer.parseInt(a[8]);
								}
								if (!a[10].isEmpty()) {
									aprsAltitude = Float.parseFloat(a[10]);
								}
	
								if (!a[11].isEmpty()) {
									final String[] station = a[11].split("-");
									callSign = station[0];
									ssid = station.length > 1 ? station[1] : "";
									table = a[12].charAt(0);
									symbol = a[12].charAt(1);
									final AprsIcon aprsIcon = new AprsIconImpl(dateOfReport, aprsPosition, aprsSpeedMadeGood,
											aprsCourseMadeGood, aprsAltitude, callSign, ssid, table, symbol);
	
									if (validReport) {
										pcs.firePropertyChange(Event.PKWDWPL_APRS_REPORT.name(), null, aprsIcon);
									}
								}
							} catch (StringIndexOutOfBoundsException | ArrayIndexOutOfBoundsException
									| NumberFormatException ex) {
								if (isDebug()) {
									LOG.log(Level.WARNING, ex.getMessage(), ex);
								}
							}
						}
						
						default -> {
							if (isDebug()) {
								LOG.log(Level.WARNING, "Unsupported NMEA sentence: {0}", completeMsg);
							}
						}
					}
				}
			}
		}
	}

	public synchronized boolean checksum(String input) {
		final String chkDat;
		final String[] dat;
		String chkSum;

		if (input.indexOf('*', 2) < 2) {
			pcs.firePropertyChange(Event.CRC_ERROR.name(), null, input);
			return false;
		} else {
			try {
				chkDat = input.substring(1, input.indexOf('*'));

				dat = input.split(",");

				chkSum = dat[dat.length - 1];
				chkSum = chkSum.substring(chkSum.indexOf('*') + 1);

				int s = chkDat.charAt(0);

				for (int i = 1; i < chkDat.length(); i++) {
					s = s ^ chkDat.charAt(i);
				}

				if (s == Integer.valueOf(chkSum, 16).intValue()) {
					return true;
				} else {
					pcs.firePropertyChange(Event.CRC_ERROR.name(), null, input);
				}
				return false;
			} catch (final NumberFormatException ex) {
				pcs.firePropertyChange(Event.CRC_ERROR.name(), null, input);
				return false;
			}
		}
	}
}
