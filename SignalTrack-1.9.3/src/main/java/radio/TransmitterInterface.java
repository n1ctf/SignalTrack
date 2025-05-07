package radio;

import java.util.List;

import radio.AbstractRadioReceiver.StandardModeName;
import tty.AbstractTeletypeController.BaudRate;
import tty.AbstractTeletypeController.DataBits;
import tty.AbstractTeletypeController.FlowControl;
import tty.AbstractTeletypeController.Parity;
import tty.AbstractTeletypeController.StopBits;

// This interface holds all the factory default specifications of a radio device. 

public interface TransmitterInterface {
	
	String getManufacturer();

	void setManufacturer(String manufacturer);

	String getModel();

	void setModel(String model);

	String getSerialNumber();

	void setSerialNumber(String serialNumber);

	boolean isSupportsMultiMode();

	void setSupportsMultiMode(boolean supportsMultiMode);

	boolean isSupportsCountryCodeRetrieval();

	void setSupportsCountryCodeRetrieval(boolean supportsCountryCodeRetrieval);

	boolean isSupportsFirmwareRetrieval();

	void setSupportsFirmwareRetrieval(boolean supportsFirmwareRetrieval);

	boolean isSupportsLTEv3();
	
	void setSupportsLTEv3(boolean supportsLTEv3);

	boolean isSupportsLTEv4();
	
	void setSupportsLTEv4(boolean supportsLTEv4);

	boolean isSupportsLTEv5();
	
	void setSupportsLTEv5(boolean supportsLTEv5);
	
	boolean isSupportsWiMax();
	
	void setSupportsWiMax(boolean supportsWiMax);
	
	boolean isSupportsWFM();
		
	void setSupportsWFM(boolean supportsWFM);	
	
	boolean isSupportsDMR();

	void setSupportsDMR(boolean supportsDMR);

	boolean isSupportsFM();

	void setSupportsFM(boolean supportsFM);

	boolean isSupportsNFM();

	void setSupportsNFM(boolean supportsNFM);
	
	boolean isSupportsEDACS();

	void setSupportsEDACS(boolean supportsEDACS);

	boolean isSupportsEDACSIP();

	void setSupportsEDACSIP(boolean supportsEDACSIP);

	boolean isSupportsEDACSEA();

	void setSupportsEDACSEA(boolean supportsEDACSEA);

	boolean isSupportsOpenSky();

	void setSupportsOpenSky(boolean supportsOpenSky);

	boolean isSupportsNXDN();

	void setSupportsNXDN(boolean supportsNXDN);

	boolean isSupportsP25P1();

	void setSupportsP25P1(boolean supportsP25P1);

	boolean isSupportsP25P2();

	void setSupportsP25P2(boolean supportsP25P2);

	boolean isSupportsP25P1NAC();

	void setSupportsP25P1NAC(boolean supportsP25P1NAC);

	boolean isSupportsP25P2NAC();

	void setSupportsP25P2NAC(boolean supportsP25P2NAC);

	boolean isSupportsSmartZone();

	void setSupportsSmartZone(boolean supportsSmartZone);

	boolean isSupportsSmartNet();

	void setSupportsSmartNet(boolean supportsSmartNet);

	boolean isSupportsAES256Encryption();

	void setSupportsAES256Encryption(boolean supportsAES256Encryption);

	boolean isSupportsDESEncryption();

	void setSupportsDESEncryption(boolean supportsDESEncryption);

	boolean isSupportsDESXLEncryption();

	void setSupportsDESXLEncryption(boolean supportsDESXLEncryption);

	boolean isSupportsDESOFBEncryption();

	void setSupportsDESOFBEncryption(boolean supportsDESOFBEncryption);

	boolean isSupportsDVPEncryption();

	void setSupportsDVPEncryption(boolean supportsDVPEncryption);

	boolean isSupportsSecureNetEncryption();

	void setSupportsSecureNetEncryption(boolean supportsSecureNetEncryption);

	boolean isSupportsADPEncryption();

	void setSupportsADPEncryption(boolean supportsADPEncryption);

	boolean isSupportsType1Encryption();

	void setSupportsType1Encryption(boolean supportsType1Encryption);

	List<StandardModeName> getModeNames();

	void setModeNameList(List<StandardModeName> modeNames);

	void setModeNameArray(StandardModeName[] modeNames);
	
	String[] getPLValues();

	void setCPLValues(String[] plValues);

	String[] getDPLValues();

	void setDPLValues(String[] dplValues);

	String getFlashCode();
	
	void setFlashCode(String flashCode);

	double getMinTxFreq();

	void setMinTxFreq(double minTxFreq);

	double getMaxTxFreq();

	void setMaxTxFreq(double maxTxFreq);

	boolean isSupportsCTS();

	void setSupportsCTS(boolean supportsCTS);

	boolean isSupportsAttenuator();

	BaudRate[] getAvailableBaudRates();

	void setAvailableBaudRates(BaudRate[] availableBaudRates);

	BaudRate getDefaultBaudRate();

	void setDefaultBaudRate(BaudRate defaultBaudRate);

	DataBits getDefaultDataBits();

	void setDefaultDataBits(DataBits defaultDataBits);

	StopBits getDefaultStopBits();

	Parity getDefaultParity();

	void setDefaultParity(Parity defaultParity);

	FlowControl getDefaultFlowControlIn();

	void setDefaultFlowControlIn(FlowControl defaultFlowControlIn);

	FlowControl getDefaultFlowControlOut();

	void setDefaultFlowControlOut(FlowControl defaultFlowControlOut);

	void setDefaultStopBits(StopBits defaultStopBits);

	boolean isSupportsRTS();

	void setSupportsRTS(boolean isSupportsRTS);

	boolean isSupportsDTR();

	void setSupportsDTR(boolean isSupportsDTR);

	boolean isSerialBaudRateFixed();

	void setSerialBaudRateFixed(boolean isSerialBaudRateFixed);

	boolean isSerialParametersFixed();

	void setSerialParametersFixed(boolean isSerialParametersFixed);

	boolean checkRxFreq(double freq);

}
