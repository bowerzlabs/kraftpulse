package com.bowerzlabs.kraftpulse.springboot.web.requests;

import com.bowerzlabs.kraftpulse.shared.response.requests.InboundHttpEventDetailResponse;
import com.bowerzlabs.kraftpulse.shared.store.KraftPulseEventStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/kraft/pulse/api/requests")
public class KraftPulseInboundRequestsController {

    private final KraftPulseEventStore kraftPulseEventStore;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public KraftPulseInboundRequestsController(KraftPulseEventStore kraftPulseEventStore) {
        this.kraftPulseEventStore = kraftPulseEventStore;
    }

    // request details
    @GetMapping("/{eventId}")
    public InboundHttpEventDetailResponse requestDetails(@PathVariable("eventId") String eventId) {
        logger.info("getting inbound request details for eventId {} and response {}", eventId, kraftPulseEventStore.requestByEventId(eventId));
        return kraftPulseEventStore.requestByEventId(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Request trace not found for ID: " + eventId));
    }

}
