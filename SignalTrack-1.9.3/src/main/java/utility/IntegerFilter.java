package utility;

import java.awt.EventQueue;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

public class IntegerFilter extends DocumentFilter {
	@Override
	public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
		final Document doc = fb.getDocument();
		final StringBuilder sb = new StringBuilder();
	    sb.append(doc.getText(0, doc.getLength()));
	    sb.insert(offset, string);
	    if (test(sb.toString())) {
	    	super.insertString(fb, offset, string, attr);
	    } else {
	    	invokeLaterInDispatchThreadIfNeeded(() -> JOptionPane.showMessageDialog(null, "Only integer numbers are permitted."));
	    }
   }

   private boolean test(String text) {
	   try {
		   if ("".equals(text)) {
			return true;
		}
		   Integer.parseInt(text);
		   return true;
	   } catch (NumberFormatException e) {
		   return false;
	   }
   }

   @Override
   public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
	   final Document doc = fb.getDocument();
	   final StringBuilder sb = new StringBuilder();
	   sb.append(doc.getText(0, doc.getLength()));
	   sb.replace(offset, offset + length, text);
	   if (test(sb.toString())) {
		   	super.replace(fb, offset, length, text, attrs);
	   } else {
		   	invokeLaterInDispatchThreadIfNeeded(() -> JOptionPane.showMessageDialog(null, "Only integer numbers are permitted."));
	   }	
   }

   	@Override
   	public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
   		final Document doc = fb.getDocument();
   		final StringBuilder sb = new StringBuilder();
   		sb.append(doc.getText(0, doc.getLength()));
   		sb.delete(offset, offset + length);
   		if (test(sb.toString())) {
   			super.remove(fb, offset, length);
   		} else {
		   	invokeLaterInDispatchThreadIfNeeded(() -> JOptionPane.showMessageDialog(null, "Only integer numbers are permitted."));
	   }

   	} 
   	
   	private static void invokeLaterInDispatchThreadIfNeeded(Runnable runnable) {
   		if (EventQueue.isDispatchThread()) {
   			runnable.run();
   		} else {
   			SwingUtilities.invokeLater(runnable);
   		}
   	}
}
