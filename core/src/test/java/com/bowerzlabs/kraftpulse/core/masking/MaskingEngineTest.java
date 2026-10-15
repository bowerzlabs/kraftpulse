
package com.bowerzlabs.kraftpulse.core.masking;

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
    private static final Logger LOG = Logger.getLogger(MaskingEngineTest.class.getName());

    @Test
    void masksSensitiveFields() {
        MaskingEngine engine = new MaskingEngine(true, Set.of());

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
        MaskingEngine engine = new MaskingEngine(true, Set.of());

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
        MaskingEngine engine = new MaskingEngine(true, Set.of());

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
        MaskingEngine engine = new MaskingEngine(true, Set.of());

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

    // --- Allowlist behavior ---

    @Test
    void allowlistedSensitiveFieldIsStillMasked() {
        MaskingEngine engine = new MaskingEngine(
                true,
                Set.of("email", "password")
        );

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
        MaskingEngine engine = new MaskingEngine(
                true,
                Set.of("username")
        );

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

        MaskingEngine engine = new MaskingEngine(true, allowlist);
        allowlist.clear();

        Map<String, String> result = engine.maskFields(
                Map.of("username", "john")
        );

        LOG.info("result: " + result);

        assertEquals("john", result.get("username"));
    }

    // --- Disabled masking ---

    @Test
    void disabledMaskingReturnsOriginalFieldsUnchanged() {
        MaskingEngine engine = new MaskingEngine(false, Set.of());

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
        MaskingEngine engine = new MaskingEngine(true, Set.of());

        Map<String, String> fields = new HashMap<>();
        fields.put("username", null);

        Map<String, String> result = engine.maskFields(fields);

        assertTrue(result.containsKey("username"));
        assertNull(result.get("username"));
    }

    @Test
    void nullFieldNameIsHandledWhenMaskingEnabled() {
        MaskingEngine engine = new MaskingEngine(true, Set.of());

        Map<String, String> fields = new HashMap<>();
        fields.put(null, "value");

        Map<String, String> result = engine.maskFields(fields);

        assertEquals("value", result.get(null));
    }

    // --- Input and output immutability ---

    @Test
    void maskingDoesNotModifyInputMap() {
        MaskingEngine engine = new MaskingEngine(true, Set.of());

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
        MaskingEngine engine = new MaskingEngine(true, Set.of());

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
        MaskingEngine engine = new MaskingEngine(true, Set.of());

        Map<String, String> fields = Map.of(
                "password", "secret",
                "email", "user@example.com",
                "username", "john"
        );

        Map<String, String> result = engine.maskFields(fields);

        assertEquals(fields.size(), result.size());
        assertEquals(fields.keySet(), result.keySet());
    }

    // --- MaskingRule behavior ---

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
