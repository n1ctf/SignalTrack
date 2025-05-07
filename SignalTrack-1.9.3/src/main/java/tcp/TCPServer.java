package tcp;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import java.util.Arrays;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ServerSocketFactory;

public class TCPServer implements AutoCloseable {

	public enum Event {
		DATA_RECEIVED, 
		DATA_TRANSMITTED, 
		CONNECTION_ACCEPTED, 
		CONNECTION_DROPPED, 
		DISCONNECT_REQUESTED_BY_CLIENT,
		DISCONNECT_REQUESTED_BY_SERVER
	}

	public static final Logger LOG = Logger.getLogger(TCPServer.class.getName());

	public static final String REPLY_DISCONNECT_GRANT = "REPLY_DISCONNECT_GRANT\n";
	public static final String LOOPBACK_REQUEST = "LOOPBACK";
	public static final String DISCONNECT_REQUEST = "DISCONNECT_REQUEST";
	public static final int DEFAULT_SO_LINGER_TIME_SECONDS = 2;
	public static final int DEFAULT_SO_TIMEOUT_MILLISECONDS = 120000;
	public static final int DEFAULT_SSO_TIMEOUT_MILLISECONDS = 0;
	public static final byte DEFAULT_TRAFFIC_CLASS = 0x1C;
	public static final boolean DEFAULT_ECHO = false;
	public static final int WRITE_QUEUE_SIZE = 1024;
	public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
	public static final boolean DEFAULT_DEBUG_MODE = true;

	private int portNumber;
	private int socketLingerTimeSeconds = DEFAULT_SO_LINGER_TIME_SECONDS;
	private int socketTimeoutMilliSeconds = DEFAULT_SO_TIMEOUT_MILLISECONDS;
	private int serverSocketTimeoutMilliSeconds = DEFAULT_SSO_TIMEOUT_MILLISECONDS;
	private byte trafficClass = DEFAULT_TRAFFIC_CLASS;
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	private boolean echo = DEFAULT_ECHO;
	private boolean allowRequests = true;
	private boolean disconnect;
	private boolean enableRun = false;
	private final Charset charset = DEFAULT_CHARSET;

    private ExecutorService executor;

	private final BlockingQueue<byte[]> writeQueue = new ArrayBlockingQueue<>(WRITE_QUEUE_SIZE);

	public TCPServer(int portNumber) {
		this.portNumber = portNumber;
	}

	public void stop() {
		allowRequests = false;
		writeQueue.clear();
		writeQueue.add(DISCONNECT_REQUEST.getBytes(charset));
		enableRun = false;
	}

	public void start() {
		try {
			if (!enableRun) {
				if (DEFAULT_DEBUG_MODE) {
					LOG.log(Level.INFO, "Opening TCP Server Socket on Port {0}", String.valueOf(portNumber));
				}
				enableRun = true;
				allowRequests = true;
				if (executor == null || executor.isShutdown() || executor.isTerminated()) {
					executor = Executors.newCachedThreadPool();
					executor.execute(new TCPStream());
				}
			} else {
				if (DEFAULT_DEBUG_MODE) {
					LOG.log(Level.INFO, "A TCP connection is already in progress. No action taken.");
				}
			}
		} catch (RejectedExecutionException ex) {
			if (DEFAULT_DEBUG_MODE) {
				LOG.log(Level.INFO, "Unable to Open TCP Server Socket on Port {0}", String.valueOf(portNumber));
			}
		}
	}

	@Override
	public void close() {
		stop();
		if (executor != null) {
			try {
				LOG.log(Level.INFO, "Initializing TCPServer.executor service termination....");
				executor.shutdown();
				executor.awaitTermination(5, TimeUnit.SECONDS);
				LOG.log(Level.INFO, "TCPServer.executor service has gracefully terminated");
			} catch (InterruptedException e) {
				executor.shutdownNow();
				LOG.log(Level.SEVERE, "TCPServer.executor service has timed out after 5 seconds of waiting to terminate processes.");
				Thread.currentThread().interrupt();
			}
		}
	}

	public int getPortNumber() {
		return portNumber;
	}

	public void setEcho(boolean echo) {
		this.echo = echo;
	}

	public void setSocketLingerTimeSeconds(int socketLingerTimeSeconds) {
		this.socketLingerTimeSeconds = socketLingerTimeSeconds;
	}

	public void setSocketTimeoutMilliSeconds(int socketTimeoutMilliSeconds) {
		this.socketTimeoutMilliSeconds = socketTimeoutMilliSeconds;
	}

	public void setServerSocketTimeoutMilliSeconds(int serverSocketTimeoutMilliSeconds) {
		this.serverSocketTimeoutMilliSeconds = serverSocketTimeoutMilliSeconds;
	}

	public void setTrafficClass(byte trafficClass) {
		this.trafficClass = trafficClass;
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}

	public boolean isPropertyChangeListenerRegistered(PropertyChangeListener listener) {
		boolean isRegistered = false;
		final PropertyChangeListener[] pcls = pcs.getPropertyChangeListeners();
		for (final PropertyChangeListener pcl : pcls) {
			if (listener.equals(pcl)) {
				isRegistered = true;
				break;
			}
		}
		return isRegistered;
	}

	public PropertyChangeListener[] getPropertyChangeListeners() {
		return pcs.getPropertyChangeListeners();
	}

	private class TCPStream implements Runnable {

		@Override
		public void run() {
			try (ServerSocket serverSocket = ServerSocketFactory.getDefault().createServerSocket(portNumber)) {
				serverSocket.setReuseAddress(true);
				serverSocket.setSoTimeout(serverSocketTimeoutMilliSeconds);
				while (enableRun) {
					LOG.log(Level.INFO, "Waiting for TCP Client to Request Connection on Port {0}", String.valueOf(serverSocket.getLocalPort()));
					try {
						final Socket socket = serverSocket.accept();
						socket.setSoLinger(false, socketLingerTimeSeconds);
						socket.setSoTimeout(socketTimeoutMilliSeconds);
						socket.setTrafficClass(trafficClass);
						socket.setReuseAddress(true);
						socket.setKeepAlive(true);
						LOG.log(Level.INFO, "TCP Connection Request was Accepted from Client at {0}:{1}", 
							new Object[] { 
								socket.getInetAddress(), 
								String.valueOf(socket.getPort())
							});
						executor.execute(new InputStream(socket));
						executor.execute(new OutputStream(socket));
						pcs.firePropertyChange(Event.CONNECTION_ACCEPTED.name(), null, socket.getInetAddress());
					} catch (SocketTimeoutException | SocketException ex) {
						LOG.log(Level.SEVERE, ex.getMessage(), ex);
					}  catch (IOException ex) {
						LOG.log(Level.SEVERE, ex.getMessage(), ex);
					}
				}
			} catch (IOException ex) {
				LOG.log(Level.SEVERE, ex.getMessage(), ex);
			}
		}
	}

	private class InputStream implements Runnable {

		private final Socket socket;

		public InputStream(Socket socket) {
			this.socket = socket;
		}

		@Override
		public synchronized void run() {
			try (DataInputStream dis = new DataInputStream(socket.getInputStream())) {

				while (enableRun) {
					LOG.log(Level.INFO, "Waiting for Data from TCP Client on: {0}", socket.getInetAddress().getHostAddress());
					
					final StringBuilder sb = new StringBuilder();
					
					while (socket.isConnected()) {
						final int avail = dis.available();
						
						if (avail > 0) {
							final byte b = dis.readByte();
							sb.append(new String(new byte[] {b}));

							if (b == 13 || b == 10 || b == -1 || disconnect) {
								break;
							}	
						} else {
							TimeUnit.MILLISECONDS.sleep(10);
						}
					}
					
					if (disconnect) {
						break;
					}
					
					pcs.firePropertyChange(Event.DATA_RECEIVED.name(), null, sb.toString());
					
					if (sb.toString().contains(LOOPBACK_REQUEST)) {
						LOG.log(Level.INFO, "LOOPBACK_REQUEST Received from TCP Client at: {0}", socket.getInetAddress().getHostAddress());
						write(sb.toString().getBytes(charset));
					} else if (sb.toString().contains(DISCONNECT_REQUEST)) {
						LOG.log(Level.INFO, "Disconnect Requested by TCP Client at: {0}", socket.getInetAddress().getHostAddress());
						pcs.firePropertyChange(Event.DISCONNECT_REQUESTED_BY_CLIENT.name(), null, socket.getInetAddress());
						write(REPLY_DISCONNECT_GRANT.getBytes(charset));
					} else if (echo) {
						final String s = sb.toString().substring(0, sb.toString().length() - 1) + " -> ";
						write(s.getBytes(charset));
					}	
					LOG.log(Level.INFO, "Data received from TCP client at: {0} -> {1}", new Object[] { socket.getInetAddress().getHostAddress(), sb });
				}
				socket.close();
			} catch (InterruptedException ex) {
				LOG.log(Level.INFO, null, ex);
				Thread.currentThread().interrupt();
			} catch (SocketException ex) {
				LOG.log(Level.INFO, "SocketException with TCP Client connection at: {0}, Message: {1}", 
						new Object[] {socket.getInetAddress().getHostAddress(), ex.getMessage()});
			} catch (IOException ex) {
				LOG.log(Level.INFO, "I/O Exception with TCP Client connection at: {0}, Message: {1}", 
						new Object[] {socket.getInetAddress().getHostAddress(), ex.getMessage()});
			} finally {
				LOG.log(Level.INFO, "Input Stream Connection to TCP Client at: {0} is Released", socket.getInetAddress().getHostAddress());
				pcs.firePropertyChange(Event.CONNECTION_DROPPED.name(), null, socket.getInetAddress());
			}
		}
	}

	private class OutputStream implements Runnable {

		private final Socket socket;

		public OutputStream(Socket socket) {
			this.socket = socket;
		}

		@Override
		public synchronized void run() {
			try (DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {

				while (enableRun) {

					if (DEFAULT_DEBUG_MODE) {
						if (writeQueue.isEmpty()) {
							LOG.log(Level.INFO, "Blocking queue is waiting for an object to send... ");
						} else {
							LOG.log(Level.INFO, "There are {0} additional objects in the blocking queue... ", writeQueue.size());
						}
					}
					
					final byte[] data = writeQueue.take();
					
					if (new String(data, charset).contains(DISCONNECT_REQUEST)) { // disconnect requested by this server
						LOG.log(Level.INFO, "Disconnect Requested by TCP Server at: {0}", socket.getInetAddress().getHostAddress());
						pcs.firePropertyChange(Event.DISCONNECT_REQUESTED_BY_SERVER.name(), null, socket.getInetAddress());
						break;
					}

					if (DEFAULT_DEBUG_MODE) {
						LOG.log(Level.INFO, "Data taken from queue for processing: {0} Remaining objects in queue: {1}",
							new Object[] { 
								new String(data, charset), 
								writeQueue.size()
							});
					}
					
					if (socket.isConnected() && !socket.isClosed()) {
						dos.write(data);
						dos.flush();
						
						pcs.firePropertyChange(Event.DATA_TRANSMITTED.name(), null, new String(data, charset));
						
						if (new String(data, charset).contains(REPLY_DISCONNECT_GRANT)) { // disconnect requested by client
							LOG.log(Level.INFO, "Server disconnected at request of client: {0}", socket.getInetAddress().getHostAddress());
							break;
						}
						
						if (DEFAULT_DEBUG_MODE) {
							LOG.log(Level.INFO, "TCP Data {0} sent to {1} via port {2}",
									new Object[] { new String(data, charset), socket.getInetAddress(),
											String.valueOf(socket.getLocalPort()) });
						}
					} else {
						if (DEFAULT_DEBUG_MODE) {
							LOG.log(Level.INFO, "Unable to Write to TCP Socket - socket {0} connected and socket {1} closed",
								new Object[] { socket.isConnected() ? "is" : "is not", socket.isClosed() ? "is" : "is not" });
						}
					}
				}
				socket.close();
			} catch (SocketException ex) {
				LOG.log(Level.INFO, "SocketException on TCP Client at: {0}", socket.getInetAddress().getHostAddress());
			} catch (IOException ex) {
				LOG.log(Level.INFO, "I/O Exeption on TCP Client at: {0}", socket.getInetAddress().getHostAddress());
			} catch (InterruptedException e) {
				LOG.log(Level.INFO, "InterruptedException on TCP Client");
				Thread.currentThread().interrupt();
			} finally {
				LOG.log(Level.INFO, "Output Stream Connection to TCP Client at: {0} is Released", socket.getInetAddress().getHostAddress());
				pcs.firePropertyChange(Event.CONNECTION_DROPPED.name(), null, socket.getInetAddress());
			}
		}
	}


	public void write(String str) {
		write(str.getBytes(charset));
	}
	
	public void write(byte[] byteArray) {
		if (allowRequests) {
			if (writeQueue.size() < WRITE_QUEUE_SIZE) {
				writeQueue.add(byteArray);
			} else {
				if (DEFAULT_DEBUG_MODE) {
					LOG.log(Level.WARNING, "TCP server writeQueue is full... Requests to write \"{0}\" has been rejected.",
						new Object[] {
							Arrays.toString(byteArray),
						}
					);
				}
			}
		}
	}

}
