package utility;

import java.util.concurrent.ExecutionException;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingWorker;

public class ButtonIconRetriever extends SwingWorker<Icon, Void> {
	private final String strImageFile;
	private final JButton btn;
    
    public ButtonIconRetriever(JButton btn, String strImageFile) {
        this.strImageFile = strImageFile;
        this.btn = btn;
        execute();
    }
    
    @Override
    protected Icon doInBackground() throws Exception {
    	return new ImageIcon(getClass().getResource(strImageFile));
    }

    @Override
    protected void done() {
        Icon icon = null;
        try {
            icon = get();
        } catch (final ExecutionException ex) {
            ex.printStackTrace();
        } catch (InterruptedException ex) {
			ex.printStackTrace();
			Thread.currentThread().interrupt();
		}
        btn.setIcon(icon);
    }
}
