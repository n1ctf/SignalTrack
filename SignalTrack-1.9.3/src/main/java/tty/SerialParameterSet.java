package tty;

import java.util.Arrays;
import java.util.List;

public class SerialParameterSet {
    private int parity;
    private int stopBits;
    private int dataBits;
    private int baudRate;
    private int flowControlIn;
    private int flowControlOut;
    private boolean dtr;
    private boolean rts;
    private Integer[] validBaudRates;
    private boolean deviceAssignedBaudRateFixed;
    private boolean deviceAssignedParametersFixed;

    public SerialParameterSet() { }

    public SerialParameterSet(int baudRate, int dataBits, int stopBits, int parity,
            boolean dtr, boolean rts, int flowControlIn, int flowControlOut, Integer[] validBaudRates,
            boolean deviceAssignedParametersFixed, boolean deviceAssignedBaudRateFixed) {
        this.baudRate = baudRate;
        this.dataBits = dataBits;
        this.stopBits = stopBits;
        this.parity = parity;
        this.dtr = dtr;
        this.rts = rts;
        this.flowControlIn = flowControlIn;
        this.flowControlOut = flowControlOut;
		this.validBaudRates = validBaudRates.clone();
		this.deviceAssignedParametersFixed = deviceAssignedParametersFixed;
		this.deviceAssignedBaudRateFixed = deviceAssignedBaudRateFixed;
    }

    public SerialParameterSet(int baudRate, int dataBits, int stopBits, int parity,
            boolean dtr, boolean rts, int flowControlIn, int flowControlOut, List<Integer> validBaudRates,
            boolean deviceAssignedParametersFixed, boolean deviceAssignedBaudRateFixed) {
    	this.baudRate = baudRate;
        this.dataBits = dataBits;
        this.stopBits = stopBits;
        this.parity = parity;
        this.dtr = dtr;
        this.rts = rts;
        this.flowControlIn = flowControlIn;
        this.flowControlOut = flowControlOut;
		this.validBaudRates = validBaudRates.toArray(new Integer[validBaudRates.size()]);
		this.deviceAssignedParametersFixed = deviceAssignedParametersFixed;
		this.deviceAssignedBaudRateFixed = deviceAssignedBaudRateFixed;
    }

    public void setDefaultCommPortParameters(int baudRate, int dataBits, int stopBits, int parity) {
        this.baudRate = baudRate;
        this.dataBits = dataBits;
        this.stopBits = stopBits;
        this.parity = parity;
    }

    public void setDefaultDTR(boolean dtr) {
        this.dtr = dtr;
    }

    public void setDefaultRTS(boolean rts) {
        this.rts = rts;
    }

    public void setDefaultFlowControlIn(int flowControlIn) {
        this.flowControlIn = flowControlIn;
    }

    public void setDefaultFlowControlOut(int flowControlOut) {
        this.flowControlOut = flowControlOut;
    }

    public void setDeviceAssignedParametersFixed(boolean deviceAssignedParametersFixed) {
        this.deviceAssignedParametersFixed = deviceAssignedParametersFixed;
    }

    public boolean isDeviceAssignedParametersFixed() {
        return deviceAssignedParametersFixed;
    }

    public void setDeviceAssignedBaudRateFixed(boolean deviceAssignedBaudRateFixed) {
        this.deviceAssignedBaudRateFixed = deviceAssignedBaudRateFixed;
    }

    public boolean isDeviceAssignedBaudRateFixed() {
        return deviceAssignedBaudRateFixed;
    }

    public int getBaudRate() {
        return baudRate;
    }

    public int getDataBits() {
        return dataBits;
    }

    public int getStopBits() {
        return stopBits;
    }

    public int getParity() {
        return parity;
    }

    public int getFlowControlIn() {
        return flowControlIn;
    }

    public int getFlowControlOut() {
        return flowControlOut;
    }

    public boolean isDTR() {
        return dtr;
    }

    public boolean isRTS() {
        return rts;
    }

    public Integer[] getValidBaudRates() {
        return validBaudRates.clone();
    }

    public void setValidBaudRates(Integer[] validBaudRates) {
        this.validBaudRates = validBaudRates.clone();
    }

    public void setValidBaudRates(List<Integer> validBaudRates) {
        this.validBaudRates = validBaudRates.toArray(new Integer[validBaudRates.size()]);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + baudRate;
        result = prime * result + dataBits;
        result = prime * result + (deviceAssignedBaudRateFixed ? 1231 : 1237);
        result = prime * result + (deviceAssignedParametersFixed ? 1231 : 1237);
        result = prime * result + (dtr ? 1231 : 1237);
        result = prime * result + flowControlIn;
        result = prime * result + flowControlOut;
        result = prime * result + parity;
        result = prime * result + (rts ? 1231 : 1237);
        result = prime * result + stopBits;
        result = prime * result + Arrays.hashCode(validBaudRates);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SerialParameterSet other = (SerialParameterSet) obj;
        if (baudRate != other.baudRate) {
            return false;
        }
        if (dataBits != other.dataBits) {
            return false;
        }
        if (deviceAssignedBaudRateFixed != other.deviceAssignedBaudRateFixed) {
            return false;
        }
        if (deviceAssignedParametersFixed != other.deviceAssignedParametersFixed) {
            return false;
        }
        if (dtr != other.dtr) {
            return false;
        }
        if (flowControlIn != other.flowControlIn) {
            return false;
        }
        if (flowControlOut != other.flowControlOut) {
            return false;
        }
        if (parity != other.parity) {
            return false;
        }
        if (rts != other.rts) {
            return false;
        }
        if (stopBits != other.stopBits) {
            return false;
        }
        return Arrays.equals(validBaudRates, other.validBaudRates);
    }

    @Override
    public String toString() {
        return "SerialParameterSet [parity=" + parity + ", stopBits=" + stopBits + ", dataBits=" + dataBits
                + ", baudRate=" + baudRate + ", flowControlIn=" + flowControlIn + ", flowControlOut=" + flowControlOut
                + ", dtrSupport=" + dtr + ", rtsSupport=" + rts + ", validBaudRates=" + Arrays.toString(validBaudRates)
                + ", deviceAssignedBaudRateFixed=" + deviceAssignedBaudRateFixed + ", deviceAssignedParametersFixed="
                + deviceAssignedParametersFixed + "]";
    }

}
