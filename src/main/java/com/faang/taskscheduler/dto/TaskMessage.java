package com.faang.taskscheduler.dto;

import com.faang.taskscheduler.model.TaskPriority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Task message sent through Kafka queue.
 * Lightweight DTO for efficient serialization.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskMessage {
    
    private String taskId;
    
    private String workflowId;
    
    private String taskType;
    
    private String taskName;
    
    private TaskPriority priority;
    
    private Map<String, Object> parameters;
    
    private Integer retryCount;
    
    private LocalDateTime scheduledAt;
    
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
