/*******************************************************************************
 *     Based on the port for SDR Trunk 
 *     Copyright (C) 2014 Dennis Sheirer
 *     
 *     Java version based on librtlsdr
 *     Copyright (C) 2012-2013 by Steve Markgraf <steve@steve-m.de>
 *     Copyright (C) 2012 by Dimitri Stolnikov <horiz0n@gmx.net>
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>
 ******************************************************************************/
package com.g0kla.rtlsdr4java;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.usb.UsbDisconnectedException;
import javax.usb.UsbException;

import org.usb4java.Device;
import org.usb4java.DeviceDescriptor;
import org.usb4java.DeviceHandle;
import org.usb4java.LibUsb;
import org.usb4java.LibUsbException;
import org.usb4java.Transfer;
import org.usb4java.TransferCallback;

//import com.g0kla.rtlsdr4java.ThreadPoolManager.ThreadType;

public abstract class RTL2832TunerController {
	public static final int INT_NULL_VALUE = -1;
	public static final long LONG_NULL_VALUE = -1l;
	public static final double DOUBLE_NULL_VALUE = -1.0D;
	public static final int TWO_TO_22_POWER = 4194304;

	public static final byte USB_INTERFACE = (byte) 0x0;
	public static final byte USB_BULK_ENDPOINT = (byte) 0x81;
	public static final byte CONTROL_ENDPOINT_IN = (byte) (LibUsb.ENDPOINT_IN | LibUsb.REQUEST_TYPE_VENDOR);
	public static final byte CONTROL_ENDPOINT_OUT = (byte) (LibUsb.ENDPOINT_OUT | LibUsb.REQUEST_TYPE_VENDOR);

	public static final long TIMEOUT_US = 1000000l; // uSeconds

	public static final byte REQUEST_ZERO = (byte) 0;

	public static final int TRANSFER_BUFFER_POOL_SIZE = 16;
	private static final int THREAD_POOL_SIZE = 1;

	public static final byte EEPROM_ADDRESS = (byte) 0xA0;

	protected static final byte[] sFIR_COEFFICIENTS = { (byte) 0xCA, (byte) 0xDC, (byte) 0xD7, (byte) 0xD8, (byte) 0xE0,
			(byte) 0xF2, (byte) 0x0E, (byte) 0x35, (byte) 0x06, (byte) 0x50, (byte) 0x9C, (byte) 0x0D, (byte) 0x71,
			(byte) 0x11, (byte) 0x14, (byte) 0x71, (byte) 0x74, (byte) 0x19, (byte) 0x41, (byte) 0xA5 };

	public static final SampleRate DEFAULT_SAMPLE_RATE = SampleRate.RATE_0_960MHZ;
	// SampleRate.RATE_0_240MHZ;
	protected Device mDevice;
	protected DeviceDescriptor mDeviceDescriptor;
	protected DeviceHandle mDeviceHandle;

	private SampleRate mSampleRate = DEFAULT_SAMPLE_RATE;

	private BufferProcessor mBufferProcessor;

	private final ByteSampleAdapter mSampleAdapter = new ByteSampleAdapter();

	private final Broadcaster<ComplexBuffer> mComplexBufferBroadcaster = new Broadcaster<>();

	private final LinkedTransferQueue<byte[]> mFilledBuffers = new LinkedTransferQueue<>();

	private SampleRateMonitor mSampleRateMonitor;
	private final AtomicInteger mSampleCounter = new AtomicInteger();
	private static final DecimalFormat mDecimalFormatter = new DecimalFormat("###,###,###.0");
	private static final DecimalFormat mPercentFormatter = new DecimalFormat("###.00");

	protected int mOscillatorFrequency = 28800000; // 28.8 MHz
	public static final int mBufferSize = 131072;

	protected Descriptor mDescriptor;
	ScheduledExecutorService threadPoolExec;

	/**
	 * Abstract tuner controller device. Use the static getTunerClass() method to
	 * determine the tuner type, and construct the corresponding child tuner
	 * controller class for that tuner type.
	 */
	protected RTL2832TunerController(Device device, DeviceDescriptor deviceDescriptor, long minTunableFrequency,
			long maxTunableFrequency, int centerUnusableBandwidth, double usableBandwidthPercentage)
			throws DeviceException {

		mDevice = device;
		mDeviceDescriptor = deviceDescriptor;
	}

	public void init(SampleRate sampleRate) throws DeviceException {
		mDeviceHandle = new DeviceHandle();

		int result = LibUsb.open(mDevice, mDeviceHandle);

		if (result != LibUsb.SUCCESS) {
			mDeviceHandle = null;

			throw new DeviceException(
					"libusb couldn't open RTL2832 usb " + "device [" + LibUsb.errorName(result) + "]");
		}

		claimInterface(mDeviceHandle);

		try {
			setSampleRate(sampleRate);
		} catch (Exception e) {
			throw new DeviceException(
					"RTL2832 Tuner Controller - couldn't " + "set default sample rate " + e.getMessage());
		}

		byte[] eeprom = null;

		try {
			/* Read the contents of the 256-byte EEPROM */
			eeprom = readEEPROM(mDeviceHandle, (short) 0, 256);
		} catch (Exception e) {
			throw new DeviceException("error while reading the EEPROM device descriptor " + e.getMessage());
		}

		try {
			mDescriptor = new Descriptor(eeprom);

			if (eeprom == null) {
				System.out.println("eeprom byte array was null - constructed: empty descriptor object");
			}
		} catch (Exception e) {
			System.out.println("error while constructing device descriptor using " + "descriptor byte array "
					+ (eeprom == null ? "[null]" : Arrays.toString(eeprom)) + e.getMessage());
		}
	}

	/**
	 * Claims the USB interface. Attempts to detach the active kernel driver if one
	 * is currently attached.
	 */
	public static void claimInterface(DeviceHandle handle) throws DeviceException {
		if (handle != null) {
			int result = LibUsb.kernelDriverActive(handle, USB_INTERFACE);

			if (result == 1) {
				result = LibUsb.detachKernelDriver(handle, USB_INTERFACE);

				if (result != LibUsb.SUCCESS) {
					throw new DeviceException(
							"ERROR: failed attempt to detach kernel driver [" + LibUsb.errorName(result) + "]");
				}
			}

			result = LibUsb.claimInterface(handle, USB_INTERFACE);

			if (result != LibUsb.SUCCESS) {
				throw new DeviceException("couldn't claim usb interface [" + LibUsb.errorName(result) + "]");
			}
		} else {
			throw new DeviceException("couldn't claim usb interface - no " + "device handle");
		}
	}

	public static void releaseInterface(DeviceHandle handle) throws DeviceException {
		int result = LibUsb.releaseInterface(handle, USB_INTERFACE);

		if (result != LibUsb.SUCCESS) {
			throw new DeviceException("couldn't release interface [" + LibUsb.errorName(result) + "]");
		}
	}

	/**
	 * Descriptor contains all identifiers and labels parsed from the EEPROM.
	 * 
	 * May return null if unable to get 256 byte eeprom descriptor from tuner or if
	 * the descriptor doesn't begin with byte values of 0x28 and 0x32 meaning it is
	 * a valid (and can be parsed) RTL2832 descriptor
	 */
	public Descriptor getDescriptor() {
		if (mDescriptor != null && mDescriptor.isValid()) {
			return mDescriptor;
		}

		return null;
	}

	public void setSamplingMode(SampleMode mode) throws LibUsbException {
		switch (mode) {
		case QUADRATURE:
			/* Set intermediate frequency to 0 Hz */
			setIFFrequency(0);

			/* Enable I/Q ADC Input */
			writeDemodRegister(mDeviceHandle, Page.ZERO, (short) 0x08, (short) 0xCD, 1);

			/* Enable zero-IF mode */
			writeDemodRegister(mDeviceHandle, Page.ONE, (short) 0xB1, (short) 0x1B, 1);

			/* Set default i/q path */
			writeDemodRegister(mDeviceHandle, Page.ZERO, (short) 0x06, (short) 0x80, 1);
			break;
		case DIRECT:
		default:
			throw new LibUsbException("QUADRATURE mode is the only mode " + "currently supported",
					LibUsb.ERROR_NOT_SUPPORTED);
		}
	}

	public void setIFFrequency(int frequency) throws LibUsbException {
		long ifFrequency = ((long) TWO_TO_22_POWER * (long) frequency) / mOscillatorFrequency * -1;

		/* Write byte 2 (high) */
		writeDemodRegister(mDeviceHandle, Page.ONE, (short) 0x19, (short) (Long.rotateRight(ifFrequency, 16) & 0x3F),
				1);

		/* Write byte 1 (middle) */
		writeDemodRegister(mDeviceHandle, Page.ONE, (short) 0x1A, (short) (Long.rotateRight(ifFrequency, 8) & 0xFF), 1);

		/* Write byte 0 (low) */
		writeDemodRegister(mDeviceHandle, Page.ONE, (short) 0x1B, (short) (ifFrequency & 0xFF), 1);
	}

	public abstract void initTuner(boolean controlI2CRepeater) throws UsbException;

	/**
	 * Provides a unique identifier to use in distinctly identifying this tuner from
	 * among other tuners of the same type, so that we can fetch a tuner
	 * configuration from the settings manager for this specific tuner.
	 * 
	 * @return serial number of the device
	 */
	public String getUniqueID() {
		if (mDescriptor != null && mDescriptor.hasSerial()) {
			return mDescriptor.getSerial();
		} else {
			int serial = (0xFF & mDeviceDescriptor.iSerialNumber());

			return "SER#" + serial;
		}
	}

	public abstract void setSampleRateFilters(int sampleRate) throws DeviceException;

	public abstract TunerType getTunerType();

	public static TunerType identifyTunerType(Device device) throws DeviceException {
		DeviceHandle handle = new DeviceHandle();

		int reason = LibUsb.open(device, handle);

		if (reason != LibUsb.SUCCESS) {
			throw new DeviceException(
					"couldn't open device - check permissions" + " (udev.rule) [" + LibUsb.errorName(reason) + "]");
		}

		TunerType tunerClass = TunerType.UNKNOWN;

		try {
			claimInterface(handle);

			/* Perform a dummy write to see if the device needs reset */
			boolean resetRequired = false;

			try {
				writeRegister(handle, Block.USB, Address.USB_SYSCTL.getAddress(), 0x09, 1);
			} catch (LibUsbException e) {

				if (e.getErrorCode() < 0) {
//					Log.errorDialog( "error performing dummy write - attempting "
//							+ "device reset", e.getMessage() );

					resetRequired = true;
				} else {
					throw new DeviceException("error performing dummy write " + "to device ["
							+ LibUsb.errorName(e.getErrorCode()) + "] " + e.getMessage());
				}
			}

			if (resetRequired) {
				reason = LibUsb.resetDevice(handle);

				try {
					writeRegister(handle, Block.USB, Address.USB_SYSCTL.getAddress(), 0x09, 1);
				} catch (LibUsbException e2) {
					throw new DeviceException(
							"device reset attempted, but lost device handle. Try restarting the application to use this device");
				}
			}

			/* Initialize the baseband */
			initBaseband(handle);

			enableI2CRepeater(handle, true);

			boolean controlI2CRepeater = false;

			/* Test for each tuner type until we find the correct one */
			if (isTuner(TunerTypeCheck.E4K, handle, controlI2CRepeater)) {
				tunerClass = TunerType.ELONICS_E4000;
			} else if (isTuner(TunerTypeCheck.FC0013, handle, controlI2CRepeater)) {
				tunerClass = TunerType.FITIPOWER_FC0013;
			} else if (isTuner(TunerTypeCheck.R820T, handle, controlI2CRepeater)) {
				tunerClass = TunerType.RAFAELMICRO_R820T;
			} else if (isTuner(TunerTypeCheck.R828D, handle, controlI2CRepeater)) {
				tunerClass = TunerType.RAFAELMICRO_R828D;
			} else if (isTuner(TunerTypeCheck.FC2580, handle, controlI2CRepeater)) {
				tunerClass = TunerType.FCI_FC2580;
			} else if (isTuner(TunerTypeCheck.FC0012, handle, controlI2CRepeater)) {
				tunerClass = TunerType.FITIPOWER_FC0012;
			}

			enableI2CRepeater(handle, false);

			releaseInterface(handle);

			LibUsb.close(handle);
		} catch (Exception e) {
			throw new DeviceException("error while determining tuner type: " + e.getMessage());
		}

		return tunerClass;
	}

	/**
	 * Releases the USB interface
	 * 
	 * @throws DeviceException
	 */
	public void release() throws DeviceException {
		try {
			if (mBufferProcessor.isRunning()) {
				mBufferProcessor.stop();
			}

			LibUsb.releaseInterface(mDeviceHandle, USB_INTERFACE);

			// LibUsb.exit( null );
		} catch (Exception e) {
			throw new DeviceException("attempt to release USB interface failed: " + e.getMessage());
		}
	}

	public void resetUSBBuffer() throws LibUsbException {
		writeRegister(mDeviceHandle, Block.USB, Address.USB_EPA_CTL.getAddress(), 0x1002, 2);
		writeRegister(mDeviceHandle, Block.USB, Address.USB_EPA_CTL.getAddress(), 0x0000, 2);
	}

	public static void initBaseband(DeviceHandle handle) throws LibUsbException {
		/* Initialize USB */
		writeRegister(handle, Block.USB, Address.USB_SYSCTL.getAddress(), 0x09, 1);
		writeRegister(handle, Block.USB, Address.USB_EPA_MAXPKT.getAddress(), 0x0002, 2);
		writeRegister(handle, Block.USB, Address.USB_EPA_CTL.getAddress(), 0x1002, 2);

		/* Power on demod */
		writeRegister(handle, Block.SYS, Address.DEMOD_CTL_1.getAddress(), 0x22, 1);
		writeRegister(handle, Block.SYS, Address.DEMOD_CTL.getAddress(), 0xE8, 1);

		/* Reset demod */
		writeDemodRegister(handle, Page.ONE, (short) 0x01, 0x14, 1); // Bit 3 = soft reset
		writeDemodRegister(handle, Page.ONE, (short) 0x01, 0x10, 1);

		/* Disable spectrum inversion and adjacent channel rejection */
		writeDemodRegister(handle, Page.ONE, (short) 0x15, 0x00, 1);
		writeDemodRegister(handle, Page.ONE, (short) 0x16, 0x0000, 2);

		/* Clear DDC shift and IF frequency registers */
		writeDemodRegister(handle, Page.ONE, (short) 0x16, 0x00, 1);
		writeDemodRegister(handle, Page.ONE, (short) 0x17, 0x00, 1);
		writeDemodRegister(handle, Page.ONE, (short) 0x18, 0x00, 1);
		writeDemodRegister(handle, Page.ONE, (short) 0x19, 0x00, 1);
		writeDemodRegister(handle, Page.ONE, (short) 0x1A, 0x00, 1);
		writeDemodRegister(handle, Page.ONE, (short) 0x1B, 0x00, 1);

		/* Set FIR coefficients */
		for (int x = 0; x < sFIR_COEFFICIENTS.length; x++) {
			writeDemodRegister(handle, Page.ONE, (short) (0x1C + x), sFIR_COEFFICIENTS[x], 1);
		}

		/* Enable SDR mode, disable DAGC (bit 5) */
		writeDemodRegister(handle, Page.ZERO, (short) 0x19, 0x05, 1);

		/* Init FSM state-holding register */
		writeDemodRegister(handle, Page.ONE, (short) 0x93, 0xF0, 1);
		writeDemodRegister(handle, Page.ONE, (short) 0x94, 0x0F, 1);

		/* Disable AGC (en_dagc, bit 0) (seems to have no effect) */
		writeDemodRegister(handle, Page.ONE, (short) 0x11, 0x00, 1);

		/* Disable RF and IF AGC loop */
		writeDemodRegister(handle, Page.ONE, (short) 0x04, 0x00, 1);

		/* Disable PID filter */
		writeDemodRegister(handle, Page.ZERO, (short) 0x61, 0x60, 1);

		/* opt_adc_iq = 0, default ADC_I/ADC_Q datapath */
		writeDemodRegister(handle, Page.ZERO, (short) 0x06, 0x80, 1);

		/*
		 * Enable Zero-if mode (en_bbin bit), DC cancellation (en_dc_est), IQ
		 * estimation/compensation (en_iq_comp, en_iq_est)
		 */
		writeDemodRegister(handle, Page.ONE, (short) 0xB1, 0x1B, 1);

		/* Disable 4.096 MHz clock output on pin TP_CK0 */
		writeDemodRegister(handle, Page.ZERO, (short) 0x0D, 0x83, 1);
	}

	protected void deinitBaseband(DeviceHandle handle)
			throws IllegalArgumentException, UsbDisconnectedException, UsbException {
		writeRegister(handle, Block.SYS, Address.DEMOD_CTL.getAddress(), 0x20, 1);
	}

	/**
	 * Sets the General Purpose Input/Output (GPIO) register bit
	 * 
	 * @param handle  - USB tuner device
	 * @param bitMask - bit mask with one for targeted register bits and zero for
	 *                the non-targeted register bits
	 * @param enabled - true to set the bit and false to clear the bit
	 * @throws UsbDisconnectedException - if the tuner device is disconnected
	 * @throws UsbException             - if there is a USB error while
	 *                                  communicating with the device
	 */
	protected static void setGPIOBit(DeviceHandle handle, byte bitMask, boolean enabled) throws LibUsbException {
		// Get current register value
		int value = readRegister(handle, Block.SYS, Address.GPO.getAddress(), 1);

		// Update the masked bits
		if (enabled) {
			value |= bitMask;
		} else {
			value &= ~bitMask;
		}

		// Write the change back to the device
		writeRegister(handle, Block.SYS, Address.GPO.getAddress(), value, 1);
	}

	/**
	 * Enables GPIO Output
	 * 
	 * @param handle  - usb tuner device
	 * @param bitMask - mask containing one bit value in targeted bit field(s)
	 * @throws UsbDisconnectedException
	 * @throws UsbException
	 */
	protected static void setGPIOOutput(DeviceHandle handle, byte bitMask) throws LibUsbException {
		// Get current register value
		int value = readRegister(handle, Block.SYS, Address.GPD.getAddress(), 1);

		// Mask the value and rewrite it
		writeRegister(handle, Block.SYS, Address.GPO.getAddress(), value & ~bitMask, 1);

		// Get current register value
		value = readRegister(handle, Block.SYS, Address.GPOE.getAddress(), 1);

		// Mask the value and rewrite it
		writeRegister(handle, Block.SYS, Address.GPOE.getAddress(), value | bitMask, 1);
	}

	protected static void enableI2CRepeater(DeviceHandle handle, boolean enabled) throws LibUsbException {
		Page page = Page.ONE;
		short address = 1;
		int value = enabled ? 0x18 : 0x10;

		writeDemodRegister(handle, page, address, value, 1);
	}

	protected boolean isI2CRepeaterEnabled() throws DeviceException {
		int register = readDemodRegister(mDeviceHandle, Page.ONE, (short) 0x1, 1);

		return register == 0x18;
	}

	protected static int readI2CRegister(DeviceHandle handle, byte i2CAddress, byte i2CRegister,
			boolean controlI2CRepeater) throws LibUsbException {
		short address = (short) (i2CAddress & 0xFF);

		ByteBuffer buffer = ByteBuffer.allocateDirect(1);
		buffer.put(i2CRegister);
		buffer.rewind();

		ByteBuffer data = ByteBuffer.allocateDirect(1);

		if (controlI2CRepeater) {
			enableI2CRepeater(handle, true);

			write(handle, address, Block.I2C, buffer);
			read(handle, address, Block.I2C, data);

			enableI2CRepeater(handle, false);
		} else {
			write(handle, address, Block.I2C, buffer);
			read(handle, address, Block.I2C, data);
		}

		return (data.get() & 0xFF);
	}

	protected void writeI2CRegister(DeviceHandle handle, byte i2CAddress, byte i2CRegister, byte value,
			boolean controlI2CRepeater) throws LibUsbException {

		short address = (short) (i2CAddress & 0xFF);

		ByteBuffer buffer = ByteBuffer.allocateDirect(2);
		buffer.put(i2CRegister);
		buffer.put(value);

		buffer.rewind();

		if (controlI2CRepeater) {
			enableI2CRepeater(handle, true);
			write(handle, address, Block.I2C, buffer);
			enableI2CRepeater(handle, false);
		} else {
			write(handle, address, Block.I2C, buffer);
		}
	}

	protected static void writeDemodRegister(DeviceHandle handle, Page page, short address, int value, int length) throws LibUsbException {
		ByteBuffer buffer = ByteBuffer.allocateDirect(length);
		buffer.order(ByteOrder.BIG_ENDIAN);

		switch (length) {
			case 1 -> buffer.put((byte) (value & 0xFF));
			case 2 -> buffer.putShort((short) (value & 0xFFFF));
			default -> throw new IllegalArgumentException(
					"Cannot write value greater " + "than 16 bits to the register - length [" + length + "]");
		}

		short index = (short) (0x10 | page.getPage());

		short newAddress = (short) (address << 8 | 0x20);

		write(handle, newAddress, index, buffer);

		readDemodRegister(handle, Page.TEN, (short) 1, length);
	}

	protected static int readDemodRegister(DeviceHandle handle, Page page, short address, int length) throws LibUsbException {
		short index = page.getPage();
		short newAddress = (short) ((address << 8) | 0x20);

		ByteBuffer buffer = ByteBuffer.allocateDirect(length);

		read(handle, newAddress, index, buffer);
		buffer.order(ByteOrder.LITTLE_ENDIAN);

		return length == 2 ? (buffer.getShort() & 0xFFFF) : (buffer.get() & 0xFF);
	}

	protected static void writeRegister(DeviceHandle handle, Block block, short address, int value, int length) throws LibUsbException {
		ByteBuffer buffer = ByteBuffer.allocateDirect(length);
		buffer.order(ByteOrder.BIG_ENDIAN);

		switch (length) {
			case 1 -> buffer.put((byte) (value & 0xFF));
			case 2 -> buffer.putShort((short) value);
			default -> throw new IllegalArgumentException(
					"Cannot write value greater " + "than 16 bits to the register - length [" + length + "]");
		}

		buffer.rewind();

		write(handle, address, block, buffer);
	}

	protected static int readRegister(DeviceHandle handle, Block block, short address, int length) throws LibUsbException {
		ByteBuffer buffer = ByteBuffer.allocateDirect(2);

		read(handle, address, block, buffer);

		buffer.order(ByteOrder.LITTLE_ENDIAN);

		return length == 2 ? (buffer.getShort() & 0xFFFF) : (buffer.get() & 0xFF);
	}

	/**
	 */
	protected static void write(DeviceHandle handle, short address, Block block, ByteBuffer buffer) throws LibUsbException {
		write(handle, address, block.getWriteIndex(), buffer);
	}

	protected static void write(DeviceHandle handle, short value, short index, ByteBuffer buffer) throws LibUsbException {
		if (handle != null) {
			int transferred = LibUsb.controlTransfer(handle, CONTROL_ENDPOINT_OUT, REQUEST_ZERO, value, index, buffer, TIMEOUT_US);

			if (transferred < 0) {
				throw new LibUsbException("error writing byte buffer", transferred);
			} else if (transferred != buffer.capacity()) {
				throw new LibUsbException(
						"transferred bytes [" + transferred + "] is not what was expected [" + buffer.capacity() + "]",
						transferred);
			}
		} else {
			throw new LibUsbException("device handle is null", LibUsb.ERROR_NO_DEVICE);
		}
	}

	/**
	 * Performs a control type read
	 */
	protected static void read(DeviceHandle handle, short address, short index, ByteBuffer buffer) throws LibUsbException {
		if (handle != null) {
			int transferred = LibUsb.controlTransfer(handle, CONTROL_ENDPOINT_IN, REQUEST_ZERO, address, index, buffer, TIMEOUT_US);

			if (transferred < 0) {
				throw new LibUsbException("read error", transferred);
			} else if (transferred != buffer.capacity()) {
				throw new LibUsbException(
						"transferred bytes [" + transferred + "] is not what was expected [" + buffer.capacity() + "]",
						transferred);
			}
		} else {
			throw new LibUsbException("device handle is null", LibUsb.ERROR_NO_DEVICE);
		}
	}

	/**
	 * Reads byte array from index at the address.
	 * 
	 * @return big-endian byte array (needs to be swapped to be usable)
	 * 
	 */
	protected static void read(DeviceHandle handle, short address, Block block, ByteBuffer buffer)
			throws LibUsbException {
		read(handle, address, block.getReadIndex(), buffer);
	}

	/**
	 * Tests if the specified tuner type is contained in the usb tuner device.
	 * 
	 * @param type               - tuner type to test for
	 * @param handle             - handle to the usb tuner device
	 * @param controlI2CRepeater - indicates if the method should control the I2C
	 *                           repeater independently
	 * 
	 * @return - true if the device is the specified tuner type
	 */
	protected static boolean isTuner(TunerTypeCheck type, DeviceHandle handle, boolean controlI2CRepeater) {
		try {
			if (type == TunerTypeCheck.FC0012 || type == TunerTypeCheck.FC2580) {
				/* Initialize the GPIOs */
				setGPIOOutput(handle, (byte) 0x20);

				/* Reset tuner before probing */
				setGPIOBit(handle, (byte) 0x20, true);
				setGPIOBit(handle, (byte) 0x20, false);
			}

			int value = readI2CRegister(handle, type.getI2CAddress(), type.getCheckAddress(), controlI2CRepeater);

			if (type == TunerTypeCheck.FC2580) {
				return ((value & 0x7F) == type.getCheckValue());
			} else {
				return (value == type.getCheckValue());
			}
		} catch (LibUsbException e) {
			// Do nothing ... it's not the specified tuner
		}

		return false;
	}

	public int getCurrentSampleRate() throws DeviceException {
		return mSampleRate.getRate();
	}

	public int getSampleRateFromTuner() throws DeviceException {
		try {
			int high = readDemodRegister(mDeviceHandle, Page.ONE, (short) 0x9F, 2);
			int low = readDemodRegister(mDeviceHandle, Page.ONE, (short) 0xA1, 2);

			int ratio = Integer.rotateLeft(high, 16) | low;

			int rate = (int) (mOscillatorFrequency * TWO_TO_22_POWER / ratio);

			SampleRate sampleRate = SampleRate.getClosest(rate);

			/* If we're not currently set to this rate, set it as the current rate */
			if (sampleRate.getRate() != rate) {
				setSampleRate(sampleRate);

				return sampleRate.getRate();
			}
		} catch (Exception e) {
			throw new DeviceException(
					"RTL2832 Tuner Controller - cannot get " + "current sample rate " + e.getMessage());
		}

		return DEFAULT_SAMPLE_RATE.getRate();
	}

	public void setSampleRate(SampleRate sampleRate) throws DeviceException {
		/* Write high-order 16-bits of sample rate ratio to demod register */
		writeDemodRegister(mDeviceHandle, Page.ONE, (short) 0x9F, sampleRate.getRatioHighBits(), 2);

		/*
		 * Write low-order 16-bits of sample rate ratio to demod register. Note: none of
		 * the defined rates have a low order ratio value, so we simply write a zero to
		 * the register
		 */
		writeDemodRegister(mDeviceHandle, Page.ONE, (short) 0xA1, 0, 2);

		/* Set sample rate correction to 0 */
		setSampleRateFrequencyCorrection(0);

		/* Reset the demod for the changes to take effect */
		writeDemodRegister(mDeviceHandle, Page.ONE, (short) 0x01, 0x14, 1);
		writeDemodRegister(mDeviceHandle, Page.ONE, (short) 0x01, 0x10, 1);

		/* Apply any tuner specific sample rate filter settings */
		setSampleRateFilters(sampleRate.getRate());

		mSampleRate = sampleRate;

		// mFrequencyController.setSampleRate( sampleRate.getRate() );

		if (mSampleRateMonitor != null) {
			mSampleRateMonitor.setSampleRate(mSampleRate.getRate());
		}
	}

	public void setSampleRateFrequencyCorrection(int ppm) throws DeviceException {
		int offset = -ppm * TWO_TO_22_POWER / 1000000;

		writeDemodRegister(mDeviceHandle, Page.ONE, (short) 0x3F, (offset & 0xFF), 1);
		writeDemodRegister(mDeviceHandle, Page.ONE, (short) 0x3E, (Integer.rotateRight(offset, 8) & 0xFF), 1);
		/* Test to retune controller to apply frequency correction */
		try {
			//////////////////mFrequencyController.setFrequency( mFrequencyController.getFrequency() );
		} catch (Exception e) {
			throw new DeviceException("couldn't set sample rate frequency correction " + e.getMessage());
		}
	}

	public int getSampleRateFrequencyCorrection() throws UsbException {
		int high = readDemodRegister(mDeviceHandle, Page.ONE, (short) 0x3E, 1);
		int low = readDemodRegister(mDeviceHandle, Page.ONE, (short) 0x3F, 1);

		return (Integer.rotateLeft(high, 8) | low);
	}

	/**
	 * Returns contents of the 256-byte EEPROM. The contents are as follows:
	 * 
	 * 256-byte EEPROM (in hex): 00/01 - 2832 Signature 03/02 - 0BDA Vendor ID 05/04
	 * - 2832 Product ID 06 - A5 (has serial id?) 07 - 16 (bit field - bit 0 =
	 * remote wakeup, bit 1 = IR enabled 08 - 02 or 12 10/09 0310 ETX(0x03) plus
	 * label length (includes length and ETX bytes) 12/11 First UTF-16 character
	 * 14/13 Second UTF-16 character ...
	 * 
	 * Label 1: vendor Label 2: product Label 3: serial Label 4,5 ... (user defined)
	 */
	public byte[] readEEPROM(DeviceHandle handle, short offset, int length) throws IllegalArgumentException {
		if (offset + length > 256) {
			throw new IllegalArgumentException("cannot read more than 256 "
					+ "bytes from EEPROM - requested to read to byte [" + (offset + length) + "]");
		}

		byte[] data = new byte[length];
		ByteBuffer buffer = ByteBuffer.allocateDirect(1);

		try {
			/* Tell the RTL-2832 to address the EEPROM */
			writeRegister(handle, Block.I2C, EEPROM_ADDRESS, (byte) offset, 1);
		} catch (LibUsbException e) {
//			throw new DeviceException( "usb error while attempting to set read address to "
//				+ "EEPROM register, prior to reading the EEPROM device "
//				+ "descriptor:"+ e.getMessage() );
		}

		for (int x = 0; x < length; x++) {
			try {
				read(handle, EEPROM_ADDRESS, Block.I2C, buffer);
				data[x] = buffer.get();
				buffer.rewind();
			} catch (Exception e) {
//				Log.errorDialog( "error while reading eeprom byte [" + x + "/" + 
//					length + "] aborting eeprom read and returning partially "
//							+ "filled descriptor byte array", e.getMessage() );
				x = length;
			}
		}

		return data;
	}

	/**
	 * Writes a single byte to the 256-byte EEPROM using the specified offset.
	 * 
	 * Note: introduce a 5 millisecond delay between each successive write to the
	 * EEPROM or subsequent writes may fail.
	 */
	public void writeEEPROMByte(DeviceHandle handle, byte offset, byte value)
			throws IllegalArgumentException, UsbDisconnectedException, UsbException {
		if (offset < 0 || offset > 255) {
			throw new IllegalArgumentException(
					"RTL2832 Tuner Controller - " + "EEPROM offset must be within range of 0 - 255");
		}

		int offsetAndValue = Integer.rotateLeft((0xFF & offset), 8) | (0xFF & value);

		writeRegister(handle, Block.I2C, EEPROM_ADDRESS, offsetAndValue, 2);
	}

	public enum Address {
		USB_SYSCTL(0x2000), 
		USB_CTRL(0x2010), 
		USB_STAT(0x2014), 
		USB_EPA_CFG(0x2144), 
		USB_EPA_CTL(0x2148),
		USB_EPA_MAXPKT(0x2158), 
		USB_EPA_MAXPKT_2(0x215A), 
		USB_EPA_FIFO_CFG(0x2160), 
		DEMOD_CTL(0x3000), 
		GPO(0x3001),
		GPI(0x3002), 
		GPOE(0x3003), 
		GPD(0x3004), 
		SYSINTE(0x3005), 
		SYSINTS(0x3006), 
		GP_CFG0(0x3007), 
		GP_CFG1(0x3008),
		SYSINTE_1(0x3009), 
		SYSINTS_1(0x300A), 
		DEMOD_CTL_1(0x300B), 
		IR_SUSPEND(0x300C);

		private int mAddress;

		private Address(int address) {
			mAddress = address;
		}

		public short getAddress() {
			return (short) mAddress;
		}
	}

	public enum Page {
		ZERO(0x0), ONE(0x1), TEN(0xA);

		private int mPage;

		private Page(int page) {
			mPage = page;
		}

		public byte getPage() {
			return (byte) (mPage & 0xFF);
		}
	}

	public enum SampleMode {
		QUADRATURE, DIRECT;
	}

	public enum Block {
		DEMOD(0), 
		USB(1), 
		SYS(2), 
		TUN(3), 
		ROM(4), 
		IR(5), 
		I2C(6); // I2C controller

		private int mValue;

		private Block(int value) {
			mValue = value;
		}

		public int getValue() {
			return mValue;
		}

		/**
		 * Returns the value left shifted 8 bits
		 */
		public short getReadIndex() {
			return (short) Integer.rotateLeft(mValue, 8);
		}

		public short getWriteIndex() {
			return (short) (getReadIndex() | 0x10);
		}
	}

	/**
	 * Sample rates supported by the RTL-2832.
	 * 
	 * Formula to calculate the ratio value:
	 * 
	 * ratio = ( ( crystal_frequency * 2^22 ) / sample_rate ) & ~3
	 * 
	 * Default crystal_frequency is 28,800,000
	 * 
	 * This produces a 32-bit value that has to be set in 2 x 16-bit registers.
	 * Place the high 16-bit value in ratioMSB and the low 16-bit value in ratioLSB.
	 * Use integer for these values to avoid sign-extension issues.
	 * 
	 * Mask the value with 0xFFFF when setting the register.
	 */
	public enum SampleRate {
		/* Note: sample rates below 1.0MHz are subject to aliasing */
		RATE_0_240MHZ(0x1E00, 240000, "0.240 MHz"), 
		RATE_0_288MHZ(0x1900, 288000, "0.288 MHz"),
		RATE_0_960MHZ(0x0780, 960000, "0.960 MHz"), 
		RATE_1_200MHZ(0x0600, 1200000, "1.200 MHz"),
		RATE_1_440MHZ(0x0500, 1440000, "1.440 MHz"), 
		RATE_1_920MHZ(0x03C0, 2016000, "2.016 MHz"),
		RATE_2_304MHZ(0x0320, 2208000, "2.208 MHz"), 
		RATE_2_400MHZ(0x0300, 2400000, "2.400 MHz"),
		RATE_2_880MHZ(0x0280, 2880000, "2.880 MHz");

		private int mRatioHigh;
		private int mRate;
		private String mLabel;

		private SampleRate(int ratioHigh, int rate, String label) {
			mRatioHigh = ratioHigh;
			mRate = rate;
			mLabel = label;
		}

		public int getRatioHighBits() {
			return mRatioHigh;
		}

		public int getRate() {
			return mRate;
		}

		public String getLabel() {
			return mLabel;
		}

		@Override
		public String toString() {
			return mLabel;
		}

		/**
		 * Returns the sample rate that is equal to the argument or the next higher
		 * sample rate
		 * 
		 * @param sampleRate
		 * @return
		 */
		public static SampleRate getClosest(int sampleRate) {
			for (SampleRate rate : values()) {
				if (rate.getRate() >= sampleRate) {
					return rate;
				}
			}
			return DEFAULT_SAMPLE_RATE;
		}
	}

	/**
	 * Adds a sample listener. If the buffer processing thread is not currently
	 * running, starts it running in a new thread.
	 */
	public void addListener(Listener<ComplexBuffer> listener) {
		synchronized (mComplexBufferBroadcaster) {
			mComplexBufferBroadcaster.addListener(listener);

			if (mBufferProcessor == null) {
				mBufferProcessor = new BufferProcessor();
			}

			if (!mBufferProcessor.isRunning()) {
				Thread thread = new Thread(mBufferProcessor);
				thread.setDaemon(true);
				thread.setName("RTL2832 Buffer Processor");

				thread.start();
			}
		}
	}

	/**
	 * Removes the sample listener. If this is the last registered listener, shuts
	 * down the buffer processing thread.
	 */
	public void removeListener(Listener<ComplexBuffer> listener) {
		synchronized (mComplexBufferBroadcaster) {
			mComplexBufferBroadcaster.removeListener(listener);

			if (!mComplexBufferBroadcaster.hasListeners()) {
				mBufferProcessor.stop();
			}

			if (mBufferProcessor == null) {
				return;
			}
			if (mBufferProcessor.isRunning()) {
				mBufferProcessor.stop();
			}
		}
	}

	boolean devicePulled; // set a flag if the device was pulled out

	/**
	 * Buffer processing thread. Fetches samples from the RTL2832 Tuner and
	 * dispatches them to all registered listeners
	 */
	public class BufferProcessor implements Runnable, TransferCallback {
		private ScheduledFuture<?> mSampleDispatcherTask;
		private LinkedTransferQueue<Transfer> mAvailableTransfers;
		private final LinkedTransferQueue<Transfer> mTransfersInProgress = new LinkedTransferQueue<>();
		private final AtomicBoolean mRunning = new AtomicBoolean();
		private ByteBuffer mLibUsbHandlerStatus;
		private boolean mCancel = false;

		public BufferProcessor() {
			threadPoolExec = Executors.newScheduledThreadPool(THREAD_POOL_SIZE);
		}

		@Override
		public void run() {
			if (mRunning.compareAndSet(false, true)) {
				try {
					setSampleRate(mSampleRate);

					resetUSBBuffer();
				} catch (DeviceException e) {
					System.err.println("couldn't start buffer processor: " + e.getMessage());

					mRunning.set(false);
				}

				prepareTransfers();

				try {
					mSampleDispatcherTask = threadPoolExec.scheduleAtFixedRate(new BufferDispatcher(), 0, 20, TimeUnit.MILLISECONDS);
//					mSampleDispatcherTask = mThreadPoolManager.scheduleFixedRate( 
//							ThreadType.SOURCE_SAMPLE_PROCESSING, new BufferDispatcher(), 
//							20, TimeUnit.MILLISECONDS );
				} catch (NullPointerException npe) {
					System.err.println("NPE! = tpm null: " + (threadPoolExec == null) + npe.getMessage());
				}

				mLibUsbHandlerStatus = ByteBuffer.allocateDirect(4);

				final List<Transfer> transfers = new ArrayList<>();

				mCancel = false;

				while (mRunning.get()) {
					if (devicePulled) {
						return;
					}
					mAvailableTransfers.drainTo(transfers);

					transfers.forEach(transfer -> {
						final int result = LibUsb.submitTransfer(transfer);

						if (result == LibUsb.SUCCESS) {
							mTransfersInProgress.add(transfer);
						} else {
							System.err.println("ERROR: Error submitting transfer [" + LibUsb.errorName(result) + "]");
						}
					});

					final int result = LibUsb.handleEventsTimeoutCompleted(null, TIMEOUT_US,
							mLibUsbHandlerStatus.asIntBuffer());

					if (result != LibUsb.SUCCESS) {
						System.err.println("ERROR: error handling events for libusb");
					}

					transfers.clear();

					mLibUsbHandlerStatus.rewind();
				}

				if (mCancel) {
					mTransfersInProgress.forEach(LibUsb::cancelTransfer);

					final int result = LibUsb.handleEventsTimeoutCompleted(null, TIMEOUT_US,
							mLibUsbHandlerStatus.asIntBuffer());

					if (result != LibUsb.SUCCESS) {
						System.err.println("ERROR: handling events for libusb during cancel");
					}

					mLibUsbHandlerStatus.rewind();
				}
			}
		}

		/**
		 * Stops the sample fetching thread
		 */
		public void stop() {
			mCancel = true;

			if (mRunning.compareAndSet(true, false)) {
				if (mSampleDispatcherTask != null) {
					mSampleDispatcherTask.cancel(true);
					mSampleDispatcherTask = null;
					mFilledBuffers.clear();
				}
			}
			threadPoolExec.shutdown();
		}

		/**
		 * Indicates if this thread is running
		 */
		public boolean isRunning() {
			return mRunning.get();
		}

		@Override
		public void processTransfer(Transfer transfer) {
			if (devicePulled) {
				return;
			}
			mTransfersInProgress.remove(transfer);

			switch (transfer.status()) {
				case LibUsb.TRANSFER_COMPLETED, LibUsb.TRANSFER_STALL -> {
					if (transfer.actualLength() > 0) {
						final ByteBuffer buffer = transfer.buffer();
	
						final byte[] data = new byte[transfer.actualLength()];
	
						buffer.get(data);
	
						buffer.rewind();
	
						if (isRunning()) {
							mFilledBuffers.add(data);
						}
					}
				}
				case LibUsb.TRANSFER_CANCELLED -> {
				}
				default -> {
					/* unexpected error */
					System.err.println("ERROR, perhaps device removed? USB data transfer error ["
							+ getTransferStatus(transfer.status()) + "] transferred actual: " + transfer.actualLength()
							+ "\nYou will probablly need to restart to reactivate this device.");
					devicePulled = true;
					return;
				}
			}

			mAvailableTransfers.add(transfer);
		}

		/**
		 * Prepares (allocates) a set of transfer buffers for use in transferring data
		 * from the tuner via the bulk interface
		 */
		private void prepareTransfers() throws LibUsbException {
			if (mAvailableTransfers == null) {
				mAvailableTransfers = new LinkedTransferQueue<>();

				for (int x = 0; x < TRANSFER_BUFFER_POOL_SIZE; x++) {
					final Transfer transfer = LibUsb.allocTransfer();

					if (transfer == null) {
						throw new LibUsbException("couldn't allocate transfer", LibUsb.ERROR_NO_MEM);
					}

					final ByteBuffer buffer = ByteBuffer.allocateDirect(mBufferSize);

					LibUsb.fillBulkTransfer(transfer, mDeviceHandle, USB_BULK_ENDPOINT, buffer, BufferProcessor.this,
							"Buffer", TIMEOUT_US);

					mAvailableTransfers.add(transfer);
				}
			}
		}
	}

	/**
	 * Fetches byte[] chunks from the raw sample buffer. Converts each byte array
	 * and broadcasts the array to all registered listeners
	 */
	public class BufferDispatcher implements Runnable {
		@Override
		public void run() {
			Thread.currentThread().setName("BufferDispatcher");
			try {
				final ArrayList<byte[]> buffers = new ArrayList<>();

				mFilledBuffers.drainTo(buffers);

				buffers.forEach(buffer -> {
					final float[] samples = mSampleAdapter.convert(buffer);

					mComplexBufferBroadcaster.broadcast(new ComplexBuffer(samples));

				});
			} catch (Exception e) {
				System.err.println("error duing rtl2832 buffer dispatcher run " + e.getMessage());
			}
		}
	}

	public static String getTransferStatus(int status) {
		return switch (status) {
			case 0 -> "TRANSFER COMPLETED (0)";
			case 1 -> "TRANSFER ERROR (1)";
			case 2 -> "TRANSFER TIMED OUT (2)";
			case 3 -> "TRANSFER CANCELLED (3)";
			case 4 -> "TRANSFER STALL (4)";
			case 5 -> "TRANSFER NO DEVICE (5)";
			case 6 -> "TRANSFER OVERFLOW (6)";
			default -> "UNKNOWN TRANSFER STATUS (" + status + ")";
		};
	}

	public class USBEventHandlingThread implements Runnable {
		/** If thread should abort. */
		private volatile boolean mAbort;

		/**
		 * Aborts the event handling thread.
		 */
		public void abort() {
			mAbort = true;
		}

		@Override
		public void run() {
			while (!mAbort) {
				final int result = LibUsb.handleEventsTimeout(null, 1000);

				if (result != LibUsb.SUCCESS) {
					mAbort = true;

					System.err.println("ERROR handling usb events [" + LibUsb.errorName(result) + "]");

					throw new LibUsbException("Unable to handle USB " + "events", result);
				}
			}
		}
	}

	public enum TunerTypeCheck {
		E4K(0xC8, 0x02, 0x40), FC0012(0xC6, 0x00, 0xA1), FC0013(0xC6, 0x00, 0xA3), FC2580(0xAC, 0x01, 0x56),
		R820T(0x34, 0x00, 0x69), R828D(0x74, 0x00, 0x69);

		private int mI2CAddress;
		private int mCheckAddress;
		private int mCheckValue;

		private TunerTypeCheck(int i2c, int address, int value) {
			mI2CAddress = i2c;
			mCheckAddress = address;
			mCheckValue = value;
		}

		public byte getI2CAddress() {
			return (byte) mI2CAddress;
		}

		public byte getCheckAddress() {
			return (byte) mCheckAddress;
		}

		public byte getCheckValue() {
			return (byte) mCheckValue;
		}
	}

	/**
	 * Averages the sample rate over a 10-second period. The count is for the number
	 * of bytes received from the tuner. There are two bytes for each sample. So, we
	 * divide by 20 to get the average sample rate.
	 */
	public class SampleRateMonitor implements Runnable {
		private static final int BUFFER_SIZE = 5;
		private int mTargetSampleRate;
		private int mSampleRateMinimum;
		private int mSampleRateMaximum;
		private int mNewTargetSampleRate;
		private final FloatAveragingBuffer mRateErrorBuffer = new FloatAveragingBuffer(BUFFER_SIZE);
		private final AtomicBoolean mRateChanged = new AtomicBoolean();

		public SampleRateMonitor(int sampleRate) {
			setTargetRate(sampleRate);
		}

		public void setSampleRate(int sampleRate) {
			mNewTargetSampleRate = sampleRate;
			mRateChanged.set(true);
		}

		private void setTargetRate(int rate) {
			mTargetSampleRate = rate;
			mSampleRateMinimum = (int) ((float) mTargetSampleRate * 0.95f);
			mSampleRateMaximum = (int) ((float) mTargetSampleRate * 1.05f);

			for (int x = 0; x < BUFFER_SIZE; x++) {
				mRateErrorBuffer.get(0);
			}
		}

		@Override
		public void run() {
			if (mRateChanged.compareAndSet(true, false)) {
				setTargetRate(mNewTargetSampleRate);

				/* Reset the sample counter */
				mSampleCounter.set(0);

				System.err.println("monitor reset for new sample rate [" + mTargetSampleRate + "]");
			} else {
				final int count = mSampleCounter.getAndSet(0);

				final float current = (float) count / 20.0f;

				/**
				 * Only accept values +/- 5% of target rate
				 */
				final float average;

				if (mSampleRateMinimum < current && current < mSampleRateMaximum) {
					average = mRateErrorBuffer.get((float) mTargetSampleRate - current);
				} else {
					average = mTargetSampleRate;
				}

				final StringBuilder sb = new StringBuilder();
				sb.append("[");
				if (mDescriptor != null) {
					sb.append(mDescriptor.getSerial());
				} else {
					sb.append("DESCRIPTOR IS NULL");
				}
				sb.append("] sample rate current [");
				sb.append(mDecimalFormatter.format(current));
				sb.append(" Hz ");
				sb.append(mPercentFormatter.format(100.0f * (current / (float) mTargetSampleRate)));
				sb.append("% ] error [");
				sb.append(average);
				sb.append(" Hz ] target ");
				sb.append(mDecimalFormatter.format(mTargetSampleRate));

				System.err.println(sb.toString());
			}
		}
	}

	/**
	 * RTL2832 EEPROM byte array descriptor parsing class
	 */
	public class Descriptor {
		private byte[] mData;
		private final ArrayList<String> mLabels = new ArrayList<>();

		public Descriptor(byte[] data) {
			if (data != null) {
				mData = data;
			} else {
				data = new byte[256];
			}

			getLabels();
		}

		public boolean isValid() {
			return mData[0] == (byte) 0x28 && mData[1] == (byte) 0x32;
		}

		public String getVendorID() {
			final int id = Integer.rotateLeft((0xFF & mData[3]), 8) | (0xFF & mData[2]);

			return "%04X".formatted(id);
		}

		public String getVendorLabel() {
			return mLabels.get(0);
		}

		public String getProductID() {
			final int id = Integer.rotateLeft((0xFF & mData[5]), 8) | (0xFF & mData[4]);

			return "%04X".formatted(id);
		}

		public String getProductLabel() {
			return mLabels.get(1);
		}

		public boolean hasSerial() {
			return mData[6] == (byte) 0xA5;
		}

		public String getSerial() {
			return mLabels.get(2);
		}

		public boolean remoteWakeupEnabled() {
			byte mask = (byte) 0x01;

			return (mData[7] & mask) == mask;
		}

		public boolean irEnabled() {
			byte mask = (byte) 0x02;

			return (mData[7] & mask) == mask;
		}

		private void getLabels() {
			mLabels.clear();

			int start = 0x09;

			while (start < 256) {
				start = getLabel(start);
			}
		}

		private int getLabel(int start) {
			/* Validate length and check second byte for ETX (0x03) */
			if (start > 254 || mData[start + 1] != (byte) 0x03) {
				return 256;
			}

			/* Get label length, including the length and ETX bytes */
			final int length = 0xFF & mData[start];

			if (start + length > 255) {
				return 256;
			}

			/* Get the label bytes */
			final byte[] data = Arrays.copyOfRange(mData, start + 2, start + length);

			/* Translate the bytes as UTF-16 Little Endian and store the label */
			final String label = new String(data, StandardCharsets.UTF_16LE);

			mLabels.add(label);

			return start + length;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("RTL-2832 EEPROM Descriptor\n");

			sb.append("Vendor: ");
			sb.append(getVendorID());
			sb.append(" [");
			sb.append(getVendorLabel());
			sb.append("]\n");

			sb.append("Product: ");
			sb.append(getProductID());
			sb.append(" [");
			sb.append(getProductLabel());
			sb.append("]\n");

			sb.append("Serial: ");
			if (hasSerial()) {
				sb.append("yes [");
				sb.append(getSerial());
				sb.append("]\n");
			} else {
				sb.append("no\n");
			}

			sb.append("Remote Wakeup Enabled: ");
			sb.append((remoteWakeupEnabled() ? "yes" : "no"));
			sb.append("\n");

			sb.append("IR Enabled: ");
			sb.append((irEnabled() ? "yes" : "no"));
			sb.append("\n");

			if (mLabels.size() > 3) {
				sb.append("Additional Labels: ");

				for (int x = 3; x < mLabels.size(); x++) {
					sb.append(" [");
					sb.append(mLabels.get(x));
					sb.append("\n");
				}
			}

			return sb.toString();
		}
	}

}
