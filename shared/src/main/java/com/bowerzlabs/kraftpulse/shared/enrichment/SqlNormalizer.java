
package com.bowerzlabs.kraftpulse.shared.enrichment;

import java.util.regex.Pattern;

public final class SqlNormalizer {

    private static final Pattern STRING_LITERAL =
            Pattern.compile("'(?:''|[^'])*'");

    private static final Pattern NUMBER_LITERAL =
            Pattern.compile(
                    "(?<![\\w.])[-+]?(?:\\d+(?:\\.\\d*)?|\\.\\d+)"
                            + "(?:[eE][-+]?\\d+)?(?![\\w.])"
            );

    private static final Pattern WHITESPACE =
            Pattern.compile("\\s+");

    private SqlNormalizer() {
    }

    public static String normalize(String sql) {
        if (sql == null || sql.isBlank()) {
            return "";
        }

        String normalized = STRING_LITERAL
                .matcher(sql)
                .replaceAll("?");

        normalized = NUMBER_LITERAL
                .matcher(normalized)
                .replaceAll("?");

        return WHITESPACE
                .matcher(normalized)
                .replaceAll(" ")
                .trim();
    }
}
