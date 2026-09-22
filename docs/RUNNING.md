# Running and configuration

ReleaseLens requires Java 27, Maven 3.6.3 or later, and Node 20.9 or later. Maven resolves backend dependencies from Maven Central. H2 uses the configured file-backed datasource, and Flyway creates the schema during startup.

Start the backend with `mvn -pl backend spring-boot:run` and the frontend with `npm --prefix frontend run dev`. Build the backend with `mvn -pl backend package`; run frontend checks with `npm --prefix frontend run typecheck`, `npm --prefix frontend run lint`, and `npm --prefix frontend run build`.

The provider configuration reference is `.env.example`. It is not loaded automatically. Set variables in the backend process environment when enabling providers:

| Variable | Purpose |
| --- | --- |
| `RELEASELENS_AI_ENABLED` | Enables Jev and Luna enrichment when `true` |
| `JEV_API_KEY` | Authenticates the Jev adapter |
| `OPENAI_API_KEY` | Authenticates the Spring AI OpenAI model adapter |
| `SPRING_AI_MODEL_CHAT` | Selects the Spring AI chat provider; use `openai` when enabling Luna |
| `OPENAI_MODEL` | Selects the chat model; defaults to `gpt-5.6-luna` |
| `RELEASELENS_WORKSPACE_ROOT` | Restricts repository paths to a configured directory |

No container runtime, external database server, queue, graph database, cloud service, or provider key is required for deterministic analysis.
