package com.mariuszilinskas.streamix.observability.grpc;

import com.mariuszilinskas.streamix.observability.context.LogContext;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.MethodDescriptor;
import org.slf4j.MDC;

import static com.mariuszilinskas.streamix.observability.grpc.GrpcMetadataKeys.*;

public final class GrpcClientInterceptor implements ClientInterceptor {

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions,
            Channel next
    ) {
        String correlationId = MDC.get(LogContext.CORRELATION_ID);
        String userId = MDC.get(LogContext.USER_ID);

        return new ForwardingClientCall.SimpleForwardingClientCall<>(next.newCall(method, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, io.grpc.Metadata headers) {
                if (correlationId != null) {
                    headers.put(CORRELATION_ID_KEY, correlationId);
                }
                if (userId != null) {
                    headers.put(USER_ID_KEY, userId);
                }
                super.start(responseListener, headers);
            }
        };
    }
}
