package tcp;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.text.PlainDocument;

import utility.BoundedIntegerFilter;

public class TcpPortPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private JFormattedTextField jftfPort;
    private final int port;
    
    public TcpPortPanel(int port) {
        this.port = port;
        
        configureComponents();
        
        initLayout();
        initListeners();
    }
    
    private void configureComponents() {
    	jftfPort = new JFormattedTextField();
    	final PlainDocument pDoc = (PlainDocument) jftfPort.getDocument();
        pDoc.setDocumentFilter(new BoundedIntegerFilter(0, 65535));
    	jftfPort.setText(String.valueOf(port));
        jftfPort.setHorizontalAlignment(SwingConstants.CENTER);
        jftfPort.setBorder(null);
    	setBorder(BorderFactory.createLineBorder(Color.GRAY));
        setPreferredSize(new Dimension(75, 21));
    }

    public int getPort() {
        return Integer.parseInt(jftfPort.getText());
    }

    private void initLayout() {
    	final GroupLayout layout = new GroupLayout(this);
        setLayout(layout);

        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addGroup(GroupLayout.Alignment.CENTER, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jftfPort, 75, 75, 75)));

        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(jftfPort, 21, 21, 21));
    }

    private void initListeners() {
        jftfPort.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent event) {
            	final JFormattedTextField tf = (JFormattedTextField) event.getSource();
                tf.setFont(new Font("Tahoma", Font.BOLD, 11));
            }

            @Override
            public void focusLost(FocusEvent event) {
            	final JFormattedTextField tf = (JFormattedTextField) event.getSource();
                tf.setFont(new Font("Tahoma", Font.PLAIN, 11));
                firePropertyChange("portChanged", null, getPort());
            }
        });

        jftfPort.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent event) {
            	// NOOP
            }

            @Override
            public void keyPressed(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_ENTER) {
                	final JFormattedTextField tf = (JFormattedTextField) event.getSource();
                    tf.setFont(new Font("Calabri", Font.PLAIN, 11));
                    tf.transferFocus();
                }
            }

            @Override
            public void keyReleased(KeyEvent event) {
            	// NOOP
            }
        });
    }
}
