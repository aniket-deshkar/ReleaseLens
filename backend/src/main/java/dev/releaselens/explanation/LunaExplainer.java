package dev.releaselens.explanation;

import dev.releaselens.config.ReleaseLensProperties;
import dev.releaselens.domain.AnalysisModels;
import dev.releaselens.domain.Finding;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class LunaExplainer {
    private final ReleaseLensProperties props; private final ObjectProvider<ChatClient.Builder> builders;
    public LunaExplainer(ReleaseLensProperties props,ObjectProvider<ChatClient.Builder> builders){this.props=props;this.builders=builders;}
    public Optional<AnalysisModels.EngineeringSummary> explain(List<Finding> findings,List<AnalysisModels.JevDecision> decisions,AnalysisModels.PolicyOutcome policy){
        if(!props.aiEnabled() || System.getenv("OPENAI_API_KEY")==null || System.getenv("OPENAI_API_KEY").isBlank()) return Optional.empty();
        ChatClient.Builder b=builders.getIfAvailable(); if(b==null)return Optional.empty();
        String evidence=findings.stream().map(f->f.id()+" | "+f.evidence().path()+":"+f.evidence().startLine()+" | "+f.title()+" | "+f.evidence().snippet()).limit(40).reduce("",(a,x)->a+x+"\n");
        String prompt="You are ReleaseLens engineering explainer. Use only the supplied evidence. Never invent files, lines, services, APIs, tests, dependencies, or relationships. Return a concise engineering summary with headline, impacts, validation, limitations. Policy="+policy.status()+"\nEvidence:\n"+evidence+"\nDecisions:"+decisions;
        try { Summary summary=b.build().prompt().system("Only cite supplied evidence.").user(prompt).call().entity(Summary.class); return Optional.of(new AnalysisModels.EngineeringSummary(summary.headline(),summary.impacts(),summary.validation(),summary.limitations())); } catch(Exception ignored){ return Optional.empty(); }
    }
    public record Summary(String headline,List<String> impacts,List<String> validation,String limitations){}
}
