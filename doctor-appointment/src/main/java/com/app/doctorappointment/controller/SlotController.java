package com.app.doctorappointment.controller;

import com.app.doctorappointment.exception.ResourceNotFoundException;
import com.app.doctorappointment.model.Slot;
import com.app.doctorappointment.repository.DoctorRepository;
import com.app.doctorappointment.repository.SlotRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/slots")
public class SlotController {

    private final SlotRepository slotRepository;
    private final DoctorRepository doctorRepository;

    public SlotController(SlotRepository slotRepository, DoctorRepository doctorRepository) {
        this.slotRepository = slotRepository;
        this.doctorRepository = doctorRepository;
    }

    @GetMapping
    public List<Slot> getAll(@RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) String status) {
        if (doctorId != null && status != null && !status.isBlank()) {
            return slotRepository.findByDoctorIdAndStatusIgnoreCaseOrderByStartTimeAsc(doctorId, status.trim());
        }

        if (doctorId != null) {
            return slotRepository.findByDoctorIdOrderByStartTimeAsc(doctorId);
        }

        return slotRepository.findAll().stream()
                .filter(slot -> status == null
                        || status.isBlank()
                        || slot.getStatus() != null && status.equalsIgnoreCase(slot.getStatus()))
                .sorted(Comparator.comparing(Slot::getStartTime))
                .toList();
    }

    @PostMapping
    public Slot add(@RequestBody Slot slot) {
        if (slot.getDoctor() == null || slot.getDoctor().getId() == null) {
            throw new IllegalArgumentException("doctor.id is required to create a slot.");
        }

        slot.setDoctor(doctorRepository.findById(slot.getDoctor().getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor " + slot.getDoctor().getId() + " was not found.")));

        if (slot.getStatus() == null || slot.getStatus().isBlank()) {
            slot.setStatus("AVAILABLE");
        }

        return slotRepository.save(slot);
    }
}
