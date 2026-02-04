package com.tirtha.sfd.service;

import com.tirtha.sfd.dto.DashboardStatsDto;
import com.tirtha.sfd.model.FailureType;
import com.tirtha.sfd.model.Severity;
import com.tirtha.sfd.repository.SilentFailureRepository;
import com.tirtha.sfd.repository.WorkflowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final WorkflowRepository workflowRepository;
    private final SilentFailureRepository silentFailureRepository;

    public DashboardStatsDto getStats() {
        return new DashboardStatsDto(
                workflowRepository.count(),
                silentFailureRepository.count(),
                silentFailureRepository.countByResolvedFalse(),
                silentFailureRepository.countBySeverity(Severity.HIGH),
                silentFailureRepository.countByFailureType(FailureType.MISSING_STEP),
                silentFailureRepository.countByFailureType(FailureType.DELAYED_STEP),
                silentFailureRepository.countByFailureType(FailureType.ML_ANOMALY)
            );
    }
}
