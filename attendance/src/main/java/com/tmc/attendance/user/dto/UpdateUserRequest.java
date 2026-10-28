package com.tmc.attendance.user.dto;

import jakarta.validation.constraints.Email;

import com.tmc.attendance.user.UserRole;

public record UpdateUserRequest(
        @Email String email,
        String password,
        UserRole role,
        String name
) {

}
