package radio;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.TreeMap;

// Utility class to contain an ITU emission designator.
// Any valid emission designator may be created in the constructor.

public class EmissionDesignator implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger.getLogger(EmissionDesignator.class.getName());

    private static TreeMap<String, String> modes = new TreeMap<>();
    
    public enum BandwidthUnits {
        H, K, M, G, NULL
    }

    public enum ModulationTypes {
        N, A, H, R, J, B, C, F, G, D, P, K, L, M, Q, V, W, X, NULL
    }

    public enum SignalTypes {
        S0, S1, S2, S3, S7, S8, S9, SX, NULL
    }

    public enum InformationTypes {
        N, A, B, C, D, E, F, W, X, NULL
    }

    public enum InformationDetails {
        A, B, C, D, E, F, G, H, J, K, L, M, N, W, X, Y, Z, NULL
    }

    public enum MultiplexingTypes {
        N, C, F, T, W, X, NULL
    }

    private BandwidthUnits bandwidthUnit = BandwidthUnits.NULL;
    private String bandwidthString;
    private ModulationTypes modulationType = ModulationTypes.NULL;
    private SignalTypes signalType = SignalTypes.NULL;
    private InformationTypes informationType = InformationTypes.NULL;
    private InformationDetails informationDetail = InformationDetails.NULL;
    private MultiplexingTypes multiplexingType = MultiplexingTypes.NULL;
    
    private double bandwidthMultiplier;

    private final EnumMap<BandwidthUnits, String> bandwidthUnits = new EnumMap<>(BandwidthUnits.class);
    private final EnumMap<ModulationTypes, String> modulationTypes = new EnumMap<>(ModulationTypes.class);
    private final EnumMap<SignalTypes, String> signalTypes = new EnumMap<>(SignalTypes.class);
    private final EnumMap<InformationTypes, String> informationTypes = new EnumMap<>(InformationTypes.class);
    private final EnumMap<InformationDetails, String> informationDetails = new EnumMap<>(InformationDetails.class);
    private final EnumMap<MultiplexingTypes, String> multiplexingTypes = new EnumMap<>(MultiplexingTypes.class);

    public EmissionDesignator(String bandwidthString, ModulationTypes modulationType,
            SignalTypes signalType, InformationTypes informationType) {
        this(bandwidthString, modulationType, signalType, informationType,
                InformationDetails.NULL, MultiplexingTypes.NULL);
    }

    public EmissionDesignator(String bandwidthString, ModulationTypes modulationType,
            SignalTypes signalType, InformationTypes informationType, InformationDetails informationDetail,
            MultiplexingTypes multiplexingType) {
        this.bandwidthString = bandwidthString;
        this.modulationType = modulationType;
        this.signalType = signalType;
        this.informationType = informationType;
        this.informationDetail = informationDetail;
        this.multiplexingType = multiplexingType;
        this.bandwidthUnit = getBandwidthUnit(bandwidthString);
        loadMaps();
    }

    public BandwidthUnits getBandwidthUnit() {
    	return getBandwidthUnit(bandwidthString);
    }
    
    private BandwidthUnits getBandwidthUnit(String bandwidthString) {
        BandwidthUnits bwu = null;
        
        int i = bandwidthString.indexOf('H');
        if (i == -1) {
            i = bandwidthString.indexOf('K');
        }
        if (i == -1) {
            i = bandwidthString.indexOf('M');
        }
        if (i == -1) {
            i = bandwidthString.indexOf('G');
        }
        
        if ((i >= 1) && (i <= 3)) {
            final String bwchar = bandwidthString.substring(i, i + 1);
            switch (bwchar) {
                case "H" ->
                    bwu = BandwidthUnits.H;
                case "K" ->
                    bwu = BandwidthUnits.K;
                case "M" ->
                    bwu = BandwidthUnits.M;
                case "G" ->
                    bwu = BandwidthUnits.G;
                default -> 
                	bwu = BandwidthUnits.NULL;
            }
        }

        return bwu;
    }

    public long getBandwidthHz() {
    	return (long) (getBandwidthNumerical(bandwidthString) * getBandwidthMultiplicationFactor(bandwidthString));
    }
    
    private double getBandwidthMultiplicationFactor(String bandwidthString) {
        double d = 1.0;
        
        int i = bandwidthString.indexOf('H');
        
        if (i == -1) {
            i = bandwidthString.indexOf('K');
        }
        if (i == -1) {
            i = bandwidthString.indexOf('M');
        }
        if (i == -1) {
            i = bandwidthString.indexOf('G');
        }

        if ((i >= 1) && (i <= 3)) {
            final String bwchar = bandwidthString.substring(i, i + 1);
            switch (bwchar) {
                case "H":
                    d = 1.0;
                    break;
                case "K":
                    d = 1E3;
                    break;
                case "M":
                    d = 1E6;
                    break;
                case "G":
                    d = 1E9;
                    break;
                default:
                    break;
            }
        }

        return d;
    }

    private double getBandwidthNumerical(String bandwidthString) {
    	final String zeroLengthString = "";
    	final String validNumbers = "\\d+";
    	String[] n = bandwidthString.split(zeroLengthString);
        StringBuilder f = new StringBuilder();
        
        for (int i = 0; i < n.length; i++) {
            if((n[i].matches(validNumbers))) {
                f.append(n[i]);
            } else {
            	f.append(".");
            }
        }
        
        return Double.parseDouble(f.toString());
    }

    private void loadMaps() {
        bandwidthUnits.put(BandwidthUnits.H, "Hertz");
        bandwidthUnits.put(BandwidthUnits.K, "Kilohertz");
        bandwidthUnits.put(BandwidthUnits.M, "Megahertz");
        bandwidthUnits.put(BandwidthUnits.G, "Gigahertz");

        modulationTypes.put(ModulationTypes.N, "Unmodulated carrier");
        modulationTypes.put(ModulationTypes.A, "Double-sideband amplitude modulation");
        modulationTypes.put(ModulationTypes.H, "Single-sideband with full carrier");
        modulationTypes.put(ModulationTypes.R, "Single-sideband with reduced or variable carrier");
        modulationTypes.put(ModulationTypes.J, "Single-sideband with suppressed carrier");
        modulationTypes.put(ModulationTypes.B, "Independent sideband");
        modulationTypes.put(ModulationTypes.C, "Vestigial sideband");
        modulationTypes.put(ModulationTypes.F, "Frequency modulation");
        modulationTypes.put(ModulationTypes.G, "Phase modulation");
        modulationTypes.put(ModulationTypes.D, "Combination of AM and FM or PM");
        modulationTypes.put(ModulationTypes.P, "Sequence of pulses without modulation");
        modulationTypes.put(ModulationTypes.K, "Pulse amplitude modulation");
        modulationTypes.put(ModulationTypes.L, "Pulse width modulation");
        modulationTypes.put(ModulationTypes.M, "Pulse position modulation");
        modulationTypes.put(ModulationTypes.Q, "Sequence of pulses, phase or frequency modulation within each pulse");
        modulationTypes.put(ModulationTypes.V, "Combination of pulse modulation methods");
        modulationTypes.put(ModulationTypes.W, "Combination of any of the above");
        modulationTypes.put(ModulationTypes.X, "None of the above");

        signalTypes.put(SignalTypes.S0, "No modulating signal");
        signalTypes.put(SignalTypes.S1, "One channel containing digital information, no subcarrier");
        signalTypes.put(SignalTypes.S2, "One channel containing digital information, using a subcarrier");
        signalTypes.put(SignalTypes.S3, "One channel containing analogue information");
        signalTypes.put(SignalTypes.S7, "More than one channel containing digital information");
        signalTypes.put(SignalTypes.S8, "More than one channel containing analogue information");
        signalTypes.put(SignalTypes.S9, "Combination of analogue and digital channels");
        signalTypes.put(SignalTypes.SX, "None of the above");
        signalTypes.put(SignalTypes.NULL, "No Information");

        informationTypes.put(InformationTypes.N, "No transmitted information");
        informationTypes.put(InformationTypes.A, "Aural telegraphy, intended to be decoded by ear, such as Morse code");
        informationTypes.put(InformationTypes.B, "Electronic telegraphy, intended to be decoded by machine (radioteletype and digital modes)");
        informationTypes.put(InformationTypes.C, "Facsimile (still images)");
        informationTypes.put(InformationTypes.D, "Data transmission, telemetry or telecommand (remote control)");
        informationTypes.put(InformationTypes.E, "Telephony (voice or music intended to be listened to by a human)");
        informationTypes.put(InformationTypes.F, "Video (television signals)");
        informationTypes.put(InformationTypes.W, "Combination of any of the above");
        informationTypes.put(InformationTypes.X, "None of the above");
        informationTypes.put(InformationTypes.NULL, "No Information");

        informationDetails.put(InformationDetails.A, "Two-condition code, elements vary in quantity and duration");
        informationDetails.put(InformationDetails.B, "Two-condition code, elements fixed in quantity and duration");
        informationDetails.put(InformationDetails.C, "Two-condition code, elements fixed in quantity and duration, error-correction included");
        informationDetails.put(InformationDetails.D, "Four-condition code, one condition per signal element");
        informationDetails.put(InformationDetails.E, "Multi-condition code, one condition per signal element");
        informationDetails.put(InformationDetails.F, "Multi-condition code, one character represented by one or more conditions");
        informationDetails.put(InformationDetails.G, "Monophonic broadcast-quality sound");
        informationDetails.put(InformationDetails.H, "Stereophonic or quadraphonic broadcast-quality sound");
        informationDetails.put(InformationDetails.J, "Commercial-quality sound (non-broadcast)");
        informationDetails.put(InformationDetails.K, "Commercial-quality soundâ€�?frequency inversion and-or band-splitting employed");
        informationDetails.put(InformationDetails.L, "Commercial-quality sound, independent FM signals, such as pilot tones, used to control the demodulated signal");
        informationDetails.put(InformationDetails.M, "Greyscale images or video");
        informationDetails.put(InformationDetails.N, "Full-color images or video");
        informationDetails.put(InformationDetails.W, "Combination of two or more of the above");
        informationDetails.put(InformationDetails.X, "None of the above");
        informationDetails.put(InformationDetails.Y, "Upper - Non-Standard");
        informationDetails.put(InformationDetails.Z, "Lower - Non-Standard");
        informationDetails.put(InformationDetails.NULL, "No Information");

        multiplexingTypes.put(MultiplexingTypes.N, "None used");
        multiplexingTypes.put(MultiplexingTypes.C, "Code-division (excluding spread spectrum)");
        multiplexingTypes.put(MultiplexingTypes.F, "Frequency-division");
        multiplexingTypes.put(MultiplexingTypes.T, "Time-division");
        multiplexingTypes.put(MultiplexingTypes.W, "Combination of Frequency-division and Time-division");
        multiplexingTypes.put(MultiplexingTypes.X, "None of the above");
        multiplexingTypes.put(MultiplexingTypes.NULL, "No Information");

        modes.put("60H0J2B", "PSK31");
        modes.put("100HN0N", "Speed Radar (10525 MHz X band; 24150 MHz Ka band)");
        modes.put("150HA1A", "Continuous Wave Telegraphy (manually read Morse Code)");
        modes.put("500HJ2D", "MT63-500 50 WPM");
        modes.put("1K00J2D", "MT63-1000 100 WPM");
        modes.put("2K00J2D", "MT63-2000 200 WPM");
        modes.put("2K80J2B", "HF RTTY (Radio Teletype)");
        modes.put("2K80J2D", "HF PACTOR-III");
        modes.put("2K80J3E", "Amplitude modulated (AM) analog voice, single sideband suppressed carrier");
        modes.put("2K80J3EY", "Amplitude modulated (AM) analog voice, upper sideband suppressed carrier");
        modes.put("2K80J3EZ", "Amplitude modulated (AM) analog voice, lower sideband suppressed carrier");
        modes.put("3K00H2B", "HF ALE MIL-STD-188-141A/FED-STD-1045");
        modes.put("3K30F1D", "6.25 kHz SCADA link (CalAmp Viper SC â€“ 173 MHz)");
        modes.put("4K00F1D", "NXDN 6.25 kHz data (IDAS, NEXEDGE)");
        modes.put("4K00F1E", "NXDN 6.25 kHz digital voice (IDAS, NEXEDGE)");
        modes.put("4K00F1W", "NXDN 6.25 kHz digital voice and data (IDAS, NEXEDGE)");
        modes.put("4K00F2D", "NXDN 6.25 kHz analog FM CW ID (IDAS, NEXEDGE)");
        modes.put("4K00J1D", "Amplitude Compandored Sideband (pilot tone/carrier)");
        modes.put("4K00J2D", "Amplitude Compandored Sideband (pilot tone/carrier)");
        modes.put("4K00J3E", "Amplitude Compandored Sideband (pilot tone/carrier) voice");
        modes.put("5K60F2D", "SCADA");
        modes.put("5K76G1E", "P25 CQPSK voice (typically used for simulcast systems â€“ this is NOT P25 Phase II)");
        modes.put("6K00A3E", "Amplitude modulated (AM) analog voice, double sideband full carrier");
        modes.put("6K00F1D", "SCADA Carrier Frequency Shift Keying");
        modes.put("6K00F2D", "SCADA Audio Frequency Shift Keying");
        modes.put("6K00F3D", "SCADA DTMF / QuickCall Analog data (not AFSK)");
        modes.put("6K50F1D", "SCADA/Data 4.8 GFSK in 12.5 kHz channel space (LMR used by CalFire for AVL)");
        modes.put("6K00F7W", "D-STAR");
        modes.put("7K60FXD", "2-slot DMR (Motorola MOTOTRBO) TDMA data");
        modes.put("7K60FXE", "2-slot DMR (Motorola MOTOTRBO) TDMA voice");
        modes.put("8K10F1D", "P25 Phase I C4FM data");
        modes.put("8K10F1E", "P25 Phase I C4FM voice");
        modes.put("8K10F1W", "P25 Phase II subscriber units (Harmonized Continuous Phase Modulation â€“ H-CPM)");
        modes.put("8K30F1D", "NXDN 12.5 kHz data (Wide IDAS, NEXEDGE)");
        modes.put("8K30F1E", "NXDN 12.5 kHz digital voice (Wide IDAS, NEXEDGE)");
        modes.put("8K30F1W", "P25 Phase I C4FM hybridized voice and data applications");
        modes.put("8K30F7W", "NXDN 12.5 kHz digital voice and data (Wide IDAS, NEXEDGE)");
        modes.put("8K50F9W", "Harris OpenSky (2 slot narrow band)");
        modes.put("8K70D1W", "P25 Linear Simulcast Modulation ASTRO (9.6 kbps in 12.5 kHz channel space)");
        modes.put("9K20F2D", "Zetron-based alphanumeric paging/alerting system");
        modes.put("9K30F1D", "SCADA/ Remote Control");
        modes.put("9K36F7W", "Yaesu System Fusion C4FM (Voice Wide * Voice Narrow + Data * Data Wide)");
        modes.put("9K70F1D", "P25 Linear Simulcast Modulation WCQPSK data");
        modes.put("9K70F1E", "P25 Linear Simulcast Modulation WCQPSK voice");
        modes.put("9K80D7W", "P25 Phase II fixed-end 2-slot TDMA (Harmonized Differential Quadrature Phase Shift Keyed modulation â€“ H-DQPSK)");
        modes.put("9K80F1D", "P25 Phase II fixed-end 2-slot TDMA H-DQPSK data");
        modes.put("9K80F1E", "P25 Phase II fixed-end 2-slot TDMA H-DQPSK voice");
        modes.put("10K0F1D", "Motorola Widepulse ASTRO simulcast data");
        modes.put("10K0F1E", "Motorola Widepulse ASTRO simulcast voice");
        modes.put("11K0F1D", "Narrowband data, type of data not specified");
        modes.put("11K0F3E", "Narrowband analog voice, considered by the FCC to be identical to 11K2F3E");
        modes.put("11K2F1D", "POCSAG paging (narrowbanded, i.e., Swissphone alerting)");
        modes.put("11K2F2D", "Frequency modulated (FM) 2.5 kHz deviation audio frequency shift keying within a 12.5 kHz channelspace (commonly used for 1.2 kbps packet, FFSK station alerting, and AFSK outdoor warning siren signaling)");
        modes.put("11K2F3D", "Frequency modulated (FM) 2.5 kHz deviation DTMF or other audible, non-frequency shift signaling, such as Whelen outdoor warning sirens or â€œKnox-BoxÂ®â€� activation");
        modes.put("11K2F3E", "Frequency modulated (FM) 2.5 kHz deviation analog voice");
        modes.put("12K1F9W", "Harris OpenSky (NPSPAC - 4 slot)");
        modes.put("13K1F9W", "Harris OpenSky (SMR - 4 slot)");
        modes.put("13K6F3E", "Frequency modulated (FM) analog voice, 3.8 kHz deviation (900 MHz)");
        modes.put("13K6W7W", "Motorola iDEN (900 MHz)");
        modes.put("14K0F1D", "Motorola 3600 baud trunked control channel (NPSPAC)");
        modes.put("16K0F1D", "Motorola 3600 baud trunked control channel");
        modes.put("16K0F2D", "4 kHz deviation FM audio frequency shift keying (72 MHz fire alarm boxes)");
        modes.put("16K0F3E", "Frequency modulated (FM) analog voice, 4 kHz deviation (NPSPAC); (FM mode in RadioReference.com Database)");
        modes.put("16K0G1D", "EPIRB (406 MHz)");
        modes.put("16K8F1E", "Encrypted Quantized Voice (Motorola DVP, DES, DES-XL on NPSPAC)");
        modes.put("17K7D7D", "Motorola HPD High Performance Data â€“ Astro 25 suite, as Motorola HAI (High performance data Air Interface) â€“ 700/800 MHz â€“ requires 25 kHz channelspace");
        modes.put("20K0D1E", "Reduced power TETRA â€“ PowerTrunk 4/TDMA fixed-end (voice)");
        modes.put("20K0D1W", "Reduced power TETRA â€“ PowerTrunk 4/TDMA fixed-end (simultaneous mixed modes)");
        modes.put("20K0D7D", "Reduced power TETRA (data)");
        modes.put("20K0D7E", "Reduced power TETRA (voice)");
        modes.put("20K0D7W", "Reduced power TETRA (simultaneous mixed modes)");
        modes.put("20K0F1D", "RD-LAP 19.2 kbps within a wideband channel (2013 compliant, meets data throughput requirement)");
        modes.put("20K0F1E", "Encrypted Quantized Voice (Motorola DVP, DES, DES-XL - NOT P25 DES-OFB/AES)");
        modes.put("20K0F3D", "Frequency modulated (FM) 5 kHz deviation DTMF or other audible, non-frequency shift signaling, such as Whelen outdoor warning sirens or â€œKnox-BoxÂ®â€� activation");
        modes.put("20K0F3E", "Frequency modulated (FM) analog voice, 5 kHz deviation; wideband 25 kHz");
        modes.put("20K0G7W", "Motorola iDEN (800 MHz)");
        modes.put("20K0W7W", "Motorola iDEN (800 MHz)");
        modes.put("20K1D1D", "Reduced power TETRA â€“ PowerTrunk 4/TDMA fixed-end (data)");
        modes.put("21K0D1W", "TETRA ETS 300 392 Standard");
        modes.put("22K0D7D", "TETRA (data)");
        modes.put("22K0D7E", "TETRA (voice)");
        modes.put("22K0D7W", "TETRA (simultaneous mixed modes)");
        modes.put("22K0DXW", "TETRA Subscriber Units (mobiles and control stations)");
        modes.put("30K0DXW", "TDMA Cellular (North America)");
        modes.put("40K0F8W", "AMPS Cellular");
        modes.put("41K7Q7W", "Iridium satellite terminals (1.616-1.626 GHz)");
        modes.put("41K7V7W", "Iridium satellite terminals (1.616-1.626 GHz)");
        modes.put("55K0P0N", "CODAR oceanographic RADAR (swooping signals on HF with approx. 1 second sweep time) 3.5 - 5 MHz");
        modes.put("100KC3F", "ReconRobotics surveillance robot video (430-450 MHz)");
        modes.put("100KP0N", "CODAR oceanographic RADAR (swooping signals on HF with approx. 1 second sweep time) 12 - 14 MHz");
        modes.put("170KP0N", "CODAR oceanographic RADAR above 24 MHz");
        modes.put("200KF8E", "Broadcast FM with Subsidiary Communications Subcarrier");
        modes.put("250KF3E", "Television Broadcast Audio (NTSC analog)");
        modes.put("300KG7W", "EDGE (Enhanced Data rates for GSM Evolution)");
        modes.put("300KGXW", "GSM Cellular");
        modes.put("500KD7W", "Broadcast Radio Digital Studio to Transmitter Link 2048 kbps 32 QAM");
        modes.put("500KF8W", "Broadcast Radio Analog Studio to Transmitter Link");
        modes.put("1M25F9W", "CDMA Cellular");
        modes.put("2M40W7D", "Remote Control Video (digital, non-NTSC)");
        modes.put("3M00W7W", "SouthernLinc LTE (all four emissions used) 3 MHz bandwidth");
        modes.put("5M00G7D", "Public Safety LTE (all four emissions used) 5 MHz bandwidth");
        modes.put("5M00W7W", "Public Safety LTE (all four emissions used) 5 MHz bandwidth");
        modes.put("5M00G2D", "Public Safety LTE (all four emissions used) 5 MHz bandwidth");
        modes.put("5M00D7D", "Public Safety LTE (all four emissions used) 5 MHz bandwidth");
        modes.put("5M75C3F", "Television, NTSC analog video (with 250K0F3E aural carrier)");
        modes.put("6M00C7W", "Television, ATSC Digital TV (video and audio)");
        modes.put("10M0G2D", "Public Safety LTE (all four emissions used) 10 MHz bandwidth");
        modes.put("10M0W7W", "Public Safety LTE (all four emissions used) 10 MHz bandwidth");
        modes.put("10M0D7D", "Public Safety LTE (all four emissions used) 10 MHz bandwidth");
        modes.put("10M0G7D", "Public Safety LTE (all four emissions used) 10 MHz bandwidth");
        modes.put("30M0D7W", "Microwave Link Transmitter using 2048 QAM in 30 MHz bandwidth");
        modes.put("42M6D7W", "Microwave Link Transmitter QPSK");
        modes.put("45M2D7W", "Microwave Link Transmitter 16 QAM 45 MHz");
        modes.put("45M8D7W", "Microwave Link Transmitter 32 QAM 45 MHz");
        modes.put("47M8D7W", "Microwave Link Transmitter 128 QAM 47 MHz");
        modes.put("47M1D7W", "Microwave Link Transmitter 256 QAM 47 MHz");
        modes.put("14K0F3E", "EDACS Analog Voice (NPSPAC)");
    }

    public SignalTypes getSignalType() {
        return signalType;
    }

    public ModulationTypes getModulationType() {
        return modulationType;
    }

    public InformationTypes getInformationType() {
        return informationType;
    }

    public InformationDetails getInformationDetail() {
        return informationDetail;
    }

    public MultiplexingTypes getMultiplexingType() {
        return multiplexingType;
    }

    public String getBandwidthUnitValue() {
        return bandwidthUnits.get(bandwidthUnit);
    }

    public String getSignalTypeValue() {
        return signalTypes.get(signalType).substring(1, 2);
    }

    public String getModulationTypeValue() {
        return modulationTypes.get(modulationType);
    }

    public String getInformationTypeValue() {
        return informationTypes.get(informationType);
    }

    public String getInformationDetailValue() {
        return informationDetails.get(informationDetail);
    }

    public String getMultiplexingTypeValue() {
        return multiplexingTypes.get(multiplexingType);
    }

    public static String[] getModeEntrySet() {
        final String[] list = new String[modes.size()];
        int i = 0;
        final Iterator<Entry<String, String>> it = modes.entrySet().iterator();
        while (it.hasNext()) {
            final Entry<String, String> pairs = it.next();
            list[i] = pairs.getKey() + "  " + pairs.getValue();
            i++;
            it.remove();
        }
        return list;
    }

    public String getITUEmissionDesignator() {
    	String str = null;
    	try {
	        str = bandwidthString + modulationType.toString() + signalType.toString().substring(1);
	        if (informationType != null) {
	        	str += informationType.toString();
	        }
	        if (!"NULL".equals(informationDetail.toString())) {
	            str += informationDetail.toString();
	            if (!"NULL".equals(multiplexingType.toString())) {
	                str += multiplexingType.toString();
	            }
	        }
    	} catch (NullPointerException ex) {
    		LOG.log(Level.SEVERE, "An invalid EmissionDesignator() instance has been instantianted....", ex);
    	}
        return str;
    }

    public String getModeDescription() {
        return modes.get(getITUEmissionDesignator());
    }

    public int getBandWidthHz() {
        String s = getITUEmissionDesignator().substring(0, 4);
        return (int) (Double.parseDouble(s) * bandwidthMultiplier);
    }

}
