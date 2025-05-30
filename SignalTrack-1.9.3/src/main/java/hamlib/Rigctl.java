package hamlib;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.UnknownHostException;

import java.nio.charset.StandardCharsets;

import java.text.DecimalFormat;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import java.util.prefs.Preferences;

import java.util.regex.Pattern;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import java.util.logging.Logger;
import java.util.logging.Level;

import network.NetworkParameterSet;
import radio.AbstractRadioReceiver;
import radio.ReceiverEvent;
import radio.TransmitterEvent;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import tcp.TcpIpConfigurationComponent;

import tty.TeletypeInterface;
import tty.AbstractTeletypeController.BaudRate;
import tty.AbstractTeletypeController.DataBits;
import tty.AbstractTeletypeController.FlowControl;
import tty.AbstractTeletypeController.Parity;
import tty.AbstractTeletypeController.StopBits;

import utility.Utility;

public class Rigctl extends AbstractRadioReceiver implements TeletypeInterface {

    private static final long serialVersionUID = 1L;
    
    public enum Function {
        FAGC, NB, COMP, VOX, TONE, TSQL, SBKIN, FBKIN, ANF, NR, AIP, APF, MON, MN, RF, ARO, LOCK, MUTE, VSC, REV, SQL, ABM, BC, MBC, RIT, AFC, SATMODE, SCOPE, RESUME, TBURST, TUNER, XIT, IFSHIFT
    }

    public enum AgcLevel {
    	AGC0, AGC1, AGC2, AGC3, AGC4, AGC5, AGC6, AGC7, AGC8, AGC9
    }
    
    public enum HamlibLevel {
        PREAMP, ATT, VOX, AF, RF, SQL, IF, APF, NR, PBT_IN, PBT_OUT, CWPITCH, RFPOWER, MICGAIN, KEYSPD, NOTCHF, COMP, BKINDL, BAL, METER, VOXGAIN, ANTIVOX, SLOPE_LOW, SLOPE_HIGH, RAWSTR, SWR, ALC, STRENGTH
    }

    public enum DCD_Type {
        RIG, DSR, CTS, CD, PARALLEL, CM108, NONE
    }

    public enum VFO_Mode {
        VFOA, VFOB, VFOC, currVFO, VFO, MEM, Main, Sub, TX, RX
    }

    public enum Mode {
        USB, LSB, CW, CWR, RTTY, RTTYR, AM, FM, WFM, AMS, PKTLSB, PKTUSB, PKTFM, DMR, P25Phase1, P25Phase2, NEXEDGE, P25Trunking, APRS, AX25, FAX, DSB
    }

    public enum PTT {
        RX, TX, TX_MIC, TX_DATA
    }

    public enum DCD_Status {
        CLOSED, OPEN
    }

    public enum Parameter {
        ANN, APO, BACKLIGHT, BEEP, TIME, BAT, KEYLIGHT
    }

    public enum MemVFO_Operation {
        CPY, XCHG, FROM_VFO, TO_VFO, MCL, UP, DOWN, BAND_UP, BAND_DOWN, LEFT, RIGHT, TUNE, TOGGLE
    }

    public enum Scan_Function {
        STOP, MEM, SLCT, PRIO, PROG, DELTA, VFO, PLT
    }

    public enum Transceive_Mode {
        OFF, RIG, POLL
    }

    public enum Reset {
        NONE, SOFTWARE, VFO, MEMORY_CLEAR, MASTER
    }

    public enum PowerStatus {
        OFF, ON, STANDBY
    }

    public enum Set {
        FREQ, MODE, VFO, RIT, XIT, PTT, RPTR_SHIFT, RPTR_OFFS, CTCSS_TONE, CTCSS_SQL, DCS_CODE, DCS_SQL, SPLIT_FREQ, SPLIT_MODE, SPLIT_VFO, TS, BANK, MEM, CHANNEL, TRN, ANT
    }

    public enum Get {
        FREQ, MODE, VFO, RIT, XIT, PTT, DCD, RPTR_SHIFT, RPTR_OFFS, CTCSS_TONE, CTCSS_SQL, DCS_CODE, DCS_SQL, SPLIT_FREQ, SPLIT_MODE, SPLIT_VFO, TS, BANK, MEM, CHANNEL, TRN, ANT
    }

    public static final boolean DEFAULT_DEBUG = false;
    public static final String DEFAULT_RIGCTLD_PATH = System.getenv("RIGCTLD_PATH");
    public static final String DEFAULT_SERIAL_NUMBER = "123ABC1234";
    public static final String DISCONNECT = "DISCONNECT";
    public static final int DEFAULT_RIGCTLD_PORT = 4532;
    public static final long DEFAULT_FREQUENCY = 145000000;
    public static final long STANDARD_PERIODIC_TIMER_INTERVAL = 500;
    public static final long DELAYED_PERIODIC_TIMER_INTERVAL = 400;
    public static final long INTERCOMMAND_PAUSE = 150;
    public static final long TERMINATE_WAIT = 40;
    public static final long WAIT_TO_CLOSE = 200;
    
    private static final int readBufferLen = 2048;
    
    private static final Logger LOG = Logger.getLogger(Rigctl.class.getName());
    
    private final Preferences userPrefs = Preferences.userRoot().node(Rigctl.class.getName());
    
    private volatile boolean terminate;
    private volatile boolean allowQueueing;

    private Process process;
    private InputStream inputStream;
    private OutputStream outputStream;
    
    private byte[] readBuffer;
    private String receivedData;

    private ExecutorService initializeExecutor;
    private ExecutorService readExecutor;
    private ExecutorService writeExecutor;
    
    private NetworkParameterSet networkParameterSet;
    
    private String lastCommandSent;
    private long sequence;

    private int rigCode = 1;
    private boolean debug = DEFAULT_DEBUG;

    private final BlockingQueue<String> itemsToWrite = new ArrayBlockingQueue<>(128);
    private RigCapabilities rigCaps;
    private Timer periodicPropertyNameTimer;
    
    private final Object scanHold = new Object();

    public Rigctl(int rigCode, File calFile, boolean clearAllPreferences) {
        super(calFile, clearAllPreferences);
        this.rigCode = rigCode;

        rigCaps = getRigCaps();

        initializeShutdownHook();
        initializeComponents();
    }

    private RigCapabilities getRigCaps() {
        if (rigCaps == null) {
            rigCaps = new RigCapabilities(rigCode, debug);
        }
        return rigCaps;
    }
    
    private void initializeComponents() {
        readBuffer = new byte[readBufferLen];
        try {
			networkParameterSet = new NetworkParameterSet(userPrefs.get("rigctldPath", DEFAULT_RIGCTLD_PATH), userPrefs.getInt("rigctldPort", DEFAULT_RIGCTLD_PORT));
		} catch (UnknownHostException e) {
			LOG.log(Level.SEVERE, null, e);
		}
    }

    private void setAllowQueueing(boolean allowQueueing) {
        this.allowQueueing = allowQueueing;
    }

    private void setTerminate(boolean terminate) {
        this.terminate = terminate;
    }

    private boolean isTerminated() {
        return terminate;
    }

    private synchronized void writeData(String data) {
        try {
            itemsToWrite.put(data);
        } catch (InterruptedException ex) {
        	LOG.log(Level.WARNING, "InterruptedException", ex);
            Thread.currentThread().interrupt();
        } catch (NullPointerException ex) {
        	LOG.log(Level.WARNING, "NullPointerException", ex);
        }
    }

    private void initializeRigctld(int rigCode, String interfaceName) {
        try {
            if (isConnected()) {
                return;
            }
            if (interfaceName != null) {
                initializeExecutor = Executors.newSingleThreadExecutor();
                initializeExecutor.execute(new InitializeRigctl(rigCode, interfaceName, debug));
            } else {
                getReceiverEvent().firePropertyChange(ReceiverEvent.POWER_STATE_CHANGE, null, false);
            }
        } catch (RejectedExecutionException ex) {
        	LOG.log(Level.WARNING, "RejctedExecutionException", ex);
            getReceiverEvent().firePropertyChange(ReceiverEvent.POWER_STATE_CHANGE, null, false);
            getReceiverEvent().firePropertyChange(ReceiverEvent.EXCEPTION, null, ex);
        }
    }

    private final class InitializeRigctl implements Runnable {

        private int rigCode;
        private String interfaceName;
        private boolean debug;

        private InitializeRigctl(int rigCode, String interfaceName, boolean debug) {
            this.rigCode = rigCode;
            this.interfaceName = interfaceName;
            this.debug = debug;
        }

        @Override
        public void run() {
            try {
                String[] com = new String[]{
                    getRigctldPath(),
                    String.format("--set-conf=write_delay=%d", getWriteDelay()),
                    String.format("--set-conf=post_write_delay=%d", getPostDelay()),
                    String.format("--set-conf=retry=%d", getRetries()),
                    String.format("--set-conf=timeout=%d", getTimeout()),
                    "-m",
                    "" + rigCode,
                    "-r",
                    interfaceName,
                    "-vv"
                };
                boolean connected = false;
                int n = 5;
                process = new ProcessBuilder(com).redirectErrorStream(true).start();
                while (!connected && n > 0 && !isTerminated()) {
                    try {
                        inputStream = process.getInputStream();
                        outputStream = process.getOutputStream();
                        connected = process.isAlive();
                        if (debug) {
                            LOG.log(Level.INFO, "process started: {0}", connected);
                        }
                    } catch (Exception e) {
                        if (debug) {
                            LOG.info("failed to start process: " + e.getMessage());
                        }
                        try {
                            TimeUnit.MILLISECONDS.sleep(500);
                        } catch (InterruptedException ex) {
                            getReceiverEvent().firePropertyChange(ReceiverEvent.EXCEPTION, null, ex);
                            LOG.log(Level.WARNING, "InterruptedterException", ex);
                            Thread.currentThread().interrupt();
                        }
                    }
                    n--;
                    if (n == 0 || connected) {
                        break;
                    }
                }
                if (connected) {
                    getReceiverEvent().firePropertyChange(ReceiverEvent.POWER_STATE_CHANGE, null, true);
                }
                if (connected) {
                    readExecutor = Executors.newSingleThreadExecutor();
                    readExecutor.execute(new ReadThread(inputStream));
                }
                if (connected) {
                    writeExecutor = Executors.newSingleThreadExecutor();
                    writeExecutor.execute(new WriteThread());
                }
                if (connected) {
                    startPeriodicPropertyNameTimer();
                }
            } catch (IOException ex) {
                getReceiverEvent().firePropertyChange(ReceiverEvent.EXCEPTION, null, ex);
                LOG.log(Level.WARNING, "IOException", ex);
            }
        }

        private int getWriteDelay() {
            return getRigCaps().getWriteDelay();
        }

        private int getPostDelay() {
            return getRigCaps().getPostDelay();
        }

        private int getRetries() {
            return getRigCaps().getRetries();
        }

        private int getTimeout() {
            return getRigCaps().getTimeout();
        }

        private void startPeriodicPropertyNameTimer() {
            if (periodicPropertyNameTimer != null) {
                periodicPropertyNameTimer.cancel();
            }
            periodicPropertyNameTimer = new Timer();
            resetTimer();
        }
    }

    private void closeConnection() {
        setTerminate(true);
        if (process != null) {
            while (process.isAlive()) {
                process.destroy();
            }
            process = null;
        }
        if (debug) {
            LOG.info("process terminated");
        }
        if (periodicPropertyNameTimer != null) {
            periodicPropertyNameTimer.cancel();
        }
        try {
			inputStream.close();
		} catch (IOException e) {
			LOG.log(Level.WARNING, "Rigctl", e);
			e.printStackTrace();
		}
        try {
			outputStream.close();
		} catch (IOException e) {
			LOG.log(Level.WARNING, "Rigctl", e);
			e.printStackTrace();
		}
        if (readExecutor != null) {
        	try {
				LOG.log(Level.INFO, "Initializing Rigctl.readExecutor service termination....");
				readExecutor.shutdown();
				readExecutor.awaitTermination(20, TimeUnit.SECONDS);
				LOG.log(Level.INFO, "Rigctl.readExecutor service has gracefully terminated");
			} catch (InterruptedException e) {
				LOG.log(Level.SEVERE, "Rigctl.readExecutor service has timed out after 20 seconds of waiting to terminate processes.");
				Thread.currentThread().interrupt();
			}
        }
        if (writeExecutor != null) {
        	itemsToWrite.clear();
        	itemsToWrite.offer(DISCONNECT);
			try {
				LOG.log(Level.INFO, "Initializing Rigctl.writeExecutor service termination....");
				writeExecutor.shutdown();
				writeExecutor.awaitTermination(20, TimeUnit.SECONDS);
				LOG.log(Level.INFO, "Rigctl.writeExecutor service has gracefully terminated");
			} catch (InterruptedException e) {
				LOG.log(Level.SEVERE, "Rigctl.writeExecutor service has timed out after 20 seconds of waiting to terminate processes.");
				Thread.currentThread().interrupt();
			}
        }
        if (initializeExecutor != null) {
        	try {
				LOG.log(Level.INFO, "Initializing Rigctl.initializeExecutor service termination....");
				initializeExecutor.shutdown();
				initializeExecutor.awaitTermination(20, TimeUnit.SECONDS);
				LOG.log(Level.INFO, "Rigctl.initializeExecutor service has gracefully terminated");
			} catch (InterruptedException e) {
				LOG.log(Level.SEVERE, "Rigctl.initializeExecutor service has timed out after 20 seconds of waiting to terminate processes.");
				Thread.currentThread().interrupt();
			}
        }
    }

    public static String findExeFileOnWindows(String filename) {
        File f = new File("\\");
        Pattern p = Pattern.compile("(?i).*?" + filename + "\\.exe.*");
        return recurseSearch(f, p);
    }

    public static String recurseSearch(File dir, Pattern search) {
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
            String rigctldPath = networkParameterSet.getHostName();
            File f = new File(rigctldPath);
            if (!f.exists() || f.isDirectory()) {
                rigctldPath = findExeFileOnWindows("rigctld");
                rigctldPath = "\"" + rigctldPath + "\"";
            }
            return rigctldPath;
        } else {
            return "rigctld";
        }
    }
    
    public int getRigctldPort() {
    	return networkParameterSet.getPortNumber();
    }

    @Override
    public void setDPL(int dpl) {
        this.dpl = dpl;
        setOpCode(Set.DCS_SQL, dpl);
    }

    @Override
    public void setPL(int pl) {
        this.pl = pl;
        setOpCode(Set.CTCSS_SQL, pl);
    }

    public void setPL(String pl) {
        setOpCode(Set.CTCSS_SQL, pl);
    }

    @Override
    public void setVolume(int volume) {
        this.volume = volume;
        setLevel(HamlibLevel.AF, Utility.byteToPercent(volume));
    }

    @Override
    public void setSquelch(int squelch) {
        this.squelch = squelch;
        setLevel(HamlibLevel.SQL, Utility.byteToPercent(squelch));
    }

    @Override
    public void setFrequency(String freqStr) {
        try {
            setFrequency(Double.parseDouble(freqStr));
        } catch (NumberFormatException ex) {
            setFrequency(0.0);
            getReceiverEvent().firePropertyChange(ReceiverEvent.EXCEPTION, null, ex);
            LOG.log(Level.WARNING, "NumberFormatException", ex);
        }
    }

    @Override
    public void setFrequency(double frequency) {
        this.frequency = frequency;
        freqChangeConfirmed = false;
        String f = new DecimalFormat("##########").format(frequency * 1E6);
        setOpCode(Set.FREQ, f);
    }

    @Override
    public synchronized void setAGC(boolean agc) {
        this.agc = agc;
        setFunction(Function.FAGC, agc);
    }

    @Override
    public void setAFC(boolean afc) {
        this.afc = afc;
        setFunction(Function.AFC, afc);
    }

    @Override
    public void setAttenuator(double attenuator) {
        this.attenuator = attenuator;
        setLevel(HamlibLevel.ATT, attenuator);
    }

    @Override
    public void setNoiseBlanker(boolean noiseBlanker) {
        this.noiseBlanker = noiseBlanker;
        setFunction(Function.NB, noiseBlanker);
    }

    @Override
    public void setIFShift(int ifShift) {
        this.ifShift = ifShift;
        setLevel(HamlibLevel.IF, ifShift);
    }

    @Override
    public void setFilterHz(final int filterHz) {
        this.filterHz = filterHz;
        if (filterHz != -1 && modeName != null) {
            setMode(modeName, filterHz);
        }
    }

    @Override
    public void setModeName(final StandardModeName emissionDesignator) {
        this.modeName = emissionDesignator;
        if (filterHz != -1 && emissionDesignator != null) {
            setMode(emissionDesignator, filterHz);
        }
    }

    private void setMode(final StandardModeName emissionDesignator, final int filterHz) {
        this.modeName = emissionDesignator;
        this.filterHz = filterHz;
        if (filterHz != -1 && emissionDesignator != null) {
            writeData("M " + emissionDesignator.name() + " " + filterHz);
        }
    }

    @Override
    public void startRadio() {

        String interfaceName = getInterfaceName();

        setAllowQueueing(true);
        setTerminate(false);

        if (!interfaceName.toUpperCase(Locale.US).contains("NOT SELECTED")) {
            initializeRigctld(rigCode, interfaceName);
            if (isSinadEnabled()) {
                startSinad();
            }
        } else {
            displayMessage("Communications Port Not Set", "IO Error");
        }
    }

    @Override
    public void stopRadio() {
        if (isSinadEnabled()) {
            stopSinad();
        }
        setAllowQueueing(false);
        itemsToWrite.clear();
        setTerminate(true);
    }

    private void processData(String data) {
        try {
            if (data == null) {
                return;
            }
            LOG.log(Level.INFO, "last command: {0} sequence: {1} reply: {2}", new Object[] {getLastCommandSent(), sequence, data});
            if (data.contains("Frequency") && lastCommandSent.contains("set_freq")) {
                freqChangeConfirmed = true;
                getReceiverEvent().firePropertyChange(ReceiverEvent.CURRENT_FREQ, null, getFrequency());
            } else if (data.toUpperCase(Locale.US).contains("LEVEL VALUE") && getLastCommandSent().contains("l RAWSTR")) {
                data = data.replaceAll("\\D", "");
                rssi = Integer.parseInt(data);
                getReceiverEvent().firePropertyChange(ReceiverEvent.RSSI, null, rssi);
                synchronized (scanHold) {
                    if (freqChangeConfirmed) {
                        scanHold.notifyAll();
                    }
                }
            } else if (data.toUpperCase(Locale.US).contains("LEVEL VALUE") && getLastCommandSent().contains("l STRENGTH")) {
                data = data.replaceAll("\\D", "");
                dBm = Integer.parseInt(data);
                getReceiverEvent().firePropertyChange(ReceiverEvent.DBM, null, dBm);
            } else if (data.toUpperCase(Locale.US).contains("DCD")) {
                data = data.replaceAll("\\D+/g", "");
                busy = (data.contains("1"));
                getReceiverEvent().firePropertyChange(ReceiverEvent.BUSY, null, busy && rssi > 0);
            }
        } catch (NumberFormatException ex) {
            getReceiverEvent().firePropertyChange(ReceiverEvent.EXCEPTION, null, ex);
            LOG.log(Level.WARNING, "NumberFormatException", ex);
        }
    }

    @Override
    public void close() {
    	closeConnection();
    	userPrefs.put("rigctldPath", networkParameterSet.getInetAddress().getCanonicalHostName());
        userPrefs.putInt("rigctldPort", networkParameterSet.getPortNumber());
    	super.close();
    }

    @Override
    public void setSquelchMode(AccessMode accessMode) {
        this.accessMode = accessMode;
        switch (accessMode) {
            case CSQ -> setFunction(Function.TSQL, false);
            case DPL -> setFunction(Function.TSQL, true);
            case PL -> setFunction(Function.TSQL, true);
            case NAC -> setFunction(Function.TSQL, false);
            default -> setFunction(Function.TSQL, false);
        }
    }

    public void setOpCode(Set s, double d) {
        setOpCode(s, String.valueOf(d));
    }

    public void setOpCode(Set s, int v) {
        setOpCode(s, String.valueOf(v));
    }

    public void setOpCode(Set s, String v) {
        writeData("\\set_" + s.name().toLowerCase(Locale.US) + " " + v);
    }

    public void getOpCode(Get g) {
        writeData("\\get_" + g.name().toLowerCase(Locale.US));
    }

    public void setFunction(Function f, boolean b) {
        String s = (b) ? "1" : "0";
        writeData("U " + f + " " + s);
    }

    public void getFunction(Function f) {
        writeData("u " + f);
    }

    public void setLevel(HamlibLevel l, double d) {
        String s = new DecimalFormat("#.##").format(d);
        writeData("L " + l + " " + s);
    }

    public void getLevel(HamlibLevel l) {
        writeData("l " + l);
    }

    private void initializeShutdownHook() {
        final Thread shutdownThread = new Thread() {
            @Override
            public void run() {
                setTerminate(true);
                itemsToWrite.clear();
                closeConnection();
            }
        };
        Runtime.getRuntime().addShutdownHook(shutdownThread);
    }

    @Override
    public long getVersionUID() {
        return serialVersionUID;
    }

    @Override
    public boolean isConnected() {
        return (process != null && process.isAlive());
    }

    private final class ReadThread implements Runnable {

        private final InputStream inputStream;

        private ReadThread(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public void run() {
            while (!isTerminated()) {
                try {
                    if (inputStream.available() > 0) {
                        processData(readInputStream(inputStream));
                    } else {
                    	TimeUnit.MILLISECONDS.sleep(TERMINATE_WAIT);
                    }
                } catch (IOException ex) {
                    getReceiverEvent().firePropertyChange(ReceiverEvent.EXCEPTION, null, ex);
                    LOG.log(Level.WARNING, "IOException", ex);
                } catch (InterruptedException ex) {
                    getReceiverEvent().firePropertyChange(ReceiverEvent.EXCEPTION, null, ex);
                    LOG.log(Level.WARNING, ex.getCause().toString(), ex);
                    Thread.currentThread().interrupt();
                }
            }
        }

        private synchronized String readInputStream(InputStream is) {
            StringBuilder sb = new StringBuilder();
            if (is != null) {
                try {
                    int len;
                    do {
                        len = is.read(readBuffer, 0, readBufferLen);
                        if (len >= 0) {
                            sb.append(new String(readBuffer, 0, len, StandardCharsets.UTF_8));
                        }
                    } while (len == readBufferLen);
                    receivedData = sb.toString().trim().replace("Rig command:", "").replace("\r", "").replace("\n", "");
                    if ("".equals(receivedData)) {
                        receivedData = null;
                    }
                } catch (IOException ex) {
                    getReceiverEvent().firePropertyChange(ReceiverEvent.EXCEPTION, null, ex);
                    LOG.log(Level.WARNING, ex.getCause().toString(), ex);
                }
            }
            getReceiverEvent().firePropertyChange(ReceiverEvent.RX_DATA, null, receivedData);
            return receivedData;
        }
    }

    private synchronized String getLastCommandSent() {
        return lastCommandSent;
    }

    private class WriteThread implements Runnable {

        private synchronized void setLastCommandSent(String lastCommandSent) {
            Rigctl.this.lastCommandSent = lastCommandSent;
        }
        
        @Override
        public void run() {
            try {
                while (!isTerminated()) {
                    if (!itemsToWrite.isEmpty()) {
                        String data = itemsToWrite.take();
                        if (debug) {
                            LOG.log(Level.INFO, "Queue Size: {0}", itemsToWrite.size());
                        }
                        setLastCommandSent(data);
                        sequence++;
                        if (allowQueueing && data != null && !"null".equals(data)) {
                            sendRadioCom(data);
                            getTransmitterEvent().firePropertyChange(TransmitterEvent.TX_DATA, null, data);
                        }
                        TimeUnit.MILLISECONDS.sleep(INTERCOMMAND_PAUSE);
                        if (debug) {
                            LOG.log(Level.INFO, "write {0} : {1}", new Object[] {sequence, data});
                        }
                    } else {
                        TimeUnit.MILLISECONDS.sleep(TERMINATE_WAIT);
                    }
                }
            } catch (final InterruptedException ex) {
                getReceiverEvent().firePropertyChange(ReceiverEvent.EXCEPTION, null, ex);
                LOG.log(Level.WARNING, ex.getCause().toString(), ex);
                Thread.currentThread().interrupt();
            } finally {
                try {
                    itemsToWrite.clear();
                    sendRadioCom("q");
                    getReceiverEvent().firePropertyChange(ReceiverEvent.POWER_STATE_CHANGE, null, false);
                    Thread.sleep(WAIT_TO_CLOSE);
                    closeConnection();
                } catch (InterruptedException ex) {
                	LOG.log(Level.WARNING, ex.getCause().toString(), ex);
                    getReceiverEvent().firePropertyChange(ReceiverEvent.EXCEPTION, null, ex);
                    Thread.currentThread().interrupt();
                }
            }
        }

        private void sendRadioCom(String str) {
            try {
                str += "\r\n";
                outputStream.write(str.getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
            } catch (IOException ex) {
            	LOG.log(Level.WARNING, ex.getCause().toString(), ex);
                stopRadio();
                getReceiverEvent().firePropertyChange(ReceiverEvent.EXCEPTION, null, ex);
            }
        }
    }

    private void displayMessage(String message, String description) {
        Runnable task = () -> JOptionPane.showMessageDialog(new JDialog(), message, description, JOptionPane.ERROR_MESSAGE);
        SwingUtilities.invokeLater(task);
    }

    private void resetTimer() {
        periodicPropertyNameTimer.schedule(new PeriodicPropertyCheck(), STANDARD_PERIODIC_TIMER_INTERVAL);
    }

    private class PeriodicPropertyCheck extends TimerTask {

        @Override
        public void run() {
            if (isAllowQueueing()) {
                if (isRssiEnabled()) {
                    queryRSSI();
                }
            }
            resetTimer();
        }

        private boolean isAllowQueueing() {
            return allowQueueing;
        }

        private void queryRSSI() {
            writeData("l RAWSTR");
        }

    }

    @Override
	public JPanel[] getConfigurationComponentArray() {
		return new TcpIpConfigurationComponent(networkParameterSet, "Rigctld Tcp/Ip Settings").getSettingsPanelArray();
	}

	@Override
	public boolean isSupportsAGC() {
		return rigCaps.canSetAgc() || rigCaps.canControlFunction(Function.FAGC);
	}

	@Override
	public boolean isSupportsAFC() {
		return rigCaps.canControlFunction(Function.AFC);
	}

	@Override
	public boolean isSupportsNoiseBlanker() {
		return rigCaps.canControlFunction(Function.NB);
	}

	@Override
	public boolean isSupportsVoiceScan() {
		return rigCaps.canControlFunction(Function.VSC);
	}

	@Override
	public boolean isSupportsIFShift() {
		return rigCaps.canControlFunction(Function.IFSHIFT);
	}

	@Override
	public boolean isSupportsVolumeSet() {
		return false;
	}

	@Override
	public boolean isSupportsSquelchSet() {
		return false;
	}

	@Override
	public boolean isSupportsMultiMode() {
		return rigCaps.getModeNameList().size() > 1;
	}

	@Override
	public boolean isSupportsMultiFilter() {
		return rigCaps.getFilters().size() > 1;
	}

	@Override
	public boolean isSupportsCountryCodeRetrieval() {
		return false;
	}

	@Override
	public boolean isSupportsFirmwareRetrieval() {
		return false;
	}

	@Override
	public boolean isSupportsFeatureCodeRetrieval() {
		return false;
	}

	@Override
	public boolean isSupportsDSP() {
		return false;
	}

	@Override
	public boolean isSupportsSINAD() {
		return false;
	}

	@Override
	public boolean isSupportsBER() {
		return false;
	}

	@Override
	public boolean isSupportsRSSI() {
		return true;
	}

	@Override
	public StandardModeName[] getModeNameValues() {
		return rigCaps.getModeNameList().toArray(new StandardModeName[rigCaps.getModeNameList().size()]);
	}

	@Override
	public Integer[] getAvailableFilters() {
		return rigCaps.getFilters().toArray(new Integer[rigCaps.getFilters().size()]);
	}

	@Override
	public double getMinRxFreq() {
		return rigCaps.getMinRxFreqMHz();
	}

	@Override
	public double getMaxRxFreq() {
		return rigCaps.getMaxRxFreqMHz();
	}

	@Override
	public boolean isSupportsCOR() {
		return false;
	}

	@Override
	public boolean isSupportsAttenuator() {
		return rigCaps.canSetAttenuator();
	}

	@Override
	public int getRssiUpperLimit() {
		return 255;
	}

	@Override
	public int getRssiLowerLimit() {
		return 0;
	}

	@Override
	public String getManufacturer() {
		return rigCaps.getMfgName();
	}

	@Override
	public String getModel() {
		return rigCaps.getModelName();
	}

	@Override
	public BaudRate[] getAvailableBaudRates() {
		return rigCaps.getValidBaudRates().toArray(new BaudRate[rigCaps.getValidBaudRates().size()]);
	}

	@Override
	public BaudRate getDefaultBaudRate() {
		return BaudRate.BAUDRATE_9600;
	}

	@Override
	public DataBits getDefaultDataBits() {
		return rigCaps.getDataBits();
	}

	@Override
	public StopBits getDefaultStopBits() {
		return rigCaps.getStopBits();
	}

	@Override
	public Parity getDefaultParity() {
		return rigCaps.getParity();
	}

	@Override
	public FlowControl getDefaultFlowControlIn() {
		return rigCaps.getFlowControl();
	}

	@Override
	public FlowControl getDefaultFlowControlOut() {
		return rigCaps.getFlowControl();
	}

	@Override
	public boolean supportsRTS() {
		return rigCaps.useRTS();
	}

	@Override
	public boolean supportsDTR() {
		return rigCaps.useDTR();
	}

	@Override
	public void setCTS(boolean cts) {
		
	}

	@Override
	public void setDSR(boolean dsr) {
		
	}

	@Override
	public boolean isSerialBaudRateFixed() {
		return rigCaps.serialBaudRateFixed();
	}

	@Override
	public boolean isSerialParametersFixed() {
		return rigCaps.serialParametersFixed();
	}

	@Override
	public String getInterfaceName() {
		return  rigCaps.getMfgName() + " " + rigCaps.getModelName() + " " + rigCaps.getRigCode();
	}

	@Override
	public double getMinAttenuator() {
		return rigCaps.getMinAttenuator();
	}

	@Override
	public double getMaxAttenuator() {
		return rigCaps.getMaxAttenuator();
	}

	@Override
	public String getDefaultSerialNumber() {
		return DEFAULT_SERIAL_NUMBER;
	}

}
