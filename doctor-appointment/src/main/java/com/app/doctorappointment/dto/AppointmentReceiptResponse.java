package com.app.doctorappointment.dto;

public record AppointmentReceiptResponse(
        Long appointmentId,
        String status,
        String mode,
        String message,
        NotificationDetailsResponse notification) {
}
