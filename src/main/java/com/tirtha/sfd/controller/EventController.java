package com.tirtha.sfd.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tirtha.sfd.model.Event;
import com.tirtha.sfd.model.Role;
import com.tirtha.sfd.model.Workflow;
import com.tirtha.sfd.service.AuthorizationService;
import com.tirtha.sfd.service.EventService;
import com.tirtha.sfd.service.FailureResolutionService;

import lombok.Data;
import lombok.RequiredArgsConstructor;

// X-User-Email :
// Identify the user (by email)
// Look up their role (ADMIN or USER)
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final FailureResolutionService resolutionService;
    private final AuthorizationService authorizationService;
    // ---------------- CREATE EVENTS ----------------

    @PostMapping
    public ResponseEntity<EventResponse> receiveEvent(
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestBody Event event
    ) {
        authorizationService.requireAnyRole(userEmail, Role.USER, Role.ADMIN);
        Event savedEvent = eventService.handleEvent(event);
        EventResponse response = new EventResponse(
                savedEvent.getId(),
                savedEvent.getStepName(),
                savedEvent.getOccurredAt(),
                "Event created successfully"
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/batch")
    public ResponseEntity<String> createEvents(
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestBody List<EventRequest> requests
    ) {
        authorizationService.requireAnyRole(userEmail, Role.USER, Role.ADMIN);
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
    public ResponseEntity<String> resolveStep(
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestBody ResolveRequest request
    ) {
        authorizationService.requireRole(userEmail, Role.ADMIN);
        resolutionService.resolveFailures(
                request.getWorkflowId(),
                request.getStepName()
        );
        return ResponseEntity.ok(
                "Failures resolved for workflow " + request.getWorkflowId()
        );
    }

    @PostMapping("/resolve-all")
    public ResponseEntity<String> resolveAll(
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestBody ResolveAllRequest request
    ) {
        authorizationService.requireRole(userEmail, Role.ADMIN);
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

    @Data
    public static class EventResponse {
        private final Long id;
        private final String stepName;
        private final LocalDateTime occurredAt;
        private final String message;
    }
}
