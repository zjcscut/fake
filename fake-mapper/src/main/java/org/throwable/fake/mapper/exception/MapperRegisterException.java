package org.throwable.fake.mapper.exception;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/8/16 23:36
 */
public class MapperRegisterException extends RuntimeException {

	public MapperRegisterException(String message) {
		super(message);
	}

	public MapperRegisterException(String message, Throwable cause) {
		super(message, cause);
	}

	public MapperRegisterException(Throwable cause) {
		super(cause);
	}
}
