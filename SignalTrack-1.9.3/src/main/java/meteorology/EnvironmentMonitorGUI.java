package meteorology;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Toolkit;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import java.time.format.DateTimeFormatter;

import java.time.temporal.ChronoUnit;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.LayoutStyle;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.netbeans.lib.awtextra.AbsoluteConstraints;
import org.netbeans.lib.awtextra.AbsoluteLayout;

import aprs.AbstractAPRSProcessor;
import aprs.AprsProcessor;
import components.EventPanel;
import gov.epa.AirNowAPI;
import gov.epa.UVIndexReader;

import gov.nasa.api.donki.NASASpaceWeatherProcessor;
import gov.nasa.api.donki.NASASpaceWeatherGUI.Style;

import gov.nasa.api.ners.NetworkEarthRotationService;

import lunar.MoonCalc;
import lunar.MoonShape;

import meteorology.AbstractEnvironmentSensor.SpeedUnit;

import n1ctf.TempSensorClient;
import n1ctf.GeigerCounterClient;
import n1ctf.AirQualitySensorClient;

import solar.SolarTimingPanel;

import time.ConsolidatedTime;
import time.DateTimeServiceComponent;

import time.AstronomicalTime;

/**
 *
 * @author John
 */
public class EnvironmentMonitorGUI extends JFrame {

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_DIALOG_TITLE = "Station N1CTF - Local Weather Conditions";
	private static final Dimension DEFAULT_BUTTON_DIM = new Dimension(100, 18);
	private static final String DEFAULT_FONT = "Arial";
	private static final int TOOLTIP_DEFAULT_DISMISS_TIMEOUT = ToolTipManager.sharedInstance().getDismissDelay();
	private static final int TOOLTIP_DEFAULT_EXTENDED_DELAY = 60000;

    private static final long UPDATE_DELAY_MILLISECONDS = 1000;
    private static final long APRS_ROTATOR_PERSISTENCE_SECONDS = 10;
	
	public enum DisplaySize {
		Size_1024x768, Size_Foundation_7_Inch
	}

	private CompassRosePanel compassRosePanel;
	
	private final AtomicBoolean toolTipRotatorRunning = new AtomicBoolean(false);

	private JLabel lblBaroPressAbs;
	private JLabel lblBaroPressStation;
	private JLabel lblBaroPressAltitude;	
	private JLabel lblAltimeterPressure;
	private JLabel lblBaroPressSeaLevel;

	private JLabel lblBlackGlobeTemp;
	private JLabel lblWindChill;
	private JLabel lblDryBulbTemp;
	private JLabel lblDailyHigh;
	private JLabel lblDailyLow;
	private JLabel lblDewPoint;
	private JLabel lblDewPointDecorated;
	private JLabel lblHeatIndex;
	private JLabel lblHeatIndexDecorated;
	private JLabel lblWetBulbTemp;
	private JLabel lblWetBulbGlobeTemp;
	private JLabel lblWetBulbGlobeTempDecorated;
	
	private JLabel lbl24HrLowLabel;
	private JLabel lbl24HrHighLabel;
	
	private WetBulbGlobeTempLabel lblWetBulbGlobeTempFlag;
	private HeatIndexTempLabel lblHeatIndexFlag;

	private JLabel lblAprsDTG;
	private JLabel lblAprsTPlus;

	private JLabel lblHumidity;

	private JLabel lblDailyRain;
	private JLabel lbl24HourRain;
	private JLabel lblMonthlyRain;
	private JLabel lblRainRate;
	private JLabel lblPeakRainRate;
	private JLabel lblYearlyRain;
	private JLabel lblWeeklyRain;

	private JLabel lblDailyRainAsOfDTG;
	private JLabel lbl24HourRainAsOfDTG;
	private JLabel lblMonthlyRainAsOfDTG;
	private JLabel lblWeeklyRainAsOfDTG;
	private JLabel lblYearlyRainAsOfDTG;

	private JLabel lblWindSpeedCurrent;
	private JLabel lblWindSpeedGusting;
	private JLabel lblWindSpeedMaxDaily;

	private JLabel lblAQIOzone;
	private JLabel lblAQIOzoneFlag;
	private JLabel lblAQIPPM10;
	private JLabel lblAQIPPM10Flag;
	private JLabel lblAQIPPM25;
	private JLabel lblAQIPPM25Flag;
	private JLabel lblAQIOzoneFlagDecorated;
	private JLabel lblAQIPPM10FlagDecorated;
	private JLabel lblAQIPPM25FlagDecorated;
	private JLabel lblTVOC;
	private JLabel lblTVOCFlag;
	private JLabel lblTVOCFlagDecorated;
	private JLabel lblECO2;
	private JLabel lblECO2Flag;
	private JLabel lblECO2FlagDecorated;
	private JLabel lblLastUpdateFromAgency;
	private JLabel lblLocAtDtg;

	private JLabel lblSolarAzimuth;
	private JLabel lblLodExcessSeconds;
	private JLabel lblSolarElevation;
	private JLabel lblSolarIrradiance;
	private JLabel lblUVARad;
	private JLabel lblUVBRad;
	private JLabel lblUVIndex;
	private JLabel lblUVIndexFlag;

	private JLabel lblLunarPhase;
	private JLabel lblLunarState;
	private JLabel lblLunarPhaseAngle;
	private JLabel lblLunarDistance;
	private JLabel lblLunarElevation;
	private JLabel lblMoonRise;
	private JLabel lblMoonSet;
	private JLabel lblLunarAzimuth;

	private JLabel lblCPM;
	private JLabel lblGammaRad;
	private JLabel lblGammaRadDecorated;
	private JLabel lblBetaRad;
	private JLabel lblBetaRadDecorated;
	private JLabel lblAlphaRad;
	private JLabel lblAlphaRadDecorated;

	private JButton exitButton;
	private JButton configGPSButton;
	private JButton configSensorButton;
	private JButton configAPRSButton;
	private JButton configSWPButton;
	private JButton configTimeZoneButton;
	
	private String location;
	private String currentAirQuality;
	private String agency;
	private String lastUpdate;

	private JLabel sunrise = new JLabel();
	private JLabel sunriseEnd = new JLabel();
	private JLabel eveningGoldenHour = new JLabel();
	private JLabel morningGoldenHour = new JLabel();
	private JLabel solarNoon = new JLabel();
	private JLabel sunsetStart = new JLabel();
	private JLabel sunset = new JLabel();
	private JLabel dusk = new JLabel();
	private JLabel nauticalDusk = new JLabel();
	private JLabel night = new JLabel();
	private JLabel nadir = new JLabel();
	private JLabel nightEnd = new JLabel();
	private JLabel nauticalDawn = new JLabel();
	private JLabel dawn = new JLabel();
	
	private MoonShape moonShape;
	
	private ZonedDateTime utcTime = ZonedDateTime.now(ZoneId.of("UTC"));
	private ZonedDateTime localTime = ZonedDateTime.now();
	
	private DisplaySize displaySize;

	private JToolBar toolBar;
	
	private transient SolarTimingPanel solarTimingPanel;
	
	private final transient DateTimeServiceComponent dts;
	private final transient AbstractEnvironmentSensor aes;
	private final transient AirNowAPI airNowAPI;
	private final transient TempSensorClient bgts;
	private final transient GeigerCounterClient gc;
	private final transient AirQualitySensorClient aqs;
	private final transient NASASpaceWeatherProcessor swp;
	private final transient NetworkEarthRotationService ners;
	private final transient AprsProcessor aprsProc;
	private final transient ConsolidatedTime consolidatedTime;
	private final transient EventPanel eventPanel;

	private JPanel guiPanel;

	private int lunarDayOfYear; // These prevent calculations from running every time a clock event is fired.
	private int solarDayOfYear; // They insure the event is fired on startup, and then once per day thereafter.

	private transient ExecutorService executor;
	
	public EnvironmentMonitorGUI(DisplaySize displaySize, AbstractEnvironmentSensor aes, AirNowAPI airNowAPI,
			TempSensorClient bgts, GeigerCounterClient gc, AirQualitySensorClient aqs, NASASpaceWeatherProcessor swp,
			NetworkEarthRotationService ners, AprsProcessor aprs, ConsolidatedTime consolidatedTime, EventPanel eventPanel) {

		this.displaySize = displaySize;
		this.aes = aes;
		this.airNowAPI = airNowAPI;
		this.bgts = bgts;
		this.gc = gc;
		this.aqs = aqs;
		this.swp = swp;
		this.ners = ners;
		this.aprsProc = aprs;
		this.consolidatedTime = consolidatedTime;
		this.eventPanel = eventPanel;

		dts = new DateTimeServiceComponent(aes.getConsolidatedTime(), ners);
		
		super.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent event) {
				if (WindowEvent.WINDOW_CLOSING == event.getID()) {
					dts.close();
		    		setAprsRotator(false);
				}
			}
		});

		initComponents();
		configureComponents();
		createToolBar();
		configureListeners();
		
		invokeLaterInDispatchThreadIfNeeded(() -> {
			if (displaySize == DisplaySize.Size_1024x768) {
				guiPanel = getGraphicalUserInterface();
				add(guiPanel);
				setUndecorated(false);
				pack();
				setSize(1020, 730);
				final Toolkit tk = Toolkit.getDefaultToolkit();
				final Dimension screenSize = tk.getScreenSize();
				setLocation((screenSize.width / 2) - (getWidth() / 2), (screenSize.height / 2) - (getHeight() / 2));
				setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
				setResizable(true);
				setVisible(true);
			} else if (displaySize == DisplaySize.Size_Foundation_7_Inch) {
				guiPanel = getGuiPanel7InchFoundationDisplay();
				add(guiPanel);
				setUndecorated(true);
				final Toolkit tk = Toolkit.getDefaultToolkit();
				final Dimension screenSize = tk.getScreenSize();
				pack();
				setSize(800, 480);
				setLocation((screenSize.width / 2) - (getWidth() / 2), (screenSize.height / 2) - (getHeight() / 2));
				setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
				setResizable(false);
				setVisible(true);
				setMaximumSize(new Dimension(800, 480));
			}
		});

		dts.startTimeStandardRotate();
	}

	private void createToolBar() {
		toolBar.add(exitButton);
		toolBar.add(configGPSButton);
		toolBar.add(configSensorButton);
		toolBar.add(configAPRSButton);
		toolBar.add(configSWPButton);
		toolBar.add(configTimeZoneButton);
	}

	public static String toHTMLFormat(String str, int width) {
		final StringBuilder sb = new StringBuilder("<HTML>");
		final int fullRounds = str.length() / width;
		int x;
		for (x = 0; x < fullRounds; x++) {
			sb.append(str.substring(x * width, (x + 1) * width));
			sb.append("<br>");
		}
		sb.append(str.substring(x * width));
		sb.append("</HTML>");
		return sb.toString();
	}
	
	private final class APRSToolTipRotator implements Runnable {
		
		private int count;
		
		private APRSToolTipRotator() {
			count = 0;
		}
		
		@Override
		public void run() {
			while (aprsProc.isRotate()) {
				if (aprsProc.getAPRSTNCClient().isReportEnabled()) {
					setToolTips(aprsProc.getAPRSTNCClient());
				}
				if (aprsProc.getAPRSISClient().isReportEnabled()) {
					setToolTips(aprsProc.getAPRSISClient());
				}
				if (aprsProc.getCWOPUpdater().isReportEnabled()) {
					setToolTips(aprsProc.getCWOPUpdater());
				}
				if (aprsProc.getWUGUpdater().isReportEnabled()) {
					setToolTips(aprsProc.getWUGUpdater());
				}
				if (aprsProc.getRadMonAPI().isReportEnabled()) {
					setToolTips(aprsProc.getRadMonAPI());
				}
			}
		}
		
		private void setToolTips(AbstractAPRSProcessor absAprsProc) {
			while (absAprsProc.isReportEnabled()) {
				try {
					if (absAprsProc.getTimeOfLastUpdate() != null && utcTime != null) {
						setAprsTPlus("T-" + ConsolidatedTime.toMinuteSecondFormat(ChronoUnit.SECONDS.between(utcTime, absAprsProc.getTimeOfNextUpdate())) + " " + absAprsProc.getAbbreviatedTag());
						setAprsTPlusToolTipText("Time in minutes and seconds until next " + absAprsProc.getTag() + " update");
						setAprsDTG(zonedDateTimeToISO(absAprsProc.getTimeOfLastUpdate()));
						setAprsDTGToolTipText(absAprsProc.getHTMLString());
					}
					if (!aprsProc.isRotate()) {
						break;
					}
					TimeUnit.MILLISECONDS.sleep(UPDATE_DELAY_MILLISECONDS);
					count++;
					if (count >= APRS_ROTATOR_PERSISTENCE_SECONDS || !aprsProc.isRotate()) {
						count = 0;
						break;
					}
				} catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				} 
			}
		}
		
		private void setAprsTPlus(String text) {
			invokeLaterInDispatchThreadIfNeeded(() -> lblAprsTPlus.setText(text));
		}
		
		private void setAprsTPlusToolTipText(String text) {
			invokeLaterInDispatchThreadIfNeeded(() -> lblAprsTPlus.setToolTipText(text));
		}
		
		private void setAprsDTG(String text) {
			invokeLaterInDispatchThreadIfNeeded(() -> lblAprsDTG.setText(text));
		}
		
		private void setAprsDTGToolTipText(String text) {
			invokeLaterInDispatchThreadIfNeeded(() -> lblAprsDTG.setToolTipText(text));
		}
		
		private String zonedDateTimeToISO(ZonedDateTime zonedDateTime) {
			final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX");
	        return zonedDateTime.format(formatter);
	    }
	}
	
	private void configureListeners() {
		final PropertyChangeListener consolidatedTimeListener = (PropertyChangeEvent event)  -> {
			if (ConsolidatedTime.ZDT_UTC.equals(event.getPropertyName())) {
				utcTime = (ZonedDateTime) event.getNewValue();
				localTime = utcTime.withZoneSameInstant(consolidatedTime.getLocalZoneId());
				
				solarTimingPanel.setZonedDateTime(localTime);

				setSolarAzimuth(AbstractEnvironmentSensor.toDecimalFormat(Math.toDegrees(AstronomicalTime.getSolarAzimuth(localTime,
						aes.getStationLatitudeDegrees(), aes.getStationLongitudeDegrees())), 4) + " \u00B0");
				
				setSolarElevation(AbstractEnvironmentSensor.toDecimalFormat(Math.toDegrees(AstronomicalTime.getSolarElevation(localTime,
						aes.getStationLatitudeDegrees(), aes.getStationLongitudeDegrees())), 4) + " \u00B0");
				
				if (solarDayOfYear != localTime.getDayOfYear()) {
					solarDayOfYear = localTime.getDayOfYear();
					invokeLaterInDispatchThreadIfNeeded(() -> {
						sunrise.setText(ConsolidatedTime.getFormattedTime(AstronomicalTime.getSunrise(localTime, aes.getStationLatitudeDegrees(), aes.getStationLongitudeDegrees()), aes.getLocale()));
						sunriseEnd.setText(ConsolidatedTime.getFormattedTime(AstronomicalTime.getSunriseEnd(localTime, aes.getStationLatitudeDegrees(), aes.getStationLongitudeDegrees()), aes.getLocale()));
						eveningGoldenHour.setText(ConsolidatedTime.getFormattedTime(AstronomicalTime.getGoldenHour(localTime, aes.getStationLatitudeDegrees(), aes.getStationLongitudeDegrees()), aes.getLocale()));
						morningGoldenHour.setText(ConsolidatedTime.getFormattedTime(AstronomicalTime.getGoldenHourEnd(localTime, aes.getStationLatitudeDegrees(), aes.getStationLongitudeDegrees()), aes.getLocale()));
						solarNoon.setText(ConsolidatedTime.getFormattedTime(AstronomicalTime.getSolarNoon(localTime, aes.getStationLongitudeDegrees()), aes.getLocale()));
						sunsetStart.setText(ConsolidatedTime.getFormattedTime(AstronomicalTime.getSunriseStart(localTime, aes.getStationLatitudeDegrees(), aes.getStationLongitudeDegrees()), aes.getLocale()));
						sunset.setText(ConsolidatedTime.getFormattedTime(AstronomicalTime.getSunset(localTime, aes.getStationLatitudeDegrees(), aes.getStationLongitudeDegrees()), aes.getLocale()));
						dusk.setText(ConsolidatedTime.getFormattedTime(AstronomicalTime.getDusk(localTime, aes.getStationLatitudeDegrees(), aes.getStationLongitudeDegrees()), aes.getLocale()));
						nauticalDusk.setText(ConsolidatedTime.getFormattedTime(AstronomicalTime.getNauticalDusk(localTime, aes.getStationLatitudeDegrees(), aes.getStationLongitudeDegrees()), aes.getLocale()));
						night.setText(ConsolidatedTime.getFormattedTime(AstronomicalTime.getNight(localTime, aes.getStationLatitudeDegrees(), aes.getStationLongitudeDegrees()), aes.getLocale()));
						nadir.setText(ConsolidatedTime.getFormattedTime(AstronomicalTime.getNadir(localTime, aes.getStationLongitudeDegrees()), aes.getLocale()));
						nightEnd.setText(ConsolidatedTime.getFormattedTime(AstronomicalTime.getNightEnd(localTime, aes.getStationLatitudeDegrees(), aes.getStationLongitudeDegrees()), aes.getLocale()));
						nauticalDawn.setText(ConsolidatedTime.getFormattedTime(AstronomicalTime.getNauticalDawn(localTime, aes.getStationLatitudeDegrees(), aes.getStationLongitudeDegrees()), aes.getLocale()));
						dawn.setText(ConsolidatedTime.getFormattedTime(AstronomicalTime.getDawn(localTime, aes.getStationLatitudeDegrees(), aes.getStationLongitudeDegrees()), aes.getLocale()));
					});
				}
				
				final MoonCalc moon = new MoonCalc(localTime, aes.getStationLatitudeDegrees(), aes.getStationLongitudeDegrees());
				
				setLunarPhase(AbstractEnvironmentSensor.toDecimalFormat(moon.getPercentageOfLunation(), 8) + " %");
				setLunarState(moon.getMoonPhaseName());
				setLunarAzimuth(AbstractEnvironmentSensor.toDecimalFormat(Math.toDegrees(moon.getAzimuth()), 8) + " \u00B0");
				setLunarPhaseAngle(AbstractEnvironmentSensor.toDecimalFormat(Math.toDegrees(moon.getAngleOfLunation()), 8) + " \u00B0");
				setLunarDistance(AbstractEnvironmentSensor.toDecimalFormat(moon.getDistance(), 6) + " km");
				setLunarElevation(AbstractEnvironmentSensor.toDecimalFormat(Math.toDegrees(moon.getElevation()), 8) + " \u00B0");

				if (lunarDayOfYear != aes.getCurrentZonedDateTime().getDayOfYear()) {
					lunarDayOfYear = aes.getCurrentZonedDateTime().getDayOfYear();
					setMoonRise(moon.getRiseTime());
					setMoonSet(moon.getSetTime());
				}
				
				setYearlyRainDTG(ConsolidatedTime.getFormattedDateTimeGroup(ConsolidatedTime.getStartOfThisYearZonedDateTime(localTime), aes.getLocale()));
				setMonthlyRainDTG(ConsolidatedTime.getFormattedDateTimeGroup(ConsolidatedTime.getStartOfThisMonthZonedDateTime(localTime), aes.getLocale()));
				setWeeklyRainDTG(ConsolidatedTime.getFormattedDateTimeGroup(ConsolidatedTime.getStartOfThisWeekZonedDateTime(localTime, aes.getLocale()), aes.getLocale()));
				setDailyRainDTG(ConsolidatedTime.getFormattedDateTimeGroup(ConsolidatedTime.getStartOfThisDayZonedDateTime(localTime), aes.getLocale()));
			}
		};
		
		final PropertyChangeListener aprsListener = (PropertyChangeEvent event) -> {
			if (AprsProcessor.Event.OPERATE_APRS_ROTATOR.name().equals(event.getPropertyName())) {
				setAprsRotator((boolean) event.getNewValue());
			}
		};
		
		final PropertyChangeListener environmentSensorListener = (PropertyChangeEvent event) -> {
			if (AbstractEnvironmentSensor.Events.WIND_SPEED_UNITS_UPDATE.name().equals(event.getPropertyName())) {
				getWindSpeedsPanelFullSize().revalidate();
				getWindSpeedsPanelFoundation7InchDisplay().revalidate();
			}
			if (AbstractEnvironmentSensor.Events.TEMP_UNITS_UPDATE.name().equals(event.getPropertyName())) {
				getTempPanelFullSize().revalidate();
				getTempPanelFoundation7InchDisplay().revalidate();
			}
			if (AbstractEnvironmentSensor.Events.PRECIPITATION_UNITS_UPDATE.name().equals(event.getPropertyName())) {
				getPrecipitationPanel().revalidate();
				getPrecipitationPanelFoundation7InchDisplay().revalidate();
			}
			if (AbstractEnvironmentSensor.Events.LAST_PEAK_RAIN_RATE_MM_PER_HR.name().equals(event.getPropertyName())) {
				final MeasurementDataGroup mdg = (MeasurementDataGroup) event.getNewValue();
				final String dtg = DateTimeFormatter.ofPattern("yyyy-MM-dd kk:mm")
						.format(ZonedDateTime.ofInstant(mdg.getZdt().toInstant(), aes.getZoneId()));
				setHourlyRainRate(aes.getRainStringFromMillimeters(aes.getRainRateMillimetersPerHour()));
				setPeakRainRate(aes.getRainStringFromMillimeters(mdg.getMagnitude()) + "@" + dtg);
			}
			if (AbstractEnvironmentSensor.Events.DATA_COMPLETE.name().equals(event.getPropertyName())) {
				final ZonedDateTime zdt = (ZonedDateTime) event.getNewValue();
				setHumidity(String.valueOf(aes.getExteriorHumidity()) + "%");
				setDewPoint(aes.getTempStringFromFahrenheit(aes.getDewPointFahrenheit(), 1));	
				setBarometricPressureAbsolute(aes.getPressureStringFromHPa(aes.getBarometricPressureAbsoluteHPA(), 2));         
				setBarometricPressureStation(aes.getPressureStringFromInchesHg(aes.getStationPressureInHg(), 2));
				setBarometricPressureSeaLevel(aes.getPressureStringFromHPa(aes.getSeaLevelPressureMillibars(), 2));
				setBarometricPressureAltitude(aes.getElevationStringFromFeet(aes.getPressureAltitudeFeet(), 1));
				setAltimeterPressure(aes.getPressureStringFromInchesHg(aes.getAltimeterPressureInHg(), 2));
				set24HourRain(aes.getRainStringFromMillimeters(aes.getRainfallMillimetersLastHour()));
				set24HourRainDTG(ConsolidatedTime.getFormattedDateTimeGroup(ConsolidatedTime.get24HoursBeforeNowZonedDateTime(zdt), aes.getLocale()));
				setSolarIrradiance(AbstractEnvironmentSensor.toDecimalFormat(aes.getLuminosityWM2(), 2) + "  lx/m\u00B2");
				setWindSpeedGusting(aes.getWindSpeedStringFromMetersPerSecond(aes.getGustingWindSpeed(SpeedUnit.MPS), 2));
				setWindSpeedCurrent(aes.getWindSpeedStringFromMetersPerSecond(aes.getCurrentWindSpeed(SpeedUnit.MPS), 2));
				setWindDirection(aes.getWindDirectionTrue());
				setHeatIndex(aes.getTempStringFromFahrenheit(aes.getHeatIndexFahrenheit(), 1));
				final String heatIndexLabel = "Heat Level " + lblHeatIndexFlag.getText() + ": " + lblHeatIndexFlag.getToolTipText();
				dynamicToolTipUpdate(lblHeatIndexDecorated, heatIndexLabel);
				lblHeatIndexFlag.setHeatIndex(aes.getHeatIndexFahrenheit());
				lblHeatIndexDecorated.setBackground(lblHeatIndexFlag.getBackground());
				lblHeatIndexDecorated.setBorder(BorderFactory.createLineBorder(Color.GRAY));
				lblHeatIndexDecorated.setText(aes.getTempStringFromFahrenheit(aes.getHeatIndexFahrenheit(), 1));
				setWindSpeedMaxDaily(aes.getWindSpeedStringFromMetersPerSecond(aes.getMaxDailyWindSpeed(SpeedUnit.MPS), 2));
				setUVIndex(String.valueOf(aes.getCurrentUvIndex()));
				setUVIndexFlag(Color.BLACK, UVIndexReader.getUvIndexWarningColor(aes.getCurrentUvIndex()), UVIndexReader.getUvIndexWarningText(aes.getCurrentUvIndex()) + " UV Risk");
				setUVARadText(AbstractEnvironmentSensor.toDecimalFormat(aes.getCurrentUvLevel(), 2) + "  \u00B5W/m\u00B2");
				setMonthlyRain(aes.getRainStringFromMillimeters(aes.getMonthlyRainMillimeters()));
				setDailyRain(aes.getRainStringFromMillimeters(aes.getDailyRainMillimeters()));
				setWeeklyRain(aes.getRainStringFromMillimeters(aes.getWeeklyRainMillimeters()));
				setYearlyRain(aes.getRainStringFromMillimeters(aes.getYearlyRainMillimeters()));
				setCurrentTemp(aes.getTempStringFromCelsius(aes.getTempExteriorCelsius(), 1));
				if (aes.getTempExteriorFahrenheit() >= -50 && aes.getTempExteriorFahrenheit() <= 50) {
					setWindChill(aes.getTempStringFromFahrenheit(aes.getWindChillDegreesFahrenheit(), 1));
				} else {
					setWindChill("OUT OF RANGE");
				}
			}
			if (AbstractEnvironmentSensor.Events.CONNECTED.name().equals(event.getPropertyName())) {
				setTitle(getConnectedTitleString((boolean) event.getNewValue()));
			}
			if (AbstractEnvironmentSensor.Events.THIS_24H_LOW_TEMP.name().equals(event.getPropertyName())) {
				final MeasurementDataGroup dailyLowTempCelsius = (MeasurementDataGroup) event.getNewValue();
				setDailyLowText(aes.getTempStringFromCelsius(dailyLowTempCelsius.getMagnitude(), 1) + "  " +
					dailyLowTempCelsius.getZdt().format(DateTimeFormatter.ofPattern("MM/dd@kkmm", aes.getLocale())));
			}
			if (AbstractEnvironmentSensor.Events.THIS_24H_HIGH_TEMP.name().equals(event.getPropertyName())) {
				final MeasurementDataGroup dailyHighTempCelsius = (MeasurementDataGroup) event.getNewValue();
				setDailyHighText(aes.getTempStringFromCelsius(dailyHighTempCelsius.getMagnitude(), 1) + "  " +
					dailyHighTempCelsius.getZdt().format(DateTimeFormatter.ofPattern("MM/dd@kkmm", aes.getLocale())));
			}
			if (AbstractEnvironmentSensor.Events.TEMP_SAMPLE_TIME_SPAN.name().equals(event.getPropertyName())) {
				setTimeSpanOfHighTempSamples((long) event.getNewValue());
				setTimeSpanOfLowTempSamples((long) event.getNewValue());
			}
		};

		final PropertyChangeListener airNowApiListener = (PropertyChangeEvent event) -> {
			if (AirNowAPI.Event.AQI_OZONE.name().equals(event.getPropertyName())) {
				invokeLaterInDispatchThreadIfNeeded(() -> {
					lblAQIOzoneFlag.setBackground(AirNowAPI.getBackground((int) event.getNewValue()));
					lblAQIOzoneFlag.setForeground(AirNowAPI.getForeground((int) event.getNewValue()));
					lblAQIOzoneFlag.setToolTipText(AirNowAPI.getToolTipText(AirNowAPI.Event.AQI_OZONE, (int) event.getNewValue()));
					lblAQIOzoneFlag.setText("Ozone : AQI = " + (int) event.getNewValue());
					lblAQIOzoneFlagDecorated.setBackground(AirNowAPI.getBackground((int) event.getNewValue()));
					lblAQIOzoneFlagDecorated.setForeground(AirNowAPI.getForeground((int) event.getNewValue()));
					if ((int) event.getNewValue() == -1) {
						lblAQIOzoneFlagDecorated.setToolTipText(AirNowAPI.OZONE_SERVICE_UNAVAILABLE_STATEMENT);
						lblAQIOzoneFlagDecorated.setText("Ozone : AQI - UNAVAILABLE");
					} else {
						lblAQIOzoneFlagDecorated.setText("Ozone : AQI = " + (int) event.getNewValue());
						lblAQIOzoneFlagDecorated.setToolTipText(AirNowAPI.getToolTipText(AirNowAPI.Event.AQI_OZONE, (int) event.getNewValue()));
					}
				});
			}
			if (AirNowAPI.Event.FAIL_AQI_OZONE.name().equals(event.getPropertyName())) {
				invokeLaterInDispatchThreadIfNeeded(() -> {
					lblAQIOzoneFlagDecorated.setBackground(AirNowAPI.getBackground(-1));
					lblAQIOzoneFlagDecorated.setForeground(AirNowAPI.getForeground(-1));
					lblAQIOzoneFlagDecorated.setToolTipText(AirNowAPI.getToolTipText(AirNowAPI.Event.AQI_OZONE, -1));
					lblAQIOzoneFlagDecorated.setText("Ozone : AQI - FAIL");
				});
			}
			if (AirNowAPI.Event.AQI_PARTICLE_POLLUTION_10_MICRONS.name().equals(event.getPropertyName())) {
				invokeLaterInDispatchThreadIfNeeded(() -> {
					lblAQIPPM10Flag.setBackground(AirNowAPI.getBackground((int) event.getNewValue()));
					lblAQIPPM10Flag.setForeground(AirNowAPI.getForeground((int) event.getNewValue()));
					lblAQIPPM10Flag.setToolTipText(AirNowAPI.getToolTipText(AirNowAPI.Event.AQI_PARTICLE_POLLUTION_10_MICRONS, (int) event.getNewValue()));
					lblAQIPPM10Flag.setText("Particulate Pollution > 10 \u03BCM : AQI = " + (int) event.getNewValue());
					lblAQIPPM10FlagDecorated.setBackground(AirNowAPI.getBackground((int) event.getNewValue()));
					lblAQIPPM10FlagDecorated.setForeground(AirNowAPI.getForeground((int) event.getNewValue()));
					lblAQIPPM10FlagDecorated.setToolTipText(AirNowAPI.getToolTipText(AirNowAPI.Event.AQI_PARTICLE_POLLUTION_10_MICRONS, (int) event.getNewValue()));
					lblAQIPPM10FlagDecorated.setText("Particle > 10 \u03BCM : AQI = " + (int) event.getNewValue());
				});
			}
			if (AirNowAPI.Event.FAIL_AQI_PPM_10.name().equals(event.getPropertyName())) {
				invokeLaterInDispatchThreadIfNeeded(() -> {
					lblAQIPPM10FlagDecorated.setBackground(AirNowAPI.getBackground(-1));
					lblAQIPPM10FlagDecorated.setForeground(AirNowAPI.getForeground(-1));
					lblAQIPPM10FlagDecorated.setToolTipText(AirNowAPI.getToolTipText(AirNowAPI.Event.AQI_PARTICLE_POLLUTION_10_MICRONS, -1));
					lblAQIPPM10FlagDecorated.setText("Particle > 10 \u03BCM : AQI - FAIL");
				});
			}
			if (AirNowAPI.Event.AQI_PARTICLE_POLLUTION_2_5_MICRONS.name().equals(event.getPropertyName())) {
				invokeLaterInDispatchThreadIfNeeded(() -> {
					lblAQIPPM25Flag.setBackground(AirNowAPI.getBackground((int) event.getNewValue()));
					lblAQIPPM25Flag.setForeground(AirNowAPI.getBackground((int) event.getNewValue()));
					lblAQIPPM25Flag.setToolTipText(AirNowAPI.getToolTipText(AirNowAPI.Event.AQI_PARTICLE_POLLUTION_2_5_MICRONS, (int) event.getNewValue()));
					lblAQIPPM25Flag.setText("Particulate Pollution > 2.5 \u03BCM : AQI = " + (int) event.getNewValue());
					lblAQIPPM25FlagDecorated.setBackground(AirNowAPI.getBackground((int) event.getNewValue()));
					lblAQIPPM25FlagDecorated.setForeground(AirNowAPI.getForeground((int) event.getNewValue()));
					lblAQIPPM25FlagDecorated.setToolTipText(AirNowAPI.getToolTipText(AirNowAPI.Event.AQI_PARTICLE_POLLUTION_2_5_MICRONS, (int) event.getNewValue()));
					lblAQIPPM25FlagDecorated.setText("Particle > 2.5 \u03BCM : AQI = " + (int) event.getNewValue());
				});
			}
			if (AirNowAPI.Event.FAIL_AQI_PPM_2_5.name().equals(event.getPropertyName())) {
				invokeLaterInDispatchThreadIfNeeded(() -> {
					lblAQIPPM25FlagDecorated.setBackground(AirNowAPI.getBackground(-1));
					lblAQIPPM25FlagDecorated.setForeground(AirNowAPI.getForeground(-1));
					lblAQIPPM25FlagDecorated.setToolTipText(AirNowAPI.getToolTipText(AirNowAPI.Event.AQI_PARTICLE_POLLUTION_2_5_MICRONS, -1));
					lblAQIPPM25FlagDecorated.setText("Particle > 2.5 \u03BCM : AQI - FAIL");
				});
			}
			if (AirNowAPI.Event.CURRENT_AIR_QUALITY_DTG.name().equals(event.getPropertyName())) {
				invokeLaterInDispatchThreadIfNeeded(() -> {
					currentAirQuality = (String) event.getNewValue();
					setLocationAtDtgText();
				});
			}
			if (AirNowAPI.Event.LOCATION.name().equals(event.getPropertyName())) {
				invokeLaterInDispatchThreadIfNeeded(() -> {
					location = (String) event.getNewValue();
					setLocationAtDtgText();
				});
			}
			if (AirNowAPI.Event.AGENCY.name().equals(event.getPropertyName())) {
				invokeLaterInDispatchThreadIfNeeded(() -> {
					agency = (String) event.getNewValue();
					setLastUpdateFromAgencyText();
				});
			}
			if (AirNowAPI.Event.LAST_UPDATE_DTG.name().equals(event.getPropertyName())) {
				invokeLaterInDispatchThreadIfNeeded(() -> {
					lastUpdate = (String) event.getNewValue();
					setLastUpdateFromAgencyText();
				});
			}
		};

		final PropertyChangeListener bgtsListener = (PropertyChangeEvent event) -> {
			if (TempSensorClient.CH1_TEMP.equals(event.getPropertyName())) {
				setBGTSTempFahrenheit((double) event.getNewValue());
			}				
		};

		final PropertyChangeListener airQualitySensorListener = (PropertyChangeEvent event) -> {
			if (AirQualitySensorClient.ECO2.equals(event.getPropertyName())) {
				setECO2((int) event.getNewValue());
			}
			if (AirQualitySensorClient.TVOC.equals(event.getPropertyName())) {
				setTVOC((int) event.getNewValue());
			}
		};

		final PropertyChangeListener geigerCounterListener = (PropertyChangeEvent event) -> {
			if (GeigerCounterClient.RADIATION_CPM.equals(event.getPropertyName())) {
				setCPM((int) event.getNewValue());
			}
			if (GeigerCounterClient.GAMMA_RADIATION.equals(event.getPropertyName())) {
				setGammaRad((double) event.getNewValue());
			}
			if (GeigerCounterClient.BETA_RADIATION.equals(event.getPropertyName())) {
				setBetaRad((double) event.getNewValue());
			}
			if (GeigerCounterClient.ALPHA_RADIATION.equals(event.getPropertyName())) {
				setAlphaRad((double) event.getNewValue());
			}
		};
		
		final PropertyChangeListener nersListener = (PropertyChangeEvent event) -> {				
			if (NetworkEarthRotationService.Event.LOD_OFFSET_SECONDS.name().equals(event.getPropertyName())) {
				setLodExcessSeconds((Double) event.getNewValue());
			}
		};
		
		aes.addPropertyChangeListener(environmentSensorListener);
		airNowAPI.addPropertyChangeListener(airNowApiListener);
		bgts.getPropertyChangeSupport().addPropertyChangeListener(bgtsListener);
		gc.getPropertyChangeSupport().addPropertyChangeListener(geigerCounterListener);
		aqs.getPropertyChangeSupport().addPropertyChangeListener(airQualitySensorListener);
		ners.getPropertyChangeSupport().addPropertyChangeListener(nersListener);
		aprsProc.getAPRSPropertyChangeSupport().addPropertyChangeListener(aprsListener);
		consolidatedTime.addPropertyChangeListener(consolidatedTimeListener);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent event) {
				if (WindowEvent.WINDOW_CLOSING == event.getID()) {
					aes.removePropertyChangeListener(environmentSensorListener);
					airNowAPI.removePropertyChangeListener(airNowApiListener);
					bgts.getPropertyChangeSupport().removePropertyChangeListener(bgtsListener);
					ners.getPropertyChangeSupport().removePropertyChangeListener(nersListener);
				}
			}
		});
	}

	private void setBGTSTempFahrenheit(double bgts) {
		if (aes.getExteriorHumidity() >= 0 && aes.getTempExteriorCelsius() > -255D) { 
			final double wbt = Meteorology.findWetBulbTemperatureCelsius(aes.getExteriorHumidity(), aes.getTempExteriorCelsius());
			final double wbgt = Meteorology.findWetBulbGlobeTemperature(wbt, Meteorology.convertFahrenheitToCelsius(bgts), aes.getTempExteriorCelsius());
			setWetBulbTempText(aes.getTempStringFromCelsius(wbt, 1));
			setWetBulbGlobeTempText(aes.getTempStringFromCelsius(wbgt, 1));
			setBlackGlobeTempText(aes.getTempStringFromFahrenheit(bgts, 1));
			final String wbgtLabel = "Heat Level " + lblWetBulbGlobeTempFlag.getText() + ": " + lblWetBulbGlobeTempFlag.getToolTipText();
			dynamicToolTipUpdate(lblWetBulbGlobeTempDecorated, wbgtLabel);
			lblWetBulbGlobeTempFlag.set(WetBulbGlobeTempLabel.Region.R1, Meteorology.convertCelsiusToFahrenheit(wbgt));
			lblWetBulbGlobeTempDecorated.setBackground(lblWetBulbGlobeTempFlag.getBackground());
			lblWetBulbGlobeTempDecorated.setBorder(BorderFactory.createLineBorder(Color.GRAY));
			lblWetBulbGlobeTempDecorated.setText(aes.getTempStringFromCelsius(wbgt, 1));
		} else {
			setWetBulbTempText("");
			setWetBulbGlobeTempText("");
			setBlackGlobeTempText("");
			dynamicToolTipUpdate(lblWetBulbGlobeTempDecorated, "OUT OF RNG");
			lblWetBulbGlobeTempFlag.set(WetBulbGlobeTempLabel.Region.R1, -255D);
			lblWetBulbGlobeTempDecorated.setBackground(lblWetBulbGlobeTempFlag.getBackground());
			lblWetBulbGlobeTempDecorated.setBorder(BorderFactory.createEmptyBorder());
			lblWetBulbGlobeTempDecorated.setText("");
		}
	}
	
	private void setAprsRotator(boolean toolTipCycle) {
		if (toolTipCycle) {
			if (!toolTipRotatorRunning.get()) { // Do not run if already running.
				executor = Executors.newSingleThreadScheduledExecutor();
				executor.execute(new APRSToolTipRotator());
				toolTipRotatorRunning.set(true);
			}
		} else {
			if (executor != null) {
				try {
					executor.shutdown();
					executor.awaitTermination(20, TimeUnit.SECONDS);
					toolTipRotatorRunning.set(false);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}
	}

	public EventPanel getEventPanel() {
		return eventPanel;
	}

	public JButton getConfigGPSButton() {
		return configGPSButton;
	}

	public JButton getTimeZoneButton() {
		return configTimeZoneButton;
	}

	public JButton getConfigSensorButton() {
		return configSensorButton;
	}
	
	public JButton getConfigAPRSButton() {
		return configAPRSButton;
	}

	public JButton getConfigSWPButton() {
		return configSWPButton;
	}

	public JButton getExitButton() {
		return exitButton;
	}

	public JPanel getGuiPanel() {
		return guiPanel;
	}

	public DateTimeServiceComponent getDateTimeService() {
		return dts;
	}
	
	private void initComponents() {
		guiPanel = new JPanel();
		toolBar = new JToolBar();
		
		setVisible(false);
		setTitle(getConnectedTitleString(aes.isConnected()));
		setAlwaysOnTop(false);
		setName("Local Environemnt Sensors");

		lblCPM = new JLabel();
		lblGammaRad = new JLabel();
		lblGammaRadDecorated = new JLabel();
		lblBetaRad = new JLabel();
		lblBetaRadDecorated = new JLabel();
		lblAlphaRad = new JLabel();
		lblAlphaRadDecorated = new JLabel();	
		
		exitButton = new JButton();
		configGPSButton = new JButton();
		configSensorButton = new JButton();
		configAPRSButton = new JButton();
		configSWPButton = new JButton();
		configTimeZoneButton = new JButton();

		lblAQIPPM25 = new JLabel();
		lblAQIPPM25Flag = new JLabel();
		lblAQIPPM10Flag = new JLabel();
		lblAQIPPM10 = new JLabel();
		lblAQIOzoneFlag = new JLabel();
		lblAQIOzone = new JLabel();
		lblECO2 = new JLabel();
		lblECO2Flag = new JLabel();
		lblECO2FlagDecorated = new JLabel();
		lblTVOC = new JLabel();
		lblTVOCFlag = new JLabel();
		lblTVOCFlagDecorated = new JLabel();
		lblLastUpdateFromAgency = new JLabel();
		lblLocAtDtg = new JLabel();

		lblAQIPPM25FlagDecorated = new JLabel();
		lblAQIPPM10FlagDecorated = new JLabel();
		lblAQIOzoneFlagDecorated = new JLabel();

		lblBaroPressAltitude = new JLabel();		
		lblBaroPressAbs = new JLabel();
		lblBaroPressStation = new JLabel();
		lblAltimeterPressure = new JLabel();
		lblBaroPressSeaLevel = new JLabel();
		
		lblDryBulbTemp = new JLabel();
		lblWetBulbTemp = new JLabel();
		lblDailyHigh = new JLabel();
		lblDailyLow = new JLabel();
		lblBlackGlobeTemp = new JLabel();
		lblWindChill = new JLabel();
		
		lblHeatIndex = new JLabel();
		lblHeatIndexDecorated = new JLabel();
		lblHeatIndexFlag = new HeatIndexTempLabel();
				
		lblWetBulbGlobeTemp = new JLabel();
		lblWetBulbGlobeTempDecorated = new JLabel();
		lblWetBulbGlobeTempFlag = new WetBulbGlobeTempLabel();
		
		lbl24HrHighLabel = new JLabel();
		lbl24HrLowLabel = new JLabel();
		
		lblAprsDTG = new JLabel();
		lblAprsTPlus = new JLabel();
		
		lblHumidity = new JLabel();
		lblDewPoint = new JLabel();
		lblDewPointDecorated = new JLabel();

		compassRosePanel = new CompassRosePanel();

		lblWindSpeedMaxDaily = new JLabel();
		lblWindSpeedGusting = new JLabel();
		lblWindSpeedCurrent = new JLabel();

		lblLunarState = new JLabel();
		lblLunarPhase = new JLabel();
		lblLunarPhaseAngle = new JLabel();
		lblLunarDistance = new JLabel();
		lblLunarAzimuth = new JLabel();
		lblLunarElevation = new JLabel();
		lblMoonRise = new JLabel();
		lblMoonSet = new JLabel();

		lblDailyRain = new JLabel();
		lbl24HourRain = new JLabel();
		lblMonthlyRain = new JLabel();
		lblRainRate = new JLabel();
		lblYearlyRain = new JLabel();
		lblWeeklyRain = new JLabel();
		lblPeakRainRate = new JLabel();

		lblDailyRainAsOfDTG = new JLabel();
		lbl24HourRainAsOfDTG = new JLabel();
		lblMonthlyRainAsOfDTG = new JLabel();
		lblWeeklyRainAsOfDTG = new JLabel();
		lblYearlyRainAsOfDTG = new JLabel();

		if (displaySize == DisplaySize.Size_1024x768) {
			moonShape = new MoonShape(aes.getCurrentZonedDateTime());
		}

		solarTimingPanel = new SolarTimingPanel(aes.getStationLatitudeDegrees(), aes.getStationLongitudeDegrees());

		lblLodExcessSeconds = new JLabel();
		lblSolarAzimuth = new JLabel();
		lblSolarElevation = new JLabel();
		lblUVARad = new JLabel();
		lblUVBRad = new JLabel();
		lblUVIndexFlag = new JLabel();
		lblUVIndex = new JLabel();
		lblSolarIrradiance = new JLabel();
	}

	private void configureComponents() {
		invokeLaterInDispatchThreadIfNeeded(() -> {
			
			toolBar.setVisible(true);
			toolBar.setRollover(true);
			toolBar.setFloatable(false);
			toolBar.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED, Color.LIGHT_GRAY, Color.GRAY));

			exitButton.setText("EXIT");
			exitButton.setPreferredSize(DEFAULT_BUTTON_DIM);
			exitButton.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));
			exitButton.setForeground(Color.BLUE);
			exitButton.setRolloverEnabled(true);
			exitButton.setFocusable(false);
			exitButton.setMultiClickThreshhold(50L);
			exitButton.setToolTipText("Exit and return to system");

			configGPSButton.setText("GPS Config");
			configGPSButton.setPreferredSize(DEFAULT_BUTTON_DIM);
			configGPSButton.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));
			configGPSButton.setForeground(Color.BLUE);
			configGPSButton.setRolloverEnabled(true);
			configGPSButton.setFocusable(false);
			configGPSButton.setMultiClickThreshhold(50L);
			configGPSButton.setToolTipText("Configure GPS");

			configAPRSButton.setText("APRS Config");
			configAPRSButton.setPreferredSize(DEFAULT_BUTTON_DIM);
			configAPRSButton.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));
			configAPRSButton.setForeground(Color.BLUE);
			configAPRSButton.setRolloverEnabled(true);
			configAPRSButton.setFocusable(false);
			configAPRSButton.setMultiClickThreshhold(50L);
			configAPRSButton.setToolTipText("Configure APRS");

			configSensorButton.setText("Sensor Config");
			configSensorButton.setPreferredSize(DEFAULT_BUTTON_DIM);
			configSensorButton.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));
			configSensorButton.setForeground(Color.BLUE);
			configSensorButton.setRolloverEnabled(true);
			configSensorButton.setFocusable(false);
			configSensorButton.setMultiClickThreshhold(50L);
			configSensorButton.setToolTipText("Configure Sensors");
			
			configSWPButton.setText("Space Weather Config");
			configSWPButton.setPreferredSize(DEFAULT_BUTTON_DIM);
			configSWPButton.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));
			configSWPButton.setForeground(Color.BLUE);
			configSWPButton.setRolloverEnabled(true);
			configSWPButton.setFocusable(false);
			configSWPButton.setMultiClickThreshhold(50L);
			configSWPButton.setToolTipText("Configure Space Weather Panel");

			configTimeZoneButton.setText("Time Zone & Locality Config");
			configTimeZoneButton.setPreferredSize(DEFAULT_BUTTON_DIM);
			configTimeZoneButton.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));
			configTimeZoneButton.setForeground(Color.BLUE);
			configTimeZoneButton.setRolloverEnabled(true);
			configTimeZoneButton.setFocusable(false);
			configTimeZoneButton.setMultiClickThreshhold(50L);
			configTimeZoneButton.setToolTipText("Configure Time Zone and Locale");
			
			lblAQIPPM10.setText("PP >= 10 \u03BCM");
			lblAQIPPM10.setHorizontalAlignment(SwingConstants.LEFT);

			lblAQIPPM10Flag.setBackground(Color.GRAY);
			lblAQIPPM10Flag.setFont(new Font(DEFAULT_FONT, Font.BOLD, 12));
			lblAQIPPM10Flag.setHorizontalAlignment(SwingConstants.CENTER);
			lblAQIPPM10Flag.setText("NO DATA");
			lblAQIPPM10Flag.setToolTipText("");
			lblAQIPPM10Flag.setBorder(BorderFactory.createLineBorder(Color.GRAY));
			lblAQIPPM10Flag.setOpaque(true);

			lblAQIPPM25.setText("PP >= 2.5 \u03BCM");
			lblAQIPPM25.setHorizontalAlignment(SwingConstants.LEFT);

			lblAQIPPM25Flag.setBackground(Color.GRAY);
			lblAQIPPM25Flag.setFont(new Font(DEFAULT_FONT, Font.BOLD, 12));
			lblAQIPPM25Flag.setHorizontalAlignment(SwingConstants.CENTER);
			lblAQIPPM25Flag.setText("NO DATA");
			lblAQIPPM25Flag.setToolTipText("");
			lblAQIPPM25Flag.setBorder(BorderFactory.createLineBorder(Color.GRAY));
			lblAQIPPM25Flag.setOpaque(true);

			lblAQIOzone.setText("Ozone AQI");
			lblAQIOzone.setHorizontalAlignment(SwingConstants.LEFT);

			lblTVOC.setText("Total VOC");
			lblTVOC.setHorizontalAlignment(SwingConstants.LEFT);
			
			lblTVOCFlag.setBackground(Color.LIGHT_GRAY);
			lblTVOCFlag.setFont(new Font(DEFAULT_FONT, Font.BOLD, 12));
			lblTVOCFlag.setHorizontalAlignment(SwingConstants.CENTER);
			lblTVOCFlag.setText("NO DATA");
			lblTVOCFlag.setToolTipText("");
			lblTVOCFlag.setBorder(BorderFactory.createLineBorder(Color.GRAY));
			lblTVOCFlag.setOpaque(true);
			
			lblTVOCFlagDecorated.setBackground(Color.LIGHT_GRAY);
			lblTVOCFlagDecorated.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));
			lblTVOCFlagDecorated.setHorizontalAlignment(SwingConstants.CENTER);
			lblTVOCFlagDecorated.setText("NO DATA");
			lblTVOCFlagDecorated.setToolTipText("");
			lblTVOCFlagDecorated.setBorder(BorderFactory.createLineBorder(Color.GRAY));
			lblTVOCFlagDecorated.setOpaque(true);
			
			lblECO2.setText("Total CO2");
			lblECO2.setHorizontalAlignment(SwingConstants.LEFT);
			
			lblECO2Flag.setBackground(Color.LIGHT_GRAY);
			lblECO2Flag.setFont(new Font(DEFAULT_FONT, Font.BOLD, 12));
			lblECO2Flag.setHorizontalAlignment(SwingConstants.CENTER);
			lblECO2Flag.setText("NO DATA");
			lblECO2Flag.setToolTipText("");
			lblECO2Flag.setBorder(BorderFactory.createLineBorder(Color.GRAY));
			lblECO2Flag.setOpaque(true);
			
			lblECO2FlagDecorated.setBackground(Color.LIGHT_GRAY);
			lblECO2FlagDecorated.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));
			lblECO2FlagDecorated.setHorizontalAlignment(SwingConstants.CENTER);
			lblECO2FlagDecorated.setText("NO DATA");
			lblECO2FlagDecorated.setToolTipText("");
			lblECO2FlagDecorated.setBorder(BorderFactory.createLineBorder(Color.GRAY));
			lblECO2FlagDecorated.setOpaque(true);

			lblAQIOzoneFlagDecorated.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));
			lblAQIOzoneFlagDecorated.setHorizontalAlignment(SwingConstants.CENTER);
			lblAQIOzoneFlagDecorated.setBorder(BorderFactory.createLineBorder(Color.GRAY));
			lblAQIOzoneFlagDecorated.setOpaque(true);
			
			lblAQIPPM25FlagDecorated.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));
			lblAQIPPM25FlagDecorated.setHorizontalAlignment(SwingConstants.CENTER);
			lblAQIPPM25FlagDecorated.setBorder(BorderFactory.createLineBorder(Color.GRAY));
			lblAQIPPM25FlagDecorated.setOpaque(true);
			
			lblAQIPPM10FlagDecorated.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));
			lblAQIPPM10FlagDecorated.setHorizontalAlignment(SwingConstants.CENTER);
			lblAQIPPM10FlagDecorated.setBorder(BorderFactory.createLineBorder(Color.GRAY));
			lblAQIPPM10FlagDecorated.setOpaque(true);
			
			lblLastUpdateFromAgency.setHorizontalAlignment(SwingConstants.LEFT);

			lblLocAtDtg.setHorizontalAlignment(SwingConstants.LEFT);

			lblWetBulbGlobeTempDecorated.setHorizontalAlignment(SwingConstants.CENTER);
			lblWetBulbGlobeTempDecorated.setHorizontalTextPosition(SwingConstants.CENTER);
			lblWetBulbGlobeTempDecorated.setOpaque(true);
			
			lblHeatIndexDecorated.setHorizontalAlignment(SwingConstants.CENTER);
			lblHeatIndexDecorated.setHorizontalTextPosition(SwingConstants.CENTER);
			lblHeatIndexDecorated.setOpaque(true);
			
			lblUVARad.setToolTipText("");
			lblUVARad.setHorizontalAlignment(SwingConstants.LEFT);

			lblUVBRad.setToolTipText("");
			lblUVBRad.setHorizontalAlignment(SwingConstants.LEFT);

			lblUVIndexFlag.setBackground(Color.LIGHT_GRAY);
			lblUVIndexFlag.setForeground(Color.BLACK);
			lblUVIndexFlag.setFont(new Font(DEFAULT_FONT, Font.BOLD, 18));
			lblUVIndexFlag.setHorizontalAlignment(SwingConstants.CENTER);
			lblUVIndexFlag.setText("NO DATA");
			lblUVIndexFlag.setToolTipText("");
			lblUVIndexFlag.setBorder(BorderFactory.createLineBorder(Color.GRAY));
			lblUVIndexFlag.setOpaque(true);

			lblUVIndex.setToolTipText("");
			lblUVIndex.setHorizontalAlignment(SwingConstants.LEFT);

			lblSolarIrradiance.setToolTipText("");
			lblSolarIrradiance.setHorizontalAlignment(SwingConstants.LEFT);

			lblAltimeterPressure.setHorizontalAlignment(SwingConstants.CENTER);
			lblAltimeterPressure.setFont(new Font(DEFAULT_FONT, Font.BOLD, 14));
			lblAltimeterPressure.setToolTipText(Meteorology.getAltimeterDefinitionText());
			
			lblAltimeterPressure.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent me) {
					ToolTipManager.sharedInstance().setDismissDelay(TOOLTIP_DEFAULT_EXTENDED_DELAY);
				}
				@Override
				public void mouseExited(MouseEvent me) {
					ToolTipManager.sharedInstance().setDismissDelay(TOOLTIP_DEFAULT_DISMISS_TIMEOUT);
				}
			});

			lblBaroPressAltitude.setHorizontalAlignment(SwingConstants.CENTER);
			lblBaroPressAltitude.setFont(new Font(DEFAULT_FONT, Font.BOLD, 14));
			lblBaroPressAltitude.setToolTipText(Meteorology.getPressureAltitudeDefinitionText());
			lblBaroPressAltitude.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent me) {
					ToolTipManager.sharedInstance().setDismissDelay(TOOLTIP_DEFAULT_EXTENDED_DELAY);
				}
				@Override
				public void mouseExited(MouseEvent me) {
					ToolTipManager.sharedInstance().setDismissDelay(TOOLTIP_DEFAULT_DISMISS_TIMEOUT);
				}
			});

			lblBaroPressStation.setHorizontalAlignment(SwingConstants.CENTER);
			lblBaroPressStation.setFont(new Font(DEFAULT_FONT, Font.BOLD, 14));
			lblBaroPressStation.setToolTipText(Meteorology.getStationPressureDefinitionText());
			lblBaroPressStation.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent me) {
					ToolTipManager.sharedInstance().setDismissDelay(TOOLTIP_DEFAULT_EXTENDED_DELAY);
				}
				@Override
				public void mouseExited(MouseEvent me) {
					ToolTipManager.sharedInstance().setDismissDelay(TOOLTIP_DEFAULT_DISMISS_TIMEOUT);
				}
			});
			
			lblBaroPressSeaLevel.setHorizontalAlignment(SwingConstants.CENTER);
			lblBaroPressSeaLevel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 14));
			lblBaroPressSeaLevel.setToolTipText(Meteorology.getSeaLevelPressureDefinitionText());
			lblBaroPressSeaLevel.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent me) {
					ToolTipManager.sharedInstance().setDismissDelay(TOOLTIP_DEFAULT_EXTENDED_DELAY);
				}
				@Override
				public void mouseExited(MouseEvent me) {
					ToolTipManager.sharedInstance().setDismissDelay(TOOLTIP_DEFAULT_DISMISS_TIMEOUT);
				}
			});
			
			lblBaroPressAbs.setHorizontalAlignment(SwingConstants.CENTER);
			lblBaroPressAbs.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));
			lblBaroPressAbs.setToolTipText(Meteorology.getAbsolutePressureDefinitionText());
			lblBaroPressAbs.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent me) {
					ToolTipManager.sharedInstance().setDismissDelay(TOOLTIP_DEFAULT_EXTENDED_DELAY);
				}
				@Override
				public void mouseExited(MouseEvent me) {
					ToolTipManager.sharedInstance().setDismissDelay(TOOLTIP_DEFAULT_DISMISS_TIMEOUT);
				}
			});

			lblDewPoint.setHorizontalAlignment(SwingConstants.LEFT);
			lblDewPointDecorated.setHorizontalAlignment(SwingConstants.CENTER);
			lblDewPointDecorated.setFont(new Font(DEFAULT_FONT, Font.BOLD, 12));

			lblRainRate.setText("0.00");
			
			lbl24HourRain.setText("0.00");

			lblAprsDTG.setHorizontalAlignment(SwingConstants.LEFT);
			lblAprsDTG.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));
			lblAprsDTG.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent me) {
					ToolTipManager.sharedInstance().setDismissDelay(TOOLTIP_DEFAULT_EXTENDED_DELAY);
				}
				@Override
				public void mouseExited(MouseEvent me) {
					ToolTipManager.sharedInstance().setDismissDelay(TOOLTIP_DEFAULT_DISMISS_TIMEOUT);
				}
			});
			
			lblAprsTPlus.setHorizontalAlignment(SwingConstants.LEFT);
			lblAprsTPlus.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));
			
			lblHumidity.setHorizontalAlignment(SwingConstants.CENTER);

			lblHumidity.setFont(new Font(DEFAULT_FONT, Font.BOLD, 14));

			lblWindSpeedMaxDaily.setHorizontalAlignment(SwingConstants.LEFT);

			lblWindSpeedGusting.setHorizontalAlignment(SwingConstants.LEFT);

			lblWindSpeedCurrent.setHorizontalAlignment(SwingConstants.LEFT);
			
			setDailyHighText("CALCULATING");
			setDailyLowText("CALCULATING");
		});
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
	
	private String getConnectedTitleString(boolean connected) {
		if (connected) {
			return DEFAULT_DIALOG_TITLE + " --- Sensor Suite: " + aes.getDeviceManufacturer() + " "
					+ aes.getDeviceModel() + " --- Status: " + "CONNECTED";
		} else {
			return DEFAULT_DIALOG_TITLE + " --- Sensor Suite: " + aes.getDeviceManufacturer() + " "
					+ aes.getDeviceModel() + " --- Status: " + "WAITING FOR CONNECTION";
		}
	}

	public static BigDecimal truncateDecimal(double x, int numberofDecimals) {
		if (x > 0) {
			return new BigDecimal(String.valueOf(x)).setScale(numberofDecimals, RoundingMode.FLOOR);
		} else {
			return new BigDecimal(String.valueOf(x)).setScale(numberofDecimals, RoundingMode.CEILING);
		}
	}

	public void setTVOC(int tvoc) {
		invokeLaterInDispatchThreadIfNeeded(() -> {
			if (tvoc >= 0) {
				lblTVOC.setText("TVOC : " + tvoc + " PPB");
				lblTVOCFlagDecorated.setText("VOC:" + tvoc + " PPB");
			} else {
				lblTVOC.setText("NO DATA");
				lblTVOCFlagDecorated.setText("NO DATA");
			}
			setTVOCFlag(AirQualitySensorClient.getTVOCFlag(tvoc));
		});
	}
	
	public void setECO2(int eco2) {
		invokeLaterInDispatchThreadIfNeeded(() -> {
			if (eco2 >= 0) {
				lblECO2.setText("ECO2 : " + eco2 + " PPM");
				lblECO2FlagDecorated.setText("CO2:" + eco2 + " PPM");
			} else {
				lblECO2.setText("NO DATA");
				lblECO2FlagDecorated.setText("NO DATA");
			}
			setECO2Flag(AirQualitySensorClient.getECO2Flag(eco2));
		});
	}

	private void setLastUpdateFromAgencyText() {
		invokeLaterInDispatchThreadIfNeeded(() -> {
			if (lastUpdate != null && agency != null) {
				lblLastUpdateFromAgency.setText("Updated: " + lastUpdate + " by " + agency);
			}
		});
	}

	private void setLocationAtDtgText() {
		invokeLaterInDispatchThreadIfNeeded(() -> {
			if (location != null && currentAirQuality != null) {
				lblLocAtDtg.setText(location + " conditions at " + currentAirQuality);
			}
		});
	}
	
	private void setECO2Flag(JLabel jLabel) {
		invokeLaterInDispatchThreadIfNeeded(() -> {
			this.lblECO2Flag.setForeground(jLabel.getForeground());
			this.lblECO2Flag.setBackground(jLabel.getBackground());
			this.lblECO2Flag.setText(jLabel.getText());
			this.lblECO2FlagDecorated.setForeground(jLabel.getForeground());
			this.lblECO2FlagDecorated.setBackground(jLabel.getBackground());
		});
	}

	private void setTVOCFlag(JLabel jLabel) {
		invokeLaterInDispatchThreadIfNeeded(() -> {
			this.lblTVOCFlag.setForeground(jLabel.getForeground());
			this.lblTVOCFlag.setBackground(jLabel.getBackground());
			this.lblTVOCFlag.setText(jLabel.getText());
			this.lblTVOCFlagDecorated.setForeground(jLabel.getForeground());
			this.lblTVOCFlagDecorated.setBackground(jLabel.getBackground());
		});
	}
	
	public static String removeAllNonNumericCharacters(String s) {
		final StringBuilder sb = new StringBuilder();
		s.chars().mapToObj(c -> (char) c).filter(c -> Character.isDigit(c) || c == '.').forEach(sb::append);
		return sb.toString();
	}

	private void setLunarPhase(String text) {
		invokeLaterInDispatchThreadIfNeeded(() -> this.lblLunarPhase.setText(text));
		if (moonShape != null) {
			moonShape.setMoonPhase(Double.parseDouble(removeAllNonNumericCharacters(text)));
		}
	}

	private void setLunarPhaseAngle(String text) {
		invokeLaterInDispatchThreadIfNeeded(() -> this.lblLunarPhaseAngle.setText(text));
	}

	private void setLunarDistance(String text) {
		invokeLaterInDispatchThreadIfNeeded(() -> this.lblLunarDistance.setText(text));
	}

	private void setLunarElevation(String text) {
		invokeLaterInDispatchThreadIfNeeded(() -> this.lblLunarElevation.setText(text));
	}

	private void setLunarAzimuth(String text) {
		invokeLaterInDispatchThreadIfNeeded(() -> this.lblLunarAzimuth.setText(text));
	}

	private void setLunarState(String text) {
		invokeLaterInDispatchThreadIfNeeded(() -> this.lblLunarState.setText(text));
	}

	private void setMoonRise(ZonedDateTime utc) {
		final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu.MM.dd HH:mm z").withLocale(aes.getLocale());
		final ZonedDateTime local = utc.withZoneSameInstant(localTime.getZone());
		final String text = local.format(formatter);
		invokeLaterInDispatchThreadIfNeeded(() -> this.lblMoonRise.setText(text));
	}

	private void setMoonSet(ZonedDateTime utc) {
		final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu.MM.dd HH:mm z").withLocale(aes.getLocale());
		final ZonedDateTime local = utc.withZoneSameInstant(localTime.getZone());
		final String text = local.format(formatter);
		invokeLaterInDispatchThreadIfNeeded(() -> this.lblMoonSet.setText(text));
	}

	private void setWindDirection(int heading) {
		invokeLaterInDispatchThreadIfNeeded(() -> this.compassRosePanel.setHeading(heading));
	}

	public JPanel getSolarTimingPanelSingleColumn() {
		return solarTimingPanel.getSolarTimingPanelSingleColumn();
	}
	
	private void setWindSpeedCurrent(String speed) {
		invokeLaterInDispatchThreadIfNeeded(() -> this.lblWindSpeedCurrent.setText(speed));
	}

	private void setWindSpeedGusting(String speed) {
		invokeLaterInDispatchThreadIfNeeded(() -> this.lblWindSpeedGusting.setText(speed));
	}

	private void setWindSpeedMaxDaily(String speed) {
		invokeLaterInDispatchThreadIfNeeded(() -> this.lblWindSpeedMaxDaily.setText(speed));
	}

	private void setHourlyRainRate(String rate) {
		invokeLaterInDispatchThreadIfNeeded(() -> this.lblRainRate.setText(rate));
	}

	private void setPeakRainRate(String rate) {
		invokeLaterInDispatchThreadIfNeeded(() -> this.lblPeakRainRate.setText(rate));
	}
	
	private void setDailyRain(String val) {
		invokeLaterInDispatchThreadIfNeeded(() -> this.lblDailyRain.setText(val));
	}

	private void set24HourRain(String val) {
		invokeLaterInDispatchThreadIfNeeded(() -> this.lbl24HourRain.setText(val));
	}

	private void setWeeklyRain(String val) {
		invokeLaterInDispatchThreadIfNeeded(() -> this.lblWeeklyRain.setText(val));
	}

	private void setMonthlyRain(String val) {
		invokeLaterInDispatchThreadIfNeeded(() -> this.lblMonthlyRain.setText(val));
	}

	private void setYearlyRain(String val) {
		invokeLaterInDispatchThreadIfNeeded(() -> this.lblYearlyRain.setText(val));
	}

	private void setDailyRainDTG(String dtg) {
		invokeLaterInDispatchThreadIfNeeded(() -> this.lblDailyRainAsOfDTG.setText(dtg));
	}

	private void set24HourRainDTG(String dtg) {
		invokeLaterInDispatchThreadIfNeeded(() -> this.lbl24HourRainAsOfDTG.setText(dtg));
	}

	private void setWeeklyRainDTG(String dtg) {
		invokeLaterInDispatchThreadIfNeeded(() -> this.lblWeeklyRainAsOfDTG.setText(dtg));
	}

	private void setMonthlyRainDTG(String dtg) {
		invokeLaterInDispatchThreadIfNeeded(() -> this.lblMonthlyRainAsOfDTG.setText(dtg));
	}

	private void setYearlyRainDTG(String dtg) {
		invokeLaterInDispatchThreadIfNeeded(() -> this.lblYearlyRainAsOfDTG.setText(dtg));
	}

	private void setCurrentTemp(String text) {
		invokeLaterInDispatchThreadIfNeeded(() -> this.lblDryBulbTemp.setText(text));
	}

	private void setWetBulbTempText(String text) {
		invokeLaterInDispatchThreadIfNeeded(() -> this.lblWetBulbTemp.setText(text));
	}
	
	private void setWetBulbGlobeTempText(String text) {
		invokeLaterInDispatchThreadIfNeeded(() -> this.lblWetBulbGlobeTemp.setText(text));
	}

	private void setTimeSpanOfHighTempSamples(long hours) {
		invokeLaterInDispatchThreadIfNeeded(() -> this.lbl24HrHighLabel.setText(String.valueOf(hours) + "H High :"));
	}
	
	private void setTimeSpanOfLowTempSamples(long hours) {
		invokeLaterInDispatchThreadIfNeeded(() -> this.lbl24HrLowLabel.setText(String.valueOf(hours) + "H Low :"));
	}
	
	private void setDailyHighText(String text) {
		invokeLaterInDispatchThreadIfNeeded(() -> this.lblDailyHigh.setText(text));
	}

	private void setDailyLowText(String text) {
		invokeLaterInDispatchThreadIfNeeded(() -> this.lblDailyLow.setText(text));
	}

	private void setBlackGlobeTempText(String text) {
		invokeLaterInDispatchThreadIfNeeded(() -> this.lblBlackGlobeTemp.setText(text));
	}

	private void setWindChill(String text) {
		invokeLaterInDispatchThreadIfNeeded(() -> this.lblWindChill.setText(text));
	}

	private void setHeatIndex(String text) {
		invokeLaterInDispatchThreadIfNeeded(() -> this.lblHeatIndex.setText(text));
	}

	private void setDewPoint(String text) {
		invokeLaterInDispatchThreadIfNeeded(() -> {
			this.lblDewPoint.setText(text);
			this.lblDewPointDecorated.setText(
					text + " " + AbstractEnvironmentSensor.temperatureUnitSymbol.get(aes.getTemperatureUnits()));
		});
	}
	
	private void setCPM(int cpm) {
		invokeLaterInDispatchThreadIfNeeded(() -> {
			if (cpm >= 0) {
				this.lblCPM.setText(String.valueOf(cpm));
				this.lblGammaRadDecorated.setToolTipText(cpm + " cpm");
			} else {
				this.lblCPM.setText("");
				this.lblGammaRadDecorated.setToolTipText("");
			}
		});
	}
	
	private void setGammaRad(double uSvPerHr) {
		invokeLaterInDispatchThreadIfNeeded(() -> {
			if (uSvPerHr >= 0) {
				this.lblGammaRad.setText(AbstractEnvironmentSensor.toRadiationFormat(uSvPerHr, 4));
				this.lblGammaRadDecorated.setText(AbstractEnvironmentSensor.toRadiationFormat(uSvPerHr, 3));
			} else {
				this.lblGammaRad.setText("");
				this.lblGammaRadDecorated.setText("");
			}
		});
	}
	
	private void setBetaRad(double uSvPerHr) {
		invokeLaterInDispatchThreadIfNeeded(() -> {
			if (uSvPerHr >= 0) {
				this.lblBetaRad.setText(AbstractEnvironmentSensor.toRadiationFormat(uSvPerHr, 4));
				this.lblBetaRadDecorated.setText(AbstractEnvironmentSensor.toRadiationFormat(uSvPerHr, 3));
			} else {
				this.lblBetaRad.setText("");
				this.lblBetaRadDecorated.setText("");
			}
		});
	}
	
	private void setAlphaRad(double uSvPerHr) {
		invokeLaterInDispatchThreadIfNeeded(() -> {
			if (uSvPerHr >= 0) {
				this.lblAlphaRad.setText(AbstractEnvironmentSensor.toRadiationFormat(uSvPerHr, 4));
				this.lblAlphaRadDecorated.setText(AbstractEnvironmentSensor.toRadiationFormat(uSvPerHr, 3));
			} else {
				this.lblAlphaRad.setText("");
				this.lblAlphaRadDecorated.setText("");
			}
		});
	}
		
	private void setHumidity(String text) {
		invokeLaterInDispatchThreadIfNeeded(() -> {		
			this.lblHumidity.setText(text);
			this.lblHumidity.setToolTipText(AbstractEnvironmentSensor.toDecimalFormat(aes.getExteriorHumidityGM3(), 1) + "gm\u00B3");
		});
	}
	
	private void setBarometricPressureAbsolute(String text) {
		invokeLaterInDispatchThreadIfNeeded(() -> 
			this.lblBaroPressAbs.setText(text + "  " + AbstractEnvironmentSensor.pressureUnitSuffix.get(aes.getPressureUnits())));
	}
	
	private void setBarometricPressureStation(String text) {
		invokeLaterInDispatchThreadIfNeeded(() -> 
			this.lblBaroPressStation.setText(text + "  " + AbstractEnvironmentSensor.pressureUnitSuffix.get(aes.getPressureUnits())));
	}
	
	private void setBarometricPressureSeaLevel(String text) {
		invokeLaterInDispatchThreadIfNeeded(() -> 
			this.lblBaroPressSeaLevel.setText(text + "  " + AbstractEnvironmentSensor.pressureUnitSuffix.get(aes.getPressureUnits())));
	}
	
	private void setBarometricPressureAltitude(String text) {
		invokeLaterInDispatchThreadIfNeeded(() -> 
			this.lblBaroPressAltitude.setText(text + "  " + AbstractEnvironmentSensor.elevationUnitSuffix.get(aes.getElevationUnits())));
	}

	private void setAltimeterPressure(String text) {
		invokeLaterInDispatchThreadIfNeeded(() -> 
			this.lblAltimeterPressure.setText(text + "  " + AbstractEnvironmentSensor.pressureUnitSuffix.get(aes.getPressureUnits())));
	}

	private void setUVARadText(String text) {
		invokeLaterInDispatchThreadIfNeeded(() -> this.lblUVARad.setText(text));
	}

	private void setSolarIrradiance(String text) {
		invokeLaterInDispatchThreadIfNeeded(() -> this.lblSolarIrradiance.setText(text));
	}

	private void setSolarAzimuth(String text) {
		invokeLaterInDispatchThreadIfNeeded(() -> this.lblSolarAzimuth.setText(text));
	}

	private void setSolarElevation(String text) {
		invokeLaterInDispatchThreadIfNeeded(() -> this.lblSolarElevation.setText(text));
	}

	private void setLodExcessSeconds(double lodExcessSeconds) {
		final String s = String.format(aes.getLocale(), "%10.6E", lodExcessSeconds) + " s";
		invokeLaterInDispatchThreadIfNeeded(() -> this.lblLodExcessSeconds.setText(s));
	}

	private void setUVIndex(String text) {
		invokeLaterInDispatchThreadIfNeeded(() -> this.lblUVIndex.setText(text));
	}

	private void setUVIndexFlag(Color fg, Color bg, String text) {
		invokeLaterInDispatchThreadIfNeeded(() -> {
			this.lblUVIndexFlag.setForeground(fg);
			this.lblUVIndexFlag.setBackground(bg);
			this.lblUVIndexFlag.setText(text);
		});
	}

	private JPanel getBaroPressPanelFoundation7InchDisplay() {
		final JPanel panel = new JPanel();

		panel.setBorder(BorderFactory.createTitledBorder(null, "Barometer", TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION, new Font(DEFAULT_FONT, Font.BOLD, 12)));

		final GroupLayout layout = new GroupLayout(panel);

		panel.setLayout(layout);

		final JLabel lblBaroPressAltitudeLabel = new JLabel();
		final JLabel lblBaroPressAbsLabel = new JLabel();
		final JLabel lblBaroPressStationLabel = new JLabel();
		final JLabel lblAltimeterPressureLabel = new JLabel();
		final JLabel lblBaroPressSeaLevelLabel = new JLabel();
		
		lblBaroPressAbsLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblBaroPressAbsLabel.setText("Absolute :");
		lblBaroPressAbsLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));

		lblBaroPressSeaLevelLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblBaroPressSeaLevelLabel.setText("Sea Level :");
		lblBaroPressSeaLevelLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));
		
		lblBaroPressStationLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblBaroPressStationLabel.setText("Station :");
		lblBaroPressStationLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));
		
		lblBaroPressAltitudeLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblBaroPressAltitudeLabel.setText("Pressure Alt :");
		lblBaroPressAltitudeLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));

		lblAltimeterPressureLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblAltimeterPressureLabel.setText("Altimeter :");
		lblAltimeterPressureLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));
		
		lblBaroPressAltitude.setHorizontalAlignment(SwingConstants.LEFT);
		lblBaroPressAltitude.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 10));

		lblBaroPressAbs.setHorizontalAlignment(SwingConstants.LEFT);
		lblBaroPressAbs.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 10));

		lblBaroPressStation.setHorizontalAlignment(SwingConstants.LEFT);
		lblBaroPressStation.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 10));

		lblBaroPressSeaLevel.setHorizontalAlignment(SwingConstants.LEFT);
		lblBaroPressSeaLevel.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 10));
		
		lblAltimeterPressure.setHorizontalAlignment(SwingConstants.LEFT);
		lblAltimeterPressure.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 10));

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
				.addGap(2)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
					.addComponent(lblBaroPressSeaLevelLabel, 80, 80, 80)
					.addComponent(lblBaroPressAbsLabel, 80, 80, 80)
					.addComponent(lblBaroPressAltitudeLabel, 80, 80, 80)
					.addComponent(lblAltimeterPressureLabel, 80, 80, 80))
				.addGap(6)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
					.addComponent(lblBaroPressSeaLevel, 90, 90, 90)
					.addComponent(lblBaroPressAbs, 90, 90, 90)
					.addComponent(lblBaroPressAltitude, 90, 90, 90)
					.addComponent(lblAltimeterPressure, 90, 90, 90))
				.addContainerGap(3, Short.MAX_VALUE)));

		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE, false)
					.addComponent(lblBaroPressSeaLevelLabel, 10, 10, 10)
					.addComponent(lblBaroPressSeaLevel, 10, 10, 10))
				.addGap(4, 4, 4)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
					.addComponent(lblBaroPressAbsLabel, 10, 10, 10)
					.addComponent(lblBaroPressAbs, 10, 10, 10))
				.addGap(4, 4, 4)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
					.addComponent(lblBaroPressAltitudeLabel, 10, 10, 10)
					.addComponent(lblBaroPressAltitude, 10, 10, 10))
				.addGap(4, 4, 4)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
					.addComponent(lblAltimeterPressureLabel, 10, 10, 10)
					.addComponent(lblAltimeterPressure, 10, 10, 10))
				.addGap(4, 4, 4)));

		return panel;
	}

	private JPanel getBaroPressPanel() {
		final JPanel panel = new JPanel();

		panel.setBorder(BorderFactory.createTitledBorder(null, "Pressure", TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION, new Font(DEFAULT_FONT, Font.BOLD, 14)));

		final GroupLayout layout = new GroupLayout(panel);

		panel.setLayout(layout);

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
				.addGap(1)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
					.addComponent(lblBaroPressAbs, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addContainerGap(1, Short.MAX_VALUE)));

		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
				.addContainerGap()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE, false)
					.addComponent(lblBaroPressAbs, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		return panel;
	}

	private JPanel getTempPanelFullSize() {
		final JPanel panel = new JPanel();

		final GroupLayout layout = new GroupLayout(panel);

		panel.setLayout(layout);

		panel.setBorder(BorderFactory.createTitledBorder(null,
				"Temperatures in " + AbstractEnvironmentSensor.temperatureUnitSymbol.get(aes.getTemperatureUnits()),
				TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
				new Font(DEFAULT_FONT, Font.BOLD, 14)));
		
		final JLabel lblWindChillLabel = new JLabel();
		final JLabel lblHeatIndexLabel = new JLabel();
		final JLabel lblDryBulbTempLabel = new JLabel();
		final JLabel lblBlackGlobeTempLabel = new JLabel();
		final JLabel lblDewPointLabel = new JLabel();
		final JLabel lblWetBulbTempLabel = new JLabel();
		final JLabel lblWetBulbGlobeTempLabel = new JLabel();

		lbl24HrHighLabel.setText("High :");
		lbl24HrHighLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lbl24HrHighLabel.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 12));

		lbl24HrLowLabel.setText("Low :");
		lbl24HrLowLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lbl24HrLowLabel.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 12));

		lblBlackGlobeTempLabel.setText("Black Globe :");
		lblBlackGlobeTempLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblBlackGlobeTempLabel.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 12));

		lblWetBulbTempLabel.setText("Wet Bulb :");
		lblWetBulbTempLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblWetBulbTempLabel.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 12));
		
		lblWetBulbGlobeTempLabel.setText("Wet Bulb Glb :");
		lblWetBulbGlobeTempLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblWetBulbGlobeTempLabel.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 12));
		
		lblDryBulbTempLabel.setText("Dry Bulb :");
		lblDryBulbTempLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblDryBulbTempLabel.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 12));

		lblWindChillLabel.setText("Wind Chill :");
		lblWindChillLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblWindChillLabel.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 12));

		lblHeatIndexLabel.setText("Heat Index :");
		lblHeatIndexLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblHeatIndexLabel.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 12));

		lblDewPointLabel.setText("Dew Point :");
		lblDewPointLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblDewPointLabel.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 12));

		lblWindChill.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 12));
		lblHeatIndex.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 12));
		lblDailyLow.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 12));
		lblDailyHigh.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 12));
		lblDryBulbTemp.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 12));
		lblBlackGlobeTemp.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 12));
		lblWetBulbTemp.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 12));
		lblWetBulbGlobeTemp.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 12));
		lblDewPoint.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 12));
		
		lblWetBulbGlobeTempFlag.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 10));
		lblWetBulbGlobeTempFlag.setOpaque(true);
		lblWetBulbGlobeTempFlag.setHorizontalAlignment(SwingConstants.CENTER);
		lblWetBulbGlobeTempFlag.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		
		lblHeatIndexFlag.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 10));
		lblHeatIndexFlag.setOpaque(true);
		lblHeatIndexFlag.setHorizontalAlignment(SwingConstants.CENTER);
		lblHeatIndexFlag.setBorder(BorderFactory.createLineBorder(Color.GRAY));

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
				.addGap(2, 2, 2)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addComponent(lbl24HrLowLabel, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 74, Short.MAX_VALUE)
					.addComponent(lbl24HrHighLabel, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 74, Short.MAX_VALUE)
					.addComponent(lblWindChillLabel, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 74, Short.MAX_VALUE)
					.addComponent(lblHeatIndexLabel, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 74, Short.MAX_VALUE)
					.addComponent(lblBlackGlobeTempLabel, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 74, Short.MAX_VALUE)
					.addComponent(lblWetBulbTempLabel, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 74, Short.MAX_VALUE)
					.addComponent(lblWetBulbGlobeTempLabel, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 74, Short.MAX_VALUE)
					.addComponent(lblDryBulbTempLabel, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 74, Short.MAX_VALUE)
					.addComponent(lblDewPointLabel, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 74, Short.MAX_VALUE))
				.addGap(2, 2, 2)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
					.addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
						.addComponent(lblWetBulbGlobeTemp, 35, 35, 35)
						.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
						.addComponent(lblWetBulbGlobeTempFlag, 62, 62, Short.MAX_VALUE))
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(lblDailyHigh, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE)
						.addComponent(lblWindChill, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE))						
					.addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
						.addComponent(lblHeatIndex, 35, 35, 35)
						.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
						.addComponent(lblHeatIndexFlag, 62, 62, Short.MAX_VALUE))	
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(lblBlackGlobeTemp, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE)
						.addComponent(lblWetBulbTemp, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE)
						.addComponent(lblDryBulbTemp, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE)
						.addComponent(lblDailyLow, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE)
						.addComponent(lblDewPoint, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE)))		
				.addContainerGap(2, Short.MAX_VALUE)));

		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
				.addGap(2, 2, 2)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
					.addComponent(lblDryBulbTempLabel, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 15, Short.MAX_VALUE)
					.addComponent(lblDryBulbTemp, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 15, Short.MAX_VALUE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
					.addComponent(lblBlackGlobeTempLabel, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 15, Short.MAX_VALUE)
					.addComponent(lblBlackGlobeTemp, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 15, Short.MAX_VALUE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
					.addComponent(lblWetBulbTempLabel, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 15, Short.MAX_VALUE)
					.addComponent(lblWetBulbTemp, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 15, Short.MAX_VALUE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
					.addComponent(lblWetBulbGlobeTempLabel, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 15, Short.MAX_VALUE)
					.addComponent(lblWetBulbGlobeTemp, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 15, Short.MAX_VALUE)
					.addComponent(lblWetBulbGlobeTempFlag, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 15, Short.MAX_VALUE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
					.addComponent(lblHeatIndexLabel, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 15, Short.MAX_VALUE)
					.addComponent(lblHeatIndex, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 15, Short.MAX_VALUE)
					.addComponent(lblHeatIndexFlag, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 15, Short.MAX_VALUE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
					.addComponent(lblWindChillLabel, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 15, Short.MAX_VALUE)
					.addComponent(lblWindChill, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 15, Short.MAX_VALUE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
					.addComponent(lblDewPoint, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 15, Short.MAX_VALUE)
					.addComponent(lblDewPointLabel, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 15, Short.MAX_VALUE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
					.addComponent(lbl24HrHighLabel, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 15, Short.MAX_VALUE)
					.addComponent(lblDailyHigh, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 15, Short.MAX_VALUE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)		
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
					.addComponent(lblDailyLow, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 15, Short.MAX_VALUE)
					.addComponent(lbl24HrLowLabel, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 15, Short.MAX_VALUE))
				.addContainerGap(2, Short.MAX_VALUE)));

		return panel;
	}

	private JPanel getTempPanelFoundation7InchDisplay() {
		final JPanel panel = new JPanel();

		final GroupLayout layout = new GroupLayout(panel);

		panel.setLayout(layout);

		panel.setBorder(BorderFactory.createTitledBorder(null,
			"Temperatures in " + AbstractEnvironmentSensor.temperatureUnitSymbol.get(aes.getTemperatureUnits()),
			TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font(DEFAULT_FONT, Font.BOLD, 12)));
		
		final JLabel lblWindChillLabel = new JLabel();
		final JLabel lblHeatIndexLabel = new JLabel();
		final JLabel lblCurrentTempLabel = new JLabel();
		final JLabel lblBlackGlobeTempLabel = new JLabel();
		final JLabel lblDewPointLabel = new JLabel();
		final JLabel lblWetBulbTempLabel = new JLabel();
		final JLabel lblWetBulbGlobeTempLabel = new JLabel();

		lbl24HrHighLabel.setText("High Temp:");
		lbl24HrHighLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lbl24HrHighLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));

		lbl24HrLowLabel.setText("Low Temp:");
		lbl24HrLowLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lbl24HrLowLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));

		lblBlackGlobeTempLabel.setText("Black Globe:");
		lblBlackGlobeTempLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblBlackGlobeTempLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));

		lblWetBulbTempLabel.setText("Wet Bulb:");
		lblWetBulbTempLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblWetBulbTempLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));
		
		lblWetBulbGlobeTempLabel.setText("Wet Bulb Glb:");
		lblWetBulbGlobeTempLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblWetBulbGlobeTempLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));
		
		lblCurrentTempLabel.setText("Dry Bulb:");
		lblCurrentTempLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblCurrentTempLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));

		lblWindChillLabel.setText("Wind Chill:");
		lblWindChillLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblWindChillLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));

		lblHeatIndexLabel.setText("Heat Index:");
		lblHeatIndexLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblHeatIndexLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));

		lblDewPointLabel.setText("Dew Point:");
		lblDewPointLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblDewPointLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));

		lblWindChill.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 10));
		lblHeatIndex.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 10));
		lblDailyLow.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 10));
		lblDailyHigh.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 10));
		lblDryBulbTemp.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 10));
		lblBlackGlobeTemp.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 10));
		lblWetBulbTemp.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 10));
		lblWetBulbGlobeTemp.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 10));
		lblDewPoint.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 10));
		
		lblWetBulbGlobeTempFlag.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 8));
		lblWetBulbGlobeTempFlag.setOpaque(true);
		lblWetBulbGlobeTempFlag.setHorizontalAlignment(SwingConstants.CENTER);
		lblWetBulbGlobeTempFlag.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		
		lblHeatIndexFlag.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 8));
		lblHeatIndexFlag.setOpaque(true);
		lblHeatIndexFlag.setHorizontalAlignment(SwingConstants.CENTER);
		lblHeatIndexFlag.setBorder(BorderFactory.createLineBorder(Color.GRAY));

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
				.addGap(2, 2, 2)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addComponent(lbl24HrLowLabel, GroupLayout.Alignment.LEADING, 78, 78, 78)
					.addComponent(lbl24HrHighLabel, GroupLayout.Alignment.LEADING, 78, 78, 78)
					.addComponent(lblWindChillLabel, GroupLayout.Alignment.LEADING, 78, 78, 78)
					.addComponent(lblHeatIndexLabel, GroupLayout.Alignment.LEADING, 78, 78, 78)
					.addComponent(lblBlackGlobeTempLabel, GroupLayout.Alignment.LEADING, 78, 78, 78)
					.addComponent(lblWetBulbTempLabel, GroupLayout.Alignment.LEADING, 78, 78, 78)
					.addComponent(lblWetBulbGlobeTempLabel, GroupLayout.Alignment.LEADING, 78, 78, 78)
					.addComponent(lblCurrentTempLabel, GroupLayout.Alignment.LEADING, 78, 78, 78)
					.addComponent(lblDewPointLabel, GroupLayout.Alignment.LEADING, 78, 78, 78))
				.addGap(2, 2, 2)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
					.addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
						.addComponent(lblWetBulbGlobeTemp, 38, 38, 38)
						.addGap(2, 2, 2)
						.addComponent(lblWetBulbGlobeTempFlag, 54, 54, Short.MAX_VALUE))
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(lblDailyHigh, GroupLayout.Alignment.LEADING, 99, 99, 99)
						.addComponent(lblWindChill, GroupLayout.Alignment.LEADING, 99, 99, 99))
					.addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
						.addComponent(lblHeatIndex, 38, 38, 38)
						.addGap(2, 2, 2)
						.addComponent(lblHeatIndexFlag, 54, 54, Short.MAX_VALUE))	
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)		
						.addComponent(lblBlackGlobeTemp, GroupLayout.Alignment.LEADING, 99, 99, 99)
						.addComponent(lblWetBulbTemp, GroupLayout.Alignment.LEADING, 99, 99, 99)
						.addComponent(lblDryBulbTemp, GroupLayout.Alignment.LEADING, 99, 99, 99)
						.addComponent(lblDailyLow, GroupLayout.Alignment.LEADING, 99, 99, 99)
						.addComponent(lblDewPoint, GroupLayout.Alignment.LEADING, 99, 99, 99)))		
				.addContainerGap(2, Short.MAX_VALUE)));

		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
				.addGap(2, 2, 2)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
					.addComponent(lblCurrentTempLabel, GroupLayout.Alignment.LEADING, 10, 10, 10)
					.addComponent(lblDryBulbTemp, GroupLayout.Alignment.LEADING, 10, 10, 10))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
					.addComponent(lblBlackGlobeTempLabel, GroupLayout.Alignment.LEADING, 10, 10, 10)
					.addComponent(lblBlackGlobeTemp, GroupLayout.Alignment.LEADING, 10, 10, 10))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
					.addComponent(lblWetBulbTempLabel, GroupLayout.Alignment.LEADING, 10, 10, 10)
					.addComponent(lblWetBulbTemp, GroupLayout.Alignment.LEADING, 10, 10, 10))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
					.addComponent(lblWetBulbGlobeTempLabel, GroupLayout.Alignment.LEADING, 10, 10, 10)
					.addComponent(lblWetBulbGlobeTemp, GroupLayout.Alignment.LEADING, 10, 10, 10)
					.addComponent(lblWetBulbGlobeTempFlag, GroupLayout.Alignment.LEADING, 10, 10, 10))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
					.addComponent(lblHeatIndexLabel, GroupLayout.Alignment.LEADING, 10, 10, 10)
					.addComponent(lblHeatIndex, GroupLayout.Alignment.LEADING, 10, 10, 10)
					.addComponent(lblHeatIndexFlag, GroupLayout.Alignment.LEADING, 10, 10, 10))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
					.addComponent(lblWindChillLabel, GroupLayout.Alignment.LEADING, 10, 10, 10)
					.addComponent(lblWindChill, GroupLayout.Alignment.LEADING, 10, 10, 10))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
					.addComponent(lblDewPoint, GroupLayout.Alignment.LEADING, 10, 10, 10)
					.addComponent(lblDewPointLabel, GroupLayout.Alignment.LEADING, 10, 10, 10))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
					.addComponent(lbl24HrHighLabel, GroupLayout.Alignment.LEADING, 10, 10, 10)
					.addComponent(lblDailyHigh, GroupLayout.Alignment.LEADING, 10, 10, 10))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)		
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
					.addComponent(lblDailyLow, GroupLayout.Alignment.LEADING, 10, 10, 10)
					.addComponent(lbl24HrLowLabel, GroupLayout.Alignment.LEADING, 10, 10, 10))
				.addContainerGap(2, Short.MAX_VALUE)));

		return panel;
	}

	private JPanel getAirNowPanel() {
		final JPanel panel = new JPanel();

		panel.setBorder(BorderFactory.createTitledBorder(null, "Air Quality", TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION, new Font(DEFAULT_FONT, Font.BOLD, 14)));

		final GroupLayout layout = new GroupLayout(panel);

		panel.setLayout(layout);

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
				.addContainerGap()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addComponent(lblLastUpdateFromAgency, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(lblLocAtDtg, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
							.addComponent(lblAQIPPM25, GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)
							.addComponent(lblAQIPPM10, GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)
							.addComponent(lblAQIOzone, GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE))
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
							.addComponent(lblAQIPPM10Flag, GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
							.addComponent(lblAQIPPM25Flag, GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
							.addComponent(lblAQIOzoneFlag, GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE))))
			.addContainerGap()));

		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
				.addGap(2, 2, 2)
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
						.addComponent(lblAQIOzoneFlag, GroupLayout.DEFAULT_SIZE, 16, Short.MAX_VALUE)
						.addComponent(lblAQIOzone, 16, 16, 16))
					.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
						.addComponent(lblAQIPPM25Flag, GroupLayout.DEFAULT_SIZE, 16, Short.MAX_VALUE)
						.addComponent(lblAQIPPM25, 16, 16, 16))
					.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
						.addComponent(lblAQIPPM10, GroupLayout.DEFAULT_SIZE, 16, Short.MAX_VALUE)
						.addComponent(lblAQIPPM10Flag, 16, 16, 16))
					.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
					.addComponent(lblLocAtDtg, 16, 16, 16)
					.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
					.addComponent(lblLastUpdateFromAgency, 16, 16, 16)
					.addContainerGap(2, Short.MAX_VALUE)));

		return panel;
	}

	private JPanel getAprsPanelFoundation7InchDisplay() {
		final JPanel panel = new JPanel();

		lblAprsDTG.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));
		lblAprsTPlus.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));

		panel.setBorder(BorderFactory.createTitledBorder(null, "APRS", TitledBorder.DEFAULT_JUSTIFICATION,
			TitledBorder.DEFAULT_POSITION, new Font(DEFAULT_FONT, Font.BOLD, 12)));

		final GroupLayout layout = new GroupLayout(panel);

		panel.setLayout(layout);

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
				.addGap(2, 2, 2)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addComponent(lblAprsDTG, 130, 130, 130))
				.addGap(2, 2, 2)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addComponent(lblAprsTPlus, 100, 100, 100))
				.addContainerGap(2, Short.MAX_VALUE)));
		
		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
				.addGap(2, 2, 2)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
					.addComponent(lblAprsDTG, GroupLayout.Alignment.LEADING, 10, 10, 10)
					.addComponent(lblAprsTPlus, 10, 10, 10))
				.addContainerGap(2, Short.MAX_VALUE)));	
		
		return panel;
	}
	
	private JPanel getHumidityPanelFoundation7InchDisplay() {
		final JPanel panel = new JPanel();

		lblHumidity.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 10));

		panel.setBorder(BorderFactory.createTitledBorder(null, "Humidity", TitledBorder.DEFAULT_JUSTIFICATION,
			TitledBorder.DEFAULT_POSITION, new Font(DEFAULT_FONT, Font.BOLD, 12)));

		final GroupLayout layout = new GroupLayout(panel);

		panel.setLayout(layout);

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
				.addGap(2, 2, 2)
				.addComponent(lblHumidity, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addGap(2, 2, 2)));

		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
				.addGap(1, 1, 1)
				.addComponent(lblHumidity, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(1, 1, 1)));

		return panel;
	}

	private JPanel getGammaRadiationPanel() {
		final JPanel panel = new JPanel();

		lblGammaRadDecorated.setFont(new Font(DEFAULT_FONT, Font.BOLD, 12));

		panel.setBorder(BorderFactory.createTitledBorder(null, "Radiation \u03B3", TitledBorder.DEFAULT_JUSTIFICATION,
			TitledBorder.DEFAULT_POSITION, new Font(DEFAULT_FONT, Font.BOLD, 14)));

		final GroupLayout layout = new GroupLayout(panel);

		panel.setLayout(layout);

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
				.addGap(2, 2, 2)
				.addComponent(lblGammaRadDecorated, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addGap(2, 2, 2)));

		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
				.addGap(1, 1, 1)
				.addComponent(lblGammaRadDecorated, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(1, 1, 1)));

		return panel;
	}
	
	private JPanel getGammaRadiationPanelFoundation7InchDisplay() {
		final JPanel panel = new JPanel();

		lblGammaRadDecorated.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 9));

		panel.setBorder(BorderFactory.createTitledBorder(null, "Radiation \u03B3", TitledBorder.DEFAULT_JUSTIFICATION,
			TitledBorder.DEFAULT_POSITION, new Font(DEFAULT_FONT, Font.BOLD, 12)));

		final GroupLayout layout = new GroupLayout(panel);

		panel.setLayout(layout);

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
				.addGap(2, 2, 2)
				.addComponent(lblGammaRadDecorated, 64, 64, 64)
				.addGap(2, 2, 2)));

		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
				.addGap(1, 1, 1)
				.addComponent(lblGammaRadDecorated, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(1, 1, 1)));

		return panel;
	}

	private JPanel getHumidityPanel() {
		final JPanel panel = new JPanel();

		panel.setBorder(BorderFactory.createTitledBorder(null, "Humidity", TitledBorder.DEFAULT_JUSTIFICATION,
			TitledBorder.DEFAULT_POSITION, new Font(DEFAULT_FONT, Font.BOLD, 14)));

		final GroupLayout layout = new GroupLayout(panel);

		panel.setLayout(layout);

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
				.addContainerGap()
				.addComponent(lblHumidity, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		
		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
				.addContainerGap()
				.addComponent(lblHumidity, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		return panel;
	}

	private JPanel getWindDirectionPanel() {
		final JPanel panel = new JPanel();

		panel.setBorder(BorderFactory.createTitledBorder(null, "Wind Direction", TitledBorder.DEFAULT_JUSTIFICATION,
			TitledBorder.DEFAULT_POSITION, new Font(DEFAULT_FONT, Font.BOLD, 14)));

		final GroupLayout layout = new GroupLayout(panel);

		panel.setLayout(layout);

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
				.addGap(1, 1, 1)
				.addComponent(compassRosePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addGap(1, 1, 1)));

		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
				.addGap(1, 1, 1)
				.addComponent(compassRosePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addGap(1, 1, 1)));

		return panel;
	}

	private JPanel getPrecipitationPanelFoundation7InchDisplay() {
		final JPanel panel = new JPanel();

		final GroupLayout layout = new GroupLayout(panel);

		panel.setLayout(layout);

		panel.setBorder(BorderFactory.createTitledBorder(null, "Precipitation in " +
			AbstractEnvironmentSensor.precipitationUnitSuffix.get(aes.getPrecipitationUnits()),
			TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font(DEFAULT_FONT, Font.BOLD, 12)));

		final JLabel lblRainRateLabel = new JLabel();
		final JLabel lblPeakRainRateLabel = new JLabel();
		final JLabel lblDailyRainLabel = new JLabel();
		final JLabel lbl24HourRainLabel = new JLabel();
		final JLabel lblWeeklyRainLabel = new JLabel();
		final JLabel lblMonthlyRainLabel = new JLabel();
		final JLabel lblYearlyRainLabel = new JLabel();
		final JLabel lblDailyRainAsOfLabel = new JLabel();
		final JLabel lbl24HourRainAsOfLabel = new JLabel();
		final JLabel lblWeeklyRainAsOfLabel = new JLabel();
		final JLabel lblMonthlyRainAsOfLabel = new JLabel();
		final JLabel lblYearlyRainAsOfLabel = new JLabel();

		lblRainRateLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblRainRateLabel.setText("Rate Per Hour =");
		lblRainRateLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));

		lblDailyRainLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblDailyRainLabel.setText("Daily Rain =");
		lblDailyRainLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));

		lbl24HourRainLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lbl24HourRainLabel.setText("Rain Last 24 Hr =");
		lbl24HourRainLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));

		lblWeeklyRainLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblWeeklyRainLabel.setText("Weekly Rain =");
		lblWeeklyRainLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));

		lblMonthlyRainLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblMonthlyRainLabel.setText("Monthly Rain =");
		lblMonthlyRainLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));

		lblYearlyRainLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblYearlyRainLabel.setText("Yearly Rain =");
		lblYearlyRainLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));
		
		lblPeakRainRateLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		lblPeakRainRateLabel.setText("peak:");
		lblPeakRainRateLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));

		lblDailyRainAsOfLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		lblDailyRainAsOfLabel.setText("as of:");
		lblDailyRainAsOfLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));
		
		lbl24HourRainAsOfLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		lbl24HourRainAsOfLabel.setText("as of:");
		lbl24HourRainAsOfLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));

		lblWeeklyRainAsOfLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		lblWeeklyRainAsOfLabel.setText("as of:");
		lblWeeklyRainAsOfLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));

		lblMonthlyRainAsOfLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		lblMonthlyRainAsOfLabel.setText("as of:");
		lblMonthlyRainAsOfLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));

		lblYearlyRainAsOfLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		lblYearlyRainAsOfLabel.setText("as of:");
		lblYearlyRainAsOfLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));

		lblDailyRain.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 10));
		lbl24HourRain.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 10));
		lblMonthlyRain.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 10));
		lblRainRate.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 10));
		lblYearlyRain.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 10));
		lblWeeklyRain.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 10));
		
		lblDailyRain.setHorizontalAlignment(SwingConstants.RIGHT);
		lbl24HourRain.setHorizontalAlignment(SwingConstants.RIGHT);
		lblMonthlyRain.setHorizontalAlignment(SwingConstants.RIGHT);
		lblRainRate.setHorizontalAlignment(SwingConstants.RIGHT);
		lblYearlyRain.setHorizontalAlignment(SwingConstants.RIGHT);
		lblWeeklyRain.setHorizontalAlignment(SwingConstants.RIGHT);

		lblPeakRainRate.setHorizontalAlignment(SwingConstants.RIGHT);
		lblDailyRainAsOfDTG.setHorizontalAlignment(SwingConstants.RIGHT);
		lbl24HourRainAsOfDTG.setHorizontalAlignment(SwingConstants.RIGHT);
		lblMonthlyRainAsOfDTG.setHorizontalAlignment(SwingConstants.RIGHT);
		lblWeeklyRainAsOfDTG.setHorizontalAlignment(SwingConstants.RIGHT);
		lblYearlyRainAsOfDTG.setHorizontalAlignment(SwingConstants.RIGHT);
		
		lblPeakRainRate.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 10));
		lblDailyRainAsOfDTG.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 10));
		lbl24HourRainAsOfDTG.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 10));
		lblMonthlyRainAsOfDTG.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 10));
		lblWeeklyRainAsOfDTG.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 10));
		lblYearlyRainAsOfDTG.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 10));

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
				.addGap(2)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addGroup(layout.createSequentialGroup()
						.addComponent(lblRainRateLabel, 100, 100, 100)
						.addGap(2)
						.addComponent(lblRainRate, 35, 35, 35)
						.addGap(2)
						.addComponent(lblPeakRainRateLabel, 50, 50, 50)	
						.addGap(2)
						.addComponent(lblPeakRainRate, 130, 130, 130))	
					.addGroup(layout.createSequentialGroup()
						.addComponent(lbl24HourRainLabel, 100, 100, 100)
						.addGap(2)
						.addComponent(lbl24HourRain, 35, 35, 35)
						.addGap(2)
						.addComponent(lbl24HourRainAsOfLabel, 50, 50, 50)
						.addGap(2)
						.addComponent(lbl24HourRainAsOfDTG, 130, 130, 130))
					.addGroup(layout.createSequentialGroup()
						.addComponent(lblDailyRainLabel, 100, 100, 100)
						.addGap(2)
						.addComponent(lblDailyRain, 35, 35, 35)
						.addGap(2)
						.addComponent(lblDailyRainAsOfLabel, 50, 50, 50)
						.addGap(2)
						.addComponent(lblDailyRainAsOfDTG, 130, 130, 130))
					.addGroup(layout.createSequentialGroup()	
						.addComponent(lblWeeklyRainLabel, 100, 100, 100)
						.addGap(2)
						.addComponent(lblWeeklyRain, 35, 35, 35)
						.addGap(2)
						.addComponent(lblWeeklyRainAsOfLabel, 50, 50, 50)
						.addGap(2)
						.addComponent(lblWeeklyRainAsOfDTG, 130, 130, 130))
					.addGroup(layout.createSequentialGroup()	
						.addComponent(lblMonthlyRainLabel, 100, 100, 100)
						.addGap(2)
						.addComponent(lblMonthlyRain, 35, 35, 35)
						.addGap(2)
						.addComponent(lblMonthlyRainAsOfLabel, 50, 50, 50)
						.addGap(2)
						.addComponent(lblMonthlyRainAsOfDTG, 130, 130, 130))
					.addGroup(layout.createSequentialGroup()						
						.addComponent(lblYearlyRainLabel, 100, 100, 100)
						.addGap(2)
						.addComponent(lblYearlyRain, 35, 35, 35)
						.addGap(2)
						.addComponent(lblYearlyRainAsOfLabel, 50, 50, 50)
						.addGap(2)
						.addComponent(lblYearlyRainAsOfDTG, 130, 130, 130)))
				.addGap(2)));

		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
				.addGap(2, 2 ,2)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addComponent(lblRainRateLabel, GroupLayout.Alignment.LEADING, 10, 10, 10)
					.addComponent(lblRainRate, GroupLayout.Alignment.LEADING, 10, 10, 10)
					.addComponent(lblPeakRainRateLabel, GroupLayout.Alignment.LEADING, 10, 10, 10)	
					.addComponent(lblPeakRainRate, GroupLayout.Alignment.LEADING, 10, 10, 10))
				.addPreferredGap(ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup()
					.addComponent(lbl24HourRainLabel, GroupLayout.Alignment.LEADING, 10, 10, 10)
					.addComponent(lbl24HourRain, GroupLayout.Alignment.LEADING, 10, 10, 10)
					.addComponent(lbl24HourRainAsOfLabel, GroupLayout.Alignment.LEADING, 10, 10, 10)
					.addComponent(lbl24HourRainAsOfDTG, GroupLayout.Alignment.LEADING, 10, 10, 10))
				.addPreferredGap(ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup()
					.addComponent(lblDailyRainLabel, GroupLayout.Alignment.LEADING, 10, 10, 10)
					.addComponent(lblDailyRain, GroupLayout.Alignment.LEADING, 10, 10, 10)
					.addComponent(lblDailyRainAsOfLabel, GroupLayout.Alignment.LEADING, 10, 10, 10)
					.addComponent(lblDailyRainAsOfDTG, GroupLayout.Alignment.LEADING, 10, 10, 10))
				.addPreferredGap(ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup()	
					.addComponent(lblWeeklyRainLabel, GroupLayout.Alignment.LEADING, 10, 10, 10)
					.addComponent(lblWeeklyRain, GroupLayout.Alignment.LEADING, 10, 10, 10)
					.addComponent(lblWeeklyRainAsOfLabel, GroupLayout.Alignment.LEADING, 10, 10, 10)
					.addComponent(lblWeeklyRainAsOfDTG, GroupLayout.Alignment.LEADING, 10, 10, 10))
				.addPreferredGap(ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup()	
					.addComponent(lblMonthlyRainLabel, GroupLayout.Alignment.LEADING, 10, 10, 10)
					.addComponent(lblMonthlyRain, GroupLayout.Alignment.LEADING, 10, 10, 10)
					.addComponent(lblMonthlyRainAsOfLabel, GroupLayout.Alignment.LEADING, 10, 10, 10)
					.addComponent(lblMonthlyRainAsOfDTG, GroupLayout.Alignment.LEADING, 10, 10, 10))
				.addPreferredGap(ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup()						
					.addComponent(lblYearlyRainLabel, GroupLayout.Alignment.LEADING, 10, 10, 10)
					.addComponent(lblYearlyRain, GroupLayout.Alignment.LEADING, 10, 10, 10)
					.addComponent(lblYearlyRainAsOfLabel, GroupLayout.Alignment.LEADING, 10, 10, 10)
					.addComponent(lblYearlyRainAsOfDTG, GroupLayout.Alignment.LEADING, 10, 10, 10))
			.addContainerGap(2, Short.MAX_VALUE)));
		
		return panel;
	}

	private final JPanel getPrecipitationPanel() {
		final JPanel panel = new JPanel();

		final GroupLayout layout = new GroupLayout(panel);

		panel.setLayout(layout);

		panel.setBorder(BorderFactory.createTitledBorder(null, "Precipitation in " +
			AbstractEnvironmentSensor.precipitationUnitSuffix.get(aes.getPrecipitationUnits()),
			TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font(DEFAULT_FONT, Font.BOLD, 14)));

		final JLabel lblRainRateLabel = new JLabel();
		final JLabel lblDailyRainLabel = new JLabel();
		final JLabel lbl24HourRainLabel = new JLabel();
		final JLabel lblWeeklyRainLabel = new JLabel();
		final JLabel lblMonthlyRainLabel = new JLabel();
		final JLabel lblYearlyRainLabel = new JLabel();
		final JLabel lblDailyRainAsOfLabel = new JLabel();
		final JLabel lbl24HourRainAsOfLabel = new JLabel();
		final JLabel lblWeeklyRainAsOfLabel = new JLabel();
		final JLabel lblMonthlyRainAsOfLabel = new JLabel();
		final JLabel lblYearlyRainAsOfLabel = new JLabel();

		lblRainRateLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblRainRateLabel.setText("Rate Per Hour =");
		lblRainRateLabel.setToolTipText("");

		lblDailyRainLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblDailyRainLabel.setText("Daily Rain =");
		lblDailyRainLabel.setToolTipText("");

		lbl24HourRainLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lbl24HourRainLabel.setText("Rain Last 24 Hr =");
		lbl24HourRainLabel.setToolTipText("");

		lblWeeklyRainLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblWeeklyRainLabel.setText("Weekly Rain =");
		lblWeeklyRainLabel.setToolTipText("");

		lblMonthlyRainLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblMonthlyRainLabel.setText("Monthly Rain =");
		lblMonthlyRainLabel.setToolTipText("");

		lblYearlyRainLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblYearlyRainLabel.setText("Yearly Rain =");
		lblYearlyRainLabel.setToolTipText("");

		lblDailyRainAsOfLabel.setText("as of:");

		lbl24HourRainAsOfLabel.setText("as of:");

		lblWeeklyRainAsOfLabel.setText("as of:");

		lblMonthlyRainAsOfLabel.setText("as of:");

		lblYearlyRainAsOfLabel.setText("as of:");

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup().addContainerGap()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
					.addComponent(lblMonthlyRainLabel, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(lblWeeklyRainLabel, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(lbl24HourRainLabel, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(lblDailyRainLabel, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(lblRainRateLabel, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 95, Short.MAX_VALUE)
					.addComponent(lblYearlyRainLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
							.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
								.addComponent(lblMonthlyRain, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 35, Short.MAX_VALUE)
								.addComponent(lblWeeklyRain, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(lbl24HourRain, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(lblDailyRain, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(lblYearlyRain, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE)))
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
							.addComponent(lblYearlyRainAsOfLabel, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 37, GroupLayout.PREFERRED_SIZE)
							.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
								.addComponent(lblMonthlyRainAsOfLabel, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 37, Short.MAX_VALUE)
								.addComponent(lblWeeklyRainAsOfLabel, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(lbl24HourRainAsOfLabel, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(lblDailyRainAsOfLabel, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
							.addComponent(lbl24HourRainAsOfDTG, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 129, Short.MAX_VALUE)
							.addComponent(lblDailyRainAsOfDTG, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 129, Short.MAX_VALUE)
							.addComponent(lblWeeklyRainAsOfDTG, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(lblMonthlyRainAsOfDTG, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(lblYearlyRainAsOfDTG, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
					.addComponent(lblRainRate, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addContainerGap()));

		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
				.addContainerGap()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
					.addComponent(lblRainRate, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(lblRainRateLabel))
				.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
					.addComponent(lbl24HourRain, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(lbl24HourRainAsOfLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(lbl24HourRainAsOfDTG, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
					.addComponent(lbl24HourRainLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
					.addComponent(lblDailyRain, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(lblDailyRainAsOfLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(lblDailyRainAsOfDTG, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
					.addComponent(lblDailyRainLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
					.addComponent(lblWeeklyRainLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(lblWeeklyRain, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(lblWeeklyRainAsOfLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(lblWeeklyRainAsOfDTG, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
					.addComponent(lblMonthlyRainLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(lblMonthlyRain, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(lblMonthlyRainAsOfLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(lblMonthlyRainAsOfDTG, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
					.addComponent(lblYearlyRainLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(lblYearlyRain, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addGroup(GroupLayout.Alignment.TRAILING, layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(lblYearlyRainAsOfLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(lblYearlyRainAsOfDTG)))
				.addContainerGap()));

		return panel;
	}

	private final JPanel getLunarConditionsPanelFoundation7InchDisplay() {
		final JPanel panel = new JPanel();

		final JLabel lblLunarStateLabel = new JLabel();
		final JLabel lblLunarPhaseLabel = new JLabel();
		final JLabel lblLunarAzimuthLabel = new JLabel();
		final JLabel lblLunarElevationLabel = new JLabel();
		final JLabel lblMoonRiseLabel = new JLabel();
		final JLabel lblMoonSetLabel = new JLabel();

		lblLunarStateLabel.setText("Lunar State :");
		lblLunarStateLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblLunarStateLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));

		lblLunarPhaseLabel.setText("Lunar Phase :");
		lblLunarPhaseLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblLunarPhaseLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));

		lblLunarAzimuthLabel.setText("Azimuth :");
		lblLunarAzimuthLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblLunarAzimuthLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));

		lblLunarElevationLabel.setText("Elevation :");
		lblLunarElevationLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblLunarElevationLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));

		lblMoonRiseLabel.setText("MoonRise: ");
		lblMoonRiseLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblMoonRiseLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));

		lblMoonSetLabel.setText("MoonSet: ");
		lblMoonSetLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblMoonSetLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));

		lblLunarState.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 10));
		lblLunarPhase.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 10));
		lblLunarAzimuth.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 10));
		lblLunarElevation.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 10));
		lblMoonRise.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 10));
		lblMoonSet.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 10));

		panel.setBorder(BorderFactory.createTitledBorder(null, "Lunar Conditions", TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION, new Font(DEFAULT_FONT, Font.BOLD, 12)));

		final GroupLayout layout = new GroupLayout(panel);

		panel.setLayout(layout);

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
				.addGap(2, 2, 2)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addComponent(lblLunarStateLabel, 90, 90, 90)
					.addComponent(lblLunarPhaseLabel, 90, 90, 90)
					.addComponent(lblLunarAzimuthLabel, 90, 90, 90)
					.addComponent(lblLunarElevationLabel, 90, 90, 90)
					.addComponent(lblMoonRiseLabel, 90, 90, 90)
					.addComponent(lblMoonSetLabel, 90, 90, 90))
				.addGap(2, 2, 2)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addComponent(lblLunarState, 140, 140, 140)
					.addComponent(lblLunarPhase, 140, 140, 140)
					.addComponent(lblLunarAzimuth, 140, 140, 140)
					.addComponent(lblLunarElevation, 140, 140, 140)
					.addComponent(lblMoonRise, 140, 140, 140)
					.addComponent(lblMoonSet, 140, 140, 140))
				.addContainerGap(2, Short.MAX_VALUE)));

		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
				.addGap(2, 2, 2)
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
						.addComponent(lblLunarStateLabel, GroupLayout.Alignment.LEADING, 10, 10, 10)
						.addComponent(lblLunarState, 10, 10, 10))
					.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(lblLunarPhaseLabel, 10, 10, 10)
						.addComponent(lblLunarPhase, 10, 10, 10))
					.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(lblLunarAzimuthLabel, 10, 10, 10)
						.addComponent(lblLunarAzimuth, 10, 10, 10))
					.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(lblLunarElevationLabel, 10, 10, 10)
						.addComponent(lblLunarElevation, 10, 10, 10))
					.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(lblMoonRiseLabel, 10, 10, 10)
						.addComponent(lblMoonRise, 10, 10, 10))
					.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(lblMoonSetLabel, 10, 10, 10)
						.addComponent(lblMoonSet, 10, 10, 10))
					.addContainerGap(2, Short.MAX_VALUE)));

		return panel;
	}

	private final JPanel getLunarConditionsPanelWithGraphics() {
		final JPanel panel = new JPanel();

		final JLabel lblLunarStateLabel = new JLabel();
		final JLabel lblLunarPhaseLabel = new JLabel();
		final JLabel lblLunarAzimuthLabel = new JLabel();
		final JLabel lblLunarElevationLabel = new JLabel();
		final JLabel lblLunarPhaseAngleLabel = new JLabel();
		final JLabel lblLunarDistanceLabel = new JLabel();
		final JLabel lblMoonRiseLabel = new JLabel();
		final JLabel lblMoonSetLabel = new JLabel();

		lblLunarStateLabel.setText("Lunar State =");
		lblLunarStateLabel.setHorizontalAlignment(SwingConstants.LEFT);

		lblLunarPhaseLabel.setText("Lunar Phase =");
		lblLunarPhaseLabel.setHorizontalAlignment(SwingConstants.LEFT);

		lblLunarAzimuthLabel.setText("Azimuth =");
		lblLunarAzimuthLabel.setHorizontalAlignment(SwingConstants.LEFT);

		lblLunarPhaseAngleLabel.setText("Phase Angle =");
		lblLunarPhaseAngleLabel.setHorizontalAlignment(SwingConstants.LEFT);

		lblLunarDistanceLabel.setText("Distance =");
		lblLunarDistanceLabel.setHorizontalAlignment(SwingConstants.LEFT);

		lblLunarElevationLabel.setText("Elevation =");
		lblLunarElevationLabel.setHorizontalAlignment(SwingConstants.LEFT);

		lblMoonRiseLabel.setText("MoonRise: ");
		lblMoonRiseLabel.setHorizontalAlignment(SwingConstants.LEFT);

		lblMoonSetLabel.setText("MoonSet: ");
		lblMoonSetLabel.setHorizontalAlignment(SwingConstants.LEFT);

		panel.setBorder(BorderFactory.createTitledBorder(null, "Lunar Conditions", TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION, new Font(DEFAULT_FONT, Font.BOLD, 14)));

		final GroupLayout layout = new GroupLayout(panel);

		panel.setLayout(layout);

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
				.addContainerGap()
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
						.addComponent(lblLunarPhaseLabel, GroupLayout.DEFAULT_SIZE, 73, Short.MAX_VALUE)
						.addComponent(lblLunarStateLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(lblLunarAzimuthLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(lblLunarDistanceLabel, GroupLayout.DEFAULT_SIZE, 73, Short.MAX_VALUE)
						.addComponent(lblLunarElevationLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(lblMoonRiseLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
					.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addGroup(layout.createSequentialGroup()
							.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
								.addComponent(lblLunarPhase, GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE)
								.addComponent(lblLunarState, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(lblLunarDistance, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(lblLunarElevation, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(lblLunarAzimuth, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
							.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(moonShape, 100, 100, GroupLayout.PREFERRED_SIZE))
						.addGroup(layout.createSequentialGroup()
							.addComponent(lblMoonRise, GroupLayout.PREFERRED_SIZE, 55, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
							.addComponent(lblMoonSetLabel, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
							.addComponent(lblMoonSet, GroupLayout.PREFERRED_SIZE, 55, GroupLayout.PREFERRED_SIZE)
							.addGap(0, 12, Short.MAX_VALUE)))
					.addContainerGap()));

		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout
				.createSequentialGroup()
				.addGap(2, 2, 2)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
							.addComponent(lblLunarPhaseLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(lblLunarPhase, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
							.addComponent(lblLunarStateLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(lblLunarState, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
							.addComponent(lblLunarDistanceLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(lblLunarDistance, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
							.addComponent(lblLunarAzimuthLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(lblLunarAzimuth, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
							.addComponent(lblLunarElevationLabel)
							.addComponent(lblLunarElevation, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE)))
					.addComponent(moonShape, 100, 100, GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING).addComponent(lblMoonRiseLabel)
					.addComponent(lblMoonRise, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE)
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(lblMoonSetLabel)
						.addComponent(lblMoonSet, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE)))
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		return panel;
	}

	private final JPanel getAirQualityPanelFoundation7InchDisplay() {

		final JPanel panel = new JPanel();

		panel.setBorder(BorderFactory.createTitledBorder(null, "Air Quality", TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION, new Font(DEFAULT_FONT, Font.BOLD, 12)));

		final GroupLayout layout = new GroupLayout(panel);

		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		panel.setLayout(layout);

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGap(2, 2, 2)
			.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(lblAQIPPM25FlagDecorated, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(lblAQIPPM10FlagDecorated, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(lblAQIOzoneFlagDecorated, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
			.addGap(2, 2, 2));

		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
				.addGap(2, 2, 2)
				.addComponent(lblAQIPPM25FlagDecorated, 22, 22, 22)
				.addGap(2, 2, 2)
				.addComponent(lblAQIPPM10FlagDecorated, 22, 22, 22)
				.addGap(2, 2, 2)
				.addComponent(lblAQIOzoneFlagDecorated, 22, 22, 22)
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		return panel;
	}

	private final JPanel getSolarRadiationPanelFoundation7InchDisplay() {
		final JPanel panel = new JPanel();

		final GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);

		panel.setBorder(BorderFactory.createTitledBorder(null, "Solar Characteristics", TitledBorder.DEFAULT_JUSTIFICATION,
			TitledBorder.DEFAULT_POSITION, new Font(DEFAULT_FONT, Font.BOLD, 12)));
		
		final JLabel sunriseLabel = new JLabel();
		final JLabel sunriseEndLabel = new JLabel();
		final JLabel morningGoldenHourLabel = new JLabel();
		final JLabel solarNoonLabel = new JLabel();
		final JLabel eveningGoldenHourLabel = new JLabel();
		final JLabel sunsetStartLabel = new JLabel();
		final JLabel sunsetLabel = new JLabel();
		final JLabel duskLabel = new JLabel();
		final JLabel nauticalDuskLabel = new JLabel();
		final JLabel nightLabel = new JLabel();	
		final JLabel nadirLabel = new JLabel();
		final JLabel nightEndLabel = new JLabel();
		final JLabel nauticalDawnLabel = new JLabel();
		final JLabel dawnLabel = new JLabel();
		
		sunrise = new JLabel();
		sunriseEnd = new JLabel();
		eveningGoldenHour = new JLabel();
		morningGoldenHour = new JLabel();
		solarNoon = new JLabel();
		sunsetStart = new JLabel();
		sunset = new JLabel();
		dusk = new JLabel();
		nauticalDusk = new JLabel();
		night = new JLabel();
		nadir = new JLabel();
		nightEnd = new JLabel();
		nauticalDawn = new JLabel();
		dawn = new JLabel();
		
		sunriseLabel.setToolTipText("Sunrise (top edge of the sun appears on the horizon)");
		sunriseEndLabel.setToolTipText("Sunrise Ends (bottom edge of the sun touches the horizon)");
		morningGoldenHourLabel.setToolTipText("Morning Golden Hour (soft light, best time for photography) Ends");
		solarNoonLabel.setToolTipText("Solar Noon (sun is in the highest position)");
		eveningGoldenHourLabel.setToolTipText("Evening Golden Hour Starts");
		sunsetStartLabel.setToolTipText("Sunset Starts (bottom edge of the sun touches the horizon)");
		sunsetLabel.setToolTipText("Sunset (sun disappears below the horizon, evening civil twilight starts)");
		duskLabel.setToolTipText("Dusk (evening nautical twilight starts)");
		nauticalDuskLabel.setToolTipText("Nautical Dusk (evening astronomical twilight starts)");
		nightLabel.setToolTipText("Night Starts (dark enough for astronomical observations)");
		nadirLabel.setToolTipText("Nadir (darkest moment of the night, sun is in the lowest position)");
		nightEndLabel.setToolTipText("Night Ends (morning astronomical twilight starts)");
		nauticalDawnLabel.setToolTipText("Nautical Dawn (morning nautical twilight starts)");
		dawnLabel.setToolTipText("Dawn (morning nautical twilight ends, morning civil twilight starts)");
		
		sunrise.setToolTipText("Sunrise (top edge of the sun appears on the horizon)");
		sunriseEnd.setToolTipText("Sunrise Ends (bottom edge of the sun touches the horizon)");
		morningGoldenHour.setToolTipText("Morning Golden Hour (soft light, best time for photography) Ends");
		solarNoon.setToolTipText("Solar Noon (sun is in the highest position)");
		eveningGoldenHour.setToolTipText("Evening Golden Hour Starts");
		sunsetStart.setToolTipText("Sunset Starts (bottom edge of the sun touches the horizon)");
		sunset.setToolTipText("Sunset (sun disappears below the horizon, evening civil twilight starts)");
		dusk.setToolTipText("Dusk (evening nautical twilight starts)");
		nauticalDusk.setToolTipText("Nautical Dusk (evening astronomical twilight starts)");
		night.setToolTipText("Night Starts (dark enough for astronomical observations)");
		nadir.setToolTipText("Nadir (darkest moment of the night, sun is in the lowest position)");
		nightEnd.setToolTipText("Night Ends (morning astronomical twilight starts)");
		nauticalDawn.setToolTipText("Nautical Dawn (morning nautical twilight starts)");
		dawn.setToolTipText("Dawn (morning nautical twilight ends, morning civil twilight starts)");
		
		final JLabel lblSolarAzimuthLabel = new JLabel();
		final JLabel lblLodExcessSecondsLabel = new JLabel();
		final JLabel lblSolarElevationLabel = new JLabel();
		final JLabel lblUVIndexLabel = new JLabel();
		final JLabel lblUVARadLabel = new JLabel();
		final JLabel lblUVBRadLabel = new JLabel();
		final JLabel lblSolarIrradianceLabel = new JLabel();

		lblLodExcessSecondsLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblLodExcessSecondsLabel.setText("ExcessLOD:");
		lblLodExcessSecondsLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));

		lblSolarAzimuthLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblSolarAzimuthLabel.setText("Azimuth:");
		lblSolarAzimuthLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));

		lblSolarElevationLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblSolarElevationLabel.setText("Elevation:");
		lblSolarElevationLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));

		lblUVIndexLabel.setText("UV Index:");
		lblUVIndexLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblUVIndexLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));

		lblSolarIrradianceLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblSolarIrradianceLabel.setText("Solar Rad:");
		lblSolarIrradianceLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));

		lblUVBRadLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblUVBRadLabel.setText("UVB:");
		lblUVBRadLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));

		lblUVARadLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblUVARadLabel.setText("UVA:");
		lblUVARadLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));

		lblLodExcessSeconds.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 10));
		lblSolarAzimuth.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 10));
		lblSolarElevation.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 10));
		lblUVIndex.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 10));
		lblUVIndexFlag.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 8));
		lblSolarIrradiance.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 10));
		lblUVBRad.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 10));
		lblUVARad.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 10));

		sunriseLabel.setHorizontalAlignment(SwingConstants.LEFT);
		sunriseLabel.setText("Sunrise:");
		sunriseLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));
		
		sunriseEndLabel.setHorizontalAlignment(SwingConstants.LEFT);
		sunriseEndLabel.setText("SunriseEnd:");
		sunriseEndLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));
		
		sunsetLabel.setHorizontalAlignment(SwingConstants.LEFT);
		sunsetLabel.setText("Sunset:");
		sunsetLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));
		
		sunsetStartLabel.setHorizontalAlignment(SwingConstants.LEFT);
		sunsetStartLabel.setText("SunsetSt:");
		sunsetStartLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));
		
		morningGoldenHourLabel.setHorizontalAlignment(SwingConstants.LEFT);
		morningGoldenHourLabel.setText("MornGldHr:");
		morningGoldenHourLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));
		
		eveningGoldenHourLabel.setHorizontalAlignment(SwingConstants.LEFT);
		eveningGoldenHourLabel.setText("EveGoldHr:");
		eveningGoldenHourLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));
		
		duskLabel.setHorizontalAlignment(SwingConstants.LEFT);
		duskLabel.setText("Dusk:");
		duskLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));
		
		nauticalDuskLabel.setHorizontalAlignment(SwingConstants.LEFT);
		nauticalDuskLabel.setText("NautDusk:");
		nauticalDuskLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));
		
		dawnLabel.setHorizontalAlignment(SwingConstants.LEFT);
		dawnLabel.setText("Dawn:");
		dawnLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));
		
		nauticalDawnLabel.setHorizontalAlignment(SwingConstants.LEFT);
		nauticalDawnLabel.setText("NautDawn:");
		nauticalDawnLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));
		
		solarNoonLabel.setHorizontalAlignment(SwingConstants.LEFT);
		solarNoonLabel.setText("SolarNoon:");
		solarNoonLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));
		
		nightEndLabel.setHorizontalAlignment(SwingConstants.LEFT);
		nightEndLabel.setText("NightEnd:");
		nightEndLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));
		
		nightLabel.setHorizontalAlignment(SwingConstants.LEFT);
		nightLabel.setText("Night:");
		nightLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));
		
		sunrise.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 10));
		sunriseEnd.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 10));
		eveningGoldenHour.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 10));
		morningGoldenHour.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 10));
		solarNoon.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 10));
		sunsetStart.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 10));
		sunset.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 10));
		dusk.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 10));
		nauticalDusk.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 10));
		night.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 10));
		nadir.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 10));
		nightEnd.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 10));
		nauticalDawn.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 10));
		dawn.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 10));
		
		sunrise.setHorizontalAlignment(SwingConstants.LEFT);
		eveningGoldenHour.setHorizontalAlignment(SwingConstants.LEFT);
		morningGoldenHour.setHorizontalAlignment(SwingConstants.LEFT);
		solarNoon.setHorizontalAlignment(SwingConstants.LEFT); 
		sunsetStart.setHorizontalAlignment(SwingConstants.LEFT);
		sunset.setHorizontalAlignment(SwingConstants.LEFT);
		dusk.setHorizontalAlignment(SwingConstants.LEFT);
		nauticalDusk.setHorizontalAlignment(SwingConstants.LEFT);
		night.setHorizontalAlignment(SwingConstants.LEFT);
		nightEnd.setHorizontalAlignment(SwingConstants.LEFT);
		nauticalDawn.setHorizontalAlignment(SwingConstants.LEFT);
		dawn.setHorizontalAlignment(SwingConstants.LEFT); 
		
		nadir.setHorizontalAlignment(SwingConstants.LEFT);
		sunriseEnd.setHorizontalAlignment(SwingConstants.LEFT); 
		
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(2)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                    .addComponent(lblSolarElevationLabel, 64, 64, 64)
                    .addComponent(lblUVIndexLabel, 64, 64, 64)
                    .addComponent(lblUVARadLabel, 64, 64, 64)
                    .addComponent(lblLodExcessSecondsLabel, 64, 64, 64)
                    .addComponent(lblSolarIrradianceLabel, 64, 64, 64)
                    .addComponent(lblSolarAzimuthLabel, 64, 64, 64))
                .addGap(10)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblUVIndex, 8, 8, 8)
                        .addGap(6)
                        .addComponent(lblUVIndexFlag, 70, 70, 70))
                    .addComponent(lblSolarAzimuth, 80, 80, 80)
                    .addComponent(lblSolarIrradiance, 80, 80, 80)
                    .addComponent(lblLodExcessSeconds, 80, 80, 80)
                    .addComponent(lblUVARad, 80, 80, 80)
                    .addComponent(lblSolarElevation, 80, 80, 80))
                .addGap(8)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                	.addComponent(nightEndLabel, 68, 68, 68)	
                	.addComponent(nauticalDawnLabel, 68, 68, 68)	
                	.addComponent(dawnLabel, 68, 68, 68)
                    .addComponent(sunriseLabel, 68, 68, 68)
                    .addComponent(morningGoldenHourLabel, 68, 68, 68)
                    .addComponent(solarNoonLabel, 68, 68, 68))
                .addGap(5)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(nightEnd, 58, 58, 58)
                    .addComponent(nauticalDawn, 58, 58, 58)
                    .addComponent(dawn, 58, 58, 58)
                    .addComponent(sunrise, 58, 58, 58)
                    .addComponent(morningGoldenHour, 58, 58, 58)
                    .addComponent(solarNoon, 58, 58, 58))
                .addGap(5)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(eveningGoldenHourLabel, 68, 68, 68)
                    .addComponent(sunsetStartLabel, 68, 68, 68)
                    .addComponent(sunsetLabel, 68, 68, 68)
                    .addComponent(duskLabel, 68, 68, 68)
                    .addComponent(nightLabel, 68, 68, 68)
                    .addComponent(nauticalDuskLabel, 68, 68, 68))
                .addGap(5)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(eveningGoldenHour, 58, 58, 58)   
                	.addComponent(sunsetStart, 58, 58, 58)
                    .addComponent(sunset, 58, 58, 58)
                    .addComponent(nauticalDusk, 58, 58, 58)
                    .addComponent(dusk, 58, 58, 58)
                    .addComponent(night, 58, 58, 58))
                .addContainerGap(5, Short.MAX_VALUE)));
		
	        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
	            .addGroup(layout.createSequentialGroup()
	                .addGap(2)
	                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
	                    .addComponent(lblUVARadLabel, 10, 10, 10)
	                    .addComponent(lblUVARad, 10, 10, 10)
	                    .addComponent(nightEndLabel, 10, 10, 10)
	                    .addComponent(nightEnd, 10, 10, 10)
	                    .addComponent(eveningGoldenHourLabel, 10, 10, 10)
	                    .addComponent(eveningGoldenHour, 10, 10, 10))
	                .addGap(4)
	                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
	                    .addComponent(lblLodExcessSecondsLabel, 10, 10, 10)
	                    .addComponent(lblLodExcessSeconds, 10, 10, 10)
	                    .addComponent(nauticalDawnLabel, 10, 10, 10)
	                    .addComponent(nauticalDawn, 10, 10, 10)
	                    .addComponent(sunsetStartLabel, 10, 10, 10)
	                    .addComponent(sunsetStart, 10, 10, 10))
	                .addGap(4)
	                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
	                    .addComponent(lblSolarIrradianceLabel, 10, 10, 10)
	                    .addComponent(lblSolarIrradiance, 10, 10, 10)
	                    .addComponent(dawnLabel, 10, 10, 10)
	                    .addComponent(dawn, 10, 10, 10)
	                    .addComponent(sunsetLabel, 10, 10, 10)
	                    .addComponent(sunset, 10, 10, 10))
	                .addGap(4)
	                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
	                    .addComponent(lblSolarAzimuthLabel, 10, 10, 10)
	                    .addComponent(lblSolarAzimuth, 10, 10, 10)
	                    .addComponent(sunriseLabel, 10, 10, 10)
	                    .addComponent(sunrise, 10, 10, 10)
	                    .addComponent(nauticalDuskLabel, 10, 10, 10)
	                    .addComponent(nauticalDusk, 10, 10, 10))
	                .addGap(4)
	                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
	                    .addComponent(lblSolarElevationLabel, 10, 10, 10)
	                    .addComponent(lblSolarElevation, 10, 10, 10)
	                    .addComponent(morningGoldenHourLabel, 10, 10, 10)
	                    .addComponent(morningGoldenHour, 10, 10, 10)
	                    .addComponent(duskLabel, GroupLayout.Alignment.TRAILING, 10, 10, 10)
	                    .addComponent(dusk, 10, 10, 10))
	                .addGap(4)
	                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
	                    .addComponent(lblUVIndexLabel, 10, 10, 10)
	                    .addComponent(lblUVIndex, 10, 10, 10)
	                    .addComponent(lblUVIndexFlag, 10, 10, 10)
	                    .addComponent(solarNoonLabel, 10, 10, 10)
	                    .addComponent(solarNoon, 10, 10, 10)
	                    .addComponent(nightLabel, 10, 10, 10)
	                    .addComponent(night, 10, 10, 10))
	                .addContainerGap(2, Short.MAX_VALUE)));
	        
	    return panel;
	}

	private JPanel getSolarRadiationPanel() {
		final JPanel panel = new JPanel();

		final GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);

		panel.setBorder(BorderFactory.createTitledBorder(null, "Solar Characteristics", TitledBorder.DEFAULT_JUSTIFICATION,
			TitledBorder.DEFAULT_POSITION, new Font(DEFAULT_FONT, Font.BOLD, 14)));

		final JLabel lblSolarAzimuthLabel = new JLabel();
		final JLabel lblLodExcessSecondsLabel = new JLabel();
		final JLabel lblSolarElevationLabel = new JLabel();
		final JLabel lblUVIndexLabel = new JLabel();
		final JLabel lblUVARadLabel = new JLabel();
		final JLabel lblUVBRadLabel = new JLabel();
		final JLabel lblSolarLabel = new JLabel();

		lblLodExcessSecondsLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblLodExcessSecondsLabel.setText("Excess LOD =");
		lblLodExcessSecondsLabel.setToolTipText("");

		lblSolarAzimuthLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblSolarAzimuthLabel.setText("Azimuth =");
		lblSolarAzimuthLabel.setToolTipText("");

		lblSolarElevationLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblSolarElevationLabel.setText("Elevation =");
		lblSolarElevationLabel.setToolTipText("");

		lblUVIndexLabel.setText("UVI = ");
		lblUVIndexLabel.setHorizontalAlignment(SwingConstants.LEFT);

		lblSolarLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblSolarLabel.setText("Solar Rad = ");
		lblSolarLabel.setToolTipText("");

		lblUVBRadLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblUVBRadLabel.setText("UVB = ");
		lblUVBRadLabel.setToolTipText("");

		lblUVARadLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblUVARadLabel.setText("UVA = ");
		lblUVARadLabel.setToolTipText("");

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
				.addContainerGap()	
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
					.addComponent(lblUVBRadLabel, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(lblUVARadLabel, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(lblSolarLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(lblLodExcessSecondsLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
							.addComponent(lblSolarIrradiance, GroupLayout.DEFAULT_SIZE, 65, Short.MAX_VALUE)
							.addComponent(lblLodExcessSeconds, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						.addGap(18, 18, 18)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
							.addComponent(lblSolarAzimuthLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(lblSolarElevationLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
							.addComponent(lblSolarAzimuth, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(lblSolarElevation, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
					.addGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
							.addComponent(lblUVBRad, GroupLayout.DEFAULT_SIZE, 65, Short.MAX_VALUE)
							.addComponent(lblUVARad, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
						.addComponent(lblUVIndexFlag, GroupLayout.DEFAULT_SIZE, 159, Short.MAX_VALUE)))
				.addContainerGap()));

		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
				.addGap(2, 2, 2)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addComponent(lblUVIndexFlag, GroupLayout.PREFERRED_SIZE, 41, GroupLayout.PREFERRED_SIZE)
					.addGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
							.addComponent(lblUVARadLabel)
							.addComponent(lblUVARad, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
							.addComponent(lblUVBRadLabel)
							.addComponent(lblUVBRad, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE))))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addComponent(lblSolarLabel, GroupLayout.Alignment.TRAILING)
					.addComponent(lblSolarIrradiance, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE)
					.addComponent(lblSolarAzimuthLabel, GroupLayout.Alignment.TRAILING)
					.addComponent(lblSolarAzimuth, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 16,  GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addComponent(lblLodExcessSecondsLabel)
					.addGroup(GroupLayout.Alignment.TRAILING, layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(lblLodExcessSeconds, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
							.addComponent(lblSolarElevationLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(lblSolarElevation, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE))))
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		return panel;
	}

	private JPanel getWindSpeedsPanelFoundation7InchDisplay() {
		final JPanel panel = new JPanel();

		final GroupLayout layout = new GroupLayout(panel);

		panel.setLayout(layout);

		panel.setBorder(BorderFactory.createTitledBorder(null, "Wind Speeds in " + AbstractEnvironmentSensor.windSpeedUnitSuffix.get(aes.getWindSpeedUnits()),
			TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font(DEFAULT_FONT, Font.BOLD, 12)));

		final JLabel lblWindSpeedGustingLabel = new JLabel();
		final JLabel lblWindSpeedMaxDailyLabel = new JLabel();
		final JLabel lblWindSpeedCurrentLabel = new JLabel();

		lblWindSpeedGustingLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblWindSpeedGustingLabel.setText("Gusting");
		lblWindSpeedGustingLabel.setToolTipText("");
		lblWindSpeedGustingLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));

		lblWindSpeedMaxDailyLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblWindSpeedMaxDailyLabel.setText("MaxDay");
		lblWindSpeedMaxDailyLabel.setToolTipText("");
		lblWindSpeedMaxDailyLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));

		lblWindSpeedCurrentLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblWindSpeedCurrentLabel.setText("Current");
		lblWindSpeedCurrentLabel.setToolTipText("");
		lblWindSpeedCurrentLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 10));

		lblWindSpeedCurrent.setHorizontalAlignment(SwingConstants.CENTER);
		lblWindSpeedCurrent.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 10));

		lblWindSpeedGusting.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 10));
		lblWindSpeedGusting.setHorizontalAlignment(SwingConstants.CENTER);

		lblWindSpeedMaxDaily.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 10));
		lblWindSpeedMaxDaily.setHorizontalAlignment(SwingConstants.CENTER);

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup().addContainerGap()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
					.addComponent(lblWindSpeedCurrentLabel, 52, 52, 52)
					.addComponent(lblWindSpeedCurrent, 52, 52, 52))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
					.addComponent(lblWindSpeedGustingLabel, 52, 52, 52)
					.addComponent(lblWindSpeedGusting, 52, 52, 52))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
					.addComponent(lblWindSpeedMaxDailyLabel, 52, 52, 52)
					.addComponent(lblWindSpeedMaxDaily, 52, 52, 52))
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE, false)
					.addComponent(lblWindSpeedCurrentLabel, 10, 10, 10)
					.addComponent(lblWindSpeedGustingLabel, 10, 10, 10)
					.addComponent(lblWindSpeedMaxDailyLabel, 10, 10, 10))
				.addGap(2, 2, 2)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
					.addComponent(lblWindSpeedMaxDaily, 10, 10, 10)
					.addComponent(lblWindSpeedCurrent, 10, 10, 10)
					.addComponent(lblWindSpeedGusting, GroupLayout.Alignment.TRAILING, 10, 10, 10))));

		return panel;
	}

	private JPanel getWindSpeedsPanelFullSize() {
		final JPanel panel = new JPanel();

		final GroupLayout layout = new GroupLayout(panel);

		panel.setLayout(layout);

		panel.setBorder(BorderFactory.createTitledBorder(null, "Wind Speeds in " + AbstractEnvironmentSensor.windSpeedUnitSuffix.get(aes.getWindSpeedUnits()),
			TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font(DEFAULT_FONT, Font.BOLD, 14)));

		final JLabel lblWindSpeedGustingLabel = new JLabel();
		final JLabel lblWindSpeedMaxDailyLabel = new JLabel();
		final JLabel lblWindSpeedCurrentLabel = new JLabel();

		lblWindSpeedGustingLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblWindSpeedGustingLabel.setText("Gusting");
		lblWindSpeedGustingLabel.setToolTipText("");
		lblWindSpeedGustingLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 12));

		lblWindSpeedMaxDailyLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblWindSpeedMaxDailyLabel.setText("MaxDay");
		lblWindSpeedMaxDailyLabel.setToolTipText("");
		lblWindSpeedMaxDailyLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 12));

		lblWindSpeedCurrentLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblWindSpeedCurrentLabel.setText("Current");
		lblWindSpeedCurrentLabel.setToolTipText("");
		lblWindSpeedCurrentLabel.setFont(new Font(DEFAULT_FONT, Font.BOLD, 12));

		lblWindSpeedCurrent.setHorizontalAlignment(SwingConstants.CENTER);
		lblWindSpeedCurrent.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 12));

		lblWindSpeedGusting.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 12));
		lblWindSpeedGusting.setHorizontalAlignment(SwingConstants.CENTER);

		lblWindSpeedMaxDaily.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 12));
		lblWindSpeedMaxDaily.setHorizontalAlignment(SwingConstants.CENTER);

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup().addContainerGap()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
					.addComponent(lblWindSpeedCurrentLabel, 70, 70, 70)
					.addComponent(lblWindSpeedCurrent, 70, 70, 70))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
					.addComponent(lblWindSpeedGustingLabel, 70, 70, 70)
					.addComponent(lblWindSpeedGusting, 70, 70, 70))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
					.addComponent(lblWindSpeedMaxDailyLabel, 70, 70, 70)
					.addComponent(lblWindSpeedMaxDaily, 70, 70, 70))
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE, false)
					.addComponent(lblWindSpeedCurrentLabel, 16, 16, 16)
					.addComponent(lblWindSpeedGustingLabel, 16, 16, 16)
					.addComponent(lblWindSpeedMaxDailyLabel, 16, 16, 16))
				.addGap(2, 2, 2)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
					.addComponent(lblWindSpeedMaxDaily, 16, 16, 16)
					.addComponent(lblWindSpeedCurrent, 16, 16, 16)
					.addComponent(lblWindSpeedGusting, GroupLayout.Alignment.TRAILING, 16, 16, 16))));

		return panel;
	}

	private JPanel getGraphicalUserInterface() {

		final JPanel panel = new JPanel();

		panel.setLayout(new AbsoluteLayout());

		panel.add(getEventPanel().getEventNarrativePanel(), new AbsoluteConstraints(2, 0, 360, 116));
		panel.add(swp.getNASASpaceWeatherGUI().getWarningFlagPanel(Style.SINGLE_COLUMN), new AbsoluteConstraints(2, 118, 360, 210));
		panel.add(getWindSpeedsPanelFullSize(), new AbsoluteConstraints(2, 330, 360, 80));
		
		panel.add(getPrecipitationPanel(), new AbsoluteConstraints(2, 412, 360, 190));
		panel.add(dts.getTimePanel(), new AbsoluteConstraints(2, 604, 360, 85));
		
		panel.add(getSolarRadiationPanel(), new AbsoluteConstraints(365, 0, 360, 138));
		panel.add(getAirNowPanel(), new AbsoluteConstraints(365, 143, 360, 167));
		panel.add(getSolarTimingPanelSingleColumn(), new AbsoluteConstraints(365, 312, 240, 377));

		panel.add(getWindDirectionPanel(), new AbsoluteConstraints(730, 0, 270, 290));
		panel.add(getTempPanelFullSize(), new AbsoluteConstraints(730, 292, 270, 226));

		panel.add(getBaroPressPanel(), new AbsoluteConstraints(610, 312, 115, 67));
		panel.add(getHumidityPanel(), new AbsoluteConstraints(610, 381, 115, 67));
		panel.add(getGammaRadiationPanel(), new AbsoluteConstraints(610, 450, 115, 68));
		
		panel.add(getLunarConditionsPanelWithGraphics(), new AbsoluteConstraints(610, 520, 390, 169));

		return panel;
	}

	private JPanel getGuiPanel7InchFoundationDisplay() {
		final JPanel panel = new JPanel();

		final JPanel jPnlSolarEvents = getEventPanel().getEventNarrativePanelFoundation7InchDisplay();
		final JPanel jpnlSolarEventWarnings = swp.getNASASpaceWeatherGUI().getWarningFlagPanelFoundation7InchDisplay();
		final JPanel jpnlWindDirection = compassRosePanel;
		final JPanel jpnlWindSpeeds = getWindSpeedsPanelFoundation7InchDisplay();
		final JPanel jpnlBaroPress = getBaroPressPanelFoundation7InchDisplay();
		final JPanel jpnlTime = dts.getTimePanelFoundation7InchDisplay();
		final JPanel jpnlPrecipitation = getPrecipitationPanelFoundation7InchDisplay();
		final JPanel jpnlAirQuality = getAirQualityPanelFoundation7InchDisplay();
		final JPanel jpnlTemp = getTempPanelFoundation7InchDisplay();
		final JPanel jpnlLunarConditions = getLunarConditionsPanelFoundation7InchDisplay();
		final JPanel jpnlSolarRadiation = getSolarRadiationPanelFoundation7InchDisplay();
		final JPanel jpnlHumidity = getHumidityPanelFoundation7InchDisplay();
		final JPanel jpnlAprs = getAprsPanelFoundation7InchDisplay();
		final JPanel jpnlGammaRadiation = getGammaRadiationPanelFoundation7InchDisplay();
		final JPanel jpnlRSG = swp.getRSGPanel().getPanel();

		final GroupLayout layout = new GroupLayout(panel);
		
		panel.setLayout(layout);

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
				.addGap(2, 2, 2)
				.addComponent(toolBar, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
			.addGroup(layout.createSequentialGroup()
				.addGap(2, 2, 2)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
					.addGroup(layout.createSequentialGroup()
						.addComponent(jpnlWindDirection, 136, 136, 136)
						.addGap(2, 2, 2)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
							.addComponent(jpnlWindSpeeds, 208, 208, 208)
							.addComponent(jpnlBaroPress, 208, 208, 208)))
					.addComponent(jpnlPrecipitation, 346, 346, 346)
					.addComponent(jPnlSolarEvents, 346, 346, 346)
					.addComponent(jpnlTime, 346, 346, 346))
				.addGap(2, 2, 2)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
					.addGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
							.addComponent(jpnlSolarEventWarnings, 250, 250, 250)
							.addComponent(jpnlLunarConditions, 250, 250, 250)
							.addComponent(jpnlAprs, 250, 250, 250))
						.addGap(2, 2, 2)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
							.addComponent(jpnlAirQuality, 135, 135, Short.MAX_VALUE)
							.addComponent(jpnlRSG, 135, 135, Short.MAX_VALUE)
							.addGroup(layout.createSequentialGroup()
								.addComponent(jpnlHumidity, 90, 90, 90)
								.addGap(1, 1, 1)
								.addComponent(jpnlGammaRadiation, 90, 90, Short.MAX_VALUE))
							.addComponent(jpnlTemp, 135, 135, Short.MAX_VALUE)))
					.addComponent(jpnlSolarRadiation, 360, 360, Short.MAX_VALUE)))
			.addGap(2, 2, 2));

		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
				.addGap(1, 1, 1)
				.addComponent(toolBar, 20, 20, 20)
				.addGap(1, 1, 1)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addGroup(layout.createSequentialGroup()
						.addComponent(jPnlSolarEvents, 50, 100, Short.MAX_VALUE)
						.addGap(2, 2, 2)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
							.addComponent(jpnlWindDirection, 136, 136, 136)
							.addGroup(layout.createSequentialGroup()
								.addComponent(jpnlWindSpeeds, 53, 53, 53)
								.addGap(2, 2, 2)
								.addComponent(jpnlBaroPress, 81, 81, 81)))
						.addGap(2, 2, 2)
						.addComponent(jpnlPrecipitation, 127, 127, 127)
						.addGap(2, 2, 2)
						.addComponent(jpnlTime, 55, 55, 55))
					.addGroup(layout.createSequentialGroup()                      
						.addComponent(jpnlSolarRadiation, 106, 106, 106)
						.addGap(2, 2, 2)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING) 
							.addGroup(layout.createSequentialGroup()                                      
								.addComponent(jpnlTemp, 50, 165, Short.MAX_VALUE)
								.addGap(2, 2, 2)
								.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
									.addComponent(jpnlHumidity, 40, 40, 40)
									.addComponent(jpnlGammaRadiation, 40, 40, 40))
								.addGap(2, 2, 2)
								.addComponent(jpnlAirQuality, 100, 100, 100)
								.addGap(2, 2, 2)
								.addComponent(jpnlRSG, 40, 40, 40))
							.addGroup(layout.createSequentialGroup()                           
								.addComponent(jpnlSolarEventWarnings, 50, 185, Short.MAX_VALUE)
								.addGap(2, 2, 2)
								.addComponent(jpnlLunarConditions, 118, 118, 118)
								.addGap(2, 2, 2)
								.addComponent(jpnlAprs, 40, 40, 40)))))
				.addGap(2, 2, 2)));
		
		return panel;
	}
	
	private void invokeLaterInDispatchThreadIfNeeded(Runnable runnable) {
		if (EventQueue.isDispatchThread()) {
			runnable.run();
		} else {
			SwingUtilities.invokeLater(runnable);
		}
	}

}
