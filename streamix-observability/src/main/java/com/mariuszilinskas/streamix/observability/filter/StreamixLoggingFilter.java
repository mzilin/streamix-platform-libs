package com.mariuszilinskas.streamix.observability.filter;

import com.mariuszilinskas.streamix.observability.util.ObservabilityUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

public final class StreamixLoggingFilter extends OncePerRequestFilter {

    private final String serviceName;
    private final String environment;

    public StreamixLoggingFilter(String serviceName, String environment) {
        this.serviceName = serviceName;
        this.environment = environment;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String correlationId = ObservabilityUtils.resolveCorrelationId(request.getHeader(ObservabilityUtils.CORRELATION_HEADER));
        String userId = request.getHeader(ObservabilityUtils.USER_ID_HEADER);

        Map<String, String> previousContext = MDC.getCopyOfContextMap();

        try {
            ObservabilityUtils.put(ObservabilityUtils.CORRELATION_ID, correlationId);
            ObservabilityUtils.put(ObservabilityUtils.USER_ID, userId);
            ObservabilityUtils.put(ObservabilityUtils.SERVICE, serviceName);
            ObservabilityUtils.put(ObservabilityUtils.ENVIRONMENT, environment);
            response.setHeader(ObservabilityUtils.CORRELATION_HEADER, correlationId);
            filterChain.doFilter(request, response);
        } finally {
            restoreContext(previousContext);
        }
    }

    private static void restoreContext(@Nullable Map<String, String> previousContext) {
        MDC.clear();
        if (previousContext != null) {
            MDC.setContextMap(previousContext);
        }
    }
}
