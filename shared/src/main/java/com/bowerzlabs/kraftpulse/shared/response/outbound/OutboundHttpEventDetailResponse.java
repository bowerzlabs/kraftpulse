package com.bowerzlabs.kraftpulse.shared.response.outbound;

import com.bowerzlabs.kraftpulse.shared.model.ApplicationOutboundHttpRequestEvent;

import java.time.Instant;
import java.util.Map;

/** Everything captured about one outbound call. */
public record OutboundHttpEventDetailResponse(
        String id,
        String requestId,
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
        boolean error,
        long durationMs,
        /** -1 when unavailable. */
        long connectionTimeMs,
        Map<String, String> requestHeaders,
        Map<String, String> responseHeaders,
        /** -1 when unknown. */
        long requestBodySize,
        long responseBodySize,
        String clientType,
        String errorType,
        String errorMessage,
        Instant timestamp
) {

    public OutboundHttpEventDetailResponse {
        requestHeaders = requestHeaders == null ? Map.of() : Map.copyOf(requestHeaders);
        responseHeaders = responseHeaders == null ? Map.of() : Map.copyOf(responseHeaders);
    }

    public static OutboundHttpEventDetailResponse from(ApplicationOutboundHttpRequestEvent e) {
        return new OutboundHttpEventDetailResponse(
                e.id(), e.requestId(), e.traceId(), e.spanId(), e.parentSpanId(),
                e.applicationName(), e.targetService(), e.httpMethod(), e.url(), e.uriTemplate(),
                e.host(), e.port(), e.statusCode(), e.statusText(), e.isError(),
                e.durationMs(), e.connectionTimeMs(), e.requestHeaders(), e.responseHeaders(),
                e.requestBodySize(), e.responseBodySize(), e.clientType(),
                e.errorType(), e.errorMessage(), e.timestamp());
    }
}