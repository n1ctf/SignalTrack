
package org.jxmapviewer.input;

import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;

import org.jxmapviewer.JXMapViewer;

/**
 * used to pan using the arrow keys
 * 
 * @author joshy
 */
public class PanKeyListener extends KeyAdapter {
	private static final int OFFSET = 10;

	private JXMapViewer viewer;

	/**
	 * @param viewer the jxmapviewer
	 */
	public PanKeyListener(JXMapViewer viewer) {
		this.viewer = viewer;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int deltaX = 0;
		int deltaY = 0;

		switch (e.getKeyCode()) {
			case KeyEvent.VK_LEFT:
				deltaX = -OFFSET;
				break;
			case KeyEvent.VK_RIGHT:
				deltaX = OFFSET;
				break;
			case KeyEvent.VK_UP:
				deltaY = -OFFSET;
				break;
			case KeyEvent.VK_DOWN:
				deltaY = OFFSET;
				break;
			default:
				break;
		}

		if (deltaX != 0 || deltaY != 0) {
			Rectangle bounds = viewer.getViewportBounds();
			double x = bounds.getCenterX() + deltaX;
			double y = bounds.getCenterY() + deltaY;
			viewer.setCenter(new Point2D.Double(x, y));
			viewer.repaint();
		}
	}
}
