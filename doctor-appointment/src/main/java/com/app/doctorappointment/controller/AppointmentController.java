package com.app.doctorappointment.controller;

import com.app.doctorappointment.service.AppointmentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService service;

    public AppointmentController(AppointmentService service) {
        this.service = service;
    }

    @PostMapping
    public String book(@RequestParam Long slotId,
            @RequestParam Long patientId,
            @RequestHeader("Idempotency-Key") String key) {
        return service.book(slotId, patientId, key);
    }
}