package com.bowerzlabs.kraftpulse.shared.config;

import java.util.Set;

public record MaskingSettings(
        boolean enabled,
        Set<String> allowlist
) {
  public MaskingSettings {
    allowlist = Set.copyOf(allowlist);
  }

  public MaskingSettings() {
    this(true, Set.of());
  }
}
