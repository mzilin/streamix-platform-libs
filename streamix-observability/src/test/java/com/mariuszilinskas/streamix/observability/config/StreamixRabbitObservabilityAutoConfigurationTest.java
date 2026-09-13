package com.mariuszilinskas.streamix.observability.config;

import com.mariuszilinskas.streamix.observability.rabbit.RabbitConsumerInterceptor;
import com.mariuszilinskas.streamix.observability.rabbit.RabbitProducerPostProcessor;
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
            assertThat(context).hasSingleBean(RabbitConsumerInterceptor.class);
            assertThat(context).hasSingleBean(RabbitProducerPostProcessor.class);
        });
    }

    @Test
    void doesNotRegisterConsumerInterceptorWhenUserProvidesOne() {
        contextRunner
                .withBean(RabbitConsumerInterceptor.class, () -> new RabbitConsumerInterceptor("svc", "test"))
                .run(context -> assertThat(context).hasSingleBean(RabbitConsumerInterceptor.class));
    }

    @Test
    void doesNotRegisterProducerPostProcessorWhenUserProvidesOne() {
        contextRunner
                .withBean(RabbitProducerPostProcessor.class, RabbitProducerPostProcessor::new)
                .run(context -> assertThat(context).hasSingleBean(RabbitProducerPostProcessor.class));
    }
}
