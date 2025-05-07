package gov.nasa.api.donki;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.Font;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import java.util.ArrayList;
import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;

import javax.swing.border.TitledBorder;

public class NASASpaceWeatherGUI {
	private static final Logger LOG = Logger.getLogger(NASASpaceWeatherGUI.class.getName());
	private static final int defaultDismissTimeout = ToolTipManager.sharedInstance().getDismissDelay();
	private static final int defaultDismissDelay = 60000;
	
	public static final String DEFAULT_NASA_DONKI_URL_STRING = "https://webtools.ccmc.gsfc.nasa.gov/DONKI/";
	public static final boolean DEFAULT_DECORATED = false;
	
	public enum Style {
		SINGLE_COLUMN,
		SINGLE_COLUMN_NO_TIMEOUT_DISPLAY,
		DUAL_COLUMN
	}

	private List<JLabel> flag;
	private List<URL> eventLink;

	private String defaultNasaDonkiUrlString = DEFAULT_NASA_DONKI_URL_STRING;
	
	public NASASpaceWeatherGUI() {
		initializeComponents();
	}
	
	private void initializeComponents() {
		invokeLaterInDispatchThreadIfNeeded(() -> {
			final List<MouseAdapter> mouseAdapter = new ArrayList<>(8);
			
			flag = new ArrayList<>(8);
			
			eventLink = new ArrayList<>(8);
			
			for (int i = 0; i < 8; i++) {
				try {
					eventLink.add(new URI(defaultNasaDonkiUrlString).toURL());
				} catch (URISyntaxException | MalformedURLException ex) {
					LOG.log(Level.WARNING, null, ex);
				}
			}
			
			for (int i = 0; i < 8; i++) {
				final int ii = i;
				final JLabel label = new JLabel();
				label.setFont(new Font("Tahoma", Font.PLAIN, 10));
				label.setBorder(BorderFactory.createLineBorder(Color.GRAY));
				label.setOpaque(true);
				label.setHorizontalAlignment(SwingConstants.CENTER);
				label.setVerticalAlignment(SwingConstants.CENTER);
				
				mouseAdapter.add(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent evt) {
						if (evt.getClickCount() == 2) {
							try {
								Desktop.getDesktop().browse(eventLink.get(ii).toURI());
							} catch (NullPointerException ex) {
								LOG.log(Level.SEVERE, "NO URL available", ex);
							} catch (IOException | URISyntaxException ex) {
								LOG.log(Level.SEVERE, null, ex);
							}
						}
					}
					@Override
					public void mouseEntered(MouseEvent me) {
					    ToolTipManager.sharedInstance().setDismissDelay(defaultDismissDelay);
					}
					@Override
					public void mouseExited(MouseEvent me) {
					    ToolTipManager.sharedInstance().setDismissDelay(defaultDismissTimeout);
					  }
				});
				
				label.addMouseListener(mouseAdapter.get(i));
				flag.add(label);
			}
		});
	}

	public void setDefaultNasaDonkiUrlString(String defaultNasaDonkiUrlString) {
		this.defaultNasaDonkiUrlString = defaultNasaDonkiUrlString;
	}

	public void setLblFlareFlag(JLabel lblFlareFlag) {
		invokeLaterInDispatchThreadIfNeeded(() -> flag.set(0, lblFlareFlag));
	}

	public void setLblCMEEventFlag(JLabel lblCMEEventFlag) {
		invokeLaterInDispatchThreadIfNeeded(() -> flag.set(1, lblCMEEventFlag));
	}
	
	public void setLblGeomagneticStormFlag(JLabel lblGeomagneticStormFlag) {
		invokeLaterInDispatchThreadIfNeeded(() -> flag.set(2, lblGeomagneticStormFlag));
	}
	
	public void setLblSolarEnergeticParticleFlag(JLabel lblSolarEnergeticParticleFlag) {
		invokeLaterInDispatchThreadIfNeeded(() -> flag.set(3, lblSolarEnergeticParticleFlag));
	}

	public void setLblInterplanetaryShockFlag(JLabel lblInterplanetaryShockFlag) {
		invokeLaterInDispatchThreadIfNeeded(() -> flag.set(4, lblInterplanetaryShockFlag));
	}
	
	public void setLblRadiationBeltEnhancementFlag(JLabel lblRadiationBeltEnhancementFlag) {
		invokeLaterInDispatchThreadIfNeeded(() -> flag.set(5, lblRadiationBeltEnhancementFlag));
	}
	
	public void setLblHighSpeedStreamFlag(JLabel lblHighSpeedStreamFlag) {
		invokeLaterInDispatchThreadIfNeeded(() -> flag.set(6, lblHighSpeedStreamFlag));
	}
	
	public void setLblMagnetopauseCrossingFlag(JLabel lblMagnetopauseCrossingFlag) {
		invokeLaterInDispatchThreadIfNeeded(() -> flag.set(7, lblMagnetopauseCrossingFlag));
	}
	
	public void setFlareEventLink(URL url) {
		eventLink.set(0, url);
	}
	
	public void setCMEEventLink(URL url) {
		eventLink.set(1, url);
	}
		
	public void setGSTEventLink(URL url) {
		eventLink.set(2, url);
	}
	
	public void setSEPEventLink(URL url) {
		eventLink.set(3, url);
	}
	
	public void setIPSEventLink(URL url) {
		eventLink.set(4, url);
	}
	
	public void setRBEEventLink(URL url) {
		eventLink.set(5, url);
	}

	public void setHSSEventLink(URL url) {
		eventLink.set(6, url);
	}
		
	public void setMPCEventLink(URL url) {
		eventLink.set(7, url);
	}
		
	public void setFlareFlag(Color fg, Color bg, String text) {
		invokeLaterInDispatchThreadIfNeeded(() -> {
			flag.get(0).setForeground(fg);
			flag.get(0).setBackground(bg);
			flag.get(0).setText(text);
		});
	}
	
	public void setCMEEventFlag(Color fg, Color bg, String text) {
		invokeLaterInDispatchThreadIfNeeded(() -> {
			flag.get(1).setForeground(fg);
			flag.get(1).setBackground(bg);
			flag.get(1).setText(text);
		});
	}
	
	public void setGeomagneticStormFlag(Color fg, Color bg, String text) {
		invokeLaterInDispatchThreadIfNeeded(() -> {
			flag.get(2).setForeground(fg);
			flag.get(2).setBackground(bg);
			flag.get(2).setText(text);
		});
	}
	
	public void setSEPEventFlag(Color fg, Color bg, String text) {
		invokeLaterInDispatchThreadIfNeeded(() -> {
			flag.get(3).setForeground(fg);
			flag.get(3).setBackground(bg);
			flag.get(3).setText(text);
		});
	}

	public void setIPSEventFlag(Color fg, Color bg, String text) {
		invokeLaterInDispatchThreadIfNeeded(() -> {
			flag.get(4).setForeground(fg);
			flag.get(4).setBackground(bg);
			flag.get(4).setText(text);
		});
	}
		
	public void setRBEEventFlag(Color fg, Color bg, String text) {
		invokeLaterInDispatchThreadIfNeeded(() -> {
			flag.get(5).setForeground(fg);
			flag.get(5).setBackground(bg);
			flag.get(5).setText(text);
		});
	}
		
	public void setHSSEventFlag(Color fg, Color bg, String text) {
		invokeLaterInDispatchThreadIfNeeded(() -> {
			flag.get(6).setForeground(fg);
			flag.get(6).setBackground(bg);
			flag.get(6).setText(text);
		});
	}
	
	public void setMPCEventFlag(Color fg, Color bg, String text) {
		invokeLaterInDispatchThreadIfNeeded(() -> {
			flag.get(7).setForeground(fg);
			flag.get(7).setBackground(bg);
			flag.get(7).setText(text);
		});
	}
	
	public JLabel getFlag(int index) {
		return flag.get(index);
	}
	
	public List<JLabel> getFlag() {
		return new ArrayList<>(flag);
	}
	
	public void setFlag(List<JLabel> flag) {
		this.flag = new ArrayList<>(flag);
	}
	
	public JPanel getPanel(Style style) {
		final JPanel panel = new JPanel();
		
		final JPanel warningFlagPanel = getWarningFlagPanel(style);
		
		final GroupLayout layout = new GroupLayout(panel);
		
        layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		
		panel.setLayout(layout);

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
        		.addGap(2, 2, 2)
                .addComponent(warningFlagPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
	
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
        		.addGap(2, 2, 2)
                .addComponent(warningFlagPanel, GroupLayout.PREFERRED_SIZE, 180, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		return panel;
	}
	
	public JPanel getWarningFlagPanel(Style style) {
		final JPanel panel = new JPanel();
		
		final GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
		
        panel.setBorder(BorderFactory.createTitledBorder(null, "Solar Event Warnings",
				TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", 1, 14)));
		
        if (style == Style.DUAL_COLUMN) {
        	layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
    			.addGroup(layout.createSequentialGroup()
    				.addContainerGap()
    				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
    					.addComponent(flag.get(0), GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 320, GroupLayout.PREFERRED_SIZE)
    					.addComponent(flag.get(2), GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 320, GroupLayout.PREFERRED_SIZE)
    					.addComponent(flag.get(4), GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 320, GroupLayout.PREFERRED_SIZE)
    					.addComponent(flag.get(6), GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 320, GroupLayout.PREFERRED_SIZE))
    				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
    				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(flag.get(1), GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 320, GroupLayout.PREFERRED_SIZE)
    					.addComponent(flag.get(3), GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 320, GroupLayout.PREFERRED_SIZE)
    					.addComponent(flag.get(5), GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 320, GroupLayout.PREFERRED_SIZE)
    					.addComponent(flag.get(7), GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 320, GroupLayout.PREFERRED_SIZE))
    				.addContainerGap()));

    		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
    			.addGroup(layout.createSequentialGroup()
    				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
    					.addComponent(flag.get(0), GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE)
    					.addComponent(flag.get(1), GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE))
    				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
    				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
    					.addComponent(flag.get(2), GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE)
    					.addComponent(flag.get(3), GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE))
    				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
    				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
    					.addComponent(flag.get(4), GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE)
    					.addComponent(flag.get(5), GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE))
    				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
    				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
    					.addComponent(flag.get(6), GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE)
    					.addComponent(flag.get(7), GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE))
    				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        }
        
        if (style == Style.SINGLE_COLUMN) {
        	layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(2, 2, 2)
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
    					.addComponent(flag.get(0), 280, 320, Short.MAX_VALUE)
    					.addComponent(flag.get(1), 280, 320, Short.MAX_VALUE)
    					.addComponent(flag.get(2), 280, 320, Short.MAX_VALUE)
    					.addComponent(flag.get(3), 280, 320, Short.MAX_VALUE)
    					.addComponent(flag.get(4), 280, 320, Short.MAX_VALUE)
    					.addComponent(flag.get(5), 280, 320, Short.MAX_VALUE)
    					.addComponent(flag.get(6), 280, 320, Short.MAX_VALUE)
    					.addComponent(flag.get(7), 280, 320, Short.MAX_VALUE))
    				.addGap(2, 2, 2)));

    		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
    			.addGroup(layout.createSequentialGroup()
    				.addGap(2, 2, 2)	
    				.addComponent(flag.get(0), 18, 18, 18)
    				.addGap(2, 2, 2)
    				.addComponent(flag.get(1), 18, 18, 18)
    				.addGap(2, 2, 2)
    				.addComponent(flag.get(2), 18, 18, 18)
    				.addGap(2, 2, 2)
    				.addComponent(flag.get(3), 18, 18, 18)
    				.addGap(2, 2, 2)
    				.addComponent(flag.get(4), 18, 18, 18)
    				.addGap(2, 2, 2)
    				.addComponent(flag.get(5), 18, 18, 18)
    				.addGap(2, 2, 2)
    				.addComponent(flag.get(6), 18, 18, 18)
    				.addGap(2, 2, 2)
    				.addComponent(flag.get(7), 18, 18, 18)
    				.addGap(2, 2, 2)));
        }
        
        return panel;
	}
	
	public JPanel getWarningFlagPanelFoundation7InchDisplay() {
		final JPanel panel = new JPanel();
		
		final GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
		
        panel.setBorder(BorderFactory.createTitledBorder(null, "Solar Event Warnings",
				TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", 1, 12)));
	
    	layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addComponent(flag.get(0), GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
					.addComponent(flag.get(1), GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
					.addComponent(flag.get(2), GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
					.addComponent(flag.get(3), GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
					.addComponent(flag.get(4), GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
					.addComponent(flag.get(5), GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
					.addComponent(flag.get(6), GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
					.addComponent(flag.get(7), GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
				.addGap(2, 2, 2)));

		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
				.addGap(2, 2, 2)	
				.addComponent(flag.get(0), 18, 18, 18)
				.addGap(2, 2, 2)
				.addComponent(flag.get(1), 18, 18, 18)
				.addGap(2, 2, 2)
				.addComponent(flag.get(2), 18, 18, 18)
				.addGap(2, 2, 2)
				.addComponent(flag.get(3), 18, 18, 18)
				.addGap(2, 2, 2)
				.addComponent(flag.get(4), 18, 18, 18)
				.addGap(2, 2, 2)
				.addComponent(flag.get(5), 18, 18, 18)
				.addGap(2, 2, 2)
				.addComponent(flag.get(6), 18, 18, 18)
				.addGap(2, 2, 2)
				.addComponent(flag.get(7), 18, 18, 18)
				.addGap(2, 2, 2)));
        
        return panel;
	}

	private static void invokeLaterInDispatchThreadIfNeeded(Runnable runnable) {
		if (EventQueue.isDispatchThread()) {
			runnable.run();
		} else {
			SwingUtilities.invokeLater(runnable);
		}
	}
}
