package gov.nasa.worldwindx.signaltrack;

import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Timer;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.SurfacePolygon;

public class GeoTile extends SurfacePolygon {
	private Position position;
	private Point2D arcSeconds;
	private List<Position> positions;
	private int id;
	private ShapeAttributes attrs;
	private ShapeAttributes tempAttrs;
	private ShapeAttributes flashAttrs;
    private boolean flash = false;
    private final ActionListener flashTimerActionListener = _ -> flashTimerActionListenerEvent();
    private final Timer flashTimer = new Timer(250, flashTimerActionListener);
    
	public GeoTile(final Point2D lonLat, final Point2D arcSeconds) {
		this(new Position(LatLon.fromDegrees(lonLat.getY(), lonLat.getX()), 0), arcSeconds);
	}

	public GeoTile(final Position position, final Point2D arcSeconds) {
		this.position = position;
		this.arcSeconds = arcSeconds;
		positions = createPositionsIterable(position, arcSeconds);
		super.setOuterBoundary(positions);
	}
	
	private List<Position> createPositionsIterable(final Position position, final Point2D arcSeconds) {
		final List<Position> p = new ArrayList<>(4);
		
		p.add(Position.fromDegrees(position.latitude.getDegrees(), 
				position.longitude.getDegrees()));
		
		p.add(Position.fromDegrees(position.latitude.getDegrees() + (arcSeconds.getY() / 3600d), 
				position.longitude.getDegrees()));
		
		p.add(Position.fromDegrees(position.latitude.getDegrees() + (arcSeconds.getY() / 3600d), 
				position.longitude.getDegrees() + (arcSeconds.getX() / 3600d)));
		
		p.add(Position.fromDegrees(position.latitude.getDegrees(), 
				position.longitude.getDegrees() + (arcSeconds.getX() / 3600d)));
		
		return p;
	}
	
	public List<Position> getPositions() {
		return positions;
	}
	
	public Position getPosition() {
		return position;
	}

	public Point2D getPoint() {
		return new Point.Double(position.longitude.degrees, position.latitude.degrees);
	}
	
	public Point2D getArcSeconds() {
		return arcSeconds;
	}

	public double getLowerLatitude() {
		return position.latitude.getDegrees();
	}
	
	public double getUpperLatitude() {
		return position.latitude.getDegrees() + (arcSeconds.getY() / 3600d);
	}
	
	public double getLeftLongitude() {
		return position.longitude.getDegrees();
	}
	
	public double getRightLongitude() {
		return position.longitude.getDegrees() + (arcSeconds.getX() / 3600d);
	}

	public void setID(int id) {
		this.id = id;
	}
	
	public int getID() {
		return id;
	}

	@Override
	public ShapeAttributes getAttributes() {
		return attrs;
	}

	@Override
	public void setAttributes(ShapeAttributes attrs) {
		super.setAttributes(attrs);
		this.attrs = attrs;
	}

	public void setFlashAttributes(ShapeAttributes flashAttrs) {
		this.flashAttrs = flashAttrs;
	}

	public boolean isFlash() {
        return flash;
    }

    public void setFlash(boolean flash) {
        this.flash = flash;
        if (flash) {
        	this.tempAttrs = super.getAttributes();
        	this.flashTimer.setRepeats(true);
	        this.flashTimer.setInitialDelay(50);
	        this.flashTimer.start();
        } else {
        	this.flashTimer.stop();
        	super.setAttributes(tempAttrs);
        }
    }
	
	private void flashTimerActionListenerEvent() {
		final Thread thread = new Thread(new FlashTimerAction());
		thread.start();
	}
    
	private class FlashTimerAction implements Runnable {
		@Override
		public void run() {
			GeoTile.super.setAttributes(GeoTile.super.getAttributes().equals(tempAttrs) ? flashAttrs : tempAttrs);
			firePropertyChange(AVKey.LAYER, null, this);
		}
	}
}
