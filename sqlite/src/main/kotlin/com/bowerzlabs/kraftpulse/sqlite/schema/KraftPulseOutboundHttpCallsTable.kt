package com.bowerzlabs.kraftpulse.sqlite.schema

import org.jetbrains.exposed.v1.core.Table
import java.time.Instant

/**
 * Outbound HTTP calls using snake_case column names aligned with `SqliteEventStore.saveAll`.
 * Add this table to the list `SqliteDatabaseFactory` creates, or it will not exist.
 */
object KraftPulseOutboundHttpCallsTable : Table("outbound_http_request_events") {
    val id = varchar("id", 128)
    val requestId = text("request_id").nullable().index()
    val traceId = text("trace_id").index()
    val spanId = text("span_id")
    val parentSpanId = text("parent_span_id").nullable()
    val applicationName = text("application_name")
    val targetService = text("target_service").nullable()
    val httpMethod = text("http_method")
    val url = text("url")
    val uriTemplate = text("uri_template").nullable()
    val host = text("host")
    val port = integer("port")
    val statusCode = integer("status_code")
    val statusText = text("status_text").nullable()
    val durationMs = long("duration_ms")
    val connectionTimeMs = long("connection_time_ms")
    val requestHeadersJson = text("request_headers_json")
    val responseHeadersJson = text("response_headers_json")
    val requestBodySize = long("request_body_size")
    val responseBodySize = long("response_body_size")
    val clientType = text("client_type").nullable()
    val errorType = text("error_type").nullable()
    val errorMessage = text("error_message").nullable()
    val isError = integer("is_error")
    val startedAt = text("started_at")
    val startedAtMs = long("started_at_ms").index()
    val eventTimestamp = text("event_timestamp").clientDefault { Instant.now().toString() }

    override val primaryKey = PrimaryKey(id)
}

