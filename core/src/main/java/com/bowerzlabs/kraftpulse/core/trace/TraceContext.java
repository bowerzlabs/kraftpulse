package com.bowerzlabs.kraftpulse.core.trace;

public record TraceContext(
        String traceId,
        String spanId,
        boolean sampled
) {
    public boolean hasTrace() {
        return traceId != null && !traceId.isBlank();
    }

    public boolean hasSpan() {
        return spanId != null && !spanId.isBlank();
    }
}