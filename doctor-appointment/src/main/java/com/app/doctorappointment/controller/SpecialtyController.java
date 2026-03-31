package com.app.doctorappointment.controller;

import com.app.doctorappointment.model.Specialty;
import com.app.doctorappointment.repository.SpecialtyRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/specialties")
public class SpecialtyController {

    private final SpecialtyRepository repo;

    public SpecialtyController(SpecialtyRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Specialty> getAll() {
        return repo.findAll();
    }

    @PostMapping
    public Specialty add(@RequestBody Specialty s) {
        return repo.save(s);
    }
}