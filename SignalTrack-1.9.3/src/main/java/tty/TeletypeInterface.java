package tty;

import tty.AbstractTeletypeController.BaudRate;
import tty.AbstractTeletypeController.DataBits;
import tty.AbstractTeletypeController.FlowControl;
import tty.AbstractTeletypeController.Parity;
import tty.AbstractTeletypeController.StopBits;

// This interface holds all the factory default specifications of a radio device. 

public interface TeletypeInterface {
	enum DCEAssert {
		DSR,
		CTS,
		DCD
	}
	
	String getManufacturer();

	String getModel();

	BaudRate[] getAvailableBaudRates();

	BaudRate getDefaultBaudRate();

	DataBits getDefaultDataBits();

	StopBits getDefaultStopBits();

	Parity getDefaultParity();

	FlowControl getDefaultFlowControlIn();

	FlowControl getDefaultFlowControlOut();
	
	boolean supportsRTS();
	
	boolean supportsDTR();
	
	void setCTS(boolean cts);
	
	void setDSR(boolean dsr);

	boolean isSerialBaudRateFixed();

	boolean isSerialParametersFixed();

}
