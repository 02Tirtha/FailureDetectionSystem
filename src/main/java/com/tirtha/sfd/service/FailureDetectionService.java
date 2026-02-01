package com.tirtha.sfd.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

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
    private final MlAnomalyDetectionService mlAnomalyDetectionService;
    private final WorkflowStepRepository workflowStepRepository;

    /**
     * Detect failures for a single incoming event
     */
    @Transactional
    public void detectAndRecordFailures(Event currentEvent) {

        Workflow workflow = currentEvent.getWorkflow();
        String currentStep = currentEvent.getStepName();
        LocalDateTime currentTime = currentEvent.getOccurredAt();

        List<WorkflowStep> steps =
                workflowStepRepository.findByWorkflowOrderByStepOrderAsc(workflow);

        WorkflowStep currentStepDef = steps.stream()
                .filter(s -> s.getStepName().equals(currentStep))
                .findFirst()
                .orElse(null);

        if (currentStepDef == null) return;

        int currentOrder = currentStepDef.getStepOrder();
        boolean isLastStep =
                currentOrder == steps.get(steps.size() - 1).getStepOrder();

        /* 🔴 MISSING STEP DETECTION */
        for (WorkflowStep step : steps) {
            if (step.getStepOrder() >= currentOrder) break;

            boolean occurred =
                    eventRepository.existsByWorkflowAndStepName(workflow, step.getStepName());

            if (!occurred) {
                createFailureOnce(
                        workflow,
                        step.getStepName(),
                        FailureType.MISSING_STEP,
                        "Expected step did not occur"
                );
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

                        // 🔑 Create DELAYED_STEP ONLY ONCE
                        createFailureOnce(
                                workflow,
                                currentStep,
                                FailureType.DELAYED_STEP,
                                "Step delayed by " + delay +
                                        " seconds (expected " +
                                        currentStepDef.getExpectedTimeSeconds() + ")"
                        );
                    } else if (!isLastStep) {
                        // ✅ Auto-resolve delayed step when flow continues
                        silentFailureRepository.resolveDelayedSteps(
                                workflow.getId(), currentStep
                        );
                    }
                }
            }
        }

        /* 🟣 ML ANOMALY DETECTION */
        if ("EMAIL_SENT".equals(currentStep)) {
            WorkflowStep prevStep = steps.stream()
                    .filter(s -> s.getStepOrder() == currentOrder - 1)
                    .findFirst()
                    .orElse(null);

            if (prevStep != null) {
                Event prevEvent =
                        eventRepository.findTopByWorkflowAndStepNameOrderByOccurredAtDesc(
                                workflow, prevStep.getStepName());

                if (prevEvent != null) {
                    long duration =
                            Duration.between(prevEvent.getOccurredAt(), currentTime).getSeconds();

                    if (mlAnomalyDetectionService.isAnomalous(
                            workflow.getId(), currentStep, duration)) {

                        createFailureOnce(
                                workflow,
                                currentStep,
                                FailureType.ML_ANOMALY,
                                "ML detected abnormal delay"
                        );
                    }
                }
            }
        }
    }

    /**
     * Create failure ONLY if it does not already exist
     */
    private void createFailureOnce(
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

        if (exists) return;

        SilentFailure failure = new SilentFailure();
        failure.setWorkflow(workflow);
        failure.setStepName(stepName);
        failure.setFailureType(type);
        failure.setSeverity(Severity.HIGH);
        failure.setMessage(message);
        failure.setDetectedAt(LocalDateTime.now());
        failure.setResolved(false);

        silentFailureRepository.save(failure);
        mailService.sendAlert(failure);
    }

    /**
     * Detect failures for all events of a workflow (scheduler)
     */
    @Transactional
    public void detectFailures(Long workflowId) {
        List<Event> events =
                eventRepository.findByWorkflowIdOrderByOccurredAt(workflowId);
        for (Event event : events) {
            detectAndRecordFailures(event);
        }
    }

    public void detectForWorkflow(Long workflowId) {
        detectFailures(workflowId);
    }

    /**
     * Manual resolve (used by API)
     */
    @Transactional
    public void resolveFailure(Long workflowId) {
        List<SilentFailure> failures =
                silentFailureRepository.findByWorkflow_IdAndResolvedFalse(workflowId);

        LocalDateTime now = LocalDateTime.now();
        for (SilentFailure failure : failures) {
            failure.setResolved(true);
            failure.setResolvedAt(now);
        }
        silentFailureRepository.saveAll(failures);
    }

    @Transactional
public void resolveDelayedStep(Long workflowId, String stepName) {
    silentFailureRepository.resolveDelayedSteps(workflowId, stepName);
}

}
