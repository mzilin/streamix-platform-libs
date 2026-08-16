package com.mariuszilinskas.streamix.observability.util;

import org.jspecify.annotations.Nullable;

import java.util.UUID;

public final class ObservabilityUtils {

    private ObservabilityUtils() {
        // Private constructor to prevent instantiation
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
