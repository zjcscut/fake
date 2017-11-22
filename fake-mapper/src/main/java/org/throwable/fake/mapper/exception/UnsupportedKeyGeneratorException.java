package org.throwable.fake.mapper.exception;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/8/19 11:47
 */
public class UnsupportedKeyGeneratorException extends RuntimeException{

	public UnsupportedKeyGeneratorException(String message) {
		super(message);
	}

	public UnsupportedKeyGeneratorException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnsupportedKeyGeneratorException(Throwable cause) {
		super(cause);
	}
}
