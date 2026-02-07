package com.faang.taskscheduler.controller;

import com.faang.taskscheduler.dto.WorkflowRequest;
import com.faang.taskscheduler.dto.WorkflowResponse;
import com.faang.taskscheduler.service.WorkflowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST API for workflow management.
 * 
 * FAANG Interview Points:
 * - RESTful API design
 * - Proper HTTP status codes
 * - Input validation
 * - Error handling
 */
@RestController
@RequestMapping("/api/workflows")
@RequiredArgsConstructor
@Slf4j
public class WorkflowController {
    
    private final WorkflowService workflowService;
    
    /**
     * Create new workflow
     * 
     * POST /api/workflows
     * Body: WorkflowRequest
     * Returns: WorkflowResponse with workflowId
     */
    @PostMapping
    public ResponseEntity<WorkflowResponse> createWorkflow(
            @Valid @RequestBody WorkflowRequest request) {
        
        log.info("POST /api/workflows - Creating workflow: {}", request.getWorkflowName());
        
        try {
            WorkflowResponse response = workflowService.createWorkflow(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid workflow request", e);
            throw new RuntimeException("Invalid request: " + e.getMessage());
        }
    }
    
    /**
     * Get workflow status
     * 
     * GET /api/workflows/{workflowId}/status
     * Returns: WorkflowResponse with current status and task details
     */
    @GetMapping("/{workflowId}/status")
    public ResponseEntity<WorkflowResponse> getWorkflowStatus(
            @PathVariable String workflowId) {
        
        log.info("GET /api/workflows/{}/status", workflowId);
        
        WorkflowResponse response = workflowService.getWorkflowStatus(workflowId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get workflow details (alias for status)
     */
    @GetMapping("/{workflowId}")
    public ResponseEntity<WorkflowResponse> getWorkflow(
            @PathVariable String workflowId) {
        
        return getWorkflowStatus(workflowId);
    }
    
    /**
     * Exception handler
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleException(RuntimeException e) {
        log.error("Error processing request", e);
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            e.getMessage(),
            System.currentTimeMillis()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Error response DTO
     */
    private record ErrorResponse(int status, String message, long timestamp) {}
}
