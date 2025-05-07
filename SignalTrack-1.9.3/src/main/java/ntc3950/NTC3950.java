package ntc3950;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 *
 * @author John
 */
public class NTC3950 {
    private static final Logger LOG = Logger.getLogger(NTC3950.class.getName());
    
    public static final double MIN_TEMP_R = 277.2E03;
    public static final double MAX_TEMP_R = 0.0619E03;
    public static final double DEFAULT_VCC = 5.0;
    public static final double DEFAULT_R1 = 47E03;
    public static final double DEFAULT_TEMP_LOW_LIMIT = -40D;
    public static final double DEFAULT_TEMP_HIGH_LIMIT = 200D;
    public static final boolean DEFAULT_DEBUG = false;
    public static final double RT_OVER_25_AT_0C = 3.265;
    public static final double NOMINAL_RES_AT_25C = 50E3;
    public static final double RT_OVER_25_AT_50C = 0.3603;
    public static final double KELVINS_AT_0C = 273.15;
    
    private double minTempR = MIN_TEMP_R;
    private double maxTempR = MAX_TEMP_R;
    private double vcc = DEFAULT_VCC; 
    private double r1 = DEFAULT_R1;
    private double rtOver25At0C = RT_OVER_25_AT_0C;
    private double nominalResAt25C = NOMINAL_RES_AT_25C;
    private double rtOver25At50C = RT_OVER_25_AT_50C;
    private double tempLowLimit = DEFAULT_TEMP_LOW_LIMIT;
    private double tempHighLimit = DEFAULT_TEMP_HIGH_LIMIT;
    private boolean debug = DEFAULT_DEBUG;
    
    public NTC3950() {}
    
    public NTC3950(boolean debug) {
        this.debug = debug;
    }
    
    public NTC3950(String[] args) throws ParseException {
        initializeCLI(args);
    }

    private void initializeCLI(String[] args) throws ParseException {
        final Options options = new Options();
        final CommandLineParser parser = new DefaultParser();
        options.addOption("v", "vcc", true, "Voltage Across R1 and thermistor");
        options.addOption("r", "r1", true, "Resistance of series resistor");
        options.addOption("rt250c", true, "RT/25 @ 0 degrees C");
        options.addOption("r25k", true, "Resistance @ 25 degrees C");
        options.addOption("rt2550c", true, "RT/25 @ 50 degrees C");
        options.addOption("d", "debug", false, "Debug Mode");
        final CommandLine cmd = parser.parse(options, args);
        if (cmd.hasOption("v")) {
            vcc = Double.parseDouble(cmd.getOptionValue("v"));
        }
        if (cmd.hasOption("r")) {
            r1 = Double.parseDouble(cmd.getOptionValue("r"));
        }
        if (cmd.hasOption("d")) {
            debug = true;
        }
        if (cmd.hasOption("rt250c")) {
            rtOver25At0C = Double.parseDouble(cmd.getOptionValue("rt250c"));
        }
        if (cmd.hasOption("rt2550c")) {
            rtOver25At50C = Double.parseDouble(cmd.getOptionValue("rt2550c"));
        }
        if (cmd.hasOption("r25k")) {
            nominalResAt25C = Double.parseDouble(cmd.getOptionValue("r25k"));
        }
    }
  
    public double getTempCelsiusLinear(double vNTC) {
        double maxIatMinTemp = vcc / (r1 + minTempR); 
        double minIatMaxTemp = vcc / (r1 + maxTempR);
        double tempHighLimitVoltMeasure;
        double tempLowLimitVoltMeasure;
        
        tempLowLimitVoltMeasure = maxIatMinTemp * minTempR;
        tempHighLimitVoltMeasure = minIatMaxTemp * maxTempR;
        
        boolean isInverted = tempLowLimitVoltMeasure >= tempHighLimitVoltMeasure;

        if (debug) LOG.log(Level.INFO, "vActual: {0}", vNTC);
        
        double vSpanAbs = Math.abs(tempHighLimitVoltMeasure - tempLowLimitVoltMeasure);
        if (debug) LOG.log(Level.INFO, "vSpanAbs: {0}", vSpanAbs);
        
        double vToLowRail = vNTC - Math.min(tempHighLimitVoltMeasure, tempLowLimitVoltMeasure);
        if (debug) LOG.log(Level.INFO, "vToLowRail: {0}", vToLowRail);

        double pctOfVSpan = vToLowRail / vSpanAbs;
        if (debug) LOG.log(Level.INFO, "pctOfVSpan: {0}", pctOfVSpan);
        
        double tSpanAbs = Math.abs(tempLowLimit - tempHighLimit);
        if (debug) LOG.log(Level.INFO, "tSpanAbs: {0}", tSpanAbs);
        
        double b = pctOfVSpan * tSpanAbs;
        if (debug) LOG.log(Level.INFO, "b: {0}", b);
        
        if (isInverted) {
            return tempHighLimit - b;
        } else {
            return tempLowLimit + b ;
        }
    }

    public double celsiusToKelvins(double c) {
        return c + KELVINS_AT_0C;
    }
    
    public double kelvinsToCelsius(double k) {
        return k - KELVINS_AT_0C;
    }
    
    public double getTempCelsius(double vNTC) {
        double vr1 = vcc - vNTC;
        double i = vr1 / (r1);
        return getSteinhartHartTempC(vNTC / i);
    }

    public double getSteinhartHartTempC(double R) {
        double R1 = rtOver25At0C * nominalResAt25C;
        double R2 = nominalResAt25C;
        double R3 = rtOver25At50C * nominalResAt25C;
        double L1 = Math.log(R1);
        double L2 = Math.log(R2);
        double L3 = Math.log(R3);
        double Y1 = 1 / celsiusToKelvins(0);
        double Y2 = 1 / celsiusToKelvins(25);
        double Y3 = 1 / celsiusToKelvins(50);
        double y2 = (Y2 - Y1) / (L2 - L1);
        double y3 = (Y3 - Y1) / (L3 - L1);
        double C = ((y3 - y2) / (L3 - L2)) * Math.pow((L1 + L2 + L3), -1.0);
        double B = y2 - (C * (Math.pow(L1,2) + (L1 * L2) + Math.pow(L2,2)));
        double A = Y1 - (L1 * (B + (C * Math.pow(L1,2))));
        
        double T = A + (B * Math.log(R)) + (C * Math.pow(Math.log(R),3));
        
        return kelvinsToCelsius(1.0/T);
    }

    public double getRtOver25At0C() {
        return rtOver25At0C;
    }

    public void setRtOver25At0C(double rtOver25At0C) {
        this.rtOver25At0C = rtOver25At0C;
    }

    public double getNominalResAt25C() {
        return nominalResAt25C;
    }

    public void setNominalResAt25C(double nominalResAt25C) {
        this.nominalResAt25C = nominalResAt25C;
    }

    public double getRtOver25At50C() {
        return rtOver25At50C;
    }

    public void setRtOver25At50C(double rtOver25At50C) {
        this.rtOver25At50C = rtOver25At50C;
    }
    
    
    public double getTempFahrenheit(double volts) {
        return celsiusToFahrenheit(getTempCelsius(volts));
    }

    public static double celsiusToFahrenheit(double celsius) {
        return (celsius * 9D / 5D) + 32D;
    }

    public double getMinTempR() {
        return minTempR;
    }

    public void setMinTempR(double minTempR) {
        this.minTempR = minTempR;
    }

    public double getMaxTempR() {
        return maxTempR;
    }

    public void setMaxTempR(double maxTempR) {
        this.maxTempR = maxTempR;
    }

    public double getVcc() {
        return vcc;
    }

    public void setVcc(double vcc) {
        this.vcc = vcc;
    }

    public double getR1() {
        return r1;
    }

    public void setR1(double r1) {
        this.r1 = r1;
    }

    public double getTempLowLimit() {
        return tempLowLimit;
    }

    public void setTempLowLimit(double tempLowLimit) {
        this.tempLowLimit = tempLowLimit;
    }

    public double getTempHighLimit() {
        return tempHighLimit;
    }

    public void setTempHighLimit(double tempHighLimit) {
        this.tempHighLimit = tempHighLimit;
    }
    
    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}
