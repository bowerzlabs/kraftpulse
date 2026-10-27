package com.bowerzlabs.kraftpulse.shared.model;

import com.bowerzlabs.kraftpulse.shared.constants.HttpMethod;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Represents an inbound HTTP request event captured at the web layer (Spring MVC / Spring WebFlux).
 * Serves as the root diagnostic unit for request tracing, endpoint performance profiling, and security context logging.
 *
 * @param metadata           Standard KraftPulse event metadata containing eventId, timestamp, applicationName, traceId, and spanId.
 * @param method             HTTP request method (GET, POST, PUT, DELETE, etc.).
 * @param path               Canonical request URI path (e.g., "/api/v1/orders/12345").
 * @param matchingPattern    Spring MVC / WebFlux matched route template (e.g., "/api/v1/orders/{id}") to prevent high-cardinality metric explosion.
 * @param statusCode         HTTP response status code returned to the client (e.g., 200, 404, 500).
 * @param durationMs         Total execution time of the HTTP request in milliseconds.
 * @param durationNanos      High-precision request execution duration in nanoseconds.
 * @param startedAt          Exact timestamp when the request execution initiated at the filter boundary.
 * @param controllerClass    Target Spring bean or handler class name (e.g., "OrderController").
 * @param handlerMethod      Target controller method name (e.g., "getOrderById").
 * @param authenticatedUser  Resolved principal/username from Spring Security context, or null if unauthenticated.
 * @param roles              List of GrantedAuthority role strings associated with the authenticated user.
 * @param clientIp           Client IP address or resolved X-Forwarded-For origin address.
 * @param userAgent          HTTP User-Agent header string for client device identification.
 * @param requestContentLength  Size of the incoming HTTP request body payload in bytes (-1 if unknown).
 * @param responseContentLength Size of the outgoing HTTP response body payload in bytes (-1 if unknown).
 * @param headers            Sanitized HTTP request headers (sensitive keys like Authorization and Cookie are masked).
 * @param queryParams        Sanitized request query parameters map.
 * @param queryCount         Total number of database queries executed during the processing of this HTTP request.
 * @param queryDurationMs    Cumulative duration spent waiting for database queries during this HTTP request in milliseconds.
 * @param hasException       True if an unhandled exception occurred during request execution.
 * @param hasNPlusOne        True if KraftPulse's query analyzer detected an N+1 query pattern during this request.
 */
public record ApplicationInboundHttpRequestEvent(
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
        boolean hasNPlusOne
) implements KraftPulseEvent {

    public ApplicationInboundHttpRequestEvent {
        roles = roles == null ? List.of() : List.copyOf(roles);
        headers = headers == null ? Map.of() : Map.copyOf(headers);
        queryParams = queryParams == null ? Map.of() : Map.copyOf(queryParams);
    }

    /**
     * Helper check to determine if the HTTP request completed with a 4xx or 5xx status or unhandled error.
     */
    public boolean isError() {
        return (statusCode != null && statusCode >= 400) || hasException;
    }

    /**
     * Factory method for creating an inbound HTTP event with execution summary metrics.
     */
    public static ApplicationInboundHttpRequestEvent create(
            EventMetadata metadata,
            HttpMethod method,
            String path,
            String matchingPattern,
            Integer statusCode,
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
            boolean hasNPlusOne
    ) {
        long durationMs = durationNanos / 1_000_000;
        return new ApplicationInboundHttpRequestEvent(
                metadata, method, path, matchingPattern, statusCode,
                durationMs, durationNanos, startedAt, controllerClass,
                handlerMethod, authenticatedUser, roles, clientIp, userAgent,
                requestContentLength, responseContentLength, headers, queryParams,
                queryCount, queryDurationMs, hasException, hasNPlusOne
        );
    }
}