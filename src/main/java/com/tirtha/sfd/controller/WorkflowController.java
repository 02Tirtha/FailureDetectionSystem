package com.tirtha.sfd.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tirtha.sfd.model.Workflow;
import com.tirtha.sfd.repository.WorkflowRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/workflows")
@RequiredArgsConstructor
public class WorkflowController {
    
    private final WorkflowRepository workflowRepository;

     // Create workflow
    @PostMapping
    public Workflow createWorkflow(@RequestBody Workflow workflow) {
        return workflowRepository.save(workflow);
    }

    // Get all workflows
    @GetMapping
    public List<Workflow> getAllWorkflows() {
        return workflowRepository.findAll();
    }
}
