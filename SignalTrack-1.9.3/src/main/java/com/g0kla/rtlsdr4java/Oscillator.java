package com.g0kla.rtlsdr4java;

public abstract class Oscillator {

	protected static final int TABLE_SIZE = 9600;
	protected double[] sinTable = new double[TABLE_SIZE];
	private int samplesPerSecond = 0;
	private double frequency = 0;
	private double phase = 0;
	private double phaseIncrement = 0;
	
	protected Oscillator(int samples, int freq) {
		this.samplesPerSecond = samples;
		this.frequency = freq;
		this.phaseIncrement = 2 * Math.PI * frequency / samplesPerSecond;
	}

	public void setFrequency(double freq) {
		this.frequency = freq;
		this.phaseIncrement = 2 * Math.PI * frequency / samplesPerSecond;		
	}
	
	public double nextSample() {
		phase += phaseIncrement;
		if (frequency > 0 && phase >= 2 * Math.PI) {
			phase = phase - 2*Math.PI;
		}
		if (frequency < 0 && phase <= 0) {
			phase = phase + 2*Math.PI;
		}
		final int idx = (int)((phase * TABLE_SIZE/(2 * Math.PI)) % TABLE_SIZE);
		return sinTable[idx];
	}
	
}

