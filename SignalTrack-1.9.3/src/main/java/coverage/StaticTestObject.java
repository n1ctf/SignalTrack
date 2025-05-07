package coverage;

import java.awt.Color;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class StaticTestObject {
	private double maxAllowedDeviation;
	private double cursorDiameter;
	private double traceDiameter;
	private double intersectPointDiameter;
	private boolean isShowFlightPaths;
	private boolean isShowAsymptotes;
	private boolean isShowCursors;
	private boolean isShapesEnabled;
	private boolean isShowTraces;
	private boolean isTraceEqualsFlightColor;
	private boolean isShowTargetRing;
	private boolean isShowIntersectPoints;
	private Color[] flightColor = new Color[14];
	private Color asymptoteColor;
	private Color cursorColor;
	private Color traceColor;
	private Color targetRingColor;
	private Color intersectPointColor;
	
	private final File testFile;
	
	private static final Logger log = Logger.getLogger(StaticTestObject.class.getName());
	public static final String APPLY_SETTINGS = "APPLY_SETTINGS";
	
    private static final Preferences userPref = Preferences.userRoot().node("jdrivetrack/prefs/StaticTestSettings");
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	
	public StaticTestObject(boolean clearAllPreferences, File testFile) {
		if (clearAllPreferences) {
			clearAllPreferences();
		}
		this.testFile = testFile;
		getSettingsFromRegistry();
	}

	public double getCursorDiameter() {
		return cursorDiameter;
	}

	public void setCursorDiameter(double cursorDiameter) {
		this.cursorDiameter = cursorDiameter;
	}

	public double getTraceDiameter() {
		return traceDiameter;
	}

	public void setTraceDiameter(double traceDiameter) {
		this.traceDiameter = traceDiameter;
	}

	public double getIntersectPointDiameter() {
		return intersectPointDiameter;
	}

	public void setIntersectPointDiameter(double intersectPointDiameter) {
		this.intersectPointDiameter = intersectPointDiameter;
	}

	public double getMaxAllowedDeviation() {
		return maxAllowedDeviation;
	}

	public void setMaxAllowedDeviation(double maxAllowedDeviation) {
		this.maxAllowedDeviation = maxAllowedDeviation;
	}

	public boolean isShowFlightPaths() {
		return isShowFlightPaths;
	}

	public void setShowFlightPaths(boolean isShowFlightPaths) {
		this.isShowFlightPaths = isShowFlightPaths;
	}

	public boolean isShowAsymptotes() {
		return isShowAsymptotes;
	}

	public void setShowAsymptotes(boolean isShowAsymptotes) {
		this.isShowAsymptotes = isShowAsymptotes;
	}

	public boolean isShapesEnabled() {
		return isShapesEnabled;
	}

	public void setShapesEnabled(boolean isShapesEnabled) {
		this.isShapesEnabled = isShapesEnabled;
	}

	public boolean isShowCursors() {
		return isShowCursors;
	}

	public void setShowCursors(boolean isShowCursors) {
		this.isShowCursors = isShowCursors;
	}

	public boolean isShowTraces() {
		return isShowTraces;
	}

	public void setShowTraces(boolean isShowTraces) {
		this.isShowTraces = isShowTraces;
	}

	public boolean isTraceEqualsFlightColor() {
		return isTraceEqualsFlightColor;
	}

	public void setTraceEqualsFlightColor(boolean isTraceEqualsFlightColor) {
		this.isTraceEqualsFlightColor = isTraceEqualsFlightColor;
	}

	public boolean isShowTargetRing() {
		return isShowTargetRing;
	}

	public void setShowTargetRing(boolean isShowTargetRing) {
		this.isShowTargetRing = isShowTargetRing;
	}

	public boolean isShowIntersectPoints() {
		return isShowIntersectPoints;
	}

	public void setShowIntersectPoints(boolean isShowIntersectPoints) {
		this.isShowIntersectPoints = isShowIntersectPoints;
	}

	public Color[] getFlightColor() {
		return flightColor;
	}

	public void setFlightColor(Color[] flightColor) {
		this.flightColor = flightColor.clone();
	}

	public Color getAsymptoteColor() {
		return asymptoteColor;
	}

	public void setAsymptoteColor(Color asymptoteColor) {
		this.asymptoteColor = asymptoteColor;
	}

	public Color getCursorColor() {
		return cursorColor;
	}

	public void setCursorColor(Color cursorColor) {
		this.cursorColor = cursorColor;
	}

	public Color getTraceColor() {
		return traceColor;
	}

	public void setTraceColor(Color traceColor) {
		this.traceColor = traceColor;
	}

	public Color getTargetRingColor() {
		return targetRingColor;
	}

	public void setTargetRingColor(Color targetRingColor) {
		this.targetRingColor = targetRingColor;
	}

	public Color getIntersectPointColor() {
		return intersectPointColor;
	}

	public void setIntersectPointColor(Color intersectPointColor) {
		this.intersectPointColor = intersectPointColor;
	}

	public File getTestFile() {
		return testFile;
	}
	
	private void getSettingsFromRegistry() {
		maxAllowedDeviation = userPref.getDouble(testFile.getName() + "_MaxAllowedDeviation", 60.0);
		cursorDiameter = userPref.getDouble(testFile.getName() + "_CursorDiameter", 3);
		traceDiameter = userPref.getDouble(testFile.getName() + "_TraceDiameter", 3);
		intersectPointDiameter = userPref.getDouble(testFile.getName() + "_IntersectPointDiameter", 3);

		isShowFlightPaths = userPref.getBoolean(testFile.getName() + "_ShowFlightPaths", false);
		isShowAsymptotes = userPref.getBoolean(testFile.getName() + "_ShowAsymptotes", false);
		isShowCursors = userPref.getBoolean(testFile.getName() + "_ShowCursors", false);
		isShowTraces = userPref.getBoolean(testFile.getName() + "_ShowTraces", false);
		isTraceEqualsFlightColor = userPref.getBoolean(testFile.getName() + "_TraceEqualsFlightColor", false);
		isShowTargetRing = userPref.getBoolean(testFile.getName() + "_ShowTargetRing", false);
		isShowIntersectPoints = userPref.getBoolean(testFile.getName() + "_ShowIntersectPoints", false);
		isShapesEnabled = userPref.getBoolean(testFile.getName() + "_ShapesEnabled", false);
		
		asymptoteColor = new Color(userPref.getInt(testFile.getName() + "_AsymptoteColor", Color.CYAN.getRGB()));
		cursorColor = new Color(userPref.getInt(testFile.getName() + "_CursorColor", Color.BLACK.getRGB()));
		traceColor = new Color(userPref.getInt(testFile.getName() + "_TraceColor", Color.BLUE.getRGB()));
		targetRingColor = new Color(userPref.getInt(testFile.getName() + "_TargetRingColor", Color.RED.getRGB()));
		intersectPointColor = new Color(userPref.getInt(testFile.getName() + "_IntersectPointColor", Color.GREEN.getRGB()));

		flightColor[0] = new Color(userPref.getInt(testFile.getName() + "_FlightColor" + 0, 0xFF0000));
		flightColor[1] = new Color(userPref.getInt(testFile.getName() + "_FlightColor" + 1, 0x00FF00));
		flightColor[2] = new Color(userPref.getInt(testFile.getName() + "_FlightColor" + 2, 0x0000FF));
		flightColor[3] = new Color(userPref.getInt(testFile.getName() + "_FlightColor" + 3, 0x00FFFF));
		flightColor[4] = new Color(userPref.getInt(testFile.getName() + "_FlightColor" + 4, 0xFF00FF));
		flightColor[5] = new Color(userPref.getInt(testFile.getName() + "_FlightColor" + 5, 0xFFFF00));
		flightColor[6] = new Color(userPref.getInt(testFile.getName() + "_FlightColor" + 6, 0x774477));
		flightColor[7] = new Color(userPref.getInt(testFile.getName() + "_FlightColor" + 7, 0x9966FF));
		flightColor[8] = new Color(userPref.getInt(testFile.getName() + "_FlightColor" + 8, 0x0099FF));
		flightColor[9] = new Color(userPref.getInt(testFile.getName() + "_FlightColor" + 9, 0x66CC00));
		flightColor[10] = new Color(userPref.getInt(testFile.getName() + "_FlightColor" + 10, 0x9900FF));
		flightColor[11] = new Color(userPref.getInt(testFile.getName() + "_FlightColor" + 11, 0xCC99FF));
		flightColor[12] = new Color(userPref.getInt(testFile.getName() + "_FlightColor" + 12, 0xCC0066));
		flightColor[13] = new Color(userPref.getInt(testFile.getName() + "_FlightColor" + 13, 0xFFCC66));
	}
	
	private void clearAllPreferences() {
		try {
			StaticTestObject.userPref.clear();
		} catch (final BackingStoreException ex) {
			log.log(Level.WARNING, ex.getMessage());
		}
	}
	
	public void saveSettings() {		
		userPref.putBoolean(testFile.getName() + "_ShowFlightPaths", isShowFlightPaths);
		userPref.putBoolean(testFile.getName() + "_ShowCursors", isShowCursors);
		userPref.putBoolean(testFile.getName() + "_ShowAsymptotes", isShowAsymptotes);
		userPref.putBoolean(testFile.getName() + "_ShowTraces", isShowTraces);
		userPref.putBoolean(testFile.getName() + "_TraceEqualsFlightColor", isTraceEqualsFlightColor);
		userPref.putBoolean(testFile.getName() + "_ShowTargetRing", isShowTargetRing);
		userPref.putBoolean(testFile.getName() + "_ShowIntersectPoints", isShowIntersectPoints);
		userPref.putBoolean(testFile.getName() + "_ShapesEnabled", isShapesEnabled);
		userPref.putDouble(testFile.getName() + "_MaxAllowedDeviation", maxAllowedDeviation);
		userPref.putDouble(testFile.getName() + "_CursorDiameter", cursorDiameter);
		userPref.putDouble(testFile.getName() + "_TraceDiameter", traceDiameter);
		userPref.putDouble(testFile.getName() + "_IntersectPointDiameter", intersectPointDiameter);
		userPref.putInt(testFile.getName() + "_AsymptoteColor", asymptoteColor.getRGB());
		userPref.putInt(testFile.getName() + "_TraceColor", traceColor.getRGB());
		userPref.putInt(testFile.getName() + "_CursorColor", cursorColor.getRGB());
		userPref.putInt(testFile.getName() + "_TargetRingColor", targetRingColor.getRGB());
		userPref.putInt(testFile.getName() + "_IntersectPointColor", intersectPointColor.getRGB());

		for (int i = 0; i < flightColor.length; i++) {
			userPref.putInt(testFile.getName() + "_FlightColor" + i, flightColor[i].getRGB()); 
		}
		
		pcs.firePropertyChange(APPLY_SETTINGS, null, null);
	}

	public PropertyChangeSupport getPropertyChangeSupport() {
		return pcs;
	}

}
