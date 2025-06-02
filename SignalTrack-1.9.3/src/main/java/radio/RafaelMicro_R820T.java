package radio;

import java.awt.EventQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JPanel;

import org.usb4java.Device;
import org.usb4java.DeviceDescriptor;
import org.usb4java.DeviceList;
import org.usb4java.LibUsb;
import org.usb4java.LibUsbException;

import com.g0kla.rtlsdr4java.ComplexBuffer;
import com.g0kla.rtlsdr4java.DeviceException;
import com.g0kla.rtlsdr4java.Listener;
import com.g0kla.rtlsdr4java.R820TTunerController;
import com.g0kla.rtlsdr4java.RTL2832TunerController;
import com.g0kla.rtlsdr4java.Source;
import com.g0kla.rtlsdr4java.RTL2832TunerController.SampleRate;

import com.g0kla.rtlsdr4java.TunerType;

public class RafaelMicro_R820T extends AbstractRadioReceiver {
	private static final long serialVersionUID = 8250966856595400820L;
	
	private static final Logger LOG = Logger.getLogger(RafaelMicro_R820T.class.getName());

	private R820TTunerController rtl = null;

    public static final String EQUIPMENT_CODE = "R820T";
    public static final String SERIAL_NUMBER = "";
    public static final String MANUFACTURER = "Rafael Micro";
    public static final String MODEL_NAME = "RTL-SDR";
    public static final String SOFTWARE_VERSION = "1.0";
    public static final String HARDWARE_VERSION = "1.0";

    public static final short DEFAULT_VENDOR_ID = (short) 0x0bda;
    public static final short DEFAULT_PRODUCT_ID = (short) 0x2838;
    public static final int DEFAULT_SAMPLE_RATE = 240000;
    
    public static final double DEFAULT_REFERENCE_IMPEDANCE = 50.0;
    public static final boolean DEFAULT_DEBUG = true;
    
    private static final boolean SUPPORTS_SINAD = false;
	private static final boolean SUPPORTS_BER = false;
	private static final boolean SUPPORTS_RSSI = false;
	private static final boolean SUPPORTS_AGC = false;
	private static final boolean SUPPORTS_AFC = false;
	private static final boolean SUPPORTS_ATTENUATOR = false;
	private static final boolean SUPPORTS_NOISE_BLANKER = false;
	private static final boolean SUPPORTS_VOLUME_CONTROL = false;
	private static final boolean SUPPORTS_SQUELCH_CONTROL = false;
	private static final boolean SUPPORTS_COR = false;
	private static final boolean SUPPORTS_MULTI_MODE = false;
	private static final boolean SUPPORTS_MULTI_FILTER = false;
	private static final boolean SUPPORTS_COUNTRY_CODE_RETRIEVAL = false;
	private static final boolean SUPPORTS_FIRMWARE_RETRIEVAL = false;
	private static final boolean SUPPORTS_IF_SHIFT = false;
	private static final boolean SUPPORTS_VOICE_SCAN = false;
	private static final boolean SUPPORTS_DSP = false;
	private static final boolean SUPPORTS_FEATURE_CODE_RETRIEVAL = false;
	private static final double MINIMUM_RX_FREQ = 0.050;
	private static final double MAXIMUM_RX_FREQ = 1300.0;
	private static final double MINIMUM_ATTN = 0.0;
    private static final double MAXIMUM_ATTN = 125.0;
	private static final int RSSI_UPPER_LIMIT = 255;
	private static final int RSSI_LOWER_LIMIT = 0;
	private static final double DEFAULT_NOISE_FLOOR = -135;
	private static final double DEFAULT_SATURATION = -21;
	private static final double ADJACENT_CHANNEL_REJECTION_DB = 60.0;
	private static final double SIGNAL_REQ_FOR_12DB_SINAD = -115.0; 
	private static final double SIGNAL_REQ_FOR_20DB_QUIETING = -117.0; 
	private static final double SIGNAL_REQ_FOR_5PCT_BER = -118;
    
	private static final EncryptionProtocol[] ENCRYPTION_PROTOCOLS = { EncryptionProtocol.CLEAR };
	
	private static final Integer[] FILTERS_HZ = { 2800, 6000, 15000, 50000, 230000 };
	
	private float i;
	private float q;
	
	private double frequencyMHz;
	
	private boolean debug = DEFAULT_DEBUG;
	
	private short vendorId;
	private short productId;
	private int sampleRate;
	
	private final ExecutorService executor = Executors.newCachedThreadPool();
	
	private static final StandardModeName[] SUPPORTED_MODES = { 
		StandardModeName.IQ
	};

	public RafaelMicro_R820T() {
		this(DEFAULT_VENDOR_ID, DEFAULT_PRODUCT_ID, DEFAULT_SAMPLE_RATE);
	}

	public RafaelMicro_R820T(short vendorId, short productId, int sampleRate) {
		this.vendorId = vendorId;
		this.productId = productId;
		this.sampleRate = sampleRate;
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				close();
			}
		});
		
		startRadio();
		setFrequencyMHz(400.000);
	}

	public R820TTunerController getR820T() {
		return rtl;
	}

	public void addListener(Listener<ComplexBuffer> listener) {
		rtl.addListener(listener);
	}

	public class RtlSource extends Source implements Listener<ComplexBuffer> {

		public RtlSource(int bufferSize) {
			super(bufferSize);
		}

		@Override
		public void receive(ComplexBuffer t) {
			final float[] iqBuffer = t.getSamples();
			if (iqBuffer.length >= 2) {
				i = iqBuffer[0];
				q = iqBuffer[1];
				if (debug) {
					LOG.log(Level.INFO, "I: {0} Q: {1} dBm: {2} RSSI: {3}", new Object[] {iqBuffer[0], iqBuffer[1], getdBm(), getRSSI()});
				}
			}
			for (float f : iqBuffer) {
				buffer.add(f);
			}
		}
	}

	@Override
	public int getRSSI() {
		final double dynamicRange = Math.abs(DEFAULT_NOISE_FLOOR - DEFAULT_SATURATION); 		// = -135 - -21 = -135 + 21 = -114
		final double rssiRangeMagnitude = Math.abs(RSSI_UPPER_LIMIT - RSSI_LOWER_LIMIT + 1D);	// = 255 - 0 + 1 = 256
		final double rssiPerdBm = rssiRangeMagnitude / dynamicRange;							// = 256 / 114 = 2.245614	
		return (int) (Math.round(getdBm() - DEFAULT_SATURATION) * rssiPerdBm); 
	}
	
	// Let's say you have I = 0.5 and Q = 1.0, and your system has a reference impedance of 50 Ohms. 
	// Magnitude: Magnitude = sqrt(0.5^2 + 1^2) = sqrt(1.25) ≈ 1.118
	// Power (Watts): Power (Watts) = (1.118^2) / 50 ≈ 0.025
	// Power (dBm): Power (dBm) = 10 * log10(0.025) + 30 ≈ -16 dBm
	
	@Override
	public double getdBm() {
		return getdBm(i, q, DEFAULT_REFERENCE_IMPEDANCE);
	}
	
	public static double getdBm(double i, double q) {
		return getdBm(i, q, DEFAULT_REFERENCE_IMPEDANCE);
	}
	
	public static double getdBm(double i, double q, double impedanceOhms) {
		final double magnitude = Math.abs(Math.sqrt((i*i) + (q*q)));
		final double watts = (magnitude*magnitude) / impedanceOhms;
		return 10 * Math.log10(watts) + 30;
	}
	
	@Override
	public String getManufacturer() {
		return MANUFACTURER;
	}

	@Override
	public String getModel() {
		return MODEL_NAME;
	}

	@Override
	public boolean isSupportsAGC() {
		return SUPPORTS_AGC;
	}

	@Override
	public boolean isSupportsAFC() {
		return SUPPORTS_AFC;
	}

	@Override
	public boolean isSupportsNoiseBlanker() {
		return SUPPORTS_NOISE_BLANKER;
	}

	@Override
	public boolean isSupportsVoiceScan() {
		return SUPPORTS_VOICE_SCAN;
	}

	@Override
	public boolean isSupportsIFShift() {
		return SUPPORTS_IF_SHIFT;
	}

	@Override
	public boolean isSupportsVolumeSet() {
		return SUPPORTS_VOLUME_CONTROL;
	}

	@Override
	public boolean isSupportsSquelchSet() {
		return SUPPORTS_SQUELCH_CONTROL;
	}

	@Override
	public boolean isSupportsMultiMode() {
		return SUPPORTS_MULTI_MODE;
	}

	@Override
	public boolean isSupportsMultiFilter() {
		return SUPPORTS_MULTI_FILTER;
	}

	@Override
	public boolean isSupportsCountryCodeRetrieval() {
		return SUPPORTS_COUNTRY_CODE_RETRIEVAL;
	}

	@Override
	public boolean isSupportsFirmwareRetrieval() {
		return SUPPORTS_FIRMWARE_RETRIEVAL;
	}

	@Override
	public boolean isSupportsDSP() {
		return SUPPORTS_DSP; 
	}

	@Override
	public boolean isSupportsSINAD() {
		return SUPPORTS_SINAD;
	}

	@Override
	public boolean isSupportsBER() {
		return SUPPORTS_BER;
	}

	@Override
	public boolean isSupportsRSSI() {
		return SUPPORTS_RSSI;
	}

	@Override
	public Integer[] getAvailableFilters() {
		return FILTERS_HZ.clone();
	}

	@Override
	public boolean isSupportsFeatureCodeRetrieval() {
		return SUPPORTS_FEATURE_CODE_RETRIEVAL;
	}

	@Override
	public double getMinRxFreq() {
		return MINIMUM_RX_FREQ;
	}

	@Override
	public double getMaxRxFreq() {
		return MAXIMUM_RX_FREQ;
	}

	@Override
	public boolean isSupportsCOR() {
		return SUPPORTS_COR;
	}

	@Override
	public boolean isSupportsAttenuator() {
		return SUPPORTS_ATTENUATOR;
	}

	@Override
	public int getRssiUpperLimit() {
		return RSSI_UPPER_LIMIT;
	}

	@Override
	public int getRssiLowerLimit() {
		return RSSI_LOWER_LIMIT;
	}

	@Override
	public EncryptionProtocol[] getEncryptionProtocols() {
		return ENCRYPTION_PROTOCOLS.clone();
	}

	@Override
	public StandardModeName[] getModeNameValues() {
		return SUPPORTED_MODES.clone();
	}

	@Override
	public double getDefaultNoiseFloor() {
		return DEFAULT_NOISE_FLOOR;
	}

	@Override
	public double getDefaultSaturation() {
		return DEFAULT_SATURATION;
	}

	@Override
	public double getAdjacentChannelRejectiondB() {
		return ADJACENT_CHANNEL_REJECTION_DB;
	}

	@Override
	public double getSignalReqFor12dBSINADdBm() {
		return SIGNAL_REQ_FOR_12DB_SINAD;
	}

	@Override
	public double getSignalReqFor20dBQuietingdBm() {
		return SIGNAL_REQ_FOR_20DB_QUIETING;
	}

	@Override
	public double getSignalReqFor5PctBERdBm() {
		return SIGNAL_REQ_FOR_5PCT_BER;
	}

	@Override
	public double getMinAttenuator() {
		return MINIMUM_ATTN;
	}

	@Override
	public double getMaxAttenuator() {
		return MAXIMUM_ATTN;
	}

	@Override
	public String getDefaultSerialNumber() {
		return SERIAL_NUMBER;
	}

	@Override
	public long getVersionUID() {
		return serialVersionUID;
	}

	@Override
	public String getInterfaceName() {
		return "Universal Serial Bus";
	}

	@Override
	public JPanel[] getConfigurationComponentArray() {
		// TODO Auto-generated method stub
		return new JPanel[0];
	}

	@Override
	public void stopRadio() {
		executor.execute(new StopRadio());
	}

	private class StopRadio implements Runnable {
		@Override
		public void run() {
			try {
				if (rtl != null) {
					rtl.release();
				}
			} catch (DeviceException ex) {
				LOG.log(Level.WARNING, ex.getLocalizedMessage());
			}
		}
	}
	
	@Override
	public boolean isConnected() {
		return rtl != null;
	}

	@Override
	public void startRadio() {
		executor.execute(new RTLSDR(vendorId, productId, sampleRate));
	}
	
	@Override
	public void setFrequencyMHz(double frequency) {
		this.frequencyMHz = frequency;
		try {
			if (rtl != null) {
				rtl.setTunedFrequency((long) (frequencyMHz * 10E6));
			}
		} catch (DeviceException ex) {
			LOG.log(Level.WARNING, "Device Exception", ex);
		}
	}
	
	private class RTLSDR implements Runnable {
		private final short vendorId;
		private final short productId;
		private final SampleRate sampleRate;
		
		private RTLSDR(short vendorId, short productId, int sampleRate) {
			this(vendorId, productId, SampleRate.getClosest(sampleRate));
		}

		private RTLSDR(short vendorId, short productId, SampleRate sampleRate) {
			this.vendorId = vendorId;
			this.productId = productId;
			this.sampleRate = sampleRate;
		}
		
		@Override
		public void run() {
			try {
				final int result = LibUsb.init(null);
				
				if (result == LibUsb.SUCCESS) {
					rtl = findDevice(vendorId, productId, sampleRate);
					
					if (rtl != null) {
						rtl.setTunedFrequency((long) (frequencyMHz * 10E6));
		
						final RtlSource rtlDataListener = new RtlSource(sampleRate.getRate() * 5);
		
						addListener(rtlDataListener);
					} else {
						LOG.log(Level.WARNING, "LibUsb failed to initialize");
					}
				} else {
					LOG.log(Level.WARNING, "LibUsb failed to initialize");
				}
			} catch (DeviceException ex) {
				LOG.log(Level.WARNING, "Device Exception", ex);
			}
		}
		
		private R820TTunerController findDevice(short vendorId, short productId, SampleRate sampleRate) {
			R820TTunerController r820t = null;

			final DeviceList deviceList = new DeviceList();

			int result = LibUsb.getDeviceList(null, deviceList);

			if (result < 0) {
				throw new LibUsbException("Unable to get device list", result);
			}

			try {
				// Iterate over all devices and scan for the right one
				for (Device device : deviceList) {
					final DeviceDescriptor descriptor = new DeviceDescriptor();

					result = LibUsb.getDeviceDescriptor(device, descriptor);

					if (result != LibUsb.SUCCESS) {
						throw new LibUsbException("Unable to read device descriptor", result);
					}

					if (descriptor.idVendor() == vendorId && descriptor.idProduct() == productId) {
						LOG.log(Level.INFO, "Device Descriptor: {0}", descriptor);

						TunerType tunerType = TunerType.UNKNOWN;
						tunerType = RTL2832TunerController.identifyTunerType(device);
						LOG.log(Level.INFO, "Found Tuner: {0}", tunerType);

						r820t = new R820TTunerController(device, descriptor);

						r820t.init(sampleRate);

						break;
					}
				}
			} catch (DeviceException ex) {
				LOG.log(Level.WARNING, ex.getLocalizedMessage());
			} finally {
				// Ensure the allocated device list is freed
				// Note don't free the list before we have opened the device that we want,
				// otherwise it fails
				LibUsb.freeDeviceList(deviceList, true);
			}
			return r820t;
		}
	}
	
	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public static void main(String[] args) {
		EventQueue.invokeLater(RafaelMicro_R820T::new);
	}

}
