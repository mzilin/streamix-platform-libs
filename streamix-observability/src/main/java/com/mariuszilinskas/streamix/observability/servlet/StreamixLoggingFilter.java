package com.mariuszilinskas.streamix.observability.servlet;

import com.mariuszilinskas.streamix.observability.logging.LogContext;
import com.mariuszilinskas.streamix.observability.logging.LogContextManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

public class StreamixLoggingFilter extends OncePerRequestFilter {

    private static final String CORRELATION_HEADER = "X-Correlation-Id";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String correlationId = request.getHeader(CORRELATION_HEADER);

        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        try {
            LogContextManager.put(
                    LogContext.CORRELATION_ID,
                    correlationId
            );

            response.setHeader(CORRELATION_HEADER, correlationId);

            filterChain.doFilter(request, response);

        } finally {
            LogContextManager.clear();
        }
    }
}