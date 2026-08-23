package com.mariuszilinskas.streamix.observability.config;

import com.mariuszilinskas.streamix.observability.kafka.StreamixKafkaConsumerInterceptor;
import com.mariuszilinskas.streamix.observability.kafka.StreamixKafkaProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.HashMap;
import java.util.Map;

@AutoConfiguration
@ConditionalOnClass(KafkaTemplate.class)
public class StreamixKafkaObservabilityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public StreamixKafkaConsumerInterceptor streamixKafkaConsumerInterceptor(Environment env) {
        String serviceName = env.getProperty("spring.application.name", "");
        String environment = String.join(",", env.getActiveProfiles());
        return new StreamixKafkaConsumerInterceptor(serviceName, environment);
    }

    @Bean
    @ConditionalOnBean(DefaultKafkaProducerFactory.class)
    @ConditionalOnMissingBean(name = "streamixKafkaProducerInterceptorRegistrar")
    public SmartInitializingSingleton streamixKafkaProducerInterceptorRegistrar(
            DefaultKafkaProducerFactory<?, ?> producerFactory
    ) {
        return () -> {
            Map<String, Object> configs = new HashMap<>(producerFactory.getConfigurationProperties());
            String existing = (String) configs.getOrDefault(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, "");
            String className = StreamixKafkaProducerInterceptor.class.getName();
            if (!existing.contains(className)) {
                configs.put(
                        ProducerConfig.INTERCEPTOR_CLASSES_CONFIG,
                        existing.isEmpty() ? className : existing + "," + className);
                producerFactory.updateConfigs(configs);
            }
        };
    }
}
