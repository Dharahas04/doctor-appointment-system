package com.app.doctorappointment.dto;

import java.time.OffsetDateTime;

public record AppointmentReservationResponse(
        Long slotId,
        String reservationToken,
        OffsetDateTime expiresAt,
        String status,
        String message) {
}
