package dev.releaselens.analysis;

import dev.releaselens.config.ReleaseLensProperties;
import dev.releaselens.domain.*;
import dev.releaselens.git.GitDiffService;
import org.springframework.stereotype.Service;

import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Service
public class DeterministicAnalyzer {
    private static final Pattern ANNOTATION = Pattern.compile("@(RestController|Controller|Service|Repository|Component|Configuration|GetMapping|PostMapping|PutMapping|PatchMapping|DeleteMapping|RequestMapping|PreAuthorize|PostAuthorize|Secured|Entity|Table|Transactional|ConfigurationProperties|Value|KafkaListener)\\b(?:\\s*\\([^)]*\\))?");
    private static final Pattern RECORD = Pattern.compile("\\brecord\\s+(\\w+)\\s*\\(([^)]*)\\)");
    private static final Pattern SQL = Pattern.compile("(?i)\\b(CREATE\\s+TABLE|DROP\\s+TABLE|DROP\\s+COLUMN|ADD\\s+COLUMN|ALTER\\s+COLUMN|RENAME\\s+TABLE|NOT\\s+NULL|FOREIGN\\s+KEY|CREATE\\s+INDEX)");
    private static final Pattern POM = Pattern.compile("<(dependency|module|version|artifactId|groupId|plugin)>\\s*([^<]+)");
    private final ReleaseLensPropertiesAdapter props;
    public DeterministicAnalyzer(ReleaseLensProperties properties) { this.props = new ReleaseLensPropertiesAdapter(properties); }
    public Output analyze(Path repo, GitDiffService.Snapshot snapshot) {
        List<AnalysisModels.SemanticChange> changes = new ArrayList<>(); List<Finding> findings = new ArrayList<>(); List<Graph.Node> nodes = new ArrayList<>(); List<Graph.Edge> edges = new ArrayList<>();
        for (ChangedFile file : snapshot.changedFiles()) {
            if (file.kind() == Enums.ChangeKind.REMOVED) continue;
            Path source = repo.resolve(file.path()); List<String> lines = file.changedLines();
            if (file.path().endsWith(".java")) inspectJava(file, source, snapshot.headSha(), lines, changes, findings, nodes, edges);
            else if (file.path().endsWith(".sql")) inspectSql(file, snapshot.headSha(), lines, changes, findings, nodes, edges);
            else if (file.path().equals("pom.xml") || file.path().endsWith("/pom.xml")) inspectPom(file, snapshot.headSha(), lines, changes, findings, nodes, edges);
            else if (file.path().matches(".*application\\.(yml|yaml|properties)$")) inspectConfig(file, snapshot.headSha(), lines, changes, findings, nodes, edges);
        }
        addTestGaps(repo, snapshot, findings);
        connectDiscoveredNodes(nodes, edges);
        return new Output(changes, findings, dedupe(nodes), dedupe(edges));
    }

    private void connectDiscoveredNodes(List<Graph.Node> nodes, List<Graph.Edge> edges) {
        for (int i = 0; i < nodes.size(); i++) {
            Graph.Node left = nodes.get(i);
            for (int j = i + 1; j < nodes.size(); j++) {
                Graph.Node right = nodes.get(j);
                if (!left.evidence().path().equals(right.evidence().path())) continue;
                Enums.EdgeType type = relation(left.type(), right.type());
                if (type == null) continue;
                Evidence evidence = left.evidence().startLine() <= right.evidence().startLine() ? left.evidence() : right.evidence();
                String id = "edge:" + left.id() + ":" + type.name().toLowerCase() + ":" + right.id();
                edges.add(new Graph.Edge(id, left.id(), right.id(), type, evidence));
            }
        }
    }

    private Enums.EdgeType relation(Enums.NodeType left, Enums.NodeType right) {
        if (left == Enums.NodeType.SERVICE && right == Enums.NodeType.API_ENDPOINT) return Enums.EdgeType.EXPOSES;
        if (left == Enums.NodeType.API_ENDPOINT && right == Enums.NodeType.DTO) return Enums.EdgeType.USES;
        if (left == Enums.NodeType.SERVICE && right == Enums.NodeType.EVENT) return Enums.EdgeType.PRODUCES;
        if (left == Enums.NodeType.EVENT && right == Enums.NodeType.DATABASE_TABLE) return Enums.EdgeType.WRITES;
        if (left == Enums.NodeType.CLASS && right == Enums.NodeType.CONFIG_KEY) return Enums.EdgeType.CONFIGURES;
        if (left == Enums.NodeType.DEPENDENCY && right == Enums.NodeType.SERVICE) return Enums.EdgeType.DEPENDS_ON;
        return null;
    }
    private void inspectJava(ChangedFile file, Path source, String rev, List<String> lines, List<AnalysisModels.SemanticChange> changes, List<Finding> findings, List<Graph.Node> nodes, List<Graph.Edge> edges) {
        String text = String.join("\n", lines); Matcher m = ANNOTATION.matcher(text); String current = file.path();
        while (m.find()) {
            String annotation = m.group(1); int line = lineAt(text, m.start()); Evidence ev = evidence(file.path(), rev, line, lines, annotation);
            Enums.NodeType nt = annotation.contains("Mapping") ? Enums.NodeType.API_ENDPOINT : annotation.equals("KafkaListener") ? Enums.NodeType.EVENT : annotation.equals("Entity") || annotation.equals("Table") ? Enums.NodeType.DATABASE_TABLE : annotation.equals("Service") ? Enums.NodeType.SERVICE : annotation.equals("ConfigurationProperties") || annotation.equals("Value") ? Enums.NodeType.CONFIG_KEY : Enums.NodeType.CLASS;
            String id = nt.name().toLowerCase()+":"+file.path()+":"+line; nodes.add(new Graph.Node(id, nt, annotation+" in "+file.path(), ev));
            if (annotation.equals("GetMapping") || annotation.equals("PostMapping") || annotation.equals("PutMapping") || annotation.equals("PatchMapping") || annotation.equals("DeleteMapping")) {
                changes.add(new AnalysisModels.SemanticChange("HTTP_MAPPING", annotation+" changed", ev, Enums.Severity.MEDIUM));
                if (annotation.equals("DeleteMapping")) findings.add(finding(file, rev, line, lines, Enums.FindingType.API_CONTRACT, "DELETE endpoint changed", "An HTTP delete mapping is part of the externally visible contract.", Enums.Severity.HIGH, id));
            }
            if (annotation.equals("PreAuthorize") || annotation.equals("PostAuthorize") || annotation.equals("Secured")) changes.add(new AnalysisModels.SemanticChange("SECURITY", annotation+" changed", ev, Enums.Severity.HIGH));
            if (annotation.equals("Entity") || annotation.equals("Table")) changes.add(new AnalysisModels.SemanticChange("PERSISTENCE", annotation+" changed", ev, Enums.Severity.HIGH));
            if (annotation.equals("KafkaListener")) changes.add(new AnalysisModels.SemanticChange("MESSAGING", "Kafka listener changed", ev, Enums.Severity.HIGH));
        }
        Matcher record = RECORD.matcher(text); while (record.find()) { int line=lineAt(text, record.start()); Evidence ev=evidence(file.path(),rev,line,lines,"record "+record.group(1)); String id="dto:"+file.path()+":"+record.group(1); nodes.add(new Graph.Node(id,Enums.NodeType.DTO,record.group(1),ev)); changes.add(new AnalysisModels.SemanticChange("CONTRACT", "Record component surface changed", ev, Enums.Severity.HIGH)); findings.add(finding(file,rev,line,lines,Enums.FindingType.API_CONTRACT,"Record DTO changed","A record component change can break consumers that deserialize this contract.",Enums.Severity.HIGH,id)); }
    }
    private void inspectSql(ChangedFile file,String rev,List<String> lines,List<AnalysisModels.SemanticChange> changes,List<Finding> findings,List<Graph.Node> nodes,List<Graph.Edge> edges) { String text=String.join("\n",lines); Matcher m=SQL.matcher(text); while(m.find()){int line=lineAt(text,m.start()); Evidence ev=evidence(file.path(),rev,line,lines,m.group(1)); changes.add(new AnalysisModels.SemanticChange("DATABASE",m.group(1)+" detected",ev,Enums.Severity.CRITICAL)); String id="db:"+file.path()+":"+line; nodes.add(new Graph.Node(id,Enums.NodeType.DATABASE_TABLE,m.group(1),ev)); if(m.group(1).toUpperCase().startsWith("DROP")) findings.add(finding(file,rev,line,lines,Enums.FindingType.DATABASE,"Destructive database migration","A destructive Flyway operation was detected in the changed migration.",Enums.Severity.CRITICAL,id)); }}
    private void inspectPom(ChangedFile file,String rev,List<String> lines,List<AnalysisModels.SemanticChange> changes,List<Finding> findings,List<Graph.Node> nodes,List<Graph.Edge> edges) { String text=String.join("\n",lines); Matcher m=POM.matcher(text); while(m.find()){int line=lineAt(text,m.start()); Evidence ev=evidence(file.path(),rev,line,lines,m.group(1)); changes.add(new AnalysisModels.SemanticChange("MAVEN",m.group(1)+" changed",ev,Enums.Severity.MEDIUM)); nodes.add(new Graph.Node("dependency:"+file.path()+":"+line,Enums.NodeType.DEPENDENCY,m.group(2).trim(),ev)); }}
    private void inspectConfig(ChangedFile file,String rev,List<String> lines,List<AnalysisModels.SemanticChange> changes,List<Finding> findings,List<Graph.Node> nodes,List<Graph.Edge> edges) { for(int i=0;i<lines.size();i++){String line=lines.get(i); if(line.contains(":" )||line.contains("=")){ Evidence ev=evidence(file.path(),rev,i+1,lines,line.trim()); String id="config:"+file.path()+":"+(i+1); nodes.add(new Graph.Node(id,Enums.NodeType.CONFIG_KEY,line.trim(),ev)); changes.add(new AnalysisModels.SemanticChange("CONFIGURATION","Configuration key changed",ev,Enums.Severity.MEDIUM)); } }}
    private void addTestGaps(Path repo,GitDiffService.Snapshot snapshot,List<Finding> findings){ for(ChangedFile f:snapshot.changedFiles()){ if(f.path().endsWith(".java") && !f.path().contains("/test/") && !f.path().startsWith("test/")){ String stem=f.path().substring(f.path().lastIndexOf('/')+1).replace(".java",""); boolean exists; try(Stream<Path> s=Files.walk(repo)){ exists=s.anyMatch(p->p.getFileName().toString().equals(stem+"Test.java")||p.getFileName().toString().equals(stem+"Tests.java")); }catch(Exception e){exists=false;} if(!exists){ Evidence ev=evidence(f.path(),snapshot.headSha(),1,f.changedLines(),f.path()); findings.add(new Finding("test-gap:"+f.path(),Enums.FindingType.TEST_GAP,"No adjacent test detected","No test file matching the changed production class was found by the deterministic fixture scan.",Enums.Severity.LOW,ev,stem,List.of())); } } }}
    private Finding finding(ChangedFile f,String rev,int line,List<String> lines,Enums.FindingType type,String title,String desc,Enums.Severity sev,String node){return new Finding(type.name().toLowerCase()+":"+f.path()+":"+line,type,title,desc,sev,evidence(f.path(),rev,line,lines,title),node,List.of(node));}
    private Evidence evidence(String path,String rev,int line,List<String> lines,String symbol){int s=Math.max(0,line-1), e=Math.min(lines.size(),s+4); String snippet=String.join("\n",lines.subList(s,e)); if(snippet.isBlank()) snippet=symbol; return new Evidence(path,rev,Math.max(1,line),Math.max(1,e),symbol,snippet.substring(0,Math.min(snippet.length(),props.maxEvidenceChars())));}
    private int lineAt(String text,int offset){return (int)text.substring(0,offset).chars().filter(c->c=='\n').count()+1;}
    private <T> List<T> dedupe(List<T> values){return values.stream().distinct().toList();}
    public record Output(List<AnalysisModels.SemanticChange> changes,List<Finding> findings,List<Graph.Node> nodes,List<Graph.Edge> edges){}
    private record ReleaseLensPropertiesAdapter(ReleaseLensProperties p){int maxEvidenceChars(){return p.maxEvidenceChars();}}
}
