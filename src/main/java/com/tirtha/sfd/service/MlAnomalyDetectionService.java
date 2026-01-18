package com.tirtha.sfd.service;

import com.tirtha.sfd.model.MlThreshold;
import com.tirtha.sfd.repository.MlThresholdRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MlAnomalyDetectionService {

    private final MlThresholdRepository mlThresholdRepository;

    /**
     * Checks if a step duration is anomalous
     */
    public boolean isAnomalous(
            Long workflowId,
            String stepName,
            long durationSeconds
    ) {

        MlThreshold threshold = mlThresholdRepository
                .findByWorkflowIdAndStepName(workflowId, stepName)
                .orElse(null);

        // Not enough data → cannot judge
        if (threshold == null || threshold.getSampleCount() < 5) {
            return false;
        }

        double limit =
                threshold.getMeanDuration() + (2 * threshold.getStdDev());

        return durationSeconds > limit;
    }
}
