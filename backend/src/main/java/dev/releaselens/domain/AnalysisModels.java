package dev.releaselens.domain;

import java.util.List;
import java.util.Map;

public final class AnalysisModels {
    private AnalysisModels() {}
    public record SemanticChange(String category, String description, Evidence evidence, Enums.Severity severity) {}
    public record JevDecision(String key, String type, String choice, Double score, Double noul,
                              Map<String, Double> probabilities, Double confidence, String model) {}
    public record PolicyOutcome(Enums.PolicyStatus status, int riskScore, boolean humanReview, List<String> reasons) {}
    public record EngineeringSummary(String headline, List<String> impacts, List<String> validation, String limitations) {}
    public record AnalysisResult(String id, String baseRef, String headRef, List<ChangedFile> changedFiles,
                                 List<SemanticChange> changes, List<Finding> findings, List<Graph.Node> nodes,
                                 List<Graph.Edge> edges, List<Graph.Path> paths, List<JevDecision> decisions,
                                 PolicyOutcome policy, EngineeringSummary summary, boolean aiEnriched, String status) {}
}
