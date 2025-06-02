package radio;

import java.beans.PropertyChangeSupport;

import java.io.File;
import java.io.IOException;

import java.lang.reflect.InvocationTargetException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.JPanel;

import hamlib.RigCodes;
import hamlib.Rigctl;

import signaltrack.SignalTrack;

// An abstract class that stores all current radio settings and handles all common functions 

public abstract class AbstractRadioReceiver implements ReceiverInterface, AutoCloseable {
	
	public enum DataString {
		SOURCE,
		MANUFACTURER,
		MODEL,
		SN,
		ADJACENT_CHANNEL_REJECTION,
		SIGNAL_REQ_FOR_12DB_SINAD,
		SIGNAL_REQ_FOR_20DB_QUIETING,
		SIGNAL_REQ_FOR_5PCT_BER,
		NOISE_FLOOR,
		SATURATION
	}
	
	public enum StandardModeName {
		IQ,
        WFM, 
        FM, 
        NFM, 
        AM, 
        USB, 
        LSB, 
        CW, 
        CWR, 
        DSTAR, 
        DMR, 
        TRBO,
        P25_PHASE_1,
        P25_PHASE_2,
        RTTY, 
        RTTYR, 
        PKTFM, 
        PKTUSB, 
        PKTLSB, 
        FAX,
        EDACS,
        EDACS_IP,
        EDACS_EA,
        LTE_V3,
        LTE_V4,
        LTE_V5,
        OPEN_SKY,
        NXDN,
        NEXEDGE,
        IDAS,
        SMART_ZONE,
        SMART_NET,
        WIMAX
    }

    public enum AccessMode {
        CSQ, 
        PL, 
        DPL, 
        NAC, 
        CC,
        MYCALL,
        TALK_GROUP,
        IMEI,
        RAN,
        UID,
        SYSID,
        NETID,
        WACN,
        TIME_SLOT,
        ASN,
        CSN,
        ICCID
    }

    public enum ProviderCatalog {
    	SIGNALTRACK,
    	HAMLIB
    }
    
    public enum EncryptionProtocol {
    	CLEAR,
    	AES_256,
    	DES,
    	DES_XL,
    	DES_OFB,
    	DVP,
    	SECURE_NET,
    	ADP,
    	TYPE_1,
    	ADS_B
    }
    
    private static final String[] TONE_SQUELCH_VALUES = { "67.0", "69.3", "71.0", "71.9", "74.4", "77.0", "79.7",
			"82.5", "85.4", "88.5", "91.5", "94.8", "97.4", "100.0", "103.5", "107.2", "110.9", "114.8", "118.8",
			"123.0", "127.3", "131.8", "136.5", "141.3", "146.2", "151.4", "156.7", "159.8", "162.2", "165.5", "167.9",
			"173.8", "177.3", "179.9", "183.5", "186.2", "189.9", "192.8", "196.6", "199.5", "203.5", "206.5", "210.7",
			"218.1", "225.7", "229.1", "233.6", "241.8", "250.3", "254.1" };

	private static final String[] DIGITAL_SQUELCH_VALUES = { "000", "023", "025", "026", "031", "032", "036", "043",
			"047", "051", "053", "054", "065", "071", "072", "073", "074", "114", "115", "116", "122", "125", "131",
			"132", "134", "143", "145", "152", "155", "156", "162", "165", "172", "174", "205", "212", "223", "225",
			"226", "243", "244", "245", "246", "251", "252", "255", "261", "263", "265", "266", "271", "274", "306",
			"311", "315", "325", "331", "332", "343", "346", "351", "356", "364", "365", "371", "411", "412", "413",
			"423", "431", "432", "445", "446", "452", "454", "455", "462", "464", "465", "466", "503", "506", "516",
			"523", "526", "532", "546", "565", "606", "612", "624", "627", "631", "632", "654", "662", "664", "703",
			"712", "723", "731", "732", "734", "743", "754" };
	
	private static final String[] DIGITAL_COLOR_CODE_VALUES = { "0/0","1/0","2/0","3/0","4/0","5/0","6/0","7/0","8/0",
			"9/0","10/0","11/0","12/0", "13/0","14/0","15/0","0/1","1/1","2/1","3/1","4/1","5/1","6/1","7/1","8/1","9/1",
			"10/1","11/1","12/1","13/1","14/1","15/1" };
	
	public static final String DEFAULT_MOTOROLA_SERIAL_NUMBER = "123ABC1234";
	
	private static String className;
	
	public static final String RX_DATA = "RX_DATA";
	public static final String TX_DATA = "TX_DATA";
	public static final String CONFIG_ERROR = "CONFIG_ERROR";
	public static final String ERROR = "ERROR";
	public static final String INVALID_ADDRESS = "INVALID_ADDRESS";
	public static final String ONLINE = "ONLINE";
	public static final String CLASS_NAME_CHANGE = "CLASS_NAME_CHANGE";
	
    private static final double DEFAULT_NOISE_FLOOR = -135;
	private static final double DEFAULT_SATURATION = -21;
	private static final double ADJACENT_CHANNEL_REJECTION_DB = 60.0;
	private static final double SIGNAL_REQ_FOR_12DB_SINAD = -115.0; 
	private static final double SIGNAL_REQ_FOR_20DB_QUIETING = -117.0; 
	private static final double SIGNAL_REQ_FOR_5PCT_BER = -118;
	
	private static final Logger LOG = Logger.getLogger(AbstractRadioReceiver.class.getName());
	
	// class name must contain manufacturer and model number
	protected static final String[] RADIO_CATALOG = { 
		"radio.Icom_PCR1000", 
		"radio.Icom_PCR2500",
		"radio.Yaesu_FT857D",
		"radio.RafaelMicro_R820T"
	};
	
	private static final long DEFAULT_TIMEOUT_PERIOD = 500;

	private boolean enableEvents = true;
	
	private CalibrationDataObject cdo;
	private Sinad sinad;

	private File calFile;

	private final Preferences userPrefs = Preferences.userRoot().node(this.getClass().getName());

	private final Object scanHold = new Object();

	private final ScanEvent scanEvent = new ScanEvent(this);

	private List<Double> berList;
	private List<Double> sinadList;
	private List<Double> dBmList;
	private List<ScanElement> scanList;

	private Timer timeoutTimer;
	private final ExecutorService scanExecutor = Executors.newSingleThreadExecutor();

	protected final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	
	private Measurement measurement;
	private int currentChannel;
	private int scanListSize = SignalTrack.DEFAULT_SCAN_CHANNEL_LIST_SIZE;
	private long sequence;
	private volatile boolean isScanning;

	private volatile long timeoutPeriod = DEFAULT_TIMEOUT_PERIOD;
	private volatile long requestTime;
	private volatile long responseTime;
	private volatile long dwellTime = -1;

	protected ReceiverEvent receiverEvent = new ReceiverEvent(this);
	protected TransmitterEvent transmitterEvent = new TransmitterEvent(this);
	
	protected StandardModeName modeName;
	protected AccessMode accessMode;
	protected volatile double frequency;
	protected volatile int volume;
	protected volatile int squelch;
	protected volatile double dBm;
	protected volatile int rssi;
	protected volatile double ber;
	protected volatile int pl;
	protected volatile int dpl;
	protected volatile boolean dplInverted;
	protected volatile int colorCode;
	protected volatile int timeSlot;
	protected String networkAccessCode;
	protected volatile boolean noiseBlanker;
	protected volatile double attenuator;
	protected volatile boolean agc;
	protected volatile boolean afc;
	protected volatile int filterHz;
	protected volatile boolean busy;
	protected volatile int ifShift;
	protected boolean berEnabled;
	protected boolean rssiEnabled;
	protected boolean sinadEnabled;
	protected boolean startRadioWithSystem;
	protected boolean freqChangeConfirmed;
	protected boolean scanEnabled;
	protected boolean sampleRSSI;
	protected boolean sampleBER;
	protected boolean sampleSINAD;
	
	protected AbstractRadioReceiver() {
		configureLogger();
	}

	protected AbstractRadioReceiver(File calFile, Boolean clearAllPreferences) {
		
		if (Boolean.TRUE.equals(clearAllPreferences)) {
			clearAllPreferences();
		}
		
		configureLogger();
		this.calFile = calFile;
		cdo = new CalibrationDataObject(this);
		sinad = new Sinad();
		initializeLists();
		loadPreferences(cdo.getUniqueIdentifier());
		initializeSinadListener();
	}
	
	public abstract JPanel[] getConfigurationComponentArray();
	
	public abstract long getVersionUID();

	public abstract void stopRadio();

	public abstract boolean isConnected();

	public abstract void startRadio();

	public abstract String getInterfaceName();
	
	protected void clearAllPreferences() {
		try {
			userPrefs.clear();
		} catch (final BackingStoreException ex) {
			LOG.log(Level.WARNING, ex.getMessage());
		}
	}

    public static String createNewDefaultCalFileRecord() {
    	final AbstractRadioReceiver defaultRadio = AbstractRadioReceiver.getRadioInstance(getRadioCatalog()[0]);
    	String newFileName = null;
    	if (defaultRadio != null) {
    		newFileName = createNewCalFileRecord(defaultRadio.getManufacturer(), defaultRadio.getModel(), defaultRadio.getDefaultSerialNumber());
    	}
    	return newFileName;
    }
	
    public static String createNewCalFileRecord(String manufacturer, String model, String serialNumber) {
    	final File file = CalibrationDataObject.getStandardCalibrationFileName(getCalParentFile(), model, serialNumber);
        
    	final DefaultRadioSpecification drs = AbstractRadioReceiver.getDefaultRadioSpecification(manufacturer, model);
        
        if (drs != null) { 
	    	try (CalibrationDataObject calibrationDataObject = new CalibrationDataObject(file, drs.getSource().name(), manufacturer,
	    			model, serialNumber, drs)) {
	    		calibrationDataObject.save();
	    	}
        }
        return file.getPath();
    }
 
    public String[] getAllDevicesWithCalFiles() {
        final File[] calFiles = getCalParentFile().listFiles();
        final List<String> devices = Collections.synchronizedList(new CopyOnWriteArrayList<>());
        if (calFiles != null) {
            for (final File file : calFiles) {
            	final String model = CalibrationDataObject.getDataString(file, DataString.MODEL).toUpperCase(Locale.US);
            	final String manufacturer = CalibrationDataObject.getDataString(file, DataString.MANUFACTURER).toUpperCase(Locale.US);
                boolean contains = false;
                for (String s : devices) {
                    if (s.toUpperCase(Locale.US).contains(model) && s.toUpperCase(Locale.US).contains(manufacturer)) {
                        contains = true;
                        break;
                    }
                }
                if (!contains) {
                    devices.add(manufacturer + " " + model);
                }
            }
        }
        return devices.toArray(new String[devices.size()]);
    }
    
	public boolean isEnableEvents() {
		return enableEvents;
	}

	public void setEnableEvents(boolean enableEvents) {
		this.enableEvents = enableEvents;
	}

	private void configureLogger() {
		try {
			final Handler fh = new FileHandler("%t/AbstractRadioReceiver.log");
			final Handler ch = new ConsoleHandler();
			LOG.addHandler(fh);
			LOG.setLevel(Level.FINEST);
			LOG.addHandler(ch);
			LOG.setUseParentHandlers(false);
		} catch (SecurityException | IOException e) {
			LOG.log(Level.WARNING, e.getMessage());
		}
	}

	private void loadPreferences(String deviceID) {
		filterHz = userPrefs.getInt(deviceID + "filter", 11200);
		volume = userPrefs.getInt(deviceID + "volume", 0);
		squelch = userPrefs.getInt(deviceID + "squelch", 0);
		pl = userPrefs.getInt(deviceID + "toneSquelch", 1000);
		dpl = userPrefs.getInt(deviceID + "digitalSquelch", 131);
		dplInverted = userPrefs.getBoolean(deviceID + "DPLInverted", false);
		colorCode = userPrefs.getInt(deviceID + "ColorCode", 0);
		timeSlot = userPrefs.getInt(deviceID + "TimeSlot", 0);
		frequency = userPrefs.getDouble(deviceID + "frequency", getMinRxFreq());
		ifShift = userPrefs.getInt(deviceID + "ifShift", 127);
		agc = userPrefs.getBoolean(deviceID + "agc", false);
		afc = userPrefs.getBoolean(deviceID + "afc", false);
		berEnabled = userPrefs.getBoolean(deviceID + "berEnabled", false);
		rssiEnabled = userPrefs.getBoolean(deviceID + "rssiEnabled", true);
		sinadEnabled = userPrefs.getBoolean(deviceID + "sinadEnabled", false);
		attenuator = userPrefs.getDouble(deviceID + "attenuator", 0.0F);
		noiseBlanker = userPrefs.getBoolean(deviceID + "noiseBlanker", false);
		accessMode = AccessMode.values()[userPrefs.getInt(deviceID + "squelchMode", AccessMode.CSQ.ordinal())];
		networkAccessCode = userPrefs.get(deviceID + "networkAccessCode", "293");
		startRadioWithSystem = userPrefs.getBoolean(deviceID + "startRadioWithSystem", false);
		scanEnabled = userPrefs.getBoolean(deviceID + "ScanEnabled", false);
		modeName = StandardModeName.valueOf(userPrefs.get(deviceID + "mode", StandardModeName.NFM.name()));

		for (int i = 0; i < scanListSize; i++) {
			scanList.get(i).setFrequency(userPrefs.getDouble(deviceID + "scan_F" + i, getMinRxFreq()));
			scanList.get(i).setScanSelected(userPrefs.getBoolean(deviceID + "scanSelect_F" + i, false));
			scanList.get(i).setPLTone(userPrefs.get(deviceID + "scanPLCode_F" + i, getToneSquelchValues()[0]));
			scanList.get(i).setDPLCode(userPrefs.get(deviceID + "scanDPLCode_F" + i, getDigitalSquelchValues()[0]));
			scanList.get(i).setDPLInverted(userPrefs.getBoolean(deviceID + "scanDPLInverted_F" + i, false));
			scanList.get(i).setNetworkAccessCode(userPrefs.get(deviceID + "scanNACCode_F" + i, getDigitalNACValues()[0]));
			scanList.get(i).setSampleBER(userPrefs.getBoolean(deviceID + "scanSampleBER_F" + i, false));
			scanList.get(i).setSampleRSSI(userPrefs.getBoolean(deviceID + "scanSampleRSSI_F" + i, true));
			scanList.get(i).setSampleSINAD(userPrefs.getBoolean(deviceID + "scanSampleSINAD_F" + i, false));
			scanList.get(i).setNoiseBlanker(userPrefs.getBoolean(deviceID + "scanNoiseBlanker_F" + i, false));
			scanList.get(i).setAGC(userPrefs.getBoolean(deviceID + "scanAGC_F" + i, false));
			scanList.get(i).setAFC(userPrefs.getBoolean(deviceID + "scanAFC_F" + i, false));
			scanList.get(i).setAttenuator(userPrefs.getDouble(deviceID + "scanAttenuator_F" + i, 0D));
			final StandardModeName smn = StandardModeName.values()[(userPrefs.getInt(deviceID + "scanEmission_F" + i, StandardModeName.NFM.ordinal()))];
			scanList.get(i).setModeName(smn);
			scanList.get(i).setSquelchMode(AccessMode.values()[userPrefs.getInt(deviceID + "scanSquelchType_F" + i, AccessMode.PL.ordinal())]);
		}
	}

	private void savePreferences(String deviceID) {
		try {
			userPrefs.putInt(deviceID + "filter", filterHz);
			userPrefs.put(deviceID + "mode", getModeName().name());
			userPrefs.putInt(deviceID + "volume", volume);
			userPrefs.putInt(deviceID + "squelch", squelch);
			userPrefs.putInt(deviceID + "toneSquelch", pl);
			userPrefs.putInt(deviceID + "digitalSquelch", dpl);
			userPrefs.putBoolean(deviceID + "DPLInverted", dplInverted);
			userPrefs.putInt(deviceID + "ColorCode", colorCode);
			userPrefs.putInt(deviceID + "TimeSlot", timeSlot);
			userPrefs.putDouble(deviceID + "frequency", frequency);
			userPrefs.putInt(deviceID + "ifShift", ifShift);
			userPrefs.putBoolean(deviceID + "agc", agc);
			userPrefs.putBoolean(deviceID + "afc", afc);
			userPrefs.putBoolean(deviceID + "berEnabled", berEnabled);
			userPrefs.putBoolean(deviceID + "rssiEnabled", rssiEnabled);
			userPrefs.putBoolean(deviceID + "sinadEnabled", sinadEnabled);
			userPrefs.putBoolean(deviceID + "noiseBlanker", noiseBlanker);
			userPrefs.putDouble(deviceID + "attenuator", attenuator);
			userPrefs.putInt(deviceID + "squelchMode", accessMode.ordinal());
			userPrefs.put(deviceID + "networkAccessCode", networkAccessCode);
			userPrefs.putBoolean(deviceID + "startRadioWithSystem", startRadioWithSystem);
			userPrefs.putBoolean(deviceID + "ScanEnabled", scanEnabled);

			for (int i = 0; i < scanListSize; i++) {
				userPrefs.putDouble(deviceID + "scan_F" + i, scanList.get(i).getFrequency());
				userPrefs.putBoolean(deviceID + "scanSelect_F" + i, scanList.get(i).isScanSelected());
				userPrefs.put(deviceID + "scanPLCode_F" + i, scanList.get(i).getPLTone());
				userPrefs.put(deviceID + "scanDPLCode_F" + i, scanList.get(i).getDPLCode());
				userPrefs.put(deviceID + "scanNACCode_F" + i, scanList.get(i).getNetworkAccessCode());
				userPrefs.putInt(deviceID + "scanSquelchType_F" + i, scanList.get(i).getSquelchMode().ordinal());
				userPrefs.putBoolean(deviceID + "scanSampleBER_F" + i, scanList.get(i).isSampleBER());
				userPrefs.putBoolean(deviceID + "scanSampleRSSI_F" + i, scanList.get(i).isSampleRSSI());
				userPrefs.putBoolean(deviceID + "scanSampleSINAD_F" + i, scanList.get(i).isSampleSINAD());
				userPrefs.putBoolean(deviceID + "scanNoiseBlanker_F" + i, scanList.get(i).isNoiseBlanker());
				userPrefs.putBoolean(deviceID + "scanAGC_F" + i, scanList.get(i).isAGC());
				userPrefs.putBoolean(deviceID + "scanAFC_F" + i, scanList.get(i).isAFC());
				userPrefs.putDouble(deviceID + "scanAttenuator_F" + i, scanList.get(i).getAttenuator());
				userPrefs.putInt(deviceID + "scanEmission_F" + i, scanList.get(i).getModeName().ordinal());
				userPrefs.putInt(deviceID + "scanBandwidthHz_F" + i, scanList.get(i).getBandwidthHz());
			}
		} catch (NullPointerException ex) {
			LOG.log(Level.SEVERE, "NullPointerException", ex);
		}
	}
    
	public static List<String> getAccessModeStringList(StandardModeName smn) {
        final List<String> list = new ArrayList<>();
        try {
	        switch (smn) {
	        	case IQ -> 
	        		list.add(AccessMode.CSQ.name());
	            case WFM ->
	                list.add(AccessMode.CSQ.name());
	            case FM -> {
	                list.add(AccessMode.CSQ.name());
	                list.add(AccessMode.PL.name());
	                list.add(AccessMode.DPL.name());
	            }
	            case NFM -> {
	                list.add(AccessMode.CSQ.name());
	                list.add(AccessMode.PL.name());
	                list.add(AccessMode.DPL.name());
	            }
	            case AM ->
	                list.add(AccessMode.CSQ.name());
	            case USB ->
	                list.add(AccessMode.CSQ.name());
	            case LSB ->
	                list.add(AccessMode.CSQ.name());
	            case CW ->
	                list.add(AccessMode.CSQ.name());
	            case CWR ->
	                list.add(AccessMode.CSQ.name());
	            case DSTAR ->
	                list.add(AccessMode.MYCALL.name());
	            case DMR -> {
	                list.add(AccessMode.CC.name());
	                list.add(AccessMode.TIME_SLOT.name());
	                list.add(AccessMode.TALK_GROUP.name());
	            }
	            case TRBO -> {
	                list.add(AccessMode.CC.name());
	                list.add(AccessMode.TIME_SLOT.name());
	                list.add(AccessMode.TALK_GROUP.name());
	            }
	            case P25_PHASE_1 -> {
	                list.add(AccessMode.NAC.name());
	                list.add(AccessMode.WACN.name());
	                list.add(AccessMode.SYSID.name());
	            }
	            case P25_PHASE_2 -> {
	                list.add(AccessMode.NAC.name());
	                list.add(AccessMode.WACN.name());
	                list.add(AccessMode.SYSID.name());
	            }
	            case RTTY ->
	                list.add(AccessMode.CSQ.name());
	            case RTTYR ->
	                list.add(AccessMode.CSQ.name());
	            case PKTFM -> {
	                list.add(AccessMode.CSQ.name());
	                list.add(AccessMode.PL.name());
	                list.add(AccessMode.DPL.name());
	            }
	            case PKTUSB ->
	                list.add(AccessMode.CSQ.name());
	            case PKTLSB ->
	                list.add(AccessMode.CSQ.name());
	            case FAX ->
	                list.add(AccessMode.CSQ.name());
	            case EDACS -> {
            		list.add(AccessMode.TALK_GROUP.name());
            		list.add(AccessMode.SYSID.name());
	            }
	            case EDACS_EA -> {
            		list.add(AccessMode.TALK_GROUP.name());
            		list.add(AccessMode.SYSID.name());
	            }	
	            case EDACS_IP -> {
            		list.add(AccessMode.TALK_GROUP.name());
            		list.add(AccessMode.SYSID.name());
	            }	
	            case SMART_ZONE -> {
            		list.add(AccessMode.TALK_GROUP.name());
            		list.add(AccessMode.SYSID.name());
            		list.add(AccessMode.NETID.name());
	            }	
	            case SMART_NET -> {
            		list.add(AccessMode.TALK_GROUP.name());
            		list.add(AccessMode.SYSID.name());
	            }	
	            case OPEN_SKY -> {
            		list.add(AccessMode.TALK_GROUP.name());
            		list.add(AccessMode.SYSID.name());
	            }
	            case NXDN -> {
	            	list.add(AccessMode.UID.name());
            		list.add(AccessMode.TALK_GROUP.name());
            		list.add(AccessMode.RAN.name());
            		list.add(AccessMode.SYSID.name());
	            }
	            case IDAS -> {
	            	list.add(AccessMode.UID.name());
            		list.add(AccessMode.TALK_GROUP.name());
            		list.add(AccessMode.RAN.name());
            		list.add(AccessMode.SYSID.name());
	            }
	            case NEXEDGE -> {
	            	list.add(AccessMode.UID.name());
            		list.add(AccessMode.TALK_GROUP.name());
            		list.add(AccessMode.RAN.name());
            		list.add(AccessMode.SYSID.name());
	            }
	            case LTE_V3 ->
        			list.add(AccessMode.ICCID.name());
	            case LTE_V4 ->
        			list.add(AccessMode.ICCID.name());
	            case LTE_V5 ->
        			list.add(AccessMode.ICCID.name());
	            case WIMAX -> {
	            	list.add(AccessMode.ASN.name());
            		list.add(AccessMode.CSN.name());
            		list.add(AccessMode.IMEI.name());
	            }   
	        }
        } catch (NullPointerException ex) {
        	ex.printStackTrace();
        }
        return list;
    }
	
	public boolean isScanEnabled() {
		return scanEnabled;
	}

	public void setScanEnabled(boolean scanEnabled) {
		this.scanEnabled = scanEnabled;
		if (scanEnabled) {
			startScan();
		} else {
			stopScan();
		}
	}

	@Override
	public EncryptionProtocol[] getEncryptionProtocols() {
		return new EncryptionProtocol[] { EncryptionProtocol.CLEAR };
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

	public void setIFShift(int ifShift) {
		this.ifShift = ifShift;
	}

	public void setFilterHz(int filterHz) {
		this.filterHz = filterHz;
	}

	public void setPL(int pl) {
		this.pl = pl;
	}

	public void setModeName(StandardModeName modeName) {
		this.modeName = modeName;
	}
	
	public StandardModeName getModeName() {
		return modeName;
	}
	
	public void setDPL(int dpl) {
		this.dpl = dpl;
	}

	public void setDPLInverted(boolean dplInverted) {
		this.dplInverted = dplInverted;
	}

	public void setAGC(boolean agc) {
		this.agc = agc;
	}

	public void setAFC(boolean afc) {
		this.afc = afc;
	}

	public void setVolume(int volume) {
		this.volume = volume;
	}

	public void setSquelch(int squelch) {
		this.squelch = squelch;
	}

	public void setNetworkAccessCode(String networkAccessCode) {
		this.networkAccessCode = networkAccessCode;
	}

	public void setNoiseBlanker(boolean noiseBlanker) {
		this.noiseBlanker = noiseBlanker;
	}

	public void setAttenuator(double attenuator) {
		this.attenuator = attenuator;
	}

	public void setSquelchMode(AccessMode accessMode) {
		this.accessMode = accessMode;
	}

	public void setDigitalColorCode(int colorCode) {
		this.colorCode = colorCode;
	}

	public int getDigitalColorCode() {
		return colorCode;
	}

	public void setTimeSlot(int timeSlot) {
		this.timeSlot = timeSlot;
	}

	public int getTimeSlot() {
		return timeSlot;
	}

	public boolean isStartRadioWithSystem() {
		return startRadioWithSystem;
	}

	public void setStartRadioWithSystem(boolean startRadioWithSystem) {
		this.startRadioWithSystem = startRadioWithSystem;
	}

	public void savePreferences() {
		savePreferences(cdo.getUniqueIdentifier());
	}

	public String getSource() {
		return cdo.getSource();
	}

	public String getSerialNumber() {
		return cdo.getSerialString();
	}
	
	public void saveClassName(String className) {
		pcs.firePropertyChange(CLASS_NAME_CHANGE, null, className);
	}
	
	public static String getClassNameChange() {
		return className;
	}
	
	public void startSinad() {
		if (sinad == null) {
			sinad = new Sinad();
		}
		sinad.startSinad();
	}

	public void stopSinad() {
		if (sinad != null) {
			sinad.stopSinad();
			sinad = null;
		}
	}

	public boolean isSinadEnabled() {
		return sinadEnabled;
	}

	public void setSinadEnabled(boolean sinadEnabled) {
		this.sinadEnabled = sinadEnabled;
	}

	public double getSinad() {
		if (sinad == null) {
			sinad = new Sinad();
		}
		return sinad.getSinad();
	}

	public double getdBm() {
		if (isScanning()) {
			return getdBmScanList().get(getCurrentChannel());
		} else {
			return cdo.getdBmElement(getRSSI());
		}
	}

	public final double getdBmPercent() {
		return toPercent(cdo.getNoiseFloor(), cdo.getSaturation(), getdBm());
	}

	public final double getdBmPercent(int element) {
		return toPercent(cdo.getNoiseFloor(), cdo.getSaturation(), getdBmScanListElement(element));
	}

	public final List<Double> getdBmPercentList() {
		final List<Double> list = new ArrayList<>();
		for (int i = 0; i < getdBmScanList().size(); i++) {
			list.add(i, getdBmPercent(i));
		}
		return list;
	}

	public final double getSinadPercent() {
		return toPercent(0, 20, getSinad());
	}

	public final double getSinadPercent(int element) {
		return toPercent(0, 20, getSinadScanList(element));
	}

	public final List<Double> getSinadPercentList() {
		final List<Double> list = new ArrayList<>();
		for (int i = 0; i < getSinadScanList().size(); i++) {
			list.add(i, getSinadPercent(i));
		}
		return list;
	}

	public final double getBERPercent() {
		return (1 - getBER()) * 100;
	}

	public final double getBERPercent(int element) {
		return (1 - getBerScanList(element)) * 100;
	}

	public final List<Double> getBERPercentList() {
		final List<Double> list = new ArrayList<>();
		for (int i = 0; i < getBerScanList().size(); i++) {
			list.add(i, getBERPercent(i));
		}
		return list;
	}

	public static double toPercent(double min, double max, double value) {
		return 100D * (1 - (max - value) / (max - min));
	}

	public File getCalFile() {
		return calFile;
	}

	public CalibrationDataObject getCalibrationDataObject() {
		return cdo;
	}

	public void setFrequencyMHz(String freqStr) {
		setFrequencyMHz(Double.parseDouble(freqStr));
	}

	public void setFrequencyMHz(double frequency) {
		this.frequency = frequency;
	}

	public int getRSSI() {
		return rssi;
	}

	public double getBER() {
		return ber;
	}

	public boolean isNoiseBlanker() {
		return noiseBlanker;
	}

	public boolean isBerEnabled() {
		return berEnabled;
	}

	public boolean isRssiEnabled() {
		return rssiEnabled;
	}

	public double getFrequency() {
		return frequency;
	}

	public double getVolume() {
		return volume;
	}

	public double getSquelch() {
		return squelch;
	}

	public int getPL() {
		return pl;
	}

	public int getDPL() {
		return dpl;
	}

	public boolean isDPLInverted() {
		return dplInverted;
	}

	public String getNetworkAccessCode() {
		return networkAccessCode;
	}

	public double getAttenuator() {
		return attenuator;
	}

	public boolean isAGC() {
		return agc;
	}

	public boolean isAFC() {
		return afc;
	}

	public int getFilterHz() {
		return filterHz;
	}

	public boolean isBusy() {
		return busy;
	}

	public int getIFShift() {
		return ifShift;
	}

	public AccessMode getSquelchMode() {
		return accessMode;
	}

	public void setBerEnabled(boolean berEnabled) {
		this.berEnabled = berEnabled;
	}

	public void setRssiEnabled(boolean rssiEnabled) {
		this.rssiEnabled = rssiEnabled;
	}
	
	@Override
	public boolean checkRxFreq(double freq) {
		return freq >= getMinRxFreq() && freq <= getMaxRxFreq();
	}
	
	private void initializeSinadListener() {
		if (isSupportsSINAD()) {
			sinad.addSinadListener(event -> getReceiverEvent().firePropertyChange(ReceiverEvent.SINAD_CHANGE, null,
				event.getdbSinad()));
		}
	}

	public static File toCalFileFromName(String fileName) {
		return new File(SignalTrack.DEFAULT_CAL_FILE_PATH + File.separator + fileName);
	}
	
	public static File getCalParentFile() {
		return new File(SignalTrack.DEFAULT_CAL_FILE_PATH);
	}
	
	public ReceiverEvent getReceiverEvent() {
		return receiverEvent;
	}
	
	public TransmitterEvent getTransmitterEvent() {
		return transmitterEvent;
	}

	@Override
	public String[] getDigitalNACValues() {
		final String[] s = new String[4096];
		for (int i = 0; i < 4096; i++) {
			s[i] = "0x%03X".formatted(i);
		}
		return s;
	}

	@Override
	public String[] getToneSquelchValues() {
		return TONE_SQUELCH_VALUES.clone();
	}

	@Override
	public String[] getDigitalSquelchValues() {
		return DIGITAL_SQUELCH_VALUES.clone();
	}
	
	@Override
	public String[] getDigitalColorCodeValues() {
		return DIGITAL_COLOR_CODE_VALUES.clone();
	}
	
	public static DefaultRadioSpecification getDefaultRadioSpecification(String manufacturer, String model) {
		final AbstractRadioReceiver radioInstance = getRadioInstanceFor(manufacturer, model);
		if (radioInstance != null) {
			return new DefaultRadioSpecification(ProviderCatalog.SIGNALTRACK, radioInstance.getRssiLowerLimit(),
				radioInstance.getRssiUpperLimit(), radioInstance.getDefaultNoiseFloor(), 
				radioInstance.getDefaultSaturation(), radioInstance.getAdjacentChannelRejectiondB(),
				radioInstance.getSignalReqFor12dBSINADdBm(), radioInstance.getSignalReqFor20dBQuietingdBm(),
				radioInstance.getSignalReqFor5PctBERdBm());	
		} else {
			return null;
		}
	}
	
	public static String getSource(String manufacturer, String model) {
		String rigCode = null;
		try {
			for (String radioCatalogElement : RADIO_CATALOG) {
				final AbstractRadioReceiver radioInstance = AbstractRadioReceiver.getRadioInstance(radioCatalogElement);
				if (radioInstance != null
						&& radioInstance.getManufacturer().toUpperCase(Locale.getDefault())
								.contains(manufacturer.toUpperCase(Locale.getDefault()))
						&& radioInstance.getModel().toUpperCase(Locale.getDefault()).contains(model.toUpperCase(Locale.getDefault()))) {
					return ProviderCatalog.SIGNALTRACK.name();
				}
			}
			rigCode = "HAMLIB " + RigCodes.getRigCode(manufacturer, model);
		} catch (NullPointerException ex) {
			LOG.log(Level.SEVERE, "getSource(String manufacturer, String model) returns NULL", ex);
		}
		return rigCode;
	}

	public static AbstractRadioReceiver getRadioInstanceFor(String manufacturer, String model) {
		AbstractRadioReceiver radioInstance = null;
		try {
			for (String radioCatalogElement : AbstractRadioReceiver.RADIO_CATALOG) {
				final AbstractRadioReceiver abstractRadioReceiver = getRadioInstance(radioCatalogElement);
				if (abstractRadioReceiver != null
						&& abstractRadioReceiver.getManufacturer().toUpperCase(Locale.getDefault())
								.contains(manufacturer.toUpperCase(Locale.getDefault()))
						&& abstractRadioReceiver.getModel().toUpperCase(Locale.getDefault()).contains(model.toUpperCase(Locale.getDefault()))) {
					radioInstance = abstractRadioReceiver;
					break;
				}
			}
		} catch (NullPointerException ex) {
			LOG.log(Level.SEVERE, "getSignalTrackInstanceFor(String manufacturer, String model) returns NULL", ex);
		}
		return radioInstance;
	}

	public static AbstractRadioReceiver getRadioInstance(File calFile, Boolean clearAllPreferences) {
		final String source;
		final String manufacturer;
		final String model;
		AbstractRadioReceiver radioInstance = null;

		if (calFile == null || "null".equalsIgnoreCase(calFile.getName()) || !calFile.exists()) {
			calFile = CalibrationDataObject.createDefaultCalibrationFile();
		}
		
		source = CalibrationDataObject.getDataString(calFile, DataString.SOURCE).toUpperCase(Locale.getDefault());
		manufacturer = CalibrationDataObject.getDataString(calFile, DataString.MANUFACTURER).toUpperCase(Locale.getDefault());
		model = CalibrationDataObject.getDataString(calFile, DataString.MODEL).toUpperCase(Locale.getDefault());
		
		try {
			if (source.toUpperCase(Locale.getDefault()).contains(ProviderCatalog.SIGNALTRACK.name())) {
				// If the calFile source entry is SIGNALTRACK, then we iterate through and find
				// a matching module in the catalog.
				for (String catalogEntry : AbstractRadioReceiver.getRadioCatalog()) {
					final AbstractRadioReceiver abstractRadioReceiver = getRadioInstance(catalogEntry, calFile, clearAllPreferences);
					if (abstractRadioReceiver != null && manufacturer != null
							&& abstractRadioReceiver.getManufacturer().toUpperCase(Locale.getDefault())
									.contains(manufacturer.toUpperCase(Locale.getDefault()))
							&& abstractRadioReceiver.getModel().toUpperCase(Locale.getDefault()).contains(model.toUpperCase(Locale.getDefault()))) {
						radioInstance = abstractRadioReceiver;
						break;
					}
				}
				
				// If we can't find a matching module, then there is a problem, so look for
				// something that will work in
				// the Hamlib library and use it instead.
				if (radioInstance == null) {
					final int rigCode = RigCodes.getRigCode(manufacturer, model);
					radioInstance = new Rigctl(rigCode, calFile, true);
				}
				
			} else if (source.toUpperCase(Locale.getDefault()).contains(ProviderCatalog.HAMLIB.name())) {
				final String[] sourceString = source.split(" ");
				final int rigCode;
				if (sourceString.length > 1) {
					rigCode = Integer.parseInt(sourceString[1]);
				} else {
					rigCode = RigCodes.getRigCode(manufacturer, model);
				}
				radioInstance = new Rigctl(rigCode, calFile, true);
			}
			
		} catch (NullPointerException ex) {
			LOG.log(Level.SEVERE, "Null Pointer Exception", ex);
		}
		
		return radioInstance;
	}

	public static AbstractRadioReceiver getRadioInstance(String className) {
		final Class<?> classTemp;
		AbstractRadioReceiver radioInstance = null;
		try {
			AbstractRadioReceiver.className = className;
			classTemp = Class.forName(className);
			radioInstance = (AbstractRadioReceiver) classTemp.getDeclaredConstructor().newInstance();
		} catch (InstantiationException e) {
			LOG.log(Level.WARNING, "InstantiationException", e);
		} catch (IllegalAccessException e) {
			LOG.log(Level.WARNING, "IllegalAccessException", e);
		} catch (IllegalArgumentException e) {
			LOG.log(Level.WARNING, "IllegalArgumentException", e);
		} catch (InvocationTargetException e) {
			LOG.log(Level.WARNING, "InvocationTargetException", e);
		} catch (NoSuchMethodException e) {
			LOG.log(Level.WARNING, "NoSuchMethodException", e);
		} catch (SecurityException e) {
			LOG.log(Level.WARNING, "SecurityException", e);
		} catch (ClassNotFoundException e) {
			LOG.log(Level.WARNING, "ClassNotFoundException", e);
		}
		return radioInstance;
	}

	public static AbstractRadioReceiver getRadioInstance(String className, File calFile, Boolean clearAllPreferences) {
		final Class<?> classTemp;
		AbstractRadioReceiver radioInstance = null;
		try {
			AbstractRadioReceiver.className = className;
			classTemp = Class.forName(className);
			final Class<?>[] cArg = new Class<?>[2];
			cArg[0] = File.class;
			cArg[1] = Boolean.class;
			radioInstance = (AbstractRadioReceiver) classTemp.getDeclaredConstructor(cArg).newInstance(calFile, clearAllPreferences);
		} catch (InstantiationException e) {
			LOG.log(Level.WARNING, "InstantiationException", e);
		} catch (IllegalAccessException e) {
			LOG.log(Level.WARNING, "IllegalAccessException", e);
		} catch (IllegalArgumentException e) {
			LOG.log(Level.WARNING, "IllegalArgumentException", e);
		} catch (InvocationTargetException e) {
			LOG.log(Level.WARNING, "InvocationTargetException", e);
		} catch (NoSuchMethodException e) {
			LOG.log(Level.WARNING, "NoSuchMethodException", e);
		} catch (SecurityException e) {
			LOG.log(Level.WARNING, "SecurityException", e);
		} catch (ClassNotFoundException e) {
			LOG.log(Level.WARNING, "ClassNotFoundException", e);
		}
		return radioInstance;
	}

	public Measurement getChannelMeasurement(ScanElement scanElement) {
		Measurement m = null;
		setPL(Integer.parseInt(scanElement.getPLTone()) / 10);
		setDPL(Integer.parseInt(scanElement.getDPLCode()));
		setDPLInverted(scanElement.isDPLInverted());
		setNetworkAccessCode(scanElement.getNetworkAccessCode());
		setDigitalColorCode(scanElement.getColorCode());
		setTimeSlot(scanElement.getTimeSlot());
		setRssiEnabled(scanElement.isSampleRSSI());
		setBerEnabled(scanElement.isSampleBER());
		setSinadEnabled(scanElement.isSampleSINAD());
		setAFC(scanElement.isAFC());
		setAGC(scanElement.isAGC());
		setNoiseBlanker(scanElement.isNoiseBlanker());
		setAttenuator(scanElement.getAttenuator());
		setSquelchMode(scanElement.getSquelchMode());
		setModeName(scanElement.getModeName());
		setFilterHz(scanElement.getBandwidthHz());
		setFrequencyMHz(scanElement.getFrequency());
			
		m = Measurement.copy(scanElement);
		
		synchronized (scanHold) {
			try {
				while (!isFreqChangeConfirmed()) {
					scanHold.wait(500);
				}
			} catch (InterruptedException ex) {
				LOG.log(Level.WARNING, "InterruptedException", ex);
				Thread.currentThread().interrupt();
			}
		}
		if (m != null) {
			m.setdBm(getdBm());
			m.setBer(getBER());
			m.setSinad(getSinad());
		}
		return m;
	}

	public boolean isFreqChangeConfirmed() {
		return freqChangeConfirmed;
	}

	public void setFreqChangeConfirmed(boolean freqChangeConfirmed) {
		this.freqChangeConfirmed = freqChangeConfirmed;
	}

	public int getScanListSize() {
		return scanList.size();
	}

	private void initializeLists() {
		scanList = Collections.synchronizedList(new CopyOnWriteArrayList<>());
		berList = Collections.synchronizedList(new CopyOnWriteArrayList<>());
		sinadList = Collections.synchronizedList(new CopyOnWriteArrayList<>());
		dBmList = Collections.synchronizedList(new CopyOnWriteArrayList<>());

		for (int i = 0; i < scanListSize; i++) {
			scanList.add(i, new ScanElement());
			berList.add(1D);
			sinadList.add(sinad.getSINADFloor());
			dBmList.add(cdo.getNoiseFloor());
		}
	}

	public void setScanListSize(int scanListSize) {
		this.scanListSize = scanListSize;
	}

	public ScanEvent getScanEvent() {
		return scanEvent;
	}

	public int getCurrentChannel() {
		return currentChannel;
	}

	public long getTimeoutPeriod() {
		return timeoutPeriod;
	}

	public void setTimeoutPeriod(long timeoutPeriod) {
		this.timeoutPeriod = timeoutPeriod;
	}

	public Measurement getMeasurement() {
		return measurement;
	}
	
	public void setScanList(List<ScanElement> scanList) {
		final boolean isScanningNow = isScanning;
		stopScanNow();
		this.scanList = Collections.unmodifiableList(scanList);
		scanEvent.firePropertyChange(ScanEvent.SCAN_LIST_MODIFIED, null, scanList);
		if (isScanningNow) {
			startScan();
		}
	}

	public List<ScanElement> getScanList() {
		return Collections.unmodifiableList(scanList);
	}

	public List<Double> getBerScanList() {
		return Collections.unmodifiableList(berList);
	}

	public List<Double> getSinadScanList() {
		return Collections.unmodifiableList(sinadList);
	}

	public List<Double> getdBmScanList() {
		return Collections.unmodifiableList(dBmList);
	}

	public double getBerScanList(int element) {
		return berList.get(element);
	}

	public double getdBmScanListElement(int element) {
		return dBmList.get(element);
	}

	public double getSinadScanList(int element) {
		return sinadList.get(element);
	}

	public ScanElement getScanElement(int channel) {
		return scanList.get(channel);
	}

	public long getDwellTime() {
		return dwellTime;
	}

	public boolean isScanning() {
		return isScanning;
	}

	public void startScan() {
		startScan(scanList);
	}

	public static String[] getRadioCatalog() {
		return RADIO_CATALOG.clone();
	}

	public static List<String> getProviderCatalog() {
		final List<String> list = new ArrayList<>(2);
		list.add(ProviderCatalog.SIGNALTRACK.name());
		list.add(ProviderCatalog.HAMLIB.name());
		return list;
	}
	
	public void startScan(List<ScanElement> scanList) {
		if (scanList == null || isScanning) {
			return;
		}
		this.scanList = Collections.unmodifiableList(scanList);
		int numberOfSelectedChannels = 0;
		for (ScanElement aScanList : scanList) {
			if (aScanList.isScanSelected()) {
				numberOfSelectedChannels++;
			}
		}
		if (numberOfSelectedChannels >= 2) {
			currentChannel = 0;
			isScanning = true;
			scanEvent.firePropertyChange(ScanEvent.SCAN_ENABLE_CHANGE, null, isScanning);
			scanExecutor.execute(new Scan());
		} else {
			isScanning = false;
			scanEvent.firePropertyChange(ScanEvent.SCAN_ENABLE_CHANGE, null, isScanning);
		}
	}

	public void stopScan() {
		isScanning = false;
		cancelTimeoutTimer();
		currentChannel = 0;
		scanEvent.firePropertyChange(ScanEvent.SCAN_ENABLE_CHANGE, null, isScanning);
	}

	public void stopScanNow() {
		scanExecutor.shutdownNow();
		stopScan();
	}

	private class Scan implements Runnable {

		@Override
		public void run() {
			while (isScanning) {
				if (currentChannel > scanList.size() - 1) {
					currentChannel = 0;
				}
				if (scanList.get(currentChannel) != null && scanList.get(currentChannel).isScanSelected()) {
					requestTime = System.currentTimeMillis();
					sequence++;
					scanList.get(currentChannel).setMeasurementSetID(sequence);
					scanList.get(currentChannel).setChannelNumber(currentChannel);
					startTimeoutTimer();
					measurement = getChannelMeasurement(scanList.get(currentChannel));
					cancelTimeoutTimer();
					berList.set(currentChannel, measurement.getBer());
					dBmList.set(currentChannel, measurement.getdBm());
					sinadList.set(currentChannel, measurement.getSinad());
					responseTime = System.currentTimeMillis();
					dwellTime = responseTime - requestTime;
					scanEvent.firePropertyChange(ScanEvent.SCAN_MEASUREMENT_READY, null, measurement);
					scanEvent.firePropertyChange(ScanEvent.SCAN_DWELL_TIME_READY, null, dwellTime);
				}
				if (currentChannel == scanList.size() - 1) {
					scanEvent.firePropertyChange(ScanEvent.SCAN_MEASUREMENT_LIST_READY, null, scanList);
				}
				currentChannel++;
			}
		}

		private void startTimeoutTimer() {
			if (timeoutTimer != null) {
				timeoutTimer.cancel();
			}
			timeoutTimer = new Timer();
			timeoutTimer.schedule(new Timeout(), timeoutPeriod);
		}
	}

	private void cancelTimeoutTimer() {
		if (timeoutTimer != null) {
			timeoutTimer.cancel();
		}
	}

	private class Timeout extends TimerTask {
		@Override
		public void run() {
			scanExecutor.shutdownNow();
			currentChannel++;
			scanExecutor.execute(new Scan());
			LOG.log(Level.INFO, "Scanner timed out waiting for receiver to tune to channel {0}", currentChannel - 1);
		}
	}

	@Override
	public void close() {
		stopRadio();
		for (Handler handler : LOG.getHandlers()) {
			LOG.removeHandler(handler);
	        handler.close();
		}
	}
	
	public static class DefaultRadioSpecification {
		private final ProviderCatalog source;
		private final int rssiLowerLimit;
		private final int rssiUpperLimit;
		private final double noiseFloor;
		private final double saturation;
		private final double adjacentChannelRejection;
		private final double signalRequiredFor12dBSinad;
		private final double signalRequiredFor20dBQuieting;
		private final double signalRequiredFor5PercentBER;
		
		public DefaultRadioSpecification(ProviderCatalog source, int rssiLowerLimit, int rssiUpperLimit, double noiseFloor,
				double saturation, double adjacentChannelRejection, double signalRequiredFor12dBSinad,
				double signalRequiredFor20dBQuieting, double signalRequiredFor5PercentBER) {
			this.source = source;
			this.rssiLowerLimit = rssiLowerLimit;
			this.rssiUpperLimit = rssiUpperLimit;
			this.noiseFloor = noiseFloor;
			this.saturation = saturation;
			this.adjacentChannelRejection = adjacentChannelRejection;
			this.signalRequiredFor12dBSinad = signalRequiredFor12dBSinad;
			this.signalRequiredFor20dBQuieting = signalRequiredFor20dBQuieting;
			this.signalRequiredFor5PercentBER = signalRequiredFor5PercentBER;
		}

		public ProviderCatalog getSource() {
			return source;
		}

		public int getRssiLowerLimit() {
			return rssiLowerLimit;
		}

		public int getRssiUpperLimit() {
			return rssiUpperLimit;
		}

		public double getNoiseFloor() {
			return noiseFloor;
		}

		public double getSaturation() {
			return saturation;
		}

		public double getAdjacentChannelRejection() {
			return adjacentChannelRejection;
		}

		public double getSignalRequiredFor12dBSinad() {
			return signalRequiredFor12dBSinad;
		}

		public double getSignalRequiredFor20dBQuieting() {
			return signalRequiredFor20dBQuieting;
		}

		public double getSignalRequiredFor5PercentBER() {
			return signalRequiredFor5PercentBER;
		}
	}
}
