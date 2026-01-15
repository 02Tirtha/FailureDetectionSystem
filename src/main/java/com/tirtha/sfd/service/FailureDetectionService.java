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
                        "MISSING_STEP",
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

                if (duration > step.getExpectedTimeSeconds()) {
                    saveFailure(
                            "DELAYED_STEP",
                            step.getStepName(),
                            "Step exceeded expected time",
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
                "Step: " + failure.getStepName() + "\n" +
                "Message: " + failure.getMessage() + "\n" +
                "Workflow ID: " + failure.getWorkflow().getId() + "\n" +
                "Detected at: " + failure.getDetectedAt()
        );

        mailSender.send(message);
    }

    private void saveFailure(String type, String stepName, String message, Long workflowId) {

           boolean exists = failureRepository.existsByWorkflowIdAndStepNameAndFailureType
           (workflowId, stepName, type);


        if (exists) return;

        SilentFailure failure = new SilentFailure();
        failure.setFailureType(type);
        failure.setStepName(stepName);
        failure.setMessage(message);
        failure.setDetectedAt(LocalDateTime.now());

        Workflow workflow = new Workflow();
        workflow.setId(workflowId);
        failure.setWorkflow(workflow);

        failureRepository.save(failure);
        sendFailureEmail(failure);
    }
}
