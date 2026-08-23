package com.mariuszilinskas.streamix.observability.config;

import com.mariuszilinskas.streamix.observability.rabbit.StreamixRabbitConsumerInterceptor;
import com.mariuszilinskas.streamix.observability.rabbit.StreamixRabbitProducerPostProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class StreamixRabbitObservabilityAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withConfiguration(AutoConfigurations.of(StreamixRabbitObservabilityAutoConfiguration.class));

    @Test
    void registersBothBeans() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(StreamixRabbitConsumerInterceptor.class);
            assertThat(context).hasSingleBean(StreamixRabbitProducerPostProcessor.class);
        });
    }

    @Test
    void doesNotRegisterConsumerInterceptorWhenUserProvidesOne() {
        contextRunner
                .withBean(StreamixRabbitConsumerInterceptor.class, () -> new StreamixRabbitConsumerInterceptor("svc", "test"))
                .run(context -> assertThat(context).hasSingleBean(StreamixRabbitConsumerInterceptor.class));
    }

    @Test
    void doesNotRegisterProducerPostProcessorWhenUserProvidesOne() {
        contextRunner
                .withBean(StreamixRabbitProducerPostProcessor.class, StreamixRabbitProducerPostProcessor::new)
                .run(context -> assertThat(context).hasSingleBean(StreamixRabbitProducerPostProcessor.class));
    }
}
