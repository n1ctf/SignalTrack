package radiolocation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jxmapviewer.Style;
import org.openstreetmap.MapMarkerCircle;
import org.openstreetmap.MapPolylineImpl;

import coverage.StaticMeasurement;
import geometry.Coordinate;
import geometry.ICoordinate;
import map.MapMarker.STYLE;

public class HyperbolicProjection extends ConicSection {

 	private boolean showTrace = false;
	private Color traceColor;
	private double traceRadius;
	private final List<MapMarkerCircle> traceMarkers;
	
	private boolean showCursor = false;
	private Color cursorColor;
	private double cursorRadius;
	private final List<MapMarkerCircle> cursorMarkers;

	private final MapPolylineImpl arcPolyline;
	private final MapPolylineImpl asymptotePolyline;
	private final MapPolylineImpl tracePolyline;
	
	public HyperbolicProjection(StaticMeasurement sma, StaticMeasurement smb, int unit,
			boolean showArc, Color arcColor, boolean showAsymptote, Color asymptoteColor, 
			boolean showCursor, Color cursorColor, double cursorRadius, boolean showTrace,
			Color traceColor, double traceRadius) { 

		super(sma, smb);
		
		arcPolyline = new MapPolylineImpl();
		arcPolyline.setPoints(getCoordinateList());
		arcPolyline.setColor(arcColor);
		
		asymptotePolyline = new MapPolylineImpl();
		asymptotePolyline.setPoints(getAsymptoteCoordinateList());
		asymptotePolyline.setColor(asymptoteColor);
		
		tracePolyline = new MapPolylineImpl();
		final List<ICoordinate> coords = Collections.synchronizedList(new CopyOnWriteArrayList<ICoordinate>());
		coords.add(new Coordinate(getSMB().getPoint()));
		coords.add(new Coordinate(getFocus()));
		tracePolyline.setPoints(coords);
		tracePolyline.setColor(traceColor);
		
		traceMarkers = Collections.synchronizedList(new CopyOnWriteArrayList<MapMarkerCircle>());
		final Style traceStyle = new Style(traceColor, traceColor, new BasicStroke(1.0f), null);

		Coordinate coord;
		
		coord = new Coordinate(getSMB().getPoint().getX(), getSMB().getPoint().getY());
		
		traceMarkers.add(new MapMarkerCircle(null, null, coord, traceRadius, STYLE.FIXED, traceStyle));
		
		coord = new Coordinate(getSMA().getPoint().getX(), getSMA().getPoint().getY());
		traceMarkers.add(new MapMarkerCircle(null, null, coord, traceRadius, STYLE.FIXED, traceStyle));

		cursorMarkers = Collections.synchronizedList(new CopyOnWriteArrayList<MapMarkerCircle>());
		final Style cursorStyle = new Style(cursorColor, cursorColor, new BasicStroke(1.0f), null);
		
		coord = new Coordinate(getCenter().getX(), getCenter().getY());
		cursorMarkers.add(new MapMarkerCircle(null, null, coord, cursorRadius, STYLE.FIXED, cursorStyle));

		coord = new Coordinate(getVertex().getX(), getVertex().getY());
		cursorMarkers.add(new MapMarkerCircle(null, null, coord, cursorRadius, STYLE.FIXED, cursorStyle));

		coord = new Coordinate(getFocus().getX(), getFocus().getY());
		cursorMarkers.add(new MapMarkerCircle(null, null, coord, cursorRadius, STYLE.FIXED, cursorStyle));

		this.showTrace = showTrace;
		this.traceColor = traceColor;
		this.traceRadius = traceRadius;
		this.showCursor = showCursor;
		this.cursorColor = cursorColor;
		this.cursorRadius = cursorRadius;
	}

	public void showCursor(boolean showCursor) {
		this.showCursor = showCursor;
		updateCursorList();
	}
	
	public void setCursorColor(Color cursorColor) {
		this.cursorColor = cursorColor;
		updateCursorList();
	}
	
	public void setCursorRadius(double cursorRadius) {
		this.cursorRadius = cursorRadius;
		updateCursorList();
	}
	
	public boolean showCursors() {
		return showCursor;
	}

	public Color getCursorColor() {
		return cursorColor;
	}
	
	public double getCursorRadius() {
		return cursorRadius;
	}

	private void updateCursorList() {
		for (final MapMarkerCircle cursor : cursorMarkers) {
			cursor.setVisible(showCursor);
			cursor.setColor(cursorColor);
			cursor.setBackColor(cursorColor);
			cursor.setRadius(cursorRadius);
		}
	}
	
	public void showTrace(boolean showTrace) {
		this.showTrace = showTrace;
		updateTraceList();
	}
	
	public void setTraceColor(Color traceColor) {
		this.traceColor = traceColor;
		tracePolyline.setColor(traceColor);
		updateTraceList();
	}
	
	public void setTraceRadius(double traceRadius) {
		this.traceRadius = traceRadius;
		updateTraceList();
	}
	
	public void setTraceStroke(Stroke stroke) {
		tracePolyline.setStroke(stroke);
	}
	
	public boolean showTrace() {
		return showTrace;
	}
	
	public Color getTraceColor() {
		return traceColor;
	}
	
	public double getTraceRadius() {
		return traceRadius;
	}
	
	public Stroke getTraceStroke() {
		return tracePolyline.getStroke();
	}
	
	private void updateTraceList() {
		for (final MapMarkerCircle trace : traceMarkers) {
			trace.setVisible(showTrace);
			trace.setColor(traceColor);
			trace.setBackColor(traceColor);
			trace.setRadius(traceRadius);
		}
	}
	
	public void showAsymptote(boolean visible) {
		asymptotePolyline.setVisible(visible);
	}
	
	public void setAsymptoteColor(Color color) {
		asymptotePolyline.setColor(color);
	}
	
	public void setAsymptoteStroke(Stroke stroke) {
		asymptotePolyline.setStroke(stroke);
	}
	
	public boolean showAsymptote() {
		return asymptotePolyline.isVisible();
	}
	
	public Color getAsymptoteColor() {
		return asymptotePolyline.getColor();
	}
	
	public Stroke getAsymptoteSStroke() {
		return asymptotePolyline.getStroke();
	}

	public void showArc(boolean visible) {
		arcPolyline.setVisible(visible);
	}

	public void setArcColor(Color color) {
		arcPolyline.setColor(color);
	}
	
	public void setArcStroke(Stroke stroke) {
		arcPolyline.setStroke(stroke);
	}
	
	public boolean showArc() {
		return arcPolyline.isVisible();
	}
	
	public Color getArcColor() {
		return arcPolyline.getColor();
	}
	
	public Stroke getArcStroke() {
		return arcPolyline.getStroke();
	}

	public MapPolylineImpl getArcPolyline() {
		return arcPolyline;
	}
	
	public MapPolylineImpl getAsymptotePolyline() {
		return asymptotePolyline;
	}

	public MapPolylineImpl getTracePolyline() {
		return tracePolyline;
	}

	public List<MapMarkerCircle> getTraceMarkers() {
		return traceMarkers;
	}
	
	public List<MapMarkerCircle> getCursorMarkers() {
		return cursorMarkers;
	}
}
