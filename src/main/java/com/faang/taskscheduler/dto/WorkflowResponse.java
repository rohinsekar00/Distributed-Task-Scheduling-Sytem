package com.faang.taskscheduler.dto;

import com.faang.taskscheduler.model.WorkflowStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowResponse {
    
    private String workflowId;
    
    private String workflowName;
    
    private WorkflowStatus status;
    
    private Integer totalTasks;
    
    private Integer completedTasks;
    
    private Integer failedTasks;
    
    private Double progressPercentage;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime startedAt;
    
    private LocalDateTime completedAt;
    
    private Long executionTimeMs;
    
    private List<TaskStatusDTO> tasks;
}
