package com.g0kla.rtlsdr4java;

public class Complex {
	double i;
	double q;

	Complex(double i, double q) {
		this.i = i;
		this.q = q;
	}

	public double geti() {
		return i;
	}

	public double getq() {
		return q;
	}

	public double magnitude() {
		return Math.sqrt(i * i + q * q);
	}

	public void multiply(double value) {
		i *= value;
		q *= value;
	}

	public void normalize() {
		final double magnitude = magnitude();

		if (magnitude != 0) {
			multiply(1.0f / magnitude());
		}
	}
}
