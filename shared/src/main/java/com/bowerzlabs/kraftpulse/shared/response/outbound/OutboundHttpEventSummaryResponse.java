package com.bowerzlabs.kraftpulse.shared.response.outbound;

import com.bowerzlabs.kraftpulse.shared.model.ApplicationOutboundHttpRequestEvent;

import java.time.Instant;

/** One row in an outbound-call list. No headers, and the path uses the template. */
public record OutboundHttpEventSummaryResponse(
        String id,
        String requestId,
        String traceId,
        String httpMethod,
        String targetService,
        String host,
        /** Normalised path such as /v1/charges/{id}. */
        String path,
        int statusCode,
        long durationMs,
        boolean error,
        String errorType,
        String clientType,
        Instant timestamp
) {

    public static OutboundHttpEventSummaryResponse from(ApplicationOutboundHttpRequestEvent e) {
        return new OutboundHttpEventSummaryResponse(
                e.id(), e.requestId(), e.traceId(), e.httpMethod(), e.targetService(), e.host(),
                pathOf(e.uriTemplate() != null ? e.uriTemplate() : e.url()),
                e.statusCode(), e.durationMs(), e.isError(), e.errorType(), e.clientType(), e.timestamp());
    }

    /** Path of an absolute URL or template, without java.net.URI (templates contain braces it rejects). */
    public static String pathOf(String urlOrTemplate) {
        if (urlOrTemplate == null || urlOrTemplate.isEmpty()) {
            return "";
        }
        String s = urlOrTemplate;
        int query = s.indexOf('?');
        if (query >= 0) {
            s = s.substring(0, query);
        }
        int scheme = s.indexOf("://");
        if (scheme < 0) {
            return s;
        }
        int slash = s.indexOf('/', scheme + 3);
        return slash < 0 ? "/" : s.substring(slash);
    }
}