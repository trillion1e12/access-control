package com.tmc.attendance.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.tmc.attendance.user.UserRole;

public record CreateUserRequest(
        @Email String email,
        @NotBlank String password,
        @NotNull UserRole role,
        @NotBlank String name
) {

}
