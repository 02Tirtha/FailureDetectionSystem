

package com.tirtha.sfd.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tirtha.sfd.model.FailureType;
import com.tirtha.sfd.model.SilentFailure;
import com.tirtha.sfd.model.Workflow;

public interface SilentFailureRepository extends JpaRepository<SilentFailure, Long> {

    Optional<SilentFailure> findTopByWorkflowAndStepNameAndFailureTypeOrderByDetectedAtDesc(
        Workflow workflow,
        String stepName,
        FailureType failureType
);

    // Optional: Get all failures for a workflow ID (useful in controller)
    List<SilentFailure> findByWorkflow_Id(Long workflowId);

    // Optional: Get all failures by type
    List<SilentFailure> findByFailureType(FailureType type);

    List<SilentFailure> findByWorkflowAndStepName(Workflow workflow, String stepName);

    List<SilentFailure> findByWorkflow_IdAndStepName(Long workflowId, String stepName);

   

    @Modifying
        @Query("""
            DELETE FROM SilentFailure f
            WHERE f.workflow.id = :workflowId
        """)
        void deleteByWorkflowId(@Param("workflowId") Long workflowId);

    @Query("select e.stepName from Event e where e.workflow = :workflow")
    List<String> findStepNamesByWorkflow(@Param("workflow") Workflow workflow);


    List<SilentFailure> findByWorkflowAndStepNameAndFailureType(Workflow workflow, String stepName, FailureType type);

        boolean existsByWorkflowAndStepNameAndFailureType(
                Workflow workflow,
                String stepName,
                FailureType failureType
        );

        @Modifying
        @Query("""
            DELETE FROM SilentFailure f
            WHERE f.workflow = :workflow
            AND f.stepName = :stepName
            AND f.failureType = :failureType
        """)
        void deleteByWorkflowAndStepNameAndFailureType(
                @Param("workflow") Workflow workflow,
                @Param("stepName") String stepName,
                @Param("failureType") FailureType failureType
        );

        @Modifying
        @Query("""
        DELETE FROM SilentFailure f
        WHERE f.workflow.id = :workflowId
        AND f.stepName = :stepName
        AND f.failureType = 'DELAYED_STEP'
        """)
        void resolveDelayedSteps(
                @Param("workflowId") Long workflowId,
                @Param("stepName") String stepName
        );

        long countBySeverity(com.tirtha.sfd.model.Severity high);        
        long countByFailureType(FailureType failureType);

        @Query("""
        SELECT 
        COUNT(f),
        MAX(f.detectedAt)
        FROM SilentFailure f
        WHERE f.workflow.id = :workflowId
        """)
        Object[] getFailureStats(@Param("workflowId") Long workflowId);

        Object[] getFailureStatsByWorkflowId(Long id);


}
