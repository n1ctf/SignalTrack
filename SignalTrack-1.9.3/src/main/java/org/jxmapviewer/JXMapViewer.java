/*
 * MapViewer.java
 *
 * Created on March 14, 2006, 2:14 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.jxmapviewer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.beans.DesignMode;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.painter.AbstractPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.GeoBounds;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.Tile;
import org.jxmapviewer.viewer.TileFactory;
import org.jxmapviewer.viewer.TileFactoryInfo;
import org.jxmapviewer.viewer.TileListener;
import org.jxmapviewer.viewer.empty.EmptyTileFactory;

/**
 * A tile oriented map component that can easily be used with tile sources on
 * the web like Google and Yahoo maps, satellite data such as NASA imagery, and
 * also with file based sources like pre-processed NASA images.
 *
 * Note, the JXMapViewer has three center point properties. The
 * <B>addressLocation</B> property represents an abstract center of the map.
 * This would usually be something like the first item in a search result. It is
 * a {@link GeoPosition}. The <b>centerPosition</b> property represents the
 * current center point of the map. If the user pans the map then the
 * centerPosition point will change but the <B>addressLocation</B> will not.
 * Calling <B>recenterToAddressLocation()</B> will move the map back to that
 * center address. The <B>center</B> property represents the same point as the
 * centerPosition property, but as a Point2D in pixel space instead of a
 * GeoPosition in lat/long space. Note that the center property is a Point2D in
 * the entire world bitmap, not in the portion of the map currently visible. You
 * can use the <B>getViewportBounds()</B> method to find the portion of the map
 * currently visible and adjust your calculations accordingly. Changing the
 * <B>center</B> property will change the <B>centerPosition</B> property and
 * vice versa. All three properties are bound.
 * 
 * @author Joshua.Marinacci@sun.com
 */
public class JXMapViewer extends JPanel implements DesignMode {
	private static final long serialVersionUID = -1L;

	/**
	 * The zoom level. Generally a value between 1 and 15 (TODO Is this true for all
	 * the mapping worlds? What does this mean if some mapping system doesn't
	 * support the zoom level?
	 */
	private int zoomLevel = 1;

	/**
	 * The position, in <I>map coordinates</I> of the center point. This is defined
	 * as the distance from the top and left edges of the map in pixels. Dragging
	 * the map component will change the center position. Zooming in/out will cause
	 * the center to be recalculated so as to remain in the center of the new "map".
	 */
	private transient Point2D center = new Point2D.Double(0, 0);

	/**
	 * Indicates whether or not to draw the borders between tiles. Defaults to
	 * false. TODO Generally not very nice looking, very much a product of testing
	 * Consider whether this should really be a property or not.
	 */
	private boolean drawTileBorders = false;

	/**
	 * Factory used by this component to grab the tiles necessary for painting the
	 * map.
	 */
	private transient TileFactory factory;

	/**
	 * The position in latitude/longitude of the "address" being mapped. This is a
	 * special coordinate that, when moved, will cause the map to be moved as well.
	 * It is separate from "center" in that "center" tracks the current center (in
	 * pixels) of the viewport whereas this will not change when panning or zooming.
	 * Whenever the addressLocation is changed, however, the map will be
	 * repositioned.
	 */
	private GeoPosition addressLocation;

	/**
	 * The overlay to delegate to for painting the "foreground" of the map
	 * component. This would include painting waypoints, day/night, etc. Also
	 * receives mouse events.
	 */
	private transient Painter<? super JXMapViewer> overlay;

	private boolean designTime;

	private transient Image loadingImage;

	private boolean restrictOutsidePanning = true;
	private boolean horizontalWrapped = true;
	private boolean infiniteMapRendering = true;

	/**
	 * If true, panning with the mouse should take place. If false, panning should
	 * not happen. Does not disable explicit setting of position via
	 * {@link setCenter}.
	 */
	private boolean panningEnabled = true;

	/**
	 * Create a new JXMapViewer. By default it will use the EmptyTileFactory
	 */
	public JXMapViewer() {
		factory = new EmptyTileFactory();
		// setTileFactory(new GoogleTileFactory());

		// make a dummy loading image
		try {
			final URL url = JXMapViewer.class.getResource("/images/loading.png");
			this.setLoadingImage(ImageIO.read(url));
		} catch (Exception ex) {
			System.out.println("could not load 'loading.png'");
			final BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
			final Graphics2D g2 = img.createGraphics();
			g2.setColor(Color.black);
			g2.fillRect(0, 0, 16, 16);
			g2.dispose();
			this.setLoadingImage(img);
		}

		// setAddressLocation(new GeoPosition(37.392137,-121.950431)); // Sun campus
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		doPaintComponent(g);
	}

	// the method that does the actual painting
	private void doPaintComponent(Graphics g) {/*
												 * if (isOpaque() || isDesignTime()) { g.setColor(getBackground());
												 * g.fillRect(0,0,getWidth(),getHeight()); }
												 */

		if (isDesignTime()) {
			// do nothing
		} else {
			final int z = getZoom();
			final Rectangle viewportBounds = getViewportBounds();
			drawMapTiles(g, z, viewportBounds);
			drawOverlays(g);
		}

		super.paintBorder(g);
	}

	/**
	 * Indicate that the component is being used at design time, such as in a visual
	 * editor like NetBeans' Matisse
	 * 
	 * @param b indicates if the component is being used at design time
	 */
	@Override
	public void setDesignTime(boolean b) {
		this.designTime = b;
	}

	/**
	 * Indicates whether the component is being used at design time, such as in a
	 * visual editor like NetBeans' Matisse
	 * 
	 * @return boolean indicating if the component is being used at design time
	 */
	@Override
	public boolean isDesignTime() {
		return designTime;
	}

	/**
	 * Draw the map tiles. This method is for implementation use only.
	 * 
	 * @param g              Graphics
	 * @param zoom           zoom level to draw at
	 * @param viewportBounds the bounds to draw within
	 */
	protected void drawMapTiles(final Graphics g, final int zoom, Rectangle viewportBounds) {
		final int size = getTileFactory().getTileSize(zoom);
		final Dimension mapSize = getTileFactory().getMapSize(zoom);

		// calculate the "visible" viewport area in tiles
		final int numWide = viewportBounds.width / size + 2;
		final int numHigh = viewportBounds.height / size + 2;

		// TilePoint topLeftTile = getTileFactory().getTileCoordinate(
		// new Point2D.Double(viewportBounds.x, viewportBounds.y));
		final TileFactoryInfo info = getTileFactory().getInfo();

		// number of tiles in x direction
		final int tpx = (int) Math.floor(viewportBounds.getX() / info.getTileSize(0));
		// number of tiles in y direction
		final int tpy = (int) Math.floor(viewportBounds.getY() / info.getTileSize(0));
		// TilePoint topLeftTile = new TilePoint(tpx, tpy);

		// p("top tile = " + topLeftTile);
		// fetch the tiles from the factory and store them in the tiles cache
		// attach the tileLoadListener
		for (int x = 0; x <= numWide; x++) {
			for (int y = 0; y <= numHigh; y++) {
				final int itpx = x + tpx;// topLeftTile.getX();
				final int itpy = y + tpy;// topLeftTile.getY();
				// TilePoint point = new TilePoint(x + topLeftTile.getX(), y +
				// topLeftTile.getY());
				// only proceed if the specified tile point lies within the area being painted
				if (g.getClipBounds().intersects(
						new Rectangle(itpx * size - viewportBounds.x, itpy * size - viewportBounds.y, size, size))) {
					final Tile tile = getTileFactory().getTile(itpx, itpy, zoom);
					final int ox = ((itpx * getTileFactory().getTileSize(zoom)) - viewportBounds.x);
					final int oy = ((itpy * getTileFactory().getTileSize(zoom)) - viewportBounds.y);

					// if the tile is off the map to the north/south, then just don't paint anything
					if (!isTileOnMap(itpx, itpy, mapSize)) {
						if (isOpaque()) {
							g.setColor(getBackground());
							g.fillRect(ox, oy, size, size);
						}
					} else if (tile.isLoaded()) {
						g.drawImage(tile.getImage(), ox, oy, null);
					} else {
						Tile superTile = null;

						// Use tile at higher zoom level with 200% magnification and if we are not
						// already at max resolution
						if (zoom < info.getMaximumZoomLevel()) {
							superTile = getTileFactory().getTile(itpx / 2, itpy / 2, zoom + 1);
						}

						if (superTile != null && superTile.isLoaded()) {
							final int offX = (itpx % 2) * size / 2;
							final int offY = (itpy % 2) * size / 2;
							g.drawImage(superTile.getImage(), ox, oy, ox + size, oy + size, offX, offY, offX + size / 2,
									offY + size / 2, null);
						} else {
							final int imageX = (getTileFactory().getTileSize(zoom) - getLoadingImage().getWidth(null))
									/ 2;
							final int imageY = (getTileFactory().getTileSize(zoom) - getLoadingImage().getHeight(null))
									/ 2;
							if (isOpaque()) {
								g.setColor(getBackground());
								g.fillRect(ox, oy, size, size);
							}
							g.drawImage(getLoadingImage(), ox + imageX, oy + imageY, null);
						}
					}
					if (isDrawTileBorders()) {

						g.setColor(Color.black);
						g.drawRect(ox, oy, size, size);
						g.drawRect(ox + size / 2 - 5, oy + size / 2 - 5, 10, 10);
						g.setColor(Color.white);
						g.drawRect(ox + 1, oy + 1, size, size);

						final String text = itpx + ", " + itpy + ", " + getZoom();
						g.setColor(Color.BLACK);
						g.drawString(text, ox + 10, oy + 30);
						g.drawString(text, ox + 10 + 2, oy + 30 + 2);
						g.setColor(Color.WHITE);
						g.drawString(text, ox + 10 + 1, oy + 30 + 1);
					}
				}
			}
		}
	}

	@SuppressWarnings("unused")
	private void drawOverlays(Graphics g) {
		if (overlay != null) {
			overlay.paint((Graphics2D) g, this, getWidth(), getHeight());
		}
	}

	private boolean isTileOnMap(int x, int y, Dimension mapSize) {
		return (y >= 0 && y < mapSize.getHeight()) && (isInfiniteMapRendering() || x >= 0 && x < mapSize.getWidth());
	}

	/**
	 * Sets the map overlay. This is a {@code Painter<JXMapViewer>} which will paint
	 * on top of the map. It can be used to draw waypoints, lines, or static
	 * overlays like text messages.
	 * 
	 * @param overlay the map overlay to use
	 */
	public void setOverlayPainter(Painter<? super JXMapViewer> overlay) {
		final Painter<? super JXMapViewer> old = getOverlayPainter();
		this.overlay = overlay;

		final PropertyChangeListener listener = (PropertyChangeEvent evt) -> {
			if (evt.getNewValue().equals(Boolean.TRUE)) {
				repaint();
			}
		};

		if (old instanceof AbstractPainter) {
			final AbstractPainter<?> ap = (AbstractPainter<?>) old;
			ap.removePropertyChangeListener("dirty", listener);
		}

		if (overlay instanceof AbstractPainter) {
			final AbstractPainter<?> ap = (AbstractPainter<?>) overlay;
			ap.addPropertyChangeListener("dirty", listener);
		}

		firePropertyChange("mapOverlay", old, getOverlayPainter());
		repaint();
	}

	/**
	 * Gets the current map overlay
	 * 
	 * @return the current map overlay
	 */
	public Painter<? super JXMapViewer> getOverlayPainter() {
		return overlay;
	}

	/**
	 * Returns the bounds of the viewport in pixels. This can be used to transform
	 * points into the world bitmap coordinate space.
	 * 
	 * @return the bounds in <em>pixels</em> of the "view" of this map
	 */
	public Rectangle getViewportBounds() {
		return calculateViewportBounds(getCenter());
	}

	private Rectangle calculateViewportBounds(Point2D centr) {
		final Insets insets = getInsets();
		// calculate the "visible" viewport area in pixels
		final int viewportWidth = getWidth() - insets.left - insets.right;
		final int viewportHeight = getHeight() - insets.top - insets.bottom;
		final double viewportX = (centr.getX() - viewportWidth / 2D);
		final double viewportY = (centr.getY() - viewportHeight / 2D);
		return new Rectangle((int) viewportX, (int) viewportY, viewportWidth, viewportHeight);
	}

	/**
	 * Set the current zoom level
	 * 
	 * @param zoom the new zoom level
	 */
	public void setZoom(int zoom) {
		if (zoom == this.zoomLevel) {
			return;
		}

		final TileFactoryInfo info = getTileFactory().getInfo();
		// don't repaint if we are out of the valid zoom levels
		if (info != null && (zoom < info.getMinimumZoomLevel() || zoom > info.getMaximumZoomLevel())) {
			return;
		}

		final int oldzoom = this.zoomLevel;
		final Point2D oldCenter = getCenter();
		final Dimension oldMapSize = getTileFactory().getMapSize(oldzoom);
		this.zoomLevel = zoom;
		this.firePropertyChange("zoom", oldzoom, zoom);

		final Dimension mapSize = getTileFactory().getMapSize(zoom);

		setCenter(new Point2D.Double(oldCenter.getX() * (mapSize.getWidth() / oldMapSize.getWidth()),
				oldCenter.getY() * (mapSize.getHeight() / oldMapSize.getHeight())));

		repaint();
	}

	/**
	 * Gets the current zoom level
	 * 
	 * @return the current zoom level
	 */
	public int getZoom() {
		return this.zoomLevel;
	}

	/**
	 * Gets the current address location of the map. This property does not change
	 * when the user pans the map. This property is bound.
	 * 
	 * @return the current map location (address)
	 */
	public GeoPosition getAddressLocation() {
		return addressLocation;
	}

	/**
	 * Gets the current address location of the map
	 * 
	 * @param addressLocation the new address location
	 */
	public void setAddressLocation(GeoPosition addressLocation) {
		final GeoPosition old = getAddressLocation();
		this.addressLocation = addressLocation;
		setCenter(getTileFactory().geoToPixel(addressLocation, getZoom()));

		firePropertyChange("addressLocation", old, getAddressLocation());
		repaint();
	}

	/**
	 * Re-centers the map to have the current address location be at the center of
	 * the map, accounting for the map's width and height.
	 */
	public void recenterToAddressLocation() {
		setCenter(getTileFactory().geoToPixel(getAddressLocation(), getZoom()));
		repaint();
	}

	/**
	 * Indicates if the tile borders should be drawn. Mainly used for debugging.
	 * 
	 * @return the value of this property
	 */
	public boolean isDrawTileBorders() {
		return drawTileBorders;
	}

	/**
	 * Set if the tile borders should be drawn. Mainly used for debugging.
	 * 
	 * @param drawTileBorders new value of this drawTileBorders
	 */
	public void setDrawTileBorders(boolean drawTileBorders) {
		final boolean old = isDrawTileBorders();
		this.drawTileBorders = drawTileBorders;
		firePropertyChange("drawTileBorders", old, isDrawTileBorders());
		repaint();
	}

	/**
	 * A property indicating the center position of the map
	 * 
	 * @param geoPosition the new property value
	 */
	public void setCenterPosition(GeoPosition geoPosition) {
		final GeoPosition oldVal = getCenterPosition();
		setCenter(getTileFactory().geoToPixel(geoPosition, zoomLevel));
		repaint();
		final GeoPosition newVal = getCenterPosition();
		firePropertyChange("centerPosition", oldVal, newVal);
	}

	/**
	 * A property indicating the center position of the map
	 * 
	 * @return the current center position
	 */
	public GeoPosition getCenterPosition() {
		return getTileFactory().pixelToGeo(getCenter(), zoomLevel);
	}

	/**
	 * Get the current factory
	 * 
	 * @return the current property value
	 */
	public TileFactory getTileFactory() {
		return factory;
	}

	/**
	 * Set the current tile factory (must not be <code>null</code>)
	 * 
	 * @param factory the new property value
	 */
	public void setTileFactory(TileFactory factory) {
		if (factory == null) {
			throw new NullPointerException("factory must not be null");
		}

		this.factory.removeTileListener(tileLoadListener);
		this.factory.dispose();

		this.factory = factory;
		this.setZoom(factory.getInfo().getDefaultZoomLevel());

		factory.addTileListener(tileLoadListener);

		repaint();
	}

	/**
	 * A property for an image which will be display when an image is still loading.
	 * 
	 * @return the current property value
	 */
	public Image getLoadingImage() {
		return loadingImage;
	}

	/**
	 * A property for an image which will be display when an image is still loading.
	 * 
	 * @param loadingImage the new property value
	 */
	public void setLoadingImage(Image loadingImage) {
		this.loadingImage = loadingImage;
	}

	/**
	 * Gets the current pixel center of the map. This point is in the global bitmap
	 * coordinate system, not as lat/longs.
	 * 
	 * @return the current center of the map as a pixel value
	 */
	public Point2D getCenter() {
		return center;
	}

	/**
	 * Sets the new center of the map in pixel coordinates.
	 * 
	 * @param center the new center of the map in pixel coordinates
	 */
	public void setCenter(Point2D center) {
		final Point2D old = this.getCenter();

		double centerX = center.getX();
		double centerY = center.getY();

		final Dimension mapSize = getTileFactory().getMapSize(getZoom());
		final int mapHeight = (int) mapSize.getHeight() * getTileFactory().getTileSize(getZoom());
		final int mapWidth = (int) mapSize.getWidth() * getTileFactory().getTileSize(getZoom());

		if (isRestrictOutsidePanning()) {
			final Insets insets = getInsets();
			final int viewportHeight = getHeight() - insets.top - insets.bottom;
			final int viewportWidth = getWidth() - insets.left - insets.right;

			// don't let the user pan over the top edge
			final Rectangle newVP = calculateViewportBounds(center);
			if (newVP.getY() < 0) {
				centerY = viewportHeight / 2D;
			}

			// don't let the user pan over the left edge
			if (!isHorizontalWrapped() && newVP.getX() < 0) {
				centerX = viewportWidth / 2D;
			}

			// don't let the user pan over the bottom edge
			if (newVP.getY() + newVP.getHeight() > mapHeight) {
				centerY = mapHeight - viewportHeight / 2D;
			}

			// don't let the user pan over the right edge
			if (!isHorizontalWrapped() && (newVP.getX() + newVP.getWidth() > mapWidth)) {
				centerX = mapWidth - viewportWidth / 2D;
			}

			// if map is to small then just center it vert
			if (mapHeight < newVP.getHeight()) {
				centerY = mapHeight / 2D;
			}

			// if map is too small then just center it horiz
			if (!isHorizontalWrapped() && mapWidth < newVP.getWidth()) {
				centerX = mapWidth / 2D;
			}
		}

		// If center is outside (0, 0,mapWidth, mapHeight)
		// compute modulo to get it back in.

		centerX = centerX % mapWidth;
		centerY = centerY % mapHeight;

		if (centerX < 0) {
			centerX += mapWidth;
		}

		if (centerY < 0) {
			centerY += mapHeight;
		}

		final GeoPosition oldGP = this.getCenterPosition();
		this.center = new Point2D.Double(centerX, centerY);
		firePropertyChange("center", old, this.center);
		firePropertyChange("centerPosition", oldGP, this.getCenterPosition());
		repaint();
	}

	/**
	 * Calculates a zoom level so that all points in the specified set will be
	 * visible on screen. This is useful if you have a bunch of points in an area
	 * like a city and you want to zoom out so that the entire city and it's points
	 * are visible without panning.
	 * 
	 * @param positions A set of GeoPositions to calculate the new zoom from
	 */
	public void calculateZoomFrom(Set<GeoPosition> positions) {
		if (positions.size() < 2) {
			return;
		}

		int zoom = getZoom();
		Rectangle2D rect = generateBoundingRect(positions, zoom);
		int count = 0;
		while (!getViewportBounds().contains(rect)) {
			final Point2D centr = new Point2D.Double(rect.getX() + rect.getWidth() / 2,
					rect.getY() + rect.getHeight() / 2);
			final GeoPosition px = getTileFactory().pixelToGeo(centr, zoom);
			setCenterPosition(px);
			count++;
			if (count > 30) {
				break;
			}

			if (getViewportBounds().contains(rect)) {
				break;
			}
			zoom += 1;
			if (zoom > getTileFactory().getInfo().getMaximumZoomLevel()) {
				break;
			}
			setZoom(zoom);
			rect = generateBoundingRect(positions, zoom);
		}
	}

	/**
	 * Zoom and center the map to a best fit around the input GeoPositions. Best fit
	 * is defined as the most zoomed-in possible view where both the width and
	 * height of a bounding box around the positions take up no more than
	 * maxFraction of the viewport width or height respectively.
	 * 
	 * @param positions   A set of GeoPositions to calculate the new zoom from
	 * @param maxFraction the maximum fraction of the viewport that should be
	 *                    covered
	 */
	public void zoomToBestFit(Set<GeoPosition> positions, double maxFraction) {
		if (positions.isEmpty()) {
			return;
		}

		if (maxFraction <= 0 || maxFraction > 1) {
			throw new IllegalArgumentException("maxFraction must be between 0 and 1");
		}

		final TileFactory tileFactory = getTileFactory();
		final TileFactoryInfo info = tileFactory.getInfo();

		if (info == null) {
			return;
		}

		// set to central position initially
		final GeoPosition centre = new GeoBounds(positions).getCenter();

		setCenterPosition(centre);

		if (positions.size() == 1) {
			return;
		}

		// repeatedly zoom in until we find the first zoom level where either the width
		// or height
		// of the points takes up more than the max fraction of the viewport

		// start with zoomed out at maximum
		int bestZoom = info.getMaximumZoomLevel();

		final Rectangle2D viewport = getViewportBounds();

		Rectangle2D bounds = generateBoundingRect(positions, bestZoom);

		// is this zoom still OK?
		while (bounds.getWidth() < viewport.getWidth() * maxFraction
				&& bounds.getHeight() < viewport.getHeight() * maxFraction) {
			if (--bestZoom < info.getMinimumZoomLevel()) {
				break;
			}

			bounds = generateBoundingRect(positions, bestZoom);
		}

		setZoom(bestZoom + 1);
	}

	private Rectangle2D generateBoundingRect(final Set<GeoPosition> positions, int zoom) {
		final Point2D point1 = getTileFactory().geoToPixel(positions.iterator().next(), zoom);
		final Rectangle2D rect = new Rectangle2D.Double(point1.getX(), point1.getY(), 0, 0);

		positions.stream().map(pos -> getTileFactory().geoToPixel(pos, zoom)).forEach(rect::add);
		return rect;
	}

	// a property change listener which forces repaints when tiles finish loading
	private final TileListener tileLoadListener = new TileListener() {
		@Override
		public void tileLoaded(Tile tile) {
			if (tile.getZoom() == getZoom()) {
				repaint();
				/*
				 * this optimization doesn't save much and it doesn't work if you wrap around
				 * the world Rectangle viewportBounds = getViewportBounds(); TilePoint tilePoint
				 * = t.getLocation(); Point point = new Point(tilePoint.getX() *
				 * getTileFactory().getTileSize(), tilePoint.getY() *
				 * getTileFactory().getTileSize()); Rectangle tileRect = new Rectangle(point,
				 * new Dimension(getTileFactory().getTileSize(),
				 * getTileFactory().getTileSize())); if (viewportBounds.intersects(tileRect)) {
				 * //convert tileRect from world space to viewport space repaint(new Rectangle(
				 * tileRect.x - viewportBounds.x, tileRect.y - viewportBounds.y, tileRect.width,
				 * tileRect.height )); }
				 */
			}
		}

		@Override
		public void allTilesLoaded() {
			// TODO Auto-generated method stub
		}

		@Override
		public void tilesLoading() {
			// TODO Auto-generated method stub
		}

	};

	/**
	 * @return true if panning is restricted or not
	 */
	public boolean isRestrictOutsidePanning() {
		return restrictOutsidePanning;
	}

	/**
	 * @param restrictOutsidePanning set if panning is restricted or not
	 */
	public void setRestrictOutsidePanning(boolean restrictOutsidePanning) {
		this.restrictOutsidePanning = restrictOutsidePanning;
	}

	/**
	 * @return true if horizontally wrapped or not
	 */
	public boolean isHorizontalWrapped() {
		return horizontalWrapped;
	}

	/**
	 * Side note: This setting is ignored when horizontaklWrapped is set to true.
	 *
	 * @param infiniteMapRendering true when infinite map rendering should be
	 *                             enabled
	 */
	public void setInfiniteMapRendering(boolean infiniteMapRendering) {
		this.infiniteMapRendering = infiniteMapRendering;
	}

	/**
	 * @return true if infinite map rendering is enabled
	 */
	public boolean isInfiniteMapRendering() {
		return horizontalWrapped || infiniteMapRendering;
	}

	/**
	 * @param horizontalWrapped true if horizontal wrap is enabled
	 */
	public void setHorizontalWrapped(boolean horizontalWrapped) {
		this.horizontalWrapped = horizontalWrapped;
	}

	/**
	 * Converts the specified GeoPosition to a point in the JXMapViewer's local
	 * coordinate space. This method is especially useful when drawing lat/long
	 * positions on the map.
	 * 
	 * @param pos a GeoPosition on the map
	 * @return the point in the local coordinate space of the map
	 */
	public Point2D convertGeoPositionToPoint(GeoPosition pos) {
		// convert from geo to world bitmap
		final Point2D pt = getTileFactory().geoToPixel(pos, getZoom());
		// convert from world bitmap to local
		final Rectangle bounds = getViewportBounds();
		return new Point2D.Double(pt.getX() - bounds.getX(), pt.getY() - bounds.getY());
	}

	/**
	 * Converts the specified Point2D in the JXMapViewer's local coordinate space to
	 * a GeoPosition on the map. This method is especially useful for determining
	 * the GeoPosition under the mouse cursor.
	 * 
	 * @param pt a point in the local coordinate space of the map
	 * @return the point converted to a GeoPosition
	 */
	public GeoPosition convertPointToGeoPosition(Point2D pt) {
		// convert from local to world bitmap
		final Rectangle bounds = getViewportBounds();
		final Point2D pt2 = new Point2D.Double(pt.getX() + bounds.getX(), pt.getY() + bounds.getY());

		// convert from world bitmap to geo
		return getTileFactory().pixelToGeo(pt2, getZoom());
	}

	public boolean isNegativeYAllowed() {
		return true;
	}

	/**
	 * Enables or disables panning. Useful for performing selections on the map.
	 * 
	 * @param enabled if true, panning is enabled (the default), if false, panning
	 *                is disabled
	 */
	public void setPanEnabled(boolean enabled) {
		this.panningEnabled = enabled;
	}

	/**
	 * Returns whether panning is enabled. If it is disabled, panning should not
	 * occur. (Used primarily by {@link PanMouseInputListener}
	 * 
	 * @return true if panning is enabled
	 */
	public boolean isPanningEnabled() {
		return this.panningEnabled;
	}
}