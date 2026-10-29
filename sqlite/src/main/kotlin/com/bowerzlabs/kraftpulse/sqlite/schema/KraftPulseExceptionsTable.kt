package com.bowerzlabs.kraftpulse.sqlite.schema

import org.jetbrains.exposed.v1.core.Table

object KraftPulseExceptionsTable : Table("kp_application_exceptions") {
    val id = text("id")
    val requestId = text("requestId")
    val traceId = text("traceId").nullable()
    val spanId = text("spanId").nullable()
    val applicationName = text("applicationName")
    val exceptionType = text("exceptionType")
    val simpleName = text("simpleName")
    val message = text("message").nullable()
    val rootCauseType = text("rootCauseType").nullable()
    val rootCauseMessage = text("rootCauseMessage").nullable()
    val originClass = text("originClass").nullable()
    val originMethod = text("originMethod").nullable()
    val lineNumber = integer("lineNumber").default(-1)
    val stacktrace = text("stacktrace").nullable()
    val httpMethod = text("httpMethod").nullable()
    val requestUri = text("requestUri").nullable()
    val queryString = text("queryString").nullable()
    val handlerName = text("handlerName").nullable()
    val clientIp = text("clientIp").nullable()
    val headersJson = text("headers").nullable()
    val linkedQuerySpanId = text("linkedQuerySpanId").nullable()
    val firstSeen = text("firstSeen")
    val lastSeen = text("lastSeen")
    val occurrenceCount = long("occurrenceCount").default(1)

    override val primaryKey = PrimaryKey(id)
}