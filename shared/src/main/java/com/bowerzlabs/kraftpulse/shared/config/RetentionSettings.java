package com.bowerzlabs.kraftpulse.shared.config;

public record RetentionSettings(
        long maxAgeHours,
        long maxEvents
) {
  public RetentionSettings() {
    this(24L, 50_000L);
  }
}
