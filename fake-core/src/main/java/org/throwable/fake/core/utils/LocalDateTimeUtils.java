package org.throwable.fake.core.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/11/17 0:12
 */
public abstract class LocalDateTimeUtils {

    private static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private static final Map<String, DateTimeFormatter> FORMATTER_MAP = new HashMap<>(1);

    //register default pattern
    static {
        FORMATTER_MAP.put(DATETIME_PATTERN, DateTimeFormatter.ofPattern(DATETIME_PATTERN));
    }

    public static LocalDateTime parse(String localDateTimeString) {
        return parse(localDateTimeString, DATETIME_PATTERN);
    }

    public static LocalDateTime parse(String localDateTimeString, String pattern) {
        return LocalDateTime.parse(localDateTimeString, getOrCreateFormatterByPattern(pattern));
    }

    public static String format(LocalDateTime localDateTime) {
        return getOrCreateFormatterByPattern(DATETIME_PATTERN).format(localDateTime);
    }

    public static String format(LocalDateTime localDateTime, String pattern) {
        return getOrCreateFormatterByPattern(pattern).format(localDateTime);
    }

    private static DateTimeFormatter getOrCreateFormatterByPattern(String pattern) {
        DateTimeFormatter formatter;
        if (!FORMATTER_MAP.containsKey(pattern)) {
            formatter = DateTimeFormatter.ofPattern(pattern);
            FORMATTER_MAP.put(pattern, formatter);
        } else {
            formatter = FORMATTER_MAP.get(pattern);
        }
        return formatter;
    }
}
