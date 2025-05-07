package radio;

import java.util.List;

public interface ScannerInterface {
	int getCurrentChannel();
	
	long getTimeoutPeriod();

	void setTimeoutPeriod(long timeoutPeriod);

	Measurement getMeasurement();

	List<ScanElement> getScanElementList();
	
	void setScanList(List<ScanElement> scanList);
	
	List<Float> getBerSCanList();
	
	List<Double> getSinadScanList();
	
	List<Integer> getRssiScanList();

	float getBerScanListElement(int element);
	
	int getRssiScanListElement(int element);
	
	double getSinadScanListElement(int element);
	
	ScanElement getScanElement(int channel);
	
	long getDwellTime();
	
	boolean isScanning();
	
	void startScan();
	
	void startScan(List<ScanElement> scanList);

	void stopScan();
	
	void stopScanNow();
}
