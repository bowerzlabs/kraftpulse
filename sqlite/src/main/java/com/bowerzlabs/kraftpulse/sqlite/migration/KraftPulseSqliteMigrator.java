package com.bowerzlabs.kraftpulse.sqlite.migration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;

import javax.sql.DataSource;

public final class KraftPulseSqliteMigrator {

    private KraftPulseSqliteMigrator() {
    }

    public static MigrateResult migrate(DataSource dataSource) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:kraftpulse/migration/sqlite")
                .table("kraftpulse_schema_history")
                .baselineOnMigrate(false)
                .outOfOrder(false)
                .load();

        return flyway.migrate();
    }
}