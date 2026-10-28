package com.tmc.attendance.user;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tmc.attendance.user.dto.CreateUserRequest;
import com.tmc.attendance.user.dto.UpdateUserRequest;
import com.tmc.attendance.user.dto.UserResponse;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public UserResponse createUser(@RequestBody @Valid CreateUserRequest request) {
        return userService.createUser(request);
    }

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable UUID id) {
        return userService.getUser(id);
    }

    @GetMapping
    public List<UserResponse> getUsers() {
        return userService.getUsers();
    }

    @PutMapping("/{id}")
    public UserResponse updateUser(@PathVariable UUID id, @RequestBody @Valid UpdateUserRequest request) {
        return userService.updateUser(id, request);
    }

    @DeleteMapping("/{id}")
    public UserResponse deleteUser(@PathVariable UUID id) {
        return userService.deleteUser(id);
    }
}
