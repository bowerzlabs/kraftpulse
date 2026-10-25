package com.bowerzlabs.kraftpulse.shared.response.requests;

import com.bowerzlabs.kraftpulse.shared.constants.HttpMethod;
import com.bowerzlabs.kraftpulse.shared.model.ApplicationInboundHttpRequestEvent;

import java.time.Instant;

/**
 * Lightweight DTO for dashboard list views and telemetry streams.
 */
public record InboundHttpEventSummaryResponse(
        String eventId,
        String traceId,
        String spanId,
        Instant timestamp,
        HttpMethod method,
        String path,
        String matchingPattern,
        Integer statusCode,
        long durationMs,
        String controllerClass,
        String handlerMethod,
        String authenticatedUser,
        int queryCount,
        long queryDurationMs,
        boolean hasException,
        boolean hasNPlusOne
) {
    public static InboundHttpEventSummaryResponse fromEvent(ApplicationInboundHttpRequestEvent event) {
        return new InboundHttpEventSummaryResponse(
                event.metadata().eventId(),
                event.metadata().traceId(),
                event.metadata().spanId(),
                event.metadata().timestamp(),
                event.method(),
                event.path(),
                event.matchingPattern(),
                event.statusCode(),
                event.durationMs(),
                event.controllerClass(),
                event.handlerMethod(),
                event.authenticatedUser(),
                event.queryCount(),
                event.queryDurationMs(),
                event.hasException(),
                event.hasNPlusOne()
        );
    }
}