package gov.nasa.worldwindx.signaltrack;

import java.awt.Color;
import java.awt.geom.Point2D;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.SurfacePolyline;

public class TestGridLayer extends RenderableLayer {
	private BasicShapeAttributes attributes;
	private Point2D tileSize;
	private Point2D gridReference;
	private Point2D gridSize;
	private Color color = Color.RED;

	public TestGridLayer() {
		configureDefaultAttributes();
	}

	public TestGridLayer(Point2D tileSize, Point2D gridReference, Point2D gridSize, Color color) {
		this.tileSize = tileSize;
		this.gridReference = gridReference;
		this.gridSize = gridSize;
		this.color = color;
		configureDefaultAttributes();
		buildGrid(tileSize, gridReference, gridSize, color);
	}

	private void configureDefaultAttributes() {
		attributes = new BasicShapeAttributes();
		attributes.setOutlineWidth(1);
		attributes.setInteriorOpacity(0.0);
		attributes.setOutlineOpacity(0.8);
		attributes.setEnableAntialiasing(true);
		attributes.setOutlineMaterial(new Material(color));
	}

	private void buildGrid(Point2D tileSize, Point2D gridReference, Point2D gridSize, Color color) {
		if ((tileSize == null) || (gridReference == null) || (gridSize == null) || (color == null)) {
			return;
		}
		attributes.setOutlineMaterial(new Material(color));

		// add vertical lines
		for (double v = gridReference.getX(); 
				v < (0.0001 + gridReference.getX() + gridSize.getX()); 
				v += (tileSize.getX() / 3600d)) {
			
			final List<LatLon> list = Collections.synchronizedList(new LinkedList<>());
			list.add(LatLon.fromDegrees(gridReference.getY(), v));
			list.add(LatLon.fromDegrees(gridReference.getY() - gridSize.getY(), v));
			final SurfacePolyline gridPolyline = new SurfacePolyline(list);
			gridPolyline.setAttributes(attributes);
			addRenderable(gridPolyline);
		}
		
		// add horizontal lines
		for (double h = gridReference.getY(); 
				h > ((gridReference.getY() - 0.0001) - gridSize.getY());
				h -= (tileSize.getY() / 3600d)) {
			
			final List<LatLon> list = Collections.synchronizedList(new LinkedList<>());
			list.add(LatLon.fromDegrees(h, gridReference.getX()));
			list.add(LatLon.fromDegrees(h, gridReference.getX() + gridSize.getX()));
			final SurfacePolyline gridPolyline = new SurfacePolyline(list);
			gridPolyline.setAttributes(attributes);
			addRenderable(gridPolyline);
		}
	}

	public BasicShapeAttributes getAttributes() {
		return attributes;
	}

	public void setAttributes(BasicShapeAttributes attributes) {
		this.attributes = attributes;
	}

	public void setTestGridParameters(Point2D tileSize, Point2D gridReference, Point2D gridSize, Color color) {
		this.tileSize = tileSize;
		this.gridReference = gridReference;
		this.gridSize = gridSize;
		this.color = color;
		removeAllRenderables();
		buildGrid(tileSize, gridReference, gridSize, color);
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public Point2D getTileSize() {
		return tileSize;
	}

	public Point2D getGridReference() {
		return gridReference;
	}

	public Point2D getGridSize() {
		return gridSize;
	}

	public void setTileSize(Point2D tileSize) {
		this.tileSize = tileSize;
		removeAllRenderables();
		buildGrid(tileSize, gridReference, gridSize, color);
	}

	public void setGridReference(Point2D gridReference) {
		this.gridReference = gridReference;
		removeAllRenderables();
		buildGrid(tileSize, gridReference, gridSize, color);
	}

	public void setGridSize(Point2D gridSize) {
		this.gridSize = gridSize;
		removeAllRenderables();
		buildGrid(tileSize, gridReference, gridSize, color);
	}

	public void setGrid(Point2D tileSize, Point2D gridReference, Point2D gridSize) {
		removeAllRenderables();
		this.tileSize = tileSize;
		this.gridReference = gridReference;
		this.gridSize = gridSize;
		buildGrid(tileSize, gridReference, gridSize, color);
	}

	public void setGridColor(Color color) {
		this.color = color;
		removeAllRenderables();
		buildGrid(tileSize, gridReference, gridSize, color);
	}

	public void repaint() {
		removeAllRenderables();
		buildGrid(tileSize, gridReference, gridSize, color);
	}

}
