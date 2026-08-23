package com.mariuszilinskas.streamix.observability.rabbit;

import com.mariuszilinskas.streamix.observability.util.ObservabilityUtils;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.MDC;
import org.springframework.amqp.core.Message;

import java.util.Map;

public final class StreamixRabbitConsumerInterceptor implements MethodInterceptor {

    private final String serviceName;
    private final String environment;

    public StreamixRabbitConsumerInterceptor(String serviceName, String environment) {
        this.serviceName = serviceName;
        this.environment = environment;
    }

    @Override
    public Object invoke(@NonNull MethodInvocation invocation) throws Throwable {
        Message message = findMessage(invocation.getArguments());

        String correlationId;
        String userId = null;

        if (message != null) {
            Map<String, Object> headers = message.getMessageProperties().getHeaders();
            correlationId = ObservabilityUtils.resolveCorrelationId(
                    headerAsString(headers.get(ObservabilityUtils.CORRELATION_HEADER))
            );
            userId = headerAsString(headers.get(ObservabilityUtils.USER_ID_HEADER));
        } else {
            correlationId = ObservabilityUtils.resolveCorrelationId(null);
        }

        Map<String, String> previousContext = MDC.getCopyOfContextMap();

        try {
            ObservabilityUtils.put(ObservabilityUtils.CORRELATION_ID, correlationId);
            ObservabilityUtils.put(ObservabilityUtils.USER_ID, userId);
            ObservabilityUtils.put(ObservabilityUtils.SERVICE, serviceName);
            ObservabilityUtils.put(ObservabilityUtils.ENVIRONMENT, environment);
            return invocation.proceed();
        } finally {
            MDC.clear();
            if (previousContext != null) {
                MDC.setContextMap(previousContext);
            }
        }
    }

    @Nullable
    private static Message findMessage(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof Message msg) {
                return msg;
            }
        }
        return null;
    }

    @Nullable
    private static String headerAsString(@Nullable Object value) {
        return value != null ? value.toString() : null;
    }
}
