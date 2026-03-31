package com.app.doctorappointment.controller;

import com.app.doctorappointment.dto.AppointmentReceiptResponse;
import com.app.doctorappointment.dto.AppointmentReservationResponse;
import com.app.doctorappointment.model.Appointment;
import com.app.doctorappointment.model.Role;
import com.app.doctorappointment.security.CurrentUser;
import com.app.doctorappointment.service.AppointmentService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService service;

    public AppointmentController(AppointmentService service) {
        this.service = service;
    }

    @PostMapping("/reserve")
    public AppointmentReservationResponse reserve(@RequestParam Long slotId,
            @RequestParam(required = false) Long patientId,
            Authentication authentication) {
        return service.reserve(slotId, resolvePatientId(authentication, patientId));
    }

    @DeleteMapping("/reserve")
    public void releaseReservation(@RequestParam Long slotId, @RequestParam String reservationToken) {
        service.releaseReservation(slotId, reservationToken);
    }

    @PostMapping("/confirm")
    public AppointmentReceiptResponse confirm(@RequestParam Long slotId,
            @RequestParam(required = false) Long patientId,
            @RequestParam String reservationToken,
            @RequestParam(required = false) String mode,
            @RequestParam(required = false) String requestId,
            Authentication authentication) {
        return service.confirm(slotId, resolvePatientId(authentication, patientId), reservationToken, requestId, mode);
    }

    @PostMapping
    public String book(@RequestParam Long slotId,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) String requestId,
            Authentication authentication) {
        return service.book(slotId, resolvePatientId(authentication, patientId), requestId);
    }

    @GetMapping
    public List<Appointment> getAppointments(@RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long doctorId,
            Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return service.getAppointments(patientId, doctorId);
        }
        CurrentUser currentUser = (CurrentUser) authentication.getPrincipal();
        if (currentUser.hasRole(Role.Patient)) {
            return service.getAppointments(currentUser.userId(), null);
        }
        return service.getAppointments(patientId, doctorId);
    }

    @PutMapping("/status")
    public Appointment updateStatus(@RequestParam Long appointmentId,
            @RequestParam String status,
            Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() != null) {
            CurrentUser currentUser = (CurrentUser) authentication.getPrincipal();
            if (currentUser.hasRole(Role.Patient)) {
                if (!"CANCELLED".equalsIgnoreCase(status)) {
                    throw new IllegalArgumentException("Patients can only cancel their own appointments.");
                }
                Appointment appointment = service.getAppointment(appointmentId);
                if (!currentUser.userId().equals(appointment.getPatientId())) {
                    throw new IllegalArgumentException("Patients can only update their own appointments.");
                }
            }
        }
        return service.updateStatus(appointmentId, status);
    }

    private Long resolvePatientId(Authentication authentication, Long patientId) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return patientId;
        }
        CurrentUser currentUser = (CurrentUser) authentication.getPrincipal();
        if (!currentUser.hasRole(Role.Patient)) {
            return patientId;
        }
        if (patientId != null && !patientId.equals(currentUser.userId())) {
            throw new IllegalArgumentException("Patients can only operate on their own appointments.");
        }
        return currentUser.userId();
    }
}
