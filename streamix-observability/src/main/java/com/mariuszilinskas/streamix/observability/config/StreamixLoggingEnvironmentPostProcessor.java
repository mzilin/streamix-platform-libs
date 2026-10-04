package com.mariuszilinskas.streamix.observability.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.jspecify.annotations.NonNull;

import java.util.Map;

public class StreamixLoggingEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String PROPERTY_SOURCE_NAME = "streamixLoggingDefaults";

    @Override

    public void postProcessEnvironment(
            @NonNull ConfigurableEnvironment environment,
            @NonNull SpringApplication application
    ) {
        if (!environment.getPropertySources().contains(PROPERTY_SOURCE_NAME)) {
            environment.getPropertySources().addLast(
                new MapPropertySource(PROPERTY_SOURCE_NAME, Map.of("logging.structured.format.console", "ecs"))
            );
        }
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
