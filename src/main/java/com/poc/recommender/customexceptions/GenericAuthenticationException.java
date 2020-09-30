package com.poc.recommender.customexceptions;

public class GenericAuthenticationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public GenericAuthenticationException(String msg) {
		super(msg);
	}
}