package com.mariuszilinskas.streamix.observability.grpc;

import com.mariuszilinskas.streamix.observability.util.ObservabilityUtils;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StreamixGrpcServerInterceptorTest {

    private final StreamixGrpcServerInterceptor interceptor = new StreamixGrpcServerInterceptor("test-service", "test");

    @Mock
    private ServerCall<Object, Object> serverCall;

    @Mock
    private ServerCallHandler<Object, Object> next;

    @Mock
    private ServerCall.Listener<Object> listener;

    @BeforeEach
    void setUp() {
        when(next.startCall(any(), any())).thenReturn(listener);
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void extractsCorrelationIdFromMetadata() {
        Metadata headers = new Metadata();
        String correlationId = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";
        headers.put(StreamixGrpcServerInterceptor.CORRELATION_ID_KEY, correlationId);

        interceptor.interceptCall(serverCall, headers, next);

        assertThat(MDC.get(ObservabilityUtils.CORRELATION_ID)).isEqualTo(correlationId);
    }

    @Test
    void generatesCorrelationIdWhenMissing() {
        Metadata headers = new Metadata();

        interceptor.interceptCall(serverCall, headers, next);

        String correlationId = MDC.get(ObservabilityUtils.CORRELATION_ID);
        assertThat(correlationId).isNotNull();
        assertThat(ObservabilityUtils.isValidCorrelationId(correlationId)).isTrue();
    }

    @Test
    void extractsUserIdFromMetadata() {
        Metadata headers = new Metadata();
        headers.put(StreamixGrpcServerInterceptor.USER_ID_KEY, "user-99");

        interceptor.interceptCall(serverCall, headers, next);

        assertThat(MDC.get(ObservabilityUtils.USER_ID)).isEqualTo("user-99");
    }

    @Test
    void echoesCorrelationIdInResponseHeaders() {
        String correlationId = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";
        Metadata inbound = new Metadata();
        inbound.put(StreamixGrpcServerInterceptor.CORRELATION_ID_KEY, correlationId);

        interceptor.interceptCall(serverCall, inbound, next);

        @SuppressWarnings("unchecked")
        org.mockito.ArgumentCaptor<ServerCall<Object, Object>> callCaptor =
                org.mockito.ArgumentCaptor.forClass(ServerCall.class);
        verify(next).startCall(callCaptor.capture(), any());

        Metadata outbound = new Metadata();
        callCaptor.getValue().sendHeaders(outbound);

        assertThat(outbound.get(StreamixGrpcServerInterceptor.CORRELATION_ID_KEY)).isEqualTo(correlationId);
    }

    @Test
    void restoresMdcOnComplete() {
        MDC.put("pre-existing", "value");
        Metadata headers = new Metadata();

        ServerCall.Listener<Object> wrappedListener = interceptor.interceptCall(serverCall, headers, next);
        wrappedListener.onComplete();

        assertThat(MDC.get(ObservabilityUtils.CORRELATION_ID)).isNull();
        assertThat(MDC.get("pre-existing")).isEqualTo("value");
    }

    @Test
    void restoresMdcOnCancel() {
        MDC.put("pre-existing", "value");
        Metadata headers = new Metadata();

        ServerCall.Listener<Object> wrappedListener = interceptor.interceptCall(serverCall, headers, next);
        wrappedListener.onCancel();

        assertThat(MDC.get(ObservabilityUtils.CORRELATION_ID)).isNull();
        assertThat(MDC.get("pre-existing")).isEqualTo("value");
    }
}
