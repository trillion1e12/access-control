package com.tmc.access.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.concurrent.atomic.AtomicReference;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RequestIdFilterTest {
    private final RequestIdFilter filter = new RequestIdFilter();
    private final AtomicReference<String> idSeenByChain = new AtomicReference<>();
    private final FilterChain chain = (request, response) -> idSeenByChain.set(MDC.get(RequestIdFilter.MDC_KEY));

    @Test
    void reusesTheIdSentByTheCaller() throws Exception {
        MockHttpServletResponse response = run("abc-123");

        assertEquals("abc-123", idSeenByChain.get());
        assertEquals("abc-123", response.getHeader(RequestIdFilter.HEADER));
    }

    @Test
    void generatesAnIdWhenNoneIsSent() throws Exception {
        MockHttpServletResponse response = run(null);

        assertNotEquals(null, idSeenByChain.get());
        assertEquals(idSeenByChain.get(), response.getHeader(RequestIdFilter.HEADER));
    }

    @Test
    void replacesAnIdThatCouldInjectTextIntoTheLogs() throws Exception {
        run("abc\nINFO fake log line");

        assertNotEquals("abc\nINFO fake log line", idSeenByChain.get());
    }

    @Test
    void removesTheIdWhenTheRequestEnds() throws Exception {
        run("abc-123");

        assertNull(MDC.get(RequestIdFilter.MDC_KEY));
    }

    private MockHttpServletResponse run(String requestId) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        if (requestId != null) {
            request.addHeader(RequestIdFilter.HEADER, requestId);
        }
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, chain);
        return response;
    }
}
