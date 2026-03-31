package com.app.doctorappointment.controller;

import com.app.doctorappointment.exception.ResourceNotFoundException;
import com.app.doctorappointment.model.Slot;
import com.app.doctorappointment.repository.DoctorRepository;
import com.app.doctorappointment.repository.SlotRepository;
import org.springframework.web.bind.annotation.*;

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
    public List<Slot> getAll() {
        return slotRepository.findAll();
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
