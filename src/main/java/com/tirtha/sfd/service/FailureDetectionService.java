package com.tirtha.sfd.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tirtha.sfd.model.Event;
import com.tirtha.sfd.model.FailureType;
import com.tirtha.sfd.model.Severity;
import com.tirtha.sfd.model.SilentFailure;
import com.tirtha.sfd.model.Workflow;
import com.tirtha.sfd.model.WorkflowStep;
import com.tirtha.sfd.repository.EventRepository;
import com.tirtha.sfd.repository.SilentFailureRepository;
import com.tirtha.sfd.repository.WorkflowStepRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FailureDetectionService {

    private final EventRepository eventRepository;
    private final SilentFailureRepository silentFailureRepository;
    private final SilentFailureMailService mailService;
    private final WorkflowStepRepository workflowStepRepository;

    /**
     * FIX: @Async so the HTTP thread returns immediately after saving the event.
     * Email is sent AFTER the @Transactional method below commits,
     * so there are no SMTP delays inside an open DB transaction.
     */
    @Async
    public void detectAndRecordFailures(Event currentEvent) {
        List<SilentFailure> newFailures = detectAndSave(currentEvent);

        // Send emails AFTER transaction has committed — outside @Transactional
        for (SilentFailure failure : newFailures) {
            mailService.sendAlert(failure);
        }
    }

    /**
     * All DB work happens here inside its own transaction.
     * Returns the list of newly created failures so the caller
     * can send emails after the transaction commits.
     */
    @Transactional
    public List<SilentFailure> detectAndSave(Event currentEvent) {

        List<SilentFailure> newFailures = new ArrayList<>();

        Workflow workflow = currentEvent.getWorkflow();
        String currentStep = currentEvent.getStepName();
        LocalDateTime currentTime = currentEvent.getOccurredAt();

        List<WorkflowStep> steps =
                workflowStepRepository.findByWorkflowOrderByStepOrderAsc(workflow);

        WorkflowStep currentStepDef = steps.stream()
                .filter(s -> s.getStepName().equals(currentStep))
                .findFirst()
                .orElse(null);

        if (currentStepDef == null) return newFailures;

        int currentOrder = currentStepDef.getStepOrder();
        boolean isLastStep =
                currentOrder == steps.get(steps.size() - 1).getStepOrder();

        /* 🔴 MISSING STEP DETECTION */
        for (WorkflowStep step : steps) {
            if (step.getStepOrder() >= currentOrder) break;

            boolean occurred =
                    eventRepository.existsByWorkflowAndStepName(workflow, step.getStepName());

            if (!occurred) {
                SilentFailure f = createFailureOnce(
                        workflow,
                        step.getStepName(),
                        FailureType.MISSING_STEP,
                        "Expected step did not occur"
                );
                if (f != null) newFailures.add(f);
            }
        }

        /* 🟠 DELAYED STEP DETECTION */
        if (currentOrder > 1) {
            WorkflowStep prevStep = steps.stream()
                    .filter(s -> s.getStepOrder() == currentOrder - 1)
                    .findFirst()
                    .orElse(null);

            if (prevStep != null) {
                Event prevEvent =
                        eventRepository.findTopByWorkflowAndStepNameOrderByOccurredAtDesc(
                                workflow, prevStep.getStepName());

                if (prevEvent != null) {
                    long delay =
                            Duration.between(prevEvent.getOccurredAt(), currentTime).getSeconds();

                    if (delay > currentStepDef.getExpectedTimeSeconds()) {
                        SilentFailure f = createFailureOnce(
                                workflow,
                                currentStep,
                                FailureType.DELAYED_STEP,
                                "Step delayed by " + delay +
                                        " seconds (expected " +
                                        currentStepDef.getExpectedTimeSeconds() + ")"
                        );
                        if (f != null) newFailures.add(f);
                    } else if (!isLastStep) {
                        silentFailureRepository.resolveDelayedSteps(
                                workflow.getId(), currentStep
                        );
                    }
                }
            }
        }

       
        return newFailures;
    }

    /**
     * Create failure ONLY if it does not already exist.
     * Returns the saved SilentFailure, or null if it already existed.
     * NOTE: mailService.sendAlert() is intentionally NOT called here —
     * it is called by the @Async method after the transaction commits.
     */
    private SilentFailure createFailureOnce(
            Workflow workflow,
            String stepName,
            FailureType type,
            String message
    ) {
        boolean exists =
                silentFailureRepository
                        .existsByWorkflowAndStepNameAndFailureType(
                                workflow, stepName, type
                        );

        if (exists) return null;

        SilentFailure failure = new SilentFailure();
        failure.setWorkflow(workflow);
        failure.setStepName(stepName);
        failure.setFailureType(type);
        failure.setSeverity(Severity.HIGH);
        failure.setMessage(message);
        failure.setDetectedAt(LocalDateTime.now());
        
        return silentFailureRepository.save(failure);
    }

    /**
     * Detect failures for all events of a workflow (scheduler).
     * This is called by the scheduler, not the HTTP thread, so
     * @Async is not needed here — but emails are still sent after
     * the transaction commits.
     */
    @Transactional
    public void detectFailures(Long workflowId) {
        List<Event> events =
                eventRepository.findByWorkflowIdOrderByOccurredAt(workflowId);
        for (Event event : events) {
            List<SilentFailure> newFailures = detectAndSave(event);
            for (SilentFailure f : newFailures) {
                mailService.sendAlert(f);
            }
        }
    }

    public void detectForWorkflow(Long workflowId) {
        detectFailures(workflowId);
    }

    

    @Transactional
    public void resolveDelayedStep(Long workflowId, String stepName) {
        silentFailureRepository.resolveDelayedSteps(workflowId, stepName);
    }
}