package com.tmc.attendance.kafka;
// https://youtu.be/5XW3f_39ipY
// https://www.geeksforgeeks.org/advance-java/spring-boot-integration-with-kafka/

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import com.tmc.attendance.attendance.InvalidAttendanceMessageException;

@Configuration
public class KafkaConfig {
    private static final long RETRY_INTERVAL_MS = 1000;
    private static final long MAX_RETRIES = 3;

    @Value("${app.kafka.attendance-topic:attendance}")
    private String attendanceTopic;

    @Value("${app.kafka.attendance-dead-letter-topic:attendance.dead-letter}")
    private String deadLetterTopic;

    @Bean
    public NewTopic attendanceTopic() {
        return TopicBuilder.name(attendanceTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic attendanceDeadLetterTopic() {
        return TopicBuilder.name(deadLetterTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }

    /**
     * A message that fails is retried, then moved to the dead-letter topic, so one
     * bad message neither blocks the messages behind it nor disappears. An invalid
     * message is moved without retrying.
     */
    @Bean
    public CommonErrorHandler attendanceErrorHandler(KafkaOperations<Object, Object> kafkaOperations) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaOperations,
                (record, exception) -> new TopicPartition(deadLetterTopic, -1)
        );
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                recoverer,
                new FixedBackOff(RETRY_INTERVAL_MS, MAX_RETRIES)
        );
        errorHandler.addNotRetryableExceptions(InvalidAttendanceMessageException.class);
        return errorHandler;
    }
}
