package com.faang.taskscheduler.model;

/**
 * Task priority levels for queue ordering
 */
public enum TaskPriority {
    LOW(0),
    MEDIUM(1),
    HIGH(2),
    CRITICAL(3);
    
    private final int level;
    
    TaskPriority(int level) {
        this.level = level;
    }
    
    public int getLevel() {
        return level;
    }
    
    public boolean isHigherThan(TaskPriority other) {
        return this.level > other.level;
    }
}
