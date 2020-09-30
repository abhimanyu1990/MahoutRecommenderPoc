package com.poc.recommender.customexceptions;

public class GenericForbiddenException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public GenericForbiddenException(String msg) {
		super(msg);
	}
}
