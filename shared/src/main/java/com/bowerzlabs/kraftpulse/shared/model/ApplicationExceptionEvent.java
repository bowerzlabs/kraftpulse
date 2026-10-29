
package com.bowerzlabs.kraftpulse.shared.model;

import java.time.Instant;
import java.util.Map;

/**
 * Represents an application exception event captured within the KraftPulse diagnostic pipeline.
 *
 * @param id                 Deterministic exception fingerprint used for grouping and deduplication.
 * @param requestId          Unique identifier of the HTTP request associated with this exception occurrence.
 * @param traceId            Distributed trace ID, when available.
 * @param spanId             Active execution span ID, when available.
 * @param applicationName    Name of the application instance capturing the event.
 * @param exceptionType      Fully qualified class name of the thrown exception.
 * @param simpleName         Short exception class name used for display.
 * @param message             Detail message from the top-level exception.
 * @param rootCauseType      Fully qualified class name of the root cause.
 * @param rootCauseMessage   Detail message from the root cause.
 * @param originClass        Fully qualified class name where the exception originated.
 * @param originMethod       Method where the exception originated.
 * @param lineNumber         Source line number, or -1 if unavailable.
 * @param stacktrace         Formatted exception stack trace.
 * @param httpMethod         HTTP method associated with the request, or null.
 * @param requestUri         Request URI path, or null.
 * @param queryString        Raw query parameters, or null.
 * @param handlerName        Spring MVC handler method, or null.
 * @param clientIp           Resolved client IP address, or null.
 * @param headers            Sanitized HTTP request headers.
 * @param linkedQuerySpanId  Optional identifier linking this exception to a database query.
 * @param firstSeen          Timestamp when this exception fingerprint was first observed.
 * @param lastSeen           Timestamp when this exception fingerprint was most recently observed.
 * @param occurrenceCount    Number of occurrences of this exception fingerprint.
 */
public record ApplicationExceptionEvent(
        String id,
        String requestId,
        String traceId,
        String spanId,
        String applicationName,
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
) implements KraftPulseEvent {

    public ApplicationExceptionEvent {
        headers = headers == null ? Map.of() : Map.copyOf(headers);
    }

    /**
     * Returns a copy of this event with masked headers.
     */
    public ApplicationExceptionEvent withMaskedHeaders(Map<String, String> maskedHeaders) {
        return new ApplicationExceptionEvent(
                id, requestId, traceId, spanId, applicationName,
                exceptionType, simpleName, message,
                rootCauseType, rootCauseMessage,
                originClass, originMethod, lineNumber, stacktrace,
                httpMethod, requestUri, queryString, handlerName,
                clientIp, maskedHeaders, linkedQuerySpanId,
                firstSeen, lastSeen, occurrenceCount
        );
    }

    @Override
    public EventMetadata metadata() {
        return new EventMetadata(
                id,
                lastSeen,
                applicationName,
                traceId,
                spanId
        );
    }

    /**
     * Creates a newly observed exception with an initial occurrence count of one.
     */
    public static ApplicationExceptionEvent createNew(
            String id,
            String requestId,
            String traceId,
            String spanId,
            String applicationName,
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
            Map<String, String> headers
    ) {
        Instant now = Instant.now();

        return new ApplicationExceptionEvent(
                id, requestId, traceId, spanId, applicationName,
                exceptionType, simpleName, message,
                rootCauseType, rootCauseMessage,
                originClass, originMethod, lineNumber, stacktrace,
                httpMethod, requestUri, queryString, handlerName,
                clientIp, headers, null, now, now, 1L
        );
    }
}
