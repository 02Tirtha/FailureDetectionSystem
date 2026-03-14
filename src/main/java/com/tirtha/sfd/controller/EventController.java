package com.tirtha.sfd.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tirtha.sfd.model.Event;
import com.tirtha.sfd.model.Workflow;
import com.tirtha.sfd.service.EventService;
import com.tirtha.sfd.service.FailureResolutionService;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final FailureResolutionService resolutionService;
    // ---------------- CREATE EVENTS ----------------

    @PostMapping
    public ResponseEntity<Void> receiveEvent(@RequestBody Event event) {
        eventService.handleEvent(event);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/batch")
    public ResponseEntity<String> createEvents(@RequestBody List<EventRequest> requests) {
        for (EventRequest request : requests) {
            Event event = new Event();
            Workflow workflow = new Workflow();
            workflow.setId(request.getWorkflowId());
            event.setWorkflow(workflow);
            event.setStepName(request.getStepName());
            event.setOccurredAt(request.getOccurredAt());

            eventService.handleEvent(event);
        }
        return ResponseEntity.ok("Events created successfully");
    }


    // ---------------- READ EVENTS ----------------

    @GetMapping("/get")
    public List<Event> getEventsByWorkflow(@RequestParam Long workflowId) {
        return eventService.getEventsByWorkflow(workflowId);
    }

    // ---------------- RESOLUTION ----------------

    @PostMapping("/resolve")
    public ResponseEntity<String> resolveStep(@RequestBody ResolveRequest request) {
        resolutionService.resolveFailures(
                request.getWorkflowId(),
                request.getStepName()
        );
        return ResponseEntity.ok(
                "Failures resolved for workflow " + request.getWorkflowId()
        );
    }

    @PostMapping("/resolve-all")
    public ResponseEntity<String> resolveAll(@RequestBody ResolveAllRequest request) {
        resolutionService.resolveAllFailures(request.getWorkflowId());
        return ResponseEntity.ok(
                "All failures resolved for workflow " + request.getWorkflowId()
        );
    }

    // ---------------- DTOs ----------------

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

    @Data
    public static class EventRequest {
        private Long workflowId;
        private String stepName;
        private LocalDateTime occurredAt;
    }
}
