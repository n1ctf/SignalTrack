 package utility;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;

public class ProgressDialog extends JDialog { 
	private static final long serialVersionUID = 1L;
	
	public enum Status {CANCELED}
	
	public static final String CLOSE = "CLOSE";
	
	private static int instanceCounter;
	
	private final JLabel statusLabel = new JLabel(); 
	private final JLabel noteLabel = new JLabel();
	private final JButton cancelButton = new JButton("Cancel");
    private JProgressBar progressBar = new JProgressBar();
    
    private final boolean isIndeterminate;
    private int progress;
    private final String status;
    private final String note;
    private int min;
    private int max;
    private final boolean showCancel;
    private boolean isAnimated;
    private boolean isAnimating;
    private boolean isWaiting;
    private final int delayMillis;
    private boolean useInstanceOffset;
    
    public ProgressDialog(boolean isIndeterminate, String status, String note, int min, int max) { 
	    this(isIndeterminate, status, note, min, max, true, true, 0);	
    }
    
    public ProgressDialog(boolean isIndeterminate, String status, String note, int min, int max, 
    		boolean showCancel, boolean isAnimated) { 
	    this(isIndeterminate, status, note, min, max, showCancel, isAnimated, 0);	
    }
    
    public ProgressDialog(boolean isIndeterminate, String status, String note, int min, int max, 
    		boolean showCancel, int delayMillis) { 
	    this(isIndeterminate, status, note, min, max, showCancel, false, delayMillis);	
    }
    
    private ProgressDialog(boolean isIndeterminate, String status, String note, int min, int max, 
    		boolean showCancel, boolean isAnimated, int delayMillis) {
    	this.isIndeterminate = isIndeterminate;
    	this.status = status;
    	this.note = note;
    	this.min = min;
    	this.max = max;
    	this.showCancel = showCancel;
    	this.isAnimated = isAnimated;
    	this.delayMillis = delayMillis;
    	
    	instanceCounter++;
    	
    	final Runnable task = () -> {
			setType(Type.POPUP);
	    	setVisible(false);
	    	setAlwaysOnTop(true);
	    	setResizable(false);
	    	setModalityType(ModalityType.MODELESS);
	    	init();
	    	initGUI();
		};
		invokeLaterInDispatchThreadIfNeeded(task);
    } 

	private void init() { 
		progressBar = new JProgressBar(SwingConstants.HORIZONTAL, min, max); 
        progressBar.setOpaque(true);
        progressBar.setIndeterminate(isIndeterminate);
        progressBar.setStringPainted(!isIndeterminate);
        progressBar.setBorderPainted(true);
        progressBar.setDoubleBuffered(true);
        
        statusLabel.setText(status);

        noteLabel.setText(note); 
        
    	cancelButton.addActionListener(_ -> {
			cancelButton.setVisible(false);
			firePropertyChange(Status.CANCELED.toString(), null, true);
			setStatusText("", max);
			setNoteText("Canceling Action");
			exit();
    	});

    } 
	
	public boolean isAnimating() {
		return isAnimating;
	}

	public void setAnimating(boolean isAnimating) {
		this.isAnimating = isAnimating;
	}

	public synchronized boolean isAnimated() {
		return isAnimated;
	}

	public synchronized void exit() {
		if (isAnimated && !isAnimating && (delayMillis == 0)) {
			final Runnable task = ()-> {
				progressBar.setIndeterminate(false);
				progressBar.setStringPainted(false);
				progressBar.setValue(progressBar.getMinimum());
				cancelButton.setEnabled(false);
				waitBeforeFadeOut();
			};
			invokeLaterInDispatchThreadIfNeeded(task);
		} else if ((delayMillis > 0) && !isWaiting) {
			waitBeforeClose();
		} else if (!isWaiting && !isAnimating) {
			instanceCounter--;
			firePropertyChange(CLOSE, null, true);
			dispose();
		}
	}
	
	public void useInstanceOffset(boolean useInstanceOffset) {
		this.useInstanceOffset = useInstanceOffset;
	}
	
	public static int getInstanceCount() {
		return instanceCounter;
	}
	
	public synchronized void closeNow() {
		instanceCounter--;
		firePropertyChange(CLOSE, null, true);
		dispose();
	}
	
	public synchronized void setPosition(int position) {
		setLocation((int) (getLocationOnScreen().getX() + (position * 200)), 
				(int) (getLocationOnScreen().getY() + (position * 120)));
	}
	
	public synchronized void setMax(int max) {
		progressBar.setMaximum(max);
		progressBar.setIndeterminate(false);
		progressBar.setStringPainted(true);
		repaint();
	}
	
	public synchronized int getMax() {
		return max;
	}

	public synchronized int getMin() {
		return min;
	}
	
	public synchronized void setMin(int min) {
		this.min = min;
		progressBar.setMinimum(min);
	}
	
	public synchronized void setAnimated(boolean isAnimated) {
		this.isAnimated = isAnimated;
	}
	
	public synchronized void setProgress(int progress) {
		this.progress = progress;
		final Runnable task = () -> {
			progressBar.setValue(Math.min(progress, max));
			progressBar.repaint();
			repaint();
		};
		invokeLaterInDispatchThreadIfNeeded(task);
		setIndeterminate(false);
		if (progress >= max) {
			exit();
		}
	}
	
	public synchronized int getProgress() {
		return progress;
	}
	
	public synchronized void setIndeterminate(boolean isIndeterminate) {
		final Runnable task = () -> {
			progressBar.setIndeterminate(isIndeterminate);
			progressBar.setStringPainted(!isIndeterminate);
		};
		invokeLaterInDispatchThreadIfNeeded(task);
	}
	
	public synchronized void setStatusText(String status, int progress) {
		this.progress = progress;
		invokeLaterInDispatchThreadIfNeeded(() -> {
			statusLabel.setText(status);
			statusLabel.repaint();
			setProgress(Math.min(progress, max));
		});
	}
	
	public synchronized void setStatusText(String status) {
		final Runnable task = () -> {
			statusLabel.setText(status);
			statusLabel.repaint();
		};
		invokeLaterInDispatchThreadIfNeeded(task);
	}
	
	public synchronized void setNoteText(String note) {
		final Runnable task = () -> noteLabel.setText(note);
		invokeLaterInDispatchThreadIfNeeded(task);
	}
	
	private synchronized void waitBeforeFadeOut() {
	    final var timerDelay = 300;
	    new Timer(timerDelay, event -> { 
	        	instanceCounter--;
	            ((Timer) event.getSource()).stop();
	            fadeOut();
	        }
	    ).start();
	}
	
	private synchronized void waitBeforeClose() {
		final Runnable dispose = () -> {
			instanceCounter--;
			firePropertyChange(CLOSE, null, true);
			dispose();
		};
    	isWaiting = true;
    	try (final ScheduledExecutorService waitScheduler = Executors.newSingleThreadScheduledExecutor()) {
    		waitScheduler.schedule(dispose, delayMillis, TimeUnit.MILLISECONDS);
    	}
	}
	
	private synchronized void fadeOut() {
		isAnimating = true;
	    final int timerDelay = 20;
	    new Timer(timerDelay, new ActionListener() {
	        private float opacity = 1.0F;
	        @Override
	        public void actionPerformed(ActionEvent e) {
	            if (opacity <= 0.0F){
	                ((Timer)e.getSource()).stop();
	                firePropertyChange(CLOSE, null, true);
	                dispose();
	            } else {
	            	setOpacity(opacity);
	            	opacity -= 0.05F;
	            }
	        }
	    }).start();
	}

	private void initGUI() {
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        
		setUndecorated(isAnimated());
        
		progressBar.setStringPainted(false);
        
        statusLabel.setFont(new Font("Calabri", Font.BOLD, 13));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setOpaque(false);
        
        noteLabel.setFont(new Font("Calabri", Font.BOLD, 17));
        noteLabel.setHorizontalAlignment(SwingConstants.CENTER);
        noteLabel.setOpaque(false);
        
        cancelButton.setVisible(showCancel);
        
        final GroupLayout layout = new GroupLayout(getContentPane());
        
        setLayout(layout);
        
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
            	.addGap(15,15,15)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                	.addComponent(noteLabel, GroupLayout.Alignment.CENTER, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(statusLabel, GroupLayout.Alignment.CENTER, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                	.addComponent(progressBar, GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
                	.addComponent(cancelButton, 90,90,90))
                .addGap(15,15,15)));
        
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(15,15,15)
                .addComponent(noteLabel, 25,25,25)
                .addGap(10,10,10)
                .addComponent(statusLabel, 25,25,25)
                .addGap(15,15,15)
                .addComponent(progressBar, 22,22,22)
                .addGap(15,15,15)
                .addComponent(cancelButton, 25,25,25)
                .addContainerGap(10,10)));
        
        pack();
        
        final Toolkit tk = Toolkit.getDefaultToolkit();
        final Dimension screenSize = tk.getScreenSize();
        
        if (useInstanceOffset) {
	        setLocation(((screenSize.width / 2) - (getWidth() / 2)) + ((instanceCounter - 1) * 60), 
	        		((screenSize.height / 2) - (getHeight() / 2)) + ((instanceCounter - 1) * 20));
        } else {
        	setLocation((screenSize.width / 2) - (getWidth() / 2), (screenSize.height / 2) - (getHeight() / 2));
        }
        
        setVisible(true);
    }

	private static synchronized void invokeLaterInDispatchThreadIfNeeded(Runnable runnable) {
		if (EventQueue.isDispatchThread()) {
			runnable.run();
		} else {
			SwingUtilities.invokeLater(runnable);
		}
	}

} 
