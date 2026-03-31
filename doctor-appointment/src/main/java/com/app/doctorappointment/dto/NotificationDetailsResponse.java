package com.app.doctorappointment.dto;

import java.time.OffsetDateTime;

public record NotificationDetailsResponse(
        String channel,
        String recipient,
        String subject,
        String detail,
        String doctorName,
        String specialtyName,
        String appointmentTime,
        String appointmentWindow,
        String instructions,
        String emailBody,
        String clinicAddress,
        String videoLink,
        String deliveryStatus,
        String reminderStatus,
        OffsetDateTime reminderAt) {
}
