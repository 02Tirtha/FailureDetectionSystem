package com.tirtha.sfd.service;

import com.tirtha.sfd.model.MlThreshold;
import com.tirtha.sfd.repository.MlThresholdRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class MlThresholdService {

    private final MlThresholdRepository thresholdRepository;

    public MlThresholdService(MlThresholdRepository thresholdRepository) {
        this.thresholdRepository = thresholdRepository;
    }

    private MlThreshold createNewThreshold(Long workflowId, String stepName) {
        MlThreshold t = new MlThreshold();
        t.setWorkflowId(workflowId);
        t.setStepName(stepName);
        t.setSampleCount(0);
        t.setMeanDuration(0);
        t.setStdDev(0);
        return t;
    }

    /**
     * ✅ LEARNING METHOD
     * This MUST be called with duration between events
     */
    @Transactional
    public MlThreshold updateThreshold(
            Long workflowId,
            String stepName,
            long durationSeconds
    ) {

        MlThreshold threshold = thresholdRepository
                .findByWorkflowIdAndStepName(workflowId, stepName)
                .orElseGet(() -> createNewThreshold(workflowId, stepName));

        int n = threshold.getSampleCount();
        double mean = threshold.getMeanDuration();
        double std = threshold.getStdDev();

        // 🔥 Online mean + std (Welford)
        n++;
        double delta = durationSeconds - mean;
        mean = mean + (delta / n);
        double delta2 = durationSeconds - mean;
        std = Math.sqrt(((n - 1) * std * std + delta * delta2) / n);

        threshold.setSampleCount(n);
        threshold.setMeanDuration(mean);
        threshold.setStdDev(std);

        return thresholdRepository.save(threshold);
    }
}
