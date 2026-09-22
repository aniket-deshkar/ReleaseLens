package dev.releaselens.domain;

import java.util.List;

public final class Graph {
    private Graph() {}
    public record Node(String id, Enums.NodeType type, String label, Evidence evidence) {}
    public record Edge(String id, String from, String to, Enums.EdgeType type, Evidence evidence) {}
    public record Path(List<String> nodeIds, List<String> edgeIds, List<Evidence> evidence) {
        public Path { nodeIds = List.copyOf(nodeIds); edgeIds = List.copyOf(edgeIds); evidence = List.copyOf(evidence); }
    }
}
