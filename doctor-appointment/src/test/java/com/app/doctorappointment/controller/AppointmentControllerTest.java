package com.app.doctorappointment.controller;

import com.app.doctorappointment.exception.ApiExceptionHandler;
import com.app.doctorappointment.exception.AppointmentConflictException;
import com.app.doctorappointment.service.AppointmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AppointmentControllerTest {

    @Mock
    private AppointmentService appointmentService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AppointmentController(appointmentService))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void returnsConflictInsteadOfInternalServerErrorForBusinessErrors() throws Exception {
        when(appointmentService.book(1L, 101L, "abc123"))
                .thenThrow(new AppointmentConflictException("Slot 1 is not available."));

        mockMvc.perform(post("/api/appointments")
                        .param("slotId", "1")
                        .param("patientId", "101")
                        .header("Idempotency-Key", "abc123"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Slot 1 is not available."))
                .andExpect(jsonPath("$.path").value("/api/appointments"));
    }
}
