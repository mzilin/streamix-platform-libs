package com.mariuszilinskas.streamix.observability;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class StreamixObservabilityAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withConfiguration(
                            AutoConfigurations.of(
                                    StreamixObservabilityAutoConfiguration.class
                            )
                    );

    @Test
    void contextLoads() {
        contextRunner.run(context -> {
            assertThat(context).isNotNull();
        });
    }
}
