package com.tirtha.sfd.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tirtha.sfd.model.FailureType;
import com.tirtha.sfd.model.Severity;
import com.tirtha.sfd.model.SilentFailure;
import com.tirtha.sfd.model.Workflow;
import com.tirtha.sfd.model.WorkflowStep;
import com.tirtha.sfd.repository.EventRepository;
import com.tirtha.sfd.repository.SilentFailureRepository;
import com.tirtha.sfd.repository.WorkflowRepository;
import com.tirtha.sfd.repository.WorkflowStepRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FailureDetectionService {

    private final WorkflowRepository workflowRepository;
    private final WorkflowStepRepository workflowStepRepository;
    private final EventRepository eventRepository;
    private final SilentFailureRepository silentFailureRepository;
    private final SilentFailureMailService mailService;

    @Transactional
    public void detectMissingSteps(Long workflowId) {

        Workflow workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new RuntimeException("Workflow not found"));

        List<WorkflowStep> steps =
                workflowStepRepository.findByWorkflowOrderByStepOrderAsc(workflow); 

        for (WorkflowStep step : steps) {

            boolean occurred = eventRepository
                    .existsByWorkflowAndStepName(workflow, step.getStepName());

            if (!occurred) {

                boolean alreadyExists =
                        silentFailureRepository
                                .existsByWorkflowAndStepNameAndFailureTypeAndResolvedFalse(
                                        workflow,
                                        step.getStepName(),
                                        FailureType.MISSING_STEP
                                );

                if (alreadyExists) continue;

                SilentFailure failure = new SilentFailure();
                failure.setWorkflow(workflow);
                failure.setStepName(step.getStepName());
                failure.setFailureType(FailureType.MISSING_STEP);
                failure.setSeverity(Severity.HIGH);
                failure.setMessage("Step was never executed");
                failure.setDetectedAt(LocalDateTime.now());
                failure.setResolved(false);

                silentFailureRepository.save(failure);
                mailService.sendAlert(failure);
            }
        }
    }
    @Transactional
public void detectWorkflowFailures(Long workflowId) {
        
    Workflow workflow = workflowRepository.findById(workflowId)
            .orElseThrow(() -> new RuntimeException("Workflow not found"));

    List<WorkflowStep> steps =
            workflowStepRepository.findByWorkflowOrderByStepOrderAsc(workflow);

    WorkflowStep previousStep = null;
    LocalDateTime previousTime = null;

    for (WorkflowStep step : steps) {

        // 🔎 Check if step occurred
      LocalDateTime safeTime = previousTime == null
        ? LocalDateTime.of(1970, 1, 1, 0, 0)
        : previousTime;

var eventOpt = eventRepository
        .findFirstByWorkflowAndStepNameAndOccurredAtAfterOrderByOccurredAtAsc(
                workflow,
                step.getStepName(),
                safeTime
        );


        // ❌ STEP MISSING
        if (eventOpt.isEmpty()) {

    createFailureIfNotExists(
            workflow,
            step.getStepName(),
            FailureType.MISSING_STEP,
            Severity.HIGH,
            "Step was never executed"
    );

    break;
}

LocalDateTime currentTime = eventOpt.get().getOccurredAt();
        System.out.println("Current: " + currentTime);

        // ⏱️ DELAY CHECK (only if previous exists)
        if (previousStep != null && previousTime != null) {
            System.out.println("Previous: " + previousTime);
    
            Long expectedDelay = step.getExpectedTimeSeconds();

            if (expectedDelay != null) {
                System.out.println("Expected Delay: " + expectedDelay);

                long actualDelay =
                        java.time.Duration.between(previousTime, currentTime)
                                .getSeconds();

                System.out.println("Actual Delay: " + actualDelay);
        
                if (actualDelay > expectedDelay) {

                   boolean alreadyExists =
                        silentFailureRepository
                        .existsByWorkflowAndStepNameAndFailureTypeAndResolvedFalse(
                                workflow,
                                step.getStepName(),
                                FailureType.DELAYED_STEP
                        );

                if (!alreadyExists) {

                        createFailureIfNotExists(
                        workflow,
                        step.getStepName(),
                        FailureType.DELAYED_STEP,
                        Severity.MEDIUM,
                        "Step delayed by " + (actualDelay - expectedDelay) + " seconds"
                        );
                }

        }
                       
        }

        // Move forward
        previousStep = step;
        previousTime = currentTime;
        
}
}
}
private void createFailureIfNotExists(
        Workflow workflow,
        String stepName,
        FailureType type,
        Severity severity,
        String message
) {

    boolean exists =
            silentFailureRepository
                    .existsByWorkflowAndStepNameAndFailureTypeAndResolvedFalse(
                            workflow,
                            stepName,
                            type
                    );

    if (exists) return;

    SilentFailure failure = new SilentFailure();
    failure.setWorkflow(workflow);
    failure.setStepName(stepName);
    failure.setFailureType(type);
    failure.setSeverity(severity);
    failure.setMessage(message);
    failure.setDetectedAt(LocalDateTime.now());
    failure.setResolved(false);

    silentFailureRepository.save(failure);
    mailService.sendAlert(failure);
}

    
}
