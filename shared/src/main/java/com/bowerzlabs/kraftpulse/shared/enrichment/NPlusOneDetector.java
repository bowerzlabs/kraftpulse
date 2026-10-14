
package com.bowerzlabs.kraftpulse.shared.enrichment;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public final class NPlusOneDetector {

    private final int threshold;

    public NPlusOneDetector() {
        this(5);
    }

    public NPlusOneDetector(int threshold) {
        if (threshold < 2) {
            throw new IllegalArgumentException(
                    "Threshold must be at least 2"
            );
        }

        this.threshold = threshold;
    }

//    public List<NPlusOneWarning> detect(
//            List<QueryEvent> queriesInTrace
//    ) {
//        if (queriesInTrace == null || queriesInTrace.isEmpty()) {
//            return List.of();
//        }
//
//        Map<String, List<QueryEvent>> groups = queriesInTrace.stream()
//                .filter(Objects::nonNull)
//                .filter(query -> query.normalizedSql() != null)
//                .filter(query -> !query.normalizedSql().isBlank())
//                .collect(Collectors.groupingBy(
//                        QueryEvent::normalizedSql,
//                        LinkedHashMap::new,
//                        Collectors.toList()
//                ));
//
//        return groups.entrySet().stream()
//                .filter(entry -> entry.getValue().size() >= threshold)
//                .map(entry -> toWarning(
//                        entry.getKey(),
//                        entry.getValue()
//                ))
//                .toList();
//    }
//
//    private NPlusOneWarning toWarning(
//            String normalizedSql,
//            List<QueryEvent> occurrences
//    ) {
//        QueryEvent first = occurrences.get(0);
//
//        long totalDurationMs = occurrences.stream()
//                .mapToLong(QueryEvent::durationMs)
//                .sum();
//
//        return new NPlusOneWarning(
//                first.repositoryClass(),
//                first.repositoryMethod(),
//                normalizedSql,
//                occurrences.size(),
//                totalDurationMs
//        );
//    }

}
