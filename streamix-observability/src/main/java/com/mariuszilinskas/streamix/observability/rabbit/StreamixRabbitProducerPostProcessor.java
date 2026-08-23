package com.mariuszilinskas.streamix.observability.rabbit;

import com.mariuszilinskas.streamix.observability.context.LogContext;
import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;

public final class StreamixRabbitProducerPostProcessor implements MessagePostProcessor {

    @Override
    @NonNull
    public Message postProcessMessage(@NonNull Message message) throws AmqpException {
        String correlationId = MDC.get(LogContext.CORRELATION_ID);
        String userId = MDC.get(LogContext.USER_ID);

        if (correlationId != null) {
            message.getMessageProperties().setHeader(LogContext.CORRELATION_HEADER, correlationId);
        }
        if (userId != null) {
            message.getMessageProperties().setHeader(LogContext.USER_ID_HEADER, userId);
        }

        return message;
    }
}
