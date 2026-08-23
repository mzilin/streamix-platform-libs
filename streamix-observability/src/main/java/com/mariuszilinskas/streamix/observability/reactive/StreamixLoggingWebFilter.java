package com.mariuszilinskas.streamix.observability.reactive;

import com.mariuszilinskas.streamix.observability.logging.LogContext;
import com.mariuszilinskas.streamix.observability.logging.MdcThreadLocalAccessor;
import com.mariuszilinskas.streamix.observability.util.ObservabilityUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

public final class StreamixLoggingWebFilter implements WebFilter {

    @Override
    @NonNull
    public Mono<Void> filter(
        @NonNull ServerWebExchange exchange,
        @NonNull WebFilterChain chain
    ) {
        String incoming = exchange
                .getRequest()
                .getHeaders()
                .getFirst(LogContext.CORRELATION_HEADER);

        String correlationId = ObservabilityUtils.isValidCorrelationId(incoming)
                ? incoming
                : UUID.randomUUID().toString();

        exchange.getResponse()
                .getHeaders()
                .set(LogContext.CORRELATION_HEADER, correlationId);

        return chain.filter(exchange)
                .contextWrite(context ->
                        context.put(
                                MdcThreadLocalAccessor.KEY,
                                Map.of(LogContext.CORRELATION_ID, correlationId)
                        )
                );
    }
}