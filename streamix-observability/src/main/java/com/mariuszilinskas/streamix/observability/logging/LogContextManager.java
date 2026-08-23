package com.mariuszilinskas.streamix.observability.logging;

import org.slf4j.MDC;

public final class LogContextManager {

    private LogContextManager() {
    }

    public static void put(String key, String value) {
        if (value != null && !value.isBlank()) {
            MDC.put(key, value);
        }
    }

    public static void remove(String key) {
        MDC.remove(key);
    }

    public static void clear() {
        MDC.clear();
    }
}