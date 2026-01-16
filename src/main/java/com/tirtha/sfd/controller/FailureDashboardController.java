package com.tirtha.sfd.controller;

import com.tirtha.sfd.model.SilentFailure;
import com.tirtha.sfd.repository.SilentFailureRepository;
import com.tirtha.sfd.service.FailureType;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard/failures")
@RequiredArgsConstructor
public class FailureDashboardController {

    private final SilentFailureRepository failureRepository;

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
}
