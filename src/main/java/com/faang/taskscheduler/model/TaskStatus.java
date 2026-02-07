package com.faang.taskscheduler.model;

/**
 * Task execution states with clear state transitions.
 * 
 * State Machine:
 * PENDING → QUEUED → ASSIGNED → RUNNING → COMPLETED/FAILED
 *                                    ↓
 *                                 RETRYING
 */
public enum TaskStatus {
    /**
     * Task created but not yet queued
     */
    PENDING,
    
    /**
     * Task sent to Kafka queue, waiting for worker
     */
    QUEUED,
    
    /**
     * Task assigned to a worker but not started
     */
    ASSIGNED,
    
    /**
     * Task currently being executed by worker
     */
    RUNNING,
    
    /**
     * Task execution completed successfully
     */
    COMPLETED,
    
    /**
     * Task failed and scheduled for retry
     */
    RETRYING,
    
    /**
     * Task failed after all retry attempts
     */
    FAILED,
    
    /**
     * Task cancelled by user or system
     */
    CANCELLED;
    
    public boolean isActive() {
        return this == RUNNING || this == ASSIGNED || this == QUEUED;
    }
    
    public boolean isWaiting() {
        return this == PENDING || this == QUEUED || this == ASSIGNED;
    }
}
