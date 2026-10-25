package com.bowerzlabs.kraftpulse.springboot.request;

import com.bowerzlabs.kraftpulse.core.capture.KraftPulseCaptureService;
import com.bowerzlabs.kraftpulse.shared.model.ApplicationInboundHttpRequestEvent;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.Instant;

public class HttpRequestCaptureFilter extends HttpFilter {

    private final KraftPulseCaptureService captureService;
    private final InboundHttpRequestEventBuilder eventBuilder;
    private final String dashboardPath;

    public HttpRequestCaptureFilter(
            KraftPulseCaptureService captureService,
            InboundHttpRequestEventBuilder eventBuilder,
            String dashboardPath
    ) {
        this.captureService = captureService;
        this.eventBuilder = eventBuilder;
        this.dashboardPath = dashboardPath != null ? dashboardPath : "/kraft/pulse";
    }

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (request.getRequestURI().startsWith(dashboardPath)) {
            chain.doFilter(request, response);
            return;
        }

        Instant startedAt = Instant.now();
        long startNanos = System.nanoTime();
        Throwable unhandledException = null;

        try {
            chain.doFilter(request, response);
        } catch (Throwable t) {
            unhandledException = t;
            throw t;
        } finally {
            long durationNanos = System.nanoTime() - startNanos;

            if (captureService != null) {
                ApplicationInboundHttpRequestEvent event = eventBuilder.build(
                        request,
                        response,
                        startedAt,
                        durationNanos,
                        unhandledException
                );
                captureService.captureInboundRequest(event);
            }
        }
    }
}