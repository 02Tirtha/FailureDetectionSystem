package com.tirtha.sfd.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tirtha.sfd.dto.WorkflowDashboardDTO;
import com.tirtha.sfd.model.SilentFailure;
import com.tirtha.sfd.model.Workflow;
import com.tirtha.sfd.repository.SilentFailureRepository;
import com.tirtha.sfd.repository.WorkflowRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/workflows")
@RequiredArgsConstructor
public class WorkflowController {
    
    private final WorkflowRepository workflowRepository;
    private final SilentFailureRepository failureRepository;

     // Create workflow
    @PostMapping
    public Workflow createWorkflow(@RequestBody Workflow workflow) {
        return workflowRepository.save(workflow);
    }

    // Get all workflows
    @GetMapping("/all")
    public List<Workflow> getAllWorkflows() {
        return workflowRepository.findAll();
    }

    @GetMapping
public List<WorkflowDashboardDTO> getworkflowDashboard() {

    return workflowRepository.findAll().stream().map(workflow -> {

        List<SilentFailure> failures =
            failureRepository.findByWorkflow_Id(workflow.getId());

        long totalFailures = failures.size();

        long unresolvedFailures = totalFailures;

        LocalDateTime lastFailureTime =
            failures.stream()
                .map(SilentFailure::getDetectedAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        return new WorkflowDashboardDTO(
            workflow.getId(),
            workflow.getName(),
            totalFailures,
            unresolvedFailures,
            lastFailureTime
        );
    }).toList();
}

    }



