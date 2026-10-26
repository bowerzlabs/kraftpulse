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
    private final String cleanDashboardPath;

    public HttpRequestCaptureFilter(
            KraftPulseCaptureService captureService,
            InboundHttpRequestEventBuilder eventBuilder,
            String dashboardPath
    ) {
        this.captureService = captureService;
        this.eventBuilder = eventBuilder;

        String path = dashboardPath != null ? dashboardPath : "/kraft/pulse";
        if (path.endsWith("/**")) {
            path = path.substring(0, path.length() - 3);
        } else if (path.endsWith("/*")) {
            path = path.substring(0, path.length() - 2);
        }
        this.cleanDashboardPath = path;
    }

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String requestURI = request.getRequestURI();

        // Bypass capture for dashboard UI, static assets, and all internal API endpoints
        if (requestURI.startsWith(cleanDashboardPath) || requestURI.contains("/kraft/pulse/api") || requestURI.contains("/kraftpulse/api")) {
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