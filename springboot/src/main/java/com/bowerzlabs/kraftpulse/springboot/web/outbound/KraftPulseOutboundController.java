package com.bowerzlabs.kraftpulse.springboot.web.outbound;

import com.bowerzlabs.kraftpulse.shared.response.outbound.OutboundHttpEventDetailResponse;
import com.bowerzlabs.kraftpulse.shared.response.outbound.OutboundHttpEventSummaryResponse;
import com.bowerzlabs.kraftpulse.shared.response.outbound.RequestOutboundCallsResponse;
import com.bowerzlabs.kraftpulse.shared.store.KraftPulseEventStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/kraft/pulse/api")
public class KraftPulseOutboundController {

    private final KraftPulseEventStore store;

    public KraftPulseOutboundController(KraftPulseEventStore store) {
        this.store = store;
    }

    /** Newest outbound calls across all requests. */
    @GetMapping("/outbound")
    public List<OutboundHttpEventSummaryResponse> recent(@RequestParam(defaultValue = "50") int limit) {
        return store.recent(limit);
    }

    @GetMapping("/outbound/{id}")
    public ResponseEntity<OutboundHttpEventDetailResponse> details(@PathVariable String id) {
        return store.findOutboundById(id)
                .map(OutboundHttpEventDetailResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** Outbound calls for one request. The id is the request's eventId (which is also its request id). */
    @GetMapping("/requests/{requestId}/outbound")
    public RequestOutboundCallsResponse forRequest(@PathVariable String requestId) {
        return RequestOutboundCallsResponse.of(
                store.findByRequestId(requestId).stream()
                        .map(OutboundHttpEventSummaryResponse::from)
                        .toList());
    }
}