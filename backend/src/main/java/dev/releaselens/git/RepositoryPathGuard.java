package dev.releaselens.git;

import dev.releaselens.config.ReleaseLensProperties;
import org.springframework.stereotype.Component;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class RepositoryPathGuard {
    private final ReleaseLensProperties properties;
    public RepositoryPathGuard(ReleaseLensProperties properties) { this.properties = properties; }
    public Path validate(String requested) {
        if (requested == null || requested.isBlank()) throw new IllegalArgumentException("repositoryPath is required");
        try {
            // Normalize without toRealPath: Java's Windows file provider can reject real-path
            // resolution in restricted local runtimes even when ordinary repository access works.
            Path candidate = Path.of(requested).toAbsolutePath().normalize();
            if (!Files.exists(candidate)) throw new IllegalArgumentException("Repository path does not exist");
            if (!Files.isDirectory(candidate.resolve(".git")) && !Files.isRegularFile(candidate.resolve(".git"))) throw new IllegalArgumentException("Path is not a Git repository");
            if (properties.workspaceRoot() != null && !properties.workspaceRoot().isBlank()) {
                Path allowed = Path.of(properties.workspaceRoot()).toAbsolutePath().normalize();
                if (!candidate.startsWith(allowed)) throw new IllegalArgumentException("Repository is outside RELEASELENS_WORKSPACE_ROOT");
            }
            return candidate;
        } catch (RuntimeException e) {
            if (e instanceof IllegalArgumentException iae) throw iae;
            throw new IllegalArgumentException("Repository path cannot be resolved", e);
        }
    }
}
