package components;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.Timer;

public class JBlinkLabel extends JLabel {
	private static final long serialVersionUID = 1L;

	private static final int DEFAULT_BLINK_RATE = 250;

	private boolean blinkingOn = true;
	
	public JBlinkLabel() {
		this("", DEFAULT_BLINK_RATE);
	}
	
	public JBlinkLabel(int blinkRate) {
		this("", blinkRate);
	}
	
	public JBlinkLabel(String text) {
		this(text, DEFAULT_BLINK_RATE);
	}
	
	public JBlinkLabel(String text, int blinkRate) {
		super(text);
	    final Timer timer = new Timer(blinkRate, new TimerListener(this));
	    timer.setInitialDelay(0);
	    timer.start();
	}
	
	public void setBlinking(boolean blinkingOn) {
	    this.blinkingOn = blinkingOn;
	}
	
	public boolean getBlinking() {
	    return this.blinkingOn;
	}
	
	private static class TimerListener implements ActionListener {
		private final JBlinkLabel bl;
		private final Color bg;
		private final Color fg;
	    private boolean isForeground = true;
	    
	    public TimerListener(JBlinkLabel bl) {
	    	this.bl = bl;
	    	fg = bl.getForeground();
	    	bg = bl.getBackground();
	    }
	 
	    @Override
		public void actionPerformed(ActionEvent e) {
	    	if (bl.blinkingOn) {
	    		if (isForeground) {
	    			bl.setForeground(fg);
	    		} else {
	    			bl.setForeground(bg);
	    		}
	    		isForeground = !isForeground;
	    	} else {
	        // here we want to make sure that the label is visible
	        // if the blinking is off.
	    		if (isForeground) {
	    			bl.setForeground(fg);
	    			isForeground = false;
	    		}
	    	}
		} 
	}
}
