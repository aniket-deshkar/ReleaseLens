package dev.releaselens.policy;

import dev.releaselens.config.ReleaseLensProperties;
import dev.releaselens.domain.*;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class ReleasePolicy {
    private final ReleaseLensProperties props;
    public ReleasePolicy(ReleaseLensProperties props){this.props=props;}
    public AnalysisModels.PolicyOutcome decide(List<Finding> findings,List<AnalysisModels.JevDecision> decisions){
        List<String> reasons=new ArrayList<>(); int score=0; boolean blocked=false;
        for(Finding f:findings){ if(f.severity()==Enums.Severity.CRITICAL){blocked=true; reasons.add(f.title()+" ["+f.evidence().path()+":"+f.evidence().startLine()+"]"); score=Math.max(score,4);} else if(f.severity()==Enums.Severity.HIGH){score=Math.max(score,3); reasons.add(f.title());} else if(f.severity()==Enums.Severity.MEDIUM) score=Math.max(score,2); }
        boolean review=score>=props.releaseRiskReviewScore();
        for(AnalysisModels.JevDecision d:decisions){ if(d.noul()!=null && d.noul()>=props.humanReviewThreshold()){review=true;reasons.add(d.key()+" probability="+d.noul());} if(d.key().equals("breakingChange")&&d.noul()!=null&&d.noul()>=props.breakingChangeThreshold()){review=true;reasons.add("breaking change probability exceeded threshold");} if(d.key().equals("downstreamFailure")&&d.noul()!=null&&d.noul()>=props.downstreamFailureThreshold()){review=true;reasons.add("downstream failure probability exceeded threshold");} }
        return new AnalysisModels.PolicyOutcome(blocked?Enums.PolicyStatus.BLOCKED:(review?Enums.PolicyStatus.REVIEW_REQUIRED:Enums.PolicyStatus.READY),score,review,reasons.stream().distinct().toList());
    }
}
