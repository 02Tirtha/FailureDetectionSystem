package com.tirtha.sfd.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tirtha.sfd.model.Event;
import com.tirtha.sfd.model.FailureType;
import com.tirtha.sfd.model.SilentFailure;
import com.tirtha.sfd.model.Workflow;
import com.tirtha.sfd.repository.EventRepository;
import com.tirtha.sfd.repository.SilentFailureRepository;
import com.tirtha.sfd.repository.WorkflowRepository;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class EventService {

    private static final Logger logger = LoggerFactory.getLogger(EventService.class);

    private final EventRepository eventRepository;
    private final WorkflowRepository workflowRepository;
    private final SilentFailureRepository silentFailureRepository;
    private final FailureDetectionService failureDetectionService;

    /* ================= HANDLE SINGLE EVENT ================= */
    @Transactional
    public Event handleEvent(Event event) {

        if (event.getWorkflow() == null || event.getWorkflow().getId() == null) {
            throw new RuntimeException("workflow.id is required");
        }

        Long workflowId = event.getWorkflow().getId();
        Workflow workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new RuntimeException("Workflow not found: " + workflowId));

        event.setWorkflow(workflow);
        Event savedEvent = eventRepository.save(event);

        try {
            autoResolveFailures(savedEvent);
            failureDetectionService.detectAndRecordFailures(savedEvent);
        } catch (Exception ex) {
            logger.warn("Failure detection skipped for event id {}: {}", savedEvent.getId(), ex.getMessage());
        }

        return savedEvent;
    }

    /* ================= AUTO RESOLVE ================= */
    @Transactional
    public void autoResolveFailures(Event event) {

        Workflow workflow = event.getWorkflow();
        String currentStep = event.getStepName();

        List<SilentFailure> unresolvedMissing =
                silentFailureRepository.findByWorkflowAndStepNameAndFailureType(
                        workflow,
                        currentStep,
                        FailureType.MISSING_STEP
                );

        if (unresolvedMissing.isEmpty()) {
            return;
        }

        silentFailureRepository.deleteAll(unresolvedMissing);
    }

    /* ================= GET EVENTS BY WORKFLOW ================= */
    @Transactional(readOnly = true)
    public List<Event> getEventsByWorkflow(Long workflowId) {
        // Use your repo method exactly
        return eventRepository.findByWorkflowIdOrderByOccurredAt(workflowId);
    }

}
