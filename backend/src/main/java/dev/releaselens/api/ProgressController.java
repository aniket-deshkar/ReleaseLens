package dev.releaselens.api;

import dev.releaselens.analysis.AnalysisCoordinator;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController @RequestMapping("/api/v1/analyses")
public class ProgressController {
    private final AnalysisCoordinator coordinator;
    public ProgressController(AnalysisCoordinator coordinator){this.coordinator=coordinator;}
    @GetMapping(value="/{id}/events",produces=MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter events(@PathVariable String id){SseEmitter emitter=new SseEmitter(30_000L); try{var r=coordinator.get(id); emitter.send(SseEmitter.event().name("analysis").data(r)); emitter.complete();}catch(Exception e){emitter.completeWithError(e);} return emitter;}
}
