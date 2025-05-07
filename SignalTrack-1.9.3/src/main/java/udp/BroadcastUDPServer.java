package udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.InterfaceAddress;

public class BroadcastUDPServer implements AutoCloseable {

	public static final int WRITE_QUEUE_SIZE = 1024;
	public static final Logger LOG = Logger.getLogger(BroadcastUDPServer.class.getName());

	private ExecutorService executor;
	private boolean enable;
	private final BlockingQueue<String> writeQueue = new ArrayBlockingQueue<>(WRITE_QUEUE_SIZE);

	public void start() {
		writeQueue.clear();
		enable = true;
		if (executor == null || executor.isShutdown() || executor.isTerminated()) {
			executor = Executors.newCachedThreadPool();
			executor.execute(new Broadcaster());
		}
	}

	public synchronized void write(String data) {
		if (writeQueue.size() < WRITE_QUEUE_SIZE) {
			LOG.log(Level.WARNING, "Data Queued for Writing: \"{0}\"", data);
			writeQueue.add(data);
		} else {
			LOG.log(Level.WARNING, "WriteQueue is Full... Requests to Write \"{0}\" is Rejected", data);
		}
	}

	private class Broadcaster implements Runnable {

		@Override
		public void run() {
			while (enable) {
				try {
					final String message = writeQueue.take();
					listAllBroadcastAddresses().forEach(address -> broadcast(message, address));
				} catch (InterruptedException ex) {
					LOG.log(Level.WARNING, ex.getMessage(), ex);
					Thread.currentThread().interrupt();
				}
			}
		}

		private void broadcast(String broadcastMessage, InetAddress address) {
			try (DatagramSocket socket = new DatagramSocket()) {
				socket.setBroadcast(true);
				final byte[] buffer = broadcastMessage.getBytes();
				final DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, 4445);
				socket.send(packet);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		private List<InetAddress> listAllBroadcastAddresses() {
			final List<InetAddress> broadcastList = new ArrayList<>();
			try {
				final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
				while (interfaces.hasMoreElements()) {
					final NetworkInterface networkInterface = interfaces.nextElement();

					if (networkInterface.isLoopback() || !networkInterface.isUp()) {
						continue;
					}

					networkInterface.getInterfaceAddresses().stream().map(InterfaceAddress::getBroadcast)
							.filter(Objects::nonNull).forEach(broadcastList::add);
				}
			} catch (SocketException ex) {
				ex.printStackTrace();
			}
			return broadcastList;
		}
	}

	@Override
	public void close() throws Exception {
		if (executor != null) {
			try {
				LOG.log(Level.INFO, "Initializing BroadcastUDPServer executor Service termination....");
				executor.shutdown();
				executor.awaitTermination(5, TimeUnit.SECONDS);
				LOG.log(Level.INFO, "BroadcastUDPServer executor Service has gracefully terminated");
			} catch (InterruptedException e) {
				executor.shutdownNow();
				LOG.log(Level.SEVERE, "BroadcastUDPServer executor Service has timed out after 5 seconds of waiting to terminate processes.");
				Thread.currentThread().interrupt();
			}
		}
	}
}
