package dev.releaselens.domain;

import java.util.List;

public record ChangedFile(String path, Enums.ChangeKind kind, String oldPath, String baseBlob, String headBlob,
                          List<String> changedLines) {
    public ChangedFile { changedLines = changedLines == null ? List.of() : List.copyOf(changedLines); }
}
