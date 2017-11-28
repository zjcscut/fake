package org.throwable.fake.amqp.exception;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/11/28 22:05
 */
public class FakeAmqpException extends RuntimeException {

	public FakeAmqpException(String message) {
		super(message);
	}

	public FakeAmqpException(String message, Throwable cause) {
		super(message, cause);
	}

	public FakeAmqpException(Throwable cause) {
		super(cause);
	}
}
