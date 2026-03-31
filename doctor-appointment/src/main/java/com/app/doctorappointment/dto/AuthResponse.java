package com.app.doctorappointment.dto;

import com.app.doctorappointment.model.Role;
import com.app.doctorappointment.model.User;

public record AuthResponse(
        Long userId,
        String fullName,
        String email,
        Role role,
        Boolean isActive,
        String token,
        String dashboardPath,
        String message) {

    public static AuthResponse fromUser(User user, String token, String message) {
        return new AuthResponse(
                user.getUserId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getIsActive(),
                token,
                user.getRole() == Role.Admin ? "/admin" : "/patient",
                message);
    }
}
