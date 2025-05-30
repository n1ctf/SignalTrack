package radio;

import java.awt.event.ActionListener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.io.File;

import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JPanel;
import javax.swing.Timer;

import tty.SerialComponent;
import tty.TeletypeInterface;

import tty.AbstractTeletypeController;
import tty.AbstractTeletypeController.BaudRate;
import tty.AbstractTeletypeController.DataBits;
import tty.AbstractTeletypeController.FlowControl;
import tty.AbstractTeletypeController.Parity;
import tty.AbstractTeletypeController.StopBits;
import tty.AbstractTeletypeController.TTYEvents;

import utility.Utility;

public class Yaesu_FT857D extends AbstractRadioReceiver implements TeletypeInterface {

	private static final long serialVersionUID = 3045764079243768863L;

	private static final Logger LOG = Logger.getLogger(Yaesu_FT857D.class.getName());

	private static final String MANUFACTURER = "YAESU";
	private static final String MODEL = "FT857D";
	private static final String DEFAULT_SERIAL_NUMBER = "0C000000";
	
	public static final String SETTINGS_TITLE_PREFIX = "Radio";
	
	private static final BaudRate BAUD_RATE = BaudRate.BAUDRATE_9600;
    private static final Parity PARITY = Parity.NONE;
    private static final StopBits STOP_BITS = StopBits.STOPBITS_2;
    private static final DataBits DATA_BITS = DataBits.DATABITS_8;
    private static final FlowControl FLOW_CONTROL_IN = FlowControl.NONE;
    private static final FlowControl FLOW_CONTROL_OUT = FlowControl.NONE;
    private static final boolean SUPPORTS_RTS = true;
    private static final boolean SUPPORTS_DTR = true;
    
	private static final boolean SUPPORTS_SINAD = true;
	private static final boolean SUPPORTS_BER = false;
	private static final boolean SUPPORTS_RSSI = true;
	private static final boolean SERIAL_PARAMETERS_FIXED = true;
	private static final boolean SERIAL_BAUD_RATE_FIXED = false;
	private static final boolean SUPPORTS_AGC = false;
	private static final boolean SUPPORTS_AFC = false;
	private static final boolean SUPPORTS_ATTENUATOR = false;
	private static final boolean SUPPORTS_NOISE_BLANKER = false;
	private static final boolean SUPPORTS_VOLUME_CONTROL = false;
	private static final boolean SUPPORTS_SQUELCH_CONTROL = false;
	
	private static final boolean SUPPORTS_COR = true;

	private static final double MINIMUM_RX_FREQ = 0.050;
	private static final double MAXIMUM_RX_FREQ = 470.0;
	private static final double MINIMUM_ATTN = 0.0;
    private static final double MAXIMUM_ATTN = 125.0;
	private static final int RSSI_UPPER_LIMIT = 15;
	private static final int RSSI_LOWER_LIMIT = 0;
    private static final double DEFAULT_NOISE_FLOOR = -135;
	private static final double DEFAULT_SATURATION = -21;
	private static final double ADJACENT_CHANNEL_REJECTION_DB = 60.0;
	private static final double SIGNAL_REQ_FOR_12DB_SINAD = -115.0; 
	private static final double SIGNAL_REQ_FOR_20DB_QUIETING = -117.0; 
	private static final double SIGNAL_REQ_FOR_5PCT_BER = -118;
	private static final boolean SUPPORTS_MULTI_MODE = true;
	private static final boolean SUPPORTS_MULTI_FILTER = true;
	private static final boolean SUPPORTS_COUNTRY_CODE_RETRIEVAL = true;
	private static final boolean SUPPORTS_FIRMWARE_RETRIEVAL = true;
	private static final boolean SUPPORTS_IF_SHIFT = false;
	private static final boolean SUPPORTS_VOICE_SCAN = false;
	private static final boolean SUPPORTS_DSP = false;
	private static final boolean SUPPORTS_FEATURE_CODE_RETRIEVAL = false;
	private static final int BUSY_CLEARANCE_PERIOD = 500;
	private static final int RSSI_CLEARANCE_PERIOD = 500;
	private static final int WRITE_PAUSE = 150;
	private static final int HEARTBEAT_PERIOD = 250;
	private static final int RSSI_AVERAGING_FACTOR = 3;

	private static final BaudRate[] AVAILABLE_BAUD_RATES = { BaudRate.BAUDRATE_4800, BaudRate.BAUDRATE_9600,
			BaudRate.BAUDRATE_38400 };

	private static final Integer[] FILTERS_HZ = { 6000, 15000 };

	private static final StandardModeName[] SUPPORTED_MODES = { 
		StandardModeName.FM,
		StandardModeName.NFM, 
		StandardModeName.AM, 
		StandardModeName.USB,
		StandardModeName.LSB, 
		StandardModeName.CW, 
		StandardModeName.RTTY,
		StandardModeName.PKTFM, 
		StandardModeName.FAX 
	};

	public static final String crlf = "\r\n";

	public static final int ft857dRssiMask = 0b00001111;
	public static final int ft857dBusyMask = 0b10000000;
	public static final int ft857dPLDecodeMask = 0b01000000;
	public static final int ft857dRxCenteredMask = 0b00100000;
	public static final int ft857dPowerMeterMask = 0b00001111;
	public static final int ft857dPTTMask = 0b10000000;
	public static final int ft857dHighVSWRMask = 0b01000000;

	protected static final int[] ft857dCommandBlockGetReceiverStatus = {0x00, 0x00, 0x00, 0x00, 0xE7};
	protected static final int[] ft857dCommandBlockGetTransmitterStatus = {0x00, 0x00, 0x00, 0x00, 0xF7};
	protected static final int[] ft857dCommandBlockGetFrequencyMode = {0x00, 0x00, 0x00, 0x00, 0x03};
	
	public static final int ft857dCommandGetReceiverStatus = 0xE7;
	public static final int ft857dCommandGetTransmitterStatus = 0xF7;
	public static final int ft857dCommandGetFrequencyMode = 0x03;
	public static final int ft857dCommandNull = 0xFF;

	public static final int ft857dCommandOperatingModeCommand = 0x07;
	public static final int ft857dCommandOperatingMode_LSB = 0x00;
	public static final int ft857dCommandOperatingMode_USB = 0x01;
	public static final int ft857dCommandOperatingMode_CW = 0x02;
	public static final int ft857dCommandOperatingMode_CWR = 0x03;
	public static final int ft857dCommandOperatingMode_AM = 0x04;
	public static final int ft857dCommandOperatingMode_WFM = 0x06;
	public static final int ft857dCommandOperatingMode_FM = 0x08;
	public static final int ft857dCommandOperatingMode_CWN = 0x82;
	public static final int ft857dCommandOperatingMode_RTTY = 0x0A;
	public static final int ft857dCommandOperatingMode_PKT = 0x0C;
	public static final int ft857dCommandOperatingMode_FMN = 0x88;

	public static final int ft857dCommandSetFrequencySuffix = 0x01;
	public static final int ft857dCommandSetToneSquelchFreqSuffix = 0x0B;
	public static final int ft857dCommandDigitalSquelchFreqSuffix = 0x0C;

	public static final int ft857dCommandSetSquelchCommand = 0x0A;
	public static final int ft857dCommandDigitalSquelchTx = 0x0C;
	public static final int ft857dCommandDigitalSquelchRx = 0x0B;
	public static final int ft857dCommandDigitalSquelchTxRx = 0x0A;
	public static final int ft857dCommandToneSquelchTx = 0x4A;
	public static final int ft857dCommandToneSquelchRx = 0x3A;
	public static final int ft857dCommandToneSquelchTxRx = 0x2A;
	public static final int ft857dCommandCarrierSquelch = 0x8A;

	private static final int READ_BUFFER_LEN = 2048;

	private static final EncryptionProtocol[] ENCRYPTION_PROTOCOLS = { EncryptionProtocol.CLEAR };
	
	private long sequence;

	private Timer busyTimer;
	private Timer rssiTimer;
	private Timer heartbeatTimer;

	private final ExecutorService executor = Executors.newCachedThreadPool();

	private final BlockingQueue<int[]> writeStack = new ArrayBlockingQueue<>(32);

	private volatile boolean allowQueueing;

	private double freqMhz;
	private int lastOpCode = ft857dCommandNull;
	private int averagingFactor = RSSI_AVERAGING_FACTOR;
	private int rssiAverageCount;
	private int rssiSum;
	private volatile boolean ptt;
	private volatile boolean plDetect;
	private volatile boolean rxCentered;
	private int powerLevel;
	private boolean highVSWR;
	private boolean connected;

	private ScheduledFuture<?> writerHandle;
	
	private AbstractTeletypeController tty = AbstractTeletypeController.getTTyPortInstance(AbstractTeletypeController.getCatalogMap().getKey("JSSC TTY Port v2.9.5"), false);
	
	private PropertyChangeListener serialPortPropertyChangeListener;

	private byte[] readBuffer;

	public Yaesu_FT857D() { }

	public Yaesu_FT857D(File calFile, Boolean clearAllPreferences) {
		super(calFile, clearAllPreferences);
		executor.execute(new Writer());
		initializeListeners();
		initializeTimers();
		tty.getPropertyChangeSupport().addPropertyChangeListener(serialPortPropertyChangeListener);
	}

	private void initializeListeners() {
		serialPortPropertyChangeListener = (PropertyChangeEvent event) -> {
			if (TTYEvents.RX_CHAR.name().equals(event.getPropertyName())) {
				try {
					String data = null;
					StringBuilder sb = new StringBuilder();
					int len;
					do {
						readBuffer = tty.readBytes();
						len = readBuffer.length;
						if (len >= 1) {
							sb.append(new String(readBuffer, 0, len));
							data = sb.toString().trim();
						}
					} while (len == READ_BUFFER_LEN);
					processData(data);
				} catch (NullPointerException ex) {
					LOG.log(Level.WARNING, ex.getMessage());
				}
			}
			if (TTYEvents.TX_DATA.name().equals(event.getPropertyName())) {
				pcs.firePropertyChange(AbstractRadioReceiver.TX_DATA, null, event.getNewValue());
			}
			if (TTYEvents.TTY_PORT_CONFIGURATION_ERROR.name().equals(event.getPropertyName())) {
				pcs.firePropertyChange(AbstractRadioReceiver.CONFIG_ERROR, event.getOldValue(), event.getNewValue());
			}
			if (TTYEvents.ERROR.name().equals(event.getPropertyName())) {
				pcs.firePropertyChange(AbstractRadioReceiver.ERROR, event.getOldValue(), event.getNewValue());
			}
			if (TTYEvents.TTY_PORT_ERROR.name().equals(event.getPropertyName())) {
				pcs.firePropertyChange(AbstractRadioReceiver.INVALID_ADDRESS, event.getOldValue(), event.getNewValue());
			}
			if (TTYEvents.DSR.name().equals(event.getPropertyName())) {
				pcs.firePropertyChange(TeletypeInterface.DCEAssert.DSR.name(), event.getOldValue(), event.getNewValue());
			}
			if (TTYEvents.CTS.name().equals(event.getPropertyName())) {
				pcs.firePropertyChange(TeletypeInterface.DCEAssert.CTS.name(), event.getOldValue(), event.getNewValue());
			}
			if (TTYEvents.RLSD.name().equals(event.getPropertyName())) {
				pcs.firePropertyChange(TeletypeInterface.DCEAssert.DCD.name(), event.getOldValue(), event.getNewValue());
			}
			if (TTYEvents.ONLINE.name().equals(event.getPropertyName())) {
				pcs.firePropertyChange(AbstractRadioReceiver.ONLINE, event.getOldValue(), event.getNewValue());
				setEnableEvents((boolean) event.getNewValue());
			}
		};
	}
	
	private void initializeTimers() {
		final ActionListener busyTimerActionListener = _ -> clearBusy();

		busyTimer = new Timer(BUSY_CLEARANCE_PERIOD, busyTimerActionListener);

		final ActionListener rssiTimerActionListener = _ -> clearRSSI();

		rssiTimer = new Timer(RSSI_CLEARANCE_PERIOD, rssiTimerActionListener);

		final ActionListener heartbeatTimerActionListener = _ -> missedHeartbeat();

		heartbeatTimer = new Timer(HEARTBEAT_PERIOD, heartbeatTimerActionListener);
		heartbeatTimer.setRepeats(true);

	}

	private void missedHeartbeat() {
		connected = false;
	}

	private void clearRSSI() {
		rssi = 0;
		if (isRssiEnabled() && !isScanning()) {
			getReceiverEvent().firePropertyChange(ReceiverEvent.RSSI, null, rssi);
		}
	}

	private void clearBusy() {
		busy = false;
		getReceiverEvent().firePropertyChange(ReceiverEvent.BUSY, null, isBusy());
	}

	@Override
	public void setSquelchMode(AccessMode accessMode) {
		this.accessMode = accessMode;
		
		int[] smode = { 0x00, 0x00, 0x00, 0x00, 0x00 };
		
		smode[0] = ft857dCommandCarrierSquelch;
		smode[4] = ft857dCommandSetSquelchCommand;
		
		if (allowQueueing) {
			switch (accessMode) {
				case DPL:
					smode[0] = ft857dCommandDigitalSquelchRx;
					break;
				case PL:
					smode[0] = ft857dCommandToneSquelchRx;
					break;
				case CSQ:
					smode[0] = ft857dCommandCarrierSquelch;
					break;
				case CC:
					break;
				case NAC:
					break;
				default:
					break;
			}
		}
		
		if (allowQueueing) {
			writeData(smode);
		}
	}

	@Override
	public void setPL(int pl) {
		this.pl = pl;
		if (pl != 0) {
			setCodedSquelch(pl * 10, 'T');
		}
	}

	private void setCodedSquelch(final int code, final char mode) {
		if (code == 0) {
			setSquelchMode(AccessMode.CSQ);
			return;
		}
		
		if (mode == 'T') {
			setSquelchMode(AccessMode.PL);
		}
		
		if (mode == 'D') {
			setSquelchMode(AccessMode.DPL);
		}

		int[] sqlByte = { 0x00, 0x00, 0x00, 0x00, 0x00 };
		byte[] bcd = Utility.toBCDArray(code, 4);

		sqlByte[0] = bcd[0];
		sqlByte[1] = bcd[1];
		sqlByte[2] = bcd[0];
		sqlByte[3] = bcd[1];
		if (mode == 'T') {
			sqlByte[4] = ft857dCommandSetToneSquelchFreqSuffix;
		}
		
		if (mode == 'D') {
			sqlByte[4] = ft857dCommandDigitalSquelchFreqSuffix;
		}
		
		if (allowQueueing) {
			writeData(sqlByte);
		}
	}

	@Override
	public void setDPL(int dpl) {
		this.dpl = dpl;
		if (dpl != 0) {
			setCodedSquelch(dpl, 'D');
		}
	}

	private void writeData(int[] bytes) {
		if (allowQueueing) {
			writeStack.add(bytes);
		}
	}

	@Override
	public void setFrequency(double freqMhz) {
		int[] data = new int[5];
		long freqHz = (long) (freqMhz * 1E5);
		byte[] bcd = Utility.toBCDArray(freqHz, 8);

		data[0] = bcd[0];
		data[1] = bcd[1];
		data[2] = bcd[2];
		data[3] = bcd[3];
		data[4] = ft857dCommandSetFrequencySuffix;

		if (allowQueueing) {
			writeData(data);
		}
	}

	@Override
	public void setModeName(StandardModeName modeName) {
		try {
			this.modeName = modeName;
			int[] opmode = { 0x00, 0x00, 0x00, 0x00, 0x00 };
			opmode[0] = ft857dCommandOperatingMode_FM;
			opmode[4] = ft857dCommandOperatingModeCommand;
			switch (modeName) {
				case FM ->
					opmode[0] = ft857dCommandOperatingMode_FM;
				case NFM ->
					opmode[0] = ft857dCommandOperatingMode_FMN;
				case AM ->
					opmode[0] = ft857dCommandOperatingMode_AM;
				case USB ->
					opmode[0] = ft857dCommandOperatingMode_USB;
				case LSB ->
					opmode[0] = ft857dCommandOperatingMode_LSB;
				case CW ->
					opmode[0] = ft857dCommandOperatingMode_CW;
				case CWR ->
					opmode[0] = ft857dCommandOperatingMode_CWR;
				case RTTY ->
					opmode[0] = ft857dCommandOperatingMode_RTTY;
				case PKTFM ->
					opmode[0] = ft857dCommandOperatingMode_PKT;
				default -> throw new IllegalArgumentException("Unexpected value: " + modeName);
	
			}
			if (allowQueueing) {
				writeData(opmode);
			}
		} catch (IllegalArgumentException ex) {
			LOG.log(Level.SEVERE, ex.getMessage());
		}
	}

	private void decode(int[] rxByte) {
		if (rxByte == null) {
			return;
		}
		try {
			switch (lastOpCode) {
				case ft857dCommandGetReceiverStatus:
					if (rxByte.length == 1) {
						String rxData = String.valueOf(ft857dCommandGetReceiverStatus) + ":" + Arrays.toString(rxByte);
						receiverEvent.firePropertyChange(ReceiverEvent.RX_DATA, null, rxData);
	
						int currentRssi = rxByte[0] & ft857dRssiMask;
						rssiSum += currentRssi;
						rssiAverageCount++;
	
						if (rssiAverageCount == averagingFactor) {
							rssiAverageCount = 0;
							rssi = rssiSum / averagingFactor;
							rssiSum = 0;
							if (isRssiEnabled() && !isScanning()) {
								getReceiverEvent().firePropertyChange(ReceiverEvent.RSSI, null, rssi);
							}
							if (rssi != currentRssi) {
								rssi = currentRssi;
							}
							rssiTimer.restart();
						}
	
						boolean isBusy = false;
						if ((rxByte[0] & ft857dBusyMask) == ft857dBusyMask) {
							isBusy = true;
						}
						receiverEvent.firePropertyChange(ReceiverEvent.BUSY, busy, isBusy);
						if (busy != isBusy) {
							busy = isBusy;
						}
						if (busy && accessMode == AccessMode.CSQ) {
							busyTimer.restart();
						}
						boolean isPLDetect = false;
						if ((rxByte[0] & ft857dPLDecodeMask) == ft857dPLDecodeMask) {
							isPLDetect = true;
						}
						receiverEvent.firePropertyChange(ReceiverEvent.PL_DECODE, plDetect, isPLDetect);
						if (plDetect != isPLDetect) {
							plDetect = isPLDetect;
						}
						if (plDetect && accessMode != AccessMode.CSQ) {
							busyTimer.restart();
						}
	
						boolean isRxCentered = false;
						if ((rxByte[0] & ft857dRxCenteredMask) == ft857dRxCenteredMask) {
							isRxCentered = true;
						}
						receiverEvent.firePropertyChange(ReceiverEvent.RX_CENTERED, rxCentered, isRxCentered);
						if (rxCentered != isRxCentered) {
							rxCentered = isRxCentered;
						}
	
						lastOpCode = ft857dCommandNull;
					}
					break;

				case ft857dCommandGetFrequencyMode:
					if (rxByte.length == 5) {
						String rxData = String.valueOf(ft857dCommandGetFrequencyMode) + ":" + Arrays.toString(rxByte);
						receiverEvent.firePropertyChange(ReceiverEvent.RX_DATA, null, rxData);
	
						int b0 = rxByte[0] * 16777216;
						int b1 = rxByte[1] * 65535;
						int b2 = rxByte[2] * 256;
						int b3 = rxByte[3] * 1;
	
						double f = (b0 + b1 + b2 + b3) / 1E5;
	
						receiverEvent.firePropertyChange(ReceiverEvent.CURRENT_FREQ, this.freqMhz, f);
						transmitterEvent.firePropertyChange(TransmitterEvent.CURRENT_FREQ, this.freqMhz, f);
						if (Math.abs(this.freqMhz - f) > 0.0000001) {
							this.freqMhz = f;
						}
	
						StandardModeName m = translateMode(rxByte[4]);
						receiverEvent.firePropertyChange(ReceiverEvent.CURRENT_MODE, this.modeName, m);
						transmitterEvent.firePropertyChange(TransmitterEvent.CURRENT_MODE, this.modeName, m);
						this.modeName = m;
	
						lastOpCode = ft857dCommandNull;
					}
					break;
					
				case ft857dCommandGetTransmitterStatus:
					if (rxByte.length == 1) {
						String txData = String.valueOf(ft857dCommandGetTransmitterStatus) + ":" + Arrays.toString(rxByte);
						transmitterEvent.firePropertyChange(TransmitterEvent.TX_DATA, null, txData);
	
						int currentPowerLevel = rxByte[0] & ft857dPowerMeterMask;
						transmitterEvent.firePropertyChange(TransmitterEvent.TX_POWER_OUTPUT_WATTS, powerLevel,
								currentPowerLevel);
						if (powerLevel != currentPowerLevel) {
							powerLevel = currentPowerLevel;
						}
						boolean isPTT = false;
						if ((rxByte[0] & ft857dPTTMask) == ft857dPTTMask) {
							isPTT = true;
						}
						transmitterEvent.firePropertyChange(TransmitterEvent.PTT, ptt, isPTT);
						ptt = isPTT;
	
						boolean isHighVSWR = false;
						if ((rxByte[0] & ft857dHighVSWRMask) == ft857dHighVSWRMask) {
							isHighVSWR = true;
						}
						receiverEvent.firePropertyChange(ReceiverEvent.PL_DECODE, highVSWR, isHighVSWR);
						highVSWR = isHighVSWR;
	
						lastOpCode = ft857dCommandNull;
					}
					break;
					
				default:
					break;
			}
		} catch (NullPointerException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void close() {
		stopRadio();
		super.close();
		for (Handler handler : LOG.getHandlers()) {
			LOG.removeHandler(handler);
	        handler.close();
		}
	}

	@Override
	public void startRadio() {
		try (ScheduledExecutorService writeScheduler = Executors.newSingleThreadScheduledExecutor()) {
			allowQueueing = true;
			writerHandle = writeScheduler.scheduleAtFixedRate(new Writer(), WRITE_PAUSE, WRITE_PAUSE, TimeUnit.MILLISECONDS);
		}
	}

	private class Writer implements Runnable {
		
		@Override
		public void run() {
			try {
				writeData(ft857dCommandBlockGetReceiverStatus);
				writeData(ft857dCommandBlockGetTransmitterStatus);
				writeData(ft857dCommandBlockGetFrequencyMode);
				
				while (!writeStack.isEmpty()) {
					int[] data = writeStack.take();
					if (data.length != 5) {
						LOG.log(Level.WARNING, "Corrupt Data Word: {0}", Arrays.toString(data));
					} else {
						LOG.log(Level.INFO, "Queue Size: {0}", writeStack.size());
						lastOpCode = data[4];
						sequence++;
						tty.write(data);
						receiverEvent.firePropertyChange(AbstractRadioReceiver.TX_DATA, null, data);
						LOG.log(Level.INFO, "Write to TTY port: {0}", sequence + " ->  " + Arrays.toString(data));
					}
				}
			} catch (final InterruptedException ex) {
				LOG.log(Level.SEVERE, ex.getMessage());
				Thread.currentThread().interrupt();
			} finally {
				writeStack.clear();
			}
		}
	}

	@Override
	public void stopRadio() {
		allowQueueing = false;
		if (writerHandle != null) {
			writerHandle.cancel(true);
		}
		rssiTimer.stop();
		busyTimer.stop();
		heartbeatTimer.stop();
		clearRSSI();
		clearBusy();
		connected = false;
	}

	@Override
	public long getVersionUID() {
		return serialVersionUID;
	}

	private StandardModeName translateMode(int modeName) {
		return switch (modeName) {
			case ft857dCommandOperatingMode_FM -> StandardModeName.FM;
			case ft857dCommandOperatingMode_WFM -> StandardModeName.WFM;
			case ft857dCommandOperatingMode_AM -> StandardModeName.AM;
			case ft857dCommandOperatingMode_USB -> StandardModeName.USB;
			case ft857dCommandOperatingMode_LSB -> StandardModeName.LSB;
			case ft857dCommandOperatingMode_CW -> StandardModeName.CW;
			case ft857dCommandOperatingMode_CWR -> StandardModeName.CWR;
			case ft857dCommandOperatingMode_FMN -> StandardModeName.NFM;
			case ft857dCommandOperatingMode_RTTY -> StandardModeName.RTTY;
			case ft857dCommandOperatingMode_PKT -> StandardModeName.PKTUSB;
			case ft857dCommandOperatingMode_CWN -> StandardModeName.CW;
			default -> null;
		};
	}

	private void processData(String data) {
		connected = true;
		heartbeatTimer.restart();
		if (data.length() == 12) {
			final int[] ia = { 
				Integer.parseInt(data.substring(0, 2)), 
				Integer.parseInt(data.substring(2, 4)),
				Integer.parseInt(data.substring(4, 6)), 
				Integer.parseInt(data.substring(6, 8)),
				Integer.parseInt(data.substring(8, 10)) };
			decode(ia);
		}
	}

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
	public JPanel[] getConfigurationComponentArray() {
		return new SerialComponent(tty).getSettingsPanelArray(SETTINGS_TITLE_PREFIX);
	}
    
    @Override
	public String getInterfaceName() {
		return tty.getPortName();
	}

	@Override
	public String getManufacturer() {
		return MANUFACTURER;
	}

	@Override
	public String getModel() {
	return MODEL;
	}

	@Override
	public boolean isSupportsAGC() {
		return SUPPORTS_AGC;
	}

	@Override
	public boolean isSupportsAFC() {
		return SUPPORTS_AFC;
	}

	@Override
	public boolean isSupportsNoiseBlanker() {
		return SUPPORTS_NOISE_BLANKER;
	}

	@Override
	public boolean isSupportsVoiceScan() {
		return SUPPORTS_VOICE_SCAN;
	}

	@Override
	public boolean isSupportsIFShift() {
		return SUPPORTS_IF_SHIFT;
	}

	@Override
	public boolean isSupportsVolumeSet() {
		return SUPPORTS_VOLUME_CONTROL;
	}

	@Override
	public boolean isSupportsSquelchSet() {
		return SUPPORTS_SQUELCH_CONTROL;
	}

	@Override
	public boolean isSupportsMultiMode() {
		return SUPPORTS_MULTI_MODE;
	}

	@Override
	public boolean isSupportsMultiFilter() {
		return SUPPORTS_MULTI_FILTER;
	}

	@Override
	public boolean isSupportsCountryCodeRetrieval() {
		return SUPPORTS_COUNTRY_CODE_RETRIEVAL;
	}

	@Override
	public boolean isSupportsFirmwareRetrieval() {
		return SUPPORTS_FIRMWARE_RETRIEVAL;
	}

	@Override
	public boolean isSupportsDSP() {
		return SUPPORTS_DSP; 
	}

	@Override
	public boolean isSupportsSINAD() {
		return SUPPORTS_SINAD;
	}

	@Override
	public boolean isSupportsBER() {
		return SUPPORTS_BER;
	}

	@Override
	public boolean isSupportsRSSI() {
		return SUPPORTS_RSSI;
	}

	@Override
	public Integer[] getAvailableFilters() {
		return FILTERS_HZ.clone();
	}

	@Override
	public boolean isSupportsFeatureCodeRetrieval() {
		return SUPPORTS_FEATURE_CODE_RETRIEVAL;
	}

	@Override
	public double getMinRxFreq() {
		return MINIMUM_RX_FREQ;
	}

	@Override
	public double getMaxRxFreq() {
		return MAXIMUM_RX_FREQ;
	}

	@Override
	public boolean isSupportsCOR() {
		return SUPPORTS_COR;
	}

	@Override
	public boolean isSupportsAttenuator() {
		return SUPPORTS_ATTENUATOR;
	}

	@Override
	public BaudRate[] getAvailableBaudRates() {
		return AVAILABLE_BAUD_RATES.clone();
	}

	@Override
	public BaudRate getDefaultBaudRate() {
		return BAUD_RATE;
	}

	@Override
	public DataBits getDefaultDataBits() {
		return DATA_BITS;
	}

	@Override
	public StopBits getDefaultStopBits() {
		return STOP_BITS;
	}

	@Override
	public Parity getDefaultParity() {
		return PARITY;
	}

	@Override
	public FlowControl getDefaultFlowControlIn() {
		return FLOW_CONTROL_IN;
	}

	@Override
	public FlowControl getDefaultFlowControlOut() {
		return FLOW_CONTROL_OUT;
	}

	@Override
	public boolean isSerialBaudRateFixed() {
		return SERIAL_BAUD_RATE_FIXED;
	}

	@Override
	public boolean isSerialParametersFixed() {
		return SERIAL_PARAMETERS_FIXED;
	}

	@Override
	public int getRssiUpperLimit() {
		return RSSI_UPPER_LIMIT;
	}

	@Override
	public int getRssiLowerLimit() {
		return RSSI_LOWER_LIMIT;
	}

	@Override
	public boolean supportsRTS() {
		return SUPPORTS_RTS;
	}

	@Override
	public void setCTS(boolean cts) {

	}

	@Override
	public void setDSR(boolean dsr) {
		
	}

	@Override
	public boolean supportsDTR() {
		return SUPPORTS_DTR;
	}

	@Override
	public StandardModeName[] getModeNameValues() {
		return SUPPORTED_MODES.clone();
	}

	@Override
	public double getDefaultNoiseFloor() {
		return DEFAULT_NOISE_FLOOR;
	}

	@Override
	public double getDefaultSaturation() {
		return DEFAULT_SATURATION;
	}

	@Override
	public double getAdjacentChannelRejectiondB() {
		return ADJACENT_CHANNEL_REJECTION_DB;
	}

	@Override
	public double getSignalReqFor12dBSINADdBm() {
		return SIGNAL_REQ_FOR_12DB_SINAD;
	}

	@Override
	public double getSignalReqFor20dBQuietingdBm() {
		return SIGNAL_REQ_FOR_20DB_QUIETING;
	}

	@Override
	public double getSignalReqFor5PctBERdBm() {
		return SIGNAL_REQ_FOR_5PCT_BER;
	}

	@Override
	public EncryptionProtocol[] getEncryptionProtocols() {
		return ENCRYPTION_PROTOCOLS.clone();
	}
	
	@Override
	public double getMinAttenuator() {
		return MINIMUM_ATTN;
	}

	@Override
	public double getMaxAttenuator() {
		return MAXIMUM_ATTN;
	}
	
	@Override
	public String getDefaultSerialNumber() {
		return DEFAULT_SERIAL_NUMBER;
	}
}
