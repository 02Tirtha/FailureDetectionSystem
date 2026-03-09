package com.tirtha.sfd.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tirtha.sfd.service.FailureDetectionService;
import com.tirtha.sfd.service.MlLearningService;

@RestController
@RequestMapping("/api/ml")
public class MLTriggerController {

    private final FailureDetectionService service;

    public MLTriggerController(FailureDetectionService service) {
        this.service = service;
    }

    // @PostMapping("/detect")
    // public String detect(@RequestParam Long workflowId) {
    //     MlLearningService.detectForWorkflow(workflowId);
    //     return "ML detection executed for workflow " + workflowId;
    // }
}

