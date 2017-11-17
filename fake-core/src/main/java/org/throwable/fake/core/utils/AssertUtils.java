package org.throwable.fake.core.utils;

import java.util.function.Supplier;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/11/17 18:13
 */
public abstract class AssertUtils {

    public static void assertThrowRuntimeException(boolean expression,
                                                   Supplier<? extends RuntimeException> runtimeExceptionSupplier) {
        if (!expression && null != runtimeExceptionSupplier) {
            throw runtimeExceptionSupplier.get();
        }
    }
}
