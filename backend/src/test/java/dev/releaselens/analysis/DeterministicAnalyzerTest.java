package dev.releaselens.analysis;

import dev.releaselens.config.ReleaseLensProperties;
import dev.releaselens.domain.Enums;
import dev.releaselens.git.GitDiffService;
import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class DeterministicAnalyzerTest {
  @Test void migrationFindingHasEvidence(){var a=new DeterministicAnalyzer(new ReleaseLensProperties(false,".",1000,8,.8,.85,.85,3));var file=new dev.releaselens.domain.ChangedFile("db/V2__drop.sql",Enums.ChangeKind.MODIFIED,null,"a","b",List.of("DROP TABLE orders;"));var out=a.analyze(Path.of("."),new GitDiffService.Snapshot("base","head",List.of(file)));assertThat(out.findings()).anyMatch(f->f.type()==Enums.FindingType.DATABASE&&f.evidence().path().equals(file.path()));}
  @Test void noEvidenceCannotBeConstructed(){org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,()->new dev.releaselens.domain.Finding("x",Enums.FindingType.SEMANTIC,"x","x",Enums.Severity.LOW,null,"x",List.of()));}
}
