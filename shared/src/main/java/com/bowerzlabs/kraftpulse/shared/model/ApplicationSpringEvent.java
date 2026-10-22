package com.bowerzlabs.kraftpulse.shared.model;

import java.time.Instant;
import java.util.List;

/**
 * Represents an internal Spring ApplicationEvent published and handled within the JVM.
 * Tracks publishers, listeners (@EventListener, @TransactionalEventListener), and processing status.
 *
 * @param id                 Unique event publication identifier.
 * @param traceId            Global W3C trace ID originating from the publisher context.
 * @param spanId             Active execution span ID representing this event publication lifecycle.
 * @param applicationName    Name of the application publishing the event.
 * @param eventType          Fully qualified class name of the published event (e.g., "com.app.event.OrderCreatedEvent").
 * @param eventSimpleName    Short class name of the event (e.g., "OrderCreatedEvent").
 * @param publisherClass     Class name of the component that invoked publishEvent() (e.g., "OrderService").
 * @param isAsync            True if one or more listeners processed this event asynchronously (@Async).
 * @param transactionPhase   Spring TransactionalEventListener phase if applicable (e.g., "AFTER_COMMIT", "AFTER_ROLLBACK", "NONE").
 * @param listenerClasses    List of listener components/methods registered to receive this event.
 * @param listenerCount      Total number of matching listeners configured for this event type.
 * @param totalDurationMs    Combined execution duration across all synchronous listeners in milliseconds.
 * @param status             Overall status (e.g., "PUBLISHED", "COMPLETED", "PARTIAL_FAILURE", "FAILED").
 * @param errorType          Fully qualified class name of an unhandled exception thrown during listener execution, or null.
 * @param errorMessage       Detailed error message if listener processing failed, or null.
 * @param timestamp          Timestamp when the event was published.
 */
public record ApplicationSpringEvent(
        String id,
        String traceId,
        String spanId,
        String applicationName,
        String eventType,
        String eventSimpleName,
        String publisherClass,
        boolean isAsync,
        String transactionPhase,
        List<String> listenerClasses,
        int listenerCount,
        long totalDurationMs,
        String status,
        String errorType,
        String errorMessage,
        Instant timestamp
) implements KraftPulseEvent {

    public ApplicationSpringEvent {
        listenerClasses = listenerClasses == null ? List.of() : List.copyOf(listenerClasses);
    }

    @Override
    public EventMetadata metadata() {
        return new EventMetadata(
                id,
                timestamp,
                applicationName,
                traceId,
                spanId
        );
    }

    public static ApplicationSpringEvent create(
            String id, String traceId, String spanId, String applicationName,
            String eventType, String eventSimpleName, String publisherClass,
            boolean isAsync, String transactionPhase, List<String> listenerClasses,
            long totalDurationMs, String status, String errorType, String errorMessage
    ) {
        return new ApplicationSpringEvent(
                id, traceId, spanId, applicationName, eventType, eventSimpleName,
                publisherClass, isAsync, transactionPhase, listenerClasses,
                listenerClasses != null ? listenerClasses.size() : 0,
                totalDurationMs, status, errorType, errorMessage, Instant.now()
        );
    }
}