package com.bowerzlabs.kraftpulse.sqlite.storage

import com.bowerzlabs.kraftpulse.shared.config.StorageSettings
import com.bowerzlabs.kraftpulse.shared.constants.HttpMethod
import com.bowerzlabs.kraftpulse.shared.model.*
import com.bowerzlabs.kraftpulse.shared.response.PagedTelemetryResponse
import com.bowerzlabs.kraftpulse.shared.response.exceptions.ExceptionSummaryResponse
import com.bowerzlabs.kraftpulse.shared.response.query.ApplicationQueryEventSummary
import com.bowerzlabs.kraftpulse.shared.response.requests.InboundHttpEventDetailResponse
import com.bowerzlabs.kraftpulse.shared.response.requests.InboundHttpEventSummaryResponse
import com.bowerzlabs.kraftpulse.shared.store.KraftPulseEventStore
import com.bowerzlabs.kraftpulse.sqlite.config.SqliteDatabaseFactory
import com.bowerzlabs.kraftpulse.sqlite.schema.KraftPulseExceptionsTable
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.less
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import com.bowerzlabs.kraftpulse.sqlite.schema.KraftPulseInboundHttpRequestsEventsTable as Events
import com.bowerzlabs.kraftpulse.sqlite.schema.KraftPulseQueriesTable as Queries

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

    // ------------------------------------------------------------------ queries

    override fun saveQuery(event: ApplicationQueryEvent) {
        println("[SqliteEventStore] saving  query $event to $database")
        saveQueries(listOf(event))
    }

    /** One transaction for the whole batch: much cheaper than one transaction per query. */
    override fun saveQueries(events: List<ApplicationQueryEvent>) {
        println("[SqliteEventStore] saving ${events.size} queries to $database")
        if (events.isEmpty()) return

        transaction(database) {
            Queries.batchInsert(events, shouldReturnGeneratedValues = false) { e ->
                this[Queries.id] = e.id()
                this[Queries.requestId] = e.requestId()
                this[Queries.traceId] = e.traceId()
                this[Queries.spanId] = e.spanId()
                this[Queries.parentSpanId] = e.parentSpanId()
                this[Queries.applicationName] = e.applicationName()
                this[Queries.databaseType] = e.databaseType()
                this[Queries.databaseName] = e.databaseName()
                this[Queries.host] = e.host()
                this[Queries.port] = e.port()
                this[Queries.dbUser] = e.dbUser()
                this[Queries.rawQuery] = e.rawQuery()
                this[Queries.normalizedQuery] = e.normalizedQuery()
                this[Queries.operationType] = e.operationType()
                this[Queries.repositoryClass] = e.repositoryClass()
                this[Queries.repositoryMethod] = e.repositoryMethod()
                this[Queries.durationMs] = e.durationMs()
                this[Queries.durationNanos] = e.durationNanos()
                this[Queries.rowsReturned] = e.rowsReturned()
                this[Queries.affectedRows] = e.affectedRows()
                this[Queries.transactionId] = e.transactionId()
                this[Queries.isSlow] = if (e.isSlow()) 1 else 0
                this[Queries.isNPlusOneSuspect] = if (e.isNPlusOneSuspect()) 1 else 0
                this[Queries.errorType] = e.errorType()
                this[Queries.errorMessage] = e.errorMessage()
                this[Queries.startedAt] = e.startedAt().toString()
                this[Queries.startedAtMs] = e.startedAt().toEpochMilli()
            }
        }
    }

    override fun queriesByRequestId(requestId: String?): List<ApplicationQueryEvent> {
        if (requestId.isNullOrBlank()) return emptyList()

        return transaction(database) {
            Queries
                .selectAll()
                .where { Queries.requestId eq requestId }
                .orderBy(Queries.startedAtMs to SortOrder.ASC)
                .map { toQueryEvent(it) }
        }
    }

    private fun toQueryEvent(row: ResultRow): ApplicationQueryEvent =
        ApplicationQueryEvent(
            row[Queries.id],
            row[Queries.requestId],
            row[Queries.traceId],
            row[Queries.spanId],
            row[Queries.parentSpanId],
            row[Queries.applicationName],
            row[Queries.databaseType],
            row[Queries.databaseName],
            row[Queries.host],
            row[Queries.port],
            row[Queries.dbUser],
            row[Queries.rawQuery],
            row[Queries.normalizedQuery],
            row[Queries.operationType],
            row[Queries.repositoryClass],
            row[Queries.repositoryMethod],
            row[Queries.durationMs],
            row[Queries.durationNanos],
            row[Queries.rowsReturned],
            row[Queries.affectedRows],
            row[Queries.transactionId],
            row[Queries.isSlow] != 0,
            row[Queries.isNPlusOneSuspect] != 0,
            row[Queries.errorType],
            row[Queries.errorMessage],
            parseInstant(row[Queries.startedAt])
        )

    // ----------------------------------------------------------------- requests

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

            val traceId = row[Events.traceId]

            // eventId doubles as the request id (see InboundHttpRequestEventBuilder)
            val requestId = row[Events.eventId]

            val exceptionRows = KraftPulseExceptionsTable
                .selectAll()
                .where { KraftPulseExceptionsTable.requestId eq requestId }
                .toList()

            val querySummaries = queriesByRequestId(requestId)
                .map { ApplicationQueryEventSummary.from(it) }

            val exceptionSummaries = exceptionRows.map { exRow ->
                ExceptionSummaryResponse(
                    exRow[KraftPulseExceptionsTable.id],
                    exRow[KraftPulseExceptionsTable.traceId],
                    exRow[KraftPulseExceptionsTable.exceptionType],
                    exRow[KraftPulseExceptionsTable.simpleName],
                    exRow[KraftPulseExceptionsTable.message],
                    exRow[KraftPulseExceptionsTable.originClass],
                    exRow[KraftPulseExceptionsTable.originMethod],
                    exRow[KraftPulseExceptionsTable.lineNumber],
                    exRow[KraftPulseExceptionsTable.httpMethod],
                    exRow[KraftPulseExceptionsTable.requestUri],
                    exRow[KraftPulseExceptionsTable.occurrenceCount],
                    parseInstant(exRow[KraftPulseExceptionsTable.lastSeen])
                )
            }

            val metadata = EventMetadata(
                row[Events.eventId],
                parseInstant(row[Events.timestamp]),
                row[Events.applicationName],
                traceId,
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

//            Optional.of(InboundHttpEventDetailResponse.fromEvent(event, exceptionSummaries))

            Optional.of(
                InboundHttpEventDetailResponse.fromEvent(
                    event,
                    exceptionSummaries,
                    querySummaries
                )
            )
        }
    }

    // --------------------------------------------------------------- exceptions

    override fun saveException(event: ApplicationExceptionEvent) {
        transaction(database) {
            // Check if exception fingerprint already exists to handle atomic recurrence counter updates
            val existing = KraftPulseExceptionsTable
                .selectAll()
                .where { KraftPulseExceptionsTable.id eq event.id() }
                .firstOrNull()

            if (existing != null) {
                val currentCount = existing[KraftPulseExceptionsTable.occurrenceCount]
                KraftPulseExceptionsTable.update({ KraftPulseExceptionsTable.id eq event.id() }) {
                    it[lastSeen] = event.lastSeen().toString()
                    it[occurrenceCount] = currentCount + 1
                }
            } else {
                KraftPulseExceptionsTable.insert {
                    it[id] = event.id()
                    it[traceId] = event.traceId()
                    it[requestId] = event.requestId()
                    it[spanId] = event.spanId()
                    it[applicationName] = event.applicationName()
                    it[exceptionType] = event.exceptionType()
                    it[simpleName] = event.simpleName()
                    it[message] = event.message()
                    it[rootCauseType] = event.rootCauseType()
                    it[rootCauseMessage] = event.rootCauseMessage()
                    it[originClass] = event.originClass()
                    it[originMethod] = event.originMethod()
                    it[lineNumber] = event.lineNumber()
                    it[stacktrace] = event.stacktrace()
                    it[httpMethod] = event.httpMethod()
                    it[requestUri] = event.requestUri()
                    it[queryString] = event.queryString()
                    it[handlerName] = event.handlerName()
                    it[clientIp] = event.clientIp()
                    it[headersJson] = writeJson(event.headers())
                    it[linkedQuerySpanId] = event.linkedQuerySpanId()
                    it[firstSeen] = event.firstSeen().toString()
                    it[lastSeen] = event.lastSeen().toString()
                    it[occurrenceCount] = event.occurrenceCount()
                }
            }
        }
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

    // --------------------------------------------------------------- retention

    override fun applyRetention(maxAgeHours: Long, maxEvents: Long) {
        transaction(database) {
            if (maxAgeHours > 0) {
                val cutoff = Instant.now().minus(maxAgeHours, ChronoUnit.HOURS)

                Events.deleteWhere {
                    Events.timestamp less cutoff.toString()
                }
                Queries.deleteWhere {
                    Queries.startedAtMs less cutoff.toEpochMilli()
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

                    oldestIds.chunked(500).forEach { chunk ->
                        Events.deleteWhere { Events.eventId inList chunk }
                        // eventId == requestId, so the queries of a removed request go with it
                        Queries.deleteWhere { Queries.requestId inList chunk }
                    }
                }

                // Queries with no request (background threads) are only bounded by this cap.
                val queryCap = maxEvents * 100
                val excessQueries = (Queries.selectAll().count() - queryCap).coerceAtLeast(0)
                if (excessQueries > 0) {
                    val oldestQueryIds = Queries
                        .select(Queries.id)
                        .orderBy(Queries.startedAtMs to SortOrder.ASC)
                        .limit(excessQueries.coerceAtMost(Int.MAX_VALUE.toLong()).toInt())
                        .map { it[Queries.id] }

                    oldestQueryIds.chunked(500).forEach { chunk ->
                        Queries.deleteWhere { Queries.id inList chunk }
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