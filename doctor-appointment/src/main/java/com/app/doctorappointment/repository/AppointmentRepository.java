package com.app.doctorappointment.repository;

import com.app.doctorappointment.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    Optional<Appointment> findByIdempotencyKey(String key);
}