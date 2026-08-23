package com.mariuszilinskas.streamix.observability.config;

import com.mariuszilinskas.streamix.observability.rabbit.StreamixRabbitConsumerInterceptor;
import com.mariuszilinskas.streamix.observability.rabbit.StreamixRabbitProducerPostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@AutoConfiguration
@ConditionalOnClass(RabbitTemplate.class)
public class StreamixRabbitObservabilityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public StreamixRabbitConsumerInterceptor streamixRabbitConsumerInterceptor(Environment env) {
        String serviceName = env.getProperty("spring.application.name", "");
        String environment = String.join(",", env.getActiveProfiles());
        return new StreamixRabbitConsumerInterceptor(serviceName, environment);
    }

    @Bean
    @ConditionalOnMissingBean
    public StreamixRabbitProducerPostProcessor streamixRabbitProducerPostProcessor() {
        return new StreamixRabbitProducerPostProcessor();
    }

    @Bean
    @ConditionalOnBean(RabbitTemplate.class)
    public SmartInitializingSingleton streamixRabbitProducerPostProcessorRegistrar(
            RabbitTemplate rabbitTemplate,
            StreamixRabbitProducerPostProcessor postProcessor
    ) {
        return () -> rabbitTemplate.addBeforePublishPostProcessors(postProcessor);
    }
}
