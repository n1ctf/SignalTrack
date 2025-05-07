package map;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import java.beans.PropertyChangeSupport;

import java.lang.reflect.InvocationTargetException;

import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.prefs.Preferences;

import javax.swing.JPanel;

import components.MapDimension;
import coverage.TestTile;

import radiolocation.ConicSection;
import radiolocation.HyperbolicProjection;

public abstract class AbstractMap extends JPanel implements AutoCloseable {

	private static final long serialVersionUID = 1L;
	
	private static final Logger LOG = Logger.getLogger(AbstractMap.class.getName());
	
	private static String className;
	
	private static final String[] SIGNALTRACK_MAP_PROVIDER_CATALOG = { 
		"org.jxmapviewer.JXMapKit",
		"gov.nasa.worldwindx.signaltrack.WorldWindMap" 
	};

	public enum SignalTrackMapNames {
		OpenStreetMap, 
		VirtualEarthMap, 
		VirtualEarthSatellite, 
		VirtualEarthHybrid, 
		WorldWindMap
	}

	public enum MapEvent {
		PROPERTY_CHANGE, 
		MOUSE_OFF_GLOBE, 
		MAP_PAINTED, 
		ZOOM_IN_DISABLED, 
		ZOOM_OUT_DISABLED, 
		MOUSE_MOVED, 
		MOUSE_DRAGGED,
		VIEWING_ALTITUDE_CHANGE, 
		MAP_PROVIDER_CHANGE, 
		DISPLAY_SHAPES, 
		SHOW_STATUS_BAR, 
		MEASURE_TOOL_SOLUTION,
		SELECTION_RECTANGLE
	}

	private final transient Preferences userPref = Preferences.userRoot().node(this.getClass().getName());

	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	private boolean displayShapes;
	private boolean showStatusBar;
	
	protected AbstractMap(String uniqueIdentifier) {
		registerShutdownHook();
		loadPreferences(uniqueIdentifier);
	}

	protected AbstractMap(BorderLayout borderLayout, String uniqueIdentifier) {
		super(borderLayout);
		registerShutdownHook();
		loadPreferences(uniqueIdentifier);
	}
	
	public abstract void showCacheViewerPanel();

	public abstract String getUniqueIdentifier();

	public abstract JPanel getConfigPanel();

	public abstract void stopAllTestTileFlash();

	public abstract void addSignalMarker(Point2D point, double size, Color color);

	public abstract void addFlightPathMarker(Point2D point, double size, Color color);

	public abstract void addIcon(Point2D point, String iconPath, Dimension iconSize, long timeToLive, long timeToGoStale);

	public abstract void addLine(Point2D point, double angle, double distance, Color color);

	public abstract void addLine(Point2D pointA, Point2D pointB, Color color);

	public abstract void addQuad(Point2D point, Point2D size, Color color);

	public abstract void changeQuadColor(int index, Color color);

	public abstract void deleteAllSignalMarkers();

	public abstract void deleteAllFlightPathMarkers();

	public abstract void deleteAllIcons();

	public abstract void deleteAllLines();

	public abstract void deleteAllQuads();

	public abstract void deleteSignalMarker(int index);

	public abstract void deleteFlightPathMarker(int index);

	public abstract void deleteQuad(int index);

	public abstract void setCenterLonLat(Point2D point);

	public abstract void setGridColor(Color gridColor);

	public abstract void setTileSize(Point2D gridSize);

	public abstract void setScale(double scale);

	public abstract void showSignalMarkers(boolean showDots);

	public abstract void showFlightPathMarkers(boolean showFlightPathMarkers);

	public abstract void showGrid(boolean showGrid);

	public abstract void showIconLabels(boolean showIconLabels);

	public abstract void showIcons(boolean showIcons);

	public abstract void showLines(boolean showLines);

	public abstract void showQuads(boolean showQuads);

	public abstract void showRings(boolean showRings);

	public abstract void showTargetRing(boolean showTargetRing);

	public abstract void zoomIn();

	public abstract void zoomOut();

	public abstract void deleteAllArcs();

	public abstract void removeArc(int index);

	public abstract void showArcs(boolean show);

	public abstract void setGpsDotRadius(double radius);

	public abstract void setGpsDotColor(Color color);

	public abstract void setTargetRingPosition(Point2D point);

	public abstract void setTargetRingRadius(double radius);

	public abstract void setTargetRingColor(Color color);

	public abstract void setTargetRing(Point2D point, double radius);

	public abstract void setTargetRing(Point2D point, double radius, Color color);

	public abstract void showMapImage(boolean showMapImage);

	public abstract void showArcAsymptotes(boolean show);

	public abstract void showArcCursors(boolean show);

	public abstract void showArcTrace(boolean show);

	public abstract void setGpsSymbol(Point2D point, double radius, Color color, int angle);

	public abstract void showGpsSymbol(boolean show);

	public abstract void setArcAsymptoteColor(Color asymptoteColor);

	public abstract void setArcColors(Color[] arcColors);

	public abstract void setArcTraceColor(Color arcTrailColor);

	public abstract void setArcCursorColor(Color arcCursorColor);

	public abstract void deleteAllArcIntersectPoints();

	public abstract void addArcIntersectPoint(Point2D ip, double radius, Color color);

	public abstract void showArcIntersectPoints(boolean show);

	public abstract void setArcIntersectPointColor(Color arcIntersectPointColor);

	public abstract void setArcCursorRadius(double radius);

	public abstract void setTraceEqualsFlightColor(boolean trailEqualsFlightColor);

	public abstract void showBulkDownloadPanel();

	public abstract void showStatisticsPanel();

	public abstract void showLayerSelectorPanel();

	public abstract void addArcIntersectPoints(List<Point2D> arcIntersectList);

	public abstract void setArcIntersectPoints(List<Point2D> arcIntersectList);

	public abstract void addArcIntersectPoints(List<Point2D> iplist, double radius, Color color);

	public abstract void setArcIntersectPoints(List<Point2D> iplist, double radius, Color color);

	public abstract void setQuadVisible(int index, boolean isVisible);

	public abstract void deleteAllTestTiles();

	public abstract void showTestTiles(boolean showTestTiles);

	public abstract void setSignalMarkerRadius(double radius);

	public abstract void setFlightPathMarkerRadius(double radius);

	public abstract void setArcTraceRadius(double radius);

	public abstract void addFlightPathMarker(Point2D pathMarker, Color color);

	public abstract long getAltitude();

	public abstract Point2D getCenterLonLat();

	public abstract Point2D getGridSize();

	public abstract Point2D getMouseCoordinates();

	public abstract double getScale();

	public abstract BufferedImage getScreenShot();

	public abstract boolean moveIcon(int index, Point2D point, long timeToLive, long timeToGoStale);

	public abstract int getIconListSize();

	public abstract boolean isShowGrid();

	public abstract boolean isShowTargetRing();

	public abstract boolean isShowGPSDot();

	public abstract boolean isShowGPSArrow();

	public abstract boolean isShowMapImage();

	public abstract void setArcIntersectPointSize(double radius);

	public abstract long getMaxAltitude();

	public abstract boolean isShowTestTiles();

	public abstract boolean isShowSignalMarkers();

	public abstract boolean isShowFlightPathMarkers();

	public abstract boolean isShowLines();

	public abstract boolean isShowRings();

	public abstract boolean isShowArcIntersectPoints();

	public abstract void addSignalMarker(Point2D point, Color color);

	public abstract Rectangle2D getMapRectangle();

	public abstract MapDimension getMapDimension();

	public abstract void redraw();

	public abstract long getMinAltitude();

	public abstract void deleteAllRings();

	public abstract void addRing(Point2D coord, double radius, Color color);

	public abstract void setGridReference(Point2D gridReference);

	public abstract void setGridSize(Point2D gridFieldDimension);

	public abstract void clearCache();

	public abstract void deleteLine(int index);

	public abstract void deleteRing(int index);

	public abstract void deleteIcon(int index);

	public abstract void deleteArc(int index);

	public abstract void deleteTestTile(TestTile testTile);

	public abstract void addTestTile(TestTile testTile);

	public abstract void setTestTileColor(TestTile testTile, Color color);

	public abstract void setSelectionRectangleMode(boolean selectionMode);

	public abstract void setSelectionRectangleOrigin(Point2D topLeft);

	public abstract Point2D getSelectionRectangleOrigin();

	public abstract void setSelectionRectangleDestination(Point2D lowerRight);

	public abstract void setSelectionRectangle(Rectangle2D selectionRectangle);

	public abstract Point2D getMouseDragCoordinates();

	public abstract Rectangle2D getSelectionRectangle();

	public abstract void deleteTestGrid();

	public abstract void clearMapObjects();

	public abstract void setGrid(Point2D tileSize, Point2D gridReference, Point2D gridSize);

	public abstract void addArc(HyperbolicProjection hyperbola);

	public abstract void addArc(ConicSection cone);

	public abstract boolean isPointInBounds(Point2D p);

	public abstract void fitToDisplay(boolean gpsMarker, boolean signalMarkers, boolean flightPathMarkers,
			boolean testTiles, boolean rings, boolean targetRing, boolean gridLines, boolean quads, boolean icons,
			boolean lines, boolean arcs, boolean arcIntersects);

	public abstract Point2D getUpperLeftLonLat();

	public abstract Point2D getLowerRightLonLat();

	public abstract Point2D getGridReference();

	public abstract void setAltitude(int altitude);

	public abstract void setGpsSymbolColor(Color color);

	public abstract void setGpsSymbolAngle(int angle);

	public abstract double getCursorElevationMeters();

	public abstract double getDisplayAltitude();

	public abstract void initialize();

	public abstract void setTileFlash(int tileID, boolean flash);

	public abstract boolean isTileFlash(int tileID);

	public abstract boolean moveIcon(String name, Point2D point, long timeToLive, long timeToGoStale);

	public abstract void addIcon(Point2D point, BufferedImage image, String iconName, Dimension iconSize,
			long timeToLive, long timeToGoStale);

	public abstract void setGpsSymbolRadius(double gpsSymbolRadius);

	public abstract double getGpsSymbolRadius();

	public abstract int getGpsSymbolAngle();

	public abstract void setGpsSymbolLonLat(Point2D gpsSymbolLonLat);

	public abstract Point2D getGpsSymbolLonLat();

	public abstract Color getGpsSymbolColor();

	public abstract void setRulerMode(boolean rulerMode);

	public abstract void setRulerColor(Color rulerColor);

	public abstract void setSelectionRectangleVisible(boolean visible);

	public abstract void setTilesToDownload(Rectangle2D selectionRectangle);

	public abstract SignalTrackMapNames getSignalTrackMapName();

	public abstract boolean hasLayerSelectorPanel();

	public abstract boolean hasBulkDownloaderPanel();

	public abstract boolean hasStatisticsPanel();

	public abstract boolean hasCacheViewerPanel();

	public void savePreferences() {
		savePreferences(getUniqueIdentifier());
	}

	public String getClassName() {
		return className;
	}
	
	public boolean isDisplayShapes() {
		return displayShapes;
	}

	public void setDisplayShapes(boolean displayShapes) {
		this.displayShapes = displayShapes;
	}

	public boolean isShowStatusBar() {
		return showStatusBar;
	}

	public void setShowStatusBar(boolean showStatusBar) {
		this.showStatusBar = showStatusBar;
	}

	private void loadPreferences(String deviceID) {
		displayShapes = userPref.getBoolean(deviceID + "DisplayShapes", false);
		showStatusBar = userPref.getBoolean(deviceID + "ShowStatusBar", false);
	}

	private void savePreferences(String deviceID) {
		userPref.putBoolean(deviceID + "DisplayShapes", displayShapes);
		userPref.putBoolean(deviceID + "ShowStatusBar", showStatusBar);
	}

	public PropertyChangeSupport getPropertyChangeSupport() {
		return pcs;
	}

	public static String[] getSignalTrackMapProviderCatalog() {
		return SIGNALTRACK_MAP_PROVIDER_CATALOG.clone();
	}

	private void registerShutdownHook() {
		final Thread shutdownThread = new Thread() {
			@Override
			public void run() {
				savePreferences(getUniqueIdentifier());
				pcs.firePropertyChange(MapEvent.PROPERTY_CHANGE.name(), null, true);
			}
		};
		Runtime.getRuntime().addShutdownHook(shutdownThread);
	}

	public static AbstractMap getMapInstance(String className, Point2D point, Long alt) {
		AbstractMap.className = className;
		final Class<?> classTemp;
		AbstractMap mapInstance = null;
		try {
			classTemp = Class.forName(className);
			final Class<?>[] cArg = new Class<?>[2];
			cArg[0] = Point2D.class;
			cArg[1] = Long.class;
			mapInstance = (AbstractMap) classTemp.getDeclaredConstructor(cArg).newInstance(point, alt);
		} catch (InstantiationException e) {
			LOG.log(Level.WARNING, "InstantiationException", e);
		} catch (IllegalAccessException e) {
			LOG.log(Level.WARNING, "IllegalAccessException", e);
		} catch (IllegalArgumentException e) {
			LOG.log(Level.WARNING, "IllegalArgumentException", e);
		} catch (InvocationTargetException e) {
			LOG.log(Level.WARNING, "InvocationTargetException", e);
		} catch (NoSuchMethodException e) {
			LOG.log(Level.WARNING, "NoSuchMethodException", e);
		} catch (SecurityException e) {
			LOG.log(Level.WARNING, "SecurityException", e);
		} catch (ClassNotFoundException e) {
			LOG.log(Level.WARNING, "ClassNotFoundException", e);
		}
		return mapInstance;
	}
	
	public static AbstractMap getMapInstance(String className, Point2D point, Long alt, Object obj) {
		AbstractMap.className = className;
		final Class<?> classTemp;
		AbstractMap mapInstance = null;
		try {
			classTemp = Class.forName(className);
			final Class<?>[] cArg = new Class<?>[3];
			cArg[0] = Point2D.class;
			cArg[1] = Long.class;
			cArg[2] = Object.class;
			mapInstance = (AbstractMap) classTemp.getDeclaredConstructor(cArg).newInstance(point, alt, obj);
		} catch (InstantiationException e) {
			LOG.log(Level.WARNING, "InstantiationException", e);
		} catch (IllegalAccessException e) {
			LOG.log(Level.WARNING, "IllegalAccessException", e);
		} catch (IllegalArgumentException e) {
			LOG.log(Level.WARNING, "IllegalArgumentException", e);
		} catch (InvocationTargetException e) {
			LOG.log(Level.WARNING, "InvocationTargetException", e);
		} catch (NoSuchMethodException e) {
			LOG.log(Level.WARNING, "NoSuchMethodException", e);
		} catch (SecurityException e) {
			LOG.log(Level.WARNING, "SecurityException", e);
		} catch (ClassNotFoundException e) {
			LOG.log(Level.WARNING, "ClassNotFoundException", e);
		}
		return mapInstance;
	}
}
