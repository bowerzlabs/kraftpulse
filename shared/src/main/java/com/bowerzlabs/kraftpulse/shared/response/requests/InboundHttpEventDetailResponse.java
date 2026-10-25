package com.bowerzlabs.kraftpulse.shared.response.requests;

import com.bowerzlabs.kraftpulse.shared.constants.HttpMethod;
import com.bowerzlabs.kraftpulse.shared.model.ApplicationInboundHttpRequestEvent;
import com.bowerzlabs.kraftpulse.shared.model.EventMetadata;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Detailed DTO for single-request diagnostic modal/drawer views.
 */
public record InboundHttpEventDetailResponse(
        EventMetadata metadata,
        HttpMethod method,
        String path,
        String matchingPattern,
        Integer statusCode,
        long durationMs,
        long durationNanos,
        Instant startedAt,
        String controllerClass,
        String handlerMethod,
        String authenticatedUser,
        List<String> roles,
        String clientIp,
        String userAgent,
        long requestContentLength,
        long responseContentLength,
        Map<String, String> headers,
        Map<String, String> queryParams,
        int queryCount,
        long queryDurationMs,
        boolean hasException,
        boolean hasNPlusOne,
        boolean isError
) {
    public static InboundHttpEventDetailResponse fromEvent(ApplicationInboundHttpRequestEvent event) {
        return new InboundHttpEventDetailResponse(
                event.metadata(),
                event.method(),
                event.path(),
                event.matchingPattern(),
                event.statusCode(),
                event.durationMs(),
                event.durationNanos(),
                event.startedAt(),
                event.controllerClass(),
                event.handlerMethod(),
                event.authenticatedUser(),
                event.roles(),
                event.clientIp(),
                event.userAgent(),
                event.requestContentLength(),
                event.responseContentLength(),
                event.headers(),
                event.queryParams(),
                event.queryCount(),
                event.queryDurationMs(),
                event.hasException(),
                event.hasNPlusOne(),
                event.isError()
        );
    }
}