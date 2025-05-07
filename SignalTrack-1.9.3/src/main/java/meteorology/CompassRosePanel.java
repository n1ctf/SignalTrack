package meteorology;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import org.netbeans.lib.awtextra.AbsoluteConstraints;
import org.netbeans.lib.awtextra.AbsoluteLayout;

public class CompassRosePanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final int N = 35;

    private Color selectColor = Color.RED;
    private int heading;
    
    private NumberFormat headingFormat;
    private final JLabel headingLabel = new JLabel();
    private boolean headingAdded;

    public CompassRosePanel() {
        super(new AbsoluteLayout());
        initializeComponents();
        configureComponents();
    }

    private void initializeComponents() {
    	headingFormat = new DecimalFormat("000");
    }
    
    private void configureComponents() {
    	headingLabel.setFont(getHeadingLabelFont());
        headingLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headingLabel.setVerticalAlignment(SwingConstants.CENTER);
        headingLabel.setOpaque(false);
        headingLabel.setVisible(true);
        
        setBorder(BorderFactory.createTitledBorder(null, "Wind Direction", TitledBorder.DEFAULT_JUSTIFICATION,
            TitledBorder.DEFAULT_POSITION, new Font("Tahoma", Font.BOLD, 12)));
    }
    
    private int getLampDiameter() {
        return (int) (Math.min(getSize().getHeight(), getSize().getWidth()) / 20);  
    }
    
    private int getRoseRadius() {
        return (int) Math.min(getSize().getHeight(), getSize().getWidth() / 2) - getLampDiameter() - 15;
    }
    
    private Point2D getRoseCenter() {
        return new Point2D.Double(getSize().getWidth() / 2, (getSize().getHeight() / 2) + 5);
    }
    
    private Font getHeadingLabelFont() {
    	if (getWidth() < 120) {
	    	return new Font("Tahoma", Font.BOLD, 12);
    	} else if (getWidth() < 140) {
	    	return new Font("Tahoma", Font.BOLD, 14);
    	} else if (getWidth() < 160) {
	    	return new Font("Tahoma", Font.BOLD, 16);
    	} else if (getWidth() < 180) {
	    	return new Font("Tahoma", Font.BOLD, 18);
    	} else if (getWidth() < 200) {
	    	return new Font("Tahoma", Font.BOLD, 29);
    	} else if (getWidth() < 220) {
	    	return new Font("Tahoma", Font.BOLD, 22);
    	} else if (getWidth() < 240) {
	    	return new Font("Tahoma", Font.BOLD, 24);
    	} else if (getWidth() >= 240) {
	    	return new Font("Tahoma", Font.BOLD, 26);
    	} 
    	
    	return new Font("Tahoma", Font.BOLD, 14);
    }
    
    public void setHeading(int heading) {
    	if (heading < 0) {
    		heading += 360;
    	}
        this.heading = heading;
        repaint();
    }

    public void setSelectColor(Color selectColor) {
        this.selectColor = selectColor;
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        final Graphics2D g2 = (Graphics2D) g.create();

        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            final double r = getRoseRadius(); // r = radius of rose
            final Point2D c = getRoseCenter(); // c = center of panel
            final int d = getLampDiameter();
            
            headingLabel.setFont(getHeadingLabelFont());
            
            if (heading == 360) {
                headingLabel.setText("---");
            } else {
                headingLabel.setText(headingFormat.format(heading) + "\u00B0");
            }
            
            if (!headingAdded) {
            	final int x = (int) (c.getX() - (headingLabel.getPreferredSize().getWidth() / 2));
            	final int y = (int) (c.getY() - (headingLabel.getPreferredSize().getHeight() / 2));
            	add(headingLabel, new AbsoluteConstraints(x, y));
            	headingAdded = true;
            }

            for (double i = 0; i < (2 * Math.PI); i += ((2 * Math.PI) / N)) {

                final double x = (c.getX() + (Math.sin(i) * r)) - (d / 2D);
                final double y = (c.getY() - (Math.cos(i) * r) - (d / 2D));

                final Ellipse2D dot = new Ellipse2D.Double(x, y, d, d);

                if (Math.abs(Math.toRadians(heading) - i) <= (((2 * Math.PI) / N) / 2)) {
                    g2.setColor(selectColor);
                    g2.draw(dot);
                    g2.fill(dot);
                } else {
                    g2.setColor(Color.GRAY);
                    g2.draw(dot);
                }
            }
        } finally {
            g2.dispose();
        }
    }
}
