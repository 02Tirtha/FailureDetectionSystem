package com.tirtha.sfd.scheduler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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

    // Thread-safe set
    private final Set<Long> runningWorkflows =
            ConcurrentHashMap.newKeySet();

    // Run every minute
    @Scheduled(fixedRate = 60000)
    public void detectFailuresForAllWorkflows() {

        System.out.println("Scheduler running at: " + LocalDateTime.now());

        List<Workflow> workflows = workflowRepository.findAll();

        for (Workflow workflow : workflows) {

            Long workflowId = workflow.getId();

            // Skip if already running
            if (!runningWorkflows.add(workflowId)) {
                System.out.println("Workflow ID " + workflowId + " already running, skipping");
                continue;
            }

            try {
              failureDetectionService.detectFailures(workflowId);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                runningWorkflows.remove(workflowId);
            }
        }
    }
}
