package com.mariuszilinskas.streamix.observability.rabbit;

import com.mariuszilinskas.streamix.observability.context.LogContext;
import com.mariuszilinskas.streamix.observability.context.LogContextManager;
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
            correlationId = LogContextManager.resolveCorrelationId(
                    headerAsString(headers.get(LogContext.CORRELATION_HEADER))
            );
            userId = headerAsString(headers.get(LogContext.USER_ID_HEADER));
        } else {
            correlationId = LogContextManager.resolveCorrelationId(null);
        }

        Map<String, String> previousContext = MDC.getCopyOfContextMap();

        try {
            LogContextManager.put(LogContext.CORRELATION_ID, correlationId);
            LogContextManager.put(LogContext.USER_ID, userId);
            LogContextManager.put(LogContext.SERVICE, serviceName);
            LogContextManager.put(LogContext.ENVIRONMENT, environment);
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
