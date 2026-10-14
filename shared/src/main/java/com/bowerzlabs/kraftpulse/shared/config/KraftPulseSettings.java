package com.bowerzlabs.kraftpulse.shared.config;

/**
 * Framework-agnostic configuration. Respective adapters bind this from
 * @ConfigurationProperties("kraftpulse"); a future Ktor adapter would bind it
 * from HOCON/application.conf. Neither core nor common know Spring exists.
 */
public abstract class KraftPulseSettings {
  private Boolean enabled;
  private String dashboardPath = "/kraft/pulse";
  private Long slowQueryThresholdMs = 100L;
  private int nPlusOneThreshold = 5;
}
