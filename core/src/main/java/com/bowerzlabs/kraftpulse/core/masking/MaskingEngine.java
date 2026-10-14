
package com.bowerzlabs.kraftpulse.core.masking;


import com.bowerzlabs.kraftpulse.shared.masking.MaskingRule;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class MaskingEngine {

    private final boolean enabled;
    private final Set<String> allowlist;

    public MaskingEngine(boolean enabled, Set<String> allowlist) {
        this.enabled = enabled;
        this.allowlist = Set.copyOf(Objects.requireNonNull(allowlist));
    }

    public Map<String, String> maskFields(
            Map<String, String> fields
    ) {
        if (!enabled || fields == null || fields.isEmpty()) {
            return fields;
        }

        return fields.entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(
                        Map.Entry::getKey,
                        entry -> {
                            String key = entry.getKey();
                            String value = entry.getValue();

                            if (allowlist.contains(key)) {
                                return value;
                            }

                            boolean sensitive = java.util.Arrays.stream(MaskingRule.values()).anyMatch(rule -> rule.matches(key));

                            return sensitive ? MaskingRule.REDACTED : value;
                        }
                ));
    }
}
