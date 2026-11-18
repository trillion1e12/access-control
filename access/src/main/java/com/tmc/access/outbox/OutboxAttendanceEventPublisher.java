package com.tmc.access.outbox;

import org.springframework.stereotype.Component;

import com.tmc.access.attendance.AttendanceEventPublisher;
import com.tmc.access.attendance.AttendanceMessage;

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
