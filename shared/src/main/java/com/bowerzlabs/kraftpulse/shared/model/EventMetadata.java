package com.bowerzlabs.kraftpulse.shared.model;

import java.time.Instant;

public record EventMetadata(
        String eventId,
        Instant timestamp,
        String applicationName,
        String traceId,
        String spanId
) {}