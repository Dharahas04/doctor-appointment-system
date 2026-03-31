package com.app.doctorappointment.service;

import com.app.doctorappointment.exception.AppointmentConflictException;
import com.app.doctorappointment.exception.ResourceNotFoundException;
import com.app.doctorappointment.model.Appointment;
import com.app.doctorappointment.model.Doctor;
import com.app.doctorappointment.model.Slot;
import com.app.doctorappointment.repository.AppointmentRepository;
import com.app.doctorappointment.repository.SlotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private SlotRepository slotRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private RedisLockService redisLockService;

    private AppointmentService appointmentService;

    @BeforeEach
    void setUp() {
        appointmentService = new AppointmentService(slotRepository, appointmentRepository, redisLockService);
    }

    @Test
    void bookThrowsConflictWhenIdempotencyKeyAlreadyExists() {
        when(appointmentRepository.findByIdempotencyKey("abc123"))
                .thenReturn(Optional.of(new Appointment()));

        AppointmentConflictException ex = assertThrows(AppointmentConflictException.class,
                () -> appointmentService.book(1L, 101L, "abc123"));

        assertEquals("This Idempotency-Key has already been used.", ex.getMessage());
    }

    @Test
    void bookThrowsNotFoundWhenSlotDoesNotExist() {
        when(appointmentRepository.findByIdempotencyKey("abc123")).thenReturn(Optional.empty());
        when(redisLockService.lock("lock:slot:1", "abc123")).thenReturn(true);
        when(slotRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> appointmentService.book(1L, 101L, "abc123"));

        assertEquals("Slot 1 was not found.", ex.getMessage());
        verify(redisLockService).unlock("lock:slot:1");
    }

    @Test
    void bookThrowsConflictWhenSlotIsAlreadyBooked() {
        Slot slot = new Slot();
        slot.setStatus("BOOKED");

        when(appointmentRepository.findByIdempotencyKey("abc123")).thenReturn(Optional.empty());
        when(redisLockService.lock("lock:slot:1", "abc123")).thenReturn(true);
        when(slotRepository.findById(1L)).thenReturn(Optional.of(slot));

        AppointmentConflictException ex = assertThrows(AppointmentConflictException.class,
                () -> appointmentService.book(1L, 101L, "abc123"));

        assertEquals("Slot 1 is not available.", ex.getMessage());
        verify(redisLockService).unlock("lock:slot:1");
    }

    @Test
    void bookMarksSlotBookedAndSavesAppointment() {
        Doctor doctor = new Doctor();
        Slot slot = new Slot();
        slot.setDoctor(doctor);
        slot.setStatus("AVAILABLE");

        when(appointmentRepository.findByIdempotencyKey("abc123")).thenReturn(Optional.empty());
        when(redisLockService.lock("lock:slot:1", "abc123")).thenReturn(true);
        when(slotRepository.findById(1L)).thenReturn(Optional.of(slot));

        String result = appointmentService.book(1L, 101L, "abc123");

        ArgumentCaptor<Appointment> appointmentCaptor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentRepository).save(appointmentCaptor.capture());
        verify(slotRepository).save(slot);
        verify(redisLockService).unlock("lock:slot:1");

        Appointment savedAppointment = appointmentCaptor.getValue();
        assertEquals("SUCCESS", result);
        assertEquals("BOOKED", slot.getStatus());
        assertEquals(doctor, savedAppointment.getDoctor());
        assertEquals(slot, savedAppointment.getSlot());
        assertEquals(101L, savedAppointment.getPatientId());
        assertEquals("CONFIRMED", savedAppointment.getStatus());
        assertEquals("abc123", savedAppointment.getIdempotencyKey());
    }
}
