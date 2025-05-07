package hamlib;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.logging.Logger;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import java.util.Collections;

import javax.swing.SwingUtilities;

import hamlib.Rigctl.Function;
import hamlib.Rigctl.HamlibLevel;
import radio.AbstractRadioReceiver.StandardModeName;
import tty.AbstractTeletypeController.BaudRate;
import tty.AbstractTeletypeController.DataBits;
import tty.AbstractTeletypeController.FlowControl;
import tty.AbstractTeletypeController.Parity;
import tty.AbstractTeletypeController.StopBits;

public final class RigCapabilities implements AutoCloseable {

    private static final Logger LOG = Logger.getLogger(RigCapabilities.class.getName());

    public static final String RIG_CAPS_READY = "RIG_CAPS_READY";

    public static final String DEFAULT_RIGCTLD_PATH = System.getenv("RIGCTLD_PATH");
    
    private static final Preferences userPrefs = Preferences.userRoot().node(RigCapabilities.class.getName());

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private boolean rigCapsReady;
    private boolean useDTR;
    private boolean useRTS;
    private boolean serialBaudRateFixed = true;
    private boolean serialParametersFixed = true;
    private String minRxFreq = "50";
    private String maxRxFreq = "3200";
    private String minTxFreq = "50";
    private String maxTxFreq = "3200";
    private double maxAttn = 125.0;
    private double minAttn = 0;
    private int writeDelay = 12;
    private int retries = 3;
    private int timeout = 400;
    private int postDelay = 2;
    private Parity parity = Parity.NONE;
    private StopBits stopBits = StopBits.STOPBITS_1;
    private DataBits dataBits = DataBits.DATABITS_8;
    private FlowControl flowControl = FlowControl.NONE;
    private String modelName;
    private String mfgName;
    private String rigCode;
    private boolean hasAGC;
    private boolean hasAttenuator;
    private boolean hasSINAD;
    private boolean hasBER;
    private boolean hasRSSI;
    private boolean hasPrivData;
    private boolean hasInit;
    private boolean hasCleanup;
    private boolean hasOpen;
    private boolean hasClose;
    private boolean canSetConf;
    private boolean canGetConf;
    private boolean canSetFrequency;
    private boolean canGetFrequency;
    private boolean canSetMode;
    private boolean canGetMode;
    private boolean canSetVFO;
    private boolean canGetVFO;
    private boolean canSetPTT;
    private boolean canGetPTT;
    private boolean canGetDCD;
    private boolean canSetRepeaterDuplex;
    private boolean canGetRepeaterDuplex;
    private boolean canSetRepeaterOffset;
    private boolean canGetRepeaterOffset;
    private boolean canSetSplitFreq;
    private boolean canGetSplitFreq;
    private boolean canSetSplitMode;
    private boolean canGetSplitMode;
    private boolean canSetSplitVFO;
    private boolean canGetSplitVFO;
    private boolean canSetTuningStep;
    private boolean canGetTuningStep;
    private boolean canSetRIT;
    private boolean canGetRIT;
    private boolean canSetXIT;
    private boolean canGetXIT;
    private boolean canSetCTCSS;
    private boolean canGetCTCSS;
    private boolean canSetDCS;
    private boolean canGetDCS;
    private boolean canSetCTCSSSquelch;
    private boolean canGetCTCSSSquelch;
    private boolean canSetDCSSquelch;
    private boolean canGetDCSSquelch;
    private boolean canSetPowerStat;
    private boolean canGetPowerStat;
    private boolean canReset;
    private boolean canGetAnt;
    private boolean canSetAnt;
    private boolean canSetTransceive;
    private boolean canGetTransceive;
    private boolean canSendDTMF;
    private boolean canRecvDTMF;
    private boolean canSendMorse;
    private boolean canDecodeEvents;
    private boolean canSetBank;
    private boolean canSetMem;
    private boolean canGetMem;
    private boolean canSetChannel;
    private boolean canGetChannel;
    private boolean canCtlMemVFO;
    private boolean canScan;
    private boolean canGetInfo;
    private boolean canGetPower2mW;
    private boolean canGetmW2Power;
    private boolean hasTargetableVFO;
    private boolean hasTransceive;
    private boolean canSetFunc;
    private boolean canSetLevel;
    private boolean canSetParam;
    private boolean canGetFunc;
    private boolean canGetLevel;
    private boolean canGetParam;
    private List<String> controlableLevels = new ArrayList<>();
    private List<String> readableLevels = new ArrayList<>();
    private List<String> controlableFunctions = new ArrayList<>();
    private List<String> readableFunctions = new ArrayList<>();
    private List<Integer> validBaudRates = new ArrayList<>();
    private List<String> validPLCodes = new ArrayList<>();
    private List<String> validDPLCodes = new ArrayList<>();
    private List<StandardModeName> modeNames = new ArrayList<>();
    private List<Integer> filters = new ArrayList<>();

    public RigCapabilities(int rigCode) {
        this(rigCode, false);
    }

    public RigCapabilities(int rigCode, boolean debug) {
        String s = runSysCommand(new String[]{ getRigctldPath(), "-m", String.valueOf(rigCode), "--dump-caps"} );
        decodeCaps(s, debug);
    }

    private void decodeCaps(String caps, boolean debug) {
        try {
            if (debug) {
                LOG.info(caps);
            }
            if (caps.toUpperCase(Locale.US).contains("UNKNOWN RIG NUM") || caps.toUpperCase(Locale.US).contains("INITIALIZATION ERROR")) {
                pcs.firePropertyChange(RIG_CAPS_READY, null, false);
                rigCapsReady = false;
            }
            boolean readFilters = false;
            String[] capsArray = caps.split("\n");
            for (String str : capsArray) {
                String[] s = str.split("\\s");
                if (s.length == 0) {
                    continue;
                }
                if (readFilters && !str.toUpperCase(Locale.US).contains(" KHZ:")) {
                    readFilters = false;
                }
                if (str.toUpperCase(Locale.US).contains("CAPS")) {
                	rigCode = s[4];
                }
                if (str.toUpperCase(Locale.US).contains("MODEL NAME:")) {
                    modelName = s[2];
                } else if (str.toUpperCase(Locale.US).contains("MFG NAME:")) {
                    mfgName = s[2];
                } else if (s[0].toUpperCase(Locale.US).contains("CTCSS:")) {
                    for (int i = 1; i < s.length - 2; i++) {
                        String a = s[i].replaceAll("[^\\d.]", "");
                        if (a.length() > 0) {
                            validPLCodes.add(a.replace("Hz, ", ""));
                        }
                    }
                } else if (s[0].toUpperCase(Locale.US).contains("DCS:")) {
                    for (int i = 1; i < s.length - 2; i++) {
                        String a = s[i].replaceAll("[^\\d.]", "");
                        if (a.length() > 0) {
                            validDPLCodes.add(a.replace(",", ""));
                        }
                    }
                } else if (str.toUpperCase(Locale.US).contains("MODE LIST:")) {
                    s = Arrays.copyOfRange(s, 2, s.length);
                    for (int i = 0; i < s.length - 1; i++) {
                        modeNames.add(StandardModeName.valueOf(s[i]));
                    }
                } else if (str.toUpperCase(Locale.US).contains("FILTERS:")) {
                    readFilters = true;
                } else if (str.toUpperCase(Locale.US).contains("AGC:")) {
                	hasAGC = !str.toUpperCase(Locale.US).contains("NONE");
                } else if (str.toUpperCase(Locale.US).contains("ATT:")) {
                	hasAttenuator = !str.toUpperCase(Locale.US).contains("NONE");	
                } else if (readFilters) {
                    filters.add(convertFilterTextToHz(str));
                } else if (str.toUpperCase(Locale.US).contains("HAS PRIV DATA:")) {
                    hasPrivData = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("HAS TRANSCEIVE:")) {
                    hasTransceive = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("HAS TARGETABLE VFO:")) {
                    hasTargetableVFO = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("HAS CLEANUP:")) {
                    hasCleanup = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("HAS INIT:")) {
                    hasInit = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("HAS OPEN:")) {
                    hasOpen = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("HAS CLOSE:")) {
                    hasClose = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN SET FUNC:")) {
                    canSetFunc = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN GET FUNC:")) {
                    canGetFunc = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN SET LEVEL:")) {
                    canSetLevel = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN GET LEVEL:")) {
                    canGetLevel = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN SET PARAM:")) {
                    canSetParam = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN GET PARAM:")) {
                    canGetParam = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN SET CONF:")) {
                    canSetConf = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN GET CONF:")) {
                    canGetConf = str.contains(" Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN SET FREQUENCY:")) {
                    canSetFrequency = str.contains(" Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN GET FREQUENCY:")) {
                    canGetFrequency = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN SET MODE:")) {
                    canSetMode = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN GET MODE:")) {
                    canGetMode = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN SET VFO:")) {
                    canSetVFO = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN GET VFO:")) {
                    canGetVFO = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN SET PTT:")) {
                    canSetPTT = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN GET PTT:")) {
                    canGetPTT = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN GET DCD:")) {
                    canGetDCD = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN SET REPEATER DUPLEX:")) {
                    canSetRepeaterDuplex = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN GET REPEATER DUPLEX:")) {
                    canGetRepeaterDuplex = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN SET REPEATER OFFSET:")) {
                    canSetRepeaterOffset = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN GET REPEATER OFFSET:")) {
                    canGetRepeaterOffset = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN SET SPLIT VFO:")) {
                    canSetSplitVFO = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN GET SPLIT VFO:")) {
                    canGetSplitVFO = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN SET SPLIT FREQ:")) {
                    canSetSplitFreq = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN GET SPLIT FREQ:")) {
                    canGetSplitFreq = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN SET SPLIT MODE:")) {
                    canSetSplitMode = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN GET SPLIT MODE:")) {
                    canSetSplitMode = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN SET TUNING STEP:")) {
                    canSetTuningStep = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN GET TUNING STEP:")) {
                    canGetTuningStep = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN SET RIT:")) {
                    canSetRIT = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN GET RIT:")) {
                    canGetRIT = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN SET XIT:")) {
                    canSetXIT = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN GET XIT:")) {
                    canGetXIT = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN SET CTCSS:")) {
                    canSetCTCSS = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN GET CTCSS:")) {
                    canGetCTCSS = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN SET DCS:")) {
                    canSetDCS = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN GET DCS:")) {
                    canGetDCS = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN SET CTCSS SQUELCH:")) {
                    canSetCTCSSSquelch = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN GET CTCSS SQUELCH:")) {
                    canGetCTCSSSquelch = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN SET DCS SQUELCH:")) {
                    canSetDCSSquelch = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN GET DCS SQUELCH:")) {
                    canGetDCSSquelch = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN SET POWER STAT:")) {
                    canSetPowerStat = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN GET POWER STAT:")) {
                    canGetPowerStat = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN RESET:")) {
                    canReset = s[2].contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN SET ANT:")) {
                    canSetAnt = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN GET ANT:")) {
                    canGetAnt = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN SET TRANSCEIVE:")) {
                    canSetTransceive = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN GET TRANSCEIVE:")) {
                    canGetTransceive = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN SEND DTMF:")) {
                    canSendDTMF = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN RECV DTMF:")) {
                    canRecvDTMF = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN SEND MORSE:")) {
                    canSendMorse = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN DECODE EVENTS:")) {
                    canDecodeEvents = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN SET BANK:")) {
                    canSetBank = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN SET MEM:")) {
                    canSetMem = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN GET MEM:")) {
                    canGetMem = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN SET CHANNEL:")) {
                    canSetChannel = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN GET CHANNEL:")) {
                    canGetChannel = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN CTL MEM/VFO:")) {
                    canCtlMemVFO = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN SCAN:")) {
                    canScan = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN GET INFO:")) {
                    canGetInfo = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN GET POWER2MW:")) {
                    canGetPower2mW = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("CAN GET MW2POWER:")) {
                    canGetmW2Power = str.contains("Y");
                } else if (str.toUpperCase(Locale.US).contains("POST WRITE DELAY:")) {
                    postDelay = Integer.parseInt(str.replaceAll("\\D+", ""));
                } else if (str.toUpperCase(Locale.US).contains("WRITE DELAY:")) {
                    writeDelay = Integer.parseInt(s[2].replaceAll("\\D+", ""));
                    timeout = Integer.parseInt(s[4].replaceAll("\\D+", ""));
                    retries = Integer.parseInt(s[5].replaceAll("\\D+", ""));
                } else if (str.toUpperCase(Locale.US).contains("SERIAL SPEED:")) {
                    try {
                        String[] x = s[2].split("\\.\\.");
                        int start = 0;
                        int end = 0;
                        String[] baudRates = getNames(BaudRate.class);
                        for (int i = 0; i < baudRates.length; i++) {
                            if (baudRates[i].contains(x[0].replaceAll("\\D+", ""))) {
                                start = i;
                            }
                            if (baudRates[i].contains(x[1].replaceAll("\\D+", ""))) {
                                end = i;
                            }
                        }
                        for (int i = start; i <= end; i++) {
                            validBaudRates.add(Integer.parseInt(baudRates[i].replaceAll("\\D+", "")));
                        }
                        // find the dataBits, parity and stop bits group

                        int dataBitIndex = -1;
                        if (dataBitIndex == -1) {
                        	dataBitIndex = str.lastIndexOf("5");
                        	dataBits = DataBits.DATABITS_5;
                        } else if (dataBitIndex == -1) {
                        	dataBitIndex = str.lastIndexOf("6");
                        	dataBits = DataBits.DATABITS_6;
                        } else if (dataBitIndex == -1) {
                        	dataBitIndex = str.lastIndexOf("7");
                        	dataBits = DataBits.DATABITS_7;
                        } else if (dataBitIndex == -1) {
                        	dataBitIndex = str.lastIndexOf("8");
                        	dataBits = DataBits.DATABITS_8;
                        }
                        
                        int parityIndex = -1;
                        if (parityIndex == -1) {
                        	parityIndex = str.substring(dataBitIndex).lastIndexOf("N");
                        	parity = Parity.NONE;
                        } else if (parityIndex == -1) {
                        	parityIndex = str.substring(dataBitIndex).lastIndexOf("O");
                        	parity = Parity.ODD;
                        } else if (parityIndex == -1) {
                        	parityIndex = str.substring(dataBitIndex).lastIndexOf("E");
                        	parity = Parity.EVEN;
                        } else if (parityIndex == -1) {
                        	parityIndex = str.substring(dataBitIndex).lastIndexOf("M");
                        	parity = Parity.MARK;
                        } else if (parityIndex == -1) {
                        	parityIndex = str.substring(dataBitIndex).lastIndexOf("S");
                        	parity = Parity.SPACE;
                        }
                        
                        int stopBitIndex = -1;
                        if (stopBitIndex == -1) {
                        	stopBitIndex = str.substring(dataBitIndex).lastIndexOf("1");
                        	stopBits = StopBits.STOPBITS_1;
                        } else if (stopBitIndex == -1) {
                        	stopBitIndex = str.substring(dataBitIndex).lastIndexOf("2");
                        	stopBits = StopBits.STOPBITS_2;
                        }
                        
                        int flowControlIndex = -1;
                        if (flowControlIndex == -1) {
                        	flowControlIndex = str.toUpperCase(Locale.US).lastIndexOf("NONE");
                        	flowControl = FlowControl.NONE;
                        } else if (flowControlIndex == -1) {
                        	flowControlIndex = str.toUpperCase(Locale.US).lastIndexOf("RTSCTS");
                        	flowControl = FlowControl.RTSCTS;
                        } else if (flowControlIndex == -1) {
                        	flowControlIndex = str.toUpperCase(Locale.US).lastIndexOf("XONXOFF");
                        	flowControl = FlowControl.XONXOFF;
                        }
   
                    } catch (ArrayIndexOutOfBoundsException ex) {
                        LOG.log(Level.WARNING, ex.getMessage());
                    }
                } else if (str.toUpperCase(Locale.US).contains("GET FUNCTIONS:")) {
                    readableFunctions = Arrays.asList(Arrays.copyOfRange(s, 2, s.length));
                } else if (str.toUpperCase(Locale.US).contains("SET FUNCTIONS:")) {
                    controlableFunctions = Arrays.asList(Arrays.copyOfRange(s, 2, s.length));
                } else if (str.toUpperCase(Locale.US).contains("GET LEVEL:")) {
                    readableLevels = Arrays.asList(Arrays.copyOfRange(s, 2, s.length));
                } else if (str.toUpperCase(Locale.US).contains("SET LEVEL:")) {
                    controlableLevels = Arrays.asList(Arrays.copyOfRange(s, 2, s.length));
                }
            }
            rigCapsReady = true;
        } catch (IllegalArgumentException ex) {
        	LOG.log(Level.WARNING, ex.getMessage());
        } catch (NullPointerException ex) {
        	LOG.log(Level.WARNING, ex.getMessage());
        } finally {
        	pcs.firePropertyChange(RIG_CAPS_READY, null, rigCapsReady);
        }
    }

    private static String[] getNames(Class<? extends Enum<?>> e) {
        return Arrays.toString(e.getEnumConstants()).replaceAll("^.|^.$", "").split(", ");
    }

    public String runSysCommand(String[] array) {
        String result = "";
        try {
            Process process = new ProcessBuilder(array).redirectErrorStream(true).start();
            Scanner scanner;
            try (InputStream inputStream = process.getInputStream()) {
                scanner = new Scanner(inputStream, StandardCharsets.UTF_8);
                result = scanner.useDelimiter("\\Z").next();
                process.destroy();
            }
            scanner.close();
        } catch (IOException ex) {
        	LOG.log(Level.WARNING, ex.getMessage());
        }
        return result;
    }

    public String findExeFileOnWindows(String filename) {
        File f = new File("\\");
        Pattern p = Pattern.compile("(?i).*?" + filename + "\\.exe.*");
        return recurseSearch(f, p);
    }

    public String recurseSearch(File dir, Pattern search) {
        String result = null;
        File[] listFile = dir.listFiles();

        if (listFile != null) {
            for (File f : listFile) {
                if (f.isDirectory()) {
                    result = recurseSearch(f, search);
                    if (result != null) {
                        return result;
                    }
                } else {
                    if (search.matcher(f.getName()).matches()) {
                        return f.getPath();
                    }
                }
            }
        }
        return result;
    }

    public String getRigctldPath() {
        String os = System.getProperty("os.name").toLowerCase(Locale.US);
        if (os.contains("win")) {
            String rigctldPath = userPrefs.get("rigctldPath", DEFAULT_RIGCTLD_PATH);
            File f = new File(rigctldPath);
            if (!f.exists() || f.isDirectory()) {
                rigctldPath = findExeFileOnWindows("rigctld");
                rigctldPath = "\"" + rigctldPath + "\"";
                userPrefs.put("rigctldPath", rigctldPath);
            }
            return rigctldPath;
        } else {
            return "rigctld";
        }
    }

    public String getRigCode() {
    	return rigCode;
    }
    
    public boolean canSetAgc() {
    	return hasAGC;
    }
    
    public boolean canSetAttenuator() {
    	return hasAttenuator;
    }
    
    public double getMaxAttenuator() {
		return maxAttn;
	}

	public double getMinAttenuator() {
		return minAttn;
	}

	public int getWriteDelay() {
        return writeDelay;
    }

    public int getRetries() {
        return retries;
    }

    public int getTimeout() {
        return timeout;
    }

    public int getPostDelay() {
        return postDelay;
    }

    public Parity getParity() {
        return parity;
    }

    public StopBits getStopBits() {
        return stopBits;
    }

    public DataBits getDataBits() {
        return dataBits;
    }

    public FlowControl getFlowControl() {
        return flowControl;
    }

    public String getModelName() {
        return modelName;
    }

    public String getMfgName() {
        return mfgName;
    }

    public boolean hasPrivData() {
        return hasPrivData;
    }

    public boolean hasInit() {
        return hasInit;
    }

    public boolean hasCleanup() {
        return hasCleanup;
    }

    public boolean hasOpen() {
        return hasOpen;
    }

    public boolean hasClose() {
        return hasClose;
    }

    public boolean canSetConf() {
        return canSetConf;
    }

    public boolean canGetConf() {
        return canGetConf;
    }

    public boolean canSetFrequency() {
        return canSetFrequency;
    }

    public boolean canGetFrequency() {
        return canGetFrequency;
    }

    public boolean canSetMode() {
        return canSetMode;
    }

    public boolean canGetMode() {
        return canGetMode;
    }

    public boolean canSetVFO() {
        return canSetVFO;
    }

    public boolean canGetVFO() {
        return canGetVFO;
    }

    public boolean canSetPTT() {
        return canSetPTT;
    }

    public boolean canGetPTT() {
        return canGetPTT;
    }

    public boolean canGetDCD() {
        return canGetDCD;
    }

    public boolean canSetRepeaterDuplex() {
        return canSetRepeaterDuplex;
    }

    public boolean canGetRepeaterDuplex() {
        return canGetRepeaterDuplex;
    }

    public boolean canSetRepeaterOffset() {
        return canSetRepeaterOffset;
    }

    public boolean canGetRepeaterOffset() {
        return canGetRepeaterOffset;
    }

    public boolean canSetSplitFreq() {
        return canSetSplitFreq;
    }

    public boolean canGetSplitFreq() {
        return canGetSplitFreq;
    }

    public boolean canSetSplitMode() {
        return canSetSplitMode;
    }

    public boolean canGetSplitMode() {
        return canGetSplitMode;
    }

    public boolean canSetSplitVFO() {
        return canSetSplitVFO;
    }

    public boolean canGetSplitVFO() {
        return canGetSplitVFO;
    }

    public boolean canSetTuningStep() {
        return canSetTuningStep;
    }

    public boolean canGetTuningStep() {
        return canGetTuningStep;
    }

    public boolean canSetRIT() {
        return canSetRIT;
    }

    public boolean canGetRIT() {
        return canGetRIT;
    }

    public boolean canSetXIT() {
        return canSetXIT;
    }

    public boolean canGetXIT() {
        return canGetXIT;
    }

    public boolean canSetCTCSS() {
        return canSetCTCSS;
    }

    public boolean canGetCTCSS() {
        return canGetCTCSS;
    }

    public boolean canSetDCS() {
        return canSetDCS;
    }

    public boolean canGetDCS() {
        return canGetDCS;
    }

    public boolean canSetCTCSSSquelch() {
        return canSetCTCSSSquelch;
    }

    public boolean canGetCTCSSSquelch() {
        return canGetCTCSSSquelch;
    }

    public boolean canSetDCSSquelch() {
        return canSetDCSSquelch;
    }

    public boolean canGetDCSSquelch() {
        return canGetDCSSquelch;
    }

    public boolean canSetPowerStat() {
        return canSetPowerStat;
    }

    public boolean canGetPowerStat() {
        return canGetPowerStat;
    }

    public boolean canReset() {
        return canReset;
    }

    public boolean canGetAnt() {
        return canGetAnt;
    }

    public boolean canSetAnt() {
        return canSetAnt;
    }

    public boolean canSetTransceive() {
        return canSetTransceive;
    }

    public boolean canGetTransceive() {
        return canGetTransceive;
    }

    public boolean canSendDTMF() {
        return canSendDTMF;
    }

    public boolean canRecvDTMF() {
        return canRecvDTMF;
    }

    public boolean canSendMorse() {
        return canSendMorse;
    }

    public boolean canDecodeEvents() {
        return canDecodeEvents;
    }

    public boolean canSetBank() {
        return canSetBank;
    }

    public boolean canSetMem() {
        return canSetMem;
    }

    public boolean canGetMem() {
        return canGetMem;
    }

    public boolean canSetChannel() {
        return canSetChannel;
    }

    public boolean canGetChannel() {
        return canGetChannel;
    }

    public boolean canCtlMemVFO() {
        return canCtlMemVFO;
    }

    public boolean canScan() {
        return canScan;
    }

    public boolean canGetInfo() {
        return canGetInfo;
    }

    public boolean canGetPower2mW() {
        return canGetPower2mW;
    }

    public boolean canGetmW2Power() {
        return canGetmW2Power;
    }

    public boolean hasTargetableVFO() {
        return hasTargetableVFO;
    }

    public boolean hasTransceive() {
        return hasTransceive;
    }

    public List<Integer> getValidBaudRates() {
        return Collections.unmodifiableList(validBaudRates);
    }

    public List<Integer> getValidPLToneIntegerList() {
        List<Integer> pl = new ArrayList<>();
        getValidPLCodes().forEach(s -> pl.add(Integer.parseInt(s)));
        return pl;
    }

    public List<Integer> getValidDPLCodeIntegerList() {
        List<Integer> dpl = new ArrayList<>();
        getValidDPLCodes().forEach(s -> dpl.add(Integer.parseInt(s)));
        return dpl;
    }

    public List<String> getValidPLCodes() {
        return Collections.unmodifiableList(validPLCodes);
    }

    public List<String> getValidDPLCodes() {
        return Collections.unmodifiableList(validDPLCodes);
    }

    public List<StandardModeName> getModeNameList() {
        return Collections.unmodifiableList(modeNames);
    }

    public List<Integer> getFilters() {
        return Collections.unmodifiableList(filters);
    }

    private int convertFilterTextToHz(String filter) {
        String d = filter;
        String s = filter.replaceAll("[^\\d.]", "");
        float f = Float.parseFloat(s);
        if (d.toUpperCase(Locale.US).contains("KHZ")) {
            f *= 1000;
        } else if (d.toUpperCase(Locale.US).contains("MHZ")) {
            f *= 1000000;
        }
        return Math.round(f);
    }

    public boolean canControlFunction(Function f) {
        return controlableFunctions.contains(f.name());
    }

    public boolean canReadFunction(Function f) {
        return readableFunctions.contains(f.name());
    }

    public boolean canSetLevel(HamlibLevel f) {
        return controlableLevels.stream().anyMatch(z -> (z.replaceAll("^[a-zA-Z]*$", "").contains(f.name())));
    }

    public boolean canReadLevel(HamlibLevel f) {
        return readableLevels.stream().anyMatch(z -> (z.replaceAll("^[a-zA-Z]*$", "").contains(f.name())));
    }

    public float getLevelUpperLimit(HamlibLevel f) {
        float d = 0;
        for (String z : readableLevels) {
            if (z.replaceAll("^[a-zA-Z]*$", "").contains(f.name())) {
                String x = z.replaceAll(".*\\(|\\).*", "");
                x = x.replace("0/0", "1");
                x = x.replace("/0", "");
                String[] y = x.split("\\.\\.");
                d = Float.parseFloat(y[1]);
                break;
            }
        }
        return d;
    }

    public float getLevelLowerLimit(HamlibLevel f) {
        float d = 0;
        for (String z : readableLevels) {
            if (z.replaceAll("^[a-zA-Z]*$", "").contains(f.name())) {
                String x = z.replaceAll(".*\\(|\\).*", "");
                x = x.replace("0/0", "1");
                String[] y = x.split("\\.\\.");
                d = Float.parseFloat(y[0]);
                break;
            }
        }
        return d;
    }

    public boolean canSetFunc() {
        return canSetFunc;
    }

    public boolean canSetLevel() {
        return canSetLevel;
    }

    public boolean canSetParam() {
        return canSetParam;
    }

    public boolean canGetLevel() {
        return canGetLevel;
    }

    public boolean canGetParam() {
        return canGetParam;
    }

    public boolean canGetFunc() {
        return canGetFunc;
    }

    public String getMinRxFreq() {
        return minRxFreq;
    }

    public String getMaxRxFreq() {
        return maxRxFreq;
    }

    public double getMinRxFreqMHz() {
        return Double.parseDouble(minRxFreq);
    }

    public double getMaxRxFreqMHz() {
        return Double.parseDouble(maxRxFreq);
    }

    public String getMinTxFreq() {
        return minTxFreq;
    }

    public String getMaxTxFreq() {
        return maxTxFreq;
    }

    public boolean useDTR() {
        return useDTR;
    }

    public boolean useRTS() {
        return useRTS;
    }

    public boolean serialBaudRateFixed() {
        return serialBaudRateFixed;
    }

    public boolean serialParametersFixed() {
        return serialParametersFixed;
    }
    
    public boolean hasSINAD() {
		return hasSINAD;
	}

	public void setHasSINAD(boolean hasSINAD) {
		this.hasSINAD = hasSINAD;
	}

	public boolean hasBER() {
		return hasBER;
	}

	public void setHasBER(boolean hasBER) {
		this.hasBER = hasBER;
	}

	public boolean hasRSSI() {
		return hasRSSI;
	}

	public void setHasRSSI(boolean hasRSSI) {
		this.hasRSSI = hasRSSI;
	}

    public boolean rigCapsReady() {
        return rigCapsReady;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public boolean isPropertyChangeListenerRegistered(PropertyChangeListener listener) {
        for (PropertyChangeListener pcl : pcs.getPropertyChangeListeners()) {
            if (pcl.equals(listener)) {
                return true;
            }
        }
        return false;
    }

    public PropertyChangeListener[] getPropertyChangeListeners() {
        return pcs.getPropertyChangeListeners();
    }

    public static void main(final String[] args) {
        Runnable task = () -> new RigCapabilities(404, true);
        SwingUtilities.invokeLater(task);
    }

	@Override
	public void close() throws Exception {
		for (Handler handler : LOG.getHandlers()) {
			LOG.removeHandler(handler);
	        handler.close();
		}
	}

}
