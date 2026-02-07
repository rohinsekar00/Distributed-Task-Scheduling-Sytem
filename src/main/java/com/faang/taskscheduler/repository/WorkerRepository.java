package com.faang.taskscheduler.repository;

import com.faang.taskscheduler.model.Worker;
import com.faang.taskscheduler.model.WorkerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkerRepository extends JpaRepository<Worker, String> {
    
    List<Worker> findByStatus(WorkerStatus status);
    
    @Query("SELECT w FROM Worker w WHERE w.status = :status " +
           "AND w.currentTaskCount < w.maxConcurrentTasks " +
           "ORDER BY w.currentTaskCount ASC, w.averageTaskDurationMs ASC")
    List<Worker> findAvailableWorkers(@Param("status") WorkerStatus status);
    
    @Query("SELECT w FROM Worker w WHERE w.status = :status " +
           "AND w.lastHeartbeatAt < :threshold")
    List<Worker> findDeadWorkers(
        @Param("status") WorkerStatus status,
        @Param("threshold") LocalDateTime threshold
    );
    
    @Query("SELECT COUNT(w) FROM Worker w WHERE w.status = :status")
    long countByStatus(@Param("status") WorkerStatus status);
    
    @Modifying
    @Query("UPDATE Worker w SET w.status = :newStatus WHERE w.id = :workerId")
    int updateWorkerStatus(
        @Param("workerId") String workerId,
        @Param("newStatus") WorkerStatus newStatus
    );
}
