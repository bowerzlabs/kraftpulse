package com.bowerzlabs.kraftpulse.core.trace;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;

import java.util.Optional;

public final class TraceResolver {

    private static final String SCOPE_NAME = "com.bowerzlabs.kraftpulse";

    private final Tracer tracer;

    public TraceResolver(OpenTelemetry openTelemetry) {
        this.tracer = openTelemetry.getTracer(SCOPE_NAME);
    }

    public Tracer tracer() {
        return tracer;
    }

    public Optional<String> currentTraceId() {
        SpanContext spanContext = Span.current().getSpanContext();

        if (!spanContext.isValid()) {
            return Optional.empty();
        }

        return Optional.of(spanContext.getTraceId());
    }

    public Optional<String> currentSpanId() {
        SpanContext spanContext = Span.current().getSpanContext();

        if (!spanContext.isValid()) {
            return Optional.empty();
        }

        return Optional.of(spanContext.getSpanId());
    }

    public Context currentContext() {
        return Context.current();
    }
}