package com.tirtha.sfd.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tirtha.sfd.model.Workflow;
import com.tirtha.sfd.model.WorkflowStep;

public interface WorkflowStepRepository extends JpaRepository<WorkflowStep, Long> {

    List<WorkflowStep> findByWorkflowIdOrderByStepOrder(Long workflowId);

    List<WorkflowStep> findByWorkflowOrderByStepOrderAsc(Workflow workflow);
    WorkflowStep findByWorkflowAndStepName(Workflow workflow, String stepName);

} 