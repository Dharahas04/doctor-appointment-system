package com.app.doctorappointment.service;

import com.app.doctorappointment.dto.UserCreateRequest;
import com.app.doctorappointment.dto.AuthRegisterRequest;
import com.app.doctorappointment.exception.DuplicateResourceException;
import com.app.doctorappointment.exception.ResourceNotFoundException;
import com.app.doctorappointment.model.Role;
import com.app.doctorappointment.model.User;
import com.app.doctorappointment.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User " + userId + " was not found."));
    }

    public User createUser(UserCreateRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new DuplicateResourceException("A user with email " + normalizedEmail + " already exists.");
        }

        User user = new User();
        user.setFullName(request.fullName().trim());
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(request.passwordHash()));
        user.setRole(request.role());
        user.setIsActive(request.isActive() == null ? Boolean.TRUE : request.isActive());
        return userRepository.save(user);
    }

    public User registerPatient(AuthRegisterRequest request) {
        return createUser(new UserCreateRequest(
                request.fullName(),
                request.email(),
                request.password(),
                Role.Patient,
                true));
    }

    public User authenticateUser(String email, String password) {
        String normalizedEmail = email.trim().toLowerCase();
        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new IllegalArgumentException("This account is inactive.");
        }
        if (!matchesPassword(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        return user;
    }

    public User authenticatePatient(String email, String password) {
        User user = authenticateUser(email, password);
        if (user.getRole() != Role.Patient) {
            throw new IllegalArgumentException("Please use a patient account to continue.");
        }
        return user;
    }

    public User getActivePatient(Long userId) {
        User user = getUser(userId);
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new IllegalArgumentException("User " + userId + " is inactive.");
        }
        if (user.getRole() != Role.Patient) {
            throw new IllegalArgumentException("User " + userId + " is not a patient.");
        }
        return user;
    }

    private boolean matchesPassword(String rawPassword, String storedPassword) {
        if (storedPassword == null) {
            return false;
        }
        if (storedPassword.startsWith("$2")) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }
        return storedPassword.equals(rawPassword);
    }
}
