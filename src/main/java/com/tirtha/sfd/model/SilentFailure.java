package com.tirtha.sfd.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter 
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "silent_failures" , indexes = {
        @Index(name = "idx_workflow_step", columnList = "workflow_id, stepName")
    })
public class SilentFailure {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long id;

     // Which step failed
    @Column(nullable = false)
    private String stepName;

    // Explanation
    private String message;

    // When failure was detected
    @Column(nullable = false)
    private LocalDateTime detectedAt;

    private String failureType; // e.g., "MISSING_STEP", "DELAYED_STEP"

    // Workflow reference
    @ManyToOne
    @JoinColumn(name = "workflow_id", nullable = false)
    private Workflow workflow;
}
