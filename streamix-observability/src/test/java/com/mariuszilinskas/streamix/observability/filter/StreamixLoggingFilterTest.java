package com.mariuszilinskas.streamix.observability.filter;

import com.mariuszilinskas.streamix.observability.util.ObservabilityUtils;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class StreamixLoggingFilterTest {

    private final StreamixLoggingFilter filter = new StreamixLoggingFilter("my-service", "test");

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void populatesAllMdcKeysOnRequest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        String correlationId = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";
        request.addHeader(ObservabilityUtils.CORRELATION_HEADER, correlationId);
        request.addHeader(ObservabilityUtils.USER_ID_HEADER, "user-42");

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        assertThat(response.getHeader(ObservabilityUtils.CORRELATION_HEADER)).isEqualTo(correlationId);
    }

    @Test
    void generatesMissingCorrelationId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        String echoed = response.getHeader(ObservabilityUtils.CORRELATION_HEADER);
        assertThat(echoed).isNotNull();
        assertThat(ObservabilityUtils.isValidCorrelationId(echoed)).isTrue();
    }

    @Test
    void generatesCorrelationIdForInvalidUuid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(ObservabilityUtils.CORRELATION_HEADER, "not-a-uuid");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        String echoed = response.getHeader(ObservabilityUtils.CORRELATION_HEADER);
        assertThat(echoed).isNotEqualTo("not-a-uuid");
        assertThat(ObservabilityUtils.isValidCorrelationId(echoed)).isTrue();
    }

    @Test
    void clearsMdcAfterRequest() throws Exception {
        MDC.put("existing-key", "existing-value");
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(MDC.get(ObservabilityUtils.CORRELATION_ID)).isNull();
        assertThat(MDC.get(ObservabilityUtils.USER_ID)).isNull();
        assertThat(MDC.get(ObservabilityUtils.SERVICE)).isNull();
        assertThat(MDC.get(ObservabilityUtils.ENVIRONMENT)).isNull();
        assertThat(MDC.get("existing-key")).isEqualTo("existing-value");
    }
}
