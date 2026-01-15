package com.tirtha.sfd.service;


import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.tirtha.sfd.repository.WorkflowRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScheduleFailureDetection {
     private final WorkflowRepository workflowRepository;
    private final FailureDetectionService failureDetectionService;

    // Run every minute (60000 ms)
    @Scheduled(fixedRate = 60000)
    public void detectFailuresForAllWorkflows() {
        System.out.println("Scheduler running at: " + LocalDateTime.now());
        workflowRepository.findAll()
                .forEach(workflow -> {
                    System.out.println("Checking workflow ID: " + workflow.getId());
                    failureDetectionService.detectFailures(workflow.getId());
                });
    }

    
        // Every 1 minute, Spring calls detectFailuresForAllWorkflows().
        // The method fetches all workflows from the database.
        // For each workflow, it runs your failure detection logic.
        // All missing/delayed steps are automatically detected and stored.

}