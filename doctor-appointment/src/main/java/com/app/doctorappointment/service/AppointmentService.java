package com.app.doctorappointment.service;

import com.app.doctorappointment.dto.AppointmentReceiptResponse;
import com.app.doctorappointment.dto.AppointmentReservationResponse;
import com.app.doctorappointment.exception.AppointmentConflictException;
import com.app.doctorappointment.exception.ResourceNotFoundException;
import com.app.doctorappointment.model.*;
import com.app.doctorappointment.repository.*;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AppointmentService {

    private final SlotRepository slotRepo;
    private final AppointmentRepository appointmentRepo;
    private final RedisLockService lockService;
    private final UserService userService;
    private final NotificationService notificationService;

    public AppointmentService(SlotRepository slotRepo,
            AppointmentRepository appointmentRepo,
            RedisLockService lockService,
            UserService userService,
            NotificationService notificationService) {
        this.slotRepo = slotRepo;
        this.appointmentRepo = appointmentRepo;
        this.lockService = lockService;
        this.userService = userService;
        this.notificationService = notificationService;
    }

    @Transactional
    public String book(Long slotId, Long patientId, String requestId) {
        userService.getActivePatient(patientId);
        String dedupeKey = normalizeBookingRequestId(requestId);

        if (appointmentRepo.findByIdempotencyKey(dedupeKey).isPresent()) {
            throw new AppointmentConflictException("This booking request was already processed.");
        }

        String key = "lock:slot:" + slotId;

        if (!lockService.lock(key, dedupeKey)) {
            throw new AppointmentConflictException("This slot is already being booked. Please retry.");
        }

        try {
            Slot slot = slotRepo.findById(slotId)
                    .orElseThrow(() -> new ResourceNotFoundException("Slot " + slotId + " was not found."));

            if (!"AVAILABLE".equalsIgnoreCase(slot.getStatus())) {
                throw new AppointmentConflictException("Slot " + slotId + " is not available.");
            }

            slot.setStatus("BOOKED");
            slotRepo.save(slot);

            Appointment a = new Appointment();
            a.setDoctor(slot.getDoctor());
            a.setSlot(slot);
            a.setPatientId(patientId);
            a.setMode(resolveAppointmentMode(slot.getDoctor().getMode(), null));
            a.setStatus("CONFIRMED");
            a.setIdempotencyKey(dedupeKey);

            appointmentRepo.save(a);

            return "SUCCESS";

        } finally {
            lockService.unlock(key);
        }
    }

    public AppointmentReservationResponse reserve(Long slotId, Long patientId) {
        userService.getActivePatient(patientId);
        Slot slot = slotRepo.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Slot " + slotId + " was not found."));

        if (!"AVAILABLE".equalsIgnoreCase(slot.getStatus())) {
            throw new AppointmentConflictException("Slot " + slotId + " is not available.");
        }

        String reservationToken = UUID.randomUUID().toString();
        String lockKey = buildLockKey(slotId);
        if (!lockService.lock(lockKey, reservationToken)) {
            throw new AppointmentConflictException("This slot is currently reserved by another patient.");
        }

        return new AppointmentReservationResponse(
                slotId,
                reservationToken,
                OffsetDateTime.now().plusMinutes(5),
                "HELD",
                "Slot reserved temporarily. Confirm booking within 5 minutes.");
    }

    @Transactional
    public AppointmentReceiptResponse confirm(Long slotId, Long patientId, String reservationToken, String requestId,
            String requestedMode) {
        User patient = userService.getActivePatient(patientId);
        String dedupeKey = normalizeConfirmRequestId(reservationToken, requestId);

        if (appointmentRepo.findByIdempotencyKey(dedupeKey).isPresent()) {
            throw new AppointmentConflictException("This booking request was already processed.");
        }

        String lockKey = buildLockKey(slotId);
        if (!lockService.isLockedBy(lockKey, reservationToken)) {
            throw new AppointmentConflictException("Your temporary slot reservation has expired. Please reserve again.");
        }

        try {
            Slot slot = slotRepo.findById(slotId)
                    .orElseThrow(() -> new ResourceNotFoundException("Slot " + slotId + " was not found."));

            if (!"AVAILABLE".equalsIgnoreCase(slot.getStatus())) {
                throw new AppointmentConflictException("Slot " + slotId + " is not available.");
            }

            Appointment appointment = new Appointment();
            appointment.setDoctor(slot.getDoctor());
            appointment.setSlot(slot);
            appointment.setPatientId(patientId);
            appointment.setMode(resolveAppointmentMode(slot.getDoctor().getMode(), requestedMode));
            appointment.setStatus("CONFIRMED");
            appointment.setIdempotencyKey(dedupeKey);

            slot.setStatus("BOOKED");
            slotRepo.save(slot);

            Appointment savedAppointment = appointmentRepo.save(appointment);

            return new AppointmentReceiptResponse(
                    savedAppointment.getId(),
                    savedAppointment.getStatus(),
                    savedAppointment.getMode(),
                    "Appointment confirmed successfully. Notification prepared for the patient.",
                    notificationService.sendConfirmation(savedAppointment, patient));
        } finally {
            lockService.unlockIfOwned(lockKey, reservationToken);
        }
    }

    public void releaseReservation(Long slotId, String reservationToken) {
        lockService.unlockIfOwned(buildLockKey(slotId), reservationToken);
    }

    @Transactional(readOnly = true)
    public List<Appointment> getAppointments(Long patientId, Long doctorId) {
        if (patientId != null) {
            return appointmentRepo.findByPatientIdOrderByIdDesc(patientId);
        }
        if (doctorId != null) {
            return appointmentRepo.findByDoctorIdOrderByIdDesc(doctorId);
        }
        return appointmentRepo.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    @Transactional(readOnly = true)
    public Appointment getAppointment(Long appointmentId) {
        return appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appointment " + appointmentId + " was not found."));
    }

    @Transactional
    public Appointment updateStatus(Long appointmentId, String status) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("status is required.");
        }

        Appointment appointment = getAppointment(appointmentId);

        String normalizedStatus = status.trim().toUpperCase();
        appointment.setStatus(normalizedStatus);

        if ("CANCELLED".equals(normalizedStatus)) {
            Slot slot = appointment.getSlot();
            slot.setStatus("AVAILABLE");
            slotRepo.save(slot);
        }

        return appointmentRepo.save(appointment);
    }

    private String buildLockKey(Long slotId) {
        return "lock:slot:" + slotId;
    }

    private String normalizeBookingRequestId(String requestId) {
        if (requestId != null && !requestId.isBlank()) {
            return requestId.trim();
        }
        return UUID.randomUUID().toString();
    }

    private String normalizeConfirmRequestId(String reservationToken, String requestId) {
        if (requestId != null && !requestId.isBlank()) {
            return requestId.trim();
        }
        if (reservationToken != null && !reservationToken.isBlank()) {
            return "confirm:" + reservationToken.trim();
        }
        return UUID.randomUUID().toString();
    }

    private String resolveAppointmentMode(String doctorMode, String requestedMode) {
        String normalizedDoctorMode = doctorMode == null ? "OFFLINE" : doctorMode.trim().toUpperCase();
        String normalizedRequestedMode = requestedMode == null || requestedMode.isBlank()
                ? "ONLINE"
                : requestedMode.trim().toUpperCase();

        if ("BOTH".equals(normalizedDoctorMode)) {
            if (!"ONLINE".equals(normalizedRequestedMode) && !"OFFLINE".equals(normalizedRequestedMode)) {
                throw new IllegalArgumentException("mode must be ONLINE or OFFLINE.");
            }
            return normalizedRequestedMode;
        }

        return normalizedDoctorMode;
    }
}
