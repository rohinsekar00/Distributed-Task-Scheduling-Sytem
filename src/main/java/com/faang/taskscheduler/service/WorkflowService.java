package com.faang.taskscheduler.service;

import com.faang.taskscheduler.dto.*;
import com.faang.taskscheduler.model.*;
import com.faang.taskscheduler.repository.TaskRepository;
import com.faang.taskscheduler.repository.WorkflowRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Workflow management service.
 * Creates DAG of tasks and manages workflow lifecycle.
 * 
 * FAANG Interview Points:
 * - DAG creation and validation
 * - Task dependency resolution
 * - Workflow orchestration
 * - Transaction management
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class WorkflowService {
    
    private final WorkflowRepository workflowRepository;
    private final TaskRepository taskRepository;
    private final TaskQueueProducer taskQueueProducer;
    private final ObjectMapper objectMapper;
    
    /**
     * Create a new workflow from request
     */
    @Transactional
    public WorkflowResponse createWorkflow(WorkflowRequest request) {
        log.info("Creating workflow: {}", request.getWorkflowName());
        
        // Validate request
        validateWorkflowRequest(request);
        
        // Create workflow entity
        Workflow workflow = Workflow.builder()
            .workflowName(request.getWorkflowName())
            .description(request.getDescription())
            .status(WorkflowStatus.PENDING)
            .inputParameters(toJson(request))
            .totalTasks(0)
            .completedTasks(0)
            .failedTasks(0)
            .build();
        
        workflow = workflowRepository.save(workflow);
        
        // Create tasks from workflow
        List<Task> tasks = createTasksFromWorkflow(workflow, request);
        
        // Update workflow with task count
        workflow.setTotalTasks(tasks.size());
        workflow.setStatus(WorkflowStatus.RUNNING);
        workflow.setStartedAt(LocalDateTime.now());
        workflowRepository.save(workflow);
        
        // Queue initial tasks (tasks with no dependencies)
        queueInitialTasks(tasks);
        
        log.info("Workflow created: workflowId={}, totalTasks={}", 
                 workflow.getId(), tasks.size());
        
        return buildWorkflowResponse(workflow, tasks);
    }
    
    /**
     * Get workflow status
     */
    @Transactional(readOnly = true)
    public WorkflowResponse getWorkflowStatus(String workflowId) {
        Workflow workflow = workflowRepository.findById(workflowId)
            .orElseThrow(() -> new RuntimeException("Workflow not found: " + workflowId));
        
        List<Task> tasks = taskRepository.findByWorkflowId(workflowId);
        
        return buildWorkflowResponse(workflow, tasks);
    }
    
    /**
     * Create task DAG for image processing workflow
     */
    private List<Task> createTasksFromWorkflow(Workflow workflow, WorkflowRequest request) {
        List<Task> allTasks = new ArrayList<>();
        
        // Process each image URL
        for (int i = 0; i < request.getImageUrls().size(); i++) {
            String imageUrl = request.getImageUrls().get(i);
            String imageId = "img-" + i;
            
            // Create task chain for this image
            List<Task> imageTasks = createImageProcessingChain(
                workflow.getId(), 
                imageUrl, 
                imageId, 
                request.getOperations(),
                request.getParameters()
            );
            
            allTasks.addAll(imageTasks);
        }
        
        // Save all tasks
        return taskRepository.saveAll(allTasks);
    }
    
    /**
     * Create task chain for single image processing
     * 
     * DAG Structure:
     * Download → Validate → Resize → Watermark → Compress → Upload
     *                         ↓
     *                    Thumbnail
     */
    private List<Task> createImageProcessingChain(
            String workflowId, 
            String imageUrl, 
            String imageId,
            List<String> operations,
            Map<String, Object> params) {
        
        List<Task> tasks = new ArrayList<>();
        
        // Task 1: Download Image
        Task downloadTask = createTask(
            workflowId,
            "IMAGE_DOWNLOAD",
            imageId + "-download",
            Map.of("imageUrl", imageUrl),
            TaskPriority.HIGH
        );
        tasks.add(downloadTask);
        
        // Task 2: Validate Image
        if (operations.contains("validate")) {
            Task validateTask = createTask(
                workflowId,
                "IMAGE_VALIDATE",
                imageId + "-validate",
                Map.of("dependsOn", downloadTask.getId()),
                TaskPriority.HIGH
            );
            tasks.add(validateTask);
        }
        
        // Task 3: Resize Image
        if (operations.contains("resize")) {
            Map<String, Object> resizeParams = new HashMap<>();
            resizeParams.put("dependsOn", downloadTask.getId());
            resizeParams.put("width", params.getOrDefault("width", 800));
            resizeParams.put("height", params.getOrDefault("height", 600));
            
            Task resizeTask = createTask(
                workflowId,
                "IMAGE_RESIZE",
                imageId + "-resize",
                resizeParams,
                TaskPriority.MEDIUM
            );
            tasks.add(resizeTask);
        }
        
        // Task 4: Create Thumbnail (parallel with watermark)
        if (operations.contains("thumbnail")) {
            Map<String, Object> thumbParams = new HashMap<>();
            thumbParams.put("dependsOn", downloadTask.getId());
            thumbParams.put("width", 150);
            thumbParams.put("height", 150);
            
            Task thumbnailTask = createTask(
                workflowId,
                "IMAGE_THUMBNAIL",
                imageId + "-thumbnail",
                thumbParams,
                TaskPriority.LOW
            );
            tasks.add(thumbnailTask);
        }
        
        // Task 5: Apply Watermark
        if (operations.contains("watermark")) {
            Task watermarkTask = createTask(
                workflowId,
                "IMAGE_WATERMARK",
                imageId + "-watermark",
                Map.of(
                    "dependsOn", imageId + "-resize",
                    "text", params.getOrDefault("watermarkText", "Sample")
                ),
                TaskPriority.MEDIUM
            );
            tasks.add(watermarkTask);
        }
        
        // Task 6: Compress Image
        if (operations.contains("compress")) {
            Task compressTask = createTask(
                workflowId,
                "IMAGE_COMPRESS",
                imageId + "-compress",
                Map.of(
                    "dependsOn", imageId + "-watermark",
                    "quality", params.getOrDefault("quality", 85)
                ),
                TaskPriority.MEDIUM
            );
            tasks.add(compressTask);
        }
        
        return tasks;
    }
    
    /**
     * Create individual task
     */
    private Task createTask(
            String workflowId,
            String taskType,
            String taskName,
            Map<String, Object> parameters,
            TaskPriority priority) {
        
        return Task.builder()
            .workflowId(workflowId)
            .taskType(taskType)
            .taskName(taskName)
            .status(TaskStatus.PENDING)
            .priority(priority)
            .inputParameters(toJson(parameters))
            .retryCount(0)
            .maxRetries(3)
            .scheduledAt(LocalDateTime.now())
            .build();
    }
    
    /**
     * Queue tasks that have no dependencies (can start immediately)
     */
    private void queueInitialTasks(List<Task> tasks) {
        tasks.stream()
            .filter(task -> {
                // Tasks that can start immediately (e.g., DOWNLOAD tasks)
                return task.getTaskType().equals("IMAGE_DOWNLOAD");
            })
            .forEach(task -> {
                task.setStatus(TaskStatus.QUEUED);
                taskRepository.save(task);
                
                TaskMessage message = TaskMessage.builder()
                    .taskId(task.getId())
                    .workflowId(task.getWorkflowId())
                    .taskType(task.getTaskType())
                    .taskName(task.getTaskName())
                    .priority(task.getPriority())
                    .parameters(fromJson(task.getInputParameters()))
                    .retryCount(task.getRetryCount())
                    .scheduledAt(task.getScheduledAt())
                    .build();
                
                taskQueueProducer.sendTask(message);
                
                log.info("Queued initial task: taskId={}, type={}", 
                         task.getId(), task.getTaskType());
            });
    }
    
    /**
     * Update workflow progress
     */
    @Transactional
    public void updateWorkflowProgress(String workflowId) {
        Workflow workflow = workflowRepository.findById(workflowId)
            .orElseThrow(() -> new RuntimeException("Workflow not found"));
        
        long completedCount = taskRepository.countByWorkflowIdAndStatus(
            workflowId, TaskStatus.COMPLETED);
        long failedCount = taskRepository.countByWorkflowIdAndStatus(
            workflowId, TaskStatus.FAILED);
        
        workflow.setCompletedTasks((int) completedCount);
        workflow.setFailedTasks((int) failedCount);
        
        // Check if workflow is complete
        if (completedCount + failedCount >= workflow.getTotalTasks()) {
            if (failedCount > 0) {
                workflow.setStatus(WorkflowStatus.PARTIALLY_COMPLETED);
            } else {
                workflow.setStatus(WorkflowStatus.COMPLETED);
            }
            workflow.setCompletedAt(LocalDateTime.now());
            
            if (workflow.getStartedAt() != null) {
                long duration = java.time.Duration.between(
                    workflow.getStartedAt(), 
                    workflow.getCompletedAt()
                ).toMillis();
                workflow.setTotalExecutionTimeMs(duration);
            }
        }
        
        workflowRepository.save(workflow);
    }
    
    /**
     * Build workflow response DTO
     */
    private WorkflowResponse buildWorkflowResponse(Workflow workflow, List<Task> tasks) {
        List<TaskStatusDTO> taskDTOs = tasks.stream()
            .map(task -> TaskStatusDTO.builder()
                .taskId(task.getId())
                .taskName(task.getTaskName())
                .taskType(task.getTaskType())
                .status(task.getStatus())
                .assignedWorkerId(task.getAssignedWorkerId())
                .retryCount(task.getRetryCount())
                .errorMessage(task.getLastErrorMessage())
                .startedAt(task.getStartedAt())
                .completedAt(task.getCompletedAt())
                .executionDurationMs(task.getExecutionDurationMs())
                .build())
            .collect(Collectors.toList());
        
        return WorkflowResponse.builder()
            .workflowId(workflow.getId())
            .workflowName(workflow.getWorkflowName())
            .status(workflow.getStatus())
            .totalTasks(workflow.getTotalTasks())
            .completedTasks(workflow.getCompletedTasks())
            .failedTasks(workflow.getFailedTasks())
            .progressPercentage(workflow.getProgressPercentage())
            .createdAt(workflow.getCreatedAt())
            .startedAt(workflow.getStartedAt())
            .completedAt(workflow.getCompletedAt())
            .executionTimeMs(workflow.getTotalExecutionTimeMs())
            .tasks(taskDTOs)
            .build();
    }
    
    /**
     * Validate workflow request
     */
    private void validateWorkflowRequest(WorkflowRequest request) {
        if (request.getImageUrls() == null || request.getImageUrls().isEmpty()) {
            throw new IllegalArgumentException("At least one image URL is required");
        }
        
        if (request.getOperations() == null || request.getOperations().isEmpty()) {
            throw new IllegalArgumentException("At least one operation is required");
        }
        
        // Validate image URLs
        request.getImageUrls().forEach(url -> {
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                throw new IllegalArgumentException("Invalid image URL: " + url);
            }
        });
    }
    
    /**
     * Helper: Convert object to JSON string
     */
    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize to JSON", e);
        }
    }
    
    /**
     * Helper: Convert JSON string to Map
     */
    private Map<String, Object> fromJson(String json) {
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            return new HashMap<>();
        }
    }
}
