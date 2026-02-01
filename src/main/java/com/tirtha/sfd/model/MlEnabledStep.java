package com.tirtha.sfd.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class MlEnabledStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String stepName;

    private long durationInSeconds;

    private LocalDateTime occurredAt;

    private boolean anomaly;

    @ManyToOne
    @JoinColumn(name = "workflow_id")
    private Workflow workflow;

    
}
