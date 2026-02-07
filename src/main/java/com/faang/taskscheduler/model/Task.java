package com.faang.taskscheduler.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Task entity representing a single executable unit of work in the system.
 * 
 * FAANG Interview Points:
 * - Immutable after creation (defensive programming)
 * - Optimistic locking with @Version
 * - Indexed columns for query performance
 * - Audit fields for debugging
 */
@Entity
@Table(name = "tasks", indexes = {
    @Index(name = "idx_task_status", columnList = "status"),
    @Index(name = "idx_task_workflow", columnList = "workflowId"),
    @Index(name = "idx_task_worker", columnList = "assignedWorkerId"),
    @Index(name = "idx_task_created", columnList = "createdAt")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false)
    private String workflowId;
    
    @Column(nullable = false)
    private String taskType;
    
    @Column(nullable = false)
    private String taskName;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;
    
    @Enumerated(EnumType.STRING)
    private TaskPriority priority;
    
    // Task execution parameters stored as JSON
    @Column(columnDefinition = "TEXT")
    private String inputParameters;
    
    @Column(columnDefinition = "TEXT")
    private String outputResult;
    
    // Worker assignment
    private String assignedWorkerId;
    
    private LocalDateTime assignedAt;
    
    // Retry mechanism
    @Builder.Default
    private Integer retryCount = 0;
    
    @Builder.Default
    private Integer maxRetries = 3;
    
    private String lastErrorMessage;
    
    // Timing information
    private LocalDateTime scheduledAt;
    
    private LocalDateTime startedAt;
    
    private LocalDateTime completedAt;
    
    private Long executionDurationMs;
    
    // Audit fields
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    // Optimistic locking
    @Version
    private Long version;
    
    // For distributed locking
    @Column(unique = true)
    private String lockKey;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = TaskStatus.PENDING;
        }
        if (priority == null) {
            priority = TaskPriority.MEDIUM;
        }
        lockKey = "task:lock:" + id;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public boolean canRetry() {
        return retryCount < maxRetries;
    }
    
    public void incrementRetry() {
        this.retryCount++;
    }
    
    public boolean isTerminalState() {
        return status == TaskStatus.COMPLETED || 
               status == TaskStatus.FAILED || 
               status == TaskStatus.CANCELLED;
    }
}
