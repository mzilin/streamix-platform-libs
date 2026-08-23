package com.mariuszilinskas.streamix.observability.kafka;

import com.mariuszilinskas.streamix.observability.util.ObservabilityUtils;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.record.TimestampType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class StreamixKafkaConsumerInterceptorTest {

    private final StreamixKafkaConsumerInterceptor interceptor = new StreamixKafkaConsumerInterceptor("test-service", "test");

    @Mock
    private Consumer<Object, Object> consumer;

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    private ConsumerRecord<Object, Object> recordWithHeaders(String correlationId, String userId) {
        RecordHeaders headers = new RecordHeaders();
        if (correlationId != null) {
            headers.add(ObservabilityUtils.CORRELATION_HEADER, correlationId.getBytes(StandardCharsets.UTF_8));
        }
        if (userId != null) {
            headers.add(ObservabilityUtils.USER_ID_HEADER, userId.getBytes(StandardCharsets.UTF_8));
        }
        return new ConsumerRecord<Object, Object>(
                "topic", 0, 0L, 0L, TimestampType.NO_TIMESTAMP_TYPE, 0, 0, null, null, headers, Optional.empty()
        );
    }

    @Test
    void extractsCorrelationIdAndUserIdFromRecordHeaders() {
        String correlationId = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";
        ConsumerRecord<Object, Object> record = recordWithHeaders(correlationId, "user-8");

        interceptor.intercept(record, consumer);

        assertThat(MDC.get(ObservabilityUtils.CORRELATION_ID)).isEqualTo(correlationId);
        assertThat(MDC.get(ObservabilityUtils.USER_ID)).isEqualTo("user-8");
    }

    @Test
    void generatesCorrelationIdWhenHeaderMissing() {
        ConsumerRecord<Object, Object> record = recordWithHeaders(null, null);

        interceptor.intercept(record, consumer);

        String correlationId = MDC.get(ObservabilityUtils.CORRELATION_ID);
        assertThat(correlationId).isNotNull();
        assertThat(ObservabilityUtils.isValidCorrelationId(correlationId)).isTrue();
    }

    @Test
    void clearsMdcOnSuccess() {
        MDC.put("pre-existing", "value");
        ConsumerRecord<Object, Object> record = recordWithHeaders(
                "a1b2c3d4-e5f6-7890-abcd-ef1234567890", null
        );
        interceptor.intercept(record, consumer);

        interceptor.success(record, consumer);

        assertThat(MDC.get(ObservabilityUtils.CORRELATION_ID)).isNull();
        assertThat(MDC.get("pre-existing")).isEqualTo("value");
    }

    @Test
    void clearsMdcOnFailure() {
        MDC.put("pre-existing", "value");
        ConsumerRecord<Object, Object> record = recordWithHeaders(
                "a1b2c3d4-e5f6-7890-abcd-ef1234567890", null
        );
        interceptor.intercept(record, consumer);

        interceptor.failure(record, new RuntimeException("fail"), consumer);

        assertThat(MDC.get(ObservabilityUtils.CORRELATION_ID)).isNull();
        assertThat(MDC.get("pre-existing")).isEqualTo("value");
    }

    @Test
    void returnsRecordUnmodified() {
        ConsumerRecord<Object, Object> record = recordWithHeaders(
                "a1b2c3d4-e5f6-7890-abcd-ef1234567890", null
        );

        ConsumerRecord<Object, Object> result = interceptor.intercept(record, consumer);
        interceptor.success(record, consumer);

        assertThat(result).isSameAs(record);
    }
}
