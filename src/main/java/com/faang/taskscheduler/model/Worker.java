package com.faang.taskscheduler.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Worker node entity for distributed execution and health monitoring.
 * 
 * FAANG Interview Points:
 * - Heartbeat mechanism for failure detection
 * - Load tracking for intelligent task assignment
 * - Worker capacity management
 */
@Entity
@Table(name = "workers", indexes = {
    @Index(name = "idx_worker_status", columnList = "status"),
    @Index(name = "idx_worker_heartbeat", columnList = "lastHeartbeatAt")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Worker {
    
    @Id
    private String id;  // Worker ID (hostname or UUID)
    
    @Column(nullable = false)
    private String hostname;
    
    private Integer port;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkerStatus status;
    
    // Capacity management
    @Builder.Default
    private Integer maxConcurrentTasks = 10;
    
    @Builder.Default
    private Integer currentTaskCount = 0;
    
    // Health monitoring
    @Column(nullable = false)
    private LocalDateTime lastHeartbeatAt;
    
    @Builder.Default
    private Integer consecutiveFailures = 0;
    
    // Performance metrics
    @Builder.Default
    private Long totalTasksProcessed = 0L;
    
    @Builder.Default
    private Long totalTasksSucceeded = 0L;
    
    @Builder.Default
    private Long totalTasksFailed = 0L;
    
    private Double averageTaskDurationMs;
    
    // Audit
    @Column(nullable = false, updatable = false)
    private LocalDateTime registeredAt;
    
    private LocalDateTime updatedAt;
    
    @Version
    private Long version;
    
    @PrePersist
    protected void onCreate() {
        registeredAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        lastHeartbeatAt = LocalDateTime.now();
        if (status == null) {
            status = WorkerStatus.ACTIVE;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public boolean hasCapacity() {
        return currentTaskCount < maxConcurrentTasks && status == WorkerStatus.ACTIVE;
    }
    
    public int getAvailableCapacity() {
        return Math.max(0, maxConcurrentTasks - currentTaskCount);
    }
    
    public double getSuccessRate() {
        if (totalTasksProcessed == 0) return 100.0;
        return (totalTasksSucceeded * 100.0) / totalTasksProcessed;
    }
    
    public void incrementTaskCount() {
        this.currentTaskCount++;
    }
    
    public void decrementTaskCount() {
        this.currentTaskCount = Math.max(0, this.currentTaskCount - 1);
    }
    
    public void recordTaskCompletion(boolean success, long durationMs) {
        this.totalTasksProcessed++;
        if (success) {
            this.totalTasksSucceeded++;
            this.consecutiveFailures = 0;
        } else {
            this.totalTasksFailed++;
            this.consecutiveFailures++;
        }
        
        // Update average duration
        if (averageTaskDurationMs == null) {
            averageTaskDurationMs = (double) durationMs;
        } else {
            averageTaskDurationMs = (averageTaskDurationMs * 0.9) + (durationMs * 0.1);
        }
    }
}
