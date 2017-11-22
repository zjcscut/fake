package org.throwable.fake.mapper.exception;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/8/18 15:10
 */
public class CollectionDeepCopyException extends RuntimeException {

    public CollectionDeepCopyException(String message) {
        super(message);
    }

    public CollectionDeepCopyException(String message, Throwable cause) {
        super(message, cause);
    }

    public CollectionDeepCopyException(Throwable cause) {
        super(cause);
    }
}
