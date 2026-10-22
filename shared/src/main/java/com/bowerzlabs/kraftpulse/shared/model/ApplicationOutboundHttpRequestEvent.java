package com.bowerzlabs.kraftpulse.shared.model;

import java.time.Instant;
import java.util.Map;

/**
 * Represents an outbound HTTP request event executed by the application
 * (e.g., via RestTemplate, WebClient, RestClient, or HTTP interface clients).
 *
 * @param id                 Unique identifier for this outbound request event.
 * @param traceId            Global W3C trace ID correlating this outbound call across microservice boundaries.
 * @param spanId             Active execution span ID representing this specific outbound HTTP invocation.
 * @param parentSpanId       Span ID of the parent operation (e.g., incoming HTTP request or background task).
 * @param applicationName    Name of the application executing the outbound request.
 * @param targetService      Resolved logical service name or host name being targeted (e.g., "payment-service" or "api.stripe.com").
 * @param httpMethod         HTTP method used for the call (e.g., "GET", "POST", "PUT", "DELETE").
 * @param url                Full target URL including protocol, host, port, and path (e.g., "https://api.stripe.com/v1/charges").
 * @param uriTemplate        Parameterized URI template if available (e.g., "https://api.stripe.com/v1/charges/{id}"), avoiding metric explosion.
 * @param host               Target server hostname or IP address (e.g., "api.stripe.com").
 * @param port               Target server port (e.g., 443, 8080).
 * @param statusCode         HTTP response status code returned by the remote server (-1 if the call failed prior to receiving a response).
 * @param statusText         HTTP response status text (e.g., "200 OK", "404 Not Found", or null if network error occurred).
 * @param durationMs         Total execution time in milliseconds from request start to receiving the full response.
 * @param connectionTimeMs   Time spent establishing the TCP/TLS connection in milliseconds (-1 if unavailable/reused).
 * @param requestHeaders     Sanitized HTTP request headers sent to the remote service (sensitive headers like Authorization masked).
 * @param responseHeaders    HTTP response headers returned by the remote service.
 * @param requestBodySize    Size of the request body payload in bytes (-1 if unknown or streaming).
 * @param responseBodySize   Size of the response body payload in bytes (-1 if unknown or streaming).
 * @param clientType         HTTP client implementation used to execute the call (e.g., "RestTemplate", "WebClient", "ApacheHttpClient").
 * @param errorType          Fully qualified class name of any thrown exception during the call (e.g., "java.net.SocketTimeoutException"), or null.
 * @param errorMessage       Error message associated with a failed call attempt, or null if successful.
 * @param timestamp          Exact timestamp when the outbound request was initiated.
 */
public record ApplicationOutboundHttpRequestEvent(
        String id,
        String traceId,
        String spanId,
        String parentSpanId,
        String applicationName,
        String targetService,
        String httpMethod,
        String url,
        String uriTemplate,
        String host,
        int port,
        int statusCode,
        String statusText,
        long durationMs,
        long connectionTimeMs,
        Map<String, String> requestHeaders,
        Map<String, String> responseHeaders,
        long requestBodySize,
        long responseBodySize,
        String clientType,
        String errorType,
        String errorMessage,
        Instant timestamp
) implements KraftPulseEvent {

    public ApplicationOutboundHttpRequestEvent {
        requestHeaders = requestHeaders == null ? Map.of() : Map.copyOf(requestHeaders);
        responseHeaders = responseHeaders == null ? Map.of() : Map.copyOf(responseHeaders);
    }

    @Override
    public EventMetadata metadata() {
        return new EventMetadata(
                id,
                timestamp,
                applicationName,
                traceId,
                spanId
        );
    }

    /**
     * Helper check to determine if the outbound HTTP call resulted in a client or server error (4xx/5xx or network failure).
     */
    public boolean isError() {
        return statusCode >= 400 || statusCode == -1 || errorType != null;
    }

    /**
     * Factory method for quickly building an outbound request event.
     */
    public static ApplicationOutboundHttpRequestEvent create(
            String id,
            String traceId,
            String spanId,
            String parentSpanId,
            String applicationName,
            String targetService,
            String httpMethod,
            String url,
            String uriTemplate,
            String host,
            int port,
            int statusCode,
            String statusText,
            long durationMs,
            long connectionTimeMs,
            Map<String, String> requestHeaders,
            Map<String, String> responseHeaders,
            long requestBodySize,
            long responseBodySize,
            String clientType,
            String errorType,
            String errorMessage
    ) {
        return new ApplicationOutboundHttpRequestEvent(
                id, traceId, spanId, parentSpanId, applicationName, targetService,
                httpMethod, url, uriTemplate, host, port, statusCode, statusText,
                durationMs, connectionTimeMs, requestHeaders, responseHeaders,
                requestBodySize, responseBodySize, clientType, errorType, errorMessage,
                Instant.now()
        );
    }
}