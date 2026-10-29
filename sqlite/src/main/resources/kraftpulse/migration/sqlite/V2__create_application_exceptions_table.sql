
CREATE TABLE IF NOT EXISTS kp_application_exceptions (
                                                         id                  TEXT PRIMARY KEY,              -- Exception fingerprint for grouping/deduplication
                                                         requestId           TEXT,                          -- HTTP request ID for occurrence correlation
                                                         traceId             TEXT,                          -- W3C trace ID
                                                         spanId              TEXT,                          -- Execution span ID
                                                         applicationName     TEXT NOT NULL,                 -- Application instance name
                                                         exceptionType       TEXT NOT NULL,                 -- Fully qualified exception class name
                                                         simpleName          TEXT NOT NULL,                 -- Short class name for UI cards
                                                         message             TEXT,                          -- Top-level exception detail message
                                                         rootCauseType       TEXT,                          -- Root cause exception class name
                                                         rootCauseMessage    TEXT,                          -- Root cause exception message
                                                         originClass         TEXT,                          -- Top stack frame origin class
                                                         originMethod        TEXT,                          -- Top stack frame method name
                                                         lineNumber          INTEGER DEFAULT -1,            -- Source line number (-1 if unavailable)
                                                         stacktrace          TEXT,                          -- Full formatted stack trace
                                                         httpMethod          TEXT,                          -- GET, POST, etc., or NULL
                                                         requestUri          TEXT,                          -- Request path or NULL
                                                         queryString         TEXT,                          -- Raw query parameters or NULL
                                                         handlerName         TEXT,                          -- Spring MVC handler signature
                                                         clientIp            TEXT,                          -- Client IP address or trusted proxy origin
                                                         headers             TEXT,                          -- Sanitized headers serialized as JSON
                                                         linkedQuerySpanId   TEXT,                          -- Optional correlated database query span ID
                                                         firstSeen           TEXT NOT NULL,                 -- First observed timestamp
                                                         lastSeen            TEXT NOT NULL,                 -- Most recently observed timestamp
                                                         occurrenceCount     INTEGER DEFAULT 1 NOT NULL     -- Cumulative count for this fingerprint
);

-- Dashboard aggregation and filtering indexes
CREATE INDEX IF NOT EXISTS idx_exceptions_last_seen
    ON kp_application_exceptions(lastSeen DESC);

CREATE INDEX IF NOT EXISTS idx_exceptions_trace_id
    ON kp_application_exceptions(traceId);

CREATE INDEX IF NOT EXISTS idx_exceptions_type
    ON kp_application_exceptions(exceptionType);

CREATE INDEX IF NOT EXISTS idx_exceptions_request_id
    ON kp_application_exceptions(requestId);
