
package com.bowerzlabs.kraftpulse.core.enrichment;

import com.bowerzlabs.kraftpulse.core.masking.MaskingEngine;
import com.bowerzlabs.kraftpulse.shared.enrichment.NPlusOneDetector;
import com.bowerzlabs.kraftpulse.shared.store.KraftPulseEventStore;

import java.util.Objects;
import java.util.logging.Logger;

public final class EnrichmentPipeline {

    private static final Logger LOG = Logger.getLogger(EnrichmentPipeline.class.getName());

    private final KraftPulseEventStore store;
    private final MaskingEngine maskingEngine;
    private final NPlusOneDetector nPlusOneDetector;

    public EnrichmentPipeline(
            KraftPulseEventStore store,
            MaskingEngine maskingEngine,
            NPlusOneDetector nPlusOneDetector
    ) {
        this.store = Objects.requireNonNull(store);
        this.maskingEngine = Objects.requireNonNull(maskingEngine);
        this.nPlusOneDetector =
                Objects.requireNonNull(nPlusOneDetector);
    }

//    public void onQueryCompleted(QueryEvent query) {
//        QueryEvent masked = query.withSql(
//                maskingEngine.mask(query.sql())
//        );
//
//        store.saveQuery(masked);
//    }
//
//    public void onRequestCompleted(RequestEvent request) {
//        List<QueryEvent> queries =
//                store.queriesByTraceId(request.traceId());
//
//        var warnings = nPlusOneDetector.detect(queries);
//
//        RequestEvent enriched =
//                request.withEnrichment(queries, warnings);
//
//        store.saveRequest(enriched);
//
//        if (!warnings.isEmpty()) {
//            log.debug(
//                    "N+1 patterns detected: traceId={}, count={}",
//                    request.traceId(),
//                    warnings.size()
//            );
//        }
//    }
//
//    public void onExceptionCaptured(ExceptionEvent event) {
//        // Ensure sensitive exception data is redacted before persistence.
//        // Add an event-level masking operation to your model/engine.
//        store.saveException(event);
//    }
//
//    public void onLifecycleCaptured(LifecycleEvent event) {
//        store.saveLifecycle(event);
//    }
//
//    public void onScheduledJobCompleted(ScheduledJobEvent event) {
//        store.saveScheduledJob(event);
//    }
//
//    public void onOutboundCallCompleted(OutboundCallEvent event) {
//        store.saveOutboundCall(event);
//    }


}
