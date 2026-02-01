package com.tirtha.sfd.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tirtha.sfd.model.*;
import com.tirtha.sfd.repository.*;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final WorkflowRepository workflowRepository;
    private final WorkflowStepRepository workflowStepRepository;
    private final SilentFailureRepository silentFailureRepository;
    private final SilentFailureMailService mailService;
    private final MlAnomalyDetectionService mlService;

    /* ================= SAVE EVENTS ================= */

    @Transactional
    public List<Event> saveEventsWithWorkflow(List<Event> events) {

        for (Event event : events) {
            Long workflowId = event.getWorkflow().getId();

            Workflow workflow = workflowRepository.findById(workflowId)
                    .orElseThrow(() -> new RuntimeException("Workflow not found: " + workflowId));

            event.setWorkflow(workflow);
        }

        return eventRepository.saveAll(events);
    }

    /* ================= MAIN ENTRY ================= */

    @Transactional
    public void handleEvent(Event event) {

        eventRepository.save(event);

        autoResolveFailures(event);

        detectAndRecordFailures(event);
    }

    /* ================= AUTO RESOLVE ================= */

    private void autoResolveFailures(Event event) {

        Workflow workflow = event.getWorkflow();
        String currentStep = event.getStepName();

        WorkflowStep currentStepDef =
                workflowStepRepository.findByWorkflowAndStepName(workflow, currentStep);

        if (currentStepDef == null) return;

        int currentOrder = currentStepDef.getStepOrder();

        List<SilentFailure> unresolvedMissing =
                silentFailureRepository.findByWorkflowAndFailureTypeAndResolvedFalse(
                        workflow, FailureType.MISSING_STEP
                );

        for (SilentFailure failure : unresolvedMissing) {

            WorkflowStep failedStep =
                    workflowStepRepository.findByWorkflowAndStepName(
                            workflow, failure.getStepName());

            if (failedStep != null && failedStep.getStepOrder() < currentOrder) {
                failure.setResolved(true);
                failure.setResolvedAt(LocalDateTime.now());
            }
        }

        silentFailureRepository.saveAll(unresolvedMissing);
    }

    /* ================= DETECTION ================= */

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

        /* 🔴 MISSING STEP DETECTION */
        for (WorkflowStep step : steps) {

            if (step.getStepOrder() >= currentOrder) break;

            boolean stepOccurred =
                    eventRepository.existsByWorkflowAndStepName(workflow, step.getStepName());

            if (!stepOccurred) {

                boolean exists =
                        silentFailureRepository.existsByWorkflowAndStepNameAndFailureTypeAndResolvedFalse(
                                workflow, step.getStepName(), FailureType.MISSING_STEP);

                if (!exists) {
                    SilentFailure failure = new SilentFailure();
                    failure.setWorkflow(workflow);
                    failure.setStepName(step.getStepName());
                    failure.setFailureType(FailureType.MISSING_STEP);
                    failure.setSeverity(Severity.HIGH);
                    failure.setMessage("Expected step did not occur");
                    failure.setDetectedAt(LocalDateTime.now());
                    failure.setResolved(false);

                    silentFailureRepository.save(failure);
                    mailService.sendAlert(failure);
                }
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

                        boolean exists =
                                silentFailureRepository.existsByWorkflowAndStepNameAndFailureTypeAndResolvedFalse(
                                        workflow, currentStep, FailureType.DELAYED_STEP);

                        if (!exists) {
                            SilentFailure failure = new SilentFailure();
                            failure.setWorkflow(workflow);
                            failure.setStepName(currentStep);
                            failure.setFailureType(FailureType.DELAYED_STEP);
                            failure.setSeverity(Severity.HIGH);
                            failure.setMessage("Step delayed by " + delay + " seconds");
                            failure.setDetectedAt(LocalDateTime.now());
                            failure.setResolved(false);

                            silentFailureRepository.save(failure);
                            mailService.sendAlert(failure);
                        }
                    }
                }
            }
        }

        /* 🟣 ML ANOMALY */
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

                    if (mlService.isAnomalous(workflow.getId(), currentStep, duration)) {

                        boolean exists =
                                silentFailureRepository.existsByWorkflowAndStepNameAndFailureTypeAndResolvedFalse(
                                        workflow, currentStep, FailureType.ML_ANOMALY);

                        if (!exists) {
                            SilentFailure failure = new SilentFailure();
                            failure.setWorkflow(workflow);
                            failure.setStepName(currentStep);
                            failure.setFailureType(FailureType.ML_ANOMALY);
                            failure.setSeverity(Severity.HIGH);
                            failure.setMessage("ML detected abnormal delay");
                            failure.setDetectedAt(LocalDateTime.now());
                            failure.setResolved(false);

                            silentFailureRepository.save(failure);
                            mailService.sendAlert(failure);
                        }
                    }
                }
            }
        }
    }
}
