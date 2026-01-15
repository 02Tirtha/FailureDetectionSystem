package com.tirtha.sfd.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "workflow_step")
public class WorkflowStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Step name like EMAIL_SENT
    @Column(nullable = false)
    private String stepName;

    // Order of step (1, 2, 3...)
    private int stepOrder;

    // Expected time in seconds (baseline)
    private int expectedTimeSeconds;

    // Many steps belong to one workflow
    @ManyToOne
    @JoinColumn(name = "workflow_id", nullable = false)
    private Workflow workflow;
}
