package com.bowerzlabs.kraftpulse.shared.store;

import com.bowerzlabs.kraftpulse.shared.model.*;
import com.bowerzlabs.kraftpulse.shared.response.PagedTelemetryResponse;
import com.bowerzlabs.kraftpulse.shared.response.requests.InboundHttpEventDetailResponse;
import com.bowerzlabs.kraftpulse.shared.response.requests.InboundHttpEventSummaryResponse;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface KraftPulseEventStore extends AutoCloseable {

    void saveRequest(ApplicationInboundHttpRequestEvent event);

    PagedTelemetryResponse<InboundHttpEventSummaryResponse> recentRequests(Instant since, int limit);

    Optional<InboundHttpEventDetailResponse> requestByEventId(String eventId);

    void saveException(ApplicationExceptionEvent event);

    void saveLifecycle(ApplicationLifecycleEvent event);

    void saveOutboundCall(ApplicationOutboundHttpRequestEvent event);

    void saveScheduledJob(ApplicationJobScheduledJobEvent event);

//    List<RequestEvent> requestsByTraceId(String traceId);
//
//    List<QueryEvent> queriesByTraceId(String traceId);
//
//    List<OutboundCallEvent> outboundCallsByTraceId(String traceId);
//
//    List<ExceptionEvent> exceptionsByTraceId(String traceId);
//
//    List<ExceptionEvent> recentExceptions(Instant since, int limit);
//
//    List<ScheduledJobEvent> recentScheduledJobs(
//            Instant since,
//            int limit
//    );
//
//    List<ScheduledJobEvent> recentScheduledJobs(int limit);

    void applyRetention(long maxAgeHours, long maxEvents);

    @Override
    void close();
}