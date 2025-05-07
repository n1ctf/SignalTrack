package utility;

import java.io.PrintWriter;
import java.io.StringWriter;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class TextAreaHandler extends Handler {

    private final JTextArea textArea = new JTextArea(550, 250);

    @Override
    public void publish(final LogRecord r) {
        SwingUtilities.invokeLater(() -> {
        	final StringWriter text = new StringWriter();
        	final PrintWriter out = new PrintWriter(text);
            out.println(textArea.getText());
            out.printf("[%s] [Thread-%d]: %s.%s -> %s", r.getLevel(), r.getLongThreadID(), r.getSourceClassName(),
                r.getSourceMethodName(), r.getMessage());
            textArea.setText(text.toString());
            textArea.setVisible(true);
        });
    }

    public JTextArea getTextArea() {
        return this.textArea;
    }
    
    public void setText(String text) {
    	textArea.setText(text);
    }

	@Override
	public void flush() {
		// TODO Auto-generated method stub
	}

	@Override
	public void close() throws SecurityException {
		// TODO Auto-generated method stub
	}

}
