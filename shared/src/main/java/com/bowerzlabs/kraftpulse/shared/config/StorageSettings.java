package com.bowerzlabs.kraftpulse.shared.config;

public sealed interface StorageSettings
        permits StorageSettings.Sqlite,
        StorageSettings.Jdbc,
        StorageSettings.Mongo,
        StorageSettings.Cloud
{

  public record Sqlite(String filePath) implements StorageSettings {
    public Sqlite() {
      this("./kraftpulse-data.db");
    }
  }

  record Jdbc(
          String jdbcUrl,
          String username,
          String password
  ) implements StorageSettings {
  }

  record Mongo(
          String connectionUrl,
          String database
  ) implements StorageSettings {
  }

  record Cloud(String connectionUrl, String apiKey)
          implements StorageSettings {
  }
}