package com.app.doctorappointment.dto;

import com.app.doctorappointment.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(
        @NotBlank(message = "fullName is required.")
        @Size(max = 100, message = "fullName must be at most 100 characters.")
        String fullName,

        @NotBlank(message = "email is required.")
        @Email(message = "email must be a valid email address.")
        @Size(max = 100, message = "email must be at most 100 characters.")
        String email,

        @NotBlank(message = "passwordHash is required.")
        String passwordHash,

        @NotNull(message = "role is required.")
        Role role,

        Boolean isActive) {
}
