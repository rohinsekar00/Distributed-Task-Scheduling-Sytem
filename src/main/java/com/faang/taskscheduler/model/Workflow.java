package com.faang.taskscheduler.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Workflow entity representing a DAG (Directed Acyclic Graph) of tasks.
 * 
 * FAANG Interview Points:
 * - Manages task dependencies
 * - Tracks overall workflow progress
 * - Supports parallel execution of independent tasks
 */
@Entity
@Table(name = "workflows", indexes = {
    @Index(name = "idx_workflow_status", columnList = "status"),
    @Index(name = "idx_workflow_created", columnList = "createdAt")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Workflow {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false)
    private String workflowName;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkflowStatus status;
    
    // DAG definition stored as JSON
    @Column(columnDefinition = "TEXT")
    private String dagDefinition;
    
    // Workflow input parameters
    @Column(columnDefinition = "TEXT")
    private String inputParameters;
    
    // Overall workflow result
    @Column(columnDefinition = "TEXT")
    private String outputResult;
    
    // Progress tracking
    @Builder.Default
    private Integer totalTasks = 0;
    
    @Builder.Default
    private Integer completedTasks = 0;
    
    @Builder.Default
    private Integer failedTasks = 0;
    
    // Timing
    private LocalDateTime startedAt;
    
    private LocalDateTime completedAt;
    
    private Long totalExecutionTimeMs;
    
    // Audit
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    @Version
    private Long version;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = WorkflowStatus.PENDING;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public double getProgressPercentage() {
        if (totalTasks == 0) return 0.0;
        return (completedTasks * 100.0) / totalTasks;
    }
    
    public boolean isComplete() {
        return status == WorkflowStatus.COMPLETED || 
               status == WorkflowStatus.FAILED || 
               status == WorkflowStatus.CANCELLED;
    }
}
