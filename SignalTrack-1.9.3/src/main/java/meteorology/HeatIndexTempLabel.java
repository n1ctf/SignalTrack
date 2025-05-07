package meteorology;

import java.awt.Color;
import java.awt.EventQueue;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

public class HeatIndexTempLabel extends JLabel {

	private static final long serialVersionUID = -1L;

	public static final String TOOL_TIP_TEXT_BELOW_INDEX = "Heat index does not apply below 80 degrees fahrenheit.";
	public static final String TOOL_TIP_TEXT_VERY_WARM = "Caution: fatigue is possible with prolonged exposure and activity. Continuing activity could result in heat cramps.";
	public static final String TOOL_TIP_TEXT_HOT = "Extreme caution: heat cramps and heat exhaustion are possible. Continuing activity could result in heat stroke.";
	public static final String TOOL_TIP_TEXT_VERY_HOT = "Danger: heat cramps and heat exhaustion are likely; heat stroke is probable with continued activity.";
	public static final String TOOL_TIP_TEXT_EXTREMELY_HOT = "Extreme danger: heat stroke is imminent.";
	public static final String TOOL_TIP_TEXT_LETHAL = "Heat index above 279 degrees fahrenheit is immediately fatal.";

    public void setHeatIndex(double temp) {
    	invokeLaterInDispatchThreadIfNeeded(() -> {
    		if (temp < 80) {
	            setText("UNDER RNG");  
	            setToolTipText(TOOL_TIP_TEXT_BELOW_INDEX);
	            setBackground(Color.GREEN);
				setForeground(Color.BLACK);
	        } else if (temp >= 80 && temp < 90) {
	            setText("VERY WARM"); 
	            setToolTipText(TOOL_TIP_TEXT_VERY_WARM);
	            setBackground(Color.YELLOW);
				setForeground(Color.BLACK);
	        } else if (temp >= 90 && temp < 104) {
	        	setText("HOT");  
	        	setToolTipText(TOOL_TIP_TEXT_HOT);
	        	setBackground(Color.ORANGE);
				setForeground(Color.BLACK);
	        } else if (temp >= 104 && temp < 128) {
	        	setText("VERY HOT"); 
	        	setToolTipText(TOOL_TIP_TEXT_VERY_HOT);
	        	setBackground(Color.MAGENTA);
				setForeground(Color.WHITE);
	        } else if (temp >= 128 && temp < 279) {
	        	setText("EXTREME"); 
	        	setToolTipText(TOOL_TIP_TEXT_EXTREMELY_HOT);
	        	setBackground(Color.RED);
				setForeground(Color.WHITE);
	        }  else if (temp >= 279) {
	        	setText("LETHAL");
	        	setToolTipText(TOOL_TIP_TEXT_LETHAL);
	        	setBackground(Color.BLACK);
				setForeground(Color.WHITE);
	        }
    	});
    }
    
	private void invokeLaterInDispatchThreadIfNeeded(Runnable runnable) {
		if (EventQueue.isDispatchThread()) {
			runnable.run();
		} else {
			SwingUtilities.invokeLater(runnable);
		}
	}
}
