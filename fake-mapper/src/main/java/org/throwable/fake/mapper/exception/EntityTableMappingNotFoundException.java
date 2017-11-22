package org.throwable.fake.mapper.exception;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/8/18 11:33
 */
public class EntityTableMappingNotFoundException extends RuntimeException{

    public EntityTableMappingNotFoundException(String message) {
        super(message);
    }

    public EntityTableMappingNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public EntityTableMappingNotFoundException(Throwable cause) {
        super(cause);
    }
}
