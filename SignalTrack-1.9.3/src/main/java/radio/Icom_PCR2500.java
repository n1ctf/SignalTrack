package radio;

import java.awt.event.ActionListener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.io.File;

import java.text.DecimalFormat;

import java.util.Arrays;
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

import radio.AbstractRadioReceiver.EncryptionProtocol;
import radio.AbstractRadioReceiver.StandardModeName;
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

@SuppressWarnings("unused")
public class Icom_PCR2500 extends AbstractRadioReceiver implements TeletypeInterface {

    private static final long serialVersionUID = 8250966856595400445L;

    private static final Logger LOG = Logger.getLogger(Icom_PCR2500.class.getName());

    private static final String MANUFACTURER = "ICOM";
    private static final String MODEL = "PCR2500";
    private static final String DEFAULT_SERIAL_NUMBER = "00000";
    
    public static final String SETTINGS_TITLE_PREFIX = "Radio";
    
    private static final BaudRate BAUD_RATE = BaudRate.BAUDRATE_38400;
    private static final Parity PARITY = Parity.NONE;
    private static final StopBits STOP_BITS = StopBits.STOPBITS_1;
    private static final DataBits DATA_BITS = DataBits.DATABITS_8;
    private static final FlowControl FLOW_CONTROL_IN = FlowControl.NONE;
    private static final FlowControl FLOW_CONTROL_OUT = FlowControl.NONE;
    private static final boolean SUPPORTS_RTS = true;
    private static final boolean SUPPORTS_DTR = true;
    
    private static final boolean SUPPORTS_SINAD = true;
    private static final boolean SUPPORTS_BER = false;
    private static final boolean SUPPORTS_RSSI = true;
    private static final boolean SERIAL_PARAMETERS_FIXED = true;
    private static final boolean SERIAL_BAUD_RATE_FIXED = true;
    private static final boolean SUPPORTS_AGC = false;
    private static final boolean SUPPORTS_AFC = false;
    private static final boolean SUPPORTS_ATTENUATOR = true;
    private static final boolean SUPPORTS_NOISE_BLANKER = true;
    private static final boolean SUPPORTS_VOLUME_CONTROL = true;
    private static final boolean SUPPORTS_SQUELCH_CONTROL = true;
    private static final double MINIMUM_RX_FREQ = 0.050;
    private static final double MAXIMUM_RX_FREQ = 3300.0;
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
	private static final boolean SUPPORTS_COR = true;
	private static final boolean SUPPORTS_MULTI_MODE = true;
	private static final boolean SUPPORTS_MULTI_FILTER = true;
	private static final boolean SUPPORTS_COUNTRY_CODE_RETRIEVAL = true;
	private static final boolean SUPPORTS_FIRMWARE_RETRIEVAL = true;
	private static final boolean SUPPORTS_IF_SHIFT = false;
	private static final boolean SUPPORTS_VOICE_SCAN = false;
	private static final boolean SUPPORTS_DSP = false;
	private static final boolean SUPPORTS_FEATURE_CODE_RETRIEVAL = false;
	
    private static final String SHUTDOWN_REQ = "SHUTDOWN";
    private static final int UPDATE_PERIOD = 250;
    private static final int TERMINATE_WAIT = 40;
    private static final int BUSY_CLEARANCE_PERIOD = 500;
    private static final int RSSI_CLEARANCE_PERIOD = 500;
    private static final int WRITE_PAUSE = 10;

    private static final BaudRate[] AVAILABLE_BAUD_RATES = {
        BaudRate.BAUDRATE_300,
        BaudRate.BAUDRATE_1200,
        BaudRate.BAUDRATE_4800,
        BaudRate.BAUDRATE_9600,
        BaudRate.BAUDRATE_19200,
        BaudRate.BAUDRATE_38400
    };

	private static final EncryptionProtocol[] ENCRYPTION_PROTOCOLS = { EncryptionProtocol.CLEAR };
	
    private static final Integer[] FILTERS_HZ = {2800, 6000, 15000, 50000, 230000};

    private static final StandardModeName[] SUPPORTED_MODES = {
        StandardModeName.LSB,
        StandardModeName.USB,
        StandardModeName.AM,
        StandardModeName.CW,
        StandardModeName.NFM,
        StandardModeName.WFM,
        StandardModeName.DSTAR,
        StandardModeName.P25_PHASE_1
    };

    private static final String lf = "\n";
    private static final double pcrdbl1e6 = 1000000.0;
    private static final String pcrBoolOn = "01";
    private static final String pcrBoolOff = "00";
    private static final String pcrFmt = "00";
    private static final String pcrFrFmt = "0000000000";

    private static final String pcrQueryFirmwareVersion = "G4?";
    private static final String pcrQueryDSP = "GD?";
    private static final String pcrQueryCountry = "GE?";
    private static final String pcrQueryRxOn = "H1?";
    private static final String pcrQueryReceiveStatusRx1 = "I0?";
    private static final String pcrQueryRSSIChangeRx1 = "I1?";
    private static final String pcrQuerySignalOffsetRx1 = "I2?";
    private static final String pcrQueryDTMFDecodeRx1 = "I3?";

    private static final String pcrReplyHeaderAcknowledge = "G0";
    private static final String pcrReplyHeaderDSPStatus = "GD";
    private static final String pcrReplyHeaderReceiveStatus = "I0";
    private static final String pcrReplyHeaderRSSIChange = "I1";
    private static final String pcrReplyHeaderSignalOffset = "I2";
    private static final String pcrReplyHeaderDTMFDecode = "I3";
    private static final String pcrReplyHeaderWaveFormData = "NE1";
    private static final String pcrReplyHeaderScanStatus = "H9";
    private static final String pcrReplyHeaderFirmware = "G4";
    private static final String pcrReplyHeaderCountry = "GE";
    private static final String pcrReplyHeaderPower = "H1";
    private static final String pcrReplyHeaderProtocol = "G2";

    private static final String pcrCommandSetBaudRatePrefix = "G1";
    private static final String pcrCommandRxOn = "H101";
    private static final String pcrCommandRxOff = "H100";
    private static final String pcrCommandAutoUpdateOff = "G300";
    private static final String pcrCommandAutoUpdateOn = "G301";
    private static final String pcrCommandBandScopeOff = "ME0000100000000000000";
    private static final String pcrCommandFrequencyPrefix = "K";
    private static final String pcrCommandFrequencyPrefixRx1 = "K0";
    private static final String pcrCommandFrequencyPrefixRx2 = "K1";
    private static final String pcrCommandSquelchDelayPrefixRx1 = "J42";
    private static final String pcrCommandAGCPrefixRx1 = "J45";
    private static final String pcrCommandAGCPrefixRx2 = "J65";
    private static final String pcrCommandATTPrefixRx1 = "J47";
    private static final String pcrCommandATTPrefixRx2 = "J67";
    private static final String pcrCommandNBPrefixRx1 = "J46";
    private static final String pcrCommandNBPrefixRx2 = "J66";
    private static final String pcrCommandSquelchPrefixRx1 = "J41";
    private static final String pcrCommandSquelchPrefixRx2 = "J61";
    private static final String pcrCommandCTCSSPrefixRx1 = "J51";
    private static final String pcrCommandCTCSSPrefixRx2 = "J71";
    private static final String pcrCommandDCSPrefixRx1 = "J520";
    private static final String pcrCommandDCSPrefixRx2 = "J720";
    private static final String pcrCommandDCSNormal = "0";
    private static final String pcrCommandDCSReverse = "2";
    private static final String pcrCommandVoiceSquelchPrefixRx1 = "J50";
    private static final String pcrCommandVoiceSquelchPrefixRx2 = "J70";
    private static final String pcrCommandVolumeLevelPrefixRx1 = "J40";
    private static final String pcrCommandVolumeLevelPrefixRx2 = "J60";
    private static final String pcrCommandIFShiftPrefixRx1 = "J43";
    private static final String pcrCommandIFShiftPrefixRx2 = "J63";
	private static final String pcrCommandProgScanPrefixRx1 = "J48";
    private static final String pcrCommandProgScanPrefixRx2 = "J68";
    private static final String pcrCommandAudioEnabled = "JA201";
    private static final String pcrCommandAudioDisabled = "JA200";
    private static final String pcrCommandDualDiversity = "J0002";
    private static final String pcrCommandSingleDiversity = "J0001";
    private static final String pcrCommandNonDiversity = "J0000";
    private static final String pcrCommandClearAllSettingsRx1 = "J530000";
    private static final String pcrCommandClearAllSettingsRx2 = "J730000";
    private static final String pcrCommandOptionUnitPowerAuto = "JC000";
    private static final String pcrCommandOptionUnitPowerOn = "JC001";
    private static final boolean voiceScan = false;
    private static final boolean fastSquelch = false;
    
	private static final int READ_BUFFER_LEN = 2048;

    private String lastCommandSent;
    private long sequence;

    private final Object onLineHold = new Object();

    private Runnable updateRunnable;
    private ScheduledExecutorService updateTimerScheduler;

    private Timer busyTimer;
    private Timer rssiTimer;

    private final BlockingQueue<String> itemsToWrite = new ArrayBlockingQueue<>(32);

    private final ExecutorService executor = Executors.newCachedThreadPool();

    private volatile boolean isConnected;
    private volatile boolean shuttingDown;
    private volatile boolean terminated;
    private volatile boolean allowQueueing;
    
    private final AbstractTeletypeController tty = AbstractTeletypeController.getTTyPortInstance(AbstractTeletypeController.getCatalogMap().getKey("JSSC TTY Port v2.9.5"), false);

	private PropertyChangeListener serialPortPropertyChangeListener;

	private byte[] readBuffer;

    public Icom_PCR2500() { }

    public Icom_PCR2500(File calFile, Boolean clearAllPreferences) {
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
					final StringBuilder sb = new StringBuilder();
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
        final ActionListener busyTimerActionListener = event -> clearBusy();

        busyTimer = new Timer(BUSY_CLEARANCE_PERIOD, busyTimerActionListener);

        final ActionListener rssiTimerActionListener = event -> clearRSSI();

        rssiTimer = new Timer(RSSI_CLEARANCE_PERIOD, rssiTimerActionListener);

        updateRunnable = this::updateRequest;

    }

    @Override
    public void setAGC(boolean agc) {
        this.agc = agc;
        cmdSwitch(agc, pcrCommandAGCPrefixRx1);
        cmdSwitch(agc, pcrCommandAGCPrefixRx2);
    }

    @Override
    public void setAttenuator(double attenuator) {
        this.attenuator = attenuator;
        cmdSwitch(attenuator > 0, pcrCommandATTPrefixRx1);
        cmdSwitch(attenuator > 0, pcrCommandATTPrefixRx2);
    }

    @Override
    public void setNoiseBlanker(boolean noiseBlanker) {
        this.noiseBlanker = noiseBlanker;
        cmdSwitch(noiseBlanker, pcrCommandNBPrefixRx1);
        cmdSwitch(noiseBlanker, pcrCommandNBPrefixRx2);
    }

    @Override
    public void setIFShift(int ifShift) {
        if ((ifShift >= 0) && (ifShift <= 255)) {
            this.ifShift = ifShift;
            if (allowQueueing) {
                writeData(pcrCommandIFShiftPrefixRx1 + Utility.integerToHex(ifShift) + lf);
                writeData(pcrCommandIFShiftPrefixRx2 + Utility.integerToHex(ifShift) + lf);
            }
        }
    }

    @Override
    public void setSquelch(int squelch) {
        if ((squelch >= 0) && (squelch <= 255)) {
            if (allowQueueing) {
                writeData(pcrCommandSquelchPrefixRx1 + Utility.integerToHex(squelch) + lf);
                writeData(pcrCommandSquelchPrefixRx2 + Utility.integerToHex(squelch) + lf);
            }
        }
    }

    @Override
    public void setVolume(int volume) {
        if ((volume >= 0) && (volume <= 255)) {
            if (allowQueueing) {
                writeData(pcrCommandVolumeLevelPrefixRx1 + Utility.integerToHex(volume) + lf);
                writeData(pcrCommandVolumeLevelPrefixRx2 + Utility.integerToHex(0) + lf);
            }
        }
    }

    private int getDigitalSquelchOrdinal(double digitalSquelchFreq) {
        int code = 0;
        for (int i = 0; i < getDigitalSquelchValues().length; i++) {
            if (Utility.equals(digitalSquelchFreq, Double.parseDouble(getDigitalSquelchValues()[i]))) {
                code = i;
            }
        }
        return code;
    }

    @Override
    public void setDPL(int dpl) {
        this.dpl = dpl;
        final int dplCode = dpl;
        if (dplCode >= 0 && dplCode <= Integer.parseInt(getDigitalSquelchValues()[getDigitalSquelchValues().length - 1])) {
            final String digitalSquelchHexCode = Utility.integerToHex(getDigitalSquelchOrdinal(dplCode));
            if (allowQueueing) {
                writeData(pcrCommandDCSPrefixRx1 + (isDPLInverted() ? pcrCommandDCSReverse : pcrCommandDCSNormal) + digitalSquelchHexCode + lf);
                writeData(pcrCommandDCSPrefixRx2 + (isDPLInverted() ? pcrCommandDCSReverse : pcrCommandDCSNormal) + digitalSquelchHexCode + lf);
            }
        }
    }

    @Override
    public void setDPLInverted(boolean dplInverted) {
        this.dplInverted = dplInverted;
        setDPL(dpl);
    }

    @Override
    public void setPL(int pl) {
        this.pl = pl;
        final double plFreq = pl / 10D;
        if (plFreq >= 0 && plFreq <= Double.parseDouble(getToneSquelchValues()[getToneSquelchValues().length - 1])) {
            final String toneSquelchHexCode = Utility.integerToHex(getToneSquelchOrdinal(plFreq));
            if (allowQueueing) {
                writeData(pcrCommandCTCSSPrefixRx1 + toneSquelchHexCode + lf);
                writeData(pcrCommandCTCSSPrefixRx2 + toneSquelchHexCode + lf);
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
            final int m = getModeOrdinal(mode);
            final int f = getFilterHzOrdinal(filter);
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
        getReceiverEvent().firePropertyChange(ReceiverEvent.RX_DATA, null, data);
        switch (data.substring(0, 2)) {
            case pcrReplyHeaderAcknowledge -> {
                switch (data.substring(2, 4)) {
                    case "01" -> getReceiverEvent().firePropertyChange(ReceiverEvent.RECEIVED_DATA_ERROR, null, "Received Data Error");
                    case "00" -> {
                        getReceiverEvent().firePropertyChange(ReceiverEvent.ACK, null, "Data Acknowledged");
                        if (lastCommandSent.contains(pcrCommandFrequencyPrefix)) {
                            setFreqChangeConfirmed(true);
                            getReceiverEvent().firePropertyChange(ReceiverEvent.CURRENT_FREQ, null, getFrequency());
                        }
                }
                    default -> getReceiverEvent().firePropertyChange(ReceiverEvent.RECEIVED_DATA_ERROR, null, "Received Data Error");
                }
            }

            case pcrReplyHeaderDSPStatus -> getReceiverEvent().firePropertyChange(ReceiverEvent.DSP, null, "01".equals(data.substring(2, 4)));

            case pcrReplyHeaderReceiveStatus -> {
                busy = decodeBusyStatus(data.substring(2, 4));
                getReceiverEvent().firePropertyChange(ReceiverEvent.BUSY, null, busy);
                busyTimer.restart();
            }

            case pcrReplyHeaderRSSIChange -> {
                if (data.length() == 4) {
                    rssi = Integer.valueOf(data.substring(2, 4), 16);
                    if (isRssiEnabled() && !isScanning()) {
                        getReceiverEvent().firePropertyChange(ReceiverEvent.RSSI, null, rssi);
                    }
                    rssiTimer.restart();
                }
            }

            case pcrReplyHeaderSignalOffset -> {
            	final String signalOffset = data.substring(2, 4);
                getReceiverEvent().firePropertyChange(ReceiverEvent.SIGNAL_OFFSET, null, signalOffset);
            }

            case pcrReplyHeaderDTMFDecode -> {
            	final String dtmfDecode = data.substring(2, 4);
                getReceiverEvent().firePropertyChange(ReceiverEvent.DTMF_DECODE, null, dtmfDecode);
            }

            case pcrReplyHeaderWaveFormData -> {
            	final String waveFormData = data.substring(2, 4);
                getReceiverEvent().firePropertyChange(ReceiverEvent.WAVEFORM_DATA, null, waveFormData);
            }

            case pcrReplyHeaderScanStatus -> {
            	final String scanStatus = data.substring(2, 4);
                getReceiverEvent().firePropertyChange(AbstractScanner.SCAN_STATUS, null, scanStatus);
            }

            case pcrReplyHeaderFirmware -> {
            	final String firmware = data.substring(2, 4);
                getReceiverEvent().firePropertyChange(ReceiverEvent.FIRMWARE, null, firmware);
            }

            case pcrReplyHeaderCountry -> {
            	final String country = data.substring(2, 4);
                getReceiverEvent().firePropertyChange(ReceiverEvent.COUNTRY, null, country);
            }

            case pcrReplyHeaderPower -> {
                synchronized (onLineHold) {
                	final boolean power = decodePowerStatus(data.substring(2, 4));
                    getReceiverEvent().firePropertyChange(ReceiverEvent.POWER_STATE_CHANGE, null, power);
                    if (power) {
                        isConnected = true;
                        onLineHold.notifyAll();
                    }
                }
            }

            case pcrReplyHeaderProtocol -> {
            	final String protocol = data.substring(2, 4);
                getReceiverEvent().firePropertyChange(ReceiverEvent.PROTOCOL, null, protocol);
            }

            default -> LOG.log(Level.INFO, "PCR2500 - Invalid Property Value : {0}", data);
        }
    }

    private boolean decodePowerStatus(String str) {
        boolean b = false;
        if ("01".equals(str)) {
            b = true;
        }
        return b;
    }

    private boolean decodeBusyStatus(String str) {
        boolean b = true;
        if ("04".equals(str)) {
            b = false;
        }
        return b;
    }

    @Override
    public void startRadio() {
        isConnected = false;
        itemsToWrite.clear();
        allowQueueing = true;

        tty.openPort();

        if (allowQueueing) {
            writeData(pcrCommandRxOn + lf);
        }

        executor.execute(new RequestReadyStatus());
    }

    private class RequestReadyStatus implements Runnable {

        @Override
        public void run() {
            writeData(pcrQueryRxOn + lf);
            synchronized (onLineHold) {
                try {
                    while (!isConnected) {
                        onLineHold.wait(1000);
                    }
                } catch (InterruptedException ex) {
                    LOG.log(Level.WARNING, ex.getMessage());
                    Thread.currentThread().interrupt();
                }
            }
            if (isConnected) {
                if (allowQueueing) {
                    writeData(pcrCommandSetBaudRatePrefix + "05" + lf);
                }
                tty.setBaudRate(BaudRate.BAUDRATE_38400);

                if (allowQueueing) {
                    writeData(pcrQueryFirmwareVersion + lf);
                }
                if (allowQueueing) {
                    writeData(pcrQueryCountry + lf);
                }
                if (allowQueueing) {
                    writeData(pcrQueryDSP + lf);
                }

                if (allowQueueing) {
                    writeData(pcrCommandClearAllSettingsRx1 + lf);
                }
                if (allowQueueing) {
                    writeData(pcrCommandClearAllSettingsRx2 + lf);
                }

                if (allowQueueing) {
                    writeData(pcrCommandNonDiversity + lf);
                }

                setVolume(volume);
                setSquelch(squelch);

                cmdSwitch(voiceScan, pcrCommandVoiceSquelchPrefixRx1);
                cmdSwitch(voiceScan, pcrCommandVoiceSquelchPrefixRx2);

                cmdSwitch(fastSquelch, pcrCommandSquelchDelayPrefixRx1);

                cmdSwitch(agc, pcrCommandAGCPrefixRx1);
                cmdSwitch(agc, pcrCommandAGCPrefixRx2);

                cmdSwitch(isNoiseBlanker(), pcrCommandNBPrefixRx1);
                cmdSwitch(isNoiseBlanker(), pcrCommandNBPrefixRx2);

                cmdSwitch(getAttenuator() > 0, pcrCommandATTPrefixRx1);
                cmdSwitch(getAttenuator() > 0, pcrCommandATTPrefixRx2);

                if (allowQueueing) {
                    writeData("JB000" + lf);
                }
                if (allowQueueing) {
                    writeData("JC400" + lf);
                }
                if (allowQueueing) {
                    writeData("JC000" + lf);
                }

                setFrequencyModeFilter(getFrequency(), getModeName(), getFilterHz());

                setIFShift(ifShift);

                setSquelchMode(getSquelchMode());

                if (allowQueueing) {
                    writeData(pcrCommandAudioEnabled + lf);
                }

                if (allowQueueing) {
                    writeData("J8001" + lf);
                }
                if (allowQueueing) {
                    writeData("J8100" + lf);
                }
                if (allowQueueing) {
                    writeData("J8200" + lf);
                }
                if (allowQueueing) {
                    writeData("J8300" + lf);
                }
                if (allowQueueing) {
                    writeData("JC500" + lf);
                }
                if (allowQueueing) {
                    writeData("J8200" + lf);
                }

                updateTimerScheduler = Executors.newSingleThreadScheduledExecutor();
                updateTimerScheduler.scheduleWithFixedDelay(updateRunnable, UPDATE_PERIOD, UPDATE_PERIOD, TimeUnit.MILLISECONDS);
            }
        }
    }

    private class InitiateRadioStop implements Runnable {

        @Override
        public void run() {
            try {
                stopSinad();
                if (updateTimerScheduler != null) {
                    updateTimerScheduler.shutdownNow();
                }
                itemsToWrite.clear();
                if (allowQueueing) {
                    writeData(pcrCommandAutoUpdateOff + lf);
                }
                if (allowQueueing) {
                    writeData(pcrCommandRxOff + lf);
                }
                Thread.sleep(80);
                if (allowQueueing) {
                    writeData(pcrQueryRxOn + lf);
                }
                allowQueueing = false;
                rssiTimer.stop();
                busyTimer.stop();
                clearRSSI();
                clearBusy();
                tty.closePort();
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
                LOG.log(Level.WARNING, ex.getMessage());
            }
        }
    }

    private void sendFrequencyToPcr(double freq, int mode, int filter) {
        setFreqChangeConfirmed(false);
        final DecimalFormat freqFormat = new DecimalFormat(pcrFrFmt);
        String strFr = freqFormat.format(freq * pcrdbl1e6);
        final DecimalFormat pcrFormat = new DecimalFormat(pcrFmt);
        strFr = strFr + pcrFormat.format(mode) + pcrFormat.format(filter);
        strFr += pcrFmt;
        if (allowQueueing) {
            writeData(pcrCommandFrequencyPrefixRx1 + strFr + lf);
            writeData(pcrCommandFrequencyPrefixRx2 + strFr + lf);
        }
    }

    private void cmdSwitch(boolean bln, String cmd) {
        String str = pcrBoolOff;
        if (bln) {
            str = pcrBoolOn;
        }
        if (allowQueueing) {
            writeData(cmd + str + lf);
        }
    }

    private void updateRequest() {
        if (allowQueueing) {
            writeData(pcrQueryRSSIChangeRx1 + lf);
            writeData(pcrQueryReceiveStatusRx1 + lf);
            writeData(pcrQuerySignalOffsetRx1 + lf);
            writeData(pcrQueryDTMFDecodeRx1 + lf);
        }
    }

    // values = 0 -> 1
    @Override
    public double getBER() {
        return 1;
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

    private void writeData(String data) {
        if (shuttingDown || terminated) {
            return;
        }
        try {
            itemsToWrite.put(data);
        } catch (final InterruptedException _) {
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
                        LOG.log(Level.INFO, "write to TTY port: {0} -> {1}", new Object[]{sequence, data.replace("\n", "").replace("\r", "")});
                        lastCommandSent = data;
                        sequence++;
                        tty.write(data);
                        Thread.sleep(WRITE_PAUSE);
                    } else {
                        Thread.sleep(TERMINATE_WAIT);
                    }
                }
            } catch (final InterruptedException ex) {
                LOG.log(Level.WARNING, ex.getMessage());
                Thread.currentThread().interrupt();
            } finally {
                terminated = true;
                updateTimerScheduler.shutdownNow();
                itemsToWrite.clear();
            }
        }
    }

    @Override
    public void stopRadio() {
        executor.execute(new InitiateRadioStop());
    }

    private void processData(final String data) {
    	final StringBuilder sb = new StringBuilder();
        
        sb.append("ItemsToWrite Remaining: ");
        sb.append(itemsToWrite.size());
        sb.append(" last command: ");
        sb.append(lastCommandSent.replace("\n", "").replace("\r", ""));
        sb.append(" sequence: ");
        sb.append(sequence);
        sb.append(" reply: ");
        sb.append(data);
        sb.append(lf);
        sb.append("---------");
        sb.append(lf);

        LOG.log(Level.INFO, "Process Data -> {0}", sb);

        sequence++;

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
    public void setFrequency(String frequency) {
        try {
            setFrequency(Double.parseDouble(frequency));
        } catch (NumberFormatException ex) {
            setFrequency(0.0);
            LOG.log(Level.WARNING, ex.getMessage());
            getReceiverEvent().firePropertyChange(ReceiverEvent.EXCEPTION, null, ex);
        }
    }

    @Override
    public void setFrequency(double frequency) {
        this.frequency = frequency;
        setFrequencyModeFilter(frequency, getModeName(), getFilterHz());
    }

    @Override
    public void setSquelchMode(AccessMode accessMode) {
        this.accessMode = accessMode;
        switch (accessMode) {
            case CSQ -> {
                setPL(0);
                setDPL(0);
            }
            case DPL -> setDPL(getDPL());
            case PL -> setPL(getPL());
            default -> throw new IllegalArgumentException("Unexpected value: " + accessMode);
        }
    }

    @Override
    public boolean isConnected() {
        return tty.isPortOpen();
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
