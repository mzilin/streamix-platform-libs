package com.mariuszilinskas.streamix.observability.servlet;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(FilterRegistrationBean.class)
public class StreamixServletObservabilityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(StreamixLoggingFilter.class)
    public StreamixLoggingFilter streamixLoggingFilter() {
        return new StreamixLoggingFilter();
    }

    @Bean
    public FilterRegistrationBean<@NonNull StreamixLoggingFilter> streamixLoggingFilterRegistration(
        StreamixLoggingFilter filter
    ) {
        FilterRegistrationBean<@NonNull StreamixLoggingFilter> registration =
                new FilterRegistrationBean<>();

        registration.setFilter(filter);
        registration.setOrder(Integer.MIN_VALUE + 10);

        return registration;
    }
}