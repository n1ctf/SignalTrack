package utility;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CPUUsageCollector extends JFrame {
	private static final long serialVersionUID = -5325803425005189037L;
	private static final Log LOG = LogFactory.getLog(CPUUsageCollector.class.getName());
	public static final String LOAD = "LOAD";
	private static final long INTERVAL = 10000L; // polling interval in ms
	private static double load; // average load over the interval
	private long totalCpuTime ; // total CPU time in millis
	private double lastLoad;
	private final transient ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
	private final transient ScheduledExecutorService loadTestScheduler = Executors.newScheduledThreadPool(1);
	private transient Runnable testLoad;
	
	private JButton btnClose;
    private JButton btnMinimize;
    
    private JLabel lblCPUTime;
    private JLabel lblCPUTimeValue;
    private JLabel lblCPUFreq;
    private JLabel lblCPUFreqValue;
    private JLabel lblCPULoad;
    private JLabel lblCPULoadValue;
    private JLabel lblOS;
    private JLabel lblOSValue;
    private JLabel lblProcessors;
    private JLabel lblProcessorsValue;
    private JLabel lblThreads;
    private JLabel lblThreadsValue;
    private JLabel lblTitle;
    private JLabel instLabel;
    
    public CPUUsageCollector(JLabel instanceLabel) {
    	this.instLabel = instanceLabel;
    	super.setVisible(false);
    }
    
	public CPUUsageCollector() { 
		initialize();
		configureComponents();
		super.getContentPane().setLayout(getGUI());
		lblOSValue.setText(System.getProperty("os.name"));
		checkLoad();
		super.setSize(275,325);
		super.setVisible(true);
	}
	
	private void checkLoad() {
		testLoad = () -> {
			final long[] ids = threadMXBean.getAllThreadIds();
			EventQueue.invokeLater(() -> lblThreadsValue.setText(String.valueOf(ids.length)));
			long time = 0L;
			for (final long id: ids) {
				final long l = threadMXBean.getThreadCpuTime(id);
				if (l >= 0L) {
					time += l;
				}
			}
			final long newCpuTime = time / 1000000L;
			EventQueue.invokeLater(() -> lblCPUTimeValue.setText(String.valueOf(newCpuTime) + " nS"));
			synchronized(this) {
				final long oldCpuTime = totalCpuTime;
				totalCpuTime = newCpuTime;
				// load = CPU time difference / sum of elapsed time for all CPUs
				EventQueue.invokeLater(() -> lblProcessorsValue.setText(String.valueOf(Runtime.getRuntime().availableProcessors())));
				load = (double) (newCpuTime - oldCpuTime) / 
						(double) (INTERVAL * Runtime.getRuntime().availableProcessors());
				if (load <= 0.01 || load > 1.0) {
					load = 0.01;
				}
				firePropertyChange(LOAD, lastLoad, load);
				if (instLabel != null) {
					EventQueue.invokeLater(() -> instLabel.setText(String.valueOf(load) + " %"));
				}
				EventQueue.invokeLater(() -> lblCPULoadValue.setText(String.valueOf(load * 100) + " %"));
				if (!Utility.equals(lastLoad, load)) {
					LOG.info(load);
				}
				lastLoad = load;
			}	
		};
		loadTestScheduler.scheduleAtFixedRate(testLoad, INTERVAL, INTERVAL, TimeUnit.MILLISECONDS);
	}


	public double getLoad() {
		synchronized(testLoad) {
			return load;
		}
	}

	public boolean isTerminated() {
		return loadTestScheduler.isTerminated();
	}

	public void shutdownNow() {
		loadTestScheduler.shutdownNow();
	}
	
	private void initialize() {
		lblCPUTime = new JLabel();
        btnClose = new JButton();
        btnMinimize = new JButton();
        lblProcessors = new JLabel();
        lblCPULoad = new JLabel();
        lblCPUFreq = new JLabel();
        lblThreads = new JLabel();
        lblOS = new JLabel();
        lblTitle = new JLabel();
        lblOSValue = new JLabel();
        lblThreadsValue = new JLabel();
        lblCPUTimeValue = new JLabel();
        lblProcessorsValue = new JLabel();
        lblCPUFreqValue = new JLabel();
        lblCPULoadValue = new JLabel();
	}
	
	private void configureComponents() {
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		
		lblCPUTime.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        lblCPUTime.setHorizontalAlignment(SwingConstants.CENTER);
        lblCPUTime.setText("CPU Time");
        lblCPUTime.setBorder(BorderFactory.createEtchedBorder());

        btnClose.setText("Close");

        btnMinimize.setText("Minimize");

        lblProcessors.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        lblProcessors.setHorizontalAlignment(SwingConstants.CENTER);
        lblProcessors.setText("Processors");
        lblProcessors.setBorder(BorderFactory.createEtchedBorder());

        lblCPULoad.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        lblCPULoad.setHorizontalAlignment(SwingConstants.CENTER);
        lblCPULoad.setText("CPU Load");
        lblCPULoad.setBorder(BorderFactory.createEtchedBorder());

        lblCPUFreq.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        lblCPUFreq.setHorizontalAlignment(SwingConstants.CENTER);
        lblCPUFreq.setText("CPU Freq");
        lblCPUFreq.setToolTipText("");
        lblCPUFreq.setBorder(BorderFactory.createEtchedBorder());

        lblThreads.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        lblThreads.setHorizontalAlignment(SwingConstants.CENTER);
        lblThreads.setText("Threads");
        lblThreads.setBorder(BorderFactory.createEtchedBorder());

        lblOS.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        lblOS.setHorizontalAlignment(SwingConstants.CENTER);
        lblOS.setText("OS");
        lblOS.setBorder(BorderFactory.createEtchedBorder());

        lblTitle.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitle.setText("System Monitor");
        lblTitle.setToolTipText("");
        lblTitle.setBorder(BorderFactory.createEtchedBorder());

        lblOSValue.setBackground(new java.awt.Color(255, 255, 255));
        lblOSValue.setHorizontalAlignment(SwingConstants.CENTER);
        lblOSValue.setToolTipText("");
        lblOSValue.setBorder(BorderFactory.createEtchedBorder());
        lblOSValue.setHorizontalTextPosition(SwingConstants.RIGHT);
        lblOSValue.setOpaque(true);

        lblThreadsValue.setBackground(new java.awt.Color(255, 255, 255));
        lblThreadsValue.setHorizontalAlignment(SwingConstants.CENTER);
        lblThreadsValue.setToolTipText("");
        lblThreadsValue.setBorder(BorderFactory.createEtchedBorder());
        lblThreadsValue.setHorizontalTextPosition(SwingConstants.RIGHT);
        lblThreadsValue.setOpaque(true);

        lblCPUTimeValue.setBackground(new java.awt.Color(255, 255, 255));
        lblCPUTimeValue.setHorizontalAlignment(SwingConstants.CENTER);
        lblCPUTimeValue.setToolTipText("");
        lblCPUTimeValue.setBorder(BorderFactory.createEtchedBorder());
        lblCPUTimeValue.setHorizontalTextPosition(SwingConstants.RIGHT);
        lblCPUTimeValue.setOpaque(true);

        lblProcessorsValue.setBackground(new java.awt.Color(255, 255, 255));
        lblProcessorsValue.setHorizontalAlignment(SwingConstants.CENTER);
        lblProcessorsValue.setToolTipText("");
        lblProcessorsValue.setBorder(BorderFactory.createEtchedBorder());
        lblProcessorsValue.setHorizontalTextPosition(SwingConstants.RIGHT);
        lblProcessorsValue.setOpaque(true);

        lblCPUFreqValue.setBackground(new java.awt.Color(255, 255, 255));
        lblCPUFreqValue.setHorizontalAlignment(SwingConstants.CENTER);
        lblCPUFreqValue.setToolTipText("");
        lblCPUFreqValue.setBorder(BorderFactory.createEtchedBorder());
        lblCPUFreqValue.setHorizontalTextPosition(SwingConstants.RIGHT);
        lblCPUFreqValue.setOpaque(true);

        lblCPULoadValue.setBackground(new java.awt.Color(255, 255, 255));
        lblCPULoadValue.setHorizontalAlignment(SwingConstants.CENTER);
        lblCPULoadValue.setToolTipText("");
        lblCPULoadValue.setBorder(BorderFactory.createEtchedBorder());
        lblCPULoadValue.setHorizontalTextPosition(SwingConstants.RIGHT);
        lblCPULoadValue.setOpaque(true);
        
        btnClose.addActionListener(_ -> dispose());
        btnMinimize.addActionListener(this::btnMinimizeActionListenerEvent);
	}

	private void btnMinimizeActionListenerEvent(ActionEvent event) {
		// TODO
	}
	
	private GroupLayout getGUI() {
		final GroupLayout layout = new GroupLayout(getContentPane());
	        
	        layout.setHorizontalGroup(
	            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
	            .addGroup(layout.createSequentialGroup()
	                .addContainerGap()
	                .addComponent(lblTitle, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	                .addContainerGap())
	            .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
	                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
	                    .addGroup(layout.createSequentialGroup()
	                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	                        .addComponent(btnMinimize, GroupLayout.PREFERRED_SIZE, 85, GroupLayout.PREFERRED_SIZE)
	                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
	                        .addComponent(btnClose, GroupLayout.PREFERRED_SIZE, 85, GroupLayout.PREFERRED_SIZE))
	                    .addGroup(layout.createSequentialGroup()
	                        .addGap(20, 20, 20)
	                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
	                            .addComponent(lblProcessors, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	                            .addComponent(lblCPUFreq, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	                            .addComponent(lblCPULoad, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	                            .addComponent(lblThreads, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	                            .addComponent(lblOS, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	                            .addComponent(lblCPUTime, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
	                        .addGap(10, 10, 10)
	                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
	                            .addComponent(lblOSValue, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 115, GroupLayout.PREFERRED_SIZE)
	                            .addComponent(lblThreadsValue, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 115, GroupLayout.PREFERRED_SIZE)
	                            .addComponent(lblCPUTimeValue, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 115, GroupLayout.PREFERRED_SIZE)
	                            .addComponent(lblProcessorsValue, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 115, GroupLayout.PREFERRED_SIZE)
	                            .addComponent(lblCPUFreqValue, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 115, GroupLayout.PREFERRED_SIZE)
	                            .addComponent(lblCPULoadValue, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 115, GroupLayout.PREFERRED_SIZE))))
	                .addGap(20, 20, 20)));
	        
	        layout.setVerticalGroup(
	            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
	            .addGroup(layout.createSequentialGroup()
	                .addGap(8, 8, 8)
	                .addComponent(lblTitle, GroupLayout.PREFERRED_SIZE, 33, GroupLayout.PREFERRED_SIZE)
	                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
	                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
	                    .addComponent(lblOS)
	                    .addComponent(lblOSValue))
	                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
	                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
	                    .addComponent(lblThreadsValue, 20, 20, 20)
	                    .addComponent(lblThreads))
	                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
	                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
	                    .addComponent(lblCPUTimeValue, 20, 20, 20)
	                    .addComponent(lblCPUTime))
	                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
	                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
	                    .addComponent(lblProcessorsValue, 20, 20, 20)
	                    .addComponent(lblProcessors))
	                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
	                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
	                    .addComponent(lblCPUFreqValue, 20, 20, 20)
	                    .addComponent(lblCPUFreq))
	                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
	                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
	                    .addComponent(lblCPULoadValue, 20, 20, 20)
	                    .addComponent(lblCPULoad))
	                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 30, Short.MAX_VALUE)
	                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
	                    .addComponent(btnMinimize)
	                    .addComponent(btnClose))
	                .addContainerGap()));

	        pack();
	        return layout;
	}
	
	public static void main(String[] args) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(CPUUsageCollector.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } 
        //</editor-fold>

        /* Create and display the form */
        EventQueue.invokeLater(() -> new CPUUsageCollector().setVisible(true));
    }
}
