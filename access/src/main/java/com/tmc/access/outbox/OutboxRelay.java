package com.tmc.access.outbox;

import java.time.Clock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Sends stored attendance messages in the order they were created. A message
 * stays in the outbox until its delivery is confirmed, so it can be sent more
 * than once; the event id lets the receiver ignore the repeats.
 */
@Component
public class OutboxRelay {
    private final OutboxEventRepository outboxEventRepository;
    private final AttendanceMessageSender attendanceMessageSender;
    private final Clock clock;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public OutboxRelay(
            OutboxEventRepository outboxEventRepository,
            AttendanceMessageSender attendanceMessageSender,
            Clock clock
    ) {
        this.outboxEventRepository = outboxEventRepository;
        this.attendanceMessageSender = attendanceMessageSender;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${app.outbox.poll-interval-ms:1000}")
    public void relayPending() {
        for (OutboxEvent outboxEvent : outboxEventRepository.findTop50ByPublishedAtIsNullOrderByCreatedAtAsc()) {
            try {
                attendanceMessageSender.send(outboxEvent.toMessage());
            } catch (RuntimeException e) {
                // Stop here so that later events are not sent ahead of this one; the next run
                // retries.
                logger.warn("Attendance event {} not sent yet, will retry: {}", outboxEvent.getId(), e.getMessage());
                return;
            }
            logger.info("Attendance event {} sent", outboxEvent.getId());
            outboxEvent.setPublishedAt(clock.instant());
            outboxEventRepository.save(outboxEvent);
        }
    }
}
