package radio;

import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import radio.AbstractRadioReceiver.AccessMode;
import radio.AbstractRadioReceiver.StandardModeName;

public abstract class AbstractScanner {

    private static final long DEFAULT_TIMEOUT_PERIOD = 500;
    private static final int DEFAULT_SCAN_LIST_SIZE = 10;

    public static final String SCAN_STATUS = "SCAN_STATUS";

    private static final Logger LOG = Logger.getLogger(AbstractScanner.class.getName());

    private final ScanEvent scanEvent = new ScanEvent(this);

    private List<Double> berList;
    private List<Double> sinadList;
    private List<Double> dBmList;

    private final Preferences userPrefs = Preferences.userRoot().node("jSignalTrack/prefs/Scanner");
    private Timer timeoutTimer;
    private final ExecutorService scanExecutor = Executors.newSingleThreadExecutor();

    private List<ScanElement> scanList;
    private Measurement measurement;
    private int currentChannel;
    private int scanListSize = DEFAULT_SCAN_LIST_SIZE;
    private long sequence;
    private volatile boolean doScan;

    private volatile long timeoutPeriod = DEFAULT_TIMEOUT_PERIOD;
    private volatile long requestTime;
    private volatile long responseTime;
    private volatile long dwellTime = -1;

    protected AbstractScanner() {
        registerShutdownHook();
        initializeLists();
        loadPreferences();
    }
    
    protected abstract Measurement getChannelMeasurement(ScanElement scanElement);

    public int getScanListSize() {
        return scanList.size();
    }

    private void initializeLists() {
        scanList = Collections.synchronizedList(new CopyOnWriteArrayList<>());
        berList = Collections.synchronizedList(new CopyOnWriteArrayList<>());
        sinadList = Collections.synchronizedList(new CopyOnWriteArrayList<>());
        dBmList = Collections.synchronizedList(new CopyOnWriteArrayList<>());
        for (int i = 0; i < scanListSize; i++) {
            scanList.add(new ScanElement());
            berList.add(-1D);
            sinadList.add(-1D);
            dBmList.add(-1D);
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

    public List<ScanElement> getScanElementList() {
        return Collections.unmodifiableList(scanList);
    }

    public void setScanList(List<ScanElement> scanList) {
        boolean isScanningNow = doScan;
        stopScanNow();
        this.scanList = new CopyOnWriteArrayList<>(scanList);
        initializeLists();
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

    public double getBerScanListElement(int element) {
        return berList.get(element);
    }

    public double getdBmScanListElement(int element) {
        return dBmList.get(element);
    }

    public double getSinadScanListElement(int element) {
        return sinadList.get(element);
    }

    public ScanElement getScanElement(int channel) {
        return scanList.get(channel);
    }

    public long getDwellTime() {
        return dwellTime;
    }

    public boolean isScanning() {
        return doScan;
    }

    public void startScan() {
        startScan(scanList);
    }

    public void startScan(List<ScanElement> scanList) {
        if (scanList == null || doScan) {
            return;
        }
        this.scanList = new CopyOnWriteArrayList<>(scanList);
        int numberOfSelectedChannels = 0;
        for (int i = 0; i < scanList.size(); i++) {
            if (scanList.get(i).isScanSelected()) {
                numberOfSelectedChannels++;
            }
        }
        if (numberOfSelectedChannels >= 2) {
            currentChannel = 0;
            doScan = true;
            scanEvent.firePropertyChange(ScanEvent.SCAN_ENABLE_CHANGE, null, true);
            scanExecutor.execute(new Scan());
        } else {
            doScan = false;
            scanEvent.firePropertyChange(ScanEvent.SCAN_ENABLE_CHANGE, null, false);
        }
    }

    public void stopScan() {
        doScan = false;
        cancelTimeoutTimer();
        currentChannel = 0;
        scanEvent.firePropertyChange(ScanEvent.SCAN_ENABLE_CHANGE, null, false);
    }

    public void stopScanNow() {
        scanExecutor.shutdownNow();
        stopScan();
    }

    private class Scan implements Runnable {

        @Override
        public void run() {
            while (doScan) {
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
            LOG.log(Level.INFO, "Scanner timed out waiting for receiver to tune to channel {0} ", currentChannel - 1);
        }
    }

    private void savePreferences() {
        for (int i = 0; i < scanListSize; i++) {
            userPrefs.putDouble("scan_F" + i, scanList.get(i).getFrequency());
            userPrefs.putBoolean("scanSelect_F" + i, scanList.get(i).isScanSelected());
            userPrefs.put("scanPLCode_F" + i, scanList.get(i).getPLTone());
            userPrefs.put("scanDPLCode_F" + i, scanList.get(i).getDPLCode());
            userPrefs.put("scanNACCode_F" + i, scanList.get(i).getNetworkAccessCode());
            userPrefs.put("scanSquelchType_F" + i, scanList.get(i).getSquelchMode().name());
            userPrefs.putBoolean("scanSampleBER_F" + i, scanList.get(i).isSampleBER());
            userPrefs.putBoolean("scanSampleRSSI_F" + i, scanList.get(i).isSampleRSSI());
            userPrefs.putBoolean("scanSampleSINAD_F" + i, scanList.get(i).isSampleSINAD());
            userPrefs.putBoolean("scanNoiseBlanker_F" + i, scanList.get(i).isNoiseBlanker());
            userPrefs.putBoolean("scanAGC_F" + i, scanList.get(i).isAGC());
            userPrefs.putBoolean("scanAFC_F" + i, scanList.get(i).isAFC());
            userPrefs.putDouble("scanAttenuator_F" + i, scanList.get(i).getAttenuator());
            userPrefs.put("scanMode_F" + i, scanList.get(i).getModeName().name());
            userPrefs.putInt("scanBandwidthHz_F" + i, scanList.get(i).getBandwidthHz());
        }
    }

    private void loadPreferences() {
        scanList.clear();
        for (int i = 0; i < scanListSize; i++) {
            ScanElement se = new ScanElement();
            se.setFrequency(userPrefs.getDouble("scan_F" + i, 0.000000));
            se.setScanSelected(userPrefs.getBoolean("scanSelect_F" + i, false));
            se.setPLTone(userPrefs.get("scanPLCode_F" + i, "110.9"));
            se.setDPLCode(userPrefs.get("scanDPLCode_F" + i, "131"));
            se.setNetworkAccessCode(userPrefs.get("scanNACCode_F" + i, "293"));
            se.setSquelchMode(AccessMode.valueOf(userPrefs.get("scanSquelchType_F" + i, "CSQ")));
            se.setSampleBER(userPrefs.getBoolean("scanSampleBER_F" + i, false));
            se.setSampleRSSI(userPrefs.getBoolean("scanSampleRSSI_F" + i, true));
            se.setSampleSINAD(userPrefs.getBoolean("scanSampleSINAD_F" + i, false));
            se.setNoiseBlanker(userPrefs.getBoolean("scanNoiseBlanker_F" + i, false));
            se.setAGC(userPrefs.getBoolean("scanAGC_F" + i, false));
            se.setAFC(userPrefs.getBoolean("scanAFC_F" + i, false));
            se.setAttenuator(userPrefs.getDouble("scanAttenuator_F" + i, 0D));
            se.setBandwidthHz(userPrefs.getInt("scanBandwidthHz_F" + i, 11200));
			StandardModeName smn = StandardModeName.valueOf(userPrefs.get("scanMode_F" + i, StandardModeName.NFM.name()));
            se.setModeName(smn);
            scanList.add(se);
        }
    }

    private void registerShutdownHook() {
        final Thread shutdownThread = new Thread() {
            @Override
            public void run() {
                if (scanExecutor != null) {
                    scanExecutor.shutdownNow();
                }
                if (timeoutTimer != null) {
                    timeoutTimer.cancel();
                }
                savePreferences();
            }
        };
        Runtime.getRuntime().addShutdownHook(shutdownThread);
    }

}
