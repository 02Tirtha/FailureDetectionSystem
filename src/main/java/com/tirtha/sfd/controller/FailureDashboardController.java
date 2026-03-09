package com.tirtha.sfd.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tirtha.sfd.controller.EventController.ResolveRequest;
import com.tirtha.sfd.model.FailureType;
import com.tirtha.sfd.model.SilentFailure;
import com.tirtha.sfd.repository.SilentFailureRepository;
import com.tirtha.sfd.service.FailureResolutionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/dashboard/failures")
@RequiredArgsConstructor

public class FailureDashboardController {

    private final SilentFailureRepository failureRepository;
    
    private final FailureResolutionService failureResolutionService;

    // All failures
    @GetMapping
    public List<SilentFailure> getAllFailures() {
        return failureRepository.findAll();
    }

    // Failures by workflow
    @GetMapping("/workflow/{workflowId}")
    public List<SilentFailure> getFailuresByWorkflow(@PathVariable Long workflowId) {
        return failureRepository.findByWorkflow_Id(workflowId);
    }

    // Failures by type
    @GetMapping("/type/{type}")
    public List<SilentFailure> getFailuresByType(@PathVariable FailureType type) {
        return failureRepository.findByFailureType(type);
    }


   @PostMapping("/resolve")
public ResponseEntity<String> resolveStep(@RequestBody ResolveRequest request) {

    failureResolutionService.resolveFailures(
            request.getWorkflowId(),
            request.getStepName()
    );

    return ResponseEntity.ok(
        "Failure resolved for workflow " +
        request.getWorkflowId() +
        ", step " +
        request.getStepName()
    );
}


    // @PutMapping("/resolve/delayed")
    // public ResponseEntity<String> resolveDelayedStep(
    //         @RequestParam Long workflowId,
    //         @RequestParam String stepName
    // ) {
    //     failureResolutionService.resolveDelayedStep(workflowId, stepName);
    //     return ResponseEntity.ok("DELAYED_STEP resolved successfully");
    // }
}
    