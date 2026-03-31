package com.app.doctorappointment.controller;

import com.app.doctorappointment.exception.ResourceNotFoundException;
import com.app.doctorappointment.model.Doctor;
import com.app.doctorappointment.repository.DoctorRepository;
import com.app.doctorappointment.repository.SpecialtyRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorRepository repo;
    private final SpecialtyRepository specialtyRepository;

    public DoctorController(DoctorRepository repo, SpecialtyRepository specialtyRepository) {
        this.repo = repo;
        this.specialtyRepository = specialtyRepository;
    }

    @GetMapping
    public List<Doctor> getAll() {
        return repo.findAll();
    }

    @PostMapping
    public Doctor add(@RequestBody Doctor d) {
        if (d.getSpecialty() != null && d.getSpecialty().getId() != null) {
            d.setSpecialty(specialtyRepository.findById(d.getSpecialty().getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Specialty " + d.getSpecialty().getId() + " was not found.")));
        }
        return repo.save(d);
    }
}
