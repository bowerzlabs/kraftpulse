package com.bowerzlabs.kraftpulse.sqlite.storage;

import com.bowerzlabs.kraftpulse.shared.config.StorageSettings;
import com.bowerzlabs.kraftpulse.shared.constants.HttpMethod;
import com.bowerzlabs.kraftpulse.shared.model.ApplicationInboundHttpRequestEvent;
import com.bowerzlabs.kraftpulse.shared.model.EventMetadata;
import com.bowerzlabs.kraftpulse.shared.response.PagedTelemetryResponse;
import com.bowerzlabs.kraftpulse.shared.response.requests.InboundHttpEventDetailResponse;
import com.bowerzlabs.kraftpulse.shared.response.requests.InboundHttpEventSummaryResponse;
import com.bowerzlabs.kraftpulse.shared.store.KraftPulseEventStore;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SqliteEventStore implements KraftPulseEventStore, AutoCloseable {
    private final Logger logger = Logger.getLogger(SqliteEventStore.class.getName());

    private final Path databasePath;
    private Connection connection;

    public SqliteEventStore(StorageSettings.Sqlite settings) {
        if (settings == null || settings.filePath() == null || settings.filePath().isBlank()) {
            throw new IllegalArgumentException("SQLite database file path must not be blank");
        }
        this.databasePath = Path.of(settings.filePath()).toAbsolutePath().normalize();
        initialize();
    }

    private void initialize() {
        try {
            Path parent = databasePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
            logger.info("Initialized SQLite event store at: " + databasePath);
        } catch (Exception e) {
            closeAfterInitializationFailure();
            throw new IllegalStateException(
                    "Failed to initialize KraftPulse SQLite database at " + databasePath, e
            );
        }
    }

    private void closeAfterInitializationFailure() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
            } finally {
                connection = null;
            }
        }
    }

    @Override
    public synchronized void saveRequest(ApplicationInboundHttpRequestEvent event) {
        logger.info("Saving inbound HTTP request event ID: " + event.metadata().eventId());

        String sql = "INSERT INTO kp_inbound_http_request_events (" +
                "event_id, timestamp, application_name, trace_id, span_id, " +
                "method, path, matching_pattern, status_code, duration_ms, " +
                "duration_nanos, started_at, controller_class, handler_method, " +
                "authenticated_user, roles_json, client_ip, user_agent, " +
                "request_content_length, response_content_length, headers_json, " +
                "query_params_json, query_count, query_duration_ms, has_exception, has_n_plus_one" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, event.metadata().eventId());
            stmt.setTimestamp(2, Timestamp.from(event.metadata().timestamp()));
            stmt.setString(3, event.metadata().applicationName());
            stmt.setString(4, event.metadata().traceId());
            stmt.setString(5, event.metadata().spanId());

            stmt.setString(6, event.method() != null ? event.method().name() : null);
            stmt.setString(7, event.path());
            stmt.setString(8, event.matchingPattern());
            if (event.statusCode() != null) {
                stmt.setInt(9, event.statusCode());
            } else {
                stmt.setNull(9, java.sql.Types.INTEGER);
            }
            stmt.setLong(10, event.durationMs());
            stmt.setLong(11, event.durationNanos());
            stmt.setTimestamp(12, event.startedAt() != null ? Timestamp.from(event.startedAt()) : null);
            stmt.setString(13, event.controllerClass());
            stmt.setString(14, event.handlerMethod());

            stmt.setString(15, event.authenticatedUser());
            stmt.setString(16, serializeList(event.roles()));
            stmt.setString(17, event.clientIp());
            stmt.setString(18, event.userAgent());

            stmt.setLong(19, event.requestContentLength());
            stmt.setLong(20, event.responseContentLength());
            stmt.setString(21, serializeMap(event.headers()));
            stmt.setString(22, serializeMap(event.queryParams()));

            stmt.setInt(23, event.queryCount());
            stmt.setLong(24, event.queryDurationMs());
            stmt.setBoolean(25, event.hasException());
            stmt.setBoolean(26, event.hasNPlusOne());

            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to persist inbound HTTP request event", e);
            throw new IllegalStateException("Failed to save inbound HTTP request event to SQLite", e);
        }
    }

    @Override
    public synchronized PagedTelemetryResponse<InboundHttpEventSummaryResponse> recentRequests(
            Instant since,
            int limit
    ) {
        StringBuilder queryBuilder = new StringBuilder(
                "SELECT event_id, trace_id, span_id, timestamp, method, path, matching_pattern, " +
                        "status_code, duration_ms, controller_class, handler_method, authenticated_user, " +
                        "query_count, query_duration_ms, has_exception, has_n_plus_one " +
                        "FROM kp_inbound_http_request_events WHERE 1=1"
        );

        List<Object> params = new ArrayList<>();

        if (since != null) {
            queryBuilder.append(" AND timestamp >= ?");
            // Pass epoch millis or SQL timestamp depending on schema; storing as epoch millis avoids text parsing issues
            params.add(since.toEpochMilli());
        }

        queryBuilder.append(" ORDER BY timestamp DESC LIMIT ?");
        params.add(limit > 0 ? limit : 50);

        List<InboundHttpEventSummaryResponse> summaries = new ArrayList<>();
        long totalElements = countRequests(since);

        try (PreparedStatement stmt = connection.prepareStatement(queryBuilder.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String methodStr = rs.getString("method");
                    HttpMethod method = methodStr != null ? HttpMethod.valueOf(methodStr) : HttpMethod.GET;
                    Integer statusCode = rs.getObject("status_code", Integer.class);

                    // Safely parse timestamp whether stored as epoch millis (long) or string
                    Instant timestamp;
                    long epochMillis = rs.getLong("timestamp");
                    if (!rs.wasNull() && epochMillis > 0) {
                        timestamp = Instant.ofEpochMilli(epochMillis);
                    } else {
                        Timestamp ts = rs.getTimestamp("timestamp");
                        timestamp = ts != null ? ts.toInstant() : Instant.now();
                    }

                    summaries.add(new InboundHttpEventSummaryResponse(
                            rs.getString("event_id"),
                            rs.getString("trace_id"),
                            rs.getString("span_id"),
                            timestamp,
                            method,
                            rs.getString("path"),
                            rs.getString("matching_pattern"),
                            statusCode,
                            rs.getLong("duration_ms"),
                            rs.getString("controller_class"),
                            rs.getString("handler_method"),
                            rs.getString("authenticated_user"),
                            rs.getInt("query_count"),
                            rs.getLong("query_duration_ms"),
                            rs.getBoolean("has_exception"),
                            rs.getBoolean("has_n_plus_one")
                    ));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to query recent inbound requests", e);
            throw new IllegalStateException("Failed to query recent HTTP requests from SQLite", e);
        }

        int effectiveLimit = limit > 0 ? limit : 50;
        return PagedTelemetryResponse.of(summaries, 0, effectiveLimit, totalElements);
    }

    private long countRequests(Instant since) {
        StringBuilder countQuery = new StringBuilder("SELECT COUNT(*) FROM kp_inbound_http_request_events WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (since != null) {
            countQuery.append(" AND timestamp >= ?");
            params.add(since.toEpochMilli());
        }

        try (PreparedStatement stmt = connection.prepareStatement(countQuery.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Failed to count requests", e);
        }
        return 0L;
    }

    private long countRequests1(Instant since) {
        StringBuilder countQuery = new StringBuilder("SELECT COUNT(*) FROM kp_inbound_http_request_events WHERE 1=1");
        List<Object> params = new ArrayList<>();

//        if (applicationName != null && !applicationName.isBlank()) {
//            countQuery.append(" AND application_name = ?");
//            params.add(applicationName);
//        }

        if (since != null) {
            countQuery.append(" AND timestamp >= ?");
            params.add(Timestamp.from(since));
        }

        try (PreparedStatement stmt = connection.prepareStatement(countQuery.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Failed to count requests", e);
        }
        return 0L;
    }

    @Override
    public synchronized Optional<InboundHttpEventDetailResponse> requestByEventId(String eventId) {
        if (eventId == null || eventId.isBlank()) {
            return Optional.empty();
        }

        String sql = "SELECT * FROM kp_inbound_http_request_events WHERE event_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, eventId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    EventMetadata metadata = new EventMetadata(
                            rs.getString("event_id"),
                            rs.getTimestamp("timestamp").toInstant(),
                            rs.getString("application_name"),
                            rs.getString("trace_id"),
                            rs.getString("span_id")
                    );

                    String methodStr = rs.getString("method");
                    HttpMethod method = methodStr != null ? HttpMethod.valueOf(methodStr) : HttpMethod.GET;
                    Integer statusCode = rs.getObject("status_code", Integer.class);
                    Timestamp startedAtTs = rs.getTimestamp("started_at");
                    Instant startedAt = startedAtTs != null ? startedAtTs.toInstant() : null;

                    List<String> roles = deserializeList(rs.getString("roles_json"));
                    Map<String, String> headers = deserializeMap(rs.getString("headers_json"));
                    Map<String, String> queryParams = deserializeMap(rs.getString("query_params_json"));

                    ApplicationInboundHttpRequestEvent event = new ApplicationInboundHttpRequestEvent(
                            metadata,
                            method,
                            rs.getString("path"),
                            rs.getString("matching_pattern"),
                            statusCode,
                            rs.getLong("duration_ms"),
                            rs.getLong("duration_nanos"),
                            startedAt,
                            rs.getString("controller_class"),
                            rs.getString("handler_method"),
                            rs.getString("authenticated_user"),
                            roles,
                            rs.getString("client_ip"),
                            rs.getString("user_agent"),
                            rs.getLong("request_content_length"),
                            rs.getLong("response_content_length"),
                            headers,
                            queryParams,
                            rs.getInt("query_count"),
                            rs.getLong("query_duration_ms"),
                            rs.getBoolean("has_exception"),
                            rs.getBoolean("has_n_plus_one")
                    );

                    return Optional.of(InboundHttpEventDetailResponse.fromEvent(event));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to query inbound request by eventId: " + eventId, e);
            throw new IllegalStateException("Failed to query request details from SQLite", e);
        }

        return Optional.empty();
    }

    private String serializeList(java.util.List<String> list) {
        if (list == null || list.isEmpty()) return "[]";
        return "[\"" + String.join("\",\"", list) + "\"]";
    }

    private List<String> deserializeList(String json) {
        if (json == null || json.isBlank() || json.equals("[]")) return List.of();
        String cleaned = json.replace("[", "").replace("]", "").replace("\"", "");
        if (cleaned.isBlank()) return List.of();
        return List.of(cleaned.split(",\\s*"));
    }

    private String serializeMap(java.util.Map<String, String> map) {
        if (map == null || map.isEmpty()) return "{}";
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (var entry : map.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(entry.getKey()).append("\":\"")
                    .append(entry.getValue() != null ? entry.getValue().replace("\"", "\\\"") : "")
                    .append("\"");
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    private Map<String, String> deserializeMap(String json) {
        Map<String, String> map = new HashMap<>();
        if (json == null || json.isBlank() || json.equals("{}")) return map;
        // Basic parser fallback for simple JSON key-value maps stored as text
        String content = json.trim();
        if (content.startsWith("{") && content.endsWith("}")) {
            content = content.substring(1, content.length() - 1);
        }
        if (content.isBlank()) return map;

        String[] pairs = content.split("\",\\s*\"");
        for (String pair : pairs) {
            String[] kv = pair.replace("\"", "").split(":\\s*", 2);
            if (kv.length == 2) {
                map.put(kv[0], kv[1]);
            }
        }
        return map;
    }

    @Override
    public synchronized void close() {
        if (connection == null) {return;}

        try {
            connection.close();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to close KraftPulse SQLite database", e);
        } finally {
            connection = null;
        }
    }
}