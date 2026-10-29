package com.tmc.access.attendance;

import java.time.Instant;
import java.util.UUID;

public record AttendanceMessage(
        UUID userId,
        AttendanceType event,
        Instant createdAt
) {

}
