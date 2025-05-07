/*
 * Copyright 2006-2009, 2017, 2020 United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 * 
 * The NASA World Wind Java (WWJ) platform is licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 * NASA World Wind Java (WWJ) also contains the following 3rd party Open Source
 * software:
 * 
 *     Jackson Parser – Licensed under Apache 2.0
 *     GDAL – Licensed under MIT
 *     JOGL – Licensed under  Berkeley Software Distribution (BSD)
 *     Gluegen – Licensed under Berkeley Software Distribution (BSD)
 * 
 * A complete listing of 3rd Party software notices and licenses included in
 * NASA World Wind Java (WWJ)  can be found in the WorldWindJava-v2.2 3rd-party
 * notices and licenses PDF found in code directory.
 */

package gov.nasa.worldwindx.signaltrack.measuretool;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;

import java.awt.*;
import java.util.ArrayList;

/**
 * @author tag
 * @version $Id: WWOMeasureToolControlPoints.java 1171 2013-02-11 21:45:02Z
 *          dcollins $
 */
public class WWOMeasureToolControlPoints implements WWOMeasureTool.ControlPointList, Renderable {
	public class ControlPoint extends GlobeAnnotation implements WWOMeasureTool.ControlPoint {
		public ControlPoint(Position position) {
			super("", position, WWOMeasureToolControlPoints.this.controlPointAttributes);
		}

		@Override
		public WWOMeasureTool getParent() {
			return WWOMeasureToolControlPoints.this.measureTool;
		}

		@Override
		public void highlight(boolean tf) {
			this.getAttributes().setHighlighted(tf);
			this.getAttributes().setBackgroundColor(tf ? this.getAttributes().getTextColor() : null);
		}

		@Override
		public synchronized Object setValue(String key, Object value) {
			return super.setValue(key, value);
		}

		@Override
		public synchronized Object getValue(String key) {
			return super.getValue(key);
		}
	}

	protected WWOMeasureTool measureTool;
	protected ArrayList<ControlPoint> points = new ArrayList<>();
	protected AnnotationAttributes controlPointAttributes;

	public WWOMeasureToolControlPoints(WWOMeasureTool measureTool) {
		this.measureTool = measureTool;

		this.controlPointAttributes = new AnnotationAttributes();
		// Define an 8x8 square centered on the screen point
		this.controlPointAttributes.setFrameShape(AVKey.SHAPE_RECTANGLE);
		this.controlPointAttributes.setLeader(AVKey.SHAPE_NONE);
		this.controlPointAttributes.setAdjustWidthToText(AVKey.SIZE_FIXED);
		this.controlPointAttributes.setSize(new Dimension(8, 8));
		this.controlPointAttributes.setDrawOffset(new Point(0, -4));
		this.controlPointAttributes.setInsets(new Insets(0, 0, 0, 0));
		this.controlPointAttributes.setBorderWidth(0);
		this.controlPointAttributes.setCornerRadius(0);
		this.controlPointAttributes.setBackgroundColor(Color.BLUE); // Normal color
		this.controlPointAttributes.setTextColor(Color.GREEN); // Highlighted color
		this.controlPointAttributes.setHighlightScale(1.2);
		this.controlPointAttributes.setDistanceMaxScale(1); // No distance scaling
		this.controlPointAttributes.setDistanceMinScale(1);
		this.controlPointAttributes.setDistanceMinOpacity(1);
	}

	@Override
	public void addToLayer(RenderableLayer layer) {
		layer.addRenderable(this);
	}

	@Override
	public void removeFromLayer(RenderableLayer layer) {
		layer.removeRenderable(this);
	}

	@Override
	public int size() {
		return this.points.size();
	}

	@Override
	public WWOMeasureTool.ControlPoint createControlPoint(Position position) {
		return new ControlPoint(position);
	}

	@Override
	public WWOMeasureTool.ControlPoint get(int index) {
		return this.points.get(index);
	}

	@Override
	public void add(WWOMeasureTool.ControlPoint controlPoint) {
		this.points.add((ControlPoint) controlPoint);
	}

	@Override
	public void remove(WWOMeasureTool.ControlPoint controlPoint) {
		this.points.remove(controlPoint);
	}

	@Override
	public void remove(int index) {
		this.points.remove(index);
	}

	@Override
	public void clear() {
		this.points.clear();
	}

	@Override
	public void render(DrawContext dc) {
		this.points.forEach(cp -> cp.render(dc));
	}
}
