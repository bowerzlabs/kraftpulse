package com.bowerzlabs.kraftpulse.shared.response.outbound;

import java.util.Comparator;
import java.util.List;

/** Outbound calls made while handling one request, with the numbers needed to spot a slow dependency. */
public record RequestOutboundCallsResponse(
        int count,
        int errorCount,
        long totalDurationMs,
        OutboundHttpEventSummaryResponse slowest,
        List<OutboundHttpEventSummaryResponse> calls
) {

    public RequestOutboundCallsResponse {
        calls = calls == null ? List.of() : List.copyOf(calls);
    }

    public static RequestOutboundCallsResponse of(List<OutboundHttpEventSummaryResponse> calls) {
        List<OutboundHttpEventSummaryResponse> list = calls == null ? List.of() : calls;
        return new RequestOutboundCallsResponse(
                list.size(),
                (int) list.stream().filter(OutboundHttpEventSummaryResponse::error).count(),
                list.stream().mapToLong(OutboundHttpEventSummaryResponse::durationMs).sum(),
                list.stream().max(Comparator.comparingLong(OutboundHttpEventSummaryResponse::durationMs)).orElse(null),
                list);
    }
}