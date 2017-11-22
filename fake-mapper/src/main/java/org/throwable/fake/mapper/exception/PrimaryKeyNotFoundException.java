package org.throwable.fake.mapper.exception;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/8/17 20:20
 */
public class PrimaryKeyNotFoundException extends RuntimeException {

    public PrimaryKeyNotFoundException(String message) {
        super(message);
    }

    public PrimaryKeyNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public PrimaryKeyNotFoundException(Throwable cause) {
        super(cause);
    }
}
