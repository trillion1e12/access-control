package com.tmc.attendance.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.tmc.attendance.user.dto.CreateUserRequest;
import com.tmc.attendance.user.dto.UpdateUserRequest;
import com.tmc.attendance.user.dto.UserResponse;

@Component
public class UserMapper {
    private final PasswordEncoder passwordEncoder;

    public UserMapper(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public AppUser toEntity(CreateUserRequest request) {
        AppUser appUser = new AppUser();
        appUser.setEmail(request.email());
        appUser.setPasswordHash(passwordEncoder.encode(request.password()));
        appUser.setRole(request.role());
        appUser.setName(request.name());
        appUser.setActive(true);
        return appUser;
    }

    public AppUser toEntity(AppUser existingUser, UpdateUserRequest request) {
        AppUser appUser = new AppUser();
        appUser.setId(existingUser.getId());
        appUser.setEmail(request.email() != null ? request.email() : existingUser.getEmail());
        appUser.setPasswordHash(request.password() != null
                ? passwordEncoder.encode(request.password())
                : existingUser.getPasswordHash());
        appUser.setRole(request.role() != null ? request.role() : existingUser.getRole());
        appUser.setName(request.name() != null ? request.name() : existingUser.getName());
        appUser.setActive(existingUser.isActive());
        return appUser;
    }

    public UserResponse toResponse(AppUser appUser) {
        return new UserResponse(
                appUser.getId(),
                appUser.getEmail(),
                appUser.getRole(),
                appUser.getName(),
                appUser.isActive()
        );
    }
}
