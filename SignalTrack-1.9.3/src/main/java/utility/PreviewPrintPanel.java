package utility;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

public class PreviewPrintPanel extends JDialog {
	private static final long serialVersionUID = 1L;

	public PreviewPrintPanel(BufferedImage image) {
		final JPanel panel = new ImagePrint(image);
		paintImage(panel);
	}
	
	public PreviewPrintPanel(JPanel panel) {
		paintImage(panel);
	}
		
	private void paintImage(JPanel panel) {
		final JPanel mPanel = panel;
		
		mPanel.setLayout(new FlowLayout());
		
		final Toolkit tk = Toolkit.getDefaultToolkit();
		final Dimension screenSize = tk.getScreenSize();

		setModalityType(ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Print Preview");

        mPanel.setBackground(new Color(255, 255, 255));
        mPanel.setOpaque(true);
        mPanel.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 255)));
 
        add(mPanel);

        pack();

		setLocation((screenSize.width / 2) - (getWidth() / 2), (screenSize.height / 2) - (getHeight() / 2));
		
		setVisible(true);
	}
	
	@Override
	public Dimension getPreferredSize() {
		final Toolkit tk = Toolkit.getDefaultToolkit();
		final Dimension screenSize = tk.getScreenSize();
		return new Dimension((int) (screenSize.width * 0.9), (int) (screenSize.height * 0.9));
	}
	
    private static final class ImagePrint extends JPanel {
        private static final long serialVersionUID = 1L;
        private final transient BufferedImage image;
        private final int w;
        private final int h;

        private ImagePrint(BufferedImage image) {
            this.image = image;
            w = image.getWidth();
            h =  image.getHeight();
            configureComponent();
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            g.drawImage(image, 0, 0, w, h, null);
        }
        
        private void configureComponent() {
        	setSize(w, h);
        }
    }	
}
