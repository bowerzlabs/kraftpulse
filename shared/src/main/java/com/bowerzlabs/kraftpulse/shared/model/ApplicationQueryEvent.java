package com.bowerzlabs.kraftpulse.shared.model;

import java.time.Instant;

/**
 * Represents a database or persistence query execution event captured within the KraftPulse diagnostic pipeline.
 * Supports both SQL (JDBC/R2DBC) and NoSQL (MongoDB, Redis, Elasticsearch) database operations.
 *
 * @param id                 Unique event identifier (e.g., generated UUID or trace-span composite key).
 * @param traceId            Global W3C trace ID correlating this query with the parent execution context.
 * @param spanId             Active execution span ID representing this specific database operation.
 * @param parentSpanId       Span ID of the parent HTTP request, background task, or messaging handler.
 * @param applicationName    Name of the application executing the database query.
 * @param databaseType       Database engine type (e.g., "POSTGRESQL", "MYSQL", "MONGODB", "REDIS").
 * @param databaseName       Target database/schema name (e.g., "orders_db").
 * @param host               Target database hostname or IP address (e.g., "db-primary.internal").
 * @param port               Target database port (e.g., 5432, 27017).
 * @param dbUser             Database username executing the operation.
 * @param rawQuery           Raw query execution string with parameters intact (used strictly for local debugging).
 * @param normalizedQuery    Sanitized query with literal values/parameters masked (e.g., "SELECT * FROM users WHERE id = ?").
 * @param operationType      Operation classification (e.g., "SELECT", "INSERT", "UPDATE", "DELETE", "AGGREGATE", "COMMAND").
 * @param repositoryClass    Spring Data repository class or DAO executing the call (e.g., "com.app.repository.UserRepository").
 * @param repositoryMethod   Spring Data repository method or DAO method executed (e.g., "findByEmailAndStatus").
 * @param durationMs         Total execution time of the query in milliseconds.
 * @param rowsReturned       Number of rows, records, or documents returned (-1 if unavailable/unknown).
 * @param affectedRows       Number of rows or documents mutated/deleted (-1 if unavailable/unknown).
 * @param transactionId      Active database transaction ID or logical transaction boundary hash, if executed within @Transactional.
 * @param isSlow             Flag indicating if execution time exceeded the configured slow query threshold (e.g., > 100ms).
 * @param isNPlusOneSuspect  Flag indicating whether KraftPulse's query analysis pipeline flagged this query as part of an N+1 cascade.
 * @param errorType          Fully qualified class name of any thrown database exception (e.g., "org.hibernate.exception.ConstraintViolationException").
 * @param errorMessage       Detailed message associated with a failed query execution, or null if successful.
 * @param startedAt          Exact timestamp when the query execution initiated.
 */
public record ApplicationQueryEvent(
        String id,
        String traceId,
        String spanId,
        String parentSpanId,
        String applicationName,
        String databaseType,
        String databaseName,
        String host,
        int port,
        String dbUser,
        String rawQuery,
        String normalizedQuery,
        String operationType,
        String repositoryClass,
        String repositoryMethod,
        long durationMs,
        long rowsReturned,
        long affectedRows,
        String transactionId,
        boolean isSlow,
        boolean isNPlusOneSuspect,
        String errorType,
        String errorMessage,
        Instant startedAt
) implements KraftPulseEvent {

    @Override
    public EventMetadata metadata() {
        return new EventMetadata(
                id,
                startedAt,
                applicationName,
                traceId,
                spanId
        );
    }

    /**
     * Helper check to determine if the query resulted in a database execution error.
     */
    public boolean isError() {
        return errorType != null || errorMessage != null;
    }

    /**
     * Factory method for constructing a newly executed query event.
     */
    public static ApplicationQueryEvent create(
            String id,
            String traceId,
            String spanId,
            String parentSpanId,
            String applicationName,
            String databaseType,
            String databaseName,
            String host,
            int port,
            String dbUser,
            String rawQuery,
            String normalizedQuery,
            String operationType,
            String repositoryClass,
            String repositoryMethod,
            long durationMs,
            long rowsReturned,
            long affectedRows,
            String transactionId,
            boolean isSlow,
            boolean isNPlusOneSuspect,
            String errorType,
            String errorMessage
    ) {
        return new ApplicationQueryEvent(
                id, traceId, spanId, parentSpanId, applicationName, databaseType,
                databaseName, host, port, dbUser, rawQuery, normalizedQuery, operationType,
                repositoryClass, repositoryMethod, durationMs, rowsReturned, affectedRows,
                transactionId, isSlow, isNPlusOneSuspect, errorType, errorMessage,
                Instant.now()
        );
    }
}