package com.tirtha.sfd.service;

import com.tirtha.sfd.model.FailureType;
import com.tirtha.sfd.model.MlEnabledStep;
import com.tirtha.sfd.model.MlThreshold;
import com.tirtha.sfd.model.Severity;
import com.tirtha.sfd.model.SilentFailure;
import com.tirtha.sfd.model.Workflow;
import com.tirtha.sfd.repository.MlEnabledStepRepository;
import com.tirtha.sfd.repository.MlThresholdRepository;
import com.tirtha.sfd.repository.WorkflowRepository;
import com.tirtha.sfd.repository.SilentFailureRepository;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MlAnomalyDetectionService {

    private final MlThresholdRepository mlThresholdRepository;
    private final MlEnabledStepRepository stepRepository;
    private final WorkflowRepository workflowRepository;
    private final SilentFailureRepository silentFailureRepository;

    /**
     * Checks if a step duration is anomalous
     * Auto-creates workflow/threshold if it doesn't exist
     */
    public boolean isAnomalous(Long workflowId, String stepName, long durationSeconds) {

    Workflow workflow = workflowRepository.findById(workflowId)
            .orElseGet(() -> {
                Workflow w = new Workflow();
                w.setId(workflowId);
                return workflowRepository.save(w);
            });

    MlThreshold threshold = mlThresholdRepository
            .findByWorkflowIdAndStepName(workflowId, stepName)
            .orElseGet(() -> {
                MlThreshold t = new MlThreshold();
                t.setWorkflowId(workflowId);
                t.setStepName(stepName);
                t.setSampleCount(0);
                t.setMeanDuration(0.0);
                t.setStdDev(0.0);
                return mlThresholdRepository.save(t);
            });

    boolean isAnomaly = false;

    // 🚨 WARM-UP PHASE
    if (threshold.getSampleCount() >= 5) {
        double limit = threshold.getMeanDuration() + 2 * threshold.getStdDev();
        isAnomaly = durationSeconds > limit;
    }

    MlEnabledStep step = new MlEnabledStep();
    step.setWorkflow(workflow);
    step.setStepName(stepName);
    step.setDurationInSeconds(durationSeconds);
    step.setOccurredAt(LocalDateTime.now());
    step.setAnomaly(isAnomaly);
    stepRepository.save(step);

    updateThresholdStats(workflow, stepName, threshold);

    if (isAnomaly) {
    SilentFailure failure = new SilentFailure();

    failure.setDetectedAt(LocalDateTime.now());
    failure.setFailureType(FailureType.ML_ANOMALY);
    failure.setSeverity(Severity.HIGH);
    failure.setWorkflow(workflow);
    failure.setStepName(stepName);

    failure.setMessage(
        "ML detected abnormal delay: " + durationSeconds + " seconds"
    );

    silentFailureRepository.save(failure);
}


    return isAnomaly;


}


/**
 * Updates sample count, mean, and standard deviation for a step
 */
    private void updateThresholdStats(Workflow workflow, String stepName, MlThreshold threshold) {
        // Get all steps for this workflow and step name
        List<MlEnabledStep> steps = stepRepository.findByWorkflowAndStepName(workflow, stepName);

        int sampleCount = steps.size();
        double mean = steps.stream()
                .mapToLong(MlEnabledStep::getDurationInSeconds)
                .average()
                .orElse(0.0);

        double stdDev = Math.sqrt(
                steps.stream()
                     .mapToDouble(s -> Math.pow(s.getDurationInSeconds() - mean, 2))
                     .average()
                     .orElse(0.0)
        );

        threshold.setSampleCount(sampleCount);
        threshold.setMeanDuration(mean);
        threshold.setStdDev(stdDev);
        mlThresholdRepository.save(threshold);
    }
}
