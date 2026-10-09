package com.example.elderai.observability;

import org.slf4j.MDC;

/**
 * 当前请求追踪信息的统一入口。
 */
public final class TraceContext {

    public static final String TRACE_ID_KEY = "traceId";
    public static final String TRACE_ID_HEADER = "X-Request-ID";
    public static final String TRACE_ID_ATTRIBUTE = TraceContext.class.getName() + ".traceId";

    private TraceContext() {
    }

    public static String currentTraceId() {
        return MDC.get(TRACE_ID_KEY);
    }
}
