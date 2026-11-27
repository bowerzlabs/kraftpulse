package com.bowerzlabs.kraftpulse.shared.config;

import java.util.List;

public record OutboundSettings(
        boolean enabled,
        boolean includeQuery,
        List<String> ignoredHosts,
        List<String> maskedHeaders,
        int maxHeaderValueLength,
        int queueCapacity,
        int batchSize,
        long flushIntervalMs
) {
    public OutboundSettings {
        ignoredHosts = ignoredHosts == null ? List.of() : List.copyOf(ignoredHosts);
        maskedHeaders = maskedHeaders == null ? List.of() : List.copyOf(maskedHeaders);
    }

    public static OutboundSettings defaults() {
        return new OutboundSettings(true, false, List.of(), List.of(), 256, 10_000, 200, 500L);
    }
}