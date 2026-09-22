package dev.releaselens.domain;

import java.util.List;

public record Finding(String id, Enums.FindingType type, String title, String description, Enums.Severity severity,
                      Evidence evidence, String symbol, List<String> relatedNodes) {
    public Finding {
        if (evidence == null) throw new IllegalArgumentException("No evidence = no finding");
        relatedNodes = relatedNodes == null ? List.of() : List.copyOf(relatedNodes);
    }
}
