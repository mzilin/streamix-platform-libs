package com.mariuszilinskas.streamix.observability.util;

import org.jspecify.annotations.Nullable;
import org.slf4j.MDC;

import java.util.UUID;

public final class LogContextManager {

    private LogContextManager() {
    }

    public static void put(String key, String value) {
        if (key != null && !key.isBlank() && value != null && !value.isBlank()) {
            MDC.put(key, value);
        }
    }

    public static void remove(String key) {
        if (key != null && !key.isBlank()) {
            MDC.remove(key);
        }
    }

    public static String resolveCorrelationId(@Nullable String value) {
        return isValidCorrelationId(value) ? value : UUID.randomUUID().toString();
    }

    public static boolean isValidCorrelationId(@Nullable String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
