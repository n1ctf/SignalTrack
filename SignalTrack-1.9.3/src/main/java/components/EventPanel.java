package components;

import java.awt.EventQueue;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import javax.swing.SwingUtilities;

import javax.swing.border.TitledBorder;

public class EventPanel {
	
	private JTextArea jtaEventNarrative;
	
	public EventPanel() {
		initializeComponents();
	}

	private void initializeComponents() {
		invokeLaterInDispatchThreadIfNeeded(() -> jtaEventNarrative = new JTextArea());
	}

	public void setEventNarrative(String text) {
		invokeLaterInDispatchThreadIfNeeded(() -> jtaEventNarrative.setText(text));
	}

	public void appendEventNarrative(String text) {
		appendEventNarrative(text, false);
	}
	
	public void appendEventNarrative(String text, boolean allowDuplication) {
		if (allowDuplication || !isDuplicate(text)  ) {
			invokeLaterInDispatchThreadIfNeeded(() -> jtaEventNarrative.append(text));
		}
	}
	
	private boolean isDuplicate(String text) {
		return jtaEventNarrative.getText().trim().contains(text.trim()); 
	}

	public JScrollPane getEventNarrativeScrollPane() {
		jtaEventNarrative.setColumns(1);
		jtaEventNarrative.setRows(100);
        
		final JScrollPane jspEventNarrative = new JScrollPane();
		jspEventNarrative.setViewportView(jtaEventNarrative);
		
		return jspEventNarrative;
	}
	
	public JPanel getEventNarrativePanelFoundation7InchDisplay() {
		final JPanel panel = new JPanel();
		
		final GroupLayout layout = new GroupLayout(panel);
		
        panel.setLayout(layout);
		
        panel.setBorder(BorderFactory.createTitledBorder(null, "Event Narrative",
				TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", Font.BOLD, 12)));
    	
        jtaEventNarrative.setColumns(1);
		jtaEventNarrative.setRows(100);
		jtaEventNarrative.setFont(new Font("Tahoma", Font.PLAIN, 10));

		final JScrollPane jspEventNarrative = new JScrollPane();
		jspEventNarrative.setViewportView(jtaEventNarrative);

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
				.addGap(1, 1, 1)
				.addComponent(jspEventNarrative, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addGap(1, 1, 1)));

		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGap(1, 1, 1)
			.addComponent(jspEventNarrative, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
			.addGap(1, 1, 1));

		return panel;
	}
	
	public JPanel getEventNarrativePanel() {
		final JPanel panel = new JPanel();
		
		final GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
		
        panel.setBorder(BorderFactory.createTitledBorder(null, "Event Narrative",
				TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", Font.BOLD, 14)));
    	
        jtaEventNarrative.setColumns(1);
		jtaEventNarrative.setRows(100);
        
		final JScrollPane jspEventNarrative = new JScrollPane();
		jspEventNarrative.setViewportView(jtaEventNarrative);

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
				.addGap(2, 2, 2)
				.addComponent(jspEventNarrative, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addGap(2, 2, 2)));

		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGap(2, 2, 2)
			.addComponent(jspEventNarrative, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
			.addGap(2, 2, 2));

		return panel;
	}

	private static void invokeLaterInDispatchThreadIfNeeded(Runnable runnable) {
		if (EventQueue.isDispatchThread()) {
			runnable.run();
		} else {
			SwingUtilities.invokeLater(runnable);
		}
	}
}
