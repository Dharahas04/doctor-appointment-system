package com.app.doctorappointment.controller;

import com.app.doctorappointment.dto.UserCreateRequest;
import com.app.doctorappointment.dto.UserResponse;
import com.app.doctorappointment.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserResponse> getAll() {
        return userService.getAllUsers().stream()
                .map(UserResponse::fromEntity)
                .toList();
    }

    @GetMapping("/{userId}")
    public UserResponse getById(@PathVariable Long userId) {
        return UserResponse.fromEntity(userService.getUser(userId));
    }

    @PostMapping
    public UserResponse create(@Valid @RequestBody UserCreateRequest request) {
        return UserResponse.fromEntity(userService.createUser(request));
    }
}
