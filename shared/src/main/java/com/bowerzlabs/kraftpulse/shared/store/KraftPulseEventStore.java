package com.bowerzlabs.kraftpulse.shared.store;

import com.bowerzlabs.kraftpulse.shared.model.ApplicationInboundHttpRequestEvent;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface KraftPulseEventStore extends AutoCloseable {

    void saveRequest(ApplicationInboundHttpRequestEvent event);

    List<ApplicationInboundHttpRequestEvent> recentRequests(String applicationName, Instant since, int limit);

    Optional<ApplicationInboundHttpRequestEvent> requestByEventId(String eventId);

    @Override
    void close();
}



//package com.bowerzlabs.kraftpulse.common.store;
//
//import com.bowerzlabs.kraftpulse.common.model.ExceptionEvent;
//import com.bowerzlabs.kraftpulse.common.model.LifecycleEvent;
//import com.bowerzlabs.kraftpulse.common.model.OutboundCallEvent;
//import com.bowerzlabs.kraftpulse.common.model.QueryEvent;
//import com.bowerzlabs.kraftpulse.common.model.RequestEvent;
//import com.bowerzlabs.kraftpulse.common.model.ScheduledJobEvent;
//
//import java.time.Instant;
//import java.util.List;
//
//public interface EventStore extends AutoCloseable {
//
//    void saveRequest(RequestEvent event);
//
//    void saveQuery(QueryEvent event);
//
//    void saveException(ExceptionEvent event);
//
//    void saveLifecycle(LifecycleEvent event);
//
//    void saveOutboundCall(OutboundCallEvent event);
//
//    void saveScheduledJob(ScheduledJobEvent event);
//
//    List<RequestEvent> recentRequests(Instant since, int limit);
//
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
//
//    void applyRetention(long maxAgeHours, long maxEvents);
//
//    @Override
//    void close();
//}
