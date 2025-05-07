package components;

import java.awt.AWTEvent;
import java.awt.AWTEventMulticaster;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

class RoundedButton extends Component {
	private static final long serialVersionUID = 1L;
	
	private transient ActionListener actionListener;     // Post action events to listeners
    private String label;                      // The Button's text
    protected boolean pressed = false; // true if the button is detented.

    /**
     * Constructs a RoundedButton with no label.
     */
    public RoundedButton() {
        this("");
    }

    /**
     * Constructs a RoundedButton with the specified label.
     *
     * @param label the label of the button
     */
    public RoundedButton(String label) {
        this.label = label;
        enableEvents(AWTEvent.MOUSE_EVENT_MASK);
    }

    /**
     * gets the label
     *
     * @see setLabel
     */
    public String getLabel() {
        return label;
    }

    /**
     * sets the label
     *
     * @see getLabel
     */
    public void setLabel(String label) {
        this.label = label;
        invalidate();
        repaint();
    }

    /**
     * paints the RoundedButton
     */
    @Override
    public void paint(Graphics g) {

        // paint the interior of the button
        if (pressed) {
            g.setColor(getBackground().darker().darker());
        } else {
            g.setColor(getBackground());
        }
        g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);

        // draw the perimeter of the button
        g.setColor(getBackground().darker().darker().darker());
        g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
        setSize(100,20);
        // draw the label centered in the button
        final Font f = getFont();
        if (f != null) {
            final FontMetrics fm = getFontMetrics(getFont());
            g.setColor(getForeground());
            g.drawString(label, (getWidth() / 2) - (fm.stringWidth(label) / 2), (getHeight() / 2) + fm.getMaxDescent());
        }
    }

    /**
     * The preferred size of the button.
     */
    @Override
    public Dimension getPreferredSize() {
        final Font f = getFont();
        if (f != null) {
            final FontMetrics fm = getFontMetrics(getFont());
            final int max = Math.max(fm.stringWidth(label) + 3, fm.getHeight() + 3);
            return new Dimension(max, max);
        } else {
            return new Dimension(10, 10);
        }
    }

    /**
     * The minimum size of the button.
     */
    @Override
    public Dimension getMinimumSize() {
        return new Dimension(10, 10);
    }

    /**
     * Adds the specified action listener to receive action events from this
     * button.
     *
     * @param listener the action listener
     */
    public void addActionListener(ActionListener listener) {
        actionListener = AWTEventMulticaster.add(actionListener, listener);
        enableEvents(AWTEvent.MOUSE_EVENT_MASK);
    }

    /**
     * Removes the specified action listener so it no longer receives action
     * events from this button.
     *
     * @param listener the action listener
     */
    public void removeActionListener(ActionListener listener) {
        actionListener = AWTEventMulticaster.remove(actionListener, listener);
    }

    /**
     * Determine if click was inside round button.
     */
    @Override
    public boolean contains(int x, int y) {
        final int mx = getSize().width / 2;
        final int my = getSize().height / 2;
        return ((((mx - x) * (mx - x)) + ((my - y) * (my - y))) <= (mx * mx));
    }

    /**
     * Paints the button and distribute an action event to all listeners.
     */
    @Override
    public void processMouseEvent(MouseEvent e) {
        switch (e.getID()) {
			case MouseEvent.MOUSE_PRESSED -> {
				// render myself inverted....
				pressed = true;
				// Repaint might flicker a bit. To avoid this, you can use
				// double buffering (see the Gauge example).
				repaint();
			}
			case MouseEvent.MOUSE_RELEASED -> {
				if (actionListener != null) {
				    actionListener.actionPerformed(new ActionEvent(
				            this, ActionEvent.ACTION_PERFORMED, label));
				}
				// render myself normal again
				if (pressed) {
				    pressed = false;
	
				    // Repaint might flicker a bit. To avoid this, you can use
				    // double buffering (see the Gauge example).
				    repaint();
				}
			}
			case MouseEvent.MOUSE_ENTERED -> {
			}
			case MouseEvent.MOUSE_EXITED -> {
				if (pressed) {
				    // Cancel! Don't send action event.
				    pressed = false;
	
				    // Repaint might flicker a bit. To avoid this, you can use
				    // double buffering (see the Gauge example).
				    repaint();
	
				    // Note: for a more complete button implementation,
				    // you wouldn't want to cancel at this point, but
				    // rather detect when the mouse re-entered, and
				    // re-highlight the button. There are a few state
				    // issues that that you need to handle, which we leave
				    // this an an exercise for the reader (I always
				    // wanted to say that!)
				}
			}
			default -> {
			}
		}
        super.processMouseEvent(e);
    }
}
