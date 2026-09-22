# Architecture

ReleaseLens has a Spring Boot backend and a Next.js interface. The backend owns repository access, analysis, policy, persistence, and provider communication. The interface displays analysis records returned by the REST API and has no access to repository credentials or provider keys.

## Backend flow

1. `AnalysisController` accepts a repository path and Git base and head references.
2. `AnalysisCoordinator` records a queued analysis in H2 and runs the analysis on the configured virtual-thread executor.
3. `RepositoryPathGuard` checks that the path exists, contains Git metadata, and falls within `RELEASELENS_WORKSPACE_ROOT` when that boundary is configured.
4. `GitDiffService` resolves both commits with JGit and reads changed content from Git blobs rather than from the checkout.
5. `DeterministicAnalyzer` emits semantic changes, findings, graph nodes, and graph edges only when it can attach `Evidence`.
6. `BlastRadiusEngine` walks evidence-backed graph edges with a configurable depth limit.
7. `JevClient` optionally returns typed decision data. `ReleasePolicy` applies deterministic severity and configured probability thresholds.
8. `LunaExplainer` optionally produces an engineering summary from the existing evidence, decisions, and policy outcome.
9. The completed result is stored as JSON in the `analyses` H2 table and returned by the API.

## Data boundaries

The browser sends only the repository path and revision references. It receives analysis results, including evidence snippets, policy reasons, and optional summaries. Provider credentials are read only from backend process environment variables. Provider calls are skipped unless `RELEASELENS_AI_ENABLED` is true and the corresponding key is present.

## Persistence

Flyway creates the `analyses` table. Each row stores the analysis identifier, lifecycle state, creation time, and serialized result payload. H2 runs in file-backed mode through the configured datasource URL.

## Extension points

- Add deterministic rules in `DeterministicAnalyzer` and cover them with focused tests and fixtures.
- Add evidence-backed relationship rules before adding graph traversal behavior.
- Keep release decisions in `ReleasePolicy`; providers may supply inputs but do not own release outcomes.
- Keep Jev behind `JevClient` and GPT-5.6 Luna behind `LunaExplainer`.
