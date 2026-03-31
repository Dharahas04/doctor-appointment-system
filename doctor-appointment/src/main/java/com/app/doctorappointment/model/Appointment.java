package com.app.doctorappointment.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "idempotencyKey" }))
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "slot_id")
    private Slot slot;

    private Long patientId;

    private String mode;

    private String status;

    private String idempotencyKey;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public Slot getSlot() {
        return slot;
    }

    public Long getPatientId() {
        return patientId;
    }

    public String getStatus() {
        return status;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getMode() {
        return mode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public void setSlot(Slot slot) {
        this.slot = slot;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }
}
