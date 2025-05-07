package tcp;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TCPOutputStream implements Runnable {
	private final Socket socket;
	private static final Logger LOG = Logger.getLogger(TCPOutputStream.class.getName());
	private static final BlockingQueue<String> itemsToWrite = new ArrayBlockingQueue<>(1024);
	
	public TCPOutputStream(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		try (DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream())) {
			while (socket.isConnected()) {
				LOG.log(Level.INFO, "Waiting for Data to Transmit...");
				final String data = itemsToWrite.take();
				outputStream.writeBytes(data + '\n');
				LOG.log(Level.INFO, "Data Transmitted: {0}", data);
			}
		} catch (SocketException ex) {
			LOG.log(Level.INFO, "Conection Closed by Server");	
		} catch (final InterruptedException ex) {
			ex.printStackTrace();
			Thread.currentThread().interrupt();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			itemsToWrite.clear();
		}
	}
	
	public static void clearItemsToWrite() {
		itemsToWrite.clear();
	}
	
	public static void queueOutgoingMessage(String data) {
		try {
			itemsToWrite.put(data);
			LOG.log(Level.INFO, "Data Queued for Transmission: {0}", data);
		} catch (final InterruptedException ex) {
			ex.printStackTrace();
			Thread.currentThread().interrupt();
		}
	}
}
