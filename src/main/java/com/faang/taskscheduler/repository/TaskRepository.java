package com.faang.taskscheduler.repository;

import com.faang.taskscheduler.model.Task;
import com.faang.taskscheduler.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, String> {
    
    List<Task> findByWorkflowId(String workflowId);
    
    List<Task> findByStatus(TaskStatus status);
    
    List<Task> findByStatusIn(List<TaskStatus> statuses);
    
    @Query("SELECT t FROM Task t WHERE t.status = :status ORDER BY t.priority DESC, t.createdAt ASC")
    List<Task> findPendingTasksOrderedByPriority(@Param("status") TaskStatus status);
    
    @Query("SELECT t FROM Task t WHERE t.assignedWorkerId = :workerId AND t.status IN :statuses")
    List<Task> findByWorkerIdAndStatuses(
        @Param("workerId") String workerId, 
        @Param("statuses") List<TaskStatus> statuses
    );
    
    @Query("SELECT COUNT(t) FROM Task t WHERE t.status = :status")
    long countByStatus(@Param("status") TaskStatus status);
    
    @Query("SELECT COUNT(t) FROM Task t WHERE t.workflowId = :workflowId AND t.status = :status")
    long countByWorkflowIdAndStatus(
        @Param("workflowId") String workflowId, 
        @Param("status") TaskStatus status
    );
    
    /**
     * Find stale tasks (tasks that haven't been updated in a while and might need recovery)
     */
    @Query("SELECT t FROM Task t WHERE t.status IN :activeStatuses " +
           "AND t.updatedAt < :threshold")
    List<Task> findStaleTasks(
        @Param("activeStatuses") List<TaskStatus> activeStatuses,
        @Param("threshold") LocalDateTime threshold
    );
    
    /**
     * Bulk update status for tasks assigned to a dead worker
     */
    @Modifying
    @Query("UPDATE Task t SET t.status = :newStatus, t.assignedWorkerId = null, " +
           "t.updatedAt = :now WHERE t.assignedWorkerId = :workerId " +
           "AND t.status IN :activeStatuses")
    int reassignTasksFromDeadWorker(
        @Param("workerId") String workerId,
        @Param("activeStatuses") List<TaskStatus> activeStatuses,
        @Param("newStatus") TaskStatus newStatus,
        @Param("now") LocalDateTime now
    );
}
