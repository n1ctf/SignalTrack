package com.g0kla.rtlsdr4java;

public class Tools {
	public static int littleEndian2(byte[] b, int bitsPerSample) {
		final byte b1 = b[0];
		final byte b2 = b[1];
		int value =  ((b2 & 0xff) << 8) | (b1 & 0xff);
		if (value > (Math.pow(2,bitsPerSample-1D)-1)) {
			value = (int) (-1*Math.pow(2,bitsPerSample) + value);
		}
		return value;
	}

	public static int bigEndian2(byte[] b, int bitsPerSample) {
		final byte b1 = b[1];
		final byte b2 = b[0];
		int value =  ((b2 & 0xff) << 8) | (b1 & 0xff);
		if (value > (2^(bitsPerSample-1)-1)) {
			value += -(2 ^ bitsPerSample);
		}
		return value;
	}
	
	public static void getDoublesFromBytes(double[]out, byte[] readBuffer) {
		for (int i = 0; i < out.length; i++) {// 4 bytes for each sample. 2 in each stereo channel.
			final byte[] ab = {readBuffer[4*i],readBuffer[4*i+1]};
			double value =  littleEndian2(ab,16);
			System.out.println(value);
			value /= 32768.0;
			out[i] = value;
		}	
	}
	
	public static double average (double avg, double newSample, int n) {
		avg -= avg / n;
		avg += newSample / n;
		return avg;
	}

	public static double psd(double re, double im, double binBandwidth) {
		return (20*Math.log10(Math.sqrt((re*re) + (im*im))/binBandwidth));
	}	
	
	public static void main(String[] args) {
		final double[] a = {0,0};
		final byte[] data = {10,10,20,20,10,(byte) 0x88,20,20};
        Tools.getDoublesFromBytes(a, data);
        System.out.println(a[0]);
        System.out.println(a[1]);
	}
}
