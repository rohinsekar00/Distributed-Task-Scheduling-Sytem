package com.faang.taskscheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for Distributed Task Scheduler.
 * 
 * FAANG Interview Talking Points:
 * - Built with Spring Boot for production-grade features
 * - Async processing for non-blocking operations
 * - Scheduled tasks for health checks and monitoring
 * - Microservice architecture ready
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class DistributedTaskSchedulerApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(DistributedTaskSchedulerApplication.class, args);
    }
}
