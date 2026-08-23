package com.mariuszilinskas.streamix.observability.rabbit;

import com.mariuszilinskas.streamix.observability.context.LogContext;
import com.mariuszilinskas.streamix.observability.context.LogContextManager;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StreamixRabbitConsumerInterceptorTest {

    private final StreamixRabbitConsumerInterceptor interceptor = new StreamixRabbitConsumerInterceptor("test-service", "test");

    @Mock
    private MethodInvocation invocation;

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void extractsCorrelationIdAndUserIdFromMessageHeaders() throws Throwable {
        String correlationId = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";
        MessageProperties props = new MessageProperties();
        props.setHeader(LogContext.CORRELATION_HEADER, correlationId);
        props.setHeader(LogContext.USER_ID_HEADER, "user-5");
        Message message = new Message(new byte[0], props);

        AtomicReference<String> capturedCorrelation = new AtomicReference<>();
        AtomicReference<String> capturedUser = new AtomicReference<>();
        when(invocation.getArguments()).thenReturn(new Object[]{message});
        when(invocation.proceed()).thenAnswer(inv -> {
            capturedCorrelation.set(MDC.get(LogContext.CORRELATION_ID));
            capturedUser.set(MDC.get(LogContext.USER_ID));
            return null;
        });

        interceptor.invoke(invocation);

        assertThat(capturedCorrelation.get()).isEqualTo(correlationId);
        assertThat(capturedUser.get()).isEqualTo("user-5");
    }

    @Test
    void generatesCorrelationIdWhenHeaderMissing() throws Throwable {
        Message message = new Message(new byte[0], new MessageProperties());
        AtomicReference<String> capturedCorrelation = new AtomicReference<>();

        when(invocation.getArguments()).thenReturn(new Object[]{message});
        when(invocation.proceed()).thenAnswer(inv -> {
            capturedCorrelation.set(MDC.get(LogContext.CORRELATION_ID));
            return null;
        });

        interceptor.invoke(invocation);

        assertThat(capturedCorrelation.get()).isNotNull();
        assertThat(LogContextManager.isValidCorrelationId(capturedCorrelation.get())).isTrue();
    }

    @Test
    void clearsMdcAfterInvocation() throws Throwable {
        MDC.put("pre-existing", "value");
        Message message = new Message(new byte[0], new MessageProperties());
        when(invocation.getArguments()).thenReturn(new Object[]{message});
        when(invocation.proceed()).thenReturn(null);

        interceptor.invoke(invocation);

        assertThat(MDC.get(LogContext.CORRELATION_ID)).isNull();
        assertThat(MDC.get(LogContext.USER_ID)).isNull();
        assertThat(MDC.get("pre-existing")).isEqualTo("value");
    }

    @Test
    void restoresMdcEvenOnException() throws Throwable {
        MDC.put("pre-existing", "value");
        Message message = new Message(new byte[0], new MessageProperties());
        when(invocation.getArguments()).thenReturn(new Object[]{message});
        when(invocation.proceed()).thenThrow(new RuntimeException("test error"));

        try {
            interceptor.invoke(invocation);
        } catch (RuntimeException ignored) {
        }

        assertThat(MDC.get(LogContext.CORRELATION_ID)).isNull();
        assertThat(MDC.get("pre-existing")).isEqualTo("value");
    }
}
