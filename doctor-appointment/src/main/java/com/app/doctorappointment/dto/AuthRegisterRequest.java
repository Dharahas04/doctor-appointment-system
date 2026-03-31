package com.app.doctorappointment.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthRegisterRequest(
        @NotBlank(message = "fullName is required.")
        @Size(max = 100, message = "fullName must be at most 100 characters.")
        String fullName,

        @NotBlank(message = "email is required.")
        @Email(message = "email must be a valid email address.")
        @Size(max = 100, message = "email must be at most 100 characters.")
        String email,

        @NotBlank(message = "password is required.")
        @Size(min = 6, message = "password must be at least 6 characters.")
        String password) {
}
