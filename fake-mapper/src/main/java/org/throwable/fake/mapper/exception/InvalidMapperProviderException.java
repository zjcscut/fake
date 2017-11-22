package org.throwable.fake.mapper.exception;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/8/20 12:06
 */
public class InvalidMapperProviderException extends RuntimeException {

	public InvalidMapperProviderException(String message) {
		super(message);
	}

	public InvalidMapperProviderException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidMapperProviderException(Throwable cause) {
		super(cause);
	}
}
