package com.mariuszilinskas.streamix.observability.config;

import com.mariuszilinskas.streamix.observability.grpc.GrpcClientInterceptor;
import com.mariuszilinskas.streamix.observability.grpc.GrpcServerInterceptor;
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
            assertThat(context).hasSingleBean(GrpcServerInterceptor.class);
            assertThat(context).hasSingleBean(GrpcClientInterceptor.class);
        });
    }

    @Test
    void doesNotRegisterServerInterceptorWhenUserProvidesOne() {
        contextRunner
                .withBean(GrpcServerInterceptor.class, () -> new GrpcServerInterceptor("svc", "test"))
                .run(context -> assertThat(context).hasSingleBean(GrpcServerInterceptor.class));
    }

    @Test
    void doesNotRegisterClientInterceptorWhenUserProvidesOne() {
        contextRunner
                .withBean(GrpcClientInterceptor.class, GrpcClientInterceptor::new)
                .run(context -> assertThat(context).hasSingleBean(GrpcClientInterceptor.class));
    }
}
