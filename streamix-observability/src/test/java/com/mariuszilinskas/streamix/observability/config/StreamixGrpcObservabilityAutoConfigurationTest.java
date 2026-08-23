package com.mariuszilinskas.streamix.observability.config;

import com.mariuszilinskas.streamix.observability.grpc.StreamixGrpcClientInterceptor;
import com.mariuszilinskas.streamix.observability.grpc.StreamixGrpcServerInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class StreamixGrpcObservabilityAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withConfiguration(AutoConfigurations.of(StreamixGrpcObservabilityAutoConfiguration.class));

    @Test
    void registersBothInterceptors() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(StreamixGrpcServerInterceptor.class);
            assertThat(context).hasSingleBean(StreamixGrpcClientInterceptor.class);
        });
    }

    @Test
    void doesNotRegisterServerInterceptorWhenUserProvidesOne() {
        contextRunner
                .withBean(StreamixGrpcServerInterceptor.class, () -> new StreamixGrpcServerInterceptor("svc", "test"))
                .run(context -> assertThat(context).hasSingleBean(StreamixGrpcServerInterceptor.class));
    }

    @Test
    void doesNotRegisterClientInterceptorWhenUserProvidesOne() {
        contextRunner
                .withBean(StreamixGrpcClientInterceptor.class, StreamixGrpcClientInterceptor::new)
                .run(context -> assertThat(context).hasSingleBean(StreamixGrpcClientInterceptor.class));
    }
}
