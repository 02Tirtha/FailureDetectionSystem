package com.tirtha.sfd.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ml_thresholds")
public class MlThreshold {
    @Id @GeneratedValue
    private Long id;

    private Long workflowId;
    private String stepName;
    private double meanDuration;
    private double stdDev;
    private int sampleCount;
}
