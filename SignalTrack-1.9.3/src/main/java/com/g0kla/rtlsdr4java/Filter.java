package com.g0kla.rtlsdr4java;

public class Filter {
	
	private Filter() {
		throw new IllegalStateException("Utility class");
	}
	
	public static double[] initTest(double sampleRate, double freq, double alpha, int len) {
		final int M = len-1;
		final double[] coeffs = new double[M+1];
		for (int i=0; i < len; i++) {
			coeffs[i] = i+1D;
		}
		return coeffs;
	}
	
	/**
	 * Calculate the values for a Raised Cosine filter
	 * @param sampleRate
	 * @param freq
	 * @param len
	 */
	public static double[] makeRaiseCosine(double sampleRate, double freq, double alpha, int len) {
		final int M = len-1;
		final double[] coeffs = new double[len];
		final double Fc = freq/sampleRate;
		
		double sumofsquares = 0;
		final double[] tempCoeffs = new double[len];
		final int limit = (int)(0.5 / (alpha * Fc));
		for (int i=0; i <= M; i++) {
			final double sinc = (Math.sin(2 * Math.PI * Fc * (i - M/2D)))/ (i - M/2D);
			final double cos = Math.cos(alpha * Math.PI * Fc * (i - M/2D)) / ( 1 - (Math.pow((2 * alpha * Fc * (i - M/2D)),2)));
			
			tempCoeffs[i] = i == M/2D ? 2 * Math.PI * Fc * cos : sinc * cos;
			
			// Care because ( 1 - ( 2 * Math.pow((alpha * Fc * (i - M/2)),2))) is zero for 
			if ((i-M/2) == limit || (i-M/2) == -limit) {
				tempCoeffs[i] = 0.25 * Math.PI * sinc;
			} 
			
			sumofsquares += tempCoeffs[i]*tempCoeffs[i];
		}
		final double gain = Math.sqrt(sumofsquares);
		for (int i=0; i < tempCoeffs.length; i++) {
			coeffs[i] = tempCoeffs[tempCoeffs.length-i-1]/gain;
			//System.out.println(coeffs[i]);
		}
		return coeffs;
	}
}
