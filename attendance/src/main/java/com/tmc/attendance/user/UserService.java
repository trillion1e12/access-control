package com.tmc.attendance.user;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.tmc.attendance.user.dto.CreateUserRequest;
import com.tmc.attendance.user.dto.UpdateUserRequest;
import com.tmc.attendance.user.dto.UserResponse;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public UserResponse createUser(CreateUserRequest request) {
        AppUser appUser = userMapper.toEntity(request);
        AppUser createdUser = userRepository.save(appUser);
        return userMapper.toResponse(createdUser);
    }

    public UserResponse getUser(UUID id) {
        AppUser appUser = userRepository.findById(id).orElseThrow();
        return userMapper.toResponse(appUser);
    }

    public List<UserResponse> getUsers() {
        List<AppUser> appUsers = userRepository.findAll();
        return appUsers.stream().map(userMapper::toResponse).toList();
    }

    public UserResponse updateUser(UUID id, UpdateUserRequest request) {
        AppUser existingUser = userRepository.findById(id).orElseThrow();
        AppUser newUser = userMapper.toEntity(existingUser, request);
        AppUser savedUser = userRepository.save(newUser);
        return userMapper.toResponse(savedUser);
    }

    public UserResponse deleteUser(UUID id) {
        AppUser existingUser = userRepository.findById(id).orElseThrow();
        existingUser.setActive(false);
        AppUser deletedUser = userRepository.save(existingUser);
        return userMapper.toResponse(deletedUser);
    }
}
