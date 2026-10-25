
package com.bowerzlabs.kraftpulse.sqlite.storage;

import com.bowerzlabs.kraftpulse.shared.config.StorageSettings;
import com.bowerzlabs.kraftpulse.shared.model.ApplicationInboundHttpRequestEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class SqliteEventStoreTest {

    @TempDir
    Path tempDir;

    @Test
    void createsDatabaseFileAtConfiguredPath() {
        Path databasePath = tempDir.resolve("kraftpulse.db");

        try (SqliteEventStore ignored = new SqliteEventStore(new StorageSettings.Sqlite(databasePath.toString()))) {
            assertTrue(Files.exists(databasePath));
            assertTrue(Files.isRegularFile(databasePath));
        }
    }

    @Test
    void createsParentDirectoriesWhenTheyDoNotExist() {
        Path databasePath = tempDir
                .resolve("data")
                .resolve("kraftpulse")
                .resolve("kraftpulse.db");

        assertFalse(Files.exists(databasePath.getParent()));

        try (SqliteEventStore ignored =
                     new SqliteEventStore(new StorageSettings.Sqlite(
                             databasePath.toString()))) {

            assertTrue(Files.isDirectory(databasePath.getParent()));
            assertTrue(Files.isRegularFile(databasePath));
        }
    }

    @Test
    void createsDatabaseInExistingDirectory() {
        Path databasePath = tempDir.resolve("existing.db");

        assertDoesNotThrow(() -> {
            try (SqliteEventStore ignored =
                         new SqliteEventStore(new StorageSettings.Sqlite(
                                 databasePath.toString()))) {
                assertTrue(Files.exists(databasePath));
            }
        });
    }

    @Test
    void rejectsNullSettings() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new SqliteEventStore(null)
        );

        assertEquals(
                "SQLite database file path must not be blank",
                exception.getMessage()
        );
    }

    @Test
    void rejectsNullFilePath() {
        StorageSettings.Sqlite settings =
                new StorageSettings.Sqlite(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> new SqliteEventStore(settings)
        );
    }

    @Test
    void rejectsBlankFilePath() {
        StorageSettings.Sqlite settings =
                new StorageSettings.Sqlite("   ");

        assertThrows(
                IllegalArgumentException.class,
                () -> new SqliteEventStore(settings)
        );
    }

    @Test
    void defaultConstructorCreatesDefaultDatabase() {
        Path defaultDatabase = Path.of("./kraftpulse-data.db")
                .toAbsolutePath()
                .normalize();

        try {
            // Avoid leaving a database in the test working directory.
            Files.deleteIfExists(defaultDatabase);

            try (SqliteEventStore ignored = new SqliteEventStore(null)) {
                assertTrue(Files.exists(defaultDatabase));
            }
        } catch (Exception e) {
            fail("Default database initialization failed", e);
        } finally {
            try {
                Files.deleteIfExists(defaultDatabase);
            } catch (Exception e) {
                fail("Could not clean up the default database file", e);
            }
        }
    }

    @Test
    void closeCanBeCalledMoreThanOnce() {
        SqliteEventStore store = new SqliteEventStore(
                new StorageSettings.Sqlite(
                        tempDir.resolve("close.db").toString())
        );

        assertDoesNotThrow(() -> {
            store.close();
            store.close();
        });
    }

    @Test
    void saveRequestIsNotImplementedYet() {
        try (SqliteEventStore store = new SqliteEventStore(
                new StorageSettings.Sqlite(
                        tempDir.resolve("save.db").toString()))) {

            assertThrows(
                    UnsupportedOperationException.class,
                    () -> store.saveRequest((ApplicationInboundHttpRequestEvent) null)
            );
        }
    }

    @Test
    void recentRequestsIsNotImplementedYet() {
        try (SqliteEventStore store = new SqliteEventStore(
                new StorageSettings.Sqlite(
                        tempDir.resolve("recent.db").toString()))) {

            assertThrows(
                    UnsupportedOperationException.class,
                    () -> store.recentRequests(
                            "test-app",
                            Instant.EPOCH,
                            10
                    )
            );
        }
    }

    @Test
    void requestByEventIdIsNotImplementedYet() {
        try (SqliteEventStore store = new SqliteEventStore(
                new StorageSettings.Sqlite(
                        tempDir.resolve("lookup.db").toString()))) {

            assertThrows(
                    UnsupportedOperationException.class,
                    () -> store.requestByEventId("event-123")
            );
        }
    }
}
