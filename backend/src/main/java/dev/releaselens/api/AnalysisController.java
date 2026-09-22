package dev.releaselens.api;

import dev.releaselens.analysis.AnalysisCoordinator;
import dev.releaselens.domain.AnalysisModels;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;

@RestController @RequestMapping("/api/v1/analyses")
public class AnalysisController {
    private final AnalysisCoordinator coordinator;
    public AnalysisController(AnalysisCoordinator coordinator){this.coordinator=coordinator;}
    @PostMapping public ResponseEntity<StartResponse> start(@Valid @RequestBody StartRequest request){String id=coordinator.start(request.repositoryPath(),request.baseRef(),request.headRef());return ResponseEntity.accepted().location(URI.create("/api/v1/analyses/"+id)).body(new StartResponse(id,"QUEUED"));}
    @GetMapping("/{id}") public AnalysisModels.AnalysisResult get(@PathVariable String id){return coordinator.get(id);}
    public record StartRequest(@NotBlank String repositoryPath,@NotBlank String baseRef,@NotBlank String headRef){}
    public record StartResponse(String id,String status){}
}
