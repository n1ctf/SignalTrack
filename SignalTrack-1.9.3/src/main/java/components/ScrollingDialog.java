package components;

import java.awt.Dialog;
import java.awt.FlowLayout;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;

public class ScrollingDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private final JTextArea textArea;
	private String text;
	
	public ScrollingDialog(String title) {
		setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		setTitle(title);
		setSize(600, 200);
        setVisible(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        getContentPane().setLayout(new FlowLayout());
 
        textArea = new JTextArea(5, 5);
        
        final JScrollPane scrollableTextArea = new JScrollPane(textArea);
 
        scrollableTextArea.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollableTextArea.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 
        getContentPane().add(scrollableTextArea);
	}
	
	public void addTextLine(String text) {
		this.text += text + "\n";
		textArea.setText(this.text);
	}
	
}
