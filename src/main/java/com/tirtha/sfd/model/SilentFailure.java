package com.tirtha.sfd.model;

import java.time.LocalDateTime;

import com.tirtha.sfd.service.FailureType;
import com.tirtha.sfd.service.Severity;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FailureType failureType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    // Workflow reference
    @ManyToOne
    @JoinColumn(name = "workflow_id", nullable = false)
    private Workflow workflow;
}
