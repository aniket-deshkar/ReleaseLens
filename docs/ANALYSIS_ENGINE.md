# Analysis engine

JGit resolves the requested base and head references, detects additions, removals, modifications, and renames, and reads the relevant object content from the selected commit. This keeps source evidence tied to the requested revisions even when the checkout contains unrelated edits.

## Supported deterministic signals

Java files are inspected for Spring stereotypes and mappings, security annotations, transactions, persistence annotations, configuration annotations, and Kafka listeners. Record declarations are treated as DTO contract surfaces. Maven POM files contribute dependency and plugin changes. Application configuration files contribute changed keys. SQL migration files contribute supported DDL operations.

`DROP TABLE` and related destructive DDL produce critical database findings. `DeleteMapping` and record DTO changes produce API contract findings. Changed production Java files without a matching test filename produce a low-severity test-gap finding.

## Evidence model

Every semantic change, finding, graph node, and graph edge is derived from `Evidence(path, revision, startLine, endLine, symbol, snippet)`. Snippets are bounded by `releaselens.max-evidence-chars`. The `Finding` type rejects null evidence, which enforces the no-evidence rule at construction time.

## Graph and blast radius

The analyzer creates supported relationships only when the related declarations appear in the same changed file and both declarations have source evidence. `BlastRadiusEngine` traverses those edges without revisiting nodes and stops at `releaselens.max-traversal-depth`. Returned paths contain node IDs, edge IDs, and edge evidence.

## Deliberate limits

The engine does not infer runtime behavior from reflection, generated code, dependency injection assembled outside the inspected source, or database-specific SQL semantics. Those conditions may deserve review, but ReleaseLens does not emit a finding without a supported evidence path.
