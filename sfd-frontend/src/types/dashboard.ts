export interface DashboardStats{
    totalWorkflows: number;
    totalFailures:number;
    unresolvedFailures: number;
    highSeverityFailures: number;
    missingCount: number;
    delayedCount: number;
}

export interface FailureCount{
    type: String;
    count: number;
}

