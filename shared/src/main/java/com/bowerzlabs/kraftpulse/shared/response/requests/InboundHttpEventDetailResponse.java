package com.bowerzlabs.kraftpulse.shared.response.requests;

import com.bowerzlabs.kraftpulse.shared.constants.HttpMethod;
import com.bowerzlabs.kraftpulse.shared.model.ApplicationInboundHttpRequestEvent;
import com.bowerzlabs.kraftpulse.shared.response.exceptions.ExceptionSummaryResponse;
import com.bowerzlabs.kraftpulse.shared.response.security.SecurityInfo;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record InboundHttpEventDetailResponse(
        String traceId,
        String spanId,
        HttpMethod method,
        String path,
        String route,
        Integer statusCode,
        long durationMs,
        Instant startedAt,
        Instant completedAt,
        ClientInfo client,
        SecurityInfo security,
        ControllerInfo controller,
        List<WaterfallNode> waterfall,
        DatabaseSummary database,
        List<OutboundCallResponse> outboundCalls,
        RequestErrorResponse error,
        List<ExceptionSummaryResponse> exceptions, // Added exceptions summary list
        PerformanceSummary performance,
        Map<String, String> attributes
) {
    public static InboundHttpEventDetailResponse fromEvent(
            ApplicationInboundHttpRequestEvent event,
            List<ExceptionSummaryResponse> exceptions
    ) {
        Instant startedAt = event.startedAt() != null ? event.startedAt() : event.metadata().timestamp();
        Instant completedAt = startedAt.plusMillis(event.durationMs());

        ClientInfo client = new ClientInfo(
                event.clientIp(),
                event.userAgent(),
                null,
                null
        );

        SecurityInfo security = new SecurityInfo(
                event.authenticatedUser() != null && !event.authenticatedUser().isBlank(),
                event.authenticatedUser(),
                event.roles() != null ? event.roles() : List.of(),
                null
        );

        ControllerInfo controller = new ControllerInfo(
                event.controllerClass(),
                event.handlerMethod()
        );

        PerformanceSummary performance = new PerformanceSummary(
                event.durationMs(),
                event.queryDurationMs(),
                0L,
                0L,
                Math.max(0, event.durationMs() - event.queryDurationMs()),
                event.durationMs(),
                List.of("HTTP Inbound", event.controllerClass() != null ? event.controllerClass() : "Controller"),
                event.durationMs() > 1000
        );

        DatabaseSummary database = new DatabaseSummary(
                event.queryCount(),
                event.queryDurationMs(),
                0,
                List.of()
        );

        RequestErrorResponse error = event.hasException() && !exceptions.isEmpty() ? new RequestErrorResponse(
                exceptions.get(0).exceptionType(),
                exceptions.get(0).message(),
                null,
                event.controllerClass(),
                event.handlerMethod(),
                (int) exceptions.get(0).occurrenceCount(),
                exceptions.get(0).lastSeen().toString(),
                exceptions.get(0).lastSeen().toString()
        ) : null;

        List<WaterfallNode> waterfall = List.of(
                new WaterfallNode(
                        event.metadata().spanId() != null ? event.metadata().spanId() : "span-1",
                        null,
                        "HTTP",
                        event.path(),
                        event.durationMs(),
                        0,
                        Map.of(),
                        List.of()
                )
        );

        return new InboundHttpEventDetailResponse(
                event.metadata().traceId(),
                event.metadata().spanId(),
                event.method(),
                event.path(),
                event.matchingPattern(),
                event.statusCode(),
                event.durationMs(),
                startedAt,
                completedAt,
                client,
                security,
                controller,
                waterfall,
                database,
                List.of(),
                error,
                exceptions,
                performance,
                Map.of()
        );
    }
}