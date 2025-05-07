package tcp;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;

import network.NetworkParameterSet;

// Provides a GUI panel for user settings

public class TcpIpConfigurationComponent {
    private static final Logger LOG = Logger.getLogger(TcpIpConfigurationComponent.class.getName());

    private JTextField[] jtfInetAddr;
    private JLabel jtfInetAddrLabel;
 
    private JTextField jtfPortNumber;
    private JLabel lblPortLabel;
    private final String panelName;
    private boolean networkParameterSetChange;
    
    private final NetworkParameterSet networkParameterSet;

    public TcpIpConfigurationComponent(NetworkParameterSet networkParameterSet, String panelName) {
		this.networkParameterSet = networkParameterSet; 
		this.panelName = panelName;
    	
        initializeComponents();
        updateComponents();
        configureListeners();
    }

    private void initializeComponents() {
        try {
	        jtfInetAddrLabel = new JLabel();
	        jtfInetAddr = new JTextField[4];
	        
	        jtfPortNumber = new JTextField();
	        lblPortLabel = new JLabel();

	        jtfPortNumber.setMaximumSize(new Dimension(50, 25));
	        jtfPortNumber.setMinimumSize(new Dimension(50, 25));
	        jtfPortNumber.setPreferredSize(new Dimension(50, 25));
	        jtfPortNumber.setFont(new Font("Calabri", Font.PLAIN, 12));

	        jtfInetAddrLabel.setText("IP Address");
	        jtfInetAddrLabel.setMaximumSize(new Dimension(70, 25));
	        jtfInetAddrLabel.setMinimumSize(new Dimension(70, 25));
	        jtfInetAddrLabel.setPreferredSize(new Dimension(70, 25));
	        
	        lblPortLabel.setText("TCP Port");
	        lblPortLabel.setMaximumSize(new Dimension(70, 25));
	        lblPortLabel.setMinimumSize(new Dimension(70, 25));
	        lblPortLabel.setPreferredSize(new Dimension(70, 25));
	        
	        for (int i = 0; i < jtfInetAddr.length; i++) {
	        	jtfInetAddr[i] = new JTextField();
	        	jtfInetAddr[i].setMaximumSize(new Dimension(35, 25));
	        	jtfInetAddr[i].setMinimumSize(new Dimension(35, 25));
	        	jtfInetAddr[i].setPreferredSize(new Dimension(35, 25));
	        	jtfInetAddr[i].setFont(new Font("Calabri", Font.PLAIN, 12));
	        }
        } catch (NullPointerException npe) {
        	LOG.log(Level.CONFIG, npe.getMessage());
        }
    }
    
	private void updateComponents() {
    	jtfPortNumber.setText(String.valueOf(networkParameterSet.getPortNumber()));
    	for (int i = 0; i < jtfInetAddr.length; i++) {
    		jtfInetAddr[i].setText(String.valueOf(asInt(networkParameterSet.getInetAddress().getAddress()[i])));
    	}
    }

	public boolean isNetworkParameterChange() {
		return networkParameterSetChange;
	}

	private int asInt(byte b) {
        int i = b;
        if (i < 0) { 
        	i += 256;
        }
        return i;
    }
    
    private void configureListeners() {
    	jtfPortNumber.addFocusListener(new FocusListener() {
	        @Override
	        public void focusGained(FocusEvent e) {
	        	jtfPortNumber.setFont(new Font("Calabri", Font.BOLD, 12));
	        }

	        @Override
	        public void focusLost(FocusEvent e) {
	        	jtfPortNumber.setFont(new Font("Calabri", Font.PLAIN, 12));
	        	networkParameterSet.setPortNumber(Integer.parseInt(jtfPortNumber.getText()));
	        }
	    });
    	jtfPortNumber.addKeyListener(new KeyListener() {
	        @Override
	        public void keyTyped(KeyEvent event) {
	        	// NO-OP
	        }

	        @Override
	        public void keyPressed(KeyEvent event) {
	            if (event.getKeyCode() == KeyEvent.VK_ENTER) {
	            	jtfPortNumber.setFont(new Font("Calabri", Font.PLAIN, 12));
	            	jtfPortNumber.transferFocus();
	            }
	        }

	        @Override
	        public void keyReleased(KeyEvent event) {
	          // NO-OP
	        }
	    });
    	for (int i = 0; i < jtfInetAddr.length; i++) {
    		final int ii = i;
    		jtfInetAddr[i].addFocusListener(new FocusListener() {
    	        @Override
    	        public void focusGained(FocusEvent e) {
    	        	jtfInetAddr[ii].setFont(new Font("Calabri", Font.BOLD, 12));
    	        }

    	        @Override
    	        public void focusLost(FocusEvent e) {
    	        	jtfInetAddr[ii].setFont(new Font("Calabri", Font.PLAIN, 12));
    	        	jtfInetAddr[ii].setFont(new Font("Calabri", Font.PLAIN, 12));
	            	jtfInetAddr[ii].transferFocus();

	            	final byte[] a = networkParameterSet.getInetAddress().getAddress();
	            	
	            	a[ii] = (byte) Integer.parseInt(jtfInetAddr[ii].getText());
	            	
	            	try {
	            		networkParameterSet.setInetAddress(InetAddress.getByAddress(a));
	        		} catch (UnknownHostException ex) {
	        			LOG.log(Level.CONFIG, ex.getMessage());
	        		}
    	        }
    	    });
    		jtfInetAddr[i].addKeyListener(new KeyListener() {
    	        @Override
    	        public void keyTyped(KeyEvent event) {
    	        	// NO-OP
    	        }

    	        @Override
    	        public void keyPressed(KeyEvent event) {
    	            if (event.getKeyCode() == KeyEvent.VK_ENTER) {
    	            	jtfInetAddr[ii].setFont(new Font("Calabri", Font.PLAIN, 12));
    	            	jtfInetAddr[ii].transferFocus();
    	            	
    	            	final byte[] a = networkParameterSet.getInetAddress().getAddress();
    	            	
    	            	a[ii] = (byte) Integer.parseInt(jtfInetAddr[ii].getText());
    	            	
    	            	try {
    	            		networkParameterSet.setInetAddress(InetAddress.getByAddress(a));
    	        		} catch (UnknownHostException ex) {
    	        			LOG.log(Level.CONFIG, ex.getMessage());
    	        		}
    	            	
    	            }
    	        }

    	        @Override
    	        public void keyReleased(KeyEvent event) {
    	          // NO-OP
    	        }
    	    });
    	}
    }    
    
    public JPanel[] getSettingsPanelArray() {
    	return new JPanel[] { getNetworkInterfaceConfigGUI() };
    }
    
    public JPanel getNetworkInterfaceConfigGUI() {
    	final JPanel panel = new JPanel();
    	
    	panel.setName(panelName);
        
    	final GroupLayout layout = new GroupLayout(panel);
        
        panel.setLayout(layout);
        
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(56, 56, 56)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(jtfInetAddrLabel, GroupLayout.PREFERRED_SIZE, 76, GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblPortLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jtfInetAddr[0], GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jtfInetAddr[1], GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jtfInetAddr[2], GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jtfInetAddr[3], GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addComponent(jtfPortNumber, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(49, 49, 49)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jtfInetAddrLabel)
                    .addComponent(jtfInetAddr[0], GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jtfInetAddr[1], GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jtfInetAddr[2], GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jtfInetAddr[3], GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPortLabel)
                    .addComponent(jtfPortNumber, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        
    	return panel;
    }

}
