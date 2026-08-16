package com.mariuszilinskas.streamix.observability.servlet;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnWebApplication(type = Type.SERVLET)
@ConditionalOnClass(FilterRegistrationBean.class)
public class StreamixServletObservabilityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public FilterRegistrationBean<@NonNull StreamixLoggingFilter> streamixLoggingFilter() {
        FilterRegistrationBean<@NonNull StreamixLoggingFilter> registration =
                new FilterRegistrationBean<>();

        registration.setFilter(new StreamixLoggingFilter());
        registration.setOrder(Integer.MIN_VALUE + 10);

        return registration;
    }
}