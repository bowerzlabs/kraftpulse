package com.bowerzlabs.kraftpulse.springboot.outbound;

import com.bowerzlabs.kraftpulse.core.trace.TraceContext;
import com.bowerzlabs.kraftpulse.core.trace.TraceProvider;
import com.bowerzlabs.kraftpulse.shared.context.RequestScope;
import com.bowerzlabs.kraftpulse.springboot.request.HttpRequestCaptureFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.Nullable;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Finds the request id, trace id and span id for the current thread. Sources, in order:
 * 1. TraceProvider.currentContext()
 * 2. attributes HttpRequestCaptureFilter put on the servlet request
 * 3. RequestScope (the thread-bound request id also used for JDBC queries)
 * This is the single place that does the lookup. The interceptors just call resolve().
 */
public class OutboundContextResolver {

    private final TraceProvider traceProvider;

    public OutboundContextResolver(@Nullable TraceProvider traceProvider) {
        this.traceProvider = traceProvider;
    }

    public CallContext resolve() {
        TraceContext ctx = null;
        try {
            ctx = traceProvider != null ? traceProvider.currentContext() : null;
        } catch (RuntimeException ignored) {
            // fall back to the other sources
        }

        String requestId = ctx != null ? ctx.requestId() : null;
        String traceId = ctx != null ? ctx.traceId() : null;
        String spanId = ctx != null ? ctx.spanId() : null;

        HttpServletRequest request = currentRequest();
        if (request != null) {
            if (isBlank(requestId)) {
                requestId = attribute(request, HttpRequestCaptureFilter.INBOUND_REQUEST_ID);
            }
            if (!validId(traceId, 32)) {
                traceId = attribute(request, HttpRequestCaptureFilter.TRACE_ID_ATTRIBUTE);
            }
            if (!validId(spanId, 16)) {
                spanId = attribute(request, HttpRequestCaptureFilter.SPAN_ID_ATTRIBUTE);
            }
        }

        if (isBlank(requestId)) {
            requestId = RequestScope.current();
        }

        return new CallContext(
                isBlank(requestId) ? null : requestId,
                validId(traceId, 32) ? traceId : null,
                validId(spanId, 16) ? spanId : null);
    }

    private static HttpServletRequest currentRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        return attributes instanceof ServletRequestAttributes servlet ? servlet.getRequest() : null;
    }

    private static String attribute(HttpServletRequest request, String name) {
        try {
            Object value = request.getAttribute(name);
            return value instanceof String s ? s : null;
        } catch (IllegalStateException recycled) {
            return null;
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    /** Hex of the expected length and not all zeros (W3C trace/span id rules). */
    private static boolean validId(String value, int length) {
        return value != null
                && value.length() == length
                && value.matches("[0-9a-fA-F]+")
                && !value.matches("0+");
    }
}