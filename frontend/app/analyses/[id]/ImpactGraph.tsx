"use client";

import { Background, Controls, MiniMap, ReactFlow } from "@xyflow/react";
import "@xyflow/react/dist/style.css";
import type { Analysis } from "../../../lib/types";

export function ImpactGraph({ analysis }: { analysis: Analysis }) {
  const nodes = analysis.nodes.map((node, index) => ({
    id: node.id,
    data: { label: `${node.type}\n${node.label}` },
    position: { x: (index % 3) * 260, y: Math.floor(index / 3) * 120 },
    style: { background: "#10233a", color: "#dff7ff", border: "1px solid #2e7188", borderRadius: 10, padding: 10, width: 220, whiteSpace: "pre-line" as const },
  }));
  const edges = analysis.edges.map((edge) => ({ id: edge.id, source: edge.from, target: edge.to, label: edge.type, style: { stroke: "#64d6c4" }, labelStyle: { fill: "#b7d9df", fontSize: 10 } }));
  if (!nodes.length) return <div className="panel p-5 muted">No graph nodes were discovered from changed files.</div>;
  return <div className="panel h-[430px] overflow-hidden p-2"><ReactFlow nodes={nodes} edges={edges} fitView><MiniMap nodeColor="#2e7188" /><Controls /><Background color="#214052" gap={18} /></ReactFlow></div>;
}
