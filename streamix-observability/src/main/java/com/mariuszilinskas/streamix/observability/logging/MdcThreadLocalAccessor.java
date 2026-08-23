package com.mariuszilinskas.streamix.observability.logging;

import io.micrometer.context.ThreadLocalAccessor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.MDC;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MdcThreadLocalAccessor implements ThreadLocalAccessor<Map<String, String>> {

    public static final String KEY = "streamix.mdc";

    private static final List<String> TRACKED_KEYS = List.of(
        LogContext.CORRELATION_ID,
        LogContext.REQUEST_ID,
        LogContext.USER_ID,
        LogContext.SERVICE,
        LogContext.ENVIRONMENT
    );

    @Override
    @NonNull
    public Object key() {
        return KEY;
    }

    @Override
    @Nullable
    public Map<String, String> getValue() {
        Map<String, String> snapshot = new HashMap<>();

        for (String key : TRACKED_KEYS) {
            String value = MDC.get(key);

            if (value != null) {
                snapshot.put(key, value);
            }
        }

        return snapshot.isEmpty() ? null : Collections.unmodifiableMap(snapshot);
    }

    @Override
    public void setValue(@Nullable Map<String, String> values) {
        clearTrackedKeys();

        if (values != null) {
            values.forEach(MDC::put);
        }
    }

    @Override
    public void setValue() {
        clearTrackedKeys();
    }

    private static void clearTrackedKeys() {
        TRACKED_KEYS.forEach(MDC::remove);
    }
}