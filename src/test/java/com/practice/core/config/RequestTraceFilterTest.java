package com.practice.core.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class RequestTraceFilterTest {

    private final RequestTraceFilter filter = new RequestTraceFilter();

    @Test
    void testUsesExistingTraceIdHeaderWhenPresent() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("traceId", "trace-123");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        filter.doFilter(request, response, filterChain);

        assertThat(request.getAttribute("traceId")).isEqualTo("trace-123");
        assertThat(response.getHeader("X-Request-Id")).isEqualTo("trace-123");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testGeneratesTraceIdWhenHeaderMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        filter.doFilter(request, response, filterChain);

        String generated = (String) request.getAttribute("traceId");
        assertThat(generated).isNotBlank();
        assertThat(response.getHeader("X-Request-Id")).isEqualTo(generated);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testGeneratesTraceIdWhenHeaderBlank() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("traceId", "  ");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        filter.doFilter(request, response, filterChain);

        String generated = (String) request.getAttribute("traceId");
        assertThat(generated).isNotBlank().isNotEqualTo("  ");
        assertThat(response.getHeader("X-Request-Id")).isEqualTo(generated);
        verify(filterChain).doFilter(request, response);
    }
}
