package com.tirtha.sfd.repository;

import com.tirtha.sfd.model.MlEnabledStep;
import com.tirtha.sfd.model.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MlEnabledStepRepository extends JpaRepository<MlEnabledStep, Long> {

    List<MlEnabledStep> findByWorkflowAndStepName(Workflow workflow, String stepName);

}
