package com.faang.taskscheduler.service;

import com.faang.taskscheduler.dto.TaskMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka producer for task distribution.
 * 
 * FAANG Interview Points:
 * - Asynchronous message publishing for high throughput
 * - Partition key strategy for load balancing
 * - Error handling and retry logic
 * - Dead letter queue for failed messages
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TaskQueueProducer {
    
    private final KafkaTemplate<String, TaskMessage> kafkaTemplate;
    
    @Value("${task-scheduler.queue.topic}")
    private String taskQueueTopic;
    
    @Value("${task-scheduler.queue.dead-letter-topic}")
    private String deadLetterTopic;
    
    /**
     * Send task to queue.
     * Uses task ID as partition key for load balancing.
     */
    public CompletableFuture<SendResult<String, TaskMessage>> sendTask(TaskMessage taskMessage) {
        log.debug("Sending task to queue: taskId={}, type={}", 
                  taskMessage.getTaskId(), taskMessage.getTaskType());
        
        CompletableFuture<SendResult<String, TaskMessage>> future = 
            kafkaTemplate.send(taskQueueTopic, taskMessage.getTaskId(), taskMessage);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Task queued successfully: taskId={}, partition={}, offset={}", 
                         taskMessage.getTaskId(),
                         result.getRecordMetadata().partition(),
                         result.getRecordMetadata().offset());
            } else {
                log.error("Failed to queue task: taskId={}", taskMessage.getTaskId(), ex);
                // Send to dead letter queue
                sendToDeadLetterQueue(taskMessage);
            }
        });
        
        return future;
    }
    
    /**
     * Send failed message to dead letter queue
     */
    public void sendToDeadLetterQueue(TaskMessage taskMessage) {
        log.warn("Sending task to dead letter queue: taskId={}", taskMessage.getTaskId());
        
        kafkaTemplate.send(deadLetterTopic, taskMessage.getTaskId(), taskMessage)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to send to DLQ: taskId={}", taskMessage.getTaskId(), ex);
                }
            });
    }
    
    /**
     * Send task with custom partition key (for colocation)
     */
    public CompletableFuture<SendResult<String, TaskMessage>> sendTaskToPartition(
            TaskMessage taskMessage, String partitionKey) {
        
        log.debug("Sending task with partition key: taskId={}, key={}", 
                  taskMessage.getTaskId(), partitionKey);
        
        return kafkaTemplate.send(taskQueueTopic, partitionKey, taskMessage);
    }
}
