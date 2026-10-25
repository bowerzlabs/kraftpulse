-- KraftPulse SQLite Telemetry Schema: Inbound HTTP Request Events
CREATE TABLE IF NOT EXISTS kp_inbound_http_request_events (
    -- Metadata (EventMetadata flattens into top-level columns)
                                                              event_id                 TEXT PRIMARY KEY NOT NULL,
                                                              timestamp                TEXT NOT NULL,               -- ISO-8601 UTC string (e.g. 2026-10-22T21:34:00Z)
                                                              application_name         TEXT NOT NULL,
                                                              trace_id                 TEXT NOT NULL,               -- W3C Trace ID for distributed tracing correlation
                                                              span_id                  TEXT NOT NULL,               -- Active execution span ID

    -- HTTP Request Specifics
                                                              method                   TEXT NOT NULL,               -- GET, POST, PUT, DELETE, etc.
                                                              path                     TEXT NOT NULL,               -- Raw URI path (e.g. /api/v1/orders/123)
                                                              matching_pattern         TEXT,                        -- Route template (e.g. /api/v1/orders/{id})
                                                              status_code              INTEGER,                     -- HTTP Status Code (e.g. 200, 404, 500)
                                                              duration_ms              INTEGER NOT NULL,            -- Execution duration in milliseconds
                                                              duration_nanos           INTEGER NOT NULL,            -- High-precision duration in nanoseconds
                                                              started_at               TEXT NOT NULL,               -- ISO-8601 UTC request start timestamp

    -- Handler & Controller Mapping
                                                              controller_class         TEXT,                        -- Spring Bean/Controller class name
                                                              handler_method           TEXT,                        -- Target Controller method name

    -- Security & Client Context
                                                              authenticated_user       TEXT,                        -- Spring Security principal/username
                                                              roles_json               TEXT NOT NULL DEFAULT '[]',  -- JSON array string (e.g. ["ROLE_USER", "ROLE_ADMIN"])
                                                              client_ip                TEXT,                        -- Client IP / X-Forwarded-For IP
                                                              user_agent               TEXT,                        -- HTTP User-Agent string

    -- Payload Sizes & Headers
                                                              request_content_length   INTEGER NOT NULL DEFAULT -1, -- Request body size in bytes
                                                              response_content_length  INTEGER NOT NULL DEFAULT -1, -- Response body size in bytes
                                                              headers_json             TEXT NOT NULL DEFAULT '{}',  -- Sanitized HTTP headers as JSON object
                                                              query_params_json        TEXT NOT NULL DEFAULT '{}',  -- Sanitized query params as JSON object

    -- Aggregated Diagnostics & Metrics
                                                              query_count              INTEGER NOT NULL DEFAULT 0,  -- Total database queries executed during request
                                                              query_duration_ms        INTEGER NOT NULL DEFAULT 0,  -- Total database wait duration in ms
                                                              has_exception            INTEGER NOT NULL DEFAULT 0,  -- 1 (true) if unhandled exception occurred, else 0
                                                              has_n_plus_one           INTEGER NOT NULL DEFAULT 0   -- 1 (true) if N+1 query pattern detected, else 0
);

-- ============================================================================
-- INDEXES FOR HIGH-PERFORMANCE ANALYTICS AND TRACING
-- ============================================================================

-- Fast lookup by W3C Trace ID (correlating HTTP requests with Queries & Exceptions)
CREATE INDEX IF NOT EXISTS idx_kp_inbound_http_trace_id
    ON kp_inbound_http_request_events (trace_id);

-- Time-range filtering for analytical dashboards
CREATE INDEX IF NOT EXISTS idx_kp_inbound_http_timestamp
    ON kp_inbound_http_request_events (timestamp DESC);

-- Endpoint performance profiling & metric aggregation by route pattern
CREATE INDEX IF NOT EXISTS idx_kp_inbound_http_pattern_status
    ON kp_inbound_http_request_events (matching_pattern, status_code);

-- Rapid diagnostic filtering for failed requests and anomalous database behaviors
CREATE INDEX IF NOT EXISTS idx_kp_inbound_http_diagnostics
    ON kp_inbound_http_request_events (has_exception, has_n_plus_one, status_code);