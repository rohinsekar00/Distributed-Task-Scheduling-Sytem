package com.faang.taskscheduler.model;

public enum WorkerStatus {
    /**
     * Worker is healthy and accepting tasks
     */
    ACTIVE,
    
    /**
     * Worker is temporarily paused
     */
    PAUSED,
    
    /**
     * Worker failed health check or missed heartbeats
     */
    DEAD,
    
    /**
     * Worker is shutting down gracefully
     */
    DRAINING
}
