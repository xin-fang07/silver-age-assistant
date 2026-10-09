package com.example.elderai.observability;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RequestTraceFilterTest {

    private final RequestTraceFilter filter = new RequestTraceFilter();

    @Test
    void shouldReuseSafeClientRequestId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(TraceContext.TRACE_ID_HEADER, "client-request-123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals("client-request-123", response.getHeader(TraceContext.TRACE_ID_HEADER));
        assertEquals("client-request-123", request.getAttribute(TraceContext.TRACE_ID_ATTRIBUTE));
        assertNull(MDC.get(TraceContext.TRACE_ID_KEY));
    }

    @Test
    void shouldReplaceUnsafeClientRequestId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(TraceContext.TRACE_ID_HEADER, "bad id\nvalue");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        String generated = response.getHeader(TraceContext.TRACE_ID_HEADER);
        assertNotEquals("bad id\nvalue", generated);
        assertEquals(32, generated.length());
        assertNull(MDC.get(TraceContext.TRACE_ID_KEY));
    }
}
