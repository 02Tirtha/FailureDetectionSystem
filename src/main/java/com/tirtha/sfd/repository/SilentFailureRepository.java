package com.tirtha.sfd.repository;

import com.tirtha.sfd.model.SilentFailure;
import com.tirtha.sfd.service.FailureType;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SilentFailureRepository extends JpaRepository<SilentFailure, Long> {

    List<SilentFailure> findByWorkflowId(Long workflowId);
    List<SilentFailure> findByFailureType(FailureType failureType);
    boolean existsByWorkflowIdAndStepNameAndFailureType(Long workflowId, String stepName, FailureType failureType);

}
