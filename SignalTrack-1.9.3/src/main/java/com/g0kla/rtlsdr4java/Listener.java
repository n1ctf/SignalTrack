package com.g0kla.rtlsdr4java;

public interface Listener<T> {
	void receive(T t);
}