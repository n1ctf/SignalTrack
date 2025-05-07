package radio;

import radio.AbstractRadioReceiver.EncryptionProtocol;
import radio.AbstractRadioReceiver.StandardModeName;

// This interface holds all the factory default specifications of a radio device. 

public interface ReceiverInterface {
	
	String getManufacturer();

	String getModel();
	
	String getDefaultSerialNumber();

	boolean isSupportsAGC();

	boolean isSupportsAFC();

	boolean isSupportsNoiseBlanker();

	boolean isSupportsVoiceScan();

	boolean isSupportsIFShift();

	boolean isSupportsVolumeSet();

	boolean isSupportsSquelchSet();

	boolean isSupportsMultiMode();

	boolean isSupportsMultiFilter();

	boolean isSupportsCountryCodeRetrieval();

	boolean isSupportsFirmwareRetrieval();

	boolean isSupportsFeatureCodeRetrieval();
	
	boolean isSupportsDSP();

	boolean isSupportsSINAD();

	boolean isSupportsBER();

	boolean isSupportsRSSI();

	StandardModeName[] getModeNameValues();

	String[] getToneSquelchValues();

	String[] getDigitalSquelchValues();

	Integer[] getAvailableFilters();
	
	String[] getDigitalNACValues();
	
	String[] getDigitalColorCodeValues();
	
	EncryptionProtocol[] getEncryptionProtocols();

	double getMinRxFreq();

	double getMaxRxFreq();
	
	double getMinAttenuator();

	double getMaxAttenuator();

	boolean isSupportsCOR();

	boolean isSupportsAttenuator();

	int getRssiUpperLimit();

	int getRssiLowerLimit();

	boolean checkRxFreq(double freq);
	
	double getDefaultNoiseFloor();
	
	double getDefaultSaturation();
	
	double getAdjacentChannelRejectiondB();
	
	double getSignalReqFor12dBSINADdBm(); 

	double getSignalReqFor20dBQuietingdBm(); 
	
	double getSignalReqFor5PctBERdBm(); 

}
