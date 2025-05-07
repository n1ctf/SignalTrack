package lunar;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

public class MoonTest extends JFrame {

	private static final long serialVersionUID = 1L;

	private final double percent;
	private int insets;

	public MoonTest(double percent) {
		this.percent = percent;
		drawGraphicalUserInterface();
	}

	private void drawGraphicalUserInterface() {
		add(new Moon());
		pack();
		setSize(100, 100);
		setMinimumSize(new Dimension(100, 100));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setResizable(false);
		final Toolkit tk = Toolkit.getDefaultToolkit();
		final Dimension screenSize = tk.getScreenSize();
		setLocation((screenSize.width / 2) - (getWidth() / 2), (screenSize.height / 2) - (getHeight() / 2));
		setVisible(true);
		insets = getInsets().left + getInsets().right;
	}

	private class Moon extends JPanel {

		private static final long serialVersionUID = 1L;

		private int toPhase(double percent) {
			return (int) (percent * ((getWidth() + insets) * 4));
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			final Graphics2D g2 = (Graphics2D) g.create();

			int phase = toPhase(percent);

			if (phase > ((getWidth() + insets) * 2)) {
				final AffineTransform a = AffineTransform.getRotateInstance(Math.PI, getWidth() / 2D, getHeight() / 2D);
				g2.setTransform(a);
				phase = ((getWidth() + insets) * 4) - phase;
			}

			try {
				if (phase >= 0 && phase <= getWidth() + insets) {
					g2.setPaint(Color.LIGHT_GRAY);
					g2.fill(new Ellipse2D.Double(0, 0, getWidth(), getHeight()));
					g2.setPaint(Color.WHITE);
					g2.fillArc(0, 0, getWidth(), getHeight(), 90, -180);
					g2.setPaint(Color.LIGHT_GRAY);
					g2.fillArc(phase / 2, 0, Math.abs(getWidth() - phase), getHeight(), 90, -180);
				} else if (phase > getWidth() + insets && phase <= (getWidth() + insets) * 2) {
					phase = (getWidth() + insets) - (phase - (getWidth() + insets));
					g2.setPaint(Color.WHITE);
					g2.fill(new Ellipse2D.Double(0, 0, getWidth(), getHeight()));
					g2.setPaint(Color.LIGHT_GRAY);
					g2.fillArc(0, 0, getWidth(), getHeight(), 90, -180);
					g2.setPaint(Color.WHITE);
					g2.fillArc(phase / 2, 0, getWidth() - phase, getHeight(), 90, -180);
				}
			} finally {
				g2.dispose();
			}
		}

	}

	public static void main(String[] args) {
		new MoonTest(0.75);
	}

}
