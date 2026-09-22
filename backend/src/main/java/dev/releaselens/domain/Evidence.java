package dev.releaselens.domain;

public record Evidence(String path, String revision, int startLine, int endLine, String symbol, String snippet) {
    public Evidence {
        if (path == null || path.isBlank() || revision == null || revision.isBlank() || startLine < 1 || endLine < startLine || snippet == null || snippet.isBlank()) {
            throw new IllegalArgumentException("Evidence requires a path, revision, valid lines and snippet");
        }
    }
}
