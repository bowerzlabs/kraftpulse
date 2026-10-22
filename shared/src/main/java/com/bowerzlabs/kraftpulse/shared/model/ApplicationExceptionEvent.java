package com.bowerzlabs.kraftpulse.shared.model;

import java.time.Instant;
import java.util.Map;

/**
 * Represents an application exception event captured within the KraftPulse diagnostic pipeline.
 *
 * @param id                 Deterministic SHA-256 fingerprint generated from exception details for auto-grouping and deduplication.
 * @param traceId            Global W3C trace ID correlating this exception across distributed service boundaries.
 * @param spanId             Active execution span ID pinpointing the specific operation where the error occurred.
 * @param exceptionType      Fully qualified class name of the thrown exception (e.g., "java.lang.NullPointerException").
 * @param simpleName         Short class name of the exception used for UI card headers and quick scanning (e.g., "NullPointerException").
 * @param message            Detail message string provided by the top-level exception instance.
 * @param rootCauseType      Fully qualified class name of the underlying unwrapped root cause exception.
 * @param rootCauseMessage   Detail message string provided by the unwrapped root cause exception.
 * @param originClass        Fully qualified class name where the exception was originally thrown (top frame of the stack trace).
 * @param originMethod       Name of the method where the exception was originally thrown.
 * @param lineNumber         Source code line number where the exception was originally thrown (-1 if unavailable).
 * @param stacktrace         Complete formatted string representation of the exception stack trace.
 * @param httpMethod         HTTP method of the request that triggered the exception (e.g., "GET", "POST"), or null if out-of-band.
 * @param requestUri         Request URI path that triggered the exception (e.g., "/api/v1/orders"), or null if out-of-band.
 * @param queryString        Raw query parameters attached to the incoming HTTP request, or null if absent.
 * @param handlerName        Target Spring MVC controller method handling the request (e.g., "OrderController#createOrder").
 * @param clientIp           Client IP address or resolved X-Forwarded-For origin IP address.
 * @param headers            Sanitized HTTP request headers (sensitive keys like Authorization or Cookie are masked).
 * @param linkedQuerySpanId  Optional correlation ID linking this exception directly to a specific executed database query span.
 * @param firstSeen          Timestamp when this specific exception fingerprint was first observed by KraftPulse.
 * @param lastSeen           Timestamp when this specific exception fingerprint was most recently observed.
 * @param occurrenceCount    Total cumulative count of times this exact exception fingerprint has recurred.
 */
public record ApplicationExceptionEvent(
        String id,
        String traceId,
        String spanId,
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
    public ApplicationExceptionEvent {
        headers = headers == null ? Map.of() : Map.copyOf(headers);
    }

    /**
     * Factory method for creating a newly observed exception event with an initial occurrence count of 1.
     */
    public static ApplicationExceptionEvent createNew(
            String id,
            String traceId,
            String spanId,
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
                id, traceId, spanId, exceptionType, simpleName, message,
                rootCauseType, rootCauseMessage, originClass, originMethod, lineNumber,
                stacktrace, httpMethod, requestUri, queryString, handlerName, clientIp,
                headers, null, now, now, 1L
        );
    }
}