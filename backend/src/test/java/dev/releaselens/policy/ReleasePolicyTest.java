package dev.releaselens.policy;

import dev.releaselens.config.ReleaseLensProperties;
import dev.releaselens.domain.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class ReleasePolicyTest {
  private final ReleasePolicy policy = new ReleasePolicy(new ReleaseLensProperties(false, ".", 1000, 8,.8,.85,.85,3));
  @Test void criticalEvidenceBlocks(){var e=new Evidence("x.sql","abc",1,1,"DROP","DROP TABLE x");var f=new Finding("f",Enums.FindingType.DATABASE,"drop","destructive",Enums.Severity.CRITICAL,e,"x",List.of());assertThat(policy.decide(List.of(f),List.of()).status()).isEqualTo(Enums.PolicyStatus.BLOCKED);}
  @Test void highFindingRequiresReview(){var e=new Evidence("X.java","abc",1,1,"@GetMapping","@GetMapping");var f=new Finding("f",Enums.FindingType.API_CONTRACT,"api","contract",Enums.Severity.HIGH,e,"x",List.of());assertThat(policy.decide(List.of(f),List.of()).status()).isEqualTo(Enums.PolicyStatus.REVIEW_REQUIRED);}
}
