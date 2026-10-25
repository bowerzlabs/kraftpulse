package com.bowerzlabs.kraftpulse.shared.response.overview;

import com.bowerzlabs.kraftpulse.shared.response.requests.InboundHttpEventSummaryResponse;

import java.util.List;

public record OverviewResponse(
        long totalRequestsLastHour,
        long totalRequestsLast24h,
        double errorRatePercent,
        double averageResponseTimeMs,
        List<EndpointStat> slowestEndpoints,
        long recentExceptionCount,
        List<RequestSummary> recentRequests,
        List<ExceptionSummary> recentExceptions
) {
    public record EndpointStat(
            String path,
            double averageDurationMs
    ) {}

    public record RequestSummary(
            String traceId,
            String method,
            String path,
            int statusCode,
            long durationMs,
            String timestamp
    ) {
        public static RequestSummary fromSummary(InboundHttpEventSummaryResponse event) {
            return new RequestSummary(
                    event.traceId(),
                    event.method() != null ? event.method().name() : "GET",
                    event.path(),
                    event.statusCode() != null ? event.statusCode() : 200,
                    event.durationMs(),
                    event.timestamp().toString()
            );
        }
    }

    public record ExceptionSummary(
            String traceId,
            String exceptionType,
            String message,
            String originClass,
            String timestamp
    ) {}
}