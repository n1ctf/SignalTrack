package aprs;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import java.net.UnknownHostException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;

import network.NetworkParameterSet;

// Provides a GUI panel for user settings

public class AprsIsMultiAddressConfigurationComponent {
	private static final Logger LOG = Logger.getLogger(AprsIsMultiAddressConfigurationComponent.class.getName());
	
	private static final String DEFAULT_FONT = "Calabri";
	
	public static final int MAX_HOSTS = 12;
	
    private JTextField[] jtfHostName;
    private JLabel lblHostNameLeft;
    private JLabel lblHostNameRight;
    
    private JTextField[] jtfPort;
    private JLabel lblPortLeft;
    private JLabel lblPortRight;
    
    private JRadioButton[] rbSelect;
    private JLabel lblSelectLeft; 
    private JLabel lblSelectRight; 
    
    private JTextField jtfCallSign;
    private JLabel lblCallSignLabel; 
    
    private JTextField jtfPassword;
    private JLabel lblPasswordLabel;
    
    private JTextField jtfEquipmentId;
    private JLabel lblEquipmentIdLabel;
    
    private JButton jbResetDefaults;

    private RadioButtonHandler rbh;
    
    private final AprsIsClient aprsIsClient;

    private ButtonGroup selectButtonGroup;
    
    public AprsIsMultiAddressConfigurationComponent(AprsIsClient aprsIsClient) {
		this.aprsIsClient = aprsIsClient;
    	
    	if (aprsIsClient.getNetParams().size() > MAX_HOSTS) {
    		throw new IndexOutOfBoundsException("Calling Method Exceeds Maximum Number Of Displayable Hosts");
    	}

    	initializeComponents();
        updateComponents();
        configureListeners();
    }

    
    private void initializeComponents() {
    	selectButtonGroup = new ButtonGroup();
    	
    	rbh = new RadioButtonHandler();

    	lblHostNameLeft = new JLabel("Host Name");
    	lblHostNameLeft.setHorizontalAlignment(SwingConstants.CENTER);
    	lblHostNameLeft.setFont(new Font(DEFAULT_FONT, Font.BOLD, 12));
    
    	lblSelectLeft = new JLabel("Select");
    	lblSelectLeft.setHorizontalAlignment(SwingConstants.CENTER);
        lblSelectLeft.setFont(new Font(DEFAULT_FONT, Font.BOLD, 12));
        
        lblPortLeft = new JLabel("Port");
        lblPortLeft.setHorizontalAlignment(SwingConstants.CENTER);
        lblPortLeft.setFont(new Font(DEFAULT_FONT, Font.BOLD, 12));
        
        lblHostNameRight = new JLabel("Host Name");
        lblHostNameRight.setHorizontalAlignment(SwingConstants.CENTER);
    	lblHostNameRight.setFont(new Font(DEFAULT_FONT, Font.BOLD, 12));
    
    	lblSelectRight = new JLabel("Select");
    	lblSelectRight.setHorizontalAlignment(SwingConstants.CENTER);
        lblSelectRight.setFont(new Font(DEFAULT_FONT, Font.BOLD, 12));
        
        lblPortRight = new JLabel("Port");
        lblPortRight.setHorizontalAlignment(SwingConstants.CENTER);
        lblPortRight.setFont(new Font(DEFAULT_FONT, Font.BOLD, 12));
        
        lblCallSignLabel = new JLabel("Call Sign");
        lblCallSignLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 12));
        
        lblPasswordLabel = new JLabel("Password");
        lblPasswordLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 12));
        
        lblEquipmentIdLabel = new JLabel("Equipment");
        lblEquipmentIdLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 12));
        
        jtfHostName = new JTextField[12];
        jtfPort = new JTextField[12];
        rbSelect = new JRadioButton[12]; 
        
        jtfCallSign = new JTextField();
        jtfPassword = new JTextField();
        jtfEquipmentId = new JTextField();
        
        jbResetDefaults = new JButton("Reset Defaults");
        jbResetDefaults.setMultiClickThreshhold(50L);
        jbResetDefaults.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));
        
        for (int i = 0; i < jtfHostName.length; i++) {
        	jtfHostName[i] = new JTextField();
        	jtfHostName[i].setFont(new Font(DEFAULT_FONT, Font.PLAIN, 12));
        }
        
        for (int i = 0; i < jtfPort.length; i++) {
        	jtfPort[i] = new JTextField();
        	jtfPort[i].setFont(new Font(DEFAULT_FONT, Font.PLAIN, 12));
        }
        
        for (int i = 0; i < rbSelect.length; i++) {
        	rbSelect[i] = new JRadioButton();
        	selectButtonGroup.add(rbSelect[i]);
        }
        
    }

	private void updateComponents() {
		for (int i = 0; i < jtfHostName.length; i++) {
			jtfHostName[i].setToolTipText(aprsIsClient.getNetParams().get(i).getDescription());
	    	jtfHostName[i].setText(String.valueOf(aprsIsClient.getNetParams().get(i).getHostName()));
        	selectButtonGroup.setSelected(rbSelect[i].getModel(), aprsIsClient.getNetParamSelect() == i);
        	final int p = aprsIsClient.getNetParams().get(i).getPortNumber();
	    	if (p >= 0) {
	    		jtfPort[i].setText(String.valueOf(p));
	    	} else {
	    		jtfPort[i].setText("");
	    	}
		}
		jtfCallSign.setText(aprsIsClient.getCallSign());
		jtfPassword.setText(aprsIsClient.getPassword());
		jtfEquipmentId.setText(aprsIsClient.getEquipmentId());
    }
    
    private void configureListeners() {
        jbResetDefaults.addActionListener(event -> {
        	if (event.getID() == ActionEvent.ACTION_PERFORMED) {
        		final List<NetworkParameterSet> list = getDefaultNetworkParameterList();
				int i = 0;
				for (Iterator<NetworkParameterSet> iter = list.iterator(); iter.hasNext();) {
					aprsIsClient.getNetParams().set(i, iter.next());
					i++;
				}
				updateComponents();
			}
        });
    	for (int i = 0; i < jtfHostName.length; i++) {
    		final int ii = i;
    		
    		rbSelect[i].addActionListener(rbh);
    		
    		jtfHostName[i].addFocusListener(new FocusListener() {
		        @Override
		        public void focusGained(FocusEvent e) {
		        	final JTextField jtf = (JTextField) e.getSource();
		        	jtf.setFont(new Font(DEFAULT_FONT, Font.BOLD, 12));
		        }
	
		        @Override
		        public void focusLost(FocusEvent e) {
		        	final JTextField jtf = (JTextField) e.getSource();
		        	jtf.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 12));
		        	try {
						aprsIsClient.getNetParams().get(ii).setHostName(jtf.getText());
					} catch (UnknownHostException ex) {
						LOG.log(Level.WARNING, ex.getMessage(), ex);
					}
		        }
		    });
	    	jtfHostName[i].addKeyListener(new KeyListener() {
		        @Override
		        public void keyTyped(KeyEvent event) {
		        	// NO-OP
		        }
	
		        @Override
		        public void keyPressed(KeyEvent event) {
		            if (event.getKeyCode() == KeyEvent.VK_ENTER) {
		            	JTextField jtf = (JTextField) event.getSource();
		            	jtf.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 12));
		            	jtf.transferFocus();
		            	try {
							aprsIsClient.getNetParams().get(ii).setHostName(jtf.getText());
						} catch (UnknownHostException ex) {
							LOG.log(Level.WARNING, ex.getMessage(), ex);
						}
		            }
		        }
	
		        @Override
		        public void keyReleased(KeyEvent event) {
		          // NO-OP
		        }
		    });
	    	jtfPort[i].addFocusListener(new FocusListener() {
		        @Override
		        public void focusGained(FocusEvent e) {
		        	JTextField jtf = (JTextField) e.getSource();
		        	jtf.setFont(new Font(DEFAULT_FONT, Font.BOLD, 12));
		        }
	
		        @Override
		        public void focusLost(FocusEvent e) {
		        	JTextField jtf = (JTextField) e.getSource();
		        	jtf.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 12));
		        	aprsIsClient.getNetParams().get(ii).setPortNumber(Integer.parseInt(jtf.getText()));
		        }
		    });
	    	jtfPort[i].addKeyListener(new KeyListener() {
		        @Override
		        public void keyTyped(KeyEvent event) {
		        	// NO-OP
		        }
	
		        @Override
		        public void keyPressed(KeyEvent event) {
		            if (event.getKeyCode() == KeyEvent.VK_ENTER) {
		            	JTextField jtf = (JTextField) event.getSource();
		            	jtf.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 12));
		            	jtf.transferFocus();
		            	aprsIsClient.getNetParams().get(ii).setPortNumber(Integer.parseInt(jtf.getText()));
		            }
		        }
	
		        @Override
		        public void keyReleased(KeyEvent event) {
		          // NO-OP
		        }
		    });
	    	jtfCallSign.addFocusListener(new FocusListener() {
		        @Override
		        public void focusGained(FocusEvent e) {
		        	JTextField jtf = (JTextField) e.getSource();
		        	jtf.setFont(new Font(DEFAULT_FONT, Font.BOLD, 12));
		        }
	
		        @Override
		        public void focusLost(FocusEvent e) {
		        	JTextField jtf = (JTextField) e.getSource();
		        	aprsIsClient.setCallSign(jtf.getText());
		        	jtf.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 12));
		        }
		    });
	    	jtfCallSign.addKeyListener(new KeyListener() {
		        @Override
		        public void keyTyped(KeyEvent event) {
		        	// NO-OP
		        }
	
		        @Override
		        public void keyPressed(KeyEvent event) {
		            if (event.getKeyCode() == KeyEvent.VK_ENTER) {
		            	JTextField jtf = (JTextField) event.getSource();
		            	jtf.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 12));
		            	jtf.transferFocus();
		            	aprsIsClient.setCallSign(jtf.getText());
		            }
		        }
	
		        @Override
		        public void keyReleased(KeyEvent event) {
		          // NO-OP
		        }
		    });
	    	jtfPassword.addFocusListener(new FocusListener() {
		        @Override
		        public void focusGained(FocusEvent e) {
		        	JTextField jtf = (JTextField) e.getSource();
		        	jtf.setFont(new Font(DEFAULT_FONT, Font.BOLD, 12));
		        }
	
		        @Override
		        public void focusLost(FocusEvent e) {
		        	JTextField jtf = (JTextField) e.getSource();
		        	jtf.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 12));
		        	aprsIsClient.setPassword(jtf.getText());
		        }
		    });
	    	jtfPassword.addKeyListener(new KeyListener() {
		        @Override
		        public void keyTyped(KeyEvent event) {
		        	// NO-OP
		        }
	
		        @Override
		        public void keyPressed(KeyEvent event) {
		            if (event.getKeyCode() == KeyEvent.VK_ENTER) {
		            	JTextField jtf = (JTextField) event.getSource();
		            	jtf.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 12));
		            	jtf.transferFocus();
		            	aprsIsClient.setPassword(jtf.getText());
		            }
		        }
	
		        @Override
		        public void keyReleased(KeyEvent event) {
		          // NO-OP
		        }
		    });
	    	jtfEquipmentId.addFocusListener(new FocusListener() {
		        @Override
		        public void focusGained(FocusEvent e) {
		        	JTextField jtf = (JTextField) e.getSource();
		        	jtf.setFont(new Font(DEFAULT_FONT, Font.BOLD, 12));
		        }
	
		        @Override
		        public void focusLost(FocusEvent e) {
		        	JTextField jtf = (JTextField) e.getSource();
		        	jtf.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 12));
		        	aprsIsClient.setEquipmentId(jtf.getText());
		        }
		    });
	    	jtfEquipmentId.addKeyListener(new KeyListener() {
		        @Override
		        public void keyTyped(KeyEvent event) {
		        	// NO-OP
		        }
	
		        @Override
		        public void keyPressed(KeyEvent event) {
		            if (event.getKeyCode() == KeyEvent.VK_ENTER) {
		            	JTextField jtf = (JTextField) event.getSource();
		            	jtf.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 12));
		            	jtf.transferFocus();
		            	aprsIsClient.setEquipmentId(jtf.getText());
		            }
		        }
	
		        @Override
		        public void keyReleased(KeyEvent event) {
		          // NO-OP
		        }
		    });
    	}
    }  
    
    public static List<NetworkParameterSet> getDefaultNetworkParameterList() {
    	List<NetworkParameterSet> list = new ArrayList<>(MAX_HOSTS);
    	
    	try {
	    	list.add(new NetworkParameterSet("rotate.aprs2.net", 14580, "Worldwide Tier 2 Round-Robin Server"));
    	} catch (UnknownHostException ex) {
			LOG.log(Level.INFO, "rotate.aprs2.net:14850 - Worldwide Tier 2 Round-Robin Server is not available\n{0}", ex.getLocalizedMessage());
		}
    	try {
    		list.add(new NetworkParameterSet("noam.aprs2.net", 14580, "North America Tier 2 Server"));
    	} catch (UnknownHostException ex) {
    		LOG.log(Level.INFO, "noam.aprs2.net:14850 - North America Tier 2 Server is not available\\n{0}", ex.getLocalizedMessage());
    	}		
    	try {
    		list.add(new NetworkParameterSet("soam.aprs2.net", 14580, "South America Tier 2 Server"));
    	} catch (UnknownHostException ex) {
    		LOG.log(Level.INFO, "soam.aprs2.net:14850 - South America Tier 2 Server is not available\\n{0}", ex.getLocalizedMessage());
    	}	
    	try {
    		list.add(new NetworkParameterSet("euro.aprs2.net", 14580, "Europe/Africa Tier 2 Server"));
    	} catch (UnknownHostException ex) {
    		LOG.log(Level.INFO, "euro.aprs2.net:14850 - Europe/Africa Tier 2 Server is not available\\n{0}", ex.getLocalizedMessage());
    	}
    	try {
    		list.add(new NetworkParameterSet("asia.aprs2.net", 14580, "Asia Tier 2 Server"));
    	} catch (UnknownHostException ex) {
    		LOG.log(Level.INFO, "asia.aprs2.net:14850 - Asia Tier 2 Server is not available\\n{0}", ex.getLocalizedMessage());
    	}
    	try {
    		list.add(new NetworkParameterSet("aunz.aprs2.net", 14580, "Oceania Tier 2 Server"));
    	} catch (UnknownHostException ex) {
    		LOG.log(Level.INFO, "aunz.aprs2.net:14850 - Oceania Tier 2 Server is not available\\n{0}", ex.getLocalizedMessage());
    	}
    	try {
    		list.add(new NetworkParameterSet("srvr.aprs-is.net", 8080, "Supports UDP, HTTP, WebSocket (WS)"));
    	} catch (UnknownHostException ex) {
    		LOG.log(Level.INFO, "srvr.aprs-is.net:8080 - UDP/HTTP/WebSocket (WS) Server is not available\\n{0}", ex.getLocalizedMessage());
    	}
    	try {
    		list.add(new NetworkParameterSet("aunz.aprs2.net", 8888, "Supports UDP, HTTP, WebSocket (WS) for AU/NZ"));
    	} catch (UnknownHostException ex) {
    		LOG.log(Level.INFO, "aunz.aprs2.net:14850 - AU/NZ UDP/HTTP/WebSocket (WS) Server is not available\\n{0}", ex.getLocalizedMessage());
    	}
    	try {
    		list.add(new NetworkParameterSet("firenet.us", 14580, "FIRENET U.S. Server"));
    	} catch (UnknownHostException ex) {
    		LOG.log(Level.INFO, "firenet.us:14850 - FIRENET U.S. server is not available\\n{0}", ex.getLocalizedMessage());
    	}
    	try {
    		list.add(new NetworkParameterSet("firenet.aprs2.net", 14580, "FIRENET Round-Robin Server"));
    	} catch (UnknownHostException ex) {
    		LOG.log(Level.INFO, "firenet.aprs2.net:14850 - FIRENET Round-Robin DNS is not available\\n{0}", ex.getLocalizedMessage());
    	}
    	try {
    		list.add(new NetworkParameterSet("rotate.aprs.net", 14580, "Round-Robin DNS for All Core Servers"));
    	} catch (UnknownHostException ex) {
    		LOG.log(Level.INFO, "rotate.aprs.net:14850 - Round-Robin DNS is not available\\n{0}", ex.getLocalizedMessage());
    	}
    	try {
    		list.add(new NetworkParameterSet("localhost", 14580, "IS Server Hosted on This Machine"));
    	} catch (UnknownHostException ex) {
    		LOG.log(Level.INFO, "localhost:14850 - Locally hosted IS server is not available on this machine\\n{0}", ex.getLocalizedMessage());
    	}
    	
    	return list;
    }
    
    public JPanel getNetworkInterfaceConfigPanel() {
    	JPanel panel = new JPanel();
    	
    	GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
        
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(lblPasswordLabel, 80, 80, 80)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jtfPassword, 100, 100, 100)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jbResetDefaults, 120, 120, 120)
                                .addGap(35, 35, 35))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                    .addComponent(lblSelectLeft)
                                    .addComponent(rbSelect[0], GroupLayout.Alignment.TRAILING)
                                    .addComponent(rbSelect[1], GroupLayout.Alignment.TRAILING)
                                    .addComponent(rbSelect[2], GroupLayout.Alignment.TRAILING)
                                    .addComponent(rbSelect[3], GroupLayout.Alignment.TRAILING)
                                    .addComponent(rbSelect[4], GroupLayout.Alignment.TRAILING)
                                    .addComponent(rbSelect[5], GroupLayout.Alignment.TRAILING))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                	.addComponent(lblHostNameLeft, 170, 170, 170)
                                    .addComponent(jtfHostName[0], 170, 170, 170)
                                    .addComponent(jtfHostName[1], 170, 170, 170)
                                    .addComponent(jtfHostName[2], 170, 170, 170)
                                    .addComponent(jtfHostName[3], 170, 170, 170)
                                    .addComponent(jtfHostName[4], 170, 170, 170)
                                    .addComponent(jtfHostName[5], 170, 170, 170))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                    .addComponent(lblPortLeft, 60, 60, 60)
                                    .addComponent(jtfPort[0], 60, 60, 60)
                                    .addComponent(jtfPort[1], 60, 60, 60)
                                    .addComponent(jtfPort[2], 60, 60, 60)
                                    .addComponent(jtfPort[3], 60, 60, 60)
                                    .addComponent(jtfPort[4], 60, 60, 60)
                                    .addComponent(jtfPort[5], 60, 60, 60))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                	.addComponent(lblSelectRight)	
                                	.addComponent(rbSelect[6], GroupLayout.Alignment.TRAILING)
                                    .addComponent(rbSelect[7], GroupLayout.Alignment.TRAILING)
                                    .addComponent(rbSelect[8], GroupLayout.Alignment.TRAILING)
                                    .addComponent(rbSelect[9], GroupLayout.Alignment.TRAILING)
                                    .addComponent(rbSelect[10], GroupLayout.Alignment.TRAILING)
                                    .addComponent(rbSelect[11], GroupLayout.Alignment.TRAILING))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                    .addComponent(lblHostNameRight, 170, 170, 170)
                                    .addComponent(jtfHostName[6], 170, 170, 170)
                                    .addComponent(jtfHostName[7], 170, 170, 170)
                                    .addComponent(jtfHostName[8], 170, 170, 170)
                                    .addComponent(jtfHostName[9], 170, 170, 170)
                                    .addComponent(jtfHostName[10], 170, 170, 170)
                                    .addComponent(jtfHostName[11], 170, 170, 170))))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                            .addComponent(lblPortRight, 60, 60, 60)
                            .addComponent(jtfPort[6], 60, 60, 60)
                            .addComponent(jtfPort[7], 60, 60, 60)
                            .addComponent(jtfPort[8], 60, 60, 60)
                            .addComponent(jtfPort[9], 60, 60, 60)
                            .addComponent(jtfPort[10], 60, 60, 60)
                            .addComponent(jtfPort[11], 60, 60, 60))
                        .addContainerGap(6, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(lblCallSignLabel, 80, 80, 80)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jtfCallSign, 100, 100, 100))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(lblEquipmentIdLabel, 80, 80, 80)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jtfEquipmentId, 250, 250, 250)))
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))));
    
            layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(lblCallSignLabel)
                        .addComponent(jtfCallSign, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                		.addComponent(lblPasswordLabel)
                        .addComponent(jtfPassword, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(jbResetDefaults, 18, 18, 18))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(lblEquipmentIdLabel)
                        .addComponent(jtfEquipmentId, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGap(18, 18, 18)
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(lblSelectLeft)
                        .addComponent(lblHostNameLeft)
                        .addComponent(lblPortLeft)
                        .addComponent(lblSelectRight)
                        .addComponent(lblHostNameRight)
                        .addComponent(lblPortRight))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(rbSelect[0])
                        .addComponent(jtfHostName[0], GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(jtfPort[0], GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(rbSelect[6])
                        .addComponent(jtfHostName[6], GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(jtfPort[6], GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(rbSelect[1])
                        .addComponent(jtfHostName[1], GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(jtfPort[1], GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(rbSelect[7])
                        .addComponent(jtfHostName[7], GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(jtfPort[7], GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(rbSelect[2])
                        .addComponent(jtfHostName[2], GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(jtfPort[2], GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(rbSelect[8])
                        .addComponent(jtfHostName[8], GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(jtfPort[8], GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(rbSelect[3])
                        .addComponent(jtfHostName[3], GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(jtfPort[3], GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(rbSelect[9])
                        .addComponent(jtfHostName[9], GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(jtfPort[9], GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(rbSelect[4])
                        .addComponent(jtfHostName[4], GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(jtfPort[4], GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(rbSelect[10])
                        .addComponent(jtfHostName[10], GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(jtfPort[10], GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(rbSelect[5])
                        .addComponent(jtfHostName[5], GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(jtfPort[5], GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(rbSelect[11])
                        .addComponent(jtfHostName[11], GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(jtfPort[11], GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );
            
    	return panel;
    }

    private class RadioButtonHandler implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent event) {
			for (int i = 0; i < rbSelect.length; i++) {
        		if (event.getSource()== rbSelect[i]) {
        			aprsIsClient.setNetParamSelect(i);
        		}
        	}
		}
    }
    
}
