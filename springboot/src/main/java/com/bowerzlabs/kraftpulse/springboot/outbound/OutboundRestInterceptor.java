package com.bowerzlabs.kraftpulse.springboot.outbound;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.time.Instant;

/**
 * Captures RestTemplate and RestClient calls (both accept this interceptor type).
 * Duration is measured until the response headers arrive. The body is never read.
 */
public class OutboundRestInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(OutboundRestInterceptor.class);

    private final OutboundCallRecorder recorder;
    private final OutboundContextResolver resolver;
    private final String clientType;

    public OutboundRestInterceptor(OutboundCallRecorder recorder, OutboundContextResolver resolver, String clientType) {
        this.recorder = recorder;
        this.resolver = resolver;
        this.clientType = clientType;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        CallContext context = resolver.resolve();
        log.debug("KraftPulse intercepting {} {} (requestId={})",
                request.getMethod(), request.getURI(), context.requestId());

        Instant startedAt = Instant.now();
        long startNanos = System.nanoTime();

        ClientHttpResponse response;
        try {
            response = execution.execute(request, body);
        } catch (IOException | RuntimeException ex) {
            recorder.record(new OutboundCallRecorder.CallData(
                    request.getMethod().name(), request.getURI(), -1, null, startedAt,
                    elapsedMs(startNanos), request.getHeaders().toSingleValueMap(), null,
                    body == null ? -1 : body.length, -1, clientType, ex, context));
            throw ex;
        }

        try {
            int status = response.getStatusCode().value();
            recorder.record(new OutboundCallRecorder.CallData(
                    request.getMethod().name(), request.getURI(), status, status + " " + response.getStatusText(),
                    startedAt, elapsedMs(startNanos), request.getHeaders().toSingleValueMap(),
                    response.getHeaders().toSingleValueMap(), body == null ? -1 : body.length,
                    response.getHeaders().getContentLength(), clientType, null, context));
        } catch (Exception e) {
            // capture must never affect the application's call, but do not hide why it failed
            log.warn("KraftPulse could not capture the response of {} {}: {}",
                    request.getMethod(), request.getURI(), e.toString());
        }
        return response;
    }

    private static long elapsedMs(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }
}