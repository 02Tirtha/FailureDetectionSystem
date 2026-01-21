package com.tirtha.sfd.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tirtha.sfd.model.Event;
import com.tirtha.sfd.model.Workflow;
import com.tirtha.sfd.repository.EventRepository;
import com.tirtha.sfd.repository.WorkflowRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final WorkflowRepository workflowRepository;

    @Transactional
public List<Event> saveEventsWithWorkflow(List<Event> events) {

    for (Event event : events) {
        Long workflowId = event.getWorkflow().getId();

        Workflow workflow = workflowRepository
            .findById(workflowId)
            .orElseThrow(() ->
                new RuntimeException("Workflow not found: " + workflowId)
            );

        event.setWorkflow(workflow); // ✅ attach existing workflow
    }

    return eventRepository.saveAll(events);
}


}
