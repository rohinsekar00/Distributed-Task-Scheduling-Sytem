package com.faang.taskscheduler.repository;

import com.faang.taskscheduler.model.Workflow;
import com.faang.taskscheduler.model.WorkflowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WorkflowRepository extends JpaRepository<Workflow, String> {
    
    List<Workflow> findByStatus(WorkflowStatus status);
    
    List<Workflow> findByStatusIn(List<WorkflowStatus> statuses);
    
    @Query("SELECT w FROM Workflow w WHERE w.createdAt >= :since ORDER BY w.createdAt DESC")
    List<Workflow> findRecentWorkflows(@Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(w) FROM Workflow w WHERE w.status = :status")
    long countByStatus(@Param("status") WorkflowStatus status);
    
    @Query("SELECT w FROM Workflow w WHERE w.status IN :activeStatuses " +
           "ORDER BY w.createdAt DESC")
    List<Workflow> findActiveWorkflows(@Param("activeStatuses") List<WorkflowStatus> activeStatuses);
}
