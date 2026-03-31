package com.app.doctorappointment.repository;

import com.app.doctorappointment.model.Specialty;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpecialtyRepository extends JpaRepository<Specialty, Long> {
}