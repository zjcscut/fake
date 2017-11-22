package org.throwable.fake.mapper.exception;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/8/17 0:54
 */
public class MetadataParseException extends RuntimeException {

	public MetadataParseException(String message) {
		super(message);
	}

	public MetadataParseException(String message, Throwable cause) {
		super(message, cause);
	}

	public MetadataParseException(Throwable cause) {
		super(cause);
	}
}
