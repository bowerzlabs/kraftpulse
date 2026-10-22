package com.bowerzlabs.kraftpulse.shared.model;

import java.time.Instant;

/**
 * Represents a scheduled background job or task execution captured within KraftPulse.
 * Supports Spring @Scheduled, Quartz, Spring Batch, and custom task schedulers.
 *
 * @param id                 Unique job execution event ID.
 * @param traceId            Global W3C trace ID assigned to the execution thread of this job run.
 * @param spanId             Active execution span ID representing this specific job run.
 * @param applicationName    Name of the application executing the scheduled task.
 * @param jobName            Logical job identifier or method reference (e.g., "DailyReportJob#generate").
 * @param jobGroup           Logical grouping or package name (e.g., "reports", "maintenance").
 * @param cronExpression     Configured cron expression or fixed-rate delay (e.g., "0 0 12 * * ?", "fixedRate=5000"), or null if ad-hoc.
 * @param executionType      Execution trigger type (e.g., "CRON", "FIXED_RATE", "FIXED_DELAY", "MANUAL").
 * @param threadName         Name of the executor thread handling the task (e.g., "scheduling-1").
 * @param durationMs         Total execution time in milliseconds.
 * @param status             Execution outcome status (e.g., "SUCCESS", "FAILED", "SKIPPED_LOCKED").
 * @param errorType          Fully qualified class name of any thrown exception, or null.
 * @param errorMessage       Detailed error message if the scheduled job failed, or null.
 * @param lastSuccessfulRun  Timestamp of the previous successful run if tracked, or null.
 * @param timestamp          Timestamp when the scheduled job execution initiated.
 */
public record ApplicationJobScheduledJobEvent(
        String id,
        String traceId,
        String spanId,
        String applicationName,
        String jobName,
        String jobGroup,
        String cronExpression,
        String executionType,
        String threadName,
        long durationMs,
        String status,
        String errorType,
        String errorMessage,
        Instant lastSuccessfulRun,
        Instant timestamp
) implements KraftPulseEvent {

    @Override
    public EventMetadata metadata() {
        return new EventMetadata(
                id,
                timestamp,
                applicationName,
                traceId,
                spanId
        );
    }

    public boolean isError() {
        return "FAILED".equalsIgnoreCase(status) || errorType != null;
    }

    public static ApplicationJobScheduledJobEvent success(
            String id, String traceId, String spanId, String applicationName,
            String jobName, String jobGroup, String cronExpression, String executionType,
            String threadName, long durationMs, Instant lastSuccessfulRun
    ) {
        return new ApplicationJobScheduledJobEvent(
                id, traceId, spanId, applicationName, jobName, jobGroup,
                cronExpression, executionType, threadName, durationMs, "SUCCESS",
                null, null, lastSuccessfulRun, Instant.now()
        );
    }

    public static ApplicationJobScheduledJobEvent failure(
            String id, String traceId, String spanId, String applicationName,
            String jobName, String jobGroup, String cronExpression, String executionType,
            String threadName, long durationMs, Throwable throwable
    ) {
        return new ApplicationJobScheduledJobEvent(
                id, traceId, spanId, applicationName, jobName, jobGroup,
                cronExpression, executionType, threadName, durationMs, "FAILED",
                throwable.getClass().getName(), throwable.getMessage(), null, Instant.now()
        );
    }
}