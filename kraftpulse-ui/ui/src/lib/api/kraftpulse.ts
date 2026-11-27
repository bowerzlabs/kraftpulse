import { apiFetch } from './client'

export interface EndpointStat {
    path: string
    averageDurationMs: number
}

export interface RequestSummary {
    eventId: string
    traceId: string
    spanId: string
    timestamp: string
    method: string
    path: string
    matchingPattern: string
    statusCode: number | null
    durationMs: number
    controllerClass: string | null
    handlerMethod: string | null
    authenticatedUser: string | null
    queryCount: number
    queryDurationMs: number
    hasException: boolean
    hasNPlusOne: boolean
}

export interface OverviewResponse {
    totalRequestsLastHour: number
    totalRequestsLast24h: number
    errorRatePercent: number
    averageResponseTimeMs: number
    recentExceptionCount: number
    recentRequests: RequestSummary[]
    recentExceptions: ExceptionSummary[]
}

export interface ExceptionSummary {
    id: string
    traceId: string
    exceptionType: string
    simpleName: string
    message: string
    originClass: string
    originMethod: string
    lineNumber: number
    httpMethod: string
    requestUri: string
    occurrenceCount: number
    lastSeen: string
}

export function getOverview(): Promise<OverviewResponse> {
    console.log("getting overview")
    return apiFetch<OverviewResponse>('/overview')
}


// ─────────────────────────────────────────────
// Query telemetry (summary + full detail)
// ─────────────────────────────────────────────

export interface QueryEventSummary {
    id: string
    traceId: string
    applicationName: string
    databaseType: string
    operationType: string
    repositoryClass: string | null
    repositoryMethod: string | null
    normalizedQuery: string
    durationMs: number
    isSlow: boolean
    isNPlusOneSuspect: boolean
    isError: boolean
    startedAt: string
}

export interface QueryEventDetail {
    id: string
    traceId: string
    spanId: string | null
    parentSpanId: string | null
    applicationName: string
    databaseType: string
    databaseName: string | null
    host: string | null
    port: number
    dbUser: string | null
    rawQuery: string
    normalizedQuery: string
    operationType: string
    repositoryClass: string | null
    repositoryMethod: string | null
    durationMs: number
    durationNanos: number
    rowsReturned: number
    affectedRows: number
    transactionId: string | null
    isSlow: boolean
    isNPlusOneSuspect: boolean
    isError: boolean
    errorType: string | null
    errorMessage: string | null
    startedAt: string
}

export function getQueryDetails(queryId: string): Promise<QueryEventDetail> {
    return apiFetch<QueryEventDetail>(
        `/queries/${encodeURIComponent(queryId)}`
    )
}


// ─────────────────────────────────────────────
// Request details
// ─────────────────────────────────────────────

export type SpanType =
    | 'HTTP'
    | 'SECURITY'
    | 'CONTROLLER'
    | 'SERVICE'
    | 'TRANSACTION'
    | 'REPOSITORY'
    | 'DATABASE'
    | 'CACHE'
    | 'OUTBOUND'
    | 'SCHEDULED_JOB'
    | 'UNKNOWN'

export interface RequestDetailsResponse {
    traceId: string
    spanId: string | null

    method: string | null
    path: string | null
    route: string | null
    statusCode: number | null
    durationMs: number | null
    startedAt: string | null
    completedAt: string | null

    client: ClientInfo | null
    security: SecurityInfo | null
    controller: ControllerInfo | null

    waterfall: WaterfallNode[]

    database: DatabaseSummary
    queries: QueryEventSummary[]
    outboundCalls: OutboundCallDetail[]
    exceptions?: ExceptionSummary[]

    error: RequestErrorResponse | null

    performance: PerformanceSummary

    attributes: Record<string, string>
}

export interface ClientInfo {
    ip: string | null
    userAgent: string | null
    host: string | null
    scheme: string | null
}

export interface SecurityInfo {
    authenticated: boolean
    principal: string | null
    authorities: string[]
    authenticationType: string | null
}

export interface ControllerInfo {
    className: string | null
    methodName: string | null
}

export interface WaterfallNode {
    spanId: string
    parentSpanId: string | null
    type: SpanType
    name: string
    durationMs: number
    startOffsetMs: number
    attributes: Record<string, string>
    children: WaterfallNode[]
}

export interface DatabaseSummary {
    queryCount: number
    totalDurationMs: number
    slowQueryCount: number
    queries: DatabaseQueryResponse[]
}

export interface DatabaseQueryResponse {
    spanId: string | null
    query: string
    normalizedQuery: string | null
    durationMs: number
    slow: boolean
    rowsReturned: number | null
    repositoryClass: string | null
    repositoryMethod: string | null
    transactionId: string | null
}

// =============================================================================
// Paste into src/lib/api/kraftpulse.ts
//   1. Delete the old `OutboundCallResponse` interface.
//   2. In `RequestDetailsResponse`, change
//          outboundCalls: OutboundCallResponse[]
//      to
//          outboundCalls: OutboundCallDetail[]
//   3. Add this interface.
// =============================================================================

/** Mirrors OutboundHttpEventDetailResponse. */
export interface OutboundCallDetail {
    id: string
    requestId: string | null
    traceId: string
    spanId: string
    parentSpanId: string | null
    applicationName: string
    targetService: string | null
    httpMethod: string
    url: string
    uriTemplate: string | null
    host: string
    port: number
    /** -1 when the call failed before a response arrived. */
    statusCode: number
    statusText: string | null
    error: boolean
    durationMs: number
    /** -1 when unavailable (always, for interceptor-based capture). */
    connectionTimeMs: number
    requestHeaders: Record<string, string>
    responseHeaders: Record<string, string>
    /** -1 when unknown. */
    requestBodySize: number
    responseBodySize: number
    clientType: string | null
    errorType: string | null
    errorMessage: string | null
    timestamp: string
}

export interface RequestErrorResponse {
    type: string | null
    message: string | null
    stacktrace: string | null
    originClass: string | null
    originMethod: string | null
    occurrenceCount: number
    firstSeen: string | null
    lastSeen: string | null
}

export interface PerformanceSummary {
    totalDurationMs: number
    databaseDurationMs: number
    outboundDurationMs: number
    cacheDurationMs: number
    applicationDurationMs: number
    criticalPathDurationMs: number
    criticalPath: string[]
    slow: boolean
}

export function getRequestDetails(
    traceId: string
): Promise<RequestDetailsResponse> {
    return apiFetch<RequestDetailsResponse>(
        `/requests/${encodeURIComponent(traceId)}`
    )
}

export interface ApplicationDetails {
    appName: string
    appVersion: string
    jvmVersion: string
    jvmVendor: string
    springBootVersion: string
    osName: string
    osArch: string
    activeProfiles: string[]
    kraftPulseVersion: string
    maxMemoryMb: number
    totalMemoryMb: number
    freeMemoryMb: number
}

export function getAppDetails(): Promise<ApplicationDetails> {
    return apiFetch<ApplicationDetails>('/app/details')
}

export function getOutboundDetails(
    outboundId: string
): Promise<OutboundCallDetail> {
    return apiFetch<OutboundCallDetail>(
        `/outbound/${encodeURIComponent(outboundId)}`
    )
}