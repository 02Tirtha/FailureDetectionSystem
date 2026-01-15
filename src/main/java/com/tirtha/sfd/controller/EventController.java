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

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventRepository eventRepository;

    // Create events
    @PostMapping
    public List<Event> createEvents(@RequestBody List<Event> events) {
        return eventRepository.saveAll(events);
    }

    // Get all events for a workflow
    @GetMapping
    public List<Event> getEventsByWorkflow(@RequestParam Long workflowId) {
        return eventRepository.findByWorkflowIdOrderByOccurredAt(workflowId);
    }
}

