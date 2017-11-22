package org.throwable.fake.mapper.exception;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/8/18 0:02
 */
public class UnsupportedElementException extends RuntimeException {

	public UnsupportedElementException(String message) {
		super(message);
	}

	public UnsupportedElementException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnsupportedElementException(Throwable cause) {
		super(cause);
	}
}
