package dev.releaselens.decision;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import dev.releaselens.config.ReleaseLensProperties;
import dev.releaselens.domain.AnalysisModels;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.util.*;

@Component
public class JevHttpClient implements JevClient {
    private final ReleaseLensProperties props; private final ObjectMapper mapper; private final RestClient client;
    public JevHttpClient(ReleaseLensProperties props,ObjectMapper mapper){this.props=props;this.mapper=mapper;this.client=RestClient.builder().baseUrl("https://api.typesafe.ai").build();}
    @Override public List<AnalysisModels.JevDecision> decide(Object state){
        if(!props.aiEnabled() || System.getenv("JEV_API_KEY")==null || System.getenv("JEV_API_KEY").isBlank()) return List.of();
        Map<String,Object> body=new LinkedHashMap<>(); body.put("model", "jev-latest"); body.put("state", state); body.put("questions", questions());
        try { JsonNode root=client.post().uri("/v1/systemone").contentType(MediaType.APPLICATION_JSON).header("Authorization","Bearer "+System.getenv("JEV_API_KEY")).body(body).retrieve().body(JsonNode.class); return parse(root); } catch(Exception ignored){ return List.of(); }
    }
    private Map<String,Object> questions(){Map<String,Object> q=new LinkedHashMap<>(); q.put("changeCategory",Map.of("type","choice","instructions","Choose the dominant change category.","criteria",Map.of("api","API or DTO contract","security","security control","database","database migration","messaging","event or Kafka contract","internal","internal refactoring"))); q.put("releaseRisk",Map.of("type","score","instructions","Score release risk from low to critical.","criteria",List.of("low","moderate","high","critical"))); q.put("breakingChange",Map.of("type","noul","instructions","Is this likely to break a consumer?")); q.put("downstreamFailure",Map.of("type","noul","instructions","Could a downstream service fail?")); q.put("requiresHumanReview",Map.of("type","noul","instructions","Does this require human review?")); q.put("requiresContractTest",Map.of("type","noul","instructions","Should a contract test be added?")); q.put("rollbackDifficulty",Map.of("type","score","instructions","Score rollback difficulty.","criteria",List.of("easy","moderate","hard","very hard"))); return q; }
    private List<AnalysisModels.JevDecision> parse(JsonNode root){if(root==null||!root.has("answers"))return List.of(); List<AnalysisModels.JevDecision> out=new ArrayList<>(); root.get("answers").properties().forEach(e->{JsonNode n=e.getValue(); out.add(new AnalysisModels.JevDecision(e.getKey(),n.path("type").asText(),n.has("choice")?n.get("choice").asText():null,n.has("score")?n.get("score").asDouble():null,n.has("noul")?n.get("noul").asDouble():null,mapper.convertValue(n.path("probabilities"),Map.class),n.has("confidence")?n.get("confidence").asDouble():null,root.path("model").asText("jev-latest")));}); return out;}
}
