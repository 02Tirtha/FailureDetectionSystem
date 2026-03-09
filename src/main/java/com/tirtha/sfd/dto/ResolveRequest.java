package com.tirtha.sfd.dto;

public class ResolveRequest {
    private Long workflowId;
    private String stepName;

    public Long getWorkflowId() { return workflowId; }
    public void setWorkflowId(Long workflowId) { this.workflowId = workflowId; }

    public String getStepName() { return stepName; }
    public void setStepName(String stepName) { this.stepName = stepName; }
}

