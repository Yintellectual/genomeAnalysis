package com.spdeveloper.chgc.genome.prediction.service;

public class MissDependencyException extends RuntimeException {

	public MissDependencyException(String message, Throwable throwable) {
		super(message, throwable);
	}
	public MissDependencyException(String message) {
		super(message);
	}
}
