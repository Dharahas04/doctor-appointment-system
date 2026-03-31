package com.app.doctorappointment.dto;

import java.time.LocalDate;

public record DailySummaryResponse(
        LocalDate date,
        long specialties,
        long doctors,
        long patients,
        long totalAppointments,
        long confirmedAppointments,
        long completedAppointments,
        long cancelledAppointments,
        long noShowAppointments,
        long onlineAppointments,
        long offlineAppointments) {
}
