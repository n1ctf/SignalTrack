/*******************************************************************************
 *     SDR Trunk 
 *     Copyright (C) 2014 Dennis Sheirer
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

import javax.usb.UsbDeviceDescriptor;

public enum TunerClass {
	AIRSPY(TunerType.AIRSPY_R820T, "1D50", "60A1", "Airspy", "Airspy"),
	GENERIC_2832(TunerType.RTL2832_VARIOUS, "0BDA", "2832", "RTL2832", "SDR"),
	GENERIC_2838(TunerType.RTL2832_VARIOUS, "0BDA", "2838", "RTL2832", "SDR"),
	COMPRO_VIDEOMATE_U620F(TunerType.ELONICS_E4000, "185B", "0620", "Compro", "Videomate U620F"),
	COMPRO_VIDEOMATE_U650F(TunerType.ELONICS_E4000, "185B", "0650", "Compro", "Videomate U620F"),
	COMPRO_VIDEOMATE_U680F(TunerType.ELONICS_E4000, "185B", "0680", "Compro", "Videomate U620F"),
	DEXATEK_LOGILINK_VG002A(TunerType.FCI_FC2580, "1D19", "1101", "Dexatek", "Logilink VG0002A"),
	DEXATEK_DIGIVOX_MINI_II_REV3(TunerType.FCI_FC2580, "1D19", "1102", "Dexatek", "MSI Digivox Mini II v3.0"),
	DEXATEK_5217_DVBT(TunerType.FCI_FC2580, "1D19", "1103", "Dexatek", "5217 DVB-T"),
	ETTUS_USRP_B100(TunerType.ETTUS_VARIOUS, "2500", "0002", "Ettus Research", "USRP B100"),
	FUNCUBE_DONGLE_PRO(TunerType.FUNCUBE_DONGLE_PRO, "04D8", "FB56", "Hamlincrest", "Funcube Dongle Pro"),
	FUNCUBE_DONGLE_PRO_PLUS(TunerType.FUNCUBE_DONGLE_PRO_PLUS, "04D8", "FB31", "Hamlincrest",
			"Funcube Dongle Pro Plus"),
	GIGABYTE_GTU7300(TunerType.FITIPOWER_FC0012, "1B80", "D393", "Gigabyte", "GT-U7300"),
	GTEK_T803(TunerType.FITIPOWER_FC0012, "1F4D", "B803", "GTek", "T803"),
	HACKRF_ONE(TunerType.HACKRF, "1D50", "6089", "Great Scott Gadgets", "HackRF One"),
	RAD1O(TunerType.HACKRF, "1D50", "CC15", "Munich hackerspace", "Rad1o"),
	LIFEVIEW_LV5T_DELUXE(TunerType.FITIPOWER_FC0012, "1F4D", "C803", "Liveview", "LV5T Deluxe"),
	MYGICA_TD312(TunerType.FITIPOWER_FC0012, "1F4D", "D286", "MyGica", "TD312"),
	PEAK_102569AGPK(TunerType.FITIPOWER_FC0012, "1B80", "D395", "Peak", "102569AGPK"),
	PROLECTRIX_DV107669(TunerType.FITIPOWER_FC0012, "1F4D", "D803", "Prolectrix", "DV107669"),
	SVEON_STV20(TunerType.FITIPOWER_FC0012, "1B80", "D39D", "Sveon", "STV20 DVB-T USB & FM"),
	TERRATEC_CINERGY_T_REV1(TunerType.FITIPOWER_FC0012, "0CCD", "00A9", "Terratec", "Cinergy T R1"),
	TERRATEC_CINERGY_T_REV3(TunerType.ELONICS_E4000, "0CCD", "00D3", "Terratec", "Cinergy T R3"),
	TERRATEC_NOXON_REV1_B3(TunerType.FITIPOWER_FC0013, "0CCD", "00B3", "Terratec", "NOXON R1 (B3)"),
	TERRATEC_NOXON_REV1_B4(TunerType.FITIPOWER_FC0013, "0CCD", "00B4", "Terratec", "NOXON R1 (B4)"),
	TERRATEC_NOXON_REV1_B7(TunerType.FITIPOWER_FC0013, "0CCD", "00B7", "Terratec", "NOXON R1 (B7)"),
	TERRATEC_NOXON_REV1_C6(TunerType.FITIPOWER_FC0013, "0CCD", "00C6", "Terratec", "NOXON R1 (C6)"),
	TERRATEC_NOXON_REV2(TunerType.ELONICS_E4000, "0CCD", "00E0", "Terratec", "NOXON R2"),
	TERRATEC_T_STICK_PLUS(TunerType.ELONICS_E4000, "0CCD", "00D7", "Terratec", "T Stick Plus"),
	TWINTECH_UT40(TunerType.FITIPOWER_FC0013, "1B80", "D3A4", "Twintech", "UT-40"),
	ZAAPA_ZTMINDVBZP(TunerType.FITIPOWER_FC0012, "1B80", "D398", "Zaapa", "ZT-MINDVBZP"),
	UNKNOWN(TunerType.UNKNOWN, "0", "0", "Unknown Manufacturer", "Unknown Device");

	private TunerType mTunerType;
	private String mVendorID;
	private String mDeviceID;
	private String mVendorDescription;
	private String mDeviceDescription;

	private TunerClass(TunerType tunerType, String vendorID, String deviceID, String vendorDescription,
			String deviceDescription) {
		mTunerType = tunerType;
		mVendorID = vendorID;
		mDeviceID = deviceID;
		mVendorDescription = vendorDescription;
		mDeviceDescription = deviceDescription;
	}

	@Override
	public String toString() {
		return "USB" + " Tuner:" + mTunerType.toString() + " Vendor:" + mVendorDescription + " Device:"
				+ mDeviceDescription + " Address:" + mVendorID + ":" + mDeviceID;
	}

	public String getVendorDeviceLabel() {
		return mVendorDescription + " " + mDeviceDescription;
	}

	public TunerType getTunerType() {
		return mTunerType;
	}

	public static TunerClass valueOf(UsbDeviceDescriptor descriptor) {
		return valueOf(descriptor.idVendor(), descriptor.idProduct());
	}

	public static TunerClass valueOf(short vendor, short product) {
		TunerClass retVal = TunerClass.UNKNOWN;

		// Cast the short to integer so that we can switch on unsigned numbers
		final int vendorID = vendor & 0xFFFF;
		final int productID = product & 0xFFFF;

		switch (vendorID) {
			case 1240 -> {
				switch (productID) {
					case 64305 -> retVal = FUNCUBE_DONGLE_PRO_PLUS;
					case 64342 -> retVal = FUNCUBE_DONGLE_PRO;
				}
			}
			case 3034 -> {
				switch (productID) {
					case 10290 -> retVal = GENERIC_2832;
					case 10296 -> retVal = GENERIC_2838;
				}
			}
			case 6235 -> {
				switch (productID) {
					case 1568 -> retVal = COMPRO_VIDEOMATE_U620F;
					case 1616 -> retVal = COMPRO_VIDEOMATE_U650F;
					case 1664 -> retVal = COMPRO_VIDEOMATE_U680F;
				}
			}
			case 7040 -> {
				switch (productID) {
					case 54163 -> retVal = GIGABYTE_GTU7300;
					case 54165 -> retVal = PEAK_102569AGPK;
					case 54168 -> retVal = ZAAPA_ZTMINDVBZP;
					case 54173 -> retVal = SVEON_STV20;
					case 54180 -> retVal = TWINTECH_UT40;
				}
			}
			case 7449 -> {
				switch (productID) {
					case 4353 -> retVal = DEXATEK_LOGILINK_VG002A;
					case 4354 -> retVal = DEXATEK_DIGIVOX_MINI_II_REV3;
					case 4355 -> retVal = DEXATEK_5217_DVBT;
				}
			}
			case 7504 -> {
				switch (productID) {
					case 24713 -> retVal = HACKRF_ONE;
					case 24737 -> retVal = AIRSPY;
					case 52245 -> retVal = HACKRF_ONE;
				}
			}
			case 8013 -> {
				switch (productID) {
					case 47107 -> retVal = GTEK_T803;
					case 51203 -> retVal = LIFEVIEW_LV5T_DELUXE;
					case 53894 -> retVal = MYGICA_TD312;
					case 55299 -> retVal = PROLECTRIX_DV107669;
				}
			}
			case 3277 -> {
				switch (productID) {
					case 169 -> retVal = TERRATEC_CINERGY_T_REV1;
					case 179 -> retVal = TERRATEC_NOXON_REV1_B3;
					case 180 -> retVal = TERRATEC_NOXON_REV1_B4;
					case 181 -> retVal = TERRATEC_NOXON_REV1_B7;
					case 198 -> retVal = TERRATEC_NOXON_REV1_C6;
					case 211 -> retVal = TERRATEC_CINERGY_T_REV3;
					case 215 -> retVal = TERRATEC_T_STICK_PLUS;
					case 224 -> retVal = TERRATEC_NOXON_REV2;
				}
			}
			case 9472 -> {
				switch (productID) {
					case 2 -> retVal = ETTUS_USRP_B100;
				}
			}
			default -> {
				// NOOP
			}
		}

		return retVal;
	}

	public String getVendorID() {
		return mVendorID;
	}

	public String getDeviceID() {
		return mDeviceID;
	}

	public String getVendorDescription() {
		return mVendorDescription;
	}

	public String getDeviceDescription() {
		return mDeviceDescription;
	}
}
