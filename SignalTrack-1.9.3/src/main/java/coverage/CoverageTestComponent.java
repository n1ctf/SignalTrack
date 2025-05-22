package coverage;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.awt.geom.RectangularShape;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.text.PlainDocument;

import utility.DoubleFilter;
import utility.IntegerFilter;
import utility.Vincenty;

public class CoverageTestComponent extends JFrame {

    private static final long serialVersionUID = 1L;

    private JPanel signalQualityDisplaySelectorPanel;
    private JPanel manualDataCollectionModeSelectorPanel;
    private JPanel rssiDataPointColoringPanel;
    private JPanel automaticParameterCalculationPanel;
    private JPanel accessabilityPanel;
    private JPanel receiverPerformanceParametersPanel;
    private JPanel reliabilityDesignTargetsPanel;
    private JPanel testParameterPanel;
    private JPanel tileConstraintsPanel;
    private JPanel valuesToBeSampledPanel;
    private JPanel testGridDimensionPanel;
    private JPanel mapImageModelPanel;
    private JPanel sinadDataPointColoringPanel;
    private JPanel berDataPointColoringPanel;
    private JPanel tileShadingPanel;
    private JPanel passFailCriteriaSelectorPanel;
    
    private JPanel sampleTimingPanel;
    
    private JPanel autoCalculatedCoverageTestParametersPanel;
    private JPanel measuredPerformancePanel;

    private JButton okButton;
    private JButton cancelButton;
    private JButton applyButton;

    private JCheckBox alertOnMinimumSamplesPerTileAcquiredCheckBox;
    private JCheckBox showSignalMarkersCheckBox;
    private JCheckBox showQuadsCheckBox;
    private JCheckBox showLinesCheckBox;
    private JCheckBox showRingsCheckBox;
    private JCheckBox showGridSquareShadingCheckBox;
    private JCheckBox showTestGridCheckBox;
    private JCheckBox enableAutoCalcCheckBox;
    private JCheckBox testRxSpecsValidForTestCheckBox;

    private JComboBox<String> predictedAreaReliabilityComboBox;
    private JComboBox<String> signalReqFor12dBSinadComboBox;
    private JComboBox<String> signalReqFor20dBQuietingComboBox;
    private JComboBox<String> dynamicRangeComboBox;
    private JComboBox<String> noiseFloorComboBox;
    private JComboBox<String> confidenceIntervalComboBox;
    private JComboBox<String> confidenceLevelComboBox;
    private JComboBox<String> requestedSignalQualityComboBox;
    private JComboBox<String> signalReqFor5PctBERComboBox;
    private JComboBox<String> requiredSizeOfTileComboBox;
    private JComboBox<String> maxVerticalSizeOfTileComboBox;
    private JComboBox<String> minVerticalSizeOfTileComboBox;
    private JComboBox<String> adjacentChannelRejectionComboBox;
    private JComboBox<String> contractualAreaReliabilityComboBox;
    private JComboBox<String> channelToDisplayComboBox;

    private JLabel channelToDisplayLabel;
    private JLabel dynamicRangeLabel;
    private JLabel maxSamplesPerTileLabel;
    private JLabel maxSpeedForValidSubSampleLabel;
    private JLabel maxNumberOfAllowedTilesLabel;
    private JLabel measReqPerSubSampleLabel;
    private JLabel minReqSampleRateLabel;
    private JLabel searchRadiusLabel;
    private JLabel minSamplesPerTileLabel;
    private JLabel minSpeedForValidSubSampleLabel;
    private JLabel noiseFloorLabel;
    private JLabel contractualAreaReliabilityComboBoxLabel;
    private JLabel confidenceIntervalComboBoxLabel;
    private JLabel confidenceLevelComboBoxLabel;
    private JLabel requestedSignalQualityLabel;
    private JLabel requiredGridPointsLabel;
    private JLabel predictedAreaReliabilityComboBoxLabel;
    private JLabel signalReqFor20dBQuietingLabel;
    private JLabel signalReqFor5PctBERLabel;
    private JLabel signalReqFor12dBSinadLabel;
    private JLabel requiredSizeOfTileLabel;
    private JLabel maxVerticalSizeOfTileComboBoxLabel;
    private JLabel minVerticalSizeOfTileComboBoxLabel;
    private JLabel wavelengthsPerSubSampleLabel;
    private JLabel adjacentChannelRejectionLabel;
    private JLabel timingRateTextFieldLabel;
    private JLabel calculatedNumberOfRequiredTilesTextFieldLabel;
    private JLabel confidenceStatementLabel;
    private JLabel totalTilesTestedLabel;
    private JLabel totalTilesNotAccessableLabel;
    private JLabel totalTilesFailedLabel;
    private JLabel totalTilesPassedLabel;
    private JLabel serviceAreaReliabilityLabel;

    private JFormattedTextField maxSamplesPerTileTextField;
    private JFormattedTextField minSamplesPerTileTextField;
    private JFormattedTextField requiredGridPointsTextField;
    private JFormattedTextField wavelengthsPerSubSampleTextField;
    private JFormattedTextField maxSpeedForValidSubSampleTextField;
    private JFormattedTextField measReqPerSubSampleTextField;
    private JFormattedTextField minReqSampleRateTextField;
    private JFormattedTextField minSpeedForValidSubSampleTextField;
    private JFormattedTextField searchRadiusTextField;
    private JFormattedTextField maxNumberOfAllowedTilesTextField;
    private JFormattedTextField timingRateTextField;
    private JFormattedTextField calculatedNumberOfRequiredTilesTextField;
    private JFormattedTextField totalTilesTestedTextField;
    private JFormattedTextField totalTilesNotAccessableTextField;
    private JFormattedTextField totalTilesFailedTextField;
    private JFormattedTextField totalTilesPassedTextField;
    private JFormattedTextField serviceAreaReliabilityTextField;

    private JTextArea confidenceStatement;

    private JScrollPane confidencePanelScrollPane;

    private ButtonGroup testCriteriaRadioButtonGroup;
    private JRadioButton greaterThanRadioButton;
    private JRadioButton windowRadioButton;

    private ButtonGroup accessabilityRadioButtonGroup;
    private JRadioButton eliminateRadioButton;
    private JRadioButton estimateRadioButton;
    private JRadioButton passRadioButton;

    private ButtonGroup signalQualityDisplaySelectorRadioButtonGroup;
    private JRadioButton displaySinadRadioButton;
    private JRadioButton displayRssiRadioButton;
    private JRadioButton displayBerRadioButton;

    private ButtonGroup calcBasisRadioButtonGroup;
    private JRadioButton calcBasedOnNumberAllowedTilesRadioButton;
    private JRadioButton calcBasedOnCARReqRadioButton;
    private JRadioButton calcBasedOnSARReqRadioButton;

    private ButtonGroup signalSampleRadioButtonGroup;
    private JRadioButton sampleBerRadioButton;
    private JRadioButton sampleRssiRadioButton;
    private JRadioButton sampleSinadRadioButton;

    private ButtonGroup manualDataCollectionModeRadioButtonGroup;
    private JRadioButton manualDataCollectionDisabledRadioButton;
    private JRadioButton manualDataCollectionContinuousModeRadioButton;
    private JRadioButton manualDataCollectionMousePressModeRadioButton;

    private ButtonGroup timingModeButtonGroup;
    private JRadioButton positionBasedTimingRadioButton;
    private JRadioButton speedBasedTimingRadioButton;
    private JRadioButton wavelengthsPerSubSampleRadioButton;
    private JRadioButton fixedTimingRateRadioButton;

    private transient ButtonModel manualDataCollectionModeModel;
    private transient ButtonModel signalQualityDisplayModeModel;
    private transient ButtonModel signalSampleModeModel;
    private transient ButtonModel accessabilityModel;
    private transient ButtonModel testCriteriaModel;
    private transient ButtonModel calcBasisModel;
    private transient ButtonModel timingModeModel;

    private JButton drawTestGridOnMapButton;
    private JButton tileSelectionModeButton;

    private JLabel gridEdgeHeightLabel;
    private JLabel gridEdgeWidthLabel;
    private JLabel gridEdgeBottomLabel;
    private JLabel gridEdgeRightLabel;
    private JLabel gridEdgeTopLabel;
    private JLabel gridEdgeLeftLabel;

    private JFormattedTextField gridWidthMilesTextField;
    private JFormattedTextField gridBottomTextField;
    private JFormattedTextField gridRightTextField;
    private JFormattedTextField gridTopTextField;
    private JFormattedTextField gridLeftTextField;
    private JFormattedTextField gridHeightMilesTextField;

    private JTextField jTextField100dBm;
    private JTextField jTextField110dBm;
    private JTextField jTextField120dBm;
    private JTextField jTextField50dBm;
    private JTextField jTextField60dBm;
    private JTextField jTextField70dBm;
    private JTextField jTextField80dBm;
    private JTextField jTextField90dBm;

    private JLabel jLabel100dBm;
    private JLabel jLabel110dBm;
    private JLabel jLabel120dBm;
    private JLabel jLabel50dBm;
    private JLabel jLabel60dBm;
    private JLabel jLabel70dBm;
    private JLabel jLabel80dBm;
    private JLabel jLabel90dBm;

    private JTextField jTextFieldTileSelectedColor;
    private JTextField jTextFieldTileInProgressColor;
    private JTextField jTextFieldTileCompleteColor;
    private JTextField jTextFieldTileInaccessableColor;
    private JTextField jTextFieldGridColor;

    private JLabel jLabelTileSelectedColorLabel;
    private JLabel jLabelTileInProgressColorLabel;
    private JLabel jLabelTileCompleteColorLabel;
    private JLabel jLabelTileInaccessableColorLabel;
    private JLabel jLabelGridColorLabel;

    private JTextField jTextField0sinad;
    private JTextField jTextField5sinad;
    private JTextField jTextField10sinad;
    private JTextField jTextField12sinad;
    private JTextField jTextField15sinad;
    private JTextField jTextField20sinad;
    private JTextField jTextField25sinad;
    private JTextField jTextField30sinad;

    private JLabel jLabel0sinad;
    private JLabel jLabel5sinad;
    private JLabel jLabel10sinad;
    private JLabel jLabel12sinad;
    private JLabel jLabel15sinad;
    private JLabel jLabel20sinad;
    private JLabel jLabel25sinad;
    private JLabel jLabel30sinad;

    private JTextField jTextField0ber;
    private JTextField jTextField5ber;
    private JTextField jTextField10ber;
    private JTextField jTextField15ber;
    private JTextField jTextField20ber;
    private JTextField jTextField25ber;
    private JTextField jTextField30ber;
    private JTextField jTextField35ber;

    private JLabel jLabel0ber;
    private JLabel jLabel5ber;
    private JLabel jLabel10ber;
    private JLabel jLabel15ber;
    private JLabel jLabel20ber;
    private JLabel jLabel25ber;
    private JLabel jLabel30ber;
    private JLabel jLabel35ber;

    private NumberFormat latFormat;
    private NumberFormat lonFormat;
    private NumberFormat heightFormat;
    private NumberFormat widthFormat;

    private final transient CoverageTestObject cto;

    public CoverageTestComponent(CoverageTestObject cto) {
        this.cto = cto;

        initializeComponents();

        updateComponents();
        configureRxSpecs();
        configureListeners();
        configureDialog();

        processAutoCalculations();
    }

    public void updateStatistics() {
        // TODO:
    }
    
    private boolean isEqual(double n, double v) {
    	return Math.abs(n - v) < 0.0000001D;
    }
    
    private void processAutoCalculations() {
        if (cto.isEnableAutoCalc()) {
            calculatedNumberOfRequiredTilesTextField.setText(String.valueOf(cto.getNumberOfRequiredTiles()));
            if (cto.getPredictedAreaReliability() - cto.getContractualAreaReliability() <= 0.0) {
                this.predictedAreaReliabilityComboBoxLabel.setForeground(Color.RED);
                this.contractualAreaReliabilityComboBoxLabel.setForeground(Color.RED);
            } else {
                this.predictedAreaReliabilityComboBoxLabel.setForeground(Color.BLACK);
                this.contractualAreaReliabilityComboBoxLabel.setForeground(Color.BLACK);
            }
            if (cto.getNumberOfRequiredTiles() > cto.getMaxNumberOfAllowedTiles()) {
                this.maxNumberOfAllowedTilesLabel.setForeground(Color.RED);
            } else {
                this.maxNumberOfAllowedTilesLabel.setForeground(Color.BLACK);
            }

            cto.getPropertyChangeSupport().firePropertyChange(CoverageTestObject.PROPERTY_CHANGE, null, true);

            /* The Greater Than Test is defined such that the percentage of test locations that
            meet the CPC equal or exceed the service area reliability target. This
            necessitates an overdesign of the system by e to provide the statistical margins
            for passing the conformance test as defined. For this test configuration, Z has
            one-tail and e is the amount of overdesign, expressed as a decimal fraction. */
            final String greaterThanStatement = "We are "
                    + cto.getConfidenceLevel() * 100.0 + "% "
                    + "confident that the actual performance "
                    + "can be determined, to within +/- "
                    + cto.getConfidenceInterval() * 100.0 / 2.0 + "% "
                    + "of the customer performance requirement for the system to work in "
                    + cto.getContractualAreaReliability() * 100.0 + "% "
                    + "of all locations in the specified coverage area, providing random locations within "
                    + "a minimum of "
                    + cto.getNumberOfRequiredTiles() + " "
                    + "tiles are tested.";

            /* The Acceptance Window test allows the percentage of test locations that meet
            the CPC to fall within an error window, +/- e, which is centered on the service area
            target reliability, to consider the acceptance test a pass. This eliminates the
            necessity for over design, but necessitates a two tail Z that increases the
            number of test samples to be evaluated. */
            final String windowStatement = "We are "
                    + cto.getConfidenceLevel() * 100.0 + "% "
                    + "confident that no more than "
                    + ((cto.getContractualAreaReliability() * 100.0)
                            - cto.getConfidenceInterval() * 100.0) + "% "
                    + "of all locations in the specified coverage area will fail to perform "
                    + "without errors, providing random locations within a minimum of "
                    + cto.getNumberOfRequiredTiles() + " "
                    + "tiles are tested.";

            if (cto.getTestCriteria() == TestCriteria.GREATER_THAN) {
                confidenceStatement.setText(greaterThanStatement);
            } else if (cto.getTestCriteria() == TestCriteria.WINDOW) {
                confidenceStatement.setText(windowStatement);
            }

        } else {
            calculatedNumberOfRequiredTilesTextField.setText("");
        }
    }

    private void configureDialog() {
        final Toolkit tk = Toolkit.getDefaultToolkit();
        final Dimension screenSize = tk.getScreenSize();
        add(getGUI());
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Coverage Test Settings");
        pack();
        setLocation((screenSize.width / 2) - (getWidth() / 2), (screenSize.height / 2) - (getHeight() / 2));
        setVisible(true);
    }

    private void initializeComponents() {
    	final JPanel sampleDistributionAcrossTilePanel;
    	final JPanel testCriteriaPanel;
        sampleDistributionAcrossTilePanel = new JPanel();
        sampleTimingPanel = new JPanel();
        testCriteriaPanel = new JPanel();
        signalQualityDisplaySelectorPanel = new JPanel();
        manualDataCollectionModeSelectorPanel = new JPanel();
        rssiDataPointColoringPanel = new JPanel();
        automaticParameterCalculationPanel = new JPanel();
        accessabilityPanel = new JPanel();
        receiverPerformanceParametersPanel = new JPanel();
        reliabilityDesignTargetsPanel = new JPanel();
        testParameterPanel = new JPanel();
        tileConstraintsPanel = new JPanel();
        valuesToBeSampledPanel = new JPanel();
        testGridDimensionPanel = new JPanel();
        mapImageModelPanel = new JPanel();
        sinadDataPointColoringPanel = new JPanel();
        berDataPointColoringPanel = new JPanel();
        berDataPointColoringPanel = new JPanel();
        testParameterPanel = new JPanel();
        tileShadingPanel = new JPanel();
        passFailCriteriaSelectorPanel = new JPanel();
        autoCalculatedCoverageTestParametersPanel = new JPanel();
        measuredPerformancePanel = new JPanel();

        channelToDisplayComboBox = new JComboBox<>();
        requiredSizeOfTileComboBox = new JComboBox<>();
        maxVerticalSizeOfTileComboBox = new JComboBox<>();
        minVerticalSizeOfTileComboBox = new JComboBox<>();
        noiseFloorComboBox = new JComboBox<>();
        dynamicRangeComboBox = new JComboBox<>();
        adjacentChannelRejectionComboBox = new JComboBox<>();
        signalReqFor12dBSinadComboBox = new JComboBox<>();
        signalReqFor20dBQuietingComboBox = new JComboBox<>();
        signalReqFor5PctBERComboBox = new JComboBox<>();
        requestedSignalQualityComboBox = new JComboBox<>();
        confidenceIntervalComboBox = new JComboBox<>();
        confidenceLevelComboBox = new JComboBox<>();
        contractualAreaReliabilityComboBox = new JComboBox<>();
        predictedAreaReliabilityComboBox = new JComboBox<>();

        channelToDisplayLabel = new JLabel();
        requiredSizeOfTileLabel = new JLabel();
        maxVerticalSizeOfTileComboBoxLabel = new JLabel();
        minVerticalSizeOfTileComboBoxLabel = new JLabel();
        maxSamplesPerTileLabel = new JLabel();
        minSamplesPerTileLabel = new JLabel();
        maxNumberOfAllowedTilesLabel = new JLabel();
        noiseFloorLabel = new JLabel();
        dynamicRangeLabel = new JLabel();
        adjacentChannelRejectionLabel = new JLabel();
        timingRateTextFieldLabel = new JLabel();
        calculatedNumberOfRequiredTilesTextFieldLabel = new JLabel();
        confidenceStatementLabel = new JLabel();
        signalReqFor12dBSinadLabel = new JLabel();
        signalReqFor20dBQuietingLabel = new JLabel();
        signalReqFor5PctBERLabel = new JLabel();
        wavelengthsPerSubSampleLabel = new JLabel();
        minReqSampleRateLabel = new JLabel();
        searchRadiusLabel = new JLabel();
        measReqPerSubSampleLabel = new JLabel();
        maxSpeedForValidSubSampleLabel = new JLabel();
        minSpeedForValidSubSampleLabel = new JLabel();
        requiredGridPointsLabel = new JLabel();
        requestedSignalQualityLabel = new JLabel();
        confidenceIntervalComboBoxLabel = new JLabel();
        confidenceLevelComboBoxLabel = new JLabel();
        contractualAreaReliabilityComboBoxLabel = new JLabel();
        predictedAreaReliabilityComboBoxLabel = new JLabel();

        timingRateTextField = new JFormattedTextField();
        maxSamplesPerTileTextField = new JFormattedTextField();
        minSamplesPerTileTextField = new JFormattedTextField();
        wavelengthsPerSubSampleTextField = new JFormattedTextField();
        requiredGridPointsTextField = new JFormattedTextField();
        minSpeedForValidSubSampleTextField = new JFormattedTextField();
        maxSpeedForValidSubSampleTextField = new JFormattedTextField();
        measReqPerSubSampleTextField = new JFormattedTextField();
        minReqSampleRateTextField = new JFormattedTextField();
        searchRadiusTextField = new JFormattedTextField();
        maxNumberOfAllowedTilesTextField = new JFormattedTextField();

        calculatedNumberOfRequiredTilesTextField = new JFormattedTextField();
        calculatedNumberOfRequiredTilesTextField.setEditable(false);

        totalTilesTestedTextField = new JFormattedTextField();
        totalTilesTestedTextField.setEditable(false);
        totalTilesTestedLabel = new JLabel("Number of Tiles Compleeted");
        totalTilesTestedLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        
        totalTilesNotAccessableTextField = new JFormattedTextField();
        totalTilesNotAccessableTextField.setEditable(false);
        totalTilesNotAccessableLabel = new JLabel("Number of Tiles Not Accessable");
        totalTilesNotAccessableLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        
        totalTilesFailedTextField = new JFormattedTextField();
        totalTilesFailedTextField.setEditable(false);
        totalTilesFailedLabel = new JLabel("Number of Tiles Failed");
        totalTilesFailedLabel.setHorizontalAlignment(SwingConstants.RIGHT);
             
        totalTilesPassedTextField = new JFormattedTextField();
        totalTilesPassedTextField.setEditable(false);
        totalTilesPassedLabel = new JLabel("Number of Tiles Passed");
        totalTilesPassedLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        
        serviceAreaReliabilityTextField = new JFormattedTextField();
        serviceAreaReliabilityTextField.setEditable(false);
        serviceAreaReliabilityLabel = new JLabel("Measured Service Area Reliability");
        serviceAreaReliabilityLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        confidenceStatement = new JTextArea();
        confidenceStatement.setColumns(20);
        confidenceStatement.setRows(4);
        confidenceStatement.setEditable(false);
        confidenceStatement.setLineWrap(true);
        confidenceStatement.setWrapStyleWord(true);

        confidencePanelScrollPane = new JScrollPane();
        confidencePanelScrollPane.setViewportView(confidenceStatement);

        calcBasisRadioButtonGroup = new ButtonGroup();
        calcBasedOnCARReqRadioButton = new JRadioButton();
        calcBasedOnSARReqRadioButton = new JRadioButton();
        calcBasedOnNumberAllowedTilesRadioButton = new JRadioButton();

        signalSampleRadioButtonGroup = new ButtonGroup();
        sampleRssiRadioButton = new JRadioButton();
        sampleBerRadioButton = new JRadioButton();
        sampleSinadRadioButton = new JRadioButton();

        showGridSquareShadingCheckBox = new JCheckBox();
        showTestGridCheckBox = new JCheckBox();
        showRingsCheckBox = new JCheckBox();
        showLinesCheckBox = new JCheckBox();
        showQuadsCheckBox = new JCheckBox();
        alertOnMinimumSamplesPerTileAcquiredCheckBox = new JCheckBox();
        showSignalMarkersCheckBox = new JCheckBox();
        enableAutoCalcCheckBox = new JCheckBox();
        testRxSpecsValidForTestCheckBox = new JCheckBox();

        testCriteriaRadioButtonGroup = new ButtonGroup();
        greaterThanRadioButton = new JRadioButton();
        windowRadioButton = new JRadioButton();

        accessabilityRadioButtonGroup = new ButtonGroup();
        eliminateRadioButton = new JRadioButton();
        estimateRadioButton = new JRadioButton();
        passRadioButton = new JRadioButton();

        signalQualityDisplaySelectorRadioButtonGroup = new ButtonGroup();
        displaySinadRadioButton = new JRadioButton();
        displayRssiRadioButton = new JRadioButton();
        displayBerRadioButton = new JRadioButton();

        manualDataCollectionModeRadioButtonGroup = new ButtonGroup();
        manualDataCollectionDisabledRadioButton = new JRadioButton();
        manualDataCollectionContinuousModeRadioButton = new JRadioButton();
        manualDataCollectionMousePressModeRadioButton = new JRadioButton();

        timingModeButtonGroup = new ButtonGroup();
        positionBasedTimingRadioButton = new JRadioButton();
        speedBasedTimingRadioButton = new JRadioButton();
        wavelengthsPerSubSampleRadioButton = new JRadioButton();
        fixedTimingRateRadioButton = new JRadioButton();

        jLabel50dBm = new JLabel();
        jTextField50dBm = new JTextField();
        jLabel60dBm = new JLabel();
        jTextField60dBm = new JTextField();
        jLabel70dBm = new JLabel();
        jTextField70dBm = new JTextField();
        jLabel80dBm = new JLabel();
        jTextField80dBm = new JTextField();
        jLabel90dBm = new JLabel();
        jTextField90dBm = new JTextField();
        jLabel100dBm = new JLabel();
        jTextField100dBm = new JTextField();
        jLabel110dBm = new JLabel();
        jTextField110dBm = new JTextField();
        jLabel120dBm = new JLabel();
        jTextField120dBm = new JTextField();

        jTextFieldTileSelectedColor = new JTextField();
        jTextFieldTileInProgressColor = new JTextField();
        jTextFieldTileCompleteColor = new JTextField();
        jTextFieldTileInaccessableColor = new JTextField();
        jTextFieldGridColor = new JTextField();

        jLabelTileSelectedColorLabel = new JLabel();
        jLabelTileInProgressColorLabel = new JLabel();
        jLabelTileInaccessableColorLabel = new JLabel();
        jLabelTileCompleteColorLabel = new JLabel();
        jLabelGridColorLabel = new JLabel();

        jTextFieldTileSelectedColor.setBackground(cto.getTileSelectedColor());
        jTextFieldTileInProgressColor.setBackground(cto.getTileInProgressColor());
        jTextFieldTileCompleteColor.setBackground(cto.getTileCompleteColor());
        jTextFieldTileInaccessableColor.setBackground(cto.getTileInaccessableColor());
        jTextFieldGridColor.setBackground(cto.getGridColor());

        jTextFieldTileSelectedColor.setEditable(false);
        jTextFieldTileSelectedColor.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

        jTextFieldTileInProgressColor.setEditable(false);
        jTextFieldTileInProgressColor.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

        jTextFieldTileCompleteColor.setEditable(false);
        jTextFieldTileCompleteColor.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

        jTextFieldTileInaccessableColor.setEditable(false);
        jTextFieldTileInaccessableColor.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

        jTextFieldGridColor.setEditable(false);
        jTextFieldGridColor.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

        jTextField50dBm.setBackground(cto.getColor50dBm());
        jTextField60dBm.setBackground(cto.getColor60dBm());
        jTextField70dBm.setBackground(cto.getColor70dBm());
        jTextField80dBm.setBackground(cto.getColor80dBm());
        jTextField90dBm.setBackground(cto.getColor90dBm());
        jTextField100dBm.setBackground(cto.getColor100dBm());
        jTextField110dBm.setBackground(cto.getColor110dBm());
        jTextField120dBm.setBackground(cto.getColor120dBm());

        jTextField50dBm.setEditable(false);
        jTextField50dBm.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        jTextField60dBm.setEditable(false);
        jTextField60dBm.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        jTextField70dBm.setEditable(false);
        jTextField70dBm.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        jTextField80dBm.setEditable(false);
        jTextField80dBm.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        jTextField90dBm.setEditable(false);
        jTextField90dBm.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        jTextField100dBm.setEditable(false);
        jTextField100dBm.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        jTextField110dBm.setEditable(false);
        jTextField110dBm.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        jTextField120dBm.setEditable(false);
        jTextField120dBm.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

        jLabel0sinad = new JLabel();
        jTextField0sinad = new JTextField();
        jLabel5sinad = new JLabel();
        jTextField5sinad = new JTextField();
        jLabel10sinad = new JLabel();
        jTextField10sinad = new JTextField();
        jLabel12sinad = new JLabel();
        jTextField12sinad = new JTextField();
        jLabel15sinad = new JLabel();
        jTextField15sinad = new JTextField();
        jLabel20sinad = new JLabel();
        jTextField20sinad = new JTextField();
        jLabel25sinad = new JLabel();
        jTextField25sinad = new JTextField();
        jLabel30sinad = new JLabel();
        jTextField30sinad = new JTextField();

        jTextField0sinad.setBackground(cto.getColor0sinad());
        jTextField5sinad.setBackground(cto.getColor5sinad());
        jTextField10sinad.setBackground(cto.getColor10sinad());
        jTextField12sinad.setBackground(cto.getColor12sinad());
        jTextField15sinad.setBackground(cto.getColor15sinad());
        jTextField20sinad.setBackground(cto.getColor20sinad());
        jTextField25sinad.setBackground(cto.getColor25sinad());
        jTextField30sinad.setBackground(cto.getColor30sinad());

        jTextField0sinad.setEditable(false);
        jTextField0sinad.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        jTextField5sinad.setEditable(false);
        jTextField5sinad.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        jTextField10sinad.setEditable(false);
        jTextField10sinad.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        jTextField12sinad.setEditable(false);
        jTextField12sinad.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        jTextField15sinad.setEditable(false);
        jTextField15sinad.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        jTextField20sinad.setEditable(false);
        jTextField20sinad.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        jTextField25sinad.setEditable(false);
        jTextField25sinad.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        jTextField30sinad.setEditable(false);
        jTextField30sinad.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

        jLabel0ber = new JLabel();
        jTextField0ber = new JTextField();
        jLabel5ber = new JLabel();
        jTextField5ber = new JTextField();
        jLabel10ber = new JLabel();
        jTextField10ber = new JTextField();
        jLabel35ber = new JLabel();
        jTextField35ber = new JTextField();
        jLabel15ber = new JLabel();
        jTextField15ber = new JTextField();
        jLabel20ber = new JLabel();
        jTextField20ber = new JTextField();
        jLabel25ber = new JLabel();
        jTextField25ber = new JTextField();
        jLabel30ber = new JLabel();
        jTextField30ber = new JTextField();

        jTextField0ber.setBackground(cto.getColor0ber());
        jTextField5ber.setBackground(cto.getColor5ber());
        jTextField10ber.setBackground(cto.getColor10ber());
        jTextField35ber.setBackground(cto.getColor35ber());
        jTextField15ber.setBackground(cto.getColor15ber());
        jTextField20ber.setBackground(cto.getColor20ber());
        jTextField25ber.setBackground(cto.getColor25ber());
        jTextField30ber.setBackground(cto.getColor30ber());

        jTextField0ber.setEditable(false);
        jTextField0ber.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        jTextField5ber.setEditable(false);
        jTextField5ber.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        jTextField10ber.setEditable(false);
        jTextField10ber.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        jTextField35ber.setEditable(false);
        jTextField35ber.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        jTextField15ber.setEditable(false);
        jTextField15ber.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        jTextField20ber.setEditable(false);
        jTextField20ber.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        jTextField25ber.setEditable(false);
        jTextField25ber.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        jTextField30ber.setEditable(false);
        jTextField30ber.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

        gridWidthMilesTextField = new JFormattedTextField();
        gridHeightMilesTextField = new JFormattedTextField();
        gridTopTextField = new JFormattedTextField();
        gridLeftTextField = new JFormattedTextField();
        gridBottomTextField = new JFormattedTextField();
        gridRightTextField = new JFormattedTextField();

        applyDoubleFilter(gridRightTextField);
        applyDoubleFilter(gridBottomTextField);
        applyDoubleFilter(gridLeftTextField);
        applyDoubleFilter(gridTopTextField);
        applyDoubleFilter(gridHeightMilesTextField);
        applyDoubleFilter(gridWidthMilesTextField);
        applyDoubleFilter(maxSpeedForValidSubSampleTextField);
        applyDoubleFilter(minSpeedForValidSubSampleTextField);
        applyDoubleFilter(searchRadiusTextField);

        applyIntegerFilter(requiredGridPointsTextField);
        applyIntegerFilter(maxNumberOfAllowedTilesTextField);
        applyIntegerFilter(wavelengthsPerSubSampleTextField);
        applyIntegerFilter(minReqSampleRateTextField);
        applyIntegerFilter(measReqPerSubSampleTextField);
        applyIntegerFilter(timingRateTextField);

        gridEdgeWidthLabel = new JLabel();
        gridEdgeTopLabel = new JLabel();
        gridEdgeLeftLabel = new JLabel();
        gridEdgeBottomLabel = new JLabel();
        gridEdgeRightLabel = new JLabel();
        gridEdgeHeightLabel = new JLabel();

        tileSelectionModeButton = new JButton("Activate Tile Selection Mode");
        tileSelectionModeButton.setMultiClickThreshhold(50L);
        drawTestGridOnMapButton = new JButton("Draw Test Grid on AbstractMap");
        drawTestGridOnMapButton.setMultiClickThreshhold(50L);

        latFormat = new DecimalFormat("00.00000");
        lonFormat = new DecimalFormat("000.00000");
        heightFormat = new DecimalFormat("000.0000");
        widthFormat = new DecimalFormat("000.0000");

        okButton = new JButton("OK");
        okButton.setMultiClickThreshhold(50L);

        cancelButton = new JButton("Cancel");
        cancelButton.setMultiClickThreshhold(50L);

        applyButton = new JButton("Apply");
        applyButton.setMultiClickThreshhold(50L);

        alertOnMinimumSamplesPerTileAcquiredCheckBox.setText("Alert on Minimum Samples per Tile Acqured");
        showGridSquareShadingCheckBox.setText("Show Progress Colors Over Grid Squares");
        showTestGridCheckBox.setText("Show Test Grid");
        showRingsCheckBox.setText("Show Rings");
        showQuadsCheckBox.setText("Show Quads");
        showLinesCheckBox.setText("Show Lines");
        enableAutoCalcCheckBox.setText("Enable Automatic Calculations");
        sampleRssiRadioButton.setText("Sample RSSI Values");
        sampleBerRadioButton.setText("Sample Bit Error Rate");
        sampleSinadRadioButton.setText("Sample SINAD Values");
        testRxSpecsValidForTestCheckBox.setText("Test Receiver Specifications Valid for Test");

        wavelengthsPerSubSampleTextField.setHorizontalAlignment(SwingConstants.RIGHT);
        requiredGridPointsTextField.setHorizontalAlignment(SwingConstants.RIGHT);
        minSpeedForValidSubSampleTextField.setHorizontalAlignment(SwingConstants.RIGHT);
        maxSpeedForValidSubSampleTextField.setHorizontalAlignment(SwingConstants.RIGHT);
        measReqPerSubSampleTextField.setHorizontalAlignment(SwingConstants.RIGHT);
        minReqSampleRateTextField.setHorizontalAlignment(SwingConstants.RIGHT);
        searchRadiusTextField.setHorizontalAlignment(SwingConstants.RIGHT);
        maxNumberOfAllowedTilesTextField.setHorizontalAlignment(SwingConstants.RIGHT);
        minSamplesPerTileTextField.setHorizontalAlignment(SwingConstants.RIGHT);
        maxSamplesPerTileTextField.setHorizontalAlignment(SwingConstants.RIGHT);
        timingRateTextField.setHorizontalAlignment(SwingConstants.RIGHT);
        calculatedNumberOfRequiredTilesTextField.setHorizontalAlignment(SwingConstants.RIGHT);

        maxNumberOfAllowedTilesLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        maxNumberOfAllowedTilesLabel.setText("Maximum Number of Allowed Tiles");

        maxSamplesPerTileLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        maxSamplesPerTileLabel.setText("Maximum Samples per Tile");

        minSamplesPerTileLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        minSamplesPerTileLabel.setText("Minimum Samples per Tile");

        gridWidthMilesTextField.setHorizontalAlignment(SwingConstants.CENTER);
        gridEdgeWidthLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        gridEdgeWidthLabel.setText("Width of Test Grid in Miles");

        gridEdgeTopLabel.setHorizontalAlignment(SwingConstants.LEFT);
        gridEdgeTopLabel.setHorizontalTextPosition(SwingConstants.LEFT);
        gridEdgeTopLabel.setText("Latitude of Upper Left Corner of Grid");
        gridTopTextField.setHorizontalAlignment(SwingConstants.CENTER);

        gridEdgeLeftLabel.setHorizontalAlignment(SwingConstants.LEFT);
        gridEdgeLeftLabel.setHorizontalTextPosition(SwingConstants.LEFT);
        gridEdgeLeftLabel.setText("Longitude of Upper Left Corner of Grid");
        gridLeftTextField.setHorizontalAlignment(SwingConstants.CENTER);

        gridBottomTextField.setHorizontalAlignment(SwingConstants.CENTER);
        gridEdgeBottomLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        gridEdgeBottomLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
        gridEdgeBottomLabel.setText("Latitude of Lower Right Corner of Grid");

        gridRightTextField.setHorizontalAlignment(SwingConstants.CENTER);
        gridEdgeRightLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        gridEdgeRightLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
        gridEdgeRightLabel.setText("Longitude of Lower Right Corner of Grid");

        gridHeightMilesTextField.setHorizontalAlignment(SwingConstants.CENTER);
        gridEdgeHeightLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        gridEdgeHeightLabel.setText("Height of Test Grid in Miles");

        positionBasedTimingRadioButton.setText("Measure Minimum Required Samples Per GPS Location Report");
        speedBasedTimingRadioButton.setText("Set Measurement Rate Based on Speed of Vehicle");
        wavelengthsPerSubSampleRadioButton.setText("Measure at Rate to Achieve Requred Measurements per Wavelength");
        fixedTimingRateRadioButton.setText("Measure at a Continuous Fixed Rate Regardless of Speed or Position");
        manualDataCollectionDisabledRadioButton.setText("Disable Manual Data Sample Collection");
        manualDataCollectionContinuousModeRadioButton.setText("Continuous Rate Data Sample Collection");
        manualDataCollectionMousePressModeRadioButton.setText("Data Sample Collection on Mouse Button Press");
        calcBasedOnCARReqRadioButton.setText("Based on Contractural Area Reliability Requirements");
        calcBasedOnSARReqRadioButton.setText("Based on Service Area Reliability Requirements");
        eliminateRadioButton.setText("Eliminate Tile from Test");
        estimateRadioButton.setText("Estimate Performance");
        passRadioButton.setText("Pass the Tile");
        calcBasedOnNumberAllowedTilesRadioButton.setText("Based on Size and Number of Allowed Tiles");
        displaySinadRadioButton.setText("Display Dot Color Based on SINAD Mesurement");
        displayRssiRadioButton.setText("Display Dot Color Based on RSSI");
        displayBerRadioButton.setText("Display Dot Color Based on Bit Error Rate");
        greaterThanRadioButton.setText("Greater Than Test");
        windowRadioButton.setText("Window Test");
        greaterThanRadioButton.setToolTipText("The test will pass if the percentage of test locations that\r\n" + " meet the CPC equal or exceed the service area reliability target.");
        windowRadioButton.setToolTipText("The test will pass if the percentage of test locations that meet\r\n" + " the CPC fall within an error window, centered about the error margin, which is centered on the service area\r\n" + " target reliability.");

        mapImageModelPanel.setBackground(new Color(153, 255, 153));
        mapImageModelPanel.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
        automaticParameterCalculationPanel.setBorder(BorderFactory.createTitledBorder("Automatic Parameter Calculation"));
        rssiDataPointColoringPanel.setBorder(BorderFactory.createTitledBorder("RSSI Data Point Coloring"));
        tileConstraintsPanel.setBorder(BorderFactory.createTitledBorder("Externally Forced Tile Constraints"));
        autoCalculatedCoverageTestParametersPanel.setBorder(BorderFactory.createTitledBorder("Auto Calculated Coverage Test Parameters"));
        measuredPerformancePanel.setBorder(BorderFactory.createTitledBorder("Measured Performance"));
        receiverPerformanceParametersPanel.setBorder(BorderFactory.createTitledBorder("Receiver Performance Parameters"));
        signalQualityDisplaySelectorPanel.setBorder(BorderFactory.createTitledBorder("Signal Quality Display"));
        manualDataCollectionModeSelectorPanel.setBorder(BorderFactory.createTitledBorder("Manual Data Collection Mode"));
        sampleTimingPanel.setBorder(BorderFactory.createTitledBorder("Sampling Options"));
        testParameterPanel.setBorder(BorderFactory.createTitledBorder("Sample Constraints"));
        reliabilityDesignTargetsPanel.setBorder(BorderFactory.createTitledBorder("Reliability Design Targets"));
        testGridDimensionPanel.setBorder(BorderFactory.createTitledBorder("Test Grid Dimensions"));
        berDataPointColoringPanel.setBorder(BorderFactory.createTitledBorder("BER Data Point Coloring"));
        sinadDataPointColoringPanel.setBorder(BorderFactory.createTitledBorder("SINAD Data Point Coloring"));
        accessabilityPanel.setBorder(BorderFactory.createTitledBorder("Inaccessable Tiles"));
        testCriteriaPanel.setBorder(BorderFactory.createTitledBorder("Test Criteria"));
        valuesToBeSampledPanel.setBorder(BorderFactory.createTitledBorder("Measurements for Calculation"));
        sampleDistributionAcrossTilePanel.setBorder(BorderFactory.createTitledBorder("Sample Distribution Across Tile"));
        tileShadingPanel.setBorder(BorderFactory.createTitledBorder("Test Tile Shading Colors"));
        passFailCriteriaSelectorPanel.setBorder(BorderFactory.createTitledBorder("Pass / Fail Criteria"));

        channelToDisplayComboBox.setModel(new DefaultComboBoxModel<>(CoverageTestObject.getChannelsToDisplay()));
        channelToDisplayLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        channelToDisplayLabel.setText("Channel To Display on AbstractMap");

        requiredSizeOfTileComboBox.setModel(new DefaultComboBoxModel<>(CoverageTestObject.getTileSizeValues()));
        requiredSizeOfTileLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        requiredSizeOfTileLabel.setText("Required Size of Tile (Arc-Seconds)");
        requiredSizeOfTileComboBox.setEditable(true);

        maxVerticalSizeOfTileComboBox.setModel(new DefaultComboBoxModel<>(CoverageTestObject.getTileSizeValues()));
        maxVerticalSizeOfTileComboBoxLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        maxVerticalSizeOfTileComboBoxLabel.setText("Maximum Allowed Size of Tile (Arc-Seconds)");
        maxVerticalSizeOfTileComboBox.setEditable(true);

        minVerticalSizeOfTileComboBox.setModel(new DefaultComboBoxModel<>(CoverageTestObject.getTileSizeValues()));
        minVerticalSizeOfTileComboBoxLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        minVerticalSizeOfTileComboBoxLabel.setText("Minimum Allowed Size of Tile (Arc-Seconds)");
        minVerticalSizeOfTileComboBox.setEditable(true);

        noiseFloorComboBox.setModel(new DefaultComboBoxModel<>(CoverageTestObject.getdBmValues()));
        noiseFloorLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        noiseFloorLabel.setText("Noise Floor");

        dynamicRangeComboBox.setModel(new DefaultComboBoxModel<>(CoverageTestObject.getdBValues()));
        dynamicRangeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        dynamicRangeLabel.setText("Dynamic Range");

        adjacentChannelRejectionComboBox.setModel(new DefaultComboBoxModel<>(CoverageTestObject.getdBValues()));
        adjacentChannelRejectionLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        adjacentChannelRejectionLabel.setText("Adjacent Channel Rejection");

        confidenceStatementLabel.setHorizontalAlignment(SwingConstants.LEFT);
        confidenceStatementLabel.setText("Confidence Statement");

        timingRateTextFieldLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        timingRateTextFieldLabel.setText("Timing Rate (milliSeconds)");

        calculatedNumberOfRequiredTilesTextFieldLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        calculatedNumberOfRequiredTilesTextFieldLabel.setText("Calculated Number of Tiles Required for Test");

        signalReqFor20dBQuietingComboBox.setModel(new DefaultComboBoxModel<>(CoverageTestObject.getdBmValues()));
        signalReqFor20dBQuietingLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        signalReqFor20dBQuietingLabel.setText("Signal Required at Antenna Port for 20 dB Quieting");

        signalReqFor12dBSinadComboBox.setModel(new DefaultComboBoxModel<>(CoverageTestObject.getdBmValues()));
        signalReqFor12dBSinadLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        signalReqFor12dBSinadLabel.setText("Signal Required at Antenna Port for 12 dB SINAD");

        signalReqFor5PctBERComboBox.setModel(new DefaultComboBoxModel<>(CoverageTestObject.getdBmValues()));
        signalReqFor5PctBERLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        signalReqFor5PctBERLabel.setText("Signal Required at Antenna Port for 5% BER ");

        requestedSignalQualityComboBox.setModel(new DefaultComboBoxModel<>());
        requestedSignalQualityLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        requestedSignalQualityLabel.setText("Customer Required Signal Quality");

        confidenceIntervalComboBox.setModel(new DefaultComboBoxModel<>(CoverageTestObject.getConfidenceIntervalValues()));
        confidenceIntervalComboBoxLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        confidenceIntervalComboBoxLabel.setText("Full Confidence Interval");
        confidenceIntervalComboBoxLabel.setToolTipText("How much the true signal quality could likely vary.");

        confidenceLevelComboBox.setModel(new DefaultComboBoxModel<>(CoverageTestObject.getConfidenceLevelValues()));
        confidenceLevelComboBoxLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        confidenceLevelComboBoxLabel.setText("Confidence Level");
        confidenceLevelComboBoxLabel.setToolTipText("How confident we are that the true signal quality will meet or exceed the requirement.");

        contractualAreaReliabilityComboBox.setModel(new DefaultComboBoxModel<>(CoverageTestObject.getAreaReliabilityValues()));
        contractualAreaReliabilityComboBoxLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        contractualAreaReliabilityComboBoxLabel.setText("Contractual Area Reliability" + "");
        contractualAreaReliabilityComboBoxLabel.setToolTipText("The probability that the required signal quality exists at every location.");

        predictedAreaReliabilityComboBox.setModel(new DefaultComboBoxModel<>(CoverageTestObject.getAreaReliabilityValues()));
        predictedAreaReliabilityComboBoxLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        predictedAreaReliabilityComboBoxLabel.setText("Predicted Area Reliability");

        wavelengthsPerSubSampleTextField.setText(String.valueOf(cto.getWavelengthsPerSubSample()));
        wavelengthsPerSubSampleLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        wavelengthsPerSubSampleLabel.setText("Wavelengths per Sub-Sample");

        searchRadiusTextField.setText(String.valueOf(cto.getSearchRadius()));
        searchRadiusLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        searchRadiusLabel.setText("Search Radius (Feet)");
        searchRadiusLabel.setToolTipText("");

        minReqSampleRateTextField.setText(String.valueOf(cto.getMinAllowedSampleRateSamplesPerSecond()));
        minReqSampleRateLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        minReqSampleRateLabel.setText("Minimmum Required Sample Rate (S/sec)");
        minReqSampleRateLabel.setToolTipText("");

        measReqPerSubSampleTextField.setText(String.valueOf(cto.getMinMeasurementsRequiredPerSubSample()));
        measReqPerSubSampleLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        measReqPerSubSampleLabel.setText("Min Measurements Required per Sub-Sample");
        measReqPerSubSampleLabel.setToolTipText("");

        maxSpeedForValidSubSampleTextField.setText(String.valueOf(cto.getMaxSpeedFPMForValidSample()));
        maxSpeedForValidSubSampleLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        maxSpeedForValidSubSampleLabel.setText("Maximum Speed for Valid Sub-Sample (FPS)");
        maxSpeedForValidSubSampleLabel.setToolTipText("");

        minSpeedForValidSubSampleTextField.setText(String.valueOf(cto.getMinSpeedFPMForValidSample()));
        minSpeedForValidSubSampleLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        minSpeedForValidSubSampleLabel.setText("Minimum Speed for Valid Sub-Sample (FPS)");
        minSpeedForValidSubSampleLabel.setToolTipText("");

        requiredGridPointsTextField.setText(String.valueOf(cto.getRequiredGridPoints()));
        requiredGridPointsLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        requiredGridPointsLabel.setText("Required Grid Points");

        jLabelTileSelectedColorLabel.setText("Tile Selected for Testing Color");
        jLabelTileInProgressColorLabel.setText("Tile Testing In Progress Color");
        jLabelTileCompleteColorLabel.setText("Tile Testing Complete Color");
        jLabelTileInaccessableColorLabel.setText("Tile Inaccessable Color");
        jLabelGridColorLabel.setText("Grid Color");

        jLabel50dBm.setText("-50 dBm -> -59 dBm");
        jLabel60dBm.setText("-60 dBm -> -69 dBm");
        jLabel70dBm.setText("-70 dBm -> -79 dBm");
        jLabel80dBm.setText("-80 dBm -> -89 dBm");
        jLabel90dBm.setText("-90 dBm -> -99 dBm");
        jLabel100dBm.setText("-100 dBm -> -109 dBm");
        jLabel110dBm.setText("-110 dBm -> -119 dBm");
        jLabel120dBm.setText("-120 dBm -> -129 dBm");

        jLabel0sinad.setText("0 dB -> 4 dB");
        jLabel5sinad.setText("5 dB -> 9 dB");
        jLabel10sinad.setText("10 dB -> 11 dB");
        jLabel12sinad.setText("12 dB -> 14 dB");
        jLabel15sinad.setText("15 dB -> 19 dB");
        jLabel20sinad.setText("20 dB -> 24 dB");
        jLabel25sinad.setText("25 dB -> 29 dB");
        jLabel30sinad.setText("30 dB -> 34 dB");

        jLabel0ber.setText("0 % -> 4 %");
        jLabel5ber.setText("5 % -> 9 %");
        jLabel10ber.setText("10 % -> 14 %");
        jLabel15ber.setText("15 % -> 19 %");
        jLabel20ber.setText("20 % -> 24 %");
        jLabel25ber.setText("25 % -> 29 %");
        jLabel30ber.setText("30 % -> 34 %");
        jLabel35ber.setText("35 % -> 39 %");

        signalQualityDisplaySelectorRadioButtonGroup.add(displaySinadRadioButton);
        signalQualityDisplaySelectorRadioButtonGroup.add(displayRssiRadioButton);
        signalQualityDisplaySelectorRadioButtonGroup.add(displayBerRadioButton);

        timingModeButtonGroup.add(fixedTimingRateRadioButton);
        timingModeButtonGroup.add(positionBasedTimingRadioButton);
        timingModeButtonGroup.add(wavelengthsPerSubSampleRadioButton);
        timingModeButtonGroup.add(speedBasedTimingRadioButton);

        manualDataCollectionModeRadioButtonGroup.add(manualDataCollectionDisabledRadioButton);
        manualDataCollectionModeRadioButtonGroup.add(manualDataCollectionContinuousModeRadioButton);
        manualDataCollectionModeRadioButtonGroup.add(manualDataCollectionMousePressModeRadioButton);

        testCriteriaRadioButtonGroup.add(greaterThanRadioButton);
        testCriteriaRadioButtonGroup.add(windowRadioButton);

        accessabilityRadioButtonGroup.add(eliminateRadioButton);
        accessabilityRadioButtonGroup.add(estimateRadioButton);
        accessabilityRadioButtonGroup.add(passRadioButton);

        signalSampleRadioButtonGroup.add(sampleRssiRadioButton);
        signalSampleRadioButtonGroup.add(sampleSinadRadioButton);
        signalSampleRadioButtonGroup.add(sampleBerRadioButton);

        calcBasisRadioButtonGroup.add(calcBasedOnCARReqRadioButton);
        calcBasisRadioButtonGroup.add(calcBasedOnSARReqRadioButton);
        calcBasisRadioButtonGroup.add(calcBasedOnNumberAllowedTilesRadioButton);
    }

    private void applyDoubleFilter(JFormattedTextField jftf) {
        final PlainDocument pDoc = (PlainDocument) jftf.getDocument();
        pDoc.setDocumentFilter(new DoubleFilter());
    }

    private void applyIntegerFilter(JFormattedTextField jftf) {
        final PlainDocument pDoc = (PlainDocument) jftf.getDocument();
        pDoc.setDocumentFilter(new IntegerFilter());
    }

    private void configureListeners() {
        final RadioButtonHandler radioButtonHandler = new RadioButtonHandler();
        
        calcBasedOnCARReqRadioButton.addItemListener(radioButtonHandler);
        calcBasedOnSARReqRadioButton.addItemListener(radioButtonHandler);
        calcBasedOnNumberAllowedTilesRadioButton.addItemListener(radioButtonHandler);
        sampleRssiRadioButton.addItemListener(radioButtonHandler);
        sampleSinadRadioButton.addItemListener(radioButtonHandler);
        sampleBerRadioButton.addItemListener(radioButtonHandler);
        passRadioButton.addItemListener(radioButtonHandler);
        estimateRadioButton.addItemListener(radioButtonHandler);
        eliminateRadioButton.addItemListener(radioButtonHandler);
        manualDataCollectionDisabledRadioButton.addItemListener(radioButtonHandler);
        manualDataCollectionContinuousModeRadioButton.addItemListener(radioButtonHandler);
        manualDataCollectionMousePressModeRadioButton.addItemListener(radioButtonHandler);
        displaySinadRadioButton.addItemListener(radioButtonHandler);
        displayRssiRadioButton.addItemListener(radioButtonHandler);
        displayBerRadioButton.addItemListener(radioButtonHandler);
        fixedTimingRateRadioButton.addItemListener(radioButtonHandler);
        positionBasedTimingRadioButton.addItemListener(radioButtonHandler);
        speedBasedTimingRadioButton.addItemListener(radioButtonHandler);
        wavelengthsPerSubSampleRadioButton.addItemListener(radioButtonHandler);
        greaterThanRadioButton.addItemListener(radioButtonHandler);
        windowRadioButton.addItemListener(radioButtonHandler);

        tileSelectionModeButton.addActionListener(_ ->
            cto.getPropertyChangeSupport().firePropertyChange(CoverageTestObject.TEST_GRID_LEARN_MODE, null, true));

        drawTestGridOnMapButton.addActionListener(_ ->
            cto.getPropertyChangeSupport().firePropertyChange(CoverageTestObject.TEST_GRID_SELECTION_MODE, null, true));

        wavelengthsPerSubSampleTextField.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent kpe) {
                if (kpe.getKeyCode() == 10) {
                    testParameterPanel.requestFocusInWindow();
                }
            }

            @Override
            public void keyReleased(KeyEvent kre) {
            	// NOOP
            }

            @Override
            public void keyTyped(KeyEvent kte) {
            	// NOOP
            }
        });

        wavelengthsPerSubSampleTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent fge) {
                wavelengthsPerSubSampleTextField.setFont(new Font(wavelengthsPerSubSampleTextField.getFont().getName(), Font.BOLD, wavelengthsPerSubSampleTextField.getFont().getSize()));
            }

            @Override
            public void focusLost(FocusEvent event) {
                wavelengthsPerSubSampleTextField.setFont(new Font(wavelengthsPerSubSampleTextField.getFont().getName(), Font.PLAIN, wavelengthsPerSubSampleTextField.getFont().getSize()));
                final JFormattedTextField tf = (JFormattedTextField) event.getSource();
                cto.setWavelengthsPerSubSample(Integer.parseInt(tf.getText()));
            }
        });

        measReqPerSubSampleTextField.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent kpe) {
                if (kpe.getKeyCode() == 10) {
                    testParameterPanel.requestFocusInWindow();
                }
            }

            @Override
            public void keyReleased(KeyEvent kre) {
            	// NOOP
            }

            @Override
            public void keyTyped(KeyEvent kte) {
            	// NOOP
            }
        });

        measReqPerSubSampleTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent fge) {
                measReqPerSubSampleTextField.setFont(new Font(measReqPerSubSampleTextField.getFont().getName(), Font.BOLD, measReqPerSubSampleTextField.getFont().getSize()));
            }

            @Override
            public void focusLost(FocusEvent event) {
                measReqPerSubSampleTextField.setFont(new Font(measReqPerSubSampleTextField.getFont().getName(), Font.PLAIN, measReqPerSubSampleTextField.getFont().getSize()));
                final JFormattedTextField tf = (JFormattedTextField) event.getSource();
                cto.setMinMeasurementsRequiredPerSubSample(Integer.parseInt(tf.getText()));
            }
        });

        minReqSampleRateTextField.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent kpe) {
                if (kpe.getKeyCode() == 10) {
                    testParameterPanel.requestFocusInWindow();
                }
            }

            @Override
            public void keyReleased(KeyEvent kre) {
            	// NOOP
            }

            @Override
            public void keyTyped(KeyEvent kte) {
            	// NOOP
            }
        });

        minReqSampleRateTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent fge) {
                minReqSampleRateTextField.setFont(new Font(minReqSampleRateTextField.getFont().getName(), Font.BOLD, minReqSampleRateTextField.getFont().getSize()));
            }

            @Override
            public void focusLost(FocusEvent event) {
                minReqSampleRateTextField.setFont(new Font(minReqSampleRateTextField.getFont().getName(), Font.PLAIN, minReqSampleRateTextField.getFont().getSize()));
                final JFormattedTextField tf = (JFormattedTextField) event.getSource();
                cto.setMinAllowedSampleRateSamplesPerSecond(Integer.parseInt(tf.getText()));
            }
        });

        searchRadiusTextField.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent kpe) {
                if (kpe.getKeyCode() == 10) {
                    testParameterPanel.requestFocusInWindow();
                }
            }

            @Override
            public void keyReleased(KeyEvent kre) {
            	// NOOP
            }

            @Override
            public void keyTyped(KeyEvent kte) {
            	// NOOP
            }
        });

        searchRadiusTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent fge) {
                searchRadiusTextField.setFont(new Font(searchRadiusTextField.getFont().getName(), Font.BOLD, searchRadiusTextField.getFont().getSize()));
            }

            @Override
            public void focusLost(FocusEvent event) {
                searchRadiusTextField.setFont(new Font(searchRadiusTextField.getFont().getName(), Font.PLAIN, searchRadiusTextField.getFont().getSize()));
                final JFormattedTextField tf = (JFormattedTextField) event.getSource();
                cto.setSearchRadius(Double.parseDouble(tf.getText()));
            }
        });

        requiredGridPointsTextField.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent kpe) {
                if (kpe.getKeyCode() == 10) {
                    testParameterPanel.requestFocusInWindow();
                }
            }

            @Override
            public void keyReleased(KeyEvent kre) {
            	// NOOP
            }

            @Override
            public void keyTyped(KeyEvent kte) {
            	// NOOP
            }
        });

        requiredGridPointsTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent fge) {
                requiredGridPointsTextField.setFont(new Font(requiredGridPointsTextField.getFont().getName(), Font.BOLD, requiredGridPointsTextField.getFont().getSize()));
            }

            @Override
            public void focusLost(FocusEvent event) {
                requiredGridPointsTextField.setFont(new Font(requiredGridPointsTextField.getFont().getName(), Font.PLAIN, requiredGridPointsTextField.getFont().getSize()));
                final JFormattedTextField tf = (JFormattedTextField) event.getSource();
                cto.setRequiredGridPoints(Integer.parseInt(tf.getText()));
            }
        });

        requiredGridPointsTextField.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent kpe) {
                if (kpe.getKeyCode() == 10) {
                    testParameterPanel.requestFocusInWindow();
                }
            }

            @Override
            public void keyReleased(KeyEvent kre) {
            	// NOOP
            }

            @Override
            public void keyTyped(KeyEvent kte) {
            	// NOOP
            }
        });

        requiredGridPointsTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent fge) {
                requiredGridPointsTextField.setFont(new Font(requiredGridPointsTextField.getFont().getName(), Font.BOLD, requiredGridPointsTextField.getFont().getSize()));
            }

            @Override
            public void focusLost(FocusEvent event) {
                requiredGridPointsTextField.setFont(new Font(requiredGridPointsTextField.getFont().getName(), Font.PLAIN, requiredGridPointsTextField.getFont().getSize()));
                final JFormattedTextField tf = (JFormattedTextField) event.getSource();
                cto.setRequiredGridPoints(Integer.parseInt(tf.getText()));
            }
        });

        maxNumberOfAllowedTilesTextField.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent kpe) {
                if (kpe.getKeyCode() == 10) {
                    tileConstraintsPanel.requestFocusInWindow();
                }
            }

            @Override
            public void keyReleased(KeyEvent kre) {
            	// NOOP
            }

            @Override
            public void keyTyped(KeyEvent kte) {
            	// NOOP
            }
        });

        maxNumberOfAllowedTilesTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent fge) {
                maxNumberOfAllowedTilesTextField.setFont(new Font(maxNumberOfAllowedTilesTextField.getFont().getName(), Font.BOLD, maxNumberOfAllowedTilesTextField.getFont().getSize()));
            }

            @Override
            public void focusLost(FocusEvent event) {
                maxNumberOfAllowedTilesTextField.setFont(new Font(maxNumberOfAllowedTilesTextField.getFont().getName(), Font.PLAIN, maxNumberOfAllowedTilesTextField.getFont().getSize()));
                final JFormattedTextField tf = (JFormattedTextField) event.getSource();
                cto.setMaxNumberOfAllowedTiles(Integer.parseInt(tf.getText()));
                processAutoCalculations();
            }
        });

        minSpeedForValidSubSampleTextField.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent kpe) {
                if (kpe.getKeyCode() == 10) {
                    tileConstraintsPanel.requestFocusInWindow();
                }
            }

            @Override
            public void keyReleased(KeyEvent kre) {
            	// NOOP
            }

            @Override
            public void keyTyped(KeyEvent kte) {
            	// NOOP
            }
        });

        minSpeedForValidSubSampleTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent fge) {
                minSpeedForValidSubSampleTextField.setFont(new Font(minSpeedForValidSubSampleTextField.getFont().getName(), Font.BOLD, minSpeedForValidSubSampleTextField.getFont().getSize()));
            }

            @Override
            public void focusLost(FocusEvent event) {
                minSpeedForValidSubSampleTextField.setFont(new Font(minSpeedForValidSubSampleTextField.getFont().getName(), Font.PLAIN, minSpeedForValidSubSampleTextField.getFont().getSize()));
                final JFormattedTextField tf = (JFormattedTextField) event.getSource();
                cto.setMinSpeedFPMForValidSample(Integer.parseInt(tf.getText()));
            }
        });

        maxSpeedForValidSubSampleTextField.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent kpe) {
                if (kpe.getKeyCode() == 10) {
                    tileConstraintsPanel.requestFocusInWindow();
                }
            }

            @Override
            public void keyReleased(KeyEvent kre) {
            	// NOOP
            }

            @Override
            public void keyTyped(KeyEvent kte) {
            	// NOOP
            }
        });

        maxSpeedForValidSubSampleTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent fge) {
                maxSpeedForValidSubSampleTextField.setFont(new Font(maxSpeedForValidSubSampleTextField.getFont().getName(), Font.BOLD, maxSpeedForValidSubSampleTextField.getFont().getSize()));
            }

            @Override
            public void focusLost(FocusEvent event) {
                maxSpeedForValidSubSampleTextField.setFont(new Font(maxSpeedForValidSubSampleTextField.getFont().getName(), Font.PLAIN, maxSpeedForValidSubSampleTextField.getFont().getSize()));
                final JFormattedTextField tf = (JFormattedTextField) event.getSource();
                cto.setMaxSpeedFPMForValidSample(Integer.parseInt(tf.getText()));
            }
        });

        gridWidthMilesTextField.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent kpe) {
                if (kpe.getKeyCode() == 10) {
                    mapImageModelPanel.requestFocusInWindow();
                }
            }

            @Override
            public void keyReleased(KeyEvent kre) {
            	// NOOP
            }

            @Override
            public void keyTyped(KeyEvent kte) {
            	// NOOP
            }
        });

        gridWidthMilesTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent fge) {
                gridWidthMilesTextField.setFont(new Font(gridWidthMilesTextField.getFont().getName(), Font.BOLD, gridWidthMilesTextField.getFont().getSize()));
                gridRightTextField.setEnabled(false);
            }

            @Override
            public void focusLost(FocusEvent fle) {
                gridRightTextField.setEnabled(true);
                setGridWidth();
            }
        });

        gridHeightMilesTextField.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent kpe) {
                if (kpe.getKeyCode() == 10) {
                    mapImageModelPanel.requestFocusInWindow();
                }
            }

            @Override
            public void keyReleased(KeyEvent kre) {
            	// NOOP
            }

            @Override
            public void keyTyped(KeyEvent kte) {
            	// NOOP
            }
        });

        gridHeightMilesTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent fge) {
                gridHeightMilesTextField.setFont(new Font(gridHeightMilesTextField.getFont().getName(), Font.BOLD, gridHeightMilesTextField.getFont().getSize()));
                gridBottomTextField.setEnabled(false);
            }

            @Override
            public void focusLost(FocusEvent fle) {
                gridBottomTextField.setEnabled(true);
                setGridHeight();
            }
        });

        gridTopTextField.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent kpe) {
                if (kpe.getKeyCode() == 10) {
                    mapImageModelPanel.requestFocusInWindow();
                }
            }

            @Override
            public void keyReleased(KeyEvent kre) {
            	// NOOP
            }

            @Override
            public void keyTyped(KeyEvent kte) {
            	// NOOP
            }
        });

        gridTopTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent fge) {
                gridTopTextField.setFont(new Font(gridTopTextField.getFont().getName(), Font.BOLD, gridTopTextField.getFont().getSize()));
            }

            @Override
            public void focusLost(FocusEvent fle) {
                setGridTop();
            }
        });

        gridBottomTextField.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent kpe) {
                if (kpe.getKeyCode() == 10) {
                    mapImageModelPanel.requestFocusInWindow();
                }
            }

            @Override
            public void keyReleased(KeyEvent kre) {
            	// NOOP
            }

            @Override
            public void keyTyped(KeyEvent kte) {
            	// NOOP
            }
        });

        gridBottomTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent fge) {
                gridBottomTextField.setFont(new Font(gridBottomTextField.getFont().getName(), Font.BOLD, gridBottomTextField.getFont().getSize()));
                gridHeightMilesTextField.setEnabled(false);
            }

            @Override
            public void focusLost(FocusEvent fle) {
                gridHeightMilesTextField.setEnabled(true);
                setGridBottom();
            }
        });

        gridLeftTextField.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent kpe) {
                if (kpe.getKeyCode() == 10) {
                    mapImageModelPanel.requestFocusInWindow();
                }
            }

            @Override
            public void keyReleased(KeyEvent kre) {
            	// NOOP
            }

            @Override
            public void keyTyped(KeyEvent kte) {
            	// NOOP
            }
        });

        gridLeftTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent fge) {
                gridLeftTextField.setFont(new Font(gridLeftTextField.getFont().getName(), Font.BOLD, gridLeftTextField.getFont().getSize()));
            }

            @Override
            public void focusLost(FocusEvent fle) {
                setGridLeft();
            }
        });

        gridRightTextField.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent kpe) {
                if (kpe.getKeyCode() == 10) {
                    mapImageModelPanel.requestFocusInWindow();
                }
            }

            @Override
            public void keyReleased(KeyEvent kre) {
            	// NOOP
            }

            @Override
            public void keyTyped(KeyEvent kte) {
            	// NOOP
            }
        });

        gridRightTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent fge) {
                gridRightTextField.setFont(new Font(gridRightTextField.getFont().getName(), Font.BOLD, gridRightTextField.getFont().getSize()));
                gridWidthMilesTextField.setEnabled(false);
            }

            @Override
            public void focusLost(FocusEvent fle) {
                gridWidthMilesTextField.setEnabled(true);
                setGridRight();
            }
        });

        okButton.addActionListener(_ -> {
            applyButton.doClick();
            dispose();
        });

        cancelButton.addActionListener(_ -> dispose());

        applyButton.addActionListener(_ -> applyButtonEvent());

        alertOnMinimumSamplesPerTileAcquiredCheckBox.addItemListener(event -> {
            final JCheckBox cb = (JCheckBox) event.getSource();
            cto.setAlertOnMinimumSamplesPerTileAcquired(cb.isSelected());
        });

        enableAutoCalcCheckBox.addItemListener(event -> {
            final JCheckBox cb = (JCheckBox) event.getSource();
            cto.setEnableAutoCalc(cb.isSelected());
            calcBasedOnCARReqRadioButton.setEnabled(cb.isSelected());
            calcBasedOnSARReqRadioButton.setEnabled(cb.isSelected());
            calcBasedOnNumberAllowedTilesRadioButton.setEnabled(cb.isSelected());
            calculatedNumberOfRequiredTilesTextField.setEnabled(cb.isSelected());
        });

        testRxSpecsValidForTestCheckBox.addItemListener(event -> {
            final JCheckBox cb = (JCheckBox) event.getSource();
            cto.setTestRxSpecsValidForTest(cb.isSelected());
            configureRxSpecs();
        });

        showGridSquareShadingCheckBox.addItemListener(event -> {
            final JCheckBox cb = (JCheckBox) event.getSource();
            cto.showGridSquareShading(cb.isSelected());
        });

        showTestGridCheckBox.addItemListener(event -> {
            final JCheckBox cb = (JCheckBox) event.getSource();
            cto.showGrid(cb.isSelected());
        });

        showLinesCheckBox.addItemListener(event -> {
            final JCheckBox cb = (JCheckBox) event.getSource();
            cto.showLines(cb.isSelected());
        });

        showRingsCheckBox.addItemListener(event -> {
            final JCheckBox cb = (JCheckBox) event.getSource();
            cto.showRings(cb.isSelected());
        });

        showQuadsCheckBox.addItemListener(event -> {
            final JCheckBox cb = (JCheckBox) event.getSource();
            cto.showQuads(cb.isSelected());
        });

        requiredSizeOfTileComboBox.addItemListener(event -> {
            final JComboBox<?> cb = (JComboBox<?>) event.getSource();
            cto.setTileSizeArcSeconds(Double.parseDouble(removeAllNonNumericCharacters((String) cb.getSelectedItem())));
        });

        maxVerticalSizeOfTileComboBox.addItemListener(event -> {
            final JComboBox<?> cb = (JComboBox<?>) event.getSource();
            cto.setMaxTileSizeArcSeconds(Double.parseDouble(removeAllNonNumericCharacters((String) cb.getSelectedItem())));
        });

        minVerticalSizeOfTileComboBox.addItemListener(event -> {
            final JComboBox<?> cb = (JComboBox<?>) event.getSource();
            cto.setMinTileSizeArcSeconds(Double.parseDouble(removeAllNonNumericCharacters((String) cb.getSelectedItem())));
        });

        noiseFloorComboBox.addItemListener(event -> {
            final JComboBox<?> cb = (JComboBox<?>) event.getSource();
            cto.setNoiseFloorIndex(cb.getSelectedIndex());
        });

        dynamicRangeComboBox.addItemListener(event -> {
            final JComboBox<?> cb = (JComboBox<?>) event.getSource();
            cto.setDynamicRangeIndex(cb.getSelectedIndex());
        });

        adjacentChannelRejectionComboBox.addItemListener(event -> {
            final JComboBox<?> cb = (JComboBox<?>) event.getSource();
            cto.setAdjacentChannelRejectionIndex(cb.getSelectedIndex());
        });

        signalReqFor12dBSinadComboBox.addItemListener(event -> {
            final JComboBox<?> cb = (JComboBox<?>) event.getSource();
            cto.setSignalReqFor12dBSinadIndex(cb.getSelectedIndex());
        });

        signalReqFor20dBQuietingComboBox.addItemListener(event -> {
            final JComboBox<?> cb = (JComboBox<?>) event.getSource();
            cto.setSignalReqFor20dBQuietingIndex(cb.getSelectedIndex());
        });

        signalReqFor5PctBERComboBox.addItemListener(event -> {
            final JComboBox<?> cb = (JComboBox<?>) event.getSource();
            cto.setSignalReqFor5PctBERIndex(cb.getSelectedIndex());
        });

        requestedSignalQualityComboBox.addItemListener(event -> {
            final JComboBox<?> cb = (JComboBox<?>) event.getSource();
            setRequestedSignalQualityComboBox(cb.getSelectedIndex());
        });

        confidenceIntervalComboBox.addItemListener(event -> {
            final JComboBox<?> cb = (JComboBox<?>) event.getSource();
            cto.setConfidenceIntervalIndex(cb.getSelectedIndex());
            processAutoCalculations();
        });

        confidenceLevelComboBox.addItemListener(event -> {
            final JComboBox<?> cb = (JComboBox<?>) event.getSource();
            cto.setConfidenceLevelIndex(cb.getSelectedIndex());
            processAutoCalculations();
        });

        contractualAreaReliabilityComboBox.addItemListener(event -> {
            final JComboBox<?> cb = (JComboBox<?>) event.getSource();
            cto.setContractualAreaReliabilityIndex(cb.getSelectedIndex());
            processAutoCalculations();
        });

        predictedAreaReliabilityComboBox.addItemListener((ItemEvent event) -> {
            final JComboBox<?> cb = (JComboBox<?>) event.getSource();
            cto.setPredictedAreaReliabilityIndex(cb.getSelectedIndex());
            processAutoCalculations();
        });

        channelToDisplayComboBox.addItemListener(event -> {
            final JComboBox<?> cb = (JComboBox<?>) event.getSource();
            cto.setChannelToDisplayIndex(cb.getSelectedIndex());
        });

        jTextFieldTileSelectedColor.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                jTextFieldTileSelectedColorMouseClicked();
            }
        });

        jTextFieldTileInProgressColor.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                jTextFieldTileInProgressColorMouseClicked();
            }
        });

        jTextFieldTileCompleteColor.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                jTextFieldTileCompleteColorMouseClicked();
            }
        });

        jTextFieldTileInaccessableColor.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                jTextFieldTileInaccessableColorMouseClicked();
            }
        });

        jTextFieldGridColor.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                jTextFieldGridColorMouseClicked();
            }
        });

        jTextField50dBm.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                jTextField50dBmMouseClicked();
            }
        });

        jTextField60dBm.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                jTextField60dBmMouseClicked();
            }
        });

        jTextField70dBm.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                jTextField70dBmMouseClicked();
            }
        });

        jTextField80dBm.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                jTextField80dBmMouseClicked();
            }
        });

        jTextField90dBm.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                jTextField90dBmMouseClicked();
            }
        });

        jTextField100dBm.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                jTextField100dBmMouseClicked();
            }
        });

        jTextField110dBm.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                jTextField110dBmMouseClicked();
            }
        });

        jTextField120dBm.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                jTextField120dBmMouseClicked();
            }
        });

        jTextField0sinad.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                jTextField0sinadMouseClicked();
            }
        });

        jTextField5sinad.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                jTextField5sinadMouseClicked();
            }
        });

        jTextField10sinad.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                jTextField10sinadMouseClicked();
            }
        });

        jTextField12sinad.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                jTextField12sinadMouseClicked();
            }
        });

        jTextField15sinad.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getButton() == MouseEvent.BUTTON1) {
                    jTextField15sinadMouseClicked();
                }
            }
        });

        jTextField20sinad.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getButton() == MouseEvent.BUTTON1) {
                    jTextField20sinadMouseClicked();
                }
            }
        });

        jTextField25sinad.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                jTextField25sinadMouseClicked();
            }
        });

        jTextField30sinad.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                jTextField30sinadMouseClicked();
            }
        });

        jTextField0ber.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                jTextField0berMouseClicked();
            }
        });

        jTextField5ber.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                jTextField5berMouseClicked();
            }
        });

        jTextField10ber.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                jTextField10berMouseClicked();
            }
        });

        jTextField35ber.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                jTextField35berMouseClicked();
            }
        });

        jTextField15ber.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                jTextField15berMouseClicked();
            }
        });

        jTextField20ber.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                jTextField20berMouseClicked();
            }
        });

        jTextField25ber.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                jTextField25berMouseClicked();
            }
        });

        jTextField30ber.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                jTextField30berMouseClicked();
            }
        });

        timingRateTextField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                timingRateTextFieldMouseClicked(event);
            }
        });

        searchRadiusTextField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                searchRadiusTextFieldMouseClicked(event);
            }
        });

        minReqSampleRateTextField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                minReqSampleRateTextFieldMouseClicked(event);
            }
        });

        measReqPerSubSampleTextField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                measReqPerSubSampleTextFieldMouseClicked(event);
            }
        });

        maxSpeedForValidSubSampleTextField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                maxSpeedForValidSubSampleTextFieldMouseClicked(event);
            }
        });

        minSpeedForValidSubSampleTextField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                minSpeedForValidSubSampleTextFieldMouseClicked(event);
            }
        });

        requiredGridPointsTextField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                requiredGridPointsTextFieldMouseClicked(event);
            }
        });

        wavelengthsPerSubSampleTextField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                wavelengthsPerSubSampleTextFieldMouseClicked(event);
            }
        });

        minSamplesPerTileTextField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                minSamplesPerTileTextFieldMouseClicked(event);
            }
        });

        maxSamplesPerTileTextField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                maxSamplesPerTileTextFieldMouseClicked(event);
            }
        });

        maxNumberOfAllowedTilesTextField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                maxNumberOfAllowedTilesTextFieldMouseClicked(event);
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent event) {
                if (WindowEvent.WINDOW_CLOSING == event.getID()) {
                    cto.saveSettingsToRegistry();
                }
            }
        });
    }

    private static double mRound(double value, double factor) {
        return Math.round(value / factor) * factor;
    }

    private static double roundToRightEdge(double right, double left, double tileWidth) {
        final double xTileSize = tileWidth;
        final long xTiles = Math.round(Math.abs(right - left) / xTileSize);
        return left + (xTiles * xTileSize);
    }

    private static double roundToBottomEdge(double bottom, double top, double tileHeight) {
        final double y = tileHeight;
        final long xTiles = Math.round(Math.abs(bottom - top) / y);
        return top + (xTiles * y);
    }

    private void setGridWidth() {
        gridWidthMilesTextField.setFont(new Font(gridWidthMilesTextField.getFont().getName(), Font.PLAIN, gridWidthMilesTextField.getFont().getSize()));
        final double degrees = Vincenty.milesToDegrees(Double.parseDouble(gridWidthMilesTextField.getText()), 90F, cto.getGridTopDegrees());
        cto.setGridWidthDegrees(mRound(degrees, cto.getTileSizeDegrees().getX()));
        gridWidthMilesTextField.setText(widthFormat.format(Vincenty.degreesToMiles(cto.getGridWidthDegrees(), 90F, cto.getGridTopDegrees())));
        if (!isEqual(cto.getGridLeftDegrees(), -999)) {
            cto.setGridRightDegrees(cto.getGridLeftDegrees() + cto.getGridWidthDegrees());
            gridRightTextField.setText(lonFormat.format(cto.getGridRightDegrees()));
        }
    }

    private void setGridHeight() {
        gridHeightMilesTextField.setFont(new Font(gridHeightMilesTextField.getFont().getName(), Font.PLAIN, gridHeightMilesTextField.getFont().getSize()));
        final double degrees = Vincenty.milesToDegrees(Double.parseDouble(gridHeightMilesTextField.getText()), 0F, cto.getGridTopDegrees());
        cto.setGridHeightDegrees(mRound(degrees, cto.getTileSizeDegrees().getY()));
        gridHeightMilesTextField.setText(heightFormat.format(Vincenty.degreesToMiles(cto.getGridHeightDegrees(), 0F, cto.getGridTopDegrees())));
        if (!isEqual(cto.getGridTopDegrees(), -999)) {
            cto.setGridBottomDegrees(cto.getGridTopDegrees() - cto.getGridHeightDegrees());
            gridBottomTextField.setText(latFormat.format(cto.getGridBottomDegrees()));
            gridBottomTextField.setEnabled(true);
        }
    }

    private void setGridLeft() {
        gridLeftTextField.setFont(new Font(gridLeftTextField.getFont().getName(), Font.PLAIN, gridLeftTextField.getFont().getSize()));
        cto.setGridLeftDegrees(Double.parseDouble(gridLeftTextField.getText()));
        gridLeftTextField.setText(lonFormat.format(cto.getGridLeftDegrees()));
        if ((cto.getGridLeftDegrees() >= -180.0) && (cto.getGridLeftDegrees() <= 180.0)) {
            gridRightTextField.setEnabled(true);
            gridWidthMilesTextField.setEnabled(true);
        } else {
            gridRightTextField.setEnabled(false);
            gridWidthMilesTextField.setEnabled(false);
        }
    }

    private void setGridTop() {
        gridTopTextField.setFont(new Font(gridTopTextField.getFont().getName(), Font.PLAIN, gridTopTextField.getFont().getSize()));
        cto.setGridTopDegrees(Double.parseDouble(gridTopTextField.getText()));
        gridTopTextField.setText(latFormat.format(cto.getGridTopDegrees()));
        if ((cto.getGridTopDegrees() >= -90.0) && (cto.getGridLeftDegrees() <= 90.0)) {
            gridBottomTextField.setEnabled(true);
            gridHeightMilesTextField.setEnabled(true);
        } else {
            gridBottomTextField.setEnabled(false);
            gridHeightMilesTextField.setEnabled(false);
        }
    }

    private void setGridBottom() {
        gridBottomTextField.setFont(new Font(gridBottomTextField.getFont().getName(), Font.PLAIN, gridBottomTextField.getFont().getSize()));
        cto.setGridBottomDegrees(roundToBottomEdge(Double.parseDouble(gridBottomTextField.getText()), cto.getGridTopDegrees(), cto.getTileSizeDegrees().getY()));
        gridBottomTextField.setText(latFormat.format(cto.getGridBottomDegrees()));
        cto.setGridHeightDegrees(Math.abs(cto.getGridTopDegrees() - cto.getGridBottomDegrees()));
        gridHeightMilesTextField.setText(heightFormat.format(Vincenty.degreesToMiles(cto.getGridHeightDegrees(), 0F, cto.getGridTopDegrees())));
    }

    private void setGridRight() {
        gridRightTextField.setFont(new Font(gridRightTextField.getFont().getName(), Font.PLAIN, gridRightTextField.getFont().getSize()));
        cto.setGridRightDegrees(roundToRightEdge(Double.parseDouble(gridRightTextField.getText()), cto.getGridLeftDegrees(), cto.getTileSizeDegrees().getX()));
        gridRightTextField.setText(lonFormat.format(cto.getGridRightDegrees()));
        cto.setGridWidthDegrees(Math.abs(cto.getGridLeftDegrees() - cto.getGridRightDegrees()));
        gridWidthMilesTextField.setText(widthFormat.format(Vincenty.degreesToMiles(cto.getGridWidthDegrees(), 90F, cto.getGridTopDegrees())));
    }

    public void setTestGridBoundaries(RectangularShape testGridBoundaries) {
        cto.setGridTopDegrees(testGridBoundaries.getY());
        gridTopTextField.setText(latFormat.format(cto.getGridTopDegrees()));
        cto.setGridLeftDegrees(testGridBoundaries.getX());
        gridLeftTextField.setText(lonFormat.format(cto.getGridLeftDegrees()));
        cto.setGridHeightDegrees(testGridBoundaries.getHeight());
        gridHeightMilesTextField.setText(heightFormat.format(Vincenty.degreesToMiles(cto.getGridHeightDegrees(), 0.0, cto.getGridTopDegrees())));
        cto.setGridWidthDegrees(testGridBoundaries.getWidth());
        gridWidthMilesTextField.setText(widthFormat.format(Vincenty.degreesToMiles(cto.getGridWidthDegrees(), 90.0, cto.getGridTopDegrees())));
        cto.setGridRightDegrees(cto.getGridLeftDegrees() + cto.getGridWidthDegrees());
        gridRightTextField.setText(lonFormat.format(cto.getGridRightDegrees()));
        cto.setGridBottomDegrees(cto.getGridTopDegrees() - cto.getGridHeightDegrees());
        gridBottomTextField.setText(latFormat.format(cto.getGridBottomDegrees()));
        processAutoCalculations();
        cto.saveSettingsToRegistry();
    }

    public void applyButtonEvent() {
        processAutoCalculations();
        cto.saveSettingsToRegistry();
        cto.getPropertyChangeSupport().firePropertyChange(CoverageTestObject.PROPERTY_CHANGE, null, null);
    }

    private void setRequestedSignalQualityComboBox(int index) {
        switch (cto.getSignalSampleMode()) {
            case SINAD ->
                cto.setRequestedSinadQualityIndex(index);
            case DBM ->
                cto.setRequestedRssiQualityIndex(index);
            case BER ->
                cto.setRequestedBerQualityIndex(index);
        }
    }

    private void timingRateTextFieldMouseClicked(MouseEvent event) {
    	final JFormattedTextField tf = (JFormattedTextField) event.getSource();
        cto.setFixedTimePeriod(Integer.parseInt(tf.getText()));
    }

    private void searchRadiusTextFieldMouseClicked(MouseEvent event) {
    	final JFormattedTextField tf = (JFormattedTextField) event.getSource();
        cto.setSearchRadius(Double.parseDouble(tf.getText()));
    }

    private void minReqSampleRateTextFieldMouseClicked(MouseEvent event) {
    	final JFormattedTextField tf = (JFormattedTextField) event.getSource();
        cto.setMinAllowedSampleRateSamplesPerSecond(Integer.parseInt(tf.getText()));
    }

    private void measReqPerSubSampleTextFieldMouseClicked(MouseEvent event) {
    	final JFormattedTextField tf = (JFormattedTextField) event.getSource();
        cto.setMinMeasurementsRequiredPerSubSample(Integer.parseInt(tf.getText()));
    }

    private void maxSpeedForValidSubSampleTextFieldMouseClicked(MouseEvent event) {
    	final JFormattedTextField tf = (JFormattedTextField) event.getSource();
        cto.setMaxSpeedFPMForValidSample(Integer.parseInt(tf.getText()));
    }

    private void minSpeedForValidSubSampleTextFieldMouseClicked(MouseEvent event) {
    	final JFormattedTextField tf = (JFormattedTextField) event.getSource();
        cto.setMinSpeedFPMForValidSample(Integer.parseInt(tf.getText()));
    }

    private void requiredGridPointsTextFieldMouseClicked(MouseEvent event) {
    	final JFormattedTextField tf = (JFormattedTextField) event.getSource();
        cto.setRequiredGridPoints(Integer.parseInt(tf.getText()));
    }

    private void wavelengthsPerSubSampleTextFieldMouseClicked(MouseEvent event) {
    	final JFormattedTextField tf = (JFormattedTextField) event.getSource();
        cto.setWavelengthsPerSubSample(Integer.parseInt(tf.getText()));
    }

    private void minSamplesPerTileTextFieldMouseClicked(MouseEvent event) {
    	final JFormattedTextField tf = (JFormattedTextField) event.getSource();
        cto.setMinSamplesPerTile(Integer.parseInt(tf.getText()));
    }

    private void maxSamplesPerTileTextFieldMouseClicked(MouseEvent event) {
    	final JFormattedTextField tf = (JFormattedTextField) event.getSource();
        cto.setMaxSamplesPerTile(Integer.parseInt(tf.getText()));
    }

    private void maxNumberOfAllowedTilesTextFieldMouseClicked(MouseEvent event) {
    	final JFormattedTextField tf = (JFormattedTextField) event.getSource();
        cto.setMaxNumberOfAllowedTiles(Integer.parseInt(tf.getText()));
    }

    private void jTextFieldTileSelectedColorMouseClicked() {
    	final Color color = JColorChooser.showDialog(this, "Select a color", jTextFieldTileSelectedColor.getBackground());
        jTextFieldTileSelectedColor.setBackground(color);
        cto.setTileSelectedColor(color);
    }

    private void jTextFieldTileInProgressColorMouseClicked() {
    	final Color color = JColorChooser.showDialog(this, "Select a color", jTextFieldTileInProgressColor.getBackground());
        jTextFieldTileInProgressColor.setBackground(color);
        cto.setTileInProgressColor(color);
    }

    private void jTextFieldTileCompleteColorMouseClicked() {
    	final Color color = JColorChooser.showDialog(this, "Select a color", jTextFieldTileCompleteColor.getBackground());
        jTextFieldTileCompleteColor.setBackground(color);
        cto.setTileCompleteColor(color);
    }

    private void jTextFieldTileInaccessableColorMouseClicked() {
    	final Color color = JColorChooser.showDialog(this, "Select a color", jTextFieldTileInaccessableColor.getBackground());
        jTextFieldTileCompleteColor.setBackground(color);
        cto.setTileInaccessableColor(color);
    }

    private void jTextFieldGridColorMouseClicked() {
    	final Color color = JColorChooser.showDialog(this, "Select a color", jTextFieldGridColor.getBackground());
        jTextFieldGridColor.setBackground(color);
        cto.setGridColor(color);
    }

    private void jTextField120dBmMouseClicked() {
    	final Color color = JColorChooser.showDialog(this, "Select a color", jTextField120dBm.getBackground());
        jTextField120dBm.setBackground(color);
        cto.setColor120dBm(color);
    }

    private void jTextField110dBmMouseClicked() {
    	final Color color = JColorChooser.showDialog(this, "Select a color", jTextField110dBm.getBackground());
        jTextField110dBm.setBackground(color);
        cto.setColor110dBm(color);
    }

    private void jTextField100dBmMouseClicked() {
    	final Color color = JColorChooser.showDialog(this, "Select a color", jTextField100dBm.getBackground());
        jTextField100dBm.setBackground(color);
        cto.setColor100dBm(color);
    }

    private void jTextField90dBmMouseClicked() {
    	final Color color = JColorChooser.showDialog(this, "Select a color", jTextField90dBm.getBackground());
        jTextField90dBm.setBackground(color);
        cto.setColor90dBm(color);
    }

    private void jTextField80dBmMouseClicked() {
    	final Color color = JColorChooser.showDialog(this, "Select a color", jTextField80dBm.getBackground());
        jTextField80dBm.setBackground(color);
        cto.setColor80dBm(color);
    }

    private void jTextField70dBmMouseClicked() {
    	final Color color = JColorChooser.showDialog(this, "Select a color", jTextField70dBm.getBackground());
        jTextField70dBm.setBackground(color);
        cto.setColor70dBm(color);
    }

    private void jTextField60dBmMouseClicked() {
    	final Color color = JColorChooser.showDialog(this, "Select a color", jTextField60dBm.getBackground());
        jTextField60dBm.setBackground(color);
        cto.setColor60dBm(color);
    }

    private void jTextField50dBmMouseClicked() {
    	final Color color = JColorChooser.showDialog(this, "Select a color", jTextField50dBm.getBackground());
        jTextField50dBm.setBackground(color);
        cto.setColor50dBm(color);
    }

    private void jTextField30sinadMouseClicked() {
    	final Color color = JColorChooser.showDialog(this, "Select a color", jTextField30sinad.getBackground());
        jTextField30sinad.setBackground(color);
        cto.setColor30sinad(color);
    }

    private void jTextField25sinadMouseClicked() {
    	final Color color = JColorChooser.showDialog(this, "Select a color", jTextField25sinad.getBackground());
        jTextField25sinad.setBackground(color);
        cto.setColor25sinad(color);
    }

    private void jTextField20sinadMouseClicked() {
    	final Color color = JColorChooser.showDialog(this, "Select a color", jTextField20sinad.getBackground());
        jTextField20sinad.setBackground(color);
        cto.setColor20sinad(color);
    }

    private void jTextField15sinadMouseClicked() {
    	final Color color = JColorChooser.showDialog(this, "Select a color", jTextField15sinad.getBackground());
        jTextField15sinad.setBackground(color);
        cto.setColor15sinad(color);
    }

    private void jTextField12sinadMouseClicked() {
    	final Color color = JColorChooser.showDialog(this, "Select a color", jTextField12sinad.getBackground());
        jTextField12sinad.setBackground(color);
        cto.setColor12sinad(color);
    }

    private void jTextField10sinadMouseClicked() {
    	final Color color = JColorChooser.showDialog(this, "Select a color", jTextField10sinad.getBackground());
        jTextField10sinad.setBackground(color);
        cto.setColor10sinad(color);
    }

    private void jTextField5sinadMouseClicked() {
    	final Color color = JColorChooser.showDialog(this, "Select a color", jTextField5sinad.getBackground());
        jTextField5sinad.setBackground(color);
        cto.setColor5sinad(color);
    }

    private void jTextField0sinadMouseClicked() {
    	final Color color = JColorChooser.showDialog(this, "Select a color", jTextField0sinad.getBackground());
        jTextField0sinad.setBackground(color);
        cto.setColor0sinad(color);
    }

    private void jTextField35berMouseClicked() {
    	final Color color = JColorChooser.showDialog(this, "Select a color", jTextField35ber.getBackground());
        jTextField35ber.setBackground(color);
        cto.setColor35ber(color);
    }

    private void jTextField30berMouseClicked() {
    	final Color color = JColorChooser.showDialog(this, "Select a color", jTextField30ber.getBackground());
        jTextField30ber.setBackground(color);
        cto.setColor30ber(color);
    }

    private void jTextField25berMouseClicked() {
    	final Color color = JColorChooser.showDialog(this, "Select a color", jTextField25ber.getBackground());
        jTextField25ber.setBackground(color);
        cto.setColor25ber(color);
    }

    private void jTextField20berMouseClicked() {
    	final Color color = JColorChooser.showDialog(this, "Select a color", jTextField20ber.getBackground());
        jTextField20ber.setBackground(color);
        cto.setColor20ber(color);
    }

    private void jTextField15berMouseClicked() {
    	final Color color = JColorChooser.showDialog(this, "Select a color", jTextField15ber.getBackground());
        jTextField15ber.setBackground(color);
        cto.setColor15ber(color);
    }

    private void jTextField10berMouseClicked() {
    	final Color color = JColorChooser.showDialog(this, "Select a color", jTextField10ber.getBackground());
        jTextField10ber.setBackground(color);
        cto.setColor10ber(color);
    }

    private void jTextField5berMouseClicked() {
    	final Color color = JColorChooser.showDialog(this, "Select a color", jTextField5ber.getBackground());
        jTextField5ber.setBackground(color);
        cto.setColor5ber(color);
    }

    private void jTextField0berMouseClicked() {
    	final Color color = JColorChooser.showDialog(this, "Select a color", jTextField0ber.getBackground());
        jTextField0ber.setBackground(color);
        cto.setColor0ber(color);
    }

    private void updateComponents() {
        predictedAreaReliabilityComboBox.setSelectedIndex(cto.getPredictedAreaReliabilityIndex());
        signalReqFor12dBSinadComboBox.setSelectedIndex(cto.getSignalReqFor12dBSinadIndex());
        signalReqFor20dBQuietingComboBox.setSelectedIndex(cto.getSignalReqFor20dBQuietingIndex());
        dynamicRangeComboBox.setSelectedIndex(cto.getDynamicRangeIndex());
        noiseFloorComboBox.setSelectedIndex(cto.getNoiseFloorIndex());
        confidenceIntervalComboBox.setSelectedIndex(cto.getConfidenceIntervalIndex());
        confidenceLevelComboBox.setSelectedIndex(cto.getConfidenceLevelIndex());
        signalReqFor5PctBERComboBox.setSelectedIndex(cto.getSignalReqFor5PctBERIndex());
        adjacentChannelRejectionComboBox.setSelectedIndex(cto.getAdjacentChannelRejectionIndex());
        contractualAreaReliabilityComboBox.setSelectedIndex(cto.getContractualAreaReliabilityIndex());
        channelToDisplayComboBox.setSelectedIndex(cto.getChannelToDisplayIndex());

        calcBasedOnCARReqRadioButton.setEnabled(cto.isEnableAutoCalc());
        calcBasedOnSARReqRadioButton.setEnabled(cto.isEnableAutoCalc());
        calcBasedOnNumberAllowedTilesRadioButton.setEnabled(cto.isEnableAutoCalc());

        testRxSpecsValidForTestCheckBox.setSelected(cto.isTestRxSpecsValidForTest());
        enableAutoCalcCheckBox.setSelected(cto.isEnableAutoCalc());
        alertOnMinimumSamplesPerTileAcquiredCheckBox.setSelected(cto.isAlertOnMinimumSamplesPerTileAcquired());
        showSignalMarkersCheckBox.setSelected(cto.isShowSignalMarkers());
        showGridSquareShadingCheckBox.setSelected(cto.isShowGridSquareShading());

        showTestGridCheckBox.setSelected(cto.isShowGrid());
        showRingsCheckBox.setSelected(cto.isShowRings());
        showQuadsCheckBox.setSelected(cto.isShowQuads());
        showLinesCheckBox.setSelected(cto.isShowLines());

        setTileSizeComboBox(requiredSizeOfTileComboBox, cto.getTileSizeDegrees());
        setTileSizeComboBox(maxVerticalSizeOfTileComboBox, cto.getMaxTileSize());
        setTileSizeComboBox(minVerticalSizeOfTileComboBox, cto.getMinTileSize());

        switch (cto.getSignalQualityDisplayMode()) {
            case SINAD ->
                signalQualityDisplayModeModel = displaySinadRadioButton.getModel();
            case DBM ->
                signalQualityDisplayModeModel = displayRssiRadioButton.getModel();
            case BER ->
                signalQualityDisplayModeModel = displayBerRadioButton.getModel();
        }

        switch (cto.getCalcBasis()) {
            case CONTRACTURAL_AREA_REQUIREMENT ->
                calcBasisModel = calcBasedOnCARReqRadioButton.getModel();
            case SERVICE_AREA_REQUIREMENT ->
                calcBasisModel = calcBasedOnSARReqRadioButton.getModel();
            case NUMBER_ALLOWED_TILES ->
                calcBasisModel = calcBasedOnNumberAllowedTilesRadioButton.getModel();
        }

        switch (cto.getSignalSampleMode()) {
            case SINAD -> {
                signalSampleModeModel = sampleSinadRadioButton.getModel();
                requestedSignalQualityComboBox.setModel(new DefaultComboBoxModel<>(CoverageTestObject.getSinadValues()));
                requestedSignalQualityComboBox.setSelectedIndex(cto.getRequestedSinadQualityIndex());
            }
            case DBM -> {
                signalSampleModeModel = sampleRssiRadioButton.getModel();
                requestedSignalQualityComboBox.setModel(new DefaultComboBoxModel<>(CoverageTestObject.getdBmValues()));
                requestedSignalQualityComboBox.setSelectedIndex(cto.getRequestedRssiQualityIndex());
            }
            case BER -> {
                signalSampleModeModel = sampleBerRadioButton.getModel();
                requestedSignalQualityComboBox.setModel(new DefaultComboBoxModel<>(CoverageTestObject.getBerValues()));
                requestedSignalQualityComboBox.setSelectedIndex(cto.getRequestedBerQualityIndex());
            }
        }

        switch (cto.getManualDataCollectionMode()) {
            case DISABLED ->
                manualDataCollectionModeModel = manualDataCollectionDisabledRadioButton.getModel();
            case CONTINUOUS ->
                manualDataCollectionModeModel = manualDataCollectionContinuousModeRadioButton.getModel();
            case MOUSE_KEYPRESS ->
                manualDataCollectionModeModel = manualDataCollectionMousePressModeRadioButton.getModel();
        }

        switch (cto.getTimingMode()) {
            case POSITION ->
                timingModeModel = positionBasedTimingRadioButton.getModel();
            case VARIABLE ->
                timingModeModel = speedBasedTimingRadioButton.getModel();
            case WAVELENGTHS ->
                timingModeModel = wavelengthsPerSubSampleRadioButton.getModel();
            case FIXED ->
                timingModeModel = fixedTimingRateRadioButton.getModel();
        }

        switch (cto.getAccessability()) {
            case ELIMINATE ->
                accessabilityModel = eliminateRadioButton.getModel();
            case ESTIMATE ->
                accessabilityModel = estimateRadioButton.getModel();
            case PASS ->
                accessabilityModel = passRadioButton.getModel();
        }

        switch (cto.getTestCriteria()) {
            case GREATER_THAN ->
                testCriteriaModel = greaterThanRadioButton.getModel();
            case WINDOW ->
                testCriteriaModel = windowRadioButton.getModel();
        }

        if ((isEqual(cto.getGridTopDegrees(), -999)) || (isEqual(cto.getGridLeftDegrees(), -999))) {
            gridBottomTextField.setEnabled(false);
            gridRightTextField.setEnabled(false);
            gridWidthMilesTextField.setEnabled(false);
            gridHeightMilesTextField.setEnabled(false);
        } else {
            gridBottomTextField.setEnabled(true);
            gridRightTextField.setEnabled(true);
            gridWidthMilesTextField.setEnabled(true);
            gridHeightMilesTextField.setEnabled(true);
        }

        if ((!isEqual(cto.getGridTopDegrees(), -999)) && (!isEqual(cto.getGridBottomDegrees(), -999))) {
            cto.setGridHeightDegrees(Math.abs(cto.getGridTopDegrees() - cto.getGridBottomDegrees()));
            gridHeightMilesTextField.setText(heightFormat.format((Vincenty.degreesToMiles(cto.getGridHeightDegrees(), 0.0, cto.getGridTopDegrees()))));
        }

        if ((!isEqual(cto.getGridLeftDegrees(), -999)) && (!isEqual(cto.getGridRightDegrees(), -999))) {
            cto.setGridWidthDegrees(Math.abs(cto.getGridLeftDegrees() - cto.getGridRightDegrees()));
            gridWidthMilesTextField.setText(widthFormat.format((Vincenty.degreesToMiles(cto.getGridWidthDegrees(), 90.0, cto.getGridTopDegrees()))));
        }

        if (!isEqual(cto.getGridTopDegrees(), -999)) {
            gridTopTextField.setText(latFormat.format(cto.getGridTopDegrees()));
        } else {
            gridTopTextField.setText("");
        }

        if (!isEqual(cto.getGridBottomDegrees(), -999)) {
            gridBottomTextField.setText(latFormat.format(cto.getGridBottomDegrees()));
        } else {
            gridBottomTextField.setText("");
        }

        if (!isEqual(cto.getGridLeftDegrees(), -999)) {
            gridLeftTextField.setText(lonFormat.format(cto.getGridLeftDegrees()));
        } else {
            gridLeftTextField.setText("");
        }

        if (!isEqual(cto.getGridRightDegrees(), -999)) {
            gridRightTextField.setText(lonFormat.format(cto.getGridRightDegrees()));
        } else {
            gridRightTextField.setText("");
        }

        if (isEqual(cto.getGridRightDegrees(), -999)) {
            cto.setGridRightDegrees(82.0);
        }
        if (isEqual(cto.getGridLeftDegrees(), -999)) {
            cto.setGridLeftDegrees(-83.0);
        }
        if (isEqual(cto.getGridTopDegrees(), -999)) {
            cto.setGridTopDegrees(40.0);
        }
        if (isEqual(cto.getGridBottomDegrees(), -999)) {
            cto.setGridBottomDegrees(39.0);
        }

        signalQualityDisplaySelectorRadioButtonGroup.setSelected(signalQualityDisplayModeModel, true);
        accessabilityRadioButtonGroup.setSelected(accessabilityModel, true);
        testCriteriaRadioButtonGroup.setSelected(testCriteriaModel, true);
        signalSampleRadioButtonGroup.setSelected(signalSampleModeModel, true);
        manualDataCollectionModeRadioButtonGroup.setSelected(manualDataCollectionModeModel, true);
        calcBasisRadioButtonGroup.setSelected(calcBasisModel, true);
        timingModeButtonGroup.setSelected(timingModeModel, true);

        maxNumberOfAllowedTilesTextField.setText(String.valueOf(cto.getMaxNumberOfAllowedTiles()));
        minSamplesPerTileTextField.setText(String.valueOf(cto.getMinSamplesPerTile()));
        maxSamplesPerTileTextField.setText(String.valueOf(cto.getMaxSamplesPerTile()));
        timingRateTextField.setText(String.valueOf(cto.getFixedTimePeriod()));
    }

    private void configureRxSpecs() {
        if (cto.isTestRxSpecsValidForTest()) {
            cto.setNoiseFloor(cto.getRadio().getCalibrationDataObject().getNoiseFloor());
            cto.setAdjacentChannelRejection(cto.getRadio().getCalibrationDataObject().getAdjacentChannelRejection());
            cto.setSignalReqFor12dBSinad(cto.getRadio().getCalibrationDataObject().getSignalReqFor12dBSINAD());
            cto.setSignalReqFor20dBQuieting(cto.getRadio().getCalibrationDataObject().getSignalReqFor12dBSINAD());
            cto.setSignalReqFor5PctBER(cto.getRadio().getCalibrationDataObject().getSignalReqFor5PctBER());
        }
        noiseFloorComboBox.setEnabled(!cto.isTestRxSpecsValidForTest());
        dynamicRangeComboBox.setEnabled(!cto.isTestRxSpecsValidForTest());
        adjacentChannelRejectionComboBox.setEnabled(!cto.isTestRxSpecsValidForTest());
        signalReqFor12dBSinadComboBox.setEnabled(!cto.isTestRxSpecsValidForTest());
        signalReqFor20dBQuietingComboBox.setEnabled(!cto.isTestRxSpecsValidForTest());
        signalReqFor5PctBERComboBox.setEnabled(!cto.isTestRxSpecsValidForTest());
    }

    private void setTileSizeComboBox(JComboBox<String> cb, Point2D.Double ts) {
        for (int i = 0; i < cb.getItemCount(); i++) {
            if (Math.abs(Double.parseDouble(cb.getItemAt(i).replaceAll("[^\\d.]", "")) - (ts.getY() * 3600D)) < 1) {
                cb.setSelectedIndex(i);
                return;
            } else if (i < cb.getItemCount() - 1 && Math.abs(Double.parseDouble(cb.getItemAt(i + 1).replaceAll("[^\\d.]", "")) - (ts.getY() * 3600D)) < 1) {
                cb.setSelectedIndex(i + 1);
                return;
            } else if (i == cb.getItemCount() - 1 || (Double.parseDouble(cb.getItemAt(i).replaceAll("[^\\d.]", "")) < (ts.getY() * 3600) && Double.parseDouble(cb.getItemAt(i + 1).replaceAll("[^\\d.]", "")) > (ts.getY() * 3600D))) {
                cb.insertItemAt(String.valueOf(ts.getY() * 3600D), i + 1);
                cb.setSelectedIndex(i + 1);
                return;
            }
        }
    }

    private class RadioButtonHandler implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent ie) {
            if (ie.getSource() == displaySinadRadioButton) {
                cto.setSignalQualityDisplayMode(SignalQualityDisplayMode.SINAD);
            } else if (ie.getSource() == displayRssiRadioButton) {
                cto.setSignalQualityDisplayMode(SignalQualityDisplayMode.DBM);
            } else if (ie.getSource() == displayBerRadioButton) {
                cto.setSignalQualityDisplayMode(SignalQualityDisplayMode.BER);
            } else if (ie.getSource() == manualDataCollectionDisabledRadioButton) {
                cto.setManualDataCollectionMode(ManualMode.DISABLED);
            } else if (ie.getSource() == manualDataCollectionContinuousModeRadioButton) {
                cto.setManualDataCollectionMode(ManualMode.CONTINUOUS);
            } else if (ie.getSource() == manualDataCollectionMousePressModeRadioButton) {
                cto.setManualDataCollectionMode(ManualMode.MOUSE_KEYPRESS);
            } else if (ie.getSource() == eliminateRadioButton) {
                cto.setAccessability(Accessability.ELIMINATE);
            } else if (ie.getSource() == estimateRadioButton) {
                cto.setAccessability(Accessability.ESTIMATE);
            } else if (ie.getSource() == passRadioButton) {
                cto.setAccessability(Accessability.PASS);
            } else if (ie.getSource() == sampleRssiRadioButton) {
                cto.setSignalSampleMode(SignalSampleMode.DBM);
                requestedSignalQualityComboBox.setModel(new DefaultComboBoxModel<>(CoverageTestObject.getdBmValues()));
                requestedSignalQualityComboBox.setSelectedIndex(cto.getRequestedRssiQualityIndex());
            } else if (ie.getSource() == sampleSinadRadioButton) {
                cto.setSignalSampleMode(SignalSampleMode.SINAD);
                requestedSignalQualityComboBox.setModel(new DefaultComboBoxModel<>(CoverageTestObject.getSinadValues()));
                requestedSignalQualityComboBox.setSelectedIndex(cto.getRequestedSinadQualityIndex());
            } else if (ie.getSource() == sampleBerRadioButton) {
                cto.setSignalSampleMode(SignalSampleMode.BER);
                requestedSignalQualityComboBox.setModel(new DefaultComboBoxModel<>(CoverageTestObject.getBerValues()));
                requestedSignalQualityComboBox.setSelectedIndex(cto.getRequestedBerQualityIndex());
            } else if (ie.getSource() == calcBasedOnCARReqRadioButton) {
                cto.setCalcBasis(CalcBasis.CONTRACTURAL_AREA_REQUIREMENT);
            } else if (ie.getSource() == calcBasedOnSARReqRadioButton) {
                cto.setCalcBasis(CalcBasis.SERVICE_AREA_REQUIREMENT);
            } else if (ie.getSource() == calcBasedOnNumberAllowedTilesRadioButton) {
                cto.setCalcBasis(CalcBasis.NUMBER_ALLOWED_TILES);
            } else if (ie.getSource() == greaterThanRadioButton) {
                cto.setTestCriteria(TestCriteria.GREATER_THAN);
                processAutoCalculations();
            } else if (ie.getSource() == windowRadioButton) {
                cto.setTestCriteria(TestCriteria.WINDOW);
                processAutoCalculations();
            } else if (ie.getSource() == fixedTimingRateRadioButton) {
                cto.setTimingMode(TimingMode.FIXED);
            } else if (ie.getSource() == positionBasedTimingRadioButton) {
                cto.setTimingMode(TimingMode.POSITION);
            } else if (ie.getSource() == wavelengthsPerSubSampleRadioButton) {
                cto.setTimingMode(TimingMode.WAVELENGTHS);
            } else if (ie.getSource() == speedBasedTimingRadioButton) {
                cto.setTimingMode(TimingMode.VARIABLE);
            }
        }
    }

    private JPanel getGUI() {
        final JPanel panel = new JPanel();
        final GroupLayout layout = new GroupLayout(panel);

        panel.setLayout(layout);

        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        final JTabbedPane tabbedPane = new JTabbedPane();

        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(tabbedPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(okButton, 90, 90, 90)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(applyButton, 90, 90, 90)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton, 90, 90, 90)
                        .addContainerGap()));

        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(tabbedPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(applyButton)
                                .addComponent(cancelButton)
                                .addComponent(okButton))
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        tabbedPane.addTab(" Coverage Test Settings ", null, getCoverageTestSettingsPanel(), null);
        tabbedPane.addTab(" Coverage Test Settings II ", null, getAdvancedCoverageTestSettingsPanel(), null);
        tabbedPane.addTab(" Display Settings ", null, getDisplaySettingsPanel(), null);
        tabbedPane.addTab(" Test Grid Settings ", null, getTestGridPanel(), null);

        return panel;
    }

    private JPanel getCoverageTestSettingsPanel() {
        final GroupLayout accessabilityPanelLayout = new GroupLayout(accessabilityPanel);

        accessabilityPanel.setLayout(accessabilityPanelLayout);

        accessabilityPanelLayout.setHorizontalGroup(accessabilityPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(accessabilityPanelLayout.createSequentialGroup()
                        .addContainerGap(10, 10)
                        .addGroup(accessabilityPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(eliminateRadioButton)
                                .addComponent(estimateRadioButton)
                                .addComponent(passRadioButton))
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        accessabilityPanelLayout.setVerticalGroup(accessabilityPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(accessabilityPanelLayout.createSequentialGroup()
                        .addContainerGap(5, 5)
                        .addComponent(eliminateRadioButton)
                        .addGap(5)
                        .addComponent(estimateRadioButton)
                        .addGap(5)
                        .addComponent(passRadioButton)
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        final GroupLayout valuesToBeSampledPanelLayout = new GroupLayout(valuesToBeSampledPanel);

        valuesToBeSampledPanel.setLayout(valuesToBeSampledPanelLayout);

        valuesToBeSampledPanelLayout.setHorizontalGroup(valuesToBeSampledPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(valuesToBeSampledPanelLayout.createSequentialGroup()
                        .addContainerGap(10, 10)
                        .addGroup(valuesToBeSampledPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(sampleRssiRadioButton)
                                .addComponent(sampleSinadRadioButton)
                                .addComponent(sampleBerRadioButton))
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        valuesToBeSampledPanelLayout.setVerticalGroup(valuesToBeSampledPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(valuesToBeSampledPanelLayout.createSequentialGroup()
                        .addContainerGap(5, 5)
                        .addComponent(sampleRssiRadioButton)
                        .addGap(5)
                        .addComponent(sampleSinadRadioButton)
                        .addGap(5)
                        .addComponent(sampleBerRadioButton)
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        final GroupLayout automaticParameterCalculationPanelLayout = new GroupLayout(automaticParameterCalculationPanel);

        automaticParameterCalculationPanel.setLayout(automaticParameterCalculationPanelLayout);

        automaticParameterCalculationPanelLayout.setHorizontalGroup(automaticParameterCalculationPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(automaticParameterCalculationPanelLayout.createSequentialGroup()
                        .addContainerGap(20, 20)
                        .addGroup(automaticParameterCalculationPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(enableAutoCalcCheckBox)
                                .addGroup(automaticParameterCalculationPanelLayout.createSequentialGroup()
                                        .addGap(20, 20, 20)
                                        .addGroup(automaticParameterCalculationPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                .addComponent(calcBasedOnNumberAllowedTilesRadioButton)
                                                .addComponent(calcBasedOnCARReqRadioButton)
                                                .addComponent(calcBasedOnSARReqRadioButton))))
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        automaticParameterCalculationPanelLayout.setVerticalGroup(automaticParameterCalculationPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(automaticParameterCalculationPanelLayout.createSequentialGroup()
                        .addContainerGap(5, 5)
                        .addComponent(enableAutoCalcCheckBox)
                        .addGap(5)
                        .addComponent(calcBasedOnCARReqRadioButton)
                        .addGap(5)
                        .addComponent(calcBasedOnSARReqRadioButton)
                        .addGap(5)
                        .addComponent(calcBasedOnNumberAllowedTilesRadioButton)
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        final GroupLayout reliabilityDesignTargetsPanelLayout = new GroupLayout(reliabilityDesignTargetsPanel);

        reliabilityDesignTargetsPanel.setLayout(reliabilityDesignTargetsPanelLayout);

        reliabilityDesignTargetsPanelLayout.setHorizontalGroup(reliabilityDesignTargetsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(GroupLayout.Alignment.TRAILING, reliabilityDesignTargetsPanelLayout.createSequentialGroup()
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(reliabilityDesignTargetsPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                .addComponent(contractualAreaReliabilityComboBoxLabel, GroupLayout.PREFERRED_SIZE, 200, GroupLayout.PREFERRED_SIZE)
                                .addComponent(confidenceIntervalComboBoxLabel, GroupLayout.PREFERRED_SIZE, 200, GroupLayout.PREFERRED_SIZE)
                                .addComponent(confidenceLevelComboBoxLabel, GroupLayout.PREFERRED_SIZE, 200, GroupLayout.PREFERRED_SIZE)
                                .addComponent(requestedSignalQualityLabel, GroupLayout.PREFERRED_SIZE, 200, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(reliabilityDesignTargetsPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                                .addComponent(requestedSignalQualityComboBox, GroupLayout.PREFERRED_SIZE, 145, GroupLayout.PREFERRED_SIZE)
                                .addComponent(contractualAreaReliabilityComboBox, GroupLayout.PREFERRED_SIZE, 145, GroupLayout.PREFERRED_SIZE)
                                .addComponent(confidenceIntervalComboBox, GroupLayout.PREFERRED_SIZE, 145, GroupLayout.PREFERRED_SIZE)
                                .addComponent(confidenceLevelComboBox, GroupLayout.PREFERRED_SIZE, 145, GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(10, 10)));

        reliabilityDesignTargetsPanelLayout.setVerticalGroup(reliabilityDesignTargetsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(reliabilityDesignTargetsPanelLayout.createSequentialGroup()
                        .addContainerGap(2, 2)
                        .addGroup(reliabilityDesignTargetsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(requestedSignalQualityLabel)
                                .addComponent(requestedSignalQualityComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addGap(2)
                        .addGroup(reliabilityDesignTargetsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(confidenceIntervalComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(confidenceIntervalComboBoxLabel))
                        .addGap(2)
                        .addGroup(reliabilityDesignTargetsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(confidenceLevelComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(confidenceLevelComboBoxLabel))
                        .addGap(2)
                        .addGroup(reliabilityDesignTargetsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(contractualAreaReliabilityComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(contractualAreaReliabilityComboBoxLabel))
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        final GroupLayout testParameterPanelLayout = new GroupLayout(testParameterPanel);

        testParameterPanel.setLayout(testParameterPanelLayout);

        testParameterPanelLayout.setHorizontalGroup(testParameterPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(GroupLayout.Alignment.TRAILING, testParameterPanelLayout.createSequentialGroup()
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(testParameterPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                .addComponent(minReqSampleRateLabel)
                                .addComponent(searchRadiusLabel)
                                .addComponent(requiredGridPointsLabel)
                                .addComponent(wavelengthsPerSubSampleLabel)
                                .addComponent(minSpeedForValidSubSampleLabel)
                                .addComponent(maxSpeedForValidSubSampleLabel)
                                .addComponent(measReqPerSubSampleLabel))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(testParameterPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                .addComponent(minReqSampleRateTextField, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE)
                                .addComponent(searchRadiusTextField, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE)
                                .addComponent(requiredGridPointsTextField, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE)
                                .addComponent(wavelengthsPerSubSampleTextField, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE)
                                .addComponent(minSpeedForValidSubSampleTextField, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE)
                                .addComponent(maxSpeedForValidSubSampleTextField, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE)
                                .addComponent(measReqPerSubSampleTextField, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(10, 10)));

        testParameterPanelLayout.setVerticalGroup(testParameterPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(testParameterPanelLayout.createSequentialGroup()
                        .addContainerGap(2, 2)
                        .addGroup(testParameterPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(requiredGridPointsTextField, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
                                .addComponent(requiredGridPointsLabel))
                        .addGap(2)
                        .addGroup(testParameterPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(searchRadiusTextField, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
                                .addComponent(searchRadiusLabel))
                        .addGap(2)
                        .addGroup(testParameterPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(wavelengthsPerSubSampleTextField, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
                                .addComponent(wavelengthsPerSubSampleLabel))
                        .addGap(2)
                        .addGroup(testParameterPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(minSpeedForValidSubSampleTextField, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
                                .addComponent(minSpeedForValidSubSampleLabel))
                        .addGap(2)
                        .addGroup(testParameterPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(maxSpeedForValidSubSampleTextField, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
                                .addComponent(maxSpeedForValidSubSampleLabel))
                        .addGap(2)
                        .addGroup(testParameterPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(minReqSampleRateTextField, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
                                .addComponent(minReqSampleRateLabel))
                        .addGap(2)
                        .addGroup(testParameterPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(measReqPerSubSampleTextField, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
                                .addComponent(measReqPerSubSampleLabel))
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        final GroupLayout testConstraintsPanelLayout = new GroupLayout(tileConstraintsPanel);

        tileConstraintsPanel.setLayout(testConstraintsPanelLayout);

        testConstraintsPanelLayout.setHorizontalGroup(testConstraintsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(GroupLayout.Alignment.TRAILING, testConstraintsPanelLayout.createSequentialGroup()
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(testConstraintsPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                                .addComponent(maxNumberOfAllowedTilesLabel)
                                .addComponent(requiredSizeOfTileLabel)
                                .addComponent(maxVerticalSizeOfTileComboBoxLabel, GroupLayout.PREFERRED_SIZE, 260, GroupLayout.PREFERRED_SIZE)
                                .addComponent(minVerticalSizeOfTileComboBoxLabel, GroupLayout.PREFERRED_SIZE, 260, GroupLayout.PREFERRED_SIZE)
                                .addComponent(minSamplesPerTileLabel)
                                .addComponent(maxSamplesPerTileLabel, GroupLayout.PREFERRED_SIZE, 260, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(testConstraintsPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                                .addComponent(maxNumberOfAllowedTilesTextField, GroupLayout.PREFERRED_SIZE, 70, Short.MAX_VALUE)
                                .addComponent(requiredSizeOfTileComboBox, GroupLayout.PREFERRED_SIZE, 70, Short.MAX_VALUE)
                                .addComponent(minVerticalSizeOfTileComboBox, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)
                                .addComponent(maxVerticalSizeOfTileComboBox, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)
                                .addComponent(minSamplesPerTileTextField, GroupLayout.PREFERRED_SIZE, 70, Short.MAX_VALUE)
                                .addComponent(maxSamplesPerTileTextField, GroupLayout.PREFERRED_SIZE, 70, Short.MAX_VALUE))
                        .addContainerGap(10, 10)));

        testConstraintsPanelLayout.setVerticalGroup(testConstraintsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(GroupLayout.Alignment.TRAILING, testConstraintsPanelLayout.createSequentialGroup()
                        .addContainerGap(5, 5)
                        .addGroup(testConstraintsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(maxNumberOfAllowedTilesTextField, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
                                .addComponent(maxNumberOfAllowedTilesLabel))
                        .addGap(2)
                        .addGroup(testConstraintsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(requiredSizeOfTileLabel)
                                .addComponent(requiredSizeOfTileComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addGap(2)
                        .addGroup(testConstraintsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(maxSamplesPerTileLabel)
                                .addComponent(maxSamplesPerTileTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addGap(2)
                        .addGroup(testConstraintsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(minSamplesPerTileLabel)
                                .addComponent(minSamplesPerTileTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addGap(2)
                        .addGroup(testConstraintsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(maxVerticalSizeOfTileComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(maxVerticalSizeOfTileComboBoxLabel))
                        .addGap(2)
                        .addGroup(testConstraintsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(minVerticalSizeOfTileComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(minVerticalSizeOfTileComboBoxLabel))
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        final JPanel panel = new JPanel();
        final GroupLayout layout = new GroupLayout(panel);

        panel.setLayout(layout);

        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(reliabilityDesignTargetsPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(tileConstraintsPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(valuesToBeSampledPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(accessabilityPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, Short.MAX_VALUE))
                                .addComponent(automaticParameterCalculationPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(testParameterPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap()));

        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(reliabilityDesignTargetsPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(tileConstraintsPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(automaticParameterCalculationPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(testParameterPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                                .addComponent(valuesToBeSampledPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(accessabilityPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                        .addGap(20, 20, 20)));

        return panel;
    }
    
    private static String removeAllNonNumericCharacters(String s) {
    	final StringBuilder sb = new StringBuilder(); 
    	s.chars() 
    	  .mapToObj(c -> (char) c) 
    	  .filter(c -> Character.isDigit(c) || c == '.') 
    	  .forEach(sb::append); 
    	return sb.toString();
    }

    private JPanel getAdvancedCoverageTestSettingsPanel() {
        final JPanel panel = new JPanel();

        final GroupLayout layout = new GroupLayout(panel);

        panel.setLayout(layout);

        final GroupLayout manualDataCollectionPanelLayout = new GroupLayout(manualDataCollectionModeSelectorPanel);

        manualDataCollectionModeSelectorPanel.setLayout(manualDataCollectionPanelLayout);

        manualDataCollectionPanelLayout.setHorizontalGroup(manualDataCollectionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(manualDataCollectionPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(manualDataCollectionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(manualDataCollectionDisabledRadioButton)
                                .addComponent(manualDataCollectionContinuousModeRadioButton)
                                .addComponent(manualDataCollectionMousePressModeRadioButton))
                        .addContainerGap()));

        manualDataCollectionPanelLayout.setVerticalGroup(manualDataCollectionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(manualDataCollectionPanelLayout.createSequentialGroup()
                        .addComponent(manualDataCollectionDisabledRadioButton)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(manualDataCollectionContinuousModeRadioButton)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(manualDataCollectionMousePressModeRadioButton)));

        final GroupLayout passFailCriteriaPanelLayout = new GroupLayout(passFailCriteriaSelectorPanel);

        passFailCriteriaSelectorPanel.setLayout(passFailCriteriaPanelLayout);

        passFailCriteriaPanelLayout.setHorizontalGroup(passFailCriteriaPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(passFailCriteriaPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(passFailCriteriaPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(greaterThanRadioButton)
                                .addComponent(windowRadioButton))
                        .addContainerGap()));

        passFailCriteriaPanelLayout.setVerticalGroup(passFailCriteriaPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(passFailCriteriaPanelLayout.createSequentialGroup()
                        .addComponent(greaterThanRadioButton)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(windowRadioButton)));

        final GroupLayout receiverPerformanceParametersPanelLayout = new GroupLayout(receiverPerformanceParametersPanel);
        receiverPerformanceParametersPanel.setLayout(receiverPerformanceParametersPanelLayout);
        receiverPerformanceParametersPanelLayout.setHorizontalGroup(
                receiverPerformanceParametersPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(GroupLayout.Alignment.TRAILING, receiverPerformanceParametersPanelLayout.createSequentialGroup()
                                .addGroup(receiverPerformanceParametersPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                        .addComponent(testRxSpecsValidForTestCheckBox, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(receiverPerformanceParametersPanelLayout.createSequentialGroup()
                                                .addContainerGap(190, Short.MAX_VALUE)
                                                .addGroup(receiverPerformanceParametersPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                                                        .addComponent(dynamicRangeLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(noiseFloorLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(adjacentChannelRejectionLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(signalReqFor12dBSinadLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(signalReqFor20dBQuietingLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(signalReqFor5PctBERLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(receiverPerformanceParametersPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                                        .addComponent(dynamicRangeComboBox, GroupLayout.PREFERRED_SIZE, 88, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(noiseFloorComboBox, GroupLayout.PREFERRED_SIZE, 88, GroupLayout.PREFERRED_SIZE)
                                                        .addGroup(receiverPerformanceParametersPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                                                .addComponent(signalReqFor12dBSinadComboBox)
                                                                .addComponent(adjacentChannelRejectionComboBox, GroupLayout.DEFAULT_SIZE, 88, Short.MAX_VALUE)
                                                                .addComponent(signalReqFor20dBQuietingComboBox)
                                                                .addComponent(signalReqFor5PctBERComboBox)))))
                                .addContainerGap())
        );
        
        receiverPerformanceParametersPanelLayout.setVerticalGroup(
                receiverPerformanceParametersPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(receiverPerformanceParametersPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(testRxSpecsValidForTestCheckBox)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(receiverPerformanceParametersPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                        .addComponent(noiseFloorLabel)
                                        .addComponent(noiseFloorComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(receiverPerformanceParametersPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                        .addComponent(dynamicRangeLabel)
                                        .addComponent(dynamicRangeComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(receiverPerformanceParametersPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                        .addComponent(adjacentChannelRejectionComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(adjacentChannelRejectionLabel))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(receiverPerformanceParametersPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                        .addComponent(signalReqFor12dBSinadLabel)
                                        .addComponent(signalReqFor12dBSinadComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(receiverPerformanceParametersPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                        .addComponent(signalReqFor20dBQuietingLabel)
                                        .addComponent(signalReqFor20dBQuietingComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(receiverPerformanceParametersPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                        .addComponent(signalReqFor5PctBERLabel)
                                        .addComponent(signalReqFor5PctBERComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        final GroupLayout sampleTimingPanelLayout = new GroupLayout(sampleTimingPanel);
        sampleTimingPanel.setLayout(sampleTimingPanelLayout);
        sampleTimingPanelLayout.setHorizontalGroup(
                sampleTimingPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(sampleTimingPanelLayout.createSequentialGroup()
                                .addGroup(sampleTimingPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(sampleTimingPanelLayout.createSequentialGroup()
                                                .addContainerGap()
                                                .addGroup(sampleTimingPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addComponent(fixedTimingRateRadioButton)
                                                        .addComponent(wavelengthsPerSubSampleRadioButton)
                                                        .addComponent(speedBasedTimingRadioButton)
                                                        .addComponent(positionBasedTimingRadioButton)))
                                        .addGroup(sampleTimingPanelLayout.createSequentialGroup()
                                                .addGap(27, 27, 27)
                                                .addComponent(timingRateTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(timingRateTextFieldLabel)))
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        
        sampleTimingPanelLayout.setVerticalGroup(
                sampleTimingPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(sampleTimingPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(positionBasedTimingRadioButton)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(speedBasedTimingRadioButton)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(wavelengthsPerSubSampleRadioButton)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(fixedTimingRateRadioButton)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(sampleTimingPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                        .addComponent(timingRateTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(timingRateTextFieldLabel))
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        final GroupLayout autoCalculatedCoverageTestParametersPanelLayout = new GroupLayout(autoCalculatedCoverageTestParametersPanel);
        autoCalculatedCoverageTestParametersPanel.setLayout(autoCalculatedCoverageTestParametersPanelLayout);
        autoCalculatedCoverageTestParametersPanelLayout.setHorizontalGroup(
                autoCalculatedCoverageTestParametersPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(autoCalculatedCoverageTestParametersPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(autoCalculatedCoverageTestParametersPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(autoCalculatedCoverageTestParametersPanelLayout.createSequentialGroup()
                                                .addComponent(calculatedNumberOfRequiredTilesTextFieldLabel, GroupLayout.DEFAULT_SIZE, 277, Short.MAX_VALUE)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(calculatedNumberOfRequiredTilesTextField, GroupLayout.PREFERRED_SIZE, 88, GroupLayout.PREFERRED_SIZE))
                                        .addComponent(confidencePanelScrollPane)
                                        .addGroup(autoCalculatedCoverageTestParametersPanelLayout.createSequentialGroup()
                                                .addComponent(confidenceStatementLabel)
                                                .addGap(0, 0, Short.MAX_VALUE)))
                                .addContainerGap())
        );
        
        autoCalculatedCoverageTestParametersPanelLayout.setVerticalGroup(
                autoCalculatedCoverageTestParametersPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(autoCalculatedCoverageTestParametersPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(autoCalculatedCoverageTestParametersPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(calculatedNumberOfRequiredTilesTextFieldLabel)
                                        .addComponent(calculatedNumberOfRequiredTilesTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(confidenceStatementLabel)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(confidencePanelScrollPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())
        );

        final GroupLayout measuredStatisticsPanelLayout = new GroupLayout(measuredPerformancePanel);
        measuredPerformancePanel.setLayout(measuredStatisticsPanelLayout);
        measuredStatisticsPanelLayout.setHorizontalGroup(
                measuredStatisticsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(measuredStatisticsPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(measuredStatisticsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(measuredStatisticsPanelLayout.createSequentialGroup()
                                                .addComponent(totalTilesTestedLabel, GroupLayout.DEFAULT_SIZE, 328, Short.MAX_VALUE)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(totalTilesTestedTextField, GroupLayout.PREFERRED_SIZE, 88, GroupLayout.PREFERRED_SIZE))
                                        .addGroup(measuredStatisticsPanelLayout.createSequentialGroup()
                                                .addComponent(totalTilesNotAccessableLabel, GroupLayout.DEFAULT_SIZE, 328, Short.MAX_VALUE)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(totalTilesNotAccessableTextField, GroupLayout.PREFERRED_SIZE, 88, GroupLayout.PREFERRED_SIZE))
                                        .addGroup(GroupLayout.Alignment.TRAILING, measuredStatisticsPanelLayout.createSequentialGroup()
                                                .addComponent(totalTilesFailedLabel, GroupLayout.DEFAULT_SIZE, 328, Short.MAX_VALUE)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(totalTilesFailedTextField, GroupLayout.PREFERRED_SIZE, 88, GroupLayout.PREFERRED_SIZE))
                                        .addGroup(measuredStatisticsPanelLayout.createSequentialGroup()
                                                .addComponent(totalTilesPassedLabel, GroupLayout.DEFAULT_SIZE, 328, Short.MAX_VALUE)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(totalTilesPassedTextField, GroupLayout.PREFERRED_SIZE, 88, GroupLayout.PREFERRED_SIZE))
                                        .addGroup(measuredStatisticsPanelLayout.createSequentialGroup()
                                                .addComponent(serviceAreaReliabilityLabel, GroupLayout.DEFAULT_SIZE, 328, Short.MAX_VALUE)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(serviceAreaReliabilityTextField, GroupLayout.PREFERRED_SIZE, 88, GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap()));
        
        measuredStatisticsPanelLayout.setVerticalGroup(
                measuredStatisticsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(measuredStatisticsPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(measuredStatisticsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(totalTilesTestedLabel)
                                        .addComponent(totalTilesTestedTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(measuredStatisticsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(totalTilesNotAccessableLabel)
                                        .addComponent(totalTilesNotAccessableTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(measuredStatisticsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(totalTilesFailedLabel)
                                        .addComponent(totalTilesFailedTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(measuredStatisticsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(totalTilesPassedLabel)
                                        .addComponent(totalTilesPassedTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(measuredStatisticsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(serviceAreaReliabilityLabel)
                                        .addComponent(serviceAreaReliabilityTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addComponent(receiverPerformanceParametersPanel, 450,450,450)
                    .addComponent(manualDataCollectionModeSelectorPanel, 450,450,450)
                    .addComponent(sampleTimingPanel, 450,450,450))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addComponent(passFailCriteriaSelectorPanel, 450,450,450)
                    .addComponent(autoCalculatedCoverageTestParametersPanel, 450,450,450)
                    .addComponent(measuredPerformancePanel, 450,450,450))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(receiverPerformanceParametersPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sampleTimingPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(manualDataCollectionModeSelectorPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(passFailCriteriaSelectorPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(autoCalculatedCoverageTestParametersPanel, GroupLayout.PREFERRED_SIZE, 190, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(measuredPerformancePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap()));

        return panel;
    }

    private JPanel getDisplaySettingsPanel() {
        final JPanel panel = new JPanel();
        final GroupLayout layout = new GroupLayout(panel);

        panel.setLayout(layout);

        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(tileShadingPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(signalQualityDisplaySelectorPanel))
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(rssiDataPointColoringPanel, 240, 240, 240)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(berDataPointColoringPanel, 210, 210, 210)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(sinadDataPointColoringPanel, 210, 210, 210)))
                        .addContainerGap()));

        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                .addComponent(signalQualityDisplaySelectorPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(tileShadingPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(rssiDataPointColoringPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(berDataPointColoringPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(sinadDataPointColoringPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addContainerGap()));

        final GroupLayout rssiDataPointColoringPanelLayout = new GroupLayout(rssiDataPointColoringPanel);

        rssiDataPointColoringPanel.setLayout(rssiDataPointColoringPanelLayout);

        rssiDataPointColoringPanelLayout.setHorizontalGroup(rssiDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(rssiDataPointColoringPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(rssiDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addGroup(rssiDataPointColoringPanelLayout.createSequentialGroup()
                                        .addComponent(jLabel90dBm)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jTextField90dBm, 30, 30, 30))
                                .addGroup(rssiDataPointColoringPanelLayout.createSequentialGroup()
                                        .addComponent(jLabel80dBm)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jTextField80dBm, 30, 30, 30))
                                .addGroup(rssiDataPointColoringPanelLayout.createSequentialGroup()
                                        .addComponent(jLabel70dBm)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jTextField70dBm, 30, 30, 30))
                                .addGroup(rssiDataPointColoringPanelLayout.createSequentialGroup()
                                        .addComponent(jLabel60dBm)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jTextField60dBm, 30, 30, 30))
                                .addGroup(rssiDataPointColoringPanelLayout.createSequentialGroup()
                                        .addComponent(jLabel50dBm)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jTextField50dBm, 30, 30, 30))
                                .addGroup(rssiDataPointColoringPanelLayout.createSequentialGroup()
                                        .addComponent(jLabel100dBm)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jTextField100dBm, 30, 30, 30))
                                .addGroup(rssiDataPointColoringPanelLayout.createSequentialGroup()
                                        .addComponent(jLabel110dBm)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jTextField110dBm, 30, 30, 30))
                                .addGroup(rssiDataPointColoringPanelLayout.createSequentialGroup()
                                        .addComponent(jLabel120dBm)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jTextField120dBm, 30, 30, 30)))
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        rssiDataPointColoringPanelLayout.setVerticalGroup(rssiDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(rssiDataPointColoringPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(rssiDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel50dBm)
                                .addComponent(jTextField50dBm, 14, 14, 14))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(rssiDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel60dBm)
                                .addComponent(jTextField60dBm, 14, 14, 14))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(rssiDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel70dBm)
                                .addComponent(jTextField70dBm, 14, 14, 14))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(rssiDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel80dBm)
                                .addComponent(jTextField80dBm, 14, 14, 14))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(rssiDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel90dBm)
                                .addComponent(jTextField90dBm, 14, 14, 14))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(rssiDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel100dBm)
                                .addComponent(jTextField100dBm, 14, 14, 14))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(rssiDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel110dBm)
                                .addComponent(jTextField110dBm, 14, 14, 14))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(rssiDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel120dBm)
                                .addComponent(jTextField120dBm, 14, 14, 14))
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        final GroupLayout berDataPointColoringPanelLayout = new GroupLayout(berDataPointColoringPanel);

        berDataPointColoringPanel.setLayout(berDataPointColoringPanelLayout);

        berDataPointColoringPanelLayout.setHorizontalGroup(berDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(berDataPointColoringPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(berDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addGroup(berDataPointColoringPanelLayout.createSequentialGroup()
                                        .addComponent(jLabel20ber)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jTextField20ber, 30, 30, 30))
                                .addGroup(berDataPointColoringPanelLayout.createSequentialGroup()
                                        .addComponent(jLabel15ber)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jTextField15ber, 30, 30, 30))
                                .addGroup(berDataPointColoringPanelLayout.createSequentialGroup()
                                        .addComponent(jLabel10ber)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jTextField10ber, 30, 30, 30))
                                .addGroup(berDataPointColoringPanelLayout.createSequentialGroup()
                                        .addComponent(jLabel5ber)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jTextField5ber, 30, 30, 30))
                                .addGroup(berDataPointColoringPanelLayout.createSequentialGroup()
                                        .addComponent(jLabel0ber)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jTextField0ber, 30, 30, 30))
                                .addGroup(berDataPointColoringPanelLayout.createSequentialGroup()
                                        .addComponent(jLabel25ber)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jTextField25ber, 30, 30, 30))
                                .addGroup(berDataPointColoringPanelLayout.createSequentialGroup()
                                        .addComponent(jLabel30ber)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jTextField30ber, 30, 30, 30))
                                .addGroup(berDataPointColoringPanelLayout.createSequentialGroup()
                                        .addComponent(jLabel35ber)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jTextField35ber, 30, 30, 30)))
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        berDataPointColoringPanelLayout.setVerticalGroup(berDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(berDataPointColoringPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(berDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel0ber)
                                .addComponent(jTextField0ber, 14, 14, 14))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(berDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel5ber)
                                .addComponent(jTextField5ber, 14, 14, 14))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(berDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel10ber)
                                .addComponent(jTextField10ber, 14, 14, 14))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(berDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel15ber)
                                .addComponent(jTextField15ber, 14, 14, 14))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(berDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel20ber)
                                .addComponent(jTextField20ber, 14, 14, 14))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(berDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel25ber)
                                .addComponent(jTextField25ber, 14, 14, 14))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(berDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel30ber)
                                .addComponent(jTextField30ber, 14, 14, 14))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(berDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel35ber)
                                .addComponent(jTextField35ber, 14, 14, 14))
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        final GroupLayout sinadDataPointColoringPanelLayout = new GroupLayout(sinadDataPointColoringPanel);
        sinadDataPointColoringPanel.setLayout(sinadDataPointColoringPanelLayout);

        sinadDataPointColoringPanelLayout.setHorizontalGroup(sinadDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(sinadDataPointColoringPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(sinadDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addGroup(sinadDataPointColoringPanelLayout.createSequentialGroup()
                                        .addComponent(jLabel20sinad)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jTextField20sinad, 30, 30, 30))
                                .addGroup(sinadDataPointColoringPanelLayout.createSequentialGroup()
                                        .addComponent(jLabel15sinad)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jTextField15sinad, 30, 30, 30))
                                .addGroup(sinadDataPointColoringPanelLayout.createSequentialGroup()
                                        .addComponent(jLabel10sinad)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jTextField10sinad, 30, 30, 30))
                                .addGroup(sinadDataPointColoringPanelLayout.createSequentialGroup()
                                        .addComponent(jLabel5sinad)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jTextField5sinad, 30, 30, 30))
                                .addGroup(sinadDataPointColoringPanelLayout.createSequentialGroup()
                                        .addComponent(jLabel0sinad)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jTextField0sinad, 30, 30, 30))
                                .addGroup(sinadDataPointColoringPanelLayout.createSequentialGroup()
                                        .addComponent(jLabel25sinad)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jTextField25sinad, 30, 30, 30))
                                .addGroup(sinadDataPointColoringPanelLayout.createSequentialGroup()
                                        .addComponent(jLabel30sinad)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jTextField30sinad, 30, 30, 30))
                                .addGroup(sinadDataPointColoringPanelLayout.createSequentialGroup()
                                        .addComponent(jLabel12sinad)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jTextField12sinad, 30, 30, 30)))
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        sinadDataPointColoringPanelLayout.setVerticalGroup(sinadDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(sinadDataPointColoringPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(sinadDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel0sinad)
                                .addComponent(jTextField0sinad, 14, 14, 14))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(sinadDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel5sinad)
                                .addComponent(jTextField5sinad, 14, 14, 14))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(sinadDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel10sinad)
                                .addComponent(jTextField10sinad, 14, 14, 14))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(sinadDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel12sinad)
                                .addComponent(jTextField12sinad, 14, 14, 14))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(sinadDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel15sinad)
                                .addComponent(jTextField15sinad, 14, 14, 14))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(sinadDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel20sinad)
                                .addComponent(jTextField20sinad, 14, 14, 14))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(sinadDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel25sinad)
                                .addComponent(jTextField25sinad, 14, 14, 14))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(sinadDataPointColoringPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel30sinad)
                                .addComponent(jTextField30sinad, 14, 14, 14))
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        final GroupLayout signalQualityDisplaySelectorPanelLayout = new GroupLayout(signalQualityDisplaySelectorPanel);

        signalQualityDisplaySelectorPanel.setLayout(signalQualityDisplaySelectorPanelLayout);

        signalQualityDisplaySelectorPanelLayout.setHorizontalGroup(signalQualityDisplaySelectorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(signalQualityDisplaySelectorPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(signalQualityDisplaySelectorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addGroup(signalQualityDisplaySelectorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(displaySinadRadioButton)
                                        .addComponent(displayRssiRadioButton)
                                        .addComponent(displayBerRadioButton))
                                .addGroup(signalQualityDisplaySelectorPanelLayout.createSequentialGroup()
                                        .addComponent(channelToDisplayLabel)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(channelToDisplayComboBox, 100, 100, 100)))
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        signalQualityDisplaySelectorPanelLayout.setVerticalGroup(signalQualityDisplaySelectorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(signalQualityDisplaySelectorPanelLayout.createSequentialGroup()
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(displaySinadRadioButton)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(displayRssiRadioButton)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(displayBerRadioButton)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(signalQualityDisplaySelectorPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(channelToDisplayLabel)
                                .addComponent(channelToDisplayComboBox))
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        final GroupLayout tileShadingPanelLayout = new GroupLayout(tileShadingPanel);

        tileShadingPanel.setLayout(tileShadingPanelLayout);

        tileShadingPanelLayout.setHorizontalGroup(tileShadingPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(tileShadingPanelLayout.createSequentialGroup()
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(tileShadingPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addGroup(tileShadingPanelLayout.createSequentialGroup()
                                        .addGroup(tileShadingPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                                .addComponent(jTextFieldTileSelectedColor, 30, 30, 30)
                                                .addComponent(jTextFieldTileInProgressColor, 30, 30, 30)
                                                .addComponent(jTextFieldTileCompleteColor, 30, 30, 30)
                                                .addComponent(jTextFieldTileInaccessableColor, 30, 30, 30)
                                                .addComponent(jTextFieldGridColor, 30, 30, 30))
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(tileShadingPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                .addComponent(jLabelTileSelectedColorLabel)
                                                .addComponent(jLabelTileInProgressColorLabel)
                                                .addComponent(jLabelTileCompleteColorLabel)
                                                .addComponent(jLabelTileInaccessableColorLabel)
                                                .addComponent(jLabelGridColorLabel))
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED))
                                .addComponent(showGridSquareShadingCheckBox)
                                .addComponent(showTestGridCheckBox))
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        tileShadingPanelLayout.setVerticalGroup(tileShadingPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(tileShadingPanelLayout.createSequentialGroup()
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(tileShadingPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(jTextFieldTileSelectedColor, 14, 14, 14)
                                .addComponent(jLabelTileSelectedColorLabel))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(tileShadingPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(jTextFieldTileInProgressColor, 14, 14, 14)
                                .addComponent(jLabelTileInProgressColorLabel))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(tileShadingPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(jTextFieldTileCompleteColor, 14, 14, 14)
                                .addComponent(jLabelTileCompleteColorLabel))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(tileShadingPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(jTextFieldTileInaccessableColor, 14, 14, 14)
                                .addComponent(jLabelTileInaccessableColorLabel))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(tileShadingPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(jTextFieldGridColor, 14, 14, 14)
                                .addComponent(jLabelGridColorLabel))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(showGridSquareShadingCheckBox)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(showTestGridCheckBox)
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        return panel;
    }

    private JPanel getTestGridPanel() {
        final JPanel panel = new JPanel();
        final GroupLayout layout = new GroupLayout(panel);

        panel.setLayout(layout);

        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(testGridDimensionPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap()));

        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(testGridDimensionPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap()));

        final GroupLayout mapImageModelPanelLayout = new GroupLayout(mapImageModelPanel);
        mapImageModelPanel.setLayout(mapImageModelPanelLayout);

        mapImageModelPanelLayout.setHorizontalGroup(
                mapImageModelPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(mapImageModelPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(mapImageModelPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(mapImageModelPanelLayout.createSequentialGroup()
                                                .addGroup(mapImageModelPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                                        .addGroup(mapImageModelPanelLayout.createSequentialGroup()
                                                                .addComponent(gridEdgeRightLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(gridRightTextField, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE))
                                                        .addGroup(mapImageModelPanelLayout.createSequentialGroup()
                                                                .addComponent(gridEdgeBottomLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(gridBottomTextField, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)))
                                                .addContainerGap())
                                        .addGroup(mapImageModelPanelLayout.createSequentialGroup()
                                                .addGroup(mapImageModelPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addGroup(mapImageModelPanelLayout.createSequentialGroup()
                                                                .addComponent(gridLeftTextField, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(gridEdgeLeftLabel, GroupLayout.DEFAULT_SIZE, 225, Short.MAX_VALUE))
                                                        .addGroup(mapImageModelPanelLayout.createSequentialGroup()
                                                                .addComponent(gridTopTextField, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(gridEdgeTopLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                                .addContainerGap(10, Short.MAX_VALUE)))));

        mapImageModelPanelLayout.setVerticalGroup(
                mapImageModelPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(mapImageModelPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(mapImageModelPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(gridTopTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(gridEdgeTopLabel))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(mapImageModelPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(gridLeftTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(gridEdgeLeftLabel))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(mapImageModelPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(gridBottomTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(gridEdgeBottomLabel))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(mapImageModelPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(gridRightTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(gridEdgeRightLabel))
                                .addContainerGap()));

        final GroupLayout testGridDimensionPanelLayout = new GroupLayout(testGridDimensionPanel);
        testGridDimensionPanel.setLayout(testGridDimensionPanelLayout);

        testGridDimensionPanelLayout.setHorizontalGroup(
                testGridDimensionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(testGridDimensionPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(testGridDimensionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                        .addComponent(drawTestGridOnMapButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(tileSelectionModeButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                                .addGroup(testGridDimensionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(GroupLayout.Alignment.TRAILING, testGridDimensionPanelLayout.createSequentialGroup()
                                                .addGroup(testGridDimensionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                                        .addComponent(gridEdgeWidthLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(gridEdgeHeightLabel, GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE))
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addGroup(testGridDimensionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                                        .addComponent(gridHeightMilesTextField)
                                                        .addComponent(gridWidthMilesTextField, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE))
                                                .addContainerGap(70, Short.MAX_VALUE))
                                        .addComponent(mapImageModelPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))));

        testGridDimensionPanelLayout.setVerticalGroup(
                testGridDimensionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(testGridDimensionPanelLayout.createSequentialGroup()
                                .addGroup(testGridDimensionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(testGridDimensionPanelLayout.createSequentialGroup()
                                                .addGap(40, 40, 40)
                                                .addComponent(drawTestGridOnMapButton)
                                                .addGap(20, 20, 20)
                                                .addComponent(tileSelectionModeButton))
                                        .addGroup(testGridDimensionPanelLayout.createSequentialGroup()
                                                .addGap(20, 20, 20)
                                                .addComponent(mapImageModelPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                                .addGap(20, 20, 20)
                                .addGroup(testGridDimensionPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(gridWidthMilesTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(gridEdgeWidthLabel))
                                .addGap(8, 8, 8)
                                .addGroup(testGridDimensionPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(gridHeightMilesTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(gridEdgeHeightLabel))
                                .addContainerGap(35, Short.MAX_VALUE)));

        return panel;
    }

}
