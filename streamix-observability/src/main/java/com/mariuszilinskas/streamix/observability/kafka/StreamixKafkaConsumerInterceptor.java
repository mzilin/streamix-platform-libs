package com.mariuszilinskas.streamix.observability.kafka;

import com.mariuszilinskas.streamix.observability.context.LogContext;
import com.mariuszilinskas.streamix.observability.context.LogContextManager;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.MDC;
import org.springframework.kafka.listener.RecordInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public final class StreamixKafkaConsumerInterceptor implements RecordInterceptor<Object, Object> {

    private final ThreadLocal<Map<String, String>> previousContextHolder = new ThreadLocal<>();

    private final String serviceName;
    private final String environment;

    public StreamixKafkaConsumerInterceptor(String serviceName, String environment) {
        this.serviceName = serviceName;
        this.environment = environment;
    }

    @Override
    @NonNull
    public ConsumerRecord<Object, Object> intercept(
            @NonNull ConsumerRecord<Object, Object> record,
            @NonNull Consumer<Object, Object> consumer
    ) {
        previousContextHolder.set(MDC.getCopyOfContextMap());

        String correlationId = LogContextManager.resolveCorrelationId(
                headerValue(record, LogContext.CORRELATION_HEADER)
        );
        String userId = headerValue(record, LogContext.USER_ID_HEADER);

        LogContextManager.put(LogContext.CORRELATION_ID, correlationId);
        LogContextManager.put(LogContext.USER_ID, userId);
        LogContextManager.put(LogContext.SERVICE, serviceName);
        LogContextManager.put(LogContext.ENVIRONMENT, environment);

        return record;
    }

    @Override
    public void success(
            @NonNull ConsumerRecord<Object, Object> record,
            @NonNull Consumer<Object, Object> consumer
    ) {
        restoreContext();
    }

    @Override
    public void failure(
            @NonNull ConsumerRecord<Object, Object> record,
            @NonNull Exception exception,
            @NonNull Consumer<Object, Object> consumer
    ) {
        restoreContext();
    }

    private void restoreContext() {
        Map<String, String> previous = previousContextHolder.get();
        previousContextHolder.remove();
        MDC.clear();
        if (previous != null) {
            MDC.setContextMap(previous);
        }
    }

    @Nullable
    private static String headerValue(ConsumerRecord<?, ?> record, String headerName) {
        Header header = record.headers().lastHeader(headerName);
        if (header == null || header.value() == null) {
            return null;
        }
        return new String(header.value(), StandardCharsets.UTF_8);
    }
}
