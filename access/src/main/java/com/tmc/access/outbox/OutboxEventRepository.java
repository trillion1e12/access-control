package com.tmc.access.outbox;

import java.util.List;
import java.util.UUID;

import org.springframework.data.repository.Repository;

public interface OutboxEventRepository extends Repository<OutboxEvent, UUID> {
    OutboxEvent save(OutboxEvent outboxEvent);

    List<OutboxEvent> findTop50ByPublishedAtIsNullOrderByCreatedAtAsc();
}
