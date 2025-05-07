package tty;

import java.util.Locale;

import jssc.SerialPort;
import jssc.SerialPortList;

public class CommPortUtility {
	
    public enum Parity {
        PARITY_NONE,
        PARITY_ODD,
        PARITY_EVEN,
        PARITY_MARK,
        PARITY_SPACE
    }

    public enum DataBits {
        DATA_BITS_5,
        DATA_BITS_6,
        DATA_BITS_7,
        DATA_BITS_8
    }

    public enum StopBits {
        STOP_BITS_1,
        STOP_BITS_2,
        STOP_BITS_1_5
    }

    public enum FlowControl {
        NONE,
        RTSCTS,
        XONXOFF
    }

    public enum BaudRates {
        BAUDRATE_110,
        BAUDRATE_300,
        BAUDRATE_600,
        BAUDRATE_1200,
        BAUDRATE_4800,
        BAUDRATE_9600,
        BAUDRATE_14400,
        BAUDRATE_19200,
        BAUDRATE_38400,
        BAUDRATE_57600,
        BAUDRATE_115200,
        BAUDRATE_128000,
        BAUDRATE_256000
    }

    public static boolean isComPortValid(String portName) {
        boolean isAvailable = false;
        if (portName.isEmpty() || !portName.toUpperCase(Locale.getDefault()).startsWith("COM")) {
            return isAvailable;
        }
        final String[] ports = SerialPortList.getPortNames();
        for (final String port : ports) {
            if (port.equals(portName)) {
                isAvailable = true;
				break;
            }
        }
        return isAvailable;
    }

    public static Integer[] getAllStandardBaudRates() {
    	final Integer[] baudRates = new Integer[13];
        baudRates[0] = SerialPort.BAUDRATE_110;
        baudRates[1] = SerialPort.BAUDRATE_300;
        baudRates[2] = SerialPort.BAUDRATE_600;
        baudRates[3] = SerialPort.BAUDRATE_1200;
        baudRates[4] = SerialPort.BAUDRATE_4800;
        baudRates[5] = SerialPort.BAUDRATE_9600;
        baudRates[6] = SerialPort.BAUDRATE_14400;
        baudRates[7] = SerialPort.BAUDRATE_19200;
        baudRates[8] = SerialPort.BAUDRATE_38400;
        baudRates[9] = SerialPort.BAUDRATE_57600;
        baudRates[10] = SerialPort.BAUDRATE_115200;
        baudRates[11] = SerialPort.BAUDRATE_128000;
        baudRates[12] = SerialPort.BAUDRATE_256000;
        return baudRates;
    }

}
