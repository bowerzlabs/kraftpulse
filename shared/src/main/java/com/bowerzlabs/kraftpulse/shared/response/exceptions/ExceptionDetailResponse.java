package com.bowerzlabs.kraftpulse.shared.response.exceptions;

import com.bowerzlabs.kraftpulse.shared.model.ApplicationExceptionEvent;
import com.bowerzlabs.kraftpulse.shared.model.EventMetadata;

import java.time.Instant;
import java.util.Map;

public record ExceptionDetailResponse(
        EventMetadata metadata,
        String exceptionType,
        String simpleName,
        String message,
        String rootCauseType,
        String rootCauseMessage,
        String originClass,
        String originMethod,
        int lineNumber,
        String stacktrace,
        String httpMethod,
        String requestUri,
        String queryString,
        String handlerName,
        String clientIp,
        Map<String, String> headers,
        String linkedQuerySpanId,
        Instant firstSeen,
        Instant lastSeen,
        long occurrenceCount
) {
    public static ExceptionDetailResponse fromEvent(ApplicationExceptionEvent event) {
        return new ExceptionDetailResponse(
                event.metadata(),
                event.exceptionType(),
                event.simpleName(),
                event.message(),
                event.rootCauseType(),
                event.rootCauseMessage(),
                event.originClass(),
                event.originMethod(),
                event.lineNumber(),
                event.stacktrace(),
                event.httpMethod(),
                event.requestUri(),
                event.queryString(),
                event.handlerName(),
                event.clientIp(),
                event.headers(),
                event.linkedQuerySpanId(),
                event.firstSeen(),
                event.lastSeen(),
                event.occurrenceCount()
        );
    }
}