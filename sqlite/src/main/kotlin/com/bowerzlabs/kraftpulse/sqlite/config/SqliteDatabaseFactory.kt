
package com.bowerzlabs.kraftpulse.sqlite.config

import com.bowerzlabs.kraftpulse.shared.config.StorageSettings
import org.jetbrains.exposed.v1.jdbc.Database
import java.nio.file.Files
import java.nio.file.Path

internal class SqliteDatabaseFactory(
    settings: StorageSettings.Sqlite
) {
    val database: Database

    init {
        require(!settings.filePath().isNullOrBlank()) {
            "SQLite database file path must not be blank"
        }

        val path = Path.of(settings.filePath()).toAbsolutePath().normalize()

        path.parent?.let(Files::createDirectories)

        database = Database.connect(
            url = "jdbc:sqlite:$path",
            driver = "org.sqlite.JDBC"
        )
    }
}
