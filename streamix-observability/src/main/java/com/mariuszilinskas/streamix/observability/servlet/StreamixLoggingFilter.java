package com.mariuszilinskas.streamix.observability.servlet;

import com.mariuszilinskas.streamix.observability.logging.LogContext;
import com.mariuszilinskas.streamix.observability.logging.LogContextManager;
import com.mariuszilinskas.streamix.observability.util.ObservabilityUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

public final class StreamixLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String incoming = request.getHeader(LogContext.CORRELATION_HEADER);
        String correlationId = ObservabilityUtils.isValidCorrelationId(incoming)
                ? incoming
                : UUID.randomUUID().toString();

        try {
            LogContextManager.put(LogContext.CORRELATION_ID, correlationId);
            response.setHeader(LogContext.CORRELATION_HEADER, correlationId);
            filterChain.doFilter(request, response);
        } finally {
            LogContextManager.clear();
        }
    }
}