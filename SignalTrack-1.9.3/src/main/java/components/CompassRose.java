package components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class CompassRose extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_NUMBER_LIGHTS = 32;
    private static final int DEFAULT_LIGHT_DIAMETER = 10;
    private static final int DEFAULT_HEADING_FONT_SIZE = 17;
    private static final int OVER_RANGE = 360; 
    
    private Color selectColor = Color.RED;
    private int heading;
    private final int d;
    private final int n;
    private final NumberFormat headingFormat = new DecimalFormat("000");
    private final JLabel headingLabel = new JLabel();

    public CompassRose() {
    	this(DEFAULT_NUMBER_LIGHTS, DEFAULT_LIGHT_DIAMETER);
    }
    
    public CompassRose(int n, int d) {
        super(new BorderLayout());
        this.n = n; // number of lights
        this.d = d; // diameter of each light
        build();
    }

    private void build() {
        headingLabel.setFont(new Font("Tahoma", Font.BOLD, DEFAULT_HEADING_FONT_SIZE));
        headingLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headingLabel.setVerticalAlignment(SwingConstants.CENTER);
        headingLabel.setOpaque(false);
        headingLabel.setVisible(true);
        
        add(headingLabel, BorderLayout.CENTER);
    }

    public void setHeading(int heading) {
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

            if (heading == OVER_RANGE) {
                headingLabel.setText("---");
            } else {
                headingLabel.setText(headingFormat.format(heading));
            }

            final double r = (getWidth() / 2D) - (2 * d); // r = radius of rose
            final double c = getWidth() / 2D; // c = center of panel

            for (double i = 0; i < (2 * Math.PI); i += ((2 * Math.PI) / n)) {

                final double x = (c + (Math.sin(i) * r)) - (d / 2D) - 2;
                final double y = (c - (Math.cos(i) * r) - (d / 2D)) + 5;

                final Ellipse2D dot = new Ellipse2D.Double(x, y, d, d);

                if (Math.abs(Math.toRadians(heading) - i) <= (((2 * Math.PI) / n) / 2)) {
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
