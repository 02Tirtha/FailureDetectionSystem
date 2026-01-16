package com.tirtha.sfd.repository;
import com.tirtha.sfd.model.MlThreshold;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MlThresholdRepository extends JpaRepository<MlThreshold, Long> {

    Optional<MlThreshold> findByWorkflowIdAndStepName(
            Long workflowId,
            String stepName
    );
}
