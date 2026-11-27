package com.bowerzlabs.kraftpulse.springboot.outbound;

import com.bowerzlabs.kraftpulse.shared.context.RequestScope;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * Captures WebClient calls. The request identity is read when the exchange is subscribed to, which is the calling
 * thread for blocking use inside a servlet request. Fully reactive chains that switch threads before subscribing
 * need context propagation to keep the request id.
 */
public class OutboundWebClientFilter implements ExchangeFilterFunction {

    private final OutboundCallRecorder recorder;
    private final OutboundContextResolver resolver;

    public OutboundWebClientFilter(OutboundCallRecorder recorder, OutboundContextResolver resolver) {
        this.recorder = recorder;
        this.resolver = resolver;
    }

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        return Mono.defer(() -> {
            CallContext context = resolveWithRequestScope(resolver);
            Instant startedAt = Instant.now();
            long startNanos = System.nanoTime();

            return next.exchange(request)
                    .doOnNext(response -> {
                        int status = response.statusCode().value();
                        HttpStatus resolved = HttpStatus.resolve(status);
                        recorder.record(new OutboundCallRecorder.CallData(
                                request.method().name(), request.url(), status,
                                resolved == null ? String.valueOf(status) : status + " " + resolved.getReasonPhrase(),
                                startedAt, elapsedMs(startNanos),
                                request.headers().toSingleValueMap(),
                                response.headers().asHttpHeaders().toSingleValueMap(),
                                request.headers().getContentLength(),
                                response.headers().contentLength().orElse(-1),
                                "WebClient", null, context));
                    })
                    .doOnError(error -> recorder.record(new OutboundCallRecorder.CallData(
                            request.method().name(), request.url(), -1, null, startedAt,
                            elapsedMs(startNanos), request.headers().toSingleValueMap(), null,
                            request.headers().getContentLength(), -1, "WebClient", error, context)));
        });
    }

    private static CallContext resolveWithRequestScope(OutboundContextResolver resolver) {
        CallContext ctx = resolver == null ? null : resolver.resolve();
        String scopedRequestId = RequestScope.current();
        if (scopedRequestId == null || scopedRequestId.isBlank()) {
            return ctx;
        }
        if (ctx == null) {
            return new CallContext(scopedRequestId, null, null);
        }
        if (ctx.requestId() == null || ctx.requestId().isBlank()) {
            return new CallContext(scopedRequestId, ctx.traceId(), ctx.spanId());
        }
        return ctx;
    }

    private static long elapsedMs(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }
}