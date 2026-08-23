package com.mariuszilinskas.streamix.observability.grpc;

import com.mariuszilinskas.streamix.observability.util.ObservabilityUtils;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.MethodDescriptor;
import org.slf4j.MDC;

public final class StreamixGrpcClientInterceptor implements ClientInterceptor {

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions,
            Channel next
    ) {
        String correlationId = MDC.get(ObservabilityUtils.CORRELATION_ID);
        String userId = MDC.get(ObservabilityUtils.USER_ID);

        return new ForwardingClientCall.SimpleForwardingClientCall<>(next.newCall(method, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, io.grpc.Metadata headers) {
                if (correlationId != null) {
                    headers.put(StreamixGrpcServerInterceptor.CORRELATION_ID_KEY, correlationId);
                }
                if (userId != null) {
                    headers.put(StreamixGrpcServerInterceptor.USER_ID_KEY, userId);
                }
                super.start(responseListener, headers);
            }
        };
    }
}
