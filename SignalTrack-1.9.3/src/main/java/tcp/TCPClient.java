package tcp;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import java.time.ZonedDateTime;

import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import java.util.logging.Level;
import java.util.logging.Logger;

public class TCPClient implements AutoCloseable {

	public enum Event {
		DATA_RECEIVED, DATA_TRANSMITTED, CONNECTION_ACCEPTED, CONNECTION_DROPPED, PING_FAILURE
	}

	public static final String DISCONNECT_REQUEST = "DISCONNECT_REQUEST";
	public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
	public static final int WRITE_QUEUE_CAPACITY = 1024;
	public static final int SOCKET_CONNECT_TIMEOUT = 15000;  // milliseconds
	public static final int SOCKET_CONNECT_TIMEOUT_RETRY = 5000;  // milliseconds
	public static final int TERMINATION_WAIT_PERIOD = 5000; // milliseconds

	private static final Logger LOG = Logger.getLogger(TCPClient.class.getName());

	private boolean connected;
	private Timer timer;
	private boolean enableRun;				// the outermost connection retry loop
	private boolean drop;					// the instruction to force drop an active connection and go back to the retry loop
	private boolean allowWriteRequests;
	private boolean queueReconnect;			// a flag that instructs the disconnect method to reconnect after disconnection

	private ExecutorService socketExecutor = Executors.newSingleThreadExecutor();  	// the primary run loop
	
	private ExecutorService writerExecutor = Executors.newSingleThreadExecutor();  	// the write queue loop
	
	private InetAddress inetAddress;
	private int portNumber;
	
	private final BlockingQueue<Object> writeQueue = new ArrayBlockingQueue<>(WRITE_QUEUE_CAPACITY);
	private Charset charset = DEFAULT_CHARSET;
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	public TCPClient() {
		this(DEFAULT_CHARSET);
	}

	public TCPClient(Charset charset) {
		this.charset = charset;

		LOG.log(Level.INFO, "TCPClient class has been instantiated at: {0}", ZonedDateTime.now());

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				close();
			}
		});
	}

	private final class TCPStream implements Runnable {
		private final int portNumber;
		private final InetAddress inetAddress;
		
		private TCPStream(InetAddress inetAddress, int portNumber) {
			this.inetAddress = inetAddress;
			this.portNumber = portNumber;
		}

		@Override
		public void run() {
			LOG.log(Level.INFO, "TCPStream thread has been instantaited for server at TCP/IP Address {0}:{1}",
					new Object[] { inetAddress, String.valueOf(portNumber) });
			
			// a Runnable loop that runs until force connected by the instantiating class
			while (enableRun) {
				
				allowWriteRequests = false;
				
				try (Socket socket = new Socket()) {
					LOG.log(Level.INFO, "Connecting to TCP server at TCP/IP Address {0}:{1}",
							new Object[] {inetAddress.getCanonicalHostName(), String.valueOf(portNumber)});
					
					socket.setReuseAddress(true);
					socket.setKeepAlive(false);
					socket.connect(new InetSocketAddress(inetAddress, portNumber), SOCKET_CONNECT_TIMEOUT);

					pcs.firePropertyChange(TCPClient.Event.CONNECTION_ACCEPTED.name(), null, socket.getRemoteSocketAddress());
					
					connected = true;
					
					LOG.log(Level.INFO, "Connection Accepted by Server at TCP/IP Address {0}:{1} using local port {2}",
							new Object[] { inetAddress.getCanonicalHostName(), String.valueOf(portNumber),
									String.valueOf(socket.getLocalPort()) });

					try (DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
							DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream())) {
						
						allowWriteRequests = true;
						
						if (socket.isConnected() && !socket.isClosed() && enableRun) {
							writerExecutor.execute(new Writer(dataOutputStream));
						}

						// the main reader loop that runs until the socket dies or enableRun forces the Runnable to end
						while (socket.isConnected() && !socket.isClosed() && enableRun) {
							final StringBuilder bld = new StringBuilder();                                                            
							
							// read a line, byte by byte, until a CR or LF or -1 (end of stream) or a forced drop
							while (socket.isConnected() && !socket.isClosed() && enableRun) {
								
								final int avail = dataInputStream.available();
								
								if (avail > 0) {
									final byte b = dataInputStream.readByte();
									bld.append(new String(new byte[] {b}));

									if (b == 13 || b == 10 || b == -1 || drop || !enableRun) {
										break;
									}	
								} else {
									TimeUnit.MILLISECONDS.sleep(5);
								}
							}
							
							// stay in the read loop until there is a forced drop
							if (drop || !enableRun) {
								break;
							}
							
							resetPing(inetAddress);
							
							LOG.log(Level.INFO, "TCP Client Input Stream: {0}", bld);
							pcs.firePropertyChange(Event.DATA_RECEIVED.name(), null, bld.toString());
						}
						
						pcs.firePropertyChange(TCPClient.Event.CONNECTION_DROPPED.name(), connected, false);
						
						connected = false;
						
						if (timer != null) {
			    			timer.cancel();
			    			timer.purge();
			    		}
						
					} catch (NoRouteToHostException e) {
						try {
							allowWriteRequests = false;
							writeQueue.clear();
							connected = false;
							Thread.sleep(SOCKET_CONNECT_TIMEOUT_RETRY);
							LOG.log(Level.SEVERE, "No route to TCP server at IP address {0}:{1}", new Object[]{inetAddress, portNumber});
						} catch (InterruptedException ex) {
							LOG.log(Level.SEVERE, ex.getMessage());
							enableRun = false;
							Thread.currentThread().interrupt();
						}						
					} catch (IOException ex) {
						LOG.log(Level.SEVERE, ex.getMessage());
					} catch (InterruptedException ex) {
						LOG.log(Level.SEVERE, ex.getMessage());
						enableRun = false;
						Thread.currentThread().interrupt();
					}	
				} catch (NoRouteToHostException e) {
					try {
						allowWriteRequests = false;
						writeQueue.clear();
						connected = false;
						Thread.sleep(SOCKET_CONNECT_TIMEOUT_RETRY);
						LOG.log(Level.SEVERE, "No route to TCP server at IP address {0}:{1}", new Object[]{inetAddress, portNumber});
					} catch (InterruptedException ex) {
						LOG.log(Level.SEVERE, ex.getMessage());
						enableRun = false;
						Thread.currentThread().interrupt();
					}						
				} catch (SocketTimeoutException e) {
					try {
						allowWriteRequests = false;
						writeQueue.clear();
						pcs.firePropertyChange(TCPClient.Event.CONNECTION_DROPPED.name(), connected, false);
						connected = false;
						Thread.sleep(SOCKET_CONNECT_TIMEOUT_RETRY);
						LOG.log(Level.INFO, "Socket timed out waiting for server. Retrying connection now...");
					} catch (InterruptedException ex) {
						LOG.log(Level.SEVERE, ex.getMessage());
						enableRun = false;
						Thread.currentThread().interrupt();
					}
				} catch (IllegalArgumentException ex) {
					LOG.log(Level.INFO, null, ex);
					enableRun = false;
				} catch (NullPointerException ex) {
					LOG.log(Level.INFO, "TCP/IP Address is NULL");
					enableRun = false;
				} catch (ConnectException ex) {
					try {
						allowWriteRequests = false;
						writeQueue.clear();
						pcs.firePropertyChange(TCPClient.Event.CONNECTION_DROPPED.name(), connected, false);
						connected = false; 
						Thread.sleep(SOCKET_CONNECT_TIMEOUT_RETRY);
						LOG.log(Level.INFO, "Server has rejeccted connection request. Retrying now...");
					} catch (InterruptedException e) {
						LOG.log(Level.SEVERE, "InterruptedException in TCPStream loop", ex);
						enableRun = false;
						Thread.currentThread().interrupt();
					}
				} catch (IOException ex) {
					LOG.log(Level.INFO, "IOException in TCPStream loop", ex);
					enableRun = false;
				}
			}
			if (timer != null) {
    			timer.cancel();
    			timer.purge();
    		}
			pcs.firePropertyChange(TCPClient.Event.CONNECTION_DROPPED.name(), connected, false);
			connected = false;
			enableRun = false;
			allowWriteRequests = false;
			writeQueue.clear();
			writerExecutorDisconnect();
			writeQueue.add(DISCONNECT_REQUEST); // Adds a poison pill entry to the writeQueue in case it is blocking 
		}
		
		private void resetPing(InetAddress address) {
			try {
				if (timer != null) {
	    			timer.cancel();
	    			timer.purge();
	    		}
	    		timer = new Timer();
	    		LOG.log(Level.INFO, "Data received from server at {0}.  Resetting Ping Timer for 10 seconds...", address.getCanonicalHostName());
	            timer.schedule(new TimerTask() {
					@Override
					public void run() {
						LOG.log(Level.INFO, "Nothing has been received over the TCP socket for 10 seconds. Pinging the server at {0}", address.getCanonicalHostName());
						new PingTester(address);
					}
				}, 10000);
			} catch (IllegalStateException ex) {
				LOG.log(Level.WARNING, "PingTimer", ex);
			}
		}
	}
	
	/**
	 * @return the connected
	 */
	public boolean isConnected() {
		return connected;
	}

	/**
	 * @return the inetAddress
	 */
	public InetAddress getInetAddress() {
		return inetAddress;
	}

	/**
	 * @return the portNumber
	 */
	public int getPortNumber() {
		return portNumber;
	}

	public synchronized void write(byte[] b) {
		if (allowWriteRequests) {
			LOG.log(Level.INFO, "Byte array {0} has been added to the WriteQueue using charset {1}", 
					new Object[] { new String(b, charset), charset.displayName() });
			String data = new String(b, charset);
			if (!data.contains("\n")) {
				data += "\n";
			}
			writeQueue.add(data.getBytes(charset));
		} else {
			LOG.log(Level.INFO, "Write request rejected: {0}", new String(b, charset));
		}
	}
	
	public synchronized void write(byte[] b, Charset charset) {
		if (allowWriteRequests) {
			LOG.log(Level.INFO, "Byte array {0} has been added to the WriteQueue using charset {1}", 
				new Object[] { new String(b, charset), charset.displayName() });
			String data = new String(b, charset);
			if (!data.contains("\n")) {
				data += "\n";
			}
			writeQueue.add(data.getBytes(charset));
		} else {
			LOG.log(Level.INFO, "Write request rejected: {0}", new String(b, charset));
		}
	}
	
	public synchronized void write(String[] sa) {
		for (String s : sa) {
			write(s);
		}
	}
	
	public synchronized void write(String str) {
		if (allowWriteRequests) {
			final String[] sa = str.split("\n");
			for (String s : sa) {
				if (!s.contains("\n")) {
					LOG.log(Level.INFO, "String {0} has been added to the WriteQueue and CRLF has been appended", s);
					s += "\n";
				} else {
					LOG.log(Level.INFO, "String {0} has been added to the WriteQueue", s);
				}
				writeQueue.add(s);
			}
		} else {
			LOG.log(Level.INFO, "Write request rejected: {0}", str);
		}
	}

	public synchronized void write(Byte b) {
		if (allowWriteRequests) {
			LOG.log(Level.INFO, "Byte {0} has been added to the WriteQueue", new String(new byte[] {b}, charset));
			writeQueue.add(b);
		} else {
			LOG.log(Level.INFO, "Write request rejected: {0}", b);
		}
	}

	public synchronized void write(Integer i) {
		if (allowWriteRequests) {
			LOG.log(Level.INFO, "Integer {0} has been added to the WriteQueue", Integer.toString(i).charAt(0));
			writeQueue.add(i);
		} else {
			LOG.log(Level.INFO, "Write request rejected: {0}", i);
		}
	}
	
	public synchronized void write(Long l) {
		if (allowWriteRequests) {
			LOG.log(Level.INFO, "Long {0} has been added to the WriteQueue", Long.toString(l));
			writeQueue.add(l);
		} else {
			LOG.log(Level.INFO, "Write request rejected: {0}", l);
		}
	}
	
	public synchronized void write(Float f) {
		if (allowWriteRequests) {
			LOG.log(Level.INFO, "Float {0} has been added to the WriteQueue", Float.toString(f));
			writeQueue.add(f);
		} else {
			LOG.log(Level.INFO, "Write request rejected: {0}", f);
		}
	}
	
	public synchronized void write(Boolean b) {
		if (allowWriteRequests) {
			LOG.log(Level.INFO, "Boolean {0} has been added to the WriteQueue", Boolean.toString(b));
			writeQueue.add(b);
		} else {
			LOG.log(Level.INFO, "Write request rejected: {0}", b);
		}
	}
	
	public synchronized void write(Character c) {
		if (allowWriteRequests) {
			LOG.log(Level.INFO, "Character {0} has been added to the WriteQueue", Character.toString(c));
			writeQueue.add(c);
		} else {
			LOG.log(Level.INFO, "Write request rejected: {0}", c);
		}
	}
	
	public synchronized void write(Double d) {
		if (allowWriteRequests) {
			LOG.log(Level.INFO, "Double {0} has been added to the WriteQueue", Double.toString(d));
			writeQueue.add(d);
		} else {
			LOG.log(Level.INFO, "Write request rejected: {0}", d);
		}
	}

	private final class Writer implements Runnable {
		private final DataOutputStream outputStream;

		private Writer(DataOutputStream outputStream) {
			this.outputStream = outputStream;
		}

		@Override
		public void run() {
			LOG.log(Level.INFO, "TCPClient.Writer has been instantianted");
			try {
				while (enableRun && allowWriteRequests) {
					final Object object = writeQueue.take();
					String info = "";
					TimeUnit.MILLISECONDS.sleep(100);
					try {
						if (outputStream != null) {
							if (object instanceof String str) {
								outputStream.writeChars(str);
								info = str;
							} else if (object instanceof Byte b) {
								outputStream.write(b);
								info = new String(new byte[] {b}, charset);
							} else if (object instanceof byte[] ba) {
								outputStream.write(ba);
								info = new String(ba, charset);
							} else if (object instanceof Integer i) {
								outputStream.writeInt(i);
								info = String.valueOf(i);
							} else if (object instanceof Long l) {
								outputStream.writeLong(l);
								info = String.valueOf(l);
							} else if (object instanceof Float f) {
								outputStream.writeFloat(f);
								info = String.valueOf(f);
							} else if (object instanceof Boolean bol) {
								outputStream.writeBoolean(bol);
								info = String.valueOf(bol);
							} else if (object instanceof Character ch) {
								outputStream.writeChar(ch);
								info = String.valueOf(ch);
							} else if (object instanceof Double d) {
								outputStream.writeDouble(d);
								info = String.valueOf(d);
							} else {
								LOG.log(Level.WARNING, "Writing of object type {0} to the TCP stack is unsupported at this time.",
										object.getClass().getName());
							}
							outputStream.flush();
							if (info.contains(DISCONNECT_REQUEST)) {
								break;
							}
						}
					} catch (NullPointerException ex) {
						writeQueue.clear();
						allowWriteRequests = false;
						LOG.log(Level.WARNING, "NullPointerException in Writer loop", ex);
						enableRun = false;
						break;
					} catch (SocketException ex) {
						writeQueue.clear();
						allowWriteRequests = false;
						LOG.log(Level.WARNING, "TCP socket closed by server due to: {0} ", ex.getMessage());
						enableRun = false;
						break;
					} catch (IOException ex) {
						writeQueue.clear();
						allowWriteRequests = false;
						LOG.log(Level.WARNING, "IOException in Writer loop", ex);
						enableRun = false;
						break;
					}
					LOG.log(Level.INFO, "DataOutputStream write: {0}", info);
					getPropertyChangeSupport().firePropertyChange(Event.DATA_TRANSMITTED.name(), null, info);
				}
				LOG.log(Level.INFO, "Writer instance has been closed");
				pcs.firePropertyChange(TCPClient.Event.CONNECTION_DROPPED.name(), connected, false);
				connected = false;
			} catch (final InterruptedException ex) {
				LOG.log(Level.WARNING, ex.getMessage(), ex);
				Thread.currentThread().interrupt();
			} catch (final RejectedExecutionException ex) {
				LOG.log(Level.WARNING, ex.getMessage(), ex);
			}
		}
	}

	public static Set<String> getStandardCharsets() {
		final Map<String, Charset> map = Charset.availableCharsets();
		return map.keySet();
	}

	public static String convert(String value, String fromEncoding, String toEncoding)
			throws UnsupportedEncodingException {
		return new String(value.getBytes(fromEncoding), toEncoding);
	}

	public static String getUTFCharset(String value) {
		final String probe = StandardCharsets.UTF_8.name();
		for (String c : getStandardCharsets()) {
			final Charset charset = Charset.forName(c);
			if (charset != null) {
				try {
					if (value.equals(convert(convert(value, charset.name(), probe), probe, charset.name()))) {
						return c;
					}
				} catch (UnsupportedEncodingException ex) {
					LOG.log(Level.INFO, ex.getMessage());
				}
			}
		}
		return "";
	}

	public static boolean isCharsetTypeUTF8(String value) {
		try {
			if (value.equals(convert(convert(value, StandardCharsets.UTF_8.name(), StandardCharsets.UTF_8.name()),
					StandardCharsets.UTF_8.name(), StandardCharsets.UTF_8.name()))) {
				return true;
			}
		} catch (UnsupportedEncodingException ex) {
			LOG.log(Level.INFO, ex.getMessage());
		}
		return false;
	}
	
	public PropertyChangeSupport getPropertyChangeSupport() {
		return pcs;
	}

	public void connect(InetAddress inetAddress, int portNumber) {
		if (!enableRun) {
			this.inetAddress = inetAddress;
			this.portNumber = portNumber;
			writeQueue.clear();
			enableRun = true;
			socketExecutor.execute(new TCPStream(inetAddress, portNumber));
		}
	}
	
	// For accommodating an IP Address or port number change while already connected
	public void reConnect(InetAddress inetAddress, int portNumber) {
		if (this.inetAddress != inetAddress || this.portNumber != portNumber) {
			this.inetAddress = inetAddress;
			this.portNumber = portNumber;
			if (enableRun) {
				queueReconnect = true;
				disconnect();
			}
		}
	}

	public void disconnect() {
		if (timer != null) {
			timer.cancel();
			timer.purge();
		}
		
		writeQueue.clear();
		allowWriteRequests = false;
		writeQueue.add(DISCONNECT_REQUEST.getBytes(StandardCharsets.UTF_8)); // request graceful closure of server socket
		drop = true;
		enableRun = false;
		connected = false;
		
		writerExecutorDisconnect();
		socketExecutorDisconnect();
	}
	
	private void socketExecutorDisconnect() {
		if (socketExecutor != null) {
			try {
				LOG.log(Level.INFO, "Initializing TCPClient.socketExecutor service termination....");		
				socketExecutor.shutdown();
				if (!socketExecutor.awaitTermination(3, TimeUnit.SECONDS)) {
					socketExecutor.shutdownNow();
				}
				LOG.log(Level.INFO, "TCPClient.socketExecutor service has gracefully terminated");	
				pcs.firePropertyChange(TCPClient.Event.CONNECTION_DROPPED.name(), null, false);
				if (queueReconnect) {
					connect(inetAddress, portNumber);
					queueReconnect = false;
				}
			} catch (InterruptedException e) {
				LOG.log(Level.SEVERE, "TCPClient.socketExecutor service has timed out after 3 seconds of waiting to terminate processes.");
				socketExecutor = null;
				Thread.currentThread().interrupt();
			}
		}
	}
	
	private void writerExecutorDisconnect() {
		if (writerExecutor != null) {
			try {
				LOG.log(Level.INFO, "Initializing TCPClient.writerExecutor service termination....");
				writerExecutor.shutdown();
				if (!writerExecutor.awaitTermination(3, TimeUnit.SECONDS)) {
					writerExecutor.shutdownNow();
				}
				LOG.log(Level.INFO, "TCPClient.writerExecutor service has gracefully terminated");			
			} catch (InterruptedException e) {
				LOG.log(Level.SEVERE, "TCPClient.writerExecutor service has timed out after 3 seconds of waiting to terminate processes.");
				writerExecutor = null;
				Thread.currentThread().interrupt();
			}
		}
	}
	
	@Override
	public void close() {
		disconnect();
		for (PropertyChangeListener pcl : pcs.getPropertyChangeListeners()) {
			pcs.removePropertyChangeListener(pcl);
		}
	}
	
	private class PingTester implements Runnable {
		private final InetAddress inetAddress;

		private PingTester(InetAddress inetAddress) {
			this.inetAddress = inetAddress;
		}
		
		@Override
		public synchronized void run() {
			final long pingMillis = System.currentTimeMillis();
			try {
				if (!inetAddress.isReachable(1000)) {
					connected = false;
					drop = true;
					pcs.firePropertyChange(Event.PING_FAILURE.name(), null, connected);
					LOG.log(Level.WARNING, "Ping Test to server at {0} failed after {1} milliseconds", 
							new Object[] {inetAddress.getCanonicalHostName(), System.currentTimeMillis() - pingMillis});
				} else {
					LOG.log(Level.INFO, "Ping Test to server at {0} compleeted in {1} milliseconds", 
							new Object[] {inetAddress.getCanonicalHostName(), System.currentTimeMillis() - pingMillis});
				}
			} catch (UnknownHostException ex) {
				pcs.firePropertyChange(Event.PING_FAILURE.name(), null, connected);
				LOG.log(Level.WARNING, "Ping Test to server at {0} failed due to an UnknownHostException", inetAddress.getCanonicalHostName());
				LOG.log(Level.WARNING, null, ex);
			} catch (IOException ex) {
				pcs.firePropertyChange(Event.PING_FAILURE.name(), null, connected);
				LOG.log(Level.WARNING, "Ping Test to server at {0} failed due to an IOException", inetAddress.getCanonicalHostName());
				LOG.log(Level.WARNING, null, ex);
			}
		}
	}
}
