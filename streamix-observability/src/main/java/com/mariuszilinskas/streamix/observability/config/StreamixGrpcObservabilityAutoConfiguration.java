package com.mariuszilinskas.streamix.observability.config;

import com.mariuszilinskas.streamix.observability.grpc.GrpcClientInterceptor;
import com.mariuszilinskas.streamix.observability.grpc.GrpcServerInterceptor;
import io.grpc.ServerInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@AutoConfiguration
@ConditionalOnClass(ServerInterceptor.class)
public class StreamixGrpcObservabilityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public GrpcServerInterceptor grpcServerInterceptor(Environment env) {
        String serviceName = env.getProperty("spring.application.name", "");
        String environment = String.join(",", env.getActiveProfiles());
        return new GrpcServerInterceptor(serviceName, environment);
    }

    @Bean
    @ConditionalOnMissingBean
    public GrpcClientInterceptor grpcClientInterceptor() {
        return new GrpcClientInterceptor();
    }
}
