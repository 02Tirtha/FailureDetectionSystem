package com.tirtha.sfd.service;

import com.tirtha.sfd.model.*;
import com.tirtha.sfd.repository.*;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FailureDetectionService {

    private final WorkflowStepRepository stepRepository;
    private final EventRepository eventRepository;
    private final SilentFailureRepository failureRepository;
    private final JavaMailSender mailSender;
    private final MlAnomalyDetectionService mlAnomalyDetectionService;
    private final MlLearningService mlLearningService;

   public void detectFailures(Long workflowId) {

    List<WorkflowStep> steps =
            stepRepository.findByWorkflowIdOrderByStepOrder(workflowId);

    List<Event> events =
            eventRepository.findByWorkflowIdOrderByOccurredAt(workflowId);

    Map<String, Event> eventMap = new HashMap<>();
    for (Event e : events) {
        eventMap.put(e.getStepName(), e);
    }

    // Find last completed step order
    int lastCompletedOrderTemp = -1;
    WorkflowStep lastCompletedStep = null;

    for (WorkflowStep step : steps) {
        if (eventMap.containsKey(step.getStepName())) {
            lastCompletedOrderTemp = step.getStepOrder();
            lastCompletedStep = step;
        }
    }

    final int lastCompletedOrder = lastCompletedOrderTemp;
    Event previousEvent = null;

    /* ---------- STEP LOOP ---------- */
    for (WorkflowStep step : steps) {

        Event currentEvent = eventMap.get(step.getStepName());

        /* ---------- SKIPPED MISSING ---------- */
        if (currentEvent == null) {

            if (step.getStepOrder() < lastCompletedOrder) {
                saveFailure(
                        FailureType.MISSING_STEP,
                        Severity.HIGH,
                        step.getStepName(),
                        "Step was skipped",
                        workflowId
                );
            }
            continue;
        }

        // Auto-resolve missing if step arrived
        autoResolveFailureIfExists(
                workflowId,
                step.getStepName(),
                FailureType.MISSING_STEP
        );

        /* ---------- DELAY CHECK ---------- */
        if (previousEvent != null) {

            long duration = Duration.between(
                    previousEvent.getOccurredAt(),
                    currentEvent.getOccurredAt()
            ).getSeconds();

            long expected = step.getExpectedTimeSeconds();
            boolean mlTriggered = false;

            if ("EMAIL_SENT".equals(step.getStepName())) {
                if (mlAnomalyDetectionService.isAnomalous(
                        workflowId,
                        step.getStepName(),
                        duration
                )) {
                    mlTriggered = true;
                    saveFailure(
                            FailureType.ML_ANOMALY,
                            Severity.HIGH,
                            step.getStepName(),
                            "ML detected abnormal delay: " + duration,
                            workflowId
                    );
                }
            }

            if (!mlTriggered && duration > expected) {
                Severity severity =
                        duration <= 2 * expected ? Severity.MEDIUM : Severity.HIGH;

                saveFailure(
                        FailureType.DELAYED_STEP,
                        severity,
                        step.getStepName(),
                        "Step delayed by " + duration +
                                " seconds (expected " + expected + ")",
                        workflowId
                );
            }
        }

            // Keep reference to previous step BEFORE updating
        Event prev = previousEvent;

        previousEvent = currentEvent;

        // Auto-resolve delay of PREVIOUS step once CURRENT step arrives
        if (prev != null) {
            failureRepository
                .findFirstByWorkflow_IdAndStepNameAndFailureTypeAndResolvedFalse(
                    workflowId,
                    prev.getStepName(),
                    FailureType.DELAYED_STEP
                )
                .ifPresent(failure -> {
                    failure.setResolved(true);
                    failure.setResolvedAt(LocalDateTime.now());
                    failureRepository.save(failure);
                    System.out.println("✅ Auto-resolved DELAYED_STEP for: " + prev.getStepName());
                });
        }

        

    }

    /* ---------- TIMEOUT-BASED MISSING (CRITICAL FIX) ---------- */
    if (lastCompletedStep != null) {

        WorkflowStep nextExpectedStep = steps.stream()
                .filter(s -> s.getStepOrder() == lastCompletedOrder + 1)
                .findFirst()
                .orElse(null);

        if (nextExpectedStep != null &&
            !eventMap.containsKey(nextExpectedStep.getStepName())) {

            Event lastEvent = eventMap.get(lastCompletedStep.getStepName());
            long waitedSeconds = Duration.between(
                    lastEvent.getOccurredAt(),
                    LocalDateTime.now()
            ).getSeconds();

            if (waitedSeconds > nextExpectedStep.getExpectedTimeSeconds()) {
                saveFailure(
                        FailureType.MISSING_STEP,
                        Severity.HIGH,
                        nextExpectedStep.getStepName(),
                        "Step did not occur within expected time",
                        workflowId
                );
            }
        }

        boolean workflowCompleted =
            eventMap.containsKey("WORKFLOW_COMPLETED");

        if (workflowCompleted) {
            return; // stop detection for this workflow
        }

    
    }
}



    @Async
    public void sendFailureEmail(SilentFailure failure) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("admin@example.com");
        message.setSubject("Workflow Failure Detected: " + failure.getStepName());
        message.setText(
                "Failure Type: " + failure.getFailureType() + "\n" +
                        "Severity: " + failure.getSeverity() + "\n" +
                        "Step: " + failure.getStepName() + "\n" +
                        "Message: " + failure.getMessage() + "\n" +
                        "Workflow ID: " + failure.getWorkflow().getId() + "\n" +
                        "Detected at: " + failure.getDetectedAt()
        );

        mailSender.send(message);
    }

    private void saveFailure(FailureType type, Severity severity, String stepName, String message, Long workflowId) {
    boolean exists =
    type == FailureType.DELAYED_STEP
        ? failureRepository.existsByWorkflow_IdAndStepNameAndFailureType(
              workflowId, stepName, type)
        : failureRepository.existsByWorkflow_IdAndStepNameAndFailureTypeAndResolvedFalse(
              workflowId, stepName, type);
        if (exists) return;

        SilentFailure failure = new SilentFailure();
        failure.setFailureType(type);
        failure.setSeverity(severity);
        failure.setStepName(stepName);
        failure.setMessage(message);
        failure.setDetectedAt(LocalDateTime.now());
        failure.setResolved(false);

        Workflow workflow = new Workflow();
        workflow.setId(workflowId);
        failure.setWorkflow(workflow);

        failureRepository.save(failure);
        sendFailureEmail(failure);
    }

    @Transactional
    private void autoResolveFailureIfExists(Long workflowId, String stepName, FailureType type) {
        failureRepository
                .findFirstByWorkflow_IdAndStepNameAndFailureTypeAndResolvedFalse(workflowId, stepName, type)
                .ifPresent(failure -> {
                    failure.setResolved(true);
                    failure.setResolvedAt(LocalDateTime.now());
                    failureRepository.save(failure);
                    System.out.println("✅ Auto-resolved " + type + " for step: " + stepName);
                });
    }

        @Transactional
private void autoResolveAllDelayedStepsUpToCurrent(Long workflowId, Map<String, Event> eventMap, String currentStepName) {
    List<SilentFailure> unresolved = failureRepository.findByWorkflow_IdAndFailureTypeAndResolvedFalse(workflowId, FailureType.DELAYED_STEP);

    for (SilentFailure failure : unresolved) {
        // Check if the step exists in the current events
        if (eventMap.containsKey(failure.getStepName())) {
            failure.setResolved(true);
            failure.setResolvedAt(LocalDateTime.now());
            failureRepository.save(failure);
            System.out.println("✅ Auto-resolved DELAYED_STEP for: " + failure.getStepName());
        }
    }
}


    @Transactional
    public void resolveFailure(Long workflowId) {
        List<SilentFailure> failures = failureRepository.findByWorkflow_IdAndResolvedFalse(workflowId);
        for (SilentFailure failure : failures) {
            failure.setResolved(true);
            failure.setResolvedAt(LocalDateTime.now());
        }
        failureRepository.saveAll(failures);
    }

    // ----------------- ML detection logic -----------------
    public void detectForWorkflow(Long workflowId) {
        System.out.println("ML DETECTION STARTED for workflow " + workflowId);

        List<Event> events = eventRepository.findByWorkflowIdOrderByOccurredAt(workflowId);
        if (events.size() < 2) return;

        Event current = events.get(events.size() - 1);
        if (!"EMAIL_SENT".equals(current.getStepName())) return;

        boolean alreadyReported = failureRepository.existsByWorkflow_IdAndStepNameAndFailureTypeAndResolvedFalse(
                workflowId, "EMAIL_SENT", FailureType.ML_ANOMALY
        );
        if (alreadyReported) return;

        Event previous = events.get(events.size() - 2);
        long duration = Duration.between(previous.getOccurredAt(), current.getOccurredAt()).getSeconds();

        mlLearningService.updateThreshold(workflowId, current.getStepName(), duration);

        boolean anomalous = mlAnomalyDetectionService.isAnomalous(workflowId, "EMAIL_SENT", duration);
        if (!anomalous) return;

        saveFailure(
                FailureType.ML_ANOMALY,
                Severity.HIGH,
                "EMAIL_SENT",
                "ML detected abnormal delay: " + duration + " seconds",
                workflowId
        );

        System.out.println("🚨 ML ANOMALY SAVED");
    }
}
