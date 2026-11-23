package com.g7.crowdfunding.shared.infrastructure.web;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @Test
    void keepsTheIdSentByTheClientAndReturnsIt() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/campaigns");
        request.addHeader(CorrelationIdFilter.HEADER, "demo-001");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> seen = new AtomicReference<>();

        filter.doFilter(request, response, (req, res) -> seen.set(MDC.get(CorrelationIdFilter.MDC_KEY)));

        assertThat(seen.get()).isEqualTo("demo-001");
        assertThat(response.getHeader(CorrelationIdFilter.HEADER)).isEqualTo("demo-001");
    }

    @Test
    void generatesAnIdWhenMissingAndCleansTheMdcAfterwards() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(new MockHttpServletRequest("GET", "/api/campaigns"), response, (req, res) -> { });

        assertThat(response.getHeader(CorrelationIdFilter.HEADER)).isNotBlank();
        assertThat(MDC.get(CorrelationIdFilter.MDC_KEY)).isNull();
    }
}
