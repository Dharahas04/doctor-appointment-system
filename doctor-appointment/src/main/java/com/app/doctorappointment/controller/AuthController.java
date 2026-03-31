package com.app.doctorappointment.controller;

import com.app.doctorappointment.dto.AuthLoginRequest;
import com.app.doctorappointment.dto.AuthRegisterRequest;
import com.app.doctorappointment.dto.AuthResponse;
import com.app.doctorappointment.model.User;
import com.app.doctorappointment.security.AuthTokenService;
import com.app.doctorappointment.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthTokenService authTokenService;

    public AuthController(UserService userService, AuthTokenService authTokenService) {
        this.userService = userService;
        this.authTokenService = authTokenService;
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody AuthRegisterRequest request) {
        User user = userService.registerPatient(request);
        return AuthResponse.fromUser(
                user,
                authTokenService.createToken(user),
                "Patient registration completed successfully.");
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthLoginRequest request) {
        User user = userService.authenticateUser(request.email(), request.password());
        return AuthResponse.fromUser(
                user,
                authTokenService.createToken(user),
                user.getRole().name() + " login successful.");
    }
}
