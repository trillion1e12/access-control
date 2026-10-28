package com.tmc.attendance.user.dto;

import java.util.UUID;

import com.tmc.attendance.user.UserRole;

public record UserResponse(
        UUID id,
        String email,
        UserRole role,
        String name,
        boolean active
) {

}
