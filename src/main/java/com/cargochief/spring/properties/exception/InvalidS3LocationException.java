package com.cargochief.spring.properties.exception;

public class InvalidS3LocationException extends S3ResourceException {
	private static final long serialVersionUID = 2057112790586228669L;

	public InvalidS3LocationException(String message) {
		super(message);
	}

}
