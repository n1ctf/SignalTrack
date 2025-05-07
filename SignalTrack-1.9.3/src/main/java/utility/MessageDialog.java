package utility;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

public class MessageDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	
	private static final String DEFAULT_BUTTON_TEXT = "OK";
	private static final String DEFAULT_MESSAGE_TEXT = "Message Text";
	private static final String DEFAULT_TITLE_TEXT = "Title Text";
	private static final ImageIcon DEFAULT_IMAGE_ICON = new ImageIcon("images/exclamation_50.gif");
	private static final Logger LOG = Logger.getLogger(MessageDialog.class.getName());
	
	private final JButton button = new JButton();
	private final JTextArea message = new JTextArea();
	private final JLabel image = new JLabel();
	
	private boolean isAnimated;
    private boolean isAnimating;
    private boolean isWaiting;

	public MessageDialog() {
		this(DEFAULT_TITLE_TEXT, DEFAULT_MESSAGE_TEXT, DEFAULT_BUTTON_TEXT, DEFAULT_IMAGE_ICON);
	}
	
	public MessageDialog(String titleText) {
		this(titleText, DEFAULT_MESSAGE_TEXT, DEFAULT_BUTTON_TEXT, DEFAULT_IMAGE_ICON);
	}
	
	public MessageDialog(String titleText, String messageText) {
		this(titleText, messageText, DEFAULT_BUTTON_TEXT, DEFAULT_IMAGE_ICON);
	}
	
	public MessageDialog(String titleText, String messageText, String buttonText) {
		this(titleText, messageText, buttonText, DEFAULT_IMAGE_ICON);
	}

	public MessageDialog(String titleText, String messageText, String buttonText, ImageIcon imageIcon) {
		super.setTitle(titleText);
		message.setText(messageText);
		button.setText(buttonText);
		image.setIcon(imageIcon);
		super.setVisible(false);
		initComponents();
		addListeners();
	}

	public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException | ClassNotFoundException ex) {
            Logger.getLogger(MessageDialog.class.getName()).log(Level.SEVERE, null, ex);
        }

        EventQueue.invokeLater(MessageDialog::new);
    }

	public void setButtonText(String buttonText) {
		button.setText(buttonText);
		repaint();
	}

	public void setMessageText(String messageText) {
		message.setText(messageText);
		repaint();
	}

	public void setImage(ImageIcon icon) {
		image.setIcon(icon);
		repaint();
	}

	public void close() {
		if (isAnimated && !isAnimating) {
			final Runnable task = this::waitBeforeFadeOut;
			invokeLaterInDispatchThreadIfNeeded(task);
		} else if (!isWaiting && !isAnimating) {
			dispose();
		}
	}

	public synchronized void waitBeforeFadeOut() {
	    final int timerDelay = 1000;
	    final Timer timer = new Timer(timerDelay, event -> {
            ((Timer)event.getSource()).stop();
            dispose();
        });
	    timer.start();
	}

	private void addListeners() {
		button.addActionListener(_ -> dispose());
	}
	
	private void initComponents() {
		setType(Type.POPUP);
    	setVisible(false);
    	setAlwaysOnTop(true);
    	setResizable(false);
    	setModalityType(ModalityType.MODELESS);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        changeColor(getComponents());
		button.setMultiClickThreshhold(50L);

		message.setBorder(null);
        message.setEditable(true);
        message.setBackground(Color.LIGHT_GRAY);
        
        final GroupLayout layout = new GroupLayout(getContentPane());
	    getContentPane().setLayout(layout);

	    layout.setHorizontalGroup(
	            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
	            .addGroup(layout.createSequentialGroup()
	                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
	                    .addComponent(button)
	                    .addGroup(layout.createSequentialGroup()
	                        .addComponent(image, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
	                        .addGap(20)
	                        .addComponent(message, GroupLayout.PREFERRED_SIZE, 340, GroupLayout.PREFERRED_SIZE)))
	                .addContainerGap()));

	    layout.setVerticalGroup(
	    		layout.createParallelGroup(GroupLayout.Alignment.LEADING)
	    		.addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
	    				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
	    						.addGroup(layout.createSequentialGroup()
	    								.addContainerGap()
	    								.addComponent(image, GroupLayout.PREFERRED_SIZE, 57, GroupLayout.PREFERRED_SIZE))
	    						.addGroup(layout.createSequentialGroup()
	    								.addGap(30)
	    								.addComponent(message, GroupLayout.PREFERRED_SIZE, 49, GroupLayout.PREFERRED_SIZE)))
	    				.addGap(5)
	    				.addComponent(button)
	    				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		
		pack();
		
		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		
		setLocation((screenSize.width / 2) - (getWidth() / 2), (screenSize.height / 2) - (getHeight() / 2));
		
		setVisible(true);
	}

	public void changeColor(Component[] comp) {
		for (Component aComp : comp) {
			if(aComp instanceof Container container) {
				changeColor(container.getComponents());
			}
			try {
				aComp.setBackground(Color.LIGHT_GRAY);
			} catch(Exception e) {
				LOG.log(Level.INFO, e.getMessage());
			}
		}
	}
	
	private static synchronized void invokeLaterInDispatchThreadIfNeeded(Runnable runnable) {
		if (EventQueue.isDispatchThread()) {
			runnable.run();
		} else {
			SwingUtilities.invokeLater(runnable);
		}
	}
}
