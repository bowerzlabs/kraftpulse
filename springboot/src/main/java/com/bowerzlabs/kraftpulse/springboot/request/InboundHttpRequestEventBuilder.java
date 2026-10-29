package com.bowerzlabs.kraftpulse.springboot.request;

import com.bowerzlabs.kraftpulse.core.trace.TraceContext;
import com.bowerzlabs.kraftpulse.core.trace.TraceProvider;
import com.bowerzlabs.kraftpulse.shared.constants.HttpMethod;
import com.bowerzlabs.kraftpulse.shared.model.ApplicationInboundHttpRequestEvent;
import com.bowerzlabs.kraftpulse.shared.model.EventMetadata;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;

import java.time.Instant;
import java.util.*;

public class InboundHttpRequestEventBuilder {

    private final String applicationName;
    private final TraceProvider traceProvider;

    public InboundHttpRequestEventBuilder(String applicationName, TraceProvider traceProvider) {
        this.applicationName = applicationName != null ? applicationName : "kraftpulse-app";
        this.traceProvider = traceProvider;
    }

    public ApplicationInboundHttpRequestEvent build(
            HttpServletRequest request,
            HttpServletResponse response,
            Instant startedAt,
            long durationNanos,
            Throwable unhandledException
    ) {
        String requestId = (String) request.getAttribute(HttpRequestCaptureFilter.INBOUND_REQUEST_ID);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
            request.setAttribute(HttpRequestCaptureFilter.INBOUND_REQUEST_ID, requestId);
        }

        TraceIds ids = resolveTraceIds(request);
        EventMetadata metadata = new EventMetadata(requestId, Instant.now(), applicationName, ids.traceId(), ids.spanId());

        String matchingPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        if (matchingPattern == null) matchingPattern = request.getRequestURI();

        String controllerClass = null;
        String handlerMethodName = null;
        Object handler = request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE);
        if (handler instanceof HandlerMethod hm) {
            controllerClass = hm.getBeanType().getSimpleName();
            handlerMethodName = hm.getMethod().getName();
        } else if (handler != null) {
            controllerClass = handler.getClass().getSimpleName();
        }

        int statusCode = response.getStatus();
        if (unhandledException != null && statusCode < 400) {
            statusCode = 500;
        }

        return ApplicationInboundHttpRequestEvent.create(
                metadata,
                HttpMethod.valueOf(request.getMethod().toUpperCase(Locale.ROOT)),
                request.getRequestURI(),
                matchingPattern,
                statusCode,
                durationNanos,
                startedAt,
                controllerClass,
                handlerMethodName,
                request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : null,
                List.of(),
                getClientIp(request),
                request.getHeader("User-Agent"),
                request.getContentLengthLong(),
                -1L,
                extractAndMaskHeaders(request),
                extractQueryParams(request),
                0,
                0L,
                unhandledException != null || statusCode >= 500,
                false
        );
    }

    private TraceIds resolveTraceIds(HttpServletRequest request) {
        String traceId = (String) request.getAttribute(HttpRequestCaptureFilter.TRACE_ID_ATTRIBUTE);
        String spanId = (String) request.getAttribute(HttpRequestCaptureFilter.SPAN_ID_ATTRIBUTE);

        if (isValid(traceId, 32, "0{32}") && isValid(spanId, 16, "0{16}")) {
            return new TraceIds(traceId, spanId);
        }

        if (traceProvider != null) {
            try {
                TraceContext ctx = traceProvider.currentContext();
                if (ctx != null) {
                    if (!isValid(traceId, 32, "0{32}") && isValid(ctx.traceId(), 32, "0{32}")) traceId = ctx.traceId();
                    if (!isValid(spanId, 16, "0{16}") && isValid(ctx.spanId(), 16, "0{16}")) spanId = ctx.spanId();
                }
            } catch (RuntimeException ignored) {}
        }

        if (!isValid(traceId, 32, "0{32}")) traceId = UUID.randomUUID().toString().replace("-", "");
        if (!isValid(spanId, 16, "0{16}")) spanId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        request.setAttribute(HttpRequestCaptureFilter.TRACE_ID_ATTRIBUTE, traceId);
        request.setAttribute(HttpRequestCaptureFilter.SPAN_ID_ATTRIBUTE, spanId);

        return new TraceIds(traceId, spanId);
    }

    private boolean isValid(String val, int len, String zeros) {
        return val != null && val.matches("[0-9a-fA-F]{" + len + "}") && !val.matches(zeros);
    }

    private Map<String, String> extractAndMaskHeaders(HttpServletRequest request) {
        Map<String, String> map = new HashMap<>();
        Enumeration<String> names = request.getHeaderNames();
        if (names != null) {
            while (names.hasMoreElements()) {
                String name = names.nextElement();
                String lower = name.toLowerCase(Locale.ROOT);
                if (lower.contains("authorization") || lower.contains("cookie") || lower.contains("api-key") || lower.contains("set-cookie")) {
                    map.put(name, "[MASKED]");
                } else {
                    map.put(name, request.getHeader(name));
                }
            }
        }
        return map;
    }

    private Map<String, String> extractQueryParams(HttpServletRequest request) {
        Map<String, String> map = new HashMap<>();
        request.getParameterMap().forEach((k, v) -> map.put(k, v == null ? "" : String.join(",", v)));
        return map;
    }

    private String getClientIp(HttpServletRequest request) {
        String fwd = request.getHeader("X-Forwarded-For");
        return (fwd != null && !fwd.isBlank()) ? fwd.split(",")[0].trim() : request.getRemoteAddr();
    }

    private record TraceIds(String traceId, String spanId) {}
}