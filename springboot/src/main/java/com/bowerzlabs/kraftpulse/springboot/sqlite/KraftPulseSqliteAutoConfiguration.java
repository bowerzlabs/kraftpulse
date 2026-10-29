package com.bowerzlabs.kraftpulse.springboot.sqlite;

import com.bowerzlabs.kraftpulse.shared.store.KraftPulseEventStore;
import com.bowerzlabs.kraftpulse.springboot.config.KraftPulseProperties;
import com.bowerzlabs.kraftpulse.sqlite.migration.KraftPulseSqliteMigrator;
import com.bowerzlabs.kraftpulse.sqlite.storage.SqliteEventStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

@AutoConfiguration
@ConditionalOnProperty(prefix = "kraftpulse.storage", name = "type", havingValue = "sqlite", matchIfMissing = true)
public class KraftPulseSqliteAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(KraftPulseSqliteAutoConfiguration.class);
    private ObjectMapper objectMapper = new ObjectMapper();

    @Bean(name = "kraftPulseSqliteDataSource", defaultCandidate = false)
    public DataSource kraftPulseSqliteDataSource(KraftPulseProperties properties) {
        String filePath = properties.getStorageProperties().getFilePath();
        if (filePath == null || filePath.isBlank()) {
            filePath = "./kraftpulse-data.db";
        }

        log.info("Initialized isolated KraftPulse SQLite DataSource targeting: {}", filePath);
        return new com.bowerzlabs.kraftpulse.sqlite.datasource.SingleFileSqliteDataSource(filePath);
    }

    @Bean
    public KraftPulseSqliteSchemaInitializer kraftPulseSqliteSchemaInitializer(
            @Qualifier("kraftPulseSqliteDataSource") DataSource kraftPulseSqliteDataSource) {
        return new KraftPulseSqliteSchemaInitializer(kraftPulseSqliteDataSource);
    }

    /**
     * Exposes the SQLite Event Store implementation as a Spring Bean for dashboard queries.
     */
    @Bean
    public KraftPulseEventStore kraftPulseEventStore(KraftPulseProperties properties) {
        com.bowerzlabs.kraftpulse.shared.config.StorageSettings.Sqlite sqliteSettings =
                new com.bowerzlabs.kraftpulse.shared.config.StorageSettings.Sqlite(
                        properties.getStorageProperties().getFilePath()
                );
        return new SqliteEventStore(sqliteSettings, objectMapper);
    }

    public static class KraftPulseSqliteSchemaInitializer implements InitializingBean {
        private final DataSource dataSource;

        public KraftPulseSqliteSchemaInitializer(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        @Override
        public void afterPropertiesSet() {
            log.info("Executing KraftPulse SQLite Flyway schema migration on isolated DataSource");
            KraftPulseSqliteMigrator.migrate(dataSource);
        }
    }
}