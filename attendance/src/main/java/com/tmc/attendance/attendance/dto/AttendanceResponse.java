package com.tmc.attendance.attendance.dto;

import java.time.Instant;
import java.util.UUID;

import com.tmc.attendance.attendance.AttendanceType;

public record AttendanceResponse(
        UUID id,
        UUID userId,
        AttendanceType event,
        Instant createdAt
) {

}
