package com.mariuszilinskas.streamix.observability.kafka;

import com.mariuszilinskas.streamix.observability.util.ObservabilityUtils;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class StreamixKafkaProducerInterceptorTest {

    private final StreamixKafkaProducerInterceptor interceptor = new StreamixKafkaProducerInterceptor();

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void writesMdcValuesToRecordHeaders() {
        MDC.put(ObservabilityUtils.CORRELATION_ID, "a1b2c3d4-e5f6-7890-abcd-ef1234567890");
        MDC.put(ObservabilityUtils.USER_ID, "user-6");

        ProducerRecord<Object, Object> record = new ProducerRecord<>("topic", "value");
        ProducerRecord<Object, Object> result = interceptor.onSend(record);

        assertThat(headerValue(result, ObservabilityUtils.CORRELATION_HEADER))
                .isEqualTo("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
        assertThat(headerValue(result, ObservabilityUtils.USER_ID_HEADER)).isEqualTo("user-6");
    }

    @Test
    void doesNotAddHeadersWhenMdcIsEmpty() {
        ProducerRecord<Object, Object> record = new ProducerRecord<>("topic", "value");
        ProducerRecord<Object, Object> result = interceptor.onSend(record);

        assertThat(result.headers().lastHeader(ObservabilityUtils.CORRELATION_HEADER)).isNull();
        assertThat(result.headers().lastHeader(ObservabilityUtils.USER_ID_HEADER)).isNull();
    }

    @Test
    void returnsOriginalRecordInstance() {
        MDC.put(ObservabilityUtils.CORRELATION_ID, "a1b2c3d4-e5f6-7890-abcd-ef1234567890");
        ProducerRecord<Object, Object> record = new ProducerRecord<>("topic", "value");

        assertThat(interceptor.onSend(record)).isSameAs(record);
    }

    private static String headerValue(ProducerRecord<?, ?> record, String name) {
        Header header = record.headers().lastHeader(name);
        return header != null ? new String(header.value(), StandardCharsets.UTF_8) : null;
    }
}
