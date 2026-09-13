package com.mariuszilinskas.streamix.observability.config;

import com.mariuszilinskas.streamix.observability.context.LogContext;
import com.mariuszilinskas.streamix.observability.filter.LoggingFilter;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class StreamixObservabilityAutoConfigurationTest {

    private final WebApplicationContextRunner contextRunner =
            new WebApplicationContextRunner()
                    .withConfiguration(AutoConfigurations.of(StreamixObservabilityAutoConfiguration.class));

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void registersLoggingFilter() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(LoggingFilter.class);
            assertThat(context).hasSingleBean(FilterRegistrationBean.class);

            FilterRegistrationBean<?> registration = context.getBean(FilterRegistrationBean.class);
            assertThat(registration.getOrder()).isEqualTo(Integer.MIN_VALUE + 10);
        });
    }

    @Test
    void doesNotRegisterFilterWhenUserProvidesOne() {
        contextRunner
                .withBean(LoggingFilter.class, () -> new LoggingFilter("svc", "test"))
                .run(context -> {
                    assertThat(context).hasSingleBean(LoggingFilter.class);
                    assertThat(context).doesNotHaveBean(FilterRegistrationBean.class);
                });
    }

    @Test
    void filterSetsServiceAndEnvironmentFromProperties() throws Exception {
        contextRunner
                .withPropertyValues("spring.application.name=users-service", "spring.profiles.active=dev")
                .run(context -> {
                    LoggingFilter filter = context.getBean(LoggingFilter.class);
                    MockHttpServletRequest request = new MockHttpServletRequest();
                    MockHttpServletResponse response = new MockHttpServletResponse();

                    filter.doFilter(request, response, (req, res) -> {
                        assertThat(MDC.get(LogContext.SERVICE)).isEqualTo("users-service");
                        assertThat(MDC.get(LogContext.ENVIRONMENT)).isEqualTo("dev");
                    });
                });
    }

    @Test
    void filterSetsCorrelationIdAndUserIdDuringRequest() throws Exception {
        contextRunner.run(context -> {
            LoggingFilter filter = context.getBean(LoggingFilter.class);
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader(LogContext.CORRELATION_HEADER, "a1b2c3d4-e5f6-7890-abcd-ef1234567890");
            request.addHeader(LogContext.USER_ID_HEADER, "user-1");
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilter(request, response, (req, res) -> {
                assertThat(MDC.get(LogContext.CORRELATION_ID)).isEqualTo("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
                assertThat(MDC.get(LogContext.USER_ID)).isEqualTo("user-1");
            });
        });
    }

    @Test
    void filterClearsMdcAfterRequest() throws Exception {
        contextRunner.run(context -> {
            LoggingFilter filter = context.getBean(LoggingFilter.class);
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilter(request, response, mock(FilterChain.class));

            assertThat(MDC.get(LogContext.CORRELATION_ID)).isNull();
            assertThat(MDC.get(LogContext.SERVICE)).isNull();
            assertThat(MDC.get(LogContext.ENVIRONMENT)).isNull();
        });
    }
}
