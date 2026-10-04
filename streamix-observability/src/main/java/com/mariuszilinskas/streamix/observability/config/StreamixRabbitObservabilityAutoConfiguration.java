package com.mariuszilinskas.streamix.observability.config;

import com.mariuszilinskas.streamix.observability.rabbit.RabbitConsumerInterceptor;
import com.mariuszilinskas.streamix.observability.rabbit.RabbitProducerPostProcessor;
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
    public RabbitConsumerInterceptor rabbitConsumerInterceptor(Environment env) {
        String serviceName = env.getProperty("spring.application.name", "");
        String environment = String.join(",", env.getActiveProfiles());
        return new RabbitConsumerInterceptor(serviceName, environment);
    }

    @Bean
    @ConditionalOnMissingBean
    public RabbitProducerPostProcessor rabbitProducerPostProcessor() {
        return new RabbitProducerPostProcessor();
    }

    @Bean
    @ConditionalOnBean(RabbitTemplate.class)
    public SmartInitializingSingleton rabbitProducerPostProcessorRegistrar(
            RabbitTemplate rabbitTemplate,
            RabbitProducerPostProcessor postProcessor
    ) {
        return () -> rabbitTemplate.addBeforePublishPostProcessors(postProcessor);
    }
}
