# Decision engine

`JevHttpClient` calls Jev once per completed deterministic analysis when provider enrichment is enabled and `JEV_API_KEY` is set. It sends the deterministic changes, findings, and impact paths with typed Choice, Score, and Noul questions for change category, release risk, breaking change, downstream failure, human review, contract testing, and rollback difficulty.

`ReleasePolicy` remains the release authority. Critical deterministic findings block release. High findings raise the risk score and can require review. Jev Noul probabilities can require review when they exceed the configured thresholds, but they cannot lower a deterministic finding severity or turn a block into an approval.

`LunaExplainer` runs only when enrichment is enabled and `OPENAI_API_KEY` is set. It receives evidence, Jev decisions, and the policy outcome through the Spring AI boundary configured for GPT-5.6 Luna. Its response is an optional engineering summary and does not alter deterministic findings, graph data, or policy status.
