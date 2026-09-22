# Architecture

ReleaseLens is a Spring Boot backend plus a Next.js developer-tool UI. The backend owns repository access, analysis, graph construction, policy, persistence, and provider adapters. The browser receives analysis records and never receives API keys.

The pipeline is strictly ordered: JGit snapshot, deterministic analyzers, graph and blast radius, evidence aggregation, optional Jev decisions, Java policy, optional Luna explanation. External models are consumers of bounded evidence and cannot create facts.
