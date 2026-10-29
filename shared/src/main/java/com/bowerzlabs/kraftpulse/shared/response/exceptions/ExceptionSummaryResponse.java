package com.bowerzlabs.kraftpulse.shared.response.exceptions;

import com.bowerzlabs.kraftpulse.shared.model.ApplicationExceptionEvent;

import java.time.Instant;

public record ExceptionSummaryResponse(
        String id,
        String traceId,
        String exceptionType,
        String simpleName,
        String message,
        String originClass,
        String originMethod,
        int lineNumber,
        String httpMethod,
        String requestUri,
        long occurrenceCount,
        Instant lastSeen
) {
    public static ExceptionSummaryResponse fromEvent(ApplicationExceptionEvent event) {
        return new ExceptionSummaryResponse(
                event.id(),
                event.traceId(),
                event.exceptionType(),
                event.simpleName(),
                event.message(),
                event.originClass(),
                event.originMethod(),
                event.lineNumber(),
                event.httpMethod(),
                event.requestUri(),
                event.occurrenceCount(),
                event.lastSeen()
        );
    }
}