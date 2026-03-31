package com.app.doctorappointment.controller;

import com.app.doctorappointment.dto.UserCreateRequest;
import com.app.doctorappointment.exception.ApiExceptionHandler;
import com.app.doctorappointment.exception.DuplicateResourceException;
import com.app.doctorappointment.model.Role;
import com.app.doctorappointment.model.User;
import com.app.doctorappointment.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new UserController(userService))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void createReturnsUserWithoutPasswordHash() throws Exception {
        User user = new User();
        user.setUserId(1L);
        user.setFullName("Alice");
        user.setEmail("alice@example.com");
        user.setPasswordHash("secret");
        user.setRole(Role.Patient);
        user.setIsActive(true);

        when(userService.createUser(any(UserCreateRequest.class))).thenReturn(user);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Alice",
                                  "email": "alice@example.com",
                                  "passwordHash": "secret",
                                  "role": "Patient"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void createReturnsConflictForDuplicateEmail() throws Exception {
        when(userService.createUser(any(UserCreateRequest.class)))
                .thenThrow(new DuplicateResourceException("A user with email alice@example.com already exists."));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Alice",
                                  "email": "alice@example.com",
                                  "passwordHash": "secret",
                                  "role": "Patient"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("A user with email alice@example.com already exists."));
    }
}
