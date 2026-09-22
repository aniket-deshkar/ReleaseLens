package dev.releaselens.analysis;

import tools.jackson.databind.ObjectMapper;
import dev.releaselens.domain.*;
import dev.releaselens.decision.JevClient;
import dev.releaselens.explanation.LunaExplainer;
import dev.releaselens.git.*;
import dev.releaselens.graph.BlastRadiusEngine;
import dev.releaselens.persistence.*;
import dev.releaselens.policy.ReleasePolicy;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class AnalysisCoordinator {
    private final RepositoryPathGuard guard; private final GitDiffService git; private final DeterministicAnalyzer analyzer; private final BlastRadiusEngine blast; private final JevClient jev; private final ReleasePolicy policy; private final LunaExplainer luna; private final AnalysisRepository repository; private final ObjectMapper mapper; private final AsyncTaskExecutor executor;
    public AnalysisCoordinator(RepositoryPathGuard guard,GitDiffService git,DeterministicAnalyzer analyzer,BlastRadiusEngine blast,JevClient jev,ReleasePolicy policy,LunaExplainer luna,AnalysisRepository repository,ObjectMapper mapper,@Qualifier("analysisExecutor") AsyncTaskExecutor executor){this.guard=guard;this.git=git;this.analyzer=analyzer;this.blast=blast;this.jev=jev;this.policy=policy;this.luna=luna;this.repository=repository;this.mapper=mapper;this.executor=executor;}
    public String start(String repo,String base,String head){String id=UUID.randomUUID().toString(); save(new AnalysisModels.AnalysisResult(id,base,head,List.of(),List.of(),List.of(),List.of(),List.of(),List.of(),List.of(),new AnalysisModels.PolicyOutcome(Enums.PolicyStatus.INCOMPLETE,0,false,List.of()),null,false,"QUEUED")); executor.execute(() -> execute(id,repo,base,head)); return id;}
    private void execute(String id,String repo,String base,String head){try{Path p=guard.validate(repo); save(status(id,"RUNNING")); GitDiffService.Snapshot snapshot=git.snapshot(p,base,head); DeterministicAnalyzer.Output output=analyzer.analyze(p,snapshot); List<Graph.Path> paths=blast.paths(output.nodes(),output.edges()); List<AnalysisModels.JevDecision> decisions=jev.decide(Map.of("changes",output.changes(),"findings",output.findings(),"impactPaths",paths)); AnalysisModels.PolicyOutcome po=policy.decide(output.findings(),decisions); Optional<AnalysisModels.EngineeringSummary> summary=luna.explain(output.findings(),decisions,po); save(new AnalysisModels.AnalysisResult(id,snapshot.baseSha(),snapshot.headSha(),snapshot.changedFiles(),output.changes(),output.findings(),output.nodes(),output.edges(),paths,decisions,po,summary.orElse(null),summary.isPresent(),"COMPLETED"));}catch(Exception e){save(new AnalysisModels.AnalysisResult(id,base,head,List.of(),List.of(),List.of(),List.of(),List.of(),List.of(),List.of(),new AnalysisModels.PolicyOutcome(Enums.PolicyStatus.INCOMPLETE,0,false,List.of(e.getMessage()==null?"analysis failed":e.getMessage())),null,false,"FAILED"));}}
    public AnalysisModels.AnalysisResult get(String id){try{return mapper.readValue(repository.findById(id).orElseThrow().getPayload(),AnalysisModels.AnalysisResult.class);}catch(Exception e){throw new NoSuchElementException(id);}}
    private AnalysisModels.AnalysisResult status(String id,String status){AnalysisModels.AnalysisResult r=get(id);return new AnalysisModels.AnalysisResult(r.id(),r.baseRef(),r.headRef(),r.changedFiles(),r.changes(),r.findings(),r.nodes(),r.edges(),r.paths(),r.decisions(),r.policy(),r.summary(),r.aiEnriched(),status);}
    private synchronized void save(AnalysisModels.AnalysisResult r){try{AnalysisEntity e=repository.findById(r.id()).orElse(new AnalysisEntity(r.id(),r.status(),mapper.writeValueAsString(r)));e.setStatus(r.status());e.setPayload(mapper.writeValueAsString(r));repository.save(e);}catch(Exception ex){throw new IllegalStateException("Unable to persist analysis",ex);}}
}
