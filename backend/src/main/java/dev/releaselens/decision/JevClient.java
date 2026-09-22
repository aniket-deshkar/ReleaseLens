package dev.releaselens.decision;

import dev.releaselens.domain.AnalysisModels;
import java.util.List;

public interface JevClient { List<AnalysisModels.JevDecision> decide(Object state); }
