import { apiFetch } from './client'

export interface EndpointStat {
    path: string
    averageDurationMs: number
}

export interface RequestSummary {
    traceId: string
    method: string
    path: string
    statusCode: number
    durationMs: number
    timestamp: string
}

export interface ExceptionSummary {
    traceId: string
    exceptionType: string
    message: string
    originClass: string
    timestamp: string
}

export interface OverviewResponse {
    totalRequestsLastHour: number
    totalRequestsLast24h: number
    errorRatePercent: number
    averageResponseTimeMs: number
    slowestEndpoints: EndpointStat[]
    recentExceptionCount: number
    recentRequests: RequestSummary[]
    recentExceptions: ExceptionSummary[]
}

export function getOverview(): Promise<OverviewResponse> {
    return apiFetch<OverviewResponse>('/overview')
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
    outboundCalls: OutboundCallResponse[]

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

export interface OutboundCallResponse {
    spanId: string | null
    method: string
    url: string
    statusCode: number | null
    durationMs: number
    startOffsetMs: number | null
    errorMessage: string | null
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