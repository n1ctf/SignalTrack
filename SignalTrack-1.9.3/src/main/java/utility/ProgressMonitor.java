package utility;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ProgressMonitor { 
    private int maximum;
    private int minimum;
    private int progress = -1; 
    private boolean indeterminate; 
    private int millisToDecideToPopup;
    private int millisToPopup;
    private String status; 
    private String title;
    private int orient;
    private String note;
    private boolean visible = true;
    
    private final List<ChangeListener> listeners = new ArrayList<>(); 
    private final ChangeEvent ce = new ChangeEvent(this); 
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    public ProgressMonitor() { 
    	this("Progress...", SwingConstants.HORIZONTAL, 0, 100, true, 500, 2000);  
    }
    
    public ProgressMonitor(boolean indeterminate) { 
    	this("Progress...", SwingConstants.HORIZONTAL, 0, 100, indeterminate, 500, 2000); 
    }
    
    public ProgressMonitor(int maximum) { 
    	this("Progress...", SwingConstants.HORIZONTAL, 0, maximum, true, 500, 2000);  
    }

    public ProgressMonitor(int maximum, boolean indeterminate) { 
        this("Progress...", SwingConstants.HORIZONTAL, 0, maximum, indeterminate, 500, 2000); 
    } 
    
    public ProgressMonitor(String title, int orient, int minimum, int maximum, boolean indeterminate, int millisToDecideToPopup, int millisToPopup) { 
        this.title = title;
    	this.orient = orient;
    	this.maximum = maximum; 
        this.minimum = minimum;
        this.indeterminate = indeterminate; 
        this.millisToDecideToPopup = millisToDecideToPopup; 
        this.millisToPopup = millisToPopup;
    } 
    
    public void setVisible(boolean visible) {
    	this.visible = visible;
    }
    
    public boolean isVisible() {
    	return visible;
    }
    
    public String getTitle() { 
        return title; 
    } 
    
    public void setTitle(String title) {
    	this.title = title;
    	fireChangeEvent();
    }
    
    public int getOrientation() { 
        return orient; 
    } 
    
    public void setOrientation(int orient) {
    	this.orient = orient;
    }
    
    public int getMaximum() { 
        return maximum; 
    } 
    
    public void setMaximum(int maximum) {
    	this.maximum = maximum;
    }
    
    public int getMinimum() { 
        return minimum; 
    } 
    
    public void setMinimum(int minimum) {
    	this.minimum = minimum;
    }

    public String getNote() { 
        return note; 
    } 
    
    public void setNote(String note) {
    	this.note = note;
    	fireChangeEvent();
    }
    
    public void setStatus(String status) { 
    	this.status = status;
    	fireChangeEvent();
    }
    
    public void setProgress(int progress) {
    	this.progress = progress;
    	fireChangeEvent();
    }
    
    public void setIndeterminate(boolean indeterminate) {
    	this.indeterminate = indeterminate; 
    }
    
    public void setMillisToDecideToPopup(int millisToDecideToPopup) {
    	this.millisToDecideToPopup = millisToDecideToPopup;
    }
    
    public void setMillisToPopup(int millisToPopup) {
    	this.millisToPopup = millisToPopup;
    }
    
    public void start(String status) { 
        if(progress != -1) {
			throw new IllegalStateException("Not Started Yet");
		} 
        this.status = status; 
        progress = 0; 
        fireChangeEvent(); 
    } 

    public void start() {
    	start(status);
    }
    
    public int getMillisToDecideToPopup() { 
        return millisToDecideToPopup; 
    } 
 
    public int getMillisToPopup() {
    	return millisToPopup;
    }
    
    public int getProgress() { 
        return progress; 
    } 
 
    public String getStatus() { 
        return status; 
    } 
 
    public boolean isIndeterminate() { 
        return indeterminate; 
    } 

    public void setProgress(String status, int progress) { 
        if(progress == -1) {
			throw new IllegalStateException("Not Started Yet");
		} 
        this.progress = progress; 
        if(status != null) {
			this.status = status;
		} 
        fireChangeEvent(); 
    } 

    protected void addChangeListener(ChangeListener listener) { 
        listeners.add(listener); 
    } 
 
    protected void removeChangeListener(ChangeListener listener) { 
        listeners.remove(listener); 
    } 
 
    protected void notifyCancel() {
    	pcs.firePropertyChange("CANCEL", null, null);
    }
    
    private void fireChangeEvent() { 
        final Iterator<ChangeListener> iter = listeners.iterator(); 
        while(iter.hasNext()) { 
            iter.next().stateChanged(ce); 
        } 
    }

	public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }
}
