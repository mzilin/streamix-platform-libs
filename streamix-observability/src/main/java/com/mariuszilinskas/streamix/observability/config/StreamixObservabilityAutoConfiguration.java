package com.mariuszilinskas.streamix.observability.config;

import com.mariuszilinskas.streamix.observability.filter.StreamixLoggingFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(HttpServletRequest.class)
public class StreamixObservabilityAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnMissingBean(StreamixLoggingFilter.class)
    static class FilterConfiguration {

        @Bean
        public StreamixLoggingFilter streamixLoggingFilter(Environment env) {
            String serviceName = env.getProperty("spring.application.name", "");
            String environment = String.join(",", env.getActiveProfiles());
            return new StreamixLoggingFilter(serviceName, environment);
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
}
