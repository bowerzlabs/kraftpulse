package com.bowerzlabs.kraftpulse.sqlite.storage

import com.bowerzlabs.kraftpulse.shared.config.StorageSettings
import com.bowerzlabs.kraftpulse.shared.constants.HttpMethod
import com.bowerzlabs.kraftpulse.shared.model.*
import com.bowerzlabs.kraftpulse.shared.response.PagedTelemetryResponse
import com.bowerzlabs.kraftpulse.shared.response.requests.InboundHttpEventDetailResponse
import com.bowerzlabs.kraftpulse.shared.response.requests.InboundHttpEventSummaryResponse
import com.bowerzlabs.kraftpulse.shared.store.KraftPulseEventStore
import com.bowerzlabs.kraftpulse.sqlite.config.SqliteDatabaseFactory
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.core.less
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import com.bowerzlabs.kraftpulse.sqlite.schema.KraftPulseEventsTable as Events

class SqliteEventStore(
    settings: StorageSettings.Sqlite,
    private val objectMapper: ObjectMapper = ObjectMapper()
) : KraftPulseEventStore, AutoCloseable {

    private val database: Database =
        SqliteDatabaseFactory(settings).database

    override fun saveRequest(event: ApplicationInboundHttpRequestEvent) {
        transaction(database) {
            Events.insert {
                it[eventId] = event.metadata().eventId()
                it[timestamp] = event.metadata().timestamp().toString()
                it[applicationName] = event.metadata().applicationName()
                it[traceId] = event.metadata().traceId()
                it[spanId] = event.metadata().spanId()

                it[method] = event.method()?.name ?: HttpMethod.GET.name
                it[path] = event.path()
                it[matchingPattern] = event.matchingPattern()
                it[statusCode] = event.statusCode()
                it[durationMs] = event.durationMs()
                it[durationNanos] = event.durationNanos()
                it[startedAt] = event.startedAt().toString()

                it[controllerClass] = event.controllerClass()
                it[handlerMethod] = event.handlerMethod()

                it[authenticatedUser] = event.authenticatedUser()
                it[rolesJson] = writeJson(event.roles())
                it[clientIp] = event.clientIp()
                it[userAgent] = event.userAgent()

                it[requestContentLength] = event.requestContentLength()
                it[responseContentLength] = event.responseContentLength()
                it[headersJson] = writeJson(event.headers())
                it[queryParamsJson] = writeJson(event.queryParams())

                it[queryCount] = event.queryCount()
                it[queryDurationMs] = event.queryDurationMs()
                it[hasException] = if (event.hasException()) 1 else 0
                it[hasNPlusOne] = if (event.hasNPlusOne()) 1 else 0
            }
        }
    }

    override fun recentRequests(
        since: Instant?,
        limit: Int
    ): PagedTelemetryResponse<InboundHttpEventSummaryResponse> {
        val effectiveLimit = limit.coerceIn(1, 500)

        return transaction(database) {
            val baseQuery = Events.selectAll()

            if (since != null) {
                baseQuery.andWhere {
                    Events.timestamp greaterEq since.toString()
                }
            }

            val total = baseQuery.count()

            val rows = baseQuery
                .orderBy(Events.timestamp to SortOrder.DESC)
                .limit(effectiveLimit)
                .toList()

            val summaries = rows.map { row ->
                InboundHttpEventSummaryResponse(
                    row[Events.eventId],
                    row[Events.traceId],
                    row[Events.spanId],
                    parseInstant(row[Events.timestamp]),
                    parseMethod(row[Events.method]),
                    row[Events.path],
                    row[Events.matchingPattern],
                    row[Events.statusCode],
                    row[Events.durationMs],
                    row[Events.controllerClass],
                    row[Events.handlerMethod],
                    row[Events.authenticatedUser],
                    row[Events.queryCount],
                    row[Events.queryDurationMs],
                    row[Events.hasException] != 0,
                    row[Events.hasNPlusOne] != 0
                )
            }

            PagedTelemetryResponse.of(
                summaries,
                0,
                effectiveLimit,
                total
            )
        }
    }

    override fun requestByEventId(
        eventId: String?
    ): Optional<InboundHttpEventDetailResponse> {
        if (eventId.isNullOrBlank()) {
            return Optional.empty()
        }

        return transaction(database) {
            val row = Events
                .selectAll()
                .where { Events.eventId eq eventId }
                .limit(1)
                .firstOrNull()
                ?: return@transaction Optional.empty()

            val metadata = EventMetadata(
                row[Events.eventId],
                parseInstant(row[Events.timestamp]),
                row[Events.applicationName],
                row[Events.traceId],
                row[Events.spanId]
            )

            val event = ApplicationInboundHttpRequestEvent(
                metadata,
                parseMethod(row[Events.method]),
                row[Events.path],
                row[Events.matchingPattern],
                row[Events.statusCode],
                row[Events.durationMs],
                row[Events.durationNanos],
                parseInstant(row[Events.startedAt]),
                row[Events.controllerClass],
                row[Events.handlerMethod],
                row[Events.authenticatedUser],
                readStringList(row[Events.rolesJson]),
                row[Events.clientIp],
                row[Events.userAgent],
                row[Events.requestContentLength],
                row[Events.responseContentLength],
                readStringMap(row[Events.headersJson]),
                readStringMap(row[Events.queryParamsJson]),
                row[Events.queryCount],
                row[Events.queryDurationMs],
                row[Events.hasException] != 0,
                row[Events.hasNPlusOne] != 0
            )

            Optional.of(InboundHttpEventDetailResponse.fromEvent(event))
        }
    }

    override fun saveException(event: ApplicationExceptionEvent) {
        // Implement when the exception event schema and persistence contract are defined.
    }

    override fun saveLifecycle(event: ApplicationLifecycleEvent) {
        // Implement when the lifecycle event schema and persistence contract are defined.
    }

    override fun saveOutboundCall(event: ApplicationOutboundHttpRequestEvent) {
        // Implement when the outbound-call event schema is defined.
    }

    override fun saveScheduledJob(event: ApplicationJobScheduledJobEvent) {
        // Implement when the scheduled-job event schema is defined.
    }

    override fun applyRetention(maxAgeHours: Long, maxEvents: Long) {
        transaction(database) {
            if (maxAgeHours > 0) {
                val cutoff = Instant.now()
                    .minus(maxAgeHours, ChronoUnit.HOURS)
                    .toString()

                Events.deleteWhere {
                    Events.timestamp less cutoff
                }
            }

            if (maxEvents > 0) {
                val excess = (Events.selectAll().count() - maxEvents)
                    .coerceAtLeast(0)

                if (excess > 0) {
                    val oldestIds = Events
                        .select(Events.eventId)
                        .orderBy(Events.timestamp to SortOrder.ASC)
                        .limit(excess.coerceAtMost(Int.MAX_VALUE.toLong()).toInt())
                        .map { it[Events.eventId] }

                    oldestIds.forEach { id ->
                        Events.deleteWhere {
                            Events.eventId eq id
                        }
                    }
                }
            }
        }
    }

    private fun writeJson(value: Any): String =
        objectMapper.writeValueAsString(value)

    private fun readStringList(json: String): List<String> =
        objectMapper.readValue(
            json,
            object : TypeReference<List<String>>() {}
        )

    private fun readStringMap(json: String): Map<String, String> =
        objectMapper.readValue(
            json,
            object : TypeReference<Map<String, String>>() {}
        )

    private fun parseInstant(value: String): Instant =
        Instant.parse(value)

    private fun parseMethod(value: String): HttpMethod =
        runCatching { HttpMethod.valueOf(value) }
            .getOrElse { HttpMethod.GET }

    override fun close() {
        // Exposed manages JDBC connections through its transaction/connection layer.
        // Do not close a shared Database configuration here.
    }
}
