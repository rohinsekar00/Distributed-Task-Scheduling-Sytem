package com.faang.taskscheduler.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Request DTO for creating image processing workflows
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowRequest {
    
    @NotBlank(message = "Workflow name is required")
    private String workflowName;
    
    private String description;
    
    @NotEmpty(message = "At least one image URL is required")
    private List<String> imageUrls;
    
    /**
     * Operations to perform: resize, watermark, compress, thumbnail
     */
    @NotEmpty(message = "At least one operation is required")
    private List<String> operations;
    
    /**
     * Optional parameters for operations
     */
    private Map<String, Object> parameters;
}
