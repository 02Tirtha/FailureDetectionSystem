package com.tirtha.sfd.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tirtha.sfd.model.Event;
import com.tirtha.sfd.model.Workflow;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByWorkflowIdOrderByOccurredAt(Long workflowId);

    boolean existsByWorkflowAndStepName(Workflow workflow, String stepName);

    Event findTopByWorkflowAndStepNameOrderByOccurredAtDesc(Workflow workflow, String stepName);
}
