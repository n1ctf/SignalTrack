package gov.nasa.worldwindx.signaltrack;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingWorker;
import javax.swing.ToolTipManager;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.awt.AWTGLReadBufferUtil;

import components.MapDimension;
import components.SurfaceLine;
import coverage.TestTile;
import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.cache.BasicDataFileStore;
import gov.nasa.worldwind.cache.FileStore;
import gov.nasa.worldwind.event.PositionListener;
import gov.nasa.worldwind.event.RenderingEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.exception.WWAbsentRequirementException;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Intersection;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Earth;
import gov.nasa.worldwind.globes.EarthFlat;
import gov.nasa.worldwind.globes.FlatGlobe;
import gov.nasa.worldwind.globes.GeographicProjection;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.globes.Globe2D;
import gov.nasa.worldwind.globes.projections.ProjectionEquirectangular;
import gov.nasa.worldwind.globes.projections.ProjectionMercator;
import gov.nasa.worldwind.globes.projections.ProjectionModifiedSinusoidal;
import gov.nasa.worldwind.globes.projections.ProjectionPolarEquidistant;
import gov.nasa.worldwind.globes.projections.ProjectionSinusoidal;
import gov.nasa.worldwind.globes.projections.ProjectionTransverseMercator;
import gov.nasa.worldwind.globes.projections.ProjectionUPS;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.layers.CompassLayer;
import gov.nasa.worldwind.layers.IconLayer;
import gov.nasa.worldwind.layers.LatLonGraticuleLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.MarkerLayer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.layers.TerrainProfileLayer;
import gov.nasa.worldwind.layers.TiledImageLayer;
import gov.nasa.worldwind.layers.ViewControlsLayer;
import gov.nasa.worldwind.layers.ViewControlsSelectListener;
import gov.nasa.worldwind.layers.Earth.MGRSGraticuleLayer;
import gov.nasa.worldwind.layers.Earth.USGSTopoHighRes;
import gov.nasa.worldwind.layers.placename.PlaceNameLayer;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.poi.PointOfInterest;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.SurfaceCircle;
import gov.nasa.worldwind.render.SurfacePolyline;
import gov.nasa.worldwind.render.SurfaceQuad;
import gov.nasa.worldwind.render.UserFacingIcon;
import gov.nasa.worldwind.render.WWIcon;
import gov.nasa.worldwind.render.markers.BasicMarker;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;
import gov.nasa.worldwind.render.markers.BasicMarkerShape;
import gov.nasa.worldwind.render.markers.Marker;
import gov.nasa.worldwind.render.markers.MarkerAttributes;
import gov.nasa.worldwind.terrain.ZeroElevationModel;
import gov.nasa.worldwind.util.StatusBar;
import gov.nasa.worldwind.util.measure.MeasureTool;
import gov.nasa.worldwind.view.orbit.BasicOrbitView;
import gov.nasa.worldwind.view.orbit.OrbitView;
import gov.nasa.worldwind.view.orbit.OrbitViewInputHandler;
import gov.nasa.worldwindx.examples.util.FileStoreDataSet;
import gov.nasa.worldwindx.signaltrack.bulkdownload.BulkDownload;
import gov.nasa.worldwindx.signaltrack.cachecleaner.DataCacheViewer;
import gov.nasa.worldwindx.signaltrack.layermanager.LayerManagerDialog;
import gov.nasa.worldwindx.signaltrack.measuretool.MeasureToolDialog;
import map.AbstractMap;
import radiolocation.ConicSection;
import radiolocation.HyperbolicProjection;
import utility.Vincenty;

/**
 *
 * @author modified by John
 */
public class WorldWindMap extends AbstractMap {
	private static final long serialVersionUID = 1423L; // This provides a unique catalog number for use by the configuration selector
	
	private static final double ZOOM_STEP = 0.8;
	private static final double DEFAULT_ARC_INTERSECT_POINT_RADIUS = 3D;
	private static final double DEFAULT_ARC_TRACE_RADIUS = 3D;
	private static final double DEFAULT_ARC_CURSOR_RADIUS = 3D;
	private static final double DEFAULT_SIGNAL_MARKER_RADIUS = 3D;
	private static final Color DEFAULT_ARC_INTERSECT_POINT_COLOR = Color.RED;
	private static final Color DEFAULT_ARC_TRACE_COLOR = Color.ORANGE;
	private static final Color DEFAULT_ARC_CURSOR_COLOR = Color.GREEN;
	private static final Color DEFAULT_ARC_ASYMPTOTE_COLOR = Color.CYAN;
	private static final boolean DEFAULT_TRACE_COLOR_MODE = true;
	private static final Point2D NULL_COORDS = new Point2D.Double(-181, -181);
	private static final int TEST_GRID_MAX_ALTITUDE = 60000;
	private static final int MIN_ALTITUDE = 267;
	private static final int MAX_ALTITUDE = 18244253;
	
	private static final Logger LOG = Logger.getLogger(WorldWindMap.class.getName());
	
	private final transient Object renderingHold = new Object();
	private final transient Point2D startupLonLat;
	private final long altitude;

	private transient BasicOrbitView view;

	private boolean showPolygons;
	private boolean showQuads;
	private boolean showSignalMarkers;
	private boolean showLines;
	private boolean showRings;
	private boolean showIcons;
	private boolean showGrid;
	private boolean showArcs;
	private boolean showFlightPathMarkers;
	private boolean showArcTraces;
	private boolean showArcIntersectPoints;
	private boolean showArcAsymptotes;
	private boolean showArcCursors;
	private boolean showIconLabels;
	private boolean showGpsSymbol;
	private boolean showTargetRing;
	private boolean showSelectionRectangle;

	private boolean traceEqualsFlightColor = DEFAULT_TRACE_COLOR_MODE;

	private double arcCursorRadius = DEFAULT_ARC_CURSOR_RADIUS;
	private double arcTraceRadius = DEFAULT_ARC_TRACE_RADIUS;
	private static final double arcIntersectPointRadius = DEFAULT_ARC_INTERSECT_POINT_RADIUS;

	private Color arcTraceColor = DEFAULT_ARC_TRACE_COLOR;
	private Color arcCursorColor = DEFAULT_ARC_CURSOR_COLOR;
	private Color arcAsymptoteColor = DEFAULT_ARC_ASYMPTOTE_COLOR;
	private Color arcIntersectPointColor = DEFAULT_ARC_INTERSECT_POINT_COLOR;

	private transient Marker gpsArrow;
	private transient Marker gpsDot;

	private transient SurfaceCircle targetRingShape;

	private transient MarkerAttributes gpsDotBma;
	private transient MarkerAttributes gpsArrowBma;
	private transient MarkerAttributes arcCursorBma;
	private transient ShapeAttributes arcAsymptoteBsa;
	private transient MarkerAttributes arcIntersectBma;

	private transient List<UserFacingIcon> iconList;
	private transient List<SurfaceQuad> quadList;
	private transient List<SurfacePolyline> lineList;
	private transient List<SurfaceCircle> ringList;
	private transient List<ConicSection> arcList;
	private transient List<GeoTile> geoTileList;
	private transient List<Marker> intersectMarkers;
	private transient List<Marker> traceDotMarkers;
	private transient List<Marker> cursorMarkers;
	private transient List<Marker> signalMarkers;
	private transient List<Marker> flightPathMarkers;

	private transient ViewControlsLayer viewControlsLayer;
	private transient AbstractLayer latLonGraticuleLayer;
	private transient TiledImageLayer usgsTopoHighResLayer;
	private transient AbstractLayer mgrsGraticuleLayer;
	private transient TerrainProfileLayer terrainProfileLayer;

	private transient IconLayer iconLayer;

	private transient MarkerLayer gpsArrowLayer;
	private transient MarkerLayer gpsDotLayer;
	private transient MarkerLayer intersectLayer;
	private transient MarkerLayer cursorLayer;
	private transient MarkerLayer traceDotLayer;
	private transient MarkerLayer signalMarkerLayer;
	private transient MarkerLayer flightPathMarkerLayer;

	private transient TestGridLayer testGridLayer;

	private transient RenderableLayer selectionRectangleLayer;
	private transient RenderableLayer targetRingLayer;
	private transient RenderableLayer lineLayer;
	private transient RenderableLayer ringLayer;
	private transient RenderableLayer quadLayer;
	private transient RenderableLayer arcLayer;
	private transient RenderableLayer asymptoteLayer;
	private transient RenderableLayer traceLineLayer;
	private transient RenderableLayer testTileLayer;

	private MeasureToolDialog mtd;
	private Color rulerColor;

	private transient ScheduledFuture<?> gridRedrawHandle;

	private FlatWorldPanel fwp;

	private transient Point2D selectionRectangleOrigin;
	private transient Point2D gridSize;
	private Point mousePosition;
	private transient Point2D mouseCoordinates = NULL_COORDS;
	private transient Point2D mouseDragCoordinates = NULL_COORDS;

	private transient ViewController viewController;

	private Color[] arcColors;
	private transient WorldWindow wwd;
	private boolean wwdRendered;
	private StatusBar statusBar;
	private transient Preferences userPref;
	private boolean isZooming;
	private boolean animationRedrawTimerActive;

	private transient MouseMotionListener mml;
	private transient MouseWheelListener mwl;
	private transient MouseListener ml;
	private transient PositionListener positionListener;
	private transient SelectListener selectListener;

	private boolean isClosing;

	static {
		// The following is required to use Swing menus with the heavyweight canvas used
		// by WorldWind.
		ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
	}

	public WorldWindMap(Point2D startupLonLat, Long altitude) {
		super(new BorderLayout(), String.valueOf(serialVersionUID));
		this.startupLonLat = startupLonLat;
		this.altitude = altitude;
		initializeVariables();
	}

	private WorldWindow getWwd() {
		return wwd;
	}

	private void initializeVariables() {
		userPref = Preferences.userRoot().node("jdrivetrack/prefs/WorldWind");
		statusBar = new StatusBar();
		view = new BasicOrbitView();
		viewControlsLayer = new ViewControlsLayer();
		wwdRendered = false;
		iconLayer = new IconLayer();
		testGridLayer = new TestGridLayer();
		targetRingLayer = new RenderableLayer();
		lineLayer = new RenderableLayer();
		ringLayer = new RenderableLayer();
		quadLayer = new RenderableLayer();
		arcLayer = new RenderableLayer();
		asymptoteLayer = new RenderableLayer();
		traceLineLayer = new RenderableLayer();
		testTileLayer = new RenderableLayer();
		selectionRectangleLayer = new RenderableLayer();
		gpsDotBma = new BasicMarkerAttributes();
		gpsArrowBma = new BasicMarkerAttributes();
		arcCursorBma = new BasicMarkerAttributes();
		arcAsymptoteBsa = new BasicShapeAttributes();
		arcIntersectBma = new BasicMarkerAttributes();
		iconList = Collections.synchronizedList(new CopyOnWriteArrayList<>());
		quadList = Collections.synchronizedList(new CopyOnWriteArrayList<>());
		lineList = Collections.synchronizedList(new CopyOnWriteArrayList<>());
		ringList = Collections.synchronizedList(new CopyOnWriteArrayList<>());
		arcList = Collections.synchronizedList(new CopyOnWriteArrayList<>());
		geoTileList = Collections.synchronizedList(new CopyOnWriteArrayList<>());
		intersectMarkers = Collections.synchronizedList(new CopyOnWriteArrayList<>());
		traceDotMarkers = Collections.synchronizedList(new CopyOnWriteArrayList<>());
		cursorMarkers = Collections.synchronizedList(new CopyOnWriteArrayList<>());
		signalMarkers = Collections.synchronizedList(new CopyOnWriteArrayList<>());
		flightPathMarkers = Collections.synchronizedList(new CopyOnWriteArrayList<>());
		flightPathMarkerLayer = new MarkerLayer();
		gpsArrowLayer = new MarkerLayer();
		gpsDotLayer = new MarkerLayer();
		intersectLayer = new MarkerLayer();
		cursorLayer = new MarkerLayer();
		traceDotLayer = new MarkerLayer();
		signalMarkerLayer = new MarkerLayer();
		targetRingShape = new SurfaceCircle();
		usgsTopoHighResLayer = new USGSTopoHighRes();
		latLonGraticuleLayer = new LatLonGraticuleLayer();
		mgrsGraticuleLayer = new MGRSGraticuleLayer();
		terrainProfileLayer = new TerrainProfileLayer();

		mml = new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent event) {
				if (view.isAnimating() && !animationRedrawTimerActive) {
					gridRedrawAnimating(50, 50);
				}
				try {
					setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					processMouseMotionEvent(event);
				} catch (final NullPointerException ex) {
					LOG.log(Level.WARNING, ex.getMessage(), ex);
				}
			}

			@Override
			public void mouseMoved(MouseEvent event) {
				mousePosition = event.getPoint();
				getPropertyChangeSupport().firePropertyChange(MapEvent.MOUSE_MOVED.name(), null, mouseCoordinates);
				processMouseEvent(event);
			}
		};

		mwl = event -> {
			final int rotation = event.getWheelRotation();
			if (rotation < 0) {
				zoomIn();
			} else {
				zoomOut();
			}
			processMouseWheelEvent(event);
		};

		ml = new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent event) {
				if (event.getButton() == MouseEvent.BUTTON1 && event.getClickCount() >= 2) {
					zoomOut();
				}
				if (event.getButton() == MouseEvent.BUTTON2) {
					view.goTo(getWwd().getCurrentPosition(), altitude);
				}
				if (event.getButton() == MouseEvent.BUTTON3 && event.getClickCount() >= 2) {
					zoomIn();
				}
				processMouseEvent(event);
			}

			@Override
			public void mouseEntered(MouseEvent event) {
				setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
				processMouseEvent(event);
				getPropertyChangeSupport().firePropertyChange(MapEvent.MOUSE_OFF_GLOBE.name(), null, false);
			}

			@Override
			public void mouseExited(MouseEvent event) {
				processMouseEvent(event);
				getPropertyChangeSupport().firePropertyChange(MapEvent.MOUSE_OFF_GLOBE.name(), null, true);
			}

			@Override
			public void mousePressed(MouseEvent event) {
				processMouseEvent(event);
			}

			@Override
			public void mouseReleased(MouseEvent event) {
				setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
				processMouseEvent(event);
			}
		};

		positionListener = event -> {
			if (event.getPosition() != null) {
				mouseCoordinates = new Point2D.Double(event.getPosition().getLongitude().getDegrees(),
						event.getPosition().getLatitude().getDegrees());
				getPropertyChangeSupport().firePropertyChange(MapEvent.MOUSE_MOVED.name(), null, mouseCoordinates);
				mouseDragCoordinates = new Point2D.Double(event.getPosition().getLongitude().getDegrees(),
						event.getPosition().getLatitude().getDegrees());
			}
		};

		selectListener = event -> {
			if (event.getTopObject() != null && event.getTopPickedObject().getParentLayer() instanceof MarkerLayer) {
				final PickedObject po = event.getTopPickedObject();
				final String a = po.getValue(AVKey.PICKED_OBJECT_ID).toString();
				final String b = po.getPosition().getLongitude().toFormattedDMSString();
				final String c = po.getPosition().getLatitude().toFormattedDMSString();
				final double d = (double) po.getValue(AVKey.PICKED_OBJECT_SIZE);
				final String str = String.format(getLocale(), "Track position %s, %s, %s, size = %f %n", a, b, c, d);
				LOG.log(Level.INFO, str);
			}
		};
	}

	private void createWorldWindMap() {
		final WorldWindowGLCanvas wwc = new WorldWindowGLCanvas();

		final Model m = (Model) WorldWind.createConfigurationComponent(AVKey.MODEL_CLASS_NAME);

		wwc.setModel(m);

		final boolean flat = userPref.getBoolean("flat", false);

		if (!flat) {
			wwc.getModel().setGlobe(new Earth());
		} else {
			final String projectionName = userPref.get("projectionName", "Mercator");
			final GeographicProjection projection = getProjection(projectionName);
			final FlatGlobe flatGlobe = new EarthFlat();
			flatGlobe.setElevationModel(new ZeroElevationModel());
			flatGlobe.setProjection(projection);
			wwc.getModel().setGlobe(flatGlobe);
		}

		final var pos = new Position(Angle.fromDegreesLatitude(startupLonLat.getY()),
				Angle.fromDegreesLongitude(startupLonLat.getX()), 0);

		view.setCenterPosition(pos);
		view.setZoom(altitude);

		wwc.setView(view);

		statusBar.setEventSource(getWwd());
		statusBar.setBorder(BorderFactory.createEtchedBorder());
		statusBar.setVisible(false);

		fwp = new FlatWorldPanel(wwc);

		add(wwc, BorderLayout.CENTER);

		this.wwd = wwc;
	}

	@Override
	public JPanel getConfigPanel() {
		final JPanel panel = new JPanel();

		final GroupLayout layout = new GroupLayout(panel);

		panel.setLayout(layout);

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addContainerGap()
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false).addComponent(fwp,
								GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
						.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
				layout.createSequentialGroup().addContainerGap().addComponent(fwp, 80, 80, 80).addContainerGap()));

		return panel;
	}

	private void configurePanel() {
		setOpaque(true);
		setDoubleBuffered(true);
		setBackground(Color.BLACK);
		setFocusable(true);
		setFocusTraversalKeysEnabled(true);
		setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

		System.setProperty("java.net.useSystemProxies", "true");

		if (Configuration.isMacOS()) {
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", "World Wind Application");
			System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
			System.setProperty("apple.awt.brushMetalLook", "true");
		} else if (Configuration.isWindowsOS()) {
			System.setProperty("sun.awt.noerasebackground", "true"); // prevents flashing during window resizing
		} else if (Configuration.isLinuxOS()) {
			// What about Linux, the best OS ever? This must be fixed right away, gentlemen!
		}
	}

	@Override
	public synchronized void removeMouseMotionListener(MouseMotionListener mml) {
		getWwd().getInputHandler().removeMouseMotionListener(mml);
	}

	@Override
	public synchronized void addMouseMotionListener(MouseMotionListener mml) {
		getWwd().getInputHandler().addMouseMotionListener(mml);
	}

	private void configureListeners() {
		getWwd().getInputHandler().addSelectListener(new ViewControlsSelectListener(getWwd(), viewControlsLayer));

		getWwd().addPositionListener(positionListener);

		getWwd().getInputHandler().addSelectListener(selectListener);

		getWwd().getInputHandler().addMouseMotionListener(mml);

		getWwd().getInputHandler().addMouseWheelListener(mwl);

		getWwd().getInputHandler().addMouseListener(ml);

		getWwd().getInputHandler().setForceRedrawOnMousePressed(true);

		getWwd().addRenderingListener(event -> {
			if (event.getStage().equals(RenderingEvent.AFTER_BUFFER_SWAP)) {
				worldWindRendered();
			}
		});

		getWwd().addRenderingExceptionListener(t -> {
			if (t instanceof WWAbsentRequirementException) {
				var message = "This computer does not meet minimum graphics requirements.\n";
				message += "Please install an up-to-date graphics driver and try again.\n";
				message += "Reason: " + t.getMessage() + "\n";
				JOptionPane.showMessageDialog(getParent(), message, "Unable to Run NASA WorldWind",
						JOptionPane.ERROR_MESSAGE);
			}
		});
	}

	private void insertMapLayers() {
		insertBeforeCompass(getWwd(), viewControlsLayer);
		insertBeforeCompass(getWwd(), mgrsGraticuleLayer);
		insertBeforeCompass(getWwd(), latLonGraticuleLayer);
		insertBeforeCompass(getWwd(), usgsTopoHighResLayer);
		insertBeforePlacenames(getWwd(), gpsArrowLayer);
		insertBeforePlacenames(getWwd(), intersectLayer);
		insertBeforePlacenames(getWwd(), asymptoteLayer);
		insertBeforePlacenames(getWwd(), targetRingLayer);
		insertBeforePlacenames(getWwd(), iconLayer);
		insertBeforePlacenames(getWwd(), lineLayer);
		insertBeforePlacenames(getWwd(), ringLayer);
		insertBeforePlacenames(getWwd(), quadLayer);
		insertBeforePlacenames(getWwd(), testTileLayer);
		insertBeforePlacenames(getWwd(), arcLayer);
		insertBeforePlacenames(getWwd(), cursorLayer);
		insertBeforePlacenames(getWwd(), traceLineLayer);
		insertBeforePlacenames(getWwd(), traceDotLayer);
		insertBeforePlacenames(getWwd(), gpsDotLayer);
		insertBeforePlacenames(getWwd(), signalMarkerLayer);
		insertBeforePlacenames(getWwd(), flightPathMarkerLayer);
		insertBeforePlacenames(getWwd(), testGridLayer);
		insertBeforePlacenames(getWwd(), terrainProfileLayer);
		insertBeforePlacenames(getWwd(), selectionRectangleLayer);

		restoreLayerSelections(getWwd());

		redraw();

		getWwd().getModel().getLayers().stream().filter(SelectListener.class::isInstance)
				.forEachOrdered(layer -> getWwd().addSelectListener((SelectListener) layer));
	}

	private void configureComponents() {
		arcIntersectBma.setShapeType(BasicMarkerShape.CYLINDER);
		arcIntersectBma.setMaterial(new Material(DEFAULT_ARC_INTERSECT_POINT_COLOR));
		arcIntersectBma.setMarkerPixels(DEFAULT_ARC_INTERSECT_POINT_RADIUS);
		arcIntersectBma.setOpacity(0.7D);
		intersectLayer.setEnabled(false);
		intersectLayer.setElevation(0);
		intersectLayer.setEnablePickSizeReturn(true);
		intersectLayer.setOverrideMarkerElevation(true);
		intersectLayer.setKeepSeparated(false);
		intersectLayer.setName("Arc Intersect Layer");
		intersectLayer.setMarkers(intersectMarkers);

		final var arcAsymBsa = new BasicShapeAttributes();
		arcAsymBsa.setInteriorOpacity(0.0);
		arcAsymBsa.setOutlineOpacity(1.0);
		arcAsymBsa.setOutlineWidth(1);
		arcAsymBsa.setEnableAntialiasing(false);
		asymptoteLayer.setEnabled(false);
		asymptoteLayer.setName("Arc Asymptote Layer");

		final var targetRingBsa = new BasicShapeAttributes();
		targetRingBsa.setInteriorMaterial(Material.RED);
		targetRingBsa.setOutlineMaterial(Material.RED);
		targetRingBsa.setInteriorOpacity(0.2);
		targetRingBsa.setOutlineOpacity(0.8);
		targetRingBsa.setOutlineWidth(2);
		targetRingBsa.setEnableAntialiasing(true);

		targetRingShape.setAttributes(targetRingBsa);
		targetRingLayer.addRenderable(targetRingShape);
		targetRingLayer.setEnabled(false);
		targetRingLayer.setName("Target Ring Layer");

		iconLayer.setEnabled(false);
		iconLayer.setName("AbstractAprsProcessor Icon Layer");

		lineLayer.setEnabled(false);
		lineLayer.setName("Line Layer");

		ringLayer.setEnabled(false);
		ringLayer.setName("Ring Layer");

		quadLayer.setEnabled(false);
		quadLayer.setName("Quad Layer");

		selectionRectangleLayer.setEnabled(false);
		selectionRectangleLayer.setName("Selection Rectangle Layer");

		testTileLayer.setEnabled(false);
		testTileLayer.setName("Test Tile Layer");

		testGridLayer.setEnabled(false);
		testGridLayer.setMaxActiveAltitude(TEST_GRID_MAX_ALTITUDE);
		testGridLayer.setName("Drive Test Grid");

		arcLayer.setEnabled(false);
		arcLayer.setName("Arc Layer");

		arcCursorBma.setShapeType(BasicMarkerShape.CYLINDER);
		arcCursorBma.setMaterial(new Material(DEFAULT_ARC_CURSOR_COLOR));
		arcCursorBma.setMarkerPixels(DEFAULT_ARC_CURSOR_RADIUS);
		arcCursorBma.setOpacity(0.7D);

		cursorLayer.setEnabled(false);
		cursorLayer.setOverrideMarkerElevation(true);
		cursorLayer.setKeepSeparated(false);
		cursorLayer.setElevation(1000D);
		cursorLayer.setName("Arc Cursor Layer");
		cursorLayer.setMarkers(cursorMarkers);

		traceLineLayer.setEnabled(false);
		traceLineLayer.setName("Arc Trace Line Layer");

		traceDotLayer.setEnabled(false);
		traceDotLayer.setName("Arc Trace Dot Layer");

		gpsArrow = new BasicMarker(Position.fromDegrees(0, 0), gpsArrowBma);
		gpsArrowBma.setShapeType(BasicMarkerShape.HEADING_ARROW);
		gpsArrowBma.setOpacity(0.3D);
		gpsArrowLayer.setOverrideMarkerElevation(true);
		gpsArrowLayer.setElevation(1000D);
		gpsArrowLayer.setKeepSeparated(true);
		gpsArrowLayer.setEnabled(false);
		gpsArrowLayer.setName("GPS Arrow");
		final List<Marker> gpsArrowMarkers = new ArrayList<>(1);
		gpsArrowMarkers.add(gpsArrow);
		gpsArrowLayer.setMarkers(gpsArrowMarkers);

		gpsDot = new BasicMarker(Position.fromDegrees(0, 0), gpsDotBma);
		gpsDotBma.setShapeType(BasicMarkerShape.CYLINDER);
		gpsDotBma.setOpacity(0.3D);
		gpsDotLayer.setOverrideMarkerElevation(true);
		gpsDotLayer.setKeepSeparated(false);
		gpsDotLayer.setElevation(1000D);
		gpsDotLayer.setEnabled(false);
		gpsDotLayer.setName("GPS Dot");
		final List<Marker> gpsDotMarkers = new ArrayList<>(1);
		gpsDotMarkers.add(gpsDot);
		gpsDotLayer.setMarkers(gpsDotMarkers);

		signalMarkerLayer.setEnabled(false);
		signalMarkerLayer.setOverrideMarkerElevation(true);
		signalMarkerLayer.setKeepSeparated(false);
		signalMarkerLayer.setElevation(1000D);
		signalMarkerLayer.setName("Signal Marker Layer");
		signalMarkerLayer.setMarkers(signalMarkers);

		flightPathMarkerLayer.setEnabled(false);
		flightPathMarkerLayer.setOverrideMarkerElevation(true);
		flightPathMarkerLayer.setKeepSeparated(false);
		flightPathMarkerLayer.setElevation(1000D);
		flightPathMarkerLayer.setName("Flight Path Marker Layer");
		flightPathMarkerLayer.setMarkers(flightPathMarkers);

		viewController = new ViewController(getWwd());
	}

	@Override
	public void showStatisticsPanel() {
		new StatisticsPanelDialog(getWwd());
	}

	@Override
	public void showCacheViewerPanel() {
		final FileStore store = new BasicDataFileStore();
		final File cacheRoot = store.getWriteLocation();
		new DataCacheViewer(cacheRoot);
	}

	@Override
	public void showLayerSelectorPanel() {
		if (!isFlatGlobe()) {
			new LayerManagerDialog(getWwd());
		}
	}

	@Override
	public void showBulkDownloadPanel() {
		ApplicationTemplate.start("World Wind Bulk Download", BulkDownload.AppFrame.class, getCenterLonLat(), altitude);
	}

	private void worldWindRendered() {
		if (!wwdRendered) {
			getPropertyChangeSupport().firePropertyChange(MapEvent.MAP_PAINTED.name(), null, true);
			if (isZooming) {
				isZooming = false;
				testGridLayer.repaint();
			}
		}
		final int feet = (int) (Vincenty.FEET_PER_METER * getDisplayAltitude());
		getPropertyChangeSupport().firePropertyChange(MapEvent.VIEWING_ALTITUDE_CHANGE.name(), null, feet);
		wwdRendered = true;
		synchronized (renderingHold) {
			renderingHold.notifyAll();
		}
	}

	private void saveLayerSelections(WorldWindow wwd) {
		wwd.getModel().getLayers().forEach(layer -> userPref.putBoolean(layer.getName(), layer.isEnabled()));
	}

	private void restoreLayerSelections(WorldWindow wwd) {
		wwd.getModel().getLayers().forEach(layer -> layer.setEnabled(userPref.getBoolean(layer.getName(), false)));
	}

	private void moveToLocation(Position position) {
		final OrbitView v = (OrbitView) this.getWwd().getView();
		final Globe globe = this.getWwd().getModel().getGlobe();

		if (globe != null && v != null) {
			((OrbitViewInputHandler) v.getViewInputHandler()).addPanToAnimator(position, Angle.ZERO, Angle.ZERO,
					position.elevation, true);
		}
	}

	protected void moveToLocation(PointOfInterest location) {
		this.moveToLocation(location.getLatlon());
	}

	private void moveToLocation(LatLon location) {
		this.moveToLocation(new Position(location, this.getAltitude()));
	}

	@Override
	public Point2D getCenterLonLat() {
		try {
			final var pos = view.computePositionFromScreenPoint(getSize().width / 2.0, getSize().height / 2.0);
			return new Point.Double(pos.getLongitude().getDegrees(), pos.getLatitude().getDegrees());
		} catch (final NullPointerException ex) {
			LOG.log(Level.SEVERE, ex.getMessage(), ex);
			return null;
		}
	}

	@Override
	public Rectangle2D.Double getMapRectangle() {
		try {
			return new Rectangle2D.Double(getUpperLeftLonLat().getX(), getUpperLeftLonLat().getY(), getWidth(),
					getHeight());
		} catch (final NullPointerException ex) {
			LOG.log(Level.SEVERE, ex.getMessage(), ex);
			return null;
		}
	}

	@Override
	public MapDimension getMapDimension() {
		return new MapDimension(getMapRectangle());
	}

	@Override
	public void setCenterLonLat(Point2D lonLat) {
		try {
			final var pos = new Position(Angle.fromDegreesLatitude(lonLat.getY()), Angle.fromDegreesLongitude(lonLat.getX()), 0.0);
			view.setCenterPosition(pos);
		} catch (final NullPointerException ex) {
			LOG.log(Level.WARNING, ex.getMessage(), ex);
		}
	}

	@Override
	public void setDisplayShapes(boolean displayShapes) {
		super.setDisplayShapes(displayShapes);
		if (arcLayer != null) {
			arcLayer.setEnabled(displayShapes && showArcs);
		}
		if (asymptoteLayer != null) {
			asymptoteLayer.setEnabled(displayShapes && showArcAsymptotes);
		}
		if (cursorLayer != null) {
			cursorLayer.setEnabled(displayShapes && showArcCursors);
		}
		if (traceLineLayer != null) {
			traceLineLayer.setEnabled(displayShapes && showArcTraces);
		}
		if (traceDotLayer != null) {
			traceDotLayer.setEnabled(displayShapes && showArcTraces);
		}
		if (quadLayer != null) {
			quadLayer.setEnabled(displayShapes && showQuads);
		}
		if (selectionRectangleLayer != null) {
			selectionRectangleLayer.setEnabled(displayShapes && showSelectionRectangle);
		}
		if (testTileLayer != null) {
			testTileLayer.setEnabled(displayShapes && showPolygons);
		}
		if (signalMarkerLayer != null) {
			signalMarkerLayer.setEnabled(displayShapes && showSignalMarkers);
		}
		if (flightPathMarkerLayer != null) {
			flightPathMarkerLayer.setEnabled(displayShapes && showFlightPathMarkers);
		}
		if (intersectLayer != null) {
			intersectLayer.setEnabled(displayShapes && showArcIntersectPoints);
		}
		if (ringLayer != null) {
			ringLayer.setEnabled(displayShapes && showRings);
		}
		if (iconLayer != null) {
			iconLayer.setEnabled(displayShapes && showIcons);
		}
		if (lineLayer != null) {
			lineLayer.setEnabled(displayShapes && showLines);
		}
		if (targetRingLayer != null) {
			targetRingLayer.setEnabled(displayShapes && showTargetRing);
		}
		if (testGridLayer != null) {
			testGridLayer.setEnabled(displayShapes && showGrid);
		}
		if (gpsDotLayer != null) {
			gpsDotLayer.setEnabled(displayShapes && showGpsSymbol);
		}
	}

	@Override
	public synchronized Point2D getMouseCoordinates() {
		return mouseCoordinates;
	}

	@Override
	public double getCursorElevationMeters() {
		if (getWwd().getCurrentPosition() != null) {
			return getWwd().getCurrentPosition().getElevation();
		} else {
			return Short.MIN_VALUE;
		}
	}

	@Override
	public double getDisplayAltitude() {
		return getWwd().getView().getEyePosition().getAltitude();
	}

	@Override
	public synchronized Point getMousePosition() {
		return mousePosition;
	}

	@Override
	public void showIcons(boolean showIcons) {
		this.showIcons = showIcons;
		iconLayer.setEnabled(showIcons);
	}

	@Override
	public void addIcon(Point2D lonLat, String iconPath, Dimension iconSize, long timeToDelete, long timeToGoStale) {
		final var pos = new Position(Angle.fromDegreesLatitude(lonLat.getY()),
				Angle.fromDegreesLongitude(lonLat.getX()), 0.0);
		final var ufi = new UserFacingIcon(iconPath, pos);
		ufi.setShowToolTip(showIconLabels);
		iconLayer.addIcon(ufi);
		iconList.add(ufi);
		redraw();
	}

	@Override
	public void deleteIcon(int index) {
		try {
			iconList.remove(index);
			redraw();
		} catch (final IndexOutOfBoundsException ex) {
			LOG.log(Level.WARNING, ex.getMessage(), ex);
		}
	}

	@Override
	public void deleteAllIcons() {
		iconLayer.removeAllIcons();
		iconList.subList(0, iconList.size()).clear();
		redraw();
	}

	@Override
	public void showIconLabels(boolean showIconLabels) {
		this.showIconLabels = showIconLabels;
		for (final WWIcon icon : iconLayer.getIcons()) {
			icon.setShowToolTip(showIconLabels);
		}
		redraw();
	}

	@Override
	public boolean moveIcon(int index, Point2D lonLat, long timeToDelete, long timeToGoStale) {
		try {
			final var pos = new Position(Angle.fromDegreesLatitude(lonLat.getY()),
					Angle.fromDegreesLongitude(lonLat.getX()), 0.0);
			iconList.get(index).moveTo(pos);
			redraw();
			return true;
		} catch (final IndexOutOfBoundsException ex) {
			LOG.log(Level.WARNING, ex.getMessage(), ex);
		}
		return false;
	}

	@Override
	public void deleteAllSignalMarkers() {
		final Iterator<Marker> itr = signalMarkerLayer.getMarkers().iterator();
		while (itr.hasNext()) {
			itr.next();
			itr.remove();
		}
		redraw();
	}

	@Override
	public void deleteAllFlightPathMarkers() {
		final Iterator<Marker> itr = flightPathMarkerLayer.getMarkers().iterator();
		while (itr.hasNext()) {
			itr.next();
			itr.remove();
		}
		redraw();
	}

	@Override
	public void deleteAllRings() {
		ringLayer.removeAllRenderables();
		ringList.subList(0, ringList.size()).clear();
		redraw();
	}

	@Override
	public void deleteAllLines() {
		lineLayer.removeAllRenderables();
		lineList.subList(0, lineList.size()).clear();
		redraw();
	}

	@Override
	public void deleteAllQuads() {
		quadLayer.removeAllRenderables();
		quadList.subList(0, quadList.size()).clear();
		redraw();
	}

	@Override
	public void deleteAllTestTiles() {
		try {
			testTileLayer.removeAllRenderables();
			geoTileList.subList(0, geoTileList.size()).clear();
			redraw();
		} catch (final NullPointerException ex) {
			LOG.log(Level.WARNING, ex.getMessage(), ex);
		}
	}

	@Override
	public boolean isShowGPSDot() {
		return gpsDotLayer.isEnabled();
	}

	@Override
	public boolean isShowGPSArrow() {
		return gpsArrowLayer.isEnabled();
	}

	@Override
	public void setGpsSymbol(final Point2D pt, final double radius, final Color color, final int angle) {
		try {
			if (angle == 360) {
				gpsDotLayer.setEnabled(showGpsSymbol);
				gpsArrowLayer.setEnabled(false);
			} else {
				gpsArrowLayer.setEnabled(showGpsSymbol);
				gpsDotLayer.setEnabled(false);
			}
			gpsDot.setPosition(Position.fromDegrees(pt.getY(), pt.getX(), 0));
			gpsArrow.setPosition(Position.fromDegrees(pt.getY(), pt.getX(), 0));
			gpsArrow.setHeading(Angle.fromDegrees(angle));
			gpsDotBma.setMaterial(new Material(color));
			gpsArrowBma.setMaterial(new Material(color));
			gpsArrowBma.setHeadingMaterial(new Material(color));
			gpsArrowBma.setMarkerPixels(radius * 1.5);
			gpsDotBma.setMarkerPixels(radius);
			redraw();
		} catch (final NullPointerException ex) {
			LOG.log(Level.WARNING, ex.getMessage(), ex);
		}
	}

	@Override
	public void setGpsDotRadius(double gpsDotRadius) {
		gpsDotBma.setMarkerPixels(gpsDotRadius);
		redraw();
	}

	@Override
	public void setGpsDotColor(Color color) {
		gpsDotBma.setMaterial(new Material(color));
		redraw();
	}

	@Override
	public void showTargetRing(boolean showTargetRing) {
		targetRingLayer.setEnabled(showTargetRing);
		redraw();
	}

	@Override
	public void setTargetRing(Point2D pt, double targetRingDiameter, Color color) {
		targetRingShape.getAttributes().setInteriorMaterial(new Material(color));
		targetRingShape.getAttributes().setOutlineMaterial(new Material(color));
		targetRingShape.setRadius(targetRingDiameter);
		targetRingShape.moveTo(Position.fromDegrees(pt.getY(), pt.getX()));
		redraw();
	}

	@Override
	public void setTargetRingPosition(Point2D pt) {
		targetRingShape.moveTo(Position.fromDegrees(pt.getY(), pt.getX()));
		redraw();
	}

	@Override
	public void setTargetRingRadius(double targetRingRadius) {
		targetRingShape.setRadius(targetRingRadius);
		redraw();
	}

	@Override
	public void setTargetRingColor(Color color) {
		targetRingShape.getAttributes().setInteriorMaterial(new Material(color));
		targetRingShape.getAttributes().setOutlineMaterial(new Material(color));
		redraw();
	}

	@Override
	public void setTargetRing(Point2D pt, double targetRingDiameter) {
		targetRingShape.moveTo(Position.fromDegrees(pt.getY(), pt.getX()));
		targetRingShape.setRadius(targetRingDiameter);
		redraw();
	}

	@Override
	public void deleteAllArcIntersectPoints() {
		final Iterator<Marker> itr = intersectLayer.getMarkers().iterator();
		while (itr.hasNext()) {
			itr.next();
			itr.remove();
		}
		redraw();
	}

	@Override
	public void setArcIntersectPointSize(double radius) {
		arcIntersectBma.setMarkerPixels(radius * 2);
		redraw();
	}

	@Override
	public void setArcIntersectPoints(List<Point2D> iplist) {
		deleteAllArcIntersectPoints();
		addArcIntersectPoints(iplist, arcIntersectPointRadius, arcIntersectPointColor);
	}

	@Override
	public void addArcIntersectPoints(List<Point2D> iplist) {
		addArcIntersectPoints(iplist, arcIntersectPointRadius, arcIntersectPointColor);
	}

	@Override
	public void setArcIntersectPoints(List<Point2D> iplist, double diameter, Color color) {
		deleteAllArcIntersectPoints();
		addArcIntersectPoints(iplist, diameter, color);
	}

	@Override
	public void addArcIntersectPoints(final List<Point2D> iplist, final double diameter, final Color color) {
		iplist.stream().map(ip -> new BasicMarker(Position.fromDegrees(ip.getY(), ip.getX()), arcIntersectBma))
				.forEachOrdered(intersectMarkers::add);
		redraw();
	}

	@Override
	public void addArcIntersectPoint(Point2D ip, double diameter, Color color) {
		final List<Point2D> iplist = new ArrayList<>(1);
		iplist.add(ip);
		addArcIntersectPoints(iplist, diameter, color);
	}

	@Override
	public void showArcIntersectPoints(boolean showArcIntersectPoints) {
		this.showArcIntersectPoints = showArcIntersectPoints;
		intersectLayer.setEnabled(showArcIntersectPoints);
		redraw();
	}

	@Override
	public void addSignalMarker(Point2D pt, Color color) {
		addSignalMarker(pt, DEFAULT_SIGNAL_MARKER_RADIUS, color);
	}

	@Override
	public void addSignalMarker(Point2D pt, double radius, Color color) {
		final var bma = new BasicMarkerAttributes();
		bma.setShapeType(BasicMarkerShape.CYLINDER);
		bma.setMaterial(new Material(color));
		bma.setMarkerPixels(radius * 2);
		bma.setOpacity(0.7D);
		final var signalMarker = new BasicMarker(Position.fromDegrees(pt.getY(), pt.getX()), bma);
		signalMarkers.add(signalMarker);
		redraw();
	}

	@Override
	public void setSignalMarkerRadius(double radius) {
		signalMarkers.forEach(signalMarker -> signalMarker.getAttributes().setMarkerPixels(radius * 2));
		redraw();
	}

	@Override
	public boolean isShowSignalMarkers() {
		return showSignalMarkers;
	}

	@Override
	public void deleteSignalMarker(int index) {
		try {
			signalMarkers.remove(index);
		} catch (final IndexOutOfBoundsException ex) {
			LOG.log(Level.SEVERE, ex.getMessage(), ex);
		}
		redraw();
	}

	@Override
	public boolean isShowArcIntersectPoints() {
		return showArcIntersectPoints;
	}

	@Override
	public synchronized void addTestTile(TestTile testTile) {
		final GeoTile geoTile = new GeoTile(testTile.getNorthWestLonLat(), testTile.getTileSizeInDegrees());
		geoTile.setID(testTile.getID());
		final BasicShapeAttributes attrs = new BasicShapeAttributes();
		attrs.setInteriorOpacity(0.3);
		attrs.setOutlineOpacity(0.4);
		attrs.setOutlineWidth(1);
		attrs.setEnableAntialiasing(true);
		attrs.setInteriorMaterial(new Material(testTile.getColor()));
		attrs.setOutlineMaterial(new Material(testTile.getColor()));
		geoTile.setAttributes(attrs);
		final BasicShapeAttributes flashAttrs = new BasicShapeAttributes();
		flashAttrs.setInteriorOpacity(0.0);
		flashAttrs.setOutlineOpacity(0.4);
		flashAttrs.setOutlineWidth(1);
		flashAttrs.setEnableAntialiasing(true);
		flashAttrs.setInteriorMaterial(new Material(testTile.getColor()));
		flashAttrs.setOutlineMaterial(new Material(testTile.getColor()));
		geoTile.setFlashAttributes(flashAttrs);
		geoTile.setFlash(false);
		geoTileList.add(geoTile);
		testTileLayer.addRenderable(geoTileList.get(geoTileList.size() - 1));
		setTestTileColor(geoTileList.get(geoTileList.size() - 1), Color.RED);
		redraw();
	}

	private synchronized void setTestTileColor(GeoTile geoTile, Color color) {
		try {
			getGeoTileByID(geoTile.getID()).getAttributes().setInteriorMaterial(new Material(color));
			getGeoTileByID(geoTile.getID()).getAttributes().setOutlineMaterial(new Material(color));
			redraw();
		} catch (final IndexOutOfBoundsException ex) {
			LOG.log(Level.WARNING, ex.getMessage(), ex);
		}
	}

	@Override
	public synchronized void setTestTileColor(TestTile testTile, Color color) {
		try {
			getGeoTileByID(testTile.getID()).getAttributes().setInteriorMaterial(new Material(color));
			getGeoTileByID(testTile.getID()).getAttributes().setOutlineMaterial(new Material(color));
			redraw();
		} catch (final IndexOutOfBoundsException ex) {
			LOG.log(Level.WARNING, ex.getMessage(), ex);
		}
	}

	@Override
	public void addQuad(Point2D lonLat, Point2D size, Color color) {
		final LatLon latLon = new LatLon(LatLon.fromDegrees(lonLat.getY(), lonLat.getX()));
		final SurfaceQuad surfaceQuad = new SurfaceQuad(latLon, size.getX(), size.getY());
		final ShapeAttributes bsa = new BasicShapeAttributes();
		bsa.setInteriorOpacity(0.3);
		bsa.setOutlineOpacity(0.4);
		bsa.setOutlineWidth(1);
		bsa.setEnableAntialiasing(true);
		bsa.setInteriorMaterial(new Material(color));
		bsa.setOutlineMaterial(new Material(color));
		surfaceQuad.setAttributes(bsa);
		quadList.add(surfaceQuad);
		quadLayer.addRenderable(surfaceQuad);
		redraw();
	}

	@Override
	public void changeQuadColor(int index, Color color) {
		try {
			quadList.get(index).getAttributes().setInteriorMaterial(new Material(color));
			quadList.get(index).getAttributes().setOutlineMaterial(new Material(color));
			redraw();
		} catch (final IndexOutOfBoundsException ex) {
			LOG.log(Level.SEVERE, ex.getMessage(), ex);
		}
	}

	@Override
	public void setQuadVisible(int index, boolean isVisible) {
		try {
			quadList.get(index).setVisible(isVisible);
			redraw();
		} catch (final IndexOutOfBoundsException ex) {
			LOG.log(Level.SEVERE, ex.getMessage(), ex);
		}
	}

	@Override
	public void deleteQuad(int index) {
		try {
			quadList.remove(index);
			redraw();
		} catch (final IndexOutOfBoundsException ex) {
			LOG.log(Level.SEVERE, ex.getMessage(), ex);
		}
	}

	@Override
	public void addLine(Point2D start, double angle, double distance, Color color) {
		final var bsa = new BasicShapeAttributes();
		bsa.setOutlineMaterial(new Material(color));
		bsa.setInteriorMaterial(new Material(color));
		bsa.setOutlineOpacity(0.8);
		bsa.setInteriorOpacity(0);
		bsa.setOutlineWidth(1);
		bsa.setEnableAntialiasing(true);
		final Point2D end = Vincenty.getVincentyDirect(start, angle, distance).getDestinationPoint();
		final List<LatLon> list = new ArrayList<>();
		list.add(LatLon.fromDegrees(start.getY(), start.getX()));
		list.add(LatLon.fromDegrees(end.getY(), end.getX()));
		final var surfacePolyline = new SurfacePolyline(list);
		surfacePolyline.setAttributes(bsa);
		lineLayer.addRenderable(surfacePolyline);
		lineList.add(surfacePolyline);
		redraw();
	}

	@Override
	public void addLine(Point2D lonLatStart, Point2D lonLatEnd, Color color) {
		final var bsa = new BasicShapeAttributes();
		bsa.setOutlineMaterial(new Material(color));
		bsa.setInteriorMaterial(new Material(color));
		bsa.setOutlineOpacity(0.8);
		bsa.setInteriorOpacity(0);
		bsa.setOutlineWidth(1);
		bsa.setEnableAntialiasing(true);
		final List<LatLon> latLonList = new ArrayList<>();
		latLonList.add(LatLon.fromDegrees(lonLatStart.getY(), lonLatStart.getX()));
		latLonList.add(LatLon.fromDegrees(lonLatEnd.getY(), lonLatEnd.getX()));
		final var surfacePolyline = new SurfacePolyline(latLonList);
		surfacePolyline.setAttributes(bsa);
		lineLayer.addRenderable(surfacePolyline);
		lineList.add(surfacePolyline);
		redraw();
	}

	@Override
	public void addRing(final Point2D lonLat, final double size, final Color color) {
		final var ringShape = new SurfaceCircle(LatLon.fromDegrees(lonLat.getY(), lonLat.getX()), size);
		final var bsa = new BasicShapeAttributes();
		bsa.setOutlineMaterial(new Material(color));
		bsa.setInteriorMaterial(new Material(color));
		bsa.setOutlineOpacity(0.8);
		bsa.setInteriorOpacity(0);
		bsa.setOutlineWidth(1);
		bsa.setEnableAntialiasing(true);
		ringShape.setAttributes(bsa);
		ringLayer.addRenderable(ringShape);
		ringList.add(ringShape);
		redraw();
	}

	@Override
	public long getAltitude() {
		return (long) view.getZoom();
	}

	@Override
	public void setAltitude(int altitude) {
		view.setZoom(altitude);
		redraw();
	}

	@Override
	public void zoomIn() {
		isZooming = true;
		resetOrbitView(view);
		view.setZoom(computeNewZoom(view, -ZOOM_STEP));
		zoomChanged();
		redraw();
	}

	@Override
	public void zoomOut() {
		isZooming = true;
		resetOrbitView(view);
		view.setZoom(computeNewZoom(view, ZOOM_STEP));
		zoomChanged();
		redraw();
	}

	@Override
	public void showGrid(boolean showGrid) {
		this.showGrid = showGrid;
		testGridLayer.setEnabled(showGrid);
		redraw();
	}

	@Override
	public boolean isShowGrid() {
		return showGrid;
	}

	@Override
	public void setTileSize(final Point2D gridSize) {
		this.gridSize = gridSize;
		final SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				synchronized (renderingHold) {
					while (!wwdRendered) {
						renderingHold.wait();
					}
				}
				return null;
			}

			@Override
			protected void done() {
				testGridLayer.setTileSize(gridSize);
				redraw();
			}
		};
		worker.execute();
	}

	@Override
	public int getIconListSize() {
		return iconList.size();
	}

	@Override
	public void showQuads(boolean showQuads) {
		this.showQuads = showQuads;
		quadLayer.setEnabled(showQuads);
		redraw();
	}

	@Override
	public void showTestTiles(boolean showPolygons) {
		this.showPolygons = showPolygons;
		testTileLayer.setEnabled(showPolygons);
		redraw();
	}

	@Override
	public void showSignalMarkers(boolean showSignalMarkers) {
		this.showSignalMarkers = showSignalMarkers;
		signalMarkerLayer.setEnabled(showSignalMarkers);
		redraw();
	}

	@Override
	public void showLines(boolean showLines) {
		this.showLines = showLines;
		lineLayer.setEnabled(showLines);
		redraw();
	}

	@Override
	public boolean isShowLines() {
		return showLines;
	}

	@Override
	public void showRings(boolean showRings) {
		this.showRings = showRings;
		ringLayer.setEnabled(showRings);
		redraw();
	}

	@Override
	public boolean isShowRings() {
		return showRings;
	}

	@Override
	public BufferedImage getScreenShot() {
		final GLProfile glProfile = GLProfile.getDefault();
		final AWTGLReadBufferUtil aWTGLReadBufferUtil = new AWTGLReadBufferUtil(glProfile, true);
		getWwd().getContext().makeCurrent();
		final GL gl = getWwd().getContext().getGL();
		getWwd().getContext().release();

		return aWTGLReadBufferUtil.readPixelsToBufferedImage(gl, 0, 0, getWidth(), getHeight(), true);
	}

	private double scaleToAltitude(double scale) {
		return 65000.0 - (5000.0 * scale);
	}

	@Override
	public void setScale(double scale) {
		view.setZoom(scaleToAltitude(scale));
		redraw();
	}

	private double altitudeToScale(double altitude) {
		return 13.0 - ((altitude / 50000.0) * 10.0);
	}

	@Override
	public double getScale() {
		return altitudeToScale(view.getZoom());
	}

	@Override
	public Point2D getGridSize() {
		return gridSize;
	}

	@Override
	public void deleteAllArcs() {
		arcLayer.removeAllRenderables();
		traceLineLayer.removeAllRenderables();
		asymptoteLayer.removeAllRenderables();
		traceDotMarkers.subList(0, traceDotMarkers.size()).clear();
		cursorMarkers.subList(0, cursorMarkers.size()).clear();
		intersectMarkers.subList(0, intersectMarkers.size()).clear();
		arcList.subList(0, arcList.size()).clear();
		redraw();
	}

	@Override
	public void removeArc(int index) {
		try {
			arcList.remove(index);
			redraw();
		} catch (final IndexOutOfBoundsException ex) {
			LOG.log(Level.SEVERE, ex.getMessage(), ex);
		}
	}

	@Override
	public void showArcs(boolean showArcs) {
		this.showArcs = showArcs;
		arcLayer.setEnabled(showArcs);
		redraw();
	}

	@Override
	public boolean isShowMapImage() {
		return isVisible();
	}

	@Override
	public void showMapImage(boolean showMapImage) {
		setVisible(showMapImage);
		redraw();
	}

	@Override
	public void showArcAsymptotes(boolean showArcAsymptotes) {
		this.showArcAsymptotes = showArcAsymptotes;
		asymptoteLayer.setEnabled(showArcAsymptotes);
		redraw();
	}

	@Override
	public void showArcCursors(boolean showArcCursors) {
		this.showArcCursors = showArcCursors;
		cursorLayer.setEnabled(showArcCursors);
		redraw();
	}

	@Override
	public void showArcTrace(boolean showArcTrace) {
		this.showArcTraces = showArcTrace;
		traceDotLayer.setEnabled(showArcTrace);
		traceLineLayer.setEnabled(showArcTrace);
		redraw();
	}

	@Override
	public void showGpsSymbol(boolean showGpsSymbol) {
		this.showGpsSymbol = showGpsSymbol;
		if (!showGpsSymbol) {
			gpsDotLayer.setEnabled(false);
			gpsArrowLayer.setEnabled(false);
		}
		redraw();
	}

	private void createArcSurfacePolyline(ConicSection cone) {
		final ShapeAttributes bsa = new BasicShapeAttributes();
		final List<LatLon> hyperbolicLatLonArray = cone.getHyperbolicLatLonList();
		final SurfacePolyline arcSurfacePolyline = new SurfacePolyline(hyperbolicLatLonArray);
		if (arcColors != null) {
			bsa.setOutlineMaterial(new Material(arcColors[cone.getSMB().getFlight()]));
		}
		bsa.setOutlineOpacity(0.9);
		bsa.setOutlineWidth(1.5);
		bsa.setEnableAntialiasing(true);
		arcSurfacePolyline.setAttributes(bsa);
		arcLayer.addRenderable(arcSurfacePolyline);
		redraw();
	}

	public void createArcCursors(ConicSection cone, double diameter, Color color) {
		arcCursorBma.setMaterial(new Material(color));
		arcCursorBma.setOpacity(0.8);
		arcCursorBma.setMarkerPixels(diameter);
		final Marker center = new BasicMarker(Position.fromDegrees(cone.getCenter().getY(), cone.getCenter().getX()),
				arcCursorBma);
		final Marker vertex = new BasicMarker(Position.fromDegrees(cone.getVertex().getY(), cone.getVertex().getX()),
				arcCursorBma);
		final Marker focus = new BasicMarker(Position.fromDegrees(cone.getFocus().getY(), cone.getFocus().getX()),
				arcCursorBma);
		cursorMarkers.add(center);
		cursorMarkers.add(vertex);
		cursorMarkers.add(focus);
		redraw();
	}

	private void createArcTraces(ConicSection cone, double diameter) {
		final var bsa = new BasicShapeAttributes();
		final var bma = new BasicMarkerAttributes();
		if (traceEqualsFlightColor && (arcColors != null)) {
			bsa.setOutlineMaterial(new Material(arcColors[cone.getSMB().getFlight()]));
			bma.setMaterial(new Material(arcColors[cone.getSMB().getFlight()]));
		} else {
			bsa.setOutlineMaterial(new Material(arcTraceColor));
			bma.setMaterial(new Material(arcTraceColor));
		}
		bsa.setOutlineOpacity(0.8);
		bsa.setOutlineWidth(1);
		bsa.setEnableAntialiasing(true);
		bma.setShapeType(BasicMarkerShape.CYLINDER);
		bma.setMarkerPixels(diameter);
		bma.setOpacity(0.8D);
		final List<LatLon> points = new ArrayList<>();
		points.add(LatLon.fromDegrees(cone.getSMA().getPoint().getY(), cone.getSMA().getPoint().getX()));
		points.add(LatLon.fromDegrees(cone.getSMB().getPoint().getY(), cone.getSMB().getPoint().getX()));
		traceLineLayer.addRenderable(new SurfacePolyline(bsa, points));
		traceDotMarkers.add(new BasicMarker(
				Position.fromDegrees(cone.getSMA().getPoint().getY(), cone.getSMA().getPoint().getX()), bma));
		traceDotMarkers.add(new BasicMarker(
				Position.fromDegrees(cone.getSMB().getPoint().getY(), cone.getSMB().getPoint().getX()), bma));
		traceDotLayer.setMarkers(traceDotMarkers);
		redraw();
	}

	private void createArcAsymptotes(ConicSection cone, Color color) {
		final var surfacePolyline = new SurfacePolyline(cone.getAsymptoteLatLonList());
		arcAsymptoteBsa.setOutlineMaterial(new Material(color));
		arcAsymptoteBsa.setInteriorMaterial(new Material(color));
		surfacePolyline.setAttributes(arcAsymptoteBsa);
		asymptoteLayer.addRenderable(surfacePolyline);
		redraw();
	}

	@Override
	public void addArc(final ConicSection cone) {
		arcList.add(cone);
		createArcSurfacePolyline(cone);
		createArcAsymptotes(cone, arcAsymptoteColor);
		createArcTraces(cone, arcTraceRadius);
		createArcCursors(cone, arcCursorRadius, arcCursorColor);
		redraw();
	}

	@Override
	public void setArcAsymptoteColor(Color arcAsymptoteColor) {
		this.arcAsymptoteColor = arcAsymptoteColor;
		arcAsymptoteBsa.setOutlineMaterial(new Material(arcAsymptoteColor));
		redraw();
	}

	@Override
	public void setArcColors(Color[] arcColors) {
		this.arcColors = arcColors.clone();
		redraw();
	}

	private void updateTraces() {
		traceLineLayer.removeAllRenderables();
		traceDotMarkers.subList(0, traceDotMarkers.size()).clear();
		arcList.forEach(cone -> createArcTraces(cone, arcTraceRadius));
		redraw();
	}

	@Override
	public void setTraceEqualsFlightColor(boolean traceEqualsFlightColor) {
		if (this.traceEqualsFlightColor == traceEqualsFlightColor) {
			return;
		}
		this.traceEqualsFlightColor = traceEqualsFlightColor;
		updateTraces();
		redraw();
	}

	@Override
	public void setArcTraceColor(Color arcTraceColor) {
		if (this.arcTraceColor.equals(arcTraceColor)) {
			return;
		}
		this.arcTraceColor = arcTraceColor;
		updateTraces();
		redraw();
	}

	@Override
	public void setArcCursorColor(Color arcCursorColor) {
		this.arcCursorColor = arcCursorColor;
		arcCursorBma.setMaterial(new Material(arcCursorColor));
		redraw();
	}

	@Override
	public void setArcIntersectPointColor(Color arcIntersectPointColor) {
		this.arcIntersectPointColor = arcIntersectPointColor;
		arcIntersectBma.setMaterial(new Material(arcIntersectPointColor));
		redraw();
	}

	@Override
	public boolean isShowTargetRing() {
		return showTargetRing;
	}

	@Override
	public void setGpsSymbolColor(Color color) {
		gpsDotBma.setMaterial(new Material(color));
		gpsArrowBma.setMaterial(new Material(color));
		gpsArrowBma.setHeadingMaterial(new Material(color));
		redraw();
	}

	@Override
	public void setGpsSymbolAngle(int gpsAngle) {
		gpsArrow.setHeading(Angle.fromDegrees(gpsAngle));
	}

	@Override
	public long getMaxAltitude() {
		return MAX_ALTITUDE;
	}

	@Override
	public void setArcTraceRadius(double arcTraceRadius) {
		this.arcTraceRadius = arcTraceRadius;
		updateTraces();
		redraw();
	}

	@Override
	public void setArcCursorRadius(double arcCursorRadius) {
		this.arcCursorRadius = arcCursorRadius;
		arcCursorBma.setMarkerPixels(arcCursorRadius * 2);
		redraw();
	}

	public static float zoomToResolution(int zoom, float latitude) {
		return (float) ((156543.034 * Math.cos(latitude)) / (2 ^ zoom));
	}

	public static float resolutionToScale(float resolution) {
		final int dpi = Toolkit.getDefaultToolkit().getScreenResolution();
		return (float) (dpi * 39.37 * resolution);
	}

	public static float scaleToOsmZoomLevel(float mapScale, float latitude) {
		final int dpi = Toolkit.getDefaultToolkit().getScreenResolution();
		final double metersPerInch = 2.54D / 100;
		final double realLengthInMeters = 40075016.686 * Math.cos(Math.toRadians(latitude));
		final double zoomLevelExp = (realLengthInMeters * dpi) / (256 * metersPerInch * mapScale);
		return (float) Math.pow(zoomLevelExp, 2);
	}

	private void insertBeforePlacenames(WorldWindow wwd, Layer layer) {
		var position = 0;
		final LayerList layers = wwd.getModel().getLayers();
		for (final Layer l : layers) {
			if (l instanceof PlaceNameLayer) {
				position = layers.indexOf(l);
			}
		}
		layers.add(position, layer);
	}

	private void insertBeforeCompass(WorldWindow wwd, Layer layer) {
		var compassPosition = 0;
		final LayerList layers = wwd.getModel().getLayers();
		for (final Layer l : layers) {
			if (l instanceof CompassLayer) {
				compassPosition = layers.indexOf(l);
			}
		}
		layers.add(compassPosition, layer);
	}

	private void gridRedraw() {
		testGridLayer.repaint();
		redraw();
		if (!view.isAnimating()) {
			gridRedrawHandle.cancel(false);
			animationRedrawTimerActive = false;
		}
	}

	private void gridRedrawAnimating(int initialDelay, int delay) {
		try (ScheduledExecutorService gridRedrawScheduler = Executors.newScheduledThreadPool(0)) {
			animationRedrawTimerActive = true;
			final Runnable gridRedraw = (this::gridRedraw);
			gridRedrawHandle = gridRedrawScheduler.scheduleAtFixedRate(gridRedraw, initialDelay, delay,
					TimeUnit.MILLISECONDS);
		}
	}

	@Override
	public long getMinAltitude() {
		return MIN_ALTITUDE;
	}

	private void zoomChanged() {
		getPropertyChangeSupport().firePropertyChange(MapEvent.ZOOM_IN_DISABLED.name(), null,
				altitude <= getMinAltitude());
		getPropertyChangeSupport().firePropertyChange(MapEvent.ZOOM_OUT_DISABLED.name(), null,
				altitude >= getMaxAltitude());
	}

	@Override
	public boolean isShowTestTiles() {
		return showPolygons;
	}

	@Override
	public void setGridReference(Point2D gridReference) {
		testGridLayer.setGridReference(gridReference);
	}

	@Override
	public void setGridSize(Point2D gridSize) {
		testGridLayer.setGridSize(gridSize);
	}

	@Override
	public void setGrid(Point2D tileSize, Point2D gridReference, Point2D gridSize) {
		testGridLayer.setGrid(tileSize, gridReference, gridSize);
	}

	@Override
	public void setGridColor(Color gridColor) {
		testGridLayer.setGridColor(gridColor);
		redraw();
	}

	@Override
	public void clearCache() {
		new Thread(new ClearCache()).start();
	}

	@Override
	public void deleteLine(int index) {
		if (index < lineList.size()) {
			lineList.get(index).setVisible(false);
			redraw();
		}
	}

	@Override
	public void deleteRing(int index) {
		if (index < ringList.size()) {
			ringList.get(index).setVisible(false);
			redraw();
		}
	}

	@Override
	public void deleteArc(int index) {
		if (index < arcList.size()) {
			arcList.remove(index);
			redraw();
		}
	}

	@Override
	public void deleteTestTile(TestTile testTile) {
		try {
			final GeoTile g = getGeoTileByID(testTile.getID());
			testTileLayer.removeRenderable(g);
			geoTileList.remove(g.getID());
			redraw();
		} catch (final IndexOutOfBoundsException ex) {
			LOG.log(Level.WARNING, ex.getMessage(), ex);
		}
	}

	@Override
	public void redraw() {
		try {
			getWwd().redraw();
		} catch (NullPointerException ex) {
			LOG.log(Level.INFO, ex.getMessage(), ex);
		}
	}

	@Override
	public Point2D getMouseDragCoordinates() {
		return mouseDragCoordinates;
	}

	@Override
	public void deleteTestGrid() {
		testGridLayer.removeAllRenderables();
		redraw();
	}

	@Override
	public void setSelectionRectangleVisible(boolean visible) {
		selectionRectangleLayer.setEnabled(visible);
		redraw();
	}

	@Override
	public void setSelectionRectangleMode(boolean selectionRectangleMode) {
		if (selectionRectangleMode) {
			setSelectionRectangle(
					new Rectangle2D.Double(getMouseCoordinates().getX(), getMouseCoordinates().getY(), 0, 0));
		} else {
			selectionRectangleLayer.removeAllRenderables();
		}
	}

	// SurfaceQuad is referenced to the center in degrees and dimensions are in
	// meters. We use The Vincente Formulae to convert between a linear Recangle2D
	// and the WGS84 ellipsoid.
	private Rectangle2D getRectangleFromSurfaceQuad(SurfaceQuad quad) {
		final Point2D center = new Point2D.Double(quad.getCenter().getLongitude().getDegrees(),
				quad.getCenter().getLatitude().getDegrees());
		final double x = Vincenty.getVincentyDirect(center, 270, quad.getWidth() / 2.0).getDestinationX();
		final double y = Vincenty.getVincentyDirect(center, 0, quad.getHeight() / 2.0).getDestnationY();
		return new Rectangle2D.Double(x, y, (center.getX() - x) * 2, (y - center.getY()) * 2);
	}

	@Override
	public Rectangle2D getSelectionRectangle() {
		final SurfaceQuad quad = (SurfaceQuad) selectionRectangleLayer.getRenderables().iterator().next();
		return getRectangleFromSurfaceQuad(quad);
	}

	public static LatLon getCenterLatLonfromRectangle(RectangularShape rectangle) {
		final double x = rectangle.getX() + (rectangle.getWidth() / 2D);
		final double y = rectangle.getY() - (rectangle.getHeight() / 2D);
		return new LatLon(Angle.fromDegrees(y), Angle.fromDegrees(x));
	}

	// Rectangle2D.Double is referenced to the upper left and all values are in
	// degrees.
	@Override
	public void setSelectionRectangle(final Rectangle2D rectangle) {
		selectionRectangleLayer.removeAllRenderables();
		final Point2D origin = new Point2D.Double(rectangle.getX(), rectangle.getY());
		final Point2D dX = new Point2D.Double(rectangle.getX() + rectangle.getWidth(), rectangle.getY());
		final Point2D dY = new Point2D.Double(rectangle.getX(), rectangle.getY() - rectangle.getHeight());
		final double width = Vincenty.getVincentyInverse(origin, dX).getDistanceMeters();
		final double height = Vincenty.getVincentyInverse(origin, dY).getDistanceMeters();
		final LatLon latLon = getCenterLatLonfromRectangle(rectangle);
		final SurfaceQuad surfaceQuad = new SurfaceQuad(latLon, width, height);
		final ShapeAttributes bsa = new BasicShapeAttributes();
		bsa.setInteriorOpacity(0.1);
		bsa.setOutlineOpacity(1.0);
		bsa.setOutlineWidth(1);
		bsa.setEnableAntialiasing(true);
		bsa.setInteriorMaterial(new Material(Color.RED));
		bsa.setOutlineMaterial(new Material(Color.RED));
		surfaceQuad.setAttributes(bsa);
		selectionRectangleLayer.addRenderable(surfaceQuad);
		redraw();
	}

	@Override
	public Point2D getSelectionRectangleOrigin() {
		return selectionRectangleOrigin;
	}

	@Override
	public void setSelectionRectangleOrigin(Point2D selectionRectangleOrigin) {
		this.selectionRectangleOrigin = selectionRectangleOrigin;
	}

	@Override
	public void setSelectionRectangleDestination(Point2D point) {
		setSelectionRectangle(new Rectangle2D.Double(getSelectionRectangleOrigin().getX(),
				getSelectionRectangleOrigin().getY(), Math.abs(point.getX() - getSelectionRectangleOrigin().getX()),
				Math.abs(getSelectionRectangleOrigin().getY() - point.getY())));
	}

	@Override
	public void addArc(HyperbolicProjection hyperbola) {
		// NO OP
	}

	@Override
	public boolean isPointInBounds(Point2D p) {
		final var imageBounds = new Rectangle(getInsets().left, getInsets().top, getWidth(), getHeight());
		return imageBounds.contains(p);
	}

	@Override
	public void fitToDisplay(boolean gpsMarker, boolean signalMarker, boolean flightPathMarker, boolean testTiles,
			boolean rings, boolean targetRing, boolean gridLines, boolean quads, boolean icons, boolean lines,
			boolean arcs, boolean arcIntersects) {

		viewController.setEnabled(true);

		final List<Object> objects = Collections.synchronizedList(new CopyOnWriteArrayList<>());

		objects.clear();

		if (arcs) {
			objects.addAll(arcList);
		}
		if (arcs) {
			objects.addAll(cursorMarkers);
		}
		if (icons) {
			objects.addAll(iconList);
		}
		if (arcIntersects) {
			objects.addAll(intersectMarkers);
		}
		if (arcIntersects) {
			objects.addAll(traceDotMarkers);
		}
		if (signalMarker) {
			objects.addAll(signalMarkers);
		}

		if (flightPathMarker) {
			objects.addAll(flightPathMarkers);
		}

		if (lines) {
			objects.addAll(lineList);
		}
		if (quads) {
			objects.addAll(quadList);
		}
		if (rings) {
			objects.addAll(ringList);
		}
		if (testTiles) {
			objects.addAll(geoTileList);
		}
		if (gpsMarker) {
			objects.add(gpsDot);
		}
		if (gpsMarker) {
			objects.add(gpsArrow);
		}
		if (targetRing) {
			objects.add(targetRingShape);
		}

		viewController.setObjectsToTrack(objects);

		viewController.gotoScene();
	}

	@Override
	public Point2D getUpperLeftLonLat() {
		final var pos = view.computePositionFromScreenPoint(0, 0);
		return new Point2D.Double(pos.getLongitude().getDegrees(), pos.getLatitude().getDegrees());
	}

	@Override
	public Point2D getLowerRightLonLat() {
		final var pos = view.computePositionFromScreenPoint(getSize().width, getHeight());
		return new Point2D.Double(pos.getLongitude().getDegrees(), pos.getLatitude().getDegrees());
	}

	@Override
	public Point2D getGridReference() {
		return testGridLayer.getGridReference();
	}

	@Override
	public void initialize() {
		createWorldWindMap();
		configureComponents();
		configurePanel();
		insertMapLayers();
		configureListeners();
	}

	@Override
	public boolean moveIcon(String name, Point2D point, long timeToDelete, long timeToGoStale) {
		for (WWIcon icon : iconLayer.getIcons()) {
			if (icon.getToolTipText().equals(name)) {
				icon.setPosition(new Position(Angle.fromDegreesLatitude(point.getY()),
						Angle.fromDegreesLongitude(point.getX()), 0.0));
				return true;
			}
		}
		return false;
	}

	@Override
	public void addIcon(Point2D point, BufferedImage image, String iconName, Dimension iconSize, long timeToDelete,
			long timeToGoStale) {
		final var pos = new Position(Angle.fromDegreesLatitude(point.getY()), Angle.fromDegreesLongitude(point.getX()),
				0.0);
		final var ufi = new UserFacingIcon(image, pos);
		ufi.setSize(iconSize);
		ufi.setToolTipText(iconName);
		iconList.add(ufi);
		iconLayer.addIcon(ufi);
		redraw();
	}

	@Override
	public void setShowStatusBar(boolean showStatusBar) {
		super.setShowStatusBar(showStatusBar);
		if (statusBar != null) {
			if (showStatusBar) {
				add(statusBar, BorderLayout.PAGE_END);
				statusBar.setVisible(showStatusBar);
			} else {
				remove(statusBar);
			}
		}
	}

	@Override
	public void setGpsSymbolRadius(double radius) {
		gpsArrowBma.setMarkerPixels(radius * 1.5);
		gpsDotBma.setMarkerPixels(radius);
	}

	@Override
	public double getGpsSymbolRadius() {
		return gpsDotBma.getMarkerPixels();
	}

	@Override
	public int getGpsSymbolAngle() {
		return (int) gpsArrow.getHeading().getDegrees();
	}

	@Override
	public void setGpsSymbolLonLat(Point2D pt) {
		gpsDot.setPosition(Position.fromDegrees(pt.getY(), pt.getX(), 0));
		gpsArrow.setPosition(Position.fromDegrees(pt.getY(), pt.getX(), 0));
	}

	@Override
	public Point2D getGpsSymbolLonLat() {
		return new Point2D.Double(gpsDot.getPosition().getLongitude().getDegrees(),
				gpsDot.getPosition().getLatitude().getDegrees());
	}

	@Override
	public Color getGpsSymbolColor() {
		return gpsDotBma.getMaterial().getAmbient();
	}

	@Override
	public void setRulerMode(boolean rulerMode) {
		if (rulerMode) {
			if (mtd != null) {
				mtd.setVisible(true);
			} else {
				mtd = new MeasureToolDialog(getWwd(), terrainProfileLayer);
				if (rulerColor != null) {
					mtd.getMeasureTool().setLineColor(rulerColor);
				}
				mtd.getMeasureTool().addPropertyChangeListener((PropertyChangeEvent event) -> {
					if (event.getPropertyName().equals(MeasureTool.EVENT_METRIC_CHANGED)) {
						final MeasureTool mt = (MeasureTool) event.getSource();
						if (mt.getOrientation() != null && mt.getPositions() != null) {
							final SurfaceLine sl = new SurfaceLine(mt.getLength(), mt.getOrientation().degrees,
									mt.getPositions().get(0).getLatitude().getDegrees());
							getPropertyChangeSupport().firePropertyChange(MapEvent.MEASURE_TOOL_SOLUTION.name(), null,
									sl);
						}
					}
				});
			}
		} else {
			mtd.dispose();
		}
	}

	@Override
	public void setTilesToDownload(Rectangle2D selectionRectangle) {
		// NO OP
	}

	private boolean isFlatGlobe() {
		return getWwd().getModel().getGlobe() instanceof FlatGlobe;
	}

	@Override
	public void close() {
		if (isClosing) {
			return;
		}
		isClosing = true;
		userPref.putBoolean("flat", isFlatGlobe());
		userPref.put("projectionName", getProjectionName());
		viewControlsLayer.dispose();
		iconLayer.dispose();
		gpsArrowLayer.dispose();
		gpsDotLayer.dispose();
		intersectLayer.dispose();
		cursorLayer.dispose();
		traceDotLayer.dispose();
		signalMarkerLayer.dispose();
		testGridLayer.dispose();
		selectionRectangleLayer.dispose();
		targetRingLayer.dispose();
		lineLayer.dispose();
		ringLayer.dispose();
		quadLayer.dispose();
		arcLayer.dispose();
		asymptoteLayer.dispose();
		traceLineLayer.dispose();
		testTileLayer.dispose();
		setLayout(null);
		if (getWwd() != null) {
			saveLayerSelections(getWwd());
			getWwd().shutdown();
			wwd = null;
		}
		savePreferences();
		removeAll();
		WorldWind.shutDown();
	}

	@Override
	public String getUniqueIdentifier() {
		return Long.toString(serialVersionUID);
	}

	@Override
	public SignalTrackMapNames getSignalTrackMapName() {
		return SignalTrackMapNames.WorldWindMap;
	}

	@Override
	public boolean hasLayerSelectorPanel() {
		return true;
	}

	@Override
	public boolean hasBulkDownloaderPanel() {
		return true;
	}

	@Override
	public boolean hasStatisticsPanel() {
		return true;
	}

	@Override
	public boolean hasCacheViewerPanel() {
		return true;
	}

	private String getProjectionName() {
		return (isFlatGlobe()) ? ((Globe2D) getWwd().getModel().getGlobe()).getProjection().getName()
				: "Equirectangular";
	}

	private GeographicProjection getProjection(String projectionName) {
		return switch (projectionName) {
		case "Mercator" -> new ProjectionMercator();
		case "Sinusoidal" -> new ProjectionSinusoidal();
		case "Modified Sinusoidal" -> new ProjectionModifiedSinusoidal();
		case "Transverse Mercator" -> new ProjectionTransverseMercator();
		case "North Polar Equidistant" -> new ProjectionPolarEquidistant(AVKey.NORTH);
		case "South Polar Equidistant" -> new ProjectionPolarEquidistant(AVKey.SOUTH);
		case "North Universal Polar Stereographic" -> new ProjectionUPS(AVKey.NORTH);
		case "South Universal Polar Stereographic" -> new ProjectionUPS(AVKey.SOUTH);
		default -> new ProjectionEquirectangular();
		};
	}

	@Override
	public void setRulerColor(Color rulerColor) {
		this.rulerColor = rulerColor;
		redraw();
	}

	protected double computeNewZoom(OrbitView view, double amount) {
		final double coeff = 0.05;
		final double change = coeff * amount;
		final double logZoom = view.getZoom() > 0 ? Math.log(view.getZoom()) : 0;
		// Zoom changes are treated as logarithmic values. This accomplishes two things:
		// 1) Zooming is slow near the globe, and fast at great distances.
		// 2) Zooming in then immediately zooming out returns the viewer to the same
		// zoom value.
		return Math.exp(logZoom + change);
	}

	/**
	 * Reset the view to an orbit view state if in first person mode (zoom = 0)
	 *
	 * @param view the orbit view to reset
	 */
	protected void resetOrbitView(OrbitView view) {
		if (view.getZoom() > 0) {
			return;
		}

		// Find out where on the terrain the eye is looking at in the viewport center
		final Vec4 centerPoint = computeSurfacePoint(view, view.getHeading(), view.getPitch());
		// Reset the orbit view center point heading, pitch and zoom
		if (centerPoint != null) {
			final Vec4 eyePoint = view.getEyePoint();
			// Center pos on terrain surface
			final Position centerPosition = getWwd().getModel().getGlobe().computePositionFromPoint(centerPoint);
			// Compute pitch and heading relative to center position
			final Vec4 normal = getWwd().getModel().getGlobe().computeSurfaceNormalAtLocation(centerPosition.getLatitude(),
					centerPosition.getLongitude());
			final Vec4 north = getWwd().getModel().getGlobe()
					.computeNorthPointingTangentAtLocation(centerPosition.getLatitude(), centerPosition.getLongitude());
			// Pitch
			view.setPitch(Angle.POS180.subtract(view.getForwardVector().angleBetween3(normal)));
			// Heading
			final Vec4 perpendicular = view.getForwardVector().perpendicularTo3(normal);
			final Angle heading = perpendicular.angleBetween3(north);
			final double direction = Math.signum(-normal.cross3(north).dot3(perpendicular));
			view.setHeading(heading.multiply(direction));
			// Zoom
			view.setZoom(eyePoint.distanceTo3(centerPoint));
			// Center pos
			view.setCenterPosition(centerPosition);
		}
	}

	/**
	 * Find out where on the terrain surface the eye would be looking at with the
	 * given heading and pitch angles.
	 *
	 * @param view    the orbit view
	 * @param heading heading direction clockwise from north.
	 * @param pitch   view pitch angle from the surface normal at the center point.
	 *
	 * @return the terrain surface point the view would be looking at in the
	 *         viewport center.
	 */
	protected Vec4 computeSurfacePoint(OrbitView view, Angle heading, Angle pitch) {
		final Globe globe = getWwd().getModel().getGlobe();
		// Compute transform to be applied to north pointing Y so that it would point in
		// the view direction
		// Move coordinate system to view center point
		Matrix transform = globe.computeSurfaceOrientationAtPosition(view.getCenterPosition());
		// Rotate so that the north pointing axes Y will point in the look at direction
		transform = transform.multiply(Matrix.fromRotationZ(heading.multiply(-1)));
		transform = transform.multiply(Matrix.fromRotationX(Angle.NEG90.add(pitch)));
		// Compute forward vector
		final Vec4 forward = Vec4.UNIT_Y.transformBy4(transform);
		// Return intersection with terrain
		final Intersection[] intersections = getWwd().getSceneController().getTerrain()
				.intersect(new Line(view.getEyePoint(), forward));
		return (intersections != null && intersections.length != 0) ? intersections[0].getIntersectionPoint() : null;
	}

	private GeoTile getGeoTileByID(int tileID) {
		final List<GeoTile> result = geoTileList.stream().filter(item -> item.getID() == tileID).toList();
		return result.get(0);
	}

	@Override
	public void setTileFlash(int tileID, boolean flash) {
		getGeoTileByID(tileID).setFlash(flash);
		redraw();
	}

	@Override
	public boolean isTileFlash(int tileID) {
		return getGeoTileByID(tileID).isFlash();
	}

	@Override
	public void stopAllTestTileFlash() {
		geoTileList.stream().forEach(tile -> tile.setFlash(false));
		redraw();
	}

	@Override
	public void clearMapObjects() {
		deleteAllSignalMarkers();
		deleteAllIcons();
		deleteAllLines();
		deleteAllQuads();
		deleteAllRings();
		deleteAllArcs();
		deleteAllTestTiles();
		deleteTestGrid();
		deleteAllArcIntersectPoints();
	}

	private class ClearCache implements Runnable {
		@Override
		public void run() {
			getWwd().getGpuResourceCache().clear();
			FileStoreDataSet.getDataSets(new BasicDataFileStore().getWriteLocation()).clear();
		}
	}

	@Override
	public void addFlightPathMarker(Point2D pt, Color color) {
		addFlightPathMarker(pt, DEFAULT_SIGNAL_MARKER_RADIUS, color);
	}

	@Override
	public void addFlightPathMarker(Point2D pt, double radius, Color color) {
		final BasicMarkerAttributes bma = new BasicMarkerAttributes();
		bma.setShapeType(BasicMarkerShape.HEADING_LINE);
		bma.setMaterial(new Material(color));
		bma.setMarkerPixels(radius * 2);
		bma.setOpacity(0.7D);
		final var flightPathMarker = new BasicMarker(Position.fromDegrees(pt.getY(), pt.getX()), bma);
		flightPathMarkers.add(flightPathMarker);
		redraw();
	}

	@Override
	public void setFlightPathMarkerRadius(double radius) {
		flightPathMarkers.forEach(flightPathMarker -> flightPathMarker.getAttributes().setMarkerPixels(radius * 2));
		redraw();
	}

	@Override
	public boolean isShowFlightPathMarkers() {
		return showFlightPathMarkers;
	}

	@Override
	public void deleteFlightPathMarker(int index) {
		try {
			flightPathMarkers.remove(index);
		} catch (final IndexOutOfBoundsException ex) {
			LOG.log(Level.SEVERE, ex.getMessage(), ex);
		}
		redraw();
	}

	@Override
	public void showFlightPathMarkers(boolean showFlightPathMarkers) {
		this.showFlightPathMarkers = showFlightPathMarkers;
	}

}
