package org.throwable.fake.mapper.exception;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/8/19 11:48
 */
public class UnsupportedKeyTypeException extends RuntimeException {

	public UnsupportedKeyTypeException(String message) {
		super(message);
	}

	public UnsupportedKeyTypeException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnsupportedKeyTypeException(Throwable cause) {
		super(cause);
	}
}
