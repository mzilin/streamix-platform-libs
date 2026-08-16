package com.mariuszilinskas.streamix.observability.logging;

public class LogContext {

    public static final String CORRELATION_ID = "correlation_id";
    public static final String REQUEST_ID = "request_id";
    public static final String SERVICE = "service";
    public static final String ENVIRONMENT = "environment";

    private LogContext() {
    }
}
