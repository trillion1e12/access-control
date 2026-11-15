package com.tmc.access.outbox;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import com.tmc.access.attendance.AttendanceMessage;

/**
 * Sends attendance messages as JSON to a Kafka topic, keyed by user so one
 * user's events stay in order.
 */
@Component
public class KafkaAttendanceMessageSender implements AttendanceMessageSender {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topic;
    private final long ackTimeoutMs;

    public KafkaAttendanceMessageSender(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${app.kafka.attendance-topic:attendance}") String topic,
            @Value("${app.kafka.ack-timeout-ms:5000}") long ackTimeoutMs
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topic = topic;
        this.ackTimeoutMs = ackTimeoutMs;
    }

    @Override
    public void send(AttendanceMessage message) {
        String payload = objectMapper.writeValueAsString(message);
        try {
            kafkaTemplate.send(topic, message.userId().toString(), payload).get(ackTimeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for the broker", e);
        } catch (ExecutionException | TimeoutException e) {
            throw new IllegalStateException("Broker did not confirm attendance event " + message.eventId(), e);
        }
    }
}
