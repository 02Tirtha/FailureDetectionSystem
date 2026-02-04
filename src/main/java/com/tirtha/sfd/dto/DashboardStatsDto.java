package com.tirtha.sfd.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DashboardStatsDto {
    private long totalWorkflows;
    private long totalFailures;
    private long unresolvedFailures;
    private long highSeverityFailures;
    private long missingCount;
    private long delayedCount;
    private long mlAnomalyCount;
}
