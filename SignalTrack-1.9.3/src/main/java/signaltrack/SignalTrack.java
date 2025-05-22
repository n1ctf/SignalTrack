package signaltrack;

import aprs.AprsProcessor;
import components.Bearing;
import components.CompassRose;
import components.EventPanel;
import components.SurfaceLine;
import components.TwoDimArrayList;
import components.TwoDimElement;
import aprs.APRSComponent;
import aprs.AbstractAPRSProcessor;
import aprs.AbstractTerminalNodeClient;
import aprs.AprsIcon;

import coverage.CoverageTestComponent;
import coverage.CoverageTestObject;
import coverage.DataMode;
import coverage.ManualMode;
import coverage.MeasurementSet;
import coverage.PositionSource;
import coverage.SignalAnalysis;
import coverage.SignalQualityDisplayMode;
import coverage.StaticMeasurement;
import coverage.StaticTestComponent;
import coverage.StaticTestObject;
import coverage.TestTile;
import coverage.TimingMode;

import database.Database;
import database.DatabaseConfig;
import database.DatabaseConfigComponent;
import database.DatabaseMode;

import geometry.Coordinate;
import geometry.CoordinateUtils;
import geometry.GeometryUtils;
import geometry.CoordinateUtils.Precision;

import gov.epa.AirNowAPI;

import gov.nasa.api.donki.NASASpaceWeatherProcessor;
import gov.nasa.api.donki.NASASpaceWeatherSettingsComponent;

import gov.nasa.api.donki.NASASpaceWeatherGUI.Style;

import gov.nasa.api.ners.NetworkEarthRotationService;

import gov.nasa.worldwind.geom.coords.MGRSCoord;

import gps.FixQuality;
import gps.AbstractGpsProcessor;
import gps.GPSComponent;
import gps.RdfQuality;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Frame;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Toolkit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.awt.geom.Point2D;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.lang.reflect.InvocationTargetException;

import java.math.RoundingMode;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.text.DecimalFormat;

import java.time.ZoneId;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import java.util.Set;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import javax.sound.sampled.UnsupportedAudioFileException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.SwingWorker.StateValue;
import javax.swing.Timer;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicFileChooserUI;

import map.AbstractMap;

import map.AbstractMap.MapEvent;

import map.AbstractMap.SignalTrackMapNames;

import map.MapComponent;

import meteorology.AbstractEnvironmentSensor;
import meteorology.EnvironmentMonitorComponent;
import meteorology.EnvironmentMonitorGUI;
import meteorology.EnvironmentMonitorGUI.DisplaySize;

import n1ctf.TempSensorClient;
import n1ctf.AirQualitySensorClient;
import n1ctf.GeigerCounterClient;

import radio.AbstractRadioReceiver;
import radio.Measurement;
import radio.RadioComponent;
import radio.ReceiverEvent;
import radio.ScanEvent;
import radio.SignalMeter;
import radiolocation.FlightInformation;
import radiolocation.RFPath;
import radiolocation.Triangulate;
import radio.AbstractRadioReceiver.StandardModeName;

import time.ConsolidatedTime;
import time.DateTimeServiceComponent;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import org.apache.commons.lang3.ThreadUtils;

import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.VirtualEarthTileFactoryInfo;

import tty.AbstractTeletypeController;
import tty.AbstractTeletypeController.TTYEvents;
import utility.AePlayWave;
import utility.CPUUsageCollector;
import utility.IconLoader;
import utility.Maidenhead;
import utility.PreviewPrintPanel;
import utility.PrintUtilities;
import utility.ProgressDialog;
import utility.Vincenty;

/**
 *
 * @author John
 */
public class SignalTrack extends JFrame {

    private enum TestMode {
        RSSI, RSSI_SINAD, SINAD, BER, RSSI_BER, MODE_NOT_SELECTED, OFF
    }

    private enum OperationMode {
        FULL, NORMAL, DEMO
    }

    public static final String TT4_PORT_B_NAME = "/dev/serial/by-id/usb-Prolific_Technology_Inc._USB-Serial_Controller_D-if00-port0";
    public static final String MAP_CACHE_DIR = "cache";
    public static final String CAL_FILE_DIR = "cal";
    public static final String DATA_FILE_DIR = "data";
    public static final String APP_NAME = "signaltrack";
    public static final String USER_HOME_PROPERTY_NAME = "user.home";
    public static final Point2D DEFAULT_STARTUP_COORDINATES = new Point2D.Double(-83.0, 40.0);
    public static final String DEFAULT_TEST_NAME = "default";
    public static final String DEFAULT_CALIBRI_FONT_NAME = "Calibri";
    public static final long DEFAULT_STARTUP_ALTITUDE = 5000;
    public static final Dimension PREFERRED_MAP_SIZE = new Dimension(800, 575);
    public static final String DEFAULT_HOME_FILE_PATH = System.getProperty(USER_HOME_PROPERTY_NAME) + File.separator + APP_NAME;
    public static final String DEFAULT_DATA_FILE_PATH = DEFAULT_HOME_FILE_PATH + File.separator + DATA_FILE_DIR;
    public static final String DEFAULT_CAL_FILE_PATH = DEFAULT_HOME_FILE_PATH + File.separator + CAL_FILE_DIR;
    public static final String DEFAULT_MAP_CACHE_PATH = DEFAULT_HOME_FILE_PATH + File.separator + MAP_CACHE_DIR;
    public static final OperationMode DEFAULT_OPERATION_MODE = OperationMode.NORMAL;
    public static final ZoneId DEFAULT_ZONE_ID = ZoneId.systemDefault();
    public static final Dimension MINIMUM_SCREEN_SIZE = new Dimension(1024, 768);
    public static final Point2D TEST_POINT = new Point2D.Double(-83.0, 40.0);
    public static final boolean DEFAULT_DEBUG_MODE = true;
    public static final String DEFAULT_COURIER_FONT_NAME = "Courier New";
    public static final String SPACE_WEATHER_PREDICTION_CENTER = "https://www.swpc.noaa.gov/products/alerts-watches-and-warnings";
    public static final Dimension DEFAULT_BUTTON_DIM = new Dimension(60, 28);
    public static final InputStream DEFAULT_PROCESS_COMPLETE_SOUND_STREAM = SignalTrack.class.getResourceAsStream("tada.wav");
    public static final int DEFAULT_SCAN_CHANNEL_LIST_SIZE = 10;

    private static final int TOOL_TIP_MANAGER_INITIAL_DELAY = 20;
    private static final double TEST_POINT_DBM = +0.0;
    private static final double TEST_SPEED_KPH = 300.0;
    private static final String LAST_DATA_FILE_DIRECTORY = "LAST_DATA_FILE_DIRECTORY";
    private static final long serialVersionUID = 1L;
    private static final double RDF_BEARING_LENGTH_IN_DEGREES = 0.500;
    private static final long VARIABLE_TIMER_RECALC_MILLIS = 5000;

    private static final Color ST_DARK_RED = new Color(117, 0, 0);
    private static final Color ST_BRIGHT_RED = new Color(255, 0, 0);
    private static final Color ST_DARK_GREEN = new Color(0, 200, 0);
    private static final Color ST_BRIGHT_GREEN = new Color(0, 255, 0);
    private static final Color ST_DARK_GRAY = Color.DARK_GRAY;
    private static final Logger LOG = Logger.getLogger(SignalTrack.class.getName());
    private static final int SIGNAL_MARKER_CHANNEL = 0;
    
    private final transient Preferences userPref = Preferences.userRoot().node(getClass().getName());

    private transient Triangulate triangulate;
    private transient ConsolidatedTime consolidatedTime;
    private transient AbstractMap abstractMap;
    private transient Database database;
    private transient AprsProcessor aprsProcessor;
    private transient AbstractGpsProcessor gpsProcessor;
    private transient AbstractEnvironmentSensor environmentSensor;
    private transient TempSensorClient bgts;
    private transient GeigerCounterClient radDetector;
    private transient AirQualitySensorClient aqs;
    private transient DateTimeServiceComponent dts;
    private transient AbstractRadioReceiver abstractRadioReceiver;
    private transient CoverageTestObject coverageTestObject;
    private transient StaticTestObject staticTestObject;
    private transient ExecutorService executor;
    private transient DatabaseConfig databaseConfig;
    private transient NASASpaceWeatherProcessor swp;
    private transient AirNowAPI airNow;
    private transient NetworkEarthRotationService ners;
    private transient EventPanel eventPanel;

    private EnvironmentMonitorGUI environmentMonitorGui;
    private EnvironmentMonitorComponent environmentMonitorComponent;
    private SignalAnalysis signalAnalysis;
    private JToolBar toolBar;
    private SignalMeter signalMeter;
    private CompassRose gpsCompassRose;
    private CoverageTestComponent coverageTestComponent;
    private StaticTestComponent staticTestComponent;
    private GPSComponent gpsComponent;
    private APRSComponent aprsComponent;
    private CPUUsageCollector cpuUsageCollector;
    private RadioComponent radioComponent;
    private PreviewPrintPanel previewPrintPanel;

    private DatabaseMode databaseMode;
    private DataMode dataMode;

    private JMenuBar menu;

    private JPanel mapPanel;
    private JPanel gpsInfoPanel;
    private JPanel infoPanel;
    private JPanel commPanel;

    private JLabel recordCountLabel;
    private JLabel logFileNameLabel;
    private JLabel cursorLatitude;
    private JLabel cursorLongitude;
    private JLabel cursorTerrainElevationFeet;
    private JLabel viewingAltitude;
    private JLabel gpsStatus;
    private JLabel gpsTxData;
    private JLabel gpsRxData;
    private JLabel gpsCTS;
    private JLabel gpsDSR;
    private JLabel gpsCD;
    private JLabel radioStatus;
    private JLabel radioTxData;
    private JLabel radioRxData;
    private JLabel radioCTS;
    private JLabel radioDSR;
    private JLabel radioCD;
    private JLabel aprsStatus;
    private JLabel aprsTxData;
    private JLabel aprsRxData;
    private JLabel aprsCTS;
    private JLabel aprsDSR;
    private JLabel aprsCD;
    private JLabel markerID;
    private JLabel gpsPanelLatitude;
    private JLabel gpsPanelLongitude;
    private JLabel gpsPanelSpeedMadeGood;
    private JLabel utcLabel;
    private JLabel cursorMGRS;
    private JLabel gpsPanelMGRS;
    private JLabel cursorMaidenheadReference;
    private JLabel gpsPanelGridSquare;
    private JLabel gpsPanelAltitude;
    private JLabel measurementPeriod;
    private JLabel measurementsThisTile;
    private JLabel maxMeasurementsPerTile;
    private JLabel minMeasurementsPerTile;
    private JLabel tilesCompleted;
    private JLabel totalTiles;
    private JLabel averagedBmInCurrentTile;
    private JLabel averageBerInCurrentTile;
    private JLabel averageSinadInCurrentTile;
    private JLabel messageLabel;

    private JLabel[] signalQuality;

    private Timer variableTimer;
    private Timer fixedTimer;
    private Timer mouseMovedTimer;
    private Timer gpsValidHeadingTimer;
    private Timer zoomOutMouseDownTimer;
    private Timer zoomInMouseDownTimer;

    private JButton saveDataFileButton;
    private JButton centerOnGpsButton;
    private JButton mapFullScreenButton;
    private JButton zoomInButton;
    private JButton zoomOutButton;
    private JButton doActionButton;
    private JButton newDataFileButton;
    private JButton openCoverageTestDataFileButton;

    private JToggleButton closeDataFileButton;
    private JToggleButton stopDataFileButton;
    private JToggleButton recordDataFileButton;
    private JToggleButton gpsButton;
    private JToggleButton radioButton;
    private JToggleButton measureButton;
    private JToggleButton aprsButton;
    private JToggleButton learnModeButton;
    private JToggleButton coverageTestButton;
    private JToggleButton staticLocationAnalysisButton;

    private JMenuItem aprsComponentMenuItem;
    private JMenuItem nasaSpaceWeatherComponentMenuItem;
    private JMenuItem gpsComponentMenuItem;
    private JMenuItem receiverComponentMenuItem;
    private JMenuItem environmentMonitorComponentMenuItem;
    private JMenuItem coverageTestSettingsMenuItem;
    private JMenuItem mapSettingsMenuItem;
    private JMenuItem mapBulkDownloaderMenuItem;
    private JMenuItem mapStatisticsMenuItem;
    private JMenuItem mapCacheViewerMenuItem;
    private JMenuItem mapLayerSelectorMenuItem;
    private JMenuItem mapClearMenuItem;
    private JMenuItem newDataFileMenuItem;
    private JMenuItem openDataFileMenuItem;
    private JMenuItem closeDataFileMenuItem;
    private JMenuItem saveDataFileMenuItem;
    private JMenuItem saveAsDataFileMenuItem;
    private JMenuItem printPreviewMenuItem;
    private JMenuItem printMenuItem;
    private JMenuItem exitMenuItem;
    private JMenuItem staticSignalLocationSettingsMenuItem;
    private JMenuItem signalAnalysisMenuItem;
    private JMenuItem databaseConfigMenuItem;
    private JMenuItem aboutMenuItem;
    private JMenuItem cpuUsageMenuItem;
    private JMenuItem localEnvironmentMonitorMenuItem;

    private String startRadioCalFileName;

    private File dataDirectory;

    private transient Point2D startupLonLat;
    private transient Point2D cursorBearingPosition;

    private double cursorBearing;

    private int tilesComplete;

    private int markerCounter;
    private int cursorBearingIndex;
    private int staticRecordRestoreProgress;
    private int staticRecordCount;
    private int coverageRecordRestoreProgress;
    private int tileRecordRestoreProgress;
    private int tileRecordCount;
    private int flightInformationFlightNumber;

    private int startMapArg = -1;
    private int startGpsArg = -1;
    private int environmentSensorSelectArg = -1;
    private int startTNCArg = -1;

    private long startupAltitude;

    private OperationMode operationMode = DEFAULT_OPERATION_MODE;

    private volatile boolean serialErrorQueued;

    private boolean coverageTestActive;
    private boolean staticAnalysisActive;
    private boolean dopplerActive;
    private boolean cursorBearingSet;
    private boolean startInDemoMode;
    private boolean clearAllPrefs;
    private boolean showCPULoad;
    private boolean mouseOffGlobe = true;
    private boolean mapDragged;
    private boolean testGridLearnMode;
    private boolean testGridSelectionMode;
    private boolean measureMode;
    private boolean bulkDownloadSelectionMode;
    private boolean mapReady;
    private boolean altKeyDown;
    private boolean inhibitLeftMouseButtonReleaseEvent;

    private transient PropertyChangeListener environmentSensorListener;
    private transient PropertyChangeListener radioListener;
    private transient PropertyChangeListener aprsListener;
    private transient PropertyChangeListener databaseListener;
    private transient PropertyChangeListener mapPropertyChangeListener;
    private transient PropertyChangeListener coverageTestObjectListener;
    private transient PropertyChangeListener gpsListener;
    private transient MouseMotionListener mapMouseMotionListener;
    private transient MouseListener mapMouseListener;

    private ProgressDialog coverageTestRestoreMonitor;
    private ProgressDialog staticTestRestoreMonitor;
    private ProgressDialog newDatabaseMonitor;

    private final long variableTimerSetMillis = System.currentTimeMillis();
    private long previousGpsTimeInMillis;
    private transient ZoneId previousTimeZoneId;

    private transient TestTile cursorTestTile;

    private String testName;

    private Boolean debug = DEFAULT_DEBUG_MODE;

    private transient Point2D mapCurrentCursor;

    private String gpsClassName;
    private String tncClassName;
    private String environmentSensorClassName;

    private SignalTrackMapNames signalTrackMapName;

    private final transient ScheduledExecutorService aprsScheduler = Executors.newScheduledThreadPool(0);
    
    private transient ScheduledFuture<?> staticSchedulerHandle;
    private transient ScheduledExecutorService staticScheduler;
        
    private final transient ExecutorService aePlayWaveExecutor = Executors.newSingleThreadExecutor();
    
    private final BlockingQueue<StaticMeasurement> measurementProcessQueue = new ArrayBlockingQueue<>(2048);

    private final TwoDimArrayList<Integer, Integer> processorStartIndex = new TwoDimArrayList<>();
    private final transient List<Bearing> bearingList = Collections.synchronizedList(new CopyOnWriteArrayList<>());
    private final transient List<Point2D> intersectList = Collections.synchronizedList(new CopyOnWriteArrayList<>());
    private final TwoDimArrayList<FlightInformation, StaticMeasurement> flightMeasurementArray = new TwoDimArrayList<>();

    private transient Action leftAction;
    private transient Action rightAction;
    private transient Action spaceAction;
    private transient Action enterAction;
    private transient Action escapeAction;
    private transient Action altAction;
    private transient Action altReleaseAction;
    private transient Action f2Action;

    /**
     *
     * @param args
     */
    public SignalTrack(String[] args) {

        initializeWindowListener();

        configureLogger();
        initializeShutdownHook();
        initializeLookAndFeel();
        initializeCLI(args);
        getSettingsFromRegistry();

        if (!validateEnvironment()) {
            LOG.log(Level.SEVERE, "Fatal System Environment Configuration Failure");
            close();
        }

        databaseConfig = new DatabaseConfig(isClearAllPrefs());
        
        setOperationMode(operationMode);

        if (isShowCPULoad()) {
            new CPUUsageCollector();
        }
    }

    private void initializeWindowListener() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent event) {
                if (WindowEvent.WINDOW_CLOSING == event.getID()) {
                    close();
                }
            }
        });
    }

    private void configureLogger() {
        try {
            final Handler fh = new FileHandler("%t/SignalTrack.log");
            final Handler ch = new ConsoleHandler();
            LOG.addHandler(fh);
            LOG.setLevel(Level.FINEST);
            LOG.addHandler(ch);
            LOG.setUseParentHandlers(false);
        } catch (SecurityException | IOException e) {
            LOG.log(Level.WARNING, e.getMessage());
        }
    }

    private void initializeCLI(String[] args) {
        try {
            final Options options = new Options();
            final CommandLineParser parser = new DefaultParser();

            options.addOption("s", true, "abstractMap style");
            options.addOption("r", true, "abstractRadioReceiver calibration file to use");
            options.addOption("g", true, "gpsProcessor");
            options.addOption("a", true, "aprs");
            options.addOption("m", "mode", true, "operation mode");
            options.addOption("d", "debug", false, "debug mode");
            options.addOption("c", "clear", false, "clear all preferences");
            options.addOption("t", true, "test name");
            options.addOption("p", "cpu", false, "cpu load indicator");
            options.addOption("w", true, "atmosphere sensor");

            final CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("r")) {
                startRadioCalFileName = cmd.getOptionValue("r");
            }

            if (cmd.hasOption("s")) {
                startMapArg = Integer.parseInt(cmd.getOptionValue("s"));
            }

            if (cmd.hasOption("g")) {
                startGpsArg = Integer.parseInt(cmd.getOptionValue("g"));
            }

            if (cmd.hasOption("a")) {
                startTNCArg = Integer.parseInt(cmd.getOptionValue("a"));
            }

            if (cmd.hasOption("c")) {
                setClearAllPrefs(true);
                clearAllPreferences();
            }

            if (cmd.hasOption("t")) {
                testName = cmd.getOptionValue("t");
            }

            if (cmd.hasOption("p")) {
                setShowCPULoad(true);
            }

            if (cmd.hasOption("m")) {
            	final String mode = cmd.getOptionValue("m");
                operationMode = OperationMode.valueOf(mode);
            }

            if (cmd.hasOption("d")) {
                setDebug(true);
            }

            if (cmd.hasOption("w")) {
                environmentSensorSelectArg = Integer.parseInt(cmd.getOptionValue("w"));
            }

        } catch (ParseException ex) {
            LOG.log(Level.WARNING, ex.getMessage(), ex);
        } catch  (IllegalArgumentException ex ) {
        	LOG.log(Level.WARNING, "An Illegal Argument has been passed via the command line", ex);
        }
    }

    private void configureFrame() {
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        setMinimumSize(MINIMUM_SCREEN_SIZE);
        setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
        setAutoRequestFocus(true);
        setResizable(true);
        setExtendedState(Frame.NORMAL);
        setAlwaysOnTop(false);
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        revalidate();
        repaint();
    }

    private boolean validateEnvironment() {
        try {
        	final File homePath = new File(DEFAULT_HOME_FILE_PATH);
            if (!homePath.exists()) {
                Files.createDirectory(homePath.toPath());
                LOG.log(Level.INFO, "SignalTrack - created application home directory: {0}", homePath);
            } else {
                LOG.log(Level.INFO, "SignalTrack - application home directory validated: {0}", homePath);
            }
        } catch (final IOException e) {
            LOG.log(Level.SEVERE, "SignalTrack: exception while creating SignalTrack home "
                    + "directory in the user home directory", e);
            return false;
        }

        try {
        	final File mapCachePath = new File(DEFAULT_MAP_CACHE_PATH);
            if (!mapCachePath.exists()) {
                Files.createDirectory(mapCachePath.toPath());
                LOG.log(Level.INFO, "SignalTrack - created map cache directory: {0}", mapCachePath);
            } else {
                LOG.log(Level.INFO, "SignalTrack - abstractMap cache directory validated: {0}", mapCachePath);
            }
        } catch (final IOException e) {
            LOG.log(Level.SEVERE, "SignalTrack: exception while creating map cache "
                    + "directory in the user home directory", e);
            return false;
        }

        try {
        	final File calFilePath = new File(DEFAULT_CAL_FILE_PATH);
            if (!calFilePath.exists()) {
                Files.createDirectory(calFilePath.toPath());
                LOG.log(Level.INFO, "SignalTrack - created calibration file directory: {0}", calFilePath);
            } else {
                LOG.log(Level.INFO, "SignalTrack - calibration file directory validated: {0}", calFilePath);
            }
        } catch (final IOException e) {
            LOG.log(Level.SEVERE, "SignalTrack: exception while creating calibration file "
                    + "directory in the user home directory", e);
            return false;
        }

        try {
        	final String calFileDirectory = DEFAULT_CAL_FILE_PATH;
            if (listFiles(calFileDirectory).toArray().length == 0) {
            	final String newFileName = AbstractRadioReceiver.createNewDefaultCalFileRecord();
                LOG.log(Level.INFO, "There are no calibration files present. Adding default cal file: {0}", newFileName);

            }
            // Check if the cal file name saved in the system is not "null", which would mean there is no cal file on record. 
            // Also, check if the saved cal file name is present in the specified directory.
            // Finally, check if the file path and name on record is actually pointing to the correct directory.
            // If any are false, then set startRadioCalFileName to the name of the first cal file present in the system.
            if (startRadioCalFileName.contains("null") || !isFilePresent(calFileDirectory, startRadioCalFileName)
                    || !startRadioCalFileName.equals(calFileDirectory + File.separator + startRadioCalFileName)) {
            	final Set<String> list = listFiles(calFileDirectory);
                startRadioCalFileName = calFileDirectory + File.separator + (String) list.toArray()[0];
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Unable to list files in directory {0}", DEFAULT_CAL_FILE_PATH);
            return false;
        }

        return dataDirectory.exists() || dataDirectory.mkdirs();
    }

    public static boolean isFilePresent(String pathToFile, String fileName) {
        try {
            for (Object obj : listFiles(pathToFile).toArray()) {
                if (fileName.equals(obj)) {
                    return true;
                }
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, ex.getMessage());
        }
        return false;
    }

    public static Set<String> listFiles(String dir) throws IOException {
        try (Stream<Path> stream = Files.list(Paths.get(dir))) {
            return stream
                .filter(file -> !Files.isDirectory(file))
                .map(Path::getFileName)
                .map(Path::toString)
                .collect(Collectors.toSet());
        }
    }

    private void setOperationMode(OperationMode operationMode) {
        switch (operationMode) {
			case NORMAL -> {
				configureFrame();
				initializeGpsObjects();
				initializeGuiComponents();
				initializeMapObjects();
				initializeTimekeepingObjects();
				initializeMeasurementObjects();
				initializeEnvironmentObjects();
				initializeAprsObjects();
				configureDesktopComponents();
				initializeComponentListeners();
				initializeEnvironmentSensorListeners();
				initializeGpsListener();
				initializeTimekeepingListeners();
				startAprsProcesses();
				startDatabaseProcesses();
				startGpsProcesses();
				startTimekeepingProcesses();
				setJMenuBar(menu);
				signalMeter.setVisible(true);
				gpsCompassRose.setVisible(true);
				gpsInfoPanel.setVisible(true);
				infoPanel.setVisible(true);
				commPanel.setVisible(true);
				toolBar.setVisible(true);
				swp = new NASASpaceWeatherProcessor(Style.SINGLE_COLUMN);
				revalidate();
			}
			case DEMO -> {
				configureFrame();
				initializeGpsObjects();
				initializeGuiComponents();
				initializeMapObjects();
				initializeTimekeepingObjects();
				initializeMeasurementObjects();
				initializeEnvironmentObjects();
				initializeAprsObjects();
				configureDesktopComponents();
				initializeComponentListeners();
				initializeEnvironmentSensorListeners();
				initializeGpsListener();
				initializeTimekeepingListeners();
				startAprsProcesses();
				startDatabaseProcesses();
				startGpsProcesses();
				startTimekeepingProcesses();
				setJMenuBar(menu);
				signalMeter.setVisible(true);
				gpsCompassRose.setVisible(true);
				gpsInfoPanel.setVisible(true);
				infoPanel.setVisible(true);
				commPanel.setVisible(true);
				toolBar.setVisible(true);
				swp = new NASASpaceWeatherProcessor(Style.SINGLE_COLUMN);
				revalidate();
				startDemo();
			}
			case FULL -> {
				configureFrame();
				initializeGpsObjects();
				initializeGuiComponents();
				initializeMapObjects();
				initializeTimekeepingObjects();
				initializeMeasurementObjects();
				initializeEnvironmentObjects();
				initializeAprsObjects();
				configureDesktopComponents();
				initializeComponentListeners();
				initializeEnvironmentSensorListeners();
				initializeGpsListener();
				initializeTimekeepingListeners();
				startAprsProcesses();
				startDatabaseProcesses();
				startGpsProcesses();
				startTimekeepingProcesses();
				setJMenuBar(null);
				signalMeter.setVisible(false);
				gpsCompassRose.setVisible(false);
				gpsInfoPanel.setVisible(false);
				infoPanel.setVisible(false);
				commPanel.setVisible(false);
				toolBar.setVisible(false);
				swp = new NASASpaceWeatherProcessor(Style.SINGLE_COLUMN);
				createNormalGUI();
				displayFullGUI();
				revalidate();
			}
		}
    }

    private void initializeTimekeepingObjects() {
        consolidatedTime = new ConsolidatedTime();
        dts = new DateTimeServiceComponent(consolidatedTime, ners);
    }

    private void initializeGuiComponents() {
        ToolTipManager.sharedInstance().setInitialDelay(TOOL_TIP_MANAGER_INITIAL_DELAY);
        toolBar = new JToolBar();
        closeDataFileButton = new JToggleButton();
        stopDataFileButton = new JToggleButton();
        recordDataFileButton = new JToggleButton();
        gpsButton = new JToggleButton();
        radioButton = new JToggleButton();
        measureButton = new JToggleButton();
        aprsButton = new JToggleButton();
        learnModeButton = new JToggleButton();
        coverageTestButton = new JToggleButton();
        staticLocationAnalysisButton = new JToggleButton();

        newDataFileButton = new JButton();
        openCoverageTestDataFileButton = new JButton();
        centerOnGpsButton = new JButton();
        mapFullScreenButton = new JButton();
        zoomInButton = new JButton();
        zoomOutButton = new JButton();
        saveDataFileButton = new JButton();
        doActionButton = new JButton();

        messageLabel = new JLabel();

        eventPanel = new EventPanel();
        
        menu = new JMenuBar();

        setJMenuBar(menu);

        final JMenu fileMenu = new JMenu(" File ");
        final JMenu gpsMenu = new JMenu(" GPS ");
        final JMenu receiverMenu = new JMenu(" Receiver ");
        final JMenu systemMenu = new JMenu(" Coverage Test ");
        final JMenu mapMenu = new JMenu(" Map ");
        final JMenu aprsMenu = new JMenu(" APRS ");
        final JMenu environmentMonitorMenu = new JMenu(" Environment ");
        final JMenu staticSignalLocationMenu = new JMenu(" Static Signal Location ");
        final JMenu signalAnalysisMenu = new JMenu(" Signal Analysis ");
        final JMenu databaseConfigMenu = new JMenu(" Database ");
        final JMenu helpMenu = new JMenu(" Help ");

        menu.add(fileMenu);
        menu.add(gpsMenu);
        menu.add(receiverMenu);
        menu.add(systemMenu);
        menu.add(mapMenu);
        menu.add(aprsMenu);
        menu.add(environmentMonitorMenu);
        menu.add(staticSignalLocationMenu);
        menu.add(signalAnalysisMenu);
        menu.add(databaseConfigMenu);
        menu.add(helpMenu);

        aprsComponentMenuItem = new JMenuItem(" APRS Settings ");
        nasaSpaceWeatherComponentMenuItem = new JMenuItem(" Space Weather Monitor Settings ");
        gpsComponentMenuItem = new JMenuItem(" GPS Settings ");
        receiverComponentMenuItem = new JMenuItem(" Receiver Settings ");
        environmentMonitorComponentMenuItem = new JMenuItem(" Environment Monitor Settings ");
        coverageTestSettingsMenuItem = new JMenuItem(" Coverage Test Settings ");
        mapSettingsMenuItem = new JMenuItem(" Map Settings ");
        mapBulkDownloaderMenuItem = new JMenuItem(" Bulk Downloader ");
        mapLayerSelectorMenuItem = new JMenuItem(" Layer Selector ");
        mapStatisticsMenuItem = new JMenuItem(" Statistics Panel ");
        mapCacheViewerMenuItem = new JMenuItem(" Cache Viewer Panel ");
        mapClearMenuItem = new JMenuItem(" Clear Map ");
        newDataFileMenuItem = new JMenuItem(" New Database File ");
        openDataFileMenuItem = new JMenuItem(" Open Coverage Test Database File ");
        closeDataFileMenuItem = new JMenuItem(" Close Database File ");
        saveDataFileMenuItem = new JMenuItem(" Save Database File ");
        saveAsDataFileMenuItem = new JMenuItem(" Save Database File As ");
        printPreviewMenuItem = new JMenuItem(" Print Preview ");
        printMenuItem = new JMenuItem(" Print... ");
        exitMenuItem = new JMenuItem(" Exit ");
        staticSignalLocationSettingsMenuItem = new JMenuItem(" Static Signal Location Settings ");
        signalAnalysisMenuItem = new JMenuItem(" Signal Analysis Monitor ");
        databaseConfigMenuItem = new JMenuItem(" Database Configuration ");
        aboutMenuItem = new JMenuItem(" About SignalTrack ");
        cpuUsageMenuItem = new JMenuItem(" CPU Usage Monitor ");
        localEnvironmentMonitorMenuItem = new JMenuItem(" Local Environment Monitor ");

        environmentMonitorMenu.add(localEnvironmentMonitorMenuItem);
        environmentMonitorMenu.addSeparator();
        environmentMonitorMenu.add(nasaSpaceWeatherComponentMenuItem);
        environmentMonitorMenu.add(environmentMonitorComponentMenuItem);
        
        fileMenu.add(newDataFileMenuItem);
        fileMenu.add(openDataFileMenuItem);
        fileMenu.add(closeDataFileMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(saveDataFileMenuItem);
        fileMenu.add(saveAsDataFileMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(printPreviewMenuItem);
        fileMenu.add(printMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(exitMenuItem);

        gpsMenu.add(gpsComponentMenuItem);

        receiverMenu.add(receiverComponentMenuItem);

        systemMenu.add(coverageTestSettingsMenuItem);

        mapMenu.add(mapSettingsMenuItem);
        mapMenu.add(mapBulkDownloaderMenuItem);
        mapMenu.add(mapStatisticsMenuItem);
        mapMenu.add(mapCacheViewerMenuItem);
        mapMenu.add(mapLayerSelectorMenuItem);
        mapMenu.addSeparator();
        mapMenu.add(mapClearMenuItem);

        signalMeter = new SignalMeter();

        gpsCompassRose = new CompassRose();

        aprsMenu.add(aprsComponentMenuItem);

        staticSignalLocationMenu.add(staticSignalLocationSettingsMenuItem);

        signalAnalysisMenu.add(signalAnalysisMenuItem);

        databaseConfigMenu.add(databaseConfigMenuItem);

        helpMenu.add(aboutMenuItem);
        helpMenu.add(cpuUsageMenuItem);

        gpsPanelLongitude = new JLabel();
        gpsPanelLatitude = new JLabel();
        utcLabel = new JLabel();
        gpsPanelSpeedMadeGood = new JLabel();
        cursorLatitude = new JLabel();
        cursorLongitude = new JLabel();
        viewingAltitude = new JLabel();
        cursorTerrainElevationFeet = new JLabel();
        gpsStatus = new JLabel();
        gpsTxData = new JLabel();
        gpsRxData = new JLabel();
        gpsCTS = new JLabel();
        gpsDSR = new JLabel();
        gpsCD = new JLabel();
        radioStatus = new JLabel();
        radioTxData = new JLabel();
        radioRxData = new JLabel();
        radioCTS = new JLabel();
        radioDSR = new JLabel();
        radioCD = new JLabel();
        signalQuality = new JLabel[DEFAULT_SCAN_CHANNEL_LIST_SIZE];
        aprsStatus = new JLabel();
        aprsTxData = new JLabel();
        aprsRxData = new JLabel();
        aprsCTS = new JLabel();
        aprsDSR = new JLabel();
        aprsCD = new JLabel();
        markerID = new JLabel();
        recordCountLabel = new JLabel();
        logFileNameLabel = new JLabel();
        gpsPanelMGRS = new JLabel();
        cursorMGRS = new JLabel();
        gpsPanelGridSquare = new JLabel();
        gpsPanelAltitude = new JLabel();
        cursorMaidenheadReference = new JLabel();
        measurementPeriod = new JLabel();
        measurementsThisTile = new JLabel();
        maxMeasurementsPerTile = new JLabel();
        minMeasurementsPerTile = new JLabel();
        tilesCompleted = new JLabel();
        totalTiles = new JLabel();
        averagedBmInCurrentTile = new JLabel();
        averageBerInCurrentTile = new JLabel();
        averageSinadInCurrentTile = new JLabel();
    }

    private void initializeMeasurementObjects() {
        executor = Executors.newCachedThreadPool();
        signalAnalysis = new SignalAnalysis();
        abstractRadioReceiver = AbstractRadioReceiver.getRadioInstance(new File(startRadioCalFileName), isClearAllPrefs());
        coverageTestObject = new CoverageTestObject(isClearAllPrefs(), abstractRadioReceiver, testName);
        staticTestObject = new StaticTestObject(isClearAllPrefs(), new File(testName));
    }

    private void initializeEnvironmentObjects() {
        if (environmentSensorSelectArg != -1) {
            if (environmentSensorSelectArg > AbstractEnvironmentSensor.getCatalogMap().size() - 1) {
                environmentSensorSelectArg = 0;
            }
            environmentSensorClassName = (String) AbstractEnvironmentSensor.getCatalogMap().keySet().toArray()[environmentSensorSelectArg];
        }

        environmentSensor = AbstractEnvironmentSensor.getInstance(environmentSensorClassName, consolidatedTime, isClearAllPrefs());
        environmentSensor.addPropertyChangeListener(environmentSensorListener);

        if (environmentSensor.isStartWithSystem()) {
            environmentSensor.startSensor();
        }

        bgts = new TempSensorClient(false, true, false, false);

        if (bgts.isStartWithSystem()) {
            bgts.start();
        }

        radDetector = new GeigerCounterClient(true, true, true, true);
        
        airNow = new AirNowAPI();

        ners = new NetworkEarthRotationService();
        
        aqs = new AirQualitySensorClient();
    }

    private void initializeGpsObjects() {
        if (startGpsArg != -1) {
            if (startGpsArg > AbstractGpsProcessor.getCatalogMap().size() - 1) {
                startGpsArg = 0;
            }
            gpsClassName = (String) AbstractGpsProcessor.getCatalogMap().keySet().toArray()[startGpsArg];
        }
        
        gpsProcessor = AbstractGpsProcessor.getReceiverInstance(gpsClassName, isClearAllPrefs());
    }

    private void initializeMapObjects() {
        cursorTestTile = new TestTile();

        if (startMapArg != -1) {
            if (startMapArg > AbstractMap.getSignalTrackMapProviderCatalog().length - 1) {
                startMapArg = 0;
            }
            signalTrackMapName = SignalTrackMapNames.values()[startMapArg];
        }

        abstractMap = initializeMapInterface(signalTrackMapName, startupLonLat, startupAltitude);
    }

    private void configureKeyActions() {
    	final String LEFT = "Left";
    	final String RIGHT = "Right";
    	final String SPACE = "Space";
    	final String ENTER = "Enter";
    	final String ALT = "Alt";
    	final String ALT_RELEASE = "AltRelease";
    	final String ESCAPE = "Escape";
    	final String F2 = "F2";

        abstractMap.addMouseListener(mapMouseListener);
        abstractMap.addMouseMotionListener(mapMouseMotionListener);
        abstractMap.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), LEFT);
        abstractMap.getActionMap().put(LEFT, leftAction);
        abstractMap.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), RIGHT);
        abstractMap.getActionMap().put(RIGHT, rightAction);
        abstractMap.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), SPACE);
        abstractMap.getActionMap().put(SPACE, spaceAction);
        abstractMap.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), ENTER);
        abstractMap.getActionMap().put(ENTER, enterAction);
        abstractMap.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), ESCAPE);
        abstractMap.getActionMap().put(ESCAPE, escapeAction);
        abstractMap.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ALT, InputEvent.ALT_DOWN_MASK, false), ALT);
        abstractMap.getActionMap().put(ALT, altAction);
        abstractMap.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ALT, 0, true), ALT_RELEASE);
        abstractMap.getActionMap().put(ALT_RELEASE, altReleaseAction);
        abstractMap.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), F2);
        abstractMap.getActionMap().put(F2, f2Action);

        leftAction = new AbstractAction(LEFT) {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isCursorBearingSet() && isDopplerActive()) {
                    if (cursorBearing > 0) {
                        cursorBearing--;
                    } else {
                        cursorBearing = 359;
                    }
                    moveRdfBearing(cursorBearingIndex, cursorBearingPosition, cursorBearing,
                            RDF_BEARING_LENGTH_IN_DEGREES, 8, ST_BRIGHT_RED);
                }
            }
        };

        rightAction = new AbstractAction(RIGHT) {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isCursorBearingSet() && isDopplerActive()) {
                    if (cursorBearing < 359) {
                        cursorBearing++;
                    } else {
                        cursorBearing = 0;
                    }
                    moveRdfBearing(cursorBearingIndex, cursorBearingPosition, cursorBearing,
                            RDF_BEARING_LENGTH_IN_DEGREES, 8, ST_BRIGHT_RED);
                }
            }
        };

        spaceAction = new AbstractAction(SPACE) {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                markerCounter++;
                invokeLaterInDispatchThreadIfNeeded(
                        () -> markerID.setText(String.format(getLocale(), "%5d", markerCounter)));
            }
        };

        enterAction = new AbstractAction(ENTER) {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (isDopplerActive()) {
                    setCursorBearingSet(true);
                    processBearingInformation();
                }
            }
        };

        escapeAction = new AbstractAction(ESCAPE) {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                setOperationMode(OperationMode.NORMAL);
            }
        };

        altAction = new AbstractAction(ALT) {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isAltKeyDown()) {
                    setAltKeyDown(true);
                }
            }
        };

        altReleaseAction = new AbstractAction(ALT_RELEASE) {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                setAltKeyDown(false);
            }
        };

        f2Action = new AbstractAction(F2) {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                // String pkwdwpl = gpsProcessor.getPKWDWPLTestString(abstractMap.getMouseCoordinates());
                // getAprsProcessor().getAPRSTNCClient().processNMEAData(pkwdwpl);
            }
        };

    }

    private void initializeAprsObjects() {
        if (startTNCArg != -1) {
            if (startTNCArg > AbstractTerminalNodeClient.getCatalogMap().size() - 1) {
                startTNCArg = 0;
            }
            tncClassName = (String) AbstractTerminalNodeClient.getCatalogMap().keySet().toArray()[startTNCArg];
        }

        aprsProcessor = new AprsProcessor(environmentSensor, isDebug());
    }

    private void initializeComponentListeners() {
        mapMouseListener = new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent event) {
            	if (event.getButton() == MouseEvent.BUTTON1 && !isInhibitLeftMouseButtonReleaseEvent()) {
            		if (isTestTileSelectMode() && isMapDragged()) {
                        invokeLaterInDispatchThreadIfNeeded(() -> {
                            doActionButton.setText("Toggle Selected Tiles");
                            doActionButton.setToolTipText("Press to add or remove selected tiles from current test database");
                            doActionButton.setVisible(true);
                        });
                        setMapDragged(false);
                    }
                    if (isBulkMapTileDownloadSelectionMode() && isMapDragged()) {
                        invokeLaterInDispatchThreadIfNeeded(() -> {
                            doActionButton.setText("Download Selected Area");
                            doActionButton.setToolTipText("Press to download selected area to offline map cache");
                            doActionButton.setVisible(true);
                        });
                        setMapDragged(false);
                    }
                }
            }

            @Override
            public void mouseEntered(final MouseEvent event) {
                setMouseOffGlobe(false);
            }

            @Override
            public void mouseExited(final MouseEvent event) {
                setMouseOffGlobe(true);
            }

            @Override
            public void mousePressed(final MouseEvent event) {
                if (event.getButton() == MouseEvent.BUTTON1) {
                    mapPanelLeftMouseButtonPressed();
                }
                if (event.getButton() == MouseEvent.BUTTON3) {
                    mapPanelRightMouseButtonPressed();
                }
            }

            @Override
            public void mouseReleased(final MouseEvent event) {
                if (event.getButton() == MouseEvent.BUTTON1 && !isInhibitLeftMouseButtonReleaseEvent()) {
                	if (isTestTileSelectMode() && isMapDragged()) {
                        invokeLaterInDispatchThreadIfNeeded(() -> {
                            doActionButton.setText("Toggle Selected Tiles");
                            doActionButton.setToolTipText("Press to add or remove selected tiles from current test database");
                            doActionButton.setVisible(true);
                        });
                        setMapDragged(false);
                    }
                    if (isBulkMapTileDownloadSelectionMode() && isMapDragged()) {
                        invokeLaterInDispatchThreadIfNeeded(() -> {
                            doActionButton.setText("Download Selected Area");
                            doActionButton.setToolTipText("Press to download selected area to offline map cache");
                            doActionButton.setVisible(true);
                        });
                        setMapDragged(false);
                    }
                }
                setInhibitLeftMouseButtonReleaseEvent(false);
            }
        };

        mapMouseMotionListener = new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
            	// TODO:: add buffer to control how much drag before action is taken
                setMapDragged(true);
                mapCurrentCursor = abstractMap.getMouseDragCoordinates();
                handleMouseDrag();
                if (isTestGridDrawOutlineMode() || isBulkMapTileDownloadSelectionMode() || isMeasureMode() || isTestTileSelectMode()) {
                    e.consume();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                if (!Coordinate.validLongitude(abstractMap.getMouseCoordinates().getY())) {
                    return;
                }

                final Point2D coords = abstractMap.getMouseCoordinates();

                if (coords != null) {
                    invokeLaterInDispatchThreadIfNeeded(() -> {
                        updateMGRSLabel(coords);
                        cursorLatitude.setText(String.format(getLocale(), "%8f", coords.getY()));
                        cursorLongitude.setText(String.format(getLocale(), "%9f", coords.getX()));
                        final double elevation = abstractMap.getCursorElevationMeters();
                        if (elevation > Short.MIN_VALUE) {
                        	final double feet = Vincenty.FEET_PER_METER * elevation;
                            cursorTerrainElevationFeet.setText(String.format(getLocale(), "%6d", (int) feet) + " FT");
                        } else {
                            cursorTerrainElevationFeet.setText("");
                        }
                    });
                }

                if (getDatabaseMode() == DatabaseMode.CLOSED) {
                    mouseMovedTimer.restart();
                } else {
                    if (coverageTestObject.getTestGridRectangle() != null && !isMouseOffGlobe()
                            && coords != null) {
                        invokeLaterInDispatchThreadIfNeeded(() -> {
                            final TestTile testTile = CoordinateUtils.lonLatToTestTile(coords,
                                    coverageTestObject.getTileSizeArcSeconds(),
                                    new Point2D.Double(coverageTestObject.getTestGridRectangle().getX(),
                                            coverageTestObject.getTestGridRectangle().getY()),
                                    new Point2D.Double(coverageTestObject.getTestGridRectangle().getWidth(),
                                            coverageTestObject.getTestGridRectangle().getHeight()));
                            if (testTile != null && !testTile.getMessage().contains("Off Test Grid")
                                    && !isTestTileSelectMode() && abstractMap.isShowGrid()) {
                                cursorMaidenheadReference.setHorizontalAlignment(SwingConstants.LEFT);
                                cursorMaidenheadReference.setText(testTile.toFormattedTestTileDesignator());
                                if ((database != null && getDatabaseMode() != DatabaseMode.CLOSED)
                                        && (cursorTestTile != null)
                                        && !cursorTestTile.equalsLonLat(testTile)) {
                                    database.getTileMeasurementSetCount(measurementsThisTile,
                                            testTile);
                                    cursorTestTile = testTile;
                                }
                            } else {
                                cursorMaidenheadReference.setHorizontalAlignment(SwingConstants.LEFT);
                                cursorMaidenheadReference.setText(" GRID SQ: ");
                            }
                        });
                    }
                }

                final boolean cursorHand = abstractMap.isPointInBounds(e.getPoint());

                if (cursorHand) {
                    abstractMap.setCursor(new Cursor(Cursor.HAND_CURSOR));
                } else {
                    if (isTestGridDrawOutlineMode()) {
                        abstractMap.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
                    } else {
                        abstractMap.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    }
                }
            }
        };

        mapPropertyChangeListener = (PropertyChangeEvent event) -> {
            if (MapEvent.PROPERTY_CHANGE.name().equals(event.getPropertyName())) {
                updateMapSettings();
            }
            if (MapEvent.MAP_PAINTED.name().equals(event.getPropertyName())) {
                updateStaticTestComponents();
                updateCoverageTestComponents();
                updateMapSettings();
                updateAPRSSettings();
                updateGPSSettings();
                setMapReady(true);
                configureKeyActions();
                createNormalGUI();
                displayFullGUI();
            }
            if (MapEvent.ZOOM_IN_DISABLED.name().equals(event.getPropertyName())) {
                disableZoomIn((boolean) event.getNewValue());
            }
            if (MapEvent.ZOOM_OUT_DISABLED.name().equals(event.getPropertyName())) {
                disableZoomOut((boolean) event.getNewValue());
            }
            if (MapEvent.VIEWING_ALTITUDE_CHANGE.name().equals(event.getPropertyName())) {
            	final long b = (long) abstractMap.getDisplayAltitude();
                viewingAltitude.setText(String.format(getLocale(), "%8d", b) + " FT");
            }
            if (MapEvent.MAP_PROVIDER_CHANGE.name().equals(event.getPropertyName())) {
                mapProviderChangeListenerEvent(event);
            }
            if (MapEvent.MEASURE_TOOL_SOLUTION.name().equals(event.getPropertyName())) {
                updateMeasureModeLabel((SurfaceLine) event.getNewValue());
            }
        };

        radioListener = event -> {
            if (TTYEvents.CTS.name().equals(event.getPropertyName())) {
                radioCTSHoldingChangeListenerEvent(event);
            }
            if (TTYEvents.DSR.name().equals(event.getPropertyName())) {
                radioDSRHoldingChangeListenerEvent(event);
            }
            if (TTYEvents.RLSD.name().equals(event.getPropertyName())) {
                radioCDHoldingChangeListenerEvent(event);
            }
            if (TTYEvents.RX_CHAR.name().equals(event.getPropertyName())) {
                // NO-OP
            }
            if (TTYEvents.RX_DATA.name().equals(event.getPropertyName())) {
                radioRxDataPropertyChangeListenerEvent(event);
            }
            if (TTYEvents.TX_DATA.name().equals(event.getPropertyName())) {
                radioTxDataPropertyChangeListenerEvent(event);
            }
            if (TTYEvents.ONLINE.name().equals(event.getPropertyName())) {
                radioOnlineChangeListenerEvent(event);
            }
            if (TTYEvents.INVALID_COMM_PORT.name().equals(event.getPropertyName())) {
                invalidComPortChangeListenerEvent(event, "abstractRadioReceiver");
            }
            if (TTYEvents.TTY_PORT_CONFIGURATION_ERROR.name().equals(event.getPropertyName())) {
                if (event.getNewValue().getClass().equals(Integer.class)) {
                    serialErrorChangeListenerEvent((int) event.getNewValue(), "Radio Port Configuration Error");
                } else if (event.getNewValue().getClass().equals(String.class)) {
                    serialErrorChangeListenerEvent(event.getSource().getClass().getName(),
                            "Radio Port Configuration Error");
                }
            }
            if (TTYEvents.ERROR.name().equals(event.getPropertyName())) {
                if (event.getNewValue().getClass().equals(Integer.class)) {
                    serialErrorChangeListenerEvent((int) event.getNewValue(), "Radio Error");
                } else if (event.getNewValue().getClass().equals(String.class)) {
                    serialErrorChangeListenerEvent(event.getSource().getClass().getName(), "Radio Error");
                }
            }
            if (ReceiverEvent.RSSI.equals(event.getPropertyName())) {
                radioRSSIChangeListenerEvent();
            }
            if (ReceiverEvent.BUSY.equals(event.getPropertyName())) {
                radioBusyChangeListenerEvent(event);
            }
            if (ReceiverEvent.POWER_STATE_CHANGE.equals(event.getPropertyName())) {
                radioPowerChangeListenerEvent(event);
            }
            if (ReceiverEvent.BER.equals(event.getPropertyName())) {
                radioBERChangeListenerEvent();
            }
            if (ReceiverEvent.ON_LINE.equals(event.getPropertyName())) {
                radioReadyChangeListenerEvent(event);
            }
            if (ScanEvent.SCAN_MEASUREMENT_READY.equals(event.getPropertyName())) {
                radioScanMeasurementReadyEvent();
            }
            if (ScanEvent.SCAN_ENABLE_CHANGE.equals(event.getPropertyName())) {
                radioScanEnableChangeListenerEvent(event);
            }
            if (ScanEvent.SCAN_LIST_MODIFIED.equals(event.getPropertyName())) {
                radioScanListChangeEvent();
            }
            if (ReceiverEvent.SINAD_CHANGE.equals(event.getPropertyName())) {
                radioSINADChangeListenerEvent();
            }
            if (ReceiverEvent.START_RADIO_WITH_SYSTEM.equals(event.getPropertyName())) {
                radioInterfaceReadyChangeListenerEvent(event);
            }
        };

        aprsListener = event -> {
            if (AbstractAPRSProcessor.Event.GPWPL_WAYPOINT_REPORT.name().equals(event.getPropertyName())) {
                waypointListenerEvent(event);
            }
            if (AprsProcessor.Event.APRS_SYMBOL_SIZE_UPDATED.name().equals(event.getPropertyName())) {
                abstractMap.redraw();
            }
            if (AbstractAPRSProcessor.Event.PKWDWPL_APRS_REPORT.name().equals(event.getPropertyName())) {
                waypointListenerEvent(event);
            }
            if (AbstractAPRSProcessor.Event.NMEA_DATA.name().equals(event.getPropertyName())) {
                aprsNMEAReceivedDataPropertyChangeListenerEvent(event);
            }
            if (AbstractAPRSProcessor.Event.CRC_ERROR.name().equals(event.getPropertyName())) {
                aprsCRCErrorEvent(event);
            }
            if (AbstractAPRSProcessor.Event.RX_DATA.name().equals(event.getPropertyName())) {
            	aprsRxDataPropertyChangeListenerEvent(event);
            }
            if (AbstractAPRSProcessor.Event.TX_DATA.name().equals(event.getPropertyName())) {
            	aprsTxDataPropertyChangeListenerEvent(event);
            }
            if (AbstractAPRSProcessor.Event.CONFIG_ERROR.name().equals(event.getPropertyName())) {
                if (event.getNewValue().getClass().equals(Integer.class)) {
                    serialErrorChangeListenerEvent((int) event.getNewValue(), "APRS Port Configuration Error");
                } else if (event.getNewValue().getClass().equals(String.class)) {
                    serialErrorChangeListenerEvent((String) event.getNewValue(), "APRS Port Configuration Error");
                }
            }
            if (AbstractAPRSProcessor.Event.ERROR.name().equals(event.getPropertyName())) {
                if (event.getNewValue().getClass().equals(Integer.class)) {
                    serialErrorChangeListenerEvent((int) event.getNewValue(), "APRS Error");
                } else if (event.getNewValue().getClass().equals(String.class)) {
                    serialErrorChangeListenerEvent((String) event.getNewValue(), "APRS Error");
                }
            }
            if (AbstractAPRSProcessor.Event.DSR.name().equals(event.getPropertyName())) {
                aprsDSRHoldingChangeListenerEvent(event);
            }
            if (AbstractAPRSProcessor.Event.CTS.name().equals(event.getPropertyName())) {
                aprsCTSHoldingChangeListenerEvent(event);
            }
            if (AbstractAPRSProcessor.Event.RLSD.name().equals(event.getPropertyName())) {
                aprsCDHoldingChangeListenerEvent(event);
            }
            if (AbstractAPRSProcessor.Event.INVALID_ADDRESS.name().equals(event.getPropertyName())) {
                invalidComPortChangeListenerEvent(event, "APRS");
            }
            if (AbstractAPRSProcessor.Event.ONLINE.name().equals(event.getPropertyName())) {
                aprsOnlineChangeListenerEvent(event);
            }
        };

        coverageTestObjectListener = event -> {
            if (CoverageTestObject.PROPERTY_CHANGE.equals(event.getPropertyName())) {
                updateCoverageTestComponents();
            }
            if (CoverageTestObject.TEST_GRID_SELECTION_MODE.equals(event.getPropertyName())) {
                setTestGridSelectionMode(event);
            }
            if (CoverageTestObject.TEST_GRID_LEARN_MODE.equals(event.getPropertyName())) {
                setTestGridLearnMode(event);
            }
            if (CoverageTestObject.TILE_SIZE_CHANGE.equals(event.getPropertyName())) {
                abstractMap.deleteTestGrid();
                abstractMap.deleteAllTestTiles();
                if (database != null) {
                    database.deleteAllTestTiles();
                }
            }
        };

        staticTestObject.getPropertyChangeSupport().addPropertyChangeListener(event -> {
            if (event.getPropertyName().equals(StaticTestObject.APPLY_SETTINGS)) {
                updateStaticTestComponents();
            }
        });
        
        databaseListener = event -> {
            LOG.log(Level.INFO, event.getPropertyName());
            if (Database.DATABASE_OPEN.equals(event.getPropertyName())) {
                databaseOpenEvent();
            }
            if (Database.DATABASE_CLOSED.equals(event.getPropertyName())) {
                databaseClosedEvent();
            }
            if (Database.MEASUREMENT_SET_RECORD_APPENDED.equals(event.getPropertyName())) {
                measurementSetTableAppendedEvent(event);
            }
            if (Database.TILE_ADDED_TO_TILE_TABLE.equals(event.getPropertyName())) {
                tileAddedToTileTableEvent(event);
            }
            if (Database.TILE_DELETED.equals(event.getPropertyName())) {
                tileDeletedEvent(event);
            }
            if (Database.MEASUREMENT_SET_RECORD_COUNT_READY.equals(event.getPropertyName())) {
                measurementSetRecordCountReadyEvent(event);
            }
            if (Database.MEASUREMENT_RECORD_COUNT_READY.equals(event.getPropertyName())) {
                measurementRecordCountReadyEvent(event);
            }
            if (Database.TILE_COMPLETE_RECORD_COUNT_READY.equals(event.getPropertyName())) {
                tileCompleteCountReadyEvent(event);
            }
            if (Database.TILE_NOT_ACCESSABLE_RECORD_COUNT_READY.equals(event.getPropertyName())) {
                tileNotAccessableCountReadyEvent(event);
            }
            if (Database.TILE_COUNT_READY.equals(event.getPropertyName())) {
                tileRecordCountReadyEvent(event);
            }
            if (Database.MEASUREMENT_SET_RECORD_READY.equals(event.getPropertyName())) {
                measurementSetRecordReadyEvent(event);
            }
            if (Database.MEASUREMENT_RECORD_READY.equals(event.getPropertyName())) {
                measurementRecordReadyEvent();
            }
            if (Database.TILE_RECORD_RESTORED.equals(event.getPropertyName())) {
                tileRecordRestoredEvent();
            }
            if (Database.STATIC_MEASUREMENT_RECORD_COUNT_READY.equals(event.getPropertyName())) {
                staticRecordCountReadyEvent(event);
            }
            if (Database.STATIC_MEASUREMENT_RECORD_APPENDED.equals(event.getPropertyName())) {
                staticTableAppendedEvent(event);
            }
            if (Database.FLIGHT_INFORMATION_RECORD_APPENDED.equals(event.getPropertyName())) {
                flightInformationTableAppendedEvent(event);
            }
            if (Database.STATIC_MEASUREMENT_RECORD_READY.equals(event.getPropertyName())) {
                staticMeasurementRecordReadyEvent();
            }
        };

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                abstractMap.setSize(mapPanel.getSize());
                abstractMap.redraw();
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                componentResized(e);
            }
        });

        zoomOutButton.addActionListener(event -> {
            if (event.getID() == ActionEvent.ACTION_PERFORMED) {
                zoomOutButtonMousePressed();
            }
        });

        zoomInButton.addActionListener(event -> {
            if (event.getID() == ActionEvent.ACTION_PERFORMED) {
                zoomInButtonMousePressed();
            }
        });

        doActionButton.addActionListener(event -> {
            if (event.getID() == ActionEvent.ACTION_PERFORMED) {
                doActionButtonMousePressed();
            }
        });

        centerOnGpsButton.addActionListener(event -> {
            if (event.getID() == ActionEvent.ACTION_PERFORMED) {
                setMapDragged(false);
                centerMapOnGpsPosition();
            }
        });

        mapFullScreenButton.addActionListener(event -> {
            if (event.getID() == ActionEvent.ACTION_PERFORMED) {
                setOperationMode(OperationMode.FULL);
            }
        });

        zoomOutButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(final MouseEvent event) {
                zoomOutButtonMouseReleased();
            }
        });

        zoomInButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(final MouseEvent event) {
                zoomInButtonMouseReleased();
            }
        });

        gpsButton.addActionListener(event -> {
            if (event.getID() == ActionEvent.ACTION_PERFORMED) {
                if (gpsButton.isSelected()) {
                    startGps();
                } else {
                    stopGps();
                }
            }
        });

        radioButton.addActionListener(event -> {
            if (event.getID() == ActionEvent.ACTION_PERFORMED) {
                if (radioButton.isSelected()) {
                    startRadio();
                } else {
                    stopRadio();
                }
            }
        });

        staticLocationAnalysisButton.addActionListener(event -> {
            if (event.getID() == ActionEvent.ACTION_PERFORMED) {
                if (staticLocationAnalysisButton.isSelected()) {
                    startStaticAnalysis();
                } else {
                    stopStaticAnalysis();
                }
            }
        });

        measureButton.addActionListener(this::measureButtonActionListenerEvent);

        aprsButton.addActionListener(event -> {
            if (event.getID() == ActionEvent.ACTION_PERFORMED) {
            	final JToggleButton btn = (JToggleButton) event.getSource();
                if (btn.isSelected()) {
                    startAPRS();
                } else {
                    stopAPRS();
                }
            }
        });

        learnModeButton.addActionListener(event -> {
            if (event.getID() == ActionEvent.ACTION_PERFORMED) {
            	final JToggleButton btn = (JToggleButton) event.getSource();
                if (btn.isSelected() && getDatabaseMode() == DatabaseMode.OPEN) {
                    setTestGridLearnMode(true);
                    setInhibitLeftMouseButtonReleaseEvent(true);
                } else {
                    setTestGridLearnMode(false);
                    setInhibitLeftMouseButtonReleaseEvent(false);
                }
            }
        });

        coverageTestButton.addActionListener(event -> {
            if (event.getID() == ActionEvent.ACTION_PERFORMED) {
                if (coverageTestButton.isSelected()) {
                    startCoverageTest();
                } else {
                    stopCoverageTest();
                }
            }
        });

        newDataFileButton.addActionListener(event -> {
            if (event.getID() == ActionEvent.ACTION_PERFORMED) {
                newDataFileMenuItem.doClick();
            }
        });

        openCoverageTestDataFileButton.addActionListener(event -> {
            if (event.getID() == ActionEvent.ACTION_PERFORMED) {
                openDataFileMenuItem.doClick();
            }
        });

        saveDataFileButton.addActionListener(event -> {
            if (event.getID() == ActionEvent.ACTION_PERFORMED) {
                saveDataFileMenuItem.doClick();
            }
        });

        closeDataFileButton.addActionListener(event -> {
            if (event.getID() == ActionEvent.ACTION_PERFORMED && closeDataFileButton.isSelected()) {
                closeDataFileMenuItem.doClick();
            }
        });

        stopDataFileButton.addActionListener(event -> {
            if (event.getID() == ActionEvent.ACTION_PERFORMED) {
                stopDataFileButton.setSelected(true);
                setDataMode(DataMode.STOP);
            }
        });

        recordDataFileButton.addActionListener(event -> {
            if (event.getID() == ActionEvent.ACTION_PERFORMED) {
                recordDataFileButton.setSelected(true);
                setDataMode(DataMode.RECORD);
            }
        });

        localEnvironmentMonitorMenuItem.addActionListener(_ -> environmentMonitorMenuItemActionListenerEvent());

        newDataFileMenuItem.addActionListener(_ -> createNewDatabase(null));

        openDataFileMenuItem.addActionListener(_ -> openDataFileMenuItemActionListenerEvent());

        closeDataFileMenuItem.addActionListener(_ -> closeDataFileMenuItemActionListenerEvent());

        saveAsDataFileMenuItem.addActionListener(this::saveAsDataFileMenuItemActionListenerEvent);

        saveDataFileMenuItem.addActionListener(this::saveDataFileMenuItemActionListenerEvent);

        printPreviewMenuItem.addActionListener(_ -> printPreviewMenuItemActionListenerEvent());

        printMenuItem.addActionListener(_ -> printMenuItemActionListenerEvent());

        exitMenuItem.addActionListener(_ -> exitMenuItemActionListenerEvent());

        aprsComponentMenuItem.addActionListener(_ -> aprsComponentMenuActionListenerEvent());

        nasaSpaceWeatherComponentMenuItem.addActionListener(_ -> nasaDonkiApiConfigComponentMenuActionListenerEvent());

        gpsComponentMenuItem.addActionListener(_ -> gpsComponentMenuActionListenerEvent());

        environmentMonitorComponentMenuItem.addActionListener(_ -> abstractAtmosphericMonitorComponentMenuActionListenerEvent());

        receiverComponentMenuItem.addActionListener(_ -> receiverComponentMenuActionListenerEvent());

        coverageTestSettingsMenuItem.addActionListener(_ -> coverageTestSettingsMenuActionListenerEvent());

        mapSettingsMenuItem.addActionListener(_ -> mapSettingsMenuActionListenerEvent());

        mapBulkDownloaderMenuItem.addActionListener(_ -> mapBulkDownloaderMenuActionListenerEvent());

        mapStatisticsMenuItem.addActionListener(_ -> mapStatisticsMenuActionListenerEvent());

        mapCacheViewerMenuItem.addActionListener(_ -> mapCacheViewerMenuActionListenerEvent());

        mapLayerSelectorMenuItem.addActionListener(_ -> mapLayerSelectorMenuActionListenerEvent());

        staticSignalLocationSettingsMenuItem.addActionListener(_ -> staticSignalLocationSettingsMenuActionListenerEvent());

        signalAnalysisMenuItem.addActionListener(_ -> signalAnalysisMenuActionListenerEvent());

        mapClearMenuItem.addActionListener(_ -> clearMapObjects());

        databaseConfigMenuItem.addActionListener(_ -> databseConfigMenuActionListenerEvent());

        aboutMenuItem.addActionListener(_ -> aboutMenuActionListenerEvent());

        cpuUsageMenuItem.addActionListener(_ -> cpuUsageMenuActionListenerEvent());

        final ActionListener mouseMovedTimerActionListener = _ -> mouseMovedTimerActionListenerEvent();

        final ActionListener variableTimerActionListener = _ -> variableTimerActionListenerEvent();

        final ActionListener fixedTimerActionListener = _ -> fixedTimerActionListenerEvent();

        final ActionListener zoomOutMouseDownTimerActionListener = _ -> zoomOutMouseDownTimerActionListenerEvent();

        final ActionListener zoomInMouseDownTimerActionListener = _ -> zoomInMouseDownTimerActionListenerEvent();

        final ActionListener gpsValidHeadingTimerActionListener = _ -> gpsValidHeadingTimerActionListenerEvent();

        coverageTestObject.getPropertyChangeSupport().addPropertyChangeListener(coverageTestObjectListener);

        variableTimer = new Timer(100, variableTimerActionListener);
        fixedTimer = new Timer(200, fixedTimerActionListener);
        mouseMovedTimer = new Timer(400, mouseMovedTimerActionListener);
        gpsValidHeadingTimer = new Timer(2000, gpsValidHeadingTimerActionListener);
        zoomOutMouseDownTimer = new Timer(300, zoomOutMouseDownTimerActionListener);
        zoomInMouseDownTimer = new Timer(300, zoomInMouseDownTimerActionListener);
        
        abstractMap.getPropertyChangeSupport().addPropertyChangeListener(mapPropertyChangeListener);

        abstractMap.initialize();
    }
    
    private void initializeEnvironmentSensorListeners() {
        environmentSensorListener = event -> {
            if (AbstractEnvironmentSensor.Events.CLASS_NAME_CHANGE.name().equals(event.getPropertyName())) {
                environmentSensorClassName = (String) event.getNewValue();
            }
        };
    }

    private void initializeGpsListener() {
        gpsListener = event -> {
        	if (AbstractGpsProcessor.CLASS_NAME_CHANGE.equals(event.getPropertyName())) {
                gpsClassName = (String) event.getNewValue(); 
                stopGps();
                gpsProcessor.getGPSPropertyChangeSupport().removePropertyChangeListener(gpsListener);
                gpsProcessor.close();
                gpsProcessor = AbstractGpsProcessor.getReceiverInstance(gpsClassName, false);
                gpsProcessor.getGPSPropertyChangeSupport().addPropertyChangeListener(gpsListener);
                LOG.log(Level.INFO, "AbstractGpsProcessor user directed change to class name: {0}", gpsClassName);
            }
            if (AbstractGpsProcessor.RADIUS_UPDATED.equals(event.getPropertyName())) {
                abstractMap.redraw();
            }
            if (AbstractGpsProcessor.GPWPL_WAYPOINT_REPORT.equals(event.getPropertyName())) {
                waypointListenerEvent(event);
            }
            if (AbstractGpsProcessor.PKWDWPL_APRS_REPORT.equals(event.getPropertyName())) {
                waypointListenerEvent(event);
            }
            if (AbstractGpsProcessor.VALID_POSITION.equals(event.getPropertyName())) {
                gpsValidPositionPropertyChangeListenerEvent(event);
            }
            if (AbstractGpsProcessor.VALID_ALTITUDE_METERS.equals(event.getPropertyName())) {
                gpsValidAltitudeMetersPropertyChangeListenerEvent(event);
            }
            if (AbstractGpsProcessor.VALID_TIME.equals(event.getPropertyName())) {
                gpsValidTimePropertyChangeListenerEvent(event);
            }
            if (AbstractGpsProcessor.TIME_ZONE_ID.equals(event.getPropertyName())) {
                gpsTimeZoneIdPropertyChangeListenerEvent(event);
            }
            if (AbstractGpsProcessor.RDF_HEADING_TRUE.equals(event.getPropertyName())) {
                rdfHeadingTruePropertyChangeListenerEvent(event);
            }
            if (AbstractGpsProcessor.FIX_QUALITY.equals(event.getPropertyName())) {
                gpsFixQualityPropertyChangeListenerEvent(event);
            }
            if (AbstractGpsProcessor.COURSE_MADE_GOOD_TRUE.equals(event.getPropertyName())) {
                gpsCourseMadeGoodTruePropertyChangeListenerEvent();
            }
            if (AbstractGpsProcessor.RX_DATA.equals(event.getPropertyName())) {
                gpsReceivedDataPropertyChangeListenerEvent(event);
            }
            if (AbstractGpsProcessor.CRC_ERROR.equals(event.getPropertyName())) {
                gpsCRCErrorEvent(event);
            }
            if (AbstractGpsProcessor.TX_DATA.equals(event.getPropertyName())) {
                gpsTxDataPropertyChangeListenerEvent(event);
            }
            if (AbstractGpsProcessor.CONFIG_ERROR.equals(event.getPropertyName())) {
                if (event.getNewValue().getClass().equals(Integer.class)) {
                    serialErrorChangeListenerEvent((int) event.getNewValue(), "GPS Port Configuration Error");
                } else if (event.getNewValue().getClass().equals(String.class)) {
                    serialErrorChangeListenerEvent((String) event.getNewValue(), "GPS Port Configuration Error");
                }
            }
            if (AbstractGpsProcessor.ERROR.equals(event.getPropertyName())) {
                if (event.getNewValue().getClass().equals(Integer.class)) {
                    serialErrorChangeListenerEvent((int) event.getNewValue(), "GPS Error");
                } else if (event.getNewValue().getClass().equals(String.class)) {
                    serialErrorChangeListenerEvent((String) event.getNewValue(), "GPS Error");
                }
            }
            if (AbstractGpsProcessor.DSR.equals(event.getPropertyName())) {
                gpsDSRHoldingPropertyChangeListenerEvent(event);
            }
            if (AbstractGpsProcessor.CTS.equals(event.getPropertyName())) {
                gpsCTSHoldingPropertyChangeListenerEvent(event);
            }
            if (AbstractGpsProcessor.RLSD.equals(event.getPropertyName())) {
                gpsCDHoldingPropertyChangeListenerEvent(event);
            }
            if (AbstractGpsProcessor.INVALID_ADDRESS.equals(event.getPropertyName())) {
                invalidComPortChangeListenerEvent(event, "GPS");
            }
            if (AbstractGpsProcessor.ONLINE.equals(event.getPropertyName())) {
                gpsOnlineChangeListenerEvent(event);
            }
        };
    }

    private void initializeTimekeepingListeners() {
        consolidatedTime.addPropertyChangeListener(event -> {
            if (ConsolidatedTime.CLOCK.equals(event.getPropertyName())) {
                networkClockUpdate(event);
            }
            if (ConsolidatedTime.FAIL.equals(event.getPropertyName())) {
                networkClockFailure();
            }
        });

    }

    private final class SignalFlash {
        private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	    private SignalFlash(JLabel label, Color onColor, Color offColor) {
            label.setBackground(onColor);
            scheduler.schedule(() -> {
            	label.setBackground(offColor); 
            	scheduler.shutdown();
            }, 250, TimeUnit.MILLISECONDS);
	    }
    }
    
    private class VariableMeasurementTimerAction implements Runnable {

        @Override
        public void run() {
        	final Point2D position;
            if (getPositionSource() == PositionSource.MANUAL) {
                if (!Coordinate.validLongitude(abstractMap.getMouseCoordinates().getY())) {
                    return;
                }
                position = abstractMap.getMouseCoordinates();
            } else {
                position = gpsProcessor.getPosition();
            }
            executor.execute(new MeasurementProcessor(TimingMode.VARIABLE, getPositionSource(), position,
                    getAssignedTestTile(position)));
        }
    }

    private class FixedMeasurementTimerAction implements Runnable {

        @Override
        public void run() {
        	final Point2D position;
            if (getPositionSource() == PositionSource.MANUAL) {
                if (!Coordinate.validLongitude(abstractMap.getMouseCoordinates().getY())) {
                    return;
                }
                position = abstractMap.getMouseCoordinates();
            } else {
                position = gpsProcessor.getPosition();
            }
            executor.execute(new MeasurementProcessor(TimingMode.FIXED, getPositionSource(), position,
                    getAssignedTestTile(position)));
        }
    }

    private PositionSource getPositionSource() {
        if (coverageTestObject.getManualDataCollectionMode() == ManualMode.MOUSE_KEYPRESS) {
            return PositionSource.MANUAL;
        } else {
            return PositionSource.GPS;
        }
    }

    private void initializeLookAndFeel() {
    	final String operatingSystem = System.getProperty("os.name").toLowerCase(getLocale());
        LOG.log(Level.INFO, "Operating System Name: {0}", operatingSystem);
        System.setProperty("java.net.useSystemProxies", "true");

        if (operatingSystem.contains("mac")) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "NASA World Wind");
            System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
            System.setProperty("apple.awt.brushMetalLook", "true");
        } else if (operatingSystem.contains("win")) {

            for (final UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    try {
                        UIManager.setLookAndFeel(info.getClassName());
                    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                            | UnsupportedLookAndFeelException ex) {
                        LOG.log(Level.WARNING, ex.getMessage(), ex);
                    }
                    break;
                }
            }

            System.setProperty("sun.awt.noerasebackground", "true");
            setDefaultLookAndFeelDecorated(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

        } else if (operatingSystem.contains("nix") || operatingSystem.contains("nux")
                || operatingSystem.contains("aix")) {
            for (final UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    try {
                        UIManager.setLookAndFeel(info.getClassName());
                    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                            | UnsupportedLookAndFeelException ex) {
                        LOG.log(Level.WARNING, ex.getMessage(), ex);
                    }
                    break;
                }
            }
        }
        LOG.log(Level.INFO, "LookAndFeel Set To: {0}", UIManager.getSystemLookAndFeelClassName());
    }

    private void aboutMenuActionListenerEvent() {
        invokeLaterInDispatchThreadIfNeeded(
                () -> JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(SignalTrack.this),
                        "SignalTrack version 1.9.3.20250519" + System.lineSeparator()
                        + "(c) Copyright John R. Chartkoff, 2025.  All rights reserved.",
                        "About SignalTrack", JOptionPane.INFORMATION_MESSAGE));
    }

    private void cpuUsageMenuActionListenerEvent() {
        if (cpuUsageCollector == null) {
            cpuUsageCollector = new CPUUsageCollector();
        }
    }

    private int addRdfBearing(final Point2D p1, final double bearing, final double length, final RdfQuality quality,
            final Color color) {
        final int index = bearingList.size();
        bearingList.add(index, new Bearing(index, p1, bearing, length, getRdfQuality(quality), color));

        final Point2D p2 = new Point2D.Double((Math.sin((bearing * Math.PI) / 180) * length) + p1.getX(),
                (Math.cos((bearing * Math.PI) / 180) * length) + p1.getY());

        abstractMap.addLine(p1, p2, color);

        return index;
    }

    private void allTileRecordsReady() {
        final List<TestTile> tileList = database.getTileRecordList();
        if (tileList != null) {
            tileList.stream().map(testTile -> {
                testTile.setColor(getTestTileColor(testTile.getMeasurementCount()));
                abstractMap.addTestTile(testTile);
                return testTile;
            }).forEachOrdered(this::updateTestTileStats);
            invokeLaterInDispatchThreadIfNeeded(() -> totalTiles
                    .setText(String.format(getLocale(), "%07d", database.getTileRecordList().size())));
        }
    }

    private void aprsCDHoldingChangeListenerEvent(final PropertyChangeEvent event) {
        invokeLaterInDispatchThreadIfNeeded(() -> {
            if ((boolean) event.getNewValue()) {
                aprsCD.setBackground(ST_BRIGHT_RED);
            } else {
                aprsCD.setBackground(ST_DARK_GRAY);
            }
            repaint();
        });
    }

    private void aprsCTSHoldingChangeListenerEvent(final PropertyChangeEvent event) {
        invokeLaterInDispatchThreadIfNeeded(() -> {
            if ((boolean) event.getNewValue()) {
                aprsCTS.setBackground(ST_BRIGHT_RED);
            } else {
                aprsCTS.setBackground(ST_DARK_GRAY);
            }
            repaint();
        });
    }

    private void aprsDSRHoldingChangeListenerEvent(final PropertyChangeEvent event) {
        invokeLaterInDispatchThreadIfNeeded(() -> {
            if ((boolean) event.getNewValue()) {
                aprsDSR.setBackground(ST_BRIGHT_RED);
            } else {
                aprsDSR.setBackground(ST_DARK_GRAY);
            }
            repaint();
        });
    }

    private void aprsRxDataPropertyChangeListenerEvent(PropertyChangeEvent event) {
        new SignalFlash(aprsRxData, ST_BRIGHT_GREEN, ST_BRIGHT_RED);
        dynamicToolTipUpdate(aprsRxData, (String) event.getNewValue());
    }

    private void aprsNMEAReceivedDataPropertyChangeListenerEvent(PropertyChangeEvent event) {
        new SignalFlash(aprsRxData, ST_BRIGHT_GREEN, ST_BRIGHT_RED);
        dynamicToolTipUpdate(aprsRxData, (String) event.getNewValue());
    }
    
    private void aprsTxDataPropertyChangeListenerEvent(PropertyChangeEvent event) {
        new SignalFlash(aprsTxData, ST_BRIGHT_GREEN, ST_BRIGHT_RED);
        dynamicToolTipUpdate(aprsTxData, (String) event.getNewValue());
    }

    private void gpsTxDataPropertyChangeListenerEvent(PropertyChangeEvent event) {
        new SignalFlash(gpsTxData, ST_BRIGHT_GREEN, ST_BRIGHT_RED);
        dynamicToolTipUpdate(gpsTxData, (String) event.getNewValue());
    }
 
    private void gpsReceivedDataPropertyChangeListenerEvent(PropertyChangeEvent event) {
    	new SignalFlash(gpsRxData, ST_BRIGHT_GREEN, ST_BRIGHT_RED);
        dynamicToolTipUpdate(gpsRxData, (String) event.getNewValue());
    }

    private void radioRxDataPropertyChangeListenerEvent(PropertyChangeEvent event) {
        new SignalFlash(radioRxData, ST_BRIGHT_GREEN, ST_BRIGHT_RED);
        dynamicToolTipUpdate(radioRxData, (String) event.getNewValue());
    }

    private void radioTxDataPropertyChangeListenerEvent(PropertyChangeEvent event) {
        new SignalFlash(radioTxData, ST_BRIGHT_GREEN, ST_BRIGHT_RED);
        dynamicToolTipUpdate(radioTxData, (String) event.getNewValue());
    }
    
    private void waypointListenerEvent(PropertyChangeEvent event) {
    	final AprsIcon aprsIcon = (AprsIcon) event.getNewValue();
        invokeLaterInDispatchThreadIfNeeded(() -> {
            // Check if APRS report is currently in the list and if it is then move it.
            // If moveIcon returns false, then create a new icon.
        	final String iconName = aprsIcon.getCallSign() + "-" + aprsIcon.getSsid();
        	final long timeToDelete = (getAprsProcessor().getTimeToLiveMinutes() * 60000L) + System.currentTimeMillis();
        	final long timeToGoStale = (getAprsProcessor().getTimeToGoStaleMinutes() * 60000L) + System.currentTimeMillis();
            if (!abstractMap.moveIcon(iconName, aprsIcon.getLonLat(), timeToDelete, timeToGoStale)) {
                abstractMap.addIcon(aprsIcon.getLonLat(), aprsIcon.getImage(), iconName, new Dimension(16, 16),
                        timeToDelete, timeToGoStale);
            }
        });
    }

    private boolean checkMapRecenter(final Point2D point) {
        try {
        	final double longitudeThreshold = Math
                    .abs((abstractMap.getUpperLeftLonLat().getX() - abstractMap.getLowerRightLonLat().getX()) / 4D);
        	final double latitudeThreshold = Math
                    .abs((abstractMap.getUpperLeftLonLat().getY() - abstractMap.getLowerRightLonLat().getY()) / 4D);
            return (((Math.abs(abstractMap.getCenterLonLat().getX() - point.getX()) > longitudeThreshold)
                    || (Math.abs(abstractMap.getCenterLonLat().getY() - point.getY()) > latitudeThreshold))
                    && !isMapDragged());
        } catch (NullPointerException ex) {
            LOG.log(Level.WARNING, ex.getMessage(), ex);
            return false;
        }
    }

    private AbstractRadioReceiver getAbstractRadio() {
        return abstractRadioReceiver;
    }

    private AprsProcessor getAprsProcessor() {
        return aprsProcessor;
    }

    private void clearAllPreferences() {
        try {
            userPref.clear();
        } catch (final BackingStoreException ex) {
            LOG.log(Level.WARNING, ex.getMessage(), ex);
        }
    }

    private void closeDataFileMenuItemActionListenerEvent() {
        if ((getDatabaseMode() != DatabaseMode.CLOSED)) {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            database.close();
            setDataMode(DataMode.STOP);
            setDatabaseMode(DatabaseMode.CLOSED);
        }
    }

    private void configureDesktopComponents() {
        mapPanel = getMapPanel();
        gpsInfoPanel = getGpsInfoPanel();
        infoPanel = getInfoPanel();
        commPanel = getCommPanel();

        createToolBar();

        toolBar.setRollover(true);
        toolBar.setFloatable(false);
        toolBar.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED, Color.LIGHT_GRAY, Color.GRAY));

        gpsCompassRose.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(EtchedBorder.LOWERED, Color.LIGHT_GRAY, Color.GRAY),
                "True Course Made Good", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                new Font("Calabri", Font.BOLD, 9)));
        gpsCompassRose.setSelectColor(Color.GRAY);
        gpsCompassRose.setOpaque(false);
        gpsCompassRose.setDoubleBuffered(true);

        signalMeter.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(EtchedBorder.LOWERED, Color.LIGHT_GRAY, Color.GRAY), "Signal Strength",
                TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Calabri", Font.BOLD, 9)));
        signalMeter.setOpaque(false);
        signalMeter.setDoubleBuffered(true);
        signalMeter.setMeterColor(Color.BLUE);
        signalMeter.setEnabled(false);

        setDataMode(DataMode.STOP);
        setDatabaseMode(DatabaseMode.CLOSED);

        setMouseOffGlobe(false);
    }

    private void coverageTestSettingsMenuActionListenerEvent() {
        if (coverageTestComponent == null) {
            coverageTestComponent = new CoverageTestComponent(coverageTestObject);
        } else {
            coverageTestComponent.toFront();
            coverageTestComponent.setEnabled(true);
            coverageTestComponent.setVisible(true);
        }
    }

    private synchronized void createBlankTestTile(Point2D anyLonLatWithinTargetTile) {
        invokeLaterInDispatchThreadIfNeeded(() -> abstractMap.setCursor(new Cursor(Cursor.WAIT_CURSOR)));
        final Point2D tileSize = coverageTestObject.getTileSizeDegrees();
        final Point2D gridRef = coverageTestObject.getGridReference();
        final Point2D gridSize = coverageTestObject.getGridSize();
        final Point2D p2d = CoordinateUtils.lonLatFromAnywhereInTileToNorthWestCornerOfTile(anyLonLatWithinTargetTile, tileSize, gridRef, gridSize);
        if (p2d != null) {
            createBlankTestTile(new Coordinate(p2d));
        } else {
            invokeLaterInDispatchThreadIfNeeded(() -> abstractMap.setCursor(new Cursor(Cursor.DEFAULT_CURSOR)));
        }
    }

    private synchronized void createBlankTestTile(Coordinate northWestCoordinate) {
        if (isTestTileSelectMode()) {
            invokeLaterInDispatchThreadIfNeeded(() -> abstractMap.setCursor(new Cursor(Cursor.WAIT_CURSOR)));
            if (database == null) {
                invokeLaterInDispatchThreadIfNeeded(() -> {
                    abstractMap.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(SignalTrack.this),
                            "Please create or open the data file for this coverage test", "SQL Database Error",
                            JOptionPane.ERROR_MESSAGE);
                });
            } else {
                final Point2D tileSizeDegrees = coverageTestObject.getTileSizeDegrees();
                final Precision requiredPrecision = getRequiredPrecision(tileSizeDegrees);
                final TestTile newTile = database.getTestTileWithThisNorthWestLonLat(northWestCoordinate.getLonLat());
                if (newTile != null) {
                    database.deleteTestTile(newTile);
                } else {
                    database.appendTileRecordWithThisNorthWestCoordinateReference(new TestTile(testName.replace(".sql", ""),
                            northWestCoordinate.getLonLat(), tileSizeDegrees, requiredPrecision));                    
                }
            }
        }
    }

    private void createNewDatabase(final File file) {
        stopAllProcesses();
        setDataMode(DataMode.STOP);
        abstractMap.deleteAllTestTiles();
        abstractMap.deleteTestGrid();
        newDatabaseMonitor = new ProgressDialog(true, "", "Building New SQL Database", 0, 100, true, true);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        database = new Database(databaseConfig);
        database.addPropertyChangeListener(databaseListener);
        markerCounter = 0;
        invokeLaterInDispatchThreadIfNeeded(() -> {
            abstractMap.deleteAllQuads();
            abstractMap.deleteAllArcIntersectPoints();
            abstractMap.deleteAllArcs();
            abstractMap.deleteAllIcons();
            abstractMap.deleteAllLines();
            abstractMap.deleteAllRings();
            abstractMap.deleteAllTestTiles();
            abstractMap.deleteAllSignalMarkers();
            abstractMap.deleteTestGrid();
            tilesCompleted.setText(String.format(getLocale(), "%07d", tilesComplete));
            recordCountLabel.setText(String.format(getLocale(), "%08d", 0));
            markerID.setText(String.format(getLocale(), "%5d", markerCounter));
        });
        if (file == null) {
            testName = DatabaseConfig.getNewDatabaseName();
            coverageTestObject.getPropertyChangeSupport().removePropertyChangeListener(coverageTestObjectListener);
            coverageTestObject = new CoverageTestObject(isClearAllPrefs(), getAbstractRadio(), testName);
            coverageTestObject.getPropertyChangeSupport().addPropertyChangeListener(coverageTestObjectListener);
            database.openDatabase(new File(dataDirectory.getPath() + File.separator + testName), coverageTestObject, staticTestObject);
            logFileNameLabel.setText(testName);
        } else {
            database.openDatabase(file, coverageTestObject, staticTestObject);
            logFileNameLabel.setText(testName);
        }
    }

    private void createToolBar() {
        try {
            zoomOutButton.setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/112_Minus_Green_16x16_72.png"))));
            zoomOutButton.setFocusable(false);
            zoomOutButton.setRolloverEnabled(true);
            zoomOutButton.setMultiClickThreshhold(50L);
            zoomOutButton.setToolTipText("Zoom Out");

            zoomInButton.setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/112_Plus_Green_16x16_72.png"))));
            zoomInButton.setFocusable(false);
            zoomInButton.setRolloverEnabled(true);
            zoomInButton.setMultiClickThreshhold(50L);
            zoomInButton.setToolTipText("Zoom In");

            gpsButton.setIcon(new ImageIcon(getClass().getResource("/Web.png")));
            gpsButton.setFocusable(false);
            gpsButton.setRolloverEnabled(true);
            gpsButton.setMultiClickThreshhold(50L);
            gpsButton.setToolTipText("Enable GPS Receiver");

            centerOnGpsButton.setIcon(new ImageIcon(getClass().getResource("/recenterOnLocationButtonImage.jpg")));
            centerOnGpsButton.setEnabled(false);
            centerOnGpsButton.setFocusable(false);
            centerOnGpsButton.setRolloverEnabled(true);
            centerOnGpsButton.setMultiClickThreshhold(50L);
            centerOnGpsButton.setToolTipText("Center Map on GPS Location");

            mapFullScreenButton.setIcon(new ImageIcon(getClass().getResource("/all-corners-image.gif")));
            mapFullScreenButton.setRolloverEnabled(true);
            mapFullScreenButton.setFocusable(false);
            mapFullScreenButton.setMultiClickThreshhold(50L);
            mapFullScreenButton.setToolTipText("Set Map to Fill the Screen");

            radioButton.setText("RADIO");
            radioButton.setPreferredSize(DEFAULT_BUTTON_DIM);
            radioButton.setFont(new Font(DEFAULT_CALIBRI_FONT_NAME, Font.BOLD, 12));
            radioButton.setForeground(Color.BLUE);
            radioButton.setRolloverEnabled(true);
            radioButton.setFocusable(false);
            radioButton.setMultiClickThreshhold(50L);
            radioButton.setToolTipText("Enable Radio System");

            measureButton.setIcon(new ImageIcon(getClass().getResource("/ruler-image.gif")));
            measureButton.setRolloverEnabled(true);
            measureButton.setFocusable(false);
            measureButton.setMultiClickThreshhold(50L);
            measureButton.setToolTipText("Measurement Ruler");

            aprsButton.setText("APRS");
            aprsButton.setPreferredSize(DEFAULT_BUTTON_DIM);
            aprsButton.setFont(new Font(DEFAULT_CALIBRI_FONT_NAME, Font.BOLD, 12));
            aprsButton.setForeground(Color.BLUE);
            aprsButton.setRolloverEnabled(true);
            aprsButton.setFocusable(false);
            aprsButton.setMultiClickThreshhold(50L);
            aprsButton.setToolTipText("Enable APRS Position Reporting");

            learnModeButton.setText("LEARN");
            learnModeButton.setPreferredSize(DEFAULT_BUTTON_DIM);
            learnModeButton.setFont(new Font(DEFAULT_CALIBRI_FONT_NAME, Font.BOLD, 12));
            learnModeButton.setForeground(ST_BRIGHT_RED);
            learnModeButton.setRolloverEnabled(true);
            learnModeButton.setFocusable(false);
            learnModeButton.setMultiClickThreshhold(50L);
            learnModeButton.setToolTipText("Learn Test Tiles to Search");

            messageLabel.setFont(new Font(DEFAULT_CALIBRI_FONT_NAME, Font.BOLD, 12));
            messageLabel.setForeground(ST_BRIGHT_RED);

            doActionButton.setFont(new Font(DEFAULT_CALIBRI_FONT_NAME, Font.BOLD, 12));
            doActionButton.setForeground(new Color(200, 0, 0));
            doActionButton.setFocusable(false);
            doActionButton.setRolloverEnabled(true);
            doActionButton.setMultiClickThreshhold(50L);
            doActionButton.setVisible(false);

            coverageTestButton.setText("DRIVE");
            coverageTestButton.setPreferredSize(DEFAULT_BUTTON_DIM);
            coverageTestButton.setFont(new Font(DEFAULT_CALIBRI_FONT_NAME, Font.BOLD, 12));
            coverageTestButton.setForeground(ST_BRIGHT_RED);
            coverageTestButton.setRolloverEnabled(true);
            coverageTestButton.setFocusable(false);
            coverageTestButton.setMultiClickThreshhold(50L);
            coverageTestButton.setToolTipText("Initialize Coverage Test");

            staticLocationAnalysisButton.setText("RDF-S");
            staticLocationAnalysisButton.setPreferredSize(DEFAULT_BUTTON_DIM);
            staticLocationAnalysisButton.setFont(new Font(DEFAULT_CALIBRI_FONT_NAME, Font.BOLD, 12));
            staticLocationAnalysisButton.setForeground(ST_BRIGHT_RED);
            staticLocationAnalysisButton.setRolloverEnabled(true);
            staticLocationAnalysisButton.setFocusable(false);
            staticLocationAnalysisButton.setMultiClickThreshhold(50L);
            staticLocationAnalysisButton.setToolTipText("Initialize Static Signal Location Search");

            newDataFileButton.setIcon(new ImageIcon(getClass().getResource("/NewDocumentHS.png")));
            newDataFileButton.setFocusable(false);
            newDataFileButton.setRolloverEnabled(true);
            newDataFileButton.setMultiClickThreshhold(50L);
            newDataFileButton.setToolTipText("Create New Data File");

            openCoverageTestDataFileButton.setIcon(new ImageIcon(getClass().getResource("/openHS.png")));
            openCoverageTestDataFileButton.setFocusable(false);
            openCoverageTestDataFileButton.setRolloverEnabled(true);
            openCoverageTestDataFileButton.setMultiClickThreshhold(50L);
            openCoverageTestDataFileButton.setToolTipText("Open Coverage Test Data File");

            saveDataFileButton.setIcon(new ImageIcon(getClass().getResource("/saveHS.png")));
            saveDataFileButton.setFocusable(false);
            saveDataFileButton.setRolloverEnabled(true);
            saveDataFileButton.setMultiClickThreshhold(50L);
            saveDataFileButton.setToolTipText("Save Data File");

            closeDataFileButton.setIcon(new ImageIcon(getClass().getResource("/closefile.gif")));
            closeDataFileButton.setFocusable(false);
            closeDataFileButton.setRolloverEnabled(true);
            closeDataFileButton.setMultiClickThreshhold(50L);
            closeDataFileButton.setToolTipText("Close Data File");

            stopDataFileButton.setIcon(new ImageIcon(getClass().getResource("/StopHS.png")));
            stopDataFileButton.setFocusable(false);
            stopDataFileButton.setRolloverEnabled(true);
            stopDataFileButton.setMultiClickThreshhold(50L);
            stopDataFileButton.setToolTipText("Stop Data File");

            recordDataFileButton.setIcon(new ImageIcon(getClass().getResource("/RecordHS.png")));
            recordDataFileButton.setFocusable(false);
            recordDataFileButton.setRolloverEnabled(true);
            recordDataFileButton.setMultiClickThreshhold(50L);
            recordDataFileButton.setToolTipText("Record Data File");

            new IconLoader(this, getClass().getResourceAsStream("/route_icon.jpg"));

            newDataFileMenuItem.setIcon(new ImageIcon(getClass().getResource("/NewDocumentHS.png")));

            toolBar.add(newDataFileButton);
            toolBar.add(openCoverageTestDataFileButton);
            toolBar.add(saveDataFileButton);
            toolBar.add(closeDataFileButton);
            toolBar.addSeparator();
            toolBar.add(stopDataFileButton);
            toolBar.add(recordDataFileButton);
            toolBar.addSeparator();
            toolBar.add(gpsButton);
            toolBar.add(centerOnGpsButton);
            toolBar.add(radioButton);
            toolBar.add(aprsButton);
            toolBar.addSeparator();
            toolBar.add(coverageTestButton);
            toolBar.add(staticLocationAnalysisButton);
            toolBar.add(learnModeButton);
            toolBar.addSeparator();
            toolBar.add(zoomInButton);
            toolBar.add(zoomOutButton);
            toolBar.addSeparator();
            toolBar.add(mapFullScreenButton);
            toolBar.add(measureButton);
            toolBar.addSeparator();
            toolBar.add(messageLabel);
            toolBar.addSeparator();
            toolBar.add(doActionButton);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    private void databaseClosedEvent() {
        database.removePropertyChangeListener(databaseListener);
        invokeLaterInDispatchThreadIfNeeded(() -> setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)));
        abstractMap.clearMapObjects();
        setDataMode(DataMode.STOP);
        setDatabaseMode(DatabaseMode.CLOSED);
    }

    private void databaseOpenEvent() {
        setDataMode(DataMode.STOP);
        setDatabaseMode(DatabaseMode.OPEN);
        database.requestTileStatistics(coverageTestObject);
        if (newDatabaseMonitor != null) {
            newDatabaseMonitor.dispose();
        }
        if (isCoverageTestActive() && (database.getTileRecordList().isEmpty())) {
            invokeLaterInDispatchThreadIfNeeded(() -> {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(SignalTrack.this),
                        "No tiles have been selected for testing.", "Invalid Selection", JOptionPane.ERROR_MESSAGE);
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            });
            stopCoverageTest();
        }
    }

    private void databseConfigMenuActionListenerEvent() {
        new DatabaseConfigComponent(databaseConfig);
    }

    private void disableZoomIn(boolean disabled) {
        invokeLaterInDispatchThreadIfNeeded(() -> zoomInButton.setEnabled(!disabled));
    }

    private void disableZoomOut(boolean disabled) {
        invokeLaterInDispatchThreadIfNeeded(() -> zoomOutButton.setEnabled(!disabled));
    }

    private void displayFullGUI() {
        final var tk = Toolkit.getDefaultToolkit();
        final Dimension screenSize = tk.getScreenSize();
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        pack();
        setLocation((screenSize.width / 2) - (getWidth() / 2), (screenSize.height / 2) - (getHeight() / 2));
        setTitle("SignalTrack Radio Network Coverage Analysis System");
        setVisible(true);
        updateWindowSizeSpecificComponents();
    }

    private void doZoomIn() {
        abstractMap.zoomIn();
    }

    private void doZoomOut() {
        abstractMap.zoomOut();
    }

    private void exitMenuItemActionListenerEvent() {
        dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }

    private void close() {
    	userPref.put("StartRadioCalFileName", startRadioCalFileName);
        userPref.put("TestName", testName.replace(".sql", ""));
        userPref.put("GPSClassName", gpsClassName);
        userPref.put("SignalTrackMapName", signalTrackMapName.name());
        userPref.put("EnvironmentMonitorClassName", environmentSensorClassName);
        
        if (aprsScheduler != null) {
			try {
				LOG.log(Level.INFO, "Initializing SignalTrack.tt4Scheduler termination....");
				aprsScheduler.shutdown();
				aprsScheduler.awaitTermination(5, TimeUnit.SECONDS);
				LOG.log(Level.INFO, "SignalTrack.tt4Scheduler has gracefully terminated");
			} catch (InterruptedException e) {
				aprsScheduler.shutdownNow();
				LOG.log(Level.SEVERE, "SignalTrack.tt4Scheduler has timed out after 5 seconds of waiting to terminate processes.");
				Thread.currentThread().interrupt();
			}
        }
        
        if (staticScheduler != null) {
			try {
				LOG.log(Level.INFO, "Initializing SignalTrack.staticScheduler termination....");
				staticScheduler.shutdown();
				staticScheduler.awaitTermination(5, TimeUnit.SECONDS);
				LOG.log(Level.INFO, "SignalTrack.staticScheduler has gracefully terminated");
			} catch (InterruptedException e) {
				staticScheduler.shutdownNow();
				LOG.log(Level.SEVERE, "SignalTrack.staticScheduler has timed out after 5 seconds of waiting to terminate processes.");
				Thread.currentThread().interrupt();
			}
        }
        
        if (abstractMap != null) {
            final Point2D point = abstractMap.getCenterLonLat();
            if ((point != null) && (point.getX() >= -180) && (point.getX() <= 180)) {
                userPref.putDouble("MapLongitude", point.getX());
            }
            if ((point != null) && (point.getY() >= -90) && (point.getY() <= 90)) {
                userPref.putDouble("MapLatitude", point.getY());
            }
            userPref.putDouble("MapScale", abstractMap.getScale());
            userPref.putLong("MapAltitude", abstractMap.getAltitude());
            clearMapObjects();
            try {
				abstractMap.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
        }

        if (database != null) {
        	userPref.put("TerminalDatabaseMode", databaseMode.name());
            userPref.put("DatabaseFile", database.getConfig().getDatabaseFile().getPath());
            database.close();
        }

        if (gpsProcessor != null) {
            gpsProcessor.close();
        }

        if (environmentSensor != null) {
            environmentSensor.removePropertyChangeListener(environmentSensorListener);
            environmentSensor.close();
        }

        if (aprsProcessor != null) {
        	userPref.put("TNCClassName", String.valueOf(aprsProcessor.getAPRSTNCClient().getClassName()));
            aprsProcessor.close();
        }

        if (abstractRadioReceiver != null) {
            abstractRadioReceiver.close();
        }

        if (bgts != null) {
            bgts.close();
        }

        if (radDetector != null) {
            radDetector.close();
        }
        
        if (dts != null) {
            dts.close();
        }

        if (swp != null) {
            swp.close();
        }

        if (airNow != null) {
            airNow.close();
        }

        if (ners != null) {
            ners.close();
        }

        for (Handler handler : LOG.getHandlers()) {
            LOG.removeHandler(handler);
            handler.close();
        }

        ThreadUtils.getAllThreads().forEach(t -> LOG.log(Level.INFO, "Thread Name: {0} , Is Daemon: {1}", new Object[] { t.getName(), t.isDaemon() }));

        System.exit(0);
    }

    public Boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    private static int getRdfQuality(final RdfQuality rdfqual) {
        return switch (rdfqual) {
            case RDF_QUAL_1 ->
                1;
            case RDF_QUAL_2 ->
                2;
            case RDF_QUAL_3 ->
                3;
            case RDF_QUAL_4 ->
                4;
            case RDF_QUAL_5 ->
                5;
            case RDF_QUAL_6 ->
                6;
            case RDF_QUAL_7 ->
                7;
            case RDF_QUAL_8 ->
                8;
            default ->
                0;
        };
    }

    private synchronized Precision getRequiredPrecision(Point2D tileSizeArcSeconds) {
        final long width = (long) Vincenty.arcSecondsToMeters(tileSizeArcSeconds.getX(), 90,
                abstractMap.getLowerRightLonLat().getY());
        final long height = (long) Vincenty.arcSecondsToMeters(tileSizeArcSeconds.getY(), 0,
                abstractMap.getLowerRightLonLat().getY());
        final long size = Math.max(width, height);
        if (size <= 9) {
            return Precision.PRECISION_1_M;
        }
        if (size <= 99) {
            return Precision.PRECISION_10_M;
        }
        if (size <= 999) {
            return Precision.PRECISION_100_M;
        }
        if (size <= 9999) {
            return Precision.PRECISION_1_KM;
        }
        if (size <= 99999) {
            return Precision.PRECISION_10_KM;
        }
        if (size <= 999999) {
            return Precision.PRECISION_100_KM;
        }
        return Precision.PRECISION_1000_KM;
    }

    private void getSettingsFromRegistry() {
        dataDirectory = new File(userPref.get(LAST_DATA_FILE_DIRECTORY, DEFAULT_DATA_FILE_PATH));
        startupLonLat = new Point2D.Double(
                userPref.getDouble("MapLongitude", DEFAULT_STARTUP_COORDINATES.getX()),
                userPref.getDouble("MapLatitude", DEFAULT_STARTUP_COORDINATES.getY()));
        if ((startupLonLat.getY() <= -90) || (startupLonLat.getY() >= 90)
                || (startupLonLat.getX() <= -180) || (startupLonLat.getX() >= 180)) {
            startupLonLat = DEFAULT_STARTUP_COORDINATES;
        }
        startupAltitude = userPref.getLong("MapAltitude", DEFAULT_STARTUP_ALTITUDE);
        startRadioCalFileName = userPref.get("StartRadioCalFileName", "null");
        testName = userPref.get("TestName", DEFAULT_TEST_NAME).replace(".sql", "");
        gpsClassName = userPref.get("GPSClassName", (String) AbstractGpsProcessor.getCatalogMap().keySet().toArray()[0]);
		tncClassName = userPref.get("TNCClassName", (String) AbstractTerminalNodeClient.getCatalogMap().keySet().toArray()[0]);
        signalTrackMapName = SignalTrackMapNames.valueOf(userPref.get("SignalTrackMapName", SignalTrackMapNames.OpenStreetMap.name()));
        environmentSensorClassName = userPref.get("EnvironmentMonitorClassName", (String) AbstractEnvironmentSensor.getCatalogMap().keySet().toArray()[0]);
    }

    private synchronized Color getTestTileColor(int measurementCount) {
        if (measurementCount == 0) {
            return coverageTestObject.getTileSelectedColor();
        } else if (measurementCount < coverageTestObject.getMinSamplesPerTile()) {
            return coverageTestObject.getTileInProgressColor();
        } else if (measurementCount >= coverageTestObject.getMinSamplesPerTile()) {
            return coverageTestObject.getTileCompleteColor();
        } else {
            return coverageTestObject.getTileSelectedColor();
        }
    }

    private synchronized Color getTestTileColor(TestTile testTile) {
        return getTestTileColor(testTile.getMeasurementCount());
    }

    private void gpsCDHoldingPropertyChangeListenerEvent(final PropertyChangeEvent event) {
        invokeLaterInDispatchThreadIfNeeded(() -> {
            if ((boolean) event.getNewValue()) {
                gpsCD.setBackground(ST_BRIGHT_GREEN);
            } else {
                gpsCD.setBackground(ST_BRIGHT_RED);
            }
        });
    }

    private void abstractAtmosphericMonitorComponentMenuActionListenerEvent() {
        stopEnvironmentSensor();
        stopBlackGlobeTempSensor();
        stopRadiationSensor();
        stopDateTimeService();
        stopAirNow();
        stopNetworkEarthRotationService();
        if (environmentMonitorComponent == null) {
            environmentMonitorComponent = new EnvironmentMonitorComponent(environmentSensor, bgts, radDetector, aqs,  dts);
        } else {
            environmentMonitorComponent.setVisible(true);
        }
    }

    private void gpsComponentMenuActionListenerEvent() {
    	stopGps();
        if (gpsComponent == null) {
            gpsComponent = new GPSComponent(gpsProcessor);
        } else {
            gpsComponent.setVisible(true);
        }
    }

    private void aprsComponentMenuActionListenerEvent() {
        stopAPRS();
        if (aprsComponent == null) {
            aprsComponent = new APRSComponent(getAprsProcessor());
        } else {
            aprsComponent.setVisible(true);
        }
    }

    private void nasaDonkiApiConfigComponentMenuActionListenerEvent() {
        new NASASpaceWeatherSettingsComponent(new NASASpaceWeatherProcessor());
    }

    private void gpsCourseMadeGoodTruePropertyChangeListenerEvent() {
        invokeLaterInDispatchThreadIfNeeded(() -> {
            gpsCompassRose.setSelectColor(AbstractGpsProcessor.getGpsColor(gpsProcessor.getFixQuality()));
            gpsValidHeadingTimer.restart();
            gpsCompassRose.setHeading((int) gpsProcessor.getCourseMadeGoodTrue());
        });
    }

    private void gpsCRCErrorEvent(final PropertyChangeEvent event) {
        if (event.getNewValue() == null) {
            return;
        }
        LOG.log(Level.INFO, (String) event.getNewValue());
        if (gpsProcessor.isReportCRCErrors()) {
            invokeLaterInDispatchThreadIfNeeded(() -> {
                final String message = (String) event.getNewValue();
                final var formattedMsg = message.substring(0, Math.min(50, message.length()));
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(SignalTrack.this), formattedMsg,
                        "GPS CRC Error", JOptionPane.ERROR_MESSAGE);
            });
        }
    }

    private void aprsCRCErrorEvent(final PropertyChangeEvent event) {
        if (event.getNewValue() == null) {
            return;
        }
        LOG.log(Level.INFO, (String) event.getNewValue());
        if (getAprsProcessor().isReportCRCErrors()) {
            invokeLaterInDispatchThreadIfNeeded(() -> {
                final String message = (String) event.getNewValue();
                final var formattedMsg = message.substring(0, Math.min(50, message.length()));
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(SignalTrack.this), formattedMsg,
                        "APRS CRC Error", JOptionPane.ERROR_MESSAGE);
            });
        }
    }

    private void gpsCTSHoldingPropertyChangeListenerEvent(final PropertyChangeEvent event) {
        invokeLaterInDispatchThreadIfNeeded(() -> {
            if ((boolean) event.getNewValue()) {
                gpsCTS.setBackground(ST_BRIGHT_GREEN);
            } else {
                gpsCTS.setBackground(ST_BRIGHT_RED);
            }
        });
    }

    private void gpsDSRHoldingPropertyChangeListenerEvent(final PropertyChangeEvent event) {
        invokeLaterInDispatchThreadIfNeeded(() -> {
            if ((boolean) event.getNewValue()) {
                gpsDSR.setBackground(ST_BRIGHT_GREEN);
            } else {
                gpsDSR.setBackground(ST_BRIGHT_RED);
            }
        });
    }

    private void gpsFixQualityPropertyChangeListenerEvent(final PropertyChangeEvent event) {
        invokeLaterInDispatchThreadIfNeeded(() -> {
            gpsStatus.setBackground(AbstractGpsProcessor.getGpsStatusBackgroundColor((FixQuality) event.getNewValue()));
            gpsStatus.setForeground(AbstractGpsProcessor.getGpsStatusForegroundColor((FixQuality) event.getNewValue()));
            gpsStatus.setText(AbstractGpsProcessor.getGpsStatusText((FixQuality) event.getNewValue()));
            abstractMap.setGpsSymbolColor(AbstractGpsProcessor.getGpsColor((FixQuality) event.getNewValue()));
            gpsPanelLatitude.setForeground(Color.BLACK);
            gpsPanelLongitude.setForeground(Color.BLACK);
            gpsPanelMGRS.setForeground(Color.BLACK);
            if (!isCoverageTestActive()) {
                gpsPanelGridSquare.setForeground(Color.BLACK);
            }
            gpsPanelSpeedMadeGood.setForeground(Color.BLACK);
            gpsPanelAltitude.setForeground(Color.BLACK);
        });
    }

    private void gpsOnlineChangeListenerEvent(final PropertyChangeEvent event) {
        invokeLaterInDispatchThreadIfNeeded(() -> {
            if ((boolean) event.getNewValue()) {
                gpsCTS.setBackground(ST_DARK_RED);
                gpsCD.setBackground(ST_DARK_RED);
                gpsDSR.setBackground(ST_DARK_RED);
                gpsRxData.setBackground(ST_DARK_RED);
                gpsTxData.setBackground(ST_DARK_RED);
            } else {
                gpsCTS.setBackground(Color.DARK_GRAY);
                gpsCD.setBackground(Color.DARK_GRAY);
                gpsDSR.setBackground(Color.DARK_GRAY);
                gpsRxData.setBackground(Color.DARK_GRAY);
                gpsTxData.setBackground(Color.DARK_GRAY);
                gpsButton.setSelected(false);
            }
        });
    }

    private void aprsOnlineChangeListenerEvent(final PropertyChangeEvent event) {
        invokeLaterInDispatchThreadIfNeeded(() -> {
            if ((boolean) event.getNewValue()) {
                aprsCTS.setBackground(ST_DARK_RED);
                aprsCD.setBackground(ST_DARK_RED);
                aprsDSR.setBackground(ST_DARK_RED);
                aprsRxData.setBackground(ST_DARK_RED);
                aprsTxData.setBackground(ST_DARK_RED);
            } else {
                aprsCTS.setBackground(ST_DARK_GRAY);
                aprsCD.setBackground(ST_DARK_GRAY);
                aprsDSR.setBackground(ST_DARK_GRAY);
                aprsRxData.setBackground(ST_DARK_GRAY);
                aprsTxData.setBackground(ST_DARK_GRAY);
            }
        });
    }

    private void mouseMovedTimerActionListenerEvent() {
        invokeLaterInDispatchThreadIfNeeded(() -> {
            cursorMaidenheadReference.setHorizontalAlignment(SwingConstants.LEFT);
            cursorMaidenheadReference
                    .setText(" GRID SQ: " + Maidenhead.lonLatToGridSquare(abstractMap.getMouseCoordinates()));
        });
    }

    private void variableTimerActionListenerEvent() {
    	final Thread thread = new Thread(new VariableMeasurementTimerAction());
        thread.start();
    }

    private void fixedTimerActionListenerEvent() {
    	final Thread thread = new Thread(new FixedMeasurementTimerAction());
        thread.start();
    }

    private void gpsValidHeadingTimerActionListenerEvent() {
        invokeLaterInDispatchThreadIfNeeded(() -> gpsCompassRose.setSelectColor(Color.GRAY));
        abstractMap.setGpsSymbolAngle(360);
    }

    private void gpsValidPositionPropertyChangeListenerEvent(final PropertyChangeEvent event) {
        setPositionComponents(PositionSource.GPS, (Point2D) event.getNewValue());
        if (environmentSensor.isUseGpsForStationLocation()) {
            environmentSensor.setStationLongitudeDegrees(((Point2D) event.getNewValue()).getX());
            environmentSensor.setStationLatitudeDegrees(((Point2D) event.getNewValue()).getY());
        }
        ConsolidatedTime.setLatitudeDegrees(((Point2D) event.getNewValue()).getX());
        ConsolidatedTime.setLongitudeDegrees(((Point2D) event.getNewValue()).getY());
        if (gpsProcessor.isCenterMapOnGPSPosition() && gpsProcessor.getFixQuality() != FixQuality.INVALID) {
            centerMapOnGpsPosition();
        }
    }

    private void gpsValidAltitudeMetersPropertyChangeListenerEvent(final PropertyChangeEvent event) {
        if (environmentSensor.isUseGpsForStationLocation()) {
            environmentSensor.setStationElevationMeters((Double) event.getNewValue());
        }
        ConsolidatedTime.setAltitudeMeters((double) event.getNewValue());
        invokeLaterInDispatchThreadIfNeeded(() -> gpsPanelAltitude.setText(String.format(getLocale(), "%6.1f FT", gpsProcessor.getAltitudeFeet())));
    }

    public static String toDecimalFormat(double value, int decimalPlaces) {
    	final DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(decimalPlaces);
        df.setMinimumFractionDigits(decimalPlaces);
        df.setRoundingMode(RoundingMode.CEILING);
        return df.format(value);
    }

    private void gpsValidTimePropertyChangeListenerEvent(final PropertyChangeEvent event) {
        final long newGpsTimeInMillis = (long) event.getNewValue();
        if ((previousGpsTimeInMillis != newGpsTimeInMillis) && gpsProcessor.getFixQuality() != FixQuality.INVALID) {
            consolidatedTime.setGpsTimeInMillis(newGpsTimeInMillis);
            environmentSensor.setGpsTimeInMillis(newGpsTimeInMillis);
        }
        previousGpsTimeInMillis = newGpsTimeInMillis;
    }

    private void gpsTimeZoneIdPropertyChangeListenerEvent(final PropertyChangeEvent event) {
        final ZoneId newTimeZoneId = (ZoneId) event.getNewValue();
        if ((previousTimeZoneId != newTimeZoneId) && gpsProcessor.getFixQuality() != FixQuality.INVALID) {
            consolidatedTime.setLocalZoneId(newTimeZoneId);
            environmentSensor.setZoneId(newTimeZoneId);
        }
        previousTimeZoneId = newTimeZoneId;
    }

    private void invalidComPortChangeListenerEvent(final PropertyChangeEvent event, final String device) {
        invokeLaterInDispatchThreadIfNeeded(
                () -> JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(SignalTrack.this),
                        "The " + device + " is configured to use comm port " + event.getNewValue() + "\n"
                        + "Please select a valid comm port from the " + device + " settings menu.\n",
                        "Comm Port Error", JOptionPane.ERROR_MESSAGE));
    }

    private boolean isOffCourse(Point2D lonLat) {
        final Point2D tileSize = coverageTestObject.getTileSizeArcSeconds();
        final Point2D gridRef = coverageTestObject.getGridReference();
        final Point2D gridSize = coverageTestObject.getGridSize();

        final Point2D lonlat = CoordinateUtils.lonLatFromAnywhereInTileToNorthWestCornerOfTile(lonLat, tileSize, gridRef, gridSize);

        if (lonlat == null) {
            return true;
        }
        final Precision precision = getRequiredPrecision(tileSize);

        return !database.isTileCreated(new TestTile(testName, lonlat, tileSize, precision));
    }

    private void mapBulkDownloaderMenuActionListenerEvent() {
        if (abstractMap.getSignalTrackMapName() == SignalTrackMapNames.WorldWindMap) {
            abstractMap.showBulkDownloadPanel();
        } else {
            setBulkMapTileDownloadSelectionMode(true);
            invokeLaterInDispatchThreadIfNeeded(() -> {
                messageLabel.setText("       MAP BULK DOWNLOAD MODE");
                doActionButton.setText("Apply Selection");
                doActionButton.setToolTipText("Press to apply selection to bulk downloader");
                doActionButton.setVisible(true);
            });
            abstractMap.setSelectionRectangleMode(true);
            abstractMap.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
        }
    }

    private void clearMapObjects() {
        abstractMap.clearMapObjects();
    }

    private void mapLayerSelectorMenuActionListenerEvent() {
        abstractMap.showLayerSelectorPanel();
    }

    private synchronized void handleMouseDrag() {
        if (isTestGridDrawOutlineMode()) {
        	final double t = GeometryUtils.getAngleFromScreenCoords(abstractMap.getSelectionRectangleOrigin(), mapCurrentCursor);
            if (t <= 90 || t >= 180) {
                return;
            }
            final double xTileSize = coverageTestObject.getTileSizeDegrees().getX();
            final double yTileSize = coverageTestObject.getTileSizeDegrees().getY();
            final long xTiles = Math
                    .round(Math.abs(abstractMap.getMouseDragCoordinates().getX() - abstractMap.getSelectionRectangle().getX())
                            / xTileSize);
            final long yTiles = Math
                    .round(Math.abs(abstractMap.getMouseDragCoordinates().getY() - abstractMap.getSelectionRectangle().getY())
                            / yTileSize);
            final double lowerRightLon = abstractMap.getSelectionRectangle().getX() + (xTiles * xTileSize);
            final double lowerRightLat = abstractMap.getSelectionRectangle().getY() - (yTiles * yTileSize);
            abstractMap.setSelectionRectangleVisible(true);
            abstractMap.setSelectionRectangleDestination(new Point2D.Double(lowerRightLon, lowerRightLat));
            setInhibitLeftMouseButtonReleaseEvent(false);
        }
        if (isTestTileSelectMode()) {
        	final double t = GeometryUtils.getAngleFromScreenCoords(abstractMap.getSelectionRectangleOrigin(), mapCurrentCursor);
            if (t <= 90 || t >= 180) {
                return;
            }
            abstractMap.setSelectionRectangleVisible(true);
            abstractMap.setSelectionRectangle(
                    GeometryUtils.getRectangleCoordsFromScreenPoints(abstractMap.getSelectionRectangleOrigin(), mapCurrentCursor));
            setInhibitLeftMouseButtonReleaseEvent(false);
        }
        if (isBulkMapTileDownloadSelectionMode()) {
        	final double t = GeometryUtils.getAngleFromScreenCoords(abstractMap.getSelectionRectangleOrigin(), mapCurrentCursor);
            if (t <= 90 || t >= 180) {
                return;
            }
            final double xTileSize = coverageTestObject.getTileSizeDegrees().getX();
            final double yTileSize = coverageTestObject.getTileSizeDegrees().getY();
            final long xTiles = Math
                    .round(Math.abs(abstractMap.getMouseDragCoordinates().getX() - abstractMap.getSelectionRectangle().getX())
                            / xTileSize);
            final long yTiles = Math
                    .round(Math.abs(abstractMap.getMouseDragCoordinates().getY() - abstractMap.getSelectionRectangle().getY())
                            / yTileSize);
            final double lowerRightLon = abstractMap.getSelectionRectangle().getX() + (xTiles * xTileSize);
            final double lowerRightLat = abstractMap.getSelectionRectangle().getY() - (yTiles * yTileSize);
            invokeLaterInDispatchThreadIfNeeded(() -> messageLabel.setText("Map Tiles Selected to Download"));
            abstractMap.setSelectionRectangleVisible(true);
            abstractMap.setSelectionRectangleDestination(new Point2D.Double(lowerRightLon, lowerRightLat));
            setInhibitLeftMouseButtonReleaseEvent(false);
        }
    }

    private void updateMeasureModeLabel(SurfaceLine dos) {
        if (measureMode) {
            invokeLaterInDispatchThreadIfNeeded(() -> messageLabel
                    .setText(String.format(getLocale(), "%9.1f", dos.getFeet()) + " ft | " + String.format(getLocale(), "%4.3f", dos.getMiles())
                            + " mi | " + String.format(getLocale(), "%4.3f", dos.getKiloMeters()) + " km | "
                            + String.format(getLocale(), "%4.3f", dos.getArcSeconds()) + " sec"));
        } else {
            invokeLaterInDispatchThreadIfNeeded(() -> messageLabel.setText(""));
        }
    }

    private void centerMapOnGpsPosition() {
        if (gpsProcessor.getFixQuality() != FixQuality.INVALID && !isMapDragged() && gpsProcessor.isEnableGPSTracking()) {
            abstractMap.fitToDisplay(true, true, true, true, true, true, true, true, true, true, true, true);
            setMapDragged(true);
        }
    }
    
    private synchronized void mapPanelLeftMouseButtonPressed() {
        if (!Coordinate.validLongitude(abstractMap.getMouseCoordinates().getY())) {
            return;
        }
        final Point2D point = abstractMap.getMouseCoordinates();
        if (isTestGridDrawOutlineMode() || isTestTileSelectMode() || isBulkMapTileDownloadSelectionMode()) {
            abstractMap.setSelectionRectangleVisible(false);
            abstractMap.setSelectionRectangleOrigin(point);
            abstractMap.setSelectionRectangleDestination(abstractMap.getMouseCoordinates());
        }
        if ((getPositionSource() == PositionSource.MANUAL) && abstractMap.isShowGrid() && isCoverageTestActive()
                && !isTestTileSelectMode()) {
            setPositionComponents(PositionSource.MANUAL, point);
        }
        if (!isCoverageTestActive() && isTestTileSelectMode() && !isMapDragged()) {
            createBlankTestTile(point);
        }
    }

    private void mapPanelRightMouseButtonPressed() {
        if (isDopplerActive() && !Coordinate.validLongitude(abstractMap.getMouseCoordinates().getY())) {
            setCursorBearingSet(false);
            cursorBearing = 0;
            cursorBearingPosition = abstractMap.getMouseCoordinates();
            cursorBearingIndex = addRdfBearing(cursorBearingPosition, 0, RDF_BEARING_LENGTH_IN_DEGREES,
                    RdfQuality.RDF_QUAL_8, ST_BRIGHT_RED);
            abstractMap.addRing(cursorBearingPosition, 6, ST_BRIGHT_RED);
        }
    }

    private void mapSettingsMenuActionListenerEvent() {
        new MapComponent(abstractMap);
    }

    private void mapStatisticsMenuActionListenerEvent() {
        abstractMap.showStatisticsPanel();
    }

    private void mapCacheViewerMenuActionListenerEvent() {
        abstractMap.showCacheViewerPanel();
    }

    private void mapProviderChangeListenerEvent(final PropertyChangeEvent event) {
        try {
            Point2D point = abstractMap.getCenterLonLat();

            if (point == null) {
                point = DEFAULT_STARTUP_COORDINATES;
            }
            
            // TODO: save all abstractMap graphics and settings add them back after changing the abstractMap provider.
            final Long altitude = abstractMap.getAltitude();

            abstractMap.setVisible(false);
            abstractMap.clearCache();
            mapPanel.removeAll();

            remove(mapPanel);

            abstractMap.getPropertyChangeSupport().removePropertyChangeListener(mapPropertyChangeListener);
            abstractMap.removeMouseListener(mapMouseListener);
            abstractMap.removeMouseMotionListener(mapMouseMotionListener);

            abstractMap.getActionMap().clear();
            abstractMap.getInputMap().clear();

            abstractMap.close();

            signalTrackMapName = (SignalTrackMapNames) event.getNewValue();

            abstractMap = initializeMapInterface(signalTrackMapName, point, altitude);

            if (abstractMap != null) {
                abstractMap.getPropertyChangeSupport().addPropertyChangeListener(mapPropertyChangeListener);
                abstractMap.initialize();
                mapPanel = getMapPanel();
            }

            createNormalGUI();
            revalidate();
            repaint();

        } catch (Exception ex) {
        	LOG.log(Level.WARNING, null, ex);
		}
    }

    private synchronized void measurementRecordReadyEvent() {
        coverageRecordRestoreProgress++;
        final String str = String.format(getLocale(), "Restoring record %d of %d records...", coverageRecordRestoreProgress,
                coverageTestRestoreMonitor.getMax());
        coverageTestRestoreMonitor.setStatusText(str, coverageRecordRestoreProgress);
    }

    private void measurementSetRecordReadyEvent(final PropertyChangeEvent event) {
        showSignalMarker(coverageTestObject.getSignalQualityDisplayMode(), SIGNAL_MARKER_CHANNEL,
                ((MeasurementSet) event.getNewValue()).getPosition());
    }

    private void measurementSetRecordCountReadyEvent(final PropertyChangeEvent event) {
        invokeLaterInDispatchThreadIfNeeded(
                () -> recordCountLabel.setText(String.format(getLocale(), "%08d", event.getNewValue())));
    }

    private synchronized void measurementSetTableAppendedEvent(PropertyChangeEvent event) {
    	final TestTile testTile = getAssignedTestTile(((MeasurementSet) event.getNewValue()).getPosition());
        updateTestTileStats(testTile);
        abstractMap.setTestTileColor(testTile, getTestTileColor(testTile));
        if (!getAbstractRadio().isScanning()) {
            final var measurement = new Measurement();
            measurement.setFrequency(getAbstractRadio().getFrequency());
            measurement.setSelected(true);
            measurement.setBer(getAbstractRadio().getBER());
            measurement.setSinad(getAbstractRadio().getSinad());
            measurement.setdBm(getAbstractRadio().getdBm());
            measurement.setChannelNumber(0);
            final MeasurementSet measurementSet = (MeasurementSet) event.getNewValue();
            database.appendMeasurement(measurementSet, measurement);
            showSignalMarker(coverageTestObject.getSignalQualityDisplayMode(), 0, measurementSet.getPosition());
        } else {
            for (var i = 0; i < DEFAULT_SCAN_CHANNEL_LIST_SIZE; i++) {
                final var measurement = new Measurement();
                measurement.setFrequency(getAbstractRadio().getScanElement(i).getFrequency());
                measurement.setSelected(getAbstractRadio().getScanElement(i).getSelected());
                measurement.setBer(getAbstractRadio().getScanElement(i).getBer());
                measurement.setSinad(getAbstractRadio().getScanElement(i).getSinad());
                measurement.setdBm(getAbstractRadio().getScanElement(i).getdBm());
                measurement.setChannelNumber(i);
                final var measurementSet = (MeasurementSet) event.getNewValue();
                database.appendMeasurement(measurementSet, measurement);
                showSignalMarker(coverageTestObject.getSignalQualityDisplayMode(), getAbstractRadio().getCurrentChannel(),
                        measurementSet.getPosition());
            }
        }
    }

    private AbstractMap initializeMapInterface(SignalTrackMapNames signalTrackMapName, Point2D point, Long altitude) {
        AbstractMap map = null;
        switch (signalTrackMapName) {
            case OpenStreetMap -> {
                map = AbstractMap.getMapInstance(AbstractMap.getSignalTrackMapProviderCatalog()[0], point, altitude,
                        new OSMTileFactoryInfo());
                invokeLaterInDispatchThreadIfNeeded(() -> {
                    viewingAltitude.setText("");
                    cursorTerrainElevationFeet.setText("");
                });
            }
            case VirtualEarthMap -> {
                map = AbstractMap.getMapInstance(AbstractMap.getSignalTrackMapProviderCatalog()[0], point, altitude,
                        new VirtualEarthTileFactoryInfo(VirtualEarthTileFactoryInfo.MAP));
                invokeLaterInDispatchThreadIfNeeded(() -> {
                    viewingAltitude.setText("");
                    cursorTerrainElevationFeet.setText("");
                });
            }
            case VirtualEarthSatellite -> {
                map = AbstractMap.getMapInstance(AbstractMap.getSignalTrackMapProviderCatalog()[0], point, altitude,
                        new VirtualEarthTileFactoryInfo(VirtualEarthTileFactoryInfo.SATELLITE));
                invokeLaterInDispatchThreadIfNeeded(() -> {
                    viewingAltitude.setText("");
                    cursorTerrainElevationFeet.setText("");
                });
            }
            case VirtualEarthHybrid -> {
                map = AbstractMap.getMapInstance(AbstractMap.getSignalTrackMapProviderCatalog()[0], point, altitude,
                        new VirtualEarthTileFactoryInfo(VirtualEarthTileFactoryInfo.HYBRID));
                invokeLaterInDispatchThreadIfNeeded(() -> {
                    viewingAltitude.setText("");
                    cursorTerrainElevationFeet.setText("");
                });
            }
            case WorldWindMap -> {
                map = AbstractMap.getMapInstance(AbstractMap.getSignalTrackMapProviderCatalog()[1], point, altitude);
                invokeLaterInDispatchThreadIfNeeded(() -> {
                    viewingAltitude.setText("");
                    cursorTerrainElevationFeet.setText("");
                });
            }
        }
        if (map != null) {
            mapBulkDownloaderMenuItem.setEnabled(map.hasBulkDownloaderPanel());
            mapLayerSelectorMenuItem.setEnabled(map.hasLayerSelectorPanel());
            mapStatisticsMenuItem.setEnabled(map.hasStatisticsPanel());
            mapCacheViewerMenuItem.setEnabled(map.hasCacheViewerPanel());
        }
        return map;
    }

    private static double getVariableTimePeriod(double sizeOfTileArcSeconds, double speedMadeGoodKPH,
            int measurementsPerTile) {
        final double timeAcrossTileAtSpeed = (Vincenty.degreesToMeters(sizeOfTileArcSeconds / 3600.0, 0, 0) / 1000.0)
                / (Math.max(speedMadeGoodKPH, 1.0));
        return (timeAcrossTileAtSpeed * 3.6E6) / (measurementsPerTile + 1);
    }

    private void moveRdfBearing(final int index, final Point2D p1, final double bearing, final double length,
            final int quality, final Color color) {
        abstractMap.deleteLine(index);
        bearingList.remove(index);

        bearingList.add(index, new Bearing(index, p1, bearing, length, quality, color));

        final Point2D p2 = new Point2D.Double((Math.sin((bearing * Math.PI) / 180) * length) + p1.getX(),
                (Math.cos((bearing * Math.PI) / 180) * length) + p1.getY());

        abstractMap.addLine(p1, p2, color);
    }

    private void networkClockFailure() {
        LOG.log(Level.WARNING, "Network Clock Failure");
    }

    private void networkClockUpdate(final PropertyChangeEvent event) {
        invokeLaterInDispatchThreadIfNeeded(() -> {
            if (utcLabel != null) {
                utcLabel.setText(" " + event.getNewValue() + " Z");
                setStrataTextTag(consolidatedTime.getTimeStratum(), utcLabel);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private JList<Class<?>> findFileList(Component comp) {
        if (comp instanceof JList) {
            return (JList<Class<?>>) comp;
        }
        if (comp instanceof Container container) {
            for (final Component c : container.getComponents()) {
                final JList<Class<?>> list = findFileList(c);
                if (list != null) {
                    return list;
                }
            }
        }
        return null;
    }

    private synchronized void measureButtonActionListenerEvent(ActionEvent event) {
        if (event.getID() == ActionEvent.ACTION_PERFORMED) {
            if (isTestTileSelectMode() || isTestGridDrawOutlineMode()) {
                setMeasureMode(false);
                abstractMap.setRulerMode(false);
            } else {
                setMeasureMode(measureButton.isSelected());
                abstractMap.setRulerMode(measureButton.isSelected());
            }
            if (!isMeasureMode()) {
                invokeLaterInDispatchThreadIfNeeded(() -> messageLabel.setText(""));
            }
        }
    }

    private int openCoverageTestDatabaseFile() {
        final var fileChooser = new JFileChooser(dataDirectory);
        final JList<Class<?>> list = findFileList(fileChooser);

        if (list != null) {
            for (final MouseListener l : list.getMouseListeners()) {
                if (l.getClass().getName().contains("FilePane")) {
                    list.removeMouseListener(l);
                    list.addMouseListener(new MouseListener() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            if (e.getClickCount() == 1) {
                                final var file = fileChooser.getSelectedFile();
                                if (file != null) {
                                    final BasicFileChooserUI ui = (BasicFileChooserUI) fileChooser.getUI();
                                    ui.setFileName(file.getName());
                                }
                            } else if (e.getClickCount() == 2) {
                                final var file = fileChooser.getSelectedFile();
                                if (file != null) {
                                    if (file.isDirectory()) {
                                        if (!file.getPath().contains(".sql")) {
                                            fileChooser.setCurrentDirectory(file);
                                        } else {
                                            fileChooser.approveSelection();
                                        }
                                    } else if (file.isFile()) {
                                        fileChooser.setSelectedFile(file);
                                    }
                                    final BasicFileChooserUI ui = (BasicFileChooserUI) fileChooser.getUI();
                                    ui.setFileName(file.getName());
                                }
                            }
                        }

                        @Override
                        public void mouseEntered(MouseEvent e) {
                            // NOOP
                        }

                        @Override
                        public void mouseExited(MouseEvent e) {
                            // NOOP
                        }

                        @Override
                        public void mousePressed(MouseEvent e) {
                            // NOOP
                        }

                        @Override
                        public void mouseReleased(MouseEvent e) {
                            // NOOP
                        }
                    });
                    break;
                }
            }
        }

        fileChooser.setFileFilter(new FileNameExtensionFilter("Coverage Test Database [*.ct.sql] ", ".ct.sql"));
        fileChooser.setAcceptAllFileFilterUsed(true);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setDialogTitle("Open Coverage Test Database File");

        final int returnVal = fileChooser.showOpenDialog(null);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            if (coverageTestComponent != null) {
                coverageTestComponent.setEnabled(false);
            }
            userPref.put(LAST_DATA_FILE_DIRECTORY, fileChooser.getCurrentDirectory().getPath());
            dataDirectory = fileChooser.getCurrentDirectory();
            if (!fileChooser.getCurrentDirectory().exists()) {
                createNewDatabase(fileChooser.getCurrentDirectory());
            } else {
                openDatabase(fileChooser.getSelectedFile());
                abstractMap.showGrid(true);
                abstractMap.showTestTiles(true);
            }
        }

        if (returnVal == JFileChooser.CANCEL_OPTION) {
            setDatabaseMode(DatabaseMode.CLOSED);
            setDataMode(DataMode.STOP);
        }

        remove(fileChooser);
        repaint();
        return returnVal;
    }

    private void openDatabase(File file) {
        stopAllProcesses();
        setDataMode(DataMode.STOP);
        // clearMapObjects();
        database = new Database(databaseConfig);
        database.addPropertyChangeListener(databaseListener);
        database.openDatabase(file, coverageTestObject, staticTestObject);
        testName = file.getName();
        invokeLaterInDispatchThreadIfNeeded(() -> logFileNameLabel.setText(testName));
    }

    private void openDataFileMenuItemActionListenerEvent() {
        openCoverageTestDatabaseFile();
    }

    private void environmentMonitorMenuItemActionListenerEvent() {
        if (environmentMonitorGui == null) {
            environmentMonitorGui = new EnvironmentMonitorGUI(DisplaySize.Size_1024x768, environmentSensor, airNow, bgts, radDetector, aqs, swp, ners, aprsProcessor, consolidatedTime, eventPanel);
        } else {
            environmentMonitorGui.setVisible(true);
        }
        environmentSensor.startSensor();
        bgts.start();
        radDetector.start();
        aqs.start();
    }

    private synchronized Point2D pointMean(final List<Point2D> list) {
        if (list.isEmpty()) {
            return null;
        }
        if (list.size() < 2) {
            return list.get(0);
        }
        double xs = 0.0;
        double ys = 0.0;
        final double xm;
        final double ym;
        final Iterator<Point2D> iterator = list.iterator();
        while (iterator.hasNext()) {
            final Point2D a = iterator.next();
            xs += a.getX();
            ys += a.getY();
        }
        xm = xs / list.size();
        ym = ys / list.size();
        return new Point2D.Double(xm, ym);
    }

    private synchronized double pointStandardDeviation(List<Point2D> list) {
        return Math.sqrt(pointVariance(list));
    }

    private synchronized double pointVariance(final List<Point2D> list) {
        double variance = 0;
        try {
            if (list.size() < 2) {
                return 0;
            }
            final Point2D mean = pointMean(list);
            if (mean != null) {
                double xv = 0.0;
                double yv = 0.0;
                final Iterator<Point2D> iterator = list.iterator();
                while (iterator.hasNext()) {
                    final Point2D a = iterator.next();
                    xv += (mean.getX() - a.getX()) * (mean.getX() - a.getX());
                    yv += (mean.getY() - a.getY()) * (mean.getY() - a.getY());
                }
                variance = Math.max(xv / list.size(), yv / list.size());
            }
        } catch (NullPointerException ex) {
            LOG.log(Level.WARNING, ex.getMessage(), ex);
        }
        return variance;
    }

    private void printMenuItemActionListenerEvent() {
        invokeLaterInDispatchThreadIfNeeded(new PrintUtilities(abstractMap.getScreenShot()));
    }

    private void printPreviewMenuItemActionListenerEvent() {
        if (previewPrintPanel == null) {
            invokeLaterInDispatchThreadIfNeeded(
                    () -> previewPrintPanel = new PreviewPrintPanel(abstractMap.getScreenShot()));
        }
    }

    private void processBearingInformation() {
        triangulate = new Triangulate(bearingList);
        triangulate.addPropertyChangeListener(event -> {
            if (StateValue.DONE.toString().equals(event.getNewValue())) {
                triangulationComplete();
            }
        });
        triangulate.execute();
    }

    private final class MeasurementProcessor implements Runnable {

        private final TimingMode timingMode;
        private final PositionSource source;
        private final Point2D currentLonLat;
        private final TestTile currentTestTile;

        private MeasurementProcessor(TimingMode timingMode, PositionSource source, Point2D currentLonLat,
                TestTile currentTestTile) {
            this.timingMode = timingMode;
            this.source = source;
            this.currentLonLat = currentLonLat;
            this.currentTestTile = currentTestTile;
        }

        private void playTileCompleteSound() {
            try {
                aePlayWaveExecutor.execute(new AePlayWave(DEFAULT_PROCESS_COMPLETE_SOUND_STREAM));
            } catch (IOException | UnsupportedAudioFileException ex) {
                LOG.log(Level.WARNING, ex.getMessage(), ex);
            }
        }

        @Override
        public void run() {
            try {
                if (!isCoverageTestActive()) {
                    return;
                }
                if (database != null && currentLonLat != null && source != null
                        && timingMode != null) {

                    if (timingMode == TimingMode.FIXED && source != PositionSource.MANUAL
                            && getDataMode() == DataMode.RECORD && currentTestTile
                                    .getMeasurementCount() <= coverageTestObject.getMaxSamplesPerTile()) {
                        addMeasurementSet(currentTestTile, currentLonLat);
                    }

                    if (source == PositionSource.MANUAL && currentTestTile
                            .getMeasurementCount() <= coverageTestObject.getMaxSamplesPerTile()) {
                        addMeasurementSet(currentTestTile, currentLonLat);
                    }

                    if (currentTestTile.getMeasurementCount() == coverageTestObject
                            .getMinSamplesPerTile()) {
                        tilesComplete++;
                        if (coverageTestObject.isAlertOnMinimumSamplesPerTileAcquired()
                                && (getDataMode() != DataMode.STOP)) {
                            playTileCompleteSound();
                        }
                    }

                    if (timingMode == TimingMode.VARIABLE && source != PositionSource.MANUAL
                            && getDataMode() == DataMode.RECORD && currentTestTile
                                    .getMeasurementCount() <= coverageTestObject.getMaxSamplesPerTile()) {
                    	final int variableDelay = (int) Math
                                .round(getVariableTimePeriod(currentTestTile.getTileSizeInDegrees().getY(),
                                        gpsProcessor.getSpeedMadeGoodKPH(), coverageTestObject.getDotsPerTile()));
                        setMeasurementPeriodText(String.format(getLocale(), "%4d", variableDelay));
                        variableTimer.setDelay(variableDelay);
                    }

                }
            } catch (NullPointerException ex) {
                LOG.log(Level.WARNING, ex.getMessage(), ex);
            }
        }

        private void addMeasurementSet(TestTile testTile, Point2D lonLat) {
        	final MeasurementSet measurementSet = new MeasurementSet();

            measurementSet.setMarker(markerCounter);
            measurementSet.setMillis(consolidatedTime.getBestTimeInMillis());
            measurementSet.setPosition(lonLat);
            measurementSet.setTestTileID(testTile.getID());

            testTile.adddBmToAverage(getAbstractRadio().getdBm());
            testTile.addBerToAverage(getAbstractRadio().getBER());
            testTile.addSinadToAverage(getAbstractRadio().getSinad());

            database.appendMeasurementSet(testTile, measurementSet);
        }
    }

    private void setPositionComponents(final PositionSource source, final Point2D currentLonLat) {
        invokeLaterInDispatchThreadIfNeeded(() -> {
            if (gpsProcessor.isEnableGPSTracking() && source != PositionSource.MANUAL) {
            	final double angle;
                if (gpsProcessor.getSpeedMadeGoodMPH() >= 2) {
                    angle = gpsProcessor.getCourseMadeGoodTrue();
                } else {
                    angle = 360;
                }
                abstractMap.showGpsSymbol(true);
                abstractMap.setGpsSymbol(currentLonLat, gpsProcessor.getGpsSymbolRadius(), AbstractGpsProcessor.getGpsColor(gpsProcessor.getFixQuality()), (int) angle);
            }

            if (gpsProcessor.isCenterMapOnGPSPosition() && !isMapDragged() && source != PositionSource.MANUAL
                    && checkMapRecenter(currentLonLat)) {
                abstractMap.setCenterLonLat(currentLonLat);
            }

            if (source == PositionSource.MANUAL) {
                gpsPanelLongitude.setText("");
                gpsPanelLatitude.setText("");
                gpsPanelSpeedMadeGood.setText("");
                gpsPanelAltitude.setText("");
                gpsPanelMGRS.setText("");

            } else {
                final String mph = String.format(getLocale(), "%3.1f", gpsProcessor.getSpeedMadeGoodMPH()) + " MPH";
                final String kph = String.format(getLocale(), "%3.1f", gpsProcessor.getSpeedMadeGoodKPH()) + " KPH";

                gpsPanelLongitude.setText(String.format(getLocale(), "%9f", currentLonLat.getX()));
                gpsPanelLatitude.setText(String.format(getLocale(), "%8f", currentLonLat.getY()));
                gpsPanelSpeedMadeGood.setText(mph + " / " + kph);
                gpsPanelAltitude.setText(String.format(getLocale(), "%6.1f FT", gpsProcessor.getAltitudeFeet()));
                gpsPanelMGRS.setText(gpsProcessor.getMGRSLocation());
            }

            if (coverageTestObject.getTimingMode() == TimingMode.VARIABLE
                    && System.currentTimeMillis() - variableTimerSetMillis > VARIABLE_TIMER_RECALC_MILLIS) {
                variableTimer.setDelay((int) getVariableTimePeriod(coverageTestObject.getTileSizeDegrees().getY(),
                        gpsProcessor.getSpeedMadeGoodKPH(), coverageTestObject.getMinSamplesPerTile()));
                setMeasurementPeriodText(String.valueOf(variableTimer.getDelay()));
            }

            if (getDataMode() == DataMode.RECORD && isCoverageTestActive()) {
                abstractMap.stopAllTestTileFlash();
                abstractMap.setTileFlash(getAssignedTestTile(currentLonLat).getID(), true);
            } else {
                abstractMap.stopAllTestTileFlash();
            }

            setGpsPanelGridSquareLabel(currentLonLat);
        });
    }

    private void radioBERChangeListenerEvent() {
        setSignalDisplay(getCoverageTestMode(), getAbstractRadio().isScanning());
        executor.execute(new MeasurementProcessor(coverageTestObject.getTimingMode(), getPositionSource(),
                gpsProcessor.getPosition(), getAssignedTestTile(gpsProcessor.getPosition())));
    }

    private void radioInterfaceReadyChangeListenerEvent(final PropertyChangeEvent event) {
    	final boolean startNow = (boolean) event.getNewValue();
        if (startNow) {
            startRadio();
        }
    }

    private void radioBusyChangeListenerEvent(final PropertyChangeEvent event) {
        invokeLaterInDispatchThreadIfNeeded(() -> {
            if (!radioButton.isSelected()) {
                return;
            }
            if ((boolean) event.getNewValue()) {
                radioStatus.setText("BUSY");
                radioStatus.setBackground(Color.YELLOW);
            } else {
                radioStatus.setText("MON");
                radioStatus.setBackground(ST_BRIGHT_RED);
            }
        });
    }

    private void radioCDHoldingChangeListenerEvent(final PropertyChangeEvent event) {
        invokeLaterInDispatchThreadIfNeeded(() -> {
            if ((boolean) event.getNewValue()) {
                radioCD.setBackground(ST_BRIGHT_GREEN);
            } else {
                radioCD.setBackground(ST_BRIGHT_RED);
            }
        });
    }

    private void radioCTSHoldingChangeListenerEvent(final PropertyChangeEvent event) {
        invokeLaterInDispatchThreadIfNeeded(() -> {
            if ((boolean) event.getNewValue()) {
                radioCTS.setBackground(ST_BRIGHT_GREEN);
            } else {
                radioCTS.setBackground(ST_BRIGHT_RED);
            }
        });
    }

    private void radioDSRHoldingChangeListenerEvent(final PropertyChangeEvent event) {
        invokeLaterInDispatchThreadIfNeeded(() -> {
            if ((boolean) event.getNewValue()) {
                radioDSR.setBackground(ST_BRIGHT_GREEN);
            } else {
                radioDSR.setBackground(ST_BRIGHT_RED);
            }
        });
    }

    private void radioOnlineChangeListenerEvent(final PropertyChangeEvent event) {
        invokeLaterInDispatchThreadIfNeeded(() -> {
            if ((boolean) event.getNewValue()) {
                radioCTS.setBackground(ST_DARK_RED);
                radioCD.setBackground(ST_DARK_RED);
                radioDSR.setBackground(ST_DARK_RED);
                radioRxData.setBackground(ST_DARK_RED);
                radioTxData.setBackground(ST_DARK_RED);
            } else {
                radioCTS.setBackground(ST_DARK_GRAY);
                radioCD.setBackground(ST_DARK_GRAY);
                radioDSR.setBackground(ST_DARK_GRAY);
                radioRxData.setBackground(ST_DARK_GRAY);
                radioTxData.setBackground(ST_DARK_GRAY);
            }
        });
    }

    private void radioPowerChangeListenerEvent(final PropertyChangeEvent event) {
        if ((boolean) event.getNewValue()) {
            invokeLaterInDispatchThreadIfNeeded(() -> {
                if ((boolean) event.getNewValue()) {
                    radioStatus.setText("POWER");
                    radioStatus.setBackground(ST_DARK_GREEN);
                } else {
                    radioStatus.setText("RADIO");
                    radioStatus.setBackground(ST_DARK_GRAY);
                }
            });
        }
    }

    private void radioReadyChangeListenerEvent(final PropertyChangeEvent event) {
        invokeLaterInDispatchThreadIfNeeded(() -> {
            if ((boolean) event.getNewValue()) {
                radioStatus.setText("ON LINE");
                radioStatus.setBackground(ST_BRIGHT_GREEN);
            } else {
                radioStatus.setText("RADIO");
                radioStatus.setBackground(ST_DARK_GRAY);
            }
        });
    }

    private void radioRSSIChangeListenerEvent() {
        setSignalDisplay(getCoverageTestMode(), getAbstractRadio().isScanning());
        executor.execute(new MeasurementProcessor(coverageTestObject.getTimingMode(), getPositionSource(),
                gpsProcessor.getPosition(), getAssignedTestTile(gpsProcessor.getPosition())));
    }

    private void radioScanEnableChangeListenerEvent(PropertyChangeEvent event) {
        invokeLaterInDispatchThreadIfNeeded(() -> {
            if ((boolean) event.getNewValue()) {
                for (JLabel element : signalQuality) {
                    element.setText("");
                }
                signalMeter.setMeterLevels(0);
                signalMeter.setActiveMeters(getAbstractRadio().getScanList());
            } else {
                signalMeter.setMeterLevels(0);
                signalMeter.setActiveMeters(null);
            }
        });
    }

    private void radioScanListChangeEvent() {
        invokeLaterInDispatchThreadIfNeeded(() -> {
            for (JLabel element : signalQuality) {
                element.setText("");
            }
            signalMeter.setMeterLevels(0);
            signalMeter.setActiveMeters(getAbstractRadio().getScanList());
        });
    }

    private void radioScanMeasurementReadyEvent() {
        setSignalDisplay(getCoverageTestMode(), getAbstractRadio().isScanning());
    }

    private void rdfHeadingTruePropertyChangeListenerEvent(final PropertyChangeEvent event) {
        if (!Coordinate.validLongitude(abstractMap.getMouseCoordinates().getY())) {
            return;
        }
        cursorBearingPosition = abstractMap.getMouseCoordinates();
        cursorBearing = (double) event.getNewValue();
        cursorBearingIndex = addRdfBearing(cursorBearingPosition, cursorBearing,
                RDF_BEARING_LENGTH_IN_DEGREES, gpsProcessor.getRdfQuality(), ST_BRIGHT_RED);
        abstractMap.addRing(cursorBearingPosition, 6, ST_BRIGHT_RED);
        processBearingInformation();
    }

    private void receiverComponentMenuActionListenerEvent() {
        if (radioComponent == null) {
            radioComponent = new RadioComponent(getAbstractRadio());
        } else {
            radioComponent.setVisible(true);
        }
    }

    private void saveAsDataFileMenuItemActionListenerEvent(final ActionEvent event) {
        if (getDatabaseMode() != DatabaseMode.CLOSED) {
            final JFileChooser fileChooser = new JFileChooser();
            final FileNameExtensionFilter filter = new FileNameExtensionFilter("Test Database Files", "sql");
            fileChooser.setFileFilter(filter);
            fileChooser.setDialogTitle("Save Database File As");
            fileChooser.showSaveDialog(null);
            if (event.getID() == JFileChooser.APPROVE_OPTION) {
            	final File from = dataDirectory;
            	final File to = fileChooser.getSelectedFile();
                coverageTestObject.getPropertyChangeSupport().removePropertyChangeListener(coverageTestObjectListener);
                coverageTestObject = new CoverageTestObject(false, getAbstractRadio(), to.getName());
                coverageTestObject.getPropertyChangeSupport().addPropertyChangeListener(coverageTestObjectListener);
                database.close();
                if (!from.renameTo(to)) {
                	final String s = to.getAbsolutePath();
                    LOG.info(s);
                    invokeLaterInDispatchThreadIfNeeded(
                            () -> JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(SignalTrack.this),
                                    "File rename failed", "I/O Exception", JOptionPane.ERROR_MESSAGE));
                    database.openDatabase(to, coverageTestObject, staticTestObject);
                } else {
                    dataDirectory = to;
                    userPref.put(LAST_DATA_FILE_DIRECTORY, dataDirectory.getParent());
                    invokeLaterInDispatchThreadIfNeeded(() -> logFileNameLabel.setText(fileChooser.getSelectedFile().getName()));
                }
            }
            remove(fileChooser);
        }
    }

    private void saveDataFileMenuItemActionListenerEvent(final ActionEvent event) {
        // NO-OP
    }

    private void serialErrorChangeListenerEvent(final int event, final String errorText) {
        if (isSerialErrorQueued()) {
            return;
        }
        setSerialErrorQueued(true);
        try {
            final String eventMessage = AbstractTeletypeController.serialPortErrorMessage(event);
            LOG.info(errorText);
            invokeLaterInDispatchThreadIfNeeded(
                    () -> JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(SignalTrack.this),
                            "Serial Port " + eventMessage, errorText, JOptionPane.ERROR_MESSAGE));
            setSerialErrorQueued(false);
        } catch (ClassCastException | NumberFormatException ex) {
            LOG.log(Level.WARNING, ex.getMessage(), ex);
        }
    }

    private void serialErrorChangeListenerEvent(final String event, final String errorText) {
        if (isSerialErrorQueued()) {
            return;
        }
        setSerialErrorQueued(true);
        try {
            LOG.info(errorText);
            invokeLaterInDispatchThreadIfNeeded(() -> JOptionPane.showMessageDialog(
                    SwingUtilities.getWindowAncestor(SignalTrack.this), event, errorText, JOptionPane.ERROR_MESSAGE));
            setSerialErrorQueued(false);
        } catch (ClassCastException | NumberFormatException ex) {
            LOG.log(Level.WARNING, ex.getMessage(), ex);
        }
    }

    private void setDatabaseMode(DatabaseMode databaseMode) {
        this.databaseMode = databaseMode;
        LOG.log(Level.INFO, "System database mode set to: {0}", databaseMode.name());
        invokeLaterInDispatchThreadIfNeeded(() -> {
            switch (databaseMode) {
                case OPEN -> {
                    newDataFileMenuItem.setEnabled(false);
                    newDataFileButton.setEnabled(false);
                    openDataFileMenuItem.setEnabled(false);
                    openCoverageTestDataFileButton.setEnabled(false);
                    closeDataFileMenuItem.setEnabled(true);
                    closeDataFileButton.setEnabled(true);
                    saveDataFileMenuItem.setEnabled(true);
                    saveDataFileButton.setEnabled(true);
                    saveAsDataFileMenuItem.setEnabled(true);
                    stopDataFileButton.setEnabled(true);
                    recordDataFileButton.setEnabled(true);
                    learnModeButton.setEnabled(true);
                }
                case CLOSED -> {
                    newDataFileMenuItem.setEnabled(true);
                    newDataFileButton.setEnabled(true);
                    openDataFileMenuItem.setEnabled(true);
                    openCoverageTestDataFileButton.setEnabled(true);
                    closeDataFileMenuItem.setEnabled(false);
                    closeDataFileButton.setEnabled(false);
                    saveDataFileMenuItem.setEnabled(false);
                    saveDataFileButton.setEnabled(false);
                    saveAsDataFileMenuItem.setEnabled(false);
                    stopDataFileButton.setEnabled(false);
                    recordDataFileButton.setEnabled(false);
                    learnModeButton.setEnabled(false);

                    minMeasurementsPerTile.setText("");
                    maxMeasurementsPerTile.setText("");
                    measurementsThisTile.setText("");
                    measurementPeriod.setText("");
                    tilesCompleted.setText("");
                    totalTiles.setText("");
                    recordCountLabel.setText("");
                    logFileNameLabel.setText("");
                    averageBerInCurrentTile.setText("");
                    averagedBmInCurrentTile.setText("");
                    averageSinadInCurrentTile.setText("");
                }
            }
        });
    }

    private void stopAllProcesses() {
        stopAPRS();
        stopCoverageTest();
        stopGps();
        stopBlackGlobeTempSensor();
        stopNetworkEarthRotationService();
        stopAirNow();
        stopDateTimeService();
        stopEnvironmentSensor();
        stopSpaceWeatherProcessor();
        stopLearnMode();
        stopRadio();
        stopStaticAnalysis();
    }

    private void setDataMode(DataMode dataMode) {
        this.dataMode = dataMode;
        LOG.log(Level.INFO, "System data mode set to: {0}", dataMode.name());
        invokeLaterInDispatchThreadIfNeeded(() -> {
            switch (dataMode) {
                case STOP -> {
                    stopDataFileButton.setEnabled(true);
                    stopDataFileButton.setSelected(true);
                    recordDataFileButton.setEnabled(true);
                    recordDataFileButton.setSelected(false);
                }
                case RECORD -> {
                    stopDataFileButton.setEnabled(true);
                    stopDataFileButton.setSelected(false);
                    recordDataFileButton.setEnabled(true);
                    recordDataFileButton.setSelected(true);
                }
                case RESTORE_COMPLETE -> {
                    stopDataFileButton.setEnabled(true);
                    stopDataFileButton.setSelected(false);
                    recordDataFileButton.setEnabled(true);
                    recordDataFileButton.setSelected(false);
                }
            }
        });
    }

    private void setGpsPanelGridSquareLabel(Point2D currentLonLat) {
        if (gpsProcessor.isPortOpen()) {
            if (isCoverageTestActive()) {
                if (isOffCourse(currentLonLat)) {
                    invokeLaterInDispatchThreadIfNeeded(() -> {
                        gpsPanelGridSquare.setText("OFF COURSE");
                        gpsPanelGridSquare.setForeground(ST_BRIGHT_RED);
                    });
                } else {
                    invokeLaterInDispatchThreadIfNeeded(() -> {
                        gpsPanelGridSquare.setText(CoordinateUtils
                                .lonLatToTestTile(currentLonLat, coverageTestObject.getTileSizeArcSeconds(),
                                        coverageTestObject.getGridReference(), coverageTestObject.getGridSize())
                                .toFormattedTestTileDesignator());
                        gpsPanelGridSquare.setForeground(ST_BRIGHT_GREEN);
                    });
                }
            } else {
                invokeLaterInDispatchThreadIfNeeded(() -> {
                    gpsPanelGridSquare.setText(Maidenhead.lonLatToGridSquare(currentLonLat));
                    gpsPanelGridSquare.setForeground(Color.BLACK);
                });
            }
        } else {
            invokeLaterInDispatchThreadIfNeeded(() -> gpsPanelGridSquare.setText(""));
        }
    }

    private void setMeasurementPeriodText(String text) {
        invokeLaterInDispatchThreadIfNeeded(() -> measurementPeriod.setText(text));
    }

    private void setMouseOffGlobe(boolean mouseOffGlobe) {
        this.mouseOffGlobe = mouseOffGlobe;
        if (mouseOffGlobe) {
            mouseMovedTimer.stop();
            invokeLaterInDispatchThreadIfNeeded(() -> {
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                cursorMaidenheadReference.setHorizontalAlignment(SwingConstants.CENTER);
                cursorMaidenheadReference.setFont(new Font("Calabri", Font.BOLD, 11));
                cursorMaidenheadReference.setText("CURSOR OFF GLOBE");
                cursorMaidenheadReference.setForeground(ST_BRIGHT_RED);
                cursorMGRS.setForeground(Color.GRAY);
                cursorLongitude.setForeground(Color.GRAY);
                cursorLatitude.setForeground(Color.GRAY);
                cursorTerrainElevationFeet.setForeground(Color.GRAY);
            });
        } else {
            invokeLaterInDispatchThreadIfNeeded(() -> {
                cursorMaidenheadReference.setHorizontalAlignment(SwingConstants.LEFT);
                cursorMaidenheadReference.setFont(new Font("Calabri", Font.PLAIN, 11));
                cursorMaidenheadReference.setForeground(Color.BLACK);
                cursorMaidenheadReference.setText(" GRID SQ: ");
                cursorMGRS.setForeground(Color.BLACK);
                cursorLongitude.setForeground(Color.BLACK);
                cursorLatitude.setForeground(Color.BLACK);
                cursorTerrainElevationFeet.setForeground(Color.BLACK);
            });
        }
    }

    private void setSignalDisplay(TestMode testMode, boolean isScanning) {
        invokeLaterInDispatchThreadIfNeeded(() -> {
            if (isScanning) {
                for (int i = 0; i < getAbstractRadio().getScanListSize(); i++) {
                    setSignalDisplay(i, testMode, isScanning);
                }
            } else {
                setSignalDisplay(0, testMode, isScanning);
            }
        });
    }

    private void setSignalDisplay(int i, TestMode testMode, boolean isScanning) {
        invokeLaterInDispatchThreadIfNeeded(() -> {
            if (null != testMode) {
                switch (testMode) {
                    case RSSI -> {
                        if (isScanning) {
                            signalMeter.setMeterLevel(i, (int) Math.round(getAbstractRadio().getdBmPercent(i)));
                            signalQuality[i].setText(String.format(getLocale(), "%3.1f", getAbstractRadio().getdBmScanListElement(i)));
                        } else {
                            signalMeter.setMeterLevel(i, (int) getAbstractRadio().getdBmPercent());
                            signalQuality[i].setText(String.format(getLocale(), "%3.1f", getAbstractRadio().getdBm()) + " dBm");
                        }
                        signalQuality[i].setToolTipText("Receive Channel " + (i + 1) + " dBm");
                    }
                    case SINAD -> {
                        if (isScanning) {
                            signalQuality[i].setText(String.format(getLocale(), "%2.1f", getAbstractRadio().getSinadScanList(i)));
                            signalMeter.setMeterLevel(i, (int) Math.round(getAbstractRadio().getSinadPercent(i)));
                        } else {
                            signalQuality[i].setText(String.format(getLocale(), "%2.1f", getAbstractRadio().getSinad()));
                            signalMeter.setMeterLevel(i, (int) Math.round(getAbstractRadio().getSinadPercent()));
                        }
                        signalQuality[i].setToolTipText("Receive Channel " + (i + 1) + " SINAD");
                    }
                    case BER -> {
                        if (isScanning) {
                            signalQuality[i].setText(String.format(getLocale(), "%3.1f", getAbstractRadio().getBerScanList(i)));
                            signalMeter.setMeterLevel(i, (int) Math.round(getAbstractRadio().getBERPercent(i)));
                        } else {
                            signalQuality[i].setText(String.format(getLocale(), "%3.1f", getAbstractRadio().getBER()));
                            signalMeter.setMeterLevel(i, (int) Math.round(getAbstractRadio().getBERPercent()));
                        }
                        signalQuality[i].setToolTipText("Receive Channel " + (i + 1) + " Bit Error Rate");
                    }
                    case RSSI_SINAD -> {
                        if (isScanning) {
                            signalQuality[i].setText(String.format(getLocale(), "%3.1f", getAbstractRadio().getdBmScanListElement(i)) + "/"
                                    + String.format(getLocale(), "%2.1f", getAbstractRadio().getSinadScanList(i)));
                            signalMeter.setMeterLevel(i, (int) Math.round(getAbstractRadio().getSinadPercent(i)));
                        } else {
                            signalQuality[i].setText(String.format(getLocale(), "%3.1f", getAbstractRadio().getdBm()) + "/"
                                    + String.format(getLocale(), "%2.1f", getAbstractRadio().getSinad()));
                            signalMeter.setMeterLevel(i, (int) Math.round(getAbstractRadio().getSinadPercent()));
                        }
                        signalQuality[i].setToolTipText("Receive Channel " + (i + 1) + " dBm / SINAD");
                    }
                    case RSSI_BER -> {
                        if (isScanning) {
                            signalQuality[i].setText(String.format(getLocale(), "%3.1f", getAbstractRadio().getdBmScanListElement(i)) + "/"
                                    + String.format(getLocale(), "%3.1f", getAbstractRadio().getBerScanList(i)));
                            signalMeter.setMeterLevel(i, (int) Math.round(getAbstractRadio().getBERPercent(i)));
                        } else {
                            signalQuality[i].setText(String.format(getLocale(), "%3.1f", getAbstractRadio().getdBm()) + "/"
                                    + String.format(getLocale(), "%3.1f", getAbstractRadio().getBER()));
                            signalMeter.setMeterLevel(i, (int) Math.round(getAbstractRadio().getBERPercent()));
                        }
                        signalQuality[i].setToolTipText("Receive Channel " + (i + 1) + " dBm / Bit Error Rate");
                    }
                    case OFF -> {
                        signalMeter.setEnabled(false);
                        signalQuality[i].setText("");
                        signalMeter.setMeterLevel(i, 0);
                        signalQuality[i].setToolTipText("Receive Channel " + (i + 1) + " Test Mode Is Not Configured");
                    }
                    case MODE_NOT_SELECTED ->
                        throw new UnsupportedOperationException("Unimplemented case: " + testMode);
                    default ->
                        throw new IllegalArgumentException("Unexpected value: " + testMode);
                }
            }
        });
    }

    private static void setStrataTextTag(final int timeStrata, JLabel utcLabel) {
        invokeLaterInDispatchThreadIfNeeded(() -> {
            if (timeStrata == ConsolidatedTime.STRATUM_GPS) {
                utcLabel.setText(utcLabel.getText() + "  STR GPS");
            } else if ((timeStrata >= ConsolidatedTime.STRATUM_NTP0)
                    && (timeStrata <= ConsolidatedTime.STRATUM_NTP15)) {
                utcLabel.setText(utcLabel.getText() + "  STR NTP" + timeStrata);
            } else if (timeStrata == ConsolidatedTime.STRATUM_UNSYNC) {
                utcLabel.setText(utcLabel.getText() + "  UNSYNC");
            }
        });
    }

    private void showSignalMarker(SignalQualityDisplayMode mode, int channel, Point2D point) {
        try {
        	final Color dotColor;
            switch (mode) {
                case SINAD -> {
                    dotColor = coverageTestObject.getSinadColor(getAbstractRadio().getSinadScanList(channel));
                    abstractMap.addSignalMarker(point, coverageTestObject.getSignalMarkerRadius(), dotColor);
                }
                case DBM -> {
                    dotColor = coverageTestObject.getdBmColor(getAbstractRadio().getdBmScanListElement(channel));
                    abstractMap.addSignalMarker(point, coverageTestObject.getSignalMarkerRadius(), dotColor);
                }
                case BER -> {
                    dotColor = coverageTestObject.getBerColor(getAbstractRadio().getBerScanList(channel));
                    abstractMap.addSignalMarker(point, coverageTestObject.getSignalMarkerRadius(), dotColor);
                }
            }
        } catch (NullPointerException ex) {
            LOG.log(Level.WARNING, ex.getMessage(), ex);
        }

    }

    private void signalAnalysisMenuActionListenerEvent() {
        signalAnalysis.showSettingsDialog(true);
    }

    private void radioSINADChangeListenerEvent() {
        setSignalDisplay(getCoverageTestMode(), getAbstractRadio().isScanning());
        executor.execute(new MeasurementProcessor(coverageTestObject.getTimingMode(), getPositionSource(),
                gpsProcessor.getPosition(), getAssignedTestTile(gpsProcessor.getPosition())));
    }

    private void startAPRS() {
        getAprsProcessor().getAPRSPropertyChangeSupport().addPropertyChangeListener(aprsListener);
        invokeLaterInDispatchThreadIfNeeded(() -> aprsButton.setSelected(true));
        getAprsProcessor().startAPRS();
        aprsCTS.setBackground(ST_DARK_RED);
        aprsDSR.setBackground(ST_DARK_RED);
        aprsCD.setBackground(ST_DARK_RED);
        aprsTxData.setBackground(ST_DARK_RED);
        aprsRxData.setBackground(ST_DARK_RED);
        aprsStatus.setText("APRS");
        aprsStatus.setBackground(ST_DARK_GREEN);
    }

    private void startCoverageTest() {
        if ((getDataMode() != DataMode.RECORD) && (getDatabaseMode() != DatabaseMode.CLOSED)) {
            setDataMode(DataMode.RECORD);
        }
        if (getDatabaseMode() == DatabaseMode.CLOSED) {
            invokeLaterInDispatchThreadIfNeeded(() -> JOptionPane.showMessageDialog(
                    SwingUtilities.getWindowAncestor(SignalTrack.this), "Please select a previously configured coverage test.",
                    "File Not Open Error", JOptionPane.ERROR_MESSAGE));
            final int returnVal = openCoverageTestDatabaseFile();
            if (returnVal == JFileChooser.CANCEL_OPTION) {
                stopCoverageTest();
                return;
            }
        } else {
            if (database.allMeasurementRecordsReady() && (database.getTileRecordList().isEmpty())) {
                invokeLaterInDispatchThreadIfNeeded(() -> JOptionPane.showMessageDialog(
                        SwingUtilities.getWindowAncestor(SignalTrack.this), "No tiles have been selected for testing.",
                        "Invalid Selection", JOptionPane.ERROR_MESSAGE));
                stopCoverageTest();
                return;
            }
        }
        setCoverageTestActive(true);
        coverageTestObject.setAllowModifications(false);
        coverageTestObject.setShapesEnabled(true);

        invokeLaterInDispatchThreadIfNeeded(() -> {
            cursorMaidenheadReference.setToolTipText("Test Tile Reference at Cursor");
            gpsPanelGridSquare.setToolTipText("Test Tile Grid Reference at GPS Location");
        });

        if (coverageTestObject.getTimingMode() == TimingMode.FIXED) {
            setMeasurementPeriodText(String.format(getLocale(), "%4d", coverageTestObject.getFixedTimePeriod()));
            fixedTimer.setDelay(coverageTestObject.getFixedTimePeriod());
            fixedTimer.start();
        } else if (coverageTestObject.getTimingMode() == TimingMode.VARIABLE) {
            variableTimer.setDelay((int) getVariableTimePeriod(coverageTestObject.getTileSizeDegrees().getY(),
                    gpsProcessor.getSpeedMadeGoodKPH(), coverageTestObject.getMinSamplesPerTile()));
            variableTimer.start();
        } else if (getPositionSource() == PositionSource.MANUAL) {
            setMeasurementPeriodText("MAN");
        }
    }

    private void startDemo() {
        executor.execute(new Demo());
    }

    private void startGps() {
        gpsProcessor.startGPS();
        gpsProcessor.setContinuousUpdate(true);
        invokeLaterInDispatchThreadIfNeeded(() -> {
            gpsPanelLatitude.setForeground(Color.DARK_GRAY);
            gpsPanelLongitude.setForeground(Color.DARK_GRAY);
            gpsPanelMGRS.setForeground(Color.DARK_GRAY);
            gpsPanelGridSquare.setForeground(Color.DARK_GRAY);
            gpsPanelSpeedMadeGood.setForeground(Color.DARK_GRAY);
            gpsPanelAltitude.setForeground(Color.DARK_GRAY);
            abstractMap.showGpsSymbol(false);
            gpsButton.setSelected(true);
            centerOnGpsButton.setEnabled(true);
            centerOnGpsButton.setSelected(true);
            gpsTxData.setBackground(ST_DARK_RED);
            gpsRxData.setBackground(ST_DARK_RED);
            gpsStatus.setText("GPS");
            gpsStatus.setBackground(ST_DARK_GREEN);
        });
    }

    private void startGpsProcesses() {
    	gpsProcessor.getGPSPropertyChangeSupport().addPropertyChangeListener(gpsListener);
        if (gpsProcessor.isStartGPSWithSystem()) {
            startGps();
        }
        centerOnGpsButton.setEnabled(gpsProcessor.isCenterMapOnGPSPosition());
    }
    
    private void startTimekeepingProcesses() {
        consolidatedTime.startAutomaticNetworkTimeUpdates();
        consolidatedTime.startClock();
    }

    private void startAprsProcesses() {
        if (getAprsProcessor().isStartAPRSWithSystem()) {
            startAPRS();
        }
    }

    private void startDatabaseProcesses() {
        if (DatabaseMode.valueOf(userPref.get("TerminalDatabaseMode", DatabaseMode.CLOSED.name())) == DatabaseMode.OPEN) {
        	final File databaseFile = new File(userPref.get("DatabaseFile", ""));
            if (!databaseFile.getPath().isEmpty()) {
                openDatabase(databaseFile);
            }
        }
    }

    private void startRadio() {
        if (getAbstractRadio() == null) {
            radioButton.setSelected(false);
            invokeLaterInDispatchThreadIfNeeded(
                    () -> JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(SignalTrack.this),
                            "No abstractRadioReceiver definitions have been installed.\nPlease contact your administrator.",
                            "Radio Configuration Exception", JOptionPane.ERROR_MESSAGE));
            return;
        }
        radioButton.setSelected(true);
        getAbstractRadio().getReceiverEvent().addPropertyChangeListener(radioListener);
        getAbstractRadio().startRadio();
        radioStatus.setText("RADIO");
        radioCTS.setBackground(ST_DARK_RED);
        radioDSR.setBackground(ST_DARK_RED);
        radioCD.setBackground(ST_DARK_RED);
        radioTxData.setBackground(ST_DARK_RED);
        radioRxData.setBackground(ST_DARK_RED);
        radioStatus.setBackground(ST_DARK_GREEN);
        signalMeter.setEnabled(true);
    }

    private void startStaticMeasurementScheduler() {
        staticScheduler = Executors.newScheduledThreadPool(0);
        staticSchedulerHandle = staticScheduler.scheduleAtFixedRate(new MeasurementCompiler(), 0, 1000,
                TimeUnit.MILLISECONDS);
    }

    private void startStaticAnalysis() {
        setStaticAnalysisActive(true);
        staticLocationAnalysisButton.setSelected(true);
        staticTestObject.setShapesEnabled(true);
        startStaticMeasurementScheduler();
    }

    private void staticMeasurementRecordReadyEvent() {
        staticRecordRestoreProgress++;
        if (staticTestRestoreMonitor != null) {
        	final String str = String.format(getLocale(), "Restoring record %1d of %2d", staticRecordRestoreProgress,
                    staticTestRestoreMonitor.getMax());
            staticTestRestoreMonitor.setStatusText(str, staticRecordRestoreProgress);
        }
        if (staticRecordCount == staticRecordRestoreProgress) {
            executor.execute(new MeasurementCompiler());
        }
    }

    private void staticRecordCountReadyEvent(final PropertyChangeEvent event) {
        staticRecordCount = (int) event.getNewValue();
        if (staticRecordCount > 0) {
            staticTestRestoreMonitor = new ProgressDialog(true, "", "Downloading Static Test Data from Disk", 0,
                    staticRecordCount, true, true);
        }
    }

    private void staticSignalLocationSettingsMenuActionListenerEvent() {
        if (staticTestComponent == null) {
            staticTestComponent = new StaticTestComponent(staticTestObject);
        } else {
            staticTestComponent.toFront();
            staticTestComponent.setEnabled(true);
            staticTestComponent.setVisible(true);
        }
    }

    private void staticTableAppendedEvent(PropertyChangeEvent event) {
    	final StaticMeasurement sm = (StaticMeasurement) event.getSource();
        try {
            measurementProcessQueue.put(sm);
        } catch (InterruptedException ex) {
            LOG.log(Level.WARNING, ex.getMessage(), ex);
            Thread.currentThread().interrupt();
        }
    }

    private void flightInformationTableAppendedEvent(final PropertyChangeEvent event) {
        final FlightInformation fi = (FlightInformation) event.getNewValue();
        flightInformationFlightNumber = fi.getFlight();
    }

    private void stopAPRS() {
        try {
            getAprsProcessor().getAPRSPropertyChangeSupport().removePropertyChangeListener(aprsListener);
            getAprsProcessor().close();
        } finally {
            invokeLaterInDispatchThreadIfNeeded(() -> {
                aprsCTS.setBackground(ST_DARK_GRAY);
                aprsDSR.setBackground(ST_DARK_GRAY);
                aprsCD.setBackground(ST_DARK_GRAY);
                aprsTxData.setBackground(ST_DARK_GRAY);
                aprsRxData.setBackground(ST_DARK_GRAY);
                aprsStatus.setBackground(ST_DARK_GRAY);
                aprsStatus.setText("APRS");
                aprsButton.setSelected(false);
                abstractMap.showIcons(false);
            });
        }
    }

    private void stopCoverageTest() {
        fixedTimer.stop();
        variableTimer.stop();
        setCoverageTestActive(false);
        coverageTestObject.setAllowModifications(true);
        coverageTestObject.setShapesEnabled(false);
        invokeLaterInDispatchThreadIfNeeded(() -> {
            coverageTestButton.setSelected(false);
            setMeasurementPeriodText("");
            cursorMaidenheadReference.setToolTipText("Maidenhead Grid Square at Cursor");
            gpsPanelGridSquare.setToolTipText("Maidenhead Grid Square at GPS Location");
        });
    }

    private void stopGps() {
        try {
            gpsProcessor.stopGPS();
        } finally {
            invokeLaterInDispatchThreadIfNeeded(() -> {
                gpsPanelLatitude.setText("");
                gpsPanelLongitude.setText("");
                gpsPanelMGRS.setText("");
                gpsPanelGridSquare.setText("");
                gpsPanelSpeedMadeGood.setText("");
                gpsPanelAltitude.setText("");
                gpsStatus.setBackground(ST_DARK_GRAY);
                gpsStatus.setText("GPS");
                gpsCTS.setBackground(ST_DARK_GRAY);
                gpsDSR.setBackground(ST_DARK_GRAY);
                gpsCD.setBackground(ST_DARK_GRAY);
                gpsTxData.setBackground(ST_DARK_GRAY);
                gpsRxData.setBackground(ST_DARK_GRAY);
                gpsButton.setSelected(false);
                abstractMap.showGpsSymbol(false);
            });
        }
    }

    private void stopRadiationSensor() {
        if (radDetector != null) {
            radDetector.close();
        }
    }
    
    private void stopBlackGlobeTempSensor() {
        if (bgts != null) {
            bgts.close();
        }
    }

    private void stopNetworkEarthRotationService() {
        if (ners != null) {
            ners.close();
        }
    }

    private void stopAirNow() {
        if (airNow != null) {
            airNow.close();
        }
    }

    private void stopDateTimeService() {
        if (dts != null) {
            dts.close();
        }
    }

    private void stopEnvironmentSensor() {
        if (environmentSensor != null) {
            environmentSensor.close();
        }
    }

    private void stopSpaceWeatherProcessor() {
        if (swp != null) {
            swp.close();
        }
    }

    private void stopLearnMode() {
        if (!isCoverageTestActive()) {
            coverageTestObject.setAllowModifications(true);
            coverageTestObject.setShapesEnabled(false);
        }
        
        invokeLaterInDispatchThreadIfNeeded(() -> {
            learnModeButton.setSelected(false);
            doActionButton.setVisible(false);
            doActionButton.setText("");
            doActionButton.setToolTipText("");
            messageLabel.setText("");
        });
        abstractMap.setSelectionRectangleMode(false);
    }

    private void stopRadio() {
        try {
        	getAbstractRadio().getReceiverEvent().removePropertyChangeListener(radioListener);
            getAbstractRadio().stopRadio();
        } finally {
            invokeLaterInDispatchThreadIfNeeded(() -> {
                radioStatus.setBackground(ST_DARK_GRAY);
                radioStatus.setText("RADIO");
                radioCTS.setBackground(ST_DARK_GRAY);
                radioDSR.setBackground(ST_DARK_GRAY);
                radioCD.setBackground(ST_DARK_GRAY);
                radioTxData.setBackground(ST_DARK_GRAY);
                radioRxData.setBackground(ST_DARK_GRAY);
                radioButton.setSelected(false);
                averageBerInCurrentTile.setText("");
                averageSinadInCurrentTile.setText("");
                averagedBmInCurrentTile.setText("");
            });
            setSignalDisplay(TestMode.OFF, false);
        }
    }

    private void stopStaticAnalysis() {
        setStaticAnalysisActive(false);
        staticLocationAnalysisButton.setSelected(false);
        if ((staticSchedulerHandle != null)) {
            staticScheduler.shutdown();
        }
    }

    private synchronized void measurementRecordCountReadyEvent(final PropertyChangeEvent event) {
    	final int c = (int) event.getNewValue();
        if (c > 0) {
            coverageTestRestoreMonitor = new ProgressDialog(true, " ", "Downloading Coverage Test Data from Disk",
                    0, c, true, true);
        }
    }

    private void tileCompleteCountReadyEvent(final PropertyChangeEvent event) {
        tilesComplete = (int) event.getNewValue();
        invokeLaterInDispatchThreadIfNeeded(() -> tilesCompleted.setText(String.format(getLocale(), "%07d", tilesComplete)));
    }

    private void tileNotAccessableCountReadyEvent(final PropertyChangeEvent event) {
        coverageTestObject.setTilesNotAccessable((int) event.getNewValue());
    }

    private synchronized void tileDeletedEvent(final PropertyChangeEvent event) {
        invokeLaterInDispatchThreadIfNeeded(() -> {
            final int total = database.getTileRecordList().size();
            totalTiles.setText(String.format(getLocale(), "%07d", total));
            final var testTile = (TestTile) event.getNewValue();
            abstractMap.deleteTestTile(testTile);
            abstractMap.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        });
    }

    private void tileRecordCountReadyEvent(final PropertyChangeEvent event) {
        tileRecordCount = (Integer) event.getNewValue();
        if (isCoverageTestActive() && tileRecordCount == 0) {
            invokeLaterInDispatchThreadIfNeeded(
                    () -> JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(SignalTrack.this),
                            "Tiles must be selected before coverage test can begin", "Coverage Test Error",
                            JOptionPane.ERROR_MESSAGE));
            stopCoverageTest();
            return;
        }

        invokeLaterInDispatchThreadIfNeeded(() -> totalTiles.setText(String.format(getLocale(), "%07d", event.getNewValue())));
    }

    private synchronized void tileRecordRestoredEvent() {
        tileRecordRestoreProgress++;
        if (tileRecordRestoreProgress == tileRecordCount) {
            allTileRecordsReady();
        }
    }

    private synchronized TestTile getAssignedTestTile(Point2D lonLat) {
        final Point2D tileSize = coverageTestObject.getTileSizeArcSeconds();
        final Point2D gridRef = coverageTestObject.getGridReference();
        final Point2D gridSize = coverageTestObject.getGridSize();
        final Point2D corner = CoordinateUtils.lonLatFromAnywhereInTileToNorthWestCornerOfTile(lonLat, tileSize, gridRef, gridSize);
        return database.getTestTileWithThisNorthWestLonLat(corner);
    }

    private synchronized void tileAddedToTileTableEvent(final PropertyChangeEvent event) {
        final int total = database.getTileRecordList().size();
        final TestTile testTile = (TestTile) event.getNewValue();
        invokeLaterInDispatchThreadIfNeeded(() -> totalTiles.setText(String.format(getLocale(), "%07d", total)));
        final String str = String.format(getLocale(), "Tile # %1d appended to database: %2s", total,
                database.getConfig().getDatabaseFile().getName());
        LOG.log(Level.INFO, str);
        testTile.setColor(getTestTileColor(0));
        abstractMap.addTestTile(testTile);
        abstractMap.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        updateTestTileStats(testTile);
    }

    private void triangulationComplete() {
        final double ringSize = Math.max(pointStandardDeviation(triangulate.getIntersectList()) * 3000, 20);
        abstractMap.setTargetRing(triangulate.getIntersectPoint(), (int) ringSize, ST_BRIGHT_GREEN);
        abstractMap.showTargetRing(true);
    }

    private void updateAPRSSettings() {
        abstractMap.showIcons(getAprsProcessor().isEnableAPRSTracking());
        abstractMap.showIconLabels(getAprsProcessor().isEnableIconLabels());
    }

    public boolean isTestTileSelectMode() {
        return testGridLearnMode;
    }

    private void setTestGridLearnMode(final PropertyChangeEvent event) {
        setTestGridLearnMode((boolean) event.getNewValue());
    }

    private void setTestGridLearnMode(boolean testGridLearnMode) {
        this.testGridLearnMode = testGridLearnMode;
        if (testGridLearnMode) {
            invokeLaterInDispatchThreadIfNeeded(() -> coverageTestComponent.setVisible(false));
            if (isCoverageTestActive()) {
                invokeLaterInDispatchThreadIfNeeded(
                        () -> JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(SignalTrack.this),
                                "Learn mode can not be activated during a coverage test.", "Invalid Selection",
                                JOptionPane.ERROR_MESSAGE));
                stopLearnMode();
                return;
            }
            if (getDatabaseMode() == DatabaseMode.CLOSED) {
                invokeLaterInDispatchThreadIfNeeded(
                        () -> JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(SignalTrack.this),
                                "Please select an existing coverage test, or create a new test.", "File Not Open Error",
                                JOptionPane.ERROR_MESSAGE));
                if (openCoverageTestDatabaseFile() == JFileChooser.CANCEL_OPTION) {
                    stopLearnMode();
                    return;
                }
            }
            if ((getDataMode() != DataMode.RECORD) && (getDatabaseMode() == DatabaseMode.OPEN)) {
                setDataMode(DataMode.RECORD);
            }
            invokeLaterInDispatchThreadIfNeeded(() -> messageLabel.setText("       TEST GRID LEARN MODE ACTIVE"));
            abstractMap.setSelectionRectangleMode(false);
            coverageTestObject.setAllowModifications(false);
            coverageTestObject.setShapesEnabled(true);
        } else {
            stopLearnMode();
        }
    }

    public boolean isTestGridDrawOutlineMode() {
        return testGridSelectionMode;
    }

    private void setTestGridSelectionMode(final PropertyChangeEvent event) {
        setTestGridDrawOutlineMode((boolean) event.getNewValue());
    }

    // sets test grid boundaries
    private void setTestGridDrawOutlineMode(boolean testGridSelectionMode) {
        this.testGridSelectionMode = testGridSelectionMode;
        if (testGridSelectionMode) {
            invokeLaterInDispatchThreadIfNeeded(() -> {
                messageLabel.setText("       TEST GRID OUTLINE MODE");
                abstractMap.deleteTestGrid();
                doActionButton.setText("Apply Test Grid");
                doActionButton.setToolTipText("Press to apply test grid boundaries");
                doActionButton.setVisible(true);
                abstractMap.setSelectionRectangleMode(true);
                coverageTestComponent.setVisible(false);
                abstractMap.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
            });
        } else {
            invokeLaterInDispatchThreadIfNeeded(() -> abstractMap.setSelectionRectangleMode(false));
        }
    }

    private void updateCoverageTestComponents() {
        try {
            invokeLaterInDispatchThreadIfNeeded(() -> {
                abstractMap.setGrid(coverageTestObject.getTileSizeArcSeconds(),
                        coverageTestObject.getGridReference(), coverageTestObject.getGridSize());
                abstractMap.setGridColor(coverageTestObject.getGridColor());
                abstractMap.setSignalMarkerRadius(coverageTestObject.getSignalMarkerRadius());
                abstractMap.showTestTiles(coverageTestObject.isShowGridSquareShading() && databaseMode == DatabaseMode.OPEN);
                abstractMap.showLines(coverageTestObject.isShowLines() && databaseMode == DatabaseMode.OPEN);
                abstractMap.showRings(coverageTestObject.isShowRings() && databaseMode == DatabaseMode.OPEN);
                abstractMap.showQuads(coverageTestObject.isShowQuads() && databaseMode == DatabaseMode.OPEN);
                abstractMap.showSignalMarkers(
                        coverageTestObject.isShowSignalMarkers() && databaseMode == DatabaseMode.OPEN);
                abstractMap.showGrid(coverageTestObject.isShowGrid() && databaseMode == DatabaseMode.OPEN);
            });

            if (isCoverageTestActive() && coverageTestObject.getTimingMode() == TimingMode.FIXED) {
                fixedTimer.setDelay(coverageTestObject.getFixedTimePeriod());
                setMeasurementPeriodText(String.valueOf(coverageTestObject.getFixedTimePeriod()));
            }

            if (isCoverageTestActive() && coverageTestObject.getTimingMode() == TimingMode.VARIABLE) {
                setMeasurementPeriodText(String.valueOf(variableTimer.getDelay()));
            }
        } catch (final NullPointerException ex) {
            LOG.log(Level.WARNING, ex.getMessage(), ex);
        }
    }

    private void updateGPSSettings() {
        abstractMap.setGpsDotRadius(gpsProcessor.getGpsSymbolRadius());
    }

    private void updateMapSettings() {
        // TODO: Fix what happens when abstractMap is changed in the MapComponent menu.....
    }

    private void updateWindowSizeSpecificComponents() {
    	final long b = (long) abstractMap.getDisplayAltitude();
        invokeLaterInDispatchThreadIfNeeded(() -> viewingAltitude.setText(String.format(getLocale(), "%6d", b) + " FT"));
    }

    private void updateMGRSLabel(final Point2D mousePosition) {
        final SwingWorker<MGRSCoord, Void> worker = new SwingWorker<MGRSCoord, Void>() {
            @Override
            protected MGRSCoord doInBackground() throws Exception {
                return CoordinateUtils.lonLatToMGRS(mousePosition, 5);
            }

            @Override
            protected void done() {
                try {
                    if (!isMouseOffGlobe()) {
                        cursorMGRS.setText(get().toString());
                    }
                } catch (final InterruptedException ex) {
                    LOG.log(Level.WARNING, ex.getMessage(), ex);
                    Thread.currentThread().interrupt();
                } catch (final ExecutionException ex) {
                    LOG.log(Level.WARNING, ex.getMessage(), ex);
                }
            }
        };
        worker.execute();
    }

    private void updateStaticTestComponents() {
        invokeLaterInDispatchThreadIfNeeded(() -> {
            abstractMap.setTargetRingColor(staticTestObject.getTargetRingColor());
            abstractMap.showTargetRing(staticTestObject.isShowTargetRing());
            abstractMap.setArcAsymptoteColor(staticTestObject.getAsymptoteColor());
            abstractMap.showArcAsymptotes(staticTestObject.isShowAsymptotes());
            abstractMap.setArcTraceColor(staticTestObject.getTraceColor());
            abstractMap.setArcCursorColor(staticTestObject.getCursorColor());
            abstractMap.setArcCursorRadius(staticTestObject.getCursorDiameter());
            abstractMap.showArcCursors(staticTestObject.isShowCursors());
            abstractMap.setArcIntersectPointColor(staticTestObject.getIntersectPointColor());
            abstractMap.setArcIntersectPointSize(staticTestObject.getIntersectPointDiameter());
            abstractMap.showArcIntersectPoints(staticTestObject.isShowIntersectPoints());
            abstractMap.setTraceEqualsFlightColor(staticTestObject.isTraceEqualsFlightColor());
        });
    }

    private void updateTestTileStats(final TestTile testTile) {
        if (testTile == null) {
            return;
        }
        invokeLaterInDispatchThreadIfNeeded(() -> {
            if (getAbstractRadio().isRssiEnabled()) {
                averagedBmInCurrentTile.setText(String.format(getLocale(), "%3.1f", testTile.getAvgdBm()) + " dBm");
            } else {
                averagedBmInCurrentTile.setText("");
            }
            if (getAbstractRadio().isBerEnabled()) {
                averageBerInCurrentTile.setText(String.format(getLocale(), "%1.2f", testTile.getAvgBer()) + " %");
            } else {
                averageBerInCurrentTile.setText("");
            }
            if (getAbstractRadio().isSinadEnabled()) {
                averageSinadInCurrentTile.setText(String.format(getLocale(), "%2.1f", testTile.getAvgSinad()) + " dB");
            } else {
                averageSinadInCurrentTile.setText("");
            }
            tilesCompleted.setText(String.format(getLocale(), "%07d", tilesComplete));
        });
        if ((getDatabaseMode() != DatabaseMode.CLOSED) && (cursorTestTile != null)
                && !cursorTestTile.equalsLonLat(testTile)) {
            database.getTileMeasurementSetCount(measurementsThisTile, testTile);
            cursorTestTile = testTile;
        }
    }

    private TestMode getCoverageTestMode() {
        TestMode testMode = TestMode.MODE_NOT_SELECTED;
        if (getAbstractRadio().isRssiEnabled()) {
            if (getAbstractRadio().isBerEnabled()) {
                testMode = TestMode.RSSI_BER;
            } else if (getAbstractRadio().isSinadEnabled()) {
                testMode = TestMode.RSSI_SINAD;
            } else {
                testMode = TestMode.RSSI;
            }
        } else if (getAbstractRadio().isBerEnabled()) {
            testMode = TestMode.BER;
        } else if (getAbstractRadio().isSinadEnabled()) {
            testMode = TestMode.SINAD;
        }
        return testMode;
    }

    private void doActionButtonMousePressed() {
    	// The test grid outline has been drawn, and lower right point has been selected. Now it is to be submitted.
        if (isTestGridDrawOutlineMode()) { 
        	// update coverageTestComponent with drive test outer boundaries selection
            coverageTestComponent.setTestGridBoundaries(abstractMap.getSelectionRectangle()); 
            // draw the grid squares inside the boundaries
            abstractMap.setGrid(coverageTestObject.getTileSizeArcSeconds(),
                    coverageTestObject.getGridReference(), coverageTestObject.getGridSize());
            invokeLaterInDispatchThreadIfNeeded(() -> {
                if (coverageTestComponent != null && !learnModeButton.isSelected()) {
                	// show the coverageTestComponent grid squares
                    coverageTestComponent.setVisible(true);
                }
                // change the cursor back to an arrow
                abstractMap.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                // hide the button, and clear the message text, as the task of drawing the test grid is complete
                doActionButton.setVisible(false);
                messageLabel.setText("");
                doActionButton.setToolTipText("");
            });
            // show the grid
            coverageTestObject.showGrid(true);
            abstractMap.showGrid(coverageTestObject.isShowGrid());
            coverageTestObject.showGridSquareShading(true);
            // show any map shapes that have been selected
            abstractMap.setDisplayShapes(true);
            // we don't need to see the selection rectangle any longer
            abstractMap.setSelectionRectangleMode(false);
            setTestGridDrawOutlineMode(false);
            setMapDragged(false);
        }
        // User is selecting which tiles within the grid are to be tested. 
        if (isTestTileSelectMode()) {
        	// here we are creating a collection of the southwest coordinates the of all the tiles inside the boundary rectangle
            final List<Coordinate> coordinates = GeometryUtils.getSouthWestCoordinatesOfAllTestTilesWithinRectangle(abstractMap.getSelectionRectangle(), coverageTestObject.getGridList());
            // Now we are creating a blank test tile in the database for each member of the above collection.
            // The receiving method, "createBlankTestTile" 
            coordinates.forEach(this::createBlankTestTile);
            invokeLaterInDispatchThreadIfNeeded(() -> {
                doActionButton.setVisible(false);
                doActionButton.setToolTipText("");
                doActionButton.setText("");
                abstractMap.setSelectionRectangleMode(false);
                if (coverageTestComponent != null && !learnModeButton.isSelected()) {
                    coverageTestComponent.setVisible(true);
                }
            });
            setMapDragged(false);
        }
        if (isBulkMapTileDownloadSelectionMode()) {
            setBulkMapTileDownloadSelectionMode(false);
            abstractMap.setSelectionRectangleMode(false);
            invokeLaterInDispatchThreadIfNeeded(() -> {
                doActionButton.setVisible(false);
                doActionButton.setToolTipText("");
                doActionButton.setText("");
                messageLabel.setText("");
            });
            abstractMap.setTilesToDownload(abstractMap.getSelectionRectangle());
            setMapDragged(false);
        }
    }

    private void zoomInButtonMousePressed() {
        zoomInMouseDownTimer.start();
        doZoomIn();
    }

    private void zoomInButtonMouseReleased() {
        zoomInMouseDownTimer.stop();
    }

    private void zoomInMouseDownTimerActionListenerEvent() {
        doZoomIn();
    }

    private void zoomOutButtonMousePressed() {
        zoomOutMouseDownTimer.start();
        doZoomOut();
    }

    private void zoomOutButtonMouseReleased() {
        zoomOutMouseDownTimer.stop();
    }

    private void zoomOutMouseDownTimerActionListenerEvent() {
        doZoomOut();
    }

    public static void invokeAndWaitInDispatchThreadIfNeeded(Runnable runnable) {
        if (EventQueue.isDispatchThread()) {
            runnable.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(runnable);
            } catch (final InvocationTargetException ex) {
                LOG.log(Level.WARNING, ex.getMessage(), ex);
            } catch (final InterruptedException ex) {
                LOG.log(Level.WARNING, ex.getMessage(), ex);
                Thread.currentThread().interrupt();
            }
        }
    }

    private static void invokeLaterInDispatchThreadIfNeeded(Runnable runnable) {
        if (EventQueue.isDispatchThread()) {
            runnable.run();
        } else {
            SwingUtilities.invokeLater(runnable);
        }
    }

    public static void main(final String[] args) {
        EventQueue.invokeLater(() -> new SignalTrack(args));
    }

    private void createNormalGUI() {
    	final GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);

        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
        		.addGroup(GroupLayout.Alignment.TRAILING,
                layout.createSequentialGroup().addContainerGap().addGroup(layout
                        .createParallelGroup(GroupLayout.Alignment.TRAILING)
                        .addComponent(toolBar, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, true)
                                        .addComponent(mapPanel, GroupLayout.DEFAULT_SIZE, PREFERRED_MAP_SIZE.width,
                                                Short.MAX_VALUE)
                                        .addComponent(infoPanel, GroupLayout.DEFAULT_SIZE,
                                                PREFERRED_MAP_SIZE.width, Short.MAX_VALUE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                        .addComponent(gpsInfoPanel, 185, 185, 185)
                                        .addComponent(gpsCompassRose, 185, 185, 185)
                                        .addComponent(signalMeter, 185, 185, 185)
                                        .addComponent(commPanel, 185, 185, 185))))
                        .addContainerGap()));

        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout
                .createSequentialGroup().addContainerGap()
                .addComponent(toolBar, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                        .addGroup(layout.createSequentialGroup().addComponent(gpsInfoPanel, 230, 230, 230)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(gpsCompassRose, 188, 188, 188)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(signalMeter,
                                GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addComponent(mapPanel, GroupLayout.DEFAULT_SIZE, PREFERRED_MAP_SIZE.height,
                                Short.MAX_VALUE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(infoPanel, 78, 78, 78).addComponent(commPanel, 78, 78, 78))
                .addContainerGap()));
    }

    private class MeasurementCompiler implements Runnable {

        @Override
        public void run() {
            // add empty records to the running list if necessary and set default starting location to zero.
            for (Iterator<FlightInformation> it = database.getFlightRecordList().iterator(); it.hasNext();) {
            	final FlightInformation fi = it.next();
                if (!processorStartIndex.getT1List().contains(fi.getFlight())) {
                    processorStartIndex.add(fi.getFlight(), 0);
                }
            }

            // iterate through each flight, one at a time - as <flght, index>
            for (Iterator<TwoDimElement<Integer, Integer>> it = processorStartIndex.iterator(); it.hasNext();) {
            	final TwoDimElement<Integer, Integer> flight = it.next();
                // iterate through each StaticMeasurement in the flight, starting where we leftAction off
            	final List<StaticMeasurement> sm = database.getStaticRecordList().stream().filter(f -> f.getFlight() == flight.getT1()).toList();
                for (int i = flight.getT2(); i < sm.size(); i++) {
                    process(sm.get(i));
                }
            }
        }

        private void process(StaticMeasurement sm) {
            // Add position to flight path for display.
            abstractMap.addFlightPathMarker(sm.getPoint(), staticTestObject.getFlightColor()[sm.getFlight()]);

            // Get StaticMeasurmemnt list for the flight attached to the static measurement.
            // Retrieve current static measurement and also 100 most recent.
            // Look for presence of defined peak.
            // If there is a defined peak, then submit it.
        }

        @SuppressWarnings("unused")
        private void updateIntersectSolution(List<Point2D> points) {
            intersectList.addAll(points);
            final Point2D meanIntersect = pointMean(intersectList);
            if (meanIntersect == null) {
                return;
            }
            abstractMap.setTargetRing(meanIntersect, getTargetRingSize(intersectList),
                    staticTestObject.getTargetRingColor());
            abstractMap.showTargetRing(staticTestObject.isShowTargetRing());
            intersectList.forEach(
                    p -> abstractMap.addArcIntersectPoint(p, staticTestObject.getIntersectPointDiameter(),
                            staticTestObject.getIntersectPointColor()));
            abstractMap.showArcIntersectPoints(staticTestObject.isShowIntersectPoints());
        }

        private double getTargetRingSize(List<Point2D> intersectList) {
            if (intersectList == null) {
                return 0;
            }
            final double stdDev = pointStandardDeviation(intersectList);
            return ((stdDev + 0.25) * 100.0) + (500.0 / (intersectList.size() * 5));
        }
    }

    public TwoDimArrayList<FlightInformation, StaticMeasurement> getFlightMeasurementArray() {
        return flightMeasurementArray;
    }

    private class Demo implements Runnable {

        @Override
        public void run() {
            try {
                if (getDatabaseMode() == DatabaseMode.CLOSED) {
                    createNewDatabase(null);
                }

                while (getDatabaseMode() != DatabaseMode.OPEN) {
                    Thread.sleep(100);
                }

                setDataMode(DataMode.RECORD);
                Thread.sleep(100);

                while (!isMapReady()) {
                    Thread.sleep(100);
                }

                startStaticAnalysis();

                abstractMap.setCenterLonLat(TEST_POINT);

                final double y1 = 100;
                final double x1 = 50;
                final double r1 = RFPath.pyth(x1, y1);
                final double t1 = Math.toDegrees(Math.atan(x1 / y1));

                final double y2 = 20;
                final double x2 = 50;
                final double r2 = RFPath.pyth(x2, y2);
                final double t2 = Math.toDegrees(Math.atan(x2 / y2));

                final Point2D pa = Vincenty.getVincentyDirect(TEST_POINT, t1, r1).getDestinationPoint();
                final Point2D pb = Vincenty.getVincentyDirect(TEST_POINT, t1, r1 - 80).getDestinationPoint();

                LOG.log(Level.INFO, "Demo: t1 {0} t2: {1}", new Object[]{t1, t2});

                final Point2D pc = Vincenty.getVincentyDirect(TEST_POINT, t1, r1).getDestinationPoint();
                final Point2D pd = Vincenty.getVincentyDirect(TEST_POINT, t2, r2).getDestinationPoint();

                LOG.log(Level.INFO, "Demo: t3 {0} t4: {1}", new Object[]{t1, t1});

                final double altFeet = 0;

                // get distances between test point and measurement locations
                final double da = Vincenty.getVincentyInverse(TEST_POINT, pa).getDistanceMeters();
                final double db = Vincenty.getVincentyInverse(TEST_POINT, pb).getDistanceMeters();
                final double dc = Vincenty.getVincentyInverse(TEST_POINT, pc).getDistanceMeters();
                final double dd = Vincenty.getVincentyInverse(TEST_POINT, pd).getDistanceMeters();

                LOG.log(Level.INFO, "Demo: simulated meters from measurement a to test point {0}", da);
                LOG.log(Level.INFO, "Demo: simulated meters from measurement b to test point {0}", db);
                LOG.log(Level.INFO, "Demo: simulated meters from measurement c to test point {0}", dc);
                LOG.log(Level.INFO, "Demo: simulated meters from measurement d to test point {0}", dd);

                // get signal strength in dBm to test point
                final double ka = TEST_POINT_DBM - RFPath.getFreeSpacePathLossdB(da, 100);
                final double kb = TEST_POINT_DBM - RFPath.getFreeSpacePathLossdB(db, 100);
                final double kc = TEST_POINT_DBM - RFPath.getFreeSpacePathLossdB(dc, 100);
                final double kd = TEST_POINT_DBM - RFPath.getFreeSpacePathLossdB(dd, 100);

                LOG.log(Level.INFO, "simulated dBm for point a {0}", ka);
                LOG.log(Level.INFO, "simulated dBm for point b {0}", kb);
                LOG.log(Level.INFO, "simulated dBm for point c {0}", kc);
                LOG.log(Level.INFO, "simulated dBm for point d {0}", kd);

                final double h1 = Vincenty.getVincentyInverse(pa, pb).getFinalBearing();
                final double h2 = Vincenty.getVincentyInverse(pc, pd).getFinalBearing();

                database.appendFlightRecord(new FlightInformation(1));

                while (flightInformationFlightNumber != 1) {
                    Thread.sleep(100);
                }

                long td = consolidatedTime.getBestTimeInMillis();

                processStaticMeasurement(new StaticMeasurement(testName, pa, td, h1, TEST_SPEED_KPH, altFeet,
                        altFeet, altFeet, 100, ka, StandardModeName.CW, 1));
                processStaticMeasurement(new StaticMeasurement(testName, pb, td, h1, TEST_SPEED_KPH, altFeet,
                        altFeet, altFeet, 100, kb, StandardModeName.CW, 1));

                database.appendFlightRecord(new FlightInformation(2));

                while (flightInformationFlightNumber != 2) {
                    Thread.sleep(100);
                }

                td = consolidatedTime.getBestTimeInMillis();

                processStaticMeasurement(new StaticMeasurement(testName, pc, td, h2, TEST_SPEED_KPH, altFeet,
                        altFeet, altFeet, 100, kc, StandardModeName.CW, 2));
                processStaticMeasurement(new StaticMeasurement(testName, pd, td, h2, TEST_SPEED_KPH, altFeet,
                        altFeet, altFeet, 100, kd, StandardModeName.CW, 2));

            } catch (final InterruptedException ex) {
                LOG.log(Level.WARNING, ex.getMessage(), ex);
                Thread.currentThread().interrupt();
            }
        }

    }

    protected void processStaticMeasurement(StaticMeasurement sm) {
        if (isStaticAnalysisActive() && (getDataMode() == DataMode.RECORD)) {
            database.appendStaticRecord(sm);
        }
    }

    private synchronized void dynamicToolTipUpdate(JLabel label, String text) {
        label.setToolTipText(text);
        repaint();
        final Point locationOnScreen = MouseInfo.getPointerInfo().getLocation();
        final Point locationOnComponent = new Point(locationOnScreen);
        SwingUtilities.convertPointFromScreen(locationOnComponent, label);
        if (label.contains(locationOnComponent)) {
            ToolTipManager.sharedInstance().mouseMoved(new MouseEvent(label, -1, System.currentTimeMillis(), 0,
                    locationOnComponent.x, locationOnComponent.y, locationOnScreen.x, locationOnScreen.y, 0, false, 0));
        }
    }

    public boolean isCoverageTestActive() {
        return coverageTestActive;
    }

    public void setCoverageTestActive(boolean coverageTestActive) {
        this.coverageTestActive = coverageTestActive;
    }

    public boolean isDopplerActive() {
        return dopplerActive;
    }

    public void setDopplerActive(boolean dopplerActive) {
        this.dopplerActive = dopplerActive;
    }

    public boolean isStartInDemoMode() {
        return startInDemoMode;
    }

    public void setStartInDemoMode(boolean startInDemoMode) {
        this.startInDemoMode = startInDemoMode;
    }

    public boolean isShowCPULoad() {
        return showCPULoad;
    }

    public void setShowCPULoad(boolean showCPULoad) {
        this.showCPULoad = showCPULoad;
    }

    public boolean isMeasureMode() {
        return measureMode;
    }

    public void setMeasureMode(boolean measureMode) {
        this.measureMode = measureMode;
        measureButton.setSelected(measureMode);
    }

    public boolean isBulkMapTileDownloadSelectionMode() {
        return bulkDownloadSelectionMode;
    }

    public void setBulkMapTileDownloadSelectionMode(boolean bulkDownloadSelectionMode) {
        this.bulkDownloadSelectionMode = bulkDownloadSelectionMode;
    }

    public boolean isSerialErrorQueued() {
        return serialErrorQueued;
    }

    public void setSerialErrorQueued(boolean serialErrorQueued) {
        this.serialErrorQueued = serialErrorQueued;
    }

    public boolean isStaticAnalysisActive() {
        return staticAnalysisActive;
    }

    public boolean isCursorBearingSet() {
        return cursorBearingSet;
    }

    public boolean isMapDragged() {
        return mapDragged;
    }

    public boolean isMapReady() {
        return mapReady;
    }

    public boolean isAltKeyDown() {
        return altKeyDown;
    }

    public void setStaticAnalysisActive(boolean staticAnalysisActive) {
        this.staticAnalysisActive = staticAnalysisActive;
    }

    public void setCursorBearingSet(boolean cursorBearingSet) {
        this.cursorBearingSet = cursorBearingSet;
    }

    public void setMapDragged(boolean mapDragged) {
        this.mapDragged = mapDragged;
    }

    public void setMapReady(boolean mapReady) {
        this.mapReady = mapReady;
    }

    public void setAltKeyDown(boolean altKeyDown) {
        this.altKeyDown = altKeyDown;
    }

    public boolean isClearAllPrefs() {
        return clearAllPrefs;
    }

    private void setClearAllPrefs(boolean clearAllPrefs) {
        this.clearAllPrefs = clearAllPrefs;
    }

    public boolean isInhibitLeftMouseButtonReleaseEvent() {
        return inhibitLeftMouseButtonReleaseEvent;
    }

    public void setInhibitLeftMouseButtonReleaseEvent(boolean inhibitLeftMouseButtonReleaseEvent) {
        this.inhibitLeftMouseButtonReleaseEvent = inhibitLeftMouseButtonReleaseEvent;
    }

    private boolean isMouseOffGlobe() {
        return mouseOffGlobe;
    }

    public DatabaseMode getDatabaseMode() {
        return databaseMode;
    }

    public DataMode getDataMode() {
        return dataMode;
    }

    private JPanel getGpsInfoPanel() {
    	final JPanel panel = new JPanel();

        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(EtchedBorder.LOWERED, Color.LIGHT_GRAY, Color.GRAY), "GPS Information",
                TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Calabri", Font.BOLD, 9)));
        panel.setOpaque(false);
        panel.setDoubleBuffered(true);

        gpsPanelLongitude.setFont(new Font("Calabri", Font.BOLD, 16));
        gpsPanelLongitude.setHorizontalAlignment(SwingConstants.CENTER);
        gpsPanelLongitude.setVerticalAlignment(SwingConstants.CENTER);
        gpsPanelLongitude.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Longitude", TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION, new Font("Calabri", Font.PLAIN, 9)));
        gpsPanelLongitude.setToolTipText("GPS Longitude (WGS84)");

        gpsPanelLatitude.setFont(new Font("Calabri", Font.BOLD, 16));
        gpsPanelLatitude.setHorizontalAlignment(SwingConstants.CENTER);
        gpsPanelLatitude.setVerticalAlignment(SwingConstants.CENTER);
        gpsPanelLatitude.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Latitude", TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION, new Font("Calabri", Font.PLAIN, 9)));
        gpsPanelLatitude.setToolTipText("GPS Latitude (WGS84)");

        gpsPanelMGRS.setFont(new Font("Calabri", Font.BOLD, 12));
        gpsPanelMGRS.setHorizontalAlignment(SwingConstants.CENTER);
        gpsPanelMGRS.setVerticalAlignment(SwingConstants.CENTER);
        gpsPanelMGRS.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "MGRS", TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION, new Font("Calabri", Font.PLAIN, 9)));
        gpsPanelMGRS.setToolTipText("Military Grid Reference System (MGRS) Position at GPS Location");

        gpsPanelGridSquare.setFont(new Font("Calabri", Font.BOLD, 12));
        gpsPanelGridSquare.setHorizontalAlignment(SwingConstants.CENTER);
        gpsPanelGridSquare.setVerticalAlignment(SwingConstants.CENTER);
        gpsPanelGridSquare.setToolTipText("Maidenhead Grid Square at GPS Location");

        gpsPanelGridSquare.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Maidenhead",
                        TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                        new Font("Calabri", Font.PLAIN, 9))));

        gpsPanelAltitude.setFont(new Font("Calabri", Font.BOLD, 12));
        gpsPanelAltitude.setHorizontalAlignment(SwingConstants.CENTER);
        gpsPanelAltitude.setVerticalAlignment(SwingConstants.CENTER);
        gpsPanelAltitude.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Altitude", TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION, new Font("Calabri", Font.PLAIN, 9)));
        gpsPanelAltitude.setToolTipText("Altitude at GPS Location (Feet)");

        gpsPanelSpeedMadeGood.setFont(new Font("Calabri", Font.BOLD, 12));
        gpsPanelSpeedMadeGood.setHorizontalAlignment(SwingConstants.CENTER);
        gpsPanelSpeedMadeGood.setVerticalAlignment(SwingConstants.CENTER);
        gpsPanelSpeedMadeGood.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Speed Made Good",
                TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Calabri", Font.PLAIN, 9)));
        gpsPanelSpeedMadeGood.setToolTipText("Speed Made Good - Distance Between Points / Time Between Points");

        final GroupLayout gpsInfoPanelLayout = new GroupLayout(panel);

        panel.setLayout(gpsInfoPanelLayout);

        gpsInfoPanelLayout.setHorizontalGroup(gpsInfoPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(gpsInfoPanelLayout.createSequentialGroup().addContainerGap().addGroup(gpsInfoPanelLayout
                        .createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(gpsPanelLatitude, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE)
                        .addComponent(gpsPanelLongitude, GroupLayout.DEFAULT_SIZE, 157, Short.MAX_VALUE)
                        .addComponent(gpsPanelMGRS, GroupLayout.DEFAULT_SIZE, 157, Short.MAX_VALUE)
                        .addComponent(gpsPanelSpeedMadeGood, GroupLayout.DEFAULT_SIZE, 157, Short.MAX_VALUE)
                        .addGroup(gpsInfoPanelLayout.createSequentialGroup()
                                .addComponent(gpsPanelGridSquare, GroupLayout.PREFERRED_SIZE, 79,
                                        GroupLayout.PREFERRED_SIZE)
                                .addGap(2).addComponent(gpsPanelAltitude, GroupLayout.PREFERRED_SIZE, 79,
                                GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap()));

        gpsInfoPanelLayout.setVerticalGroup(gpsInfoPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(gpsInfoPanelLayout.createSequentialGroup().addContainerGap()
                        .addComponent(gpsPanelLatitude, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE)
                        .addComponent(gpsPanelLongitude, GroupLayout.PREFERRED_SIZE, 39,
                                GroupLayout.PREFERRED_SIZE)
                        .addComponent(gpsPanelMGRS, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE)
                        .addGroup(gpsInfoPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(gpsPanelGridSquare, GroupLayout.PREFERRED_SIZE, 39,
                                        GroupLayout.PREFERRED_SIZE)
                                .addComponent(gpsPanelAltitude, GroupLayout.PREFERRED_SIZE, 39,
                                        GroupLayout.PREFERRED_SIZE))
                        .addComponent(gpsPanelSpeedMadeGood, GroupLayout.PREFERRED_SIZE, 39,
                                GroupLayout.PREFERRED_SIZE)
                        .addContainerGap()));

        return panel;
    }

    private JPanel getCommPanel() {
    	final JPanel panel = new JPanel();

        panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED, Color.LIGHT_GRAY, Color.GRAY));
        panel.setOpaque(false);
        panel.setDoubleBuffered(true);

        gpsStatus.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        gpsStatus.setHorizontalAlignment(SwingConstants.CENTER);
        gpsStatus.setFont(new Font("Calabri", Font.BOLD, 10));
        gpsStatus.setOpaque(true);
        gpsStatus.setToolTipText("GPS Receive Status");
        gpsStatus.setBackground(ST_DARK_GRAY);
        gpsStatus.setText("GPS");

        gpsTxData.setBackground(ST_DARK_GRAY);
        gpsTxData.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        gpsTxData.setOpaque(true);
        gpsTxData.setToolTipText("GPS Port - Transmitted Data");

        gpsRxData.setBackground(ST_DARK_GRAY);
        gpsRxData.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        gpsRxData.setOpaque(true);
        gpsRxData.setToolTipText("GPS Port - Received Data");

        gpsCTS.setBackground(ST_DARK_GRAY);
        gpsCTS.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        gpsCTS.setOpaque(true);
        gpsCTS.setToolTipText("GPS Port - Clear To Send Line Active");

        gpsDSR.setBackground(ST_DARK_GRAY);
        gpsDSR.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        gpsDSR.setOpaque(true);
        gpsDSR.setToolTipText("GPS Port - Data Set Ready Line Active");

        gpsCD.setBackground(ST_DARK_GRAY);
        gpsCD.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        gpsCD.setOpaque(true);
        gpsCD.setToolTipText("GPS Port - Carrier Detect Line Active");

        panel.add(gpsStatus);
        panel.add(gpsTxData);
        panel.add(gpsRxData);
        panel.add(gpsCTS);
        panel.add(gpsDSR);
        panel.add(gpsCD);

        radioStatus.setBackground(ST_DARK_GRAY);
        radioStatus.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        radioStatus.setHorizontalAlignment(SwingConstants.CENTER);
        radioStatus.setFont(new Font("Calabri", Font.BOLD, 10));
        radioStatus.setText("RADIO");
        radioStatus.setOpaque(true);
        radioStatus.setToolTipText("Radio Receive Status");

        radioTxData.setBackground(ST_DARK_GRAY);
        radioTxData.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        radioTxData.setOpaque(true);
        radioTxData.setToolTipText("Radio Port - Transmitted Data");

        radioRxData.setBackground(ST_DARK_GRAY);
        radioRxData.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        radioRxData.setOpaque(true);
        radioRxData.setToolTipText("Radio Port - Received Data");

        radioCTS.setBackground(ST_DARK_GRAY);
        radioCTS.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        radioCTS.setOpaque(true);
        radioCTS.setToolTipText("Radio Port - Clear To Send Line Active");

        radioDSR.setBackground(ST_DARK_GRAY);
        radioDSR.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        radioDSR.setOpaque(true);
        radioDSR.setToolTipText("Radio Port - Data Set Ready Line Active");

        radioCD.setBackground(ST_DARK_GRAY);
        radioCD.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        radioCD.setOpaque(true);
        radioCD.setToolTipText("Radio Port - Carrier Detect Line Active");

        panel.add(radioStatus);
        panel.add(radioTxData);
        panel.add(radioRxData);
        panel.add(radioCTS);
        panel.add(radioDSR);
        panel.add(radioCD);

        aprsStatus.setBackground(ST_DARK_GRAY);
        aprsStatus.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        aprsStatus.setHorizontalAlignment(SwingConstants.CENTER);
        aprsStatus.setFont(new Font("Calabri", Font.BOLD, 10));
        aprsStatus.setText("APRS");
        aprsStatus.setOpaque(true);
        aprsStatus.setToolTipText("APRS Receive Status");

        aprsTxData.setBackground(ST_DARK_GRAY);
        aprsTxData.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        aprsTxData.setOpaque(true);
        aprsTxData.setToolTipText("APRS Port - Transmitted Data");

        aprsRxData.setBackground(ST_DARK_GRAY);
        aprsRxData.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        aprsRxData.setOpaque(true);
        aprsRxData.setToolTipText("APRS Port - Received Data");

        aprsCTS.setBackground(ST_DARK_GRAY);
        aprsCTS.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        aprsCTS.setOpaque(true);
        aprsCTS.setToolTipText("APRSr Port - Clear To Send Line Active");

        aprsDSR.setBackground(ST_DARK_GRAY);
        aprsDSR.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        aprsDSR.setOpaque(true);
        aprsDSR.setToolTipText("APRSr Port - Data Set Ready Line Active");

        aprsCD.setBackground(ST_DARK_GRAY);
        aprsCD.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        aprsCD.setOpaque(true);
        aprsCD.setToolTipText("APRS Port - Carrier Detect Line Active");

        panel.add(aprsStatus);
        panel.add(aprsTxData);
        panel.add(aprsRxData);
        panel.add(aprsCTS);
        panel.add(aprsDSR);
        panel.add(aprsCD);

        final GroupLayout commPanelLayout = new GroupLayout(panel);

        panel.setLayout(commPanelLayout);

        commPanelLayout.setHorizontalGroup(commPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(commPanelLayout.createSequentialGroup().addContainerGap().addGroup(commPanelLayout
                        .createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(commPanelLayout.createSequentialGroup().addComponent(aprsStatus, 55, 55, 55)
                                .addGap(3).addComponent(aprsTxData, 18, 18, 18).addGap(3)
                                .addComponent(aprsRxData, 18, 18, 18).addGap(3)
                                .addComponent(aprsDSR, 18, 18, 18).addGap(3).addComponent(aprsCTS, 18, 18, 18)
                                .addGap(3).addComponent(aprsCD, 18, 18, 18))
                        .addGroup(commPanelLayout.createSequentialGroup().addComponent(radioStatus, 55, 55, 55)
                                .addGap(3).addComponent(radioTxData, 18, 18, 18).addGap(3)
                                .addComponent(radioRxData, 18, 18, 18).addGap(3)
                                .addComponent(radioDSR, 18, 18, 18).addGap(3)
                                .addComponent(radioCTS, 18, 18, 18).addGap(3)
                                .addComponent(radioCD, 18, 18, 18))
                        .addGroup(commPanelLayout.createSequentialGroup().addComponent(gpsStatus, 55, 55, 55)
                                .addGap(3).addComponent(gpsTxData, 18, 18, 18).addGap(3)
                                .addComponent(gpsRxData, 18, 18, 18).addGap(3)
                                .addComponent(gpsDSR, 18, 18, 18).addGap(3).addComponent(gpsCTS, 18, 18, 18)
                                .addGap(3).addComponent(gpsCD, 18, 18, 18)))
                        .addContainerGap()));

        commPanelLayout.setVerticalGroup(commPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(commPanelLayout.createSequentialGroup().addContainerGap()
                        .addGroup(commPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(aprsStatus, 18, 18, 18).addComponent(aprsTxData, 18, 18, 18)
                                .addComponent(aprsRxData, 18, 18, 18).addComponent(aprsDSR, 18, 18, 18)
                                .addComponent(aprsCTS, 18, 18, 18).addComponent(aprsCD, 18, 18, 18))
                        .addGap(4)
                        .addGroup(commPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(radioStatus, 18, 18, 18).addComponent(radioTxData, 18, 18, 18)
                                .addComponent(radioRxData, 18, 18, 18).addComponent(radioDSR, 18, 18, 18)
                                .addComponent(radioCTS, 18, 18, 18).addComponent(radioCD, 18, 18, 18))
                        .addGap(4)
                        .addGroup(commPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(gpsStatus, 18, 18, 18).addComponent(gpsTxData, 18, 18, 18)
                                .addComponent(gpsRxData, 18, 18, 18).addComponent(gpsDSR, 18, 18, 18)
                                .addComponent(gpsCTS, 18, 18, 18).addComponent(gpsCD, 18, 18, 18))
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        return panel;
    }

    private JPanel getMapPanel() {
    	final JPanel panel = new JPanel();

        panel.setDoubleBuffered(true);
        panel.setOpaque(true);
        panel.setBackground(Color.BLACK);
        panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED, Color.LIGHT_GRAY, Color.GRAY));

        final GroupLayout mapPanelLayout = new GroupLayout(panel);
        panel.setLayout(mapPanelLayout);

        mapPanelLayout.setHorizontalGroup(
                mapPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(abstractMap));

        mapPanelLayout.setVerticalGroup(
                mapPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(abstractMap));

        return panel;
    }

    private JPanel getInfoPanel() {
    	final JPanel panel = new JPanel();

        panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED, Color.LIGHT_GRAY, Color.GRAY));
        panel.setOpaque(false);
        panel.setDoubleBuffered(true);

        for (var i = 0; i < signalQuality.length; i++) {
            signalQuality[i] = new JLabel();
            signalQuality[i].setBorder(BorderFactory.createLineBorder(Color.GRAY));
            signalQuality[i].setHorizontalAlignment(SwingConstants.CENTER);
            signalQuality[i].setFont(new Font("Calabri", Font.PLAIN, 11));
            signalQuality[i].setToolTipText("RSSI (dBm) of Channel " + i);
            signalQuality[i].setText("");
            panel.add(signalQuality[i]);
        }

        markerID.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        markerID.setHorizontalAlignment(SwingConstants.CENTER);
        markerID.setVerticalAlignment(SwingConstants.CENTER);
        markerID.setFont(new Font("Calabri", Font.PLAIN, 11));
        markerID.setToolTipText("Record Marker");
        panel.add(markerID);

        recordCountLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        recordCountLabel.setHorizontalAlignment(SwingConstants.CENTER);
        recordCountLabel.setFont(new Font("Calabri", Font.PLAIN, 11));
        recordCountLabel.setToolTipText("Record Count");
        panel.add(recordCountLabel);

        cursorMGRS.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        cursorMGRS.setHorizontalAlignment(SwingConstants.CENTER);
        cursorMGRS.setFont(new Font("Calabri", Font.PLAIN, 11));
        cursorMGRS.setToolTipText("MGRS Location of Cursor");
        panel.add(cursorMGRS);

        measurementPeriod.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        measurementPeriod.setHorizontalAlignment(SwingConstants.CENTER);
        measurementPeriod.setFont(new Font("Calabri", Font.PLAIN, 11));
        measurementPeriod.setToolTipText("Measurement Timer Period in Milliseconds");
        panel.add(measurementPeriod);

        measurementsThisTile.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        measurementsThisTile.setHorizontalAlignment(SwingConstants.CENTER);
        measurementsThisTile.setVerticalAlignment(SwingConstants.CENTER);
        measurementsThisTile.setFont(new Font("Calabri", Font.PLAIN, 11));
        measurementsThisTile.setToolTipText("Measurements Taken in This Tile");
        panel.add(measurementsThisTile);

        tilesCompleted.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        tilesCompleted.setHorizontalAlignment(SwingConstants.CENTER);
        tilesCompleted.setVerticalAlignment(SwingConstants.CENTER);
        tilesCompleted.setFont(new Font("Calabri", Font.PLAIN, 11));
        tilesCompleted.setToolTipText("Total Number of Fully Measured Tiles");
        panel.add(tilesCompleted);

        totalTiles.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        totalTiles.setHorizontalAlignment(SwingConstants.CENTER);
        totalTiles.setVerticalAlignment(SwingConstants.CENTER);
        totalTiles.setFont(new Font("Calabri", Font.PLAIN, 11));
        totalTiles.setToolTipText("Total Number of Tiles Designated for Testing");
        panel.add(totalTiles);

        averagedBmInCurrentTile.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        averagedBmInCurrentTile.setHorizontalAlignment(SwingConstants.CENTER);
        averagedBmInCurrentTile.setVerticalAlignment(SwingConstants.CENTER);
        averagedBmInCurrentTile.setFont(new Font("Calabri", Font.PLAIN, 11));
        averagedBmInCurrentTile.setToolTipText("Average RSSI in Current Tile");
        panel.add(averagedBmInCurrentTile);

        averageBerInCurrentTile.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        averageBerInCurrentTile.setHorizontalAlignment(SwingConstants.CENTER);
        averageBerInCurrentTile.setFont(new Font("Calabri", Font.PLAIN, 11));
        averageBerInCurrentTile.setToolTipText("Average BER in Current Tile");
        panel.add(averageBerInCurrentTile);

        averageSinadInCurrentTile.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        averageSinadInCurrentTile.setHorizontalAlignment(SwingConstants.CENTER);
        averageSinadInCurrentTile.setFont(new Font("Calabri", Font.PLAIN, 11));
        averageSinadInCurrentTile.setToolTipText("Average SINAD in Current Tile");
        panel.add(averageSinadInCurrentTile);

        minMeasurementsPerTile.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        minMeasurementsPerTile.setHorizontalAlignment(SwingConstants.LEFT);
        minMeasurementsPerTile.setFont(new Font("Calabri", Font.PLAIN, 11));
        minMeasurementsPerTile.setToolTipText("Minimum Measurements Required to Complete Tile");
        panel.add(minMeasurementsPerTile);

        maxMeasurementsPerTile.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        maxMeasurementsPerTile.setHorizontalAlignment(SwingConstants.LEFT);
        maxMeasurementsPerTile.setFont(new Font("Calabri", Font.PLAIN, 11));
        maxMeasurementsPerTile.setToolTipText("Maximum Measurements Allowed Per Tile");
        panel.add(maxMeasurementsPerTile);

        cursorMaidenheadReference.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        cursorMaidenheadReference.setHorizontalAlignment(SwingConstants.LEFT);
        cursorMaidenheadReference.setFont(new Font("Calabri", Font.PLAIN, 11));
        cursorMaidenheadReference.setToolTipText("Maidenhead Grid Square at Cursor");
        cursorMaidenheadReference.setText(" GRID SQ:");
        panel.add(cursorMaidenheadReference);

        cursorLatitude.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        cursorLatitude.setHorizontalAlignment(SwingConstants.CENTER);
        cursorLatitude.setFont(new Font("Calabri", Font.PLAIN, 11));
        cursorLatitude.setToolTipText("Latitude at Mouse Cursor");
        panel.add(cursorLatitude);

        cursorLongitude.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        cursorLongitude.setHorizontalAlignment(SwingConstants.CENTER);
        cursorLongitude.setFont(new Font("Calabri", Font.PLAIN, 11));
        cursorLongitude.setToolTipText("Longitude at Mouse Cursor");
        panel.add(cursorLongitude);

        logFileNameLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        logFileNameLabel.setHorizontalAlignment(SwingConstants.LEFT);
        logFileNameLabel.setFont(new Font(DEFAULT_COURIER_FONT_NAME, Font.PLAIN, 11));
        logFileNameLabel.setToolTipText("Log File Name");
        panel.add(logFileNameLabel);

        cursorTerrainElevationFeet.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        cursorTerrainElevationFeet.setHorizontalAlignment(SwingConstants.CENTER);
        cursorTerrainElevationFeet.setFont(new Font("Calabri", Font.PLAIN, 11));
        cursorTerrainElevationFeet.setToolTipText("Height of Terrain Above Mean Sea Level at Cursor");
        panel.add(cursorTerrainElevationFeet);

        viewingAltitude.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        viewingAltitude.setHorizontalAlignment(SwingConstants.CENTER);
        viewingAltitude.setFont(new Font("Calabri", Font.PLAIN, 11));
        viewingAltitude.setToolTipText("Viewing Altitude");
        panel.add(viewingAltitude);

        utcLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        utcLabel.setFont(new Font("Calabri", Font.PLAIN, 10));
        utcLabel.setToolTipText("Universal Coordinated Time");
        utcLabel.setOpaque(true);
        utcLabel.setHorizontalAlignment(SwingConstants.LEFT);
        panel.add(utcLabel);

        final GroupLayout infoPanelLayout = new GroupLayout(panel);

        panel.setLayout(infoPanelLayout);

        infoPanelLayout.setHorizontalGroup(infoPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(infoPanelLayout.createSequentialGroup().addGap(1)
                        .addGroup(infoPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addGroup(infoPanelLayout.createSequentialGroup()
                                        .addGap(3)
                                        .addComponent(cursorLatitude, 90, 90, 90)
                                        .addGap(3)
                                        .addComponent(cursorLongitude, 90, 90, 90)
                                        .addGap(3)
                                        .addComponent(cursorMGRS, 144, 144, 144)
                                        .addGap(3)
                                        .addComponent(cursorMaidenheadReference, 126, 126, 126)
                                        .addGap(3)
                                        .addComponent(viewingAltitude, 85, 85, 85)
                                        .addGap(3)
                                        .addComponent(cursorTerrainElevationFeet, 85, 85, 85)
                                        .addGap(3)
                                        .addComponent(utcLabel, 148, 148, 148))
                                .addGroup(infoPanelLayout.createSequentialGroup().addGap(3)
                                        .addComponent(logFileNameLabel, 154, 154, 154).addGap(3)
                                        .addComponent(measurementPeriod, 46, 46, 46).addGap(3)
                                        .addComponent(measurementsThisTile, 53, 53, 53).addGap(3)
                                        .addComponent(minMeasurementsPerTile, 53, 53, 53).addGap(3)
                                        .addComponent(maxMeasurementsPerTile, 53, 53, 53).addGap(3)
                                        .addComponent(tilesCompleted, 63, 63, 63).addGap(3)
                                        .addComponent(totalTiles, 63, 63, 63).addGap(3)
                                        .addComponent(averagedBmInCurrentTile, 64, 64, 64).addGap(3)
                                        .addComponent(averageBerInCurrentTile, 64, 64, 64).addGap(3)
                                        .addComponent(averageSinadInCurrentTile, 64, 64, 64).addGap(3)
                                        .addComponent(recordCountLabel, 80, 80, 80))
                                .addGroup(infoPanelLayout.createSequentialGroup().addGap(3)
                                        .addComponent(signalQuality[0], 70, 70, 70).addGap(3)
                                        .addComponent(signalQuality[1], 70, 70, 70).addGap(3)
                                        .addComponent(signalQuality[2], 70, 70, 70).addGap(3)
                                        .addComponent(signalQuality[3], 70, 70, 70).addGap(3)
                                        .addComponent(signalQuality[4], 70, 70, 70).addGap(3)
                                        .addComponent(signalQuality[5], 70, 70, 70).addGap(3)
                                        .addComponent(signalQuality[6], 70, 70, 70).addGap(3)
                                        .addComponent(signalQuality[7], 70, 70, 70).addGap(3)
                                        .addComponent(signalQuality[8], 70, 70, 70).addGap(3)
                                        .addComponent(signalQuality[9], 70, 70, 70).addGap(3)
                                        .addComponent(markerID, 57, 57, 57)))
                        .addContainerGap()));

        infoPanelLayout.setVerticalGroup(infoPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(infoPanelLayout.createSequentialGroup().addContainerGap()
                        .addGroup(infoPanelLayout.createSequentialGroup()
                                .addGroup(infoPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(signalQuality[0], 18, 18, 18)
                                        .addComponent(signalQuality[1], 18, 18, 18)
                                        .addComponent(signalQuality[2], 18, 18, 18)
                                        .addComponent(signalQuality[3], 18, 18, 18)
                                        .addComponent(signalQuality[4], 18, 18, 18)
                                        .addComponent(signalQuality[5], 18, 18, 18)
                                        .addComponent(signalQuality[6], 18, 18, 18)
                                        .addComponent(signalQuality[7], 18, 18, 18)
                                        .addComponent(signalQuality[8], 18, 18, 18)
                                        .addComponent(signalQuality[9], 18, 18, 18)
                                        .addComponent(markerID, 18, 18, 18))
                                .addGap(4)
                                .addGroup(infoPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(logFileNameLabel, 18, 18, 18)
                                        .addComponent(measurementPeriod, 18, 18, 18)
                                        .addComponent(measurementsThisTile, 18, 18, 18)
                                        .addComponent(minMeasurementsPerTile, 18, 18, 18)
                                        .addComponent(maxMeasurementsPerTile, 18, 18, 18)
                                        .addComponent(tilesCompleted, 18, 18, 18)
                                        .addComponent(totalTiles, 18, 18, 18)
                                        .addComponent(averagedBmInCurrentTile, 18, 18, 18)
                                        .addComponent(averageBerInCurrentTile, 18, 18, 18)
                                        .addComponent(averageSinadInCurrentTile, 18, 18, 18)
                                        .addComponent(recordCountLabel, 18, 18, 18))
                                .addGap(4)
                                .addGroup(infoPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(cursorLongitude, 18, 18, 18)
                                        .addComponent(cursorLatitude, 18, 18, 18)
                                        .addComponent(cursorMGRS, 18, 18, 18)
                                        .addComponent(cursorMaidenheadReference, 18, 18, 18)
                                        .addComponent(viewingAltitude, 18, 18, 18)
                                        .addComponent(cursorTerrainElevationFeet, 18, 18, 18)
                                        .addComponent(utcLabel, 18, 18, 18)))
                        .addContainerGap()));

        return panel;
    }

    private void initializeShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                close();
            }
        });
    }
}
