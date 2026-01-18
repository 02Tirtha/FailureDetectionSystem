package com.tirtha.sfd.service;

import com.tirtha.sfd.model.MlThreshold;
import com.tirtha.sfd.repository.MlThresholdRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MlLearningService {

    private final MlThresholdRepository mlThresholdRepository;

    /**
     * Update ML statistics for a workflow step using incremental learning
     */
    public void updateThreshold(
            Long workflowId,
            String stepName,
            long durationSeconds
    ) {

        MlThreshold threshold = mlThresholdRepository
                .findByWorkflowIdAndStepName(workflowId, stepName)
                .orElseGet(() -> createNewThreshold(workflowId, stepName));

        System.out.println(
        "[ML-LEARN] workflow=" + workflowId +
        ", step=" + stepName +
        ", existingSamples=" + threshold.getSampleCount()
);        
        int n = threshold.getSampleCount();
        double oldMean = threshold.getMeanDuration();
        double oldStd = threshold.getStdDev();

        System.out.println(
        "[ML-LEARN] incoming duration=" + durationSeconds +
        "s, oldMean=" + oldMean +
        ", oldStd=" + oldStd
);


        // Incremental mean calculation
        double newMean = (oldMean * n + durationSeconds) / (n + 1);

        // Incremental standard deviation
        double variance = ((n * Math.pow(oldStd, 2))
                + Math.pow(durationSeconds - oldMean, 2)) / (n + 1);

        double newStd = Math.sqrt(variance);

        threshold.setMeanDuration(newMean);
        threshold.setStdDev(newStd);
        threshold.setSampleCount(n + 1);

        System.out.println(
        "[ML-LEARN] UPDATED → newMean=" + newMean +
        ", newStd=" + newStd +
        ", samples=" + (n + 1)
);

        mlThresholdRepository.save(threshold);
    }

    private MlThreshold createNewThreshold(Long workflowId, String stepName) {
        MlThreshold threshold = new MlThreshold();
        threshold.setWorkflowId(workflowId);
        threshold.setStepName(stepName);
        threshold.setMeanDuration(0);
        threshold.setStdDev(0);
        threshold.setSampleCount(0);
        return threshold;
    }
}
