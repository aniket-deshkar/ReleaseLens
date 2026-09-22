# Local setup

Use Java 27, Maven 3.6.3 or later, and Node 20.9 or later. Maven resolves all backend dependencies from Maven Central. The application uses a file-backed H2 database under `data/`; Flyway creates its schema on first start. Set environment variables from `.env.example` in the server process when enabling providers; the scripts do not load dotenv files. No Docker, WSL, database server, queue, or cloud infrastructure is required.
