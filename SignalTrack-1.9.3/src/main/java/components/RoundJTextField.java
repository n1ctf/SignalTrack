package components;

import java.awt.Graphics;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JTextField;

public class RoundJTextField extends JTextField {
	private static final long serialVersionUID = 1L;
	
	private transient Shape shape;
	
    public RoundJTextField(int size) {
        super(size);
        setOpaque(false);
    }
    
    @Override
	protected void paintComponent(Graphics g) {
         g.setColor(getBackground());
         g.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
         super.paintComponent(g);
    }
    
    @Override
	protected void paintBorder(Graphics g) {
         g.setColor(getForeground());
         g.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
    }
    
    @Override
	public boolean contains(int x, int y) {
         if ((shape == null) || !shape.getBounds().equals(getBounds())) {
             shape = new RoundRectangle2D.Float(0, 0, getWidth()-1f, getHeight()-1f, 15, 15);
         }
         return shape.contains(x, y);
    }
}
