CREATE TABLE outbound_http_request_events (
                                              id VARCHAR(128) PRIMARY KEY,
                                              trace_id VARCHAR(128) NOT NULL,
                                              span_id VARCHAR(128),
                                              request_id VARCHAR(128),
                                              parent_span_id VARCHAR(128),
                                              application_name VARCHAR(255) NOT NULL,
                                              target_service VARCHAR(255),
                                              http_method VARCHAR(16) NOT NULL,
                                              url TEXT NOT NULL,
                                              uri_template TEXT,
                                              host VARCHAR(255),
                                              port INTEGER NOT NULL DEFAULT -1,
                                              status_code INTEGER NOT NULL DEFAULT -1,
                                              status_text VARCHAR(128),
                                              duration_ms BIGINT NOT NULL DEFAULT 0,
                                              connection_time_ms BIGINT NOT NULL DEFAULT -1,
                                              request_headers_json TEXT NOT NULL DEFAULT '{}',
                                              response_headers_json TEXT NOT NULL DEFAULT '{}',
                                              request_body_size BIGINT NOT NULL DEFAULT -1,
                                              response_body_size BIGINT NOT NULL DEFAULT -1,
                                              client_type VARCHAR(128),
                                              error_type VARCHAR(512),
                                              error_message TEXT,
                                              is_error INTEGER NOT NULL DEFAULT 0,
                                              started_at TEXT NOT NULL DEFAULT '',
                                              started_at_ms BIGINT NOT NULL DEFAULT 0,
                                              event_timestamp TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_outbound_http_events_trace_id
    ON outbound_http_request_events (trace_id);

CREATE INDEX idx_outbound_http_events_request_id
    ON outbound_http_request_events (request_id);

CREATE INDEX idx_outbound_http_events_timestamp
    ON outbound_http_request_events (event_timestamp DESC);

CREATE INDEX idx_outbound_http_events_target_service
    ON outbound_http_request_events (target_service);

CREATE INDEX idx_outbound_http_events_started_at_ms
    ON outbound_http_request_events (started_at_ms);