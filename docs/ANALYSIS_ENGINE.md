# Analysis engine

JGit resolves base and head refs and detects additions, removals, modifications, and renames. Changed-file content is read from the selected Git blob, so evidence is tied to the requested revision even when the working tree has local edits. Java source is scanned for supported Spring, security, persistence, transaction, messaging, and contract declarations. POM, configuration, and Flyway files use explicit deterministic patterns.

Each finding stores `Evidence(path, revision, startLine, endLine, symbol, snippet)`. The graph stores evidence on nodes and edges. Traversal is cycle-safe and depth-bounded; paths retain their node IDs, edge IDs, and evidence list.
