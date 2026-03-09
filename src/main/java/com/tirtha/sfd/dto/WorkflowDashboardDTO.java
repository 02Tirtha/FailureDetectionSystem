package com.tirtha.sfd.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WorkflowDashboardDTO {
     private Long workflowId;
    private String workflowName;

    private long totalFailures;
    private long unresolvedFailures;

    private LocalDateTime lastFailureTime;
}
