package com.app.doctorappointment.dto;

import java.util.List;

public record AnalyticsOverviewResponse(
        List<AnalyticsPointResponse> appointmentsByDay,
        List<AnalyticsPointResponse> appointmentsByMode,
        List<AnalyticsPointResponse> appointmentsByDoctor) {
}
