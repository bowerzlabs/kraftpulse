package com.bowerzlabs.kraftpulse.shared.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Represents a JVM or Spring Boot context lifecycle milestone (Startup, Ready, Refresh, Shutdown).
 *
 * @param id               Unique lifecycle event identifier.
 * @param applicationName  Name of the application instance firing the event.
 * @param state            Lifecycle phase (e.g., "STARTING", "REFRESHED", "READY", "SHUTTING_DOWN", "CLOSED").
 * @param bootDurationMs   Total time taken for Spring Boot to start up in milliseconds (-1 for shutdown events).
 * @param activeProfiles   List of active Spring profiles enabled for the running application context (e.g., ["prod", "aws"]).
 * @param javaVersion      JRE/JDK runtime version (e.g., "21.0.2").
 * @param springBootVersion Version of Spring Boot framework in use (e.g., "3.2.4").
 * @param processId        JVM Operating System Process ID (PID).
 * @param jvmArguments     List of JVM startup flags and arguments passed to the application.
 * @param environmentInfo  Key system metadata (e.g., OS Name, Arch, Available Processors, Max Memory).
 * @param timestamp        Timestamp when the state transition occurred.
 */
public record ApplicationLifecycleEvent(
        String id,
        String applicationName,
        String state,
        long bootDurationMs,
        List<String> activeProfiles,
        String javaVersion,
        String springBootVersion,
        long processId,
        List<String> jvmArguments,
        Map<String, String> environmentInfo,
        Instant timestamp
) implements KraftPulseEvent {

    public ApplicationLifecycleEvent {
        activeProfiles = activeProfiles == null ? List.of() : List.copyOf(activeProfiles);
        jvmArguments = jvmArguments == null ? List.of() : List.copyOf(jvmArguments);
        environmentInfo = environmentInfo == null ? Map.of() : Map.copyOf(environmentInfo);
    }

    @Override
    public EventMetadata metadata() {
        return new EventMetadata(
                id,
                timestamp,
                applicationName,
                "", // No request trace for container lifecycle
                ""
        );
    }
}