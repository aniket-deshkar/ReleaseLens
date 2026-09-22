# Security

Repository paths are canonicalized and constrained by `RELEASELENS_WORKSPACE_ROOT` when configured. ReleaseLens reads Git objects and source files but never runs repository code, hooks, scripts, or builds. It rejects paths outside the workspace and does not expose provider keys to the browser. Evidence and provider payloads are bounded; secrets are not logged or persisted.
