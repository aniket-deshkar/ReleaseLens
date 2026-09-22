package dev.releaselens.git;

import dev.releaselens.config.ReleaseLensProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RepositoryPathGuardTest {
    @Test
    void acceptsRepositoryMetadataWithoutRealPathResolution(@TempDir Path temp) throws Exception {
        Files.createDirectory(temp.resolve(".git"));
        var guard = new RepositoryPathGuard(new ReleaseLensProperties(false, "", 1000, 8, .8, .85, .85, 3));
        assertThat(guard.validate(temp.toString())).isEqualTo(temp.toAbsolutePath().normalize());
    }

    @Test
    void rejectsDirectoryWithoutGitMetadata(@TempDir Path temp) {
        var guard = new RepositoryPathGuard(new ReleaseLensProperties(false, "", 1000, 8, .8, .85, .85, 3));
        assertThatThrownBy(() -> guard.validate(temp.toString()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Path is not a Git repository");
    }
}
