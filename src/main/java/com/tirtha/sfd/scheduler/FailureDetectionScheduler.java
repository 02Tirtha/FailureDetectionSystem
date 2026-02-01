package com.tirtha.sfd.scheduler;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.tirtha.sfd.model.Workflow;
import com.tirtha.sfd.repository.WorkflowRepository;
import com.tirtha.sfd.service.FailureDetectionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FailureDetectionScheduler {

    private final WorkflowRepository workflowRepository;
    private final FailureDetectionService failureDetectionService;

    // Track workflow IDs that are currently being processed
    private final Set<Long> runningWorkflows = new HashSet<>();

    // Run every minute
    @Scheduled(fixedRate = 60000)
    public void detectFailuresForAllWorkflows() {
        System.out.println("Scheduler running at: " + LocalDateTime.now());

        List<Workflow> workflows = workflowRepository.findAll();

        for (Workflow workflow : workflows) {

            // Skip workflow if it's already running in this JVM
            if (runningWorkflows.contains(workflow.getId())) {
                System.out.println("Workflow ID " + workflow.getId() + " is already running. Skipping...");
                continue;
            }

            // Mark workflow as running
            runningWorkflows.add(workflow.getId());

            try {
                // Detect failures
                failureDetectionService.detectFailures(workflow.getId());
            } finally {
                // Remove from running set after detection
                runningWorkflows.remove(workflow.getId());
            }
        }
    }
}
