package com.bowerzlabs.kraftpulse.springboot.config;

import com.bowerzlabs.kraftpulse.shared.config.KraftPulseSettings;
import com.bowerzlabs.kraftpulse.shared.config.MaskingSettings;
import com.bowerzlabs.kraftpulse.shared.config.RetentionSettings;
import com.bowerzlabs.kraftpulse.shared.config.StorageSettings;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Spring Boot configuration properties for KraftPulse.
 */
@ConfigurationProperties(prefix = "kraftpulse")
public class KraftPulseProperties implements KraftPulseSettings {

    private boolean enabled = true;
    private String dashboardPath = "/kraft/pulse";
    private long slowQueryThresholdMs = 500;
    private int nPlusOneThreshold = 10;

    /**
     * Inner mutable holder for Spring Boot property binding (e.g. kraftpulse.storage.file-path).
     */
    private StorageProperties storage = new StorageProperties();
    private MaskingSettings masking = new MaskingSettings();
    private RetentionSettings retention = new RetentionSettings();

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getDashboardPath() {
        return dashboardPath;
    }

    public void setDashboardPath(String dashboardPath) {
        this.dashboardPath = dashboardPath;
    }

    @Override
    public long getSlowQueryThresholdMs() {
        return slowQueryThresholdMs;
    }

    public void setSlowQueryThresholdMs(long slowQueryThresholdMs) {
        this.slowQueryThresholdMs = slowQueryThresholdMs;
    }

    @Override
    public int getNPlusOneThreshold() {
        return nPlusOneThreshold;
    }

    public void setNPlusOneThreshold(int nPlusOneThreshold) {
        this.nPlusOneThreshold = nPlusOneThreshold;
    }

    /**
     * Converts bound properties into the immutable StorageSettings interface required by core engine.
     */
    @Override
    public StorageSettings getStorage() {
        return storage.toStorageSettings();
    }

    public StorageProperties getStorageProperties() {
        return storage;
    }

    public void setStorage(StorageProperties storage) {
        this.storage = storage;
    }

    @Override
    public MaskingSettings getMasking() {
        return masking;
    }

    public void setMasking(MaskingSettings masking) {
        this.masking = masking;
    }

    @Override
    public RetentionSettings getRetention() {
        return retention;
    }

    public void setRetention(RetentionSettings retention) {
        this.retention = retention;
    }

    /**
     * Mutable JavaBean class that matches Spring Boot configuration keys:
     * kraftpulse.storage.file-path, kraftpulse.storage.jdbc-url, etc.
     */
    public static class StorageProperties {
        private String type = "sqlite";
        private String filePath;
        private String jdbcUrl;
        private String username;
        private String password;
        private String connectionUrl;
        private String database;
        private String apiKey;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public String getJdbcUrl() {
            return jdbcUrl;
        }

        public void setJdbcUrl(String jdbcUrl) {
            this.jdbcUrl = jdbcUrl;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getConnectionUrl() {
            return connectionUrl;
        }

        public void setConnectionUrl(String connectionUrl) {
            this.connectionUrl = connectionUrl;
        }

        public String getDatabase() {
            return database;
        }

        public void setDatabase(String database) {
            this.database = database;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public StorageSettings toStorageSettings() {
            if ("jdbc".equalsIgnoreCase(type)) {
                return new StorageSettings.Jdbc(jdbcUrl, username, password);
            }
            if ("mongo".equalsIgnoreCase(type)) {
                return new StorageSettings.Mongo(connectionUrl, database);
            }
            if ("cloud".equalsIgnoreCase(type)) {
                return new StorageSettings.Cloud(connectionUrl, apiKey);
            }
            return new StorageSettings.Sqlite(filePath);
        }
    }
}