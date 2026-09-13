package com.mariuszilinskas.streamix.observability.config;

import com.mariuszilinskas.streamix.observability.kafka.KafkaConsumerInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class StreamixKafkaObservabilityAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withConfiguration(AutoConfigurations.of(StreamixKafkaObservabilityAutoConfiguration.class));

    @Test
    void registersConsumerInterceptor() {
        contextRunner.run(context ->
                assertThat(context).hasSingleBean(KafkaConsumerInterceptor.class)
        );
    }

    @Test
    void doesNotRegisterConsumerInterceptorWhenUserProvidesOne() {
        contextRunner
                .withBean(KafkaConsumerInterceptor.class, () -> new KafkaConsumerInterceptor("svc", "test"))
                .run(context -> assertThat(context).hasSingleBean(KafkaConsumerInterceptor.class));
    }
}
