package com.tirtha.sfd.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.tirtha.sfd.model.SilentFailure;
import com.tirtha.sfd.repository.SilentFailureRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FailureResolutionService {

    private final SilentFailureRepository failureRepo;

    /**
     * Deletes failures for a given workflow and step/event.
     *
     * @param workflowId the workflow ID
     * @param stepName the step name or ML anomaly type
     */
  public void resolveFailures(Long workflowId, String stepName) {

    List<SilentFailure> failures =
        failureRepo.findByWorkflow_IdAndStepName(workflowId, stepName);

    failureRepo.deleteAll(failures);
}

public void resolveAllFailures(Long workflowId) {

        List<SilentFailure> failures =
            failureRepo.findByWorkflow_Id(workflowId);

        failureRepo.deleteAll(failures);
    }


}
