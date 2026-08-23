package com.mariuszilinskas.streamix.observability.grpc;

import com.mariuszilinskas.streamix.observability.util.ObservabilityUtils;
import io.grpc.ForwardingServerCall;
import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import org.jspecify.annotations.Nullable;
import org.slf4j.MDC;

import java.util.Map;

public final class StreamixGrpcServerInterceptor implements ServerInterceptor {

    static final Metadata.Key<String> CORRELATION_ID_KEY =
            Metadata.Key.of(ObservabilityUtils.CORRELATION_HEADER, Metadata.ASCII_STRING_MARSHALLER);

    static final Metadata.Key<String> USER_ID_KEY =
            Metadata.Key.of(ObservabilityUtils.USER_ID_HEADER, Metadata.ASCII_STRING_MARSHALLER);

    private final String serviceName;
    private final String environment;

    public StreamixGrpcServerInterceptor(String serviceName, String environment) {
        this.serviceName = serviceName;
        this.environment = environment;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata inboundHeaders,
            ServerCallHandler<ReqT, RespT> next
    ) {
        String correlationId = ObservabilityUtils.resolveCorrelationId(inboundHeaders.get(CORRELATION_ID_KEY));
        String userId = inboundHeaders.get(USER_ID_KEY);

        Map<String, String> previousContext = MDC.getCopyOfContextMap();
        ObservabilityUtils.put(ObservabilityUtils.CORRELATION_ID, correlationId);
        ObservabilityUtils.put(ObservabilityUtils.USER_ID, userId);
        ObservabilityUtils.put(ObservabilityUtils.SERVICE, serviceName);
        ObservabilityUtils.put(ObservabilityUtils.ENVIRONMENT, environment);

        ServerCall<ReqT, RespT> wrappedCall = new ForwardingServerCall.SimpleForwardingServerCall<>(call) {
            @Override
            public void sendHeaders(Metadata responseHeaders) {
                responseHeaders.put(CORRELATION_ID_KEY, correlationId);
                super.sendHeaders(responseHeaders);
            }
        };

        ServerCall.Listener<ReqT> listener = next.startCall(wrappedCall, inboundHeaders);

        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(listener) {
            @Override
            public void onComplete() {
                try {
                    super.onComplete();
                } finally {
                    restoreContext(previousContext);
                }
            }

            @Override
            public void onCancel() {
                try {
                    super.onCancel();
                } finally {
                    restoreContext(previousContext);
                }
            }
        };
    }

    private static void restoreContext(@Nullable Map<String, String> previousContext) {
        MDC.clear();
        if (previousContext != null) {
            MDC.setContextMap(previousContext);
        }
    }
}
