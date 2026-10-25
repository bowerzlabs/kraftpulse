package com.bowerzlabs.kraftpulse.sqlite.storage;

import com.bowerzlabs.kraftpulse.shared.config.StorageSettings;
import com.bowerzlabs.kraftpulse.shared.store.EventStoreProvider;
import com.bowerzlabs.kraftpulse.shared.store.KraftPulseEventStore;

/**
 * Discovers and creates SQLite-backed event stores for KraftPulse.
 *
 * <p>This provider supports SQLite storage settings and is intended
 * to be discovered through Java's {@link java.util.ServiceLoader}.</p>
 */
public final class SqliteEventStoreProvider implements EventStoreProvider {

    @Override
    public boolean supports(StorageSettings settings) {
        return settings instanceof StorageSettings.Sqlite;
    }

    @Override
    public KraftPulseEventStore create(StorageSettings settings) {
        if (!(settings instanceof StorageSettings.Sqlite sqliteSettings)) {
            throw new IllegalArgumentException(
                    "SqliteEventStoreProvider only supports SQLite storage"
            );
        }

        return new SqliteEventStore(sqliteSettings);
    }

}
