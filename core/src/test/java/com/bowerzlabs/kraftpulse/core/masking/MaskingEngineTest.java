package com.bowerzlabs.kraftpulse.core.masking;

import com.bowerzlabs.kraftpulse.shared.config.MaskingSettings;
import com.bowerzlabs.kraftpulse.shared.masking.MaskingRule;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class MaskingEngineTest {

    private static final String REDACTED = MaskingRule.REDACTED;
    private static final Logger LOG =
            Logger.getLogger(MaskingEngineTest.class.getName());

    private MaskingEngine engine(boolean enabled, Set<String> allowlist) {
        return new MaskingEngine(new MaskingSettings(enabled, allowlist));
    }

    @Test
    void masksSensitiveFields() {
        MaskingEngine engine = engine(true, Set.of());

        Map<String, String> fields = Map.of(
                "password", "secret123",
                "email", "user@example.com",
                "api_key", "abc123",
                "card_number", "4111111111111234",
                "username", "john"
        );

        Map<String, String> result = engine.maskFields(fields);
        LOG.info("result: " + result);

        assertEquals(REDACTED, result.get("password"));
        assertEquals("u***@***", result.get("email"));
        assertEquals(REDACTED, result.get("api_key"));
        assertEquals("4111**********34", result.get("card_number"));
        assertEquals("john", result.get("username"));
    }

    @Test
    void preservesNonSensitiveFields() {
        MaskingEngine engine = engine(true, Set.of());

        Map<String, String> fields = Map.of(
                "username", "john",
                "firstName", "John",
                "age", "28",
                "city", "Nakuru"
        );

        LOG.info("result: " + engine.maskFields(fields));

        assertEquals(fields, engine.maskFields(fields));
    }

    @Test
    void masksEveryDefinedRule() {
        MaskingEngine engine = engine(true, Set.of());

        Map<String, String> fields = Map.ofEntries(
                Map.entry("password", "secret"),
                Map.entry("passwd", "secret"),
                Map.entry("pwd", "secret"),
                Map.entry("token", "secret"),
                Map.entry("api_key", "secret"),
                Map.entry("api-key", "secret"),
                Map.entry("client_secret", "secret"),
                Map.entry("authorization", "Bearer abc"),
                Map.entry("cookie", "session=abc"),
                Map.entry("card_number", "4111111111111234"),
                Map.entry("cc_num", "4111111111111234"),
                Map.entry("email", "user@example.com")
        );

        Map<String, String> result = engine.maskFields(fields);
        LOG.info("result: " + result);

        assertEquals(REDACTED, result.get("password"));
        assertEquals(REDACTED, result.get("passwd"));
        assertEquals(REDACTED, result.get("pwd"));
        assertEquals(REDACTED, result.get("token"));
        assertEquals(REDACTED, result.get("api_key"));
        assertEquals(REDACTED, result.get("api-key"));
        assertEquals(REDACTED, result.get("client_secret"));
        assertEquals(REDACTED, result.get("authorization"));
        assertEquals(REDACTED, result.get("cookie"));
        assertEquals("4111**********34", result.get("card_number"));
        assertEquals("4111**********34", result.get("cc_num"));
        assertEquals("u***@***", result.get("email"));
    }

    @Test
    void maskingRulesAreCaseInsensitive() {
        MaskingEngine engine = engine(true, Set.of());

        Map<String, String> fields = Map.of(
                "PASSWORD", "secret",
                "Password", "secret",
                "API_KEY", "secret",
                "Authorization", "Bearer abc",
                "EMAIL", "user@example.com"
        );

        Map<String, String> result = engine.maskFields(fields);
        LOG.info("result: " + result);

        assertEquals(REDACTED, result.get("PASSWORD"));
        assertEquals(REDACTED, result.get("Password"));
        assertEquals(REDACTED, result.get("API_KEY"));
        assertEquals(REDACTED, result.get("Authorization"));
        assertEquals("u***@***", result.get("EMAIL"));
    }

    @Test
    void allowlistedSensitiveFieldIsStillMasked() {
        MaskingEngine engine = engine(true, Set.of("email", "password"));

        Map<String, String> fields = Map.of(
                "email", "user@example.com",
                "password", "secret"
        );

        Map<String, String> result = engine.maskFields(fields);
        LOG.info("result: " + result);

        assertEquals("u***@***", result.get("email"));
        assertEquals(REDACTED, result.get("password"));
    }

    @Test
    void allowlistUsesExactCaseSensitiveFieldNames() {
        MaskingEngine engine = engine(true, Set.of("username"));

        Map<String, String> fields = Map.of(
                "username", "john",
                "Username", "jane"
        );

        Map<String, String> result = engine.maskFields(fields);
        LOG.info("result: " + result);

        assertEquals("john", result.get("username"));
        assertEquals("jane", result.get("Username"));
    }

    @Test
    void allowlistIsDefensivelyCopied() {
        Set<String> allowlist = new HashSet<>();
        allowlist.add("username");

        MaskingSettings settings = new MaskingSettings(true, allowlist);
        MaskingEngine engine = new MaskingEngine(settings);

        allowlist.clear();

        Map<String, String> result = engine.maskFields(
                Map.of("username", "john")
        );

        LOG.info("result: " + result);

        assertEquals("john", result.get("username"));
    }

    @Test
    void disabledMaskingReturnsOriginalFieldsUnchanged() {
        MaskingEngine engine = engine(false, Set.of());

        Map<String, String> fields = Map.of(
                "password", "secret",
                "email", "user@example.com"
        );

        Map<String, String> result = engine.maskFields(fields);
        LOG.info("result: " + result);

        assertSame(fields, result);
        assertEquals("secret", result.get("password"));
        assertEquals("user@example.com", result.get("email"));
    }

    @Test
    void nullFieldValueIsPreservedWhenMaskingEnabled() {
        MaskingEngine engine = engine(true, Set.of());

        Map<String, String> fields = new HashMap<>();
        fields.put("username", null);

        Map<String, String> result = engine.maskFields(fields);

        assertTrue(result.containsKey("username"));
        assertNull(result.get("username"));
    }

    @Test
    void nullFieldNameIsHandledWhenMaskingEnabled() {
        MaskingEngine engine = engine(true, Set.of());

        Map<String, String> fields = new HashMap<>();
        fields.put(null, "value");

        Map<String, String> result = engine.maskFields(fields);

        assertEquals("value", result.get(null));
    }

    @Test
    void maskingDoesNotModifyInputMap() {
        MaskingEngine engine = engine(true, Set.of());

        Map<String, String> fields = new HashMap<>();
        fields.put("password", "secret");
        fields.put("username", "john");

        Map<String, String> result = engine.maskFields(fields);
        LOG.info("result: " + result);

        assertEquals("secret", fields.get("password"));
        assertEquals("john", fields.get("username"));
        assertEquals(REDACTED, result.get("password"));
    }

    @Test
    void maskedResultIsUnmodifiable() {
        MaskingEngine engine = engine(true, Set.of());

        Map<String, String> result = engine.maskFields(
                Map.of("password", "secret", "username", "john")
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> result.put("email", "user@example.com")
        );
    }

    @Test
    void maskingDoesNotChangeNumberOfFields() {
        MaskingEngine engine = engine(true, Set.of());

        Map<String, String> fields = Map.of(
                "password", "secret",
                "email", "user@example.com",
                "username", "john"
        );

        Map<String, String> result = engine.maskFields(fields);

        assertEquals(fields.size(), result.size());
        assertEquals(fields.keySet(), result.keySet());
    }

    @Test
    void maskingRuleDoesNotMatchNullFieldName() {
        for (MaskingRule rule : MaskingRule.values()) {
            assertFalse(rule.matches(null), rule.name());
        }
    }

    @Test
    void maskingRuleDoesNotMatchUnrelatedFieldNames() {
        for (MaskingRule rule : MaskingRule.values()) {
            assertFalse(rule.matches("username"), rule.name());
        }
    }

}