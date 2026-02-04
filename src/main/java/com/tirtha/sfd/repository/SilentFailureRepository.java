

package com.tirtha.sfd.repository;

import java.util.List;

import javax.print.attribute.standard.Severity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tirtha.sfd.model.Event;
import com.tirtha.sfd.model.FailureType;
import com.tirtha.sfd.model.SilentFailure;
import com.tirtha.sfd.model.Workflow;

public interface SilentFailureRepository extends JpaRepository<SilentFailure, Long> {

    // Check if unresolved failure exists for a workflow + step + type
    boolean existsByWorkflowAndStepNameAndFailureTypeAndResolvedFalse(
            Workflow workflow, String stepName, FailureType failureType
    );

    // Get all unresolved failures for a workflow
    List<SilentFailure> findByWorkflowAndResolvedFalse(Workflow workflow);

    // Get unresolved failures for a workflow and step
    List<SilentFailure> findByWorkflowAndStepNameAndResolvedFalse(Workflow workflow, String stepName);

    // Optional: Get all failures for a workflow ID (useful in controller)
    List<SilentFailure> findByWorkflow_Id(Long workflowId);

    // Optional: Get all failures by type
    List<SilentFailure> findByFailureType(FailureType type);

    List<SilentFailure> findByWorkflowId(Long workflowId);

    List<SilentFailure> findByWorkflowAndFailureTypeAndResolvedFalse(Workflow workflow, FailureType missingStep);

    List<SilentFailure> findByWorkflow_IdAndStepNameAndResolvedFalse(Long workflowId, String stepName);

    List<SilentFailure> findByWorkflow_IdAndResolvedFalse(Long workflowId);

    List<Event> findByWorkflowIdAndStepNameAndFailureTypeAndResolvedTrue(Long workflowId, String stepName,
            String string);

    List<SilentFailure> findByWorkflowAndStepNameAndFailureType(Workflow workflow, String stepName, FailureType type);

        boolean existsByWorkflowAndStepNameAndFailureType(
                Workflow workflow,
                String stepName,
                FailureType failureType
        );

            @Modifying
        @Query("""
        UPDATE SilentFailure f
        SET f.resolved = true, f.resolvedAt = CURRENT_TIMESTAMP
        WHERE f.workflow.id = :workflowId
        AND f.stepName = :stepName
        AND f.failureType = 'DELAYED_STEP'
        AND f.resolved = false
        """)
        void resolveDelayedSteps(
                @Param("workflowId") Long workflowId,
                @Param("stepName") String stepName
        );

        long count();
        long countByResolvedFalse();
        long countBySeverity(com.tirtha.sfd.model.Severity high);        
        long countByFailureType(FailureType failureType);

}
