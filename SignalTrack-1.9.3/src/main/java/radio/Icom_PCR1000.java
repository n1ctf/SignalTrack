package radio;

import java.awt.event.ActionListener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.io.File;

import java.text.DecimalFormat;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
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

public class Icom_PCR1000 extends AbstractRadioReceiver implements TeletypeInterface  {

	private static final long serialVersionUID = 8250966856595400445L;

	private static final Logger LOG = Logger.getLogger(Icom_PCR1000.class.getName());

	private static final String MANUFACTURER = "ICOM";
	private static final String MODEL = "PCR1000";
	private static final String DEFAULT_SERIAL_NUMBER = "00000";
	
	public static final String SETTINGS_TITLE_PREFIX = "Radio";

		
	// TTY Port Constants
	private static final BaudRate BAUDRATE = BaudRate.BAUDRATE_9600;
	private static final Parity PARITY = Parity.NONE;
	private static final StopBits STOP_BITS = StopBits.STOPBITS_1;
    private static final DataBits DATA_BITS = DataBits.DATABITS_8;
    private static final FlowControl FLOW_CONTROL_IN = FlowControl.NONE;
    private static final FlowControl FLOW_CONTROL_OUT = FlowControl.NONE;
    private static final boolean SUPPORTS_RTS = true;
    private static final boolean SUPPORTS_DTR = true;
    private static final boolean SERIAL_PARAMETERS_FIXED = true;
	private static final boolean SERIAL_BAUD_RATE_FIXED = false;
	
	// RF Deck Constants
	private static final boolean SUPPORTS_SINAD = true;
	private static final boolean SUPPORTS_BER = false;
	private static final boolean SUPPORTS_RSSI = true;
	private static final boolean SUPPORTS_AGC = false;
	private static final boolean SUPPORTS_AFC = false;
	private static final boolean SUPPORTS_ATTENUATOR = true;
	private static final boolean SUPPORTS_NOISE_BLANKER = true;
	private static final boolean SUPPORTS_VOLUME_CONTROL = true;
	private static final boolean SUPPORTS_SQUELCH_CONTROL = true;
	private static final boolean SUPPORTS_COR = true;
	private static final boolean SUPPORTS_MULTI_MODE = true;
	private static final boolean SUPPORTS_MULTI_FILTER = true;
	private static final boolean SUPPORTS_COUNTRY_CODE_RETRIEVAL = true;
	private static final boolean SUPPORTS_FIRMWARE_RETRIEVAL = true;
	private static final boolean SUPPORTS_IF_SHIFT = false;
	private static final boolean SUPPORTS_VOICE_SCAN = false;
	private static final boolean SUPPORTS_DSP = false;
	private static final boolean SUPPORTS_FEATURE_CODE_RETRIEVAL = false;
	private static final double MINIMUM_RX_FREQ = 0.050;
	private static final double MAXIMUM_RX_FREQ = 1300.0;
	private static final double MINIMUM_ATTN = 0.0;
    private static final double MAXIMUM_ATTN = 125.0;
	private static final int RSSI_UPPER_LIMIT = 230;
	private static final int RSSI_LOWER_LIMIT = 0;
	private static final double DEFAULT_NOISE_FLOOR = -135;
	private static final double DEFAULT_SATURATION = -21;
	private static final double ADJACENT_CHANNEL_REJECTION_DB = 60.0;
	private static final double SIGNAL_REQ_FOR_12DB_SINAD = -115.0; 
	private static final double SIGNAL_REQ_FOR_20DB_QUIETING = -117.0; 
	private static final double SIGNAL_REQ_FOR_5PCT_BER = -118; 

	private static final String SHUTDOWN_REQ = "SHUTDOWN";
	private static final int UPDATE_PERIOD = 100;
	private static final int TERMINATION_WAIT_PERIOD = 250;
	private static final int TERMINATE_WAIT = 40;
	private static final int BUSY_CLEARANCE_PERIOD = 500;
	private static final int RSSI_CLEARANCE_PERIOD = 500;
	private static final int WRITE_PAUSE = 20;

	private static final EncryptionProtocol[] ENCRYPTION_PROTOCOLS = { EncryptionProtocol.CLEAR };
	
	private static final BaudRate[] AVAILABLE_BAUD_RATES = { BaudRate.BAUDRATE_1200, BaudRate.BAUDRATE_4800,
			BaudRate.BAUDRATE_9600, BaudRate.BAUDRATE_19200, BaudRate.BAUDRATE_38400 };

	private static final Integer[] FILTERS_HZ = { 2800, 6000, 15000, 50000, 230000 };
	
	private static final StandardModeName[] SUPPORTED_MODES = { 
		StandardModeName.WFM,
		StandardModeName.FM, 
		StandardModeName.NFM, 
		StandardModeName.AM,
		StandardModeName.USB, 
		StandardModeName.LSB 
	};

	public static final String crlf = "\r\n";
	public static final double pcrdbl1e6 = 1000000.0;
	public static final String pcrBoolOn = "01";
	public static final String pcrBoolOff = "00";
	public static final String pcrFmt = "00";
	public static final String pcrFrFmt = "0000000000";

	public static final String pcrQueryFirmwareVersion = "G4?";
	public static final String pcrQueryDSP = "GD?";
	public static final String pcrQueryCountry = "GE?";
	public static final String pcrQueryRxOn = "H1?";
	public static final String pcrQuerySquelchStatus = "I0?";
	public static final String pcrQuerySignalStrength = "I1?";
	public static final String pcrQueryFrequencyOffset = "I2?";
	public static final String pcrQueryDTMFTone = "I3?";

	public static final String pcrCommandRxOn = "H101";
	public static final String pcrCommandRxOff = "H100";
	public static final String pcrCommandAutoUpdateOn = "G301";
	public static final String pcrCommandAutoUpdateOff = "G300";
	public static final String pcrCommandBandScopeOff = "ME0000100000000000000";

	public static final String pcrCommandAFCPrefix = "J44";
	public static final String pcrCommandAGCPrefix = "J45";
	public static final String pcrCommandATTPrefix = "J47";
	public static final String pcrCommandNBPrefix = "J46";
	public static final String pcrCommandSquelchPrefix = "J41";
	public static final String pcrCommandCTCSSPrefix = "J51";
	public static final String pcrCommandVoiceScanPrefix = "J50";
	public static final String pcrCommandVolumeLevelPrefix = "J40";
	public static final String pcrCommandFrequencyPrefix = "K0";
	public static final String pcrCommandIFShiftPrefix = "J43";

	public static final String pcrReplyHeaderAck = "G0";

	public static final String pcrReplyHeaderReceiveStatus = "I0\\?";
	public static final String pcrReplyHeaderRSSIChange = "I1\\?";
	public static final String pcrReplyHeaderSignalOffset = "I2\\?";
	public static final String pcrReplyHeaderDTMFDecode = "I3\\?";
	public static final String pcrReplyHeaderWaveFormData = "NE10";
	public static final String pcrReplyHeaderScanStatus = "H9";
	public static final String pcrReplyHeaderFirmware = "G4\\?";
	public static final String pcrReplyHeaderCountry = "GE\\?";
	public static final String pcrReplyHeaderOptionalDevice = "GD";
	public static final String pcrReplyHeaderPower = "H1\\?";
	public static final String pcrReplyHeaderProtocol = "G2";
	public static final String pcrInitialize = "LE20050" + crlf + "LE20040";

	public static final String pcrCommandTrackingFilterAutomatic = "LD82NN";

	public static final String pcrCommandModeLSB = "00";
	public static final String pcrCommandModeUSB = "01";
	public static final String pcrCommandModeAM = "02";
	public static final String pcrCommandModeCW = "03";
	public static final String pcrCommandModeNULL = "04";
	public static final String pcrCommandModeNarrowFM = "05";
	public static final String pcrCommandModeWideFM = "06";

	public static final String pcrCommandBaud1200 = "G100";
	public static final String pcrCommandBaud2400 = "G101";
	public static final String pcrCommandBaud4800 = "G102";
	public static final String pcrCommandBaud9600 = "G103";
	public static final String pcrCommandBaud19200 = "G104";
	public static final String pcrCommandBaud38400 = "G105";

	private static final int READ_BUFFER_LEN = 2048;
	
	private boolean voiceScan;
	
	private String lastCommandSent;
	private long sequence;

	private final Object onLineHold = new Object();
	private final Object scanHold = new Object();

	private Runnable updateTimer;
	private ScheduledExecutorService updateTimerScheduler;

	private Timer busyTimer;
	private Timer rssiTimer;

	private final ExecutorService executor = Executors.newCachedThreadPool();

	private final BlockingQueue<String> itemsToWrite = new ArrayBlockingQueue<>(32);

	private volatile boolean isConnected;
	private volatile boolean shuttingDown;
	private volatile boolean terminated;
	private volatile boolean allowQueueing;

	private AbstractTeletypeController tty = 
	    AbstractTeletypeController.getTTyPortInstance(AbstractTeletypeController.getCatalogMap().getKey("JSSC TTY Port v2.9.5"), false);
	
	private PropertyChangeListener serialPortPropertyChangeListener;

	private byte[] readBuffer;

	// this allows the retrieval of supported features, which is independent of the calFile.
	public Icom_PCR1000() { } 	

	public Icom_PCR1000(File calFile, Boolean clearAllPreferences) {
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

		updateTimerScheduler = Executors.newSingleThreadScheduledExecutor();
		updateTimer = this::updateRequest;
	}

	@Override
	public void setAFC(boolean afc) {
		this.afc = afc;
		cmdSwitch(afc, pcrCommandAFCPrefix);
	}

	@Override
	public void setAGC(boolean agc) {
		this.agc = agc;
		cmdSwitch(agc, pcrCommandAGCPrefix);
	}

	@Override
	public void setAttenuator(double attenuator) {
		this.attenuator = attenuator;
		cmdSwitch(attenuator > 0, pcrCommandATTPrefix);
	}

	@Override
	public void setNoiseBlanker(boolean noiseBlanker) {
		this.noiseBlanker = noiseBlanker;
		cmdSwitch(noiseBlanker, pcrCommandNBPrefix);
	}

	@Override
	public void setIFShift(int ifShift) {
		if ((ifShift >= 0) && (ifShift <= 255)) {
			this.ifShift = ifShift;
			if (allowQueueing) {
				writeData(pcrCommandIFShiftPrefix + Utility.integerToHex(ifShift) + crlf);
			}
		}
	}

	@Override
	public void setSquelch(int squelch) {
		if ((squelch >= 0) && (squelch <= 255)) {
			this.squelch = squelch;
			if (allowQueueing) {
				writeData(pcrCommandSquelchPrefix + Utility.integerToHex(percentToInteger(squelch)) + crlf);
			}
		}
	}

	@Override
	public void setVolume(int volume) {
		if ((volume >= 0) && (volume <= 255)) {
			this.volume = volume;
			if (allowQueueing) {
				writeData(pcrCommandVolumeLevelPrefix + Utility.integerToHex(percentToInteger(volume)) + crlf);
			}
		}
	}

	@Override
	public void setPL(int toneSquelch) {
		double toneSquelchFreq = toneSquelch / 10D;
		if (toneSquelchFreq >= 0
				&& toneSquelchFreq <= Double.parseDouble(getToneSquelchValues()[getToneSquelchValues().length - 1])) {
			this.pl = toneSquelch;
			final String toneSquelchHexCode = Utility.integerToHex(getToneSquelchOrdinal(toneSquelchFreq));
			if (allowQueueing) {
				writeData(pcrCommandCTCSSPrefix + toneSquelchHexCode + crlf);
			}
		}
	}

	private int getToneSquelchOrdinal(double toneSquelchFreq) {
		int code = 0;
		for (int i = 0; i < getToneSquelchValues().length; i++) {
			if (Utility.equals(toneSquelchFreq, Double.parseDouble(getToneSquelchValues()[i]))) {
				code = i;
			}
		}
		return code;
	}

	private void setFrequencyModeFilter(double frequency, StandardModeName mode, int filter) {
		if ((frequency >= MINIMUM_RX_FREQ) && (frequency <= MAXIMUM_RX_FREQ)) {
			this.frequency = frequency;
			this.modeName = mode;
			this.filterHz = filter;
			int m = getModeOrdinal(mode);
			int f = getFilterHzOrdinal(filter);
			if (m != -1 && f != -1) {
				sendFrequencyToPcr(frequency, m, f);
			}
		}
	}

	private int getModeOrdinal(StandardModeName mode) {
		int m = -1;
		for (int i = 0; i < SUPPORTED_MODES.length; i++) {
			if (SUPPORTED_MODES[i] == mode) {
				m = i;
			}
		}
		return m;
	}

	private int getFilterHzOrdinal(int filterHz) {
		int f = -1;
		for (int i = 0; i < FILTERS_HZ.length; i++) {
			if (FILTERS_HZ[i] == filterHz) {
				f = i;
				break;
			}
		}
		return f;
	}

	private void pcrDecoder(String data) {
		receiverEvent.firePropertyChange(ReceiverEvent.RX_DATA, null, data);
		freqChangeConfirmed = false;
		try {
			switch (data.substring(0, 2)) {
				case pcrReplyHeaderAck -> {
					switch (data.substring(2, 4)) {
						case "01" ->
							receiverEvent.firePropertyChange(ReceiverEvent.RECEIVED_DATA_ERROR, null, "Received Data Error");
						case "00" -> {
							receiverEvent.firePropertyChange(ReceiverEvent.ACK, null, "Data Acknowledged");
							if (lastCommandSent.contains(pcrCommandFrequencyPrefix)) {
								freqChangeConfirmed = true;
								receiverEvent.firePropertyChange(ReceiverEvent.CURRENT_FREQ, null, frequency);
							}
						}
						default -> receiverEvent.firePropertyChange(ReceiverEvent.RECEIVED_DATA_ERROR, null, "Received Data Error");
					}
				}
		
				case pcrReplyHeaderReceiveStatus -> {
					busy = decodeBusyStatus(data.substring(2, 4));
					receiverEvent.firePropertyChange(ReceiverEvent.BUSY, null, busy);
					busyTimer.restart();
				}
		
				case pcrReplyHeaderRSSIChange -> {
					if (data.length() == 4) {
						rssi = Integer.valueOf(data.substring(2, 4), 16);
						if (rssiEnabled && !scanEnabled) {
							receiverEvent.firePropertyChange(ReceiverEvent.RSSI, null, rssi);
						}
						if (rssiEnabled && scanEnabled) {
							synchronized (scanHold) {
								if (freqChangeConfirmed) {
									scanHold.notifyAll();
								}
							}
						}
						rssiTimer.restart();
					}
				}
		
				case pcrReplyHeaderSignalOffset -> {
					String signalOffset = data.substring(2, 4);
					receiverEvent.firePropertyChange(ReceiverEvent.SIGNAL_OFFSET, null, signalOffset);
				}
		
				case pcrReplyHeaderDTMFDecode -> {
					String dtmfDecode = data.substring(2, 4);
					receiverEvent.firePropertyChange(ReceiverEvent.DTMF_DECODE, null, dtmfDecode);
				}
		
				case pcrReplyHeaderWaveFormData -> {
					String waveFormData = data.substring(2, 4);
					receiverEvent.firePropertyChange(ReceiverEvent.WAVEFORM_DATA, null, waveFormData);
				}
		
				case pcrReplyHeaderScanStatus -> {
					String scanStatus = data.substring(2, 4);
					receiverEvent.firePropertyChange(AbstractScanner.SCAN_STATUS, null, scanStatus);
				}
		
				case pcrReplyHeaderFirmware -> {
					String firmware = data.substring(2, 4);
					receiverEvent.firePropertyChange(ReceiverEvent.FIRMWARE, null, firmware);
				}
		
				case pcrReplyHeaderCountry -> {
					String country = data.substring(2, 4);
					receiverEvent.firePropertyChange(ReceiverEvent.COUNTRY, null, country);
				}
		
				case pcrReplyHeaderOptionalDevice -> {
					String dsp = data.substring(2, 4);
					receiverEvent.firePropertyChange(ReceiverEvent.DSP, null, dsp);
				}
		
				case pcrReplyHeaderPower -> {
					synchronized (onLineHold) {
						boolean power = decodePowerStatus(data.substring(2, 4));
						receiverEvent.firePropertyChange(ReceiverEvent.POWER_STATE_CHANGE, null, power);
						if (power) {
							isConnected = true;
							onLineHold.notifyAll();
						}
					}
				}
		
				case pcrReplyHeaderProtocol -> {
					String protocol = data.substring(2, 4);
					receiverEvent.firePropertyChange(ReceiverEvent.PROTOCOL, null, protocol);
				}
		
				default -> throw new IllegalArgumentException("Unexpected Reply Header: " + modeName);
			}
		} catch (IllegalArgumentException ex) {
			LOG.log(Level.SEVERE, ex.getMessage());
		}
	}

	private boolean decodePowerStatus(String str) {
		return "01".equals(str);
	}

	private boolean decodeBusyStatus(String str) {
		return "04".equals(str);
	}

	@Override
	public void startRadio() {
		isConnected = false;
		itemsToWrite.clear();
		allowQueueing = true;
		tty.openPort();
		if (allowQueueing) {
			writeData(pcrInitialize + crlf);
		}
		if (allowQueueing) {
			writeData(pcrCommandRxOn + crlf);
		}
		if (allowQueueing) {
			writeData(pcrCommandAutoUpdateOff + crlf);
		}
		if (allowQueueing) {
			writeData(pcrQueryFirmwareVersion + crlf);
		}
		if (allowQueueing) {
			writeData(pcrQueryCountry + crlf);
		}
		if (allowQueueing) {
			writeData(pcrQueryDSP + crlf);
		}
		setFrequencyModeFilter(frequency, modeName, filterHz);
		setVolume(volume);
		setSquelch(squelch);
		setPL(pl);
		cmdSwitch(voiceScan, pcrCommandVoiceScanPrefix);
		setIFShift(ifShift);
		cmdSwitch(agc, pcrCommandAGCPrefix);
		cmdSwitch(afc, pcrCommandAFCPrefix);
		cmdSwitch(noiseBlanker, pcrCommandNBPrefix);
		cmdSwitch(attenuator > 0, pcrCommandATTPrefix);
		if (allowQueueing) {
			writeData(pcrCommandBandScopeOff + crlf);
		}
		if (allowQueueing) {
			writeData(pcrQueryRxOn + crlf);
		}
		if (isSinadEnabled()) {
			startSinad();
		}
		executor.execute(new RequestReadyStatus());
	}

	private class RequestReadyStatus implements Runnable {

		@Override
		public void run() {
			writeData(pcrQueryRxOn + crlf);
			synchronized (onLineHold) {
				try {
					while (!isConnected) {
						onLineHold.wait(1000);
					}
				} catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				}
			}
			if (isConnected) {
				updateTimerScheduler.scheduleWithFixedDelay(updateTimer, UPDATE_PERIOD, UPDATE_PERIOD,
						TimeUnit.MILLISECONDS);
			}
		}
	}

	private class InitiateRadioStop implements Runnable {

		@Override
		public void run() {
			try {
				tty.closePort();
				stopSinad();
				updateTimerScheduler.shutdown();
				rssiTimer.stop();
				busyTimer.stop();
				itemsToWrite.clear();
				if (allowQueueing) {
					writeData(pcrCommandAutoUpdateOff + crlf);
				}
				if (allowQueueing) {
					writeData(pcrCommandRxOff + crlf);
				}
				Thread.sleep(80);
				if (allowQueueing) {
					writeData(pcrQueryRxOn + crlf);
				}
				allowQueueing = false;
				itemsToWrite.clear();
				clearRSSI();
				clearBusy();
				receiverEvent.firePropertyChange(ReceiverEvent.CANCEL_EVENTS, null, true);
				updateTimerScheduler.awaitTermination(TERMINATION_WAIT_PERIOD, TimeUnit.MILLISECONDS);
				receiverEvent.firePropertyChange(AbstractRadioReceiver.ONLINE, null, false);
				receiverEvent.firePropertyChange(ReceiverEvent.RADIO_THREADS_TERMINATED, null, true);
			} catch (final InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		}
	}

	private void sendFrequencyToPcr(double freq, int mode, int filter) {
		final DecimalFormat freqFormat = new DecimalFormat(pcrFrFmt);
		String strFr = pcrCommandFrequencyPrefix + freqFormat.format(freq * pcrdbl1e6);
		final DecimalFormat pcrFormat = new DecimalFormat(pcrFmt);
		strFr = strFr + pcrFormat.format(mode) + pcrFormat.format(filter);
		strFr += pcrFmt;
		if (allowQueueing) {
			writeData(strFr + crlf);
		}
	}

	private void cmdSwitch(boolean bln, String cmd) {
		String str = pcrBoolOff;
		if (bln) {
			str = pcrBoolOn;
		}
		if (allowQueueing) {
			writeData(cmd + str + crlf);
		}
	}

	private void updateRequest() {
		if (allowQueueing) {
			writeData(pcrQuerySignalStrength + crlf);
		}
		if (allowQueueing) {
			writeData(pcrQuerySquelchStatus + crlf);
		}
	}

	private void clearRSSI() {
		rssi = 0;
		if (rssiEnabled && !scanEnabled) {
			receiverEvent.firePropertyChange(ReceiverEvent.RSSI, null, rssi);
		}
	}

	private void clearBusy() {
		busy = false;
		receiverEvent.firePropertyChange(ReceiverEvent.BUSY, null, busy);
	}

	private void writeData(String data) {
		if (shuttingDown || terminated) {
			return;
		}
		try {
			itemsToWrite.put(data);
		} catch (final InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}

	@Override
	public void close() {
		shuttingDown = true;
		writeData(SHUTDOWN_REQ);
		stopRadio();
		super.close();
		for (Handler handler : LOG.getHandlers()) {
			LOG.removeHandler(handler);
	        handler.close();
		}		
	}

	private class Writer implements Runnable {

		@Override
		public void run() {
			try {
				while (!terminated) {
					if (!itemsToWrite.isEmpty()) {
						String data = itemsToWrite.take();
						LOG.log(Level.INFO, "Queue Size: {0}", itemsToWrite.size());
						lastCommandSent = data;
						sequence++;
						tty.write(data);
						receiverEvent.firePropertyChange(AbstractRadioReceiver.TX_DATA, null, data);
						Thread.sleep(WRITE_PAUSE);
						LOG.log(Level.INFO, "write to TTY port: {0}", sequence + " ->  " + data);
					} else {
						Thread.sleep(TERMINATE_WAIT);
					}
				}
			} catch (final InterruptedException ex) {
				Thread.currentThread().interrupt();
			} finally {
				terminated = true;
				itemsToWrite.clear();
			}
		}
	}

	private long percentToInteger(double percent) {
		return Math.round(percent * 99);
	}

	@Override
	public void stopRadio() {
		executor.execute(new InitiateRadioStop());
	}

	private void processData(String data) {
		String str = String.format("last command: %s reply: %s", lastCommandSent, data);
		LOG.log(Level.INFO, str);

		int iHStart;
		int iGStart;
		int iIStart;

		boolean sent;

		String rStr = data;

		do {
			sent = false;
			iHStart = rStr.indexOf('H', 0);
			iIStart = rStr.indexOf('I', 0);
			iGStart = rStr.indexOf('G', 0);
			if ((iHStart >= 0) && (rStr.length() >= (iHStart + 4))) {
				pcrDecoder(rStr.substring(iHStart, iHStart + 4));
				if (rStr.length() >= 4) {
					rStr = rStr.substring(iHStart + 4);
				}
				sent = true;
			}
			if ((iIStart >= 0) && (rStr.length() >= (iIStart + 4))) {
				pcrDecoder(rStr.substring(iIStart, iIStart + 4));
				if (rStr.length() >= 4) {
					rStr = rStr.substring(iIStart + 4);
				}
				sent = true;
			}
			if ((iGStart >= 0) && (rStr.length() >= (iGStart + 4))) {
				pcrDecoder(rStr.substring(iGStart, iGStart + 4));
				if (rStr.length() >= 4) {
					rStr = rStr.substring(iGStart + 4);
				}
				sent = true;
			}
		} while (sent);
	}

	@Override
	public int getRSSI() {
		return rssi;
	}

	@Override
	public long getVersionUID() {
		return serialVersionUID;
	}

	@Override
	public void setFrequency(String freqStr) {
		try {
			setFrequency(Double.parseDouble(freqStr));
		} catch (NumberFormatException ex) {
			setFrequency(0.0);
			receiverEvent.firePropertyChange(ReceiverEvent.EXCEPTION, null, ex);
		}
	}

	@Override
	public void setFrequency(double frequency) {
		setFrequencyModeFilter(frequency, modeName, filterHz);
	}

	@Override
	public void setSquelchMode(AccessMode accessMode) {
		this.accessMode = accessMode;
		switch (accessMode) {
		case CSQ -> setPL(0);
		case DPL -> setPL(0);
		case PL -> setPL(getPL());
		default -> setPL(0);
		}
	}

	@Override
	public boolean isConnected() {
		return isConnected;
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
		return BAUDRATE;
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
	public EncryptionProtocol[] getEncryptionProtocols() {
		return ENCRYPTION_PROTOCOLS.clone();
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
