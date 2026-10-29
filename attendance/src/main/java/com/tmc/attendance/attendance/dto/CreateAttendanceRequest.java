package com.tmc.attendance.attendance.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

import com.tmc.attendance.attendance.AttendanceType;

public record CreateAttendanceRequest(
        @NotNull UUID userId,
        @NotNull AttendanceType event
) {

}
