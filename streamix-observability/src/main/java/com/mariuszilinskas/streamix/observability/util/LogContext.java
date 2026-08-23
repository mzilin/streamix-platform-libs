package com.mariuszilinskas.streamix.observability.util;

public final class LogContext {

    public static final String CORRELATION_ID = "correlation_id";
    public static final String USER_ID = "user_id";
    public static final String SERVICE = "service";
    public static final String ENVIRONMENT = "environment";

    public static final String CORRELATION_HEADER = "X-Correlation-Id";
    public static final String USER_ID_HEADER = "X-User-Id";

    private LogContext() {
    }
}
