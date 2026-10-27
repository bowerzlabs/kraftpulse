package com.bowerzlabs.kraftpulse.shared.response.overview;

import com.bowerzlabs.kraftpulse.shared.response.exceptions.ExceptionSummaryResponse;
import com.bowerzlabs.kraftpulse.shared.response.requests.InboundHttpEventSummaryResponse;

import java.util.List;

public record OverviewResponse(
        long totalRequestsLastHour,
        long totalRequestsLast24h,
        double errorRatePercent,
        double averageResponseTimeMs,
//        List<EndpointStat> slowestEndpoints,
        long recentExceptionCount,
        List<InboundHttpEventSummaryResponse> recentRequests,
        List<ExceptionSummaryResponse> recentExceptions
) {

}