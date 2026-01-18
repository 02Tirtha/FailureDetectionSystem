package com.tirtha.sfd.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tirtha.sfd.model.Event;
import com.tirtha.sfd.repository.EventRepository;
import com.tirtha.sfd.service.FailureDetectionService;
import com.tirtha.sfd.service.MlLearningService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventRepository eventRepository;
    private final MlLearningService mlLearningService;
    private final FailureDetectionService failureDetectionService;

    // Create events
    @PostMapping
public List<Event> createEvents(@RequestBody List<Event> events) {

    List<Event> savedEvents = eventRepository.saveAll(events);

    for (Event event : savedEvents) {

        // ML only for EMAIL_SENT
        if ("EMAIL_SENT".equals(event.getStepName())) {

            // find previous event
            eventRepository
                .findByWorkflowIdOrderByOccurredAt(event.getWorkflow().getId())
                .stream()
                .filter(e -> e.getOccurredAt().isBefore(event.getOccurredAt())) //exclude current event to avoid matching itself
                .reduce((first, second) -> second) //Go through the list and keep replacing the value with the next one to get most recent event
                .ifPresent(previousEvent -> {

                    long duration = java.time.Duration.between(
                            previousEvent.getOccurredAt(),
                            event.getOccurredAt()
                    ).getSeconds();

                    mlLearningService.updateThreshold(
                            event.getWorkflow().getId(),
                            "EMAIL_SENT",
                            duration
                    );
                });
        }
    }

    // 🚀 AUTO TRIGGER FAILURE DETECTION
    savedEvents.stream()
            .map(e -> e.getWorkflow().getId())
            .distinct()
            .forEach(failureDetectionService::detectFailures);

    failureDetectionService.detectFailures(events.get(0).getWorkflow().getId());


    return savedEvents;
}


    // Get all events for a workflow
    @GetMapping
    public List<Event> getEventsByWorkflow(@RequestParam Long workflowId) {
        return eventRepository.findByWorkflowIdOrderByOccurredAt(workflowId);
    }
  

}

