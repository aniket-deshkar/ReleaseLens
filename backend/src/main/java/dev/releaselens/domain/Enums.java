package dev.releaselens.domain;

public final class Enums {
    private Enums() {}
    public enum ChangeKind { MODIFIED, ADDED, REMOVED, RENAMED }
    public enum FindingType { API_CONTRACT, SECURITY, DATABASE, CONFIGURATION, MESSAGING, DEPENDENCY, TEST_GAP, SEMANTIC }
    public enum Severity { INFO, LOW, MEDIUM, HIGH, CRITICAL }
    public enum NodeType { SERVICE, CLASS, API_ENDPOINT, DTO, EVENT, KAFKA_TOPIC, DATABASE_TABLE, CONFIG_KEY, DEPENDENCY, TEST }
    public enum EdgeType { CALLS, EXPOSES, USES, PRODUCES, CONSUMES, READS, WRITES, CONFIGURES, DEPENDS_ON, TESTS }
    public enum PolicyStatus { READY, REVIEW_REQUIRED, BLOCKED, INCOMPLETE }
}
