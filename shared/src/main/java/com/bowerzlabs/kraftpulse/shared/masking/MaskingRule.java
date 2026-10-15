package com.bowerzlabs.kraftpulse.shared.masking;

import java.util.regex.Pattern;

public enum MaskingRule {

    PASSWORD(
            "(?i)(password|passwd|pwd|passphrase)",
            MaskingStrategy.FULL
    ),

    TOKEN(
            "(?i)(access[_-]?token|refresh[_-]?token|"
                    + "id[_-]?token|token|api[_-]?key|"
                    + "client[_-]?secret|secret[_-]?key|secret)",
            MaskingStrategy.FULL
    ),

    AUTHORIZATION_HEADER(
            "(?i)(authorization|proxy[_-]?authorization|"
                    + "cookie|set[_-]?cookie)",
            MaskingStrategy.FULL
    ),


    EMAIL(
            "(?i)(email|email[_-]?address|e[_-]?mail)",
            MaskingStrategy.EMAIL_HINT
    ),

    CARD_NUMBER(
            "(?i)(card[_-]?number|cc[_-]?num|"
                    + "credit[_-]?card[_-]?number|pan)",
            MaskingStrategy.CARD_NUMBER
    ),

    PHONE(
            "(?i)(phone|phone[_-]?number|mobile|telephone)",
            MaskingStrategy.PHONE
    );

    public static final String REDACTED = "***REDACTED***";

    private final Pattern fieldPattern;
    private final MaskingStrategy strategy;

    MaskingRule(String regex, MaskingStrategy strategy) {
        this.fieldPattern = Pattern.compile(regex);
        this.strategy = strategy;
    }

    public Pattern fieldPattern() {
        return fieldPattern;
    }

    public MaskingStrategy strategy() {
        return strategy;
    }

    public boolean matches(String fieldName) {
        return fieldName != null
                && fieldPattern.matcher(fieldName).find();
    }
}


