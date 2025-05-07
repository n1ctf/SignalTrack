package tcp;

/**
 *
 * @author John
 */
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.UnknownHostException;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import java.time.ZonedDateTime;

import java.util.Arrays;
import java.util.HexFormat;
import java.util.Map;
import java.util.Set;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import java.util.logging.Level;
import java.util.logging.Logger;

import network.NetworkParameterSet;

public class SynchronizedTCPClient implements AutoCloseable {

	public enum Event {
		DATA_RECEIVED, DATA_TRANSMITTED, CONNECTION_ACCEPTED, CONNECTION_DROPPED
	}

	private static final Logger LOG = Logger.getLogger(SynchronizedTCPClient.class.getName());
	
	public static final String DISCONNECT_REQUEST = "DISCONNECT\n";
	public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
	public static final boolean DEFAULT_DEBUG_MODE = true;
	public static final int WRITE_QUEUE_SIZE = 1024;
	public static final int DEFAULT_CONNECT_TIMEOUT_MILLISECONDS = 60000;
	public static final int DEFAULT_READ_TIMEOUT_MILLISECONDS = 1000;
	public static final int DEFAULT_WAIT_FOR_RESPONSE_MILLISECONDS = 2000;

	private final BlockingQueue<WriterDataSet> writeQueue = new ArrayBlockingQueue<>(WRITE_QUEUE_SIZE);
	
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	private ExecutorService writerReaderExecutor = Executors.newSingleThreadExecutor();
	
	private boolean debug = DEFAULT_DEBUG_MODE;
	private boolean connected;
	private boolean allowRequests = true;
	private boolean enableRun = false;
	private Charset charset = DEFAULT_CHARSET;
	
	public SynchronizedTCPClient() {
		this(DEFAULT_CHARSET);
	}

	public SynchronizedTCPClient(Charset charset) {
		this.charset = charset;
		
		if (debug) {
			LOG.log(Level.INFO, "SynchronizedTCPClient class has been instantiated at: {0}", ZonedDateTime.now());
		}
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				close();
			}
		});
	}

	public void stopTCPConnection() {
		enableRun = false;
		allowRequests = false;
		writeQueue.clear();
		writeQueue.add(new WriterDataSet(DISCONNECT_REQUEST.getBytes(DEFAULT_CHARSET), -1));
	}

	public void startTCPConnection(InetAddress inetAddress, int portNumber) {
		startTCPConnection(new NetworkParameterSet(inetAddress, portNumber));
	}

	public void startTCPConnection(URI uri) {
		try {
			startTCPConnection(new NetworkParameterSet(uri));
		} catch (UnknownHostException e) {
			if (debug) {
				LOG.log(Level.SEVERE, null, e);
			}
		}
	}
	
	public void startTCPConnection(String hostName, int portNumber) {
		try {
			startTCPConnection(new NetworkParameterSet(hostName, portNumber));
		} catch (UnknownHostException e) {
			if (debug) {
				LOG.log(Level.SEVERE, null, e);
			}
		}
	}

	public void startTCPConnection(NetworkParameterSet netParams) {
		try {
			if (!enableRun) {
				if (debug) {
					LOG.log(Level.INFO, "Opening TCP Socket to {0} using Port {1}",
						new Object[] { 
							netParams.getInetAddress().getHostAddress(), 
							String.valueOf(netParams.getPortNumber()) 
						}
					);
				}
				enableRun = true;
				writerReaderExecutor.submit(new TCPStream(netParams.getInetAddress(), netParams.getPortNumber()));
			} else {
				if (debug) {
					LOG.log(Level.INFO, "TCP service is already enabled. No action taken.");
				}
			}
		} catch (RejectedExecutionException ex) {
			if (debug) {
				LOG.log(Level.INFO, "Unable to instiantiate new TCPStream({0}, {1}",
					new Object[] {
							netParams.getInetAddress(),
							netParams.getPortNumber()
					}
				);
			}
		}
	}
	
	public void setCharset(Charset charset) {
		this.charset = charset;
	}

	public boolean isConnected() {
		return connected;
	}
	
	public boolean allowRequests() {
		return allowRequests;
	}

	@Override
	public void close() {
		stopTCPConnection();
		if (writerReaderExecutor != null) {
			try {
				LOG.log(Level.INFO, "Initializing TCPClient.writerReaderExecutor service termination....");
				writerReaderExecutor.shutdown();
				if (!writerReaderExecutor.awaitTermination(3, TimeUnit.SECONDS)) {
					writerReaderExecutor.shutdownNow();
				}
				LOG.log(Level.INFO, "TCPClient.writerReaderExecutor service has gracefully terminated");			
			} catch (InterruptedException e) {
				LOG.log(Level.SEVERE, "TCPClient.writerReaderExecutor service has timed out after 3 seconds of waiting to terminate processes.");
				writerReaderExecutor = null;
				Thread.currentThread().interrupt();
			}
		}
		for (PropertyChangeListener pcl : pcs.getPropertyChangeListeners()) {
			pcs.removePropertyChangeListener(pcl);
		}
	}

	public void synchronizedWriteLine(String str) {
		synchronizedWrite(str.getBytes(charset), -1);
	}
	
	public void synchronizedWriteLine(byte[] byteArray) {
		synchronizedWrite(byteArray, -1);
	}
	
	public void synchronizedWrite(byte[] byteArray, int numberBytesToRead) {
		if (allowRequests) {
			if (writeQueue.size() < WRITE_QUEUE_SIZE) {
				writeQueue.add(new WriterDataSet(byteArray, numberBytesToRead));
				if (debug) {
					if (numberBytesToRead > -1) {
						LOG.log(Level.INFO, "The following data has been submitted to the transmission queue: {0} \n  with a requested response length of {1} characters.",
							new Object[] {HexFormat.of().formatHex(byteArray), numberBytesToRead});
					} else {
						LOG.log(Level.INFO, "The following data has been submitted to the transmission queue: {0} \n  with a request for all data up to the EOL marker.",
								new Object[] {HexFormat.of().formatHex(byteArray)});
					}
				}
			} else {
				if (debug) {
					LOG.log(Level.INFO, "TCP client writeQueue is full... Requests to write \"{0}\" has been rejected.",
						new Object[] {HexFormat.of().formatHex(byteArray)});
				}
			}
		} else {
			writeQueue.clear();
			enableRun = false;
		}
	}

	private final class TCPStream implements Runnable {

		private final InetAddress inetAddress;
		private final int portNumber;
		
		private final ReadWriteLock rwlock = new ReentrantReadWriteLock();
		
		private TCPStream(InetAddress inetAddress, int portNumber) {
			this.inetAddress = inetAddress;
			this.portNumber = portNumber;
		}

		@Override
		public void run() {
			while (enableRun) {
				try (Socket socket = new Socket()) {
					while ((!socket.isConnected() || socket.isClosed()) && enableRun) {
						try {
							socket.setKeepAlive(true);
							socket.setSoTimeout(DEFAULT_READ_TIMEOUT_MILLISECONDS);
							if (debug) {
								LOG.log(Level.INFO, "Client is requesting connection to server at {0}:{1}",
										new Object[] {inetAddress, String.valueOf(portNumber)});							
							}
							socket.connect(new InetSocketAddress(inetAddress, portNumber), DEFAULT_CONNECT_TIMEOUT_MILLISECONDS);
							connected = socket.isConnected() && !socket.isClosed();
							if (debug) {
								LOG.log(Level.INFO, "Client {0} to server at {1}:{2}",
									new Object[] { 
										connected ? "is connected" : "failed connecting",
										socket.getInetAddress(), 
										"%5d".formatted(socket.getPort())
									}
								);
							}
						} catch (SocketTimeoutException ex) {
							LOG.log(Level.INFO, "Client failed connecting to server at {0}:{1} after {2} milliseconds of waiting... Retrying",
								new Object[] { 
										inetAddress.getHostAddress(), 
										String.valueOf(portNumber), 
										String.valueOf(DEFAULT_CONNECT_TIMEOUT_MILLISECONDS)
									}
							);
						} catch (IOException ex) {
							connected = socket.isConnected() && !socket.isClosed();
							try {
								Thread.sleep(DEFAULT_CONNECT_TIMEOUT_MILLISECONDS);
								if (debug) {
									LOG.log(Level.INFO, "Client failed connecting to server at {0}:{1} after {2} milliseconds of waiting... Retrying",
										new Object[] { 
											inetAddress.getHostAddress(), 
											String.valueOf(portNumber), 
											String.valueOf(DEFAULT_CONNECT_TIMEOUT_MILLISECONDS)
										}
									);
								}
							} catch (InterruptedException ex1) {
								if (debug) {
									LOG.log(Level.INFO, "Interrupted while waiting to retry the failed connection...");
								}
								Thread.currentThread().interrupt();
							}
						} catch (IllegalArgumentException ex) {
							if (debug) {
								LOG.log(Level.INFO, "Invalid IP Address: {0}", inetAddress);
							}
							connected = socket.isConnected() && !socket.isClosed();
							enableRun = false;
						}
	
						if ((!socket.isConnected() || socket.isClosed()) && debug) {
							LOG.log(Level.INFO, "Retrying Connection Now");
						}
						
						if (socket.isConnected() && !socket.isClosed()) {
							pcs.firePropertyChange(Event.CONNECTION_ACCEPTED.name(), null, true);
							if (debug) {
								LOG.log(Level.INFO, "Connection Accepted by Server at {0}:{1}", new Object[] {inetAddress, String.valueOf(portNumber)});
							}
						}
						
						if (socket.isConnected() && !socket.isClosed() && enableRun) {
							try (DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
								DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()))) {
							
								while (enableRun) {
									if (debug) {
										if (writeQueue.isEmpty()) {
											LOG.log(Level.INFO, "Blocking queue is waiting for a WriterDataSet object to send... ");
										} else {
											LOG.log(Level.INFO, "There are {0} additional WriterDataSet objects in the blocking queue... ", writeQueue.size());
										}
									}	
									
									WriterDataSet wds = null;

									wds = writeQueue.take();
									
									if (new String(wds.getByteArray(), charset).contains(DISCONNECT_REQUEST) || !enableRun) {
										break;
									}

									if (debug && wds != null) {
										LOG.log(Level.INFO, "WriterDataSet taken from queue for transmission: {0}\n"
												+ "  Remaining WriterDataSet objects in queue: {1}", 
												new Object[] {HexFormat.of().formatHex(wds.getByteArray()), writeQueue.size()});
									}
	
									if (socket.isConnected() && !socket.isClosed()) {
										connected = socket.isConnected() && !socket.isClosed();
										
										rwlock.writeLock().lock();
										
										if (wds != null) {
											dataOutputStream.write(wds.getByteArray());
											dataOutputStream.flush();
										}
										
										if (wds != null) {
											pcs.firePropertyChange(Event.DATA_TRANSMITTED.name(), null, new String(wds.byteArray, charset));
										}
										
										rwlock.writeLock().unlock();
										
										if (debug && wds != null) {
											LOG.log(Level.INFO, "TCP Data {0} sent to {1} via port {2}",
												new Object[] { 
													HexFormat.of().formatHex(wds.getByteArray()), 
													inetAddress.getHostName(),
													String.valueOf(socket.getLocalPort())
												}
											);
										}
										
										if (wds != null && wds.getNumberBytesToRead() >= 0) {
										    rwlock.readLock().lock(); 
										    final byte[] b = blockUntilNBytesReadInputStreamRead(dataInputStream, wds.getNumberBytesToRead());
											rwlock.readLock().unlock();
											pcs.firePropertyChange(Event.DATA_RECEIVED.name(), null, b);
										} else if (wds != null && wds.getNumberBytesToRead() < 0) {
											rwlock.readLock().lock();
											final byte[] b = blockUntilEOLInputStreamRead(socket, dataInputStream);
											rwlock.readLock().unlock();
											pcs.firePropertyChange(Event.DATA_RECEIVED.name(), null, b);
										}
										
										if (!enableRun) {
											break;
										}
									} else {
										enableRun = false;
										connected = socket.isConnected() && !socket.isClosed();
										if (debug) {
											LOG.log(Level.INFO, "Unable to Write to TCP Socket - socket {0} connected and socket {1} closed",
												new Object[] {
													socket.isConnected() ? "is" : "is not",
													socket.isClosed() ? "is" : "is not" 
											});
										}
									}
								}
								
							} catch (IOException ex) {
								LOG.log(Level.SEVERE, null, ex);
								enableRun = false;
							}
							
						}
					}

				} catch (InterruptedException ex) {
					LOG.log(Level.SEVERE, "class TCPStream", ex);
					Thread.currentThread().interrupt();
				} catch (IOException | NullPointerException ex) {
					LOG.log(Level.SEVERE, "class TCPStream", ex);
				} finally {
					connected = false;
					allowRequests = false;
					writeQueue.clear();
					writeQueue.add(new WriterDataSet(DISCONNECT_REQUEST.getBytes(charset), -1));
					pcs.firePropertyChange(Event.CONNECTION_DROPPED.name(), null, true);
					if (debug) {
						LOG.log(Level.INFO, "TCP client socket connection to {0}:{1} is closed.",
							new Object[] {inetAddress.getHostAddress(), String.valueOf(portNumber)});
					}
				}
			}
		}

		private byte[] blockUntilEOLInputStreamRead(Socket socket, DataInputStream dataInputStream) throws IOException {
			final StringBuilder b = new StringBuilder();

			while (socket.isConnected() && !socket.isClosed() && enableRun) {
				try {
					if (dataInputStream.available() > 0) { 
						final int readResult = dataInputStream.read();
						
						if (readResult == 13 || readResult == 10 || readResult == -1) {
							break;
						}
						
						if (readResult != 0) {
							b.append((char) readResult);
						}
					}
				} catch (SocketTimeoutException ex) {	
					LOG.log(Level.INFO, "Socket Timeout during readInputStream");
					return new byte[] {};
				}
			}
			
			if (b.toString().contains(DISCONNECT_REQUEST)) {
				dataInputStream.close();
			}
			
			LOG.log(Level.INFO, "Socket is Connected: {0}, Socket is Open: {1}, EnableRun: {2}",
					new Object[] {socket.isConnected(), !socket.isClosed(), enableRun});
			
			return b.toString().getBytes(charset);
		}

		
		private byte[] blockUntilNBytesReadInputStreamRead(DataInputStream dataInputStream, int numberBytesToRead) throws IOException {
			final int readLength = 0;
			byte[] b = null;
			try {
				b = new byte[numberBytesToRead];
					
				dataInputStream.readNBytes(b, 0, numberBytesToRead);
				
				if (b.length == numberBytesToRead) {
					return Arrays.copyOf(b, b.length);
				} else {
					return new byte[] {};
				}
			} catch (NullPointerException ex) {
				LOG.log(Level.WARNING, "Number of bytes to read is null", ex);
				dataInputStream.close();
				return new byte[] {};
			} catch (IndexOutOfBoundsException ex) {
				if (debug) {
					LOG.log(Level.WARNING, "b.length: {0}, numberBytesToRead: {1}, readLength: {2}, inputStream.avail(): {3}\n Received Data: {4}",
							new Object[] {b.length, numberBytesToRead, readLength, dataInputStream.available(), Arrays.toString(b)});
				}
				ex.printStackTrace();
				dataInputStream.close();
				return new byte[] {};
			} catch (IOException ex) {
				dataInputStream.close();
				return new byte[] {};
			}
		}
	}
	
	private class WriterDataSet {
		private final byte[] byteArray;
		private final int numberBytesToRead;
		
		public WriterDataSet(byte[] byteArray, int numberBytesToRead) {
			this.byteArray = byteArray.clone();
			this.numberBytesToRead = numberBytesToRead;
		}

		public byte[] getByteArray() {
			return byteArray.clone();
		}

		public int getNumberBytesToRead() {
			return numberBytesToRead;
		}
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}

	public void removeAllPropertyChangeListeners() {
		for (PropertyChangeListener pcl : pcs.getPropertyChangeListeners()) {
			pcs.removePropertyChangeListener(pcl);
		}
	}

	public void removeAllPropertyChangeListeners(PropertyChangeListener listener) {
		for (PropertyChangeListener pcl : pcs.getPropertyChangeListeners()) {
			if (listener.equals(pcl)) {
				pcs.removePropertyChangeListener(pcl);
			}
		}
	}
	
	public boolean isPropertyChangeListenerRegistered(PropertyChangeListener listener) {
		boolean registered = false;
		for (PropertyChangeListener pcl : pcs.getPropertyChangeListeners()) {
			if (listener.equals(pcl)) {
				registered = true;
				break;
			}
		}
		return registered;
	}
	
	public boolean isPropertyChangeListenerRegistered() {
		return pcs.getPropertyChangeListeners().length > 0;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public static Set<String> getStandardCharsets() {
		final Map<String, Charset> map = Charset.availableCharsets();
		return map.keySet();
	}
	
	public static String convert(String value, String fromEncoding, String toEncoding)
			throws UnsupportedEncodingException {
		return new String(value.getBytes(fromEncoding), toEncoding);
	}
	
	public static String getUTFCharset(String value) throws UnsupportedEncodingException {
		final String probe = StandardCharsets.UTF_8.name();
		for (String c : getStandardCharsets()) {
			final Charset charset = Charset.forName(c);
			final boolean condition = charset != null && value.equals(convert(convert(value, charset.name(), probe), probe, charset.name()));
			if (condition) {
				return c;
			}
		}
		return "";
	}

	public static boolean isCharsetTypeUTF8(String value) throws UnsupportedEncodingException {
		return (value.equals(convert(convert(value, StandardCharsets.UTF_8.name(), StandardCharsets.UTF_8.name()),
				StandardCharsets.UTF_8.name(), StandardCharsets.UTF_8.name())));
	}
	
}
