package com.bowerzlabs.kraftpulse.shared.model;

import com.bowerzlabs.kraftpulse.shared.constants.HttpMethod;

import java.time.Instant;
import java.util.List;

public record ApplicationInboundHttpRequestEvent(
        EventMetadata metadata,
        HttpMethod method,
        String path,
        Integer statusCode,
        long durationNanos,
        Instant startedAt,
        String controllerClass,
        String handlerMethod,
        String authenticatedUser,
        List<String> roles,
        String clientIp
//        List<QueryEvent> queries,
//        List<ExceptionEvent> exceptions,
//        List<NPlusOneWarning> nPlusOneWarnings
) implements KraftPulseEvent {

    public ApplicationInboundHttpRequestEvent {
        roles = roles == null ? List.of() : List.copyOf(roles);
//        queries = queries == null ? List.of() : List.copyOf(queries);
//        exceptions = exceptions == null ? List.of() : List.copyOf(exceptions);
//        nPlusOneWarnings = nPlusOneWarnings == null
//                ? List.of()
//                : List.copyOf(nPlusOneWarnings);
    }
}