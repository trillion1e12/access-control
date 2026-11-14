package com.tmc.access.attendance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class OutboxRelayTest {
    private static final Instant NOW = Instant.parse("2026-11-13T01:00:00Z");

    private final OutboxEventRepository outboxEventRepository = mock(OutboxEventRepository.class);
    private final List<UUID> sent = new ArrayList<>();
    private boolean brokerAvailable = true;

    private final OutboxRelay outboxRelay = new OutboxRelay(
            outboxEventRepository,
            message -> {
                if (!brokerAvailable) {
                    throw new IllegalStateException("broker down");
                }
                sent.add(message.eventId());
            },
            Clock.fixed(NOW, ZoneOffset.UTC)
    );

    private final OutboxEvent first = pending(NOW.minusSeconds(2));
    private final OutboxEvent second = pending(NOW.minusSeconds(1));

    @Test
    void sendsPendingEventsInOrderAndMarksThemPublished() {
        when(outboxEventRepository.findTop50ByPublishedAtIsNullOrderByCreatedAtAsc())
                .thenReturn(List.of(first, second));

        outboxRelay.relayPending();

        assertEquals(List.of(first.getId(), second.getId()), sent);
        assertEquals(NOW, first.getPublishedAt());
        assertEquals(NOW, second.getPublishedAt());
    }

    @Test
    void keepsEventsPendingWhenBrokerIsDown() {
        brokerAvailable = false;
        when(outboxEventRepository.findTop50ByPublishedAtIsNullOrderByCreatedAtAsc())
                .thenReturn(List.of(first, second));

        outboxRelay.relayPending();

        assertEquals(List.of(), sent);
        assertNull(first.getPublishedAt());
        assertNull(second.getPublishedAt());
    }

    private static OutboxEvent pending(Instant createdAt) {
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setId(UUID.randomUUID());
        outboxEvent.setUserId(UUID.randomUUID());
        outboxEvent.setEvent(AttendanceType.CHECK_IN);
        outboxEvent.setCreatedAt(createdAt);
        return outboxEvent;
    }
}
