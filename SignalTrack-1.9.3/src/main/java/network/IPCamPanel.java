package network;

import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JPanel;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamEvent;
import com.github.sarxos.webcam.WebcamListener;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamPanel.DrawMode;
import com.github.sarxos.webcam.WebcamResolution;
import com.github.sarxos.webcam.ds.ipcam.IpCamDeviceRegistry;
import com.github.sarxos.webcam.ds.ipcam.IpCamMode;

public class IPCamPanel extends JPanel implements WebcamListener, AutoCloseable {
	public enum CameraEvent {
		OPEN, CLOSED, DISPOSED, IMAGE_OBTAINED
	}

	private static final String CAMERA_EVENT = "CameraEvent";
	private static final Logger LOG = Logger.getLogger(IPCamPanel.class.getName());

	private static final long serialVersionUID = 1L;

	private final URL url;
	private transient Webcam webcam;
	private WebcamPanel webcamPanel;

	public static final boolean DEFAULT_FPS_DISPLAYED = true;
	public static final double DEFAULT_FPS_LIMIT = 1d;
	public static final DrawMode DEFAULT_DRAW_MODE = DrawMode.FIT;

	private final transient ExecutorService executor = Executors.newSingleThreadExecutor();

	public IPCamPanel(URL url) {
		this.url = url;

		executor.execute(new WebcamViewer());
		
		add(webcamPanel);
	}

	private class WebcamViewer implements Runnable {

		@Override
		public void run() {

			IpCamDeviceRegistry.register(url.getPath(), url, IpCamMode.PUSH);

			webcam = Webcam.getWebcams().get(0);
			webcam.setViewSize(WebcamResolution.VGA.getSize());
			webcam.addWebcamListener(IPCamPanel.this);

			webcamPanel = new WebcamPanel(webcam);
			webcamPanel.setFPSLimit(DEFAULT_FPS_LIMIT);
			webcamPanel.setFPSDisplayed(DEFAULT_FPS_DISPLAYED);
			webcamPanel.setDrawMode(DEFAULT_DRAW_MODE);
		}

	}

	public boolean isFpsDisplayed() {
		return webcamPanel.isFPSDisplayed();
	}

	public void setFpsDisplayed(boolean fpsDisplayed) {
		webcamPanel.setFPSDisplayed(fpsDisplayed);
	}

	public double getFpsLimit() {
		return webcamPanel.getFPSLimit();
	}

	public void setFpsLimit(double fpsLimit) {
		webcamPanel.setFPSLimit(fpsLimit);
	}

	public DrawMode getDrawMode() {
		return webcamPanel.getDrawMode();
	}

	public void setDrawMode(DrawMode drawMode) {
		webcamPanel.setDrawMode(drawMode);
	}

	@Override
	public void webcamOpen(WebcamEvent we) {
		firePropertyChange(CAMERA_EVENT, null, CameraEvent.OPEN);
	}

	@Override
	public void webcamClosed(WebcamEvent we) {
		firePropertyChange(CAMERA_EVENT, null, CameraEvent.CLOSED);
	}

	@Override
	public void webcamDisposed(WebcamEvent we) {
		firePropertyChange(CAMERA_EVENT, null, CameraEvent.DISPOSED);
	}

	@Override
	public void webcamImageObtained(WebcamEvent we) {
		firePropertyChange(CAMERA_EVENT, null, CameraEvent.IMAGE_OBTAINED);
	}

	@Override
	public void close() throws Exception {
		if (executor != null) {
			try {
				LOG.log(Level.INFO, "Initializing IPCamPanel executor Service termination....");
				executor.shutdown();
				executor.awaitTermination(3, TimeUnit.SECONDS);
				LOG.log(Level.INFO, "IPCamPanel executor Service has gracefully terminated");
			} catch (InterruptedException e) {
				LOG.log(Level.SEVERE, "IPCamPanel executor Service has timed out after 3 seconds of waiting to terminate processes.");
				Thread.currentThread().interrupt();
			}
		}
	}

}
