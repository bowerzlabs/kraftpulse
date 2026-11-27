package com.bowerzlabs.kraftpulse.shared.response.requests;

public record OutboundCallResponse(String spanId, String method, String url, Integer statusCode, long durationMs, Long startOffsetMs, String errorMessage) {}
