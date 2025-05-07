package utility;

import java.math.BigDecimal;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;

public class FormattedTextFieldVerifier extends InputVerifier {
	private final double minValue;
	private final double maxValue;
	
	public FormattedTextFieldVerifier(double minValue, double maxValue) {
		this.minValue = minValue;
		this.maxValue = maxValue;
	}
	
    @Override
	public boolean verify(JComponent input) {
        if (input instanceof JFormattedTextField ftf) {
        	final AbstractFormatter formatter = ftf.getFormatter();
            if (formatter != null) {
            	final String text = ftf.getText();
                try {
                	final BigDecimal value = new BigDecimal(text);
                    return value.doubleValue() <= maxValue && value.doubleValue() >= minValue;
                 } catch (NumberFormatException nfe) {
                     return false;
                 }
             }
         }
         return true;
     }
    
     @Override
     public boolean shouldYieldFocus(JComponent input) {
         return verify(input);
     }
 }
