package com.tirtha.sfd.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tirtha.sfd.model.Workflow;

public interface WorkflowRepository extends JpaRepository<Workflow, Long> {
    
    long count();
} 
