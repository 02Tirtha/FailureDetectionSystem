package com.tirtha.sfd.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tirtha.sfd.model.Event;
import com.tirtha.sfd.repository.EventRepository;
import com.tirtha.sfd.service.EventService;
import com.tirtha.sfd.service.FailureDetectionService;
import com.tirtha.sfd.service.FailureResolutionService;
import com.tirtha.sfd.service.MlLearningService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventRepository eventRepository;
    private final MlLearningService mlLearningService;
    private final FailureDetectionService failureDetectionService;
    private final EventService eventService;
    private final FailureResolutionService resolutionService;
    // Create events
    @PostMapping("/batch")
public List<Event> createEvents(@RequestBody List<Event> events) {

    List<Event> savedEvents = eventRepository.saveAll(events);

    for (Event event : savedEvents) {
        
        // ML only for EMAIL_SENT
        if ("EMAIL_SENT".equals(event.getStepName())) {

            // find previous event
            eventRepository
                .findByWorkflowIdOrderByOccurredAt(event.getWorkflow().getId())
                .stream()
                .filter(e -> e.getOccurredAt().isBefore(event.getOccurredAt())) //exclude current event to avoid matching itself
                .reduce((first, second) -> second) //Go through the list and keep replacing the value with the next one to get most recent event
                .ifPresent(previousEvent -> {

                    long duration = java.time.Duration.between(
                            previousEvent.getOccurredAt(),
                            event.getOccurredAt()
                    ).getSeconds();

                    mlLearningService.updateThreshold(
                            event.getWorkflow().getId(),
                            "EMAIL_SENT",
                            duration
                    );
                });
        }
    }

    // 🚀 AUTO TRIGGER FAILURE DETECTION
    savedEvents.stream()
            .map(e -> e.getWorkflow().getId())
            .distinct()
            .forEach(failureDetectionService::detectFailures);

    failureDetectionService.detectFailures(events.get(0).getWorkflow().getId());


    return savedEvents;
}


    // Get all events for a workflow
    @GetMapping
    public List<Event> getEventsByWorkflow(@RequestParam Long workflowId) {
        return eventRepository.findByWorkflowIdOrderByOccurredAt(workflowId);
    }
    
      @PostMapping
    public ResponseEntity<Void> receiveEvent(@RequestBody Event event) {
        eventService.handleEvent(event);
        return ResponseEntity.ok().build();
    }

 
    /**
 * Resolve failures for a specific step/event in a workflow
 * Accepts JSON like: { "workflowId": 1, "stepName": "EMAIL_SENT" }
 */
@PostMapping("/resolve")
public ResponseEntity<String> resolveStep(@RequestBody ResolveRequest request) {
    resolutionService.resolveFailures(request.getWorkflowId(), request.getStepName());
    return ResponseEntity.ok("Failures resolved for workflow " + request.getWorkflowId() +
                             ", step " + request.getStepName());
}

/**
 * Resolve all failures for a workflow
 * Accepts JSON like: { "workflowId": 1 }
 */
@PostMapping("/resolve-all")
public ResponseEntity<String> resolveAll(@RequestBody ResolveAllRequest request) {
    resolutionService.resolveAllFailures(request.getWorkflowId());
    return ResponseEntity.ok("All failures resolved for workflow " + request.getWorkflowId());
}


     // DTOs
    public static class ResolveRequest {
        private Long workflowId;
        private String stepName;

        public Long getWorkflowId() { return workflowId; }
        public void setWorkflowId(Long workflowId) { this.workflowId = workflowId; }

        public String getStepName() { return stepName; }
        public void setStepName(String stepName) { this.stepName = stepName; }
    }

    public static class ResolveAllRequest {
        private Long workflowId;

        public Long getWorkflowId() { return workflowId; }
        public void setWorkflowId(Long workflowId) { this.workflowId = workflowId; }
    }

}