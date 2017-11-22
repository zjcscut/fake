package org.throwable.fake.mapper.exception;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/8/18 1:19
 */
public class ColumnNotFoundException extends RuntimeException {

	public ColumnNotFoundException(String message) {
		super(message);
	}

	public ColumnNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public ColumnNotFoundException(Throwable cause) {
		super(cause);
	}
}
