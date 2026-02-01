package com.tirtha.sfd.controller;

import com.tirtha.sfd.model.FailureType;
import com.tirtha.sfd.model.SilentFailure;
import com.tirtha.sfd.repository.SilentFailureRepository;
import com.tirtha.sfd.service.FailureDetectionService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard/failures")
@RequiredArgsConstructor
public class FailureDashboardController {

    private final SilentFailureRepository failureRepository;
    private final FailureDetectionService failureDetectionService;


    // All failures
    @GetMapping
    public List<SilentFailure> getAllFailures() {
        return failureRepository.findAll();
    }

    // Failures by workflow
    @GetMapping("/workflow/{workflowId}")
    public List<SilentFailure> getFailuresByWorkflow(@PathVariable Long workflowId) {
        return failureRepository.findByWorkflowId(workflowId);
    }

    // Failures by type
    @GetMapping("/type/{type}")
    public List<SilentFailure> getFailuresByType(@PathVariable FailureType type) {
        return failureRepository.findByFailureType(type);
    }


    @PostMapping("/resolve/{workflowId}")
    public String resolveFailure(@PathVariable Long workflowId) {
        failureDetectionService.resolveFailure(workflowId);
        return "Failure resolved for workflow " + workflowId;
    }


    @PutMapping("/resolve/delayed")
    public ResponseEntity<String> resolveDelayedStep(
            @RequestParam Long workflowId,
            @RequestParam String stepName
    ) {
        failureDetectionService.resolveDelayedStep(workflowId, stepName);
        return ResponseEntity.ok("DELAYED_STEP resolved successfully");
    }
}
    