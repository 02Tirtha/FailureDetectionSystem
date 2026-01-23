package com.tirtha.sfd.repository;

import com.tirtha.sfd.model.FailureType;
import com.tirtha.sfd.model.SilentFailure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SilentFailureRepository
        extends JpaRepository<SilentFailure, Long> {

    boolean existsByWorkflow_IdAndStepNameAndFailureTypeAndResolvedFalse(
            Long workflowId,
            String stepName,
            FailureType failureType
    );

    List<SilentFailure> findByWorkflow_IdAndResolvedFalse(Long workflowId);
    List<SilentFailure> findByWorkflowId(Long workflowId);
    List<SilentFailure> findByFailureType(FailureType failureType);
}


