package com.app.doctorappointment.security;

import com.app.doctorappointment.model.Role;

public record CurrentUser(
        Long userId,
        String email,
        Role role) {

    public boolean hasRole(Role expectedRole) {
        return role == expectedRole;
    }
}
