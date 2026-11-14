package com.tmc.access.attendance;

import java.time.Instant;
import java.util.UUID;

/**
 * One entry or exit of a user, as published on the attendance topic.
 *
 * @param eventId
 *            identifies the entry or exit; publishing it again must reuse the
 *            same value
 */
public record AttendanceMessage(
        UUID eventId,
        UUID userId,
        AttendanceType event,
        Instant createdAt
) {

}
