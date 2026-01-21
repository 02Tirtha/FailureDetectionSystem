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

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStepName() { return stepName; }
    public void setStepName(String stepName) { this.stepName = stepName; }

    public long getDurationInSeconds() { return durationInSeconds; }
    public void setDurationInSeconds(long durationInSeconds) { this.durationInSeconds = durationInSeconds; }

    public LocalDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; }

    public Workflow getWorkflow() { return workflow; }
    public void setWorkflow(Workflow workflow) { this.workflow = workflow; }
}
