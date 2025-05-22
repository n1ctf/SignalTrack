package coverage;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import java.beans.PropertyChangeSupport;

import java.io.File;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import radio.AbstractRadioReceiver;
import utility.Vincenty;

public class CoverageTestObject {

    public static final String PROPERTY_CHANGE = "PROPERTY_CHANGE";
    public static final String TILE_SIZE_CHANGE = "TILE_SIZE_CHANGE";
    public static final String TEST_GRID_SELECTION_MODE = "TEST_GRID_SELECTION_MODE";
    public static final String TEST_GRID_LEARN_MODE = "TEST_GRID_LEARN_MODE";
    public static final String REPLACE_ALL_NON_DIGITS = "\\D";
    
    public static final Point2D DEFAULT_TEST_GRID_REFERENCE = new Point.Double(-83.0, 40.0);

    private static final Logger LOG = Logger.getLogger(CoverageTestObject.class.getName());
    private static final String[] areaReliabilityValues = new String[]{"90%", "95%", "97%", "98%", "99%"};
    private static final String[] confidenceLevelValues = new String[]{"90%", "95%", "97%", "98%", "99%"};
    private static final String[] confidenceIntervalValues = new String[]{"0.25%", "0.5%", "0.75%", "1.0%", 
        "1.25%", "1.4%", "1.5%", "1.75%", "2.0%", "2.25%", "2.5%", "2.75%", "3.0%", "3.25%", "3.5%", 
        "3.75%", "4.0%"};
    
    private static final String[] channelsToDisplay = new String[]{"VFO", "SCAN 0", "SCAN 1", "SCAN 2", "SCAN 3", "SCAN 4", "SCAN 5", "SCAN 6", "SCAN 7", "SCAN 8", "SCAN 9"};

    private static final String[] dBmValues = new String[91];
    private static final String[] dBValues = new String[91];
    private static final String[] sinadValues = new String[31];
    private static final String[] berValues = new String[41];

    private static final HashMap<Double, Double> oneSidedArgumentOfUnitNormal = HashMap.newHashMap(9);
    private static final HashMap<Double, Double> twoSidedArgumentOfUnitNormal = HashMap.newHashMap(9);
    private static final Preferences userPrefs = Preferences.userRoot().node("jdrivetrack/prefs/CoverageTestSettings");

    private static final String[] tileSizeValues = new String[]{
        "0.05",
        "0.1",
        "0.2",
        "0.4",
        "0.5",
        "1",
        "4.86",
        "5",
        "10",
        "15",
        "20",
        "30",
        "40",
        "50",
        "60",
        "64.79",
        "70",
        "80",
        "90",
        "100",
        "120",
        "180"
    };
    
    private String testName;

    private int id;
    private int tilesNotAccessable;
    private int requiredGridPoints;
    private int maxNumberOfAllowedTiles;
    private int minNumberOfAllowedTiles;
    private int minTimePerTile;
    private int wavelengthsPerSubSample;
    private int dotsPerTile;
    private int minMeasurementsRequiredPerSubSample;
    private int minAllowedSampleRateSamplesPerSecond;
    private int minSpeedFPMForValidSample;
    private int maxSpeedFPMForValidSample;
    private int tilesPassed;
    private int tilesTested;
    private int minSamplesPerTile;
    private int maxSamplesPerTile;
    private int fixedTimePeriod;
    private int channelToDisplayIndex;
    private int contractualAreaReliabilityIndex;
    private int predictedAreaReliabilityIndex;
    private int confidenceIntervalIndex;
    private int confidenceLevelIndex;
    private int signalReqFor12dBSinadIndex;
    private int signalReqFor20dBQuietingIndex;
    private int dynamicRangeIndex;
    private int noiseFloorIndex;
    private int percentageOfTimeIndex;
    private int requestedSinadQualityIndex;
    private int requestedRssiQualityIndex;
    private int requestedBerQualityIndex;
    private int signalReqFor5PctBERIndex;
    private int adjacentChannelRejectionIndex;
    private int percentageOfCoverageAreaIndex;

    private double signalMarkerRadius;
    private double searchRadius;
    private double gridLeftDegrees;
    private double gridTopDegrees;
    private double gridHeightDegrees;
    private double gridWidthDegrees;
    private double degreesLatitudeForCalculation;

    private boolean alertOnMinimumSamplesPerTileAcquired;
    private boolean showGridSquareShading;
    private boolean showSignalMarkers;
    private boolean showRings;
    private boolean showQuads;
    private boolean showLines;
    private boolean enableAutoCalc;
    private boolean showGrid;
    private boolean shapesEnabled;
    private boolean testRxSpecsValidForTest;
    private boolean allowModifications = true;
    private boolean tileSizeCommited;

    private ManualMode manualDataCollectionMode = ManualMode.CONTINUOUS;
    private SignalQualityDisplayMode signalQualityDisplayMode = SignalQualityDisplayMode.DBM;
    private SignalSampleMode signalSampleMode = SignalSampleMode.DBM;
    private Accessability accessability = Accessability.ELIMINATE;
    private TestCriteria testCriteria = TestCriteria.GREATER_THAN;
    private CalcBasis calcBasis = CalcBasis.NUMBER_ALLOWED_TILES;
    private TimingMode timingMode = TimingMode.FIXED;
    private Point2D.Double tileSize;
    private Point2D.Double maxTileSize;
    private Point2D.Double minTileSize;

    private Rectangle2D testGridRectangle;

    private Color tileSelectedColor = new Color(127, 127, 127, 64);
    private Color tileInProgressColor = new Color(255, 255, 0, 64);
    private Color tileInaccessableColor = new Color(127, 0, 0, 64);
    private Color tileCompleteColor = new Color(0, 255, 0, 64);
    private Color gridColor = new Color(255, 0, 0, 255);

    private Color color50dBm = new Color(0, 255, 0);
    private Color color60dBm = new Color(0, 255, 0);
    private Color color70dBm = new Color(0, 255, 0);
    private Color color80dBm = new Color(255, 255, 0);
    private Color color90dBm = new Color(255, 255, 0);
    private Color color100dBm = new Color(255, 255, 0);
    private Color color110dBm = new Color(255, 0, 0);
    private Color color120dBm = new Color(255, 0, 0);

    private Color color0sinad = new Color(0, 255, 0);
    private Color color5sinad = new Color(0, 255, 0);
    private Color color10sinad = new Color(0, 255, 0);
    private Color color12sinad = new Color(255, 255, 0);
    private Color color15sinad = new Color(255, 255, 0);
    private Color color20sinad = new Color(255, 255, 0);
    private Color color25sinad = new Color(255, 0, 0);
    private Color color30sinad = new Color(255, 0, 0);

    private Color color0ber = new Color(0, 255, 0);
    private Color color5ber = new Color(0, 255, 0);
    private Color color10ber = new Color(0, 255, 0);
    private Color color15ber = new Color(255, 255, 0);
    private Color color20ber = new Color(255, 255, 0);
    private Color color25ber = new Color(255, 255, 0);
    private Color color30ber = new Color(255, 0, 0);
    private Color color35ber = new Color(255, 0, 0);

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private AbstractRadioReceiver abstractRadioReceiver;

    private CoverageTestObject() {
    	
        registerShutdownHook();
        loadHashMaps();
    }

    public CoverageTestObject(boolean clearAllPrefs, AbstractRadioReceiver abstractRadioReceiver, String testName) {
        this.abstractRadioReceiver = abstractRadioReceiver;
        this.testName = testName.replace(".sql", "");
        if (clearAllPrefs) {
            try {
                userPrefs.clear();
            } catch (final BackingStoreException ex) {
                LOG.log(Level.WARNING, "BackingStoreException", ex);
            }
        }
        registerShutdownHook();
        loadHashMaps();
        getSettingsFromRegistry();
        initializeConstants();
    }

    private static void initializeConstants() {
        for (int i = 0; i < dBmValues.length; i++) {
            dBmValues[i] = String.valueOf(i - 140) + " dBm";
        }

        for (int i = 0; i < dBValues.length; i++) {
            dBValues[i] = String.valueOf(i + 50) + " dB";
        }

        for (int i = 0; i < sinadValues.length; i++) {
            sinadValues[i] = String.valueOf(i) + " dB SINAD";
        }

        for (int i = 0; i < berValues.length; i++) {
            berValues[i] = String.valueOf(i) + " % BER";
        }
    }

    public AbstractRadioReceiver getRadio() {
        return abstractRadioReceiver;
    }

    private void setRadio(AbstractRadioReceiver abstractRadioReceiver) {
        this.abstractRadioReceiver = abstractRadioReceiver;
    }

    public PropertyChangeSupport getPropertyChangeSupport() {
        return pcs;
    }

    private void loadHashMaps() {
        oneSidedArgumentOfUnitNormal.put(0.5, 0.0);
        oneSidedArgumentOfUnitNormal.put(0.7, 0.524);
        oneSidedArgumentOfUnitNormal.put(0.8, 0.841);
        oneSidedArgumentOfUnitNormal.put(0.85, 1.036);
        oneSidedArgumentOfUnitNormal.put(0.90, 1.281);
        oneSidedArgumentOfUnitNormal.put(0.95, 1.645);
        oneSidedArgumentOfUnitNormal.put(0.97, 1.881);
        oneSidedArgumentOfUnitNormal.put(0.98, 2.055);
        oneSidedArgumentOfUnitNormal.put(0.99, 2.326);

        twoSidedArgumentOfUnitNormal.put(0.5, 0.6745);
        twoSidedArgumentOfUnitNormal.put(0.7, 1.036);
        twoSidedArgumentOfUnitNormal.put(0.8, 1.281);
        twoSidedArgumentOfUnitNormal.put(0.85, 1.439);
        twoSidedArgumentOfUnitNormal.put(0.90, 1.645);
        twoSidedArgumentOfUnitNormal.put(0.95, 1.960);
        twoSidedArgumentOfUnitNormal.put(0.97, 2.170);
        twoSidedArgumentOfUnitNormal.put(0.98, 2.326);
        twoSidedArgumentOfUnitNormal.put(0.99, 2.579);
    }

    public void saveSettingsToRegistry() {
        userPrefs.putInt(testName + "_CalcBasis", calcBasis.ordinal());
        userPrefs.putInt(testName + "_Accessability", accessability.ordinal());
        userPrefs.putInt(testName + "_SignalSampleMode", signalSampleMode.ordinal());
        userPrefs.putInt(testName + "_SignalQualityDisplayMode", signalQualityDisplayMode.ordinal());
        userPrefs.putInt(testName + "_ManualDataCollectionMode", manualDataCollectionMode.ordinal());
        userPrefs.putInt(testName + "_TestCriteria", testCriteria.ordinal());
        userPrefs.putInt(testName + "_TimingMode", timingMode.ordinal());

        userPrefs.putInt(testName + "_MinMeasurementsRequiredPerSubSample", minMeasurementsRequiredPerSubSample);
        userPrefs.putInt(testName + "_MinAllowedSampleRateSamplesPerSecond", minAllowedSampleRateSamplesPerSecond);
        userPrefs.putInt(testName + "_MaxNumberOfAllowedTiles", maxNumberOfAllowedTiles);
        userPrefs.putInt(testName + "_MinNumberOfAllowedTiles", minNumberOfAllowedTiles);
        userPrefs.putInt(testName + "_RequiredGridPoints", requiredGridPoints);
        userPrefs.putInt(testName + "_MinSamplesPerTile", minSamplesPerTile);
        userPrefs.putInt(testName + "_MaxSamplesPerTile", maxSamplesPerTile);
        userPrefs.putInt(testName + "_FixedTimePeriod", fixedTimePeriod);
        userPrefs.putInt(testName + "_MinSpeedFPMForValidSample", minSpeedFPMForValidSample);
        userPrefs.putInt(testName + "_MaxSpeedFPMForValidSample", maxSpeedFPMForValidSample);

        userPrefs.putInt(testName + "_ChannelToDisplayIndex", channelToDisplayIndex);
        userPrefs.putInt(testName + "_WavelengthsPerSubSample", wavelengthsPerSubSample);
        userPrefs.putInt(testName + "_PredictedAreaReliabilityIndex", predictedAreaReliabilityIndex);
        userPrefs.putInt(testName + "_ContracturalAreaReliabilityIndex", contractualAreaReliabilityIndex);
        userPrefs.putInt(testName + "_SignalReqFor12dBSinadIndex", signalReqFor12dBSinadIndex);
        userPrefs.putInt(testName + "_SignalReqFor20dBQuietingIndex", signalReqFor20dBQuietingIndex);
        userPrefs.putInt(testName + "_SignalReqFor5PctBERIndex", signalReqFor5PctBERIndex);
        userPrefs.putInt(testName + "_DynamicRangeIndex", dynamicRangeIndex);
        userPrefs.putInt(testName + "_NoiseFloorIndex", noiseFloorIndex);
        userPrefs.putInt(testName + "_AdjacentChannelRejectionIndex", adjacentChannelRejectionIndex);
        userPrefs.putInt(testName + "_RequestedSinadQualityIndex", requestedSinadQualityIndex);
        userPrefs.putInt(testName + "_RequestedRssiQualityIndex", requestedRssiQualityIndex);
        userPrefs.putInt(testName + "_RequestedBerQualityIndex", requestedBerQualityIndex);
        userPrefs.putInt(testName + "_PercentageOfCoverageAreaIndex", percentageOfCoverageAreaIndex);
        userPrefs.putInt(testName + "_PercentageOfTimeIndex", percentageOfTimeIndex);
        userPrefs.putInt(testName + "_ConfidenceIntervalIndex", confidenceIntervalIndex);
        userPrefs.putInt(testName + "_ConfidenceLevelIndex", confidenceLevelIndex);

        userPrefs.putDouble(testName + "_SearchRadius", searchRadius);
        userPrefs.putDouble(testName + "_SignalMarkerRadius", signalMarkerRadius);
        userPrefs.putDouble(testName + "_GridHeightDegrees", gridHeightDegrees);
        userPrefs.putDouble(testName + "_GridWidthDegrees", gridWidthDegrees);
        userPrefs.putDouble(testName + "_GridEdgeTop", gridTopDegrees);
        userPrefs.putDouble(testName + "_GridEdgeLeft", gridLeftDegrees);

        userPrefs.putDouble(testName + "_TileSizeLatitude", tileSize.getY());
        userPrefs.putDouble(testName + "_TileSizeLongitude", tileSize.getX());
        userPrefs.putDouble(testName + "_MaxTileSizeLatitude", maxTileSize.getY());
        userPrefs.putDouble(testName + "_MaxTileSizeLongitude", maxTileSize.getX());
        userPrefs.putDouble(testName + "_MinTileSizeLatitude", minTileSize.getY());
        userPrefs.putDouble(testName + "_MinTileSizeLongitude", minTileSize.getX());

        userPrefs.putBoolean(testName + "_TestRxSpecsValidForTest", testRxSpecsValidForTest);
        userPrefs.putBoolean(testName + "_AlertOnMinimumSamplesPerTileAcquired", alertOnMinimumSamplesPerTileAcquired);
        userPrefs.putBoolean(testName + "_ShowGridSquareShading", showGridSquareShading);
        userPrefs.putBoolean(testName + "_ShowSignalMarkers", showSignalMarkers);
        userPrefs.putBoolean(testName + "_ShowRings", showRings);
        userPrefs.putBoolean(testName + "_ShowQuads", showQuads);
        userPrefs.putBoolean(testName + "_ShowLines", showLines);
        userPrefs.putBoolean(testName + "_ShowGrid", showGrid);
        userPrefs.putBoolean(testName + "_EnableAutoCalc", enableAutoCalc);

        userPrefs.putLong(testName + "_ColorTileSelected", tileSelectedColor.getRGB());
        userPrefs.putLong(testName + "_ColorTileInProgress", tileInProgressColor.getRGB());
        userPrefs.putLong(testName + "_ColorTileInaccessable", tileInaccessableColor.getRGB());
        userPrefs.putLong(testName + "_ColorTileComplete", tileCompleteColor.getRGB());

        userPrefs.putLong(testName + "_ColorGrid", gridColor.getRGB());
        userPrefs.putLong(testName + "_Color50dBm", color50dBm.getRGB());
        userPrefs.putLong(testName + "_Color60dBm", color60dBm.getRGB());
        userPrefs.putLong(testName + "_Color70dBm", color70dBm.getRGB());
        userPrefs.putLong(testName + "_Color80dBm", color80dBm.getRGB());
        userPrefs.putLong(testName + "_Color90dBm", color90dBm.getRGB());
        userPrefs.putLong(testName + "_Color100dBm", color100dBm.getRGB());
        userPrefs.putLong(testName + "_Color110dBm", color110dBm.getRGB());
        userPrefs.putLong(testName + "_Color120dBm", color120dBm.getRGB());
        userPrefs.putLong(testName + "_Color0sinad", color0sinad.getRGB());
        userPrefs.putLong(testName + "_Color5sinad", color5sinad.getRGB());
        userPrefs.putLong(testName + "_Color10sinad", color10sinad.getRGB());
        userPrefs.putLong(testName + "_Color12sinad", color12sinad.getRGB());
        userPrefs.putLong(testName + "_Color15sinad", color15sinad.getRGB());
        userPrefs.putLong(testName + "_Color20sinad", color20sinad.getRGB());
        userPrefs.putLong(testName + "_Color25sinad", color25sinad.getRGB());
        userPrefs.putLong(testName + "_Color30sinad", color30sinad.getRGB());
        userPrefs.putLong(testName + "_Color0ber", color0ber.getRGB());
        userPrefs.putLong(testName + "_Color5ber", color5ber.getRGB());
        userPrefs.putLong(testName + "_Color10ber", color10ber.getRGB());
        userPrefs.putLong(testName + "_Color15ber", color15ber.getRGB());
        userPrefs.putLong(testName + "_Color20ber", color20ber.getRGB());
        userPrefs.putLong(testName + "_Color25ber", color25ber.getRGB());
        userPrefs.putLong(testName + "_Color30ber", color30ber.getRGB());
        userPrefs.putLong(testName + "_Color35ber", color35ber.getRGB());
    }

    private void getSettingsFromRegistry() {
        calcBasis = CalcBasis.values()[userPrefs.getInt(testName + "_CalcBasis", 0)];
        accessability = Accessability.values()[userPrefs.getInt(testName + "_Accessability", 0)];
        signalSampleMode = SignalSampleMode.values()[userPrefs.getInt(testName + "_SignalSampleMode", 0)];
        signalQualityDisplayMode = SignalQualityDisplayMode.values()[userPrefs.getInt(testName + "_SignalQualityDisplayMode", 0)];
        manualDataCollectionMode = ManualMode.values()[userPrefs.getInt(testName + "_ManualDataCollectionMode", 0)];
        testCriteria = TestCriteria.values()[userPrefs.getInt(testName + "_TestCriteria", 0)];
        timingMode = TimingMode.values()[userPrefs.getInt(testName + "_TimingMode", 0)];
        requiredGridPoints = userPrefs.getInt(testName + "_RequiredGridPoints", 100);
        minSamplesPerTile = userPrefs.getInt(testName + "_MinSamplesPerTile", 20);
        maxSamplesPerTile = userPrefs.getInt(testName + "_MaxSamplesPerTile", 600);
        fixedTimePeriod = userPrefs.getInt(testName + "_FixedTimePeriod", 200);
        maxNumberOfAllowedTiles = userPrefs.getInt(testName + "_MaxNumberOfAllowedTiles", 5000);
        minNumberOfAllowedTiles = userPrefs.getInt(testName + "_MinNumberOfAllowedTiles", 100);
        minSpeedFPMForValidSample = userPrefs.getInt(testName + "_MinSpeedFPMForValidSample", 100);
        maxSpeedFPMForValidSample = userPrefs.getInt(testName + "_MaxSpeedFPMForValidSample", 2500);
        wavelengthsPerSubSample = userPrefs.getInt(testName + "_WavelengthsPerSubSample", 30);
        minMeasurementsRequiredPerSubSample = userPrefs.getInt(testName + "_MinMeasurementsRequiredPerSubSample", 60);
        minAllowedSampleRateSamplesPerSecond = userPrefs.getInt(testName + "_MinAllowedSampleRateSamplesPerSecond", 20);
        channelToDisplayIndex = userPrefs.getInt(testName + "_ChannelToDisplayIndex", 0);
        predictedAreaReliabilityIndex = userPrefs.getInt(testName + "_PredictedAreaReliabilityIndex", 2);
        confidenceIntervalIndex = userPrefs.getInt(testName + "_ConfidenceIntervalIndex", 4);
        confidenceLevelIndex = userPrefs.getInt(testName + "_ConfidenceLevelIndex", 4);
        contractualAreaReliabilityIndex = userPrefs.getInt(testName + "_ContracturalAreaReliabilityIndex", 1);
        signalReqFor12dBSinadIndex = userPrefs.getInt(testName + "_SignalReqFor12dBSinadIndex", 3);
        signalReqFor20dBQuietingIndex = userPrefs.getInt(testName + "_SignalReqFor20dBQuietingIndex", 3);
        signalReqFor5PctBERIndex = userPrefs.getInt(testName + "_SignalReqFor5PctBERIndex", 3);
        dynamicRangeIndex = userPrefs.getInt(testName + "_DynamicRangeIndex", 3);
        noiseFloorIndex = userPrefs.getInt(testName + "_NoiseFloorIndex", 3);
        adjacentChannelRejectionIndex = userPrefs.getInt(testName + "_AdjacentChannelRejectionIndex", 3);
        requestedSinadQualityIndex = userPrefs.getInt(testName + "_RequestedSinadQualityIndex", 3);
        requestedRssiQualityIndex = userPrefs.getInt(testName + "_RequestedRssiQualityIndex", 3);
        requestedBerQualityIndex = userPrefs.getInt(testName + "_RequestedBerQualityIndex", 3);
        percentageOfCoverageAreaIndex = userPrefs.getInt(testName + "_PercentageOfCoverageAreaIndex", 3);
        percentageOfTimeIndex = userPrefs.getInt(testName + "_PercentageOfTimeIndex", 3);

        tileSize = new Point2D.Double(userPrefs.getDouble(testName + "_TileSizeLongitude", 0.008333),
                userPrefs.getDouble(testName + "_TileSizeLatitude", 0.008333));

        maxTileSize = new Point2D.Double(userPrefs.getDouble(testName + "_MaxTileSizeLongitude", 0.017997),
                userPrefs.getDouble(testName + "_MaxTileSizesLatitude", 0.017997));

        minTileSize = new Point2D.Double(userPrefs.getDouble(testName + "_MinTileSizeLongitude", 0.00135),
                userPrefs.getDouble(testName + "_MinTileSizeLatitude", 0.00135));

        searchRadius = userPrefs.getDouble(testName + "_SearchRadius", 1200D);
        signalMarkerRadius = userPrefs.getDouble(testName + "_SignalMarkerRadius", 3D);
        gridTopDegrees = userPrefs.getDouble(testName + "_GridEdgeTop", -999D);
        gridLeftDegrees = userPrefs.getDouble(testName + "_GridEdgeLeft", -999D);
        gridHeightDegrees = userPrefs.getDouble(testName + "_GridHeightDegrees", -999D);
        gridWidthDegrees = userPrefs.getDouble(testName + "_GridWidthDegrees", -999D);

        testRxSpecsValidForTest = userPrefs.getBoolean(testName + "_TestRxSpecsValidForTest", true);
        alertOnMinimumSamplesPerTileAcquired = userPrefs.getBoolean(testName + "_AlertOnMinimumSamplesPerTileAcquired", false);

        showGridSquareShading(userPrefs.getBoolean(testName + "_ShowGridSquareShading", true));
        showSignalMarkers(userPrefs.getBoolean(testName + "_ShowSignalMarkers", true));
        showRings(userPrefs.getBoolean(testName + "_ShowRings", true));
        showQuads(userPrefs.getBoolean(testName + "_ShowQuads", true));
        showLines(userPrefs.getBoolean(testName + "_ShowLines", true));
        showGrid(userPrefs.getBoolean(testName + "_ShowGrid", true));
        setEnableAutoCalc(userPrefs.getBoolean(testName + "_EnableAutoCalc", true));

        tileSelectedColor = getColor(userPrefs.getLong(testName + "_ColorTileSelected", new Color(127, 127, 127, 64).getRGB()));
        tileInProgressColor = getColor(userPrefs.getLong(testName + "_ColorTileInProgress", new Color(255, 255, 0, 64).getRGB()));
        tileInaccessableColor = getColor(userPrefs.getLong(testName + "_ColorTileInaccessable", new Color(127, 0, 0, 64).getRGB()));
        tileCompleteColor = getColor(userPrefs.getLong(testName + "_ColorTileComplete", new Color(0, 255, 0, 64).getRGB()));

        gridColor = getColor(userPrefs.getLong(testName + "_ColorGrid", new Color(255, 0, 0, 255).getRGB()));
        color50dBm = getColor(userPrefs.getLong(testName + "_Color50dBm", new Color(255, 255, 255).getRGB()));
        color60dBm = getColor(userPrefs.getLong(testName + "_Color60dBm", new Color(223, 223, 255).getRGB()));
        color70dBm = getColor(userPrefs.getLong(testName + "_Color70dBm", new Color(191, 191, 255).getRGB()));
        color80dBm = getColor(userPrefs.getLong(testName + "_Color80dBm", new Color(159, 159, 255).getRGB()));
        color90dBm = getColor(userPrefs.getLong(testName + "_Color90dBm", new Color(127, 127, 255).getRGB()));
        color100dBm = getColor(userPrefs.getLong(testName + "_Color100dBm", new Color(95, 95, 255).getRGB()));
        color110dBm = getColor(userPrefs.getLong(testName + "_Color110dBm", new Color(63, 63, 255).getRGB()));
        color120dBm = getColor(userPrefs.getLong(testName + "_Color120dBm", new Color(31, 31, 255).getRGB()));
        color0sinad = getColor(userPrefs.getLong(testName + "_Color0sinad", new Color(255, 0, 0).getRGB()));
        color5sinad = getColor(userPrefs.getLong(testName + "_Color5sinad", new Color(255, 0, 0).getRGB()));
        color10sinad = getColor(userPrefs.getLong(testName + "_Color10sinad", new Color(255, 0, 0).getRGB()));
        color12sinad = getColor(userPrefs.getLong(testName + "_Color12sinad", new Color(255, 0, 0).getRGB()));
        color15sinad = getColor(userPrefs.getLong(testName + "_Color15sinad", new Color(255, 0, 0).getRGB()));
        color20sinad = getColor(userPrefs.getLong(testName + "_Color20sinad", new Color(255, 0, 0).getRGB()));
        color25sinad = getColor(userPrefs.getLong(testName + "_Color25sinad", new Color(255, 0, 0).getRGB()));
        color30sinad = getColor(userPrefs.getLong(testName + "_Color30sinad", new Color(255, 0, 0).getRGB()));
        color0ber = getColor(userPrefs.getLong(testName + "_Color0ber", new Color(255, 0, 0).getRGB()));
        color5ber = getColor(userPrefs.getLong(testName + "_Color5ber", new Color(255, 0, 0).getRGB()));
        color10ber = getColor(userPrefs.getLong(testName + "_Color10ber", new Color(255, 0, 0).getRGB()));
        color15ber = getColor(userPrefs.getLong(testName + "_Color15ber", new Color(255, 0, 0).getRGB()));
        color20ber = getColor(userPrefs.getLong(testName + "_Color20ber", new Color(255, 0, 0).getRGB()));
        color25ber = getColor(userPrefs.getLong(testName + "_Color25ber", new Color(255, 0, 0).getRGB()));
        color30ber = getColor(userPrefs.getLong(testName + "_Color30ber", new Color(255, 0, 0).getRGB()));
        color35ber = getColor(userPrefs.getLong(testName + "_Color35ber", new Color(255, 0, 0).getRGB()));
    }

    public boolean isAllowModifications() {
        return allowModifications;
    }

    public void setAllowModifications(boolean allowModifications) {
        this.allowModifications = allowModifications;
    }

    public boolean isTileSizeCommited() {
        return tileSizeCommited;
    }

    public void setTileSizeCommited(boolean tileSizeCommited) {
        this.tileSizeCommited = tileSizeCommited;
    }

    public String getTestName() {
        return testName.replace(".sql", "");
    }

    private void setTestName(String testName) {
        this.testName = testName.replace(".sql", "");
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public static String[] getTileSizeValues() {
        return tileSizeValues.clone();
    }

    public static String[] getAreaReliabilityValues() {
        return areaReliabilityValues.clone();
    }

    public static String[] getConfidenceIntervalValues() {
        return confidenceIntervalValues.clone();
    }

    public static String[] getConfidenceLevelValues() {
        return confidenceLevelValues.clone();
    }

    public static String[] getChannelsToDisplay() {
        return channelsToDisplay.clone();
    }

    public static String[] getdBmValues() {
        return dBmValues.clone();
    }

    public static String[] getdBValues() {
        return dBValues.clone();
    }

    public static String[] getSinadValues() {
        return sinadValues.clone();
    }

    public static String[] getBerValues() {
        return berValues.clone();
    }

    public int getSignalReqFor12dBSinadIndex() {
        return signalReqFor12dBSinadIndex;
    }

    public void setSignalReqFor12dBSinadIndex(int signalReqFor12dBSinadIndex) {
        this.signalReqFor12dBSinadIndex = signalReqFor12dBSinadIndex;
    }

    public int getSignalReqFor20dBQuietingIndex() {
        return signalReqFor20dBQuietingIndex;
    }

    public void setSignalReqFor20dBQuietingIndex(int signalReqFor20dBQuietingIndex) {
        this.signalReqFor20dBQuietingIndex = signalReqFor20dBQuietingIndex;
    }

    public int getDynamicRangeIndex() {
        return dynamicRangeIndex;
    }

    public void setDynamicRangeIndex(int dynamicRangeIndex) {
        this.dynamicRangeIndex = dynamicRangeIndex;
    }

    public int getTilesNotAccessable() {
        return tilesNotAccessable;
    }

    public void setTilesNotAccessable(int tilesNotAccessable) {
        this.tilesNotAccessable = tilesNotAccessable;
    }

    public int getNoiseFloorIndex() {
        return noiseFloorIndex;
    }

    public void setNoiseFloorIndex(int noiseFloorIndex) {
        this.noiseFloorIndex = noiseFloorIndex;
    }

    public int getPercentageOfTimeIndex() {
        return percentageOfTimeIndex;
    }

    public void setPercentageOfTimeIndex(int percentageOfTimeIndex) {
        this.percentageOfTimeIndex = percentageOfTimeIndex;
    }

    public int getRequestedSinadQualityIndex() {
        return requestedSinadQualityIndex;
    }

    public void setRequestedSinadQualityIndex(int requestedSinadQualityIndex) {
        this.requestedSinadQualityIndex = requestedSinadQualityIndex;
    }

    public int getRequestedRssiQualityIndex() {
        return requestedRssiQualityIndex;
    }

    public void setRequestedRssiQualityIndex(int requestedRssiQualityIndex) {
        this.requestedRssiQualityIndex = requestedRssiQualityIndex;
    }

    public int getRequestedBerQualityIndex() {
        return requestedBerQualityIndex;
    }

    public void setRequestedBerQualityIndex(int requestedBerQualityIndex) {
        this.requestedBerQualityIndex = requestedBerQualityIndex;
    }

    public int getSignalReqFor5PctBERIndex() {
        return signalReqFor5PctBERIndex;
    }

    public void setSignalReqFor5PctBERIndex(int signalReqFor5PctBERIndex) {
        this.signalReqFor5PctBERIndex = signalReqFor5PctBERIndex;
    }

    public int getAdjacentChannelRejectionIndex() {
        return adjacentChannelRejectionIndex;
    }

    public void setAdjacentChannelRejectionIndex(int adjacentChannelRejectionIndex) {
        this.adjacentChannelRejectionIndex = adjacentChannelRejectionIndex;
    }

    public int getPercentageOfCoverageAreaIndex() {
        return percentageOfCoverageAreaIndex;
    }

    public void setPercentageOfCoverageAreaIndex(int percentageOfCoverageAreaIndex) {
        this.percentageOfCoverageAreaIndex = percentageOfCoverageAreaIndex;
    }

    public int getMinSpeedFPMForValidSample() {
        return minSpeedFPMForValidSample;
    }

    public void setMinSpeedFPMForValidSample(int minSpeedFPMForValidSample) {
        this.minSpeedFPMForValidSample = minSpeedFPMForValidSample;
    }

    public int getMaxSpeedFPMForValidSample() {
        return maxSpeedFPMForValidSample;
    }

    public void setMaxSpeedFPMForValidSample(int maxSpeedFPMForValidSample) {
        this.maxSpeedFPMForValidSample = maxSpeedFPMForValidSample;
    }

    public int getMinMeasurementsRequiredPerSubSample() {
        return minMeasurementsRequiredPerSubSample;
    }

    public void setMinMeasurementsRequiredPerSubSample(int minMeasurementsRequiredPerSubSample) {
        this.minMeasurementsRequiredPerSubSample = minMeasurementsRequiredPerSubSample;
    }

    public int getMinAllowedSampleRateSamplesPerSecond() {
        return minAllowedSampleRateSamplesPerSecond;
    }

    public void setMinAllowedSampleRateSamplesPerSecond(int minAllowedSampleRateSamplesPerSecond) {
        this.minAllowedSampleRateSamplesPerSecond = minAllowedSampleRateSamplesPerSecond;
    }

    public TestCriteria getTestCriteria() {
        return testCriteria;
    }

    public void setTestCriteria(TestCriteria testCriteria) {
        this.testCriteria = testCriteria;
    }

    public TimingMode getTimingMode() {
        return timingMode;
    }

    public void setTimingMode(TimingMode timingMode) {
        this.timingMode = timingMode;
    }

    public Rectangle2D getTestGridRectangle() {
        return testGridRectangle;
    }

    public void setTestGridRectangle(Rectangle2D testGridRectangle) {
        this.testGridRectangle = testGridRectangle;
    }

    public Point2D.Double getGridReference() {
        return new Point.Double(getGridLeftDegrees(), getGridTopDegrees());
    }

    public Point2D.Double getGridSize() {
        return new Point.Double(getGridWidthDegrees(), getGridHeightDegrees());
    }

    public int getRequiredGridPoints() {
        return requiredGridPoints;
    }

    public void setRequiredGridPoints(int requiredGridPoints) {
        this.requiredGridPoints = requiredGridPoints;
    }

    public int getMinSamplesPerTile() {
        return minSamplesPerTile;
    }

    public void setMinSamplesPerTile(int minSamplesPerTile) {
        this.minSamplesPerTile = minSamplesPerTile;
    }

    public int getFixedTimePeriod() {
        return fixedTimePeriod;
    }

    public void setFixedTimePeriod(int fixedTimePeriod) {
        this.fixedTimePeriod = fixedTimePeriod;
    }

    public int getMaxSamplesPerTile() {
        return maxSamplesPerTile;
    }

    public void setMaxSamplesPerTile(int maxSamplesPerTile) {
        this.maxSamplesPerTile = maxSamplesPerTile;
    }

    public int getMaxNumberOfAllowedTiles() {
        return maxNumberOfAllowedTiles;
    }

    public void setMaxNumberOfAllowedTiles(int maxNumberOfAllowedTiles) {
        this.maxNumberOfAllowedTiles = maxNumberOfAllowedTiles;
    }

    public int getChannelToDisplayIndex() {
        return channelToDisplayIndex;
    }

    public void setChannelToDisplayIndex(int channelToDisplayIndex) {
        this.channelToDisplayIndex = channelToDisplayIndex;
    }

    public String getChannelToDisplay() {
        return channelsToDisplay[channelToDisplayIndex];
    }

    public int getMinTimePerTile() {
        return minTimePerTile;
    }

    public void setMinTimePerTile(int minTimePerTile) {
        this.minTimePerTile = minTimePerTile;
    }

    public int getWavelengthsPerSubSample() {
        return wavelengthsPerSubSample;
    }

    public void setWavelengthsPerSubSample(int wavelengthsPerSubSample) {
        this.wavelengthsPerSubSample = wavelengthsPerSubSample;
    }

    public int getDotsPerTile() {
        return dotsPerTile;
    }

    public void setDotsPerTile(int dotsPerTile) {
        this.dotsPerTile = dotsPerTile;
    }

    public int getMinNumberOfAllowedTiles() {
        return minNumberOfAllowedTiles;
    }

    public void setMinNumberOfAllowedTiles(int minNumberOfAllowedTiles) {
        this.minNumberOfAllowedTiles = minNumberOfAllowedTiles;
    }
    
    public double getContractualAreaReliability() {
        return Double.parseDouble(areaReliabilityValues[contractualAreaReliabilityIndex].replaceAll(REPLACE_ALL_NON_DIGITS, "")) / 100D;
    }

    public int getContractualAreaReliabilityIndex() {
        return contractualAreaReliabilityIndex;
    }

    public void setContractualAreaReliabilityIndex(int contractualAreaReliabilityIndex) {
        this.contractualAreaReliabilityIndex = contractualAreaReliabilityIndex;
    }

    public double getPredictedAreaReliability() {
        return Double.parseDouble(areaReliabilityValues[predictedAreaReliabilityIndex].replaceAll(REPLACE_ALL_NON_DIGITS, "")) / 100D;
    }

    public int getPredictedAreaReliabilityIndex() {
        return predictedAreaReliabilityIndex;
    }

    public void setPredictedAreaReliabilityIndex(int predictedAreaReliabilityIndex) {
        this.predictedAreaReliabilityIndex = predictedAreaReliabilityIndex;
    }

    public double getConfidenceInterval() {
        return Double.parseDouble(confidenceIntervalValues[confidenceIntervalIndex].replaceAll(REPLACE_ALL_NON_DIGITS, "")) / 100D;
    }

    public double getConfidenceLevel() {
        return Double.parseDouble(confidenceLevelValues[confidenceLevelIndex].replaceAll(REPLACE_ALL_NON_DIGITS, "")) / 100D;
    }

    public int getConfidenceIntervalIndex() {
        return confidenceIntervalIndex;
    }

    public void setConfidenceIntervalIndex(int confidenceIntervalIndex) {
        this.confidenceIntervalIndex = confidenceIntervalIndex;
    }

    public int getConfidenceLevelIndex() {
        return confidenceLevelIndex;
    }

    public void setConfidenceLevelIndex(int confidenceLevelIndex) {
        this.confidenceLevelIndex = confidenceLevelIndex;
    }

    public double getSignalReqFor12dBSinad() {
        return Double.parseDouble(dBmValues[signalReqFor12dBSinadIndex].replaceAll(REPLACE_ALL_NON_DIGITS, ""));
    }

    public void setSignalReqFor12dBSinad(double signalReqFor12dBSinad) {
        for (int i = 0; i < dBmValues.length; i++) {
            if (dBmValues[i].contains(String.valueOf(signalReqFor12dBSinad))) {
                signalReqFor12dBSinadIndex = i;
                return;
            }
        }
    }

    public double getSignalReqFor20dBQuieting() {
        return Double.parseDouble(dBmValues[signalReqFor20dBQuietingIndex].replaceAll(REPLACE_ALL_NON_DIGITS, ""));
    }

    public void setSignalReqFor20dBQuieting(double signalReqFor20dBQuieting) {
        for (int i = 0; i < dBmValues.length; i++) {
            if (dBmValues[i].contains(String.valueOf(signalReqFor20dBQuieting))) {
                signalReqFor20dBQuietingIndex = i;
                return;
            }
        }
    }

    public double getDynamicRange() {
        return Double.parseDouble(dBValues[dynamicRangeIndex].replaceAll(REPLACE_ALL_NON_DIGITS, ""));
    }

    public void setDynamicRange(double dynamicRange) {
        for (int i = 0; i < dBmValues.length; i++) {
            if (dBValues[i].contains(String.valueOf(dynamicRange))) {
                dynamicRangeIndex = i;
                return;
            }
        }
    }

    public double getNoiseFloor() {
        return Double.parseDouble(dBmValues[noiseFloorIndex].replaceAll(REPLACE_ALL_NON_DIGITS, ""));
    }

    public void setNoiseFloor(double noiseFloor) {
        for (int i = 0; i < dBmValues.length; i++) {
            if (dBmValues[i].contains(String.valueOf(noiseFloor))) {
                noiseFloorIndex = i;
                return;
            }
        }
    }

    public double getPercentageOfTime() {
        return Double.parseDouble(areaReliabilityValues[percentageOfTimeIndex].replaceAll(REPLACE_ALL_NON_DIGITS, ""));
    }

    public double getRequestedSinadQuality() {
        return Double.parseDouble(sinadValues[requestedSinadQualityIndex].replaceAll(REPLACE_ALL_NON_DIGITS, ""));
    }

    public double getRequestedRssiQuality() {
        return Double.parseDouble(dBValues[requestedRssiQualityIndex].replaceAll(REPLACE_ALL_NON_DIGITS, ""));
    }

    public double getRequestedBerQuality() {
        return Double.parseDouble(berValues[requestedBerQualityIndex].replaceAll(REPLACE_ALL_NON_DIGITS, ""));
    }

    public double getSignalReqFor5PctBER() {
        return Double.parseDouble(dBmValues[signalReqFor5PctBERIndex].replaceAll(REPLACE_ALL_NON_DIGITS, ""));
    }

    public void setSignalReqFor5PctBER(double signalReqFor5PctBER) {
        for (int i = 0; i < dBmValues.length; i++) {
            if (dBmValues[i].contains(String.valueOf(signalReqFor5PctBER))) {
                signalReqFor5PctBERIndex = i;
                return;
            }
        }
    }

    public double getAdjacentChannelRejection() {
        return Double.parseDouble(dBValues[adjacentChannelRejectionIndex].replaceAll(REPLACE_ALL_NON_DIGITS, ""));
    }

    public void setAdjacentChannelRejection(double adjacentChannelRejection) {
        for (int i = 0; i < dBValues.length; i++) {
            if (dBValues[i].contains(String.valueOf(adjacentChannelRejection))) {
                adjacentChannelRejectionIndex = i;
                return;
            }
        }
    }

    public double getPercentageOfCoverageArea() {
        return Double.parseDouble(areaReliabilityValues[percentageOfCoverageAreaIndex].replaceAll(REPLACE_ALL_NON_DIGITS, ""));
    }

    public double getSignalMarkerRadius() {
        return signalMarkerRadius;
    }

    public void setSignalMarkerRadius(double signalMarkerRadius) {
        this.signalMarkerRadius = signalMarkerRadius;
    }

    public double getSearchRadius() {
        return searchRadius;
    }

    public void setSearchRadius(double searchRadius) {
        this.searchRadius = searchRadius;
    }

    public double getGridLeftDegrees() {
        return gridLeftDegrees;
    }

    public void setGridLeftDegrees(double gridLeftDegrees) {
        this.gridLeftDegrees = gridLeftDegrees;
        updateTestGridRectangle();
    }

    public double getGridTopDegrees() {
        return gridTopDegrees;
    }

    public void setGridTopDegrees(double gridTopDegrees) {
        this.gridTopDegrees = gridTopDegrees;
        updateTestGridRectangle();
    }

    public void setGridRightDegrees(double gridRight) {
        gridWidthDegrees = Math.abs(gridRight - gridLeftDegrees);
        updateTestGridRectangle();
    }

    public double getGridRightDegrees() {
        return gridLeftDegrees + gridWidthDegrees;
    }

    public void setGridBottomDegrees(double gridBottom) {
        gridHeightDegrees = Math.abs(gridBottom - gridTopDegrees);
        updateTestGridRectangle();
    }

    public double getGridBottomDegrees() {
        return gridTopDegrees - gridHeightDegrees;
    }

    public double getGridHeightDegrees() {
        return gridHeightDegrees;
    }

    public void setGridHeightDegrees(double gridHeightDegrees) {
        this.gridHeightDegrees = (Math.abs(gridHeightDegrees) >= 0D &&  Math.abs(gridHeightDegrees) < 180D) ? gridHeightDegrees : 0;
        updateTestGridRectangle();
    }

    public double getGridWidthDegrees() {
        return gridWidthDegrees;
    }

    public void setGridWidthDegrees(double gridWidthDegrees) {
        this.gridWidthDegrees = (Math.abs(gridWidthDegrees) >= 0D &&  Math.abs(gridWidthDegrees) < 180D) ? gridWidthDegrees : 0;
        updateTestGridRectangle();
    }

    public double getDegreesLatitudeForCalculation() {
        return degreesLatitudeForCalculation;
    }

    public void setDegreesLatitudeForCalculation(double degreesLatitudeForCalculation) {
        this.degreesLatitudeForCalculation = degreesLatitudeForCalculation;
    }

    public boolean isAlertOnMinimumSamplesPerTileAcquired() {
        return alertOnMinimumSamplesPerTileAcquired;
    }

    public void setAlertOnMinimumSamplesPerTileAcquired(boolean alertOnMinimumSamplesPerTileAcquired) {
        this.alertOnMinimumSamplesPerTileAcquired = alertOnMinimumSamplesPerTileAcquired;
    }

    public boolean isShowGridSquareShading() {
        return showGridSquareShading;
    }

    public void showGridSquareShading(boolean showGridSquareShading) {
        this.showGridSquareShading = showGridSquareShading;
    }

    public boolean isShowSignalMarkers() {
        return showSignalMarkers;
    }

    public void showSignalMarkers(boolean showSignalMarkers) {
        this.showSignalMarkers = showSignalMarkers;
    }

    public boolean isShowRings() {
        return showRings;
    }

    public void showRings(boolean showRings) {
        this.showRings = showRings;
    }

    public boolean isShowQuads() {
        return showQuads;
    }

    public void showQuads(boolean showQuads) {
        this.showQuads = showQuads;
    }

    public boolean isShowLines() {
        return showLines;
    }

    public void showLines(boolean showLines) {
        this.showLines = showLines;
    }

    public boolean isEnableAutoCalc() {
        return enableAutoCalc;
    }

    public void setEnableAutoCalc(boolean enableAutoCalc) {
        this.enableAutoCalc = enableAutoCalc;
    }

    public boolean isShowGrid() {
        return showGrid;
    }

    public void showGrid(boolean showGrid) {
        this.showGrid = showGrid;
    }

    public boolean isShapesEnabled() {
        return shapesEnabled;
    }

    public void setShapesEnabled(boolean shapesEnabled) {
        this.shapesEnabled = shapesEnabled;
    }

    public boolean isTestRxSpecsValidForTest() {
        return testRxSpecsValidForTest;
    }

    public void setTestRxSpecsValidForTest(boolean testRxSpecsValidForTest) {
        this.testRxSpecsValidForTest = testRxSpecsValidForTest;
    }

    public ManualMode getManualDataCollectionMode() {
        return manualDataCollectionMode;
    }

    public void setManualDataCollectionMode(ManualMode manualDataCollectionMode) {
        this.manualDataCollectionMode = manualDataCollectionMode;
    }

    public SignalQualityDisplayMode getSignalQualityDisplayMode() {
        return signalQualityDisplayMode;
    }

    public void setSignalQualityDisplayMode(SignalQualityDisplayMode signalQualityDisplayMode) {
        this.signalQualityDisplayMode = signalQualityDisplayMode;
    }

    public SignalSampleMode getSignalSampleMode() {
        return signalSampleMode;
    }

    public void setSignalSampleMode(SignalSampleMode signalSampleMode) {
        this.signalSampleMode = signalSampleMode;
    }

    public Accessability getAccessability() {
        return accessability;
    }

    public void setAccessability(Accessability accessability) {
        this.accessability = accessability;
    }

    public CalcBasis getCalcBasis() {
        return calcBasis;
    }

    public void setCalcBasis(CalcBasis calcBasis) {
        this.calcBasis = calcBasis;
    }

    public Color getTileSelectedColor() {
        return tileSelectedColor;
    }

    public void setTileSelectedColor(Color tileSelectedColor) {
        this.tileSelectedColor = tileSelectedColor;
    }

    public Color getTileInProgressColor() {
        return tileInProgressColor;
    }

    public void setTileInProgressColor(Color tileInProgressColor) {
        this.tileInProgressColor = tileInProgressColor;
    }

    public Color getTileInaccessableColor() {
        return tileInaccessableColor;
    }

    public void setTileInaccessableColor(Color tileInaccessableColor) {
        this.tileInaccessableColor = tileInaccessableColor;
    }

    public Color getTileCompleteColor() {
        return tileCompleteColor;
    }

    public void setTileCompleteColor(Color tileCompleteColor) {
        this.tileCompleteColor = tileCompleteColor;
    }

    public Color getGridColor() {
        return gridColor;
    }

    public void setGridColor(Color gridColor) {
        this.gridColor = gridColor;
    }

    public Color getColor50dBm() {
        return color50dBm;
    }

    public void setColor50dBm(Color color50dBm) {
        this.color50dBm = color50dBm;
    }

    public Color getColor60dBm() {
        return color60dBm;
    }

    public void setColor60dBm(Color color60dBm) {
        this.color60dBm = color60dBm;
    }

    public Color getColor70dBm() {
        return color70dBm;
    }

    public void setColor70dBm(Color color70dBm) {
        this.color70dBm = color70dBm;
    }

    public Color getColor80dBm() {
        return color80dBm;
    }

    public void setColor80dBm(Color color80dBm) {
        this.color80dBm = color80dBm;
    }

    public Color getColor90dBm() {
        return color90dBm;
    }

    public void setColor90dBm(Color color90dBm) {
        this.color90dBm = color90dBm;
    }

    public Color getColor100dBm() {
        return color100dBm;
    }

    public void setColor100dBm(Color color100dBm) {
        this.color100dBm = color100dBm;
    }

    public Color getColor110dBm() {
        return color110dBm;
    }

    public void setColor110dBm(Color color110dBm) {
        this.color110dBm = color110dBm;
    }

    public Color getColor120dBm() {
        return color120dBm;
    }

    public void setColor120dBm(Color color120dBm) {
        this.color120dBm = color120dBm;
    }

    public Color getColor0sinad() {
        return color0sinad;
    }

    public void setColor0sinad(Color color0sinad) {
        this.color0sinad = color0sinad;
    }

    public Color getColor5sinad() {
        return color5sinad;
    }

    public void setColor5sinad(Color color5sinad) {
        this.color5sinad = color5sinad;
    }

    public Color getColor10sinad() {
        return color10sinad;
    }

    public void setColor10sinad(Color color10sinad) {
        this.color10sinad = color10sinad;
    }

    public Color getColor12sinad() {
        return color12sinad;
    }

    public void setColor12sinad(Color color12sinad) {
        this.color12sinad = color12sinad;
    }

    public Color getColor15sinad() {
        return color15sinad;
    }

    public void setColor15sinad(Color color15sinad) {
        this.color15sinad = color15sinad;
    }

    public Color getColor20sinad() {
        return color20sinad;
    }

    public void setColor20sinad(Color color20sinad) {
        this.color20sinad = color20sinad;
    }

    public Color getColor25sinad() {
        return color25sinad;
    }

    public void setColor25sinad(Color color25sinad) {
        this.color25sinad = color25sinad;
    }

    public Color getColor30sinad() {
        return color30sinad;
    }

    public void setColor30sinad(Color color30sinad) {
        this.color30sinad = color30sinad;
    }

    public Color getColor0ber() {
        return color0ber;
    }

    public void setColor0ber(Color color0ber) {
        this.color0ber = color0ber;
    }

    public Color getColor5ber() {
        return color5ber;
    }

    public void setColor5ber(Color color5ber) {
        this.color5ber = color5ber;
    }

    public Color getColor10ber() {
        return color10ber;
    }

    public void setColor10ber(Color color10ber) {
        this.color10ber = color10ber;
    }

    public Color getColor15ber() {
        return color15ber;
    }

    public void setColor15ber(Color color15ber) {
        this.color15ber = color15ber;
    }

    public Color getColor20ber() {
        return color20ber;
    }

    public void setColor20ber(Color color20ber) {
        this.color20ber = color20ber;
    }

    public Color getColor25ber() {
        return color25ber;
    }

    public void setColor25ber(Color color25ber) {
        this.color25ber = color25ber;
    }

    public Color getColor30ber() {
        return color30ber;
    }

    public void setColor30ber(Color color30ber) {
        this.color30ber = color30ber;
    }

    public Color getColor35ber() {
        return color35ber;
    }

    public void setColor35ber(Color color35ber) {
        this.color35ber = color35ber;
    }

    public Map<Double, Double> getOneSidedArgumentOfUnitNormal() {
        return Collections.unmodifiableMap(oneSidedArgumentOfUnitNormal);
    }

    public Map<Double, Double> getTwoSidedArgumentOfUnitNormal() {
        return Collections.unmodifiableMap(twoSidedArgumentOfUnitNormal);
    }

    public Preferences getUserPrefs() {
        return userPrefs;
    }

    private Color getColor(long color) {
        final int r = (int) ((color & 0x0000000000ff0000L) >> 16);
        final int g = (int) ((color & 0x000000000000ff00L) >> 8);
        final int b = (int) (color & 0x00000000000000ffL);
        final int a = (int) ((color & 0x00000000ff000000L) >> 24);
        return new Color(r, g, b, a);
    }

    public Point2D.Double getTileSizeDegrees() {
        return tileSize;
    }

    public Point2D.Double getTileSizeArcSeconds() {
        return new Point2D.Double(tileSize.getX() * 3600D, tileSize.getY() * 3600D);
    }

    public void setTileSizeArcSeconds(double tileSizeArcSecondsLatitude) {
        setTileSize(new Point2D.Double(tileSizeArcSecondsLatitude / 3600D, tileSizeArcSecondsLatitude / 3600D));
    }

    public void setTileSize(Point2D.Double tileSize) {
        if (this.tileSize.hashCode() != tileSize.hashCode()) {
            pcs.firePropertyChange(CoverageTestObject.TILE_SIZE_CHANGE, null, tileSize);
        }
        this.tileSize = tileSize;
    }

    public Point2D.Double getMaxTileSize() {
        return maxTileSize;
    }

    public void setMaxTileSizeArcSeconds(double maxTileSizeArcSecondsLatitude) {
        setMaxTileSize(new Point2D.Double(maxTileSizeArcSecondsLatitude / 3600D, maxTileSizeArcSecondsLatitude / 3600D));
    }

    public void setMaxTileSize(Point2D.Double maxTileSize) {
        this.maxTileSize = maxTileSize;
    }

    public Point2D.Double getMinTileSize() {
        return minTileSize;
    }

    public void setMinTileSizeArcSeconds(double minTileSizeArcSecondsLatitude) {
        setMinTileSize(new Point2D.Double(minTileSizeArcSecondsLatitude / 3600D, minTileSizeArcSecondsLatitude / 3600D));
    }

    public void setMinTileSize(Point2D.Double minTileSize) {
        this.minTileSize = minTileSize;
    }

    private void updateTestGridRectangle() {
        testGridRectangle = new Rectangle2D.Double(gridLeftDegrees, gridTopDegrees, gridWidthDegrees, gridHeightDegrees);
    }

    public Color getBerColor(double ber) {
        Color color = Color.BLACK;
        if (ber <= 4) {
            color = color0ber;
        } else if (ber <= 9) {
            color = color5ber;
        } else if (ber <= 14) {
            color = color10ber;
        } else if (ber <= 19) {
            color = color15ber;
        } else if (ber <= 24) {
            color = color20ber;
        } else if (ber <= 29) {
            color = color25ber;
        } else if (ber <= 34) {
            color = color30ber;
        } else if (ber <= 99) {
            color = color35ber;
        }
        return color;
    }

    public Color getdBmColor(double dBm) {
        Color color = Color.BLACK;
        if (dBm <= -120) {
            color = color120dBm;
        } else if (dBm <= -110) {
            color = color110dBm;
        } else if (dBm <= -100) {
            color = color100dBm;
        } else if (dBm <= -90) {
            color = color90dBm;
        } else if (dBm <= -80) {
            color = color80dBm;
        } else if (dBm <= -70) {
            color = color70dBm;
        } else if (dBm <= -60) {
            color = color60dBm;
        } else if (dBm <= 0) {
            color = color50dBm;
        }
        return color;
    }

    public Color getSinadColor(double sinad) {
        Color color = Color.BLACK;
        if (sinad <= 4) {
            color = color0sinad;
        } else if (sinad <= 9) {
            color = color5sinad;
        } else if (sinad <= 11) {
            color = color10sinad;
        } else if (sinad <= 14) {
            color = color12sinad;
        } else if (sinad <= 19) {
            color = color15sinad;
        } else if (sinad <= 24) {
            color = color20sinad;
        } else if (sinad <= 29) {
            color = color25sinad;
        } else if (sinad <= 99) {
            color = color30sinad;
        }
        return color;
    }

    public long getNumberOfRequiredTiles() {
        final double z;
        if (testCriteria == TestCriteria.GREATER_THAN) {
            z = oneSidedArgumentOfUnitNormal.get(getConfidenceLevel());
        } else {
            z = twoSidedArgumentOfUnitNormal.get(getConfidenceLevel());
        }
        final double p = getContractualAreaReliability() + getConfidenceInterval();
        final double q = 1 - p;
        final double e = getConfidenceInterval();
        final int m = getMinNumberOfAllowedTiles();
        return Math.max(Math.round(((z * z) * p * q) / (e * e)), m);
    }

    public long getRequiredSubsamples(double cl, double car, double ci) {
        final double za2 = twoSidedArgumentOfUnitNormal.get(cl);
        final double e = ci / 2.0;
        return Math.round((car * (1 - car)) * ((za2 / e) * (za2 / e)));
    }

    public long getRequiredGridPoints(double cl, double ci) {
        final double za2 = twoSidedArgumentOfUnitNormal.get(cl);
        return Math.round((za2 * za2) / (4 * (ci * ci)));
    }

    public double getFadeRate(double mph, double freqMHz) {
        final double feetPerHour = mph * 5280.0;
        final double metersPerHour = feetPerHour / Vincenty.FEET_PER_METER;
        final double metersPerSecond = metersPerHour / 3600.0;
        final double wavelength = 299.79250 / freqMHz;
        return metersPerSecond / wavelength;
    }

    public double getCoveredAreaReliability() {
        return ((double) tilesPassed / tilesTested) * 100.0;
    }

    public int getSubsamplesRequired(double sar, double arg, double ci) {
        return (int) ((arg * sar * (1 - sar)) / (ci / 2));
    }

    public int getTilesPassed() {
        return tilesPassed;
    }

    public void setTilesPassed(int tilesPassed) {
        this.tilesPassed = tilesPassed;
    }

    public int getTilesTested() {
        return tilesTested;
    }

    public void setTilesTested(int tilesTested) {
        this.tilesTested = tilesTested;
    }

    public static CoverageTestObject toCoverageTestSettings(final Object[] obj) {
        final CoverageTestObject cto = new CoverageTestObject();
        try {
            cto.setId((Integer) obj[0]);
            cto.setTestName((String) obj[1]);
            cto.setMinTimePerTile((Integer) obj[2]);
            cto.setSignalSampleMode(SignalSampleMode.values()[(Integer) obj[3]]);
            cto.setTileSize(new Point2D.Double((Double) obj[4], (Double) obj[5]));
            cto.setMinMeasurementsRequiredPerSubSample((Integer) obj[6]);
            cto.setMaxSamplesPerTile((Integer) obj[7]);
            cto.setMinSamplesPerTile((Integer) obj[8]);
            cto.setGridLeftDegrees((Double) obj[9]);
            cto.setGridTopDegrees((Double) obj[10]);
            cto.setGridWidthDegrees((Double) obj[11]);
            cto.setGridHeightDegrees((Double) obj[12]);
            cto.setDotsPerTile((Integer) obj[13]);
            cto.setRadio(AbstractRadioReceiver.getRadioInstance(new File((String) obj[14]), false));
        } catch (ClassCastException ex) {
            throw new ClassCastException();
        }
        return cto;
    }

    public Object[] toObjectArray(final CoverageTestObject cto) {
        final Object[] obj = new Object[15];
        obj[0] = cto.getId();
        obj[1] = cto.getTestName().replace(".sql", "");
        obj[2] = cto.getMinTimePerTile();
        obj[3] = cto.getSignalSampleMode().ordinal();
        obj[4] = cto.getTileSizeDegrees().getX();
        obj[5] = cto.getTileSizeDegrees().getY();
        obj[6] = cto.getMinMeasurementsRequiredPerSubSample();
        obj[7] = cto.getMaxSamplesPerTile();
        obj[8] = cto.getMinSamplesPerTile();
        obj[9] = cto.getGridLeftDegrees();
        obj[10] = cto.getGridTopDegrees();
        obj[11] = cto.getGridWidthDegrees();
        obj[12] = cto.getGridHeightDegrees();
        obj[13] = cto.getDotsPerTile();
        obj[14] = cto.getRadio().getCalFile().getPath();
        return obj;
    }

    public List<Rectangle2D> getGridList() {
        final List<Rectangle2D> list = Collections.synchronizedList(new CopyOnWriteArrayList<>());
        final Point2D.Double tileSizeDegrees = getTileSizeDegrees();
        for (double x = getGridLeftDegrees(); x <= ((getGridRightDegrees() - tileSizeDegrees.getX()) + 0.0001); x += tileSizeDegrees.getX()) {
            for (double y = getGridTopDegrees(); y >= ((getGridBottomDegrees() + tileSizeDegrees.getY()) - 0.0001); y -= tileSizeDegrees.getY()) {
                list.add(new Rectangle2D.Double(x, y, tileSizeDegrees.getX(), tileSizeDegrees.getY()));
            }
        }
        return list;
    }

    private void registerShutdownHook() {
        final Thread shutdownThread = new Thread() {
            @Override
            public void run() {
                saveSettingsToRegistry();
            }
        };
        Runtime.getRuntime().addShutdownHook(shutdownThread);
    }

}
