package com.mariuszilinskas.streamix.observability.reactive;

import com.mariuszilinskas.streamix.observability.logging.LogContext;
import com.mariuszilinskas.streamix.observability.logging.LogContextManager;
import org.jspecify.annotations.NonNull;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class StreamixLoggingWebFilter implements WebFilter {

    private static final String CORRELATION_HEADER = "X-Correlation-Id";

    @Override
    @NonNull
    public Mono<Void> filter(
            @NonNull ServerWebExchange exchange,
            @NonNull WebFilterChain chain
    ) {
        String correlationId =
                exchange.getRequest()
                        .getHeaders()
                        .getFirst(CORRELATION_HEADER);

        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        final String finalCorrelationId = correlationId;

        exchange.getResponse()
                .getHeaders()
                .set(CORRELATION_HEADER, finalCorrelationId);

        return Mono.defer(() -> {
            LogContextManager.put(
                    LogContext.CORRELATION_ID,
                    finalCorrelationId
            );

            return chain.filter(exchange)
                    .doFinally(signal -> LogContextManager.clear());
        });
    }
}