package components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import javax.swing.JPanel;

public class BarMeter extends JPanel {

    private static final long serialVersionUID = 1L;

    private final Rectangle bar = new Rectangle();
    private int level;
    private Color color = Color.BLUE;

    public BarMeter() {
        configure();
    }

    private void configure() {
        setOpaque(false);
        setDoubleBuffered(true);
        setVisible(true);
    }
    
    public void setLevel(int level) {
        this.level = level;
        repaint();
    }

    public void setColor(Color color) {
        this.color = color;
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        final Graphics2D g2 = (Graphics2D) g;
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            bar.setSize(getWidth(), (level * getHeight()) / 100);
            bar.setLocation(0, getHeight() - ((level * getHeight()) / 100));
            g2.setColor(color);
            g2.fill(bar);
            g2.draw(bar);
        } finally {
            g2.dispose();
        }
    }

}
