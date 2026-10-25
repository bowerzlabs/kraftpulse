package com.bowerzlabs.kraftpulse.shared.response;

import java.util.List;

/**
 * Standard paginated envelope for telemetry dashboard queries.
 */
public record PagedTelemetryResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean isFirst,
        boolean isLast
) {
    public PagedTelemetryResponse {
        content = content == null ? List.of() : List.copyOf(content);
    }

    public static <T> PagedTelemetryResponse<T> of(List<T> content, int page, int size, long totalElements) {
        int effectiveSize = Math.max(1, size);
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / effectiveSize);

        boolean isFirst = page <= 0;
        boolean isLast = totalPages == 0 || page >= totalPages - 1;

        return new PagedTelemetryResponse<>(
                content,
                page,
                size,
                totalElements,
                totalPages,
                isFirst,
                isLast
        );
    }
}