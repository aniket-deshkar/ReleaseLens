package dev.releaselens.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "releaselens")
public record ReleaseLensProperties(
        boolean aiEnabled,
        String workspaceRoot,
        int maxEvidenceChars,
        int maxTraversalDepth,
        double humanReviewThreshold,
        double breakingChangeThreshold,
        double downstreamFailureThreshold,
        int releaseRiskReviewScore) {
    public ReleaseLensProperties {
        maxEvidenceChars = maxEvidenceChars <= 0 ? 2400 : maxEvidenceChars;
        maxTraversalDepth = maxTraversalDepth <= 0 ? 8 : maxTraversalDepth;
        humanReviewThreshold = humanReviewThreshold <= 0 ? .80 : humanReviewThreshold;
        breakingChangeThreshold = breakingChangeThreshold <= 0 ? .85 : breakingChangeThreshold;
        downstreamFailureThreshold = downstreamFailureThreshold <= 0 ? .85 : downstreamFailureThreshold;
        releaseRiskReviewScore = releaseRiskReviewScore <= 0 ? 3 : releaseRiskReviewScore;
    }
}
