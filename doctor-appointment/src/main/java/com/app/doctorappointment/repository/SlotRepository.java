package com.app.doctorappointment.repository;

import com.app.doctorappointment.model.Slot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SlotRepository extends JpaRepository<Slot, Long> {
    Optional<Slot> findByIdAndStatus(Long id, String status);
    List<Slot> findByDoctorIdOrderByStartTimeAsc(Long doctorId);
    List<Slot> findByDoctorIdAndStatusIgnoreCaseOrderByStartTimeAsc(Long doctorId, String status);
}
