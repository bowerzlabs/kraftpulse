package com.bowerzlabs.kraftpulse.shared.model;

import java.time.Instant;

/**
 * Represents a cache execution event captured within the KraftPulse diagnostic pipeline.
 * Supports Spring Cache abstractions, Redis, Caffeine, EHCache, and Memcached.
 *
 * @param id              Unique event identifier.
 * @param traceId         Global W3C trace ID correlating this cache operation with the parent execution context.
 * @param spanId          Active execution span ID representing this specific cache invocation.
 * @param applicationName Name of the application executing the cache operation.
 * @param cacheSystem     Cache provider implementation (e.g., "REDIS", "CAFFEINE", "EHCACHE", "HAZELCAST").
 * @param cacheName       Logical cache region or namespace (e.g., "users", "productCatalog").
 * @param action          Operation executed (e.g., "GET", "PUT", "EVICT", "CLEAR", "BULK_GET").
 * @param key             Target cache key string, or composite summary for batch operations (sanitized).
 * @param hitCount        Number of cache hits registered during this operation.
 * @param missCount       Number of cache misses registered during this operation.
 * @param durationMs      Total time taken to execute the cache operation in milliseconds.
 * @param sizeBytes       Size of the payload retrieved or stored in bytes (-1 if unknown/unsupported).
 * @param isHit           Boolean flag indicating whether the operation yielded at least one cache hit.
 * @param errorType       Fully qualified class name of any thrown cache exception, or null.
 * @param errorMessage    Detailed error message if the cache call failed, or null if successful.
 * @param timestamp       Timestamp when the cache operation occurred.
 */
public record ApplicationCacheEvent(
        String id,
        String traceId,
        String spanId,
        String applicationName,
        String cacheSystem,
        String cacheName,
        String action,
        String key,
        long hitCount,
        long missCount,
        long durationMs,
        long sizeBytes,
        boolean isHit,
        String errorType,
        String errorMessage,
        Instant timestamp
) implements KraftPulseEvent {

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

    public static ApplicationCacheEvent hit(
            String id, String traceId, String spanId, String applicationName,
            String cacheSystem, String cacheName, String key, long durationMs, long sizeBytes
    ) {
        return new ApplicationCacheEvent(
                id, traceId, spanId, applicationName, cacheSystem, cacheName,
                "GET", key, 1L, 0L, durationMs, sizeBytes, true, null, null, Instant.now()
        );
    }

    public static ApplicationCacheEvent miss(
            String id, String traceId, String spanId, String applicationName,
            String cacheSystem, String cacheName, String key, long durationMs
    ) {
        return new ApplicationCacheEvent(
                id, traceId, spanId, applicationName, cacheSystem, cacheName,
                "GET", key, 0L, 1L, durationMs, 0L, false, null, null, Instant.now()
        );
    }

    public static ApplicationCacheEvent bulkGet(
            String id, String traceId, String spanId, String applicationName,
            String cacheSystem, String cacheName, String keySummary,
            long hitCount, long missCount, long durationMs, long sizeBytes
    ) {
        return new ApplicationCacheEvent(
                id, traceId, spanId, applicationName, cacheSystem, cacheName,
                "BULK_GET", keySummary, hitCount, missCount, durationMs, sizeBytes,
                hitCount > 0, null, null, Instant.now()
        );
    }
}