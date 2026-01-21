package com.tirtha.sfd.controller;

import com.tirtha.sfd.service.MlAnomalyDetectionService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import com.tirtha.sfd.repository.MlThresholdRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/anomaly")
@RequiredArgsConstructor
public class MlAnomalyController {

    private final MlAnomalyDetectionService mlAnomalyDetectionService;
    private final MlThresholdRepository mlThresholdRepository; 

    // POST endpoint to submit a new step duration and get anomaly result
    @PostMapping("/check")
    public ResponseEntity<AnomalyResponse> checkStep(
            @RequestBody AnomalyRequest request
    ) {
        boolean isAnomaly = mlAnomalyDetectionService.isAnomalous(
                request.getWorkflowId(),
                request.getStepName(),
                request.getDurationSeconds()
        );

        AnomalyResponse response = new AnomalyResponse(
                request.getWorkflowId(),
                request.getStepName(),
                request.getDurationSeconds(),
                isAnomaly
        );

        return ResponseEntity.ok(response);
    }
    @GetMapping("/threshold/{workflowId}/{stepName}")
    public ResponseEntity<?> getThreshold(
            @PathVariable Long workflowId,
            @PathVariable String stepName
    ) {
        return mlThresholdRepository.findByWorkflowIdAndStepName(workflowId, stepName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}

// Request DTO
@Data
class AnomalyRequest {
    private Long workflowId;
    private String stepName;
    private long durationSeconds;
}

// Response DTO
@Data
@AllArgsConstructor
class AnomalyResponse {
    private Long workflowId;
    private String stepName;
    private long durationSeconds;
    private boolean anomaly;
}
