package com.tirtha.sfd.service;

import com.tirtha.sfd.model.*;
import com.tirtha.sfd.repository.*;
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


    public void detectFailures(Long workflowId) {

        List<WorkflowStep> steps =
                stepRepository.findByWorkflowIdOrderByStepOrder(workflowId);

        List<Event> events =
                eventRepository.findByWorkflowIdOrderByOccurredAt(workflowId);

        // Map stepName → event
        Map<String, Event> eventMap = new HashMap<>();
        for (Event event : events) {
            eventMap.put(event.getStepName(), event);
        }

        Event previousEvent = null;

        for (WorkflowStep step : steps) {

            Event currentEvent = eventMap.get(step.getStepName());

            // Missing step
            if (currentEvent == null) {
                saveFailure(
                    FailureType.MISSING_STEP,
                    Severity.HIGH,
                    step.getStepName(),
                    "Expected step did not occur",
                    workflowId
            );
                continue;
            }

           // Delay check (previous → current)
        if (previousEvent != null) {

            long duration = Duration.between(
                    previousEvent.getOccurredAt(),
                    currentEvent.getOccurredAt()
            ).getSeconds();

            boolean mlTriggered = false;
            // ML anomaly check (only for EMAIL_SENT)
                if ("EMAIL_SENT".equals(step.getStepName())) {

                    boolean anomalous = mlAnomalyDetectionService.isAnomalous(
                            workflowId,
                            step.getStepName(),
                            duration
                    );

                    if (anomalous) {
                        mlTriggered = true;
                        saveFailure(
                                FailureType.ML_ANOMALY,
                                Severity.HIGH,
                                step.getStepName(),
                                "ML detected abnormal delay: " + duration + " seconds",
                                workflowId
                        );
                    }
                }
            // skip delay check if ML already triggered 
            long expected = step.getExpectedTimeSeconds();

            if (!mlTriggered && duration > expected) {

                Severity severity;

                if (duration <= 2 * expected) {
                    severity = Severity.MEDIUM;
                } else {
                    severity = Severity.HIGH;
                }

                saveFailure(
                        FailureType.DELAYED_STEP,
                        severity,
                        step.getStepName(),
                        "Step delayed by " + duration + " seconds (expected " + expected + ")",
                        workflowId
                );
            }
}


            previousEvent = currentEvent;
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

    private void saveFailure(FailureType type,
        Severity severity,
        String stepName,
        String message,
        Long workflowId) {

           boolean exists = failureRepository.existsByWorkflowIdAndStepNameAndFailureType
           (workflowId, stepName, type);


        if (exists) return;
        SilentFailure failure = new SilentFailure();
        failure.setFailureType(type);
        failure.setSeverity(severity);
        failure.setStepName(stepName);
        failure.setMessage(message);
        failure.setDetectedAt(LocalDateTime.now());

        Workflow workflow = new Workflow();
        workflow.setId(workflowId);
        failure.setWorkflow(workflow);

        failureRepository.save(failure);
        sendFailureEmail(failure);

        
    }
    public void detectForWorkflow(Long workflowId) {

    System.out.println("ML DETECTION STARTED for workflow " + workflowId);

    // 1️⃣ Fetch events safely
    List<Event> events =
            eventRepository.findByWorkflowIdOrderByOccurredAt(workflowId);

    if (events.size() < 2) {
        System.out.println("[ML-DETECT] Not enough events to compare");
        return;
    }

    // 2️⃣ Get last event
    Event current = events.get(events.size() - 1);

    // ML only for EMAIL_SENT
    if (!"EMAIL_SENT".equals(current.getStepName())) {
        System.out.println("[ML-DETECT] Last step is not EMAIL_SENT");
        return;
    }

    boolean alreadyReported =
            failureRepository.existsByWorkflowIdAndStepNameAndFailureType(
                    workflowId,
                    "EMAIL_SENT",
                    FailureType.ML_ANOMALY
            );

    if (alreadyReported) {
        System.out.println("[ML-DETECT] ML anomaly already reported, skipping");
        return;
    }

    // 3️⃣ Get previous event
    Event previous = events.get(events.size() - 2);

    long duration = Duration.between(
            previous.getOccurredAt(),
            current.getOccurredAt()
    ).getSeconds();

    System.out.println("[ML-DETECT] Duration = " + duration + " seconds");

    // 4️⃣ Check anomaly
    boolean anomalous =
            mlAnomalyDetectionService.isAnomalous(
                    workflowId,
                    "EMAIL_SENT",
                    duration
            );

    if (!anomalous) {
        System.out.println("[ML-DETECT] Not anomalous");
        return;
    }

    // 5️⃣ Save anomaly (avoid duplicates)
    boolean exists = failureRepository
            .existsByWorkflowIdAndStepNameAndFailureType(
                    workflowId,
                    "EMAIL_SENT",
                    FailureType.ML_ANOMALY
            );

    if (!exists) {
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

}
