package radio;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.TreeMap;

// Utility class to contain an ITU emission designator.
// Any valid emission designator may be created in the constructor.

public class ITUEmissionDesignator {
    private static final Logger LOG = Logger.getLogger(ITUEmissionDesignator.class.getName());

    private static final TreeMap<String, String> standardModes = new TreeMap<>();

    public enum ModulationType {
        N("Unmodulated Carrier"),
    	A("Double-Sideband Amplitude Modulation"),
        H("Single-sideband with full carrier"),
        R("Single-sideband with reduced or variable carrier"),
        J("Single-sideband with suppressed carrier"),
        B("Independent sideband"),
        C("Vestigial sideband"),
        F("Frequency modulation"),
        G("Phase modulation"),
        D("Combination of AM and FM or PM"),
        P("Sequence of pulses without modulation"),
        K("Pulse amplitude modulation"),
        L("Pulse width modulation"),
        M("Pulse position modulation"),
        Q("Sequence of pulses(phase or frequency modulation within each pulse"),
        V("Combination of pulse modulation methods"),
        W("Combination of any of the above"),
        X("Cases Not Otherwise Covered");
        
        private String description;
        
        private ModulationType(String description) {
        	this.description = description;
        }
        
        public String getDescription() {
        	return description;
        }
    }

    public enum SignalType {
        S0("0", "No modulating signal"),
        S1("1", "One channel containing digital information(no subcarrier"),
        S2("2", "One channel containing digital information(using a subcarrier"),
        S3("3", "One channel containing analogue information"),
        S7("7", "More than one channel containing digital information"),
        S8("8", "More than one channel containing analogue information"),
        S9("9", "Combination of analogue and digital channels"),
    	SX("X", "Cases Not Otherwise Covered");
        
    	private String typeCode;
        private String description;
        
        private SignalType(String typeCode, String description) {
        	this.typeCode = typeCode;
        	this.description = description;
        }
        
        public String getDescription() {
        	return description;
        }
        
        public String getTypeCode() {
        	return typeCode;
        }
    }

    public enum InformationType {
        N("No transmitted information"),
        A("Aural telegraphy(intended to be decoded by ear(such as Morse code"),
        B("Electronic telegraphy(intended to be decoded by machine (radioteletype and digital modes)"),
        C("Facsimile (still images)"),
        D("Data transmission(telemetry or telecommand (remote control)"),
        E("Telephony (voice or music intended to be listened to by a human)"),
        F("Video (television signals)"),
        W("Combination of any of the above"),
        X("Cases Not Otherwise Covered");
        
        private String description;
        
        private InformationType(String description) {
        	this.description = description;
        }
        
        public String getDescription() {
        	return description;
        }
    }

    public enum DetailsOfSignal {
        A("Two-condition code(elements vary in quantity and duration"),
        B("Two-condition code(elements fixed in quantity and duration"),
        C("Two-condition code(elements fixed in quantity and duration(error-correction included"),
        D("Four-condition code(one condition per signal element"),
        E("Multi-condition code(one condition per signal element"),
        F("Multi-condition code(one character represented by one or more conditions"),
        G("Monophonic broadcast-quality sound"),
        H("Stereophonic or quadraphonic broadcast-quality sound"),
        J("Commercial-quality sound (non-broadcast)"),
        K("Commercial-quality soundâ€�?frequency inversion and-or band-splitting employed"),
        L("Commercial-quality sound(independent FM signals(such as pilot tones(used to control the demodulated signal"),
        M("Greyscale images or video"),
        N("Full-color images or video"),
        W("Combination of two or more of the above"),
        X("Cases Not Otherwise Covered"),
        NULL("Information Not Provided");
        
        private String description;
        
        private DetailsOfSignal(String description) {
        	this.description = description;
        }
        
        public String getDescription() {
        	return description;
        }
    }

    public enum MultiplexingType {
    	N("None used"),
        C("Code-division (excluding spread spectrum)"),
        F("Frequency-division"),
        T("Time-division"),
        W("Combination of Frequency-division and Time-division"),
        X("Other Types of Multiplexing"),
        NULL("Information Not Provided");
        
        private String description;
        
        private MultiplexingType(String description) {
        	this.description = description;
        }
        
        public String getDescription() {
        	return description;
        }
    }

    private String bandwidthString;
    private ModulationType modulationType;
    private SignalType signalType;
    private InformationType informationType;
    private DetailsOfSignal detailsOfSignal;
    private MultiplexingType multiplexingType;
  
    public ITUEmissionDesignator(String bandwidthString, radio.ITUEmissionDesignator.ModulationType modulationType,
            radio.ITUEmissionDesignator.SignalType signalType, radio.ITUEmissionDesignator.InformationType informationType) {
        this(bandwidthString, modulationType, signalType, informationType,
            radio.ITUEmissionDesignator.DetailsOfSignal.NULL, radio.ITUEmissionDesignator.MultiplexingType.NULL);
    }

    public ITUEmissionDesignator(String bandwidthString, radio.ITUEmissionDesignator.ModulationType modulationType,
            radio.ITUEmissionDesignator.SignalType signalType, radio.ITUEmissionDesignator.InformationType informationType, 
            radio.ITUEmissionDesignator.DetailsOfSignal detailsOfSignal, 
            radio.ITUEmissionDesignator.MultiplexingType multiplexingType) {
        this.bandwidthString = bandwidthString;
        this.modulationType = modulationType;
        this.signalType = signalType;
        this.informationType = informationType;
        this.detailsOfSignal = detailsOfSignal;
        this.multiplexingType = multiplexingType;
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
				case "H" -> d = 1.0;
				case "K" -> d = 1E3;
				case "M" -> d = 1E6;
				case "G" -> d = 1E9;
				default -> d = 0.1;
			}
        }

        return d;
    }

    private double getBandwidthNumerical(String bandwidthString) {
    	final String zeroLengthString = "";
    	final String validNumbers = "\\d+";
    	final String[] n = bandwidthString.split(zeroLengthString);
        final StringBuilder f = new StringBuilder();
        
        for (String aN : n) {
            if((aN.matches(validNumbers))) {
                f.append(aN);
            } else {
            	f.append(".");
            }
        }
        
        return Double.parseDouble(f.toString());
    }

    private static void loadMaps() {
        standardModes.put("60H0J2B", "PSK31");
        standardModes.put("100HN0N", "Speed Radar (10525 MHz X band; 24150 MHz Ka band)");
        standardModes.put("150HA1A", "Continuous Wave Telegraphy (manually read Morse Code)");
        standardModes.put("500HJ2D", "MT63-500 50 WPM");
        standardModes.put("1K00J2D", "MT63-1000 100 WPM");
        standardModes.put("2K00J2D", "MT63-2000 200 WPM");
        standardModes.put("2K80J2B", "HF RTTY (Radio Teletype)");
        standardModes.put("2K80J2D", "HF PACTOR-III");
        standardModes.put("2K80J3E", "Amplitude modulated (AM) analog voice, single sideband suppressed carrier");
        standardModes.put("2K80J3EY", "Amplitude modulated (AM) analog voice, upper sideband suppressed carrier");
        standardModes.put("2K80J3EZ", "Amplitude modulated (AM) analog voice, lower sideband suppressed carrier");
        standardModes.put("3K00H2B", "HF ALE MIL-STD-188-141A/FED-STD-1045");
        standardModes.put("3K30F1D", "6.25 kHz SCADA link (CalAmp Viper SC â€“ 173 MHz)");
        standardModes.put("4K00F1D", "NXDN 6.25 kHz data (IDAS, NEXEDGE)");
        standardModes.put("4K00F1E", "NXDN 6.25 kHz digital voice (IDAS, NEXEDGE)");
        standardModes.put("4K00F1W", "NXDN 6.25 kHz digital voice and data (IDAS, NEXEDGE)");
        standardModes.put("4K00F2D", "NXDN 6.25 kHz analog FM CW ID (IDAS, NEXEDGE)");
        standardModes.put("4K00J1D", "Amplitude Compandored Sideband (pilot tone/carrier)");
        standardModes.put("4K00J2D", "Amplitude Compandored Sideband (pilot tone/carrier)");
        standardModes.put("4K00J3E", "Amplitude Compandored Sideband (pilot tone/carrier) voice");
        standardModes.put("5K60F2D", "SCADA");
        standardModes.put("5K76G1E", "P25 CQPSK voice (typically used for simulcast systems â€“ this is NOT P25 Phase II)");
        standardModes.put("6K00A3E", "Amplitude modulated (AM) analog voice, double sideband full carrier");
        standardModes.put("6K00F1D", "SCADA Carrier Frequency Shift Keying");
        standardModes.put("6K00F2D", "SCADA Audio Frequency Shift Keying");
        standardModes.put("6K00F3D", "SCADA DTMF / QuickCall Analog data (not AFSK)");
        standardModes.put("6K50F1D", "SCADA/Data 4.8 GFSK in 12.5 kHz channel space (LMR used by CalFire for AVL)");
        standardModes.put("6K00F7W", "D-STAR");
        standardModes.put("7K60FXD", "2-slot DMR (Motorola MOTOTRBO) TDMA data");
        standardModes.put("7K60FXE", "2-slot DMR (Motorola MOTOTRBO) TDMA voice");
        standardModes.put("8K10F1D", "P25 Phase I C4FM data");
        standardModes.put("8K10F1E", "P25 Phase I C4FM voice");
        standardModes.put("8K10F1W", "P25 Phase II subscriber units (Harmonized Continuous Phase Modulation â€“ H-CPM)");
        standardModes.put("8K30F1D", "NXDN 12.5 kHz data (Wide IDAS, NEXEDGE)");
        standardModes.put("8K30F1E", "NXDN 12.5 kHz digital voice (Wide IDAS, NEXEDGE)");
        standardModes.put("8K30F1W", "P25 Phase I C4FM hybridized voice and data applications");
        standardModes.put("8K30F7W", "NXDN 12.5 kHz digital voice and data (Wide IDAS, NEXEDGE)");
        standardModes.put("8K50F9W", "Harris OpenSky (2 slot narrow band)");
        standardModes.put("8K70D1W", "P25 Linear Simulcast Modulation ASTRO (9.6 kbps in 12.5 kHz channel space)");
        standardModes.put("9K20F2D", "Zetron-based alphanumeric paging/alerting system");
        standardModes.put("9K30F1D", "SCADA/ Remote Control");
        standardModes.put("9K36F7W", "Yaesu System Fusion C4FM (Voice Wide * Voice Narrow + Data * Data Wide)");
        standardModes.put("9K70F1D", "P25 Linear Simulcast Modulation WCQPSK data");
        standardModes.put("9K70F1E", "P25 Linear Simulcast Modulation WCQPSK voice");
        standardModes.put("9K80D7W", "P25 Phase II fixed-end 2-slot TDMA (Harmonized Differential Quadrature Phase Shift Keyed modulation â€“ H-DQPSK)");
        standardModes.put("9K80F1D", "P25 Phase II fixed-end 2-slot TDMA H-DQPSK data");
        standardModes.put("9K80F1E", "P25 Phase II fixed-end 2-slot TDMA H-DQPSK voice");
        standardModes.put("10K0F1D", "Motorola Widepulse ASTRO simulcast data");
        standardModes.put("10K0F1E", "Motorola Widepulse ASTRO simulcast voice");
        standardModes.put("11K0F1D", "Narrowband data, type of data not specified");
        standardModes.put("11K0F3E", "Narrowband analog voice, considered by the FCC to be identical to 11K2F3E");
        standardModes.put("11K2F1D", "POCSAG paging (narrowbanded, i.e., Swissphone alerting)");
        standardModes.put("11K2F2D", "Frequency modulated (FM) 2.5 kHz deviation audio frequency shift keying within a 12.5 kHz channelspace (commonly used for 1.2 kbps packet, FFSK station alerting, and AFSK outdoor warning siren signaling)");
        standardModes.put("11K2F3D", "Frequency modulated (FM) 2.5 kHz deviation DTMF or other audible, non-frequency shift signaling, such as Whelen outdoor warning sirens or â€œKnox-BoxÂ®â€� activation");
        standardModes.put("11K2F3E", "Frequency modulated (FM) 2.5 kHz deviation analog voice");
        standardModes.put("12K1F9W", "Harris OpenSky (NPSPAC - 4 slot)");
        standardModes.put("13K1F9W", "Harris OpenSky (SMR - 4 slot)");
        standardModes.put("13K6F3E", "Frequency modulated (FM) analog voice, 3.8 kHz deviation (900 MHz)");
        standardModes.put("13K6W7W", "Motorola iDEN (900 MHz)");
        standardModes.put("14K0F1D", "Motorola 3600 baud trunked control channel (NPSPAC)");
        standardModes.put("16K0F1D", "Motorola 3600 baud trunked control channel");
        standardModes.put("16K0F2D", "4 kHz deviation FM audio frequency shift keying (72 MHz fire alarm boxes)");
        standardModes.put("16K0F3E", "Frequency modulated (FM) analog voice, 4 kHz deviation (NPSPAC); (FM mode in RadioReference.com Database)");
        standardModes.put("16K0G1D", "EPIRB (406 MHz)");
        standardModes.put("16K8F1E", "Encrypted Quantized Voice (Motorola DVP, DES, DES-XL on NPSPAC)");
        standardModes.put("17K7D7D", "Motorola HPD High Performance Data â€“ Astro 25 suite, as Motorola HAI (High performance data Air Interface) â€“ 700/800 MHz â€“ requires 25 kHz channelspace");
        standardModes.put("20K0D1E", "Reduced power TETRA â€“ PowerTrunk 4/TDMA fixed-end (voice)");
        standardModes.put("20K0D1W", "Reduced power TETRA â€“ PowerTrunk 4/TDMA fixed-end (simultaneous mixed modes)");
        standardModes.put("20K0D7D", "Reduced power TETRA (data)");
        standardModes.put("20K0D7E", "Reduced power TETRA (voice)");
        standardModes.put("20K0D7W", "Reduced power TETRA (simultaneous mixed modes)");
        standardModes.put("20K0F1D", "RD-LAP 19.2 kbps within a wideband channel (2013 compliant, meets data throughput requirement)");
        standardModes.put("20K0F1E", "Encrypted Quantized Voice (Motorola DVP, DES, DES-XL - NOT P25 DES-OFB/AES)");
        standardModes.put("20K0F3D", "Frequency modulated (FM) 5 kHz deviation DTMF or other audible, non-frequency shift signaling, such as Whelen outdoor warning sirens or â€œKnox-BoxÂ®â€� activation");
        standardModes.put("20K0F3E", "Frequency modulated (FM) analog voice, 5 kHz deviation; wideband 25 kHz");
        standardModes.put("20K0G7W", "Motorola iDEN (800 MHz)");
        standardModes.put("20K0W7W", "Motorola iDEN (800 MHz)");
        standardModes.put("20K1D1D", "Reduced power TETRA â€“ PowerTrunk 4/TDMA fixed-end (data)");
        standardModes.put("21K0D1W", "TETRA ETS 300 392 Standard");
        standardModes.put("22K0D7D", "TETRA (data)");
        standardModes.put("22K0D7E", "TETRA (voice)");
        standardModes.put("22K0D7W", "TETRA (simultaneous mixed modes)");
        standardModes.put("22K0DXW", "TETRA Subscriber Units (mobiles and control stations)");
        standardModes.put("30K0DXW", "TDMA Cellular (North America)");
        standardModes.put("40K0F8W", "AMPS Cellular");
        standardModes.put("41K7Q7W", "Iridium satellite terminals (1.616-1.626 GHz)");
        standardModes.put("41K7V7W", "Iridium satellite terminals (1.616-1.626 GHz)");
        standardModes.put("55K0P0N", "CODAR oceanographic RADAR (swooping signals on HF with approx. 1 second sweep time) 3.5 - 5 MHz");
        standardModes.put("100KC3F", "ReconRobotics surveillance robot video (430-450 MHz)");
        standardModes.put("100KP0N", "CODAR oceanographic RADAR (swooping signals on HF with approx. 1 second sweep time) 12 - 14 MHz");
        standardModes.put("170KP0N", "CODAR oceanographic RADAR above 24 MHz");
        standardModes.put("200KF8E", "Broadcast FM with Subsidiary Communications Subcarrier");
        standardModes.put("250KF3E", "Television Broadcast Audio (NTSC analog)");
        standardModes.put("300KG7W", "EDGE (Enhanced Data rates for GSM Evolution)");
        standardModes.put("300KGXW", "GSM Cellular");
        standardModes.put("500KD7W", "Broadcast Radio Digital Studio to Transmitter Link 2048 kbps 32 QAM");
        standardModes.put("500KF8W", "Broadcast Radio Analog Studio to Transmitter Link");
        standardModes.put("1M25F9W", "CDMA Cellular");
        standardModes.put("2M40W7D", "Remote Control Video (digital, non-NTSC)");
        standardModes.put("3M00W7W", "SouthernLinc LTE (all four emissions used) 3 MHz bandwidth");
        standardModes.put("5M00G7D", "Public Safety LTE (all four emissions used) 5 MHz bandwidth");
        standardModes.put("5M00W7W", "Public Safety LTE (all four emissions used) 5 MHz bandwidth");
        standardModes.put("5M00G2D", "Public Safety LTE (all four emissions used) 5 MHz bandwidth");
        standardModes.put("5M00D7D", "Public Safety LTE (all four emissions used) 5 MHz bandwidth");
        standardModes.put("5M75C3F", "Television, NTSC analog video (with 250K0F3E aural carrier)");
        standardModes.put("6M00C7W", "Television, ATSC Digital TV (video and audio)");
        standardModes.put("10M0G2D", "Public Safety LTE (all four emissions used) 10 MHz bandwidth");
        standardModes.put("10M0W7W", "Public Safety LTE (all four emissions used) 10 MHz bandwidth");
        standardModes.put("10M0D7D", "Public Safety LTE (all four emissions used) 10 MHz bandwidth");
        standardModes.put("10M0G7D", "Public Safety LTE (all four emissions used) 10 MHz bandwidth");
        standardModes.put("30M0D7W", "Microwave Link Transmitter using 2048 QAM in 30 MHz bandwidth");
        standardModes.put("42M6D7W", "Microwave Link Transmitter QPSK");
        standardModes.put("45M2D7W", "Microwave Link Transmitter 16 QAM 45 MHz");
        standardModes.put("45M8D7W", "Microwave Link Transmitter 32 QAM 45 MHz");
        standardModes.put("47M8D7W", "Microwave Link Transmitter 128 QAM 47 MHz");
        standardModes.put("47M1D7W", "Microwave Link Transmitter 256 QAM 47 MHz");
        standardModes.put("14K0F3E", "EDACS Analog Voice (NPSPAC)");
    }

    public radio.ITUEmissionDesignator.SignalType getSignalType() {
        return signalType;
    }

    public radio.ITUEmissionDesignator.ModulationType getModulationType() {
        return modulationType;
    }

    public radio.ITUEmissionDesignator.InformationType getInformationType() {
        return informationType;
    }

    public radio.ITUEmissionDesignator.DetailsOfSignal getInformationDetail() {
        return detailsOfSignal;
    }

    public radio.ITUEmissionDesignator.MultiplexingType getMultiplexingType() {
        return multiplexingType;
    }

    public static String[] getStandardModeSet() {
    	loadMaps();
        final String[] stringArray = new String[standardModes.size()];
        int i = 0;
        final Iterator<Entry<String, String>> it = standardModes.entrySet().iterator();
        while (it.hasNext()) {
            final Entry<String, String> pairs = it.next();
            stringArray[i] = pairs.getKey() + "  " + pairs.getValue();
            i++;
            it.remove();
        }
        return stringArray;
    }

    public String getITUEmissionDesignator() {
    	String str = null;
    	try {
	        str = bandwidthString + modulationType.name() + signalType.getTypeCode() + informationType.name();
	        if (!"NULL".equals(detailsOfSignal.name())) {
	            str += detailsOfSignal.toString();
	            if (!"NULL".equals(multiplexingType.name())) {
	                str += multiplexingType.name();
	            } else {
	            	str += "-";
	            }
	        } else {
	        	str += "--";
	        }
	        
    	} catch (NullPointerException ex) {
    		LOG.log(Level.SEVERE, "An invalid EmissionDesignator() instance has been instantianted....", ex);
    	}
        return str;
    }

    public String getModeDescription() {
        return standardModes.get(getITUEmissionDesignator());
    }

}
