package com.bowerzlabs.kraftpulse.springboot.outbound;

import com.bowerzlabs.kraftpulse.shared.config.OutboundSettings;
import com.bowerzlabs.kraftpulse.shared.model.ApplicationOutboundHttpRequestEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

/**
 * Turns raw call data from any HTTP client into a sanitised event linked to the incoming request, and hands
 * it to the capture service. It never throws, so capture cannot break the application's own call.
 */
public class OutboundCallRecorder {

    private static final Logger log = LoggerFactory.getLogger(OutboundCallRecorder.class);

    private static final String MASK = "[MASKED]";   // same marker the exception capture uses
    private static final int MAX_ERROR_MESSAGE = 500;
    private static final Set<String> BUILT_IN_MASKED = Set.of(
            "authorization", "proxy-authorization", "cookie", "set-cookie",
            "x-api-key", "api-key", "x-auth-token", "x-csrf-token", "x-xsrf-token");

    /** Raw facts about one call. {@code context} must be resolved when the call STARTS. */
    public record CallData(
            String httpMethod,
            URI uri,
            int statusCode,
            String statusText,
            Instant startedAt,
            long durationMs,
            Map<String, String> requestHeaders,
            Map<String, String> responseHeaders,
            long requestBodySize,
            long responseBodySize,
            String clientType,
            Throwable error,
            CallContext context
    ) {
    }

    private final Consumer<ApplicationOutboundHttpRequestEvent> sink;
    private final OutboundSettings settings;
    private final String applicationName;
    private final Set<String> maskedNames = new HashSet<>(BUILT_IN_MASKED);
    private final Set<String> ignoredHosts = new HashSet<>();

    public OutboundCallRecorder(Consumer<ApplicationOutboundHttpRequestEvent> sink,
                                OutboundSettings settings,
                                String applicationName) {
        this.sink = Objects.requireNonNull(sink, "sink must not be null");
        this.settings = settings != null ? settings : OutboundSettings.defaults();
        this.applicationName = applicationName != null ? applicationName : "kraftpulse-app";
        this.settings.maskedHeaders().forEach(h -> maskedNames.add(h.toLowerCase(Locale.ROOT)));
        this.settings.ignoredHosts().forEach(h -> ignoredHosts.add(h.toLowerCase(Locale.ROOT)));
    }

    public void record(CallData d) {
        if (!settings.enabled()) {
            return;
        }
        try {
            URI uri = d.uri();
            String host = uri == null ? null : uri.getHost();
            if (host == null || ignoredHosts.contains(host.toLowerCase(Locale.ROOT))) {
                return;
            }

            CallContext ctx = d.context();
            String url = safeUrl(uri);

            sink.accept(new ApplicationOutboundHttpRequestEvent(
                    UUID.randomUUID().toString(),
                    ctx == null ? null : ctx.requestId(),
                    ctx != null && ctx.traceId() != null ? ctx.traceId() : newTraceId(),
                    newSpanId(),
                    ctx == null ? null : ctx.spanId(),
                    applicationName,
                    host,
                    d.httpMethod(),
                    url,
                    UriNormalizer.template(url),
                    host,
                    portOf(uri),
                    d.statusCode(),
                    d.statusText(),
                    d.durationMs(),
                    -1,
                    sanitize(d.requestHeaders()),
                    sanitize(d.responseHeaders()),
                    d.requestBodySize(),
                    d.responseBodySize(),
                    d.clientType(),
                    d.error() == null ? null : d.error().getClass().getName(),
                    d.error() == null ? null : truncate(d.error().getMessage(), MAX_ERROR_MESSAGE),
                    d.startedAt()));
        } catch (Exception e) {
            log.debug("KraftPulse could not record an outbound call", e);
        }
    }

    /** scheme://host[:port]/path: no user-info, and no query unless enabled. */
    private String safeUrl(URI uri) {
        StringBuilder sb = new StringBuilder();
        sb.append(uri.getScheme()).append("://").append(uri.getHost());
        if (uri.getPort() != -1) {
            sb.append(':').append(uri.getPort());
        }
        String path = uri.getRawPath();
        sb.append(path == null || path.isEmpty() ? "/" : path);
        if (settings.includeQuery() && uri.getRawQuery() != null) {
            sb.append('?').append(uri.getRawQuery());
        }
        return sb.toString();
    }

    private static int portOf(URI uri) {
        if (uri.getPort() != -1) {
            return uri.getPort();
        }
        return "https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
    }

    private Map<String, String> sanitize(Map<String, String> headers) {
        Map<String, String> result = new LinkedHashMap<>();
        if (headers == null) {
            return result;
        }
        for (Map.Entry<String, String> h : headers.entrySet()) {
            String name = h.getKey();
            if (name == null) {
                continue;
            }
            String value = h.getValue() == null ? "" : h.getValue();
            result.put(name, isSensitive(name) ? MASK : truncate(value, settings.maxHeaderValueLength()));
        }
        return result;
    }

    private boolean isSensitive(String name) {
        String n = name.toLowerCase(Locale.ROOT);
        return maskedNames.contains(n)
                || n.contains("token") || n.contains("secret") || n.contains("password") || n.contains("api-key");
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }

    /** 32 hex characters, never all zero. */
    private static String newTraceId() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        return String.format("%016x%016x", r.nextLong(), r.nextLong() | 1L);
    }

    /** 16 hex characters, never all zero. */
    private static String newSpanId() {
        return String.format("%016x", ThreadLocalRandom.current().nextLong() | 1L);
    }
}