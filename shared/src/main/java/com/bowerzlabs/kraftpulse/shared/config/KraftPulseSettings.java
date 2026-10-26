package com.bowerzlabs.kraftpulse.shared.config;

import java.util.Set;

/**
 * Framework-agnostic configuration. Respective adapters bind this from
 * @ConfigurationProperties("kraftpulse"); a future Ktor adapter would bind it
 * from HOCON/application.conf. Neither core nor common know Spring exists.
 */
public interface KraftPulseSettings {

  boolean isEnabled();

  long getSlowQueryThresholdMs();

  int getNPlusOneThreshold();

  StorageSettings getStorage();

  MaskingSettings getMasking();

  RetentionSettings  getRetention();
}
