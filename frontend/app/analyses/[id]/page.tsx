"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import { getAnalysis } from "../../../lib/api";
import type { Analysis, Finding } from "../../../lib/types";
import { ImpactGraph } from "./ImpactGraph";

function FindingCard({ f }: { f: Finding }) {
  return <article className="panel p-4"><div className="flex items-start justify-between gap-4"><div><p className="muted text-xs uppercase">{f.type}</p><h3 className="mt-1 font-semibold">{f.title}</h3><p className="muted mt-2 text-sm">{f.description}</p></div><span className={`badge ${f.severity.toLowerCase()}`}>{f.severity}</span></div><pre className="mt-3 overflow-auto rounded bg-slate-950 p-3 text-xs text-cyan-100">{f.evidence.path}:{f.evidence.startLine}{"\n"}{f.evidence.snippet}</pre></article>;
}

export default function AnalysisPage() {
  const p = useParams<{ id: string }>();
  const [analysis, setAnalysis] = useState<Analysis>();
  const [error, setError] = useState("");
  useEffect(() => {
    let timer: ReturnType<typeof setInterval> | undefined;
    const load = async () => { try { const value = await getAnalysis(p.id); setAnalysis(value); if (value.status !== "COMPLETED" && value.status !== "FAILED") timer = setInterval(load, 1500); } catch (e) { setError(e instanceof Error ? e.message : "Unable to load"); } };
    void load();
    return () => { if (timer) clearInterval(timer); };
  }, [p.id]);
  if (error) return <main className="p-8 text-rose-300">{error}</main>;
  if (!analysis) return <main className="p-8 muted">Running deterministic analysis...</main>;
  const badge = analysis.policy.status === "BLOCKED" ? "critical" : analysis.policy.status === "REVIEW_REQUIRED" ? "high" : "low";
  return <main className="mx-auto max-w-7xl p-8"><header className="flex flex-wrap items-end justify-between gap-4"><div><p className="muted text-sm">Analysis {analysis.id}</p><h1 className="mt-2 text-3xl font-bold">Release readiness</h1></div><span className={`badge ${badge}`}>{analysis.policy.status}</span></header><section className="mt-6 grid gap-4 md:grid-cols-4"><div className="panel p-4"><p className="muted text-sm">Risk score</p><p className="mt-2 text-3xl font-bold">{analysis.policy.riskScore}/4</p></div><div className="panel p-4"><p className="muted text-sm">Findings</p><p className="mt-2 text-3xl font-bold">{analysis.findings.length}</p></div><div className="panel p-4"><p className="muted text-sm">Changed files</p><p className="mt-2 text-3xl font-bold">{analysis.changedFiles.length}</p></div><div className="panel p-4"><p className="muted text-sm">AI enrichment</p><p className="mt-2 font-semibold">{analysis.aiEnriched ? "Luna summary" : "Offline / deterministic"}</p></div></section><section className="mt-6"><div className="mb-3 flex items-end justify-between"><div><h2 className="text-xl font-semibold">Impact graph</h2><p className="muted text-sm">Relationships are shown only when the deterministic scan has source evidence.</p></div><span className="muted text-sm">{analysis.nodes.length} / {analysis.edges.length} (nodes / edges)</span></div><ImpactGraph analysis={analysis} /></section><div className="mt-6 grid gap-6 lg:grid-cols-[1.3fr_.7fr]"><section><h2 className="mb-3 text-xl font-semibold">Evidence-backed findings</h2><div className="space-y-3">{analysis.findings.length ? analysis.findings.map((f) => <FindingCard key={f.id} f={f} />) : <div className="panel p-5 muted">No evidence-backed findings were produced.</div>}</div></section><aside className="space-y-4"><div className="panel p-5"><h2 className="font-semibold">Why this outcome?</h2><ul className="muted mt-3 list-disc space-y-2 pl-5 text-sm">{analysis.policy.reasons.length ? analysis.policy.reasons.map((reason, i) => <li key={i}>{reason}</li>) : <li>No policy overrides.</li>}</ul></div>{analysis.summary && <div className="panel p-5"><h2 className="font-semibold">Engineering summary</h2><p className="mt-3">{analysis.summary.headline}</p><ul className="muted mt-3 list-disc space-y-2 pl-5 text-sm">{analysis.summary.impacts.map((impact, i) => <li key={i}>{impact}</li>)}</ul></div>}</aside></div></main>;
}
