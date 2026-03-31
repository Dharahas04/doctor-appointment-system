package com.app.doctorappointment.dto;

import com.app.doctorappointment.model.Role;
import com.app.doctorappointment.model.User;

import java.time.LocalDateTime;

public record UserResponse(
        Long userId,
        String fullName,
        String email,
        Role role,
        Boolean isActive,
        LocalDateTime createdAt) {

    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getUserId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getIsActive(),
                user.getCreatedAt());
    }
}
