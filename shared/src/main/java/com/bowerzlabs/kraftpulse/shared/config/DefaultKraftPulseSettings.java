
package com.bowerzlabs.kraftpulse.shared.config;

import java.util.Objects;

/**
 * Default implementation of {@link KraftPulseSettings}.
 *
 * <p>Provides framework-independent default configuration for KraftPulse.
 * These settings can be used when no framework-specific configuration has
 * been supplied.
 *
 * <p>The default configuration enables KraftPulse, sets the slow-query
 * threshold to 100 milliseconds, sets the N+1 query threshold to 5, and
 * initializes default retention, masking, and SQLite storage settings.
 *
 * <p>This class belongs to the shared configuration module and does not
 * depend on any application framework.
 *
 * @param enabled whether KraftPulse is enabled
 * @param slowQueryThresholdMs the execution-time threshold in milliseconds
 *        above which a query is considered slow
 * @param nPlusOneThreshold the number of repeated queries that triggers
 *        potential N+1 query detection
 * @param retention the event retention configuration
 * @param masking the sensitive-data masking configuration
 * @param storage the event storage configuration
 *
 * @see KraftPulseSettings
 * @see RetentionSettings
 * @see MaskingSettings
 * @see StorageSettings
 */
public record DefaultKraftPulseSettings(
        boolean enabled,
        long slowQueryThresholdMs,
        int nPlusOneThreshold,
        RetentionSettings retention,
        MaskingSettings masking,
        StorageSettings storage
) implements KraftPulseSettings {

    /**
     * Validates and creates a settings instance.
     *
     * <p>The retention, masking, and storage configurations are required
     * and must not be {@code null}.
     *
     * @throws NullPointerException if retention, masking, or storage is null
     */
    public DefaultKraftPulseSettings {
        Objects.requireNonNull(retention, "retention must not be null");
        Objects.requireNonNull(masking, "masking must not be null");
        Objects.requireNonNull(storage, "storage must not be null");
    }

    /**
     * Creates a settings instance with the default KraftPulse configuration.
     *
     * <p>The defaults are:
     * <ul>
     *   <li>KraftPulse enabled</li>
     *   <li>Slow-query threshold of 100 milliseconds</li>
     *   <li>N+1 query threshold of 5</li>
     *   <li>Default retention settings</li>
     *   <li>Default masking settings</li>
     *   <li>SQLite storage using its default database path</li>
     * </ul>
     */
    public DefaultKraftPulseSettings() {
        this(
                true,
                100L,
                5,
                new RetentionSettings(),
                new MaskingSettings(),
                new StorageSettings.Sqlite()
        );
    }

    /**
     * Returns whether KraftPulse is enabled.
     *
     * @return {@code true} if KraftPulse is enabled; {@code false} otherwise
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Returns the slow-query detection threshold.
     *
     * @return the threshold in milliseconds
     */
    @Override
    public long getSlowQueryThresholdMs() {
        return slowQueryThresholdMs;
    }

    /**
     * Returns the threshold for detecting potential N+1 query problems.
     *
     * @return the number of repeated queries that triggers detection
     */
    @Override
    public int getNPlusOneThreshold() {
        return nPlusOneThreshold;
    }

    /**
     * Returns the event retention configuration.
     *
     * @return the non-null retention settings
     */
    @Override
    public RetentionSettings getRetention() {
        return retention;
    }

    /**
     * Returns the sensitive-data masking configuration.
     *
     * @return the non-null masking settings
     */
    @Override
    public MaskingSettings getMasking() {
        return masking;
    }

    /**
     * Returns the event storage configuration.
     *
     * @return the non-null storage settings
     */
    @Override
    public StorageSettings getStorage() {
        return storage;
    }
}
