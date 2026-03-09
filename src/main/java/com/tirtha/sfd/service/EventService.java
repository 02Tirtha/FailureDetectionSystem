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
import com.tirtha.sfd.repository.WorkflowRepository;
import com.tirtha.sfd.repository.WorkflowStepRepository;

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
    private final FailureDetectionService failureDetectionService;

    /* ================= SAVE EVENTS ================= */
    @Transactional
    public List<Event> saveEventsWithWorkflow(List<Event> events) {
        if (events == null || events.isEmpty()) {
            throw new RuntimeException("Events list cannot be null or empty");
        }

        for (Event event : events) {
            if (event.getWorkflow() == null || event.getWorkflow().getId() == null) {
                throw new RuntimeException("workflow.id is required");
            }
            Long workflowId = event.getWorkflow().getId();

            // Fetch full workflow
            Workflow workflow = workflowRepository.findById(workflowId)
                    .orElseThrow(() -> new RuntimeException("Workflow not found: " + workflowId));
            event.setWorkflow(workflow);

            // Ensure stepName exists in workflow
            WorkflowStep step = workflowStepRepository.findByWorkflowAndStepName(workflow, event.getStepName());
            if (step == null) {
                throw new RuntimeException("Step not found in workflow: " + event.getStepName());
            }
        }

        return eventRepository.saveAll(events);
    }

    /* ================= MAIN ENTRY ================= */
    @Transactional
        public void handleEvent(Event event) {

            // 1. Save event with proper workflow reference
            if (event.getWorkflow() == null || event.getWorkflow().getId() == null) {
                throw new RuntimeException("workflow.id is required");
            }
            Long workflowId = event.getWorkflow().getId();

            Workflow workflow = workflowRepository.findById(workflowId)
                    .orElseThrow(() -> new RuntimeException("Workflow not found: " + workflowId));

            event.setWorkflow(workflow);
            eventRepository.save(event);

            // 2. AUTO RESOLVE (UNCHANGED)
            autoResolveFailures(event);

            // 3. FAILURE DETECTION (MOVED, NOT REMOVED)
            failureDetectionService.detectMissingSteps(workflowId);

            detectAndRecordFailures(event);
        }


    /* ================= AUTO RESOLVE ================= */
    private void autoResolveFailures(Event event) {

        Workflow workflow = event.getWorkflow();
        String currentStep = event.getStepName();

        WorkflowStep currentStepDef = workflowStepRepository.findByWorkflowAndStepName(workflow, currentStep);
        if (currentStepDef == null) return;

        int currentOrder = currentStepDef.getStepOrder();

        List<SilentFailure> unresolvedMissing =
                silentFailureRepository.findByWorkflowAndFailureTypeAndResolvedFalse(
                        workflow, FailureType.MISSING_STEP
                );

        for (SilentFailure failure : unresolvedMissing) {
            WorkflowStep failedStep = workflowStepRepository.findByWorkflowAndStepName(
                    workflow, failure.getStepName());

            if (failedStep != null && failedStep.getStepOrder() < currentOrder) {
                failure.setResolved(true);
                failure.setResolvedAt(LocalDateTime.now());
            }
        }

        if (unresolvedMissing != null && !unresolvedMissing.isEmpty()) {
            silentFailureRepository.saveAll(unresolvedMissing);
        }
    }

    /* ================= DETECTION ================= */
    @Transactional
    public void detectAndRecordFailures(Event currentEvent) {

        Workflow workflow = currentEvent.getWorkflow();
        String currentStep = currentEvent.getStepName();
        LocalDateTime currentTime = currentEvent.getOccurredAt();

        List<WorkflowStep> steps = workflowStepRepository.findByWorkflowOrderByStepOrderAsc(workflow);

        WorkflowStep currentStepDef = steps.stream()
                .filter(s -> s.getStepName().equals(currentStep))
                .findFirst()
                .orElse(null);

        if (currentStepDef == null) return;

        int currentOrder = currentStepDef.getStepOrder();

        /* 🔴 MISSING STEP DETECTION */
        for (WorkflowStep step : steps) {
            if (step.getStepOrder() >= currentOrder) break;

            boolean stepOccurred = eventRepository.existsByWorkflowAndStepName(workflow, step.getStepName());

            if (!stepOccurred) {
                createFailure(workflow, step.getStepName(), FailureType.MISSING_STEP,
                        "Step was skipped before " + currentStep);
            }
        }

        /* 🟠 DELAYED STEP DETECTION */
        if (currentOrder > 1) {
            WorkflowStep prevStep = steps.stream()
                    .filter(s -> s.getStepOrder() == currentOrder - 1)
                    .findFirst()
                    .orElse(null);

            if (prevStep != null) {
               Optional<Event> prevEventOpt =
        eventRepository.findTopByWorkflowAndStepNameOrderByOccurredAtDesc(
                workflow, prevStep.getStepName());

if (prevEventOpt.isPresent()) {

    Event prevEvent = prevEventOpt.get();

    long delay = Duration.between(
            prevEvent.getOccurredAt(),
            currentTime
    ).getSeconds();

    if (delay > currentStepDef.getExpectedTimeSeconds()) {
        createFailure(
                workflow,
                currentStep,
                FailureType.DELAYED_STEP,
                "Step delayed by " + delay +
                " seconds (expected " +
                currentStepDef.getExpectedTimeSeconds() + ")"
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
                Optional<Event> prevEventOpt =
        eventRepository.findTopByWorkflowAndStepNameOrderByOccurredAtDesc(
                workflow,
                prevStep.getStepName()
        );
            if (prevEventOpt.isPresent()) {

    Event prevEvent = prevEventOpt.get();

    long delay = Duration.between(
            prevEvent.getOccurredAt(),
            currentTime
    ).getSeconds();

    if (delay > prevStep.getExpectedTimeSeconds()) {

        createFailure(
                workflow,
                currentStep,
                FailureType.DELAYED_STEP,
                "Step delayed by " + delay +
                " seconds (expected " +
                prevStep.getExpectedTimeSeconds() + ")"
        );
    }


                    if (mlService.isAnomalous(workflow.getId(), currentStep, delay)) {
                        createFailure(workflow, currentStep, FailureType.ML_ANOMALY,
                                "ML detected abnormal delay");
                    }
                }
            }
        }
    }

    /* ================= HELPER ================= */
    private void createFailure(Workflow workflow, String stepName, FailureType type, String message) {

        boolean exists = silentFailureRepository.existsByWorkflowAndStepNameAndFailureTypeAndResolvedFalse(
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


    public List<Event> getEventsByWorkflow(Long workflowId) {
    return eventRepository.findByWorkflowIdOrderByOccurredAt(workflowId);
}


}
