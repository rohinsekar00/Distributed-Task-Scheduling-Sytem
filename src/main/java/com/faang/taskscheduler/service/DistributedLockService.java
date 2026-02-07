package com.faang.taskscheduler.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Distributed locking service using Redis (Redisson).
 * 
 * FAANG Interview Points:
 * - Prevents duplicate task execution across workers
 * - Uses try-with-resources for automatic lock release
 * - Handles lock timeout and failure scenarios
 * - Thread-safe operations in distributed environment
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DistributedLockService {
    
    private final RedissonClient redissonClient;
    
    @Value("${task-scheduler.lock.wait-time-ms:5000}")
    private long waitTime;
    
    @Value("${task-scheduler.lock.lease-time-ms:30000}")
    private long leaseTime;
    
    /**
     * Execute action with distributed lock.
     * 
     * @param lockKey Unique lock identifier
     * @param action Action to execute while holding lock
     * @return Result of action
     * @throws RuntimeException if lock cannot be acquired
     */
    public <T> T executeWithLock(String lockKey, Supplier<T> action) {
        RLock lock = redissonClient.getLock(lockKey);
        
        try {
            boolean acquired = lock.tryLock(waitTime, leaseTime, TimeUnit.MILLISECONDS);
            
            if (!acquired) {
                log.warn("Failed to acquire lock: {}", lockKey);
                throw new RuntimeException("Could not acquire lock: " + lockKey);
            }
            
            log.debug("Lock acquired: {}", lockKey);
            
            try {
                return action.get();
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                    log.debug("Lock released: {}", lockKey);
                }
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Lock acquisition interrupted: " + lockKey, e);
        }
    }
    
    /**
     * Execute action with lock, return void
     */
    public void executeWithLock(String lockKey, Runnable action) {
        executeWithLock(lockKey, () -> {
            action.run();
            return null;
        });
    }
    
    /**
     * Try to acquire lock without blocking
     */
    public boolean tryLock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            return lock.tryLock(0, leaseTime, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    /**
     * Release lock manually
     */
    public void unlock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
            log.debug("Manual lock release: {}", lockKey);
        }
    }
    
    /**
     * Check if lock is currently held
     */
    public boolean isLocked(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        return lock.isLocked();
    }
}
