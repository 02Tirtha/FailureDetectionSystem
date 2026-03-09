package com.tirtha.sfd.controller;

import com.tirtha.sfd.service.FailureDetectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/failures")
@RequiredArgsConstructor
public class FailureDetectionController {

    private final FailureDetectionService failureDetectionService;

    @PostMapping("/detect/{workflowId}")
    public String detectFailures(@PathVariable Long workflowId) {

       failureDetectionService.detectWorkflowFailures(workflowId);

        return "Failure detection completed for workflow ID: " + workflowId;
    }

   
}
