
package com.bowerzlabs.kraftpulse.shared.masking;

import java.util.regex.Pattern;

public enum MaskingRule {

    PASSWORD("(?i)password|passwd|pwd"),
    TOKEN("(?i)token|api[_-]?key|secret"),
    AUTHORIZATION_HEADER("(?i)authorization|cookie"),
    CARD_NUMBER("(?i)card[_-]?number|cc[_-]?num"),
    EMAIL("(?i)email");

    public static final String REDACTED = "***REDACTED***";

    private final Pattern fieldPattern;

    MaskingRule(String regex) {
        this.fieldPattern = Pattern.compile(regex);
    }

    public Pattern fieldPattern() {
        return fieldPattern;
    }

    public boolean matches(String fieldName) {
        return fieldName != null
                && fieldPattern.matcher(fieldName).find();
    }

}
