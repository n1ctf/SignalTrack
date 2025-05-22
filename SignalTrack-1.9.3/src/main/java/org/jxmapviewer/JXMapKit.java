/*
 * JXMapKit.java
 *
 * Created on November 19, 2006, 3:52 AM
 */
package org.jxmapviewer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;

import java.awt.event.ActionEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;

import javax.swing.event.ChangeEvent;
import javax.swing.event.MouseInputListener;

import org.apache.commons.math3.util.Precision;

import org.jxmapviewer.cache.CachePreloader;
import org.jxmapviewer.cache.FileBasedLocalCache;

import org.jxmapviewer.fma.jtiledownloader.DownloadConfigurationBBoxLatLon;
import org.jxmapviewer.fma.jtiledownloader.DownloadJob;
import org.jxmapviewer.fma.jtiledownloader.ProgressBar;
import org.jxmapviewer.fma.jtiledownloader.TileList;
import org.jxmapviewer.fma.jtiledownloader.TileListDownloader;
import org.jxmapviewer.fma.jtiledownloader.TileProviderIf;
import org.jxmapviewer.fma.jtiledownloader.TileProviderList;

import org.jxmapviewer.input.CenterMapListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;

import org.jxmapviewer.painter.AbstractPainter;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;

import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.Tile;
import org.jxmapviewer.viewer.TileCache;
import org.jxmapviewer.viewer.TileFactory;
import org.jxmapviewer.viewer.TileFactoryInfo;
import org.jxmapviewer.viewer.TileListener;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;

import org.openstreetmap.GpsArrowIcon;
import org.openstreetmap.MapIconImpl;
import org.openstreetmap.MapMarkerCircle;
import org.openstreetmap.MapMarkerDot;
import org.openstreetmap.MapMarkerTriangle;
import org.openstreetmap.MapPolygonImpl;
import org.openstreetmap.MapPolylineImpl;
import org.openstreetmap.MapRectangleImpl;

import components.Layer;
import components.MapDimension;
import components.SurfaceLine;
import coverage.StaticMeasurement;
import coverage.TestTile;

import geometry.Coordinate;
import geometry.ICoordinate;
import map.AbstractMap;
import map.GpsArrow;
import map.MapIcon;
import map.MapMarker;
import map.MapPolygon;
import map.MapPolyline;
import map.MapRectangle;
import map.MapTriangle;

import radiolocation.ConicSection;
import radiolocation.HyperbolicProjection;
import utility.IconLoader;
import utility.Vincenty;

public class JXMapKit extends AbstractMap {
	private static final long serialVersionUID = 7398L;

	private static final long ZOOM_LEVEL_CONVERSION_FACTOR = 500;
	private static final Cursor DEFAULT_MAP_CURSOR = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
	private static final Dimension DEFAULT_PRINTER_PAGE_SIZE = new Dimension(1035, 800);
	private static final int GPS_SYMBOL_OPACITY = 80;
	private static final double GPS_DOT_SIZE_ADJUST_FACTOR = 1D;
	private static final double GPS_ARROW_SIZE_ADJUST_FACTOR = 4D;
	private static final double DEFAULT_GPS_SYMBOL_RADIUS = 5D;
	private static final boolean SLIDER_REVERSED = false;
	private static final boolean USE_ICON_FOR_ARROW = false;
	private static final String DEFAULT_FONT = "Calibri"; //$NON-NLS-1$
	private static final Color DEFAULT_GPS_SYMBOL_COLOR = Color.GRAY;
	private static final int DEFAULT_GPS_SYMBOL_ANGLE = 360;
	private static final String DEFAULT_ARROW_ICON = "/default_gps_arrow.png"; //$NON-NLS-1$

	private static final Logger LOG = Logger.getLogger(JXMapKit.class.getName());
	
	private JXMapViewer mainMap;
	private JXMapViewer miniMap;
	private JSlider zoomSlider;

	private Color[] arcColors;
	private double arcTraceRadius;
	private Color arcCursorColor;
	private Color arcTraceColor;
	private Color arcAsymptoteColor;

	private boolean showTestTiles;
	private boolean showQuads;
	private boolean showSignalMarkers;
	private boolean showFlightPathMarkers;
	private boolean showLines;
	private boolean showRings;
	private boolean showIcons;
	private boolean showGrid;
	private boolean showArcs;
	private boolean showArcTraces;
	private boolean traceEqualsFlightColor;
	private boolean showArcIntersectPoints;
	private boolean showArcAsymptotes;
	private boolean showArcCursors;
	private boolean showIconLabels;
	private boolean showGpsSymbol;
	private boolean showTargetRing;
	private boolean showMapImage = true;
	private boolean selectionRectangleMode;
	private boolean rulerMode;
	private boolean doArcComponentColorUpdate;
	private boolean scrollWrapEnabled;
	private boolean addressLocationShown = true;
	private boolean dataProviderCreditShown = true;
	private boolean zoomChanging;

	private double arcCursorRadius;
	private double arcIntersectPointSize;

	private Color arcIntersectPointColor;

	private transient Point2D gridReference;
	private transient Point2D tileSize;
	private transient Point2D gridSize;
	private transient Point2D mouseLonLat = new Point2D.Double(-181, -181);
	private transient Point2D mouseDragLonLat = new Point2D.Double(-181, -181);
	private transient Point2D mousePressOrigin;

	private transient MapMarkerCircle targetRing;
	private transient MapMarkerDot gpsDot;
	private transient MapRectangleImpl selectionRectangle;
	private transient GpsArrow gpsArrow;
	private transient MapPolylineImpl ruler;

	private final transient TileListener tileListener;
	
	private transient List<HyperbolicProjection> arcList;
	private transient List<MapMarkerTriangle> arcIntersectList;
	private transient List<MapPolylineImpl> lineList;
	private transient List<MapMarkerCircle> signalMarkerList;
	private transient List<MapMarkerCircle> flightPathMarkerList;
	private transient List<MapMarkerCircle> ringList;
	private transient List<MapRectangleImpl> quadList;
	private transient List<MapPolygon> testTileList;
	private transient List<MapRectangleImpl> gridLines;
	private transient List<MapIconImpl> iconList;
	
	private transient ScheduledExecutorService iconScheduler;
	private transient ScheduledExecutorService flashScheduler;
	
	private final transient AbstractPainter<JXMapViewer> dataProviderCreditPainter = new AbstractPainter<>(false) {
		@Override
		
		protected void doPaint(Graphics2D g, JXMapViewer map, int width, int height) {
			g.setPaint(Color.WHITE);
			g.drawString("data ", 50, map.getHeight() - 10); //$NON-NLS-1$
		}
	};

	private final transient WaypointPainter<Waypoint> addressLocationPainter = new WaypointPainter<>() {
		@Override
		public synchronized Set<Waypoint> getWaypoints() {
			final Set<Waypoint> set = new HashSet<>();
			if (getAddressLocation() != null) {
				set.add(new DefaultWaypoint(getAddressLocation()));
			} else {
				set.add(new DefaultWaypoint(0, 0));
			}
			return set;
		}
	};
	
	private transient Point2D gpsSymbolLonLat;
	private Color gpsSymbolColor = DEFAULT_GPS_SYMBOL_COLOR;
	private int gpsSymbolAngle = DEFAULT_GPS_SYMBOL_ANGLE;
	private double gpsSymbolRadius = DEFAULT_GPS_SYMBOL_RADIUS;

	private final transient Painter<JXMapViewer> painter;

	private Color gridColor = Color.RED;

	private transient MouseInputListener mia;
	private transient GeoPosition mapCenterPosition = new GeoPosition(0, 0);

	private final transient Point2D centerLonLat;
	private final transient TileFactoryInfo tileFactoryInfo;
	private transient List<ICoordinate> rulerCoords;
	private final long altitude;
	private File cacheDir;

	private transient DefaultTileFactory tileFactory;

	public JXMapKit(Point2D centerLonLat, Long altitude, Object tileFactoryInfo) {
		super(String.valueOf(serialVersionUID));

		this.tileListener = new TileListener() {
			@Override
			public void allTilesLoaded() {
				// NO OP
			}

			@Override
			public void tileLoaded(Tile tile) {
				// NO OP
			}

			@Override
			public void tilesLoading() {
				// NO OP
			}
		};

		this.painter = new Painter<>() {
			@Override
			public void paint(Graphics2D g, JXMapViewer map, int width, int height) {
				final Graphics2D g2d = (Graphics2D) g.create();

				try {
					if (JXMapKit.this.showQuads && JXMapKit.this.quadList != null && isDisplayShapes()) {
						JXMapKit.this.quadList.stream().filter(quad -> (quad.isVisible()))
								.forEachOrdered(quad -> paintRectangle(g2d, quad));
					}

					if (JXMapKit.this.showIcons && JXMapKit.this.iconList != null && isDisplayShapes()) {
						JXMapKit.this.iconList.stream().filter(icon -> (icon.isVisible()))
								.forEachOrdered(icon -> paintIcon(g2d, icon));
					}

					if (JXMapKit.this.selectionRectangleMode && JXMapKit.this.selectionRectangle != null) {
						paintRectangle(g2d, JXMapKit.this.selectionRectangle);
					}

					if (JXMapKit.this.rulerMode && JXMapKit.this.ruler != null) {
						paintPolyline(g2d, JXMapKit.this.ruler);
					}

					if (JXMapKit.this.showTestTiles && JXMapKit.this.testTileList != null && isDisplayShapes()) {
						JXMapKit.this.testTileList.stream().filter(testTile -> (testTile.isVisible()))
								.forEachOrdered(testTile -> paintPolygon(g2d, testTile));
					}

					if (JXMapKit.this.showGrid && JXMapKit.this.gridLines != null && JXMapKit.this.tileSize != null
							&& isDisplayShapes()) {
						JXMapKit.this.gridLines.forEach(gridLine -> paintRectangle(g2d, gridLine));
					}

					if (JXMapKit.this.showRings && JXMapKit.this.ringList != null && isDisplayShapes()) {
						JXMapKit.this.ringList.stream().filter(marker -> (marker.isVisible()))
								.forEachOrdered(marker -> paintMarker(g2d, marker));
					}

					if (JXMapKit.this.showSignalMarkers && JXMapKit.this.signalMarkerList != null
							&& isDisplayShapes()) {
						JXMapKit.this.signalMarkerList.stream().filter(marker -> (marker.isVisible()))
								.forEachOrdered(marker -> paintMarker(g2d, marker));
					}

					if (JXMapKit.this.showFlightPathMarkers && JXMapKit.this.flightPathMarkerList != null
							&& isDisplayShapes()) {
						JXMapKit.this.flightPathMarkerList.stream().filter(marker -> (marker.isVisible()))
								.forEachOrdered(marker -> paintMarker(g2d, marker));
					}

					if (JXMapKit.this.showArcs && JXMapKit.this.arcList != null && isDisplayShapes()) {
						updateArcComponentColors();
						final List<Integer> displayedList = new CopyOnWriteArrayList<>();
						for (int i = JXMapKit.this.arcList.size() - 1; i >= 0; i--) {
							if (!displayedList.contains(JXMapKit.this.arcList.get(i).getSMB().getFlight())) {
								if (JXMapKit.this.arcList.get(i).showArc()) {
									paintPolyline(g2d, JXMapKit.this.arcList.get(i).getArcPolyline());
								}
								if (JXMapKit.this.arcList.get(i).showAsymptote()) {
									paintPolyline(g2d, JXMapKit.this.arcList.get(i).getAsymptotePolyline());
								}
								if (JXMapKit.this.arcList.get(i).showCursors()) {
									paintMarkers(g2d, JXMapKit.this.arcList.get(i).getCursorMarkers());
								}
								displayedList.add(JXMapKit.this.arcList.get(i).getSMB().getFlight());
							}
						}
						JXMapKit.this.arcList.stream().filter(arc -> (arc.showTrace())).map(arc -> {
							paintPolyline(g2d, arc.getTracePolyline());
							return arc;
						}).forEachOrdered(arc -> paintMarkers(g2d, arc.getTraceMarkers()));
					}

					if (JXMapKit.this.showArcIntersectPoints && JXMapKit.this.arcIntersectList != null && isDisplayShapes()) {
						paintTriangles(g2d, JXMapKit.this.arcIntersectList);
					}

					if (JXMapKit.this.showGpsSymbol && JXMapKit.this.gpsDot != null && isDisplayShapes()) {
						if (JXMapKit.this.gpsDot.isVisible()) {
							paintMarker(g2d, JXMapKit.this.gpsDot);
						}
						if (JXMapKit.this.gpsArrow.isVisible()) {
							if (USE_ICON_FOR_ARROW) {
								paintIcon(g2d, (MapIcon) JXMapKit.this.gpsArrow);
							} else {
								paintPolyline(g2d, (MapPolyline) JXMapKit.this.gpsArrow);
							}
						}
					}

					if (JXMapKit.this.showTargetRing && JXMapKit.this.targetRing != null && isDisplayShapes()
							&& JXMapKit.this.targetRing.isVisible()) {
						paintMarker(g2d, JXMapKit.this.targetRing);
					}

					if (JXMapKit.this.showLines && JXMapKit.this.lineList != null && isDisplayShapes()) {
						JXMapKit.this.lineList.stream().filter(polyline -> (polyline.isVisible()))
								.forEachOrdered(polyline -> paintPolyline(g2d, polyline));
					}
					
				} catch (ConcurrentModificationException ex) {
					LOG.log(Level.WARNING, ex.getLocalizedMessage(), ex);
				} finally {
					g2d.dispose();
				}
			}

			private void updateArcComponentColors() {
				if (JXMapKit.this.doArcComponentColorUpdate) {
					for (int i = 0; i < JXMapKit.this.arcColors.length; i++) {
						for (int a = 0; a < JXMapKit.this.arcList.size(); a++) {
							if (JXMapKit.this.arcList.get(a).getSMB().getFlight() == i) {
								if (JXMapKit.this.traceEqualsFlightColor) {
									JXMapKit.this.arcList.get(a).setTraceColor(JXMapKit.this.arcColors[i]);
								} else {
									JXMapKit.this.arcList.get(a).setTraceColor(JXMapKit.this.arcTraceColor);
								}
								JXMapKit.this.arcList.get(a).setArcColor(JXMapKit.this.arcColors[i]);
							}
						}
					}
					for (int a = 0; a < JXMapKit.this.arcList.size(); a++) {
						JXMapKit.this.arcList.get(a).setAsymptoteColor(JXMapKit.this.arcAsymptoteColor);
						JXMapKit.this.arcList.get(a).setCursorColor(JXMapKit.this.arcCursorColor);
					}
					JXMapKit.this.doArcComponentColorUpdate = false;
				}
			}

			private void paintMarkers(Graphics2D g, List<MapMarkerCircle> markers) {
				markers.forEach(marker -> paintMarker(g, marker));
			}

			private void paintTriangles(Graphics2D g, List<MapMarkerTriangle> triangles) {
				triangles.forEach(triangle -> paintTriangle(g, triangle));
			}
		};
		this.centerLonLat = centerLonLat;
		this.altitude = altitude;
		this.tileFactoryInfo = (TileFactoryInfo) tileFactoryInfo;
	}

	@Override
	public void initialize() {
		initComponents();

		setDataProviderCreditShown(false);
		setAddressLocationShown(false);

		tileFactory = new DefaultTileFactory(tileFactoryInfo);

		// Setup local file cache
		cacheDir = new File(System.getProperty("user.home") + File.separator + ".jxmapviewer2"); //$NON-NLS-1$ //$NON-NLS-2$
		tileFactory.setLocalCache(new FileBasedLocalCache(cacheDir, false));
		tileFactory.setTileCache(new TileCache());
		new CachePreloader(cacheDir, tileFactory.getTileCache());

		setTileFactory(tileFactory);

		mainMap.getTileFactory().addTileListener(tileListener);

		mainMap.setCenterPosition(new GeoPosition(centerLonLat.getY(), centerLonLat.getX()));
		miniMap.setCenterPosition(new GeoPosition(centerLonLat.getY(), centerLonLat.getX()));
		mainMap.setRestrictOutsidePanning(true);
		miniMap.setRestrictOutsidePanning(true);

		rebuildMainMapOverlay();

		mainMap.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseClicked(MouseEvent e) {
				// NO OP
			}

			@Override
			public void mousePressed(MouseEvent e) {
				mousePressOrigin = JXMapKit.this.mainMap.convertPointToGeoPosition(e.getPoint()).getLonLat();
			}
			
			@Override
			public void mouseReleased(MouseEvent e) {
				// NO OP
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				// NO OP
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				// NO OP
			}
			
		});

		mainMap.addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseMoved(MouseEvent e) {
				JXMapKit.this.mouseLonLat = JXMapKit.this.mainMap.convertPointToGeoPosition(e.getPoint()).getLonLat();
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				// TODO: add filter to prevent small drags from firing
				JXMapKit.this.mouseDragLonLat = JXMapKit.this.mainMap.convertPointToGeoPosition(e.getPoint()).getLonLat();
				if (rulerMode && mousePressOrigin != null) {
					setRulerEnds(mousePressOrigin, mouseDragLonLat);
					getPropertyChangeSupport().firePropertyChange(MapEvent.MEASURE_TOOL_SOLUTION.name(), null,
							new SurfaceLine(mousePressOrigin, mouseDragLonLat));
				}
			}
		});

		mainMap.addPropertyChangeListener(event -> {
			if ("center".equals(event.getPropertyName())) {
				final Point2D mapCenter = (Point2D) event.getNewValue();
				final TileFactory tf = mainMap.getTileFactory();
				final GeoPosition mapPos = tf.pixelToGeo(mapCenter, mainMap.getZoom());
				miniMap.setCenterPosition(mapPos);
			}
			if ("centerPosition".equals(event.getPropertyName())) {
				mapCenterPosition = (GeoPosition) event.getNewValue();
				miniMap.setCenterPosition(mapCenterPosition);
				final Point2D pt = miniMap.getTileFactory().geoToPixel(mapCenterPosition, miniMap.getZoom());
				miniMap.setCenter(pt);
				miniMap.repaint();
			}

		});

		// Add interactions
		mia = new PanMouseInputListener(mainMap);
		mainMap.addMouseListener(mia);
		mainMap.addMouseMotionListener(mia);

		mainMap.addMouseListener(new CenterMapListener(mainMap));

		mainMap.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mainMap));

		mainMap.addPropertyChangeListener("zoom", _ -> { //$NON-NLS-1$
			zoomSlider.setValue(mainMap.getZoom());
			miniMap.setZoom(mainMap.getZoom() + 4);
			miniMap.repaint();
		});

		miniMap.setOverlayPainter((g, map, _, _) -> {
			// get the viewport rectangle of the main map
			final Rectangle mainMapBounds = mainMap.getViewportBounds();
			
			// convert to Point2Ds
			Point2D upperLeft2D = mainMapBounds.getLocation();
			Point2D lowerRight2D = new Point.Double(upperLeft2D.getX() + mainMapBounds.getWidth(),
					upperLeft2D.getY() + mainMapBounds.getHeight());
			
			// convert to GeoPostions
			final GeoPosition upperLeft = mainMap.getTileFactory().pixelToGeo(upperLeft2D, mainMap.getZoom());
			final GeoPosition lowerRight = mainMap.getTileFactory().pixelToGeo(lowerRight2D, mainMap.getZoom());
			
			// convert to Point2Ds on the mini-map
			upperLeft2D = map.getTileFactory().geoToPixel(upperLeft, map.getZoom());
			lowerRight2D = map.getTileFactory().geoToPixel(lowerRight, map.getZoom());
			
			final Graphics2D g2d = (Graphics2D) g.create();
			final Rectangle rect = map.getViewportBounds();
			g2d.translate(-rect.getX(), -rect.getY());
			g2d.setPaint(Color.RED);
			g2d.drawRect((int) upperLeft2D.getX(), (int) upperLeft2D.getY(),
					(int) (lowerRight2D.getX() - upperLeft2D.getX()), (int) (lowerRight2D.getY() - upperLeft2D.getY()));
			g2d.setPaint(new Color(255, 0, 0, 50));
			g2d.fillRect((int) upperLeft2D.getX(), (int) upperLeft2D.getY(),
					(int) (lowerRight2D.getX() - upperLeft2D.getX()), (int) (lowerRight2D.getY() - upperLeft2D.getY()));
			g2d.dispose();
		});

		setZoom(altitudeToZoom(altitude));

		iconScheduler = Executors.newSingleThreadScheduledExecutor();
			iconScheduler.scheduleAtFixedRate(new IconSchedule(), 5, 5, TimeUnit.SECONDS);
		
		flashScheduler = Executors.newSingleThreadScheduledExecutor();
		flashScheduler.scheduleAtFixedRate(new FlashScheduler(), 0, 250, TimeUnit.MILLISECONDS);

		getPropertyChangeSupport().firePropertyChange(MapEvent.MAP_PAINTED.name(), null, true);
	}

	private class FlashScheduler implements Runnable {
		@Override
		public synchronized void run() {
			testTileList.forEach(testTile -> {
				final Color fc = testTile.getStyle().getColor();
				if (testTile.isFlash()) {
					// Uses the color instance variable to remember what the original backColor was.
					// The color variable is not used when painting the tile because text is not
					// used, so we can get away with using it to remember the Alpha value, in particular.
					if (testTile.getStyle().getBackColor().getAlpha() > 0) {
						testTile.getStyle().setBackColor(new Color(fc.getRed(), fc.getGreen(), fc.getBlue(), 0));
					} else {
						testTile.getStyle().setBackColor(new Color(fc.getRed(), fc.getGreen(), fc.getBlue(), fc.getAlpha()));
					}
				} else {
					testTile.getStyle().setBackColor(new Color(fc.getRed(), fc.getGreen(), fc.getBlue(), fc.getAlpha()));
				}
			});
			JXMapKit.this.mainMap.repaint();
		}
	}

	private void setZoom(int zoom) {
		zoomChanging = true;
		mainMap.setZoom(zoom);
		mainMap.repaint();

		getPropertyChangeSupport().firePropertyChange(MapEvent.VIEWING_ALTITUDE_CHANGE.name(), null,
				(long) (Vincenty.FEET_PER_METER
						* zoomToAltitude(tileFactoryInfo.getMaximumZoomLevel() - mainMap.getZoom())));

		if (SLIDER_REVERSED) {
			zoomSlider.setValue(zoomSlider.getMaximum() - zoom);
		} else {
			zoomSlider.setValue(zoom);
		}
		zoomChanging = false;
	}

	/**
	 * Returns an action which can be attached to buttons or menu items to make the
	 * map zoom out
	 *
	 * @return a preconfigured Zoom Out action
	 */
	private Action getZoomOutAction() {
		return new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				setZoom(JXMapKit.this.mainMap.getZoom() - 1);
			}
		};
	}

	/**
	 * Returns an action which can be attached to buttons or menu items to make the
	 * map zoom in
	 *
	 * @return a preconfigured Zoom In action
	 */
	private Action getZoomInAction() {
		return new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				setZoom(JXMapKit.this.mainMap.getZoom() + 1);
			}
		};
	}

	private void initComponents() {
		GridBagConstraints gridBagConstraints;
		final JPanel jPanel = new JPanel();

		mainMap = new JXMapViewer();
		miniMap = new JXMapViewer();

		final JButton zoomInButton = new JButton();
		final JButton zoomOutButton = new JButton();

		zoomSlider = new JSlider();

		setOpaque(true);
		setBackground(Color.BLACK);
		setFocusable(true);
		setFocusTraversalKeysEnabled(true);
		setCursor(DEFAULT_MAP_CURSOR);
		setIgnoreRepaint(true);
		setLayout(new GridBagLayout());

		mainMap.setLayout(new GridBagLayout());

		miniMap.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
		miniMap.setMinimumSize(new Dimension(100, 100));
		miniMap.setPreferredSize(new Dimension(100, 100));
		miniMap.setLayout(new GridBagLayout());

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = GridBagConstraints.SOUTHEAST;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;

		mainMap.add(miniMap, gridBagConstraints);

		jPanel.setOpaque(false);
		jPanel.setLayout(new GridBagLayout());

		zoomInButton.setAction(getZoomInAction());
		zoomInButton.setIcon(new ImageIcon(getClass().getResource("/minus.png"))); //$NON-NLS-1$
		zoomInButton.setMargin(new Insets(2, 2, 2, 2));
		zoomInButton.setMaximumSize(new Dimension(20, 20));
		zoomInButton.setMinimumSize(new Dimension(20, 20));
		zoomInButton.setOpaque(false);
		zoomInButton.setPreferredSize(new Dimension(20, 20));

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.anchor = GridBagConstraints.NORTHEAST;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;

		jPanel.add(zoomInButton, gridBagConstraints);

		zoomOutButton.setAction(getZoomOutAction());
		zoomOutButton.setIcon(new ImageIcon(getClass().getResource("/plus.png"))); //$NON-NLS-1$
		zoomOutButton.setMargin(new Insets(2, 2, 2, 2));
		zoomOutButton.setMaximumSize(new Dimension(20, 20));
		zoomOutButton.setMinimumSize(new Dimension(20, 20));
		zoomOutButton.setOpaque(false);
		zoomOutButton.setPreferredSize(new Dimension(20, 20));

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
		gridBagConstraints.weighty = 1.0;

		jPanel.add(zoomOutButton, gridBagConstraints);

		zoomSlider.setOpaque(false);
		zoomSlider.setMajorTickSpacing(1);
		zoomSlider.setMaximum(15);
		zoomSlider.setMinimum(10);
		zoomSlider.setMinorTickSpacing(1);
		zoomSlider.setOrientation(SwingConstants.VERTICAL);
		zoomSlider.setPaintTicks(true);
		zoomSlider.setSnapToTicks(true);
		zoomSlider.setMinimumSize(new Dimension(35, 100));
		zoomSlider.setPreferredSize(new Dimension(35, 190));
		zoomSlider.addChangeListener(this::zoomSliderStateChanged);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.fill = GridBagConstraints.VERTICAL;
		gridBagConstraints.anchor = GridBagConstraints.NORTH;

		jPanel.add(zoomSlider, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = GridBagConstraints.SOUTHWEST;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.insets = new Insets(4, 4, 4, 4);

		mainMap.add(jPanel, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;

		final Style selectionRectangleStyle = new Style(Color.RED, Color.RED, new BasicStroke(),
				new Font(DEFAULT_FONT, Font.BOLD, 12));

		JXMapKit.this.selectionRectangle = new MapRectangleImpl(null, null, new Coordinate(getCenterLonLat()),
				new Coordinate(getCenterLonLat()), selectionRectangleStyle);

		JXMapKit.this.rulerCoords = Collections.synchronizedList(new LinkedList<>());

		final Style rulerStyle = new Style(Color.RED, Color.RED, new BasicStroke(2), new Font(DEFAULT_FONT, Font.BOLD, 12));

		JXMapKit.this.ruler = new MapPolylineImpl(null, null, JXMapKit.this.rulerCoords, rulerStyle);

		final Style gpsDotStyle = new Style(new Color(255, 0, 0, 128), new Color(255, 0, 0, GPS_SYMBOL_OPACITY),
				new BasicStroke(), null);

		JXMapKit.this.gpsDot = new MapMarkerDot(new Coordinate(getCenterLonLat()), DEFAULT_GPS_SYMBOL_RADIUS,
				gpsDotStyle);

		if (USE_ICON_FOR_ARROW) {
			BufferedImage arrowImage = IconLoader.getDefaultIcon(new Dimension(16, 16));
			try {
				arrowImage = ImageIO.read(getClass().getResourceAsStream(DEFAULT_ARROW_ICON));
			} catch (IOException ex) {
				LOG.log(Level.WARNING, ex.getMessage(), ex);
			}
			JXMapKit.this.gpsArrow = new GpsArrowIcon(arrowImage, null, new Coordinate(getCenterLonLat()),
					new Dimension(16, 16), null);

		} else {
			JXMapKit.this.gpsArrow = new GpsArrowPolyline(null, null, new Coordinate(getCenterLonLat()).getLonLat(), 0,
					8.0, new Style(Color.GREEN, new Color(0, 255, 0, GPS_SYMBOL_OPACITY),
							new BasicStroke(3F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER), null));
		}

		final Style targetRingStyle = new Style(new Color(255, 0, 0, 128), new Color(255, 0, 0, 16), new BasicStroke(),
				null);

		JXMapKit.this.targetRing = new MapMarkerCircle(null, null, new Coordinate(getCenterLonLat()), 0,
				MapMarker.STYLE.ARC_SECONDS, targetRingStyle);

		JXMapKit.this.arcList = Collections.synchronizedList(new LinkedList<>());
		JXMapKit.this.arcIntersectList = Collections.synchronizedList(new LinkedList<>());
		JXMapKit.this.signalMarkerList = Collections.synchronizedList(new LinkedList<>());
		JXMapKit.this.ringList = Collections.synchronizedList(new LinkedList<>());
		JXMapKit.this.quadList = Collections.synchronizedList(new LinkedList<>());
		JXMapKit.this.testTileList = Collections.synchronizedList(new LinkedList<>());
		JXMapKit.this.gridLines = Collections.synchronizedList(new LinkedList<>());
		JXMapKit.this.lineList = Collections.synchronizedList(new LinkedList<>());
		JXMapKit.this.iconList = Collections.synchronizedList(new LinkedList<>());

		add(mainMap, gridBagConstraints);
	}

	private void zoomSliderStateChanged(ChangeEvent evt) {
		final JSlider s = (JSlider) evt.getSource();
		if (!zoomChanging) {
			setZoom(s.getValue());
		}
	}

	/**
	 * Sets the tile factory for both embedded JXMapViewer components. Calling this
	 * method will also reset the center and zoom levels of both maps, as well as
	 * the bounds of the zoom slider.
	 *
	 * @param fact the new TileFactory
	 */
	private void setTileFactory(TileFactory fact) {
		mainMap.setTileFactory(fact);
		mainMap.setZoom(fact.getInfo().getDefaultZoomLevel());
		mainMap.setCenterPosition(new GeoPosition(0, 0));
		miniMap.setTileFactory(fact);
		miniMap.setZoom(fact.getInfo().getDefaultZoomLevel() + 3);
		miniMap.setCenterPosition(new GeoPosition(0, 0));
		zoomSlider.setMinimum(fact.getInfo().getMinimumZoomLevel());
		zoomSlider.setMaximum(fact.getInfo().getMaximumZoomLevel());
		mainMap.repaint();
		miniMap.repaint();
	}

	/**
	 * @return the address location
	 */
	protected GeoPosition getAddressLocation() {
		return mainMap.getAddressLocation();
	}

	/**
	 * @param b the visibility flag
	 */
	private void setAddressLocationShown(boolean b) {
		final boolean old = isAddressLocationShown();
		addressLocationShown = b;
		addressLocationPainter.setVisible(b);
		getPropertyChangeSupport().firePropertyChange("addressLocationShown", old, b); //$NON-NLS-1$
		mainMap.repaint();
	}

	/**
	 * @return true if the address location is shown
	 */
	private boolean isAddressLocationShown() {
		return addressLocationShown;
	}

	/**
	 * @param b the visibility flag
	 */
	private void setDataProviderCreditShown(boolean b) {
		final boolean old = isDataProviderCreditShown();
		dataProviderCreditShown = b;
		dataProviderCreditPainter.setVisible(b);
		mainMap.repaint();
		getPropertyChangeSupport().firePropertyChange("dataProviderCreditShown", old, b); //$NON-NLS-1$
	}

	/**
	 * @return true if the data provider credit is shown
	 */
	private boolean isDataProviderCreditShown() {
		return dataProviderCreditShown;
	}

	@SuppressWarnings("unchecked")
	private void rebuildMainMapOverlay() {
		final CompoundPainter<JXMapViewer> cp = new CompoundPainter<>();
		cp.setCacheable(false);
		cp.setPainters(dataProviderCreditPainter, addressLocationPainter, painter);
		mainMap.setOverlayPainter(cp);
	}

	@Override
	public void setDisplayShapes(boolean displayShapes) {
		super.setDisplayShapes(displayShapes);

		if (mainMap == null) {
			return;
		}

		gpsDot.setVisible(displayShapes && showGpsSymbol);
		gpsArrow.setVisible(displayShapes && showGpsSymbol);
		targetRing.setVisible(displayShapes && showTargetRing);

		mainMap.repaint();
	}

	@Override
	public Point2D getCenterLonLat() {
		return mainMap.getCenterPosition().getLonLat();
	}

	@Override
	public void setCenterLonLat(Point2D point) {
		mainMap.setCenterPosition(new GeoPosition(point));
		miniMap.setCenterPosition(new GeoPosition(point));
		mainMap.repaint();
	}

	@Override
	public Point2D getMouseCoordinates() {
		return mouseLonLat == null ? new Point2D.Double(-181, -181) : mouseLonLat;
	}

	@Override
	public Point2D getMouseDragCoordinates() {
		return mouseDragLonLat == null ? new Point2D.Double(-181, -181) : mouseDragLonLat;
	}

	@Override
	public void setTileSize(Point2D tileSize) {
		this.tileSize = tileSize;
		if (showGrid && tileSize != null && gridReference != null && gridSize != null
				&& gridColor != null) {
			gridLines.clear();
			gridLines.addAll(buildGrid(tileSize, gridReference, gridSize, gridColor));
		}
		mainMap.repaint();
	}

	@Override
	public void showMapImage(boolean showMapImage) {
		this.showMapImage = showMapImage;
		mainMap.repaint();
	}

	@Override
	public void showGrid(boolean showGrid) {
		this.showGrid = showGrid;
		if (!showGrid) {
			gridLines.clear();
		}
		if (showGrid && tileSize != null && gridReference != null && gridSize != null
				&& gridColor != null) {
			gridLines.clear();
			gridLines.addAll(buildGrid(tileSize, gridReference, gridSize, gridColor));
		}
		mainMap.repaint();
	}

	@Override
	public boolean isShowTargetRing() {
		return targetRing.isVisible();
	}

	@Override
	public boolean isShowGrid() {
		return showGrid;
	}

	@Override
	public boolean isShowMapImage() {
		return showMapImage;
	}

	@Override
	public boolean isShowGPSDot() {
		return gpsDot.isVisible();
	}

	@Override
	public boolean isShowGPSArrow() {
		return gpsArrow.isVisible();
	}

	@Override
	public void deleteAllArcIntersectPoints() {
		if (arcIntersectList == null) {
			return;
		}
		arcIntersectList.clear();
		mainMap.repaint();
	}

	@Override
	public void showGpsSymbol(boolean showGpsSymbol) {
		if (this.showGpsSymbol != showGpsSymbol) {
			this.showGpsSymbol = showGpsSymbol;
			mainMap.repaint();
		}
	}

	@Override
	public Color getGpsSymbolColor() {
		return gpsSymbolColor;
	}

	@Override
	public void setGpsSymbolColor(Color gpsSymbolColor) {
		this.gpsSymbolColor = gpsSymbolColor;
		setGpsSymbol(gpsSymbolLonLat, gpsSymbolRadius, gpsSymbolColor, gpsSymbolAngle);
	}

	@Override
	public Point2D getGpsSymbolLonLat() {
		return gpsSymbolLonLat;
	}

	@Override
	public void setGpsSymbolLonLat(Point2D gpsSymbolLonLat) {
		this.gpsSymbolLonLat = gpsSymbolLonLat;
		setGpsSymbol(gpsSymbolLonLat, gpsSymbolRadius, gpsSymbolColor, gpsSymbolAngle);
	}

	@Override
	public int getGpsSymbolAngle() {
		return gpsSymbolAngle;
	}

	@Override
	public void setGpsSymbolAngle(int gpsSymbolAngle) {
		this.gpsSymbolAngle = gpsSymbolAngle;
		setGpsSymbol(gpsSymbolLonLat, gpsSymbolRadius, gpsSymbolColor, gpsSymbolAngle);
	}

	@Override
	public double getGpsSymbolRadius() {
		return gpsSymbolRadius;
	}

	@Override
	public void setGpsSymbolRadius(double gpsSymbolRadius) {
		this.gpsSymbolRadius = gpsSymbolRadius;
		setGpsSymbol(gpsSymbolLonLat, gpsSymbolRadius, gpsSymbolColor, gpsSymbolAngle);
	}

	@Override
	public void setGpsSymbol(Point2D point, double radius, Color color, int angle) {
		gpsSymbolLonLat = point;
		gpsSymbolRadius = radius;
		gpsSymbolColor = color;
		gpsSymbolAngle = angle;

		if (point == null) {
			return;
		}

		if (angle == 360) {
			gpsDot.setVisible(true);
			gpsDot.setLon(point.getX());
			gpsDot.setLat(point.getY());
			gpsDot.setRadius(radius * GPS_DOT_SIZE_ADJUST_FACTOR);
			gpsDot.setColor(color);
			gpsDot.setBackColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), GPS_SYMBOL_OPACITY));
			gpsArrow.setVisible(false);
		} else {
			if (USE_ICON_FOR_ARROW) {
				gpsArrow.setAngle(angle);
			} else {
				gpsArrow = new GpsArrowPolyline(null, null, point, angle, radius,
						new Style(color,
								new Color(color.getRed(), color.getGreen(), color.getBlue(), GPS_SYMBOL_OPACITY),
								new BasicStroke(3F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER), null));
			}

			gpsArrow.setVisible(true);
			gpsDot.setVisible(false);
		}
		mainMap.repaint();
	}

	@Override
	public void showTargetRing(boolean showTargetRing) {
		this.showTargetRing = showTargetRing;
		if (targetRing.isVisible() != showTargetRing) {
			targetRing.setVisible(showTargetRing && isDisplayShapes());
		}
		mainMap.repaint();
	}

	@Override
	public void setGpsDotRadius(double radius) {
		gpsDot.setRadius(radius * GPS_DOT_SIZE_ADJUST_FACTOR);
		mainMap.repaint();
	}

	@Override
	public void setGpsDotColor(Color color) {
		gpsDot.setColor(color);
		gpsDot.setBackColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), GPS_SYMBOL_OPACITY));
		mainMap.repaint();
	}

	@Override
	public void setTargetRingPosition(Point2D point) {
		targetRing.setLon(point.getX());
		targetRing.setLat(point.getY());
		mainMap.repaint();
	}

	@Override
	public void setTargetRingRadius(double radius) {
		targetRing.setRadius(radius);
		mainMap.repaint();
	}

	@Override
	public void setTargetRingColor(Color color) {
		targetRing.setColor(color);
		mainMap.repaint();
	}

	@Override
	public void setTargetRing(Point2D point, double radius) {
		targetRing.setLon(point.getX());
		targetRing.setLat(point.getY());
		targetRing.setRadius(radius);
		mainMap.repaint();
	}

	@Override
	public void setTargetRing(Point2D point, double radius, Color color) {
		targetRing.setLon(point.getX());
		targetRing.setLat(point.getY());
		targetRing.setRadius(radius);
		targetRing.setColor(color);
		mainMap.repaint();
	}

	@Override
	public void addArcIntersectPoint(Point2D point, double width, Color color) {
		final Style style = new Style(color, color, new BasicStroke(), null);
		final MapMarkerTriangle mmt = new MapMarkerTriangle(null, null, new Coordinate(point), width,
				MapTriangle.STYLE.FIXED, style);
		arcIntersectList.add(mmt);
		mainMap.repaint();
	}

	@Override
	public void addLine(Point2D p1, double angle, double distance, Color color) {
		final Point2D p2 = Vincenty.getVincentyDirect(p1, angle, distance).getDestinationPoint();
		addLine(p1, p2, color);
	}

	@Override
	public void addLine(Point2D p1, Point2D p2, Color color) {
		final List<ICoordinate> coords = new CopyOnWriteArrayList<>();
		coords.add(new Coordinate(p1));
		coords.add(new Coordinate(p2));
		final Style style = new Style(color, color, new BasicStroke(), null);
		final MapPolylineImpl line = new MapPolylineImpl(null, null, coords, style);
		lineList.add(line);
	}

	@Override
	public void addArc(ConicSection cone) {
		addArc(cone.getSMA(), cone.getSMB(), cone.getSMB().getFlight());
	}

	private void addArc(StaticMeasurement sma, StaticMeasurement smb, int unit) {
		final Color traceColor;
		final Color arcColor;
		if (traceEqualsFlightColor) {
			traceColor = arcColors[unit];
			arcColor = arcColors[unit];
		} else {
			traceColor = arcTraceColor;
			arcColor = arcTraceColor;
		}
		final HyperbolicProjection hyperbola = new HyperbolicProjection(sma, smb, unit, showArcs, arcColor,
				showArcAsymptotes, arcAsymptoteColor, showArcCursors, arcCursorColor,
				arcCursorRadius, showArcTraces, traceColor, arcTraceRadius);
		arcList.add(hyperbola);
	}

	@Override
	public void setArcColors(Color[] arcColors) {
		this.arcColors = arcColors.clone();
		doArcComponentColorUpdate = true;
		mainMap.repaint();
	}

	@Override
	public void setTraceEqualsFlightColor(boolean traceEqualsFlightColor) {
		this.traceEqualsFlightColor = traceEqualsFlightColor;
		doArcComponentColorUpdate = true;
		mainMap.repaint();
	}

	@Override
	public Point2D getUpperLeftLonLat() {
		return mainMap.convertPointToGeoPosition(new Point2D.Double(0, 0)).getLonLat();
	}

	@Override
	public Point2D getLowerRightLonLat() {
		return mainMap
				.convertPointToGeoPosition(new Point2D.Double(mainMap.getWidth(), mainMap.getHeight()))
				.getLonLat();
	}

	@Override
	public void setArcAsymptoteColor(Color arcAsymptoteColor) {
		this.arcAsymptoteColor = arcAsymptoteColor;
		doArcComponentColorUpdate = true;
		mainMap.repaint();
	}

	@Override
	public void setArcCursorColor(Color arcCursorColor) {
		this.arcCursorColor = arcCursorColor;
		doArcComponentColorUpdate = true;
		mainMap.repaint();
	}

	@Override
	public void setArcTraceColor(Color arcTraceColor) {
		this.arcTraceColor = arcTraceColor;
		doArcComponentColorUpdate = true;
		mainMap.repaint();
	}

	@Override
	public void setArcIntersectPointColor(Color arcIntersectPointColor) {
		this.arcIntersectPointColor = arcIntersectPointColor;
		arcIntersectList.stream().map(triangle -> {
			triangle.setColor(arcIntersectPointColor);
			return triangle;
		}).forEachOrdered(triangle -> triangle.setBackColor(arcIntersectPointColor));
		mainMap.repaint();
	}

	@Override
	public void removeArc(int index) {
		try {
			arcList.remove(index);
			mainMap.repaint();
		} catch (final IndexOutOfBoundsException ex) {
			LOG.log(Level.WARNING, ex.getMessage(), ex);
		}
	}

	@Override
	public void addIcon(Point2D point, BufferedImage image, String iconName, Dimension iconSize, long timeToDelete,
			long timeToGoStale) {
		final MapIconImpl icon = new MapIconImpl(image, iconName, new Coordinate(point), iconSize);
		icon.setTimeToDelete(timeToDelete);
		icon.setTimeToGoStale(timeToGoStale);
		iconList.add(icon);
		mainMap.repaint();
	}

	@Override
	public void addIcon(Point2D point, String iconName, Dimension iconSize, long timeToDelete, long timeToGoStale) {
		final BufferedImage image = IconLoader.getDefaultIcon(new Dimension(16, 16));
		try (InputStream is = getClass().getResourceAsStream(iconName)) {
			new IconLoader(image, is);
			final MapIconImpl icon = new MapIconImpl(image, iconName, new Coordinate(point), iconSize);
			icon.setTimeToGoStale(timeToGoStale);
			icon.setTimeToDelete(timeToDelete);
			iconList.add(icon);
			mainMap.repaint();
		} catch (IOException ex) {
			LOG.log(Level.WARNING, ex.getMessage(), ex);
		}
	}

	@Override
	public boolean moveIcon(String name, Point2D point, long timeToDelete, long timeToGoStale) {
		final Iterator<MapIconImpl> iterator = iconList.iterator();
		while (iterator.hasNext()) {
			final MapIconImpl icon = iterator.next();
			if (icon.getName().contentEquals(name)) {
				icon.setLonLat(new Coordinate(point));
				icon.setTimeToDelete(timeToDelete);
				icon.setTimeToGoStale(timeToGoStale);
				mainMap.repaint();
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean moveIcon(int id, Point2D point, long timeToDelete, long timeToGoStale) {
		final Iterator<MapIconImpl> iterator = iconList.iterator();
		while (iterator.hasNext()) {
			final MapIconImpl icon = iterator.next();
			if (icon.getID() == id) {
				icon.setLonLat(new Coordinate(point));
				icon.setTimeToDelete(timeToDelete);
				icon.setTimeToGoStale(timeToGoStale);
				mainMap.repaint();
				return true;
			}
		}
		return false;
	}

	@Override
	public void addRing(Point2D coord, double radius, Color color) {
		final Style style = new Style(color, new Color(0, 0, 0, 0), new BasicStroke(),
				new Font(DEFAULT_FONT, Font.BOLD, 12));
		final MapMarkerCircle ring = new MapMarkerCircle(null, null, new Coordinate(coord), radius,
				MapMarker.STYLE.FIXED, style);
		ringList.add(ring);
		mainMap.repaint();
	}

	@Override
	public void addSignalMarker(Point2D coord, Color color) {
		addSignalMarker(coord, 2, color);
	}

	@Override
	public void addSignalMarker(Point2D point, double signalMarkerRadius, Color color) {
		final Style style = new Style(color, color, new BasicStroke(), null);
		final MapMarkerCircle signalMarker = new MapMarkerCircle(null, null, new Coordinate(point), signalMarkerRadius,
				MapMarker.STYLE.FIXED, style);
		signalMarkerList.add(signalMarker);
		mainMap.repaint();
	}

	@Override
	public void deleteAllQuads() {
		if (quadList == null) {
			return;
		}
		quadList.clear();
		mainMap.repaint();
	}

	@Override
	public void addQuad(Point2D point, Point2D size, Color color) {
		final Style style = new Style(color, color, new BasicStroke(), new Font(DEFAULT_FONT, Font.BOLD, 12));
		final MapRectangleImpl quad = new MapRectangleImpl(null, null, new Coordinate(point),
				new Coordinate(point.getY() - size.getY(), point.getX() + size.getX()), style);
		quadList.add(quad);
	}

	@Override
	public void changeQuadColor(int index, Color color) {
		try {
			final Style style = new Style(new Color(color.getRed(), color.getGreen(), color.getBlue(), 128),
					new Color(color.getRed(), color.getGreen(), color.getBlue(), 128), new BasicStroke(),
					new Font(DEFAULT_FONT, Font.BOLD, 12));
			quadList.get(index).setStyle(style);
			mainMap.repaint();
		} catch (final IndexOutOfBoundsException ex) {
			LOG.log(Level.WARNING, ex.getMessage(), ex);
		}
	}

	@Override
	public void deleteTestTile(TestTile testTile) {
		final Iterator<MapPolygon> iter = testTileList.iterator();
		while (iter.hasNext()) {
			final MapPolygon mp = iter.next();
			if (testTile.getID() == mp.getID()) {
				iter.remove();
			}
		}
		mainMap.repaint();
	}

	@Override
	public void deleteArc(int index) {
		try {
			arcList.remove(index);
			mainMap.repaint();
		} catch (final IndexOutOfBoundsException ex) {
			LOG.log(Level.WARNING, ex.getMessage(), ex);
		}
	}

	@Override
	public void deleteIcon(int index) {
		try {
			iconList.remove(index);
			mainMap.repaint();
		} catch (final IndexOutOfBoundsException ex) {
			LOG.log(Level.WARNING, ex.getMessage(), ex);
		}
	}

	@Override
	public void deleteRing(int index) {
		try {
			ringList.remove(index);
			mainMap.repaint();
		} catch (final IndexOutOfBoundsException ex) {
			LOG.log(Level.WARNING, ex.getMessage(), ex);
		}
	}

	@Override
	public void deleteQuad(int index) {
		try {
			quadList.remove(index);
			mainMap.repaint();
		} catch (final IndexOutOfBoundsException ex) {
			LOG.log(Level.WARNING, ex.getMessage(), ex);
		}
	}

	@Override
	public void deleteLine(int index) {
		try {
			lineList.remove(index);
			mainMap.repaint();
		} catch (final IndexOutOfBoundsException ex) {
			LOG.log(Level.WARNING, ex.getMessage(), ex);
		}
	}

	@Override
	public int getIconListSize() {
		return iconList.size();
	}

	@Override
	public void showArcs(boolean showArcs) {
		this.showArcs = showArcs;
		arcList.forEach(arc -> arc.showArc(isDisplayShapes() && showArcs));
	}

	@Override
	public void showArcTrace(boolean showArcTraces) {
		this.showArcTraces = showArcTraces;
		arcList.forEach(arc -> arc.showTrace(isDisplayShapes() && showArcTraces));
	}

	@Override
	public void showArcAsymptotes(boolean showArcAsymptotes) {
		this.showArcAsymptotes = showArcAsymptotes;
		arcList.forEach(arc -> arc.showAsymptote(isDisplayShapes() && showArcAsymptotes));
	}

	@Override
	public void showArcCursors(boolean showArcCursors) {
		this.showArcCursors = showArcCursors;
		arcList.forEach(arc -> arc.showCursor(isDisplayShapes() && showArcCursors));
	}

	@Override
	public void showArcIntersectPoints(boolean showArcIntersectPoints) {
		this.showArcIntersectPoints = showArcIntersectPoints;
		arcIntersectList.forEach(triangle -> triangle.setVisible(isDisplayShapes() && showArcIntersectPoints));
	}

	@Override
	public void showIconLabels(boolean showIconLabels) {
		this.showIconLabels = showIconLabels;
		iconList.forEach(icon -> icon.getLayer().setVisibleTexts(isDisplayShapes() && showIconLabels));
	}

	public boolean isShowIconLabels() {
		return showIconLabels;
	}

	@Override
	public void showLines(boolean showLines) {
		this.showLines = showLines;
		mainMap.repaint();
	}

	@Override
	public void showQuads(boolean showQuads) {
		this.showQuads = showQuads;
		mainMap.repaint();
	}

	@Override
	public void showRings(boolean showRings) {
		this.showRings = showRings;
		mainMap.repaint();
	}

	@Override
	public void showSignalMarkers(boolean showSignalMarkers) {
		this.showSignalMarkers = showSignalMarkers;
		mainMap.repaint();
	}

	@Override
	public void showIcons(boolean showIcons) {
		this.showIcons = showIcons;
		iconList.forEach(icon -> icon.setVisible(isDisplayShapes() && showIcons));
		mainMap.repaint();
	}

	@Override
	public BufferedImage getScreenShot() {
		final Dimension initialSize = getSize();
		setSize(DEFAULT_PRINTER_PAGE_SIZE);
		final BufferedImage image = new BufferedImage(getSize().width, getSize().height, BufferedImage.TYPE_INT_RGB);
		paint(image.createGraphics());
		setSize(initialSize);
		return image;
	}

	@Override
	public void setScale(double scale) {
		final double epsilon = 0.000001D;
		
		if (!Precision.equals(mainMap.getZoom() - 3.0, scale, epsilon)) {
			setZoom((int) (scale + 3.0));
			mainMap.repaint();
		}
	}

	@Override
	public double getScale() {
		return mainMap.getZoom() - 3D;
	}

	@Override
	public void deleteSignalMarker(int index) {
		try {
			signalMarkerList.remove(index);
			mainMap.repaint();
		} catch (final IndexOutOfBoundsException ex) {
			LOG.log(Level.WARNING, ex.getMessage(), ex);
		}
	}

	@Override
	public void setArcCursorRadius(double arcCursorRadius) {
		this.arcCursorRadius = arcCursorRadius;
	}

	@Override
	public void showBulkDownloadPanel() {
		// NO OP
	}

	@Override
	public void setTilesToDownload(Rectangle2D selectionRectangle) {
		setSelectionRectangleMode(false);

		final TileProviderIf provider;

		switch (AbstractMap.SignalTrackMapNames.valueOf(tileFactoryInfo.getName())) {
			case OpenStreetMap -> provider = new TileProviderList().getTileProviderList()[0];
			case VirtualEarthMap -> provider = new TileProviderList().getTileProviderList()[1];
			case VirtualEarthSatellite -> provider = new TileProviderList().getTileProviderList()[2];
			case VirtualEarthHybrid -> provider = new TileProviderList().getTileProviderList()[3];
			default -> provider = new TileProviderList().getTileProviderList()[0];
		}

		final DownloadConfigurationBBoxLatLon dl = new DownloadConfigurationBBoxLatLon();

		dl.setMinLat(selectionRectangle.getMinY());
		dl.setMinLon(selectionRectangle.getMinX());
		dl.setMaxLat(selectionRectangle.getMaxY());
		dl.setMaxLon(selectionRectangle.getMaxX());

		final DownloadJob job = new DownloadJob();

		job.setOutputLocation(cacheDir.getPath());
		job.setOutputZoomLevels(Integer.toString(provider.getMaxZoom()));
		job.setTileServer(provider.getTileServerUrl());

		final TileList list = dl.getTileList(job);

		final TileListDownloader tld = new TileListDownloader(cacheDir.getPath(), list, provider);

		new ProgressBar(list.getTileListToDownload().size(), tld).setVisible(true);
	}

	@Override
	public void showStatisticsPanel() {
		// NO OP
	}

	@Override
	public void showLayerSelectorPanel() {
		// NO OP
	}

	@Override
	public void addArcIntersectPoints(List<Point2D> arcIntersectPoints) {
		addArcIntersectPoints(arcIntersectPoints, arcIntersectPointSize, arcIntersectPointColor);
	}

	@Override
	public void setArcIntersectPoints(List<Point2D> arcIntersectPoints) {
		arcIntersectList.clear();
		addArcIntersectPoints(arcIntersectPoints, arcIntersectPointSize, arcIntersectPointColor);
	}

	@Override
	public void addArcIntersectPoints(List<Point2D> arcIntersectPoints, double width, Color color) {
		final Style style = new Style(color, color, new BasicStroke(), null);
		arcIntersectPoints.stream()
				.map(ip -> new MapMarkerTriangle(null, null, new Coordinate(ip), width, MapTriangle.STYLE.FIXED, style))
				.forEachOrdered(arcIntersectList::add);
		mainMap.repaint();
	}

	@Override
	public void setArcIntersectPoints(List<Point2D> arcIntersectPoints, double width, Color color) {
		arcIntersectList.clear();
		addArcIntersectPoints(arcIntersectPoints, width, color);
	}

	@Override
	public void setQuadVisible(int index, boolean isVisible) {
		quadList.get(index).setVisible(isVisible);
	}

	// This method takes a TestTile object and creates a MapPolygon.
	// Then, it add the MapPolygon to a List of <MapPolygons> to be displayed on the JXMapKit.mainMap
	// TODO: this is not placing the <MapPolygon> in the right location, and must be debugged.
	@Override
	public void addTestTile(TestTile testTile) {
		final Style style = new Style(testTile.getColor(), testTile.getColor(), new BasicStroke(), null);
		final List<ICoordinate> coordinateList = new ArrayList<>();
		testTile.getNorthWestBasedTileCoordinateSet().forEach(lonlat -> coordinateList.add(new Coordinate(lonlat)));
		final MapPolygon tile = new MapPolygonImpl(null, null, coordinateList, style, testTile.getID());
		testTileList.add(tile);
		LOG.log(Level.INFO, "Test tile ID: {0} added to map", testTile.getID());
		mainMap.repaint();
	}
	
	@Override
	public void setTestTileColor(TestTile testTile, Color color) {
		try {
			final Style style = new Style(color, color, new BasicStroke(), null);
			testTileList.stream().filter(aTestTileList -> aTestTileList.getID() == testTile.getID()).forEach(aTestTileList -> aTestTileList.setStyle(style));
			mainMap.repaint();
		} catch (final IndexOutOfBoundsException ex) {
			LOG.log(Level.WARNING, ex.getMessage(), ex);
		}
	}

	private MapPolygon getTestTileByID(int tileID) {
		final List<MapPolygon> result = testTileList.stream().filter(item -> item.getID() == tileID).toList();
		return result.get(0);
	}

	@Override
	public void stopAllTestTileFlash() {
		testTileList.forEach(tt -> tt.setFlash(false));
	}

	@Override
	public void deleteAllTestTiles() {
		if (testTileList == null) {
			return;
		}
		testTileList.clear();
		mainMap.repaint();
	}

	@Override
	public void showTestTiles(boolean showTestTiles) {
		this.showTestTiles = showTestTiles;
	}

	@Override
	public void addFlightPathMarker(Point2D point, double size, Color color) {
		final Style style = new Style(color, color, new BasicStroke(), null);
		final MapMarkerCircle flightPathMarker = new MapMarkerCircle(null, null, new Coordinate(point), size,
				MapMarker.STYLE.FIXED, style);
		flightPathMarkerList.add(flightPathMarker);
		mainMap.repaint();
	}

	@Override
	public void deleteAllFlightPathMarkers() {
		if (flightPathMarkerList == null) {
			return;
		}
		flightPathMarkerList.clear();
		mainMap.repaint();
	}

	@Override
	public void deleteFlightPathMarker(int index) {
		try {
			flightPathMarkerList.remove(index);
			mainMap.repaint();
		} catch (final IndexOutOfBoundsException ex) {
			LOG.log(Level.WARNING, ex.getMessage(), ex);
		}
	}

	@Override
	public void showFlightPathMarkers(boolean showFlightPathMarkers) {
		this.showFlightPathMarkers = showFlightPathMarkers;
		mainMap.repaint();
	}

	@Override
	public void setFlightPathMarkerRadius(double radius) {
		flightPathMarkerList.forEach(aFlightPathMarkerList -> aFlightPathMarkerList.setRadius(radius));
	}

	@Override
	public void addFlightPathMarker(Point2D point, Color color) {
		addFlightPathMarker(point, 2, color);
	}

	@Override
	public boolean isShowFlightPathMarkers() {
		return showFlightPathMarkers;
	}

	@Override
	public boolean isShowSignalMarkers() {
		return showSignalMarkers;
	}

	@Override
	public void setSignalMarkerRadius(double signalMarkerRadius) {
		signalMarkerList.forEach(aSignalMarkerList -> aSignalMarkerList.setRadius(signalMarkerRadius));
	}

	@Override
	public void setArcTraceRadius(double arcTraceRadius) {
		arcList.forEach(arc -> arc.setCursorRadius(arcTraceRadius));
		this.arcTraceRadius = arcTraceRadius;
		mainMap.repaint();
	}

	@Override
	public void setArcIntersectPointSize(double arcIntersectPointSize) {
		arcIntersectList.forEach(triangle -> triangle.setWidth((int) arcIntersectPointSize));
		this.arcIntersectPointSize = arcIntersectPointSize;
		mainMap.repaint();
	}

	@Override
	public boolean isShowTestTiles() {
		return showTestTiles;
	}

	@Override
	public boolean isShowLines() {
		return showLines;
	}

	@Override
	public boolean isShowRings() {
		return showRings;
	}

	@Override
	public boolean isShowArcIntersectPoints() {
		return showArcIntersectPoints;
	}

	@Override
	public Rectangle2D.Double getMapRectangle() {
		return new Rectangle2D.Double(getUpperLeftLonLat().getX(), getUpperLeftLonLat().getY(), getWidth(), getHeight());
	}

	@Override
	public MapDimension getMapDimension() {
		return new MapDimension(getUpperLeftLonLat().getY(), getLowerRightLonLat().getX(), getLowerRightLonLat().getY(),
			getUpperLeftLonLat().getX());
	}

	@Override
	public void redraw() {
		mainMap.repaint();
	}

	private synchronized void paintIcon(Graphics2D g, MapIcon icon) {
		final Point2D p2d = mainMap.convertGeoPositionToPoint(new GeoPosition(icon.getLonLat()));
		final Point p = new Point((int) p2d.getX(), (int) p2d.getY());
		icon.paint(g, p, icon.getSize(), icon.getAngle());
	}

	private synchronized void paintTriangle(Graphics2D g, MapTriangle triangle) {
		try {
			final Point2D p2d = mainMap.convertGeoPositionToPoint(new GeoPosition(triangle.getLonLat()));
			final Point p = new Point((int) p2d.getX(), (int) p2d.getY());
			if (scrollWrapEnabled) {
				final int tilesize = mainMap.getTileFactory().getInfo().getTileSize(mainMap.getZoom());
				final int mapSize = tilesize << mainMap.getZoom();
				triangle.paint(g, p, triangle.getWidth());
				final int xSave = (int) p.getX();
				int xWrap = xSave;
				// overscan of 15 allows up to 30-pixel triangles to gracefully scroll off the
				// edge of the panel
				while ((xWrap -= mapSize) >= -15) {
					p.setLocation(xWrap, p.getY());
					triangle.paint(g, p, triangle.getWidth());
				}
				xWrap = xSave;
				while ((xWrap += mapSize) <= (getWidth() + 15)) {
					p.setLocation(xWrap, p.getY());
					triangle.paint(g, p, triangle.getWidth());
				}
			} else {
				triangle.paint(g, p, triangle.getWidth());
			}
		} catch (NullPointerException ex) {
			LOG.log(Level.WARNING, ex.getMessage(), ex);
		}
	}

	protected synchronized void paintMarker(Graphics2D g, MapMarker marker) {
		try {
			final Point2D p2d = mainMap.convertGeoPositionToPoint(new GeoPosition(marker.getLonLat()));
			final Point p = new Point((int) p2d.getX(), (int) p2d.getY());
			final Integer radius = getRadius(marker, p);
			if (scrollWrapEnabled) {
				final int tilesize = mainMap.getTileFactory().getInfo().getTileSize(mainMap.getZoom());
				final int mapSize = tilesize << mainMap.getZoom();
				marker.paint(g, p, radius);
				final int xSave = (int) p.getX();
				int xWrap = xSave;
				// overscan of 15 allows up to 30-pixel markers to gracefully scroll off the
				// edge of the panel
				while ((xWrap -= mapSize) >= -15) {
					p.setLocation(xWrap, p.getY());
					marker.paint(g, p, radius);
				}
				xWrap = xSave;
				while ((xWrap += mapSize) <= (getWidth() + 15)) {
					p.setLocation(xWrap, p.getY());
					marker.paint(g, p, radius);
				}
			} else {
				marker.paint(g, p, radius);
			}
		} catch (NullPointerException ex) {
			LOG.log(Level.WARNING, ex.getMessage(), ex);
		}
	}

	private synchronized void paintRectangle(Graphics2D g, MapRectangle rectangle) {
		try {
			final Point2D p2dUpperLeft = mainMap
					.convertGeoPositionToPoint(new GeoPosition(rectangle.getUpperLeft().getLonLat()));
			final Point pUpperLeft = new Point((int) p2dUpperLeft.getX(), (int) p2dUpperLeft.getY());
			final Point2D p2dLowerRight = mainMap
					.convertGeoPositionToPoint(new GeoPosition(rectangle.getLowerRight().getLonLat()));
			final Point pLowerRight = new Point((int) p2dLowerRight.getX(), (int) p2dLowerRight.getY());
			if (scrollWrapEnabled) {
				final int tilesize = mainMap.getTileFactory().getInfo().getTileSize(mainMap.getZoom());
				final int mapSize = tilesize << mainMap.getZoom();
				final int xUpperLeftSave = (int) pUpperLeft.getX();
				int xUpperLeftWrap = xUpperLeftSave;
				final int xLowerRightSave = (int) pLowerRight.getX();
				int xLowerRightWrap = xLowerRightSave;
				while ((xLowerRightWrap -= mapSize) >= 0) {
					xUpperLeftWrap -= mapSize;
					pUpperLeft.setLocation(xUpperLeftWrap, pUpperLeft.getY());
					pLowerRight.setLocation(xLowerRightWrap, pLowerRight.getY());
					rectangle.paint(g, pUpperLeft, pLowerRight);
				}
				xUpperLeftWrap = xUpperLeftSave;
				xLowerRightWrap = xLowerRightSave;
				while ((xUpperLeftWrap += mapSize) <= getWidth()) {
					xLowerRightWrap += mapSize;
					pUpperLeft.setLocation(xUpperLeftWrap, pUpperLeft.getY());
					pLowerRight.setLocation(xLowerRightWrap, pLowerRight.getY());
					rectangle.paint(g, pUpperLeft, pLowerRight);
				}
			} else {
				rectangle.paint(g, pUpperLeft, pLowerRight);
			}
		} catch (NullPointerException ex) {
			LOG.log(Level.WARNING, ex.getMessage(), ex);
		}
	}

	private synchronized void paintPolygon(Graphics2D g, MapPolygon mapPolygon) {
		final List<? extends ICoordinate> coords = mapPolygon.getPoints();
		if ((coords != null) && (coords.size() >= 3)) {
			final List<Point> points = new LinkedList<>();
			coords.stream().map(c -> this.mainMap.convertGeoPositionToPoint(new GeoPosition(c.getLonLat())))
					.map(p2d -> new Point((int) p2d.getX(), (int) p2d.getY())).forEachOrdered(points::add);
			mapPolygon.paint(g, points);
			if (this.scrollWrapEnabled) {
				final int tilesize = this.mainMap.getTileFactory().getInfo().getTileSize(this.mainMap.getZoom());
				final int mapSize = tilesize << this.mainMap.getZoom();
				List<Point> pointsWrapped = new LinkedList<>(points);
				boolean keepWrapping = true;
				while (keepWrapping) {
					for (final Point2D p : pointsWrapped) {
						int x = (int) p.getX();
						x -= mapSize;
						p.setLocation(x, p.getY());
						if (p.getX() < 0) {
							keepWrapping = false;
						}
					}
					mapPolygon.paint(g, pointsWrapped);
				}
				pointsWrapped = new LinkedList<>(points);
				keepWrapping = true;
				while (keepWrapping) {
					for (final Point2D p : pointsWrapped) {
						int x = (int) p.getX();
						x -= mapSize;
						p.setLocation(x, p.getY());
						if (p.getX() > getWidth()) {
							keepWrapping = false;
						}
					}
					mapPolygon.paint(g, pointsWrapped);
				}
			}
		}
	}

	private synchronized void paintPolyline(Graphics2D g, MapPolyline polyline) {
		try {
			final List<? extends ICoordinate> coords = polyline.getPoints();
			if ((coords != null) && (coords.size() >= 2)) {
				final List<Point> points = new LinkedList<>();
				coords.stream().map(c -> this.mainMap.convertGeoPositionToPoint(new GeoPosition(c.getLonLat())))
						.map(p2d -> new Point((int) p2d.getX(), (int) p2d.getY())).forEachOrdered(points::add);
				polyline.paint(g, points);
				if (this.scrollWrapEnabled) {
					final int tilesize = this.mainMap.getTileFactory().getInfo().getTileSize(this.mainMap.getZoom());
					final int mapSize = tilesize << this.mainMap.getZoom();
					List<Point> pointsWrapped = new LinkedList<>(points);
					boolean keepWrapping = true;
					while (keepWrapping) {
						for (final Point2D p : pointsWrapped) {
							int x = (int) p.getX();
							x -= mapSize;
							p.setLocation(x, p.getY());
							if (p.getX() < 0) {
								keepWrapping = false;
							}
						}
						polyline.paint(g, pointsWrapped);
					}
					pointsWrapped = new LinkedList<>(points);
					keepWrapping = true;
					while (keepWrapping) {
						for (final Point2D p : pointsWrapped) {
							int x = (int) p.getX();
							x -= mapSize;
							p.setLocation(x, p.getY());
							if (p.getX() > getWidth()) {
								keepWrapping = false;
							}
						}
						polyline.paint(g, pointsWrapped);
					}
				}
			}
		} catch (NullPointerException ex) {
			LOG.log(Level.WARNING, ex.getMessage(), ex);
		}
	}

	@Override
	public void setTileFlash(int tileID, boolean isFlash) {
		getTestTileByID(tileID).setFlash(isFlash);
	}

	@Override
	public boolean isTileFlash(int tileID) {
		return getTestTileByID(tileID).isFlash();
	}

	private int arcSecondsToPixels(double arcSeconds) {
		final double screenHorizArcSecTotal = Math
				.abs(getMapDimension().getLeftLongitude() - getMapDimension().getRightLongitude()) * 3600.0;
		final int screenHorizPixelsTotal = getSize().width;
		final double percentageOfWidth = arcSeconds / screenHorizArcSecTotal;
		return (int) (screenHorizPixelsTotal * percentageOfWidth);
	}

	private Integer getLatOffsetPixels(double lon, double lat, double offset, boolean checkOutside) {
		Integer y = null;
		try {
			final Point2D p = this.mainMap.convertGeoPositionToPoint(new GeoPosition(new Point2D.Double(lon, lat)));
			y = (int) (p.getY() - this.mainMap.getCenter().getY() - (getHeight() / 2D));
			if (checkOutside && ((y - offset < 0) || (y + offset > getHeight()))) {
				return null;
			}
		} catch (NullPointerException ex) {
			LOG.log(Level.WARNING, ex.getMessage(), ex);
		}
		return y;
	}

	private Integer getRadius(MapMarker marker, Point2D p) {
		if (marker.getMarkerStyle() == MapMarker.STYLE.FIXED) {
			return (int) marker.getRadius();
		} else if (marker.getMarkerStyle() == MapMarker.STYLE.ARC_SECONDS) {
			return arcSecondsToPixels(marker.getRadius());
		} else if ((p != null) && (marker.getMarkerStyle() == MapMarker.STYLE.VARIABLE)) {
			return getLatOffsetPixels(marker.getLon(), marker.getLat(), marker.getRadius(), false);
		} else {
			return null;
		}
	}

	@Override
	public Point2D getGridReference() {
		return this.gridReference;
	}

	@Override
	public void setGridColor(Color color) {
		this.gridColor = color;
		if (this.gridLines == null) {
			return;
		}
		final Style style = new Style(color, color, new BasicStroke(), new Font(DEFAULT_FONT, Font.BOLD, 12));
		this.gridLines.forEach(gridLine -> gridLine.setStyle(style));
		this.mainMap.repaint();
	}

	@Override
	public void setGridReference(Point2D gridReference) {
		this.gridReference = gridReference;
		if (this.tileSize != null && gridReference != null && this.gridSize != null && this.gridColor != null) {
			this.gridLines.clear();
			this.gridLines.addAll(buildGrid(this.tileSize, gridReference, this.gridSize, this.gridColor));
		}
		this.mainMap.repaint();
	}

	@Override
	public Point2D getGridSize() {
		return this.gridSize;
	}

	@Override
	public void setGridSize(Point2D gridSize) {
		this.gridSize = gridSize;
		if (this.tileSize != null && gridReference != null && this.gridSize != null && this.gridColor != null) {
			this.gridLines.clear();
			this.gridLines.addAll(buildGrid(this.tileSize, this.gridReference, gridSize, this.gridColor));
		}
		this.mainMap.repaint();
	}

	@Override
	public void setGrid(Point2D tileSize, Point2D gridReference, Point2D gridSize) {
		this.gridSize = gridSize;
		this.tileSize = tileSize;
		this.gridReference = gridReference;
		if (this.tileSize != null && gridReference != null && this.gridSize != null && this.gridColor != null) {
			this.gridLines.clear();
			this.gridLines.addAll(buildGrid(this.tileSize, this.gridReference, gridSize, this.gridColor));
		}
		this.mainMap.repaint();
	}

	private static List<? extends MapRectangleImpl> buildGrid(Point2D tileSizeArcSeconds, Point2D gridReferenceLonLat,
			Point2D gridSizeDegrees, Color color) {
		final List<MapRectangleImpl> grid = Collections.synchronizedList(new LinkedList<>());
		final Style style = new Style(new Color(color.getRed(), color.getGreen(), color.getBlue(), 192),
				new Color(color.getRed(), color.getGreen(), color.getBlue(), 192), new BasicStroke(), null);
		// add vertical lines
		for (double v = gridReferenceLonLat.getX(); v < (0.0001 + gridReferenceLonLat.getX()
				+ gridSizeDegrees.getX()); v += (tileSizeArcSeconds.getX() / 3600D)) {
			final MapRectangleImpl gridLine = new MapRectangleImpl(null, null,
					new Coordinate(v, gridReferenceLonLat.getY()),
					new Coordinate(v, gridReferenceLonLat.getY() - gridSizeDegrees.getY()), style);
			grid.add(gridLine);
		}
		// add horizontal lines
		for (double h = gridReferenceLonLat.getY(); h > ((gridReferenceLonLat.getY() - 0.0001)
				- gridSizeDegrees.getY()); h -= (tileSizeArcSeconds.getY() / 3600D)) {
			final MapRectangleImpl gridLine = new MapRectangleImpl(null, null,
					new Coordinate(gridReferenceLonLat.getX(), h),
					new Coordinate(gridReferenceLonLat.getX() + gridSizeDegrees.getX(), h), style);
			grid.add(gridLine);
		}
		return grid;
	}

	@Override
	public boolean isOptimizedDrawingEnabled() {
		return false;
	}

	@Override
	public void deleteTestGrid() {
		this.gridLines.clear();
		this.mainMap.repaint();
	}

	@Override
	public void deleteAllSignalMarkers() {
		if (this.signalMarkerList == null) {
			return;
		}
		this.signalMarkerList.clear();
		this.mainMap.repaint();
	}

	@Override
	public void deleteAllIcons() {
		if (this.iconList == null) {
			return;
		}
		this.iconList.clear();
		this.mainMap.repaint();
	}

	@Override
	public void deleteAllLines() {
		if (this.lineList == null) {
			return;
		}
		this.lineList.clear();
		this.mainMap.repaint();
	}

	@Override
	public void deleteAllArcs() {
		if (this.arcList == null) {
			return;
		}
		this.arcList.clear();
		this.mainMap.repaint();
	}

	@Override
	public void deleteAllRings() {
		if (this.ringList == null) {
			return;
		}
		this.ringList.clear();
		this.mainMap.repaint();
	}

	@Override
	public Rectangle2D getSelectionRectangle() {
		return this.selectionRectangle.getRectangle2D();
	}

	@Override
	public void setSelectionRectangleVisible(boolean visible) {
		this.selectionRectangle.setVisible(visible);
		this.mainMap.repaint();
	}

	@Override
	public void setRulerMode(boolean rulerMode) {
		this.rulerMode = rulerMode;
		if (rulerMode) {
			this.mainMap.removeMouseMotionListener(this.mia);
		} else {
			ruler.setPoints(null);
			for (MouseMotionListener l : this.mainMap.getMouseMotionListeners()) {
				if (l.equals(this.mia)) {
					this.mainMap.removeMouseMotionListener(this.mia);
				}
			}
			this.mainMap.addMouseMotionListener(this.mia);
		}
		this.mainMap.repaint();
	}

	@Override
	public void setSelectionRectangleMode(boolean selectionRectangleMode) {
		this.selectionRectangleMode = selectionRectangleMode;
		if (selectionRectangleMode) {
			this.setSelectionRectangleDestination(
					new Point2D.Double(this.getSelectionRectangle().getX(), this.getSelectionRectangle().getY())); 
			this.mainMap.removeMouseMotionListener(this.mia);
		} else {
			for (MouseMotionListener l : this.mainMap.getMouseMotionListeners()) {
				if (l.equals(this.mia)) {
					this.mainMap.removeMouseMotionListener(this.mia);
				}
			}
			this.mainMap.addMouseMotionListener(this.mia);
		}
		this.mainMap.repaint();
	}

	private void setRulerEnds(Point2D begin, Point2D end) {
		this.rulerCoords.clear();
		this.rulerCoords.add(new Coordinate(begin));
		this.rulerCoords.add(new Coordinate(end));
		this.ruler.setPoints(this.rulerCoords);
		this.mainMap.repaint();
	}

	@Override
	public void setRulerColor(Color rulerColor) {
		ruler.setColor(rulerColor);
	}

	@Override
	public void setSelectionRectangle(Rectangle2D selectionRectangle) {
		this.selectionRectangle.setRectangle(selectionRectangle);
		this.mainMap.repaint();
	}

	@Override
	public void setSelectionRectangleOrigin(Point2D upperLeft) {
		this.selectionRectangle.setUpperLeft(new Coordinate(upperLeft));
		this.mainMap.repaint();
	}

	@Override
	public Point2D getSelectionRectangleOrigin() {
		return this.selectionRectangle.getUpperLeft().getLonLat();
	}

	@Override
	public void setSelectionRectangleDestination(Point2D lowerRight) {
		this.selectionRectangle.setLowerRight(new Coordinate(lowerRight.getX(), lowerRight.getY()));
		this.mainMap.repaint();
	}

	@Override
	public void addArc(radiolocation.HyperbolicProjection hyperbola) {
		// NO OP
	}

	@Override
	public void clearCache() {
		// NO OP
	}

	@Override
	public void zoomIn() {
		setZoom(this.mainMap.getZoom() - 1);
		this.mainMap.repaint();
	}

	@Override
	public void zoomOut() {
		setZoom(this.mainMap.getZoom() + 1);
		this.mainMap.repaint();
	}

	@Override
	public long getMaxAltitude() {
		return zoomToAltitude(this.mainMap.getTileFactory().getInfo().getMaximumZoomLevel());
	}

	@Override
	public long getMinAltitude() {
		return zoomToAltitude(this.mainMap.getTileFactory().getInfo().getMinimumZoomLevel());
	}

	@Override
	public boolean isPointInBounds(Point2D point) {
		final Rectangle imageBounds = new Rectangle(getInsets().left, getInsets().top, getWidth(), getHeight());
		return imageBounds.contains(point);
	}

	@Override
	public void fitToDisplay(boolean gpsMarker, boolean signalMarkers, boolean flightPathMarkers, boolean testTiles,
			boolean rings, boolean target, boolean testGrid, boolean quads, boolean icons, boolean lines, boolean arcs,
			boolean arcIntersects) {

		final Set<GeoPosition> mapItems = new HashSet<>();

		try {
			if (gpsDot != null && gpsMarker && this.gpsDot.isVisible()) {
				mapItems.add(new GeoPosition(this.gpsDot.getCoordinate().getLonLat()));
			}
		} catch (NullPointerException ex) {
			LOG.log(Level.WARNING, ex.getMessage(), ex);
		}

		try {
			if (gpsArrow != null && gpsMarker && this.gpsArrow.isVisible()) {
				mapItems.add(new GeoPosition(this.gpsArrow.getLonLat()));
			}
		} catch (NullPointerException ex) {
			LOG.log(Level.WARNING, ex.getMessage(), ex);
		}

		try {
			if (signalMarkerList != null && signalMarkers) {
				this.signalMarkerList.stream().filter(signalMarker -> (signalMarker.isVisible())).forEachOrdered(
						signalMarker -> mapItems.add(new GeoPosition(signalMarker.getCoordinate().getLonLat())));
			}
		} catch (NullPointerException ex) {
			LOG.log(Level.WARNING, ex.getMessage(), ex);
		}

		try {
			if (flightPathMarkerList != null && flightPathMarkers) {
				this.flightPathMarkerList.stream().filter(signalMarker -> (signalMarker.isVisible())).forEachOrdered(
						signalMarker -> mapItems.add(new GeoPosition(signalMarker.getCoordinate().getLonLat())));
			}
		} catch (NullPointerException ex) {
			LOG.log(Level.WARNING, ex.getMessage(), ex);
		}

		try {
			if (rings) {
				this.ringList.stream().filter(ring -> (ring.isVisible()))
						.forEachOrdered(ring -> mapItems.add(new GeoPosition(ring.getCoordinate().getLonLat())));
			}
		} catch (NullPointerException ex) {
			LOG.log(Level.WARNING, ex.getMessage(), ex);
		}

		try {
			if (icons) {
				this.iconList.stream().filter(icon -> (icon.isVisible()))
						.forEachOrdered(icon -> mapItems.add(new GeoPosition(icon.getLonLat())));

			}
		} catch (NullPointerException ex) {
			LOG.log(Level.WARNING, ex.getMessage(), ex);
		}

		try {
			if (target && this.targetRing.isVisible()) {
				mapItems.add(new GeoPosition(this.targetRing.getCoordinate().getLonLat()));
			}
		} catch (NullPointerException ex) {
			LOG.log(Level.WARNING, ex.getMessage(), ex);
		}

		try {
			if (testTiles) {
				testTileList.stream().filter(MapPolygon::isVisible).forEach(testTile -> mapItems.add(new GeoPosition(testTile.getNorthWestLonLat())));
			}
		} catch (NullPointerException ex) {
			LOG.log(Level.WARNING, ex.getMessage(), ex);
		}

		try {
			if (quads) {
				this.quadList.stream().filter(quad -> (quad.isVisible())).forEachOrdered(
						quad -> quad.getPoints().forEach(c -> mapItems.add(new GeoPosition(c.getLonLat()))));
			}
		} catch (NullPointerException ex) {
			LOG.log(Level.WARNING, ex.getMessage(), ex);
		}

		try {
			if (testGrid) {
				this.gridLines.stream().filter(testLine -> (testLine.isVisible())).forEachOrdered(
						testLine -> testLine.getPoints().forEach(c -> mapItems.add(new GeoPosition(c.getLonLat()))));
			}
		} catch (NullPointerException ex) {
			LOG.log(Level.WARNING, ex.getMessage(), ex);
		}

		try {
			if (lines) {
				this.lineList.stream().filter(line -> (line.isVisible())).forEachOrdered((MapPolylineImpl line) -> line
						.getPoints().forEach(c -> mapItems.add(new GeoPosition(c.getLonLat()))));
			}
		} catch (NullPointerException ex) {
			LOG.log(Level.WARNING, ex.getMessage(), ex);
		}

		try {
			if (arcIntersects) {
				this.arcIntersectList.stream().filter(arcIntersect -> (arcIntersect.isVisible()))
						.forEachOrdered(arcIntersect -> mapItems.add(new GeoPosition(arcIntersect.getLonLat())));
			}
		} catch (NullPointerException ex) {
			LOG.log(Level.WARNING, ex.getMessage(), ex);
		}

		try {
			if (arcs) {
				this.arcList.stream().map((HyperbolicProjection arc) -> {
					if (arc.showArc()) {
						arc.getArcPointList().forEach(c -> mapItems.add(new GeoPosition(c)));
					}
					return arc;
				}).map((HyperbolicProjection arc) -> {
					if (arc.showAsymptote()) {
						arc.getAsymptotePointList().forEach((Point2D c) -> mapItems.add(new GeoPosition(c)));
					}
					return arc;
				}).filter(arc -> (arc.showTrace())).forEachOrdered((HyperbolicProjection arc) -> arc.getTracePolyline()
						.getPoints().forEach(c -> mapItems.add(new GeoPosition(c.getLonLat()))));
			}
		} catch (NullPointerException ex) {
			LOG.log(Level.WARNING, ex.getMessage(), ex);
		}

		this.mainMap.zoomToBestFit(mapItems, 0.7);
	}

	@Override
	public synchronized void addMouseMotionListener(MouseMotionListener l) {
		this.mainMap.addMouseMotionListener(l);
	}

	@Override
	public synchronized void addMouseListener(MouseListener l) {
		this.mainMap.addMouseListener(l);
	}

	@Override
	public synchronized void addKeyListener(KeyListener l) {
		this.mainMap.addKeyListener(l);
	}

	@Override
	public double getCursorElevationMeters() {
		return Short.MIN_VALUE;
	}

	@Override
	public double getDisplayAltitude() {
		return getMapScale() / 2.0;
	}
	
	private class GpsArrowPolyline extends MapPolylineImpl implements GpsArrow {

		private final Point2D point;
		private final int angle;
		private final double radius;

		public GpsArrowPolyline(Layer layer, String name, Point2D point, int angle, double radius, Style style) {
			super(layer, name, style);

			this.point = point;
			this.angle = angle;
			this.radius = radius;

			super.setPoints(buildArrow(point, angle, radius));
		}

		private List<ICoordinate> buildArrow(Point2D point, double angle, double length) {
			final List<ICoordinate> coords = new CopyOnWriteArrayList<>();
			try {
				final double meters = (length * getMetersPerPixel()) * GPS_ARROW_SIZE_ADJUST_FACTOR;
				final Point2D arrowTail = Vincenty.getVincentyDirect(point, angle + 180, meters).getDestinationPoint();
				final Point2D arrowLeft = Vincenty.getVincentyDirect(point, angle + 150, meters / 2).getDestinationPoint();
				final Point2D arrowRight = Vincenty.getVincentyDirect(point, angle + 210, meters / 2).getDestinationPoint();
				coords.add(new Coordinate(arrowTail));
				coords.add(new Coordinate(point));
				coords.add(new Coordinate(arrowLeft));
				coords.add(new Coordinate(point));
				coords.add(new Coordinate(arrowRight));
				coords.add(new Coordinate(point));
			} catch (IllegalArgumentException ex) {
				LOG.log(Level.WARNING, ex.getMessage(), ex);
			}

			return coords;
		}

		private Double getMetersPerPixel() {
			Double ret = 0D;
			try {
				final Point2D ul = getUpperLeftLonLat();
				if (ul == null) {
					return ret;
				}
				final Point2D lr = getLowerRightLonLat();
				if (lr == null) {
					return ret;
				}
				final double verticalScreenDegrees = ul.distance(ul.getX(), lr.getY());
				final double degreesPerPixel = verticalScreenDegrees / getHeight();

				ret = Vincenty.degreesToMeters(degreesPerPixel, 0, lr.getY());
			} catch (NullPointerException ex) {
				LOG.log(Level.WARNING, ex.getMessage(), ex);
			}
			return ret;
		}

		@Override
		public Point2D getLonLat() {
			if (super.getPoints().isEmpty()) {
				return null;
			}
			return super.getPoints().get(0).getLonLat();
		}

		@Override
		public int getAngle() {
			return this.angle;
		}

		@Override
		public void setAngle(int angle) {
			super.setPoints(buildArrow(this.point, angle, this.radius));
		}

		@Override
		public double getRadius() {
			return this.radius;
		}

	}

	@Override
    public void close() {
		this.tileFactory.dispose();
		if (iconScheduler != null) {
			try {
				LOG.log(Level.SEVERE, "Initializing JXMapKit.iconScheduler service termination....");
				iconScheduler.shutdown();
				iconScheduler.awaitTermination(2, TimeUnit.SECONDS);
				LOG.log(Level.SEVERE, "JXMapKit.iconScheduler service has gracefully terminated");
			} catch (InterruptedException e) {
				iconScheduler.shutdownNow();
				LOG.log(Level.SEVERE, "JXMapKit.iconScheduler service has timed out after 2 seconds of waiting to terminate processes.");
				Thread.currentThread().interrupt();
			}
		}
		if (flashScheduler != null) {
			try {
				LOG.log(Level.SEVERE, "Initializing JXMapKit.flashScheduler service termination....");
				flashScheduler.shutdown();
				flashScheduler.awaitTermination(2, TimeUnit.SECONDS);
				LOG.log(Level.SEVERE, "JXMapKit.flashScheduler service has gracefully terminated");
			} catch (InterruptedException e) {
				flashScheduler.shutdownNow();
				LOG.log(Level.SEVERE, "JXMapKit.flashScheduler service has timed out after 2 seconds of waiting to terminate processes.");
				Thread.currentThread().interrupt();
			}
		}
    }
	
	@Override
	public String getUniqueIdentifier() {
		return Long.toString(serialVersionUID);
	}

	private double getMapWidthViewInMeters() {
		final Point2D leftEdge = JXMapKit.this.mainMap.convertPointToGeoPosition(new Point(0, 0)).getLonLat();
		final Point2D rightEdge = JXMapKit.this.mainMap.convertPointToGeoPosition(new Point(0, this.mainMap.getWidth()))
				.getLonLat();
		return Vincenty.getVincentyInverse(leftEdge, rightEdge).getDistanceMeters();
	}

	private double getMapScale() {
		final double mapWidthInPixels = mainMap.getViewportBounds().getWidth();
		final int pixelsPerInch = Toolkit.getDefaultToolkit().getScreenResolution();
		final double feetOfMapViewWidth = (mapWidthInPixels / pixelsPerInch) / 12;
		final double mapSceneWidthActualFeet = Vincenty.metersToFeet(getMapWidthViewInMeters());
		return mapSceneWidthActualFeet / feetOfMapViewWidth;
	}

	private long zoomToAltitude(int zoom) {
		return (long) (ZOOM_LEVEL_CONVERSION_FACTOR
				* Math.pow(1.7, this.tileFactoryInfo.getMaximumZoomLevel() - (double) zoom));
	}

	private int altitudeToZoom(long altitude) {
		return (int) (this.tileFactoryInfo.getMaximumZoomLevel()
				- logb((double) altitude / ZOOM_LEVEL_CONVERSION_FACTOR, 1.7));
	}

	private static double logb(double a, double b) {
		return Math.log(a) / Math.log(b);
	}

	@Override
	public void setAltitude(int altitude) {
		setZoom(altitudeToZoom(altitude));
	}

	@Override
	public long getAltitude() {
		return zoomToAltitude(this.mainMap.getZoom());
	}

	@Override
	public SignalTrackMapNames getSignalTrackMapName() {
		return SignalTrackMapNames.valueOf(this.tileFactoryInfo.getName());
	}

	@Override
	public boolean hasLayerSelectorPanel() {
		return false;
	}

	@Override
	public boolean hasBulkDownloaderPanel() {
		return true;
	}

	@Override
	public boolean hasStatisticsPanel() {
		return false;
	}

	private class IconSchedule implements Runnable {

		@Override
		public void run() {
			if (JXMapKit.this.iconList != null) {
				final ListIterator<MapIconImpl> itr = JXMapKit.this.iconList.listIterator();
				while (itr.hasNext()) {
					final MapIconImpl mi = itr.next();
					if (System.currentTimeMillis() - mi.getTimeToGoStale() > 0) {
						mi.setStrikeout(true);
					}
					if (System.currentTimeMillis() - mi.getTimeToDelete() > 0) {
						itr.remove();
					}
					mainMap.repaint();
				}
			}
		}
	}

	@Override
	public JPanel getConfigPanel() {
		return new JPanel();
	}

	@Override
	public void showCacheViewerPanel() {
		// NO OP
	}

	@Override
	public boolean hasCacheViewerPanel() {
		return false;
	}

	@Override
	public void clearMapObjects() {
		deleteAllSignalMarkers();
		deleteAllFlightPathMarkers();
		deleteAllIcons();
		deleteAllLines();
		deleteAllQuads();
		deleteAllRings();
		deleteAllArcs();
		deleteAllTestTiles();
		deleteTestGrid();
		deleteAllArcIntersectPoints();
	}

}
