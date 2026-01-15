package com.tirtha.sfd.repository;

import com.tirtha.sfd.model.SilentFailure;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SilentFailureRepository extends JpaRepository<SilentFailure, Long> {

    List<SilentFailure> findByWorkflowId(Long workflowId);
    List<SilentFailure> findByFailureType(String failureType);
    boolean existsByWorkflowIdAndStepNameAndFailureType(Long workflowId, String stepName, String failureType);

}
