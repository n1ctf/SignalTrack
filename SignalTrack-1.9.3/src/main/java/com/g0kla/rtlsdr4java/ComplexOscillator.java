package com.g0kla.rtlsdr4java;

public class ComplexOscillator {

	private final Oscillator cosOsc;
	private final Oscillator sinOsc;
	
	public ComplexOscillator(int samples, int freq) {
		cosOsc = new CosOscillator(samples, freq);
		sinOsc = new SinOscillator(samples, freq);
	}
	
	public void setFrequency(double freq) {
		cosOsc.setFrequency(freq);
		sinOsc.setFrequency(freq);
	}
	
	public Complex nextSample() {
		final double i = cosOsc.nextSample();
		final double q = sinOsc.nextSample();
		return new Complex(i, q);
	}
}
