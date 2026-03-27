package com.tirtha.sfd.controller;

import com.tirtha.sfd.model.Event;
import com.tirtha.sfd.repository.EventRepository;
import com.tirtha.sfd.service.FailureDetectionService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/failures")
@RequiredArgsConstructor
public class FailureDetectionController {

    private final FailureDetectionService failureDetectionService;
    private final EventRepository eventRepository;

    // @PostMapping("/detect/{workflowId}")
    public String detectFailures(@PathVariable Long workflowId) {

        Event latestEvent =
                eventRepository.findTopByWorkflowIdOrderByOccurredAtDesc(workflowId);

        if (latestEvent == null) {
            return "No events found for workflow ID: " + workflowId;
        }

        // ✅ NEW METHOD
        failureDetectionService.detectAndRecordFailures(latestEvent);

        return "Failure detection completed for workflow ID: " + workflowId;
    }
}