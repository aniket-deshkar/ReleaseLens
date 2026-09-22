package dev.releaselens.graph;

import dev.releaselens.config.ReleaseLensProperties;
import dev.releaselens.domain.Graph;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class BlastRadiusEngine {
    private final ReleaseLensProperties props;
    public BlastRadiusEngine(ReleaseLensProperties props){this.props=props;}
    public List<Graph.Path> paths(List<Graph.Node> nodes,List<Graph.Edge> edges){
        Map<String,List<Graph.Edge>> out=new HashMap<>(); edges.forEach(e->out.computeIfAbsent(e.from(),k->new ArrayList<>()).add(e));
        List<Graph.Path> result=new ArrayList<>();
        for(Graph.Node start:nodes){
            walk(start.id(),start.id(),out,new ArrayList<>(List.of(start.id())),new ArrayList<>(),new ArrayList<>(),result,new HashSet<>());
        }
        return result.stream().distinct().toList();
    }
    private void walk(String root,String current,Map<String,List<Graph.Edge>> out,List<String> ns,List<String> es,List<dev.releaselens.domain.Evidence> ev,List<Graph.Path> result,Set<String> seen){
        if(ns.size()>props.maxTraversalDepth() || !seen.add(current)) return;
        List<Graph.Edge> next=out.getOrDefault(current,List.of());
        if(next.isEmpty() && ns.size()>1) result.add(new Graph.Path(ns,es,ev));
        for(Graph.Edge e:next){ if(ns.contains(e.to())) continue; List<String> nns=new ArrayList<>(ns);nns.add(e.to());List<String> nes=new ArrayList<>(es);nes.add(e.id());List<dev.releaselens.domain.Evidence> nev=new ArrayList<>(ev);if(e.evidence()!=null)nev.add(e.evidence());walk(root,e.to(),out,nns,nes,nev,result,new HashSet<>(seen)); }
    }
}
