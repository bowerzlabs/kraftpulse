CREATE TABLE IF NOT EXISTS kraftpulse_queries (
                                                  id                     TEXT PRIMARY KEY,
                                                  request_id             TEXT,
                                                  trace_id               TEXT,
                                                  span_id                TEXT,
                                                  parent_span_id         TEXT,
                                                  application_name       TEXT,
                                                  database_type          TEXT,
                                                  database_name          TEXT,
                                                  host                   TEXT,
                                                  port                   INTEGER NOT NULL DEFAULT -1,
                                                  db_user                TEXT,
                                                  raw_query              TEXT,
                                                  normalized_query       TEXT,
                                                  operation_type         TEXT,
                                                  repository_class       TEXT,
                                                  repository_method      TEXT,
                                                  duration_ms            INTEGER NOT NULL DEFAULT 0,
                                                  duration_nanos         INTEGER NOT NULL DEFAULT 0,
                                                  rows_returned          INTEGER NOT NULL DEFAULT -1,
                                                  affected_rows          INTEGER NOT NULL DEFAULT -1,
                                                  transaction_id         TEXT,
                                                  is_slow                INTEGER NOT NULL DEFAULT 0,
                                                  is_n_plus_one_suspect  INTEGER NOT NULL DEFAULT 0,
                                                  error_type             TEXT,
                                                  error_message          TEXT,
                                                  started_at             TEXT NOT NULL,
                                                  started_at_ms          INTEGER NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_kraftpulse_queries_request ON kraftpulse_queries (request_id, started_at_ms);
CREATE INDEX IF NOT EXISTS idx_kraftpulse_queries_started ON kraftpulse_queries (started_at_ms);