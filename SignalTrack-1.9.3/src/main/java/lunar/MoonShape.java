package lunar;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.time.ZonedDateTime;

import javax.swing.JPanel;

import time.AstronomicalTime;

public class MoonShape extends JPanel {

	public static final double DEFAULT_DEGREES_OF_ROTATION = 135D;
	
	private static final long serialVersionUID = 1L;

	private double phasePercent;
	private transient ZonedDateTime zdt;
	
	public MoonShape() {
		this(0.0);
	}
	
	public MoonShape(ZonedDateTime zdt) {
		this.zdt = zdt;
		phasePercent = AstronomicalTime.getLunarPhase(zdt);
	}
	
	public MoonShape(double phasePercent) {
		this.phasePercent = phasePercent;
	}

	private double getPhaseAngleDegrees() {
		return AstronomicalTime.getLunarPhaseAngle(zdt);
	}

	public void setMoonPhase(double percent) {
		this.phasePercent = percent / 100.0;
		super.repaint();
	}
	
	private int toPhase(double phasePercent) {
		return (int) (phasePercent * (getWidth() * 4));
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		final Graphics2D g2 = (Graphics2D) g.create();

		int phase = toPhase(phasePercent);

		if (phase > (getWidth() * 2)) {
			final AffineTransform a = AffineTransform.getRotateInstance(Math.PI + getPhaseAngleDegrees(), getWidth() / 2D, getHeight() / 2D);
			g2.setTransform(a);
			phase = (getWidth() * 4) - phase;
		} else {
			g2.setTransform(AffineTransform.getRotateInstance(getPhaseAngleDegrees(), getWidth() / 2D, getHeight() / 2D));
		}

		try {

			if (phase >= 0 && phase <= getWidth()) {
				
				g2.setPaint(Color.LIGHT_GRAY);
				g2.fill(new Ellipse2D.Double(0, 0, getWidth(), getHeight()));
				g2.setPaint(Color.WHITE);
				g2.fillArc(0, 0, getWidth(), getHeight(), 90, -180);
				g2.setPaint(Color.LIGHT_GRAY);
				g2.fillArc(phase / 2, 0, getWidth() - phase, getHeight(), 90, -180);
				
			} else if (phase > getWidth() && phase <= getWidth() * 2) {
				
				final int i = Math.abs(phase - getWidth());
				phase =  getWidth() - i;
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
