package com.tmc.access.outbox;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tmc.access.attendance.AttendanceMessage;
import com.tmc.access.attendance.AttendanceType;

/**
 * An attendance message waiting to be sent, stored so that it survives a broker
 * or service outage.
 */
@Entity
@Table(name = "attendance_outbox")
public class OutboxEvent {
    @Id
    private UUID id;

    private UUID userId;

    @Enumerated(EnumType.STRING)
    private AttendanceType event;

    private Instant createdAt;

    /** Null until the broker has confirmed the message. */
    private Instant publishedAt;

    public static OutboxEvent from(AttendanceMessage message) {
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setId(message.eventId());
        outboxEvent.setUserId(message.userId());
        outboxEvent.setEvent(message.event());
        outboxEvent.setCreatedAt(message.createdAt());
        return outboxEvent;
    }

    public AttendanceMessage toMessage() {
        return new AttendanceMessage(id, userId, event, createdAt);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public AttendanceType getEvent() {
        return event;
    }

    public void setEvent(AttendanceType event) {
        this.event = event;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

}
