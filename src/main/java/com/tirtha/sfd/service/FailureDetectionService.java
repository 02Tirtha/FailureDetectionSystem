package com.tirtha.sfd.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    @Transactional
    public void detectAndRecordFailures(Event currentEvent) {

        Workflow workflow = currentEvent.getWorkflow();
        String currentStep = currentEvent.getStepName().trim();
        LocalDateTime currentTime = currentEvent.getOccurredAt();

        List<WorkflowStep> steps =
                workflowStepRepository.findByWorkflowOrderByStepOrderAsc(workflow);

        WorkflowStep currentStepDef = steps.stream()
                .filter(s -> s.getStepName().equalsIgnoreCase(currentStep))
                .findFirst()
                .orElse(null);

        if (currentStepDef == null) return;

        int currentOrder = currentStepDef.getStepOrder();
        boolean isLastStep =
                currentOrder == steps.get(steps.size() - 1).getStepOrder();

        resolveMissingForStep(workflow, currentStepDef.getStepName());

        Long currentEventId = currentEvent.getId();
        if (currentEventId == null) {
            return;
        }

        WorkflowStep firstStep = steps.get(0);
        Event lastStartEvent =
                eventRepository.findTopByWorkflowAndStepNameAndIdLessThanEqualOrderByIdDesc(
                        workflow,
                        firstStep.getStepName().trim(),
                        currentEventId
                );
        Long runStartId =
                (lastStartEvent != null) ? lastStartEvent.getId() : currentEventId;

        boolean hasMissingEarlier = false;

        /* 🔴 MISSING STEP DETECTION — only past steps */
        for (WorkflowStep step : steps) {
            if (step.getStepOrder() >= currentOrder) break;

            boolean occurredInRun =
                    eventRepository.existsByWorkflowAndStepNameAndIdBetween(
                            workflow,
                            step.getStepName().trim(),
                            runStartId,
                            currentEventId
                    );

            if (!occurredInRun) {
                hasMissingEarlier = true;
                createOrUpdateFailure(
                        workflow,
                        step.getStepName(),
                        FailureType.MISSING_STEP,
                        Severity.HIGH,
                        "Expected step did not occur"
                );
            }
        }

        /* 🟠 DELAYED STEP DETECTION — only if previous event exists and no missing steps */
        if (currentOrder > 1) {
            if (!hasMissingEarlier) {
                WorkflowStep prevStep = steps.stream()
                        .filter(s -> s.getStepOrder() == currentOrder - 1)
                        .findFirst()
                        .orElse(null);

                if (prevStep != null) {
                    Event lastEventBeforeCurrent =
                            eventRepository.findTopByWorkflowAndIdLessThanOrderByIdDesc(
                                    workflow,
                                    currentEventId
                            );

                    if (lastEventBeforeCurrent != null
                            && lastEventBeforeCurrent.getStepName().trim()
                                    .equalsIgnoreCase(prevStep.getStepName().trim())) {

                        Event prevEvent =
                                eventRepository.findTopByWorkflowAndStepNameAndIdLessThanOrderByIdDesc(
                                        workflow,
                                        prevStep.getStepName().trim(),
                                        currentEventId
                                );

                        if (prevEvent != null
                                && prevEvent.getId() >= runStartId
                                && prevStep.getExpectedTimeSeconds() != null) {
                            long delay = Duration.between(prevEvent.getOccurredAt(), currentTime).getSeconds();

                            if (delay > prevStep.getExpectedTimeSeconds()) {
                                createOrUpdateFailure(
                                        workflow,
                                        currentStep,
                                        FailureType.DELAYED_STEP,
                                        Severity.MEDIUM,
                                        "Step delayed by " + delay +
                                                " seconds (expected " +
                                                prevStep.getExpectedTimeSeconds() + ")"
                                );
                            } else if (!isLastStep) {
                                silentFailureRepository.resolveDelayedSteps(workflow.getId(), currentStep);
                            }
                        }
                    }
                }
            }
        }

        /* 🟣 ML ANOMALY DETECTION */
        if ("EMAIL_SENT".equalsIgnoreCase(currentStep)) {
            WorkflowStep prevStep = steps.stream()
                    .filter(s -> s.getStepOrder() == currentOrder - 1)
                    .findFirst()
                    .orElse(null);

            if (prevStep != null) {
                Optional<Event> prevEventOpt =
                        eventRepository.findTopByWorkflowAndStepNameOrderByOccurredAtDesc(
                                workflow,
                                prevStep.getStepName().trim()
                        );

                if (prevEventOpt.isPresent()) {
                    Event prevEvent = prevEventOpt.get();

                    long duration = Duration.between(prevEvent.getOccurredAt(), currentTime).getSeconds();

                    if (mlAnomalyDetectionService.isAnomalous(workflow.getId(), currentStep, duration)) {
                        createOrUpdateFailure(
                                workflow,
                                currentStep,
                                FailureType.ML_ANOMALY,
                                Severity.HIGH,
                                "ML detected abnormal delay"
                        );
                    }
                }
            }
        }
    }

    private void createOrUpdateFailure(
            Workflow workflow,
            String stepName,
            FailureType type,
            Severity severity,
            String message
    ) {
        Optional<SilentFailure> existingFailureOpt =
                silentFailureRepository.findTopByWorkflowAndStepNameAndFailureTypeOrderByDetectedAtDesc(
                        workflow,
                        stepName,
                        type
                );

        if (existingFailureOpt.isPresent()) {
            SilentFailure existingFailure = existingFailureOpt.get();

            existingFailure.setMessage(message);
            existingFailure.setSeverity(severity);
            silentFailureRepository.save(existingFailure);
        } else {
            SilentFailure failure = new SilentFailure();
            failure.setWorkflow(workflow);
            failure.setStepName(stepName);
            failure.setFailureType(type);
            failure.setSeverity(severity);
            failure.setMessage(message);
            failure.setDetectedAt(LocalDateTime.now());

            silentFailureRepository.save(failure);
            mailService.sendAlert(failure);
        }
    }

    private void resolveMissingForStep(Workflow workflow, String stepName) {
        List<SilentFailure> missingFailures =
                silentFailureRepository.findByWorkflowAndStepNameAndFailureType(
                        workflow,
                        stepName,
                        FailureType.MISSING_STEP
                );

        if (missingFailures.isEmpty()) {
            return;
        }

        silentFailureRepository.deleteAll(missingFailures);
    }

    @Transactional
    public void detectFailures(Long workflowId) {
        Event latestEvent = eventRepository.findTopByWorkflowIdOrderByOccurredAtDesc(workflowId);
        if (latestEvent != null) {
            detectAndRecordFailures(latestEvent);
        }
    }

    public void detectForWorkflow(Long workflowId) {
        detectFailures(workflowId);
    }

    @Transactional
    public void resolveFailure(Long workflowId) {
        List<SilentFailure> failures =
                silentFailureRepository.findByWorkflow_Id(workflowId);
        silentFailureRepository.deleteAll(failures);
    }

    @Transactional
    public void resolveDelayedStep(Long workflowId, String stepName) {
        silentFailureRepository.resolveDelayedSteps(workflowId, stepName);
    }
}
