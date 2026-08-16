package com.mariuszilinskas.streamix.observability.reactive;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.context.annotation.Bean;
import org.springframework.web.server.WebFilter;

@AutoConfiguration
@ConditionalOnWebApplication(type = Type.REACTIVE)
@ConditionalOnClass(WebFilter.class)
public class StreamixReactiveObservabilityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public WebFilter streamixLoggingWebFilter() {
        return new StreamixLoggingWebFilter();
    }
}