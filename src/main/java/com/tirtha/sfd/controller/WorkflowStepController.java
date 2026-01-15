package com.tirtha.sfd.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tirtha.sfd.model.WorkflowStep;
import com.tirtha.sfd.repository.WorkflowStepRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/workflow-steps")
@RequiredArgsConstructor
public class WorkflowStepController {

    private final WorkflowStepRepository stepRepository;

    // Create workflow steps
    @PostMapping
    public List<WorkflowStep> createSteps(@RequestBody List<WorkflowStep> steps) {
        return stepRepository.saveAll(steps);
    }

    // Get all steps for a workflow
    @GetMapping
    public List<WorkflowStep> getStepsByWorkflow(@RequestParam Long workflowId) {
        return stepRepository.findByWorkflowIdOrderByStepOrder(workflowId);
    }

    
}
