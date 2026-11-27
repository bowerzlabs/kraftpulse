package com.bowerzlabs.kraftpulse.shared.store;

import com.bowerzlabs.kraftpulse.shared.model.*;
import com.bowerzlabs.kraftpulse.shared.response.PagedTelemetryResponse;
import com.bowerzlabs.kraftpulse.shared.response.outbound.OutboundHttpEventSummaryResponse;
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

    void saveScheduledJob(ApplicationJobScheduledJobEvent event);

    void saveOutbound(ApplicationOutboundHttpRequestEvent event);

    /** Saves a batch. Backends should override this with a single transaction. */
    default void saveAll(List<ApplicationOutboundHttpRequestEvent> events) {
        events.forEach(this::saveOutbound);
    }

    /** Calls made for one incoming request, oldest first. */
    List<ApplicationOutboundHttpRequestEvent> findByRequestId(String requestId);

    Optional<ApplicationOutboundHttpRequestEvent> findOutboundById(String id);

    /** Newest first. */
    List<OutboundHttpEventSummaryResponse> recent(int limit);

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

    void saveQuery(ApplicationQueryEvent event);

    default void saveQueries(List<ApplicationQueryEvent> events) {
        events.forEach(this::saveQuery);
    }

    default List<ApplicationQueryEvent> queriesByRequestId(String requestId) {
        return List.of();
    }

    void applyRetention(long maxAgeHours, long maxEvents);

    @Override
    void close();
}