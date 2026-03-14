package com.tirtha.sfd.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tirtha.sfd.model.Event;
import com.tirtha.sfd.model.Workflow;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByWorkflowId(Long workflowId);

    // Get all events for a workflow ordered by time
    List<Event> findByWorkflowIdOrderByOccurredAt(Long workflowId);

    // Check if a step occurred **before a specific time**
//     boolean existsByWorkflowAndStepNameAndOccurredAtBefore(
//             Workflow workflow, String stepName, LocalDateTime occurredAt
//     );

        @Query("""
    select count(e) > 0
    from Event e
    where e.workflow = :workflow
      and e.stepName = :stepName
""")
boolean stepOccurred(
    @Param("workflow") Workflow workflow,
    @Param("stepName") String stepName
);

    // Find the latest event for a step **before a given time** (used for DELAYED_STEP)
    Event findTopByWorkflowAndStepNameAndOccurredAtBeforeOrderByOccurredAtDesc(
            Workflow workflow, String stepName, LocalDateTime occurredAt
    );

    // Optional: check if a step exists anywhere (can still be used)
    boolean existsByWorkflowAndStepName(Workflow workflow, String stepName);

    // Optional: find the last occurrence of a step
    Optional<Event> findTopByWorkflowAndStepNameOrderByOccurredAtDesc(Workflow workflow, String stepName);

    boolean existsByWorkflow_IdAndStepName(Long id, String stepName);

    Event findTopByWorkflowAndOccurredAtBeforeOrderByOccurredAtDesc(
            Workflow workflow, LocalDateTime occurredAt
    );

    @Query("""
    select count(e) > 0
    from Event e
    where e.workflow = :workflow
      and e.stepName = :stepName
      and e.occurredAt >= :startTime
      and e.occurredAt <= :endTime
""")
    boolean existsByWorkflowAndStepNameAndOccurredAtBetween(
            @Param("workflow") Workflow workflow,
            @Param("stepName") String stepName,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    Event findTopByWorkflowAndOccurredAtBetweenOrderByOccurredAtDesc(
            Workflow workflow, LocalDateTime startTime, LocalDateTime endTime
    );

    Event findTopByWorkflowAndStepNameAndIdLessThanEqualOrderByIdDesc(
            Workflow workflow, String stepName, Long id
    );

    Event findTopByWorkflowAndStepNameAndIdLessThanOrderByIdDesc(
            Workflow workflow, String stepName, Long id
    );

    Event findTopByWorkflowAndIdLessThanOrderByIdDesc(
            Workflow workflow, Long id
    );

    boolean existsByWorkflowAndStepNameAndIdBetween(
            Workflow workflow, String stepName, Long startId, Long endId
    );

    Event findTopByWorkflowIdOrderByOccurredAtDesc(Long workflowId);

 @Query("select e.stepName from Event e where e.workflow = :workflow")
    List<String> findStepNamesByWorkflow(@Param("workflow") Workflow workflow);

    Optional<Event> findFirstByWorkflowAndStepNameAndOccurredAtAfterOrderByOccurredAtAsc(
        Workflow workflow,
        String stepName,
        LocalDateTime occurredAt
);
//     List<Event> findByWorkflowIdAndOccurredAtBeforeOrderByOccurredAt(Long id, LocalDateTime occurredAt);
}
