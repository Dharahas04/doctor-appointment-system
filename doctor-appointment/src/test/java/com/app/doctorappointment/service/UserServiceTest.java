package com.app.doctorappointment.service;

import com.app.doctorappointment.dto.UserCreateRequest;
import com.app.doctorappointment.exception.DuplicateResourceException;
import com.app.doctorappointment.exception.ResourceNotFoundException;
import com.app.doctorappointment.model.Role;
import com.app.doctorappointment.model.User;
import com.app.doctorappointment.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void createUserRejectsDuplicateEmail() {
        UserCreateRequest request = new UserCreateRequest(
                "Alice",
                "alice@example.com",
                "hash",
                Role.Patient,
                true);
        when(userRepository.existsByEmailIgnoreCase("alice@example.com")).thenReturn(true);

        DuplicateResourceException ex = assertThrows(DuplicateResourceException.class,
                () -> userService.createUser(request));

        assertEquals("A user with email alice@example.com already exists.", ex.getMessage());
    }

    @Test
    void createUserNormalizesEmailAndDefaultsActive() {
        User saved = new User();
        saved.setUserId(1L);
        saved.setFullName("Alice");
        saved.setEmail("alice@example.com");
        saved.setPasswordHash("hash");
        saved.setRole(Role.Patient);
        saved.setIsActive(true);

        UserCreateRequest request = new UserCreateRequest(
                " Alice ",
                "Alice@Example.com ",
                "hash",
                Role.Patient,
                null);

        when(userRepository.existsByEmailIgnoreCase("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("hash")).thenReturn("encoded-hash");
        when(userRepository.save(any(User.class))).thenReturn(saved);

        User result = userService.createUser(request);

        verify(userRepository).save(any(User.class));
        assertEquals(1L, result.getUserId());
        assertEquals("alice@example.com", result.getEmail());
        assertEquals(Role.Patient, result.getRole());
    }

    @Test
    void getActivePatientRejectsInactiveUsers() {
        User user = new User();
        user.setUserId(9L);
        user.setRole(Role.Patient);
        user.setIsActive(false);
        when(userRepository.findById(9L)).thenReturn(Optional.of(user));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.getActivePatient(9L));

        assertEquals("User 9 is inactive.", ex.getMessage());
    }

    @Test
    void getUserThrowsWhenMissing() {
        when(userRepository.findById(7L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> userService.getUser(7L));

        assertEquals("User 7 was not found.", ex.getMessage());
    }

    @Test
    void authenticatePatientRejectsInvalidPassword() {
        User user = new User();
        user.setUserId(1L);
        user.setEmail("alice@example.com");
        user.setRole(Role.Patient);
        user.setIsActive(true);
        user.setPasswordHash("$2a$mocked");

        when(userRepository.findByEmailIgnoreCase("alice@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "$2a$mocked")).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.authenticatePatient("alice@example.com", "wrong"));

        assertEquals("Invalid email or password.", ex.getMessage());
    }
}
