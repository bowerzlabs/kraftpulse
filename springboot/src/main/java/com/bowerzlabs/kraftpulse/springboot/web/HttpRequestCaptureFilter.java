package com.bowerzlabs.kraftpulse.springboot.web;

import com.bowerzlabs.kraftpulse.core.capture.KraftPulseCaptureService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class HttpRequestCaptureFilter extends HttpFilter {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final KraftPulseCaptureService captureService;
    private final String dashboardPath;

    public HttpRequestCaptureFilter(KraftPulseCaptureService captureService, String dashboardPath) {
        this.captureService = captureService;
        this.dashboardPath = dashboardPath != null ? dashboardPath : "/kraft/pulse";
    }

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String requestURI = request.getRequestURI();

        // 1. Skip telemetry capture for KraftPulse UI/API requests to avoid self-referential loops
        if (requestURI.startsWith(dashboardPath)) {
            chain.doFilter(request, response);
            return;
        }

        long startTime = System.currentTimeMillis();

        try {
            // 2. Pass request to consumer app
            chain.doFilter(request, response);
        } finally {
            long durationMs = System.currentTimeMillis() - startTime;
            int status = response.getStatus();

            logger.info("Captured HTTP request: {} {} -> {} ({}ms)", request.getMethod(), requestURI, status, durationMs);

            // 3. Dispatch event capture to background service
            if (captureService != null) {
                logger.info("Capturing inbound HTTP request");
                // captureService.captureInboundRequest(request, response, durationMs);
            }
        }
    }
}