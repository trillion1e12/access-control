package com.tmc.access.attendance;

import org.springframework.stereotype.Component;

/**
 * Stores the message in the outbox table; {@link OutboxRelay} sends it
 * afterwards.
 */
@Component
public class OutboxAttendanceEventPublisher implements AttendanceEventPublisher {
    private final OutboxEventRepository outboxEventRepository;

    public OutboxAttendanceEventPublisher(OutboxEventRepository outboxEventRepository) {
        this.outboxEventRepository = outboxEventRepository;
    }

    @Override
    public void publish(AttendanceMessage message) {
        outboxEventRepository.save(OutboxEvent.from(message));
    }
}
