package com.bowerzlabs.kraftpulse.core.trace;

import java.util.Optional;

public interface TraceProvider {

    TraceContext currentContext();

    TraceContext startTrace(String operationName);

    TraceContext startChildSpan(String operationName);

    void endTrace(TraceContext context);

    void endSpan(TraceContext context);
}