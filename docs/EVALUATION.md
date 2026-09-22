# Evaluation

Evaluation fixtures use committed base and head revisions. Each fixture should assert the finding type, evidence path and line range, graph relationships, blast-radius paths, and release policy outcome. Provider-based evaluation must remain separate from deterministic rule evaluation because provider output cannot create facts.

Use threshold-boundary fixtures for human-review, breaking-change, and downstream-failure probabilities. Exercise offline operation with provider enrichment disabled and no provider keys. Publish accuracy or coverage figures only when they are calculated from recorded fixture results.
