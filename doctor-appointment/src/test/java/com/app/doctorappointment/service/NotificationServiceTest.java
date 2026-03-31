package com.app.doctorappointment.service;

import com.app.doctorappointment.dto.NotificationDetailsResponse;
import com.app.doctorappointment.model.Appointment;
import com.app.doctorappointment.model.Doctor;
import com.app.doctorappointment.model.Slot;
import com.app.doctorappointment.model.Specialty;
import com.app.doctorappointment.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class NotificationServiceTest {

    private final NotificationService notificationService = new NotificationService();

    @Test
    void buildConfirmationIncludesBookingDetailsAndAppointmentTime() {
        Specialty specialty = new Specialty();
        specialty.setName("Cardiology");

        Doctor doctor = new Doctor();
        doctor.setName("Dr. Meera Nair");
        doctor.setMode("ONLINE");
        doctor.setClinicAddress("PulsePoint Clinic, MG Road, Bengaluru");
        doctor.setSpecialty(specialty);

        Slot slot = new Slot();
        slot.setDoctor(doctor);
        slot.setStartTime(LocalDateTime.of(2026, 3, 31, 18, 0));
        slot.setEndTime(LocalDateTime.of(2026, 3, 31, 18, 30));

        Appointment appointment = new Appointment();
        appointment.setDoctor(doctor);
        appointment.setSlot(slot);
        appointment.setMode("ONLINE");
        appointment.setStatus("CONFIRMED");
        appointment.setPatientId(1L);
        setPrivateField(appointment, "id", 42L);

        User patient = new User();
        patient.setFullName("Aarav Patel");
        patient.setEmail("aarav@example.com");

        NotificationDetailsResponse response = notificationService.buildConfirmation(appointment, patient);

        assertEquals("EMAIL", response.channel());
        assertEquals("aarav@example.com", response.recipient());
        assertEquals("Dr. Meera Nair", response.doctorName());
        assertEquals("Cardiology", response.specialtyName());
        assertTrue(response.subject().contains("Dr. Meera Nair"));
        assertTrue(response.subject().contains("31 Mar 2026"));
        assertTrue(response.appointmentWindow().contains("31 Mar 2026"));
        assertTrue(response.emailBody().contains("Appointment time: 31 Mar 2026"));
        assertTrue(response.emailBody().contains("Video link: https://telemed.pulsepoint.local/visit/42"));
        assertTrue(response.instructions().contains("Join the video consultation"));
        assertEquals("https://telemed.pulsepoint.local/visit/42", response.videoLink());
        assertEquals("PREVIEW_ONLY", response.deliveryStatus());
        assertNotNull(response.reminderAt());
    }

    @Test
    void sendConfirmationUsesConfiguredMailSender() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        NotificationService configuredService = new NotificationService(mailSender, true, "clinic@example.com");

        NotificationDetailsResponse response = configuredService.sendConfirmation(onlineAppointment(), patient());

        verify(mailSender).send(any(SimpleMailMessage.class));
        assertEquals("SENT", response.deliveryStatus());
        assertTrue(response.detail().contains("confirmation email has been sent"));
    }

    private Appointment onlineAppointment() {
        Specialty specialty = new Specialty();
        specialty.setName("Cardiology");

        Doctor doctor = new Doctor();
        doctor.setName("Dr. Meera Nair");
        doctor.setMode("ONLINE");
        doctor.setClinicAddress("PulsePoint Clinic, MG Road, Bengaluru");
        doctor.setSpecialty(specialty);

        Slot slot = new Slot();
        slot.setDoctor(doctor);
        slot.setStartTime(LocalDateTime.of(2026, 3, 31, 18, 0));
        slot.setEndTime(LocalDateTime.of(2026, 3, 31, 18, 30));

        Appointment appointment = new Appointment();
        appointment.setDoctor(doctor);
        appointment.setSlot(slot);
        appointment.setMode("ONLINE");
        appointment.setStatus("CONFIRMED");
        appointment.setPatientId(1L);
        setPrivateField(appointment, "id", 42L);
        return appointment;
    }

    private User patient() {
        User patient = new User();
        patient.setFullName("Aarav Patel");
        patient.setEmail("aarav@example.com");
        return patient;
    }

    private void setPrivateField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError("Unable to set field " + fieldName, ex);
        }
    }
}
