package com.mariuszilinskas.streamix.observability.config;

import com.mariuszilinskas.streamix.observability.async.MdcTaskDecorator;
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
                assertThat(context).hasSingleBean(MdcTaskDecorator.class)
        );
    }

    @Test
    void doesNotRegisterDecoratorWhenUserProvidesOne() {
        contextRunner
                .withBean(MdcTaskDecorator.class, MdcTaskDecorator::new)
                .run(context -> assertThat(context).hasSingleBean(MdcTaskDecorator.class));
    }
}
