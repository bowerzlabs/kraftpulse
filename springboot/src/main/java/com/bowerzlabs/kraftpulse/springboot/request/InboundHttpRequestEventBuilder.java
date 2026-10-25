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
        String eventId = UUID.randomUUID().toString();

        // Resolve trace and span IDs from OpenTelemetry context via TraceProvider
        String traceId = eventId;
        String spanId = eventId; // Fallback to unique ID if no active span exists

        if (traceProvider != null) {
            TraceContext ctx = traceProvider.currentContext();
            if (ctx != null) {
                if (ctx.traceId() != null && !ctx.traceId().isBlank()) {
                    traceId = ctx.traceId();
                }
                if (ctx.spanId() != null && !ctx.spanId().isBlank()) {
                    spanId = ctx.spanId();
                }
            }
        }

        EventMetadata metadata = new EventMetadata(
                eventId,
                Instant.now(),
                applicationName,
                traceId,
                spanId
        );

        // 1. Extract URI template matched by Spring MVC
        String matchingPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        if (matchingPattern == null) {
            matchingPattern = request.getRequestURI();
        }

        // 2. Safely resolve Controller Class and Handler Method names
        String controllerClass = null;
        String handlerMethodName = null;
        Object handler = request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE);

        if (handler instanceof HandlerMethod handlerMethod) {
            controllerClass = handlerMethod.getBeanType().getSimpleName();
            handlerMethodName = handlerMethod.getMethod().getName();
        } else if (handler != null) {
            controllerClass = handler.getClass().getSimpleName();
        }

        // 3. Sanitized Headers & Parameters
        Map<String, String> headers = extractAndMaskHeaders(request);
        Map<String, String> queryParams = extractQueryParams(request);

        return ApplicationInboundHttpRequestEvent.create(
                metadata,
                HttpMethod.valueOf(request.getMethod().toUpperCase()),
                request.getRequestURI(),
                matchingPattern,
                response.getStatus(),
                durationNanos,
                startedAt,
                controllerClass,
                handlerMethodName,
                null,
                List.of(),
                getClientIp(request),
                request.getHeader("User-Agent"),
                request.getContentLengthLong(),
                response.getBufferSize(),
                headers,
                queryParams,
                0,
                0L,
                unhandledException != null || response.getStatus() >= 500,
                false
        );
    }

    private Map<String, String> extractAndMaskHeaders(HttpServletRequest request) {
        Map<String, String> map = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                String lower = name.toLowerCase();
                if (lower.contains("authorization") || lower.contains("cookie") || lower.contains("api-key")) {
                    map.put(name, "******");
                } else {
                    map.put(name, request.getHeader(name));
                }
            }
        }
        return map;
    }

    private Map<String, String> extractQueryParams(HttpServletRequest request) {
        Map<String, String> map = new HashMap<>();
        request.getParameterMap().forEach((k, v) -> map.put(k, String.join(",", v)));
        return map;
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}