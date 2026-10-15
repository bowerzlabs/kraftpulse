package com.bowerzlabs.kraftpulse.core.masking;

import com.bowerzlabs.kraftpulse.shared.masking.MaskingRule;
import com.bowerzlabs.kraftpulse.shared.masking.MaskingStrategy;

import java.lang.reflect.Array;
import java.util.*;

public final class MaskingEngine {
    private final boolean enabled;
    private final Set<String> allowlist;
    private final boolean exposeEmailDomain;

    public MaskingEngine(boolean enabled, Set<String> allowlist) {
        this(enabled, allowlist, false);
    }

    public MaskingEngine(boolean enabled, Set<String> allowlist, boolean exposeEmailDomain) {
        this.enabled = enabled;
        this.allowlist = Set.copyOf(Objects.requireNonNull(allowlist));
        this.exposeEmailDomain = exposeEmailDomain;
    }

    public Map<String, String> maskFields(Map<String, String> fields) {
        if (!enabled || fields == null || fields.isEmpty()) return fields;
        Map<String, String> result = new LinkedHashMap<>();
        fields.forEach((key, value) -> result.put(key, (String) maskField(key, value)));
        return Collections.unmodifiableMap(result);
    }

    public Object mask(Object payload) {
        return enabled ? maskValue(payload) : payload;
    }

    private Object maskValue(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<Object, Object> result = new LinkedHashMap<>();
            Object semanticKey = map.get("key");
            boolean hasSemanticValue = semanticKey instanceof String && map.containsKey("value");

            map.forEach((key, item) -> {
                String field = key instanceof String s ? s : null;
                if (hasSemanticValue && "value".equals(key)) {
                    result.put(key, maskField((String) semanticKey, item));
                } else {
                    result.put(key, field == null ? maskValue(item) : maskField(field, item));
                }
            });
            return Collections.unmodifiableMap(result);
        }

        if (value instanceof Iterable<?> items) {
            List<Object> result = new ArrayList<>();
            items.forEach(item -> result.add(maskValue(item)));
            return Collections.unmodifiableList(result);
        }

        if (value != null && value.getClass().isArray()) {
            List<Object> result = new ArrayList<>();
            for (int i = 0; i < Array.getLength(value); i++) {
                result.add(maskValue(Array.get(value, i)));
            }
            return Collections.unmodifiableList(result);
        }

        return value;
    }

    private Object maskField(String field, Object value) {
        if (value == null) return null;

        for (MaskingRule rule : MaskingRule.values()) {
            if (rule.matches(field)) {
                return maskSensitive(rule.strategy(), value);
            }
        }

        return field != null && allowlist.contains(field)
                && !(value instanceof Map<?, ?> || value instanceof Iterable<?> || value.getClass().isArray())
                ? value
                : maskValue(value);
    }

    private Object maskSensitive(MaskingStrategy strategy, Object value) {
        if (!(value instanceof String text)) return MaskingRule.REDACTED;
        if (text.isEmpty()) return text;

        try {
            return switch (strategy) {
                case FULL -> MaskingRule.REDACTED;
                case EMAIL_HINT -> maskEmail(text);
                case CARD_NUMBER -> maskCardNumber(text);
                case PHONE -> maskPhone(text);
            };
        } catch (RuntimeException exception) {
            return MaskingRule.REDACTED;
        }
    }

    private String maskEmail(String value) {
        int at = value.indexOf('@');
        if (at <= 0 || at != value.lastIndexOf('@') || at == value.length() - 1) {
            return MaskingRule.REDACTED;
        }

        String local = value.substring(0, at);
        String domain = value.substring(at + 1);

        if (local.isBlank() || domain.isBlank() || !domain.contains(".")) {
            return MaskingRule.REDACTED;
        }

        return local.charAt(0) + "***@" + (exposeEmailDomain ? domain : "***");
    }

    private String maskCardNumber(String value) {
        String digits = value.replaceAll("\\D", "");
        if (digits.length() < 12 || digits.length() > 19) {
            return MaskingRule.REDACTED;
        }

        return digits.substring(0, 4) + "*".repeat(digits.length() - 6) + digits.substring(digits.length() - 2);
    }

    private String maskPhone(String value) {
        String phone = value.trim();
        if (!phone.startsWith("+")) return MaskingRule.REDACTED;

        String digits = phone.substring(1).replaceAll("\\D", "");
        if (digits.length() < 8 || digits.length() > 15) {
            return MaskingRule.REDACTED;
        }

        int codeLength = digits.startsWith("254") ? 3
                : digits.startsWith("1") ? 1
                  : digits.startsWith("44") || digits.startsWith("91") ? 2
                    : 0;

        if (codeLength == 0 || digits.length() - codeLength < 4) {
            return MaskingRule.REDACTED;
        }

        String country = digits.substring(0, codeLength);
        String national = digits.substring(codeLength);

        return "+" + country + "*".repeat(national.length() - 2) + national.substring(national.length() - 2);
    }
}