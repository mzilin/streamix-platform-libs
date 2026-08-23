package com.mariuszilinskas.streamix.observability.rabbit;

import com.mariuszilinskas.streamix.observability.context.LogContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import static org.assertj.core.api.Assertions.assertThat;

class StreamixRabbitProducerPostProcessorTest {

    private final StreamixRabbitProducerPostProcessor postProcessor = new StreamixRabbitProducerPostProcessor();

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void writesMdcValuesToMessageHeaders() {
        MDC.put(LogContext.CORRELATION_ID, "a1b2c3d4-e5f6-7890-abcd-ef1234567890");
        MDC.put(LogContext.USER_ID, "user-3");

        Message message = new Message(new byte[0], new MessageProperties());
        Message processed = postProcessor.postProcessMessage(message);

        String correlationHeader = processed.getMessageProperties().getHeader(LogContext.CORRELATION_HEADER);
        String userIdHeader = processed.getMessageProperties().getHeader(LogContext.USER_ID_HEADER);

        assertThat(correlationHeader).isEqualTo("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
        assertThat(userIdHeader).isEqualTo("user-3");
    }

    @Test
    void doesNotAddHeadersWhenMdcIsEmpty() {
        Message message = new Message(new byte[0], new MessageProperties());
        Message processed = postProcessor.postProcessMessage(message);

        String correlationHeader = processed.getMessageProperties().getHeader(LogContext.CORRELATION_HEADER);
        String userIdHeader = processed.getMessageProperties().getHeader(LogContext.USER_ID_HEADER);

        assertThat(correlationHeader).isNull();
        assertThat(userIdHeader).isNull();
    }

    @Test
    void returnsOriginalMessageInstance() {
        MDC.put(LogContext.CORRELATION_ID, "a1b2c3d4-e5f6-7890-abcd-ef1234567890");
        Message message = new Message(new byte[0], new MessageProperties());

        assertThat(postProcessor.postProcessMessage(message)).isSameAs(message);
    }
}
