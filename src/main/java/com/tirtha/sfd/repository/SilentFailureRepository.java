package com.tirtha.sfd.repository;

import com.tirtha.sfd.model.FailureType;
import com.tirtha.sfd.model.SilentFailure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SilentFailureRepository
        extends JpaRepository<SilentFailure, Long> {

    boolean existsByWorkflow_IdAndStepNameAndFailureTypeAndResolvedFalse(
            Long workflowId,
            String stepName,
            FailureType failureType
    );

    Optional<SilentFailure> findFirstByWorkflow_IdAndStepNameAndFailureTypeAndResolvedFalse(
            Long workflowId,
            String stepName,
            FailureType failureType
    );

    List<SilentFailure> findByWorkflow_IdAndResolvedFalse(Long workflowId);
    List<SilentFailure> findByWorkflowId(Long workflowId);
    List<SilentFailure> findByFailureType(FailureType failureType);

    List<SilentFailure> findByWorkflow_IdAndStepNameAndResolvedFalse(Long workflowId, String stepName);

    List<SilentFailure> findByWorkflow_IdAndFailureTypeAndResolvedFalse(Long workflowId, FailureType failureType);
    boolean existsByWorkflow_IdAndStepNameAndFailureType(
        Long workflowId, String stepName, FailureType failureType);


}


