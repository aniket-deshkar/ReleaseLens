# Security

Repository paths are normalized and must contain Git metadata. When `RELEASELENS_WORKSPACE_ROOT` is configured, repository paths outside that boundary are rejected. ReleaseLens reads Git objects and source files; it does not execute repository code, Git hooks, scripts, or builds.

Provider keys are read from backend process environment variables. They are not stored in H2, returned by the REST API, or exposed to the browser. Jev and Luna calls are skipped unless enrichment is explicitly enabled and the corresponding key is present.

Evidence snippets are bounded in size before persistence or provider use. Analysis failures preserve a concise failure message in the stored policy reasons. Avoid placing secrets in repository source, configuration files, commit messages, or provider prompts.
