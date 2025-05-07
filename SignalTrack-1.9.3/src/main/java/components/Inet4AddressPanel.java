package components;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Ellipse2D;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.text.PlainDocument;

import utility.BoundedIntegerFilter;

public class Inet4AddressPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private JFormattedTextField[] jftfOctet;

	private final DotPanel[] dot = new DotPanel[3];

	private byte[] octet = { 0, 0, 0, 0 };

	private static final Logger log = Logger.getLogger(Inet4AddressPanel.class.getName());

	public Inet4AddressPanel(Inet4Address ipv4) {
		setBorder(BorderFactory.createLineBorder(Color.GRAY));
		setPreferredSize(new Dimension(192, 21));
		jftfOctet = new JFormattedTextField[4];
		octet = ipv4.getAddress();

		initialize();
		initLayout();
	}

	private void initialize() {
		for (int i = 0; i < 4; i++) {
			jftfOctet[i] = new JFormattedTextField();
			jftfOctet[i].setFont(new Font("Tahoma", Font.PLAIN, 11));
			jftfOctet[i].setBorder(null);
			jftfOctet[i].setText(String.valueOf(octet[i]));
			jftfOctet[i].setToolTipText("The value of this octet in the IPv4 address. [0-255]");
			jftfOctet[i].setDisabledTextColor(Color.BLACK);
			jftfOctet[i].setHorizontalAlignment(SwingConstants.CENTER);
			
			applyFilter(jftfOctet[i]);
			
			jftfOctet[i].addFocusListener(new FocusListener() {
				@Override
				public void focusGained(FocusEvent event) {
					final JFormattedTextField tf = (JFormattedTextField) event.getSource();
					tf.setFont(new Font("Tahoma", Font.BOLD, 11));
				}
				@Override
				public void focusLost(FocusEvent event) {
					final JFormattedTextField tf = (JFormattedTextField) event.getSource();
					tf.setFont(new Font("Tahoma", Font.PLAIN, 11));
					firePropertyChange("ipv4Changed", null, getInet4Address());
				}	
			});
			
			jftfOctet[i].addKeyListener(new KeyListener() {
				@Override
				public void keyTyped(KeyEvent event) {
					// UNSUPPORTED OPERATION
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
			      // UNSUPPORTED OPERATION
			    }
			});
		}

		for (int i = 0; i < 3; i++) {
			dot[i] = new DotPanel(3, Color.BLACK);
		}
	}

	private void applyFilter(JFormattedTextField jftf) {
		final PlainDocument pDoc = (PlainDocument) jftf.getDocument();
		pDoc.setDocumentFilter(new BoundedIntegerFilter(0, 128));
	}

	public InetAddress getInet4Address() {
		InetAddress ipv4 = null;
		try {
			ipv4 = InetAddress.getByAddress(octet);
		} catch (UnknownHostException e) {
			log.log(Level.WARNING, "UnknownHostException", e);
		}
		return ipv4;
	}

	public void initLayout() {
		final GroupLayout layout = new GroupLayout(this);
		setLayout(layout);

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
			.addGroup(layout.createSequentialGroup()
				.addComponent(jftfOctet[0], 45, 45, 45)
				.addComponent(dot[0], 4, 4, 4)
				.addComponent(jftfOctet[1], 45, 45, 45)
				.addComponent(dot[1], 4, 4, 4)
				.addComponent(jftfOctet[2], 45, 45, 45)
				.addComponent(dot[2], 4, 4, 4)
				.addComponent(jftfOctet[3], 45, 45, 45)));
		
		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(jftfOctet[0], 21, 21, 21)
				.addComponent(dot[0], 21, 21, 21)
				.addComponent(jftfOctet[1], 21, 21, 21)
				.addComponent(dot[1], 21, 21, 21)
				.addComponent(jftfOctet[2], 21, 21, 21)
				.addComponent(dot[2], 21, 21, 21)
				.addComponent(jftfOctet[3], 21, 21, 21)));
	}

	public static class DotPanel extends Canvas {
		private static final long serialVersionUID = 1L;
		private int dotWidth;
		private Color dotColor;

		public DotPanel(int dotWidth, Color dotColor) {
			this.dotWidth = dotWidth;
			this.dotColor = dotColor;
			setBackground(Color.WHITE);
		}

		@Override
		public void paint(Graphics g) {
			final Graphics2D g2 = (Graphics2D) g;
			try {
				final int width = getWidth();
				final int height = getHeight();
	
				g2.setColor(dotColor);
	
				final int xPos;
				final int yPos;
	
				xPos = (width / 2) - (dotWidth / 2);
				yPos = (height / 2) - (dotWidth / 2);
	
				final Ellipse2D dot = new Ellipse2D.Float(xPos, yPos, dotWidth, dotWidth);
				g2.fill(dot);
			} finally {
				g2.dispose();
			}
		}
	}
}
