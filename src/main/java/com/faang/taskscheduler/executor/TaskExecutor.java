package com.faang.taskscheduler.executor;

import java.util.Map;

/**
 * Interface for task execution strategies.
 * Allows different task types to be plugged in.
 * 
 * FAANG Interview Points:
 * - Strategy pattern for extensibility
 * - Generic task execution interface
 * - Type-safe parameter handling
 */
public interface TaskExecutor {
    
    /**
     * Execute the task with given parameters
     * 
     * @param parameters Task input parameters
     * @return Task execution result
     * @throws Exception if task execution fails
     */
    Map<String, Object> execute(Map<String, Object> parameters) throws Exception;
    
    /**
     * Get the task type this executor handles
     */
    String getTaskType();
    
    /**
     * Validate input parameters before execution
     */
    default void validateParameters(Map<String, Object> parameters) {
        // Override in implementations
    }
    
    /**
     * Estimate execution time in milliseconds
     */
    default long estimateExecutionTime(Map<String, Object> parameters) {
        return 10000; // Default 10 seconds
    }
}
