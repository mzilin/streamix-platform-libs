package com.mariuszilinskas.streamix.observability.config;

import com.mariuszilinskas.streamix.observability.async.StreamixMdcTaskDecorator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskDecorator;

@AutoConfiguration
@ConditionalOnClass(TaskDecorator.class)
public class StreamixAsyncObservabilityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public StreamixMdcTaskDecorator streamixMdcTaskDecorator() {
        return new StreamixMdcTaskDecorator();
    }
}
