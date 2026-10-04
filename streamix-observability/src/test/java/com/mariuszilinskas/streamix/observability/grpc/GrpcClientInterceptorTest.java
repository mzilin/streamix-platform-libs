package com.mariuszilinskas.streamix.observability.grpc;

import com.mariuszilinskas.streamix.observability.context.LogContext;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static com.mariuszilinskas.streamix.observability.grpc.GrpcMetadataKeys.*;

@ExtendWith(MockitoExtension.class)
class GrpcClientInterceptorTest {

    private final GrpcClientInterceptor interceptor = new GrpcClientInterceptor();

    @Mock
    private Channel channel;

    @Mock
    @SuppressWarnings("rawtypes")
    private ClientCall clientCall;

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    @SuppressWarnings("unchecked")
    void writesMdcCorrelationIdToOutgoingMetadata() {
        MDC.put(LogContext.CORRELATION_ID, "a1b2c3d4-e5f6-7890-abcd-ef1234567890");
        MDC.put(LogContext.USER_ID, "user-7");
        when(channel.newCall(any(), any())).thenReturn(clientCall);

        MethodDescriptor<Object, Object> method = mock(MethodDescriptor.class);
        ClientCall<Object, Object> call = interceptor.interceptCall(method, CallOptions.DEFAULT, channel);

        ClientCall.Listener<Object> responseListener = mock(ClientCall.Listener.class);
        Metadata sentHeaders = new Metadata();
        call.start(responseListener, sentHeaders);

        assertThat(sentHeaders.get(CORRELATION_ID_KEY))
                .isEqualTo("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
        assertThat(sentHeaders.get(USER_ID_KEY)).isEqualTo("user-7");
    }

    @Test
    @SuppressWarnings("unchecked")
    void doesNotAddHeadersWhenMdcIsEmpty() {
        when(channel.newCall(any(), any())).thenReturn(clientCall);

        MethodDescriptor<Object, Object> method = mock(MethodDescriptor.class);
        ClientCall<Object, Object> call = interceptor.interceptCall(method, CallOptions.DEFAULT, channel);

        ClientCall.Listener<Object> responseListener = mock(ClientCall.Listener.class);
        Metadata sentHeaders = new Metadata();
        call.start(responseListener, sentHeaders);

        assertThat(sentHeaders.get(CORRELATION_ID_KEY)).isNull();
        assertThat(sentHeaders.get(USER_ID_KEY)).isNull();
    }
}
