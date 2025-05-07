package radio;

import java.awt.EventQueue;
import java.beans.PropertyChangeSupport;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import components.TwoDimArrayList;
import components.TwoDimElement;
import radio.AbstractRadioReceiver.DataString;
import radio.AbstractRadioReceiver.DefaultRadioSpecification;
import utility.Utility;

// This CalibrationDataObject() stores preferences and calibration data for a specific manufacturer, model and serial number
// of every radio in the users inventory. data is stored in a record on the specified file system.

public class CalibrationDataObject implements AutoCloseable {
	
	public enum Event {
		SEMAPHORE_ACQUIRE,
		SEMAPHORE_RELEASE
	}
	
	public static final double DEFAULT_ADJACENT_CHANNEL_REJECTION = 60.0; 
	public static final double DEFAULT_SIGNAL_REQ_FOR_12DB_SINAD = -115.0;
	public static final double DEFAULT_SIGNAL_REQ_FOR_20DB_QUIETING = -117.0;
	public static final double DEFAULT_SIGNAL_REQ_FOR_5PCT_BER = -118.0;
	
	private static final Logger LOG = Logger.getLogger(CalibrationDataObject.class.getName());
	public static final PropertyChangeSupport pcs = new PropertyChangeSupport(CalibrationDataObject.class.getName());
	
    private final TwoDimArrayList<Integer, Double> calibrationSelectorList = new TwoDimArrayList<>();

    private boolean validFile;
    
    private String source;
    private String manufacturer;
    private String model;
    private String serialNumber;
    private double adjacentChannelRejection;
    private double signalReqFor12dBSINAD;
    private double signalReqFor20dBQuieting;
    private double signalReqFor5PctBER;
    private double noiseFloor;
    private double saturation;
    
    private File calFile;
    private AbstractRadioReceiver radio;
    private final Semaphore semaphore = new Semaphore(1);
    
    // constructor for opening existing files
    public CalibrationDataObject(AbstractRadioReceiver radio) {
        this.radio = radio;
        this.calFile = radio.getCalFile();
        openCalabrationDataObjectFile(calFile);
    }
    
    // used for making new files
    public CalibrationDataObject(File calFile, String source, String manufacturer, String model, String serialNumber,
    	DefaultRadioSpecification drs) {

    	this.calFile = calFile;
        this.source = source;
        this.manufacturer = manufacturer;
        this.model = model;
        this.serialNumber = serialNumber;
        this.noiseFloor = drs.getNoiseFloor();
        this.saturation = drs.getSaturation();
        this.adjacentChannelRejection = drs.getAdjacentChannelRejection();
        this.signalReqFor12dBSINAD = drs.getSignalRequiredFor12dBSinad();
        this.signalReqFor20dBQuieting = drs.getSignalRequiredFor20dBQuieting();
        this.signalReqFor5PctBER = drs.getSignalRequiredFor5PercentBER();

        if (!calFile.exists()) {
            try (RandomAccessFile raf = new RandomAccessFile(calFile, "rw")) {
            	pcs.firePropertyChange(Event.SEMAPHORE_ACQUIRE.name(), null, null);
                semaphore.acquire();
                
                final Path path = Paths.get(calFile.getParent());
                
                if (!path.toFile().exists()) {
                    if (!(new File(path.getParent().toString()).mkdirs())) {
                        LOG.log(Level.WARNING, "Unable to create directory: {0}", path);
                    }
                }
                
                if (!Paths.get(calFile.getPath()).toFile().exists()) {
                    final BufferedWriter writer = new BufferedWriter(new FileWriter(calFile, StandardCharsets.UTF_8));
                    writer.close();
                }
                
                calibrationSelectorList.clear();
                
                // This gets a linear List<double> of dBm measurements,
                // ranging from the weakest readable signal to the strongest.
                // The size of the returned array is based on the number of possible RSSI values the radio is able to report.
                final double[] dBmArray = getDefaultdBmSelectorArray(drs.getRssiLowerLimit(), drs.getRssiUpperLimit(), noiseFloor, saturation);
                
                // This loads arrayList, a two dimensional array of all the valid RSSI values on the left,
                // and the dBmArray. It assumes that the higher RSSI is the stronger signal.
                for (int i = 0; i < dBmArray.length; i++) {
                    calibrationSelectorList.add(drs.getRssiLowerLimit() + i, dBmArray[i]);
                }
                
                final String[] t = new String[calibrationSelectorList.size() + DataString.values().length];
                
                loadSpecificationArray(t);
                
                for (int i = 0; i < calibrationSelectorList.size(); i++) {
                    t[i + DataString.values().length] = calibrationSelectorList.getT1(i) + "=" + calibrationSelectorList.getT2(i) + System.lineSeparator();
                }
                
                raf.seek(0);
                
                for (final String element : t) {
                    raf.writeBytes(element);
                }
            } catch (final NullPointerException | IOException e) {
                LOG.log(Level.WARNING, e.getMessage());
            } catch (InterruptedException e) {
            	LOG.log(Level.WARNING, e.getMessage());
            	Thread.currentThread().interrupt();
			} finally {
                semaphore.release();
                pcs.firePropertyChange(Event.SEMAPHORE_RELEASE.name(), null, null);
            }
        }
    }
	
    private void openCalabrationDataObjectFile(File calFile) {
    	if (calFile.exists() && !calFile.isDirectory()) {
            boolean success = true;
            try (RandomAccessFile raf = new RandomAccessFile(calFile, "r")) {
            	pcs.firePropertyChange(Event.SEMAPHORE_ACQUIRE.name(), null, null);
                semaphore.acquire();
                calibrationSelectorList.clear();
                int i = 0;
                String inputString;
                while (raf.getFilePointer() < raf.length()) {
                    inputString = raf.readLine();
                    if (inputString.length() > 0) {
                        final String[] stringArray = inputString.split("=");
                        if (i <= 9) {
                            success = stringArray[0].contentEquals(DataString.values()[i].name()); // test for correct field name
                            if (!success) {
                                break;
                            }
                        } 
                        if (i == 0) {
                            source = stringArray[1];
                        }
                        if (i == 1) {
                            manufacturer = stringArray[1];
                        }
                        if (i == 2) {
                            model = stringArray[1];
                        }
                        if (i == 3) {
                            serialNumber = stringArray[1];
                        }
                        if (i == 4) {
                            adjacentChannelRejection = Double.parseDouble(stringArray[1]);
                        }
                        if (i == 5) {
                            signalReqFor12dBSINAD = Double.parseDouble(stringArray[1]);
                        }
                        if (i == 6) {
                            signalReqFor20dBQuieting = Double.parseDouble(stringArray[1]);
                        }
                        if (i == 7) {
                            signalReqFor5PctBER = Double.parseDouble(stringArray[1]);
                        }
                        if (i == 8) {
                            noiseFloor = Double.parseDouble(stringArray[1]);
                        }
                        if (i == 9) {
                            saturation = Double.parseDouble(stringArray[1]);
                        }
                        if (i >= 10) {
                            calibrationSelectorList.add(Integer.parseInt(stringArray[0]), Double.parseDouble(stringArray[1]));
                        }
                    }
                    i++;
                }
                if (calibrationSelectorList.isEmpty()) { // this checks for errors in the calibration record 
                	success = false;
                	LOG.log(Level.INFO, "Calibration record for {0} {1} {2} is incomplete", 
                		new Object[] { manufacturer, model, serialNumber });     
                }
            } catch (final InterruptedException e) {
                LOG.log(Level.WARNING, e.getMessage());
                success = false;
                Thread.currentThread().interrupt();
            } catch (final IOException e) {
                LOG.log(Level.WARNING, e.getMessage());
                success = false;
            }
            semaphore.release();
            pcs.firePropertyChange(Event.SEMAPHORE_RELEASE.name(), null, null);
            if (success) {
                validFile = true;
            }
        } else {
            validFile = false;
            invokeLaterInDispatchThreadIfNeeded(() -> JOptionPane.showMessageDialog(new JDialog(),
                "Calibration file does not exist", "File Not Found Error", JOptionPane.ERROR_MESSAGE));
        }
    }

    public CalibrationComponent openCalibrationComponent() {
    	return new CalibrationComponent(this);
    }
    
    public boolean isValidFile() {
        return validFile;
    }

    public String getSource() {
        return source;
    }

    public void setNoiseFloor(double noiseFloor) {
        modifyCalibrationSelectordBmList(noiseFloor, saturation);
    }

    public void setSaturation(double saturation) {
    	modifyCalibrationSelectordBmList(noiseFloor, saturation);
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModelString() {
        return model;
    }

    public void setModelString(String model) {
        this.model = model;
    }

    public String getSerialString() {
        return serialNumber;
    }

    public void setSerialString(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public double getAdjacentChannelRejection() {
        return adjacentChannelRejection;
    }

    public void setAdjacentChannelRejection(double adjacentChannelRejection) {
        this.adjacentChannelRejection = adjacentChannelRejection;
    }

    public double getSignalReqFor12dBSINAD() {
        return signalReqFor12dBSINAD;
    }

    public void setSignalReqFor12dBSINAD(double signalReqFor12dBSINAD) {
        this.signalReqFor12dBSINAD = signalReqFor12dBSINAD;
    }

    public double getSignalReqFor20dBQuieting() {
        return signalReqFor20dBQuieting;
    }

    public void setSignalReqFor20dBQuieting(double signalReqFor20dBQuieting) {
        this.signalReqFor20dBQuieting = signalReqFor20dBQuieting;
    }

    public double getSignalReqFor5PctBER() {
        return signalReqFor5PctBER;
    }

    public void setSignalReqFor5PctBER(double signalReqFor5PctBER) {
        this.signalReqFor5PctBER = signalReqFor5PctBER;
    }

    public List<TwoDimElement<Integer, Double>> getCalibrationSelectorList() {
        return Collections.unmodifiableList(calibrationSelectorList);
    }

    public String[] getRssiStringArray() {
        final String[] rssiStringArray = new String[calibrationSelectorList.size()];
        for (int i = 0; i < calibrationSelectorList.size(); i++) {
            rssiStringArray[i] = Integer.toString(calibrationSelectorList.getT1(i));
        }
        return rssiStringArray;
    }

    public IntervalXYDataset getXYSeriesCollection() {
        final XYSeries series = new XYSeries("");
        final Iterator<TwoDimElement<Integer, Double>> iter = calibrationSelectorList.iterator();
        while (iter.hasNext()) {
            final TwoDimElement<Integer, Double> element = iter.next();
            series.add(element.getT1(), element.getT2());
        }
        return new XYSeriesCollection(series);
    }

    public int getCalibrationSelectorListSize() {
        return calibrationSelectorList.size();
    }

    public List<Double> getdBmList() {
        return calibrationSelectorList.getT2List();
    }

    public List<Integer> getRssiList() {
        return calibrationSelectorList.getT1List();
    }

	public File getCalFile() {
        return calFile;
    }

    public synchronized double getdBm(int index) {
        return calibrationSelectorList.getT2(index);
    }

    public synchronized double getdBmElement(int rssi) {
        if ((rssi < Math.min(calibrationSelectorList.getT1(0), calibrationSelectorList.getT1(calibrationSelectorList.size() - 1)))
                || (rssi > Math.max(calibrationSelectorList.getT1(0), calibrationSelectorList.getT1(calibrationSelectorList.size() - 1)))) {
            return 0;
        }
        double dBm = 0;
        for (int i = 0; i < calibrationSelectorList.size(); i++) {
            dBm = calibrationSelectorList.getT2(i);
            if (rssi >= calibrationSelectorList.getT1(i)) {
                break;
            }
        }
        return dBm;
    }

    public void setdBm(int index, double dBm) {
        calibrationSelectorList.setT2(index, dBm);
    }

    public int getRssi(int index) {
        return calibrationSelectorList.getT1(index);
    }

    public int getRssiElement(double dBm) {
        int rssi = 0;
        if (isValiddBmValue(dBm)) {
            for (int i = 0; i < calibrationSelectorList.size(); i++) {
                rssi = calibrationSelectorList.getT1(i);
                if (dBm >= calibrationSelectorList.getT2(i)) {
                    break;
                }
            }
        }
        return rssi;
    }

    public void setRssi(int index, int rssi) {
        calibrationSelectorList.setT1(index, rssi);
    }

    public double getSaturation() {
        return saturation;
    }

    public double getNoiseFloor() {
        return noiseFloor;
    }

    private boolean isValiddBmValue(double dBm) {
        return (dBm <= getSaturation()) && (dBm >= getNoiseFloor());
    }

    private void modifyCalibrationSelectordBmList(double noiseFloor, double saturation) {
    	this.noiseFloor = noiseFloor;
        this.saturation = saturation;
        final double[] dBmArray = getDefaultdBmSelectorArray(calibrationSelectorList.getT1List().get(0), calibrationSelectorList.getT1List().get(calibrationSelectorList.size() - 1),	
        	noiseFloor, saturation);
        calibrationSelectorList.clear();
        for (int i = 0; i < dBmArray.length; i++) {
            calibrationSelectorList.add(radio.getRssiLowerLimit() + i, dBmArray[i]);
        }
    }

    public static double[] getDefaultdBmSelectorArray(int rssiLowerLimit, int rssiUpperLimit, double noiseFloor, double saturation) {
        final int count = Math.abs(rssiUpperLimit - rssiLowerLimit);
        final double range = Math.abs(noiseFloor - saturation);
        final double step = Math.abs(range / (count - 1));
        final double[] array = new double[count];
        double value = Math.max(saturation, noiseFloor);
        for (int i = array.length - 1; i >= 0; i--) {
            array[i] = Utility.round(value, 1);
            value -= step;
        }
        return array;
    }

    public Double[] getCalibrationSelectordBmArray() {
        return calibrationSelectorList.getT2List().toArray(new Double[calibrationSelectorList.getT2List().size()]);
    }

    public static String getDataString(File calFile, DataString dataString) {
        String s = dataString.name();
        try (RandomAccessFile raf = new RandomAccessFile(calFile, "r")) {
            String inputString;
            while (raf.getFilePointer() < raf.length()) {
                inputString = raf.readLine();
                final String[] inputArray = inputString.split("=");
                if (inputArray[0].equalsIgnoreCase(s)) {
                    s = inputArray[1];
                    break;
                }
            }
        } catch (final ArrayIndexOutOfBoundsException | IOException ex) {
            LOG.log(Level.WARNING, ex.getMessage());
        }
        return s;
    }

    public void save() {
    	ExecutorService executor = Executors.newSingleThreadExecutor();
    		executor.execute(new SaveCalFile());
    }

    private final class SaveCalFile implements Runnable {
    	
        @Override
        public void run() {
        	try {
        		pcs.firePropertyChange(Event.SEMAPHORE_ACQUIRE.name(), null, null);
	        	semaphore.acquire();
	        	Files.delete(calFile.toPath());
	            try (RandomAccessFile raf = new RandomAccessFile(calFile, "rw")) {
	                final String[] t = new String[calibrationSelectorList.size() + DataString.values().length];
	                loadSpecificationArray(t);
	                for (int i = 0; i < calibrationSelectorList.size(); i++) {
	                    t[i + DataString.values().length] = calibrationSelectorList.getT1(i) + "=" + calibrationSelectorList.getT2(i) + System.lineSeparator();
	                }
	                raf.seek(0);
	                for (final String element : t) {
	                    raf.writeBytes(element);
	                }
	            } 
	        } catch (final IOException e) {
	            LOG.log(Level.WARNING, e.getMessage());
            } catch (final InterruptedException e) {
            	LOG.log(Level.WARNING, e.getMessage());
            	Thread.currentThread().interrupt();
            } finally {
            	semaphore.release();
            	pcs.firePropertyChange(Event.SEMAPHORE_RELEASE.name(), null, null);
            }
        }
    }

    private void loadSpecificationArray(String[] t) {
        t[0] = DataString.SOURCE.name() + "=" + source + System.lineSeparator();
        t[1] = DataString.MANUFACTURER.name() + "=" + manufacturer + System.lineSeparator();
        t[2] = DataString.MODEL.name() + "=" + model + System.lineSeparator();
        t[3] = DataString.SN.name() + "=" + serialNumber + System.lineSeparator();
        t[4] = DataString.ADJACENT_CHANNEL_REJECTION.name() + "=" + adjacentChannelRejection + System.lineSeparator();
        t[5] = DataString.SIGNAL_REQ_FOR_12DB_SINAD.name() + "=" + signalReqFor12dBSINAD + System.lineSeparator();
        t[6] = DataString.SIGNAL_REQ_FOR_20DB_QUIETING.name() + "=" + signalReqFor20dBQuieting + System.lineSeparator();
        t[7] = DataString.SIGNAL_REQ_FOR_5PCT_BER.name() + "=" + signalReqFor5PctBER + System.lineSeparator();
        t[8] = DataString.NOISE_FLOOR.name() + "=" + noiseFloor + System.lineSeparator();
        t[9] = DataString.SATURATION.name() + "=" + saturation + System.lineSeparator();
    }

	// This method creates a dummy calFile record on the file system for new installs, and returns a File() instance of it, 
	// to avoid null pointer exceptions.
	public static File createDefaultCalibrationFile() {
		AbstractRadioReceiver radio = AbstractRadioReceiver.getRadioInstance(AbstractRadioReceiver.getRadioCatalog()[0]);
		return radio.getCalFile();
	}
    
    public String getUniqueIdentifier() {
        return manufacturer + "_" + model + "_" + serialNumber + "_";
    }

    private static void invokeLaterInDispatchThreadIfNeeded(Runnable runnable) {
        if (EventQueue.isDispatchThread()) {
            runnable.run();
        } else {
            SwingUtilities.invokeLater(runnable);
        }
    }
    
    public static File getStandardCalibrationFileName(File calFileDirectory, String model, String sn) {
    	return new File(calFileDirectory.getPath() + File.separator + model + "_" + sn + ".cal");
    }

    public static PropertyChangeSupport getPropertyChangeSupport() {
    	return pcs;
    }
    
    @Override
    public void close() {
    	save();
    }
    
}
