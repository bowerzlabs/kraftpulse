package com.bowerzlabs.kraftpulse.shared.enrichment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SqlNormalizerTest {

    @Nested
    @DisplayName("Null, Empty, and Blank Input Tests")
    class NullAndBlankTests {

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Should return empty string for null or empty input")
        void normalize_nullOrEmptyInput_returnsEmptyString(String input) {
            assertEquals("", SqlNormalizer.normalize(input));
        }

        @Test
        @DisplayName("Should return empty string for whitespace-only input")
        void normalize_blankInput_returnsEmptyString() {
            assertEquals("", SqlNormalizer.normalize("   \n\t  "));
        }
    }

    @Nested
    @DisplayName("String Literal Replacement Tests")
    class StringLiteralTests {

        @Test
        @DisplayName("Should mask single-quoted string literals")
        void normalize_simpleStringLiteral_replacesWithQuestionMark() {
            String sql = "SELECT * FROM users WHERE email = 'john@example.com'";
            String expected = "SELECT * FROM users WHERE email = ?";
            assertEquals(expected, SqlNormalizer.normalize(sql));
        }

        @Test
        @DisplayName("Should handle multiple string literals in a single query")
        void normalize_multipleStringLiterals_replacesAll() {
            String sql = "SELECT * FROM users WHERE first_name = 'John' AND last_name = 'Doe'";
            String expected = "SELECT * FROM users WHERE first_name = ? AND last_name = ?";
            assertEquals(expected, SqlNormalizer.normalize(sql));
        }

        @Test
        @DisplayName("Should handle single quotes escaped via double single-quotes ('')")
        void normalize_escapedSingleQuote_replacesCorrectly() {
            String sql = "SELECT * FROM comments WHERE content = 'It''s a great day!'";
            String expected = "SELECT * FROM comments WHERE content = ?";
            assertEquals(expected, SqlNormalizer.normalize(sql));
        }

        @Test
        @DisplayName("Should replace empty string literals ('') with ?")
        void normalize_emptyStringLiteral_replacesWithQuestionMark() {
            String sql = "UPDATE users SET nickname = '' WHERE id = 1";
            String expected = "UPDATE users SET nickname = ? WHERE id = ?";
            assertEquals(expected, SqlNormalizer.normalize(sql));
        }
    }

    @Nested
    @DisplayName("Numeric Literal Replacement Tests")
    class NumericLiteralTests {

        @Test
        @DisplayName("Should mask integer literals")
        void normalize_integerLiteral_replacesWithQuestionMark() {
            String sql = "SELECT * FROM orders WHERE id = 1042 AND status_id = 3";
            String expected = "SELECT * FROM orders WHERE id = ? AND status_id = ?";
            assertEquals(expected, SqlNormalizer.normalize(sql));
        }

        @Test
        @DisplayName("Should mask decimal and floating-point literals")
        void normalize_decimalLiteral_replacesWithQuestionMark() {
            String sql = "SELECT * FROM products WHERE price > 19.99 AND weight <= 0.5";
            String expected = "SELECT * FROM products WHERE price > ? AND weight <= ?";
            assertEquals(expected, SqlNormalizer.normalize(sql));
        }

        @Test
        @DisplayName("Should mask scientific notation numbers")
        void normalize_scientificNotation_replacesWithQuestionMark() {
            String sql = "SELECT * FROM metrics WHERE value > 1.23e-4 OR score = 5E+10";
            String expected = "SELECT * FROM metrics WHERE value > ? OR score = ?";
            assertEquals(expected, SqlNormalizer.normalize(sql));
        }

        @Test
        @DisplayName("Should mask negative and explicitly signed numbers")
        void normalize_signedNumbers_replacesWithQuestionMark() {
            String sql = "UPDATE accounts font SET balance = -150.75 WHERE id = +42";
            String expected = "UPDATE accounts font SET balance = ? WHERE id = ?";
            assertEquals(expected, SqlNormalizer.normalize(sql));
        }

        @Test
        @DisplayName("Should NOT replace numbers embedded inside SQL identifiers/table names")
        void normalize_numbersInIdentifiers_preservesIdentifier() {
            String sql = "SELECT user_v2_id, table1.col2 FROM user_v2 WHERE id = 99";
            String expected = "SELECT user_v2_id, table1.col2 FROM user_v2 WHERE id = ?";
            assertEquals(expected, SqlNormalizer.normalize(sql));
        }
    }

    @Nested
    @DisplayName("Whitespace Collapsing & Trimming Tests")
    class WhitespaceTests {

        @Test
        @DisplayName("Should collapse consecutive spaces, tabs, and newlines to a single space")
        void normalize_multilineAndExcessWhitespace_collapsesToSingleSpace() {
            String sql = "SELECT   id,\n\t  name  \n FROM   users \t WHERE status = 'ACTIVE'";
            String expected = "SELECT id, name FROM users WHERE status = ?";
            assertEquals(expected, SqlNormalizer.normalize(sql));
        }

        @Test
        @DisplayName("Should trim leading and trailing whitespace")
        void normalize_leadingAndTrailingWhitespace_trimsCleanly() {
            String sql = "   DELETE FROM session WHERE expired = 1   ";
            String expected = "DELETE FROM session WHERE expired = ?";
            assertEquals(expected, SqlNormalizer.normalize(sql));
        }
    }

    @Nested
    @DisplayName("Complex Real-World Queries Tests")
    class ComplexQueryTests {

        @Test
        @DisplayName("Should correctly normalize complex IN clause with multiple literals")
        void normalize_inClauseWithLiterals_normalizesCleanly() {
            String sql = "SELECT * FROM products WHERE category_id IN (1, 2, 3) AND status = 'AVAILABLE'";
            String expected = "SELECT * FROM products WHERE category_id IN (?, ?, ?) AND status = ?";
            assertEquals(expected, SqlNormalizer.normalize(sql));
        }

        @Test
        @DisplayName("Should correctly normalize complex INSERT statements")
        void normalize_insertStatement_replacesAllValues() {
            String sql = "INSERT INTO audit_logs (user_id, action, attempt, points) VALUES (502, 'LOGIN_FAILED', 3, 12.5)";
            String expected = "INSERT INTO audit_logs (user_id, action, attempt, points) VALUES (?, ?, ?, ?)";
            assertEquals(expected, SqlNormalizer.normalize(sql));
        }

        @Test
        @DisplayName("Should handle string literals containing digits correctly")
        void normalize_stringLiteralContainingNumbers_masksAsSingleStringParam() {
            String sql = "SELECT * FROM orders WHERE code = 'ORD-2026-99' AND user_id = 42";
            String expected = "SELECT * FROM orders WHERE code = ? AND user_id = ?";
            assertEquals(expected, SqlNormalizer.normalize(sql));
        }
    }
}