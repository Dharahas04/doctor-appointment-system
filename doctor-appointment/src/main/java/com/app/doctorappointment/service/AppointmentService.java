package com.app.doctorappointment.service;

import com.app.doctorappointment.exception.AppointmentConflictException;
import com.app.doctorappointment.exception.ResourceNotFoundException;
import com.app.doctorappointment.model.*;
import com.app.doctorappointment.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppointmentService {

    private final SlotRepository slotRepo;
    private final AppointmentRepository appointmentRepo;
    private final RedisLockService lockService;

    public AppointmentService(SlotRepository slotRepo,
            AppointmentRepository appointmentRepo,
            RedisLockService lockService) {
        this.slotRepo = slotRepo;
        this.appointmentRepo = appointmentRepo;
        this.lockService = lockService;
    }

    @Transactional
    public String book(Long slotId, Long patientId, String requestId) {

        if (appointmentRepo.findByIdempotencyKey(requestId).isPresent()) {
            throw new AppointmentConflictException("This Idempotency-Key has already been used.");
        }

        String key = "lock:slot:" + slotId;

        if (!lockService.lock(key, requestId)) {
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
            a.setStatus("CONFIRMED");
            a.setIdempotencyKey(requestId);

            appointmentRepo.save(a);

            return "SUCCESS";

        } finally {
            lockService.unlock(key);
        }
    }
}
