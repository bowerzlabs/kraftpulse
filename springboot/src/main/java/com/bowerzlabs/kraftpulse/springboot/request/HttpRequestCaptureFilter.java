
package com.bowerzlabs.kraftpulse.springboot.request;

import com.bowerzlabs.kraftpulse.core.capture.KraftPulseCaptureService;
import com.bowerzlabs.kraftpulse.core.trace.TraceContext;
import com.bowerzlabs.kraftpulse.core.trace.TraceProvider;
import com.bowerzlabs.kraftpulse.shared.model.ApplicationInboundHttpRequestEvent;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

public class HttpRequestCaptureFilter extends HttpFilter {

    private static final Logger log =
            LoggerFactory.getLogger(HttpRequestCaptureFilter.class);

    public static final String TRACE_ID_ATTRIBUTE = "kraftpulse.traceId";
    public static final String SPAN_ID_ATTRIBUTE = "kraftpulse.spanId";
    public static final String INBOUND_REQUEST_ID = "kraftpulse.requestId";

    private final KraftPulseCaptureService captureService;
    private final InboundHttpRequestEventBuilder eventBuilder;
    private final TraceProvider traceProvider;
    private final String cleanDashboardPath;
    private final String applicationName;

    public HttpRequestCaptureFilter(
            KraftPulseCaptureService captureService,
            InboundHttpRequestEventBuilder eventBuilder,
            TraceProvider traceProvider,
            String dashboardPath,
            String applicationName
    ) {
        this.captureService = captureService;
        this.eventBuilder = eventBuilder;
        this.traceProvider = traceProvider;
        this.applicationName = applicationName != null
                ? applicationName
                : "kraftpulse-app";

        String path = dashboardPath != null ? dashboardPath : "/kraft/pulse";
        if (path.endsWith("/**")) {
            path = path.substring(0, path.length() - 3);
        } else if (path.endsWith("/*")) {
            path = path.substring(0, path.length() - 2);
        }
        this.cleanDashboardPath = path;
    }

    @Override
    protected void doFilter(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {

        if (isKraftPulseEndpoint(request.getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }

        String requestId = UUID.randomUUID().toString();
        TraceIds traceIds = resolveTraceIds();

        request.setAttribute(INBOUND_REQUEST_ID, requestId);
        request.setAttribute(TRACE_ID_ATTRIBUTE, traceIds.traceId());
        request.setAttribute(SPAN_ID_ATTRIBUTE, traceIds.spanId());

        Instant startedAt = Instant.now();
        long startNanos = System.nanoTime();
        Throwable unhandledException = null;

        try {
            chain.doFilter(request, response);
        } catch (IOException | ServletException | RuntimeException | Error ex) {
            unhandledException = ex;
            throw ex;
        } finally {
            if (captureService != null) {
                try {
                    long durationNanos = System.nanoTime() - startNanos;

                    ApplicationInboundHttpRequestEvent event = eventBuilder.build(
                            request,
                            response,
                            startedAt,
                            durationNanos,
                            unhandledException
                    );
                    captureService.captureInboundRequest(event);
                } catch (RuntimeException ex) {
                    log.warn(
                            "Failed to capture inbound request telemetry for requestId={}",
                            requestId,
                            ex
                    );
                }
            }
        }
    }

    private TraceIds resolveTraceIds() {
        if (traceProvider != null) {
            try {
                TraceContext context = traceProvider.currentContext();

                if (context != null
                        && isValidTraceId(context.traceId())
                        && isValidSpanId(context.spanId())) {
                    return new TraceIds(context.traceId(), context.spanId());
                }
            } catch (RuntimeException ex) {
                log.debug("Unable to resolve active trace context; generating fallback IDs", ex);
            }
        }

        return new TraceIds(
                UUID.randomUUID().toString().replace("-", ""),
                UUID.randomUUID().toString().replace("-", "").substring(0, 16)
        );
    }

    private boolean isValidTraceId(String value) {
        return value != null
                && value.matches("[0-9a-fA-F]{32}")
                && !value.matches("0{32}");
    }

    private boolean isValidSpanId(String value) {
        return value != null
                && value.matches("[0-9a-fA-F]{16}")
                && !value.matches("0{16}");
    }

    private boolean isKraftPulseEndpoint(String uri) {
        return uri.startsWith(cleanDashboardPath)
                || uri.contains("/kraft/pulse/api")
                || uri.contains("/kraftpulse/api");
    }

    private record TraceIds(String traceId, String spanId) {}
}
