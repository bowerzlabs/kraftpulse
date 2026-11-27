package com.bowerzlabs.kraftpulse.springboot.outbound;

import java.util.regex.Pattern;

/**
 * Best-effort URI template: numeric, UUID and long-hex path segments become {id}.
 * Only a heuristic. /users/john stays as is. The interceptors only ever see the expanded URL.
 */
final class UriNormalizer {

    private static final Pattern NUMERIC = Pattern.compile("\\d+");
    private static final Pattern UUID = Pattern.compile(
            "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");
    private static final Pattern LONG_HEX = Pattern.compile("[0-9a-fA-F]{16,}");

    private UriNormalizer() {
    }

    static String template(String url) {
        int query = url.indexOf('?');
        String base = query < 0 ? url : url.substring(0, query);
        int scheme = base.indexOf("://");
        int pathStart = scheme < 0 ? 0 : base.indexOf('/', scheme + 3);
        if (pathStart < 0) {
            return base;
        }
        String prefix = base.substring(0, pathStart);
        String[] segments = base.substring(pathStart).split("/", -1);
        for (int i = 0; i < segments.length; i++) {
            if (looksLikeId(segments[i])) {
                segments[i] = "{id}";
            }
        }
        return prefix + String.join("/", segments);
    }

    private static boolean looksLikeId(String s) {
        return !s.isEmpty()
                && (NUMERIC.matcher(s).matches() || UUID.matcher(s).matches() || LONG_HEX.matcher(s).matches());
    }
}