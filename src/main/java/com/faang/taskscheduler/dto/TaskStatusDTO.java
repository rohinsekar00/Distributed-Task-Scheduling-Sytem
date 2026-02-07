package com.faang.taskscheduler.dto;

import com.faang.taskscheduler.model.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskStatusDTO {
    
    private String taskId;
    
    private String taskName;
    
    private String taskType;
    
    private TaskStatus status;
    
    private String assignedWorkerId;
    
    private Integer retryCount;
    
    private String errorMessage;
    
    private LocalDateTime startedAt;
    
    private LocalDateTime completedAt;
    
    private Long executionDurationMs;
}
