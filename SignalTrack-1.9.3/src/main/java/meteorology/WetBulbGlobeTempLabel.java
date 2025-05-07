package meteorology;

import java.awt.Color;

import javax.swing.JLabel;

public class WetBulbGlobeTempLabel extends JLabel {

	private static final long serialVersionUID = -1L;

	public enum Region {
		R1, R2, R3
	}
	
	public static final String HEAT_STRESS_LOW = "LOW";
	public static final String HEAT_STRESS_ELEVATED = "ELEVATED";
	public static final String HEAT_STRESS_MODERATE = "MODERATE";
	public static final String HEAT_STRESS_HIGH = "HIGH";
	public static final String HEAT_STRESS_EXTREME = "EXTREME";
	public static final String HEAT_STRESS_OUT_OF_RANGE = "OUT OF RNG";
	
	public static final String TOOL_TIP_TEXT_LOW = "Proceed using normal precautions";
	public static final String TOOL_TIP_TEXT_ELEVATED = "Take at least 15 minutes of breaks each hour if working or exercising in direct sunlight";
	public static final String TOOL_TIP_TEXT_MODERATE = "Take at least 30 minutes of breaks each hour if working or exercising in direct sunlight";
	public static final String TOOL_TIP_TEXT_HIGH = "Take at least 40 minutes of breaks each hour if working or exercising in direct sunlight";
	public static final String TOOL_TIP_TEXT_EXTREME = "Take at least 45 minutes of breaks each hour if working or exercising in direct sunlight";
	public static final String TOOL_TIP_TEXT_OUT_OF_RANGE = "No measurements available";

	public static final Region DEFAULT_REGION = Region.R1;

	private Region region;
	private double wbgtf;
	
	public WetBulbGlobeTempLabel() {
		this(DEFAULT_REGION);
	}
	
	public WetBulbGlobeTempLabel(Region region) {
		this.region = region;
	}
	
	public void setRegion(Region region) {
		set(region, wbgtf);
	}

	public void setWbgtf(double wbgtf) {
		set(region, wbgtf);
	}
	
	public void set(Region region, double wbgtf) {
		this.region = region;
		this.wbgtf = wbgtf;
		if (region == Region.R1) {
			if (wbgtf < -254) {
				setBackground(Color.WHITE);
				setForeground(Color.BLACK);
				setText(HEAT_STRESS_OUT_OF_RANGE);
				setToolTipText(TOOL_TIP_TEXT_OUT_OF_RANGE);
			} else if (wbgtf >= -254 && wbgtf < 72.3) {
				setBackground(Color.GREEN);
				setForeground(Color.BLACK);
				setText(HEAT_STRESS_LOW);
				setToolTipText(TOOL_TIP_TEXT_LOW);
			} else if (wbgtf >= 72.3 && wbgtf <= 76.1) {
				setBackground(Color.YELLOW);
				setForeground(Color.BLACK);
				setText(HEAT_STRESS_ELEVATED);
				setToolTipText(TOOL_TIP_TEXT_ELEVATED);
			} else if (wbgtf >= 76.2 && wbgtf <= 80.1) {
				setBackground(Color.ORANGE);
				setForeground(Color.BLACK);
				setText(HEAT_STRESS_MODERATE);
				setToolTipText(TOOL_TIP_TEXT_MODERATE);
			} else if (wbgtf >= 80.1 && wbgtf <= 84.0) {
				setBackground(Color.RED);
				setForeground(Color.WHITE);
				setText(HEAT_STRESS_HIGH);
				setToolTipText(TOOL_TIP_TEXT_HIGH);
			} else if (wbgtf > 84.0) {
				setBackground(Color.BLACK);
				setForeground(Color.WHITE);
				setText(HEAT_STRESS_EXTREME);
				setToolTipText(TOOL_TIP_TEXT_EXTREME);
			}
		} else if (region == Region.R2) {
			if (wbgtf < -254) {
				setBackground(Color.WHITE);
				setForeground(Color.BLACK);
				setText(HEAT_STRESS_OUT_OF_RANGE);
				setToolTipText(TOOL_TIP_TEXT_OUT_OF_RANGE);
			} else if (wbgtf >= -254 && wbgtf < 75.9) {
				setBackground(Color.GREEN);
				setForeground(Color.BLACK);
				setText(HEAT_STRESS_LOW);
				setToolTipText(TOOL_TIP_TEXT_LOW);
			} else if (wbgtf >= 75.9 && wbgtf <= 78.7) {
				setBackground(Color.YELLOW);
				setForeground(Color.BLACK);
				setText(HEAT_STRESS_ELEVATED);
				setToolTipText(TOOL_TIP_TEXT_ELEVATED);
			} else if (wbgtf >= 78.8 && wbgtf <= 83.7) {
				setBackground(Color.ORANGE);
				setForeground(Color.BLACK);
				setText(HEAT_STRESS_MODERATE);
				setToolTipText(TOOL_TIP_TEXT_MODERATE);
			} else if (wbgtf >= 83.8 && wbgtf <= 87.6) {
				setBackground(Color.RED);
				setForeground(Color.WHITE);
				setText(HEAT_STRESS_HIGH);
				setToolTipText(TOOL_TIP_TEXT_HIGH);
			} else if (wbgtf > 87.6) {
				setBackground(Color.BLACK);
				setForeground(Color.WHITE);
				setText(HEAT_STRESS_EXTREME);
				setToolTipText(TOOL_TIP_TEXT_EXTREME);
			}
		} else if (region == Region.R3) {
			if (wbgtf < -254) {
				setBackground(Color.WHITE);
				setForeground(Color.BLACK);
				setText(HEAT_STRESS_OUT_OF_RANGE);
				setToolTipText(TOOL_TIP_TEXT_OUT_OF_RANGE);
			} else if (wbgtf >= -254 && wbgtf < 78.3) {
				setBackground(Color.GREEN);
				setForeground(Color.BLACK);
				setText(HEAT_STRESS_LOW);
				setToolTipText(TOOL_TIP_TEXT_LOW);
			} else if (wbgtf >= 78.3 && wbgtf <= 82.0) {
				setBackground(Color.YELLOW);
				setForeground(Color.BLACK);
				setText(HEAT_STRESS_ELEVATED);
				setToolTipText(TOOL_TIP_TEXT_ELEVATED);
			} else if (wbgtf >= 82.1 && wbgtf <= 86.0) {
				setBackground(Color.ORANGE);
				setForeground(Color.BLACK);
				setText(HEAT_STRESS_MODERATE);
				setToolTipText(TOOL_TIP_TEXT_MODERATE);
			} else if (wbgtf >= 86.1 && wbgtf <= 90.0) {
				setBackground(Color.RED);
				setForeground(Color.WHITE);
				setText(HEAT_STRESS_HIGH);
				setToolTipText(TOOL_TIP_TEXT_HIGH);
			} else if (wbgtf > 90.0) {
				setBackground(Color.BLACK);
				setForeground(Color.WHITE);
				setText(HEAT_STRESS_EXTREME);
				setToolTipText(TOOL_TIP_TEXT_EXTREME);
			}
		}
	}
	
}
