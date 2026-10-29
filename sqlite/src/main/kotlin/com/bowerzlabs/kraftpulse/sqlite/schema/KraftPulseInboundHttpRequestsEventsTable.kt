
package com.bowerzlabs.kraftpulse.sqlite.schema

import org.jetbrains.exposed.v1.core.Table

object KraftPulseInboundHttpRequestsEventsTable : Table("kp_inbound_http_request_events") {
    // Metadata
    val eventId = text("event_id")
    val timestamp = text("timestamp")
    val applicationName = text("application_name")
    val traceId = text("trace_id")
    val spanId = text("span_id")

    // HTTP request
    val method = text("method")
    val path = text("path")
    val matchingPattern = text("matching_pattern").nullable()
    val statusCode = integer("status_code").nullable()
    val durationMs = long("duration_ms")
    val durationNanos = long("duration_nanos")
    val startedAt = text("started_at")

    // Handler and controller
    val controllerClass = text("controller_class").nullable()
    val handlerMethod = text("handler_method").nullable()

    // Security and client context
    val authenticatedUser = text("authenticated_user").nullable()
    val rolesJson = text("roles_json").default("[]")
    val clientIp = text("client_ip").nullable()
    val userAgent = text("user_agent").nullable()

    // Payload sizes and request metadata
    val requestContentLength = long("request_content_length").default(-1L)
    val responseContentLength = long("response_content_length").default(-1L)
    val headersJson = text("headers_json").default("{}")
    val queryParamsJson = text("query_params_json").default("{}")

    // Diagnostics
    val queryCount = integer("query_count").default(0)
    val queryDurationMs = long("query_duration_ms").default(0L)
    val hasException = integer("has_exception").default(0)
    val hasNPlusOne = integer("has_n_plus_one").default(0)

    override val primaryKey = PrimaryKey(eventId)

    init {
        index(
            customIndexName = "idx_kp_inbound_http_trace_id",
            isUnique = false,
            columns = arrayOf(traceId)
        )

        index(
            customIndexName = "idx_kp_inbound_http_timestamp",
            isUnique = false,
            columns = arrayOf(timestamp)
        )

        index(
            customIndexName = "idx_kp_inbound_http_pattern_status",
            isUnique = false,
            columns = arrayOf(matchingPattern, statusCode)
        )

        index(
            customIndexName = "idx_kp_inbound_http_diagnostics",
            isUnique = false,
            columns = arrayOf(hasException, hasNPlusOne, statusCode)
        )
    }
}
