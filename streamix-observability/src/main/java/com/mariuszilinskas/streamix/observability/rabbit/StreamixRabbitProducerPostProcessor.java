package com.mariuszilinskas.streamix.observability.rabbit;

import com.mariuszilinskas.streamix.observability.util.ObservabilityUtils;
import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;

public final class StreamixRabbitProducerPostProcessor implements MessagePostProcessor {

    @Override
    @NonNull
    public Message postProcessMessage(@NonNull Message message) throws AmqpException {
        String correlationId = MDC.get(ObservabilityUtils.CORRELATION_ID);
        String userId = MDC.get(ObservabilityUtils.USER_ID);

        if (correlationId != null) {
            message.getMessageProperties().setHeader(ObservabilityUtils.CORRELATION_HEADER, correlationId);
        }
        if (userId != null) {
            message.getMessageProperties().setHeader(ObservabilityUtils.USER_ID_HEADER, userId);
        }

        return message;
    }
}
