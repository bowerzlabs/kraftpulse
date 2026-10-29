package com.bowerzlabs.kraftpulse.springboot.exception;

import com.bowerzlabs.kraftpulse.core.capture.KraftPulseCaptureService;
import com.bowerzlabs.kraftpulse.core.trace.TraceContext;
import com.bowerzlabs.kraftpulse.shared.model.ApplicationExceptionEvent;
import com.bowerzlabs.kraftpulse.springboot.request.HttpRequestCaptureFilter;
import com.bowerzlabs.kraftpulse.springboot.telemetry.KraftPulseTelemetry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

public class KraftPulseExceptionCaptureResolver implements HandlerExceptionResolver, Ordered {

    private static final Logger log = LoggerFactory.getLogger(KraftPulseExceptionCaptureResolver.class);

    private final KraftPulseCaptureService captureService;
    private final KraftPulseTelemetry telemetry;
    private final String applicationName;

    public KraftPulseExceptionCaptureResolver(
            KraftPulseCaptureService captureService,
            @Nullable KraftPulseTelemetry telemetry,
            String applicationName
    ) {
        this.captureService = captureService;
        this.telemetry = telemetry;
        this.applicationName = applicationName != null ? applicationName : "kraftpulse-app";
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public @Nullable ModelAndView resolveException(
            HttpServletRequest request,
            HttpServletResponse response,
            @Nullable Object handler,
            Exception ex
    ) {
        if (ex == null || captureService == null) {
            return null;
        }

        String uri = request.getRequestURI();
        if (uri != null && (uri.contains("/kraft/pulse") || uri.contains("/kraftpulse"))) {
            return null;
        }

        String requestId = (String) request.getAttribute(HttpRequestCaptureFilter.INBOUND_REQUEST_ID);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
            request.setAttribute(HttpRequestCaptureFilter.INBOUND_REQUEST_ID, requestId);
        }

        String traceId = (String) request.getAttribute(HttpRequestCaptureFilter.TRACE_ID_ATTRIBUTE);
        String spanId = (String) request.getAttribute(HttpRequestCaptureFilter.SPAN_ID_ATTRIBUTE);

        if ((!isValidTrace(traceId, 32, "0{32}") || !isValidTrace(spanId, 16, "0{16}")) && telemetry != null) {
            try {
                TraceContext ctx = telemetry.currentContext();
                if (ctx != null) {
                    if (!isValidTrace(traceId, 32, "0{32}") && isValidTrace(ctx.traceId(), 32, "0{32}")) {
                        traceId = ctx.traceId();
                    }
                    if (!isValidTrace(spanId, 16, "0{16}") && isValidTrace(ctx.spanId(), 16, "0{16}")) {
                        spanId = ctx.spanId();
                    }
                }
            } catch (RuntimeException ignored) {}
        }

        if (!isValidTrace(traceId, 32, "0{32}")) traceId = UUID.randomUUID().toString().replace("-", "");
        if (!isValidTrace(spanId, 16, "0{16}")) spanId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        request.setAttribute(HttpRequestCaptureFilter.TRACE_ID_ATTRIBUTE, traceId);
        request.setAttribute(HttpRequestCaptureFilter.SPAN_ID_ATTRIBUTE, spanId);

        StackTraceElement top = ex.getStackTrace().length > 0 ? ex.getStackTrace()[0] : null;
        String originClass = top != null ? top.getClassName() : "UnknownClass";
        String originMethod = top != null ? top.getMethodName() : "UnknownMethod";
        int line = top != null ? top.getLineNumber() : -1;

        Throwable root = getRootCause(ex);
        String exType = ex.getClass().getName();

        // Compute unique exception instance ID to prevent database collisions between requests
        String exceptionId = UUID.randomUUID().toString();

        ApplicationExceptionEvent event = ApplicationExceptionEvent.createNew(
                exceptionId, requestId, traceId, spanId, applicationName,
                exType, ex.getClass().getSimpleName(), ex.getMessage(),
                root.getClass().getName(), root.getMessage(),
                originClass, originMethod, line, formatStackTrace(ex),
                request.getMethod(), uri, request.getQueryString(),
                extractHandlerName(handler), extractClientIp(request), extractHeaders(request)
        );

        try {
            captureService.captureException(event);
        } catch (RuntimeException e) {
            log.warn("Failed to capture exception telemetry for requestId={}", requestId, e);
        }

        return null;
    }

    private boolean isValidTrace(String val, int len, String zeros) {
        return val != null && val.matches("[0-9a-fA-F]{" + len + "}") && !val.matches(zeros);
    }

    private Throwable getRootCause(Throwable t) {
        Throwable cause = t;
        while (cause.getCause() != null && cause.getCause() != cause) cause = cause.getCause();
        return cause;
    }

    private String formatStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        try (PrintWriter pw = new PrintWriter(sw)) { t.printStackTrace(pw); }
        return sw.toString();
    }

    private @Nullable String extractHandlerName(@Nullable Object handler) {
        if (handler instanceof HandlerMethod hm) {
            return hm.getBeanType().getSimpleName() + "#" + hm.getMethod().getName();
        }
        return handler != null ? handler.toString() : null;
    }

    private String extractClientIp(HttpServletRequest req) {
        String fwd = req.getHeader("X-Forwarded-For");
        return (fwd != null && !fwd.isBlank()) ? fwd.split(",")[0].trim() : req.getRemoteAddr();
    }

    private Map<String, String> extractHeaders(HttpServletRequest req) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> names = req.getHeaderNames();
        if (names != null) {
            while (names.hasMoreElements()) {
                String name = names.nextElement();
                String lower = name.toLowerCase(Locale.ROOT);
                if (lower.contains("authorization") || lower.contains("cookie") || lower.contains("set-cookie")) {
                    headers.put(name, "[MASKED]");
                } else {
                    headers.put(name, req.getHeader(name));
                }
            }
        }
        return Collections.unmodifiableMap(headers);
    }
}