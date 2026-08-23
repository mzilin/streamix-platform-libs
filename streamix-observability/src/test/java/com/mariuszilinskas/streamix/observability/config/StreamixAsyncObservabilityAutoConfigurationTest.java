package com.mariuszilinskas.streamix.observability.config;

import com.mariuszilinskas.streamix.observability.async.StreamixMdcTaskDecorator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class StreamixAsyncObservabilityAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withConfiguration(AutoConfigurations.of(StreamixAsyncObservabilityAutoConfiguration.class));

    @Test
    void registersTaskDecorator() {
        contextRunner.run(context ->
                assertThat(context).hasSingleBean(StreamixMdcTaskDecorator.class)
        );
    }

    @Test
    void doesNotRegisterDecoratorWhenUserProvidesOne() {
        contextRunner
                .withBean(StreamixMdcTaskDecorator.class, StreamixMdcTaskDecorator::new)
                .run(context -> assertThat(context).hasSingleBean(StreamixMdcTaskDecorator.class));
    }
}
