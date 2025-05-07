package gov.nasa.api.donki;

import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

public class RSGPanel {
	public static final Font DEFAULT_FONT = new Font("Arial", Font.BOLD, 12);

	private final JLabel lblR;
	private final JLabel lblS;
	private final JLabel lblG;
	
	/**
	 * Create the panel.
	 */
	public RSGPanel() {
		this.lblR = new JLabel();
		this.lblS = new JLabel();
		this.lblG = new JLabel();
		
		lblR.setFont(DEFAULT_FONT);
		lblS.setFont(DEFAULT_FONT);
		lblG.setFont(DEFAULT_FONT);
		
		lblR.setHorizontalAlignment(SwingConstants.CENTER);
		lblS.setHorizontalAlignment(SwingConstants.CENTER);
		lblG.setHorizontalAlignment(SwingConstants.CENTER);
		
		lblR.setVerticalAlignment(SwingConstants.CENTER);
		lblS.setVerticalAlignment(SwingConstants.CENTER);
		lblG.setVerticalAlignment(SwingConstants.CENTER);
		
		lblR.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		lblR.setOpaque(true);
		lblR.setText("R0");
		lblR.setBackground(Color.GREEN);
		
		lblS.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		lblS.setOpaque(true);
		lblS.setText("S0");
		lblS.setBackground(Color.GREEN);
		
		lblG.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		lblG.setOpaque(true);
		lblG.setText("G0");
		lblG.setBackground(Color.GREEN);
	}
	
	public void setRadioBlackout(RadioBlackout r) {
		lblR.setText(r.name());
		lblR.setBackground(getColor(r.ordinal()));
	}
	
	public void setSolarRadiationStorm(SolarRadiationStorm s) {
		lblS.setText(s.name());
		lblS.setBackground(getColor(s.ordinal()));
	}
	
	public void setGeomagneticStorm(GeomagneticStorm g) {
		lblG.setText(g.name());
		lblG.setBackground(getColor(g.ordinal()));
	}
	
	private Color getColor(int i) {
		return switch (i) {
			case 0 -> new Color(0, 255, 0);
			case 1 -> new Color(255, 255, 0);
			case 2 -> new Color(255, 204, 0);
			case 3 -> new Color(255, 102, 0);
			case 4 -> new Color(255, 0, 0);
			case 5 -> new Color(126, 0, 35);
			default -> Color.LIGHT_GRAY;
		};
	}
	
	public JPanel getPanel() {
		final JPanel panel = new JPanel();
		final GroupLayout layout = new GroupLayout(panel);
        
		panel.setBorder(BorderFactory.createTitledBorder(null, "Solar Alerts",
				TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", 1, 10)));
		
		panel.setLayout(layout);
        
		layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addComponent(lblR, 40, 40, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblS, 40, 40, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblG, 40, 40, Short.MAX_VALUE)
                .addGap(2, 2, 2))
        );
		
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lblR, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblS, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblG, javax.swing.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE))
                .addGap(2, 2, 2))
        );

        return panel;
	}

}
